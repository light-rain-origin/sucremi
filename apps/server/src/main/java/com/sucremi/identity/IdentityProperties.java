package com.sucremi.identity;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 身份相关配置，前缀 {@code sucremi.identity}。
 */
@ConfigurationProperties("sucremi.identity")
public class IdentityProperties {

    /** 开发期固定身份使用的 app_user.id（对应 V2 迁移 seed 的本地用户）。 */
    private long devUserId = 1L;

    public long getDevUserId() {
        return devUserId;
    }

    public void setDevUserId(long devUserId) {
        this.devUserId = devUserId;
    }
}
