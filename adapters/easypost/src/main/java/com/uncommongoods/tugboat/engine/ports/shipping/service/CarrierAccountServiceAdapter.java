package com.uncommongoods.tugboat.engine.ports.shipping.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.easypost.model.CarrierAccount;
import com.easypost.service.CarrierAccountService;
import com.easypost.service.EasyPostClient;
import com.uncommongoods.tugboat.engine.ports.shipping.model.CarrierAccountAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ICarrierAccount;

public class CarrierAccountServiceAdapter implements ICarrierAccountService {
    private final CarrierAccountService carrierAccountService;

    public CarrierAccountServiceAdapter(EasyPostClient client) {
        this.carrierAccountService = client.carrierAccount;
    }

    public CarrierAccountServiceAdapter(CarrierAccountService carrierAccountService) {
        this.carrierAccountService = carrierAccountService;
    }

    @Override
    public ICarrierAccount create(Map<String, Object> params) throws TugboatException {
        try {
            CarrierAccount account = carrierAccountService.create(params);
            return account != null ? new CarrierAccountAdapter(account) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public ICarrierAccount retrieve(String id) throws TugboatException {
        try {
            CarrierAccount account = carrierAccountService.retrieve(id);
            return account != null ? new CarrierAccountAdapter(account) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public List<ICarrierAccount> all() throws TugboatException {
        try {
            List<CarrierAccount> accounts = carrierAccountService.all();
            List<ICarrierAccount> adaptedAccounts = new ArrayList<>();

            for (CarrierAccount account : accounts) {
                adaptedAccounts.add(new CarrierAccountAdapter(account));
            }

            return adaptedAccounts;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public List<ICarrierAccount> all(Map<String, Object> params) throws TugboatException {
        try {
            List<CarrierAccount> accounts = carrierAccountService.all(params);
            List<ICarrierAccount> adaptedAccounts = new ArrayList<>();

            for (CarrierAccount account : accounts) {
                adaptedAccounts.add(new CarrierAccountAdapter(account));
            }

            return adaptedAccounts;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public ICarrierAccount update(String id, Map<String, Object> params) throws TugboatException {
        try {
            CarrierAccount account = carrierAccountService.update(id, params);
            return account != null ? new CarrierAccountAdapter(account) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) throws TugboatException {
        try {
            carrierAccountService.delete(id);
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    public CarrierAccountService getCarrierAccountService() {
        return carrierAccountService;
    }
}
