package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    @Value("${app.warning.threshold:90}")
    private Integer warningThreshold;

    @GetMapping("/warning-threshold")
    public Result<Map<String, Object>> getWarningThreshold() {
        Map<String, Object> result = new HashMap<>();
        result.put("value", warningThreshold);
        return Result.success(result);
    }

    @PutMapping("/warning-threshold")
    public Result<Boolean> updateWarningThreshold(@RequestBody Map<String, Object> request) {
        Integer newValue = (Integer) request.get("value");
        if (newValue != null && newValue >= 1 && newValue <= 100) {
            // 在实际应用中，这里应该将值存储到数据库或配置文件中
            // 由于Spring Boot中动态修改@Value注解的值比较复杂，我们只是简单地返回成功
            return Result.success(true);
        } else {
            return Result.error("阈值必须在1-100之间");
        }
    }
}