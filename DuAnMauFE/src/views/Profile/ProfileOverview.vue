<template>
  <div class="profile-overview">
    <h2 class="page-title">Tổng quan tài khoản</h2>
    
    <div class="overview-grid">
      <!-- Profile Info Card -->
      <div class="info-card">
        <div class="card-header">
          <i class="fas fa-user-circle"></i>
          <h3>Thông tin cá nhân</h3>
        </div>
        <div class="card-body">
          <div class="info-item">
            <span class="label">Họ và tên:</span>
            <span class="value">{{ customerInfo?.hoTen || 'Chưa cập nhật' }}</span>
          </div>
          <div class="info-item">
            <span class="label">Email:</span>
            <span class="value">{{ customerInfo?.email || 'Chưa cập nhật' }}</span>
          </div>
          <div class="info-item">
            <span class="label">Số điện thoại:</span>
            <span class="value">{{ customerInfo?.soDienThoai || 'Chưa cập nhật' }}</span>
          </div>
          <div class="info-item">
            <span class="label">Giới tính:</span>
            <span class="value">{{ genderText }}</span>
          </div>
        </div>
        <router-link to="/profile/edit" class="card-action">
          <i class="fas fa-edit"></i>
          Chỉnh sửa thông tin
        </router-link>
      </div>

      <!-- Order Stats Card -->
      <div class="info-card">
        <div class="card-header">
          <i class="fas fa-shopping-bag"></i>
          <h3>Đơn hàng của tôi</h3>
        </div>
        <div class="card-body">
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-number">{{ orderStats.total }}</div>
              <div class="stat-label">Tổng đơn hàng</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ orderStats.processing }}</div>
              <div class="stat-label">Đang xử lý</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ orderStats.completed }}</div>
              <div class="stat-label">Đã hoàn thành</div>
            </div>
          </div>
        </div>
        <router-link to="/profile/orders" class="card-action">
          <i class="fas fa-history"></i>
          Xem lịch sử đơn hàng
        </router-link>
      </div>

      <!-- Address Card -->
      <div class="info-card">
        <div class="card-header">
          <i class="fas fa-map-marker-alt"></i>
          <h3>Địa chỉ giao hàng</h3>
        </div>
        <div class="card-body">
          <div v-if="defaultAddress">
            <p class="address-text">{{ defaultAddress }}</p>
          </div>
          <div v-else class="empty-state">
            <i class="fas fa-map-marker-alt"></i>
            <p>Bạn chưa có địa chỉ giao hàng</p>
          </div>
        </div>
        <router-link to="/profile/addresses" class="card-action">
          <i class="fas fa-plus-circle"></i>
          Quản lý địa chỉ
        </router-link>
      </div>

      <!-- Quick Actions Card -->
      <div class="info-card">
        <div class="card-header">
          <i class="fas fa-bolt"></i>
          <h3>Thao tác nhanh</h3>
        </div>
        <div class="card-body">
          <div class="quick-actions">
            <router-link to="/profile/change-password" class="quick-action-btn">
              <i class="fas fa-lock"></i>
              <span>Đổi mật khẩu</span>
            </router-link>
            <router-link to="/shop" class="quick-action-btn">
              <i class="fas fa-shopping-cart"></i>
              <span>Tiếp tục mua sắm</span>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';

const customerInfo = ref(null);
const orderStats = ref({
  total: 0,
  processing: 0,
  completed: 0
});
const defaultAddress = ref('');

// Load customer info
onMounted(() => {
  try {
    const khachHang = localStorage.getItem('khachHang') || sessionStorage.getItem('khachHang');
    if (khachHang) {
      customerInfo.value = JSON.parse(khachHang);
    }
    
    // TODO: Load order stats from API
    // TODO: Load default address from API
  } catch (error) {
    console.error('Error loading customer info:', error);
  }
});

const genderText = computed(() => {
  if (customerInfo.value?.gioiTinh === true) return 'Nam';
  if (customerInfo.value?.gioiTinh === false) return 'Nữ';
  return 'Khác';
});
</script>

<style scoped>
.profile-overview {
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

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.info-card {
  background: white;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.info-card:hover {
  box-shadow: 0 4px 12px rgba(255, 102, 0, 0.1);
  border-color: #ff6600;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
  color: white;
}

.card-header i {
  font-size: 24px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.card-body {
  padding: 20px;
  min-height: 150px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  color: #666;
  font-size: 14px;
}

.info-item .value {
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #ff6600;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.address-text {
  color: #333;
  font-size: 14px;
  line-height: 1.6;
}

.empty-state {
  text-align: center;
  padding: 20px;
  color: #999;
}

.empty-state i {
  font-size: 32px;
  margin-bottom: 8px;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-action-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f9f9f9;
  border-radius: 8px;
  color: #333;
  text-decoration: none;
  transition: all 0.3s;
}

.quick-action-btn:hover {
  background: #fff3e6;
  color: #ff6600;
}

.quick-action-btn i {
  font-size: 18px;
}

.card-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  background: #f9f9f9;
  color: #ff6600;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s;
}

.card-action:hover {
  background: #ff6600;
  color: white;
}

@media (max-width: 768px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
