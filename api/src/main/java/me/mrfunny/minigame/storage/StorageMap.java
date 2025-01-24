package me.mrfunny.minigame.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
