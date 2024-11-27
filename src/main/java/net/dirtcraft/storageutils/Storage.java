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
public class Storage<T extends TaskContext> {

    protected final LoggerAdapter logger;
    protected final StorageImplementation<T> implementation;

    public Storage(final LoggerAdapter logger, final StorageImplementation<T> implementation) {
        this.logger = logger;
        this.implementation = implementation;
    }

    public StorageImplementation<T> getImplementation() {
        return this.implementation;
    }

    public Collection<StorageImplementation<T>> getImplementations() {
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
    public <ST extends Storage.Task<T>> void performTask(@NonNull final ST task) {
        this.implementation.performTask(task);
    }

    /**
     * Performs a result task on the database.
     *
     * @param task the result task
     */
    public <SR extends Storage.ResultTask<T, R>, R> R performTask(@NonNull final SR task) {
        return this.implementation.performTask(task);
    }

    @FunctionalInterface
    public interface ResultTask<T extends TaskContext, R> {

        /**
         * Performs this operation on the given task context.
         *
         * @param context the task context
         * @return the result
         */
        R execute(@NonNull T context) throws Exception;
    }

    @FunctionalInterface
    public interface Task<T extends TaskContext> extends ResultTask<T, Void> {

        /**
         * Performs this operation on the given task context. Does not return a result.
         *
         * @param context the task context
         */
        void executeNoResult(@NonNull T context) throws Exception;

        @Override
        default Void execute(@NonNull final T context) throws Exception {
            this.executeNoResult(context);
            return null;
        }
    }
}
