package me.mrfunny.minigame.bedwars.setup;

import me.mrfunny.minigame.common.TeamColor;
import net.minestom.server.entity.Player;

public class BedwarsSetupPlayerData {

    private final Player player;

    public BedwarsSetupPlayerData(Player player) {
        this.player = player;
    }

    public PositionTypes selectedPositionToClick;
    public TeamColor selectedTeam;
}
