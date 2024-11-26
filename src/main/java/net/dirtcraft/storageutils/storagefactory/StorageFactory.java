/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.storagefactory;

import java.util.Set;
import net.dirtcraft.storageutils.Storage;
import net.dirtcraft.storageutils.StorageType;
import net.dirtcraft.storageutils.taskcontext.TaskContext;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface StorageFactory<T extends TaskContext> {

    @NonNull Set<StorageType> getRequiredTypes();

    @NonNull Storage<?> getInstance() throws Exception;
}
