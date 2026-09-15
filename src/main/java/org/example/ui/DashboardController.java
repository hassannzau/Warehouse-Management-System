package org.example.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import org.example.client.WarehouseApiClient;
import org.example.response.InvoiceResponse;
import org.example.response.StoreResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public class DashboardController extends GridPane {

    private final WarehouseApiClient apiClient;
    private final StoreResponse store;

    private final Label totalProductsLabel = new Label();
    private final Label lowStockLabel = new Label();
    private final Label todaysSalesLabel = new Label();

    public DashboardController(WarehouseApiClient apiClient, StoreResponse store) {
        this.apiClient = apiClient;
        this.store = store;
        buildLayout();
    }

    private void buildLayout() {
        setPadding(new Insets(20));
        setHgap(15);
        setVgap(15);

        Label title = new Label("Dashboard — " + store.getName());
        title.setFont(Font.font(18));
        add(title, 0, 0, 2, 1);

        addStatRow(1, "Total Products:", totalProductsLabel);
        addStatRow(2, "Low Stock Items:", lowStockLabel);
        addStatRow(3, "Today's Sales:", todaysSalesLabel);

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refresh());
        add(refreshButton, 0, 4);
    }

    private void addStatRow(int row, String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.setFont(Font.font(14));
        valueLabel.setFont(Font.font(14));
        add(label, 0, row);
        add(valueLabel, 1, row);
    }

    public void refresh() {
        totalProductsLabel.setText(String.valueOf(apiClient.listProducts().size()));
        lowStockLabel.setText(String.valueOf(apiClient.listLowStock().size()));
        todaysSalesLabel.setText(formatCurrency(computeTodaysSales()));
    }

    private BigDecimal computeTodaysSales() {
        LocalDate today = LocalDate.now();
        return apiClient.listInvoices(store.getId(), YearMonth.from(today)).stream()
                .filter(invoice -> invoice.getCreatedAt().toLocalDate().equals(today))
                .map(InvoiceResponse::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String formatCurrency(BigDecimal amount) {
        return "$" + amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
