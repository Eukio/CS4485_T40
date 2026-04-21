//CML210008 CS4485.0W1 03.22.2026
//SentenceBuilder.java
package com.example.test.backend;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SentenceBuilder{
    private final WordService wordService;
    private final Random rng = new Random();
    private String lastSentence="";
    private int maxLength = 50;

    public SentenceBuilder(WordService wordService){
        /**standard issue constructor*/
        this.wordService = wordService;
    }

    public SentenceBuilder withMaxLength(int maxLength){
        /** limits the freedom of expression to 50 words */
        this.maxLength = maxLength;
        return this;
    }

    public String buildSentence(String startingWord, int algo) throws SQLException{
        /** Will build a sentence starting with the user given word*/
        long currentId = wordService.getWordId(startingWord.toLowerCase());
        List<String> words = new ArrayList<>();
        words.add(startingWord.toLowerCase());

        for(int i = 0; i < maxLength; i++){
            if(wordService.canEnd(currentId)) break;

            List<WordCandidate> candidates = wordService.getNextWord(currentId);
            if(candidates.isEmpty()) break;

            WordCandidate next = switch(algo) {
                case 0 -> greedyPick(candidates);
                case 1 -> weightedPick(candidates);
                //case 2 -> out.println("BPE is future me's problem");
                default -> weightedPick(candidates);
            };

            words.add(next.word());
            currentId = next.id();
        }

        this.lastSentence = format(words);
        return this.lastSentence;
        }

        // this is for my own personal gratification. so i can chain markovs like a madman.
        public SentenceBuilder markovBuildSentence(String startingWord) throws SQLException{
            buildSentence(startingWord, 1);
            return this;
        }

        public String result(){
            return lastSentence;
        }

        private WordCandidate weightedPick(List<WordCandidate> candidates){
            /** Picks a word based on its frequency. As of now it's a simple weighted random selection */
            long total = candidates.stream().mapToLong(WordCandidate::frequency).sum();
            long roll = (long)(rng.nextDouble() * total);
            long cumulative = 0;
            for(WordCandidate candidate : candidates){
                cumulative += candidate.frequency();
                if(roll<cumulative){
                    return candidate;
            }}
            return candidates.get(candidates.size() - 1);
        }

        private WordCandidate greedyPick(List<WordCandidate> candidates){
            /** Just picks the most frequent word. Literal biblical levels of greed. This is the greed talked about in revelations. */
            return candidates.get(0);
        }

        private String format(List<String> words){
            /** Formats the list of words into a sentence */
            if(words.isEmpty()){
                return "";
            }
            String first = words.get(0);
            words.set(0, Character.toUpperCase(first.charAt(0)) + first.substring(1));
            return String.join(" ", words) + ".";
    }



}


