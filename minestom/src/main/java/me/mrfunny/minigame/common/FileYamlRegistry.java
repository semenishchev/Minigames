package me.mrfunny.minigame.common;

import com.fasterxml.jackson.core.type.TypeReference;
import me.mrfunny.minigame.bedwars.instance.BedwarsStorage;
import me.mrfunny.minigame.minestom.Main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FileYamlRegistry<T> {
    private final Map<String, T> entries = new HashMap<>();
    private final File folder;

    public FileYamlRegistry(String namespace, String name) {
        File folder = new File(namespace, name);
        if(!folder.exists()) {
            folder.mkdirs();
        } else if(!folder.isDirectory()) {
            throw new RuntimeException(folder.getAbsolutePath() + " is not a directory!");
        }
        this.folder = folder;
        for(File file : folder.listFiles()) {
            try {
                entries.put(
                    file.getName().replace(".yml", ""),
                    Main.YAML.readValue(file, new TypeReference<T>() {})
                );
            } catch(Exception e) {
                Main.LOGGER.error("Failed to read YAML event registry for {}", file.getPath(), e);
            }
        }
    }

    public T get(String id) {
        return entries.get(id);
    }

    public void put(String id, T entity) {
        entries.put(id, entity);
    }

    public void save(String id, T value, boolean update) throws IOException {
        if(update) {
            entries.put(id, value);
        }
        File file = new File(folder, id + ".yml");
        Main.YAML.writeValue(file, value);
    }

    public Set<Map.Entry<String, T>> getEntries() {
        return entries.entrySet();
    }

    public void save() throws IOException {
        for (Map.Entry<String, T> entry : entries.entrySet()) {
            save(entry.getKey(), entry.getValue(), false);
        }
    }
}
