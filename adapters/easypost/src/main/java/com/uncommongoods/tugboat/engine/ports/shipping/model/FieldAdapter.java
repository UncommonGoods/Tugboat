package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.EasyPostResource;
import com.easypost.model.Field;

public class FieldAdapter extends EasyPostResource implements IField {
    private final Field field;

    public FieldAdapter(Field field) {
        super();
        this.field = field;
    }

    @Override
    public String getVisibility() {
        return field.getVisibility();
    }

    @Override
    public String getLabel() {
        return field.getLabel();
    }

    @Override
    public String getValue() {
        return field.getValue();
    }

    public Field getField() {
        return field;
    }
}
