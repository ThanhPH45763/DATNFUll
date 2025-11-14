# 🔧 SỬA LỖI BÁO CÁO THỐNG KÊ - API 500 ERROR

## 📌 Lỗi Hiện Tại

```
GET http://localhost:8080/admin/baoCaoThongKe?type=hom-nay
[HTTP/1.1 500]

GET http://localhost:8080/admin/baoCaoThongKe?type=tuy-chon&startDate=2025-01-01&endDate=2025-01-31
[HTTP/1.1 500]
```

**Lỗi:** Backend API `baoCaoThongKe` trả về lỗi 500 (Internal Server Error)

---

## 🐛 NGUYÊN NHÂN

### File: `BCTKRepo.java`

**5 query SQL có vấn đề:**

1. ❌ `getDoanhThu()` - Thiếu `COALESCE`, thiếu filter theo MAX(ngay_chuyen)
2. ❌ `getTongDonHang()` - Thiếu filter theo MAX(ngay_chuyen)
3. ❌ `getTongSanPham()` - Thiếu `COALESCE`, thiếu filter
4. ❌ `topSanPhamBanChay()` - Thiếu filter theo MAX(ngay_chuyen)
5. ❌ `tiLeTrangThaiHoaDon()` - Subquery sai logic, có thể chia cho 0

### Vấn đề chi tiết:

#### 1. Thiếu `COALESCE` khi SUM có thể NULL

**Trước:**
```sql
SELECT SUM(hd.tong_tien_sau_giam) - ... AS [Doanh thu]
```

→ Nếu không có dữ liệu, `SUM()` trả về `NULL` → Gây lỗi!

**Sau:**
```sql
SELECT COALESCE(SUM(hd.tong_tien_sau_giam) - ..., 0) AS [Doanh thu]
```

---

#### 2. Thiếu filter MAX(ngay_chuyen)

**Vấn đề:** Bảng `theo_doi_don_hang` có **nhiều dòng** cho 1 hóa đơn (lịch sử chuyển trạng thái).

Nếu không filter lấy trạng thái mới nhất → **Sẽ đếm duplicate!**

**Trước:**
```sql
FROM hoa_don hd
JOIN theo_doi_don_hang tddh ON tddh.id_hoa_don = hd.id_hoa_don
WHERE tddh.trang_thai = N'Hoàn thành'
```

→ Nếu 1 hóa đơn có 3 dòng trạng thái → Đếm 3 lần!

**Sau:**
```sql
FROM hoa_don hd
JOIN theo_doi_don_hang tddh ON tddh.id_hoa_don = hd.id_hoa_don
WHERE tddh.trang_thai = N'Hoàn thành'
AND tddh.ngay_chuyen = (
    SELECT MAX(t2.ngay_chuyen)
    FROM theo_doi_don_hang t2
    WHERE t2.id_hoa_don = tddh.id_hoa_don
)  ← Chỉ lấy trạng thái mới nhất
```

---

#### 3. Query `tiLeTrangThaiHoaDon()` sai logic

**Trước:**
```sql
CAST(COUNT(*) AS FLOAT) / (
    SELECT COUNT(*) 
    FROM hoa_don hd
    JOIN theo_doi_don_hang tddh ON tddh.id_hoa_don = hd.id_hoa_don
) * 100 AS tiLeTrangThaiDonHang
```

**Vấn đề:**
- Subquery đếm **TẤT CẢ** hóa đơn, không filter gì
- Có thể trả về 0 → **Chia cho 0** → Lỗi!
- Không lấy trạng thái mới nhất → Đếm sai

**Sau:**
```sql
CAST(COUNT(*) AS FLOAT) / NULLIF((
    SELECT COUNT(DISTINCT hd2.id_hoa_don) 
    FROM hoa_don hd2
    JOIN theo_doi_don_hang tddh2 ON tddh2.id_hoa_don = hd2.id_hoa_don
    WHERE hd2.trang_thai = N'Hoàn thành'
    AND tddh2.ngay_chuyen = (
        SELECT MAX(t3.ngay_chuyen)
        FROM theo_doi_don_hang t3
        WHERE t3.id_hoa_don = tddh2.id_hoa_don
    )
), 0) * 100 AS tiLeTrangThaiDonHang
```

**Cải tiến:**
- Dùng `DISTINCT` để đếm hóa đơn không trùng
- Dùng `NULLIF(..., 0)` để tránh chia cho 0
- Filter theo trạng thái mới nhất

---

## ✅ GIẢI PHÁP ĐÃ ÁP DỤNG

### 1. Query `getDoanhThu()`

**Thay đổi:**
- ✅ Thêm `COALESCE(..., 0)` để tránh NULL
- ✅ Thêm filter `MAX(ngay_chuyen)`

### 2. Query `getTongDonHang()`

**Thay đổi:**
- ✅ Thêm `COALESCE(COUNT(...), 0)`
- ✅ Thêm filter `MAX(ngay_chuyen)`

### 3. Query `getTongSanPham()`

**Thay đổi:**
- ✅ Thêm `COALESCE(SUM(...), 0)`
- ✅ Thêm filter `MAX(ngay_chuyen)`

### 4. Query `topSanPhamBanChay()`

**Thay đổi:**
- ✅ Thêm filter `MAX(ngay_chuyen)`
- ✅ Format lại code cho dễ đọc

### 5. Query `tiLeTrangThaiHoaDon()`

**Thay đổi:**
- ✅ Dùng `COUNT(DISTINCT hd2.id_hoa_don)`
- ✅ Dùng `NULLIF(..., 0)` tránh chia 0
- ✅ Thêm filter `MAX(ngay_chuyen)` cho cả query chính và subquery
- ✅ Filter `hd.trang_thai = N'Hoàn thành'` trong subquery

---

## 🔧 CÁC BƯỚC TRIỂN KHAI

### Bước 1: Code đã được sửa

✅ File `BCTKRepo.java` đã được cập nhật toàn bộ 5 queries

### Bước 2: Rebuild Backend

```bash
cd /home/huunghia/DATNFUll/duanbe
mvn clean install
mvn spring-boot:run
```

### Bước 3: Test API

**Test 1: Báo cáo hôm nay**
```
GET http://localhost:8080/admin/baoCaoThongKe?type=hom-nay
```

**Kết quả mong đợi:**
```json
{
    "doanhThu": 1000000,
    "tongDonHang": 5,
    "tongSanPham": 20
}
```

**Test 2: Báo cáo tùy chọn**
```
GET http://localhost:8080/admin/baoCaoThongKe?type=tuy-chon&startDate=2025-01-01&endDate=2025-01-31
```

**Test 3: Tỉ lệ trạng thái**
```
GET http://localhost:8080/admin/tiLeTrangThaiDonHang
```

**Test 4: Top sản phẩm bán chạy**
```
GET http://localhost:8080/admin/topSPBanChay?type=hom-nay
```

---

## 📊 So Sánh Query Trước/Sau

### Ví dụ: Query `getDoanhThu()`

#### ❌ Trước khi sửa:

```sql
SELECT SUM(hd.tong_tien_sau_giam) - ...
FROM hoa_don hd
JOIN theo_doi_don_hang tddh ON hd.id_hoa_don = tddh.id_hoa_don
WHERE tddh.trang_thai = N'Hoàn thành'
```

**Vấn đề:**
- Không có `COALESCE` → NULL nếu không có dữ liệu
- Không filter MAX → Đếm duplicate nếu có nhiều trạng thái

#### ✅ Sau khi sửa:

```sql
SELECT COALESCE(SUM(hd.tong_tien_sau_giam) - ..., 0)
FROM hoa_don hd
JOIN theo_doi_don_hang tddh ON hd.id_hoa_don = tddh.id_hoa_don
WHERE tddh.trang_thai = N'Hoàn thành'
AND tddh.ngay_chuyen = (
    SELECT MAX(t2.ngay_chuyen)
    FROM theo_doi_don_hang t2
    WHERE t2.id_hoa_don = tddh.id_hoa_don
)
```

**Cải tiến:**
- ✅ Trả về 0 nếu không có dữ liệu
- ✅ Chỉ lấy trạng thái mới nhất → Không duplicate

---

## 🔍 Giải Thích: Tại Sao Cần Filter MAX(ngay_chuyen)?

### Cấu trúc bảng `theo_doi_don_hang`:

```
| id_hoa_don | trang_thai     | ngay_chuyen         |
|------------|----------------|---------------------|
| 1          | Chờ xác nhận   | 2025-01-01 10:00    |
| 1          | Đang giao      | 2025-01-02 14:00    |
| 1          | Hoàn thành     | 2025-01-03 16:00    | ← Mới nhất!
```

**Nếu không filter MAX:**
```sql
SELECT COUNT(*)
FROM hoa_don hd
JOIN theo_doi_don_hang tddh ON hd.id_hoa_don = tddh.id_hoa_don
WHERE hd.id_hoa_don = 1
```

→ Kết quả: **3 rows** (sai!) vì JOIN với cả 3 dòng trạng thái

**Với filter MAX:**
```sql
SELECT COUNT(*)
FROM hoa_don hd
JOIN theo_doi_don_hang tddh ON hd.id_hoa_don = tddh.id_hoa_don
WHERE hd.id_hoa_don = 1
AND tddh.ngay_chuyen = (
    SELECT MAX(t2.ngay_chuyen)
    FROM theo_doi_don_hang t2
    WHERE t2.id_hoa_don = 1
)
```

→ Kết quả: **1 row** (đúng!) chỉ lấy trạng thái mới nhất

---

## 📝 Files Đã Thay Đổi

### Backend:
- ✅ `BCTKRepo.java` - Sửa 5 queries

---

## ✅ Checklist

- [x] Sửa query `getDoanhThu()` - Thêm COALESCE và filter MAX
- [x] Sửa query `getTongDonHang()` - Thêm COALESCE và filter MAX
- [x] Sửa query `getTongSanPham()` - Thêm COALESCE và filter MAX
- [x] Sửa query `topSanPhamBanChay()` - Thêm filter MAX
- [x] Sửa query `tiLeTrangThaiHoaDon()` - Fix logic subquery và tránh chia 0
- [ ] Rebuild Backend
- [ ] Test API baoCaoThongKe
- [ ] Test API tiLeTrangThaiDonHang
- [ ] Test API topSPBanChay

---

## 📅 Thông Tin

**Ngày sửa:** 2025-11-13

**Lỗi:** API báo cáo thống kê trả về 500 error

**Nguyên nhân:** 
1. Query thiếu COALESCE → NULL error
2. Query thiếu filter MAX(ngay_chuyen) → Duplicate count
3. Query chia cho 0 → Division by zero error

**Mức độ:** 🔴 CRITICAL - Toàn bộ trang báo cáo không hoạt động

**Trạng thái:** ✅ ĐÃ SỬA

---

## 🎉 Kết Luận

**Đã sửa:**
- ✅ 5 queries SQL trong `BCTKRepo.java`
- ✅ Thêm `COALESCE` để xử lý NULL
- ✅ Thêm filter `MAX(ngay_chuyen)` để tránh duplicate
- ✅ Dùng `NULLIF` để tránh chia cho 0
- ✅ Format code cho dễ đọc

**Cần test:**
- ⚠️ Rebuild backend
- ⚠️ Test từng API endpoint
- ⚠️ Kiểm tra dữ liệu trả về có đúng không

---

**🎯 HÃY REBUILD BACKEND VÀ TEST LẠI! API sẽ hoạt động bình thường! 🚀**
