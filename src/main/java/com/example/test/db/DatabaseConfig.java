package com.example.test.db;

/**
 * Simple immutable holder for database connection settings.
 *
 * Using a record here keeps the code short and clear.
 */
public record DatabaseConfig(String jdbcUrl, String username, String password) {
}
