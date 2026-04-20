package com.woridge.chatbot.messaging.consumer;

import com.rabbitmq.client.Channel;
import com.woridge.chatbot.config.RabbitMQConfig;
import com.woridge.chatbot.messaging.event
        .ResignationCreatedEvent;
import com.woridge.chatbot.repository
        .SnapshotEmployeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation
        .RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler
        .annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResignationEventConsumer {

    private final SnapshotEmployeRepository
            repository;

    @RabbitListener(
            queues = RabbitMQConfig
                    .CHATBOT_RESIGNATION_QUEUE
    )
    public void onResignationCreated(
            ResignationCreatedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG)
            long tag)
            throws IOException {
        try {
            log.info(
                    "ResignationCreated reçu : {}",
                    event.getUserId()
            );

            repository.findByUserId(
                            event.getUserId()
                    )
                    .ifPresent(snapshot -> {
                        snapshot.setADemissionne(
                                true
                        );
                        snapshot.setDateDemission(
                                event.getDateDemission()
                        );
                        repository.save(snapshot);
                    });

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error(
                    "Erreur ResignationCreated : {}",
                    e.getMessage()
            );
            channel.basicNack(tag, false, true);
        }
    }
}