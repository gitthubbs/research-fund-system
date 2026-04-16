package edu.university.researchfundsystem.common.aspect; // ★ 新增

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.university.researchfundsystem.common.SecurityUtils;
import edu.university.researchfundsystem.common.annotation.Log;
import edu.university.researchfundsystem.entity.SysLog;
import edu.university.researchfundsystem.entity.SysUser;
import edu.university.researchfundsystem.service.SysLogService;
import edu.university.researchfundsystem.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private final SysLogService logService;
    private final SysUserService userService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        long beginTime = System.currentTimeMillis();
        
        Object result = null;
        Throwable exception = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            try {
                // 保存日志
                saveLog(joinPoint, logAnnotation, result, exception);
            } catch (Exception e) {
                log.error("日志记录异常: {}", e.getMessage());
            }
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, Log logAnnotation, Object result, Throwable exception) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;
        
        HttpServletRequest request = attributes.getRequest();
        
        SysLog sysLog = new SysLog();
        sysLog.setAction(logAnnotation.value());
        sysLog.setCreateTime(LocalDateTime.now());
        
        // 设置用户信息
        Long userId = SecurityUtils.getCurrentUserId(request);
        if (userId != null) {
            sysLog.setUserId(userId);
            SysUser user = userService.getById(userId);
            if (user != null) {
                sysLog.setUsername(user.getUsername());
            } else {
                sysLog.setUsername("Unknown(" + userId + ")");
            }
        } else {
            sysLog.setUserId(0L);
            sysLog.setUsername("Anonymous");
        }

        // 设置请求环境信息
        sysLog.setIpAddress(getIpAddr(request));
        sysLog.setUserAgent(request.getHeader("User-Agent"));
        
        // 设置请求数据 (取第一个参数，通常是 @RequestBody 对象)
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                sysLog.setRequestData(objectMapper.writeValueAsString(args[0]));
            }
        } catch (Exception e) {
            sysLog.setRequestData("Error serializing request data");
        }

        // 设置结果与状态码
        if (exception != null) {
            sysLog.setStatusCode(500);
            sysLog.setResponseData(exception.getMessage());
        } else {
            sysLog.setStatusCode(200);
            try {
                sysLog.setResponseData(objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                sysLog.setResponseData("Error serializing response data");
            }
        }

        logService.save(sysLog);
    }

    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
