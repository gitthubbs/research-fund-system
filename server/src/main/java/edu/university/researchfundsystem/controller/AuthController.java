package edu.university.researchfundsystem.controller;

import edu.university.researchfundsystem.common.Result;
import edu.university.researchfundsystem.entity.SysUser;
import edu.university.researchfundsystem.model.dto.LoginRequest;
import edu.university.researchfundsystem.model.vo.LoginResponseVO;
import edu.university.researchfundsystem.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService userService;

    @PostMapping("/login")
    public Result<LoginResponseVO> login(@RequestBody LoginRequest loginRequest) {
        SysUser user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        LoginResponseVO response = new LoginResponseVO();
        response.setToken("token-" + user.getId());
        response.setRole(user.getRole());

        LoginResponseVO.UserInfo userInfo = new LoginResponseVO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setName(user.getRealName());
        userInfo.setRole(user.getRole());
        userInfo.setDepartment(user.getDepartment());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        response.setUser(userInfo);

        return Result.success(response);
    }
}
