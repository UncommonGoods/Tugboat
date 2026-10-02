package com.uncommongoods.tugboat.engine.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.ports.shipping.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Models for a fictional shipping provider, registered only from test scope.
 *
 * <p>Stands in for a third-party adapter jar: nothing in main source knows these
 * exist, so a test that round-trips one proves the SPI carries a provider the
 * engine was never compiled against. The shipment holds interface-typed children
 * so nesting can be exercised.
 */
public final class VendorModels {

    private VendorModels() {}

    public static final String PROVIDER = "vendor";

    /** Serializes its own fields, so JSON member names match field names. */
    private static final Gson GSON = new Gson();

    public static class VendorAddress implements IAddress {
        private final String id;
        private final String name;
        private final String city;

        public VendorAddress(String id, String name, String city) {
            this.id = id;
            this.name = name;
            this.city = city;
        }

        public VendorAddress(Map<String, Object> map) {
            this.id = MapValues.asString(map, "id");
            this.name = MapValues.asString(map, "name");
            this.city = MapValues.asString(map, "city");
        }

        @Override public String getId() { return id; }
        @Override public String getName() { return name; }
        @Override public String getCity() { return city; }
        @Override public String getMode() { return "test"; }
        @Override public String getObject() { return "Address"; }
        @Override public Date getCreatedAt() { return null; }
        @Override public Date getUpdatedAt() { return null; }
        @Override public String getCompany() { return null; }
        @Override public String getStreet1() { return null; }
        @Override public String getStreet2() { return null; }
        @Override public String getState() { return null; }
        @Override public String getZip() { return null; }
        @Override public String getCountry() { return null; }
        @Override public String getPhone() { return null; }
        @Override public String getEmail() { return null; }
        @Override public String getMessage() { return null; }
        @Override public String getCarrierFacility() { return null; }
        @Override public String getFederalTaxId() { return null; }
        @Override public Boolean getResidential() { return null; }
        @Override public IAddressVerifications getVerifications() { return null; }
        @Override public String prettyPrint() { return toString(); }
        @Override public String toString() { return "VendorAddress{id='" + id + "'}"; }
        @Override public JsonElement toJson() { return GSON.toJsonTree(this); }
        @Override public Map<String, Object> toMap() { return new HashMap<>(); }
        @Override public String getProviderType() { return PROVIDER; }
    }

    public static class VendorRate implements IRate {
        private final String id;
        private final String carrier;
        private Float rate;

        public VendorRate(String id, String carrier, Float rate) {
            this.id = id;
            this.carrier = carrier;
            this.rate = rate;
        }

        public VendorRate(Map<String, Object> map) {
            this.id = MapValues.asString(map, "id");
            this.carrier = MapValues.asString(map, "carrier");
            this.rate = MapValues.asFloat(map, "rate");
        }

        @Override public String getId() { return id; }
        @Override public String getCarrier() { return carrier; }
        @Override public Float getRate() { return rate; }
        @Override public void setRate(Float rate) { this.rate = rate; }
        @Override public void setCarrier(String carrier) { }
        @Override public String getService() { return "Ground"; }
        @Override public void setService(String service) { }
        @Override public String getCurrency() { return "USD"; }
        @Override public Float getListRate() { return null; }
        @Override public String getListCurrency() { return null; }
        @Override public Float getRetailRate() { return null; }
        @Override public String getRetailCurrency() { return null; }
        @Override public Number getDeliveryDays() { return null; }
        @Override public void setDeliveryDays(Number deliveryDays) { }
        @Override public String getDeliveryDate() { return null; }
        @Override public void setDeliveryDate(String deliveryDate) { }
        @Override public Boolean getDeliveryDateGuaranteed() { return null; }
        @Override public Number getEstDeliveryDays() { return null; }
        @Override public String getShipmentId() { return null; }
        @Override public String getCarrierAccountId() { return null; }
        @Override public String getBillingType() { return null; }
        @Override public String getMode() { return "test"; }
        @Override public String getObject() { return "Rate"; }
        @Override public Date getCreatedAt() { return null; }
        @Override public Date getUpdatedAt() { return null; }
        @Override public String prettyPrint() { return toString(); }
        @Override public String toString() { return "VendorRate{id='" + id + "'}"; }
        @Override public JsonElement toJson() { return GSON.toJsonTree(this); }
        @Override public Map<String, Object> toMap() { return new HashMap<>(); }
        @Override public String getProviderType() { return PROVIDER; }
        @Override public IRate clone() { return new VendorRate(id, carrier, rate); }
    }

    public static class VendorShipment implements IShipment {
        private final String id;
        private final IAddress toAddress;
        private final List<IRate> rates;
        private IRate selectedRate;

        public VendorShipment(String id, IAddress toAddress, List<IRate> rates) {
            this.id = id;
            this.toAddress = toAddress;
            this.rates = rates;
        }

        public VendorShipment(Map<String, Object> map, ModelResolver resolver) {
            this.id = MapValues.asString(map, "id");
            this.toAddress = resolver.resolve(EntityType.ADDRESS, map.get("toAddress"));
            this.rates = resolver.resolveList(EntityType.RATE, map.get("rates"));
            this.selectedRate = resolver.resolve(EntityType.RATE, map.get("selectedRate"));
        }

        @Override public String getId() { return id; }
        @Override public IAddress getToAddress() { return toAddress; }
        @Override public List<IRate> getRates() { return rates; }
        @Override public IRate getSelectedRate() { return selectedRate; }
        @Override public void setSelectedRate(IRate rate) { this.selectedRate = rate; }
        @Override public String getMode() { return "test"; }
        @Override public String getObject() { return "Shipment"; }
        @Override public Date getCreatedAt() { return null; }
        @Override public Date getUpdatedAt() { return null; }
        @Override public String getService() { return "Ground"; }
        @Override public String getReference() { return null; }
        @Override public Boolean getIsReturn() { return false; }
        @Override public IAddress getBuyerAddress() { return null; }
        @Override public IAddress getFromAddress() { return null; }
        @Override public IAddress getReturnAddress() { return null; }
        @Override public Map<String, Object> getOptions() { return new HashMap<>(); }
        @Override public List<ICarrierAccount> getCarrierAccounts() { return new ArrayList<>(); }
        @Override public IRate lowestRate() throws TugboatException { return null; }
        @Override public IRate lowestRate(List<String> carriers, List<String> services) throws TugboatException { return null; }
        @Override public IRate lowestRate(List<String> carriers) throws TugboatException { return null; }
        @Override public IParcel getParcel() { return null; }
        @Override public ICustomsInfo getCustomsInfo() { return null; }
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
        @Override public String prettyPrint() { return toString(); }
        @Override public String toString() { return "VendorShipment{id='" + id + "'}"; }
        @Override public JsonElement toJson() { return GSON.toJsonTree(this); }
        @Override public Map<String, Object> toMap() { return new HashMap<>(); }
        @Override public String getProviderType() { return PROVIDER; }
    }
}
