package com.example.test.backend;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.util.Properties;
import java.sql.Connection;



public class TestWService {
    public Properties loadConfig() throws IOException {
        Properties props = new Properties();

        props.load(Files.newInputStream(
            Paths.get("C:/Users/joshu/Documents/Projects/CSCapstone/CS4485_T40/configsql.properties")
        ));
        return props;
    }
    
    public void connectFromConfig() throws Exception {
        Properties props = loadConfig();

        String url = props.getProperty("db.jdbcUrl");
        String user = props.getProperty("db.username");
        String pass = props.getProperty("db.password");

        con = DriverManager.getConnection(url, user, pass);
    }


    public static void main(String[] args){
        WordService ws = new WordService();
        try{
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
