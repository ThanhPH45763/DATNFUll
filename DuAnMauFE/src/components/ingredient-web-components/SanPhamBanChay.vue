<template>
    <div class="san-pham-ban-chay" ref="sectionRef" :class="{ 'visible': isVisible }">
        <div class="container p-0">
            <div class="section-header">
                <h4 class="section-title">SẢN PHẨM BÁN CHẠY NHẤT</h4>
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
    await store.getSanPhamBanChayNhat();
    
    // Chuyển đổi dữ liệu từ API sang định dạng phù hợp với template
    if (store.listSanPhamBanChayNhat && store.listSanPhamBanChayNhat.length > 0) {
        bestSellingProducts.value = store.listSanPhamBanChayNhat.map(item => ({
            id: item.id,
            image: item.image || 'http://res.cloudinary.com/dtwsqkqpc/image/upload/v1742823877/oionww3qsqhfwvuvxeko.jpg',
            price: `${item.price?.toLocaleString()}₫` || '0₫',
            oldPrice: item.oldPrice > item.price ? `${item.oldPrice.toLocaleString()}₫` : null,
            discountPercent: item.oldPrice && item.price ?
                Math.round(((item.oldPrice - item.price) / item.oldPrice) * 100) : 0,
            discount: item.oldPrice && item.price ?
                `-${Math.round(((item.oldPrice - item.price) / item.oldPrice) * 100)}%` : null,
            name: item.name || 'Sản phẩm không tên',
            brand: item.brand || 'Chưa có thương hiệu',
            rating: item.rating || 0,
            reviews: item.reviews || 0
        }));
    } else {
        console.log('Không có dữ liệu sản phẩm từ API');
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
/* ===================================================
   PRODUCT BESTSELLERS - Elegant Design
   =================================================== */

.san-pham-ban-chay {
    padding: var(--space-3xl) 0;
    font-family: var(--font-primary);
    background-color: var(--color-bg-section);
    opacity: 0;
    transform: translateY(20px);
    transition: opacity 0.5s ease, transform 0.5s ease;
}

.san-pham-ban-chay.visible {
    opacity: 1;
    transform: translateY(0);
}

.container {
    max-width: var(--container-max);
    margin: 0 auto;
    padding: 0 var(--space-md);
}

/* ========== Section Header ========== */
.section-header {
    text-align: center;
    margin-bottom: var(--space-2xl);
}

.section-title {
    font-size: var(--text-2xl);
    font-weight: var(--weight-semibold);
    color: var(--color-primary);
    margin-bottom: var(--space-sm);
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.section-divider {
    height: 2px;
    width: 60px;
    background-color: var(--color-accent);
    margin: 0 auto;
}

/* ========== Products Grid ========== */
.products-grid {
    padding: var(--space-md);
    display: flex;
    flex-wrap: wrap;
    gap: var(--space-md);
}

.product-card {
    position: relative;
    flex: 0 0 calc(20% - var(--space-md));
    background-color: var(--color-white);
    border-radius: var(--radius-lg);
    overflow: hidden;
    cursor: pointer;
    transition: all var(--transition-base);
    border: 1px solid var(--color-border);
}

.visible .product-card {
    opacity: 1;
    transform: translateY(0);
}

.product-card:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg);
    border-color: var(--color-primary);
}

/* ========== Product Image ========== */
.product-image-container {
    position: relative;
    overflow: hidden;
    aspect-ratio: 1/1;
}

.product-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform var(--transition-slow);
}

.product-card:hover .product-image {
    transform: scale(1.05);
}

.discount-badge {
    position: absolute;
    top: var(--space-sm);
    right: var(--space-sm);
    background-color: var(--color-error);
    color: var(--color-white);
    font-weight: var(--weight-semibold);
    font-size: var(--text-xs);
    padding: var(--space-xs) var(--space-sm);
    border-radius: var(--radius-md);
    z-index: 2;
}

/* ========== Product Overlay ========== */
.product-overlay {
    position: absolute;
    bottom: -60px;
    left: 0;
    width: 100%;
    background: linear-gradient(to top, rgba(26, 35, 50, 0.95), transparent);
    padding: var(--space-md);
    transition: bottom var(--transition-base);
    opacity: 0;
}

.product-overlay.active {
    bottom: 0;
    opacity: 1;
}

.overlay-buttons {
    display: flex;
    justify-content: center;
    gap: var(--space-sm);
}

.overlay-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: var(--space-xs);
    background: var(--color-white);
    border: none;
    border-radius: var(--radius-md);
    padding: var(--space-xs) var(--space-md);
    font-size: var(--text-sm);
    font-weight: var(--weight-medium);
    color: var(--color-primary);
    cursor: pointer;
    transition: all var(--transition-fast);
    text-decoration: none;
    font-family: var(--font-primary);
}

.overlay-btn:hover {
    background: var(--color-primary);
    color: var(--color-white);
    transform: translateY(-1px);
}

/* ========== Product Info ========== */
.product-info {
    padding: var(--space-md);
}

.product-price-row {
    display: flex;
    align-items: center;
    gap: var(--space-sm);
    margin-bottom: var(--space-sm);
}

.product-price {
    font-size: var(--text-lg);
    font-weight: var(--weight-semibold);
    color: var(--color-primary);
}

.product-old-price {
    font-size: var(--text-sm);
    color: var(--color-text-muted);
    text-decoration: line-through;
}

.product-discount {
    font-size: var(--text-xs);
    font-weight: var(--weight-semibold);
    color: var(--color-white);
    background-color: var(--color-error);
    padding: 2px var(--space-xs);
    border-radius: var(--radius-sm);
}

.product-name {
    font-size: var(--text-base);
    font-weight: var(--weight-medium);
    color: var(--color-text);
    margin-bottom: var(--space-sm);
    line-height: 1.4;
    height: 2.8em;
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
    font-size: var(--text-sm);
}

.product-brand {
    color: var(--color-text-light);
    font-weight: var(--weight-medium);
}

.product-rating {
    display: flex;
    align-items: center;
    gap: var(--space-xs);
    color: var(--color-text-muted);
}

.product-rating :deep(svg) {
    color: var(--color-accent);
    font-size: var(--text-base);
}

/* ========== Carousel Controls ========== */
.carousel-container {
    position: relative;
    margin-bottom: var(--space-xl);
}

.custom-arrow {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 44px;
    height: 44px;
    background: var(--color-white);
    color: var(--color-primary);
    border-radius: var(--radius-full);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
    border: 1px solid var(--color-border);
    font-size: var(--text-lg);
    transition: all var(--transition-base);
    opacity: 0;
    visibility: hidden;
    box-shadow: var(--shadow-md);
}

.custom-arrow:hover {
    background: var(--color-primary);
    color: var(--color-white);
    border-color: var(--color-primary);
}

.custom-arrow.visible {
    opacity: 1;
    visibility: visible;
}

.prev-arrow {
    left: -20px;
}

.next-arrow {
    right: -20px;
}

/* ========== Ant Design Carousel Overrides ========== */
:deep(.ant-carousel) {
    width: 100%;
}

:deep(.ant-carousel .slick-dots-bottom) {
    bottom: -30px;
}

:deep(.ant-carousel .slick-dots li button) {
    background: var(--color-border);
    opacity: 0.5;
    width: 8px;
    height: 8px;
    border-radius: var(--radius-full);
    transition: all var(--transition-base);
}

:deep(.ant-carousel .slick-dots li.slick-active button) {
    background: var(--color-primary);
    opacity: 1;
    width: 24px;
    border-radius: var(--radius-sm);
}

/* ========== Modal Styles ========== */
.product-detail-modal {
    padding: var(--space-lg);
    font-family: var(--font-primary);
}

:deep(.ant-modal) {
    z-index: 9999 !important;
}

:deep(.ant-modal-mask) {
    z-index: 9998 !important;
}

:deep(.ant-modal-wrap) {
    z-index: 9999 !important;
}

:deep(.ant-modal-content) {
    border-radius: var(--radius-lg);
}

:deep(.ant-modal-header) {
    border-bottom: 1px solid var(--color-border);
    padding: var(--space-lg);
}

:deep(.ant-modal-title) {
    font-family: var(--font-primary);
    font-size: var(--text-xl);
    font-weight: var(--weight-semibold);
    color: var(--color-primary);
}

:deep(.ant-modal-body) {
    padding: var(--space-lg);
}

.product-detail-content {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: var(--space-xl);
}

.product-images {
    display: flex;
    flex-direction: column;
    gap: var(--space-md);
}

.main-image {
    width: 100%;
    aspect-ratio: 1;
    border-radius: var(--radius-lg);
    overflow: hidden;
    border: 1px solid var(--color-border);
}

.main-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.thumbnail-images {
    display: flex;
    gap: var(--space-sm);
    overflow-x: auto;
}

.thumbnail-images img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: var(--radius-md);
    cursor: pointer;
    border: 2px solid var(--color-border);
    transition: all var(--transition-base);
}

.thumbnail-images img:hover {
    border-color: var(--color-primary);
}

.product-info-detail {
    display: flex;
    flex-direction: column;
    gap: var(--space-lg);
}

.price-section {
    display: flex;
    align-items: center;
    gap: var(--space-md);
}

.current-price {
    font-size: var(--text-3xl);
    font-weight: var(--weight-semibold);
    color: var(--color-primary);
}

.old-price {
    font-size: var(--text-lg);
    color: var(--color-text-muted);
    text-decoration: line-through;
}

.brand-section {
    display: flex;
    align-items: center;
    gap: var(--space-sm);
}

.brand-label {
    color: var(--color-text-light);
    font-weight: var(--weight-medium);
}

.brand-value {
    font-weight: var(--weight-semibold);
    color: var(--color-text);
}

.rating-section {
    display: flex;
    align-items: center;
}

.rating {
    display: flex;
    align-items: center;
    gap: var(--space-xs);
}

.rating :deep(svg) {
    color: var(--color-accent);
}

.description-section h4 {
    margin-bottom: var(--space-sm);
    color: var(--color-primary);
    font-size: var(--text-lg);
    font-weight: var(--weight-semibold);
}

.description-section p {
    color: var(--color-text-light);
    line-height: 1.6;
}

.variants-section {
    display: flex;
    flex-direction: column;
    gap: var(--space-lg);
}

.color-variants h4,
.size-variants h4 {
    margin-bottom: var(--space-sm);
    color: var(--color-text);
    font-size: var(--text-base);
    font-weight: var(--weight-semibold);
}

.color-options,
.size-options {
    display: flex;
    gap: var(--space-sm);
    flex-wrap: wrap;
}

.color-option {
    width: 36px;
    height: 36px;
    border-radius: var(--radius-full);
    cursor: pointer;
    border: 2px solid var(--color-border);
    transition: all var(--transition-base);
}

.color-option.selected {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 2px rgba(var(--color-primary-rgb), 0.2);
}

.size-option {
    min-width: 50px;
    padding: var(--space-sm) var(--space-md);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    text-align: center;
    cursor: pointer;
    transition: all var(--transition-base);
    font-weight: var(--weight-medium);
}

.size-option:hover {
    border-color: var(--color-primary);
    color: var(--color-primary);
}

.size-option.selected {
    background-color: var(--color-primary);
    border-color: var(--color-primary);
    color: var(--color-white);
}

.quantity-section h4 {
    margin-bottom: var(--space-sm);
    color: var(--color-text);
    font-size: var(--text-base);
    font-weight: var(--weight-semibold);
}

.quantity-controls {
    display: flex;
    align-items: center;
    gap: var(--space-md);
}

.quantity-value {
    font-size: var(--text-xl);
    font-weight: var(--weight-semibold);
    min-width: 40px;
    text-align: center;
}

.action-buttons {
    margin-top: var(--space-md);
}

.action-buttons :deep(.ant-btn-primary) {
    background-color: var(--color-primary);
    border-color: var(--color-primary);
    font-family: var(--font-primary);
    font-weight: var(--weight-medium);
    height: 48px;
    font-size: var(--text-base);
    border-radius: var(--radius-md);
}

.action-buttons :deep(.ant-btn-primary:hover) {
    background-color: var(--color-primary-light);
    border-color: var(--color-primary-light);
}

/* ========== Responsive Design ========== */
@media (max-width: 1200px) {
    .product-card {
        flex: 0 0 calc(25% - var(--space-md));
    }
}

@media (max-width: 992px) {
    .product-card {
        flex: 0 0 calc(33.333% - var(--space-md));
    }
    
    .product-detail-content {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 768px) {
    .product-card {
        flex: 0 0 calc(50% - var(--space-md));
    }
    
    .san-pham-ban-chay {
        padding: var(--space-2xl) 0;
    }
}

@media (max-width: 576px) {
    .product-card {
        flex: 0 0 100%;
    }
    
    .custom-arrow {
        width: 36px;
        height: 36px;
        font-size: var(--text-base);
    }
    
    .prev-arrow {
        left: var(--space-xs);
    }
    
    .next-arrow {
        right: var(--space-xs);
    }
}
</style>
