package me.mrfunny.minigame.bedwars.instance.stage;

import me.mrfunny.minigame.bedwars.instance.BedwarsInstance;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.trait.InstanceEvent;

import java.util.LinkedList;
import java.util.function.Consumer;

// todo: make it common
public abstract class GameStage {
    protected final BedwarsInstance instance;
    private final LinkedList<EventListener<? extends InstanceEvent>> registeredListeners = new LinkedList<>();

    public GameStage(BedwarsInstance instance) {
        this.instance = instance;
    }
    public abstract void start();
    public void end() {}

    public void deregister() {
        for (EventListener<? extends InstanceEvent> registeredListener : registeredListeners) {
            instance.eventNode().removeListener(registeredListener);
        }
    }

    public <T extends InstanceEvent> void handle(Class<T> eventClass, Consumer<T> listener) {
        EventListener<T> listenerObj = EventListener.of(eventClass, listener);
        registeredListeners.add(listenerObj);
        instance.eventNode().addListener(listenerObj);
    }
}
