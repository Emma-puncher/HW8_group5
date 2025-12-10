package com.example.GoogleQuery.filter;

import com.example.GoogleQuery.model.SearchResult;
import java.util.ArrayList;

/**
 * Filter - 篩選器介面
 * 所有篩選器都必須實作此介面
 */
public interface Filter {
    
    /**
     * 執行篩選
     * @param results 搜尋結果列表
     * @return 篩選後的結果列表
     */
    ArrayList<SearchResult> filter(ArrayList<SearchResult> results);
    
    /**
     * 獲取篩選器描述
     * @return 描述文字
     */
    default String getDescription() {
        return this.getClass().getSimpleName();
    }
}

    }

    /**
     * 設定是否記錄統計資訊
     * @param logStatistics 是否記錄
     */
    public void setLogStatistics(boolean logStatistics) {
        this.logStatistics = logStatistics;
    }

    /**
     * 是否記錄統計資訊
     * @return 是否記錄
     */
    public boolean isLogStatistics() {
        return logStatistics;
    }

    /**
     * 執行篩選並返回統計資訊
     * @param results 搜尋結果列表
     * @return 包含結果和統計的物件
     */
    public FilterResult filterWithStatistics(ArrayList<SearchResult> results) {
        FilterResult filterResult = new FilterResult();
        filterResult.originalCount = results.size();
        
        ArrayList<SearchResult> filteredResults = results;
        
        for (Filter filter : filters) {
            int beforeCount = filteredResults.size();
            filteredResults = filter.filter(filteredResults);
            int afterCount = filteredResults.size();
            
            FilterStepStatistics stepStats = new FilterStepStatistics();
            stepStats.filterName = filter.getDescription();
            stepStats.beforeCount = beforeCount;
            stepStats.afterCount = afterCount;
            stepStats.removedCount = beforeCount - afterCount;
            stepStats.retentionRate = beforeCount > 0 
                ? (double) afterCount / beforeCount * 100 
                : 0.0;
            
            filterResult.stepStatistics.add(stepStats);
        }
        
        filterResult.results = filteredResults;
        filterResult.finalCount = filteredResults.size();
        filterResult.totalRetentionRate = results.size() > 0 
            ? (double) filteredResults.size() / results.size() * 100 
            : 0.0;
        
        return filterResult;
    }

    /**
     * 篩選結果物件（包含統計資訊）
     */
    public static class FilterResult {
        public ArrayList<SearchResult> results;
        public int originalCount;
        public int finalCount;
        public double totalRetentionRate;
        public List<FilterStepStatistics> stepStatistics = new ArrayList<>();
        
        /**
         * 獲取統計報告
         * @return 報告文字
         */
        public String getStatisticsReport() {
            StringBuilder report = new StringBuilder();
            report.append("=== 篩選統計報告 ===\n");
            report.append("原始結果: ").append(originalCount).append(" 筆\n");
            report.append("最終結果: ").append(finalCount).append(" 筆\n");
            report.append("總保留率: ").append(String.format("%.1f%%", totalRetentionRate)).append("\n\n");
            
            report.append("各步驟統計:\n");
            for (int i = 0; i < stepStatistics.size(); i++) {
                FilterStepStatistics step = stepStatistics.get(i);
                report.append("步驟 ").append(i + 1).append(": ").append(step.filterName).append("\n");
                report.append("  前: ").append(step.beforeCount).append(" 筆\n");
                report.append("  後: ").append(step.afterCount).append(" 筆\n");
                report.append("  移除: ").append(step.removedCount).append(" 筆\n");
                report.append("  保留率: ").append(String.format("%.1f%%", step.retentionRate)).append("\n\n");
            }
            
            return report.toString();
        }
    }

    /**
     * 單步篩選統計
     */
    public static class FilterStepStatistics {
        public String filterName;
        public int beforeCount;
        public int afterCount;
        public int removedCount;
        public double retentionRate;
    }

    /**
     * 應用篩選鏈（靜態方法）
     * @param results 搜尋結果
     * @param filters 篩選器列表
     * @return 篩選後的結果
     */
    public static ArrayList<SearchResult> apply(ArrayList<SearchResult> results, Filter... filters) {
        FilterChain chain = new FilterChain();
        for (Filter filter : filters) {
            chain.addFilter(filter);
        }
        return chain.filter(results);
    }

    /**
     * 獲取篩選器鏈描述
     * @return 描述文字
     */
    @Override
    public String getDescription() {
        if (filters.isEmpty()) {
            return "空的篩選器鏈";
        }
        
        StringBuilder description = new StringBuilder("篩選器鏈（");
        description.append(filters.size()).append(" 個篩選器）:\n");
        
        for (int i = 0; i < filters.size(); i++) {
            description.append("  ").append(i + 1).append(". ")
                      .append(filters.get(i).getDescription());
            if (i < filters.size() - 1) {
                description.append("\n");
            }
        }
        
        return description.toString();
    }

    /**
     * 複製篩選器鏈
     * @return 新的篩選器鏈實例
     */
    public FilterChain copy() {
        FilterChain newChain = new FilterChain(new ArrayList<>(this.filters));
        newChain.setLogStatistics(this.logStatistics);
        return newChain;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FilterChain that = (FilterChain) obj;
        return filters.equals(that.filters);
    }

    @Override
    public int hashCode() {
        return filters.hashCode();
    }
}

