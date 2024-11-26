/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.storagefactory;

import net.dirtcraft.storageutils.Storage;
import net.dirtcraft.storageutils.StorageType;
import net.dirtcraft.storageutils.logging.LoggerAdapter;
import net.dirtcraft.storageutils.taskcontext.TaskContext;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class AbstractStorageFactory<S extends Storage<T>, T extends TaskContext> implements StorageFactory<T> {

    private final LoggerAdapter logger;

    public AbstractStorageFactory(final LoggerAdapter logger) {
        this.logger = logger;
    }

    @NonNull
    protected abstract StorageType getStorageType();

    @NonNull
    protected abstract S createStorage(@NonNull StorageType type);

    @Override
    public @NonNull S getInstance() throws Exception {
        final StorageType type = this.getStorageType();

        this.logger.info("Loading storage provider... [" + type.name() + "]");

        final S storage = this.createStorage(type);

        storage.init();
        return storage;
    }
}
