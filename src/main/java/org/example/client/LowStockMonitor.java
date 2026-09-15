package org.example.client;

import org.example.response.ProductResponse;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/*
 * Periodically polls the REST API for products at or below their reorder threshold
 * and notifies a listener (the UI) so it can surface an alert without the user having
 * to manually check the Low Stock screen */
public class LowStockMonitor {

    private final WarehouseApiClient apiClient;
    private final int intervalMinutes;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "low-stock-monitor");
        t.setDaemon(true);
        return t;
    });

    public LowStockMonitor(WarehouseApiClient apiClient, int intervalMinutes) {
        this.apiClient = apiClient;
        this.intervalMinutes = intervalMinutes;
    }

    public void start(Consumer<List<ProductResponse>> onLowStockFound) {
        executor.scheduleAtFixedRate(() -> {
            try {
                List<ProductResponse> lowStock = apiClient.listLowStock();
                if (!lowStock.isEmpty()) {
                    onLowStockFound.accept(lowStock);
                }
            } catch (Exception e) {
                // A transient failure (e.g. the API being briefly unreachable) shouldn't
                // kill the recurring check.
                e.printStackTrace();
            }
        }, intervalMinutes, intervalMinutes, TimeUnit.MINUTES);
    }

    public void stop() {
        executor.shutdownNow();
    }
}
