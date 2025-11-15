# QUICK START - Test Payment Integration

## Bước 1: Khởi động Backend
```bash
cd /home/huunghia/DATNFUll/duanbe
./mvnw spring-boot:run
```
Đợi đến khi thấy: `Started DuanbeApplication`

## Bước 2: Test API (Terminal mới)

### Test PayOS
```bash
curl -X POST "http://localhost:8080/api/payment/create-qr?idHoaDon=1&paymentMethod=payos"
```

### Test ZaloPay
```bash
curl -X POST "http://localhost:8080/api/payment/create-qr?idHoaDon=1&paymentMethod=zalopay"
```

## Bước 3: Khởi động Frontend
```bash
cd /home/huunghia/DATNFUll/DuAnMauFE
npm run dev
```

## Bước 4: Test trên trình duyệt

1. Mở: http://localhost:5173
2. Chọn sản phẩm → Thêm vào giỏ hàng
3. Vào giỏ hàng → Thanh toán
4. Điền thông tin giao hàng
5. Chọn: **"Thanh toán QR Code"**
6. Nhấn: **"Đặt hàng ngay"**
7. Modal hiện ra → Chọn PayOS hoặc ZaloPay
8. Quét QR code bằng app ngân hàng/ZaloPay
9. Xác nhận thanh toán
10. Hệ thống tự động phát hiện (3s) và thông báo thành công

## Kết Quả Mong Đợi

### ✅ Backend Log (ZaloPay):
```
ZaloPay Order Data: {"app_id":2553,"app_trans_id":"251116_1731711234567",...}
ZaloPay MAC: abc123...
ZaloPay Response: {"return_code":1,"order_url":"https://sb-openapi.zalopay.vn/v2/qr/..."}
```

### ✅ Frontend Console:
```
Payment Method: online-qr
Created Invoice ID: 123
QR URL: https://...
Payment Status: success
```

### ✅ Database:
```sql
-- Hóa đơn được tạo với ghi chú chứa transaction ID
SELECT * FROM hoa_don WHERE ghi_chu LIKE '%ZaloPay:%';

-- Sau khi thanh toán, trạng thái được cập nhật
UPDATE hoa_don SET trang_thai = 'Đã thanh toán' WHERE id_hoa_don = 123;
```

## Troubleshooting Nhanh

### Backend không khởi động?
```bash
# Kiểm tra port 8080 có bị chiếm không
lsof -i :8080

# Kill process nếu cần
kill -9 <PID>
```

### API trả về 404?
- Đợi backend khởi động hoàn tất (30-60s)
- Kiểm tra URL: `http://localhost:8080/api/payment/create-qr`

### Frontend không kết nối được backend?
- Kiểm tra backend đang chạy: `curl http://localhost:8080`
- Kiểm tra CORS đã được config trong controller

### ZaloPay trả về error?
- Xem backend log để debug
- Kiểm tra app_trans_id có unique không
- Verify MAC signature generation

## Test Checklist

- [ ] Backend compile thành công
- [ ] Backend khởi động không lỗi
- [ ] API PayOS trả về QR code
- [ ] API ZaloPay trả về QR code
- [ ] Frontend hiển thị modal
- [ ] Modal cho phép chọn phương thức
- [ ] QR code hiển thị chính xác
- [ ] Auto-check hoạt động (3s)
- [ ] Thanh toán thành công → DB cập nhật
- [ ] Modal đóng và redirect đúng

## Files Quan Trọng Cần Kiểm Tra

### Backend:
```
duanbe/src/main/java/com/example/duanbe/
  ├── controller/UnifiedPaymentController.java  ← API endpoints
  ├── service/ZaloPayService.java               ← ZaloPay logic
  └── config/ZaloPayConfig.java                 ← Credentials
```

### Frontend:
```
DuAnMauFE/src/
  ├── components/PaymentMethodModal.vue         ← Modal chọn phương thức
  └── components/ThanhToanDonHang-BanHang.vue  ← Trang thanh toán
```

## Support

Nếu gặp vấn đề:
1. Đọc `HUONG_DAN_TICH_HOP_THANH_TOAN_QR.md`
2. Xem `TEST_PAYMENT_API.md` để test riêng API
3. Check `TOMTAT_TICH_HOP_THANH_TOAN.md` để hiểu tổng quan

---
**Ready to test!** 🚀
