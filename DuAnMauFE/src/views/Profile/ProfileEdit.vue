<template>
  <div class="profile-edit">
    <h2 class="page-title">Chỉnh sửa thông tin cá nhân</h2>

    <a-form
      :model="formData"
      :rules="rules"
      layout="vertical"
      @finish="handleSubmit"
      class="edit-form"
    >
      <div class="form-grid">
        <!-- Họ và tên -->
        <a-form-item label="Họ và tên" name="hoTen" required>
          <a-input 
            v-model:value="formData.hoTen" 
            placeholder="Nhập họ và tên"
            size="large"
          />
        </a-form-item>

        <!-- Email (readonly) -->
        <a-form-item label="Email">
          <a-input 
            :value="formData.email" 
            disabled 
            size="large"
          >
            <template #suffix>
              <i class="fas fa-lock" style="color: #999"></i>
            </template>
          </a-input>
          <small style="color: #999;">Email không thể thay đổi</small>
        </a-form-item>

        <!-- Số điện thoại -->
        <a-form-item label="Số điện thoại" name="soDienThoai">
          <a-input 
            v-model:value="formData.soDienThoai" 
            placeholder="Nhập số điện thoại"
            size="large"
          />
        </a-form-item>

        <!-- Giới tính -->
        <a-form-item label="Giới tính" name="gioiTinh">
          <a-radio-group v-model:value="formData.gioiTinh" size="large">
            <a-radio :value="true">Nam</a-radio>
            <a-radio :value="false">Nữ</a-radio>
            <a-radio :value="null">Khác</a-radio>
          </a-radio-group>
        </a-form-item>

        <!-- Ngày sinh -->
        <a-form-item label="Ngày sinh" name="ngaySinh">
          <a-date-picker 
            v-model:value="formData.ngaySinh"
            placeholder="Chọn ngày sinh"
            format="DD/MM/YYYY"
            size="large"
            style="width: 100%"
          />
        </a-form-item>

        <!-- Địa chỉ -->
        <a-form-item label="Địa chỉ" name="diaChi" class="full-width">
          <a-textarea 
            v-model:value="formData.diaChi"
            placeholder="Nhập địa chỉ"
            :rows="3"
            size="large"
          />
        </a-form-item>
      </div>

      <!-- Actions -->
      <div class="form-actions">
        <a-button 
          type="default" 
          size="large"
          @click="handleCancel"
        >
          Hủy
        </a-button>
        <a-button 
          type="primary" 
          html-type="submit" 
          size="large"
          :loading="isSubmitting"
        >
          Lưu thay đổi
        </a-button>
      </div>
    </a-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { toast } from 'vue3-toastify';
import { khachHangService } from '@/services/khachHangService';
import dayjs from 'dayjs';

const router = useRouter();
const isSubmitting = ref(false);

const formData = ref({
  idKhachHang: null,
  maKhachHang: '',
  hoTen: '',
  email: '',
  soDienThoai: '',
  gioiTinh: true,
  ngaySinh: null,
  diaChi: '',
  trangThai: 'Đang hoạt động'
});

const rules = {
  hoTen: [
    { required: true, message: 'Vui lòng nhập họ và tên', trigger: 'blur' },
    { min: 4, message: 'Họ tên phải có ít nhất 4 ký tự', trigger: 'blur' }
  ],
  soDienThoai: [
    { pattern: /^(84|0[3|5|7|8|9])[0-9]{8}$/, message: 'Số điện thoại không hợp lệ', trigger: 'blur' }
  ]
};

// Load customer data
onMounted(async () => {
  try {
    const khachHang = JSON.parse(localStorage.getItem('khachHang') || sessionStorage.getItem('khachHang'));
    
    if (!khachHang || !khachHang.idKhachHang) {
      toast.error('Không tìm thấy thông tin khách hàng');
      router.push('/profile');
      return;
    }

    // Get full customer details from API
    const response = await khachHangService.getKhachHangDetail(khachHang.idKhachHang);
    
    if (response.error) {
      toast.error(response.message || 'Không thể tải thông tin');
      return;
    }

    // Map data to form
    const customer = response.khachHang;
    formData.value = {
      idKhachHang: customer.idKhachHang,
      maKhachHang: customer.maKhachHang,
      hoTen: customer.hoTen,
      email: customer.email,
      soDienThoai: customer.soDienThoai,
      gioiTinh: customer.gioiTinh,
      ngaySinh: customer.ngaySinh ? dayjs(customer.ngaySinh) : null,
      diaChi: customer.diaChi || '',
      trangThai: customer.trangThai
    };

  } catch (error) {
    console.error('Error loading customer data:', error);
    toast.error('Có lỗi xảy ra khi tải thông tin');
  }
});

const handleSubmit = async () => {
  try {
    isSubmitting.value = true;

    const updateData = {
      idKhachHang: formData.value.idKhachHang,
      maKhachHang: formData.value.maKhachHang,
      hoTen: formData.value.hoTen,
      email: formData.value.email,
      soDienThoai: formData.value.soDienThoai,
      gioiTinh: formData.value.gioiTinh,
      ngaySinh: formData.value.ngaySinh ? formData.value.ngaySinh.toDate() : null,
      diaChi: formData.value.diaChi,
      trangThai: formData.value.trangThai,
      diaChiList: [] // Empty array, addresses managed separately
    };

    const result = await khachHangService.suaKhachHang(updateData);

    if (result.error) {
      toast.error(result.message || 'Cập nhật thất bại');
      return;
    }

    // Update localStorage with new data
    const updatedCustomer = result.khachHang;
    const storageKey = localStorage.getItem('khachHang') ? 'khachHang' : 'sessionStorage';
    
    if (storageKey === 'khachHang') {
      localStorage.setItem('khachHang', JSON.stringify(updatedCustomer));
    } else {
      sessionStorage.setItem('khachHang', JSON.stringify(updatedCustomer));
    }

    toast.success('Cập nhật thông tin thành công!');
    router.push('/profile');

  } catch (error) {
    console.error('Error updating profile:', error);
    toast.error('Có lỗi xảy ra khi cập nhật');
  } finally {
    isSubmitting.value = false;
  }
};

const handleCancel = () => {
  router.push('/profile');
};
</script>

<style scoped>
.profile-edit {
  max-width: 100%;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #ff6600;
}

.edit-form {
  max-width: 800px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px 24px;
  margin-bottom: 32px;
}

.form-grid .full-width {
  grid-column: span 2;
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #333;
}

:deep(.ant-input),
:deep(.ant-input-number),
:deep(.ant-picker),
:deep(.ant-select-selector) {
  border-radius: 8px;
}

:deep(.ant-input:focus),
:deep(.ant-input-number:focus),
:deep(.ant-picker:focus),
:deep(.ant-select-focused .ant-select-selector) {
  border-color: #ff6600;
  box-shadow: 0 0 0 2px rgba(255, 102, 0, 0.1);
}

:deep(.ant-radio-checked .ant-radio-inner) {
  border-color: #ff6600;
  background-color: #ff6600;
}

:deep(.ant-radio:hover .ant-radio-inner) {
  border-color: #ff6600;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

:deep(.ant-btn-primary) {
  background-color: #ff6600;
  border-color: #ff6600;
}

:deep(.ant-btn-primary:hover) {
  background-color: #e55a00;
  border-color: #e55a00;
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-grid .full-width {
    grid-column: span 1;
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .form-actions button {
    width: 100%;
  }
}
</style>
