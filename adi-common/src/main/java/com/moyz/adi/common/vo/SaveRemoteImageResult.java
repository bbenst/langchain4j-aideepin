package com.moyz.adi.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 保存Remote图片Result对象
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SaveRemoteImageResult {
    /**
     * original名称
     */
    private String originalName;
    /**
     * ext
     */
    private String ext;
    /**
     * 路径或URL
     */
    private String pathOrUrl;
}
