/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import net.dirtcraft.storageutils.logging.LoggerAdapter;
import net.dirtcraft.storageutils.storage.implementation.StorageImplementation;

/**
 * Provides a {@link CompletableFuture} based API for interacting with a
 * {@link StorageImplementation}.
 */
public class Storage<S extends StorageImplementation> {

    protected final LoggerAdapter logger;
    protected final S implementation;

    public Storage(final LoggerAdapter logger, final S implementation) {
        this.logger = logger;
        this.implementation = implementation;
    }

    public S getImplementation() {
        return this.implementation;
    }

    public Collection<S> getImplementations() {
        return Collections.singleton(this.implementation);
    }

    public void init() throws Exception {
        this.implementation.init();
    }

    public void shutdown() {
        try {
            this.implementation.shutdown();
        } catch (final Exception e) {
            this.logger.severe("Failed to shutdown storage implementation", e);
        }
    }
}
