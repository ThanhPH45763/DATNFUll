<template>
    <div class="mb-4 d-flex justify-content-between">
        <div class="d-flex gap-2 flex-wrap">
            <template
                v-if="!store.checkRouter.includes('/quanlysanpham/add') && !store.checkRouter.includes('/quanlysanpham/update')">
                <a-button type="" @click="showFilter" class="d-flex align-items-center btn-filter">
                    <FilterOutlined class="icon-filler" />
                    <span class="button-text">Bộ lọc</span>
                </a-button>

                <a-button v-if="isFiltering" type="danger" style="margin-left:10px;border-radius: 5px;"
                    @click="resetFilter">
                    <SyncOutlined />
                    <span class="button-text">Xóa bộ lọc</span>
                </a-button>

                <a-drawer v-model:open="visible" class="custom-class" root-class-name="root-class-name"
                    :root-style="{ color: 'black' }" title="Bộ lọc sản phẩm" placement="right"
                    @after-open-change="afterOpenChange" :footer-style="{ textAlign: 'right' }">

                    <div class="drawer-content">
                        <!-- Hiển thị trạng thái tải dữ liệu -->
                        <div v-if="danhMucList.length === 0 || thuongHieuList.length === 0" class="loading-state">
                            <p>Đang tải dữ liệu...</p>
                        </div>

                        <!-- Bộ lọc Danh mục -->
                        <div class="filter-group">
                            <div class="filter-title">Danh mục</div>
                            <a-select v-model:value="valueDanhMuc" @change="updatePreview" style="width: 100%"
                                mode="multiple" placeholder="Chọn danh mục" :maxTagCount="2"
                                :maxTagPlaceholder="(omittedValues) => `+ ${omittedValues.length} danh mục khác`">
                                <a-select-option v-for="item in danhMucList" :key="item.id_danh_muc"
                                    :value="item.id_danh_muc">
                                    {{ item.ten_danh_muc }}
                                </a-select-option>
                            </a-select>
                        </div>

                        <!-- Bộ lọc Thương hiệu -->
                        <div class="filter-group">
                            <div class="filter-title">Thương hiệu</div>
                            <a-select v-model:value="valueThuongHieu" @change="updatePreview" style="width: 100%"
                                mode="multiple" placeholder="Chọn thương hiệu" :maxTagCount="2"
                                :maxTagPlaceholder="(omittedValues) => `+ ${omittedValues.length} thương hiệu khác`">
                                <a-select-option v-for="item in thuongHieuList" :key="item.id_thuong_hieu"
                                    :value="item.id_thuong_hieu">
                                    {{ item.ten_thuong_hieu }}
                                </a-select-option>
                            </a-select>
                        </div>

                        <!-- Bộ lọc Chất liệu -->
                        <div class="filter-group">
                            <div class="filter-title">Chất liệu</div>
                            <a-select v-model:value="valueChatLieu" @change="updatePreview" style="width: 100%"
                                mode="multiple" placeholder="Chọn chất liệu" :maxTagCount="2"
                                :maxTagPlaceholder="(omittedValues) => `+ ${omittedValues.length} chất liệu khác`">
                                <a-select-option v-for="item in chatLieuList" :key="item.id_chat_lieu"
                                    :value="item.id_chat_lieu">
                                    {{ item.ten_chat_lieu }}
                                </a-select-option>
                            </a-select>
                        </div>

                        <!-- Bộ lọc Màu sắc -->
                        <div class="filter-group">
                            <div class="filter-title">Màu sắc</div>
                            <a-select v-model:value="valueMauSac" @change="updatePreview" style="width: 100%"
                                mode="multiple" placeholder="Chọn màu sắc" :maxTagCount="2"
                                :maxTagPlaceholder="(omittedValues) => `+ ${omittedValues.length} màu sắc khác`">
                                <a-select-option v-for="item in mauSacList" :key="item.id_mau_sac"
                                    :value="item.id_mau_sac">
                                    {{ item.ten_mau_sac }}
                                </a-select-option>
                            </a-select>
                        </div>

                        <!-- Bộ lọc Kích thước -->
                        <div class="filter-group">
                            <div class="filter-title">Kích thước</div>
                            <a-select v-model:value="valueSize" @change="updatePreview" style="width: 100%"
                                mode="multiple" placeholder="Chọn kích thước" :maxTagCount="2"
                                :maxTagPlaceholder="(omittedValues) => `+ ${omittedValues.length} kích thước khác`">
                                <a-select-option v-for="item in kichThuocList" :key="item.id_kich_thuoc"
                                    :value="item.id_kich_thuoc">
                                    {{ item.gia_tri + ' ' + item.don_vi }}
                                </a-select-option>
                            </a-select>
                        </div>

                        <!-- Bộ lọc Giá -->
                        <div class="filter-group">
                            <div class="filter-title">Giá</div>
                            <a-slider v-model:value="value2" @change="updatePreview" range :min="0"
                                :max="maxPriceFromProducts" :step="100000"
                                :tipFormatter="value => `${value.toLocaleString('vi-VN')} đ`" />
                            <div class="price-range">
                                <span>{{ value2[0].toLocaleString('vi-VN') }} đ</span>
                                <span>{{ value2[1].toLocaleString('vi-VN') }} đ</span>
                            </div>
                        </div>

                        <!-- Nút Áp dụng bộ lọc -->
                        <!-- Thay thế đoạn code hiện tại hiển thị dự đoán sản phẩm -->
                        <div class="align-items-center mt-4 mb-2">
                            <div class="preview-count ">
                                <template v-if="isPreviewLoading">
                                    <a-spin size="small" />
                                    <span class="ms-2">Đang tính toán...</span>
                                </template>
                                <template v-else>
                                    <a-tag color="#ff6600">
                                        <strong>{{ previewFilterCount }}</strong> sản phẩm sẽ được hiển thị
                                    </a-tag>
                                </template>
                            </div>
                            <br>
                            <div class="d-flex justify-content-between align-items-center">
                                <a-button class="me-2" @click="resetFilter">
                                    Reset
                                </a-button>
                                <a-button type="primary" @click="filterProducts" :loading="isPreviewLoading">
                                    Áp dụng
                                </a-button>
                            </div>
                        </div>

                    </div>
                </a-drawer>

                <!-- <a-select class="mb-2 ms-2 custom-select" v-model:value="luuBien" show-search placeholder="Sắp xếp"
                    style="width: 150px;" :options="listSort" :filter-option="filterOption"></a-select> -->
                <!-- <a-select class="mb-2 ms-2 custom-select" v-model:value="xemTheo" show-search placeholder="Xem theo"
                    style="width: 150px;" :options="listXemTheo" :filter-option="filterOption"></a-select> -->

                <a-button v-if="store.id_roles !== 3" type="" class="d-flex align-items-center btn-filter"
                    @click="showExportModal" :disabled="disabledByRoles">
                    <ExportOutlined class="icon-filler" />
                    <span class="button-text">Xuất excel</span>
                </a-button>
            </template>
            <!-- Nhập excel button always visible -->
            <a-button v-if="store.id_roles !== 3" type="" class="d-flex align-items-center btn-filter"
                @click="openModalImportExcel = true">
                <ImportOutlined class="icon-filler" />
                <span class="button-text">Nhập excel</span>
            </a-button>
            <!-- Modal nhập excel -->
            <a-modal v-model:open="openModalImportExcel" title="Nhập Excel" width="650px">
                <div class="upload-container">
                    <div class="upload-instructions mb-3">
                        <h5>Hướng dẫn nhập Excel</h5>
                        <ol>
                            <li>Tải <a @click.prevent="downloadTemplate" href="#" class="template-link">file mẫu</a>
                                hoặc sử dụng
                                file Excel có định dạng tương tự</li>
                            <li>Điền thông tin sản phẩm (các trường có dấu * là bắt buộc)</li>
                            <li>Tải lên file và kiểm tra dữ liệu trước khi lưu</li>
                        </ol>
                    </div>

                    <div class="upload-area" :class="{ 'has-file': selectedFile, 'is-dragging': isDragging }"
                        @dragenter.prevent="isDragging = true" @dragover.prevent="isDragging = true"
                        @dragleave.prevent="isDragging = false" @drop.prevent="handleFileDrop">
                        <input type="file" ref="fileInput" style="display: none" accept=".xlsx,.xls"
                            @change="handleManualFileChange" />

                        <template v-if="!selectedFile">
                            <div class="upload-placeholder">
                                <upload-outlined class="upload-icon" />
                                <p class="mb-2">Kéo thả file Excel vào đây hoặc</p>
                                <a-button type="primary" @click="triggerFileSelect">
                                    Chọn file
                                </a-button>
                            </div>
                        </template>

                        <div v-else class="selected-file">
                            <file-excel-outlined class="file-icon" />
                            <div class="file-info">
                                <p class="file-name">{{ selectedFile.name }}</p>
                                <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
                            </div>
                            <div class="file-actions">
                                <a-button type="text" @click="replaceFile">
                                    <reload-outlined />
                                </a-button>
                                <a-button type="text" @click="removeFile">
                                    <delete-outlined />
                                </a-button>
                            </div>
                        </div>
                    </div>

                    <div v-if="importError" class="error-message mt-3">
                        <alert-outlined />
                        <span>{{ importError }}</span>
                    </div>

                    <div v-if="importValidating" class="validating-status mt-3">
                        <loading-outlined spin />
                        <span>Đang kiểm tra dữ liệu...</span>
                    </div>
                </div>

                <template #footer>
                    <a-button key="back" @click="closeImportModal">Hủy</a-button>
                    <a-button key="preview" type="default" :disabled="!selectedFile || uploadLoading"
                        @click="validateAndPreview">
                        <eye-outlined />
                        Xem trước
                    </a-button>
                    <a-button key="submit" type="primary" :loading="uploadLoading"
                        :disabled="!selectedFile || uploadLoading" @click="handleImportExcel">
                        <import-outlined />
                        Import
                    </a-button>
                </template>
            </a-modal>
            <!-- Modal hiển thị dữ liệu import -->
            <a-modal v-model:open="importExcelModal" title="Xác nhận dữ liệu import" width="90%"
                :style="{ top: '20px' }">
                <div class="data-preview-stats" v-if="importExcelData.length">
                    <div class="stat-item">
                        <p class="stat-value">{{ importExcelData.length }}</p>
                        <p class="stat-label">Tổng số dòng</p>
                    </div>
                    <div class="stat-item">
                        <p class="stat-value">{{ countValidRows() }}</p>
                        <p class="stat-label">Dòng hợp lệ</p>
                    </div>
                    <div class="stat-item text-warning" v-if="countInvalidRows() > 0">
                        <p class="stat-value">{{ countInvalidRows() }}</p>
                        <p class="stat-label">Dòng có lỗi</p>
                    </div>
                </div>

                <div class="data-action-buttons mb-3" v-if="importExcelData.length">
                    <a-radio-group v-model:value="dataPreviewMode" button-style="solid">
                        <a-radio-button value="all">Tất cả dữ liệu</a-radio-button>
                        <a-radio-button value="valid" :disabled="countValidRows() === 0">Chỉ dữ liệu hợp
                            lệ</a-radio-button>
                        <a-radio-button value="invalid" :disabled="countInvalidRows() === 0">Chỉ dữ liệu
                            lỗi</a-radio-button>
                    </a-radio-group>
                </div>

                <div class="table-container">
                    <table class="table table-bordered">
                        <thead>
                            <tr>
                                <th>STT</th>
                                <th>Tên sản phẩm</th>
                                <th>Giá bán</th>
                                <th>Số lượng</th>
                                <th>Danh mục</th>
                                <th>Thương hiệu</th>
                                <th>Chất liệu</th>
                                <th>Màu sắc</th>
                                <th>Kích thước</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(item, index) in filteredImportData" :key="index"
                                :class="{ 'has-error': hasRowError(item) }">
                                <td>{{ index + 1 }}</td>
                                <td :class="{ 'cell-error': !item.sanPham.ten_san_pham }">
                                    {{ item.sanPham.ten_san_pham || '(Thiếu dữ liệu)' }}
                                </td>
                                <td :class="{ 'cell-error': item.gia_ban === 0 }">
                                    {{ formatPrice(item.gia_ban) }}
                                </td>
                                <td :class="{ 'cell-error': item.so_luong === 0 || item.so_luong === null }">
                                    {{ item.so_luong || '(Thiếu dữ liệu)' }}
                                </td>
                                <td :class="{ 'cell-error': !item.sanPham.danhMuc.ten_danh_muc }">
                                    {{ item.sanPham.danhMuc.ten_danh_muc || '(Thiếu dữ liệu)' }}
                                </td>
                                <td :class="{ 'cell-error': !item.sanPham.thuongHieu.ten_thuong_hieu }">
                                    {{ item.sanPham.thuongHieu.ten_thuong_hieu || '(Thiếu dữ liệu)' }}
                                </td>
                                <td :class="{ 'cell-error': !item.sanPham.chatLieu.ten_chat_lieu }">
                                    {{ item.sanPham.chatLieu.ten_chat_lieu || '(Thiếu dữ liệu)' }}
                                </td>
                                <td :class="{ 'cell-error': !item.mauSac.ma_mau_sac }">
                                    {{ (item.mauSac.ma_mau_sac && item.mauSac.ten_mau_sac) ?
                                        (item.mauSac.ma_mau_sac + ' ' + item.mauSac.ten_mau_sac) : '(Thiếu dữ liệu)' }}
                                </td>
                                <td :class="{ 'cell-error': !item.kichThuoc.gia_tri }">
                                    {{ (item.kichThuoc.gia_tri) ?
                                        (item.kichThuoc.gia_tri + ' ' + item.kichThuoc.don_vi) : '(Thiếu dữ liệu)' }}
                                </td>

                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="import-summary mt-3" v-if="countInvalidRows() > 0">
                    <a-alert type="warning" show-icon>
                        <template #message>
                            <span>Cảnh báo: Có {{ countInvalidRows() }} dòng dữ liệu chứa lỗi</span>
                        </template>
                        <template #description>
                            <p>Các ô có nền đỏ là dữ liệu bị thiếu hoặc không hợp lệ. Bạn có thể:</p>
                            <ul>
                                <li>Tiếp tục import (chỉ các dòng hợp lệ sẽ được lưu)</li>
                                <li>Hủy để sửa file Excel và thử lại</li>
                            </ul>
                        </template>
                    </a-alert>
                </div>

                <template #footer>
                    <div class="d-flex justify-content-end align-items-center">
                        <a-button key="back" @click="importExcelModal = false">Hủy</a-button>
                    <a-button key="submit" style="background-color: #ff6600" type="primary" :loading="uploadLoading" :disabled="countValidRows() === 0"
                            @click="saveExcelImport" class="d-flex align-items-center">
                            <save-outlined class="icon-filler" />
                            Lưu {{ countValidRows() }} sản phẩm
                        </a-button>
                    </div>
                </template>
            </a-modal>
        </div>
        <template
            v-if="!store.checkRouter.includes('/quanlysanpham/add') && !store.checkRouter.includes('/quanlysanpham/update')">
            <a-button v-if="store.id_roles !== 3" type="primary" style="background-color: #ff6600"
                @click="changeRouter('/admin/quanlysanpham/add')" class="d-flex align-items-center">
                <PlusOutlined />
                <span class="button-text">Thêm sản phẩm</span>
            </a-button>
        </template>
    </div>

    <a-modal v-model:open="exportModalVisible" title="Xuất Excel" width="700px">
        <div class="export-modal-content">
            <p>Chọn các trường thông tin bạn muốn xuất:</p>
            <a-checkbox v-model:checked="selectAllFields" @change="handleSelectAllFields">
                Chọn tất cả các trường
            </a-checkbox>

            <div class="field-selection mt-3">
                <a-row :gutter="[16, 8]">
                    <a-col :span="8" v-for="field in exportFields" :key="field.value">
                        <a-checkbox v-model:checked="field.selected">{{ field.label }}</a-checkbox>
                    </a-col>
                </a-row>
            </div>

            <a-divider />

            <p>Chọn sản phẩm:</p>
            <a-radio-group v-model:value="exportSelection" class="mb-3">
                <a-radio value="all">Tất cả sản phẩm</a-radio>
                <a-radio value="filtered" :disabled="!isFiltering">Sản phẩm đã lọc</a-radio>
                <a-radio value="selected" :disabled="!hasSelectedProducts">Sản phẩm đã chọn</a-radio>
            </a-radio-group>

            <div v-if="exportSelection === 'selected' && selectedRows.length === 0" class="alert alert-warning">
                Bạn chưa chọn sản phẩm nào. Vui lòng chọn ít nhất một sản phẩm hoặc chọn tùy chọn khác.
            </div>
        </div>

        <template #footer>
            <a-button key="test" type="default" @click="exportTestExcel">
                <file-excel-outlined />
                Xuất dữ liệu mẫu
            </a-button>
            <a-button key="back" @click="exportModalVisible = false">Hủy</a-button>
            <a-button key="submit" type="primary" :loading="exportLoading" :disabled="!canExport"
                @click="handleExportExcel">
                Xuất Excel
            </a-button>
        </template>
    </a-modal>
</template>

<script setup>
import { ref, onMounted, computed, watch, onBeforeUnmount } from 'vue';
import * as XLSX from 'xlsx';
import {
    FilterOutlined,
    PlusOutlined,
    ExportOutlined,
    ImportOutlined,
    UploadOutlined,
    SyncOutlined,
    FileExcelOutlined,
    DeleteOutlined,
    ReloadOutlined,
    EyeOutlined,
    AlertOutlined,
    LoadingOutlined,
    SaveOutlined
} from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { useGbStore } from '@/stores/gbStore';
import { useRoute } from 'vue-router';
import { message } from 'ant-design-vue';
import { Upload } from 'ant-design-vue';
import { storeToRefs } from 'pinia';
// Thêm vào phần import trên cùng
import { sanPhamService } from '@/services/sanPhamService';
// Định nghĩa các events
const emit = defineEmits(['refresh-data']);

const route = useRoute();
const store = useGbStore();
const visible = ref(false);
const value = ref('Hoạt động');


// Thêm biến để lưu số lượng sản phẩm dự kiến
const previewFilterCount = ref(0);
const isPreviewLoading = ref(false);

// Hàm xem trước kết quả lọc
const previewFilterResults = async () => {
    isPreviewLoading.value = true;

    try {
        // Lấy danh sách tên từ các ID được chọn (giống như trong filterProducts)
        const listDanhMuc = valueDanhMuc.value.length > 0
            ? valueDanhMuc.value.map(id => {
                const danhMuc = danhMucList.value.find(dm => dm.id_danh_muc === id);
                return danhMuc ? danhMuc.ten_danh_muc : '';
            }).filter(ten => ten !== '')
            : [];

        const listThuongHieu = valueThuongHieu.value.length > 0
            ? valueThuongHieu.value.map(id => {
                const thuongHieu = thuongHieuList.value.find(th => th.id_thuong_hieu === id);
                return thuongHieu ? thuongHieu.ten_thuong_hieu : '';
            }).filter(ten => ten !== '')
            : [];

        const listChatLieu = valueChatLieu.value.length > 0
            ? valueChatLieu.value.map(id => {
                const chatLieu = chatLieuList.value.find(cl => cl.id_chat_lieu === id);
                return chatLieu ? chatLieu.ten_chat_lieu : '';
            }).filter(ten => ten !== '')
            : [];

        const listMauSac = valueMauSac.value.length > 0
            ? valueMauSac.value.map(id => {
                const mauSac = mauSacList.value.find(ms => ms.id_mau_sac === id);
                return mauSac ? mauSac.ten_mau_sac : '';
            }).filter(ten => ten !== '')
            : [];

        const listKichThuoc = valueSize.value.length > 0
            ? valueSize.value.map(id => {
                const kichThuoc = kichThuocList.value.find(kt => kt.id_kich_thuoc === id);
                return kichThuoc ? kichThuoc.gia_tri : '';
            }).filter(giatri => giatri !== '')
            : [];

        const giaBanMin = value2.value[0];
        const giaBanMax = value2.value[1];

        // Lấy keyword hiện tại
        const currentKeyword = store.searchFilterParams?.keyword || '';

        // Gọi API để lấy kết quả xem trước (sử dụng hàm riêng)
        const response = await sanPhamService.locSanPhamVaChiTietSanPham(
            currentKeyword,
            giaBanMin,
            giaBanMax,
            listMauSac,
            listDanhMuc,
            listThuongHieu,
            listChatLieu,
            listKichThuoc
        );

        // Cập nhật số lượng sản phẩm dự kiến
        if (response) {
            if (Array.isArray(response)) {
                previewFilterCount.value = response.length;
            } else if (typeof response === 'object' && response.data && Array.isArray(response.data)) {
                previewFilterCount.value = response.data.length;
            } else {
                previewFilterCount.value = 0;
            }
        } else {
            previewFilterCount.value = 0;
        }

    } catch (error) {
        console.error('Lỗi khi xem trước kết quả lọc:', error);
        previewFilterCount.value = 0;
    } finally {
        isPreviewLoading.value = false;
    }
};



// Function to update store.checkRouter based on the current route
const updateCheckRouter = () => {
    store.getPath(route.path);
    store.getRoutePresent(route.path);
    console.log('Current path:', route.path);
    console.log('Updated store.checkRouter:', store.checkRouter);
};

// Thêm hàm loadFilterData để tải lại dữ liệu cho bộ lọc
const loadFilterData = async () => {
    console.log('Đang tải lại dữ liệu cho bộ lọc...');
    try {
        await Promise.all([
            store.getAllDM(),   // Danh mục
            store.getAllTH(),   // Thương hiệu
            store.getAllCL(),   // Chất liệu
            store.getAllMS(),   // Màu sắc
            store.getAllKT(),   // Kích thước
        ]);
        console.log('Đã tải lại dữ liệu cho bộ lọc thành công');
        store.needFilterRefresh = false;
    } catch (error) {
        console.error('Lỗi khi tải lại dữ liệu cho bộ lọc:', error);
    }
};

// Thêm hàm để xử lý sự kiện filter-data-updated
const handleFilterDataUpdated = () => {
    console.log('Đã nhận sự kiện filter-data-updated');
    loadFilterData().then(() => {
        // Kiểm tra xem có thuộc tính mới để thêm vào bộ lọc không
        checkAndSelectNewAttributes();
    });
};

// Thêm hàm mới để kiểm tra và chọn các thuộc tính mới
const checkAndSelectNewAttributes = () => {
    // Lấy thuộc tính mới từ localStorage nếu có
    const lastAddedAttributes = localStorage.getItem('lastAddedAttributes');
    if (lastAddedAttributes) {
        try {
            const attributes = JSON.parse(lastAddedAttributes);
            console.log('Đã tìm thấy thuộc tính mới cần chọn:', attributes);

            // Thêm thuộc tính mới vào các lựa chọn
            if (attributes.danhMuc) {
                valueDanhMuc.value = [...valueDanhMuc.value, attributes.danhMuc];
            }
            if (attributes.thuongHieu) {
                valueThuongHieu.value = [...valueThuongHieu.value, attributes.thuongHieu];
            }
            if (attributes.chatLieu) {
                valueChatLieu.value = [...valueChatLieu.value, attributes.chatLieu];
            }
            if (attributes.mauSac) {
                valueMauSac.value = [...valueMauSac.value, attributes.mauSac];
            }
            if (attributes.kichThuoc) {
                valueSize.value = [...valueSize.value, attributes.kichThuoc];
            }

            // Xóa khỏi localStorage sau khi sử dụng
            localStorage.removeItem('lastAddedAttributes');

            console.log('Đã cập nhật các giá trị đã chọn trong bộ lọc:');
            console.log('- Danh mục:', valueDanhMuc.value);
            console.log('- Thương hiệu:', valueThuongHieu.value);
            console.log('- Chất liệu:', valueChatLieu.value);
            console.log('- Màu sắc:', valueMauSac.value);
            console.log('- Kích thước:', valueSize.value);
        } catch (error) {
            console.error('Lỗi khi xử lý thuộc tính mới:', error);
        }
    }
};

// Watch for route changes to handle browser back button
watch(() => route.path, (newPath) => {
    console.log('Route changed to:', newPath);
    updateCheckRouter();
}, { immediate: true });

// Watch cho needFilterRefresh để tải lại dữ liệu khi cần
watch(() => store.needFilterRefresh, (newValue) => {
    if (newValue) {
        console.log('Phát hiện needFilterRefresh = true, tải lại dữ liệu bộ lọc');
        loadFilterData();
    }
});

// Computed property để lấy giá bán lớn nhất
const maxPriceFromProducts = computed(() => {
    if (store.giaMax) {
        return store.giaMax; // Giá trị mặc định nếu chưa có dữ liệu
    }
    // Làm tròn đến hàng trăm nghìn gần nhất và thêm khoảng dư
    // return Math.ceil(store.giaMax / 100000) * 100000 + 100000;
});

// Sử dụng mảng để lưu nhiều giá trị
const valueDanhMuc = ref([]);
const valueThuongHieu = ref([]);
const valueChatLieu = ref([]);
const valueMauSac = ref([]);
const valueSize = ref([]);
const value2 = ref([0, 10000000]);

// Cập nhật value2 khi maxPriceFromProducts thay đổi
watch(() => maxPriceFromProducts.value, (newMaxPrice) => {
    value2.value = [0, newMaxPrice];
}, { immediate: true });

const xemTheo = ref('0');

// Sử dụng computed cho isFiltering để tự động cập nhật
const isFiltering = computed(() => {
    return Object.keys(store.filterCriteria).length > 0;
});


// Định nghĩa computed cho các danh sách dropdown
const danhMucList = computed(() => {
    return store.getAllDanhMuc || [];
});

const thuongHieuList = computed(() => {
    return store.getAllThuongHieu || [];
});

const chatLieuList = computed(() => {
    return store.getAllChatLieu || [];
});

const mauSacList = computed(() => {
    return store.getAllMauSac || [];
});

const kichThuocList = computed(() => {
    return store.getAllKichThuoc || [];
});

// Theo dõi thay đổi của store để cập nhật UI
watch(() => store.getFilteredProducts, (newValue) => {
    console.log('Số lượng sản phẩm đã lọc/tìm kiếm đã thay đổi:', newValue?.length);
}, { deep: true });

const listSort = ref([
    { value: '1', label: 'Sắp xếp theo' },
    { value: '2', label: 'Mã sản phẩm tăng dần' },
    { value: '3', label: 'Mã sản phẩm giảm dần' },
    { value: '4', label: 'Tên tăng dần' },
    { value: '5', label: 'Tên giảm dần' },
    { value: '6', label: 'Số lượng tăng dần' },
    { value: '7', label: 'Số lượng giảm dần' },
]);

// const listXemTheo = ref([
//     { value: '0', label: 'Tất cả sản phẩm' },
//     { value: '1', label: '5 sản phẩm' },
//     { value: '2', label: '10 sản phẩm' },
//     { value: '3', label: '15 sản phẩm' },
//     { value: '4', label: '20 sản phẩm' },
// ])
const luuBien = ref('1');
const openModalImportExcel = ref(false);
const fileList = ref([]);
const uploadLoading = ref(false);
const selectedFile = ref(null);
const importExcelModal = ref(false);
const importExcelData = ref([]);
const isDragging = ref(false);
const importError = ref('');
const importValidating = ref(false);
const dataPreviewMode = ref('all');

const showFilter = async () => {
    // Kiểm tra dữ liệu trước khi mở bộ lọc
    await checkAndLoadFilterData();
    visible.value = true;

    // Sau khi mở drawer, tính toán số lượng sản phẩm
    try {
        await previewFilterResults();
    } catch (error) {
        console.error('Lỗi khi tính toán số lượng sản phẩm:', error);
    }
};
const updatePreview = async () => {
    if (visible.value) {
        await previewFilterResults();
    }
};
const onClose = () => {
    visible.value = false;
};

const router = useRouter();
const changeRouter = (routers) => {
    // Make sure to update the store before navigation
    store.getPath(routers);
    store.getRoutePresent(route.path);
    store.getIndex(routers); // Make sure to call getIndex to update menu selection

    // Log for debugging
    console.log('Navigating to:', routers);
    console.log('Updated store.checkRouter:', store.checkRouter);

    // Navigate
    router.push(routers);
};

// Hàm kiểm tra và tải dữ liệu cho bộ lọc
const checkAndLoadFilterData = async () => {
    try {
        // Kiểm tra các danh sách cần thiết và tải nếu cần
        if (!store.getAllDanhMuc || store.getAllDanhMuc.length === 0) {
            console.log('Đang tải danh mục...');
            await store.getAllDM();
        }

        if (!store.getAllThuongHieu || store.getAllThuongHieu.length === 0) {
            console.log('Đang tải thương hiệu...');
            await store.getAllTH();
        }

        if (!store.getAllChatLieu || store.getAllChatLieu.length === 0) {
            console.log('Đang tải chất liệu...');
            await store.getAllCL();
        }

        if (!store.getAllMauSac || store.getAllMauSac.length === 0) {
            console.log('Đang tải màu sắc...');
            await store.getAllMS();
        }

        if (!store.getAllKichThuoc || store.getAllKichThuoc.length === 0) {
            console.log('Đang tải kích thước...');
            await store.getAllKT();
        }

        // Log số lượng dữ liệu đã tải
        console.log(`Dữ liệu lọc sẵn sàng:
            - Danh mục: ${store.getAllDanhMuc?.length || 0}
            - Thương hiệu: ${store.getAllThuongHieu?.length || 0}
            - Chất liệu: ${store.getAllChatLieu?.length || 0}
            - Màu sắc: ${store.getAllMauSac?.length || 0}
            - Kích thước: ${store.getAllKichThuoc?.length || 0}`);
    } catch (error) {
        console.error('Lỗi khi tải dữ liệu cho bộ lọc:', error);
        message.error('Không thể tải dữ liệu cho bộ lọc');
    }
};

// Tải dữ liệu ban đầu
onMounted(async () => {
    console.log('Menu Action component mounted');
    try {
        value2.value = [0, maxPriceFromProducts.value];
        // await store.getAllCTSP();
        await store.getAllDM();   // Thay thế loadDanhMuc
        await store.getAllTH();   // Thay thế loadThuongHieu
        await store.getAllCL();   // Thay thế loadChatLieu
        await store.getAllMS();   // Thay thế loadMauSac
        await store.getAllKT();   // Thay thế loadKichThuoc
        await store.getGiaMax();
        // Đăng ký sự kiện lắng nghe filter-data-updated
        window.addEventListener('filter-data-updated', handleFilterDataUpdated);

        // Thêm sự kiện lắng nghe cho search-filter-changed
        window.addEventListener('search-filter-changed', (event) => {
            console.log('Nhận sự kiện search-filter-changed:', event.detail);
            // Bạn có thể thêm xử lý nếu cần
        });

        // Kiểm tra nếu cần refresh bộ lọc
        if (store.needFilterRefresh) {
            loadFilterData();
        }
    } catch (error) {
        console.error('Lỗi khi tải dữ liệu ban đầu:', error);
        message.error('Có lỗi xảy ra khi tải dữ liệu. Vui lòng thử lại sau!');
    }
});

// Cập nhật onBeforeUnmount để remove event listener
onBeforeUnmount(() => {
    window.removeEventListener('filter-data-updated', handleFilterDataUpdated);
    window.removeEventListener('search-filter-changed', () => { });
});

const filterProducts = async () => {
    try {
        // Đã có sẵn kết quả dự đoán
        console.log('Áp dụng bộ lọc, dự kiến có', previewFilterCount.value, 'sản phẩm');

        // Các tham số đã được chuẩn bị trong hàm previewFilterResults
        // Thực hiện tương tự như trước nhưng không cần tính toán lại các tham số

        // Chuyển đổi ID sang tên
        // Lấy danh sách tên danh mục từ ID được chọn
        const listDanhMuc = valueDanhMuc.value.length > 0
            ? valueDanhMuc.value.map(id => {
                const danhMuc = danhMucList.value.find(dm => dm.id_danh_muc === id);
                return danhMuc ? danhMuc.ten_danh_muc : '';
            }).filter(ten => ten !== '')
            : [];

        // Các tham số khác tương tự như cũ
        const listThuongHieu = valueThuongHieu.value.length > 0
            ? valueThuongHieu.value.map(id => {
                const thuongHieu = thuongHieuList.value.find(th => th.id_thuong_hieu === id);
                return thuongHieu ? thuongHieu.ten_thuong_hieu : '';
            }).filter(ten => ten !== '')
            : [];

        const listChatLieu = valueChatLieu.value.length > 0
            ? valueChatLieu.value.map(id => {
                const chatLieu = chatLieuList.value.find(cl => cl.id_chat_lieu === id);
                return chatLieu ? chatLieu.ten_chat_lieu : '';
            }).filter(ten => ten !== '')
            : [];

        const listMauSac = valueMauSac.value.length > 0
            ? valueMauSac.value.map(id => {
                const mauSac = mauSacList.value.find(ms => ms.id_mau_sac === id);
                return mauSac ? mauSac.ten_mau_sac : '';
            }).filter(ten => ten !== '')
            : [];

        const listKichThuoc = valueSize.value.length > 0
            ? valueSize.value.map(id => {
                const kichThuoc = kichThuocList.value.find(kt => kt.id_kich_thuoc === id);
                return kichThuoc ? kichThuoc.gia_tri : '';
            }).filter(giatri => giatri !== '')
            : [];

        const giaBanMin = value2.value[0];
        const giaBanMax = value2.value[1];
        const keyword = store.searchFilterParams.keyword;
        // Cập nhật đối tượng tìm kiếm và lọc trong store
        store.updateSearchFilterParams({
            keyword,
            giaBanMin,
            giaBanMax,
            listMauSac,
            listDanhMuc,
            listThuongHieu,
            listChatLieu,
            listKichThuoc
        });

        // Áp dụng tìm kiếm và lọc
        await store.applySearchAndFilter();

        // Đóng drawer
        visible.value = false;

        // Hiển thị thông báo
        message.success(`Đã áp dụng bộ lọc, hiển thị ${previewFilterCount.value} sản phẩm`);
    } catch (error) {
        console.error('Lỗi khi lọc sản phẩm:', error);
        message.error('Có lỗi xảy ra khi lọc sản phẩm');
    }
};

// Hàm reset bộ lọc
const resetFilter = async () => {
    // Reset các giá trị lọc trong component
    valueDanhMuc.value = [];
    valueThuongHieu.value = [];
    valueChatLieu.value = [];
    valueMauSac.value = [];
    valueSize.value = [];
    value2.value = [0, store.giaMax];

    try {
        // Lưu keyword hiện tại
        const currentKeyword = store.searchFilterParams.keyword;

        // Reset tất cả tham số lọc nhưng giữ lại keyword
        store.updateSearchFilterParams({
            keyword: currentKeyword,
            giaBanMin: null,
            giaBanMax: store.giaMax,
            listMauSac: [],
            listDanhMuc: [],
            listThuongHieu: [],
            listChatLieu: [],
            listKichThuoc: []
        });
        // Đảm bảo reset filterCriteria trong store nếu updateSearchFilterParams không tự xóa
        store.filterCriteria = {};
        // Áp dụng tìm kiếm và lọc
        await store.applySearchAndFilter();
        await previewFilterResults();
        // Thông báo
        message.success('Đã reset bộ lọc');
    } catch (error) {
        console.error('Lỗi khi reset bộ lọc:', error);
        message.error('Có lỗi xảy ra khi reset bộ lọc');
    }
};


const afterOpenChange = bool => {
    console.log('open', bool);
};

const filterOption = (input, option) => {
    return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};

// const formatPrice = (value) => {
//     return new Intl.NumberFormat('vi-VN', {
//         style: 'currency',
//         currency: 'VND'
//     }).format(value);
// };

const handleRemove = (file) => {
    fileList.value = [];
    selectedFile.value = null;
    return true;
};

const handleFileChange = (info) => {
    fileList.value = [...info.fileList].slice(-1);

    const file = info.file;

    if (file.status === 'removed') {
        selectedFile.value = null;
    } else {
        // Store the file object correctly - prefer the raw file object if available
        selectedFile.value = file.originFileObj ? file : file;
        console.log('File selected:', selectedFile.value);
        console.log('File is Blob?', selectedFile.value instanceof Blob);
        console.log('File is File?', selectedFile.value instanceof File);

        // Check file type and size to ensure it's valid
        if (file.type && !file.type.includes('spreadsheet') && !file.type.includes('excel')) {
            message.warning('Vui lòng chọn file Excel (.xlsx hoặc .xls)');
        }
    }
};

// Thêm hàm mới để trigger file select
const fileInput = ref(null);

const triggerFileSelect = () => {
    fileInput.value.click();
};

const handleManualFileChange = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Kiểm tra file type
    const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
        file.type === 'application/vnd.ms-excel' ||
        file.name.endsWith('.xlsx') ||
        file.name.endsWith('.xls');

    if (!isExcel) {
        message.error('Vui lòng chỉ tải lên file Excel (.xlsx hoặc .xls)');
        event.target.value = ''; // Reset input
        return;
    }

    // Kiểm tra kích thước file (dưới 5MB)
    const isLt5M = file.size / 1024 / 1024 < 5;
    if (!isLt5M) {
        message.error('File phải nhỏ hơn 5MB!');
        event.target.value = ''; // Reset input
        return;
    }

    console.log('File được chọn thủ công:', file);
    console.log('File type:', file.type);
    console.log('File size:', file.size);

    selectedFile.value = file;
};

const handleImportExcel = async (event) => {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }

    // Nếu chưa có file, yêu cầu chọn file
    if (!selectedFile.value) {
        message.error('Vui lòng chọn file Excel!');
        return;
    }

    // Bắt đầu quá trình validation và preview
    validateAndPreview();
};

// Thêm hàm validateExcelData để kiểm tra dữ liệu ở frontend
const validateExcelData = (data) => {
    if (!data || !Array.isArray(data) || data.length === 0) {
        return {
            isValid: false,
            message: 'Không có dữ liệu trong file Excel',
            data: []
        };
    }

    // Chuẩn hóa dữ liệu
    const normalizedData = data.map(row => {
        // Tạo cấu trúc dữ liệu giống như backend trả về để dễ xử lý
        return {
            sanPham: {
                ten_san_pham: (row['Tên sản phẩm'] || '').toString().trim(),
                danhMuc: {
                    ten_danh_muc: (row['Danh mục'] || '').toString().trim()
                },
                thuongHieu: {
                    ten_thuong_hieu: (row['Thương hiệu'] || '').toString().trim()
                },
                chatLieu: {
                    ten_chat_lieu: (row['Chất liệu'] || '').toString().trim()
                }
            },
            gia_ban: parseFloat(row['Giá bán']) || 0,
            so_luong: parseInt(row['Số lượng']) || 0,
            mauSac: {
                ma_mau_sac: (row['Màu sắc'] || '').toString().trim(),
                ten_mau_sac: (row['Màu sắc'] || '').toString().trim()
            },
            kichThuoc: {
                gia_tri: String(row['Giá trị kích thước'] || '').trim().split(' ')[0] || '',
                don_vi: (row['Đơn vị'] || '').toString().trim()
            },
            // trang_thai: row['Trạng thái'] === 'Hoạt động' || row['Trạng thái'] === true
        };
    });

    // Kiểm tra dữ liệu có hợp lệ không
    let hasErrors = false;
    normalizedData.forEach(item => {
        if (!item.sanPham.ten_san_pham ||
            !item.sanPham.danhMuc.ten_danh_muc ||
            !item.sanPham.thuongHieu.ten_thuong_hieu ||
            !item.sanPham.chatLieu.ten_chat_lieu ||
            item.gia_ban <= 0 ||
            item.so_luong <= 0 ||
            !item.mauSac.ma_mau_sac ||
            !item.kichThuoc.gia_tri) {
            hasErrors = true;
        }
    });

    return {
        isValid: !hasErrors,
        message: hasErrors ? 'File Excel có dữ liệu không hợp lệ' : 'Dữ liệu hợp lệ',
        data: normalizedData
    };
};

// Cập nhật hàm validateAndPreview để sử dụng validate ở frontend
const validateAndPreview = async () => {
    if (!selectedFile.value) {
        message.error('Vui lòng chọn file Excel!');
        return;
    }

    importValidating.value = true;
    importError.value = '';

    try {
        // Đọc file Excel bằng XLSX
        const reader = new FileReader();

        reader.onload = (e) => {
            try {
                const data = e.target.result;
                const workbook = XLSX.read(data, { type: 'array' });
                const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
                const excelData = XLSX.utils.sheet_to_json(firstSheet, { header: 2 });

                console.log('Dữ liệu Excel đọc được:', excelData);

                // Validate dữ liệu ở frontend
                const validation = validateExcelData(excelData);

                if (validation.data.length > 0) {
                    importExcelData.value = validation.data;
                    importExcelModal.value = true;
                    openModalImportExcel.value = false;

                    // Hiển thị cảnh báo nếu có lỗi
                    if (!validation.isValid) {
                        message.warning('File Excel có một số dòng dữ liệu không hợp lệ');
                    }
                } else {
                    importError.value = 'Không tìm thấy dữ liệu trong file Excel hoặc định dạng không hợp lệ';
                }
            } catch (error) {
                console.error('Lỗi khi xử lý file Excel:', error);
                importError.value = 'Không thể đọc file Excel: ' + error.message;
            } finally {
                importValidating.value = false;
            }
        };

        reader.onerror = (error) => {
            console.error('Lỗi khi đọc file:', error);
            importError.value = 'Không thể đọc file Excel';
            importValidating.value = false;
        };

        reader.readAsArrayBuffer(selectedFile.value);
    } catch (error) {
        console.error('Lỗi khi validate Excel:', error);
        importError.value = 'Không thể đọc file Excel: ' + error.message;
        importValidating.value = false;
    }
};

// Cập nhật hàm saveExcelImport để chỉ gửi dữ liệu hợp lệ lên server
const saveExcelImport = async () => {
    uploadLoading.value = true;
    try {
        if (!store.saveExcelImport) {
            console.error('Store không có hàm saveExcelImport');
            message.error('Lỗi hệ thống: Không thể lưu dữ liệu!');
            return;
        }

        // Nếu có dòng không hợp lệ, chỉ lưu các dòng hợp lệ
        const dataToSave = countInvalidRows() > 0
            ? importExcelData.value.filter(item => !hasRowError(item))
            : importExcelData.value;

        // Log dữ liệu sẽ được lưu
        console.log('Dữ liệu sẽ được lưu vào CSDL:', dataToSave);
        console.log('Số lượng dòng dữ liệu hợp lệ:', dataToSave.length);

        // Log thông tin thêm về dữ liệu
        const categoryCounts = {};
        dataToSave.forEach(item => {
            const category = item.sanPham.danhMuc.ten_danh_muc;
            categoryCounts[category] = (categoryCounts[category] || 0) + 1;
        });
        console.log('Phân loại sản phẩm theo danh mục:', categoryCounts);

        if (dataToSave.length === 0) {
            message.error('Không có dữ liệu hợp lệ để lưu!');
            uploadLoading.value = false;
            return;
        }

        const result = await store.saveExcelImport(dataToSave);
        console.log('Kết quả lưu dữ liệu:', result);

        if (result) {
            message.success(`Đã lưu thành công ${dataToSave.length} sản phẩm!`);
            importExcelModal.value = false;

            // Cập nhật store
            console.log('Bắt đầu tải dữ liệu sản phẩm mới nhất...');
            await store.getAllSanPhamNgaySua();
            console.log('Dữ liệu sản phẩm mới nhất đã được tải:', store.getAllSanPham?.length || 0);
            store.justAddedProduct = true;

            // Cập nhật dữ liệu bộ lọc để bao gồm các giá trị mới
            message.loading({ content: 'Đang cập nhật dữ liệu bộ lọc...', key: 'updateFilter' });

            // Tải lại toàn bộ dữ liệu lọc
            await Promise.all([
                store.getAllDM(),   // Danh mục
                store.getAllTH(),   // Thương hiệu
                store.getAllCL(),   // Chất liệu
                store.getAllMS(),   // Màu sắc
                store.getAllKT(),   // Kích thước
            ]);

            console.log('Đã cập nhật dữ liệu bộ lọc sau khi import Excel');
            message.success({ content: 'Dữ liệu bộ lọc đã được cập nhật', key: 'updateFilter', duration: 2 });

            // Xóa cache sản phẩm nếu có
            try {
                localStorage.removeItem('cached_products');
                console.log('Đã xóa cache sản phẩm');
            } catch (e) {
                console.error('Lỗi khi xóa cache:', e);
            }

            // Emit event để thông báo component cha làm mới dữ liệu
            emit('refresh-data');

            // Chuyển hướng trang với setTimeout để đảm bảo dữ liệu đã được cập nhật
            setTimeout(() => {
                router.push('/admin/quanlysanpham');
            }, 300);
        }
    } catch (error) {
        console.error('Lỗi khi lưu dữ liệu:', error);
        message.error('Đã xảy ra lỗi khi lưu dữ liệu: ' + (error.response?.data?.message || error.message || ''));
    } finally {
        uploadLoading.value = false;
    }
};

// Thêm các biến và hàm cho tính năng xuất Excel
const exportModalVisible = ref(false);
const exportLoading = ref(false);
const exportSelection = ref('all'); // 'all', 'filtered', hoặc 'selected'
const selectAllFields = ref(false);
const exportFields = ref([
    { label: 'Mã sản phẩm', value: 'ma_san_pham', selected: true },
    { label: 'Tên sản phẩm', value: 'ten_san_pham', selected: true },
    { label: 'ID chi tiết sản phẩm', value: 'id_chi_tiet_san_pham', selected: true },
    { label: 'Danh mục', value: 'ten_danh_muc', selected: true },
    { label: 'Thương hiệu', value: 'ten_thuong_hieu', selected: true },
    { label: 'Chất liệu', value: 'ten_chat_lieu', selected: true },
    { label: 'Số lượng', value: 'tong_so_luong', selected: true },
    { label: 'Giá bán', value: 'gia_ban', selected: true },
    { label: 'Màu sắc', value: 'mau_sac', selected: true },
    { label: 'Kích thước', value: 'kich_thuoc', selected: true },
    { label: 'Trạng thái', value: 'trang_thai', selected: true },
    { label: 'QR Code', value: 'qr_code', selected: false },
    { label: 'Ngày tạo', value: 'ngay_tao', selected: false },
    { label: 'Ngày sửa', value: 'ngay_sua', selected: false },
    { label: 'Mô tả', value: 'mo_ta', selected: false }
]);

// Lấy danh sách sản phẩm được chọn từ component cha
const selectedRows = ref([]);
const hasSelectedProducts = computed(() => selectedRows.value.length > 0);

// Kiểm tra xem có thể xuất được không
const canExport = computed(() => {
    // Kiểm tra nếu có ít nhất 1 trường được chọn
    const hasSelectedFields = exportFields.value.some(field => field.selected);

    // Nếu chọn sản phẩm đã chọn, cần có ít nhất 1 sản phẩm được chọn
    const hasValidSelection = exportSelection.value !== 'selected' || hasSelectedProducts.value;

    return hasSelectedFields && hasValidSelection;
});

// Hàm hiển thị modal xuất Excel
const showExportModal = () => {
    // Kiểm tra và tải thêm dữ liệu chi tiết sản phẩm nếu cần
    if (!store.getAllChiTietSanPham || store.getAllChiTietSanPham.length === 0) {
        message.info('Đang tải dữ liệu chi tiết sản phẩm...');
        // store.getAllCTSP().then(() => {
        //     console.log('Đã tải: ', store.getAllChiTietSanPham?.length || 0, 'chi tiết sản phẩm');
        // });
    }

    // Hiển thị modal
    exportModalVisible.value = true;
};

// Thêm nút để kiểm tra trực tiếp
const exportTestExcel = () => {
    try {
        message.loading('Đang xuất dữ liệu mẫu...');

        // Tạo một số dữ liệu mẫu
        const mockData = [
            {
                'Mã sản phẩm': 'SP001',
                'Tên sản phẩm': 'Áo thể thao nam',
                'Danh mục': 'Áo thể thao',
                'Thương hiệu': 'Nike',
                'Chất liệu': 'Polyester',
                'Số lượng': 10,
                'Giá bán': 350000,
                'Màu sắc': 'Đỏ',
                'Kích thước': 'L'
            },
            {
                'Mã sản phẩm': 'SP002',
                'Tên sản phẩm': 'Quần thể thao nữ',
                'Danh mục': 'Quần thể thao',
                'Thương hiệu': 'Adidas',
                'Chất liệu': 'Cotton',
                'Số lượng': 15,
                'Giá bán': 450000,
                'Màu sắc': 'Đen',
                'Kích thước': 'M'
            }
        ];

        // Tạo workbook và worksheet
        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.json_to_sheet(mockData);

        // Thiết lập độ rộng cột
        const wscols = [
            { wch: 15 }, // Mã sản phẩm
            { wch: 25 }, // Tên sản phẩm
            { wch: 15 }, // Danh mục
            { wch: 15 }, // Thương hiệu
            { wch: 15 }, // Chất liệu
            { wch: 10 }, // Số lượng
            { wch: 15 }, // Giá bán
            { wch: 15 }, // Màu sắc
            { wch: 15 }  // Kích thước
        ];
        ws['!cols'] = wscols;

        // Thêm worksheet vào workbook
        XLSX.utils.book_append_sheet(wb, ws, "Dữ liệu mẫu");

        // Tạo tên file với timestamp hiện tại
        const fileName = `test-data-${new Date().toISOString().split('T')[0]}.xlsx`;

        // Xuất file Excel
        XLSX.writeFile(wb, fileName);

        message.success('Xuất Excel mẫu thành công!');
    } catch (error) {
        console.error('Lỗi khi xuất Excel mẫu:', error);
        message.error('Có lỗi xảy ra khi xuất Excel mẫu');
    }
};

// Sửa lại hàm xử lý xuất Excel
const handleExportExcel = async () => {
    try {
        exportLoading.value = true;
        message.loading({ content: 'Đang chuẩn bị dữ liệu...', key: 'export' });

        const selectedFields = exportFields.value
            .filter(field => field.selected)
            .map(field => field.value);

        let productsToExport = [];
        switch (exportSelection.value) {
            case 'all':
                productsToExport = await store.getAllSP();
                break;
            case 'filtered':
                productsToExport = store.filteredProductsData || [];
                break;
            case 'selected':
                productsToExport = selectedRows.value || [];
                break;
        }

        if (productsToExport.length === 0) {
            message.warning('Không có sản phẩm để xuất');
            return;
        }

        // Log để kiểm tra dữ liệu sản phẩm
        console.log('Sản phẩm để xuất:', productsToExport);

        // Lấy chi tiết sản phẩm
        const chiTietSanPhams = [];
        for (const product of productsToExport) {
            try {
                const ctsp = await store.getCTSPBySanPham(product.id_san_pham);
                
                console.log(`Chi tiết sản phẩm cho ${product.id_san_pham}:`, ctsp);

                // Xử lý nhiều trường hợp trả về
                if (ctsp) {
                    if (Array.isArray(ctsp)) {
                        chiTietSanPhams.push(...ctsp);
                    } else if (typeof ctsp === 'object') {
                        chiTietSanPhams.push(ctsp);
                    }
                }
            } catch (error) {
                console.error(`Lỗi khi lấy chi tiết sản phẩm ${product.id_san_pham}:`, error);
            }
        }

        console.log('Tổng số chi tiết sản phẩm:', chiTietSanPhams.length);

        // Chuẩn bị dữ liệu Excel
        const excelData = chiTietSanPhams.map(ct => {
            const row = {};
            selectedFields.forEach(field => {
                switch(field) {
                    case 'ma_san_pham':
                        row['Mã sản phẩm'] = ct.ma_san_pham || '';
                        break;
                    case 'ten_san_pham':
                        row['Tên sản phẩm'] = ct.ten_san_pham || '';
                        break;
                    case 'id_chi_tiet_san_pham':
                        row['ID chi tiết SP'] = ct.id_chi_tiet_san_pham || '';
                        break;
                    case 'ten_danh_muc':
                        row['Danh mục'] = ct.ten_danh_muc || '';
                        break;
                    case 'ten_thuong_hieu':
                        row['Thương hiệu'] = ct.ten_thuong_hieu || '';
                        break;
                    case 'ten_chat_lieu':
                        row['Chất liệu'] = ct.ten_chat_lieu || '';
                        break;
                    case 'tong_so_luong':
                        row['Số lượng'] = ct.so_luong || 0;
                        break;
                    case 'gia_ban':
                        row['Giá bán'] = ct.gia_ban || 0;
                        break;
                    case 'mau_sac':
                        row['Màu sắc'] = ct.ten_mau || '';
                        break;
                    case 'kich_thuoc':
                        row['Kích thước'] = ct.gia_tri ? `${ct.gia_tri} ${ct.don_vi || ''}`.trim() : '';
                        break;
                    case 'trang_thai':
                        row['Trạng thái'] = ct.trang_thai || '';
                        break;
                    case 'ngay_tao':
                        row['Ngày tạo'] = ct.ngay_tao || '';
                        break;
                    case 'ngay_sua':
                        row['Ngày sửa'] = ct.ngay_sua || '';
                        break;
                    case 'qr_code':
                        row['QR Code'] = ct.qr_code || '';
                        break;
                    case 'mo_ta':
                        row['Mô tả'] = ct.mo_ta || '';
                        break;
                }
            });
            return row;
        });

        console.log('Dữ liệu Excel:', excelData);

        // Xuất Excel
        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.json_to_sheet(excelData);
        
        // Điều chỉnh độ rộng cột
        const colWidths = [
            { wch: 15 },  // Mã sản phẩm
            { wch: 30 },  // Tên sản phẩm
            { wch: 15 },  // ID chi tiết SP
            { wch: 20 },  // Danh mục
            { wch: 20 },  // Thương hiệu
            { wch: 20 },  // Chất liệu
            { wch: 10 },  // Số lượng
            { wch: 15 },  // Giá bán
            { wch: 15 },  // Màu sắc
            { wch: 15 },  // Kích thước
            { wch: 15 },  // Trạng thái
            { wch: 15 },  // Ngày tạo
            { wch: 15 },  // Ngày sửa
            { wch: 20 },  // QR Code
            { wch: 30 }   // Mô tả
        ];
        ws['!cols'] = colWidths;

        XLSX.utils.book_append_sheet(wb, ws, "Chi tiết sản phẩm");
        
        const fileName = `chi-tiet-san-pham-${new Date().toISOString().split('T')[0]}.xlsx`;
        XLSX.writeFile(wb, fileName);

        message.success({ content: `Xuất thành công ${excelData.length} sản phẩm`, key: 'export' });
        exportModalVisible.value = false;
    } catch (error) {
        console.error('Lỗi xuất Excel:', error);
        message.error({ content: 'Xuất Excel thất bại: ' + error.message, key: 'export' });
    } finally {
        exportLoading.value = false;
    }
};

// Hàm chuẩn bị dữ liệu Excel từ chi tiết sản phẩm
const prepareDetailExcelData = (chiTietSanPhams, selectedFields, filteredProductIds = []) => {
    if (!chiTietSanPhams || chiTietSanPhams.length === 0) {
        console.error('Không có dữ liệu chi tiết sản phẩm để xuất');
        return [];
    }

    console.log('Chuẩn bị dữ liệu Excel từ', chiTietSanPhams.length, 'chi tiết sản phẩm');
    console.log('Mẫu chi tiết sản phẩm đầu tiên:', JSON.parse(JSON.stringify(chiTietSanPhams[0])));

    // Lọc chi tiết sản phẩm theo ID sản phẩm nếu có
    let filteredCTSPs = chiTietSanPhams;
    if (filteredProductIds.length > 0 && exportSelection.value !== 'all') {
        filteredCTSPs = chiTietSanPhams.filter(ct =>
            ct && filteredProductIds.includes(
                typeof ct.id_san_pham === 'number'
                    ? ct.id_san_pham
                    : ct.sanPham?.id_san_pham)
        );
        console.log('Sau khi lọc theo ID sản phẩm:', filteredCTSPs.length, 'chi tiết sản phẩm');
    }

    // Tạo dữ liệu Excel
    return filteredCTSPs.map(ct => {
        // Tạo đối tượng dữ liệu Excel trống
        const row = {};

        // Thêm thông tin từ chi tiết sản phẩm
        if (selectedFields.includes('ma_san_pham')) row['Mã sản phẩm'] = ct.ma_san_pham || '';
        if (selectedFields.includes('ten_san_pham')) row['Tên sản phẩm'] = ct.ten_san_pham || '';
        if (selectedFields.includes('id_chi_tiet_san_pham')) row['ID chi tiết SP'] = ct.id_chi_tiet_san_pham || '';

        // Thêm thông tin danh mục, thương hiệu, chất liệu
        if (selectedFields.includes('ten_danh_muc')) row['Danh mục'] = ct.ten_danh_muc || '';
        if (selectedFields.includes('ten_thuong_hieu')) row['Thương hiệu'] = ct.ten_thuong_hieu || '';
        if (selectedFields.includes('ten_chat_lieu')) row['Chất liệu'] = ct.ten_chat_lieu || '';

        // Thêm thông tin màu sắc và kích thước
        if (selectedFields.includes('mau_sac')) row['Màu sắc'] = ct.ten_mau || '';
        if (selectedFields.includes('kich_thuoc')) {
            const kichThuoc = ct.gia_tri || '';
            const donVi = ct.don_vi || '';
            row['Kích thước'] = kichThuoc + (donVi ? ' ' + donVi : '');
        }

        // Thêm thông tin số lượng và giá bán
        if (selectedFields.includes('tong_so_luong')) row['Số lượng'] = ct.so_luong || 0;
        if (selectedFields.includes('gia_ban')) row['Giá bán'] = ct.gia_ban || 0;

        // Thêm thông tin trạng thái và thời gian
        if (selectedFields.includes('trang_thai')) row['Trạng thái'] = ct.trang_thai || '';
        if (selectedFields.includes('ngay_tao')) row['Ngày tạo'] = ct.ngay_tao || '';
        if (selectedFields.includes('ngay_sua')) row['Ngày sửa'] = ct.ngay_sua || '';

        // Thêm thông tin QR và mô tả
        if (selectedFields.includes('qr_code')) row['QR Code'] = ct.qr_code || '';
        if (selectedFields.includes('mo_ta')) row['Mô tả'] = ct.mo_ta || '';

        return row;
    });
};

// Hàm thiết lập độ rộng cột
const setColumnWidths = (data) => {
    if (!data || data.length === 0) return [];

    const sample = data[0];
    return Object.keys(sample).map(key => {
        // Thiết lập độ rộng dựa trên loại trường
        if (key === 'Tên sản phẩm' || key === 'Mô tả') {
            return { wch: 40 }; // Rộng hơn cho text dài
        } else if (key === 'Màu sắc' || key === 'Kích thước') {
            return { wch: 20 }; // Trung bình cho danh sách
        } else {
            return { wch: 15 }; // Mặc định cho các trường còn lại
        }
    });
};

// Phương thức để nhận danh sách sản phẩm được chọn từ component cha
const updateSelectedRows = (rows) => {
    selectedRows.value = rows;
};

// Thêm phương thức để nhận thông tin phân trang ảo
const updateVirtualPaginationInfo = (paginationInfo) => {
    console.log('Đã nhận thông tin phân trang:', paginationInfo);
    if (!paginationInfo) return;

    // Lưu thông tin phân trang để sử dụng khi lọc
    const { currentPage, pageSize, total } = paginationInfo;

    // Cập nhật bộ lọc nếu cần
    // Đảm bảo khi lọc vẫn giữ được thông tin phân trang
    // và có thể quay lại đúng trang khi bỏ lọc
};

// Expose để component cha có thể gọi
defineExpose({
    checkAndLoadFilterData,
    updateSelectedRows,
    updateVirtualPaginationInfo
});

// Các hàm tiện ích
const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const formatPrice = (price) => {
    if (!price) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0
    }).format(price);
};

// Xử lý kéo thả file
const handleFileDrop = (event) => {
    isDragging.value = false;
    const files = event.dataTransfer.files;
    if (files.length > 0) {
        validateAndProcessFile(files[0]);
    }
};

// Kiểm tra và xử lý file
const validateAndProcessFile = (file) => {
    importError.value = '';

    // Kiểm tra file type
    const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
        file.type === 'application/vnd.ms-excel' ||
        file.name.endsWith('.xlsx') ||
        file.name.endsWith('.xls');

    if (!isExcel) {
        importError.value = 'Vui lòng chỉ tải lên file Excel (.xlsx hoặc .xls)';
        return;
    }

    // Kiểm tra kích thước file (dưới 5MB)
    const isLt5M = file.size / 1024 / 1024 < 5;
    if (!isLt5M) {
        importError.value = 'File phải nhỏ hơn 5MB!';
        return;
    }

    console.log('File hợp lệ:', file);
    console.log('File type:', file.type);
    console.log('File size:', file.size);

    selectedFile.value = file;

    // Reset input file để có thể chọn lại cùng một file
    if (fileInput.value) {
        fileInput.value.value = '';
    }
};

// Thay thế file hiện tại
const replaceFile = () => {
    triggerFileSelect();
};

// Xóa file đã chọn
const removeFile = () => {
    selectedFile.value = null;
    importError.value = '';
    if (fileInput.value) {
        fileInput.value.value = '';
    }
};

// Đóng modal import
const closeImportModal = () => {
    openModalImportExcel.value = false;
    selectedFile.value = null;
    importError.value = '';
    if (fileInput.value) {
        fileInput.value.value = '';
    }
};

// Đếm số dòng hợp lệ
const countValidRows = () => {
    if (!importExcelData.value || importExcelData.value.length === 0) return 0;

    return importExcelData.value.filter(item => !hasRowError(item)).length;
};

// Đếm số dòng không hợp lệ
const countInvalidRows = () => {
    if (!importExcelData.value || importExcelData.value.length === 0) return 0;

    return importExcelData.value.filter(item => hasRowError(item)).length;
};

// Kiểm tra xem dòng có lỗi không
const hasRowError = (item) => {
    if (!item) return true;

    return !item.sanPham.ten_san_pham ||
        item.gia_ban === 0 ||
        item.so_luong === 0 ||
        item.so_luong === null ||
        !item.sanPham.danhMuc.ten_danh_muc ||
        !item.sanPham.thuongHieu.ten_thuong_hieu ||
        !item.sanPham.chatLieu.ten_chat_lieu ||
        !item.mauSac.ma_mau_sac ||
        !item.kichThuoc.gia_tri;
};

// Lọc dữ liệu theo chế độ xem
const filteredImportData = computed(() => {
    if (!importExcelData.value || importExcelData.value.length === 0) return [];

    switch (dataPreviewMode.value) {
        case 'valid':
            return importExcelData.value.filter(item => !hasRowError(item));
        case 'invalid':
            return importExcelData.value.filter(item => hasRowError(item));
        case 'all':
        default:
            return importExcelData.value;
    }
});

// Tải template mẫu
const downloadTemplate = () => {
    try {
        // Định nghĩa các cột cho file mẫu
        const headers = [
            'Tên sản phẩm',
            'Danh mục',
            'Thương hiệu',
            'Chất liệu',
            'Giá bán',
            'Số lượng',
            'Màu sắc',
            'Giá trị kích thước',
            'Đơn vị'
        ];

        // Dữ liệu mẫu
        const sampleData = [
            {
                'Tên sản phẩm': 'Áo thun Cotton',
                'Danh mục': 'Áo thun',
                'Thương hiệu': 'Nike',
                'Chất liệu': 'Cotton',
                'Giá bán': 150000,
                'Số lượng': 100,
                'Màu sắc': 'Đen',
                'Giá trị kích thước': 'L',
                'Đơn vị': 'Size'
            },
            {
                'Tên sản phẩm': 'Quần Jean Nam',
                'Danh mục': 'Quần Jean',
                'Thương hiệu': 'Levi\'s',
                'Chất liệu': 'Jean',
                'Giá bán': 550000,
                'Số lượng': 50,
                'Màu sắc': 'Xanh dương',
                'Giá trị kích thước': '32',
                'Đơn vị': 'Size'
            }
        ];

        // Tạo workbook và worksheet
        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.json_to_sheet(sampleData, { header: headers });

        // Thiết lập độ rộng cột
        const wscols = [
            { wch: 30 }, // Tên sản phẩm
            { wch: 20 }, // Danh mục
            { wch: 20 }, // Thương hiệu
            { wch: 20 }, // Chất liệu
            { wch: 15 }, // Giá bán
            { wch: 10 }, // Số lượng
            { wch: 15 }, // Màu sắc
            { wch: 15 }, // Giá trị kích thước
            { wch: 10 }  // Đơn vị
        ];
        ws['!cols'] = wscols;

        // Thêm worksheet vào workbook
        XLSX.utils.book_append_sheet(wb, ws, "Mau_Nhap_San_Pham");

        // Xuất file
        XLSX.writeFile(wb, "mau_nhap_san_pham.xlsx");
        
        message.success('Đã tải xuống file mẫu thành công!');
    } catch (error) {
        console.error('Lỗi khi tạo file mẫu:', error);
        message.error('Có lỗi xảy ra khi tạo file mẫu');
    }
};

// Khi người dùng chọn/bỏ chọn tất cả các trường
const handleSelectAllFields = (e) => {
    const checked = e.target.checked;
    exportFields.value.forEach(field => {
        field.selected = checked;
    });
};

// Theo dõi thay đổi của từng trường để cập nhật selectAllFields
watch(() => [...exportFields.value.map(f => f.selected)], (newVal) => {
    selectAllFields.value = newVal.every(v => v === true);
}, { deep: true });

const disabledByRoles = computed(() => store.id_roles === 3);
// Watch for sorting option changes
watch(() => luuBien.value, (newValue) => {
    if (newValue !== '1') {
        console.log('Sorting option changed to:', newValue);
        // Dispatch a custom event that can be caught by the table component
        const sortEvent = new CustomEvent('sort-option-changed', {
            detail: {
                option: newValue
            }
        });
        window.dispatchEvent(sortEvent);
    }
});
</script>

<style scoped>
/* ===== WHITE & DEEP ORANGE BUTTON THEME ===== */

/* Primary buttons - Deep orange theme */
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

:deep(.ant-btn-primary i),
:deep(.ant-btn-primary .anticon) {
    color: white !important;
}

/* Filter buttons - White background with orange border */
.btn-filter,
.filter-btn-custom {
    color: #ff6600 !important; /* Deep orange text */
    background: white !important;
    border-color: #ff6600 !important;
    border-radius: 8px !important;
    box-shadow: 0 2px 8px rgba(255, 102, 0, 0.2) !important;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1) !important;
    font-weight: 500 !important;
}

.btn-filter:hover,
.filter-btn-custom:hover,
.btn-filter:focus,
.filter-btn-custom:focus {
    background: rgba(255, 102, 0, 0.1) !important;
    border-color: #ff6600 !important;
    color: #e55a00 !important;
    box-shadow: 0 4px 15px rgba(255, 102, 0, 0.3) !important;
    transform: translateY(-2px);
}

.btn-filter .icon-filler,
.filter-btn-custom .icon-filler,
.btn-filter .anticon,
.filter-btn-custom .anticon {
    color: #ff6600 !important; /* Deep orange icons */
    transition: all 0.3s ease !important;
}

.btn-filter:hover .icon-filler,
.btn-filter:hover .anticon,
.filter-btn-custom:hover .icon-filler,
.filter-btn-custom:hover .anticon {
    color: #e55a00 !important; /* Darker orange on hover */
    transform: scale(1.1);
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

:deep(.ant-btn-dangerous i),
:deep(.ant-btn-dangerous .anticon) {
    color: white !important;
}

/* Default buttons with orange accents */
:deep(.ant-btn-default:not(.btn-filter):not(.filter-btn-custom)),
:deep(.ant-btn) {
    background: white !important;
    border-color: #ff6600 !important;
    color: #ff6600 !important;
    box-shadow: 0 2px 8px rgba(255, 102, 0, 0.1) !important;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    border-radius: 8px !important;
    font-weight: 500;
}

:deep(.ant-btn-default:not(.btn-filter):not(.filter-btn-custom):hover),
:deep(.ant-btn-default:not(.btn-filter):not(.filter-btn-custom):focus),
:deep(.ant-btn:hover),
:deep(.ant-btn:focus) {
    background: rgba(255, 102, 0, 0.1) !important;
    border-color: #e55a00 !important;
    color: #e55a00 !important;
    box-shadow: 0 4px 15px rgba(255, 102, 0, 0.2) !important;
    transform: translateY(-2px);
}

:deep(.ant-btn-default i),
:deep(.ant-btn-default .anticon) {
    color: #ff6600 !important;
}

:deep(.ant-btn-default:hover i),
:deep(.ant-btn-default:hover .anticon),
:deep(.ant-btn:hover i),
:deep(.ant-btn:hover .anticon) {
    color: #e55a00 !important;
    transform: scale(1.1);
}

/* Add product button - Special primary button */
:deep(.ant-btn-primary:not(.ant-btn-dangerous)),
.ant-btn:not(.btn-filter):not(.filter-btn-custom):not(.ant-btn-dangerous) {
    background: #ff6600 !important;
    border-color: #ff6600 !important;
    color: white !important;
}

.ant-btn:not(.btn-filter):not(.filter-btn-custom):not(.ant-btn-dangerous):hover {
    background: #e55a00 !important;
    border-color: #e55a00 !important;
}

/* File action buttons in import */
.file-actions :deep(.ant-btn) {
    color: #ff6600 !important;
    border-color: #ff6600 !important;
}

.file-actions :deep(.ant-btn:hover) {
    color: #e55a00 !important;
    border-color: #e55a00 !important;
    background: rgba(255, 102, 0, 0.1) !important;
}

/* Modal footer buttons */
:deep(.ant-modal-footer .ant-btn) {
    border-radius: 8px !important;
    font-weight: 500 !important;
}

/* Custom select (if needed) - override existing styles */
:deep(.custom-select) {
    .ant-select-selector {
        border-color: #ff6600 !important;
        background-color: white !important;
        border-radius: 8px !important;
        color: #ff6600 !important;
        transition: all 0.3s ease !important;
    }

    .ant-select-arrow,
    .ant-select-clear,
    .ant-select-selection-search-icon {
        color: #ff6600 !important;
        transition: all 0.3s ease !important;
    }

    &:hover {
        .ant-select-selector {
            border-color: #e55a00 !important;
            background-color: rgba(255, 102, 0, 0.05) !important;
            color: #e55a00 !important;
        }

        .ant-select-arrow,
        .ant-select-clear,
        .ant-select-selection-search-icon {
            color: #e55a00 !important;
        }
    }

    &.ant-select-focused .ant-select-selector,
    &.ant-select-open .ant-select-selector {
        border-color: #ff6600 !important;
        background-color: white !important;
        color: #ff6600 !important;
        box-shadow: 0 0 0 2px rgba(255, 102, 0, 0.2) !important;
    }
}

/* Update existing icon-filler to maintain consistency */
.icon-filler {
    transition: all 0.3s ease !important;
}

.custom-class {
    z-index: 1000;
}

.loading-state {
    padding: 20px;
    text-align: center;
    color: #999;
}

.filter-group {
    margin-bottom: 24px;
}

.filter-title {
    font-weight: 600;
    margin-bottom: 8px;
}

.price-range {
    display: flex;
    justify-content: space-between;
    margin-top: 8px;
}

.filter-actions {
    margin-top: 32px;
}

.drawer-content {
    padding: 0 16px;
    height: 100%;
    overflow-y: auto;
}

/* Styles cho phần import Excel */
.upload-container {
    margin-bottom: 20px;
}

.upload-instructions {
    background-color: #f9f9f9;
    padding: 15px;
    border-radius: 6px;
    margin-bottom: 15px;
}

.upload-instructions h5 {
    font-weight: 600;
    margin-bottom: 10px;
}

.upload-instructions ol {
    padding-left: 20px;
    margin-bottom: 0;
}

.upload-instructions .template-link {
    color: #ff6600;
    text-decoration: underline;
    cursor: pointer;
    font-weight: 500;
}

.upload-area {
    border: 2px dashed #d9d9d9;
    border-radius: 6px;
    padding: 20px;
    text-align: center;
    transition: all 0.3s;
}

.upload-area.is-dragging {
    border-color: #ff6600;
    background-color: rgba(255, 102, 0, 0.05);
}

.upload-area.has-file {
    border-color: #52c41a;
    background-color: rgba(82, 196, 26, 0.05);
}

.upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 120px;
}

.upload-icon {
    font-size: 32px;
    color: #999;
    margin-bottom: 10px;
}

.selected-file {
    display: flex;
    align-items: center;
    padding: 10px;
    background-color: #fafafa;
    border-radius: 4px;
}

.file-icon {
    font-size: 28px;
    color: #52c41a;
    margin-right: 15px;
}

.file-info {
    flex-grow: 1;
    text-align: left;
}

.file-name {
    margin: 0;
    font-weight: 500;
}

.file-size {
    margin: 0;
    color: #888;
    font-size: 12px;
}

.file-actions {
    display: flex;
    gap: 5px;
}

.error-message {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #ff4d4f;
    padding: 8px 12px;
    background-color: #fff2f0;
    border: 1px solid #ffccc7;
    border-radius: 4px;
}

.validating-status {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #1890ff;
    padding: 8px 12px;
    background-color: #e6f7ff;
    border: 1px solid #91d5ff;
    border-radius: 4px;
}

/* Styles cho modal dữ liệu import */
.data-preview-stats {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
}

.stat-item {
    flex: 1;
    text-align: center;
    padding: 10px 15px;
    background-color: #f9f9f9;
    border-radius: 4px;
}

.stat-value {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
}

.stat-label {
    margin: 0;
    color: #666;
}

.text-warning .stat-value {
    color: #fa8c16;
}

.data-action-buttons {
    margin-bottom: 15px;
}

.table-container {
    max-height: 70vh;
    overflow-y: auto;
    overflow-x: auto;
}

.table {
    width: 100%;
    border-collapse: collapse;
}

.table th,
.table td {
    padding: 8px;
    text-align: left;
    white-space: nowrap;
}

.table th {
    position: sticky;
    top: 0;
    background-color: #f5f5f5;
    z-index: 1;
}

.table-bordered th,
.table-bordered td {
    border: 1px solid #dee2e6;
}

.table tr.has-error {
    background-color: #fff1f0;
}

.table td.cell-error {
    background-color: #ffccc7;
    color: #cf1322;
}

.import-summary {
    margin-top: 15px;
}

:deep(.custom-select) {
    .ant-select-selector {
        border-color: #ff6600 !important;
        background-color: #ff6600 !important;
        border-radius: 6px !important;
        color: white !important;
        transition: all 0.3s ease;
    }

    .ant-select-arrow,
    .ant-select-clear,
    .ant-select-selection-search-icon {
        color: #ff6600 !important;
        transition: all 0.3s ease;
        color: white !important;
    }

    &:hover {
        .ant-select-selector {
            border-color: #ff6600 !important;
            background-color: #ff6600 !important;
            color: white !important;
        }

        .ant-select-arrow,
        .ant-select-clear,
        .ant-select-selection-search-icon {
            color: white !important;
        }
    }

    &.ant-select-focused .ant-select-selector,
    &.ant-select-open .ant-select-selector {
        border-color: #ff6600 !important;
        background-color: #ff6600 !important;
        color: white !important;
        box-shadow: 0 0 0 2px rgba(255, 102, 0, 0.2) !important;
    }

    &.ant-select-focused,
    &.ant-select-open {

        .ant-select-arrow,
        .ant-select-clear,
        .ant-select-selection-search-icon {
            color: white !important;
        }
    }

    .ant-select-selection-placeholder,
    .ant-select-selection-item {
        color: inherit;
    }
}

:deep(.ant-select-dropdown) {
    .ant-select-item {
        transition: all 0.3s ease;
    }

    .ant-select-item-option-active {
        background-color: rgba(255, 102, 0, 0.1) !important;
    }

    .ant-select-item-option-selected {
        background-color: #ff6600 !important;
        color: white !important;
    }
}

.button-text {
    margin-left: 4px;
}

@media (max-width: 768px) {
    .button-text {
        display: none;
    }

    .btn-filter,
    .ant-btn {
        padding: 4px 8px;
        min-width: 32px;
        justify-content: center;
    }

    .custom-select {
        width: 100px !important;
    }
}

@media (max-width: 576px) {
    .custom-select {
        width: 80px !important;
    }

    .d-flex.gap-2 {
        gap: 0.25rem !important;
    }
}

.btn-filter,
.ant-btn {
    transition: all 0.3s ease;
}

.icon-filler {
    display: flex;
    align-items: center;
    justify-content: center;
}

/* Style cho export Excel */
.export-modal-content {
    max-height: 70vh;
    overflow-y: auto;
}

.field-selection {
    margin-top: 16px;
    margin-bottom: 16px;
}
</style>