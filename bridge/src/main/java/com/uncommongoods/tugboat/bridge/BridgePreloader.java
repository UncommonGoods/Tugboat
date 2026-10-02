// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import javafx.animation.PauseTransition;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class BridgePreloader extends Preloader {
    private static final double MIN_SPLASH_SECONDS = 2.5;
    private Stage preloaderStage;
    private boolean appReady = false;
    private boolean minTimeElapsed = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.preloaderStage = primaryStage;

        ImageView splashImage;
        try {
            Image splash = new Image(getClass().getResourceAsStream("/images/splash.png"));
            splashImage = new ImageView(splash);
            splashImage.setFitWidth(400);
            splashImage.setFitHeight(300);
            splashImage.setPreserveRatio(false);
        } catch (Exception e) {
            splashImage = null;
        }

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #addad9;");

        if (splashImage != null) {
            root.getChildren().add(splashImage);
        } else {
            javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Tugboat Bridge");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");

            javafx.scene.control.Label loadingLabel = new javafx.scene.control.Label("Loading...");
            loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

            root.getChildren().addAll(titleLabel, loadingLabel);
            root.setSpacing(10);
        }

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.centerOnScreen();
        primaryStage.show();

        PauseTransition minDisplayTimer = new PauseTransition(Duration.seconds(MIN_SPLASH_SECONDS));
        minDisplayTimer.setOnFinished(e -> {
            minTimeElapsed = true;
            if (appReady) {
                preloaderStage.hide();
            }
        });
        minDisplayTimer.play();

        System.out.println("Preloader displayed");
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        if (info instanceof StateChangeNotification) {
            StateChangeNotification stateNotification = (StateChangeNotification) info;
            if (stateNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
                System.out.println("Hiding preloader - main application starting");
                appReady = true;
                if (minTimeElapsed) {
                    preloaderStage.hide();
                }
            }
        }
    }

    @Override
    public void handleProgressNotification(ProgressNotification info) {
        System.out.println("Preloader progress: " + (info.getProgress() * 100) + "%");
    }

    @Override
    public boolean handleErrorNotification(ErrorNotification info) {
        System.err.println("Preloader error: " + info.getCause());
        if (preloaderStage != null) {
            preloaderStage.hide();
        }
        return true;
    }
}
