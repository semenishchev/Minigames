package me.mrfunny.minigame.bedwars.setup;

import me.mrfunny.minigame.bedwars.instance.BedwarsGameTypes;
import me.mrfunny.minigame.bedwars.event.BedwarsEventData;
import me.mrfunny.minigame.bedwars.data.BedwarsGeneratorData;
import me.mrfunny.minigame.bedwars.team.BedwarsTeamData;
import me.mrfunny.minigame.common.TeamColor;
import net.minestom.server.coordinate.Pos;

import java.util.List;
import java.util.Map;
public class BedwarsMapConfig {
    public String mapName;
    public String mapBiome;
    public Pos mapMin;
    public Pos mapMax;
    public Pos lobbyPos;
    public BedwarsGameTypes gameType;
    public Map<TeamColor, BedwarsTeamData> teams;
    public Map<BedwarsGeneratorData.GeneratorType, BedwarsGeneratorData.StandardGeneratorRuntimeData> standardGeneratorPerformance;
    public List<BedwarsGeneratorData> globalGenerators;
    public List<BedwarsEventData> customEvents;
    public String predefinedEvents;
}
