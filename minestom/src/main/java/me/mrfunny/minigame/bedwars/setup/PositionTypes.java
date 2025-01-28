package me.mrfunny.minigame.bedwars.setup;

public enum PositionTypes {
    // global positions
    MAP_MIN,
    MAP_MAX,
    LOBBY_MIN,
    LOBBY_MAX,
    LOBBY_SPAWN,

    // team positions
    TEAM_SPAWN(true),
    TEAM_CORNER_MIN(true),
    TEAM_CORNER_MAX(true),
    ITEM_SHOP(true),
    TEAM_UPGRADES(true),
    BED(true),
    TEAM_CHEST(true);

    private final boolean isTeamPosition;

    PositionTypes() {
        this.isTeamPosition = false;
    }

    PositionTypes(boolean isTeamPosition) {
        this.isTeamPosition = isTeamPosition;
    }

    public boolean isTeamPosition() {
        return isTeamPosition;
    }
}
