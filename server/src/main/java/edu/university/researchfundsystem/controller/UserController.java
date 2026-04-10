package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.entity.SysUser;
import edu.university.researchfundsystem.model.vo.UserListItemVO;
import edu.university.researchfundsystem.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService userService;

    @GetMapping("/list")
    public Result<List<UserListItemVO>> list() {
        List<UserListItemVO> users = userService.list().stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
        return Result.success(users);
    }

    @PostMapping("/create")
    public Result<Long> save(@RequestBody SysUser user) {
        user.setCreateTime(java.time.LocalDateTime.now());
        boolean success = userService.save(user);
        if (success) {
            return Result.success(user.getId());
        }
        return Result.error("用户创建失败");
    }

    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody SysUser user) {
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        return Result.success(userService.updateById(user));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }

    @PutMapping("/role/{id}")
    public Result<Boolean> switchRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String role = request.get("role");
        if (role == null || (!"admin".equals(role) && !"researcher".equals(role))) {
            return Result.error("角色参数无效");
        }

        SysUser user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setRole(role);
        return Result.success(userService.updateById(user));
    }

    private UserListItemVO toListItem(SysUser user) {
        UserListItemVO item = new UserListItemVO();
        item.setId(user.getId());
        item.setUsername(user.getUsername());
        item.setName(user.getRealName());
        item.setRole(user.getRole());
        item.setDepartment(user.getDepartment());
        item.setPhone(user.getPhone());
        item.setEmail(user.getEmail());
        item.setCreateTime(user.getCreateTime());
        return item;
    }
}
