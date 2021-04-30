package io.github.agentrkid.redis.packet.listener;

import io.github.agentrkid.redis.packet.Packet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class PacketReceiveEvent {

    private final Packet packet;

    @Setter private boolean asyncResponse;
    @Setter private Packet responsePacket;
}
