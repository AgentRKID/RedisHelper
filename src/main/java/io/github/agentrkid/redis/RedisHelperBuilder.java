package io.github.agentrkid.redis;

import io.github.agentrkid.redis.config.RedisOptions;

import java.util.concurrent.TimeUnit;

public class RedisHelperBuilder {
    private RedisOptions options;

    private String sendingName = "Default-Sender";

    private TimeUnit timeoutUnit = TimeUnit.SECONDS;
    private int timeoutTime = 30;

    private boolean useDefault;

    public RedisHelperBuilder setOptions(RedisOptions options) {
        this.options = options;
        return this;
    }

    public RedisHelperBuilder setSendingName(String sendingName) {
        this.sendingName = sendingName;
        return this;
    }

    public RedisHelperBuilder setTimeoutUnit(TimeUnit timeoutUnit) {
        this.timeoutUnit = timeoutUnit;
        return this;
    }

    public RedisHelperBuilder setTimeoutTime(int timeoutTime) {
        this.timeoutTime = timeoutTime;
        return this;
    }

    public RedisHelperBuilder useDefaultPub(boolean useDefault) {
        useDefault = useDefault;
        return this;
    }

    public RedisHelper build() {
        return new RedisHelper(timeoutTime, timeoutUnit, sendingName, options, useDefault);
    }

    public static RedisHelperBuilder newBuilder() {
        return new RedisHelperBuilder();
    }
}
