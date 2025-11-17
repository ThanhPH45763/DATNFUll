# 🎯 HIỂU ĐÚNG VỀ ZALOPAY CALLBACK - THEO TÀI LIỆU CHÍNH THỨC

## ❌ VẤN ĐỀ BẠN GẶP PHẢI

Tôi đã phân tích sai! Bây giờ tôi hiểu rõ vấn đề:

**Callback URL hiện tại:**
```java
public static final String CALLBACK_URL = "https://fourcha-adolph-noncondescending.ngrok-free.dev";
```

**VẤN ĐỀ:**
- Thiếu endpoint `/api/zalopay/callback` ở cuối!
- ZaloPay không biết gửi callback về đâu!

---

## 📚 CÁCH ZALOPAY CALLBACK HOẠT ĐỘNG (THEO TÀI LIỆU CHÍNH THỨC)

### 1. Luồng thanh toán ZaloPay đầy đủ:

```
[1] Frontend gọi API: POST /api/zalopay/create-order
    ↓
[2] Backend tạo order ZaloPay với CALLBACK_URL
    ↓
[3] ZaloPay trả về: 
    - return_code = 1
    - order_url (link thanh toán)
    - zp_trans_token (mã giao dịch)
    ↓
[4] Frontend hiển thị QR code từ order_url
    ↓
[5] User quét QR bằng app ZaloPay và thanh toán
    ↓
[6] ⚡ ZaloPay GỌI CALLBACK về backend:
    POST https://your-ngrok-url.com/api/zalopay/callback
    {
        "data": "...",  // Encrypted data
        "mac": "..."    // Chữ ký xác thực
    }
    ↓
[7] Backend xử lý callback:
    - Verify MAC (dùng KEY2)
    - Parse data
    - Cập nhật DB: trang_thai = "Đã thanh toán"
    - Trả về: {"return_code": 1, "return_message": "success"}
    ↓
[8] Frontend polling: GET /api/zalopay/check-status
    - Kiểm tra DB đã update chưa
    - Nếu "Đã thanh toán" → Đóng modal, hiển thị thông báo
```

---

## 🔧 FIX ĐÚNG - BƯỚC 1: SỬA CALLBACK_URL

### File: `duanbe/src/main/java/com/example/duanbe/config/ZaloPayConfig.java`

```java
package com.example.duanbe.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ZaloPayConfig {
    // Sandbox credentials (môi trường test)
    public static final String APP_ID = "2553";
    public static final String KEY1 = "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL";
    public static final String KEY2 = "kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz";
    
    // Sandbox endpoints
    public static final String ENDPOINT_CREATE = "https://sb-openapi.zalopay.vn/v2/create";
    public static final String ENDPOINT_QUERY = "https://sb-openapi.zalopay.vn/v2/query";
    
    // ✅ CALLBACK_URL PHẢI CÓ ENDPOINT ĐẦY ĐỦ!
    // Thay YOUR_NGROK_URL bằng URL ngrok thật của bạn
    public static final String CALLBACK_URL = "https://fourcha-adolph-noncondescending.ngrok-free.dev/api/zalopay/callback";
    
    // Redirect URL (sau khi thanh toán xong)
    public static final String REDIRECT_URL = "http://localhost:5173/admin/banhang";
}
```

**CHÚ Ý:**
- URL phải bắt đầu bằng `https://` (ngrok tự động có SSL)
- Phải có endpoint đầy đủ: `/api/zalopay/callback`
- URL này ZaloPay sẽ GỌI VỀ khi user thanh toán thành công

---

## 🔧 FIX ĐÚNG - BƯỚC 2: XÁC NHẬN CALLBACK ENDPOINT

### File: `duanbe/src/main/java/com/example/duanbe/controller/ZaloPayController.java`

Kiểm tra callback endpoint (line ~130-170):

```java
/**
 * ⚡ CALLBACK TỪ ZALOPAY
 * ZaloPay sẽ gọi endpoint này khi user thanh toán thành công
 * 
 * Request từ ZaloPay:
 * {
 *   "data": "{\"app_id\":2553,\"app_trans_id\":\"...\",\"app_time\":...,\"amount\":...}",
 *   "mac": "abc123..."
 * }
 */
@PostMapping("/callback")
public ResponseEntity<?> callback(@RequestBody String jsonStr) {
    Map<String, Object> result = new HashMap<>();
    
    try {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║  ⚡ ZALOPAY CALLBACK NHẬN ĐƯỢC                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        System.out.println("📨 Raw JSON: " + jsonStr);
        
        // 1. Parse JSON từ ZaloPay
        JsonObject cbdata = gson.fromJson(jsonStr, JsonObject.class);
        String dataStr = cbdata.get("data").getAsString();
        String receivedMac = cbdata.get("mac").getAsString();
        
        System.out.println("📦 Data string: " + dataStr);
        System.out.println("🔐 MAC nhận được: " + receivedMac);

        // 2. Verify MAC (dùng KEY2)
        String calculatedMac = HMACUtil.HMacHexStringEncode("HmacSHA256", ZaloPayConfig.KEY2, dataStr);
        System.out.println("🔐 MAC tính toán: " + calculatedMac);

        if (!calculatedMac.equals(receivedMac)) {
            System.out.println("❌ MAC KHÔNG KHỚP - Callback không hợp lệ!");
            result.put("return_code", -1);
            result.put("return_message", "MAC không hợp lệ");
            return ResponseEntity.ok(result);
        }
        
        System.out.println("✅ MAC HỢP LỆ - Callback chính thức từ ZaloPay");

        // 3. Parse dữ liệu giao dịch
        JsonObject data = gson.fromJson(dataStr, JsonObject.class);
        String appTransId = data.get("app_trans_id").getAsString();
        long amount = data.get("amount").getAsLong();
        long appTime = data.get("app_time").getAsLong();
        
        System.out.println("📝 App Trans ID: " + appTransId);
        System.out.println("💰 Số tiền: " + amount);
        System.out.println("🕐 Thời gian: " + appTime);

        // 4. Tìm hóa đơn trong DB (dựa vào app_trans_id)
        System.out.println("🔍 Tìm hóa đơn chứa app_trans_id: " + appTransId);
        Optional<HoaDon> hoaDonOpt = hoaDonRepo.findByGhiChuContaining(appTransId);

        if (hoaDonOpt.isPresent()) {
            HoaDon hoaDon = hoaDonOpt.get();
            
            System.out.println("✅ TÌM THẤY HÓA ĐƠN:");
            System.out.println("   - ID: " + hoaDon.getId_hoa_don());
            System.out.println("   - Mã: " + hoaDon.getMa_hoa_don());
            System.out.println("   - Trạng thái cũ: " + hoaDon.getTrang_thai());
            
            // 5. Cập nhật trạng thái hóa đơn
            hoaDon.setTrang_thai("Đã thanh toán");
            hoaDon.setHinh_thuc_thanh_toan("Chuyển khoản (ZaloPay)");
            hoaDonRepo.save(hoaDon);
            
            System.out.println("✅ ĐÃ CẬP NHẬT TRẠNG THÁI: Đã thanh toán");
            System.out.println("╔═══════════════════════════════════════════════════════╗");
            System.out.println("║  ✅ CALLBACK XỬ LÝ THÀNH CÔNG                        ║");
            System.out.println("╚═══════════════════════════════════════════════════════╝\n");
            
            // 6. Trả về success cho ZaloPay
            result.put("return_code", 1);
            result.put("return_message", "success");
        } else {
            System.out.println("❌ KHÔNG TÌM THẤY HÓA ĐƠN!");
            System.out.println("   - App Trans ID: " + appTransId);
            System.out.println("   - Kiểm tra lại ghi_chu trong database");
            System.out.println("╚═══════════════════════════════════════════════════════╝\n");
            
            result.put("return_code", 0);
            result.put("return_message", "Không tìm thấy hóa đơn");
        }
        
    } catch (Exception e) {
        System.err.println("❌ LỖI KHI XỬ LÝ CALLBACK:");
        e.printStackTrace();
        result.put("return_code", -1);
        result.put("return_message", e.getMessage());
    }

    // ⚠️ QUAN TRỌNG: Phải trả về JSON cho ZaloPay
    return ResponseEntity.ok(result);
}
```

**CHÚ Ý QUAN TRỌNG:**
1. ✅ Endpoint phải là `@PostMapping("/callback")`
2. ✅ Phải verify MAC bằng KEY2
3. ✅ Phải trả về `{"return_code": 1, "return_message": "success"}` cho ZaloPay
4. ✅ Nếu không trả về đúng, ZaloPay sẽ retry callback nhiều lần!

---

## 🔧 FIX ĐÚNG - BƯỚC 3: TEST CALLBACK

### 3.1. Kiểm tra Ngrok đang chạy

```bash
# Kiểm tra ngrok có đang chạy không
ps aux | grep ngrok

# Nếu không có, chạy lại:
ngrok http 8080

# Lấy URL mới (ví dụ: https://abc-def-ghi.ngrok-free.app)
```

### 3.2. Cập nhật CALLBACK_URL

Sửa `ZaloPayConfig.java`:
```java
public static final String CALLBACK_URL = "https://abc-def-ghi.ngrok-free.app/api/zalopay/callback";
//                                                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
//                                                Thay bằng URL ngrok của bạn
```

### 3.3. Restart backend

```bash
cd /home/huunghia/DATNFUll/duanbe
./mvnw spring-boot:run
```

### 3.4. Test callback thủ công

```bash
# Test xem callback endpoint có hoạt động không
curl -X POST https://abc-def-ghi.ngrok-free.app/api/zalopay/callback \
  -H "Content-Type: application/json" \
  -d '{
    "data": "{\"app_id\":2553,\"app_trans_id\":\"test123\"}",
    "mac": "invalid_mac_for_test"
  }'
```

**Kết quả mong đợi:**
```json
{"return_code":-1,"return_message":"MAC không hợp lệ"}
```

**Nếu thấy response này** → Callback endpoint HOẠT ĐỘNG!

---

## 🧪 TEST FLOW ĐẦY ĐỦ

### Bước 1: Tạo order và lấy QR

1. Vào trang bán hàng
2. Thêm sản phẩm vào hóa đơn
3. Click "Hiển thị QR ZaloPay"
4. **Xem log backend:**

```
=== TẠO ORDER ZALOPAY ===
Mã hóa đơn: HD001
Số tiền: 500000
>>> Gọi ZaloPay Create Order API...
ZaloPay Order Data: {...}
ZaloPay Response: {"return_code":1,"order_url":"...","app_trans_id":"251117_1731831234567"}
✅ Đã lưu app_trans_id: 251117_1731831234567
=== END TẠO ORDER ===
```

### Bước 2: Kiểm tra DB

```sql
SELECT id_hoa_don, ma_hoa_don, trang_thai, ghi_chu 
FROM hoa_don 
WHERE ma_hoa_don = 'HD001';
```

**Kết quả:**
```
id_hoa_don | ma_hoa_don | trang_thai | ghi_chu
1          | HD001      | Đang chờ   | ZaloPay:251117_1731831234567
```

### Bước 3: Thanh toán bằng app ZaloPay

1. Mở app ZaloPay trên điện thoại
2. Quét QR code
3. Thanh toán

### Bước 4: Xem log callback

**Backend sẽ nhận callback từ ZaloPay:**

```
╔═══════════════════════════════════════════════════════╗
║  ⚡ ZALOPAY CALLBACK NHẬN ĐƯỢC                       ║
╚═══════════════════════════════════════════════════════╝
📨 Raw JSON: {"data":"...","mac":"..."}
📦 Data string: {"app_id":2553,"app_trans_id":"251117_1731831234567",...}
🔐 MAC nhận được: abc123...
🔐 MAC tính toán: abc123...
✅ MAC HỢP LỆ - Callback chính thức từ ZaloPay
📝 App Trans ID: 251117_1731831234567
💰 Số tiền: 500000
🔍 Tìm hóa đơn chứa app_trans_id: 251117_1731831234567
✅ TÌM THẤY HÓA ĐƠN:
   - ID: 1
   - Mã: HD001
   - Trạng thái cũ: Đang chờ
✅ ĐÃ CẬP NHẬT TRẠNG THÁI: Đã thanh toán
╔═══════════════════════════════════════════════════════╗
║  ✅ CALLBACK XỬ LÝ THÀNH CÔNG                        ║
╚═══════════════════════════════════════════════════════╝
```

**Nếu KHÔNG thấy log này** → Callback không hoạt động!

### Bước 5: Frontend polling sẽ phát hiện

```
=== CHECK STATUS DEBUG ===
ID Hóa đơn: 1
Trạng thái hiện tại trong DB: Đã thanh toán
>>> HÓA ĐƠN ĐÃ THANH TOÁN TRƯỚC ĐÓ - KHÔNG GỌI ZALOPAY
=== END CHECK STATUS ===
```

Frontend sẽ:
- Đóng modal
- Hiển thị "Thanh toán thành công"
- Update UI

---

## 🐛 DEBUG: NẾU CALLBACK KHÔNG HOẠT ĐỘNG

### Kiểm tra 1: Ngrok có đang chạy không?

```bash
curl https://abc-def-ghi.ngrok-free.app/api/zalopay/callback
```

**Nếu lỗi** → Ngrok đã tắt hoặc URL sai

### Kiểm tra 2: CALLBACK_URL có đúng không?

Xem log khi tạo order:
```
ZaloPay Order Data: {...,"callback_url":"https://..."}
```

**callback_url phải có `/api/zalopay/callback` ở cuối!**

### Kiểm tra 3: Ngrok Dashboard

Vào: `http://127.0.0.1:4040`

Xem có request POST `/api/zalopay/callback` không?

**Nếu KHÔNG CÓ** → ZaloPay không gọi được (URL sai hoặc Ngrok chặn)

### Kiểm tra 4: Ngrok có bị password page không?

Test bằng curl:
```bash
curl -X POST https://abc-def-ghi.ngrok-free.app/api/zalopay/callback \
  -H "Content-Type: application/json" \
  -d '{"data":"test","mac":"test"}'
```

**Nếu thấy HTML** → Ngrok bị chặn, cần authtoken

---

## 📊 SO SÁNH: CALLBACK vs POLLING

| Cơ chế | Thời gian | Ưu điểm | Nhược điểm |
|--------|-----------|---------|------------|
| **Callback** | ~1-2 giây | ⚡ Nhanh, real-time | Cần tunnel (Ngrok) |
| **Polling** | 3-30 giây | ✅ Không cần tunnel | 🐢 Chậm, tốn resource |

**Khuyến nghị:**
- Production: Dùng **CALLBACK** (domain thật, không cần tunnel)
- Development: Dùng **CALLBACK + Ngrok** (như bạn đang làm)
- Không nên dùng chỉ polling (quá chậm)

---

## ✅ CHECKLIST HOÀN CHỈNH

- [ ] CALLBACK_URL có đầy đủ `/api/zalopay/callback`
- [ ] Ngrok đang chạy: `ps aux | grep ngrok`
- [ ] URL Ngrok đúng trong ZaloPayConfig.java
- [ ] Đã restart backend sau khi sửa config
- [ ] Test callback thủ công bằng curl → Thấy response JSON
- [ ] Tạo order → Xem log có `callback_url` đúng
- [ ] Thanh toán → Xem log backend có nhận callback
- [ ] Kiểm tra DB: trang_thai = "Đã thanh toán"
- [ ] Frontend polling → Đóng modal, hiển thị success

---

## 🎯 KẾT LUẬN

**LỖI CHÍNH:**
```java
// ❌ SAI
public static final String CALLBACK_URL = "https://fourcha-adolph-noncondescending.ngrok-free.dev";

// ✅ ĐÚNG
public static final String CALLBACK_URL = "https://fourcha-adolph-noncondescending.ngrok-free.dev/api/zalopay/callback";
```

**FLOW ĐÚNG:**
1. User thanh toán trên app ZaloPay
2. ZaloPay GỌI: `POST https://your-ngrok.com/api/zalopay/callback`
3. Backend nhận callback → Update DB
4. Frontend polling → Phát hiện DB đã update → Đóng modal

**KHÔNG PHẢI:**
- Frontend không cần gọi ZaloPay query API nhiều
- Callback mới là cơ chế CHÍNH
- Polling chỉ là backup

Hãy sửa CALLBACK_URL và test lại! 🚀
