package me.mrfunny.minigame.bedwars.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import me.mrfunny.minigame.bedwars.event.impl.SetGeneratorProperties;
import me.mrfunny.minigame.bedwars.instance.stage.BedwarsActiveStage;

@JsonDeserialize(using = EventActionDeserializer.class)
@JsonSerialize(using = EventActionSerializer.class)
public abstract class BedwarsEventAction {
    public enum EventType {
        SET_GENERATOR_PROPERTIES(SetGeneratorProperties.class);

        private final Class<? extends BedwarsEventAction> clazz;

        EventType(Class<? extends BedwarsEventAction> clazz) {
            this.clazz = clazz;
        }

        public Class<? extends BedwarsEventAction> getRuntimeClass() {
            return clazz;
        }
    }

    @JsonIgnore
    EventType eventType;

    public EventType getEventType() {
        return eventType;
    }

    public void apply(BedwarsActiveStage game) {}
}
