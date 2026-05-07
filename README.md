# Sentence Builder

Sentence Builder is a JavaFX application that generates short sentences using corpus-driven Markov-style models and an optional BPE (Byte Pair Encoding) tokenizer. The project stores word statistics and transitions in a MySQL database and exposes generation features through a simple frontend UI.

**What this repo contains**
- Backend: data access, generation algorithms, BPE tokenizer, and persistence.
- Frontend: JavaFX UI with scenes for building sentences, autocomplete, uploads, and reports.
- Utilities: corpus loader, configuration loader, and DB helpers.

**Architecture (high level)**
- **Backend**
	- `WordService` — database access wrapper for words and transitions ([src/main/java/com/example/test/backend/WordService.java](src/main/java/com/example/test/backend/WordService.java)).
	- `SentenceBuilder` — orchestrates sentence generation and selection strategies (greedy, weighted, temperature, BPE) ([src/main/java/com/example/test/backend/SentenceBuilder.java](src/main/java/com/example/test/backend/SentenceBuilder.java)).
	- `BPEMarkovChain` & `BPETokenizer` — optional subword-based selection using a trained BPE vocabulary ([src/main/java/com/example/test/backend/BPEMarkovChain.java](src/main/java/com/example/test/backend/BPEMarkovChain.java)) and ([src/main/java/com/example/test/backend/BPETokenizer.java](src/main/java/com/example/test/backend/BPETokenizer.java)).
	- `SentenceHistory` — stores and queries previously generated sentences ([src/main/java/com/example/test/backend/SentenceHistory.java](src/main/java/com/example/test/backend/SentenceHistory.java)).
	- `DatabaseManager` — central JDBC helper used by importers and services ([src/main/java/com/example/test/db/DatabaseManager.java](src/main/java/com/example/test/db/DatabaseManager.java)).

- **Frontend**
	- JavaFX entry: `HelloApplication` (startup, DB config, background tokenizer training) ([src/main/java/com/example/test/HelloApplication.java](src/main/java/com/example/test/HelloApplication.java)).
	- Scenes: `BuildSentencesScene` contains the Generate button and invokes generation ([src/main/java/com/example/test/Scenes/BuildSentencesScene.java](src/main/java/com/example/test/Scenes/BuildSentencesScene.java)).
	- The app can be launched with `com.example.test.Launcher` or directly from the IDE.

- **Utilities / Data**
	- `CorpusLoader` — loads the local corpus text used to train the BPE tokenizer ([src/main/java/com/example/test/util/CorpusLoader.java](src/main/java/com/example/test/util/CorpusLoader.java)).
	- `ConfigLoader` — reads `configsql.properties` for DB connection and settings ([src/main/java/com/example/test/util/ConfigLoader.java](src/main/java/com/example/test/util/ConfigLoader.java)).
	- `TextPreprocessor` — preprocessing helpers used by the importer ([src/main/java/com/example/test/util/TextPreprocessor.java](src/main/java/com/example/test/util/TextPreprocessor.java)).

**Database**
- Schema file: [src/main/resources/schema.sql](src/main/resources/schema.sql). Key tables:
	- `words`: unique normalized words and start/end flags.
	- `word_links`: directed transitions and frequencies between words (used by the Markov generator).
	- `imported_files`: metadata for source files.
	- `generated_sentences`: persisted generated sentences and timestamps.

**How generation (inference) works**
1. App startup (`HelloApplication`) configures the DB and creates `WordService`.
2. The BPE tokenizer is trained in a background thread (if enabled) using `CorpusLoader`.
3. When the user clicks **Generate Sentence** in `BuildSentencesScene`, the UI creates a new `SentenceBuilder` and calls `buildSentence(seed, algorithm)` which:
	 - looks up the seed word id via `WordService`,
	 - retrieves next-word candidates from `word_links`,
	 - selects the next token using one of several strategies (greedy, weighted random, temperature, or BPE-augmented),
	 - repeats until a terminal condition (max length, `can_end`, no candidates).

**Run / Setup (local)**
Prerequisites:
- Java 21 (project compiled for Java 21 per `pom.xml`).
- MySQL 8+ (or compatible) running and reachable.

1. Import the schema into your MySQL instance (adjust user/host as needed):

```bash
mysql -u <user> -p < src/main/resources/schema.sql
```

2. Edit `configsql.properties` (root of project) and set `db.jdbcUrl`, `db.username`, `db.password` and any other settings. See [configsql.properties](configsql.properties).

3. Build and run with the included Maven wrapper (Windows example):

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd clean javafx:run
```

Alternatively run from your IDE using the launcher class `com.example.test.Launcher` or `com.example.test.HelloApplication`.

**Developer notes & testing**
- The repository contains console test helpers in `Main.java` which show example calls to the generator and BPE flow ([src/main/java/com/example/test/Main.java](src/main/java/com/example/test/Main.java)).
- The BPE tokenizer training can be expensive depending on the corpus; the app trains it in a background thread on startup.
- For import and debugging, `DatabaseManager` provides utility methods used by the importer to upsert words and transitions.

**Next steps / improvements**
- Persist and expose the trained tokenizer state for faster startup.
- Add unit tests for generation strategies and DB-layer mocks.
- Improve BPE integration for longer-context selection.

