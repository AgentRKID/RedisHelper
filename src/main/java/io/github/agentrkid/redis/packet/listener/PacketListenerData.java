package io.github.agentrkid.redis.packet.listener;

import io.github.agentrkid.redis.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

@AllArgsConstructor
@Getter
public class PacketListenerData {
    private final Object listener;
    private final Method method;
    private final Class<? extends Packet> packetType;
}
