package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.Map;

public interface IShipmentOptions extends JsonSerializable, Mappable {
    Map<String, Object> toMap();

    void validateOptions();
}
