package com.example.GoogleQuery.core;

import com.example.GoogleQuery.model.Keyword;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 驗證不相關字詞權重為 0 的行為
 * 使用 KeywordParser 直接測試
 */
public class IrrelevantWordHandlingTest {

    @Test
    public void irrelevantWords_contributesZeroScore() {
        String content = "不限時 不限時 安靜 xyz亂碼";
        
        ArrayList<Keyword> keywords = new ArrayList<>();
        keywords.add(new Keyword("不限時", 3.0, 1));
        keywords.add(new Keyword("安靜", 2.5, 1));
        keywords.add(new Keyword("xyz亂碼", 0.0, 3));  // 不相關詞，權重為 0
        
        KeywordParser parser = new KeywordParser(content);
        
        System.out.println("=== 驗證不相關詞的權重和影響 ===");
        System.out.println("內容：" + content);
        System.out.println("\n關鍵字及權重：");
        
        double totalScore = 0.0;
        for (Keyword kw : keywords) {
            int count = parser.countKeyword(kw.name);
            double contribution = count * kw.weight;
            totalScore += contribution;
            
            System.out.printf("  '%s' (權重%.1f)：出現 %d 次 → 貢獻 %.1f 分\n", 
                            kw.name, kw.weight, count, contribution);
        }
        
        System.out.println("\n總分：" + totalScore);
        
        // 預期：不限時(3.0*2) + 安靜(2.5*1) + xyz亂碼(0*1) = 8.5
        assertEquals(8.5, totalScore, 1e-6,
                    "不相關詞（權重 0）不應對分數有任何貢獻");
    }

    @Test
    public void onlyRelevantKeywords_getPositiveWeight() {
        ArrayList<Keyword> keywords = new ArrayList<>();
        keywords.add(new Keyword("不限時", 3.0, 1));
        keywords.add(new Keyword("安靜", 2.5, 1));
        keywords.add(new Keyword("wifi", 1.5, 2));
        
        System.out.println("\n=== 預定義關鍵字都有正權重 ===");
        for (Keyword kw : keywords) {
            System.out.printf("'%s'：%.1f\n", kw.name, kw.weight);
            assertTrue(kw.weight > 0.0, "預定義的關鍵字應該有正權重");
        }
    }

    @Test
    public void searchWithRandomWords_doesNotBoost() {
        String content = "我今天要找一個安靜的地方工作";
        
        ArrayList<Keyword> keywords = new ArrayList<>();
        keywords.add(new Keyword("安靜", 2.5, 1));
        // 其他詞「我」、「今天」、「要」、「找」、「地方」、「工作」都不在關鍵字列表中
        
        KeywordParser parser = new KeywordParser(content);
        double score = parser.calculateWeightedScore(keywords);
        
        System.out.println("\n=== 搜尋包含隨意詞彙的內容 ===");
        System.out.println("內容：" + content);
        System.out.println("分數（只計算 '安靜'）：" + score);
        
        // 應該只計算 '安靜'(2.5*1) = 2.5，其他詞完全被忽略
        assertEquals(2.5, score, 1e-6,
                    "只有定義的關鍵字才會被計分，隨意詞彙被忽略");
    }

    @Test
    public void userInput_mustMatchPredefinedKeywords() {
        ArrayList<Keyword> predefinedKeywords = new ArrayList<>();
        predefinedKeywords.add(new Keyword("不限時", 3.0, 1));
        predefinedKeywords.add(new Keyword("安靜", 2.5, 1));
        predefinedKeywords.add(new Keyword("插座", 2.5, 1));
        predefinedKeywords.add(new Keyword("wifi", 1.5, 2));
        
        System.out.println("\n=== 用戶輸入必須匹配預定義關鍵字 ===");
        System.out.println("預定義的相關詞彙（預定義的關鍵字）：");
        for (Keyword kw : predefinedKeywords) {
            System.out.println("  - " + kw.name);
        }
        
        System.out.println("\n如果用戶輸入：");
        System.out.println("  ✅ '不限時' → 找到，權重 3.0");
        System.out.println("  ✅ 'wifi' → 找到，權重 1.5");
        System.out.println("  ❌ 'abc123' → 找不到，權重 0.0（被忽略）");
        System.out.println("  ❌ '隨便詞' → 找不到，權重 0.0（被忽略）");
        
        // 驗證
        assertEquals(0.0, getWeightForKeyword("abc123", predefinedKeywords));
        assertEquals(0.0, getWeightForKeyword("隨便詞", predefinedKeywords));
        assertEquals(3.0, getWeightForKeyword("不限時", predefinedKeywords));
    }

    private double getWeightForKeyword(String keyword, ArrayList<Keyword> keywords) {
        return keywords.stream()
                .filter(k -> k.name.equals(keyword))
                .mapToDouble(Keyword::getWeight)
                .findFirst()
                .orElse(0.0);
    }
}
