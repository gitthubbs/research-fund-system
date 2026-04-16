package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.common.SecurityUtils;
import edu.university.researchfundsystem.entity.SysUser;
import edu.university.researchfundsystem.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final SysUserService userService;

    @GetMapping("/me")
    public Result<SysUser> getProfile(HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未登录或会话已过期");
        }
        SysUser user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null); // 隐藏密码
        }
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<Boolean> updateProfile(HttpServletRequest request, @RequestBody SysUser user) {
        Long currentUserId = SecurityUtils.getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("未登录");
        }
        
        // 核心安全：强制设置 ID 为当前会话 ID，防止越权修改他人
        user.setId(currentUserId);
        
        // 禁止修改账号、角色、密码
        user.setUsername(null);
        user.setRole(null);
        user.setPassword(null);
        
        boolean success = userService.updateById(user);
        return Result.success(success);
    }

    @PostMapping("/change-password")
    public Result<Boolean> changePassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Long userId = SecurityUtils.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }
        
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }
        
        try {
            boolean success = userService.changePassword(userId, oldPassword, newPassword);
            return Result.success(success);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}