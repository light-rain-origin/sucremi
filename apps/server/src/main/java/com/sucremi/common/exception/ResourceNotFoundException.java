package com.sucremi.common.exception;

/**
 * 请求的资源不存在，或不属于当前用户（映射为 HTTP 404）。
 *
 * <p>跨用户访问统一返回 404（而非 403），避免泄露资源是否存在。</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
