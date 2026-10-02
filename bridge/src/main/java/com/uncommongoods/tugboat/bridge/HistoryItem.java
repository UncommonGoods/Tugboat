// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.bridge.service.PrinterService;
import com.uncommongoods.tugboat.bridge.service.TugboatFactory;

import static com.uncommongoods.tugboat.engine.state.State.*;

public class HistoryItem {
    private static final ConfigurationManager configManager = ConfigurationManager.getInstance();
    private static final TugboatFactory tugboatFactory = new TugboatFactory(configManager);
    private static final PrinterService printerService = new PrinterService();

    private final String shipmentId;
    private final String carrier;
    private final String service;
    private final String charge;
    private final String trackingNumber;
    private final Button printButton;
    private final Button voidButton;
    private final Tugboat tugboat;

    public HistoryItem(String shipmentId, String carrier, String service, String charge, String trackingNumber, Tugboat tugboat) {
        this.shipmentId = shipmentId;
        this.carrier = carrier;
        this.service = service;
        this.charge = charge;
        this.trackingNumber = trackingNumber;
        this.tugboat = tugboat;

        // Create buttons with narrow vertical sizing
        this.printButton = new Button("Print");
        this.printButton.getStyleClass().add("small-button");
        this.printButton.setStyle("-fx-padding: 4 8 4 8; -fx-font-size: 12px;");
        this.printButton.setPrefHeight(30);
        this.printButton.setMaxHeight(30);
        this.printButton.setMinHeight(30);
        this.printButton.setOnAction(e -> handlePrint());

        this.voidButton = new Button("Void");
        this.voidButton.getStyleClass().add("small-button");
        this.voidButton.setStyle("-fx-padding: 4 8 4 8; -fx-font-size: 12px;");
        this.voidButton.setPrefHeight(30);
        this.voidButton.setMaxHeight(30);
        this.voidButton.setMinHeight(30);
        this.voidButton.setOnAction(e -> handleVoid());

        if (tugboat != null && tugboat.getPackageState() != null &&
            tugboat.getPackageState().getState().equals(PURCHASED)) {
            this.printButton.setDisable(true);
            this.printButton.setStyle("-fx-padding: 4 8 4 8; -fx-font-size: 12px; -fx-opacity: 0.5;");
        }

        if (tugboat != null && tugboat.getPackageState() != null &&
            tugboat.getPackageState().getState().equals(VOIDED)) {
            this.voidButton.setDisable(true);
            this.voidButton.setStyle("-fx-padding: 4 8 4 8; -fx-font-size: 12px; -fx-opacity: 0.5;");
            this.printButton.setDisable(true);
            this.printButton.setStyle("-fx-padding: 4 8 4 8; -fx-font-size: 12px; -fx-opacity: 0.5;");
        }
    }

    public static HistoryItem fromTugboat(Tugboat tugboat) {
        String shipmentId = tugboat.getCargoId();
        String carrier = "Unknown";
        String service = "Unknown";
        String charge = "N/A";
        String trackingNumber = "N/A";

        if (tugboat.getSelectedRate() != null) {
            charge = "$" + String.format("%.2f", tugboat.getSelectedRate().getRate());
            carrier = tugboat.getSelectedRate().getCarrier();
            service = tugboat.getSelectedRate().getService();
        }

        if (tugboat.getSelectedShipment() != null && tugboat.getSelectedShipment().getTracker() != null) {
            trackingNumber = tugboat.getSelectedShipment().getTracker().getTrackingCode();
        }

        return new HistoryItem(shipmentId, carrier, service, charge, trackingNumber, tugboat);
    }

    private void handlePrint() {
        printButton.setDisable(true);

        Task<Void> printTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (tugboat == null) {
                    throw new Exception("Cannot print - tugboat is null");
                }
                tugboatFactory.adopt(tugboat);
                var labels = tugboat.reprint();
                System.out.println("Reprinted shipment: " + shipmentId);
                printerService.printLabels(labels, shipmentId);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    FontIcon checkIcon = new FontIcon(MaterialDesignC.CHECK);
                    checkIcon.setIconSize(12);
                    printButton.setGraphic(checkIcon);
                    printButton.setText("");

                    PauseTransition pause = new PauseTransition(Duration.seconds(3));
                    pause.setOnFinished(e -> {
                        printButton.setGraphic(null);
                        printButton.setText("Print");
                        printButton.setDisable(false);
                    });
                    pause.play();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    printButton.setDisable(false);

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Print Failed");
                    alert.setHeaderText("Failed to print shipment " + shipmentId);
                    alert.setContentText(getException().getMessage());
                    alert.showAndWait();
                });
            }
        };

        Thread backgroundThread = new Thread(printTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void handleVoid() {
        voidButton.setDisable(true);
        printButton.setDisable(true);

        Task<Void> voidTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (tugboat == null) {
                    throw new Exception("Cannot void - tugboat is null");
                }
                tugboatFactory.adopt(tugboat);
                tugboat.voidLabel();
                System.out.println("Voided shipment: " + shipmentId);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    FontIcon checkIcon = new FontIcon(MaterialDesignC.CHECK);
                    checkIcon.setIconSize(12);
                    voidButton.setGraphic(checkIcon);
                    voidButton.setText("");

                    PauseTransition pause = new PauseTransition(Duration.seconds(3));
                    pause.setOnFinished(e -> {
                        voidButton.setGraphic(null);
                        voidButton.setText("Void");
                    });
                    pause.play();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    voidButton.setDisable(false);

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Void Failed");
                    alert.setHeaderText("Failed to void shipment " + shipmentId);
                    alert.setContentText(getException().getMessage());
                    alert.showAndWait();
                });
            }
        };

        Thread backgroundThread = new Thread(voidTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    public String getShipmentId() { return shipmentId; }
    public String getCarrier() { return carrier; }
    public String getService() { return service; }
    public String getCharge() { return charge; }
    public String getTrackingNumber() { return trackingNumber; }
    public Button getPrintButton() { return printButton; }
    public Button getVoidButton() { return voidButton; }
    public Tugboat getTugboat() { return tugboat; }
}
