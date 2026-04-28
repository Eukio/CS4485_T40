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
    private final int threshold = 15;
    private BPETokenizer tokenizer = null;
    private BPEMarkovChain bpeChain = null;
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


    public String buildSentence(String startingWord, int algo) throws SQLException {
        // If no starting word, pick a random one from the DB
        if (startingWord == null || startingWord.isBlank()
                || startingWord.equalsIgnoreCase("[END]")
                || startingWord.equals(".")
                || !wordService.wordExists(startingWord.toLowerCase())) {
            startingWord = wordService.getRandomWord();
        }

        long currentId = wordService.getWordId(startingWord.toLowerCase());
        List<String> words = new ArrayList<>();
        words.add(startingWord.toLowerCase());

        for (int i = 0; i < maxLength; i++) {
            List<WordCandidate> candidates = wordService.getNextWord(currentId);
            if (candidates.isEmpty()) {
                break;
            }
            if (i >= 3 && wordService.canEnd(currentId) && candidates.size() < threshold) {
                break;
            }

            WordCandidate next = switch (algo) {
                case 0 -> greedyPick(candidates);
                case 1 -> weightedPick(candidates);
                case 2 -> temperaturePick(candidates, 1.5);
                case 3 -> bpeChain != null ? bpeChain.pick(candidates, currentId, tokenizer) : weightedPick(candidates);
                default -> weightedPick(candidates);
            };

            //exclude the word [END]
            if (next.word().equalsIgnoreCase("[END]")) {
                break;
            }
            words.add(next.word());
            currentId = next.id();
        }

        this.lastSentence = format(words);
        return this.lastSentence;
    }
        public SentenceBuilder withBPE(BPEMarkovChain bpeChain, BPETokenizer tokenizer){
            /** This is a bit of a hack to avoid circular imports. I want the sentence builder to be able to use the BPEMarkovChain's pick method, but the BPEMarkovChain also needs to use the SentenceBuilder for its markovBuildSentence method. So instead of passing the BPEMarkovChain in the constructor, I just pass it in here. It's not ideal, but it works. */
            this.bpeChain = bpeChain;
            this.tokenizer = tokenizer;
            return this;
        }

        // this is for my own personal gratification. so i can chain markovs like a madman.
        public SentenceBuilder markovBuildSentence(String startingWord) throws SQLException{
            buildSentence(startingWord, 3);
            return this;
        }

        public String result(){
            return lastSentence;
        }

        protected WordCandidate weightedPick(List<WordCandidate> candidates){
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

        protected WordCandidate greedyPick(List<WordCandidate> candidates){
            /** Just picks the most frequent word. Literal biblical levels of greed. This is the greed talked about in revelations. */
            return candidates.get(0);
        }

        protected WordCandidate temperaturePick(List<WordCandidate> candidates, double temperature){
            // Got bored. Temperature scaling is pretty easy - just scale frequencies by a set temperature. 
            // that's a lie. I stole this from my ML class notes. 
            double total = candidates.stream().mapToDouble(c -> Math.pow(c.frequency(), 1.0 / temperature)).sum();
            double roll = rng.nextDouble() * total;
            double cumulative = 0;
            
            for(WordCandidate candidate : candidates){
                cumulative += Math.pow(candidate.frequency(), 1.0 / temperature);
                if(roll < cumulative) return candidate;
            }
            return candidates.get(candidates.size() - 1);
        }

        protected String format(List<String> words){
            /** Formats the list of words into a sentence */
            if(words.isEmpty()){
                return "";
            }
            String first = words.get(0);
            words.set(0, Character.toUpperCase(first.charAt(0)) + first.substring(1));
            return String.join(" ", words) + ".";
}}


