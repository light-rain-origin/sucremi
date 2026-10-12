package com.sucremi.health;

import com.sucremi.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查接口。
 *
 * <p>用途：前端开发代理连通性验证与基础存活探测。不依赖数据库，
 * 以便在数据库未就绪时也能确认后端进程正常。</p>
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @Value("${spring.application.name:sucremi-server}")
    private String appName;

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("app", appName);
        body.put("time", Instant.now().toString());
        return ApiResponse.ok(body);
    }
}
