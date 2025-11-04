-- ===================================================================
-- CẬP NHẬT BẢNG KHÁCH HÀNG - TÍCH HỢP TÀI KHOẢN TRỰC TIẾP
-- ===================================================================

USE QLBanQuanAo;
GO

-- Bước 1: Kiểm tra cấu trúc bảng khach_hang hiện tại
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'khach_hang'
ORDER BY ORDINAL_POSITION;
GO

-- Bước 2: Đảm bảo các cột cần thiết đã tồn tại
-- (Bảng của bạn đã có đầy đủ, chỉ cần kiểm tra)

-- Kiểm tra xem cột ten_dang_nhap có tồn tại không
IF NOT EXISTS (
    SELECT 1 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'khach_hang' 
    AND COLUMN_NAME = 'ten_dang_nhap'
)
BEGIN
    ALTER TABLE khach_hang
    ADD ten_dang_nhap NVARCHAR(100) NOT NULL DEFAULT '';
    PRINT 'Đã thêm cột ten_dang_nhap';
END
ELSE
BEGIN
    PRINT 'Cột ten_dang_nhap đã tồn tại';
END
GO

-- Kiểm tra xem cột mat_khau có tồn tại không
IF NOT EXISTS (
    SELECT 1 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'khach_hang' 
    AND COLUMN_NAME = 'mat_khau'
)
BEGIN
    ALTER TABLE khach_hang
    ADD mat_khau NVARCHAR(255) NOT NULL DEFAULT '';
    PRINT 'Đã thêm cột mat_khau';
END
ELSE
BEGIN
    PRINT 'Cột mat_khau đã tồn tại';
END
GO

-- Bước 3: Đảm bảo các ràng buộc UNIQUE
IF NOT EXISTS (
    SELECT 1 
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE TABLE_NAME = 'khach_hang' 
    AND CONSTRAINT_NAME LIKE '%ten_dang_nhap%'
)
BEGIN
    ALTER TABLE khach_hang
    ADD CONSTRAINT UQ_khach_hang_ten_dang_nhap UNIQUE (ten_dang_nhap);
    PRINT 'Đã thêm ràng buộc UNIQUE cho ten_dang_nhap';
END
ELSE
BEGIN
    PRINT 'Ràng buộc UNIQUE cho ten_dang_nhap đã tồn tại';
END
GO

-- Bước 4: Kiểm tra dữ liệu hiện tại
PRINT '--- Kiểm tra dữ liệu khách hàng ---';
SELECT 
    id_khach_hang,
    ma_khach_hang,
    ten_dang_nhap,
    CASE 
        WHEN mat_khau IS NULL OR mat_khau = '' THEN 'Chưa có mật khẩu'
        ELSE 'Đã có mật khẩu'
    END AS trang_thai_mat_khau,
    email,
    ho_ten,
    trang_thai
FROM khach_hang
ORDER BY id_khach_hang DESC;
GO

-- Bước 5: CẬP NHẬT MẬT KHẨU MẶC ĐỊNH CHO KHÁCH HÀNG CHƯA CÓ
-- (Mật khẩu "12345678" đã được mã hóa bằng BCrypt)
DECLARE @defaultPassword NVARCHAR(255) = '$2a$10$EIXEhYWXvxLRmqNxZ8L8Xu3rZ7qY6WVZ5Hk8qYzqXg9LQZjqXzq.u';

UPDATE khach_hang
SET mat_khau = @defaultPassword
WHERE mat_khau IS NULL OR mat_khau = '' OR LEN(mat_khau) < 10;

PRINT 'Đã cập nhật mật khẩu mặc định cho các khách hàng chưa có mật khẩu';
GO

-- Bước 6: CẬP NHẬT TÊN ĐĂNG NHẬP = EMAIL (Nếu chưa có)
UPDATE khach_hang
SET ten_dang_nhap = email
WHERE ten_dang_nhap IS NULL OR ten_dang_nhap = '';

PRINT 'Đã cập nhật ten_dang_nhap = email cho các khách hàng';
GO

-- Bước 7: Kiểm tra kết quả sau khi cập nhật
PRINT '--- Kết quả sau khi cập nhật ---';
SELECT 
    id_khach_hang,
    ma_khach_hang,
    ten_dang_nhap,
    LEFT(mat_khau, 20) + '...' AS mat_khau_preview,
    email,
    ho_ten,
    trang_thai,
    ngay_lap
FROM khach_hang
ORDER BY id_khach_hang DESC;
GO

-- Bước 8: TẠO INDEX để tối ưu tìm kiếm
IF NOT EXISTS (
    SELECT 1 
    FROM sys.indexes 
    WHERE name = 'IX_khach_hang_ten_dang_nhap' 
    AND object_id = OBJECT_ID('khach_hang')
)
BEGIN
    CREATE INDEX IX_khach_hang_ten_dang_nhap 
    ON khach_hang(ten_dang_nhap);
    PRINT 'Đã tạo index cho ten_dang_nhap';
END
ELSE
BEGIN
    PRINT 'Index IX_khach_hang_ten_dang_nhap đã tồn tại';
END
GO

IF NOT EXISTS (
    SELECT 1 
    FROM sys.indexes 
    WHERE name = 'IX_khach_hang_email' 
    AND object_id = OBJECT_ID('khach_hang')
)
BEGIN
    CREATE INDEX IX_khach_hang_email 
    ON khach_hang(email);
    PRINT 'Đã tạo index cho email';
END
ELSE
BEGIN
    PRINT 'Index IX_khach_hang_email đã tồn tại';
END
GO

-- ===================================================================
-- OPTIONAL: XÓA BẢNG TAI_KHOAN NẾU KHÔNG DÙNG NỮA
-- ===================================================================

-- ⚠️ CHÚ Ý: Chỉ chạy phần này sau khi đã migrate hết dữ liệu và test kỹ

/*
-- Bước 1: Kiểm tra xem bảng tai_khoan có đang được sử dụng không
SELECT 
    fk.name AS 'Foreign Key Name',
    OBJECT_NAME(fk.parent_object_id) AS 'Referencing Table',
    COL_NAME(fkc.parent_object_id, fkc.parent_column_id) AS 'Referencing Column',
    OBJECT_NAME(fk.referenced_object_id) AS 'Referenced Table',
    COL_NAME(fkc.referenced_object_id, fkc.referenced_column_id) AS 'Referenced Column'
FROM sys.foreign_keys AS fk
INNER JOIN sys.foreign_key_columns AS fkc 
    ON fk.object_id = fkc.constraint_object_id
WHERE OBJECT_NAME(fk.referenced_object_id) = 'tai_khoan'
   OR OBJECT_NAME(fk.parent_object_id) = 'tai_khoan';

-- Bước 2: Xóa các ràng buộc Foreign Key liên quan đến tai_khoan
-- (Thay thế tên ràng buộc tương ứng trong database của bạn)
-- ALTER TABLE khach_hang DROP CONSTRAINT FK_khach_hang_tai_khoan;
-- ALTER TABLE nhan_vien DROP CONSTRAINT FK_nhan_vien_tai_khoan;

-- Bước 3: Xóa bảng tai_khoan
-- DROP TABLE tai_khoan;
-- PRINT 'Đã xóa bảng tai_khoan';
*/

-- ===================================================================
-- HOÀN TẤT
-- ===================================================================

PRINT '';
PRINT '=================================================================';
PRINT 'HOÀN TẤT CẬP NHẬT BẢNG KHÁCH HÀNG';
PRINT '=================================================================';
PRINT '';
PRINT 'Các thay đổi đã được áp dụng:';
PRINT '  ✓ Đảm bảo cột ten_dang_nhap và mat_khau tồn tại';
PRINT '  ✓ Thêm ràng buộc UNIQUE cho ten_dang_nhap';
PRINT '  ✓ Cập nhật mật khẩu mặc định cho khách hàng';
PRINT '  ✓ Cập nhật ten_dang_nhap = email';
PRINT '  ✓ Tạo index để tối ưu hiệu suất';
PRINT '';
PRINT 'Bước tiếp theo:';
PRINT '  1. Kiểm tra dữ liệu trong bảng khach_hang';
PRINT '  2. Test đăng nhập/đăng ký với logic mới';
PRINT '  3. Backup database trước khi áp dụng lên production';
PRINT '  4. Sau khi test kỹ, có thể xóa bảng tai_khoan (nếu không dùng)';
PRINT '=================================================================';
GO
