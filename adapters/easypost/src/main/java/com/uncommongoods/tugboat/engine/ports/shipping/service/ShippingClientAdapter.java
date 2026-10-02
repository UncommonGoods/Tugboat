package com.uncommongoods.tugboat.engine.ports.shipping.service;

import java.util.function.Function;

import com.easypost.exception.General.MissingParameterError;
import com.easypost.service.EasyPostClient;
import com.uncommongoods.tugboat.engine.ports.shipping.model.RequestHookResponsesAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.ResponseHookResponsesAdapter;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IRequestHookResponses;
import com.uncommongoods.tugboat.engine.ports.shipping.model.IResponseHookResponses;

public class ShippingClientAdapter implements IShippingClient {
    private final EasyPostClient client;
    private final IAddressService addressService;
    private final IBatchService batchService;
    private final ICarrierAccountService carrierAccountService;
    private final IOrderService orderService;
    private final IParcelService parcelService;
    private final IRateService rateService;
    private final IRefundService refundService;
    private final IScanFormService scanFormService;
    private final IShipmentService shipmentService;
    private final ITrackerService trackerService;


    private ShippingClientAdapter(Builder builder) throws MissingParameterError {
        this.client = createEasyPostClient(builder);
        this.addressService = new AddressServiceAdapter(this.client);
        this.batchService = new BatchServiceAdapter(this.client);
        this.carrierAccountService = new CarrierAccountServiceAdapter(this.client);
        this.parcelService = new ParcelServiceAdapter(this.client);
        this.orderService = new OrderServiceAdapter(this.client);
        this.rateService = new RateServiceAdapter(this.client);
        this.refundService = new RefundServiceAdapter(this.client);
        this.scanFormService = new ScanFormServiceAdapter(this.client);
        this.shipmentService = new ShipmentServiceAdapter(this.client);
        this.trackerService = new TrackerServiceAdapter(this.client);
    }

    public ShippingClientAdapter(EasyPostClient client) {
        this.client = client;
        this.addressService = new AddressServiceAdapter(this.client);
        this.batchService = new BatchServiceAdapter(this.client);
        this.carrierAccountService = new CarrierAccountServiceAdapter(this.client);
        this.parcelService = new ParcelServiceAdapter(this.client);
        this.orderService = new OrderServiceAdapter(this.client);
        this.rateService = new RateServiceAdapter(this.client);
        this.refundService = new RefundServiceAdapter(this.client);
        this.scanFormService = new ScanFormServiceAdapter(this.client);
        this.shipmentService = new ShipmentServiceAdapter(this.client);
        this.trackerService = new TrackerServiceAdapter(this.client);
    }

    private EasyPostClient createEasyPostClient(Builder builder) throws MissingParameterError {
        if (builder.connectTimeoutMilliseconds != null && builder.readTimeoutMilliseconds != null && builder.apiBase != null) {
            return new EasyPostClient(builder.apiKey, builder.connectTimeoutMilliseconds, builder.readTimeoutMilliseconds, builder.apiBase);
        } else if (builder.apiBase != null) {
            return new EasyPostClient(builder.apiKey, builder.apiBase);
        } else {
            return new EasyPostClient(builder.apiKey);
        }
    }

    public ShippingClientAdapter(String apiKey) throws MissingParameterError {
        this(new EasyPostClient(apiKey));
    }

    public ShippingClientAdapter(String apiKey, String apiBase) throws MissingParameterError {
        this(new EasyPostClient(apiKey, apiBase));
    }

    public ShippingClientAdapter(String apiKey, String apiBase, int connectTimeoutMilliseconds) throws MissingParameterError {
        this(new EasyPostClient(apiKey, apiBase));
    }

    public ShippingClientAdapter(String apiKey, String apiBase, int connectTimeoutMilliseconds, int readTimeoutMilliseconds) throws MissingParameterError {
        this(new EasyPostClient(apiKey, apiBase));
    }

    public ShippingClientAdapter(String apiKey, int connectTimeoutMilliseconds,
                                 int readTimeoutMilliseconds, String apiBase) throws MissingParameterError {
        this(new EasyPostClient(apiKey, connectTimeoutMilliseconds, readTimeoutMilliseconds, apiBase));
    }

    public static class Builder {
        private final String apiKey;
        private String apiBase;
        private Integer connectTimeoutMilliseconds;
        private Integer readTimeoutMilliseconds;

        public Builder(String apiKey) {
            this.apiKey = apiKey;
        }

        public Builder apiBase(String apiBase) {
            this.apiBase = apiBase;
            return this;
        }

        public Builder connectTimeoutMilliseconds(int connectTimeoutMilliseconds) {
            this.connectTimeoutMilliseconds = connectTimeoutMilliseconds;
            return this;
        }

        public Builder readTimeoutMilliseconds(int readTimeoutMilliseconds) {
            this.readTimeoutMilliseconds = readTimeoutMilliseconds;
            return this;
        }

        public ShippingClientAdapter build() throws MissingParameterError {
            return new ShippingClientAdapter(this);
        }
    }

    public static Builder builder(String apiKey) {
        return new Builder(apiKey);
    }

    public EasyPostClient getClient() {
        return client;
    }

    @Override
    public IAddressService getAddressService() {
        return addressService;
    }

    @Override
    public IBatchService getBatchService() {
        return batchService;
    }

    @Override
    public IParcelService getParcelService() {
        return parcelService;
    }

    @Override
    public IRateService getRateService() {
        return rateService;
    }

    @Override
    public IShipmentService getShipmentService() {
        return shipmentService;
    }

    @Override
    public ICarrierAccountService getCarrierAccountService() {
        return carrierAccountService;
    }

    @Override
    public IOrderService getOrderService() {
        return orderService;
    }

    @Override
    public IRefundService getRefundService() {
        return refundService;
    }

    @Override
    public IScanFormService getScanFormService() {
        return scanFormService;
    }

    @Override
    public ITrackerService getTrackerService() {
        return trackerService;
    }

    @Override
    public void subscribeToRequestHook(Function<IRequestHookResponses, Object> function) {
        client.subscribeToRequestHook(requestHookResponses -> {
            IRequestHookResponses adapter = new RequestHookResponsesAdapter(requestHookResponses);
            return function.apply(adapter);
        });
    }

    @Override
    public void unsubscribeFromRequestHook(Function<IRequestHookResponses, Object> function) {
        // This may not work perfectly due to thje Function wrapper differences
        client.unsubscribeFromRequestHook(requestHookResponses -> {
            IRequestHookResponses adapter = new RequestHookResponsesAdapter(requestHookResponses);
            return function.apply(adapter);
        });
    }

    @Override
    public void subscribeToResponseHook(Function<IResponseHookResponses, Object> function) {
        client.subscribeToResponseHook(responseHookResponses -> {
            IResponseHookResponses adapter = new ResponseHookResponsesAdapter(responseHookResponses);
            return function.apply(adapter);
        });
    }

    @Override
    public void unsubscribeFromResponseHook(Function<IResponseHookResponses, Object> function) {
        client.unsubscribeFromResponseHook(responseHookResponses -> {
            IResponseHookResponses adapter = new ResponseHookResponsesAdapter(responseHookResponses);
            return function.apply(adapter);
        });
    }

    @Override
    public int getConnectionTimeoutMilliseconds() {
        return client.getConnectionTimeoutMilliseconds();
    }

    @Override
    public int getReadTimeoutMilliseconds() {
        return client.getReadTimeoutMilliseconds();
    }

    @Override
    public String getApiKey() {
        return client.getApiKey();
    }

    @Override
    public String getApiVersion() {
        return client.getApiVersion();
    }

    @Override
    public String getApiBase() {
        return client.getApiBase();
    }
}
