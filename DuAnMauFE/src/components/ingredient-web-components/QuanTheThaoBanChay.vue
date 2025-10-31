<template>
    <div class="best-selling-products" ref="sectionRef" :class="{ 'visible': isVisible }">
        <div class="container p-0">
            <div class="section-header">
                <h4 class="section-title">Quần thể thao HOT nhất</h4>
                <div class="section-divider"></div>
            </div>

            <div class="carousel-container" @mouseenter="showArrows = true" @mouseleave="showArrows = false">
                <button class="custom-arrow prev-arrow" @click="prevSlide" :class="{ 'visible': showArrows }">
                    <left-outlined />
                </button>
                <button class="custom-arrow next-arrow" @click="nextSlide" :class="{ 'visible': showArrows }">
                    <right-outlined />
                </button>

                <a-carousel ref="carousel" autoplay dots-class="custom-dots">
                    <div v-for="(slideGroup, groupIndex) in productSlides" :key="groupIndex">
                        <div class="row products-grid">
                            <div class="col product-card" v-for="(product, index) in slideGroup" :key="index"
                                @mouseenter="activeProduct = product.id" @mouseleave="activeProduct = null">
                                <div class="product-image-container">
                                    <img class="product-image" :src="product.image" alt="Product image">
                                    <div class="discount-badge" v-if="product.discountPercent">
                                        -{{ product.discountPercent }}%
                                    </div>
                                    <div class="product-overlay" :class="{ 'active': activeProduct === product.id }">
                                        <div class="overlay-buttons">
                                            <router-link
                                                :to="{ name: 'sanPhamDetail-BanHang', params: { id: product.id } }"
                                                class="overlay-btn view-btn">
                                                <eye-outlined />
                                                <span>Xem</span>
                                            </router-link>
                                            <button class="overlay-btn cart-btn" @click="showProductDetail(product)">
                                                <shopping-cart-outlined />
                                                <span>Thêm</span>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                                <div class="product-info">
                                    <div class="product-price-row">
                                        <span class="product-price">{{ product.price }}</span>
                                        <span class="product-old-price" v-if="product.oldPrice">{{ product.oldPrice
                                            }}</span>
                                        <span class="product-discount" v-if="product.discount">{{ product.discount
                                            }}</span>
                                    </div>
                                    <h6 class="product-name">{{ product.name }}</h6>
                                    <div class="product-meta">
                                        <span class="product-brand">{{ product.brand }}</span>
                                        <div class="product-rating">
                                            <star-filled />
                                            <span>{{ product.rating }} ({{ product.reviews }})</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </a-carousel>
            </div>
        </div>
    </div>

    <!-- Product Detail Modal -->
    <a-modal v-model:visible="modalVisible" :title="selectedProduct?.name" width="800px" :footer="null"
        @cancel="handleModalCancel" :zIndex="9999" :maskStyle="{ zIndex: 9998 }" :wrapStyle="{ zIndex: 9999 }" centered
        :style="{ top: '20px' }">
        <div class="product-detail-modal">
            <div class="product-detail-content">
                <div class="product-images">
                    <div class="main-image">
                        <img :src="selectedProduct?.image" :alt="selectedProduct?.name">
                    </div>
                    <div class="thumbnail-images" v-if="selectedProduct?.variants">
                        <img v-for="(variant, index) in selectedProduct.variants" :key="index" :src="variant.image"
                            :alt="variant.name" @click="selectedProduct.image = variant.image">
                    </div>
                </div>
                <div class="product-info-detail">
                    <div class="price-section">
                        <span class="current-price">{{ selectedProduct?.price }}</span>
                        <span class="old-price" v-if="selectedProduct?.oldPrice">{{ selectedProduct?.oldPrice }}</span>
                        <span class="discount-badge" v-if="selectedProduct?.discount">{{ selectedProduct?.discount
                        }}</span>
                    </div>
                    <div class="brand-section">
                        <span class="brand-label">Thương hiệu:</span>
                        <span class="brand-value">{{ selectedProduct?.brand }}</span>
                    </div>
                    <div class="rating-section">
                        <div class="rating">
                            <star-filled />
                            <span>{{ selectedProduct?.rating }} ({{ selectedProduct?.reviews }})</span>
                        </div>
                    </div>
                    <div class="description-section">
                        <h4>Mô tả sản phẩm</h4>
                        <p>{{ selectedProduct?.description || 'Chưa có mô tả chi tiết' }}</p>
                    </div>
                    <div class="variants-section">
                        <div class="color-variants">
                            <h4>Màu sắc</h4>
                            <div class="color-options">
                                <div v-for="(color, index) in selectedProduct?.colors" :key="index" class="color-option"
                                    :class="{ 'selected': selectedColor === color }" :style="{ backgroundColor: color }"
                                    @click="selectedColor = color">
                                </div>
                            </div>
                        </div>
                        <div class="size-variants">
                            <h4>Kích thước</h4>
                            <div class="size-options">
                                <div v-for="(size, index) in selectedProduct?.sizes" :key="index" class="size-option"
                                    :class="{ 'selected': selectedSize === size }" @click="selectedSize = size">
                                    {{ size }}
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="quantity-section">
                        <h4>Số lượng</h4>
                        <div class="quantity-controls">
                            <a-button @click="decreaseQuantity" :disabled="quantity <= 1">
                                <minus-outlined />
                            </a-button>
                            <span class="quantity-value">{{ quantity }}</span>
                            <a-button @click="increaseQuantity" :disabled="quantity >= selectedProduct?.stock">
                                <plus-outlined />
                            </a-button>
                        </div>
                    </div>
                    <div class="action-buttons">
                        <a-button type="primary" size="large" @click="addToCart" :disabled="!isValidSelection">
                            Thêm vào giỏ hàng
                        </a-button>
                    </div>
                </div>
            </div>
        </div>
    </a-modal>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useIntersectionObserver } from '@vueuse/core';
import {
    EyeOutlined,
    ShoppingCartOutlined,
    StarFilled,
    LeftOutlined,
    RightOutlined,
    MinusOutlined,
    PlusOutlined
} from '@ant-design/icons-vue';
import { useGbStore } from '@/stores/gbStore';
import { message } from 'ant-design-vue';

// Tham chiếu đến carousel
const store = useGbStore();
const carousel = ref(null);
const sectionRef = ref(null);
const isVisible = ref(false);
const showArrows = ref(false);

// Sử dụng Intersection Observer để theo dõi khi phần tử xuất hiện trong viewport
onMounted(async () => {
    await store.getSanPhamBySP('quần');
    // Chuyển đổi dữ liệu từ API sang định dạng phù hợp với template
    if (store.listSanPhamBanHang && store.listSanPhamBanHang.length > 0) {
        bestSellingProducts.value = store.listSanPhamBanHang.map(item => ({
            id: item.id_san_pham || item.id,
            image: item.hinh_anh || 'http://res.cloudinary.com/dtwsqkqpc/image/upload/v1742823877/oionww3qsqhfwvuvxeko.jpg',
            price: `${item.gia_khuyen_mai_cao_nhat?.toLocaleString()}₫` || '0₫',
            oldPrice: item.gia_max > item.gia_khuyen_mai_cao_nhat ? `${item.gia_max.toLocaleString()}₫` : null,
            discountPercent: item.gia_max && item.gia_khuyen_mai_cao_nhat ?
                Math.round(((item.gia_max - item.gia_khuyen_mai_cao_nhat) / item.gia_max) * 100) : 0,
            discount: item.gia_max && item.gia_khuyen_mai_cao_nhat ?
                `-${Math.round(((item.gia_max - item.gia_khuyen_mai_cao_nhat) / item.gia_max) * 100)}%` : null,
            name: item.ten_san_pham || 'Sản phẩm không tên',
            brand: item.ten_thuong_hieu || 'Chưa có thương hiệu',
            rating: item.danh_gia || 0,
            reviews: item.so_luong_danh_gia || 0
        }));
        console.log('Dữ liệu quần đã chuyển đổi:', bestSellingProducts.value);
    } else {
        console.log('Không có dữ liệu quần từ API');
    }

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

// Hàm điều khiển carousel
const nextSlide = () => {
    if (carousel.value) {
        carousel.value.next();
    }
};

const prevSlide = () => {
    if (carousel.value) {
        carousel.value.prev();
    }
};

// Sản phẩm mẫu
const bestSellingProducts = ref([]);

// Chia sản phẩm thành các slide, mỗi slide 5 sản phẩm
// Nếu slide cuối không đủ 5 sản phẩm, lấy thêm từ đầu danh sách
const productSlides = computed(() => {
    const slides = [];
    const productsPerSlide = 5;
    const products = [...bestSellingProducts.value];

    // Nếu slide cuối không đủ 5 sản phẩm, lấy thêm từ đầu danh sách
    const totalNeeded = Math.ceil(products.length / productsPerSlide) * productsPerSlide;
    if (totalNeeded > products.length) {
        const extraNeeded = totalNeeded - products.length;
        // Lấy thêm sản phẩm từ đầu danh sách
        const extraProducts = products.slice(0, extraNeeded).map(product => ({
            ...product,
            id: product.id // Thêm id mới để tránh trùng lặp
        }));
        products.push(...extraProducts);
    }

    // Chia thành các slide
    for (let i = 0; i < products.length; i += productsPerSlide) {
        slides.push(products.slice(i, i + productsPerSlide));
    }

    return slides;
});

const activeProduct = ref(null);

// Modal state
const modalVisible = ref(false);
const selectedProduct = ref(null);
const selectedColor = ref(null);
const selectedSize = ref(null);
const quantity = ref(1);

// Methods
const showProductDetail = (product) => {
    selectedProduct.value = {
        ...product,
        colors: ['#000000', '#FF0000', '#0000FF'], // Màu sắc mẫu
        sizes: ['S', 'M', 'L', 'XL', 'XXL'], // Kích thước mẫu
        stock: 10, // Số lượng tồn kho mẫu
        description: 'Quần thể thao chất liệu thun co giãn, thoáng khí, thấm hút mồ hôi tốt. Thiết kế đơn giản, dễ phối đồ, phù hợp cho các hoạt động thể thao và mặc hàng ngày.'
    };
    modalVisible.value = true;
};

const handleModalCancel = () => {
    modalVisible.value = false;
    selectedProduct.value = null;
    selectedColor.value = null;
    selectedSize.value = null;
    quantity.value = 1;
};

const decreaseQuantity = () => {
    if (quantity.value > 1) {
        quantity.value--;
    }
};

const increaseQuantity = () => {
    if (quantity.value < selectedProduct.value?.stock) {
        quantity.value++;
    }
};

const isValidSelection = computed(() => {
    return selectedColor.value && selectedSize.value;
});

const addToCart = () => {
    if (!isValidSelection.value) {
        message.warning('Vui lòng chọn màu sắc và kích thước');
        return;
    }

    const cartItem = {
        id: selectedProduct.value.id,
        ten_san_pham: selectedProduct.value.name,
        hinh_anh: selectedProduct.value.image,
        gia: parseInt(selectedProduct.value.price.replace(/[^\d]/g, '')),
        so_luong: quantity.value,
        so_luong_ton: selectedProduct.value.stock,
        ten_mau_sac: selectedColor.value,
        ten_kich_thuoc: selectedSize.value,
        selected: true
    };

    store.addToCart(cartItem);
    message.success('Đã thêm sản phẩm vào giỏ hàng');
    handleModalCancel();
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&display=swap');

.best-selling-products {
    padding: 2rem 0;
    font-family: 'Montserrat', sans-serif;
    background-color: #f8f9fa;
    opacity: 0;
    transform: translateY(30px);
    transition: opacity 0.8s ease, transform 0.8s ease;
}

.best-selling-products.visible {
    opacity: 1;
    transform: translateY(0);
}

.container {
    max-width: 1280px;
    margin: 0 auto;

}

.section-header {
    /* margin-bottom: 2rem; */
    text-align: center;
}

.section-title {
    font-size: 1.5rem;
    font-weight: 600;
    color: #333;
    /* margin-bottom: 0.5rem; */
    position: relative;
    display: inline-block;
}

.section-divider {
    height: 3px;
    width: 100px;
    background-color: #3a86ff;
    margin: 0 auto;
}

.products-grid {
    padding: 1rem;
    display: flex;
    flex-wrap: wrap;
}

.product-card {
    position: relative;
    flex: 0 0 20%;
    max-width: 20%;
    padding: 15px;
    border-radius: 8px;
    margin-bottom: 20px;
    background-color: #fff;
    cursor: pointer;
    opacity: 0;
    transform: translateY(20px);
    transition: transform 0.3s ease, box-shadow 0.3s ease, opacity 0.5s ease;
}

.product-card:hover {
    transform: translateY(-10px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
    z-index: 5;
}

@media (max-width: 1200px) {
    .product-card {
        flex: 0 0 25%;
        max-width: 25%;
    }
}

@media (max-width: 992px) {
    .product-card {
        flex: 0 0 33.333%;
        max-width: 33.333%;
    }
}

@media (max-width: 768px) {
    .product-card {
        flex: 0 0 50%;
        max-width: 50%;
    }
}

@media (max-width: 576px) {
    .product-card {
        flex: 0 0 100%;
        max-width: 100%;
    }
}

.visible .product-card {
    opacity: 1;
    transform: translateY(0);
}

.visible .product-card:hover {
    transform: translateY(-10px);
}

.visible .product-card:nth-child(1) {
    transition-delay: 0.1s;
}

.visible .product-card:nth-child(2) {
    transition-delay: 0.2s;
}

.visible .product-card:nth-child(3) {
    transition-delay: 0.3s;
}

.visible .product-card:nth-child(4) {
    transition-delay: 0.4s;
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
    aspect-ratio: 1/1;
    object-fit: cover;
    transition: transform 0.5s ease;
}

.product-card:hover .product-image {
    transform: scale(1.05);
}

.discount-badge {
    position: absolute;
    top: 10px;
    right: 10px;
    background-color: #ff3a3a;
    color: white;
    font-weight: 600;
    font-size: 12px;
    padding: 5px 10px;
    border-radius: 4px;
    z-index: 2;
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
    text-decoration: none;
}

.overlay-btn:hover {
    background: #3a86ff;
    color: white;
}

.overlay-btn span {
    margin-left: 5px;
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
    font-size: 15px;
    font-weight: 600;
    color: #333;
    margin-right: 8px;
}

.product-old-price {
    font-size: 12px;
    color: #999;
    text-decoration: line-through;
    margin-right: 8px;
}

.product-discount {
    font-size: 11px;
    font-weight: 600;
    color: white;
    background-color: #ff3a3a;
    padding: 2px 6px;
    border-radius: 10px;
}

.product-name {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin-bottom: 8px;
    line-height: 1.4;
    height: 40px;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
}

.product-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.product-brand {
    font-size: 12px;
    font-weight: 500;
    color: #666;
}

.product-rating {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: #666;
}

.product-rating :deep(svg) {
    color: #ffc107;
    margin-right: 3px;
    font-size: 14px;
}

.carousel-container {
    position: relative;
    padding: 0 50px;
    margin-bottom: 30px;
}

.custom-arrow {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 40px;
    height: 40px;
    background: rgba(0, 0, 0, 0.5);
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
    border: none;
    font-size: 18px;
    transition: all 0.3s ease;
    opacity: 0;
    visibility: hidden;
}

.custom-arrow:hover {
    background: rgba(0, 0, 0, 0.7);
}

.custom-arrow.visible {
    opacity: 1;
    visibility: visible;
}

.prev-arrow {
    left: 10px;
}

.next-arrow {
    right: 10px;
}

:deep(.ant-carousel) {
    width: 100%;
}

:deep(.ant-carousel .slick-dots-bottom) {
    bottom: -25px;
}

:deep(.ant-carousel .slick-dots li button) {
    background: #d9d9d9;
    opacity: 0.4;
}

:deep(.ant-carousel .slick-dots li.slick-active button) {
    background: #3a86ff;
    opacity: 1;
}

/* Modal styles */
.product-detail-modal {
    padding: 20px;
    position: relative;
    z-index: 10000;
}

:deep(.ant-modal) {
    z-index: 9999 !important;
    padding-top: 20px !important;
}

:deep(.ant-modal-mask) {
    z-index: 9998 !important;
}

:deep(.ant-modal-wrap) {
    z-index: 9999 !important;
}

:deep(.ant-modal-content) {
    position: relative;
    z-index: 10000;
    margin-top: 0 !important;
}

:deep(.ant-modal-header) {
    position: relative;
    z-index: 10000;
}

:deep(.ant-modal-body) {
    position: relative;
    z-index: 10000;
}

.product-detail-content {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 30px;
}

.product-images {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.main-image {
    width: 100%;
    aspect-ratio: 1;
    border-radius: 8px;
    overflow: hidden;
}

.main-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.thumbnail-images {
    display: flex;
    gap: 10px;
    overflow-x: auto;
    padding: 5px 0;
}

.thumbnail-images img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 4px;
    cursor: pointer;
    transition: opacity 0.3s ease;
}

.thumbnail-images img:hover {
    opacity: 0.8;
}

.product-info-detail {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.price-section {
    display: flex;
    align-items: center;
    gap: 10px;
}

.current-price {
    font-size: 24px;
    font-weight: 600;
    color: #333;
}

.old-price {
    font-size: 16px;
    color: #999;
    text-decoration: line-through;
}

.brand-section {
    display: flex;
    align-items: center;
    gap: 10px;
}

.brand-label {
    color: #666;
}

.brand-value {
    font-weight: 500;
    color: #333;
}

.rating-section {
    display: flex;
    align-items: center;
    gap: 5px;
}

.rating {
    display: flex;
    align-items: center;
    gap: 5px;
}

.rating :deep(svg) {
    color: #ffc107;
}

.description-section h4 {
    margin-bottom: 10px;
    color: #333;
}

.description-section p {
    color: #666;
    line-height: 1.6;
}

.variants-section {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.color-variants h4,
.size-variants h4 {
    margin-bottom: 10px;
    color: #333;
}

.color-options {
    display: flex;
    gap: 10px;
}

.color-option {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.3s ease;
}

.color-option.selected {
    border-color: #3a86ff;
}

.size-options {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
}

.size-option {
    padding: 5px 15px;
    border: 1px solid #ddd;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.size-option.selected {
    background-color: #3a86ff;
    color: white;
    border-color: #3a86ff;
}

.quantity-section h4 {
    margin-bottom: 10px;
    color: #333;
}

.quantity-controls {
    display: flex;
    align-items: center;
    gap: 10px;
}

.quantity-value {
    min-width: 40px;
    text-align: center;
    font-weight: 500;
}

.action-buttons {
    margin-top: 20px;
}

.action-buttons button {
    width: 100%;
    height: 40px;
}

@media (max-width: 768px) {
    .product-detail-content {
        grid-template-columns: 1fr;
    }
}
</style>