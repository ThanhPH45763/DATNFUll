# 🔴 LỖI NGHIÊM TRỌNG: TRANG_THAI LÀ BIT (0/1) KHÔNG PHẢI STRING

## ❌ VẤN ĐỀ

### Database Schema:
```sql
trang_thai bit DEFAULT 1 NULL
```
→ Giá trị: `0` hoặc `1` (hoặc `true`/`false` khi parse sang JSON)

### Frontend đang làm SAI:
```javascript
if (variant.trang_thai === 'Hoạt động')  // ❌ LUÔN FALSE!
```

→ So sánh `1` với `'Hoạt động'` → KHÔNG BAO GIỜ ĐÚNG!

---

## 🔍 CÁC CHỖ CẦN SỬA

### 1. **Dòng 697** - Khởi tạo size
```javascript
// TRƯỚC (SAI):
co_san: variant.trang_thai === 'Hoạt động',

// SAU (ĐÚNG):
co_san: variant.trang_thai === 1 || variant.trang_thai === true,
```

### 2. **Dòng 992** - Check variant availability
```javascript
// TRƯỚC:
matchedVariant.trang_thai === 'Hoạt động' && 

// SAU:
(matchedVariant.trang_thai === 1 || matchedVariant.trang_thai === true) &&
```

### 3. **Dòng 1493** - Set trang_thai
```javascript
// TRƯỚC:
trang_thai: matchedVariant.trang_thai || 'Hoạt động'

// SAU:
trang_thai: matchedVariant.trang_thai
```

### 4. **Dòng 1597** - Check if not active
```javascript
// TRƯỚC:
if (matchedVariant.trang_thai !== 'Hoạt động') {

// SAU:
if (!matchedVariant.trang_thai && matchedVariant.trang_thai !== 1) {
```

### 5. **Dòng 1838** - Check refreshed variant
```javascript
// TRƯỚC:
if (refreshedVariant.trang_thai !== 'Hoạt động' || refreshedVariant.so_luong <= 0) {

// SAU:
if ((!refreshedVariant.trang_thai && refreshedVariant.trang_thai !== 1) || refreshedVariant.so_luong <= 0) {
```

### 6. **Dòng 2465** - availableSizes computed
```javascript
// TRƯỚC:
if (variant.trang_thai === 'Hoạt động' && variant.so_luong > 0) {

// SAU:
if ((variant.trang_thai === 1 || variant.trang_thai === true) && variant.so_luong > 0) {
```

### 7. **Dòng 2480** - Set trang_thai in availableSizes
```javascript
// TRƯỚC:
trang_thai: sizeData.hasAvailable ? 'Hoạt động' : 'Không hoạt động'

// SAU:
trang_thai: sizeData.hasAvailable ? 1 : 0
```

---

## ✅ GIẢI PHÁP TỐI ƯU

### Tạo helper function để kiểm tra:

```javascript
// Thêm ở đầu script section
const isTrangThaiActive = (trangThai) => {
    return trangThai === 1 || trangThai === true || trangThai === '1';
};
```

### Sau đó dùng ở mọi nơi:
```javascript
// Thay vì:
if (variant.trang_thai === 'Hoạt động')

// Dùng:
if (isTrangThaiActive(variant.trang_thai))
```

---

## 🎯 CÁCH FIX NHANH

Tìm và thay thế tất cả:

1. `variant.trang_thai === 'Hoạt động'` → `(variant.trang_thai === 1 || variant.trang_thai === true)`
2. `variant.trang_thai !== 'Hoạt động'` → `(!variant.trang_thai && variant.trang_thai !== 1)`
3. `'Hoạt động'` trong context trang_thai → `1`
4. `'Không hoạt động'` trong context trang_thai → `0`

---

## 📊 MAPPING ĐÚNG

| Database | JSON Response | JavaScript Check |
|----------|---------------|------------------|
| `1` (bit) | `true` hoặc `1` | `trangThai === 1 \|\| trangThai === true` |
| `0` (bit) | `false` hoặc `0` | `!trangThai \|\| trangThai === 0` |

---

## ⚠️ LƯU Ý

- Nếu backend trả về `trang_thai` dạng số: dùng `=== 1` hoặc `=== 0`
- Nếu backend trả về `trang_thai` dạng boolean: dùng `=== true` hoặc `=== false`
- An toàn nhất: kiểm tra cả 2 → `trangThai === 1 || trangThai === true`

---

## 🔧 TEST

Sau khi sửa, mở console browser và kiểm tra:
```javascript
console.log('productDetails:', productDetails.value);
console.log('First variant trang_thai:', productDetails.value[0].trang_thai);
console.log('Type:', typeof productDetails.value[0].trang_thai);
```

Sẽ thấy: `1` hoặc `true`, **KHÔNG PHẢI** `"Hoạt động"`!
