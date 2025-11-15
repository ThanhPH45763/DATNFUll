# Test Payment API

## Test với hóa đơn ID = 1

### 1. Test tạo QR PayOS
```bash
curl -X POST "http://localhost:8080/api/payment/create-qr?idHoaDon=1&paymentMethod=payos"
```

### 2. Test tạo QR ZaloPay
```bash
curl -X POST "http://localhost:8080/api/payment/create-qr?idHoaDon=1&paymentMethod=zalopay"
```

### 3. Test check status PayOS
```bash
curl "http://localhost:8080/api/payment/check-status?idHoaDon=1&paymentMethod=payos"
```

### 4. Test check status ZaloPay
```bash
curl "http://localhost:8080/api/payment/check-status?idHoaDon=1&paymentMethod=zalopay"
```

## Expected Responses

### PayOS Create QR - Success
```json
{
  "error": false,
  "paymentMethod": "payos",
  "checkoutUrl": "https://pay.payos.vn/web/...",
  "qrUrl": "data:image/png;base64,...",
  "orderCode": 1731711234567,
  "amount": 500000,
  "message": "Tạo mã QR PayOS thành công"
}
```

### ZaloPay Create QR - Success
```json
{
  "error": false,
  "paymentMethod": "zalopay",
  "qrUrl": "https://sb-openapi.zalopay.vn/v2/qr/...",
  "orderUrl": "https://sb-openapi.zalopay.vn/v2/qr/...",
  "appTransId": "251116_1731711234567",
  "amount": 500000,
  "message": "Tạo mã QR ZaloPay thành công"
}
```

### Check Status - Pending
```json
{
  "error": false,
  "status": "pending",
  "message": "Chưa thanh toán"
}
```

### Check Status - Success
```json
{
  "error": false,
  "status": "success",
  "message": "Thanh toán thành công"
}
```

## Common Errors

### Error: Không tìm thấy hóa đơn
```json
{
  "error": true,
  "message": "Không tìm thấy hóa đơn"
}
```
**Fix**: Tạo hóa đơn trước hoặc thay đổi idHoaDon

### Error: Phương thức thanh toán không hợp lệ
```json
{
  "error": true,
  "message": "Phương thức thanh toán không hợp lệ. Vui lòng chọn 'payos' hoặc 'zalopay'"
}
```
**Fix**: Sử dụng paymentMethod=payos hoặc paymentMethod=zalopay

### ZaloPay Error: return_code 2
```json
{
  "return_code": 2,
  "return_message": "...",
  "sub_return_code": -16
}
```
**Fix**: Xem log backend để debug MAC signature hoặc app_trans_id
