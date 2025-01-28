package me.mrfunny.minigame.bedwars.setup;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.mrfunny.minigame.bedwars.event.BedwarsEventRegistry;
import me.mrfunny.minigame.bedwars.instance.BedwarsGameTypes;
import me.mrfunny.minigame.bedwars.instance.BedwarsStorage;
import me.mrfunny.minigame.common.ChunkPerFileChunkLoader;
import me.mrfunny.minigame.minestom.Main;
import me.mrfunny.minigame.minestom.deployment.MinigameDeployment;
import net.hollowcube.schem.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.*;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;

import java.io.*;
import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class BedwarsSetup extends InstanceContainer {
    private static BedwarsSetup instance;
    private static BedwarsMapConfig config;
    private static File configFile;
    private static String mapName;
    private static File mapWorld;
    private static ChunkPerFileChunkLoader minigameChunkLoader;
    private static Map<UUID, BedwarsSetupPlayerData> playerData = new HashMap<>();

    public BedwarsSetup() {
        super(UUID.randomUUID(), DimensionType.OVERWORLD);
    }

    public static void init(String mapName, String worldOrSchematic) throws Exception {
        if(instance != null) return;
        instance = new BedwarsSetup();
        instance.setChunkSupplier(LightingChunk::new);
        BedwarsSetup.mapName = mapName;
        BedwarsEventRegistry.initRegistries(new File(BedwarsStorage.COLLECTION_NAME, "events"));
        File mapConfig = MinigameDeployment.getMapConfig(BedwarsStorage.COLLECTION_NAME, mapName);
        configFile = mapConfig;
        if(mapConfig.exists()) {
            config = Main.YAML.readValue(mapConfig, BedwarsMapConfig.class);
        } else {
            Main.LOGGER.info("Creating an empty config");
            config = new BedwarsMapConfig();
        }
        File worldFile = MinigameDeployment.getMapWorld(BedwarsStorage.COLLECTION_NAME, mapName + ".zip");
        if(!worldFile.exists() && worldOrSchematic == null) {
            throw new IllegalArgumentException(mapName + " does not have a world yet. Provide a path in a 3rd argument to a schematic or regular mc world to use as a reference");
        }
        mapWorld = worldFile;
        minigameChunkLoader = new ChunkPerFileChunkLoader(instance.getUniqueId(), worldFile, true, Biome.PLAINS);
        AtomicReference<Point> highestPointRef = new AtomicReference<>();
        if(worldOrSchematic != null) {
            if(worldFile.exists()) {
                Main.LOGGER.warn("Loading an external world as reference, but map world already exists. Stop the server if this is a mistake");
            }
            File setupFile = new File(worldOrSchematic);
            if(!setupFile.exists()) {
                throw new FileNotFoundException("Map folder doesn't exist");
            }
            if(setupFile.isDirectory()) {
                setupInAnvil(worldOrSchematic, setupFile, highestPointRef);
            } else checker: {
                long start = System.nanoTime();
                if(setupFile.getName().endsWith(".zip")) {
                    // copy world from another setup
                    ChunkPerFileChunkLoader copyFrom = new ChunkPerFileChunkLoader(instance.getUniqueId(), setupFile, false, Biome.PLAINS);
                    instance.setChunkLoader(copyFrom);
                    copyFrom.loadAllChunks(instance);
                    break checker;
                }
                Main.LOGGER.info("Loading schematic as world");
                SchematicReader reader = new SchematicReader();
                Schematic readSchematic = reader.read(setupFile.toPath());
                Main.LOGGER.info("Reading block edits");
                Long2ObjectOpenHashMap<Map<BlockVec, Block>> edits = new Long2ObjectOpenHashMap<>();
                int[] pastedBlocks = {0};
                Point offset = readSchematic.offset();
                try {

                    readSchematic.apply(Rotation.NONE, (point, block) -> {
                        pastedBlocks[0]++;
                        edits.computeIfAbsent(CoordConversion.chunkIndex(point), e -> new HashMap<>())
                            .put(new BlockVec((point.blockX() - offset.blockX()) & 15, point.blockY(), (point.blockZ() - offset.blockZ()) & 15), block);
                        Point highestPoint = highestPointRef.get();
                        if(highestPoint == null) {
                            highestPointRef.set(point);
                            return;
                        }
                        if(point.blockY() > highestPoint.blockY()) {
                            highestPointRef.set(point);
                        }
                    });
                } catch (BufferUnderflowException ignored){}
                if(highestPointRef.get() == null) {
                    throw new IllegalStateException("Map is empty");
                }
                Main.LOGGER.info("Applied {}/{} blocks", pastedBlocks[0], readSchematic.size());
                Main.LOGGER.info("Applying schematic");
                for(Long2ObjectMap.Entry<Map<BlockVec, Block>> entry : edits.long2ObjectEntrySet()) {
                    long chunkPos = entry.getLongKey();
                    int chunkX = CoordConversion.chunkIndexGetX(chunkPos);
                    int chunkZ = CoordConversion.chunkIndexGetZ(chunkPos);
                    Chunk chunk = instance.loadChunk(chunkX, chunkZ).join();
                    for(Map.Entry<BlockVec, Block> update : entry.getValue().entrySet()) {
                        chunk.setBlock(update.getKey(), update.getValue());
                    }
                }
                Main.LOGGER.info("Loaded {} chunks", instance.getChunks().size());
                Main.LOGGER.info("Using minigame chunk loader for further edits");
                instance.setChunkLoader(minigameChunkLoader);
                Main.LOGGER.info("Done in {}ms", (System.nanoTime() - start) / 1_000_000);
            }
        } else {
            instance.setChunkLoader(minigameChunkLoader);
            Main.LOGGER.info("Using saved world");
        }

        checker: if(config.lobbyPos == null) {
            Point highestPoint = highestPointRef.get();
            if(highestPoint == null) {
                break checker;
            }
            Main.LOGGER.info("Lobby pos is not set, setting highest point at the map {}", highestPoint);
            config.lobbyPos = new Pos(highestPoint.blockX(), highestPoint.blockY() + 1, highestPoint.blockZ());
        }

        MinecraftServer.getInstanceManager().registerInstance(instance);
        GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();
        handler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instance);
            player.setGameMode(GameMode.CREATIVE);
            player.setRespawnPoint(new Pos(0, 100, 0));
        });
        handler.addListener(PlayerDisconnectEvent.class, event -> {
            playerData.remove(event.getPlayer().getUuid());
        });
        registerCommands();
    }

    private static void setupInAnvil(String worldOrSchematic, File setupFile, AtomicReference<Point> highestPointRef) {
        Main.LOGGER.info("Using an anvil loader as initial world");
        AnvilLoader chunkLoader = new AnvilLoader(worldOrSchematic);
        instance.setChunkLoader(chunkLoader);
        Main.LOGGER.info("Scanning world for not empty chunks");
        File regions = new File(setupFile, "region");
        if(!regions.exists()) {
            throw new IllegalStateException("Empty anvil world");
        }
        int minX = 0;
        int minZ = 0;
        int maxX = 0;
        int maxZ = 0;
        for (File file : regions.listFiles()) {
            String name = file.getName();
            if(!name.endsWith(".mca")) continue;
            String[] data = name.split("\\.");
            int x = Integer.parseInt(data[1]);
            int z = Integer.parseInt(data[2]);
            if(x > maxX) {
                maxX = x;
            }
            if(z > maxZ) {
                maxZ = z;
            }
            if(z < minZ) {
                minZ = z;
            }
            if(x < minX) {
                minX = x;
            }
        }
        // convert coordinates from region to chunks
        minX = minX << 5;
        minZ = minZ << 5;
        maxZ = maxZ << 5;
        maxX = maxX << 5;
        for(int x = minX; x <= maxX; x++) {
            for(int z = minZ; z <= maxZ; z++) {
                Chunk chunk = instance.loadChunk(x, z).join();
                for (Section section : chunk.getSections()) {
                    if (section.blockPalette() == null || section.blockPalette().count() <= 1) continue;
                    highestPointRef.set(new Pos(x << 4, 100, z << 4));
                    break;
                }
            }
        }
    }

    private static void registerCommands() {
        MinecraftServer.getCommandManager().register(
            new SelectModeCommand(),
            new SelectStandardEvent(),
            new SaveCommand(),
            new PosCommand(),
            new PosClickCommand()
        );
    }

    public static BedwarsSetup getInstance() {
        return instance;
    }

    public static class SelectModeCommand extends Command {
        public SelectModeCommand() {
            super("selectmode");
            var modeArgument = ArgumentType.Enum("mode", BedwarsGameTypes.class);
            addSyntax((sender, context) -> {
                BedwarsGameTypes gameType = context.get(modeArgument);
                config.gameType = gameType;
                sender.sendMessage("Selected " + gameType.name() + "(" + gameType.getBalancerName() + ")");
            }, modeArgument);
        }
    }

    public static class SelectStandardEvent extends Command {
        public SelectStandardEvent() {
            super("selectevents");
            var eventRegistryRef = ArgumentType.Word("eventregistry").setSuggestionCallback((sender, context, suggestion) -> {
                for(String key : BedwarsEventRegistry.getRegistries().keySet()) {
                    suggestion.addEntry(new SuggestionEntry(key));
                }
            });
            addSyntax((sender, context) -> {
                String reference = context.get(eventRegistryRef);
                if(BedwarsEventRegistry.getRegistry(reference) == null) {
                    sender.sendMessage(reference + " does not exist");
                    return;
                }
                sender.sendMessage("Selected " + reference + " as predefined event registry");
                config.predefinedEvents = reference;
            }, eventRegistryRef);
        }
    }

    public static class SaveCommand extends Command {
        public SaveCommand() {
            super("save");
            setDefaultExecutor((sender, context) -> {
                try {
                    Main.YAML.writeValue(configFile, config);
                } catch(Exception e) {
                    Main.LOGGER.error("Failed to save map config {}", mapName, e);
                    sender.sendMessage("Failed to save. Check console");
                }
                if(config.mapMin == null) {
                    sender.sendMessage("Saved config. Don't saving map because map minimum point is not set");
                    return;
                }
                if(config.mapMax == null) {
                    sender.sendMessage("Saved config. Don't saving map because map maximum point is not set");
                    return;
                }
                long start = System.nanoTime();

                int minChunkX = config.mapMin.chunkX();
                int minChunkZ = config.mapMin.chunkZ();
                int maxChunkX = config.mapMax.chunkX();
                int maxChunkZ = config.mapMax.chunkZ();
                try {
                    minigameChunkLoader.beginWrite();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // swaps
                if(minChunkX > maxChunkX) {
                    minChunkX = minChunkX ^ maxChunkX;
                    maxChunkX = minChunkX ^ maxChunkX;
                    minChunkX = minChunkX ^ maxChunkX;
                }
                if(minChunkZ > maxChunkZ) {
                    minChunkZ = minChunkZ ^ maxChunkZ;
                    maxChunkZ = minChunkZ ^ maxChunkZ;
                    minChunkZ = minChunkZ ^ maxChunkZ;
                }
                for(int x = minChunkX; x <= maxChunkX; x++) {
                    for(int z = minChunkZ; z <= maxChunkZ; z++) {
                        Chunk chunk = instance.loadChunk(x, z).join();
                        try {
                            minigameChunkLoader.saveChunk(chunk);
                        } catch(Exception e) {
                            Main.LOGGER.error("Failed to save chunk {} {}", chunk.getChunkX(), chunk.getChunkZ(), e);
                        }
                    }
                }
                try {
                    minigameChunkLoader.endWrite();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                sender.sendMessage("Saved");
                Main.LOGGER.info("Took {}ms to save", (System.nanoTime() - start) / 1_000_000);
            });
        }
    }

    public static class PosClickCommand extends Command {
        public PosClickCommand() {
            super("pos-click");
            var posArgument = ArgumentType.Enum("position-type", PositionTypes.class);
            addSyntax((sender, context) -> {
                if(!(sender instanceof Player player)) return;
                PositionTypes type = context.get(posArgument);
                sender.sendMessage("Click on block with a stick you want to be " + type);
            }, posArgument);
        }
    }
    public static class PosCommand extends Command {
        public PosCommand() {
            super("pos");
            var posArgument = ArgumentType.Enum("position-type", PositionTypes.class);
            addSyntax((sender, context) -> {
                if(!(sender instanceof Player player)) return;
                PositionTypes type = context.get(posArgument);
                Pos pos = player.getPosition();
                pos = new Pos(pos.blockX(), pos.blockY(), pos.blockZ(), pos.yaw(), pos.pitch());
                switch(type) {
                    case MAP_MAX -> config.mapMax = pos;
                    case MAP_MIN -> config.mapMin = pos;
                }
                sender.sendMessage("Updated position " + type + " to " + pos);
            }, posArgument);
        }
    }

    public static BedwarsSetupPlayerData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUuid(), p -> {
            return new BedwarsSetupPlayerData(player);
        });
    }
}
