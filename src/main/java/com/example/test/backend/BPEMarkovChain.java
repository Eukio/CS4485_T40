//CML210008 CS4485.0W1 04.21.2026
//BPEMarkovChain.java

package com.example.test.backend;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class BPEMarkovChain{
    private final WordService wordService;
    private final SentenceBuilder sentenceBuilder;
    private String lastSentence="";
    private final Random rng = new Random();
    private final int maxLength = 50;

    public BPEMarkovChain(WordService wordService, SentenceBuilder sentenceBuilder){
        this.wordService = wordService;
        this.sentenceBuilder = sentenceBuilder;
    }

    public WordCandidate pick(List<WordCandidate> candidates, long currentId, BPETokenizer tokenizer) throws SQLException{
        /**standard issue helper method. */
        return bpePick(candidates, currentId, tokenizer);
    }

    private WordCandidate bpePick(List<WordCandidate> candidates, long currentId, BPETokenizer tokenizer) throws SQLException{
        /** This method picks the next word candidate based on a combination of its frequency and its relationship to the previous word. */
        List<WordCandidate> previousCandidates = wordService.getPreviousWord(currentId);
        List<WordCandidate> merged = boost(candidates, previousCandidates);
        return markovTemperaturePick(merged, tokenizer.encode(wordService.getWordById(currentId) + "").size() > 1 ? 1.5 : 1.0);
    }

    private List<WordCandidate> boost(List<WordCandidate> candidates, List<WordCandidate> previousCandidates){
        /** This method boosts the scores of candidates that have appeared as previous words, to encourage more coherent chains. */
        Map<Long, Long> scores = new HashMap<>();
        for(WordCandidate candidate : candidates){
            scores.put(candidate.id(), candidate.frequency());
        }
        for(WordCandidate candidate : previousCandidates){
            if(scores.containsKey(candidate.id())){
                scores.put(candidate.id(), scores.get(candidate.id()) + candidate.frequency());
            }}
            return candidates.stream().map(candidate -> new WordCandidate(candidate.id(), candidate.word(), scores.getOrDefault(candidate.id(), candidate.frequency()))).collect(Collectors.toList());
    }   

    private WordCandidate markovTemperaturePick(List<WordCandidate> candidates, double temperature){
        /** I promise it's not temperaturePick with a different name. Don't even mention cirular imports. */
        double totalFreq = candidates.stream().mapToDouble(c -> Math.pow(c.frequency(), 1.0 / temperature *1.1)).sum();
        double roll = rng.nextDouble() * totalFreq;
        double cumulative = 0;
        
        for(WordCandidate candidate : candidates){
            cumulative += Math.pow(candidate.frequency(), 1.0 / temperature * 1.1);
            if(roll < cumulative) return candidate;
        }
        return candidates.get(candidates.size() - 1);
    }

    public String buildSentenceBPE(String startingWord, BPETokenizer tokenizer) throws SQLException{
        /** This method would use the BPE tokenizer to generate sentences based on subword units. */
        List<Integer> tokenIds = tokenizer.encode(startingWord); 
        if(tokenIds.isEmpty()){
            return sentenceBuilder.buildSentence(startingWord, 1); //fallback to random weighted pick.
        }

        List<String> words = new ArrayList<>();
        words.add(startingWord.toLowerCase());

        long currentId = wordService.getWordId(startingWord.toLowerCase());
        // long previousId = currentId;// start off with the same word, marked as starting.

        for(int i=0;i<maxLength;i++){
            if(wordService.canEnd(currentId)){
                break;
            }

            List<WordCandidate> candidates = wordService.getNextWord(currentId);
            if(candidates.isEmpty()){
                break;
            }

            List<WordCandidate> previousCandidates = wordService.getPreviousWord(currentId);
            List<WordCandidate> merged = boost(candidates, previousCandidates);
            tokenIds = tokenizer.encode(words.get(words.size() - 1));
            double temperature = tokenIds.size() > 1 ? 1.5 : 1.0; 
            WordCandidate bpeNext = markovTemperaturePick(merged, temperature);

            words.add(bpeNext.word());
            currentId = bpeNext.id();
        }
        this.lastSentence = sentenceBuilder.format(words);
        return this.lastSentence;
}}