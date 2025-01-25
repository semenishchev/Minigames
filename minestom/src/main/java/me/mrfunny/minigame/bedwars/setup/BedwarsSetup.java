package me.mrfunny.minigame.bedwars.setup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import me.mrfunny.minigame.bedwars.instance.BedwarsGameTypes;
import me.mrfunny.minigame.bedwars.instance.BedwarsStorage;
import me.mrfunny.minigame.minestom.Main;
import me.mrfunny.minigame.minestom.deployment.MinigameDeployment;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.world.DimensionType;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class BedwarsSetup extends InstanceContainer {
    private static BedwarsSetup instance;
    private static ObjectMapper objectMapper;
    private final String mapName;
    private static BedwarsMapConfig config;

    public BedwarsSetup(String mapName, IChunkLoader loader) {
        super(UUID.randomUUID(), DimensionType.OVERWORLD, loader);
        this.mapName = mapName;
    }

    public static void init(String mapName, String worldOrSchematic) throws IOException {
        if(instance != null) return;
        objectMapper = new ObjectMapper(new YAMLFactory()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        ).findAndRegisterModules()
            .enable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        instance = new BedwarsSetup(mapName, null);
        File mapConfig = MinigameDeployment.getMapConfig(BedwarsStorage.COLLECTION_NAME, mapName);
        if(worldOrSchematic != null) {
            File setupFile = new File(worldOrSchematic);
            if(setupFile.exists() && setupFile.isDirectory()) {
                Main.LOGGER.info("Loading an anvil loader as initial world");
                AnvilLoader loader = new AnvilLoader(worldOrSchematic);
                instance.setChunkLoader(loader);
            }
        }
        if(mapConfig.exists()) {
            config = objectMapper.readValue(mapConfig, BedwarsMapConfig.class);
        }

        MinigameDeployment.getMapSchematic(BedwarsStorage.COLLECTION_NAME, mapName);
        MinecraftServer.getInstanceManager().registerInstance(instance);
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instance);
            player.setGameMode(GameMode.CREATIVE);
            player.setRespawnPoint(new Pos(0, 100, 0));
        });
    }

    private static void registerCommands() {
        MinecraftServer.getCommandManager().register();
    }

    private static void registerListeners() {

    }

    public static BedwarsSetup getInstance() {
        return instance;
    }

    public class SelectModeCommand extends Command {

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
                sender.sendMessage("Selected " + gameType.name() + "(" + gameType.getBalancerName() + ")");
            }, modeArgument);
        }
    }
}
