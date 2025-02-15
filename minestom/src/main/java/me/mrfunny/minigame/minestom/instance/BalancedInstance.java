package me.mrfunny.minigame.minestom.instance;

import me.mrfunny.minigame.deployment.Deployment;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BalancedInstance extends InstanceContainer {
    private static final AtomicInteger instanceCount = new AtomicInteger(0);
    private final @Nullable String subtype;
    private final AtomicInteger reservedSpots = new AtomicInteger(0);
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
        return getMaxPlayers() <= reservedSpots.get() + amount;
    }

    public int getMaxPlayers() {
        return 0;
    }

    public boolean reserveSpots(int amount) {
        if(!canAcceptMorePlayers(amount)) return false;
        synchronized(reservedSpots) {
            reservedSpots.addAndGet(amount);
        }
        deployment.getScheduler().schedule(() -> {
            synchronized(reservedSpots) {
                reservedSpots.addAndGet(-amount);
            }
        }, 3, TimeUnit.SECONDS); // kinda arbitrary but idc
        return true;
    }


    public @Nullable String getSubtype() {
        return subtype;
    }
}
