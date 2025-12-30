/**
 * State Synchronization Service
 * Ensures FE and BE data consistency before critical operations
 */

import { invoiceStateManager } from '../stores/invoiceStateManager.js';
import { invoiceStorage } from '../utils/invoiceStorage.js';
import { banHangService } from '@/services/banHangService.js';
import { message } from 'ant-design-vue';

export class StateSyncService {
  constructor() {
    this.SYNC_THRESHOLD = 100; // 100 VND difference threshold
    this.SYNC_TIMEOUT = 10000; // 10 seconds timeout
    this.MAX_RETRY_ATTEMPTS = 3;
  }

  /**
   * Comprehensive sync before payment
   */
  async syncBeforePayment(invoiceId, options = {}) {
    try {
      console.log('🔄 Starting comprehensive sync before payment:', invoiceId);
      
      // Step 1: Get fresh data from backend
      const backendData = await this.getFreshBackendData(invoiceId);
      
      // Step 2: Get current frontend data
      const frontendData = invoiceStateManager.getCurrentInvoice();
      
      if (!frontendData) {
        throw new Error('No current invoice to sync');
      }
      
      // Step 3: Calculate and compare totals
      const comparison = await this.compareTotals(frontendData, backendData);
      
      // Step 4: Handle differences
      if (comparison.hasDifference) {
        const shouldContinue = await this.handleDifference(comparison, options);
        
        if (!shouldContinue) {
          throw new Error('User cancelled sync due to price difference');
        }
        
        // Sync frontend with backend if user agrees
        if (shouldContinue) {
          await invoiceStateManager.setCurrentInvoice(comparison.backendData, {
            source: 'payment_sync'
          });
        }
      }
      
      console.log('✅ Sync completed:', {
        hasDifference: comparison.hasDifference,
        synced: shouldContinue,
        difference: comparison.difference
      });
      
      return {
        success: true,
        synced: shouldContinue,
        hasDifference: comparison.hasDifference,
        difference: comparison.difference,
        backendData: comparison.backendData,
        frontendData: comparison.frontendData
      };
      
    } catch (error) {
      console.error('❌ Error during sync:', error);
      throw error;
    }
  }

  /**
   * Get fresh data from backend
   */
  async getFreshBackendData(invoiceId) {
    try {
      console.log('📡 Fetching fresh data from backend...');
      
      // Parallel calls for better performance
      const [invoiceData, itemsData] = await Promise.all([
        banHangService.getHoaDonByIdHoaDon(invoiceId),
        banHangService.getAllSPHD(invoiceId)
      ]);
      
      if (invoiceData.error || itemsData.error) {
        throw new Error('Failed to fetch backend data');
      }
      
      // Combine data
      const combinedData = {
        ...invoiceData,
        items: itemsData
      };
      
      console.log('✅ Fresh data fetched:', {
        invoiceId,
        itemCount: itemsData.length
      });
      
      return combinedData;
      
    } catch (error) {
      console.error('❌ Error fetching backend data:', error);
      throw error;
    }
  }

  /**
   * Compare totals between frontend and backend
   */
  async compareTotals(frontendData, backendData) {
    try {
      const frontendTotals = this.calculateFrontendTotals(frontendData);
      const backendTotals = this.calculateBackendTotals(backendData);
      
      const difference = Math.abs(frontendTotals.total - backendTotals.total);
      
      console.log('💰 Total comparison:', {
        frontend: frontendTotals.total,
        backend: backendTotals.total,
        difference
      });
      
      return {
        frontendTotals,
        backendTotals,
        difference,
        hasDifference: difference > this.SYNC_THRESHOLD,
        comparison: {
          items: this.compareItems(frontendData.items || [], backendData.items || []),
          totals: {
            subtotal: Math.abs(frontendTotals.subtotal - backendTotals.subtotal),
            discount: Math.abs(frontendTotals.discount - backendTotals.discount),
            shipping: Math.abs(frontendTotals.shipping - backendTotals.shipping),
            total: difference
          }
        }
      };
      
    } catch (error) {
      console.error('❌ Error comparing totals:', error);
      throw error;
    }
  }

  /**
   * Calculate frontend totals
   */
  calculateFrontendTotals(frontendData) {
    try {
      const items = frontendData.items || [];
      
      const subtotal = items.reduce((sum, item) => {
        return sum + (item.gia_ban * item.so_luong);
      }, 0);
      
      const discount = frontendData.voucher ? 
        (frontendData.voucher.gia_tri_giam || 0) : 0;
      
      const shipping = frontendData.phi_van_chuyen || 0;
      const total = Math.max(0, subtotal - discount + shipping);
      
      console.log('💰 Frontend totals calculated:', {
        subtotal,
        discount,
        shipping,
        total
      });
      
      return { subtotal, discount, shipping, total };
      
    } catch (error) {
      console.error('❌ Error calculating frontend totals:', error);
      return { subtotal: 0, discount: 0, shipping: 0, total: 0 };
    }
  }

  /**
   * Calculate backend totals
   */
  calculateBackendTotals(backendData) {
    try {
      const items = backendData.items || [];
      
      const subtotal = items.reduce((sum, item) => {
        return sum + (item.don_gia * item.so_luong);
      }, 0);
      
      const discount = backendData.tong_tien_truoc_giam - backendData.tong_tien_sau_giam;
      const shipping = backendData.phi_van_chuyen || 0;
      const total = backendData.tong_tien_sau_giam + shipping;
      
      console.log('💰 Backend totals calculated:', {
        subtotal,
        discount,
        shipping,
        total
      });
      
      return { subtotal, discount, shipping, total };
      
    } catch (error) {
      console.error('❌ Error calculating backend totals:', error);
      return { subtotal: 0, discount: 0, shipping: 0, total: 0 };
    }
  }

  /**
   * Compare items between frontend and backend
   */
  compareItems(frontendItems, backendItems) {
    try {
      const comparison = [];
      
      // Create lookup maps
      const frontendMap = new Map();
      const backendMap = new Map();
      
      frontendItems.forEach(item => {
        frontendMap.set(item.id_chi_tiet_san_pham, item);
      });
      
      backendItems.forEach(item => {
        backendMap.set(item.id_chi_tiet_san_pham, item);
      });
      
      // Compare each item
      for (const [id, frontendItem] of frontendMap) {
        const backendItem = backendMap.get(id);
        
        if (!backendItem) {
          // Item exists in frontend but not in backend
          comparison.push({
            type: 'missing_in_backend',
            id,
            frontendItem,
            backendItem: null
          });
        } else {
          const differences = [];
          
          // Compare critical fields
          if (frontendItem.so_luong !== backendItem.so_luong) {
            differences.push({
              type: 'quantity_mismatch',
              frontend: frontendItem.so_luong,
              backend: backendItem.so_luong
            });
          }
          
          if (Math.abs(frontendItem.don_gia - backendItem.don_gia) > this.SYNC_THRESHOLD) {
            differences.push({
              type: 'price_difference',
              frontend: frontendItem.don_gia,
              backend: backendItem.don_gia
            });
          }
          
          if (differences.length > 0) {
            comparison.push({
              type: 'item_mismatch',
              id,
              frontendItem,
              backendItem,
              differences
            });
          }
        }
      }
      
      console.log('🔍 Item comparison completed:', {
        totalFrontend: frontendItems.length,
        totalBackend: backendItems.length,
        mismatches: comparison.length
      });
      
      return comparison;
      
    } catch (error) {
      console.error('❌ Error comparing items:', error);
      return [];
    }
  }

  /**
   * Handle price differences
   */
  async handleDifference(comparison, options = {}) {
    try {
      console.log('⚠️ Price differences detected:', comparison.difference);
      
      if (options.autoResolve) {
        // Auto-resolve if difference is small
        if (comparison.difference < this.SYNC_THRESHOLD / 2) {
          console.log('🤖 Auto-resolving small difference:', comparison.difference);
          return true; // Accept small differences
        }
      }
      
      // Show detailed comparison dialog
      const shouldContinue = await this.showComparisonDialog(comparison);
      
      return shouldContinue;
      
    } catch (error) {
      console.error('❌ Error handling difference:', error);
      return false;
    }
  }

  /**
   * Show comparison dialog
   */
  async showComparisonDialog(comparison) {
    return new Promise((resolve) => {
      message.warning({
        content: 'Phát hiện sự khác biệt về giá giữa frontend và backend',
        duration: 0, // Keep visible
        key: 'price-difference-warning'
      });
      
      // You can implement a detailed modal here if needed
      console.log('📋 Comparison dialog shown - user decision needed');
      
      // For now, auto-resolve based on difference threshold
      setTimeout(() => {
        resolve(comparison.difference < this.SYNC_THRESHOLD);
      }, 1000);
    });
  }

  /**
   * Sync single item
   */
  async syncItem(invoiceId, itemId, updates = {}) {
    try {
      console.log('🔄 Syncing single item:', itemId, updates);
      
      // Get current item from backend
      const backendItems = await banHangService.getAllSPHD(invoiceId);
      const backendItem = backendItems.find(item => item.id_chi_tiet_san_pham === itemId);
      
      if (!backendItem) {
        throw new Error('Item not found in backend');
      }
      
      // Apply updates
      Object.assign(backendItem, updates);
      
      // Update in backend
      const response = await banHangService.setSPHD(
        invoiceId,
        itemId,
        backendItem.so_luong,
        backendItem.don_gia
      );
      
      if (response.error) {
        throw new Error('Failed to sync item: ' + response.message);
      }
      
      // Refresh invoice data
      await invoiceStateManager.refreshInvoice(invoiceId);
      
      console.log('✅ Item synced successfully');
      
      return true;
      
    } catch (error) {
      console.error('❌ Error syncing item:', error);
      throw error;
    }
  }

  /**
   * Validate operation
   */
  async validateOperation(invoiceId, operation) {
    try {
      console.log('🔍 Validating operation:', operation);
      
      // Check invoice exists and is valid
      const invoice = await this.getFreshBackendData(invoiceId);
      
      if (!invoice || invoice.trang_thai === 'Hoàn thành') {
        throw new Error('Invoice not found or already completed');
      }
      
      // Check stock availability
      if (operation.type === 'add_item' || operation.type === 'update_quantity') {
        const stockCheck = await this.validateStock(operation.productId, operation.quantity);
        
        if (!stockCheck.valid) {
          throw new Error(stockCheck.message || 'Insufficient stock');
        }
      }
      
      console.log('✅ Operation validated');
      return { valid: true };
      
    } catch (error) {
      console.error('❌ Validation error:', error);
      return { valid: false, error: error.message };
    }
  }

  /**
   * Validate stock availability
   */
  async validateStock(productId, requestedQuantity) {
    try {
      console.log('📦 Validating stock for:', productId, requestedQuantity);
      
      // Get realtime stock
      const stockResponse = await banHangService.getCTSPRealtime(productId);
      
      if (!stockResponse || stockResponse.so_luong < requestedQuantity) {
        return {
          valid: false,
          message: `Insufficient stock. Available: ${stockResponse?.so_luong || 0}, Requested: ${requestedQuantity}`
        };
      }
      
      console.log('✅ Stock validation passed');
      return { valid: true };
      
    } catch (error) {
      console.error('❌ Stock validation error:', error);
      return { valid: false, message: 'Stock validation failed' };
    }
  }

  /**
   * Get sync statistics
   */
  getSyncStats() {
    try {
      const storageStats = invoiceStorage.getStorageStats();
      
      return {
        storage: storageStats,
        lastSync: invoiceStateManager.lastSyncTime,
        invoiceCount: invoiceStateManager.tabContext?.size || 0,
        activeInvoice: invoiceStateManager.currentInvoice ? {
          id: invoiceStateManager.currentInvoice.id_hoa_don,
          itemCount: invoiceStateManager.currentInvoice.items?.length || 0
        } : null
      };
      
    } catch (error) {
      console.error('❌ Error getting sync stats:', error);
      return null;
    }
  }
}

// Export singleton instance
export const stateSyncService = new StateSyncService();