/**
 * Recovery Service for POS
 * Handles payment recovery, state validation, and error recovery
 */

import { invoiceStorage } from './invoiceStorage.js';
import { paymentState } from './paymentState.js';
import { banHangService } from '@/services/banHangService.js';
import { thanhToanService } from '@/services/thanhToan.js';

export class RecoveryService {
  constructor() {
    this.RECOVERY_TIMEOUT = 30 * 1000; // 30 seconds for recovery operations
    this.MAX_RECOVERY_ATTEMPTS = 3;
  }

  /**
   * Check for pending payment recovery
   */
  async checkPendingPayment() {
    try {
      console.log('🔄 Checking for pending payment recovery...');
      
      // Get pending payment from storage
      const pendingPayment = invoiceStorage.getPendingPayment();
      
      if (!pendingPayment) {
        console.log('✅ No pending payment found');
        return null;
      }

      console.log('🔄 Found pending payment:', pendingPayment);

      // Validate payment state
      const validation = await this.validatePaymentState(pendingPayment);
      
      if (!validation.isValid) {
        console.warn('⚠️ Invalid payment state, cleaning up:', validation.errors);
        await this.cleanupInvalidPayment();
        return null;
      }

      // Check if payment is stale
      if (pendingPayment.stale) {
        console.log('⚠️ Payment is stale:', pendingPayment);
        return { ...pendingPayment, status: 'stale' };
      }

      return pendingPayment;
    } catch (error) {
      console.error('❌ Error checking pending payment:', error);
      return null;
    }
  }

  /**
   * Validate payment state with backend
   */
  async validatePaymentState(paymentState) {
    const errors = [];
    
    try {
      // Basic validation
      if (!paymentState.invoiceId) {
        errors.push('Missing invoice ID');
      }
      
      if (!paymentState.paymentMethod) {
        errors.push('Missing payment method');
      }
      
      if (!paymentState.amount || paymentState.amount <= 0) {
        errors.push('Invalid amount');
      }

      if (errors.length > 0) {
        return { isValid: false, errors };
      }

      // Backend validation
      const invoiceValidation = await this.validateWithBackend(paymentState);
      
      if (!invoiceValidation.isValid) {
        errors.push(...invoiceValidation.errors);
      }

      return {
        isValid: errors.length === 0,
        errors
      };
    } catch (error) {
      console.error('❌ Error validating payment state:', error);
      return { isValid: false, errors: ['Validation error: ' + error.message] };
    }
  }

  /**
   * Validate with backend
   */
  async validateWithBackend(paymentState) {
    try {
      console.log('🔍 Validating with backend:', paymentState.invoiceId);
      
      // Check invoice exists and status
      const invoiceData = await banHangService.getHoaDonByIdHoaDon(paymentState.invoiceId);
      
      if (!invoiceData || invoiceData.error) {
        return { isValid: false, errors: ['Invoice not found or invalid'] };
      }

      // Check if invoice is already paid
      if (invoiceData.trang_thai === 'Hoàn thành') {
        return { isValid: false, errors: ['Invoice already completed'] };
      }

      // Check payment status with payment provider
      let paymentStatus = null;
      
      if (paymentState.paymentMethod === 'ZaloPay') {
        paymentStatus = await thanhToanService.checkZaloPayStatus(paymentState.invoiceId);
      }

      // Check if payment was actually successful
      if (paymentStatus && paymentStatus.return_code === 1) {
        return { 
          isValid: false, 
          errors: ['Payment already completed but not updated'],
          needsSync: true,
          paymentStatus
        };
      }

      return { isValid: true, invoiceData, paymentStatus };
    } catch (error) {
      console.error('❌ Backend validation error:', error);
      return { 
        isValid: false, 
        errors: ['Backend validation failed: ' + error.message] 
      };
    }
  }

  /**
   * Resume payment from recovery
   */
  async resumePayment(paymentState) {
    try {
      console.log('🔄 Resuming payment:', paymentState.invoiceId);
      
      // Import payment state
      paymentState.import(paymentState);
      
      // Start monitoring
      return await this.startPaymentMonitoring(paymentState);
    } catch (error) {
      console.error('❌ Error resuming payment:', error);
      throw error;
    }
  }

  /**
   * Start payment monitoring for recovery
   */
  async startPaymentMonitoring(paymentState) {
    return new Promise((resolve, reject) => {
      let retryCount = 0;
      const maxRetries = 20; // 1 minute max
      
      const monitor = setInterval(async () => {
        try {
          retryCount++;
          
          // Check payment status
          let status = null;
          
          if (paymentState.paymentMethod === 'ZaloPay') {
            status = await thanhToanService.checkZaloPayStatus(paymentState.invoiceId);
          }

          if (status) {
            if (status.return_code === 1) {
              // Success
              await this.handlePaymentSuccess(paymentState, status);
              clearInterval(monitor);
              resolve({ success: true, status });
            } else if (status.return_code === -1) {
              // Failed
              await this.handlePaymentFailure(paymentState, status);
              clearInterval(monitor);
              resolve({ success: false, status });
            }
          }

          // Check timeout
          if (retryCount >= maxRetries) {
            await this.handlePaymentTimeout(paymentState);
            clearInterval(monitor);
            resolve({ success: false, timeout: true });
          }
        } catch (error) {
          console.error('❌ Payment monitoring error:', error);
          if (retryCount >= maxRetries) {
            clearInterval(monitor);
            reject(error);
          }
        }
      }, 3000);
    });
  }

  /**
   * Handle payment success from recovery
   */
  async handlePaymentSuccess(paymentState, status) {
    try {
      console.log('✅ Payment success from recovery:', paymentState.invoiceId);
      
      // Update invoice status
      await banHangService.trangThaiDonHang(paymentState.invoiceId);
      
      // Clear payment state
      await this.cleanupPaymentState();
      
      // Show success message
      this.showSuccessMessage(paymentState, status);
      
      return true;
    } catch (error) {
      console.error('❌ Error handling payment success:', error);
      throw error;
    }
  }

  /**
   * Handle payment failure from recovery
   */
  async handlePaymentFailure(paymentState, status) {
    try {
      console.log('❌ Payment failure from recovery:', paymentState.invoiceId);
      
      // Clear payment state
      await this.cleanupPaymentState();
      
      // Show failure message
      this.showFailureMessage(paymentState, status);
      
      return false;
    } catch (error) {
      console.error('❌ Error handling payment failure:', error);
      throw error;
    }
  }

  /**
   * Handle payment timeout from recovery
   */
  async handlePaymentTimeout(paymentState) {
    try {
      console.log('⏰ Payment timeout from recovery:', paymentState.invoiceId);
      
      // Clear payment state
      await this.cleanupPaymentState();
      
      // Show timeout message
      this.showTimeoutMessage(paymentState);
      
      return false;
    } catch (error) {
      console.error('❌ Error handling payment timeout:', error);
      throw error;
    }
  }

  /**
   * Cleanup invalid payment state
   */
  async cleanupInvalidPayment() {
    try {
      await invoiceStorage.clearPaymentState();
      paymentState.reset();
      console.log('🧹 Cleaned up invalid payment state');
    } catch (error) {
      console.error('❌ Error cleaning up payment state:', error);
    }
  }

  /**
   * Cleanup payment state after completion
   */
  async cleanupPaymentState() {
    try {
      await invoiceStorage.clearPaymentState();
      paymentState.reset();
      console.log('🧹 Payment state cleaned up');
    } catch (error) {
      console.error('❌ Error cleaning up payment state:', error);
    }
  }

  /**
   * Show success message
   */
  showSuccessMessage(paymentState, status) {
    // Import and use message component
    import('ant-design-vue').then(({ message }) => {
      message.success({
        content: `Thanh toán ZaloPay thành công cho hóa đơn ${paymentState.invoiceId}`,
        duration: 5
      });
    });
  }

  /**
   * Show failure message
   */
  showFailureMessage(paymentState, status) {
    import('ant-design-vue').then(({ message }) => {
      message.error({
        content: `Thanh toán ZaloPay thất bại cho hóa đơn ${paymentState.invoiceId}`,
        duration: 5
      });
    });
  }

  /**
   * Show timeout message
   */
  showTimeoutMessage(paymentState) {
    import('ant-design-vue').then(({ message }) => {
      message.warning({
        content: `Thanh toán ZaloPay hết hạn cho hóa đơn ${paymentState.invoiceId}. Vui lòng thử lại.`,
        duration: 5
      });
    });
  }

  /**
   * Get recovery options based on payment state
   */
  getRecoveryOptions(paymentState) {
    const options = [];
    
    if (paymentState.stale) {
      options.push({
        key: 'restart',
        label: 'Thực hiện thanh toán mới',
        type: 'primary',
        action: 'restart'
      });
    } else if (paymentState.status === 'pending' || paymentState.status === 'processing') {
      options.push({
        key: 'continue',
        label: 'Tiếp tục kiểm tra thanh toán',
        type: 'primary',
        action: 'continue'
      });
      
      options.push({
        key: 'cancel',
        label: 'Hủy thanh toán',
        type: 'danger',
        action: 'cancel'
      });
    } else if (paymentState.status === 'failed') {
      options.push({
        key: 'retry',
        label: 'Thử lại thanh toán',
        type: 'primary',
        action: 'retry',
        disabled: !paymentState.canRetry
      });
      
      options.push({
        key: 'cancel',
        label: 'Hủy thanh toán',
        type: 'danger',
        action: 'cancel'
      });
    }

    return options;
  }

  /**
   * Execute recovery action
   */
  async executeRecoveryAction(action, paymentState) {
    try {
      switch (action) {
        case 'continue':
          return await this.resumePayment(paymentState);
          
        case 'retry':
          return await this.retryPayment(paymentState);
          
        case 'cancel':
          return await this.cancelPayment(paymentState);
          
        case 'restart':
          return await this.restartPayment(paymentState);
          
        default:
          throw new Error(`Unknown recovery action: ${action}`);
      }
    } catch (error) {
      console.error(`❌ Error executing recovery action ${action}:`, error);
      throw error;
    }
  }

  /**
   * Retry payment
   */
  async retryPayment(paymentState) {
    try {
      console.log('🔄 Retrying payment:', paymentState.invoiceId);
      
      // Reset retry count
      paymentState.retryCount = 0;
      
      // Update payment URL
      if (paymentState.paymentMethod === 'ZaloPay') {
        const response = await thanhToanService.handleZaloPayPayment(
          paymentState.invoiceId,
          paymentState.amount
        );
        
        paymentState.updateWithZaloPayResponse(response);
        
        // Open ZaloPay in new tab
        window.open(response.order_url, '_blank');
      }
      
      return true;
    } catch (error) {
      console.error('❌ Error retrying payment:', error);
      throw error;
    }
  }

  /**
   * Cancel payment
   */
  async cancelPayment(paymentState) {
    try {
      console.log('❌ Cancelling payment:', paymentState.invoiceId);
      
      // Clear payment state
      await this.cleanupPaymentState();
      
      return { cancelled: true };
    } catch (error) {
      console.error('❌ Error cancelling payment:', error);
      throw error;
    }
  }

  /**
   * Restart payment (for stale payments)
   */
  async restartPayment(paymentState) {
    try {
      console.log('🔄 Restarting payment:', paymentState.invoiceId);
      
      // Clear old payment state
      await this.cleanupPaymentState();
      
      // Return instruction to start new payment
      return { 
        restart: true,
        invoiceId: paymentState.invoiceId,
        amount: paymentState.amount
      };
    } catch (error) {
      console.error('❌ Error restarting payment:', error);
      throw error;
    }
  }

  /**
   * Auto-recovery on app initialization
   */
  async autoRecovery() {
    try {
      console.log('🔄 Starting auto-recovery process...');
      
      const pendingPayment = await this.checkPendingPayment();
      
      if (!pendingPayment) {
        console.log('✅ No recovery needed');
        return null;
      }

      // Auto-recover only if payment is still valid and not stale
      if (!pendingPayment.stale && pendingPayment.isActive) {
        console.log('🔄 Auto-recovering payment:', pendingPayment.invoiceId);
        return await this.resumePayment(pendingPayment);
      } else {
        console.log('⚠️ Payment requires manual recovery:', pendingPayment);
        return pendingPayment; // Return for manual handling
      }
    } catch (error) {
      console.error('❌ Auto-recovery error:', error);
      return null;
    }
  }
}

// Export singleton instance
export const recoveryService = new RecoveryService();