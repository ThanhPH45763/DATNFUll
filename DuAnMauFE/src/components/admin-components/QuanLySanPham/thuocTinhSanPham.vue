<template>
    <div class="product-attributes">
        <a-card>
            <h2 class="page-title">Quản Lý Thuộc Tính Sản Phẩm</h2>

            <a-tabs v-model:activeKey="currentTab">
                <!-- Tab Danh Mục -->
                <a-tab-pane key="category" tab="Danh Mục">
                    <div class="content-area">
                        <div class="add-form">
                            <a-input v-model:value="newAttribute.name" placeholder="Thêm danh mục mới"
                                class="input-field" :status="validationStatus.category" @blur="validateNewAttribute" @keyup.enter="addNewAttribute">
                                <template #suffix>
                                    <a-button type="primary" @click="addNewAttribute" :disabled="!isValidNewAttr">
                                        <template #icon>
                                            <plus-outlined />
                                        </template>
                                        Thêm mới
                                    </a-button>
                                </template>
                            </a-input>
                            <div v-if="validationError.category" class="validation-error">
                                {{ validationError.category }}
                            </div>
                        </div>

                        <div class="filter-section">
                            <a-select v-model:value="statusFilters.category" style="width: 200px; margin-bottom: 16px"
                                @change="onFilterChange('category')">
                                <a-select-option value="all">Tất cả trạng thái</a-select-option>
                                <a-select-option value="active">Hoạt động</a-select-option>
                                <a-select-option value="inactive">Không hoạt động</a-select-option>
                            </a-select>
                        </div>

                        <a-table :dataSource="getFilteredData" :columns="categoryColumns" :pagination="{
                            total: getTotalFilteredItems,
                            pageSize: 5,
                            current: currentPage,
                            onChange: onPageChange,
                            showSizeChanger: false
                        }">
                            <template #bodyCell="{ column, record, index }">
                                <template v-if="column.key === 'action'">
                                    <div class="attribute-actions">
                                        <a-button type="primary" @click="showEditModal(record, 'category')">
                                            <template #icon><edit-outlined /></template>
                                        </a-button>
                                    </div>
                                </template>
                                <template v-else-if="column.key === 'status'">
                                    <a-switch v-model:checked="record.status"
                                        :style="{ backgroundColor: record.status ? '#ff6b35' : '#ccc' }"
                                        @change="(checked) => handleStatusChange(index, checked)" />
                                </template>
                            </template>
                        </a-table>
                    </div>
                </a-tab-pane>

                <!-- Tab Thương Hiệu -->
                <a-tab-pane key="brand" tab="Thương Hiệu">
                    <div class="content-area">
                        <div class="add-form">
                            <a-input v-model:value="newAttribute.name" placeholder="Thêm thương hiệu mới"
                                class="input-field" :status="validationStatus.brand" @blur="validateNewAttribute" @keyup.enter="addNewAttribute">
                                <template #suffix>
                                    <a-button type="primary" @click="addNewAttribute" :disabled="!isValidNewAttr">
                                        <template #icon>
                                            <plus-outlined />
                                        </template>
                                        Thêm mới
                                    </a-button>
                                </template>
                            </a-input>
                            <div v-if="validationError.brand" class="validation-error">
                                {{ validationError.brand }}
                            </div>
                        </div>

                        <div class="filter-section">
                            <a-select v-model:value="statusFilters.brand" style="width: 200px; margin-bottom: 16px"
                                @change="onFilterChange('brand')">
                                <a-select-option value="all">Tất cả trạng thái</a-select-option>
                                <a-select-option value="active">Hoạt động</a-select-option>
                                <a-select-option value="inactive">Không hoạt động</a-select-option>
                            </a-select>
                        </div>

                        <a-table :dataSource="getFilteredData" :columns="brandColumns" :pagination="{
                            total: getTotalFilteredItems,
                            pageSize: 5,
                            current: currentPage,
                            onChange: onPageChange,
                            showSizeChanger: false
                        }">
                            <template #bodyCell="{ column, record, index }">
                                <template v-if="column.key === 'action'">
                                    <div class="attribute-actions">
                                        <a-button type="primary" @click="showEditModal(record, 'brand')">
                                            <template #icon><edit-outlined /></template>
                                        </a-button>
                                    </div>
                                </template>
                                <template v-else-if="column.key === 'status'">
                                    <a-switch v-model:checked="record.status"
                                        :style="{ backgroundColor: record.status ? '#ff6b35' : '#ccc' }"
                                        @change="(checked) => handleStatusChange(index, checked)" />
                                </template>
                            </template>
                        </a-table>
                    </div>
                </a-tab-pane>

                <!-- Tab Chất Liệu -->
                <a-tab-pane key="material" tab="Chất Liệu">
                    <div class="content-area">
                        <div class="add-form">
                            <a-input v-model:value="newAttribute.name" placeholder="Thêm chất liệu mới"
                                class="input-field" :status="validationStatus.material" @blur="validateNewAttribute" @keyup.enter="addNewAttribute">
                                <template #suffix>
                                    <a-button type="primary" @click="addNewAttribute" :disabled="!isValidNewAttr">
                                        <template #icon>
                                            <plus-outlined />
                                        </template>
                                        Thêm mới
                                    </a-button>
                                </template>
                            </a-input>
                            <div v-if="validationError.material" class="validation-error">
                                {{ validationError.material }}
                            </div>
                        </div>

                        <div class="filter-section">
                            <a-select v-model:value="statusFilters.material" style="width: 200px; margin-bottom: 16px"
                                @change="onFilterChange('material')">
                                <a-select-option value="all">Tất cả trạng thái</a-select-option>
                                <a-select-option value="active">Hoạt động</a-select-option>
                                <a-select-option value="inactive">Không hoạt động</a-select-option>
                            </a-select>
                        </div>

                        <a-table :dataSource="getFilteredData" :columns="materialColumns" :pagination="{
                            total: getTotalFilteredItems,
                            pageSize: 5,
                            current: currentPage,
                            onChange: onPageChange,
                            showSizeChanger: false
                        }">
                            <template #bodyCell="{ column, record, index }">
                                <template v-if="column.key === 'action'">
                                    <div class="attribute-actions">
                                        <a-button type="primary" @click="showEditModal(record, 'material')">
                                            <template #icon><edit-outlined /></template>
                                        </a-button>
                                    </div>
                                </template>
                                <template v-else-if="column.key === 'status'">
                                    <a-switch v-model:checked="record.status"
                                        :style="{ backgroundColor: record.status ? '#ff6b35' : '#ccc' }"
                                        @change="(checked) => handleStatusChange(index, checked)" />
                                </template>
                            </template>
                        </a-table>
                    </div>
                </a-tab-pane>

                <!-- Tab Màu Sắc -->
                <a-tab-pane key="color" tab="Màu Sắc">
                    <div class="content-area">
                        <div class="add-form">
                            <a-input v-model:value="newAttribute.name" placeholder="Thêm màu sắc mới"
                                class="input-field" :status="validationStatus.color" @blur="validateNewAttribute" @keyup.enter="addNewAttribute">
                                <template #suffix>
                                    <a-button type="primary" @click="addNewAttribute" :disabled="!isValidNewAttr">
                                        <template #icon>
                                            <plus-outlined />
                                        </template>
                                        Thêm mới
                                    </a-button>
                                </template>
                            </a-input>
                            <div v-if="validationError.color" class="validation-error">
                                {{ validationError.color }}
                            </div>
                        </div>

                        <div class="filter-section">
                            <a-select v-model:value="statusFilters.color" style="width: 200px; margin-bottom: 16px"
                                @change="onFilterChange('color')">
                                <a-select-option value="all">Tất cả trạng thái</a-select-option>
                                <a-select-option value="active">Hoạt động</a-select-option>
                                <a-select-option value="inactive">Không hoạt động</a-select-option>
                            </a-select>
                        </div>

                        <a-table :dataSource="getFilteredData" :columns="colorColumns" :pagination="{
                            total: getTotalFilteredItems,
                            pageSize: 5,
                            current: currentPage,
                            onChange: onPageChange,
                            showSizeChanger: false
                        }">
                            <template #bodyCell="{ column, record, index }">
                                <template v-if="column.key === 'action'">
                                    <div class="attribute-actions">
                                        <a-button type="primary" @click="showEditModal(record, 'color')">
                                            <template #icon><edit-outlined /></template>
                                        </a-button>
                                    </div>
                                </template>
                                <template v-else-if="column.key === 'status'">
                                    <a-switch v-model:checked="record.status"
                                        :style="{ backgroundColor: record.status ? '#ff6b35' : '#ccc' }"
                                        @change="(checked) => handleStatusChange(index, checked)" />
                                </template>
                            </template>
                        </a-table>
                    </div>
                </a-tab-pane>

                <!-- Tab Kích Thước -->
                <a-tab-pane key="size" tab="Kích Thước">
                    <div class="content-area">
                        <div class="add-form">
                            <div class="d-flex gap-2">
                                <a-input v-model:value="newAttribute.value" placeholder="Nhập giá trị"
                                    class="input-field" :status="validationStatus.sizeValue"
                                    @blur="validateNewAttribute" style="width: 60%" @keyup.enter="addNewAttribute">
                                </a-input>
                                <a-input v-model:value="newAttribute.unit" placeholder="Nhập đơn vị (tùy chọn)"
                                    class="input-field" style="width: 30%" @keyup.enter="addNewAttribute">
                                </a-input>
                                <a-button type="primary" @click="addNewAttribute" :disabled="!isValidNewAttr"
                                    style="width: 10%">
                                    <template #icon>
                                        <plus-outlined />
                                    </template>
                                </a-button>
                            </div>
                            <div v-if="validationError.sizeValue" class="validation-error">
                                {{ validationError.sizeValue }}
                            </div>
                        </div>

                        <div class="filter-section">
                            <a-select v-model:value="statusFilters.size" style="width: 200px; margin-bottom: 16px"
                                @change="onFilterChange('size')">
                                <a-select-option value="all">Tất cả trạng thái</a-select-option>
                                <a-select-option value="active">Hoạt động</a-select-option>
                                <a-select-option value="inactive">Không hoạt động</a-select-option>
                            </a-select>
                        </div>

                        <a-table :dataSource="getFilteredData" :columns="sizeColumns" :pagination="{
                            total: getTotalFilteredItems,
                            pageSize: 5,
                            current: currentPage,
                            onChange: onPageChange,
                            showSizeChanger: false
                        }">
                            <template #bodyCell="{ column, record, index }">
                                <template v-if="column.key === 'action'">
                                    <div class="attribute-actions">
                                        <a-button type="primary" @click="showEditModal(record, 'size')">
                                            <template #icon><edit-outlined /></template>
                                        </a-button>
                                    </div>
                                </template>
                                <template v-else-if="column.key === 'status'">
                                    <a-switch v-model:checked="record.status"
                                        :style="{ backgroundColor: record.status ? '#ff6b35' : '#ccc' }"
                                        @change="(checked) => handleStatusChange(index, checked)" />
                                </template>
                            </template>
                        </a-table>
                    </div>
                </a-tab-pane>
            </a-tabs>
        </a-card>

        <!-- Add modal for editing -->
        <a-modal v-model:visible="editModalVisible" :title="editModalTitle" @ok="saveEditModal">
            <template v-if="currentTab === 'size'">
                <a-form layout="vertical">
                    <a-form-item label="Giá trị" :validateStatus="editValidationStatus.value"
                        :help="editValidationError.value">
                        <a-input v-model:value="editingData.value" @blur="validateEditingData" />
                    </a-form-item>
                    <a-form-item label="Đơn vị (tùy chọn)">
                        <a-input v-model:value="editingData.unit" />
                    </a-form-item>
                </a-form>
            </template>
            <template v-else>
                <a-form layout="vertical">
                    <a-form-item :label="getNameLabel()" :validateStatus="editValidationStatus.name"
                        :help="editValidationError.name">
                        <a-input v-model:value="editingData.name" @blur="validateEditingData" />
                    </a-form-item>
                </a-form>
            </template>
        </a-modal>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { PlusOutlined, EditOutlined, CheckOutlined } from '@ant-design/icons-vue'
import { useGbStore } from '../../../stores/gbStore'
import { storeToRefs } from 'pinia'
import { Modal, message } from 'ant-design-vue'
import dayjs from 'dayjs'

const store = useGbStore()
const currentTab = ref('category')
const isEditing = ref(false)
const editingItem = ref(null)
const currentPage = ref(1)

// Thêm biến để quản lý modal sửa
const editModalVisible = ref(false)
const editModalTitle = ref('Sửa thuộc tính')
const editingData = ref({
    name: '',
    value: '',
    unit: '',
    id: null,
    type: ''
})

// Thêm biến để quản lý validate
const validationStatus = ref({
    category: '',
    brand: '',
    material: '',
    color: '',
    sizeValue: ''
})

const validationError = ref({
    category: '',
    brand: '',
    material: '',
    color: '',
    sizeValue: ''
})

const editValidationStatus = ref({
    name: '',
    value: ''
})

const editValidationError = ref({
    name: '',
    value: ''
})

const isValidNewAttr = ref(true)

// Thêm biến statusFilters và phương thức liên quan
const statusFilters = ref({
    category: 'all',
    brand: 'all',
    material: 'all',
    color: 'all',
    size: 'all'
})

// Cấu hình cột cho từng bảng
const categoryColumns = [
    {
        title: 'Mã danh mục',
        dataIndex: 'ma_danh_muc',
        key: 'ma_danh_muc',
        width: '15%'
    },
    {
        title: 'Tên danh mục',
        dataIndex: 'ten_danh_muc',
        key: 'ten_danh_muc',
        width: '25%'
    },
    {
        title: 'Trạng thái',
        dataIndex: 'trang_thai',
        key: 'status',
        width: '15%'
    },
    {
        title: 'Ngày tạo',
        dataIndex: 'ngay_tao',
        key: 'ngay_tao',
        width: '15%',
        customRender: ({ text }) => dayjs(text).format('HH:mm:ss DD/MM/YYYY')
    },
    {
        title: 'Ngày sửa',
        dataIndex: 'ngay_sua',
        key: 'ngay_sua',
        width: '15%',
        customRender: ({ text }) => text ? dayjs(text).format('HH:mm:ss DD/MM/YYYY') : ''
    },
    {
        title: 'Thao tác',
        key: 'action',
        width: '15%'
    }
]

const brandColumns = [
    {
        title: 'Mã thương hiệu',
        dataIndex: 'ma_thuong_hieu',
        key: 'ma_thuong_hieu',
        width: '15%'
    },
    {
        title: 'Tên thương hiệu',
        dataIndex: 'ten_thuong_hieu',
        key: 'ten_thuong_hieu',
        width: '25%'
    },
    {
        title: 'Trạng thái',
        dataIndex: 'trang_thai',
        key: 'status',
        width: '15%'
    },
    {
        title: 'Ngày tạo',
        dataIndex: 'ngay_tao',
        key: 'ngay_tao',
        width: '15%',
        customRender: ({ text }) => dayjs(text).format('HH:mm:ss DD/MM/YYYY')
    },
    {
        title: 'Ngày sửa',
        dataIndex: 'ngay_sua',
        key: 'ngay_sua',
        width: '15%',
        customRender: ({ text }) => text ? dayjs(text).format('HH:mm:ss DD/MM/YYYY') : ''
    },
    {
        title: 'Thao tác',
        key: 'action',
        width: '15%'
    }
]

const materialColumns = [
    {
        title: 'Mã chất liệu',
        dataIndex: 'ma_chat_lieu',
        key: 'ma_chat_lieu',
        width: '15%'
    },
    {
        title: 'Tên chất liệu',
        dataIndex: 'ten_chat_lieu',
        key: 'ten_chat_lieu',
        width: '25%'
    },
    {
        title: 'Trạng thái',
        dataIndex: 'trang_thai',
        key: 'status',
        width: '15%'
    },
    {
        title: 'Ngày tạo',
        dataIndex: 'ngay_tao',
        key: 'ngay_tao',
        width: '15%',
        customRender: ({ text }) => dayjs(text).format('HH:mm:ss DD/MM/YYYY')
    },
    {
        title: 'Ngày sửa',
        dataIndex: 'ngay_sua',
        key: 'ngay_sua',
        width: '15%',
        customRender: ({ text }) => text ? dayjs(text).format('HH:mm:ss DD/MM/YYYY') : ''
    },
    {
        title: 'Thao tác',
        key: 'action',
        width: '15%'
    }
]

const colorColumns = [
    {
        title: 'Mã màu sắc',
        dataIndex: 'ma_mau_sac',
        key: 'ma_mau_sac',
        width: '15%'
    },
    {
        title: 'Tên màu sắc',
        dataIndex: 'ten_mau_sac',
        key: 'ten_mau_sac',
        width: '25%'
    },
    {
        title: 'Trạng thái',
        dataIndex: 'trang_thai',
        key: 'status',
        width: '15%'
    },
    {
        title: 'Ngày tạo',
        dataIndex: 'ngay_tao',
        key: 'ngay_tao',
        width: '15%',
        customRender: ({ text }) => dayjs(text).format('HH:mm:ss DD/MM/YYYY')
    },
    {
        title: 'Ngày sửa',
        dataIndex: 'ngay_sua',
        key: 'ngay_sua',
        width: '15%',
        customRender: ({ text }) => text ? dayjs(text).format('HH:mm:ss DD/MM/YYYY') : ''
    },
    {
        title: 'Thao tác',
        key: 'action',
        width: '15%'
    }
]

const sizeColumns = [
    {
        title: 'Mã kích thước',
        dataIndex: 'ma_kich_thuoc',
        key: 'ma_kich_thuoc',
        width: '15%'
    },
    {
        title: 'Kích thước',
        dataIndex: 'gia_tri',
        key: 'kich_thuoc',
        width: '25%',
        customRender: ({ record }) => `${record.gia_tri} ${record.don_vi}`
    },
    {
        title: 'Đơn vị',
        dataIndex: 'don_vi',
        key: 'don_vi',
        width: '15%'
    },
    {
        title: 'Trạng thái',
        dataIndex: 'trang_thai',
        key: 'status',
        width: '15%'
    },
    {
        title: 'Ngày tạo',
        dataIndex: 'ngay_tao',
        key: 'ngay_tao',
        width: '15%',
        customRender: ({ text }) => dayjs(text).format('HH:mm:ss DD/MM/YYYY')
    },
    {
        title: 'Ngày sửa',
        dataIndex: 'ngay_sua',
        key: 'ngay_sua',
        width: '15%',
        customRender: ({ text }) => text ? dayjs(text).format('HH:mm:ss DD/MM/YYYY') : ''
    },
    {
        title: 'Thao tác',
        key: 'action',
        width: '15%'
    }
]

// Data states
const categories = ref([])
const brands = ref([])
const materials = ref([])
const colors = ref([])
const sizes = ref([])

// Load data khi component được mount
onMounted(async () => {
    try {
        await Promise.all([
            loadCategories(),
            loadBrands(),
            loadMaterials(),
            loadColors(),
            loadSizes()
        ])
    } catch (error) {
        console.error('Error loading data:', error)
        message.error('Có lỗi khi tải dữ liệu')
    }
})

// Load functions
async function loadCategories() {
    try {
        const response = await store.getDanhMucList()
        categories.value = response.map(item => ({
            ...item,
            key: item.id_danh_muc,
            isEditing: false,
            editName: '',
            status: item.trang_thai
        })).sort((a, b) => new Date(b.ngay_tao) - new Date(a.ngay_tao))
    } catch (error) {
        console.error('Error loading categories:', error)
        message.error('Không thể tải danh sách danh mục')
    }
}

async function loadBrands() {
    try {
        const response = await store.getThuongHieuList()
        brands.value = response.map(item => ({
            ...item,
            key: item.id_thuong_hieu,
            isEditing: false,
            editName: '',
            status: item.trang_thai
        })).sort((a, b) => new Date(b.ngay_tao) - new Date(a.ngay_tao))
    } catch (error) {
        console.error('Error loading brands:', error)
        message.error('Không thể tải danh sách thương hiệu')
    }
}

async function loadMaterials() {
    try {
        const response = await store.getChatLieuList()
        materials.value = response.map(item => ({
            ...item,
            key: item.id_chat_lieu,
            isEditing: false,
            editName: '',
            status: item.trang_thai
        })).sort((a, b) => new Date(b.ngay_tao) - new Date(a.ngay_tao))
    } catch (error) {
        console.error('Error loading materials:', error)
        message.error('Không thể tải danh sách chất liệu')
    }
}

async function loadColors() {
    try {
        const response = await store.getMauSacList()
        colors.value = response.map(item => ({
            ...item,
            key: item.id_mau_sac,
            isEditing: false,
            editName: '',
            status: item.trang_thai
        })).sort((a, b) => new Date(b.ngay_tao) - new Date(a.ngay_tao))
    } catch (error) {
        console.error('Error loading colors:', error)
        message.error('Không thể tải danh sách màu sắc')
    }
}

async function loadSizes() {
    try {
        const response = await store.getSizeList()
        sizes.value = response.map(item => ({
            ...item,
            key: item.id_kich_thuoc,
            isEditing: false,
            editName: '',
            status: item.trang_thai
        })).sort((a, b) => new Date(b.ngay_tao) - new Date(a.ngay_tao))
    } catch (error) {
        console.error('Error loading sizes:', error)
        message.error('Không thể tải danh sách kích thước')
    }
}

// New attribute input
const newAttribute = ref({
    name: '', // for other tabs
    value: '', // for size value
    unit: ''  // for size unit
})

// Get current items based on selected tab
const getCurrentItems = computed(() => {
    switch (currentTab.value) {
        case 'category': return categories.value
        case 'brand': return brands.value
        case 'material': return materials.value
        case 'color': return colors.value
        case 'size': return sizes.value
        default: return []
    }
})

// Thêm computed property để lọc dữ liệu
const getFilteredData = computed(() => {
    const allItems = getCurrentItems.value
    const currentFilter = statusFilters.value[currentTab.value]

    // Lọc theo trạng thái nếu cần
    let filteredItems = allItems
    if (currentFilter !== 'all') {
        filteredItems = allItems.filter(item => {
            return currentFilter === 'active' ? item.status : !item.status
        })
    }

    // Phân trang dữ liệu đã lọc
    const startIndex = (currentPage.value - 1) * 5
    const endIndex = startIndex + 5
    return filteredItems.slice(startIndex, endIndex)
})

// Thêm computed property để lấy tổng số item đã lọc
const getTotalFilteredItems = computed(() => {
    const allItems = getCurrentItems.value
    const currentFilter = statusFilters.value[currentTab.value]

    // Nếu hiển thị tất cả, trả về tổng số item
    if (currentFilter === 'all') {
        return allItems.length
    }

    // Nếu lọc, trả về số lượng item đã lọc
    return allItems.filter(item => {
        return currentFilter === 'active' ? item.status : !item.status
    }).length
})

// Thêm hàm xử lý khi thay đổi trang
const onPageChange = (page) => {
    currentPage.value = page
}

// Thêm hàm xử lý khi thay đổi trạng thái
const handleStatusChange = async (index, checked) => {
    try {
        const allItems = getCurrentItems.value
        const item = allItems[index]

        switch (currentTab.value) {
            case 'category':
                await store.changeTrangThaiDanhMuc(item.id_danh_muc)
                break
            case 'brand':
                await store.changeTrangThaiThuongHieu(item.id_thuong_hieu)
                break
            case 'material':
                await store.changeTrangThaiChatLieu(item.id_chat_lieu)
                break
            case 'color':
                await store.changeTrangThaiMauSac(item.id_mau_sac)
                break
            case 'size':
                await store.changeTrangThaiKichThuoc(item.id_kich_thuoc)
                break
        }

        // Tải lại dữ liệu sau khi cập nhật
        await loadCurrentTab()
    } catch (error) {
        console.error('Error updating status:', error)
        message.error('Có lỗi xảy ra khi cập nhật trạng thái')
    }
}

// Thêm hàm validateNewAttribute
const validateNewAttribute = () => {
    isValidNewAttr.value = true;

    // Reset validation status
    validationStatus.value.category = '';
    validationStatus.value.brand = '';
    validationStatus.value.material = '';
    validationStatus.value.color = '';
    validationStatus.value.sizeValue = '';

    // Reset validation error messages
    validationError.value.category = '';
    validationError.value.brand = '';
    validationError.value.material = '';
    validationError.value.color = '';
    validationError.value.sizeValue = '';

    if (currentTab.value === 'size') {
        // Validate size
        if (!newAttribute.value.value?.trim()) {
            validationStatus.value.sizeValue = 'error';
            validationError.value.sizeValue = 'Vui lòng nhập giá trị kích thước!';
            isValidNewAttr.value = false;
            return;
        }



        // Kiểm tra trùng lặp size
        const isDuplicateSize = sizes.value.some(item =>
            item.gia_tri?.toLowerCase() === newAttribute.value.value.trim().toLowerCase() &&
            item.don_vi?.toLowerCase() === (newAttribute.value.unit?.trim() || '').toLowerCase() &&
            item.trang_thai
        );

        if (isDuplicateSize) {
            validationStatus.value.sizeValue = 'error';
            validationError.value.sizeValue = 'Kích thước này đã tồn tại!';
            isValidNewAttr.value = false;
            return;
        }
    } else {
        // Validate name for other tabs
        if (!newAttribute.value.name?.trim()) {
            validationStatus.value[currentTab.value] = 'error';
            validationError.value[currentTab.value] = 'Vui lòng nhập tên thuộc tính!';
            isValidNewAttr.value = false;
            return;
        }

        // Kiểm tra độ dài tên
        if (newAttribute.value.name.trim().length < 2) {
            validationStatus.value[currentTab.value] = 'error';
            validationError.value[currentTab.value] = 'Tên phải có ít nhất 2 ký tự!';
            isValidNewAttr.value = false;
            return;
        }

        // Giới hạn độ dài tùy thuộc vào loại thuộc tính
        const maxLength = currentTab.value === 'color' ? 15 : 50;
        if (newAttribute.value.name.trim().length > maxLength) {
            validationStatus.value[currentTab.value] = 'error';
            validationError.value[currentTab.value] = `Tên không được vượt quá ${maxLength} ký tự!`;
            isValidNewAttr.value = false;
            return;
        }

        // Kiểm tra ký tự hợp lệ - không chứa số
        const regex = /^[a-zA-ZÀ-ỹ\s\-]+$/;

        if (!regex.test(newAttribute.value.name)) {
            validationStatus.value[currentTab.value] = 'error';
            validationError.value[currentTab.value] = 'Tên chỉ được chứa chữ cái, dấu cách và dấu gạch ngang, không chứa số';
            isValidNewAttr.value = false;
            return;
        }

        // Kiểm tra trùng lặp
        let currentList = [];
        let checkField = '';

        switch (currentTab.value) {
            case 'category':
                currentList = categories.value;
                checkField = 'ten_danh_muc';
                break;
            case 'brand':
                currentList = brands.value;
                checkField = 'ten_thuong_hieu';
                break;
            case 'material':
                currentList = materials.value;
                checkField = 'ten_chat_lieu';
                break;
            case 'color':
                currentList = colors.value;
                checkField = 'ten_mau_sac';
                break;
        }

        const isDuplicate = currentList.some(item =>
            item[checkField]?.toLowerCase() === newAttribute.value.name.trim().toLowerCase() &&
            item.trang_thai
        );

        if (isDuplicate) {
            validationStatus.value[currentTab.value] = 'error';
            validationError.value[currentTab.value] = 'Tên này đã tồn tại!';
            isValidNewAttr.value = false;
            return;
        }
    }
}

// Methods for managing attributes
const addNewAttribute = async () => {
    validateNewAttribute();

    if (!isValidNewAttr.value) {
        return;
    }

    try {
        let response;

        switch (currentTab.value) {
            case 'size':
                response = await store.addKichThuoc(
                    newAttribute.value.value.trim(),
                    newAttribute.value.unit.trim()
                );
                break;
            case 'category':
                response = await store.addDanhMuc(newAttribute.value.name.trim());
                break;
            case 'brand':
                response = await store.addThuongHieu(newAttribute.value.name.trim());
                break;
            case 'material':
                response = await store.addChatLieu(newAttribute.value.name.trim());
                break;
            case 'color':
                response = await store.addMauSac(newAttribute.value.name.trim());
                break;
        }

        if (!response || response.error) {
            throw new Error('Có lỗi xảy ra khi thêm mới');
        }

        // Reset form
        if (currentTab.value === 'size') {
            newAttribute.value.value = '';
            newAttribute.value.unit = '';
        } else {
            newAttribute.value.name = '';
        }

        // Reset validation
        validationStatus.value[currentTab.value] = '';
        validationError.value[currentTab.value] = '';

        // Reload data
        await loadCurrentTab();
        message.success('Thêm mới thành công!');
    } catch (error) {
        console.error('Error adding:', error);
        message.error('Có lỗi xảy ra khi thêm mới: ' + error.message);
    }
};

const showEditModal = (record, type) => {
    // Xác định dữ liệu cần chỉnh sửa dựa trên tab hiện tại
    let data = {};

    switch (type) {
        case 'category':
            data = {
                id: record.id_danh_muc,
                name: record.ten_danh_muc,
            };
            break;
        case 'brand':
            data = {
                id: record.id_thuong_hieu,
                name: record.ten_thuong_hieu,
            };
            break;
        case 'material':
            data = {
                id: record.id_chat_lieu,
                name: record.ten_chat_lieu,
            };
            break;
        case 'color':
            data = {
                id: record.id_mau_sac,
                name: record.ten_mau_sac,
            };
            break;
        case 'size':
            data = {
                id: record.id_kich_thuoc,
                value: record.gia_tri,
                unit: record.don_vi || '',
            };
            break;
    }

    // Reset validation
    editValidationStatus.value.name = '';
    editValidationError.value.name = '';
    editValidationStatus.value.value = '';
    editValidationError.value.value = '';

    // Cập nhật dữ liệu đang chỉnh sửa
    editingData.value = data;
    editModalVisible.value = true;
    currentTab.value = type;
    editModalTitle.value = `Sửa ${type === 'size' ? 'kích thước' : type.charAt(0).toUpperCase() + type.slice(1)}`;
}

const saveEditModal = async () => {
    if (!validateEditingData()) {
        return;
    }

    try {
        let updateData;
        let response;

        if (currentTab.value === 'size') {
            updateData = {
                id_kich_thuoc: editingData.value.id,
                gia_tri: editingData.value.value.trim(),
                don_vi: editingData.value.unit?.trim() || '',
                trang_thai: true
            };
            response = await store.updateKichThuoc(updateData);
        } else {
            switch (currentTab.value) {
                case 'category':
                    updateData = {
                        id_danh_muc: editingData.value.id,
                        ten_danh_muc: editingData.value.name.trim(),
                        trang_thai: true
                    };
                    response = await store.updateDanhMuc(updateData);
                    break;
                case 'brand':
                    updateData = {
                        id_thuong_hieu: editingData.value.id,
                        ten_thuong_hieu: editingData.value.name.trim(),
                        trang_thai: true
                    };
                    response = await store.updateThuongHieu(updateData);
                    break;
                case 'material':
                    updateData = {
                        id_chat_lieu: editingData.value.id,
                        ten_chat_lieu: editingData.value.name.trim(),
                        trang_thai: true
                    };
                    response = await store.updateChatLieu(updateData);
                    break;
                case 'color':
                    updateData = {
                        id_mau_sac: editingData.value.id,
                        ten_mau_sac: editingData.value.name.trim(),
                        trang_thai: true
                    };
                    response = await store.updateMauSac(updateData);
                    break;
            }
        }

        if (!response || response.error) {
            throw new Error('Có lỗi xảy ra khi cập nhật');
        }

        message.success('Cập nhật thành công!');
        editModalVisible.value = false;
        await loadCurrentTab();
    } catch (error) {
        console.error('Error updating:', error);
        message.error('Có lỗi xảy ra khi cập nhật: ' + error.message);
    }
};

const validateEditingData = () => {
    // Reset validation
    editValidationStatus.value.name = '';
    editValidationError.value.name = '';
    editValidationStatus.value.value = '';
    editValidationError.value.value = '';

    if (currentTab.value === 'size') {
        // Validate size value
        if (!editingData.value.value?.trim()) {
            editValidationStatus.value.value = 'error';
            editValidationError.value.value = 'Vui lòng nhập giá trị kích thước!';
            return false;
        }



        // Kiểm tra trùng lặp size (ngoại trừ chính nó)
        const isDuplicateSize = sizes.value.some(item =>
            item.id_kich_thuoc !== editingData.value.id &&
            item.gia_tri?.toLowerCase() === editingData.value.value.trim().toLowerCase() &&
            item.don_vi?.toLowerCase() === (editingData.value.unit?.trim() || '').toLowerCase() &&
            item.trang_thai
        );

        if (isDuplicateSize) {
            editValidationStatus.value.value = 'error';
            editValidationError.value.value = 'Kích thước này đã tồn tại!';
            return false;
        }
    } else {
        // Validate name for other attributes
        if (!editingData.value.name?.trim()) {
            editValidationStatus.value.name = 'error';
            editValidationError.value.name = 'Vui lòng nhập tên thuộc tính!';
            return false;
        }

        // Kiểm tra độ dài tên
        if (editingData.value.name.trim().length < 2) {
            editValidationStatus.value.name = 'error';
            editValidationError.value.name = 'Tên phải có ít nhất 2 ký tự!';
            return false;
        }

        // Giới hạn độ dài tùy thuộc vào loại thuộc tính
        const maxLength = currentTab.value === 'color' ? 15 : 50;
        if (editingData.value.name.trim().length > maxLength) {
            editValidationStatus.value.name = 'error';
            editValidationError.value.name = `Tên không được vượt quá ${maxLength} ký tự!`;
            return false;
        }

        // Kiểm tra ký tự hợp lệ - không chứa số
        const regex = /^[a-zA-ZÀ-ỹ\s\-]+$/;

        if (!regex.test(editingData.value.name)) {
            editValidationStatus.value.name = 'error';
            editValidationError.value.name = 'Tên chỉ được chứa chữ cái, dấu cách và dấu gạch ngang, không chứa số';
            return false;
        }

        // Kiểm tra trùng lặp tên (ngoại trừ chính nó)
        let currentList = [];
        let checkField = '';
        let idField = '';

        switch (currentTab.value) {
            case 'category':
                currentList = categories.value;
                checkField = 'ten_danh_muc';
                idField = 'id_danh_muc';
                break;
            case 'brand':
                currentList = brands.value;
                checkField = 'ten_thuong_hieu';
                idField = 'id_thuong_hieu';
                break;
            case 'material':
                currentList = materials.value;
                checkField = 'ten_chat_lieu';
                idField = 'id_chat_lieu';
                break;
            case 'color':
                currentList = colors.value;
                checkField = 'ten_mau_sac';
                idField = 'id_mau_sac';
                break;
        }

        const isDuplicate = currentList.some(item =>
            item[idField] !== editingData.value.id &&
            item[checkField]?.toLowerCase() === editingData.value.name.trim().toLowerCase() &&
            item.trang_thai
        );

        if (isDuplicate) {
            editValidationStatus.value.name = 'error';
            editValidationError.value.name = 'Tên này đã tồn tại!';
            return false;
        }
    }

    return true;
};

// Get the label for the name field based on current tab
const getNameLabel = () => {
    switch (currentTab.value) {
        case 'category': return 'Tên danh mục';
        case 'brand': return 'Tên thương hiệu';
        case 'material': return 'Tên chất liệu';
        case 'color': return 'Tên màu sắc';
        default: return 'Tên';
    }
};

// Function to load data for current tab
const loadCurrentTab = async () => {
    switch (currentTab.value) {
        case 'category': await loadCategories(); break;
        case 'brand': await loadBrands(); break;
        case 'material': await loadMaterials(); break;
        case 'color': await loadColors(); break;
        case 'size': await loadSizes(); break;
    }
};

// Thêm phương thức xử lý thay đổi bộ lọc
const onFilterChange = (tabKey) => {
    currentPage.value = 1 // Reset về trang đầu tiên khi thay đổi bộ lọc
}

// Watch for tab changes to reset forms and validation
watch(currentTab, () => {
    // Reset form data
    newAttribute.value = {
        name: '',
        value: '',
        unit: ''
    };

    // Reset validation
    Object.keys(validationStatus.value).forEach(key => {
        validationStatus.value[key] = '';
        validationError.value[key] = '';
    });

    isValidNewAttr.value = true;

    // Reset về trang đầu tiên
    currentPage.value = 1;
});
</script>

<style scoped>
.product-attributes {
    padding: 20px;
}

.page-title {
    color: #333;
    margin-bottom: 24px;
    font-size: 24px;
}

.add-form {
    margin-bottom: 20px;
}

.input-field {
    width: 100%;
}

.attribute-actions {
    display: flex;
    gap: 8px;
}

:deep(.ant-input-group-addon) {
    padding: 0;
}

:deep(.ant-input-group-addon .ant-btn) {
    border-start-start-radius: 0;
    border-end-start-radius: 0;
}

:deep(.ant-card-body) {
    padding: 24px;
}

:deep(.ant-table-thead > tr > th) {
    background: #f0f2f5;
}

/* Căn chỉnh icon + ở giữa theo chiều dọc */
:deep(.anticon-plus) {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    vertical-align: middle;
    height: 100%;
}

:deep(.ant-btn > .anticon) {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    vertical-align: middle;
}

/* Cập nhật màu hover và focus cho input */
:deep(.ant-input:hover) {
    border-color: #ff6b35 !important;
}

:deep(.ant-input:focus),
:deep(.ant-input-focused),
:deep(.ant-input-affix-wrapper:focus),
:deep(.ant-input-affix-wrapper-focused) {
    border-color: #ff6b35 !important;
    box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.2) !important;
}

:deep(.ant-input-affix-wrapper:hover) {
    border-color: #ff6b35 !important;
}

/* Cập nhật màu tab khi được nhấn giữ */
:deep(.ant-tabs-tab:active) {
    color: #ff6b35 !important;
}

:deep(.ant-tabs-tab.ant-tabs-tab-active:active) {
    color: #e05545 !important;
}

:deep(.ant-tabs-tab-btn:active) {
    color: #ff6b35 !important;
}

:deep(.ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn:active) {
    color: #e05545 !important;
}

/* Cập nhật màu pagination khi active */
:deep(.ant-pagination-item:active) {
    border-color: #e05545 !important;
}

:deep(.ant-pagination-item:active a) {
    color: #e05545 !important;
}

/* Màu nút switch */
:deep(.ant-switch) {
    min-width: 70px;
    height: 24px;
    line-height: 22px;
    background-color: #ccc;
}

:deep(.ant-switch-checked) {
    background-color: #ff6b35 !important;
}

:deep(.ant-switch:hover:not(.ant-switch-disabled)) {
    background-color: #ffa166;
}

:deep(.ant-switch-handle) {
    width: 20px;
    height: 20px;
}

:deep(.ant-switch-checked .ant-switch-handle) {
    left: calc(100% - 20px - 2px);
}

:deep(.ant-switch-inner) {
    margin: 0 25px 0 7px;
}

/* Đồng bộ màu các nút */
:deep(.ant-btn-primary) {
    background-color: #ff6b35 !important;
    border-color: #ff6b35 !important;
}

:deep(.ant-btn-primary:hover) {
    background-color: #ffa166 !important;
    border-color: #ffa166 !important;
}

:deep(.ant-btn-primary:active) {
    background-color: #e05545 !important;
    border-color: #e05545 !important;
}

/* Đồng bộ màu tabs */
:deep(.ant-tabs) {
    color: rgba(0, 0, 0, 0.65);
}

:deep(.ant-tabs-tab) {
    color: rgba(0, 0, 0, 0.65);
}

:deep(.ant-tabs-tab:hover) {
    color: #ff6b35 !important;
}

:deep(.ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn) {
    color: #ff6b35 !important;
    font-weight: 500;
}

:deep(.ant-tabs-ink-bar) {
    background-color: #ff6b35 !important;
}

:deep(.ant-tabs-nav::before) {
    border-bottom-color: #ff6b35 !important;
}

:deep(.ant-tabs-nav-list .ant-tabs-tab-active) {
    border-bottom-color: #ff6b35 !important;
}

/* Đồng bộ màu thanh phân trang (pagination) */
:deep(.ant-pagination-item:hover) {
    border-color: #ff6b35 !important;
}

:deep(.ant-pagination-item:hover a) {
    color: #ff6b35 !important;
}

:deep(.ant-pagination-item-active) {
    border-color: #ff6b35 !important;
}

:deep(.ant-pagination-item-active a) {
    color: #ff6b35 !important;
}

:deep(.ant-pagination-prev:hover .ant-pagination-item-link),
:deep(.ant-pagination-next:hover .ant-pagination-item-link) {
    color: #ff6b35 !important;
    border-color: #ff6b35 !important;
}

:deep(.ant-pagination-jump-prev .ant-pagination-item-container .ant-pagination-item-link-icon),
:deep(.ant-pagination-jump-next .ant-pagination-item-container .ant-pagination-item-link-icon) {
    color: #ff6b35 !important;
}

.validation-error {
    color: #ff4d4f;
    font-size: 14px;
    margin-top: 4px;
}

/* Đồng bộ màu select (bộ lọc) */
:deep(.ant-select:not(.ant-select-disabled):hover .ant-select-selector) {
    border-color: #ff6b35 !important;
}

:deep(.ant-select-focused:not(.ant-select-disabled).ant-select:not(.ant-select-customize-input) .ant-select-selector) {
    border-color: #ff6b35 !important;
    box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.2) !important;
}

:deep(.ant-select-item-option-selected:not(.ant-select-item-option-disabled)) {
    background-color: rgba(255, 107, 53, 0.1) !important;
    color: #ff6b35 !important;
}

:deep(.ant-select-item-option-active:not(.ant-select-item-option-disabled)) {
    background-color: rgba(255, 107, 53, 0.1) !important;
}

:deep(.ant-select-arrow) {
    color: rgba(0, 0, 0, 0.5);
}

:deep(.ant-select:hover .ant-select-arrow) {
    color: #ff6b35 !important;
}

:deep(.ant-select-focused .ant-select-arrow) {
    color: #ff6b35 !important;
}
</style>