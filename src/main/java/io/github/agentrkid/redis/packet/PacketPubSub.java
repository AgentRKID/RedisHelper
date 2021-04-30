package io.github.agentrkid.redis.packet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.agentrkid.redis.RedisHelper;
import io.github.agentrkid.redis.packet.listener.PacketListenerData;
import io.github.agentrkid.redis.packet.listener.PacketReceiveEvent;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class PacketPubSub extends JedisPubSub {
    protected final RedisHelper helper;

    public PacketPubSub(RedisHelper helper) {
        this.helper = helper;
    }

    @Override
    public void onMessage(String channel, String message) {
        JsonObject object = JsonParser.parseString(message).getAsJsonObject();

        try {
            // Create a new instance of
            // the packet class and deserialize
            final Packet packet = (Packet) Class.forName(object.get("packetClass").getAsString()).getConstructor().newInstance();
            packet.fromJsonObject(object);

            final UUID packetUuid = packet.getPacketUuid();
            final String sentFrom = object.get("sentFrom").getAsString();

            // Check if it was a response
            // if not carry on
            if (object.get("response").getAsBoolean()) {
                final PacketResponseHandler responseHandler = helper.getCachedResponseHandlers().getIfPresent(packetUuid);

                if (responseHandler != null) {
                    responseHandler.handleResponse(packet);
                    return;
                }
            }

            final PacketReceiveEvent event = new PacketReceiveEvent(packet);

            for (PacketListenerData listener : helper.getListeners()) {
                if (listener.getPacketType() != packet.getClass()) {
                    continue;
                }
                listener.getMethod().invoke(listener.getListener(), event);
            }

            // Check if we have a response packet to use
            if (event.getResponsePacket() != null) {
                final Packet responsePacket = event.getResponsePacket();

                // We need to give back the same uuid as the packet
                // which was sent so the server knows which packet handler to use
                responsePacket.setPacketUuid(packetUuid);

                if (event.isAsyncResponse()) {
                    RedisHelper.EXECUTOR.execute(() -> helper.sendPacket(responsePacket, sentFrom, true));
                } else {
                    helper.sendPacket(responsePacket, sentFrom, true);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * @param channels the channels to register
     */
    public void registerChannel(String... channels) {
        RedisHelper.EXECUTOR.execute(() -> helper.runRedisCommand(redis -> {
            redis.subscribe(this, channels);
            return null;
        }));
    }
}
