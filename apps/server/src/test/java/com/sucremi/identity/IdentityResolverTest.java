package com.sucremi.identity;

import com.sucremi.common.exception.UnauthenticatedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 身份解析器单元测试（纯 POJO，无需 Spring 上下文/数据库）。
 */
class IdentityResolverTest {

    @Test
    void devResolverReturnsConfiguredId() {
        assertEquals(42L, new DevUserResolver(42L).currentUserId());
    }

    @Test
    void denyingResolverRejects() {
        assertThrows(UnauthenticatedException.class,
                () -> new DenyingUserResolver().currentUserId());
    }
}
