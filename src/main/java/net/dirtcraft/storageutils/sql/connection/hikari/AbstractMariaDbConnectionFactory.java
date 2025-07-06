/*
 * Copyright (c) 2025 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.sql.connection.hikari;

import java.util.function.Function;
import net.dirtcraft.storageutils.StorageCredentials;

public abstract class AbstractMariaDbConnectionFactory extends AbstractDriverBasedHikariConnectionFactory {

    public AbstractMariaDbConnectionFactory(final StorageCredentials configuration) {
        super(configuration);
    }

    @Override
    public Function<String, String> getStatementProcessor() {
        return s -> s.replace('\'', '`'); // use backticks for quotes
    }

    @Override
    protected String defaultPort() {
        return "3306";
    }

    @Override
    protected String driverClassName() {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    protected String driverJdbcIdentifier() {
        return "mariadb";
    }
}
