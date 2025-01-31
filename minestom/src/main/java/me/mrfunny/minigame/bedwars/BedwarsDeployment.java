package me.mrfunny.minigame.bedwars;

import io.github.togar2.pvp.feature.CombatFeatureSet;
import io.github.togar2.pvp.feature.CombatFeatures;
import io.github.togar2.pvp.feature.provider.DifficultyProvider;
import io.github.togar2.pvp.utils.CombatVersion;
import me.mrfunny.minigame.bedwars.data.BedwarsPlayerData;
import me.mrfunny.minigame.bedwars.instance.BedwarsGameTypes;
import me.mrfunny.minigame.bedwars.instance.BedwarsInstance;
import me.mrfunny.minigame.bedwars.registry.BedwarsRegistry;
import me.mrfunny.minigame.deployment.info.DeploymentInfo;
import me.mrfunny.minigame.minestom.deployment.MinigameDeployment;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class BedwarsDeployment extends MinigameDeployment<BedwarsInstance> {

    private final CombatFeatureSet combatFeatures;
    private final EnumMap<BedwarsGameTypes, List<String>> availableMaps = new EnumMap<>(BedwarsGameTypes.class);
    private final EnumMap<BedwarsGameTypes, List<BedwarsInstance>> availableInstances = new EnumMap<>(BedwarsGameTypes.class);

    public BedwarsDeployment(DeploymentInfo deploymentInfo) {
        super(deploymentInfo);
        BedwarsRegistry.init();
        MinecraftServer.getConnectionManager().setPlayerProvider(BedwarsPlayerData::new);
        combatFeatures = CombatFeatures.empty()
            .version(CombatVersion.MODERN)
            .difficulty(DifficultyProvider.DEFAULT)
            .add(CombatFeatures.VANILLA_DAMAGE)
            .add(CombatFeatures.VANILLA_ATTACK)
            .add(CombatFeatures.VANILLA_ATTACK_COOLDOWN)
            .add(CombatFeatures.VANILLA_ARMOR)
            .add(CombatFeatures.VANILLA_PLAYER_STATE)
            .add(CombatFeatures.VANILLA_SWEEPING)
            .add(CombatFeatures.VANILLA_POTION)
            .add(CombatFeatures.VANILLA_BOW)
            .add(CombatFeatures.VANILLA_CRITICAL)
            .add(CombatFeatures.VANILLA_EXPLOSION)
            .add(CombatFeatures.VANILLA_EXPLOSIVE)
            .add(CombatFeatures.VANILLA_KNOCKBACK)
            .add(CombatFeatures.VANILLA_EFFECT)
            .add(CombatFeatures.VANILLA_ENCHANTMENT)
            .add(CombatFeatures.VANILLA_REGENERATION)
            .add(CombatFeatures.VANILLA_PROJECTILE_ITEM)
            .add(CombatFeatures.VANILLA_FALL)
            .add(CombatFeatures.VANILLA_EQUIPMENT)
            .build();
    }

    @Override
    public BedwarsInstance createInstanceObject(@NotNull String subtype, @Nullable Map<String, String> data) {
        BedwarsInstance bedwarsInstance;
        try {
            BedwarsGameTypes gameType = BedwarsGameTypes.valueOf(subtype.toUpperCase());
            String map = data.get("map");
            if(map == null) {
                map = availableMaps.get(gameType).getFirst();
            }
            if(map == null) {
                this.balancer.reportError("No map could be picked");
                return null;
            }
            bedwarsInstance = new BedwarsInstance(gameType, map, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bedwarsInstance.addActiveStageNode(combatFeatures::createNode);
        return bedwarsInstance;
    }

    @Override
    public @Nullable String pickRandomSubtype() {
        return BedwarsGameTypes.DUO.name();
    }

    @Override
    public @NotNull List<String> getSupportedSubtypes() {
        return Arrays.stream(BedwarsGameTypes.values())
            .map(BedwarsGameTypes::getBalancerName)
            .toList();
    }

    @Override
    public UUID getAvailableInstanceOfType(@NotNull String subtype, int playersInTeam) {
        return null;
    }
}
