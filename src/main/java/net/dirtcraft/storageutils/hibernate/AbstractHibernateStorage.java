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
    public void init() {
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
        final int retriesUponConnectionLoss = this.getRetriesUponConnectionLoss();
        final int retriesUponException = this.getRetriesUponException();
        int connectionTryIndex = 0;

        while (true) {
            try (final Session session = this.connectionFactory.getConnection().openSession()) {
                int tryIndex = 0;

                while (true) {
                    final Transaction transaction = session.beginTransaction();

                    try {
                        final T taskContext = this.createTaskContext(session);
                        final R result = task.execute(taskContext);

                        transaction.commit();
                        // execute tasks after transaction was successfully committed
                        taskContext.executeTasks();

                        return result;
                    } catch (final Exception e) {
                        transaction.rollback();

                        if (e instanceof PersistenceException
                                || e instanceof SQLTransactionRollbackException) {
                            tryIndex++;

                            if (tryIndex <= retriesUponException) {
                                continue;
                            }

                            this.logger.severe(
                                    "Ran into persistence exception after trying {} times.",
                                    tryIndex);
                        }

                        if (e instanceof RuntimeException) {
                            throw (RuntimeException) e;
                        }

                        throw new CompletionException(e);
                    }
                }
            } catch (final JDBCConnectionException e) {
                connectionTryIndex++;

                if (connectionTryIndex <= retriesUponConnectionLoss) {
                    // we are doing this due to the reconnect properties if we fail to establish
                    // a connection, hibernate will automatically try to reconnect
                    continue;
                }

                this.logger.severe("Could not open session after trying {} times.",
                        connectionTryIndex);
                throw new CompletionException(e);
            }
        }
    }
}
