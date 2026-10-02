// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.manifest;

import com.google.gson.JsonElement;
import com.uncommongoods.tugboat.engine.ports.cache.ICacheClient;
import com.uncommongoods.tugboat.engine.ports.cache.JedisRedisAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddress;
import com.uncommongoods.tugboat.engine.ports.shipping.model.MapValues;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddressCollection;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddressVerifications;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRequestHookResponses;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IResponseHookResponses;
import com.uncommongoods.tugboat.engine.ports.shipping.service.*;
import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class PickupFacilityTest {

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

    @Test
    void testCreateSaveAndRetrieveManifestFacility() throws TugboatException {
        String facilityCode = "FACILITY_001";
        IAddress facilityAddress = createTestWarehouseAddress();

        PickupFacility facility = new PickupFacility(engineConfig, facilityCode, facilityAddress);
        facility.create();

        assertNotNull(facility.getPickupFacilityCode());
        assertEquals(facilityCode, facility.getPickupFacilityCode());
        assertNotNull(facility.getAddress());

        PickupFacility retrievedFacility = new PickupFacility(engineConfig, facilityCode, facilityAddress);
        retrievedFacility.retrieve();

        assertEquals(facilityCode, retrievedFacility.getPickupFacilityCode());
        assertNotNull(retrievedFacility.getPickupGroups());
        assertTrue(retrievedFacility.getPickupGroups().isEmpty());
    }

    @Test
    void testCreatePickupGroupAndRetrieve() throws TugboatException {
        String facilityCode = "FACILITY_002";
        String manifestGroupName = "MORNING SHIPMENTS";
        String carrierAccountId = "CARRIER_123";
        LocalDate manifestDate = LocalDate.now();
        IAddress facilityAddress = createTestWarehouseAddress();

        PickupFacility facility = new PickupFacility(engineConfig, facilityCode, facilityAddress);
        facility.create();

        String groupId = facility.createPickupGroup(manifestGroupName, carrierAccountId, manifestDate);
        PickupGroup group = facility.getPickupGroup(groupId);
        assertNotNull(group);
        assertNotNull(group.getPickupGroupId());
        assertEquals(manifestGroupName, group.getPickupGroupName());
        assertEquals(carrierAccountId, group.getCarrierAccountId());
        assertEquals(manifestDate, group.getPickupDate());

        PickupFacility retrievedFacility = new PickupFacility(engineConfig, facilityCode, facilityAddress);
        retrievedFacility.retrieve();

        List<PickupGroup> groups = retrievedFacility.getPickupGroups();
        assertEquals(1, groups.size());

        PickupGroup retrievedGroup = groups.getFirst();
        assertEquals(group.getPickupGroupId(), retrievedGroup.getPickupGroupId());
        assertEquals(manifestGroupName, retrievedGroup.getPickupGroupName());
        assertEquals(carrierAccountId, retrievedGroup.getCarrierAccountId());
        assertEquals(manifestDate, retrievedGroup.getPickupDate());
    }

    @Test
    void testManifestFacilityRequiresCacheClient() {
        EngineConfig configWithoutCache = EngineConfig.builder().build();
        IAddress facilityAddress = createTestWarehouseAddress();

        assertThrows(IllegalStateException.class, () -> {
            new PickupFacility(configWithoutCache, "FACILITY_003", facilityAddress);
        });
    }

    @Test
    void testManifestFacilityRequiresFacilityCode() {
        IAddress facilityAddress = createTestWarehouseAddress();

        assertThrows(IllegalArgumentException.class, () -> {
            new PickupFacility(engineConfig, null, facilityAddress);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new PickupFacility(engineConfig, "", facilityAddress);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new PickupFacility(engineConfig, "   ", facilityAddress);
        });
    }

    @Test
    void testCreatePickupGroupRequiresName() throws TugboatException {
        IAddress facilityAddress = createTestWarehouseAddress();
        PickupFacility facility = new PickupFacility(engineConfig, "FACILITY_004", facilityAddress);
        facility.create();

        assertThrows(IllegalArgumentException.class, () -> {
            facility.createPickupGroup(null, "CARRIER_123", LocalDate.now());
        });

        assertThrows(IllegalArgumentException.class, () -> {
            facility.createPickupGroup("", "CARRIER_123", LocalDate.now());
        });

        assertThrows(IllegalArgumentException.class, () -> {
            facility.createPickupGroup("   ", "CARRIER_123", LocalDate.now());
        });
    }

    @Test
    void testGetPickupGroupByName() throws TugboatException {
        String facilityCode = "FACILITY_005";
        String manifestGroupName = "EVENING BATCH";
        IAddress facilityAddress = createTestWarehouseAddress();

        PickupFacility facility = new PickupFacility(engineConfig, facilityCode, facilityAddress);
        facility.create();

        String groupId = facility.createPickupGroup(manifestGroupName, "CARRIER_456", LocalDate.now());
        PickupGroup group = facility.getPickupGroup(groupId);
        PickupGroup foundGroup = facility.getPickupGroupByName(manifestGroupName);
        assertNotNull(foundGroup);
        assertEquals(group.getPickupGroupId(), foundGroup.getPickupGroupId());
        assertEquals(manifestGroupName, foundGroup.getPickupGroupName());

        assertNull(facility.getPickupGroupByName("Nonexistent Group"));
        assertNull(facility.getPickupGroupByName(null));
    }

    @Test
    void testClosePickupGroupByName() throws TugboatException {
        String facilityCode = "FACILITY_006";
        String manifestGroupName = "NIGHT SHIPMENTS";
        LocalDate closeDate = LocalDate.now();
        IAddress facilityAddress = createTestWarehouseAddress();

        PickupFacility facility = new PickupFacility(engineConfig, facilityCode, facilityAddress);
        facility.create();

        String groupId = facility.createPickupGroup(manifestGroupName, "CARRIER_789", LocalDate.now());

        PickupGroup closedGroup = facility.closePickupGroup(manifestGroupName, closeDate);
        assertNotNull(closedGroup);
        String ClosedGroupId = closedGroup.getPickupGroupId();
        assertNotEquals(groupId, ClosedGroupId);
    }

    @Test
    void testClosePickupGroupValidation() throws TugboatException {
        String facilityCode = "FACILITY_007";
        IAddress facilityAddress = createTestWarehouseAddress();
        PickupFacility facility = new PickupFacility(engineConfig, facilityCode, facilityAddress);
        facility.create();

        assertThrows(IllegalArgumentException.class, () -> {
            facility.closePickupGroup("Nonexistent Group", LocalDate.now());
        });

        assertThrows(IllegalArgumentException.class, () -> {
            facility.closePickupGroup(null, LocalDate.now());
        });

        assertThrows(IllegalArgumentException.class, () -> {
            facility.closePickupGroup("", LocalDate.now());
        });

        facility.createPickupGroup("Test Group", "CARRIER_123", LocalDate.now());
        assertThrows(IllegalArgumentException.class, () -> {
            facility.closePickupGroup("Test Group", null);
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            facility.closePickupGroup("Test Group", LocalDate.now().minusDays(1));
        });
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("cannot be in the past"));
    }

    @Test
    void testManifestBatchBulkOperations() throws TugboatException {
        String facilityCode = "FACILITY_008";
        IAddress facilityAddress = createTestWarehouseAddress();
        PickupFacility facility = new PickupFacility(engineConfig, facilityCode, facilityAddress);
        facility.create();

        String groupId = facility.createPickupGroup("Bulk Test", "CARRIER_BULK", LocalDate.now());
        PickupGroup group = facility.getPickupGroup(groupId);
        ManifestBatch batch = new ManifestBatch(engineConfig, groupId, group.getManifestBatchId());
        batch.create();

        List<ManifestBatchCargo> cargos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String cargoId = "CARGO_"+i;
            String externalPackageId = "shp_1234567890";
            CarrierService carrierService = new CarrierService("UPS", "Ground");
            List<String> trackingCodes = Arrays.asList("123425asdsf", "3453kjfslj", "234234jksk");
            ManifestBatchCargo cargo = new ManifestBatchCargo(cargoId, externalPackageId, carrierService, trackingCodes);
            cargos.add(cargo);
        }

        batch.addCargo(cargos);
        assertTrue(batch.isOpen());
        assertEquals(3, batch.getManifestBatchCargo().size());

        List<String> cargoIdsToRemove = List.of("CARGO_0", "CARGO_2");
        batch.removeCargo(cargoIdsToRemove);
        assertEquals(1, batch.getManifestBatchCargo().size());
        assertEquals("CARGO_1", batch.getManifestBatchCargo().get(0).getCargoId());


        assertNull(batch.getScanFormId());
        batch.setScanFormId("scanform_123");
        assertEquals("scanform_123", batch.getScanFormId());

        batch.save();
        ManifestBatch retrievedBatch = new ManifestBatch(engineConfig, group.getPickupGroupId(), group.getManifestBatchId());
        retrievedBatch.retrieve();
        assertEquals("scanform_123", retrievedBatch.getScanFormId());
    }

    @Test
    void testManifestBatchCloseWithoutShippingClient() throws TugboatException {
        String facilityCode = "FACILITY_009";
        IAddress facilityAddress = createTestWarehouseAddress();
        PickupFacility facility = new PickupFacility(engineConfig, facilityCode, facilityAddress);
        facility.create();

        String groupId = facility.createPickupGroup("ScanForm Test", "CARRIER_SCAN", LocalDate.now());
        PickupGroup group = facility.getPickupGroup(groupId);
        ManifestBatch batch = new ManifestBatch(engineConfig, group.getPickupGroupId(), group.getManifestBatchId());
        batch.create();

        String cargoId = "CARGO_SF1";
        String externalPackageId = "shp_1234567890";
        CarrierService carrierService = new CarrierService("UPS", "Ground");
        List<String> trackingCodes = Arrays.asList("123425asdsf", "3453kjfslj", "234234jksk");
        ManifestBatchCargo cargo1 = new ManifestBatchCargo(cargoId, externalPackageId, carrierService, trackingCodes);
        cargo1.setCargoId("CARGO_SF1");
        cargo1.setExternalPackageId("shp_1234567890");

        String cargoId2 = "CARGO_SF1";
        String externalPackageId2 = "shp_0987654321";
        CarrierService carrierService2 = new CarrierService("UPS", "Ground");
        List<String> trackingCodes2 = Arrays.asList("3345jlaj489", "9934kfj389dsc", "454kjk3j5454");
        ManifestBatchCargo cargo2 = new ManifestBatchCargo(cargoId2, externalPackageId2, carrierService2, trackingCodes2);

        batch.addCargo(cargo1);
        batch.addCargo(cargo2);

        batch.close();

        assertTrue(batch.isClosed());
        assertNull(batch.getScanFormId()); // No ScanForm created since no shipping client
    }

    private static IAddress createTestWarehouseAddress() {
        return new TestAddress(
            "test_addr_" + System.currentTimeMillis(),
            "Test Warehouse",
            "100 Warehouse Blvd",
            "Commerce City",
            "CA",
            "90210",
            "US",
            "Test Logistics Co"
        );
    }

    public static class TestAddress implements IAddress {
        private final String id;
        private final String name;
        private final String street1;
        private final String city;
        private final String state;
        private final String zip;
        private final String country;
        private final String company;

        public TestAddress(String id, String name, String street1, String city, String state, String zip, String country, String company) {
            this.id = id;
            this.name = name;
            this.street1 = street1;
            this.city = city;
            this.state = state;
            this.zip = zip;
            this.country = country;
            this.company = company;
        }

        public TestAddress(Map<String, Object> map) {
            this.id = MapValues.asString(map, "id");
            this.name = MapValues.asString(map, "name");
            this.street1 = MapValues.asString(map, "street1");
            this.city = MapValues.asString(map, "city");
            this.state = MapValues.asString(map, "state");
            this.zip = MapValues.asString(map, "zip");
            this.country = MapValues.asString(map, "country");
            this.company = MapValues.asString(map, "company");
        }

        @Override public String getId() { return id; }
        @Override public String getName() { return name; }
        @Override public String getStreet1() { return street1; }
        @Override public String getCity() { return city; }
        @Override public String getState() { return state; }
        @Override public String getZip() { return zip; }
        @Override public String getCountry() { return country; }
        @Override public String getCompany() { return company; }

        @Override public String getMode() { return "test"; }
        @Override public String getObject() { return "Address"; }
        @Override public Date getCreatedAt() { return new Date(); }
        @Override public Date getUpdatedAt() { return new Date(); }
        @Override public String getStreet2() { return null; }
        @Override public String getPhone() { return null; }
        @Override public String getEmail() { return null; }
        @Override public String getMessage() { return null; }
        @Override public String getCarrierFacility() { return null; }
        @Override public String getFederalTaxId() { return null; }
        @Override public Boolean getResidential() { return false; }
        @Override public IAddressVerifications getVerifications() { return null; }
        @Override public String toString() { return "TestAddress{id='" + id + "'}"; }
        @Override public String prettyPrint() { return toString(); }

        @Override
        public JsonElement toJson() {
            return new com.google.gson.Gson().toJsonTree(this);
        }

        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("name", name);
            map.put("street1", street1);
            map.put("city", city);
            map.put("state", state);
            map.put("zip", zip);
            map.put("country", country);
            map.put("company", company);
            return map;
        }

        @Override
        public String getProviderType() {
            return "test";
        }
    }

    // Mock shipping client for testing
    private static class TestShippingClient implements IShippingClient {
        @Override
        public IAddressService getAddressService() {
            return new TestAddressService();
        }

        @Override public IBatchService getBatchService() { return null; }
        @Override public IParcelService getParcelService() { return null; }
        @Override public IRateService getRateService() { return null; }
        @Override public IShipmentService getShipmentService() { return null; }
        @Override public ICarrierAccountService getCarrierAccountService() { return null; }
        @Override public IOrderService getOrderService() { return null; }
        @Override public IRefundService getRefundService() { return null; }
        @Override public IScanFormService getScanFormService() { return null; }
        @Override public ITrackerService getTrackerService() { return null; }

        @Override public void subscribeToRequestHook(Function<IRequestHookResponses, Object> function) {}
        @Override public void unsubscribeFromRequestHook(Function<IRequestHookResponses, Object> function) {}
        @Override public void subscribeToResponseHook(Function<IResponseHookResponses, Object> function) {}
        @Override public void unsubscribeFromResponseHook(Function<IResponseHookResponses, Object> function) {}

        @Override public int getConnectionTimeoutMilliseconds() { return 30000; }
        @Override public int getReadTimeoutMilliseconds() { return 60000; }
        @Override public String getApiKey() { return "test_key"; }
        @Override public String getApiVersion() { return "v1"; }
        @Override public String getApiBase() { return "https://test.api.com"; }
    }

    private static class TestAddressService implements IAddressService {
        @Override
        public IAddress create(Map<String, Object> params) throws TugboatException {
            String name = (String) params.get("name");
            String street1 = (String) params.get("street1");
            String city = (String) params.get("city");
            String state = (String) params.get("state");
            String zip = (String) params.get("zip");
            String country = (String) params.get("country");
            String company = (String) params.get("company");

            return new TestAddress(
                "created_addr_" + System.currentTimeMillis(),
                name != null ? name : "Created Address",
                street1 != null ? street1 : "123 Created St",
                city != null ? city : "Created City",
                state != null ? state : "CA",
                zip != null ? zip : "12345",
                country != null ? country : "US",
                company != null ? company : "Created Company"
            );
        }

        @Override
        public IAddress retrieve(String id) throws TugboatException {
            return createTestWarehouseAddress();
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
}
