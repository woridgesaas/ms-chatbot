package com.woridge.chatbot.messaging.consumer;

import com.rabbitmq.client.Channel;
import com.woridge.chatbot.config.RabbitMQConfig;
import com.woridge.chatbot.messaging.event.*;
import com.woridge.chatbot.model.SnapshotEmploye;
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
public class EmployeeEventConsumer {

    private final SnapshotEmployeRepository repository;

    @RabbitListener(queues = RabbitMQConfig.CHATBOT_USERS_QUEUE)
    public void onUserCreated(
            UserCreatedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {
        try {
            log.info("UserCreated reçu : {} tenant: {}",
                    event.getUserId(), event.getTenant());

            SnapshotEmploye snapshot = repository
                    .findByUserIdAndTenant(
                            event.getUserId(), event.getTenant())
                    .orElse(new SnapshotEmploye());

            snapshot.setUserId(event.getUserId());
            snapshot.setNom(event.getNom());
            snapshot.setPrenom(event.getPrenom());
            snapshot.setEmail(event.getEmail());
            snapshot.setDepartement(event.getDepartement());
            snapshot.setPoste(event.getPoste());
            snapshot.setTenant(event.getTenant());

            repository.save(snapshot);
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("Erreur UserCreated : {}", e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.CHATBOT_USERS_QUEUE)
    public void onUserUpdated(
            UserUpdatedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {
        try {
            log.info("UserUpdated reçu : {}", event.getUserId());

            SnapshotEmploye snapshot = repository
                    .findByUserIdAndTenant(
                            event.getUserId(), event.getTenant())
                    .orElse(new SnapshotEmploye());

            snapshot.setUserId(event.getUserId());
            snapshot.setNom(event.getNom());
            snapshot.setPrenom(event.getPrenom());
            snapshot.setEmail(event.getEmail());
            snapshot.setDepartement(event.getDepartement());
            snapshot.setPoste(event.getPoste());
            snapshot.setTenant(event.getTenant());

            repository.save(snapshot);
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("Erreur UserUpdated : {}", e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.CHATBOT_USERS_QUEUE)
    public void onUserDeleted(
            UserDeletedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {
        try {
            log.info("UserDeleted reçu : {}", event.getUserId());
            repository.deleteById(event.getUserId());
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Erreur UserDeleted : {}", e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }
    @RabbitListener(
            queues = RabbitMQConfig
                    .CHATBOT_USER_CHATBOT_SYNC_QUEUE
    )
    public void onUserChatbotSync(
            UserChatbotSyncEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {

        try {
            log.info(
                    "UserChatbotSync recu — "
                            + "userId: {} tenant: {}",
                    event.getUserId(),
                    event.getTenant()
            );

            SnapshotEmploye snapshot = repository
                    .findByUserIdAndTenant(
                            event.getUserId(),
                            event.getTenant()
                    )
                    .orElse(new SnapshotEmploye());

            snapshot.setUserId(event.getUserId());
            snapshot.setNom(event.getNom());
            snapshot.setPrenom(event.getPrenom());
            snapshot.setEmail(event.getEmail());
            snapshot.setDepartement(
                    event.getDepartement()
            );
            snapshot.setPoste(event.getPoste());
            snapshot.setTenant(event.getTenant());

            // Solde congé 18 jours par défaut
            // seulement si c'est un nouvel employé
            if (snapshot.getSoldeConge() == null
                    || snapshot.getSoldeConge() == 0) {
                snapshot.setSoldeConge(18);
            }

            snapshot.setADemissionne(false);
            repository.save(snapshot);

            log.info(
                    "Snapshot sauvegarde — userId: {} "
                            + "tenant: {}",
                    event.getUserId(),
                    event.getTenant()
            );

            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error(
                    "Erreur UserChatbotSync : {}",
                    e.getMessage()
            );
            channel.basicNack(tag, false, true);
        }
    }
}