package com.example.GoogleQuery.core;

import com.example.GoogleQuery.model.Keyword;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 展示如何禁用動態權重調整
 * 驗證禁用後使用固定權重進行排名
 */
public class DisableDynamicWeightTest {

    @Test
    public void whenDisableDynamicWeight_weightsShouldBeUnchanged() {
        SearchEngine searchEngine = new SearchEngine();
        
        ArrayList<Keyword> keywords = new ArrayList<>();
        keywords.add(new Keyword("wifi", 1.5, 2));
        keywords.add(new Keyword("安靜", 2.5, 1));
        keywords.add(new Keyword("咖啡", 0.8, 3));
        
        searchEngine.keywords = keywords;
        
        // 獲取原始權重
        double originalWifiWeight = keywords.stream()
                .filter(k -> k.name.equals("wifi"))
                .findFirst()
                .map(Keyword::getWeight)
                .orElse(0.0);
        
        // 禁用動態權重調整
        searchEngine.setEnableDynamicWeightAdjustment(false);
        
        System.out.println("\n=== 禁用動態權重調整 ===");
        System.out.println("原始 wifi 權重: " + originalWifiWeight);
        
        // 調用 adjustKeywordWeights（應該沒有效果，因為已禁用）
        searchEngine.adjustKeywordWeights("wifi");
        
        // 檢查權重是否保持不變
        double currentWifiWeight = keywords.stream()
                .filter(k -> k.name.equals("wifi"))
                .findFirst()
                .map(Keyword::getWeight)
                .orElse(0.0);
        
        System.out.println("調整後 wifi 權重: " + currentWifiWeight);
        assertEquals(originalWifiWeight, currentWifiWeight, 
                    "禁用動態調整時，權重應保持不變");
        
        System.out.println("✅ 權重保持不變");
    }

    @Test
    public void compareEnabledVsDisabled_scoresDiffer() {
        String content = "wifi wifi 安靜";
        
        // === 啟用動態權重（預設） ===
        SearchEngine enabledEngine = new SearchEngine();
        ArrayList<Keyword> enabledKeywords = new ArrayList<>();
        enabledKeywords.add(new Keyword("wifi", 1.5, 2));
        enabledKeywords.add(new Keyword("安靜", 2.5, 1));
        enabledEngine.keywords = enabledKeywords;
        
        KeywordParser enabledParser = new KeywordParser(content);
        double baseScore = enabledParser.calculateWeightedScore(enabledKeywords);
        System.out.println("\n=== 啟用動態權重 ===");
        System.out.println("原始分數（未調整）: " + baseScore);
        
        enabledEngine.adjustKeywordWeights("wifi");
        double enabledScore = enabledParser.calculateWeightedScore(enabledKeywords);
        System.out.println("搜尋 'wifi' 後分數: " + enabledScore);
        
        // === 禁用動態權重 ===
        SearchEngine disabledEngine = new SearchEngine();
        ArrayList<Keyword> disabledKeywords = new ArrayList<>();
        disabledKeywords.add(new Keyword("wifi", 1.5, 2));
        disabledKeywords.add(new Keyword("安靜", 2.5, 1));
        disabledEngine.keywords = disabledKeywords;
        disabledEngine.setEnableDynamicWeightAdjustment(false);
        
        KeywordParser disabledParser = new KeywordParser(content);
        System.out.println("\n=== 禁用動態權重 ===");
        System.out.println("原始分數: " + baseScore);
        
        disabledEngine.adjustKeywordWeights("wifi");
        double disabledScore = disabledParser.calculateWeightedScore(disabledKeywords);
        System.out.println("搜尋 'wifi' 後分數: " + disabledScore);
        
        // 啟用時分數應該更高（因為 wifi 權重被提升）
        System.out.println("\n=== 對比 ===");
        System.out.println("啟用動態權重的分數: " + enabledScore);
        System.out.println("禁用動態權重的分數: " + disabledScore);
        assertTrue(enabledScore > disabledScore, 
                  "啟用動態權重應該導致分數更高");
    }

    @Test
    public void defaultStateIsEnabled() {
        SearchEngine engine = new SearchEngine();
        assertTrue(engine.isDynamicWeightAdjustmentEnabled(),
                  "動態權重調整預設應該是啟用狀態");
        System.out.println("\n✅ 動態權重調整預設為啟用");
    }

    @Test
    public void canToggleMultipleTimes() {
        SearchEngine engine = new SearchEngine();
        
        System.out.println("\n=== 多次切換狀態 ===");
        
        // 第一次禁用
        engine.setEnableDynamicWeightAdjustment(false);
        assertFalse(engine.isDynamicWeightAdjustmentEnabled());
        
        // 再次啟用
        engine.setEnableDynamicWeightAdjustment(true);
        assertTrue(engine.isDynamicWeightAdjustmentEnabled());
        
        // 再次禁用
        engine.setEnableDynamicWeightAdjustment(false);
        assertFalse(engine.isDynamicWeightAdjustmentEnabled());
        
        System.out.println("✅ 可以多次切換狀態");
    }
}
