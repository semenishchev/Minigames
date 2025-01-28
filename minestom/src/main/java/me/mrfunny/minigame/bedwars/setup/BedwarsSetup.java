package me.mrfunny.minigame.bedwars.setup;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.mrfunny.minigame.bedwars.data.BedwarsGeneratorData;
import me.mrfunny.minigame.bedwars.instance.BedwarsGameTypes;
import me.mrfunny.minigame.bedwars.instance.BedwarsStorage;
import me.mrfunny.minigame.bedwars.registry.BedwarsRegistry;
import me.mrfunny.minigame.bedwars.team.BedwarsTeamData;
import me.mrfunny.minigame.common.ChunkPerFileChunkLoader;
import me.mrfunny.minigame.common.TeamColor;
import me.mrfunny.minigame.common.command.AnchorCommand;
import me.mrfunny.minigame.common.command.GamemodeCommand;
import me.mrfunny.minigame.common.command.TeleportCommand;
import me.mrfunny.minigame.minestom.Main;
import me.mrfunny.minigame.minestom.deployment.MinigameDeployment;
import net.hollowcube.schem.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.*;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.BufferUnderflowException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class BedwarsSetup extends InstanceContainer {
    private static final Map<UUID, BedwarsSetupPlayerData> playerData = new HashMap<>();
    private static final @NotNull Tag<Boolean> stickTag = Tag.Boolean("centered");
    public static final String BED_ENTITY_ID = "minecraft:bed";
    private static BedwarsSetup instance;
    private static BedwarsMapConfig config;
    private static File configFile;
    private static String mapName;
    private static ChunkPerFileChunkLoader minigameChunkLoader;

    public BedwarsSetup() {
        super(UUID.randomUUID(), DimensionType.OVERWORLD);
    }

    public static void init(String mapName, String worldOrSchematic) throws Exception {
        if(instance != null) return;
        instance = new BedwarsSetup();
        instance.setChunkSupplier(LightingChunk::new);
        instance.setTimeRate(0);
        instance.setTimeSynchronizationTicks(0);
        BedwarsSetup.mapName = mapName;
        BedwarsRegistry.init();
        File mapConfig = MinigameDeployment.getMapConfig(BedwarsStorage.COLLECTION_NAME, mapName);
        configFile = mapConfig;
        if(mapConfig.exists()) {
            config = Main.YAML.readValue(mapConfig, BedwarsMapConfig.class);
        } else {
            Main.LOGGER.info("Creating an empty config");
            config = new BedwarsMapConfig();
        }
        config.mapName = mapName;
        File worldFile = MinigameDeployment.getMapWorld(BedwarsStorage.COLLECTION_NAME, mapName);
        if(!worldFile.exists() && worldOrSchematic == null) {
            throw new IllegalArgumentException(mapName + " does not have a world yet. Provide a path in a 3rd argument to a schematic or regular mc world to use as a reference");
        }
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

        checker: if(config.lobbySpawn == null) {
            Point highestPoint = highestPointRef.get();
            if(highestPoint == null) {
                break checker;
            }
            Main.LOGGER.info("Lobby pos is not set, setting highest point at the map {}", highestPoint);
            config.lobbySpawn = new Pos(highestPoint.blockX(), highestPoint.blockY() + 1, highestPoint.blockZ());
        }
        MinecraftServer.getInstanceManager().registerInstance(instance);

        registerHandlers();
        registerCommands();
    }

    private static void registerHandlers() {
        GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();
        handler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instance);
            player.setGameMode(GameMode.CREATIVE);
            player.setRespawnPoint(config.lobbySpawn != null ? config.lobbySpawn : new Pos(0, 100, 0));
            player.setPermissionLevel(4);
            player.getInventory().addItemStack(ItemStack.builder(Material.STICK)
                .customName(Component.text("Position stick", NamedTextColor.GREEN))
                .lore(Component.text("Use with /pos-click", NamedTextColor.GRAY))
                .set(stickTag, false)
                .build()
            );
            player.getInventory().addItemStack(ItemStack.builder(Material.BLAZE_ROD)
                .customName(Component.text("Centered position stick", NamedTextColor.GREEN))
                .lore(Component.text("Use with /pos-click", NamedTextColor.GRAY), Component.text("Centers the position with the block aka adds 0.5 on XZ axes and 1 on Y axis"))
                .set(stickTag, true)
                .build()
            );
        });
        handler.addListener(PlayerDisconnectEvent.class, event -> {
            playerData.remove(event.getPlayer().getUuid());
        });
        handler.addListener(PlayerBlockInteractEvent.class, event -> {
            if(event.getHand() != PlayerHand.MAIN) return;
            Player player = event.getPlayer();
            ItemStack itemClicked = player.getItemInMainHand();
            Boolean centered = itemClicked.getTag(stickTag);
            if(centered == null) return;
            event.setCancelled(true);
            Pos pos = new Pos(event.getBlockPosition());
            if(centered) {
                pos = pos.add(0.5, 1, 0.5);
            }
            BedwarsSetupPlayerData data = getPlayerData(player);
            PositionTypes posType = data.selectedPositionToClick;
            if(posType == null) return;
            data.selectedPositionToClick = null;
            if(posType.isTeamPosition()) {
                if(data.selectedTeam == null) {
                    player.sendMessage(Component.text("You have no team selected", NamedTextColor.RED));
                    return;
                }
                handleTeamPosition(posType, data.selectedTeam, pos);
                player.sendMessage("Updated " + posType + " to " + pos);
                return;
            }
            handleGlobalPosition(posType, pos);
            player.sendMessage("Updated " + posType + " to " + pos);
        });

        handler.addListener(PlayerBlockPlaceEvent.class, event -> {
            if(!isBed(event.getBlock())) return;
            for (BlockFace value : BlockFace.values()) {
                if(value == BlockFace.BOTTOM || value == BlockFace.TOP) continue;
                BlockVec neighbourPos = event.getBlockPosition().relative(value);
                Block neighbourBlock = instance.getBlock(neighbourPos);
                if(!isBed(neighbourBlock)) continue;
                event.setBlock(event.getBlock().withProperties(Map.of(
                    "facing", neighbourBlock.getProperty("facing"),
                    "part", neighbourBlock.getProperty("part").equals("foot") ? "head" : "foot"
                )));
                return;
            }
            Direction direction = MathUtils.getHorizontalDirection(event.getPlayer().getPosition().yaw());
            event.setBlock(event.getBlock().withProperties(Map.of(
                "facing", direction.name().toLowerCase(),
                "part", "foot"
            )));
        });
    }

    private static boolean isBed(Block block) {
        String blockEntityId = block.registry().blockEntity();
        return blockEntityId != null && blockEntityId.equals(BED_ENTITY_ID);
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
            new PosClickCommand(),
            new GamemodeCommand(),
            new TeleportCommand(),
            new AnchorCommand(),
            new AddGeneratorCommand(),
            new SelectTeamCommand(),
            new SelectStandardGenerators()
        );
    }

    public static BedwarsSetup getInstance() {
        return instance;
    }

    public static class SelectTeamCommand extends Command {
        public SelectTeamCommand() {
            super("selectteam");
            ArgumentEnum<TeamColor> teamArg = ArgumentType.Enum("team", TeamColor.class);
            addSyntax((sender, context) -> {
                if(!(sender instanceof Player player)) return;
                BedwarsGameTypes gameType = config.gameType;
                if(gameType == null) {
                    sender.sendMessage("Game type not selected");
                    return;
                }
                if(config.teams == null) {
                    config.teams = new HashMap<>();
                }
                if(config.teams.size() >= config.gameType.getTeamsCount()) {
                    sender.sendMessage("This game type doesn't support more teams");
                    return;
                }

                TeamColor color = context.get(teamArg);
                getPlayerData(player).selectedTeam = config.teams.computeIfAbsent(color, BedwarsTeamData::new);
                player.sendMessage(Component.text("Selected " + color.name(), color.chatColor));
            }, teamArg);
        }
    }

    public static class AddGeneratorCommand extends Command {
        public AddGeneratorCommand() {
            super("addgenerator");
            ArgumentEnum<BedwarsGeneratorData.GeneratorType> genType = ArgumentType.Enum("type", BedwarsGeneratorData.GeneratorType.class);
            addSyntax((sender, context) -> {
                if(!(sender instanceof Player player)) return;
                BedwarsGeneratorData.GeneratorType type = context.get(genType);
                BedwarsTeamData selectedTeam = getPlayerData(player).selectedTeam;
                List<BedwarsGeneratorData> generatorData;
                if(selectedTeam != null) {
                    generatorData = selectedTeam.generators;
                    if(generatorData == null) {
                        generatorData = selectedTeam.generators = new LinkedList<>();
                    }
                    player.sendMessage(Component.text("Receiver: " + selectedTeam.color.name(), selectedTeam.color.chatColor));
                } else {
                    generatorData = config.globalGenerators;
                    if(generatorData == null) {
                        generatorData = config.globalGenerators = new LinkedList<>();
                    }
                }
                BlockVec pos = new BlockVec(player.getPosition());
                BedwarsGeneratorData gen = new BedwarsGeneratorData(type, pos);
                generatorData.add(gen);
                player.sendMessage("Generator " + type + " added: " + gen.uuid);
            }, genType);
        }
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
                for(var entry : BedwarsRegistry.EVENTS.getEntries()) {
                    suggestion.addEntry(new SuggestionEntry(entry.getKey()));
                }
            });
            addSyntax((sender, context) -> {
                String reference = context.get(eventRegistryRef);
                if(BedwarsRegistry.EVENTS.get(reference) == null) {
                    sender.sendMessage(reference + " does not exist");
                    return;
                }
                sender.sendMessage("Selected " + reference + " as predefined event registry");
                config.predefinedEvents = reference;
            }, eventRegistryRef);
        }
    }

    public static class SelectStandardGenerators extends Command {
        public SelectStandardGenerators() {
            super("selectgenerators");
            var eventRegistryRef = ArgumentType.Word("generatorregistry").setSuggestionCallback((sender, context, suggestion) -> {
                for(var entry : BedwarsRegistry.GENERATOR_PERFORMANCE.getEntries()) {
                    suggestion.addEntry(new SuggestionEntry(entry.getKey()));
                }
            });
            addSyntax((sender, context) -> {
                String reference = context.get(eventRegistryRef);
                if(BedwarsRegistry.GENERATOR_PERFORMANCE.get(reference) == null) {
                    sender.sendMessage(reference + " does not exist");
                    return;
                }
                sender.sendMessage("Selected " + reference + " as predefined generator performance registry");
                config.predefinedGeneratorPerformance = reference;
            }, eventRegistryRef);
        }
    }

    public static class PosClickCommand extends Command {
        public PosClickCommand() {
            super("pos-click");
            var posArgument = ArgumentType.Enum("position-type", PositionTypes.class);
            addSyntax((sender, context) -> {
                if(!(sender instanceof Player player)) return;
                PositionTypes type = context.get(posArgument);
                getPlayerData(player).selectedPositionToClick = type;
                sender.sendMessage("Click on block with a stick you want to be " + type);
            }, posArgument);
        }
    }

    public static class PosCommand extends Command {
        public PosCommand() {
            super("pos");
            var posArgument = ArgumentType.Enum("position-type", PositionTypes.class);
            var centrate = ArgumentType.Literal("centrate");
            CommandExecutor executor = (sender, context) -> {
                if (!(sender instanceof Player player)) return;
                BedwarsSetupPlayerData data = getPlayerData(player);
                PositionTypes type = context.get(posArgument);
                Pos pos = player.getPosition();
                if (context.has(centrate)) {
                    pos = new Pos(pos.blockX() + 0.5, pos.blockY(), pos.blockZ() + 0.5, pos.yaw(), pos.pitch());
                }
                if (type.isTeamPosition()) {
                    if (data.selectedTeam == null) {
                        player.sendMessage("This is a team position and you don't have a team selected");
                        return;
                    }
                    handleTeamPosition(
                        type,
                        data.selectedTeam,
                        pos
                    );
                    sender.sendMessage("Updated position " + type + " to " + pos);
                    return;
                }

                handleGlobalPosition(type, pos);
                sender.sendMessage("Updated position " + type + " to " + pos);
            };
            addSyntax(executor, posArgument, centrate);
            addSyntax(executor, posArgument);
        }
    }

    public static void handleGlobalPosition(PositionTypes type, Pos pos) {
        switch(type) {
            case MAP_MAX -> config.mapMax = pos;
            case MAP_MIN -> config.mapMin = pos;
            case LOBBY_MIN -> config.lobbyMin = pos;
            case LOBBY_MAX -> config.lobbyMax = pos;
            case LOBBY_SPAWN -> config.lobbySpawn = pos;
        }
    }

    public static void handleTeamPosition(PositionTypes type, BedwarsTeamData data, Pos pos) {
        switch(type) {
            case TEAM_SPAWN -> data.spawnPos = pos;
            case TEAM_CORNER_MIN -> data.protectedCornerMin = pos;
            case TEAM_CORNER_MAX -> data.protectedCornerMax = pos;
            case ITEM_SHOP -> data.itemShopPos = pos;
            case TEAM_UPGRADES -> data.teamUpgradesPos = pos;
            case BED -> {
                Block block = instance.getBlock(new BlockVec(pos));
                if(!isBed(block)) {
                    instance.sendMessage(Component.text("Not a bed"));
                    return;
                }
                data.bedPos = pos;
            }
            case TEAM_CHEST -> data.teamChestPos = pos;
        }
    }

    public static class SaveCommand extends Command {
        public enum SaveType {
            TEAM, CONFIG, WORLD, ALL
        }
        public SaveCommand() {
            super("save");
            ArgumentEnum<SaveType> saveType = ArgumentType.Enum("save-type", SaveType.class);
            setDefaultExecutor((sender, context) -> {
                saveConfig(sender);
                saveMap(sender);
            });

            addSyntax((sender, context) -> {
                SaveType type = context.get(saveType);
                switch (type) {
                    case TEAM -> {
                        if(!(sender instanceof Player player)) return;
                        BedwarsSetupPlayerData data = getPlayerData(player);
                        BedwarsTeamData selectedTeam = data.selectedTeam;
                        if(selectedTeam == null) {
                            player.sendMessage("Team not selected");
                            return;
                        }
                        TeamColor color = selectedTeam.color;
                        if(color == null) {
                            player.sendMessage("Team color not selected");
                            return;
                        }
                        config.teams.put(color, selectedTeam);
                        saveConfig(sender);
                    }
                    case CONFIG -> {
                        saveConfig(sender);
                    }
                    case WORLD -> {
                        saveMap(sender);
                    }
                    case ALL -> {
                        saveConfig(sender);
                        saveMap(sender);
                    }
                }
            }, saveType);
        }

        private void saveMap(CommandSender sender) {
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
        }

        private void saveConfig(CommandSender sender) {
            try {
                Main.YAML.writeValue(configFile, config);
                sender.sendMessage("Saved config");
            } catch(Exception e) {
                Main.LOGGER.error("Failed to save map config {}", mapName, e);
                sender.sendMessage("Failed to save. Check console");
            }
        }
    }

    public static BedwarsSetupPlayerData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUuid(), p -> new BedwarsSetupPlayerData(player));
    }
}
