<template>
    <div class="hot-collections mt-3" ref="sectionRef" :class="{ 'visible': isVisible }">
        <div class="container">
            <div class="section-header">
                <h2 class="section-title">Bộ sưu tập hot</h2>
                <div class="section-divider"></div>
            </div>
            <div class="d-flex justify-content-around mt-2">
                <a href="nav-link" class="collection-item">
                    <img src="../../images/icon-hot/BF_circle_golden_hour.webp" alt="">
                </a>
                <a href="nav-link" class="collection-item">
                    <img src="../../images/icon-hot/Frame 17.webp" alt="">
                </a>
                <a href="nav-link" class="collection-item">
                    <img src="../../images/icon-hot/Frame 18.webp" alt="">
                </a>
                <a href="nav-link" class="collection-item">
                    <img src="../../images/icon-hot/Frame 19.webp" alt="">
                </a>
                <a href="nav-link" class="collection-item">
                    <img src="../../images/icon-hot/Frame 26.webp" alt="">
                </a>
                <a href="nav-link" class="collection-item">
                    <img src="../../images/icon-hot/Frame 25.webp" alt="">
                </a>
                <a href="nav-link" class="collection-item">
                    <img src="../../images/icon-hot/Frame 21.webp" alt="">
                </a>
                <a href="nav-link" class="collection-item">
                    <img src="../../images/icon-hot/Frame 28 (2).webp" alt="">
                </a>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useIntersectionObserver } from '@vueuse/core';

const sectionRef = ref(null);
const isVisible = ref(false);

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
.hot-collections {
    padding: 1rem 0;
    opacity: 0;
    transform: translateY(30px);
    transition: opacity 0.8s ease, transform 0.8s ease;
}

.hot-collections.visible {
    opacity: 1;
    transform: translateY(0);
}

.section-header {
    margin-bottom: 1rem;
    text-align: center;
}

.section-title {
    font-size: 1.5rem;
    font-weight: 600;
    color: #333;
    margin-bottom: 0.5rem;
}

.section-divider {
    width: 50px;
    height: 3px;
    background: linear-gradient(90deg, #3a86ff, #5e60ce);
    margin: 0 auto;
    border-radius: 3px;
}

.collection-item {
    opacity: 0;
    transform: translateY(20px);
    transition: all 0.5s ease;
}

.visible .collection-item {
    opacity: 1;
    transform: translateY(0);
}

.visible .collection-item:nth-child(1) {
    transition-delay: 0.1s;
}

.visible .collection-item:nth-child(2) {
    transition-delay: 0.2s;
}

.visible .collection-item:nth-child(3) {
    transition-delay: 0.3s;
}

.visible .collection-item:nth-child(4) {
    transition-delay: 0.4s;
}

.visible .collection-item:nth-child(5) {
    transition-delay: 0.5s;
}

.visible .collection-item:nth-child(6) {
    transition-delay: 0.6s;
}

.visible .collection-item:nth-child(7) {
    transition-delay: 0.7s;
}

.visible .collection-item:nth-child(8) {
    transition-delay: 0.8s;
}

.visible .collection-item:nth-child(9) {
    transition-delay: 0.9s;
}

img {
    width: 70px;
    height: 70px;
    object-fit: cover;
    border-radius: 50%;
    transition: all 0.3s ease;
}

img:hover {
    transform: scale(1.1);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
    img {
        width: 60px;
        height: 60px;
    }
}

@media (max-width: 576px) {
    img {
        width: 45px;
        height: 45px;
    }
}
</style>