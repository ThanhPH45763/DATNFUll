<template>
    <div class="" style="max-height: 280px;">
        <div class="modal fade searchModal" id="staticBackdrop" tabindex="-1" role="dialog"
            :style="{ display: store.id === 4 ? 'block' : 'none' }" :class="{ show: store.id }">
            <div class="container-fluid" @mouseleave="store.hideModalSideBar(0)">
                <div class="modal-content bg-light text-dark row d-flex justify-content-evenly m-0">
                    <div class="row sports-categories justify-content-center">
                        <!-- Menu -->
                        <div class="col-md-2 px-1">
                            <div class="category-section">
                                <div class="modal-header d-flex justify-content-between ps-0 pe-0">
                                    <div class="">
                                        <span class="category-title">Phụ kiện tập luyện</span>
                                    </div>
                                    <div class="">
                                        <span class="view-all"><a href="" style="color: #f33b47; font-weight: bolder;" class="nav-link">XEM TẤT CẢ</a></span>
                                    </div>
                                </div>
                                <div class="modal-body p-0 m-0 mt-2">
                                    <p class="product-item"><a href="" class="nav-link">Băng quấn cổ tay</a></p>
                                    <p class="product-item"><a href="" class="nav-link">Băng trán</a></p>
                                    <p class="product-item"><a href="" class="nav-link">Găng tay tập gym</a></p>
                                    <p class="product-item"><a href="" class="nav-link">Dây nhảy</a></p>
                                    <p class="product-item"><a href="" class="nav-link">Băng bảo vệ đầu gối</a></p>
                                </div>
                            </div>
                        </div>
                        <!-- End Menu -->

                        <!-- Menu -->
                        <div class="col-md-2 px-1">
                            <div class="category-section">
                                <div class="modal-header d-flex justify-content-between ps-0 pe-0">
                                    <div class="">
                                        <span class="category-title">Phụ kiện bảo vệ</span>
                                    </div>
                                    <div class="">
                                        <span class="view-all"><a href="" style="color: #f33b47; font-weight: bolder;" class="nav-link">XEM TẤT CẢ</a></span>
                                    </div>
                                </div>
                                <div class="modal-body p-0 m-0 mt-2">
                                    <p class="product-item"><a href="" class="nav-link">Mũ bảo hiểm</a></p>
                                    <p class="product-item"><a href="" class="nav-link">Bọc gối</a></p>
                                    <p class="product-item"><a href="" class="nav-link">Bọc khuỷu tay</a></p>
                                    <p class="product-item"><a href="" class="nav-link">Bọc ống chân</a></p>
                                </div>
                            </div>
                        </div>
                        <!-- End Menu -->

                        <!-- Menu -->
                        <div class="col-md-2 px-1">
                            <div class="category-section">
                                <div class="modal-header d-flex justify-content-between ps-0 pe-0">
                                    <div class="">
                                        <span class="category-title">Thời trang thể thao</span>
                                    </div>
                                    <div class="">
                                        <span class="view-all"><a href="" style="color: #f33b47; font-weight: bolder;" class="nav-link">XEM TẤT CẢ</a></span>
                                    </div>
                                </div>
                                <div class="modal-body p-0 m-0 mt-2">
                                    <p class="product-item"><a href="" class="nav-link">Nón lưỡi trai</a></p>
                                    <p class="product-item"><a href="" class="nav-link">Kính mát thể thao</a></p>
                                    <p class="product-item"><a href="" class="nav-link">Túi đeo chéo thể thao</a></p>
                                    <p class="product-item"><a href="" class="nav-link">Đồng hồ thể thao</a></p>
                                </div>
                            </div>
                        </div>
                        <!-- End Menu -->

                        <!-- Menu -->
                        <div class="col-md-2 px-1">
                            <div class="category-section">
                                <div class="modal-header d-flex justify-content-between ps-0 pe-0">
                                    <div class="">
                                        <span class="category-title">Đo lường & sức khỏe</span>
                                    </div>
                                    <div class="">
                                        <span class="view-all"><a href="" style="color: #f33b47; font-weight: bolder;" class="nav-link">XEM TẤT CẢ</a></span>
                                    </div>
                                </div>
                                <div class="modal-body p-0 m-0 mt-2">
                                    <p class="product-item"><a href="" class="nav-link">Đồng hồ thông minh</a></p>
                                    <p class="product-item"><a href="" class="nav-link">Vòng đeo tay fitness</a></p>
                                    <p class="product-item"><a href="" class="nav-link">Máy đo nhịp tim</a></p>
                                </div>
                            </div>
                        </div>
                        <!-- End Menu -->

                        <!-- Menu -->
                        <div class="col-md-2 px-1">
                            <div class="category-section">
                                <div class="modal-header d-flex justify-content-between ps-0 pe-0">
                                    <div class="">
                                        <span class="category-title">Dụng cụ mang nước</span>
                                    </div>
                                    <div class="">
                                        <span class="view-all"><a href="" style="color: #f33b47; font-weight: bolder;" class="nav-link">XEM TẤT CẢ</a></span>
                                    </div>
                                </div>
                                <div class="modal-body p-0 m-0 mt-2">
                                    <p class="product-item"><a href="" class="nav-link">Bình nước thể thao</a></p>
                                    <p class="product-item"><a href="" class="nav-link">Túi nước chạy bộ</a></p>
                                </div>
                            </div>
                        </div>
                        <!-- End Menu -->
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useGbStore } from '../stores/gbStore';
import { onMounted, onUnmounted } from 'vue';

const store = useGbStore();

// Thêm hàm xử lý sự kiện scroll
const handleScroll = () => {
    if (store.id === 4) {
        store.hideModalSideBar(0);
    }
};

// Thêm event listener khi component được mount
onMounted(() => {
    window.addEventListener('scroll', handleScroll);
});

// Xóa event listener khi component bị unmount để tránh memory leak
onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll);
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&display=swap');

.searchModal {
    margin-top: 8.5rem;
    animation: slideDown 0.3s ease-out;
    width: 100%;
}

.modal-content {
    border-radius: 12px;
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
    padding: 20px 15px;
    background: linear-gradient(to bottom, #ffffff, #f8f9fa) !important;
    border: 1px solid #e9ecef;
}

.sports-categories {
    display: flex;
    flex-wrap: wrap;
}

.category-section {
    padding: 0 10px;
    border-radius: 8px;
    transition: all 0.3s ease;
    height: 100%;
}

.category-section:hover {
    background-color: rgba(58, 134, 255, 0.05);
}

@keyframes slideDown {
    from {
        transform: translateY(-20px);
        opacity: 0;
    }

    to {
        transform: translateY(0);
        opacity: 1;
    }
}

.category-title {
    font-family: 'Montserrat', sans-serif;
    font-size: 16px;
    font-weight: 600;
    color: #333;
    letter-spacing: 0.5px;
    position: relative;
    padding-bottom: 5px;
}

.category-title::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 30px;
    height: 2px;
    background-color: #3a86ff;
}

.view-all {
    font-family: 'Montserrat', sans-serif;
    font-size: 9px;
    font-weight: 500;
}

.product-item {
    font-family: 'Montserrat', sans-serif;
    font-size: 13px;
    margin-bottom: 8px;
    transition: all 0.2s ease;
}

.nav-link {
    color: #555;
    transition: all 0.2s ease-in-out;
    position: relative;
    display: inline-block;
    padding: 2px 0;
}

.nav-link:hover {
    color: #3a86ff;
    text-decoration: none;
    transform: translateX(3px);
    font-weight: 500;
}

.nav-link::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 0;
    height: 1px;
    background-color: #3a86ff;
    transition: width 0.3s ease;
}

.nav-link:hover::after {
    width: 100%;
}

.modal-header {
    border-bottom: none;
    padding-bottom: 10px;
}

.modal-body {
    padding-top: 5px;
}

.img {
    overflow: hidden;
    border-radius: 8px;
}

.imgProduct {
    overflow: hidden;
    transition: transform 0.3s ease-in-out;
    border-radius: 8px;
}

.imgProduct:hover {
    transform: scale3d(1.05, 1.05, 1.05);
}

.iconShopingCart {
    cursor: pointer;
    transition: transform 0.1s ease-in-out;
}

.boxShoping:hover {
    border-radius: 50%;
    background-color: rgb(138, 138, 138);
    cursor: pointer;
}

.boxShoping {
    padding: 11px 15px;
    transition: transform 0.2s ease-in-out;
}

.iconShopingCart,
.boxShoping:hover {
    transform: scale3d(1.1, 1.1, 1.1)
}
</style>