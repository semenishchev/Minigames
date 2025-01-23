package me.mrfunny.minigame.balancer;

import me.mrfunny.minigame.deployment.Deployment;

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
    public abstract void reportNewInstanceId(String subtype, String instanceId);

    /**
     * Lets LB know that instance has completely cleaned up after a game and may be reused again
     * @param instanceId
     */
    public abstract void markInstanceDone(String instanceId);

    /**
     * Lets LB know that instance is no longer accessible by that ID
     * @param instanceId Destroyed instance
     */
    public abstract void markInstanceDestroyed(String instanceId, String reason);
    public abstract void markPlayerConnected(String instanceId, UUID player);
    public abstract void serverKeepalive(String serverId);
    public abstract void markServerStarted(String serverId);
    public abstract void markServerStopped(String serverId, String error);

    public abstract void reportError(String error);
}
