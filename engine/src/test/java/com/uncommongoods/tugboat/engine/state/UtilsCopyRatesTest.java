// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine.state;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRate;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Rates handed to Tugboat.setRates() used to be the very same objects held by the shipment/order
 * rate responses, so any later edit (the rated hook adjusts price, service and delivery date) wrote
 * straight through into the carrier's original response. These pin the copy that prevents that.
 */
class UtilsCopyRatesTest {

    @Test
    void copyRatesSharesNoInstanceWithTheSource() throws TugboatException {
        List<IRate> source = List.of(new StubRate("rate_1", "UPS", "Ground", 10.50f),
                                     new StubRate("rate_2", "USPS", "Priority", 8.25f));

        List<IRate> copies = Utils.copyRates(source);

        assertEquals(source.size(), copies.size());
        for (int i = 0; i < source.size(); i++) {
            assertNotSame(source.get(i), copies.get(i));
            assertEquals(source.get(i).getId(), copies.get(i).getId());
            assertEquals(source.get(i).getCarrier(), copies.get(i).getCarrier());
            assertEquals(source.get(i).getService(), copies.get(i).getService());
            assertEquals(source.get(i).getRate(), copies.get(i).getRate());
        }
    }

    @Test
    void editingACopyLeavesTheSourceRateUntouched() throws TugboatException {
        StubRate original = new StubRate("rate_1", "UPS", "Ground", 10.50f);

        IRate copy = Utils.copyRates(List.of(original)).getFirst();
        copy.setRate(0.01f);
        copy.setService("CHANGED");
        copy.setDeliveryDate("2030-01-01T00:00:00Z");

        assertEquals(10.50f, original.getRate());
        assertEquals("Ground", original.getService());
        assertNull(original.getDeliveryDate());
    }

    @Test
    void aFailedCopyThrowsRatherThanHandingBackTheOriginal() {
        IRate uncloneable = new StubRate("rate_1", "UPS", "Ground", 10.50f) {
            @Override
            public IRate clone() throws CloneNotSupportedException {
                throw new CloneNotSupportedException("nope");
            }
        };

        TugboatException e = assertThrows(TugboatException.class, () -> Utils.copyRates(List.of(uncloneable)));
        assertTrue(e.getMessage().contains("rate_1"));
        assertInstanceOf(CloneNotSupportedException.class, e.getCause());
    }

    @Test
    void copiedRatesAreUnmodifiableLikeTheListTheyReplaced() throws TugboatException {
        List<IRate> copies = Utils.copyRates(List.of(new StubRate("rate_1", "UPS", "Ground", 10.50f)));

        assertThrows(UnsupportedOperationException.class,
            () -> copies.add(new StubRate("rate_2", "USPS", "Priority", 8.25f)));
    }

    /**
     * Mirrors ParselRate / IntlRate: setters assign fields in place, and clone() hands back a
     * genuinely separate instance.
     */
    private static class StubRate implements IRate {
        private final String id;
        private String carrier;
        private String service;
        private Float rate;
        private Number deliveryDays;
        private String deliveryDate;

        StubRate(String id, String carrier, String service, Float rate) {
            this.id = id;
            this.carrier = carrier;
            this.service = service;
            this.rate = rate;
        }

        @Override public IRate clone() throws CloneNotSupportedException {
            StubRate copy = new StubRate(id, carrier, service, rate);
            copy.deliveryDays = this.deliveryDays;
            copy.deliveryDate = this.deliveryDate;
            return copy;
        }

        @Override public String getCarrier() { return carrier; }
        @Override public void setCarrier(String carrier) { this.carrier = carrier; }
        @Override public String getService() { return service; }
        @Override public void setService(String service) { this.service = service; }
        @Override public Float getRate() { return rate; }
        @Override public void setRate(Float rate) { this.rate = rate; }
        @Override public Number getDeliveryDays() { return deliveryDays; }
        @Override public void setDeliveryDays(Number deliveryDays) { this.deliveryDays = deliveryDays; }
        @Override public String getDeliveryDate() { return deliveryDate; }
        @Override public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }

        @Override public String getCurrency() { return "USD"; }
        @Override public Float getListRate() { return null; }
        @Override public String getListCurrency() { return null; }
        @Override public Float getRetailRate() { return null; }
        @Override public String getRetailCurrency() { return null; }
        @Override public Boolean getDeliveryDateGuaranteed() { return null; }
        @Override public Number getEstDeliveryDays() { return deliveryDays; }
        @Override public String getShipmentId() { return "shp_" + id; }
        @Override public String getCarrierAccountId() { return "ca_" + carrier; }
        @Override public String getBillingType() { return null; }

        @Override public String getId() { return id; }
        @Override public String getMode() { return "test"; }
        @Override public String getObject() { return "Rate"; }
        @Override public Date getCreatedAt() { return null; }
        @Override public Date getUpdatedAt() { return null; }
        @Override public String prettyPrint() { return toString(); }
        @Override public String getProviderType() { return "test"; }

        @Override public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("carrier", carrier);
            map.put("service", service);
            map.put("rate", rate);
            return map;
        }

        @Override public JsonElement toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("id", id);
            json.addProperty("carrier", carrier);
            json.addProperty("service", service);
            json.addProperty("rate", rate);
            return json;
        }

        @Override public String toString() {
            return "StubRate{id='" + id + "', carrier='" + carrier + "', service='" + service + "', rate=" + rate + "}";
        }
    }
}
