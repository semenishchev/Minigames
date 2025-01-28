package me.mrfunny.minigame.bedwars.instance.stage;

import me.mrfunny.minigame.bedwars.instance.BedwarsInstance;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.InstanceEvent;

import java.util.LinkedList;
import java.util.function.Consumer;

// todo: make it common
public abstract class BedwarsStage {
    protected final BedwarsInstance instance;
    private final LinkedList<EventListener<? extends InstanceEvent>> registeredListeners = new LinkedList<>();
    protected final EventNode<InstanceEvent> stageEventHandler;

    public BedwarsStage(BedwarsInstance instance) {
        this.instance = instance;
        stageEventHandler = EventNode.type(getClass().getSimpleName(), EventFilter.INSTANCE, ((instanceEvent, given) -> {
            boolean flag = given == this.instance;
            if(!flag) {
                System.out.println("I WS RIGHT"); // todo: remove this, idk how child nodes work or even if this check is needed
            }
            return flag;
        }));
    }
    public abstract void start();
    public void end() {}

    public void register() {
        this.instance.eventNode().addChild(this.stageEventHandler);
    }
    public void deregister() {
        this.instance.eventNode().removeChild(this.stageEventHandler);
    }

    public BedwarsInstance getInstance() {
        return instance;
    }
}
