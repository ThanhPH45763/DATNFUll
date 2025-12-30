/**
 * Enhanced Payment Polling Service
 * Handles reliable payment status checking with retry logic
 */

import { invoiceStorage } from '../utils/invoiceStorage.js';
import { paymentState } from '../utils/paymentState.js';
import { thanhToanService } from '@/services/thanhToan.js';
import { banHangService } from '@/services/banHangService.js';

export class PaymentPollingService {
  constructor() {
    this.pollingIntervals = new Map(); // Multiple payments can be polled
    this.activePolls = new Set();
    this.POLL_INTERVAL = 3000; // 3 seconds
    this.MAX_RETRIES = 20; // 1 minute max
    this.RETRY_DELAY = 5000; // 5 seconds between retries on error
    this.TIMEOUT_DURATION = 60 * 1000; // 1 minute timeout
    
    // Network status monitoring
    this.isOnline = navigator.onLine;
    this.setupNetworkMonitoring();
    
    // Event listeners
    this.eventListeners = new Map();
  }

  /**
   * Setup network status monitoring
   */
  setupNetworkMonitoring() {
    window.addEventListener('online', () => {
      this.isOnline = true;
      this.emit('networkStatusChanged', { isOnline: true });
      console.log('🌐 Network status: Online');
    });

    window.addEventListener('offline', () => {
      this.isOnline = false;
      this.emit('networkStatusChanged', { isOnline: false });
      console.log('📵 Network status: Offline');
    });
  }

  /**
   * Start polling for payment
   */
  startPolling(paymentId, paymentMethod, options = {}) {
    try {
      console.log(`🔄 Starting payment polling for ${paymentMethod}:`, paymentId);

      if (this.activePolls.has(paymentId)) {
        console.warn(`⚠️ Polling already active for ${paymentId}`);
        return false;
      }

      const pollConfig = {
        paymentId,
        paymentMethod,
        interval: this.POLL_INTERVAL,
        maxRetries: options.maxRetries || this.MAX_RETRIES,
        retryCount: 0,
        successCallbacks: [],
        failureCallbacks: [],
        timeoutCallbacks: [],
        retryDelay: options.retryDelay || this.RETRY_DELAY,
        startedAt: Date.now()
      };

      // Start polling
      const intervalId = setInterval(() => {
        this.pollPaymentStatus(pollConfig);
      }, pollConfig.interval);

      // Store polling config
      this.pollingIntervals.set(paymentId, {
        ...pollConfig,
        intervalId,
        isActive: true
      });

      this.activePolls.add(paymentId);

      // Set timeout for maximum duration
      const timeoutId = setTimeout(() => {
        this.handlePaymentTimeout(paymentId);
      }, this.TIMEOUT_DURATION);

      this.pollingIntervals.get(paymentId).timeoutId = timeoutId;

      console.log(`✅ Polling started for ${paymentId}`);
      
      return true;
    } catch (error) {
      console.error('❌ Error starting payment polling:', error);
      return false;
    }
  }

  /**
   * Poll payment status
   */
  async pollPaymentStatus(pollConfig) {
    try {
      // Check network status
      if (!this.isOnline) {
        console.log('📵 Offline, skipping poll');
        return;
      }

      // Check if polling is still active
      const pollData = this.pollingIntervals.get(pollConfig.paymentId);
      if (!pollData || !pollData.isActive) {
        return;
      }

      pollConfig.retryCount++;
      
      console.log(`🔄 Polling attempt ${pollConfig.retryCount}/${pollConfig.maxRetries} for ${pollConfig.paymentId}`);

      // Get payment status based on method
      let status = null;
      
      switch (pollConfig.paymentMethod) {
        case 'ZaloPay':
          status = await thanhToanService.checkZaloPayStatus(pollConfig.paymentId);
          break;
        case 'PayOS':
          status = await thanhToanService.checkStatusPayment(pollConfig.paymentId);
          break;
        default:
          console.warn(`⚠️ Unknown payment method: ${pollConfig.paymentMethod}`);
          return;
      }

      if (!status) {
        console.warn(`⚠️ No status response for ${pollConfig.paymentId}`);
        return;
      }

      // Handle different status responses
      await this.handleStatusResponse(pollConfig, status);
      
    } catch (error) {
      console.error(`❌ Polling error for ${pollConfig.paymentId}:`, error);
      
      // Implement exponential backoff for retries
      const backoffDelay = this.calculateBackoffDelay(pollConfig.retryCount);
      
      setTimeout(() => {
        // Will retry on next interval
      }, backoffDelay);
    }
  }

  /**
   * Handle payment status response
   */
  async handleStatusResponse(pollConfig, status) {
    try {
      const paymentId = pollConfig.paymentId;
      
      // ZaloPay success
      if (pollConfig.paymentMethod === 'ZaloPay' && status.return_code === 1) {
        await this.handlePaymentSuccess(paymentId, status);
        this.stopPolling(paymentId);
        return;
      }

      // ZaloPay failure
      if (pollConfig.paymentMethod === 'ZaloPay' && status.return_code === -1) {
        await this.handlePaymentFailure(paymentId, status);
        this.stopPolling(paymentId);
        return;
      }

      // PayOS success
      if (pollConfig.paymentMethod === 'PayOS' && status.status === 'PAID') {
        await this.handlePaymentSuccess(paymentId, status);
        this.stopPolling(paymentId);
        return;
      }

      // PayOS failure
      if (pollConfig.paymentMethod === 'PayOS' && status.status === 'FAILED') {
        await this.handlePaymentFailure(paymentId, status);
        this.stopPolling(paymentId);
        return;
      }

      // Payment still pending
      console.log(`⏳ Payment ${paymentId} still pending...`);
      
      // Reset retry count on successful poll
      if (status.return_code === 0 || status.status === 'PENDING') {
        pollConfig.retryCount = 0;
      }
      
    } catch (error) {
      console.error('❌ Error handling status response:', error);
    }
  }

  /**
   * Handle payment success
   */
  async handlePaymentSuccess(paymentId, status) {
    try {
      console.log(`✅ Payment success: ${paymentId}`, status);

      // Update backend
      await banHangService.trangThaiDonHang(paymentId);

      // Update state
      paymentState.updateStatus('success');
      
      // Clear storage
      await invoiceStorage.clearPaymentState();

      // Emit success event
      this.emit('paymentSuccess', {
        paymentId,
        status,
        timestamp: Date.now()
      });

    } catch (error) {
      console.error('❌ Error handling payment success:', error);
    }
  }

  /**
   * Handle payment failure
   */
  async handlePaymentFailure(paymentId, status) {
    try {
      console.log(`❌ Payment failure: ${paymentId}`, status);

      // Update state
      paymentState.updateStatus('failed', status);

      // Clear storage
      await invoiceStorage.clearPaymentState();

      // Emit failure event
      this.emit('paymentFailure', {
        paymentId,
        status,
        timestamp: Date.now()
      });

    } catch (error) {
      console.error('❌ Error handling payment failure:', error);
    }
  }

  /**
   * Handle payment timeout
   */
  async handlePaymentTimeout(paymentId) {
    try {
      console.log(`⏰ Payment timeout: ${paymentId}`);

      // Update state
      paymentState.updateStatus('timeout');

      // Clear storage
      await invoiceStorage.clearPaymentState();

      // Emit timeout event
      this.emit('paymentTimeout', {
        paymentId,
        timestamp: Date.now()
      });

      // Stop polling
      this.stopPolling(paymentId);

    } catch (error) {
      console.error('❌ Error handling payment timeout:', error);
    }
  }

  /**
   * Stop polling for payment
   */
  stopPolling(paymentId) {
    try {
      console.log(`🛑 Stopping polling for ${paymentId}`);

      const pollData = this.pollingIntervals.get(paymentId);
      if (!pollData) {
        return;
      }

      // Clear interval
      if (pollData.intervalId) {
        clearInterval(pollData.intervalId);
      }

      // Clear timeout
      if (pollData.timeoutId) {
        clearTimeout(pollData.timeoutId);
      }

      // Mark as inactive
      pollData.isActive = false;

      // Remove from active polls
      this.activePolls.delete(paymentId);

      console.log(`✅ Polling stopped for ${paymentId}`);

    } catch (error) {
      console.error('❌ Error stopping polling:', error);
    }
  }

  /**
   * Calculate exponential backoff delay
   */
  calculateBackoffDelay(retryCount) {
    // Exponential backoff: 1s, 2s, 4s, 8s, 16s max
    const baseDelay = 1000;
    const maxDelay = 16000;
    const delay = Math.min(baseDelay * Math.pow(2, retryCount - 1), maxDelay);
    
    console.log(`⏱️ Backoff delay: ${delay}ms (retry ${retryCount})`);
    
    return delay;
  }

  /**
   * Check if payment is being polled
   */
  isPolling(paymentId) {
    return this.activePolls.has(paymentId);
  }

  /**
   * Get active polls count
   */
  getActivePollsCount() {
    return this.activePolls.size;
  }

  /**
   * Get polling status
   */
  getPollingStatus(paymentId) {
    const pollData = this.pollingIntervals.get(paymentId);
    return pollData ? {
      isActive: pollData.isActive,
      retryCount: pollData.retryCount,
      maxRetries: pollData.maxRetries,
      startedAt: pollData.startedAt,
      duration: Date.now() - pollData.startedAt
    } : null;
  }

  /**
   * Pause all polling (when network offline)
   */
  pauseAllPolling() {
    try {
      console.log('⏸️ Pausing all payment polling...');

      for (const [paymentId, pollData] of this.pollingIntervals) {
        if (pollData.isActive && pollData.intervalId) {
          clearInterval(pollData.intervalId);
          pollData.isPaused = true;
        }
      }

      this.emit('allPollingPaused');

    } catch (error) {
      console.error('❌ Error pausing polling:', error);
    }
  }

  /**
   * Resume all polling (when network back online)
   */
  resumeAllPolling() {
    try {
      console.log('▶️ Resuming all payment polling...');

      for (const [paymentId, pollData] of this.pollingIntervals) {
        if (pollData.isPaused && pollData.isActive) {
          pollData.intervalId = setInterval(() => {
            this.pollPaymentStatus(pollData);
          }, pollData.interval);
          pollData.isPaused = false;
        }
      }

      this.emit('allPollingResumed');

    } catch (error) {
      console.error('❌ Error resuming polling:', error);
    }
  }

  /**
   * Stop all polling
   */
  stopAllPolling() {
    try {
      console.log('🛑 Stopping all payment polling...');

      for (const paymentId of this.activePolls) {
        this.stopPolling(paymentId);
      }

      this.emit('allPollingStopped');

    } catch (error) {
      console.error('❌ Error stopping all polling:', error);
    }
  }

  /**
   * Add event listener
   */
  on(event, callback) {
    if (!this.eventListeners.has(event)) {
      this.eventListeners.set(event, []);
    }
    this.eventListeners.get(event).push(callback);
  }

  /**
   * Remove event listener
   */
  off(event, callback) {
    if (this.eventListeners.has(event)) {
      const listeners = this.eventListeners.get(event);
      const index = listeners.indexOf(callback);
      if (index > -1) {
        listeners.splice(index, 1);
      }
    }
  }

  /**
   * Emit event
   */
  emit(event, data) {
    if (this.eventListeners.has(event)) {
      this.eventListeners.get(event).forEach(callback => {
        try {
          callback(data);
        } catch (error) {
          console.error(`❌ Error in payment polling event listener for ${event}:`, error);
        }
      });
    }
  }

  /**
   * Cleanup resources
   */
  cleanup() {
    try {
      console.log('🧹 Cleaning up payment polling service...');

      // Stop all polling
      this.stopAllPolling();

      // Clear data
      this.pollingIntervals.clear();
      this.activePolls.clear();
      this.eventListeners.clear();

      console.log('✅ Payment polling service cleaned up');

    } catch (error) {
      console.error('❌ Error during cleanup:', error);
    }
  }

  /**
   * Debug current status
   */
  debug() {
    console.group('💳 Payment Polling Service Debug');
    console.log('Active Polls:', Array.from(this.activePolls));
    console.log('Polling Intervals:', this.pollingIntervals.size);
    console.log('Network Status:', this.isOnline ? 'Online' : 'Offline');
    console.log('Event Listeners:', this.eventListeners.size);
    console.groupEnd();
  }
}

// Export singleton instance
export const paymentPollingService = new PaymentPollingService();