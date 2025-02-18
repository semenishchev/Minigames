package me.mrfunny.minigame.minestom.instance;

import me.mrfunny.minigame.api.deployment.Deployment;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class BalancedInstance extends InstanceContainer {
    private static final AtomicInteger instanceCount = new AtomicInteger(0);
    private final @Nullable String subtype;
    protected final Set<UUID> reservedSpots = new HashSet<>();
    private final Deployment deployment;

    public static UUID createManagedUuid() {
        return getManagedUuid(instanceCount.getAndIncrement());
    }

    public static UUID getManagedUuid(int number) {
        return new UUID(0L, number);
    }
    public BalancedInstance(Deployment deployment, @Nullable String subtype, IChunkLoader loader) {
        super(createManagedUuid(), DimensionType.OVERWORLD, loader);
        this.subtype = subtype;
        this.deployment = deployment;
        setGenerator(null);
        setChunkSupplier(LightingChunk::new);
    }

    public boolean canAcceptMorePlayers(int amount) {
        if(getMaxPlayers() == -1) return true;
        synchronized (reservedSpots) {
            return reservedSpots.size() + amount <= getMaxPlayers();
        }
    }

    public int getMaxPlayers() {
        return -1;
    }

    public boolean reserveSpots(Set<UUID> players, Consumer<UUID> onExpire) {
        if(!canAcceptMorePlayers(players.size())) return false;
        synchronized(reservedSpots) {
            reservedSpots.addAll(players);
        }
        deployment.getScheduler().schedule(() -> {
            synchronized(reservedSpots) {
                for (UUID player : players) {
                    if(getPlayerByUuid(player) != null) continue; // player has joined
                    if(onExpire != null) onExpire.accept(player);
                    reservedSpots.remove(player);
                }
            }
        }, 3, TimeUnit.SECONDS); // kinda arbitrary but idc
        return true;
    }


    public @Nullable String getSubtype() {
        return subtype;
    }
}
