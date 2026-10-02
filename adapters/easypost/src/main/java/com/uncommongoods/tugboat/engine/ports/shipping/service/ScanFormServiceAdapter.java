package com.uncommongoods.tugboat.engine.ports.shipping.service;

import java.util.Map;

import com.easypost.exception.General.EndOfPaginationError;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.easypost.model.ScanForm;
import com.easypost.model.ScanFormCollection;
import com.easypost.service.EasyPostClient;
import com.easypost.service.ScanformService;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ScanFormAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ScanFormCollectionAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IScanForm;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IScanFormCollection;

public class ScanFormServiceAdapter implements IScanFormService {
    private final ScanformService scanFormService;

    public ScanFormServiceAdapter(EasyPostClient client) {
        this.scanFormService = client.scanForm;
    }

    public ScanFormServiceAdapter(ScanformService scanFormService) {
        this.scanFormService = scanFormService;
    }

    @Override
    public IScanForm create(Map<String, Object> params) throws TugboatException {
        try {
            ScanForm scanForm = scanFormService.create(params);
            return scanForm != null ? new ScanFormAdapter(scanForm) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IScanForm retrieve(String id) throws TugboatException {
        try {
            ScanForm scanForm = scanFormService.retrieve(id);
            return scanForm != null ? new ScanFormAdapter(scanForm) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IScanFormCollection all(Map<String, Object> params) throws TugboatException {
        try {
            ScanFormCollection scanFormCollection = scanFormService.all(params);
            return scanFormCollection != null ? new ScanFormCollectionAdapter(scanFormCollection) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IScanFormCollection getNextPage(IScanFormCollection collection) throws EndOfPaginationException {
        if (!(collection instanceof ScanFormCollectionAdapter)) {
            throw new IllegalArgumentException("Collection must be an ScanFormCollectionAdapter");
        }

        ScanFormCollection addressCollection = ((ScanFormCollectionAdapter) collection).getCollection();
        try {
            ScanFormCollection nextPage = scanFormService.getNextPage(addressCollection);
            return nextPage != null ? new ScanFormCollectionAdapter(nextPage) : null;
        } catch (EndOfPaginationError e) {
            throw new EndOfPaginationException(e.getMessage(), e);
        }
    }

    @Override
    public IScanFormCollection getNextPage(IScanFormCollection collection, Integer pageSize) throws EndOfPaginationException {
        if (!(collection instanceof ScanFormCollectionAdapter)) {
            throw new IllegalArgumentException("Collection must be an ScanFormCollectionAdapter");
        }

        ScanFormCollection addressCollection = ((ScanFormCollectionAdapter) collection).getCollection();
        try {
            ScanFormCollection nextPage = scanFormService.getNextPage(addressCollection, pageSize);
            return nextPage != null ? new ScanFormCollectionAdapter(nextPage) : null;
        } catch (EndOfPaginationError e) {
            throw new EndOfPaginationException(e.getMessage(), e);
        }
    }

    public ScanformService getScanFormService() {
        return scanFormService;
    }
}
