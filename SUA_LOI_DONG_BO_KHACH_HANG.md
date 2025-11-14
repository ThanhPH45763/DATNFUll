# 🔧 SỬA LỖI ĐỒNG BỘ THÔNG TIN KHÁCH HÀNG SAU KHI TẠO MỚI

## 📌 Hiện Tượng

**Lỗi:** Sau khi nhập đầy đủ thông tin khách hàng mới từ form `formKhachHangBH.vue` và nhấn thanh toán, hệ thống vẫn báo:

```
"Vui lòng nhập đầy đủ thông tin giao hàng (Tên, SĐT, Địa chỉ) hoặc chọn khách hàng."
```

**Log cho thấy:**
```
Lưu thông tin khách hàng: 2 null 
Array [ "số 1, Xã Thanh Xương, Huyện Điện Biên, Tỉnh Điện Biên" ]
nghia 0998789876 nghia@gmail.com
```

→ Thông tin **ĐÃ ĐƯỢC LƯU** vào database, nhưng **CHƯA ĐỒNG BỘ** vào `activeTabData.value.hd`!

---

## 🐛 NGUYÊN NHÂN

### Flow hiện tại (SAI):

1. User nhập thông tin khách hàng trong `formKhachHangBH.vue`
2. Nhấn "Lưu" → Gọi API `addKHHD()` → ✅ Lưu vào DB thành công
3. Lưu `localStorage.setItem('luuTTKHBH', true)` → ✅ OK
4. Component cha `TheHeader-BanHang.vue` đọc localStorage → ✅ Phát hiện có thay đổi
5. **NHƯNG:** Chỉ gọi `refreshHoaDon()` mà **KHÔNG CẬP NHẬT** thông tin vào `activeTabData.value.hd` ❌
6. User nhấn thanh toán → Validate kiểm tra `activeTabData.value.hd.ten_khach_hang` → **RỖNG!** ❌

---

## 🔍 PHÂN TÍCH CHI TIẾT

### 1. File: `formKhachHangBH.vue` (Line 375-377)

**❌ Code CŨ:**

```javascript
await gbStore.addKHHD(idHoaDon, null, diaChiList, formData.tenKhachHang, formData.soDienThoai, formData.email);

localStorage.setItem('luuTTKHBH', JSON.stringify(true)); // ← Chỉ lưu boolean!
```

**Vấn đề:** 
- Chỉ lưu `true` vào localStorage
- Không lưu thông tin khách hàng (tên, SĐT, địa chỉ)
- Component cha không biết data nào để cập nhật!

---

### 2. File: `TheHeader-BanHang.vue` (Line 1386-1401)

**❌ Code CŨ:**

```javascript
const checkAndApplyLocalData = async () => {
    const checkluuTTKHBH = JSON.parse(localStorage.getItem('luuTTKHBH'));
    if (checkluuTTKHBH === true) {  // ← Chỉ kiểm tra boolean
        isLoading.value = true;
        try {
            const idHoaDon = activeTabData.value.hd.id_hoa_don;
            await refreshHoaDon(idHoaDon); // ← Chỉ refresh, KHÔNG cập nhật thông tin KH!
        } finally {
            localStorage.removeItem('luuTTKHBH');
            isLoading.value = false;
        }
    }
};
```

**Vấn đề:**
- Chỉ `refreshHoaDon()` → Lấy lại dữ liệu từ backend
- **KHÔNG** cập nhật `ten_khach_hang`, `so_dien_thoai`, `dia_chi` vào `activeTabData.value.hd`
- Validate vẫn thấy các field này RỖNG!

---

## ✅ GIẢI PHÁP

### 1. Sửa `formKhachHangBH.vue` - Lưu đầy đủ thông tin

**✅ Code MỚI:**

```javascript
await gbStore.addKHHD(idHoaDon, null, diaChiList, formData.tenKhachHang, formData.soDienThoai, formData.email);

// ✅ Lưu OBJECT chứa đầy đủ thông tin
localStorage.setItem('luuTTKHBH', JSON.stringify({
    saved: true,
    ten_khach_hang: formData.tenKhachHang,
    so_dien_thoai: formData.soDienThoai,
    dia_chi: diaChiList[0], // Lấy địa chỉ đầu tiên
    email: formData.email
}));
```

**Thay đổi:**
- ✅ Lưu object thay vì boolean
- ✅ Bao gồm: `ten_khach_hang`, `so_dien_thoai`, `dia_chi`, `email`

---

### 2. Sửa `TheHeader-BanHang.vue` - Đọc và cập nhật thông tin

**✅ Code MỚI:**

```javascript
const checkAndApplyLocalData = async () => {
    const customerData = JSON.parse(localStorage.getItem('luuTTKHBH'));
    if (customerData && customerData.saved) {  // ← Kiểm tra object
        console.log('📥 Đọc thông tin khách hàng từ localStorage:', customerData);
        
        isLoading.value = true;
        try {
            const idHoaDon = activeTabData.value.hd.id_hoa_don;
            
            // ✅ CẬP NHẬT thông tin khách hàng vào hóa đơn
            Object.assign(activeTabData.value.hd, {
                ten_khach_hang: customerData.ten_khach_hang,
                so_dien_thoai: customerData.so_dien_thoai,
                dia_chi: customerData.dia_chi,
                email: customerData.email
            });
            
            console.log('✅ Đã cập nhật thông tin vào hóa đơn');
            
            await refreshHoaDon(idHoaDon);
        } finally {
            localStorage.removeItem('luuTTKHBH');
            isLoading.value = false;
        }
    }
};
```

**Thay đổi:**
- ✅ Đọc object từ localStorage
- ✅ Dùng `Object.assign()` để cập nhật `activeTabData.value.hd`
- ✅ Cập nhật **TRƯỚC KHI** gọi `refreshHoaDon()`
- ✅ Thêm console.log để debug

---

## 📊 So Sánh Flow Trước/Sau

### ❌ Flow CŨ (SAI):

```
1. User nhập form → Lưu DB ✅
2. localStorage = true
3. Component cha đọc → refreshHoaDon() ✅
4. activeTabData.value.hd.ten_khach_hang = undefined ❌
5. Validate FAIL ❌
```

### ✅ Flow MỚI (ĐÚNG):

```
1. User nhập form → Lưu DB ✅
2. localStorage = {ten_khach_hang, so_dien_thoai, dia_chi} ✅
3. Component cha đọc object ✅
4. Object.assign() → Cập nhật activeTabData.value.hd ✅
5. activeTabData.value.hd.ten_khach_hang = "nghia" ✅
6. Validate PASS ✅
7. Thanh toán thành công ✅
```

---

## 🔧 CÁC BƯỚC TRIỂN KHAI

### Bước 1: Code đã được sửa

✅ File `formKhachHangBH.vue` (Line 375-384):
- Thay đổi localStorage từ `true` → Object chứa thông tin KH

✅ File `TheHeader-BanHang.vue` (Line 1386-1414):
- Đọc object từ localStorage
- Dùng `Object.assign()` để cập nhật thông tin

### Bước 2: Không cần rebuild backend

Chỉ cần **reload trang Frontend** (Ctrl + F5)

### Bước 3: Test Lại

**Kịch bản test:**

1. Vào trang Bán hàng tại quầy
2. Thêm sản phẩm vào giỏ
3. Chọn "Giao hàng"
4. **KHÔNG** chọn khách hàng có sẵn
5. Nhấn nút "Thêm khách hàng mới" (hoặc mở form nhập)
6. Nhập thông tin:
   - Tên: "Nguyễn Văn A"
   - SĐT: "0123456789"
   - Email: "test@gmail.com"
   - Địa chỉ: "Số 1, Xã ABC, Huyện XYZ, Tỉnh DEF"
7. Nhấn "Lưu"
8. **Quan sát console log:**
   ```
   📥 Đọc thông tin khách hàng từ localStorage: {ten_khach_hang: "Nguyễn Văn A", ...}
   ✅ Đã cập nhật thông tin vào hóa đơn
   ```
9. Nhập phí vận chuyển
10. Nhấn "Thanh toán"

**Kết quả mong đợi:**
- ✅ Console log hiển thị thông tin đã cập nhật
- ✅ KHÔNG hiển thị lỗi "Vui lòng nhập đầy đủ thông tin"
- ✅ Cho phép thanh toán thành công

---

## 🔍 Debug Tips

### Nếu vẫn bị lỗi:

**1. Kiểm tra localStorage:**

Mở Chrome DevTools → Application → Local Storage → http://localhost:5173

Xem giá trị của `luuTTKHBH`:
```json
{
    "saved": true,
    "ten_khach_hang": "nghia",
    "so_dien_thoai": "0998789876",
    "dia_chi": "số 1, Xã Thanh Xương, Huyện Điện Biên, Tỉnh Điện Biên",
    "email": "nghia@gmail.com"
}
```

**2. Kiểm tra console log:**

Sau khi lưu form, phải thấy log:
```
📥 Đọc thông tin khách hàng từ localStorage: {...}
✅ Đã cập nhật thông tin vào hóa đơn
```

**3. Kiểm tra activeTabData:**

Mở Vue DevTools → Components → TheHeaderBanHang → activeTabData.value.hd

Xem các field:
- `ten_khach_hang`: "nghia" ✅
- `so_dien_thoai`: "0998789876" ✅
- `dia_chi`: "số 1, Xã Thanh Xương..." ✅

**4. Nếu vẫn undefined:**

Thêm log trước validate:
```javascript
console.log('🔍 Check trước validate:', {
    ten_khach_hang: currentTab.hd.ten_khach_hang,
    so_dien_thoai: currentTab.hd.so_dien_thoai,
    dia_chi: currentTab.hd.dia_chi
});
```

---

## 📝 Lưu Ý Quan Trọng

### Tại sao dùng localStorage thay vì emit event?

**Ưu điểm:**
- ✅ Component form và component cha độc lập
- ✅ Không cần truyền props/emit qua lại
- ✅ Dữ liệu persist ngay cả khi reload trang

**Nhược điểm:**
- ⚠️ Phải polling (kiểm tra mỗi 3s)
- ⚠️ Có thể bị delay

### Best Practice:

**Nếu muốn cải thiện, có thể dùng:**

1. **Event Bus:**
```javascript
// Trong formKhachHangBH.vue
import { eventBus } from '@/utils/eventBus';
eventBus.emit('customer-saved', customerData);

// Trong TheHeader-BanHang.vue
eventBus.on('customer-saved', (data) => {
    Object.assign(activeTabData.value.hd, data);
});
```

2. **Vuex/Pinia Store:**
```javascript
// Store action
setCustomerInfo(customerData) {
    this.currentCustomer = customerData;
}

// Component cha watch
watch(() => store.currentCustomer, (newData) => {
    if (newData) {
        Object.assign(activeTabData.value.hd, newData);
    }
});
```

---

## ✅ Checklist

- [x] Sửa `formKhachHangBH.vue` - Lưu object thay vì boolean
- [x] Sửa `TheHeader-BanHang.vue` - Đọc và cập nhật thông tin
- [x] Thêm console.log để debug
- [ ] Reload trang Frontend
- [ ] Test tạo khách hàng mới
- [ ] Kiểm tra console log
- [ ] Kiểm tra localStorage
- [ ] Test thanh toán thành công

---

## 📅 Thông Tin

**Ngày sửa:** 2025-11-13

**Lỗi:** Thông tin khách hàng không đồng bộ sau khi tạo mới

**Nguyên nhân:** 
1. localStorage chỉ lưu boolean → Không có data để cập nhật
2. Component cha chỉ refreshHoaDon() → Không cập nhật activeTabData

**Giải pháp:**
1. Lưu object đầy đủ vào localStorage
2. Dùng Object.assign() để cập nhật activeTabData.value.hd

**Mức độ:** 🔴 CRITICAL - Không thể thanh toán đơn giao hàng với KH mới

**Trạng thái:** ✅ ĐÃ SỬA

---

## 🎉 Kết Luận

**Vấn đề:** Data được lưu vào DB nhưng không sync vào Vue reactive state

**Giải pháp:** Lưu data vào localStorage → Component cha đọc và cập nhật state

**Test:** Reload trang và thử tạo khách hàng mới → Thanh toán

---

**🎯 RELOAD TRANG VÀ TEST LẠI! Lần này thông tin sẽ được đồng bộ! 🚀**
