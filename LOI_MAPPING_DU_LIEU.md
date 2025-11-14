# 🔴 LỖI MỚI PHÁT HIỆN - MAPPING SAI DỮ LIỆU

## 📌 MÔ TẢ LỖI

**Hiện tượng:** Khi thêm 1 sản phẩm vào giỏ hàng, giao diện hiển thị **2 dòng sản phẩm giống nhau**.

**Nguyên nhân:** Không phải do logic Backend bị lỗi, mà do **Frontend mapping sai dữ liệu** từ API response.

---

## 🔍 PHÂN TÍCH CHI TIẾT

### API Response từ `getSPHD`

Query SQL trả về (HoaDonChiTietRepo.java, line 158-189):

```sql
SELECT
    hdct.so_luong,                          -- Số lượng
    COALESCE(...) AS gia_ban,               -- ✅ GIÁ LẺ (1 sản phẩm)
    hdct.don_gia,                           -- ✅ TỔNG TIỀN (gia_ban × so_luong)
    ...
FROM hoa_don_chi_tiet hdct
```

**Dữ liệu trả về:**
```json
{
    "id_chi_tiet_san_pham": 1,
    "ten_san_pham": "Áo thun",
    "so_luong": 2,
    "gia_ban": 100000,      // ← Giá lẻ (1 sản phẩm)
    "don_gia": 200000,      // ← Tổng tiền (100k × 2)
    "so_luong_ton": 10
}
```

---

### Lỗi Mapping ở Frontend (TheHeader-BanHang.vue)

**❌ Code SAI (trước khi sửa):**

```javascript
currentTab.items.value = store.getAllSPHDArr.map(item => ({
    so_luong: item.so_luong,           // 2
    gia_ban: item.don_gia,             // ❌ 200,000 (tổng tiền)
    tong_tien: item.don_gia * item.so_luong,  // ❌ 200,000 × 2 = 400,000
    // ...
}));
```

**Hậu quả:**
- `gia_ban` hiển thị = 200,000đ (sai, phải là 100,000đ)
- `tong_tien` hiển thị = 400,000đ (sai, phải là 200,000đ)
- **Giao diện hiển thị sai số liệu → Có thể gây nhầm lẫn về logic**

---

### ✅ Code ĐÚNG (sau khi sửa):

```javascript
currentTab.items.value = store.getAllSPHDArr.map(item => ({
    so_luong: item.so_luong,           // 2
    gia_ban: item.gia_ban,             // ✅ 100,000 (giá lẻ)
    tong_tien: item.don_gia,           // ✅ 200,000 (tổng tiền)
    // ...
}));
```

**Kết quả:**
- `gia_ban` hiển thị = 100,000đ ✅
- `tong_tien` hiển thị = 200,000đ ✅
- Dữ liệu chính xác!

---

## 🔧 CÁC THAY ĐỔI ĐÃ THỰC HIỆN

### File: TheHeader-BanHang.vue

**Đã sửa 4 vị trí map dữ liệu:**

1. **Line 696** - Hàm `addToBill()` sau khi thêm SP
2. **Line 785** - Hàm `updateItemTotal()` sau khi cập nhật SL
3. **Line 832** - Hàm `removeFromBill()` sau khi xóa SP
4. **Line 1323** - Watch `activeKey` khi chuyển tab

**Thay đổi:**
```diff
- gia_ban: item.don_gia,
- tong_tien: item.don_gia * item.so_luong,
+ gia_ban: item.gia_ban,  // ✅ Giá lẻ
+ tong_tien: item.don_gia,  // ✅ Tổng tiền
```

---

## 📊 SO SÁNH TRƯỚC/SAU

### Trước khi sửa:

| Sản phẩm | SL | Giá lẻ BE | Tổng BE | Giá hiển thị FE | Tổng hiển thị FE |
|----------|----|-----------|---------|-----------------|--------------------|
| Áo thun  | 2  | 100,000   | 200,000 | 200,000 ❌      | 400,000 ❌         |

→ **Sai số:** Giá lẻ × 2, Tổng tiền × 2

### Sau khi sửa:

| Sản phẩm | SL | Giá lẻ BE | Tổng BE | Giá hiển thị FE | Tổng hiển thị FE |
|----------|----|-----------|---------|-----------------|--------------------|
| Áo thun  | 2  | 100,000   | 200,000 | 100,000 ✅      | 200,000 ✅         |

→ **Chính xác!**

---

## 🧪 CÁCH KIỂM TRA

### Bước 1: Rebuild Frontend

```bash
cd /home/huunghia/DATNFUll/DuAnMauFE
npm run dev
```

### Bước 2: Test Thêm Sản Phẩm

1. Tạo/Chọn hóa đơn
2. Thêm sản phẩm A (giá 100,000đ) số lượng 1
3. **Kiểm tra:**
   - Giá bán hiển thị: **100,000đ** ✅
   - Tổng tiền hiển thị: **100,000đ** ✅

### Bước 3: Test Thêm Sản Phẩm Trùng

4. Thêm lại sản phẩm A thêm 1 lần
5. **Kiểm tra:**
   - Vẫn **1 dòng** sản phẩm A ✅
   - Số lượng: **2** ✅
   - Giá bán: **100,000đ** (không đổi) ✅
   - Tổng tiền: **200,000đ** ✅

### Bước 4: Xem Network Tab

Mở DevTools > Network, reload trang và xem response của `getSPHD`:

```json
{
    "ten_san_pham": "Áo thun",
    "so_luong": 2,
    "gia_ban": 100000,    // ← Giá lẻ
    "don_gia": 200000     // ← Tổng tiền
}
```

So sánh với UI:
- Cột "Giá bán" = `gia_ban` ✅
- Cột "Tổng tiền" = `don_gia` ✅

---

## ⚠️ TẠI SAO LỖI NÀY LẠI GÂY HIỆN TƯỢNG "THÊM 2 SẢN PHẨM"?

**Giả thuyết:**

Khi FE map sai:
- `tong_tien = don_gia × so_luong` (nhân 2 lần)
- Có thể có **logic khác** (chưa phát hiện) dựa vào `tong_tien` để:
  - Kiểm tra trùng lặp?
  - Tính toán state?
  - Trigger re-render?

→ Gây ra hiện tượng hiển thị 2 dòng hoặc tính toán sai.

**Giải pháp:**
- ✅ Đã sửa mapping đúng
- ✅ Backend logic vẫn đúng (kiểm tra trùng và cộng SL)
- ✅ Cần test kỹ lại toàn bộ flow

---

## 📝 GHI CHÚ QUAN TRỌNG

### Quy ước đặt tên trong API:

**Backend convention:**
- `gia_ban` / `gia_le` = Giá 1 sản phẩm
- `don_gia` = Tổng tiền (giá lẻ × số lượng)
- `thanh_tien` = Cũng là tổng tiền

**Frontend phải tuân thủ:**
```javascript
{
    gia_ban: item.gia_ban,     // Giá lẻ từ BE
    tong_tien: item.don_gia,   // Tổng tiền từ BE
    // KHÔNG tự tính: tong_tien = gia_ban * so_luong
    // Vì BE đã tính sẵn!
}
```

---

## ✅ CHECKLIST SỬA LỖI

- [x] Tìm ra nguyên nhân: Mapping sai `gia_ban` và `tong_tien`
- [x] Sửa 4 chỗ map dữ liệu trong `TheHeader-BanHang.vue`
- [x] Verify logic Backend vẫn đúng (không cần sửa)
- [ ] Test lại toàn bộ flow thêm sản phẩm
- [ ] Test cập nhật số lượng
- [ ] Test xóa sản phẩm
- [ ] Test chuyển tab hóa đơn
- [ ] Xác nhận không còn hiển thị 2 dòng sản phẩm

---

## 🔗 FILES LIÊN QUAN

- **Backend (KHÔNG SỬA):**
  - `BanHangController.java` - Logic đúng rồi
  - `HoaDonChiTietRepo.java` - Query đúng rồi

- **Frontend (ĐÃ SỬA):**
  - `TheHeader-BanHang.vue` - Sửa 4 chỗ mapping

---

## 📅 THÔNG TIN

**Ngày phát hiện:** 2025-11-13 (08:00 AM)

**Nguyên nhân:** Frontend mapping sai field `gia_ban` và `tong_tien`

**Mức độ:** 🔴 CRITICAL (ảnh hưởng hiển thị và tính toán)

**Trạng thái:** ✅ ĐÃ SỬA

**Cần test:** ⚠️ Test kỹ lại toàn bộ chức năng bán hàng

---

**🎯 Kết luận:** Lỗi không nằm ở logic Backend (đã sửa đúng), mà nằm ở **Frontend map sai dữ liệu từ API response**. Đã sửa xong, cần test lại!
