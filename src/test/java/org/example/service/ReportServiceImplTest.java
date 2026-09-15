package org.example.service;

import org.example.entity.Invoice;
import org.example.entity.InvoiceItem;
import org.example.entity.Product;
import org.example.entity.Store;
import org.example.repository.InvoiceRepository;
import org.example.request.MonthlyReportRequest;
import org.example.response.MonthlySalesReportResponse;
import org.example.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Test
    void monthlySummaryAssemblesReportFromInvoices() {
        ReportServiceImpl reportService = new ReportServiceImpl(invoiceRepository);
        YearMonth month = YearMonth.of(2026, 9);

        Store store = new Store("Main Store", "");
        store.setId(1L);
        Product widget = new Product();
        widget.setId(10L);
        widget.setName("Widget");
        widget.setStore(store);
        widget.setUnitPrice(new BigDecimal("10.00"));

        Invoice invoice = new Invoice(store);
        invoice.setInvoiceNumber("INV-1");
        invoice.setTotalAmount(new BigDecimal("150.00"));
        InvoiceItem item = new InvoiceItem(widget, 10, new BigDecimal("10.00"));
        invoice.addItem(item);

        when(invoiceRepository.findWithItemsByStoreAndDateRange(eq(1L), any(), any())).thenReturn(List.of(invoice));

        MonthlySalesReportResponse report = reportService.monthlySummary(new MonthlyReportRequest(1L, month));

        assertEquals(month, report.getMonth());
        assertEquals(1, report.getInvoiceCount());
        assertEquals(new BigDecimal("150.00"), report.getTotalSales());
        assertEquals(1, report.getProductBreakdown().size());
        assertEquals(10, report.getProductBreakdown().get(0).getQuantitySold());
    }
}
