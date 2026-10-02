// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class HazmatModalController implements Initializable {

    @FXML
    private Label hazmatMessageLabel;

    @FXML
    private Button okButton;

    private Runnable onClose;
    private HazmatModalCallback callback;
    private String unCode;

    public interface HazmatModalCallback {
        void onProceed();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup is done in setHazmatInfo method
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setCallback(HazmatModalCallback callback) {
        this.callback = callback;
    }

    public void setHazmatInfo(String unCode) {
        this.unCode = unCode;
        if (unCode != null && !unCode.isEmpty()) {
            hazmatMessageLabel.setText("APPLY LABEL \"" + unCode + "\" TO SIDE OF BOX");
        } else {
            hazmatMessageLabel.setText("HAZMAT HANDLING REQUIRED");
        }
    }

    @FXML
    private void proceed() {
        if (callback != null) {
            callback.onProceed();
        }
        closeModal();
    }

    private void closeModal() {
        if (onClose != null) {
            onClose.run();
        }
    }

    public String getUnCode() {
        return unCode;
    }
}