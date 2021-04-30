package io.github.agentrkid.redis;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.agentrkid.redis.command.RedisCommand;
import io.github.agentrkid.redis.config.RedisOptions;
import io.github.agentrkid.redis.packet.Packet;
import io.github.agentrkid.redis.packet.PacketExceptionHandler;
import io.github.agentrkid.redis.packet.PacketPubSub;
import io.github.agentrkid.redis.packet.PacketResponseHandler;
import io.github.agentrkid.redis.packet.listener.PacketHandler;
import io.github.agentrkid.redis.packet.listener.PacketListener;
import io.github.agentrkid.redis.packet.listener.PacketListenerData;
import io.github.agentrkid.redis.packet.listener.PacketReceiveEvent;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RedisHelper {
    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    public static final Gson GSON = new GsonBuilder().serializeNulls().create();

    @Getter private final Cache<UUID, PacketResponseHandler> cachedResponseHandlers;
    private final Map<UUID, PacketExceptionHandler> cachedExceptionHandlers;

    @Getter private final List<PacketListenerData> listeners = new ArrayList<>();

    @Getter private final String sendingName;

    @Getter private final JedisPool pool;

    public RedisHelper(long handlerTimeout, TimeUnit timeoutUnit, String sendingName, RedisOptions options) {
        cachedResponseHandlers = CacheBuilder.newBuilder().expireAfterWrite(handlerTimeout, timeoutUnit).build();
        cachedExceptionHandlers = Maps.newConcurrentMap();

        this.sendingName = sendingName;

        if (options == null) {
            this.pool = new JedisPool(new JedisPoolConfig(), "127.0.0.1", 6379);
        } else {
            String password = options.getPassword();

            if (password.isEmpty()) {
                this.pool = new JedisPool(new JedisPoolConfig(), options.getHost(), options.getPort());
            } else {
                this.pool = new JedisPool(new JedisPoolConfig(), options.getHost(), options.getPort(), options.getUser(), password);
            }
        }

        // Direct pub sub allowing access directly
        new PacketPubSub(this).registerChannel(sendingName);
    }

    /**
     * @param packet the packet to send
     * @param channel the the channel to send to
     * @param response if its a response or not
     */
    public void sendPacket(Packet packet, String channel, boolean response) {
        UUID packetUuid = packet.getPacketUuid();

        try {
            JsonObject object = packet.toJsonObject();

            // Add all of our default properties
            object.addProperty("packetUuid", packet.getPacketUuid().toString());
            object.addProperty("packetClass", packet.getClass().getName());
            object.addProperty("sentFrom", sendingName);
            object.addProperty("response", response);

            // Send the object
            // as a string to redis
            runRedisCommand(redis -> redis.publish(channel, GSON.toJson(object)));
        } catch (Exception exception) {
            // We encountered an error send it to the exception handler
            if (cachedExceptionHandlers.containsKey(packetUuid)) {
                cachedExceptionHandlers.get(packetUuid).handleException(exception);
            }
        }
        cachedExceptionHandlers.remove(packetUuid);
    }

    /**
     * Sends a non response packet
     *
     * @param packet the packet to send
     * @param channel the channel to send to
     */
    public void sendPacket(Packet packet, String channel) {
        sendPacket(packet, channel, false);
    }

    /**
     * Sends a packet with a packet
     * response handler
     *
     * @param packet the packet to send
     * @param channel the channel to send to
     * @param responseHandler the response handler
     */
    public void sendPacket(Packet packet, String channel, PacketResponseHandler responseHandler) {
        cachedResponseHandlers.put(packet.getPacketUuid(), responseHandler);
        sendPacket(packet, channel, false);
    }

    /**
     * Sends a packet with both a
     * response and exception handler
     *
     * @param packet the packet to send
     * @param channel the channel to send to
     * @param responseHandler the response handler
     * @param exceptionHandler the exception handler
     */
    public void sendPacket(Packet packet, String channel, PacketResponseHandler responseHandler, PacketExceptionHandler exceptionHandler) {
        UUID packetUuid = packet.getPacketUuid();

        cachedResponseHandlers.put(packetUuid, responseHandler);
        cachedExceptionHandlers.put(packetUuid, exceptionHandler);

        sendPacket(packet, channel, false);
    }

    /**
     * @param packet the packet to send
     * @param channel the channel to send to
     * @param exceptionHandler the exception handler for callback
     */
    public void sendPacket(Packet packet, String channel, PacketExceptionHandler exceptionHandler) {
        cachedExceptionHandlers.put(packet.getPacketUuid(), exceptionHandler);
        sendPacket(packet, channel);
    }

    /**
     * @param listener the packet listener class
     */
    public void registerListener(PacketListener listener) {
        Class<?> clazz = listener.getClass();

        for (Method method : clazz.getDeclaredMethods()) {
            PacketHandler handlerAnnotation =  method.getDeclaredAnnotation(PacketHandler.class);

            if (handlerAnnotation == null) {
                continue;
            }

            if (method.getParameters().length > 0) {
                if (!PacketReceiveEvent.class.isAssignableFrom(method.getParameters()[0].getType())) {
                    throw new RuntimeException(method.getParameters()[0].getName() + " Is not a instance of packet");
                }

                method.setAccessible(true);
                listeners.add(new PacketListenerData(listener, method, handlerAnnotation.packetType()));
            } else {
                throw new RuntimeException("Tried registering a method which had a packet handler interface but no receive event");
            }
        }
    }

    /**
     * @param redisCommand the redis command to run
     * @param <T> the returnable value that is wanted
     * @return the returnable value
     */
    public <T> T runRedisCommand(RedisCommand<T> redisCommand) {
        Jedis jedis = this.pool.getResource();
        T result = null;

        try {
            result = redisCommand.execute(jedis);
        } catch (Exception ex) {
            ex.printStackTrace();

            if (jedis != null) {
                pool.returnBrokenResource(jedis);
            }
        } finally {
            if (jedis != null) {
                pool.returnResource(jedis);
            }
        }
        return result;
    }
}
