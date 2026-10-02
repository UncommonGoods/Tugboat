package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.google.gson.annotations.Expose;
import java.time.LocalDateTime;
import java.util.Arrays;

public abstract class ShipmentOptions implements IShipmentOptions, Cloneable {
    // Basic options
    @Expose
    private Boolean additionalHandling;
    @Expose
    private String addressValidationLevel;
    @Expose
    private Boolean alcohol;
    @Expose
    private Boolean byDrone;
    @Expose
    private Boolean carbonNeutral;
    @Expose
    private String carrierNotificationEmail;
    @Expose
    private String carrierNotificationSms;

    // COD options
    @Expose
    private String codAmount;
    @Expose
    private String codMethod;
    @Expose
    private String codAddressId;

    // Content and currency
    @Expose
    private String contentDescription;
    @Expose
    private String currency;

    // Delivery options
    @Expose
    private String deliveryConfirmation;
    @Expose
    private LocalDateTime deliveryMinDatetime;
    @Expose
    private LocalDateTime deliveryMaxDatetime;
    @Expose
    private String dropoffType;
    @Expose
    private Integer dateAdvance;

    // Dry ice options
    @Expose
    private Boolean dryIce;
    @Expose
    private Boolean dryIceMedical;
    @Expose
    private String dryIceWeight;

    // Duty payment
    @Expose
    private DutyPayment dutyPayment;

    // Shipping instructions
    @Expose
    private String endorsement;
    @Expose
    private String endShipperId;
    @Expose
    private Double freightCharge;
    @Expose
    private String handlingInstructions;
    @Expose
    private String hazmat;
    @Expose
    private Boolean holdForPickup;
    @Expose
    private String incoterm;
    @Expose
    private String invoiceNumber;

    // Label options
    @Expose
    private String labelDate;
    @Expose
    private String labelFormat;
    @Expose
    private Boolean postageLabelInline;

    // Live animal
    @Expose
    private String liveAnimal;
    @Expose
    private Boolean machinable;
    @Expose
    private String merchantId;

    // Payment
    @Expose
    private Payment payment;

    // Perishable and pickup
    @Expose
    private Boolean perishable;
    @Expose
    private LocalDateTime pickupMinDatetime;
    @Expose
    private LocalDateTime pickupMaxDatetime;

    // Custom print fields
    @Expose
    private String printCustom1;
    @Expose
    private String printCustom2;
    @Expose
    private String printCustom3;
    @Expose
    private Boolean printCustom1Barcode;
    @Expose
    private Boolean printCustom2Barcode;
    @Expose
    private Boolean printCustom3Barcode;
    @Expose
    private String printCustom1Code;
    @Expose
    private String printCustom2Code;
    @Expose
    private String printCustom3Code;

    // Delivery preferences
    @Expose
    private Boolean saturdayDelivery;
    @Expose
    private String specialRatesEligibility;

    // SmartPost options
    @Expose
    private String smartpostHub;
    @Expose
    private String smartpostManifest;

    // DHL specific
    @Expose
    private String billingRef;

    // USPS specific options
    @Expose
    private Boolean certifiedMail;
    @Expose
    private Boolean registeredMail;
    @Expose
    private Double registeredMailAmount;
    @Expose
    private Boolean returnReceipt;

    // Constructors
    public ShipmentOptions() {
    }

    // Getters and Setters
    public Boolean getAdditionalHandling() {
        return additionalHandling;
    }

    public void setAdditionalHandling(Boolean additionalHandling) {
        this.additionalHandling = additionalHandling;
    }

    public String getAddressValidationLevel() {
        return addressValidationLevel;
    }

    public void setAddressValidationLevel(String addressValidationLevel) {
        this.addressValidationLevel = addressValidationLevel;
    }

    public Boolean getAlcohol() {
        return alcohol;
    }

    public void setAlcohol(Boolean alcohol) {
        this.alcohol = alcohol;
    }

    public Boolean getByDrone() {
        return byDrone;
    }

    public void setByDrone(Boolean byDrone) {
        this.byDrone = byDrone;
    }

    public Boolean getCarbonNeutral() {
        return carbonNeutral;
    }

    public void setCarbonNeutral(Boolean carbonNeutral) {
        this.carbonNeutral = carbonNeutral;
    }

    public String getCarrierNotificationEmail() {
        return carrierNotificationEmail;
    }

    public void setCarrierNotificationEmail(String carrierNotificationEmail) {
        this.carrierNotificationEmail = carrierNotificationEmail;
    }

    public String getCarrierNotificationSms() {
        return carrierNotificationSms;
    }

    public void setCarrierNotificationSms(String carrierNotificationSms) {
        this.carrierNotificationSms = carrierNotificationSms;
    }

    public String getCodAmount() {
        return codAmount;
    }

    public void setCodAmount(String codAmount) {
        this.codAmount = codAmount;
    }

    public String getCodMethod() {
        return codMethod;
    }

    public void setCodMethod(String codMethod) {
        this.codMethod = codMethod;
    }

    public String getCodAddressId() {
        return codAddressId;
    }

    public void setCodAddressId(String codAddressId) {
        this.codAddressId = codAddressId;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDeliveryConfirmation() {
        return deliveryConfirmation;
    }

    public void setDeliveryConfirmation(String deliveryConfirmation) {
        this.deliveryConfirmation = deliveryConfirmation;
    }

    public LocalDateTime getDeliveryMinDatetime() {
        return deliveryMinDatetime;
    }

    public void setDeliveryMinDatetime(LocalDateTime deliveryMinDatetime) {
        this.deliveryMinDatetime = deliveryMinDatetime;
    }

    public LocalDateTime getDeliveryMaxDatetime() {
        return deliveryMaxDatetime;
    }

    public void setDeliveryMaxDatetime(LocalDateTime deliveryMaxDatetime) {
        this.deliveryMaxDatetime = deliveryMaxDatetime;
    }

    public String getDropoffType() {
        return dropoffType;
    }

    public void setDropoffType(String dropoffType) {
        this.dropoffType = dropoffType;
    }

    public Integer getDateAdvance() {
        return dateAdvance;
    }

    public void setDateAdvance(Integer dateAdvance) {
        this.dateAdvance = dateAdvance;
    }

    public Boolean getDryIce() {
        return dryIce;
    }

    public void setDryIce(Boolean dryIce) {
        this.dryIce = dryIce;
    }

    public Boolean getDryIceMedical() {
        return dryIceMedical;
    }

    public void setDryIceMedical(Boolean dryIceMedical) {
        this.dryIceMedical = dryIceMedical;
    }

    public String getDryIceWeight() {
        return dryIceWeight;
    }

    public void setDryIceWeight(String dryIceWeight) {
        this.dryIceWeight = dryIceWeight;
    }

    public DutyPayment getDutyPayment() {
        return dutyPayment;
    }

    public void setDutyPayment(DutyPayment dutyPayment) {
        this.dutyPayment = dutyPayment;
    }

    public String getEndorsement() {
        return endorsement;
    }

    public void setEndorsement(String endorsement) {
        this.endorsement = endorsement;
    }

    public String getEndShipperId() {
        return endShipperId;
    }

    public void setEndShipperId(String endShipperId) {
        this.endShipperId = endShipperId;
    }

    public Double getFreightCharge() {
        return freightCharge;
    }

    public void setFreightCharge(Double freightCharge) {
        this.freightCharge = freightCharge;
    }

    public String getHandlingInstructions() {
        return handlingInstructions;
    }

    public void setHandlingInstructions(String handlingInstructions) {
        this.handlingInstructions = handlingInstructions;
    }

    public String getHazmat() {
        return hazmat;
    }

    public void setHazmat(String hazmat) {
        this.hazmat = hazmat;
    }

    public Boolean getHoldForPickup() {
        return holdForPickup;
    }

    public void setHoldForPickup(Boolean holdForPickup) {
        this.holdForPickup = holdForPickup;
    }

    public String getIncoterm() {
        return incoterm;
    }

    public void setIncoterm(String incoterm) {
        this.incoterm = incoterm;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getLabelDate() {
        return labelDate;
    }

    public void setLabelDate(String labelDate) {
        this.labelDate = labelDate;
    }

    public String getLabelFormat() {
        return labelFormat;
    }

    public void setLabelFormat(String labelFormat) {
        this.labelFormat = labelFormat;
    }

    public Boolean getPostageLabelInline() {
        return postageLabelInline;
    }

    public void setPostageLabelInline(Boolean postageLabelInline) {
        this.postageLabelInline = postageLabelInline;
    }

    public String getLiveAnimal() {
        return liveAnimal;
    }

    public void setLiveAnimal(String liveAnimal) {
        this.liveAnimal = liveAnimal;
    }

    public Boolean getMachinable() {
        return machinable;
    }

    public void setMachinable(Boolean machinable) {
        this.machinable = machinable;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Boolean getPerishable() {
        return perishable;
    }

    public void setPerishable(Boolean perishable) {
        this.perishable = perishable;
    }

    public LocalDateTime getPickupMinDatetime() {
        return pickupMinDatetime;
    }

    public void setPickupMinDatetime(LocalDateTime pickupMinDatetime) {
        this.pickupMinDatetime = pickupMinDatetime;
    }

    public LocalDateTime getPickupMaxDatetime() {
        return pickupMaxDatetime;
    }

    public void setPickupMaxDatetime(LocalDateTime pickupMaxDatetime) {
        this.pickupMaxDatetime = pickupMaxDatetime;
    }

    public String getPrintCustom1() {
        return printCustom1;
    }

    public void setPrintCustom1(String printCustom1) {
        this.printCustom1 = printCustom1;
    }

    public String getPrintCustom2() {
        return printCustom2;
    }

    public void setPrintCustom2(String printCustom2) {
        this.printCustom2 = printCustom2;
    }

    public String getPrintCustom3() {
        return printCustom3;
    }

    public void setPrintCustom3(String printCustom3) {
        this.printCustom3 = printCustom3;
    }

    public Boolean getPrintCustom1Barcode() {
        return printCustom1Barcode;
    }

    public void setPrintCustom1Barcode(Boolean printCustom1Barcode) {
        this.printCustom1Barcode = printCustom1Barcode;
    }

    public Boolean getPrintCustom2Barcode() {
        return printCustom2Barcode;
    }

    public void setPrintCustom2Barcode(Boolean printCustom2Barcode) {
        this.printCustom2Barcode = printCustom2Barcode;
    }

    public Boolean getPrintCustom3Barcode() {
        return printCustom3Barcode;
    }

    public void setPrintCustom3Barcode(Boolean printCustom3Barcode) {
        this.printCustom3Barcode = printCustom3Barcode;
    }

    public String getPrintCustom1Code() {
        return printCustom1Code;
    }

    public void setPrintCustom1Code(String printCustom1Code) {
        this.printCustom1Code = printCustom1Code;
    }

    public String getPrintCustom2Code() {
        return printCustom2Code;
    }

    public void setPrintCustom2Code(String printCustom2Code) {
        this.printCustom2Code = printCustom2Code;
    }

    public String getPrintCustom3Code() {
        return printCustom3Code;
    }

    public void setPrintCustom3Code(String printCustom3Code) {
        this.printCustom3Code = printCustom3Code;
    }

    public Boolean getSaturdayDelivery() {
        return saturdayDelivery;
    }

    public void setSaturdayDelivery(Boolean saturdayDelivery) {
        this.saturdayDelivery = saturdayDelivery;
    }

    public String getSpecialRatesEligibility() {
        return specialRatesEligibility;
    }

    public void setSpecialRatesEligibility(String specialRatesEligibility) {
        this.specialRatesEligibility = specialRatesEligibility;
    }

    public String getSmartpostHub() {
        return smartpostHub;
    }

    public void setSmartpostHub(String smartpostHub) {
        this.smartpostHub = smartpostHub;
    }

    public String getSmartpostManifest() {
        return smartpostManifest;
    }

    public void setSmartpostManifest(String smartpostManifest) {
        this.smartpostManifest = smartpostManifest;
    }

    public String getBillingRef() {
        return billingRef;
    }

    public void setBillingRef(String billingRef) {
        this.billingRef = billingRef;
    }

    public Boolean getCertifiedMail() {
        return certifiedMail;
    }

    public void setCertifiedMail(Boolean certifiedMail) {
        this.certifiedMail = certifiedMail;
    }

    public Boolean getRegisteredMail() {
        return registeredMail;
    }

    public void setRegisteredMail(Boolean registeredMail) {
        this.registeredMail = registeredMail;
    }

    public Double getRegisteredMailAmount() {
        return registeredMailAmount;
    }

    public void setRegisteredMailAmount(Double registeredMailAmount) {
        this.registeredMailAmount = registeredMailAmount;
    }

    public Boolean getReturnReceipt() {
        return returnReceipt;
    }

    public void setReturnReceipt(Boolean returnReceipt) {
        this.returnReceipt = returnReceipt;
    }

    // Inner classes for complex objects
    public static class DutyPayment {
        private String type;
        private String account;
        private String country;
        private String postalCode;

        public DutyPayment() {
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public DutyPayment clone() {
            DutyPayment cloned = new DutyPayment();
            cloned.type = this.type;
            cloned.account = this.account;
            cloned.country = this.country;
            cloned.postalCode = this.postalCode;
            return cloned;
        }
    }

    public static class Payment {
        private String type;
        private String account;
        private String country;
        private String postalCode;

        public Payment() {
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public Payment clone() {
            Payment cloned = new Payment();
            cloned.type = this.type;
            cloned.account = this.account;
            cloned.country = this.country;
            cloned.postalCode = this.postalCode;
            return cloned;
        }
    }

    @Override
    public ShipmentOptions clone() {
        try {
            ShipmentOptions cloned = (ShipmentOptions) super.clone();

            // Deep clone complex objects
            if (this.dutyPayment != null) {
                cloned.dutyPayment = this.dutyPayment.clone();
            }
            if (this.payment != null) {
                cloned.payment = this.payment.clone();
            }

            // All other fields are primitives, Strings, or immutable objects (LocalDateTime)
            // so they are safely copied by super.clone() -> Object.clone()

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone ShipmentOptions", e);
        }
    }

    @Override
    public void validateOptions() {
        validateEnumField("codMethod", getCodMethod(), ShipmentOptionValues.CodMethod.class);
        validateEnumField("deliveryConfirmation", getDeliveryConfirmation(), ShipmentOptionValues.DeliveryConfirmation.class);
        validateEnumField("dropoffType", getDropoffType(), ShipmentOptionValues.DropoffType.class);
        validateEnumField("endorsement", getEndorsement(), ShipmentOptionValues.Endorsement.class);
        validateEnumField("hazmat", getHazmat(), ShipmentOptionValues.HazmatType.class);
        validateEnumField("incoterm", getIncoterm(), ShipmentOptionValues.Incoterm.class);
        validateEnumField("labelFormat", getLabelFormat(), ShipmentOptionValues.LabelFormat.class);
        validateEnumField("liveAnimal", getLiveAnimal(), ShipmentOptionValues.LiveAnimalType.class);
        validateMultiEnumField("printCustom1Code", getPrintCustom1Code(), ShipmentOptionValues.FedExPrintCustomCode.class, ShipmentOptionValues.UPSPrintCustomCode.class);
        validateMultiEnumField("printCustom2Code", getPrintCustom2Code(), ShipmentOptionValues.FedExPrintCustomCode.class, ShipmentOptionValues.UPSPrintCustomCode.class);
        validateMultiEnumField("printCustom3Code", getPrintCustom3Code(), ShipmentOptionValues.FedExPrintCustomCode.class, ShipmentOptionValues.UPSPrintCustomCode.class);

        // Validate nested objects
        validateDutyPayment();
        validatePayment();

        // Validate special rates eligibility (can be comma-separated)
        validateSpecialRatesEligibility();
    }

    protected <E extends Enum<E>> void validateEnumField(String fieldName, String value, Class<E> enumClass) {
        if (value == null || value.trim().isEmpty()) {
            return; // Allow null/empty values
        }

        try {
            Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("Invalid value '%s' for field '%s'. Valid values are: %s",
                    value, fieldName, Arrays.toString(enumClass.getEnumConstants())));
        }
    }

    protected void validateMultiEnumField(String fieldName, String value, Class<?>... enumClasses) {
        if (value == null || value.trim().isEmpty()) {
            return; // Allow null/empty values
        }

        boolean isValid = false;
        StringBuilder validValues = new StringBuilder();

        for (Class<?> enumClass : enumClasses) {
            try {
                @SuppressWarnings("unchecked")
                Class<Enum> enumType = (Class<Enum>) enumClass;
                Enum.valueOf(enumType, value.trim().toUpperCase());
                isValid = true;
                break;
            } catch (IllegalArgumentException e) {
                // Try next enum class
            }

            if (!validValues.isEmpty()) {
                validValues.append(", ");
            }
            validValues.append(Arrays.toString(enumClass.getEnumConstants()));
        }

        // Special handling for UPS codes that need to be looked up by actual code value
        if (!isValid) {
            for (Class<?> enumClass : enumClasses) {
                if (enumClass == ShipmentOptionValues.UPSPrintCustomCode.class) {
                    for (ShipmentOptionValues.UPSPrintCustomCode upsCode : ShipmentOptionValues.UPSPrintCustomCode.values()) {
                        if (upsCode.getCode().equals(value.trim())) {
                            isValid = true;
                            break;
                        }
                    }
                }
            }
        }

        if (!isValid) {
            throw new IllegalArgumentException(
                String.format("Invalid value '%s' for field '%s'. Valid values are: %s",
                    value, fieldName, validValues.toString()));
        }
    }

    protected void validateDutyPayment() {
        DutyPayment dutyPayment = getDutyPayment();
        if (dutyPayment != null && dutyPayment.getType() != null) {
            validateEnumField("dutyPayment.type", dutyPayment.getType(), ShipmentOptionValues.DutyPaymentType.class);
        }
    }

    protected void validatePayment() {
        Payment payment = getPayment();
        if (payment != null && payment.getType() != null) {
            validateEnumField("payment.type", payment.getType(), ShipmentOptionValues.PaymentType.class);
        }
    }

    protected void validateSpecialRatesEligibility() {
        String specialRates = getSpecialRatesEligibility();
        if (specialRates == null || specialRates.trim().isEmpty()) {
            return;
        }

        // Handle comma-separated values
        String[] rates = specialRates.split(",");
        for (String rate : rates) {
            String trimmedRate = rate.trim();
            boolean isValid = false;

            for (ShipmentOptionValues.SpecialRatesEligibility eligibility : ShipmentOptionValues.SpecialRatesEligibility.values()) {
                if (eligibility.getValue().equals(trimmedRate)) {
                    isValid = true;
                    break;
                }
            }

            if (!isValid) {
                throw new IllegalArgumentException(
                    String.format("Invalid special rates eligibility value '%s'. Valid values are: %s",
                        trimmedRate, Arrays.toString(ShipmentOptionValues.SpecialRatesEligibility.values())));
            }
        }
    }
}
