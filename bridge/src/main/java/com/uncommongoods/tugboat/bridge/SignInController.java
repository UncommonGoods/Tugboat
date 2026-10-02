// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class SignInController implements Initializable {

    private static final Preferences prefs = Preferences.userNodeForPackage(SignInController.class);
    private static final String SETTINGS_PASSWORD_KEY = "tugboat.settings.password";
    private static final String ADMIN_PASSWORD_KEY = "tugboat.admin.password";
    private static final String DEFAULT_PASSWORD = "Ever-Judge-Union";
    private static final String DEFAULT_ADMIN_PASSWORD = "Cloud-Health-Suit";

    private static final ReadOnlyBooleanWrapper adminMode = new ReadOnlyBooleanWrapper(false);

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
    private VBox parentContentArea;

    // Konami code easter egg
    private final List<KeyCode> konamiCode = Arrays.asList(
        KeyCode.UP, KeyCode.UP, KeyCode.DOWN, KeyCode.DOWN,
        KeyCode.LEFT, KeyCode.RIGHT, KeyCode.LEFT, KeyCode.RIGHT,
        KeyCode.B, KeyCode.A
    );
    private final List<KeyCode> keySequence = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeDefaultPassword();
        setupPasswordField();
        bindPasswordFields();
    }

    public void setParentContentArea(VBox contentArea) {
        this.parentContentArea = contentArea;
    }

    private void initializeDefaultPassword() {
        String existingPassword = prefs.get(SETTINGS_PASSWORD_KEY, null);
        if (existingPassword == null) {
            prefs.put(SETTINGS_PASSWORD_KEY, DEFAULT_PASSWORD);
        }

        String existingAdminPassword = prefs.get(ADMIN_PASSWORD_KEY, null);
        if (existingAdminPassword == null) {
            prefs.put(ADMIN_PASSWORD_KEY, DEFAULT_ADMIN_PASSWORD);
        }
    }

    private void setupPasswordField() {
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (checkKonamiCode(event.getCode())) {
                    resetPasswordToDefault();
                } else {
                    signIn();
                }
            } else {
                checkKonamiCode(event.getCode());
            }
        });

        passwordVisible.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (checkKonamiCode(event.getCode())) {
                    resetPasswordToDefault();
                } else {
                    signIn();
                }
            } else {
                checkKonamiCode(event.getCode());
            }
        });
    }

    private void bindPasswordFields() {
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
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordVisible.requestFocus();
            passwordIcon.setIconLiteral("mdi2e-eye-off");
        } else {
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordField.requestFocus();
            passwordIcon.setIconLiteral("mdi2e-eye");
        }
    }

    @FXML
    private void signIn() {
        String enteredPassword = isPasswordVisible ? passwordVisible.getText() : passwordField.getText();
        String storedPassword = prefs.get(SETTINGS_PASSWORD_KEY, DEFAULT_PASSWORD);

        if (enteredPassword.equals(storedPassword)) {
            loadSettingsScreen();
        } else {
            showError("Incorrect password");
        }
    }

    private void loadSettingsScreen() {
        if (parentContentArea != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
                Node settingsScreen = loader.load();
                parentContentArea.getChildren().clear();
                parentContentArea.getChildren().add(settingsScreen);
            } catch (IOException e) {
                System.err.println("Failed to load settings screen: " + e.getMessage());
                showError("Failed to load settings");
            }
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().setAll("danger"); // Ensure danger styling
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

    private boolean checkKonamiCode(KeyCode keyCode) {
        keySequence.add(keyCode);

        if (keySequence.size() > 11) {
            keySequence.remove(0);
        }

        if (keySequence.size() >= 11) {
            List<KeyCode> lastTen = keySequence.subList(keySequence.size() - 11, keySequence.size() - 1);
            KeyCode lastKey = keySequence.get(keySequence.size() - 1);

            if (lastTen.equals(konamiCode) && lastKey == KeyCode.ENTER) {
                keySequence.clear();
                return true;
            }
        }

        return false;
    }

    private void resetPasswordToDefault() {
        prefs.put(SETTINGS_PASSWORD_KEY, DEFAULT_PASSWORD);

        passwordField.clear();
        passwordVisible.clear();

        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        errorLabel.setText("Password reset to default");
        errorLabel.getStyleClass().setAll("success"); // Use success styling instead of danger
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> {
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
                errorLabel.getStyleClass().setAll("danger"); // Reset to danger styling
            })
        );
        timeline.play();

        System.out.println("Konami code activated - password reset to default");
    }

    public static boolean isAdminMode() {
        return adminMode.get();
    }

    public static void setAdminMode(boolean mode) {
        adminMode.set(mode);
    }

    public static ReadOnlyBooleanProperty adminModeProperty() {
        return adminMode.getReadOnlyProperty();
    }

    public static String getAdminPassword() {
        return prefs.get(ADMIN_PASSWORD_KEY, DEFAULT_ADMIN_PASSWORD);
    }

    public static void setAdminPassword(String password) {
        prefs.put(ADMIN_PASSWORD_KEY, password);
    }

    public static void setSettingsPassword(String password) {
        prefs.put(SETTINGS_PASSWORD_KEY, password);
    }
}
