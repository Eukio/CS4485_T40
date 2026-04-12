package com.example.test;

import java.io.FileInputStream;
import java.io.IOException;
import static java.lang.System.out;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import com.example.test.backend.GeneratedSentence;
import com.example.test.backend.SentenceBuilder;
import com.example.test.backend.SentenceHistory;
import com.example.test.backend.WordService;
import com.example.test.db.DatabaseConfig;
import com.example.test.db.DatabaseManager;
import com.example.test.service.BookFolderImporter;

/**
 * Program entry point.
 *
 * This class only handles:
 * 1) reading command-line arguments,
 * 2) validating the books folder,
 * 3) creating the database connection layer,
 * 4) launching the folder importer.
 *
 * Keeping main() small makes the project easier to test and maintain.
 */
public class Main {
    private static Properties loadConfig() throws IOException {
        Properties props = new Properties();
        try(FileInputStream fis = new FileInputStream("configsql.properties")){
            props.load(fis);
        }
        if(props.getProperty("db.jdbcUrl") == null){
            throw new RuntimeException("Missing required property: db.jdbcUrl");
        }
        //props.load(new FileInputStream("configsql.properties"));
        return props;
    }

    public static void main(String[] args) {
        Properties props;
        try {
            props = loadConfig();
        } catch (IOException e) {
            System.err.println("Could not load configsql.properties: " + e.getMessage());
            return;
        }

        String jdbcUrl = args.length > 0 ? args[0] : props.getProperty("db.jdbcUrl");
        String username = args.length > 1 ? args[1] : props.getProperty("db.username");
        String password = args.length > 2 ? args[2] : props.getProperty("db.password");
        String folderPath = args.length > 3 ? args[3] : "src/main/java/com/example/test/books";
        boolean skipAlreadyImported = args.length > 4 ? Boolean.parseBoolean(args[4]) : true;


        
        // Stop early if the folder path is invalid.
        Path folder = Paths.get(folderPath);

        if (!Files.isDirectory(folder)) {
            System.err.println("Error: folder does not exist or is not a directory -> " + folderPath);
            return;
        }

        // Bundle connection settings into one object so they can be passed cleanly.
        DatabaseConfig config = new DatabaseConfig(jdbcUrl, username, password);

        // try-with-resources ensures the database connection closes automatically.
        try (DatabaseManager databaseManager = new DatabaseManager(config)) {
            BookFolderImporter importer = new BookFolderImporter(databaseManager); // hello there
            folder = Paths.get(folderPath);
            importer.importFolder(folder, skipAlreadyImported);
            System.out.println("Import completed successfully.");
        } catch (Exception ex) {
            System.err.println("Import failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    
        out.println("\n--- Running tests for backend classes ---");
        try (DatabaseManager db = new DatabaseManager(new DatabaseConfig(jdbcUrl, username, password))) {
            testWordService(db);
            testSentenceBuilder(db);
            testSentenceHistory(db);
        } catch (Exception e) {
            System.err.println("Tests failed: " + e.getMessage());
            e.printStackTrace();
        }
    
    
    }

    /**
     * Prints the exact command format expected by the program.
     */
    private static void printUsage() {
        out.println("Usage:");
        out.println("java -jar sentence-builder-importer-1.0.0-jar-with-dependencies.jar " +
                "<jdbcUrl> <dbUser> <dbPassword> <folderPath> <skipAlreadyImported>");
        out.println();
        out.println("Example:");
        out.println("java -jar target/sentence-builder-importer-1.0.0-jar-with-dependencies.jar " +
                "jdbc:mysql://localhost:3306/sentence_builder root mypassword C:/books true");
    }

    private static void testWordService(DatabaseManager db){
        out.println("\n--- Testing WordService.java Yatta! ---");
        try{
            WordService ws = new WordService(db);
            out.println("wordExists('the'): " + ws.wordExists("the"));
            long id = ws.getWordId("the");
            out.println("getWordId('the'): " + id);
            out.println("canEnd: " + ws.canEnd(id));
            out.println("canStart: " + ws.canStart(id));
            ws.getNextWord(id).stream().limit(5).forEach(c ->
                out.println("  next: " + c.word() + " freq: " + c.frequency()));
            out.println("Yatta! WordService Passed!");
        }catch(Exception e){
            System.err.println("WordService is not daijoubu :( reason: " + e.getMessage());
            e.printStackTrace();
    }}

    private static void testSentenceBuilder(DatabaseManager db){
        out.println("\n--- Testing SentenceBuilder.java Wahoo! ---");
        try{
            WordService ws = new WordService(db);
            SentenceBuilder sb = new SentenceBuilder(ws);

            String weighted = sb.buildSentence("the", 1);
            out.println("Weighted: " + weighted);

            String greedy = sb.buildSentence("the", 0);
            out.println("Greedy:   " + greedy);

            String chained = sb.withMaxLength(10)
                .markovBuildSentence("the")
                .markovBuildSentence("the")
                .result();
            out.println("Chained:  " + chained);

            out.println("SentenceBuilder passed. Yay!");
        }catch(Exception e){
            System.err.println("SentenceBuilder fail le. I so disapointed. Reason: " + e.getMessage());
            e.printStackTrace();
    }}

    private static void testSentenceHistory(DatabaseManager db){
        out.println("\n--- Testing SentenceHistory.java We like having fun around here ---");
        try{
            SentenceHistory sh = new SentenceHistory(db);

            sh.save("Annie said meow", "weighted");
            sh.save("Annie said meow", "weighted");
            sh.save("A different sentence entirely.", "greedy");

            out.println("isDuplicate: " + sh.isDuplicate("Annie said meow"));
            out.println("getDuplicates: " + sh.getDuplicates());

            out.println("getAll:");
            for (GeneratedSentence gs : sh.getAll()) {
                out.println("  [" + gs.algorithm() + "] " + gs.sentence());
            }

                out.println("SentenceHistory passed. We are having fun! :)");
        }catch(Exception e){
            System.err.println("SentenceHistory failed. we did not in fact have fun. reason: " + e.getMessage());
            e.printStackTrace();
}}}