import model.DocumentData;
import search.TFIDF;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class SequentialSearch {

    public static final String BOOKS_DIRECTORY = "/Users/Samuel/OneDrive/Desktop/Distributed_Document_Search/src/main/resources/books";
    public static final String SEARCH_QUERY_1 = "The blood is the life!";
    public static final String SEARCH_QUERY_2= "Some people without brains do an awful lot of talking, don't you think?";
    public static final String SEARCH_QUERY_3 = "Your strength is just an accident arising from the weakness of others.";

    /*public static  void main(String[] args) {
        File documentsDirectory = new File(BOOKS_DIRECTORY);

        List<String> booksFileNames = Arrays.asList(documentsDirectory.list())
                .stream()
                .map(documentName -> BOOKS_DIRECTORY + "/" + documentName)
                .collect(Collectors.toList());
        List<String> terms_1 = TFIDF.getWordsFromLine(SEARCH_QUERY_3);

        try{
            findMostRelevantDocuments(booksFileNames, terms_1);
        }catch(Exception e){
            e.printStackTrace();
        }

    } */

    private static void findMostRelevantDocuments(List<String> booksFileNames, List<String> terms)
            throws FileNotFoundException {
        Map<String, DocumentData> documentResults = new HashMap<>();

        for(String document :  booksFileNames) {
            BufferedReader reader = new BufferedReader(new FileReader(document));
            List<String> lines = reader.lines().collect(Collectors.toList());
            List<String> words = TFIDF.getWordsFromLine(lines);
            DocumentData documentData = TFIDF.createDocumentData(words, terms);
            documentResults.put(document, documentData);
        }

        Map<Double, List<String>>  mostRelevantDocuments = TFIDF.getDocumentSortedByScore(terms, documentResults);
        printResults(mostRelevantDocuments);
    }

    private static void printResults(Map<Double, List<String> > mostRelevantDocuments) {
        for(Map.Entry<Double, List<String>> entry : mostRelevantDocuments.entrySet()) {
            double score = entry.getKey();
            for (String word : entry.getValue()) {
                System.out.println(String.format("Book : %s - score: %f", word.split("/")[10], score));
            }
        }
    }
}
