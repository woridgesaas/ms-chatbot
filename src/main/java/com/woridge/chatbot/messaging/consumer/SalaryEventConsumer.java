package com.woridge.chatbot.messaging.consumer;

import com.rabbitmq.client.Channel;
import com.woridge.chatbot.config.RabbitMQConfig;
import com.woridge.chatbot.messaging.event.SalaireVerseEvent;
import com.woridge.chatbot.repository.SnapshotEmployeRepository;
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
public class SalaryEventConsumer {

    private final SnapshotEmployeRepository repository;

    @RabbitListener(queues = RabbitMQConfig.CHATBOT_SALARY_QUEUE)
    public void onSalaireVerse(
            SalaireVerseEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {
        try {
            log.info("SalaireVerse reçu : {} mois: {}",
                    event.getUserId(), event.getMois());

            repository.findByUserIdAndTenant(
                            event.getUserId(), event.getTenant())
                    .ifPresent(snapshot -> {
                        snapshot.setSalaireActuel(event.getMontant());
                        snapshot.setMoisSalaire(event.getMois());
                        repository.save(snapshot);
                    });

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Erreur SalaireVerse : {}", e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }
}