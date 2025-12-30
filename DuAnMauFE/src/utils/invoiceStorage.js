/**
 * Multi-layer storage utility for POS invoice management
 * Provides reliable persistence with automatic fallback and cleanup
 */

export class InvoiceStorage {
  constructor() {
    this.STORAGE_KEYS = {
      SESSION: {
        ACTIVE_INVOICE: 'pos_active_invoice',
        PAYMENT_STATE: 'pos_payment_state',
        TAB_CONTEXT: 'pos_tab_context'
      },
      LOCAL: {
        INVOICE_BACKUP: 'pos_invoice_backup',
        RECOVERY_DATA: 'pos_recovery_data',
        LAST_ACTIVE_TAB: 'pos_last_active_tab'
      }
    };
    
    this.TTL = {
      PAYMENT_STATE: 15 * 60 * 1000, // 15 minutes
      INVOICE_BACKUP: 30 * 60 * 1000, // 30 minutes
      RECOVERY_DATA: 60 * 60 * 1000  // 1 hour
    };
  }

  /**
   * Save invoice data with multi-layer persistence
   */
  saveInvoice(invoiceData, options = {}) {
    try {
      const timestampedData = {
        data: invoiceData,
        timestamp: Date.now(),
        version: '1.0',
        ...options
      };

      // Layer 1: Session storage for current session
      this.setSessionStorage(this.STORAGE_KEYS.SESSION.ACTIVE_INVOICE, timestampedData);
      
      // Layer 2: Local storage for backup (only if not in memory only mode)
      if (!options.memoryOnly) {
        this.setLocalStorage(this.STORAGE_KEYS.LOCAL.INVOICE_BACKUP, timestampedData);
      }

      console.log('✅ Invoice saved to storage:', {
        id: invoiceData.id_hoa_don || invoiceData.id,
        memoryOnly: options.memoryOnly,
        timestamp: timestampedData.timestamp
      });

      return true;
    } catch (error) {
      console.error('❌ Error saving invoice to storage:', error);
      return false;
    }
  }

  /**
   * Recover invoice data from multiple sources
   */
  recoverInvoice() {
    try {
      // Priority 1: Session storage (most recent)
      let invoiceData = this.getFromSessionStorage(this.STORAGE_KEYS.SESSION.ACTIVE_INVOICE);
      
      if (invoiceData && this.validateInvoiceData(invoiceData)) {
        console.log('✅ Invoice recovered from session storage');
        return invoiceData;
      }

      // Priority 2: Local storage (backup)
      invoiceData = this.getFromLocalStorage(this.STORAGE_KEYS.LOCAL.INVOICE_BACKUP);
      
      if (invoiceData && this.validateInvoiceData(invoiceData)) {
        console.log('✅ Invoice recovered from local storage backup');
        // Restore to session storage for faster access
        this.setSessionStorage(this.STORAGE_KEYS.SESSION.ACTIVE_INVOICE, invoiceData);
        return invoiceData;
      }

      console.log('⚠️ No valid invoice data found for recovery');
      return null;
    } catch (error) {
      console.error('❌ Error recovering invoice from storage:', error);
      return null;
    }
  }

  /**
   * Save payment state with TTL
   */
  savePaymentState(paymentState) {
    try {
      const timestampedData = {
        ...paymentState,
        timestamp: Date.now(),
        expiresAt: Date.now() + this.TTL.PAYMENT_STATE
      };

      this.setSessionStorage(this.STORAGE_KEYS.SESSION.PAYMENT_STATE, timestampedData);
      this.setLocalStorage(this.STORAGE_KEYS.LOCAL.RECOVERY_DATA, timestampedData);

      console.log('✅ Payment state saved:', {
        invoiceId: paymentState.invoiceId,
        method: paymentState.paymentMethod,
        expiresAt: new Date(timestampedData.expiresAt).toLocaleString()
      });

      return true;
    } catch (error) {
      console.error('❌ Error saving payment state:', error);
      return false;
    }
  }

  /**
   * Get pending payment state
   */
  getPendingPayment() {
    try {
      let paymentState = this.getFromSessionStorage(this.STORAGE_KEYS.SESSION.PAYMENT_STATE);
      
      if (!paymentState) {
        paymentState = this.getFromLocalStorage(this.STORAGE_KEYS.LOCAL.RECOVERY_DATA);
      }

      if (!paymentState) {
        return null;
      }

      // Check if payment state is still valid
      if (Date.now() > paymentState.expiresAt) {
        console.log('⚠️ Payment state expired, cleaning up...');
        this.clearPaymentState();
        return { ...paymentState, stale: true };
      }

      return paymentState;
    } catch (error) {
      console.error('❌ Error getting pending payment:', error);
      return null;
    }
  }

  /**
   * Clear payment state
   */
  clearPaymentState() {
    try {
      this.removeSessionStorage(this.STORAGE_KEYS.SESSION.PAYMENT_STATE);
      this.removeLocalStorage(this.STORAGE_KEYS.LOCAL.RECOVERY_DATA);
      console.log('✅ Payment state cleared');
      return true;
    } catch (error) {
      console.error('❌ Error clearing payment state:', error);
      return false;
    }
  }

  /**
   * Save tab context
   */
  saveTabContext(tabContext) {
    try {
      const timestampedData = {
        data: tabContext,
        timestamp: Date.now()
      };

      this.setSessionStorage(this.STORAGE_KEYS.SESSION.TAB_CONTEXT, timestampedData);
      return true;
    } catch (error) {
      console.error('❌ Error saving tab context:', error);
      return false;
    }
  }

  /**
   * Get tab context
   */
  getTabContext() {
    try {
      const context = this.getFromSessionStorage(this.STORAGE_KEYS.SESSION.TAB_CONTEXT);
      return context?.data || null;
    } catch (error) {
      console.error('❌ Error getting tab context:', error);
      return null;
    }
  }

  /**
   * Cleanup expired data
   */
  cleanup() {
    try {
      const now = Date.now();
      
      // Clean local storage
      Object.entries(this.STORAGE_KEYS.LOCAL).forEach(([key, storageKey]) => {
        const data = this.getFromLocalStorage(storageKey);
        if (data && data.expiresAt && now > data.expiresAt) {
          this.removeLocalStorage(storageKey);
          console.log(`🧹 Cleaned up expired ${key}`);
        }
      });

      // Clean session storage
      Object.entries(this.STORAGE_KEYS.SESSION).forEach(([key, storageKey]) => {
        const data = this.getFromSessionStorage(storageKey);
        if (data && data.expiresAt && now > data.expiresAt) {
          this.removeSessionStorage(storageKey);
          console.log(`🧹 Cleaned up expired session ${key}`);
        }
      });

      console.log('✅ Storage cleanup completed');
    } catch (error) {
      console.error('❌ Error during cleanup:', error);
    }
  }

  /**
   * Validate invoice data structure
   */
  validateInvoiceData(invoiceData) {
    if (!invoiceData || !invoiceData.data) {
      return false;
    }

    const data = invoiceData.data;
    
    // Basic validation
    if (!data.id_hoa_don && !data.id) {
      console.warn('⚠️ Invoice missing ID');
      return false;
    }

    // Check if data is too old (older than 24 hours)
    if (Date.now() - invoiceData.timestamp > 24 * 60 * 60 * 1000) {
      console.warn('⚠️ Invoice data too old (>24 hours)');
      return false;
    }

    return true;
  }

  // Helper methods for storage operations
  setSessionStorage(key, data) {
    try {
      sessionStorage.setItem(key, JSON.stringify(data));
    } catch (error) {
      // Handle sessionStorage full
      if (error.name === 'QuotaExceededError') {
        console.warn('⚠️ SessionStorage full, clearing old data...');
        this.clearOldSessionData();
        sessionStorage.setItem(key, JSON.stringify(data));
      } else {
        throw error;
      }
    }
  }

  getSessionStorage(key) {
    try {
      const data = sessionStorage.getItem(key);
      return data ? JSON.parse(data) : null;
    } catch (error) {
      console.warn('⚠️ Error parsing sessionStorage data:', error);
      sessionStorage.removeItem(key);
      return null;
    }
  }

  removeSessionStorage(key) {
    sessionStorage.removeItem(key);
  }

  setLocalStorage(key, data) {
    try {
      localStorage.setItem(key, JSON.stringify(data));
    } catch (error) {
      // Handle localStorage full
      if (error.name === 'QuotaExceededError') {
        console.warn('⚠️ LocalStorage full, clearing old data...');
        this.clearOldData();
        localStorage.setItem(key, JSON.stringify(data));
      } else {
        throw error;
      }
    }
  }

  getFromLocalStorage(key) {
    try {
      const data = localStorage.getItem(key);
      return data ? JSON.parse(data) : null;
    } catch (error) {
      console.warn('⚠️ Error parsing localStorage data:', error);
      localStorage.removeItem(key);
      return null;
    }
  }

  removeLocalStorage(key) {
    localStorage.removeItem(key);
  }

  clearOldSessionData() {
    // Keep only recent data
    const keys = Object.values(this.STORAGE_KEYS.SESSION);
    keys.forEach(key => sessionStorage.removeItem(key));
  }

  clearOldData() {
    // Keep only essential data, remove old backups
    const keys = Object.values(this.STORAGE_KEYS.LOCAL);
    keys.forEach(key => {
      const data = this.getFromLocalStorage(key);
      if (data && data.expiresAt && Date.now() > data.expiresAt) {
        this.removeLocalStorage(key);
      }
    });
  }

  /**
   * Get storage statistics for debugging
   */
  getStorageStats() {
    try {
      const stats = {
        sessionStorage: {},
        localStorage: {},
        totalSize: 0
      };

      // Calculate sessionStorage usage
      Object.entries(this.STORAGE_KEYS.SESSION).forEach(([name, key]) => {
        const data = this.getSessionStorage(key);
        if (data) {
          stats.sessionStorage[name] = {
            exists: true,
            size: JSON.stringify(data).length,
            timestamp: data.timestamp
          };
        } else {
          stats.sessionStorage[name] = { exists: false };
        }
      });

      // Calculate localStorage usage
      Object.entries(this.STORAGE_KEYS.LOCAL).forEach(([name, key]) => {
        const data = this.getFromLocalStorage(key);
        if (data) {
          stats.localStorage[name] = {
            exists: true,
            size: JSON.stringify(data).length,
            timestamp: data.timestamp
          };
        } else {
          stats.localStorage[name] = { exists: false };
        }
      });

      return stats;
    } catch (error) {
      console.error('❌ Error getting storage stats:', error);
      return null;
    }
  }
}

// Export singleton instance
export const invoiceStorage = new InvoiceStorage();