-- ============================================================
-- KIỂM TRA NGAY - SẢN PHẨM BỊ DUPLICATE TRONG DATABASE
-- ============================================================

-- 1. Kiểm tra sản phẩm bị trùng trong hóa đơn hiện tại
SELECT 
    hdct.id_hoa_don_chi_tiet,
    hdct.id_hoa_don,
    hdct.id_chi_tiet_san_pham,
    sp.ten_san_pham,
    hdct.so_luong,
    hdct.don_gia
FROM hoa_don_chi_tiet hdct
JOIN chi_tiet_san_pham ctsp ON ctsp.id_chi_tiet_san_pham = hdct.id_chi_tiet_san_pham
JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
WHERE hdct.id_chi_tiet_san_pham = 1  -- ID sản phẩm "Áo sơ mi trắng"
ORDER BY hdct.id_hoa_don_chi_tiet;

-- 2. Đếm số lần xuất hiện
SELECT 
    id_hoa_don,
    id_chi_tiet_san_pham,
    COUNT(*) as so_lan_xuat_hien
FROM hoa_don_chi_tiet
WHERE id_chi_tiet_san_pham = 1
GROUP BY id_hoa_don, id_chi_tiet_san_pham
HAVING COUNT(*) > 1;

-- 3. Xem toàn bộ chi tiết hóa đơn vừa tạo (thay @idHoaDon)
DECLARE @idHoaDon INT = 1;  -- Thay bằng ID hóa đơn bạn vừa test

SELECT 
    hdct.id_hoa_don_chi_tiet,
    hdct.id_chi_tiet_san_pham,
    sp.ten_san_pham,
    hdct.so_luong,
    hdct.don_gia
FROM hoa_don_chi_tiet hdct
JOIN chi_tiet_san_pham ctsp ON ctsp.id_chi_tiet_san_pham = hdct.id_chi_tiet_san_pham
JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
WHERE hdct.id_hoa_don = @idHoaDon;

-- ============================================================
-- KẾT QUẢ MONG ĐỢI:
-- - Query 1: Nếu có 2 dòng giống nhau → Database bị duplicate
-- - Query 2: Nếu trả về kết quả → Có sản phẩm bị trùng
-- - Query 3: Xem chi tiết hóa đơn
-- ============================================================
