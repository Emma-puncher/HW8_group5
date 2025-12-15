package com.example.GoogleQuery.core;

import com.example.GoogleQuery.model.Keyword;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class WeightCalculationTest {

    @Test
    public void calculateWeightedScore_matchesExpectation() {
        String content = "wifi wifi 咖啡";
        KeywordParser parser = new KeywordParser(content);

        ArrayList<Keyword> keywords = new ArrayList<>();
        keywords.add(new Keyword("wifi", 1.5, 2));
        keywords.add(new Keyword("咖啡", 0.8, 3));

        double score = parser.calculateWeightedScore(keywords);
        assertEquals(3.8, score, 1e-6);
    }

    @Test
    public void changingWeight_changesScore() {
        String content = "wifi wifi 咖啡";
        KeywordParser parser = new KeywordParser(content);

        ArrayList<Keyword> keywords = new ArrayList<>();
        Keyword wifi = new Keyword("wifi", 1.5, 2);
        Keyword coffee = new Keyword("咖啡", 0.8, 3);
        keywords.add(wifi);
        keywords.add(coffee);

        double base = parser.calculateWeightedScore(keywords);

        // 更改權重後，分數應增加
        wifi.setWeight(3.0);
        double changed = parser.calculateWeightedScore(keywords);

        assertTrue(changed > base);
        assertEquals(6.8, changed, 1e-6);
    }
}
