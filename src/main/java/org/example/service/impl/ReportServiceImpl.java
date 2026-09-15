package org.example.service.impl;

import org.example.entity.Invoice;
import org.example.entity.InvoiceItem;
import org.example.repository.InvoiceRepository;
import org.example.request.MonthlyReportRequest;
import org.example.response.MonthlySalesReportResponse;
import org.example.response.ProductSalesLineResponse;
import org.example.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final InvoiceRepository invoiceRepository;

    public ReportServiceImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlySalesReportResponse monthlySummary(MonthlyReportRequest request) {
        Long storeId = request.getStoreId();
        YearMonth month = request.getMonth();
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();

        List<Invoice> invoices = invoiceRepository.findWithItemsByStoreAndDateRange(storeId, start, end);

        BigDecimal totalSales = invoices.stream().map(Invoice::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        int invoiceCount = invoices.size();

        Map<Long, ProductSalesAccumulator> byProduct = new LinkedHashMap<>();
        for (Invoice invoice : invoices) {
            for (InvoiceItem item : invoice.getItems()) {
                byProduct.computeIfAbsent(item.getProduct().getId(),
                                id -> new ProductSalesAccumulator(item.getProduct().getName()))
                        .add(item.getQuantity(), item.getLineTotal());
            }
        }

        List<ProductSalesLineResponse> breakdown = new ArrayList<>();
        byProduct.forEach((productId, accumulator) -> breakdown.add(
                new ProductSalesLineResponse(productId, accumulator.productName, accumulator.quantity, accumulator.amount)));
        breakdown.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));

        return new MonthlySalesReportResponse(month, invoiceCount, totalSales, breakdown);
    }

    private static final class ProductSalesAccumulator {
        private final String productName;
        private int quantity = 0;
        private BigDecimal amount = BigDecimal.ZERO;

        private ProductSalesAccumulator(String productName) {
            this.productName = productName;
        }

        private void add(int quantity, BigDecimal amount) {
            this.quantity += quantity;
            this.amount = this.amount.add(amount);
        }
    }
}
