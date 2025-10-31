<template>
    <div class="payment-callback-container">
        <div v-if="isLoading" class="loading-container">
            <a-spin size="large" />
            <p class="loading-text">Đang xử lý thanh toán...</p>
        </div>
        <div v-else class="payment-status-card">
            <div class="status-icon" :class="statusClass">
                <check-circle-outlined v-if="paymentStatus === 'PAID'" />
                <close-circle-outlined v-if="paymentStatus === 'CANCELLED'" />
                <loading-outlined v-if="paymentStatus === 'PENDING'" />
            </div>

            <h1 class="status-title">{{ statusTitle }}</h1>
            <p class="status-message">{{ statusMessage }}</p>

            <div class="order-info" v-if="paymentStatus === 'PAID'">
                <h3>Thông tin đơn hàng</h3>
                <div class="info-row">
                    <span class="label">Mã đơn hàng:</span>
                    <span class="value">{{ orderCode }}</span>
                </div>
                <div class="info-row">
                    <span class="label">Số tiền:</span>
                    <span class="value">{{ formatCurrency(orderAmount) }}</span>
                </div>
                <div class="info-row">
                    <span class="label">Thời gian:</span>
                    <span class="value">{{ formatDate(paymentTime) }}</span>
                </div>
            </div>

            <div class="action-buttons">
                <a-button type="primary" @click="goToOrderTracking" v-if="paymentStatus === 'PAID'">
                    Theo dõi đơn hàng
                </a-button>
                <a-button @click="goToHome">
                    Về trang chủ
                </a-button>
                <a-button type="primary" @click="tryAgain" v-if="paymentStatus === 'CANCELLED'">
                    Thử lại
                </a-button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { message, Spin } from 'ant-design-vue';
import {
    CheckCircleOutlined,
    CloseCircleOutlined,
    LoadingOutlined
} from '@ant-design/icons-vue';
import { banHangOnlineService } from '@/services/banHangOnlineService';
import { thanhToanService } from '@/services/thanhToan';
import { Store } from 'lucide-vue-next';
import { useGbStore } from '@/stores/gbStore';
const router = useRouter();
const route = useRoute();
const store = useGbStore();


// Khai báo các biến
const paymentStatus = ref('PENDING');
const orderCode = ref('');
const orderAmount = ref(0);
const paymentTime = ref(new Date());
const isLoading = ref(true);
const dataLoaded = ref(false);

// CSS class cho biểu tượng trạng thái
const statusClass = computed(() => {
    switch (paymentStatus.value) {
        case 'PAID': return 'success';
        case 'CANCELLED': return 'failed';
        case 'PENDING': return 'processing';
        default: return '';
    }
});

// Computed properties cho nội dung động
const statusTitle = computed(() => {
    switch (paymentStatus.value) {
        case 'PAID': return 'Thanh toán thành công!';
        case 'CANCELLED': return 'Thanh toán thất bại!';
        case 'PENDING': return 'Đang xử lý thanh toán...';
        default: return 'Trạng thái thanh toán';
    }
});

const statusMessage = computed(() => {
    switch (paymentStatus.value) {
        case 'PAID':
            return `Cảm ơn bạn đã đặt hàng. Chúng tôi đã nhận được thanh toán của bạn và đang xử lý đơn hàng. Email xác nhận đã được gửi đến địa chỉ email của bạn.`;
        case 'CANCELLED':
            return 'Có lỗi xảy ra trong quá trình thanh toán. Vui lòng thử lại hoặc chọn phương thức thanh toán khác.';
        case 'PENDING':
            return 'Hệ thống đang xử lý thanh toán của bạn. Vui lòng không đóng trang này.';
        default:
            return 'Đang kiểm tra trạng thái thanh toán của bạn.';
    }
});

// Format tiền tệ
const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
};

// Format ngày
const formatDate = (date) => {
    return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    }).format(date);
};

// Hàm điều hướng
const goToHome = () => {
    router.push('/home');
};

const goToOrderTracking = () => {
    router.push(`/tracuudonhang-banhang?code=${orderCode.value}`);
};

const tryAgain = () => {
    router.push('/thanhtoan-banhang');
};

// Lấy orderCode từ localStorage
const getOrderCodeFromLocalStorage = () => {
    try {
        const responeThanhToanStr = localStorage.getItem('responseThanhToan');
        if (responeThanhToanStr) {
            const responeThanhToan = JSON.parse(responeThanhToanStr);
            return responeThanhToan?.data?.orderCode;
        }
    } catch (error) {
        console.error('Lỗi khi lấy orderCode từ localStorage:', error);
    }
    return null;
};

// Kiểm tra dữ liệu hóa đơn đã có trong localStorage chưa
const checkInvoiceDataInLocalStorage = () => {
    const hoaDonStr = localStorage.getItem('hoaDon');
    const hoaDonChiTietStr = localStorage.getItem('hoaDonChiTiet');
    return (hoaDonStr && hoaDonChiTietStr);
};

// Tạo hóa đơn từ dữ liệu được lưu trong localStorage
const createOrderFromLocalStorage = async () => {
    try {
        const hoaDonStr = localStorage.getItem('hoaDon');
        const hoaDonChiTietStr = localStorage.getItem('hoaDonChiTiet');

        if (!hoaDonStr || !hoaDonChiTietStr) {
            console.error('Không tìm thấy dữ liệu hóa đơn trong localStorage');
            return null;
        }

        const hoaDon = JSON.parse(hoaDonStr);
        const hoaDonChiTiet = JSON.parse(hoaDonChiTietStr);

        console.log('Dữ liệu hóa đơn:', hoaDon);
        console.log('Dữ liệu hóa đơn chi tiết:', hoaDonChiTiet);

        // Gọi API tạo hóa đơn
        const response = await banHangOnlineService.createOrder(hoaDon);
        console.log('Kết quả tạo hóa đơn:', response);
        const isThanhToanMuaNgay = localStorage.getItem('isThanhToanMuaNgay') || null;
        // Gọi API tạo hóa đơn chi tiết
        let responseChiTiet;
        if (isThanhToanMuaNgay) {
            responseChiTiet = await banHangOnlineService.createOrderChiTietMuaNgay(hoaDonChiTiet);
            console.log('Kết quả tạo hóa đơn chi tiết mua ngay:', responseChiTiet);
        } else {
            responseChiTiet = await banHangOnlineService.createOrderChiTiet(hoaDonChiTiet);
            console.log('Kết quả tạo hóa đơn chi tiết:', responseChiTiet);
        }
        // const maHoaDon = localStorage.getItem('lastOrderCode');
        if (response && responseChiTiet) {
            // Lấy danh sách sản phẩm đã thanh toán
            const paidProducts = hoaDonChiTiet.map(item => {
                return {
                    id: item.chiTietSanPham.id_chi_tiet_san_pham
                };
            });

            console.log('Sản phẩm đã thanh toán:', paidProducts);

            // Lấy giỏ hàng hiện tại
            if (!isThanhToanMuaNgay) {
                const currentCart = JSON.parse(localStorage.getItem('gb-sport-cart') || '[]');
                console.log('Giỏ hàng hiện tại:', currentCart);

                // Lọc giỏ hàng, chỉ giữ lại những sản phẩm chưa thanh toán
                const updatedCart = currentCart.filter(cartItem => {
                    return !paidProducts.some(paidItem => paidItem.id === cartItem.id);
                });

                console.log('Giỏ hàng sau khi cập nhật:', updatedCart);

                if (updatedCart.length > 0) {
                    // Nếu còn sản phẩm trong giỏ hàng, cập nhật lại giỏ hàng
                    localStorage.setItem('gb-sport-cart', JSON.stringify(updatedCart));
                } else {
                    // Nếu không còn sản phẩm nào, xóa giỏ hàng
                    localStorage.removeItem('gb-sport-cart');
                }
            } else {
                store.setIsThanhToanMuaNgay(false);
            }
        }
        // Xóa dữ liệu từ localStorage sau khi đã tạo hóa đơn
        localStorage.removeItem('hoaDon');
        localStorage.removeItem('hoaDonChiTiet');
        localStorage.removeItem('isThanhToanMuaNgay');
        if (response && response.ma_hoa_don) {
            orderCode.value = response.ma_hoa_don;
            return response;
        }
    } catch (error) {
        console.error('Lỗi khi tạo hóa đơn:', error);
        message.error('Có lỗi xảy ra khi tạo hóa đơn. Vui lòng thử lại sau.');
    }
    return null;
};

// Kiểm tra trạng thái thanh toán
const checkPaymentStatus = async () => {
    try {
        isLoading.value = true;

        // Kiểm tra xem có dữ liệu hóa đơn trong localStorage không
        if (!checkInvoiceDataInLocalStorage()) {
            console.error('Không tìm thấy dữ liệu hóa đơn trong localStorage');
            paymentStatus.value = 'CANCELLED';
            message.error('Không tìm thấy thông tin đơn hàng. Vui lòng thử lại.');
            isLoading.value = false;
            return;
        }

        const orderCodeValue = getOrderCodeFromLocalStorage();

        if (!orderCodeValue) {
            console.error('Không tìm thấy mã đơn hàng');
            paymentStatus.value = 'CANCELLED';
            isLoading.value = false;
            return;
        }

        // Gọi API kiểm tra trạng thái thanh toán
        const result = await thanhToanService.checkStatusPayment(orderCodeValue);
        console.log('Kết quả kiểm tra trạng thái thanh toán:', result);

        if (result && result.error === 0) {
            paymentStatus.value = result.status;

            if (result.status === 'PAID') {
                // Nếu thanh toán thành công, tạo hóa đơn
                const orderResult = await createOrderFromLocalStorage();

                if (orderResult) {
                    // Lấy thông tin từ kết quả tạo hóa đơn
                    orderCode.value = orderResult.ma_hoa_don;
                    orderAmount.value = orderResult.tong_tien_sau_giam || 0;
                    message.success('Thanh toán thành công! Đơn hàng của bạn đã được xác nhận.');
                    dataLoaded.value = true;
                } else {
                    message.warning('Thanh toán thành công nhưng không thể tạo hóa đơn.');
                }
            } else if (result.status === 'CANCELLED') {
                message.error('Thanh toán đã bị hủy.');
            }
        } else {
            paymentStatus.value = 'CANCELLED';
            message.error(result?.message || 'Có lỗi xảy ra khi kiểm tra trạng thái thanh toán.');
        }
    } catch (error) {
        console.error('Lỗi khi kiểm tra trạng thái thanh toán:', error);
        paymentStatus.value = 'CANCELLED';
        message.error('Có lỗi xảy ra khi kiểm tra trạng thái thanh toán.');
    } finally {
        isLoading.value = false;
    }
};

onMounted(async () => {
    await checkPaymentStatus();
});
</script>

<style scoped>
.payment-callback-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 80vh;
    padding: 20px;
    background-color: #f5f5f5;
}

.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 300px;
}

.loading-text {
    margin-top: 16px;
    font-size: 16px;
    color: #666;
}

.payment-status-card {
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
    padding: 40px;
    text-align: center;
    max-width: 600px;
    width: 100%;
}

.status-icon {
    font-size: 72px;
    margin-bottom: 24px;
}

.status-icon.success {
    color: #52c41a;
}

.status-icon.failed {
    color: #ff4d4f;
}

.status-icon.processing {
    color: #1890ff;
}

.status-title {
    font-size: 28px;
    margin-bottom: 16px;
    color: #333;
}

.status-message {
    font-size: 16px;
    color: #666;
    margin-bottom: 32px;
}

.order-info {
    margin: 32px 0;
    text-align: left;
    background-color: #f9f9f9;
    padding: 20px;
    border-radius: 8px;
}

.order-info h3 {
    margin-bottom: 16px;
    color: #333;
}

.info-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
}

.label {
    color: #666;
    font-weight: 500;
}

.value {
    font-weight: 600;
    color: #333;
}

.action-buttons {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 32px;
}
</style>