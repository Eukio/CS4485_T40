-- ============================================================
-- Sentence Builder database schema
--
-- This schema supports the assignment requirements:
-- 1) store words and their frequencies
-- 2) store sentence-start and sentence-end counts
-- 3) store word -> next_word transitions
-- 4) store imported file metadata
-- 5) store generated sentence history for later features
-- ============================================================

CREATE DATABASE IF NOT EXISTS sentence_builder
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE sentence_builder;

-- ------------------------------------------------------------
-- imported_files
--
-- One row per imported text file.
-- Tracks:
-- - file name/path
-- - total words found in that file
-- - when the file was imported
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS imported_files (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  filename     VARCHAR(512)    NOT NULL,
  word_count   BIGINT UNSIGNED NOT NULL DEFAULT 0,
  imported_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_imported_files_filename (filename),
  KEY idx_imported_files_imported_at (imported_at)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- words
--
-- One row per unique normalized word.
-- Tracks:
-- - total_count: total appearances across all imported files
-- - start_count: times the word starts a sentence
-- - end_count: times the word ends a sentence
-- - can_start / can_end: helpful metadata flags for later generation
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS words (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  word         VARCHAR(128)    NOT NULL,
  total_count  BIGINT UNSIGNED NOT NULL DEFAULT 0,
  start_count  BIGINT UNSIGNED NOT NULL DEFAULT 0,
  end_count    BIGINT UNSIGNED NOT NULL DEFAULT 0,
  can_start    TINYINT(1)      NOT NULL DEFAULT 0,
  can_end      TINYINT(1)      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uq_words_word (word),
  KEY idx_words_total_count (total_count),
  KEY idx_words_start_count (start_count),
  KEY idx_words_end_count   (end_count)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- word_links
--
-- Directed edges between words.
-- Example: if "the" is followed by "cat", then this table stores
-- (word_id for "the", next_word_id for "cat", frequency).
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS word_links (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  word_id       BIGINT UNSIGNED NOT NULL,
  next_word_id  BIGINT UNSIGNED NOT NULL,
  frequency     BIGINT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uq_word_links_pair (word_id, next_word_id),
  KEY idx_word_links_word_id (word_id),
  KEY idx_word_links_next_word_id (next_word_id),
  CONSTRAINT fk_word_links_word
    FOREIGN KEY (word_id) REFERENCES words(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_word_links_next_word
    FOREIGN KEY (next_word_id) REFERENCES words(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- generated_sentences
--
-- Not used by the importer yet, but included because the full project
-- needs to remember generated sentences and detect duplicates.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS generated_sentences (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  sentence      TEXT            NOT NULL,
  created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  algorithm     VARCHAR(64)     NULL,
  starting_word VARCHAR(128)    NULL,
  PRIMARY KEY (id),
  KEY idx_generated_sentences_created_at (created_at),
  KEY idx_generated_sentences_sentence_prefix (sentence(255))
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- word_file_counts
--
-- Optional helper table that stores how many times each word appears
-- inside each imported file.
-- Useful for reporting and debugging.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS word_file_counts (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  file_id       BIGINT UNSIGNED NOT NULL,
  word_id       BIGINT UNSIGNED NOT NULL,
  count_in_file BIGINT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uq_word_file (file_id, word_id),
  KEY idx_wfc_word_id (word_id),
  CONSTRAINT fk_wfc_file
    FOREIGN KEY (file_id) REFERENCES imported_files(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_wfc_word
    FOREIGN KEY (word_id) REFERENCES words(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB;

SHOW TABLES