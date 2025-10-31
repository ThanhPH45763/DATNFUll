import axiosInstance from "@/config/axiosConfig";
import { message } from 'ant-design-vue';

const handlePayOSPayment = async (orderData) => {
    try {
        // Gọi API backend để tạo đơn hàng và lấy thông tin thanh toán PayOS
        const response = await axiosInstance.post('/order/create', orderData);
        localStorage.setItem('paymentResponse', JSON.stringify(response.data));
        window.location.href = response.data.data.checkoutUrl;
        console.log(response.data)
        return response.data;
    } catch (error) {
        console.error('Lỗi khi tạo thanh toán:', error);
        message.error(error.response?.data?.message || 'Không thể tạo thanh toán. Vui lòng thử lại sau.');
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

export const thanhToanService = {
    handlePayOSPayment,
    checkStatusPayment
}