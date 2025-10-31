<template>
    <div class="container">
        <div class="modal fade searchModal" id="staticBackdrop" :style="{ display: store.status ? 'block' : 'none' }"
            :class="{ show: store.status }" @click.self="store.showModal(false)" tabindex="-1" role="dialog">
            <div class="container">
                <div class="modal-wrapper bg-light text-dark" @wheel.stop>
                    <div class="modal-header d-flex align-items-center justify-content-between">
                        <div class="d-flex align-items-center">
                            <Search class="search-icon me-2" />
                            <h1 class="modal-title">Kết quả tìm kiếm phổ biến</h1>
                        </div>
                        <button type="button" @click="store.showModal(false)" class="close-btn" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body row">
                        <div class="col-sm-4 trending-section">
                            <div class="section-header">
                                <h5 class="section-title">Top tìm kiếm</h5>
                                <div class="section-divider"></div>
                            </div>
                            <ul class="trending-list">
                                <li class="trending-item" 
                                    v-for="(item, index) in trendingSearches" 
                                    :key="index"
                                    @mouseenter="hoverItem = index" 
                                    @mouseleave="hoverItem = null"
                                    @click="handleSearchSuggestion(item)">
                                    <div class="trending-item-content">
                                        <Search :class="{ 'search-icon-highlight': hoverItem === index }" />
                                        <span class="trending-text">{{ item }}</span>
                                    </div>
                                    <div class="trending-item-hover"></div>
                                </li>
                            </ul>
                        </div>
                        <div class="col-sm-8 suggestions-section">
                            <div class="section-header">
                                <h5 class="section-title">Gợi ý cho bạn</h5>
                                <div class="section-divider"></div>
                            </div>
                            <div class="row products-grid">
                                <div class="col-sm-4 product-card" v-for="(product, index) in suggestedProducts"
                                    :key="index" @mouseenter="activeProduct = index" @mouseleave="activeProduct = null">
                                    <div class="product-image-container">
                                        <img class="product-image" :src="product.image" alt="Product image">
                                        <div class="product-overlay" :class="{ 'active': activeProduct === index }">
                                            <div class="overlay-buttons">
                                                <button class="overlay-btn view-btn">
                                                    <Eye size="18" />
                                                    <span>Xem</span>
                                                </button>
                                                <button class="overlay-btn cart-btn">
                                                    <ShoppingCart size="18" />
                                                    <span>Thêm</span>
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="product-info">
                                        <div class="product-price-row">
                                            <span class="product-price">{{ product.price }}</span>
                                            <span class="product-old-price">{{ product.oldPrice }}</span>
                                            <span class="product-discount">{{ product.discount }}</span>
                                        </div>
                                        <h6 class="product-name">{{ product.name }}</h6>
                                        <div class="product-meta">
                                            <span class="product-brand">{{ product.brand }}</span>
                                            <div class="product-rating">
                                                <i class="bi bi-star-fill"></i>
                                                <span>{{ product.rating }} ({{ product.reviews }})</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <div class="search-tags">
                            <span class="search-tag" v-for="(tag, index) in popularTags" :key="index">{{ tag }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useGbStore } from '../stores/gbStore';
import { Search, ShoppingCart, Eye } from 'lucide-vue-next';
import { ref, watch } from 'vue';

const store = useGbStore();
const hoverItem = ref(null);
const activeProduct = ref(null);

// Xử lý ngăn cuộn trang nền khi modal hiển thị
watch(() => store.status, (newValue) => {
    if (newValue) {
        document.body.style.overflow = 'hidden';
    } else {
        document.body.style.overflow = '';
    }
}, { immediate: true });

// Dữ liệu mẫu
const trendingSearches = ref([
    'Giày thể thao nam',
    'Áo thun thể thao nữ',
    'Quần legging tập gym',
    'Bóng đá size 5',
    'Găng tay tập gym',
    'Áo khoác thể thao',
    'Vợt cầu lông',
    'Túi đựng giày',
    'Băng đô thể thao'
]);
//fake giao diện sản phẩm gợi ý
const suggestedProducts = ref([
    {
        image: 'http://res.cloudinary.com/dtwsqkqpc/image/upload/v1742823877/oionww3qsqhfwvuvxeko.jpg',
        price: '999.000₫',
        oldPrice: '2.000.000₫',
        discount: '-50%',
        name: 'Áo khoác nỉ leo núi nam giữ ấm - MH100 xanh lá',
        brand: 'ADIDAS',
        rating: '4.9',
        reviews: '1.2k'
    },
    {
        image: '../images/product/shoe/anhGiayTheThao.webp',
        price: '999.000₫',
        oldPrice: '2.000.000₫',
        discount: '-50%',
        name: 'Áo khoác nỉ leo núi nam giữ ấm - MH100 xanh lá',
        brand: 'ADIDAS',
        rating: '4.9',
        reviews: '1.2k'
    },
    {
        image: '../images/product/shoe/anhGiayTheThao.webp',
        price: '999.000₫',
        oldPrice: '2.000.000₫',
        discount: '-50%',
        name: 'Áo khoác nỉ leo núi nam giữ ấm - MH100 xanh lá',
        brand: 'ADIDAS',
        rating: '4.9',
        reviews: '1.2k'
    }
]);

const popularTags = ref([
    'Thể thao nam', 'Thể thao nữ', 'Giày chạy bộ', 'Áo thun', 'Quần short', 'Phụ kiện gym', 'Sale 50%'
]);

const handleSearchSuggestion = async (keyword) => {
  try {
    store.isProductLoading = true;
    // Gọi API tìm kiếm từ store
    await store.getSanPhamByTenSP(keyword);
    // Đóng modal
    store.showModal(false);
  } catch (error) {
    console.error('Lỗi khi tìm kiếm:', error);
  } finally {
    store.isProductLoading = false;
  }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&display=swap');

.searchModal {
    margin-top: 5rem;
    animation: fadeInDown 0.4s ease-out;
    backdrop-filter: blur(5px);
    overflow-y: auto;
    max-height: 80vh;
}

.modal-wrapper {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
    border: 1px solid rgba(230, 230, 230, 0.7);
    background: linear-gradient(to bottom, #ffffff, #f8f9fa);
    position: relative;
}

.modal-header {
    padding: 20px 25px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.modal-title {
    font-family: 'Montserrat', sans-serif;
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0;
}

.close-btn {
    background: none;
    border: none;
    font-size: 24px;
    color: #666;
    cursor: pointer;
    transition: all 0.2s ease;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.close-btn:hover {
    background-color: rgba(0, 0, 0, 0.05);
    color: #333;
}

.search-icon {
    color: #666;
}

.search-icon-highlight {
    color: #3a86ff;
}

.modal-body {
    padding: 0;
}

.section-header {
    padding: 15px 25px;
    position: relative;
    margin-bottom: 10px;
}

.section-title {
    font-family: 'Montserrat', sans-serif;
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0;
}

.section-divider {
    width: 40px;
    height: 3px;
    background-color: #3a86ff;
    margin-top: 8px;
}

.trending-section {
    border-right: 1px solid rgba(0, 0, 0, 0.05);
    padding: 0;
    height: 100%;
}

.trending-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.trending-item {
    position: relative;
    padding: 12px 25px;
    cursor: pointer;
    transition: all 0.2s ease;
    overflow: hidden;
}

.trending-item-content {
    display: flex;
    align-items: center;
    position: relative;
    z-index: 2;
}

.trending-item-hover {
    position: absolute;
    top: 0;
    left: 0;
    width: 0;
    height: 100%;
    background-color: rgba(58, 134, 255, 0.1);
    transition: width 0.3s ease;
    z-index: 1;
}

.trending-item:hover .trending-item-hover {
    width: 100%;
}

.trending-text {
    font-family: 'Montserrat', sans-serif;
    font-size: 14px;
    color: #555;
    margin-left: 12px;
    transition: all 0.2s ease;
}

.trending-item:hover .trending-text {
    color: #3a86ff;
    transform: translateX(5px);
}

.suggestions-section {
    padding: 0;
}

.products-grid {
    padding: 0 15px;
}

.product-card {
    padding: 15px;
    transition: all 0.3s ease;
    border-radius: 8px;
    margin-bottom: 20px;
}

.product-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
}

.product-image-container {
    position: relative;
    overflow: hidden;
    border-radius: 8px;
    margin-bottom: 12px;
}

.product-image {
    width: 100%;
    height: auto;
    transition: transform 0.5s ease;
}

.product-card:hover .product-image {
    transform: scale(1.05);
}

.product-overlay {
    position: absolute;
    bottom: -50px;
    left: 0;
    width: 100%;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
    padding: 15px;
    transition: all 0.3s ease;
    opacity: 0;
}

.product-overlay.active {
    bottom: 0;
    opacity: 1;
}

.overlay-buttons {
    display: flex;
    justify-content: space-around;
}

.overlay-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    background: white;
    border: none;
    border-radius: 20px;
    padding: 5px 12px;
    font-size: 12px;
    font-weight: 500;
    color: #333;
    cursor: pointer;
    transition: all 0.2s ease;
}

.overlay-btn span {
    margin-left: 5px;
}

.overlay-btn:hover {
    background: #3a86ff;
    color: white;
}

.product-info {
    padding: 0 5px;
}

.product-price-row {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
}

.product-price {
    font-family: 'Montserrat', sans-serif;
    font-size: 15px;
    font-weight: 600;
    color: #333;
    margin-right: 8px;
}

.product-old-price {
    font-family: 'Montserrat', sans-serif;
    font-size: 12px;
    color: #999;
    text-decoration: line-through;
    margin-right: 8px;
}

.product-discount {
    font-family: 'Montserrat', sans-serif;
    font-size: 11px;
    font-weight: 600;
    color: white;
    background-color: #ff3a3a;
    padding: 2px 6px;
    border-radius: 10px;
}

.product-name {
    font-family: 'Montserrat', sans-serif;
    font-size: 13px;
    font-weight: 600;
    color: #333;
    margin-bottom: 8px;
    line-height: 1.4;
    height: 36px;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

.product-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.product-brand {
    font-family: 'Montserrat', sans-serif;
    font-size: 12px;
    font-weight: 500;
    color: #666;
}

.product-rating {
    display: flex;
    align-items: center;
    font-family: 'Montserrat', sans-serif;
    font-size: 12px;
    color: #666;
}

.product-rating i {
    color: #ffc107;
    margin-right: 3px;
}

.modal-footer {
    padding: 15px 25px;
    border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.search-tags {
    display: flex;
    flex-wrap: wrap;
}

.search-tag {
    font-family: 'Montserrat', sans-serif;
    font-size: 12px;
    color: #555;
    background-color: rgba(0, 0, 0, 0.05);
    padding: 5px 12px;
    border-radius: 20px;
    margin-right: 8px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: all 0.2s ease;
}

.search-tag:hover {
    background-color: #3a86ff;
    color: white;
}

@keyframes fadeInDown {
    from {
        opacity: 0;
        transform: translateY(-20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>