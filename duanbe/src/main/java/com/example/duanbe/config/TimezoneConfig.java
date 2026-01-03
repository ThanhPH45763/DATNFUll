package com.example.duanbe.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * ✅ TIMEZONE CONFIGURATION: Asia/Ho_Chi_Minh (UTC+7)
 * 
 * Sets JVM default timezone to Vietnam timezone.
 * All LocalDateTime.now() calls will automatically use this timezone.
 * 
 * @author System
 * @since 2026-01-03
 */
@Configuration
public class TimezoneConfig {

    public static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final TimeZone VIETNAM_TIMEZONE = TimeZone.getTimeZone(VIETNAM_ZONE);

    @PostConstruct
    public void init() {
        // Set JVM default timezone to Vietnam
        TimeZone.setDefault(VIETNAM_TIMEZONE);

        // Log confirmation
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("✅ Timezone configured: " + VIETNAM_TIMEZONE.getDisplayName());
        System.out.println("✅ Zone ID: " + VIETNAM_ZONE.getId());
        System.out.println("✅ Current offset: UTC" + VIETNAM_TIMEZONE.getRawOffset() / (1000 * 60 * 60));
        System.out.println("═══════════════════════════════════════════════════");
    }
}
