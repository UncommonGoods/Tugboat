// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.update.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.uncommongoods.tugboat.bridge.update.UpdateClient;
import com.uncommongoods.tugboat.bridge.update.UpdateCoordinator;
import com.uncommongoods.tugboat.bridge.update.UpdateEnvironment;
import com.uncommongoods.tugboat.bridge.update.UpdateManifest;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;

import java.nio.file.Path;

/**
 * Blocking dialog shown when an update is detected while the app is running.
 * The user's only options are installing the update or exiting the app: the
 * window cannot be closed or dismissed.
 */
public class MandatoryUpdateDialog {

    private final Stage stage;
    private final UpdateManifest manifest;
    private final UpdateClient client;
    private final ProgressBar progressBar;
    private final Label statusLabel;
    private final Button updateButton;
    private final Button exitButton;
    private final HBox buttonRow;

    public static void show(Stage owner, UpdateManifest manifest, UpdateClient client) {
        new MandatoryUpdateDialog(owner, manifest, client).stage.show();
    }

    private MandatoryUpdateDialog(Stage owner, UpdateManifest manifest, UpdateClient client) {
        this.manifest = manifest;
        this.client = client;

        FontIcon updateIcon = new FontIcon(MaterialDesignD.DOWNLOAD);
        updateIcon.setIconSize(32);
        updateIcon.getStyleClass().add("update-icon");

        Label titleLabel = new Label("Update Required");
        titleLabel.getStyleClass().add("update-title");
        titleLabel.setStyle("-fx-font-size: 18px;");

        Label versionLabel = new Label("Version " + manifest.version + " is available");
        versionLabel.getStyleClass().add("update-version");

        Label currentVersionLabel = new Label("You are using version " + UpdateEnvironment.currentVersion());
        currentVersionLabel.getStyleClass().add("current-version");

        VBox contentBox = new VBox(12, updateIcon, titleLabel, versionLabel, currentVersionLabel);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getStyleClass().add("update-modal");
        contentBox.setPadding(new Insets(30));

        if (manifest.releaseNotes != null && !manifest.releaseNotes.isBlank()) {
            TextArea notesArea = new TextArea(manifest.releaseNotes);
            notesArea.setEditable(false);
            notesArea.setWrapText(true);
            notesArea.setPrefRowCount(4);
            notesArea.getStyleClass().add("release-notes");
            contentBox.getChildren().add(notesArea);
        }

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(280);
        progressBar.getStyleClass().add("update-progress");
        progressBar.setVisible(false);
        progressBar.setManaged(false);

        statusLabel = new Label("The application must be updated to continue.");
        statusLabel.getStyleClass().add("update-status");
        statusLabel.setWrapText(true);

        updateButton = new Button("Update Now");
        updateButton.getStyleClass().addAll("update-button", "accent");
        updateButton.setOnAction(e -> beginUpdate());

        exitButton = new Button("Exit Application");
        exitButton.getStyleClass().add("update-button");
        exitButton.setOnAction(e -> Platform.exit());

        buttonRow = new HBox(15, updateButton, exitButton);
        buttonRow.setAlignment(Pos.CENTER);

        contentBox.getChildren().addAll(progressBar, statusLabel, buttonRow);

        Scene scene = new Scene(contentBox, 420, -1);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setScene(scene);
        // No way out: the dialog cannot be closed, only "Update Now" or
        // "Exit Application".
        stage.setOnCloseRequest(javafx.event.Event::consume);
        stage.centerOnScreen();
    }

    private void beginUpdate() {
        updateButton.setDisable(true);
        exitButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setManaged(true);
        statusLabel.setText("Downloading update...");

        Thread downloadThread = new Thread(() -> {
            try {
                Path installer = client.download(manifest, (bytesRead, totalBytes) -> {
                    if (totalBytes > 0) {
                        Platform.runLater(() -> progressBar.setProgress((double) bytesRead / totalBytes));
                    }
                });
                Platform.runLater(() -> statusLabel.setText("Installing update... the application will restart."));
                if (!UpdateCoordinator.launchInstallerAndExit(installer)) {
                    Platform.runLater(() -> statusLabel.setText("Dev simulate: install skipped."));
                }
            } catch (Exception e) {
                System.err.println("Update failed: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> showFailure(e));
            }
        }, "update-download");
        downloadThread.setDaemon(true);
        downloadThread.start();
    }

    private void showFailure(Exception e) {
        progressBar.setVisible(false);
        progressBar.setManaged(false);
        statusLabel.setText("Update failed: " + e.getMessage());
        updateButton.setText("Retry");
        updateButton.setDisable(false);
        exitButton.setDisable(false);
    }
}
