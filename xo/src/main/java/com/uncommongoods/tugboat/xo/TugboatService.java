// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.xo;

import com.easypost.exception.EasyPostException;
import com.easypost.exception.General.MissingParameterError;
import com.easypost.model.Tracker;
import com.easypost.service.EasyPostClient;
import com.google.gson.JsonArray;
import io.micronaut.context.annotation.Value;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import com.uncommongoods.tugboat.engine.Tugboat;
import com.uncommongoods.tugboat.engine.TugboatOptions;
import com.uncommongoods.tugboat.engine.ports.cache.CacheClientFactory;
import com.uncommongoods.tugboat.engine.ports.cache.ICacheClient;
import com.uncommongoods.tugboat.engine.ports.config.TugboatSettings;
import com.uncommongoods.tugboat.engine.ports.shipping.model.BoxSpec;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IParcel;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IPostageLabel;
import com.uncommongoods.tugboat.engine.ports.shipping.service.BoxCatalog;
import com.uncommongoods.tugboat.engine.ports.shipping.service.IShippingClient;
import com.uncommongoods.tugboat.engine.ports.shipping.service.ShippingClientAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.service.ShippingClientFactory;
import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.hooks.TugboatHookProvider;
import com.uncommongoods.tugboat.engine.hooks.impl.TugboatHooks;
import com.uncommongoods.tugboat.engine.manifest.PickupFacility;
import com.uncommongoods.tugboat.engine.model.TugboatParcel;
import com.uncommongoods.tugboat.engine.state.ShipmentComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.uncommongoods.tugboat.engine.state.State.*;

@Singleton
public class TugboatService {

    private static final Logger logger = LoggerFactory.getLogger(TugboatService.class);

    /** Optional env var naming which cache client factory to use. */
    private static final String CACHE_TYPE_SETTING = "CACHE_TYPE";

    /** Discovered separately and registered as "default"; see createEngineConfig. */
    private static final String EASYPOST_CLIENT_TYPE = "easypost";

    @Value("${tugboat.cache.prefix}")
    private String cacheClientPrefix;

    @Value("${tugboat.easypost.api.key}")
    private String easypostApiKey;

    @Value("${tugboat.esw.api.key}")
    private String eswApiKey;

    @Value("${tugboat.esw.security.url}")
    private String eswSecurityUrl;

    @Value("${tugboat.esw.package.url}")
    private String eswPackageUrl;

    @Value("${tugboat.esw.brand.code}")
    private String eswBrandCode;

    @Value("${tugboat.esw.grant.type}")
    private String eswGrantType;

    @Value("${tugboat.esw.client.id}")
    private String eswClientId;

    @Value("${tugboat.parsel.api.key}")
    private String parselApiKey;

    private TugboatHookProvider hooks;

    private EngineConfig engineConfig;

    /** Optional; null when no {@link BoxCatalog} is on the classpath. */
    private BoxCatalog boxCatalog;

    @PostConstruct
    private void initializeService() {
        publishVendorSettings();
        this.hooks = new TugboatHooks();
        this.engineConfig = createEngineConfig();
        this.boxCatalog = loadBoxCatalog();
    }

    @PreDestroy
    private void shutdownService() {
        // The hook provider owns a connection pool; release it on shutdown.
        if (hooks != null) {
            try {
                hooks.close();
            } catch (Exception e) {
                logger.warn("Failed to close hook provider: {}", e.getMessage());
            }
        }
        // A box catalog may hold one too; one that holds nothing needs no teardown.
        if (boxCatalog instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                logger.warn("Failed to close box catalog: {}", e.getMessage());
            }
        }
    }

    /**
     * Discover the {@link BoxCatalog} that resolves a packer's box id to
     * dimensions, or {@code null} when none is registered — box ids come from
     * deployment-specific systems, so the catalog is an optional provider like
     * the shipping and cache clients. Without one, {@link #validateBoxSize} is
     * a no-op and a box purchase behaves exactly like a plain purchase.
     */
    private BoxCatalog loadBoxCatalog() {
        for (BoxCatalog candidate : ServiceLoader.load(BoxCatalog.class)) {
            logger.info("Using box catalog {}", candidate.getClass().getName());
            return candidate;
        }
        logger.info("No box catalog registered; box size validation is disabled");
        return null;
    }

    /**
     * Expose the vendor client configuration from {@code application.yml} to
     * adapter factories, which read their {@link ShippingClientFactory#configKeys()}
     * through {@link TugboatSettings}. Names are namespaced by client type, so
     * ESW's "API Key" resolves as {@code esw API Key} (equivalently the
     * {@code ESW_API_KEY} environment variable).
     *
     * <p>Publishing rather than reading the environment directly keeps XO's
     * configuration in one place while letting optional adapters stay optional:
     * a key published for an adapter that is not on the classpath is simply
     * never read.
     */
    private void publishVendorSettings() {
        Map<String, String> settings = new LinkedHashMap<>();
        settings.put("esw API Key", eswApiKey);
        settings.put("esw Security URL", eswSecurityUrl);
        settings.put("esw Package URL", eswPackageUrl);
        settings.put("esw Brand Code", eswBrandCode);
        settings.put("esw Grant Type", eswGrantType);
        settings.put("esw Client ID", eswClientId);
        settings.put("parsel API Key", parselApiKey);
        TugboatSettings.publish(settings);
    }

    public Tugboat getTugboat(String shipmentId) throws TugboatException {
        var builder = Tugboat.builder(engineConfig, shipmentId)
                .options(getTugboatOptions());
        PickupFacility pickupFacility = getPickupFacility();
        if (pickupFacility != null) {
            builder.pickupFacility(pickupFacility);
        }
        return builder.build().retrieve();
    }

    public String initializeTugboat(String shipmentId) throws TugboatException {
        Tugboat tugboat = getTugboat(shipmentId);
        return tugboat.initialize().toString();
    }

    public String rateTugboat(String shipmentId) throws TugboatException {
        Tugboat tugboat = getTugboat(shipmentId);
        return tugboat.rate().toString();
    }

    public String rerateTugboat(String shipmentId) throws TugboatException {
        Tugboat tugboat = getTugboat(shipmentId);
        if (tugboat.getPackageState().getState().ordinal() <= SHOPPED.ordinal()) {
            tugboat.reset();
        }
        return tugboat.rate().toString();
    }

    public String shopTugboat(String shipmentId) throws TugboatException {
        Tugboat tugboat = getTugboat(shipmentId);
        return tugboat.shop().toString();
    }

    public String  purchaseTugboat(String shipmentId) throws TugboatException {
        Tugboat tugboat = getTugboat(shipmentId);
        return tugboat.purchase().toString();
    }

    /**
     * Reconcile a shipment's parcel with the box the packer actually used,
     * before postage is bought.
     *
     * <p>Only a single-parcel domestic non-letter shipment is eligible, and only
     * when the registered {@link BoxCatalog} knows the box id and its dimensions
     * differ from the parcel's by more than half a unit on any side. When they
     * do, an already-purchased label is voided, the shipment is rewound to
     * INITIAL if it had progressed no further than SHOPPED, and it is
     * re-initialized against the box's dimensions with everything but the parcel
     * preserved.
     *
     * <p>With no catalog registered, or an id it does not recognize, this
     * returns without touching the shipment.
     */
    public void validateBoxSize(String shipmentId, int boxId) throws TugboatException {
        if (boxCatalog == null) {
            logger.info("shipment {}: no box catalog; skipping box size validation", shipmentId);
            return;
        }

        Tugboat tugboat = getTugboat(shipmentId);
        if (INITIAL.equals(tugboat.getPackageState().getState())) {
            tugboat.initialize();
        }
        boolean isBoxChangeEligible = tugboat.getParcels().size() == 1 && tugboat.getMetadata() != null &&
            !(tugboat.getMetadata().has("isInternational") &&
                tugboat.getMetadata().get("isInternational").getAsBoolean()) &&
            !(tugboat.getMetadata().has("isLetter") &&
                tugboat.getMetadata().get("isLetter").getAsBoolean());
        if (!isBoxChangeEligible) {
            return;
        }

        Optional<BoxSpec> found = boxCatalog.find(boxId);
        if (found.isEmpty()) {
            logger.info("shipment {}: box {} is not a known swappable box; leaving the parcel as is",
                shipmentId, boxId);
            return;
        }
        BoxSpec box = found.get();

        List<Float> dims = Stream.of(box.length(), box.width(), box.height())
            .sorted()
            .toList();
        IParcel parcel = tugboat.getParcels().getFirst();
        List<Float> parcelDims = Stream.of(parcel.getLength(), parcel.getWidth(), parcel.getHeight())
            .sorted()
            .toList();
        boolean dimsMatch = IntStream.range(0, dims.size())
            .allMatch(i -> Math.abs(dims.get(i) - parcelDims.get(i)) <= 0.5);
        if (dimsMatch) {
            return;
        }

        logger.info("shipment {}: changing box size to {}", shipmentId, box.name());
        if (tugboat.getPackageState().getState().equals(PURCHASED)) {
            tugboat.voidLabel();
        }
        if (tugboat.getPackageState().getState().ordinal() <= SHOPPED.ordinal()) {
            tugboat.reset();
        }
        IParcel newParcel = new TugboatParcel(parcel.getWeight(), box.length(), box.width(), box.height());
        tugboat.setParcels(new ArrayList<>(List.of(newParcel)));
        tugboat.initialize(List.of(ShipmentComponent.PARCELS));
    }

    public String printTugboat(String shipmentId) throws TugboatException {
        Tugboat tugboat = getTugboat(shipmentId);
        List<IPostageLabel> labels = tugboat.print();
        JsonArray labelsArray = new JsonArray();
        for (IPostageLabel label : labels) {
            labelsArray.add(label.toJsonString());
        }
        return labelsArray.getAsString();
    }

    public String  reprintTugboat(String shipmentId) throws TugboatException {
        Tugboat tugboat = getTugboat(shipmentId);
        List<IPostageLabel> labels = tugboat.reprint();
        JsonArray labelsArray = new JsonArray();
        for (IPostageLabel label : labels) {
            labelsArray.add(label.toJsonString());
        }
        return labelsArray.getAsString();
    }

    public String voidTugboat(String shipmentId) throws TugboatException {
        Tugboat tugboat = getTugboat(shipmentId);
        return tugboat.voidLabel().toString();
    }

    public String getManifestGroup(String pickupFacilityCode, String manifestGroupName) throws TugboatException {
        PickupFacility manifestFacility = getPickupFacility(pickupFacilityCode);
        return manifestFacility.getPickupGroupByName(manifestGroupName).toString();
    }

    public String createTracker(String trackingCode, String carrier) throws EasyPostException {
        try {
            EasyPostClient client = new EasyPostClient(easypostApiKey);
            Map<String, Object> trackerParams = new HashMap<>();
            trackerParams.put("tracking_code", trackingCode);
            trackerParams.put("carrier", carrier);
            Tracker trackerResponse = client.tracker.create(trackerParams);
            logger.info("Created tracker with ID: {}", trackerResponse.getId());
            return trackerResponse.getId();
        } catch (EasyPostException e) {
            String errorMsg = String.format("Failed to create tracker for %s/%s: %s", carrier, trackingCode, e.getMessage());
            logger.error(errorMsg, e);
            throw e;
        }
    }

    public com.easypost.model.Shipment getReturnLabel(String returnId, String orderId, String shipmentId) throws EasyPostException {
        EasyPostClient client = new EasyPostClient(this.easypostApiKey);
        PickupFacility brooklynFacility = getPickupFacility("BK");
        if (brooklynFacility == null || brooklynFacility.getAddress() == null) {
            throw new IllegalStateException(
                "Return labels require a pickup facility, which requires a cache client (set CACHE_URL)");
        }
        String brooklynAddrId = brooklynFacility.getAddress().getId();
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("id", brooklynAddrId);
        Map<String, Object> parcelMap = new HashMap<>();
        parcelMap.put("id", "prcl_5a14987da171484aaafd7679ea45d931"); // hardcoded 4x4x4, 15 oz - doesn't seem to matter for returns
        Map<String, Object> optionsParams = new HashMap<>();
        optionsParams.put("label_format", "PNG");
        optionsParams.put("cost_center", "09");
        optionsParams.put("postage_label_inline", true);
        Map<String, Object> shipmentParams = new HashMap<>();
        shipmentParams.put("from_address", addressMap);
        shipmentParams.put("to_address", addressMap);
        shipmentParams.put("parcel", parcelMap);
        shipmentParams.put("is_return", true);
        shipmentParams.put("carrier_accounts", List.of("ca_8c8df61669d84610b743ab46480deb82")); // UMI 6FW132
        shipmentParams.put("service", "SingleReturn");
        shipmentParams.put("options", optionsParams);
        shipmentParams.put("reference", "return-" + returnId + "-" + orderId + "-" + shipmentId + "-" + UUID.randomUUID().toString().substring(0, 5));
        return client.shipment.create(shipmentParams);
    }

    private TugboatOptions getTugboatOptions() {
        TugboatOptions tugboatOptions = new TugboatOptions();
        tugboatOptions.setLabelFormat("ZPL");
        tugboatOptions.setPostageLabelInline(true);
        tugboatOptions.setSelectLowestRate(false);
        hooks.applyTo(tugboatOptions);
        return tugboatOptions;
    }

    /**
     * Retrieve a pickup facility, or {@code null} when running without a cache
     * (facilities are cache-backed, so there is nothing to retrieve) or when the
     * cache holds no facility under this code (e.g. a fresh local redis).
     */
    public PickupFacility getPickupFacility(String pickupFacilityCode) {
        if (engineConfig.getCacheClient() == null) {
            return null;
        }
        PickupFacility pickupFacility = new PickupFacility(engineConfig, pickupFacilityCode);
        pickupFacility.retrieve();
        if (pickupFacility.getPickupFacilityId() == null) {
            logger.warn("No pickup facility {} in the cache; continuing without one", pickupFacilityCode);
            return null;
        }
        return pickupFacility;
    }

    public PickupFacility getPickupFacility() {
        return getPickupFacility("BK");
    }

    private EngineConfig createEngineConfig() {
        Map<String, IShippingClient> shippingClients = new HashMap<>();
        shippingClients.put("default", createEasyPostShippingClient());
        shippingClients.putAll(createOptionalShippingClients());

        EngineConfig.Builder builder = EngineConfig.builder()
                .withCachePrefix(cacheClientPrefix)
                .withShippingClients(shippingClients);

        ICacheClient cacheClient = createCacheClient();
        if (cacheClient != null) {
            builder.withCacheClient(cacheClient);
        }
        return builder.build();
    }

    /**
     * Build the cache client from the first registered {@link CacheClientFactory}
     * (or the one named by {@code CACHE_TYPE}), resolving its config keys as
     * environment variables — "Cache URL" reads {@code CACHE_URL}. Returns
     * {@code null} to run without a cache, which leaves each request stateless
     * and disables pickups and manifests.
     */
    private ICacheClient createCacheClient() {
        String requestedType = TugboatSettings.get(CACHE_TYPE_SETTING, "").trim();

        CacheClientFactory factory = null;
        for (CacheClientFactory candidate : ServiceLoader.load(CacheClientFactory.class)) {
            if (!requestedType.isEmpty() && requestedType.equalsIgnoreCase(candidate.type())) {
                factory = candidate;
                break;
            }
            if (factory == null) {
                factory = candidate;
            }
        }

        if (factory == null) {
            logger.warn("No cache client factory registered; running without a cache");
            return null;
        }

        Map<String, String> config = new LinkedHashMap<>();
        boolean hasValue = false;
        for (String configKey : factory.configKeys()) {
            String value = TugboatSettings.get(configKey, "");
            config.put(configKey, value);
            hasValue |= !value.isBlank();
        }

        if (!hasValue) {
            logger.warn("No cache configured; running without a cache");
            return null;
        }

        try {
            return factory.create(config);
        } catch (Exception e) {
            logger.error("Failed to create '{}' cache client: {}", factory.type(), e.getMessage());
            return null;
        }
    }

    private IShippingClient createEasyPostShippingClient() {
        try {
            EasyPostClient client = new EasyPostClient(this.easypostApiKey);
            return new ShippingClientAdapter(client);
        } catch (MissingParameterError e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Build the non-EasyPost shipping clients from whichever
     * {@link ShippingClientFactory} implementations are on the classpath,
     * resolving each factory's config keys as type-namespaced settings (see
     * {@link #publishVendorSettings()}).
     *
     * <p>The vendor adapters are optional build dependencies: when one is not
     * present its factory is never discovered and its client is skipped, so XO
     * runs EasyPost-only without any code change. A discovered factory with no
     * configuration is skipped too, and one that fails to build is logged
     * rather than failing startup.
     */
    private Map<String, IShippingClient> createOptionalShippingClients() {
        Map<String, IShippingClient> clients = new LinkedHashMap<>();

        List<ShippingClientFactory> discovered = new ArrayList<>();
        ServiceLoader.load(ShippingClientFactory.class).forEach(discovered::add);

        logger.info("Discovered {} shipping client factories: {}", discovered.size(),
            discovered.stream().map(ShippingClientFactory::type).sorted().toList());

        for (ShippingClientFactory factory : discovered) {
            String type = factory.type();
            if (EASYPOST_CLIENT_TYPE.equals(type)) {
                continue;
            }

            Map<String, String> config = new LinkedHashMap<>();
            boolean hasValue = false;
            for (String configKey : factory.configKeys()) {
                String value = TugboatSettings.get(type + " " + configKey, "");
                config.put(configKey, value);
                hasValue |= !value.isBlank();
            }

            if (!hasValue) {
                logger.info("No configuration for '{}' shipping client; skipping", type);
                continue;
            }

            try {
                clients.put(type, factory.create(config));
                logger.info("Registered '{}' shipping client", type);
            } catch (Exception e) {
                logger.error("Failed to create '{}' shipping client: {}", type, e.getMessage());
            }
        }
        return clients;
    }
}
