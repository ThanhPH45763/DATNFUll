-- ============================================================
-- FILE KIỂM TRA DATABASE - BÁN HÀNG TẠI QUẦY
-- ============================================================
-- Mục đích: Kiểm tra dữ liệu sau khi sửa lỗi thêm sản phẩm trùng
-- Ngày tạo: 2025-11-13
-- ============================================================

USE QLBanQuanAo;
GO

-- ============================================================
-- 1. KIỂM TRA SẢN PHẨM TRÙNG TRONG HÓA ĐƠN
-- ============================================================
-- Tìm các hóa đơn có sản phẩm bị trùng (xuất hiện > 1 lần)
-- Nếu query này trả về kết quả → Vẫn còn lỗi

SELECT 
    hd.id_hoa_don,
    hd.ma_hoa_don,
    hd.trang_thai,
    hdct.id_chi_tiet_san_pham,
    sp.ten_san_pham,
    COUNT(*) as so_lan_xuat_hien
FROM hoa_don_chi_tiet hdct
JOIN hoa_don hd ON hd.id_hoa_don = hdct.id_hoa_don
JOIN chi_tiet_san_pham ctsp ON ctsp.id_chi_tiet_san_pham = hdct.id_chi_tiet_san_pham
JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
WHERE hd.trang_thai IN (N'Đang chờ', N'Chưa thanh toán')
GROUP BY hd.id_hoa_don, hd.ma_hoa_don, hd.trang_thai, hdct.id_chi_tiet_san_pham, sp.ten_san_pham
HAVING COUNT(*) > 1
ORDER BY hd.id_hoa_don;

-- Kết quả mong đợi: KHÔNG CÓ DÒNG NÀO
-- Nếu có dòng → Còn lỗi trùng sản phẩm

-- ============================================================
-- 2. KIỂM TRA GIÁ KHUYẾN MÃI ĐƯỢC TÍNH ĐÚNG
-- ============================================================
-- So sánh giá sau giảm đã lưu vs giá tính lại

SELECT 
    ctsp.id_chi_tiet_san_pham,
    sp.ten_san_pham,
    ctsp.gia_ban AS gia_goc,
    km.kieu_giam_gia,
    km.gia_tri_giam,
    km.gia_tri_toi_da,
    ctkm.gia_sau_giam AS gia_da_luu,
    
    -- ✅ Tính lại giá theo logic
    CASE 
        WHEN km.kieu_giam_gia = N'Phần trăm' THEN
            ctsp.gia_ban - 
            CASE 
                WHEN (ctsp.gia_ban * km.gia_tri_giam / 100.0) > ISNULL(km.gia_tri_toi_da, ctsp.gia_ban)
                THEN ISNULL(km.gia_tri_toi_da, ctsp.gia_ban)
                ELSE (ctsp.gia_ban * km.gia_tri_giam / 100.0)
            END
        WHEN km.kieu_giam_gia = N'Tiền mặt' THEN
            ctsp.gia_ban - 
            CASE 
                WHEN km.gia_tri_giam > ISNULL(km.gia_tri_toi_da, ctsp.gia_ban)
                THEN ISNULL(km.gia_tri_toi_da, ctsp.gia_ban)
                ELSE km.gia_tri_giam
            END
        ELSE ctsp.gia_ban
    END AS gia_tinh_lai,
    
    -- ✅ Kiểm tra sai lệch
    CASE 
        WHEN ABS(ctkm.gia_sau_giam - 
            CASE 
                WHEN km.kieu_giam_gia = N'Phần trăm' THEN
                    ctsp.gia_ban - 
                    CASE 
                        WHEN (ctsp.gia_ban * km.gia_tri_giam / 100.0) > ISNULL(km.gia_tri_toi_da, ctsp.gia_ban)
                        THEN ISNULL(km.gia_tri_toi_da, ctsp.gia_ban)
                        ELSE (ctsp.gia_ban * km.gia_tri_giam / 100.0)
                    END
                WHEN km.kieu_giam_gia = N'Tiền mặt' THEN
                    ctsp.gia_ban - 
                    CASE 
                        WHEN km.gia_tri_giam > ISNULL(km.gia_tri_toi_da, ctsp.gia_ban)
                        THEN ISNULL(km.gia_tri_toi_da, ctsp.gia_ban)
                        ELSE km.gia_tri_giam
                    END
                ELSE ctsp.gia_ban
            END) > 1
        THEN N'⚠️ SAI'
        ELSE N'✅ ĐÚNG'
    END AS ket_qua
    
FROM chi_tiet_khuyen_mai ctkm
JOIN chi_tiet_san_pham ctsp ON ctsp.id_chi_tiet_san_pham = ctkm.id_chi_tiet_san_pham
JOIN khuyen_mai km ON km.id_khuyen_mai = ctkm.id_khuyen_mai
JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
WHERE km.trang_thai = N'Đang diễn ra'
  AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
ORDER BY sp.ten_san_pham;

-- Kết quả mong đợi: Cột [ket_qua] đều là '✅ ĐÚNG'
-- Nếu có '⚠️ SAI' → Cần cập nhật lại gia_sau_giam

-- ============================================================
-- 3. KIỂM TRA SẢN PHẨM CÓ NHIỀU KHUYẾN MÃI
-- ============================================================
-- Tìm sản phẩm có > 1 khuyến mãi cùng lúc

SELECT 
    ctsp.id_chi_tiet_san_pham,
    sp.ten_san_pham,
    ctsp.gia_ban AS gia_goc,
    COUNT(*) AS so_khuyen_mai,
    MIN(ctkm.gia_sau_giam) AS gia_tot_nhat,
    STRING_AGG(CAST(ctkm.gia_sau_giam AS VARCHAR), ', ') AS cac_gia_khuyen_mai
FROM chi_tiet_khuyen_mai ctkm
JOIN chi_tiet_san_pham ctsp ON ctsp.id_chi_tiet_san_pham = ctkm.id_chi_tiet_san_pham
JOIN khuyen_mai km ON km.id_khuyen_mai = ctkm.id_khuyen_mai
JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
WHERE km.trang_thai = N'Đang diễn ra'
  AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
GROUP BY ctsp.id_chi_tiet_san_pham, sp.ten_san_pham, ctsp.gia_ban
HAVING COUNT(*) > 1
ORDER BY so_khuyen_mai DESC;

-- Kết quả: Hiển thị sản phẩm có nhiều KM và giá tốt nhất được chọn

-- ============================================================
-- 4. KIỂM TRA HÓA ĐƠN ĐANG CHỜ VÀ CHI TIẾT
-- ============================================================
-- Xem chi tiết hóa đơn đang chờ thanh toán

SELECT 
    hd.id_hoa_don,
    hd.ma_hoa_don,
    hd.trang_thai,
    hd.ngay_tao,
    COUNT(DISTINCT hdct.id_chi_tiet_san_pham) AS so_san_pham_khac_nhau,
    SUM(hdct.so_luong) AS tong_so_luong,
    hd.tong_tien_truoc_giam,
    hd.tong_tien_sau_giam
FROM hoa_don hd
LEFT JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id_hoa_don
WHERE hd.trang_thai IN (N'Đang chờ', N'Chưa thanh toán')
GROUP BY hd.id_hoa_don, hd.ma_hoa_don, hd.trang_thai, hd.ngay_tao, hd.tong_tien_truoc_giam, hd.tong_tien_sau_giam
ORDER BY hd.ngay_tao DESC;

-- ============================================================
-- 5. XEM CHI TIẾT MỘT HÓA ĐƠN CỤ THỂ
-- ============================================================
-- Thay @idHoaDon bằng ID hóa đơn cần kiểm tra

DECLARE @idHoaDon INT = 1; -- Thay đổi ID này

SELECT 
    hdct.id_hoa_don_chi_tiet,
    sp.ten_san_pham,
    ms.ten_mau_sac,
    kt.gia_tri AS kich_thuoc,
    hdct.so_luong,
    hdct.don_gia AS don_gia_luu,
    hdct.don_gia / hdct.so_luong AS gia_le,
    
    -- Giá từ chi_tiet_san_pham
    ctsp.gia_ban AS gia_goc_ctsp,
    
    -- Giá khuyến mãi tốt nhất
    (SELECT MIN(ctkm2.gia_sau_giam)
     FROM chi_tiet_khuyen_mai ctkm2
     JOIN khuyen_mai km2 ON km2.id_khuyen_mai = ctkm2.id_khuyen_mai
     WHERE ctkm2.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
       AND km2.trang_thai = N'Đang diễn ra'
       AND GETDATE() BETWEEN km2.ngay_bat_dau AND km2.ngay_het_han
    ) AS gia_khuyen_mai_tot_nhat,
    
    ctsp.so_luong AS ton_kho_hien_tai
FROM hoa_don_chi_tiet hdct
JOIN chi_tiet_san_pham ctsp ON ctsp.id_chi_tiet_san_pham = hdct.id_chi_tiet_san_pham
JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
LEFT JOIN mau_sac ms ON ms.id_mau_sac = ctsp.id_mau_sac
LEFT JOIN kich_thuoc kt ON kt.id_kich_thuoc = ctsp.id_kich_thuoc
WHERE hdct.id_hoa_don = @idHoaDon
ORDER BY hdct.id_hoa_don_chi_tiet;

-- ============================================================
-- 6. KIỂM TRA TỒN KHO VÀ SỐ LƯỢNG TRONG HÓA ĐƠN
-- ============================================================
-- Đảm bảo số lượng trong hóa đơn không vượt quá tồn kho ban đầu

SELECT 
    ctsp.id_chi_tiet_san_pham,
    sp.ten_san_pham,
    ms.ten_mau_sac,
    kt.gia_tri AS kich_thuoc,
    ctsp.so_luong AS ton_kho_hien_tai,
    ISNULL(SUM(hdct.so_luong), 0) AS dang_trong_hoa_don,
    ctsp.so_luong + ISNULL(SUM(hdct.so_luong), 0) AS tong_ton_ban_dau
FROM chi_tiet_san_pham ctsp
JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
LEFT JOIN mau_sac ms ON ms.id_mau_sac = ctsp.id_mau_sac
LEFT JOIN kich_thuoc kt ON kt.id_kich_thuoc = ctsp.id_kich_thuoc
LEFT JOIN hoa_don_chi_tiet hdct ON hdct.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
LEFT JOIN hoa_don hd ON hd.id_hoa_don = hdct.id_hoa_don 
    AND hd.trang_thai IN (N'Đang chờ', N'Chưa thanh toán')
GROUP BY ctsp.id_chi_tiet_san_pham, sp.ten_san_pham, ms.ten_mau_sac, kt.gia_tri, ctsp.so_luong
ORDER BY sp.ten_san_pham;

-- ============================================================
-- 7. DỌN DỮ LIỆU SẢN PHẨM TRÙNG (NẾU CÓ)
-- ============================================================
-- ⚠️ CHỈ CHẠY NẾU QUERY #1 PHÁT HIỆN CÓ SẢN PHẨM TRÙNG

-- Backup trước khi xóa
SELECT * 
INTO hoa_don_chi_tiet_backup_20251113
FROM hoa_don_chi_tiet;

-- Gộp sản phẩm trùng thành 1 dòng (giữ dòng đầu, cộng số lượng)
WITH DuplicateProducts AS (
    SELECT 
        id_hoa_don,
        id_chi_tiet_san_pham,
        MIN(id_hoa_don_chi_tiet) AS id_giu_lai,
        SUM(so_luong) AS tong_so_luong,
        MAX(don_gia / so_luong) AS gia_le
    FROM hoa_don_chi_tiet hdct
    JOIN hoa_don hd ON hd.id_hoa_don = hdct.id_hoa_don
    WHERE hd.trang_thai IN (N'Đang chờ', N'Chưa thanh toán')
    GROUP BY id_hoa_don, id_chi_tiet_san_pham
    HAVING COUNT(*) > 1
)
-- Cập nhật dòng giữ lại
UPDATE hdct
SET 
    so_luong = dp.tong_so_luong,
    don_gia = dp.gia_le * dp.tong_so_luong
FROM hoa_don_chi_tiet hdct
JOIN DuplicateProducts dp ON dp.id_giu_lai = hdct.id_hoa_don_chi_tiet;

-- Xóa các dòng trùng (giữ lại dòng đầu)
WITH DuplicateProducts AS (
    SELECT 
        id_hoa_don,
        id_chi_tiet_san_pham,
        MIN(id_hoa_don_chi_tiet) AS id_giu_lai
    FROM hoa_don_chi_tiet hdct
    JOIN hoa_don hd ON hd.id_hoa_don = hdct.id_hoa_don
    WHERE hd.trang_thai IN (N'Đang chờ', N'Chưa thanh toán')
    GROUP BY id_hoa_don, id_chi_tiet_san_pham
    HAVING COUNT(*) > 1
)
DELETE hdct
FROM hoa_don_chi_tiet hdct
WHERE EXISTS (
    SELECT 1 
    FROM DuplicateProducts dp
    WHERE dp.id_hoa_don = hdct.id_hoa_don
      AND dp.id_chi_tiet_san_pham = hdct.id_chi_tiet_san_pham
      AND dp.id_giu_lai <> hdct.id_hoa_don_chi_tiet
);

-- ============================================================
-- 8. THÊM UNIQUE CONSTRAINT (TÙY CHỌN)
-- ============================================================
-- Ngăn chặn thêm sản phẩm trùng ở mức database

-- Kiểm tra constraint đã tồn tại chưa
IF NOT EXISTS (
    SELECT 1 
    FROM sys.indexes 
    WHERE name = 'UK_hoa_don_ctsp' 
      AND object_id = OBJECT_ID('hoa_don_chi_tiet')
)
BEGIN
    ALTER TABLE hoa_don_chi_tiet
    ADD CONSTRAINT UK_hoa_don_ctsp UNIQUE (id_hoa_don, id_chi_tiet_san_pham);
    PRINT N'✅ Đã thêm UNIQUE constraint thành công!';
END
ELSE
BEGIN
    PRINT N'ℹ️ UNIQUE constraint đã tồn tại.';
END;

-- ============================================================
-- KẾT THÚC
-- ============================================================
PRINT N'';
PRINT N'=== ✅ HOÀN TẤT KIỂM TRA ===';
PRINT N'Hãy xem kết quả các query ở trên để đánh giá tình trạng dữ liệu.';
