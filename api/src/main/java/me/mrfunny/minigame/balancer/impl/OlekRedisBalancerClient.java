package me.mrfunny.minigame.balancer.impl;

import me.mrfunny.minigame.balancer.LoadBalancerClient;
import me.mrfunny.minigame.deployment.Deployment;
import me.mrfunny.minigame.storage.StorageMap;
import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OlekRedisBalancerClient extends LoadBalancerClient {

    public OlekRedisBalancerClient(Deployment requestHandler) {
        super(requestHandler);
    }

    @Override
    public void markCanBeDisabled() {

    }

    @Override
    public void reportNewInstanceId(String subtype, UUID instanceId) {

    }

    @Override
    public void markInstanceDone(UUID instanceId) {

    }

    @Override
    public void markInstanceDestroyed(UUID instanceId, String reason) {

    }

    @Override
    public void markPlayerConnected(UUID instanceId, UUID player) {

    }

    @Override
    public void serverKeepalive() {

    }

    @Override
    public void markServerStarted() {

    }

    @Override
    public void markServerStopped(String error) {

    }

    @Override
    public void reportError(String error) {

    }

    @Override
    public void sendPlayer(UUID player, String serverId, UUID instanceId) {

    }

    @Override
    public void kickPlayer(UUID player, Component reason) {

    }

    @Override
    public List<String> getPlayerPermissionNodes(UUID player) {
        return List.of();
    }

    @Override
    public boolean isBanned(UUID player) {
        return false;
    }

    @Override
    public void banPlayer(UUID player, Duration duration, String reason) {

    }

    @Override
    public StorageMap getGlobalPlayerData(UUID player) {
        return null;
    }

    @Override
    public StorageMap getPlayerData(UUID player, String collection) {
        return null;
    }

    @Override
    public void updateGlobalPlayerData(UUID player, StorageMap data) {

    }

    @Override
    public void updatePlayerData(UUID player, String collection, StorageMap data) {

    }

    @Override
    public CompletableFuture<String> requestGameServer(UUID player, String minigameType, String subtype, Map<String, Object> data) {
        return null;
    }
}
