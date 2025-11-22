package com.example.duanbe.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ZaloPayConfig {
    // Sandbox credentials (môi trường test)
    public static final String APP_ID = "2554";
    public static final String KEY1 = "sdngKKJmqEMzvh5QQcdD2A9XBSKUNaYn";
    public static final String KEY2 = "trMrHtvjo6myautxDUiAcYsVtaeQ8nhf";
    
    // Sandbox endpoints
    public static final String ENDPOINT_CREATE = "https://sb-openapi.zalopay.vn/v2/create";
    public static final String ENDPOINT_QUERY = "https://sb-openapi.zalopay.vn/v2/query";
    
    // Redirect URL sau khi thanh toán (không bắt buộc cho sandbox)
    public static final String REDIRECT_URL = "http://localhost:5173/admin/banhang";
}
