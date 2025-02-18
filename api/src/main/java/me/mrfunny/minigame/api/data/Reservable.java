package me.mrfunny.minigame.api.data;

import java.util.Set;
import java.util.UUID;

public interface Reservable {
    default boolean reserveSpots(Set<UUID> players) {
        throw new UnsupportedOperationException();
    }
    default boolean supportsReservingSpots() {
        return false;
    }

    default void unreserve(UUID playersToPlay) {
        throw new UnsupportedOperationException();
    }
}
