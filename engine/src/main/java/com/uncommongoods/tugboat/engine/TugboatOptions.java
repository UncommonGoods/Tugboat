// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.engine;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ShipmentOptions;
import com.uncommongoods.tugboat.engine.serialization.TugboatGson;
import com.uncommongoods.tugboat.engine.hooks.TugboatHook;
import com.uncommongoods.tugboat.engine.hooks.TugboatErrorHook;
import com.uncommongoods.tugboat.engine.manifest.CarrierService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TugboatOptions extends ShipmentOptions {
    @Expose
    private String pickupFacilityCode;
    @Expose
    private List<String> carrierAccountIds;
    @Expose
    private CarrierService selectedCarrierService;
    @Expose
    private boolean selectLowestRate = false;

    private TugboatHook initialHook;
    private TugboatHook ratedHook;
    private TugboatHook shoppedHook;
    private TugboatHook purchasedHook;
    private TugboatHook printedHook;
    private TugboatHook voidedHook;
    private TugboatErrorHook errorHook;

    // Getters and setters for PackageOptions specific fields
    public String getPickupFacilityCode() {
        return pickupFacilityCode;
    }

    public void setPickupFacilityCode(String pickupFacilityCode) {
        this.pickupFacilityCode = pickupFacilityCode;
    }

    public List<String> getCarrierAccountIds() {
        return carrierAccountIds;
    }

    public void setCarrierAccountIds(List<String> carrierAccountIds) {
        this.carrierAccountIds = carrierAccountIds;
    }

    public CarrierService getSelectedCarrierService() {
        return selectedCarrierService;
    }

    public void setSelectedCarrierService(CarrierService selectedCarrierService) {
        this.selectedCarrierService = selectedCarrierService;
    }

    public boolean isSelectLowestRate() {
        return selectLowestRate;
    }

    public void setSelectLowestRate(boolean selectLowestRate) {
        this.selectLowestRate = selectLowestRate;
    }

    // Hook getters and setters
    public TugboatHook getInitialHook() {
        return initialHook;
    }

    public void setInitialHook(TugboatHook initialHook) {
        this.initialHook = initialHook;
    }

    public TugboatHook getRatedHook() {
        return ratedHook;
    }

    public void setRatedHook(TugboatHook ratedHook) {
        this.ratedHook = ratedHook;
    }

    public TugboatHook getShoppedHook() {
        return shoppedHook;
    }

    public void setShoppedHook(TugboatHook shoppedHook) {
        this.shoppedHook = shoppedHook;
    }

    public TugboatHook getPurchasedHook() {
        return purchasedHook;
    }

    public void setPurchasedHook(TugboatHook purchasedHook) {
        this.purchasedHook = purchasedHook;
    }

    public TugboatHook getPrintedHook() {
        return printedHook;
    }

    public void setPrintedHook(TugboatHook printedHook) {
        this.printedHook = printedHook;
    }

    public TugboatHook getVoidedHook() {
        return voidedHook;
    }

    public void setVoidedHook(TugboatHook voidedHook) {
        this.voidedHook = voidedHook;
    }

    public TugboatErrorHook getErrorHook() {
        return errorHook;
    }

    public void setErrorHook(TugboatErrorHook errorHook) {
        this.errorHook = errorHook;
    }

    @Override
    public JsonElement toJson() {
        return TugboatGson.cargo().toJsonTree(this);
    }

    @Override
    public String toString() {
        return TugboatGson.cargo().toJson(this);
    }

    /**
     * Merges another TugboatOptions into this one, with values in this instance taking precedence.
     * Only null fields in this instance will be populated from the other instance.
     *
     * @param other the other TugboatOptions to merge from
     */
    public TugboatOptions mergeFrom(TugboatOptions other) {
        if (other == null) {
            return this;
        }

        // Merge TugboatOptions specific fields
        if (this.pickupFacilityCode == null && other.pickupFacilityCode != null) {
            this.pickupFacilityCode = other.pickupFacilityCode;
        }
        if (this.carrierAccountIds == null && other.carrierAccountIds != null) {
            this.carrierAccountIds = other.carrierAccountIds;
        }
        if (this.selectedCarrierService == null && other.selectedCarrierService != null) {
            this.selectedCarrierService = other.selectedCarrierService;
        }

        // Merge hook fields
        if (this.initialHook == null && other.initialHook != null) {
            this.initialHook = other.initialHook;
        }
        if (this.ratedHook == null && other.ratedHook != null) {
            this.ratedHook = other.ratedHook;
        }
        if (this.shoppedHook == null && other.shoppedHook != null) {
            this.shoppedHook = other.shoppedHook;
        }
        if (this.purchasedHook == null && other.purchasedHook != null) {
            this.purchasedHook = other.purchasedHook;
        }
        if (this.printedHook == null && other.printedHook != null) {
            this.printedHook = other.printedHook;
        }
        if (this.voidedHook == null && other.voidedHook != null) {
            this.voidedHook = other.voidedHook;
        }
        if (this.errorHook == null && other.errorHook != null) {
            this.errorHook = other.errorHook;
        }

        // Merge inherited ShipmentOptions fields
        if (getAdditionalHandling() == null && other.getAdditionalHandling() != null) {
            setAdditionalHandling(other.getAdditionalHandling());
        }
        if (getAddressValidationLevel() == null && other.getAddressValidationLevel() != null) {
            setAddressValidationLevel(other.getAddressValidationLevel());
        }
        if (getAlcohol() == null && other.getAlcohol() != null) {
            setAlcohol(other.getAlcohol());
        }
        if (getByDrone() == null && other.getByDrone() != null) {
            setByDrone(other.getByDrone());
        }
        if (getCarbonNeutral() == null && other.getCarbonNeutral() != null) {
            setCarbonNeutral(other.getCarbonNeutral());
        }
        if (getCarrierNotificationEmail() == null && other.getCarrierNotificationEmail() != null) {
            setCarrierNotificationEmail(other.getCarrierNotificationEmail());
        }
        if (getCarrierNotificationSms() == null && other.getCarrierNotificationSms() != null) {
            setCarrierNotificationSms(other.getCarrierNotificationSms());
        }
        if (getCodAmount() == null && other.getCodAmount() != null) {
            setCodAmount(other.getCodAmount());
        }
        if (getCodMethod() == null && other.getCodMethod() != null) {
            setCodMethod(other.getCodMethod());
        }
        if (getCodAddressId() == null && other.getCodAddressId() != null) {
            setCodAddressId(other.getCodAddressId());
        }
        if (getContentDescription() == null && other.getContentDescription() != null) {
            setContentDescription(other.getContentDescription());
        }
        if (getCurrency() == null && other.getCurrency() != null) {
            setCurrency(other.getCurrency());
        }
        if (getDeliveryConfirmation() == null && other.getDeliveryConfirmation() != null) {
            setDeliveryConfirmation(other.getDeliveryConfirmation());
        }
        if (getDeliveryMinDatetime() == null && other.getDeliveryMinDatetime() != null) {
            setDeliveryMinDatetime(other.getDeliveryMinDatetime());
        }
        if (getDeliveryMaxDatetime() == null && other.getDeliveryMaxDatetime() != null) {
            setDeliveryMaxDatetime(other.getDeliveryMaxDatetime());
        }
        if (getDropoffType() == null && other.getDropoffType() != null) {
            setDropoffType(other.getDropoffType());
        }
        if (getDateAdvance() == null && other.getDateAdvance() != null) {
            setDateAdvance(other.getDateAdvance());
        }
        if (getDryIce() == null && other.getDryIce() != null) {
            setDryIce(other.getDryIce());
        }
        if (getDryIceMedical() == null && other.getDryIceMedical() != null) {
            setDryIceMedical(other.getDryIceMedical());
        }
        if (getDryIceWeight() == null && other.getDryIceWeight() != null) {
            setDryIceWeight(other.getDryIceWeight());
        }
        if (getEndorsement() == null && other.getEndorsement() != null) {
            setEndorsement(other.getEndorsement());
        }
        if (getEndShipperId() == null && other.getEndShipperId() != null) {
            setEndShipperId(other.getEndShipperId());
        }
        if (getFreightCharge() == null && other.getFreightCharge() != null) {
            setFreightCharge(other.getFreightCharge());
        }
        if (getHandlingInstructions() == null && other.getHandlingInstructions() != null) {
            setHandlingInstructions(other.getHandlingInstructions());
        }
        if (getHazmat() == null && other.getHazmat() != null) {
            setHazmat(other.getHazmat());
        }
        if (getHoldForPickup() == null && other.getHoldForPickup() != null) {
            setHoldForPickup(other.getHoldForPickup());
        }
        if (getIncoterm() == null && other.getIncoterm() != null) {
            setIncoterm(other.getIncoterm());
        }
        if (getInvoiceNumber() == null && other.getInvoiceNumber() != null) {
            setInvoiceNumber(other.getInvoiceNumber());
        }
        if (getLabelDate() == null && other.getLabelDate() != null) {
            setLabelDate(other.getLabelDate());
        }
        if (getLabelFormat() == null && other.getLabelFormat() != null) {
            setLabelFormat(other.getLabelFormat());
        }
        if (getPostageLabelInline() == null && other.getPostageLabelInline() != null) {
            setPostageLabelInline(other.getPostageLabelInline());
        }
        if (getLiveAnimal() == null && other.getLiveAnimal() != null) {
            setLiveAnimal(other.getLiveAnimal());
        }
        if (getMachinable() == null && other.getMachinable() != null) {
            setMachinable(other.getMachinable());
        }
        if (getMerchantId() == null && other.getMerchantId() != null) {
            setMerchantId(other.getMerchantId());
        }
        if (getPerishable() == null && other.getPerishable() != null) {
            setPerishable(other.getPerishable());
        }
        if (getPickupMinDatetime() == null && other.getPickupMinDatetime() != null) {
            setPickupMinDatetime(other.getPickupMinDatetime());
        }
        if (getPickupMaxDatetime() == null && other.getPickupMaxDatetime() != null) {
            setPickupMaxDatetime(other.getPickupMaxDatetime());
        }
        if (getPrintCustom1() == null && other.getPrintCustom1() != null) {
            setPrintCustom1(other.getPrintCustom1());
        }
        if (getPrintCustom2() == null && other.getPrintCustom2() != null) {
            setPrintCustom2(other.getPrintCustom2());
        }
        if (getPrintCustom3() == null && other.getPrintCustom3() != null) {
            setPrintCustom3(other.getPrintCustom3());
        }
        if (getPrintCustom1Barcode() == null && other.getPrintCustom1Barcode() != null) {
            setPrintCustom1Barcode(other.getPrintCustom1Barcode());
        }
        if (getPrintCustom2Barcode() == null && other.getPrintCustom2Barcode() != null) {
            setPrintCustom2Barcode(other.getPrintCustom2Barcode());
        }
        if (getPrintCustom3Barcode() == null && other.getPrintCustom3Barcode() != null) {
            setPrintCustom3Barcode(other.getPrintCustom3Barcode());
        }
        if (getPrintCustom1Code() == null && other.getPrintCustom1Code() != null) {
            setPrintCustom1Code(other.getPrintCustom1Code());
        }
        if (getPrintCustom2Code() == null && other.getPrintCustom2Code() != null) {
            setPrintCustom2Code(other.getPrintCustom2Code());
        }
        if (getPrintCustom3Code() == null && other.getPrintCustom3Code() != null) {
            setPrintCustom3Code(other.getPrintCustom3Code());
        }
        if (getSaturdayDelivery() == null && other.getSaturdayDelivery() != null) {
            setSaturdayDelivery(other.getSaturdayDelivery());
        }
        if (getSpecialRatesEligibility() == null && other.getSpecialRatesEligibility() != null) {
            setSpecialRatesEligibility(other.getSpecialRatesEligibility());
        }
        if (getSmartpostHub() == null && other.getSmartpostHub() != null) {
            setSmartpostHub(other.getSmartpostHub());
        }
        if (getSmartpostManifest() == null && other.getSmartpostManifest() != null) {
            setSmartpostManifest(other.getSmartpostManifest());
        }
        if (getBillingRef() == null && other.getBillingRef() != null) {
            setBillingRef(other.getBillingRef());
        }
        if (getCertifiedMail() == null && other.getCertifiedMail() != null) {
            setCertifiedMail(other.getCertifiedMail());
        }
        if (getRegisteredMail() == null && other.getRegisteredMail() != null) {
            setRegisteredMail(other.getRegisteredMail());
        }
        if (getRegisteredMailAmount() == null && other.getRegisteredMailAmount() != null) {
            setRegisteredMailAmount(other.getRegisteredMailAmount());
        }
        if (getReturnReceipt() == null && other.getReturnReceipt() != null) {
            setReturnReceipt(other.getReturnReceipt());
        }
        if (getDutyPayment() == null && other.getDutyPayment() != null) {
            setDutyPayment(other.getDutyPayment());
        }
        if (getPayment() == null && other.getPayment() != null) {
            setPayment(other.getPayment());
        }
        return this;
    }

    @Override
    public TugboatOptions clone() {
        TugboatOptions cloned = (TugboatOptions) super.clone();
        if (this.carrierAccountIds != null) {
            cloned.carrierAccountIds = new ArrayList<>(this.carrierAccountIds);
        }

        if (this.selectedCarrierService != null) {
            cloned.selectedCarrierService =
                new CarrierService(this.selectedCarrierService.carrier(), this.selectedCarrierService.service());
        }

        cloned.initialHook = this.initialHook;
        cloned.ratedHook = this.ratedHook;
        cloned.shoppedHook = this.shoppedHook;
        cloned.purchasedHook = this.purchasedHook;
        cloned.printedHook = this.printedHook;
        cloned.voidedHook = this.voidedHook;
        cloned.errorHook = this.errorHook;

        return cloned;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> options = new java.util.HashMap<>();

        // Basic options
        if (getAdditionalHandling() != null) options.put("additional_handling", getAdditionalHandling());
        if (getAddressValidationLevel() != null) options.put("address_validation_level", getAddressValidationLevel());
        if (getAlcohol() != null) options.put("alcohol", getAlcohol());
        if (getByDrone() != null) options.put("by_drone", getByDrone());
        if (getCarbonNeutral() != null) options.put("carbon_neutral", getCarbonNeutral());
        if (getCarrierNotificationEmail() != null) options.put("carrier_notification_email", getCarrierNotificationEmail());
        if (getCarrierNotificationSms() != null) options.put("carrier_notification_sms", getCarrierNotificationSms());

        // COD options
        if (getCodAmount() != null) options.put("cod_amount", getCodAmount());
        if (getCodMethod() != null) options.put("cod_method", getCodMethod());
        if (getCodAddressId() != null) options.put("cod_address_id", getCodAddressId());

        // Content and currency
        if (getContentDescription() != null) options.put("content_description", getContentDescription());
        if (getCurrency() != null) options.put("currency", getCurrency());

        // Delivery options
        if (getDeliveryConfirmation() != null) options.put("delivery_confirmation", getDeliveryConfirmation());
        if (getDeliveryMinDatetime() != null) options.put("delivery_min_datetime", getDeliveryMinDatetime().toString());
        if (getDeliveryMaxDatetime() != null) options.put("delivery_max_datetime", getDeliveryMaxDatetime().toString());
        if (getDropoffType() != null) options.put("dropoff_type", getDropoffType());
        if (getDateAdvance() != null) options.put("date_advance", getDateAdvance());

        // Dry ice options
        if (getDryIce() != null) options.put("dry_ice", getDryIce());
        if (getDryIceMedical() != null) options.put("dry_ice_medical", getDryIceMedical());
        if (getDryIceWeight() != null) options.put("dry_ice_weight", getDryIceWeight());

        // Shipping instructions
        if (getEndorsement() != null) options.put("endorsement", getEndorsement());
        if (getEndShipperId() != null) options.put("end_shipper_id", getEndShipperId());
        if (getFreightCharge() != null) options.put("freight_charge", getFreightCharge());
        if (getHandlingInstructions() != null) options.put("handling_instructions", getHandlingInstructions());
        if (getHazmat() != null) options.put("hazmat", getHazmat());
        if (getHoldForPickup() != null) options.put("hold_for_pickup", getHoldForPickup());
        if (getIncoterm() != null) options.put("incoterm", getIncoterm());
        if (getInvoiceNumber() != null) options.put("invoice_number", getInvoiceNumber());

        // Label options
        if (getLabelDate() != null) options.put("label_date", getLabelDate());
        if (getLabelFormat() != null) options.put("label_format", getLabelFormat());
        if (getPostageLabelInline() != null) options.put("postage_label_inline", getPostageLabelInline());

        // Live animal and misc
        if (getLiveAnimal() != null) options.put("live_animal", getLiveAnimal());
        if (getMachinable() != null) options.put("machinable", getMachinable());
        if (getMerchantId() != null) options.put("merchant_id", getMerchantId());

        // Perishable and pickup
        if (getPerishable() != null) options.put("perishable", getPerishable());
        if (getPickupMinDatetime() != null) options.put("pickup_min_datetime", getPickupMinDatetime().toString());
        if (getPickupMaxDatetime() != null) options.put("pickup_max_datetime", getPickupMaxDatetime().toString());

        // Custom print fields
        if (getPrintCustom1() != null) options.put("print_custom_1", getPrintCustom1());
        if (getPrintCustom2() != null) options.put("print_custom_2", getPrintCustom2());
        if (getPrintCustom3() != null) options.put("print_custom_3", getPrintCustom3());
        if (getPrintCustom1Barcode() != null) options.put("print_custom_1_barcode", getPrintCustom1Barcode());
        if (getPrintCustom2Barcode() != null) options.put("print_custom_2_barcode", getPrintCustom2Barcode());
        if (getPrintCustom3Barcode() != null) options.put("print_custom_3_barcode", getPrintCustom3Barcode());
        if (getPrintCustom1Code() != null) options.put("print_custom_1_code", getPrintCustom1Code());
        if (getPrintCustom2Code() != null) options.put("print_custom_2_code", getPrintCustom2Code());
        if (getPrintCustom3Code() != null) options.put("print_custom_3_code", getPrintCustom3Code());

        // Delivery preferences
        if (getSaturdayDelivery() != null) options.put("saturday_delivery", getSaturdayDelivery());
        if (getSpecialRatesEligibility() != null) options.put("special_rates_eligibility", getSpecialRatesEligibility());

        // SmartPost options
        if (getSmartpostHub() != null) options.put("smartpost_hub", getSmartpostHub());
        if (getSmartpostManifest() != null) options.put("smartpost_manifest", getSmartpostManifest());

        // DHL specific
        if (getBillingRef() != null) options.put("billing_ref", getBillingRef());

        // USPS specific options
        if (getCertifiedMail() != null) options.put("certified_mail", getCertifiedMail());
        if (getRegisteredMail() != null) options.put("registered_mail", getRegisteredMail());
        if (getRegisteredMailAmount() != null) options.put("registered_mail_amount", getRegisteredMailAmount());
        if (getReturnReceipt() != null) options.put("return_receipt", getReturnReceipt());

        // Add nested objects
        if (getDutyPayment() != null) {
            Map<String, Object> dutyPaymentMap = new java.util.HashMap<>();
            if (getDutyPayment().getType() != null) dutyPaymentMap.put("type", getDutyPayment().getType());
            if (getDutyPayment().getAccount() != null) dutyPaymentMap.put("account", getDutyPayment().getAccount());
            if (getDutyPayment().getCountry() != null) dutyPaymentMap.put("country", getDutyPayment().getCountry());
            if (getDutyPayment().getPostalCode() != null) dutyPaymentMap.put("postal_code", getDutyPayment().getPostalCode());
            if (!dutyPaymentMap.isEmpty()) options.put("duty_payment", dutyPaymentMap);
        }

        if (getPayment() != null) {
            Map<String, Object> paymentMap = new java.util.HashMap<>();
            if (getPayment().getType() != null) paymentMap.put("type", getPayment().getType());
            if (getPayment().getAccount() != null) paymentMap.put("account", getPayment().getAccount());
            if (getPayment().getCountry() != null) paymentMap.put("country", getPayment().getCountry());
            if (getPayment().getPostalCode() != null) paymentMap.put("postal_code", getPayment().getPostalCode());
            if (!paymentMap.isEmpty()) options.put("payment", paymentMap);
        }

        return options;
    }

    @Override
    public String getProviderType() {
        return "tugboat";
    }
}
