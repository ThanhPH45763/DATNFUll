import { createRouter, createWebHistory } from 'vue-router'
import home from './home-router.js';
import admin from './admin-router.js';
// import test from './test-router.js';
// import sell from './sell-router.js';  
import dangNhapDangKy from './dangNhapDangKy.js';
import profileRoutes from './profile-router.js';
import Unauthorized403 from '@/components/admin-components/Error/error.vue';
import { isAuthenticated, getCurrentUser } from './guards.js';

const routes = [...home, ...admin, ...dangNhapDangKy, ...profileRoutes, {
  path: '/unauthorized',
  name: 'unauthorized',
  component: Unauthorized403
}];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // Nếu có savedPosition (khi sử dụng nút back/forward của trình duyệt)
    if (savedPosition) {
      return savedPosition;
    }

    // Mặc định luôn cuộn lên đầu trang khi chuyển route
    return { top: 0 };
  }
})

// Thêm global navigation guard
router.beforeEach((to, from, next) => {
  // Lấy thông tin đăng nhập từ localStorage/sessionStorage
  const isLoggedIn = sessionStorage.getItem('isLoggedIn') === 'true';
  const idRoles = sessionStorage.getItem('id_roles');

  // Kiểm tra customer authentication (dùng guards.js)
  const isCustomerLoggedIn = isAuthenticated();
  const currentUser = getCurrentUser();

  // PHÂN QUYỀN CHÍNH XÁC HƠN
  if (to.path.startsWith('/admin')) {
    if (!isLoggedIn) {
      next('/unauthorized'); // Chưa đăng nhập
      return;
    }

    // Nhân viên (role 3) chỉ được truy cập các trang được cho phép
    if (idRoles === '3') {
      if (to.name === 'admin-ban-hang' ||
        to.name === 'admin-quan-ly-hoa-don' ||
        to.name === 'profile' ||
        to.name === 'admin-quan-ly-san-pham' ||
        to.name === 'admin-hoa-don-chi-tiet' ||
        to.name === 'traHang') { // Thêm đường dẫn profile
        next(); // Cho phép truy cập
      } else if (to.path === '/admin') {
        next('/admin/banhang'); // Chuyển hướng về trang bán hàng
      } else {
        next('/unauthorized'); // Không có quyền truy cập
      }
    }
    // Admin và Quản lý (role 1, 2) được truy cập tất cả trang admin
    else if (idRoles === '1' || idRoles === '2') {
      next();
    }
    // Các role khác không được truy cập trang admin
    else {
      next('/unauthorized');
    }
  }
  // Check customer-only protected routes
  else if (to.meta.requiresAuth) {
    if (!isCustomerLoggedIn) {
      next({
        path: '/login-register/login',
        query: { redirect: to.fullPath }
      });
    } else {
      next();
    }
  }
  // Guest routes (login/register) - redirect if already logged in
  else if (to.meta.requiresGuest) {
    console.log('=== Guest Guard Debug ===');
    console.log('isCustomerLoggedIn:', isCustomerLoggedIn);
    console.log('isLoggedIn (admin):', isLoggedIn);
    console.log('Trying to access:', to.path);

    if (isCustomerLoggedIn || isLoggedIn) {
      console.log('→ Redirecting to /home (detected login)');
      next('/home');
    } else {
      console.log('→ Allowing access (no login detected)');
      next();
    }
  }
  // Các trang không phải admin
  else {
    next();
  }
});

// ✅ NEW: Router guard for real-time data refresh
router.afterEach((to, from) => {
  // Refresh data khi chuyển trang bán hàng
  if (to.path.startsWith('/admin/banhang') && 
      to.path !== from.path) {
    // Trigger refresh sau 100ms để đảm bảo component mounted
    setTimeout(() => {
      window.dispatchEvent(new CustomEvent('refresh-banhang-data'));
    }, 100);
  }
});

export default router
