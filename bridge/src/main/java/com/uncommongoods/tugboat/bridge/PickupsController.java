// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.application.Platform;
import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.engine.manifest.PickupFacility;
import com.uncommongoods.tugboat.engine.manifest.PickupGroup;
import com.uncommongoods.tugboat.engine.manifest.ManifestBatch;
import com.uncommongoods.tugboat.engine.manifest.CarrierService;
import com.uncommongoods.tugboat.engine.manifest.CarrierServiceGroup;
import com.uncommongoods.tugboat.engine.model.TugboatAddress;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddress;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ICarrierAccount;
import com.uncommongoods.tugboat.bridge.util.CarrierAccountUtil;
import com.uncommongoods.tugboat.bridge.util.CarrierAccountUtil.CarrierAccountItem;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class PickupsController implements Initializable {

    @FXML
    private VBox contentContainer;

    @FXML
    private StackPane contentStackPane;

    @FXML
    private VBox facilityAddressForm;

    @FXML
    private TextField facilityCodeField;

    @FXML
    private TextField companyField;

    @FXML
    private TextField street1Field;

    @FXML
    private TextField street2Field;

    @FXML
    private TextField cityField;

    @FXML
    private TextField stateField;

    @FXML
    private TextField countryField;

    @FXML
    private TextField zipField;

    @FXML
    private TextField phoneField;

    @FXML
    private Button facilitySubmitButton;

    @FXML
    private VBox addPickupGroupForm;

    @FXML
    private TextField groupNameField;

    @FXML
    private ComboBox<String> shipperCombo;

    @FXML
    private ComboBox<CarrierAccountItem> carrierAccountCombo;

    @FXML
    private Button createGroupButton;

    @FXML
    private Button rollAllGroupsButton;

    private final ConfigurationManager configManager = ConfigurationManager.getInstance();
    private PickupFacility pickupFacility;
    private String newlyCreatedGroupId = null;
    private final Map<String, List<ICarrierAccount>> carrierAccountCache = new HashMap<>();
    private StackPane loadingOverlay;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFormEventHandlers();
        Platform.runLater(() -> {
            setupLoadingOverlay();
            loadPickupData();
        });
    }

    private void setupLoadingOverlay() {
        loadingOverlay = new StackPane();
        loadingOverlay.getStyleClass().add("loading-overlay");
        loadingOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");

        ProgressBar loadingProgressBar = new ProgressBar();
        loadingProgressBar.setPrefWidth(200);
        loadingProgressBar.setProgress(-1); // Indeterminate progress

        Label loadingLabel = new Label("Loading Pickups...");
        loadingLabel.getStyleClass().addAll("text-caption");

        VBox loadingContent = new VBox(10);
        loadingContent.setAlignment(javafx.geometry.Pos.CENTER);
        loadingContent.getChildren().addAll(loadingProgressBar, loadingLabel);
        loadingContent.setStyle("-fx-background-color: -color-bg-default; -fx-background-radius: 6px; -fx-padding: 20px;");

        loadingOverlay.getChildren().add(loadingContent);
        loadingOverlay.setVisible(false);

        Platform.runLater(() -> {
            if (contentStackPane != null) {
                contentStackPane.getChildren().add(loadingOverlay);
            }
        });
    }

    private void setupFormEventHandlers() {
        if (facilityCodeField != null) {
            facilityCodeField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 6) {
                    facilityCodeField.setText(oldValue);
                } else if (!newValue.equals(newValue.toUpperCase())) {
                    facilityCodeField.setText(newValue.toUpperCase());
                }
            });
        }

        if (groupNameField != null) {
            groupNameField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 28) {
                    groupNameField.setText(oldValue);
                } else if (!newValue.equals(newValue.toUpperCase())) {
                    groupNameField.setText(newValue.toUpperCase());
                }
            });
        }

        if (shipperCombo != null) {
            shipperCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                populateCarrierAccounts();
            });
        }

        if (carrierAccountCombo != null) {
            carrierAccountCombo.setConverter(new javafx.util.StringConverter<CarrierAccountItem>() {
                @Override
                public String toString(CarrierAccountItem item) {
                    return item != null ? item.toString() : "";
                }

                @Override
                public CarrierAccountItem fromString(String string) {
                    return carrierAccountCombo.getItems().stream()
                        .filter(item -> item.toString().equals(string))
                        .findFirst()
                        .orElse(null);
                }
            });
        }

        // Setup form submit buttons
        if (facilitySubmitButton != null) {
            facilitySubmitButton.setOnAction(event -> handleFacilitySubmit());
        }

        if (createGroupButton != null) {
            createGroupButton.setOnAction(event -> handleCreateGroup());
        }

        if (rollAllGroupsButton != null) {
            rollAllGroupsButton.setOnAction(event -> handleRollAllGroups());
        }
    }

    private void loadPickupData() {
        showLoading(true);

        // background task for loading carrier accounts
        Task<Void> loadingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                EngineConfig engineConfig = configManager.getActiveConfig();
                carrierAccountCache.putAll(CarrierAccountUtil.loadAllCarrierAccounts(engineConfig));
                return null;
            }

            @Override
            protected void succeeded() {
                javafx.application.Platform.runLater(() -> {
                    try {
                        pickupFacility = configManager.getPickupFacility();

                        if (pickupFacility == null) {
                            EngineConfig engineConfig = configManager.getActiveConfig();
                            if (engineConfig.getCacheClient() == null) {
                                hideAllForms();
                                showError("Pickups and manifests require a cache. Set the cache fields on the Settings page.");
                                showLoading(false);
                                return;
                            }
                            pickupFacility = new PickupFacility(engineConfig, "BK");
                            pickupFacility.retrieve();

                            configManager.setPickupFacility(pickupFacility);
                        }

                        hideAllForms();

                        if (pickupFacility.getAddress() == null) {
                            showFacilityAddressForm();
                        } else if (pickupFacility.getPickupGroups() == null || pickupFacility.getPickupGroups().isEmpty()) {
                            showAddPickupGroupForm();
                        } else {
                            showManifestGroups();
                        }

                        showLoading(false);
                    } catch (Exception e) {
                        System.err.println("Error loading pickup data: " + e.getMessage());
                        e.printStackTrace();
                        showError("Failed to load pickup data: " + e.getMessage());
                        showLoading(false);
                    }
                });
            }

            @Override
            protected void failed() {
                javafx.application.Platform.runLater(() -> {
                    System.err.println("Error loading carrier accounts: " + getException().getMessage());
                    getException().printStackTrace();
                    showError("Failed to load carrier accounts: " + getException().getMessage());
                    showLoading(false);
                });
            }
        };

        Thread backgroundThread = new Thread(loadingTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(show);
        }
    }

    private ICarrierAccount findCarrierAccount(String shippingClientKey, String carrierAccountId) {
        return CarrierAccountUtil.findCarrierAccount(carrierAccountCache, shippingClientKey, carrierAccountId);
    }

    private void hideAllForms() {
        if (facilityAddressForm != null) {
            facilityAddressForm.setVisible(false);
            facilityAddressForm.setManaged(false);
        }
        if (addPickupGroupForm != null) {
            addPickupGroupForm.setVisible(false);
            addPickupGroupForm.setManaged(false);
        }
        if (rollAllGroupsButton != null) {
            rollAllGroupsButton.setVisible(false);
            rollAllGroupsButton.setManaged(false);
        }
    }

    private void populateCarrierAccounts() {
        String selectedShipper = shipperCombo.getSelectionModel().getSelectedItem();
        if (selectedShipper == null) return;

        carrierAccountCombo.getItems().clear();
        List<ICarrierAccount> carrierAccounts = carrierAccountCache.get(selectedShipper);
        if (carrierAccounts != null) {
            for (var account : carrierAccounts) {
                carrierAccountCombo.getItems().add(new CarrierAccountItem(
                    account.getId(),
                    account.getReadable(),
                    account.getDescription()
                ));
            }
        }
    }

    private void handleFacilitySubmit() {
        try {
            String facilityCode = facilityCodeField.getText().trim().toUpperCase();

            Map<String, Object> facilityAddressMap = new HashMap<>();
            facilityAddressMap.put("company", companyField.getText());
            facilityAddressMap.put("street1", street1Field.getText());
            facilityAddressMap.put("street2", street2Field.getText());
            facilityAddressMap.put("city", cityField.getText());
            facilityAddressMap.put("state", stateField.getText());
            facilityAddressMap.put("country", countryField.getText());
            facilityAddressMap.put("zip", zipField.getText());
            facilityAddressMap.put("phone", phoneField.getText());

            IAddress facilityAddress = new TugboatAddress(facilityAddressMap);
            EngineConfig engineConfig = configManager.getActiveConfig();
            pickupFacility = new PickupFacility(engineConfig, facilityCode, facilityAddress);
            pickupFacility.create();

            configManager.setPickupFacility(pickupFacility);

            loadPickupData();
        } catch (Exception e) {
            System.err.println("Error creating facility: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to create facility: " + e.getMessage());
        }
    }

    private void handleCreateGroup() {
        String groupName = groupNameField.getText();
        String selectedShipper = shipperCombo.getSelectionModel().getSelectedItem();
        String carrierAccountId;

        CarrierAccountItem selectedItem = carrierAccountCombo.getValue();
        if (selectedItem != null) {
            carrierAccountId = selectedItem.getId();
        } else {
            carrierAccountId = carrierAccountCombo.getEditor().getText();
        }

        if (groupName == null || groupName.trim().isEmpty()) {
            showError("Group name is required");
            return;
        }
        if (selectedShipper == null || selectedShipper.trim().isEmpty()) {
            showError("Shipper is required");
            return;
        }
        if (carrierAccountId == null || carrierAccountId.trim().isEmpty()) {
            showError("Carrier account is required");
            return;
        }

        try {
            LocalDate manifestDate = LocalDate.now();
            String groupId = pickupFacility.createPickupGroup(groupName, carrierAccountId, manifestDate, selectedShipper);
            System.out.println("Created pickup group: " + groupId);
            newlyCreatedGroupId = groupId;
            loadPickupData();
        } catch (Exception e) {
            System.err.println("Error creating pickup group: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to create pickup group: " + e.getMessage());
        }
    }

    private void showFacilityAddressForm() {
        facilityCodeField.setText("BK");
        companyField.setText("Dr. Steve Brule");
        street1Field.setText("123 Fake St");
        street2Field.setText("");
        cityField.setText("Brooklyn");
        stateField.setText("NY");
        countryField.setText("US");
        zipField.setText("11220");
        phoneField.setText("2027621401");

        facilityAddressForm.setVisible(true);
        facilityAddressForm.setManaged(true);
    }

    private void showAddPickupGroupForm() {
        populateAddPickupGroupForm();
        addPickupGroupForm.setVisible(true);
        addPickupGroupForm.setManaged(true);
    }

    private void populateAddPickupGroupForm() {
        try {
            EngineConfig engineConfig = configManager.getActiveConfig();
            var shippingClients = engineConfig.getShippingClients();
            shipperCombo.getItems().clear();
            shipperCombo.getItems().addAll(shippingClients.keySet());
            shipperCombo.getSelectionModel().select("default"); // Set default selection
        } catch (Exception e) {
            System.err.println("Error loading shipping clients: " + e.getMessage());
        }

        populateCarrierAccounts();
    }

    private void showManifestGroups() {
        List<PickupGroup> groups = pickupFacility.getPickupGroups();

        contentContainer.getChildren().clear();
        for (PickupGroup group : groups) {
            boolean isExpanded = newlyCreatedGroupId != null && newlyCreatedGroupId.equals(group.getPickupGroupId());
            TitledPane groupPane = createManifestGroupPane(group, isExpanded);
            contentContainer.getChildren().add(groupPane);
        }

        newlyCreatedGroupId = null;

        populateAddPickupGroupForm();

        if (addPickupGroupForm.getParent() != null) {
            ((VBox) addPickupGroupForm.getParent()).getChildren().remove(addPickupGroupForm);
        }
        addPickupGroupForm.setVisible(true);
        addPickupGroupForm.setManaged(true);
        contentContainer.getChildren().add(addPickupGroupForm);

        rollAllGroupsButton.setVisible(true);
        rollAllGroupsButton.setManaged(true);

        VBox paddingBox = new VBox();
        paddingBox.setPrefHeight(50);
        contentContainer.getChildren().add(paddingBox);
    }


    private TitledPane createManifestGroupPane(PickupGroup group, boolean expanded) {
        TitledPane groupPane = new TitledPane();
        groupPane.setText(group.getPickupGroupName());
        groupPane.setExpanded(expanded);
        groupPane.getStyleClass().add("settings-panel");

        VBox content = new VBox(10);
        content.setPadding(new Insets(5, 20, 20, 20)); // Reduced top padding from 20 to 10

        Label pickupDateLabel = new Label("Pickup Date: " + group.getPickupDate());
        content.getChildren().add(pickupDateLabel);

        int shipmentCount = 0;
        try {
            if (group.getManifestBatchId() != null) {
                EngineConfig engineConfig = configManager.getActiveConfig();
                ManifestBatch manifestBatch = new ManifestBatch(engineConfig, group.getPickupGroupId(), group.getManifestBatchId());
                manifestBatch.retrieve();
                shipmentCount = manifestBatch.getManifestBatchCargo().size();
            }
        } catch (Exception e) {
            System.err.println("Error getting shipment count: " + e.getMessage());
        }

        Label shipmentsLabel = new Label("Shipments shipped: " + shipmentCount);
        content.getChildren().add(shipmentsLabel);

        HBox datePickerRow = new HBox(10);
        datePickerRow.setStyle("-fx-alignment: center-left;");

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        DatePicker datePicker = new DatePicker(group.getPickupDate());
        datePicker.setEditable(false);
        datePicker.setPrefWidth(200);

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });

        Button closePickupButton = new Button("Close Pickup");
        closePickupButton.setOnAction(event -> showClosePickupModal(group, datePicker.getValue()));

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button removeGroupButton = new Button("Remove Group");
        removeGroupButton.getStyleClass().add("danger");
        removeGroupButton.setOnAction(event -> showRemoveGroupModal(group));

        datePickerRow.getChildren().addAll(datePicker, closePickupButton, spacer, removeGroupButton);
        content.getChildren().add(datePickerRow);

        // Add carrier account information using cached data
        ICarrierAccount carrierAccount = findCarrierAccount(group.getShippingClientKey(), group.getCarrierAccountId());
        if (carrierAccount != null) {
            String displayText = carrierAccount.getReadable();
            if (carrierAccount.getDescription() != null && !carrierAccount.getDescription().trim().isEmpty()) {
                displayText += " | " + carrierAccount.getDescription();
            }
            Label carrierDisplayLabel = new Label(displayText);
            content.getChildren().add(carrierDisplayLabel);

            Label carrierIdLabel = new Label("Carrier Account: " + carrierAccount.getId());
            content.getChildren().add(carrierIdLabel);
        } else {
            Label carrierIdLabel = new Label("Carrier Account: " + group.getCarrierAccountId());
            content.getChildren().add(carrierIdLabel);
        }

        VBox servicesSection = createServicesSection(group);
        content.getChildren().add(servicesSection);

        groupPane.setContent(content);
        return groupPane;
    }

    private VBox createServicesSection(PickupGroup group) {
        VBox servicesSection = new VBox(10);
        servicesSection.setPadding(new Insets(10, 0, 0, 0));

        Label servicesLabel = new Label("Services:");
        servicesLabel.getStyleClass().addAll("field-label", "text-caption");

        ComboBox<String> servicesCombo = new ComboBox<>();
        servicesCombo.setPromptText("Select a service");
        servicesCombo.setEditable(true);
        servicesCombo.setPrefWidth(300);

        ObservableList<String> selectedServices = FXCollections.observableArrayList();

        FlowPane selectedServicesPane = new FlowPane();
        selectedServicesPane.setHgap(5);
        selectedServicesPane.setVgap(5);
        selectedServicesPane.setPadding(new Insets(10, 0, 0, 0));

        // Populate with actual services from global carrier services (excluding already selected)
        final ObservableList<String> availableServices = FXCollections.observableArrayList();

        // Populate with already selected services from the pickup group first
        try {
            for (CarrierService service : group.getAvailableServices()) {
                String serviceDisplay = service.service();
                selectedServices.add(serviceDisplay);
                addServiceTag(selectedServicesPane, serviceDisplay, selectedServices, group, servicesCombo, availableServices);
            }
        } catch (Exception e) {
            System.err.println("Error loading existing services: " + e.getMessage());
        }
        try {
            var globalCarrierServices = pickupFacility.getGlobalCarrierServices();
            for (CarrierServiceGroup serviceGroup : globalCarrierServices) {
                // Check if this service group contains services for the pickup group's carrier account
                if (serviceGroup.getCarrierAccountIds().contains(group.getCarrierAccountId())) {
                    for (CarrierService carrierService : serviceGroup.getCarrierServices()) {
                        String serviceDisplay = carrierService.service();
                        if (!selectedServices.contains(serviceDisplay) && !availableServices.contains(serviceDisplay)) {
                            availableServices.add(serviceDisplay);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading global carrier services: " + e.getMessage());
            String[] sampleServices = {"GROUND", "EXPRESS", "OVERNIGHT", "2DAY", "INTERNATIONAL"};
            for (String sample : sampleServices) {
                if (!selectedServices.contains(sample)) {
                    availableServices.add(sample);
                }
            }
        }

        servicesCombo.setItems(availableServices);

        servicesCombo.getSelectionModel().clearSelection();

        // service selection
        servicesCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Service selection changed: oldValue=" + oldValue + ", newValue=" + newValue);
            System.out.println("Selected services: " + selectedServices);
            if (newValue != null && !newValue.trim().isEmpty() &&
                !selectedServices.contains(newValue)) {
                System.out.println("Adding service: " + newValue);

                final String serviceToAdd = newValue;

                // Defer to avoid JavaFX concurrency issues
                javafx.application.Platform.runLater(() -> {
                    try {
                        servicesCombo.getSelectionModel().clearSelection();
                        servicesCombo.setValue(null);


                        String carrier = findCarrierByCarrierAccount(group.getCarrierAccountId());
                        System.out.println("Carrier found: " + carrier);

                        if (carrier != null) {
                            CarrierService carrierServiceToAdd = new CarrierService(carrier, serviceToAdd);
                            System.out.println("Creating CarrierService: " + carrierServiceToAdd);
                            pickupFacility.addServiceToGroup(group.getPickupGroupName(), carrierServiceToAdd);
                            System.out.println("Added service " + serviceToAdd + " to group " + group.getPickupGroupName());

                            selectedServices.add(serviceToAdd);
                            addServiceTag(selectedServicesPane, serviceToAdd, selectedServices, group, servicesCombo, availableServices);

                            ObservableList<String> updatedItems = FXCollections.observableArrayList();
                            for (String item : availableServices) {
                                if (!selectedServices.contains(item)) {
                                    updatedItems.add(item);
                                }
                            }
                            servicesCombo.setItems(updatedItems);
                        }

                    } catch (Exception e) {
                        System.err.println("Error handling service selection: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } else {
                System.out.println("Service selection ignored - newValue=" + newValue +
                    ", alreadySelected=" + (newValue != null && selectedServices.contains(newValue)));
            }
        });

        // Handle manual user entry
        servicesCombo.setOnKeyPressed(event -> {
            try {
                if (event.getCode().toString().equals("ENTER")) {
                    String enteredService = servicesCombo.getEditor().getText();
                    if (enteredService != null && !enteredService.trim().isEmpty() &&
                        !selectedServices.contains(enteredService)) {
                        String carrier = findCarrierByCarrierAccount(group.getCarrierAccountId());

                        if (carrier != null) {
                            CarrierService carrierServiceToAdd = new CarrierService(carrier, enteredService);
                            pickupFacility.addServiceToGroup(group.getPickupGroupName(), carrierServiceToAdd);
                            System.out.println("Added service " + enteredService + " to group " + group.getPickupGroupName());

                            selectedServices.add(enteredService);
                            addServiceTag(selectedServicesPane, enteredService, selectedServices, group, servicesCombo, availableServices);

                            javafx.application.Platform.runLater(() -> {
                                servicesCombo.getItems().remove(enteredService);
                                servicesCombo.getEditor().clear();
                                servicesCombo.getSelectionModel().clearSelection();
                            });
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error handling manual service entry: " + e.getMessage());
            }
        });

        servicesSection.getChildren().addAll(servicesLabel, servicesCombo, selectedServicesPane);
        return servicesSection;
    }

    private CarrierService findCarrierServiceByName(String serviceName, String carrierAccountId) {
        try {
            var globalCarrierServices = pickupFacility.getGlobalCarrierServices();
            return globalCarrierServices.stream()
                .filter(serviceGroup -> serviceGroup.getCarrierAccountIds().contains(carrierAccountId))
                .flatMap(carrierServiceGroup -> carrierServiceGroup.getCarrierServices().stream())
                .filter(carrierService -> carrierService.service().equals(serviceName))
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            System.err.println("Error finding carrier service: " + e.getMessage());
        }
        return null;
    }

    private String findCarrierByCarrierAccount(String carrierAccountId) {
        try {
            var globalCarrierServices = pickupFacility.getGlobalCarrierServices();
            return globalCarrierServices.stream()
                .filter(serviceGroup -> serviceGroup.getCarrierAccountIds().contains(carrierAccountId))
                .flatMap(carrierServiceGroup -> carrierServiceGroup.getCarrierServices().stream())
                .map(CarrierService::carrier)
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            System.err.println("Error finding carrier service: " + e.getMessage());
        }
        return null;
    }

    private void addServiceTag(FlowPane container, String service, ObservableList<String> selectedServices, PickupGroup group, ComboBox<String> servicesCombo, ObservableList<String> availableServices) {
        HBox serviceTag = new HBox(5);
        serviceTag.getStyleClass().add("tag");
        serviceTag.setPadding(new Insets(3, 6, 3, 6));
        serviceTag.setStyle("-fx-background-color: -color-accent-subtle; -fx-background-radius: 3px;");
        serviceTag.setAlignment(javafx.geometry.Pos.CENTER); // Center align content vertically

        Label serviceLabel = new Label(service);
        serviceLabel.getStyleClass().add("text-small");

        Label removeButton = new Label("×");
        removeButton.getStyleClass().add("remove-box-button");

        removeButton.setOnMouseClicked(event -> {
            try {
                CarrierService carrierServiceToRemove = null;
                for (CarrierService carrierService : group.getAvailableServices()) {
                    if (carrierService.service().equals(service)) {
                        carrierServiceToRemove = carrierService;
                        break;
                    }
                }

                if (carrierServiceToRemove != null) {
                    pickupFacility.removeServiceFromGroup(group.getPickupGroupId(), carrierServiceToRemove);
                    System.out.println("Removed service " + service + " from group " + group.getPickupGroupName());
                }

                selectedServices.remove(service);
                container.getChildren().remove(serviceTag);

                // Defer to avoid JavaFX concurrency issues
                javafx.application.Platform.runLater(() -> {
                    if (!availableServices.contains(service)) {
                        availableServices.add(service);
                    }

                    ObservableList<String> updatedItems = FXCollections.observableArrayList();
                    for (String item : availableServices) {
                        if (!selectedServices.contains(item)) {
                            updatedItems.add(item);
                        }
                    }
                    servicesCombo.setItems(updatedItems);
                });
            } catch (Exception e) {
                System.err.println("Error removing service: " + e.getMessage());
                e.printStackTrace();
            }
        });

        serviceTag.getChildren().addAll(serviceLabel, removeButton);
        container.getChildren().add(serviceTag);
    }

    private void showClosePickupModal(PickupGroup group, LocalDate selectedDate) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Confirm Close Pickup");
        dialog.setHeaderText("Are you sure you want to close out " + group.getPickupGroupName() +
                            " and change the pickup date to " + selectedDate + "?");

        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(closeButtonType, cancelButtonType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == closeButtonType) {
                try {
                    this.pickupFacility.retrieve();
                    pickupFacility.closePickupGroup(group.getPickupGroupName(), selectedDate);
                    System.out.println("Closed pickup group: " + group.getPickupGroupName());
                    loadPickupData();
                } catch (Exception e) {
                    System.err.println("Error closing pickup group: " + e.getMessage());
                    e.printStackTrace();
                    showError("Failed to close pickup group: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showRemoveGroupModal(PickupGroup group) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Confirm Remove Group");
        dialog.setHeaderText("Are you sure you want to remove " + group.getPickupGroupName() + "?");

        ButtonType removeButtonType = new ButtonType("Remove", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(removeButtonType, cancelButtonType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == removeButtonType) {
                try {
                    pickupFacility.removePickupGroup(group.getPickupGroupId());
                    System.out.println("Removed pickup group: " + group.getPickupGroupName());
                    loadPickupData(); // Reload the view
                } catch (Exception e) {
                    System.err.println("Error removing pickup group: " + e.getMessage());
                    e.printStackTrace();
                    showError("Failed to remove pickup group: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void handleRollAllGroups() {
        try {
            this.pickupFacility.retrieve();
            List<PickupGroup> groups = pickupFacility.getPickupGroups();
            if (groups == null || groups.isEmpty()) {
                showError("No pickup groups to roll");
                return;
            }

            LocalDate today = LocalDate.now();

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Confirm Roll All Groups");
            dialog.setHeaderText("Are you sure you want to roll all pickup groups to " + today + "?");

            ButtonType rollButtonType = new ButtonType("Roll All", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(rollButtonType, cancelButtonType);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == rollButtonType) {
                    // Show loading overlay
                    Platform.runLater(() -> showLoading(true));

                    // background task for rolling groups
                    Task<Void> rollTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            for (PickupGroup group : groups) {
                                pickupFacility.closePickupGroup(group.getPickupGroupName(), today);
                            }
                            System.out.println("Rolled all pickup groups to " + today);
                            return null;
                        }

                        @Override
                        protected void succeeded() {
                            Platform.runLater(() -> {
                                loadPickupData(); // Reload the view
                            });
                        }

                        @Override
                        protected void failed() {
                            Platform.runLater(() -> {
                                showLoading(false);
                                System.err.println("Error rolling pickup groups: " + getException().getMessage());
                                getException().printStackTrace();
                                showError("Failed to roll pickup groups: " + getException().getMessage());
                            });
                        }
                    };

                    // Run task in background thread
                    Thread backgroundThread = new Thread(rollTask);
                    backgroundThread.setDaemon(true);
                    backgroundThread.start();
                }
                return null;
            });

            dialog.showAndWait();
        } catch (Exception e) {
            System.err.println("Error handling roll all groups: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to roll pickup groups: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
