<template>
    <!-- <a-space align="center" style="margin-bottom: 16px">
        CheckStrictly:
        <a-switch v-model:checked="rowSelection.checkStrictly"></a-switch>
    </a-space> -->
    <!-- <a-table :columns="columns" :data-source="data" :row-selection="rowSelection" :expandable="expandableConfig"
        class="components-table-demo-nested" /> -->
    <div>
        <menuAction ref="menuActionRef" @refresh-data="handleMenuActionRefresh" />
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h4 class="ms-3 mb-0">Danh sách sản phẩm</h4>
            <a-button @click="refreshData" :loading="isLoading" type="primary" class="refresh-button" shape="circle">
                <ReloadOutlined />
            </a-button>
        </div>

        <!-- Loading skeleton -->
        <template v-if="isLoading">
            <a-skeleton active :paragraph="{ rows: 10 }" />
        </template>

        <!-- Data table -->
        <template v-else>
            <!-- Thêm bộ lọc trạng thái -->
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div class="status-filter">
                    <a-radio-group v-model:value="statusFilter" @change="handleStatusFilterChange" button-style="solid"
                        class="custom-radio-group">
                        <a-radio-button value="">Tất cả</a-radio-button>
                        <a-radio-button :value="true">Hoạt động</a-radio-button>
                        <a-radio-button :value="false">Không hoạt động</a-radio-button>
                    </a-radio-group>
                    <span class="ms-3">
                        <span class="badge bg-primary">{{ filteredDisplayData.length }} sản phẩm</span>
                        <template v-if="statusFilter !== ''">
                            <strong class="ms-2">Lọc theo: {{ statusFilter === true ? 'Hoạt động' : 'Không hoạt động' }}</strong>
                            <template v-if="displayData.length > filteredDisplayData.length">
                                <span class="text-muted ms-2">(trong tổng số {{ displayData.length }} sản phẩm)</span>
                            </template>
                        </template>
                    </span>
                </div>
            </div>

            <a-table :columns="columns" :row-selection="rowSelection" :data-source="filteredDisplayData"
                class="components-table-demo-nested" :row-key="record => record.id_san_pham"
                :pagination="{ pageSize: 10, showSizeChanger: true, pageSizeOptions: ['10', '20', '50'] }"
                :scroll="{ x: 1300 }" @change="handleTableChange">
                <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'trang_thai'">
                        <a-switch @change="(checked) => handleSwitchClick(record.id_san_pham, checked)"
                            :style="{ backgroundColor: record.trang_thai === true ? '#ff6600' : '#ccc' }"
                            :checked="record.trang_thai === true"
                            :disabled="record.trang_thai === false"
                            class="custom-orange-switch" />
                        <!-- <span class="ms-2">{{ record.trang_thai === true ? 'Hoạt động' : 'Không hoạt động' }}</span> -->
                    </template>
                    <template v-if="column.key === 'hinh_anh'">
                        <a-image style="width: 40px; height: 40px;" :src="record.hinh_anh" @error="handleImageError" />
                    </template>
                    <template v-if="column.key === 'gia_ban'">
                        {{ formatCurrency(record.gia_ban) }}
                    </template>
                    <template v-if="column.key === 'action'">
                        <div class="action-buttons d-flex gap-2">
                            <a-button type="primary" @click="() => showVariants(record)"
                                class="d-flex align-items-center view-btn primary-btn-table"
                                title="Xem chi tiết biến thể"
                                size="small" style="background-color: #ff6600 !important; border-color: #ff6600 !important;">
                                <EyeOutlined />
                                <span class="btn-text">Xem</span>
                            </a-button>
                            <a-button v-if="store.id_roles !== 3" type="danger" @click="changeRouter(record.id_san_pham)"
                                class="d-flex align-items-center btn btn-danger edit-btn danger-btn-table"
                                title="Chỉnh sửa sản phẩm"
                                size="small" style="background-color: white !important; border-color: #ff6600 !important; color: #ff6600 !important;">
                                <EditOutlined />
                                <span class="btn-text">Sửa</span>
                            </a-button>
                        </div>
                    </template>
                </template>
            </a-table>

            <!-- Cache info -->
            <!-- <a-alert class="mt-3" type="info" show-icon>
                <template #message>
                    <span>Dữ liệu được lưu trong bộ nhớ tạm (cache) để tối ưu tốc độ tải. Thời gian lưu: 5 phút.</span>
                </template>
                <template #description>
                    <span>Thời gian tải trang: {{ loadTime }}ms</span>
                </template>
            </a-alert> -->
        </template>

        <!-- Drawer for product variants -->
        <template v-if="drawerVisible">
            <a-drawer v-model:open="drawerVisible" title="Chi tiết biến thể sản phẩm" placement="right" :width="700"
                :footer-style="{ textAlign: 'right' }" @close="closeVariantDrawer">
                <template v-if="currentProduct">
                    <div class="product-info mb-4">
                        <h3>{{ currentProduct.ten_san_pham }}</h3>
                        <div class="d-flex gap-3 align-items-center">
                            <a-image style="width: 60px; height: 60px;" :src="currentProduct.hinh_anh"
                                @error="handleImageError" />
                            <div>
                                <p><strong>Mã sản phẩm:</strong> {{ currentProduct.ma_san_pham }}</p>
                                <p>
                                    <strong>Trạng thái:</strong>
                                    <span :class="currentProduct.trang_thai === true ? 'text-success' : 'text-danger'">
                                        {{ currentProduct.trang_thai === true ? 'Hoạt động' : 'Không hoạt động' }}
                                    </span>
                                </p>
                            </div>
                        </div>
                    </div>

                    <!-- Loading skeleton cho CTSP -->
                    <template v-if="drawerLoading">
                        <a-skeleton active :paragraph="{ rows: 5 }" />
                    </template>

                    <template v-else>
                        <h4>Danh sách biến thể</h4>

                        <!-- Bộ lọc trạng thái CTSP -->
                        <div class="variant-status-filter mb-3">
                            <a-radio-group v-model:value="ctspStatusFilter" @change="filterCTSPByStatus"
                                class="custom-radio-group">
                                <a-radio-button value="">Tất cả</a-radio-button>
                                <a-radio-button :value="true">Hoạt động</a-radio-button>
                                <a-radio-button :value="false">Không hoạt động</a-radio-button>
                            </a-radio-group>
                            <span class="ms-3" v-if="ctspStatusFilter !== ''">
                                <strong>Lọc theo:</strong> {{ ctspStatusFilter === true ? 'Hoạt động' : 'Không hoạt động' }}
                                <span class="badge bg-primary ms-2">{{ filteredCTSPCount }} biến thể</span>
                            </span>
                        </div>

                        <!-- Các nút chức năng bulk action -->
                        <div v-if="selectedCTSPKeys.length >= 2" class="bulk-actions mb-3 d-flex gap-2">
                            <a-button type="primary" @click="bulkChangeStatus(true)" class="bulk-action-btn">
                                <CheckCircleOutlined />
                                <span>Chuyển thành Hoạt động</span>
                            </a-button>
                            <a-button danger @click="bulkChangeStatus(false)" class="bulk-action-btn">
                                <StopOutlined />
                                <span>Chuyển thành Không hoạt động</span>
                            </a-button>
                        </div>

                        <!-- Bảng CTSP -->
                        <a-table :columns="columnsCTSP" :row-selection="{
                            selectedRowKeys: selectedCTSPKeys,
                            onChange: (keys, rows) => handleCTSPSelection(keys, rows, currentProduct.id_san_pham)
                        }" :data-source="filteredCTSPData" :pagination="false"
                            :row-key="record => record.id_chi_tiet_san_pham" class="ctsp-table">
                            <template #bodyCell="{ column, record: ctspRecord }">
                                <template v-if="column.key === 'trang_thai'">
                                    <a-switch
                                        :style="{ backgroundColor: ctspRecord.trang_thai === true ? '#ff6600' : '#ccc' }"
                                        size="small" :checked="ctspRecord.trang_thai === true"
                                        @change="() => changeStatusCTSP(ctspRecord)"
                                        class="custom-orange-switch" />
                                    <!-- <span class="ms-2 small">{{ ctspRecord.trang_thai === true ? 'Hoạt động' : 'Không hoạt động' }}</span> -->
                                </template>
                                <template v-if="column.key === 'gia_ban'">
                                    {{ formatCurrency(ctspRecord.gia_ban) }}
                                </template>
                                <template v-if="column.key === 'qrcode'">
                                    <a-tooltip title="Tải QR Code">
                                        <a-qrcode @click="showConfirmDownload(ctspRecord)" :size="60"
                                            :value="ctspRecord.id_chi_tiet_san_pham.toString()" />
                                    </a-tooltip>
                                </template>
                                <template v-if="column.key === 'action'">
                                    <a-button @click="showDrawer" type="" style="color: white;"
                                        class="d-flex align-items-center btn btn-warning">
                                        <EditOutlined />Sửa
                                    </a-button>
                                </template>
                            </template>
                        </a-table>
                    </template>
                </template>
            </a-drawer>
        </template>

        <!-- Existing drawer for editing product details -->
        <a-drawer v-model:open="open" class="custom-class" root-class-name="root-class-name"
            :root-style="{ color: 'black' }" title="Chi tiết sản phẩm" placement="left"
            @after-open-change="afterOpenChange" :footer-style="{ textAlign: 'right' }">
            <a-form :model="productDetails" layout="vertical" @submit.prevent="handleSubmit">
                <a-form-item label="Tên sản phẩm" name="ten_san_pham">
                    <a-input v-model:value="productDetails.ten_san_pham" placeholder="Nhập tên sản phẩm" />
                </a-form-item>

                <a-form-item label="Danh mục" name="danh_muc_id">
                    <a-select v-model:value="productDetails.danh_muc_id" placeholder="Chọn danh mục">
                        <a-select-option v-for="item in danhMucList" :key="item.id_danh_muc" :value="item.id_danh_muc">
                            {{ item.ten_danh_muc }}
                        </a-select-option>
                    </a-select>
                </a-form-item>

                <a-form-item label="Giá bán" name="gia_ban">
                    <a-input-number v-model:value="productDetails.gia_ban" placeholder="Nhập giá bán"
                        style="width: 100%;" />
                </a-form-item>

                <!-- Thêm các trường khác tương tự -->

                <a-form-item>
                    <a-button type="primary" html-type="submit">Lưu</a-button>
                    <a-button @click="closeDrawer">Hủy</a-button>
                </a-form-item>
            </a-form>
        </a-drawer>
    </div>

</template>
<script setup>
import menuAction from '@/components/admin-components/QuanLySanPham/menuAction.vue';
import {
    EditOutlined, PlusOutlined, DeleteOutlined, EyeOutlined, ReloadOutlined,
    CheckCircleOutlined, StopOutlined, QrcodeOutlined
} from '@ant-design/icons-vue';
import { onMounted, ref, render, computed, watch, onBeforeUnmount, nextTick, onUnmounted, h } from 'vue';
import { useGbStore } from '@/stores/gbStore';
import { message, Modal as AModal } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { Modal } from 'bootstrap';
import '../../../config/fonts/Roboto-bold'
import '../../../config/fonts/Roboto-normal';
import QRCode from 'qrcode';
import { jsPDF } from 'jspdf';

// Khai báo các key cache và thời gian cache
const PRODUCTS_CACHE_KEY = 'products_data';
const CTSP_CACHE_PREFIX = 'ctsp_for_product_';
const CACHE_DURATION = 5 * 60 * 1000; // 5 phút
// Hàm xử lý sự kiện search-filter-changed
const handleSearchFilterChanged = (event) => {
    console.log('Nhận được sự kiện search-filter-changed');
    if (event.detail && event.detail.results) {
        console.log('Cập nhật displayData với', event.detail.results.length, 'sản phẩm');
        // Bây giờ đây là lệnh gán hợp lệ vì displayData là ref, không phải computed
        displayData.value = event.detail.results.map((item, index) => ({
            stt: index + 1,
            key: item.id_san_pham,
            id_san_pham: item.id_san_pham,
            ma_san_pham: item.ma_san_pham || '',
            ten_san_pham: item.ten_san_pham || '',
            hinh_anh: item.hinh_anh || '',
            chi_muc: (item.ten_danh_muc || '') + "/" + (item.ten_thuong_hieu || '') + "/" + (item.ten_chat_lieu || ''),
            trang_thai: item.trang_thai || '',
            tong_so_luong: item.tong_so_luong || 0,
            ngay_cap_nhat: item.ngay_cap_nhat || ''
        }));
    }
};
// Hàm kiểm tra cache còn hạn không
const isCacheValid = (cacheKey) => {
    const cacheData = JSON.parse(localStorage.getItem(cacheKey) || 'null');
    if (!cacheData) return false;
    return Date.now() - cacheData.timestamp < CACHE_DURATION;
};

// Hàm lấy dữ liệu từ cache
const getFromCache = (cacheKey) => {
    const cacheData = JSON.parse(localStorage.getItem(cacheKey) || 'null');
    if (!cacheData) return null;
    return cacheData.data;
};

// Hàm lưu dữ liệu vào cache
const saveToCache = (cacheKey, data) => {
    const cacheData = {
        data,
        timestamp: Date.now()
    };
    localStorage.setItem(cacheKey, JSON.stringify(cacheData));
};

// Xóa cache
const clearCache = (key) => {
    try {
        localStorage.removeItem(key);
        console.log(`Đã xóa cache: ${key}`);
    } catch (e) {
        console.error('Lỗi khi xóa cache:', e);
    }
};

// Xóa tất cả cache liên quan đến sản phẩm
const clearAllProductsCache = () => {
    try {
        // Xóa cache danh sách sản phẩm
        clearCache(PRODUCTS_CACHE_KEY);

        // Xóa tất cả cache chi tiết sản phẩm
        // Lặp qua tất cả các key trong localStorage
        for (let i = 0; i < localStorage.length; i++) {
            const key = localStorage.key(i);
            // Nếu key bắt đầu bằng prefix của CTSP, xóa nó
            if (key && key.startsWith(CTSP_CACHE_PREFIX)) {
                clearCache(key);
            }
        }

        console.log('Đã xóa tất cả cache sản phẩm');
    } catch (e) {
        console.error('Lỗi khi xóa tất cả cache sản phẩm:', e);
    }
};

// Force refresh cache param
const forceRefresh = ref(false);

// Thêm biến loading để hiển thị skeleton
const isLoading = ref(true);

const router = useRouter();
const open = ref(false);
const store = useGbStore();

// New refs for variant drawer
const drawerVisible = ref(false);
const currentProduct = ref(null);

// Thêm biến loading state cho drawer
const drawerLoading = ref(false);

// Biến lưu thời gian tải trang
const loadTime = ref(0);

// Function to show variants drawer with lazy loading
const showVariants = async (product) => {
    drawerVisible.value = true;
    drawerLoading.value = true;
    currentProduct.value = product;

    try {
        // Kiểm tra cache trước khi gọi API
        const cacheKey = `${CTSP_CACHE_PREFIX}${product.id_san_pham}`;

        if (!forceRefresh.value && isCacheValid(cacheKey)) {
            console.log('Sử dụng cache cho CTSP');
            const cachedData = getFromCache(cacheKey);
            if (cachedData) {
                productCTSPMap.value.set(product.id_san_pham, cachedData);
                drawerLoading.value = false;
                return;
            }
        }

        // Nếu không có cache, gọi API để lấy CTSP cho sản phẩm cụ thể
        console.log('Gọi API lấy CTSP cho sản phẩm:', product.id_san_pham);
        await getCTSPForProduct(product);

    } catch (error) {
        console.error('Lỗi khi tải chi tiết sản phẩm:', error);
        message.error('Không thể tải thông tin chi tiết sản phẩm');
    } finally {
        drawerLoading.value = false;
    }
};

// Function to close variant drawer and clean up
const closeVariantDrawer = () => {
    drawerVisible.value = false;
    currentProduct.value = null;
    ctspStatusFilter.value = ''; // Reset bộ lọc
    drawerLoading.value = false;
};

// Hàm kiểm tra chất lượng dữ liệu để debug
const verifyDataQuality = () => {
    if (!displayData.value || displayData.value.length === 0) {
        console.warn('verifyDataQuality: Không có dữ liệu để kiểm tra');
        return;
    }

    // Kiểm tra trạng thái
    const totalItems = displayData.value.length;
    const booleanStatusItems = displayData.value.filter(item => typeof item.trang_thai === 'boolean').length;
    const trueStatusItems = displayData.value.filter(item => item.trang_thai === true).length;
    const falseStatusItems = displayData.value.filter(item => item.trang_thai === false).length;
    const stringStatusItems = displayData.value.filter(item => typeof item.trang_thai === 'string').length;
    const undefinedStatusItems = displayData.value.filter(item => item.trang_thai === undefined).length;

    console.log('================ KIỂM TRA CHẤT LƯỢNG DỮ LIỆU ================');
    console.log(`Tổng số sản phẩm: ${totalItems}`);
    console.log(`Số sản phẩm có trạng thái kiểu Boolean: ${booleanStatusItems} (${(booleanStatusItems/totalItems*100).toFixed(1)}%)`);
    console.log(`Số sản phẩm có trạng thái true: ${trueStatusItems} (${(trueStatusItems/totalItems*100).toFixed(1)}%)`);
    console.log(`Số sản phẩm có trạng thái false: ${falseStatusItems} (${(falseStatusItems/totalItems*100).toFixed(1)}%)`);

    if (stringStatusItems > 0) {
        console.warn(`Có ${stringStatusItems} sản phẩm có trạng thái kiểu String! Cần chuyển đổi.`);
        // Liệt kê các giá trị string độc đáo
        const uniqueStringValues = new Set(displayData.value
            .filter(item => typeof item.trang_thai === 'string')
            .map(item => item.trang_thai));
        console.warn('Giá trị chuỗi được tìm thấy:', Array.from(uniqueStringValues));
    }

    if (undefinedStatusItems > 0) {
        console.warn(`Có ${undefinedStatusItems} sản phẩm có trạng thái undefined! Cần kiểm tra.`);
    }

    // Kiểm tra tổng cộng
    if (trueStatusItems + falseStatusItems !== totalItems) {
        console.error(`Tổng số không khớp! Tổng cộng (${trueStatusItems + falseStatusItems}) khác với tổng số sản phẩm (${totalItems})`);
    } else {
        console.log('Tổng cộng đúng: tất cả sản phẩm đều có trạng thái Boolean.');
    }
    console.log('================================================================');
};


const changeRouter = (path) => {
    try {
        // Wait until current tick completes before navigating
        setTimeout(() => {
            router.push('/admin/quanlysanpham/update/' + path);
        }, 0);
    } catch (error) {
        console.error('Navigation error:', error);
        message.error('Không thể mở trang chỉnh sửa. Vui lòng thử lại.');
    }
}
const productDetails = ref({
    ten_san_pham: '',
    danh_muc_id: null,
    gia_ban: null,
    // Thêm các trường khác
});
function handleSubmit() {
    // Xử lý logic lưu sản phẩm
    console.log('Product details:', productDetails.value);
    // Gọi; API hoặc cập nhật store
};
function closeDrawer() {
    open.value = false;
};
const danhMucList = ref([]);

const variants = ref([]);

const showDrawer = () => {
    open.value = true;
}
// Update the columns to include options for both highest and lowest price
const columns = [
    {
        title: '#',
        dataIndex: 'stt',
        key: 'stt',
        width: '5%',
        align: 'center',
        render: (text, record, index) => index + 1
    },
    {
        title: 'Mã sản phẩm',
        dataIndex: 'ma_san_pham',
        key: 'ma_san_pham',
        width: '8%',
        sorter: (a, b) => a.ma_san_pham.localeCompare(b.ma_san_pham),
        sortDirections: ['ascend', 'descend'],
    },
    {
        title: 'Tên sản phẩm',
        dataIndex: 'ten_san_pham',
        key: 'ten_san_pham',
        width: '15%',
        sorter: (a, b) => a.ten_san_pham.localeCompare(b.ten_san_pham),
        sortDirections: ['ascend', 'descend'],
    },
    {
        title: 'Hình ảnh',
        dataIndex: 'hinh_anh',
        key: 'hinh_anh',
        width: '8%',
        customRender: ({ text }) => {
            if (!text) return null;
            // Xử lý đường dẫn ảnh
            let imageUrl = text;
            if (!text.startsWith('http')) {
                imageUrl = text.startsWith('/') ?
                    `http://localhost:8080${text}` :
                    `http://localhost:8080/${text}`;
            }
            return h('img', {
                src: imageUrl,
                style: {
                    width: '40px',
                    height: '40px',
                    objectFit: 'cover',
                    borderRadius: '4px'
                },
                onError: (e) => {
                    console.warn('Lỗi tải hình ảnh:', text);
                    e.target.src = '/image/logo/error-image.png';
                }
            });
        }
    },
    {
        title: 'SL',
        dataIndex: 'tong_so_luong',
        key: 'tong_so_luong',
        width: '8%',
        sorter: (a, b) => a.tong_so_luong - b.tong_so_luong,
        sortDirections: ['ascend', 'descend'],
    },
    {
        title: 'Danh mục/Thương hiệu/Chất liệu',
        dataIndex: 'chi_muc',
        key: 'chi_muc',
        width: '22%',
    },
    {
        title: 'Trạng thái',
        dataIndex: 'trang_thai',
        key: 'trang_thai',
        width: '10%',
    },
    {
        title: 'Hành động',
        dataIndex: 'action',
        key: 'action',
        width: '25%',
    },
];
const columnsCTSP = [
    // {
    //     title: 'Tên sản phẩm',
    //     dataIndex: 'ten_san_pham',
    //     key: 'ten_san_pham',
    // },
    {
        title: 'Màu sắc',
        dataIndex: 'mau_sac',
        key: 'mau_sac',
    },
    {
        title: 'Size',
        dataIndex: 'size',
        key: 'size',
    },
    {
        title: 'Số lượng',
        dataIndex: 'so_luong',
        key: 'so_luong',
        sorter: (a, b) => a.so_luong - b.so_luong,
        sortDirections: ['ascend', 'descend'],
    },
    {
        title: 'Giá',
        dataIndex: 'gia_ban',
        key: 'gia_ban',
        sorter: (a, b) => a.gia_ban - b.gia_ban,
        sortDirections: ['ascend', 'descend'],
        render: (text) => formatCurrency(text)
    },
    {
        title: 'Trạng thái',
        dataIndex: 'trang_thai',
        key: 'trang_thai',
    },
    {
        title: 'QR Code',
        key: 'qrcode',
    },
];
const showConfirmDownload = (ctspRecord) => {
    console.log('Record:', ctspRecord);
    AModal.confirm({
        title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
            h(QrcodeOutlined, { style: 'color: #1890ff; font-size: 22px;' }),
            h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Tải QR Code')
        ]),
        content: () => h('div', { style: 'padding: 8px 0;' }, [
            h('p', { style: 'margin: 0 0 12px 0; font-size: 14px;' }, `Bạn có muốn tải QR Code cho sản phẩm này không?`),
            h('div', { style: 'background: #e6f7ff; padding: 12px; border-radius: 6px; border: 1px solid #91d5ff;' }, [
                h('div', { style: 'color: #096dd9; font-size: 13px;' }, [
                    h('div', { style: 'font-weight: 500; margin-bottom: 6px;' }, 'Thông tin sản phẩm:'),
                    h('div', null, `• ${ctspRecord.ten_san_pham}`),
                    h('div', null, `• Màu: ${ctspRecord.mau_sac} - Size: ${ctspRecord.size}`)
                ])
            ])
        ]),
        okText: 'Tải v\u1ec1',
        cancelText: 'Hủy',
        okButtonProps: { size: 'large', style: { height: '38px' } },
        cancelButtonProps: { size: 'large', style: { height: '38px' } },
        centered: true,
        width: 450,
        onOk: () => generateQRCodePDF(ctspRecord),
    });
};

const generateQRCodePDF = async (ctspRecord) => {
    try {
        if (!ctspRecord || !ctspRecord.id_chi_tiet_san_pham) {
            throw new Error('Dữ liệu sản phẩm không hợp lệ.');
        }

        const doc = new jsPDF();

        // Tạo mã QR Code từ id_chi_tiet_san_pham
        const qrCodeDataUrl = await QRCode.toDataURL(ctspRecord.id_chi_tiet_san_pham.toString());

        // Thêm thông tin sản phẩm lên đầu file PDF
        doc.setFont("Roboto", "bold");
        doc.setFontSize(16);
        const title = "Thông tin sản phẩm chi tiết";
        const pageWidth = doc.internal.pageSize.getWidth();
        const titleWidth = doc.getTextWidth(title);
        const centerX = (pageWidth - titleWidth) / 2;
        doc.text(title, centerX, 20);

        doc.setFont("Roboto", "normal");
        doc.setFontSize(12);
        doc.text(`Tên sản phẩm: ${ctspRecord.ten_san_pham}`, 20, 40);
        doc.text(`Màu sắc: ${ctspRecord.mau_sac}`, 20, 50);
        doc.text(`Size: ${ctspRecord.size}`, 20, 60);
        doc.text(`Đơn giá: ${ctspRecord.gia_ban}`, 20, 70);

        // Thêm mã QR Code to hơn vào giữa file PDF
        const qrSize = 100;
        const qrX = (pageWidth - qrSize) / 2;
        doc.addImage(qrCodeDataUrl, 'PNG', qrX, 80, qrSize, qrSize);

        // Lưu file PDF
        doc.save(`QRCode_${ctspRecord.id_chi_tiet_san_pham}.pdf`);
    } catch (error) {
        console.error("Lỗi khi tạo QR Code PDF:", error);
        message.error("Không thể tạo QR Code PDF. Vui lòng thử lại.");
    }
};

const data = ref([]);
const selectedCTSPKeys = ref([]);
const rowSelection = ref({
    selectedRowKeys: [],
    onChange: (selectedRowKeys, selectedRows) => {
        rowSelection.value.selectedRowKeys = selectedRowKeys;

        // Lưu danh sách các sản phẩm đã chọn
        const selectedProducts = new Set(selectedRowKeys);

        // Cập nhật chọn CTSP cho các sản phẩm đã chọn
        selectedRowKeys.forEach(async (key) => {
            const record = data.value.find(item => item.id_san_pham === key);
            if (record) {
                await getCTSPForProduct(record);
                const childItems = productCTSPMap.value.get(record.id_san_pham) || [];
                childItems.forEach(item => {
                    if (!selectedCTSPKeys.value.includes(item.id_chi_tiet_san_pham)) {
                        selectedCTSPKeys.value.push(item.id_chi_tiet_san_pham);
                    }
                });
            }
        });

        // Loại bỏ CTSP của các sản phẩm bị bỏ chọn
        data.value.forEach(item => {
            if (!selectedProducts.has(item.id_san_pham)) {
                const childItems = productCTSPMap.value.get(item.id_san_pham) || [];
                selectedCTSPKeys.value = selectedCTSPKeys.value.filter(
                    key => !childItems.some(child => child.id_chi_tiet_san_pham === key)
                );
            }
        });

        // Truyền danh sách sản phẩm đã chọn cho menuAction component
        if (menuActionRef.value) {
            menuActionRef.value.updateSelectedRows(selectedRows);
        }
    }
});

// Hàm format dữ liệu CTSP
const formatCTSPData = (ctspList) => {
    return ctspList.map(item => ({
        key: item.id_chi_tiet_san_pham,
        ten_san_pham: item.ten_san_pham,
        hinh_anh: item.hinh_anh,
        gia_ban: item.gia_ban,
        mau_sac: item.ten_mau,
        size: item.gia_tri + ' ' + item.don_vi,
        so_luong: item.so_luong,
        trang_thai: item.trang_thai,
    }));
};
// Thêm ref để lưu trữ CTSP cho từng sản phẩm
const productCTSPMap = ref(new Map());
const getCTSPForProduct = async (product) => {
    try {
        // Gọi API để lấy CTSP cho sản phẩm cụ thể
        console.log('Gọi API lấy CTSP cho sản phẩm:', product.id_san_pham);
        await store.getCTSPBySanPham(product.id_san_pham);

        // Lấy và kiểm tra dữ liệu trả về từ API
        const rawData = store.getCTSPBySanPhams;
        if (rawData && rawData.length > 0) {
            console.log('Dữ liệu CTSP nhận được từ API:', rawData.length, 'bản ghi');

            // Kiểm tra kiểu dữ liệu của trường trang_thai trong bản ghi đầu tiên để debug
            if (rawData.length > 0) {
                const firstItem = rawData[0];
                console.log(`Kiểm tra trạng thái của bản ghi đầu tiên:`);
                console.log(`- Giá trị: ${firstItem.trang_thai}`);
                console.log(`- Kiểu dữ liệu: ${typeof firstItem.trang_thai}`);
            }

            // Đếm số lượng trạng thái mỗi loại
            const booleanStatusItems = rawData.filter(item => typeof item.trang_thai === 'boolean').length;
            const stringStatusItems = rawData.filter(item => typeof item.trang_thai === 'string').length;
            const undefinedStatusItems = rawData.filter(item => item.trang_thai === undefined).length;

            if (stringStatusItems > 0) {
                console.warn(`${stringStatusItems}/${rawData.length} chi tiết sản phẩm có trạng thái dạng chuỗi - sẽ được chuyển đổi.`);
            }
        }

        // Chuyển đổi và chuẩn hóa dữ liệu CTSP
        const ctspList = store.getCTSPBySanPhams.map(ctsp => {
            // Xử lý trạng thái
            let trangThai;
            if (typeof ctsp.trang_thai === 'boolean') {
                // Nếu đã là boolean, giữ nguyên
                trangThai = ctsp.trang_thai;
            } else if (ctsp.trang_thai === 'true' || ctsp.trang_thai === 'Hoạt động' || ctsp.trang_thai === 1 || ctsp.trang_thai === '1') {
                // Chuyển đổi các trường hợp active thành true
                trangThai = true;
            } else {
                // Các trường hợp còn lại đều xem là false
                trangThai = false;
            }

            return {
                key: ctsp.id_chi_tiet_san_pham,
                id_chi_tiet_san_pham: ctsp.id_chi_tiet_san_pham,
                id_san_pham: product.id_san_pham,
                ten_san_pham: ctsp.ten_san_pham,
                hinh_anh: ctsp.hinh_anh,
                gia_ban: ctsp.gia_ban,
                mau_sac: ctsp.ten_mau,
                size: ctsp.gia_tri + ' ' + ctsp.don_vi,
                so_luong: ctsp.so_luong,
                trang_thai: trangThai, // Sử dụng trạng thái đã chuẩn hóa
            };
        });

        // Kiểm tra số lượng CTSP theo trạng thái sau khi chuẩn hóa
        const activeCtsp = ctspList.filter(item => item.trang_thai === true).length;
        const inactiveCtsp = ctspList.filter(item => item.trang_thai === false).length;
        console.log(`Phân tích CTSP sau khi chuẩn hóa: ${activeCtsp} hoạt động, ${inactiveCtsp} không hoạt động`);

        // Lưu vào map để sử dụng
        productCTSPMap.value.set(product.id_san_pham, ctspList);
        return ctspList;
    } catch (error) {
        console.error('Lỗi khi lấy dữ liệu CTSP:', error);
        message.error('Không thể lấy thông tin chi tiết sản phẩm');
        return [];
    }
};

const handleSwitchClick = (idSanPham, checked) => {
    if (store.id_roles === 3) {
        message.warning('Bạn không có quyền thay đổi trạng thái sản phẩm!');
        return;
    }
    // Nếu có quyền, gọi hàm thay đổi trạng thái
    changeStatusSanPham(idSanPham, checked);
};
const changeStatusSanPham = async (id, checked) => {
    try {
        // Hiển thị thông báo đang xử lý
        const messageKey = `status-change-${id}`;
        message.loading({ content: 'Đang cập nhật trạng thái...', key: messageKey });

        // Lưu lại trạng thái gốc trước khi cập nhật
        const originalData = [...data.value];

        // Gọi API thông qua store
        const result = await store.changeStatusSanPham(id);

        if (result.success) {
            // Hiển thị thông báo thành công
            message.success({
                content: result.message,
                key: messageKey,
                duration: 2
            });

            // Đảm bảo UI đã được cập nhật chính xác theo response
            // Tìm và cập nhật sản phẩm trong danh sách hiển thị
            displayData.value = displayData.value.map(item => {
                if (item.id_san_pham === id) {
                    return { ...item, trang_thai: result.newStatus };
                }
                return item;
            });

            // Cập nhật cache nếu có
            // const productsCache = getFromCache(PRODUCTS_CACHE_KEY);
            // if (productsCache) {
            //     const updatedCache = productsCache.map(p => {
            //         if (p.id_san_pham === id) {
            //             return { ...p, trang_thai: result.newStatus };
            //         }
            //         return p;
            //     });
            //     saveToCache(PRODUCTS_CACHE_KEY, updatedCache);
            // }

            // Nếu CTSP đã được tải cho sản phẩm này, cần cập nhật chúng
            if (productCTSPMap.value.has(id)) {
                // Lấy và tải lại chi tiết sản phẩm để đảm bảo đồng bộ dữ liệu
                await getCTSPForProduct({ id_san_pham: id });
            }
        } else {
            // Hiển thị thông báo lỗi
            message.error({
                content: result.message || 'Không thể cập nhật trạng thái',
                key: messageKey,
                duration: 2
            });

            // Khôi phục dữ liệu ban đầu trong trường hợp lỗi
            data.value = originalData;
        }
    } catch (error) {
        console.error('Lỗi khi thay đổi trạng thái:', error);
        message.error('Có lỗi xảy ra khi thay đổi trạng thái');
    }
};
const handleSwitchClickCTSP = (idCTSP) => {
    if (store.id_roles === 3) {
        message.warning('Bạn không có quyền thay đổi trạng thái sản phẩm!');
        return;
    }
    // Nếu có quyền, gọi hàm thay đổi trạng thái
    changeStatusCTSP(idCTSP);
};
const changeStatusCTSP = async (ctspRecord) => {
    try {
        const ctspId = ctspRecord.id_chi_tiet_san_pham;
        const currentStatus = ctspRecord.trang_thai;
        const productId = currentProduct.value.id_san_pham;

        // Xác định trạng thái mới (ngược lại với trạng thái hiện tại)
        const newStatus = currentStatus === true ? false : true;

        // Thông báo đang xử lý
        const messageKey = `status-change-${ctspId}`;
        message.loading({ content: 'Đang cập nhật trạng thái...', key: messageKey });

        // Lưu trạng thái ban đầu để hoàn tác nếu cần
        const originalCtspList = [...productCTSPMap.value.get(productId) || []];
        const originalData = [...displayData.value];
        const originalCurrentProduct = { ...currentProduct.value };

        // Cập nhật UI ngay lập tức - Optimistic Update
        const updatedCtspList = originalCtspList.map(item => {
            if (item.id_chi_tiet_san_pham === ctspId) {
                return { ...item, trang_thai: newStatus };
            }
            return item;
        });

        // Cập nhật Map để UI hiển thị đúng - Dùng cách set lại toàn bộ Map để trigger reactivity
        const newMap = new Map(productCTSPMap.value);
        newMap.set(productId, updatedCtspList);
        productCTSPMap.value = newMap;

        // Gọi API thông qua store
        const result = await store.updateCTSPStatus(ctspId, newStatus);

        if (result.success) {
            // Hiển thị thông báo thành công
            message.success({
                content: result.message,
                key: messageKey,
                duration: 2
            });

            // Kiểm tra xem có thông tin cập nhật sản phẩm cha không
            if (result.data && result.data.sanPham && result.data.sanPham.id_san_pham !== undefined) {
                const parentId = result.data.sanPham.id_san_pham;
                const newParentStatus = result.data.sanPham.trang_thai;

                console.log(`Cập nhật UI sản phẩm ${parentId} thành ${newParentStatus}`);

                // Cập nhật danh sách sản phẩm với trạng thái mới của sản phẩm cha
                displayData.value = displayData.value.map(item => {
                    if (item.id_san_pham === parentId) {
                        return { ...item, trang_thai: newParentStatus };
                    }
                    return item;
                });

                // Cập nhật sản phẩm trong drawer nếu đang hiển thị
                if (currentProduct.value && currentProduct.value.id_san_pham === parentId) {
                    currentProduct.value = {
                        ...currentProduct.value,
                        trang_thai: newParentStatus
                    };
                }
            }
        } else {
            // Hiển thị thông báo lỗi
            message.error({
                content: result.message || 'Không thể cập nhật trạng thái',
                key: messageKey,
                duration: 2
            });

            // Hoàn tác UI nếu có lỗi
            const rollbackMap = new Map(productCTSPMap.value);
            rollbackMap.set(productId, originalCtspList);
            productCTSPMap.value = rollbackMap;

            displayData.value = originalData;
            if (currentProduct.value) {
                currentProduct.value = originalCurrentProduct;
            }
        }
    } catch (error) {
        console.error('Lỗi khi thay đổi trạng thái:', error);
        message.error('Có lỗi xảy ra khi thay đổi trạng thái');
    }
};

const afterOpenChange = bool => {
    console.log('open', bool);
};

const handleOk = e => {
    console.log(e);
    open.value = false;
};

// Update the handleCTSPSelection to work with the drawer
const handleCTSPSelection = (selectedKeys, selectedRows, parentId) => {
    // Cập nhật lại danh sách CTSP đã chọn, loại bỏ các CTSP cũ của sản phẩm hiện tại
    const otherProductCTSPs = selectedCTSPKeys.value.filter(key => {
        // Kiểm tra key có thuộc sản phẩm hiện tại không
        const currentProductCTSPs = productCTSPMap.value.get(parentId) || [];
        return !currentProductCTSPs.some(item => item.id_chi_tiet_san_pham === key);
    });

    // Thêm các CTSP mới được chọn của sản phẩm hiện tại
    selectedCTSPKeys.value = [...otherProductCTSPs, ...selectedKeys];

    // Cập nhật selection của sản phẩm cha
    const childItems = productCTSPMap.value.get(parentId) || [];
    const allChildKeys = childItems.map(item => item.id_chi_tiet_san_pham);
    const allChildrenSelected = allChildKeys.length > 0 && allChildKeys.every(key => selectedKeys.includes(key));

    const currentParentKeys = [...rowSelection.value.selectedRowKeys];
    if (allChildrenSelected && !currentParentKeys.includes(parentId)) {
        rowSelection.value.selectedRowKeys = [...currentParentKeys, parentId];
    } else if (!allChildrenSelected && currentParentKeys.includes(parentId)) {
        rowSelection.value.selectedRowKeys = currentParentKeys.filter(key => key !== parentId);
    }
};

// Cập nhật hàm xử lý lọc trạng thái
const handleStatusFilterChange = () => {
    console.log('Lọc theo trạng thái:', statusFilter.value);
    // Không cần gọi API, chỉ cần cập nhật biến statusFilter
    // và để computed displayData tự động lọc lại dữ liệu
};

const displayData = ref([]);
const filteredDisplayData = computed(() => {
    if (statusFilter.value !== "") {
        const filterValue = statusFilter.value;
        console.log(`Đang lọc với statusFilter:`, filterValue,
                   `kiểu: ${typeof filterValue}`);

        // Đếm sản phẩm theo từng trạng thái trước khi lọc
        const trueCount = displayData.value.filter(
            item => item.trang_thai === true).length;
        const falseCount = displayData.value.filter(
            item => item.trang_thai === false).length;
        const otherCount = displayData.value.length - trueCount - falseCount;

        console.log(`Trước khi lọc: ${displayData.value.length} sản phẩm`);
        console.log(`- ${trueCount} sản phẩm hoạt động (true)`);
        console.log(`- ${falseCount} sản phẩm không hoạt động (false)`);
        if (otherCount > 0) {
            console.log(`- ${otherCount} sản phẩm có trạng thái khác Boolean`);
        }

        // Lọc với xử lý chặt chẽ hơn
        const result = displayData.value.filter(item => {
            // Chuyển đổi nếu cần
            const itemStatus = typeof item.trang_thai === 'string'
                ? (item.trang_thai === 'Hoạt động' || item.trang_thai === 'true')
                : item.trang_thai;

            // So sánh với filterValue
            return itemStatus === filterValue;
        });

        console.log(`Kết quả lọc: ${result.length} sản phẩm`);
        return result;
    }

    return displayData.value;
});
// Tham chiếu tới menuAction component
const menuActionRef = ref(null);

// Hàm làm mới dữ liệu
const refreshData = async () => {
    isLoading.value = true;
    try {
        // Xóa cache để đảm bảo tải dữ liệu mới nhất
        clearAllProductsCache();
        forceRefresh.value = true;

        // Reset các tham số tìm kiếm và lọc
        store.resetSearchFilterParams();

        // Tải lại dữ liệu
        await store.getAllSP();
        console.log('Tải lại dữ liệu thành công, số lượng:', store.getAllSanPham.length);

        // Format dữ liệu sử dụng hàm formatProductData đã cải tiến
        const formattedData = formatProductData(store.getAllSanPham);

        data.value = formattedData;
        displayData.value = formattedData;

        // Kiểm tra chất lượng dữ liệu sau khi làm mới
        verifyDataQuality();

        message.success('Đã tải lại dữ liệu');
    } catch (error) {
        console.error('Lỗi khi tải lại dữ liệu:', error);
        message.error('Có lỗi xảy ra khi tải lại dữ liệu');
    } finally {
        isLoading.value = false;
    }
};
// Cập nhật dữ liệu hiển thị khi có sự thay đổi trong filteredProductsData
watch(() => store.filteredProductsData, (newData) => {
    console.log('Phát hiện thay đổi filteredProductsData trong store:', newData.length, 'sản phẩm');
    if (newData) {
        displayData.value = newData;
    }
}, { deep: true });
//Hàm tải dữ liệu sản phẩm
const formatCurrency = (value) => {
    if (!value && value !== 0) return '';
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0
    }).format(value);
};

// Thêm biến lọc trạng thái - giá trị rỗng để chịn tất cả, true/false cho hoạt động/không hoạt động
const statusFilter = ref('');

// Thêm biến lọc trạng thái chi tiết sản phẩm - giá trị rỗng để chịn tất cả, true/false cho hoạt động/không hoạt động
const ctspStatusFilter = ref('');

// Thêm biến đếm số lượng biến thể hiển thị
const filteredCTSPCount = computed(() => {
    if (!currentProduct.value) return 0;
    return filteredCTSPData.value.length;
});

// Thêm ref để lưu trữ dữ liệu biến thể đã lọc
const filteredCTSPData = computed(() => {
    if (!currentProduct.value) return [];

    // Lấy danh sách chi tiết sản phẩm từ Map
    const ctspList = productCTSPMap.value.get(currentProduct.value.id_san_pham) || [];

    // In thông tin về trạng thái của các CTSP để kiểm tra
    const activeCount = ctspList.filter(item => item.trang_thai === true).length;
    const inactiveCount = ctspList.filter(item => item.trang_thai === false).length;
    const otherCount = ctspList.length - activeCount - inactiveCount;

    if (ctspList.length > 0 && otherCount > 0) {
        console.warn(`Có ${otherCount} chi tiết sản phẩm có trạng thái không phải Boolean! Cần kiểm tra.`);
        // Kiểm tra và chuyển đổi lại trạng thái
        ctspList.forEach((item, index) => {
            if (typeof item.trang_thai !== 'boolean') {
                console.warn(`CTSP #${index} có trạng thái kiểu ${typeof item.trang_thai}, giá trị: ${item.trang_thai}`);
                // Chuyển đổi trạng thái thành boolean
                if (item.trang_thai === 'Hoạt động' || item.trang_thai === 'true' || item.trang_thai === '1' || item.trang_thai === 1) {
                    item.trang_thai = true;
                } else {
                    item.trang_thai = false;
                }
            }
        });
    }

    // Nếu không có bộ lọc trạng thái (giá trị rỗng), trả về tất cả
    if (ctspStatusFilter.value === "") {
        return ctspList;
    }

    // Lọc theo trạng thái (true hoặc false) - đảm bảo đúng kiểu dữ liệu
    const filterValue = ctspStatusFilter.value;
    console.log(`Đang lọc ${ctspList.length} chi tiết sản phẩm theo trạng thái: ${filterValue ? 'Hoạt động' : 'Không hoạt động'}`);

    // In thông tin về kết quả lọc
    const result = ctspList.filter(item => item.trang_thai === filterValue);
    console.log(`Kết quả lọc: ${result.length}/${ctspList.length} chi tiết`);

    return result;
});

// Hàm xử lý lọc trạng thái chi tiết sản phẩm
const filterCTSPByStatus = () => {
    console.log('Lọc theo trạng thái chi tiết:', ctspStatusFilter.value);
};

// Hàm xử lý lỗi khi tải hình ảnh
const handleImageError = (e) => {
    const imgElement = e.target;
    const originalSrc = imgElement.src;

    console.error('Không thể tải hình ảnh:', originalSrc);

    // Thêm cache-buster và loading state
    const timestamp = Date.now();
    const cacheBustedSrc = `${originalSrc}${originalSrc.includes('?') ? '&' : '?'}_=${timestamp}`;

    // Hiển thị trạng thái loading
    imgElement.src = 'https://placehold.co/150x150?text=Đang+tải...';

    setTimeout(() => {
        const img = new Image();
        img.src = cacheBustedSrc;

        img.onload = () => {
            imgElement.src = cacheBustedSrc;
            console.log('Tải lại thành công với cache-buster:', cacheBustedSrc);
        };

        img.onerror = () => {
            imgElement.src = 'https://placehold.co/150x150/gray/white?text=No+Image';
            console.log('Lỗi tải lại với cache-buster');
        };
    }, 3000);
};

// Hàm thay đổi trạng thái cho nhiều chi tiết sản phẩm một lúc
const bulkChangeStatus = async (newStatus) => {
    try {
        const selectedRowKeys = selectedCTSPKeys.value;
        if (!selectedRowKeys.length) {
            message.warning('Vui lòng chọn ít nhất một biến thể sản phẩm');
            return;
        }

        if (!currentProduct.value) {
            message.error('Không tìm thấy thông tin sản phẩm hiện tại');
            return;
        }

        const productId = currentProduct.value.id_san_pham;

        // Thông báo đang xử lý
        const messageKey = `bulk-status-change-${productId}`;
        message.loading({ content: 'Đang cập nhật trạng thái...', key: messageKey });

        // Lưu trạng thái ban đầu để hoàn tác nếu cần
        const originalCtspList = [...productCTSPMap.value.get(productId) || []];
        const originalData = [...data.value];
        const originalCurrentProduct = { ...currentProduct.value };

        // 1. Cập nhật UI ngay lập tức - Optimistic Update
        const updatedCtspList = originalCtspList.map(item => {
            if (selectedRowKeys.includes(item.id_chi_tiet_san_pham)) {
                return { ...item, trang_thai: newStatus };
            }
            return item;
        });

        // Cập nhật Map để UI hiển thị đúng - Dùng cách set lại toàn bộ Map để trigger reactivity
        const newMap = new Map(productCTSPMap.value);
        newMap.set(productId, updatedCtspList);
        productCTSPMap.value = newMap;

        // 2. Gọi API thông qua store
        const result = await store.bulkUpdateCTSPStatus(selectedRowKeys, newStatus);

        if (result.success) {
            // Hiển thị thông báo thành công
            message.success({
                content: result.message,
                key: messageKey,
                duration: 2
            });

            // Xóa các hàng đã chọn sau khi thay đổi trạng thái
            selectedCTSPKeys.value = [];

            // Cập nhật cache chi tiết sản phẩm
            // const cacheKey = `${CTSP_CACHE_PREFIX}${productId}`;
            // saveToCache(cacheKey, updatedCtspList);

            // Nếu có sản phẩm cha đã được cập nhật
            if (result.parentStatusUpdated && result.updatedParents && result.updatedParents.length > 0) {
                console.log(`Cập nhật UI cho ${result.updatedParents.length} sản phẩm cha`);

                // Cập nhật danh sách sản phẩm với trạng thái mới của các sản phẩm cha
                displayData.value = displayData.value.map(item => {
                    // Tìm sản phẩm cha tương ứng trong danh sách đã cập nhật
                    const updatedParent = result.updatedParents.find(p => p.id === item.id_san_pham);
                    if (updatedParent) {
                        console.log(`Cập nhật UI sản phẩm ${item.id_san_pham} thành ${updatedParent.status}`);
                        return { ...item, trang_thai: updatedParent.status };
                    }
                    return item;
                });

                // Cập nhật sản phẩm trong drawer nếu đang hiển thị
                if (currentProduct.value) {
                    const updatedCurrentParent = result.updatedParents.find(p => p.id === currentProduct.value.id_san_pham);
                    if (updatedCurrentParent) {
                        currentProduct.value = {
                            ...currentProduct.value,
                            trang_thai: updatedCurrentParent.status
                        };
                    }
                }
            }
        } else {
            // Hiển thị thông báo lỗi
            message.error({
                content: result.message || 'Không thể cập nhật trạng thái',
                key: messageKey,
                duration: 2
            });

            // Hoàn tác UI nếu có lỗi
            const rollbackMap = new Map(productCTSPMap.value);
            rollbackMap.set(productId, originalCtspList);
            productCTSPMap.value = rollbackMap;

            data.value = originalData;
            if (currentProduct.value) {
                currentProduct.value = originalCurrentProduct;
            }
        }
    } catch (error) {
        console.error('Lỗi khi thay đổi trạng thái hàng loạt:', error);
        message.error('Có lỗi xảy ra khi thay đổi trạng thái hàng loạt');
    }
};

// Handler for refresh-data event from menuAction component
const handleMenuActionRefresh = async () => {
    console.log('Received refresh-data event from menuAction');

    // Force refresh all data and clear cache
    forceRefresh.value = true;
    clearAllProductsCache();
    isLoading.value = true;

    try {
        // Make sure to load the latest data by date
        console.log('Tải dữ liệu sản phẩm mới nhất sau khi import Excel...');
        const newData = await store.getAllSanPhamNgaySua();
        console.log('Số lượng sản phẩm sau khi refresh:', newData?.length || 0);
        displayData.value = formatProductData(newData);

        message.success('Dữ liệu đã được cập nhật sau khi import Excel');
    } catch (error) {
        console.error('Lỗi khi làm mới dữ liệu sau import Excel:', error);
        message.error('Có lỗi xảy ra khi cập nhật dữ liệu');
    } finally {
        isLoading.value = false;
        forceRefresh.value = false;
    }
};

// Handle table change events (sorting, pagination, etc.)
const handleTableChange = (pagination, filters, sorter) => {
    console.log('Table params changed:', { pagination, filters, sorter });

    // Process sorting if sorter is provided
    if (sorter) {
        console.log(`Sorting by ${sorter.field}, order: ${sorter.order}`);
    }
};

// Function to handle sorting from menu dropdown
const handleExternalSort = (event) => {
    const sortOption = event.detail.option;
    console.log('Received external sort event with option:', sortOption);

    // Apply the sorting based on the option
    let sortedData = [...displayData.value]; // Create a copy to avoid reactivity issues

    switch (sortOption) {
        case '2': // Mã sản phẩm ascending
            sortedData.sort((a, b) => a.ma_san_pham.localeCompare(b.ma_san_pham));
            break;
        case '3': // Mã sản phẩm descending
            sortedData.sort((a, b) => b.ma_san_pham.localeCompare(a.ma_san_pham));
            break;
        case '4': // Name ascending
            sortedData.sort((a, b) => a.ten_san_pham.localeCompare(b.ten_san_pham));
            break;
        case '5': // Name descending
            sortedData.sort((a, b) => b.ten_san_pham.localeCompare(a.ten_san_pham));
            break;
        case '6': // Quantity ascending
            sortedData.sort((a, b) => a.tong_so_luong - b.tong_so_luong);
            break;
        case '7': // Quantity descending
            sortedData.sort((a, b) => b.tong_so_luong - a.tong_so_luong);
            break;
        default:
            // No sorting
            break;
    }

    // Update data with sorted array
    if (sortOption !== '1') {
        data.value = sortedData;

        // Force re-render
        isLoading.value = true;
        setTimeout(() => {
            isLoading.value = false;
        }, 100);
    }
};
// Hàm kiểm tra và format dữ liệu sản phẩm
const formatProductData = (products) => {
    if (!products || !Array.isArray(products)) {
        console.warn('Dữ liệu sản phẩm không hợp lệ:', products);
        return [];
    }

    console.log('Format dữ liệu cho', products.length, 'sản phẩm');
    // Log mẫu một sản phẩm để debug
    if (products.length > 0) {
        console.log('Mẫu sản phẩm đầu tiên:', JSON.stringify(products[0], null, 2));
    }
    if (!products || products.length === 0) {
        console.log('Không có dữ liệu sản phẩm để format');
        return [];
    }

    console.log('Dữ liệu gốc từ API (3 sản phẩm đầu):', products.slice(0, 3));

    return products.map((item, index) => {
        // Xác định trạng thái Boolean một cách nhất quán
        let trangThai;
        if (typeof item.trang_thai === 'boolean') {
            // Nếu đã là boolean, giữ nguyên
            trangThai = item.trang_thai;
        } else if (item.trang_thai === true || item.trang_thai === 'true' ||
                  item.trang_thai === 1 || item.trang_thai === '1' ||
                  item.trang_thai === 'Hoạt động') {
            trangThai = true;
        } else {
            // Tất cả các trường hợp còn lại đều xem là false
            trangThai = false;
        }

        if (index < 3) {
            console.log(`SP #${index + 1}: ${item.ten_san_pham}, trạng thái gốc:`,
                       item.trang_thai, 'đã chuyển thành:', trangThai);
        }

        return {
            stt: index + 1,
            key: item.id_san_pham,
            id_san_pham: item.id_san_pham,
            ma_san_pham: item.ma_san_pham || '',
            ten_san_pham: item.ten_san_pham || '',
            hinh_anh: item.hinh_anh || '',
            chi_muc: (item.ten_danh_muc || '') + "/" + (item.ten_thuong_hieu || '') + "/" + (item.ten_chat_lieu || ''),
            trang_thai: trangThai, // Luôn là Boolean
            tong_so_luong: item.tong_so_luong || 0,
            ngay_cap_nhat: item.ngay_cap_nhat || ''
        };
    });
};
onMounted(async () => {
//     console.log('Component mounted - Bắt đầu khởi tạo...');
//     try {
//         // Đảm bảo store được khởi tạo
//         if (!store.getAllSanPham || store.getAllSanPham.length === 0) {
//             console.log('Chưa có dữ liệu trong store, tải mới...');
//             await store.getAllSP();
//         }
//     const startTime = performance.now();
//     isLoading.value = true;
// } catch (error) {
//     console.error('Lỗi khi tải dữ liệu:', error);
//     message.error('Có lỗi xảy ra khi tải dữ liệu');
// } finally {
//     isLoading.value = false;
// }
    try {
        const startTime = performance.now();
        // Đăng ký lắng nghe sự kiện
        window.addEventListener('search-filter-changed', handleSearchFilterChanged);
        window.addEventListener('sort-option-changed', handleExternalSort);

        console.log('Đã đăng ký lắng nghe sự kiện search-filter-changed');
        console.log('Bắt đầu tải dữ liệu sản phẩm...');

        // Xóa cache để đảm bảo tải dữ liệu mới nhất từ API khi tải lại trang
        clearAllProductsCache();
        forceRefresh.value = true;

        // Kiểm tra xem đã có dữ liệu lọc chưa
        if (store.filteredProductsData && store.filteredProductsData.length > 0) {
            console.log("Có dữ liệu lọc trong store, số lượng:", store.filteredProductsData.length);
            console.log("Trạng thái justAddedProduct:", store.justAddedProduct);

            if(!store.justAddedProduct){
                displayData.value = formatProductData(store.filteredProductsData);
                console.log('Khởi tạo displayData với filteredProductsData:', displayData.value.length, 'sản phẩm');
            }else{
                console.log('Tải dữ liệu mới nhất theo ngày sửa...');
                await store.getAllSanPhamNgaySua();
                displayData.value = formatProductData(store.getAllSanPham);
                console.log('Theo ngày sửa - Tổng số:', store.getAllSanPham.length, 'sản phẩm');
            }
        } else {
            console.log('Không có dữ liệu lọc trong store, tải dữ liệu mới...');
            // Logic tải dữ liệu từ API
            if (store.justAddedProduct) {
                console.log('justAddedProduct=true - Tải sản phẩm theo ngày sửa...');
                await store.getAllSanPhamNgaySua();
                displayData.value = formatProductData(store.getAllSanPham);
                store.justAddedProduct = false;
                console.log('Tải xong, số lượng:', store.getAllSanPham.length);
            } else {
                console.log('Tải tất cả sản phẩm...');
                await store.getAllSP();
                console.log('Tải xong, số lượng:', store.getAllSanPham.length);
            }

            // Format dữ liệu và cập nhật display
            data.value = formatProductData(store.getAllSanPham);
            displayData.value = formatProductData(store.getAllSanPham);
        }

        // Kiểm tra sản phẩm theo trạng thái sau khi tải xong
        const activeCount = displayData.value.filter(item => item.trang_thai === true).length;
        const inactiveCount = displayData.value.filter(item => item.trang_thai === false).length;
        console.log(`Phân tích dữ liệu sau khi tải:`);
        console.log(`- Tổng số sản phẩm: ${displayData.value.length}`);
        console.log(`- Số sản phẩm hoạt động: ${activeCount}`);
        console.log(`- Số sản phẩm không hoạt động: ${inactiveCount}`);

        // Kiểm tra chất lượng dữ liệu chi tiết
        verifyDataQuality();

        loadTime.value = Math.round(performance.now() - startTime);
    } catch (error) {
        console.error('Lỗi khi tải dữ liệu:', error);
        message.error('Có lỗi xảy ra khi tải dữ liệu sản phẩm');
    } finally {
        isLoading.value = false;
    }
});

onUnmounted(() => {
    // Hủy đăng ký sự kiện
    window.removeEventListener('search-filter-changed', handleSearchFilterChanged);
    window.removeEventListener('sort-option-changed', handleExternalSort);
    console.log('Đã hủy đăng ký sự kiện');
});
// Add the event listener to the existing onMounted function
</script>
<style scoped>
:deep(.ctsp-table) {

    /* Giảm padding của header */
    .ant-table-thead>tr>th {
        padding: 8px;
        font-size: 13px;
    }

    /* Giảm padding của cells */
    .ant-table-tbody>tr>td {
        padding: 6px 8px;
        font-size: 12px;
    }

    /* Style cho header */
    .ant-table-thead>tr>th {
        background: #f5f5f5;
        font-weight: 600;
    }

    /* Thêm border và màu nền nhẹ */
    .ant-table {
        border: 1px solid #f0f0f0;
        background: #fafafa;
    }
}

.custom-class {
    z-index: 1000;
}

/* Style cho bộ lọc trạng thái */
.status-filter {
    margin-bottom: 10px;
}

.status-filter .ant-radio-button-wrapper {
    margin-right: 5px;
}

/* Style cho nút lọc trạng thái với màu cam đậm #ff6600 */
:deep(.custom-radio-group) {
    .ant-radio-button-wrapper {
        color: white;
        background-color: #ff6600;
        border-color: #ff6600;
        transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);

        &:first-child {
            border-left-color: #ff6600;
        }

        &:hover {
            color: white;
            background-color: #e55a00;
            border-color: #e55a00;
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
        }

        &.ant-radio-button-wrapper-checked {
            background-color: #e55a00;
            border-color: #e55a00;
            box-shadow: 0 4px 8px rgba(229, 90, 0, 0.3);

            &:before {
                background-color: #e55a00;
            }
        }

        &:focus-within {
            box-shadow: 0 0 0 3px rgba(255, 102, 0, 0.3);
        }
    }
}

/* Style cho bộ lọc trạng thái trong drawer */
.variant-status-filter {
    margin-bottom: 15px;
}

:deep(.variant-status-filter .custom-radio-group) {
    .ant-radio-button-wrapper {
        color: white;
        background-color: #ff6600;
        border-color: #ff6600;
        transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);

        &:first-child {
            border-left-color: #ff6600;
        }

        &:hover {
            color: white;
            background-color: #e55a00;
            border-color: #e55a00;
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
        }

        &.ant-radio-button-wrapper-checked {
            background-color: #e55a00;
            border-color: #e55a00;
            box-shadow: 0 4px 8px rgba(229, 90, 0, 0.3);

            &:before {
                background-color: #e55a00;
            }
        }

        &:focus-within {
            box-shadow: 0 0 0 3px rgba(255, 102, 0, 0.3);
        }
    }
}

/* Style cho các nút bulk action */
.bulk-actions {
    margin-top: 10px;
}

.bulk-action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 5px;
    padding: 5px 15px;
    border-radius: 6px;
    font-weight: 500;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    position: relative;
}

.bulk-action-btn:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.bulk-action-btn:active {
    transform: translateY(-1px);
    box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
}

.bulk-action-btn::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.1);
    transform: translateX(-100%);
    transition: transform 0.5s ease;
}

.bulk-action-btn:hover::before {
    transform: translateX(0);
}

:deep(.bulk-action-btn .anticon) {
    vertical-align: middle;
    margin-right: 5px;
    font-size: 16px;
    transition: transform 0.3s ease;
}

.bulk-action-btn:hover :deep(.anticon) {
    transform: scale(1.2);
}

/* Style nút primary */
:deep(.ant-btn-primary) {
    background: #f33b47;
    border-color: #f33b47;
    box-shadow: 0 2px 5px rgba(243, 59, 71, 0.3);
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

:deep(.ant-btn-primary:hover),
:deep(.ant-btn-primary:focus) {
    background: #ff4d58;
    border-color: #ff4d58;
    box-shadow: 0 4px 10px rgba(243, 59, 71, 0.4);
    transform: translateY(-2px);
}

/* Style nút danger */
:deep(.ant-btn-dangerous) {
    background: #ff4d4f;
    border-color: #ff4d4f;
    color: white;
    box-shadow: 0 2px 5px rgba(255, 77, 79, 0.3);
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

:deep(.ant-btn-dangerous:hover),
:deep(.ant-btn-dangerous:focus) {
    background: #ff7875;
    border-color: #ff7875;
    color: white;
    box-shadow: 0 4px 10px rgba(255, 77, 79, 0.4);
    transform: translateY(-2px);
}

/* ===== WHITE & DEEP ORANGE BUTTON STYLING ===== */

/* Primary buttons - Deep Orange theme */
:deep(.ant-btn-primary) {
    background: #ff6600 !important; /* Deep orange */
    border-color: #ff6600 !important;
    color: white !important;
    box-shadow: 0 2px 8px rgba(255, 102, 0, 0.3) !important;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    border-radius: 8px !important;
    font-weight: 500;
}

:deep(.ant-btn-primary:hover),
:deep(.ant-btn-primary:focus) {
    background: #e55a00 !important; /* Darker orange */
    border-color: #e55a00 !important;
    box-shadow: 0 4px 15px rgba(255, 102, 0, 0.4) !important;
    transform: translateY(-2px);
}

/* Edit buttons (Warning style) - Deep Orange theme */
:deep(.btn-warning) {
    background: white !important;
    border-color: #ff6600 !important;
    color: #ff6600 !important;
    box-shadow: 0 2px 8px rgba(255, 102, 0, 0.2) !important;
    border-radius: 8px !important;
    font-weight: 500;
}

:deep(.btn-warning:hover) {
    background: rgba(255, 102, 0, 0.1) !important;
    border-color: #ff6600 !important;
    color: #e55a00 !important;
    box-shadow: 0 4px 15px rgba(255, 102, 0, 0.3) !important;
    transform: translateY(-2px);
}

/* Style nút biến thể */
:deep(.d-flex .ant-btn) {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 15px;
    border-radius: 8px;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    border-color: #ff6600 !important;
}

:deep(.d-flex .ant-btn:hover) {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
    border-color: #e55a00 !important;
}

:deep(.d-flex .ant-btn .anticon) {
    transition: transform 0.3s ease;
}

:deep(.d-flex .ant-btn:hover .anticon) {
    transform: scale(1.1);
}

/* Bulk action buttons - Enhanced styling */
.bulk-action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 8px 18px !important;
    border-radius: 8px !important;
    font-weight: 600 !important;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15) !important;
    min-height: 40px !important;
}

.bulk-action-btn:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2) !important;
}

.bulk-action-btn:active {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
}

/* Danger buttons - Deep orange danger style */
:deep(.ant-btn-dangerous) {
    background: #ff6600 !important;
    border-color: #ff6600 !important;
    color: white !important;
    box-shadow: 0 2px 8px rgba(255, 102, 0, 0.3) !important;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    border-radius: 8px !important;
    font-weight: 500;
}

:deep(.ant-btn-dangerous:hover),
:deep(.ant-btn-dangerous:focus) {
    background: #e55a00 !important;
    border-color: #e55a00 !important;
    color: white !important;
    box-shadow: 0 4px 15px rgba(255, 102, 0, 0.4) !important;
    transform: translateY(-2px);
}

/* Style cho nút làm mới */
.refresh-button {
    width: 45px;
    height: 45px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    background-color: #f33b47;
    border-color: #f33b47;
    box-shadow: 0 3px 6px rgba(0, 0, 0, 0.16);
    position: relative;
    overflow: hidden;
}

.refresh-button::after {
    content: "";
    position: absolute;
    top: 50%;
    left: 50%;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0) 70%);
    transform: scale(0);
    opacity: 0;
    transition: transform 0.5s, opacity 0.3s;
}

.refresh-button:hover {
    transform: rotate(180deg) scale(1.1);
    background-color: #ff4d58;
    border-color: #ff4d58;
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.2);
}

.refresh-button:hover::after {
    transform: scale(2);
    opacity: 1;
}

.refresh-button:focus {
    background-color: #d62a38;
    border-color: #d62a38;
}

/* Style cho switch */
:deep(.ant-switch) {
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
}

:deep(.ant-switch:hover) {
    box-shadow: 0 3px 6px rgba(0, 0, 0, 0.16);
}

:deep(.ant-switch-handle::before) {
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

:deep(.ant-switch-checked:hover .ant-switch-handle::before) {
    transform: scale(1.1);
}

/* Style cho checkbox */
:deep(.ant-checkbox) {
    .ant-checkbox-inner {
        border-color: #f33b47;
        border-radius: 4px;
        transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    }

    &:hover .ant-checkbox-inner {
        border-color: #ff6b76;
        box-shadow: 0 0 0 2px rgba(243, 59, 71, 0.2);
    }

    &.ant-checkbox-checked .ant-checkbox-inner {
        background-color: #f33b47;
        border-color: #f33b47;
    }

    &.ant-checkbox-checked:hover .ant-checkbox-inner {
        background-color: #ff6b76;
        border-color: #ff6b76;
    }

    &.ant-checkbox-indeterminate .ant-checkbox-inner::after {
        background-color: #f33b47;
    }
}

/* Style cho checkbox trong bảng */
:deep(.ant-table-selection) {

    .ant-checkbox-wrapper:hover .ant-checkbox-inner,
    .ant-checkbox:hover .ant-checkbox-inner {
        border-color: #ff6b76;
    }

    .ant-checkbox-checked::after {
        border-color: #f33b47;
    }

    .ant-checkbox-checked .ant-checkbox-inner {
        background-color: #f33b47;
        border-color: #f33b47;
        animation: checkboxEffect 0.36s ease-in-out;
    }

    .ant-checkbox-checked:hover .ant-checkbox-inner {
        background-color: #ff6b76;
        border-color: #ff6b76;
    }
}

/* Style cho hàng được chọn trong bảng */
:deep(.ant-table-row-selected > td) {
    background-color: rgba(243, 59, 71, 0.1) !important;
}

/* Keyframes for checkbox animation */
@keyframes checkboxEffect {
    0% {
        transform: scale(1);
        opacity: 0.5;
    }

    50% {
        transform: scale(1.2);
        opacity: 0.7;
    }

    100% {
        transform: scale(1);
        opacity: 1;
    }
}

/* ===== RESPONSIVE DESIGN FOR WHITE & ORANGE THEME ===== */

/* Mobile and Tablet Styles */
@media (max-width: 992px) {
    .action-buttons {
        flex-direction: column !important;
        gap: 4px !important;
        width: 100% !important;
    }

    .action-buttons .ant-btn {
        width: 100% !important;
        justify-content: center !important;
        padding: 8px 12px !important;
        font-size: 13px !important;
    }

    .action-buttons .btn-text {
        display: inline !important; /* Show text on mobile */
    }

    /* Reduce table column sizes on mobile */
    .ant-table {
        font-size: 12px !important;
    }

    .ant-table-thead > tr > th,
    .ant-table-tbody > tr > td {
        padding: 8px 4px !important;
    }
}

@media (max-width: 768px) {
    .view-btn .btn-text,
    .edit-btn .btn-text {
        display: none !important; /* Hide text on small screens to save space */
    }

    .action-buttons .ant-btn {
        padding: 6px 8px !important;
        min-width: 35px !important;
    }

    /* Mobile table scroll */
    .ant-table-content {
        overflow-x: auto !important;
    }

    /* Stack bulk action buttons vertically on mobile */
    .bulk-actions {
        flex-direction: column !important;
    }

    .bulk-actions .bulk-action-btn {
        width: 100% !important;
        margin-bottom: 8px !important;
    }
}

/* Desktop specific enhancements */
@media (min-width: 993px) {
    .action-buttons .ant-btn {
        min-width: 70px !important; /* Ensure minimum width on desktop */
    }

    .btn-text {
        display: inline !important; /* Show text on desktop */
    }
}

/* Ensure table text remains black as requested */
:deep(.ant-table-cell) {
    color: #000000 !important; /* Force black text in table cells */
}

:deep(.ant-table-thead .ant-table-cell) {
    color: #000000 !important; /* Headers also black */
}

:deep(.ant-table-row .ant-table-cell) {
    color: #000000 !important; /* Data rows black */
}

/* Keep data text black but allow button text to be colored */
.action-buttons .ant-btn {
    color: inherit !important; /* Preserve button text colors */
}

/* Enhanced table appearance */
:deep(.ant-table) {
    background: white !important;
    border-radius: 8px !important;
    overflow: hidden !important;
    box-shadow: 0 2px 10px rgba(255, 102, 0, 0.1) !important;
}

:deep(.ant-table-thead > tr > th) {
    background: #f8f9fa !important;
    border-bottom: 2px solid #ff6600 !important;
    font-weight: 600 !important;
    color: #000000 !important;
}

/* Hover effects for table rows */
:deep(.ant-table-row:hover > td) {
    background-color: rgba(255, 102, 0, 0.03) !important;
}

/* Pagination styling - Orange theme */
:deep(.ant-pagination) {
    .ant-pagination-item {
        border-color: #ff6600 !important;
        border-radius: 6px !important;
        transition: all 0.3s ease !important;

        a {
            color: #ff6600 !important;
        }

        &:hover {
            border-color: #e55a00 !important;
        }

        &:hover a {
            color: #e55a00 !important;
        }
    }

    .ant-pagination-item-active {
        background: #ff6600 !important;
        border-color: #ff6600 !important;

        a {
            color: white !important;
        }
    }

    .ant-btn {
        border-color: #ff6600 !important;
        border-radius: 6px !important;
        transition: all 0.3s ease !important;

        &:hover {
            border-color: #e55a00 !important;
            color: #e55a00 !important;
        }
    }

    .ant-pagination-options .ant-select-selector {
        border-color: #ff6600 !important;
        border-radius: 6px !important;

        &:hover {
            border-color: #e55a00 !important;
        }
    }

    .ant-pagination-options .ant-select-arrow {
        color: #ff6600 !important;
    }
}
</style>