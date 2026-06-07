package vn.edu.fpt.dto;

import java.math.BigDecimal;

public record MonthlyRevenueDTO(
        Integer year,
        Integer month,
        BigDecimal revenue
) {}
