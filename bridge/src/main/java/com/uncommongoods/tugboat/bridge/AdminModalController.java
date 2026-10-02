// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminModalController implements Initializable {

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisible;

    @FXML
    private FontIcon passwordIcon;

    @FXML
    private Button signInButton;

    @FXML
    private Label errorLabel;

    private boolean isPasswordVisible = false;
    private Runnable onClose;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupPasswordField();
        bindPasswordFields();
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    private void setupPasswordField() {
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                enterAdminMode();
            }
        });

        passwordVisible.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                enterAdminMode();
            }
        });
    }

    private void bindPasswordFields() {
        // Keep password fields in sync
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!passwordVisible.isFocused()) {
                passwordVisible.setText(newVal);
            }
        });

        passwordVisible.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!passwordField.isFocused()) {
                passwordField.setText(newVal);
            }
        });
    }

    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            // Show password as plain text
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordVisible.requestFocus();
            passwordIcon.setIconLiteral("mdi2e-eye-off");
        } else {
            // Hide password
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordField.requestFocus();
            passwordIcon.setIconLiteral("mdi2e-eye");
        }
    }

    @FXML
    private void enterAdminMode() {
        String enteredPassword = isPasswordVisible ? passwordVisible.getText() : passwordField.getText();
        String adminPassword = SignInController.getAdminPassword();

        if (enteredPassword.equals(adminPassword)) {
            // Password correct - enter admin mode
            SignInController.setAdminMode(true);
            closeModal();
        } else {
            // Password incorrect - show error
            showError("Incorrect admin password");
        }
    }

    @FXML
    public void cancel() {
        closeModal();
    }

    private void closeModal() {
        if (onClose != null) {
            onClose.run();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().setAll("danger");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        passwordField.clear();
        passwordVisible.clear();

        if (isPasswordVisible) {
            passwordVisible.requestFocus();
        } else {
            passwordField.requestFocus();
        }

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(4), e -> {
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            })
        );
        timeline.play();
    }
}
