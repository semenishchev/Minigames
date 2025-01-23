package me.mrfunny.minigame.bedwars;

import me.mrfunny.minigame.minestom.instance.BalancedInstance;
import net.minestom.server.instance.IChunkLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BedwarsInstance extends BalancedInstance {
    private final BedwarsGameTypes gameType;
    private boolean allowTeamSelector = false;
    private boolean maintenance = false;

    public BedwarsInstance(@NotNull String subtype, IChunkLoader loader) {
        super(subtype, loader);
        this.gameType = BedwarsGameTypes.valueOf(subtype.toUpperCase());
    }

    public void setAllowTeamSelector(boolean allowTeamSelector) {
        this.allowTeamSelector = allowTeamSelector;
    }

    public boolean isLobbyStage() {
        return false;
    }
}
