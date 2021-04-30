package io.github.agentrkid.redis.packet;

public interface PacketResponseHandler {
    /**
     * @param packet the response packet
     */
    void handleResponse(Packet packet);
}
