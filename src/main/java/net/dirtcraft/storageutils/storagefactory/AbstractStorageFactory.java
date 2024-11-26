/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.storagefactory;

import net.dirtcraft.storageutils.Storage;
import net.dirtcraft.storageutils.StorageType;
import net.dirtcraft.storageutils.implementation.StorageImplementation;
import net.dirtcraft.storageutils.logging.LoggerAdapter;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class AbstractStorageFactory implements StorageFactory {

    private final LoggerAdapter logger;

    public AbstractStorageFactory(final LoggerAdapter logger) {
        this.logger = logger;
    }

    @NonNull
    protected abstract StorageType getStorageType();

    @NonNull
    protected abstract StorageImplementation createNewImplementation(final StorageType method);

    @Override
    public Storage getInstance() throws Exception {
        final StorageType type = this.getStorageType();

        this.logger.info("Loading storage provider... [" + type.name() + "]");

        final Storage storage = new Storage(this.logger, this.createNewImplementation(type));

        storage.init();
        return storage;
    }
}
