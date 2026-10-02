// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.uncommongoods.tugboat.bridge.service.ScaleService;
import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IParcel;
import com.uncommongoods.tugboat.bridge.service.PrinterService;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.model.TugboatParcel;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import static com.uncommongoods.tugboat.engine.state.State.PURCHASED;

public class WeightMarginModalController implements Initializable {

    private static final Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);
    private static final String DEVICE_PREFS_PREFIX = "tugboat.device.";

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Label scaleWeightLabel;

    @FXML
    private Label estimatedWeightLabel;

    @FXML
    private Label differenceLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private Button yesButton;

    @FXML
    private Button noButton;

    private Runnable onClose;
    private WeightMarginModalCallback callback;
    private Tugboat tugboat;
    private double scaleWeight;
    private double estimatedWeight;
    private double difference;
    private PrinterService printerService = new PrinterService();
    private java.util.function.Supplier<String> currentScaleWeightSupplier;

    public interface WeightMarginModalCallback {
        void onProceedWithShipping();
        void onCancelShipping();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setCallback(WeightMarginModalCallback callback) {
        this.callback = callback;
    }

    public void setCurrentScaleWeightSupplier(java.util.function.Supplier<String> supplier) {
        this.currentScaleWeightSupplier = supplier;
    }

    public void setWeightInfo(Tugboat tugboat, double scaleWeight, double estimatedWeight) {
        this.tugboat = tugboat;
        this.scaleWeight = scaleWeight;
        this.estimatedWeight = estimatedWeight;
        this.difference = Math.abs(scaleWeight - estimatedWeight);

        scaleWeightLabel.setText(String.format("%.2f lbs", scaleWeight));
        estimatedWeightLabel.setText(String.format("%.2f lbs", estimatedWeight));
        differenceLabel.setText(String.format("%.2f lbs", difference));
    }

    @FXML
    private void handleYes() {
        boolean isAdminMode = SignInController.isAdminMode();

        try {
            double currentScaleWeight = getCurrentScaleWeight();
            double currentDifference = Math.abs((currentScaleWeight * 16) - (estimatedWeight * 16));

            String marginStr = prefs.get(DEVICE_PREFS_PREFIX + "scale.weightMargin", "48");
            double weightMargin = Double.parseDouble(marginStr);

            if (currentScaleWeight == 0) {
                String errorMessage = "Scale weight is zero. Lift package and re-place it on the scale.";
                printerService.printErrorMessage(errorMessage);
                System.err.println(errorMessage);
                closeModal();
                if (callback != null) {
                    callback.onCancelShipping();
                }
            }

            if (isAdminMode) {
                handleAdminModeWeightCorrection();
                closeModal();
                if (callback != null) {
                    callback.onProceedWithShipping();
                }
            } else {
                if (currentDifference > weightMargin) {
                    String errorMessage = String.format(
                        "The difference between the scale weight %.2f lb and the estimated weight %.2f lb is unusually large. Check package.",
                        currentScaleWeight, estimatedWeight);

                    printerService.printErrorMessage(errorMessage);
                    System.err.println(errorMessage);

                    if (tugboat.getOptions() != null && tugboat.getOptions().getErrorHook() != null) {
                        try {
                            tugboat.getOptions().getErrorHook().execute(tugboat, new Exception(errorMessage));
                        } catch (TugboatException ignored) {
                            // getErrorHook().execute() always throws
                        }
                    }

                    closeModal();
                    if (callback != null) {
                        callback.onCancelShipping();
                    }
                } else {
                    closeModal();
                    if (callback != null) {
                        callback.onProceedWithShipping();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling weight validation: " + e.getMessage());
            e.printStackTrace();
            closeModal();
            if (callback != null) {
                callback.onProceedWithShipping();
            }
        }
    }

    @FXML
    private void handleNo() {
        closeModal();
        if (callback != null) {
            callback.onCancelShipping();
        }
    }

    private void handleAdminModeWeightCorrection() throws Exception {
        if (tugboat == null) return;

        if (tugboat.getPackageState() != null && tugboat.getPackageState().getState().equals(PURCHASED)) {
            tugboat.voidLabel();
        } else {
            tugboat.reset();
        }

        String currentScaleWeightStr = ScaleService.getInstance().getCurrentWeight();
        double currentScaleWeight = Double.parseDouble(currentScaleWeightStr.equals("--") ? "0" : currentScaleWeightStr);

        if (tugboat.getParcels() != null && !tugboat.getParcels().isEmpty()) {
            IParcel parcel = tugboat.getParcels().get(0);
            if (parcel instanceof TugboatParcel) {
                ((TugboatParcel) parcel).setWeight((float) currentScaleWeight * 16);
            }
        }
    }

    private double getCurrentScaleWeight() {
        try {
            if (currentScaleWeightSupplier != null) {
                String scaleWeightStr = currentScaleWeightSupplier.get();
                return Double.parseDouble(scaleWeightStr.equals("--") ? "0" : scaleWeightStr);
            }
        } catch (NumberFormatException e) {
            // fail silently
        }
        return scaleWeight;
    }

    private void printWeightDifferenceError() {
        String errorMessage = String.format(
            "The difference between the scale weight %.2f and the estimated weight %.2f is %.2f is unusually large. Check package.",
            scaleWeight, estimatedWeight, difference);

        printerService.printErrorMessage(errorMessage);
        System.err.println(errorMessage);
    }

    private void closeModal() {
        if (onClose != null) {
            onClose.run();
        }
    }

    public double getUpdatedWeight() {
        return scaleWeight;
    }
}
