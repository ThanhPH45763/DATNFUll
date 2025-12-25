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
          <h3>
            <filter-outlined /> BỘ LỌC SẢN PHẨM
            <!-- ✅ Badge tổng số bộ lọc active -->
            <a-badge v-if="totalActiveFilters > 0" :count="totalActiveFilters"
              :number-style="{ backgroundColor: '#ff6600' }" style="margin-left: 8px;" />
          </h3>
        </div>

        <!-- Loại sản phẩm (Danh mục) -->
        <div class="filter-group">
          <h4>
            Loại sản phẩm
            <a-badge v-if="selectedTypes.length > 0" :count="selectedTypes.length"
              :number-style="{ backgroundColor: '#52c41a', fontSize: '10px' }" />
          </h4>
          <div class="product-type-container">
            <a-spin v-if="loadingCategories" />
            <a-checkbox-group v-else v-model:value="selectedTypes">
              <div class="type-checkbox" v-for="category in categories" :key="category.id_danh_muc">
                <a-checkbox :value="category.ten_danh_muc">
                  {{ category.ten_danh_muc }}
                </a-checkbox>
              </div>
            </a-checkbox-group>
          </div>
        </div>

        <!-- Thương hiệu -->
        <div class="filter-group">
          <h4>
            Thương hiệu
            <a-badge v-if="selectedBrands.length > 0" :count="selectedBrands.length"
              :number-style="{ backgroundColor: '#52c41a', fontSize: '10px' }" />
          </h4>
          <div class="brand-container">
            <a-spin v-if="loadingBrands" />
            <a-checkbox-group v-else v-model:value="selectedBrands">
              <div class="brand-checkbox" v-for="brand in brands" :key="brand.id_thuong_hieu">
                <a-checkbox :value="brand.ten_thuong_hieu">
                  {{ brand.ten_thuong_hieu }}
                </a-checkbox>
              </div>
            </a-checkbox-group>
          </div>
        </div>

        <!-- Chất liệu -->
        <div class="filter-group">
          <h4>
            Chất liệu
            <a-badge v-if="selectedMaterials.length > 0" :count="selectedMaterials.length"
              :number-style="{ backgroundColor: '#52c41a', fontSize: '10px' }" />
          </h4>
          <div class="material-container">
            <a-spin v-if="loadingMaterials" />
            <a-checkbox-group v-else v-model:value="selectedMaterials">
              <div class="material-checkbox" v-for="material in materials" :key="material.id_chat_lieu">
                <a-checkbox :value="material.ten_chat_lieu">
                  {{ material.ten_chat_lieu }}
                </a-checkbox>
              </div>
            </a-checkbox-group>
          </div>
        </div>

        <!-- Kích thước -->
        <div class="filter-group">
          <h4>
            Kích thước
            <a-badge v-if="selectedSizes.length > 0" :count="selectedSizes.length"
              :number-style="{ backgroundColor: '#52c41a', fontSize: '10px' }" />
          </h4>
          <div class="size-options">
            <a-spin v-if="loadingSizes" />
            <template v-else>
              <a-tag v-for="size in sizes" :key="size.id_kich_thuoc"
                :class="{ 'size-tag-active': selectedSizes.includes(size.gia_tri + ' ' + size.don_vi) }"
                @click="toggleSize(size.gia_tri + ' ' + size.don_vi)">
                {{ size.gia_tri }} {{ size.don_vi }}
              </a-tag>
            </template>
          </div>
        </div>

        <!-- Màu sắc -->
        <div class="filter-group">
          <h4>
            Màu sắc
            <a-badge v-if="selectedColors.length > 0" :count="selectedColors.length"
              :number-style="{ backgroundColor: '#52c41a', fontSize: '10px' }" />
          </h4>
          <div class="color-options">
            <a-spin v-if="loadingColors" />
            <template v-else>
              <div v-for="color in colors" :key="color.id_mau_sac" class="color-option"
                :class="{ 'active': selectedColors.includes(color.ten_mau_sac) }"
                @click="toggleColor(color.ten_mau_sac)">
                <span class="color-dot" :style="{ backgroundColor: color.ma_mau_sac }"></span>
                <span class="color-name">{{ color.ten_mau_sac }}</span>
              </div>
            </template>
          </div>
        </div>

        <!-- Giá -->
        <div class="filter-group">
          <h4>
            Khoảng giá
            <a-badge v-if="selectedPrice[0] !== minPrice || selectedPrice[1] !== maxPrice" :count="1"
              :number-style="{ backgroundColor: '#52c41a', fontSize: '10px' }" />
          </h4>
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
          <!-- Preview count -->
          <div v-if="totalActiveFilters > 0" class="preview-count mb-2">
            <a-tag color="#ff6600">
              <template v-if="previewFilterCount > 0">
                <strong>{{ previewFilterCount }}</strong> sản phẩm sẽ được hiển thị
              </template>
              <template v-else>
                Không có sản phẩm phù hợp
              </template>
            </a-tag>
          </div>

          <a-button type="primary" block @click="applyFilters" :loading="store.isProductLoading" size="large"
            :disabled="totalActiveFilters === 0 || previewFilterCount === 0">
            <filter-filled /> Áp dụng bộ lọc
          </a-button>
          <a-button block @click="resetFilters" class="reset-button" size="large" :disabled="totalActiveFilters === 0">
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

// ✅ State cho dữ liệu từ API
const categories = ref([]); // Danh mục
const brands = ref([]); // Thương hiệu
const materials = ref([]); // Chất liệu
const sizes = ref([]); // Kích thước
const colors = ref([]); // Màu sắc

// ✅ Loading states
const loadingCategories = ref(false);
const loadingBrands = ref(false);
const loadingMaterials = ref(false);
const loadingSizes = ref(false);
const loadingColors = ref(false);

const minPrice = 0;
const maxPrice = 1000000;

const selectedTypes = ref([]); // Danh mục đã chọn
const selectedBrands = ref([]); // Thương hiệu đã chọn
const selectedPrice = ref([minPrice, maxPrice]);
const selectedColors = ref([]); // Tên màu đã chọn
const selectedMaterials = ref([]); // Chất liệu đã chọn
const selectedSizes = ref([]); // Kích thước đã chọn
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

// ✅ Helper function: Transform CTSP response thành product list
const transformCTSPToProducts = (ctspList) => {
  if (!ctspList || !Array.isArray(ctspList)) return [];

  // Group CTSP by id_san_pham
  const productMap = new Map();

  ctspList.forEach(ctsp => {
    const productId = ctsp.id_san_pham;

    if (!productMap.has(productId)) {
      // Tạo sản phẩm mới
      productMap.set(productId, {
        id: productId,
        id_san_pham: productId,
        name: ctsp.ten_san_pham,
        ten_san_pham: ctsp.ten_san_pham,
        image: ctsp.hinh_anh,
        hinh_anh: ctsp.hinh_anh,
        price: ctsp.gia_ban, // Giá của CTSP đầu tiên
        gia_ban: ctsp.gia_ban,
        oldPrice: null, // Có thể tính từ giá gốc nếu có
        type: ctsp.ten_danh_muc || '',
        brand: ctsp.ten_thuong_hieu || '',
        rating: 0,
        reviews: 0,
        // Thông tin tổng hợp từ các CTSP
        totalQuantity: 0,
        variants: [] // Lưu các biến thể
      });
    }

    // Cập nhật thông tin sản phẩm
    const product = productMap.get(productId);
    product.totalQuantity += ctsp.so_luong || 0;

    // Tìm giá thấp nhất
    if (ctsp.gia_ban < product.price) {
      product.price = ctsp.gia_ban;
      product.gia_ban = ctsp.gia_ban;
    }

    // Lưu variant
    product.variants.push({
      id_chi_tiet_san_pham: ctsp.id_chi_tiet_san_pham,
      mau_sac: ctsp.ten_mau,
      size: `${ctsp.gia_tri} ${ctsp.don_vi}`,
      so_luong: ctsp.so_luong,
      gia_ban: ctsp.gia_ban
    });
  });

  // Convert Map to Array
  return Array.from(productMap.values());
};

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

onMounted(async () => {
  // ✅ Load tiêu chí lọc từ database
  await loadFilterCriteria();

  // Load products theo filter
  fetchProductsByFilter(route.query.filter);
});

// ✅ Load tất cả tiêu chí lọc từ API
const loadFilterCriteria = async () => {
  try {
    // Load danh mục
    loadingCategories.value = true;
    const categoriesData = await store.getDanhMucList();
    if (categoriesData && !categoriesData.error) {
      categories.value = categoriesData.filter(dm => dm.trang_thai === true || dm.trang_thai === 1);
    }
    loadingCategories.value = false;

    // Load thương hiệu
    loadingBrands.value = true;
    const brandsData = await store.getThuongHieuList();
    if (brandsData && !brandsData.error) {
      brands.value = brandsData.filter(th => th.trang_thai === true || th.trang_thai === 1);
    }
    loadingBrands.value = false;

    // Load chất liệu
    loadingMaterials.value = true;
    const materialsData = await store.getChatLieuList();
    if (materialsData && !materialsData.error) {
      materials.value = materialsData.filter(cl => cl.trang_thai === true || cl.trang_thai === 1);
    }
    loadingMaterials.value = false;

    // Load kích thước
    loadingSizes.value = true;
    const sizesData = await store.getSizeList();
    if (sizesData && !sizesData.error) {
      sizes.value = sizesData.filter(kt => kt.trang_thai === true || kt.trang_thai === 1);
    }
    loadingSizes.value = false;

    // Load màu sắc
    loadingColors.value = true;
    const colorsData = await store.getMauSacList();
    if (colorsData && !colorsData.error) {
      colors.value = colorsData.filter(ms => ms.trang_thai === true || ms.trang_thai === 1);
    }
    loadingColors.value = false;

    console.log('✅ Loaded filter criteria:', {
      categories: categories.value.length,
      brands: brands.value.length,
      materials: materials.value.length,
      sizes: sizes.value.length,
      colors: colors.value.length
    });
  } catch (error) {
    console.error('Lỗi khi load tiêu chí lọc:', error);
    message.error('Không thể tải tiêu chí lọc');
  }
};

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

// ✅ Tính tổng số bộ lọc đang active
const totalActiveFilters = computed(() => {
  let count = 0;
  if (selectedBrands.value.length > 0) count++;
  if (selectedTypes.value.length > 0) count++;
  if (selectedMaterials.value.length > 0) count++;
  if (selectedSizes.value.length > 0) count++;
  if (selectedColors.value.length > 0) count++;
  if (selectedPrice.value[0] !== minPrice || selectedPrice.value[1] !== maxPrice) count++;
  return count;
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

// ✅ Preview số lượng sản phẩm sẽ lọc được
const previewFilterCount = ref(0);

// Hàm tính preview (được gọi khi user thay đổi bộ lọc)
watch([
  selectedTypes,
  selectedBrands,
  selectedPrice,
  selectedColors,
  selectedMaterials,
  selectedSizes
], async () => {
  if (totalActiveFilters.value > 0) {
    try {
      const keyword = searchQuery.value || '';
      const giaBanMin = selectedPrice.value[0];
      const giaBanMax = selectedPrice.value[1];

      // ✅ Gọi API lọc CTSP
      const ctspResult = await store.locAndTimKiemSanPhamVaChiTietSanPham(
        keyword,
        giaBanMin,
        giaBanMax,
        selectedColors.value,
        selectedTypes.value,
        selectedBrands.value,
        selectedMaterials.value,
        selectedSizes.value
      );

      // ✅ Transform CTSP thành products
      if (ctspResult && !ctspResult.error) {
        const products = transformCTSPToProducts(ctspResult);
        previewFilterCount.value = products.length;
        console.log(`Preview: ${ctspResult.length} CTSP → ${products.length} sản phẩm`);
      } else {
        previewFilterCount.value = 0;
      }
    } catch (error) {
      previewFilterCount.value = 0;
    }
  } else {
    previewFilterCount.value = 0;
  }
}, { deep: true });

// ✅ Apply filters - GỌI API VÀ TRANSFORM DỮ LIỆU
const applyFilters = async () => {
  try {
    store.isProductLoading = true;
    currentPage.value = 1;

    const keyword = searchQuery.value || '';
    const giaBanMin = selectedPrice.value[0];
    const giaBanMax = selectedPrice.value[1];

    const listMauSac = selectedColors.value;
    const listDanhMuc = selectedTypes.value;
    const listThuongHieu = selectedBrands.value;
    const listChatLieu = selectedMaterials.value;
    const listKichThuoc = selectedSizes.value;

    console.log('✅ Applying filters:', {
      keyword, giaBanMin, giaBanMax,
      listMauSac, listDanhMuc, listThuongHieu, listChatLieu, listKichThuoc
    });

    // ✅ Gọi API lọc CTSP
    const ctspResult = await store.locAndTimKiemSanPhamVaChiTietSanPham(
      keyword,
      giaBanMin,
      giaBanMax,
      listMauSac,
      listDanhMuc,
      listThuongHieu,
      listChatLieu,
      listKichThuoc
    );

    // ✅ Transform CTSP thành products và lưu vào store
    if (ctspResult && !ctspResult.error) {
      const products = transformCTSPToProducts(ctspResult);

      // Lưu vào store để hiển thị
      store.listSanPhamBanHang = products;

      console.log(`✅ Filtered: ${ctspResult.length} CTSP → ${products.length} sản phẩm unique`);

      if (products.length > 0) {
        message.success(`Đã tìm thấy ${products.length} sản phẩm phù hợp (${ctspResult.length} biến thể)`);
      } else {
        message.info('Không tìm thấy sản phẩm nào phù hợp với bộ lọc đã chọn');
      }
    } else {
      store.listSanPhamBanHang = [];
      message.info('Không tìm thấy sản phẩm nào phù hợp với bộ lọc đã chọn');
    }
  } catch (error) {
    console.error('❌ Lỗi khi lọc sản phẩm:', error);
    message.error('Lỗi khi lọc sản phẩm, vui lòng thử lại');
  } finally {
    store.isProductLoading = false;
  }
};

// ✅ Reset filters
const resetFilters = async () => {
  selectedTypes.value = [];
  selectedBrands.value = [];
  selectedPrice.value = [minPrice, maxPrice];
  selectedColors.value = [];
  selectedMaterials.value = [];
  selectedSizes.value = [];
  sortBy.value = 'default';
  searchQuery.value = '';

  // Reload all products
  try {
    store.isProductLoading = true;
    filterKeywords.value = [];
    await store.getSanPhamByTenSP(''); // Load tất cả sản phẩm
    message.success('Đã làm mới bộ lọc');
  } catch (error) {
    console.error('Lỗi khi reset:', error);
  } finally {
    store.isProductLoading = false;
  }
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

  /* ✅ Ghim sidebar khi scroll */
  position: sticky;
  top: 80px;
  /* Offset để tránh dính vào header (header khoảng 60-70px) */
  max-height: calc(100vh - 100px);
  /* Chiều cao tối đa, trừ offset top và bottom */
  overflow-y: auto;
  /* Cho phép scrollbar riêng nếu nội dung dài */
}

/* Custom scrollbar cho sidebar */
.sidebar::-webkit-scrollbar {
  width: 6px;
}

.sidebar::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.sidebar::-webkit-scrollbar-thumb {
  background: #f33b47;
  border-radius: 10px;
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background: #ff5160;
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
  height: 42px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.filter-actions .ant-btn-primary {
  background-color: #f33b47;
  border-color: #f33b47;
  box-shadow: 0 2px 8px rgba(243, 59, 71, 0.3);
}

.filter-actions .ant-btn-primary:hover:not(:disabled) {
  background-color: #ff5160;
  border-color: #ff5160;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(243, 59, 71, 0.4);
}

.filter-actions .ant-btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

.reset-button {
  border: 1px solid #d9d9d9;
  background: #f5f5f5;
  color: #666;
}

.reset-button:hover:not(:disabled) {
  border-color: #f33b47;
  color: #f33b47;
  background: #fff7f8;
}

.reset-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ✅ Preview count styling */
.preview-count {
  text-align: center;
  padding: 8px;
  background: #fff7f8;
  border-radius: 6px;
  border: 1px solid #ffcdd2;
}

.preview-count .ant-tag {
  font-size: 13px;
  padding: 4px 12px;
  border: none;
}

/* ✅ Badge animations */
:deep(.ant-badge) {
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.1);
  }
}

/* ✅ Filter group hover effect */
.filter-group {
  transition: all 0.2s ease;
}

.filter-group:hover {
  background-color: #fcfcfc;
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
