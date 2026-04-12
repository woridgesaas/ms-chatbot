package com.woridge.chatbot.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config
        .SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection
        .ConnectionFactory;
import org.springframework.amqp.rabbit.core
        .RabbitTemplate;
import org.springframework.amqp.support.converter
        .Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation
        .Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Exchanges woridge internes ──
    public static final String USERS_EXCHANGE =
            "woridge.users.exchange";
    public static final String ATTENDANCE_EXCHANGE =
            "woridge.attendance.exchange";
    public static final String RESIGNATION_EXCHANGE =
            "woridge.resignation.exchange";
    public static final String SALARY_EXCHANGE =
            "woridge.salary.exchange";
    public static final String JOB_OFFER_EXCHANGE =
            "woridge.joboffer.exchange";
    public static final String INTERVIEW_EXCHANGE =
            "woridge.interview.exchange";
    public static final String DOCUMENT_EXCHANGE =
            "woridge.document.exchange";

    // ── Exchange ms-users notification ──
    public static final String NOTIFICATION_EXCHANGE =
            "notification.exchange";

    // ── Queues internes ──
    public static final String CHATBOT_USERS_QUEUE =
            "chatbot.users.queue";
    public static final String CHATBOT_ATTENDANCE_QUEUE =
            "chatbot.attendance.queue";
    public static final String CHATBOT_RESIGNATION_QUEUE =
            "chatbot.resignation.queue";
    public static final String CHATBOT_SALARY_QUEUE =
            "chatbot.salary.queue";
    public static final String CHATBOT_JOB_OFFER_QUEUE =
            "chatbot.joboffer.queue";
    public static final String CHATBOT_INTERVIEW_QUEUE =
            "chatbot.interview.queue";
    public static final String CHATBOT_DOCUMENT_QUEUE =
            "chatbot.document.queue";

    // ── Queue chatbot sync ──
    public static final String
            CHATBOT_USER_CHATBOT_SYNC_QUEUE =
            "chatbot.user.chatbot.sync.queue";

    // ══════════════════════════════════════
    // EXCHANGES
    // ══════════════════════════════════════

    @Bean
    public FanoutExchange usersExchange() {
        return new FanoutExchange(
                USERS_EXCHANGE, true, false
        );
    }

    @Bean
    public FanoutExchange attendanceExchange() {
        return new FanoutExchange(
                ATTENDANCE_EXCHANGE, true, false
        );
    }

    @Bean
    public FanoutExchange resignationExchange() {
        return new FanoutExchange(
                RESIGNATION_EXCHANGE, true, false
        );
    }

    @Bean
    public FanoutExchange salaryExchange() {
        return new FanoutExchange(
                SALARY_EXCHANGE, true, false
        );
    }

    @Bean
    public FanoutExchange jobOfferExchange() {
        return new FanoutExchange(
                JOB_OFFER_EXCHANGE, true, false
        );
    }

    @Bean
    public FanoutExchange interviewExchange() {
        return new FanoutExchange(
                INTERVIEW_EXCHANGE, true, false
        );
    }

    @Bean
    public FanoutExchange documentExchange() {
        return new FanoutExchange(
                DOCUMENT_EXCHANGE, true, false
        );
    }

    // ── Exchange notification ms-users ──
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(
                NOTIFICATION_EXCHANGE, true, false
        );
    }

    // ══════════════════════════════════════
    // QUEUES
    // ══════════════════════════════════════

    @Bean
    public Queue chatbotUsersQueue() {
        return new Queue(CHATBOT_USERS_QUEUE, true);
    }

    @Bean
    public Queue chatbotAttendanceQueue() {
        return new Queue(
                CHATBOT_ATTENDANCE_QUEUE, true
        );
    }

    @Bean
    public Queue chatbotResignationQueue() {
        return new Queue(
                CHATBOT_RESIGNATION_QUEUE, true
        );
    }

    @Bean
    public Queue chatbotSalaryQueue() {
        return new Queue(CHATBOT_SALARY_QUEUE, true);
    }

    @Bean
    public Queue chatbotJobOfferQueue() {
        return new Queue(
                CHATBOT_JOB_OFFER_QUEUE, true
        );
    }

    @Bean
    public Queue chatbotInterviewQueue() {
        return new Queue(
                CHATBOT_INTERVIEW_QUEUE, true
        );
    }

    @Bean
    public Queue chatbotDocumentQueue() {
        return new Queue(
                CHATBOT_DOCUMENT_QUEUE, true
        );
    }

    @Bean
    public Queue userChatbotSyncQueue() {
        return QueueBuilder
                .durable(CHATBOT_USER_CHATBOT_SYNC_QUEUE)
                .build();
    }

    // ══════════════════════════════════════
    // BINDINGS
    // ══════════════════════════════════════

    @Bean
    public Binding bindUsers() {
        return BindingBuilder
                .bind(chatbotUsersQueue())
                .to(usersExchange());
    }

    @Bean
    public Binding bindAttendance() {
        return BindingBuilder
                .bind(chatbotAttendanceQueue())
                .to(attendanceExchange());
    }

    @Bean
    public Binding bindResignation() {
        return BindingBuilder
                .bind(chatbotResignationQueue())
                .to(resignationExchange());
    }

    @Bean
    public Binding bindSalary() {
        return BindingBuilder
                .bind(chatbotSalaryQueue())
                .to(salaryExchange());
    }

    @Bean
    public Binding bindJobOffer() {
        return BindingBuilder
                .bind(chatbotJobOfferQueue())
                .to(jobOfferExchange());
    }

    @Bean
    public Binding bindInterview() {
        return BindingBuilder
                .bind(chatbotInterviewQueue())
                .to(interviewExchange());
    }

    @Bean
    public Binding bindDocument() {
        return BindingBuilder
                .bind(chatbotDocumentQueue())
                .to(documentExchange());
    }

    // ── Binding chatbot sync ──
    @Bean
    public Binding userChatbotSyncBinding() {
        return BindingBuilder
                .bind(userChatbotSyncQueue())
                .to(notificationExchange())
                .with("user.chatbot.sync");
    }

    // ══════════════════════════════════════
    // INFRASTRUCTURE
    // ══════════════════════════════════════

    @Bean
    public Jackson2JsonMessageConverter
    messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory) {
        RabbitTemplate template =
                new RabbitTemplate(connectionFactory);
        template.setMessageConverter(
                messageConverter()
        );
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory
    rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAcknowledgeMode(
                AcknowledgeMode.MANUAL
        );
        return factory;
    }
}