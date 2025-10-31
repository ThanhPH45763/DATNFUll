<template>
    <div class="banner-container mt-3" ref="sectionRef" :class="{ 'visible': isVisible }">
        <div class="marquee-container">
            <div class="marquee-content">
                <div class="marquee-text">
                    <span class="marquee-item marquee-item-star"><i class="fas fa-star"></i> Chào mừng đến với gian hàng
                        của chúng tôi</span>
                        <span class="marquee-separator"><i class="fas fa-circle"></i></span>
                        <span class="marquee-item marquee-item-fire"><i class="fas fa-fire"></i> Giảm giá lên đến 30% cho
                        nhiều sản phẩm</span>
                    <span class="marquee-separator"><i class="fas fa-circle"></i></span>
                    <span class="marquee-item marquee-item-truck"><i class="fas fa-shipping-fast"></i> Miễn phí vận
                        chuyển cho đơn hàng trên 2.000.000đ</span>
                </div>
            </div>
        </div>
        <div class="carousel-wrapper" @mouseenter="showArrows = true" @mouseleave="showArrows = false">
            <a-carousel arrows autoplay class="carousel" ref="carousel">
                <template #prevArrow>
                    <div class="custom-slick-arrow custom-slick-prev" :class="{ 'visible': showArrows }">
                        <left-circle-outlined />
                    </div>
                </template>
                <template #nextArrow>
                    <div class="custom-slick-arrow custom-slick-next" :class="{ 'visible': showArrows }">
                        <right-circle-outlined />
                    </div>
                </template>
                <div class="borderImage">
                    <img src="../images/banner/Banner-PC-3.png" alt="">
                </div>
                <div class="borderImage">
                    <img src="../images/banner/banner1.webp" alt="">
                </div>
                <div class="borderImage">
                    <img src="../images/banner/slider_2.jpg" alt="">
                </div>
            </a-carousel>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { LeftCircleOutlined, RightCircleOutlined } from '@ant-design/icons-vue';
import { useIntersectionObserver } from '@vueuse/core';
import '@fortawesome/fontawesome-free/css/all.min.css';

const showArrows = ref(false);
const sectionRef = ref(null);
const isVisible = ref(false);
const carousel = ref(null);

// Sử dụng Intersection Observer để theo dõi khi phần tử xuất hiện trong viewport
onMounted(() => {
    const { stop } = useIntersectionObserver(
        sectionRef,
        ([{ isIntersecting }]) => {
            if (isIntersecting) {
                isVisible.value = true;
                stop(); // Dừng quan sát sau khi đã hiển thị
            }
        },
        { threshold: 0.2 } // Hiển thị khi ít nhất 20% phần tử xuất hiện trong viewport
    );
});
</script>

<style scoped>
.banner-container {
    opacity: 0;
    transform: translateY(30px);
    transition: opacity 0.8s ease, transform 0.8s ease;
}

.banner-container.visible {
    opacity: 1;
    transform: translateY(0);
}

.marquee-container {
    position: relative;
    overflow: hidden;
    height: 50px;
    margin-bottom: 15px;
    background: linear-gradient(45deg, #3a86ff, #5e60ce, #7209b7);
    border-radius: 8px;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.1);
    position: relative;
    z-index: 1;
}

.marquee-container::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(45deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0));
    z-index: -1;
    border-radius: 8px;
}

.marquee-content {
    height: 100%;
    display: flex;
    align-items: center;
    animation: marquee 15s linear infinite;
}

.marquee-text {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    color: white;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 0.5px;
    font-family: 'Montserrat', sans-serif;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
    padding: 0 15px;
}

.marquee-item {
    display: inline-flex;
    align-items: center;
    padding: 0 15px;
    white-space: nowrap;
    transition: all 0.3s ease;
}

.marquee-item i {
    margin-right: 10px;
    font-size: 16px;
    filter: drop-shadow(0 2px 3px rgba(0, 0, 0, 0.2));
}

.marquee-item-star {
    color: #ffdd00;
    animation: pulse 2s infinite;
}

.marquee-item-star i {
    animation: spin 8s linear infinite;
}

.marquee-item-fire {
    color: #ff9e00;
    animation: pulse 2.2s infinite;
}

.marquee-item-fire i {
    animation: flame 1.5s ease-in-out infinite alternate;
}

.marquee-item-truck {
    color: #00ffcc;
    animation: pulse 2.4s infinite;
}

.marquee-item-truck i {
    animation: moveLeftRight 2s ease-in-out infinite;
}

.marquee-item-gift {
    color: #ff71ce;
    animation: pulse 2.6s infinite;
}

.marquee-item-gift i {
    animation: bounce 1.5s ease infinite;
}

.marquee-separator {
    display: inline-flex;
    align-items: center;
    margin: 0 15px;
    opacity: 0.6;
}

.marquee-separator i {
    font-size: 6px;
    color: rgba(255, 255, 255, 0.8);
    filter: drop-shadow(0 0 3px rgba(255, 255, 255, 0.5));
}

@keyframes marquee {
    0% {
        transform: translateX(0);
    }

    100% {
        transform: translateX(-100%);
    }
}

@keyframes pulse {
    0% {
        transform: scale(1);
    }

    50% {
        transform: scale(1.05);
    }

    100% {
        transform: scale(1);
    }
}

@keyframes spin {
    from {
        transform: rotate(0deg);
    }

    to {
        transform: rotate(360deg);
    }
}

@keyframes flame {
    0% {
        transform: scale(1) translateY(0);
        opacity: 0.8;
    }

    100% {
        transform: scale(1.2) translateY(-2px);
        opacity: 1;
    }
}

@keyframes moveLeftRight {
    0% {
        transform: translateX(-3px);
    }

    50% {
        transform: translateX(3px);
    }

    100% {
        transform: translateX(-3px);
    }
}

@keyframes bounce {

    0%,
    100% {
        transform: translateY(0);
    }

    50% {
        transform: translateY(-5px);
    }
}

.carousel-wrapper {
    position: relative;
    margin-top: 15px;
}

.carousel {
    width: 100%;
    opacity: 0;
    transform: translateY(20px);
    transition: all 0.6s ease;
    transition-delay: 0.3s;
}

.visible .carousel {
    opacity: 1;
    transform: translateY(0);
}

.borderImage {
    height: 100%;
    overflow: hidden;
    border-radius: 10px;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    position: relative;
}

.borderImage::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0.1) 0%, rgba(0, 0, 0, 0) 20%, rgba(0, 0, 0, 0) 80%, rgba(0, 0, 0, 0.1) 100%);
    pointer-events: none;
}

.borderImage img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.8s ease;
}

.borderImage:hover img {
    transform: scale(1.03);
}

.custom-slick-arrow {
    color: white;
    background-color: rgba(0, 0, 0, 0.3);
    font-size: 20px;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    z-index: 2;
    opacity: 0;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.custom-slick-prev {
    left: 15px;
}

.custom-slick-next {
    right: 15px;
}

.custom-slick-arrow.visible {
    opacity: 1;
}

.custom-slick-arrow:hover {
    background-color: rgba(0, 0, 0, 0.5);
    transform: translateY(-50%) scale(1.1);
}

:deep(.slick-slide) {
    text-align: center;
    height: 450px;
    line-height: 160px;
    overflow: hidden;
}

:deep(.slick-arrow) {
    z-index: 10;
}

:deep(.slick-prev),
:deep(.slick-next) {
    font-size: 0;
    line-height: 0;
    position: absolute;
    top: 50%;
    display: block;
    width: 40px;
    height: 40px;
    padding: 0;
    transform: translate(0, -50%);
    cursor: pointer;
    color: transparent;
    border: none;
    outline: none;
    background: transparent;
}

@media (max-width: 1200px) {
    :deep(.slick-slide) {
        height: 400px;
    }
}

@media (max-width: 992px) {
    :deep(.slick-slide) {
        height: 350px;
    }
}

@media (max-width: 768px) {
    :deep(.slick-slide) {
        height: 300px;
    }

    .marquee-text {
        font-size: 14px;
    }

    .marquee-item i {
        font-size: 14px;
    }
}

@media (max-width: 576px) {
    :deep(.slick-slide) {
        height: 250px;
    }

    .marquee-text {
        font-size: 12px;
    }

    .marquee-item i {
        font-size: 12px;
    }

    .marquee-separator {
        margin: 0 10px;
    }

    .marquee-separator i {
        font-size: 4px;
    }
}
</style>