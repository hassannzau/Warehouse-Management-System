package org.example.service;

import org.example.request.AddProductRequest;
import org.example.request.StockInRequest;
import org.example.request.StockOutRequest;
import org.example.request.UpdateProductRequest;
import org.example.response.ProductResponse;
import org.example.response.StockTransactionResponse;

import java.util.List;
import java.util.Optional;

public interface InventoryService {

    List<ProductResponse> listProducts();

    List<ProductResponse> listLowStock();

    Optional<ProductResponse> findProduct(Long id);

    Long addProduct(AddProductRequest request);

    void updateProduct(UpdateProductRequest request);

    void deleteProduct(Long id);

    void stockIn(StockInRequest request);

    void stockOut(StockOutRequest request);

    List<StockTransactionResponse> history(Long productId);
}
