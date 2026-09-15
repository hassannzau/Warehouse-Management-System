package org.example.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.client.WarehouseApiClient;
import org.example.exception.ApiException;

/*
 * Blocking login screen shown before the main window. The app has no anonymous
 * access, so nothing else builds until this succeeds.*/
public final class LoginDialog {

    private LoginDialog() {
    }

    // @return true once login succeeds; false if the user closes the window instead.
    public static boolean showAndAuthenticate(WarehouseApiClient apiClient) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Warehouse Management System — Login");
        stage.setResizable(false);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #b00020;");
        Button loginButton = new Button("Login");

        boolean[] success = {false};

        Runnable attempt = () -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Enter username and password");
                return;
            }
            loginButton.setDisable(true);
            try {
                apiClient.login(username, password);
                success[0] = true;
                stage.close();
            } catch (ApiException e) {
                errorLabel.setText(e.getMessage());
                passwordField.clear();
            } finally {
                loginButton.setDisable(false);
            }
        };
        loginButton.setOnAction(e -> attempt.run());
        passwordField.setOnAction(e -> attempt.run());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Username:"), usernameField);
        grid.addRow(1, new Label("Password:"), passwordField);
        grid.add(loginButton, 1, 2);
        grid.add(errorLabel, 0, 3, 2, 1);

        stage.setScene(new Scene(grid, 320, 190));
        stage.showAndWait();
        return success[0];
    }
}
