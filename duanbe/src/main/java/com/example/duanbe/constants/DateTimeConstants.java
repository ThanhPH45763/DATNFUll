package com.example.duanbe.constants;

import com.example.duanbe.config.TimezoneConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ✅ DATE TIME CONSTANTS: Centralized datetime patterns and utilities
 * 
 * Provides:
 * - Timezone constants
 * - Date formatters
 * - Helper method for LocalDateTime with Vietnam timezone
 * 
 * @author System
 * @since 2026-01-03
 */
public class DateTimeConstants {

    // ═══════════════════════════════════════════════════
    // TIMEZONE
    // ═══════════════════════════════════════════════════
    public static final String TIMEZONE_ID = "Asia/Ho_Chi_Minh";

    // ═══════════════════════════════════════════════════
    // FORMATTERS
    // ═══════════════════════════════════════════════════

    /**
     * ISO format for database storage: 2026-01-03 19:30:00
     */
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Date only format: 2026-01-03
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Vietnamese display format: 03/01/2026 19:30:00
     */
    public static final DateTimeFormatter DATETIME_DISPLAY_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Vietnamese date only: 03/01/2026
     */
    public static final DateTimeFormatter DATE_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ═══════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════

    /**
     * Get current LocalDateTime in Vietnam timezone
     * 
     * @return Current datetime in Asia/Ho_Chi_Minh timezone
     */
    public static LocalDateTime nowVN() {
        return LocalDateTime.now(TimezoneConfig.VIETNAM_ZONE);
    }

    /**
     * Format LocalDateTime for database storage
     * 
     * @param dateTime The datetime to format
     * @return Formatted string in yyyy-MM-dd HH:mm:ss
     */
    public static String formatForDB(LocalDateTime dateTime) {
        if (dateTime == null)
            return null;
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Format LocalDateTime for Vietnamese display
     * 
     * @param dateTime The datetime to format
     * @return Formatted string in dd/MM/yyyy HH:mm:ss
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null)
            return "N/A";
        return dateTime.format(DATETIME_DISPLAY_FORMATTER);
    }
}
