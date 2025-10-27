package com.email.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;
    private final String apiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder,
                                 @Value("${gemini.api.url}") String baseUrl,
                                 @Value("${gemini.api.key}") String geminiApiKey) {
        this.apiKey = geminiApiKey;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public String generateEmailReply(EmailRequest emailRequest) {

        //Build prompt
        String prompt = buildPrompt(emailRequest);
        //Prepare Raw JSON Body
        String requestBody = String.format("""
                {
                    "contents": [
                    {
                      "parts": [
                        {
                          "text": "%s"
                        }
                      ]
                    }
                  ]
                }""", prompt);
        //Send Request
        String respone = webClient.post().uri(uriBuilder ->
                        uriBuilder.path("v1beta/models/gemini-2.5-flash:generateContent").build())
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        //Extract Response
        return extractResponseContent(respone);
    }

    private String extractResponseContent(String respone) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(respone);
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert email writer. Craft a professional, detailed and context-aware reply to the email below. ");
        prompt.append("You are an expert email assistant. Write a natural, context-aware, and professional reply to the email below. ");
        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append("Maintain a ").append(emailRequest.getTone()).append(" tone throughout. ");
        }
        prompt.append("Focus on clarity, empathy, and maintaining a polite conversational flow. ");
        prompt.append("Do not include explanations about why the response works—only provide the final email reply.\n\n");
        prompt.append("Original email:\n").append(emailRequest.getEmailContent());


        return prompt.toString();
    }
}
