package com.woridge.chatbot.messaging.consumer;

import com.rabbitmq.client.Channel;
import com.woridge.chatbot.config.RabbitMQConfig;
import com.woridge.chatbot.messaging.event.DocumentUploadedEvent;
import com.woridge.chatbot.service.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentEventConsumer {

    private final IngestionService ingestionService;

    @RabbitListener(queues = RabbitMQConfig.CHATBOT_DOCUMENT_QUEUE)
    public void onDocumentUploaded(
            DocumentUploadedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {
        try {
            log.info("DocumentUploaded reçu : {} tenant: {}",
                    event.getFileName(), event.getTenantId());

            ingestionService.ingestFromUrl(
                    event.getFileUrl(),
                    event.getDocType(),
                    event.getTenantId()
            );

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Erreur DocumentUploaded : {}",
                    e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }
}