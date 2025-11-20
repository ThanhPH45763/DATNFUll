<template>
    <a-modal
        :visible="visible"
        title="Chọn phương thức thanh toán"
        :footer="null"
        :width="600"
        @cancel="handleCancel"
    >
        <!-- Step 1: Chọn phương thức thanh toán -->
        <div v-if="currentStep === 'select'" class="payment-selection">
            <h3 class="text-center mb-4">Vui lòng chọn phương thức thanh toán</h3>
            
            <div class="payment-options">
                <div 
                    class="payment-option-card" 
                    :class="{ 'selected': selectedMethod === 'payos' }"
                    @click="selectPaymentMethod('payos')"
                >
                    <div class="payment-icon">
                        <img src="../images/icon/logoVietQR.png" alt="PayOS" style="width: 60px; height: 60px;" />
                    </div>
                    <div class="payment-details">
                        <h4>PayOS (VietQR)</h4>
                        <p>Quét mã QR để thanh toán qua ngân hàng</p>
                    </div>
                    <div class="check-icon" v-if="selectedMethod === 'payos'">
                        <check-circle-filled style="color: #52c41a; font-size: 24px;" />
                    </div>
                </div>

                <div 
                    class="payment-option-card" 
                    :class="{ 'selected': selectedMethod === 'zalopay' }"
                    @click="selectPaymentMethod('zalopay')"
                >
                    <div class="payment-icon">
                        <img src="https://cdn.haitrieu.com/wp-content/uploads/2022/10/Logo-ZaloPay.png" 
                             alt="ZaloPay" style="width: 80px; height: 40px; object-fit: contain;" />
                    </div>
                    <div class="payment-details">
                        <h4>ZaloPay</h4>
                        <p>Thanh toán qua ví điện tử ZaloPay</p>
                    </div>
                    <div class="check-icon" v-if="selectedMethod === 'zalopay'">
                        <check-circle-filled style="color: #52c41a; font-size: 24px;" />
                    </div>
                </div>
            </div>

            <div class="modal-actions mt-4">
                <a-button @click="handleCancel" size="large">Hủy</a-button>
                <a-button 
                    type="primary" 
                    @click="confirmPaymentMethod" 
                    :disabled="!selectedMethod"
                    :loading="loading"
                    size="large"
                >
                    Tiếp tục
                </a-button>
            </div>
        </div>

        <!-- Step 2: Hiển thị QR Code -->
        <div v-if="currentStep === 'qr'" class="qr-display">
            <div class="qr-header text-center mb-4">
                <h3>Quét mã QR để thanh toán</h3>
                <p class="payment-method-name">
                    <img v-if="selectedMethod === 'payos'" 
                         src="../images/icon/logoVietQR.png" 
                         alt="PayOS" 
                         style="width: 40px; height: 40px; margin-right: 8px;" />
                    <img v-else 
                         src="https://cdn.haitrieu.com/wp-content/uploads/2022/10/Logo-ZaloPay.png" 
                         alt="ZaloPay" 
                         style="width: 60px; height: 30px; object-fit: contain; margin-right: 8px;" />
                    {{ selectedMethod === 'payos' ? 'PayOS' : 'ZaloPay' }}
                </p>
            </div>

            <div class="qr-code-container" v-if="qrCodeUrl">
                <img :src="qrCodeUrl" alt="QR Code" class="qr-code-image" />
                <p class="qr-description mt-3 text-center">
                    Mở ứng dụng {{ selectedMethod === 'payos' ? 'Ngân hàng' : 'ZaloPay' }} và quét mã QR này để thanh toán
                </p>
                <div class="payment-amount text-center mt-2">
                    <p class="amount-label">Số tiền thanh toán:</p>
                    <p class="amount-value">{{ formatCurrency(amount) }}</p>
                </div>
            </div>

            <div v-else class="text-center">
                <a-spin size="large" />
                <p class="mt-3">Đang tạo mã QR...</p>
            </div>

            <!-- Status Check -->
            <div class="payment-status mt-4" v-if="paymentStatus">
                <a-alert 
                    :message="paymentStatus.message" 
                    :type="paymentStatus.type"
                    show-icon
                />
            </div>

            <div class="modal-actions mt-4">
                <a-button @click="goBack" size="large">Quay lại</a-button>
                <a-button 
                    type="primary" 
                    @click="checkPaymentStatus" 
                    :loading="checking"
                    size="large"
                >
                    Kiểm tra thanh toán
                </a-button>
            </div>

            <p class="text-center mt-3 text-muted small">
                Hệ thống sẽ tự động kiểm tra trạng thái thanh toán
            </p>
        </div>
    </a-modal>
</template>

<script setup>
import { ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import { CheckCircleFilled } from '@ant-design/icons-vue';
import axiosInstance from '@/config/axiosConfig';

const props = defineProps({
    visible: Boolean,
    invoiceId: Number,
    amount: Number
});

const emit = defineEmits(['update:visible', 'payment-success', 'payment-cancelled']);

const currentStep = ref('select'); // 'select' hoặc 'qr'
const selectedMethod = ref(null);
const qrCodeUrl = ref(null);
const loading = ref(false);
const checking = ref(false);
const paymentStatus = ref(null);
let statusCheckInterval = null;

// Chọn phương thức thanh toán
const selectPaymentMethod = (method) => {
    selectedMethod.value = method;
};

// Xác nhận và tạo QR code
const confirmPaymentMethod = async () => {
    if (!selectedMethod.value) {
        message.warning('Vui lòng chọn phương thức thanh toán');
        return;
    }

    loading.value = true;
    try {
        const response = await axiosInstance.post('api/payment/create-qr', null, {
            params: {
                idHoaDon: props.invoiceId,
                paymentMethod: selectedMethod.value
            }
        });

        if (response.data.error) {
            message.error(response.data.message || 'Không thể tạo mã QR');
            return;
        }

        // Lấy QR code URL
        qrCodeUrl.value = response.data.qrUrl || response.data.orderUrl;
        
        // Chuyển sang bước hiển thị QR
        currentStep.value = 'qr';

        // Bắt đầu tự động kiểm tra trạng thái
        startStatusCheck();

        message.success('Tạo mã QR thành công');
    } catch (error) {
        console.error('Lỗi khi tạo QR:', error);
        message.error('Có lỗi xảy ra khi tạo mã QR');
    } finally {
        loading.value = false;
    }
};

// Kiểm tra trạng thái thanh toán
const checkPaymentStatus = async () => {
    checking.value = true;
    try {
        const response = await axiosInstance.get('api/payment/check-status', {
            params: {
                idHoaDon: props.invoiceId,
                paymentMethod: selectedMethod.value
            }
        });

        if (response.data.error) {
            paymentStatus.value = {
                type: 'error',
                message: response.data.message
            };
            return;
        }

        if (response.data.status === 'success') {
            paymentStatus.value = {
                type: 'success',
                message: 'Thanh toán thành công!'
            };
            
            // Dừng auto check
            stopStatusCheck();
            
            // Thông báo thành công
            setTimeout(() => {
                emit('payment-success', {
                    method: selectedMethod.value,
                    invoiceId: props.invoiceId
                });
                handleClose();
            }, 1500);
        } else if (response.data.status === 'cancelled') {
            paymentStatus.value = {
                type: 'error',
                message: 'Thanh toán đã bị hủy'
            };
            stopStatusCheck();
        } else {
            paymentStatus.value = {
                type: 'info',
                message: 'Chưa nhận được thanh toán. Vui lòng quét mã QR.'
            };
        }
    } catch (error) {
        console.error('Lỗi khi kiểm tra thanh toán:', error);
        paymentStatus.value = {
            type: 'error',
            message: 'Không thể kiểm tra trạng thái thanh toán'
        };
    } finally {
        checking.value = false;
    }
};

// Tự động kiểm tra trạng thái mỗi 3 giây
const startStatusCheck = () => {
    statusCheckInterval = setInterval(() => {
        checkPaymentStatus();
    }, 3000);
};

const stopStatusCheck = () => {
    if (statusCheckInterval) {
        clearInterval(statusCheckInterval);
        statusCheckInterval = null;
    }
};

// Quay lại bước chọn phương thức
const goBack = () => {
    stopStatusCheck();
    currentStep.value = 'select';
    qrCodeUrl.value = null;
    paymentStatus.value = null;
};

// Hủy modal
const handleCancel = () => {
    stopStatusCheck();
    emit('payment-cancelled');
    handleClose();
};

const handleClose = () => {
    emit('update:visible', false);
    // Reset sau khi đóng
    setTimeout(() => {
        currentStep.value = 'select';
        selectedMethod.value = null;
        qrCodeUrl.value = null;
        paymentStatus.value = null;
    }, 300);
};

// Format tiền
const formatCurrency = (value) => {
    return new Intl.NumberFormat('vi-VN', { 
        style: 'currency', 
        currency: 'VND' 
    }).format(value || 0);
};

// Dọn dẹp khi component bị unmount
watch(() => props.visible, (newVal) => {
    if (!newVal) {
        stopStatusCheck();
    }
});
</script>

<style scoped>
.payment-selection {
    padding: 20px 10px;
}

.payment-options {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.payment-option-card {
    display: flex;
    align-items: center;
    padding: 20px;
    border: 2px solid #e8e8e8;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
}

.payment-option-card:hover {
    border-color: #1890ff;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.payment-option-card.selected {
    border-color: #52c41a;
    background-color: #f6ffed;
}

.payment-icon {
    flex-shrink: 0;
    margin-right: 15px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.payment-details {
    flex: 1;
}

.payment-details h4 {
    margin: 0 0 5px 0;
    font-size: 16px;
    font-weight: 600;
    color: #262626;
}

.payment-details p {
    margin: 0;
    font-size: 14px;
    color: #8c8c8c;
}

.check-icon {
    position: absolute;
    right: 20px;
}

.modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}

.qr-display {
    padding: 20px 10px;
}

.qr-header h3 {
    margin: 0 0 10px 0;
    font-size: 20px;
    font-weight: 600;
    color: #262626;
}

.payment-method-name {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: #595959;
    margin: 0;
}

.qr-code-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px;
    background: #fafafa;
    border-radius: 8px;
}

.qr-code-image {
    max-width: 300px;
    width: 100%;
    height: auto;
    border: 3px solid #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.qr-description {
    font-size: 14px;
    color: #595959;
    max-width: 350px;
}

.payment-amount {
    margin-top: 15px;
    padding: 15px;
    background: #fff;
    border-radius: 8px;
    width: 100%;
}

.amount-label {
    margin: 0 0 5px 0;
    font-size: 14px;
    color: #8c8c8c;
}

.amount-value {
    margin: 0;
    font-size: 24px;
    font-weight: 700;
    color: #f5222d;
}

.payment-status {
    margin-top: 20px;
}

.text-muted {
    color: #8c8c8c;
}

.small {
    font-size: 12px;
}
</style>
