<template>
  <div class="product-list-page">
    <!-- Breadcrumb + Ảnh + Tiêu đề -->
    <div class="header mt-4">
      <a-breadcrumb class="breadcrumb">
        <a-breadcrumb-item @click="goHome" style="cursor:pointer">Trang chủ</a-breadcrumb-item>
        <a-breadcrumb-item>
          {{ getBreadcrumbLabel(filterKeywords.value) }}
        </a-breadcrumb-item>
      </a-breadcrumb>
    </div>

    <div class="main-content">
      <!-- Sidebar: Bộ lọc -->
      <aside class="sidebar">
        <div class="filter-header">
          <h3><filter-outlined /> BỘ LỌC SẢN PHẨM</h3>
        </div>

        <!-- Giới tính -->
        <div class="filter-group">
          <h4>Giới tính</h4>
          <div class="gender-buttons">
            <a-button :class="{ 'active-gender': selectedGender === 'nam' }"
              @click="selectedGender = selectedGender === 'nam' ? null : 'nam'">
              <man-outlined /> Nam
            </a-button>
            <a-button :class="{ 'active-gender': selectedGender === 'nu' }"
              @click="selectedGender = selectedGender === 'nu' ? null : 'nu'">
              <woman-outlined /> Nữ
            </a-button>
            <a-button :class="{ 'active-gender': selectedGender === 'unisex' }"
              @click="selectedGender = selectedGender === 'unisex' ? null : 'unisex'">
              <team-outlined /> Unisex
            </a-button>
          </div>
        </div>

        <!-- Thương hiệu -->
        <div class="filter-group">
          <h4>Thương hiệu</h4>
          <div class="brand-container">
            <a-checkbox-group v-model:value="selectedBrands">
              <div class="brand-checkbox" v-for="brand in brands" :key="brand">
                <a-checkbox :value="brand">
                  {{ brand }}
                </a-checkbox>
              </div>
            </a-checkbox-group>
          </div>
        </div>

        <!-- Loại sản phẩm -->
        <div class="filter-group">
          <h4>Loại sản phẩm</h4>
          <div class="product-type-container">
            <a-checkbox-group v-model:value="selectedTypes">
              <div class="type-checkbox" v-for="type in productTypes" :key="type.value">
                <a-checkbox :value="type.value">
                  {{ type.label }} <span class="count-tag">({{ type.count }})</span>
                </a-checkbox>
              </div>
            </a-checkbox-group>
          </div>
        </div>

        <!-- Chất liệu -->
        <div class="filter-group">
          <h4>Chất liệu</h4>
          <div class="material-container">
            <a-checkbox-group v-model:value="selectedMaterials">
              <div class="material-checkbox" v-for="material in materials" :key="material">
                <a-checkbox :value="material">
                  {{ material }}
                </a-checkbox>
              </div>
            </a-checkbox-group>
          </div>
        </div>

        <!-- Kích thước -->
        <div class="filter-group">
          <h4>Kích thước</h4>
          <div class="size-options">
            <a-tag v-for="size in sizes" :key="size" :class="{ 'size-tag-active': selectedSizes.includes(size) }"
              @click="toggleSize(size)">
              {{ size }}
            </a-tag>
          </div>
        </div>

        <!-- Màu sắc -->
        <div class="filter-group">
          <h4>Màu sắc</h4>
          <div class="color-options">
            <div v-for="color in colors" :key="color.value" class="color-option"
              :class="{ 'active': selectedColors.includes(color.value) }" @click="toggleColor(color.value)">
              <span class="color-dot" :style="{ backgroundColor: color.value }"></span>
              <span class="color-name">{{ color.name }}</span>
            </div>
          </div>
        </div>

        <!-- Giá -->
        <div class="filter-group">
          <h4>Khoảng giá</h4>
          <a-slider v-model:value="selectedPrice" :min="minPrice" :max="maxPrice" range
            :tip-formatter="formatCurrency" />
          <div class="price-range">
            <span>{{ formatCurrency(selectedPrice[0]) }}</span>
            <span>-</span>
            <span>{{ formatCurrency(selectedPrice[1]) }}</span>
          </div>
        </div>

        <!-- Nút áp dụng và làm mới -->
        <div class="filter-actions">
          <a-button type="primary" block @click="applyFilters">
            <filter-filled /> Áp dụng bộ lọc
          </a-button>
          <a-button block @click="resetFilters" class="reset-button">
            <clear-outlined /> Làm mới bộ lọc
          </a-button>
        </div>
      </aside>

      <!-- Danh sách sản phẩm -->
      <section class="product-list">
        <!-- Hiển thị loading khi đang tải sản phẩm -->
        <div v-if="store.isProductLoading" class="loading-container">
          <div class="loading-spinner">
            <div class="spinner"></div>
          </div>
          <div class="loading-text">Đang tải sản phẩm...</div>
        </div>

        <div v-else>
          <div class="list-header">
            <span>{{ filteredProducts.length }} Sản phẩm</span>
            <a-select v-model:value="sortBy" style="width: 160px">
              <a-select-option value="default">Sắp xếp theo</a-select-option>
              <a-select-option value="price-asc">Giá tăng dần</a-select-option>
              <a-select-option value="price-desc">Giá giảm dần</a-select-option>
            </a-select>
          </div>
          <a-row :gutter="[24, 24]">
            <a-col v-for="product in displayedProducts" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6">
              <div class="product-card" @mouseenter="activeProduct = product.id" @mouseleave="activeProduct = null">
                <div class="product-image-container">
                  <img class="product-image" :src="product.image" alt="Product image">
                  <div class="discount-badge" v-if="product.oldPrice">
                    -{{ Math.round(100 * (1 - product.price / product.oldPrice)) }}%
                  </div>
                  <div class="product-overlay" :class="{ 'active': activeProduct === product.id }">
                    <div class="overlay-buttons">
                      <button class="overlay-btn view-btn" @click="router.push('/sanphamdetail/' + product.id)">
                        <eye-outlined />
                        <span>Xem</span>
                      </button>
                      <button class="overlay-btn cart-btn">
                        <shopping-cart-outlined />
                        <span>Thêm</span>
                      </button>
                    </div>
                  </div>
                </div>
                <div class="product-info">
                  <div class="product-price-row">
                    <span class="product-price">{{ formatCurrency(product.price) }}</span>
                    <span class="product-old-price" v-if="product.oldPrice">{{ formatCurrency(product.oldPrice)
                      }}</span>
                    <span class="product-discount" v-if="product.oldPrice">
                      -{{ Math.round(100 * (1 - product.price / product.oldPrice)) }}%
                    </span>
                  </div>
                  <h6 class="product-name">{{ product.name }}</h6>
                  <div class="product-meta">
                    <span class="product-brand">{{ product.brand }}</span>
                    <div class="product-rating">
                      <star-filled />
                      <span>{{ product.rating || 0 }} ({{ product.reviews || 0 }})</span>
                    </div>
                  </div>
                </div>
              </div>
            </a-col>
          </a-row>

          <!-- Thay thế phần nút Xem thêm cũ bằng đoạn code này -->
          <div v-if="hasMoreProducts" class="load-more-section">
            <!-- Phần preview sản phẩm tiếp theo -->
            <div class="next-products-preview">
              <a-row :gutter="[24, 24]">
                <a-col v-for="product in nextProducts" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6">
                  <div class="product-card preview-card">
                    <div class="product-image-container">
                      <img class="product-image" :src="product.image" alt="Product preview">
                    </div>
                  </div>
                </a-col>
              </a-row>
            </div>

            <!-- Nút xem thêm dạng text -->
            <button class="text-load-more" @click="loadMore">
              Xem thêm
              <down-outlined class="down-icon" />
            </button>
          </div>

          <!-- Thông báo không có sản phẩm -->
          <div v-if="displayedProducts.length === 0" class="empty-state">
            <a-empty description="Không có sản phẩm nào phù hợp" />
          </div>
        </div>
      </section>
    </div>
  </div>

</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { FilterOutlined, EyeOutlined, ShoppingCartOutlined, StarFilled, DownOutlined, ManOutlined, WomanOutlined, TeamOutlined, FilterFilled, ClearOutlined } from '@ant-design/icons-vue';
import { useRouter, useRoute } from 'vue-router';
import { useGbStore } from '@/stores/gbStore';
import { message } from 'ant-design-vue';

const store = useGbStore();
const route = useRoute();
const router = useRouter();

const categoryImage = 'https://contents.mediadecathlon.com/p2159822/k$e2e2e2e2e2e2e2e2e2e2e2e2e2e2e2e2/ao-khong-tay.jpg?format=auto&quality=70&f=650x0';

const productTypes = [
  { value: 'tank', label: 'Áo Không Tay', count: 43 },
  { value: 'shirt', label: 'Áo Thun', count: 65 },
  { value: 'pants', label: 'Quần Thể Thao', count: 38 },
  { value: 'jacket', label: 'Áo Khoác', count: 27 }
];
const genders = [
  { value: 'nam', label: 'NAM', count: 43 },
  { value: 'nu', label: 'NỮ', count: 38 },
  { value: 'unisex', label: 'UNISEX', count: 15 }
];
const brands = ['Nike', 'Adidas', 'Puma', 'Decathlon', 'Under Armour', 'New Balance', 'Reebok'];
const colors = [
  { value: '#000000', name: 'Đen' },
  { value: '#FFFFFF', name: 'Trắng' },
  { value: '#FF0000', name: 'Đỏ' },
  { value: '#0000FF', name: 'Xanh dương' },
  { value: '#008000', name: 'Xanh lá' },
  { value: '#FFFF00', name: 'Vàng' },
  { value: '#FFA500', name: 'Cam' },
  { value: '#800080', name: 'Tím' },
  { value: '#A52A2A', name: 'Nâu' },
  { value: '#808080', name: 'Xám' }
];

// Thêm dữ liệu cho chất liệu
const materials = ['Cotton', 'Polyester', 'Nylon', 'Spandex', 'Vải thể thao co giãn', 'Len', 'Lông cừu'];

// Thêm dữ liệu cho kích thước
const sizes = ['XS', 'S', 'M', 'L', 'XL', 'XXL', '28', '30', '32', '34', '36'];

const minPrice = 0;
const maxPrice = 1000000;

const selectedTypes = ref([]);
const selectedGender = ref(null);
const selectedBrands = ref([]);
const selectedPrice = ref([minPrice, maxPrice]);
const selectedColors = ref([]);
const selectedMaterials = ref([]);
const selectedSizes = ref([]);
const sortBy = ref('default');

const searchQuery = ref('');

// Lấy keyword từ query (có thể là string hoặc mảng)
const filterKeywords = ref(
  Array.isArray(route.query.filter)
    ? route.query.filter
    : route.query.filter
      ? [route.query.filter]
      : []
);

// Theo dõi khi route thay đổi (user click sidebar)
watch(() => route.query.filter, (newVal) => {
  filterKeywords.value = Array.isArray(newVal)
    ? newVal
    : newVal
      ? [newVal]
      : [];
});

// Thêm watch để theo dõi query search
watch(
  () => route.query.search,
  async (newSearchQuery) => {
    if (newSearchQuery) {
      try {
        store.isProductLoading = true;
        // Reset các filter khi có tìm kiếm mới
        selectedTypes.value = [];
        selectedGender.value = null;
        selectedBrands.value = [];
        selectedPrice.value = [minPrice, maxPrice];
        selectedColors.value = [];
        selectedMaterials.value = [];
        selectedSizes.value = [];

        // Gọi API tìm kiếm
        await store.getSanPhamByTenSP(newSearchQuery);
      } catch (error) {
        console.error('Lỗi khi tìm kiếm:', error);
      } finally {
        store.isProductLoading = false;
      }
    }
  },
  { immediate: true }
);

// Thêm watch để theo dõi thay đổi route.query
watch(
  () => route.query.filter,
  async (newFilter) => {
    if (newFilter) {
      try {
        store.isProductLoading = true;
        // Reset các filter hiện tại
        selectedTypes.value = [];
        selectedGender.value = null;
        selectedBrands.value = [];
        selectedPrice.value = [minPrice, maxPrice];
        selectedColors.value = [];
        selectedMaterials.value = [];
        selectedSizes.value = [];

        // Cập nhật filterKeywords để hiển thị đúng breadcrumb
        filterKeywords.value = [newFilter];

        // Gọi API tìm kiếm nếu chưa có dữ liệu
        if (!store.listSanPhamBanHang?.length) {
          await store.getSanPhamByTenSP(newFilter);
        }
      } catch (error) {
        console.error('Lỗi khi tìm kiếm:', error);
      } finally {
        store.isProductLoading = false;
      }
    }
  },
  { immediate: true }
);

// Theo dõi khi danh sách sản phẩm thay đổi
watch(() => store.listSanPhamBanHang, (newProducts) => {
  if (newProducts) {
    // Cập nhật breadcrumb nếu đang trong chế độ tìm kiếm
    filterKeywords.value = [];
    // Reset các bộ lọc khi có kết quả tìm kiếm mới
    selectedTypes.value = [];
    selectedGender.value = null;
    selectedBrands.value = [];
    selectedPrice.value = [minPrice, maxPrice];
    selectedColors.value = [];
    selectedMaterials.value = [];
    selectedSizes.value = [];
  }
}, { immediate: true });

// Toggle size chọn
function toggleSize(size) {
  if (selectedSizes.value.includes(size)) {
    selectedSizes.value = selectedSizes.value.filter(s => s !== size);
  } else {
    selectedSizes.value.push(size);
  }
}

// Toggle color chọn
function toggleColor(color) {
  if (selectedColors.value.includes(color)) {
    selectedColors.value = selectedColors.value.filter(c => c !== color);
  } else {
    selectedColors.value.push(color);
  }
}

// Lọc sản phẩm từ store theo nhiều keyword
const filteredProducts = computed(() => {
  if (!store.listSanPhamBanHang) return [];

  // Nếu là trang siêu sale, trả về trực tiếp danh sách từ API
  if (filterKeywords.value.includes('supersale')) {
    console.log('Hiển thị sản phẩm siêu sale:', store.listSanPhamBanHang.length);
    return store.listSanPhamBanHang;
  }

  console.log('Filter keywords:', filterKeywords.value);

  // Định nghĩa các danh mục
  const categoryKeywords = ['Bóng đá', 'Bóng rổ', 'Cầu lông', 'Đạp xe', 'Chạy bộ', 'Yoga', 'Nam', 'Nữ'];

  // Định nghĩa các loại sản phẩm
  const productTypeKeywords = ['Quần', 'Áo', 'Váy', 'Tank top'];

  // Tách filter thành 2 loại
  const categoryFilters = filterKeywords.value.filter(kw => categoryKeywords.includes(kw));
  const productTypeFilters = filterKeywords.value.filter(kw => productTypeKeywords.includes(kw));

  return store.listSanPhamBanHang.filter(product => {
    // Kiểm tra theo danh mục (type)
    const matchCategory = categoryFilters.length === 0 ||
      categoryFilters.some(kw => product.type && product.type.toLowerCase().includes(kw.toLowerCase()));

    // Kiểm tra theo tên sản phẩm
    const matchProductType = productTypeFilters.length === 0 ||
      productTypeFilters.some(kw => product.name && product.name.toLowerCase().includes(kw.toLowerCase()));

    // Phải thỏa mãn cả 2 điều kiện
    return matchCategory && matchProductType;
  });
});

const sortedAndFilteredProducts = computed(() => {
  let arr = [...filteredProducts.value];
  if (sortBy.value === 'price-asc') arr.sort((a, b) => a.price - b.price);
  else if (sortBy.value === 'price-desc') arr.sort((a, b) => b.price - a.price);
  else if (sortBy.value === 'new') arr.sort((a, b) => b.isNew - a.isNew);
  return arr;
});

function goHome() {
  router.push('/home');
}
function formatCurrency(val) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
}

const activeProduct = ref(null);

const breadcrumbMap = [
  {
    keywords: ['Bóng đá', 'Bóng rổ', 'Cầu lông', 'Đạp xe', 'Chạy bộ', 'Yoga'],
    label: 'Môn thể thao'
  },
  {
    keywords: ['Nam'],
    label: 'Nam'
  },
  {
    keywords: ['Nữ'],
    label: 'Nữ'
  },
  {
    keywords: [],
    label: 'Tất cả sản phẩm'
  },
  {
    keywords: ['Quần', 'Váy'],
    label: 'Quần'
  },
  {
    keywords: ['Áo', 'Tank top'],
    label: 'Áo'
  }
  // Thêm các nhóm khác nếu cần
];

function getBreadcrumbLabel(filterKeywords) {
  if (filterKeywords && filterKeywords[0] === 'supersale') {
    return 'Siêu sale sập sàn';
  }
  if (
    !filterKeywords ||
    filterKeywords.length === 0 ||
    (filterKeywords.length === 1 && (!filterKeywords[0] || filterKeywords[0] === 'all'))
  ) {
    return 'Tất cả sản phẩm';
  }
  if (filterKeywords[0] === 'Sport') {
    return 'Môn thể thao';
  }
  // Danh sách từ khóa của môn thể thao
  const sportsKeywords = ['Bóng đá', 'Bóng rổ', 'Cầu lông', 'Đạp xe', 'Chạy bộ', 'Yoga'];

  // Kiểm tra nếu filter chứa bất kỳ từ khóa môn thể thao nào
  if (filterKeywords.some(kw => sportsKeywords.includes(kw))) {
    return 'Môn thể thao';
  }

  // Kiểm tra các trường hợp còn lại
  for (const group of breadcrumbMap) {
    if (group.keywords.length && filterKeywords.length === 1 && group.keywords.includes(filterKeywords[0])) {
      return group.label;
    }
  }

  return filterKeywords[0] || 'Tất cả sản phẩm';
}

function fetchProductsByFilter(filter) {
  if (filter === 'supersale') {
    // Nếu là trang siêu sale thì chỉ gọi API lấy sản phẩm siêu sale
    store.getSanPhamSieuSale();
    return;
  }

  // Các logic filter khác giữ nguyên
  const categories = ['Bóng đá', 'Bóng rổ', 'Cầu lông', 'Đạp xe', 'Chạy bộ', 'Yoga', 'Nam', 'Nữ'];
  const productTypes = ['Quần', 'Áo', 'Váy', 'Tank top'];

  if (!filter || filter === 'all') {
    store.getSanPhamByTenDM('');
  } else if (filter === 'Sport') {
    const sports = ['Bóng đá', 'Bóng rổ', 'Cầu lông', 'Đạp xe', 'Chạy bộ', 'Yoga'];
    store.getSanPhamByTenDM(sports);
  } else if (categories.includes(filter)) {
    store.getSanPhamByTenDM(filter);
  } else if (productTypes.includes(filter)) {
    store.getSanPhamByTenSP(filter);
  }
}

onMounted(() => {
  fetchProductsByFilter(route.query.filter);
});

watch(() => route.query.filter, (newFilter) => {
  fetchProductsByFilter(newFilter);
});

const itemsPerPage = 20; // Số sản phẩm mỗi trang
const currentPage = ref(1); // Trang hiện tại
const showLoadMore = ref(true); // Hiển thị nút "Xem thêm"

const displayedProducts = computed(() => {
  const endIndex = Math.min(currentPage.value * itemsPerPage, sortedAndFilteredProducts.value.length);
  return sortedAndFilteredProducts.value.slice(0, endIndex);
});

const hasMoreProducts = computed(() => {
  return displayedProducts.value.length < sortedAndFilteredProducts.value.length;
});

const loadMore = () => {
  const totalProducts = sortedAndFilteredProducts.value.length;
  const currentlyShowing = currentPage.value * itemsPerPage;

  if (currentlyShowing < totalProducts) {
    currentPage.value++;
  }
};

watch([
  () => route.query.filter,
  selectedTypes,
  selectedGender,
  selectedBrands,
  selectedPrice,
  selectedColors,
  selectedMaterials,
  selectedSizes,
  sortBy
], () => {
  currentPage.value = 1;
});

const nextProducts = computed(() => {
  const startIndex = currentPage.value * itemsPerPage;
  const endIndex = startIndex + 4; // Chỉ lấy 4 sản phẩm để preview
  return sortedAndFilteredProducts.value.slice(startIndex, endIndex);
});

const applyFilters = () => {
  // Lọc sản phẩm dựa trên các tiêu chí đã chọn
  currentPage.value = 1; // Reset trang về đầu tiên khi lọc

  // Có thể thêm logic tìm kiếm API hoặc lọc tại đây

  // Thông báo kết quả lọc
  const totalFilteredProducts = filteredProducts.value.length;
  if (totalFilteredProducts > 0) {
    message.success(`Đã tìm thấy ${totalFilteredProducts} sản phẩm phù hợp`);
  } else {
    message.info('Không tìm thấy sản phẩm nào phù hợp với bộ lọc đã chọn');
  }
};

const resetFilters = () => {
  selectedTypes.value = [];
  selectedGender.value = null;
  selectedBrands.value = [];
  selectedPrice.value = [minPrice, maxPrice];
  selectedColors.value = [];
  selectedMaterials.value = [];
  selectedSizes.value = [];
  sortBy.value = 'default';
  message.success('Đã làm mới bộ lọc');
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&display=swap');

.breadcrumb {
  color: #f80f0fe5;
  font-family: Montserrat, sans-serif !important;
}

.product-list-page {
  max-width: 1300px;
  margin: 0 auto;
  padding: 0 16px;
}

.header {
  margin-bottom: 24px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-top: 12px;
}

.category-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.main-content {
  display: flex;
  gap: 32px;
}

.sidebar {
  width: 300px;
  background: #fff;
  border-radius: 12px;
  padding: 0;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  height: fit-content;
  transition: all 0.3s ease;
}

.sidebar:hover {
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.15);
}

.filter-header {
  background: linear-gradient(45deg, #f33b47, #ff5160);
  color: white;
  padding: 15px 20px;
  border-radius: 12px 12px 0 0;
}

.filter-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
}

.filter-group {
  margin: 0;
  padding: 18px 20px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.filter-group:hover {
  background-color: #f9f9f9;
}

.filter-group h4 {
  margin-bottom: 15px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  position: relative;
  padding-left: 0;
}

.filter-group h4::before {
  content: '';
  position: absolute;
  left: -20px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background: #f33b47;
  border-radius: 10px;
}

/* Giới tính */
.gender-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.gender-buttons .ant-btn {
  flex: 1;
  text-align: center;
  border-radius: 20px;
  height: 36px;
  border: 1px solid #d9d9d9;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  transition: all 0.3s;
}

.gender-buttons .ant-btn:hover {
  color: #f33b47;
  border-color: #f33b47;
}

.active-gender {
  color: white !important;
  background-color: #f33b47 !important;
  border-color: #f33b47 !important;
}

/* Thương hiệu và loại sản phẩm */
.brand-container,
.product-type-container,
.material-container {
  max-height: 150px;
  overflow-y: auto;
  padding-right: 10px;
}

.brand-container::-webkit-scrollbar,
.product-type-container::-webkit-scrollbar,
.material-container::-webkit-scrollbar {
  width: 5px;
}

.brand-container::-webkit-scrollbar-thumb,
.product-type-container::-webkit-scrollbar-thumb,
.material-container::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 10px;
}

.brand-container::-webkit-scrollbar-thumb:hover,
.product-type-container::-webkit-scrollbar-thumb:hover,
.material-container::-webkit-scrollbar-thumb:hover {
  background: #ccc;
}

.brand-checkbox,
.type-checkbox,
.material-checkbox {
  margin-bottom: 10px;
}

.count-tag {
  color: #999;
  font-size: 12px;
}

/* Kích thước */
.size-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.size-options .ant-tag {
  cursor: pointer;
  border-radius: 4px;
  padding: 4px 12px;
  font-size: 13px;
  margin: 0;
  border: 1px solid #d9d9d9;
  background: white;
  transition: all 0.2s;
}

.size-options .ant-tag:hover {
  border-color: #f33b47;
  color: #f33b47;
}

.size-tag-active {
  background-color: #f33b47 !important;
  color: white !important;
  border-color: #f33b47 !important;
}

/* Màu sắc */
.color-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.color-option {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 48%;
  padding: 5px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.color-option:hover {
  background-color: #f5f5f5;
}

.color-option.active {
  background-color: #f9f0f1;
}

.color-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 1px solid #ddd;
  display: inline-block;
}

.color-name {
  font-size: 13px;
  color: #666;
}

.active .color-name {
  color: #f33b47;
  font-weight: 500;
}

/* Giá */
.price-range {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
  margin-top: 12px;
}

/* Các nút bộ lọc */
.filter-actions {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-actions .ant-btn {
  height: 38px;
  border-radius: 6px;
}

.filter-actions .ant-btn-primary {
  background-color: #f33b47;
  border-color: #f33b47;
  box-shadow: 0 2px 8px rgba(243, 59, 71, 0.3);
  transition: all 0.3s;
}

.filter-actions .ant-btn-primary:hover {
  background-color: #ff5160;
  border-color: #ff5160;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(243, 59, 71, 0.4);
}

.reset-button {
  border: 1px solid #d9d9d9;
  background: #f5f5f5;
  color: #666;
  font-weight: 500;
}

.reset-button:hover {
  border-color: #999;
  color: #333;
}

/* Style filter cho Ant Slider */
:deep(.ant-slider-track) {
  background-color: #f33b47 !important;
}

:deep(.ant-slider-handle) {
  border-color: #f33b47 !important;
}

:deep(.ant-slider:hover .ant-slider-track) {
  background-color: #ff5160 !important;
}

:deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #f33b47 !important;
  border-color: #f33b47 !important;
}

:deep(.ant-checkbox-wrapper:hover .ant-checkbox-inner) {
  border-color: #f33b47 !important;
}

:deep(.ant-radio-button-wrapper-checked) {
  background: #f33b47 !important;
  border-color: #f33b47 !important;
}

.product-list {
  flex: 1;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.product-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px #f0f1f2;
  padding: 16px;
  cursor: pointer;
  position: relative;
  transition: box-shadow 0.2s;
  margin-bottom: 20px;
}

.product-card:hover {
  box-shadow: 0 4px 16px #e6e6e6;
}

.product-image-container {
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 12px;
}

.product-image {
  width: 100%;
  height: 220px;
  object-fit: cover;
  border-radius: 8px;
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

.empty-state {
  margin-top: 48px;
  text-align: center;
}

.price-range {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}

.load-more-section {
  position: relative;
  margin: 32px 0;
  text-align: center;
}

.next-products-preview {
  position: relative;
  height: 100px;
  overflow: hidden;
  margin-bottom: 20px;
}

.next-products-preview::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100%;
  background: linear-gradient(to bottom,
      rgba(255, 255, 255, 0) 0%,
      rgba(255, 255, 255, 0.8) 30%,
      rgba(255, 255, 255, 0.9) 50%,
      rgba(255, 255, 255, 1) 100%);
  pointer-events: none;
}

.preview-card {
  opacity: 0.5;
  transform: scale(0.95);
  pointer-events: none;
}

.text-load-more {
  background: none;
  border: none;
  color: #f33b47;
  font-size: 16px;
  font-weight: 600;
  padding: 8px 16px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.text-load-more:hover {
  color: #ff5060;
  transform: translateY(-2px);
}

.text-load-more .down-icon {
  font-size: 14px;
  transition: transform 0.3s ease;
}

.text-load-more:hover .down-icon {
  transform: translateY(2px);
}

/* Điều chỉnh lại style cho product-card trong preview */
.preview-card .product-image-container {
  height: 80px;
}

.preview-card .product-image {
  height: 100%;
}

/* CSS cho loading spinner */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 300px;
}

.loading-spinner {
  position: relative;
  width: 60px;
  height: 60px;
}

.spinner {
  width: 60px;
  height: 60px;
  border: 4px solid rgba(243, 59, 71, 0.2);
  border-top-color: #f33b47;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  margin-top: 16px;
  font-size: 1.2rem;
  font-weight: 600;
  color: #f33b47;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
