// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import atlantafx.base.theme.NordLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.uncommongoods.tugboat.bridge.update.UpdateCoordinator;
import com.uncommongoods.tugboat.bridge.update.UpdateEnvironment;
import com.uncommongoods.tugboat.bridge.update.UpdateManifest;
import com.uncommongoods.tugboat.bridge.update.ui.MandatoryUpdateDialog;
import com.uncommongoods.tugboat.bridge.update.ui.UpdateProgressWindow;

import java.nio.file.Path;

public class BridgeMain extends Application {

    private final UpdateCoordinator updateCoordinator = new UpdateCoordinator();
    private UpdateManifest pendingStartupUpdate;

    @Override
    public void init() throws Exception {
        notifyPreloader(new Preloader.ProgressNotification(0.1));

        // Check for a newer version while the splash screen is visible; the
        // check is bounded and any failure means "no update".
        pendingStartupUpdate = updateCoordinator.blockingStartupCheck().orElse(null);
        notifyPreloader(new Preloader.ProgressNotification(0.7));
        notifyPreloader(new Preloader.ProgressNotification(1.0));

        // Notify that we're about to start the main application
        notifyPreloader(new Preloader.StateChangeNotification(
            Preloader.StateChangeNotification.Type.BEFORE_START));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Set AtlantaFX Nord Light theme
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

        if (pendingStartupUpdate != null) {
            runStartupUpdate(primaryStage, pendingStartupUpdate);
            return;
        }

        launchMainUi(primaryStage);
    }

    /**
     * Silently downloads and installs a startup update, then restarts. The
     * main UI is never built; if anything fails the app falls through to a
     * normal launch on the old version.
     */
    private void runStartupUpdate(Stage primaryStage, UpdateManifest manifest) {
        System.out.println("Startup update available: " + UpdateEnvironment.currentVersion()
            + " -> " + manifest.version);
        UpdateProgressWindow progressWindow = new UpdateProgressWindow(manifest.version);
        progressWindow.show();

        Thread updateThread = new Thread(() -> {
            try {
                Path installer = updateCoordinator.client().download(manifest, progressWindow::setProgress);
                progressWindow.setStatus("Installing update... the application will restart.");
                if (UpdateCoordinator.launchInstallerAndExit(installer)) {
                    return;
                }
                // Dev simulation: fall through to a normal launch.
                Platform.runLater(() -> {
                    progressWindow.close();
                    launchMainUiSafely(primaryStage);
                });
            } catch (Exception e) {
                System.err.println("Startup update failed (launching current version): " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    progressWindow.close();
                    launchMainUiSafely(primaryStage);
                });
            }
        }, "startup-update");
        updateThread.setDaemon(true);
        updateThread.start();
    }

    private void launchMainUiSafely(Stage primaryStage) {
        try {
            launchMainUi(primaryStage);
        } catch (Exception e) {
            System.err.println("Failed to launch main UI: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }

    private void launchMainUi(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setTitle(UpdateEnvironment.appDisplayName());
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Set application icon (PNG format works on all platforms)
        try {
            Image icon = new Image(getClass().getResourceAsStream("/icons/tugboat.png"));
            primaryStage.getIcons().add(icon);
            System.out.println("Loaded application icon: /icons/tugboat.png");
        } catch (Exception e) {
            System.err.println("Warning: Could not load application icon: " + e.getMessage());
            e.printStackTrace();
        }

        // Add shutdown hook to cleanup resources
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Application closing - cleaning up resources...");
            updateCoordinator.shutdown();
            ConfigurationManager.getInstance().shutdown();
        });

        // Also add JVM shutdown hook as backup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("JVM shutdown hook - cleaning up resources...");
            ConfigurationManager.getInstance().shutdown();
        }));

        primaryStage.show();

        // While running, surface updates in a blocking dialog: the user can
        // only install the update or exit the app.
        updateCoordinator.startPeriodicChecks(manifest ->
            Platform.runLater(() -> {
                System.out.println("Runtime update available: " + manifest.version);
                MandatoryUpdateDialog.show(primaryStage, manifest, updateCoordinator.client());
            }));
    }
}
