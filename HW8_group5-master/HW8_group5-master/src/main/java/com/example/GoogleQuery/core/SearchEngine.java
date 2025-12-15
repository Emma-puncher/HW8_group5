package com.example.GoogleQuery.core;

import com.example.GoogleQuery.model.*;
import java.util.*;
import com.example.GoogleQuery.service.KeywordService;
import com.example.GoogleQuery.service.RankingService;

/**
 * SearchEngine - 咖啡廳搜尋引擎核心類別
 * 整合關鍵字解析、排名、Hashtag 生成等功能
 */
public class SearchEngine {
    
    private ArrayList<WebPage> allPages;              // 所有咖啡廳網站
    protected ArrayList<Keyword> keywords;             // 搜尋關鍵字清單（protected 供測試訪問）
    private Ranker ranker;                             // 排名系統
    private HashtagGenerator hashtagGenerator;         // Hashtag 生成器
    private BaselineScoreCalculator baselineCalculator; // 基準分數計算器
    private Map<String, WebTree> webTrees;            // 網站樹結構

    private KeywordService keywordService;         // 關鍵字服務
    private RankingService rankingService;         // 排名服務
    private boolean enableDynamicWeightAdjustment = true;  // 是否啟用動態權重調整（預設啟用）
    
    /**
     * 建構子
     */
    public SearchEngine() {
        this.allPages = new ArrayList<>();
        this.keywords = new ArrayList<>();
        this.webTrees = new HashMap<>();
        this.ranker = new Ranker(allPages);
        this.hashtagGenerator = new HashtagGenerator();
        this.baselineCalculator = new BaselineScoreCalculator();
    }
    
    /**
     * 建構子（指定咖啡廳列表和關鍵字）
     * @param pages 咖啡廳列表
     * @param keywords 關鍵字列表
     */
    public SearchEngine(ArrayList<WebPage> pages, ArrayList<Keyword> keywords) {
        this.allPages = pages != null ? pages : new ArrayList<>();
        this.keywords = keywords != null ? keywords : new ArrayList<>();
        this.webTrees = new HashMap<>();
        this.ranker = new Ranker(null);
        this.hashtagGenerator = new HashtagGenerator();
        this.baselineCalculator = new BaselineScoreCalculator();
    }

    /**
     * 建構子（整合 Service 層）(SearchService 會使用)
     * @param keywordService 關鍵字服務
     * @param rankingService 排名服務
     */
    public SearchEngine(KeywordService keywordService, RankingService rankingService) {
        this.allPages = new ArrayList<>();
        this.keywords = new ArrayList<>();
        this.webTrees = new HashMap<>();
        this.ranker = new Ranker(null);
        this.hashtagGenerator = new HashtagGenerator();
        this.baselineCalculator = new BaselineScoreCalculator();
        
        // 保存 Service 參考
        this.keywordService = keywordService;
        this.rankingService = rankingService;
        
        // 從 KeywordService 載入關鍵字
        if (keywordService != null) {
            loadKeywordsFromService();
        }
    }

    /**
     * 從 KeywordService 載入關鍵字
     */
    private void loadKeywordsFromService() {
        List<com.example.GoogleQuery.model.Keyword> allKeywords = 
            keywordService.getAllKeywords();
        
        for (com.example.GoogleQuery.model.Keyword kw : allKeywords) {
            // 假設你的 core.Keyword 和 model.Keyword 需要轉換
            // 或者直接使用同一個 Keyword 類別
            this.keywords.add(kw);
        }
    }

    /**
     * 設定 KeywordService（如果需要在建立後設定）
     */
    public void setKeywordService(KeywordService keywordService) {
        this.keywordService = keywordService;
        loadKeywordsFromService();
    }

    /**
     * 設定 RankingService（如果需要在建立後設定）
     */
    public void setRankingService(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /**
     * 設定是否啟用動態權重調整
     * @param enable true = 啟用（預設），false = 禁用（使用固定權重）
     */
    public void setEnableDynamicWeightAdjustment(boolean enable) {
        this.enableDynamicWeightAdjustment = enable;
        if (enable) {
            System.out.println("[SearchEngine] 已啟用動態權重調整");
        } else {
            System.out.println("[SearchEngine] 已禁用動態權重調整（使用固定權重）");
        }
    }

    /**
     * 取得是否啟用動態權重調整
     */
    public boolean isDynamicWeightAdjustmentEnabled() {
        return enableDynamicWeightAdjustment;
    }
    
    /**
     * 初始化搜尋引擎
     * 計算所有咖啡廳的基準分數（baseline score）
     */
    public void initialize() {
        System.out.println("=== 搜尋引擎初始化 ===");
        
        // 計算每個網站的基準分數
        baselineCalculator.calculateAllBaselineScores(allPages, keywords);
        
        System.out.println("已載入 " + allPages.size() + " 個咖啡廳");
        System.out.println("已設定 " + keywords.size() + " 個關鍵字");
        System.out.println("基準分數計算完成");
        System.out.println("初始化完成！\n");
    }
    
    /**
     * 執行基本搜尋
     * @param query 搜尋字串
     * @return 排序後的搜尋結果
     */
    public ArrayList<SearchResult> search(String query) {
        return search(query, null, null);
    }

    /**
     * 基本搜尋（不帶篩選條件）
     * @param query 搜尋字串
     * @param pages 要搜尋的網頁列表
     * @return 排序後的搜尋結果
     */
    public ArrayList<SearchResult> search(String query, ArrayList<WebPage> pages) {
        // 暫時設定 allPages 為傳入的 pages
        ArrayList<WebPage> originalPages = this.allPages;
        this.allPages = pages;
        
        // 呼叫進階搜尋，不帶篩選條件
        ArrayList<SearchResult> results = search(query, null, null);
        
        // 恢復原本的 allPages
        this.allPages = originalPages;
        
        return results;
    }
    
    /**
     * 執行進階搜尋（支援地區和功能篩選）
     * @param query 搜尋字串
     * @param districts 地區列表（null 表示不篩選）
     * @param features 功能列表（null 表示不篩選）
     * @return 排序後的搜尋結果
     */
    public ArrayList<SearchResult> search(String query, List<String> districts, List<String> features) {
        // 1. 動態調整關鍵字權重（根據使用者查詢，如果啟用的話）
        if (enableDynamicWeightAdjustment) {
            adjustKeywordWeights(query);
        }
        
        // 2. 篩選網站（根據地區和功能）
        ArrayList<WebPage> filteredPages = filterPages(districts, features);
        
        if (filteredPages.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 3. 建立 Ranker 並計算分數
        ranker = new Ranker(filteredPages);
        ranker.computeFinalScores(keywords);
        
        // 4. 標準化分數（0-100）
        ranker.normalizeScores();
        
        // 5. 生成 Hashtags（使用者輸入 + 網站 Top 3 關鍵字）
        // ❌ 註釋掉：不要動態生成 hashtags，保留咖啡廳原本的 hashtags
        // generateHashtagsForPages(filteredPages, query);
        
        // 6. 檢查查詢是否包含相關關鍵字
        // 如果查詢詞與任何關鍵字都無關，直接返回空結果
        if (!hasRelevantKeywords(query)) {
            return new ArrayList<>();
        }
        
        // 7. 取得排名結果
        ArrayList<SearchResult> results = ranker.getRankedResults();
        
        // 8. 過濾掉分數太低的結果（分數 > 0）
        ArrayList<SearchResult> filteredResults = new ArrayList<>();
        for (SearchResult result : results) {
            if (result.getScore() > 0) {
                filteredResults.add(result);
            }
        }
        
        return filteredResults;
    }

    
    /**
     * 檢查查詢字串是否相關（包含關鍵詞、店名、地址等）
     * 如果查詢既不包含任何關鍵詞，也不包含任何咖啡廳資訊，才視為無關
     * @param query 查詢字串
     * @return 是否為相關搜詞
     */
    private boolean hasRelevantKeywords(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        
        String lowerQuery = query.toLowerCase();
        
        // 1. 檢查是否包含 keywords.json 中的任何關鍵字
        for (Keyword keyword : keywords) {
            if (lowerQuery.contains(keyword.name.toLowerCase())) {
                return true;  // 找到相關關鍵字
            }
        }
        
        // 2. 檢查是否包含任何咖啡廳的名稱、地址、或地區
        // 這樣用戶搜尋地點、店名都能得到結果
        for (WebPage page : allPages) {
            // 檢查店名
            if (page.getName() != null && lowerQuery.contains(page.getName().toLowerCase())) {
                return true;
            }
            
            // 檢查地區
            if (page.getDistrict() != null && lowerQuery.contains(page.getDistrict().toLowerCase())) {
                return true;
            }
            
            // 檢查地址（至少包含一個關鍵詞，如"台北市"、"中山"等）
            if (page.getAddress() != null) {
                String address = page.getAddress().toLowerCase();
                // 檢查常見地點詞語
                if (lowerQuery.contains("台北") || 
                    address.contains("台北") && lowerQuery.contains("台北")) {
                    return true;
                }
            }
        }
        
        // 如果既沒有相關關鍵字，也沒有相關地點資訊，視為無關
        return false;
    }

    /**
     * 根據查詢字串動態調整關鍵字權重
     * 如果查詢包含某些關鍵字，提高其權重
     * @param query 查詢字串
     */
    protected void adjustKeywordWeights(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        
        String lowerQuery = query.toLowerCase();
        
        // 先記錄原始權重快照（方便 debug）
        Map<String, Double> before = new LinkedHashMap<>();
        for (Keyword keyword : keywords) {
            before.put(keyword.name, keyword.getWeight());
        }

        // 重置所有關鍵字權重（避免累積效應）
        for (Keyword keyword : keywords) {
            keyword.resetWeight();
        }

        // 如果查詢包含關鍵字，提高其權重（使用 boostWeight 以明確基於 originalWeight 調整）
        for (Keyword keyword : keywords) {
            if (lowerQuery.contains(keyword.name.toLowerCase())) {
                keyword.boostWeight(1.5);  // 提高 50% 權重
            }
        }

        // 記錄調整後權重並輸出差異（僅輸出有變化的項目）
        Map<String, Double> after = new LinkedHashMap<>();
        for (Keyword keyword : keywords) {
            after.put(keyword.name, keyword.getWeight());
        }

        System.out.println("[SearchEngine] adjustKeywordWeights - query: " + query);
        for (String name : before.keySet()) {
            double b = before.get(name);
            double a = after.getOrDefault(name, b);
            if (Double.compare(a, b) != 0) {
                System.out.printf("  %s: %.3f -> %.3f\n", name, b, a);
            }
        }
    }
    
    /**
     * 篩選網站（根據地區和功能）
     * @param districts 地區列表
     * @param features 功能列表
     * @return 篩選後的網站列表
     */
    private ArrayList<WebPage> filterPages(List<String> districts, List<String> features) {
        // 如果沒有篩選條件，返回所有網站
        if ((districts == null || districts.isEmpty()) && 
            (features == null || features.isEmpty())) {
            return new ArrayList<>(allPages);
        }
        
        ArrayList<WebPage> filtered = new ArrayList<>();
        
        for (WebPage page : allPages) {
            boolean matchDistrict = true;
            boolean matchFeature = true;
            
            // 檢查地區篩選
            if (districts != null && !districts.isEmpty()) {
                matchDistrict = districts.contains(page.getDistrict());
            }
            
            // 檢查功能篩選
            if (features != null && !features.isEmpty()) {
                matchFeature = pageHasFeatures(page, features);
            }

            /*  檢查功能篩選
            if (features != null && !features.isEmpty()) {
                matchFeature = false;
                for (String feature : features) {
                    if (page.getCategory().contains(feature)) {
                        matchFeature = true;
                        break;
                    }
                }
            }
            */

            // 必須同時符合地區和功能篩選
            if (matchDistrict && matchFeature) {
                filtered.add(page);
            }
        }
        
        return filtered;
    }

    /**
     * 檢查網頁是否具有指定的功能
     * @param page 網頁
     * @param requiredFeatures 需要的功能列表
     * @return 是否符合（需要全部符合）
     */
    private boolean pageHasFeatures(WebPage page, List<String> requiredFeatures) {
        if (requiredFeatures == null || requiredFeatures.isEmpty()) {
            return true;
        }
        
        // 取得頁面的功能列表
        List<String> pageFeatures = page.getFeatures();
        
        // 如果頁面沒有功能資訊，則不符合
        if (pageFeatures == null || pageFeatures.isEmpty()) {
            return false;
        }
        
        // 檢查是否包含所有需要的功能（AND 邏輯）
        for (String required : requiredFeatures) {
            if (!pageFeatures.contains(required)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 為所有網站生成 Hashtags
     * 結合使用者輸入的關鍵字（最多 2 個）+ 網站 Top 3 關鍵字
     * @param pages 網站列表
     * @param userQuery 使用者查詢
     */
    private void generateHashtagsForPages(ArrayList<WebPage> pages, String userQuery) {
        for (WebPage page : pages) {
            // 使用 HashtagGenerator 生成 Hashtags
            String hashtags = hashtagGenerator.generate(
                page.getContent(),     // 網站內容
                keywords,              // 所有關鍵字
                userQuery,             // 使用者查詢
                3,                     // 網站取前 3 個關鍵字
                2                      // 使用者查詢取前 2 個關鍵字
            );
            
            page.setHashtags(hashtags);
        }
    }
    
    /**
     * 取得熱門推薦咖啡廳（根據 baseline score）
     * @param limit 返回數量
     * @return 熱門咖啡廳列表
     */
    public ArrayList<SearchResult> getRecommendations(int limit) {
        // 根據基準分數排序
        ArrayList<WebPage> sortedPages = new ArrayList<>(allPages);
        sortedPages.sort((p1, p2) -> Double.compare(p2.getScore(), p1.getScore()));
        
        // 取前 N 個
        int actualLimit = Math.min(limit, sortedPages.size());
        ArrayList<SearchResult> recommendations = new ArrayList<>();
        
        for (int i = 0; i < actualLimit; i++) {
            WebPage page = sortedPages.get(i);
            recommendations.add(new SearchResult(page, page.getScore()));
        }
        
        return recommendations;
    }
    
    /**
     * 根據 ID 取得咖啡廳
     * @param cafeId 咖啡廳 ID
     * @return 咖啡廳資訊，找不到返回 null
     */
    public WebPage getCafeById(String cafeId) {
        for (WebPage page : allPages) {
            if (page.getUrl().contains(cafeId) || page.getName().equals(cafeId)) {
                return page;
            }
        }
        return null;
    }
    
    /**
     * 搜尋建議（自動完成）
     * 根據使用者輸入的部分文字，返回相關的關鍵字建議
     * @param partialQuery 部分查詢字串
     * @return 建議的關鍵字列表
     */
    public List<String> getSearchSuggestions(String partialQuery) {
        if (partialQuery == null || partialQuery.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerQuery = partialQuery.toLowerCase();
        List<String> suggestions = new ArrayList<>();
        
        // 從關鍵字列表中找出匹配的
        for (Keyword keyword : keywords) {
            if (keyword.name.toLowerCase().contains(lowerQuery)) {
                suggestions.add(keyword.name);
            }
        }
        
        // 限制返回數量
        return suggestions.size() > 10 ? suggestions.subList(0, 10) : suggestions;
    }
    
    /**
     * 建立 WebTree（用於計算深度權重）
     * @param rootPage 根網站
     * @param maxDepth 最大深度
     */
    public void buildWebTree(WebPage rootPage, int maxDepth) {
        WebTree tree = new WebTree(rootPage);
        tree.buildTree(tree.getRoot(), 0, maxDepth);
        
        // 計算樹的後序分數（包含深度權重）
        tree.setPostOrderScore(keywords);
        
        webTrees.put(rootPage.getUrl(), tree);
    }
    
    /**
     * 取得所有網站
     * @return 網站列表
     */
    public ArrayList<WebPage> getAllPages() {
        return new ArrayList<>(allPages);
    }
    
    /**
     * 新增網站
     * @param page 網站
     */
    public void addPage(WebPage page) {
        if (!allPages.contains(page)) {
            allPages.add(page);
        }
    }
    
    /**
     * 移除網站
     * @param page 網站
     */
    public void removePage(WebPage page) {
        allPages.remove(page);
    }
    
    /**
     * 取得 Ranker
     * @return Ranker 物件
     */
    public Ranker getRanker() {
        return ranker;
    }
    
    /**
     * 取得關鍵字列表
     * @return 關鍵字列表
     */
    public ArrayList<Keyword> getKeywords() {
        return new ArrayList<>(keywords);
    }
    
    /**
     * 新增關鍵字
     * @param keyword 關鍵字
     */
    public void addKeyword(Keyword keyword) {
        keywords.add(keyword);
    }
    
    /**
     * 設定關鍵字列表
     * @param keywords 關鍵字列表
     */
    public void setKeywords(ArrayList<Keyword> keywords) {
        this.keywords = keywords;
    }
    
    /**
     * 取得 BaselineScoreCalculator
     * @return BaselineScoreCalculator 物件
     */
    public BaselineScoreCalculator getBaselineCalculator() {
        return baselineCalculator;
    }
    
    /**
     * 取得 HashtagGenerator
     * @return HashtagGenerator 物件
     */
    public HashtagGenerator getHashtagGenerator() {
        return hashtagGenerator;
    }
    
    /**
     * 列印搜尋結果
     * @param results 搜尋結果列表
     * @param topN 顯示前 N 個
     */
    public void printResults(ArrayList<SearchResult> results, int topN) {
        int limit = Math.min(topN, results.size());
        
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║          咖啡廳搜尋結果 (Top " + limit + ")                      ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
        
        for (int i = 0; i < limit; i++) {
            SearchResult result = results.get(i);
            WebPage page = result.getPage();
            
            System.out.printf("【%d】%s\n", i + 1, page.getName());
            System.out.printf("    分數: %.2f\n", result.getScore());
            System.out.printf("    標籤: %s\n", page.getHashtags());
            System.out.printf("    地區: %s | 分類: %s\n", page.getDistrict(), page.getFeatures());
            System.out.printf("    地址: %s\n", page.getAddress());
            System.out.printf("    網址: %s\n", page.getUrl());
            System.out.println();
        }
    }
    
    /**
     * 取得搜尋引擎統計資訊
     * @return 統計資訊
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCafes", allPages.size());
        stats.put("totalKeywords", keywords.size());
        stats.put("webTreesBuilt", webTrees.size());
        
        // 計算平均基準分數
        double avgScore = allPages.stream()
                                  .mapToDouble(WebPage::getScore)
                                  .average()
                                  .orElse(0.0);
        stats.put("averageBaselineScore", avgScore);
        
        return stats;
    }
}

