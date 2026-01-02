/**
 * Validation Rules và Error Messages
 * Centralized validation logic cho payment system
 */

// Helper function: Format currency
const formatCurrency = (value) => {
    if (!value && value !== 0) return '0';
    return Number(value).toLocaleString('vi-VN');
};

export const ERROR_MESSAGES = {
    REQUIRED: (fieldName) => `Vui lòng nhập ${fieldName}`,
    INVALID_NUMBER: 'Phải là số hợp lệ',
    MIN_VALUE: (fieldName, min) => `${fieldName} phải lớn hơn hoặc bằng ${formatCurrency(min)}đ`,
    PHONE_DIGITS_ONLY: 'Số điện thoại chỉ được chứa chữ số',
    PHONE_MAX_LENGTH: 'Số điện thoại không được vượt quá 15 chữ số',
    EMAIL_INVALID: 'Email không hợp lệ'
};

export const validationRules = {
    /**
     * Kiểm tra bắt buộc nhập
     */
    required: (value, fieldName) => {
        if (value === null || value === undefined || value.toString().trim() === '') {
            return ERROR_MESSAGES.REQUIRED(fieldName);
        }
        return null;
    },

    /**
     * Kiểm tra có phải số
     */
    isNumber: (value) => {
        if (value === '' || value === null || value === undefined) {
            return null; // Skip if empty (use required rule separately)
        }
        if (isNaN(Number(value))) {
            return ERROR_MESSAGES.INVALID_NUMBER;
        }
        return null;
    },

    /**
     * Kiểm tra giá trị tối thiểu
     */
    minValue: (value, min, fieldName) => {
        const numValue = Number(value);
        if (isNaN(numValue)) {
            return null; // Skip if not a number (use isNumber rule separately)
        }
        if (numValue < min) {
            return ERROR_MESSAGES.MIN_VALUE(fieldName, min);
        }
        return null;
    },

    /**
     * Kiểm tra số điện thoại
     */
    phone: (value) => {
        if (!value || value.trim() === '') {
            return null; // Skip if empty (use required rule separately)
        }

        // Chỉ cho phép chữ số
        if (!/^[0-9]+$/.test(value)) {
            return ERROR_MESSAGES.PHONE_DIGITS_ONLY;
        }

        // Độ dài tối đa 15
        if (value.length > 15) {
            return ERROR_MESSAGES.PHONE_MAX_LENGTH;
        }

        return null;
    },

    /**
     * Kiểm tra email (optional field)
     */
    email: (value) => {
        if (!value || value.trim() === '') {
            return null; // Email is optional
        }

        // Simple check: must contain @
        if (!value.includes('@')) {
            return ERROR_MESSAGES.EMAIL_INVALID;
        }

        return null;
    }
};

/**
 * Helper: Chạy nhiều validation rules và trả về lỗi đầu tiên
 */
export function validateField(...validationResults) {
    for (const result of validationResults) {
        if (result !== null) {
            return result;
        }
    }
    return null;
}
