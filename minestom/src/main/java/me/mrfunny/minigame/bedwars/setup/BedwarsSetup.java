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
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    public BedwarsSetup() {
        super(UUID.randomUUID(), DimensionType.OVERWORLD);
    }

    public static void init(String mapName, String worldOrSchematic) throws Exception {
        if(instance != null) return;
        instance = new BedwarsSetup();
        BedwarsSetup.mapName = mapName;
        BedwarsEventRegistry.initRegistries(new File(BedwarsStorage.COLLECTION_NAME, "events"));
        File mapConfig = MinigameDeployment.getMapConfig(BedwarsStorage.COLLECTION_NAME, mapName);
        configFile = mapConfig;
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
                    instance.setChunkLoader(new AnvilLoader(worldOrSchematic));
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
                }
                Main.LOGGER.info("Loaded {} chunks", instance.getChunks().size());
                Main.LOGGER.info("Using minigame chunk loader");
                instance.setChunkLoader(minigameChunkLoader);
                Main.LOGGER.info("Done");
            }
        }
        if(mapConfig.exists()) {
            config = Main.YAML.readValue(mapConfig, BedwarsMapConfig.class);
        }

        MinecraftServer.getInstanceManager().registerInstance(instance);
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instance);
            player.setGameMode(GameMode.CREATIVE);
            player.setRespawnPoint(new Pos(0, 100, 0));
        });
    }

    private static void registerCommands() {
        MinecraftServer.getCommandManager().register(
            new SelectModeCommand(),
            new SelectStandardEvent(),
            new SaveCommand()
        );
    }

    public static BedwarsSetup getInstance() {
        return instance;
    }

    public static class SelectModeCommand extends Command {
        public SelectModeCommand() {
            super("selectmode");
            var modeArgument = ArgumentType.Enum("mode", BedwarsGameTypes.class)
                .setSuggestionCallback((sender, context, suggestion) -> {
                    for(BedwarsGameTypes value : BedwarsGameTypes.values()) {
                        suggestion.addEntry(new SuggestionEntry(value.name()));
                    }
                });
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
                for(@NotNull Chunk chunk : instance.getChunks()) {
                    try {
                        minigameChunkLoader.saveChunk(chunk);
                    } catch(Exception e) {
                        Main.LOGGER.error("Failed to save chunk {} {}", chunk.getChunkX(), chunk.getChunkZ(), e);
                    }
                }
            });
        }
    }
}
