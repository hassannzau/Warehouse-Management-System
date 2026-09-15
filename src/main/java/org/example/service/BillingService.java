package org.example.service;

import org.example.request.CreateInvoiceRequest;
import org.example.response.InvoiceResponse;

import java.time.YearMonth;
import java.util.List;

public interface BillingService {

    // Creates a sale invoice, deducting stock for each line item. The whole operation
     //is atomic: if any line has insufficient stock, nothing is persisted.

    InvoiceResponse createInvoice(CreateInvoiceRequest request);

    List<InvoiceResponse> listInvoices(Long storeId, YearMonth month);
}
