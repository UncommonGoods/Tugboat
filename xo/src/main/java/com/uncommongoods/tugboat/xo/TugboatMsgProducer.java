// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.xo;

import io.micronaut.jms.annotations.JMSProducer;
import io.micronaut.jms.annotations.Queue;
import io.micronaut.messaging.annotation.MessageBody;
import io.micronaut.messaging.annotation.MessageHeader;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static io.micronaut.jms.sqs.configuration.SqsConfiguration.CONNECTION_FACTORY_BEAN_NAME;

@Singleton
public class TugboatMsgProducer {
    private static final Logger logger = LoggerFactory.getLogger(TugboatMsgProducer.class);

    @JMSProducer(CONNECTION_FACTORY_BEAN_NAME)
    public interface SqsProducer {
        @Queue("${tugboat.queue}")
        void send(@MessageBody String shipmentId, @MessageHeader("task") String task, @MessageHeader("id") String messageId);
    }

    private final SqsProducer sqsProducer;

    public TugboatMsgProducer(SqsProducer sqsProducer) {
        this.sqsProducer = sqsProducer;
    }

    public void sendMessage(String shipmentId, String task) {
        String messageId = shipmentId + "-" + UUID.randomUUID();
        try {
            logger.info("sending task {} to queue for shipment {}", task, shipmentId);
            sqsProducer.send(shipmentId, task, messageId);
            logger.info("successfully sent task {} to queue for shipment {}", task, shipmentId);
        } catch (Exception e) {
            String errMsg = String.format("error sending shipment %s to consumer -- %s", shipmentId, e.getMessage());
            logger.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
    }
}
