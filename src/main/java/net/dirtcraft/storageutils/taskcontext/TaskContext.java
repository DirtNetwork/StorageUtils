/*
 * Copyright (c) 2025 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.taskcontext;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.Session;

public interface TaskContext {

    /**
     * Gets the context.session(). Safe resource, warnings can be ignored.
     *
     * @return the session
     */
    @NonNull
    Session session();

    /**
     * Queues a runnable task upon commit.
     *
     * @param runnable the runnable
     */
    void queue(@NonNull Runnable runnable);

    /**
     * Executes the runnable tasks upon commit.
     */
    void executeTasks();

    /**
     * Queues a runnable task upon rollback.
     *
     * @param runnable the runnable
     */
    void onRollback(@NonNull final Runnable runnable);

    /**
     * Executes the runnable tasks upon rollback.
     */
    void executeRollbackTasks();
}
