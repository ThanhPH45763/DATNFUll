# Hướng dẫn test sửa lỗi phí vận chuyển ZaloPay

## Vấn đề
Tiền ship không được tính trong hóa đơn ZaloPay khi chuyển link thanh toán.

## Nguyên nhân
1. Frontend gọi ZaloPay mà không đảm bảo `phi_van_chuyen` đã được lưu vào DB
2. Không có validation để kiểm tra phí ship trước khi tạo ZaloPay order
3. `phi_van_chuyen` có thể bị NULL trong database

## Giải pháp đã thực hiện

### 1. Thêm debug log (ZaloPayController.java)
- In ra toàn bộ thông tin hóa đơn khi tạo ZaloPay order
- Kiểm tra giá trị `phi_van_chuyen` và `tong_tien_sau_giam`

### 2. Thêm validation Frontend (TheHeader-BanHang.vue)
- Kiểm tra phí vận chuyển trước khi gọi ZaloPay
- Yêu cầu tính phí ship cho đơn "Giao hàng"
- Cho phép phí ship = 0 cho đơn "Nhận tại cửa hàng"

### 3. Thêm validation Backend (ZaloPayController.java)
- Kiểm tra `phi_van_chuyen` trước khi tạo order
- Block thanh toán nếu phí ship = 0 cho đơn giao hàng

## Các bước test

### Test Case 1: Đơn giao hàng chưa tính phí ship
1. Tạo hóa đơn mới
2. Thêm sản phẩm
3. Chọn "Giao hàng" nhưng không chọn địa chỉ
4. Chọn ZaloPay thanh toán
5. **Expected**: Hiển thị lỗi "Phí vận chuyển chưa được tính"

### Test Case 2: Đơn giao hàng đã tính phí ship
1. Tạo hóa đơn mới
2. Thêm sản phẩm
3. Chọn "Giao hàng" và điền đầy đủ địa chỉ
4. Chờ hệ thống tính phí ship
5. Chọn ZaloPay thanh toán
6. **Expected**: ZaloPay mở với tổng tiền = tiền sản phẩm + phí ship

### Test Case 3: Đơn nhận tại cửa hàng
1. Tạo hóa đơn mới
2. Thêm sản phẩm
3. Chọn "Nhận tại cửa hàng"
4. Chọn ZaloPay thanh toán
5. **Expected**: ZaloPay mở với tổng tiền = tiền sản phẩm (không có phí ship)

### Test Case 4: Kiểm tra log
1. Kiểm tra console backend khi tạo ZaloPay order
2. **Expected**: Thấy log thông tin chi tiết hóa đơn và phí vận chuyển

## Lệnh kiểm tra database
```sql
-- Kiểm tra các hóa đơn ZaloPay gần đây
SELECT TOP 10
    hd.id_hoa_don,
    hd.ma_hoa_don,
    hd.ngay_tao,
    hd.trang_thai,
    hd.hinh_thuc_thanh_toan,
    hd.tong_tien_truoc_giam,
    hd.tong_tien_sau_giam,
    hd.phi_van_chuyen,
    hd.ghi_chu
FROM hoa_don hd
WHERE hd.ngay_tao >= DATEADD(DAY, -7, GETDATE())
    AND (hd.hinh_thuc_thanh_toan LIKE '%Chuyển khoản%' OR hd.ghi_chu LIKE '%ZaloPay%')
ORDER BY hd.ngay_tao DESC;
```

## Notes
- Frontend validation sẽ chặn user trước khi gọi API
- Backend validation là lớp bảo vệ cuối cùng
- Debug log giúp dễ dàng troubleshoot trong tương lai
