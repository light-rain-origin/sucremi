package com.sucremi.common.exception;

/** 表示资源基于过期版本被修改，客户端应重新读取后再提交。 */
public class ResourceConflictException extends RuntimeException {
    /** 创建并发修改冲突异常。 */
    public ResourceConflictException(String message) {
        super(message);
    }
}
