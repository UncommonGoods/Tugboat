package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.easypost.model.Address;
import com.easypost.model.EasyPostResource;
import com.easypost.model.ScanForm;

import java.util.List;

public class ScanFormAdapter extends EasyPostResource implements IScanForm {
    private final ScanForm scanForm;

    public ScanFormAdapter(ScanForm scanForm) {
        super();
        this.scanForm = scanForm;
    }

    @Override
    public String getStatus() {
        return scanForm.getStatus();
    }

    @Override
    public String getMessage() {
        return scanForm.getMessage();
    }

    @Override
    public IAddress getFromAddress() {
        Address address = scanForm.getFromAddress();
        return address != null ? new AddressAdapter(address) : null;
    }

    @Override
    public List<String> getTrackingCodes() {
        return scanForm.getTrackingCodes();
    }

    @Override
    public String getFormUrl() {
        return scanForm.getFormUrl();
    }

    @Override
    public String getFormFileType() {
        return scanForm.getFormFileType();
    }

    @Override
    public String getConfirmation() {
        return scanForm.getConfirmation();
    }

    @Override
    public String getBatchId() {
        return scanForm.getBatchId();
    }

    public ScanForm getScanForm() {
        return scanForm;
    }
}
