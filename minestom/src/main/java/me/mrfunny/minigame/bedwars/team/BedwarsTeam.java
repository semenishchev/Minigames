package me.mrfunny.minigame.bedwars.team;

import me.mrfunny.minigame.bedwars.data.BedwarsPlayerData;
import me.mrfunny.minigame.common.TeamColor;
import me.mrfunny.minigame.api.data.BasicTeam;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BedwarsTeam implements BasicTeam {
    private final Set<BedwarsPlayerData> members = new HashSet<>();
    private final Set<UUID> reservedSpots = new HashSet<>();
    private final TeamColor teamColor;
    private final Team minecraftTeam;
    private final int maxPlayers;

    public BedwarsTeam(int maxPlayers, TeamColor color) {
        this.teamColor = color;
        this.minecraftTeam = MinecraftServer.getTeamManager().createBuilder(color.name())
            .teamDisplayName(Component.text(color.name(), color.chatColor))
            .teamColor(color.namedColor)
            .collisionRule(TeamsPacket.CollisionRule.PUSH_OTHER_TEAMS)
            .seeInvisiblePlayers()
            .updateTeamPacket()
            .build();
        this.maxPlayers = maxPlayers;
    }

    public void addMember(BedwarsPlayerData playerData) {
        if(playerData.getTeam() != null) {
            throw new IllegalArgumentException("This team is already a team");
        }
        if(playerData.getBedwarsTeam() != this) {
            throw new IllegalStateException("Before adding the player on the team, it should have its field set to this team");
        }
        this.minecraftTeam.addMember(playerData.getUsername());
        members.add(playerData);
        reservedSpots.add(playerData.getUuid());
    }

    public void removeMember(BedwarsPlayerData playerData) {
        if(playerData.getTeam() != null) {
            throw new IllegalArgumentException("This player exists on another team");
        }
        this.minecraftTeam.removeMember(playerData.getUsername());
        this.members.remove(playerData);
        this.reservedSpots.remove(playerData.getUuid());
    }

    @Override
    public TeamColor getColor() {
        return this.teamColor;
    }

    @Override
    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    @Override
    public int getPlayersCount() {
        return this.members.size();
    }

    public Set<BedwarsPlayerData> getMembers() {
        return this.members;
    }

    @Override
    public boolean reserveSpots(Set<UUID> players) {
        if(reservedSpots.size() + players.size() > maxPlayers) {
            return false;
        }
        reservedSpots.addAll(players);
        return true;
    }

    @Override
    public boolean supportsReservingSpots() {
        return true;
    }

    @Override
    public void unreserve(UUID didntJoin) {
        reservedSpots.remove(didntJoin);
    }
}
