
package com.woridge.chatbot.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.woridge.chatbot.enumeration.IntentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntentResult {

    private IntentType intent;
    private double confidence;
}