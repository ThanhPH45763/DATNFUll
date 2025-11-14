# 🔍 HƯỚNG DẪN DEBUG LỖI DUPLICATE ITEMS

## 📌 Hiện Tượng

- Khi click thêm 1 sản phẩm vào giỏ hàng
- Database có 1 record với `so_luong = 2` ✅ (ĐÚNG)
- Nhưng UI hiển thị **2 dòng giống nhau**, mỗi dòng `so_luong = 2` ❌ (SAI)

## 🔍 Giả Thuyết

**Frontend đang map dữ liệu 2 lần hoặc có duplicate trong array!**

Có thể do:
1. `store.getAllSPHDArr` bị duplicate
2. `currentTab.items.value` bị append thay vì replace
3. Watch `activeKey` trigger lại khi thêm sản phẩm
4. Có 2 lời gọi `getAllSPHD()` đồng thời

## 🛠️ Đã Thêm Debug Logs

### File: TheHeader-BanHang.vue

**Đã thêm console.log ở 2 chỗ:**

1. **Hàm `addToBill()` (line 657-756)**
   - Log khi bắt đầu thêm SP
   - Log khi gọi API themSPHDMoi
   - Log khi gọi API getAllSPHD
   - Log số lượng items từ server
   - Log chi tiết từng item
   - Log sau khi map vào currentTab.items.value

2. **Watch `activeKey` (line 1319-1343)**
   - Log khi watch trigger
   - Log khi gọi API getAllSPHD
   - Log số lượng items từ server
   - Log sau khi map

## 📝 Cách Debug

### Bước 1: Rebuild Frontend

```bash
cd /home/huunghia/DATNFUll/DuAnMauFE
npm run dev
```

### Bước 2: Mở Browser DevTools

1. Mở Chrome DevTools (F12)
2. Chuyển sang tab **Console**
3. Clear console (Ctrl + L)

### Bước 3: Test Thêm Sản Phẩm

1. Vào trang Bán hàng tại quầy
2. Chọn/Tạo hóa đơn
3. Tìm và **click chọn 1 sản phẩm**
4. **Quan sát console log**

### Bước 4: Phân Tích Log

**Tìm các log sau (theo thứ tự):**

```
🛒 BẮT ĐẦU thêm sản phẩm: Áo thun ID: 1
📡 GỌI API themSPHDMoi...
✅ API themSPHDMoi thành công
📡 GỌI API getAllSPHD để refresh...
📦 Dữ liệu từ server: 1 items
📦 Chi tiết: [{"id":1,"name":"Áo thun","qty":2}]
🎨 Mapped items: 1 items
🎨 Chi tiết items: [{"id":1,"name":"Áo thun","qty":2}]
✅ HOÀN TẤT thêm sản phẩm
```

**Kiểm tra:**

✅ **Nếu log hiển thị "1 items"** → Backend trả về đúng, lỗi nằm ở rendering

❌ **Nếu log hiển thị "2 items"** → Backend trả về duplicate, hoặc store bị cache sai

### Bước 5: Kiểm Tra Watch Trigger

**Tìm log của watch:**

```
👁️ WATCH activeKey triggered, newKey: invoiceTab_xxx
📡 WATCH: GỌI API getAllSPHD cho hóa đơn: 1
📦 WATCH: Dữ liệu từ server: 2 items  ← ⚠️ Nếu thấy log này NGAY SAU addToBill
🎨 WATCH: Mapped items: 2 items        ← ⚠️ Thì watch đang trigger lại!
```

**❓ Câu hỏi quan trọng:**

1. **Watch có trigger NGAY SAU khi addToBill() xong không?**
   - Nếu CÓ → Watch đang ghi đè lên dữ liệu mới
   - Nếu KHÔNG → Vấn đề nằm ở chỗ khác

2. **"📦 Dữ liệu từ server: X items" - X là bao nhiêu?**
   - Nếu X = 1 → Backend đúng
   - Nếu X = 2 → Backend trả về duplicate

3. **"🎨 Mapped items: Y items" - Y là bao nhiêu?**
   - Nếu Y = X → Map đúng
   - Nếu Y > X → Map bị duplicate

### Bước 6: Kiểm Tra UI

Sau khi xem log, kiểm tra UI:

1. Mở Vue DevTools (Extension)
2. Chọn component `TheHeaderBanHang`
3. Tìm `currentInvoiceItems` trong data
4. Xem có bao nhiêu items

**So sánh:**
- Console log: "🎨 Mapped items: **1** items"
- Vue DevTools: `currentInvoiceItems.length = ?`
- UI hiển thị: **Bao nhiêu dòng?**

## 🎯 Các Trường Hợp Có Thể Xảy Ra

### Case 1: Backend trả về duplicate

**Log:**
```
📦 Dữ liệu từ server: 2 items
📦 Chi tiết: [{"id":1,"name":"Áo","qty":2}, {"id":1,"name":"Áo","qty":2}]
```

**Nguyên nhân:** Backend `getSPHD` query sai, trả về 2 records giống nhau

**Giải pháp:** Sửa query trong `HoaDonChiTietRepo.java`

---

### Case 2: Store bị append thay vì replace

**Log:**
```
📦 Dữ liệu từ server: 1 items  ← Lần 1
...
📦 Dữ liệu từ server: 2 items  ← Lần 2 (sau khi thêm lần 2)
```

**Nguyên nhân:** `store.getAllSPHDArr` bị push thêm thay vì gán mới

**Giải pháp:** Kiểm tra `gbStore.js` - Hàm `getAllSPHD()`

---

### Case 3: Watch trigger lại ngay sau addToBill

**Log:**
```
✅ HOÀN TẤT thêm sản phẩm
👁️ WATCH activeKey triggered  ← Ngay sau đó
📡 WATCH: GỌI API getAllSPHD
🎨 WATCH: Mapped items: 1 items
```

**Nguyên nhân:** Watch `{ immediate: true }` hoặc activeKey bị thay đổi

**Giải pháp:** Thêm flag `isUpdating` để skip watch

---

### Case 4: Map sai - Tạo duplicate trong array

**Log:**
```
📦 Dữ liệu từ server: 1 items
🎨 Mapped items: 2 items  ← ⚠️ SAI!
```

**Nguyên nhân:** Logic map bị lỗi, tạo ra duplicate

**Giải pháp:** Kiểm tra lại hàm map

---

### Case 5: Vue render duplicate do key sai

**Log:**
```
🎨 Mapped items: 1 items
🎨 Chi tiết: [{"id":1,"name":"Áo","qty":2}]
```

**Nhưng UI hiển thị 2 dòng**

**Nguyên nhân:** Vue `:key` không unique hoặc bị cache

**Giải pháp:** 
- Thay `:key="item.id_chi_tiet_san_pham"` 
- Thành `:key="item.id_hoa_don_chi_tiet"` (nếu có)
- Hoặc `:key="`${item.id_hoa_don}_${item.id_chi_tiet_san_pham}`"`

---

## 📊 Checklist Debug

Sau khi test, điền vào đây:

- [ ] Console log hiển thị "📦 Dữ liệu từ server: **___** items"
- [ ] Console log hiển thị "🎨 Mapped items: **___** items"
- [ ] Watch có trigger ngay sau addToBill: **YES / NO**
- [ ] Vue DevTools: `currentInvoiceItems.length = ___`
- [ ] UI hiển thị: **___** dòng
- [ ] Database (SQL): `SELECT * FROM hoa_don_chi_tiet WHERE id_hoa_don = 1` → **___** records

## 🔧 Giải Pháp Tạm Thời

Nếu vẫn bị duplicate, thử thêm `Array.from(new Set(...))` để loại bỏ:

```javascript
currentTab.items.value = Array.from(
    new Map(
        store.getAllSPHDArr.map(item => [item.id_chi_tiet_san_pham, {
            id_hoa_don: item.id_hoa_don,
            id_chi_tiet_san_pham: item.id_chi_tiet_san_pham,
            // ... rest
        }])
    ).values()
);
```

Hoặc dùng `lodash.uniqBy`:

```javascript
import { uniqBy } from 'lodash';

currentTab.items.value = uniqBy(
    store.getAllSPHDArr.map(...),
    'id_chi_tiet_san_pham'
);
```

---

## 📅 Thông Tin

**File debug:** `TheHeader-BanHang.vue`

**Đã thêm log:** ✅

**Cần test:** ⚠️ Làm theo hướng dẫn trên và gửi kết quả console log

**Mục tiêu:** Tìm chính xác chỗ nào tạo ra duplicate

---

**🎯 SAU KHI TEST, HÃY CHỤP LẠI CONSOLE LOG VÀ GỬI CHO TÔI!**
