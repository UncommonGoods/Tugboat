// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import com.uncommongoods.tugboat.bridge.util.ModalOverlay;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Button hamburgerButton;

    @FXML
    private VBox leftNavigation;

    @FXML
    private VBox contentArea;

    @FXML
    private HBox topBar;

    @FXML
    private Button adminButton;

    @FXML
    private FontIcon adminIcon;

    @FXML
    private Button pickupsButton;

    @FXML
    private Button setOriginButton;

    @FXML
    private Button devModeButton;

    @FXML
    private Button exitDevModeButton;

    @FXML
    private javafx.scene.control.Label devIndicatorLabel;

    private boolean isNavOpen = false;
    private static final double NAV_WIDTH = 300.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            ConfigurationManager configManager = ConfigurationManager.getInstance();
            configManager.initializeConfigurations();
            System.out.println("Configuration manager initialized on application startup");
        } catch (Exception e) {
            System.err.println("Failed to initialize configurations on application startup: " + e.getMessage());
        }

        // Initially hide the nav
        leftNavigation.setTranslateX(-NAV_WIDTH);

        // close nav when clicking outside
        contentArea.setOnMouseClicked(event -> {
            if (isNavOpen) {
                toggleNavigation();
            }
        });

        updateAdminUI();
        updateDevModeUI();

        /*Keep the nav in sync with state other screens can change — the Settings
        page's environment toggle turns admin mode on and switches environments.
        This controller lives as long as the app, so plain listeners are fine.*/
        SignInController.adminModeProperty().addListener((obs, was, isAdmin) -> updateAdminUI());
        ConfigurationManager.getInstance().devEnvironmentProperty()
            .addListener((obs, was, isDev) -> updateDevModeUI());
        /*Saving the Settings page's cache fields rebuilds the environment, which
         swaps the origin flow: Set Origin for a cacheless station, Pickups once
         there is a cache to hold a facility. */
        ConfigurationManager.getInstance().cacheConfiguredProperty()
            .addListener((obs, was, hasCache) -> updateCacheDependentNav());

        // Load shipping screen as default
        loadShippingScreen();
    }

    @FXML
    private void toggleNavigation() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(200), leftNavigation);

        if (isNavOpen) {
            transition.setToX(-NAV_WIDTH);
            isNavOpen = false;
        } else {
            transition.setToX(0);
            isNavOpen = true;
        }

        transition.play();
    }

    @FXML
    private void showShipping() {
        loadShippingScreen();
    }

    private void loadShippingScreen() {
        loadScreen("/fxml/shipping.fxml");
    }

    @FXML
    private void showSetOrigin() {
        loadScreen("/fxml/set-origin.fxml");
    }

    @FXML
    private void showHistory() {
        loadScreen("/fxml/history.fxml");
    }

    @FXML
    private void showPickups() {
        loadScreen("/fxml/pickups.fxml");
    }

    @FXML
    private void showSettings() {
        loadSignInScreen();
    }

    private void loadSignInScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signin.fxml"));
            Node signInScreen = loader.load();

            SignInController signInController = loader.getController();
            signInController.setParentContentArea(contentArea);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(signInScreen);

        } catch (IOException e) {
            System.err.println("Error loading sign-in screen");
            e.printStackTrace();
        }
    }

    private void loadScreen(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node screen = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(screen);

        } catch (IOException e) {
            System.err.println("Error loading screen: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    private void showAdminModal() {
        if (SignInController.isAdminMode()) {
            exitAdminMode();
        } else {
            showAdminLoginModal();
        }
    }

    private void showAdminLoginModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-modal.fxml"));
            Node modalContent = loader.load();

            AdminModalController modalController = loader.getController();

            // Shown last so a failure during setup leaves no orphaned overlay.
            modalController.setOnClose(ModalOverlay.show(adminButton, modalContent));

        } catch (IOException e) {
            System.err.println("Error loading admin modal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exitAdminMode() {
        SignInController.setAdminMode(false);
    }

    @FXML
    private void enterDevMode() {
        ConfigurationManager.getInstance().setEnvironment(true);
        System.out.println("Entered DEV mode");
    }

    @FXML
    private void exitDevMode() {
        ConfigurationManager.getInstance().setEnvironment(false);
        System.out.println("Exited DEV mode");
    }

    private void updateDevModeUI() {
        boolean isAdmin = SignInController.isAdminMode();
        boolean isDev = ConfigurationManager.getInstance().isDevEnvironment();

        devIndicatorLabel.setVisible(isDev);
        devIndicatorLabel.setManaged(isDev);

        devModeButton.setVisible(isAdmin && !isDev);
        devModeButton.setManaged(isAdmin && !isDev);

        exitDevModeButton.setVisible(isAdmin && isDev);
        exitDevModeButton.setManaged(isAdmin && isDev);
    }

    private void updateAdminUI() {
        if (SignInController.isAdminMode()) {
            adminIcon.setIconLiteral("mdi2l-lock-open");
            adminButton.setText("Exit Admin");

            topBar.setStyle("-fx-background-color: -color-accent-emphasis;");
        } else {
            adminIcon.setIconLiteral("mdi2l-lock");
            adminButton.setText("Admin");

            topBar.setStyle("");
        }

        updateCacheDependentNav();
        updateDevModeUI();
    }

    /**
     * Show whichever origin flow the active environment supports. Pickup
     * facilities live in the cache, so without one the station sets a plain
     * origin address instead; Pickups additionally stays admin-only.
     */
    private void updateCacheDependentNav() {
        boolean hasCache = ConfigurationManager.getInstance().isCacheConfigured();

        setOriginButton.setVisible(!hasCache);
        setOriginButton.setManaged(!hasCache);

        boolean showPickups = hasCache && SignInController.isAdminMode();
        pickupsButton.setVisible(showPickups);
        pickupsButton.setManaged(showPickups);
    }
}
