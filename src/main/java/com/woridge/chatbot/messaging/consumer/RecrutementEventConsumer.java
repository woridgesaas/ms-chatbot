package com.woridge.chatbot.messaging.consumer;

import com.rabbitmq.client.Channel;
import com.woridge.chatbot.config.RabbitMQConfig;
import com.woridge.chatbot.messaging.event.*;
import com.woridge.chatbot.model.StatsRecrutement;
import com.woridge.chatbot.repository
        .StatsRecrutementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation
        .RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler
        .annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecrutementEventConsumer {

    private final StatsRecrutementRepository
            repository;

    @RabbitListener(
            queues = RabbitMQConfig
                    .CHATBOT_JOB_OFFER_QUEUE
    )
    public void onOffrePubliee(
            OffrePublieeEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG)
            long tag)
            throws IOException {
        try {
            log.info("OffrePubliee reçue");

            LocalDate mois = LocalDate.now()
                    .withDayOfMonth(1);
            StatsRecrutement stats =
                    getOrCreate(mois);

            stats.setNbOffresPubliees(
                    stats.getNbOffresPubliees() + 1
            );
            stats.setNbOffresActives(
                    stats.getNbOffresActives() + 1
            );
            repository.save(stats);

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error(
                    "Erreur OffrePubliee : {}",
                    e.getMessage()
            );
            channel.basicNack(tag, false, true);
        }
    }

    @RabbitListener(
            queues = RabbitMQConfig
                    .CHATBOT_JOB_OFFER_QUEUE
    )
    public void onCandidatureRecue(
            CandidatureRecueEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG)
            long tag)
            throws IOException {
        try {
            log.info("CandidatureRecue reçue");

            LocalDate mois = LocalDate.now()
                    .withDayOfMonth(1);
            StatsRecrutement stats =
                    getOrCreate(mois);

            stats.setNbCandidatures(
                    stats.getNbCandidatures() + 1
            );
            stats.setNbCandidaturesEnAttente(
                    stats.getNbCandidaturesEnAttente() + 1
            );
            repository.save(stats);

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error(
                    "Erreur CandidatureRecue : {}",
                    e.getMessage()
            );
            channel.basicNack(tag, false, true);
        }
    }

    @RabbitListener(
            queues = RabbitMQConfig
                    .CHATBOT_INTERVIEW_QUEUE
    )
    public void onEntretien(
            EntretienEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG)
            long tag)
            throws IOException {
        try {
            log.info(
                    "Entretien {}",
                    event.getType()
            );

            LocalDate mois = LocalDate.now()
                    .withDayOfMonth(1);
            StatsRecrutement stats =
                    getOrCreate(mois);

            if ("PLANIFIE".equals(
                    event.getType()
            )) {
                stats.setNbEntretiensPlanifies(
                        stats.getNbEntretiensPlanifies() + 1
                );
            } else if ("EFFECTUE".equals(
                    event.getType()
            )) {
                stats.setNbEntretiensEffectues(
                        stats.getNbEntretiensEffectues() + 1
                );
            }

            repository.save(stats);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error(
                    "Erreur Entretien : {}",
                    e.getMessage()
            );
            channel.basicNack(tag, false, true);
        }
    }

    private StatsRecrutement getOrCreate(
            LocalDate mois) {
        return repository
                .findByMois(mois)
                .orElse(StatsRecrutement.builder()
                        .mois(mois)
                        .build());
    }
}