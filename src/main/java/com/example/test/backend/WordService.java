package com.example.test.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.test.db.DatabaseManager;

public class WordService{
    private final Connection con;
    public WordService(DatabaseManager dbManager){
        /**standard issue connection */
        this.con = dbManager.connection();
    }

    @FunctionalInterface
    /**This isn't as common or familiar as my other code, so I'll go into deeper detail.
     * Basically this helps with reducing the complexity of writing multiple queries.
     * Instead of writing the same try with resources block every time, I can just write the query and then handle the result set in a lambda.
     * It's a bit more abstract but it reduces boilerplate and makes the code cleaner.
     * Plus, it reduces the amount of code I have to write, and I would rather write less than write more.
     * I'm not being paid after all.*/
    private interface ResultSetHandler<T>{
        /** I had to split this off because lambda expressions are annoying*/
        T handle(ResultSet resSet) throws SQLException;
    }

    private record AutoCompleteKey(long wordId, int limit){};

    private final Map<AutoCompleteKey, List<WordCandidate>> autocompleteCache = new ConcurrentHashMap<>();

    

    private <T> T query(String sql, ResultSetHandler<T> handler, Object... params) throws SQLException{
        /** This guy is the NOT lambda part. It handles all the annoying JDBC stuff.
         * It just throws the query, and then it passes the result set to the lambda to handle.
         * The lambda handler then throws back whatever type T the lambda cooked up.
         * Is it lazy? Probably. But I get to write less code this way.
         */
        try(PreparedStatement prepStat = con.prepareStatement(sql)){
            for(int i=0; i<params.length;i++){
                prepStat.setObject(i+1, params[i]);
            }
            try(ResultSet resSet = prepStat.executeQuery()){
                return handler.handle(resSet);
            }}}




    public long getWordId(String word) throws SQLException{
        /** gets the id of a word, if it doesn't exist it creates it and then returns the id. */
        return query("SELECT id FROM words WHERE word = ?", resSet -> {
            if(resSet.next()){
                return resSet.getLong("id");
            }
            throw new SQLException("Word not found: " + word);
        }, word.toLowerCase());
    }

    public boolean canEnd(long wordId) throws SQLException{
        /** searches the database for the word's ID and it checksthe column if it can end, and if it is true returns true. else false. */
        return query("SELECT can_end FROM words WHERE id = ?", resSet -> resSet.next() && resSet.getBoolean("can_end"), wordId);
    }

    public boolean canStart(long wordId) throws SQLException{
        /** same thing as canEnd but for the can_start column. Because i'm lazy */
        return query("SELECT can_start FROM words WHERE id = ?", resSet -> resSet.next() && resSet.getBoolean("can_start"), wordId);
    }

    public List<WordCandidate> getNextWord(long wordId) throws SQLException{
        /** Gets the next words in the sequence */
        String sql = """
                SELECT w1.next_word_id, w.word, w1.frequency
                FROM word_links w1
                JOIN words w ON w.id = w1.next_word_id
                WHERE w1.word_id = ?
                ORDER BY w1.frequency DESC
                """;
        return query(sql, resSet -> {
            List<WordCandidate> results = new ArrayList<>();
            while(resSet.next()){
                results.add(new WordCandidate(resSet.getLong("next_word_id"), resSet.getString("word"), resSet.getLong("frequency")));
            }
            return results;
        }, wordId);
    }

    public List<WordCandidate> getPreviousWord(long wordId) throws SQLException{
        /** same as getNextWord but for the previous words in the sequence */
        String sql = """
                SELECT w1.word_id, w.word, w1.frequency
                FROM word_links w1
                JOIN words w ON w.id = w1.word_id
                WHERE w1.next_word_id = ?
                ORDER BY w1.frequency DESC
                """;
        return query(sql, resSet -> {
            List<WordCandidate> results = new ArrayList<>();
            while(resSet.next()){
                results.add(new WordCandidate(resSet.getLong("word_id"), resSet.getString("word"), resSet.getLong("frequency")));
            }
            return results;
        }, wordId);
    }

    public List<WordCandidate> getAutocompleteCandidates(long wordId, int limit) throws SQLException{
        /** Gets autocomplete candidates based on a prefix */
        AutoCompleteKey key = new AutoCompleteKey(wordId, limit);

        if (autocompleteCache.containsKey(key)) {
            System.out.println("CACHE HIT: " + key);
            return autocompleteCache.get(key);
        }
        
        String sql = """
                SELECT w1.next_word_id, w.word, w1.frequency
                FROM word_links w1
                JOIN words w ON w.id = w1.next_word_id
                WHERE w1.word_id = ?
                ORDER BY w1.frequency DESC
                LIMIT ?
                """;
        List<WordCandidate> result = query(sql, resSet -> {
            List<WordCandidate> results = new ArrayList<>();
            while(resSet.next()){
                results.add(new WordCandidate(
                    resSet.getLong("next_word_id"),
                    resSet.getString("word"),
                    resSet.getLong("frequency")
                ));
            }
            return results;
        }, wordId, limit);

        System.out.println("CACHE MISS: " + key);
        autocompleteCache.put(key, result);
        return result;
    }

    public String getWordById(long wordId) throws SQLException{
        /** Gets a word by its ID. */
        String sql = "SELECT word FROM words WHERE id = ?";
        return query(sql, resSet -> {
            if(resSet.next()){
                return resSet.getString("word");
            }
            throw new SQLException("Word not found: " + wordId);
        }, wordId);
    }

    public boolean wordExists(String word) throws SQLException{
        /** checks if a word exists in the database. */
        return query("SELECT id FROM words WHERE word = ?", resSet -> resSet.next(), word.toLowerCase());
    }
    public void newWord(String prev, String curr) throws SQLException{
        System.out.println(prev + " " + curr);
        if(wordExists(prev) && wordExists(curr)){
            // update
            System.out.println(prev + " exists.");

        }
            boolean wordLinkExists = (wordExists(prev) && wordExists(curr)) ? query(
                """
                     SELECT EXISTS (
                         SELECT 1
                         FROM word_links
                         WHERE word_id = ? AND next_word_id = ?
                     );   
                """
                , resSet -> (resSet.next() ? resSet.getBoolean(1) : false), getWordId(prev.toLowerCase()), getWordId(curr.toLowerCase())) : false;
            System.out.println(prev + "->" + curr + " " + (wordLinkExists ? "Exists" : "Does not Exist"));
            if(!wordLinkExists) { //insert wordLink Count
                String sql = """
                            INSERT INTO word_links
                            VALUES (?, ?, 1);
                        """;
                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    // pstmt.setInt(1, 101); // Set the first parameter (?) to 101
                    pstmt.setString(1, prev);
                    pstmt.setString(2, curr);
                    int numUpdate = pstmt.executeUpdate();

                    if (numUpdate == 0) {
                        System.out.println(prev + " then " + curr);
                    }
                }
                System.out.println("added " + prev + "->" + curr);
            }else { //update link Count
                String sql = """
                    UPDATE word_links w1
                    SET frequency = frequency + 1
                    WHERE word_id = ? AND next_word_id = ?;        
                """;
                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    // pstmt.setInt(1, 101); // Set the first parameter (?) to 101
                    pstmt.setLong(1, getWordId(prev));
                    pstmt.setLong(2, getWordId(curr));
                    int numUpdate = pstmt.executeUpdate();

                    if (numUpdate == 0) {
                        System.out.println(prev + " then " + curr);
                    }
                }
                System.out.println("incremented " + prev + "->" + curr);
            }
    }
}
