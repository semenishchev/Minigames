package me.mrfunny.minigame.bedwars.setup;

import me.mrfunny.minigame.bedwars.instance.BedwarsGameTypes;
import me.mrfunny.minigame.bedwars.event.BedwarsEventData;
import me.mrfunny.minigame.bedwars.data.BedwarsGeneratorData;
import me.mrfunny.minigame.bedwars.instance.BedwarsStorage;
import me.mrfunny.minigame.bedwars.team.BedwarsTeamData;
import me.mrfunny.minigame.common.TeamColor;
import me.mrfunny.minigame.minestom.Main;
import net.minestom.server.coordinate.Pos;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
public class BedwarsMapConfig {
    public String mapName;
    public String mapBiome;
    public Pos mapMin;
    public Pos mapMax;
    public Pos lobbySpawn;
    public Pos lobbyMin;
    public Pos lobbyMax;
    public BedwarsGameTypes gameType;
    public Map<TeamColor, BedwarsTeamData> teams;
    public List<BedwarsGeneratorData> globalGenerators;
    public Map<BedwarsGeneratorData.GeneratorType, BedwarsGeneratorData.StandardGeneratorRuntimeData> customGeneratorPerformance;
    public List<BedwarsEventData> customEvents;
    public String predefinedEvents;
    public String predefinedGeneratorPerformance;

    public static BedwarsMapConfig read(String map) throws IOException {
        return Main.YAML.readValue(new File(BedwarsStorage.COLLECTION_NAME, map), BedwarsMapConfig.class);
    }
}
