package com.woridge.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MsChatbotApplication {
	public static void main(String[] args) {
		SpringApplication.run(MsChatbotApplication.class, args);
	}
}