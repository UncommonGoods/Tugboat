package com.uncommongoods.tugboat.engine.ports.shipping.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.easypost.model.ScanForm;
import com.easypost.model.ScanFormCollection;

public class ScanFormCollectionAdapter extends PaginatedCollectionAdapter<IScanForm, ScanForm, ScanFormCollection> implements IScanFormCollection {

    public ScanFormCollectionAdapter(ScanFormCollection collection) {
        super(collection);
    }

    @Override
    public List<IScanForm> getScanForms() {
        List<ScanForm> scanForms = collection.getScanForms();
        if (scanForms == null) {
            return null;
        }

        List<IScanForm> adaptedScanForms = new ArrayList<>();
        for (ScanForm scanForm : scanForms) {
            adaptedScanForms.add(new ScanFormAdapter(scanForm));
        }

        return adaptedScanForms;
    }

    @Override
    public Map<String, Object> buildNextPageParameters(List<IScanForm> entries, Integer pageSize) throws EndOfPaginationException {
        if (entries == null || entries.isEmpty()) {
            throw new EndOfPaginationException("No current entries to paginate from");
        }

        String lastId = entries.get(entries.size() - 1).getId();

        Map<String, Object> parameters = new java.util.HashMap<>();
        parameters.put("before_id", lastId);

        if (pageSize != null) {
            parameters.put("page_size", pageSize);
        }

        return parameters;
    }

    @Override
    protected List<ScanForm> convertInterfaceToConcreteList(List<IScanForm> interfaceEntries) {
        if (interfaceEntries == null) {
            return null;
        }

        List<ScanForm> concreteEntries = new ArrayList<>();
        for (IScanForm item : interfaceEntries) {
            if (item instanceof ScanFormAdapter) {
                concreteEntries.add(((ScanFormAdapter) item).getScanForm());
            } else {
                throw new IllegalArgumentException("ScanForm must be a ScanFormAdapter");
            }
        }

        return concreteEntries;
    }
}
