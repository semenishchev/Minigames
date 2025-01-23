package me.mrfunny.minigame.minestom.instance;

import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class BalancedInstance extends InstanceContainer {
    private static final AtomicInteger instanceCount = new AtomicInteger(0);
    private final @Nullable String subtype;

    public static UUID createManagedUuid() {
        return getManagedUuid(instanceCount.getAndIncrement());
    }

    public static UUID getManagedUuid(int number) {
        return new UUID(0L, number);
    }
    public BalancedInstance(@Nullable String subtype, IChunkLoader loader) {
        super(createManagedUuid(), DimensionType.OVERWORLD, loader);
        this.subtype = subtype;
        setGenerator(null);
        setChunkSupplier(LightingChunk::new);
    }

    public @Nullable String getSubtype() {
        return subtype;
    }
}
