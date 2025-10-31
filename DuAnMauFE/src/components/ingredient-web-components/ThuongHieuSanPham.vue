<template>
    <div class="thuong-hieu-container my-4" ref="sectionRef" :class="{ 'visible': isVisible }">
        <div class="container">
            <div class="section-header">
                <h2 class="section-title">Thương hiệu nổi bật</h2>
                <div class="section-divider">
                </div>
                <div class="row g-3 mt-2">
                    <!-- Hàng đầu tiên: 5 thương hiệu -->
                    <div class="col-6 col-md-4 col-lg">
                        <div class="brand-item">
                            <div class="brand-image">
                                <img src="../../images/thuongHieuSanPham/ThuongHieuNike.jpg" alt="Nike">
                            </div>
                        </div>
                    </div>

                    <div class="col-6 col-md-4 col-lg">
                        <div class="brand-item">
                            <div class="brand-image">
                                <img src="../../images/thuongHieuSanPham/ThuongHieuAdidas.jpg" alt="Adidas">
                            </div>
                        </div>
                    </div>

                    <div class="col-6 col-md-4 col-lg">
                        <div class="brand-item">
                            <div class="brand-image">
                                <img src="../../images/thuongHieuSanPham/ThuongHieuFila.jpg" alt="Fila">
                            </div>
                        </div>
                    </div>

                    <div class="col-6 col-md-4 col-lg">
                        <div class="brand-item">
                            <div class="brand-image">
                                <img src="../../images/thuongHieuSanPham/ThuongHieuSpeedo.jpg" alt="Speedo">
                            </div>
                        </div>
                    </div>

                    <div class="col-6 col-md-4 col-lg">
                        <div class="brand-item">
                            <div class="brand-image">
                                <img src="../../images/thuongHieuSanPham/ThuongHieuHoka.jpg" alt="Hoka">
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Hàng thứ hai: 5 thương hiệu -->
                <div class="row g-3 mt-2">
                    <div class="col-6 col-md-4 col-lg">
                        <div class="brand-item">
                            <div class="brand-image">
                                <img src="../../images/thuongHieuSanPham/ThuongHieuSkeche.jpg" alt="Skechers">
                            </div>
                        </div>
                    </div>

                    <div class="col-6 col-md-4 col-lg">
                        <div class="brand-item">
                            <div class="brand-image">
                                <img src="../../images/thuongHieuSanPham/ThuongHieuColombia.jpg" alt="Colombia">
                            </div>
                        </div>
                    </div>

                    <div class="col-6 col-md-4 col-lg">
                        <div class="brand-item">
                            <div class="brand-image">
                                <img src="../../images/thuongHieuSanPham/ThuongHieuAmor.jpg" alt="Amor">
                            </div>
                        </div>
                    </div>

                    <div class="col-6 col-md-4 col-lg">
                        <div class="brand-item">
                            <div class="brand-image">
                                <img src="../../images/thuongHieuSanPham/ThuongHieuCross.jpg" alt="Cross">
                            </div>
                        </div>
                    </div>

                    <div class="col-6 col-md-4 col-lg">
                        <div class="brand-item">
                            <div class="brand-image">
                                <img src="../../images/thuongHieuSanPham/ThuongHieuTeva.jpg" alt="Teva">
                            </div>
                        </div>
                    </div>
                </div>
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
.thuong-hieu-container {
    padding: 1rem 0;
    opacity: 0;
    transform: translateY(30px);
    transition: opacity 0.8s ease, transform 0.8s ease;
}

.thuong-hieu-container.visible {
    opacity: 1;
    transform: translateY(0);
}

.section-header {
    margin-bottom: 0.8rem;
    text-align: center;
}

.section-title {
    font-size: 1.5rem;
    font-weight: 600;
    color: #333;
    margin-bottom: 0.4rem;
}

.section-divider {
    width: 50px;
    height: 3px;
    background-color: #3a86ff;
    margin: 0 auto;
}

.brand-item {
    position: relative;
    overflow: hidden;
    border-radius: 10px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
    cursor: pointer;
    transition: all 0.3s ease;
    height: 100%;
    background-color: #fff;
    border: 1px solid rgba(0, 0, 0, 0.05);
    opacity: 0;
    transform: translateY(20px);
    transition: all 0.5s ease;
}

.brand-item:hover {
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
    transform: scale(0.97);
}

.brand-image {
    overflow: hidden;
    border-radius: 5px;
    height: 100px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f8f9fa;
}

.brand-image img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    transition: transform 0.4s ease;
    padding: 0.2rem;
}

.brand-item:hover .brand-image img {
    transform: scale(0.95);
}

/* Animation for items on page load */
.visible .col-6:nth-child(1) .brand-item {
    animation-delay: 0.1s;
    opacity: 1;
    transform: translateY(0);
    transition-delay: 0.1s;
}

.visible .col-6:nth-child(2) .brand-item {
    animation-delay: 0.2s;
    opacity: 1;
    transform: translateY(0);
    transition-delay: 0.2s;
}

.visible .col-6:nth-child(3) .brand-item {
    animation-delay: 0.3s;
    opacity: 1;
    transform: translateY(0);
    transition-delay: 0.3s;
}

.visible .col-6:nth-child(4) .brand-item {
    animation-delay: 0.4s;
    opacity: 1;
    transform: translateY(0);
    transition-delay: 0.4s;
}

.visible .col-6:nth-child(5) .brand-item {
    animation-delay: 0.5s;
    opacity: 1;
    transform: translateY(0);
    transition-delay: 0.5s;
}

.visible .col-6:nth-child(6) .brand-item {
    animation-delay: 0.6s;
    opacity: 1;
    transform: translateY(0);
    transition-delay: 0.6s;
}

.visible .col-6:nth-child(7) .brand-item {
    animation-delay: 0.7s;
    opacity: 1;
    transform: translateY(0);
    transition-delay: 0.7s;
}

.visible .col-6:nth-child(8) .brand-item {
    animation-delay: 0.8s;
    opacity: 1;
    transform: translateY(0);
    transition-delay: 0.8s;
}

.visible .col-6:nth-child(9) .brand-item {
    animation-delay: 0.9s;
    opacity: 1;
    transform: translateY(0);
    transition-delay: 0.9s;
}

.visible .col-6:nth-child(10) .brand-item {
    animation-delay: 1s;
    opacity: 1;
    transform: translateY(0);
    transition-delay: 1s;
}

/* Responsive adjustments */
@media (max-width: 992px) {
    .brand-image {
        height: 90px;
    }
}

@media (max-width: 768px) {
    .brand-image {
        height: 80px;
    }
}

@media (max-width: 576px) {
    .brand-image {
        height: 70px;
    }
}
</style>