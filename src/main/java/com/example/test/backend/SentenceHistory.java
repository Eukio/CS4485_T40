//CML210008 CS4485.0w1 04.11.2026
//SentenceHistory.java
package com.example.test.backend;

import com.example.test.db.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SentenceHistory{
    private final Connection con;

    public SentenceHistory(DatabaseManager dbManager){
        /**standard issue connection */
        this.con = dbManager.connection();
    }

    @FunctionalInterface
    /**I stole this line for line from the WordService class. It's the same exact thing. */
    private interface ResultSetHandler<T>{
        T handle(ResultSet resSet) throws SQLException;
    }

    private <T> T query(String sql, ResultSetHandler<T> handler, Object... params) throws SQLException{
        try(PreparedStatement prepStat = con.prepareStatement(sql)){
            for(int i=0; i<params.length;i++){
                prepStat.setObject(i+1, params[i]);
            }
            try(ResultSet resSet = prepStat.executeQuery()){
                return handler.handle(resSet);
    }}}


    public void save(String sentence, String algo) throws SQLException{
        /** Saves a generated sentence to the database. */
        String sql = "INSERT INTO generated_sentences(sentence, algorithm) VALUES (?, ?)";
        try(PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, sentence);
            ps.setString(2, algo);
            ps.executeUpdate();
    }}

    public boolean isDuplicate(String sentence) throws SQLException{
        /** Checks if an identical sentence has been generated before. */
        return query("SELECT 1 FROM generated_sentences WHERE sentence = ? LIMIT 1", resSet -> resSet.next(), sentence);
    }

    public List<GeneratedSentence> getAll() throws SQLException{
        /** Returns all generated sentences, most recent first. */
        String sql = """
            SELECT id, sentence, algorithm, created_at
            FROM generated_sentences
            ORDER BY created_at DESC
            """;
        return query(sql, resSet -> {
            List<GeneratedSentence> results = new ArrayList<>();
            while(resSet.next()) {
                results.add(new GeneratedSentence(
                    resSet.getLong("id"),
                    resSet.getString("sentence"),
                    resSet.getString("algorithm"),
                    resSet.getTimestamp("created_at").toLocalDateTime()
                ));
            }
            return results;
        });
    }

    public List<String> getDuplicates() throws SQLException{
        /** Returns sentences that have been generated more than once. */
        String sql = """
            SELECT sentence
            FROM generated_sentences
            GROUP BY sentence
            HAVING COUNT(*) > 1
            ORDER BY COUNT(*) DESC
            """;
        return query(sql, resSet -> {
            List<String> results = new ArrayList<>();
            while(resSet.next()) {
                results.add(resSet.getString("sentence"));
            }
            return results;
        });
}}
