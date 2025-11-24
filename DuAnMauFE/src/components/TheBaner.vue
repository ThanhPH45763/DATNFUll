<template>
    <div class="banner-elegant" ref="sectionRef" :class="{ 'visible': isVisible }">
        <!-- Elegant Promotional Banner (thay thế marquee) -->
        <div class="promo-banner">
            <div class="promo-content">
                <span class="promo-item">
                    <i class="fas fa-truck"></i>
                    Miễn phí vận chuyển cho đơn hàng trên 2.000.000₫
                </span>
                <span class="promo-divider">|</span>
                <span class="promo-item">
                    <i class="fas fa-tag"></i>
                    Giảm giá lên đến 30%
                </span>
                <span class="promo-divider">|</span>
                <span class="promo-item">
                    <i class="fas fa-shield-alt"></i>
                    Đổi trả trong 30 ngày
                </span>
            </div>
        </div>

        <!-- Hero Carousel -->
        <div class="carousel-elegant" @mouseenter="showArrows = true" @mouseleave="showArrows = false">
            <a-carousel arrows autoplay :autoplaySpeed="5000" class="carousel" ref="carousel">
                <template #prevArrow>
                    <div class="carousel-arrow carousel-prev" :class="{ 'visible': showArrows }">
                        <left-outlined />
                    </div>
                </template>
                <template #nextArrow>
                    <div class="carousel-arrow carousel-next" :class="{ 'visible': showArrows }">
                        <right-outlined />
                    </div>
                </template>
                
                <div class="carousel-slide">
                    <img src="../images/banner/Banner-PC-3.png" alt="Banner chính">
                    <div class="slide-overlay"></div>
                </div>
                <div class="carousel-slide">
                    <img src="../images/banner/banner1.webp" alt="Banner sản phẩm">
                    <div class="slide-overlay"></div>
                </div>
                <div class="carousel-slide">
                    <img src="../images/banner/slider_2.jpg" alt="Banner khuyến mãi">
                    <div class="slide-overlay"></div>
                </div>
            </a-carousel>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { LeftOutlined, RightOutlined } from '@ant-design/icons-vue';
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
        { threshold: 0.1 } // Hiển thị khi ít nhất 10% phần tử xuất hiện trong viewport
    );
});
</script>

<style scoped>
/* ===================================================
   HERO BANNER - Elegant Men's Dress Shirt Design
   =================================================== */

.banner-elegant {
    margin-top: var(--space-md);
    opacity: 0;
    transform: translateY(20px);
    transition: opacity 0.6s ease, transform 0.6s ease;
}

.banner-elegant.visible {
    opacity: 1;
    transform: translateY(0);
}

/* ========== Promotional Banner (thay thế marquee) ========== */
.promo-banner {
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
    padding: var(--space-md) var(--space-lg);
    border-radius: var(--radius-lg);
    margin-bottom: var(--space-lg);
    box-shadow: var(--shadow-sm);
}

.promo-content {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-wrap: wrap;
    gap: var(--space-lg);
    font-family: var(--font-primary);
    color: var(--color-white);
    font-size: var(--text-sm);
    font-weight: var(--weight-medium);
}

.promo-item {
    display: inline-flex;
    align-items: center;
    gap: var(--space-sm);
    white-space: nowrap;
}

.promo-item i {
    font-size: var(--text-base);
    opacity: 0.9;
}

.promo-divider {
    color: rgba(255, 255, 255, 0.3);
    font-size: var(--text-lg);
}

/* Subtle hover effect cho promo items */
.promo-item:hover {
    opacity: 0.85;
    transition: opacity var(--transition-base);
}

/* ========== Hero Carousel ========== */
.carousel-elegant {
    position: relative;
    border-radius: var(--radius-lg);
    overflow: hidden;
    box-shadow: var(--shadow-md);
}

.carousel {
    width: 100%;
}

.carousel-slide {
    position: relative;
    height: 500px;
    overflow: hidden;
}

.carousel-slide img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.8s ease;
}

/* Subtle overlay for better text readability if needed */
.slide-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(
        to bottom,
        rgba(0, 0, 0, 0.05) 0%,
        rgba(0, 0, 0, 0) 20%,
        rgba(0, 0, 0, 0) 80%,
        rgba(0, 0, 0, 0.1) 100%
    );
    pointer-events: none;
}

/* Subtle zoom effect on hover */
.carousel-slide:hover img {
    transform: scale(1.02);
}

/* ========== Carousel Navigation Arrows ========== */
.carousel-arrow {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 48px;
    height: 48px;
    background-color: rgba(255, 255, 255, 0.9);
    color: var(--color-primary);
    border-radius: var(--radius-full);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
    font-size: var(--text-xl);
    opacity: 0;
    transition: all var(--transition-base);
    box-shadow: var(--shadow-md);
}

.carousel-arrow:hover {
    background-color: var(--color-primary);
    color: var(--color-white);
    transform: translateY(-50%) scale(1.1);
}

.carousel-arrow.visible {
    opacity: 1;
}

.carousel-prev {
    left: var(--space-lg);
}

.carousel-next {
    right: var(--space-lg);
}

/* ========== Ant Design Carousel Overrides ========== */
:deep(.slick-slide) {
    text-align: center;
    height: 500px;
    overflow: hidden;
}

:deep(.slick-dots) {
    bottom: var(--space-lg);
}

:deep(.slick-dots li button) {
    background: rgba(255, 255, 255, 0.5);
    width: 10px;
    height: 10px;
    border-radius: var(--radius-full);
    transition: all var(--transition-base);
}

:deep(.slick-dots li.slick-active button) {
    background: var(--color-white);
    width: 30px;
    border-radius: var(--radius-sm);
}

:deep(.slick-dots li button:hover) {
    background: var(--color-white);
}

/* ========== Responsive Design ========== */
@media (max-width: 1200px) {
    .carousel-slide {
        height: 450px;
    }
    
    :deep(.slick-slide) {
        height: 450px;
    }
}

@media (max-width: 992px) {
    .carousel-slide {
        height: 400px;
    }
    
    :deep(.slick-slide) {
        height: 400px;
    }
    
    .promo-content {
        font-size: var(--text-xs);
        gap: var(--space-md);
    }
}

@media (max-width: 768px) {
    .carousel-slide {
        height: 350px;
    }
    
    :deep(.slick-slide) {
        height: 350px;
    }
    
    .promo-banner {
        padding: var(--space-sm) var(--space-md);
    }
    
    .promo-content {
        flex-direction: column;
        gap: var(--space-sm);
        text-align: center;
    }
    
    .promo-divider {
        display: none;
    }
    
    .carousel-arrow {
        width: 40px;
        height: 40px;
        font-size: var(--text-lg);
    }
    
    .carousel-prev {
        left: var(--space-sm);
    }
    
    .carousel-next {
        right: var(--space-sm);
    }
}

@media (max-width: 576px) {
    .carousel-slide {
        height: 280px;
    }
    
    :deep(.slick-slide) {
        height: 280px;
    }
    
    .banner-elegant {
        margin-top: var(--space-sm);
    }
    
    .carousel-elegant {
        border-radius: var(--radius-md);
    }
}
</style>