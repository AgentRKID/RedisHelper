package io.github.agentrkid.redis.packet.listener;

import io.github.agentrkid.redis.packet.Packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketHandler {
    Class<? extends Packet> packetType();
}
