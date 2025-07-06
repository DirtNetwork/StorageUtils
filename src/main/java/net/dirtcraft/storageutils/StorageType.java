/*
 * Copyright (c) 2025 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils;

import org.checkerframework.checker.nullness.qual.NonNull;

public enum StorageType {

    MARIADB("mariadb", "org.mariadb.jdbc.Driver"),
    MYSQL("mysql", "com.mysql.jdbc.Driver");

    private final String jdbcDriverIdentifier;
    private final String driver;

    StorageType(final String jdbcDriverIdentifier, final String driver) {
        this.jdbcDriverIdentifier = jdbcDriverIdentifier;
        this.driver = driver;
    }

    public static StorageType parse(final String name, @NonNull final StorageType def) {
        for (final StorageType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return def;
    }

    public String getJdbcDriverIdentifier() {
        return this.jdbcDriverIdentifier;
    }

    public String getDriver() {
        return this.driver;
    }
}
