/**
 * Payment State Management for POS
 * Handles payment flow state with TTL and recovery capabilities
 */

export class PaymentState {
  constructor() {
    this.state = {
      invoiceId: null,
      paymentMethod: null,
      amount: null,
      appTransId: null,
      orderUrl: null,
      initiatedAt: null,
      expiresAt: null,
      status: 'pending', // pending, processing, success, failed, timeout
      retryCount: 0,
      maxRetries: 10,
      lastError: null,
      metadata: {}
    };
    
    this.TTL = 15 * 60 * 1000; // 15 minutes
    this.RETRY_DELAY = 3000; // 3 seconds
    this.MAX_RETRY_COUNT = 10;
  }

  /**
   * Create new payment state
   */
  create(invoiceId, amount, paymentMethod, metadata = {}) {
    const now = Date.now();
    
    this.state = {
      invoiceId,
      paymentMethod,
      amount,
      appTransId: null,
      orderUrl: null,
      initiatedAt: now,
      expiresAt: now + this.TTL,
      status: 'pending',
      retryCount: 0,
      maxRetries: this.MAX_RETRY_COUNT,
      lastError: null,
      metadata: {
        returnUrl: window.location.href,
        userAgent: navigator.userAgent,
        ...metadata
      }
    };

    console.log('💳 Payment state created:', {
      invoiceId,
      amount,
      method: paymentMethod,
      expiresAt: new Date(this.state.expiresAt).toLocaleString()
    });

    return this.state;
  }

  /**
   * Update payment state with ZaloPay response
   */
  updateWithZaloPayResponse(response) {
    if (!response || response.return_code !== 1) {
      throw new Error('Invalid ZaloPay response');
    }

    this.state.appTransId = response.app_trans_id;
    this.state.orderUrl = response.order_url;
    this.state.status = 'processing';
    this.state.metadata.zaloPayResponse = response;

    console.log('💳 Payment state updated with ZaloPay response:', {
      appTransId: response.app_trans_id,
      orderUrl: response.order_url
    });
  }

  /**
   * Update payment status
   */
  updateStatus(status, error = null) {
    const oldStatus = this.state.status;
    this.state.status = status;
    
    if (error) {
      this.state.lastError = {
        message: error.message || error,
        timestamp: Date.now(),
        stack: error.stack
      };
    }

    console.log(`💳 Payment status updated: ${oldStatus} → ${status}`, error || '');

    if (status === 'failed' || status === 'timeout') {
      this.state.retryCount++;
    }

    return this.state;
  }

  /**
   * Increment retry count
   */
  incrementRetry() {
    this.state.retryCount++;
    
    console.log(`💳 Payment retry ${this.state.retryCount}/${this.state.maxRetries}`);
    
    return this.state.retryCount;
  }

  /**
   * Check if payment can be retried
   */
  canRetry() {
    return this.state.retryCount < this.state.maxRetries;
  }

  /**
   * Check if payment state is expired
   */
  isExpired() {
    return Date.now() > this.state.expiresAt;
  }

  /**
   * Check if payment state is stale (old and not completed)
   */
  isStale() {
    const isExpired = this.isExpired();
    const isPendingOrProcessing = ['pending', 'processing'].includes(this.state.status);
    const isOld = Date.now() - this.state.initiatedAt > 30 * 60 * 1000; // 30 minutes
    
    return (isExpired && isPendingOrProcessing) || (isOld && isPendingOrProcessing);
  }

  /**
   * Get remaining time in seconds
   */
  getRemainingTime() {
    const remaining = Math.max(0, this.state.expiresAt - Date.now());
    return Math.floor(remaining / 1000);
  }

  /**
   * Get formatted remaining time
   */
  getRemainingTimeFormatted() {
    const seconds = this.getRemainingTime();
    if (seconds <= 0) {
      return 'Đã hết hạn';
    }

    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    
    if (minutes > 0) {
      return `${minutes} phút ${remainingSeconds} giây`;
    } else {
      return `${remainingSeconds} giây`;
    }
  }

  /**
   * Check if payment is still active
   */
  isActive() {
    return !this.isExpired() && 
           !['success', 'failed', 'timeout', 'cancelled'].includes(this.state.status);
  }

  /**
   * Get current state (copy for safety)
   */
  getState() {
    return { ...this.state };
  }

  /**
   * Reset payment state
   */
  reset() {
    const oldState = this.state;
    
    this.state = {
      invoiceId: null,
      paymentMethod: null,
      amount: null,
      appTransId: null,
      orderUrl: null,
      initiatedAt: null,
      expiresAt: null,
      status: 'pending',
      retryCount: 0,
      maxRetries: this.MAX_RETRY_COUNT,
      lastError: null,
      metadata: {}
    };

    console.log('💳 Payment state reset');
    
    return oldState;
  }

  /**
   * Validate payment state integrity
   */
  validate() {
    const errors = [];

    if (!this.state.invoiceId) {
      errors.push('Missing invoiceId');
    }

    if (!this.state.amount || this.state.amount <= 0) {
      errors.push('Invalid amount');
    }

    if (!this.state.paymentMethod) {
      errors.push('Missing paymentMethod');
    }

    if (!this.state.initiatedAt) {
      errors.push('Missing initiatedAt');
    }

    if (this.state.expiresAt && this.state.expiresAt <= Date.now()) {
      errors.push('Payment state expired');
    }

    if (errors.length > 0) {
      console.warn('💳 Payment state validation failed:', errors);
      return false;
    }

    return true;
  }

  /**
   * Get payment summary for UI
   */
  getSummary() {
    return {
      invoiceId: this.state.invoiceId,
      method: this.state.paymentMethod,
      amount: this.state.amount,
      status: this.state.status,
      statusText: this.getStatusText(),
      remainingTime: this.getRemainingTimeFormatted(),
      canRetry: this.canRetry(),
      retryCount: this.state.retryCount,
      maxRetries: this.state.maxRetries,
      isExpired: this.isExpired(),
      isStale: this.isStale(),
      lastError: this.state.lastError?.message || null
    };
  }

  /**
   * Get human-readable status text
   */
  getStatusText() {
    const statusMap = {
      'pending': 'Đang chờ thanh toán',
      'processing': 'Đang xử lý',
      'success': 'Thành công',
      'failed': 'Thất bại',
      'timeout': 'Hết hạn',
      'cancelled': 'Đã hủy'
    };

    return statusMap[this.state.status] || 'Không xác định';
  }

  /**
   * Export for storage
   */
  export() {
    return {
      ...this.state,
      exportedAt: Date.now(),
      version: '1.0'
    };
  }

  /**
   * Import from storage
   */
  import(data) {
    if (!data) {
      throw new Error('Cannot import payment state: no data provided');
    }

    // Validate imported data
    if (!data.invoiceId || !data.paymentMethod || !data.amount) {
      throw new Error('Invalid payment state data');
    }

    this.state = {
      ...this.state,
      ...data
    };

    // Adjust TTL if needed
    if (this.state.expiresAt && Date.now() > this.state.expiresAt) {
      this.state.status = 'timeout';
    }

    console.log('💳 Payment state imported:', this.getSummary());

    return this.state;
  }

  /**
   * Debug log current state
   */
  debug() {
    console.group('💳 Payment State Debug');
    console.log('State:', this.state);
    console.log('Summary:', this.getSummary());
    console.log('Validation:', this.validate());
    console.log('Remaining time:', this.getRemainingTimeFormatted());
    console.groupEnd();
  }
}

// Export singleton instance
export const paymentState = new PaymentState();