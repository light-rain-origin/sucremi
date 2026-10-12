package com.sucremi.common.api;

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
 * @param timestamp 服务端生成响应的时间戳（毫秒）
 */
public record ApiResponse<T>(int code, String message, T data, long timestamp) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "OK", data, Instant.now().toEpochMilli());
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, Instant.now().toEpochMilli());
    }
}
