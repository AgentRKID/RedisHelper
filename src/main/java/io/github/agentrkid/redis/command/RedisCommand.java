package io.github.agentrkid.redis.command;

import redis.clients.jedis.Jedis;

public interface RedisCommand<T> {
    T execute(Jedis jedis);
}
