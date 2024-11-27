/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.implementation;

import net.dirtcraft.storageutils.Storage;
import net.dirtcraft.storageutils.taskcontext.TaskContext;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface StorageImplementation<T extends TaskContext> {

    void init() throws Exception;

    void shutdown();

    <SR extends Storage.ResultTask<T, R>, R> R performTask(@NonNull SR task);
}
