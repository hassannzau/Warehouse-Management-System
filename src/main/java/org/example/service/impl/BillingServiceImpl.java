package org.example.service.impl;

import org.example.entity.Invoice;
import org.example.entity.InvoiceItem;
import org.example.entity.Product;
import org.example.entity.StockTransaction;
import org.example.entity.Store;
import org.example.enums.TransactionType;
import org.example.repository.InvoiceRepository;
import org.example.repository.ProductRepository;
import org.example.repository.StockTransactionRepository;
import org.example.repository.StoreRepository;
import org.example.request.CreateInvoiceRequest;
import org.example.response.InvoiceLineResponse;
import org.example.response.InvoiceResponse;
import org.example.service.BillingService;
import org.example.exception.InsufficientStockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class BillingServiceImpl implements BillingService {

    private static final DateTimeFormatter INVOICE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ProductRepository productRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final StoreRepository storeRepository;

    public BillingServiceImpl(ProductRepository productRepository, StockTransactionRepository stockTransactionRepository,
                               InvoiceRepository invoiceRepository, StoreRepository storeRepository) {
        this.productRepository = productRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.invoiceRepository = invoiceRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        Map<Long, Integer> lineItems = request.getLineItems();
        if (lineItems.isEmpty()) {
            throw new IllegalArgumentException("An invoice needs at least one line item");
        }

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown store: " + request.getStoreId()));

        // Validate every line before mutating anything, so a failure on a later line
        // never leaves an earlier line's stock deduction applied.
        Map<Long, Product> products = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> entry : lineItems.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for product " + productId);
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown product: " + productId));
            if (product.getQuantityOnHand() < quantity) {
                throw new InsufficientStockException(
                        "Insufficient stock for " + product.getName() + ": have "
                                + product.getQuantityOnHand() + ", requested " + quantity);
            }
            products.put(productId, product);
        }

        Invoice invoice = new Invoice(store);
        invoice.setInvoiceNumber(generateInvoiceNumber());

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : lineItems.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();
            Product product = products.get(productId);

            product.setQuantityOnHand(product.getQuantityOnHand() - quantity);
            stockTransactionRepository.save(new StockTransaction(
                    product, TransactionType.OUT, quantity, product.getUnitPrice(), "Sale " + invoice.getInvoiceNumber()));

            InvoiceItem item = new InvoiceItem(product, quantity, product.getUnitPrice());
            invoice.addItem(item);
            total = total.add(item.getLineTotal());
        }
        invoice.setTotalAmount(total);

        Invoice saved = invoiceRepository.save(invoice);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> listInvoices(Long storeId, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();
        return invoiceRepository.findWithItemsByStoreAndDateRange(storeId, start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        List<InvoiceLineResponse> lines = invoice.getItems().stream()
                .map(item -> new InvoiceLineResponse(item.getProduct().getId(), item.getProduct().getName(),
                        item.getQuantity(), item.getUnitPrice(), item.getLineTotal()))
                .collect(Collectors.toList());
        return new InvoiceResponse(invoice.getId(), invoice.getInvoiceNumber(), invoice.getStore().getId(),
                invoice.getTotalAmount(), invoice.getCreatedAt(), lines);
    }

    private String generateInvoiceNumber() {
        String timestamp = LocalDateTime.now().format(INVOICE_TIMESTAMP);
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "INV-" + timestamp + "-" + suffix;
    }
}
