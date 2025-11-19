# PHÂN TÍCH VÀ CẢI TIẾN SQL QUERY CHI TIẾT SẢN PHẨM

## 🔴 CÁC LỖI NGHIÊM TRỌNG CẦN SỬA NGAY

### 1. MÂU THUẪN DATABASE vs ENTITY vs QUERY

**Database thực tế (QLquanAo.sql):**
```sql
CREATE TABLE binh_luan (
    id_binh_luan int IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang int,
    id_chi_tiet_san_pham int,
    noi_dung_binh_luan nvarchar(MAX),  -- ⚠️ Tên cột thực tế
    danh_gia int CHECK (danh_gia >= 1 AND danh_gia <= 5),
    ngay_tao datetime,
    ngay_sua datetime,
    chinh_sua bit
)
```

**Entity Java (BinhLuan.java):**
```java
@Column(name = "binh_luan")  // ❌ Sai tên cột!
private String binh_luan;

@Column(name = "danh_gia")
private Float danh_gia;  // ❌ Sai kiểu dữ liệu! DB là INT, Entity là Float
```

**Query SQL hiện tại:**
```sql
COUNT(*) AS so_luong_danh_gia  -- Đếm cả bình luận không có đánh giá
```

### 2. LỖI CTE AnhSanPham

**Query hiện tại:**
```sql
-- CTE định nghĩa:
AnhSanPham AS (
    SELECT
        id_chi_tiet_san_pham,
        STUFF(...) AS hinh_anh  -- ✅ Cột tên 'hinh_anh'
    ...
)

-- Nhưng SELECT lại gọi:
COALESCE(asp.anh_dai_dien, '') AS hinh_anh  -- ❌ Cột 'anh_dai_dien' không tồn tại!
```

### 3. VẤN ĐỀ LOGIC NGHIỆP VỤ

**a) Đếm đánh giá không chính xác:**
```sql
COUNT(*) AS so_luong_danh_gia  -- Đếm tất cả dòng, kể cả không đánh giá
```
Nên sửa thành:
```sql
COUNT(danh_gia) AS so_luong_danh_gia  -- Chỉ đếm có đánh giá
```

**b) Tính trung bình sai:**
```sql
AVG(COALESCE(danh_gia, 0) * 1.0)  -- Tính cả đánh giá = 0 (không hợp lệ vì CHECK >= 1)
```
Nên sửa thành:
```sql
AVG(danh_gia * 1.0)  -- Bỏ COALESCE vì danh_gia có CHECK >= 1
```

---

## ✅ QUERY ĐÃ ĐƯỢC SỬA HOÀN CHỈNH

```sql
@Query(nativeQuery = true, value = """
    WITH DanhGiaSanPham AS (
        SELECT
            id_chi_tiet_san_pham,
            AVG(danh_gia * 1.0) AS danh_gia_trung_binh,
            COUNT(danh_gia) AS so_luong_danh_gia
        FROM binh_luan
        WHERE danh_gia IS NOT NULL
        GROUP BY id_chi_tiet_san_pham
    ),
    KhuyenMaiHieuLuc AS (
        SELECT
            ctkm.id_chi_tiet_san_pham,
            km.kieu_giam_gia,
            km.gia_tri_giam,
            ROW_NUMBER() OVER (
                PARTITION BY ctkm.id_chi_tiet_san_pham
                ORDER BY 
                    CASE 
                        WHEN km.kieu_giam_gia = 'Phần trăm' 
                        THEN km.gia_tri_giam / 100.0
                        ELSE km.gia_tri_giam
                    END DESC,
                    km.ngay_bat_dau DESC
            ) AS rn
        FROM chi_tiet_khuyen_mai ctkm
        JOIN khuyen_mai km ON ctkm.id_khuyen_mai = km.id_khuyen_mai
        WHERE km.trang_thai = 1
          AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
    ),
    KhuyenMaiHieuLucNhat AS (
        SELECT 
            id_chi_tiet_san_pham,
            kieu_giam_gia,
            gia_tri_giam
        FROM KhuyenMaiHieuLuc
        WHERE rn = 1
    ),
    AnhSanPham AS (
        SELECT
            id_chi_tiet_san_pham,
            STUFF((
                SELECT ',' + ha.hinh_anh
                FROM hinh_anh ha
                WHERE ha.id_chi_tiet_san_pham = outer_ha.id_chi_tiet_san_pham
                  AND ha.hinh_anh IS NOT NULL 
                  AND ha.hinh_anh <> ''
                ORDER BY 
                    CASE WHEN ha.anh_chinh = 1 THEN 0 ELSE 1 END,
                    ha.id_hinh_anh
                FOR XML PATH('')
            ), 1, 1, '') AS hinh_anh
        FROM hinh_anh outer_ha
        GROUP BY id_chi_tiet_san_pham
    )

    SELECT
        ctsp.id_chi_tiet_san_pham,
        sp.id_san_pham,
        sp.ma_san_pham,
        sp.ten_san_pham,
        sp.mo_ta,
        dm.ten_danh_muc,
        th.ten_thuong_hieu,
        cl.ten_chat_lieu,
        COALESCE(asp.hinh_anh, sp.anh_dai_dien, '') AS hinh_anh,
        kt.gia_tri,
        kt.don_vi,
        ms.ma_mau_sac,
        ms.ten_mau_sac,
        kt.id_kich_thuoc,
        ms.id_mau_sac,
        ctsp.ngay_tao,
        ctsp.ngay_sua,
        ctsp.so_luong,
        COALESCE(dgs.danh_gia_trung_binh, 0) AS danh_gia_trung_binh,
        COALESCE(dgs.so_luong_danh_gia, 0) AS so_luong_danh_gia,
        ctsp.gia_ban AS GiaGoc,
        COALESCE(
            CASE
                WHEN kh.kieu_giam_gia = 'Phần trăm' 
                THEN ctsp.gia_ban * (1 - kh.gia_tri_giam / 100.0)
                WHEN kh.kieu_giam_gia = 'Tiền mặt' 
                THEN ctsp.gia_ban - kh.gia_tri_giam
            END,
            ctsp.gia_ban
        ) AS GiaHienTai,
        kh.gia_tri_giam AS GiaTriKhuyenMai,
        kh.kieu_giam_gia AS KieuKhuyenMai,
        ctsp.trang_thai
    FROM chi_tiet_san_pham ctsp
    INNER JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
    INNER JOIN danh_muc_san_pham dm ON sp.id_danh_muc = dm.id_danh_muc
    INNER JOIN thuong_hieu th ON sp.id_thuong_hieu = th.id_thuong_hieu
    INNER JOIN chat_lieu cl ON sp.id_chat_lieu = cl.id_chat_lieu
    LEFT JOIN KhuyenMaiHieuLucNhat kh ON ctsp.id_chi_tiet_san_pham = kh.id_chi_tiet_san_pham
    LEFT JOIN DanhGiaSanPham dgs ON ctsp.id_chi_tiet_san_pham = dgs.id_chi_tiet_san_pham
    LEFT JOIN kich_thuoc kt ON kt.id_kich_thuoc = ctsp.id_kich_thuoc
    LEFT JOIN mau_sac ms ON ms.id_mau_sac = ctsp.id_mau_sac
    LEFT JOIN AnhSanPham asp ON ctsp.id_chi_tiet_san_pham = asp.id_chi_tiet_san_pham
    WHERE
        sp.trang_thai = 1
        AND sp.id_san_pham = :idSanPham
    ORDER BY
        ctsp.id_chi_tiet_san_pham;
    """)
ArrayList<ChiTietSanPhamView> getCTSPBySanPhamFull(@Param("idSanPham") Integer idSanPham);
```

---

## 📊 CÁC THAY ĐỔI CHÍNH

### 1. ✅ Sửa CTE DanhGiaSanPham
```sql
-- TRƯỚC:
AVG(COALESCE(danh_gia, 0) * 1.0)  -- Sai logic
COUNT(binh_luan)                   -- Cột không tồn tại

-- SAU:
AVG(danh_gia * 1.0)               -- Đúng vì CHECK >= 1
COUNT(danh_gia)                    -- Chỉ đếm có giá trị
WHERE danh_gia IS NOT NULL         -- Bảo đảm chính xác
```

### 2. ✅ Sửa CTE KhuyenMaiHieuLuc
```sql
-- Thêm điều kiện trang_thai
WHERE km.trang_thai = 1

-- Sắp xếp ưu tiên khuyến mãi tốt nhất
ORDER BY 
    CASE 
        WHEN km.kieu_giam_gia = 'Phần trăm' 
        THEN km.gia_tri_giam / 100.0
        ELSE km.gia_tri_giam
    END DESC
```

### 3. ✅ Sửa CTE AnhSanPham
```sql
-- TRƯỚC:
COALESCE(asp.anh_dai_dien, '')  -- Cột không tồn tại

-- SAU:
COALESCE(asp.hinh_anh, sp.anh_dai_dien, '')  -- Ưu tiên ảnh sản phẩm, fallback ảnh đại diện

-- Sắp xếp ảnh chính lên đầu
ORDER BY 
    CASE WHEN ha.anh_chinh = 1 THEN 0 ELSE 1 END,
    ha.id_hinh_anh
```

### 4. ✅ Tối ưu SELECT
- Loại bỏ SELECT * trong CTE
- Chỉ lấy cột cần thiết để giảm memory

---

## 🔧 CẦN SỬA ENTITY JAVA

### File: BinhLuan.java
```java
@Column(name = "noi_dung_binh_luan")  // ✅ Sửa tên cột
private String noiDungBinhLuan;

@Column(name = "danh_gia")
private Integer danhGia;  // ✅ Sửa từ Float sang Integer
```

---

## 📈 CẢI TIẾN HIỆU SUẤT

### 1. Tạo Index
```sql
-- Index cho bình luận
CREATE NONCLUSTERED INDEX IX_BinhLuan_CTSP_DanhGia 
ON binh_luan(id_chi_tiet_san_pham, danh_gia);

-- Index cho khuyến mãi
CREATE NONCLUSTERED INDEX IX_ChiTietKhuyenMai_CTSP 
ON chi_tiet_khuyen_mai(id_chi_tiet_san_pham, id_khuyen_mai);

CREATE NONCLUSTERED INDEX IX_KhuyenMai_TrangThai_NgayHL
ON khuyen_mai(trang_thai, ngay_bat_dau, ngay_het_han)
INCLUDE (kieu_giam_gia, gia_tri_giam);

-- Index cho hình ảnh
CREATE NONCLUSTERED INDEX IX_HinhAnh_CTSP_AnhChinh
ON hinh_anh(id_chi_tiet_san_pham, anh_chinh)
INCLUDE (hinh_anh);
```

### 2. Cải tiến query AnhSanPham (nếu performance vẫn chậm)
```sql
-- Thay vì FOR XML PATH, dùng STRING_AGG (SQL Server 2017+)
AnhSanPham AS (
    SELECT
        id_chi_tiet_san_pham,
        STRING_AGG(hinh_anh, ',') 
        WITHIN GROUP (ORDER BY 
            CASE WHEN anh_chinh = 1 THEN 0 ELSE 1 END,
            id_hinh_anh
        ) AS hinh_anh
    FROM hinh_anh
    WHERE hinh_anh IS NOT NULL AND hinh_anh <> ''
    GROUP BY id_chi_tiet_san_pham
)
```

---

## 🎯 CHECKLIST TRIỂN KHAI

- [ ] 1. Sửa BinhLuan Entity (tên cột và kiểu dữ liệu)
- [ ] 2. Update query trong ChiTietSanPhamRepo.java
- [ ] 3. Tạo indexes trong database
- [ ] 4. Test query với EXPLAIN/Execution Plan
- [ ] 5. Kiểm tra kết quả hiển thị đúng trên frontend
- [ ] 6. Monitor performance khi có nhiều sản phẩm

---

## 📝 LƯU Ý

1. **Composite Key vs Auto Increment**: Entity dùng composite key nhưng DB có `id_binh_luan` AUTO_INCREMENT → Nên chọn 1 trong 2 cách
2. **Kiểu dữ liệu**: `danh_gia` nên là INT (1-5 sao) thay vì Float
3. **NULL handling**: Với CHECK constraint, không cần COALESCE(danh_gia, 0)
4. **CTE vs Subquery**: CTE dễ đọc hơn nhưng có thể không được optimize tốt bằng derived table trong một số TH
