/*
 * Copyright (c) 2025 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.hibernate.connection;

import net.dirtcraft.storageutils.StorageCredentials;
import net.dirtcraft.storageutils.StorageType;
import net.dirtcraft.storageutils.connection.AbstractConnectionFactory;
import net.dirtcraft.storageutils.logging.LoggerAdapter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public abstract class AbstractHibernateConnectionFactory extends AbstractConnectionFactory<SessionFactory> {

    protected final StorageType storageType;
    protected final Configuration configuration;
    protected final StandardServiceRegistry registry;
    protected SessionFactory sessionFactory;

    public AbstractHibernateConnectionFactory(final LoggerAdapter logger,
            final StorageType storageType, final StorageCredentials credentials) {
        super(logger, credentials);
        this.storageType = storageType;
        this.configuration = this.initConfig();
        this.registry = this.initRegistry();
    }

    protected abstract void addAnnotatedClasses(@NonNull final Configuration configuration);

    protected abstract void addProperties(@NonNull final Configuration configuration);

    @Override
    public void init() {
        this.sessionFactory = this.configuration.buildSessionFactory(this.registry);
    }

    @Override
    public void shutdown() {
        this.sessionFactory.close();
    }

    @Override
    public @NonNull SessionFactory getConnection() {
        return this.sessionFactory;
    }

    @Override
    public @NonNull String driverJdbcIdentifier() {
        return this.storageType.getJdbcDriverIdentifier();
    }

    @Override
    public @NonNull String getDriverClass() {
        return this.storageType.getDriver();
    }

    @NonNull
    protected Configuration initConfig() {
        final Configuration configuration = new Configuration();

        this.addAnnotatedClasses(configuration);
        this.addProperties(configuration);

        return configuration;
    }

    @NonNull
    protected StandardServiceRegistry initRegistry() {
        return new StandardServiceRegistryBuilder().applySettings(
                this.configuration.getProperties()).build();
    }
}
