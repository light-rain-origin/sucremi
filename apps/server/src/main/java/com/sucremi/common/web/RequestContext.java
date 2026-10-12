package com.sucremi.common.web;

import org.slf4j.MDC;

/**
 * 请求级上下文工具：保存并读取当前请求的追踪 ID。
 *
 * <p>由 {@link RequestIdFilter} 在每个请求开始时写入、结束时清理，
 * 底层使用 SLF4J {@link MDC}（线程绑定），因此日志里也能直接打印同一个 ID。</p>
 */
public final class RequestContext {

    /** MDC 中保存 requestId 的键，日志 pattern 可用 %X{requestId} 引用。 */
    public static final String REQUEST_ID_KEY = "requestId";

    /** 透传与回写请求 ID 的 HTTP 头名。 */
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private RequestContext() {
    }

    public static String currentRequestId() {
        return MDC.get(REQUEST_ID_KEY);
    }

    public static void setRequestId(String requestId) {
        MDC.put(REQUEST_ID_KEY, requestId);
    }

    public static void clear() {
        MDC.remove(REQUEST_ID_KEY);
    }
}
