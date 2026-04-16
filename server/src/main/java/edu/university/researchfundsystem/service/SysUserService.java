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

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}
