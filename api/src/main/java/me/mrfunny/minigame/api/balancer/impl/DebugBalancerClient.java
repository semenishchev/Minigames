package me.mrfunny.minigame.api.balancer.impl;

import me.mrfunny.minigame.api.balancer.LoadBalancerClient;
import me.mrfunny.minigame.api.deployment.Deployment;
import me.mrfunny.minigame.api.storage.StorageMap;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DebugBalancerClient extends LoadBalancerClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("Debug Balancer");

    public DebugBalancerClient(Deployment requestHandler) {
        super(requestHandler);
    }

    @Override
    public void markCanBeDisabled() {
        LOGGER.info("Marking server {} as can be disabled", this.requestHandler.getDeploymentInfo().getServerId());
    }

    @Override
    public void reportNewInstanceId(String subtype, UUID instanceId, Map<String, String> data) {
        LOGGER.info("reporting new instance");
    }

    @Override
    public void markInstanceDone(UUID instanceId) {
        LOGGER.info("Game finished in {}", instanceId);
    }

    @Override
    public void markInstanceDestroyed(UUID instanceId, String reason) {
        LOGGER.info("Instance {} destroyed, reason: {}", instanceId, reason);
    }

    @Override
    public void markPlayerConnected(UUID instanceId, UUID player) {
        LOGGER.info("Player {} connected to {}", player, instanceId);
    }

    @Override
    public void serverKeepalive() {
    }

    @Override
    public void markServerStarted() {
        LOGGER.info("Server {} started", this.requestHandler.getDeploymentInfo().getServerId());
    }

    @Override
    public void markServerStopped(String error) {
        LOGGER.info("Server {} stopped", this.requestHandler.getDeploymentInfo().getServerId());
    }

    @Override
    public void reportError(String error) {
        LOGGER.info("Error - {}", error);
    }

    @Override
    public void sendPlayer(UUID player, String serverId, UUID instanceId) {
        LOGGER.info("Sending player to {} - {}", player, serverId);
    }

    @Override
    public void kickPlayer(UUID player, Component reason) {
        LOGGER.info("Requesting player {} to be kicked", player);
    }

    @Override
    public Set<String> getPlayerPermissionNodes(UUID player) {
        return Set.of();
    }

    @Override
    public boolean isBanned(UUID player) {
        return false;
    }

    @Override
    public void banPlayer(UUID player, Duration duration, String reason) {
        LOGGER.info("Banning player to {} - {}", player, duration);
    }

    @Override
    public StorageMap getGlobalPlayerData(UUID player) {
        return new StorageMap();
    }

    @Override
    public StorageMap getPlayerData(UUID player, String collection) {
        return new StorageMap();
    }

    @Override
    public void updateGlobalPlayerData(UUID player, StorageMap data) {
        LOGGER.info("Updating global data of {} to {}", player, data);
    }

    @Override
    public void updatePlayerData(UUID player, String collection, StorageMap data) {
        LOGGER.info("Updating player data to {} - {} in collection {}", player, data, collection);
    }

    @Override
    public CompletableFuture<String> requestGameServer(UUID player, String minigameType, String subtype, Map<String, String> data) {
        return null;
    }
}
