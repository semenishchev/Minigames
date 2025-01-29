package me.mrfunny.minigame.bedwars.instance;

import me.mrfunny.minigame.bedwars.instance.stage.BedwarsStage;
import me.mrfunny.minigame.bedwars.instance.stage.BedwarsLobby;
import me.mrfunny.minigame.bedwars.setup.BedwarsMapConfig;
import me.mrfunny.minigame.common.ChunkPerFileChunkLoader;
import me.mrfunny.minigame.minestom.deployment.MinigameDeployment;
import me.mrfunny.minigame.minestom.instance.BalancedInstance;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class BedwarsInstance extends BalancedInstance {
    private final BedwarsGameTypes gameType;
    private final BedwarsMapConfig mapConfig;
    private final LinkedList<Supplier<EventNode<? extends InstanceEvent>>> activeStageNodes = new LinkedList<>();
    private BedwarsStage gameStage;

    public BedwarsInstance(@NotNull BedwarsGameTypes gameType, String map, Map<String, String> data) throws IOException {
        super(gameType.name(), null);
        this.gameType = gameType;
        this.mapConfig = BedwarsMapConfig.read(map);
        setChunkLoader(new ChunkPerFileChunkLoader(getUniqueId(),
            MinigameDeployment.getMapWorld(BedwarsStorage.COLLECTION_NAME, map),
            false,
            DynamicRegistry.Key.of(mapConfig.mapBiome)
        ));
        setGameStage(new BedwarsLobby(this, Objects.equals("true", data.get("teamSelector"))));
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

    public void addActiveStageNode(Supplier<EventNode<? extends InstanceEvent>> supplier) {
        activeStageNodes.add(supplier);
    }

    public LinkedList<Supplier<EventNode<? extends InstanceEvent>>> getActiveStageNodes() {
        return activeStageNodes;
    }
}
