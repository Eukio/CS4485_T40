package com.example.test;

import java.io.IOException;
import static java.lang.System.out;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

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
    public static void main(String[] args) throws IOException {
        // We expect exactly 5 arguments:
        // 0 = JDBC URL
        // 1 = DB username
        // 2 = DB password
        // 3 = folder containing .txt books
        // 4 = whether to skip files already imported (true/false)

        /*
        if (args.length != 5) {
            printUsage();
            return;
        }

        String jdbcUrl = args[0];
        String username = args[1];
        String password = args[2];
        Path folderPath = Paths.get(args[3]);
        boolean skipAlreadyImported = Boolean.parseBoolean(args[4]);
        */

        Properties props = new Properties();
        props.load(Files.newInputStream(Paths.get("C:/Users/joshu/Documents/Projects/CSCapstone/CS4485_T40/configsql.properties")));

        String jdbcUrl = args.length > 0 ? args[0] : props.getProperty("db.jdbcUrl");
        String username = args.length > 1 ? args[1] : props.getProperty("db.username");
        String password = args.length > 2 ? args[2] : props.getProperty("db.password");
        String folderPath = args.length > 3 ? args[3] : props.getProperty("db.folderPath");
        boolean skipAlreadyImported = args.length > 4 ? Boolean.parseBoolean(args[4]) : Boolean.parseBoolean(props.getProperty("db.skipAlreadyImported"));


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
}
