package edu.university.researchfundsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.university.researchfundsystem.entity.FundCategory;
import edu.university.researchfundsystem.mapper.FundCategoryMapper;
import edu.university.researchfundsystem.service.FundCategoryService;
import org.springframework.stereotype.Service;

@Service
public class FundCategoryServiceImpl extends ServiceImpl<FundCategoryMapper, FundCategory>
        implements FundCategoryService {
}
