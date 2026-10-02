package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.CustomsItem;
import com.easypost.model.EasyPostResource;

public class CustomsItemAdapter extends EasyPostResource implements ICustomsItem {
    private final CustomsItem customsItem;

    public CustomsItemAdapter(CustomsItem customsItem) {
        super();
        this.customsItem = customsItem;
    }

    @Override
    public String getDescription() {
        return customsItem.getDescription();
    }

    @Override
    public String getHsTariffNumber() {
        return customsItem.getHsTariffNumber();
    }

    @Override
    public String getOriginCountry() {
        return customsItem.getOriginCountry();
    }

    @Override
    public int getQuantity() {
        return customsItem.getQuantity();
    }

    @Override
    public Float getValue() {
        return customsItem.getValue();
    }

    @Override
    public Float getWeight() {
        return customsItem.getWeight();
    }

    @Override
    public String getCode() {
        return customsItem.getCode();
    }

    @Override
    public String getCurrency() {
        return customsItem.getCurrency();
    }

    public CustomsItem getCustomsItem() {
        return customsItem;
    }
}
