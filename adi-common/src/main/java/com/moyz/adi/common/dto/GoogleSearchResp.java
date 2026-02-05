package com.moyz.adi.common.dto;

import lombok.Data;

import java.util.List;

/**
 * Google搜索响应
 */
@Data
public class GoogleSearchResp {
    /**
     * kind
     */
    private String kind;
    /**
     * queries
     */
    private Queries queries;
    /**
     * 搜索Information
     */
    private SearchInformation searchInformation;
    /**
     * items
     */
    private List<Item> items;
    /**
     * error
     */
    private GoogleSearchError error;

    @Data
    public static class Queries {
        /**
         * 请求
         */
        private Request[] request;
    }

    @Data
    public static class Request {
        /**
         * 标题
         */
        private String title;
        /**
         * totalResults
         */
        private String totalResults;
        /**
         * 搜索Terms
         */
        private String searchTerms;
        /**
         * 数量
         */
        private Integer count;
        /**
         * 开始索引
         */
        private Integer startIndex;
        /**
         * 输入Encoding
         */
        private String inputEncoding;
        /**
         * 输出Encoding
         */
        private String outputEncoding;
    }

    @Data
    public static class SearchInformation {
        /**
         * 搜索时间
         */
        private double searchTime;
        /**
         * formatted搜索时间
         */
        private String formattedSearchTime;
        /**
         * totalResults
         */
        private String totalResults;
        /**
         * formattedTotalResults
         */
        private String formattedTotalResults;
    }

    @Data
    public static class Item {
        /**
         * kind
         */
        private String kind;
        /**
         * 标题
         */
        private String title;
        /**
         * html标题
         */
        private String htmlTitle;
        /**
         * link
         */
        private String link;
        /**
         * snippet
         */
        private String snippet;
    }
}
