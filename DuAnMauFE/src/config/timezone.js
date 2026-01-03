/**
 * ✅ TIMEZONE CONFIGURATION
 * 
 * Centralized timezone configuration for the entire frontend application.
 * All date/time operations should reference these constants.
 * 
 * @author System
 * @since 2026-01-03
 */

export const TIMEZONE = 'Asia/Ho_Chi_Minh';

export const DATE_FORMATS = {
    // Database/API format (ISO-like)
    ISO: 'yyyy-MM-dd HH:mm:ss',

    // Vietnamese display formats
    DATETIME: 'dd/MM/yyyy HH:mm:ss',
    DATE: 'dd/MM/yyyy',
    DATETIME_SHORT: 'dd/MM/yyyy HH:mm',
    TIME: 'HH:mm:ss',
    TIME_SHORT: 'HH:mm',

    // Alternative formats
    DATE_FULL: 'EEEE, dd MMMM yyyy', // e.g., "Thứ Sáu, 03 Tháng Một 2026"
    MONTH_YEAR: 'MM/yyyy'
};

export default {
    TIMEZONE,
    DATE_FORMATS
};
