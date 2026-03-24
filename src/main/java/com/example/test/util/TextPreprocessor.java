package com.example.test.util;

import com.example.test.model.ParsedSentence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class that converts raw text into normalized sentences and words.
 *
 * Important design choices:
 * - sentence boundaries are approximated using ., !, ?
 * - words are lowercased
 * - apostrophes inside words are preserved (don't, it's)
 * - punctuation around words is ignored
 */
public final class TextPreprocessor {
    // Split after sentence-ending punctuation when it is followed by whitespace.
    private static final Pattern SENTENCE_SPLIT_PATTERN = Pattern.compile("(?<=[.!?])\\s+");

    // Matches words made of letters/digits, with optional apostrophe groups inside the word.
    // Examples matched: hello, don't, it's, room101
    private static final Pattern WORD_PATTERN = Pattern.compile("[a-z0-9]+(?:'[a-z0-9]+)*");

    private TextPreprocessor() {
        // Utility class: prevent instantiation.
    }

    /**
     * Converts raw text into a list of parsed sentences.
     */
    public static List<ParsedSentence> parseSentences(String text) {
        String normalized = normalizeRawText(text);
        String[] chunks = SENTENCE_SPLIT_PATTERN.split(normalized);
        List<ParsedSentence> sentences = new ArrayList<>();

        for (String chunk : chunks) {
            List<String> words = tokenize(chunk);
            if (!words.isEmpty()) {
                sentences.add(new ParsedSentence(words));
            }
        }
        return sentences;
    }

    /**
     * Extracts normalized words from one string chunk.
     */
    public static List<String> tokenize(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        Matcher matcher = WORD_PATTERN.matcher(lower);
        List<String> words = new ArrayList<>();

        while (matcher.find()) {
            words.add(matcher.group());
        }
        return words;
    }

    /**
     * Performs simple normalization before sentence splitting/tokenization.
     *
     * This helps handle many text files that contain curly quotes, em dashes,
     * line breaks, and inconsistent spacing.
     */
    private static String normalizeRawText(String text) {
        return text
                .replace('\u2018', '\'') // left single quotation mark -> apostrophe
                .replace('\u2019', '\'') // right single quotation mark -> apostrophe
                .replace('\u201C', '"')  // left double quote -> normal quote
                .replace('\u201D', '"')  // right double quote -> normal quote
                .replace('\u2014', ' ')  // em dash -> space
                .replace('\u2013', ' ')  // en dash -> space
                .replace('\r', ' ')      // carriage returns -> space
                .replace('\n', ' ')      // newlines -> space
                .replaceAll("\\s+", " ")
                .trim();
    }
}
