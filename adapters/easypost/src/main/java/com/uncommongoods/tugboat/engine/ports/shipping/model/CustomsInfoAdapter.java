package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.easypost.model.CustomsInfo;
import com.easypost.model.CustomsItem;
import com.easypost.model.EasyPostResource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class CustomsInfoAdapter extends EasyPostResource implements ICustomsInfo, Mappable {
    private final CustomsInfo customsInfo;

    public CustomsInfoAdapter(CustomsInfo customsInfo) {
        super();
        this.customsInfo = customsInfo;
    }

    @Override
    public String getContentsType() {
        return customsInfo.getContentsType();
    }

    @Override
    public String getContentsExplanation() {
        return customsInfo.getContentsExplanation();
    }

    @Override
    public boolean isCustomsCertify() {
        return customsInfo.isCustomsCertify();
    }

    @Override
    public String getCustomsSigner() {
        return customsInfo.getCustomsSigner();
    }

    @Override
    public String getNonDeliveryOption() {
        return customsInfo.getNonDeliveryOption();
    }

    @Override
    public String getRestrictionType() {
        return customsInfo.getRestrictionType();
    }

    @Override
    public String getRestrictionComments() {
        return customsInfo.getRestrictionComments();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ICustomsItem> getCustomsItems() {
        List<CustomsItem> items = customsInfo.getCustomsItems();
        if (items == null) {
            return null;
        }

        return items.stream()
            .map(CustomsItemAdapter::new)
            .collect(Collectors.toList());
    }

    @Override
    public String getEelPfc() {
        return customsInfo.getEelPfc();
    }

    @Override
    public String getDeclaration() {
        return customsInfo.getDeclaration();
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        if (getContentsType() != null) map.put("contents_type", getContentsType());
        if (getContentsExplanation() != null) map.put("contents_explanation", getContentsExplanation());
        map.put("customs_certify", isCustomsCertify());
        if (getCustomsSigner() != null) map.put("customs_signer", getCustomsSigner());
        if (getNonDeliveryOption() != null) map.put("non_delivery_option", getNonDeliveryOption());
        if (getRestrictionType() != null) map.put("restriction_type", getRestrictionType());
        if (getRestrictionComments() != null) map.put("restriction_comments", getRestrictionComments());
        if (getEelPfc() != null) map.put("eel_pfc", getEelPfc());
        if (getDeclaration() != null) map.put("declaration", getDeclaration());

        // Add customs items
        List<ICustomsItem> customsItems = getCustomsItems();
        if (customsItems != null) {
            List<Map<String, Object>> itemMaps = new ArrayList<>();
            for (ICustomsItem item : customsItems) {
                if (item instanceof Mappable) {
                    itemMaps.add(((Mappable) item).toMap());
                }
            }
            map.put("customs_items", itemMaps);
        }

        return map;
    }

    public CustomsInfo getCustomsInfo() {
        return customsInfo;
    }

    @Override
    public JsonElement toJson() {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
        return gson.toJsonTree(this);
    }

    @Override
    public String getProviderType() {
        return "easypost";
    }
}
