# Hướng Dẫn Tích Hợp Thanh Toán PayOS và ZaloPay

## Tổng Quan
Dự án đã được tích hợp hệ thống thanh toán thống nhất cho phép khách hàng chọn giữa PayOS hoặc ZaloPay khi thanh toán online. QR code sẽ hiển thị ngay trên trang web, không cần chuyển sang trang mới.

## Tính Năng Mới

### 1. **Unified Payment Controller** (Backend)
- **File**: `/duanbe/src/main/java/com/example/duanbe/controller/UnifiedPaymentController.java`
- **Endpoints**:
  - `POST /api/payment/create-qr` - Tạo mã QR cho PayOS hoặc ZaloPay
    - Params: `idHoaDon`, `paymentMethod` (payos/zalopay)
  - `GET /api/payment/check-status` - Kiểm tra trạng thái thanh toán
    - Params: `idHoaDon`, `paymentMethod`

### 2. **Payment Method Modal** (Frontend)
- **File**: `/DuAnMauFE/src/components/PaymentMethodModal.vue`
- **Chức năng**:
  - Bước 1: Cho phép chọn PayOS hoặc ZaloPay
  - Bước 2: Hiển thị QR code để quét
  - Auto-check trạng thái thanh toán mỗi 3 giây

### 3. **Cập Nhật Trang Thanh Toán**
- **File**: `/DuAnMauFE/src/components/ThanhToanDonHang-BanHang.vue`
- Thêm phương thức thanh toán mới: "Thanh toán QR Code"
- Tích hợp modal chọn phương thức

## Cách Sử Dụng

### Cho Khách Hàng:
1. Chọn sản phẩm và vào trang thanh toán
2. Điền thông tin giao hàng
3. Chọn "Thanh toán QR Code"
4. Nhấn "Đặt hàng ngay"
5. Modal hiện ra với 2 lựa chọn:
   - **PayOS**: Thanh toán qua VietQR (hỗ trợ tất cả ngân hàng)
   - **ZaloPay**: Thanh toán qua ví ZaloPay
6. Chọn phương thức và quét mã QR
7. Hệ thống tự động kiểm tra và xác nhận khi thanh toán thành công

### Flow Hoạt Động:

```
[Khách hàng đặt hàng] 
    ↓
[Tạo hóa đơn trong DB]
    ↓
[Hiển thị modal chọn phương thức]
    ↓
[Gọi API tạo QR code] → /api/payment/create-qr
    ↓
[Hiển thị QR code]
    ↓
[Auto-check trạng thái] → /api/payment/check-status (mỗi 3s)
    ↓
[Thanh toán thành công] → Cập nhật DB
```

## Cấu Hình ZaloPay

### 1. Credentials (đã cấu hình sẵn)
```java
// File: ZaloPayConfig.java
APP_ID = "2553"
KEY1 = "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL"
KEY2 = "kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz"
```

### 2. Endpoints
- **Sandbox**: `https://sb-openapi.zalopay.vn/v2/create`
- **Query**: `https://sb-openapi.zalopay.vn/v2/query`

### 3. Sửa Lỗi ZaloPay Không Tạo Được QR

**Lỗi thường gặp**:
- app_trans_id trùng lặp
- MAC signature không đúng
- Thiếu trường bắt buộc

**Đã sửa**:
```java
// Tạo app_trans_id unique
String appTransId = getCurrentTimeString("yyMMdd") + "_" + System.currentTimeMillis();

// Đảm bảo app_id là integer
order.put("app_id", Integer.parseInt(ZaloPayConfig.APP_ID));

// Log để debug
System.out.println("ZaloPay Order Data: " + gson.toJson(order));
System.out.println("ZaloPay MAC: " + mac);
```

### 4. Test ZaloPay Sandbox

```bash
# Gọi API tạo QR
curl -X POST "http://localhost:8080/api/payment/create-qr?idHoaDon=1&paymentMethod=zalopay"

# Kiểm tra response
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

## Cấu Hình PayOS

### Credentials (đã có sẵn)
```properties
PAYOS_CLIENT_ID=30965015-9adc-4cb9-8afc-073995fe805c
PAYOS_API_KEY=82ad6f69-754c-4f45-85c8-da89f8423973
PAYOS_CHECKSUM_KEY=988c02f4c4ab53b04f91c8b9fdbebe860ab12f78b4ec905cc797f1bf44752801
```

## Kiểm Tra Và Debug

### 1. Kiểm tra backend đang chạy
```bash
curl http://localhost:8080/api/payment/create-qr?idHoaDon=1&paymentMethod=payos
```

### 2. Xem log ZaloPay
```bash
# Backend sẽ log:
- ZaloPay Order Data: {...}
- ZaloPay MAC String: ...
- ZaloPay MAC: ...
- ZaloPay Response: {...}
```

### 3. Kiểm tra trong Database
```sql
-- Kiểm tra hóa đơn đã tạo
SELECT * FROM hoa_don WHERE ghi_chu LIKE '%ZaloPay:%' OR ghi_chu LIKE '%PayOS:%'

-- Kiểm tra trạng thái thanh toán
SELECT ma_hoa_don, trang_thai, hinh_thuc_thanh_toan, ghi_chu 
FROM hoa_don 
WHERE trang_thai = 'Đã thanh toán'
```

## API Documentation

### 1. Create QR Code
```
POST /api/payment/create-qr
Params:
  - idHoaDon: Integer (required)
  - paymentMethod: String (payos|zalopay)

Response (Success):
{
  "error": false,
  "paymentMethod": "zalopay",
  "qrUrl": "https://...",
  "orderUrl": "https://...",
  "amount": 500000,
  "message": "Tạo mã QR thành công"
}

Response (Error):
{
  "error": true,
  "message": "Error message"
}
```

### 2. Check Payment Status
```
GET /api/payment/check-status
Params:
  - idHoaDon: Integer (required)
  - paymentMethod: String (payos|zalopay)

Response:
{
  "error": false,
  "status": "success|pending|cancelled",
  "message": "Thanh toán thành công"
}
```

## Xử Lý Lỗi ZaloPay

### Lỗi: "return_code: 2, subreturn_code: -16"
**Nguyên nhân**: app_trans_id không đúng format hoặc trùng lặp
**Giải pháp**: 
```java
// Đã sửa - sử dụng timestamp để tạo ID unique
String appTransId = getCurrentTimeString("yyMMdd") + "_" + System.currentTimeMillis();
```

### Lỗi: "return_code: 2, subreturn_code: -1"
**Nguyên nhân**: MAC signature không đúng
**Giải pháp**:
```java
// Đảm bảo thứ tự các trường đúng
String data = app_id + "|" + app_trans_id + "|" + app_user + "|" + amount + "|" + app_time + "|" + embed_data + "|" + item;
```

### Lỗi: "return_code: 2, subreturn_code: -4"
**Nguyên nhân**: Thiếu trường bắt buộc hoặc sai kiểu dữ liệu
**Giải pháp**:
```java
// app_id phải là integer, không phải string
order.put("app_id", Integer.parseInt(ZaloPayConfig.APP_ID));
```

## Test Scenarios

### 1. Test PayOS
1. Chọn "Thanh toán QR Code"
2. Chọn PayOS
3. QR code hiển thị
4. Quét bằng app ngân hàng
5. Thanh toán thành công → Modal đóng, chuyển về trang chủ

### 2. Test ZaloPay
1. Chọn "Thanh toán QR Code"
2. Chọn ZaloPay
3. QR code hiển thị
4. Quét bằng app ZaloPay
5. Thanh toán thành công → Modal đóng, chuyển về trang chủ

### 3. Test Auto-check
1. Mở modal QR
2. Không thanh toán
3. Quan sát console: API check-status gọi mỗi 3 giây
4. Thanh toán → Sau 3 giây hệ thống tự phát hiện

## Troubleshooting

### Frontend không hiển thị modal
```bash
# Kiểm tra import
import PaymentMethodModal from './PaymentMethodModal.vue';

# Kiểm tra components đã register
components: { PaymentMethodModal }
```

### Backend trả về lỗi CORS
```java
@CrossOrigin(origins = "*") // Đã thêm vào controller
```

### QR code không load
```javascript
// Kiểm tra response
console.log('QR URL:', response.data.qrUrl);

// Đảm bảo URL hợp lệ
<img :src="qrCodeUrl" />
```

## Next Steps

### 1. Nâng Cấp Production
- Thay đổi từ Sandbox sang Production credentials
- Cấu hình webhook callback cho realtime updates
- Thêm retry logic cho failed payments

### 2. Bảo Mật
- Xác thực MAC signature trong callback
- Validate payment amount
- Log tất cả transactions

### 3. UX Improvements
- Thêm timer đếm ngược
- Hiển thị lịch sử giao dịch
- Email confirmation khi thanh toán thành công

## Liên Hệ Hỗ Trợ

Nếu gặp vấn đề:
1. Kiểm tra log backend
2. Kiểm tra console frontend
3. Test API bằng Postman/curl
4. Xem ZaloPay documentation: https://docs.zalopay.vn/

---
**Cập nhật**: 16/11/2024
**Version**: 1.0.0
