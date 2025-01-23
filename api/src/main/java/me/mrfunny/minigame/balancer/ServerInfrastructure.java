package me.mrfunny.minigame.balancer;

import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a global server infrastructure which can do global operations on player
 */
public interface ServerInfrastructure {
    void sendPlayer(UUID player, String serverId, String instanceId);
    void kickPlayer(UUID player, Component reason);
    List<String> getPlayerPermissionNodes(UUID player);
    boolean isBanned(UUID player);
    void banPlayer(UUID player, Duration duration, String reason);
    Map<String, Object> getGlobalPlayerData(UUID player);
    Map<String, Object> getPlayerData(UUID player, String collection);
    void updateGlobalPlayerData(UUID player, Map<String, Object> data);
    void updatePlayerData(UUID player, String collection, Map<String, Object> data);
    CompletableFuture<String> requestGameServer(UUID player, String minigameType, String subtype, Map<String, Object> data);
}
