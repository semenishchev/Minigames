package me.mrfunny.minigame.common.util;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.ArrayList;
import java.util.HashMap;

public class ArrayBackedHashMap<K, V> extends HashMap<K, V> {
    private final Object2IntOpenHashMap<K> keyToIndex = new Object2IntOpenHashMap<>();
    private final ArrayList<K> keys = new ArrayList<>();
    private final ArrayList<V> values = new ArrayList<>();
    public ArrayBackedHashMap() {
        keyToIndex.defaultReturnValue(-1);
    }
    @Override
    public V put(K key, V value) {
        keys.add(key);
        values.add(value);
        keyToIndex.put(key, keyToIndex.size() - 1);
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        int index = keyToIndex.removeInt(key);
        if(index == -1) return null;
        keys.remove(index);
        values.remove(index);
        return super.remove(key);
    }

    @Override
    public boolean remove(Object key, Object value) {
        int index = keyToIndex.removeInt(key);
        if(index == -1) return false;
        keys.remove(index);
        values.remove(index);
        return super.remove(key, value);
    }

    public ArrayList<K> getKeys() {
        return keys;
    }

    public ArrayList<V> getValues() {
        return values;
    }
}
