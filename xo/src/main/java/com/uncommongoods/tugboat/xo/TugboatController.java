// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.xo;

import com.easypost.exception.EasyPostException;
import com.easypost.model.Shipment;
import com.google.gson.JsonObject;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import com.uncommongoods.tugboat.engine.manifest.PickupFacility;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Inject;

import com.uncommongoods.tugboat.engine.exception.TugboatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller("${tugboat.api.path:/tugboat}")
public class TugboatController {
    private static final Logger logger = LoggerFactory.getLogger(TugboatController.class);

    @Inject
    private TugboatMsgProducer tugboatMsgProducer;

    @Inject
    private TugboatService tugboatService;

    @Get("/pickup-facility/{pickupFacilityCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> getManifestGroup(@PathVariable String pickupFacilityCode) throws TugboatException {
        PickupFacility pickupFacility = tugboatService.getPickupFacility(pickupFacilityCode);
        if (pickupFacility == null) {
            return HttpResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("pickup facilities require a cache client (set CACHE_URL)");
        }
        return HttpResponse.ok(pickupFacility.toString());
    }

    @Get("/pickup-group/{pickupFacilityCode}/{groupName}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> getManifestGroup(@PathVariable String pickupFacilityCode, @PathVariable String groupName) throws TugboatException {
        return HttpResponse.ok(tugboatService.getManifestGroup(pickupFacilityCode, groupName));
    }

    @Post("/tracker/{trackingCode}/{carrier}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> createTracker(@PathVariable String trackingCode,
                                              @PathVariable String carrier) {
        try {
            String trackerId = tugboatService.createTracker(trackingCode, carrier);
            return HttpResponse.ok(trackerId);

        } catch (Exception e) {
            String errorMsg = String.format("tracker error: %s", e.getMessage());
            logger.error(errorMsg, e);
            return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }

    @Get("/{shipmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> retrieve(@PathVariable String shipmentId) throws TugboatException {
        return HttpResponse.ok(tugboatService.getTugboat(shipmentId).toString());
    }

    @Post("/{shipmentId}/initialize")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> initialize(@PathVariable String shipmentId,
                                           @QueryValue @Nullable Boolean async) throws TugboatException {
        if (async != null && async) {
            return enqueueMessage(shipmentId, "initialize");
        }

        return HttpResponse.ok(tugboatService.initializeTugboat(shipmentId));
    }

    @Post("/{shipmentId}/rate")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> rate(@PathVariable String shipmentId,
                                     @QueryValue @Nullable Boolean async) throws TugboatException {
        if (async != null && async) {
            return enqueueMessage(shipmentId, "rate");
        }

        return HttpResponse.ok(tugboatService.rateTugboat(shipmentId));
    }

    @Post("/{shipmentId}/re-rate")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> rerate(@PathVariable String shipmentId,
                                       @QueryValue @Nullable Boolean async) throws TugboatException {
        if (async != null && async) {
            return enqueueMessage(shipmentId, "re-rate");
        }

        return HttpResponse.ok(tugboatService.rerateTugboat(shipmentId));
    }


    @Post("/{shipmentId}/shop")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> shop(@PathVariable String shipmentId,
                                     @QueryValue @Nullable Boolean async) throws TugboatException {
        if (async != null && async) {
            return enqueueMessage(shipmentId, "shop");
        }

        return HttpResponse.ok(tugboatService.shopTugboat(shipmentId));
    }

    @Post("/{shipmentId}/purchase")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> purchase(@PathVariable String shipmentId,
                                         @QueryValue @Nullable Boolean async) throws TugboatException {
        if (async != null && async) {
            return enqueueMessage(shipmentId, "purchase");
        }

        return HttpResponse.ok(tugboatService.purchaseTugboat(shipmentId));
    }

    @Post("/batch/purchase")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> batchPurchase(@Body List<String> shipmentIds) {
        try {
            for (String shipmentId : shipmentIds) {
                tugboatMsgProducer.sendMessage(shipmentId, "purchase");
            }

            String successMsg = String.format("task purchase has been enqueued for %d shipments", shipmentIds.size());
            return HttpResponse.ok(successMsg);

        } catch (Exception e) {
            String errMsg = String.format("error sending batch purchase to consumer -- %s", e.getMessage());
            logger.error(errMsg, e);
            return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errMsg);
        }
    }

    @Get("/{shipmentId}/print")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> print(@PathVariable String shipmentId) throws TugboatException {
        return HttpResponse.ok(tugboatService.printTugboat(shipmentId));
    }

    @Get("/{shipmentId}/re-print")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> reprint(@PathVariable String shipmentId) throws TugboatException {
        return HttpResponse.ok(tugboatService.reprintTugboat(shipmentId));
    }

    @Post("/{shipmentId}/void")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> voidLabel(@PathVariable String shipmentId,
                                          @QueryValue @Nullable Boolean async) throws TugboatException {
        if (async != null && async) {
            return enqueueMessage(shipmentId, "void");
        }

        return HttpResponse.ok(tugboatService.voidTugboat(shipmentId));
    }

    @Get("/return-label/{returnId}/{orderId}/{shipmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<String> getReturnLabel(@PathVariable String returnId,
                                               @PathVariable String orderId,
                                               @PathVariable String shipmentId) {
        try {
            Shipment returnShipment = tugboatService.getReturnLabel(returnId, orderId, shipmentId);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("trackingNumber", returnShipment.getTrackingCode());
            jsonObject.addProperty("shippingLabel", returnShipment.getPostageLabel().getLabelFile());
            jsonObject.addProperty("shippingLabelImageType", "image/png; base64");
            return HttpResponse.ok(jsonObject.toString());
        } catch (EasyPostException e) {
            logger.error("return-{}-{}-{} {}", returnId, orderId, shipmentId, e.getMessage(), e);
            return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("failed to generate label for return-" + returnId + "-" + orderId + "-" + shipmentId);
        }
    }

    public HttpResponse<String> enqueueMessage(String shipmentId, String task) {
        try {
            tugboatMsgProducer.sendMessage(shipmentId, task);

            String successMsg = String.format("task %s has been enqueued for shipment %s", task, shipmentId);
            return HttpResponse.ok(successMsg);

        } catch (Exception e) {
            String errMsg = String.format("error sending shipment %s to consumer -- %s", shipmentId, e.getMessage());
            logger.error(errMsg, e);
            return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errMsg);
        }
    }
}
