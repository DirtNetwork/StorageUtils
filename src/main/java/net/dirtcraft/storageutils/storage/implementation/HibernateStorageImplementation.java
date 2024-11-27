/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.storage.implementation;

import net.dirtcraft.storageutils.storage.HibernateStorage;
import net.dirtcraft.storageutils.taskcontext.TaskContext;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface HibernateStorageImplementation<T extends TaskContext> extends StorageImplementation {

    <R> R performTask(HibernateStorage.@NonNull ResultTask<T, R> task);
}
