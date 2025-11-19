<template>
    <div class="row">
        <div class="col-md-6 border-end">
            <h5>Thêm sản phẩm</h5>
            <a-form :model="formState" :label-col="labelCol" :wrapper-col="wrapperCol" layout="horizontal" ref="formRef"
                :rules="rules">
                <a-form-item label="Mã sản phẩm" name="ma_san_pham"
                    :rules="[{ required: true, message: 'Vui lòng nhập mã sản phẩm!' }]">
                    <a-input v-model:value="formState.ma_san_pham" readonly disabled />
                </a-form-item>
                <a-form-item label="Tên sản phẩm" name="ten_san_pham" :rules="[
                    { required: true, message: 'Vui lòng nhập tên sản phẩm!' },
                    { validator: validateProductName }
                ]">
                    <a-input v-model:value="formState.ten_san_pham" :maxLength="100" show-count />
                </a-form-item>

                <a-form-item label="Danh mục" name="id_danh_muc"
                    :rules="[{ required: true, message: 'Vui lòng chọn danh mục!' }]">
                    <div class="d-flex gap-2">
                        <!-- Input ẩn để form binding - KHÔNG DISABLE -->
                        <a-input 
                            v-model:value="formState.id_danh_muc" 
                            style="display: none;"
                        />
                        
                        <a-auto-complete
                            v-model:value="danhMucSearch"
                            placeholder="Nhập tên danh mục"
                            :options="filteredDanhMucOptions"
                            @select="onSelectDanhMuc"
                            @search="onSearchDanhMuc"
                            class="flex-grow-1"
                            :backfill="true"
                            dropdown-class-name="attribute-dropdown"
                        >
                            <template #option="{ value, label }">
                                <div class="option-item">
                                    {{ label }} (ID: {{ value }})
                                </div>
                            </template>
                        </a-auto-complete>
                        <a-button type="primary" @click="showAddDanhMucModal">
                            <plus-outlined />
                        </a-button>
                    </div>
                </a-form-item>

                <a-form-item label="Thương hiệu" name="id_thuong_hieu"
                    :rules="[{ required: true, message: 'Vui lòng chọn thương hiệu!' }]">
                    <div class="d-flex gap-2">
                        <!-- Input ẩn để form binding - KHÔNG DISABLE -->
                        <a-input 
                            v-model:value="formState.id_thuong_hieu" 
                            style="display: none;"
                        />
                        
                        <a-auto-complete
                            v-model:value="thuongHieuSearch"
                            placeholder="Nhập tên thương hiệu"
                            :options="filteredThuongHieuOptions"
                            @select="onSelectThuongHieu"
                            @search="onSearchThuongHieu"
                            class="flex-grow-1"
                            :backfill="true"
                            dropdown-class-name="attribute-dropdown"
                        >
                            <template #option="{ value, label }">
                                <div class="option-item">
                                    {{ label }} (ID: {{ value }})
                                </div>
                            </template>
                        </a-auto-complete>
                        <a-button type="primary" @click="showAddThuongHieuModal">
                            <plus-outlined />
                        </a-button>
                    </div>
                </a-form-item>

                <a-form-item label="Chất liệu" name="id_chat_lieu"
                    :rules="[{ required: true, message: 'Vui lòng chọn chất liệu!' }]">
                    <div class="d-flex gap-2">
                        <!-- Input ẩn để form binding - KHÔNG DISABLE -->
                        <a-input 
                            v-model:value="formState.id_chat_lieu" 
                            style="display: none;"
                        />
                        
                        <a-auto-complete
                            v-model:value="chatLieuSearch"
                            placeholder="Nhập tên chất liệu"
                            :options="filteredChatLieuOptions"
                            @select="onSelectChatLieu"
                            @search="onSearchChatLieu"
                            class="flex-grow-1"
                            :backfill="true"
                            dropdown-class-name="attribute-dropdown"
                        >
                            <template #option="{ value, label }">
                                <div class="option-item">
                                    {{ label }} (ID: {{ value }})
                                </div>
                            </template>
                        </a-auto-complete>
                        <a-button type="primary" @click="showAddChatLieuModal">
                            <plus-outlined />
                        </a-button>
                    </div>
                </a-form-item>

                <a-form-item label="Hình ảnh" name="hinh_anh">
                    <a-upload v-model:file-list="fileList" list-type="picture-card" :max-count="1"
                        :customRequest="handleProductImageUpload" @remove="handleProductImageRemove">
                        <div v-if="fileList.length < 1">
                            <plus-outlined />
                            <div style="margin-top: 8px">Upload</div>
                        </div>
                    </a-upload>
                </a-form-item>

                <a-form-item label="Mô tả sản phẩm" name="mo_ta">
                    <QuillEditor
                        v-model:content="formState.mo_ta"
                        contentType="html"
                        toolbar="full" 
                        theme="snow"
                        placeholder="Nhập mô tả sản phẩm..."
                        class="editor-container"
                    />
                </a-form-item>

                <a-form-item :wrapper-col="{ span: 14, offset: 4 }">
                    <a-button type="primary" html-type="submit" :loading="loading" @click="validateForm">
                        Xác nhận thông tin
                    </a-button>
                    <a-button style="margin-left: 10px" @click="resetForm()">
                        Làm mới
                    </a-button>
                </a-form-item>
            </a-form>
        </div>
        <div class="col-md-6">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h5>Biến thể sản phẩm</h5>
                <a-button v-if="isProductValidated" type="primary" @click="addVariantType"
                    class="d-flex align-items-center">
                    <plus-outlined />Thêm dạng biến thể
                </a-button>
                <span v-else class="text-muted">
                    Vui lòng xác nhận thông tin sản phẩm trước khi thêm biến thể
                </span>
            </div>

            <template v-if="isProductValidated">
                <div class="mb-4 p-3 border rounded bg-light">
                    <a-form-item label="Giá" name="gia_chung" :validate-status="formState.giaChungValidateStatus"
                        :help="formState.giaChungValidateMessage" style="margin-bottom: 0;">
                        <div class="d-flex align-items-center gap-2">
                            <a-switch v-model:checked="useCommonPrice" :checked-children="'Dùng giá chung'"
                                :un-checked-children="'Giá riêng'" @change="handlePriceChange" />
                            <div v-if="useCommonPrice" class="d-flex gap-2 w-100">
                                <a-input-number v-model:value="formState.gia_ban_chung" :min="1000" :max="100000000"
                                    :formatter="value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                                    :parser="value => value.replace(/\$\s?|(,*)/g, '')" style="width: 100%"
                                    placeholder="Giá bán chung" @change="value => validateGiaChung(value)"
                                    @blur="() => validateGiaChung(formState.gia_ban_chung, true)"
                                    @input="handleGiaChungInput" />
                            </div>
                        </div>
                    </a-form-item>
                </div>

                <div v-for="(variantType, typeIndex) in variantTypes" :key="typeIndex"
                    class="variant-type-item mb-4 p-3 border rounded">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h6>Dạng biến thể #{{ typeIndex + 1 }}</h6>
                        <a-button type="text" danger @click="removeVariantType(typeIndex)">
                            <delete-outlined />
                        </a-button>
                    </div>

                    <a-form class="form-bien-the" layout="vertical">
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <a-form-item label="Màu sắc"
                                    :rules="[{ required: true, message: 'Vui lòng chọn màu sắc!' }]">
                                    <div class="d-flex gap-2">
                                        <a-auto-complete
                                            v-model:value="variantType.mauSacSearch"
                                            placeholder="Nhập tên màu sắc"
                                            :options="getFilteredMauSacOptions(typeIndex)"
                                            @select="value => onSelectMauSac(value, typeIndex)"
                                            @search="text => onSearchMauSac(text, typeIndex)"
                                            class="flex-grow-1"
                                        >
                                            <template #option="{ value, label, color }">
                                                <div class="d-flex align-items-center">
                                                    <span class="color-preview" :style="{ backgroundColor: color }"></span>
                                                    {{ label }}
                                                </div>
                                            </template>
                                        </a-auto-complete>
                                        <a-button type="primary" @click="showAddMauSacModal">
                                            <plus-outlined />
                                        </a-button>
                                    </div>
                                </a-form-item>
                            </div>
                            <div class="col-md-6">
                                <a-form-item label="Kích thước"
                                    :rules="[{ required: true, message: 'Vui lòng chọn ít nhất một kích thước!' }]">
                                    <div class="d-flex gap-2">
                                        <a-select v-model:value="variantType.selectedSizes" mode="multiple"
                                            placeholder="Tìm và chọn kích thước (có thể chọn nhiều)..." class="flex-grow-1"
                                            @change="(values) => handleSizeChange(values, typeIndex)"
                                            show-search
                                            :filter-option="createAdvancedFilterOption()">
                                            <a-select-option v-for="size in sortedSizeList" :key="size.id_kich_thuoc"
                                                :value="size.id_kich_thuoc">
                                                {{ size.gia_tri }}{{ size.don_vi ? ' ' + size.don_vi : '' }}
                                            </a-select-option>
                                        </a-select>
                                        <a-button type="primary" @click="showAddKichThuocModal">
                                            <plus-outlined />
                                        </a-button>
                                        <a-button type="default" size="small" @click="showSizeGuideModal"
                                            class="size-guide-btn">
                                            <QuestionCircleOutlined />
                                        </a-button>
                                    </div>
                                </a-form-item>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <a-form-item label="Số lượng" :validateStatus="variantType.soLuongValidateStatus"
                                    :help="variantType.soLuongValidateMessage">
                                    <a-input-number v-model:value="variantType.so_luong" :min="1" :max="100000"
                                        style="width: 100%" @change="(value) => validateSoLuong(value, typeIndex)"
                                        @blur="() => validateSoLuong(variantType.so_luong, typeIndex, true)"
                                        @input="(value) => handleSoLuongInput(value, typeIndex)" />
                                </a-form-item>
                            </div>
                            <div class="col-md-6">
                                <a-form-item label="Giá sản phẩm" :validateStatus="variantType.giaBanValidateStatus"
                                    :help="variantType.giaBanValidateMessage">
                                    <a-input-number v-model:value="variantType.gia_ban" :min="1000" :max="999999999"
                                        :disabled="useCommonPrice"
                                        :formatter="value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                                        :parser="value => value.replace(/\$\s?|(,*)/g, '')" style="width: 100%"
                                        @change="(value) => validateGiaBan(value, typeIndex)"
                                        @blur="() => validateGiaBan(variantType.gia_ban, typeIndex, true)"
                                        @input="(value) => handleGiaBanInput(value, typeIndex)" />
                                </a-form-item>
                            </div>
                        </div>

                        <a-form-item label="Hình ảnh biến thể"
                            :rules="[{ required: true, message: 'Vui lòng chọn ít nhất 1 hình ảnh!' }]">
                            <div class="variant-images-container">
                                <!-- Hiển thị cảnh báo nếu chưa chọn màu -->
                                <a-alert 
                                    v-if="!variantType.id_mau_sac" 
                                    message="Vui lòng chọn màu sắc trước khi upload ảnh" 
                                    type="warning" 
                                    show-icon 
                                    class="mb-3"
                                />
                                
                                <a-upload v-model:file-list="variantType.fileList" list-type="picture-card"
                                    :max-count="5" :multiple="true"
                                    :disabled="!variantType.id_mau_sac"
                                    :before-upload="(file) => beforeUpload(file, variantType.fileList ? variantType.fileList.length : 0)"
                                    :customRequest="(options) => handleCustomRequest(options, typeIndex)"
                                    @change="(info) => handleVariantTypeImageChange(info, typeIndex)"
                                    @remove="(file) => handleRemoveImage(file, variantType, typeIndex)">
                                    <div v-if="!variantType.fileList || variantType.fileList.length < 5">
                                        <plus-outlined />
                                        <div style="margin-top: 8px">
                                            {{ variantType.id_mau_sac ? 'Upload' : 'Chọn màu trước' }}
                                        </div>
                                    </div>
                                    <template #itemRender="{ file, actions }">
                                        <div 
                                            class="image-item-wrapper" 
                                            :class="{ 'is-primary': file.uid === variantType.primaryImageUid }"
                                            @click="() => setPrimaryImage(file.uid, typeIndex)"
                                        >
                                            <img :src="file.url || file.thumbUrl" alt="variant" class="uploaded-image" />
                                            <div class="image-actions">
                                                <a-button 
                                                    type="text" 
                                                    danger 
                                                    size="small"
                                                    @click.stop="actions.remove"
                                                    class="remove-btn"
                                                >
                                                    <delete-outlined />
                                                </a-button>
                                            </div>
                                            <div v-if="file.uid === variantType.primaryImageUid" class="primary-badge">
                                                <CheckCircleFilled /> Ảnh chính
                                            </div>
                                            <div v-else class="hover-hint">
                                                Click để chọn làm ảnh chính
                                            </div>
                                        </div>
                                    </template>
                                </a-upload>
                                <div v-if="variantType.fileList && variantType.fileList.length > 1" class="image-note">
                                    <ExclamationCircleOutlined /> Click vào ảnh để chọn ảnh chính
                                </div>
                            </div>
                        </a-form-item>
                    </a-form>

                    <!-- Hiển thị danh sách biến thể đã tạo từ dạng biến thể này -->
                    <div v-if="variantType.selectedSizes && variantType.selectedSizes.length > 0 && variantType.fileList && variantType.fileList.length > 0" class="mt-3">
                        <h6 class="mb-2">Chọn ảnh cho từng biến thể:</h6>
                        <a-alert 
                            message="Hướng dẫn chọn ảnh" 
                            description="Chọn các ảnh cho từng biến thể. Mỗi ảnh chỉ có thể được sử dụng cho 1 biến thể duy nhất. Nếu chọn ít nhất 1 ảnh, bạn phải chọn 1 ảnh làm ảnh chính."
                            type="info" 
                            show-icon 
                            class="mb-3"
                        />
                        
                        <div class="variants-image-selection">
                            <div v-for="variant in getVariantsFromType(typeIndex)" 
                                 :key="getVariantKey(variant)" 
                                 class="variant-image-card mb-3 p-3 border rounded">
                                <div class="variant-info mb-2">
                                    <strong>{{ variant.mau_sac_name }} - {{ variant.kich_thuoc_name }}</strong>
                                    <span class="text-muted ms-2">({{ variant.selectedImages?.length || 0 }} ảnh đã chọn)</span>
                                    <span v-if="!variant.selectedImages || variant.selectedImages.length === 0" class="badge bg-secondary text-white ms-2">
                                        <ExclamationCircleOutlined /> Không có ảnh
                                    </span>
                                </div>
                                
                                <div class="image-selection-grid">
                                    <div v-for="image in variantType.fileList.filter(f => f.status === 'done')" 
                                         :key="image.uid"
                                         class="image-selection-item"
                                         :class="{ 
                                             'selected': variant.selectedImages?.includes(image.uid),
                                             'disabled': isImageUsed(image.uid, variant),
                                             'primary': variant.primaryImageUid === image.uid
                                         }"
                                    >
                                        <div class="image-wrapper" @click="() => toggleImageForVariant(variant, image.uid)">
                                            <img :src="image.url || image.thumbUrl" alt="variant image" />
                                            
                                            <!-- Checkbox để chọn/bỏ chọn ảnh -->
                                            <div class="image-checkbox">
                                                <a-checkbox 
                                                    :checked="variant.selectedImages?.includes(image.uid)"
                                                    :disabled="isImageUsed(image.uid, variant)"
                                                    @click.stop="() => toggleImageForVariant(variant, image.uid)"
                                                />
                                            </div>
                                            
                                            <!-- Badge hiển thị trạng thái -->
                                            <div v-if="isImageUsed(image.uid, variant)" class="image-badge used-badge">
                                                Đã dùng
                                            </div>
                                            <div v-else-if="variant.primaryImageUid === image.uid" class="image-badge primary-badge">
                                                <CheckCircleFilled /> Ảnh chính
                                            </div>
                                        </div>
                                        
                                        <!-- Radio button để chọn ảnh chính -->
                                        <div v-if="variant.selectedImages?.includes(image.uid)" class="primary-selector">
                                            <a-radio 
                                                :checked="variant.primaryImageUid === image.uid"
                                                @click="() => setPrimaryImageForVariant(variant, image.uid)"
                                            >
                                                Đặt làm ảnh chính
                                            </a-radio>
                                        </div>
                                    </div>
                                </div>
                                
                                <!-- Cảnh báo nếu chọn ảnh nhưng chưa chọn ảnh chính -->
                                <a-alert 
                                    v-if="variant.selectedImages?.length > 0 && !variant.primaryImageUid"
                                    message="Vui lòng chọn 1 ảnh làm ảnh chính"
                                    type="warning"
                                    show-icon
                                    class="mt-2"
                                />
                            </div>
                        </div>
                    </div>
                    
                    <!-- Hiển thị bảng biến thể đơn giản khi chưa có ảnh -->
                    <div v-else-if="variantType.selectedSizes && variantType.selectedSizes.length > 0" class="mt-3">
                        <h6 class="mb-2">Biến thể đã tạo:</h6>
                        <a-alert 
                            v-if="!variantType.fileList || variantType.fileList.length === 0"
                            message="Vui lòng upload ảnh cho màu này để chọn ảnh cho từng biến thể"
                            type="info"
                            show-icon
                            class="mb-3"
                        />
                        <a-table :dataSource="getVariantsFromType(typeIndex)" :columns="variantColumns" size="small"
                            :pagination="false" :rowKey="record => record.id_mau_sac + '-' + record.id_kich_thuoc">
                            <template #bodyCell="{ column, record }">
                                <template v-if="column.key === 'action'">
                                    <a-button type="text" danger
                                        @click="removeVariantByKeys(record.id_mau_sac, record.id_kich_thuoc)">
                                        <delete-outlined />
                                    </a-button>
                                </template>
                            </template>
                        </a-table>
                    </div>
                </div>

                <!-- Thêm nút lưu ở đây, bên ngoài tất cả các điều kiện v-if khác -->
                <div class="mt-4 text-center" v-if="isProductValidated && variantTypes.length > 0">
                    <a-button type="primary" size="large" html-type="submit" :loading="loading" @click="onFinish">
                        <PlusOutlined /> Lưu tất cả biến thể
                    </a-button>
                    <div class="mt-2 text-muted">
                        Tổng số biến thể: {{ variants.length }}
                    </div>
                </div>
            </template>

            <div v-else class="text-center p-4 border rounded">
                <a-empty description="Vui lòng xác nhận thông tin sản phẩm trước khi thêm biến thể">
                    <template #image>
                        <ExclamationCircleOutlined style="font-size: 48px; color: #faad14;" />
                    </template>
                </a-empty>
            </div>
        </div>
    </div>

    <a-modal :visible="previewVisible" :title="previewTitle" :footer="null" @cancel="handleCancel">
        <img alt="example" style="width: 100%" :src="previewImage" />
    </a-modal>

    <!-- Add modals for quick-add functionality -->
    <a-modal v-model:visible="danhMucModalVisible" title="Thêm danh mục mới" @ok="submitDanhMuc">
        <a-form layout="vertical" ref="danhMucFormRef" :model="newDanhMuc">
            <a-form-item label="Tên danh mục" name="ten_danh_muc" :rules="[
                { required: true, message: 'Vui lòng nhập tên danh mục!' },
                { validator: validateDanhMucName }
            ]">
                <a-input v-model:value="newDanhMuc.ten_danh_muc" :maxLength="50" show-count />
            </a-form-item>
        </a-form>
    </a-modal>

    <a-modal v-model:visible="thuongHieuModalVisible" title="Thêm thương hiệu mới" @ok="submitThuongHieu">
        <a-form layout="vertical" ref="thuongHieuFormRef" :model="newThuongHieu">
            <a-form-item label="Tên thương hiệu" name="ten_thuong_hieu" :rules="[
                { required: true, message: 'Vui lòng nhập tên thương hiệu!' },
                { validator: validateThuongHieuName }
            ]">
                <a-input v-model:value="newThuongHieu.ten_thuong_hieu" :maxLength="50" show-count />
            </a-form-item>
        </a-form>
    </a-modal>

    <a-modal v-model:visible="chatLieuModalVisible" title="Thêm chất liệu mới" @ok="submitChatLieu">
        <a-form layout="vertical" ref="chatLieuFormRef" :model="newChatLieu">
            <a-form-item label="Tên chất liệu" name="ten_chat_lieu" :rules="[
                { required: true, message: 'Vui lòng nhập tên chất liệu!' },
                { validator: validateChatLieuName }
            ]">
                <a-input v-model:value="newChatLieu.ten_chat_lieu" :maxLength="50" show-count />
            </a-form-item>
        </a-form>
    </a-modal>

    <a-modal v-model:visible="mauSacModalVisible" title="Thêm màu sắc mới" @ok="submitMauSac">
        <a-form layout="vertical" ref="mauSacFormRef" :model="newMauSac">
            <a-form-item label="Tên màu sắc" name="ten_mau_sac" :rules="[
                { required: true, message: 'Vui lòng nhập tên màu sắc!' },
                { validator: validateMauSacName }
            ]">
                <a-input v-model:value="newMauSac.ten_mau_sac" :maxLength="15" show-count />
            </a-form-item>
        </a-form>
    </a-modal>

    <a-modal v-model:visible="kichThuocModalVisible" title="Thêm kích thước mới" @ok="submitKichThuoc">
        <a-form layout="vertical" ref="kichThuocFormRef" :model="newKichThuoc">
            <a-form-item label="Giá trị" name="gia_tri" :rules="[
                { required: true, message: 'Vui lòng nhập giá trị kích thước!' },
                { validator: validateKichThuocValue }
            ]">
                <a-input v-model:value="newKichThuoc.gia_tri" :maxLength="5" show-count />
            </a-form-item>
            <a-form-item label="Đơn vị" name="don_vi">
                <a-input v-model:value="newKichThuoc.don_vi" :maxLength="5" show-count />
            </a-form-item>
        </a-form>
    </a-modal>

    <!-- Thêm modal hiển thị hướng dẫn chọn size -->
    <a-modal v-model:visible="sizeGuideModalVisible" title="Hướng dẫn chọn size" width="800px" footer={null}
        class="size-guide-modal">
        <a-tabs>
            <a-tab-pane key="1" tab="Size nữ">
                <div class="size-guide-content">
                    <h3 class="size-guide-title">Bảng size Uniqlo Nữ</h3>
                    <div class="size-tables">
                        <div class="table-container">
                            <table class="size-table">
                                <thead>
                                    <tr>
                                        <th rowspan="2">UNIQLO SIZE</th>
                                        <th colspan="2">Phần trên</th>
                                        <th colspan="3">Phần dưới</th>
                                    </tr>
                                    <tr>
                                        <th>NGỰC</th>
                                        <th>EO</th>
                                        <th>INCH</th>
                                        <th>US SIZE</th>
                                        <th>CM</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>XS</td>
                                        <td>31 1/2-33</td>
                                        <td>80-84</td>
                                        <td>24-25</td>
                                        <td>0-2</td>
                                        <td>61.0-63.5</td>
                                    </tr>
                                    <tr>
                                        <td>S</td>
                                        <td>33-34 3/4</td>
                                        <td>84-88</td>
                                        <td>26-27</td>
                                        <td>4-6</td>
                                        <td>66.0-68.5</td>
                                    </tr>
                                    <tr>
                                        <td>M</td>
                                        <td>35-36 1/2</td>
                                        <td>89-93</td>
                                        <td>28-29</td>
                                        <td>8-10</td>
                                        <td>71-73.5</td>
                                    </tr>
                                    <tr>
                                        <td>L</td>
                                        <td>37-38 1/2</td>
                                        <td>94-98</td>
                                        <td>30</td>
                                        <td>12</td>
                                        <td>76.0</td>
                                    </tr>
                                    <tr>
                                        <td>XL</td>
                                        <td>39-40 1/2</td>
                                        <td>99-103</td>
                                        <td>32</td>
                                        <td>14</td>
                                        <td>81.0</td>
                                    </tr>
                                    <tr>
                                        <td>XXL</td>
                                        <td>41-42 1/2</td>
                                        <td>104-108</td>
                                        <td>34</td>
                                        <td>16</td>
                                        <td>86.0</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>

                        <div class="table-container">
                            <h4 class="measurement-title">Hướng dẫn cách đo</h4>
                            <table class="size-table">
                                <thead>
                                    <tr>
                                        <th>SIZE</th>
                                        <th colspan="2">XXS</th>
                                        <th colspan="2">XS</th>
                                        <th colspan="2">S</th>
                                        <th colspan="2">M</th>
                                    </tr>
                                    <tr>
                                        <th></th>
                                        <th>inch</th>
                                        <th>cm</th>
                                        <th>inch</th>
                                        <th>cm</th>
                                        <th>inch</th>
                                        <th>cm</th>
                                        <th>inch</th>
                                        <th>cm</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>Chiều dài lưng</td>
                                        <td>21 1/4</td>
                                        <td>54</td>
                                        <td>21 2/3</td>
                                        <td>55</td>
                                        <td>22 4/9</td>
                                        <td>57</td>
                                        <td>23 2/9</td>
                                        <td>59</td>
                                    </tr>
                                    <tr>
                                        <td>Chiều rộng vai</td>
                                        <td>14</td>
                                        <td>35.5</td>
                                        <td>14 3/8</td>
                                        <td>36.5</td>
                                        <td>14 3/4</td>
                                        <td>37.5</td>
                                        <td>15 1/3</td>
                                        <td>39</td>
                                    </tr>
                                    <tr>
                                        <td>Chiều rộng cơ thể</td>
                                        <td>16 1/2</td>
                                        <td>42</td>
                                        <td>17 5/7</td>
                                        <td>45</td>
                                        <td>18 1/2</td>
                                        <td>47</td>
                                        <td>19 2/3</td>
                                        <td>50</td>
                                    </tr>
                                    <tr>
                                        <td>Chiều dài tay</td>
                                        <td>22 5/6</td>
                                        <td>58</td>
                                        <td>23 2/9</td>
                                        <td>59</td>
                                        <td>23 2/9</td>
                                        <td>59</td>
                                        <td>23 5/8</td>
                                        <td>60</td>
                                    </tr>
                                </tbody>
                            </table>

                            <table class="size-table mt-3">
                                <thead>
                                    <tr>
                                        <th>SIZE</th>
                                        <th colspan="2">L</th>
                                        <th colspan="2">XL</th>
                                        <th colspan="2">XXL</th>
                                    </tr>
                                    <tr>
                                        <th></th>
                                        <th>inch</th>
                                        <th>cm</th>
                                        <th>inch</th>
                                        <th>cm</th>
                                        <th>inch</th>
                                        <th>cm</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>Chiều dài lưng</td>
                                        <td>24</td>
                                        <td>61</td>
                                        <td>24 4/5</td>
                                        <td>63</td>
                                        <td>25 3/5</td>
                                        <td>65</td>
                                    </tr>
                                    <tr>
                                        <td>Chiều rộng vai</td>
                                        <td>16</td>
                                        <td>40.5</td>
                                        <td>16 1/3</td>
                                        <td>41.5</td>
                                        <td>16 3/4</td>
                                        <td>42.5</td>
                                    </tr>
                                    <tr>
                                        <td>Chiều rộng cơ thể</td>
                                        <td>20 6/7</td>
                                        <td>53</td>
                                        <td>22</td>
                                        <td>56</td>
                                        <td>23 2/9</td>
                                        <td>59</td>
                                    </tr>
                                    <tr>
                                        <td>Chiều dài tay</td>
                                        <td>23 5/8</td>
                                        <td>60</td>
                                        <td>23 5/8</td>
                                        <td>60</td>
                                        <td>23 5/8</td>
                                        <td>60</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </a-tab-pane>
            <a-tab-pane key="2" tab="Size nam">
                <div class="size-guide-content">
                    <h3 class="size-guide-title">Bảng size Uniqlo Nam</h3>
                    <div class="size-tables">
                        <div class="table-container">
                            <table class="size-table">
                                <thead>
                                    <tr>
                                        <th rowspan="2">UNIQLO SIZE</th>
                                        <th colspan="2">Phần trên</th>
                                        <th colspan="3">Phần dưới</th>
                                    </tr>
                                    <tr>
                                        <th>INCH</th>
                                        <th>CM</th>
                                        <th>INCH</th>
                                        <th>CM</th>
                                        <th>INCH</th>
                                        <th>CM</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>XS</td>
                                        <td>32-35</td>
                                        <td>81-89</td>
                                        <td>14 1/4</td>
                                        <td>36</td>
                                        <td>32</td>
                                        <td>81</td>
                                    </tr>
                                    <tr>
                                        <td>S</td>
                                        <td>36-38</td>
                                        <td>90-97</td>
                                        <td>15</td>
                                        <td>38</td>
                                        <td>33</td>
                                        <td>84</td>
                                    </tr>
                                    <tr>
                                        <td>M</td>
                                        <td>39-41</td>
                                        <td>97-104</td>
                                        <td>15 3/4</td>
                                        <td>40</td>
                                        <td>34</td>
                                        <td>86.5</td>
                                    </tr>
                                    <tr>
                                        <td>L</td>
                                        <td>41-44</td>
                                        <td>104-112</td>
                                        <td>16 1/2</td>
                                        <td>42</td>
                                        <td>35</td>
                                        <td>89</td>
                                    </tr>
                                    <tr>
                                        <td>XL</td>
                                        <td>44-47</td>
                                        <td>112-119</td>
                                        <td>17 1/4</td>
                                        <td>44</td>
                                        <td>35 3/4</td>
                                        <td>91</td>
                                    </tr>
                                    <tr>
                                        <td>XXL</td>
                                        <td>47-50</td>
                                        <td>119-127</td>
                                        <td>18 1/4</td>
                                        <td>46.5</td>
                                        <td>36 1/2</td>
                                        <td>92.5</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>

                        <div class="table-container">
                            <h4 class="measurement-title">Hướng dẫn cách đo</h4>
                            <table class="size-table">
                                <thead>
                                    <tr>
                                        <th>SIZE</th>
                                        <th colspan="2">XS</th>
                                        <th colspan="2">S</th>
                                        <th colspan="2">M</th>
                                    </tr>
                                    <tr>
                                        <th></th>
                                        <th>inch</th>
                                        <th>cm</th>
                                        <th>inch</th>
                                        <th>cm</th>
                                        <th>inch</th>
                                        <th>cm</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>Chiều dài lưng</td>
                                        <td>25 1/5</td>
                                        <td>64</td>
                                        <td>26 3/8</td>
                                        <td>67</td>
                                        <td>27 5/9</td>
                                        <td>70</td>
                                    </tr>
                                    <tr>
                                        <td>Chiều rộng vai</td>
                                        <td>15 3/4</td>
                                        <td>40</td>
                                        <td>16 1/3</td>
                                        <td>41.5</td>
                                        <td>17</td>
                                        <td>43</td>
                                    </tr>
                                    <tr>
                                        <td>Chiều rộng cơ thể</td>
                                        <td>18 1/2</td>
                                        <td>47</td>
                                        <td>19 2/3</td>
                                        <td>50</td>
                                        <td>20 6/7</td>
                                        <td>53</td>
                                    </tr>
                                    <tr>
                                        <td>Chiều dài tay</td>
                                        <td>7 1/2</td>
                                        <td>19</td>
                                        <td>7 7/8</td>
                                        <td>20</td>
                                        <td>8 1/2</td>
                                        <td>21.5</td>
                                    </tr>
                                    <tr>
                                        <td>Chiều rộng tay</td>
                                        <td>7 2/5</td>
                                        <td>19.5</td>
                                        <td>8</td>
                                        <td>20.5</td>
                                        <td>8 1/4</td>
                                        <td>21</td>
                                    </tr>
                                </tbody>
                            </table>

                            <table class="size-table mt-3">
                                <thead>
                                    <tr>
                                        <th>SIZE</th>
                                        <th colspan="2">L</th>
                                        <th colspan="2">XL</th>
                                    </tr>
                                    <tr>
                                        <th></th>
                                        <th>inch</th>
                                        <th>cm</th>
                                        <th>inch</th>
                                        <th>cm</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>Chiều dài lưng</td>
                                        <td>28 3/4</td>
                                        <td>73</td>
                                        <td>30</td>
                                        <td>76</td>
                                    </tr>
                                    <tr>
                                        <td>Chiều rộng vai</td>
                                        <td>17 5/7</td>
                                        <td>45</td>
                                        <td>18 1/2</td>
                                        <td>47</td>
                                    </tr>
                                    <tr>
                                        <td>Chiều rộng cơ thể</td>
                                        <td>22 4/9</td>
                                        <td>57</td>
                                        <td>24</td>
                                        <td>61</td>
                                    </tr>
                                    <tr>
                                        <td>Chiều dài tay</td>
                                        <td>8 6/7</td>
                                        <td>22.5</td>
                                        <td>8 6/7</td>
                                        <td>22.5</td>
                                    </tr>
                                    <tr>
                                        <td>Chiều rộng tay</td>
                                        <td>8 2/3</td>
                                        <td>22</td>
                                        <td>9 4/9</td>
                                        <td>24</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </a-tab-pane>
        </a-tabs>
    </a-modal>
</template>

<script setup>
import { ref, reactive, onMounted, watch, onBeforeUnmount, nextTick, computed } from 'vue';
import { PlusOutlined, DeleteOutlined, ExclamationCircleOutlined, BoldOutlined, ItalicOutlined, UnderlineOutlined, StrikethroughOutlined, DownOutlined, AlignLeftOutlined, AlignCenterOutlined, AlignRightOutlined, QuestionCircleOutlined, SearchOutlined, CheckCircleFilled } from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';
import { useGbStore } from '@/stores/gbStore';
import { useRouter } from 'vue-router';
import axios from 'axios';
import axiosInstance from '@/config/axiosConfig';
import { testService } from '@/services/testService';
import { QuillEditor } from '@vueup/vue-quill';
import '@vueup/vue-quill/dist/vue-quill.snow.css';

const store = useGbStore();
const router = useRouter();
const loading = ref(false);
const fileList = ref([]);
const productImageLoading = ref(false);

// Form layout
const labelCol = {
    style: {
        width: '120px',
    },
};
const wrapperCol = {
    span: 14,
};

// Form state
const useCommonPrice = ref(false);
const formState = reactive({
    ma_san_pham: '',
    ten_san_pham: '',
    mo_ta: '',
    hinh_anh: '',
    id_danh_muc: undefined,
    id_thuong_hieu: undefined,
    id_chat_lieu: undefined,
    trang_thai: true,
    gia_nhap_chung: 0,
    gia_ban_chung: 0,
    ngay_tao: new Date().toISOString(),
    ngay_cap_nhat: new Date().toISOString(),
    giaChungValidateStatus: '',
    giaChungValidateMessage: ''
});

// Lists for selects
const danhMucList = ref([]);
const thuongHieuList = ref([]);
const chatLieuList = ref([]);

// Thêm state cho biến thể
const variants = ref([]);
const mauSacList = ref([]); // Danh sách màu sắc từ API
const sizeList = ref([]); // Danh sách size từ API

// Thêm state cho dạng biến thể
const variantTypes = ref([]);

// Thêm state cho preview ảnh
const previewVisible = ref(false);
const previewImage = ref('');
const previewTitle = ref('');

// Map để tracking ảnh đã được sử dụng: { imageUid: { variantKey, isPrimary } }
const imageUsageMap = ref(new Map());

const formRef = ref(null);
const isProductValidated = ref(false);

// Thêm biến đánh dấu component còn mounted không
const mounted = ref(true);
const editorRef = ref(null);

onBeforeUnmount(() => {
    mounted.value = false;
});


const updateFormStateFromEditor = () => {
    if (editorRef.value) {
        formState.mo_ta = editorRef.value.innerHTML;
    }
};

// Cập nhật nội dung editor khi formState.mo_ta thay đổi từ bên ngoài (ví dụ từ a-typography-paragraph)
watch(() => formState.mo_ta, (newVal) => {
    if (editorRef.value && newVal !== editorRef.value.innerHTML) {
        editorRef.value.innerHTML = newVal;
    }
});

// Function to remove Vietnamese diacritics
const removeDiacritics = (str) => {
    if (!str) return '';

    return str.normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '') // Remove diacritics
        .replace(/đ/g, 'd').replace(/Đ/g, 'D'); // Replace Vietnamese 'd/D'
};

// Hàm chuẩn hóa chuỗi: xóa khoảng trắng đầu/cuối và thay khoảng trắng thừa giữa các từ bằng 1 khoảng trắng
const normalizeString = (str) => {
    if (!str) return '';

    // Xóa khoảng trắng ở đầu/cuối và thay nhiều khoảng trắng bằng một khoảng trắng
    return str.trim().replace(/\s+/g, ' ');
};

// Validator cho danh mục
const validateDanhMucName = (_, value) => {
    if (!value) return Promise.resolve();

    // Chuẩn hóa đầu vào - loại bỏ khoảng trắng thừa
    const normalizedValue = normalizeString(value);
    const normalizedInput = removeDiacritics(normalizedValue.toLowerCase());

    // Kiểm tra trùng lặp trên danh sách kết hợp (API + local)
    const existingItem = [...danhMucList.value, ...newLocalAttributes.danhMuc]
        .find(item => {
            const normalizedName = removeDiacritics(normalizeString(item.ten_danh_muc).toLowerCase());
            return normalizedName === normalizedInput;
        });

    if (existingItem) {
        if (existingItem.trang_thai === 'Không hoạt động') {
            return Promise.reject('Danh mục này đã tồn tại nhưng đang ở trạng thái không hoạt động!');
        }
        return Promise.reject('Danh mục này đã tồn tại!');
    }

    return Promise.resolve();
};

// Validator cho thương hiệu
const validateThuongHieuName = (_, value) => {
    if (!value) return Promise.resolve();

    // Chuẩn hóa đầu vào - loại bỏ khoảng trắng thừa
    const normalizedValue = normalizeString(value);
    const normalizedInput = removeDiacritics(normalizedValue.toLowerCase());

    // Kiểm tra trùng lặp trên danh sách kết hợp (API + local)
    const existingItem = [...thuongHieuList.value, ...newLocalAttributes.thuongHieu]
        .find(item => {
            const normalizedName = removeDiacritics(normalizeString(item.ten_thuong_hieu).toLowerCase());
            return normalizedName === normalizedInput;
        });

    if (existingItem) {
        if (existingItem.trang_thai === 'Không hoạt động') {
            return Promise.reject('Thương hiệu này đã tồn tại nhưng đang ở trạng thái không hoạt động!');
        }
        return Promise.reject('Thương hiệu này đã tồn tại!');
    }

    return Promise.resolve();
};

// Validator cho chất liệu
const validateChatLieuName = (_, value) => {
    if (!value) return Promise.resolve();

    // Chuẩn hóa đầu vào trước khi kiểm tra độ dài
    const normalizedValue = normalizeString(value);

    // Kiểm tra độ dài
    if (normalizedValue.length < 2) {
        return Promise.reject('Tên chất liệu phải có ít nhất 2 ký tự!');
    }

    if (normalizedValue.length > 50) {
        return Promise.reject('Tên chất liệu không được vượt quá 50 ký tự!');
    }

    // Kiểm tra chỉ chứa chữ cái, số, dấu cách và dấu gạch ngang
    if (!/^[a-zA-Z0-9À-ỹ\s\-]+$/.test(normalizedValue)) {
        return Promise.reject('Tên chất liệu chỉ được chứa chữ cái, số, dấu cách và dấu gạch ngang');
    }

    // Chuẩn hóa đầu vào - loại bỏ khoảng trắng thừa
    const normalizedInput = removeDiacritics(normalizedValue.toLowerCase());

    // Kiểm tra trùng lặp trên danh sách kết hợp (API + local)
    const existingItem = [...chatLieuList.value, ...newLocalAttributes.chatLieu]
        .find(item => {
            const normalizedName = removeDiacritics(normalizeString(item.ten_chat_lieu).toLowerCase());
            return normalizedName === normalizedInput;
        });

    if (existingItem) {
        if (existingItem.trang_thai === 'Không hoạt động') {
            return Promise.reject('Chất liệu này đã tồn tại nhưng đang ở trạng thái không hoạt động!');
        }
        return Promise.reject('Chất liệu này đã tồn tại!');
    }

    return Promise.resolve();
};

// Validator cho màu sắc
const validateMauSacName = (_, value) => {
    if (!value) return Promise.resolve();

    // Chuẩn hóa đầu vào - loại bỏ khoảng trắng thừa
    const normalizedValue = normalizeString(value);
    const normalizedInput = removeDiacritics(normalizedValue.toLowerCase());

    // Kiểm tra trùng lặp trên danh sách kết hợp (API + local)
    const existingItem = [...mauSacList.value, ...newLocalAttributes.mauSac]
        .find(item => {
            const normalizedName = removeDiacritics(normalizeString(item.ten_mau_sac).toLowerCase());
            return normalizedName === normalizedInput;
        });

    if (existingItem) {
        if (existingItem.trang_thai === 'Không hoạt động') {
            return Promise.reject('Màu sắc này đã tồn tại nhưng đang ở trạng thái không hoạt động!');
        }
        return Promise.reject('Màu sắc này đã tồn tại!');
    }

    return Promise.resolve();
};

// Validator cho kích thước
const validateKichThuocValue = (_, value) => {
    if (!value) return Promise.resolve();

    // Lấy giá trị đơn vị hiện tại và chuẩn hóa
    const donVi = newKichThuoc.don_vi ? normalizeString(newKichThuoc.don_vi) : '';

    // Chuẩn hóa đầu vào - loại bỏ khoảng trắng thừa
    const normalizedValue = normalizeString(value);
    const normalizedGiaTri = removeDiacritics(normalizedValue.toLowerCase());
    const normalizedDonVi = removeDiacritics(donVi.toLowerCase());

    // Kiểm tra trùng lặp trên danh sách kết hợp (API + local)
    const existingItem = [...sizeList.value, ...newLocalAttributes.kichThuoc]
        .find(item => {
            const itemGiaTri = removeDiacritics(normalizeString(item.gia_tri.toString()).toLowerCase());
            const itemDonVi = item.don_vi ? removeDiacritics(normalizeString(item.don_vi).toLowerCase()) : '';

            return itemGiaTri === normalizedGiaTri &&
                (normalizedDonVi ? itemDonVi === normalizedDonVi : !itemDonVi);
        });

    if (existingItem) {
        if (existingItem.trang_thai === 'Không hoạt động') {
            return Promise.reject('Kích thước này đã tồn tại nhưng đang ở trạng thái không hoạt động!');
        }
        return Promise.reject('Kích thước này đã tồn tại!');
    }

    return Promise.resolve();
};

// Định nghĩa rules cho form
const rules = {
    ten_san_pham: [
        { required: true, message: 'Vui lòng nhập tên sản phẩm!' }
    ],
    id_danh_muc: [
        { required: true, message: 'Vui lòng chọn danh mục!' }
    ],
    id_thuong_hieu: [
        { required: true, message: 'Vui lòng chọn thương hiệu!' }
    ],
    id_chat_lieu: [
        { required: true, message: 'Vui lòng chọn chất liệu!' }
    ],
    gia_ban_chung: [
        {
            validator: (_, value) => {
                if (useCommonPrice.value && (!value || value < 1000)) {
                    return Promise.reject('Giá bán phải lớn hơn 1000!');
                }
                if (useCommonPrice.value && (!value || value > 999999999)) {
                    return Promise.reject('Giá bán phải nhỏ hơn 999.999.999!');
                }
                return Promise.resolve();
            }
        }
    ],
    gia_chung: [
        {
            validator: (_, value) => {
                if (useCommonPrice.value) {
                    if (!formState.gia_ban_chung || formState.gia_ban_chung < 1000) {
                        return Promise.reject('Giá bán phải lớn hơn 1000!');
                    }
                    if (!formState.gia_ban_chung || formState.gia_ban_chung > 999999999) {
                        return Promise.reject('Giá bán phải nhỏ hơn 999.999.999')
                    }
                }
                return Promise.resolve();
            }
        }
    ]
};

// Validate tên sản phẩm
const validateProductName = async (rule, value) => {
    if (!value) return Promise.reject('Tên sản phẩm không được để trống');

    // Chuẩn hóa đầu vào - loại bỏ khoảng trắng thừa
    const normalizedValue = normalizeString(value);

    // Kiểm tra độ dài
    if (normalizedValue.length < 3) {
        return Promise.reject('Tên sản phẩm phải có ít nhất 3 ký tự!');
    }

    if (normalizedValue.length > 100) {
        return Promise.reject('Tên sản phẩm không được vượt quá 100 ký tự!');
    }

    // Kiểm tra nếu tên sản phẩm chỉ chứa số
    if (/^\d+$/.test(normalizedValue)) {
        return Promise.reject('Tên sản phẩm không được chỉ chứa số');
    }

    // Kiểm tra nếu tên sản phẩm chứa ký tự đặc biệt
    // Cho phép chữ cái, số, dấu cách, dấu gạch ngang, dấu chấm, dấu phẩy và dấu ngoặc
    if (!/^(?=.*[a-zA-ZÀ-ỹ])[a-zA-Z0-9À-ỹ\s\-\.,()/&+'"\[\]®™©℠$€£¥₫°²³%+×÷±µ=]+$/.test(normalizedValue)) {
        return Promise.reject('Tên sản phẩm phải chứa ít nhất một ký tự chữ cái và chỉ được chứa các ký tự cho phép');
    }

    // Chuẩn hóa tên sản phẩm - loại bỏ khoảng trắng thừa và dấu
    const normalizedInput = removeDiacritics(normalizedValue.toLowerCase());

    // Kiểm tra tên sản phẩm đã tồn tại trong danh sách sản phẩm
    const existingProduct = store.getAllSanPham.find(p => {
        const normalizedName = removeDiacritics(normalizeString(p.ten_san_pham).toLowerCase());
        return normalizedName === normalizedInput;
    });

    if (existingProduct) {
        return Promise.reject('Tên sản phẩm đã tồn tại trong danh sách sản phẩm');
    }

    return Promise.resolve();
};

const handleGiaChungInput = (value) => {
    // Chuyển sang dạng chuỗi để kiểm tra
    const stringValue = String(value).replace(/,/g, '');

    // Kiểm tra chỉ chứa số
    if (!/^\d*$/.test(stringValue)) {
        validateGiaChung(value);
    } else {
        // Nếu là số thì chuyển sang số và validate
        validateGiaChung(Number(stringValue));
    }
};

const validateGiaChung = (value, isBlur = false) => {
    if (!useCommonPrice.value) {
        formState.giaChungValidateStatus = '';
        formState.giaChungValidateMessage = '';
        return true;
    }

    // Nếu giá trị không được cung cấp
    if (value === undefined || value === null || value === '') {
        formState.giaChungValidateStatus = 'error';
        formState.giaChungValidateMessage = 'Vui lòng nhập giá chung';
        return false;
    }

    // Nếu giá trị không phải là số
    if (isNaN(value)) {
        formState.giaChungValidateStatus = 'error';
        formState.giaChungValidateMessage = 'Vui lòng chỉ nhập số';
        return false;
    }

    // Chuyển đổi sang số để so sánh
    const numValue = Number(value);

    // Nếu giá trị < 1000
    if (numValue < 1000) {
        formState.giaChungValidateStatus = 'error';
        formState.giaChungValidateMessage = 'Giá tối thiểu là 1,000đ';
        return false;
    }

    // Nếu giá trị quá lớn
    if (numValue > 100000000) {
        formState.giaChungValidateStatus = 'error';
        formState.giaChungValidateMessage = 'Giá không được vượt quá 100,000,000đ';
        return false;
    }

    // Nếu hợp lệ
    formState.giaChungValidateStatus = 'success';
    formState.giaChungValidateMessage = '';

    // Nếu giá hợp lệ, cập nhật giá cho tất cả các biến thể nếu đang sử dụng giá chung
    if (isBlur || (!isNaN(numValue) && numValue >= 1000 && numValue <= 100000000)) {
        if (useCommonPrice.value) {
            formState.gia_ban_chung = numValue;
            // Cập nhật giá cho tất cả biến thể
            variantTypes.value.forEach((type, index) => {
                if (type) {
                    type.gia_ban = numValue;
                    updateVariantsFromType(index);
                }
            });
        }
    }

    return true;
};

// Hàm validate form
const validateForm = async () => {
    try {
        console.log('Kiểm tra giá trị của formState trước khi validate:', {
            id_danh_muc: formState.id_danh_muc,
            id_thuong_hieu: formState.id_thuong_hieu,
            id_chat_lieu: formState.id_chat_lieu
        });
        
        // Kiểm tra trước xem các giá trị đã được gán đúng chưa
        if (!formState.id_danh_muc) {
            console.log('id_danh_muc chưa có giá trị, kiểm tra lại danhMucSearch:', danhMucSearch.value);
            // Nếu danhMucSearch có giá trị nhưng id_danh_muc chưa được gán, tìm kiếm giá trị tương ứng
            if (danhMucSearch.value) {
                const sourceList = combinedDanhMucList.value.length > 0 ? 
                    combinedDanhMucList.value : danhMucList.value;
                const item = sourceList.find(item => 
                    item.ten_danh_muc.toLowerCase() === danhMucSearch.value.toLowerCase());
                if (item) {
                    console.log('Tìm thấy danh mục tương ứng, gán giá trị id_danh_muc:', item.id_danh_muc);
                    formState.id_danh_muc = item.id_danh_muc;
                    // Tự động validate field này
                    if (formRef.value) formRef.value.validateFields(['id_danh_muc']);
                }
            }
        }
        
        if (!formState.id_thuong_hieu) {
            console.log('id_thuong_hieu chưa có giá trị, kiểm tra lại thuongHieuSearch:', thuongHieuSearch.value);
            // Nếu thuongHieuSearch có giá trị nhưng id_thuong_hieu chưa được gán, tìm kiếm giá trị tương ứng
            if (thuongHieuSearch.value) {
                const sourceList = combinedThuongHieuList.value.length > 0 ? 
                    combinedThuongHieuList.value : thuongHieuList.value;
                const item = sourceList.find(item => 
                    item.ten_thuong_hieu.toLowerCase() === thuongHieuSearch.value.toLowerCase());
                if (item) {
                    console.log('Tìm thấy thương hiệu tương ứng, gán giá trị id_thuong_hieu:', item.id_thuong_hieu);
                    formState.id_thuong_hieu = item.id_thuong_hieu;
                    // Tự động validate field này
                    if (formRef.value) formRef.value.validateFields(['id_thuong_hieu']);
                }
            }
        }
        
        if (!formState.id_chat_lieu) {
            console.log('id_chat_lieu chưa có giá trị, kiểm tra lại chatLieuSearch:', chatLieuSearch.value);
            // Nếu chatLieuSearch có giá trị nhưng id_chat_lieu chưa được gán, tìm kiếm giá trị tương ứng
            if (chatLieuSearch.value) {
                const sourceList = combinedChatLieuList.value.length > 0 ? 
                    combinedChatLieuList.value : chatLieuList.value;
                const item = sourceList.find(item => 
                    item.ten_chat_lieu.toLowerCase() === chatLieuSearch.value.toLowerCase());
                if (item) {
                    console.log('Tìm thấy chất liệu tương ứng, gán giá trị id_chat_lieu:', item.id_chat_lieu);
                    formState.id_chat_lieu = item.id_chat_lieu;
                    // Tự động validate field này
                    if (formRef.value) formRef.value.validateFields(['id_chat_lieu']);
                }
            }
        }
        
        // Kiểm tra lại sau khi đã gán các giá trị
        console.log('Giá trị của formState sau khi xử lý:', {
            id_danh_muc: formState.id_danh_muc,
            id_thuong_hieu: formState.id_thuong_hieu,
            id_chat_lieu: formState.id_chat_lieu
        });
        
        // Kiểm tra nếu đang dùng giá chung thì validate giá chung
        if (useCommonPrice.value) {
            const isGiaChungValid = validateGiaChung(formState.gia_ban_chung);
            if (!isGiaChungValid) {
                message.error('Vui lòng kiểm tra lại giá chung');
                return;
            }
        }

        // Validate form using formRef
        await formRef.value.validate();
        console.log('Form validated successfully');
        isProductValidated.value = true;
        console.log('isProductValidated set to true');
        message.success('Thông tin sản phẩm hợp lệ, bạn có thể thêm biến thể');
    } catch (errorInfo) {
        console.log('Validation failed:', errorInfo);
        isProductValidated.value = false;
        message.error('Vui lòng điền đầy đủ thông tin sản phẩm');
    }
};

// Thêm dạng biến thể mới (theo màu sắc)
const addVariantType = () => {
    const newVariantType = {
        id_mau_sac: null,
        selectedSizes: [],
        so_luong: 1, // Giá trị mặc định là 1
        gia_ban: useCommonPrice.value ? (formState.gia_ban_chung || 1000) : 1000, // Giá trị mặc định là 1000
        fileList: [], // Pool ảnh cho màu này (tối đa 3 ảnh)
        primaryImageUid: null, // Thêm thuộc tính để lưu UID của ảnh chính
        // Thêm các trường validate status và message
        soLuongValidateStatus: '',
        soLuongValidateMessage: '',
        giaBanValidateStatus: '',
        giaBanValidateMessage: '',
        // Thêm thuộc tính mauSacSearch để sử dụng cho auto-complete
        mauSacSearch: '',
        // Thêm cờ để biết đã tạo biến thể chưa
        variantsCreated: false
    };

    variantTypes.value.push(newVariantType);
    updateAvailableColors();

    // Validate số lượng và giá mặc định
    const typeIndex = variantTypes.value.length - 1;
    validateSoLuong(newVariantType.so_luong, typeIndex);
    validateGiaBan(newVariantType.gia_ban, typeIndex);
};

// Xóa dạng biến thể
const removeVariantType = (index) => {
    // Xóa các biến thể con liên quan đến dạng biến thể này
    const typeToRemove = variantTypes.value[index];
    if (typeToRemove.id_mau_sac) {
        variants.value = variants.value.filter(v => v.id_mau_sac !== typeToRemove.id_mau_sac);
    }

    // Xóa dạng biến thể
    variantTypes.value.splice(index, 1);

    // Cập nhật lại danh sách màu có sẵn
    updateAvailableColors();
};

//uploadẢnh
const beforeUpload = (file) => {
    const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
    if (!isJpgOrPng) {
        message.error('Bạn chỉ có thể tải lên file JPG/PNG!');
    }
    const isLt2M = file.size / 1024 / 1024 < 2;
    if (!isLt2M) {
        message.error('Hình ảnh phải nhỏ hơn 2MB!');
    }
    return isJpgOrPng && isLt2M;
};

// Thêm hàm handleCancel để đóng modal preview
const handleCancel = () => {
    previewVisible.value = false;
};

//upload ảnh
const uploadImage = async (file) => {
    if (!file) {
        console.warn('No file provided for upload');
        return null;
    }

    const CLOUD_NAME = 'dryt7bnjl';
    const UPLOAD_PRESET = 'vue_upload_image';
    const FOLDER_NAME = 'vue-ecom';
    const API_URL = `https://api.cloudinary.com/v1_1/${CLOUD_NAME}/image/upload`;

    console.log('Uploading file to Cloudinary:', file);
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', UPLOAD_PRESET);
    formData.append('folder', FOLDER_NAME);


    try {
        const response = await axios.post(API_URL, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
        console.log('Cloudinary upload response:', response.data);
        if (response.data && response.data.secure_url) {
            return response.data.secure_url; // Trả về URL an toàn của ảnh
        }
        return null;
    } catch (error) {
        console.error('Cloudinary upload error details:', error);
        const errorMessage = error.response?.data?.error?.message || error.message;
        message.error('Có lỗi khi upload ảnh lên Cloudinary: ' + errorMessage);
        return null;
    }
};

const handleCustomRequest = async ({ file, onSuccess, onError, onProgress }, typeIndex) => {
    try {
        const variantType = variantTypes.value[typeIndex];
        if (!variantType) {
            throw new Error('Không tìm thấy biến thể tương ứng');
        }

        console.log('=== UPLOAD ẢNH DEBUG ===');
        console.log('typeIndex:', typeIndex);
        console.log('variantType:', variantType);
        console.log('id_mau_sac:', variantType.id_mau_sac);

        // Kiểm tra xem đã chọn màu sắc chưa
        if (!variantType.id_mau_sac) {
            message.warning('Vui lòng chọn màu sắc trước khi upload ảnh!');
            onError(new Error('Chưa chọn màu sắc'));
            return;
        }

        // Set loading state cho file này
        imageLoadingStates.value.set(file.uid, true);

        // Khởi tạo list ảnh cho màu này nếu chưa có
        if (!variantImageLists.value.has(variantType.id_mau_sac)) {
            variantImageLists.value.set(variantType.id_mau_sac, []);
            console.log('Khởi tạo list ảnh mới cho màu:', variantType.id_mau_sac);
        }

        console.log('Bắt đầu upload file:', file.name);

        // Upload file lên cloud
        const responseUrl = await uploadImage(file);

        console.log('Upload xong, URL:', responseUrl);

        if (responseUrl) {
            // Thêm URL vào danh sách ảnh của màu sắc này
            const currentImages = variantImageLists.value.get(variantType.id_mau_sac) || [];
            currentImages.push(responseUrl);
            updateImagesForColor(variantType.id_mau_sac, currentImages);

            console.log('Đã thêm ảnh vào list, tổng số ảnh:', currentImages.length);
            console.log('variantImageLists:', Array.from(variantImageLists.value.entries()));

            // Cập nhật fileList trong variantType để hiển thị ảnh
            const fileInList = variantType.fileList.find(f => f.uid === file.uid);
            if (fileInList) {
                fileInList.status = 'done';
                fileInList.url = responseUrl;
                fileInList.response = responseUrl;
            }

            // Nếu đây là ảnh đầu tiên, tự động đặt làm ảnh chính
            if (variantType.fileList.filter(f => f.status === 'done').length === 1) {
                variantType.primaryImageUid = file.uid;
                console.log('Tự động đặt ảnh đầu tiên làm ảnh chính:', file.uid);
            }

            onSuccess(responseUrl);
            message.success('Tải ảnh lên thành công');
        } else {
            throw new Error('Không nhận được URL từ Cloudinary');
        }
    } catch (error) {
        console.error('Lỗi upload:', error);
        onError(error);
        message.error('Không thể tải lên hình ảnh: ' + error.message);
    } finally {
        imageLoadingStates.value.delete(file.uid);
    }
};

// Hàm xử lý xóa ảnh
const handleRemoveImage = async (file, variantType, typeIndex) => {
    Modal.confirm({
        title: 'Xác nhận xóa',
        content: 'Bạn có chắc chắn muốn xóa ảnh này không?',
        okText: 'Đồng ý',
        cancelText: 'Hủy',
        async onOk() {
            try {
                const loadingKey = 'deletingImage';
                message.loading({ content: 'Đang xóa ảnh...', key: loadingKey });

                const url = file.url || file.response;
                if (!url) throw new Error('Không tìm thấy URL của ảnh');

                // Lấy public_id từ URL, bao gồm cả thư mục
                const folderName = 'vue-ecom/';
                const startIndex = url.indexOf(folderName);
                if (startIndex === -1) {
                    throw new Error('URL không hợp lệ hoặc không chứa thư mục ảnh');
                }
                const publicIdWithFolder = url.substring(startIndex);
                const publicId = publicIdWithFolder.substring(0, publicIdWithFolder.lastIndexOf('.'));


                // Gọi API xóa ảnh
                await testService.deleteImage(publicId);

                // Cập nhật danh sách ảnh của màu sắc này
                const currentImages = variantImageLists.value.get(variantType.id_mau_sac) || [];
                const updatedImages = currentImages.filter(img => !img.includes(publicId));
                updateImagesForColor(variantType.id_mau_sac, updatedImages);

                // Cập nhật fileList của variantType
                variantType.fileList = variantType.fileList.filter(f => f.uid !== file.uid);

                // Nếu ảnh bị xóa là ảnh chính, tự động chọn ảnh đầu tiên còn lại làm ảnh chính
                if (variantType.primaryImageUid === file.uid) {
                    if (variantType.fileList.length > 0) {
                        variantType.primaryImageUid = variantType.fileList[0].uid;
                        console.log('Tự động chọn ảnh chính mới:', variantType.fileList[0].uid);
                    } else {
                        variantType.primaryImageUid = null;
                    }
                }

                message.success({ content: 'Đã xóa ảnh thành công', key: loadingKey });
            } catch (error) {
                console.error('Lỗi khi xóa ảnh:', error);
                message.error('Không thể xóa ảnh: ' + error.message);
            }
        }
    });
};

// Hàm đặt ảnh chính
const setPrimaryImage = (fileUid, typeIndex) => {
    const variantType = variantTypes.value[typeIndex];
    if (!variantType) return;
    
    variantType.primaryImageUid = fileUid;
    console.log('Đã đặt ảnh chính:', fileUid, 'cho biến thể:', typeIndex);
    message.success('Đã chọn ảnh chính');
};

// Cập nhật kích thước có sẵn khi thay đổi màu sắc
const updateAvailableSizes = (typeIndex) => {
    // Xóa các biến thể cũ của dạng biến thể này
    const currentType = variantTypes.value[typeIndex];
    if (currentType.id_mau_sac) {
        variants.value = variants.value.filter(v => v.id_mau_sac !== currentType.id_mau_sac);
    }

    // Reset danh sách kích thước đã chọn
    currentType.selectedSizes = [];

    // Cập nhật lại biến thể từ dạng biến thể
    updateVariantsFromType(typeIndex);

    // Cập nhật danh sách màu sắc khả dụng cho các dạng biến thể khác
    // Không cần gọi updateAvailableColors() vì nó chỉ cập nhật availableColors.value
    // mà chúng ta đã thay đổi để sử dụng getAvailableColorsForVariant(typeIndex)
};

// Xử lý khi thay đổi kích thước
const handleSizeChange = (selectedSizes, typeIndex) => {
    const type = variantTypes.value[typeIndex];

    // Lưu lại giá và số lượng hiện tại của type
    const currentPrice = type.gia_ban;
    const currentQuantity = type.so_luong;

    // Cập nhật danh sách kích thước đã chọn
    type.selectedSizes = selectedSizes;

    // Đảm bảo tất cả biến thể mới tạo đều có cùng giá và số lượng
    updateVariantsFromType(typeIndex);
};

// Sửa hàm cập nhật tất cả biến thể cùng màu sắc để không bao giờ đặt giá null
const updateAllVariantsWithSameColor = (colorId, price, quantity) => {
    if (!colorId) return; // Không làm gì nếu không có ID màu sắc

    // Cập nhật giá cho tất cả biến thể cùng màu sắc
    variants.value.forEach(variant => {
        if (variant.id_mau_sac === colorId) {
            // Đảm bảo không đặt giá trị null
            if (price !== undefined && price !== null) variant.gia_ban = price;
            if (quantity !== undefined && quantity !== null) variant.so_luong = quantity;
        }
    });

    // Cập nhật giá và số lượng cho tất cả dạng biến thể cùng màu sắc
    variantTypes.value.forEach(type => {
        if (type.id_mau_sac === colorId) {
            // Đảm bảo không đặt giá trị null
            if (price !== undefined && price !== null) type.gia_ban = price;
            if (quantity !== undefined && quantity !== null) type.so_luong = quantity;
        }
    });
};

// Thêm hook để đồng bộ khi giá bán hoặc số lượng thay đổi trong variantType
watch(variantTypes, (newValue, oldValue) => {
    // Duyệt qua từng variantType
    newValue.forEach((type, index) => {
        // Kiểm tra variantType cũ có tồn tại không
        if (oldValue && oldValue[index]) {
            const oldType = oldValue[index];

            // Nếu giá bán thay đổi
            if (type.gia_ban !== oldType.gia_ban) {
                // Cập nhật giá cho tất cả biến thể cùng màu sắc
                updateAllVariantsWithSameColor(type.id_mau_sac, type.gia_ban, undefined);
            }

            // Nếu số lượng thay đổi
            if (type.so_luong !== oldType.so_luong) {
                // Cập nhật số lượng cho tất cả biến thể cùng màu sắc
                updateAllVariantsWithSameColor(type.id_mau_sac, undefined, type.so_luong);
            }
        }
    });
}, { deep: true });

// Thêm một biến để theo dõi quá trình validate
const isValidating = ref(false);

// Sửa hàm updateVariantsFromType để đảm bảo không bao giờ có giá trị null
const updateVariantsFromType = (typeIndex) => {
    if (!mounted.value) return;

    const type = variantTypes.value[typeIndex];
    if (!type) return;

    // Validate số lượng và giá bán trước khi cập nhật nhưng sử dụng phiên bản an toàn
    const isSoLuongValid = validateSoLuongSafe(type.so_luong, typeIndex);
    const isGiaBanValid = validateGiaBanSafe(type.gia_ban, typeIndex);

    // Nếu không hợp lệ thì không cập nhật
    if (!isSoLuongValid || !isGiaBanValid) return;

    // Xóa các biến thể cũ của dạng biến thể này
    variants.value = variants.value.filter(v => v.id_mau_sac !== type.id_mau_sac);

    // Nếu chưa chọn màu hoặc chưa chọn kích thước nào thì không tạo biến thể
    if (!type.id_mau_sac || !type.selectedSizes || type.selectedSizes.length === 0) {
        console.log('No valid color or sizes selected');
        return;
    }

    // Tìm thông tin màu sắc trong danh sách kết hợp (API + local)
    const colorInfo = combinedMauSacList.value.find(c => c.id_mau_sac === type.id_mau_sac);
    if (!colorInfo) {
        console.error('Color not found:', type.id_mau_sac);
        return;
    }

    // Đảm bảo giá và số lượng có giá trị hợp lệ
    const safePrice = type.gia_ban !== null && type.gia_ban !== undefined ? type.gia_ban : 1000;
    const safeQuantity = type.so_luong !== null && type.so_luong !== undefined ? type.so_luong : 1;

    // Tạo biến thể mới cho mỗi kích thước đã chọn
    type.selectedSizes.forEach(sizeId => {
        const sizeInfo = combinedSizeList.value.find(s => s.id_kich_thuoc === sizeId);
        if (!sizeInfo) {
            console.error('Size not found:', sizeId);
            return;
        }

        // Tạo biến thể mới với giá trị an toàn
        variants.value.push({
            id_mau_sac: type.id_mau_sac,
            id_kich_thuoc: sizeId,
            mau_sac_name: colorInfo.ma_mau_sac + ' ' + colorInfo.ten_mau_sac,
            kich_thuoc_name: sizeInfo.gia_tri,
            so_luong: safeQuantity,
            gia_ban: safePrice,
            fileList: type.fileList ? [...type.fileList] : [],
            hinh_anh: [],
            selectedImages: [], // Mảng các UID ảnh đã chọn
            primaryImageUid: null // UID của ảnh chính
        });
    });

    // Thông báo cập nhật thành công
    console.log(`Đã cập nhật ${variants.value.filter(v => v.id_mau_sac === type.id_mau_sac).length} biến thể với màu ${colorInfo.ten_mau_sac}`);
    console.log('Total variants:', variants.value.length);
};

// Lấy danh sách biến thể từ một dạng biến thể
const getVariantsFromType = (typeIndex) => {
    const type = variantTypes.value[typeIndex];
    if (!type.id_mau_sac) {
        console.log(`No color selected for variant type #${typeIndex + 1}`);
        return [];
    }

    const result = variants.value.filter(v => v.id_mau_sac === type.id_mau_sac);
    console.log(`Found ${result.length} variants for type #${typeIndex + 1} with color ID ${type.id_mau_sac}`);
    return result;
};

// ============ CÁC HÀM XỬ LÝ CHỌN ẢNH CHO BIẾN THỂ ============

// Tạo key duy nhất cho variant
const getVariantKey = (variant) => {
    return `mau_${variant.id_mau_sac}_size_${variant.id_kich_thuoc}`;
};

// Toggle chọn/bỏ chọn ảnh cho biến thể
const toggleImageForVariant = (variant, imageUid) => {
    const variantKey = getVariantKey(variant);
    
    // Kiểm tra xem ảnh đã được chọn cho biến thể này chưa
    const imageIndex = variant.selectedImages.indexOf(imageUid);
    
    if (imageIndex > -1) {
        // Bỏ chọn ảnh
        variant.selectedImages.splice(imageIndex, 1);
        
        // Xóa khỏi imageUsageMap
        imageUsageMap.value.delete(imageUid);
        
        // Nếu đây là ảnh chính, reset primaryImageUid
        if (variant.primaryImageUid === imageUid) {
            variant.primaryImageUid = null;
            // Tự động chọn ảnh đầu tiên làm ảnh chính nếu còn ảnh
            if (variant.selectedImages.length > 0) {
                variant.primaryImageUid = variant.selectedImages[0];
            }
        }
    } else {
        // Kiểm tra ảnh đã được dùng cho biến thể khác chưa
        if (imageUsageMap.value.has(imageUid)) {
            const usage = imageUsageMap.value.get(imageUid);
            message.warning('Ảnh này đã được chọn cho biến thể khác!');
            return;
        }
        
        // Chọn ảnh
        variant.selectedImages.push(imageUid);
        
        // Thêm vào imageUsageMap
        imageUsageMap.value.set(imageUid, {
            variantKey: variantKey,
            isPrimary: false
        });
        
        // Nếu đây là ảnh đầu tiên, tự động đặt làm ảnh chính
        if (variant.selectedImages.length === 1) {
            variant.primaryImageUid = imageUid;
            imageUsageMap.value.set(imageUid, {
                variantKey: variantKey,
                isPrimary: true
            });
        }
    }
};

// Đặt ảnh chính cho biến thể
const setPrimaryImageForVariant = (variant, imageUid) => {
    const variantKey = getVariantKey(variant);
    
    // Kiểm tra ảnh có trong danh sách đã chọn không
    if (!variant.selectedImages.includes(imageUid)) {
        message.warning('Vui lòng chọn ảnh này trước khi đặt làm ảnh chính!');
        return;
    }
    
    // Cập nhật isPrimary trong imageUsageMap
    // Reset tất cả ảnh của biến thể này
    variant.selectedImages.forEach(uid => {
        const usage = imageUsageMap.value.get(uid);
        if (usage && usage.variantKey === variantKey) {
            imageUsageMap.value.set(uid, {
                ...usage,
                isPrimary: uid === imageUid
            });
        }
    });
    
    // Đặt ảnh chính
    variant.primaryImageUid = imageUid;
};

// Kiểm tra ảnh đã được sử dụng chưa
const isImageUsed = (imageUid, currentVariant) => {
    const currentKey = getVariantKey(currentVariant);
    
    if (!imageUsageMap.value.has(imageUid)) {
        return false;
    }
    
    const usage = imageUsageMap.value.get(imageUid);
    // Ảnh đã dùng cho biến thể khác
    return usage.variantKey !== currentKey;
};

// Lấy pool ảnh của màu cho biến thể
const getImagePoolForVariant = (variant, typeIndex) => {
    const variantType = variantTypes.value[typeIndex];
    if (!variantType || !variantType.fileList) {
        return [];
    }
    
    return variantType.fileList.filter(f => f.status === 'done');
};

// Xóa một biến thể cụ thể bằng id màu sắc và id kích thước
const removeVariantByKeys = (colorId, sizeId) => {
    // Tìm index của biến thể cần xóa
    const index = variants.value.findIndex(
        v => v.id_mau_sac === colorId && v.id_kich_thuoc === sizeId
    );

    if (index !== -1) {
        const variant = variants.value[index];
        const variantKey = getVariantKey(variant);
        
        // Xóa các ảnh đã chọn khỏi imageUsageMap
        if (variant.selectedImages && variant.selectedImages.length > 0) {
            variant.selectedImages.forEach(imageUid => {
                const usage = imageUsageMap.value.get(imageUid);
                if (usage && usage.variantKey === variantKey) {
                    imageUsageMap.value.delete(imageUid);
                }
            });
        }
        
        // Xóa biến thể
        variants.value.splice(index, 1);

        // Cập nhật lại danh sách kích thước đã chọn trong dạng biến thể
        const typeIndex = variantTypes.value.findIndex(vt => vt.id_mau_sac === colorId);
        if (typeIndex !== -1) {
            variantTypes.value[typeIndex].selectedSizes = variantTypes.value[typeIndex].selectedSizes.filter(
                id => id !== sizeId
            );
        }
    }
};

// Xử lý khi upload hình ảnh cho dạng biến thể
const handleVariantTypeImageChange = (info, typeIndex) => {
    if (info.file.status === 'uploading') {
        loading.value = true;
        return;
    }

    if (info.file.status === 'done') {
        loading.value = false;
        // Giới hạn số lượng file là 5 (đã update từ 3 lên 5)
        const limitedFileList = info.fileList.slice(0, 5);
        // Cập nhật fileList cho dạng biến thể
        variantTypes.value[typeIndex].fileList = limitedFileList;
        console.log('Variant type image files updated:', limitedFileList);

        // Nếu chỉ có 1 ảnh, tự động set làm ảnh chính
        if (limitedFileList.length === 1 && limitedFileList[0].status === 'done') {
            variantTypes.value[typeIndex].primaryImageUid = limitedFileList[0].uid;
            console.log('Tự động đặt ảnh duy nhất làm ảnh chính');
        }

        // Cập nhật biến thể - Gán fileList cho tất cả các biến thể thuộc dạng biến thể này
        const variants = getVariantsFromType(typeIndex);
        variants.forEach(variant => {
            variant.fileList = [...limitedFileList];
        });

        // Cập nhật biến thể
        updateVariantsFromType(typeIndex);
    }

    if (info.file.status === 'error') {
        loading.value = false;
        message.error(`${info.file.name} tải lên thất bại.`);
    }

    // Nếu xóa ảnh
    if (info.file.status === 'removed') {
        variantTypes.value[typeIndex].fileList = [...info.fileList];

        // Cập nhật biến thể - Gán fileList cho tất cả các biến thể thuộc dạng biến thể này
        const variants = getVariantsFromType(typeIndex);
        variants.forEach(variant => {
            variant.fileList = [...info.fileList];
        });
    }
};

// Cập nhật hàm resetForm để hỗ trợ dạng biến thể
const resetForm = () => {
    Object.keys(formState).forEach(key => {
        formState[key] = '';
    });
    fileList.value = [];
    variants.value = [];
    variantTypes.value = [];
    variantImageLists.value.clear(); // Xóa danh sách ảnh của các màu
    imageLoadingStates.value.clear(); // Xóa trạng thái loading
    isProductValidated.value = false;
    
    // Reset search fields
    danhMucSearch.value = '';
    thuongHieuSearch.value = '';
    chatLieuSearch.value = '';
    
    // Tạo lại mã sản phẩm mới
    formState.ma_san_pham = generateProductCode(store.getAllSanPham);
};

// Cập nhật watch cho giá chung để sử dụng nextTick
watch([() => formState.gia_nhap_chung, () => formState.gia_ban_chung, () => useCommonPrice.value],
    async ([newGiaNhap, newGiaBan, newUseCommon]) => {
        if (newUseCommon && mounted.value) {
            // Sử dụng nextTick để đảm bảo DOM đã được cập nhật trước khi thay đổi giá
            await nextTick();

            // Cập nhật giá cho tất cả dạng biến thể
            variantTypes.value.forEach((type, index) => {
                if (type) {
                    type.gia_nhap = newGiaNhap;
                    type.gia_ban = newGiaBan;
                    updateVariantsFromType(index);
                }
            });
        }
    }
);
watch(() => variants.value, (newVal) => {
    console.log('Chi tiết sản phẩm đã thay đổi:', newVal);
    console.table(newVal);
}, { deep: true });

// Thêm watch để in ra mảng dạng biến thể
watch(() => variantTypes.value, (newVal) => {
    console.log('Dạng biến thể đã thay đổi:', newVal);
    console.table(newVal);
}, { deep: true });

// Hàm tạo mã sản phẩm tự động
const generateProductCode = (products) => {
    if (!products || products.length === 0) {
        return 'SP001';
    }

    // Lọc ra các mã sản phẩm bắt đầu bằng 'SP' và lấy số lớn nhất
    const productCodes = products
        .map(p => p.ma_san_pham)
        .filter(code => code.startsWith('SP0'))
        .map(code => parseInt(code.substring(2)) || 0);

    const maxNumber = Math.max(...productCodes);
    const nextNumber = maxNumber + 1;

    // Format số với 3 chữ số (ví dụ: 001, 002, ...)
    return `SP0${String(nextNumber).padStart(2, '0')}`;
};

// Thêm hàm kiểm tra dữ liệu
const validateFormData = (data) => {
    if (!data.ten_san_pham) throw new Error('Tên sản phẩm không được để trống');
    if (!data.id_danh_muc) throw new Error('Vui lòng chọn danh mục');
    if (!data.id_thuong_hieu) throw new Error('Vui lòng chọn thương hiệu');
    if (!data.id_chat_lieu) throw new Error('Vui lòng chọn chất liệu');

    // Kiểm tra giá chung
    if (useCommonPrice.value) {
        if (!data.gia_ban_chung || data.gia_ban_chung < 1000) {
            throw new Error('Giá bán phải lớn hơn 1000!');
        }
    }

    // Kiểm tra số lượng và giá trong các biến thể
    if (variants.value.length > 0) {
        variants.value.forEach((variant, index) => {
            if (!variant.so_luong || variant.so_luong < 1) {
                throw new Error(`Số lượng của biến thể #${index + 1} phải lớn hơn 0!`);
            }
            if (!useCommonPrice.value) {
                if (!variant.gia_ban || variant.gia_ban < 1000) {
                    throw new Error(`Giá bán của biến thể #${index + 1} phải lớn hơn 1000!`);
                }
            }
        });
    }

    return true;
};

// Add new refs for storing local attributes that haven't been saved to DB yet
const newLocalAttributes = reactive({
    danhMuc: [],
    thuongHieu: [],
    chatLieu: [],
    mauSac: [],
    kichThuoc: []
});

// Create maps to store temp IDs for new attributes
const tempIdMaps = reactive({
    danhMuc: new Map(),
    thuongHieu: new Map(),
    chatLieu: new Map(),
    mauSac: new Map(),
    kichThuoc: new Map()
});

// Generate a temporary negative ID to identify unsaved items
const generateTempId = (prefix) => {
    return `temp_${prefix}_${Date.now()}_${Math.floor(Math.random() * 1000)}`;
};

// Create computed properties that combine API data with local data
const combinedDanhMucList = computed(() => {
    return [...danhMucList.value.filter(item => item.trang_thai === true), ...newLocalAttributes.danhMuc];
});

const combinedThuongHieuList = computed(() => {
    return [...thuongHieuList.value.filter(item => item.trang_thai === true), ...newLocalAttributes.thuongHieu];
});

const combinedChatLieuList = computed(() => {
    return [...chatLieuList.value.filter(item => item.trang_thai === true), ...newLocalAttributes.chatLieu];
});

const combinedMauSacList = computed(() => {
    return [...mauSacList.value.filter(item => item.trang_thai === true), ...newLocalAttributes.mauSac];
});

const combinedSizeList = computed(() => {
    return [...sizeList.value.filter(item => item.trang_thai === true), ...newLocalAttributes.kichThuoc];
});

// Sắp xếp kích thước theo thứ tự tăng dần
const sortedSizeList = computed(() => {
    return [...combinedSizeList.value].sort((a, b) => {
        // Nếu kích thước là số, sắp xếp theo giá trị số
        const numA = parseFloat(a.gia_tri);
        const numB = parseFloat(b.gia_tri);
        
        if (!isNaN(numA) && !isNaN(numB)) {
            return numA - numB;
        }
        
        // Nếu không phải số, sắp xếp theo chuỗi
        return a.gia_tri.localeCompare(b.gia_tri);
    });
});

// Modify to use combined lists
const availableColors = ref([]);
const variantColumns = ref([
    {
        title: 'Màu sắc',
        dataIndex: 'mau_sac_name',
        key: 'mau_sac_name',
    },
    {
        title: 'Kích thước',
        dataIndex: 'kich_thuoc_name',
        key: 'kich_thuoc_name',
    },
    {
        title: 'Số lượng',
        dataIndex: 'so_luong',
        key: 'so_luong',
    },
    {
        title: 'Giá bán',
        dataIndex: 'gia_ban',
        key: 'gia_ban',
        customRender: ({ text }) => {
            return text ? text.toLocaleString('vi-VN') + ' đ' : '0 đ';
        }
    },
    {
        title: 'Thao tác',
        key: 'action',
    }
]);

// Modify to use combined lists
const updateAvailableColors = () => {
    if (!mauSacList.value && newLocalAttributes.mauSac.length === 0) return;

    const usedColorIds = variantTypes.value
        .filter(vt => vt.id_mau_sac)
        .map(vt => vt.id_mau_sac);

    availableColors.value = combinedMauSacList.value.filter(color => !usedColorIds.includes(color.id_mau_sac));
};

// Thêm hàm mới để lấy danh sách màu sắc có sẵn cho từng dạng biến thể
const getAvailableColorsForVariant = (currentTypeIndex) => {
    if (!mauSacList.value && newLocalAttributes.mauSac.length === 0) return [];

    // Lấy danh sách ID màu sắc đã được sử dụng trong các dạng biến thể khác
    const usedColorIds = variantTypes.value
        .filter((vt, index) => vt.id_mau_sac && index !== currentTypeIndex)
        .map(vt => vt.id_mau_sac);

    // Lọc và trả về danh sách màu sắc chưa được sử dụng
    return combinedMauSacList.value.filter(color => !usedColorIds.includes(color.id_mau_sac));
};

// Replace the Add functions to add locally instead of calling APIs immediately
const handleAddDanhMuc = async () => {
    try {
        if (!newDanhMuc.ten_danh_muc) {
            message.error('Vui lòng nhập tên danh mục!');
            return;
        }

        // Xóa danh mục hiện tại trong newLocalAttributes và thay thế bằng danh mục mới
        newLocalAttributes.danhMuc = [];

        // Generate a temporary ID
        const tempId = generateTempId('dm');

        // Create a new local category
        const newCategory = {
            id_danh_muc: tempId,
            ten_danh_muc: newDanhMuc.ten_danh_muc.trim(),
            trang_thai: 'Hoạt động',
            _isNew: true // Mark as new to identify later
        };

        // Add to local list
        newLocalAttributes.danhMuc.push(newCategory);

        // Luôn gán giá trị cho form field - không cần điều kiện
        formState.id_danh_muc = tempId;

        // Lưu ID danh mục mới vào localStorage để component menuAction có thể chọn nó
        saveLastAddedAttribute('danhMuc', tempId, newCategory.ten_danh_muc);

        message.success('Đã thêm danh mục mới (sẽ lưu khi bạn lưu sản phẩm)');
        newDanhMuc.ten_danh_muc = '';
        danhMucModalVisible.value = false;
    } catch (error) {
        console.error('Lỗi khi thêm danh mục:', error);
        message.error('Có lỗi xảy ra khi thêm danh mục!');
    }
};

const handleAddThuongHieu = async () => {
    try {
        if (!newThuongHieu.ten_thuong_hieu) {
            message.error('Vui lòng nhập tên thương hiệu!');
            return;
        }

        // Xóa thương hiệu hiện tại trong newLocalAttributes và thay thế bằng thương hiệu mới
        newLocalAttributes.thuongHieu = [];

        // Generate a temporary ID
        const tempId = generateTempId('th');

        // Create a new local brand
        const newBrand = {
            id_thuong_hieu: tempId,
            ten_thuong_hieu: newThuongHieu.ten_thuong_hieu.trim(),
            trang_thai: 'Hoạt động',
            _isNew: true // Mark as new to identify later
        };

        // Add to local list
        newLocalAttributes.thuongHieu.push(newBrand);

        // Luôn gán giá trị cho form field - không cần điều kiện
        formState.id_thuong_hieu = tempId;

        // Lưu ID thương hiệu mới vào localStorage để component menuAction có thể chọn nó
        saveLastAddedAttribute('thuongHieu', tempId, newBrand.ten_thuong_hieu);

        message.success('Đã thêm thương hiệu mới (sẽ lưu khi bạn lưu sản phẩm)');
        newThuongHieu.ten_thuong_hieu = '';
        thuongHieuModalVisible.value = false;
    } catch (error) {
        console.error('Lỗi khi thêm thương hiệu:', error);
        message.error('Có lỗi xảy ra khi thêm thương hiệu!');
    }
};

const handleAddChatLieu = async () => {
    try {
        if (!newChatLieu.ten_chat_lieu) {
            message.error('Vui lòng nhập tên chất liệu!');
            return;
        }

        // Xóa chất liệu hiện tại trong newLocalAttributes và thay thế bằng chất liệu mới
        newLocalAttributes.chatLieu = [];

        // Generate a temporary ID
        const tempId = generateTempId('cl');

        // Create a new local material
        const newMaterial = {
            id_chat_lieu: tempId,
            ten_chat_lieu: newChatLieu.ten_chat_lieu.trim(),
            trang_thai: 'Hoạt động',
            _isNew: true // Mark as new to identify later
        };

        // Add to local list
        newLocalAttributes.chatLieu.push(newMaterial);

        // Luôn gán giá trị cho form field - không cần điều kiện
        formState.id_chat_lieu = tempId;

        // Lưu ID chất liệu mới vào localStorage để component menuAction có thể chọn nó
        saveLastAddedAttribute('chatLieu', tempId, newMaterial.ten_chat_lieu);

        message.success('Đã thêm chất liệu mới (sẽ lưu khi bạn lưu sản phẩm)');
        newChatLieu.ten_chat_lieu = '';
        chatLieuModalVisible.value = false;
    } catch (error) {
        console.error('Lỗi khi thêm chất liệu:', error);
        message.error('Có lỗi xảy ra khi thêm chất liệu!');
    }
};

const handleAddMauSac = async () => {
    try {
        if (!newMauSac.ten_mau_sac) {
            message.error('Vui lòng nhập tên màu sắc!');
            return;
        }

        // Xóa màu sắc hiện tại trong newLocalAttributes và thay thế bằng màu sắc mới
        newLocalAttributes.mauSac = [];

        // Generate a temporary ID
        const tempId = generateTempId('ms');

        // Create a new local color
        const newColor = {
            id_mau_sac: tempId,
            ten_mau_sac: newMauSac.ten_mau_sac.trim(),
            ma_mau_sac: `MS${newLocalAttributes.mauSac.length + 1}`, // Generate a temporary code
            trang_thai: 'Hoạt động',
            _isNew: true // Mark as new to identify later
        };

        // Add to local list
        newLocalAttributes.mauSac.push(newColor);

        // Lưu ID màu sắc mới vào localStorage để component menuAction có thể chọn nó
        saveLastAddedAttribute('mauSac', tempId, newColor.ten_mau_sac);

        // Tự động chọn màu sắc mới cho biến thể hiện tại đang mở
        if (variantTypes.value.length > 0) {
            // Tìm biến thể đầu tiên chưa có màu sắc hoặc biến thể cuối cùng
            const targetTypeIndex = variantTypes.value.findIndex(type => !type.id_mau_sac);
            const index = targetTypeIndex >= 0 ? targetTypeIndex : variantTypes.value.length - 1;

            // Gán màu sắc mới cho biến thể
            if (index >= 0) {
                variantTypes.value[index].id_mau_sac = tempId;
                console.log(`Đã tự động chọn màu sắc mới (${newColor.ten_mau_sac}) cho biến thể #${index + 1}`);

                // Cập nhật các biến thể từ dạng biến thể
                updateVariantsFromType(index);
            }
        }

        message.success('Đã thêm màu sắc mới (sẽ lưu khi bạn lưu sản phẩm)');
        newMauSac.ten_mau_sac = '';
        mauSacModalVisible.value = false;

        // Update available colors
        updateAvailableColors();
    } catch (error) {
        console.error('Lỗi khi thêm màu sắc:', error);
        message.error('Có lỗi xảy ra khi thêm màu sắc!');
    }
};

const handleAddKichThuoc = async () => {
    try {
        if (!newKichThuoc.gia_tri) {
            message.error('Vui lòng nhập giá trị kích thước!');
            return;
        }

        // Xóa kích thước hiện tại trong newLocalAttributes và thay thế bằng kích thước mới
        newLocalAttributes.kichThuoc = [];

        // Generate a temporary ID
        const tempId = generateTempId('kt');

        // Đảm bảo đơn vị là chuỗi rỗng nếu không được nhập
        const donVi = newKichThuoc.don_vi ? newKichThuoc.don_vi.trim() : '';

        // Create a new local size
        const newSize = {
            id_kich_thuoc: tempId,
            gia_tri: newKichThuoc.gia_tri.trim(),
            don_vi: donVi,
            trang_thai: 'Hoạt động',
            _isNew: true // Mark as new to identify later
        };

        // Add to local list
        newLocalAttributes.kichThuoc.push(newSize);

        // Lưu ID kích thước mới vào localStorage để component menuAction có thể chọn nó
        saveLastAddedAttribute('kichThuoc', tempId, newSize.gia_tri + (donVi ? ' ' + donVi : ''));

        // Tự động thêm kích thước mới vào danh sách đã chọn của biến thể hiện tại
        if (variantTypes.value.length > 0) {
            // Tìm biến thể đầu tiên đã có màu sắc hoặc biến thể cuối cùng
            const targetTypeIndex = variantTypes.value.findIndex(type => type.id_mau_sac);
            const index = targetTypeIndex >= 0 ? targetTypeIndex : variantTypes.value.length - 1;

            if (index >= 0 && variantTypes.value[index].selectedSizes) {
                // Thêm kích thước mới vào danh sách đã chọn
                variantTypes.value[index].selectedSizes.push(tempId);
                console.log(`Đã tự động thêm kích thước mới (${newSize.gia_tri} ${newSize.don_vi}) vào biến thể #${index + 1}`);

                // Cập nhật danh sách biến thể
                handleSizeChange(variantTypes.value[index].selectedSizes, index);
            }
        }

        message.success('Đã thêm kích thước mới (sẽ lưu khi bạn lưu sản phẩm)');
        newKichThuoc.gia_tri = '';
        newKichThuoc.don_vi = '';
        kichThuocModalVisible.value = false;
    } catch (error) {
        console.error('Lỗi khi thêm kích thước:', error);
        message.error('Có lỗi xảy ra khi thêm kích thước!');
    }
};

// Sửa hàm lưu thuộc tính vào localStorage để chỉ lưu thuộc tính mới nhất
const saveLastAddedAttribute = (type, id, name) => {
    try {
        // Không ghép nối từ localStorage nữa, mà tạo đối tượng mới mỗi lần lưu
        let attributes = {};

        // Lấy dữ liệu hiện tại từ localStorage để giữ các loại thuộc tính khác
        const existingData = localStorage.getItem('lastAddedAttributes');
        if (existingData) {
            attributes = JSON.parse(existingData);
        }

        // Thêm/thay thế thuộc tính mới
        attributes[type] = {
            id: id,
            name: name
        };

        // Lưu lại vào localStorage
        localStorage.setItem('lastAddedAttributes', JSON.stringify(attributes));

        console.log(`Đã cập nhật ${type} mới nhất (${name}, ID: ${id}) vào localStorage`);
    } catch (error) {
        console.error('Lỗi khi lưu thuộc tính mới vào localStorage:', error);
    }
};

// Thêm hàm để tải thuộc tính từ localStorage trong onMounted
const loadAttributesFromLocalStorage = () => {
    try {
        const storedData = localStorage.getItem('lastAddedAttributes');
        if (!storedData) return;

        const attributes = JSON.parse(storedData);

        // Xóa mảng newLocalAttributes hiện tại để cập nhật lại từ đầu
        newLocalAttributes.danhMuc = [];
        newLocalAttributes.thuongHieu = [];
        newLocalAttributes.chatLieu = [];
        newLocalAttributes.mauSac = [];
        newLocalAttributes.kichThuoc = [];

        // Thêm vào thuộc tính từ localStorage
        if (attributes.danhMuc) {
            // Tạo đối tượng mới với ID tạm thời
            const tempId = generateTempId('dm');
            const newCategory = {
                id_danh_muc: tempId,
                ten_danh_muc: attributes.danhMuc.name,
                trang_thai: 'Hoạt động',
                _isNew: true
            };
            newLocalAttributes.danhMuc.push(newCategory);

            // Tự động chọn danh mục trong form
            formState.id_danh_muc = tempId;

            // Cập nhật lại ID trong localStorage
            attributes.danhMuc.id = tempId;
            localStorage.setItem('lastAddedAttributes', JSON.stringify(attributes));
        }

        if (attributes.thuongHieu) {
            const tempId = generateTempId('th');
            const newBrand = {
                id_thuong_hieu: tempId,
                ten_thuong_hieu: attributes.thuongHieu.name,
                trang_thai: 'Hoạt động',
                _isNew: true
            };
            newLocalAttributes.thuongHieu.push(newBrand);

            // Tự động chọn thương hiệu trong form
            formState.id_thuong_hieu = tempId;

            // Cập nhật lại ID trong localStorage
            attributes.thuongHieu.id = tempId;
            localStorage.setItem('lastAddedAttributes', JSON.stringify(attributes));
        }

        if (attributes.chatLieu) {
            const tempId = generateTempId('cl');
            const newMaterial = {
                id_chat_lieu: tempId,
                ten_chat_lieu: attributes.chatLieu.name,
                trang_thai: 'Hoạt động',
                _isNew: true
            };
            newLocalAttributes.chatLieu.push(newMaterial);

            // Tự động chọn chất liệu trong form
            formState.id_chat_lieu = tempId;

            // Cập nhật lại ID trong localStorage
            attributes.chatLieu.id = tempId;
            localStorage.setItem('lastAddedAttributes', JSON.stringify(attributes));
        }

        if (attributes.mauSac) {
            const tempId = generateTempId('ms');
            const newColor = {
                id_mau_sac: tempId,
                ten_mau_sac: attributes.mauSac.name,
                ma_mau_sac: `MS${newLocalAttributes.mauSac.length + 1}`,
                trang_thai: 'Hoạt động',
                _isNew: true
            };
            newLocalAttributes.mauSac.push(newColor);

            // Cập nhật lại ID trong localStorage
            attributes.mauSac.id = tempId;
            localStorage.setItem('lastAddedAttributes', JSON.stringify(attributes));
        }

        if (attributes.kichThuoc) {
            const tempId = generateTempId('kt');
            // Kiểm tra và tách giá trị và đơn vị từ name (nếu có)
            let giaTri = attributes.kichThuoc.name;
            let donVi = '';

            const parts = attributes.kichThuoc.name.split(' ');
            if (parts.length > 1) {
                giaTri = parts[0];
                donVi = parts.slice(1).join(' ');
            }

            const newSize = {
                id_kich_thuoc: tempId,
                gia_tri: giaTri,
                don_vi: donVi,
                trang_thai: 'Hoạt động',
                _isNew: true
            };
            newLocalAttributes.kichThuoc.push(newSize);

            // Cập nhật lại ID trong localStorage
            attributes.kichThuoc.id = tempId;
            localStorage.setItem('lastAddedAttributes', JSON.stringify(attributes));
        }

        console.log('Đã tải thuộc tính từ localStorage:', newLocalAttributes);
    } catch (error) {
        console.error('Lỗi khi tải thuộc tính từ localStorage:', error);
    }
};

// Modify onFinish to save new attributes first, then use their IDs to save the product
const onFinish = async () => {
    if (!isProductValidated.value) {
        message.error('Vui lòng xác nhận thông tin sản phẩm trước');
        return;
    }

    loading.value = true;
    try {
        console.log('=== BẮT ĐẦU LƯU SẢN PHẨM ===');
        console.log('variantTypes:', variantTypes.value);
        console.log('variants:', variants.value);
        
        // Validate variants
        if (variants.value.length === 0) {
            throw new Error('Vui lòng thêm ít nhất một biến thể');
        }

        // Validate từng biến thể
        for (const variant of variants.value) {
            if (!variant.id_mau_sac || !variant.id_kich_thuoc) {
                throw new Error('Vui lòng điền đầy đủ thông tin cho tất cả biến thể');
            }
            
            // Validate ảnh cho biến thể (OPTIONAL - không bắt buộc)
            if (variant.selectedImages && variant.selectedImages.length > 0) {
                // Nếu có chọn ảnh thì phải có ảnh chính
                if (!variant.primaryImageUid) {
                    throw new Error(`Vui lòng chọn ảnh chính cho biến thể ${variant.mau_sac_name} - ${variant.kich_thuoc_name}`);
                }
            }
            
            // Validate giá của biến thể
            if (!useCommonPrice.value) {
                if (!variant.gia_ban || variant.gia_ban < 1000) {
                    throw new Error(`Giá bán của biến thể ${variant.mau_sac_name} - ${variant.kich_thuoc_name} phải lớn hơn 1000!`);
                }
            }
        }

        // Validate giá chung
        if (useCommonPrice.value) {
            if (!formState.gia_ban_chung || formState.gia_ban_chung < 1000) {
                throw new Error('Giá bán phải lớn hơn 1000!');
            }
        }

        // Sử dụng URL ảnh đã được upload trước đó
        if (fileList.value && fileList.value.length > 0 && fileList.value[0].response) {
            formState.hinh_anh = fileList.value[0].response;
        }

        // 1. First, save any new attributes and get their real IDs
        const idMappings = {
            danhMuc: new Map(),
            thuongHieu: new Map(),
            chatLieu: new Map(),
            mauSac: new Map(),
            kichThuoc: new Map()
        };

        // 1.1 Save Danh Mục if needed
        if (formState.id_danh_muc && formState.id_danh_muc.toString().startsWith('temp_')) {
            const danhMucItem = newLocalAttributes.danhMuc.find(item => item.id_danh_muc === formState.id_danh_muc);
            if (danhMucItem) {
                const response = await store.addDanhMuc(danhMucItem.ten_danh_muc);
                if (response.success) {
                    idMappings.danhMuc.set(formState.id_danh_muc, response.data.id_danh_muc);
                    formState.id_danh_muc = response.data.id_danh_muc;
                } else {
                    throw new Error(`Không thể thêm danh mục: ${response.message || 'Lỗi không xác định'}`);
                }
            }
        }

        // 1.2 Save Thương Hiệu if needed
        if (formState.id_thuong_hieu && formState.id_thuong_hieu.toString().startsWith('temp_')) {
            const thuongHieuItem = newLocalAttributes.thuongHieu.find(item => item.id_thuong_hieu === formState.id_thuong_hieu);
            if (thuongHieuItem) {
                const response = await store.addThuongHieu(thuongHieuItem.ten_thuong_hieu);
                if (response.success) {
                    idMappings.thuongHieu.set(formState.id_thuong_hieu, response.data.id_thuong_hieu);
                    formState.id_thuong_hieu = response.data.id_thuong_hieu;
                } else {
                    throw new Error(`Không thể thêm thương hiệu: ${response.message || 'Lỗi không xác định'}`);
                }
            }
        }

        // 1.3 Save Chất Liệu if needed
        if (formState.id_chat_lieu && formState.id_chat_lieu.toString().startsWith('temp_')) {
            const chatLieuItem = newLocalAttributes.chatLieu.find(item => item.id_chat_lieu === formState.id_chat_lieu);
            if (chatLieuItem) {
                const response = await store.addChatLieu(chatLieuItem.ten_chat_lieu);
                if (response.success) {
                    idMappings.chatLieu.set(formState.id_chat_lieu, response.data.id_chat_lieu);
                    formState.id_chat_lieu = response.data.id_chat_lieu;
                } else {
                    throw new Error(`Không thể thêm chất liệu: ${response.message || 'Lỗi không xác định'}`);
                }
            }
        }

        // 1.4 Save all new colors used in variants
        const usedColorIds = new Set(variants.value.map(v => v.id_mau_sac));
        const newColorIds = Array.from(usedColorIds).filter(id => id.toString().startsWith('temp_'));

        for (const tempColorId of newColorIds) {
            const colorItem = newLocalAttributes.mauSac.find(item => item.id_mau_sac === tempColorId);
            if (colorItem) {
                const response = await store.addMauSac(colorItem.ten_mau_sac);
                if (response.success) {
                    idMappings.mauSac.set(tempColorId, response.data.id_mau_sac);
                } else {
                    throw new Error(`Không thể thêm màu sắc: ${response.message || 'Lỗi không xác định'}`);
                }
            }
        }

        // 1.5 Save all new sizes used in variants
        const usedSizeIds = new Set(variants.value.map(v => v.id_kich_thuoc));
        const newSizeIds = Array.from(usedSizeIds).filter(id => id.toString().startsWith('temp_'));
        for (const tempSizeId of newSizeIds) {
            const sizeItem = newLocalAttributes.kichThuoc.find(item => item.id_kich_thuoc === tempSizeId);
            if (sizeItem) {
                if (sizeItem.don_vi) {
                    console.log('sizeItem.gia_tri khi ton tai don vi', sizeItem.gia_tri);
                    const response = await store.addKichThuoc(sizeItem.gia_tri, sizeItem.don_vi);
                    if (response.success) {
                        idMappings.kichThuoc.set(tempSizeId, response.data.id_kich_thuoc);
                    }
                } else {
                    sizeItem.gia_tri = sizeItem.gia_tri.toString();
                    sizeItem.don_vi = '';
                    console.log('sizeItem.gia_tri khi khong ton tai don vi', sizeItem.gia_tri);
                    const response = await store.addKichThuoc(sizeItem.gia_tri, sizeItem.don_vi);
                    if (response.success) {
                        idMappings.kichThuoc.set(tempSizeId, response.data.id_kich_thuoc);
                    }
                }
            }
        }

        // 2. Update variants with real IDs before saving
        const updatedVariants = variants.value.map(variant => {
            const variantCopy = { ...variant };

            // Replace temporary color ID with real ID if needed
            if (variant.id_mau_sac.toString().startsWith('temp_') && idMappings.mauSac.has(variant.id_mau_sac)) {
                variantCopy.id_mau_sac = idMappings.mauSac.get(variant.id_mau_sac);

                // Also update the color name if needed
                const newColorItem = newLocalAttributes.mauSac.find(item => item.id_mau_sac === variant.id_mau_sac);
                if (newColorItem) {
                    // Get the updated color from the API response
                    const realColor = mauSacList.value.find(c => c.id_mau_sac === variantCopy.id_mau_sac);
                    if (realColor) {
                        variantCopy.mau_sac_name = `${realColor.ma_mau_sac} ${realColor.ten_mau_sac}`;
                    }
                }
            }

            // Replace temporary size ID with real ID if needed
            if (variant.id_kich_thuoc.toString().startsWith('temp_') && idMappings.kichThuoc.has(variant.id_kich_thuoc)) {
                variantCopy.id_kich_thuoc = idMappings.kichThuoc.get(variant.id_kich_thuoc);

                // Also update the size name if needed
                const newSizeItem = newLocalAttributes.kichThuoc.find(item => item.id_kich_thuoc === variant.id_kich_thuoc);
                if (newSizeItem) {
                    // Get the updated size from the API response
                    const realSize = sizeList.value.find(s => s.id_kich_thuoc === variantCopy.id_kich_thuoc);
                    if (realSize) {
                        variantCopy.kich_thuoc_name = realSize.gia_tri;
                    }
                }
            }

            return variantCopy;
        });

        // Also update variant types to use real IDs to maintain UI consistency
        variantTypes.value.forEach(type => {
            if (type.id_mau_sac && type.id_mau_sac.toString().startsWith('temp_') && idMappings.mauSac.has(type.id_mau_sac)) {
                type.id_mau_sac = idMappings.mauSac.get(type.id_mau_sac);
            }

            if (type.selectedSizes) {
                type.selectedSizes = type.selectedSizes.map(sizeId => {
                    if (sizeId.toString().startsWith('temp_') && idMappings.kichThuoc.has(sizeId)) {
                        return idMappings.kichThuoc.get(sizeId);
                    }
                    return sizeId;
                });
            }
        });

        // 3. Now save the product with updated IDs
        console.log('FormState trước khi gửi với các ID thật:', formState);
        const response = await store.createSanPham(formState);
        console.log('Response nhận được:', response);

        // Kiểm tra response
        if (!response || !response.success) {
            throw new Error(response?.message || 'Không nhận được dữ liệu phản hồi hợp lệ từ server');
        }

        // Lấy ID sản phẩm từ data trả về
        const productId = response.data.id_san_pham;
        if (!productId) {
            throw new Error('Không nhận được ID sản phẩm từ server');
        }

        // 4. Create variants with real IDs - Mỗi biến thể có ảnh riêng
        await Promise.all(updatedVariants.map(async (variant) => {
            // Lấy variantType tương ứng với màu này
            const variantType = variantTypes.value.find(type => type.id_mau_sac === variant.id_mau_sac);
            
            // Build danh sách ảnh từ selectedImages của variant
            let variantImages = [];
            
            if (variantType && variantType.fileList) {
                // Nếu user đã chọn ảnh cụ thể cho variant này
                if (variant.selectedImages && variant.selectedImages.length > 0) {
                    // Lấy ảnh từ fileList của variantType dựa trên selectedImages
                    variantImages = variant.selectedImages.map(imageUid => {
                        const file = variantType.fileList.find(f => f.uid === imageUid);
                        return file ? (file.url || file.response) : null;
                    }).filter(url => url !== null);
                } 
                // ✅ LOGIC MỚI: Nếu chưa chọn ảnh cụ thể, tự động lấy TẤT CẢ ảnh của màu
                else {
                    variantImages = variantType.fileList
                        .filter(f => f.status === 'done')
                        .map(f => f.url || f.response)
                        .filter(url => url !== null);
                    
                    console.log(`✅ Tự động gán tất cả ảnh cho variant ${variant.mau_sac_name} - ${variant.kich_thuoc_name}`);
                }
                
                // Sắp xếp: ảnh chính lên đầu
                const primaryUid = variant.primaryImageUid || variantType.primaryImageUid;
                if (primaryUid) {
                    const primaryFile = variantType.fileList.find(f => f.uid === primaryUid);
                    if (primaryFile) {
                        const primaryUrl = primaryFile.url || primaryFile.response;
                        // Xóa ảnh chính khỏi vị trí cũ
                        variantImages = variantImages.filter(img => img !== primaryUrl);
                        // Thêm ảnh chính vào đầu
                        variantImages.unshift(primaryUrl);
                    }
                }
            }

            // Lưu danh sách ảnh (có thể rỗng nếu không có ảnh)
            const images = variantImages.length > 0 ? variantImages : [];

            console.log('Variant data before create:', {
                variant: `${variant.mau_sac_name} - ${variant.kich_thuoc_name}`,
                id_san_pham: productId,
                hasSelectedImages: !!(variant.selectedImages && variant.selectedImages.length > 0),
                selectedImages_count: variant.selectedImages?.length || 0,
                images_count: images.length,
                primaryImageUid: variant.primaryImageUid || variantType?.primaryImageUid,
                hinh_anh: images
            });

            // Tạo chi tiết sản phẩm với ảnh
            await store.createCTSP({
                ...variant,
                id_san_pham: productId,
                trang_thai: true,
                ngay_tao: new Date().toISOString(),
                ngay_sua: new Date().toISOString(),
                hinh_anh: images
            });
        }));

        message.success(response.message || 'Thêm sản phẩm và biến thể thành công!');
        await store.getAllSanPhamNgaySua();

        // Refresh all attribute lists
        await store.getDanhMucList();
        await store.getThuongHieuList();
        await store.getChatLieuList();
        await store.getMauSacList();
        await store.getSizeList();

        // Đánh dấu vừa thêm sản phẩm mới
        await store.getAllSanPhamNgaySua();
        // Đánh dấu vừa thêm sản phẩm mới
        store.changeAction(true);
        localStorage.setItem('justAddedProduct', 'true');
        // Đánh dấu cần refresh bộ lọc
        store.needFilterRefresh = true;

        // Emit một event toàn cục để thông báo cần refresh bộ lọc
        window.dispatchEvent(new CustomEvent('filter-data-updated'));

        router.push('/admin/quanlysanpham');
    } catch (error) {
        console.error('Chi tiết lỗi:', error);
        if (error.response?.data) {
            // Xử lý lỗi từ server
            const errorMessage = error.response.data.message || 'Có lỗi xảy ra khi thêm sản phẩm';
            message.error(errorMessage);
        } else {
            // Xử lý lỗi khác
            message.error(error.message || 'Có lỗi xảy ra khi thêm sản phẩm');
        }
    } finally {
        loading.value = false;
    }
};

// Xử lý thay đổi giá chung
const handlePriceChange = () => {
    if (useCommonPrice.value) {
        // Validate giá chung với giá hiện tại
        const isValid = validateGiaChung(formState.gia_ban_chung);

        if (isValid) {
            // Cập nhật tất cả giá biến thể theo giá chung
            variantTypes.value.forEach((type, index) => {
                type.gia_ban = formState.gia_ban_chung;
                // Sử dụng validateGiaBanSafe để tránh vòng lặp đệ quy
                validateGiaBanSafe(type.gia_ban, index);
                updateVariantsFromType(index);
            });
        }
    } else {
        // Reset validate status khi không dùng giá chung
        formState.giaChungValidateStatus = '';
        formState.giaChungValidateMessage = '';
    }
};

// Watch changes in formState để debug
watch(() => formState, (newVal) => {
    console.log('FormState changed:', newVal);
}, { deep: true });

// Xử lý khi upload hình ảnh cho từng biến thể cụ thể
const handleVariantImageChange = (info, variant) => {
    if (info.file.status === 'uploading') {
        loading.value = true;
        return;
    }

    if (info.file.status === 'done') {
        loading.value = false;
        // Giới hạn số lượng file
        const limitedFileList = info.fileList.slice(0, 3);
        // Cập nhật fileList cho biến thể cụ thể
        variant.fileList = limitedFileList;
        console.log('Variant image updated:', variant);
    }

    if (info.file.status === 'error') {
        loading.value = false;
        message.error(`${info.file.name} tải lên thất bại.`);
    }

    // Nếu xóa ảnh
    if (info.file.status === 'removed') {
        variant.fileList = [...info.fileList];
    }
};

// Thêm hàm xử lý khi giá biến thể thay đổi
const handleVariantPriceChange = (value, variant, field) => {
    if (field === 'gia_ban') {
        // Convert value to number and validate
        const numValue = parseFloat(String(value).replace(/,/g, ''));

        if (isNaN(numValue) || numValue < 1000) {
            message.error('Giá bán phải lớn hơn 1000!');
            variant.gia_ban = 1000; // Reset về giá trị tối thiểu
            return;
        }

        // Update the variant price directly
        variant.gia_ban = numValue;
    }
};

// Thêm watch cho formState để kiểm tra validation
watch(() => formState, async (newVal) => {
    if (!formRef.value) return;
    try {
        await formRef.value.validate();
        isProductValidated.value = true;
    } catch (error) {
        isProductValidated.value = false;
    }
}, { deep: true });

const someAsyncFunction = async () => {
    try {
        const result = await someApi();
        if (!mounted.value) return; // Không cập nhật nếu component đã unmount
        // Xử lý kết quả
    } catch (error) {
        if (!mounted.value) return;
        // Xử lý lỗi
    }
};

// Add new refs for modals
const danhMucModalVisible = ref(false);
const thuongHieuModalVisible = ref(false);
const chatLieuModalVisible = ref(false);
const mauSacModalVisible = ref(false);
const kichThuocModalVisible = ref(false);

// Add new refs for form data
const newDanhMuc = reactive({
    ten_danh_muc: ''
});

const newThuongHieu = reactive({
    ten_thuong_hieu: ''
});

const newChatLieu = reactive({
    ten_chat_lieu: ''
});

const newMauSac = reactive({
    ten_mau_sac: ''
});

const newKichThuoc = reactive({
    gia_tri: '',
    don_vi: ''
});

// Add functions to show modals
const showAddDanhMucModal = () => {
    danhMucModalVisible.value = true;
};

const showAddThuongHieuModal = () => {
    thuongHieuModalVisible.value = true;
};

const showAddChatLieuModal = () => {
    chatLieuModalVisible.value = true;
};

const showAddMauSacModal = () => {
    mauSacModalVisible.value = true;
};

const showAddKichThuocModal = () => {
    kichThuocModalVisible.value = true;
};

// Thêm refs cho các form
const danhMucFormRef = ref(null);
const thuongHieuFormRef = ref(null);
const chatLieuFormRef = ref(null);
const mauSacFormRef = ref(null);
const kichThuocFormRef = ref(null);

// Hiển thị toast modal nếu có lỗi
const showValidationError = (title, message) => {
    Modal.error({
        title: title,
        content: message,
    });
};

// Thêm các hàm xử lý validate và submit
const submitDanhMuc = () => {
    if (danhMucFormRef.value) {
        danhMucFormRef.value.validateFields()
            .then(() => {
                handleAddDanhMuc();
            })
            .catch(errors => {
                console.log('Validate Failed:', errors);
            });
    }
};

const submitThuongHieu = () => {
    if (thuongHieuFormRef.value) {
        thuongHieuFormRef.value.validateFields()
            .then(() => {
                handleAddThuongHieu();
            })
            .catch(errors => {
                console.log('Validate Failed:', errors);
            });
    }
};

const submitChatLieu = () => {
    if (chatLieuFormRef.value) {
        chatLieuFormRef.value.validateFields()
            .then(() => {
                handleAddChatLieu();
            })
            .catch(errors => {
                console.log('Validate Failed:', errors);
            });
    }
};

const submitMauSac = () => {
    if (mauSacFormRef.value) {
        mauSacFormRef.value.validateFields()
            .then(() => {
                handleAddMauSac();
            })
            .catch(errors => {
                console.log('Validate Failed:', errors);
            });
    }
};

const submitKichThuoc = () => {
    if (kichThuocFormRef.value) {
        kichThuocFormRef.value.validateFields()
            .then(() => {
                handleAddKichThuoc();
            })
            .catch(errors => {
                console.log('Validate Failed:', errors);
            });
    }
};

// Validate số lượng trong biến thể
const validateSoLuong = (value, typeIndex, isBlur = false) => {
    const type = variantTypes.value[typeIndex];

    // Nếu giá trị không được cung cấp
    if (value === undefined || value === null || value === '') {
        type.soLuongValidateStatus = 'error';
        type.soLuongValidateMessage = 'Vui lòng nhập số lượng';
        return false;
    }

    // Nếu giá trị không phải là số
    if (isNaN(value)) {
        type.soLuongValidateStatus = 'error';
        type.soLuongValidateMessage = 'Vui lòng chỉ nhập số';
        return false;
    }

    // Chuyển đổi sang số để so sánh
    const numValue = Number(value);

    // Nếu giá trị <= 0
    if (numValue <= 0) {
        type.soLuongValidateStatus = 'error';
        type.soLuongValidateMessage = 'Số lượng tối thiểu là 1';
        return false;
    }

    // Nếu giá trị không phải số nguyên
    if (!Number.isInteger(numValue)) {
        type.soLuongValidateStatus = 'error';
        type.soLuongValidateMessage = 'Số lượng phải là số nguyên';
        return false;
    }

    // Nếu giá trị quá lớn
    if (numValue > 100000) {
        type.soLuongValidateStatus = 'error';
        type.soLuongValidateMessage = 'Số lượng không được vượt quá 100,000';
        return false;
    }

    // Nếu hợp lệ
    type.soLuongValidateStatus = 'success';
    type.soLuongValidateMessage = '';

    // Cập nhật biến thể
    if (isBlur || (!isNaN(numValue) && numValue > 0 && numValue <= 100000 && Number.isInteger(numValue))) {
        updateVariantsFromType(typeIndex);
    }

    return true;
};

// Phiên bản an toàn của validateSoLuong để tránh vòng lặp đệ quy
const validateSoLuongSafe = (value, typeIndex, isBlur = false) => {
    const type = variantTypes.value[typeIndex];

    // Nếu giá trị không được cung cấp
    if (value === undefined || value === null || value === '') {
        type.soLuongValidateStatus = 'error';
        type.soLuongValidateMessage = 'Vui lòng nhập số lượng';
        return false;
    }

    // Nếu giá trị không phải là số
    if (isNaN(value)) {
        type.soLuongValidateStatus = 'error';
        type.soLuongValidateMessage = 'Vui lòng chỉ nhập số';
        return false;
    }

    // Chuyển đổi sang số để so sánh
    const numValue = Number(value);

    // Nếu giá trị <= 0
    if (numValue <= 0) {
        type.soLuongValidateStatus = 'error';
        type.soLuongValidateMessage = 'Số lượng tối thiểu là 1';
        return false;
    }

    // Nếu giá trị không phải số nguyên
    if (!Number.isInteger(numValue)) {
        type.soLuongValidateStatus = 'error';
        type.soLuongValidateMessage = 'Số lượng phải là số nguyên';
        return false;
    }

    // Nếu giá trị quá lớn
    if (numValue > 100000) {
        type.soLuongValidateStatus = 'error';
        type.soLuongValidateMessage = 'Số lượng không được vượt quá 100,000';
        return false;
    }

    // Nếu hợp lệ
    type.soLuongValidateStatus = 'success';
    type.soLuongValidateMessage = '';

    // QUAN TRỌNG: Không gọi updateVariantsFromType ở đây để tránh vòng lặp đệ quy
    return true;
};

// Xử lý input số lượng
const handleSoLuongInput = (value, typeIndex) => {
    // Chuyển sang dạng chuỗi để kiểm tra
    const stringValue = String(value);

    // Kiểm tra chỉ chứa số
    if (!/^\d*$/.test(stringValue)) {
        validateSoLuong(value, typeIndex);
    } else {
        // Nếu là số thì chuyển sang số và validate
        validateSoLuong(Number(value), typeIndex);
    }
};

// Validate giá bán trong biến thể
const validateGiaBan = (value, typeIndex, isBlur = false) => {
    const type = variantTypes.value[typeIndex];

    // Nếu giá trị không phải là số
    if (isNaN(value)) {
        type.giaBanValidateStatus = 'error';
        type.giaBanValidateMessage = 'Vui lòng chỉ nhập số';
        return false;
    }

    // Nếu giá trị < 1000
    if (value < 1000) {
        type.giaBanValidateStatus = 'error';
        type.giaBanValidateMessage = 'Giá tối thiểu là 1,000đ';
        return false;
    }

    // Nếu giá trị quá lớn
    if (value > 100000000) {
        type.giaBanValidateStatus = 'error';
        type.giaBanValidateMessage = 'Giá không được vượt quá 100,000,000đ';
        return false;
    }

    // Nếu hợp lệ
    type.giaBanValidateStatus = 'success';
    type.giaBanValidateMessage = '';

    // Cập nhật biến thể
    if (isBlur || (!isNaN(value) && value >= 1000 && value <= 100000000)) {
        updateVariantsFromType(typeIndex);
    }

    return true;
};

// Xử lý input giá bán
const handleGiaBanInput = (value, typeIndex) => {
    // Chuyển sang dạng chuỗi để kiểm tra
    const stringValue = String(value).replace(/,/g, '');

    // Kiểm tra chỉ chứa số
    if (!/^\d*$/.test(stringValue)) {
        validateGiaBan(value, typeIndex);
    } else {
        // Nếu là số thì chuyển sang số và validate
        validateGiaBan(Number(stringValue), typeIndex);
    }
};

// Phiên bản an toàn của validateGiaBan để tránh vòng lặp đệ quy
const validateGiaBanSafe = (value, typeIndex, isBlur = false) => {
    const type = variantTypes.value[typeIndex];

    // Nếu giá trị không phải là số
    if (isNaN(value)) {
        type.giaBanValidateStatus = 'error';
        type.giaBanValidateMessage = 'Vui lòng chỉ nhập số';
        return false;
    }

    // Nếu giá trị < 1000
    if (value < 1000) {
        type.giaBanValidateStatus = 'error';
        type.giaBanValidateMessage = 'Giá tối thiểu là 1,000đ';
        return false;
    }

    // Nếu giá trị quá lớn
    if (value > 100000000) {
        type.giaBanValidateStatus = 'error';
        type.giaBanValidateMessage = 'Giá không được vượt quá 100,000,000đ';
        return false;
    }

    // Nếu hợp lệ
    type.giaBanValidateStatus = 'success';
    type.giaBanValidateMessage = '';

    // QUAN TRỌNG: Không gọi updateVariantsFromType ở đây để tránh vòng lặp đệ quy
    return true;
};

// Add new refs for modals
const sizeGuideModalVisible = ref(false);

// Add functions to show modals
const showSizeGuideModal = () => {
    sizeGuideModalVisible.value = true;
};

// Thêm state để quản lý ảnh
const imageLoadingStates = ref(new Map()); // Theo dõi trạng thái loading của từng ảnh
const variantImageLists = ref(new Map()); // Lưu danh sách ảnh cho mỗi màu sắc

// Hàm lấy biến thể đầu tiên của một màu sắc
const getFirstVariantByColor = (colorId) => {
    return variants.value.find(v => v.id_mau_sac === colorId);
};

// Hàm cập nhật danh sách ảnh cho một màu sắc
const updateImagesForColor = (colorId, images) => {
    variantImageLists.value.set(colorId, images);

    // Tìm biến thể đầu tiên của màu này và gán list ảnh
    const firstVariant = getFirstVariantByColor(colorId);
    if (firstVariant) {
        firstVariant.hinh_anh = images;

        // Các biến thể còn lại của màu này sẽ có list ảnh rỗng
        variants.value.forEach(variant => {
            if (variant.id_mau_sac === colorId && variant !== firstVariant) {
                variant.hinh_anh = [];
            }
        });
    }
};

// Hàm xử lý upload ảnh sản phẩm chính
const handleProductImageUpload = async ({ file, onSuccess, onError, onProgress }) => {
    try {
        // Set loading state
        productImageLoading.value = true;

        // Upload file lên cloud
        const responseUrl = await uploadImage(file);

        if (responseUrl) {
            // Cập nhật URL vào formState
            formState.hinh_anh = responseUrl;

            // Cập nhật fileList
            fileList.value = [{
                uid: file.uid,
                name: file.name,
                status: 'done',
                url: responseUrl,
                response: responseUrl // Lưu URL vào response để truy cập sau này
            }];

            onSuccess(responseUrl);
            message.success('Tải ảnh sản phẩm lên thành công');
        } else {
            throw new Error('Không nhận được URL từ Cloudinary');
        }
    } catch (error) {
        console.error('Lỗi upload:', error);
        onError(error);
        message.error('Không thể tải lên hình ảnh: ' + error.message);
    } finally {
        productImageLoading.value = false;
    }
};

// Hàm xử lý xóa ảnh sản phẩm chính
const handleProductImageRemove = async (file) => {
    Modal.confirm({
        title: 'Xác nhận xóa',
        content: 'Bạn có chắc chắn muốn xóa ảnh sản phẩm này không?',
        okText: 'Đồng ý',
        cancelText: 'Hủy',
        async onOk() {
            try {
                const loadingKey = 'deletingProductImage';
                message.loading({ content: 'Đang xóa ảnh...', key: loadingKey });

                const url = file.url || file.response;
                if (!url) throw new Error('Không tìm thấy URL của ảnh');

                // Lấy public_id từ URL, bao gồm cả thư mục
                const folderName = 'vue-ecom/';
                const startIndex = url.indexOf(folderName);
                if (startIndex === -1) {
                    throw new Error('URL không hợp lệ hoặc không chứa thư mục ảnh');
                }
                const publicIdWithFolder = url.substring(startIndex);
                const publicId = publicIdWithFolder.substring(0, publicIdWithFolder.lastIndexOf('.'));


                // Gọi API xóa ảnh
                await testService.deleteImage(publicId);

                // Reset formState và fileList
                formState.hinh_anh = '';
                fileList.value = [];

                message.success({ content: 'Đã xóa ảnh sản phẩm thành công', key: loadingKey });
            } catch (error) {
                console.error('Lỗi khi xóa ảnh:', error);
                message.error('Không thể xóa ảnh: ' + error.message);
            }
        }
    });
};

// Thêm biến reactive cho AutoComplete
const danhMucSearch = ref('');
const thuongHieuSearch = ref('');
const chatLieuSearch = ref('');

// Thêm computed properties để lọc danh sách tùy chọn
const filteredDanhMucOptions = computed(() => {
    console.log('Tính toán lại filteredDanhMucOptions');
    
    // Bảo vệ nếu combinedDanhMucList rỗng
    if (!combinedDanhMucList.value || combinedDanhMucList.value.length === 0) {
        console.warn('combinedDanhMucList rỗng hoặc không xác định');
        // Thử trực tiếp danhMucList nếu combined không có
        if (danhMucList.value && danhMucList.value.length > 0) {
            console.log('Sử dụng danhMucList thay thế, có', danhMucList.value.length, 'mục');
        } else {
            console.warn('Không có dữ liệu danh mục nào');
            return [];
        }
    }
    
    // Nếu searchText undefined thì gán giá trị rỗng
    const searchText = (danhMucSearch.value || '').toLowerCase();
    console.log('Từ khóa tìm kiếm danh mục:', searchText);
    
    // Sử dụng danh sách hợp nhất từ combined hoặc fallback là danh sách gốc
    const sourceList = combinedDanhMucList.value.length > 0 ? 
        combinedDanhMucList.value : 
        (danhMucList.value || []);
    
    console.log('Nguồn dữ liệu danh mục có', sourceList.length, 'mục');
    
    // Nếu đã có một danh mục đã chọn trong formState, hiển thị tên của nó trong ô search
    if (formState.id_danh_muc) {
        const selectedItem = sourceList.find(item => item.id_danh_muc === formState.id_danh_muc);
        if (selectedItem && searchText === selectedItem.ten_danh_muc.toLowerCase()) {
            return [];
        }
    }
    
    // Filter và map dữ liệu
    const result = sourceList
        .filter(item => {
            // Kiểm tra trạng thái
            if (!item.trang_thai) {
                return false;
            }
            
            // Kiểm tra tên chứa chuỗi tìm kiếm
            const tenDanhMuc = item.ten_danh_muc || '';
            return tenDanhMuc.toLowerCase().includes(searchText);
        })
        .map(item => ({
            value: item.id_danh_muc,
            label: item.ten_danh_muc
        }));
    
    console.log('Kết quả lọc danh mục:', result.length, 'mục khớp');
    return result;
});

const filteredThuongHieuOptions = computed(() => {
    console.log('Tính toán lại filteredThuongHieuOptions');
    
    // Bảo vệ nếu combinedThuongHieuList rỗng
    if (!combinedThuongHieuList.value || combinedThuongHieuList.value.length === 0) {
        console.warn('combinedThuongHieuList rỗng hoặc không xác định');
        // Thử trực tiếp thuongHieuList nếu combined không có
        if (thuongHieuList.value && thuongHieuList.value.length > 0) {
            console.log('Sử dụng thuongHieuList thay thế, có', thuongHieuList.value.length, 'mục');
        } else {
            console.warn('Không có dữ liệu thương hiệu nào');
            return [];
        }
    }
    
    // Nếu searchText undefined thì gán giá trị rỗng
    const searchText = (thuongHieuSearch.value || '').toLowerCase();
    console.log('Từ khóa tìm kiếm thương hiệu:', searchText);
    
    // Sử dụng danh sách hợp nhất từ combined hoặc fallback là danh sách gốc
    const sourceList = combinedThuongHieuList.value.length > 0 ? 
        combinedThuongHieuList.value : 
        (thuongHieuList.value || []);
    
    console.log('Nguồn dữ liệu thương hiệu có', sourceList.length, 'mục');
    
    // Nếu đã có một thương hiệu đã chọn trong formState, hiển thị tên của nó trong ô search
    if (formState.id_thuong_hieu) {
        const selectedItem = sourceList.find(item => item.id_thuong_hieu === formState.id_thuong_hieu);
        if (selectedItem && searchText === selectedItem.ten_thuong_hieu.toLowerCase()) {
            return [];
        }
    }
    
    // Filter và map dữ liệu
    const result = sourceList
        .filter(item => {
            // Kiểm tra trạng thái
            if (!item.trang_thai) {
                return false;
            }
            
            // Kiểm tra tên chứa chuỗi tìm kiếm
            const tenThuongHieu = item.ten_thuong_hieu || '';
            return tenThuongHieu.toLowerCase().includes(searchText);
        })
        .map(item => ({
            value: item.id_thuong_hieu,
            label: item.ten_thuong_hieu
        }));
    
    console.log('Kết quả lọc thương hiệu:', result.length, 'mục khớp');
    return result;
});

const filteredChatLieuOptions = computed(() => {
    console.log('Tính toán lại filteredChatLieuOptions');
    
    // Bảo vệ nếu combinedChatLieuList rỗng
    if (!combinedChatLieuList.value || combinedChatLieuList.value.length === 0) {
        console.warn('combinedChatLieuList rỗng hoặc không xác định');
        // Thử trực tiếp chatLieuList nếu combined không có
        if (chatLieuList.value && chatLieuList.value.length > 0) {
            console.log('Sử dụng chatLieuList thay thế, có', chatLieuList.value.length, 'mục');
        } else {
            console.warn('Không có dữ liệu chất liệu nào');
            return [];
        }
    }
    
    // Nếu searchText undefined thì gán giá trị rỗng
    const searchText = (chatLieuSearch.value || '').toLowerCase();
    console.log('Từ khóa tìm kiếm chất liệu:', searchText);
    
    // Sử dụng danh sách hợp nhất từ combined hoặc fallback là danh sách gốc
    const sourceList = combinedChatLieuList.value.length > 0 ? 
        combinedChatLieuList.value : 
        (chatLieuList.value || []);
    
    console.log('Nguồn dữ liệu chất liệu có', sourceList.length, 'mục');
    
    // Nếu đã có một chất liệu đã chọn trong formState, hiển thị tên của nó trong ô search
    if (formState.id_chat_lieu) {
        const selectedItem = sourceList.find(item => item.id_chat_lieu === formState.id_chat_lieu);
        if (selectedItem && searchText === selectedItem.ten_chat_lieu.toLowerCase()) {
            return [];
        }
    }
    
    // Filter và map dữ liệu
    const result = sourceList
        .filter(item => {
            // Kiểm tra trạng thái
            if (!item.trang_thai ) {
                return false;
            }
            
            // Kiểm tra tên chứa chuỗi tìm kiếm
            const tenChatLieu = item.ten_chat_lieu || '';
            return tenChatLieu.toLowerCase().includes(searchText);
        })
        .map(item => ({
            value: item.id_chat_lieu,
            label: item.ten_chat_lieu
        }));
    
    console.log('Kết quả lọc chất liệu:', result.length, 'mục khớp');
    return result;
});

// Thêm handlers cho các sự kiện của AutoComplete
const onSelectDanhMuc = (value) => {
    console.log('onSelectDanhMuc được gọi với giá trị:', value);
    
    // Chuyển đổi giá trị sang số nếu cần
    const numericValue = typeof value === 'string' && !isNaN(Number(value)) ? 
        Number(value) : value;
    
    // Cập nhật formState
    formState.id_danh_muc = numericValue;
    
    // Tìm item trong combồ hoặc danh sách gốc
    const sourceList = combinedDanhMucList.value.length > 0 ? 
        combinedDanhMucList.value : danhMucList.value;
        
    const selectedItem = sourceList.find(item => 
        String(item.id_danh_muc) === String(numericValue));
    
    danhMucSearch.value = selectedItem?.ten_danh_muc || '';
    
    // Force validation: clear rồi validate lại
    nextTick(() => {
        if (formRef.value && numericValue) {
            formRef.value.clearValidate(['id_danh_muc']);
            formRef.value.validateFields(['id_danh_muc']).catch(() => {});
        }
    });
    
    console.log('formState.id_danh_muc sau khi cập nhật:', formState.id_danh_muc);
};

const onSelectThuongHieu = (value) => {
    console.log('onSelectThuongHieu được gọi với giá trị:', value);
    
    // Chuyển đổi giá trị sang số nếu cần
    const numericValue = typeof value === 'string' && !isNaN(Number(value)) ? 
        Number(value) : value;
    
    // Cập nhật formState
    formState.id_thuong_hieu = numericValue;
    
    // Tìm item trong combồ hoặc danh sách gốc
    const sourceList = combinedThuongHieuList.value.length > 0 ? 
        combinedThuongHieuList.value : thuongHieuList.value;
        
    const selectedItem = sourceList.find(item => 
        String(item.id_thuong_hieu) === String(numericValue));
    
    thuongHieuSearch.value = selectedItem?.ten_thuong_hieu || '';
    
    // Force validation: clear rồi validate lại
    nextTick(() => {
        if (formRef.value && numericValue) {
            formRef.value.clearValidate(['id_thuong_hieu']);
            formRef.value.validateFields(['id_thuong_hieu']).catch(() => {});
        }
    });
    
    console.log('formState.id_thuong_hieu sau khi cập nhật:', formState.id_thuong_hieu);
};

const onSelectChatLieu = (value) => {
    console.log('onSelectChatLieu được gọi với giá trị:', value);
    
    // Chuyển đổi giá trị sang số nếu cần
    const numericValue = typeof value === 'string' && !isNaN(Number(value)) ? 
        Number(value) : value;
    
    // Cập nhật formState
    formState.id_chat_lieu = numericValue;
    
    // Tìm item trong combồ hoặc danh sách gốc
    const sourceList = combinedChatLieuList.value.length > 0 ? 
        combinedChatLieuList.value : chatLieuList.value;
        
    const selectedItem = sourceList.find(item => 
        String(item.id_chat_lieu) === String(numericValue));
    
    chatLieuSearch.value = selectedItem?.ten_chat_lieu || '';
    
    // Force validation: clear rồi validate lại
    nextTick(() => {
        if (formRef.value && numericValue) {
            formRef.value.clearValidate(['id_chat_lieu']);
            formRef.value.validateFields(['id_chat_lieu']).catch(() => {});
        }
    });
    
    console.log('formState.id_chat_lieu sau khi cập nhật:', formState.id_chat_lieu);
};

const onSearchDanhMuc = (searchText) => {
    console.log('onSearchDanhMuc được gọi với:', searchText);
    danhMucSearch.value = searchText;
    
    // Nếu trống rỗng hoàn toàn, reset id_danh_muc
    if (!searchText.trim()) {
        formState.id_danh_muc = undefined;
    }
};

const onSearchThuongHieu = (searchText) => {
    console.log('onSearchThuongHieu được gọi với:', searchText);
    thuongHieuSearch.value = searchText;
    
    // Nếu trống rỗng hoàn toàn, reset id_thuong_hieu
    if (!searchText.trim()) {
        formState.id_thuong_hieu = undefined;
    }
};

const onSearchChatLieu = (searchText) => {
    console.log('onSearchChatLieu được gọi với:', searchText);
    chatLieuSearch.value = searchText;
    
    // Nếu trống rỗng hoàn toàn, reset id_chat_lieu
    if (!searchText.trim()) {
        formState.id_chat_lieu = undefined;
    }
};

// Thêm watchers để cập nhật giá trị hiển thị khi formState thay đổi
watch(() => formState.id_danh_muc, (newVal) => {
    console.log('Watch detected change in id_danh_muc:', newVal);
    
    if (newVal) {
        // Tìm item trong combồ hoặc danh sách gốc
        const sourceList = combinedDanhMucList.value.length > 0 ? 
            combinedDanhMucList.value : danhMucList.value;
            
        if (!sourceList || sourceList.length === 0) {
            console.warn('Không có danh sách danh mục để tìm kiếm');
            return;
        }
        
        console.log('Tìm kiếm trong danh sách danh mục có', sourceList.length, 'phần tử');
        const item = sourceList.find(item => String(item.id_danh_muc) === String(newVal));
        
        if (item) {
            danhMucSearch.value = item.ten_danh_muc;
            console.log('Cập nhật danhMucSearch thành:', item.ten_danh_muc);
        } else {
            console.warn('Đã có id_danh_muc nhưng không tìm thấy danh mục tương ứng:', newVal);
        }
    } else {
        danhMucSearch.value = '';
        console.log('Reset danhMucSearch thành rỗng');
    }
});

watch(() => formState.id_thuong_hieu, (newVal) => {
    console.log('Watch detected change in id_thuong_hieu:', newVal);
    
    if (newVal) {
        // Tìm item trong combồ hoặc danh sách gốc
        const sourceList = combinedThuongHieuList.value.length > 0 ? 
            combinedThuongHieuList.value : thuongHieuList.value;
            
        if (!sourceList || sourceList.length === 0) {
            console.warn('Không có danh sách thương hiệu để tìm kiếm');
            return;
        }
        
        console.log('Tìm kiếm trong danh sách thương hiệu có', sourceList.length, 'phần tử');
        const item = sourceList.find(item => String(item.id_thuong_hieu) === String(newVal));
        
        if (item) {
            thuongHieuSearch.value = item.ten_thuong_hieu;
            console.log('Cập nhật thuongHieuSearch thành:', item.ten_thuong_hieu);
        } else {
            console.warn('Đã có id_thuong_hieu nhưng không tìm thấy thương hiệu tương ứng:', newVal);
        }
    } else {
        thuongHieuSearch.value = '';
        console.log('Reset thuongHieuSearch thành rỗng');
    }
});

watch(() => formState.id_chat_lieu, (newVal) => {
    console.log('Watch detected change in id_chat_lieu:', newVal);
    
    if (newVal) {
        // Tìm item trong combồ hoặc danh sách gốc
        const sourceList = combinedChatLieuList.value.length > 0 ? 
            combinedChatLieuList.value : chatLieuList.value;
            
        if (!sourceList || sourceList.length === 0) {
            console.warn('Không có danh sách chất liệu để tìm kiếm');
            return;
        }
        
        console.log('Tìm kiếm trong danh sách chất liệu có', sourceList.length, 'phần tử');
        const item = sourceList.find(item => String(item.id_chat_lieu) === String(newVal));
        
        if (item) {
            chatLieuSearch.value = item.ten_chat_lieu;
            console.log('Cập nhật chatLieuSearch thành:', item.ten_chat_lieu);
        } else {
            console.warn('Đã có id_chat_lieu nhưng không tìm thấy chất liệu tương ứng:', newVal);
        }
    } else {
        chatLieuSearch.value = '';
        console.log('Reset chatLieuSearch thành rỗng');
    }
});

// Thêm watchers cho danh sách gốc để cập nhật lại text khi danh sách được tải
watch(() => danhMucList.value, (newList) => {
    console.log('danhMucList đã thay đổi, có', newList.length, 'mục');
    
    // Nếu đã có ID được chọn, cập nhật lại text hiển thị
    if (formState.id_danh_muc && newList.length > 0) {
        const item = newList.find(item => String(item.id_danh_muc) === String(formState.id_danh_muc));
        if (item) {
            danhMucSearch.value = item.ten_danh_muc;
            console.log('Cập nhật lại danhMucSearch từ danh sách mới:', item.ten_danh_muc);
        }
    }
});

watch(() => thuongHieuList.value, (newList) => {
    console.log('thuongHieuList đã thay đổi, có', newList.length, 'mục');
    
    // Nếu đã có ID được chọn, cập nhật lại text hiển thị
    if (formState.id_thuong_hieu && newList.length > 0) {
        const item = newList.find(item => String(item.id_thuong_hieu) === String(formState.id_thuong_hieu));
        if (item) {
            thuongHieuSearch.value = item.ten_thuong_hieu;
            console.log('Cập nhật lại thuongHieuSearch từ danh sách mới:', item.ten_thuong_hieu);
        }
    }
});

watch(() => chatLieuList.value, (newList) => {
    console.log('chatLieuList đã thay đổi, có', newList.length, 'mục');
    
    // Nếu đã có ID được chọn, cập nhật lại text hiển thị
    if (formState.id_chat_lieu && newList.length > 0) {
        const item = newList.find(item => String(item.id_chat_lieu) === String(formState.id_chat_lieu));
        if (item) {
            chatLieuSearch.value = item.ten_chat_lieu;
            console.log('Cập nhật lại chatLieuSearch từ danh sách mới:', item.ten_chat_lieu);
        }
    }
});

// Gộp tất cả logic vào một hook onMounted duy nhất
onMounted(async () => {
    console.log('Component themSanPham đang khởi tạo...');
    // Khởi tạo nội dung editor
    if (editorRef.value) {
        editorRef.value.innerHTML = formState.mo_ta || '';
    }

    // Theo dõi sự thay đổi của mauSacList để cập nhật màu sắc có sẵn
    watch(() => mauSacList.value, () => {
        updateAvailableColors();
        console.log('mauSacList đã thay đổi, cập nhật availableColors');
    }, { immediate: true });

    // Fetch initial data
    try {
        console.log('Bắt đầu tải dữ liệu từ API...');
        // Load tất cả thuộc tính từ localStorage trước
        loadAttributesFromLocalStorage();

        // Lấy danh sách sản phẩm để tạo mã tự động
        await store.getAllSanPhamNgaySua();

        // Tạo mã sản phẩm tự động
        formState.ma_san_pham = generateProductCode(store.getAllSanPham);

        // Thay thế bằng các actions thực tế từ store của bạn
        console.log('Đang tải danh mục...');
        await store.getDanhMucList();
        console.log('Đã tải danh mục:', store.danhMucList);
        
        console.log('Đang tải thương hiệu...');
        await store.getThuongHieuList();
        console.log('Đã tải thương hiệu:', store.thuongHieuList);
        
        console.log('Đang tải chất liệu...');
        await store.getChatLieuList();
        console.log('Đã tải chất liệu:', store.chatLieuList);

        // Gán giá trị từ store
        danhMucList.value = store.danhMucList;
        thuongHieuList.value = store.thuongHieuList;
        chatLieuList.value = store.chatLieuList;

        // Thêm các API calls để lấy danh sách màu sắc và size
        console.log('Đang tải màu sắc...');
        await store.getMauSacList();
        console.log('Đã tải màu sắc:', store.mauSacList);
        
        console.log('Đang tải kích thước...');
        await store.getSizeList();
        console.log('Đã tải kích thước:', store.sizeList);
        
        mauSacList.value = store.mauSacList;
        sizeList.value = store.sizeList;

        // Khởi tạo giá trị hiển thị cho AutoComplete từ formState
        await nextTick(); // Đợi DOM cập nhật
        
        // Cập nhật giá trị hiển thị cho các trường tìm kiếm
        if (formState.id_danh_muc) {
            const item = combinedDanhMucList.value.find(item => item.id_danh_muc === formState.id_danh_muc);
            if (item) {
                danhMucSearch.value = item.ten_danh_muc;
                console.log('Đã cập nhật giá trị tìm kiếm danh mục:', danhMucSearch.value);
            }
        }
        
        if (formState.id_thuong_hieu) {
            const item = combinedThuongHieuList.value.find(item => item.id_thuong_hieu === formState.id_thuong_hieu);
            if (item) {
                thuongHieuSearch.value = item.ten_thuong_hieu;
                console.log('Đã cập nhật giá trị tìm kiếm thương hiệu:', thuongHieuSearch.value);
            }
        }
        
        if (formState.id_chat_lieu) {
            const item = combinedChatLieuList.value.find(item => item.id_chat_lieu === formState.id_chat_lieu);
            if (item) {
                chatLieuSearch.value = item.ten_chat_lieu;
                console.log('Đã cập nhật giá trị tìm kiếm chất liệu:', chatLieuSearch.value);
            }
        }
        
        // Kiểm tra xem các danh sách có dữ liệu không
        console.log('Kiểm tra dữ liệu sau khi tải:');
        console.log('danhMucList:', danhMucList.value);
        console.log('thuongHieuList:', thuongHieuList.value);
        console.log('chatLieuList:', chatLieuList.value);
        console.log('mauSacList:', mauSacList.value);
        console.log('sizeList:', sizeList.value);
        
        // Cập nhật các options cho autocomplete
        console.log('filteredDanhMucOptions:', filteredDanhMucOptions.value);
        console.log('filteredThuongHieuOptions:', filteredThuongHieuOptions.value);
        console.log('filteredChatLieuOptions:', filteredChatLieuOptions.value);
        
    } catch (error) {
        message.error('Có lỗi khi tải dữ liệu!');
        console.error('Chi tiết lỗi:', error);
    }
});

// Thêm hàm tiện ích để loại bỏ dấu tiếng Việt
const removeDiacriticsForSearch = (str) => {
    if (!str) return '';
    return str.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
};

// Tạo hàm filter chuẩn để áp dụng cho các select khác
const createAdvancedFilterOption = () => {
    return (input, option) => {
        const optionText = option.children.toLowerCase();
        const searchText = input.toLowerCase();
        
        // Tìm kiếm thông thường
        if (optionText.indexOf(searchText) >= 0) {
            return true;
        }
        
        // Tìm kiếm không dấu
        const normalizedOptionText = removeDiacriticsForSearch(optionText);
        const normalizedSearchText = removeDiacriticsForSearch(searchText);
        
        return normalizedOptionText.indexOf(normalizedSearchText) >= 0;
    };
};

// Thêm handler cho auto-complete màu sắc
const onSelectMauSac = (value, typeIndex) => {
    const variantType = variantTypes.value[typeIndex];
    if (!variantType) return;
    
    variantType.id_mau_sac = value;
    // Cập nhật kích thước có sẵn khi thay đổi màu sắc
    updateAvailableSizes(typeIndex);
    // Cập nhật tên màu sắc trong ô tìm kiếm
    const selectedItem = combinedMauSacList.value.find(item => item.id_mau_sac === value);
    if (selectedItem) {
        variantType.mauSacSearch = selectedItem.ten_mau_sac;
    }
};

const onSearchMauSac = (searchText, typeIndex) => {
    const variantType = variantTypes.value[typeIndex];
    if (variantType) {
        variantType.mauSacSearch = searchText;
        
        // Nếu tìm kiếm trống, xóa id_mau_sac
        if (!searchText.trim()) {
            variantType.id_mau_sac = null;
        }
    }
};

// Computed property cho danh sách màu sắc lọc theo từ khóa tìm kiếm
const getFilteredMauSacOptions = (typeIndex) => {
    const variantType = variantTypes.value[typeIndex];
    if (!variantType) return [];
    
    const searchText = variantType.mauSacSearch?.toLowerCase() || '';
    const normalizedSearch = removeDiacriticsForSearch(searchText);
    const availableColors = getAvailableColorsForVariant(typeIndex);
    
    return availableColors
        .filter(item => {
            const tenMauSac = item.ten_mau_sac.toLowerCase();
            const normalizedTenMauSac = removeDiacriticsForSearch(tenMauSac);
            const maMauSac = item.ma_mau_sac.toLowerCase();
            
            return tenMauSac.includes(searchText) || 
                   normalizedTenMauSac.includes(normalizedSearch) ||
                   maMauSac.includes(searchText);
        })
        .map(item => ({
            value: item.id_mau_sac,
            label: item.ten_mau_sac,
            color: item.ma_mau_sac
        }));
};

// Thêm watch cho từng giá trị id_mau_sac trong mỗi variantType
watch(variantTypes, (newTypes) => {
    newTypes.forEach((type, index) => {
        if (type.id_mau_sac && (!type.mauSacSearch || type.mauSacSearch === '')) {
            const selectedItem = combinedMauSacList.value.find(item => item.id_mau_sac === type.id_mau_sac);
            if (selectedItem) {
                type.mauSacSearch = selectedItem.ten_mau_sac;
            }
        }
    });
}, { deep: true });
</script>

<style scoped>
.form-bien-the {
    z-index: 1200;
}

.ant-upload-picture-card-wrapper {
    width: 100%;
}

:deep(.ant-form-item) {
    margin-bottom: 16px;
}

:deep(.ant-form-item-label) {
    text-align: left;
}

:deep(.ant-upload-list-picture-card-container) {
    width: 100px;
    height: 100px;
}

.variant-type-item {
    background-color: #fafafa;
    transition: all 0.3s ease;
}

.variant-type-item:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

:deep(.ant-form-item) {
    margin-bottom: 12px;
}

:deep(.ant-input-number) {
    width: 100%;
}

:deep(.ant-upload-list-picture-card) {
    display: flex;
    gap: 8px;
}

:deep(.ant-upload-list-picture-card-container) {
    width: 100px;
    height: 100px;
    margin: 0 !important;
}

:deep(.ant-upload.ant-upload-select-picture-card) {
    width: 100px;
    height: 100px;
    margin: 0;
}

.text-muted {
    font-size: 14px;
    color: #999;
}

.ant-empty {
    margin: 32px 0;
}

.flex-grow-1 {
    flex-grow: 1;
}

.gap-2 {
    gap: 0.5rem;
}

/* Style cho AutoComplete */
:deep(.attribute-dropdown) {
    max-height: 300px;
    overflow-y: auto;
}

:deep(.attribute-dropdown .ant-select-item) {
    padding: 8px 12px;
}

:deep(.option-item) {
    display: flex;
    align-items: center;
    padding: 4px 0;
}

:deep(.ant-auto-complete-option) {
    transition: all 0.2s;
}

:deep(.ant-auto-complete-option:hover) {
    background-color: #f5f5f5;
}

/* Style cho autocomplete */
:deep(.ant-select-dropdown) {
    border-radius: 6px !important;
    box-shadow: 0 3px 6px -4px rgba(0, 0, 0, 0.12), 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 9px 28px 8px rgba(0, 0, 0, 0.05) !important;
}

:deep(.ant-auto-complete) {
    width: 100%;
}

:deep(.ant-auto-complete .ant-input) {
    border-radius: 6px;
    height: 36px;
    transition: all 0.3s;
}

:deep(.ant-auto-complete .ant-input:hover) {
    border-color: #40a9ff;
}

:deep(.ant-auto-complete .ant-input:focus) {
    border-color: #40a9ff;
    box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

:deep(.ant-select-item-option-content) {
    white-space: normal !important;
    word-break: break-word !important;
}

/* Styling for plus buttons */
:deep(.ant-btn-primary) {
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
    transition: all 0.3s;
    box-shadow: 0 2px 0 rgba(0, 0, 0, 0.045);
}

/* Style for plus buttons next to inputs */
.d-flex.gap-2 .ant-btn-primary {
    width: 32px;
    height: 32px;
    padding: 0;
    border-radius: 4px;
    box-shadow: 0 2px 0 rgba(0, 0, 0, 0.045);
}

.d-flex.gap-2 .ant-btn-primary:hover {
    transform: none;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.d-flex.gap-2 .ant-btn-primary:active {
    transform: none;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.d-flex.gap-2 .ant-btn-primary .anticon {
    font-size: 14px;
}

/* Style for the "Add Variant Type" button */
.d-flex.justify-content-between .ant-btn-primary {
    padding: 0 15px;
    height: 32px;
    min-width: 150px;
    border-radius: 4px;
    box-shadow: none;
}

.d-flex.justify-content-between .ant-btn-primary:hover {
    transform: none;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.d-flex.justify-content-between .ant-btn-primary:active {
    transform: none;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.d-flex.justify-content-between .ant-btn-primary .anticon {
    margin-right: 8px;
    font-size: 14px;
}

/* Match input height */
:deep(.ant-input),
:deep(.ant-select),
:deep(.ant-input-number) {
    height: 32px;
}

:deep(.ant-select-selector) {
    height: 32px !important;
    line-height: 32px !important;
}

:deep(.ant-select-selection-item) {
    line-height: 32px !important;
}

:deep(.ant-input-number-input) {
    height: 32px;
}

/* Rich text editor styles */
.rich-editor {
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    margin-top: 8px;
}

.rich-editor .toolbar {
    padding: 8px;
    border-bottom: 1px solid #f0f0f0;
    background-color: #fafafa;
}

.rich-editor .editor-content {
    padding: 12px;
    min-height: 150px;
    outline: none;
    background-color: #fff;
}

.rich-editor .editor-content:focus {
    border-color: #40a9ff;
    box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

/* Rich text editor styles - enhanced */
.rich-editor {
    border: 1px solid #d9d9d9;
    border-radius: 6px;
    margin-top: 8px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
    overflow: hidden;
}

.rich-editor .toolbar {
    padding: 10px;
    border-bottom: 1px solid #f0f0f0;
    background-color: #fafafa;
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    align-items: center;
}

.rich-editor .editor-content {
    padding: 16px;
    min-height: 180px;
    outline: none;
    background-color: #fff;
    transition: all 0.3s;
    line-height: 1.6;
    font-size: 14px;
}

.rich-editor .editor-content:focus {
    border-color: #40a9ff;
    box-shadow: inset 0 0 3px rgba(24, 144, 255, 0.2);
}

/* Styles for a-select and select multiple */
:deep(.ant-select-selector) {
    border-radius: 6px !important;
    padding: 0 8px !important;
    transition: all 0.3s !important;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05) !important;
}

:deep(.ant-select:hover .ant-select-selector) {
    border-color: #40a9ff !important;
}

:deep(.ant-select-focused .ant-select-selector) {
    border-color: #40a9ff !important;
    box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2) !important;
}

/* Style for multiple select tags */
:deep(.ant-select-multiple .ant-select-selection-item) {
    background-color: #f0f7ff !important;
    border: 1px solid #d6e4ff !important;
    border-radius: 4px !important;
    color: #1890ff !important;
    margin: 2px 4px 2px 0 !important;
    height: 24px !important;
    line-height: 22px !important;
    padding: 0 8px !important;
}

:deep(.ant-select-multiple .ant-select-selection-item-remove) {
    color: #1890ff !important;
    margin-left: 4px !important;
}

:deep(.ant-select-multiple .ant-select-selection-overflow) {
    display: flex !important;
    flex-wrap: wrap !important;
    margin-top: 2px !important;
}

/* Improvements for input numbers */
:deep(.ant-input-number) {
    border-radius: 6px !important;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05) !important;
}

:deep(.ant-input-number:hover) {
    border-color: #40a9ff !important;
}

:deep(.ant-input-number-focused) {
    border-color: #40a9ff !important;
    box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2) !important;
}

/* Improvements for layout and spacing */
.variant-type-item {
    background-color: #fafafa;
    transition: all 0.3s ease;
    border-radius: 8px !important;
    border: 1px solid #f0f0f0 !important;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05) !important;
    padding: 16px !important;
}

.variant-type-item:hover {
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08) !important;
}

/* Base form styles */
:deep(.ant-form-item-label) {
    font-weight: 500 !important;
    padding-bottom: 4px !important;
}

:deep(.ant-form-item-label > label) {
    color: #333 !important;
}

/* Button styling improvements */
:deep(.ant-btn) {
    border-radius: 6px !important;
    font-weight: 500 !important;
    display: inline-flex !important;
    align-items: center !important;
    justify-content: center !important;
}

:deep(.ant-btn-primary) {
    background: linear-gradient(to bottom, #40a9ff, #1890ff) !important;
    border-color: #1890ff !important;
    box-shadow: 0 2px 4px rgba(24, 144, 255, 0.2) !important;
}

:deep(.ant-btn-primary:hover) {
    background: linear-gradient(to bottom, #65bfff, #40a9ff) !important;
    border-color: #40a9ff !important;
    box-shadow: 0 4px 8px rgba(24, 144, 255, 0.3) !important;
}

.size-guide-btn {
    margin-left: 10px;
    background-color: #f0f0f0;
    border: none;
    color: #333;
    padding: 5px 10px;
    text-align: center;
    text-decoration: none;
    display: inline-block;
    font-size: 14px;
    border-radius: 4px;
    transition: background-color 0.3s ease;
}

.size-guide-btn:hover {
    background-color: #e0e0e0;
}

.size-guide-modal {
    max-width: 90vw;
}

.size-guide-content {
    padding: 15px;
}

.size-guide-title {
    font-size: 22px;
    font-weight: 600;
    color: #1890ff;
    margin-bottom: 20px;
    text-align: center;
    border-bottom: 2px solid #f0f0f0;
    padding-bottom: 10px;
}

.size-tables {
    display: flex;
    flex-direction: column;
    gap: 30px;
}

.table-container {
    margin-bottom: 20px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    border-radius: 8px;
    overflow: hidden;
}

.measurement-title {
    font-size: 18px;
    font-weight: 500;
    color: #333;
    margin: 15px 0;
    padding: 0 15px;
}

.size-table {
    width: 100%;
    border-collapse: collapse;
    border: 1px solid #f0f0f0;
    background-color: white;
}

.size-table th {
    background-color: #1890ff;
    color: white;
    font-weight: 600;
    padding: 12px 8px;
    text-align: center;
    border: 1px solid #e8e8e8;
}

.size-table td {
    padding: 10px 8px;
    text-align: center;
    border: 1px solid #e8e8e8;
}

.size-table tr:nth-child(even) {
    background-color: #f9f9f9;
}

.size-table tr:hover {
    background-color: #f5f5f5;
}

.mt-3 {
    margin-top: 15px;
}

@media (max-width: 768px) {
    .size-tables {
        flex-direction: column;
    }

    .size-table {
        font-size: 12px;
    }

    .size-table th,
    .size-table td {
        padding: 6px 4px;
    }
}

/* Style cho editor */
.editor-container {
  height: 300px;
  margin-bottom: 20px;
  border-radius: 6px;
}

.ql-toolbar {
  border-top-left-radius: 6px;
  border-top-right-radius: 6px;
  background-color: #f6f6f6;
  border-color: #d9d9d9;
}

.ql-container {
  border-bottom-left-radius: 6px;
  border-bottom-right-radius: 6px;
  border-color: #d9d9d9;
  min-height: 250px;
}

.ql-editor {
  font-family: 'Roboto', sans-serif;
  font-size: 14px;
  line-height: 1.6;
}

/* Thêm hiệu ứng hover/focus */
.ql-container:hover, .ql-toolbar:hover {
  border-color: #f33b47;
}

/* Style phù hợp với theme của ứng dụng */
.ql-toolbar .ql-stroke {
  stroke: #333;
}

.ql-toolbar .ql-fill {
  fill: #333;
}

.ql-toolbar button:hover .ql-stroke {
  stroke: #f33b47;
}

.ql-toolbar button:hover .ql-fill {
  fill: #f33b47;
}

.ql-toolbar button.ql-active .ql-stroke {
  stroke: #f33b47;
}

.ql-toolbar button.ql-active .ql-fill {
  fill: #f33b47;
}

/* ============ STYLES CHO UI CHỌN ẢNH CHO BIẾN THỂ ============ */
.variants-image-selection {
    margin-top: 12px;
}

.variant-image-card {
    background-color: #fafafa;
    transition: all 0.3s;
}

.variant-image-card:hover {
    background-color: #f5f5f5;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.variant-info {
    padding-bottom: 8px;
    border-bottom: 1px solid #e8e8e8;
}

.image-selection-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 12px;
    margin-top: 12px;
}

.image-selection-item {
    position: relative;
    border-radius: 8px;
    overflow: hidden;
    transition: all 0.3s;
    border: 2px solid transparent;
}

.image-selection-item.selected {
    border-color: #1890ff;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.image-selection-item.primary {
    border-color: #52c41a;
    box-shadow: 0 2px 8px rgba(82, 196, 26, 0.4);
}

.image-selection-item.disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.image-selection-item.disabled .image-wrapper {
    cursor: not-allowed;
}

.image-wrapper {
    position: relative;
    width: 100%;
    padding-top: 100%; /* 1:1 Aspect Ratio */
    cursor: pointer;
    background-color: #fff;
}

.image-wrapper img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.image-checkbox {
    position: absolute;
    top: 8px;
    left: 8px;
    z-index: 10;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 4px;
    padding: 2px;
}

.image-badge {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 4px 8px;
    font-size: 11px;
    font-weight: 600;
    text-align: center;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
}

.primary-badge {
    background: linear-gradient(to top, rgba(82, 196, 26, 0.95), rgba(82, 196, 26, 0.85));
}

.used-badge {
    background: linear-gradient(to top, rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0.6));
}

.primary-selector {
    margin-top: 6px;
    text-align: center;
    font-size: 12px;
}

.primary-selector :deep(.ant-radio-wrapper) {
    font-size: 12px;
}

/* Rich Text Editor styles */
.editor-container {
  height: 300px;
  border-radius: 6px;
  margin-bottom: 16px;
}

:deep(.ql-toolbar) {
  border-top-left-radius: 6px;
  border-top-right-radius: 6px;
  background-color: #f6f6f6;
  border-color: #d9d9d9;
}

:deep(.ql-container) {
  border-bottom-left-radius: 6px;
  border-bottom-right-radius: 6px;
  border-color: #d9d9d9;
  min-height: 250px;
}

:deep(.ql-editor) {
  font-family: 'Roboto', sans-serif;
  font-size: 14px;
  line-height: 1.6;
}

:deep(.ql-container:hover), :deep(.ql-toolbar:hover) {
  border-color: #f33b47;
}

:deep(.ql-toolbar .ql-stroke) {
  stroke: #333;
}

:deep(.ql-toolbar .ql-fill) {
  fill: #333;
}

:deep(.ql-toolbar button:hover .ql-stroke) {
  stroke: #f33b47;
}

:deep(.ql-toolbar button:hover .ql-fill) {
  fill: #f33b47;
}

:deep(.ql-toolbar button.ql-active .ql-stroke) {
  stroke: #f33b47;
}

:deep(.ql-toolbar button.ql-active .ql-fill) {
  fill: #f33b47;
}

/* Thêm vào phần style */
.color-preview {
    display: inline-block;
    width: 18px;
    height: 18px;
    border-radius: 4px;
    margin-right: 10px;
    border: 1px solid #d9d9d9;
    vertical-align: middle;
}

/* Style cho container ảnh biến thể */
.variant-images-container {
    width: 100%;
}

.mb-3 {
    margin-bottom: 12px;
}

/* Style upload disabled */
:deep(.ant-upload.ant-upload-select-picture-card.ant-upload-disabled) {
    background-color: #f5f5f5 !important;
    cursor: not-allowed !important;
    opacity: 0.6;
}

:deep(.ant-upload.ant-upload-select-picture-card.ant-upload-disabled:hover) {
    border-color: #d9d9d9 !important;
}

/* Style cho mỗi ảnh trong upload */
.image-item-wrapper {
    position: relative;
    width: 104px;
    height: 104px;
    border: 2px solid transparent;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    overflow: hidden;
}

.image-item-wrapper:hover {
    border-color: #1890ff;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.image-item-wrapper.is-primary {
    border-color: #52c41a;
    box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);
}

.uploaded-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.image-actions {
    position: absolute;
    top: 4px;
    right: 4px;
    z-index: 10;
}

.remove-btn {
    background: rgba(255, 255, 255, 0.9) !important;
    border-radius: 4px;
    padding: 4px 8px !important;
}

.primary-badge {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(82, 196, 26, 0.95);
    color: white;
    padding: 4px 8px;
    font-size: 12px;
    font-weight: 500;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
}

.hover-hint {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(0, 0, 0, 0.7);
    color: white;
    padding: 4px 8px;
    font-size: 11px;
    text-align: center;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.image-item-wrapper:hover .hover-hint {
    opacity: 1;
}

.image-note {
    margin-top: 8px;
    color: #faad14;
    font-size: 13px;
    display: flex;
    align-items: center;
    gap: 6px;
}

/* Override default ant-design upload styles */
:deep(.ant-upload-list-picture-card-container) {
    width: 104px !important;
    height: 104px !important;
    margin: 0 8px 8px 0 !important;
}

:deep(.ant-upload.ant-upload-select-picture-card) {
    width: 104px !important;
    height: 104px !important;
    margin: 0 8px 8px 0 !important;
}

:deep(.ant-upload-list-picture-card .ant-upload-list-item) {
    padding: 0 !important;
    border: none !important;
}

:deep(.ant-upload-list-picture-card .ant-upload-list-item-info) {
    height: 100% !important;
}

/* CSS ẩn scrollbar trong dropdown */
:deep(.ant-select-dropdown) {
    &::-webkit-scrollbar {
        width: 8px;
    }
    &::-webkit-scrollbar-thumb {
        background-color: #ccc;
        border-radius: 4px;
    }
    &::-webkit-scrollbar-track {
        background-color: #f1f1f1;
    }
}

/* Tùy chỉnh option trong dropdown */
:deep(.ant-select-item) {
    padding: 8px 12px;
}

/* ========== ORANGE THEME OVERRIDES ========== */

/* Orange primary colors */
:deep(.ant-btn-primary),
:deep(.ant-btn-primary:hover),
:deep(.ant-btn-primary:focus) {
    background: #ff6b35 !important;
    border-color: #ff6b35 !important;
    color: #fff !important;
}

:deep(.ant-btn-primary:hover) {
    background: #e55a2b !important;
    border-color: #e55a2b !important;
}

:deep(.ant-btn-primary:active) {
    background: #cc4f27 !important;
    border-color: #cc4f27 !important;
}

/* Orange input borders on focus */
:deep(.ant-input:focus),
:deep(.ant-input-focused) {
    border-color: #ff6b35 !important;
    box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.2) !important;
}

:deep(.ant-select-focused .ant-select-selector),
:deep(.ant-select:focus .ant-select-selector) {
    border-color: #ff6b35 !important;
    box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.2) !important;
}

:deep(.ant-input-number-focused .ant-input-number-input) {
    border-color: #ff6b35 !important;
    box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.2) !important;
}

/* Orange hover effects */
:deep(.ant-auto-complete .ant-input:hover),
:deep(.ant-select:hover .ant-select-selector),
:deep(.ant-input-number:hover) {
    border-color: #ff6b35 !important;
}

/* Quill editor orange theme */
:deep(.ql-container:hover),
:deep(.ql-toolbar:hover),
:deep(.ql-container:focus),
:deep(.ql-toolbar:focus) {
    border-color: #ff6b35 !important;
}

:deep(.ql-toolbar button:hover .ql-stroke) {
    stroke: #ff6b35 !important;
}

:deep(.ql-toolbar button:hover .ql-fill) {
    fill: #ff6b35 !important;
}

:deep(.ql-toolbar button.ql-active .ql-stroke) {
    stroke: #ff6b35 !important;
}

:deep(.ql-toolbar button.ql-active .ql-fill) {
    fill: #ff6b35 !important;
}

/* Orange tabs */
:deep(.ant-tabs-tab:hover) {
    color: #ff6b35 !important;
}

:deep(.ant-tabs-tab.ant-tabs-tab-active) {
    color: #ff6b35 !important;
    border-bottom-color: #ff6b35 !important;
}

:deep(.ant-tabs-ink-bar) {
    background-color: #ff6b35 !important;
}

/* Orange alerts and badges */
:deep(.ant-alert-warning) {
    border-color: #ff6b35 !important;
    background-color: #fff7e6 !important;
}

:deep(.ant-alert-warning .ant-alert-icon) {
    color: #ff6b35 !important;
}

/* Orange modal buttons */
:deep(.ant-modal-footer .ant-btn-primary) {
    background: #ff6b35 !important;
    border-color: #ff6b35 !important;
}

:deep(.ant-modal-footer .ant-btn-primary:hover) {
    background: #e55a2b !important;
    border-color: #e55a2b !important;
}

/* Orange checkboxes and switches */
:deep(.ant-checkbox-checked .ant-checkbox-inner) {
    background: #ff6b35 !important;
    border-color: #ff6b35 !important;
}

:deep(.ant-switch-checked) {
    background: #ff6b35 !important;
    border-color: #ff6b35 !important;
}

/* Orange radio buttons */
:deep(.ant-radio-wrapper:hover .ant-radio-inner) {
    border-color: #ff6b35 !important;
}

:deep(.ant-radio-checked .ant-radio-inner) {
    border-color: #ff6b35 !important;
    background: #fff !important;
}

:deep(.ant-radio-checked .ant-radio-inner::after) {
    background-color: #ff6b35 !important;
}

/* Orange selection colors */
:deep(.ant-select-selection-item) {
    background-color: #fff7e6 !important;
    border-color: #ffd591 !important;
    color: #d46b08 !important;
}

/* Orange table hover */
:deep(.ant-table-tbody > tr:hover > td) {
    background: #fff7e6 !important;
}

/* Orange tag colors */
:deep(.ant-tag) {
    color: #ff6b35 !important;
    border-color: #ff6b35 !important;
}

/* Orange text links */
:deep(.ant-typography a),
:deep(.ant-link) {
    color: #ff6b35 !important;
}

:deep(.ant-typography a:hover),
:deep(.ant-link:hover) {
    color: #e55a2b !important;
}

/* Orange text selection */
::selection {
    background-color: rgba(255, 107, 53, 0.3) !important;
    color: inherit !important;
}

/* Loading spinners orange */
:deep(.ant-spin-nested-loading .ant-spin-container .ant-spin-spinning) {
    color: #ff6b35 !important;
}

:deep(.ant-spin-dot-item) {
    background-color: #ff6b35 !important;
}
</style>