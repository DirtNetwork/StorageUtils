/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.connection;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface ConnectionFactory<T> {

    void init();

    void shutdown();

    @NonNull T getConnection();

    @NonNull String driverJdbcIdentifier();

    @NonNull String getDriverClass();

    @NonNull String getAddress();

    int getPort();

    @NonNull String getDatabase();

    @NonNull String getUsername();

    @NonNull String getPassword();

    int getPoolSize();
}
