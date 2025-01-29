package me.mrfunny.minigame.deployment;

import me.mrfunny.minigame.balancer.LoadBalancerClient;
import me.mrfunny.minigame.deployment.info.DeploymentInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * Resembles a deployed server. It is the class that end software will implement.
 */
public abstract class Deployment {
    protected final DeploymentInfo deploymentInfo;
    protected LoadBalancerClient balancer;
    protected Throwable encounteredError;

    public Deployment(DeploymentInfo deploymentInfo) {
        this.deploymentInfo = deploymentInfo;
    }

    /**
     * Creates an instance in accor
     * @param subtype precise subtype of the minigame. Usually getServerType() + subtype together
     * @return instance id. Usually Deployment#getServerId() + internal id provided by this server
     */
    public abstract UUID createInstance(@NotNull String subtype, @Nullable Map<String, String> data);

    /**
     * Asks server to destroy an instance.
     * @param instanceId Instance ID to destroy
     */
    public abstract void destroyInstance(@NotNull UUID instanceId);

    /**
     * In which instance ID should be a player which will connect soon to be put
     * @param instanceId Instance ID to put player in
     * @param player Player which will conenct soon
     */
    public abstract void putPlayerIn(@NotNull UUID instanceId, @NotNull UUID player);

    /**
     * Should return to which instance a player should be put
     * @param player Player to check
     * @return Instance ID if present, null if not
     */
    public abstract @Nullable UUID getInstanceOf(UUID player);

    /**
     * Used when players are in a group (party). Used to look up an instance where there's a completely free team
     * @param subtype
     * @param playersInTeam
     * @return
     */
    public abstract UUID getAvailableInstanceOfType(@NotNull String subtype, int playersInTeam);

    /**
     * @return Player count accross all instances.
     */
    public abstract int getTotalPlayers();

    public abstract void stopServer();
    public void onLoad() {}

    public @NotNull String getServerType() {
        return this.deploymentInfo.getMinigameType();
    }

    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public void setBalancer(LoadBalancerClient balancer) {
        this.balancer = balancer;
    }

    public final void load() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.balancer.markServerStopped(encounteredError == null ? null : encounteredError.getMessage());
        }));
        try {
            this.onLoad();
            this.balancer.markServerStarted();
        } catch (Throwable t) {
            encounteredError = t;
            throw t;
        }
    }

    public void setEncounteredError(Throwable encounteredError) {
        this.encounteredError = encounteredError;
    }
}
