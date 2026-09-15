package org.example.controller;

import org.example.response.StoreResponse;
import org.example.service.StoreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/default")
    public StoreResponse getDefaultStore() {
        return storeService.getDefaultStore();
    }
}
