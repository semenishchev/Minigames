package me.mrfunny.minigame.minestom.deployment;

import me.mrfunny.minigame.deployment.Deployment;
import me.mrfunny.minigame.deployment.info.DeploymentInfo;
import me.mrfunny.minigame.minestom.instance.BalancedInstance;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class MinigameDeployment extends Deployment {
    private final Map<UUID, @NotNull UUID> playerToInstance = new HashMap<>();
    public MinigameDeployment(DeploymentInfo deploymentInfo) {
        super(deploymentInfo);
    }

    public abstract BalancedInstance createInstanceObject(@NotNull String subtype, @Nullable Map<String, Objects> data);

    @Override
    public UUID createInstance(@NotNull String subtype, @Nullable Map<String, Objects> data) {
        BalancedInstance instance = createInstanceObject(subtype, data);
        MinecraftServer.getInstanceManager().registerInstance(instance);
        return instance.getUniqueId();
    }

    @Override
    public void destroyInstance(@NotNull UUID instanceId) {
        Instance instance = MinecraftServer.getInstanceManager().getInstance(instanceId);
        if(instance == null) {
            this.balancer.reportError("Requested instance to destroy " + instanceId + " is not an active instance on this server");
            return;
        }
        for (@NotNull Player player : instance.getPlayers()) {
            player.kick("Instance unregistered");
        }
        MinecraftServer.getInstanceManager().unregisterInstance(instance);
    }

    @Override
    public void putPlayerIn(@NotNull UUID instanceId, @NotNull UUID player) {
        if (MinecraftServer.getInstanceManager().getInstance(instanceId) == null) {
            this.balancer.reportError("Requested to put " + player + " into a non existent instance " + instanceId);
            return;
        }
        playerToInstance.put(player, instanceId);
    }

    @Override
    public @Nullable UUID getInstanceOf(UUID player) {
        return playerToInstance.get(player);
    }

    @Override
    public UUID getAvailableInstanceOfType(@NotNull String subtype) {
        return null;
    }

    @Override
    public int getTotalPlayers() {
        return 0;
    }

    @Override
    public void stopServer() {
        if(MinecraftServer.isStopping()) return;
        MinecraftServer.stopCleanly();
    }

    @NotNull
    public abstract List<String> getSupportedSubtypes();
}
