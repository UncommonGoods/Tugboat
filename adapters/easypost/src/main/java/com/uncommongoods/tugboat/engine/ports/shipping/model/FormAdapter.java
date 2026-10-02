package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.Form;

public class FormAdapter implements IForm {
    private final Form form;

    public FormAdapter(Form form) {
        this.form = form;
    }

    @Override
    public String getId() {
        return form.getId();
    }

    @Override
    public String getObject() {
        return form.getObject();
    }

    @Override
    public String getFormType() {
        return form.getFormType();
    }

    @Override
    public String getFormUrl() {
        return form.getFormUrl();
    }

    @Override
    public Boolean getSubmittedElectronically() {
        return form.getSubmittedElectronically();
    }

    @Override
    public String prettyPrint() {
        return form.prettyPrint();
    }

    @Override
    public java.util.Date getUpdatedAt() {
        return form.getUpdatedAt();
    }

    @Override
    public String getMode() {
        return form.getMode();
    }

    @Override
    public java.util.Date getCreatedAt() {
        return form.getCreatedAt();
    }

    public Form getForm() {
        return form;
    }
}
