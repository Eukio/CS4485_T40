//CML210008 CS4485.0W1 03.22.2026
//BPETokenizer.java
package com.example.test.backend;

import static java.lang.System.out;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.core.type.TypeReference;

public class BPETokenizer{
    private List<String> vocab = new ArrayList<>();
    private Map<String,Integer> stoi = new HashMap<>();
    private Map<Integer,String> itos = new HashMap<>();
    private List<String[]> merges = new ArrayList<>();
    private final int updateThreshold=10;
    private final Map<String,Integer> wordCounts = new HashMap<>();
    private int vocabSize=0;
    private int vocabGrowth=0;
    private boolean trained = false;

    public boolean isTrained(){
        return trained;
    }

    public int getVocabGrowth(){
        return vocabGrowth;
    }

    public int getVocabSize(){
        return vocabSize;
    }

    public void resetVocabGrowth(){
        vocabGrowth = 0;
    }

    private Map<String,Integer> getWordTokens(String text){
        Matcher matcher = Pattern.compile("\\w+|[^\\w\\s]").matcher(text.toLowerCase());
        Map<String,Integer> counts = new HashMap<>();
        
        while(matcher.find()) {
            counts.merge(matcher.group(), 1, Integer::sum);
        }
        
        Map<String, Integer> wordTokens = new LinkedHashMap<>();
        for(Map.Entry<String, Integer> entry:counts.entrySet()) {
            String word = entry.getKey();
            String tokenized;
            if(word.chars().allMatch(Character::isLetterOrDigit)){
                tokenized = String.join(" ", word.split(""))+" </w>";
            }else{
                tokenized = word;
            }
            wordTokens.put(tokenized, entry.getValue());
        }
        return wordTokens;
    }

    private Map<String, Integer> getPairs(Map<String, Integer> wordTokens) {
        Map<String, Integer> pairs = new HashMap<>();
        for(Map.Entry<String, Integer> entry:wordTokens.entrySet()) {
            String[] symbols = entry.getKey().split(" ");
            int freq = entry.getValue();
            for(int i=0;i<symbols.length-1;i++){
                pairs.merge(symbols[i]+"\t"+symbols[i+1],freq,Integer::sum);
        }}
        return pairs;
    }

    private Map<String, Integer> mergeVocab(String[] pair, Map<String, Integer> wordTokens){
        Map<String, Integer> newWordTokens = new LinkedHashMap<>();

        String bigram = Pattern.quote(String.join(" ", pair));
        Pattern p = Pattern.compile("(?<!\\S)" + bigram + "(?!\\S)");
        for(Map.Entry<String, Integer> entry:wordTokens.entrySet()){
            String newWord = p.matcher(entry.getKey()).replaceAll(Matcher.quoteReplacement(String.join("",pair)));
            newWordTokens.put(newWord, entry.getValue());
        }
        return newWordTokens;
    }

    public BPETokenizer train(String text, int numMerges){
        /*Train BPE tokenizer on text*/
        out.println("Training BPE tokenizer...");
        if(trained){
            out.println("Tokenizer already trained. Skipping...");
            return this;
        }
        
        Map<String, Integer> wordTokens = this.getWordTokens(text);
        this.merges = new ArrayList<>();
        
        //Start with character-level vocabulary
        Set<String> vocab = new HashSet<>();
        for(String word:wordTokens.keySet()) {
            vocab.addAll(Arrays.asList(word.split(" ")));
        }
        
        for(int i=0;i<numMerges;i++){
            Map<String, Integer> pairs = this.getPairs(wordTokens);
            if(pairs.isEmpty()){
                out.println("No more pairs to merge at iteration "+i);
                break;
            } 
            String bestPairKey = Collections.max(pairs.entrySet(), Map.Entry.comparingByValue()).getKey();
            String [] bestPair = bestPairKey.split("\t");

            if(pairs.get(bestPairKey) < 2){
                out.println("Stopping early at iteration "+i+": pair frequency too low");
                break;
            }
                
            wordTokens = this.mergeVocab(bestPair, wordTokens);
            this.merges.add(bestPair);
            
            // Update vocabulary
            String mergedToken = String.join("", bestPair);
            vocab.add(mergedToken);
            
            if(i % 100 == 0){
                out.println("Merge "+i+": "+bestPairKey+" (freq: "+pairs.get(bestPairKey)+")");
        }}
        // Add special tokens
        vocab.add("<unk>");  // Unknown token
        vocab.add("<pad>");  // Padding token
        vocab.add("<eos>");  // End of sequence
        vocab.add("<bos>");  // Beginning of sequence
        
        this.vocab = new ArrayList<>(vocab);
        Collections.sort(this.vocab);
        this.vocabSize = this.vocab.size();
        
        this.stoi = new HashMap<>();
        this.itos = new HashMap<>();
        for(int i = 0; i < this.vocab.size(); i++) {
            String token = this.vocab.get(i);
            this.stoi.put(token, i);
            this.itos.put(i, token);
        }
        
        out.println("Training complete. Vocabulary size: " + this.vocabSize);
        trained = true;
        return this;
    }

    public List<Integer> encode(String text) {
        if(text == null || text.strip().isEmpty()){
            return new ArrayList<>();
        }

        Map<String, Integer> wordTokens = getWordTokens(text);

        // Apply learned merges in order
        for(String[] pair:this.merges) {
            wordTokens = mergeVocab(pair, wordTokens);
        }

        //[NEW]: Added for extra protection if preprocessing fails to catch something
        int unkId = this.stoi.getOrDefault("<unk>", 0);
        
        // convert to token IDs
        List<Integer> tokens = new ArrayList<>();
        for(String word : wordTokens.keySet()) {
            String[] wordSplit = word.split(" ");
            for(String token:wordSplit){
                if(this.stoi.containsKey(token)){
                    tokens.add(this.stoi.get(token));
                }else{
                    for(char c:token.toCharArray()){
                        tokens.add(this.stoi.getOrDefault(String.valueOf(c), unkId));
        }}}}
        return tokens;
    }

    public String decode(List<Integer> tokenIds) {
        /*Decode token IDs back to text*/
        if(tokenIds == null || tokenIds.isEmpty()){
            return "";
        }

        List<String> tokens = new ArrayList<>();
        for(Integer token_id:tokenIds) {
            if(this.itos.containsKey(token_id)){
                tokens.add(this.itos.get(token_id));
            }else{
                tokens.add("<unk>");
        }}
        String text = String.join("", tokens);
        text = text.replace("</w>", " ");
        text = text.replaceAll("\\s+", " ");
        return text.strip();
    }

    //[NEW]: Considering pkl is python only, we use JSON since it'll be compatible with mySQL.
    // public void save(String filepath){
    //     try{
    //         ObjectMapper mapper = new ObjectMapper();
    //         Map<String, Object> data = new LinkedHashMap<>();
    //         data.put("vocab", this.vocab);
    //         data.put("stoi", this.stoi);
    //         data.put("itos", this.itos);
    //         data.put("merges", this.merges);
    //         data.put("vocabSize", this.vocabSize);
    //         data.put("updateThreshold", this.updateThreshold);
    //         mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filepath), data);
    //         out.println("Tokenizer saved to " + filepath);
    //     }catch(IOException e){
    //         out.println("Error saving tokenizer: " + e.getMessage());
    // }}

    //[NEW]: Considering pkl is python only, we use JSON since it can be compatible with mySQL.
    // @SuppressWarnings("unchecked")
    // public BPETokenizer load(String filepath){
    //     try{
    //         // Read JSON file and populate tokenizer state using ObjectMapper
    //         ObjectMapper mapper = new ObjectMapper();
    //         Map<String, Object> data = mapper.readValue(new File(filepath), new TypeReference<>(){});
    //         this.vocab = (List<String>) data.get("vocab");
    //         this.stoi = (Map<String,Integer>) data.get("stoi");
    //         this.vocabSize = (int) data.get("vocabSize");
    //         this.updateThreshold = (int) data.getOrDefault("updateThreshold", 10);

    //         // Since JSON keys are strings, we need to convert itos keys back to integers
    //         Map<String, String> itosRaw = (Map<String, String>) data.get("itos");
    //         this.itos = new HashMap<>();
    //         for(Map.Entry<String, String> e : itosRaw.entrySet()){
    //             this.itos.put(Integer.parseInt(e.getKey()), e.getValue());
    //         }

    //         // Merges is a list of lists in JSON, we need to convert it back to List<String[]>
    //         List<List<String>> mergesRaw = (List<List<String>>) data.get("merges");
    //         this.merges = new ArrayList<>();
    //         for(List<String> pair : mergesRaw){
    //             this.merges.add(new String[]{pair.get(0), pair.get(1)});
    //         }

    //         out.println("Tokenizer loaded from " + filepath);
    //     }catch(IOException e){
    //         out.println("Error loading tokenizer: " + e.getMessage());
    //         throw new RuntimeException("Failed to load tokenizer", e);
    //     }
    //     return this;
    // }

    public boolean updateVocab(String newText){
        Matcher matcher = Pattern.compile("\\w+|[^\\w\\s]").matcher(newText.toLowerCase());
        while(matcher.find()) {
            this.wordCounts.merge(matcher.group(),1,Integer::sum);
        }
        List<String> newWords = new ArrayList<>();
        for(Map.Entry<String, Integer> entry:this.wordCounts.entrySet()) {
            String word = entry.getKey();
            if(entry.getValue() >= this.updateThreshold && !this.stoi.containsKey(word + "</w>")){
                newWords.add(word + "</w>");
        }}
        
        if(!newWords.isEmpty()){
            out.println("Adding "+newWords.size()+" new words to vocabulary");
            int startIndex = this.vocab.size();
            this.vocab.addAll(newWords);
            this.vocabSize = this.vocab.size();
            
            for(int i=0;i<newWords.size();i++){
                String word = newWords.get(i);
                this.stoi.put(word, startIndex+i);
                this.itos.put(startIndex+i, word);
            }
            //[NEW]: adding a counter instead of saving as this will be called multiple times and to be more efficient we're having a theshold to save
            this.vocabGrowth += newWords.size();
            return true;
        }
        return false;
    }

    //[NEW]: adding trigrams and bigrams as a backup since we will be using markov chains
    //https://web.stanford.edu/~jurafsky/slp3/3.pdf used this as a reference. oddly a fascinating read.
    public List<int[]> toNgrams(List<Integer> tokenIds, int n){
        List<int[]> ngrams = new ArrayList<>();
        for(int i=0;i<tokenIds.size()-(n-1);i++){
            int[] ngram = new int[n];
            for(int j=0;j<n;j++){
                ngram[j] = tokenIds.get(i+j);
            }
            ngrams.add(ngram);
        }
        return ngrams;
    }

    public void getVocabStats(){
        /* Print vocabulary statistics */
        out.println("Vocabulary size: " + this.vocabSize);
        out.println("Number of merges: " + this.merges.size());
        out.println("Special tokens: " + this.vocab.stream().filter(token -> token.startsWith("<")).collect(Collectors.toList()));

        // Show most common merge patterns
        if(!this.merges.isEmpty()){
            out.println("First 10 merges:");
            for(int i=0;i<Math.min(10, this.merges.size());i++){
                String[] merge = this.merges.get(i);
                out.println("  "+(i+1)+": "+merge[0]+" + "+merge[1]+" -> "+String.join("", merge));
}}}}