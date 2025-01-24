package me.mrfunny.minigame.bedwars;

import me.mrfunny.minigame.storage.StorageKey;

public class BedwarsStorage {
    public static final String COLLECTION_NAME = "bedwars";
    public static final StorageKey<Integer> KILLS = new StorageKey<>("kills");
    public static final StorageKey<Integer> FINAL_KILLS = new StorageKey<>("finals");
    public static final StorageKey<Integer> DEATHS = new StorageKey<>("deaths");
    public static final StorageKey<Integer> BEDS_BROKEN = new StorageKey<>("beds_broken");
}
