package edu.university.researchfundsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.researchfundsystem.entity.SysUser;
import edu.university.researchfundsystem.mapper.SysUserMapper;
import edu.university.researchfundsystem.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public SysUser login(String username, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username)
                .eq(SysUser::getPassword, password); // 注意：实际项目中应使用加密比对
        return this.getOne(queryWrapper);
    }
}
