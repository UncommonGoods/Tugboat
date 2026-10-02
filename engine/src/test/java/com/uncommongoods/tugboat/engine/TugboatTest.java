// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine;

import com.google.gson.JsonElement;
import com.uncommongoods.tugboat.engine.ports.cache.ICacheClient;
import com.uncommongoods.tugboat.engine.ports.cache.JedisRedisAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.*;
import com.uncommongoods.tugboat.engine.ports.shipping.model.MapValues;
import com.uncommongoods.tugboat.engine.ports.shipping.service.*;
import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.manifest.PickupFacility;
import com.uncommongoods.tugboat.engine.model.TugboatAddress;
import com.uncommongoods.tugboat.engine.model.TugboatParcel;
import com.uncommongoods.tugboat.engine.state.InitialTugboatState;
import com.uncommongoods.tugboat.engine.state.RatedTugboatState;
import com.uncommongoods.tugboat.engine.state.State;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class TugboatTest {

    private static final Logger logger = Logger.getLogger(TugboatTest.class.getName());

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    private static EngineConfig engineConfig;

    @BeforeAll
    static void setUp() {
        String redisHost = redis.getHost();
        Integer redisPort = redis.getMappedPort(6379);

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(poolConfig, redisHost, redisPort);

        ICacheClient cacheClient = new JedisRedisAdapter(jedisPool);

        engineConfig = EngineConfig.builder()
            .withCacheClient(cacheClient)
            .withCachePrefix("TEST")
            .withShippingClient("default", new TestShippingClient())
            .build();
    }

    @AfterEach
    void cleanUp() {
        try (redis.clients.jedis.Jedis jedis = new redis.clients.jedis.Jedis(redis.getHost(), redis.getMappedPort(6379))) {
            jedis.flushDB();
        }
    }

    // Constructor Tests
    @Test
    void testTugboatBuilderConstructor() {
        String cargoId = "CARGO_001";
        TugboatAddress originAddress = createTestOriginAddress();
        TugboatAddress destinationAddress = createTestDestinationAddress();
        TugboatAddress returnAddress = createTestReturnAddress();
        List<IParcel> parcels = new ArrayList<>(createTestParcels());
        TugboatOptions options = new TugboatOptions();

        Tugboat tugboat = Tugboat.builder(engineConfig, cargoId)
            .expectedDeliveryDate(LocalDate.now().plusDays(2))
            .originAddress(originAddress)
            .destinationAddress(destinationAddress)
            .returnAddress(returnAddress)
            .parcels(parcels)
            .options(options)
            .build();

        assertNotNull(tugboat);
        assertEquals(cargoId, tugboat.getCargoId());
        assertEquals(LocalDate.now().plusDays(2), tugboat.getExpectedDeliveryDate());
        assertNotNull(tugboat.getOriginAddress());
        assertNotNull(tugboat.getDestinationAddress());
        assertNotNull(tugboat.getReturnAddress());
        assertNotNull(tugboat.getParcels());
        assertEquals(2, tugboat.getParcels().size());
        assertNotNull(tugboat.getOptions());
        assertEquals(engineConfig, tugboat.getEngineConfig());
        assertNull(tugboat.getPickupFacility());
    }

    @Test
    void testTugboatBuilderConstructorWithManifestFacility() throws TugboatException {
        String cargoId = "CARGO_002";
        String facilityCode = "FACILITY_001";
        TugboatAddress facilityAddress = createTestWarehouseAddress();
        PickupFacility pickupFacility = new PickupFacility(engineConfig, facilityCode, facilityAddress);
        pickupFacility.create();

        Tugboat tugboat = Tugboat.builder(engineConfig, cargoId)
            .pickupFacility(pickupFacility)
            .originAddress(createTestOriginAddress())
            .destinationAddress(createTestDestinationAddress())
            .parcels(new ArrayList<>(createTestParcels()))
            .build();

        assertNotNull(tugboat);
        assertEquals(cargoId, tugboat.getCargoId());
        assertNotNull(tugboat.getPickupFacility());
        assertEquals(facilityCode, tugboat.getPickupFacility().getPickupFacilityCode());
    }

    @Test
    void testTugboatBuilderAddressesHelper() {
        String cargoId = "CARGO_003";
        TugboatAddress originAddress = createTestOriginAddress();
        TugboatAddress destinationAddress = createTestDestinationAddress();
        TugboatAddress returnAddress = createTestReturnAddress();

        Tugboat tugboat = Tugboat.builder(engineConfig, cargoId)
            .addresses(originAddress, destinationAddress, returnAddress)
            .parcels(new ArrayList<>(createTestParcels()))
            .build();

        assertNotNull(tugboat);
        assertEquals(cargoId, tugboat.getCargoId());
        assertNotNull(tugboat.getOriginAddress());
        assertNotNull(tugboat.getDestinationAddress());
        assertNotNull(tugboat.getReturnAddress());
        assertEquals(originAddress.getId(), tugboat.getOriginAddress().getId());
        assertEquals(destinationAddress.getId(), tugboat.getDestinationAddress().getId());
        assertEquals(returnAddress.getId(), tugboat.getReturnAddress().getId());
    }

    @Test
    void testTugboatJsonConstructor() {
        String cargoId = "CARGO_004";
        Tugboat originalTugboat = Tugboat.builder(engineConfig, cargoId)
            .expectedDeliveryDate(LocalDate.now().plusDays(1))
            .originAddress(createTestOriginAddress())
            .destinationAddress(createTestDestinationAddress())
            .parcels(new ArrayList<>(createTestParcels()))
            .build();

        String json = originalTugboat.toString();
        assertNotNull(json);
        assertFalse(json.trim().isEmpty());

        Tugboat tugboatFromJson = new Tugboat(json);
        assertNotNull(tugboatFromJson);
        assertEquals(cargoId, tugboatFromJson.getCargoId());
        assertEquals(LocalDate.now().plusDays(1), tugboatFromJson.getExpectedDeliveryDate());
    }

    // Load Method Tests
    @Test
    void testRateMethodWithoutManifestFacility() throws TugboatException {
        String cargoId = "CARGO_005";

        Tugboat tugboat = Tugboat.builder(engineConfig, cargoId)
            .originAddress(createTestOriginAddress())
            .destinationAddress(createTestDestinationAddress())
            .parcels(new ArrayList<>(createTestParcels()))
            .build();

        // Initially should be in INITIAL state
        assertInstanceOf(InitialTugboatState.class, tugboat.getPackageState());
        assertEquals(State.INITIAL, tugboat.getPackageState().getState());

        Tugboat ratedTugboat = tugboat.rate();

        assertNotNull(ratedTugboat);
        assertSame(tugboat, ratedTugboat); // Should return same instance
        assertInstanceOf(RatedTugboatState.class, ratedTugboat.getPackageState());
        assertEquals(State.RATED, ratedTugboat.getPackageState().getState());
    }

    /*@Test
    void testRateMethodWithManifestFacility() throws TugboatException {
        String cargoId = "CARGO_006";
        String facilityCode = "FACILITY_002";
        TugboatAddress facilityAddress = createTestWarehouseAddress();
        PickupFacility pickupFacility = new PickupFacility(engineConfig, facilityCode, facilityAddress);
        pickupFacility.create();

        Tugboat tugboat = Tugboat.builder(engineConfig, cargoId)
            .pickupFacility(pickupFacility)
            .originAddress(createTestOriginAddress())
            .destinationAddress(createTestDestinationAddress())
            .parcels(new ArrayList<>(createTestParcels()))
            .build();

        // Initially should be in INITIAL state
        assertInstanceOf(InitialTugboatState.class, tugboat.getPackageState());
        assertEquals(State.INITIAL, tugboat.getPackageState().getState());

        // Load should transition to LOADED state
        Tugboat ratedTugboat = tugboat.rate();

        assertNotNull(ratedTugboat);
        assertSame(tugboat, ratedTugboat);
        assertInstanceOf(RatedTugboatState.class, ratedTugboat.getPackageState());
        assertEquals(State.RATED, ratedTugboat.getPackageState().getState());
        assertNotNull(ratedTugboat.getPickupFacility());
        assertEquals(facilityCode, ratedTugboat.getPickupFacility().getPickupFacilityCode());
    }*/

    /*@Test
    void testLoadMethodCallsSaveAutomatically() throws TugboatException {
        String cargoId = "CARGO_007";

        Tugboat tugboat = Tugboat.builder(engineConfig, cargoId)
            .originAddress(createTestOriginAddress())
            .destinationAddress(createTestDestinationAddress())
            .parcels(new ArrayList<>(createTestParcels()))
            .build();

        tugboat.rate();

        // Log the JSON that was saved to cache
        String savedJson = tugboat.toString();
        // logger.info("testLoadMethodCallsSaveAutomatically - Saved JSON: " + savedJson);


        // Verify it was saved to cache by creating a new instance and retrieving
        Tugboat retrievedTugboat = Tugboat.builder(engineConfig, cargoId).build();

        retrievedTugboat.retrieve();

        // Should have the same state as the loaded tugboat
        assertInstanceOf(RatedTugboatState.class, retrievedTugboat.getPackageState());
        assertEquals(State.RATED, retrievedTugboat.getPackageState().getState());
    }*/

    // Retrieve Method Tests
    @Test
    void testRetrieveMethodWithEmptyCache() throws TugboatException {
        String cargoId = "CARGO_008";

        Tugboat tugboat = Tugboat.builder(engineConfig, cargoId)
            .originAddress(createTestOriginAddress())
            .destinationAddress(createTestDestinationAddress())
            .parcels(new ArrayList<>(createTestParcels()))
            .build();

        // Retrieve from empty cache should return the same instance unchanged
        Tugboat retrievedTugboat = tugboat.retrieve();

        assertNotNull(retrievedTugboat);
        assertSame(tugboat, retrievedTugboat);
        assertEquals(cargoId, retrievedTugboat.getCargoId());
        assertInstanceOf(InitialTugboatState.class, retrievedTugboat.getPackageState());
    }

    /*@Test
    void testRetrieveMethodWithCachedData() throws TugboatException {
        String cargoId = "CARGO_009";

        // Create and load a tugboat to save it to cache
        Tugboat originalTugboat = Tugboat.builder(engineConfig, cargoId)
            .expectedDeliveryDate(LocalDate.now().plusDays(3))
            .originAddress(createTestOriginAddress())
            .destinationAddress(createTestDestinationAddress())
            .parcels(new ArrayList<>(createTestParcels()))
            .build();


        originalTugboat.rate(); // This saves to cache

        // Log the JSON that was saved to cache
        String savedJson = originalTugboat.toString();
         logger.info("testRetrieveMethodWithCachedData - Saved JSON: " + savedJson);

        // Create a new tugboat instance and retrieve from cache
        Tugboat newTugboat = Tugboat.builder(engineConfig, cargoId).build();


        Tugboat retrievedTugboat = newTugboat.retrieve();

        assertNotNull(retrievedTugboat);
        assertSame(newTugboat, retrievedTugboat);
        assertEquals(cargoId, retrievedTugboat.getCargoId());
        assertEquals(LocalDate.now().plusDays(3), retrievedTugboat.getExpectedDeliveryDate());
        assertInstanceOf(InitialTugboatState.class, retrievedTugboat.getPackageState());
        assertEquals(State.RATED, retrievedTugboat.getPackageState().getState());
    }*/

    /*@Test
    void testRetrieveMethodWithManifestFacility() throws TugboatException {
        String cargoId = "CARGO_010";
        String facilityCode = "FACILITY_003";
        TugboatAddress facilityAddress = createTestWarehouseAddress();
        PickupFacility pickupFacility = new PickupFacility(engineConfig, facilityCode, facilityAddress);
        pickupFacility.create();

        // Create and load a tugboat with manifest facility
        Tugboat originalTugboat = Tugboat.builder(engineConfig, cargoId)
            .pickupFacility(pickupFacility)
            .originAddress(createTestOriginAddress())
            .destinationAddress(createTestDestinationAddress())
            .parcels(new ArrayList<>(createTestParcels()))
            .build();

        originalTugboat.rate(); // This saves to cache

        // Log the JSON that was saved to cache
        String savedJson = originalTugboat.toString();
        // logger.info("testRetrieveMethodWithManifestFacility - Saved JSON: " + savedJson);

        // Create a new tugboat instance and retrieve from cache
        Tugboat newTugboat = Tugboat.builder(engineConfig, cargoId).build();

        Tugboat retrievedTugboat = newTugboat.retrieve();

        assertNotNull(retrievedTugboat);
        assertEquals(cargoId, retrievedTugboat.getCargoId());
        assertNotNull(retrievedTugboat.getPickupFacility());
        assertEquals(facilityCode, retrievedTugboat.getPickupFacility().getPickupFacilityCode());
        assertInstanceOf(InitialTugboatState.class, retrievedTugboat.getPackageState());
        assertEquals(State.RATED, retrievedTugboat.getPackageState().getState());
    }*/

    @Test
    void testRetrieveMethodWithInvalidJson() throws TugboatException {
        String cargoId = "CARGO_011";

        // Manually put invalid JSON in cache
        String cacheKey = engineConfig.getCachePrefix() + "TBCARGO:" + cargoId;
        engineConfig.getCacheClient().set(cacheKey, "invalid json");

        Tugboat tugboat = Tugboat.builder(engineConfig, cargoId)
            .originAddress(createTestOriginAddress())
            .destinationAddress(createTestDestinationAddress())
            .parcels(new ArrayList<>(createTestParcels()))
            .build();

        // Should throw TugboatException for invalid JSON in cache
        TugboatException exception = assertThrows(TugboatException.class, tugboat::retrieve);

        assertTrue(exception.getMessage().contains("Failed to deserialize cached Tugboat data"));
    }

    // Helper methods for creating test objects
    private static TugboatAddress createTestOriginAddress() {
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("name", "John Sender");
        addressMap.put("street1", "123 Origin Street");
        addressMap.put("city", "Origin City");
        addressMap.put("state", "CA");
        addressMap.put("zip", "90210");
        addressMap.put("country", "US");
        addressMap.put("company", "Origin Company");
        return new TugboatAddress(addressMap);
    }

    private static TugboatAddress createTestDestinationAddress() {
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("name", "Jane Recipient");
        addressMap.put("street1", "456 Destination Ave");
        addressMap.put("city", "Destination City");
        addressMap.put("state", "NY");
        addressMap.put("zip", "10001");
        addressMap.put("country", "US");
        addressMap.put("company", "Destination Company");
        return new TugboatAddress(addressMap);
    }

    private static TugboatAddress createTestReturnAddress() {
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("name", "Return Department");
        addressMap.put("street1", "789 Return Blvd");
        addressMap.put("city", "Return City");
        addressMap.put("state", "TX");
        addressMap.put("zip", "75001");
        addressMap.put("country", "US");
        addressMap.put("company", "Return Company");
        return new TugboatAddress(addressMap);
    }

    private static TugboatAddress createTestWarehouseAddress() {
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("name", "Test Warehouse");
        addressMap.put("street1", "100 Warehouse Blvd");
        addressMap.put("city", "Commerce City");
        addressMap.put("state", "CA");
        addressMap.put("zip", "90210");
        addressMap.put("country", "US");
        addressMap.put("company", "Test Logistics Co");
        return new TugboatAddress(addressMap);
    }

    private static List<TugboatParcel> createTestParcels() {
        List<TugboatParcel> parcels = new ArrayList<>();
        parcels.add(createTestParcel("parcel_1", 10.0f, 8.0f, 6.0f, 2.5f));
        parcels.add(createTestParcel("parcel_2", 12.0f, 10.0f, 8.0f, 3.0f));
        return parcels;
    }

    private static TugboatParcel createTestParcel(String id, float length, float width, float height, float weight) {
        Map<String, Object> parcelMap = new HashMap<>();
        parcelMap.put("length", length);
        parcelMap.put("width", width);
        parcelMap.put("height", height);
        parcelMap.put("weight", weight);
        return new TugboatParcel(parcelMap);
    }

    // Test implementation classes - now using real Tugboat DTOs instead of mocks

    // Mock shipping client for testing
    private static class TestShippingClient implements IShippingClient {
        @Override
        public IShipmentService getShipmentService() {
            return new TestShipmentService();
        }

        @Override
        public IOrderService getOrderService() {
            return new TestOrderService();
        }

        @Override
        public IAddressService getAddressService() {
            return new TestAddressService();
        }
        @Override public IBatchService getBatchService() { return null; }
        @Override
        public IParcelService getParcelService() {
            return new TestParcelService();
        }
        @Override public IRateService getRateService() { return null; }
        @Override public ICarrierAccountService getCarrierAccountService() { return null; }
        @Override public IRefundService getRefundService() { return null; }
        @Override public IScanFormService getScanFormService() { return null; }
        @Override public ITrackerService getTrackerService() { return null; }

        // Hook methods
        @Override public void subscribeToRequestHook(Function<IRequestHookResponses, Object> function) {}
        @Override public void unsubscribeFromRequestHook(Function<IRequestHookResponses, Object> function) {}
        @Override public void subscribeToResponseHook(Function<IResponseHookResponses, Object> function) {}
        @Override public void unsubscribeFromResponseHook(Function<IResponseHookResponses, Object> function) {}

        // Configuration methods
        @Override public int getConnectionTimeoutMilliseconds() { return 30000; }
        @Override public int getReadTimeoutMilliseconds() { return 60000; }
        @Override public String getApiKey() { return "test_key"; }
        @Override public String getApiVersion() { return "v1"; }
        @Override public String getApiBase() { return "https://test.api.com"; }
    }

    private static class TestShipmentService implements IShipmentService {
        @Override
        public IShipment create(Map<String, Object> params) throws TugboatException {
            return new TestShipment("test_shipment_" + System.currentTimeMillis());
        }

        // Minimal implementations for other methods
        @Override public IShipment retrieve(String id) throws TugboatException { return null; }
        @Override public IShipmentCollection all(Map<String, Object> params) throws TugboatException { return null; }
        @Override public IShipmentCollection getNextPage(IShipmentCollection collection) throws EndOfPaginationException { return null; }
        @Override public IShipmentCollection getNextPage(IShipmentCollection collection, Integer pageSize) throws EndOfPaginationException { return null; }

        @Override
        public IShipment newRates(String id) throws TugboatException {
            return null;
        }

        @Override
        public IShipment newRates(String id, Map<String, Object> params) throws TugboatException {
            return null;
        }

        @Override
        public List<ISmartRate> smartRates(String id) throws TugboatException {
            return List.of();
        }

        @Override
        public List<ISmartRate> smartRates(String id, Map<String, Object> params) throws TugboatException {
            return List.of();
        }

        @Override public IShipment buy(String id, Map<String, Object> params) throws TugboatException { return null; }

        @Override
        public IShipment buy(String id, IRate rate) throws TugboatException {
            return null;
        }

        @Override
        public IShipment buy(String id, IRate rate, String endShipperId) throws TugboatException {
            return null;
        }

        @Override
        public IShipment buy(String id, Map<String, Object> params, String endShipperId) throws TugboatException {
            return null;
        }

        @Override public IShipment refund(String id) throws TugboatException { return null; }

        @Override
        public IShipment refund(String id, Map<String, Object> params) throws TugboatException {
            return null;
        }

        @Override public IShipment insure(String id, Map<String, Object> params) throws TugboatException { return null; }

        @Override
        public ISmartRate lowestSmartRate(String id, int deliveryDay, ISmartRateAccuracy deliveryAccuracy) throws TugboatException {
            return null;
        }

        @Override
        public ISmartRate findLowestSmartRate(List<ISmartRate> smartRates, int deliveryDay, ISmartRateAccuracy deliveryAccuracy) throws TugboatException {
            return null;
        }

        @Override
        public IShipment generateForm(String id, String formType) throws TugboatException {
            return null;
        }

        @Override
        public IShipment generateForm(String id, String formType, Map<String, Object> formOptions) throws TugboatException {
            return null;
        }

        @Override
        public List<IEstimatedDeliveryDate> retrieveEstimatedDeliveryDate(String id, String plannedShipDate) throws TugboatException {
            return List.of();
        }

        @Override
        public List<IRecommendShipDateForShipmentResult> recommendShipDate(String id, String desiredDeliveryDate) throws TugboatException {
            return List.of();
        }

        @Override
        public void setShipment(IShipment shipment) throws TugboatException {

        }

        @Override public IShipment label(String id, Map<String, Object> params) throws TugboatException { return null; }
    }

    private static class TestOrderService implements IOrderService {
        @Override
        public IOrder create(Map<String, Object> params) throws TugboatException {
            return new TestOrder("test_order_" + System.currentTimeMillis());
        }

        // Minimal implementations for other methods
        @Override public IOrder retrieve(String id) throws TugboatException { return null; }

        @Override
        public IOrder newRates(String id) throws TugboatException {
            return null;
        }

        @Override
        public IOrder newRates(String id, Map<String, Object> params) throws TugboatException {
            return null;
        }
        @Override public IOrder buy(String id, Map<String, Object> params) throws TugboatException { return null; }

        @Override
        public IOrder buy(String id, IRate rate) throws TugboatException {
            return null;
        }
    }

    private static class TestAddressService implements IAddressService {
        @Override
        public IAddress create(Map<String, Object> params) throws TugboatException {
            return new TugboatAddress(params);
        }

        @Override
        public IAddress retrieve(String id) throws TugboatException {
            return createTestOriginAddress();
        }

        @Override
        public IAddressCollection all(Map<String, Object> params) throws TugboatException {
            return null;
        }

        @Override
        public IAddressCollection getNextPage(IAddressCollection collection) throws EndOfPaginationException {
            throw new EndOfPaginationException("No more pages");
        }

        @Override
        public IAddressCollection getNextPage(IAddressCollection collection, Integer pageSize) throws EndOfPaginationException {
            throw new EndOfPaginationException("No more pages");
        }

        @Override
        public IAddress createAndVerify(Map<String, Object> params) throws TugboatException {
            return create(params);
        }

        @Override
        public IAddress verify(String id) throws TugboatException {
            return retrieve(id);
        }
    }

    private static class TestParcelService implements IParcelService {
        @Override
        public IParcel create(Map<String, Object> params) throws TugboatException {
            return new TugboatParcel(params);
        }

        @Override
        public IParcel retrieve(String id) throws TugboatException {
            Map<String, Object> parcelMap = new HashMap<>();
            parcelMap.put("length", 10.0f);
            parcelMap.put("width", 8.0f);
            parcelMap.put("height", 6.0f);
            parcelMap.put("weight", 2.5f);
            return new TugboatParcel(parcelMap);
        }
    }

    public static class TestShipment implements IShipment {
        private final String id;

        public TestShipment(String id) {
            this.id = id;
        }

        public TestShipment(Map<String, Object> map) {
            this.id = MapValues.asString(map, "id");
        }

        // IEasyPostResource methods
        @Override public String getId() { return id; }
        @Override public String getMode() { return "test"; }
        @Override public String getObject() { return "Shipment"; }
        @Override public Date getCreatedAt() { return new Date(); }
        @Override public Date getUpdatedAt() { return new Date(); }
        @Override public String toString() { return "TestShipment{id='" + id + "'}"; }
        @Override public String prettyPrint() { return toString(); }

        // IRateResponse methods
        @Override public String getService() { return "Ground"; }
        @Override public String getReference() { return "ref_" + id; }
        @Override public Boolean getIsReturn() { return false; }
        @Override public IAddress getToAddress() { return null; }
        @Override public IAddress getBuyerAddress() { return null; }
        @Override public IAddress getFromAddress() { return null; }
        @Override public IAddress getReturnAddress() { return null; }
        @Override public List<IRate> getRates() { return new ArrayList<>(); }
        @Override public Map<String, Object> getOptions() { return new HashMap<>(); }
        @Override public List<ICarrierAccount> getCarrierAccounts() { return new ArrayList<>(); }
        @Override public IRate lowestRate() throws TugboatException { return null; }
        @Override public IRate lowestRate(List<String> carriers, List<String> services) throws TugboatException { return null; }
        @Override public IRate lowestRate(List<String> carriers) throws TugboatException { return null; }

        // IShipment-specific methods
        @Override public IParcel getParcel() { return null; }
        @Override public ICustomsInfo getCustomsInfo() { return null; }
        @Override public IRate getSelectedRate() { return null; }

        @Override
        public void setSelectedRate(IRate rate) {

        }

        @Override public IPostageLabel getPostageLabel() { return null; }
        @Override public IScanForm getScanForm() { return null; }
        @Override public String getOrderId() { return null; }
        @Override public List<IForm> getForms() { return new ArrayList<>(); }
        @Override public ITracker getTracker() { return null; }
        @Override public String getInsurance() { return null; }
        @Override public String getTrackingCode() { return null; }
        @Override public String getStatus() { return "pending"; }
        @Override public String getRefundStatus() { return null; }
        @Override public String getBatchId() { return null; }
        @Override public String getBatchStatus() { return null; }
        @Override public String getBatchMessage() { return null; }
        @Override public String getUspsZone() { return null; }
        @Override public List<IShipmentMessage> getMessages() { return new ArrayList<>(); }
        @Override public List<ITaxIdentifier> getTaxIdentifiers() { return new ArrayList<>(); }
        @Override public List<IFee> getFees() { return new ArrayList<>(); }

        @Override
        public JsonElement toJson() {
            return new com.google.gson.Gson().toJsonTree(this);
        }

        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("object", "Shipment");
            return map;
        }

        @Override
        public String getProviderType() {
            return "test";
        }
    }

    public static class TestOrder implements IOrder {
        private final String id;

        public TestOrder(String id) {
            this.id = id;
        }

        public TestOrder(Map<String, Object> map) {
            this.id = MapValues.asString(map, "id");
        }

        // IEasyPostResource methods
        @Override public String getId() { return id; }
        @Override public String getMode() { return "test"; }
        @Override public String getObject() { return "Order"; }
        @Override public Date getCreatedAt() { return new Date(); }
        @Override public Date getUpdatedAt() { return new Date(); }
        @Override public String toString() { return "TestOrder{id='" + id + "'}"; }
        @Override public String prettyPrint() { return toString(); }

        // IRateResponse methods
        @Override public String getService() { return "Ground"; }
        @Override public String getReference() { return "ref_" + id; }
        @Override public Boolean getIsReturn() { return false; }
        @Override public IAddress getToAddress() { return null; }
        @Override public IAddress getBuyerAddress() { return null; }
        @Override public IAddress getFromAddress() { return null; }
        @Override public IAddress getReturnAddress() { return null; }
        @Override public List<IRate> getRates() { return new ArrayList<>(); }
        @Override public Map<String, Object> getOptions() { return new HashMap<>(); }
        @Override public List<ICarrierAccount> getCarrierAccounts() { return new ArrayList<>(); }
        @Override public IRate lowestRate() throws TugboatException { return null; }
        @Override public IRate lowestRate(List<String> carriers, List<String> services) throws TugboatException { return null; }
        @Override public IRate lowestRate(List<String> carriers) throws TugboatException { return null; }

        // IOrder-specific methods
        @Override public ICustomsInfo getCustomsInfo() { return null; }
        @Override public List<IShipment> getShipments() { return new ArrayList<>(); }
        @Override public List<IShipmentMessage> getMessages() { return new ArrayList<>(); }

        @Override
        public JsonElement toJson() {
            return new com.google.gson.Gson().toJsonTree(this);
        }

        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("object", "Order");
            return map;
        }

        @Override
        public String getProviderType() {
            return "test";
        }
    }
}
