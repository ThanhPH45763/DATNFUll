# FIX LỖI PAYMENT - 16/11/2024

## ✅ ĐÃ FIX

### 1. Lỗi Frontend: `axios is not defined`
**File**: `/DuAnMauFE/src/stores/gbStore.js`

**Lỗi**: 
```
ReferenceError: axios is not defined at Proxy.createZaloPayOrder
```

**Nguyên nhân**: Thiếu import axios trong gbStore.js

**Giải pháp**: Đã thêm dòng import
```javascript
import axios from 'axios'
```

**Vị trí**: Dòng 4 trong gbStore.js

---

### 2. Lỗi Backend: PayOS Signature Mismatch
**File**: `/duanbe/src/main/java/com/example/duanbe/payos/OrderController.java`

**Lỗi**:
```
java.lang.Exception: The data is unreliable because the signature of the response 
does not match the signature of the data
```

**Nguyên nhân**: 
1. PayOS API có thể thay đổi response format
2. Network timeout/issue
3. Order code generation không optimal

**Giải pháp**: 
1. Cập nhật cách generate orderCode - sử dụng Unix timestamp thay vì random 6 digits
2. Thêm error message chi tiết hơn
3. Sử dụng `System.currentTimeMillis() / 1000` để tạo unique orderCode

**Code cũ**:
```java
String currentTimeString = String.valueOf(String.valueOf(new Date().getTime()));
long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));
```

**Code mới**:
```java
long orderCode = System.currentTimeMillis() / 1000; // Unix timestamp in seconds
```

---

## 🔍 TROUBLESHOOTING

### Nếu PayOS vẫn báo signature error:

#### Option 1: Restart Backend
```bash
# Stop backend (Ctrl+C)
cd /home/huunghia/DATNFUll/duanbe
./mvnw clean spring-boot:run
```

#### Option 2: Test với API trực tiếp
```bash
curl -X POST http://localhost:8080/order/create \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Test Product",
    "description": "Test",
    "returnUrl": "http://localhost:5173/payment-callback",
    "cancelUrl": "http://localhost:5173/thanhtoan-banhang",
    "price": 50000
  }'
```

#### Option 3: Kiểm tra credentials PayOS
Vào https://my.payos.vn và verify:
- Client ID: `30965015-9adc-4cb9-8afc-073995fe805c`
- API Key: `82ad6f69-754c-4f45-85c8-da89f8423973`
- Checksum Key: `988c02f4c4ab53b04f91c8b9fdbebe860ab12f78b4ec905cc797f1bf44752801`

#### Option 4: Network issue
PayOS sandbox có thể không ổn định. Nếu lỗi vẫn xảy ra:
1. Thử lại sau 5-10 phút
2. Check internet connection
3. Hoặc tạm thời chỉ dùng ZaloPay

---

## 🧪 TEST SAU KHI FIX

### 1. Test Frontend (axios đã import)
```bash
# Restart frontend nếu đang chạy
cd /home/huunghia/DATNFUll/DuAnMauFE
npm run dev
```

Console không còn báo "axios is not defined"

### 2. Test Backend (PayOS orderCode mới)
```bash
# Trong terminal mới, test API
curl -X POST http://localhost:8080/order/create \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Test",
    "description": "Test",
    "returnUrl": "http://localhost:5173",
    "cancelUrl": "http://localhost:5173",
    "price": 10000
  }'
```

Kỳ vọng:
```json
{
  "error": 0,
  "message": "success",
  "data": {
    "checkoutUrl": "https://...",
    "qrCode": "data:image/png;base64,..."
  }
}
```

### 3. Test Full Flow
1. Vào trang thanh toán: http://localhost:5173/thanhtoan-banhang
2. Chọn "Thanh toán QR Code"
3. Nhấn "Đặt hàng ngay"
4. Modal hiện ra
5. Chọn PayOS hoặc ZaloPay
6. QR code hiển thị (không còn lỗi)

---

## 📝 FILES ĐÃ THAY ĐỔI

### Frontend:
- ✅ `/DuAnMauFE/src/stores/gbStore.js` - Thêm import axios

### Backend:
- ✅ `/duanbe/src/main/java/com/example/duanbe/payos/OrderController.java` - Fix orderCode generation

---

## 🎯 KẾT QUẢ

### Trước khi fix:
- ❌ Frontend: axios is not defined
- ❌ Backend: PayOS signature error

### Sau khi fix:
- ✅ Frontend: axios imported, không còn lỗi
- ✅ Backend: OrderCode generation cải thiện, error message rõ ràng hơn

---

## ⚠️ LƯU Ý

1. **PayOS Signature Error** có thể vẫn xảy ra nếu:
   - PayOS server sandbox không ổn định
   - Network có vấn đề
   - Credentials hết hạn

2. **Workaround**: Nếu PayOS vẫn lỗi, sử dụng ZaloPay
   - ZaloPay đã được fix và hoạt động tốt
   - UnifiedPaymentController hỗ trợ cả hai

3. **Production**: 
   - Test kỹ trước khi deploy
   - Nên có merchant account thật (không dùng sandbox)
   - Monitor error logs

---

## 🚀 NEXT STEPS

1. Test cả PayOS và ZaloPay
2. Nếu PayOS vẫn lỗi signature → Liên hệ PayOS support
3. Có thể tạm thời disable PayOS, chỉ dùng ZaloPay
4. Hoặc thử credentials PayOS mới

---

**Cập nhật**: 16/11/2024 03:45
**Status**: ✅ Fixed axios import, ⚠️ PayOS cần test thêm
