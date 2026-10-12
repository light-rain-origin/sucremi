package com.sucremi.identity;

/**
 * 开发期固定身份：始终返回配置的本地用户 id。
 *
 * <p>仅在开发相关 profile（local/dev/test）下注册，见 {@link IdentityConfig}。
 * 严禁用于公开部署——非开发 profile 下由 {@link DenyingUserResolver} 兜底拒绝。</p>
 */
public class DevUserResolver implements CurrentUserResolver {

    private final long devUserId;

    public DevUserResolver(long devUserId) {
        this.devUserId = devUserId;
    }

    @Override
    public long currentUserId() {
        return devUserId;
    }
}
