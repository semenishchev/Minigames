package me.mrfunny.minigame.bedwars.instance;

import me.mrfunny.minigame.bedwars.team.BedwarsTeam;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.LightingChunk;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class BedwarsChunk extends LightingChunk {
    public Set<BedwarsTeam> containsProtectedZonesOf = new HashSet<>();
    public BedwarsChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
    }

    public void intersectsWithProtectedZone(@NotNull BedwarsTeam team) {
        containsProtectedZonesOf.add(team);
    }

    public void clearIntersection(BedwarsTeam team) {
        containsProtectedZonesOf.remove(team);
    }
}
