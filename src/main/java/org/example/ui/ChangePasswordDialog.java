package org.example.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.client.WarehouseApiClient;
import org.example.exception.ApiException;

//Self-service password change, reachable from the Account menu once logged in.
public final class ChangePasswordDialog {

    private ChangePasswordDialog() {
    }

    public static void show(WarehouseApiClient apiClient) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Change Password");
        stage.setResizable(false);

        PasswordField currentField = new PasswordField();
        currentField.setPromptText("Current password");
        PasswordField newField = new PasswordField();
        newField.setPromptText("New password (min 6 characters)");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm new password");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #b00020;");
        Button changeButton = new Button("Change Password");

        Runnable attempt = () -> {
            String current = currentField.getText();
            String updated = newField.getText();
            String confirm = confirmField.getText();
            if (current.isEmpty() || updated.isEmpty()) {
                errorLabel.setText("Both fields are required");
                return;
            }
            if (!updated.equals(confirm)) {
                errorLabel.setText("New password and confirmation don't match");
                return;
            }
            changeButton.setDisable(true);
            try {
                apiClient.changePassword(current, updated);
                stage.close();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Password changed successfully");
                alert.showAndWait();
            } catch (ApiException e) {
                errorLabel.setText(e.getMessage());
            } finally {
                changeButton.setDisable(false);
            }
        };
        changeButton.setOnAction(e -> attempt.run());
        confirmField.setOnAction(e -> attempt.run());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Current:"), currentField);
        grid.addRow(1, new Label("New:"), newField);
        grid.addRow(2, new Label("Confirm:"), confirmField);
        grid.add(changeButton, 1, 3);
        grid.add(errorLabel, 0, 4, 2, 1);

        stage.setScene(new Scene(grid, 340, 220));
        stage.showAndWait();
    }
}
