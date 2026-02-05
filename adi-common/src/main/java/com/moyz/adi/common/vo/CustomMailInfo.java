package com.moyz.adi.common.vo;

import lombok.Data;

/**
 * CustomMail信息对象
 */
@Data
public class CustomMailInfo {
    /**
     * host
     */
    private String host;
    /**
     * port
     */
    private Integer port;
    /**
     * sender名称
     */
    private String senderName;
    /**
     * senderMail
     */
    private String senderMail;
    /**
     * sender密码
     */
    private String senderPassword;
    /**
     * ccMails
     */
    private String ccMails;
    /**
     * toMails
     */
    private String toMails;
    /**
     * subject
     */
    private String subject;
    /**
     * 内容
     */
    private String content;
}
