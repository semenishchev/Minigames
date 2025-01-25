package me.mrfunny.bedwars;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import me.mrfunny.minigame.bedwars.instance.BedwarsGameTypes;
import me.mrfunny.minigame.bedwars.data.GeneratorData;

import me.mrfunny.minigame.bedwars.setup.BedwarsMapConfig;
import me.mrfunny.minigame.bedwars.team.TeamData;
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
        config.gameType = BedwarsGameTypes.TRIO;
        config.lobbyPos = new Pos(0, 64, 0);
        config.generatorPerformance = Map.of(
            GeneratorData.GeneratorType.DIAMOND, new GeneratorData.StandardGeneratorRuntimeData(GeneratorData.GeneratorType.IRON, 20, 2),
            GeneratorData.GeneratorType.EMERALD, new GeneratorData.StandardGeneratorRuntimeData(GeneratorData.GeneratorType.GOLD, 200, 1)
        );
        config.globalGenerators = List.of(
            new GeneratorData(GeneratorData.GeneratorType.DIAMOND, Pos.ZERO, true),
            new GeneratorData(GeneratorData.GeneratorType.EMERALD, Pos.ZERO.withCoord(14, 1, 1), true)
        );
        List<GeneratorData> islandGenerators = List.of(
            new GeneratorData(GeneratorData.GeneratorType.IRON, new Pos(99, 0, 99), false)
        );
        config.teams = Map.of(
            TeamColor.RED, new TeamData(TeamColor.RED, islandGenerators, new Pos(123, 0, 123), new Pos(234, 0, 234), new Pos(312, 0, 312), new Pos(456, 0, 456), new Pos(457, 0, 456), null, null, null, null)
        );
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)).findAndRegisterModules();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, config);
        System.out.println(writer.toString());
    }
}
