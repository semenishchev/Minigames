package me.mrfunny.minigame.bedwars.event;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class EventActionSerializer extends StdSerializer<BedwarsEventAction> {
    protected EventActionSerializer() {
        super(BedwarsEventAction.class);
    }

    @Override
    public void serialize(BedwarsEventAction action, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", action.eventType.name());
        gen.writeObjectField("args", action);
        gen.writeEndObject();
    }
}
