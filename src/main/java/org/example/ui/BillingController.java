package org.example.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.example.exception.ApiException;
import org.example.client.CartItem;
import org.example.client.WarehouseApiClient;
import org.example.request.CreateInvoiceRequest;
import org.example.response.InvoiceResponse;
import org.example.response.ProductResponse;
import org.example.response.StoreResponse;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BillingController extends BorderPane {

    private final WarehouseApiClient apiClient;
    private final StoreResponse store;
    private final Runnable onChange;

    private final ComboBox<ProductResponse> productCombo = new ComboBox<>();
    private final Spinner<Integer> quantitySpinner = new Spinner<>(1, 1000, 1);
    private final TableView<CartItem> cartTable = new TableView<>();
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private final Label totalLabel = new Label("Total: $0.00");

    public BillingController(WarehouseApiClient apiClient, StoreResponse store, Runnable onChange) {
        this.apiClient = apiClient;
        this.store = store;
        this.onChange = onChange;
        buildLayout();
        reloadProducts();
    }

    private void buildLayout() {
        setPadding(new Insets(15));

        productCombo.setPromptText("Select product");
        quantitySpinner.setEditable(true);
        Button addButton = new Button("Add to Cart");
        addButton.setOnAction(e -> addToCart());
        HBox addRow = new HBox(10, productCombo, quantitySpinner, addButton);
        setTop(addRow);

        TableColumn<CartItem, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        TableColumn<CartItem, Number> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<CartItem, BigDecimal> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        TableColumn<CartItem, BigDecimal> lineTotalCol = new TableColumn<>("Line Total");
        lineTotalCol.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));
        cartTable.getColumns().addAll(List.of(nameCol, qtyCol, priceCol, lineTotalCol));
        cartTable.setItems(cartItems);
        setCenter(cartTable);

        Button removeButton = new Button("Remove Selected");
        removeButton.setOnAction(e -> removeSelected());
        Button clearButton = new Button("Clear Cart");
        clearButton.setOnAction(e -> clearCart());
        Button completeButton = new Button("Complete Sale");
        completeButton.setOnAction(e -> completeSale());

        totalLabel.setFont(Font.font(16));
        HBox actionsRow = new HBox(10, removeButton, clearButton, completeButton);
        VBox bottomBox = new VBox(10, totalLabel, actionsRow);
        bottomBox.setPadding(new Insets(15, 0, 0, 0));
        setBottom(bottomBox);
    }

    private void reloadProducts() {
        productCombo.setItems(FXCollections.observableArrayList(apiClient.listProducts()));
    }

    private void addToCart() {
        ProductResponse product = productCombo.getValue();
        if (product == null) {
            showError("Select a product");
            return;
        }
        int quantity = quantitySpinner.getValue();
        cartItems.add(new CartItem(product.getId(), product.getName(), quantity, product.getUnitPrice()));
        updateTotal();
    }

    private void removeSelected() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartItems.remove(selected);
            updateTotal();
        }
    }

    private void clearCart() {
        cartItems.clear();
        updateTotal();
    }

    private void updateTotal() {
        BigDecimal total = cartItems.stream().map(CartItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLabel.setText("Total: $" + total.setScale(2, java.math.RoundingMode.HALF_UP));
    }

    private void completeSale() {
        if (cartItems.isEmpty()) {
            showError("Cart is empty");
            return;
        }
        Map<Long, Integer> lineItems = new LinkedHashMap<>();
        for (CartItem item : cartItems) {
            lineItems.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        try {
            InvoiceResponse invoice = apiClient.createInvoice(new CreateInvoiceRequest(store.getId(), lineItems));
            showReceipt(invoice);
            clearCart();
            reloadProducts();
            onChange.run();
        } catch (ApiException e) {
            showError(e.getMessage());
        }
    }

    private void showReceipt(InvoiceResponse invoice) {
        String itemLines = invoice.getItems().stream()
                .map(i -> i.getProductName() + " x" + i.getQuantity() + " @ $" + i.getUnitPrice() + " = $" + i.getLineTotal())
                .collect(Collectors.joining("\n"));
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sale Complete");
        alert.setHeaderText("Invoice " + invoice.getInvoiceNumber() + " — Total: $" + invoice.getTotalAmount());
        alert.setContentText(itemLines);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}
