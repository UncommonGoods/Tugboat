package com.uncommongoods.tugboat.engine.ports.shipping.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.easypost.exception.General.EndOfPaginationError;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import com.uncommongoods.tugboat.engine.exception.EndOfPaginationException;
import com.easypost.model.Refund;
import com.easypost.model.RefundCollection;
import com.easypost.service.EasyPostClient;
import com.easypost.service.RefundService;
import com.uncommongoods.tugboat.engine.ports.shipping.model.RefundAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.RefundCollectionAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRefund;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRefundCollection;

public class RefundServiceAdapter implements IRefundService {
    private final RefundService refundService;

    public RefundServiceAdapter(EasyPostClient client) {
        this.refundService = client.refund;
    }

    public RefundServiceAdapter(RefundService refundService) {
        this.refundService = refundService;
    }

    @Override
    public List<IRefund> create(Map<String, Object> params) throws TugboatException {
        try {
            List<Refund> refunds = refundService.create(params);
            List<IRefund> adaptedRefunds = new ArrayList<>();

            for (Refund refund : refunds) {
                adaptedRefunds.add(new RefundAdapter(refund));
            }

            return adaptedRefunds;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IRefund retrieve(String id) throws TugboatException {
        try {
            Refund refund = refundService.retrieve(id);
            return refund != null ? new RefundAdapter(refund) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IRefundCollection all(Map<String, Object> params) throws TugboatException {
        try {
            RefundCollection collection = refundService.all(params);
            return collection != null ? new RefundCollectionAdapter(collection) : null;
        } catch (com.easypost.exception.EasyPostException e) {
            throw new TugboatException(e.getMessage(), e);
        }
    }

    @Override
    public IRefundCollection getNextPage(IRefundCollection collection) throws EndOfPaginationException {
        try {
            RefundCollection concrete = extractConcreteCollection(collection);
            RefundCollection result = refundService.getNextPage(concrete);
            return result != null ? new RefundCollectionAdapter(result) : null;
        } catch (EndOfPaginationError e) {
            throw new EndOfPaginationException(e.getMessage(), e);
        }
    }

    @Override
    public IRefundCollection getNextPage(IRefundCollection collection, Integer pageSize) throws EndOfPaginationException {
        try {
            RefundCollection concrete = extractConcreteCollection(collection);
            RefundCollection result = refundService.getNextPage(concrete, pageSize);
            return result != null ? new RefundCollectionAdapter(result) : null;
        } catch (EndOfPaginationError e) {
            throw new EndOfPaginationException(e.getMessage(), e);
        }
    }

    private RefundCollection extractConcreteCollection(IRefundCollection collection) {
        if (collection instanceof RefundCollectionAdapter) {
            return ((RefundCollectionAdapter) collection).getRefundCollection();
        }
        throw new IllegalArgumentException("Expected RefundCollectionAdapter, got " + collection.getClass().getSimpleName());
    }

    public RefundService getRefundService() {
        return refundService;
    }
}
