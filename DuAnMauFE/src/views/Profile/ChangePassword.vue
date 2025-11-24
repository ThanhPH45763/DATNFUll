<template>
  <div class="change-password">
    <h2 class="page-title">Đổi mật khẩu</h2>

    <div class="password-form-container">
      <a-form
        :model="formData"
        :rules="rules"
        layout="vertical"
        @finish="handleSubmit"
        class="password-form"
      >
        <!-- Current Password -->
        <a-form-item label="Mật khẩu hiện tại" name="currentPassword" required>
          <a-input-password 
            v-model:value="formData.currentPassword" 
            placeholder="Nhập mật khẩu hiện tại"
            size="large"
          >
            <template #prefix>
              <i class="fas fa-lock" style="color: #999"></i>
            </template>
          </a-input-password>
        </a-form-item>

        <!-- New Password -->
        <a-form-item label="Mật khẩu mới" name="newPassword" required>
          <a-input-password 
            v-model:value="formData.newPassword" 
            placeholder="Nhập mật khẩu mới"
            size="large"
          >
            <template #prefix>
              <i class="fas fa-key" style="color: #999"></i>
            </template>
          </a-input-password>
          <small style="color: #666;">Mật khẩu phải có 6-20 ký tự, không chứa khoảng trắng</small>
        </a-form-item>

        <!-- Confirm Password -->
        <a-form-item label="Xác nhận mật khẩu mới" name="confirmPassword" required>
          <a-input-password 
            v-model:value="formData.confirmPassword" 
            placeholder="Nhập lại mật khẩu mới"
            size="large"
          >
            <template #prefix>
              <i class="fas fa-check-circle" style="color: #999"></i>
            </template>
          </a-input-password>
        </a-form-item>

        <!-- Security Tips -->
        <div class="security-tips">
          <i class="fas fa-info-circle"></i>
          <div>
            <strong>Lưu ý bảo mật:</strong>
            <ul>
              <li>Không sử dụng mật khẩu quá đơn giản</li>
              <li>Nên thay đổi mật khẩu định kỳ</li>
              <li>Không chia sẻ mật khẩu với người khác</li>
            </ul>
          </div>
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
            Đổi mật khẩu
          </a-button>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { toast } from 'vue3-toastify';
import axiosInstance from '@/config/axiosConfig';

const router = useRouter();
const isSubmitting = ref(false);

const formData = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// Custom validator for password match
const validatePasswordMatch = async (rule, value) => {
  if (value && value !== formData.value.newPassword) {
    return Promise.reject('Mật khẩu xác nhận không khớp');
  }
  return Promise.resolve();
};

const rules = {
  currentPassword: [
    { required: true, message: 'Vui lòng nhập mật khẩu hiện tại', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: 'Vui lòng nhập mật khẩu mới', trigger: 'blur' },
    { min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự', trigger: 'blur' },
    { max: 20, message: 'Mật khẩu không được vượt quá 20 ký tự', trigger: 'blur' },
    { pattern: /^\S+$/, message: 'Mật khẩu không được chứa khoảng trắng', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Vui lòng xác nhận mật khẩu mới', trigger: 'blur' },
    { validator: validatePasswordMatch, trigger: 'blur' }
  ]
};

const handleSubmit = async () => {
  try {
    isSubmitting.value = true;

    // Get customer email
    const khachHang = JSON.parse(localStorage.getItem('khachHang') || sessionStorage.getItem('khachHang'));
    
    if (!khachHang || !khachHang.email) {
      toast.error('Không tìm thấy thông tin tài khoản');
      return;
    }

    // Call change password API
    const response = await axiosInstance.post(
      `/api/khach-hang/change-password?email=${khachHang.email}`,
      {
        oldPassword: formData.value.currentPassword,
        newPassword: formData.value.newPassword
      }
    );

    if (response.data.successMessage) {
      toast.success('Đổi mật khẩu thành công!');
      
      // Clear form
      formData.value = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      };

      // Redirect to profile after 1.5s
      setTimeout(() => {
        router.push('/profile');
      }, 1500);
    } else {
      toast.error(response.data.error || 'Đổi mật khẩu thất bại');
    }

  } catch (error) {
    console.error('Error changing password:', error);
    const errorMessage = error.response?.data?.error || 'Có lỗi xảy ra khi đổi mật khẩu';
    toast.error(errorMessage);
  } finally {
    isSubmitting.value = false;
  }
};

const handleCancel = () => {
  router.push('/profile');
};
</script>

<style scoped>
.change-password {
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

.password-form-container {
  max-width: 600px;
  margin: 0 auto;
  background: #f9f9f9;
  padding: 32px;
  border-radius: 12px;
}

.password-form {
  background: white;
  padding: 24px;
  border-radius: 8px;
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #333;
}

:deep(.ant-input-affix-wrapper),
:deep(.ant-input-password) {
  border-radius: 8px;
}

:deep(.ant-input-affix-wrapper:focus),
:deep(.ant-input-affix-wrapper-focused) {
  border-color: #ff6600;
  box-shadow: 0 0 0 2px rgba(255, 102, 0, 0.1);
}

.security-tips {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fff7e6;
  border-left: 4px solid #ff6600;
  border-radius: 8px;
  margin-bottom: 24px;
}

.security-tips i {
  font-size: 20px;
  color: #ff6600;
  margin-top: 2px;
}

.security-tips strong {
  color: #333;
  font-size: 14px;
}

.security-tips ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
}

.security-tips li {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
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
  .password-form-container {
    padding: 20px;
  }

  .password-form {
    padding: 16px;
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .form-actions button {
    width: 100%;
  }
}
</style>
