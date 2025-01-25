package me.mrfunny.minigame.bedwars.instance;

import me.mrfunny.minigame.bedwars.instance.stage.GameStage;
import me.mrfunny.minigame.bedwars.instance.stage.LobbyStage;
import me.mrfunny.minigame.minestom.instance.BalancedInstance;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public class BedwarsInstance extends BalancedInstance {
    private final BedwarsGameTypes gameType;
    private boolean allowTeamSelector = false;
    private boolean maintenance = false;
    private GameStage gameStage;

    public BedwarsInstance(@NotNull String subtype, IChunkLoader loader) {
        super(subtype, loader);
        this.gameType = BedwarsGameTypes.valueOf(subtype.toUpperCase());
    }

    public void setAllowTeamSelector(boolean allowTeamSelector) {
        this.allowTeamSelector = allowTeamSelector;
    }

    public boolean isLobbyStage() {
        return gameStage instanceof LobbyStage;
    }

    public void setGameStage(GameStage gameStage) {
        if(this.gameStage != null) {
            this.gameStage.end();
            this.gameStage.deregister();
        }
        if(gameStage == null) return;
        this.gameStage = gameStage;
        gameStage.start();
    }
}
