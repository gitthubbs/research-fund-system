package edu.university.researchfundsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("fund_category")
public class FundCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String categoryName;
}
