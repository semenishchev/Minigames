package me.mrfunny.minigame.bedwars.event;

import me.mrfunny.minigame.minestom.Main;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BedwarsEventRegistry {
    public List<BedwarsEventData> events;

    private static final Map<String, BedwarsEventRegistry> registries = new HashMap<>();
    private static File folder;
    public static void initRegistries(File folder) {
        if(!folder.exists()) {
            folder.mkdirs();
        }
        BedwarsEventRegistry.folder = folder;
        for(File file : folder.listFiles()) {
            try {
                registries.put(
                    file.getName().replace(".yml", ""),
                    Main.YAML.readValue(file, BedwarsEventRegistry.class)
                );
            } catch(Exception e) {
                Main.LOGGER.error("Failed to read YAML event registry for {}", file.getPath(), e);
            }
        }
    }

    public static BedwarsEventRegistry getRegistry(String name) {
        return registries.get(name);
    }

    public static void createRegistry(String registryName, BedwarsEventRegistry registry) throws Exception {
        registries.put(registryName, registry);
        Main.YAML.writeValue(new File(folder, registryName + ".yml"), registry);
    }

    public static Map<String, BedwarsEventRegistry> getRegistries() {
        return registries;
    }
}
