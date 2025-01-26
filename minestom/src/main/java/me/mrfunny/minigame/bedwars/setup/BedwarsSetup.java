package me.mrfunny.minigame.bedwars.setup;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.mrfunny.minigame.bedwars.event.BedwarsEventRegistry;
import me.mrfunny.minigame.bedwars.instance.BedwarsGameTypes;
import me.mrfunny.minigame.bedwars.instance.BedwarsStorage;
import me.mrfunny.minigame.common.ChunkPerFileChunkLoader;
import me.mrfunny.minigame.minestom.Main;
import me.mrfunny.minigame.minestom.deployment.MinigameDeployment;
import net.hollowcube.schem.Rotation;
import net.hollowcube.schem.Schematic;
import net.hollowcube.schem.SchematicReader;
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
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
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
        File worldFile = MinigameDeployment.getMapWorld(BedwarsStorage.COLLECTION_NAME, mapName);
        if(!worldFile.exists()) {
            worldFile.mkdirs();
        }
        mapWorld = worldFile;
        minigameChunkLoader = new ChunkPerFileChunkLoader(instance.getUniqueId(), worldFile, true, Biome.PLAINS);
        if(worldOrSchematic != null) {
            File setupFile = new File(worldOrSchematic);
            if(!setupFile.exists()) {
                throw new FileNotFoundException("Map folder doesn't exist");
            }
            if(setupFile.isDirectory()) {
                File checker = new File(setupFile, "level.dat");
                if(checker.exists()) {
                    Main.LOGGER.info("Using an anvil loader as initial world");
                    AnvilLoader chunkLoader = new AnvilLoader(worldOrSchematic);
                    instance.setChunkLoader(chunkLoader);
                } else {
                    Main.LOGGER.info("Using minigame chunk loader for edit");
                    instance.setChunkLoader(minigameChunkLoader);
                }
            } else {
                Main.LOGGER.info("Loading schematic as world");
                SchematicReader reader = new SchematicReader();
                Schematic readSchematic;
                try(FileInputStream fis = new FileInputStream(setupFile)) {
                    readSchematic = reader.read(fis);
                }
                Main.LOGGER.info("Reading block edits");
                Long2ObjectOpenHashMap<Map<BlockVec, Block>> edits = new Long2ObjectOpenHashMap<>();
                AtomicReference<Point> highestPointRef = new AtomicReference<>();
                Point offset = readSchematic.offset();
                readSchematic.apply(Rotation.NONE, (point, block) -> {
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
                if(config.lobbyPos == null) {
                    Point highestPoint = highestPointRef.get();
                    Main.LOGGER.info("Lobby pos is not set, setting highest point at the map {}", highestPoint);
                    config.lobbyPos = new Pos(highestPoint.blockX(), highestPoint.blockY() + 1, highestPoint.blockZ());
                }
                Main.LOGGER.info("Applying schematic");
                for(Long2ObjectMap.Entry<Map<BlockVec, Block>> entry : edits.long2ObjectEntrySet()) {
                    long chunkPos = entry.getLongKey();
                    int chunkX = CoordConversion.chunkIndexGetX(chunkPos);
                    int chunkZ = CoordConversion.chunkIndexGetZ(chunkPos);
                    Chunk chunk = instance.loadChunk(chunkX, chunkZ).join();
                    for(Map.Entry<BlockVec, Block> update : entry.getValue().entrySet()) {
                        chunk.setBlock(update.getKey(), update.getValue());
                    }
//                    LightingChunk.relight(instance, List.of(chunk));
                }
                Main.LOGGER.info("Loaded {} chunks", instance.getChunks().size());
                Main.LOGGER.info("Using minigame chunk loader");
                instance.setChunkLoader(minigameChunkLoader);
                Main.LOGGER.info("Done");
            }
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
    }

    private static void registerCommands() {
        MinecraftServer.getCommandManager().register(
            new SelectModeCommand(),
            new SelectStandardEvent(),
            new SaveCommand(),
            new PosCommand()
        );
    }

    public static BedwarsSetup getInstance() {
        return instance;
    }

    public static class SelectModeCommand extends Command {
        public SelectModeCommand() {
            super("selectmode");
            var modeArgument = ArgumentType.Enum("mode", BedwarsGameTypes.class);
//                .setSuggestionCallback((sender, context, suggestion) -> {
//                    for(BedwarsGameTypes value : BedwarsGameTypes.values()) {
//                        suggestion.addEntry(new SuggestionEntry(value.name()));
//                    }
//                });
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
                    sender.sendMessage("Saved");
                } catch(Exception e) {
                    Main.LOGGER.error("Failed to save map config {}", mapName, e);
                    sender.sendMessage("Failed to save. Check console");
                }
                if(config.mapMin == null) {
                    sender.sendMessage("Don't saving map because map minimum point is not set");
                    return;
                }
                if(config.mapMax == null) {
                    sender.sendMessage("Don't saving map because map maximum point is not set");
                    return;
                }
                int minChunkX = config.mapMin.chunkX();
                int minChunkZ = config.mapMin.chunkZ();
                int maxChunkX = config.mapMax.chunkX();
                int maxChunkZ = config.mapMax.chunkZ();
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
            });
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
                pos = new Pos(pos.blockX(), pos.blockY(), pos.blockZ());
                switch(type) {

                }
                sender.sendMessage("Updated position " + type + " to " + pos);
            });
        }
    }

    public static BedwarsSetupPlayerData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUuid(), p -> {
            return new BedwarsSetupPlayerData(player);
        });
    }
}
