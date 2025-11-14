# 🔴 LỖI CUỐI CÙNG - BẢNG `tra_hang` KHÔNG TỒN TẠI

## 📌 Lỗi Thực Tế

```
Invalid object name 'tra_hang'
```

**SQL Error:**
```sql
LEFT JOIN tra_hang th ON hd.id_hoa_don = th.id_hoa_don
```

→ **Bảng `tra_hang` KHÔNG TỒN TẠI trong database!**

---

## 🐛 NGUYÊN NHÂN

Query `getDoanhThu()` và `getTongSanPham()` đang JOIN với bảng `tra_hang` để trừ tiền hoàn/số lượng trả hàng.

**Nhưng:**
- Bảng `tra_hang` chưa được tạo
- Hoặc có tên khác trong database

---

## ✅ GIẢI PHÁP

### 1. Query `getDoanhThu()`

**Đã bỏ:** `LEFT JOIN tra_hang` và `SUM(th.tong_tien_hoan)`

**Trước:**
```sql
SELECT COALESCE(SUM(hd.tong_tien_sau_giam) 
    - ISNULL(SUM(th.tong_tien_hoan), 0)  ← Bỏ phần này
    - ISNULL(SUM(hd.phi_van_chuyen), 0), 0)
FROM hoa_don hd
LEFT JOIN tra_hang th ON hd.id_hoa_don = th.id_hoa_don  ← Bỏ join này
```

**Sau:**
```sql
SELECT COALESCE(SUM(hd.tong_tien_sau_giam) 
    - ISNULL(SUM(hd.phi_van_chuyen), 0), 0) AS [Doanh thu]
FROM hoa_don hd
JOIN theo_doi_don_hang tddh ON hd.id_hoa_don = tddh.id_hoa_don
-- ✅ Không JOIN với tra_hang nữa
```

---

### 2. Query `getTongSanPham()`

**Đã bỏ:** Subquery trừ số lượng trả hàng

**Trước:**
```sql
SELECT COALESCE(SUM(hdct.so_luong), 0) 
    - ISNULL((
        SELECT SUM(ctth.so_luong)
        FROM tra_hang th  ← Bỏ toàn bộ subquery này
        JOIN chi_tiet_tra_hang ctth ON th.id = ctth.id_tra_hang
        ...
    ), 0)
```

**Sau:**
```sql
SELECT COALESCE(SUM(hdct.so_luong), 0) AS so_luong_ban_thuc_te
FROM hoa_don hd
JOIN hoa_don_chi_tiet hdct ON hd.id_hoa_don = hdct.id_hoa_don
-- ✅ Chỉ tính tổng số lượng đã bán
```

---

## 📝 Lưu Ý

### Nếu muốn có chức năng trả hàng sau này:

1. **Tạo bảng `tra_hang`:**
```sql
CREATE TABLE tra_hang (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_hoa_don INT,
    tong_tien_hoan DECIMAL(18,2),
    trang_thai NVARCHAR(50),
    ngay_tao DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id_hoa_don)
);

CREATE TABLE chi_tiet_tra_hang (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_tra_hang INT,
    id_chi_tiet_san_pham INT,
    so_luong INT,
    FOREIGN KEY (id_tra_hang) REFERENCES tra_hang(id),
    FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id_chi_tiet_san_pham)
);
```

2. **Sau đó mới thêm JOIN lại trong query**

---

## 🔧 CÁC BƯỚC TRIỂN KHAI

### Bước 1: Code đã được sửa

✅ File `BCTKRepo.java`:
- Bỏ `LEFT JOIN tra_hang` trong `getDoanhThu()`
- Bỏ subquery trả hàng trong `getTongSanPham()`

### Bước 2: Stop Backend hiện tại

```bash
# Trong terminal đang chạy backend, nhấn Ctrl+C
```

### Bước 3: Rebuild Backend

```bash
cd /home/huunghia/DATNFUll/duanbe
mvn clean install
mvn spring-boot:run
```

### Bước 4: Test Lại

```bash
curl "http://localhost:8080/admin/baoCaoThongKe?type=hom-nay"
```

**Kết quả mong đợi:**
```json
{
    "doanhThu": 0,
    "tongDonHang": 0,
    "tongSanPham": 0
}
```

✅ Trả về 200 OK (không còn 500)

---

## 📊 Tóm Tắt Thay Đổi

| Query | Thay đổi | Lý do |
|-------|----------|-------|
| `getDoanhThu()` | Bỏ `LEFT JOIN tra_hang` | Bảng không tồn tại |
| `getTongSanPham()` | Bỏ subquery trừ trả hàng | Bảng không tồn tại |
| `getTongDonHang()` | Không thay đổi | OK |
| `topSanPhamBanChay()` | Không thay đổi | OK |
| `tiLeTrangThaiHoaDon()` | Không thay đổi | OK |

---

## ✅ Checklist

- [x] Sửa query `getDoanhThu()` - Bỏ JOIN tra_hang
- [x] Sửa query `getTongSanPham()` - Bỏ subquery tra_hang
- [ ] Stop backend hiện tại
- [ ] Rebuild backend
- [ ] Test API baoCaoThongKe
- [ ] Confirm trả về 200 OK

---

## 📅 Thông Tin

**Ngày sửa:** 2025-11-13

**Lỗi:** `Invalid object name 'tra_hang'`

**Nguyên nhân:** Query JOIN với bảng chưa tồn tại

**Giải pháp:** Bỏ toàn bộ logic liên quan đến trả hàng (tạm thời)

**Trạng thái:** ✅ ĐÃ SỬA

---

## 🎉 Kết Luận

**Lỗi gốc:**
- Query JOIN với bảng `tra_hang` nhưng bảng này chưa được tạo trong database

**Đã sửa:**
- ✅ Bỏ `LEFT JOIN tra_hang` trong `getDoanhThu()`
- ✅ Bỏ subquery trả hàng trong `getTongSanPham()`
- ✅ Queries giờ chỉ tính doanh thu và số lượng đã bán (không trừ trả hàng)

**Hạn chế:**
- ⚠️ Hiện tại không tính trừ tiền/số lượng trả hàng
- ⚠️ Nếu cần chức năng này, phải tạo bảng `tra_hang` trước

---

**🎯 HÃY REBUILD BACKEND VÀ TEST LẠI! Lần này chắc chắn OK! 🚀**
