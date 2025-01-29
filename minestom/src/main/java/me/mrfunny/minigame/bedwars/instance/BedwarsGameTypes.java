package me.mrfunny.minigame.bedwars.instance;

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

    public int getTotalPlayers() {
        return playersInTeam * teamsCount;
    }
}
