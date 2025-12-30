import axiosInstance from "@/config/axiosConfig";
import { message } from 'ant-design-vue';

const handlePayOSPayment = async (orderData) => {
    try {
        // Gọi API backend để tạo đơn hàng và lấy thông tin thanh toán PayOS
        const response = await axiosInstance.post('/order/create', orderData);
        console.log('Payment Response:', response.data);

        // Kiểm tra response data
        if (!response.data || !response.data.data) {
            throw new Error('Phản hồi từ server không hợp lệ');
        }

        const checkoutUrl = response.data.data.checkoutUrl;
        if (!checkoutUrl) {
            throw new Error('Không nhận được link thanh toán từ server');
        }

        localStorage.setItem('paymentResponse', JSON.stringify(response.data));
        window.location.href = checkoutUrl;
        return response.data;
    } catch (error) {
        console.error('Lỗi khi tạo thanh toán PayOS:', error);
        const errorMessage = error.response?.data?.message || error.message || 'Không thể tạo thanh toán PayOS. Vui lòng thử lại sau.';
        message.error(errorMessage);
        throw error; // Throw để proceedToPayment có thể catch
    }
};

const handleZaloPayPayment = async (idHoaDon, tongThanhToan) => {
    try {
        // ✅ TRUYỀN TỔNG TIỀN TỪ FE CHO BE
        // BE sẽ dùng số tiền này để thanh toán ZaloPay
        const response = await axiosInstance.post('/api/zalopay/create-order', null, {
            params: {
                idHoaDon,
                tongThanhToan  // ← TRUYỀN TỔNG TIỀN
            }
        });
        console.log('ZaloPay Response:', response.data);

        // Kiểm tra return_code
        if (!response.data || response.data.return_code !== 1) {
            const errorMsg = response.data?.return_message || 'Không thể tạo thanh toán ZaloPay';
            throw new Error(errorMsg);
        }

        const orderUrl = response.data.order_url;
        if (!orderUrl) {
            throw new Error('Không nhận được link thanh toán ZaloPay');
        }

        localStorage.setItem('zaloPayResponse', JSON.stringify(response.data));
        
        // ✅ THAY ĐỔI: Mở ZaloPay trong tab mới thay vì hard redirect
        window.open(orderUrl, '_blank');
        
        return response.data;
    } catch (error) {
        console.error('Lỗi khi tạo thanh toán ZaloPay:', error);
        const errorMessage = error.response?.data?.return_message || error.message || 'Không thể tạo thanh toán ZaloPay. Vui lòng thử lại sau.';
        message.error(errorMessage);
        throw error;
    }
};

const checkStatusPayment = async (orderCode) => {
    try {
        const response = await axiosInstance.get(`/payment/payment-status?orderId=${orderCode}`);
        return response.data;
    } catch (error) {
        console.error('Lỗi khi kiểm tra trạng thái thanh toán:', error);
    }
};

const checkZaloPayStatus = async (idHoaDon) => {
    try {
        const response = await axiosInstance.get('/api/zalopay/check-status', {
            params: { idHoaDon }
        });
        return response.data;
    } catch (error) {
        console.error('Lỗi khi kiểm tra trạng thái ZaloPay:', error);
        return null;
    }
};

// ✅ ENHANCED ZALOPAY HANDLER
export { handleZaloPayPayment, checkZaloPayStatus } from './enhancedZaloPayService.js';

// ✅ PAYOS HANDLER (unchanged)
export const handlePayOSPayment = async (orderData) => {
    try {
        // Gọi API backend để tạo đơn hàng và lấy thông tin thanh toán PayOS
        const response = await axiosInstance.post('/order/create', orderData);
        console.log('Payment Response:', response.data);

        // Kiểm tra response data
        if (!response.data || !response.data.data) {
            throw new Error('Phản hồi từ server không hợp lệ');
        }

        const checkoutUrl = response.data.data.checkoutUrl;
        if (!checkoutUrl) {
            throw new Error('Không nhận được link thanh toán từ server');
        }

        localStorage.setItem('paymentResponse', JSON.stringify(response.data));
        window.location.href = checkoutUrl;
        return response.data;
    } catch (error) {
        console.error('Lỗi khi tạo thanh toán PayOS:', error);
        const errorMessage = error.response?.data?.message || error.message || 'Không thể tạo thanh toán PayOS. Vui lòng thử lại sau.';
        message.error(errorMessage);
        throw error; // Throw để proceedToPayment có thể catch
    }
};

export const thanhToanService = {
    handlePayOSPayment,
    handleZaloPayPayment,
    checkStatusPayment,
    checkZaloPayStatus
}