package search;

import model.DocumentData;

import java.util.*;

public class TFIDF {

    public static double calculateTermFrequency(List<String> words, String term){
        long count = 0;
        for(String word : words){
            if(term.equalsIgnoreCase(word)){
                count++;
            }
        }
        return (double) count/words.size();
    }

    public static DocumentData createDocumentData(List<String> words, List<String> terms){
        DocumentData documentData = new DocumentData();
        for(String term : terms){
            documentData.putTermFrequency(term, calculateTermFrequency(words,term));
        }
        return documentData;
    }

    private static double getInverseDocumentFrequency(String term, Map<String, DocumentData> documentData){
        double num_term = 0;
        for(String word : documentData.keySet()){
            DocumentData docData = documentData.get(word);
            double frequency = docData.getFrequency(term);
            if(frequency > 0){
                num_term ++;
            }
        }
        return  num_term == 0 ? 0.0 : Math.log(documentData.size())/num_term;
    }

    private static Map<String, Double> getIDFMap(List<String> terms, Map<String, DocumentData> documentData){

        Map<String, Double> termToidf = new HashMap<>();
        for(String term : terms){
            double idf = getInverseDocumentFrequency(term, documentData);
            termToidf.put(term, idf);
        }
        return termToidf;
    }

    private static double calculateDocumentScore(List<String> terms, DocumentData documentData,
                                                 Map<String, Double> termToidf){
        double score = 0;
        for(String term : terms){
            score += documentData.getFrequency(term) *  termToidf.get(term);
        }
        return score;
    }

    private static void addDocumentScoreToTreeMap(TreeMap<Double, List<String>> scoreToDoc, double score, String document){
        List<String> documentsWithCurrentScore = scoreToDoc.get(score);
        if(documentsWithCurrentScore == null){
            documentsWithCurrentScore = new ArrayList<>();
        }
        documentsWithCurrentScore.add(document);
        scoreToDoc.put(score, documentsWithCurrentScore);
    }

    public static Map<Double, List<String>> getDocumentSortedByScore(List<String> terms,
                                                                     Map<String, DocumentData> documentData){

        TreeMap<Double, List<String>> scoreToDocument = new TreeMap<>(); //To make sure our map is sorted as we go
        Map<String, Double> termToidf = getIDFMap(terms, documentData);

        for(String document : documentData.keySet()){
            DocumentData docData = documentData.get(document);
            double score = calculateDocumentScore(terms, docData,termToidf);
            addDocumentScoreToTreeMap(scoreToDocument, score, document);
        }

        return scoreToDocument;
    }

    public static List<String> getWordsFromLine(String line){
        return Arrays.asList(line.split(" (\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
    }

    public static List<String> getWordsFromLine(List<String> lines){
        List<String> words = new ArrayList<>();
        for(String line : lines){
            words.addAll(getWordsFromLine(line));
        }
        return words;
    }
}
