package com.sucremi.identity;

import com.sucremi.common.exception.UnauthenticatedException;

/**
 * 失败关闭的兜底解析器：没有任何可信身份来源时一律拒绝。
 *
 * <p>当未启用开发期固定身份、也尚未接入真实认证时生效，确保无身份状态下无法访问
 * 需要归属的业务数据（返回 401）。将来注册真实解析器后自动让位
 * （见 {@link IdentityConfig} 的 {@code @ConditionalOnMissingBean}）。</p>
 */
public class DenyingUserResolver implements CurrentUserResolver {

    @Override
    public long currentUserId() {
        throw new UnauthenticatedException("当前请求无可信身份：请接入认证，或在开发环境启用 local profile。");
    }
}
