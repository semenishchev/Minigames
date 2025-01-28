package me.mrfunny.minigame.bedwars.event;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class EventActionDeserializer extends StdDeserializer<BedwarsEventAction> {
    protected EventActionDeserializer() {
        super(BedwarsEventAction.class);
    }

    @Override
    public BedwarsEventAction deserialize(JsonParser parser, DeserializationContext context) throws IOException, JacksonException {
        JsonNode node = parser.getCodec().readTree(parser);
        BedwarsEventAction.EventType type = BedwarsEventAction.EventType.valueOf(node.get("type").toString().toUpperCase());
        BedwarsEventAction action = context.readTreeAsValue(node.get("args"), type.getRuntimeClass());
        action.eventType = type;
        return action;
    }
}
