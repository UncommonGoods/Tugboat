package com.uncommongoods.tugboat.engine.ports.shipping.model;

import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.easypost.model.Refund;
import com.easypost.model.RefundCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefundCollectionAdapter extends PaginatedCollectionAdapter<IRefund, Refund, RefundCollection> implements IRefundCollection {

    public RefundCollectionAdapter(RefundCollection collection) {
        super(collection);
    }

    @Override
    protected List<Refund> convertInterfaceToConcreteList(List<IRefund> interfaceEntries) {
        if (interfaceEntries == null) {
            return null;
        }

        List<Refund> concreteEntries = new ArrayList<>();
        for (IRefund item : interfaceEntries) {
            if (item instanceof RefundAdapter) {
                concreteEntries.add(((RefundAdapter) item).getRefund());
            } else {
                throw new IllegalArgumentException("Expected RefundAdapter, got " + item.getClass().getSimpleName());
            }
        }

        return concreteEntries;
    }

    protected List<IRefund> convertConcreteToInterfaceList(List<Refund> concreteEntries) {
        if (concreteEntries == null) {
            return null;
        }

        List<IRefund> interfaceEntries = new ArrayList<>();
        for (Refund item : concreteEntries) {
            interfaceEntries.add(new RefundAdapter(item));
        }

        return interfaceEntries;
    }

    @Override
    public Map<String, Object> buildNextPageParameters(List<IRefund> entries, Integer pageSize) throws EndOfPaginationException {
        if (entries == null || entries.isEmpty()) {
            throw new EndOfPaginationException("No current entries to paginate from");
        }

        String lastId = entries.get(entries.size() - 1).getId();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("before_id", lastId);

        if (pageSize != null) {
            parameters.put("page_size", pageSize);
        }

        return parameters;
    }

    public RefundCollection getRefundCollection() {
        return collection;
    }
}
