package org.example.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.example.exception.ApiException;
import org.example.client.WarehouseApiClient;
import org.example.request.StockInRequest;
import org.example.request.StockOutRequest;
import org.example.response.ProductResponse;
import org.example.response.StockTransactionResponse;

import java.math.BigDecimal;
import java.util.List;

public class StockTransactionController extends BorderPane {

    private final WarehouseApiClient apiClient;
    private final Runnable onChange;

    private final ComboBox<ProductResponse> productCombo = new ComboBox<>();
    private final ToggleGroup typeGroup = new ToggleGroup();
    private final RadioButton inButton = new RadioButton("Stock IN");
    private final RadioButton outButton = new RadioButton("Stock OUT");
    private final TextField quantityField = new TextField();
    private final TextField unitPriceField = new TextField();
    private final TextArea noteField = new TextArea();
    private final TableView<StockTransactionResponse> historyTable = new TableView<>();

    public StockTransactionController(WarehouseApiClient apiClient, Runnable onChange) {
        this.apiClient = apiClient;
        this.onChange = onChange;
        buildLayout();
        reloadProducts();
    }

    private void buildLayout() {
        setPadding(new Insets(15));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);

        inButton.setToggleGroup(typeGroup);
        outButton.setToggleGroup(typeGroup);
        inButton.setSelected(true);

        int row = 0;
        form.addRow(row++, new Label("Product:"), productCombo);
        form.addRow(row++, new Label("Type:"), new HBox(15, inButton, outButton));
        form.addRow(row++, new Label("Quantity:"), quantityField);
        form.addRow(row++, new Label("Unit Price:"), unitPriceField);
        noteField.setPrefRowCount(2);
        form.addRow(row++, new Label("Note:"), noteField);

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> submit());
        form.addRow(row, submitButton);

        productCombo.valueProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                unitPriceField.setText(selected.getUnitPrice().toPlainString());
                refreshHistory(selected.getId());
            }
        });

        TableColumn<StockTransactionResponse, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<StockTransactionResponse, Number> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<StockTransactionResponse, BigDecimal> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        TableColumn<StockTransactionResponse, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        TableColumn<StockTransactionResponse, String> dateCol = new TableColumn<>("When");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        historyTable.getColumns().addAll(List.of(typeCol, qtyCol, priceCol, noteCol, dateCol));

        setTop(form);
        setCenter(historyTable);
        BorderPane.setMargin(historyTable, new Insets(15, 0, 0, 0));
    }

    private void reloadProducts() {
        productCombo.setItems(FXCollections.observableArrayList(apiClient.listProducts()));
    }

    private void refreshHistory(Long productId) {
        historyTable.setItems(FXCollections.observableArrayList(apiClient.history(productId)));
    }

    private void submit() {
        ProductResponse product = productCombo.getValue();
        if (product == null) {
            showError("Select a product");
            return;
        }
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            BigDecimal unitPrice = new BigDecimal(unitPriceField.getText().trim());
            String note = noteField.getText().trim();

            Toggle selectedToggle = typeGroup.getSelectedToggle();
            if (selectedToggle == inButton) {
                apiClient.stockIn(new StockInRequest(product.getId(), quantity, unitPrice, note));
            } else {
                apiClient.stockOut(new StockOutRequest(product.getId(), quantity, unitPrice, note));
            }

            reloadProducts();
            refreshHistory(product.getId());
            quantityField.clear();
            noteField.clear();
            onChange.run();
        } catch (NumberFormatException e) {
            showError("Quantity and Unit Price must be valid numbers");
        } catch (ApiException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}
