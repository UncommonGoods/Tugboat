package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.PostageLabel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.util.Date;
import java.util.Map;

public class PostageLabelAdapter implements IPostageLabel {
    Gson gson = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .create();
    private PostageLabel label;

    public PostageLabelAdapter(PostageLabel label) {
        super();
        this.label = label;
    }

    public PostageLabelAdapter(Map<String, Object> labelMap) {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
        String json = gson.toJson(labelMap);
        this.label = gson.fromJson(json, PostageLabel.class);
    }

    @Override
    public int getDateAdvance() {
        return label.getDateAdvance();
    }

    @Override
    public String getIntegratedForm() {
        return label.getIntegratedForm();
    }

    @Override
    public int getLabelResolution() {
        return label.getLabelResolution();
    }

    @Override
    public String getLabelSize() {
        return label.getLabelSize();
    }

    @Override
    public String getLabelType() {
        return label.getLabelType();
    }

    @Override
    public String getLabelUrl() {
        return label.getLabelUrl();
    }

    @Override
    public String getLabelFile() {
        return label.getLabelFile();
    }

    @Override
    public void setLabelFile(String labelFile) {
        Map<String, Object> labelMap = gson.fromJson(this.toJsonString(), Map.class);
        labelMap.put("labelFile", labelFile);
        String json = gson.toJson(labelMap);
        this.label = gson.fromJson(json, PostageLabel.class);
    }

    @Override
    public String getLabelFileType() {
        return label.getLabelFileType();
    }

    @Override
    public String getLabelPdfSize() {
        return label.getLabelPdfSize();
    }

    @Override
    public String getLabelPdfType() {
        return label.getLabelPdfType();
    }

    @Override
    public String getLabelPdfUrl() {
        return label.getLabelPdfUrl();
    }

    @Override
    public String getLabelPdfFileType() {
        return label.getLabelPdfFileType();
    }

    @Override
    public String getLabelEpl2Size() {
        return label.getLabelEpl2Size();
    }

    @Override
    public String getLabelEpl2Type() {
        return label.getLabelEpl2Type();
    }

    @Override
    public String getLabelEpl2Url() {
        return label.getLabelEpl2Url();
    }

    @Override
    public String getLabelEpl2FileType() {
        return label.getLabelEpl2FileType();
    }

    @Override
    public String getLabelZplSize() {
        return label.getLabelZplSize();
    }

    @Override
    public String getLabelZplType() {
        return label.getLabelZplType();
    }

    @Override
    public String getLabelZplUrl() {
        return label.getLabelZplUrl();
    }

    @Override
    public String getLabelZplFileType() {
        return label.getLabelZplFileType();
    }

    public PostageLabel getPostageLabel() {
        return label;
    }

    @Override
    public JsonElement toJson() {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
        return gson.toJsonTree(this.label);
    }

    @Override
    public String toJsonString() {
        return toJson().toString();
    }

    @Override
    public String getId() {
        return label.getId();
    }

    @Override
    public String getMode() {
        return label.getMode();
    }

    @Override
    public String getObject() {
        return label.getObject();
    }

    @Override
    public Date getCreatedAt() {
        return label.getCreatedAt();
    }

    @Override
    public Date getUpdatedAt() {
        return label.getUpdatedAt();
    }

    @Override
    public String prettyPrint() {
        return "";
    }

    @Override
    public String getProviderType() {
        return "easypost";
    }
}
