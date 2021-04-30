package io.github.agentrkid.redis.packet;

import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.Random;

@Getter
public class TestPacket extends Packet {
    private boolean noob;

    @Override
    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();

        object.addProperty("noob", new Random().nextBoolean());

        return object;
    }

    @Override
    public void fromJsonObject(JsonObject object) {
        super.fromJsonObject(object);

        this.noob = object.get("noob").getAsBoolean();
    }
}
