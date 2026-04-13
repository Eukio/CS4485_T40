package com.example.test.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WordService {
    private String word;
    private int count;
    private int id;
    private String nextWord;
    private String previousWord;
    private Connection con;

    public record WordCandidate(long id, String word, long frequency) {}



    public WordService(String word, int count, int id, String nextWord, String previousWord) {
        this.word = word;
        this.count = count;
        this.id = id;
        this.nextWord = nextWord;
        this.previousWord = previousWord;
        
    }
    public int getId() { return id; }
    public String getText() { return word; }
    public int getCount() { return count; }
    public String getPrevWord() { return previousWord; }
    public String getNextWord() { return nextWord; }

    public void connection(String url, String user, String password) throws SQLException {
         con = DriverManager.getConnection(url, user, password);
    }
    public void close() throws SQLException {
        if (con != null) con.close();
    }
    
    //grab user input here
    public String getStartingWord(){
        
        return null;
    }

    public List<WordCandidate> getNextWord(long wordId) throws SQLException{// return a list? (like the datastructure list)
        String sqlStatement = """
                SELECT wl.next_word_id, w.word, wl.frequency
                FROM word_links wl
                JOIN words w on w.id = wl.next_word_id
                WHERE wl.word_id = ? 
                ORDER BY wl.frequency DESC
                """;

        List<WordCandidate> results = new ArrayList<>();
        try(PreparedStatement prepStmt = con.prepareStatement(sqlStatement)){
            prepStmt.setLong(1, wordId);
            try(ResultSet rs = prepStmt.executeQuery()){
                while(rs.next()){
                    results.add(new WordCandidate(
                        rs.getLong("next_word_id"),
                        rs.getString("word"),
                        rs.getLong("frequency")
                    ));

                }
            }
        
        
        }catch(SQLException e){
            e.printStackTrace();
        }
        //return a list of the id, word(string) and frequency(as long). 
        return results;
    }

    //public boolean canEnd(long wordId){}

    //public String handleDeadEnd(){}

}