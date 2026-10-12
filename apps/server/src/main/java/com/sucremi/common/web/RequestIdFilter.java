package com.sucremi.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 为每个请求建立追踪 ID。
 *
 * <p>优先沿用调用方传入的 {@code X-Request-Id}（便于跨服务串联），否则生成一个 UUID。
 * 该 ID 写入 {@link RequestContext}（供统一响应与日志读取），并回写到响应头。</p>
 */
@Component
public class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = request.getHeader(RequestContext.REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        RequestContext.setRequestId(requestId);
        response.setHeader(RequestContext.REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestContext.clear();
        }
    }
}
