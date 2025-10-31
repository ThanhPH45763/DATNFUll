<template>
  <div class="profile-page">
    <div class="profile-header">
      <h2 class="page-title">Hồ sơ của tôi</h2>
    </div>
    
    <div class="profile-content">
      <div class="profile-card">
        <div class="profile-avatar">
          <img :src="nhanVien?.anhNhanVien || defaultAvatar" alt="Avatar">
          <h3 class="profile-name">{{ nhanVien?.tenNhanVien }}</h3>
          <p class="profile-role">{{ getRoleName(nhanVien?.taiKhoan?.roles?.id_roles) }}</p>
          <a-tag :color="nhanVien?.trangThai === 'Đang hoạt động' ? 'green' : 'red'" class="status-tag">
            {{ nhanVien?.trangThai }}
          </a-tag>
        </div>

        <div class="profile-details">
          <div class="detail-section">
            <h4>Thông tin cá nhân</h4>
            <div class="detail-row">
              <div class="detail-item">
                <div class="detail-label">Mã nhân viên</div>
                <div class="detail-value">{{ nhanVien?.maNhanVien }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">Giới tính</div>
                <div class="detail-value">{{ nhanVien?.gioiTinh ? 'Nam' : 'Nữ' }}</div>
              </div>
            </div>
            
            <div class="detail-row">
              <div class="detail-item">
                <div class="detail-label">Ngày sinh</div>
                <div class="detail-value">{{ formatDate(nhanVien?.ngaySinh) }}</div>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h4>Thông tin liên hệ</h4>
            <div class="detail-row">
              <div class="detail-item">
                <div class="detail-label">Email</div>
                <div class="detail-value">{{ nhanVien?.email }}</div>
              </div>
              <div class="detail-item">
                <div class="detail-label">Số điện thoại</div>
                <div class="detail-value">{{ nhanVien?.soDienThoai }}</div>
              </div>
            </div>
            
            <div class="detail-row">
              <div class="detail-item full-width">
                <div class="detail-label">Địa chỉ</div>
                <div class="detail-value address">{{ nhanVien?.diaChiLienHe }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useGbStore } from '@/stores/gbStore';
import dayjs from 'dayjs';

const store = useGbStore();
const nhanVien = ref(null);
const defaultAvatar = 'https://static.vecteezy.com/system/resources/previews/019/879/186/non_2x/user-icon-on-transparent-background-free-png.png';

// Format ngày tháng
const formatDate = (date) => {
  if (!date) return '';
  return dayjs(date).format('DD/MM/YYYY');
};

// Lấy tên vai trò
const getRoleName = (roleId) => {
  const roles = {
    1: 'Admin',
    2: 'Quản lý',
    3: 'Nhân viên'
  };
  return roles[roleId] || 'Không xác định';
};

onMounted(() => {
  try {
    // Lấy thông tin từ sessionStorage
    const userDetailsStr = sessionStorage.getItem('userDetails');
    if (userDetailsStr) {
      nhanVien.value = JSON.parse(userDetailsStr);
      console.log('Thông tin nhân viên từ sessionStorage:', nhanVien.value);
    } else {
      // Fallback vào store nếu cần
      nhanVien.value = store.userDetails;
    }
  } catch (error) {
    console.error('Lỗi khi lấy thông tin nhân viên:', error);
  }
});
</script>

<style scoped>
.profile-page {
  width: 100%;
}

.profile-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.profile-content {
  width: 100%;
}

.profile-card {
  display: flex;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.profile-avatar {
  width: 280px;
  padding: 30px 20px;
  background: linear-gradient(to bottom, #f6f6f6, #ffffff);
  text-align: center;
  border-right: 1px solid #f0f0f0;
}

.profile-avatar img {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 16px;
  border: 4px solid white;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.profile-name {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #1a1a1a;
}

.profile-role {
  color: #666;
  margin-bottom: 12px;
  font-size: 14px;
}

.status-tag {
  font-size: 12px;
  padding: 0 10px;
}

.profile-details {
  flex: 1;
  padding: 30px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
  color: #1a1a1a;
}

.detail-row {
  display: flex;
  margin-bottom: 16px;
}

.detail-item {
  flex: 1;
  min-width: 0;
}

.full-width {
  width: 100%;
}

.detail-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 6px;
}

.detail-value {
  font-size: 15px;
  color: #262626;
  font-weight: 500;
}

.address {
  line-height: 1.5;
}

@media (max-width: 768px) {
  .profile-card {
    flex-direction: column;
  }
  
  .profile-avatar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
  }
  
  .detail-row {
    flex-direction: column;
  }
  
  .detail-item {
    margin-bottom: 16px;
  }
}
</style>
