package org.example.controller;

import org.example.request.CreateInvoiceRequest;
import org.example.response.InvoiceResponse;
import org.example.service.BillingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/createInvoice")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse createInvoice(@RequestBody CreateInvoiceRequest request) {
        return billingService.createInvoice(request);
    }

    @GetMapping
    public List<InvoiceResponse> listInvoices(@RequestParam Long storeId,
                                               @RequestParam int year,
                                               @RequestParam int month) {
        return billingService.listInvoices(storeId, YearMonth.of(year, month));
    }
}
