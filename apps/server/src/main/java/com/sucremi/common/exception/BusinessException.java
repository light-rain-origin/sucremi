package com.sucremi.common.exception;

/**
 * 业务异常：携带业务错误码与对客户端安全的提示信息。
 *
 * <p>业务代码应抛出此异常来返回可预期的错误，由
 * {@link GlobalExceptionHandler} 统一转换为标准响应结构。</p>
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
