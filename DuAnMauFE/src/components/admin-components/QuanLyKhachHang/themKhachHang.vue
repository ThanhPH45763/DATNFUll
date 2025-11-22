<template>
  <div class="container">
    <div class="header-section">
      <h2>Thêm khách hàng</h2>
      <a-button type="text" class="btn-back" @click="router.push('/admin/quanlykhachhang')">
        <template #icon><arrow-left-outlined /></template>
        Quay lại
      </a-button>
    </div>
    <a-form :model="formData" :label-col="{ span: 24 }" :wrapper-col="{ span: 24 }" @submit="confirmThemKhachHang"
      @reset="resetForm" class="form-container">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="Họ tên khách hàng" :validate-status="errors.hoTen ? 'error' : ''"
            :help="errors.hoTen">
            <a-input v-model:value="formData.hoTen" placeholder="Nhập tên khách hàng" class="input-field" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="Giới tính" :validate-status="errors.gioiTinh ? 'error' : ''" :help="errors.gioiTinh">
            <a-radio-group v-model:value="formData.gioiTinh" class="radio-group">
              <a-radio :value="true">Nam</a-radio>
              <a-radio :value="false">Nữ</a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="Ngày sinh" :validate-status="errors.ngaySinh ? 'error' : ''" :help="errors.ngaySinh">
            <a-date-picker v-model:value="formData.ngaySinh" format="DD/MM/YYYY" placeholder="Chọn ngày sinh"
              class="input-field w-100" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="Số điện thoại" :validate-status="errors.soDienThoai ? 'error' : ''"
            :help="errors.soDienThoai">
            <a-input v-model:value="formData.soDienThoai" placeholder="Nhập số điện thoại" class="input-field" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="Email" :validate-status="errors.email ? 'error' : ''" :help="errors.email">
            <a-input v-model:value="formData.email" placeholder="Nhập email" class="input-field" />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- Danh sách địa chỉ -->
      <div v-for="(diaChi, index) in formData.diaChiList" :key="index" class="address-section">
        <div class="address-header">
          <h3>Địa chỉ {{ index + 1 }}</h3>
          <a-button type="link" danger size="small" @click="xoaDiaChi(index)" v-if="formData.diaChiList.length > 1">
            <delete-outlined /> Xóa
          </a-button>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :md="6">
            <a-form-item label="Tỉnh/Thành phố"
              :validate-status="errors.diaChiErrors[index]?.tinhThanhPho ? 'error' : ''"
              :help="errors.diaChiErrors[index]?.tinhThanhPho">
              <a-select v-model:value="diaChi.tinhThanhPho" placeholder="Chọn Tỉnh/Thành phố"
                @change="() => handleProvinceChange(index)" class="select-field">
                <a-select-option v-for="province in provinces" :key="province.code" :value="province.name">
                  {{ province.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-form-item label="Quận/Huyện" :validate-status="errors.diaChiErrors[index]?.quanHuyen ? 'error' : ''"
              :help="errors.diaChiErrors[index]?.quanHuyen">
              <a-select v-model:value="diaChi.quanHuyen" placeholder="Chọn Quận/Huyện" :disabled="!diaChi.tinhThanhPho"
                @change="() => handleDistrictChange(index)" class="select-field">
                <a-select-option v-for="district in districts[index]" :key="district.code" :value="district.name">
                  {{ district.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-form-item label="Phường/Xã" :validate-status="errors.diaChiErrors[index]?.xaPhuong ? 'error' : ''"
              :help="errors.diaChiErrors[index]?.xaPhuong">
              <a-select v-model:value="diaChi.xaPhuong" placeholder="Chọn Phường/Xã" :disabled="!diaChi.quanHuyen"
                class="select-field">
                <a-select-option v-for="ward in wards[index]" :key="ward.code" :value="ward.name">
                  {{ ward.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="5">
            <a-form-item label="Số nhà, tên đường" :validate-status="errors.diaChiErrors[index]?.soNha ? 'error' : ''"
              :help="errors.diaChiErrors[index]?.soNha">
              <a-input v-model:value="diaChi.soNha" placeholder="Số nhà, tên đường..." class="input-field" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="1">
            <a-form-item label="Mặc định">
              <a-checkbox v-model:checked="diaChi.diaChiMacDinh" @change="handleDefaultChange(index)" />
            </a-form-item>
          </a-col>
        </a-row>
      </div>

      <a-button type="dashed" block class="btn-add-address" @click="themDiaChi">
        <plus-outlined /> Thêm địa chỉ khác
      </a-button>

      <div class="form-actions">
        <a-button type="primary" html-type="submit" size="large"
          style="background-color: #d02c39; border-color: #d02c39; color: white;">
          Tạo tài khoản
        </a-button>

        <a-button html-type="reset" size="large">
          Làm mới
        </a-button>
      </div>
    </a-form>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, h } from 'vue';
import { useGbStore } from '@/stores/gbStore';
import { toast } from 'vue3-toastify';
import { useRouter } from 'vue-router';
import { ArrowLeftOutlined, DeleteOutlined, PlusOutlined, UserAddOutlined, ExclamationCircleOutlined } from '@ant-design/icons-vue';
import { Modal } from 'ant-design-vue';

const gbStore = useGbStore();
const router = useRouter();
const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);

const formData = reactive({
  maKhachHang: '',
  hoTen: '',
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
  hoTen: '',
  gioiTinh: '',
  ngaySinh: '',
  soDienThoai: '',
  email: '',
  diaChiErrors: [{}]
});

const validateForm = () => {
  let isValid = true;
  Object.keys(errors).forEach(key => {
    if (key !== 'diaChiErrors') errors[key] = '';
  });
  errors.diaChiErrors = formData.diaChiList.map(() => ({}));

  // Chuẩn hóa các trường văn bản
  formData.hoTen = formData.hoTen?.replace(/\s+/g, ' ').trim() || '';
  formData.soDienThoai = formData.soDienThoai?.replace(/\s+/g, '').trim() || '';
  formData.email = formData.email?.replace(/\s+/g, '').trim() || '';

  // Validate họ tên (từ backend: NotBlank, Size max 100, Pattern chỉ chữ cái)
  if (!formData.hoTen) {
    errors.hoTen = 'Tên khách hàng không được để trống';
    isValid = false;
  } else if (!/^[a-zA-Z\s\u00C0-\u1EF9]+$/.test(formData.hoTen)) {
    errors.hoTen = 'Tên chỉ được chứa chữ cái';
    isValid = false;
  } else if (formData.hoTen.length > 100) {
    errors.hoTen = 'Tên khách hàng không được vượt quá 100 ký tự';
    isValid = false;
  } else if (formData.hoTen.length < 2) {
    errors.hoTen = 'Tên khách hàng không được nhỏ hơn 2 ký tự';
    isValid = false;
  }

  // Validate giới tính (từ backend: NotNull)
  if (formData.gioiTinh === null) {
    errors.gioiTinh = 'Vui lòng chọn giới tính';
    isValid = false;
  }

  // Validate ngày sinh (từ backend: NotNull, PastOrPresent, tuổi >= 13)
  if (!formData.ngaySinh) {
    errors.ngaySinh = 'Ngày sinh không được để trống';
    isValid = false;
  } else {
    const ngaySinh = new Date(formData.ngaySinh);
    const now = new Date();
    if (ngaySinh > now) {
      errors.ngaySinh = 'Ngày sinh không được là ngày trong tương lai';
      isValid = false;
    } else {
      const tuoi = now.getFullYear() - ngaySinh.getFullYear();
      if (tuoi < 14) {
        errors.ngaySinh = 'Khách hàng phải từ 14 tuổi trở lên';
        isValid = false;
      }
    }
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

  // Validate địa chỉ (từ backend: NotBlank cho tất cả các trường, soNha max 255, Pattern chỉ chữ và số)
  if (formData.diaChiList.length === 0) {
    errors.diaChiErrors.push({ general: 'Phải có ít nhất một địa chỉ' });
    isValid = false;
  } else {
    formData.diaChiList.forEach((diaChi, index) => {
      diaChi.soNha = diaChi.soNha?.replace(/\s+/g, ' ').trim() || '';
      diaChi.tinhThanhPho = diaChi.tinhThanhPho?.replace(/\s+/g, ' ').trim() || '';
      diaChi.quanHuyen = diaChi.quanHuyen?.replace(/\s+/g, ' ').trim() || '';
      diaChi.xaPhuong = diaChi.xaPhuong?.replace(/\s+/g, ' ').trim() || '';

      if (!diaChi.tinhThanhPho) {
        errors.diaChiErrors[index].tinhThanhPho = 'Tỉnh/Thành phố không được để trống';
        isValid = false;
      }
      if (!diaChi.quanHuyen) {
        errors.diaChiErrors[index].quanHuyen = 'Quận/Huyện không được để trống';
        isValid = false;
      }
      if (!diaChi.xaPhuong) {
        errors.diaChiErrors[index].xaPhuong = 'Phường/Xã không được để trống';
        isValid = false;
      }
      if (!diaChi.soNha) {
        errors.diaChiErrors[index].soNha = 'Số nhà, tên đường không được để trống';
        isValid = false;
      } else if (!/^[a-zA-Z0-9\s\u00C0-\u1EF9]+$/.test(diaChi.soNha)) {
        errors.diaChiErrors[index].soNha = 'Số nhà, tên đường chỉ được chứa chữ cái và số';
        isValid = false;
      } else if (diaChi.soNha.length > 255) {
        errors.diaChiErrors[index].soNha = 'Số nhà, tên đường không được vượt quá 255 ký tự';
        isValid = false;
      }
    });
  }

  // Đảm bảo có ít nhất một địa chỉ mặc định
  if (!formData.diaChiList.some(d => d.diaChiMacDinh) && formData.diaChiList.length > 0) {
    formData.diaChiList[0].diaChiMacDinh = true;
  }

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
  const diaChi = formData.diaChiList[index];
  if (diaChi.tinhThanhPho) {
    try {
      const provinceCode = provinces.value.find(p => p.name === diaChi.tinhThanhPho)?.code;
      if (!provinceCode) {
        console.error('Không tìm thấy provinceCode cho tỉnh:', diaChi.tinhThanhPho);
        return;
      }
      const response = await fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`);
      const data = await response.json();
      districts.value[index] = data.districts || [];
      diaChi.quanHuyen = '';
      diaChi.xaPhuong = '';
      wards.value[index] = [];
    } catch (error) {
      console.error('Lỗi khi tải quận/huyện:', error);
    }
  }
};

const handleDistrictChange = async (index) => {
  const diaChi = formData.diaChiList[index];
  if (diaChi.quanHuyen) {
    try {
      const districtCode = districts.value[index].find(d => d.name === diaChi.quanHuyen)?.code;
      if (!districtCode) {
        console.error('Không tìm thấy districtCode cho quận/huyện:', diaChi.quanHuyen);
        return;
      }
      const response = await fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`);
      const data = await response.json();
      wards.value[index] = data.wards || [];
      diaChi.xaPhuong = '';
    } catch (error) {
      console.error('Lỗi khi tải phường/xã:', error);
    }
  }
};

const themDiaChi = () => {
  if (formData.diaChiList.length >= 5) {
    toast.error('Không thể thêm quá 5 địa chỉ!');
    return;
  }
  formData.diaChiList.push({
    soNha: '',
    xaPhuong: '',
    quanHuyen: '',
    tinhThanhPho: '',
    diaChiMacDinh: false
  });
  districts.value.push([]);
  wards.value.push([]);
  errors.diaChiErrors.push({});
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
      diaChi.diaChiMacDinh = i === index;
    });
  } else if (!formData.diaChiList.some(d => d.diaChiMacDinh)) {
    formData.diaChiList[0].diaChiMacDinh = true;
  }
};

const resetForm = () => {
  Object.assign(formData, {
    maKhachHang: '',
    hoTen: '',
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
  if (dataToSend.ngaySinh) {
    dataToSend.ngaySinh = new Date(dataToSend.ngaySinh).toISOString();
  }

  try {
    const result = await gbStore.themKhachHang(dataToSend);
    if (result) {
      toast.success('Thêm khách hàng thành công!', {
        autoClose: 2000,
        position: 'top-right'
      });
      setTimeout(() => {
        router.push('/admin/quanlykhachhang');
      }, 2000);
    }
  } catch (error) {
    console.error('Lỗi khi thêm khách hàng:', error);
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
      toast.error('Có lỗi xảy ra khi thêm khách hàng');
    }
  }
};

const confirmThemKhachHang = () => {
  Modal.confirm({
    title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
      h(UserAddOutlined, { style: 'color: #52c41a; font-size: 22px;' }),
      h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Tạo tài khoản khách hàng')
    ]),
    content: () => h('div', { style: 'padding: 8px 0;' }, [
      h('p', { style: 'margin: 0 0 12px 0; font-size: 14px;' }, 'Bạn có chắc chắn muốn tạo tài khoản khách hàng này không?'),
      h('div', { style: 'background: #f6ffed; padding: 12px; border-radius: 6px; border: 1px solid #b7eb8f;' }, [
        h('div', { style: 'display: flex; align-items: center; gap: 8px; color: #52c41a;' }, [
          h(ExclamationCircleOutlined, { style: 'font-size: 14px;' }),
          h('span', { style: 'font-size: 13px;' }, 'Mật khẩu sẽ được gửi qua email')
        ])
      ])
    ]),
    okText: 'Tạo tài khoản',
    cancelText: 'Hủy',
    okButtonProps: { size: 'large', style: { height: '38px', background: '#52c41a', borderColor: '#52c41a' } },
    cancelButtonProps: { size: 'large', style: { height: '38px' } },
    centered: true,
    width: 450,
    onOk: () => {
      themKhachHang();
    }
  });
};

onMounted(async () => {
  await loadProvinces();
  districts.value = [[]];
  wards.value = [[]];
});
</script>

<style scoped>
:root {
  --primary-color: #1890ff;
  --text-color: #1f2a44;
  --border-color: #d9d9d9;
  --background-color: #ffffff;
  --danger-color: #ff4d4f;
  --shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  --border-radius: 6px;
}

.container {
  max-width: 1200px;
  margin: 24px auto;
  padding: 24px;
  background: var(--background-color);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-section h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-color);
  margin: 0;
}

.btn-back {
  color: var(--text-color);
  font-weight: 500;
}

.btn-back:hover {
  color: var(--primary-color);
}

.form-container {
  padding: 24px;
  background: #fafafa;
  border-radius: var(--border-radius);
}

.input-field,
.select-field {
  border-radius: var(--border-radius);
  transition: border-color 0.3s, box-shadow 0.3s;
}

.input-field:focus,
.select-field:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.radio-group {
  display: flex;
  gap: 24px;
  padding: 8px 0;
}

.address-section {
  background: var(--background-color);
  padding: 16px;
  margin-bottom: 16px;
  border-radius: var(--border-radius);
  box-shadow: var(--shadow);
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.address-header h3 {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color);
  margin: 0;
}

.btn-add-address {
  margin: 16px 0;
  color: var(--primary-color);
  border-color: var(--primary-color);
  background: #e6f7ff;
}

.btn-add-address:hover {
  background: #bae7ff;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.form-actions .ant-btn {
  border-radius: var(--border-radius);
  padding: 8px 24px;
}

.form-actions .ant-btn-primary:hover {
  background: #40a9ff;
}

.form-actions .ant-btn-default {
  border-color: var(--border-color);
  color: var(--text-color);
}

.form-actions .ant-btn-default:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}
</style>
