// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge;

import com.uncommongoods.tugboat.engine.TugboatOptions;
import com.uncommongoods.tugboat.engine.ports.cache.CacheClientFactory;
import com.uncommongoods.tugboat.engine.ports.cache.ICacheClient;
import com.uncommongoods.tugboat.engine.ports.config.TugboatSettings;
import com.uncommongoods.tugboat.engine.ports.shipping.service.IShippingClient;
import com.uncommongoods.tugboat.engine.ports.shipping.service.ShippingClientFactory;
import com.uncommongoods.tugboat.engine.config.EngineConfig;
import com.uncommongoods.tugboat.bridge.util.EncryptionUtil;
import com.uncommongoods.tugboat.engine.hooks.TugboatHookProvider;
import com.uncommongoods.tugboat.engine.hooks.impl.TugboatHooks;
import com.uncommongoods.tugboat.engine.manifest.PickupFacility;

import com.uncommongoods.tugboat.bridge.service.OriginAddressStore;
import com.uncommongoods.tugboat.bridge.service.PrinterPreferences;
import com.uncommongoods.tugboat.bridge.service.ShipmentHistoryStore;
import com.uncommongoods.tugboat.engine.model.TugboatAddress;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.prefs.Preferences;

/**
 * Manages configuration for the Tugboat Bridge application.
 * Handles building EngineConfig objects for both PROD and DEV environments
 * and tracks the currently active environment.
 *
 * <p>All data configuration comes from the Settings page's Data section, stored
 * in preferences per environment:
 * <ul>
 *   <li>{@code tugboat.data.{env}.setting.{NAME}} — freeform name/value rows
 *       (values encrypted). Published to {@link TugboatSettings} for the active
 *       environment so hooks and adapters can read them like environment
 *       variables.</li>
 *   <li>{@code tugboat.data.{env}.client.{providerKey}.type} — a shipping
 *       client's factory type.</li>
 *   <li>{@code tugboat.data.{env}.client.{providerKey}.cfg.{configKey}} — that
 *       client's config. Clients are built via the {@link ShippingClientFactory}
 *       implementations discovered with {@link ServiceLoader}.</li>
 * </ul>
 *
 * <p>The cache client is built the same way, from a {@link CacheClientFactory}
 * discovered with {@link ServiceLoader}, but there is no provider list: the
 * first registered factory wins unless a {@code CACHE_TYPE} setting names one.
 * Its {@code configKeys()} are stored as ordinary settings rows under their
 * canonical names (e.g. "Cache URL" -> {@code CACHE_URL}), so the same names
 * work as environment variables in XO. The cache is optional: when no factory
 * is registered or the values are blank, the engine runs without one.
 */
public class ConfigurationManager {

    private static final Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);
    private static final String prefsPrefix = "tugboat.";
    private static final String dataPrefsPrefix = prefsPrefix + "data.";

    /** Reserved setting names consumed by the Bridge itself. */
    public static final String CACHE_URL_SETTING = "CACHE_URL";
    public static final String CACHE_PORT_SETTING = "CACHE_PORT";

    /** Optional setting naming which {@link CacheClientFactory} to use. */
    public static final String CACHE_TYPE_SETTING = "CACHE_TYPE";

    /** The seeded, non-removable shipping client every environment starts with. */
    public static final String DEFAULT_CLIENT_KEY = "default";
    public static final String DEFAULT_CLIENT_TYPE = "easypost";

    private EngineConfig prodEngineConfig;
    private EngineConfig devEngineConfig;
    private TugboatOptions prodTugboatOptions;
    private TugboatOptions devTugboatOptions;
    private TugboatHookProvider prodHookProvider;
    private TugboatHookProvider devHookProvider;
    private ICacheClient prodCacheClient;
    private ICacheClient devCacheClient;
    private boolean isDevEnvironment = false;

    private final ReadOnlyBooleanWrapper devEnvironment = new ReadOnlyBooleanWrapper(false);

    private final ReadOnlyBooleanWrapper cacheConfigured = new ReadOnlyBooleanWrapper(false);
    private PickupFacility pickupFacility;

    private ConfigurationManager() {}

    private static final class InstanceHolder {
        private static final ConfigurationManager instance = new ConfigurationManager();
    }

    public static ConfigurationManager getInstance() {
        return InstanceHolder.instance;
    }

    public void initializeConfigurations() {
        if (!isConfigured()) {
            migrateLegacyPrefsIfNeeded("prod");
            migrateLegacyPrefsIfNeeded("dev");
            publishActiveEnvironmentSettings();
            this.prodEngineConfig = buildEngineConfigForEnvironment("prod");
            this.devEngineConfig = buildEngineConfigForEnvironment("dev");
            this.prodTugboatOptions = buildTugboatOptionsForEnvironment("prod");
            this.devTugboatOptions = buildTugboatOptionsForEnvironment("dev");
            initializePickupFacility();
            System.out.println("Configurations initialized for both environments");
        } else {
            System.out.println("Configurations already initialized, skipping");
        }
    }

    public EngineConfig getActiveConfig() {
        if (prodEngineConfig == null || devEngineConfig == null) {
            initializeConfigurations();
        }
        return isDevEnvironment ? devEngineConfig : prodEngineConfig;
    }

    public TugboatOptions getActiveTugboatOptions() {
        if (prodTugboatOptions == null || devTugboatOptions == null) {
            initializeConfigurations();
        }
        TugboatOptions options = (isDevEnvironment ? devTugboatOptions : prodTugboatOptions).clone();
        options.setLabelFormat(PrinterPreferences.resolveLabelFormat().name());
        return options;
    }

    public EngineConfig getProdEngineConfig() {
        if (prodEngineConfig == null) {
            initializeConfigurations();
        }
        return prodEngineConfig;
    }

    public EngineConfig getDevEngineConfig() {
        if (devEngineConfig == null) {
            initializeConfigurations();
        }
        return devEngineConfig;
    }

    public TugboatOptions getProdTugboatOptions() {
        if (prodTugboatOptions == null) {
            initializeConfigurations();
        }
        return prodTugboatOptions;
    }

    public TugboatOptions getDevTugboatOptions() {
        if (devTugboatOptions == null) {
            initializeConfigurations();
        }
        return devTugboatOptions;
    }

    public boolean isDevEnvironment() {
        return isDevEnvironment;
    }

    public ReadOnlyBooleanProperty devEnvironmentProperty() {
        return devEnvironment.getReadOnlyProperty();
    }

    public void setEnvironment(boolean isDev) {
        this.isDevEnvironment = isDev;
        publishActiveEnvironmentSettings();
        initializePickupFacility();
        ShipmentHistoryStore.getInstance().clear();
        System.out.println("Environment switched to: " + (isDev ? "DEV" : "PROD"));
        devEnvironment.set(isDev);
    }

    public void refreshConfigurations() {
        closeCacheClient(prodCacheClient, "PROD");
        closeCacheClient(devCacheClient, "DEV");
        closeHookProvider(prodHookProvider, "PROD");
        closeHookProvider(devHookProvider, "DEV");
        prodCacheClient = null;
        devCacheClient = null;
        prodHookProvider = null;
        devHookProvider = null;

        migrateLegacyPrefsIfNeeded("prod");
        migrateLegacyPrefsIfNeeded("dev");
        publishActiveEnvironmentSettings();
        this.prodEngineConfig = buildEngineConfigForEnvironment("prod");
        this.devEngineConfig = buildEngineConfigForEnvironment("dev");
        this.prodTugboatOptions = buildTugboatOptionsForEnvironment("prod");
        this.devTugboatOptions = buildTugboatOptionsForEnvironment("dev");
        initializePickupFacility();
    }

    public void refreshConfiguration(String environment) {
        if ("prod".equalsIgnoreCase(environment)) {
            closeCacheClient(prodCacheClient, "PROD");
            closeHookProvider(prodHookProvider, "PROD");
            prodCacheClient = null;
            prodHookProvider = null;
            publishActiveEnvironmentSettings();
            this.prodEngineConfig = buildEngineConfigForEnvironment("prod");
            this.prodTugboatOptions = buildTugboatOptionsForEnvironment("prod");
            initializePickupFacility();
            System.out.println("Refreshed PROD configuration");
        } else if ("dev".equalsIgnoreCase(environment)) {
            closeCacheClient(devCacheClient, "DEV");
            closeHookProvider(devHookProvider, "DEV");
            devCacheClient = null;
            devHookProvider = null;
            publishActiveEnvironmentSettings();
            this.devEngineConfig = buildEngineConfigForEnvironment("dev");
            this.devTugboatOptions = buildTugboatOptionsForEnvironment("dev");
            initializePickupFacility();
            System.out.println("Refreshed DEV configuration");
        }
    }

    public String getCurrentEnvironmentName() {
        return isDevEnvironment ? "DEV" : "PROD";
    }

    public PickupFacility getPickupFacility() {
        if (this.pickupFacility == null) {
            return null;
        }
        this.pickupFacility.retrieve();
        return this.pickupFacility;
    }

    public void setPickupFacility(PickupFacility pickupFacility) {
        this.pickupFacility = pickupFacility;
    }


    public TugboatAddress getOriginAddress() {
        return OriginAddressStore.load();
    }

    public void setOriginAddress(TugboatAddress originAddress) {
        OriginAddressStore.save(originAddress);
    }

    public boolean isCacheConfigured() {
        EngineConfig activeConfig = getActiveConfig();
        return activeConfig != null && activeConfig.getCacheClient() != null;
    }

    public ReadOnlyBooleanProperty cacheConfiguredProperty() {
        return cacheConfigured.getReadOnlyProperty();
    }

    private void initializePickupFacility() {
        boolean hasCache = false;
        try {
            EngineConfig activeConfig = getActiveConfig();
            hasCache = activeConfig != null && activeConfig.getCacheClient() != null;
            if (activeConfig != null && activeConfig.getCacheClient() == null) {
                System.out.println("No cache client; pickup facility unavailable");
                this.pickupFacility = null;
            } else if (activeConfig != null) {
                this.pickupFacility = new PickupFacility(activeConfig, "BK");
                this.pickupFacility.retrieve();
                System.out.println("PickupFacility initialized successfully");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize PickupFacility: " + e.getMessage());
            this.pickupFacility = null;
        }
        cacheConfigured.set(hasCache);
    }

    public boolean isConfigured() {
        return prodEngineConfig != null && devEngineConfig != null &&
               prodTugboatOptions != null && devTugboatOptions != null;
    }

    public void shutdown() {
        System.out.println("Shutting down ConfigurationManager...");
        closeCacheClient(prodCacheClient, "PROD");
        closeCacheClient(devCacheClient, "DEV");
        closeHookProvider(prodHookProvider, "PROD");
        closeHookProvider(devHookProvider, "DEV");
        prodCacheClient = null;
        devCacheClient = null;
        prodHookProvider = null;
        devHookProvider = null;
        System.out.println("ConfigurationManager shutdown complete");
    }

    public Map<String, String> readSettingsForEnvironment(String environment) {
        String settingPrefix = dataPrefsPrefix + environment.toLowerCase() + ".setting.";
        Map<String, String> settings = new TreeMap<>();

        String[] keys;
        try {
            keys = prefs.keys();
        } catch (Exception e) {
            System.err.println("Failed to read settings for " + environment + ": " + e.getMessage());
            return settings;
        }

        // Guard each row separately. EncryptionUtil.decrypt re-throws on failure,
        // so a single unreadable row used to abort the whole scan and return a
        // partial map -- which looks exactly like "the cache was never configured"
        // and is just as hard to diagnose. Skip the bad row, keep the rest.
        for (String key : keys) {
            if (!key.startsWith(settingPrefix)) {
                continue;
            }
            String name = key.substring(settingPrefix.length());
            if (name.isEmpty()) {
                continue;
            }
            try {
                String value = EncryptionUtil.decrypt(prefs.get(key, ""));
                settings.put(name, value != null ? value : "");
            } catch (Exception e) {
                System.err.println("Skipping unreadable setting '" + name + "' for " +
                    environment + ": " + e.getMessage());
            }
        }
        return settings;
    }

    /**
     * One-time migration from the legacy fixed-field preference keys to the
     * client/setting layout. Runs only when no new-style keys exist yet, so it is
     * idempotent and safe to call on every startup.
     *
     * <p>This must run before the engine configs are built.
     */
    // TODO remove this or keep it to repuprose in case the shape of prefs change in the future?
    static void migrateLegacyPrefsIfNeeded(String env) {
        String envPrefix = dataPrefsPrefix + env + ".";
        try {
            for (String key : prefs.keys()) {
                if (key.startsWith(envPrefix + "setting.") || key.startsWith(envPrefix + "client.")) {
                    return; // already on the new layout
                }
            }

            String legacyShippingKey = EncryptionUtil.decrypt(prefs.get(envPrefix + "shippingKey", ""));
            String legacyCacheUrl = prefs.get(envPrefix + "cacheUrl", "");
            String legacyCachePort = prefs.get(envPrefix + "cachePort", "");
            if ((legacyShippingKey == null || legacyShippingKey.isEmpty())
                && legacyCacheUrl.isEmpty() && legacyCachePort.isEmpty()) {
                return; // nothing to migrate
            }

            System.out.println("Migrating legacy data config for " + env + " environment");

            if (legacyShippingKey != null && !legacyShippingKey.isEmpty()) {
                prefs.put(envPrefix + "client." + DEFAULT_CLIENT_KEY + ".type", DEFAULT_CLIENT_TYPE);
                prefs.put(envPrefix + "client." + DEFAULT_CLIENT_KEY + ".cfg.API Key",
                    EncryptionUtil.encrypt(legacyShippingKey));
            }
            putMigratedSetting(envPrefix, CACHE_URL_SETTING, legacyCacheUrl);
            putMigratedSetting(envPrefix, CACHE_PORT_SETTING, legacyCachePort);

            // Values previously consumed by the hooks: carry them over as
            // freeform rows so they remain visible to hooks via TugboatSettings.
            putMigratedSetting(envPrefix, "SERVICES_URL", prefs.get(envPrefix + "servicesUrl", ""));
            putMigratedSetting(envPrefix, "DB_URL", prefs.get(envPrefix + "dbUrl", ""));
            putMigratedSetting(envPrefix, "DB_USER", prefs.get(envPrefix + "dbUser", ""));
            putMigratedSetting(envPrefix, "DB_PASSWORD",
                EncryptionUtil.decrypt(prefs.get(envPrefix + "dbPassword", "")));
        } catch (Exception e) {
            System.err.println("Failed to migrate legacy data config for " + env + ": " + e.getMessage());
        }
    }

    private static void putMigratedSetting(String envPrefix, String name, String value) {
        if (value != null && !value.isEmpty()) {
            prefs.put(envPrefix + "setting." + name, EncryptionUtil.encrypt(value));
        }
    }

    /**
     * Read the configured shipping clients for an environment: provider key ->
     * (factory type, decrypted config values keyed by the factory's config keys).
     */
    public Map<String, ClientEntry> readClientsForEnvironment(String environment) {
        String clientPrefix = dataPrefsPrefix + environment.toLowerCase() + ".client.";
        Map<String, ClientEntry> clients = new LinkedHashMap<>();
        try {
            for (String key : prefs.keys()) {
                if (!key.startsWith(clientPrefix)) {
                    continue;
                }
                String remainder = key.substring(clientPrefix.length());
                int dot = remainder.indexOf('.');
                if (dot <= 0) {
                    continue;
                }
                String providerKey = remainder.substring(0, dot);
                String field = remainder.substring(dot + 1);
                ClientEntry entry = clients.computeIfAbsent(providerKey, k -> new ClientEntry());
                if (field.equals("type")) {
                    entry.type = prefs.get(key, "");
                } else if (field.startsWith("cfg.")) {
                    String configKey = field.substring("cfg.".length());
                    entry.config.put(configKey, EncryptionUtil.decrypt(prefs.get(key, "")));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to read clients for " + environment + ": " + e.getMessage());
        }
        return clients;
    }

    public static final class ClientEntry {
        public String type = "";
        public final Map<String, String> config = new LinkedHashMap<>();
    }

    /**
     * Discover available shipping client factories via ServiceLoader, keyed by
     * {@link ShippingClientFactory#type()}.
     */
    public static Map<String, ShippingClientFactory> loadClientFactories() {
        Map<String, ShippingClientFactory> factories = new TreeMap<>();
        for (ShippingClientFactory factory : ServiceLoader.load(ShippingClientFactory.class)) {
            factories.put(factory.type(), factory);
        }

        if (factories.isEmpty()) {
            System.err.println("No shipping client factories registered; no shipping client can be configured");
        } else {
            System.out.println("Discovered " + factories.size() +
                " shipping client factories: " + factories.keySet());
        }
        return factories;
    }

    /**
     * Discover the cache client factory to use, or {@code null} when none is
     * registered (the cache is optional). Unlike shipping clients there is no
     * user-facing choice: the first registered factory wins, unless a
     * {@code CACHE_TYPE} setting names one, in which case that one is preferred.
     */
    public static CacheClientFactory loadCacheClientFactory(Map<String, String> settings) {
        String requestedType = settings.getOrDefault(CACHE_TYPE_SETTING, "").trim();

        CacheClientFactory first = null;
        for (CacheClientFactory factory : ServiceLoader.load(CacheClientFactory.class)) {
            if (!requestedType.isEmpty() && requestedType.equalsIgnoreCase(factory.type())) {
                return factory;
            }
            if (first == null) {
                first = factory;
            }
        }

        if (first == null) {
            System.out.println("No cache client factory registered; running without a cache");
        } else if (!requestedType.isEmpty()) {
            System.err.println("No cache client factory of type '" + requestedType +
                "'; falling back to '" + first.type() + "'");
        }
        return first;
    }

    private void publishActiveEnvironmentSettings() {
        String environment = isDevEnvironment ? "dev" : "prod";
        TugboatSettings.publish(readSettingsForEnvironment(environment));
    }

    private EngineConfig buildEngineConfigForEnvironment(String environment) {
        try {
            Map<String, String> settings = readSettingsForEnvironment(environment);

            EngineConfig.Builder builder = EngineConfig.builder();

            ICacheClient cacheClient = buildCacheClient(environment, settings);
            if (cacheClient != null) {
                if ("prod".equalsIgnoreCase(environment)) {
                    closeCacheClient(prodCacheClient, "PROD");
                    prodCacheClient = cacheClient;
                } else {
                    closeCacheClient(devCacheClient, "DEV");
                    devCacheClient = cacheClient;
                }
                builder.withCacheClient(cacheClient);
            }

            Map<String, IShippingClient> shippingClients = buildShippingClients(environment);
            if (!shippingClients.isEmpty()) {
                builder.withShippingClients(shippingClients);
            }

            builder.withCachePrefix("TUGBOAT" + environment.toUpperCase());

            return builder.build();

        } catch (Exception e) {
            System.err.println("Failed to build EngineConfig for " + environment + " environment: " + e.getMessage());
            return EngineConfig.builder()
                .withCachePrefix("TUGBOAT" + environment.toUpperCase())
                .build();
        }
    }

    /**
     * Build the shipping clients configured for an environment via the
     * ServiceLoader-discovered factories.
     */
    private Map<String, IShippingClient> buildShippingClients(String environment) {
        Map<String, IShippingClient> shippingClients = new HashMap<>();
        Map<String, ShippingClientFactory> factories = loadClientFactories();

        for (Map.Entry<String, ClientEntry> entry : readClientsForEnvironment(environment).entrySet()) {
            String providerKey = entry.getKey();
            ClientEntry client = entry.getValue();
            ShippingClientFactory factory = factories.get(client.type);
            if (factory == null) {
                System.err.println("No shipping client factory of type '" + client.type +
                    "' for client '" + providerKey + "' (" + environment + ")");
                continue;
            }

            boolean hasValue = client.config.values().stream().anyMatch(v -> v != null && !v.isBlank());
            if (!hasValue) {
                continue;
            }
            try {
                shippingClients.put(providerKey, factory.create(client.config));
            } catch (Exception e) {
                System.err.println("Failed to create '" + client.type + "' client '" + providerKey +
                    "' for " + environment + ": " + e.getMessage());
            }
        }
        return shippingClients;
    }

    /**
     * Build the cache client for an environment via the ServiceLoader-discovered
     * factory, or {@code null} to run without a cache.
     *
     * <p>Config values come from this environment's settings map rather than
     * {@link TugboatSettings}: configs are built for both PROD and DEV, but only
     * the active environment is published, so the published map would hand the
     * inactive environment the wrong cache.
     */
    private ICacheClient buildCacheClient(String environment, Map<String, String> settings) {
        CacheClientFactory factory = loadCacheClientFactory(settings);
        if (factory == null) {
            return null;
        }

        Map<String, String> config = new LinkedHashMap<>();
        boolean hasValue = false;
        for (String configKey : factory.configKeys()) {
            String value = settings.getOrDefault(TugboatSettings.normalize(configKey), "");
            config.put(configKey, value);
            hasValue |= !value.isBlank();
        }

        if (!hasValue) {
            System.out.println("No cache configured for " + environment + "; running without a cache");
            return null;
        }

        try {
            return factory.create(config);
        } catch (Exception e) {
            System.err.println("Failed to create '" + factory.type() + "' cache client for " +
                environment + ": " + e.getMessage());
            return null;
        }
    }

    private TugboatOptions buildTugboatOptionsForEnvironment(String environment) {
        TugboatOptions tugboatOptions = new TugboatOptions();
        tugboatOptions.setPostageLabelInline(true);
        tugboatOptions.setSelectLowestRate(false);

        try {
            // The hook provider reads its config (DB_URL, DB_USER, ...) from
            // TugboatSettings in its constructor, but only the *active*
            // environment's rows are published. Publish this environment's
            // rows for the construction, then restore the active map — the
            // same per-environment trap buildCacheClient sidesteps by reading
            // the settings map directly.
            TugboatSettings.publish(readSettingsForEnvironment(environment));
            TugboatHookProvider hookProvider;
            try {
                hookProvider = new TugboatHooks();
            } finally {
                publishActiveEnvironmentSettings();
            }
            hookProvider.applyTo(tugboatOptions);
            if ("prod".equalsIgnoreCase(environment)) {
                prodHookProvider = hookProvider;
            } else {
                devHookProvider = hookProvider;
            }
        } catch (Exception e) {
            System.err.println("Failed to wire hooks for " + environment + ": " + e.getMessage());
        }

        return tugboatOptions;
    }

    private void closeHookProvider(TugboatHookProvider hookProvider, String environmentName) {
        if (hookProvider == null) {
            return;
        }
        try {
            hookProvider.close();
        } catch (Exception e) {
            System.err.println("Failed to close hook provider for " + environmentName + ": " + e.getMessage());
            // continue even if it fails
        }
    }

    /**
     * Release a cache client's connection resources
     */
    private void closeCacheClient(ICacheClient cacheClient, String environmentName) {
        if (!(cacheClient instanceof AutoCloseable closeable)) {
            return;
        }

        try {
            closeable.close();
            System.out.println("Closed cache client for " + environmentName + " environment");
        } catch (Exception e) {
            System.err.println("Failed to close cache client for " + environmentName + " environment: " + e.getMessage());
            // continue even if it fails
        }
    }
}
