package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RiskServiceClient {
    private static final String RISK_SERVICE_HOST = 
        System.getenv().getOrDefault("RISK_SERVICE_HOST", "localhost");

    private static final String RISK_SERVICE_PORT = 
        System.getenv().getOrDefault("RISK_SERVICE_PORT", "8000");

    private static final String ANALYZE_URL = 
        "http://" + RISK_SERVICE_HOST + ":" + RISK_SERVICE_PORT + "/analyze";
    
    private final HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public RiskAnalysis analyzeTransaction(double amount, String transactionType, int recentTransactions) throws Exception {
        String json = String.format(
            "{\"amount\": %.2f, \"transaction_type\": \"%s\", \"recent_transactions\": %d}",
            amount, 
            transactionType,
            recentTransactions
        );

        /*
        System.out.println("ANALYZE_URL = " + ANALYZE_URL);
        System.out.println("JSON BODY = " + json);
        */

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ANALYZE_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        //System.out.println("METHOD = " + request.method());

        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());

        //System.out.println("HTTP STATUS = " + response.statusCode());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Risk service returned HTTP " + response.statusCode());
        }
        JsonNode responseJson = objectMapper.readTree(response.body());
        
        double riskScore = responseJson.get("risk_score").asDouble();
        boolean flagged = responseJson.get("flagged").asBoolean();
        String reason = responseJson.get("reason").asText();

        return new RiskAnalysis(riskScore, flagged, reason);
    }
}