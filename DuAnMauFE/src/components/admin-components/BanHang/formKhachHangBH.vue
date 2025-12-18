<template>
    <div class="customer-form-wrapper">
        <!-- Header with icon -->
        <div class="form-header">
            <team-outlined class="header-icon" />
            <h2 class="header-title">Thông tin khách hàng</h2>
        </div>

        <form @submit.prevent="themKhachHang" @reset.prevent="resetForm" class="customer-form">
            <a-form :model="formData" :label-col="{ span: 24 }" :wrapper-col="{ span: 24 }">
                <!-- Thông tin cơ bản -->
                <div class="form-section">
                    <a-row :gutter="16">
                        <a-col :span="8">
                            <a-form-item label="Họ tên khách hàng" :validate-status="errors.tenKhachHang ? 'error' : ''"
                                :help="errors.tenKhachHang">
                                <a-input v-model:value="formData.tenKhachHang" placeholder="Nhập tên khách hàng"
                                    class="custom-input" size="large" />
                            </a-form-item>
                        </a-col>

                        <a-col :span="8">
                            <a-form-item label="Số điện thoại" :validate-status="errors.soDienThoai ? 'error' : ''"
                                :help="errors.soDienThoai">
                                <a-input v-model:value="formData.soDienThoai" placeholder="Nhập số điện thoại"
                                    class="custom-input" size="large" />
                            </a-form-item>
                        </a-col>

                        <a-col :span="8">
                            <a-form-item label="Email" :validate-status="errors.email ? 'error' : ''"
                                :help="errors.email">
                                <a-input v-model:value="formData.email" placeholder="Nhập email" class="custom-input"
                                    size="large" />
                            </a-form-item>
                        </a-col>
                    </a-row>
                </div>

                <!-- Danh sách địa chỉ -->
                <div v-for="(diaChi, index) in formData.diaChiList" :key="index" class="address-card">
                    <div class="address-card-header">
                        <environment-outlined class="address-icon" />
                        <span class="address-title">Địa chỉ giao hàng</span>
                    </div>

                    <a-row :gutter="16">
                        <a-col :span="6">
                            <a-form-item label="Tỉnh/Thành phố"
                                :validate-status="errors.diaChiErrors[index]?.tinhThanhPho ? 'error' : ''"
                                :help="errors.diaChiErrors[index]?.tinhThanhPho">
                                <a-select v-model:value="diaChi.tinhThanhPho" placeholder="Chọn Tỉnh/Thành phố"
                                    class="custom-select" size="large" @change="() => handleProvinceChange(index)">
                                    <a-select-option v-for="province in provinces" :key="province.code"
                                        :value="province.name">
                                        {{ province.name }}
                                    </a-select-option>
                                </a-select>
                            </a-form-item>
                        </a-col>

                        <a-col :span="6">
                            <a-form-item label="Quận/Huyện"
                                :validate-status="errors.diaChiErrors[index]?.quanHuyen ? 'error' : ''"
                                :help="errors.diaChiErrors[index]?.quanHuyen">
                                <a-select v-model:value="diaChi.quanHuyen" placeholder="Chọn Quận/Huyện"
                                    class="custom-select" size="large" :disabled="!diaChi.tinhThanhPho"
                                    @change="() => handleDistrictChange(index)">
                                    <a-select-option v-for="district in districts[index]" :key="district.code"
                                        :value="district.name">
                                        {{ district.name }}
                                    </a-select-option>
                                </a-select>
                            </a-form-item>
                        </a-col>

                        <a-col :span="6">
                            <a-form-item label="Phường/Xã"
                                :validate-status="errors.diaChiErrors[index]?.xaPhuong ? 'error' : ''"
                                :help="errors.diaChiErrors[index]?.xaPhuong">
                                <a-select v-model:value="diaChi.xaPhuong" placeholder="Chọn Phường/Xã"
                                    class="custom-select" size="large" :disabled="!diaChi.quanHuyen"
                                    @change="() => handleWardChange(index)">
                                    <a-select-option v-for="ward in wards[index]" :key="ward.code" :value="ward.name">
                                        {{ ward.name }}
                                    </a-select-option>
                                </a-select>
                            </a-form-item>
                        </a-col>

                        <a-col :span="6">
                            <a-form-item label="Số nhà, tên đường"
                                :validate-status="errors.diaChiErrors[index]?.soNha ? 'error' : ''"
                                :help="errors.diaChiErrors[index]?.soNha">
                                <a-input v-model:value="diaChi.soNha" placeholder="Số nhà, tên đường..."
                                    class="custom-input" size="large" />
                            </a-form-item>
                        </a-col>
                    </a-row>

                    <!-- Hiển thị phí vận chuyển dự tính -->
                    <div v-if="calculatedShippingFee > 0" class="shipping-fee-display">
                        <div class="shipping-fee-content">
                            <DollarOutlined class="shipping-fee-icon" />
                            <span class="shipping-fee-label">Phí vận chuyển dự tính:</span>
                            <span class="shipping-fee-amount">{{ formatVND(calculatedShippingFee) }}</span>
                        </div>
                    </div>

                    <button type="button" class="btn-remove-address" @click="xoaDiaChi(index)"
                        v-if="formData.diaChiList.length > 1">
                        <delete-outlined />
                        Xóa địa chỉ
                    </button>
                </div>

                <!-- Nút hành động -->
                <div class="action-buttons-wrapper">
                    <a-button type="default" size="large" class="btn-add-customer" @click="confirmThemKhachHang">
                        <template #icon>
                            <user-add-outlined />
                        </template>
                        Thêm khách mới
                    </a-button>

                    <a-button type="primary" size="large" class="btn-save-info" @click="luuThongTinKhachHang">
                        <template #icon>
                            <save-outlined />
                        </template>
                        Lưu thông tin khách hàng
                    </a-button>

                    <a-button size="large" class="btn-reset" @click="resetForm">
                        <template #icon>
                            <redo-outlined />
                        </template>
                        Làm mới
                    </a-button>
                </div>
            </a-form>
        </form>
    </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch, onUnmounted, h } from 'vue';
import { useGbStore } from '@/stores/gbStore';
import { toast } from 'vue3-toastify';
import { Modal as AModal } from 'ant-design-vue';
import {
    UserAddOutlined,
    SaveOutlined,
    TeamOutlined,
    EnvironmentOutlined,
    DollarOutlined,
    DeleteOutlined,
    RedoOutlined
} from '@ant-design/icons-vue';
import { calculateShippingFee, formatVND } from '@/utils/shippingFeeCalculator';

const emit = defineEmits(['shippingFeeCalculated']);

const gbStore = useGbStore();
const calculatedShippingFee = ref(0);
const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);

const formData = reactive({
    maKhachHang: '',
    tenKhachHang: '',
    gioiTinh: null,
    soDienThoai: '',
    ngaySinh: null,
    email: '',
    trangThai: 'Đang hoạt động',
    diaChiList: [{
        soNha: '',
        xaPhuong: '',
        quanHuyen: '',
        tinhThanhPho: '',
        diaChiMacDinh: true
    }]
});

const errors = reactive({
    tenKhachHang: '',
    gioiTinh: '',
    ngaySinh: '',
    soDienThoai: '',
    email: '',
    diaChiErrors: [{}]
});

const validateForm = () => {
    let isValid = true;

    // Reset lỗi
    Object.keys(errors).forEach(key => {
        if (key !== 'diaChiErrors') errors[key] = '';
    });
    errors.diaChiErrors = formData.diaChiList.map(() => ({}));

    // Chuẩn hóa các trường văn bản
    formData.tenKhachHang = formData.tenKhachHang?.replace(/\s+/g, ' ').trim() || '';
    formData.soDienThoai = formData.soDienThoai?.replace(/\s+/g, '').trim() || '';
    formData.email = formData.email?.replace(/\s+/g, '').trim() || '';

    // Validate họ tên (từ backend: NotBlank, Size max 100, Pattern chỉ chữ cái)
    if (!formData.tenKhachHang) {
        errors.tenKhachHang = 'Tên khách hàng không được để trống';
        isValid = false;
    } else if (!/^[a-zA-Z\s\u00C0-\u1EF9]+$/.test(formData.tenKhachHang)) {
        errors.tenKhachHang = 'Tên chỉ được chứa chữ cái';
        isValid = false;
    } else if (formData.tenKhachHang.length > 100) {
        errors.tenKhachHang = 'Tên khách hàng không được vượt quá 100 ký tự';
        isValid = false;
    } else if (formData.tenKhachHang.length < 2) {
        errors.tenKhachHang = 'Tên khách hàng không được nhỏ hơn 2 ký tự';
        isValid = false;
    }

    // Validate số điện thoại (từ backend: NotBlank, Pattern 0\d{9})
    if (!formData.soDienThoai) {
        errors.soDienThoai = 'Số điện thoại không được để trống';
        isValid = false;
    } else if (!validatePhoneNumber(formData.soDienThoai)) {
        errors.soDienThoai = 'Số điện thoại phải bắt đầu bằng 0 và đúng 10 chữ số (VD: 0912345678)';
        isValid = false;
    }

    // Validate email (từ backend: NotBlank, Email, Size max 100)
    if (!formData.email) {
        errors.email = 'Email không được để trống';
        isValid = false;
    } else if (!validateEmail(formData.email)) {
        errors.email = 'Email không hợp lệ (VD: example@gmail.com)';
        isValid = false;
    } else if (formData.email.length > 100) {
        errors.email = 'Email không được vượt quá 100 ký tự';
        isValid = false;
    }

    // Kiểm tra danh sách địa chỉ
    formData.diaChiList.forEach((diaChi, index) => {
        if (!diaChi.tinhThanhPho) {
            errors.diaChiErrors[index].tinhThanhPho = 'Vui lòng chọn tỉnh/thành phố';
            isValid = false;
        }
        if (!diaChi.quanHuyen && diaChi.tinhThanhPho) {
            errors.diaChiErrors[index].quanHuyen = 'Vui lòng chọn quận/huyện';
            isValid = false;
        }
        if (!diaChi.xaPhuong && diaChi.quanHuyen) {
            errors.diaChiErrors[index].xaPhuong = 'Vui lòng chọn phường/xã';
            isValid = false;
        }
        if (!diaChi.soNha.trim()) {
            errors.diaChiErrors[index].soNha = 'Vui lòng nhập số nhà, tên đường';
            isValid = false;
        }
    });

    return isValid;
};

const validatePhoneNumber = (phone) => {
    const cleanedPhone = phone.replace(/\s+/g, '');
    const regex = /^(0)(3[2-9]|5[2689]|7[06-9]|8[1-9]|9[0-9])[0-9]{7}$/;
    return regex.test(cleanedPhone);
};

const validateEmail = (email) => {
    const regex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
    return regex.test(email);
};

const loadProvinces = async () => {
    try {
        const response = await fetch('https://provinces.open-api.vn/api/p/');
        provinces.value = await response.json();
    } catch (error) {
        console.error('Lỗi khi tải tỉnh/thành:', error);
    }
};

const handleProvinceChange = async (index) => {
    if (formData.diaChiList[index].tinhThanhPho) {
        try {
            console.log(`🏙️ Đang tải quận/huyện cho tỉnh: ${formData.diaChiList[index].tinhThanhPho}`);
            const province = provinces.value.find(p => p.name === formData.diaChiList[index].tinhThanhPho);

            if (!province) {
                console.error(`❌ Không tìm thấy mã tỉnh cho: ${formData.diaChiList[index].tinhThanhPho}`);
                console.log('Danh sách tỉnh có sẵn:', provinces.value.map(p => p.name));
                districts.value[index] = [];
                wards.value[index] = [];
                return;
            }

            const provinceCode = province.code;
            console.log(`📍 Mã tỉnh: ${provinceCode}`);

            const response = await fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`);
            const data = await response.json();

            if (data && data.districts) {
                districts.value[index] = data.districts;
                console.log(`✅ Đã tải ${data.districts.length} quận/huyện`);
            } else {
                console.error('❌ API không trả về dữ liệu districts');
                districts.value[index] = [];
            }

            // Không reset quận/huyện nếu đang load dữ liệu từ localStorage
            if (!formData.diaChiList[index]._isLoading) {
                formData.diaChiList[index].quanHuyen = '';
                wards.value[index] = [];
            }
        } catch (error) {
            console.error('❌ Lỗi khi tải quận/huyện:', error);
            districts.value[index] = [];
            wards.value[index] = [];
        }
    } else {
        console.log(`⚠️ Chưa chọn tỉnh/thành phố cho index ${index}`);
        districts.value[index] = [];
        wards.value[index] = [];
    }
};

const handleDistrictChange = async (index) => {
    if (formData.diaChiList[index].quanHuyen) {
        try {
            console.log(`🏘️ Đang tải phường/xã cho quận/huyện: ${formData.diaChiList[index].quanHuyen}`);

            if (!districts.value[index] || districts.value[index].length === 0) {
                console.error('❌ Chưa có dữ liệu quận/huyện');
                wards.value[index] = [];
                return;
            }

            const district = districts.value[index].find(d => d.name === formData.diaChiList[index].quanHuyen);

            if (!district) {
                console.error(`❌ Không tìm thấy mã quận/huyện cho: ${formData.diaChiList[index].quanHuyen}`);
                console.log('Danh sách quận/huyện có sẵn:', districts.value[index].map(d => d.name));
                wards.value[index] = [];
                return;
            }

            const districtCode = district.code;
            console.log(`📍 Mã quận/huyện: ${districtCode}`);

            const response = await fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`);
            const data = await response.json();

            if (data && data.wards) {
                wards.value[index] = data.wards;
                console.log(`✅ Đã tải ${data.wards.length} phường/xã`);
            } else {
                console.error('❌ API không trả về dữ liệu wards');
                wards.value[index] = [];
            }

            // Không reset phường/xã nếu đang load dữ liệu từ localStorage
            if (!formData.diaChiList[index]._isLoading) {
                formData.diaChiList[index].xaPhuong = '';
            }

            // Tính phí vận chuyển khi chọn quận/huyện
            updateShippingFee(index);
        } catch (error) {
            console.error('❌ Lỗi khi tải phường/xã:', error);
            wards.value[index] = [];
        }
    } else {
        console.log(`⚠️ Chưa chọn quận/huyện cho index ${index}`);
        wards.value[index] = [];
    }
};

const handleWardChange = async (index) => {
    // Khi chọn phường/xã, tính phí vận chuyển
    const diaChi = formData.diaChiList[index];
    if (diaChi.tinhThanhPho && diaChi.quanHuyen && diaChi.xaPhuong) {
        console.log(`🏘️ Đã chọn phường/xã: ${diaChi.xaPhuong}`);
        updateShippingFee(index);
    }
};

const updateShippingFee = async (index) => {
    const diaChi = formData.diaChiList[index];
    if (diaChi.tinhThanhPho && diaChi.quanHuyen) {
        console.log(`📦 Đang tính phí vận chuyển qua GHTK API...`);

        try {
            // Lấy tổng tiền hóa đơn hiện tại (nếu có)
            const idHoaDon = gbStore.getCurrentHoaDonId();
            const hoaDonHienTai = idHoaDon ? gbStore.getAllHoaDonCTTArr.find(hd => hd.id_hoa_don === idHoaDon) : null;
            const tongTienHoaDon = Math.round(hoaDonHienTai?.tong_tien_truoc_giam || 150000); // Convert to integer
            
            // ✅ Chuẩn bị tham số cho GHTK API
            // GHTK yêu cầu tên tỉnh/quận KHÔNG có tiền tố "Tỉnh"/"Quận"
            const cleanProvince = diaChi.tinhThanhPho.replace(/^(Tỉnh|Thành phố)\s+/i, '');
            const cleanDistrict = diaChi.quanHuyen.replace(/^(Quận|Huyện|Thị xã|Thành phố)\s+/i, '');
            
            console.log(`🎯 GHTK params:`, {
                from: 'Hà Nội - Đống Đa',
                to: `${cleanProvince} - ${cleanDistrict}`,
                weight: 500,
                value: tongTienHoaDon
            });
            
            // ✅ LUÔN gọi API GHTK để tính phí (không cần idHoaDon)
            const result = await gbStore.tinhPhiShip(
                'Hà Nội',              // GHTK yêu cầu bỏ "Tỉnh"
                'Đống Đa',             // GHTK yêu cầu bỏ "Quận"
                cleanProvince,         // Tỉnh khách (đã bỏ tiền tố)
                cleanDistrict,         // Quận khách (đã bỏ tiền tố)
                500,                   // 500 gram
                tongTienHoaDon         // Tổng tiền (integer)
            );

            if (result && !result.error && result.fee) {
                calculatedShippingFee.value = result.fee;
                console.log(`✅ Phí vận chuyển từ GHTK: ${formatVND(calculatedShippingFee.value)}`);
                
                // ✅ Emit event để parent cập nhật ngay
                emit('shippingFeeCalculated', calculatedShippingFee.value);
                
                // Chỉ cập nhật vào backend NẾU có hóa đơn
                if (idHoaDon) {
                    await gbStore.setTrangThaiNhanHang(idHoaDon, 'Giao hàng', calculatedShippingFee.value);

                    localStorage.setItem('shippingFeeUpdated', JSON.stringify({
                        idHoaDon,
                        phiVanChuyen: calculatedShippingFee.value,
                        timestamp: Date.now()
                    }));

                    toast.success(`Phí vận chuyển GHTK: ${formatVND(calculatedShippingFee.value)}`, {
                        autoClose: 2000,
                        position: 'top-right'
                    });
                } else {
                    // Chưa có hóa đơn - vẫn lưu vào localStorage để khi tạo hóa đơn sẽ dùng
                    localStorage.setItem('calculatedShippingFee', calculatedShippingFee.value);
                    console.log(`ℹ️ Phí vận chuyển dự kiến (chưa có hóa đơn): ${formatVND(calculatedShippingFee.value)}`);
                }
            } else {
                throw new Error('Không nhận được phí vận chuyển từ GHTK');
            }
        } catch (error) {
            console.error('❌ Lỗi khi gọi API GHTK:', error);
            // Fallback về tính phí cố định
            calculatedShippingFee.value = calculateShippingFee(diaChi.tinhThanhPho, diaChi.quanHuyen);
            console.log(`📦 Sử dụng phí dự kiến: ${formatVND(calculatedShippingFee.value)}`);

            const idHoaDon = gbStore.getCurrentHoaDonId();
            if (idHoaDon) {
                await gbStore.setTrangThaiNhanHang(idHoaDon, 'Giao hàng', calculatedShippingFee.value);

                localStorage.setItem('shippingFeeUpdated', JSON.stringify({
                    idHoaDon,
                    phiVanChuyen: calculatedShippingFee.value,
                    timestamp: Date.now()
                }));
            }

            toast.warning(`Dùng phí dự kiến: ${formatVND(calculatedShippingFee.value)}. GHTK tạm thời không khả dụng.`, {
                autoClose: 3000,
                position: 'top-right'
            });
        }
    }
};

const xoaDiaChi = (index) => {
    formData.diaChiList.splice(index, 1);
    districts.value.splice(index, 1);
    wards.value.splice(index, 1);
    errors.diaChiErrors.splice(index, 1);
    if (!formData.diaChiList.some(d => d.diaChiMacDinh) && formData.diaChiList.length > 0) {
        formData.diaChiList[0].diaChiMacDinh = true;
    }
};

const handleDefaultChange = (index) => {
    if (formData.diaChiList[index].diaChiMacDinh) {
        formData.diaChiList.forEach((diaChi, i) => {
            diaChi.diaChiMacDinh = (i === index);
        });
    } else if (!formData.diaChiList.some(d => d.diaChiMacDinh)) {
        formData.diaChiList[0].diaChiMacDinh = true;
    }
};



const resetForm = () => {
    Object.assign(formData, {
        maKhachHang: '',
        tenKhachHang: '',
        gioiTinh: null,
        soDienThoai: '',
        ngaySinh: null,
        email: '',
        trangThai: 'Đang hoạt động',
        diaChiList: [{
            soNha: '',
            xaPhuong: '',
            quanHuyen: '',
            tinhThanhPho: '',
            diaChiMacDinh: true
        }]
    });
    Object.keys(errors).forEach(key => {
        if (key !== 'diaChiErrors') errors[key] = '';
    });
    errors.diaChiErrors = [{}];
    districts.value = [[]];
    wards.value = [[]];
};

const themKhachHang = async () => {
    if (!validateForm()) {
        toast.error('Vui lòng điền đầy đủ và chính xác thông tin!');
        return;
    }

    const dataToSend = { ...formData };
    console.log("datagui:", dataToSend);
    try {
        const result = await gbStore.themKhachHangBH(dataToSend);
        await new Promise(resolve => setTimeout(resolve, 500));
        const idHoaDon = gbStore.getCurrentHoaDonId();
        const diaChiList = formData.diaChiList.map(diaChi => {
            return `${diaChi.soNha}, ${diaChi.xaPhuong}, ${diaChi.quanHuyen}, ${diaChi.tinhThanhPho}`;
        });
        const newKhachHang = await gbStore.getLatestKhachHang();
        const idKH = newKhachHang ? newKhachHang.idKhachHang : null;
        await gbStore.addKHHD(idHoaDon, idKH, diaChiList, formData.tenKhachHang, formData.soDienThoai, formData.email);
        localStorage.setItem('luuTTKHBH', JSON.stringify(true));
        localStorage.setItem('khachHangBH', JSON.stringify(dataToSend));
        if (result) {
            toast.success('Thêm khách hàng thành công!', {
                autoClose: 2000,
                position: 'top-right'
            });
        }
    } catch (error) {
        console.error('Lỗi khi thêm khách hàng:', error);
        console.log('Error object:', error);
        console.log('Response:', error.response);
        console.log('Message:', error.message);
        if (error.response && error.response.data && error.response.data.error) {
            if (error.response.data.error.includes('Email đã được sử dụng')) {
                errors.email = 'Email đã được sử dụng!';
                toast.error('Email đã được sử dụng!');
            } else if (error.response.data.error.includes('Mã khách hàng đã tồn tại')) {
                toast.error('Mã khách hàng đã tồn tại!');
            } else {
                toast.error(error.response.data.error);
            }
        } else {
            toast.error(`Có lỗi xảy ra: ${error.message || 'Không thể kết nối đến server'}`);
        }
    }
};
const luuThongTin = async () => {
    if (!validateForm()) {
        toast.error('Vui lòng điền đầy đủ và chính xác thông tin!');
        return;
    }

    const idHoaDon = gbStore.getCurrentHoaDonId()
    console.log('idHoaDon', idHoaDon)
    const diaChiList = formData.diaChiList.map(diaChi => {
        return `${diaChi.soNha}, ${diaChi.xaPhuong}, ${diaChi.quanHuyen}, ${diaChi.tinhThanhPho}`;
    });

    console.log('Địa chỉ gộp:', diaChiList);

    // Thực hiện logic lưu thông tin (ví dụ: gửi dữ liệu đến API)
    const dataToSend = {
        ...formData,
        diaChiList, // Thêm chuỗi địa chỉ gộp vào dữ liệu gửi đi
        idHoaDon,  // Thêm ID hóa đơn
    };

    console.log('Dữ liệu gửi đi:', dataToSend);

    console.log('Lưu thông tin khách hàng:', idHoaDon, null, diaChiList, formData.tenKhachHang, formData.soDienThoai, formData.email);

    const khachHangList = await gbStore.getAllKhachHangNoPage();
    const existingKhachHang = khachHangList?.find(kh =>
        kh.tenKhachHang === formData.tenKhachHang &&
        kh.soDienThoai === formData.soDienThoai
    );
    const idKH = existingKhachHang ? existingKhachHang.idKhachHang : null;

    await gbStore.addKHHD(idHoaDon, idKH, diaChiList, formData.tenKhachHang, formData.soDienThoai, formData.email);

    // ✅ Lưu thông tin vào localStorage để component cha đọc được
    localStorage.setItem('luuTTKHBH', JSON.stringify({
        saved: true,
        ten_khach_hang: formData.tenKhachHang,
        so_dien_thoai: formData.soDienThoai,
        dia_chi: diaChiList[0], // Lấy địa chỉ đầu tiên
        email: formData.email
    }));

    toast.success('Lưu thông tin khách hàng thành công!', {
        autoClose: 2000,
        position: 'top-right'
    });

};

const confirmThemKhachHang = () => {
    AModal.confirm({
        title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
            h(UserAddOutlined, { style: 'color: #52c41a; font-size: 22px;' }),
            h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Thêm khách hàng mới')
        ]),
        content: () => h('div', { style: 'padding: 8px 0;' }, [
            h('p', { style: 'margin: 0; font-size: 14px;' }, 'Bạn có muốn thêm khách hàng này vào hệ thống không?')
        ]),
        okText: 'Thêm khách hàng',
        cancelText: 'Hủy',
        okButtonProps: { size: 'large', style: { height: '38px', background: '#52c41a', borderColor: '#52c41a' } },
        cancelButtonProps: { size: 'large', style: { height: '38px' } },
        centered: true,
        width: 450,
        onOk: () => {
            themKhachHang();
        },
    });
};

const luuThongTinKhachHang = () => {
    AModal.confirm({
        title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
            h(SaveOutlined, { style: 'color: #1890ff; font-size: 22px;' }),
            h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Lưu thông tin KH')
        ]),
        content: () => h('div', { style: 'padding: 8px 0;' }, [
            h('p', { style: 'margin: 0; font-size: 14px;' }, 'Bạn có muốn lưu thông tin khách hàng này không?')
        ]),
        okText: 'Lưu',
        cancelText: 'Hủy',
        okButtonProps: { size: 'large', style: { height: '38px' } },
        cancelButtonProps: { size: 'large', style: { height: '38px' } },
        centered: true,
        width: 420,
        onOk: () => {
            luuThongTin();
        }
    });
};

const tachDiaChi = (diaChiDayDu) => {
    const result = {
        soNha: '',
        xaPhuong: '',
        quanHuyen: '',
        tinhThanhPho: ''
    };

    if (!diaChiDayDu) return result;

    const parts = diaChiDayDu.split(',').map(p => p.trim());

    // Giả định định dạng là: "số nhà, xã/phường, quận/huyện, tỉnh/thành phố"
    if (parts.length >= 4) {
        result.soNha = parts[0];
        result.xaPhuong = timTenGanDung(parts[1], 'ward');
        result.quanHuyen = timTenGanDung(parts[2], 'district');
        result.tinhThanhPho = timTenGanDung(parts[3], 'province');
    }

    return result;
};

const timTenGanDung = (tenTuClient, cap, index = 0) => {
    const normalize = str => str.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");

    const normalizedInput = normalize(tenTuClient);

    let danhSach = [];
    if (cap === 'province') {
        danhSach = provinces.value || [];
    } else if (cap === 'district') {
        danhSach = districts.value[index] || [];
    } else if (cap === 'ward') {
        danhSach = wards.value[index] || [];
    }

    const matched = danhSach.find(item => normalize(item.name).includes(normalizedInput));
    return matched ? matched.name : tenTuClient;
};

const props = defineProps({
    triggerUpdate: Number,
});


onMounted(async () => {
    await initializeLocationData();

    const checkKH = localStorage.getItem('chonKH');
    if (checkKH === 'true') {
        await loadKhachHangTuLocalStorage();

        await handleAllAddressLevels();
    }
});

// Cleanup scroll lock khi component unmount
onUnmounted(() => {
    // Remove overflow hidden from body if it exists
    document.body.style.overflow = '';
    document.body.style.paddingRight = '';
    // Remove modal mask classes
    const modalMask = document.querySelector('.ant-modal-mask');
    if (modalMask) {
        modalMask.remove();
    }
});



const initializeLocationData = async () => {
    await loadProvinces();
    districts.value = [[]];
    wards.value = [[]];
};

const loadKhachHangTuLocalStorage = async () => {
    const khachHangData = localStorage.getItem('khachHangBH');
    if (!khachHangData) return;

    try {
        const khachHang = JSON.parse(khachHangData);
        formData.tenKhachHang = khachHang.hoTen || '';
        formData.soDienThoai = khachHang.soDienThoai || '';
        formData.email = khachHang.email || '';

        if (khachHang.diaChi) {
            const diaChi = tachDiaChi(khachHang.diaChi);
            formData.diaChiList = [{
                soNha: diaChi.soNha || '',
                xaPhuong: diaChi.xaPhuong || '',
                quanHuyen: diaChi.quanHuyen || '',
                tinhThanhPho: diaChi.tinhThanhPho || '',
                diaChiMacDinh: true
            }];
        }
    } catch (err) {
        console.error('Lỗi khi đọc khách hàng:', err);
    }
};

const handleAllAddressLevels = async () => {
    if (formData.diaChiList.length === 0) return;

    console.log('🔄 Bắt đầu xử lý tất cả các cấp địa chỉ...');

    for (let index = 0; index < formData.diaChiList.length; index++) {
        const diaChi = formData.diaChiList[index];
        console.log(`📍 Đang xử lý địa chỉ tại index ${index}:`, diaChi);

        // Đánh dấu đang load để không reset dữ liệu
        formData.diaChiList[index]._isLoading = true;

        // Bước 1: Gọi API tỉnh và đợi hoàn thành
        if (diaChi.tinhThanhPho) {
            console.log(`1️⃣ Tải danh sách quận/huyện cho: ${diaChi.tinhThanhPho}`);
            await handleProvinceChange(index);

            // Đợi một chút để API hoàn thành
            await new Promise(resolve => setTimeout(resolve, 100));

            // Sau khi có danh sách quận/huyện, tìm tên chính xác
            if (districts.value[index] && districts.value[index].length > 0) {
                const matchedDistrict = timTenGanDung(diaChi.quanHuyen, 'district', index);
                formData.diaChiList[index].quanHuyen = matchedDistrict;
                console.log(`✅ Quận/Huyện đã map: ${matchedDistrict}`);
            }
        }

        // Bước 2: Gọi API huyện và đợi hoàn thành
        if (formData.diaChiList[index].quanHuyen) {
            console.log(`2️⃣ Tải danh sách phường/xã cho: ${formData.diaChiList[index].quanHuyen}`);
            await handleDistrictChange(index);

            // Đợi một chút để API hoàn thành
            await new Promise(resolve => setTimeout(resolve, 100));

            // Sau khi có danh sách phường/xã, tìm tên chính xác
            if (wards.value[index] && wards.value[index].length > 0) {
                const matchedWard = timTenGanDung(diaChi.xaPhuong, 'ward', index);
                formData.diaChiList[index].xaPhuong = matchedWard;
                console.log(`✅ Phường/Xã đã map: ${matchedWard}`);
            }
        }

        // Gỡ cờ loading
        delete formData.diaChiList[index]._isLoading;

        // ✅ Tính phí vận chuyển sau khi load xong địa chỉ
        if (formData.diaChiList[index].tinhThanhPho && formData.diaChiList[index].quanHuyen) {
            console.log(`💰 Tính phí vận chuyển cho địa chỉ đã load`);
            await updateShippingFee(index);
        }
    }

    console.log('✅ Hoàn thành xử lý tất cả các cấp địa chỉ');
};

// Watch triggerUpdate để reload khi component cha yêu cầu (đặt sau khi function được định nghĩa)
watch(
    () => props.triggerUpdate,
    async () => {
        await loadKhachHangTuLocalStorage();
    },
    { immediate: true }
);

</script>

<style scoped>
/* Wrapper */
.customer-form-wrapper {
    margin-top: 24px;
    background: #ffffff;
    padding: 0;
}

/* Form Header */
.form-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 20px 24px;
    background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
    border-radius: 12px 12px 0 0;
    margin-bottom: 24px;
}

.header-icon {
    font-size: 28px;
    color: white;
}

.header-title {
    margin: 0;
    color: white;
    font-size: 20px;
    font-weight: 600;
}

/* Form Section */
.form-section {
    padding: 0 24px 20px 24px;
    border-bottom: 2px solid #f0f0f0;
    margin-bottom: 24px;
}

/* Custom Inputs */
:deep(.custom-input .ant-input) {
    border-radius: 8px;
    border: 2px solid #e8e8e8;
    font-size: 14px;
    transition: all 0.3s ease;
}

:deep(.custom-input .ant-input:hover) {
    border-color: #ff6600;
}

:deep(.custom-input .ant-input:focus) {
    border-color: #ff6600;
    box-shadow: 0 0 0 2px rgba(255, 102, 0, 0.1);
}

/* Custom Select */
:deep(.custom-select .ant-select-selector) {
    border-radius: 8px !important;
    border: 2px solid #e8e8e8 !important;
    font-size: 14px !important;
    transition: all 0.3s ease !important;
}

:deep(.custom-select:hover .ant-select-selector) {
    border-color: #ff6600 !important;
}

:deep(.custom-select.ant-select-focused .ant-select-selector) {
    border-color: #ff6600 !important;
    box-shadow: 0 0 0 2px rgba(255, 102, 0, 0.1) !important;
}

/* Address Card */
.address-card {
    background: linear-gradient(135deg, #ffffff 0%, #f9f9f9 100%);
    border: 2px solid #e8e8e8;
    border-radius: 12px;
    padding: 20px 24px;
    margin: 0 24px 20px 24px;
    transition: all 0.3s ease;
}

.address-card:hover {
    border-color: #ff6600;
    box-shadow: 0 2px 12px rgba(255, 102, 0, 0.1);
}

.address-card-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 2px solid #f0f0f0;
}

.address-icon {
    font-size: 22px;
    color: #ff6600;
}

.address-title {
    font-size: 16px;
    font-weight: 600;
    color: #262626;
}

/* Shipping Fee Card */
.shipping-fee-card {
    background: linear-gradient(135deg, #fff7e6 0%, #ffffff 100%);
    border: 2px solid #ffd591;
    border-radius: 10px;
    padding: 16px 20px;
    margin-top: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: all 0.3s ease;
}

.shipping-fee-card:hover {
    border-color: #ff6600;
    box-shadow: 0 2px 10px rgba(255, 102, 0, 0.15);
}

.shipping-fee-header {
    display: flex;
    align-items: center;
    gap: 10px;
}

.shipping-fee-icon {
    font-size: 20px;
    color: #ff6600;
}

.shipping-fee-title {
    font-size: 14px;
    font-weight: 500;
    color: #595959;
}

.shipping-fee-amount {
    font-size: 18px;
    font-weight: 700;
    color: #ff6600;
}

/* Remove Address Button */
.btn-remove-address {
    margin-top: 16px;
    padding: 8px 16px;
    background: #fff;
    border: 2px solid #ff4d4f;
    border-radius: 8px;
    color: #ff4d4f;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
    display: inline-flex;
    align-items: center;
    gap: 8px;
}

.btn-remove-address:hover {
    background: #ff4d4f;
    color: white;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3);
}

/* Action Buttons Wrapper */
.action-buttons-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 16px;
    padding: 24px;
    border-top: 2px solid #f0f0f0;
    margin-top: 20px;
}

/* Add Customer Button */
.btn-add-customer {
    height: 44px !important;
    padding: 0 24px !important;
    border-radius: 8px !important;
    font-size: 14px !important;
    font-weight: 500 !important;
    border: 2px solid #52c41a !important;
    color: #52c41a !important;
    transition: all 0.3s ease !important;
}

.btn-add-customer:hover {
    background: #52c41a !important;
    color: white !important;
    transform: translateY(-2px) !important;
    box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3) !important;
}

/* Save Info Button */
.btn-save-info {
    height: 44px !important;
    padding: 0 24px !important;
    border-radius: 8px !important;
    font-size: 14px !important;
    font-weight: 500 !important;
    background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%) !important;
    border: none !important;
    transition: all 0.3s ease !important;
}

.btn-save-info:hover {
    background: linear-gradient(135deg, #ff8533 0%, #ffa366 100%) !important;
    transform: translateY(-2px) !important;
    box-shadow: 0 4px 12px rgba(255, 102, 0, 0.35) !important;
}

/* Reset Button */
.btn-reset {
    height: 44px !important;
    padding: 0 24px !important;
    border-radius: 8px !important;
    font-size: 14px !important;
    font-weight: 500 !important;
    border: 2px solid #d9d9d9 !important;
    color: #595959 !important;
    transition: all 0.3s ease !important;
}

.btn-reset:hover {
    border-color: #ff6600 !important;
    color: #ff6600 !important;
    transform: translateY(-1px) !important;
}

/* Form Labels */
:deep(.ant-form-item-label > label) {
    font-weight: 500;
    color: #262626;
    font-size: 14px;
}

/* Error Messages */
:deep(.ant-form-item-explain-error) {
    font-size: 13px;
}
</style>