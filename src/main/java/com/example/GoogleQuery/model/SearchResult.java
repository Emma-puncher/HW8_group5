/**
 * SearchResult - 搜尋結果封裝類別
 * 將 WebPage 和其對應的分數包裝在一起
 */
public class SearchResult implements Comparable<SearchResult> {
    
    private WebPage page;
    private double score;
    
    /**
     * 建構子
     * @param page 網站
     * @param score 分數
     */
    public SearchResult(WebPage page, double score) {
        this.page = page;
        this.score = score;
    }
    
    /**
     * 取得網站
     * @return WebPage 物件
     */
    public WebPage getPage() {
        return page;
    }
    
    /**
     * 取得分數
     * @return 分數
     */
    public double getScore() {
        return score;
    }
    
    /**
     * 設定分數
     * @param score 分數
     */
    public void setScore(double score) {
        this.score = score;
    }
    
    /**
     * 取得網站名稱（便利方法）
     * @return 名稱
     */
    public String getName() {
        return page.getName();
    }
    
    /**
     * 取得網站 URL（便利方法）
     * @return URL
     */
    public String getUrl() {
        return page.getUrl();
    }
    
    /**
     * 取得 Hashtags（便利方法）
     * @return Hashtags 字串
     */
    public String getHashtags() {
        return page.getHashtags();
    }
    
    /**
     * 取得預覽文字（便利方法）
     * @return 預覽文字
     */
    public String getPreview() {
        return page.getPreview();
    }
    
    /**
     * 取得地區（便利方法）
     * @return 地區
     */
    public String getDistrict() {
        return page.getDistrict();
    }
    
    /**
     * 取得分類（便利方法）
     * @return 分類
     */
    public String getCategory() {
        return page.getCategory();
    }
    
    /**
     * 取得地址（便利方法）
     * @return 地址
     */
    public String getAddress() {
        return page.getAddress();
    }
    
    /**
     * 轉換為 JSON 格式字串（用於 API 回傳）
     * @return JSON 字串
     */
    public String toJson() {
        return String.format(
            "{" +
            "\"name\": \"%s\", " +
            "\"url\": \"%s\", " +
            "\"score\": %.2f, " +
            "\"hashtags\": \"%s\", " +
            "\"preview\": \"%s\", " +
            "\"district\": \"%s\", " +
            "\"category\": \"%s\", " +
            "\"address\": \"%s\"" +
            "}",
            escapeJson(page.getName()),
            escapeJson(page.getUrl()),
            score,
            escapeJson(page.getHashtags()),
            escapeJson(page.getPreview()),
            escapeJson(page.getDistrict()),
            escapeJson(page.getCategory()),
            escapeJson(page.getAddress())
        );
    }
    
    /**
     * 轉換為格式化的顯示字串
     * @return 格式化字串
     */
    @Override
    public String toString() {
        return String.format(
            "【%s】\n" +
            "  分數: %.2f | 標籤: %s\n" +
            "  地區: %s | 分類: %s\n" +
            "  地址: %s\n" +
            "  網址: %s",
            page.getName(),
            score,
            page.getHashtags(),
            page.getDistrict(),
            page.getCategory(),
            page.getAddress(),
            page.getUrl()
        );
    }
    
    /**
     * 實作 Comparable 介面（用於排序）
     * 按分數降序排列
     * @param other 另一個 SearchResult
     * @return 比較結果
     */
    @Override
    public int compareTo(SearchResult other) {
        return Double.compare(other.score, this.score);  // 降序
    }
    
    /**
     * equals 方法
     * 基於網站 URL 判斷
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SearchResult other = (SearchResult) obj;
        return page.equals(other.page);
    }
    
    /**
     * hashCode 方法
     */
    @Override
    public int hashCode() {
        return page.hashCode();
    }
    
    /**
     * 轉義 JSON 特殊字元
     * @param str 原始字串
     * @return 轉義後的字串
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}

