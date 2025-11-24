<template>
  <div class="profile-layout">
    <!-- Header/Breadcrumb -->
    <div class="profile-breadcrumb">
      <router-link to="/home" class="breadcrumb-link">Trang chủ</router-link>
      <span class="separator">/</span>
      <span class="current">Tài khoản của tôi</span>
    </div>

    <div class="profile-container">
      <!-- Sidebar Menu -->
      <aside class="profile-sidebar">
        <div class="profile-user-info">
          <div class="user-avatar">
            <i class="fas fa-user-circle"></i>
          </div>
          <div class="user-details">
            <h4>{{ customerName }}</h4>
            <p>{{ customerEmail }}</p>
          </div>
        </div>

        <nav class="profile-menu">
          <router-link 
            to="/profile" 
            exact
            class="menu-item"
            active-class="active">
            <i class="fas fa-tachometer-alt"></i>
            <span>Tổng quan</span>
          </router-link>

          <router-link 
            to="/profile/edit" 
            class="menu-item"
            active-class="active">
            <i class="fas fa-user-edit"></i>
            <span>Thông tin cá nhân</span>
          </router-link>

          <router-link 
            to="/profile/addresses" 
            class="menu-item"
            active-class="active">
            <i class="fas fa-map-marker-alt"></i>
            <span>Địa chỉ của tôi</span>
          </router-link>

          <router-link 
            to="/profile/orders" 
            class="menu-item"
            active-class="active">
            <i class="fas fa-shopping-bag"></i>
            <span>Đơn hàng của tôi</span>
          </router-link>

          <router-link 
            to="/profile/change-password" 
            class="menu-item"
            active-class="active">
            <i class="fas fa-lock"></i>
            <span>Đổi mật khẩu</span>
          </router-link>

          <a @click="handleLogout" class="menu-item logout-item">
            <i class="fas fa-sign-out-alt"></i>
            <span>Đăng xuất</span>
          </a>
        </nav>
      </aside>

      <!-- Main Content Area -->
      <main class="profile-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { Modal } from 'ant-design-vue';
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
import { h } from 'vue';

const router = useRouter();

// Get customer info from store or localStorage
const customerInfo = computed(() => {
  try {
    const khachHang = localStorage.getItem('khachHang') || sessionStorage.getItem('khachHang');
    return khachHang ? JSON.parse(khachHang) : null;
  } catch {
    return null;
  }
});

const customerName = computed(() => customerInfo.value?.hoTen || 'Khách hàng');
const customerEmail = computed(() => customerInfo.value?.email || '');

const handleLogout = () => {
  Modal.confirm({
    title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
      h(ExclamationCircleOutlined, { style: 'color: #faad14; font-size: 22px;' }),
      h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Xác nhận đăng xuất')
    ]),
    content: 'Bạn có chắc chắn muốn đăng xuất khỏi tài khoản không?',
    okText: 'Đăng xuất',
    cancelText: 'Hủy',
    okButtonProps: { 
      danger: true,
      size: 'large',
      style: { backgroundColor: '#ff6600', borderColor: '#ff6600' }
    },
    cancelButtonProps: { size: 'large' },
    centered: true,
    onOk: () => {
      // Clear all auth data
      localStorage.removeItem('khachHang');
      localStorage.removeItem('userInfo');
      sessionStorage.removeItem('khachHang');
      sessionStorage.removeItem('userInfo');
      
      // Redirect to home
      router.push('/home');
    }
  });
};
</script>

<style scoped>
.profile-layout {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px 0;
}

.profile-breadcrumb {
  max-width: 1200px;
  margin: 0 auto 20px;
  padding: 0 20px;
  font-size: 14px;
}

.breadcrumb-link {
  color: #666;
  text-decoration: none;
  transition: color 0.3s;
}

.breadcrumb-link:hover {
  color: #ff6600;
}

.separator {
  margin: 0 8px;
  color: #999;
}

.current {
  color: #333;
  font-weight: 500;
}

.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 24px;
}

/* Sidebar */
.profile-sidebar {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  height: fit-content;
  position: sticky;
  top: 20px;
}

.profile-user-info {
  text-align: center;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 24px;
}

.user-avatar {
  margin-bottom: 12px;
}

.user-avatar i {
  font-size: 64px;
  color: #ff6600;
}

.user-details h4 {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.user-details p {
  margin: 0;
  font-size: 13px;
  color: #666;
  word-break: break-word;
}

.profile-menu {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  color: #666;
  text-decoration: none;
  transition: all 0.3s;
  cursor: pointer;
}

.menu-item i {
  font-size: 18px;
  width: 20px;
  text-align: center;
}

.menu-item span {
  font-size: 14px;
  font-weight: 500;
}

.menu-item:hover {
  background-color: #fff3e6;
  color: #ff6600;
}

.menu-item.active {
  background-color: #ff6600;
  color: white;
}

.menu-item.logout-item {
  margin-top: 12px;
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
  color: #ff4d4f;
}

.menu-item.logout-item:hover {
  background-color: #fff1f0;
  color: #ff4d4f;
}

/* Main Content */
.profile-content {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  min-height: 500px;
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .profile-container {
    grid-template-columns: 1fr;
  }

  .profile-sidebar {
    position: static;
  }

  .profile-content {
    padding: 20px;
  }
}
</style>
