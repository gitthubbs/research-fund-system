package edu.university.researchfundsystem.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendVO {

    /**
     * 历史真实支出金额列表
     */
    private List<BigDecimal> actualData;

    /**
     * 月份列表 (yyyy-MM)
     */
    private List<String> months;

    /**
     * 预测数据
     */
    private Forecast forecast;

    /**
     * 理想月均预算基准线
     */
    private BigDecimal baseline;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Forecast {
        /**
         * 预测月份 (yyyy-MM)
         */
        private String nextMonth;

        /**
         * 预测支出金额 (SMA 计算结果)
         */
        private BigDecimal value;

        /**
         * 是否触发预警 (value > baseline)
         */
        private boolean isAlert;
    }
}
