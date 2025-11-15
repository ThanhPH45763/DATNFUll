# Sửa Lỗi Không Gen Được QR Code ZaloPay

## Vấn đề
ZaloPay trả về `qr_code` là **chuỗi EMV QR** (dạng text), không phải URL hình ảnh.
Code cũ đang dùng `order_url` (deep link) thay vì `qr_code`.

## Nguyên nhân
```javascript
// ❌ SAI - order_url là deep link, không phải QR image
zaloPayQRUrl.value = result.order_url;

// ✅ ĐÚNG - Cần convert qr_code string thành image
const qrDataUrl = await QRCode.toDataURL(result.qr_code);
```

## Giải pháp đã áp dụng

### 1. Frontend - TheHeader-BanHang.vue

#### Thêm biến lưu QR code string:
```javascript
const zaloPayQRCode = ref(''); // QR code string từ ZaloPay
```

#### Sửa hàm showZaloPayQR:
```javascript
const showZaloPayQR = async () => {
    try {
        const result = await store.createZaloPayOrder(idHoaDon);
        
        if (result.return_code === 1 && result.qr_code) {
            // Convert QR code string sang image data URL
            const qrDataUrl = await QRCode.toDataURL(result.qr_code, {
                width: 300,
                margin: 2
            });
            zaloPayQRUrl.value = qrDataUrl; // Lưu image URL
            zaloPayQRCode.value = result.qr_code; // Lưu QR string gốc
            
            showZaloPayModal.value = true;
            paymentStatus.value = 'checking';
            startCheckingPaymentStatus();
        }
    } catch (error) {
        message.error('Lỗi tạo QR: ' + error.message);
    }
};
```

### 2. Backend - ZaloPayController.java

Đã sửa validation và error handling:
```java
// Kiểm tra số tiền hợp lệ
if (tongTien == null || tongTien.compareTo(BigDecimal.ZERO) <= 0) {
    return ResponseEntity.badRequest().body(Map.of(
        "return_code", -1,
        "return_message", "Số tiền thanh toán không hợp lệ"
    ));
}

// Bọc try-catch khi lưu DB
try {
    hoaDonRepo.save(hoaDon);
} catch (Exception saveEx) {
    System.err.println("Lỗi khi lưu: " + saveEx.getMessage());
}
```

### 3. Store - gbStore.js

Sửa API call đúng cú pháp:
```javascript
// ✅ ĐÚNG
const response = await axiosInstance.post('api/zalopay/create-order', null, {
    params: { idHoaDon: invoiceId }
});

// ❌ SAI (code cũ)
// const response = await axiosInstance.post(
//     'http://localhost:8080/api/zalopay/create-order?idHoaDon=', 
//     invoiceId
// );
```

## Cách Test

1. **Restart Frontend** (Vite dev server):
   ```bash
   # Ctrl+C trong terminal đang chạy
   npm run dev
   ```

2. **Kiểm tra trong trình duyệt**:
   - Mở Console (F12)
   - Tạo/chọn hóa đơn
   - Nhấn nút thanh toán ZaloPay
   - Xem log: `Tạo QR ZaloPay cho hóa đơn ID: ...`
   - Xem log: `ZaloPay Response: ...` (phải có `return_code: 1` và `qr_code`)
   - Modal hiện lên với QR code hình ảnh

3. **Kiểm tra QR Code**:
   - Mở app ZaloPay trên điện thoại
   - Quét mã QR trên màn hình
   - Xác nhận thanh toán

## Response mẫu từ ZaloPay

```json
{
    "return_code": 1,
    "return_message": "Giao dịch thành công",
    "sub_return_code": 1,
    "sub_return_message": "Giao dịch thành công",
    "zp_trans_token": "ACd-I0lEHIZEbgeHQvRrnjuA",
    "order_url": "https://qcgateway.zalopay.vn/openinapp?order=...",
    "order_token": "ACd-I0lEHIZEbgeHQvRrnjuA",
    "qr_code": "00020101021226530010vn.zalopay..." 
}
```

## Flow hoàn chỉnh

1. User nhấn nút "Thanh toán ZaloPay"
2. Frontend gọi `store.createZaloPayOrder(idHoaDon)`
3. Backend gọi ZaloPay API `/create` với thông tin hóa đơn
4. ZaloPay trả về response chứa `qr_code` (string)
5. Frontend dùng thư viện `qrcode` convert string → image data URL
6. Hiển thị QR code trong modal
7. User quét QR bằng app ZaloPay
8. Frontend poll API `/check-status` mỗi 3s
9. Khi thanh toán thành công, đóng modal và cập nhật hóa đơn

## Lưu ý

- ZaloPay sandbox có thể không hoạt động nếu chưa đăng ký merchant
- Production cần đăng ký và cấu hình thông tin merchant thật
- QR code có thời gian hết hạn (thường 15 phút)
