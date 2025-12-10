package com.example.GoogleQuery.config;

import com.example.GoogleQuery.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DataLoaderConfig - 資料載入配置
 * 在應用程式啟動時載入 JSON 資料（咖啡廳、關鍵字等）
 */
@Configuration
public class DataLoaderConfig {
    
    private final Gson gson = new Gson();
    
    /**
     * 載入咖啡廳資料
     * @return 咖啡廳列表
     */
    @Bean(name = "cafeList")
    public ArrayList<Cafe> loadCafes() {
        try {
            System.out.println("正在載入咖啡廳資料...");
            
            ClassPathResource resource = new ClassPathResource("data/cafes.json");
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            
            // 定義 JSON 結構類型
            Type cafeListType = new TypeToken<CafeDataWrapper>(){}.getType();
            CafeDataWrapper wrapper = gson.fromJson(reader, cafeListType);
            
            reader.close();
            
            if (wrapper != null && wrapper.cafes != null) {
                System.out.println("成功載入 " + wrapper.cafes.size() + " 家咖啡廳");
                return new ArrayList<>(wrapper.cafes);
            } else {
                System.out.println("警告：咖啡廳資料為空，使用預設資料");
                return createDefaultCafes();
            }
            
        } catch (IOException e) {
            System.err.println("無法載入咖啡廳資料: " + e.getMessage());
            System.out.println("使用預設咖啡廳資料");
            return createDefaultCafes();
        }
    }
    
    /**
     * 載入關鍵字資料
     * @return 關鍵字列表
     */
    @Bean(name = "keywordList")
    public ArrayList<Keyword> loadKeywords() {
        try {
            System.out.println("正在載入關鍵字資料...");
            
            ClassPathResource resource = new ClassPathResource("data/keywords.json");
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            
            Type keywordListType = new TypeToken<KeywordDataWrapper>(){}.getType();
            KeywordDataWrapper wrapper = gson.fromJson(reader, keywordListType);
            
            reader.close();
            
            ArrayList<Keyword> allKeywords = new ArrayList<>();
            
            if (wrapper != null) {
                // 合併所有分級的關鍵字
                if (wrapper.core_keywords != null) {
                    allKeywords.addAll(convertToKeywords(wrapper.core_keywords, KeywordTier.CORE));
                }
                if (wrapper.secondary_keywords != null) {
                    allKeywords.addAll(convertToKeywords(wrapper.secondary_keywords, KeywordTier.SECONDARY));
                }
                if (wrapper.reference_keywords != null) {
                    allKeywords.addAll(convertToKeywords(wrapper.reference_keywords, KeywordTier.REFERENCE));
                }
                
                System.out.println("成功載入 " + allKeywords.size() + " 個關鍵字");
                return allKeywords;
            } else {
                System.out.println("警告：關鍵字資料為空，使用預設資料");
                return createDefaultKeywords();
            }
            
        } catch (IOException e) {
            System.err.println("無法載入關鍵字資料: " + e.getMessage());
            System.out.println("使用預設關鍵字資料");
            return createDefaultKeywords();
        }
    }
    
    /**
     * 載入基準分數資料
     * @return 基準分數 Map
     */
    @Bean(name = "baselineScores")
    public Map<String, Double> loadBaselineScores() {
        try {
            System.out.println("正在載入基準分數...");
            
            ClassPathResource resource = new ClassPathResource("data/baseline-scores.json");
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            
            Type mapType = new TypeToken<Map<String, Double>>(){}.getType();
            Map<String, Double> scores = gson.fromJson(reader, mapType);
            
            reader.close();
            
            if (scores != null) {
                System.out.println("成功載入 " + scores.size() + " 個基準分數");
                return scores;
            } else {
                System.out.println("警告：基準分數資料為空");
                return new java.util.HashMap<>();
            }
            
        } catch (IOException e) {
            System.err.println("無法載入基準分數: " + e.getMessage());
            return new java.util.HashMap<>();
        }
    }
    
    /**
     * 轉換 JSON 資料為 Keyword 物件
     */
    private List<Keyword> convertToKeywords(List<Map<String, Object>> data, KeywordTier tier) {
        List<Keyword> keywords = new ArrayList<>();
        
        for (Map<String, Object> item : data) {
            String name = (String) item.get("name");
            double weight = ((Number) item.get("weight")).doubleValue();
            
            Keyword keyword = new Keyword(name, weight);
            keyword.setTier(tier);
            keywords.add(keyword);
        }
        
        return keywords;
    }
    
    /**
     * 建立預設咖啡廳資料（當 JSON 檔案不存在時使用）
     */
    private ArrayList<Cafe> createDefaultCafes() {
        ArrayList<Cafe> cafes = new ArrayList<>();
        
        // 範例咖啡廳 1
        Cafe cafe1 = new Cafe(
            "cafe_001",
            "讀字書店咖啡廳",
            "https://example.com/cafe1",
            "大安區",
            "台北市大安區羅斯福路三段269巷16號"
        );
        cafe1.setNoTimeLimit(true);
        cafe1.setHasSocket(true);
        cafe1.setHasWifi(true);
        cafe1.setQuiet(true);
        cafe1.setDescription("結合書店與咖啡廳，提供舒適的閱讀空間");
        cafe1.setRating(4.5);
        cafe1.setReviewCount(120);
        cafes.add(cafe1);
        
        // 範例咖啡廳 2
        Cafe cafe2 = new Cafe(
            "cafe_002",
            "工業風咖啡",
            "https://example.com/cafe2",
            "中山區",
            "台北市中山區南京東路二段123號"
        );
        cafe2.setHasSocket(true);
        cafe2.setHasWifi(true);
        cafe2.setDescription("工業風格裝潢，適合工作");
        cafe2.setRating(4.2);
        cafe2.setReviewCount(85);
        cafes.add(cafe2);
        
        return cafes;
    }
    
    /**
     * 建立預設關鍵字資料
     */
    private ArrayList<Keyword> createDefaultKeywords() {
        ArrayList<Keyword> keywords = new ArrayList<>();
        
        // 核心詞
        keywords.add(new Keyword("不限時", 3.0));
        keywords.add(new Keyword("安靜", 2.8));
        keywords.add(new Keyword("插座", 2.7));
        keywords.add(new Keyword("wifi", 2.6));
        keywords.add(new Keyword("適合讀書", 2.9));
        
        // 次要詞
        keywords.add(new Keyword("咖啡", 1.8));
        keywords.add(new Keyword("舒適", 1.6));
        keywords.add(new Keyword("文青", 1.5));
        
        // 參考詞
        keywords.add(new Keyword("甜點", 0.8));
        keywords.add(new Keyword("早午餐", 0.7));
        
        return keywords;
    }
    
    // ========== 內部類別（用於 JSON 解析） ==========
    
    /**
     * 咖啡廳資料包裝類別
     */
    private static class CafeDataWrapper {
        List<Cafe> cafes;
    }
    
    /**
     * 關鍵字資料包裝類別
     */
    private static class KeywordDataWrapper {
        List<Map<String, Object>> core_keywords;
        List<Map<String, Object>> secondary_keywords;
        List<Map<String, Object>> reference_keywords;
    }
}

