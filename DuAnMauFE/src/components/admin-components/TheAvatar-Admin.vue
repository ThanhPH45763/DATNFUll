<template>
  <div class="d-flex align-items-center">
    <!-- Hiển thị tên tài khoản -->
    <span class="ms-2 me-2" style="font-family: Verdana, Geneva, Tahoma, sans-serif;">
      {{ store.userDetails?.tenNhanVien }}
    </span>

    <!-- Dropdown với avatar -->
    <a-dropdown>
      <a-avatar size="large" :src="store.userDetails?.anhNhanVien">
        <template #icon v-if="!store.userDetails?.anhNhanVien">
          <UserOutlined />
        </template>
      </a-avatar>

      <template #overlay>
        <a-menu>
          <a-menu-item key="0">
            <a style="text-decoration: none;" @click="goToProfile">
              Hồ sơ
            </a>
          </a-menu-item>
          <!-- <a-menu-item key="0">
            <a target="_blank" style="text-decoration: none;" rel="noopener noreferrer" href="#">
              Đổi mật khẩu
            </a>
          </a-menu-item> -->
          <a-menu-item key="1">
            <a style="text-decoration: none;" @click="showPasswordModal = true">
              Đổi mật khẩu
            </a>
          </a-menu-item>
          <a-menu-item key="2">
            <a style="text-decoration: none;" @click="store.logout()">
              Đăng xuất
            </a>
          </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
    
    <!-- Modal đổi mật khẩu -->
    <a-modal v-model:visible="showPasswordModal" title="Đổi mật khẩu" @ok="changePassword" :confirmLoading="isLoading" @cancel="resetPasswordForm">
      <a-form :model="passwordForm" layout="vertical">
        <a-form-item label="Mật khẩu hiện tại" name="currentPassword">
          <a-input-password v-model:value="passwordForm.currentPassword" placeholder="Nhập mật khẩu hiện tại" />
        </a-form-item>
        <a-form-item label="Mật khẩu mới" name="newPassword">
          <a-input-password v-model:value="passwordForm.newPassword" placeholder="Nhập mật khẩu mới" />
        </a-form-item>
        <a-form-item label="Xác nhận mật khẩu mới" name="confirmPassword">
          <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="Nhập lại mật khẩu mới" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { UserOutlined } from '@ant-design/icons-vue';
import { useGbStore } from '@/stores/gbStore';
import { onMounted, reactive, ref } from 'vue';
import { khachHangService } from '@/services/khachHangService';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import axiosInstance from '@/config/axiosConfig';
const store = useGbStore();
const router = useRouter();
const goToProfile = () => {
  router.push('/admin/profile');
};
// Form đổi mật khẩu
const passwordForm = reactive({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
});
const resetPasswordForm = () => {
    passwordForm.currentPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
    showPasswordModal.value = false; // Đóng modal
};
const isLoading = ref(false);
const showPasswordModal = ref(false);
// Xử lý đổi mật khẩu
const changePassword = async () => {
    const { currentPassword, newPassword, confirmPassword } = passwordForm;

    // Kiểm tra trống
    if (!currentPassword || !newPassword || !confirmPassword) {
        return message.error('Vui lòng nhập đầy đủ thông tin');
    }

    // Kiểm tra mật khẩu mới độ dài tối thiểu
    if (newPassword.length < 6) {
        return message.error('Mật khẩu mới phải có ít nhất 6 ký tự');
    }
    if (newPassword.length > 20) {
        return message.error('Mật khẩu mới không được quá 20 ký tự');
    }

    // Không chứa khoảng trắng
    if (/\s/.test(newPassword)) {
        return message.error('Mật khẩu mới không được chứa khoảng trắng');
    }

    // Kiểm tra khớp mật khẩu xác nhận
    if (newPassword !== confirmPassword) {
        return message.error('Mật khẩu xác nhận không khớp');
    }

    try {
        isLoading.value = true;

        const response = await axiosInstance.post('api/khach-hang/change-password', {
            oldPassword: currentPassword,
            newPassword
        });

        // Kiểm tra phản hồi từ API
        if (response.data.successMessage) {
            message.success(response.data.successMessage);

            // Reset form
            passwordForm.currentPassword = '';
            passwordForm.newPassword = '';
            passwordForm.confirmPassword = '';
            showPasswordModal.value = false; // Đóng modal nếu muốn
        } else {
            message.error(response.data.error || 'Có lỗi xảy ra khi đổi mật khẩu');
        }
    } catch (error) {
        // Xử lý lỗi từ API
        if (error.response?.status === 403) {
            message.error('Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại!');
            localStorage.removeItem('token');
            router.push('/login-register/loginAdmin');
        } else if (error.response?.data?.error) {
            message.error(error.response.data.error);
        } else {
            message.error('Có lỗi xảy ra khi đổi mật khẩu');
        }
        console.error(error);
    } finally {
        isLoading.value = false;
    }
};

// Kiểm tra trạng thái khi component được mount
onMounted(() => {
  console.log('Store trong TheAvatar (onMounted):', store);
  console.log('User Info trong TheAvatar:', store.userInfo);
  console.log('User Details trong TheAvatar:', store.userDetails);

  // Nếu không có userDetails nhưng đã đăng nhập, thử lấy lại thông tin chi tiết
  if (store.isLoggedIn && !store.userDetails && store.userInfo?.ten_dang_nhap) {
    console.log('Khôi phục userDetails từ API...');
    khachHangService.getUserDetail({
      username: store.userInfo.ten_dang_nhap,
      id_roles: store.id_roles
    }).then(userDetails => {
      store.userDetails = userDetails;
      sessionStorage.setItem('userDetails', JSON.stringify(userDetails));
      if (localStorage.getItem('isLoggedIn') === 'true') {
        localStorage.setItem('userDetails', JSON.stringify(userDetails));
      }
      console.log('User Details sau khi khôi phục:', store.userDetails);
    }).catch(error => {
      console.error('Lỗi khi khôi phục userDetails:', error);
    });
  }
});
</script>