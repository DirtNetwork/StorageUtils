/*
 * Copyright (c) 2025 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils;

import java.util.Map;
import java.util.Objects;

public class StorageCredentials {

    private final String address;
    private final String database;
    private final String username;
    private final String password;
    private final int maxPoolSize;
    private final int minIdleConnections;
    private final int maxLifetime;
    private final int keepAliveTime;
    private final int connectionTimeout;
    private final Map<String, String> properties;

    public StorageCredentials(final String address, final String database, final String username,
            final String password, final int maxPoolSize, final int minIdleConnections,
            final int maxLifetime, final int keepAliveTime, final int connectionTimeout,
            final Map<String, String> properties) {
        this.address = address;
        this.database = database;
        this.username = username;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.minIdleConnections = minIdleConnections;
        this.maxLifetime = maxLifetime;
        this.keepAliveTime = keepAliveTime;
        this.connectionTimeout = connectionTimeout;
        this.properties = properties;
    }

    public String getAddress() {
        return Objects.requireNonNull(this.address, "address");
    }

    public String getDatabase() {
        return Objects.requireNonNull(this.database, "database");
    }

    public String getUsername() {
        return Objects.requireNonNull(this.username, "username");
    }

    public String getPassword() {
        return Objects.requireNonNull(this.password, "password");
    }

    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    public int getMinIdleConnections() {
        return this.minIdleConnections;
    }

    public int getMaxLifetime() {
        return this.maxLifetime;
    }

    public int getKeepAliveTime() {
        return this.keepAliveTime;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }
}
