package me.mrfunny.minigame.api.balancer;

import me.mrfunny.minigame.api.deployment.Deployment;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public abstract class LoadBalancerClient implements ServerInfrastructure {
    protected final Deployment requestHandler;

    public LoadBalancerClient(Deployment requestHandler) {
        this.requestHandler = requestHandler;
    }
    /**
     * Lets LB know that this server can be shut down, no active games
     */
    public abstract void markCanBeDisabled();

    /**
     * Lets LB know that the new instance is available
     * @param subtype Minigame subtype
     * @param instanceId instance id
     */
    public abstract void reportNewInstanceId(String subtype, UUID instanceId, Map<String, String> data);

    /**
     * Lets LB know that instance has completely cleaned up after a game and may be reused again
     * @param instanceId
     */
    public abstract void markInstanceDone(UUID instanceId);

    /**
     * Lets LB know that instance is no longer accessible by that ID
     * @param instanceId Destroyed instance
     */
    public abstract void markInstanceDestroyed(UUID instanceId, String reason);
    public abstract void markPlayerConnected(UUID instanceId, UUID player);
    public abstract void serverKeepalive();
    public abstract void markServerStarted();
    public abstract void markServerStopped(String error);

    public abstract void reportError(String error);

    public @Nullable UUID getInstanceOf(UUID player) {
        return null;
    }
}
