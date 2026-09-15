package org.example.service.impl;

import org.example.repository.StoreRepository;
import org.example.response.StoreResponse;
import org.example.service.StoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    public StoreServiceImpl(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponse getDefaultStore() {
        return storeRepository.findAll().stream()
                .min(java.util.Comparator.comparing(store -> store.getId()))
                .map(StoreResponse::from)
                .orElseThrow(() -> new IllegalStateException("No store found - has the database been seeded?"));
    }
}
