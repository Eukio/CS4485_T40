package com.example.test.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.test.backend.WordCandidate;

/**
 * Central database access class.
 *
 * All SQL operations go through this class so the rest of the program does not need
 * to worry about raw SQL strings, prepared statements, or transaction handling.
 */
public class DatabaseManager implements AutoCloseable {
    private final Connection connection;

    /**
     * Opens the JDBC connection and turns auto-commit off.
     *
     * We want one file import to behave like one transaction:
     * - if the import succeeds -> commit
     * - if anything fails      -> rollback
     */
    public DatabaseManager(DatabaseConfig config) throws SQLException {
        this.connection = DriverManager.getConnection(config.jdbcUrl(), config.username(), config.password());
        this.connection.setAutoCommit(false);
    }

    /**
     * Exposes the connection if needed for advanced operations.
     * Not heavily used in this starter project, but convenient to keep available.
     */
    public Connection connection() {
        return connection;
    }

    /**
     * Checks whether a file has already been imported.
     *
     * @param filename absolute normalized file path
     * @return true if the file already exists in imported_files
     */
    public boolean isFileAlreadyImported(String filename) throws SQLException {
        String sql = "SELECT 1 FROM imported_files WHERE filename = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, filename);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Inserts a row into imported_files, or updates it if the same filename already exists.
     *
     * @return the database id of the imported_files row
     */
    public long insertImportedFile(String filename, long wordCount, LocalDateTime importedAt) throws SQLException {
        String sql = """
                INSERT INTO imported_files(filename, word_count, imported_at)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    word_count = VALUES(word_count),
                    imported_at = VALUES(imported_at)
                """;

        // RETURN_GENERATED_KEYS lets us fetch the new id when a fresh row is inserted.
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, filename);
            ps.setLong(2, wordCount);
            ps.setTimestamp(3, Timestamp.valueOf(importedAt));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        // If MySQL updated an existing row instead of creating a new one, generated keys may be empty.
        // In that case, fetch the id manually.
        return findImportedFileId(filename);
    }

    /**
     * Looks up the id of a file row after insert/update.
     */
    public long findImportedFileId(String filename) throws SQLException {
        String sql = "SELECT id FROM imported_files WHERE filename = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, filename);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Imported file row not found for filename: " + filename);
    }

    /**
     * Loads all known words and their ids into memory.
     *
     * This is useful after upserting words, because later tables (word_links and word_file_counts)
     * use word ids instead of the raw text.
     */
    public Map<String, Long> loadExistingWordIds() throws SQLException {
        Map<String, Long> wordIds = new HashMap<>();
        String sql = "SELECT id, word FROM words";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                wordIds.put(rs.getString("word"), rs.getLong("id"));
            }
        }
        return wordIds;
    }

    /**
     * Inserts a new word or increments the counts of an existing word.
     *
     * The assignment requires tracking:
     * - total occurrences of each word
     * - occurrences at the start of a sentence
     * - occurrences at the end of a sentence
     */
    public void upsertWord(String word, long totalInc, long startInc, long endInc) throws SQLException {
        String sql = """
                INSERT INTO words(word, total_count, start_count, end_count, can_start, can_end)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    total_count = total_count + VALUES(total_count),
                    start_count = start_count + VALUES(start_count),
                    end_count = end_count + VALUES(end_count),
                    can_start = CASE WHEN (start_count + VALUES(start_count)) > 0 THEN 1 ELSE 0 END,
                    can_end = CASE WHEN (end_count + VALUES(end_count)) > 0 THEN 1 ELSE 0 END
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, word);
            ps.setLong(2, totalInc);
            ps.setLong(3, startInc);
            ps.setLong(4, endInc);
            ps.setBoolean(5, startInc > 0);
            ps.setBoolean(6, endInc > 0);
            ps.executeUpdate();
        }
    }

    /**
     * Looks up one word id directly.
     *
     * Not used much because loading all word ids at once is usually faster for the importer,
     * but this method is still helpful for debugging or future features.
     */
    public long findWordId(String word) throws SQLException {
        String sql = "SELECT id FROM words WHERE word = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, word);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Word not found after upsert: " + word);
    }

    /**
     * Inserts or increments a transition in the word_links table.
     *
     * Example: if "the" is followed by "cat" 10 times, frequency stores that count.
     */
    public void upsertWordLink(long wordId, long nextWordId, long frequencyInc) throws SQLException {
        String sql = """
                INSERT INTO word_links(word_id, next_word_id, frequency)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    frequency = frequency + VALUES(frequency)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, wordId);
            ps.setLong(2, nextWordId);
            ps.setLong(3, frequencyInc);
            ps.executeUpdate();
        }
    }

    /**
     * Stores how many times a word appeared inside one specific file.
     *
     * This is optional for the assignment, but useful for reporting and debugging.
     */
    public void upsertWordFileCount(long fileId, long wordId, long countInc) throws SQLException {
        String sql = """
                INSERT INTO word_file_counts(file_id, word_id, count_in_file)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    count_in_file = count_in_file + VALUES(count_in_file)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, fileId);
            ps.setLong(2, wordId);
            ps.setLong(3, countInc);
            ps.executeUpdate();
        }
    }

    /**
     * Permanently saves all changes made during the current transaction.
     */
    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * Rolls back the current transaction if an import fails.
     *
     * The method suppresses rollback exceptions because the original import error is usually
     * the one we care about most.
     */
    public void rollbackQuietly() {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    public List<WordCandidate> getNextWord(long wordId) throws SQLException{// return a list? (like the datastructure list)
        String sqlStatement = """
                SELECT wl.next_word_id, w.word, wl.frequency
                FROM word_links wl
                JOIN words w on w.id = wl.next_word_id
                WHERE wl.word_id = ? 
                ORDER BY wl.frequency DESC
                """;

        List<WordCandidate> results = new ArrayList<>();
        try(PreparedStatement prepStmt = connection.prepareStatement(sqlStatement)){
            prepStmt.setLong(1, wordId);
            try(ResultSet rs = prepStmt.executeQuery()){
                while(rs.next()){
                    results.add(new WordCandidate(
                        rs.getLong("next_word_id"),
                        rs.getString("word"),
                        rs.getLong("frequency")
                    ));

                }
            }
        
        
        }catch(SQLException e){
            e.printStackTrace();
        }
        //return a list of the id, word(string) and frequency(as long). 
        return results;
        
    }

    /**
     * Closes the database connection.
     */
    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
