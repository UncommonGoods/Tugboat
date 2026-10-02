// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import atlantafx.base.controls.ToggleSwitch;
import com.uncommongoods.tugboat.bridge.data.Country;
import com.uncommongoods.tugboat.bridge.data.State;
import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.ports.shipping.model.*;
import com.uncommongoods.tugboat.engine.manifest.CarrierService;
import com.uncommongoods.tugboat.engine.manifest.PickupFacility;
import com.uncommongoods.tugboat.engine.model.TugboatAddress;
import com.uncommongoods.tugboat.engine.model.TugboatParcel;
import com.uncommongoods.tugboat.engine.state.ShipmentComponent;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.animation.RotateTransition;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Bounds;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.fxml.FXMLLoader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.prefs.Preferences;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.bridge.service.TugboatFactory;
import com.uncommongoods.tugboat.bridge.service.ScaleService;
import com.uncommongoods.tugboat.bridge.service.PrinterService;
import com.uncommongoods.tugboat.bridge.service.ShipmentHistoryStore;
import com.uncommongoods.tugboat.bridge.util.AddressFormSupport;
import com.uncommongoods.tugboat.bridge.util.ModalOverlay;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;

import static com.uncommongoods.tugboat.engine.state.ShipmentComponent.DESTINATION_ADDRESS;
import static com.uncommongoods.tugboat.engine.state.ShipmentComponent.PARCELS;
import static com.uncommongoods.tugboat.engine.state.State.*;

public class ShippingController implements Initializable {

    private final ConfigurationManager configManager = ConfigurationManager.getInstance();
    private final TugboatFactory tugboatFactory = new TugboatFactory(configManager);
    private final ScaleService scaleService = ScaleService.getInstance();
    private final PrinterService printerService = new PrinterService();
    private static final Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);
    private static final String DEVICE_PREFS_PREFIX = "tugboat.device.";
    private static final int MAX_CARGO_ID_LENGTH = 72;

    private Tugboat currentTugboat = null;
    private RotateTransition spinAnimation;
    private boolean addressFieldsChanged = false;
    private boolean parcelChanged = false;
    private boolean shipmentOriginReady = false;
    private boolean manualEntryMode = false;

    private String currentScaleWeight = "0.0";
    private Timeline weightLabelUpdateTimer;

    @FXML
    private Button rateButton;

    @FXML
    private Button shipButton;

    @FXML
    private ToggleSwitch autoShipToggle;

    @FXML
    private HBox autoShipBox;

    @FXML
    private TextField cargoIdField;

    @FXML
    private Button loadButton;

    @FXML
    private TextField weightField;

    @FXML
    private Label weightLabel;

    @FXML
    private TextField dimensionsField;

    @FXML
    private ComboBox<IRate> serviceCombo;

    @FXML
    private TextField totalChargeField;

    @FXML
    private TextField expectedDateField;

    @FXML
    private TextField nameField;

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
    private ScrollPane mainScrollPane;

    @FXML
    private Label addBoxButton;

    @FXML
    private VBox multiBoxContainer;

    @FXML
    private VBox shippedStatusBox;

    @FXML
    private HBox originWarningBox;

    @FXML
    private VBox localHistorySection;

    @FXML
    private TableView<HistoryItem> localHistoryTable;

    @FXML
    private TableColumn<HistoryItem, String> localShipmentIdColumn;

    @FXML
    private TableColumn<HistoryItem, String> localCarrierColumn;

    @FXML
    private TableColumn<HistoryItem, String> localServiceColumn;

    @FXML
    private TableColumn<HistoryItem, String> localChargeColumn;

    @FXML
    private TableColumn<HistoryItem, String> localTrackingColumn;

    @FXML
    private TableColumn<HistoryItem, Button> localPrintColumn;

    @FXML
    private TableColumn<HistoryItem, Button> localVoidColumn;

    private int boxCounter = 1;
    private final List<VBox> boxContainers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceCombo.setPromptText("not yet rated");
        // The combo holds the rate itself; this only renders it. Nothing parses the label back.
        serviceCombo.setConverter(new StringConverter<IRate>() {
            @Override
            public String toString(IRate rate) {
                return rate == null ? "" : formatRateLabel(rate);
            }

            @Override
            public IRate fromString(String label) {
                return null;
            }
        });

        manualEntryMode = !configManager.isCacheConfigured();

        checkShipmentOrigin();

        AddressFormSupport.configureCountryCombo(countryCombo);
        countryCombo.setValue(Country.getDefault());

        AddressFormSupport.configureStateCombo(stateCombo);

        autoShipToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                System.out.println("Auto-ship enabled");
                addBoxButton.setVisible(false);
                addBoxButton.setManaged(false);

            } else {
                System.out.println("Auto-ship disabled");
                addBoxButton.setVisible(true);
                addBoxButton.setManaged(true);
            }
            updateButtonStates();
            resetChangeTracking();
        });
        // Auto-ship loads a cargo id and ships it in one step; with nothing to
        // load from, it stays off and out of the way, as does LOAD itself.
        if (manualEntryMode) {
            autoShipBox.setVisible(false);
            autoShipBox.setManaged(false);
            loadButton.setVisible(false);
            loadButton.setManaged(false);
            // With LOAD gone, the cargo id field takes over the width the button
            // was using so it lines up with the rest of the panel.
            cargoIdField.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(cargoIdField, Priority.ALWAYS);
            cargoIdField.getStyleClass().removeAll("cargo-id-field", "left-pill");
            cargoIdField.getStyleClass().add("rounded");
        } else {
            autoShipToggle.setSelected(true);
        }

        configureWeightEntry();

        setupCargoIdField();

        setupChangeTracking();

        setupServiceSelection();

        startScaleMonitoring();

        startWeightLabelTimer();

        setupLocalHistoryTable();
        ShipmentHistoryStore.getInstance().addListener("shipping", this::refreshLocalHistory);
        refreshLocalHistory();
    }

    private void setupLocalHistoryTable() {
        localShipmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("shipmentId"));
        localCarrierColumn.setCellValueFactory(new PropertyValueFactory<>("carrier"));
        localServiceColumn.setCellValueFactory(new PropertyValueFactory<>("service"));
        localChargeColumn.setCellValueFactory(new PropertyValueFactory<>("charge"));
        localTrackingColumn.setCellValueFactory(new PropertyValueFactory<>("trackingNumber"));
        localPrintColumn.setCellValueFactory(new PropertyValueFactory<>("printButton"));
        localVoidColumn.setCellValueFactory(new PropertyValueFactory<>("voidButton"));
    }

    private void refreshLocalHistory() {
        List<Tugboat> recent = ShipmentHistoryStore.getInstance().getRecent(5);
        if (recent.isEmpty()) {
            localHistorySection.setVisible(false);
            localHistorySection.setManaged(false);
        } else {
            localHistorySection.setVisible(true);
            localHistorySection.setManaged(true);
            var items = FXCollections.<HistoryItem>observableArrayList();
            for (Tugboat t : recent) {
                items.add(HistoryItem.fromTugboat(t));
            }
            localHistoryTable.setItems(items);
            localHistoryTable.setFixedCellSize(40);
            // header (~30) + rows * cellSize + small buffer
            localHistoryTable.setPrefHeight(30 + items.size() * 40 + 2);
            localHistoryTable.setMinHeight(30 + items.size() * 40 + 2);
        }
    }

    private void setupCargoIdField() {
        cargoIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > MAX_CARGO_ID_LENGTH) {
                cargoIdField.setText(newValue.substring(0, MAX_CARGO_ID_LENGTH));
            }
        });

        cargoIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateButtonStates();
        });

        cargoIdField.setOnKeyPressed(event -> {
            // Nothing to load and no auto-ship in manual entry: the id is just a
            // reference the operator types, so Enter has nowhere to go.
            if (manualEntryMode) {
                return;
            }
            // Auto-ship leaves the buttons disabled and ships from here instead,
            // so this path needs the same origin guard they get.
            if (event.getCode().toString().equals("ENTER") && hasCargoId() && shipmentOriginReady) {
                if (autoShipToggle.isSelected()) {
                    shipShipment();
                } else {
                    loadShipment();
                }
            }
        });

        updateButtonStates();
    }

    /**
     * Cargo ids are free-form strings, so any non-blank entry is submittable; the
     * carrier decides whether it resolves to a shipment.
     */
    private boolean hasCargoId() {
        return !cargoIdField.getText().isBlank();
    }

    /**
     * The cargo id to ship under. Typed-in shipments answer to nothing upstream,
     * so a blank id gets one made up here — and shown in the field, since it ends
     * up on the label as the reference and on the error slip as a barcode.
     *
     * <p>FX thread only.
     */
    private String resolveCargoId() {
        if (manualEntryMode && !hasCargoId()) {
            String generated = "TB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            cargoIdField.setText(generated);
            return generated;
        }
        return cargoIdField.getText();
    }

    /**
     * Work out whether shipments have an origin — a pickup facility with an
     * address, or the address saved on the Set Origin page — and say so on the
     * page when they do not. Without one the engine refuses to initialize a
     * Tugboat, so there is nothing useful the buttons could do.
     */
    private void checkShipmentOrigin() {
        PickupFacility pickupFacility = getPickupFacility();
        shipmentOriginReady = (pickupFacility != null && pickupFacility.getAddress() != null)
            || configManager.getOriginAddress() != null;

        originWarningBox.setVisible(!shipmentOriginReady);
        originWarningBox.setManaged(!shipmentOriginReady);
    }

    private void updateButtonStates() {
        if (!shipmentOriginReady) {
            loadButton.setDisable(true);
            rateButton.setDisable(true);
            shipButton.setDisable(true);
            return;
        }

        boolean hasValidId = hasCargoId();
        boolean autoShipEnabled = autoShipToggle.isSelected();

        // Typed-in shipments are rateable as soon as there is an origin. the cargo id is optional here and generated when left blank.
        if (manualEntryMode) {
            loadButton.setDisable(true);
            boolean printed = currentTugboat != null && currentTugboat.getPackageState() != null &&
                currentTugboat.getPackageState().getState().equals(PRINTED);
            rateButton.setDisable(printed);
            shipButton.setDisable(printed);
            return;
        }

        if (autoShipEnabled) {
            loadButton.setDisable(true);
            rateButton.setDisable(true);
            shipButton.setDisable(true);
        } else {
            loadButton.setDisable(!hasValidId);

            boolean shouldEnableRateButton = currentTugboat != null || hasValidId;
            if (currentTugboat != null && currentTugboat.getPackageState() != null &&
                currentTugboat.getPackageState().getState().equals(PRINTED)) {
                shouldEnableRateButton = false;
            }
            rateButton.setDisable(!shouldEnableRateButton);
            shipButton.setDisable(!shouldEnableRateButton);
        }
    }

    private void loadShipmentForAutoShip() {
        String cargoId = cargoIdField.getText();
        if (cargoId.length() != 8) {
            System.err.println("Invalid cargo ID for auto-ship: must be exactly 8 digits");
            return;
        }
        executeLoadShipmentForAutoShip(cargoId);
    }

    private void executeLoadShipmentForAutoShip(String cargoId) {
        RotateTransition loadSpinAnimation = animateStateTransitionButton(shipButton);

        Task<Void> loadAndShipTask = getShipTask(cargoId, loadSpinAnimation, null, null);

        Thread backgroundThread = new Thread(loadAndShipTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private RotateTransition animateStateTransitionButton(Button button) {
        button.setDisable(true);

        FontIcon spinIcon = new FontIcon(MaterialDesignL.LOADING);
        spinIcon.setIconSize(12);
        button.setGraphic(spinIcon);
        button.setText("");

        RotateTransition loadSpinAnimation = new RotateTransition(Duration.seconds(1), spinIcon);
        loadSpinAnimation.setByAngle(360);
        loadSpinAnimation.setCycleCount(RotateTransition.INDEFINITE);
        loadSpinAnimation.play();
        return loadSpinAnimation;
    }

    private void setupChangeTracking() {
        ChangeListener<String> addressListener = (obs, oldVal, newVal) -> {
            if (!autoShipToggle.isSelected()) {
                addressFieldsChanged = true;
            }
        };
        ChangeListener<String> parcelListener = (obs, oldVal, newVal) -> {
            if (!autoShipToggle.isSelected()) {
                parcelChanged = true;
            }
        };
        nameField.textProperty().addListener(addressListener);
        address1Field.textProperty().addListener(addressListener);
        address2Field.textProperty().addListener(addressListener);
        cityField.textProperty().addListener(addressListener);
        postalCodeField.textProperty().addListener(addressListener);
        phoneField.textProperty().addListener(addressListener);

        countryCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!autoShipToggle.isSelected()) {
                addressFieldsChanged = true;
            }
        });
        stateCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!autoShipToggle.isSelected()) {
                addressFieldsChanged = true;
            }
        });

        dimensionsField.textProperty().addListener(parcelListener);
        weightField.textProperty().addListener(parcelListener);

        dimensionsField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER") && !boxContainers.isEmpty()) {
                showBoxWeightCaptureModal(0);
            }
        });
    }

    private void setupServiceSelection() {
        serviceCombo.valueProperty().addListener((obs, oldVal, newVal) ->
            Optional.ofNullable(newVal)
                .map(ShippingController::formatCharge)
                .ifPresent(totalChargeField::setText)
        );
    }

    private static String formatCharge(IRate rate) {
        return "$" + String.format("%.2f", rate.getRate());
    }

    private static String formatRateLabel(IRate rate) {
        return rate.getCarrier() + " " + rate.getService() + " " + formatCharge(rate);
    }

    private void showButtonError(Button button, String originalText) {
        button.setGraphic(null);
        button.setText("❌");
        button.setStyle("-fx-background-color: -color-danger-subtle; -fx-text-fill: -color-danger-emphasis; -fx-font-weight: bold;");

        // After 3 seconds remove error indicator
        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> {
            button.setGraphic(null);
            button.setText(originalText);
            button.setStyle("");
            button.setDisable(false);
        });
        pause.play();
    }

    private CarrierService getSelectedCarrierService() {
        return Optional.ofNullable(serviceCombo.getValue())
            .map(rate -> new CarrierService(rate.getCarrier(), rate.getService()))
            .orElse(null);
    }

    @FXML
    private void rateShipment() {
        if (effectiveWeightLbs() <= 0) {
            reportInputError(rateButton, "RATE", weightErrorMessage());
            return;
        }

        if (manualEntryMode) {
            String problem = manualEntryError();
            if (problem != null) {
                reportInputError(rateButton, "RATE", problem);
                return;
            }
        }

        String cargoIdText = resolveCargoId();
        // The form is the whole shipment in manual entry, so it is read here on
        // the FX thread and handed to the task.
        TugboatAddress formAddress = manualEntryMode ? addressFromForm() : null;
        List<IParcel> formParcels = manualEntryMode ? parcelsFromForm() : null;

        if (currentTugboat != null || manualEntryMode || hasCargoId()) {
            rateButton.setDisable(true);

            FontIcon spinIcon = new FontIcon(MaterialDesignL.LOADING);
            spinIcon.setIconSize(12);
            rateButton.setGraphic(spinIcon);
            rateButton.setText("");

            spinAnimation = new RotateTransition(Duration.seconds(1), spinIcon);
            spinAnimation.setByAngle(360);
            spinAnimation.setCycleCount(RotateTransition.INDEFINITE);
            spinAnimation.play();

            Task<Void> rateTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    boolean builtFromForm = false;
                    if (currentTugboat == null) {
                        if (manualEntryMode) {
                            currentTugboat = tugboatFactory.rebuild(cargoIdText, formAddress, formParcels);
                            builtFromForm = true;
                        } else {
                            currentTugboat = tugboatFactory.load(cargoIdText);
                        }
                    }

                    List<ShipmentComponent> preserve = preservedComponents();
                    // A Tugboat built from the form is already carrying the edits.
                    Tugboat tugboatToRate = builtFromForm ? currentTugboat : applyUserEdits(cargoIdText, preserve);

                    tugboatToRate.retrieve(preserve);
                    tugboatFactory.ensureHooksApplied(tugboatToRate);

                    // A purchased label has to be voided before the shipment can be rated again. anything earlier just resets.
                    if (tugboatToRate.getPackageState() != null &&
                        tugboatToRate.getPackageState().getState().equals(PURCHASED)) {
                        tugboatToRate.voidLabel(preserve);
                        System.out.println("Tugboat was PURCHASED, voided before rating");
                    } else {
                        tugboatToRate.reset();
                    }

                    tugboatToRate.rate(preserve);
                    System.out.println("Tugboat rated successfully");

                    Platform.runLater(() -> {
                        resetChangeTracking();
                        populateServiceDropdown(currentTugboat);
                        updateExpectedDateField(currentTugboat);
                        // Populate form if fields are empty (tugboat was loaded during rate)
                        if (nameField.getText().isEmpty() && address1Field.getText().isEmpty()) {
                            populateFormFromTugboat(currentTugboat);
                        }
                    });
                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        if (spinAnimation != null) {
                            spinAnimation.stop();
                        }

                        FontIcon checkIcon = new FontIcon(MaterialDesignC.CHECK);
                        checkIcon.setIconSize(12);
                        rateButton.setGraphic(checkIcon);
                        rateButton.setText("");

                        PauseTransition pause = new PauseTransition(Duration.seconds(3));
                        pause.setOnFinished(e -> {
                            rateButton.setGraphic(null);
                            rateButton.setText("RATE");
                            rateButton.setDisable(false);
                        });
                        pause.play();
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        if (spinAnimation != null) {
                            spinAnimation.stop();
                        }

                        showButtonError(rateButton, "RATE");

                        String errorMessage = "Error rating tugboat: " + getException().getMessage();
                        System.err.println(errorMessage);
                        getException().printStackTrace();

                        printerService.printErrorMessage(errorMessage);
                    });
                }
            };

            Thread backgroundThread = new Thread(rateTask);
            backgroundThread.setDaemon(true);
            backgroundThread.start();

        } else {
            System.err.println("No tugboat loaded to rate");
        }
    }

    @FXML
    private void shipShipment() {
        if (effectiveWeightLbs() <= 0) {
            reportInputError(shipButton, "SHIP", weightErrorMessage());
            return;
        }

        if (autoShipToggle.isSelected()) {
            String currentCargoId = cargoIdField.getText();
            if (currentTugboat == null || !currentCargoId.equals(currentTugboat.getCargoId())) {
                loadShipmentForAutoShip();
                return;
            }
        }
        executeShipShipment();
    }

    private void executeShipShipment() {
        if (manualEntryMode) {
            String problem = manualEntryError();
            if (problem != null) {
                reportInputError(shipButton, "SHIP", problem);
                return;
            }
        }

        mainScrollPane.setVvalue(0);
        // Allow shipping if we have a tugboat OR if we have a cargo ID
        String cargoIdText = resolveCargoId();
        TugboatAddress formAddress = manualEntryMode ? addressFromForm() : null;
        List<IParcel> formParcels = manualEntryMode ? parcelsFromForm() : null;

        if (currentTugboat != null || manualEntryMode || hasCargoId()) {
            RotateTransition shipSpinAnimation = animateStateTransitionButton(shipButton);

            Task<Void> shipTask = getShipTask(cargoIdText, shipSpinAnimation, formAddress, formParcels);

            Thread backgroundThread = new Thread(shipTask);
            backgroundThread.setDaemon(true);
            backgroundThread.start();

        } else {
            System.err.println("No tugboat loaded to ship");
        }
    }

    Task<Void> getShipTask(String cargoId, RotateTransition animation,
                           TugboatAddress formAddress, List<IParcel> formParcels) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (scaleService.isScaleConfigured()) {
                    double scaleWeight = Double.parseDouble(currentScaleWeight.equals("--") ? "0" : currentScaleWeight);
                    if (scaleWeight <= 0) {
                        throw new TugboatException("Scale weight is zero. Lift package and re-place it on the scale.");
                    }
                }

                boolean builtFromForm = false;
                if (needsShipmentData(currentTugboat)) {
                    if (manualEntryMode) {
                        currentTugboat = tugboatFactory.rebuild(cargoId, formAddress, formParcels);
                        builtFromForm = true;
                    } else {
                        currentTugboat = tugboatFactory.load(cargoId);
                    }
                }

                List<ShipmentComponent> preserve = preservedComponents();
                if (!builtFromForm) {
                    applyUserEdits(cargoId, preserve);
                }

                tugboatFactory.ensureHooksApplied(currentTugboat);

                CarrierService selectedService = getSelectedCarrierService();
                if (selectedService != null) {
                    currentTugboat.getOptions().setSelectedCarrierService(selectedService);
                }

                // Get weight from tugboat's parcel data
                double parcelWeight = 0.0;
                if (currentTugboat.getParcels() != null && !currentTugboat.getParcels().isEmpty()) {
                    IParcel firstParcel = currentTugboat.getParcels().getFirst();
                    if (firstParcel.getWeight() != null) {
                        parcelWeight = firstParcel.getWeight() / 16.0; // Convert oz to lbs
                    }
                }

                CountDownLatch shippingCompleteLatch = new CountDownLatch(1);
                AtomicReference<Throwable> shippingError = new AtomicReference<>();

                performShippingWithWeightValidation(currentTugboat, cargoId, parcelWeight, preserve, shippingCompleteLatch, shippingError);

                // block until the latch is released
                try {
                    shippingCompleteLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Shipping interrupted", e);
                }

                if (shippingError.get() != null) {
                    Throwable error = shippingError.get();
                    if (error instanceof Exception) {
                        throw (Exception) error;
                    } else {
                        throw new RuntimeException("Shipping failed", error);
                    }
                }

                return null;
            }

            @Override
            protected void succeeded() {
                animation.stop();

                ShipmentHistoryStore.getInstance().add(currentTugboat);

                populateFormFromTugboat(currentTugboat);
                populateServiceDropdown(currentTugboat);
                updateExpectedDateField(currentTugboat);

                // Fill service dropdown after successful auto-ship
                if (currentTugboat.getSelectedRate() != null) {
                    serviceCombo.setValue(currentTugboat.getSelectedRate());
                }

                FontIcon checkIcon = new FontIcon(MaterialDesignC.CHECK);
                checkIcon.setIconSize(12);
                shipButton.setGraphic(checkIcon);
                shipButton.setText("");

                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                pause.setOnFinished(e -> {
                    shipButton.setGraphic(null);
                    shipButton.setText("SHIP");
                    shipButton.setDisable(false);
                    currentTugboat = null;
                    clearInputs();
                });
                pause.play();
            }

            @Override
            protected void failed() {
                animation.stop();
                showButtonError(shipButton, "SHIP");

                if (!(getException() instanceof ShippingCancelledException)) {
                    String errorMessage = getException().getMessage();
                    System.err.println(errorMessage);
                    getException().printStackTrace();

                    printerService.printErrorMessage(errorMessage);
                }

                currentTugboat = null;
                clearInputs();
            }
        };
    }

    @FXML
    private void loadShipment() {
        String cargoId = cargoIdField.getText();
        if (cargoId.length() != 8) {
            System.err.println("Invalid cargo ID: must be exactly 8 digits");
            return;
        }

        RotateTransition loadSpinAnimation = animateStateTransitionButton(loadButton);

        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                currentTugboat = tugboatFactory.load(cargoId);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    loadSpinAnimation.stop();

                    FontIcon checkIcon = new FontIcon(MaterialDesignC.CHECK);
                    checkIcon.setIconSize(12);
                    loadButton.setGraphic(checkIcon);
                    loadButton.setText("");

                    populateFormFromTugboat(currentTugboat);

                    populateServiceDropdown(currentTugboat);
                    updateExpectedDateField(currentTugboat);

                    updateButtonStates();

                    PauseTransition pause = new PauseTransition(Duration.seconds(2));
                    pause.setOnFinished(e -> {
                        loadButton.setGraphic(null);
                        loadButton.setText("LOAD");
                        loadButton.setDisable(false);
                    });
                    pause.play();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    loadSpinAnimation.stop();
                    showButtonError(loadButton, "LOAD");
                    PauseTransition delayedUpdate = new PauseTransition(Duration.seconds(3));
                    delayedUpdate.setOnFinished(e -> updateButtonStates());
                    delayedUpdate.play();

                    String errorMessage = "Error loading tugboat: " + getException().getMessage();
                    System.err.println(errorMessage);
                    getException().printStackTrace();

                    printerService.printErrorMessage(errorMessage);

                    currentTugboat = null;
                });
            }
        };

        Thread backgroundThread = new Thread(loadTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void clearInputs(boolean clearTugboat) {
        cargoIdField.clear();
        weightField.setText("0");
        dimensionsField.setText("0X0X0");
        serviceCombo.setValue(null);
        totalChargeField.clear();
        expectedDateField.clear();

        nameField.clear();
        address1Field.clear();
        address2Field.clear();
        countryCombo.setValue(Country.getDefault());
        postalCodeField.clear();
        cityField.clear();
        stateCombo.setValue(null);
        phoneField.clear();

        boxContainers.clear();
        boxCounter = 1;
        syncBoxContainersToUI();

        shippedStatusBox.setVisible(false);

        cargoIdField.requestFocus();
        if (clearTugboat) {
            this.currentTugboat = null;
        }

        // A cleared form is a fresh start. the cargo id no longer says anything
        // about what the buttons should do, so follow the usual rules.
        if (manualEntryMode) {
            resetChangeTracking();
            updateButtonStates();
        } else {
            rateButton.setDisable(true);
            shipButton.setDisable(true);
        }
    }

    private void clearInputs() {
        this.clearInputs(false);
    }

    @FXML
    private void clearInputsAndTugboat() {
        this.clearInputs(true);
    }

    @FXML
    private void addBox(MouseEvent event) {
        boxCounter++;

        VBox boxContainer = new VBox(16);
        boxContainer.getStyleClass().addAll("shipment-info-panel");
        boxContainer.setPadding(new Insets(20));

        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setSpacing(10);

        Label boxTitle = new Label("Box " + boxCounter);
        boxTitle.getStyleClass().addAll("section-title", "title-4");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label removeButton = new Label("×");
        removeButton.getStyleClass().add("remove-box-button");
        removeButton.setOnMouseClicked(e -> removeBox(boxContainer));

        header.getChildren().addAll(boxTitle, spacer, removeButton);


        HBox fieldsRow = new HBox(10);

        VBox weightSection = new VBox(4);
        weightSection.setPrefWidth(100);
        Label weightLabel = new Label("weight (lbs)");
        weightLabel.getStyleClass().addAll("field-label", "text-caption");
        TextField weightField = new TextField("0");
        weightField.getStyleClass().addAll("rounded", "weight-field-readonly");
        weightField.setEditable(false);
        weightField.textProperty().addListener((obs, oldVal, newVal) -> parcelChanged = true);
        weightSection.getChildren().addAll(weightLabel, weightField);


        VBox dimsSection = new VBox(4);
        HBox.setHgrow(dimsSection, Priority.ALWAYS);
        Label dimsLabel = new Label("package dimensions (in)");
        dimsLabel.getStyleClass().addAll("field-label", "text-caption");
        TextField dimsField = new TextField("0X0X0");
        dimsField.getStyleClass().add("rounded");
        dimsField.textProperty().addListener((obs, oldVal, newVal) -> parcelChanged = true);
        dimsField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().toString().equals("ENTER")) {
                int runtimeIndex = boxContainers.indexOf(boxContainer);
                if (runtimeIndex >= 0) {
                    showBoxWeightCaptureModal(runtimeIndex + 1);
                }
            }
        });
        dimsSection.getChildren().addAll(dimsLabel, dimsField);

        fieldsRow.getChildren().addAll(weightSection, dimsSection);


        boxContainer.getChildren().addAll(header, fieldsRow);


        VBox.setMargin(boxContainer, new Insets(15, 0, 0, 0));

        boxContainers.add(boxContainer);
        syncBoxContainersToUI();
        scrollToNode(boxContainer);
        dimensionsField.requestFocus();

        System.out.println("Added Box " + boxCounter);
    }

    private void removeBox(VBox boxContainer) {
        boxContainers.remove(boxContainer);
        renumberBoxContainers();
        syncBoxContainersToUI();
        Optional.ofNullable(currentTugboat)
            .map(Tugboat::getParcels)
            .filter(p -> p.size() > 1)
            .ifPresent(List::removeLast);
        System.out.println("Removed box and renumbered remaining boxes");
    }

    private void renumberBoxContainers() {
        AtomicInteger counter = new AtomicInteger(2);

        boxContainers.forEach(boxContainer ->
            boxContainer.getChildren().stream()
                .filter(HBox.class::isInstance)
                .map(HBox.class::cast)
                .findFirst()
                .flatMap(header ->
                    header.getChildren().stream()
                        .filter(Label.class::isInstance)
                        .map(Label.class::cast)
                        .filter(label -> label.getText().startsWith("Box "))
                        .findFirst())
                .ifPresent(label -> label.setText("Box " + counter.getAndIncrement())));

        boxCounter = counter.get() - 1;
    }

    private void syncBoxContainersToUI() {
        multiBoxContainer.getChildren().clear();
        multiBoxContainer.getChildren().addAll(boxContainers);
    }

    private void scrollToNode(Node node) {
        Platform.runLater(() -> {
            Node content = mainScrollPane.getContent();
            Bounds contentBounds = content.getBoundsInLocal();
            Bounds nodeBoundsInContent = content.sceneToLocal(
                node.localToScene(node.getBoundsInLocal()));

            double contentHeight = contentBounds.getHeight();
            double viewportHeight = mainScrollPane.getViewportBounds().getHeight();

            if (contentHeight <= viewportHeight) return;

            double nodeCenter = nodeBoundsInContent.getMinY()
                + nodeBoundsInContent.getHeight() / 2;
            double targetVvalue = (nodeCenter - viewportHeight / 2)
                / (contentHeight - viewportHeight);
            mainScrollPane.setVvalue(Math.max(0.0, Math.min(1.0, targetVvalue)));
        });
    }

    private PickupFacility getPickupFacility() {
        return configManager.getPickupFacility();
    }

    private void resetChangeTracking() {
        addressFieldsChanged = false;
        parcelChanged = false;
    }

    private boolean hasUserEdits() {
        return addressFieldsChanged || parcelChanged;
    }

    /**
     * The components a retrieve must not clobber. Only user edits are worth
     * protecting; otherwise the cache is the better source.
     */
    private List<ShipmentComponent> preservedComponents() {
        return hasUserEdits() ? List.of(DESTINATION_ADDRESS, PARCELS) : Collections.emptyList();
    }

    private TugboatAddress addressFromForm() {
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("name", nameField.getText());
        addressMap.put("street1", address1Field.getText());
        addressMap.put("street2", address2Field.getText());
        addressMap.put("city", cityField.getText());
        addressMap.put("zip", postalCodeField.getText());
        addressMap.put("phone", phoneField.getText());

        if (countryCombo.getValue() != null) {
            addressMap.put("country", countryCombo.getValue().getCode());
        }
        if (stateCombo.getValue() != null) {
            addressMap.put("state", stateCombo.getValue().getAbbreviation());
        }

        return new TugboatAddress(addressMap);
    }

    private List<IParcel> parcelsFromForm() {
        return collectAllParcelsFromUI().stream()
            .map(IParcel.class::cast)
            .collect(Collectors.toList());
    }

    /**
     * Whether a shipment still has to be fetched or built. A Tugboat with no
     * destination has nothing to ship.
     */
    private boolean needsShipmentData(Tugboat tugboat) {
        if (tugboat == null || tugboat.getDestinationAddress() == null) {
            return true;
        }
        String street1 = tugboat.getDestinationAddress().getStreet1();
        return street1 == null || street1.isBlank();
    }

    /**
     * What is missing from a typed-in shipment, or null when it is ready to go.
     * The engine would catch all of this, but it answers with an exception the
     * error hook has wrapped in a ZPL label; this says it in plain words instead.
     */
    private String manualEntryError() {
        if (isBlank(nameField)) {
            return "A destination name is required";
        }
        if (isBlank(address1Field)) {
            return "A destination address 1 is required";
        }
        if (isBlank(cityField)) {
            return "A destination city is required";
        }
        if (countryCombo.getValue() == null) {
            return "Select a destination country";
        }
        if (stateCombo.getValue() == null && countryCombo.getValue() == Country.getDefault()) {
            return "Select a destination state";
        }
        if (isBlank(postalCodeField)) {
            return "A destination postal code is required";
        }
        if (parseDimensions(dimensionsField.getText()) == null) {
            // collectAllParcelsFromUI drops the parcel when this does not parse
            return "Package dimensions must look like 12x8x4";
        }
        return null;
    }

    private boolean isBlank(TextField field) {
        return field.getText() == null || field.getText().isBlank();
    }

    /*Put a bad-input message in front of the packer the same way a failed
    shipment is: red on the button, and on whichever printer is configured.*/
    private void reportInputError(Button button, String buttonText, String message) {
        showButtonError(button, buttonText);
        System.err.println(message);
        printerService.printErrorMessage(message);
    }

    private String weightErrorMessage() {
        return scaleService.isScaleConfigured()
            ? "Scale weight is zero. Lift package and re-place it on the scale."
            : "Enter a package weight.";
    }

    /**
     * Fold whatever the user typed into {@link #currentTugboat}, rebuilding it so
     * the edits win over the cached values. A purchased shipment is voided first,
     * since its label no longer describes what is being shipped.
     */
    private Tugboat applyUserEdits(String cargoId, List<ShipmentComponent> preserve) throws TugboatException {
        if (!hasUserEdits()) {
            return currentTugboat;
        }

        if (currentTugboat.getPackageState() != null &&
            currentTugboat.getPackageState().getState().equals(PURCHASED)) {
            currentTugboat.voidLabel(preserve);
            System.out.println("Tugboat was PURCHASED with changes, voided before rebuilding");
        }

        TugboatAddress address = addressFieldsChanged ? addressFromForm() : null;
        List<IParcel> parcels = parcelChanged ? parcelsFromForm() : currentTugboat.getParcels();

        currentTugboat = tugboatFactory.rebuild(cargoId, address, parcels);
        return currentTugboat;
    }

    private Float[] parseDimensions(String dimensionsStr) {
        if (dimensionsStr == null || !dimensionsStr.matches("^((\\d*\\.\\d*)|\\d+)[Xx]((\\d*\\.\\d*)|\\d+)[Xx]((\\d*\\.\\d*)|\\d+)$")) {
            return null;
        }

        String[] parts = dimensionsStr.split("[Xx]");
        Float[] dimensions = new Float[3];

        try {
            for (int i = 0; i < 3; i++) {
                dimensions[i] = Float.parseFloat(parts[i]);
            }
            return dimensions;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<TugboatParcel> collectAllParcelsFromUI() {
        List<TugboatParcel> parcels = new ArrayList<>();

        // First parcel
        String mainWeightText = weightField.getText();
        String mainDimensionsText = dimensionsField.getText();

        Float mainWeight = null;
        try {
            mainWeight = Float.parseFloat(mainWeightText) * 16;
        } catch (NumberFormatException e) {
            mainWeight = 0.0f;
        }

        Float[] mainDimensions = parseDimensions(mainDimensionsText);
        if (mainDimensions != null) {
            TugboatParcel mainParcel = new TugboatParcel(mainWeight, mainDimensions[0], mainDimensions[1], mainDimensions[2]);
            parcels.add(mainParcel);
        }

        // Additional parcels
        int boxNumber = 2;
        for (VBox boxContainer : boxContainers) {
            TugboatParcel parcel = extractParcelFromBoxContainer(boxContainer);
            if (parcel != null) {
                parcels.add(parcel);
            } else {
                System.err.println("Failed to extract parcel from Box " + boxNumber + " - check weight/dimensions format");
            }
            boxNumber++;
        }

        System.out.println("Collected " + parcels.size() + " parcel(s) from UI (1 main + " + (parcels.size() - 1) + " additional)");
        return parcels;
    }

    private TugboatParcel extractParcelFromBoxContainer(VBox boxContainer) {
        TextField weightField = null;
        TextField dimensionsField = null;

        // Navigate through the box container structure to find the weight and dimensions fields
        for (Node child : boxContainer.getChildren()) {
            if (child instanceof HBox fieldsRow) {
                if (fieldsRow.getChildren().size() >= 2) {
                    Node firstChild = fieldsRow.getChildren().get(0);
                    Node secondChild = fieldsRow.getChildren().get(1);

                    // Check if this looks like the fields row
                    if (firstChild instanceof VBox weightSection &&
                        secondChild instanceof VBox dimsSection) {

                        for (Node weightChild : weightSection.getChildren()) {
                            if (weightChild instanceof TextField) {
                                weightField = (TextField) weightChild;
                                break;
                            }
                        }

                        for (Node dimsChild : dimsSection.getChildren()) {
                            if (dimsChild instanceof TextField) {
                                dimensionsField = (TextField) dimsChild;
                                break;
                            }
                        }

                        if (weightField != null && dimensionsField != null) {
                            break;
                        }
                    }
                }
            }
        }

        if (weightField == null || dimensionsField == null) {
            System.err.println("Could not find weight or dimensions field in box container");
            return null;
        }

        try {
            Float weight = Float.parseFloat(weightField.getText()) * 16;
            Float[] dimensions = parseDimensions(dimensionsField.getText());

            if (dimensions != null) {
                return new TugboatParcel(weight, dimensions[0], dimensions[1], dimensions[2]);
            }
        } catch (NumberFormatException e) {
            System.err.println("Error parsing parcel data from box container: " + e.getMessage());
        }

        return null;
    }

    private void updateExpectedDateField(Tugboat tugboat) {
        if (tugboat == null || tugboat.getExpectedDeliveryDate() == null) {
            expectedDateField.clear();
            return;
        }

        LocalDate deliveryDate = tugboat.getExpectedDeliveryDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        expectedDateField.setText(deliveryDate.format(formatter));
    }

    private void populateServiceDropdown(Tugboat tugboat) {
        if (tugboat == null || tugboat.getRates() == null) {
            serviceCombo.setPromptText("not yet rated");
            serviceCombo.getItems().clear();
            serviceCombo.setDisable(true);
            return;
        }

        try {
            Set<IRate> rates;
            if (tugboat.getParcels().size() > 1) {
                rates = dedupeByCarrierService(tugboat.getOrderRateResponses().stream()
                    .flatMap(order -> order.getRates().stream()));
            } else {
                rates = dedupeByCarrierService(tugboat.getShipmentRateResponses().stream()
                    .flatMap(shipment -> shipment.getRates().stream()));
            }

            if (!rates.isEmpty() && getPickupFacility() != null) {
                Set<CarrierService> pickupGroupServices = getPickupFacility().getPickupGroups().stream()
                    .flatMap(pickupGroup -> pickupGroup.getAvailableServices().stream())
                    .collect(Collectors.toSet());
                rates = rates.stream()
                    .filter(rate -> pickupGroupServices.contains(new CarrierService(rate.getCarrier(), rate.getService())))
                    .collect(Collectors.toSet());
            }

            // The rate itself is the item. the converter renders it. Sorted by carrier desc then service.
            serviceCombo.getItems().clear();
            rates.stream()
                .sorted(Comparator.comparing(IRate::getCarrier, Comparator.reverseOrder())
                    .thenComparing(IRate::getService))
                .forEach(serviceCombo.getItems()::add);

            boolean isRatedOrLess = tugboat.getPackageState() == null ||
                                   tugboat.getPackageState().getState().ordinal() <= RATED.ordinal();

            if (isRatedOrLess) {
                serviceCombo.setDisable(false);
                if (!serviceCombo.getItems().isEmpty()) {
                    serviceCombo.setPromptText("Select a service");
                } else {
                    serviceCombo.setPromptText("No services available");
                }
            } else {
                serviceCombo.setDisable(true);
                if (tugboat.getSelectedRate() != null) {
                    serviceCombo.setValue(tugboat.getSelectedRate());
                } else {
                    serviceCombo.setPromptText("Service selected");
                }
            }
        } catch (Exception e) {
            System.err.println("Error populating service dropdown: " + e.getMessage());
            serviceCombo.setPromptText("Error loading services");
            serviceCombo.setDisable(true);
        }
    }

    /*One entry per carrier/service. Keeps the cheapest duplicate, since RatedTugboatState
    shops the lowest rate among matches -- otherwise the displayed price could differ
    from what is charged.*/
    private Set<IRate> dedupeByCarrierService(Stream<IRate> rates) {
        return new HashSet<>(rates
            .filter(rate -> rate.getCarrier() != null && rate.getService() != null)
            .collect(Collectors.toMap(
                rate -> rate.getCarrier() + "|" + rate.getService(),
                rate -> rate,
                (existing, replacement) -> existing.getRate() <= replacement.getRate() ? existing : replacement
            ))
            .values());
    }

    private void loadMultipleParcels(List<IParcel> parcels, boolean isShipped) {
        multiBoxContainer.getChildren().clear();

        if (parcels.size() <= 1) {
            return;
        }

        // Create box containers for parcels starting from index 1
        for (int i = 1; i < parcels.size(); i++) {
            IParcel parcel = parcels.get(i);
            boxCounter++;

            VBox boxContainer = new VBox(16);
            boxContainer.getStyleClass().addAll("shipment-info-panel");
            boxContainer.setPadding(new Insets(20));

            HBox header = new HBox();
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            header.setSpacing(10);

            Label boxTitle = new Label("Box " + boxCounter);
            boxTitle.getStyleClass().addAll("section-title", "title-4");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            if (!isShipped) {
                Label removeButton = new Label("×");
                removeButton.getStyleClass().add("remove-box-button");
                removeButton.setOnMouseClicked(e -> removeBox(boxContainer));
                header.getChildren().addAll(boxTitle, spacer, removeButton);
            } else {
                header.getChildren().addAll(boxTitle, spacer);
            }

            HBox fieldsRow = new HBox(10);

            VBox weightSection = new VBox(4);
            weightSection.setPrefWidth(100);
            Label weightLabel = new Label("weight (lbs)");
            weightLabel.getStyleClass().addAll("field-label", "text-caption");
            TextField weightField = new TextField();
            weightField.getStyleClass().addAll("rounded", "weight-field-readonly");
            weightField.setEditable(false);
            weightField.textProperty().addListener((obs, oldVal, newVal) -> parcelChanged = true);

            if (parcel.getWeight() != null) {
                weightField.setText(BigDecimal.valueOf(parcel.getWeight() / 16).setScale(2, RoundingMode.HALF_UP).toString());
            } else {
                weightField.setText("0");
            }

            weightSection.getChildren().addAll(weightLabel, weightField);

            VBox dimsSection = new VBox(4);
            HBox.setHgrow(dimsSection, Priority.ALWAYS);
            Label dimsLabel = new Label("package dimensions (in)");
            dimsLabel.getStyleClass().addAll("field-label", "text-caption");
            TextField dimsField = new TextField();
            dimsField.getStyleClass().add("rounded");
            dimsField.textProperty().addListener((obs, oldVal, newVal) -> parcelChanged = true);
            dimsField.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    int runtimeIndex = boxContainers.indexOf(boxContainer);
                    if (runtimeIndex >= 0) {
                        showBoxWeightCaptureModal(runtimeIndex + 1);
                    }
                }
            });

            if (parcel.getLength() != null && parcel.getWidth() != null && parcel.getHeight() != null) {
                String dimensions = parcel.getLength() + "X" + parcel.getWidth() + "X" + parcel.getHeight();
                dimsField.setText(dimensions);
            } else {
                dimsField.setText("0X0X0");
            }

            dimsSection.getChildren().addAll(dimsLabel, dimsField);

            fieldsRow.getChildren().addAll(weightSection, dimsSection);

            boxContainer.getChildren().addAll(header, fieldsRow);

            boxContainers.add(boxContainer);
        }

        syncBoxContainersToUI();
    }

    private void updateUIForPrintedState(Tugboat tugboat) {
        boolean isPrinted = tugboat != null &&
                           tugboat.getPackageState() != null &&
                           tugboat.getPackageState().getState() != null &&
                           tugboat.getPackageState().getState().equals(PRINTED);

        if (isPrinted) {
            shippedStatusBox.setVisible(true);
            shippedStatusBox.setManaged(true);

            rateButton.setDisable(true);
            shipButton.setDisable(true);
            // autoShipToggle.setDisable(true);

            nameField.setDisable(true);
            address1Field.setDisable(true);
            address2Field.setDisable(true);
            cityField.setDisable(true);
            postalCodeField.setDisable(true);
            phoneField.setDisable(true);
            countryCombo.setDisable(true);
            stateCombo.setDisable(true);

            weightField.setDisable(true);
            dimensionsField.setDisable(true);
            serviceCombo.setDisable(true);

            addBoxButton.setDisable(true);
            boxContainers.forEach(this::disableNodeAndChildren);

        } else {
            shippedStatusBox.setVisible(false);
            shippedStatusBox.setManaged(false);

            nameField.setDisable(false);
            address1Field.setDisable(false);
            address2Field.setDisable(false);
            cityField.setDisable(false);
            postalCodeField.setDisable(false);
            phoneField.setDisable(false);
            countryCombo.setDisable(false);
            stateCombo.setDisable(false);
            shipButton.setDisable(false);

            weightField.setDisable(false);
            dimensionsField.setDisable(false);
            serviceCombo.setDisable(false);

            addBoxButton.setDisable(false);
            boxContainers.forEach(this::enableNodeAndChildren);

            if (!manualEntryMode) {
                autoShipToggle.setDisable(false);
            }
            updateButtonStates();
        }
    }

    private void disableNodeAndChildren(Node node) {
        if (node instanceof TextField) {
            ((TextField) node).setDisable(true);
        } else if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                disableNodeAndChildren(child);
            }
        }
    }

    private void enableNodeAndChildren(Node node) {
        if (node instanceof TextField) {
            ((TextField) node).setDisable(false);
        } else if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                enableNodeAndChildren(child);
            }
        }
    }

    private void populateFormFromTugboat(Tugboat tugboat) {
        clearInputs();
        if (tugboat == null) return;
        cargoIdField.setText(tugboat.getCargoId());

        IAddress destinationAddress = tugboat.getDestinationAddress();
        if (destinationAddress != null) {
            if (destinationAddress.getName() != null) {
                nameField.setText(destinationAddress.getName());
            }
            if (destinationAddress.getStreet1() != null) {
                address1Field.setText(destinationAddress.getStreet1());
            }
            if (destinationAddress.getStreet2() != null) {
                address2Field.setText(destinationAddress.getStreet2());
            }
            if (destinationAddress.getCity() != null) {
                cityField.setText(destinationAddress.getCity());
            }
            if (destinationAddress.getZip() != null) {
                postalCodeField.setText(destinationAddress.getZip());
            }
            if (destinationAddress.getPhone() != null) {
                phoneField.setText(destinationAddress.getPhone());
            }

            AddressFormSupport.applyCountryCode(countryCombo, destinationAddress.getCountry());
            AddressFormSupport.applyStateAbbreviation(stateCombo, destinationAddress.getState());
        }

        if (tugboat.getParcels() != null && !tugboat.getParcels().isEmpty()) {
            IParcel firstParcel = tugboat.getParcels().get(0);

            if (firstParcel.getWeight() != null) {
                weightField.setText(BigDecimal.valueOf(firstParcel.getWeight() / 16).setScale(2, RoundingMode.HALF_UP).toString());
            }

            if (firstParcel.getLength() != null && firstParcel.getWidth() != null && firstParcel.getHeight() != null) {
                String dimensions = firstParcel.getLength() + "X" + firstParcel.getWidth() + "X" + firstParcel.getHeight();
                dimensionsField.setText(dimensions);
            }

            boolean isShipped = tugboat.getPackageState() != null &&
                               tugboat.getPackageState().getState() != null &&
                               tugboat.getPackageState().getState().equals(PRINTED);

            loadMultipleParcels(tugboat.getParcels(), isShipped);
        }

        updateUIForPrintedState(tugboat);
        resetChangeTracking();
    }

    private void startScaleMonitoring() {
        String initial = scaleService.getCurrentWeight();
        currentScaleWeight = initial.equals("--") ? "0" : initial;

        scaleService.startMonitoring(weight -> currentScaleWeight = weight.equals("--") ? "0" : weight);
    }

    private void startWeightLabelTimer() {
        if (weightLabelUpdateTimer != null) {
            weightLabelUpdateTimer.stop();
        }

        weightLabelUpdateTimer = new Timeline(new KeyFrame(Duration.millis(333), e -> updateWeightLabel()));
        weightLabelUpdateTimer.setCycleCount(Timeline.INDEFINITE);
        weightLabelUpdateTimer.play();
    }

    private void updateWeightLabel() {
        if (boxContainers.isEmpty() && scaleService.isScaleConfigured()) {
            String scaleText = currentScaleWeight.equals("--") ? "0" : currentScaleWeight;
            weightLabel.setText("weight (lbs) " + scaleText);
        } else {
            weightLabel.setText("weight (lbs)");
        }
    }

    /**
     * The weight field is filled from the scale and styled read-only to say so.
     * With no scale configured it is the operator who types the weight, so it has
     * to look like the input it has always been.
     */
    private void configureWeightEntry() {
        if (!scaleService.isScaleConfigured()) {
            weightField.getStyleClass().remove("weight-field-readonly");
        }
    }

    /**
     * The weight to ship by, in pounds: the live scale reading when there is a
     * scale, otherwise whatever was typed in the weight field.
     */
    private double effectiveWeightLbs() {
        String weightText = scaleService.isScaleConfigured() ? currentScaleWeight : weightField.getText();
        try {
            return Double.parseDouble(weightText.equals("--") ? "0" : weightText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean shouldShowWeightValidationModal(Tugboat tugboat, double fieldWeight) {
        if (tugboat == null || tugboat.getParcels() == null || tugboat.getParcels().isEmpty()) {
            return false;
        }

        // Nothing to weigh against without a scale
        if (!scaleService.isScaleConfigured()) {
            return false;
        }

        if (!boxContainers.isEmpty()) {
            return false; // Skip validation for multi-parcel
        }

        try {
            double scaleWeight = Double.parseDouble(currentScaleWeight.equals("--") ? "0" : currentScaleWeight);

            String marginStr = prefs.get(DEVICE_PREFS_PREFIX + "scale.weightMargin", "48");
            double weightMargin = Double.parseDouble(marginStr);


            //  x16 for oz conversion
            double difference = Math.abs((scaleWeight*16) - (fieldWeight*16));

            return difference > weightMargin;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showWeightValidationModal(Tugboat tugboat, double fieldWeight, Runnable onProceed, Runnable onCancel) {
        try {
            double scaleWeight = Double.parseDouble(currentScaleWeight.equals("--") ? "0" : currentScaleWeight);

            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/weight-margin-modal.fxml"));
                    Node modalContent = loader.load();

                    WeightMarginModalController modalController = loader.getController();

                    modalController.setWeightInfo(tugboat, scaleWeight, fieldWeight);
                    modalController.setCurrentScaleWeightSupplier(() -> currentScaleWeight);
                    modalController.setCallback(new WeightMarginModalController.WeightMarginModalCallback() {
                        @Override
                        public void onProceedWithShipping() {
                            if (SignInController.isAdminMode()) {
                                double updatedWeight = modalController.getUpdatedWeight();
                                weightField.setText(String.valueOf(updatedWeight));
                                parcelChanged = true;
                            }

                            if (onProceed != null) onProceed.run();
                        }

                        @Override
                        public void onCancelShipping() {
                            Platform.runLater(() -> {
                                currentTugboat = null;
                                clearInputs();
                            });
                            if (onCancel != null) onCancel.run();
                        }
                    });
                    modalController.setOnClose(ModalOverlay.show(shipButton, modalContent));

                } catch (Exception e) {
                    System.err.println("Error showing weight validation modal: " + e.getMessage());
                    e.printStackTrace();
                    if (onProceed != null) onProceed.run();
                }
            });
        } catch (NumberFormatException e) {
            if (onProceed != null) onProceed.run();
        }
    }

    private void executeShipping(Tugboat tugboat, String cargoId, List<ShipmentComponent> preserve) throws Exception {
        var labels = tugboat.print(preserve);
        System.out.println("Tugboat shipped successfully: " + tugboat.getCargoId());

        if (requiresHazmatModal(tugboat)) {
            Platform.runLater(() -> showHazmatModal(tugboat, labels));
        } else {
            printerService.printLabels(labels, cargoId);
            Platform.runLater(this::resetChangeTracking);
        }
    }

    private void performShippingWithWeightValidation(Tugboat tugboat, String cargoId,
                                                     double fieldWeight, List<ShipmentComponent> preserve,
                                                     CountDownLatch completionLatch, AtomicReference<Throwable> errorRef) {
        boolean isMultiParcel = tugboat.getParcels().size() > 1;
        if (!isMultiParcel && shouldShowWeightValidationModal(tugboat, fieldWeight)) {
            showWeightValidationModal(tugboat, fieldWeight, () -> {
                try {
                    executeShipping(tugboat, cargoId, preserve);
                    completionLatch.countDown();
                } catch (Exception e) {
                    errorRef.set(e);
                    completionLatch.countDown();
                }
            }, () -> {
                // User clicked NO or weight validation failed - cancelled
                errorRef.set(new ShippingCancelledException("Shipping cancelled due to weight validation failure"));
                completionLatch.countDown();
            });
        } else {
            try {
                executeShipping(tugboat, cargoId, preserve);
                completionLatch.countDown();
            } catch (Exception e) {
                errorRef.set(e);
                completionLatch.countDown();
            }
        }
    }

    private TextField findWeightFieldInBox(VBox boxContainer) {
        for (Node child : boxContainer.getChildren()) {
            if (child instanceof HBox fieldsRow) {
                if (!fieldsRow.getChildren().isEmpty()) {
                    Node firstChild = fieldsRow.getChildren().getFirst();
                    if (firstChild instanceof VBox weightSection) {
                        for (Node weightChild : weightSection.getChildren()) {
                            if (weightChild instanceof TextField) {
                                return (TextField) weightChild;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private TextField findDimsFieldInBox(VBox boxContainer) {
        for (Node child : boxContainer.getChildren()) {
            if (child instanceof HBox fieldsRow) {
                if (fieldsRow.getChildren().size() >= 2) {
                    Node secondChild = fieldsRow.getChildren().get(1);
                    if (secondChild instanceof VBox dimsSection) {
                        for (Node dimsChild : dimsSection.getChildren()) {
                            if (dimsChild instanceof TextField) {
                                return (TextField) dimsChild;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void showBoxWeightCaptureModal(int boxIndex) {
        int totalBoxes = 1 + boxContainers.size();
        boolean isFinalBox = (boxIndex == boxContainers.size());
        String buttonLabel = isFinalBox ? "Ship" : "Confirm and Proceed to Next Box";

        Platform.runLater(() -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Weight Capture");
            dialog.setHeaderText("Confirm scale weight for Box " + (boxIndex + 1) + " of " + totalBoxes);
            dialog.initModality(Modality.APPLICATION_MODAL);

            GridPane content = new GridPane();
            content.setHgap(10);
            content.setVgap(10);
            content.setPadding(new Insets(20));

            Label scaleWeightLabel = new Label("Scale weight: " + currentScaleWeight + " lbs");
            scaleWeightLabel.getStyleClass().add("modal-text");
            content.add(scaleWeightLabel, 0, 0);

            dialog.getDialogPane().setContent(content);

            ButtonType confirmButtonType = new ButtonType(buttonLabel, ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(confirmButtonType);

            Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);

            Runnable updateWeightDisplay = () -> {
                scaleWeightLabel.setText("Scale weight: " + currentScaleWeight + " lbs");
                try {
                    double weightValue = Double.parseDouble(
                        currentScaleWeight.equals("--") ? "0" : currentScaleWeight);
                    confirmButton.setDisable(weightValue <= 0);
                } catch (NumberFormatException e) {
                    confirmButton.setDisable(true);
                }
            };
            updateWeightDisplay.run();

            Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(500), e -> updateWeightDisplay.run()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

            dialog.setOnCloseRequest(e -> timeline.stop());

            dialog.setResultConverter(dialogButton -> {
                timeline.stop();
                if (dialogButton == confirmButtonType) {
                    try {
                        double weightValue = Double.parseDouble(currentScaleWeight);
                        if (weightValue > 0) {
                            if (boxIndex == 0) {
                                weightField.setText(currentScaleWeight);
                            } else {
                                VBox boxContainer = boxContainers.get(boxIndex - 1);
                                TextField boxWeightField = findWeightFieldInBox(boxContainer);
                                if (boxWeightField != null) {
                                    boxWeightField.setText(currentScaleWeight);
                                }
                            }
                            parcelChanged = true;

                            if (isFinalBox) {
                                executeShipShipment();
                            } else {
                                VBox nextBoxContainer = boxContainers.get(boxIndex);
                                TextField nextDimsField = findDimsFieldInBox(nextBoxContainer);
                                if (nextDimsField != null) {
                                    nextDimsField.requestFocus();
                                    scrollToNode(nextDimsField);
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid weight value: " + currentScaleWeight);
                    }
                }
                return null;
            });

            dialog.showAndWait();
        });
    }

    private boolean requiresHazmatModal(Tugboat tugboat) {
        if (tugboat == null || tugboat.getOptions() == null || tugboat.getOptions().getHazmat() == null) {
            return false;
        }

        String hazmatType = tugboat.getOptions().getHazmat();
        return isEligibleHazmatType(hazmatType);
    }

    private boolean isEligibleHazmatType(String hazmatType) {
        if (hazmatType == null || hazmatType.isEmpty()) {
            return false;
        }

        // Based on the TS code logic - these types require hazmat modal
        return hazmatType.equals("PRIMARY_CONTAINED") ||
               hazmatType.equals("PRIMARY_PACKED") ||
               hazmatType.equals("SECONDARY_CONTAINED") ||
               hazmatType.equals("SECONDARY_PACKED") ||
               hazmatType.equals("LIMITED_QUANTITY");
    }

    private String getUnCodeForHazmat(String hazmatType) {
        if (hazmatType == null) return null;

        return switch (hazmatType) {
            case "PRIMARY_CONTAINED" -> "UN3091";
            case "PRIMARY_PACKED" -> "UN3090";
            case "SECONDARY_CONTAINED" -> "UN3481";
            case "SECONDARY_PACKED" -> "UN3480";
            case "LIMITED_QUANTITY" -> "LIMITED QUANTITY";
            default -> null;
        };
    }

    private void showHazmatModal(Tugboat tugboat, List<IPostageLabel> labels) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hazmat-modal.fxml"));
            Node modalContent = loader.load();

            HazmatModalController modalController = loader.getController();

            String unCode = getUnCodeForHazmat(tugboat.getOptions().getHazmat());
            modalController.setHazmatInfo(unCode);
            modalController.setCallback(new HazmatModalController.HazmatModalCallback() {
                @Override
                public void onProceed() {
                    try {
                        printerService.printLabels(labels, tugboat.getCargoId());
                        Platform.runLater(() -> resetChangeTracking());
                    } catch (Exception e) {
                        System.err.println("Error processing labels after hazmat confirmation: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

            modalController.setOnClose(ModalOverlay.show(shipButton, modalContent));

        } catch (Exception e) {
            System.err.println("Error showing hazmat modal: " + e.getMessage());
            e.printStackTrace();
            try {
                printerService.printLabels(labels, tugboat.getCargoId());
                Platform.runLater(this::resetChangeTracking);
            } catch (Exception fallbackError) {
                System.err.println("Fallback label processing also failed: " + fallbackError.getMessage());
                fallbackError.printStackTrace();
            }
        }
    }
}
