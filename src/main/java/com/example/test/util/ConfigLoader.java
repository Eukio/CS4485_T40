package com.example.test.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class ConfigLoader {
    public static Properties loadConfig() throws IOException {
        Properties props = new Properties();
        try(FileInputStream fis = new FileInputStream("configsql.properties")){
            props.load(fis);
        }
        if(props.getProperty("db.jdbcUrl") == null){
            throw new RuntimeException("Missing required property: db.jdbcUrl");
        }
        return props;
    }


}
