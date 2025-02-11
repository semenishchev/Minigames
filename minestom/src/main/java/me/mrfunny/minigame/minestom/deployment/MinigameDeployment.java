package me.mrfunny.minigame.minestom.deployment;

import me.mrfunny.minigame.deployment.Deployment;
import me.mrfunny.minigame.deployment.info.DeploymentInfo;
import me.mrfunny.minigame.minestom.instance.BalancedInstance;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public abstract class MinigameDeployment<T extends BalancedInstance> extends Deployment {
    private final Map<UUID, @NotNull UUID> playerToInstance = new HashMap<>();
    public MinigameDeployment(DeploymentInfo deploymentInfo) {
        super(deploymentInfo);
    }

    public abstract T createInstanceObject(@NotNull String subtype, @Nullable Map<String, String> data);

    @Override
    public UUID createInstance(@Nullable String subtype, @Nullable Map<String, String> data) {
        if (subtype == null) {
            subtype = pickRandomSubtype();
        }
        if(subtype == null) {
            this.balancer.reportError("Failed to create a minigame instance of " + subtype + " on " + this.deploymentInfo.getServerId());
            return null;
        }
        BalancedInstance instance = createInstanceObject(subtype, data);
        if(instance == null) return null;
        MinecraftServer.getInstanceManager().registerInstance(instance);
        this.balancer.reportNewInstanceId(subtype, instance.getUniqueId());
        return instance.getUniqueId();
    }

    public abstract @Nullable String pickRandomSubtype();

    @Override
    public void destroyInstance(@NotNull UUID instanceId) {
        this.balancer.markInstanceDestroyed(instanceId, null);
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
    public int getTotalPlayers() {
        return MinecraftServer.getConnectionManager().getOnlinePlayerCount();
    }

    @Override
    public void stopServer() {
        if(MinecraftServer.isStopping()) return;
        for (@NotNull Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
            balancer.markInstanceDestroyed(instance.getUniqueId(), "Server stop");
        }
        MinecraftServer.stopCleanly();
    }

    @NotNull
    public abstract List<String> getSupportedSubtypes();

    public Instance getAssignedInstance(Player player) {
        return null;
    }

    public static File getMapDataFolder(String minigame, String mapName) {
        File folder = new File(minigame + "/maps/" + mapName);
        if(!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    public static File getMapWorld(String minigame, String mapName) {
        return new File(getMapDataFolder(minigame, mapName), "chunks.zip");
    }

    public static File getMapConfig(String minigame, String mapName) {
        return new File(getMapDataFolder(minigame, mapName), "map.yml");
    }
}
