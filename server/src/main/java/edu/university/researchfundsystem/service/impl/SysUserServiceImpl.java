package edu.university.researchfundsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.researchfundsystem.entity.SysUser;
import edu.university.researchfundsystem.mapper.SysUserMapper;
import edu.university.researchfundsystem.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public SysUser login(String username, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        SysUser user = this.getOne(queryWrapper);
        
        // 使用 BCrypt 进行安全校验
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = this.getById(userId);
        if (user == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码验证失败");
        }
        // 存储加密后的新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(user);
    }

    @Override
    public boolean save(SysUser entity) {
        if (entity.getPassword() != null) {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        }
        return super.save(entity);
    }

    @Override
    public boolean updateById(SysUser entity) {
        // 如果提供了新密码（非哈希格式鉴别较难，此处假设 update 接口传来的 password 必为明文新密码）
        // 实际开发中通常单独提供修改密码接口，这里为保证管理端修改用户时的通用性进行加密
        if (entity.getPassword() != null && !entity.getPassword().isEmpty()) {
            // 简单判断：如果看起来不像 BCrypt 哈希值，则加密
            if (!entity.getPassword().startsWith("$2a$")) {
                entity.setPassword(passwordEncoder.encode(entity.getPassword()));
            }
        }
        return super.updateById(entity);
    }
}
