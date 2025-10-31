<template>
    <div class="new-products" ref="sectionRef" :class="{ 'visible': isVisible }">
        <div class="container">
            <div class="section-header">
                <h4 class="section-title text-center">[New] Sản phẩm mới cho tất cả</h4>
                <div class="section-divider"></div>
                <div class="banner-row row mt-4">
                      
                    <div class="col-md-1">
                    </div>
                    <div class="col-md-5 banner-col" style="cursor: pointer;">
                        <img @click.prevent="handleSidebarClick('Nam')" src="../../images/banerSale/saleNam.webp" alt="Sale Nam" class="banner-image">
                    </div>
                    <div class="col-md-5 banner-col" style="cursor: pointer;">
                        <img @click.prevent="handleSidebarClick(['Nữ'])" src="../../images/banerSale/saleNu.webp" alt="Sale Nữ" class="banner-image">
                    </div>
                    <div class="col-md-1">
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useIntersectionObserver } from '@vueuse/core';
import { useRouter } from 'vue-router';
import { useGbStore } from '../../stores/gbStore';
const sectionRef = ref(null);
const isVisible = ref(false);
const router = useRouter();
const store = useGbStore();
const isLoading = ref(false);

// Truyền 1 hoặc nhiều keyword (dạng mảng hoặc chuỗi)
async function handleSidebarClick(keywords) {
  isLoading.value = true;
  await store.getSanPhamByTenDM(keywords);
  isLoading.value = false;
  // Truyền keyword qua query, nếu là mảng sẽ thành filter[]=a&filter[]=b
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
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&display=swap');

.new-products {
    font-family: 'Montserrat', sans-serif;
    padding: 2rem 0;
    background-color: #f8f9fa;
    opacity: 0;
    transform: translateY(30px);
    transition: opacity 0.8s ease, transform 0.8s ease;
}

.new-products.visible {
    opacity: 1;
    transform: translateY(0);
}

.section-header {
    margin-bottom: 1.5rem;
    text-align: center;
}

.section-title {
    font-size: 1.5rem;
    font-weight: 600;
    color: #333;
    margin-bottom: 0.5rem;
}

.section-divider {
    height: 3px;
    width: 100px;
    background-color: #3a86ff;
    margin: 0 auto;
}

.banner-row {
    margin-top: 1.5rem;
}

.banner-col {
    padding: 0.5rem;
    opacity: 0;
    transform: translateY(20px);
    transition: all 0.5s ease;
}

.visible .banner-col {
    opacity: 1;
    transform: translateY(0);
}

.visible .banner-col:nth-child(1) {
    transition-delay: 0.1s;
}

.visible .banner-col:nth-child(2) {
    transition-delay: 0.3s;
}

.visible .banner-col:nth-child(3) {
    transition-delay: 0.5s;
}

.banner-image {
    width: 100%;
    height: 200px;
    object-fit: cover;
    border-radius: 8px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
}

.banner-image:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 15px rgba(0, 0, 0, 0.15);
}

@media (max-width: 768px) {
    .banner-col {
        margin-bottom: 15px;
    }

    .banner-image {
        height: 150px;
    }
}
</style>