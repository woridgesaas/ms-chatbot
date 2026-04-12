package com.woridge.chatbot.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.woridge.chatbot.enumeration.DocType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentUploadedEvent {
    private String tenantId;
    private String fileUrl;
    private DocType docType;
    private String fileName;
}