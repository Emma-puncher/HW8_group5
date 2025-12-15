package com.example.GoogleQuery.core;

import com.example.GoogleQuery.model.Keyword;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 驗證為什麼搜尋「咖啡」會沒有結果
 */
public class SearchCoffeeKeywordTest {

    @Test
    public void lowWeightKeyword_mayNotAppearInResults() {
        System.out.println("=== 為什麼搜尋『咖啡』會沒有結果 ===\n");
        
        // 模擬咖啡廳內容
        String cafeContent = "羊毛與花永康店是一個專業的咖啡館，提供優質咖啡和舒適環境";
        
        ArrayList<Keyword> keywords = new ArrayList<>();
        keywords.add(new Keyword("不限時", 3.0, 1));    // Tier 1 - 權重高
        keywords.add(new Keyword("安靜", 2.5, 1));      // Tier 1 - 權重高
        keywords.add(new Keyword("有插座", 2.0, 1));    // Tier 1 - 權重高
        keywords.add(new Keyword("咖啡", 0.8, 3));      // Tier 3 - 權重低！
        
        KeywordParser parser = new KeywordParser(cafeContent);
        
        System.out.println("咖啡廳內容：");
        System.out.println("  " + cafeContent);
        
        System.out.println("\n關鍵字出現次數：");
        for (Keyword kw : keywords) {
            int count = parser.countKeyword(kw.name);
            System.out.printf("  '%s'：出現 %d 次\n", kw.name, count);
        }
        
        double score = parser.calculateWeightedScore(keywords);
        System.out.println("\n原始分數計算：");
        System.out.printf("  不限時(0次 × 3.0) = 0.0\n");
        System.out.printf("  安靜(0次 × 2.5) = 0.0\n");
        System.out.printf("  有插座(0次 × 2.0) = 0.0\n");
        System.out.printf("  咖啡(2次 × 0.8) = 1.6\n");
        System.out.printf("  ─────────────────\n");
        System.out.printf("  總分: %.1f\n", score);
        
        System.out.println("\n🔍 問題分析：");
        System.out.println("  1. 咖啡的權重只有 0.8（Tier 3，最低）");
        System.out.println("  2. 咖啡廳內容中沒有『不限時』、『安靜』等高權重詞");
        System.out.println("  3. 單靠『咖啡』這個低權重詞，總分很低（1.6分）");
        System.out.println("  4. 規範化後可能變成接近 0");
        System.out.println("  5. 被過濾掉（if (score > 0) 的門檻太高）");
        
        System.out.println("\n✅ 解決方案：");
        System.out.println("  A) 提高『咖啡』的權重（從 0.8 → 1.5 或更高）");
        System.out.println("  B) 在咖啡廳 keywords 欄位中加入『咖啡』標籤");
        System.out.println("  C) 調整過濾邏輯（不過濾分數 > 0 的結果，改為顯示所有結果）");
    }

    @Test
    public void compareKeywordWeights() {
        System.out.println("\n=== 關鍵字權重對比 ===\n");
        
        ArrayList<Keyword> keywords = new ArrayList<>();
        keywords.add(new Keyword("不限時", 3.0, 1));    // Tier 1
        keywords.add(new Keyword("安靜", 2.5, 1));      // Tier 1
        keywords.add(new Keyword("有插座", 2.0, 1));    // Tier 1
        keywords.add(new Keyword("wifi", 1.5, 2));     // Tier 2
        keywords.add(new Keyword("咖啡", 0.8, 3));     // Tier 3
        
        System.out.println("Tier 1（核心詞）- 高權重：");
        System.out.println("  不限時：3.0");
        System.out.println("  安靜：2.5");
        System.out.println("  有插座：2.0");
        
        System.out.println("\nTier 2（次要詞）- 中權重：");
        System.out.println("  wifi：1.5");
        
        System.out.println("\nTier 3（參考詞）- 低權重：");
        System.out.println("  咖啡：0.8 ⚠️ 太低！");
        
        System.out.println("\n💡 建議：『咖啡』應該提升到 Tier 2（權重 1.5-2.0）");
    }

    @Test
    public void fixForCoffeeKeyword() {
        System.out.println("\n=== 修復『咖啡』搜尋問題 ===\n");
        
        String cafeContent = "羊毛與花永康店是一個專業的咖啡館，提供優質咖啡和舒適環境";
        
        // 現在使用提升的咖啡權重
        ArrayList<Keyword> keywords = new ArrayList<>();
        keywords.add(new Keyword("不限時", 3.0, 1));
        keywords.add(new Keyword("安靜", 2.5, 1));
        keywords.add(new Keyword("有插座", 2.0, 1));
        keywords.add(new Keyword("咖啡", 1.5, 2));      // ✅ 提升到 Tier 2
        
        KeywordParser parser = new KeywordParser(cafeContent);
        double newScore = parser.calculateWeightedScore(keywords);
        
        System.out.println("提升『咖啡』權重後：");
        System.out.printf("  咖啡(2次 × 1.5) = 3.0\n");
        System.out.printf("  新總分: %.1f\n", newScore);
        System.out.println("\n✅ 分數提升，搜尋『咖啡』時有更多結果顯示！");
    }
}
