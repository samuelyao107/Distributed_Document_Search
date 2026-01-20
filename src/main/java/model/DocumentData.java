package model;

import java.util.HashMap;
import java.util.Map;

public class DocumentData {

    private Map<String, Double> termToFrequency = new HashMap<String, Double>();

    public void putTermFrequency(String term, Double frequency) {
        termToFrequency.put(term, frequency);
    }

    public Double getFrequency(String term) {
        return termToFrequency.get(term);
    }
}
