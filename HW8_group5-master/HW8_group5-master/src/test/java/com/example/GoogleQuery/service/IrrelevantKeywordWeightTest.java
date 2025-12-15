package com.example.GoogleQuery.service;

import com.example.GoogleQuery.model.Keyword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 驗證不相關的字詞權重很低（或為 0）
 */
public class IrrelevantKeywordWeightTest {

    private KeywordService keywordService;

    @BeforeEach
    public void setup() {
        // 使用預設關鍵字初始化
        keywordService = new KeywordService(new ArrayList<>());
        keywordService.init();
    }

    @Test
    public void irrelevantKeyword_hasZeroWeight() {
        // 不相關的字詞
        String irrelevantWord = "這是一個不存在的詞彙";
        
        double weight = keywordService.getKeywordWeight(irrelevantWord);
        
        System.out.println("不相關字詞：'" + irrelevantWord + "'");
        System.out.println("權重：" + weight);
        
        assertEquals(0.0, weight, 
                    "不相關字詞應該返回 0.0 權重");
    }

    @Test
    public void relatedKeyword_hasNonZeroWeight() {
        // 相關的字詞（來自 keywords.json）
        String relatedWord = "不限時";  // 核心關鍵字，權重應該是 3.0
        
        double weight = keywordService.getKeywordWeight(relatedWord);
        
        System.out.println("\n相關字詞：'" + relatedWord + "'");
        System.out.println("權重：" + weight);
        
        assertTrue(weight > 0.0, 
                  "相關字詞應該有非零權重");
    }

    @Test
    public void compareWeights_relevantVsIrrelevant() {
        String relevant = "不限時";     // 應該有高權重（3.0）
        String irrelevant = "xyz123";   // 應該是 0.0
        
        double relevantWeight = keywordService.getKeywordWeight(relevant);
        double irrelevantWeight = keywordService.getKeywordWeight(irrelevant);
        
        System.out.println("\n=== 相關 vs 不相關 ===");
        System.out.println("相關字詞 '" + relevant + "'：" + relevantWeight);
        System.out.println("不相關字詞 '" + irrelevant + "'：" + irrelevantWeight);
        
        assertTrue(relevantWeight > irrelevantWeight, 
                  "相關字詞權重應高於不相關字詞");
        assertEquals(0.0, irrelevantWeight,
                    "不相關字詞應為 0.0");
    }

    @Test
    public void searchWithIrrelevantWords_doesNotBoostScore() {
        String content = "不限時 不限時 安靜";
        
        ArrayList<Keyword> keywords = new ArrayList<>();
        keywords.add(new Keyword("不限時", 3.0, 1));
        keywords.add(new Keyword("安靜", 2.5, 1));
        keywords.add(new Keyword("xyz不存在", 0.0, 3));  // 不存在的詞
        
        com.example.GoogleQuery.core.KeywordParser parser = 
            new com.example.GoogleQuery.core.KeywordParser(content);
        
        double score = parser.calculateWeightedScore(keywords);
        
        // 預期分數：不限時(3.0*2) + 安靜(2.5*1) + xyz不存在(0*0) = 8.5
        System.out.println("\n=== 包含不相關詞的分數計算 ===");
        System.out.println("內容：" + content);
        System.out.println("計算分數：");
        System.out.println("  不限時(出現2次, 權重3.0) = 6.0");
        System.out.println("  安靜(出現1次, 權重2.5) = 2.5");
        System.out.println("  xyz不存在(出現0次, 權重0.0) = 0.0");
        System.out.println("總分：" + score);
        
        assertEquals(8.5, score, 1e-6,
                    "不相關詞不應對分數有任何貢獻");
    }

    @Test
    public void allKeywords_areStoredWithPositiveWeight() {
        // 驗證所有預載入的關鍵字都有正權重
        var allKeywords = keywordService.getAllKeywords();
        
        System.out.println("\n=== 所有預載入的關鍵字 ===");
        System.out.println("總數：" + allKeywords.size());
        
        for (Keyword kw : allKeywords) {
            System.out.printf("  %s：%.2f\n", kw.getName(), kw.getWeight());
            assertTrue(kw.getWeight() > 0.0, 
                      "所有預載入的關鍵字應該有正權重");
        }
    }
}
