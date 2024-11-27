/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.sql.connection.hikari;

import com.zaxxer.hikari.HikariConfig;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import net.dirtcraft.storageutils.StorageCredentials;

/**
 * Extension of {@link AbstractHikariConnectionFactory} that uses the driver class name to
 * configure Hikari.
 */
public abstract class AbstractDriverBasedHikariConnectionFactory extends AbstractHikariConnectionFactory {

    protected AbstractDriverBasedHikariConnectionFactory(final StorageCredentials configuration) {
        super(configuration);
    }

    protected static void deregisterDriver(final String driverClassName) {
        final Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            final Driver driver = drivers.nextElement();
            if (driver.getClass().getName().equals(driverClassName)) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (final SQLException e) {
                    // ignore
                }
            }
        }
    }

    protected abstract String driverClassName();

    protected abstract String driverJdbcIdentifier();

    @Override
    protected void configureDatabase(final HikariConfig config, final String address,
            final String port, final String databaseName, final String username,
            final String password) {
        config.setDriverClassName(this.driverClassName());
        config.setJdbcUrl(
                String.format("jdbc:%s://%s:%s/%s", this.driverJdbcIdentifier(), address, port,
                        databaseName));
        config.setUsername(username);
        config.setPassword(password);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();

        // Calling Class.forName("<driver class name>") is enough to call the static initializer
        // which makes our driver available in DriverManager. We don't want that, so unregister
        // it after the pool has been set up.
        deregisterDriver(this.driverClassName());
    }
}
