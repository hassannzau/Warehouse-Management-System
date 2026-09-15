package org.example.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.example.client.WarehouseApiClient;
import org.example.exception.ApiException;
import org.example.response.UserResponse;

import java.util.List;

/*
 * Admin-only screen for creating accounts - the app has no self-registration, so
 * this is the only way anyone besides the seeded default admin gets a login.
 * {@link org.example.ui.WarehouseDesktopApp} only adds this tab when the logged-in
 * user's role is ADMIN.
 */
public class UsersController extends BorderPane {

    private final WarehouseApiClient apiClient;

    private final TableView<UserResponse> table = new TableView<>();
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "MANAGER", "STAFF"));

    public UsersController(WarehouseApiClient apiClient) {
        this.apiClient = apiClient;
        buildLayout();
        reloadUsers();
    }

    private void buildLayout() {
        setPadding(new Insets(15));

        TableColumn<UserResponse, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        TableColumn<UserResponse, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        table.getColumns().addAll(List.of(usernameCol, roleCol));
        setCenter(table);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(15, 0, 0, 0));

        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password (min 6 characters)");
        roleCombo.setPromptText("Role");
        Button createButton = new Button("Create User");
        createButton.setOnAction(e -> createUser());

        form.addRow(0, new Label("Username:"), usernameField);
        form.addRow(1, new Label("Password:"), passwordField);
        form.addRow(2, new Label("Role:"), roleCombo);
        form.add(createButton, 1, 3);

        setBottom(form);
    }

    private void reloadUsers() {
        table.setItems(FXCollections.observableArrayList(apiClient.listUsers()));
    }

    private void createUser() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String role = roleCombo.getValue();
        if (username.isEmpty() || password.isEmpty() || role == null) {
            showError("Username, password and role are all required");
            return;
        }
        try {
            apiClient.createUser(username, password, role);
            usernameField.clear();
            passwordField.clear();
            roleCombo.setValue(null);
            reloadUsers();
        } catch (ApiException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}
