package me.mrfunny.minigame.minestom.serial;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;

import java.io.IOException;

public class PosSerializer extends StdSerializer<Point> {
    public PosSerializer() {
        super(Point.class);
    }

    @Override
    public void serialize(Point point, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("x", point.x());
        gen.writeNumberField("y", point.y());
        gen.writeNumberField("z", point.z());
        if(point instanceof Pos pos) {
            gen.writeNumberField("yaw", pos.yaw());
            gen.writeNumberField("pitch", pos.pitch());
        }
        gen.writeEndObject();
    }
}
