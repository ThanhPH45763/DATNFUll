# 🔧 SỬA LỖI: Conversion failed when converting nvarchar to bit

## ❌ LỖI GẶP PHẢI

```
SQL Error: 245, SQLState: S0001
Conversion failed when converting the nvarchar value 'Hoạt động' to data type bit.
```

## 🔍 NGUYÊN NHÂN

Trong database SQL Server, cột `san_pham.trang_thai` là kiểu **`BIT`** (boolean: 0/1), nhưng query đang dùng:

```sql
WHERE sp.trang_thai = N'Hoạt động'  -- ❌ SAI: So sánh BIT với string
```

## ✅ GIẢI PHÁP

Đã sửa query trong `SanPhamRepo.java` (line 310):

```sql
WHERE sp.trang_thai = 1  -- ✅ ĐÚNG: So sánh BIT với boolean
```

## 📋 CẤU TRÚC DATABASE

### Bảng `san_pham`:
```sql
CREATE TABLE san_pham (
    ...
    trang_thai BIT DEFAULT 1 NULL,  -- ← Kiểu BIT (0/1)
    ...
)
```
- `1` = Hoạt động (true)
- `0` = Không hoạt động (false)

### Bảng `khuyen_mai`:
```sql
CREATE TABLE khuyen_mai (
    ...
    trang_thai NVARCHAR(50),  -- ← Kiểu NVARCHAR (string)
    ...
)
```
- Giá trị: `'Đang diễn ra'`, `'Đã kết thúc'`, etc.

## 🔧 THAY ĐỔI CODE

**File:** `/duanbe/src/main/java/com/example/duanbe/repository/SanPhamRepo.java`

**Dòng 310** - Query `listSanPhamByTenDM`:

```java
// TRƯỚC (LỖI):
WHERE 
    sp.trang_thai = N'Hoạt động'  // ❌
    AND km.trang_thai = N'Đang diễn ra'  // ✅ Cái này đúng vì khuyen_mai.trang_thai là NVARCHAR

// SAU (ĐÚNG):
WHERE 
    sp.trang_thai = 1  // ✅ Sửa thành 1 (BIT)
    AND km.trang_thai = N'Đang diễn ra'  // ✅ Giữ nguyên vì là NVARCHAR
```

## ✅ KIỂM TRA

1. **Build lại project:**
```bash
cd duanbe
./mvnw clean compile
```

2. **Restart server:**
```bash
./mvnw spring-boot:run
```

3. **Test API:**
```bash
curl "http://localhost:8080/admin/quan_ly_san_pham/getSanPhamByTenDM/formatted?tenDanhMuc=Áo"
```

## 🎯 KẾT QUẢ

✅ Query chạy thành công
✅ Không còn lỗi conversion
✅ Dữ liệu trả về đúng format

## 📝 LƯU Ý

**Quy tắc chung:**
- `san_pham.trang_thai` = BIT → Dùng `1` hoặc `0`
- `khuyen_mai.trang_thai` = NVARCHAR → Dùng `N'Đang diễn ra'`
- `chi_tiet_san_pham.trang_thai` = BIT → Dùng `1` hoặc `0`

**Kiểm tra kiểu dữ liệu trong SQL:**
```sql
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'san_pham' 
  AND COLUMN_NAME = 'trang_thai';
```

---

**Thời gian sửa:** 2025-11-17 17:36
**Trạng thái:** ✅ Đã sửa và test thành công
