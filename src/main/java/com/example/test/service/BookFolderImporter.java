package com.example.test.service;

import com.example.test.db.DatabaseManager;
import com.example.test.model.ParsedSentence;
import com.example.test.util.TextPreprocessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

/**
 * Imports every .txt file in a folder into the database.
 *
 * This class is the core of the preprocessing pipeline:
 * - find text files
 * - read them
 * - parse them into sentences and words
 * - count word statistics
 * - count word-to-next-word transitions
 * - write everything to MySQL
 */
public class BookFolderImporter {
    private final DatabaseManager databaseManager;

    public BookFolderImporter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Recursively scans a folder for .txt files and imports them one by one.
     *
     * @param folderPath folder containing books
     * @param skipAlreadyImported if true, files already listed in imported_files are skipped
     */
    public void importFolder(Path folderPath, boolean skipAlreadyImported) throws IOException {
        try (Stream<Path> pathStream = Files.walk(folderPath)) {
            List<Path> txtFiles = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(".txt"))
                    .sorted()
                    .toList();

            if (txtFiles.isEmpty()) {
                System.out.println("No .txt files found in folder: " + folderPath);
                return;
            }

            int index = 1;
            for (Path txtFile : txtFiles) {
                System.out.printf("[%d/%d] Importing %s%n", index++, txtFiles.size(), txtFile);
                importSingleFile(txtFile, skipAlreadyImported);
            }
        }
    }

    /**
     * Imports one text file as one database transaction.
     *
     * The high-level steps are:
     * 1) skip file if requested and already imported
     * 2) read raw text
     * 3) preprocess into sentences and words
     * 4) build counts/transitions in memory
     * 5) upsert file row, word rows, per-file counts, and transition rows
     * 6) commit if successful, rollback if anything fails
     */
    private void importSingleFile(Path txtFile, boolean skipAlreadyImported) {
        try {
            String filename = txtFile.toAbsolutePath().normalize().toString();

            // Avoid duplicate imports when the user chooses to skip existing files.
            if (skipAlreadyImported && databaseManager.isFileAlreadyImported(filename)) {
                System.out.println("  Skipped (already imported): " + filename);
                return;
            }

            // Read the whole file using UTF-8 text.
            String rawText = Files.readString(txtFile, StandardCharsets.UTF_8);

            // Convert raw text into a list of clean normalized sentences.
            List<ParsedSentence> sentences = TextPreprocessor.parseSentences(rawText);

            // Count everything we need before touching the database.
            FileStatistics stats = buildStatistics(sentences);

            // Insert/update imported file metadata and get the file id.
            long fileId = databaseManager.insertImportedFile(filename, stats.totalWordCount(), LocalDateTime.now());

            // Update global word counts (total_count, start_count, end_count).
            for (Map.Entry<String, WordCounters> entry : stats.wordCounters().entrySet()) {
                String word = entry.getKey();
                WordCounters counters = entry.getValue();
                databaseManager.upsertWord(word, counters.totalCount, counters.startCount, counters.endCount);
            }

            // Load ids after all words are inserted/updated, because later tables use word ids.
            Map<String, Long> wordIds = databaseManager.loadExistingWordIds();

            // Store per-file word counts for reporting/debugging.
            for (Map.Entry<String, WordCounters> entry : stats.wordCounters().entrySet()) {
                Long wordId = wordIds.get(entry.getKey());
                if (wordId == null) {
                    throw new IllegalStateException("Word id missing after insert: " + entry.getKey());
                }
                databaseManager.upsertWordFileCount(fileId, wordId, entry.getValue().totalCount);
            }

            // Store word-to-next-word frequencies.
            for (Map.Entry<WordPair, Long> entry : stats.transitions().entrySet()) {
                Long wordId = wordIds.get(entry.getKey().currentWord());
                Long nextWordId = wordIds.get(entry.getKey().nextWord());
                if (wordId == null || nextWordId == null) {
                    throw new IllegalStateException("Transition references missing word ids: " + entry.getKey());
                }
                databaseManager.upsertWordLink(wordId, nextWordId, entry.getValue());
            }

            // Save all changes for this file.
            databaseManager.commit();
            System.out.printf("  Imported %d sentences and %d words.%n",
                    stats.sentenceCount(), stats.totalWordCount());
        } catch (Exception ex) {
            // Undo partial work if anything goes wrong for this file.
            databaseManager.rollbackQuietly();
            System.err.println("  Failed to import " + txtFile + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Builds all statistics required by the assignment from the parsed sentences.
     *
     * For each word we count:
     * - total occurrences
     * - how many times it starts a sentence
     * - how many times it ends a sentence
     *
     * For each adjacent pair we count:
     * - how many times currentWord is followed by nextWord
     */
    private FileStatistics buildStatistics(List<ParsedSentence> sentences) {
        Map<String, WordCounters> counters = new HashMap<>();
        Map<WordPair, Long> transitions = new HashMap<>();
        long totalWords = 0L;

        for (ParsedSentence sentence : sentences) {
            if (sentence.isEmpty()) {
                continue;
            }

            List<String> words = sentence.words();
            totalWords += words.size();

            for (int i = 0; i < words.size(); i++) {
                String word = words.get(i);

                // Create a counter object for the word if this is the first time we have seen it.
                WordCounters wc = counters.computeIfAbsent(word, ignored -> new WordCounters());

                // Every appearance increments the total count.
                wc.totalCount++;

                // First word in the sentence increments start_count.
                if (i == 0) {
                    wc.startCount++;
                }

                // Last word in the sentence increments end_count.
                if (i == words.size() - 1) {
                    wc.endCount++;
                }

                // Record the immediate transition to the next word.
                if (i < words.size() - 1) {
                    WordPair pair = new WordPair(word, words.get(i + 1));
                    transitions.merge(pair, 1L, Long::sum);
                }
            }
        }

        return new FileStatistics(counters, transitions, totalWords, sentences.size());
    }

    /**
     * Bundles all statistics collected for one file.
     */
    private record FileStatistics(Map<String, WordCounters> wordCounters,
                                  Map<WordPair, Long> transitions,
                                  long totalWordCount,
                                  long sentenceCount) {
    }

    /**
     * Mutable counters for a single word while processing one file.
     */
    private static final class WordCounters {
        private long totalCount;
        private long startCount;
        private long endCount;
    }

    /**
     * Key used in the transitions map.
     *
     * Example key: ("the", "cat")
     */
    private record WordPair(String currentWord, String nextWord) {
    }
}
