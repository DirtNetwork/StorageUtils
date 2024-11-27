/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.storage;

import net.dirtcraft.storageutils.logging.LoggerAdapter;
import net.dirtcraft.storageutils.storage.implementation.HibernateStorageImplementation;
import net.dirtcraft.storageutils.taskcontext.TaskContext;
import org.checkerframework.checker.nullness.qual.NonNull;

public class HibernateStorage<T extends TaskContext> extends Storage<HibernateStorageImplementation<T>> {

    public HibernateStorage(final LoggerAdapter logger,
            final HibernateStorageImplementation<T> implementation) {
        super(logger, implementation);
    }

    /**
     * Performs a task on the database.
     *
     * @param task the task
     */
    public void performTask(@NonNull final Task<T> task) {
        this.implementation.performTask(task);
    }

    /**
     * Performs a result task on the database.
     *
     * @param task the result task
     */
    public <R> R performTask(@NonNull final ResultTask<T, R> task) {
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
