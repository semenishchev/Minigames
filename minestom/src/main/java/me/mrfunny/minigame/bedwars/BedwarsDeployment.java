package me.mrfunny.minigame.bedwars;

import me.mrfunny.minigame.bedwars.instance.BedwarsGameTypes;
import me.mrfunny.minigame.bedwars.instance.BedwarsInstance;
import me.mrfunny.minigame.bedwars.registry.BedwarsRegistry;
import me.mrfunny.minigame.deployment.info.DeploymentInfo;
import me.mrfunny.minigame.minestom.deployment.MinigameDeployment;
import net.minestom.server.instance.IChunkLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class BedwarsDeployment extends MinigameDeployment<BedwarsInstance> {
    public BedwarsDeployment(DeploymentInfo deploymentInfo) {
        super(deploymentInfo);
        BedwarsRegistry.init();
    }

    @Override
    public BedwarsInstance createInstanceObject(@NotNull String subtype, @Nullable Map<String, Object> data) {
        BedwarsInstance bedwarsInstance;
        try {
            bedwarsInstance = new BedwarsInstance(subtype, data.get("map").toString(), data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bedwarsInstance;
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
