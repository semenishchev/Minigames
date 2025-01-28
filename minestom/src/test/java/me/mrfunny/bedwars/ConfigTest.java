package me.mrfunny.bedwars;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import me.mrfunny.minigame.bedwars.instance.BedwarsGameTypes;
import me.mrfunny.minigame.bedwars.data.BedwarsGeneratorData;

import me.mrfunny.minigame.bedwars.setup.BedwarsMapConfig;
import me.mrfunny.minigame.bedwars.team.BedwarsTeamData;
import me.mrfunny.minigame.common.TeamColor;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class ConfigTest {
    @Test
    public void testWrite() throws IOException {
        BedwarsMapConfig config = new BedwarsMapConfig();
        config.mapName = "Test";
        config.mapBiome = "minecraft:plains";
        config.gameType = BedwarsGameTypes.TRIO;
    }
}
