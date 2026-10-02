// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import atlantafx.base.controls.ToggleSwitch;
import com.uncommongoods.tugboat.bridge.util.EncryptionUtil;
import com.uncommongoods.tugboat.bridge.service.PrinterPreferences;
import com.uncommongoods.tugboat.bridge.service.PrinterService;
import com.uncommongoods.tugboat.bridge.service.ScaleService;
import com.uncommongoods.tugboat.engine.ports.cache.CacheClientFactory;
import com.uncommongoods.tugboat.engine.ports.config.TugboatSettings;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ShipmentOptionValues.LabelFormat;
import com.uncommongoods.tugboat.engine.ports.shipping.service.ShippingClientFactory;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.application.Platform;
import java.util.Timer;
import java.util.TimerTask;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fazecast.jSerialComm.SerialPort;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.Preferences;

public class SettingsController implements Initializable {

    private static final Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);

    private static final String prefsPrefix = "tugboat.";
    private static final String devicePrefsPrefix = prefsPrefix + "device.";
    private static final String dataPrefsPrefix = prefsPrefix + "data.";

    /** Shared left-column width so client fields and settings rows line up. */
    private static final double LABEL_COLUMN_WIDTH = 220;

    @FXML
    private ComboBox<String> scalePortCombo;

    @FXML
    private TextField scaleWeightMarginField;

    @FXML
    private ComboBox<String> zplPrinterCombo;

    @FXML
    private ComboBox<String> pdfPrinterCombo;

    @FXML
    private Button weighButton;

    @FXML
    private Button zeroButton;

    @FXML
    private Button zplPrintTestButton;

    @FXML
    private Button pdfPrintTestButton;

    @FXML
    private FontIcon zplPrintTestCheckmark;

    @FXML
    private Label zplPrintTestError;

    @FXML
    private FontIcon pdfPrintTestCheckmark;

    @FXML
    private Label pdfPrintTestError;

    @FXML
    private Label scaleReadingLabel;

    @FXML
    private Button saveDevicesButton;

    @FXML
    private FontIcon saveDevicesCheckmark;

    @FXML
    private VBox clientsContainer;

    @FXML
    private VBox settingsRowsContainer;

    @FXML
    private FontIcon saveDataConfigCheckmark;

    @FXML
    private ToggleSwitch environmentToggle;

    @FXML
    private ToggleSwitch labelFormatToggle;

    @FXML
    private PasswordField settingsPasswordField;

    @FXML
    private TextField settingsPasswordVisible;

    @FXML
    private FontIcon settingsPasswordIcon;

    @FXML
    private FontIcon saveSettingsPasswordCheckmark;

    @FXML
    private Label settingsPasswordError;

    @FXML
    private PasswordField adminPasswordField;

    @FXML
    private TextField adminPasswordVisible;

    @FXML
    private FontIcon adminPasswordIcon;

    @FXML
    private FontIcon saveAdminPasswordCheckmark;

    @FXML
    private Label adminPasswordError;

    private final ConfigurationManager configManager = ConfigurationManager.getInstance();

    private final PrinterService printerService = new PrinterService();

    /** Guards the environment toggle and ConfigurationManager from echoing each other. */
    private boolean syncingEnvironment = false;

    /**
     * Follow environment switches made elsewhere — the left nav's Dev Mode buttons
     * are reachable while this page is showing, and the Data section reads and
     * writes whichever environment the toggle names.
     */
    private final ChangeListener<Boolean> environmentListener = (obs, was, isDev) -> {
        if (syncingEnvironment) {
            return;
        }
        syncingEnvironment = true;
        try {
            environmentToggle.setSelected(isDev);
        } finally {
            syncingEnvironment = false;
        }
        loadDataSettings();
    };

    private Map<String, ShippingClientFactory> clientFactories = new LinkedHashMap<>();

    private final List<ClientRow> clientRows = new ArrayList<>();
    private final List<SettingRow> settingRows = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateScalePortDropdown();
        populatePrinterDropdowns();

        scalePortCombo.setPromptText("Select scale port");
        zplPrinterCombo.setPromptText("Select ZPL printer");
        pdfPrinterCombo.setPromptText("Select PDF printer");

        loadDeviceSettings();

        setupButtonStateListeners();

        updateButtonStates();

        environmentToggle.setSelected(configManager.isDevEnvironment());
        environmentToggle.selectedProperty().addListener((obs, was, isDev) -> {
            if (syncingEnvironment) {
                return;
            }
            syncingEnvironment = true;
            try {
                SignInController.setAdminMode(true);
                configManager.setEnvironment(isDev);
            } finally {
                syncingEnvironment = false;
            }
            loadDataSettings();
        });
        // Weak, because a new controller is created every time this page opens: a
        // strong listener on the singleton would keep every past one alive.
        configManager.devEnvironmentProperty()
            .addListener(new WeakChangeListener<>(environmentListener));

        clientFactories = ConfigurationManager.loadClientFactories();

        loadDataSettings();
    }

    @FXML
    private void weighScale() {
        System.out.println("Weigh button clicked");
        String selectedPort = scalePortCombo.getValue();
        if (selectedPort == null || selectedPort.isEmpty()) {
            System.out.println("No scale port selected");
            return;
        }
        System.out.println("Getting weight reading from scale");
        String reading = ScaleService.getInstance().weighOnce(selectedPort);
        updateScaleReadingDisplay(reading);
    }

    @FXML
    private void zeroScale() {
        System.out.println("Zero button clicked");
        String selectedPort = scalePortCombo.getValue();
        if (selectedPort == null || selectedPort.isEmpty()) {
            System.out.println("No scale port selected");
            return;
        }
        System.out.println("Zeroing scale");
        ScaleService.getInstance().zeroScale(selectedPort);

        Platform.runLater(() -> {
            try {
                Thread.sleep(500);
                String reading = ScaleService.getInstance().weighOnce(selectedPort);
                updateScaleReadingDisplay(reading);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @FXML
    private void testZplPrint() {
        System.out.println("ZPL Print Test button clicked");
        String selectedPrinter = zplPrinterCombo.getValue();
        if (!PrinterPreferences.isConfigured(selectedPrinter)) {
            System.out.println("No ZPL printer selected");
            return;
        }
        System.out.println("Testing ZPL printer: " + selectedPrinter);
        reportTestPrint(printerService.testZplPrinter(selectedPrinter), zplPrintTestCheckmark, zplPrintTestError);
    }

    @FXML
    private void testPdfPrint() {
        System.out.println("PDF Print Test button clicked");
        String selectedPrinter = pdfPrinterCombo.getValue();
        if (!PrinterPreferences.isConfigured(selectedPrinter)) {
            System.out.println("No PDF printer selected");
            return;
        }
        System.out.println("Testing PDF printer: " + selectedPrinter);
        reportTestPrint(printerService.testPdfPrinter(selectedPrinter), pdfPrintTestCheckmark, pdfPrintTestError);
    }

    private void reportTestPrint(CompletableFuture<Boolean> printJob, FontIcon checkmark, Label errorLabel) {
        checkmark.setVisible(false);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        printJob.thenAccept(success -> {
            if (success) {
                showCheckmark(checkmark);
            } else {
                showError(errorLabel, "Print failed - check the printer");
            }
        }).exceptionally(throwable -> {
            System.err.println("Test print failed: " + throwable.getMessage());
            showError(errorLabel, "Print failed - check the printer");
            return null;
        });
    }

    @FXML
    private void saveDevices() {
        System.out.println("Save Devices button clicked");
        String scalePort = scalePortCombo.getValue();
        String zplPrinter = zplPrinterCombo.getValue();
        String pdfPrinter = pdfPrinterCombo.getValue();
        String weightMargin = scaleWeightMarginField.getText();
        LabelFormat labelFormat = labelFormatToggle.isSelected() ? LabelFormat.PDF : LabelFormat.ZPL;

        System.out.println("Scale Port: " + scalePort);
        System.out.println("ZPL Printer: " + zplPrinter);
        System.out.println("PDF Printer: " + pdfPrinter);
        System.out.println("Label Format: " + labelFormat);
        System.out.println("Weight Margin: " + weightMargin);

        prefs.put(devicePrefsPrefix + "scale.port", scalePort != null ? scalePort : "");
        prefs.put(devicePrefsPrefix + "scale.weightMargin", weightMargin != null ? weightMargin : "48");
        PrinterPreferences.save(zplPrinter, pdfPrinter, labelFormat);

        System.out.println("Device settings saved successfully");

        // Show checkmark for 4 seconds
        showCheckmark(saveDevicesCheckmark);
    }

    // Data section -  shipping clients + freeform settings rows

    /**
     * masked value input: PasswordField/TextField pair with an eye toggle.
     */
    private final class ValueField {
        final HBox node;
        final PasswordField hidden;
        final TextField shown;

        ValueField() {
            hidden = new PasswordField();
            shown = new TextField();
            shown.setManaged(false);
            shown.setVisible(false);
            HBox.setHgrow(hidden, Priority.ALWAYS);
            HBox.setHgrow(shown, Priority.ALWAYS);

            FontIcon icon = new FontIcon("mdi2e-eye");
            icon.setIconSize(16);
            Button toggle = new Button();
            toggle.setGraphic(icon);
            toggle.getStyleClass().add("eye-toggle-button");
            toggle.setOnAction(e -> togglePasswordVisibility(hidden, shown, icon));

            node = new HBox(hidden, shown, toggle);
            node.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(node, Priority.ALWAYS);
        }

        String getValue() {
            return hidden.isVisible() ? hidden.getText() : shown.getText();
        }

        void setValue(String value) {
            if (hidden.isVisible()) {
                hidden.setText(value);
            } else {
                shown.setText(value);
            }
        }
    }

    /** shipping client entry: provider key + factory type + config fields. */
    private final class ClientRow {
        final VBox node;
        final TextField providerKeyField;
        final ComboBox<String> typeCombo;
        final VBox configBox;
        final Map<String, ValueField> configFields = new LinkedHashMap<>();
        final boolean isDefault;

        ClientRow(String providerKey, String type, Map<String, String> config, boolean isDefault) {
            this.isDefault = isDefault;

            providerKeyField = new TextField(providerKey);
            providerKeyField.setPromptText("client key");
            providerKeyField.setPrefWidth(LABEL_COLUMN_WIDTH);
            providerKeyField.setMinWidth(LABEL_COLUMN_WIDTH);
            providerKeyField.setMaxWidth(LABEL_COLUMN_WIDTH);
            providerKeyField.setEditable(!isDefault);

            typeCombo = new ComboBox<>();
            typeCombo.getItems().addAll(clientFactories.keySet());
            typeCombo.setPrefWidth(220);
            typeCombo.setPromptText("type");
            if (type != null && clientFactories.containsKey(type)) {
                typeCombo.setValue(type);
            }

            configBox = new VBox(6);

            HBox header = new HBox(10, providerKeyField, typeCombo);
            header.setAlignment(Pos.CENTER_LEFT);

            node = new VBox(6, header, configBox);

            if (!isDefault) {
                Button removeButton = new Button();
                FontIcon removeIcon = new FontIcon("mdi2c-close");
                removeIcon.setIconSize(16);
                removeButton.setGraphic(removeIcon);
                removeButton.setOnAction(e -> {
                    clientRows.remove(this);
                    clientsContainer.getChildren().remove(node);
                });
                header.getChildren().add(removeButton);
            }

            typeCombo.valueProperty().addListener((obs, oldType, newType) -> rebuildConfigFields(config));
            rebuildConfigFields(config);
        }

        private void rebuildConfigFields(Map<String, String> initialConfig) {
            Map<String, String> current = new LinkedHashMap<>();
            if (initialConfig != null) {
                current.putAll(initialConfig);
            }
            configFields.forEach((key, field) -> {
                String value = field.getValue();
                if (value != null && !value.isEmpty()) {
                    current.put(key, value);
                }
            });

            configFields.clear();
            configBox.getChildren().clear();

            ShippingClientFactory factory = typeCombo.getValue() != null
                ? clientFactories.get(typeCombo.getValue())
                : null;
            if (factory == null) {
                return;
            }
            for (String configKey : factory.configKeys()) {
                Label label = new Label(configKey + ":");
                label.getStyleClass().addAll("field-label", "text-caption");
                label.setPrefWidth(LABEL_COLUMN_WIDTH);
                label.setMinWidth(LABEL_COLUMN_WIDTH);
                ValueField field = new ValueField();
                field.setValue(current.getOrDefault(configKey, ""));
                configFields.put(configKey, field);
                HBox row = new HBox(10, label, field.node);
                row.setAlignment(Pos.CENTER_LEFT);
                configBox.getChildren().add(row);
            }
        }

        String getProviderKey() {
            return providerKeyField.getText() != null ? providerKeyField.getText().trim() : "";
        }

        String getType() {
            return typeCombo.getValue() != null ? typeCombo.getValue() : "";
        }

        Map<String, String> getConfig() {
            Map<String, String> config = new LinkedHashMap<>();
            configFields.forEach((key, field) -> config.put(key, field.getValue() != null ? field.getValue() : ""));
            return config;
        }
    }

    /**
     * A freeform name/value settings row. Fixed rows display a label but store
     * under a canonical name (e.g. "Cache URL" -> CACHE_URL).
     */
    private final class SettingRow {
        final HBox node;
        final TextField nameField;
        final ValueField valueField;
        final String canonicalName; // non-null for fixed rows

        SettingRow(String displayName, String canonicalName, String value) {
            this.canonicalName = canonicalName;

            nameField = new TextField(displayName);
            nameField.setPromptText("name");
            nameField.setPrefWidth(LABEL_COLUMN_WIDTH);
            nameField.setMinWidth(LABEL_COLUMN_WIDTH);
            nameField.setMaxWidth(LABEL_COLUMN_WIDTH);
            nameField.setEditable(canonicalName == null);

            valueField = new ValueField();
            valueField.setValue(value != null ? value : "");

            node = new HBox(10, nameField, valueField.node);
            node.setAlignment(Pos.CENTER_LEFT);
            if (canonicalName == null) {
                Button removeButton = new Button();
                FontIcon removeIcon = new FontIcon("mdi2c-close");
                removeIcon.setIconSize(16);
                removeButton.setGraphic(removeIcon);
                removeButton.setOnAction(e -> {
                    settingRows.remove(this);
                    settingsRowsContainer.getChildren().remove(node);
                });
                node.getChildren().add(removeButton);
            }
        }

        /** The name this row is stored/published under. */
        String getName() {
            if (canonicalName != null) {
                return canonicalName;
            }
            return nameField.getText() != null ? nameField.getText().trim() : "";
        }

        String getValue() {
            return valueField.getValue() != null ? valueField.getValue() : "";
        }
    }

    @FXML
    private void addClient() {
        addClientRow("", null, Map.of(), false);
    }

    @FXML
    private void addSettingRow() {
        addSettingRow("", null, "");
    }

    private ClientRow addClientRow(String providerKey, String type, Map<String, String> config, boolean isDefault) {
        ClientRow row = new ClientRow(providerKey, type, config, isDefault);
        clientRows.add(row);
        clientsContainer.getChildren().add(row.node);
        return row;
    }

    private SettingRow addSettingRow(String displayName, String canonicalName, String value) {
        SettingRow row = new SettingRow(displayName, canonicalName, value);
        settingRows.add(row);
        settingsRowsContainer.getChildren().add(row.node);
        return row;
    }

    @FXML
    private void saveDataConfig() {
        String environment = environmentToggle.isSelected() ? "DEV" : "PROD";
        System.out.println("Save Data Config button clicked - " + environment + " environment");

        String envPrefix = dataPrefsPrefix + environment.toLowerCase() + ".";

        try {
            // Clear the previously stored data config for this environment
            for (String key : prefs.keys()) {
                if (key.startsWith(envPrefix + "setting.") || key.startsWith(envPrefix + "client.")) {
                    prefs.remove(key);
                }
            }

            // Shipping clients
            for (ClientRow row : clientRows) {
                String providerKey = row.getProviderKey();
                if (providerKey.isEmpty()) {
                    continue;
                }
                prefs.put(envPrefix + "client." + providerKey + ".type", row.getType());
                row.getConfig().forEach((configKey, value) ->
                    prefs.put(envPrefix + "client." + providerKey + ".cfg." + configKey,
                        EncryptionUtil.encrypt(value)));
            }

            // Settings rows (fixed rows store under their canonical names)
            for (SettingRow row : settingRows) {
                String name = row.getName();
                if (name.isEmpty()) {
                    continue;
                }
                prefs.put(envPrefix + "setting." + name, EncryptionUtil.encrypt(row.getValue()));
            }
        } catch (Exception e) {
            System.err.println("Failed to save data config for " + environment + ": " + e.getMessage());
            return;
        }

        System.out.println("Data config settings saved successfully for " + environment + " environment");

        configManager.refreshConfiguration(environment.toLowerCase());

        showCheckmark(saveDataConfigCheckmark);
    }

    private void loadDataSettings() {
        String environment = environmentToggle.isSelected() ? "DEV" : "PROD";
        String env = environment.toLowerCase();

        // Normally a no-op: startup already migrated both environments. Kept so
        // opening Settings still self-heals a profile that somehow missed it.
        ConfigurationManager.migrateLegacyPrefsIfNeeded(env);

        clientRows.clear();
        clientsContainer.getChildren().clear();
        settingRows.clear();
        settingsRowsContainer.getChildren().clear();

        // Clients: seeded default entry first, then the rest
        Map<String, ConfigurationManager.ClientEntry> clients = configManager.readClientsForEnvironment(env);
        ConfigurationManager.ClientEntry defaultEntry =
            clients.remove(ConfigurationManager.DEFAULT_CLIENT_KEY);
        String defaultType = defaultEntry != null && !defaultEntry.type.isEmpty()
            ? defaultEntry.type
            : ConfigurationManager.DEFAULT_CLIENT_TYPE;
        addClientRow(ConfigurationManager.DEFAULT_CLIENT_KEY, defaultType,
            defaultEntry != null ? defaultEntry.config : Map.of(), true);
        clients.forEach((providerKey, entry) ->
            addClientRow(providerKey, entry.type, entry.config, false));

        // Settings rows: the cache client's fields as fixed rows first, then
        // freeform, then one blank. The fields come from the discovered
        // factory's configKeys(), so a different cache adapter brings its own
        // rows with no changes here.
        Map<String, String> settings = configManager.readSettingsForEnvironment(env);
        CacheClientFactory cacheFactory = ConfigurationManager.loadCacheClientFactory(settings);
        if (cacheFactory != null) {
            for (String configKey : cacheFactory.configKeys()) {
                String canonicalName = TugboatSettings.normalize(configKey);
                addSettingRow(configKey, canonicalName, settings.remove(canonicalName));
            }
        }
        settings.forEach((name, value) -> addSettingRow(name, null, value));
        addSettingRow("", null, "");

        System.out.println("Loaded data settings for " + environment + " environment");
    }

    @FXML
    private void saveSettingsPassword() {
        savePassword(SignInController::setSettingsPassword, getSettingsPasswordValue(),
                settingsPasswordField, settingsPasswordVisible,
                saveSettingsPasswordCheckmark, settingsPasswordError, "Settings");
    }

    @FXML
    private void saveAdminPassword() {
        savePassword(SignInController::setAdminPassword, getAdminPasswordValue(),
                adminPasswordField, adminPasswordVisible,
                saveAdminPasswordCheckmark, adminPasswordError, "Admin");
    }

    private void savePassword(Consumer<String> setter, String newPassword, PasswordField passwordField,
                              TextField visibleField, FontIcon checkmark, Label errorLabel, String label) {
        if (newPassword.trim().isEmpty()) {
            showError(errorLabel, "Password cannot be empty");
            return;
        }

        setter.accept(newPassword);
        System.out.println(label + " password updated successfully");

        showCheckmark(checkmark);
        passwordField.clear();
        visibleField.clear();
    }

    private void loadDeviceSettings() {
        String scalePort = prefs.get(devicePrefsPrefix + "scale.port", "");
        String zplPrinter = PrinterPreferences.getZplPrinter();
        String pdfPrinter = PrinterPreferences.getPdfPrinter();
        String weightMargin = prefs.get(devicePrefsPrefix + "scale.weightMargin", "48");

        if (!scalePort.isEmpty()) {
            scalePortCombo.setValue(scalePort);
        }

        if (!zplPrinter.isEmpty()) {
            zplPrinterCombo.setValue(zplPrinter);
        }

        if (!pdfPrinter.isEmpty()) {
            pdfPrinterCombo.setValue(pdfPrinter);
        }

        labelFormatToggle.setSelected(PrinterPreferences.getPreferredFormat() == LabelFormat.PDF);

        scaleWeightMarginField.setText(weightMargin);
    }

    @FXML
    private void toggleSettingsPasswordVisibility() {
        togglePasswordVisibility(settingsPasswordField, settingsPasswordVisible, settingsPasswordIcon);
    }

    private void togglePasswordVisibility(PasswordField passwordField, TextField visibleField, FontIcon icon) {
        if (passwordField.isVisible()) {
            visibleField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visibleField.setVisible(true);
            visibleField.setManaged(true);
            icon.setIconLiteral("mdi2e-eye-off");
        } else {
            passwordField.setText(visibleField.getText());
            visibleField.setVisible(false);
            visibleField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            icon.setIconLiteral("mdi2e-eye");
        }
    }

    @FXML
    private void toggleAdminPasswordVisibility() {
        togglePasswordVisibility(adminPasswordField, adminPasswordVisible, adminPasswordIcon);
    }

    private String getSettingsPasswordValue() {
        return settingsPasswordField.isVisible() ? settingsPasswordField.getText() : settingsPasswordVisible.getText();
    }

    private String getAdminPasswordValue() {
        return adminPasswordField.isVisible() ? adminPasswordField.getText() : adminPasswordVisible.getText();
    }


    private void populatePrinterDropdowns() {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        List<String> zplPrinters = new ArrayList<>();
        List<String> pdfPrinters = new ArrayList<>();

        for (PrintService service : printServices) {
            String printerName = service.getName();

            if (isZplPrinter(printerName)) {
                zplPrinters.add(printerName);
            } else {
                pdfPrinters.add(printerName);
            }
        }

        zplPrinterCombo.getItems().clear();
        if (zplPrinters.isEmpty()) {
            zplPrinterCombo.getItems().add(PrinterPreferences.NO_ZPL_PRINTERS);
        } else {
            zplPrinterCombo.getItems().addAll(zplPrinters);
        }

        pdfPrinterCombo.getItems().clear();
        if (pdfPrinters.isEmpty()) {
            pdfPrinterCombo.getItems().add(PrinterPreferences.NO_PDF_PRINTERS);
        } else {
            pdfPrinterCombo.getItems().addAll(pdfPrinters);
        }
    }

    private void populateScalePortDropdown() {
        SerialPort[] ports = SerialPort.getCommPorts();

        List<String> availablePorts = new ArrayList<>();

        for (SerialPort port : ports) {
            String portInfo = port.getSystemPortName();
            if (port.getPortDescription() != null && !port.getPortDescription().isEmpty()) {
                portInfo += " - " + port.getPortDescription();
            }
            availablePorts.add(portInfo);
        }

        scalePortCombo.getItems().clear();
        if (availablePorts.isEmpty()) {
            scalePortCombo.getItems().add("No serial ports found");
        } else {
            scalePortCombo.getItems().addAll(availablePorts);
        }
    }

    private void updateScaleReadingDisplay(String reading) {
        Platform.runLater(() -> {
            scaleReadingLabel.setText("Current reading: " + reading + " Lb");
        });
    }

    private boolean isZplPrinter(String printerName) {
        String nameLower = printerName.toLowerCase();
        return nameLower.contains("zebra") ||
               nameLower.contains("zpl") ||
               nameLower.contains("zdesigner") ||
               nameLower.contains("zt") ||
               nameLower.contains("zc") ||
               nameLower.contains("lp28") ||
               nameLower.contains("lp24");
    }

    private void setupButtonStateListeners() {
        scalePortCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateScaleButtonStates();
        });

        zplPrinterCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateZplButtonState();
            updateLabelFormatState();
        });

        pdfPrinterCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            updatePdfButtonState();
            updateLabelFormatState();
        });
    }

    private void updateButtonStates() {
        updateScaleButtonStates();
        updateZplButtonState();
        updatePdfButtonState();
        updateLabelFormatState();
    }

    private void updateScaleButtonStates() {
        boolean hasValidSelection = hasValidPortSelection(scalePortCombo);
        weighButton.setDisable(!hasValidSelection);
        zeroButton.setDisable(!hasValidSelection);

        scaleReadingLabel.setVisible(hasValidSelection);
        scaleReadingLabel.setManaged(hasValidSelection);

        if (hasValidSelection && scaleReadingLabel.getText().isEmpty()) {
            scaleReadingLabel.setText("Current reading: -- Lb");
        }
    }

    private void updateZplButtonState() {
        boolean hasValidSelection = hasValidPrinterSelection(zplPrinterCombo);
        zplPrintTestButton.setDisable(!hasValidSelection);
    }

    private void updatePdfButtonState() {
        boolean hasValidSelection = hasValidPrinterSelection(pdfPrinterCombo);
        pdfPrintTestButton.setDisable(!hasValidSelection);
    }

    /**
     * The default format is only a choice when the station has both printers.
     * With one printer the format follows it, so show that and lock the toggle
     * rather than let the page claim a format nothing can print.
     */
    private void updateLabelFormatState() {
        boolean hasZpl = hasValidPrinterSelection(zplPrinterCombo);
        boolean hasPdf = hasValidPrinterSelection(pdfPrinterCombo);
        boolean isChoice = hasZpl && hasPdf;

        labelFormatToggle.setDisable(!isChoice);
        if (!isChoice) {
            labelFormatToggle.setSelected(hasPdf);
        }
    }

    private boolean hasValidPortSelection(ComboBox<String> portCombo) {
        String selection = portCombo.getValue();
        return selection != null &&
               !selection.isEmpty() &&
               !selection.startsWith("No serial ports found");
    }

    private boolean hasValidPrinterSelection(ComboBox<String> printerCombo) {
        return PrinterPreferences.isConfigured(printerCombo.getValue());
    }

    private void showCheckmark(FontIcon checkmarkIcon) {
        Platform.runLater(() -> checkmarkIcon.setVisible(true));

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> checkmarkIcon.setVisible(false));
            }
        }, 4000);
    }

    private void showError(Label errorLabel, String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    errorLabel.setVisible(false);
                    errorLabel.setManaged(false);
                });
            }
        }, 4000);
    }
}
