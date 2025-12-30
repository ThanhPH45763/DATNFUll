/**
 * Smart Polling Utility
 * Features:
 * - Exponential backoff on errors
 * - Visibility-based interval adjustment
 * - Max retries and timeout
 * - Automatic cleanup
 */

const POLLING_CONFIG = {
    FAST_POLL: 2000,       // Active polling (2s)
    NORMAL_POLL: 3000,     // Normal polling (3s) 
    SLOW_POLL: 10000,      // Tab inactive (10s)
    MAX_RETRIES: 5,
    BACKOFF_MULTIPLIER: 1.5,
    MAX_INTERVAL: 15000    // Cap at 15s
};

export class SmartPoller {
    constructor(fetchFn, options = {}) {
        this.fetchFn = fetchFn;
        this.baseInterval = options.interval || POLLING_CONFIG.NORMAL_POLL;
        this.currentInterval = this.baseInterval;
        this.maxPolls = options.maxPolls || 60; // 3 minutes at 3s interval
        this.onSuccess = options.onSuccess || (() => { });
        this.onTimeout = options.onTimeout || (() => { });
        this.onError = options.onError || (() => { });

        this.timer = null;
        this.pollCount = 0;
        this.errorCount = 0;
        this.isRunning = false;
        this.visibilityHandler = null;
    }

    start() {
        if (this.isRunning) {
            console.warn('Poller already running');
            return;
        }

        this.isRunning = true;
        this.pollCount = 0;
        this.errorCount = 0;

        // Setup visibility detection
        this.setupVisibilityDetection();

        // Start polling
        this.poll();
    }

    async poll() {
        if (!this.isRunning) return;

        this.pollCount++;
        console.log(`🔍 Smart Poll #${this.pollCount}/${this.maxPolls} (interval: ${this.currentInterval}ms)`);

        try {
            const result = await this.fetchFn();

            // Reset error count on success
            this.errorCount = 0;
            this.currentInterval = this.baseInterval;

            // Check if polling should continue
            if (result.shouldStop) {
                console.log('✅ Polling stopped by success callback');
                this.onSuccess(result.data);
                this.stop();
                return;
            }

            // Check timeout
            if (this.pollCount >= this.maxPolls) {
                console.log('⏱️ Polling timeout reached');
                this.onTimeout();
                this.stop();
                return;
            }

            // Schedule next poll
            this.scheduleNext();

        } catch (error) {
            console.error(`❌ Poll error #${this.errorCount + 1}:`, error);
            this.errorCount++;

            // Exponential backoff on errors
            if (this.errorCount < POLLING_CONFIG.MAX_RETRIES) {
                this.currentInterval = Math.min(
                    this.currentInterval * POLLING_CONFIG.BACKOFF_MULTIPLIER,
                    POLLING_CONFIG.MAX_INTERVAL
                );
                console.log(`⚠️ Backing off to ${this.currentInterval}ms`);
                this.onError(error, this.errorCount);
                this.scheduleNext();
            } else {
                console.error('🛑 Max retries reached, stopping poller');
                this.onError(error, this.errorCount, true);
                this.stop();
            }
        }
    }

    scheduleNext() {
        if (!this.isRunning) return;

        this.timer = setTimeout(() => this.poll(), this.currentInterval);
    }

    setupVisibilityDetection() {
        this.visibilityHandler = () => {
            if (document.hidden) {
                // Tab inactive - slow down polling
                console.log('👁️ Tab hidden - slowing polling');
                this.currentInterval = POLLING_CONFIG.SLOW_POLL;
            } else {
                // Tab active - resume normal speed
                console.log('👁️ Tab visible - resuming normal polling');
                this.currentInterval = this.baseInterval;

                // Immediate poll when tab becomes visible
                if (this.timer) {
                    clearTimeout(this.timer);
                    this.poll();
                }
            }
        };

        document.addEventListener('visibilitychange', this.visibilityHandler);
    }

    stop() {
        this.isRunning = false;

        if (this.timer) {
            clearTimeout(this.timer);
            this.timer = null;
        }

        if (this.visibilityHandler) {
            document.removeEventListener('visibilitychange', this.visibilityHandler);
            this.visibilityHandler = null;
        }

        console.log('🛑 Poller stopped');
    }

    restart() {
        this.stop();
        this.start();
    }
}

export default SmartPoller;
