/*
 * Copyright (c) 2025 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class SchemaReader {

    private static final Pattern ALTER_TABLE_PATTERN =
            Pattern.compile("^ALTER TABLE [`\"']([^`\"']+)[`\"'].*");
    private static final Pattern CREATE_INDEX_PATTERN =
            Pattern.compile("^CREATE INDEX.* ON [`\"']([^`\"']+)[`\"'].*");
    private static final Pattern CREATE_TABLE_PATTERN =
            Pattern.compile("^CREATE TABLE [`\"']([^`\"']+)[`\"'].*");
    private static final List<Pattern> TABLE_PATTERNS =
            Arrays.asList(ALTER_TABLE_PATTERN, CREATE_INDEX_PATTERN, CREATE_TABLE_PATTERN);

    private SchemaReader() {}

    /**
     * Parses a schema file to a list of SQL statements
     *
     * @param is the input stream to read from
     * @return a list of statements
     * @throws IOException if an error occurs whilst reading the file
     */
    public static List<String> getStatements(final InputStream is) throws IOException {
        final List<String> queries = new LinkedList<>();

        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("--") || line.startsWith("#")) {
                    continue;
                }

                sb.append(line);

                // check for end of declaration
                if (line.endsWith(";")) {
                    sb.deleteCharAt(sb.length() - 1);

                    final String result = sb.toString().trim().replaceAll(" +", " ");
                    if (!result.isEmpty()) {
                        queries.add(result);
                    }

                    // reset
                    sb = new StringBuilder();
                }
            }
        }

        return queries;
    }

    @NonNull
    public static String tableFromStatement(final String statement) {
        for (final Pattern pattern : TABLE_PATTERNS) {
            final Matcher matcher = pattern.matcher(statement);

            if (matcher.matches()) {
                return matcher.group(1).toLowerCase(Locale.ROOT);
            }
        }

        throw new IllegalArgumentException("Unknown statement type: " + statement);
    }

    /**
     * Filters which statements should be executed based on the current list of tables in the
     * database
     *
     * @param statements    the statements to filter
     * @param currentTables the current tables in the database
     * @return the filtered list of statements
     */
    public static List<String> filterStatements(final Collection<String> statements,
            final Collection<String> currentTables) {
        return statements.stream()
                .filter(statement -> !currentTables.contains(tableFromStatement(statement)))
                .collect(Collectors.toList());
    }
}
