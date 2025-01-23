package me.mrfunny.minigame.bedwars;

import me.mrfunny.minigame.deployment.info.DeploymentInfo;
import me.mrfunny.minigame.minestom.deployment.MinigameDeployment;
import me.mrfunny.minigame.minestom.instance.BalancedInstance;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.IChunkLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BedwarsDeployment extends MinigameDeployment {
    public BedwarsDeployment(DeploymentInfo deploymentInfo) {
        super(deploymentInfo);
    }

    @Override
    public BalancedInstance createInstanceObject(@NotNull String subtype, @Nullable Map<String, Objects> data) {
        IChunkLoader schematicChunkLoader = null; // todo
        return new BalancedInstance(subtype, schematicChunkLoader);
    }

    @Override
    public @NotNull List<String> getSupportedSubtypes() {
        return Arrays.stream(BedwarsGameTypes.values())
            .map(BedwarsGameTypes::getBalancerName)
            .toList();
    }

    @Override
    public UUID getAvailableInstanceOfType(@NotNull String subtype) {
        return super.getAvailableInstanceOfType(subtype);
    }
}
