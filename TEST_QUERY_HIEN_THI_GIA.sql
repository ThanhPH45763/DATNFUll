-- =====================================================
-- SCRIPT TEST: HIỂN THỊ GIÁ SẢN PHẨM VỚI KHUYẾN MÃI
-- =====================================================

USE QLBanQuanAo;
GO

-- =====================================================
-- BƯỚC 1: XEM DỮ LIỆU HIỆN TẠI
-- =====================================================

-- 1.1. Danh sách khuyến mãi đang hiệu lực
SELECT 
    km.ma_khuyen_mai,
    km.ten_khuyen_mai,
    km.kieu_giam_gia,
    km.gia_tri_giam,
    km.gia_tri_toi_da,
    km.ngay_bat_dau,
    km.ngay_het_han,
    km.trang_thai,
    CASE 
        WHEN GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han 
        THEN N'✅ Đang hiệu lực'
        ELSE N'❌ Hết hạn'
    END AS hieu_luc
FROM khuyen_mai km
ORDER BY km.ngay_bat_dau DESC;

-- 1.2. Chi tiết sản phẩm có khuyến mãi
SELECT 
    sp.ten_san_pham,
    ctsp.gia_ban AS gia_goc,
    km.kieu_giam_gia,
    km.gia_tri_giam,
    CASE 
        WHEN km.kieu_giam_gia = N'Phần trăm' 
        THEN ctsp.gia_ban * (1 - km.gia_tri_giam / 100)
        WHEN km.kieu_giam_gia = N'Tiền mặt' 
        THEN ctsp.gia_ban - km.gia_tri_giam
        ELSE ctsp.gia_ban
    END AS gia_sau_giam,
    CASE 
        WHEN km.kieu_giam_gia = N'Phần trăm' 
        THEN CAST((ctsp.gia_ban - ctsp.gia_ban * (1 - km.gia_tri_giam / 100)) / ctsp.gia_ban * 100 AS INT)
        WHEN km.kieu_giam_gia = N'Tiền mặt' 
        THEN CAST((km.gia_tri_giam / ctsp.gia_ban * 100) AS INT)
        ELSE 0
    END AS phan_tram_giam
FROM chi_tiet_san_pham ctsp
JOIN san_pham sp ON ctsp.id_san_pham = sp.id_san_pham
JOIN chi_tiet_khuyen_mai ctkm ON ctsp.id_chi_tiet_san_pham = ctkm.id_chi_tiet_san_pham
JOIN khuyen_mai km ON ctkm.id_khuyen_mai = km.id_khuyen_mai
WHERE GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
    AND km.trang_thai = N'Đang diễn ra';

-- =====================================================
-- BƯỚC 2: TEST QUERY CHÍNH (TỪ SanPhamRepo.java)
-- =====================================================

DECLARE @tenDanhMuc NVARCHAR(100) = N'Áo'; -- Thay đổi tên danh mục để test

WITH KhuyenMaiHieuLuc AS (
    SELECT 
        ctkm.id_chi_tiet_san_pham,
        GiamGia = CASE 
            WHEN km.kieu_giam_gia = N'Phần trăm' THEN 
                CASE 
                    WHEN ctsp.gia_ban * km.gia_tri_giam / 100 > ISNULL(km.gia_tri_toi_da, 999999999)
                        THEN ctsp.gia_ban - km.gia_tri_toi_da
                    ELSE ctsp.gia_ban * (1 - km.gia_tri_giam / 100)
                END
            WHEN km.kieu_giam_gia = N'Tiền mặt' THEN ctsp.gia_ban - km.gia_tri_giam
            ELSE ctsp.gia_ban
        END
    FROM chi_tiet_khuyen_mai ctkm
    JOIN khuyen_mai km 
        ON ctkm.id_khuyen_mai = km.id_khuyen_mai
        AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
        AND km.trang_thai = N'Đang diễn ra'
    JOIN chi_tiet_san_pham ctsp 
        ON ctkm.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
),
GiaTotNhat AS (
    SELECT
        ctsp.id_san_pham,
        GiaGiamMin = MIN(ISNULL(kh.GiamGia, ctsp.gia_ban)),
        GiaGiamMax = MAX(ISNULL(kh.GiamGia, ctsp.gia_ban))
    FROM chi_tiet_san_pham ctsp
    LEFT JOIN KhuyenMaiHieuLuc kh 
        ON ctsp.id_chi_tiet_san_pham = kh.id_chi_tiet_san_pham
    GROUP BY ctsp.id_san_pham
)
SELECT DISTINCT
    sp.id_san_pham,
    sp.ten_san_pham,
    dm.ten_danh_muc,
    SUM(ctsp.so_luong) OVER (PARTITION BY sp.id_san_pham) AS tong_so_luong,
    MAX(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham) AS gia_max,
    MIN(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham) AS gia_min,
    COALESCE(gt.GiaGiamMin, MIN(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham)) AS gia_tot_nhat,
    COALESCE(gt.GiaGiamMax, MAX(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham)) AS gia_khuyen_mai_cao_nhat,
    -- Logic tính toán hiển thị
    CASE 
        WHEN gt.GiaGiamMin < MIN(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham)
        THEN N'✅ CÓ KHUYẾN MÃI'
        ELSE N'❌ KHÔNG KM'
    END AS trang_thai_km,
    CASE 
        WHEN gt.GiaGiamMin < MIN(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham)
        THEN CAST((MIN(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham) - gt.GiaGiamMin) 
                  / MIN(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham) * 100 AS INT)
        ELSE 0
    END AS phan_tram_giam
FROM san_pham sp
INNER JOIN danh_muc_san_pham dm 
    ON sp.id_danh_muc = dm.id_danh_muc
INNER JOIN thuong_hieu th 
    ON sp.id_thuong_hieu = th.id_thuong_hieu
INNER JOIN chat_lieu cl 
    ON sp.id_chat_lieu = cl.id_chat_lieu
INNER JOIN chi_tiet_san_pham ctsp 
    ON sp.id_san_pham = ctsp.id_san_pham
LEFT JOIN GiaTotNhat gt 
    ON sp.id_san_pham = gt.id_san_pham
WHERE 
    sp.trang_thai = N'Hoạt động'
    AND EXISTS (SELECT 1
               FROM STRING_SPLIT(@tenDanhMuc, ',') AS kw
               WHERE dm.ten_danh_muc LIKE '%' + LTRIM(RTRIM(kw.value)) + '%')
ORDER BY sp.id_san_pham;

-- =====================================================
-- BƯỚC 3: TẠO DỮ LIỆU TEST (NẾU CHƯA CÓ)
-- =====================================================

-- 3.1. Tạo khuyến mãi test
IF NOT EXISTS (SELECT 1 FROM khuyen_mai WHERE ma_khuyen_mai = 'KM_TEST_001')
BEGIN
    INSERT INTO khuyen_mai (ma_khuyen_mai, ten_khuyen_mai, mo_ta, ngay_bat_dau, ngay_het_han, 
                           gia_tri_giam, gia_tri_toi_da, kieu_giam_gia, trang_thai)
    VALUES (
        'KM_TEST_001',
        N'Khuyến mãi test giảm 30%',
        N'Khuyến mãi để test hiển thị giá',
        DATEADD(DAY, -7, GETDATE()),  -- Bắt đầu từ 7 ngày trước
        DATEADD(DAY, 30, GETDATE()),  -- Hết hạn sau 30 ngày
        30.00,                         -- Giảm 30%
        100000.00,                     -- Tối đa 100k
        N'Phần trăm',
        N'Đang diễn ra'
    );
    
    PRINT N'✅ Đã tạo khuyến mãi test: KM_TEST_001';
END
ELSE
BEGIN
    PRINT N'⚠️ Khuyến mãi KM_TEST_001 đã tồn tại';
END

-- 3.2. Áp dụng khuyến mãi cho một số chi tiết sản phẩm
-- (Chỉ áp dụng nếu chưa có)
DECLARE @id_km INT;
SELECT @id_km = id_khuyen_mai FROM khuyen_mai WHERE ma_khuyen_mai = 'KM_TEST_001';

IF @id_km IS NOT NULL
BEGIN
    -- Áp dụng cho 5 chi tiết sản phẩm đầu tiên
    INSERT INTO chi_tiet_khuyen_mai (id_khuyen_mai, id_chi_tiet_san_pham)
    SELECT TOP 5 @id_km, ctsp.id_chi_tiet_san_pham
    FROM chi_tiet_san_pham ctsp
    WHERE NOT EXISTS (
        SELECT 1 FROM chi_tiet_khuyen_mai 
        WHERE id_khuyen_mai = @id_km 
        AND id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
    );
    
    PRINT N'✅ Đã áp dụng khuyến mãi cho sản phẩm test';
END

-- =====================================================
-- BƯỚC 4: KIỂM TRA KẾT QUẢ
-- =====================================================

-- 4.1. So sánh sản phẩm có KM vs không KM
SELECT 
    sp.ten_san_pham,
    ctsp.gia_ban,
    CASE 
        WHEN ctkm.id_ctkm IS NOT NULL THEN N'✅ Có KM'
        ELSE N'❌ Không KM'
    END AS co_khuyen_mai,
    km.gia_tri_giam,
    km.kieu_giam_gia
FROM chi_tiet_san_pham ctsp
JOIN san_pham sp ON ctsp.id_san_pham = sp.id_san_pham
LEFT JOIN chi_tiet_khuyen_mai ctkm ON ctsp.id_chi_tiet_san_pham = ctkm.id_chi_tiet_san_pham
LEFT JOIN khuyen_mai km ON ctkm.id_khuyen_mai = km.id_khuyen_mai
    AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
WHERE sp.trang_thai = N'Hoạt động';

-- =====================================================
-- BƯỚC 5: XÓA DỮ LIỆU TEST (NẾU CẦN)
-- =====================================================

/*
-- Uncomment để xóa dữ liệu test
DELETE FROM chi_tiet_khuyen_mai 
WHERE id_khuyen_mai IN (SELECT id_khuyen_mai FROM khuyen_mai WHERE ma_khuyen_mai = 'KM_TEST_001');

DELETE FROM khuyen_mai WHERE ma_khuyen_mai = 'KM_TEST_001';

PRINT N'✅ Đã xóa dữ liệu test';
*/

PRINT N'';
PRINT N'=====================================================';
PRINT N'✅ HOÀN TẤT TEST SCRIPT';
PRINT N'=====================================================';
