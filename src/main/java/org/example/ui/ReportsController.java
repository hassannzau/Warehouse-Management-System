package org.example.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.example.client.WarehouseApiClient;
import org.example.request.MonthlyReportRequest;
import org.example.response.MonthlySalesReportResponse;
import org.example.response.ProductSalesLineResponse;
import org.example.response.StoreResponse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReportsController extends BorderPane {

    private final WarehouseApiClient apiClient;
    private final StoreResponse store;

    private final ComboBox<Integer> yearCombo = new ComboBox<>();
    private final ComboBox<Month> monthCombo = new ComboBox<>();
    private final Label totalSalesLabel = new Label("Total Sales: $0.00");
    private final Label invoiceCountLabel = new Label("Invoices: 0");
    private final TableView<ProductSalesLineResponse> breakdownTable = new TableView<>();

    private MonthlySalesReportResponse currentReport;

    public ReportsController(WarehouseApiClient apiClient, StoreResponse store) {
        this.apiClient = apiClient;
        this.store = store;
        buildLayout();
    }

    private void buildLayout() {
        setPadding(new Insets(15));

        int currentYear = LocalDate.now().getYear();
        yearCombo.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(currentYear - 5, currentYear).boxed().collect(Collectors.toList())));
        yearCombo.setValue(currentYear);

        monthCombo.setItems(FXCollections.observableArrayList(Month.values()));
        monthCombo.setValue(LocalDate.now().getMonth());

        Button generateButton = new Button("Generate Report");
        generateButton.setOnAction(e -> generateReport());
        Button exportButton = new Button("Export CSV");
        exportButton.setOnAction(e -> exportCsv());

        HBox controls = new HBox(10, new Label("Year:"), yearCombo, new Label("Month:"), monthCombo,
                generateButton, exportButton);
        setTop(controls);

        TableColumn<ProductSalesLineResponse, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        TableColumn<ProductSalesLineResponse, Number> qtyCol = new TableColumn<>("Quantity Sold");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
        TableColumn<ProductSalesLineResponse, BigDecimal> amountCol = new TableColumn<>("Total Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        breakdownTable.getColumns().addAll(List.of(nameCol, qtyCol, amountCol));

        totalSalesLabel.setFont(Font.font(15));
        invoiceCountLabel.setFont(Font.font(15));
        VBox summaryBox = new VBox(5, totalSalesLabel, invoiceCountLabel);
        summaryBox.setPadding(new Insets(15, 0, 10, 0));

        VBox centerBox = new VBox(10, summaryBox, breakdownTable);
        setCenter(centerBox);
    }

    private void generateReport() {
        Integer year = yearCombo.getValue();
        Month month = monthCombo.getValue();
        if (year == null || month == null) {
            return;
        }
        currentReport = apiClient.monthlySummary(new MonthlyReportRequest(store.getId(), YearMonth.of(year, month.getValue())));
        totalSalesLabel.setText("Total Sales: $" + currentReport.getTotalSales().setScale(2, java.math.RoundingMode.HALF_UP));
        invoiceCountLabel.setText("Invoices: " + currentReport.getInvoiceCount());
        breakdownTable.setItems(FXCollections.observableArrayList(currentReport.getProductBreakdown()));
    }

    private void exportCsv() {
        if (currentReport == null) {
            showError("Generate a report first");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("sales-report-" + currentReport.getMonth() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file == null) {
            return;
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Product,Quantity Sold,Total Amount\n");
            for (ProductSalesLineResponse line : currentReport.getProductBreakdown()) {
                writer.write(escapeCsv(line.getProductName()) + "," + line.getQuantitySold() + "," + line.getTotalAmount() + "\n");
            }
            writer.write("\nTotal Sales," + currentReport.getTotalSales() + "\n");
            writer.write("Invoice Count," + currentReport.getInvoiceCount() + "\n");
        } catch (IOException e) {
            showError("Failed to write CSV: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}
