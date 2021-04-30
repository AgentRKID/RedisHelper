package io.github.agentrkid.redis;

import io.github.agentrkid.redis.packet.PacketPubSub;
import io.github.agentrkid.redis.packet.PacketResponseHandler;
import io.github.agentrkid.redis.packet.TestPacket;
import io.github.agentrkid.redis.packet.listener.PacketHandler;
import io.github.agentrkid.redis.packet.listener.PacketListener;
import io.github.agentrkid.redis.packet.listener.PacketReceiveEvent;

public class Main implements PacketListener {
    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        RedisHelper helper = RedisHelperBuilder.newBuilder().build();

        // Register the current class as a listener
        helper.registerListener(this);


        new Thread(() -> {
            while(true) {
                // Sends a test packet then listens for any response packet.
                helper.sendPacket(new TestPacket(), helper.getSendingName(), (PacketResponseHandler) packet -> System.out.println(packet + " was a response."));

                try {
                    Thread.sleep(25L);
                } catch (Exception ignored) {
                }
            }
        }).start();
    }

    @PacketHandler(packetType = TestPacket.class)
    public void handlePacket(PacketReceiveEvent event) {
        // Sets the response packet
        event.setResponsePacket(new TestPacket());
        System.out.println("Handled");
    }
}
