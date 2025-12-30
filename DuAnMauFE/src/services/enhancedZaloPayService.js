/**
 * Enhanced ZaloPay Payment Handler
 * Integrates with new state management and recovery system
 */

import { message } from 'ant-design-vue';
import { invoiceStorage } from '@/utils/invoiceStorage.js';
import { invoiceStateManager } from '@/stores/invoiceStateManager.js';
import { paymentState } from '@/utils/paymentState.js';
import { paymentPollingService } from '@/services/paymentPollingService.js';
import { thanhToanService } from '@/services/thanhToan.js';
import { banHangService } from '@/services/banHangService.js';

/**
 * Enhanced ZaloPay payment handler with new architecture
 */
export const handleZaloPayPayment = async (idHoaDon, tongThanhToan, options = {}) => {
    try {
        console.log('🚀 Starting enhanced ZaloPay payment:', { idHoaDon, tongThanhToan });
        
        // ✅ STEP 1: VALIDATE AND SYNC BEFORE PAYMENT
        console.log('🔄 Step 1: Validating and syncing invoice...');
        
        const syncResult = await invoiceStateManager.syncBeforePayment();
        
        if (syncResult.needsSync) {
            // Show difference dialog
            const userConfirmed = await showPriceDifferenceDialog(syncResult);
            
            if (!userConfirmed) {
                console.log('❌ User cancelled payment due to price difference');
                return { cancelled: true, reason: 'price_difference' };
            }
            
            // Sync with backend data
            await invoiceStateManager.setCurrentInvoice(syncResult.backendData, {
                source: 'payment_sync'
            });
            
            console.log('✅ Invoice synced with backend data');
        }

        // ✅ STEP 2: CREATE PAYMENT STATE
        console.log('💳 Step 2: Creating payment state...');
        
        const paymentStateInstance = paymentState.create(idHoaDon, tongThanhToan, 'ZaloPay', {
            ...options,
            invoiceData: invoiceStateManager.getCurrentInvoice()
        });
        
        // ✅ STEP 3: SAVE STATE TO STORAGE
        console.log('💾 Step 3: Saving payment state...');
        
        await invoiceStorage.savePaymentState(paymentStateInstance.getState());
        
        // ✅ STEP 4: CALL ZALOPAY API
        console.log('📡 Step 4: Calling ZaloPay API...');
        
        const response = await thanhToanService.handleZaloPayPayment(idHoaDon, tongThanhToan);
        
        if (!response || response.return_code !== 1) {
            throw new Error(response?.return_message || 'Không thể tạo thanh toán ZaloPay');
        }
        
        // ✅ STEP 5: UPDATE PAYMENT STATE WITH RESPONSE
        console.log('💳 Step 5: Updating payment state with ZaloPay response...');
        
        paymentStateInstance.updateWithZaloPayResponse(response);
        
        // Save updated state
        await invoiceStorage.savePaymentState(paymentStateInstance.getState());
        
        // ✅ STEP 6: OPEN ZALOPAY IN NEW TAB (NO HARD REDIRECT)
        console.log('🔗 Step 6: Opening ZaloPay in new tab...');
        
        window.open(response.order_url, '_blank');
        
        // ✅ STEP 7: START BACKGROUND POLLING
        console.log('🔄 Step 7: Starting background polling...');
        
        const pollingStarted = paymentPollingService.startPolling(idHoaDon, 'ZaloPay', {
            maxRetries: options.maxRetries || 20,
            retryDelay: options.retryDelay || 3000
        });
        
        if (!pollingStarted) {
            throw new Error('Failed to start payment polling');
        }
        
        // ✅ STEP 8: SETUP EVENT LISTENERS
        console.log('👂 Step 8: Setting up event listeners...');
        
        paymentPollingService.on('paymentSuccess', (data) => {
            console.log('✅ ZaloPay payment success:', data);
            
            // Show success message
            message.success({
                content: `Thanh toán ZaloPay thành công cho hóa đơn ${data.paymentId}!`,
                duration: 5
            });
            
            // Handle success in invoice manager
            await invoiceStateManager.refreshInvoice(idHoaDon);
            
            // Clear payment state
            await invoiceStorage.clearPaymentState();
            paymentState.reset();
        });
        
        paymentPollingService.on('paymentFailure', (data) => {
            console.log('❌ ZaloPay payment failure:', data);
            
            // Show error message
            message.error({
                content: `Thanh toán ZaloPay thất bại cho hóa đơn ${data.paymentId}`,
                duration: 5
            });
            
            // Clear payment state
            await invoiceStorage.clearPaymentState();
            paymentState.updateStatus('failed', data.status);
        });
        
        paymentPollingService.on('paymentTimeout', (data) => {
            console.log('⏰ ZaloPay payment timeout:', data);
            
            // Show timeout message
            message.warning({
                content: `Thanh toán ZaloPay hết hạn cho hóa đơn ${data.paymentId}. Vui lòng thử lại.`,
                duration: 5
            });
            
            // Clear payment state
            await invoiceStorage.clearPaymentState();
            paymentState.updateStatus('timeout');
        });
        
        console.log('✅ ZaloPay payment flow initiated successfully');
        
        return {
            success: true,
            paymentId: idHoaDon,
            amount: tongThanhToan,
            orderUrl: response.order_url,
            appTransId: response.app_trans_id,
            state: paymentStateInstance.getState()
        };
        
    } catch (error) {
        console.error('❌ Error in enhanced ZaloPay payment:', error);
        
        // Cleanup on error
        await invoiceStorage.clearPaymentState();
        paymentState.reset();
        paymentPollingService.stopPolling(idHoaDon);
        
        // Show error message
        message.error({
            content: 'Lỗi khi tạo thanh toán ZaloPay: ' + error.message,
            duration: 5
        });
        
        throw error;
    }
};

/**
 * Show price difference dialog
 */
const showPriceDifferenceDialog = (syncResult) => {
    return new Promise((resolve) => {
        Modal.confirm({
            title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
                h(ExclamationCircleOutlined, { style: 'color: #ff4d4f; font-size: 22px;' }),
                h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Phát hiện sự khác biệt về giá')
            ]),
            width: 600,
            content: () => h('div', { style: 'padding: 15px 0;' }, [
                h('p', { style: 'margin-bottom: 15px; font-size: 14px;' }, [
                    h('span', { style: 'color: #666;' }, 'Dữ liệu đã thay đổi trong lúc bạn thao tác. Giá trị hiện tại là:'),
                ]),
                h('div', { style: 'display: flex; justify-content: space-between; margin: 15px 0; padding: 10px; background: #f5f5f5; border-radius: 6px;' }, [
                    h('div', [
                        h('div', { style: 'font-weight: bold; color: #ff6600; margin-bottom: 5px;' }, 'Giao diện:'),
                        h('div', { style: 'color: #ff6600;' }, formatCurrency(syncResult.feTotal))
                    ]),
                    h('div', [
                        h('div', { style: 'font-weight: bold; color: #52c41a; margin-bottom: 5px;' }, 'Hệ thống:'),
                        h('div', { style: 'color: #52c41a;' }, formatCurrency(syncResult.dbTotal))
                    ])
                ]),
                h('p', { style: 'margin-top: 15px; color: #666; font-size: 13px;' }, [
                    h('span', { style: 'color: #ff6600;' }, 'Bạn có muốn tiếp tục với giá mới nhất từ hệ thống không?')
                ])
            ]),
            okText: 'Tiếp tục với giá hệ thống',
            cancelText: 'Hủy thanh toán',
            onOk: () => resolve(true),
            onCancel: () => resolve(false),
            centered: true,
            maskClosable: false,
            zIndex: 1000
        });
    });
};

/**
 * Check ZaloPay status with enhanced handling
 */
export const checkZaloPayStatus = async (idHoaDon) => {
    try {
        console.log('🔍 Checking ZaloPay status for:', idHoaDon);
        
        // Check if we have a pending payment state
        const pendingPayment = invoiceStorage.getPendingPayment();
        
        if (pendingPayment && pendingPayment.invoiceId === idHoaDon && pendingPayment.paymentMethod === 'ZaloPay') {
            console.log('📋 Found pending payment state');
            
            // Update payment state
            paymentState.import(pendingPayment);
            
            // Start polling if not already active
            if (!paymentPollingService.isPolling(idHoaDon)) {
                console.log('🔄 Starting polling from pending payment state');
                paymentPollingService.startPolling(idHoaDon, 'ZaloPay');
            }
            
            return {
                ...pendingPayment,
                hasPendingState: true
            };
        }
        
        // Otherwise call API normally
        const response = await thanhToanService.checkZaloPayStatus(idHoaDon);
        
        console.log('📡 ZaloPay status response:', response);
        
        return response;
        
    } catch (error) {
        console.error('❌ Error checking ZaloPay status:', error);
        return null;
    }
};

/**
 * Auto-recovery on page load
 */
export const autoRecoverZaloPayPayment = async () => {
    try {
        console.log('🔄 Starting auto-recovery for ZaloPay payment...');
        
        // Check for pending payment
        const pendingPayment = await recoveryService.checkPendingPayment();
        
        if (!pendingPayment) {
            console.log('✅ No pending payment to recover');
            return null;
        }
        
        if (pendingPayment.paymentMethod !== 'ZaloPay') {
            console.log('⚠️ Pending payment is not ZaloPay');
            return null;
        }
        
        if (pendingPayment.stale) {
            console.log('⚠️ Pending payment is stale, showing recovery dialog');
            
            // Show recovery dialog for stale payments
            const action = await showPaymentRecoveryDialog(pendingPayment);
            
            if (action === 'restart') {
                console.log('🔄 Restarting payment...');
                return await recoveryService.executeRecoveryAction('restart', pendingPayment);
            } else {
                console.log('❌ Cancelled stale payment');
                await recoveryService.executeRecoveryAction('cancel', pendingPayment);
                return null;
            }
        }
        
        // Auto-recover active payments
        console.log('🔄 Auto-recovering active payment...');
        
        const result = await recoveryService.resumePayment(pendingPayment);
        
        console.log('✅ Auto-recovery completed:', result);
        
        return result;
        
    } catch (error) {
        console.error('❌ Error in auto-recovery:', error);
        return null;
    }
};

/**
 * Show payment recovery dialog
 */
const showPaymentRecoveryDialog = (paymentState) => {
    return new Promise((resolve) => {
        const options = recoveryService.getRecoveryOptions(paymentState);
        
        Modal.confirm({
            title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
                h(ExclamationCircleOutlined, { style: 'color: #faad14; font-size: 22px;' }),
                h('span', { style: 'font-size: 16px; font-weight: 600;' }, 
                    paymentState.stale ? 'Phục hồi thanh toán hết hạn' : 'Phục hồi thanh toán gián đoạn')
            ]),
            width: 500,
            content: () => h('div', { style: 'padding: 15px 0;' }, [
                h('div', { style: 'margin-bottom: 15px;' }, [
                    h('strong', 'Phát hiện thanh toán chưa hoàn thành:')
                ]),
                
                h('div', { style: 'background: #f8f9fa; padding: 12px; border-radius: 6px; margin-bottom: 15px;' }, [
                    h('div', { style: 'display: flex; justify-content: space-between; margin-bottom: 8px;' }, [
                        h('span', 'Mã hóa đơn:'),
                        h('span', { style: 'font-weight: bold;' }, paymentState.invoiceId)
                    ]),
                    h('div', { style: 'display: flex; justify-content: space-between; margin-bottom: 8px;' }, [
                        h('span', 'Số tiền:'),
                        h('span', { style: 'font-weight: bold; color: #52c41a;' }, formatCurrency(paymentState.amount))
                    ]),
                    h('div', { style: 'display: flex; justify-content: space-between; margin-bottom: 8px;' }, [
                        h('span', 'Phương thức:'),
                        h('span', { style: 'font-weight: bold;' }, paymentState.paymentMethod)
                    ]),
                    h('div', { style: 'display: flex; justify-content: space-between; margin-bottom: 8px;' }, [
                        h('span', 'Trạng thái:'),
                        h('span', { style: 'font-weight: bold; color: #1890ff;' }, paymentState.statusText)
                    ]),
                    
                    paymentState.stale && h('div', { style: 'margin-top: 10px; padding: 10px; background: #fff2e8; border-radius: 4px;' }, [
                        h('span', { style: 'color: #fa8c16;' }, 
                            '⚠️ Thanh toán đã hết hạn (15 phút). Vui lòng thực hiện lại.')
                    ])
                ])
            ]),
            okText: 'Chọn hành động',
            onOk: () => {
                const selectedOption = options[0]?.key;
                console.log('User selected:', selectedOption);
                resolve(selectedOption);
            },
            onCancel: () => {
                console.log('User cancelled recovery');
                resolve(null);
            },
            centered: true,
            maskClosable: false,
            zIndex: 1000
        });
    });
};

/**
 * Format currency for display
 */
const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount || 0);
};