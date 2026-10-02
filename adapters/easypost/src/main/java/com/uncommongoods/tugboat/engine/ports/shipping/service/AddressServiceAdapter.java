package com.uncommongoods.tugboat.engine.ports.shipping.service;

import java.util.Map;

import com.easypost.exception.EasyPostException;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.easypost.model.Address;
import com.easypost.model.AddressCollection;
import com.easypost.service.AddressService;
import com.easypost.service.EasyPostClient;
import com.uncommongoods.tugboat.engine.ports.shipping.model.AddressAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.AddressCollectionAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddress;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IAddressCollection;

public class AddressServiceAdapter implements IAddressService {
    private final AddressService addressService;

    public AddressServiceAdapter(EasyPostClient client) {
        this.addressService = client.address;
    }

    public AddressServiceAdapter(AddressService addressService) {
        this.addressService = addressService;
    }

    @Override
    public IAddress create(Map<String, Object> params) throws TugboatException {
        try {
            Address address = addressService.create(params);
            return address != null ? new AddressAdapter(address) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IAddress retrieve(String id) throws TugboatException {
        try {
            Address address = addressService.retrieve(id);
            return address != null ? new AddressAdapter(address) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IAddressCollection all(Map<String, Object> params) throws TugboatException {
        try {
            AddressCollection addressCollection = addressService.all(params);
            return addressCollection != null ? new AddressCollectionAdapter(addressCollection) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IAddressCollection getNextPage(IAddressCollection collection) throws EndOfPaginationException {
        if (!(collection instanceof AddressCollectionAdapter)) {
            throw new IllegalArgumentException("Collection must be an AddressCollectionAdapter");
        }

        try {
            AddressCollection addressCollection = ((AddressCollectionAdapter) collection).getCollection();
            AddressCollection nextPage = addressService.getNextPage(addressCollection);
            return nextPage != null ? new AddressCollectionAdapter(nextPage) : null;
        } catch (EasyPostException e) {
            throw new EndOfPaginationException(e.getMessage(), e);
        }
    }

    @Override
    public IAddressCollection getNextPage(IAddressCollection collection, Integer pageSize) throws EndOfPaginationException {
        if (!(collection instanceof AddressCollectionAdapter)) {
            throw new IllegalArgumentException("Collection must be an AddressCollectionAdapter");
        }

        try {
            AddressCollection addressCollection = ((AddressCollectionAdapter) collection).getCollection();
            AddressCollection nextPage = addressService.getNextPage(addressCollection, pageSize);
            return nextPage != null ? new AddressCollectionAdapter(nextPage) : null;
        } catch (EasyPostException e) {
            throw new EndOfPaginationException(e.getMessage(), e);
        }
    }

    @Override
    public IAddress createAndVerify(Map<String, Object> params) throws TugboatException {
        try {
            Address address = addressService.createAndVerify(params);
            return address != null ? new AddressAdapter(address) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IAddress verify(String id) throws TugboatException {
        try {
            Address address = addressService.verify(id);
            return address != null ? new AddressAdapter(address) : null;
        } catch (EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    public AddressService getAddressService() {
        return addressService;
    }
}
