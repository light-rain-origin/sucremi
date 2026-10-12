package com.sucremi.common.api;

import com.sucremi.common.web.RequestContext;

import java.time.Instant;

/**
 * 统一 API 响应结构。
 *
 * <p>约定：{@code code == 0} 表示成功，非 0 表示业务或系统错误。
 * 响应中只包含对客户端安全的信息，不暴露堆栈或内部细节。</p>
 *
 * @param code      业务状态码，0 为成功
 * @param message   可读的提示信息
 * @param data      业务数据，错误时为 null
 * @param requestId 本次请求的追踪 ID（与响应头 X-Request-Id 一致，便于日志关联）
 * @param timestamp 服务端生成响应的时间戳（毫秒）
 */
public record ApiResponse<T>(int code, String message, T data, String requestId, long timestamp) {

    public static <T> ApiResponse<T> ok(T data) {
        return build(0, "OK", data);
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return build(code, message, null);
    }

    private static <T> ApiResponse<T> build(int code, String message, T data) {
        return new ApiResponse<>(code, message, data,
                RequestContext.currentRequestId(), Instant.now().toEpochMilli());
    }
}
