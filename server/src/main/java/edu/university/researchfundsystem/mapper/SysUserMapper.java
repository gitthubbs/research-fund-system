package edu.university.researchfundsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.university.researchfundsystem.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
