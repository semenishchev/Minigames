package me.mrfunny.minigame.bedwars.instance.stage;

import me.mrfunny.minigame.Translations;
import me.mrfunny.minigame.bedwars.instance.BedwarsInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class BedwarsLobby extends BedwarsStage {
    private final ItemStack backToLobby = ItemStack.builder(Material.RED_BED)
        .customName(Translations.COMMON_TO_LOBBY.color(NamedTextColor.RED))
        .build();
    private final ItemStack teamSelector;
    public BedwarsLobby(BedwarsInstance instance, boolean teamSelector) {
        super(instance);
        this.teamSelector = teamSelector ? ItemStack.builder(Material.COMPASS)
            .customName(Translations.BEDWARS_TEAM_SELECTOR.color(NamedTextColor.YELLOW))
            .build() : null;
    }

    @Override
    public void start() {
        stageEventHandler.addListener(PlayerBlockPlaceEvent.class, event -> {
            event.setCancelled(true);
        });
        stageEventHandler.addListener(PlayerBlockInteractEvent.class, event -> {
            event.setCancelled(true);
        });
        stageEventHandler.addListener(PlayerBlockBreakEvent.class, event -> {
            event.setCancelled(true);
        });
        stageEventHandler.addListener(ItemDropEvent.class, event -> {
            event.setCancelled(true);
        });
        stageEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();
            player.setGameMode(GameMode.ADVENTURE);

            PlayerInventory inventory = player.getInventory();
            if(teamSelector != null) {
                inventory.addItemStack(teamSelector);
            }
            inventory.addItemStack(backToLobby);
        });
    }
}
