package edu.university.researchfundsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.researchfundsystem.entity.SysLog;
import edu.university.researchfundsystem.mapper.SysLogMapper;
import edu.university.researchfundsystem.service.SysLogService;
import org.springframework.stereotype.Service;

@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {
}