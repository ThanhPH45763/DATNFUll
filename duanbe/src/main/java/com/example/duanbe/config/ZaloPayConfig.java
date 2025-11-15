package com.example.duanbe.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ZaloPayConfig {
    // Sandbox credentials (môi trường test)
    public static final String APP_ID = "2553";
    public static final String KEY1 = "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL";
    public static final String KEY2 = "kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz";
    
    // Sandbox endpoints
    public static final String ENDPOINT_CREATE = "https://sb-openapi.zalopay.vn/v2/create";
    public static final String ENDPOINT_QUERY = "https://sb-openapi.zalopay.vn/v2/query";
    
    // Callback và redirect URLs - cập nhật cho phù hợp
    public static final String CALLBACK_URL = "http://localhost:8080/api/zalopay/callback";
    public static final String REDIRECT_URL = "http://localhost:5173/payment-callback";
}
