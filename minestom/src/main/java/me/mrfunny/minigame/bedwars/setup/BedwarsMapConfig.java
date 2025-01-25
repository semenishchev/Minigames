package me.mrfunny.minigame.bedwars.setup;

import me.mrfunny.minigame.bedwars.instance.BedwarsGameTypes;
import me.mrfunny.minigame.bedwars.data.EventData;
import me.mrfunny.minigame.bedwars.data.GeneratorData;
import me.mrfunny.minigame.bedwars.team.TeamData;
import me.mrfunny.minigame.common.TeamColor;
import net.minestom.server.coordinate.Pos;

import java.util.List;
import java.util.Map;

public class BedwarsMapConfig {
    public String mapName;
    public Pos lobbyPos;
    public BedwarsGameTypes gameType;
    public Map<TeamColor, TeamData> teams;
    public Map<GeneratorData.GeneratorType, GeneratorData.StandardGeneratorRuntimeData> standardGeneratorPerformance;
    public List<GeneratorData> globalGenerators;
    public Map<String, EventData> events;
}
