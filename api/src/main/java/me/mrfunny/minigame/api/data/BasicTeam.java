package me.mrfunny.minigame.api.data;

import java.util.Set;
import java.util.UUID;

public interface BasicTeam extends Reservable {
    BasicTeamColor getColor();
    int getMaxPlayers();
    int getPlayersCount();
}
