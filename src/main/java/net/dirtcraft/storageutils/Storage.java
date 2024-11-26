/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import net.dirtcraft.storageutils.implementation.StorageImplementation;
import net.dirtcraft.storageutils.logging.LoggerAdapter;
import net.dirtcraft.storageutils.taskcontext.TaskContext;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Provides a {@link CompletableFuture} based API for interacting with a
 * {@link StorageImplementation}.
 */
public class Storage {

    private final LoggerAdapter logger;
    private final StorageImplementation implementation;

    public Storage(final LoggerAdapter logger, final StorageImplementation implementation) {
        this.logger = logger;
        this.implementation = implementation;
    }

    public StorageImplementation getImplementation() {
        return this.implementation;
    }

    public Collection<StorageImplementation> getImplementations() {
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

    /**
     * Performs a task on the database.
     *
     * @param task the task
     */
    public void performTask(@NonNull final Task task) {
        this.implementation.performTask(task);
    }

    /**
     * Performs a result task on the database.
     *
     * @param task the result task
     */
    public <T> @NonNull T performTask(@NonNull final ResultTask<T> task) {
        return this.implementation.performTask(task);
    }

    @FunctionalInterface
    public interface ResultTask<R> {

        /**
         * Performs this operation on the given task context.
         *
         * @param context the context
         * @return the result
         */
        R execute(@NonNull TaskContext context) throws Exception;
    }

    @FunctionalInterface
    public interface Task extends ResultTask<Void> {

        /**
         * Performs this operation on the given task context. Does not return a result.
         *
         * @param context the context
         */
        void executeNoResult(@NonNull TaskContext context) throws Exception;

        @Override
        default Void execute(@NonNull final TaskContext context) throws Exception {
            this.executeNoResult(context);
            return null;
        }
    }
}
