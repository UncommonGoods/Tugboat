// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ICarrierAccount;
import com.uncommongoods.tugboat.bridge.util.CarrierAccountUtil;
import com.uncommongoods.tugboat.bridge.util.CarrierAccountUtil.CarrierAccountItem;
import com.uncommongoods.tugboat.bridge.service.ShipmentHistoryStore;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static com.uncommongoods.tugboat.engine.state.State.*;

public class HistoryController implements Initializable {


    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<CarrierAccountItem> carrierAccountCombo;

    @FXML
    private TableView<HistoryItem> historyTable;

    @FXML
    private TableColumn<HistoryItem, String> shipmentIdColumn;

    @FXML
    private TableColumn<HistoryItem, String> carrierColumn;

    @FXML
    private TableColumn<HistoryItem, String> serviceColumn;

    @FXML
    private TableColumn<HistoryItem, String> chargeColumn;

    @FXML
    private TableColumn<HistoryItem, String> trackingColumn;

    @FXML
    private TableColumn<HistoryItem, Button> printColumn;

    @FXML
    private TableColumn<HistoryItem, Button> voidColumn;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private Label pageLabel;

    @FXML
    private HBox paginationBox;

    @FXML
    private StackPane tableStackPane;

    private static final ConfigurationManager configManager = ConfigurationManager.getInstance();
    private final Map<String, List<ICarrierAccount>> carrierAccountCache = new HashMap<>();

    private static final int ITEMS_PER_PAGE = 25;
    private ObservableList<HistoryItem> allHistoryData = FXCollections.observableArrayList();
    private ObservableList<HistoryItem> filteredHistoryData = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int totalPages = 0;
    private String currentCarrierAccountFilter = "ALL";
    private String currentSearchText = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupSearchField();
        setupPaginationButtons();

        Platform.runLater(() -> {
            loadCarrierAccountsAsync();
        });
    }

    private void loadCarrierAccountsAsync() {
        Task<Void> loadingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                EngineConfig engineConfig = configManager.getActiveConfig();
                carrierAccountCache.putAll(CarrierAccountUtil.loadAllCarrierAccounts(engineConfig));
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    setupCarrierAccountCombo();
                    ShipmentHistoryStore.getInstance().addListener("history", HistoryController.this::refreshFromStore);
                    refreshFromStore();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    System.err.println("Error loading carrier accounts: " + getException().getMessage());
                    setupCarrierAccountCombo();
                    ShipmentHistoryStore.getInstance().addListener("history", HistoryController.this::refreshFromStore);
                    refreshFromStore();
                });
            }
        };

        Thread backgroundThread = new Thread(loadingTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void refreshFromStore() {
        List<Tugboat> tugboats = ShipmentHistoryStore.getInstance().getAll();
        allHistoryData.clear();
        for (Tugboat tugboat : tugboats) {
            if (tugboat.getPackageState() != null &&
                (tugboat.getPackageState().getState().equals(PURCHASED) ||
                 tugboat.getPackageState().getState().equals(PRINTED))) {
                allHistoryData.add(HistoryItem.fromTugboat(tugboat));
            }
        }
        applyFilters();
        setupPagination();
        updateTableData();
    }

    private void setupTableColumns() {
        shipmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("shipmentId"));
        carrierColumn.setCellValueFactory(new PropertyValueFactory<>("carrier"));
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("service"));
        chargeColumn.setCellValueFactory(new PropertyValueFactory<>("charge"));
        trackingColumn.setCellValueFactory(new PropertyValueFactory<>("trackingNumber"));
        printColumn.setCellValueFactory(new PropertyValueFactory<>("printButton"));
        voidColumn.setCellValueFactory(new PropertyValueFactory<>("voidButton"));
    }

    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentSearchText = newValue != null ? newValue.trim().toLowerCase() : "";
            applyFilters();
            setupPagination();
            updateTableData();
        });

        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String searchText = searchField.getText();
                if (searchText != null && searchText.trim().length() == 8) {
                    performDirectCacheLookup(searchText.trim());
                }
            }
        });
    }

    private void setupPaginationButtons() {
        prevButton.setOnAction(e -> goToPreviousPage());
        nextButton.setOnAction(e -> goToNextPage());
    }

    private void setupCarrierAccountCombo() {
        CarrierAccountItem allAccountsItem = new CarrierAccountItem("ALL", "Accounts", "Filter by carrier account");
        carrierAccountCombo.getItems().add(allAccountsItem);

        for (String clientKey : carrierAccountCache.keySet()) {
            List<ICarrierAccount> accounts = carrierAccountCache.get(clientKey);
            if (accounts != null) {
                for (ICarrierAccount account : accounts) {
                    carrierAccountCombo.getItems().add(new CarrierAccountItem(
                        account.getId(),
                        account.getReadable(),
                        account.getDescription()
                    ));
                }
            }
        }

        carrierAccountCombo.getSelectionModel().selectFirst();

        carrierAccountCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentCarrierAccountFilter = newValue.getId();
                applyFilters();
                setupPagination();
                updateTableData();
            }
        });
    }

    private void performDirectCacheLookup(String cargoId) {
        Task<Tugboat> lookupTask = new Task<Tugboat>() {
            @Override
            protected Tugboat call() throws Exception {
                try {
                    EngineConfig engineConfig = configManager.getActiveConfig();
                    if (engineConfig != null && engineConfig.getCacheClient() != null) {
                        String key = engineConfig.getCachePrefix() + "TBCARGO:" + cargoId;
                        String tugboatJson = engineConfig.getCacheClient().get(key);

                        if (tugboatJson != null && !tugboatJson.trim().isEmpty()) {
                            Tugboat tugboat = new Tugboat(tugboatJson);

                            if (tugboat.getPackageState() != null &&
                                (tugboat.getPackageState().getState().equals(PURCHASED) ||
                                 tugboat.getPackageState().getState().equals(PRINTED))) {
                                return tugboat;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error performing direct cache lookup for " + cargoId + ": " + e.getMessage());
                }
                return null;
            }
        };

        lookupTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                Tugboat result = lookupTask.getValue();
                if (result != null) {
                    ShipmentHistoryStore.getInstance().addIfAbsent(result);
                    System.out.println("Direct cache lookup found: " + result.getCargoId());
                } else {
                    System.out.println("No result found for direct cache lookup: " + cargoId);
                }
            });
        });

        lookupTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                System.err.println("Direct cache lookup failed: " + lookupTask.getException().getMessage());
            });
        });

        Thread backgroundThread = new Thread(lookupTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void applyFilters() {
        filteredHistoryData.clear();

        for (HistoryItem item : allHistoryData) {
            boolean matchesCarrierAccount = true;
            boolean matchesSearch = true;

            if (!"ALL".equals(currentCarrierAccountFilter)) {
                matchesCarrierAccount = false;

                if (item.getTugboat() != null && item.getTugboat().getSelectedRate() != null) {
                    String carrierAccountId = item.getTugboat().getSelectedRate().getCarrierAccountId();
                    if (currentCarrierAccountFilter.equals(carrierAccountId)) {
                        matchesCarrierAccount = true;
                    }
                }
            }

            if (!currentSearchText.isEmpty()) {
                matchesSearch = false;

                if (item.getShipmentId() != null) {
                    String shipmentId = item.getShipmentId().toLowerCase();
                    if (shipmentId.startsWith(currentSearchText)) {
                        matchesSearch = true;
                    }
                }
            }

            if (matchesCarrierAccount && matchesSearch) {
                filteredHistoryData.add(item);
            }
        }
    }

    private void setupPagination() {
        totalPages = (int) Math.ceil((double) filteredHistoryData.size() / ITEMS_PER_PAGE);
        currentPage = 0;

        if (filteredHistoryData.size() <= ITEMS_PER_PAGE && paginationBox != null) {
            paginationBox.setVisible(false);
            paginationBox.setManaged(false);
        } else if (paginationBox != null) {
            paginationBox.setVisible(true);
            paginationBox.setManaged(true);
        }

        updatePaginationControls();
    }

    private void updatePaginationControls() {
        if (pageLabel != null) {
            pageLabel.setText("Page " + (currentPage + 1) + " of " + Math.max(1, totalPages));
        }

        if (prevButton != null) {
            prevButton.setDisable(currentPage == 0);
        }

        if (nextButton != null) {
            nextButton.setDisable(currentPage >= totalPages - 1);
        }
    }

    private void updateTableData() {
        if (historyTable == null) return;

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filteredHistoryData.size());

        ObservableList<HistoryItem> pageData = FXCollections.observableArrayList(
            filteredHistoryData.subList(startIndex, endIndex)
        );

        historyTable.setItems(pageData);
    }

    private void goToPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateTableData();
            updatePaginationControls();
        }
    }

    private void goToNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateTableData();
            updatePaginationControls();
        }
    }
}
