package io.github.agentrkid.redis.packet;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public abstract class Packet {
    @Setter private UUID packetUuid = UUID.randomUUID();

    /**
     * Encodes the packet as a json object
     *
     * @return the packet encoded in json
     */
    public abstract JsonObject toJsonObject();

    /**
     * Decodes the packet from json
     *
     * @param object the object to decode
     */
    public void fromJsonObject(JsonObject object) {
        this.packetUuid = UUID.fromString(object.get("packetUuid").getAsString());
    }
}
