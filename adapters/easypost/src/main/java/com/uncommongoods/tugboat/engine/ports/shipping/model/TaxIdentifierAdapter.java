package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.TaxIdentifier;

public class TaxIdentifierAdapter implements ITaxIdentifier {
    private final TaxIdentifier taxIdentifier;

    public TaxIdentifierAdapter(TaxIdentifier taxIdentifier) {
        this.taxIdentifier = taxIdentifier;
    }

    @Override
    public String getId() {
        return taxIdentifier.getId();
    }

    @Override
    public String getObject() {
        return taxIdentifier.getObject();
    }

    @Override
    public String getEntity() {
        return taxIdentifier.getEntity();
    }

    @Override
    public String getTaxId() {
        return taxIdentifier.getTaxId();
    }

    @Override
    public String getTaxIdType() {
        return taxIdentifier.getTaxIdType();
    }

    @Override
    public String getIssuingCountry() {
        return taxIdentifier.getIssuingCountry();
    }

    @Override
    public String prettyPrint() {
        return taxIdentifier.prettyPrint();
    }

    @Override
    public java.util.Date getUpdatedAt() {
        return taxIdentifier.getUpdatedAt();
    }

    @Override
    public String getMode() {
        return taxIdentifier.getMode();
    }

    @Override
    public java.util.Date getCreatedAt() {
        return taxIdentifier.getCreatedAt();
    }

    public TaxIdentifier getTaxIdentifier() {
        return taxIdentifier;
    }
}
