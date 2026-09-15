package org.example.controller;

import org.example.request.StockInRequest;
import org.example.request.StockOutRequest;
import org.example.response.StockTransactionResponse;
import org.example.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock-transactions")
public class StockTransactionController {

    private final InventoryService inventoryService;

    public StockTransactionController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/in")
    public ResponseEntity<Void> stockIn(@RequestBody StockInRequest request) {
        inventoryService.stockIn(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/out")
    public ResponseEntity<Void> stockOut(@RequestBody StockOutRequest request) {
        inventoryService.stockOut(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/product/{productId}")
    public List<StockTransactionResponse> history(@PathVariable Long productId) {
        return inventoryService.history(productId);
    }
}
