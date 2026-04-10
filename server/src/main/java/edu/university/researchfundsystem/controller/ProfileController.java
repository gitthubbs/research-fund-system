package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.entity.SysUser;
import edu.university.researchfundsystem.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final SysUserService userService;

    @GetMapping("/me")
    public Result<SysUser> getProfile() {
        // 这里应该从认证上下文中获取当前用户ID
        // 为了演示，我们暂时返回一个默认用户
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRealName("测试用户");
        user.setRole("researcher");
        user.setDepartment("计算机学院");
        user.setPhone("13800138000");
        user.setEmail("test@example.com");
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<Boolean> updateProfile(@RequestBody SysUser user) {
        // 这里应该从认证上下文中获取当前用户ID
        // 并只允许更新当前用户的资料
        user.setPassword(null); // 不允许更新密码，如果需要更新密码应该使用专门的接口
        boolean success = userService.updateById(user);
        return Result.success(success);
    }
}