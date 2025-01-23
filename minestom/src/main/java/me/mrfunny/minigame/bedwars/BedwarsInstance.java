package me.mrfunny.minigame.bedwars;

import me.mrfunny.minigame.minestom.instance.BalancedInstance;
import net.minestom.server.instance.IChunkLoader;
import org.jetbrains.annotations.Nullable;

public class BedwarsInstance extends BalancedInstance {

    public BedwarsInstance(@Nullable String subtype, IChunkLoader loader) {
        super(subtype, loader);
    }

    public boolean isLobbyStage() {
        return false;
    }
}
