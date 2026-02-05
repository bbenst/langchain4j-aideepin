package com.moyz.adi.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * AliOss配置对象
 */
@Data
public class AliOssConfig {
    /**
     * endpoint
     */
    private String endpoint;
    /**
     * access键ID
     */
    @JsonProperty("access_key_id")
    private String accessKeyId;
    /**
     * access键Secret
     */
    @JsonProperty("access_key_secret")
    private String accessKeySecret;
    /**
     * bucket名称
     */
    @JsonProperty("bucket_name")
    private String bucketName;
}
