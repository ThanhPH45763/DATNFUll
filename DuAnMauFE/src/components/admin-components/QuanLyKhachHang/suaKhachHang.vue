<template>
  <div class="container">
    <div class="header-section">
      <h2>Sửa thông tin khách hàng</h2>
      <a-button type="text" class="btn-back" @click="router.push('/admin/quanlykhachhang')">
        <template #icon><arrow-left-outlined /></template>
        Quay lại
      </a-button>
    </div>
    <a-form :model="formData" :label-col="{ span: 24 }" :wrapper-col="{ span: 24 }" @submit="confirmSubmitForm"
      @reset="resetForm" class="form-container">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :md="8">
          <a-form-item label="Mã khách hàng">
            <a-input v-model:value="formData.maKhachHang" disabled class="input-field" />
          </a-form-item>
        </a-col>
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
            <a-input v-model:value="formData.email" placeholder="Nhập email" disabled class="input-field" />
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
          style="background-color: #ff6600; border-color: #ff6600; color: white;">
          Cập nhật
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
import { useRouter, useRoute } from 'vue-router';
import { useGbStore } from '@/stores/gbStore';
import { toast } from 'vue3-toastify';
import dayjs from 'dayjs';
import { ArrowLeftOutlined, DeleteOutlined, PlusOutlined, SaveOutlined, ExclamationCircleOutlined } from '@ant-design/icons-vue';
import { Modal } from 'ant-design-vue';

const gbStore = useGbStore();
const router = useRouter();
const route = useRoute();
const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);

const formData = reactive({
  idKhachHang: null,
  maKhachHang: '',
  hoTen: '',
  gioiTinh: null,
  soDienThoai: '',
  ngaySinh: null,
  email: '',
  trangThai: 'Đang hoạt động',
  diaChiList: []
});

const errors = reactive({
  hoTen: '',
  gioiTinh: '',
  ngaySinh: '',
  soDienThoai: '',
  email: '',
  diaChiErrors: []
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
    errors.hoTen = 'Họ tên chỉ được chứa chữ cái';
    isValid = false;
  } else if (formData.hoTen.length > 100) {
    errors.hoTen = 'Tên khách hàng không được vượt quá 100 ký tự';
    isValid = false;
  }
  else if (formData.hoTen.length < 2) {
    errors.hoTen = 'Tên khách hàng không được nhỏ hơn 2 ký tự';
    isValid = false;
  }

  // Validate giới tính (từ backend: NotNull)
  if (formData.gioiTinh === null) {
    errors.gioiTinh = 'Vui lòng chọn giới tính';
    isValid = false;
  }

  // Validate ngày sinh (từ backend: NotNull, PastOrPresent, tuổi >= 14)
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
      const currentQuanHuyen = diaChi.quanHuyen;
      if (!districts.value[index].some(d => d.name === currentQuanHuyen)) {
        diaChi.quanHuyen = '';
        diaChi.xaPhuong = '';
        wards.value[index] = [];
      } else if (diaChi.quanHuyen) {
        await handleDistrictChange(index);
      }
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
      const currentXaPhuong = diaChi.xaPhuong;
      if (!wards.value[index].some(w => w.name === currentXaPhuong)) {
        diaChi.xaPhuong = '';
      }
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
  const currentMaKhachHang = formData.maKhachHang;
  const currentEmail = formData.email;

  Object.assign(formData, {
    idKhachHang: null,
    maKhachHang: currentMaKhachHang,
    hoTen: '',
    gioiTinh: null,
    soDienThoai: '',
    ngaySinh: null,
    email: currentEmail,
    trangThai: 'Đang hoạt động',
    diaChiList: [{
      soNha: '',
      xaPhuong: '',
      quanHuyen: '',
      tinhThanhPho: '',
      diaChiMacDinh: true
    }]
  });

  districts.value = [[]];
  wards.value = [[]];
  Object.assign(errors, {
    hoTen: '',
    gioiTinh: '',
    ngaySinh: '',
    soDienThoai: '',
    email: '',
    diaChiErrors: [{}]
  });
};

const loadKhachHang = async () => {
  try {
    const id = route.params.id;
    const khachHang = await gbStore.getKhachHangByIdForEdit(id);

    if (!khachHang) {
      toast.error('Không tìm thấy thông tin khách hàng');
      router.push('/admin/quanlykhachhang');
      return;
    }

    Object.assign(formData, {
      idKhachHang: khachHang.idKhachHang,
      maKhachHang: khachHang.maKhachHang,
      hoTen: khachHang.hoTen,
      gioiTinh: khachHang.gioiTinh,
      soDienThoai: khachHang.soDienThoai,
      ngaySinh: khachHang.ngaySinh ? dayjs(khachHang.ngaySinh) : null,
      email: khachHang.email,
      trangThai: khachHang.trangThai || 'Đang hoạt động',
      diaChiList: []
    });

    if (khachHang.diaChiList && khachHang.diaChiList.length > 0) {
      formData.diaChiList = khachHang.diaChiList.map((diaChi, index) => {
        districts.value[index] = [];
        wards.value[index] = [];
        errors.diaChiErrors[index] = {};
        return {
          soNha: diaChi.soNha || '',
          xaPhuong: diaChi.xaPhuong || '',
          quanHuyen: diaChi.quanHuyen || '',
          tinhThanhPho: diaChi.tinhThanhPho || '',
          diaChiMacDinh: diaChi.diaChiMacDinh || false
        };
      });

      for (let index = 0; index < formData.diaChiList.length; index++) {
        const diaChi = formData.diaChiList[index];
        if (diaChi.tinhThanhPho) {
          await handleProvinceChange(index);
          if (diaChi.quanHuyen) {
            await handleDistrictChange(index);
          }
        }
      }
    } else {
      formData.diaChiList.push({
        soNha: '',
        xaPhuong: '',
        quanHuyen: '',
        tinhThanhPho: '',
        diaChiMacDinh: true
      });
      districts.value.push([]);
      wards.value.push([]);
      errors.diaChiErrors.push({});
    }
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu khách hàng:', error);
    toast.error('Không thể tải thông tin khách hàng');
    router.push('/admin/quanlykhachhang');
  }
};

const confirmSubmitForm = async () => {
  Modal.confirm({
    title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
      h(SaveOutlined, { style: 'color: #1890ff; font-size: 22px;' }),
      h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Cập nhật thông tin khách hàng')
    ]),
    content: () => h('div', { style: 'padding: 8px 0;' }, [
      h('p', { style: 'margin: 0 0 12px 0; font-size: 14px;' }, 'Bạn có chắc chắn muốn cập nhật thông tin khách hàng này không?'),
      h('div', { style: 'background: #e6f7ff; padding: 12px; border-radius: 6px; border: 1px solid #91d5ff;' }, [
        h('div', { style: 'display: flex; align-items: center; gap: 8px; color: #1890ff;' }, [
          h(ExclamationCircleOutlined, { style: 'font-size: 14px;' }),
          h('span', { style: 'font-size: 13px;' }, 'Thông tin sẽ được lưu ngay lập tức')
        ])
      ])
    ]),
    okText: 'Cập nhật',
    cancelText: 'Hủy',
    okButtonProps: { size: 'large', style: { height: '38px' } },
    cancelButtonProps: { size: 'large', style: { height: '38px' } },
    centered: true,
    width: 450,
    async onOk() {
      if (!validateForm()) {
        toast.error('Vui lòng điền đầy đủ và chính xác thông tin!');
        return;
      }

      const dataToSend = { ...formData };
      if (dataToSend.ngaySinh) {
        dataToSend.ngaySinh = dataToSend.ngaySinh.toISOString();
      }

      try {
        const result = await gbStore.suaKhachHang(dataToSend);
        if (result) {
          toast.success('Cập nhật khách hàng thành công!', {
            autoClose: 2000,
            position: 'top-right'
          });

          setTimeout(() => {
            router.push('/admin/quanlykhachhang');
          }, 2000);
        }
      } catch (error) {
        console.error('Lỗi khi cập nhật khách hàng:', error);
        toast.error('Có lỗi xảy ra khi cập nhật khách hàng');
      }
    }
  });
};

onMounted(async () => {
  await loadProvinces();
  await loadKhachHang();
});
</script>

<style scoped>
:root {
  --primary-color: #ff6600;
  --secondary-color: #1890ff;
  --text-color: #1f2a44;
  --border-color: #d9d9d9;
  --background-color: #ffffff;
  --danger-color: #ff4d4f;
  --shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  --border-radius: 8px;
}

.container {
  max-width: 100%;
  width: 100%;
  margin: 0;
  padding: 32px;
  background: #f5f5f5;
  min-height: calc(100vh - 64px);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e8e8e8;
}

.header-section h2 {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-color);
  margin: 0;
}

.btn-back {
  color: var(--text-color);
  font-weight: 500;
  font-size: 15px;
  padding: 8px 16px;
  border-radius: var(--border-radius);
}

.btn-back:hover {
  color: var(--primary-color);
  background: #fff5f0;
}

.form-container {
  padding: 32px;
  background: var(--background-color);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow);
  border: 1px solid #e8e8e8;
}

/* Input và Select fields đồng bộ */
.input-field,
.select-field,
:deep(.ant-input),
:deep(.ant-select-selector),
:deep(.ant-picker) {
  height: 40px !important;
  border-radius: 6px !important;
  border: 1px solid var(--border-color) !important;
  font-size: 14px !important;
  transition: all 0.3s ease !important;
}

:deep(.ant-input):hover,
:deep(.ant-select-selector):hover,
:deep(.ant-picker):hover {
  border-color: var(--primary-color) !important;
}

.input-field:focus,
.select-field:focus,
:deep(.ant-input):focus,
:deep(.ant-select-focused .ant-select-selector),
:deep(.ant-picker-focused) {
  border-color: var(--primary-color) !important;
  box-shadow: 0 0 0 3px rgba(255, 102, 0, 0.1) !important;
  outline: none !important;
}

/* Label styling */
:deep(.ant-form-item-label > label) {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-color);
}

/* Date picker full width */
:deep(.ant-picker) {
  width: 100%;
}

.radio-group {
  display: flex;
  gap: 24px;
  padding: 8px 0;
}

:deep(.ant-radio-wrapper) {
  font-size: 14px;
}

.address-section {
  background: #fafafa;
  padding: 24px;
  margin-bottom: 24px;
  border-radius: var(--border-radius);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  border: 1px solid #e8e8e8;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.address-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color);
  margin: 0;
}

.btn-add-address {
  margin: 24px 0;
  height: 48px;
  color: var(--primary-color);
  border-color: var(--primary-color);
  background: #fff5f0;
  font-weight: 500;
  font-size: 15px;
  border-radius: var(--border-radius);
}

.btn-add-address:hover {
  background: #ffe0cc;
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 2px solid #e8e8e8;
}

.form-actions .ant-btn {
  height: 44px;
  border-radius: var(--border-radius);
  padding: 0 32px;
  font-size: 15px;
  font-weight: 500;
}

.form-actions .ant-btn-primary {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.form-actions .ant-btn-primary:hover {
  background: #ff7f29;
  border-color: #ff7f29;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 102, 0, 0.3);
}

.form-actions .ant-btn-default {
  border-color: var(--border-color);
  color: var(--text-color);
}

.form-actions .ant-btn-default:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

/* Checkbox styling */
:deep(.ant-checkbox-wrapper) {
  font-size: 14px;
}

:deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

:deep(.ant-checkbox:hover .ant-checkbox-inner) {
  border-color: var(--primary-color);
}

/* Error state */
:deep(.ant-form-item-has-error .ant-input),
:deep(.ant-form-item-has-error .ant-select-selector),
:deep(.ant-form-item-has-error .ant-picker) {
  border-color: var(--danger-color) !important;
}

:deep(.ant-form-item-has-error .ant-input:focus),
:deep(.ant-form-item-has-error .ant-select-focused .ant-select-selector),
:deep(.ant-form-item-has-error .ant-picker-focused) {
  box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.1) !important;
}

</style>
