package com.sucremi.identity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 身份解析器装配。
 *
 * <p>策略（见 docs/decisions/0001-identity-and-data-ownership.md）：</p>
 * <ul>
 *   <li>开发相关 profile（local/dev/test）：注册 {@link DevUserResolver}（固定身份）。</li>
 *   <li>其它情况（未指定 profile 或 prod）：由 {@link DenyingUserResolver} 兜底，
 *       需要身份的请求返回 401（失败关闭），防止被误用于公开部署。</li>
 *   <li>将来接入真实认证时，注册一个 {@link CurrentUserResolver} 实现（如基于
 *       Spring Security 的 SecurityContext），兜底 bean 会自动让位。</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(IdentityProperties.class)
public class IdentityConfig {

    private static final Logger log = LoggerFactory.getLogger(IdentityConfig.class);

    @Bean
    @Profile({"local", "dev", "test"})
    public CurrentUserResolver devUserResolver(IdentityProperties properties) {
        log.warn("开发期固定身份已启用 (dev-user-id={})。严禁用于公开部署；生产请以非开发 profile 启动。",
                properties.getDevUserId());
        return new DevUserResolver(properties.getDevUserId());
    }

    @Bean
    @ConditionalOnMissingBean(CurrentUserResolver.class)
    public CurrentUserResolver denyingUserResolver() {
        log.info("未启用开发期身份且无真实认证解析器：需要身份的请求将返回 401（失败关闭）。");
        return new DenyingUserResolver();
    }
}
