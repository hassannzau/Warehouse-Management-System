package org.example.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.example.client.LowStockMonitor;
import org.example.client.WarehouseApiClient;
import org.example.response.ProductResponse;
import org.example.response.StoreResponse;

import java.util.List;
import java.util.stream.Collectors;

public class WarehouseDesktopApp extends Application {

    private LowStockMonitor lowStockMonitor;

    @Override
    public void start(Stage primaryStage) {
        WarehouseApiClient apiClient = new WarehouseApiClient();
        if (!LoginDialog.showAndAuthenticate(apiClient)) {
            Platform.exit();
            return;
        }

        StoreResponse store = apiClient.getDefaultStore();

        LowStockController lowStockController = new LowStockController(apiClient);
        DashboardController dashboardController = new DashboardController(apiClient, store);
        ProductsController productsController = new ProductsController(apiClient, store,
                apiClient.getCurrentRole(), dashboardController::refresh);
        StockTransactionController stockTransactionController = new StockTransactionController(apiClient,
                () -> { dashboardController.refresh(); lowStockController.refresh(); });
        BillingController billingController = new BillingController(apiClient, store,
                () -> { dashboardController.refresh(); lowStockController.refresh(); });
        ReportsController reportsController = new ReportsController(apiClient, store);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                new Tab("Dashboard", dashboardController),
                new Tab("Products", productsController),
                new Tab("Stock In/Out", stockTransactionController),
                new Tab("Billing", billingController),
                new Tab("Reports", reportsController),
                new Tab("Low Stock", lowStockController)
        );
        if ("ADMIN".equals(apiClient.getCurrentRole())) {
            tabPane.getTabs().add(new Tab("Users", new UsersController(apiClient)));
        }
        tabPane.getTabs().forEach(tab -> tab.setClosable(false));

        dashboardController.refresh();
        lowStockController.refresh();

        int intervalMinutes = Integer.parseInt(System.getProperty("lowstock.check.interval.minutes", "30"));
        lowStockMonitor = new LowStockMonitor(apiClient, intervalMinutes);
        lowStockMonitor.start(this::onLowStockDetected);

        MenuItem changePasswordItem = new MenuItem("Change Password");
        changePasswordItem.setOnAction(e -> ChangePasswordDialog.show(apiClient));
        Menu accountMenu = new Menu("Account", null, changePasswordItem);
        MenuBar menuBar = new MenuBar(accountMenu);
        menuBar.setUseSystemMenuBar(true);

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(tabPane);

        primaryStage.setTitle("Warehouse Management System");
        primaryStage.setScene(new Scene(root, 1000, 650));
        primaryStage.show();
    }

    private void onLowStockDetected(List<ProductResponse> lowStockProducts) {
        Platform.runLater(() -> {
            String names = lowStockProducts.stream().map(ProductResponse::getName).collect(Collectors.joining(", "));
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Low Stock Alert");
            alert.setHeaderText(lowStockProducts.size() + " product(s) at or below reorder threshold");
            alert.setContentText(names);
            alert.show();
        });
    }

    @Override
    public void stop() {
        if (lowStockMonitor != null) {
            lowStockMonitor.stop();
        }
    }
}
