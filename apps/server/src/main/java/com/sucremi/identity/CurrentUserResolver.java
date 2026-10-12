package com.sucremi.identity;

/**
 * 当前请求的身份解析器——业务层获取“当前用户”的唯一入口。
 *
 * <p>设计目的：让业务代码与具体认证方式解耦。阶段 1 用开发期固定身份实现；
 * 将来接入 Spring Security + JWT / 第三方关联登录时，只需替换本接口的实现
 * （如从 SecurityContext 读取），业务层无需改动。
 * 详见 docs/decisions/0001-identity-and-data-ownership.md。</p>
 */
public interface CurrentUserResolver {

    /**
     * 返回当前已认证用户的 {@code app_user.id}。
     *
     * @throws com.sucremi.common.exception.UnauthenticatedException 当前请求无可信身份时
     */
    long currentUserId();
}
