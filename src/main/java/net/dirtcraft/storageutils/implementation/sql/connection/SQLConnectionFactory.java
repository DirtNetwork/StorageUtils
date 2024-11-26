/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.implementation.sql.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public interface SQLConnectionFactory {

    void init();

    void shutdown() throws Exception;

    Function<String, String> getStatementProcessor();

    Connection getConnection() throws SQLException;

}