package com.sucremi.common.exception;

/**
 * 当前请求没有可信的用户身份（映射为 HTTP 401）。
 *
 * <p>在尚未接入真实认证、且未启用开发期固定身份时抛出，用于“失败关闭”：
 * 防止无身份状态下访问需要归属的业务数据。</p>
 */
public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException(String message) {
        super(message);
    }
}
