package com.uncommongoods.tugboat.engine.ports.shipping.service;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.easypost.model.Parcel;
import com.easypost.service.EasyPostClient;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IParcel;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ParcelAdapter;

import java.util.Map;

public class ParcelServiceAdapter implements IParcelService {
    private final EasyPostClient client;

    public ParcelServiceAdapter(EasyPostClient client) {
        this.client = client;
    }

    @Override
    public IParcel create(Map<String, Object> params) throws TugboatException {
        try {
            Parcel parcel = client.parcel.create(params);
            return new ParcelAdapter(parcel);
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IParcel retrieve(String id) throws TugboatException {
        try {
            Parcel parcel = client.parcel.retrieve(id);
            return new ParcelAdapter(parcel);
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }
}
