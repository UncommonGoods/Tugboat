package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ShipmentOptionsAdapter  extends ShipmentOptions {
    @Override
    public JsonElement toJson() {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
        return gson.toJsonTree(this);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> optionsMap = new HashMap<>();

        addIfNotNull(optionsMap, "additional_handling", this.getAdditionalHandling());
        addIfNotNull(optionsMap, "address_validation_level", this.getAddressValidationLevel());
        addIfNotNull(optionsMap, "alcohol", this.getAlcohol());
        addIfNotNull(optionsMap, "by_drone", this.getByDrone());
        addIfNotNull(optionsMap, "carbon_neutral", this.getCarbonNeutral());
        addIfNotNull(optionsMap, "carrier_notification_email", this.getCarrierNotificationEmail());
        addIfNotNull(optionsMap, "carrier_notification_sms", this.getCarrierNotificationSms());

        addIfNotNull(optionsMap, "cod_amount", this.getCodAmount());
        addIfNotNull(optionsMap, "cod_method", this.getCodMethod());
        addIfNotNull(optionsMap, "cod_address_id", this.getCodAddressId());

        addIfNotNull(optionsMap, "content_description", this.getContentDescription());
        addIfNotNull(optionsMap, "currency", this.getCurrency());

        addIfNotNull(optionsMap, "delivery_confirmation", this.getDeliveryConfirmation());
        addDateTimeIfNotNull(optionsMap, "delivery_min_datetime", this.getDeliveryMinDatetime());
        addDateTimeIfNotNull(optionsMap, "delivery_max_datetime", this.getDeliveryMaxDatetime());
        addIfNotNull(optionsMap, "dropoff_type", this.getDropoffType());
        addIfNotNull(optionsMap, "date_advance", this.getDateAdvance());

        addIfNotNull(optionsMap, "dry_ice", this.getDryIce());
        addIfNotNull(optionsMap, "dry_ice_medical", this.getDryIceMedical());
        addIfNotNull(optionsMap, "dry_ice_weight", this.getDryIceWeight());

        addDutyPaymentIfNotNull(optionsMap, this.getDutyPayment());

        addIfNotNull(optionsMap, "endorsement", this.getEndorsement());
        addIfNotNull(optionsMap, "end_shipper_id", this.getEndShipperId());
        addIfNotNull(optionsMap, "freight_charge", this.getFreightCharge());
        addIfNotNull(optionsMap, "handling_instructions", this.getHandlingInstructions());
        addIfNotNull(optionsMap, "hazmat", this.getHazmat());
        addIfNotNull(optionsMap, "hold_for_pickup", this.getHoldForPickup());
        addIfNotNull(optionsMap, "incoterm", this.getIncoterm());
        addIfNotNull(optionsMap, "invoice_number", this.getInvoiceNumber());

        addIfNotNull(optionsMap, "label_date", this.getLabelDate());
        addIfNotNull(optionsMap, "label_format", this.getLabelFormat());
        addIfNotNull(optionsMap, "postage_label_inline", this.getPostageLabelInline());

        addIfNotNull(optionsMap, "live_animal", this.getLiveAnimal());
        addIfNotNull(optionsMap, "machinable", this.getMachinable());
        addIfNotNull(optionsMap, "merchant_id", this.getMerchantId());

        addPaymentIfNotNull(optionsMap, this.getPayment());

        addIfNotNull(optionsMap, "perishable", this.getPerishable());
        addDateTimeIfNotNull(optionsMap, "pickup_min_datetime", this.getPickupMinDatetime());
        addDateTimeIfNotNull(optionsMap, "pickup_max_datetime", this.getPickupMaxDatetime());

        addIfNotNull(optionsMap, "print_custom_1", this.getPrintCustom1());
        addIfNotNull(optionsMap, "print_custom_2", this.getPrintCustom2());
        addIfNotNull(optionsMap, "print_custom_3", this.getPrintCustom3());
        addIfNotNull(optionsMap, "print_custom_1_barcode", this.getPrintCustom1Barcode());
        addIfNotNull(optionsMap, "print_custom_2_barcode", this.getPrintCustom2Barcode());
        addIfNotNull(optionsMap, "print_custom_3_barcode", this.getPrintCustom3Barcode());
        addIfNotNull(optionsMap, "print_custom_1_code", this.getPrintCustom1Code());
        addIfNotNull(optionsMap, "print_custom_2_code", this.getPrintCustom2Code());
        addIfNotNull(optionsMap, "print_custom_3_code", this.getPrintCustom3Code());

        addIfNotNull(optionsMap, "saturday_delivery", this.getSaturdayDelivery());
        addIfNotNull(optionsMap, "special_rates_eligibility", this.getSpecialRatesEligibility());

        addIfNotNull(optionsMap, "smartpost_hub", this.getSmartpostHub());
        addIfNotNull(optionsMap, "smartpost_manifest", this.getSmartpostManifest());

        addIfNotNull(optionsMap, "billing_ref", this.getBillingRef());

        addIfNotNull(optionsMap, "certified_mail", this.getCertifiedMail());
        addIfNotNull(optionsMap, "registered_mail", this.getRegisteredMail());
        addIfNotNull(optionsMap, "registered_mail_amount", this.getRegisteredMailAmount());
        addIfNotNull(optionsMap, "return_receipt", this.getReturnReceipt());

        return optionsMap.isEmpty() ? null : optionsMap;
    }

    private static void addIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private static void addDateTimeIfNotNull(Map<String, Object> map, String key, java.time.LocalDateTime dateTime) {
        if (dateTime != null) {
            map.put(key, dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    private static void addDutyPaymentIfNotNull(Map<String, Object> map, ShipmentOptions.DutyPayment dutyPayment) {
        if (dutyPayment != null) {
            Map<String, Object> dutyMap = new HashMap<>();
            addIfNotNull(dutyMap, "type", dutyPayment.getType());
            addIfNotNull(dutyMap, "account", dutyPayment.getAccount());
            addIfNotNull(dutyMap, "country", dutyPayment.getCountry());
            addIfNotNull(dutyMap, "postal_code", dutyPayment.getPostalCode());

            if (!dutyMap.isEmpty()) {
                map.put("duty_payment", dutyMap);
            }
        }
    }

    private static void addPaymentIfNotNull(Map<String, Object> map, ShipmentOptions.Payment payment) {
        if (payment != null) {
            Map<String, Object> paymentMap = new HashMap<>();
            addIfNotNull(paymentMap, "type", payment.getType());
            addIfNotNull(paymentMap, "account", payment.getAccount());
            addIfNotNull(paymentMap, "country", payment.getCountry());
            addIfNotNull(paymentMap, "postal_code", payment.getPostalCode());

            if (!paymentMap.isEmpty()) {
                map.put("payment", paymentMap);
            }
        }
    }

    @Override
    public String getProviderType() {
        return "easypost";
    }
}
