package me.mrfunny.minigame.api.storage;

public class StorageKey<T> {
    private final String name;

    public StorageKey(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
