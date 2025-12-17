<template>
    <div v-if="shouldShowSearch" class="container-fluid" style="width: 70%;">
        <form class="d-flex align-items-center justify-content-start" role="search" @submit.prevent="handleSearch">
            <SearchOutlined class="icon-search" @click="handleSearch" />
            <input class="form-control me-2" v-model="searchInput" type="search" 
                :placeholder="searchPlaceholder"
                aria-label="Search">
        </form>
    </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useGbStore } from '@/stores/gbStore';
import { SearchOutlined } from '@ant-design/icons-vue';
import { useRoute } from 'vue-router';

const store = useGbStore();
const route = useRoute();
const searchInput = ref('');

// Hàm chuẩn hóa chuỗi tìm kiếm: xóa khoảng trắng thừa ở đầu, cuối và giữa các từ
const formatSearchString = (text) => {
    if (!text) return '';
    // Xóa khoảng trắng ở đầu và cuối, sau đó thay thế nhiều khoảng trắng liên tiếp bằng một khoảng trắng
    return text.trim().replace(/\s+/g, ' ');
};

// Chuỗi tìm kiếm đã được xử lý
const formattedSearchInput = computed(() => {
    return formatSearchString(searchInput.value);
});

// Determine if search should be shown
const shouldShowSearch = computed(() => {
    const hideRoutes = ['admin']; // Thống kê
    return !hideRoutes.includes(route.name);
});

// Dynamic placeholder based on route
const searchPlaceholder = computed(() => {
    const placeholders = {
        'admin-quan-ly-san-pham': 'Tìm sản phẩm...',
        'admin-quan-ly-hoa-don': 'Tìm hóa đơn...',
        'admin-quan-ly-khach-hang': 'Tìm khách hàng...',
        'admin-quan-ly-nhan-vien': 'Tìm nhân viên...',
        'admin-quan-ly-voucher': 'Tìm voucher...',
        'admin-quan-ly-khuyen-mai': 'Tìm khuyến mãi...'
    };
    return placeholders[route.name] || 'Bạn muốn tìm gì?';
});

// Hàm xóa kết quả tìm kiếm
const clearSearchResults = () => {
    if (route.name === 'admin-quan-ly-san-pham') {
        // Chỉ cập nhật keyword, giữ nguyên các tham số lọc khác
        store.updateSearchFilterParams({ keyword: '' });
        store.applySearchAndFilter();
    } else if (route.name === 'admin-quan-ly-hoa-don') {
        store.searchs = '';
        store.hoaDonSearch = [];
    } else if (route.name === 'admin-quan-ly-voucher') {
        store.voucherSearchs = '';
        store.voucherSearch = [];
    } else if (route.name === 'admin-quan-ly-khuyen-mai') {
        store.khuyenMaiSearchs = '';
        store.khuyenMaiSearch = [];
    } else {
        // Xử lý các trường hợp khác
        store.searchs = '';
        store.nhanVienSearch = [];
        store.getAllKhachHangArr = [];
    }
    searchInput.value = '';
};

// Theo dõi sự thay đổi của route và xóa kết quả tìm kiếm khi route thay đổi
watch(() => route.name, (newRouteName, oldRouteName) => {
    if (newRouteName !== oldRouteName) {
        clearSearchResults();
    }
}, { immediate: true });

// Theo dõi sự thay đổi của ô tìm kiếm
watch(searchInput, (newValue) => {
    // Nếu ô tìm kiếm trống, tự động load lại tất cả dữ liệu
    if (!newValue || newValue.trim() === '') {
        if (route.name === 'admin-quan-ly-san-pham') {
            // Chỉ cập nhật keyword, giữ nguyên các tham số lọc khác
            store.updateSearchFilterParams({ keyword: '' });
            store.applySearchAndFilter();
        } else if (route.name === 'admin-quan-ly-nhan-vien') {
            store.getAllNhanVien(0, 5);
        } else if (route.name === 'admin-quan-ly-khach-hang') {
            store.getAllKhachHang(0, 3, null, null);
        } else if (route.name === 'admin-quan-ly-hoa-don') {
            store.getAllHoaDon(0, 5);
        } else if (route.name === 'admin-quan-ly-voucher') {
            store.getAllVouchers(0, 5);
        } else if (route.name === 'admin-quan-ly-khuyen-mai') {
            store.getAllKhuyenMai(0, 5);
        }
    }
});

// Hàm xử lý tìm kiếm
const handleSearch = async () => {
    const formattedValue = formattedSearchInput.value;
    const currentFilters = { ...store.searchFilterParams };
    
    if (!searchInput.value || searchInput.value.trim() === '') {
        clearSearchResults();
        return;
    }

    console.log('Đang tìm kiếm với từ khóa:', searchInput.value);

    try {
        // Sản phẩm
        if (route.name === 'admin-quan-ly-san-pham') {
            if (!formattedValue) {
                currentFilters.keyword = '';
                store.updateSearchFilterParams(currentFilters);
            } else {
                currentFilters.keyword = formattedValue;
                store.updateSearchFilterParams(currentFilters);
            }
            
            await store.applySearchAndFilter();
            window.dispatchEvent(new CustomEvent('search-filter-changed', {
                detail: {
                    results: store.filteredProductsData,
                    keyword: formattedValue
                }
            }));
            searchInput.value = formattedValue;
        }
        // Hóa đơn
        else if (route.name === 'admin-quan-ly-hoa-don') {
            store.searchs = searchInput.value;
            await store.searchHoaDon(searchInput.value, 0, 5);
            console.log('Kết quả tìm kiếm hóa đơn:', store.hoaDonSearch);
        }
        // Khách hàng
        else if (route.name === 'admin-quan-ly-khach-hang') {
            store.searchs = searchInput.value;
            await store.getAllKhachHang(0, 3, searchInput.value, null);
            console.log('Kết quả tìm kiếm khách hàng:', store.khachHangSearch);
        }
        // Nhân viên
        else if (route.name === 'admin-quan-ly-nhan-vien') {
            store.searchs = searchInput.value;
            await store.searchNhanVien(searchInput.value, 0, 5);
            console.log('Kết quả tìm kiếm nhân viên:', store.nhanVienSearch);
        }
        // Voucher
        else if (route.name === 'admin-quan-ly-voucher') {
            store.voucherSearchs = searchInput.value;
            await store.searchVoucher(searchInput.value, 0, 5);
            console.log('Kết quả tìm kiếm voucher:', store.voucherSearch);
        }
        // Khuyến mãi
        else if (route.name === 'admin-quan-ly-khuyen-mai') {
            store.khuyenMaiSearchs = searchInput.value;
            await store.searchKhuyenMai(searchInput.value, 0, 5);
            console.log('Kết quả tìm kiếm khuyến mãi:', store.khuyenMaiSearch);
        }
        else {
            console.log('Chức năng tìm kiếm chưa được hỗ trợ cho route này:', route.name);
        }
    } catch (error) {
        console.error('Lỗi khi tìm kiếm:', error);
    }
};

</script>

<style scoped>
.form-control {
    border: none;
    outline: none;
}

.icon-search {
    font-size: 16px;
    color: #565656;
    cursor: pointer;
}

.form-control:focus {
    box-shadow: none;
}
</style>