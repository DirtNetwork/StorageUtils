/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.implementation;

import net.dirtcraft.storageutils.Storage;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface StorageImplementation {

    void init() throws Exception;

    void shutdown();

    <T> T performTask(final Storage.@NonNull ResultTask<T> task);
}
