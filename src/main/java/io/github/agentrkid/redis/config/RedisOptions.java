package io.github.agentrkid.redis.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RedisOptions {
    private final String host;
    private final int port;
    private final String user;
    private final String password;
}
