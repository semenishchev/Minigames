package me.mrfunny.minigame.bedwars.instance.stage;

import me.mrfunny.minigame.bedwars.instance.BedwarsInstance;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.InstanceEvent;

public class BedwarsLobby extends BedwarsStage {
    public BedwarsLobby(BedwarsInstance instance) {
        super(instance);
    }

    @Override
    public void start() {
        EventNode<InstanceEvent> handler = instance.eventNode();
        handle(PlayerBlockPlaceEvent.class, event -> {
            event.setCancelled(true);
        });
        handle(PlayerBlockInteractEvent.class, event -> {
            event.setCancelled(true);
        });
        handle(PlayerBlockBreakEvent.class, event -> {
            event.setCancelled(true);
        });
        handle(PlayerSpawnEvent.class, event -> {
            event.getPlayer().setGameMode(GameMode.ADVENTURE);
        });
    }
}
