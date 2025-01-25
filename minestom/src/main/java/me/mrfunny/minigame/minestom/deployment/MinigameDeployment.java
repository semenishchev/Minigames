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

    public abstract T createInstanceObject(@NotNull String subtype, @Nullable Map<String, Objects> data);

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

    /**
     * Get an instance for the player where there's at least 1 place free
     * @param subtype
     * @return
     */
    @Override
    public UUID getAvailableInstanceOfType(@NotNull String subtype) {
        return null;
    }

    /**
     * Used when players are in a group (party). Used to look up an instance where there's a completely free team
     * @param subtype
     * @param playersInTeam
     * @return
     */
    @Override
    public UUID getAvailableInstanceOfType(@NotNull String subtype, int playersInTeam) {
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

    public static File getMapDataFolder(String minigame, String mapName) {
        File folder = new File(minigame + "/maps/" + mapName);
        if(!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    public static File getMapWorld(String minigame, String mapName) {
        return new File(getMapDataFolder(minigame, mapName), "chunks");
    }

    public static File getMapConfig(String minigame, String mapName) {
        return new File(getMapDataFolder(minigame, mapName), "map.yml");
    }
}
