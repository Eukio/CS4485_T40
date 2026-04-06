package com.example.test.model;

import java.util.List;

/**
 * Represents one parsed sentence as a list of normalized words.
 *
 * Example:
 *   "The cat sleeps." -> ["the", "cat", "sleeps"]
 */
public record ParsedSentence(List<String> words) {
    /**
     * Convenience helper used during import so we can skip empty results safely.
     */
    public boolean isEmpty() {
        return words == null || words.isEmpty();
    }
}
