/**
 * Invoice State Manager for POS
 * Centralized state management with persistence and recovery capabilities
 */

import { invoiceStorage } from '../utils/invoiceStorage.js';
import { banHangService } from '@/services/banHangService.js';
import { useGbStore } from '@/stores/gbStore.js';

export class InvoiceStateManager {
  constructor() {
    this.currentInvoice = null;
    this.currentTabId = null;
    this.tabContext = new Map();
    this.invoiceHistory = new Map();
    this.lastSyncTime = null;
    this.syncDebounceTimer = null;
    
    this.SYNC_DEBOUNCE_TIME = 1000; // 1 second
    this.MAX_HISTORY_SIZE = 50;
    
    // Event listeners
    this.eventListeners = new Map();
    
    // Auto-save interval
    this.autoSaveInterval = null;
    this.startAutoSave();
  }

  /**
   * Start auto-save mechanism
   */
  startAutoSave() {
    this.autoSaveInterval = setInterval(() => {
      this.autoSave();
    }, 5000); // Save every 5 seconds
  }

  /**
   * Stop auto-save mechanism
   */
  stopAutoSave() {
    if (this.autoSaveInterval) {
      clearInterval(this.autoSaveInterval);
      this.autoSaveInterval = null;
    }
  }

  /**
   * Auto-save current state
   */
  autoSave() {
    try {
      if (this.currentInvoice && this.currentTabId) {
        this.saveInvoice(this.currentInvoice, { autoSave: true });
      }
    } catch (error) {
      console.error('❌ Auto-save error:', error);
    }
  }

  /**
   * Create new invoice
   */
  async createInvoice() {
    try {
      console.log('📄 Creating new invoice...');
      
      // Call backend to create invoice
      const response = await banHangService.createHoaDon();
      
      if (response.error || !response.id_hoa_don) {
        throw new Error('Failed to create invoice: ' + (response.message || 'Unknown error'));
      }

      const newInvoice = {
        ...response,
        items: [],
        customer: null,
        shipping: {
          method: 'Nhận tại cửa hàng',
          fee: 0,
          address: null
        },
        payment: {
          method: 'Tiền mặt',
          status: 'pending',
          amount: 0
        },
        totals: {
          subtotal: 0,
          discount: 0,
          shipping: 0,
          total: 0
        },
        createdAt: Date.now(),
        lastModified: Date.now(),
        version: '1.0'
      };

      // Set as current invoice
      this.setCurrentInvoice(newInvoice);
      
      // Add to history
      this.addToHistory(newInvoice);
      
      console.log('✅ New invoice created:', newInvoice.id_hoa_don);
      
      return newInvoice;
    } catch (error) {
      console.error('❌ Error creating invoice:', error);
      throw error;
    }
  }

  /**
   * Set current invoice
   */
  setCurrentInvoice(invoice, options = {}) {
    try {
      const oldInvoice = this.currentInvoice;
      
      // Update current invoice
      this.currentInvoice = invoice;
      this.currentTabId = invoice.id_hoa_don;
      
      // Update tab context
      this.tabContext.set(invoice.id_hoa_don, {
        invoice,
        lastAccessed: Date.now(),
        isActive: true
      });
      
      // Save to storage
      this.saveInvoice(invoice, options);
      
      // Emit event
      this.emit('invoiceChanged', {
        invoice,
        oldInvoice,
        source: options.source || 'manual'
      });
      
      console.log('📄 Current invoice set:', invoice.id_hoa_don);
    } catch (error) {
      console.error('❌ Error setting current invoice:', error);
    }
  }

  /**
   * Save invoice with persistence
   */
  async saveInvoice(invoice, options = {}) {
    try {
      if (!invoice || !invoice.id_hoa_don) {
        console.warn('⚠️ Cannot save invalid invoice');
        return false;
      }

      // Add metadata
      const invoiceWithMetadata = {
        ...invoice,
        lastModified: Date.now(),
        savedAt: Date.now(),
        ...options
      };

      // Save to multiple layers
      const saved = invoiceStorage.saveInvoice(invoiceWithMetadata, {
        memoryOnly: options.memoryOnly,
        source: options.source || 'auto-save'
      });

      if (saved) {
        // Update last sync time
        this.lastSyncTime = Date.now();
        
        // Emit event
        this.emit('invoiceSaved', {
          invoice: invoiceWithMetadata,
          options
        });
      }

      return saved;
    } catch (error) {
      console.error('❌ Error saving invoice:', error);
      return false;
    }
  }

  /**
   * Get current invoice
   */
  getCurrentInvoice() {
    return this.currentInvoice;
  }

  /**
   * Get invoice by ID
   */
  getInvoice(invoiceId) {
    try {
      // Check current invoice first
      if (this.currentInvoice && this.currentInvoice.id_hoa_don === invoiceId) {
        return this.currentInvoice;
      }

      // Check tab context
      const tabData = this.tabContext.get(invoiceId);
      if (tabData && tabData.invoice) {
        return tabData.invoice;
      }

      // Check history
      return this.invoiceHistory.get(invoiceId);
    } catch (error) {
      console.error('❌ Error getting invoice:', error);
      return null;
    }
  }

  /**
   * Recover invoice from storage
   */
  async recoverInvoice() {
    try {
      console.log('🔄 Attempting to recover invoice...');
      
      // Get from storage
      const recoveredData = invoiceStorage.recoverInvoice();
      
      if (!recoveredData) {
        console.log('✅ No invoice to recover');
        return null;
      }

      // Validate recovered data
      const validation = await this.validateInvoice(recoveredData.data);
      
      if (!validation.isValid) {
        console.warn('⚠️ Recovered invoice validation failed:', validation.errors);
        await this.cleanupInvalidInvoice();
        return null;
      }

      // Set as current invoice
      await this.setCurrentInvoice(recoveredData.data, {
        source: 'recovery'
      });

      console.log('✅ Invoice recovered:', recoveredData.data.id_hoa_don);
      
      return recoveredData.data;
    } catch (error) {
      console.error('❌ Error recovering invoice:', error);
      return null;
    }
  }

  /**
   * Validate invoice with backend
   */
  async validateInvoice(invoice) {
    const errors = [];
    
    try {
      // Basic validation
      if (!invoice.id_hoa_don) {
        errors.push('Missing invoice ID');
      }

      if (!Array.isArray(invoice.items)) {
        errors.push('Invalid items array');
      }

      // Backend validation
      if (invoice.id_hoa_don) {
        const backendData = await banHangService.getHoaDonByIdHoaDon(invoice.id_hoa_don);
        
        if (!backendData || backendData.error) {
          errors.push('Invoice not found in backend');
        } else {
          // Compare status
          if (backendData.trang_thai === 'Hoàn thành') {
            errors.push('Invoice already completed');
          }
        }
      }

      return {
        isValid: errors.length === 0,
        errors
      };
    } catch (error) {
      console.error('❌ Error validating invoice:', error);
      return {
        isValid: false,
        errors: ['Validation error: ' + error.message]
      };
    }
  }

  /**
   * Add product to invoice
   */
  async addProduct(productId, quantity = 1) {
    try {
      if (!this.currentInvoice) {
        throw new Error('No current invoice');
      }

      console.log('➕ Adding product to invoice:', productId, quantity);
      
      // Call backend to add product
      const response = await banHangService.themSPHDMoi(
        this.currentInvoice.id_hoa_don,
        productId,
        quantity
      );

      if (response.error) {
        throw new Error('Failed to add product: ' + response.message);
      }

      // Refresh invoice from backend to get accurate data
      await this.refreshInvoice(this.currentInvoice.id_hoa_don);
      
      console.log('✅ Product added successfully');
      
      return true;
    } catch (error) {
      console.error('❌ Error adding product:', error);
      throw error;
    }
  }

  /**
   * Update product quantity
   */
  async updateProductQuantity(productId, newQuantity) {
    try {
      if (!this.currentInvoice) {
        throw new Error('No current invoice');
      }

      console.log('🔄 Updating product quantity:', productId, newQuantity);
      
      // Find product in current invoice
      const existingItem = this.currentInvoice.items.find(
        item => item.id_chi_tiet_san_pham === productId
      );

      if (!existingItem) {
        throw new Error('Product not found in invoice');
      }

      // Call backend to update
      const response = await banHangService.setSPHD(
        this.currentInvoice.id_hoa_don,
        productId,
        newQuantity
      );

      if (response.error) {
        throw new Error('Failed to update quantity: ' + response.message);
      }

      // Refresh invoice from backend
      await this.refreshInvoice(this.currentInvoice.id_hoa_don);
      
      console.log('✅ Product quantity updated');
      
      return true;
    } catch (error) {
      console.error('❌ Error updating product quantity:', error);
      throw error;
    }
  }

  /**
   * Remove product from invoice
   */
  async removeProduct(productId) {
    try {
      if (!this.currentInvoice) {
        throw new Error('No current invoice');
      }

      console.log('➖ Removing product from invoice:', productId);
      
      // Call backend to remove
      const response = await banHangService.xoaSPHD(
        this.currentInvoice.id_hoa_don,
        productId
      );

      if (response.error) {
        throw new Error('Failed to remove product: ' + response.message);
      }

      // Refresh invoice from backend
      await this.refreshInvoice(this.currentInvoice.id_hoa_don);
      
      console.log('✅ Product removed successfully');
      
      return true;
    } catch (error) {
      console.error('❌ Error removing product:', error);
      throw error;
    }
  }

  /**
   * Refresh invoice from backend
   */
  async refreshInvoice(invoiceId) {
    try {
      console.log('🔄 Refreshing invoice from backend:', invoiceId);
      
      // Get fresh data from backend
      const invoiceData = await banHangService.getHoaDonByIdHoaDon(invoiceId);
      const itemsData = await banHangService.getAllSPHD(invoiceId);
      
      if (invoiceData.error || itemsData.error) {
        throw new Error('Failed to refresh invoice data');
      }

      // Update current invoice
      const refreshedInvoice = {
        ...this.currentInvoice,
        ...invoiceData,
        items: itemsData,
        lastModified: Date.now()
      };

      this.setCurrentInvoice(refreshedInvoice, {
        source: 'refresh'
      });

      console.log('✅ Invoice refreshed from backend');
      
      return refreshedInvoice;
    } catch (error) {
      console.error('❌ Error refreshing invoice:', error);
      throw error;
    }
  }

  /**
   * Calculate invoice totals
   */
  calculateTotals(invoice) {
    try {
      if (!invoice || !Array.isArray(invoice.items)) {
        return {
          subtotal: 0,
          discount: 0,
          shipping: 0,
          total: 0
        };
      }

      // Calculate subtotal
      const subtotal = invoice.items.reduce((sum, item) => {
        return sum + (item.don_gia * item.so_luong);
      }, 0);

      // Get discount and shipping from invoice
      const discount = invoice.voucher ? invoice.voucher.gia_tri_giam || 0 : 0;
      const shipping = invoice.phi_van_chuyen || 0;

      // Calculate total
      const total = Math.max(0, subtotal - discount + shipping);

      const totals = {
        subtotal,
        discount,
        shipping,
        total,
        breakdown: {
          items: invoice.items.length,
          quantities: invoice.items.reduce((sum, item) => sum + item.so_luong, 0)
        }
      };

      console.log('💰 Invoice totals calculated:', totals);
      
      return totals;
    } catch (error) {
      console.error('❌ Error calculating totals:', error);
      return {
        subtotal: 0,
        discount: 0,
        shipping: 0,
        total: 0
      };
    }
  }

  /**
   * Sync invoice before payment
   */
  async syncBeforePayment() {
    try {
      if (!this.currentInvoice) {
        throw new Error('No current invoice to sync');
      }

      console.log('🔄 Syncing invoice before payment...');
      
      // Get fresh data from backend
      const backendInvoice = await banHangService.getHoaDonByIdHoaDon(this.currentInvoice.id_hoa_don);
      const backendItems = await banHangService.getAllSPHD(this.currentInvoice.id_hoa_don);
      
      if (backendInvoice.error || backendItems.error) {
        throw new Error('Failed to get backend data for sync');
      }

      // Calculate totals
      const frontendTotals = this.calculateTotals(this.currentInvoice);
      const backendTotals = this.calculateTotals({
        ...backendInvoice,
        items: backendItems
      });

      // Compare totals
      const difference = Math.abs(frontendTotals.total - backendTotals.total);
      
      if (difference > 100) { // More than 100 VND difference
        console.warn('⚠️ Price difference detected:', {
          frontend: frontendTotals.total,
          backend: backendTotals.total,
          difference
        });

        return {
          needsSync: true,
          difference,
          frontendTotals,
          backendTotals,
          backendData: {
            ...backendInvoice,
            items: backendItems
          }
        };
      }

      // No sync needed, refresh current invoice
      await this.refreshInvoice(this.currentInvoice.id_hoa_don);
      
      return {
        needsSync: false,
        difference: 0,
        frontendTotals,
        backendTotals
      };
    } catch (error) {
      console.error('❌ Error syncing before payment:', error);
      throw error;
    }
  }

  /**
   * Add to history
   */
  addToHistory(invoice) {
    try {
      if (this.invoiceHistory.size >= this.MAX_HISTORY_SIZE) {
        // Remove oldest entry
        const oldestKey = this.invoiceHistory.keys().next().value;
        this.invoiceHistory.delete(oldestKey);
      }

      this.invoiceHistory.set(invoice.id_hoa_don, {
        invoice,
        addedAt: Date.now()
      });
    } catch (error) {
      console.error('❌ Error adding to history:', error);
    }
  }

  /**
   * Cleanup invalid invoice
   */
  async cleanupInvalidInvoice() {
    try {
      console.log('🧹 Cleaning up invalid invoice...');
      
      // Clear current invoice
      this.currentInvoice = null;
      this.currentTabId = null;
      
      // Clear storage
      invoiceStorage.clearPaymentState();
      
      // Emit event
      this.emit('invoiceCleanup', {
        reason: 'invalid'
      });
    } catch (error) {
      console.error('❌ Error cleaning up invoice:', error);
    }
  }

  /**
   * Clear current invoice
   */
  clearCurrentInvoice() {
    try {
      const oldInvoice = this.currentInvoice;
      
      this.currentInvoice = null;
      this.currentTabId = null;
      
      // Clear current tab from context
      if (oldInvoice) {
        this.tabContext.delete(oldInvoice.id_hoa_don);
      }
      
      console.log('🧹 Current invoice cleared');
      
      return oldInvoice;
    } catch (error) {
      console.error('❌ Error clearing current invoice:', error);
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
          console.error(`❌ Error in event listener for ${event}:`, error);
        }
      });
    }
  }

  /**
   * Get storage statistics
   */
  getStorageStats() {
    return invoiceStorage.getStorageStats();
  }

  /**
   * Cleanup resources
   */
  cleanup() {
    try {
      console.log('🧹 Cleaning up InvoiceStateManager...');
      
      // Stop auto-save
      this.stopAutoSave();
      
      // Clear current invoice
      this.clearCurrentInvoice();
      
      // Clear storage
      invoiceStorage.cleanup();
      
      // Clear event listeners
      this.eventListeners.clear();
      
      console.log('✅ InvoiceStateManager cleaned up');
    } catch (error) {
      console.error('❌ Error during cleanup:', error);
    }
  }

  /**
   * Debug current state
   */
  debug() {
    console.group('📄 Invoice State Manager Debug');
    console.log('Current Invoice:', this.currentInvoice);
    console.log('Current Tab ID:', this.currentTabId);
    console.log('Tab Context Size:', this.tabContext.size);
    console.log('History Size:', this.invoiceHistory.size);
    console.log('Last Sync Time:', new Date(this.lastSyncTime));
    console.log('Storage Stats:', this.getStorageStats());
    console.groupEnd();
  }
}

// Export singleton instance
export const invoiceStateManager = new InvoiceStateManager();