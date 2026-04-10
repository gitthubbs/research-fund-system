package edu.university.researchfundsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.university.researchfundsystem.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    /**
     * 用户登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户信息，失败则返回 null
     */
    SysUser login(String username, String password);
}
