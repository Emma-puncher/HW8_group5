package com.example.GoogleQuery.service;

import com.example.GoogleQuery.model.Keyword;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * KeywordService - 關鍵字處理服務
 * 管理咖啡廳領域的分級關鍵字及其權重
 */
@Service
public class KeywordService {

    // 三層級關鍵字
    private Map<String, Double> tier1Keywords; // 核心詞 (2.0-3.0)
    private Map<String, Double> tier2Keywords; // 次要詞 (1.0-1.9)
    private Map<String, Double> tier3Keywords; // 參考詞 (0.5-1.0)
    
    // 所有關鍵字的統一映射
    private Map<String, Keyword> allKeywordsMap;
    
    // 多語言關鍵字對照
    private Map<String, String> keywordTranslations;
    
    // 停用詞列表
    private Set<String> stopWords;

    /**
     * 初始化：載入關鍵字資料
     */
    @PostConstruct
    public void init() {
        try {
            loadKeywordsData();
            loadTranslations();
            loadStopWords();
            
            System.out.println("KeywordService 初始化完成：");
            System.out.println("  - Tier 1 關鍵字: " + tier1Keywords.size());
            System.out.println("  - Tier 2 關鍵字: " + tier2Keywords.size());
            System.out.println("  - Tier 3 關鍵字: " + tier3Keywords.size());
            System.out.println("  - 停用詞: " + stopWords.size());
            
        } catch (Exception e) {
            System.err.println("KeywordService 初始化失敗: " + e.getMessage());
            initializeDefaultKeywords();
        }
    }

    /**
     * 從 JSON 載入關鍵字資料
     */
    private void loadKeywordsData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("data/keywords.json").getInputStream();
        
        Map<String, Object> data = mapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});
        
        // 載入 Tier 1 關鍵字
        tier1Keywords = new HashMap<>();
        Map<String, Object> tier1Data = (Map<String, Object>) data.get("tier1");
        if (tier1Data != null) {
            List<String> keywords = (List<String>) tier1Data.get("keywords");
            Double weight = ((Number) tier1Data.get("weight")).doubleValue();
            for (String keyword : keywords) {
                tier1Keywords.put(keyword, weight);
            }
        }
        
        // 載入 Tier 2 關鍵字
        tier2Keywords = new HashMap<>();
        Map<String, Object> tier2Data = (Map<String, Object>) data.get("tier2");
        if (tier2Data != null) {
            List<String> keywords = (List<String>) tier2Data.get("keywords");
            Double weight = ((Number) tier2Data.get("weight")).doubleValue();
            for (String keyword : keywords) {
                tier2Keywords.put(keyword, weight);
            }
        }
        
        // 載入 Tier 3 關鍵字
        tier3Keywords = new HashMap<>();
        Map<String, Object> tier3Data = (Map<String, Object>) data.get("tier3");
        if (tier3Data != null) {
            List<String> keywords = (List<String>) tier3Data.get("keywords");
            Double weight = ((Number) tier3Data.get("weight")).doubleValue();
            for (String keyword : keywords) {
                tier3Keywords.put(keyword, weight);
            }
        }
        
        // 建立統一映射
        buildAllKeywordsMap();
    }

    /**
     * 建立所有關鍵字的統一映射
     */
    private void buildAllKeywordsMap() {
        allKeywordsMap = new HashMap<>();
        
        tier1Keywords.forEach((keyword, weight) -> 
            allKeywordsMap.put(keyword, new Keyword(keyword, weight, 1))
        );
        
        tier2Keywords.forEach((keyword, weight) -> 
            allKeywordsMap.put(keyword, new Keyword(keyword, weight, 2))
        );
        
        tier3Keywords.forEach((keyword, weight) -> 
            allKeywordsMap.put(keyword, new Keyword(keyword, weight, 3))
        );
    }

    /**
     * 載入多語言關鍵字對照
     */
    private void loadTranslations() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("data/keyword-translations.json").getInputStream();
            
            keywordTranslations = mapper.readValue(inputStream, new TypeReference<Map<String, String>>() {});
            
        } catch (IOException e) {
            System.out.println("未找到翻譯檔案，使用預設設定");
            keywordTranslations = new HashMap<>();
        }
    }

    /**
     * 載入停用詞
     */
    private void loadStopWords() {
        stopWords = new HashSet<>();
        
        try {
            InputStream inputStream = new ClassPathResource("config/stopwords.txt").getInputStream();
            Scanner scanner = new Scanner(inputStream, "UTF-8");
            
            while (scanner.hasNextLine()) {
                String word = scanner.nextLine().trim();
                if (!word.isEmpty() && !word.startsWith("#")) {
                    stopWords.add(word);
                }
            }
            
            scanner.close();
            
        } catch (IOException e) {
            System.out.println("未找到停用詞檔案，使用預設停用詞");
            initializeDefaultStopWords();
        }
    }

    /**
     * 初始化預設關鍵字（當無法載入 JSON 時使用）
     */
    private void initializeDefaultKeywords() {
        tier1Keywords = new HashMap<>();
        tier1Keywords.put("讀書", 2.5);
        tier1Keywords.put("工作", 2.5);
        tier1Keywords.put("安靜", 2.5);
        tier1Keywords.put("插座", 2.5);
        tier1Keywords.put("不限時", 3.0);
        
        tier2Keywords = new HashMap<>();
        tier2Keywords.put("wifi", 1.5);
        tier2Keywords.put("舒適", 1.5);
        tier2Keywords.put("寬敞", 1.5);
        tier2Keywords.put("明亮", 1.5);
        
        tier3Keywords = new HashMap<>();
        tier3Keywords.put("咖啡", 0.8);
        tier3Keywords.put("座位", 0.8);
        tier3Keywords.put("環境", 0.8);
        
        buildAllKeywordsMap();
    }

    /**
     * 初始化預設停用詞
     */
    private void initializeDefaultStopWords() {
        stopWords = new HashSet<>(Arrays.asList(
            "的", "了", "是", "在", "我", "有", "和", "就", "不", "人",
            "都", "一", "一個", "上", "也", "很", "到", "說", "要", "去"
        ));
    }

    /**
     * 獲取關鍵字權重
     * @param keyword 關鍵字
     * @return 權重值（如果不存在則返回 0.0）
     */
    public double getKeywordWeight(String keyword) {
        Keyword kw = allKeywordsMap.get(keyword);
        return kw != null ? kw.getWeight() : 0.0;
    }

    /**
     * 檢查是否為領域關鍵字
     * @param keyword 關鍵字
     * @return 是否為領域關鍵字
     */
    public boolean isDomainKeyword(String keyword) {
        return allKeywordsMap.containsKey(keyword);
    }

    /**
     * 獲取關鍵字的層級
     * @param keyword 關鍵字
     * @return 層級 (1, 2, 3) 或 0（不存在）
     */
    public int getKeywordTier(String keyword) {
        Keyword kw = allKeywordsMap.get(keyword);
        return kw != null ? kw.getTier() : 0;
    }

    /**
     * 檢查是否為停用詞
     * @param word 詞彙
     * @return 是否為停用詞
     */
    public boolean isStopWord(String word) {
        return stopWords.contains(word);
    }

    /**
     * 過濾停用詞
     * @param words 詞彙列表
     * @return 過濾後的詞彙列表
     */
    public List<String> filterStopWords(List<String> words) {
        return words.stream()
                .filter(word -> !isStopWord(word))
                .collect(Collectors.toList());
    }

    /**
     * 翻譯關鍵字（支援多語言）
     * @param keyword 關鍵字
     * @return 翻譯後的關鍵字（如果沒有翻譯則返回原詞）
     */
    public String translateKeyword(String keyword) {
        return keywordTranslations.getOrDefault(keyword, keyword);
    }

    /**
     * 提取使用者輸入中的領域關鍵字
     * @param userInput 使用者輸入
     * @return 領域關鍵字列表
     */
    public List<String> extractDomainKeywords(String userInput) {
        List<String> domainKeywords = new ArrayList<>();
        
        if (userInput == null || userInput.isEmpty()) {
            return domainKeywords;
        }
        
        String input = userInput.toLowerCase();
        
        // 檢查所有領域關鍵字是否出現在輸入中
        for (String keyword : allKeywordsMap.keySet()) {
            if (input.contains(keyword)) {
                domainKeywords.add(keyword);
            }
        }
        
        return domainKeywords;
    }

    /**
     * 計算關鍵字的加權分數
     * @param keywords 關鍵字列表
     * @param counts 每個關鍵字的出現次數
     * @return 總加權分數
     */
    public double calculateWeightedScore(List<String> keywords, Map<String, Integer> counts) {
        double totalScore = 0.0;
        
        for (String keyword : keywords) {
            double weight = getKeywordWeight(keyword);
            int count = counts.getOrDefault(keyword, 0);
            totalScore += weight * count;
        }
        
        return totalScore;
    }

    /**
     * 獲取所有關鍵字
     * @return 所有關鍵字列表
     */
    public List<String> getAllKeywords() {
        return new ArrayList<>(allKeywordsMap.keySet());
    }

    /**
     * 獲取指定層級的關鍵字
     * @param tier 層級 (1, 2, 3)
     * @return 該層級的關鍵字列表
     */
    public List<String> getKeywordsByTier(int tier) {
        return allKeywordsMap.values().stream()
                .filter(kw -> kw.getTier() == tier)
                .map(Keyword::getName)
                .collect(Collectors.toList());
    }

    /**
     * 獲取 Tier 1 關鍵字（核心詞）
     * @return Tier 1 關鍵字列表
     */
    public List<String> getTier1Keywords() {
        return new ArrayList<>(tier1Keywords.keySet());
    }

    /**
     * 獲取 Tier 2 關鍵字（次要詞）
     * @return Tier 2 關鍵字列表
     */
    public List<String> getTier2Keywords() {
        return new ArrayList<>(tier2Keywords.keySet());
    }

    /**
     * 獲取 Tier 3 關鍵字（參考詞）
     * @return Tier 3 關鍵字列表
     */
    public List<String> getTier3Keywords() {
        return new ArrayList<>(tier3Keywords.keySet());
    }

    /**
     * 獲取關鍵字詳細資訊
     * @param keyword 關鍵字
     * @return Keyword 物件
     */
    public Keyword getKeywordInfo(String keyword) {
        return allKeywordsMap.get(keyword);
    }

    /**
     * 依權重排序關鍵字
     * @param keywords 關鍵字列表
     * @return 排序後的關鍵字列表（權重由高到低）
     */
    public List<String> sortByWeight(List<String> keywords) {
        return keywords.stream()
                .sorted((a, b) -> Double.compare(getKeywordWeight(b), getKeywordWeight(a)))
                .collect(Collectors.toList());
    }

    /**
     * 依出現次數和權重排序關鍵字
     * @param keywordCounts 關鍵字及其出現次數
     * @return 排序後的關鍵字列表
     */
    public List<String> sortByWeightedCount(Map<String, Integer> keywordCounts) {
        return keywordCounts.entrySet().stream()
                .sorted((a, b) -> {
                    double scoreA = getKeywordWeight(a.getKey()) * a.getValue();
                    double scoreB = getKeywordWeight(b.getKey()) * b.getValue();
                    return Double.compare(scoreB, scoreA);
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 驗證關鍵字
     * @param keyword 關鍵字
     * @return 是否有效（不為空、不是停用詞、長度合理）
     */
    public boolean isValidKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = keyword.trim();
        
        // 檢查長度（1-20 字元）
        if (trimmed.length() < 1 || trimmed.length() > 20) {
            return false;
        }
        
        // 不能是停用詞
        if (isStopWord(trimmed)) {
            return false;
        }
        
        return true;
    }

    /**
     * 新增關鍵字（動態擴充）
     * @param keyword 關鍵字
     * @param weight 權重
     * @param tier 層級
     * @return 是否新增成功
     */
    public boolean addKeyword(String keyword, double weight, int tier) {
        if (!isValidKeyword(keyword)) {
            return false;
        }
        
        if (allKeywordsMap.containsKey(keyword)) {
            return false; // 已存在
        }
        
        Keyword kw = new Keyword(keyword, weight, tier);
        allKeywordsMap.put(keyword, kw);
        
        // 同時加入對應的層級 Map
        switch (tier) {
            case 1:
                tier1Keywords.put(keyword, weight);
                break;
            case 2:
                tier2Keywords.put(keyword, weight);
                break;
            case 3:
                tier3Keywords.put(keyword, weight);
                break;
        }
        
        return true;
    }

    /**
     * 更新關鍵字權重
     * @param keyword 關鍵字
     * @param newWeight 新權重
     * @return 是否更新成功
     */
    public boolean updateKeywordWeight(String keyword, double newWeight) {
        Keyword kw = allKeywordsMap.get(keyword);
        if (kw == null) {
            return false;
        }
        
        kw.setWeight(newWeight);
        
        // 同步更新層級 Map
        int tier = kw.getTier();
        switch (tier) {
            case 1:
                tier1Keywords.put(keyword, newWeight);
                break;
            case 2:
                tier2Keywords.put(keyword, newWeight);
                break;
            case 3:
                tier3Keywords.put(keyword, newWeight);
                break;
        }
        
        return true;
    }

    /**
     * 獲取關鍵字統計資訊
     * @return 統計資訊
     */
    public Map<String, Object> getKeywordStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalKeywords", allKeywordsMap.size());
        stats.put("tier1Count", tier1Keywords.size());
        stats.put("tier2Count", tier2Keywords.size());
        stats.put("tier3Count", tier3Keywords.size());
        stats.put("stopWordsCount", stopWords.size());
        stats.put("translationsCount", keywordTranslations.size());
        
        // 平均權重
        double avgWeight = allKeywordsMap.values().stream()
                .mapToDouble(Keyword::getWeight)
                .average()
                .orElse(0.0);
        stats.put("averageWeight", avgWeight);
        
        return stats;
    }

    /**
     * 檢查服務狀態
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "KeywordService");
        status.put("status", "running");
        status.put("keywordsLoaded", allKeywordsMap.size() > 0);
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
}


