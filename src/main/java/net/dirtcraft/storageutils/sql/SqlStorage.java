/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.sql;

import java.util.function.Function;
import net.dirtcraft.storageutils.logging.LoggerAdapter;
import net.dirtcraft.storageutils.sql.connection.SQLConnectionFactory;

public class SqlStorage {

    protected final LoggerAdapter logger;
    protected final SQLConnectionFactory sqlConnectionFactory;
    protected final Function<String, String> statementProcessor;

    public SqlStorage(final LoggerAdapter logger, final SQLConnectionFactory sqlConnectionFactory,
            final String tablePrefix) {
        this.logger = logger;
        this.sqlConnectionFactory = sqlConnectionFactory;
        this.statementProcessor = sqlConnectionFactory.getStatementProcessor()
                .compose(s -> s.replace("{prefix}", tablePrefix));
    }

    public SQLConnectionFactory getConnectionFactory() {
        return this.sqlConnectionFactory;
    }

    public Function<String, String> getStatementProcessor() {
        return this.statementProcessor;
    }

    public void init() {
        this.sqlConnectionFactory.init();
    }

    public void shutdown() {
        try {
            this.sqlConnectionFactory.shutdown();
        } catch (final Exception e) {
            this.logger.severe("Exception whilst disabling SQL storage", e);
        }
    }
}
