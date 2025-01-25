package me.mrfunny.minigame.bedwars.team;

import me.mrfunny.minigame.bedwars.data.BedwarsPlayerData;
import me.mrfunny.minigame.common.TeamColor;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;

public class BedwarsTeam {
    private final Set<BedwarsPlayerData> members = new HashSet<>();
    private final TeamColor teamColor;
    private final Team minecraftTeam;

    public BedwarsTeam(TeamColor color) {
        this.teamColor = color;
        this.minecraftTeam = MinecraftServer.getTeamManager().createBuilder(color.name())
            .teamDisplayName(Component.text(color.name(), color.chatColor))
            .teamColor(color.namedColor)
            .collisionRule(TeamsPacket.CollisionRule.PUSH_OTHER_TEAMS)
            .seeInvisiblePlayers()
            .updateTeamPacket()
            .build();

    }

    public void addMember(BedwarsPlayerData playerData) {
        if(playerData.getTeam() != null) {
            throw new IllegalArgumentException("This team is already a team");
        }
        if(playerData.getTeam() != this) {
            throw new IllegalStateException("Before adding the player on the team, it should have its field set to this team");
        }
        this.minecraftTeam.addMember(playerData.getUsername());
        members.add(playerData);
    }

    public void removeMember(BedwarsPlayerData playerData) {
        if(playerData.getTeam() != null) {
            throw new IllegalArgumentException("This player exists on another team");
        }
        this.minecraftTeam.removeMember(playerData.getUsername());
        this.members.remove(playerData);
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public Set<BedwarsPlayerData> getMembers() {
        return members;
    }
}
