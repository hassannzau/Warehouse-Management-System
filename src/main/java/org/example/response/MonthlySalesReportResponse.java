package org.example.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySalesReportResponse {
    private YearMonth month;
    private int invoiceCount;
    private BigDecimal totalSales;
    private List<ProductSalesLineResponse> productBreakdown;
}
