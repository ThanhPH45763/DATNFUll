package com.example.duanbe.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ZaloPayConfig {
    // Sandbox credentials (môi trường test)
    public static final String APP_ID = "554";
    public static final String KEY1 = "8NdU5pG5R2spGHGhyO99HN1OhD8IQJBn";
    public static final String KEY2 = "uUfsWgfLkRLzq6W2uNXTCxrfxs51auny";
    
    // Sandbox endpoints
    public static final String ENDPOINT_CREATE = "https://sb-openapi.zalopay.vn/v2/create";
    public static final String ENDPOINT_QUERY = "https://sb-openapi.zalopay.vn/v2/query";
    
    // Redirect URL sau khi thanh toán (không bắt buộc cho sandbox)
    public static final String REDIRECT_URL = "http://localhost:5173/admin/banhang";
}
