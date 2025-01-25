package me.mrfunny.minigame.bedwars.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;

public class GeneratorData {
    public enum GeneratorType {
        IRON(Component.translatable("generator.iron", "Iron", NamedTextColor.WHITE)),
        GOLD(Component.translatable("generator.gold", "Gold", NamedTextColor.GOLD)),
        DIAMOND(Component.translatable("generator.diamond", "Diamond", NamedTextColor.AQUA)),
        EMERALD(Component.translatable("generator.emerald", "Emerald", NamedTextColor.DARK_GREEN)),;
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
        public GeneratorData.GeneratorType type;
        public int spawnPeriod = 20; // ticks
        public int itemsPerSpawn = 1;

        public StandardGeneratorRuntimeData() {}
        public StandardGeneratorRuntimeData(GeneratorData.GeneratorType type, int spawnPeriod, int itemsPerSpawn) {
            this.type = type;
            this.spawnPeriod = spawnPeriod;
            this.itemsPerSpawn = itemsPerSpawn;
        }
    }

    public GeneratorType type;
    public Pos pos;

    public GeneratorData() {}

    public GeneratorData(GeneratorType type, Pos pos, boolean showHologram) {
        this.type = type;
        this.pos = pos;
    }
}
