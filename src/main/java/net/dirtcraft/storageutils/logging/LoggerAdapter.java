/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.logging;

/**
 * Represents a logger instance.
 * <p>
 * Functions use '{}' as a placeholder for arguments.
 */
public interface LoggerAdapter {

    void info(String s, Object... args);

    void warn(String s, Object... args);

    void severe(String s, Object... args);

    void severe(String s, Throwable t);
}
