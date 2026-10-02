// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.xo;

import io.micronaut.context.annotation.Requires;
import io.micronaut.jms.annotations.JMSListener;
import io.micronaut.jms.annotations.Queue;
import io.micronaut.messaging.annotation.MessageBody;
import io.micronaut.messaging.annotation.MessageHeader;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import com.uncommongoods.tugboat.engine.exception.TugboatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.micronaut.jms.sqs.configuration.SqsConfiguration.CONNECTION_FACTORY_BEAN_NAME;

@Requires(property = "micronaut.jms.sqs.enabled", value = "true", defaultValue = "true")
@JMSListener(CONNECTION_FACTORY_BEAN_NAME)
@Singleton
public class TugboatMsgConsumer {
    private static final Logger logger = LoggerFactory.getLogger(TugboatMsgConsumer.class);

    @Inject
    private TugboatService tugboatService;

    @Queue("${tugboat.queue}")
    public void receive(@MessageBody String shipmentId,
                       @MessageHeader("task") String task,
                       @MessageHeader("id") String messageId) {

        logger.info("received message {} for shipment {} with task {}", messageId, shipmentId, task);

        try {
            processMessage(shipmentId, task, messageId);
            logger.info("successfully processed message {} for shipment {}", messageId, shipmentId);
        } catch (Exception e) {
            logger.error("error processing message {} for shipment {} -- {}", messageId, shipmentId, e.getMessage(), e);
            throw e;
        }
    }

    private void processMessage(String shipmentId, String task, String messageId) {
        logger.info("processing task {} for shipment {}", task, shipmentId);

        try {
            switch (task) {
                case "initialize":
                    tugboatService.initializeTugboat(shipmentId);
                    break;
                case "rate":
                    tugboatService.rateTugboat(shipmentId);
                    break;
                case "re-rate":
                    tugboatService.rerateTugboat(shipmentId);
                    break;
                case "shop":
                    tugboatService.shopTugboat(shipmentId);
                    break;
                case "purchase":
                    tugboatService.purchaseTugboat(shipmentId);
                    break;
                case "void":
                    tugboatService.voidTugboat(shipmentId);
                    break;
                default:
                    logger.warn("unknown task type {} for shipment {}", task, shipmentId);
            }
        } catch (TugboatException e) {
            logger.error("tugboat exception processing task {} for shipment {} - {}", task, shipmentId, e.getMessage(), e);
            throw new RuntimeException("Failed to process tugboat task", e);
        }
    }
}
