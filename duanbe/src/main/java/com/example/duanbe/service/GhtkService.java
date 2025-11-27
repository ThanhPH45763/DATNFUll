package com.example.duanbe.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class GhtkService {

    @Value("${ghtk.token}")
    private String ghtkToken;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String BASE_URL = "https://services.giaohangtietkiem.vn";

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghtkToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // 1. Tạo đơn hàng
    public ResponseEntity<String> createOrder(Map<String, Object> orderData) {
        String url = BASE_URL + "/services/shipment/order";
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(orderData, buildHeaders());
        return restTemplate.postForEntity(url, request, String.class);
    }

    // 2. Hủy đơn hàng
    public ResponseEntity<String> cancelOrder(String orderCode) {
        String url = BASE_URL + "/services/shipment/cancel/" + orderCode;
        HttpEntity<Void> request = new HttpEntity<>(buildHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    // 3. Tra cứu đơn hàng
    public ResponseEntity<String> getOrderInfo(String orderCode) {
        String url = BASE_URL + "/services/shipment/v2/" + orderCode;
        HttpEntity<Void> request = new HttpEntity<>(buildHeaders());
        return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
    }

    // 4. Tính phí vận chuyển
    public ResponseEntity<String> calculateFee(String pickProvince, String pickDistrict, String province,
            String district, int weight, int value) {
        try {
            String url = BASE_URL + "/services/shipment/fee";

            // Build URL with proper encoding for Vietnamese characters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("pick_province", pickProvince)
                    .queryParam("pick_district", pickDistrict)
                    .queryParam("province", province)
                    .queryParam("district", district)
                    .queryParam("weight", weight)
                    .queryParam("value", value);

            HttpEntity<Void> request = new HttpEntity<>(buildHeaders());
            // Use build().encode().toUri() to properly encode Vietnamese characters
            return restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, request, String.class);
        } catch (Exception e) {
            // Return error details for debugging
            String errorMessage = String.format("{\"success\":false,\"message\":\"%s\",\"error\":\"GHTK_API_ERROR\"}",
                    e.getMessage());
            return ResponseEntity.status(400).body(errorMessage);
        }
    }
}
