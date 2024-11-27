/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.connection;

import net.dirtcraft.storageutils.StorageCredentials;
import net.dirtcraft.storageutils.logging.LoggerAdapter;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class AbstractConnectionFactory<T> implements ConnectionFactory<T> {

    protected final LoggerAdapter logger;
    private final StorageCredentials credentials;
    private final String address;
    private final int port;

    protected AbstractConnectionFactory(final LoggerAdapter logger,
            final StorageCredentials credentials) {
        this.logger = logger;
        this.credentials = credentials;

        final String[] addressSplit = credentials.getAddress().split(":");
        this.address = addressSplit[0];


        if (addressSplit.length > 1) {
            int port;
            final String raw = addressSplit[1];

            try {
                port = Integer.parseInt(raw);
            } catch (final NumberFormatException ignored) {
                logger.warn("Could not transform '{}' to a port.", raw);
                port = this.getDefaultPort();
            }

            this.port = port;
        } else {
            this.port = this.getDefaultPort();
        }
    }

    @Override
    public @NonNull String getAddress() {
        return this.address;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public @NonNull String getDatabase() {
        return this.credentials.getDatabase();
    }

    @Override
    public @NonNull String getUsername() {
        return this.credentials.getUsername();
    }

    @Override
    public @NonNull String getPassword() {
        return this.credentials.getPassword();
    }

    @Override
    public int getPoolSize() {
        return this.credentials.getMaxPoolSize();
    }

    protected int getDefaultPort() {
        return 3306;
    }
}
