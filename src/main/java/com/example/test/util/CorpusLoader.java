//CML210008 CS4485.0W1 04.21.2026
//CorpusLoader.java

package com.example.test.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CorpusLoader{
    public static String loadCorpusText(){
        Path booksFolder = Paths.get("src/main/java/com/example/test/books");
        StringBuilder corpus = new StringBuilder();
        try{
            Files.list(booksFolder).filter(p->p.toString().endsWith(".txt")).forEach(p -> {
                try{
                    String text = Files.readString(p);
                    List<String> words = TextPreprocessor.tokenize(text);
                    corpus.append(String.join(" ", words)).append(" ");
                }catch(IOException e){
                    System.err.println("Error reading file: " + p + " -> " + e.getMessage());
                }});
        }catch(IOException e){
            System.err.println("Error listing books folder: " + e.getMessage());
        }
        return corpus.toString();   
}}
