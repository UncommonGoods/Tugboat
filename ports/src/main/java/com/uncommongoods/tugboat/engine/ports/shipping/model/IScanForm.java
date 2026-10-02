package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.List;

public interface IScanForm extends IEasyPostResource {
    String getStatus();

    String getMessage();

    IAddress getFromAddress();

    List<String> getTrackingCodes();

    String getFormUrl();

    String getFormFileType();

    String getConfirmation();

    String getBatchId();
}
