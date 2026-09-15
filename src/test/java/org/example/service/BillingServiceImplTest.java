package org.example.service;

import org.example.entity.Invoice;
import org.example.entity.Product;
import org.example.entity.Store;
import org.example.exception.InsufficientStockException;
import org.example.repository.InvoiceRepository;
import org.example.repository.ProductRepository;
import org.example.repository.StockTransactionRepository;
import org.example.repository.StoreRepository;
import org.example.request.CreateInvoiceRequest;
import org.example.response.InvoiceResponse;
import org.example.service.impl.BillingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockTransactionRepository stockTransactionRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private StoreRepository storeRepository;

    private BillingServiceImpl billingService;
    private Store store;
    private Product widget;
    private Product gadget;

    @BeforeEach
    void setUp() {
        billingService = new BillingServiceImpl(productRepository, stockTransactionRepository, invoiceRepository, storeRepository);

        store = new Store("Main Store", "");
        store.setId(1L);

        widget = product(1L, "WIDGET", new BigDecimal("10.00"), 10);
        gadget = product(2L, "GADGET", new BigDecimal("25.00"), 5);
    }

    private Product product(Long id, String sku, BigDecimal price, int quantity) {
        Product product = new Product();
        product.setId(id);
        product.setSku(sku);
        product.setName(sku);
        product.setStore(store);
        product.setUnitPrice(price);
        product.setQuantityOnHand(quantity);
        product.setReorderThreshold(1);
        return product;
    }

    @Test
    void createInvoiceDeductsStockAndComputesTotal() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(productRepository.findById(1L)).thenReturn(Optional.of(widget));
        when(productRepository.findById(2L)).thenReturn(Optional.of(gadget));
        when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Map<Long, Integer> lineItems = new LinkedHashMap<>();
        lineItems.put(1L, 2);
        lineItems.put(2L, 1);

        InvoiceResponse invoice = billingService.createInvoice(new CreateInvoiceRequest(1L, lineItems));

        assertEquals(new BigDecimal("45.00"), invoice.getTotalAmount());
        assertEquals(8, widget.getQuantityOnHand());
        assertEquals(4, gadget.getQuantityOnHand());
    }

    @Test
    void insufficientStockRollsBackWholeInvoice() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(productRepository.findById(1L)).thenReturn(Optional.of(widget));
        when(productRepository.findById(2L)).thenReturn(Optional.of(gadget));

        Map<Long, Integer> lineItems = new LinkedHashMap<>();
        lineItems.put(1L, 2);
        lineItems.put(2L, 999);

        assertThrows(InsufficientStockException.class,
                () -> billingService.createInvoice(new CreateInvoiceRequest(1L, lineItems)));

        assertEquals(10, widget.getQuantityOnHand());
        assertEquals(5, gadget.getQuantityOnHand());
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void emptyLineItemsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> billingService.createInvoice(new CreateInvoiceRequest(1L, Map.of())));
    }
}
