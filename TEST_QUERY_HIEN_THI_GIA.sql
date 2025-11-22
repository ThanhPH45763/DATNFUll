-- TEST QUERY: Kiểm tra giá khuyến mãi có được lấy đúng không
-- Chạy query này trong SQL Server Management Studio để debug

-- BƯỚC 1: Kiểm tra dữ liệu khuyến mãi
USE QLBanQuanAo;

-- Xem tất cả khuyến mãi đang diễn ra
SELECT 
    km.id_khuyen_mai,
    km.ma_khuyen_mai,
    km.ten_khuyen_mai,
    km.trang_thai,
    km.ngay_bat_dau,
    km.ngay_het_han,
    GETDATE() AS ngay_hien_tai,
    CASE 
        WHEN GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han THEN N'✅ Còn hiệu lực'
        ELSE N'❌ Hết hiệu lực'
    END AS trang_thai_hieu_luc
FROM khuyen_mai km
WHERE km.trang_thai = N'Đang diễn ra';

-- BƯỚC 2: Xem chi tiết sản phẩm trong khuyến mãi
SELECT 
    km.ma_khuyen_mai,
    km.ten_khuyen_mai,
    sp.ma_san_pham,
    sp.ten_san_pham,
    ctsp.id_chi_tiet_san_pham,
    ms.ten_mau_sac,
    kt.gia_tri AS kich_thuoc,
    ctsp.gia_ban AS gia_goc,
    ctkm.gia_sau_giam,
    (ctsp.gia_ban - ctkm.gia_sau_giam) AS so_tien_giam
FROM chi_tiet_khuyen_mai ctkm
JOIN khuyen_mai km ON ctkm.id_khuyen_mai = km.id_khuyen_mai
JOIN chi_tiet_san_pham ctsp ON ctkm.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
JOIN san_pham sp ON ctsp.id_san_pham = sp.id_san_pham
JOIN mau_sac ms ON ctsp.id_mau_sac = ms.id_mau_sac
JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id_kich_thuoc
WHERE km.trang_thai = N'Đang diễn ra'
AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
ORDER BY ctsp.id_chi_tiet_san_pham;

-- BƯỚC 3: Test subquery lấy giá khuyến mãi
SELECT 
    ctkm.id_chi_tiet_san_pham,
    MIN(ctkm.gia_sau_giam) AS giaHienTai,
    COUNT(*) AS so_khuyen_mai
FROM chi_tiet_khuyen_mai ctkm
JOIN khuyen_mai km ON ctkm.id_khuyen_mai = km.id_khuyen_mai
WHERE km.trang_thai = N'Đang diễn ra'
AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
GROUP BY ctkm.id_chi_tiet_san_pham;

-- BƯỚC 4: Test query chính (giống getAllCTSPKM)
SELECT 
    ctsp.id_chi_tiet_san_pham,
    sp.ma_san_pham,
    sp.ten_san_pham,
    dm.ten_danh_muc,
    ms.ten_mau_sac AS ten_mau,
    kt.gia_tri,
    ctsp.so_luong,
    ctsp.gia_ban AS gia_goc,
    km_max.giaHienTai AS gia_khuyen_mai,
    COALESCE(km_max.giaHienTai, ctsp.gia_ban) AS gia_ban,
    CASE 
        WHEN km_max.giaHienTai IS NOT NULL THEN N'✅ Có KM'
        ELSE N'❌ Không KM'
    END AS trang_thai_km,
    ctsp.trang_thai,
    sp.anh_dai_dien as hinh_anh
FROM chi_tiet_san_pham ctsp
JOIN san_pham sp ON ctsp.id_san_pham = sp.id_san_pham
JOIN danh_muc_san_pham dm ON sp.id_danh_muc = dm.id_danh_muc
JOIN mau_sac ms ON ctsp.id_mau_sac = ms.id_mau_sac
JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id_kich_thuoc
LEFT JOIN hinh_anh ha ON ctsp.id_chi_tiet_san_pham = ha.id_chi_tiet_san_pham AND ha.anh_chinh = 1
LEFT JOIN ( 
    SELECT
        ctkm.id_chi_tiet_san_pham,
        MIN(ctkm.gia_sau_giam) AS giaHienTai
    FROM chi_tiet_khuyen_mai ctkm
    JOIN khuyen_mai km ON ctkm.id_khuyen_mai = km.id_khuyen_mai
    WHERE km.trang_thai = N'Đang diễn ra'
    AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
    GROUP BY ctkm.id_chi_tiet_san_pham
) km_max ON ctsp.id_chi_tiet_san_pham = km_max.id_chi_tiet_san_pham
WHERE ctsp.trang_thai = 1
ORDER BY ctsp.id_chi_tiet_san_pham;

-- BƯỚC 5: Kiểm tra một sản phẩm cụ thể (thay ID bằng sản phẩm bạn test)
DECLARE @id_ctsp INT = 1;  -- ⚠️ Thay đổi ID này thành sản phẩm bạn đang test

SELECT 
    'Thông tin sản phẩm' AS loai_thong_tin,
    ctsp.id_chi_tiet_san_pham,
    sp.ten_san_pham,
    ctsp.gia_ban AS gia_goc,
    NULL AS gia_sau_giam,
    NULL AS ten_khuyen_mai
FROM chi_tiet_san_pham ctsp
JOIN san_pham sp ON ctsp.id_san_pham = sp.id_san_pham
WHERE ctsp.id_chi_tiet_san_pham = @id_ctsp

UNION ALL

SELECT 
    'Khuyến mãi đang áp dụng' AS loai_thong_tin,
    ctsp.id_chi_tiet_san_pham,
    sp.ten_san_pham,
    ctsp.gia_ban AS gia_goc,
    ctkm.gia_sau_giam,
    km.ten_khuyen_mai
FROM chi_tiet_san_pham ctsp
JOIN san_pham sp ON ctsp.id_san_pham = sp.id_san_pham
JOIN chi_tiet_khuyen_mai ctkm ON ctsp.id_chi_tiet_san_pham = ctkm.id_chi_tiet_san_pham
JOIN khuyen_mai km ON ctkm.id_khuyen_mai = km.id_khuyen_mai
WHERE ctsp.id_chi_tiet_san_pham = @id_ctsp
AND km.trang_thai = N'Đang diễn ra'
AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han;
