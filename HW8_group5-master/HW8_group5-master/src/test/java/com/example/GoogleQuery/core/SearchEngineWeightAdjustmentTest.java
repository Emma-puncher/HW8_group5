package com.example.GoogleQuery.core;

import com.example.GoogleQuery.model.Keyword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 驗證 SearchEngine.adjustKeywordWeights(...) 的正確行為
 * 確認使用者輸入的關鍵字會提升權重，進而影響排名分數
 */
public class SearchEngineWeightAdjustmentTest {

    private SearchEngine searchEngine;
    private ArrayList<Keyword> keywords;

    @BeforeEach
    public void setup() {
        // 建立 SearchEngine 並初始化關鍵字
        searchEngine = new SearchEngine();
        
        keywords = new ArrayList<>();
        keywords.add(new Keyword("wifi", 1.5, 2));
        keywords.add(new Keyword("安靜", 2.5, 1));
        keywords.add(new Keyword("插座", 2.5, 1));
        keywords.add(new Keyword("咖啡", 0.8, 3));
        
        // 手動設定 keywords
        searchEngine.keywords = keywords;
    }

    @Test
    public void adjustKeywordWeights_withUserInput_boostsMatchingKeywords() {
        // 原始權重
        double originalWifiWeight = keywords.stream()
                .filter(k -> k.name.equals("wifi"))
                .findFirst()
                .orElse(new Keyword("", 0))
                .getWeight();
        
        // 調整權重（模擬使用者搜尋「wifi」）
        String userQuery = "wifi";
        searchEngine.adjustKeywordWeights(userQuery);
        
        // 檢查 wifi 的權重是否被提升（1.5 * 1.5 = 2.25）
        double adjustedWifiWeight = keywords.stream()
                .filter(k -> k.name.equals("wifi"))
                .findFirst()
                .orElse(new Keyword("", 0))
                .getWeight();
        
        assertEquals(originalWifiWeight * 1.5, adjustedWifiWeight, 1e-6);
        System.out.println("[Test] wifi 權重從 " + originalWifiWeight + " 提升至 " + adjustedWifiWeight);
    }

    @Test
    public void adjustKeywordWeights_multipleMatches_boostsAll() {
        double originalWifiWeight = 1.5;
        double originalQuietWeight = 2.5;
        
        // 調整權重（模擬使用者搜尋「wifi 安靜」）
        String userQuery = "wifi 安靜";
        searchEngine.adjustKeywordWeights(userQuery);
        
        double adjustedWifiWeight = keywords.stream()
                .filter(k -> k.name.equals("wifi"))
                .findFirst()
                .map(Keyword::getWeight)
                .orElse(0.0);
        
        double adjustedQuietWeight = keywords.stream()
                .filter(k -> k.name.equals("安靜"))
                .findFirst()
                .map(Keyword::getWeight)
                .orElse(0.0);
        
        // 兩個都應該被提升
        assertEquals(originalWifiWeight * 1.5, adjustedWifiWeight, 1e-6);
        assertEquals(originalQuietWeight * 1.5, adjustedQuietWeight, 1e-6);
        
        System.out.println("[Test] wifi 和安靜都被提升 50%");
    }

    @Test
    public void adjustKeywordWeights_nonMatching_unchangedWeight() {
        double originalCoffeeWeight = 0.8;
        
        // 調整權重（使用者搜尋「wifi」，不包含「咖啡」）
        String userQuery = "wifi";
        searchEngine.adjustKeywordWeights(userQuery);
        
        double adjustedCoffeeWeight = keywords.stream()
                .filter(k -> k.name.equals("咖啡"))
                .findFirst()
                .map(Keyword::getWeight)
                .orElse(0.0);
        
        // 咖啡的權重應保持原狀（透過 resetWeight() 回到 originalWeight）
        assertEquals(originalCoffeeWeight, adjustedCoffeeWeight, 1e-6);
        
        System.out.println("[Test] 未匹配的關鍵字「咖啡」權重保持不變");
    }

    @Test
    public void scoreChangesDueToWeightAdjustment_demonstratesImpact() {
        // 建立內容
        String content = "wifi wifi 安靜 咖啡";
        KeywordParser parser = new KeywordParser(content);
        
        // 計算原始分數
        double originalScore = parser.calculateWeightedScore(keywords);
        System.out.println("[Test] 原始分數: " + originalScore);
        
        // 調整權重
        searchEngine.adjustKeywordWeights("wifi");
        
        // 重新計算分數（現在 wifi 權重更高）
        // 清除快取以重新計算
        parser.clearCache();
        double adjustedScore = parser.calculateWeightedScore(keywords);
        System.out.println("[Test] 調整後分數: " + adjustedScore);
        
        // 分數應該增加（因為 wifi 被提升權重）
        assertTrue(adjustedScore > originalScore, 
                  "調整後分數應該高於原始分數");
        
        // 驗證分數增量符合預期
        // 原始: wifi(1.5*2) + 安靜(2.5*1) + 咖啡(0.8*1) = 3.0 + 2.5 + 0.8 = 6.3
        // 調整: wifi(2.25*2) + 安靜(2.5*1) + 咖啡(0.8*1) = 4.5 + 2.5 + 0.8 = 7.8
        double expectedIncrease = 1.5; // (2.25 - 1.5) * 2 = 1.5
        assertEquals(expectedIncrease, adjustedScore - originalScore, 1e-6);
    }
}
