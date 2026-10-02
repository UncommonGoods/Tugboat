// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.update.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.uncommongoods.tugboat.bridge.update.UpdateEnvironment;

/**
 * Small splash-style window shown while a startup update downloads and
 * installs. Styled to match BridgePreloader.
 */
public class UpdateProgressWindow {

    private final Stage stage;
    private final ProgressBar progressBar;
    private final Label statusLabel;

    public UpdateProgressWindow(String targetVersion) {
        Label titleLabel = new Label("Updating " + UpdateEnvironment.appDisplayName() + " to " + targetVersion);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        progressBar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setPrefWidth(300);

        statusLabel = new Label("Downloading update...");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        VBox root = new VBox(15, titleLabel, progressBar, statusLabel);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #addad9;");

        stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setAlwaysOnTop(true);
        stage.setScene(new Scene(root, 400, 160));
        stage.centerOnScreen();
    }

    public void show() {
        stage.show();
    }

    public void close() {
        runOnFxThread(stage::close);
    }

    public void setProgress(long bytesRead, long totalBytes) {
        runOnFxThread(() -> {
            if (totalBytes > 0) {
                progressBar.setProgress((double) bytesRead / totalBytes);
            }
        });
    }

    public void setStatus(String text) {
        runOnFxThread(() -> statusLabel.setText(text));
    }

    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
