package edu.university.researchfundsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.entity.SysLog;
import edu.university.researchfundsystem.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final SysLogService logService;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysLog::getCreateTime);
        wrapper.last("LIMIT 20"); // 限制返回最近20条记录
        
        List<SysLog> logs = logService.list(wrapper);
        
        // ★ 修改：转换为前端期望的格式 (operator, module, content)
        List<Map<String, Object>> result = logs.stream().map(log -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", log.getId());
            map.put("operator", log.getUsername()); // ★ 修改
            map.put("module", "科研经费系统"); // 目前统一模块名
            map.put("content", log.getAction()); // ★ 修改
            map.put("time", log.getCreateTime().toString());
            map.put("ip", log.getIpAddress());
            map.put("status", log.getStatusCode());
            return map;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }
}