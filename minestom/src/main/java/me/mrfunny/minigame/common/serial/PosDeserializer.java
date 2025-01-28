package me.mrfunny.minigame.common.serial;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;

import java.io.IOException;

public class PosDeserializer extends StdDeserializer<Point> {
    public PosDeserializer() {
        super(Point.class);
    }

    @Override
    public Point deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JacksonException {
        JsonNode node = parser.getCodec().readTree(parser);
        return new Pos(node.get("x").doubleValue(), node.get("y").doubleValue(), node.get("z").doubleValue(), (float) node.get("yaw").asDouble(0.0), (float) node.get("pitch").asDouble(0.0));
    }
}
