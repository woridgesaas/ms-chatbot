package com.woridge.chatbot.messaging.consumer;

import com.rabbitmq.client.Channel;
import com.woridge.chatbot.config.RabbitMQConfig;
import com.woridge.chatbot.messaging.event.*;
import com.woridge.chatbot.model.StatsConge;
import com.woridge.chatbot.repository.SnapshotEmployeRepository;
import com.woridge.chatbot.repository.StatsCongeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CongeEventConsumer {

    private final StatsCongeRepository statsCongeRepository;
    private final SnapshotEmployeRepository snapshotRepository;

    @RabbitListener(queues = RabbitMQConfig.CHATBOT_ATTENDANCE_QUEUE)
    public void onCongeApprouve(
            CongeApprouveEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {
        try {
            log.info("CongeApprouve reçu : {} type: {}",
                    event.getEmployeId(), event.getTypeConge());

            LocalDate mois = LocalDate.now().withDayOfMonth(1);

            // Mettre à jour stats
            StatsConge stats = statsCongeRepository
                    .findByTenantAndMois(event.getTenant(), mois)
                    .orElse(StatsConge.builder()
                            .tenant(event.getTenant())
                            .mois(mois)
                            .build());

            stats.setNbEnConge(stats.getNbEnConge() + 1);

            // Incrémenter par type
            switch (event.getTypeConge().toUpperCase()) {
                case "ANNUEL"    -> stats.setNbCongeAnnuel(
                        stats.getNbCongeAnnuel() + 1);
                case "MALADIE"   -> stats.setNbCongeMaladie(
                        stats.getNbCongeMaladie() + 1);
                case "MATERNITE" -> stats.setNbCongeMaternite(
                        stats.getNbCongeMaternite() + 1);
            }

            statsCongeRepository.save(stats);

            // Mettre à jour solde employé
            snapshotRepository.findByUserIdAndTenant(
                            event.getEmployeId(), event.getTenant())
                    .ifPresent(snapshot -> {
                        snapshot.setSoldeConge(event.getSoldeRestant());
                        snapshotRepository.save(snapshot);
                    });

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Erreur CongeApprouve : {}", e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.CHATBOT_ATTENDANCE_QUEUE)
    public void onCongeTermine(
            CongeTermineEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag)
            throws IOException {
        try {
            log.info("CongeTermine reçu : {}", event.getEmployeId());

            LocalDate mois = LocalDate.now().withDayOfMonth(1);

            statsCongeRepository
                    .findByTenantAndMois(event.getTenant(), mois)
                    .ifPresent(stats -> {
                        int nb = stats.getNbEnConge() - 1;
                        stats.setNbEnConge(Math.max(0, nb));
                        statsCongeRepository.save(stats);
                    });

            // Mettre à jour solde employé
            snapshotRepository.findByUserIdAndTenant(
                            event.getEmployeId(), event.getTenant())
                    .ifPresent(snapshot -> {
                        snapshot.setSoldeConge(event.getSoldeRestant());
                        snapshotRepository.save(snapshot);
                    });

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Erreur CongeTermine : {}", e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }
}