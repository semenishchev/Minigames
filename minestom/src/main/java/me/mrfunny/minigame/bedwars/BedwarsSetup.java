package me.mrfunny.minigame.bedwars;

import me.mrfunny.minigame.minestom.instance.BalancedInstance;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.world.DimensionType;

import java.util.UUID;

public class BedwarsSetup extends InstanceContainer {
    private static BedwarsSetup instance;
    private final String mapName;

    public BedwarsSetup(String mapName, IChunkLoader loader) {
        super(UUID.randomUUID(), DimensionType.OVERWORLD, loader);
        this.mapName = mapName;
    }

    public static void init(String mapName, String setupArg) {
        if(instance != null) return;
        instance = new BedwarsSetup(mapName, null);
        MinecraftServer.getInstanceManager().registerInstance(instance);
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instance);
            player.setGameMode(GameMode.CREATIVE);
            player.setRespawnPoint(new Pos(0, 100, 0));
        });
    }

    public static BedwarsSetup getInstance() {
        return instance;
    }
}
