package me.mrfunny.minigame.api.storage;

import java.util.HashMap;
import java.util.Map;

public class StorageMap {
    private final Map<String, Object> backingMap = new HashMap<>();
    public <T> StorageMap add(StorageKey<T> key, T value) {
        backingMap.put(key.getName(), value);
        return this;
    }
    public StorageMap remove(StorageKey<?> key) {
        backingMap.remove(key.getName());
        return this;
    }

    public <T> T get(StorageKey<T> key) {
        return (T) backingMap.get(key.getName());
    }

    public Map<String, Object> getBackingMap() {
        return backingMap;
    }
}
