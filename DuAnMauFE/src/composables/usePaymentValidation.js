/**
 * Payment Validation Composable
 * Handles all payment-related validation logic
 */

import { ref, computed } from 'vue';
import { validationRules, validateField } from '@/utils/validationRules';

export function usePaymentValidation() {
    // Reactive errors state
    const errors = ref({
        tien_khach_dua: null,
        ho_ten: null,
        sdt: null,
        dia_chi: null,
        email: null
    });

    /**
     * Validate cash payment (for both in-store and delivery)
     * @param {number|string} cashGiven - Money given by customer
     * @param {number} totalAmount - Total invoice amount
     */
    const validateCashPayment = (cashGiven, totalAmount) => {
        errors.value.tien_khach_dua = validateField(
            validationRules.required(cashGiven, 'số tiền khách đưa'),
            validationRules.isNumber(cashGiven),
            validationRules.minValue(cashGiven, totalAmount, 'Số tiền khách đưa')
        );
    };

    /**
     * Clear cash payment error
     */
    const clearCashError = () => {
        errors.value.tien_khach_dua = null;
    };

    /**
     * Validate delivery customer information
     * @param {Object} customerData - Customer information
     * @param {string} customerData.ho_ten - Customer name
     * @param {string} customerData.sdt - Phone number
     * @param {string} customerData.dia_chi - Delivery address
     * @param {string} customerData.email - Email (optional)
     */
    const validateDeliveryInfo = (customerData) => {
        errors.value.ho_ten = validationRules.required(customerData.ho_ten, 'tên khách hàng');

        errors.value.sdt = validateField(
            validationRules.required(customerData.sdt, 'số điện thoại'),
            validationRules.phone(customerData.sdt)
        );

        errors.value.dia_chi = validationRules.required(customerData.dia_chi, 'địa chỉ giao hàng');

        // Email is optional
        errors.value.email = validationRules.email(customerData.email);
    };

    /**
     * Clear all delivery info errors
     */
    const clearDeliveryErrors = () => {
        errors.value.ho_ten = null;
        errors.value.sdt = null;
        errors.value.dia_chi = null;
        errors.value.email = null;
    };

    /**
     * Check if payment can proceed (no errors)
     */
    const canProceedPayment = computed(() => {
        return !Object.values(errors.value).some(error => error !== null);
    });

    /**
     * Check if there are any errors
     */
    const hasErrors = computed(() => {
        return Object.values(errors.value).some(error => error !== null);
    });

    /**
     * Get all active errors
     */
    const activeErrors = computed(() => {
        return Object.entries(errors.value)
            .filter(([_, error]) => error !== null)
            .map(([field, error]) => ({ field, error }));
    });

    return {
        errors,
        validateCashPayment,
        clearCashError,
        validateDeliveryInfo,
        clearDeliveryErrors,
        canProceedPayment,
        hasErrors,
        activeErrors
    };
}
