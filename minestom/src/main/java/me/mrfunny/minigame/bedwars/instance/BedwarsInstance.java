package me.mrfunny.minigame.bedwars.instance;

import me.mrfunny.minigame.bedwars.instance.stage.BedwarsStage;
import me.mrfunny.minigame.bedwars.instance.stage.BedwarsLobby;
import me.mrfunny.minigame.bedwars.setup.BedwarsMapConfig;
import me.mrfunny.minigame.common.ChunkPerFileChunkLoader;
import me.mrfunny.minigame.minestom.deployment.MinigameDeployment;
import me.mrfunny.minigame.minestom.instance.BalancedInstance;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

public class BedwarsInstance extends BalancedInstance {
    private final BedwarsGameTypes gameType;
    private final BedwarsMapConfig mapConfig;
    private final boolean allowTeamSelector;
    private BedwarsStage gameStage;

    public BedwarsInstance(@NotNull String subtype, String map, Map<String, Object> data) throws IOException {
        super(subtype, null);
        this.gameType = BedwarsGameTypes.valueOf(subtype.toUpperCase());
        this.mapConfig = BedwarsMapConfig.read(map);
        setChunkLoader(new ChunkPerFileChunkLoader(getUniqueId(),
            MinigameDeployment.getMapWorld(BedwarsStorage.COLLECTION_NAME, map),
            false,
            DynamicRegistry.Key.of(mapConfig.mapBiome)
        ));
        this.allowTeamSelector = (Boolean) data.getOrDefault("teamSelector", false);
    }

    public boolean isAllowTeamSelector() {
        return allowTeamSelector;
    }

    public BedwarsGameTypes getGameType() {
        return gameType;
    }

    public BedwarsMapConfig getMapConfig() {
        return mapConfig;
    }

    public boolean isLobbyStage() {
        return gameStage instanceof BedwarsLobby;
    }

    public void setGameStage(BedwarsStage gameStage) {
        if(this.gameStage != null) {
            this.gameStage.end();
            this.gameStage.deregister();
        }
        if(gameStage == null) return;
        this.gameStage = gameStage;
        gameStage.register();
        gameStage.start();
    }
}
