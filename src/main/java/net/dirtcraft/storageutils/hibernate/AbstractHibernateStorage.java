/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.hibernate;

import java.sql.SQLTransactionRollbackException;
import java.util.concurrent.CompletionException;
import javax.persistence.PersistenceException;
import net.dirtcraft.storageutils.hibernate.connection.AbstractHibernateConnectionFactory;
import net.dirtcraft.storageutils.logging.LoggerAdapter;
import net.dirtcraft.storageutils.storage.HibernateStorage;
import net.dirtcraft.storageutils.storage.implementation.HibernateStorageImplementation;
import net.dirtcraft.storageutils.taskcontext.TaskContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.JDBCConnectionException;

public abstract class AbstractHibernateStorage<T extends TaskContext> implements HibernateStorageImplementation<T> {

    protected final LoggerAdapter logger;
    protected final AbstractHibernateConnectionFactory connectionFactory;

    public AbstractHibernateStorage(final LoggerAdapter logger,
            final AbstractHibernateConnectionFactory connectionFactory) {
        this.logger = logger;
        this.connectionFactory = connectionFactory;
    }

    protected abstract int getRetriesUponConnectionLoss();

    protected abstract int getRetriesUponException();

    @NonNull
    protected abstract T createTaskContext(@NonNull Session session);

    @Override
    public void init() throws Exception {
        this.connectionFactory.init();
    }

    @Override
    public void shutdown() {
        try {
            this.connectionFactory.shutdown();
        } catch (final Exception e) {
            this.logger.severe("Exception whilst disabling Hibernate storage.", e);
        }
    }

    /**
     * Perform a task in a context.session(). Session is started and committed automatically.
     *
     * @param task the task
     */
    @Override
    public <R> R performTask(final HibernateStorage.@NonNull ResultTask<T, R> task) {
        final int retriesUponConnectionLoss = this.getRetriesUponConnectionLoss() + 1;
        final int retriesUponDeadlock = this.getRetriesUponException() + 1;
        int connectionTryIndex = 0;

        while (true) {
            try (final Session session = this.connectionFactory.getConnection().openSession()) {
                int tryIndex = 0;

                while (tryIndex <= retriesUponDeadlock) {
                    System.out.println("TRY INDEX: " + tryIndex + " <= " + retriesUponDeadlock);

                    final Transaction transaction = session.beginTransaction();

                    try {
                        final T taskContext = this.createTaskContext(session);
                        final R result = task.execute(taskContext);

                        transaction.commit();
                        // execute tasks after transaction was successfully committed
                        taskContext.executeTasks();

                        return result;
                    } catch (final PersistenceException | SQLTransactionRollbackException e) {
                        tryIndex++;
                        this.logger.severe("Caught exception.", e);
                        this.logger.info("Ran into deadlock. Trying to perform task again: #{}",
                                tryIndex);
                    } catch (final Exception e) {
                        System.out.println("ROLLBACK: " + e.getMessage());
                        transaction.rollback();

                        if (e instanceof RuntimeException) {
                            throw (RuntimeException) e;
                        }

                        throw new CompletionException(e);
                    }
                }
            } catch (final JDBCConnectionException e) {
                connectionTryIndex++;

                if (connectionTryIndex > retriesUponConnectionLoss) {
                    throw new CompletionException(e);
                }

                // we are doing this due to the reconnect properties
                // if we fail to establish a connection, hibernate will automatically try to
                // reconnect
                this.logger.info("Could not open session. Trying to reconnect: #{}",
                        connectionTryIndex);
            }
        }
    }
}
