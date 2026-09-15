package org.example.service.impl;

import org.example.entity.Category;
import org.example.entity.Product;
import org.example.entity.StockTransaction;
import org.example.entity.Store;
import org.example.enums.TransactionType;
import org.example.repository.CategoryRepository;
import org.example.repository.ProductRepository;
import org.example.repository.StockTransactionRepository;
import org.example.repository.StoreRepository;
import org.example.request.AddProductRequest;
import org.example.request.StockInRequest;
import org.example.request.StockOutRequest;
import org.example.request.UpdateProductRequest;
import org.example.response.ProductResponse;
import org.example.response.StockTransactionResponse;
import org.example.exception.InsufficientStockException;
import org.example.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public InventoryServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
                                 StoreRepository storeRepository, StockTransactionRepository stockTransactionRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.storeRepository = storeRepository;
        this.stockTransactionRepository = stockTransactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts() {
        return productRepository.findAllByOrderByNameAsc().stream().map(ProductResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> listLowStock() {
        return productRepository.findLowStockOrderByNameAsc().stream().map(ProductResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductResponse> findProduct(Long id) {
        return productRepository.findById(id).map(ProductResponse::from);
    }

    @Override
    @Transactional
    public Long addProduct(AddProductRequest request) {
        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setCategory(resolveCategory(request.getCategoryId()));
        product.setStore(resolveStore(request.getStoreId()));
        product.setUnitPrice(request.getUnitPrice());
        product.setQuantityOnHand(request.getQuantityOnHand());
        product.setReorderThreshold(request.getReorderThreshold());
        return productRepository.save(product).getId();
    }

    @Override
    @Transactional
    public void updateProduct(UpdateProductRequest request) {
        Product product = productRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown product: " + request.getId()));
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setCategory(resolveCategory(request.getCategoryId()));
        product.setStore(resolveStore(request.getStoreId()));
        product.setUnitPrice(request.getUnitPrice());
        product.setQuantityOnHand(request.getQuantityOnHand());
        product.setReorderThreshold(request.getReorderThreshold());
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void stockIn(StockInRequest request) {
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown product: " + request.getProductId()));
        product.setQuantityOnHand(product.getQuantityOnHand() + request.getQuantity());
        stockTransactionRepository.save(new StockTransaction(
                product, TransactionType.IN, request.getQuantity(), request.getUnitPrice(), request.getNote()));
    }

    @Override
    @Transactional
    public void stockOut(StockOutRequest request) {
        int quantity = request.getQuantity();
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown product: " + request.getProductId()));
        if (product.getQuantityOnHand() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for " + product.getName() + ": have " + product.getQuantityOnHand()
                            + ", requested " + quantity);
        }
        product.setQuantityOnHand(product.getQuantityOnHand() - quantity);
        stockTransactionRepository.save(new StockTransaction(
                product, TransactionType.OUT, quantity, request.getUnitPrice(), request.getNote()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockTransactionResponse> history(Long productId) {
        return stockTransactionRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(StockTransactionResponse::from)
                .toList();
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown category: " + categoryId));
    }

    private Store resolveStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown store: " + storeId));
    }
}
