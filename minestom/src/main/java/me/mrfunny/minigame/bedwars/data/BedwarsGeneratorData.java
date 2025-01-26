package me.mrfunny.minigame.bedwars.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import me.mrfunny.minigame.Translations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;

import javax.naming.Name;

public class BedwarsGeneratorData {
    public enum GeneratorType {
        IRON(Translations.BEDWARS_IRON_GENERATOR.color(NamedTextColor.WHITE)),
        GOLD(Translations.BEDWARS_GOLD_GENERATOR.color(NamedTextColor.GOLD)),
        DIAMOND(Translations.BEDWARS_DIAMOND_GENERATOR.color(NamedTextColor.AQUA)),
        EMERALD(Translations.BEDWARS_EMERALD_GENERATOR.color(NamedTextColor.DARK_GREEN));
        private final Component name;

        GeneratorType(Component name) {
            this.name = name;
        }

        public Component getName() {
            return name;
        }
    }

    public static class StandardGeneratorRuntimeData {
        @JsonIgnore
        public BedwarsGeneratorData.GeneratorType type;
        public int spawnPeriod = 20; // ticks
        public int itemsPerSpawn = 1;

        public StandardGeneratorRuntimeData() {}
        public StandardGeneratorRuntimeData(BedwarsGeneratorData.GeneratorType type, int spawnPeriod, int itemsPerSpawn) {
            this.type = type;
            this.spawnPeriod = spawnPeriod;
            this.itemsPerSpawn = itemsPerSpawn;
        }
    }

    public GeneratorType type;
    public Pos pos;

    public BedwarsGeneratorData() {}

    public BedwarsGeneratorData(GeneratorType type, Pos pos, boolean showHologram) {
        this.type = type;
        this.pos = pos;
    }
}
