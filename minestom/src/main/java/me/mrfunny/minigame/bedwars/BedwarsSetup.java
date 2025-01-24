package me.mrfunny.minigame.bedwars;

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
import java.util.UUID;

public class BedwarsSetup extends InstanceContainer {
    private static BedwarsSetup instance;
    private final String mapName;
    private BedwarsGameTypes gameType;

    public BedwarsSetup(String mapName, IChunkLoader loader) {
        super(UUID.randomUUID(), DimensionType.OVERWORLD, loader);
        this.mapName = mapName;
    }

    public static void init(String mapName, String setupArg) {
        if(instance != null) return;
        instance = new BedwarsSetup(mapName, null);
        if(setupArg != null) {
            File setupFile = new File(setupArg);
            if(setupFile.exists() && setupFile.isDirectory()) {
                AnvilLoader loader = new AnvilLoader(setupArg);
                instance.setChunkLoader(loader);
            }
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
                gameType = context.get(modeArgument);
                sender.sendMessage("Selected " + gameType.name() + "(" + gameType.getBalancerName() + ")");
            }, modeArgument);
        }
    }
}
