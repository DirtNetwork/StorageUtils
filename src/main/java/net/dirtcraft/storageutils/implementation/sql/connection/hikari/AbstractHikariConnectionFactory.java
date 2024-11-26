/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.implementation.sql.connection.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.dirtcraft.storageutils.StorageCredentials;
import net.dirtcraft.storageutils.implementation.sql.connection.SQLConnectionFactory;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Abstract {@link SQLConnectionFactory} using a {@link HikariDataSource}.
 */
public abstract class AbstractHikariConnectionFactory implements SQLConnectionFactory {

    private final StorageCredentials configuration;
    private HikariDataSource hikari;

    public AbstractHikariConnectionFactory(final StorageCredentials configuration) {
        this.configuration = configuration;
    }

    // dumb plugins seem to keep doing stupid stuff with shading of SLF4J and Log4J.
    // detect this and print a more useful error message.
    protected abstract void handleClassloadingError(final Throwable throwable);

    /**
     * Gets the default port used by the database
     *
     * @return the default port
     */
    protected abstract String defaultPort();

    /**
     * Configures the {@link HikariConfig} with the relevant database properties.
     *
     * <p>Each driver does this slightly differently...</p>
     *
     * @param config       the hikari config
     * @param address      the database address
     * @param port         the database port
     * @param databaseName the database name
     * @param username     the database username
     * @param password     the database password
     */
    protected abstract void configureDatabase(HikariConfig config, String address, String port,
            String databaseName, String username, String password);

    @NonNull
    protected abstract String getPoolName();

    @Override
    public void init() {
        final HikariConfig config;

        try {
            config = new HikariConfig();
        } catch (final LinkageError e) {
            this.handleClassloadingError(e);
            throw e;
        }

        // set pool name so the logging output can be linked back to us
        config.setPoolName(this.getPoolName());

        // get the database info/credentials from the config file
        final String[] addressSplit = this.configuration.getAddress().split(":");
        final String address = addressSplit[0];
        final String port = addressSplit.length > 1 ? addressSplit[1] : this.defaultPort();

        // allow the implementation to configure the HikariConfig appropriately with these values
        try {
            this.configureDatabase(config, address, port, this.configuration.getDatabase(),
                    this.configuration.getUsername(), this.configuration.getPassword());
        } catch (final NoSuchMethodError e) {
            this.handleClassloadingError(e);
        }

        // get the extra connection properties from the config
        final Map<String, Object> properties = new HashMap<>(this.configuration.getProperties());

        // allow the implementation to override/make changes to these properties
        this.overrideProperties(properties);

        // set the properties
        this.setProperties(config, properties);

        // configure the connection pool
        config.setMaximumPoolSize(this.configuration.getMaxPoolSize());
        config.setMinimumIdle(this.configuration.getMinIdleConnections());
        config.setMaxLifetime(this.configuration.getMaxLifetime());
        config.setKeepaliveTime(this.configuration.getKeepAliveTime());
        config.setConnectionTimeout(this.configuration.getConnectionTimeout());

        // don't perform any initial connection validation - we subsequently call #getConnection
        // to set up the schema anyway
        config.setInitializationFailTimeout(-1);

        this.hikari = new HikariDataSource(config);

        this.postInitialize();
    }

    @Override
    public void shutdown() {
        if (this.hikari != null) {
            this.hikari.close();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (this.hikari == null) {
            throw new SQLException("Unable to get a connection from the pool. (hikari is null)");
        }

        final Connection connection = this.hikari.getConnection();
        if (connection == null) {
            throw new SQLException(
                    "Unable to get a connection from the pool. (getConnection returned null)");
        }

        return connection;
    }

    /**
     * Allows the connection factory instance to override certain properties before they are set.
     *
     * @param properties the current properties
     */
    protected void overrideProperties(final Map<String, Object> properties) {
        // https://github.com/brettwooldridge/HikariCP/wiki/Rapid-Recovery
        properties.putIfAbsent("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)));
    }

    /**
     * Sets the given connection properties onto the config.
     *
     * @param config     the hikari config
     * @param properties the properties
     */
    protected void setProperties(final HikariConfig config, final Map<String, Object> properties) {
        for (final Map.Entry<String, Object> property : properties.entrySet()) {
            config.addDataSourceProperty(property.getKey(), property.getValue());
        }
    }

    /**
     * Called after the Hikari pool has been initialised
     */
    protected void postInitialize() {}
}
