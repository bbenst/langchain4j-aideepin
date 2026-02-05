package com.moyz.adi.common.dto;

import com.moyz.adi.common.annotation.NotAllFieldsEmptyCheck;
import lombok.Data;

/**
 * 用户更新请求
 */
@Data
@NotAllFieldsEmptyCheck
public class UserUpdateReq {
    /**
     * secret键
     */
    private String secretKey;
    /**
     * 上下文启用
     */
    private Boolean contextEnable;
    /**
     * 上下文MsgPair数量
     */
    private Integer contextMsgPairNum;
}
