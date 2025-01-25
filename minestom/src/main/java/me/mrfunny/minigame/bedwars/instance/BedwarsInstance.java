package me.mrfunny.minigame.bedwars.instance;

import me.mrfunny.minigame.bedwars.instance.stage.BedwarsStage;
import me.mrfunny.minigame.bedwars.instance.stage.BedwarsLobby;
import me.mrfunny.minigame.minestom.instance.BalancedInstance;
import net.minestom.server.instance.IChunkLoader;
import org.jetbrains.annotations.NotNull;

public class BedwarsInstance extends BalancedInstance {
    private final BedwarsGameTypes gameType;
    private boolean allowTeamSelector = false;
    private boolean maintenance = false;
    private BedwarsStage gameStage;

    public BedwarsInstance(@NotNull String subtype, IChunkLoader loader) {
        super(subtype, loader);
        this.gameType = BedwarsGameTypes.valueOf(subtype.toUpperCase());
    }

    public void setAllowTeamSelector(boolean allowTeamSelector) {
        this.allowTeamSelector = allowTeamSelector;
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
        gameStage.start();
    }
}
