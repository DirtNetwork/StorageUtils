/*
 * Copyright (c) 2025 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.sql.BatchUpdateException;
import java.sql.SQLTransactionRollbackException;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.persistence.PersistenceException;
import net.dirtcraft.storageutils.hibernate.connection.AbstractHibernateConnectionFactory;
import net.dirtcraft.storageutils.logging.LoggerAdapter;
import net.dirtcraft.storageutils.storage.HibernateStorage;
import net.dirtcraft.storageutils.storage.implementation.HibernateStorageImplementation;
import net.dirtcraft.storageutils.taskcontext.TaskContext;
import net.dirtcraft.storageutils.util.SchemaReader;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.JDBCConnectionException;

public abstract class AbstractHibernateStorage<T extends TaskContext>
        implements HibernateStorageImplementation<T> {

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
        this.applySchema();
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
                    final T taskContext = this.createTaskContext(session);

                    try {
                        final R result = task.execute(taskContext);

                        transaction.commit();
                        // execute tasks after transaction was successfully committed
                        taskContext.executeTasks();

                        return result;
                    } catch (final Exception e) {
                        if (transaction.isActive()) {
                            transaction.rollback();
                            taskContext.executeRollbackTasks();

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

    @Nullable
    protected InputStream getSchema() {
        return null;
    }

    protected void applySchema() throws IOException {
        List<String> statements;

        try (final InputStream is = this.getSchema()) {
            if (is == null) {
                throw new IOException("Could not locate schema file.");
            }

            statements = SchemaReader.getStatements(is);
        }

        statements = SchemaReader.filterStatements(statements, this.getTables());

        if (statements.isEmpty()) {
            return;
        }

        final List<String> finalStatements = statements;
        final AtomicBoolean utf8mb4Unsupported = new AtomicBoolean(false);

        try {
            this.performTask(context -> {
                final Session session = context.session();

                for (final String query : finalStatements) {
                    session.createNativeQuery(query).executeUpdate();
                }

                return null;
            });
        } catch (final CompletionException e) {
            if (e.getCause() instanceof BatchUpdateException) {
                if (!e.getMessage().contains("Unknown character set")) {
                    throw e;
                }

                utf8mb4Unsupported.set(true);
            }
        }

        // try again
        if (utf8mb4Unsupported.get()) {
            this.performTask(context -> {
                final Session session = context.session();

                for (final String query : finalStatements) {
                    session.createNativeQuery(query.replace("utf8mb4", "utf8")).executeUpdate();
                }

                return null;
            });
        }
    }

    @SuppressWarnings("unchecked")
    @NonNull
    protected List<String> getTables() {
        return this.performTask(context -> (List<String>) context.session().createNativeQuery(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = '"
                        + this.connectionFactory.getDatabase() + '\'').getResultList());
    }
}
