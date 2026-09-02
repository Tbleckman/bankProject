package service;

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
    
    private final HttpClient client = HttpClient.newHttpClient();

    public String analzyeTransaction(double amount, String transactionType, int recentTransactions) throws Exception {
        String json = String.format(
            " {\\\"amount\\\": %.2f, \\\"transaction_type\\\": \\\"%s\\\", \\\"recent_transactions\\\": %d} ",
            amount, 
            transactionType,
            recentTransactions
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ANALYZE_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = 
            client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}