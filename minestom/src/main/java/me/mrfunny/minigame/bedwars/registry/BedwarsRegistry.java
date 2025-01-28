package me.mrfunny.minigame.bedwars.registry;

import me.mrfunny.minigame.bedwars.event.BedwarsEventData;
import me.mrfunny.minigame.bedwars.instance.BedwarsStorage;
import me.mrfunny.minigame.common.FileYamlRegistry;

import java.util.*;

public class BedwarsRegistry {
    public static void init() { /* Just needed to initialize the class */ }
    public static final FileYamlRegistry<List<BedwarsEventData>> EVENTS = new FileYamlRegistry<>(BedwarsStorage.COLLECTION_NAME, "events");
    public static final FileYamlRegistry<List<BedwarsEventData>> GENERATOR_PERFORMANCE = new FileYamlRegistry<>(BedwarsStorage.COLLECTION_NAME, "generators-performance");
}
