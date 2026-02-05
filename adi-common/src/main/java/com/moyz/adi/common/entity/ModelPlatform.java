package com.moyz.adi.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ModelPlatform对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "adi_model_platform", autoResultMap = true)
@Schema(title = "ModelPlatform对象", description = "模型平台表")
public class ModelPlatform extends BaseEntity {
    /**
     * 名称
     */
    @Schema(title = "名称")
    @TableField("name")
    private String name;
    /**
     * 标题(更易理解记忆的名称)
     */
    @Schema(title = "标题(更易理解记忆的名称)")
    @TableField("title")
    private String title;
    /**
     * 说明
     */
    @Schema(title = "说明")
    @TableField("remark")
    private String remark;
    /**
     * 库URL
     */
    @Schema(title = "base url")
    @TableField("base_url")
    private String baseUrl;
    /**
     * api键
     */
    @Schema(title = "api key")
    @TableField("api_key")
    private String apiKey;
    /**
     * secret key, 可选，目前只有百度的千帆在用，其他模型平台的key直接放到 api_key 即可
     */
    @Schema(title = "secret key, 可选，目前只有百度的千帆在用，其他模型平台的key直接放到 api_key 即可")
    @TableField("secret_key")
    private String secretKey;
    /**
     * 是否开启代理，代理的详细配置在全局配置里，路径：adi.proxy
     */
    @Schema(title = "是否开启代理，代理的详细配置在全局配置里，路径：adi.proxy")
    @TableField("is_proxy_enable")
    private Boolean isProxyEnable;
    /**
     * 平台接口是否兼容OpenAI API
     */
    @Schema(title = "平台接口是否兼容OpenAI API")
    @TableField("is_openai_api_compatible")
    private Boolean isOpenaiApiCompatible;
}
