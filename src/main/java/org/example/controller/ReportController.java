package org.example.controller;

import org.example.request.MonthlyReportRequest;
import org.example.response.MonthlySalesReportResponse;
import org.example.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public MonthlySalesReportResponse monthlySummary(@RequestParam Long storeId,
                                                      @RequestParam int year,
                                                      @RequestParam int month) {
        return reportService.monthlySummary(new MonthlyReportRequest(storeId, YearMonth.of(year, month)));
    }
}
