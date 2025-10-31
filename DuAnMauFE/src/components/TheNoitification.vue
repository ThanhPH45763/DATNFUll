<template>
    <div v-if="store.checkNoitification" class="notification-overlay">
        <div class="notification-container" :class="{ 'show': isVisible }">
            <div class="notification-content">
                <div class="notification-header">
                    <div class="notification-badge">Hot Deal</div>
                    <button class="close-button" @click="closeNotification">
                        <span>&times;</span>
                    </button>
                </div>
                <div class="notification-body">
                    <div class="notification-layout">
                        <div class="notification-left">
                            <div class="notification-image">
                                <img src="../images/product/shoe/anhGiayTheThao.webp" alt="Sale Product">
                                <div class="image-badge">HOT</div>
                            </div>
                        </div>
                        <div class="notification-right">
                            <h1 class="notification-title">Siêu Sale Sập Sàn!</h1>
                            <div class="notification-discount">
                                <span class="discount-value">30%</span>
                                <span class="discount-text">OFF</span>
                            </div>
                            <p class="notification-description">Chỉ trong hôm nay - Giảm đến 30% cho tất cả sản phẩm</p>

                            <div class="notification-timer">
                                <p>Ưu đãi kết thúc sau:</p>
                                <div class="timer-container">
                                    <div class="timer-item">
                                        <span class="timer-value">{{ hours }}</span>
                                        <span class="timer-label">Giờ</span>
                                    </div>
                                    <div class="timer-separator">:</div>
                                    <div class="timer-item">
                                        <span class="timer-value">{{ minutes }}</span>
                                        <span class="timer-label">Phút</span>
                                    </div>
                                    <div class="timer-separator">:</div>
                                    <div class="timer-item">
                                        <span class="timer-value">{{ seconds }}</span>
                                        <span class="timer-label">Giây</span>
                                    </div>
                                </div>
                            </div>

                            <div class="notification-cta">
                                <button class="shop-now-button" @click="shopNow">Mua Ngay</button>
                                <button class="view-more-button" @click="closeNotification">Xem Sau</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useGbStore } from '../stores/gbStore';
import { ref, onMounted, onBeforeUnmount } from 'vue';

const store = useGbStore();
const isVisible = ref(false);
const hours = ref(5);
const minutes = ref(59);
const seconds = ref(59);
let timerInterval = null;

const closeNotification = () => {
    isVisible.value = false;
    setTimeout(() => {
        store.closeNoitification();
    }, 300); // Đợi animation kết thúc rồi mới đóng
};

const shopNow = () => {
    closeNotification();
    // Có thể thêm logic chuyển hướng đến trang sản phẩm ở đây
};

const updateTimer = () => {
    if (seconds.value > 0) {
        seconds.value--;
    } else {
        if (minutes.value > 0) {
            minutes.value--;
            seconds.value = 59;
        } else {
            if (hours.value > 0) {
                hours.value--;
                minutes.value = 59;
                seconds.value = 59;
            } else {
                // Hết thời gian
                clearInterval(timerInterval);
            }
        }
    }
};

onMounted(() => {
    // Hiển thị notification với animation sau khi component được mount
    setTimeout(() => {
        isVisible.value = true;
    }, 100);

    // Khởi động bộ đếm ngược
    timerInterval = setInterval(updateTimer, 1000);
});

onBeforeUnmount(() => {
    // Dọn dẹp interval khi component bị hủy
    clearInterval(timerInterval);
});
</script>

<style scoped>
.notification-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.7);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
    backdrop-filter: blur(5px);
}

.notification-container {
    max-width: 800px;
    width: 90%;
    background: linear-gradient(135deg, #ffffff, #f8f9fa);
    border-radius: 16px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2), 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
    overflow: hidden;
    transform: scale(0.9);
    opacity: 0;
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.notification-container.show {
    transform: scale(1);
    opacity: 1;
}

.notification-content {
    padding: 0;
    position: relative;
}

.notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background: linear-gradient(135deg, #3a86ff, #5e60ce);
    position: relative;
}

.notification-badge {
    background-color: #ff4757;
    color: white;
    font-weight: bold;
    padding: 5px 12px;
    border-radius: 20px;
    font-size: 0.8rem;
    text-transform: uppercase;
    letter-spacing: 1px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
}

.close-button {
    background: transparent;
    border: none;
    color: white;
    font-size: 1.5rem;
    cursor: pointer;
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: all 0.2s;
}

.close-button:hover {
    background-color: rgba(255, 255, 255, 0.2);
}

.notification-body {
    padding: 20px;
}

.notification-layout {
    display: flex;
    gap: 20px;
}

.notification-left {
    flex: 1;
    display: flex;
    align-items: center;
}

.notification-right {
    flex: 1.2;
    text-align: left;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.notification-title {
    margin: 0 0 15px;
    color: #333;
    font-size: 2rem;
    font-weight: 800;
    text-transform: uppercase;
    letter-spacing: -0.5px;
    background: linear-gradient(to right, #ff4757, #ff6b81);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    line-height: 1.2;
}

.notification-discount {
    display: inline-block;
    margin: 10px 0 20px;
    position: relative;
}

.discount-value {
    font-size: 3.5rem;
    font-weight: 900;
    color: #ff4757;
    line-height: 1;
    display: inline-block;
    text-shadow: 2px 2px 0 rgba(0, 0, 0, 0.1);
}

.discount-text {
    font-size: 1.5rem;
    font-weight: 700;
    color: #333;
    margin-left: 5px;
    position: relative;
    top: -10px;
}

.notification-description {
    margin: 0 0 20px;
    color: #666;
    font-size: 1rem;
    line-height: 1.5;
}

.notification-image {
    position: relative;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    width: 100%;
    height: 100%;
    max-height: 300px;
}

.notification-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: transform 0.5s;
}

.notification-image:hover img {
    transform: scale(1.05);
}

.image-badge {
    position: absolute;
    top: 10px;
    right: 10px;
    background-color: #ff4757;
    color: white;
    font-weight: bold;
    padding: 5px 10px;
    border-radius: 5px;
    font-size: 0.8rem;
    transform: rotate(5deg);
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
}

.notification-cta {
    display: flex;
    gap: 10px;
    margin: 20px 0 0;
}

.shop-now-button,
.view-more-button {
    flex: 1;
    padding: 12px 20px;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    font-size: 1rem;
    cursor: pointer;
    transition: all 0.2s;
}

.shop-now-button {
    background: linear-gradient(135deg, #3a86ff, #5e60ce);
    color: white;
    box-shadow: 0 4px 10px rgba(58, 134, 255, 0.3);
}

.shop-now-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 15px rgba(58, 134, 255, 0.4);
}

.view-more-button {
    background-color: #f1f2f6;
    color: #333;
}

.view-more-button:hover {
    background-color: #e9ecef;
}

.notification-timer {
    margin-top: 10px;
}

.notification-timer p {
    margin: 0 0 10px;
    color: #666;
    font-size: 0.9rem;
}

.timer-container {
    display: flex;
    align-items: center;
    gap: 5px;
}

.timer-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    background-color: #f8f9fa;
    border-radius: 8px;
    padding: 5px 10px;
    min-width: 60px;
}

.timer-value {
    font-size: 1.5rem;
    font-weight: 700;
    color: #333;
}

.timer-label {
    font-size: 0.7rem;
    color: #666;
    text-transform: uppercase;
}

.timer-separator {
    font-size: 1.5rem;
    font-weight: 700;
    color: #333;
    margin-top: -5px;
}

/* Responsive adjustments */
@media (max-width: 768px) {
    .notification-layout {
        flex-direction: column;
    }

    .notification-left,
    .notification-right {
        flex: none;
        width: 100%;
    }

    .notification-image {
        max-height: 200px;
    }

    .notification-right {
        text-align: center;
    }

    .timer-container {
        justify-content: center;
    }
}

@media (max-width: 576px) {
    .notification-container {
        width: 95%;
    }

    .notification-title {
        font-size: 1.5rem;
    }

    .discount-value {
        font-size: 2.5rem;
    }

    .discount-text {
        font-size: 1.2rem;
    }

    .timer-item {
        min-width: 45px;
    }

    .timer-value {
        font-size: 1.2rem;
    }
}
</style>