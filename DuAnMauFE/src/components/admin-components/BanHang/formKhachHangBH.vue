<template>
    <div>
        <div class="header-section">
            <h2>Thông tin khách hàng</h2>
        </div>
        <form @submit.prevent="themKhachHang" @reset.prevent="resetForm">
            <a-form :model="formData" :label-col="{ span: 24 }" :wrapper-col="{ span: 24 }">
                <!-- Thông tin cơ bản -->
                <a-row :gutter="12">
                    <a-col :span="8">
                        <a-form-item label="Họ tên khách hàng" :validate-status="errors.tenKhachHang ? 'error' : ''"
                            :help="errors.tenKhachHang">
                            <a-input v-model:value="formData.tenKhachHang" placeholder="Nhập tên khách hàng" />
                        </a-form-item>
                    </a-col>

                    <a-col :span="8">
                        <a-form-item label="Số điện thoại" :validate-status="errors.soDienThoai ? 'error' : ''"
                            :help="errors.soDienThoai">
                            <a-input v-model:value="formData.soDienThoai" placeholder="Nhập số điện thoại" />
                        </a-form-item>
                    </a-col>

                    <a-col :span="8">
                        <a-form-item label="Email" :validate-status="errors.email ? 'error' : ''" :help="errors.email">
                            <a-input v-model:value="formData.email" placeholder="Nhập email" />
                        </a-form-item>
                    </a-col>
                </a-row>

                <!-- Danh sách địa chỉ -->
                <div v-for="(diaChi, index) in formData.diaChiList" :key="index" class="address-section">
                    <a-row :gutter="16">
                        <a-col :span="6">
                            <a-form-item label="Tỉnh/Thành phố"
                                :validate-status="errors.diaChiErrors[index]?.tinhThanhPho ? 'error' : ''"
                                :help="errors.diaChiErrors[index]?.tinhThanhPho">
                                <a-select v-model:value="diaChi.tinhThanhPho" placeholder="Chọn Tỉnh/Thành phố"
                                    @change="() => handleProvinceChange(index)">
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
                                    :disabled="!diaChi.tinhThanhPho" @change="() => handleDistrictChange(index)">
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
                                    :disabled="!diaChi.quanHuyen">
                                    <a-select-option v-for="ward in wards[index]" :key="ward.code" :value="ward.name">
                                        {{ ward.name }}
                                    </a-select-option>
                                </a-select>
                            </a-form-item>
                        </a-col>

                        <a-col :span="5">
                            <a-form-item label="Số nhà, tên đường"
                                :validate-status="errors.diaChiErrors[index]?.soNha ? 'error' : ''"
                                :help="errors.diaChiErrors[index]?.soNha">
                                <a-input v-model:value="diaChi.soNha" placeholder="Số nhà, tên đường..." />
                            </a-form-item>
                        </a-col>
                    </a-row>
                    
                    <!-- Hiển thị phí vận chuyển dự tính -->
                    <a-row v-if="calculatedShippingFee > 0" :gutter="16" class="mt-2">
                        <a-col :span="24">
                            <a-alert
                                type="info"
                                :message="`Phí vận chuyển dự tính: ${formatVND(calculatedShippingFee)}`"
                                show-icon
                            />
                        </a-col>
                    </a-row>
                    
                    <button type="button" class="btn btn-danger" @click="xoaDiaChi(index)"
                        v-if="formData.diaChiList.length > 1">
                        Xóa địa chỉ
                    </button>
                </div>

                <!-- Nút hành động -->
                <div class="mt-4">
                    <button type="button" class="btn btn-warning me-2" @click="confirmThemKhachHang">Thêm khách
                        mới</button>
                    <button type="button" class="btn btn-warning me-2" @click="luuThongTinKhachHang">Lưu thông tin khách
                        hàng</button>
                    <button type="button" class="btn btn-secondary" @click="resetForm">Làm mới</button>
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
import { UserAddOutlined, SaveOutlined } from '@ant-design/icons-vue';
import { calculateShippingFee, formatVND } from '@/utils/shippingFeeCalculator';

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
            const provinceCode = provinces.value.find(p => p.name === formData.diaChiList[index].tinhThanhPho)?.code;
            const response = await fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`);
            const data = await response.json();
            districts.value[index] = data.districts;
            formData.diaChiList[index].quanHuyen = '';
            wards.value[index] = [];
        } catch (error) {
            console.error('Lỗi khi tải quận/huyện:', error);
        }
    }
};

const handleDistrictChange = async (index) => {
    if (formData.diaChiList[index].quanHuyen) {
        try {
            const districtCode = districts.value[index].find(d => d.name === formData.diaChiList[index].quanHuyen)?.code;
            const response = await fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`);
            const data = await response.json();
            wards.value[index] = data.wards;
            formData.diaChiList[index].xaPhuong = '';
            
            // Tính phí vận chuyển khi chọn quận/huyện
            updateShippingFee(index);
        } catch (error) {
            console.error('Lỗi khi tải phường/xã:', error);
        }
    }
};

const updateShippingFee = async (index) => {
    const diaChi = formData.diaChiList[index];
    if (diaChi.tinhThanhPho && diaChi.quanHuyen) {
        calculatedShippingFee.value = calculateShippingFee(diaChi.tinhThanhPho, diaChi.quanHuyen);
        
        console.log(`📦 Phí vận chuyển: ${formatVND(calculatedShippingFee.value)}`);
        
        // Cập nhật phí vận chuyển vào hóa đơn hiện tại
        const idHoaDon = gbStore.getCurrentHoaDonId();
        if (idHoaDon && calculatedShippingFee.value > 0) {
            try {
                await gbStore.setTrangThaiNhanHang(idHoaDon, 'Giao hàng', calculatedShippingFee.value);
                
                // Lưu vào localStorage để component cha cập nhật
                localStorage.setItem('shippingFeeUpdated', JSON.stringify({
                    idHoaDon,
                    phiVanChuyen: calculatedShippingFee.value,
                    timestamp: Date.now()
                }));
                
                toast.success(`Phí vận chuyển: ${formatVND(calculatedShippingFee.value)}`, {
                    autoClose: 2000,
                    position: 'top-right'
                });
            } catch (error) {
                console.error('Lỗi khi cập nhật phí vận chuyển:', error);
            }
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

const timTenGanDung = (tenTuClient, cap) => {
    const normalize = str => str.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");

    const normalizedInput = normalize(tenTuClient);

    let danhSach = [];
    if (cap === 'province') {
        danhSach = provinces.value || [];
    } else if (cap === 'district') {
        danhSach = districts.value[0] || []; // chỉ lấy theo index 0 (vì lúc này chưa phân index)
    } else if (cap === 'ward') {
        danhSach = wards.value[0] || [];
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

watch(() => props.triggerUpdate, async () => {
    if (localStorage.getItem('chonKH') === 'true') {
        await loadKhachHangTuLocalStorage();
        await handleAllAddressLevels();
        localStorage.setItem('chonKH', 'false');
    }
}, { immediate: true });

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
        formData.tenKhachHang = khachHang.tenKhachHang || '';
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

    for (let index = 0; index < formData.diaChiList.length; index++) {
        const diaChi = formData.diaChiList[index];
        console.log(`Đang xử lý địa chỉ tại index ${index}:`, diaChi);

        // Gọi API tỉnh
        await handleProvinceChange(index);
        formData.diaChiList[index].quanHuyen = timTenGanDung(diaChi.quanHuyen, 'district');

        // Gọi API huyện
        await handleDistrictChange(index);
        formData.diaChiList[index].xaPhuong = timTenGanDung(diaChi.xaPhuong, 'ward');
    }
};



</script>

<style scoped>
.header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.header-section h2 {
    margin: 0;
}

.btn-secondary {
    margin-left: auto;
}

.address-section {
    border: 1px solid #e8e8e8;
    padding: 16px;
    margin-bottom: 16px;
    border-radius: 4px;
    position: relative;
}

.address-section h3 {
    margin-bottom: 16px;
    font-size: 16px;
    color: #333;
}

.btn-danger {
    position: absolute;
    top: 16px;
    right: 16px;
}
</style>