package com.example.test.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

    //Josh
    

    /**
     * Closes the database connection.
     */
    @Override
    public void close() throws SQLException {
        connection.close();
    }




    //Christian Verderame
    //From here I have code to print out the characteristics of a word, such as its count
    //frquency as a starting or ending word, and the next or previous words to the words you are asking for in
    //the GUI
    /**
     * Holds basic information about a word from the "words" table.
     */
    public static class WordInfo {
        public long id;
        public String word;
        public long totalCount;
        public long startCount;
        public long endCount;
        public boolean canStart;
        public boolean canEnd;
    }

    /**
     * Represents a related word (next or previous) and how often it appears.
     */
    public static class WordNeighbor {
        public String word;
        public long frequency;

        public WordNeighbor(String word, long frequency) {
            this.word = word;
            this.frequency = frequency;
        }

        @Override
        public String toString() {
            return word + " (" + frequency + ")";
        }
    }


    /**
     * Combines all details about a word:
     * - its info
     * - words that come after it
     * - words that come before it
     */
    public static class WordDetails {
        public WordInfo info;
        public java.util.List<WordNeighbor> nextWords;
        public java.util.List<WordNeighbor> previousWords;
    }

    /**
     * Finds a word in the database using the TEXT (not id).
     *
     * Example:
     * input = "the"
     *
     * This runs:
     * SELECT * FROM words WHERE word = 'the'
     */
    public WordInfo getWordInfo(String word) throws SQLException {
        String sql = """
        SELECT id, word, total_count, start_count, end_count, can_start, can_end
        FROM words
        WHERE word = ?
    """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // Normalize input (lowercase + trim spaces)
            ps.setString(1, word.toLowerCase().trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    WordInfo info = new WordInfo();

                    // Extract values from database row
                    info.id = rs.getLong("id");
                    info.word = rs.getString("word");
                    info.totalCount = rs.getLong("total_count");
                    info.startCount = rs.getLong("start_count");
                    info.endCount = rs.getLong("end_count");
                    info.canStart = rs.getBoolean("can_start");
                    info.canEnd = rs.getBoolean("can_end");

                    return info;
                }
            }
        }

        // If no match found
        throw new SQLException("Word not found: " + word);
    }


    /**
     * Finds all words that come AFTER the given word.
     *
     * Example:
     * "the" → ["cat", "dog", "man"]
     *
     * This works by:
     * 1. Finding the row where word = 'the'
     * 2. Joining to word_links
     * 3. Getting the next_word_id
     * 4. Converting that id back into a word
     */
    public java.util.List<WordNeighbor> getNextWords(String word) throws SQLException {
        String sql = """
        SELECT w2.word AS next_word, wl.frequency
        FROM word_links wl
        JOIN words w1 ON wl.word_id = w1.id
        JOIN words w2 ON wl.next_word_id = w2.id
        WHERE w1.word = ?
        ORDER BY wl.frequency DESC
        LIMIT 10
    """;

        java.util.List<WordNeighbor> list = new java.util.ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, word.toLowerCase().trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new WordNeighbor(
                            rs.getString("next_word"),
                            rs.getLong("frequency")
                    ));
                }
            }
        }

        return list;
    }


    /**
     * Finds all words that come BEFORE the given word.
     *
     * Example:
     * "cat" → ["the", "a", "my"]
     *
     * This works by reversing the relationship:
     * Instead of word_id → next_word_id,
     * we look for rows where next_word_id = current word
     */
    public java.util.List<WordNeighbor> getPreviousWords(String word) throws SQLException {
        String sql = """
        SELECT w1.word AS prev_word, wl.frequency
        FROM word_links wl
        JOIN words w1 ON wl.word_id = w1.id
        JOIN words w2 ON wl.next_word_id = w2.id
        WHERE w2.word = ?
        ORDER BY wl.frequency DESC
        LIMIT 10
    """;

        java.util.List<WordNeighbor> list = new java.util.ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, word.toLowerCase().trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new WordNeighbor(
                            rs.getString("prev_word"),
                            rs.getLong("frequency")
                    ));
                }
            }
        }

        return list;
    }


    /**
     * Main function that combines everything.
     *
     * Given a word:
     * - gets its info
     * - gets next words
     * - gets previous words
     */
    public WordDetails getWordDetails(String word) throws SQLException {
        WordDetails details = new WordDetails();

        // Basic info about the word
        details.info = getWordInfo(word);

        // Words that come after
        details.nextWords = getNextWords(word);

        // Words that come before
        details.previousWords = getPreviousWords(word);

        return details;
    }



    /**
     * Simple debug function to print everything about a word.
     */
    public String printWordDetails(String word) throws SQLException {
        WordDetails d = getWordDetails(word);

        if (d == null) {
            return "Word not found: " + word;
        }

        //full string with all the info to print out in GUI
        String  fullWordInfo = "";

        fullWordInfo = fullWordInfo +  "WORD: " + d.info.word + "\nTOTAL COUNT: " + d.info.totalCount + "\nSTART COUNT: " + d.info.startCount +
                "\nEND COUNT: " + d.info.endCount + "\n";

        fullWordInfo = fullWordInfo + "\nNEXT WORDS: \n";
        for (WordNeighbor w : d.nextWords) {
            fullWordInfo =  fullWordInfo + w + "\n";
        }

        fullWordInfo = fullWordInfo + "\nPREVIOUS WORDS: \n";
        for (WordNeighbor w : d.previousWords) {
            fullWordInfo =  fullWordInfo + w + "\n";
        }

        return fullWordInfo;
        /*
        System.out.println("WORD: " + d.info.word);
        System.out.println("TOTAL COUNT: " + d.info.totalCount);
        System.out.println("START COUNT: " + d.info.startCount);
        System.out.println("END COUNT: " + d.info.endCount);

        System.out.println("\nNEXT WORDS:");
        for (WordNeighbor w : d.nextWords) {
            System.out.println("  " + w);
        }

        System.out.println("\nPREVIOUS WORDS:");
        for (WordNeighbor w : d.previousWords) {
            System.out.println("  " + w);
        }

         */
    }

    //Christian Verderame
    /**
     * Returns all file stats as a formatted string (for GUI display).
     */
    public String getAllFilesStatsString() throws SQLException {
        String sql = """
        SELECT f.id, f.filename,
               COUNT(wfc.word_id) AS unique_words,
               COALESCE(SUM(wfc.count_in_file), 0) AS total_words
        FROM imported_files f
        LEFT JOIN word_file_counts wfc ON f.id = wfc.file_id
        GROUP BY f.id, f.filename
        ORDER BY f.filename
    """;

        StringBuilder sb = new StringBuilder();

        sb.append("=== FILES IN DATABASE ===\n\n");

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                sb.append("File ID: ").append(rs.getLong("id")).append("\n");
                sb.append("Name: ").append(rs.getString("filename")).append("\n");
                sb.append("Total Words: ").append(rs.getLong("total_words")).append("\n");
                sb.append("Unique Words: ").append(rs.getLong("unique_words")).append("\n");
                sb.append("------------------------\n");
            }
        }

        return sb.toString();
    }

//TODO: String that sees a list of all words in the system and information about them

//TODO: Keep track of what sentences have been generated so your user can look for duplicates


}
