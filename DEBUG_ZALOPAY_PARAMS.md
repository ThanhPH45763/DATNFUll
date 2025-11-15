# TEST ZALOPAY API - DEBUG

## Vấn đề: Axios đang serialize toàn bộ store object thay vì chỉ idHoaDon

## Fix: Thêm validation trong gbStore.js

```javascript
// Trước:
async checkZaloPayStatus(idHoaDon) {
  const response = await axiosInstance.get('api/zalopay/check-status', {
    params: { idHoaDon }
  });
}

// Sau:
async checkZaloPayStatus(idHoaDon) {
  // Ensure idHoaDon is a number
  const invoiceId = typeof idHoaDon === 'object' ? idHoaDon.id_hoa_don : idHoaDon;
  
  const response = await axiosInstance.get('api/zalopay/check-status', {
    params: { idHoaDon: invoiceId }
  });
}
```

## Test Lại:

### 1. Restart Frontend
```bash
# Stop frontend (Ctrl+C)
cd /home/huunghia/DATNFUll/DuAnMauFE
npm run dev
```

### 2. Test trong Console
```javascript
// Mở F12 Console
const store = window.$pinia.state.value.gbStore;
console.log('Store:', store);
console.log('ID:', store.currentHoaDonId);

// Test gọi hàm
store.checkZaloPayStatus(5);
```

### 3. Xem Network Tab
- F12 → Network
- Trigger payment
- Xem request URL: Phải là `/api/zalopay/check-status?idHoaDon=5`
- KHÔNG PHẢI: `/api/zalopay/check-status?idHoaDon[$id]=gbStore&...`

## Nếu vẫn lỗi - Debug thêm:

### Option 1: Log chi tiết
Thêm vào đầu hàm trong gbStore.js:
```javascript
async checkZaloPayStatus(idHoaDon) {
  console.log('[DEBUG] Input:', typeof idHoaDon, idHoaDon);
  const invoiceId = typeof idHoaDon === 'object' ? idHoaDon.id_hoa_don : idHoaDon;
  console.log('[DEBUG] Cleaned:', typeof invoiceId, invoiceId);
  // ...
}
```

### Option 2: Force type conversion
```javascript
const invoiceId = Number(
  typeof idHoaDon === 'object' ? idHoaDon.id_hoa_don : idHoaDon
);
```

### Option 3: Kiểm tra nơi gọi
Trong TheHeader-BanHang.vue:
```javascript
console.log('[CALL] ID:', activeTabData.value.hd.id_hoa_don);
const result = await store.checkZaloPayStatus(activeTabData.value.hd.id_hoa_don);
```

## Expected Result:
- URL sạch: `?idHoaDon=5`
- No object serialization
- Status 200 từ backend

---
**Updated**: 16/11/2024 03:59
**Status**: 🔧 Testing required after restart
