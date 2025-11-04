# 🔧 CHI TIẾT SỬA LỖI FRONTEND

## 📊 TỔNG QUAN
Tìm thấy **30+ dòng code** trong **15 file** cần sửa liên quan đến:
- `id_roles` 
- `taiKhoan`
- `roles`

---

## ✅ **ĐÃ SỬA**

### 1. **viewDangNhap.vue** ✅
**File**: `src/views/DangNhapDangKy/viewDangNhap.vue`
**Dòng 238**: Đã sửa kiểm tra `id_roles` → kiểm tra `khachHang`

---

## ⚠️ **CẦN SỬA NGAY**

### 2. **viewDNAdmin.vue** - ĐĂNG NHẬP ADMIN
**File**: `src/views/DangNhapDangKy/viewDNAdmin.vue`

**Các dòng cần sửa**:
- Dòng 226: `sessionStorage.setItem('id_roles', '1');`
- Dòng 250: `if (result.id_roles === 4)`
- Dòng 256: `console.log('...', result.id_roles);`
- Dòng 259: `if (result.id_roles === 3)`

**Logic mới cho Admin**:
```javascript
// Backend login admin sẽ trả về:
{
  "successMessage": "Đăng nhập thành công!",
  "admin": {
    "idNhanVien": 1,
    "tenNhanVien": "Admin",
    "email": "admin@gbsports.com"
  }
}

// Không còn id_roles
// Phân biệt bằng endpoint: /api/admin/login vs /api/khach-hang/login
```

---

### 3. **gbStore.js** - STORE QUẢN LÝ STATE
**File**: `src/stores/gbStore.js`

**Cần sửa**:
- Dòng 99: `id_roles: localStorage.getItem('id_roles') || null`
- Dòng 1424: `if (!result.taiKhoan || !result.taiKhoan.ten_dang_nhap)`

**Sửa thành**:
```javascript
// Loại bỏ id_roles khỏi store
state: {
    // Xóa: id_roles
    currentUser: null, // Lưu thông tin user hiện tại
    isAdmin: false, // Phân biệt admin/khachhang
}

// Dòng 1424 sửa:
if (!result.tenDangNhap) {
    throw new Error('Thiếu thông tin tài khoản');
}
```

---

### 4. **router/index.js** - ROUTING
**File**: `src/router/index.js`

**Dòng 33**: `const idRoles = sessionStorage.getItem('id_roles');`

**Sửa logic kiểm tra auth**:
```javascript
// Thay vì kiểm tra id_roles
const isAuthenticated = () => {
    const khachHang = localStorage.getItem('khachHang');
    const admin = localStorage.getItem('admin');
    return khachHang || admin;
};

// Route guard
router.beforeEach((to, from, next) => {
    if (to.meta.requiresAuth && !isAuthenticated()) {
        next('/login');
    } else {
        next();
    }
});
```

---

### 5. **axiosConfig.js** - HTTP INTERCEPTOR
**File**: `src/config/axiosConfig.js`

**Dòng 32, 37**: `localStorage/sessionStorage.removeItem('id_roles')`

**Sửa thành**:
```javascript
// Khi logout hoặc lỗi 401
localStorage.removeItem('khachHang');
localStorage.removeItem('admin');
sessionStorage.clear();
```

---

### 6. **khachHang.vue** - TRANG KHÁCH HÀNG
**File**: `src/components/web-components/KhachHang/khachHang.vue`

**Dòng 1859, 1861**: Kiểm tra `id_roles`

**Sửa thành**:
```javascript
// Kiểm tra xem user có phải khách hàng không
const khachHang = JSON.parse(localStorage.getItem('khachHang'));
if (!khachHang) {
    router.push('/login');
}
```

---

## 🚫 **CÓ THỂ BỎ QUA (KHÔNG LIÊN QUAN KHÁCH HÀNG)**

### 7-15. **Các file Admin/NhanVien**
Các file sau liên quan đến ADMIN/NHÂN VIÊN, KHÔNG ẢNH HƯỞNG đến khách hàng:

- `TheFraming-Admin.vue` (Menu admin)
- `TheAvatar-Admin.vue` (Avatar admin)
- `tableSanPham.vue` (Quản lý sản phẩm)
- `menuAction.vue` (Menu actions admin)
- `tableKhachHang.vue` (Admin xem danh sách KH)
- `suaNhanVien.vue` (Sửa nhân viên)
- `Profile/NhanVien.vue` (Profile nhân viên)

**Lý do**: Đây là phần admin/nhân viên, có thể giữ nguyên logic cũ hoặc sửa sau.

---

## 🎯 **ƯU TIÊN SỬA**

### **MỨC ĐỘ CAO** (Ảnh hưởng trực tiếp đến khách hàng):
1. ✅ viewDangNhap.vue (ĐÃ SỬA)
2. ⚠️ gbStore.js
3. ⚠️ router/index.js
4. ⚠️ axiosConfig.js
5. ⚠️ khachHang.vue

### **MỨC ĐỘ TRUNG BÌNH**:
6. ⚠️ viewDNAdmin.vue (nếu dùng trang này)

### **MỨC ĐỘ THẤP** (Admin/Nhân viên):
7-15. Các file admin (có thể sửa sau)

---

## 📝 **CODE MẪU ĐỂ SỬA**

### **gbStore.js - Loại bỏ id_roles**
```javascript
// src/stores/gbStore.js
export const useGbStore = defineStore('gbStore', {
    state: () => ({
        // XÓA: id_roles
        currentUser: null,
        isAdmin: false,
        // ... các state khác
    }),
    
    actions: {
        login(userData, isAdminLogin = false) {
            this.currentUser = userData;
            this.isAdmin = isAdminLogin;
            
            if (isAdminLogin) {
                localStorage.setItem('admin', JSON.stringify(userData));
            } else {
                localStorage.setItem('khachHang', JSON.stringify(userData));
            }
        },
        
        logout() {
            this.currentUser = null;
            this.isAdmin = false;
            localStorage.removeItem('khachHang');
            localStorage.removeItem('admin');
            sessionStorage.clear();
        }
    }
});
```

### **router/index.js - Guard mới**
```javascript
// src/router/index.js
router.beforeEach((to, from, next) => {
    const khachHang = localStorage.getItem('khachHang');
    const admin = localStorage.getItem('admin');
    
    // Route cần đăng nhập
    if (to.meta.requiresAuth) {
        if (to.meta.requiresAdmin && !admin) {
            next('/admin/login');
        } else if (!khachHang && !admin) {
            next('/login');
        } else {
            next();
        }
    } else {
        next();
    }
});
```

### **axiosConfig.js - Response interceptor**
```javascript
// src/config/axiosConfig.js
axios.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // Xóa tất cả thông tin đăng nhập
            localStorage.removeItem('khachHang');
            localStorage.removeItem('admin');
            sessionStorage.clear();
            
            // Redirect về login
            router.push('/login');
        }
        return Promise.reject(error);
    }
);
```

---

## 🚀 **HÀNH ĐỘNG TIẾP THEO**

1. Sửa **gbStore.js** - Loại bỏ `id_roles`
2. Sửa **router/index.js** - Guard mới
3. Sửa **axiosConfig.js** - Interceptor
4. Sửa **khachHang.vue** - Kiểm tra auth
5. Test đầy đủ flow:
   - Đăng ký
   - Đăng nhập
   - Xem profile
   - Đặt hàng
   - Logout

---

## ✅ CHECKLIST

- [x] viewDangNhap.vue
- [ ] gbStore.js
- [ ] router/index.js
- [ ] axiosConfig.js
- [ ] khachHang.vue
- [ ] viewDNAdmin.vue (nếu cần)

---

