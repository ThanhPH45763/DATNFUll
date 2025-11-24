<template class="sticky-top w-100">
    <div class="sidebar-fixed">
        <nav class="navbar navbar-expand-lg bg-body-tertiary">
            <div class="container-fluid">
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarScroll"
                    aria-controls="navbarScroll" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarScroll">
                    <ul class="navbar-nav mx-auto my-2 my-lg-0 navbar-nav-scroll" style="--bs-scroll-height: 70px;">
                        <li class="nav-item me-lg-5">
                            <a class="nav-link" style="cursor: pointer;" @click.prevent="handleSidebarClick('all')">Tất cả sản phẩm</a>
                        </li>
                        <li class="nav-item me-lg-5">
                            <!-- <a class="nav-link" style="cursor: pointer;" @click.prevent="handleSidebarClick('Sport')">Môn thể thao</a> -->
                        </li>
                        <li class="nav-item me-lg-5">
                            <!-- <a class="nav-link" style="cursor: pointer;" @click.prevent="handleSidebarClick('Nam')">Nam</a> -->
                        </li>
                        <li class="nav-item me-lg-5">
                            <!-- <a class="nav-link" style="cursor: pointer;" @click.prevent="handleSidebarClick('Nữ')">Nữ</a> -->
                        </li>
                        <li class="nav-item me-lg-5">
                            <a class="nav-link" style="cursor: pointer;" @click.prevent="handleSieuSaleClick">Siêu sale sập sàn</a>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>

    </div>
</template>

<script setup>
import { ChevronDown } from 'lucide-vue-next';
import { useGbStore } from '../stores/gbStore';
import { useRouter } from 'vue-router';
import { ref } from 'vue';

const router = useRouter();
const store = useGbStore();
const isLoading = ref(false);

// Giữ lại hàm handleSidebarClick cho các mục khác
async function handleSidebarClick(keywords) {
  isLoading.value = true;
  if (keywords === 'all') {
    await store.getSanPhamByTenDM('');
  } else if (keywords === 'Sport') {
    const sports = ['Bóng đá', 'Bóng rổ', 'Cầu lông', 'Đạp xe', 'Chạy bộ', 'Yoga'];
    await store.getSanPhamByTenDM(sports);
  } else {
    await store.getSanPhamByTenDM(keywords);
  }
  isLoading.value = false;
  router.push({ path: '/danhSachSanPham', query: { filter: keywords } });
}

// Đơn giản hóa hàm handleSieuSaleClick, chỉ lấy sản phẩm và chuyển trang
async function handleSieuSaleClick() {
  try {
    isLoading.value = true;
    await store.getSanPhamSieuSale();
    console.log('Đã lấy sản phẩm siêu sale'); // Log để kiểm tra
    router.push({ 
      path: '/danhSachSanPham', 
      query: { filter: 'supersale' } 
    });
  } catch (error) {
    console.error('Lỗi khi lấy sản phẩm siêu sale:', error);
  } finally {
    isLoading.value = false;
  }
}
</script>

<style scoped>
.sidebar-fixed {
    position: fixed;
    top: 5rem;
    /* Chiều cao của header */
    left: 0;
    right: 0;
    z-index: 1020;
    background: white;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.nav-item>a {
    position: relative;
    /* Đặt vị trí tương đối để pseudo-element hoạt động */
    color: rgb(58, 43, 82);
    /* Màu chữ */
    text-decoration: none;
    /* Xóa gạch chân mặc định */
    transition: color 0.3s ease-in-out;
    /* Hiệu ứng chuyển màu */
}

.nav-item>a::before {
    content: '';
    /* Bắt buộc để hiển thị pseudo-element */
    position: absolute;
    bottom: 0;
    /* Đặt pseudo-element ở dưới cùng */
    left: 0;
    /* Bắt đầu từ bên trái */
    width: 0%;
    /* Bắt đầu với chiều rộng bằng 0 */
    height: 3px;
    /* Độ dày của border */
    background-color: rgb(47, 47, 47);
    /* Màu của border */
    transition: width 0.3s ease-in-out;
    /* Hiệu ứng mở rộng từ trái sang phải */
}

.nav-item>a:hover::before {
    width: 100%;
    /* Khi hover, border mở rộng toàn bộ chiều rộng */
}

/* CSS cho responsive */
@media (max-width: 991.98px) {
    .navbar-collapse {
        background-color: white;
        padding: 1rem;
        border-radius: 0.25rem;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    }
    
    .navbar-nav {
        width: 100%;
    }
    
    .nav-item {
        margin-bottom: 0.5rem;
    }
}

/* Đảm bảo các menu con không hiển thị khi sidebar đã collapse */
@media (max-width: 991.98px) {
    .sidebar-fixed > nav {
        z-index: 1030;
    }
}
</style>