package org.example.service;

import org.example.request.MonthlyReportRequest;
import org.example.response.MonthlySalesReportResponse;

public interface ReportService {

    MonthlySalesReportResponse monthlySummary(MonthlyReportRequest request);
}
