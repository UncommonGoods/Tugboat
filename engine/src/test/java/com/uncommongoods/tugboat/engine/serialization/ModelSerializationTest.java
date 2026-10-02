package com.uncommongoods.tugboat.engine.serialization;

import com.google.gson.Gson;
import com.uncommongoods.tugboat.engine.model.TugboatAddress;
import com.uncommongoods.tugboat.engine.model.TugboatParcel;
import com.uncommongoods.tugboat.engine.ports.shipping.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-tripping of polymorphic shipping models.
 *
 * <p>The behaviour under test is that a model comes back as the class that wrote
 * it. Before provider identity was authoritative, an unrecognized one was matched
 * against every registered class until a constructor accepted it -- which one
 * always did, since they read the keys they know and ignore the rest -- so a model
 * could come back as another provider's type with most of its fields silently gone.
 */
class ModelSerializationTest {

    private static final Gson GSON = TugboatGson.cargo();

    // A provider the engine has no compile-time knowledge of, contributed purely
    // through META-INF/services, is discovered and usable.
    @Test
    void roundTripsAProviderTheEngineWasNeverCompiledAgainst() {
        VendorModels.VendorAddress original = new VendorModels.VendorAddress("v_1", "Ada", "Portland");

        IAddress restored = GSON.fromJson(GSON.toJson(original, IAddress.class), IAddress.class);

        assertInstanceOf(VendorModels.VendorAddress.class, restored);
        assertEquals("v_1", restored.getId());
        assertEquals("Ada", restored.getName());
        assertEquals("Portland", restored.getCity());
    }

    @Test
    void roundTripsTheEnginesOwnModels() {
        TugboatAddress address = new TugboatAddress(
            "Ada", "1 Main St", null, "Portland", "OR", "97201", "US", "555-0100", "ada@example.com", true);

        IAddress restored = GSON.fromJson(GSON.toJson(address, IAddress.class), IAddress.class);

        assertInstanceOf(TugboatAddress.class, restored);
        assertEquals("Ada", restored.getName());
        assertEquals("Portland", restored.getCity());
        assertEquals("97201", restored.getZip());
    }

    @Test
    void writesTheProviderTokenIntoTheJson() {
        String json = GSON.toJson(new VendorModels.VendorAddress("v_1", "Ada", "Portland"), IAddress.class);

        assertEquals("vendor", GSON.fromJson(json, Map.class).get(EntityType.TYPE_KEY));
    }

    // The regression test for the swallow: an unknown token must fail, not be
    // quietly rebuilt as whichever registered class tolerates the data.
    @Test
    void refusesAnUnknownProviderTokenInsteadOfGuessing() {
        String json = "{\"id\":\"x_1\",\"name\":\"Ada\",\"" + EntityType.TYPE_KEY + "\":\"nosuchvendor\"}";

        // Gson wraps what a type adapter throws, so assert on the message chain.
        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> GSON.fromJson(json, IAddress.class));

        String message = thrown.getMessage();
        assertTrue(message.contains("nosuchvendor"), message);
        assertTrue(message.contains("tugboat"),
            "the message should list what is registered: " + message);
    }

    // A model whose getProviderType() disagrees with its provider's token would
    // write JSON nothing could read. Catch it on the write, not on some later read.
    @Test
    void refusesToWriteAModelWhoseTokenIsNotRegistered() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
            () -> GSON.toJson(new UnregisteredAddress(), IAddress.class));

        assertTrue(thrown.getMessage().contains("wat"), thrown.getMessage());
    }

    @Test
    void nestedModelsKeepTheirOwnIdentity() {
        VendorModels.VendorShipment shipment = new VendorModels.VendorShipment(
            "v_ship_1",
            new VendorModels.VendorAddress("v_addr_1", "Ada", "Portland"),
            List.of(new VendorModels.VendorRate("v_rate_1", "UPS", 12.50f)));

        IShipment restored = GSON.fromJson(GSON.toJson(shipment, IShipment.class), IShipment.class);

        assertInstanceOf(VendorModels.VendorShipment.class, restored);
        assertInstanceOf(VendorModels.VendorAddress.class, restored.getToAddress());
        assertEquals("Ada", restored.getToAddress().getName());
        assertEquals(1, restored.getRates().size());
        assertEquals("UPS", restored.getRates().get(0).getCarrier());
        assertEquals(12.50f, restored.getRates().get(0).getRate());
    }

    // Cargo written before nested tagging has children with no token of their own.
    // They must still load, as the provider that wrote the parent.
    @Test
    void untaggedChildrenFallBackToTheirParentsProvider() {
        String json = "{\"id\":\"v_ship_1\","
            + "\"toAddress\":{\"id\":\"v_addr_1\",\"name\":\"Ada\",\"city\":\"Portland\"},"
            + "\"rates\":[{\"id\":\"v_rate_1\",\"carrier\":\"UPS\",\"rate\":12.5}],"
            + "\"" + EntityType.TYPE_KEY + "\":\"vendor\"}";

        IShipment restored = GSON.fromJson(json, IShipment.class);

        assertInstanceOf(VendorModels.VendorAddress.class, restored.getToAddress());
        assertEquals("Ada", restored.getToAddress().getName());
        assertEquals("UPS", restored.getRates().get(0).getCarrier());
    }

    // A child that does carry its own token is rebuilt as that provider, even when
    // it differs from the parent's.
    @Test
    void taggedChildrenOverrideTheParentsProvider() {
        String json = "{\"id\":\"v_ship_1\","
            + "\"toAddress\":{\"name\":\"Ada\",\"city\":\"Portland\",\""
            + EntityType.TYPE_KEY + "\":\"tugboat\"},"
            + "\"" + EntityType.TYPE_KEY + "\":\"vendor\"}";

        IShipment restored = GSON.fromJson(json, IShipment.class);

        assertInstanceOf(VendorModels.VendorShipment.class, restored);
        assertInstanceOf(TugboatAddress.class, restored.getToAddress());
        assertEquals("Ada", restored.getToAddress().getName());
    }

    @Test
    void nullModelsSurviveTheRoundTrip() {
        assertNull(GSON.fromJson(GSON.toJson(null, IAddress.class), IAddress.class));
    }

    // Both engine models serialize createdAt but historically read created_at, so
    // the timestamps were dropped every time. Both spellings must load.
    @Test
    void parcelAcceptsEitherSpellingOfItsTimestampKeys() {
        TugboatParcel camel = new TugboatParcel(Map.of("id", "p_1", "createdAt", "2026-08-25T10:00:00Z"));
        TugboatParcel snake = new TugboatParcel(Map.of("id", "p_2", "created_at", "2026-08-25T10:00:00Z"));

        assertNotNull(camel.getCreatedAt(), "camelCase key should be read");
        assertNotNull(snake.getCreatedAt(), "legacy snake_case key should still be read");
        assertEquals(camel.getCreatedAt(), snake.getCreatedAt());
    }

    @Test
    void parcelTimestampSurvivesARoundTrip() {
        TugboatParcel parcel = new TugboatParcel(Map.of("id", "p_1", "createdAt", "2026-08-25T10:00:00Z"));

        IParcel restored = GSON.fromJson(GSON.toJson(parcel, IParcel.class), IParcel.class);

        assertEquals(parcel.getCreatedAt(), restored.getCreatedAt());
    }

    /** Claims a provider token nothing registers. */
    private static final class UnregisteredAddress extends VendorModels.VendorAddress {
        UnregisteredAddress() {
            super("u_1", "Nobody", "Nowhere");
        }

        @Override
        public String getProviderType() {
            return "wat";
        }
    }
}
