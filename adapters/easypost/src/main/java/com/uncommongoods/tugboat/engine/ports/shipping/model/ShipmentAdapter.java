package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.easypost.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class ShipmentAdapter implements IShipment {
    private final Shipment shipment;

    public ShipmentAdapter(Shipment shipment) {
        super();
        this.shipment = shipment;
    }

    public ShipmentAdapter(Map<String, Object> shipmentMap) {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
        String json = gson.toJson(shipmentMap);
        this.shipment = gson.fromJson(json, Shipment.class);
    }

    @Override
    public String getReference() {
        return shipment.getReference();
    }

    @Override
    public Boolean getIsReturn() {
        return shipment.getIsReturn();
    }

    @Override
    public IAddress getToAddress() {
        Address address = shipment.getToAddress();
        return address != null ? new AddressAdapter(address) : null;
    }

    @Override
    public IAddress getBuyerAddress() {
        Address address = shipment.getBuyerAddress();
        return address != null ? new AddressAdapter(address) : null;
    }

    @Override
    public IAddress getFromAddress() {
        Address address = shipment.getFromAddress();
        return address != null ? new AddressAdapter(address) : null;
    }

    @Override
    public IAddress getReturnAddress() {
        Address address = shipment.getReturnAddress();
        return address != null ? new AddressAdapter(address) : null;
    }

    @Override
    public IParcel getParcel() {
        Parcel parcel = shipment.getParcel();
        return parcel != null ? new ParcelAdapter(parcel) : null;
    }

    @Override
    public ICustomsInfo getCustomsInfo() {
        CustomsInfo customsInfo = shipment.getCustomsInfo();
        return customsInfo != null ? new CustomsInfoAdapter(customsInfo) : null;
    }

    @Override
    public IRate getSelectedRate() {
        Rate rate = shipment.getSelectedRate();
        return rate != null ? new RateAdapter(rate) : null;
    }

    @Override
    public void setSelectedRate(IRate rate) {
        // no-op
    }

    @Override
    public List<IRate> getRates() {
        List<Rate> rates = shipment.getRates();
        return rates != null ? rates.stream().map(RateAdapter::new).collect(Collectors.toList()) : null;
    }

    @Override
    public IPostageLabel getPostageLabel() {
        PostageLabel postageLabel = shipment.getPostageLabel();
        return postageLabel != null ? new PostageLabelAdapter(postageLabel) : null;
    }

    @Override
    public IScanForm getScanForm() {
        ScanForm scanForm = shipment.getScanForm();
        return scanForm != null ? new ScanFormAdapter(scanForm) : null;
    }

    @Override
    public String getOrderId() {
        return shipment.getOrderId();
    }

    @Override
    public List<IForm> getForms() {
        List<Form> forms = shipment.getForms();
        if (forms == null) {
            return null;
        }
        List<IForm> adaptedForms = new ArrayList<>();
        for (Form form : forms) {
            adaptedForms.add(new FormAdapter(form));
        }
        return adaptedForms;
    }

    @Override
    public ITracker getTracker() {
        Tracker tracker = shipment.getTracker();
        return tracker != null ? new TrackerAdapter(tracker) : null;
    }

    @Override
    public String getInsurance() {
        return shipment.getInsurance();
    }

    @Override
    public String getTrackingCode() {
        return shipment.getTrackingCode();
    }

    @Override
    public String getStatus() {
        return shipment.getStatus();
    }

    @Override
    public String getRefundStatus() {
        return shipment.getRefundStatus();
    }

    @Override
    public String getBatchId() {
        return shipment.getBatchId();
    }

    @Override
    public String getBatchStatus() {
        return shipment.getBatchStatus();
    }

    @Override
    public String getBatchMessage() {
        return shipment.getBatchMessage();
    }

    @Override
    public String getUspsZone() {
        return shipment.getUspsZone();
    }

    @Override
    public Map<String, Object> getOptions() {
        return shipment.getOptions();
    }

    @Override
    public List<IShipmentMessage> getMessages() {
        List<ShipmentMessage> messages = shipment.getMessages();
        if (messages == null) {
            return null;
        }
        List<IShipmentMessage> adaptedMessages = new ArrayList<>();
        for (ShipmentMessage message : messages) {
            adaptedMessages.add(new ShipmentMessageAdapter(message));
        }
        return adaptedMessages;
    }

    @Override
    public List<ITaxIdentifier> getTaxIdentifiers() {
        List<TaxIdentifier> taxIdentifiers = shipment.getTaxIdentifiers();
        if (taxIdentifiers == null) {
            return null;
        }
        List<ITaxIdentifier> adaptedTaxIdentifiers = new ArrayList<>();
        for (TaxIdentifier taxIdentifier : taxIdentifiers) {
            adaptedTaxIdentifiers.add(new TaxIdentifierAdapter(taxIdentifier));
        }
        return adaptedTaxIdentifiers;
    }

    @Override
    public List<ICarrierAccount> getCarrierAccounts() {
        List<CarrierAccount> carrierAccounts = shipment.getCarrierAccounts();
        if (carrierAccounts == null) {
            return null;
        }
        List<ICarrierAccount> adaptedCarrierAccounts = new ArrayList<>();
        for (CarrierAccount carrierAccount : carrierAccounts) {
            adaptedCarrierAccounts.add(new CarrierAccountAdapter(carrierAccount));
        }
        return adaptedCarrierAccounts;
    }

    @Override
    public String getService() {
        return shipment.getService();
    }

    @Override
    public List<IFee> getFees() {
        List<Fee> fees = shipment.getFees();
        if (fees == null) {
            return null;
        }
        List<IFee> adaptedFees = new ArrayList<>();
        for (Fee fee : fees) {
            adaptedFees.add(new FeeAdapter(fee));
        }
        return adaptedFees;
    }

    @Override
    public IRate lowestRate() throws TugboatException {
        try {
            Rate rate = shipment.lowestRate();
            return rate != null ? new RateAdapter(rate) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IRate lowestRate(List<String> carriers, List<String> services) throws TugboatException {
        try {
            Rate rate = shipment.lowestRate(carriers, services);
            return rate != null ? new RateAdapter(rate) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IRate lowestRate(List<String> carriers) throws TugboatException {
        try {
            Rate rate = shipment.lowestRate(carriers);
            return rate != null ? new RateAdapter(rate) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public String getId() {
        return shipment.getId();
    }

    @Override
    public String getMode() {
        return shipment.getMode();
    }

    @Override
    public String getObject() {
        return shipment.getObject();
    }

    @Override
    public Date getCreatedAt() {
        return shipment.getCreatedAt();
    }

    @Override
    public Date getUpdatedAt() {
        return shipment.getUpdatedAt();
    }

    @Override
    public String toString() {
        return shipment.toString();
    }

    @Override
    public String prettyPrint() {
        return shipment.prettyPrint();
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (getId() != null) map.put("id", getId());
        if (getReference() != null) map.put("reference", getReference());
        if (getStatus() != null) map.put("status", getStatus());
        if (getTrackingCode() != null) map.put("tracking_code", getTrackingCode());
        if (getIsReturn() != null) map.put("is_return", getIsReturn());
        if (getService() != null) map.put("service", getService());
        if (getOrderId() != null) map.put("order_id", getOrderId());
        if (getInsurance() != null) map.put("insurance", getInsurance());
        if (getRefundStatus() != null) map.put("refund_status", getRefundStatus());
        if (getBatchId() != null) map.put("batch_id", getBatchId());
        if (getBatchStatus() != null) map.put("batch_status", getBatchStatus());
        if (getBatchMessage() != null) map.put("batch_message", getBatchMessage());
        if (getUspsZone() != null) map.put("usps_zone", getUspsZone());

        // Add nested objects
        IAddress fromAddress = getFromAddress();
        if (fromAddress != null) {
            map.put("from_address", fromAddress.toMap());
        }

        IAddress toAddress = getToAddress();
        if (toAddress != null) {
            map.put("to_address", toAddress.toMap());
        }

        IAddress returnAddress = getReturnAddress();
        if (returnAddress != null) {
            map.put("return_address", returnAddress.toMap());
        }

        IAddress buyerAddress = getBuyerAddress();
        if (buyerAddress != null) {
            map.put("buyer_address", buyerAddress.toMap());
        }

        IParcel parcel = getParcel();
        if (parcel != null) {
            map.put("parcel", parcel.toMap());
        }

        ICustomsInfo customsInfo = getCustomsInfo();
        if (customsInfo != null) {
            map.put("customs_info", customsInfo.toMap());
        }

        // Add lists
        List<IRate> rates = getRates();
        if (rates != null) {
            List<Map<String, Object>> rateMaps = new ArrayList<>();
            for (IRate rate : rates) {
                if (rate instanceof Mappable) {
                    rateMaps.add(rate.toMap());
                }
            }
            map.put("rates", rateMaps);
        }

        if (getOptions() != null) map.put("options", getOptions());

        return map;
    }

    public Shipment getShipment() {
        return shipment;
    }

    @Override
    public JsonElement toJson() {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
        return gson.toJsonTree(this.shipment);
    }

    @Override
    public String toJsonString() {
        return this.toJson().toString();
    }

    @Override
    public String getProviderType() {
        return "easypost";
    }
}
