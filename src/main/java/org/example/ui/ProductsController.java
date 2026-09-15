package org.example.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.example.exception.ApiException;
import org.example.client.WarehouseApiClient;
import org.example.request.AddProductRequest;
import org.example.request.UpdateProductRequest;
import org.example.response.CategoryResponse;
import org.example.response.ProductResponse;
import org.example.response.StoreResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductsController extends BorderPane {

    private final WarehouseApiClient apiClient;
    private final StoreResponse store;
    private final String role;
    private final Runnable onChange;

    private final TableView<ProductResponse> table = new TableView<>();
    private final ObservableList<ProductResponse> products = FXCollections.observableArrayList();

    private final TextField skuField = new TextField();
    private final TextField nameField = new TextField();
    private final ComboBox<CategoryResponse> categoryCombo = new ComboBox<>();
    private final TextField newCategoryField = new TextField();
    private final TextField unitPriceField = new TextField();
    private final TextField quantityField = new TextField();
    private final TextField reorderField = new TextField();

    private ProductResponse selectedProduct;

    public ProductsController(WarehouseApiClient apiClient, StoreResponse store, String role, Runnable onChange) {
        this.apiClient = apiClient;
        this.store = store;
        this.role = role;
        this.onChange = onChange;
        buildLayout();
        reloadCategories();
        reloadProducts();
    }

    // Mirrors the @PreAuthorize rules on ProductController/CategoryController - disabling
    // here is a UX nicety, the API is what actually enforces this.
    private boolean canWriteCatalog() {
        return "ADMIN".equals(role) || "MANAGER".equals(role);
    }

    private boolean canDelete() {
        return "ADMIN".equals(role);
    }

    @SuppressWarnings("unchecked")
    private void buildLayout() {
        setPadding(new Insets(15));

        TableColumn<ProductResponse, String> skuCol = new TableColumn<>("SKU");
        skuCol.setCellValueFactory(new PropertyValueFactory<>("sku"));
        TableColumn<ProductResponse, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<ProductResponse, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        TableColumn<ProductResponse, BigDecimal> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        TableColumn<ProductResponse, Number> qtyCol = new TableColumn<>("Qty On Hand");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantityOnHand"));
        TableColumn<ProductResponse, Number> reorderCol = new TableColumn<>("Reorder At");
        reorderCol.setCellValueFactory(new PropertyValueFactory<>("reorderThreshold"));

        table.getColumns().addAll(List.of(skuCol, nameCol, categoryCol, priceCol, qtyCol, reorderCol));
        table.setItems(products);
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> populateForm(selected));
        setCenter(table);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(15, 0, 0, 0));

        int row = 0;
        form.addRow(row++, new Label("SKU:"), skuField);
        form.addRow(row++, new Label("Name:"), nameField);
        categoryCombo.setPromptText("Select category");
        newCategoryField.setPromptText("Or type a new category name");
        Button addCategoryButton = new Button("Add Category");
        addCategoryButton.setOnAction(e -> addNewCategory());
        addCategoryButton.setDisable(!canWriteCatalog());
        HBox categoryBox = new HBox(8, categoryCombo, newCategoryField, addCategoryButton);
        form.addRow(row++, new Label("Category:"), categoryBox);
        form.addRow(row++, new Label("Unit Price:"), unitPriceField);
        form.addRow(row++, new Label("Quantity On Hand:"), quantityField);
        form.addRow(row++, new Label("Reorder Threshold:"), reorderField);

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> addProduct());
        addButton.setDisable(!canWriteCatalog());
        Button updateButton = new Button("Update Selected");
        updateButton.setOnAction(e -> updateProduct());
        updateButton.setDisable(!canWriteCatalog());
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> deleteProduct());
        deleteButton.setDisable(!canDelete());
        Button clearButton = new Button("Clear Form");
        clearButton.setOnAction(e -> clearForm());

        HBox buttonBox = new HBox(10, addButton, updateButton, deleteButton, clearButton);
        form.addRow(row, buttonBox);

        setBottom(form);
    }

    private void reloadCategories() {
        CategoryResponse previousSelection = categoryCombo.getValue();
        categoryCombo.setItems(FXCollections.observableArrayList(apiClient.listCategories()));
        if (previousSelection != null) {
            categoryCombo.getItems().stream()
                    .filter(c -> c.getId().equals(previousSelection.getId()))
                    .findFirst()
                    .ifPresent(categoryCombo::setValue);
        }
    }

    private void addNewCategory() {
        String name = newCategoryField.getText().trim();
        if (name.isEmpty()) {
            showError("Enter a category name first");
            return;
        }
        apiClient.addCategory(name);
        newCategoryField.clear();
        reloadCategories();
    }

    private void reloadProducts() {
        products.setAll(apiClient.listProducts());
    }

    private void populateForm(ProductResponse product) {
        selectedProduct = product;
        if (product == null) {
            return;
        }
        skuField.setText(product.getSku());
        nameField.setText(product.getName());
        unitPriceField.setText(product.getUnitPrice().toPlainString());
        quantityField.setText(String.valueOf(product.getQuantityOnHand()));
        reorderField.setText(String.valueOf(product.getReorderThreshold()));
        if (product.getCategoryId() != null) {
            categoryCombo.getItems().stream()
                    .filter(c -> c.getId().equals(product.getCategoryId()))
                    .findFirst()
                    .ifPresent(categoryCombo::setValue);
        } else {
            categoryCombo.setValue(null);
        }
    }

    private void clearForm() {
        selectedProduct = null;
        table.getSelectionModel().clearSelection();
        skuField.clear();
        nameField.clear();
        categoryCombo.setValue(null);
        unitPriceField.clear();
        quantityField.clear();
        reorderField.clear();
    }

    private void addProduct() {
        parseForm().ifPresent(form -> {
            try {
                apiClient.addProduct(new AddProductRequest(
                        form.sku(), form.name(), form.categoryId(), store.getId(),
                        form.unitPrice(), form.quantity(), form.reorder()));
                reloadProducts();
                clearForm();
                onChange.run();
            } catch (ApiException e) {
                showError(e.getMessage());
            }
        });
    }

    private void updateProduct() {
        if (selectedProduct == null) {
            showError("Select a product to update");
            return;
        }
        parseForm().ifPresent(form -> {
            try {
                apiClient.updateProduct(new UpdateProductRequest(
                        selectedProduct.getId(), form.sku(), form.name(), form.categoryId(), selectedProduct.getStoreId(),
                        form.unitPrice(), form.quantity(), form.reorder()));
                reloadProducts();
                clearForm();
                onChange.run();
            } catch (ApiException e) {
                showError(e.getMessage());
            }
        });
    }

    private void deleteProduct() {
        if (selectedProduct == null) {
            showError("Select a product to delete");
            return;
        }
        try {
            apiClient.deleteProduct(selectedProduct.getId());
            reloadProducts();
            clearForm();
            onChange.run();
        } catch (ApiException e) {
            showError(e.getMessage());
        }
    }

    private record ProductFormData(String sku, String name, Long categoryId, BigDecimal unitPrice,
                                    int quantity, int reorder) {
    }

    private Optional<ProductFormData> parseForm() {
        try {
            String sku = skuField.getText().trim();
            String name = nameField.getText().trim();
            if (sku.isEmpty() || name.isEmpty()) {
                showError("SKU and Name are required");
                return Optional.empty();
            }
            BigDecimal unitPrice = new BigDecimal(unitPriceField.getText().trim());
            int quantity = Integer.parseInt(quantityField.getText().trim());
            int reorder = Integer.parseInt(reorderField.getText().trim());
            CategoryResponse category = categoryCombo.getValue();

            return Optional.of(new ProductFormData(sku, name, category != null ? category.getId() : null,
                    unitPrice, quantity, reorder));
        } catch (NumberFormatException e) {
            showError("Unit Price, Quantity and Reorder Threshold must be valid numbers");
            return Optional.empty();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}
