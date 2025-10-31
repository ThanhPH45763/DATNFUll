<template>
    <div class="danh-muc-container my-4" ref="sectionRef" :class="{ 'visible': isVisible }">
        <div class="container">
            <div class="section-header">
                <h2 class="section-title">Phân loại sản phẩm</h2>
                <div class="section-divider">
                </div>
                <div class="row g-3 mt-2">
                    <div class="col-md-2"></div>
                    <div class="col-md-4">
                        <div class="category-item">
                            <div class="category-content" @click.prevent="handleSidebarClick(['Áo','Tank top'])">
                                <div class="category-image">
                                    <img src="../../images/danhMucSanPham/DanhMucAoNganTay.png" alt="Áo ngắn tay">
                                    <div class="category-overlay">
                                        <div class="overlay-content">
                                            <i class="fas fa-eye"></i>
                                            <span>Xem ngay</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="category-name">
                                    <span>Áo</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class=" col-md-4">
                        <div class="category-item">
                            <div class="category-content" @click.prevent="handleSidebarClick(['Quần','Váy'])">
                                <div class="category-image">
                                    <img  src="../../images/danhMucSanPham/DanhMucQuan.png" alt="Quần dài">
                                    <div class="category-overlay">
                                        <div class="overlay-content">
                                            <i class="fas fa-eye"></i>
                                            <span>Xem ngay</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="category-name">
                                    <span>Quần & Váy</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-2"></div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import '@fortawesome/fontawesome-free/css/all.min.css';
import { ref, onMounted } from 'vue';
import { useIntersectionObserver } from '@vueuse/core';
import { useGbStore } from '../../stores/gbStore';
import { useRouter } from 'vue-router';
const store = useGbStore();
const router = useRouter();
const isLoading = ref(false);
const sectionRef = ref(null);
const isVisible = ref(false);
// Truyền 1 hoặc nhiều keyword (dạng mảng hoặc chuỗi)
async function handleSidebarClick(keywords) {
  console.log('Click danh mục:', keywords);
  isLoading.value = true;
  await store.getSanPhamByTenSP(keywords);
  isLoading.value = false;
  router.push({ name: 'danhSachSanPham', query: { filter: keywords } });
}
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
.danh-muc-container {
    padding: 1rem 0;
    opacity: 0;
    transform: translateY(30px);
    transition: opacity 0.8s ease, transform 0.8s ease;
}

.danh-muc-container.visible {
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
    background: linear-gradient(90deg, #3a86ff, #5e60ce);
    margin: 0 auto;
    border-radius: 3px;
    margin-top: 10px;
}

.category-item {
    position: relative;
    overflow: hidden;
    border-radius: 10px;
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
    cursor: pointer;
    transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    height: 100%;
    padding: 0.5rem;
    background-color: #fff;
    border: 1px solid rgba(0, 0, 0, 0.05);
}

.category-item:hover {
    box-shadow: 0 5px 15px rgba(58, 134, 255, 0.15);
    transform: translateY(-5px);
    border-color: rgba(58, 134, 255, 0.2);
}

.category-content {
    display: flex;
    flex-direction: row;
    align-items: center;
    height: 100%;
}

.category-image {
    position: relative;
    overflow: hidden;
    border-radius: 8px;
    height: 80px;
    width: 80px;
    min-width: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f8f9fa;
    margin-right: 10px;
}

.category-image img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    transition: transform 0.5s ease;
    padding: 0.2rem;
    z-index: 1;
}

.category-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(to bottom, rgba(58, 134, 255, 0.7), rgba(94, 96, 206, 0.7));
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: all 0.4s ease;
    z-index: 2;
    transform: scale(0.9);
    border-radius: 8px;
}

.overlay-content {
    text-align: center;
    color: white;
    transform: translateY(10px);
    transition: all 0.3s ease;
}

.overlay-content i {
    font-size: 1.5rem;
    margin-bottom: 5px;
    display: block;
}

.overlay-content span {
    font-size: 0.9rem;
    font-weight: 500;
    display: block;
}

.category-item:hover .category-overlay {
    opacity: 1;
    transform: scale(1);
}

.category-item:hover .overlay-content {
    transform: translateY(0);
}

.category-item:hover .category-image img {
    transform: scale(1.1);
}

.category-name {
    flex: 1;
    text-align: left;
    font-weight: 600;
    color: #333;
    transition: all 0.3s ease;
    font-size: 0.9rem;
    position: relative;
    padding-bottom: 5px;
}

.category-name::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 0;
    height: 2px;
    background: linear-gradient(90deg, #3a86ff, #5e60ce);
    transition: all 0.3s ease;
}

.category-item:hover .category-name {
    color: #3a86ff;
}

.category-item:hover .category-name::after {
    width: 50%;
}

/* Animation for items on page load */
@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.col-6,
.col-md-4,
.col-lg,
.col-lg-2 {
    opacity: 0;
    transform: translateY(20px);
    transition: opacity 0.6s ease, transform 0.6s ease;
}

.visible .col-6,
.visible .col-md-4,
.visible .col-lg,
.visible .col-lg-2 {
    opacity: 1;
    transform: translateY(0);
}

.visible .col-6:nth-child(1),
.visible .col-lg:nth-child(1) {
    transition-delay: 0.1s;
}

.visible .col-6:nth-child(2),
.visible .col-lg:nth-child(2) {
    transition-delay: 0.2s;
}

.visible .col-6:nth-child(3),
.visible .col-lg:nth-child(3) {
    transition-delay: 0.3s;
}

.visible .col-6:nth-child(4),
.visible .col-lg:nth-child(4) {
    transition-delay: 0.4s;
}

.visible .col-6:nth-child(5),
.visible .col-lg:nth-child(5) {
    transition-delay: 0.5s;
}

.visible .col-6:nth-child(6),
.visible .col-lg-2:nth-child(1) {
    transition-delay: 0.6s;
}

.visible .col-6:nth-child(7),
.visible .col-lg-2:nth-child(2) {
    transition-delay: 0.7s;
}

/* Responsive adjustments */
@media (max-width: 992px) {
    .category-image {
        width: 70px;
        height: 70px;
        min-width: 70px;
    }

    .overlay-content i {
        font-size: 1.2rem;
    }

    .overlay-content span {
        font-size: 0.8rem;
    }
}

@media (max-width: 768px) {
    .category-image {
        width: 60px;
        height: 60px;
        min-width: 60px;
    }

    .category-name {
        font-size: 0.8rem;
    }
}

@media (max-width: 576px) {
    .category-image {
        width: 50px;
        height: 50px;
        min-width: 50px;
    }

    .overlay-content i {
        font-size: 1rem;
    }

    .overlay-content span {
        font-size: 0.7rem;
    }
}
</style>