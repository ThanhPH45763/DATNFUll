import { createRouter, createWebHistory } from 'vue-router'
import home from './home-router.js';
import admin from './admin-router.js';
// import test from './test-router.js';
// import sell from './sell-router.js';  
import dangNhapDangKy from './dangNhapDangKy.js';
import Unauthorized403 from '@/components/admin-components/Error/error.vue';

const routes = [...home, ...admin, ...dangNhapDangKy, {
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
  // Lấy thông tin đăng nhập từ localStorage
  const isLoggedIn = sessionStorage.getItem('isLoggedIn') === 'true';
  const idRoles = sessionStorage.getItem('id_roles');
  
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
  // Các trang không phải admin
  else {
    next();
  }
});

export default router
