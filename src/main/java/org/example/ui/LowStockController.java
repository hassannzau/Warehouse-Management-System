package org.example.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import org.example.client.WarehouseApiClient;
import org.example.response.ProductResponse;

import java.math.BigDecimal;
import java.util.List;

public class LowStockController extends BorderPane {

    private final WarehouseApiClient apiClient;
    private final TableView<ProductResponse> table = new TableView<>();

    public LowStockController(WarehouseApiClient apiClient) {
        this.apiClient = apiClient;
        buildLayout();
    }

    private void buildLayout() {
        setPadding(new Insets(15));

        TableColumn<ProductResponse, String> skuCol = new TableColumn<>("SKU");
        skuCol.setCellValueFactory(new PropertyValueFactory<>("sku"));
        TableColumn<ProductResponse, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<ProductResponse, Number> qtyCol = new TableColumn<>("Qty On Hand");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantityOnHand"));
        TableColumn<ProductResponse, Number> reorderCol = new TableColumn<>("Reorder At");
        reorderCol.setCellValueFactory(new PropertyValueFactory<>("reorderThreshold"));
        TableColumn<ProductResponse, BigDecimal> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        table.getColumns().addAll(List.of(skuCol, nameCol, qtyCol, reorderCol, priceCol));
        setCenter(table);

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refresh());
        setTop(refreshButton);
        BorderPane.setMargin(refreshButton, new Insets(0, 0, 10, 0));
    }

    public void refresh() {
        table.setItems(FXCollections.observableArrayList(apiClient.listLowStock()));
    }
}
