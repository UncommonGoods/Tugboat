// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import com.uncommongoods.tugboat.bridge.data.Country;
import com.uncommongoods.tugboat.bridge.data.State;
import com.uncommongoods.tugboat.bridge.util.AddressFormSupport;
import com.uncommongoods.tugboat.engine.model.TugboatAddress;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Set Origin page: where a station without a cache says what address its shipments ship from.
 */
public class SetOriginController implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private TextField companyField;

    @FXML
    private TextField address1Field;

    @FXML
    private TextField address2Field;

    @FXML
    private ComboBox<Country> countryCombo;

    @FXML
    private TextField postalCodeField;

    @FXML
    private TextField cityField;

    @FXML
    private ComboBox<State> stateCombo;

    @FXML
    private TextField phoneField;

    @FXML
    private FontIcon saveOriginCheckmark;

    @FXML
    private Label saveOriginError;

    private final ConfigurationManager configManager = ConfigurationManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AddressFormSupport.configureCountryCombo(countryCombo);
        AddressFormSupport.configureStateCombo(stateCombo);
        countryCombo.setValue(Country.getDefault());

        populateFormFromSavedOrigin();
    }

    private void populateFormFromSavedOrigin() {
        TugboatAddress origin = configManager.getOriginAddress();
        if (origin == null) {
            return;
        }

        setIfPresent(nameField, origin.getName());
        setIfPresent(companyField, origin.getCompany());
        setIfPresent(address1Field, origin.getStreet1());
        setIfPresent(address2Field, origin.getStreet2());
        setIfPresent(cityField, origin.getCity());
        setIfPresent(postalCodeField, origin.getZip());
        setIfPresent(phoneField, origin.getPhone());

        AddressFormSupport.applyCountryCode(countryCombo, origin.getCountry());
        AddressFormSupport.applyStateAbbreviation(stateCombo, origin.getState());
    }

    private void setIfPresent(TextField field, String value) {
        if (value != null) {
            field.setText(value);
        }
    }

    @FXML
    private void saveOrigin() {
        String validationError = validate();
        if (validationError != null) {
            showError(validationError);
            return;
        }

        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("name", nameField.getText().trim());
        addressMap.put("company", companyField.getText().trim());
        addressMap.put("street1", address1Field.getText().trim());
        addressMap.put("street2", address2Field.getText().trim());
        addressMap.put("city", cityField.getText().trim());
        addressMap.put("zip", postalCodeField.getText().trim());
        addressMap.put("phone", phoneField.getText().trim());
        addressMap.put("country", countryCombo.getValue().getCode());
        addressMap.put("state", stateCombo.getValue().getAbbreviation());

        configManager.setOriginAddress(new TugboatAddress(addressMap));
        System.out.println("Origin address saved");

        showCheckmark();
    }

    private String validate() {
        if (isBlank(nameField) && isBlank(companyField)) {
            return "Enter a name or a company";
        }
        if (isBlank(address1Field)) {
            return "Address 1 is required";
        }
        if (isBlank(cityField)) {
            return "City is required";
        }
        if (stateCombo.getValue() == null) {
            return "Select a state";
        }
        if (isBlank(postalCodeField)) {
            return "Postal code is required";
        }
        if (countryCombo.getValue() == null) {
            return "Select a country";
        }
        if (isBlank(phoneField)) {
            return "Phone is required";
        }
        return null;
    }

    private boolean isBlank(TextField field) {
        return field.getText() == null || field.getText().isBlank();
    }

    private void showCheckmark() {
        saveOriginError.setVisible(false);
        saveOriginError.setManaged(false);
        saveOriginCheckmark.setVisible(true);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> saveOriginCheckmark.setVisible(false));
                timer.cancel();
            }
        }, 4000);
    }

    private void showError(String message) {
        saveOriginCheckmark.setVisible(false);
        saveOriginError.setText(message);
        saveOriginError.setVisible(true);
        saveOriginError.setManaged(true);
    }
}
