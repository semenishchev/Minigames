package me.mrfunny.minigame.bedwars.instance;

import me.mrfunny.minigame.bedwars.instance.stage.BedwarsStage;
import me.mrfunny.minigame.bedwars.instance.stage.BedwarsLobby;
import me.mrfunny.minigame.bedwars.setup.BedwarsMapConfig;
import me.mrfunny.minigame.bedwars.team.BedwarsTeam;
import me.mrfunny.minigame.common.ChunkPerFileChunkLoader;
import me.mrfunny.minigame.common.TeamColor;
import me.mrfunny.minigame.api.deployment.Deployment;
import me.mrfunny.minigame.minestom.deployment.MinigameDeployment;
import me.mrfunny.minigame.minestom.instance.BalancedInstance;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;

public class BedwarsInstance extends BalancedInstance {
    private final BedwarsGameTypes gameType;
    private final BedwarsMapConfig mapConfig;
    private final LinkedList<Supplier<EventNode<? extends InstanceEvent>>> activeStageNodes = new LinkedList<>();
    private final boolean privateGame;
    private final Map<TeamColor, BedwarsTeam> teams = new HashMap<>();
    private final boolean teamSelector;
    private BedwarsStage gameStage;

    public BedwarsInstance(Deployment deployment, @NotNull BedwarsGameTypes gameType, String map, Map<String, String> data) throws IOException {
        super(deployment, gameType.name(), null);
        this.gameType = gameType;
        this.mapConfig = BedwarsMapConfig.read(map);
        ChunkPerFileChunkLoader chunkLoader = new ChunkPerFileChunkLoader(getUniqueId(),
            MinigameDeployment.getMapWorld(BedwarsStorage.COLLECTION_NAME, map),
            false,
            Biome.PLAINS
        );
        setChunkLoader(chunkLoader);
        chunkLoader.loadAllChunks(this);
        constructTeams();
        this.privateGame = Boolean.parseBoolean(data.get("private"));
        teamSelector = Boolean.parseBoolean(data.get("teamSelector"));
        setGameStage(new BedwarsLobby(this, teamSelector));
    }

    private void constructTeams() {

    }

    public boolean isPrivateGame() {
        return privateGame;
    }

    @Override
    public boolean canAcceptMorePlayers(int amount) {
        if(!super.canAcceptMorePlayers(amount)) return false;
        int maxTeamSize = this.gameType.getTotalPlayers();
        if((maxTeamSize - this.getPlayers().size()) < amount) return false;
        if(teamSelector) return true;
        if(isPrivateGame()) return false;
        if(this.gameType.getPlayersInTeam() < amount) return false;
        for (BedwarsTeam team : teams.values()) {
            if(maxTeamSize - team.getPlayersCount() >= amount) {
                return true;
            }
        }
        return false;
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

    public Map<TeamColor, BedwarsTeam> getTeams() {
        return this.teams;
    }

    public boolean hasTeamSelector() {
        return teamSelector;
    }

    @Override
    public int getMaxPlayers() {
        return gameType.getTotalPlayers();
    }
}
