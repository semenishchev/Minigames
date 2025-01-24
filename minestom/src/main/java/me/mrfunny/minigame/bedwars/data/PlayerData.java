package me.mrfunny.minigame.bedwars.data;

import me.mrfunny.minigame.bedwars.team.BedwarsTeam;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Team;

import java.lang.ref.WeakReference;

public class PlayerData {
    private final WeakReference<Player> player;
    private final String username;
    private BedwarsTeam memberOf;

    public PlayerData(Player player) {
        this.player = new WeakReference<>(player);
        this.username = player.getUsername();
    }

    public BedwarsTeam getTeam() {
        return memberOf;
    }

    public void setTeam(BedwarsTeam team) {
        BedwarsTeam memberOf = this.memberOf;
        if(memberOf != null) {
            this.memberOf = null;
            memberOf.removeMember(this);
        }
        (this.memberOf = team).addMember(this);
    }

    public Player getPlayer() {
        return player.get();
    }

    public boolean isOnline() {
        Player player = getPlayer();
        return player != null && player.isOnline();
    }

    public String getUsername() {
        return username;
    }
}
