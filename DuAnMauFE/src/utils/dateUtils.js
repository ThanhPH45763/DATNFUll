/**
 * ✅ DATE UTILITIES - Asia/Ho_Chi_Minh Timezone
 * 
 * Provides consistent date/time formatting across the application.
 * All functions automatically handle Vietnam timezone (UTC+7).
 * 
 * Dependencies: date-fns, date-fns-tz
 * 
 * @author System
 * @since 2026-01-03
 */

import { format, parseISO } from 'date-fns';
import { formatInTimeZone, toZonedTime } from 'date-fns-tz';
import { TIMEZONE, DATE_FORMATS } from '@/config/timezone';

/**
 * Format date/time for Vietnamese display
 * 
 * @param {string|Date|null} date - Date to format
 * @param {string} formatStr - Format pattern (defaults to DATETIME)
 * @returns {string} Formatted date string or 'N/A' if invalid
 * 
 * @example
 * formatDateTime('2026-01-03T19:30:00') // "03/01/2026 19:30:00"
 * formatDateTime(new Date(), DATE_FORMATS.DATE) // "03/01/2026"
 */
export const formatDateTime = (date, formatStr = DATE_FORMATS.DATETIME) => {
    if (!date) return 'N/A';

    try {
        // Handle string dates (from API)
        const dateObj = typeof date === 'string' ? parseISO(date) : date;

        // Format in Vietnam timezone
        return formatInTimeZone(dateObj, TIMEZONE, formatStr);
    } catch (error) {
        console.error('Error formatting date:', error, 'Input:', date);
        return 'Invalid Date';
    }
};

/**
 * Format date only (no time)
 * 
 * @param {string|Date|null} date - Date to format
 * @returns {string} Formatted date string (dd/MM/yyyy)
 * 
 * @example
 * formatDate('2026-01-03T19:30:00') // "03/01/2026"
 */
export const formatDate = (date) => {
    return formatDateTime(date, DATE_FORMATS.DATE);
};

/**
 * Format time only (no date)
 * 
 * @param {string|Date|null} date - Date to format
 * @returns {string} Formatted time string (HH:mm:ss)
 * 
 * @example
 * formatTime('2026-01-03T19:30:45') // "19:30:45"
 */
export const formatTime = (date) => {
    return formatDateTime(date, DATE_FORMATS.TIME);
};

/**
 * Format date/time for API requests (ISO format in VN timezone)
 * 
 * @param {string|Date|null} date - Date to format
 * @returns {string|null} ISO formatted string or null
 * 
 * @example
 * formatDateForAPI(new Date()) // "2026-01-03 19:30:00"
 */
export const formatDateForAPI = (date) => {
    if (!date) return null;

    try {
        const dateObj = typeof date === 'string' ? parseISO(date) : date;
        return formatInTimeZone(dateObj, TIMEZONE, DATE_FORMATS.ISO);
    } catch (error) {
        console.error('Error formatting date for API:', error);
        return null;
    }
};

/**
 * Get current date/time in Vietnam timezone
 * 
 * @returns {Date} Current Vietnam time
 * 
 * @example
 * const vnNow = getCurrentVNTime();
 */
export const getCurrentVNTime = () => {
    return toZonedTime(new Date(), TIMEZONE);
};

/**
 * Check if date is today (in Vietnam timezone)
 * 
 * @param {string|Date} date - Date to check
 * @returns {boolean} True if date is today
 * 
 * @example
 * isToday('2026-01-03T10:00:00') // true (if today is Jan 3, 2026)
 */
export const isToday = (date) => {
    if (!date) return false;

    try {
        const dateObj = typeof date === 'string' ? parseISO(date) : date;
        const vnDate = toZonedTime(dateObj, TIMEZONE);
        const vnNow = getCurrentVNTime();

        return vnDate.toDateString() === vnNow.toDateString();
    } catch (error) {
        return false;
    }
};

/**
 * Debug timezone information (for development)
 * 
 * @param {string|Date} date - Date to debug
 * 
 * @example
 * debugTimezone(new Date());
 * // Logs timezone info to console
 */
export const debugTimezone = (date) => {
    const dateObj = typeof date === 'string' ? parseISO(date) : date;

    console.group('🕐 Timezone Debug');
    console.log('Input:', date);
    console.log('Browser Time:', dateObj.toString());
    console.log('VN Time:', formatDateTime(dateObj));
    console.log('UTC:', dateObj.toISOString());
    console.log('Timezone:', TIMEZONE);
    console.groupEnd();
};

// Default export
export default {
    formatDateTime,
    formatDate,
    formatTime,
    formatDateForAPI,
    getCurrentVNTime,
    isToday,
    debugTimezone
};
