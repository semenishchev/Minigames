package me.mrfunny.minigame.bedwars;

import me.mrfunny.minigame.common.TeamColor;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public enum BedwarsGameTypes {
    SOLO(1, 8), DUO(2, 8), TRIO(3, 4), QUADRO(4, 4), FOUR_V_FOUR(4, 2);

    private final int playersInTeam;
    private final int teamsCount;
    private final String balancerName;

    BedwarsGameTypes(int playersInTeam, int teamsCount) {
        this.playersInTeam = playersInTeam;
        this.teamsCount = teamsCount;
        this.balancerName = playersInTeam + "X" + teamsCount;
    }

    public String getBalancerName() {
        return balancerName;
    }

    public int getPlayersInTeam() {
        return playersInTeam;
    }

    public int getTeamsCount() {
        return teamsCount;
    }

    public List<TeamColor> getTeamColors() {
        return switch(this) {
            case SOLO, DUO -> List.of(TeamColor.values());
            case TRIO, QUADRO -> List.of(TeamColor.RED, TeamColor.YELLOW, TeamColor.GREEN, TeamColor.BLUE);
            case FOUR_V_FOUR -> List.of(TeamColor.RED, TeamColor.BLUE);
        };
    }
}
