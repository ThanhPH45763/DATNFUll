<template>
    <div>
        <h2>Thêm nhân viên</h2>
        <a-form :model="formData" @finish="themNhanVien" layout="vertical" class="nhvform">
            <div class="container">
                <a-row :gutter="24">
                    <!-- Phần ảnh đại diện -->
                    <a-col :span="8">
                        <div class="avatar-label">
                            <label>Ảnh đại diện</label>
                        </div>
                        <div class="avatar-container d-flex justify-content-center">
                            <div class="avatar-upload">
                                <div class="avatar-preview" @click="triggerFileInput">
                                    <img :src="previewImage || defaultAvatar" alt="Avatar" class="rounded-circle"
                                        :class="{ 'uploading': isUploading }">
                                    <button v-if="previewImage && !isUploading" @click.stop="removeImage"
                                        class="delete-btn" type="button">
                                        ×
                                    </button>
                                    <div v-if="isUploading" class="upload-indicator">
                                        <i class="fas fa-spinner fa-spin"></i>
                                    </div>
                                    <div v-else class="camera-icon">
                                        <i class="fas fa-camera"></i>
                                    </div>
                                </div>
                                <input type="file" id="imageUpload" accept=".png, .jpg, .jpeg"
                                    @change="handleImageChange" class="d-none" ref="fileInput">
                                <small v-if="formData.anhNhanVien" class="text-success d-block mt-2">
                                    <i class="fas fa-check-circle"></i> Ảnh đã được tải lên
                                </small>
                            </div>
                        </div>
                    </a-col>

                    <!-- Form nhập liệu bên phải -->
                    <a-col :span="16">
                        <a-row :gutter="16">
                            <!-- Mã nhân viên -->
                            <a-col :span="12">
                                <a-form-item label="Mã nhân viên" class="form-item-custom">
                                    <a-input v-model:value="formData.maNhanVien" disabled />
                                </a-form-item>
                            </a-col>

                            <!-- Tên nhân viên -->
                            <a-col :span="12">
                                <a-form-item label="Tên nhân viên" class="form-item-custom"
                                    :validateStatus="errors.tenNhanVien.length ? 'error' : ''" 
                                    :help="errors.tenNhanVien.join(', ')">
                                    <a-input v-model:value="formData.tenNhanVien" placeholder="Nhập tên nhân viên" />
                                </a-form-item>
                            </a-col>
                        </a-row>

                        <a-row :gutter="16">
                            <!-- Giới tính -->
                            <a-col :span="12">
                                <a-form-item label="Giới tính" class="form-item-custom"
                                    :validateStatus="errors.gioiTinh.length ? 'error' : ''" :help="errors.gioiTinh.join(', ')">
                                    <a-radio-group v-model:value="formData.gioiTinh">
                                        <a-radio :value="true">Nam</a-radio>
                                        <a-radio :value="false">Nữ</a-radio>
                                    </a-radio-group>
                                </a-form-item>
                            </a-col>

                            <!-- Ngày sinh -->
                            <a-col :span="12">
                                <a-form-item label="Ngày sinh" class="form-item-custom"
                                    :validateStatus="errors.ngaySinh.length ? 'error' : ''" :help="errors.ngaySinh.join(', ')">
                                    <a-date-picker v-model:value="formData.ngaySinh" format="DD-MM-YYYY"
                                        style="width: 100%" placeholder="Chọn ngày sinh" :valueFormat="'DD-MM-YYYY'" />
                                </a-form-item>
                            </a-col>
                        </a-row>

                        <!-- Số điện thoại và Email -->
                        <a-row :gutter="16">
                            <a-col :span="12">
                                <a-form-item label="Số điện thoại" class="form-item-custom"
                                    :validateStatus="errors.soDienThoai.length ? 'error' : ''" :help="errors.soDienThoai.join(', ')">
                                    <a-input v-model:value="formData.soDienThoai" placeholder="Nhập số điện thoại" />
                                </a-form-item>
                            </a-col>

                            <a-col :span="12">
                                <a-form-item label="Email" class="form-item-custom"
                                    :validateStatus="errors.email.length ? 'error' : ''" :help="errors.email.join(', ')">
                                    <a-input v-model:value="formData.email" placeholder="Nhập email" />
                                </a-form-item>
                            </a-col>
                        </a-row>
                    </a-col>
                </a-row>

                <a-row :gutter="16">
                    <!-- Tỉnh/Thành phố -->
                    <a-col :span="8">
                        <a-form-item label="Tỉnh/Thành phố" class="form-item-custom"
                            :validateStatus="errors.selectedProvince.length ? 'error' : ''" :help="errors.selectedProvince.join(', ')">
                            <a-select v-model:value="selectedProvince" show-search placeholder="Chọn Tỉnh/Thành phố"
                                :options="provinceOptions" :filter-option="filterOption" @change="handleProvinceChange"
                                allowClear></a-select>
                        </a-form-item>
                    </a-col>

                    <!-- Quận/Huyện -->
                    <a-col :span="8">
                        <a-form-item label="Quận/Huyện" class="form-item-custom"
                            :validateStatus="errors.selectedDistrict.length ? 'error' : ''" :help="errors.selectedDistrict.join(', ')">
                            <a-select v-model:value="selectedDistrict" show-search placeholder="Chọn Quận/Huyện"
                                :options="districtOptions" :filter-option="filterOption" :disabled="!selectedProvince"
                                @change="handleDistrictChange" allowClear></a-select>
                        </a-form-item>
                    </a-col>

                    <!-- Phường/Xã -->
                    <a-col :span="8">
                        <a-form-item label="Phường/Xã" class="form-item-custom"
                            :validateStatus="errors.selectedWard.length ? 'error' : ''" :help="errors.selectedWard.join(', ')">
                            <a-select v-model:value="selectedWard" show-search placeholder="Chọn Phường/Xã"
                                :options="wardOptions" :filter-option="filterOption" :disabled="!selectedDistrict"
                                allowClear></a-select>
                        </a-form-item>
                    </a-col>

                    <!-- Địa chỉ cụ thể -->
                    <a-col :span="24">
                        <a-form-item label="Địa chỉ cụ thể" class="form-item-custom"
                            :validateStatus="errors.diaChiLienHe.length ? 'error' : ''" :help="errors.diaChiLienHe.join(', ')">
                            <a-input v-model:value="formData.diaChiLienHe" placeholder="Số nhà, tên đường..." />
                        </a-form-item>
                    </a-col>
                </a-row>
            </div>

            <!-- Buttons -->
            <div class="form-actions">
                <a-button html-type="submit" :loading="isUploading" class="action-btn create-btn">
                    Tạo tài khoản
                </a-button>
                <a-button @click="resetForm" :disabled="isUploading" class="action-btn reset-btn">
                    Làm mới
                </a-button>
            </div>
        </a-form>
    </div>
</template>


<script setup>
import { ref, onMounted, reactive } from 'vue';
//Import service ảnh
import axiosInstance from '@/config/axiosConfig';
import { message, Modal, Form, Input, Select, DatePicker, Radio, Button, Row, Col, Divider } from 'ant-design-vue';
import { useGbStore } from '@/stores/gbStore';
import { toast } from 'vue3-toastify';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import { nhanVienService } from '@/services/nhanVienService';
//--------------------------------------
// Khai báo biến cho ảnh
const store = useGbStore();
const previewImage = ref(null);
const fileInput = ref(null);
const selectedFile = ref(null);
const defaultAvatar = ref('https://static.vecteezy.com/system/resources/previews/019/879/186/non_2x/user-icon-on-transparent-background-free-png.png');
const isUploading = ref(false); // Add loading state for image upload
const router = useRouter();

// Form data
const formData = reactive({
    maNhanVien: '',
    tenNhanVien: '',
    gioiTinh: null,
    ngaySinh: '',
    soDienThoai: '',
    email: '',
    diaChiLienHe: '',
    anhNhanVien: null,
    trangThai: 'Đang hoạt động'
});

// Địa chỉ
const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);
const selectedProvince = ref('');
const selectedDistrict = ref('');
const selectedWard = ref('');

// Khai báo options cho select
const provinceOptions = ref([]);
const districtOptions = ref([]);
const wardOptions = ref([]);

// Thêm state cho danh sách nhân viên
const adminStaffList = ref([]);

// Hàm filter cho search
const filterOption = (input, option) => {
    return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};

// Thêm hàm chuẩn hóa chuỗi với các chế độ khác nhau
const normalizeString = (str, mode = 'default') => {
    if (!str) return '';
    
    switch (mode) {
        case 'phone':
            // Xóa tất cả khoảng trắng trong số điện thoại
            return str.replace(/\s+/g, '');
            
        case 'email':
            // Xóa tất cả khoảng trắng trong email
            return str.replace(/\s+/g, '');
            
        default:
            // Chế độ mặc định: xóa khoảng trắng đầu cuối và thay thế nhiều khoảng trắng liên tiếp bằng một khoảng trắng
            return str.trim().replace(/\s+/g, ' ');
    }
};



const validateForm = () => {
    let isValid = true;

    // Reset tất cả lỗi
    Object.keys(errors).forEach(key => {
        errors[key] = [];
    });

    // Chuẩn hóa dữ liệu trước khi validate
    formData.tenNhanVien = normalizeString(formData.tenNhanVien);
    formData.soDienThoai = normalizeString(formData.soDienThoai, 'phone');
    formData.email = normalizeString(formData.email, 'email');
    formData.diaChiLienHe = normalizeString(formData.diaChiLienHe);

    // Check tên nhân viên
    if (!formData.tenNhanVien) {
        errors.tenNhanVien.push('Vui lòng nhập tên nhân viên');
        isValid = false;
    } else {
        if (formData.tenNhanVien.length < 2 || formData.tenNhanVien.length > 100) {
            errors.tenNhanVien.push('Tên nhân viên phải từ 2 đến 100 ký tự');
            isValid = false;
        }
        if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]+/.test(formData.tenNhanVien)) {
            errors.tenNhanVien.push('Tên nhân viên không được chứa ký tự đặc biệt');
            isValid = false;
        }
        if (/[0-9]/.test(formData.tenNhanVien)) {
            errors.tenNhanVien.push('Tên nhân viên không được chứa số');
            isValid = false;
        }
    }

    // Check giới tính
    if (formData.gioiTinh === null || formData.gioiTinh === undefined || formData.gioiTinh === '') {
        errors.gioiTinh.push('Vui lòng chọn giới tính');
        isValid = false;
    }

    // Check ngày sinh
    if (!formData.ngaySinh) {
        errors.ngaySinh.push('Vui lòng chọn ngày sinh');
        isValid = false;
    } else {
        const ngaySinh = dayjs(formData.ngaySinh, 'DD-MM-YYYY');
        const today = dayjs();
        
        // Kiểm tra ngày trong tương lai
        if (ngaySinh.isAfter(today)) {
            errors.ngaySinh.push('Ngày sinh không được là ngày trong tương lai');
            isValid = false;
        }
        
        // Tính tuổi
        const tuoi = today.diff(ngaySinh, 'year');
        
        if (tuoi < 18) {
            errors.ngaySinh.push('Nhân viên phải đủ 18 tuổi');
            isValid = false;
        }
        
        if (tuoi > 60) {
            errors.ngaySinh.push('Nhân viên không được quá 60 tuổi');
            isValid = false;
        }
    }

    // Check email
    if (!formData.email) {
        errors.email.push('Vui lòng nhập email');
        isValid = false;
    } else {
        if (formData.email.length < 6 || formData.email.length > 320) {
            errors.email.push('Email phải có độ dài từ 6 đến 320 ký tự');
            isValid = false;
        }
        if (/[\(\),;:<>\[\]"\\\s]/.test(formData.email)) {
            errors.email.push('Email không được chứa các ký tự đặc biệt như (), , : ; < > [ ] " \\ và khoảng trắng');
            isValid = false;
        }
        if (!validateEmail(formData.email)) {
            errors.email.push('Email không hợp lệ (VD: example@gmail.com)');
            isValid = false;
        }
        if (adminStaffList.value.some(nv => 
            normalizeString(nv.email, 'email') === formData.email)) {
            errors.email.push('Email này đã tồn tại trong hệ thống');
            isValid = false;
        }
    }

    // Check số điện thoại
    if (!formData.soDienThoai) {
        errors.soDienThoai.push('Vui lòng nhập số điện thoại');
        isValid = false;
    } else {
        if (!validatePhoneNumber(formData.soDienThoai)) {
            errors.soDienThoai.push('Số điện thoại không hợp lệ (VD: 0912345678)');
            isValid = false;
        }
        if (adminStaffList.value.some(nv => 
            normalizeString(nv.soDienThoai, 'phone') === formData.soDienThoai)) {
            errors.soDienThoai.push('Số điện thoại này đã tồn tại trong hệ thống');
            isValid = false;
        }
    }

    // Check địa chỉ
    if (!formData.diaChiLienHe) {
        errors.diaChiLienHe.push('Vui lòng nhập địa chỉ cụ thể');
        isValid = false;
    } else {
        if (formData.diaChiLienHe.length < 2 || formData.diaChiLienHe.length > 100) {
            errors.diaChiLienHe.push('Địa chỉ phải từ 2 đến 100 ký tự');
            isValid = false;
        }
        if (/[@$!^%&*<>{}]/.test(formData.diaChiLienHe)) {
            errors.diaChiLienHe.push('Địa chỉ không được chứa các ký tự đặc biệt (@, $, !, ^, %, &, *, <, >, {, })');
            isValid = false;
        }
        if (!/[^0-9]/.test(formData.diaChiLienHe)) {
            errors.diaChiLienHe.push('Địa chỉ không được chứa toàn số');
            isValid = false;
        }
    }

    // Check các trường địa chỉ hành chính
    if (!selectedProvince.value) {
        errors.selectedProvince.push('Vui lòng chọn Tỉnh/Thành phố');
        isValid = false;
    }
    if (!selectedDistrict.value && selectedProvince.value) {
        errors.selectedDistrict.push('Vui lòng chọn Quận/Huyện');
        isValid = false;
    }
    if (!selectedWard.value && selectedDistrict.value) {
        errors.selectedWard.push('Vui lòng chọn Phường/Xã');
        isValid = false;
    }

    if (!isValid) {
        toast.error('Vui lòng điền đầy đủ và chính xác thông tin!');
        console.log('Các trường có lỗi:', Object.entries(errors).filter(([_, value]) => value.length > 0));
    }

    return isValid;
};

// Cập nhật hàm validatePhoneNumber
const validatePhoneNumber = (phone) => {
    // Chuẩn hóa số điện thoại trước khi validate
    const normalizedPhone = normalizeString(phone, 'phone');
    // Số điện thoại Việt Nam: bắt đầu bằng 0, theo sau là 9 hoặc 10 chữ số
    const regex = /^0\d{9,10}$/;
    return regex.test(normalizedPhone);
};

// Cập nhật hàm validateEmail
const validateEmail = (email) => {
    // Chuẩn hóa email trước khi validate
    const normalizedEmail = normalizeString(email, 'email');
    if (normalizedEmail.length < 6 || normalizedEmail.length > 320) {
        return false;
    }
    const hasInvalidChar = /[\(\),;:<>\[\]"\\\s]/.test(normalizedEmail);
    if (hasInvalidChar) {
        return false;
    }
    const emailRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
    return emailRegex.test(normalizedEmail);
};

// Tính tuổi
const calculateAge = (birthdate) => {
    const today = new Date();
    // Convert dayjs to native Date if needed
    const birthDate = birthdate instanceof dayjs ? birthdate.toDate() : new Date(birthdate);
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
        age--;
    }
    return age;
};

// Lỗi
const errors = reactive({
    tenNhanVien: [],
    gioiTinh: [],
    ngaySinh: [],
    soDienThoai: [],
    email: [],
    selectedProvince: [],
    selectedDistrict: [],
    selectedWard: [],
    diaChiLienHe: []
});

// Xử lý ảnh
const triggerFileInput = () => {
    fileInput.value.click();
};

const handleImageChange = (event) => {
    const file = event.target.files[0];
    if (file) {
        if (validateImage(file)) {
            selectedFile.value = file;
            const reader = new FileReader();
            reader.onload = (e) => {
                previewImage.value = e.target.result;
                // Upload image to cloud when selected
                uploadImageToCloud(file);
            };
            reader.readAsDataURL(file);
        } else {
            toast.error('Vui lòng chọn file ảnh có định dạng jpg, jpeg, png và kích thước dưới 5MB');
            fileInput.value.value = '';
        }
    }
};

const validateImage = (file) => {
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png'];
    const maxSize = 5 * 1024 * 1024; // 5MB
    return validTypes.includes(file.type) && file.size <= maxSize;
};

// Uploads image to cloud service and updates formData with the URL
const uploadImageToCloud = async (file) => {
    if (!file) {
        return;
    }

    try {
        // Show loading indicator
        isUploading.value = true;
        const toastId = toast.loading('Đang tải ảnh lên...');

        const formDataUpload = new FormData();
        formDataUpload.append('file', file);

        const response = await axiosInstance.post('testImage', formDataUpload, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });

        if (response.data) {
            // Update formData with cloud image URL
            formData.anhNhanVien = response.data;
            toast.update(toastId, {
                render: 'Tải ảnh thành công',
                type: 'success',
                isLoading: false,
                autoClose: 2000
            });
            console.log('Uploaded image URL:', response.data);
        } else {
            toast.update(toastId, {
                render: 'Không thể tải ảnh lên',
                type: 'error',
                isLoading: false,
                autoClose: 2000
            });
        }
    } catch (error) {
        console.error('Upload error:', error);
        toast.error('Có lỗi khi tải ảnh lên: ' + (error.response?.data || error.message));
    } finally {
        isUploading.value = false;
    }
};

const removeImage = (e) => {
    e.stopPropagation();

    // Delete image from cloud if we have a URL
    if (formData.anhNhanVien) {
        deleteImageFromCloud(formData.anhNhanVien);
    }

    // Reset all image related values
    previewImage.value = null;
    selectedFile.value = null;
    formData.anhNhanVien = null;

    if (fileInput.value) {
        fileInput.value.value = '';
    }
};

// Delete image from cloud service
const deleteImageFromCloud = async (imageUrl) => {
    if (!imageUrl) return;

    try {
        // Lấy publicId từ URL
        const urlParts = imageUrl.split('/');
        const fileName = urlParts[urlParts.length - 1];
        const publicId = fileName.split('.')[0]; // Lấy phần trước .jpg hoặc .png

        // Gọi API xóa ảnh
        await axiosInstance.delete("testDeleteImage?publicId=" + publicId);
        console.log('Đã xóa ảnh từ cloud');
    } catch (error) {
        console.error('Error deleting image:', error);
        // We don't show error toast here to avoid confusing the user
    }
};

// API địa chỉ
const loadProvinces = async () => {
    try {
        const response = await fetch('https://provinces.open-api.vn/api/p/');
        const data = await response.json();
        provinces.value = data;
        // Chuyển đổi format cho a-select
        provinceOptions.value = data.map(province => ({
            value: province.code,
            label: province.name
        }));
    } catch (error) {
        console.error('Lỗi khi tải tỉnh/thành:', error);
    }
};

const handleProvinceChange = async (value) => {
    if (value) {
        try {
            const response = await fetch(`https://provinces.open-api.vn/api/p/${value}?depth=2`);
            const data = await response.json();
            districts.value = data.districts;
            // Chuyển đổi format cho a-select
            districtOptions.value = data.districts.map(district => ({
                value: district.code,
                label: district.name
            }));
            selectedDistrict.value = undefined;
            selectedWard.value = undefined;
            wardOptions.value = [];
        } catch (error) {
            console.error('Lỗi khi tải quận/huyện:', error);
        }
    } else {
        districts.value = [];
        districtOptions.value = [];
        selectedDistrict.value = undefined;
        wardOptions.value = [];
        selectedWard.value = undefined;
    }
};

const handleDistrictChange = async (value) => {
    if (value) {
        try {
            const response = await fetch(`https://provinces.open-api.vn/api/d/${value}?depth=2`);
            const data = await response.json();
            wards.value = data.wards;
            // Chuyển đổi format cho a-select
            wardOptions.value = data.wards.map(ward => ({
                value: ward.code,
                label: ward.name
            }));
            selectedWard.value = undefined;
        } catch (error) {
            console.error('Lỗi khi tải phường/xã:', error);
        }
    } else {
        wards.value = [];
        wardOptions.value = [];
        selectedWard.value = undefined;
    }
};

// Reset form
const resetForm = () => {
    Modal.confirm({
        title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
            h(ExclamationCircleOutlined, { style: 'color: #faad14; font-size: 22px;' }),
            h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Làm mới form')
        ]),
        content: () => h('div', { style: 'padding: 8px 0;' }, [
            h('p', { style: 'margin: 0 0 12px 0; font-size: 14px;' }, 'Bạn có chắc muốn làm mới form?'),
            h('div', { style: 'background: #fffbe6; padding: 12px; border-radius: 6px; border: 1px solid #ffe58f;' }, [
                h('div', { style: 'display: flex; align-items: center; gap: 8px; color: #d48806;' }, [
                    h(ExclamationCircleOutlined, { style: 'font-size: 14px;' }),
                    h('span', { style: 'font-size: 13px;' }, 'Tất cả dữ liệu đã nhập sẽ bị xóa')
                ])
            ])
        ]),
        okText: 'Làm mới',
        cancelText: 'Hủy',
        okButtonProps: { danger: true, size: 'large', style: { height: '38px' } },
        cancelButtonProps: { size: 'large', style: { height: '38px' } },
        centered: true,
        width: 450,
        onOk: async () => {
            try {
                Object.assign(formData, {
                    tenNhanVien: '',
                    gioiTinh: null,
                    ngaySinh: '',
                    soDienThoai: '',
                    email: '',
                    diaChiLienHe: '',
                    anhNhanVien: null,
                    trangThai: 'Đang hoạt động'
                });

                // Reset ảnh
                if (formData.anhNhanVien) {
                    // Attempt to delete the image from cloud if there was one
                    deleteImageFromCloud(formData.anhNhanVien);
                }

                previewImage.value = null;
                selectedFile.value = null;
                if (fileInput.value) {
                    fileInput.value.value = '';
                }

                // Reset địa chỉ
                selectedProvince.value = '';
                selectedDistrict.value = '';
                selectedWard.value = '';
                districts.value = [];
                wards.value = [];

                // Clear any error messages
                Object.keys(errors).forEach(key => {
                    errors[key] = [];
                });

                message.success('Đã làm mới form');
            } catch (error) {
                console.error('Lỗi khi reset form:', error);
                message.error('Có lỗi xảy ra khi làm mới form');
            }
        }
    });
};

const taoMaNhanVienMoi = (nhanVienArr) => {
    if (!nhanVienArr || nhanVienArr.length === 0) {
        return 'NV01';
    }

    const nhanVienMoi = nhanVienArr
        .map(nv => nv.maNhanVien)
        .filter(code => code.startsWith('NV'))
        .map(code => parseInt(code.substring(2)) || 0);

    const maxNumber = Math.max(...nhanVienMoi);
    const nextNumber = maxNumber + 1;

    return `NV${String(nextNumber).padStart(2, '0')}`;
};
//--------------------------------------

const themNhanVien = async () => {
    if (isUploading.value) {
        toast.warning('Vui lòng đợi tải ảnh lên hoàn tất');
        return;
    }

    if (validateForm()) {
        Modal.confirm({
            title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
                h(UserAddOutlined, { style: 'color: #52c41a; font-size: 22px;' }),
                h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Thêm nhân viên mới')
            ]),
            content: () => h('div', { style: 'padding: 8px 0;' }, [
                h('p', { style: 'margin: 0 0 12px 0; font-size: 14px;' }, 'Bạn có chắc chắn muốn thêm nhân viên này không?'),
                h('div', { style: 'background: #f6ffed; padding: 12px; border-radius: 6px; border: 1px solid #b7eb8f;' }, [
                    h('div', { style: 'color: #389e0d; font-size: 13px;' }, [
                        h('div', { style: 'font-weight: 500; margin-bottom: 4px;' }, 'Thông tin nhân viên:'),
                        h('div', null, `• Tên: ${formData.tenNhanVien}`),
                        h('div', null, `• SĐT: ${formData.soDienThoai}`),
                        h('div', null, `• Email: ${formData.email}`)
                    ])
                ])
            ]),
            okText: 'Thêm nhân viên',
            cancelText: 'Hủy',
            okButtonProps: { size: 'large', style: { height: '38px', background: '#52c41a', borderColor: '#52c41a' } },
            cancelButtonProps: { size: 'large', style: { height: '38px' } },
            centered: true,
            width: 480,
            onOk: async () => {
                try {
                    const province = provinces.value.find(p => p.code === selectedProvince.value)?.name || '';
                    const district = districts.value.find(d => d.code === selectedDistrict.value)?.name || '';
                    const ward = wards.value.find(w => w.code === selectedWard.value)?.name || '';

                    // Chuyển đổi định dạng ngày từ DD-MM-YYYY sang YYYY-MM-DD cho API
                    let formattedDate = '';
                    if (formData.ngaySinh) {
                        // Parse ngày từ định dạng DD-MM-YYYY
                        const parsedDate = dayjs(formData.ngaySinh, 'DD-MM-YYYY');
                        // Format lại thành YYYY-MM-DD cho API
                        formattedDate = parsedDate.format('YYYY-MM-DD');
                    }

                    const nhanVienMoi = {
                        ...formData,
                        ngaySinh: formattedDate, // Sử dụng định dạng YYYY-MM-DD cho API
                        diaChiLienHe: `${formData.diaChiLienHe}, ${ward}, ${district}, ${province}`.trim()
                    };

                    console.log('Dữ liệu truyền vào mới thêm nhân viên', nhanVienMoi);
                    const themNhanVien = await store.themNhanVien(nhanVienMoi);
                    if (themNhanVien.error) {
                        toast.error('Có lỗi xảy ra');
                        console.log(themNhanVien.error);
                    } else {
                        toast.success('Thêm nhân viên thành công');
                        setTimeout(() => {
                            console.log('Chuyển trang...');
                            window.location.href = '/admin/quanlynhanvien';
                        }, 1000);
                    }
                } catch (error) {
                    console.error(error);
                }
            },
            onCancel() {
                console.log('Đã hủy thêm nhân viên');
            }
        });
    }
}

const getAdminStaffList = async () => {
    try {
        const response = await axiosInstance.get('admin/quan-ly-nhan-vien/listTrangAdmin');
        adminStaffList.value = response.data;
        console.log('Danh sách nhân viên admin:', adminStaffList.value);
    } catch (error) {
        console.error('Lỗi khi lấy danh sách nhân viên admin:', error);
    }
};

onMounted(async () => {
    try {
        // Load provinces
        await loadProvinces();

        // Thêm: Lấy danh sách nhân viên từ API mới để check trùng
        await getAdminStaffList();

        // Giữ nguyên phần còn lại
        await store.layDanhSachNhanVien();
        formData.maNhanVien = taoMaNhanVienMoi(store.nhanVienArr);

        console.log('Khởi tạo mã nhân viên:', formData.maNhanVien);
    } catch (error) {
        console.error('Lỗi khởi tạo:', error);
        toast.error('Có lỗi khi khởi tạo dữ liệu');
    }
});
</script>

<style scoped>
.avatar-container {
    display: flex;
    align-items: flex-end;
}

.avatar-label {
    font-weight: 500;
    color: #333;
    padding-top: 10px;
}

.avatar-upload {
    text-align: center;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 8px;
    width: fit-content;
}

.avatar-preview {
    width: 150px;
    height: 150px;
    position: relative;
    margin: 0 auto;
    cursor: pointer;
    background: #fff;
    border-radius: 50%;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.avatar-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border: 3px solid #fff;
    border-radius: 50%;
    background-color: #fff;
    transition: all 0.3s ease;
}

.avatar-preview img.uploading {
    opacity: 0.6;
    filter: blur(1px);
}

/* Upload indicator */
.upload-indicator {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.3);
    z-index: 3;
}

.upload-indicator i {
    font-size: 32px;
    color: white;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

/* Style cho nút xóa */
.delete-btn {
    position: absolute;
    top: 0;
    right: 0;
    width: 25px;
    height: 25px;
    background-color: #000000;
    color: white;
    border: 2px solid #fff;
    border-radius: 50%;
    font-size: 18px;
    line-height: 1;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: all 0.2s;
    z-index: 2;
    border: 2px solid #000000;
}

/* Icon camera khi hover */
.camera-icon {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 30px;
    opacity: 0;
    transition: all 0.2s;
}

/* Hiệu ứng hover */
.avatar-preview:hover .camera-icon {
    opacity: 1;
}

.avatar-preview:hover .delete-btn {
    opacity: 1;
}

.delete-btn:hover {
    transform: scale(1.1);
    background-color: white;
    color: black;
}

/* Cải thiện căn chỉnh form */
.nhvform {
    max-width: 1300px;
    margin: 0 auto;
    padding: 0 15px;
}

.form-item-custom {
    margin-bottom: 20px;
}

.form-actions {
    display: flex;
    padding-top: 24px;
    margin-top: 16px;
    border-top: 1px solid #f0f0f0;
}

.action-btn {
    margin-right: 12px;
    min-width: 150px;
    height: 40px;
    font-size: 16px;
}

/* Điều chỉnh các select & input để căn đều */
:deep(.ant-select) {
    width: 100%;
}

:deep(.ant-select-selector) {
    height: 40px !important;
    padding: 4px 15px !important;
    display: flex;
    align-items: center;
}

:deep(.ant-input),
:deep(.ant-picker),
:deep(.ant-select-selector),
:deep(.ant-radio-wrapper) {
    font-size: 15px;
}

:deep(.ant-input) {
    height: 40px;
    padding: 6px 15px;
}

:deep(.ant-picker) {
    height: 40px;
    padding: 6px 15px;
}

:deep(.ant-form-item-label) {
    padding-bottom: 6px;
}

:deep(.ant-form-item-label > label) {
    font-weight: 500;
    font-size: 15px;
}

:deep(.ant-divider) {
    margin: 24px 0;
    font-weight: 500;
    font-size: 16px;
}

:deep(.ant-select-selection-item) {
    line-height: 32px;
}

:deep(.ant-radio-wrapper) {
    font-size: 15px;
    height: 40px;
    line-height: 40px;
}

:deep(.ant-form-item-control-input-content) {
    min-height: 40px;
    display: flex;
    align-items: center;
}

/* Tùy chỉnh màu sắc cho các thành phần form */
:deep(.ant-input:hover),
:deep(.ant-input:focus),
:deep(.ant-picker:hover),
:deep(.ant-picker-focused),
:deep(.ant-select:hover .ant-select-selector),
:deep(.ant-select-focused .ant-select-selector),
:deep(.ant-select-open .ant-select-selector) {
    border-color: #f33b47 !important;
    box-shadow: 0 0 0 2px rgba(243, 59, 71, 0.2) !important;
}

:deep(.ant-input:focus) {
    border-color: #f33b47 !important;
    box-shadow: 0 0 0 2px rgba(243, 59, 71, 0.2) !important;
}

:deep(.ant-radio-checked .ant-radio-inner) {
    border-color: #f33b47 !important;
    background-color: #f33b47 !important;
}

:deep(.ant-radio:hover .ant-radio-inner) {
    border-color: #f33b47 !important;
}

:deep(.ant-form-item-has-error .ant-input),
:deep(.ant-form-item-has-error .ant-input:hover),
:deep(.ant-form-item-has-error .ant-select-selector),
:deep(.ant-form-item-has-error .ant-select-selector:hover) {
    border-color: #ff4d4f !important;
}

/* Nút tạo tài khoản với màu riêng */
.create-btn {
    background-color: #f33b47;
    border-color: #f33b47;
    color: white;
}

.create-btn:hover,
.create-btn:focus {
    background-color: #ff5060;
    border-color: #ff5060;
    color: white;
}

.create-btn:active {
    background-color: #d63040;
    border-color: #d63040;
}

/* Nút làm mới với hiệu ứng hover */
.reset-btn:hover,
.reset-btn:focus {
    border-color: #f33b47;
    color: #f33b47;
}

.reset-btn:active {
    border-color: #d63040;
    color: #d63040;
}

/* Đảm bảo placeholder hiển thị đúng cho select */
:deep(.ant-select-placeholder) {
    color: #bfbfbf;
    opacity: 1 !important;
}

:deep(.ant-select-selection-placeholder) {
    opacity: 1 !important;
    color: #bfbfbf;
}
</style>