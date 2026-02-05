package com.moyz.adi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索Return网页页对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchReturnWebPage {
    /**
     * 标题
     */
    private String title;
    /**
     * link
     */
    private String link;
    /**
     * snippet
     */
    private String snippet;
    /**
     * 内容
     */
    private String content;
}
