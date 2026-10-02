package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.EasyPostResource;
import com.easypost.model.Field;
import com.easypost.model.Fields;

import java.util.HashMap;
import java.util.Map;

public class FieldsAdapter extends EasyPostResource implements IFields {
    private final Fields fields;

    public FieldsAdapter(Fields fields) {
        super();
        this.fields = fields;
    }

    @Override
    public Map<String, IField> getCredentials() {
        Map<String, Field> credentials = fields.getCredentials();
        if (credentials == null) {
            return null;
        }

        Map<String, IField> adaptedCredentials = new HashMap<>();
        for (Map.Entry<String, Field> entry : credentials.entrySet()) {
            adaptedCredentials.put(entry.getKey(), new FieldAdapter(entry.getValue()));
        }

        return adaptedCredentials;
    }

    @Override
    public Map<String, IField> getTestCredentials() {
        Map<String, Field> testCredentials = fields.getTestCredentials();
        if (testCredentials == null) {
            return null;
        }

        Map<String, IField> adaptedTestCredentials = new HashMap<>();
        for (Map.Entry<String, Field> entry : testCredentials.entrySet()) {
            adaptedTestCredentials.put(entry.getKey(), new FieldAdapter(entry.getValue()));
        }

        return adaptedTestCredentials;
    }

    @Override
    public boolean isAutoLink() {
        return fields.isAutoLink();
    }

    @Override
    public boolean isCustomWorkflow() {
        return fields.isCustomWorkflow();
    }

    public Fields getFields() {
        return fields;
    }
}
