package me.mrfunny.minigame.api.balancer;

import me.mrfunny.minigame.api.storage.StorageMap;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a global server infrastructure which can do global operations on player
 */
public interface ServerInfrastructure {
    void sendPlayer(UUID player, String serverId, UUID instanceId);
    void kickPlayer(UUID player, Component reason);
    Set<String> getPlayerPermissionNodes(UUID player);
    boolean isBanned(UUID player);
    void banPlayer(UUID player, Duration duration, String reason);
    StorageMap getGlobalPlayerData(UUID player);
    StorageMap getPlayerData(UUID player, String collection);
    void updateGlobalPlayerData(UUID player, StorageMap data);
    void updatePlayerData(UUID player, String collection, StorageMap data);
    CompletableFuture<String> requestGameServer(UUID player, String minigameType, String subtype, Map<String, String> data);
}
