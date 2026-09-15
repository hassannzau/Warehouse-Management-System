package org.example.service;

import org.example.entity.Product;
import org.example.entity.Store;
import org.example.exception.InsufficientStockException;
import org.example.repository.CategoryRepository;
import org.example.repository.ProductRepository;
import org.example.repository.StockTransactionRepository;
import org.example.repository.StoreRepository;
import org.example.request.StockInRequest;
import org.example.request.StockOutRequest;
import org.example.response.ProductResponse;
import org.example.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the service layer, mocking the Spring Data JPA repositories rather
 * than talking to a real database.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private StockTransactionRepository stockTransactionRepository;

    private InventoryServiceImpl inventoryService;
    private Product widget;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(productRepository, categoryRepository, storeRepository, stockTransactionRepository);

        widget = new Product();
        widget.setId(1L);
        widget.setSku("SKU-1");
        widget.setName("Widget");
        widget.setStore(new Store("Main Store", ""));
        widget.getStore().setId(1L);
        widget.setUnitPrice(new BigDecimal("9.99"));
        widget.setQuantityOnHand(10);
        widget.setReorderThreshold(5);
    }

    @Test
    void stockInIncreasesQuantityAndRecordsTransaction() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(widget));

        inventoryService.stockIn(new StockInRequest(1L, 5, new BigDecimal("9.99"), "restock"));

        assertEquals(15, widget.getQuantityOnHand());
        verify(stockTransactionRepository).save(any());
    }

    @Test
    void stockOutDecreasesQuantity() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(widget));

        inventoryService.stockOut(new StockOutRequest(1L, 4, new BigDecimal("9.99"), "sale"));

        assertEquals(6, widget.getQuantityOnHand());
        verify(stockTransactionRepository).save(any());
    }

    @Test
    void stockOutBeyondAvailableQuantityThrows() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(widget));

        assertThrows(InsufficientStockException.class,
                () -> inventoryService.stockOut(new StockOutRequest(1L, 100, new BigDecimal("9.99"), "sale")));

        assertEquals(10, widget.getQuantityOnHand());
        verify(stockTransactionRepository, never()).save(any());
    }

    @Test
    void listLowStockMapsRepositoryResultsToResponses() {
        widget.setQuantityOnHand(2);
        when(productRepository.findLowStockOrderByNameAsc()).thenReturn(List.of(widget));

        List<ProductResponse> lowStock = inventoryService.listLowStock();

        assertEquals(1, lowStock.size());
        assertEquals("Widget", lowStock.get(0).getName());
        assertEquals(1L, lowStock.get(0).getStoreId());
    }
}
