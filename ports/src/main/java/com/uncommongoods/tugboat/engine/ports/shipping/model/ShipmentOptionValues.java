package com.uncommongoods.tugboat.engine.ports.shipping.model;

public class ShipmentOptionValues {
    // COD Payment Methods
    public enum CodMethod {
        CASH,
        CHECK,
        MONEY_ORDER
    }

    // Delivery Confirmation Options
    public enum DeliveryConfirmation {
        // General options
        ADULT_SIGNATURE,
        SIGNATURE,
        NO_SIGNATURE,

        // FedEx specific
        INDIRECT_SIGNATURE,
        SERVICE_DEFAULT,

        // USPS specific
        ADULT_SIGNATURE_RESTRICTED,
        SIGNATURE_RESTRICTED,

        // Canada Post specific
        DO_NOT_SAFE_DROP,

        // GSO specific
        STANDARD_SIGNATURE
    }

    // Dropoff Types
    public enum DropoffType {
        REGULAR_PICKUP,
        SCHEDULED_PICKUP,
        RETAIL_LOCATION,
        STATION,
        DROP_BOX
    }

    // Duty Payment Types
    public enum DutyPaymentType {
        SENDER,
        THIRD_PARTY,
        RECEIVER
    }

    // Endorsement Options
    public enum Endorsement {
        ADDRESS_SERVICE_REQUESTED,
        FORWARDING_SERVICE_REQUESTED,
        CHANGE_SERVICE_REQUESTED,
        RETURN_SERVICE_REQUESTED,
        LEAVE_IF_NO_RESPONSE
    }

    // Hazmat Types
    public enum HazmatType {
        // FedEx and DHL eCommerce
        PRIMARY_CONTAINED,
        PRIMARY_PACKED,
        PRIMARY,
        SECONDARY_CONTAINED,
        SECONDARY_PACKED,
        SECONDARY,
        ORMD,
        LITHIUM,

        // FedEx, DHL eCommerce, and USPS
        LIMITED_QUANTITY,

        // USPS specific
        AIR_ELIGIBLE_ETHANOL,
        CLASS_1,
        CLASS_3,
        CLASS_7,
        CLASS_8_CORROSIVE,
        CLASS_8_WET_BATTERY,
        CLASS_9_NEW_LITHIUM_INDIVIDUAL,
        CLASS_9_USED_LITHIUM,
        CLASS_9_NEW_LITHIUM_DEVICE,
        CLASS_9_DRY_ICE,
        CLASS_9_UNMARKED_LITHIUM,
        CLASS_9_MAGNETIZED,
        DIVISION_4_1,
        DIVISION_5_1,
        DIVISION_5_2,
        DIVISION_6_1,
        DIVISION_6_2,
        EXCEPTED_QUANTITY_PROVISION,
        GROUND_ONLY,
        ID8000,
        LIGHTERS,
        SMALL_QUANTITY_PROVISION
    }

    // Incoterms
    public enum Incoterm {
        CFR,
        CIF,
        CIP,
        CPT,
        DAT,
        DAP,
        DDP,
        EXW,
        FAS,
        FCA,
        FOB
    }

    // Label Formats
    public enum LabelFormat {
        PNG,
        PDF,
        ZPL,
        EPL2
    }

    // Live Animal Types
    public enum LiveAnimalType {
        BEES,
        DAY_OLD_POULTRY,
        ADULT_BIRDS,
        OTHER_LIVES
    }

    // Payment Types
    public enum PaymentType {
        SENDER,
        THIRD_PARTY,
        RECEIVER,
        COLLECT
    }

    // Print Custom Codes for FedEx
    public enum FedExPrintCustomCode {
        CUSTOMER_REFERENCE(null), // Default when not provided
        PO, // Purchase Order Number
        DP, // Department Number
        RMA; // Return Merchandise Authorization

        private final String description;

        FedExPrintCustomCode() {
            this.description = null;
        }

        FedExPrintCustomCode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Print Custom Codes for UPS
    public enum UPSPrintCustomCode {
        AJ("AJ"), // Accounts Receivable Customer Account
        AT("AT"), // Appropriation Number
        BM("BM"), // Bill of Lading Number
        COD_NUMBER("9V"), // Collect on Delivery (COD) Number
        ON("ON"), // Dealer Order Number
        DP("DP"), // Department Number
        FDA_PRODUCT_CODE("3Q"), // Food and Drug Administration (FDA) Product Code
        IK("IK"), // Invoice Number
        MK("MK"), // Manifest Key Number
        MJ("MJ"), // Model Number
        PM("PM"), // Part Number
        PC("PC"), // Production Code
        PO("PO"), // Purchase Order Number
        RQ("RQ"), // Purchase Request Number
        RZ("RZ"), // Return Authorization Number
        SA("SA"), // Salesperson Number
        SE("SE"), // Serial Number
        ST("ST"), // Store Number
        TN("TN"), // Transaction Reference Number
        EI("EI"), // Employer's ID Number
        TJ("TJ"); // Federal Taxpayer ID Number

        private final String code;

        UPSPrintCustomCode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // Special Rates Eligibility
    public enum SpecialRatesEligibility {
        MEDIA_MAIL("USPS.MEDIAMAIL"),
        LIBRARY_MAIL("USPS.LIBRARYMAIL");

        private final String value;

        SpecialRatesEligibility(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        // Method to combine multiple eligibilities
        public static String combine(SpecialRatesEligibility... eligibilities) {
            return String.join(",",
                java.util.Arrays.stream(eligibilities)
                    .map(SpecialRatesEligibility::getValue)
                    .toArray(String[]::new));
        }
    }

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
    }
}
