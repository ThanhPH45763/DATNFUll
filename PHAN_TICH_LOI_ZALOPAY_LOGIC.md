# 🐛 PHÂN TÍCH LỖI LOGIC ZALOPAY - TỰ ĐỘNG THANH TOÁN

## 🚨 VẤN ĐỀ BẠN GẶP PHẢI

**Hiện tượng:**
- Bạn tạo QR code thanh toán
- **CHƯA QUÉT QR**, **CHƯA THANH TOÁN**
- Nhưng hóa đơn tự động chuyển trạng thái "Đã thanh toán"

**Nguyên nhân:** Logic SAI trong code!

---

## 🔍 PHÂN TÍCH CHI TIẾT

### ❌ LỖI 1: Frontend tự động gọi check-status (Nghi ngờ cao)

**Vị trí:** Frontend (Vue.js) - Có thể có polling tự động

```javascript
// Frontend có thể đang làm thế này:
setInterval(() => {
    checkZaloPayStatus(idHoaDon); // Gọi API check-status mỗi 3 giây
}, 3000);
```

**Logic trong ZaloPayController.java (Line 86-127):**

```java
@GetMapping("/check-status")
public ResponseEntity<?> checkStatus(@RequestParam Integer idHoaDon) {
    HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)...
    
    // ⚠️ NGUY HIỂM: Kiểm tra trạng thái DB trước
    if ("Đã thanh toán".equalsIgnoreCase(hoaDon.getTrang_thai())) {
        result.put("return_code", 1);  // ← Trả về SUCCESS!
        result.put("return_message", "Thanh toán thành công");
        return ResponseEntity.ok(result);
    }
    
    // Sau đó mới query ZaloPay
    Map<String, Object> result = zaloPayService.queryOrder(appTransId);
    
    // ⚠️ VẤN ĐỀ: Nếu ZaloPay trả về return_code = 1 (THÀNH CÔNG)
    if (result.get("return_code") != null && (Double) result.get("return_code") == 1.0) {
        hoaDon.setTrang_thai("Đã thanh toán");  // ← TỰ ĐỘNG SET!
        hoaDonRepo.save(hoaDon);
    }
}
```

### ❓ ZaloPay Sandbox trả về gì khi CHƯA thanh toán?

**Theo tài liệu ZaloPay:**
- `return_code = 1`: Đã thanh toán thành công
- `return_code = 2`: Đang xử lý (chưa thanh toán)
- `return_code = 3`: Giao dịch thất bại/hủy

**VẤN ĐỀ:** Môi trường **SANDBOX** của ZaloPay có thể:
1. Auto-approve mọi giao dịch (để test dễ dàng)
2. Trả về `return_code = 1` ngay lập tức
3. **KHÔNG CẦN** quét QR thật!

---

## ❌ LỖI 2: Callback URL có thể sai

**Trong ZaloPayConfig.java:**
```java
public static final String CALLBACK_URL = "http://localhost:8080/api/zalopay/callback";
```

**VẤN ĐỀ:**
- Nếu bạn chưa update sang Ngrok URL
- ZaloPay KHÔNG THỂ gọi callback
- **NHƯNG** frontend vẫn polling `/check-status`
- Nếu sandbox auto-approve → Trạng thái vẫn thành công!

---

## ❌ LỖI 3: Không có validation trạng thái hóa đơn

**Trong create-order (Line 36-81):**

```java
@PostMapping("/create-order")
public ResponseEntity<?> createOrder(@RequestParam("idHoaDon") Integer idHoaDon) {
    HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)...
    
    // ⚠️ THIẾU: Không kiểm tra trạng thái hiện tại!
    // Nếu hóa đơn đã "Đã thanh toán" vẫn cho tạo order mới?
    
    Map<String, Object> result = zaloPayService.createOrder(...);
    
    // Lưu app_trans_id
    hoaDon.setGhi_chu(ghiChuCu + " ZaloPay:" + result.get("app_trans_id"));
    hoaDonRepo.save(hoaDon);
}
```

---

## ✅ GIẢI PHÁP ĐÚNG

### FIX 1: Thêm logging để debug

**File:** `ZaloPayController.java`

```java
@GetMapping("/check-status")
public ResponseEntity<?> checkStatus(@RequestParam Integer idHoaDon) {
    HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
    
    System.out.println("=== CHECK STATUS DEBUG ===");
    System.out.println("ID Hóa đơn: " + idHoaDon);
    System.out.println("Trạng thái hiện tại: " + hoaDon.getTrang_thai());
    System.out.println("Ghi chú: " + hoaDon.getGhi_chu());
    
    // Nếu đã thanh toán, trả về luôn
    if ("Đã thanh toán".equalsIgnoreCase(hoaDon.getTrang_thai())) {
        System.out.println(">>> HÓA ĐƠN ĐÃ THANH TOÁN TRƯỚC ĐÓ!");
        Map<String, Object> result = new HashMap<>();
        result.put("return_code", 1);
        result.put("return_message", "Thanh toán thành công");
        return ResponseEntity.ok(result);
    }
    
    String ghiChu = hoaDon.getGhi_chu();
    if (ghiChu == null || !ghiChu.contains("ZaloPay:")) {
        System.out.println(">>> CHƯA TẠO ĐơN ZALOPAY!");
        return ResponseEntity.badRequest().body(Map.of(
            "return_code", -1,
            "return_message", "Chưa tạo đơn hàng ZaloPay"
        ));
    }
    
    String appTransId = extractAppTransId(ghiChu);
    System.out.println("App Trans ID: " + appTransId);
    System.out.println(">>> GỌI ZALOPAY QUERY API...");
    
    Map<String, Object> result = zaloPayService.queryOrder(appTransId);
    
    System.out.println("ZaloPay Response: " + result);
    System.out.println("Return Code: " + result.get("return_code"));
    
    // Chỉ update nếu ZaloPay confirm thành công
    if (result.get("return_code") != null && (Double) result.get("return_code") == 1.0) {
        System.out.println(">>> ZALOPAY XÁC NHẬN THÀNH CÔNG - CẬP NHẬT DB");
        hoaDon.setTrang_thai("Đã thanh toán");
        hoaDon.setHinh_thuc_thanh_toan("Chuyển khoản (ZaloPay)");
        hoaDonRepo.save(hoaDon);
    } else {
        System.out.println(">>> ZALOPAY CHƯA THANH TOÁN - Return Code: " + result.get("return_code"));
    }
    
    System.out.println("=== END CHECK STATUS ===");
    return ResponseEntity.ok(result);
}
```

### FIX 2: Thêm logging vào ZaloPayService

**File:** `ZaloPayService.java` - Method `queryOrder` (Line 111-143)

```java
public Map<String, Object> queryOrder(String appTransId) {
    try {
        System.out.println("=== ZALOPAY QUERY ORDER ===");
        System.out.println("App Trans ID: " + appTransId);
        
        Map<String, String> params = new HashMap<>();
        params.put("app_id", ZaloPayConfig.APP_ID);
        params.put("app_trans_id", appTransId);
        
        String data = params.get("app_id") + "|" + params.get("app_trans_id") + "|" + ZaloPayConfig.KEY1;
        String mac = HMACUtil.HMacHexStringEncode("HmacSHA256", ZaloPayConfig.KEY1, data);
        params.put("mac", mac);
        
        System.out.println("Query Params: " + gson.toJson(params));
        
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(ZaloPayConfig.ENDPOINT_QUERY);
        
        StringEntity entity = new StringEntity(gson.toJson(params), "UTF-8");
        entity.setContentType("application/json");
        post.setEntity(entity);
        
        CloseableHttpResponse response = client.execute(post);
        String responseString = EntityUtils.toString(response.getEntity());
        
        System.out.println("ZaloPay Query Response: " + responseString);
        
        Map<String, Object> result = gson.fromJson(responseString, Map.class);
        
        System.out.println("Parsed Return Code: " + result.get("return_code"));
        System.out.println("=== END ZALOPAY QUERY ===");
        
        client.close();
        
        return result;
        
    } catch (Exception e) {
        System.err.println("Error querying ZaloPay: " + e.getMessage());
        e.printStackTrace();
        Map<String, Object> error = new HashMap<>();
        error.put("return_code", -1);
        error.put("return_message", e.getMessage());
        return error;
    }
}
```

### FIX 3: Thêm validation trong create-order

```java
@PostMapping("/create-order")
public ResponseEntity<?> createOrder(@RequestParam("idHoaDon") Integer idHoaDon) {
    try {
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        
        // ✅ KIỂM TRA: Hóa đơn đã thanh toán chưa?
        if ("Đã thanh toán".equalsIgnoreCase(hoaDon.getTrang_thai())) {
            return ResponseEntity.badRequest().body(Map.of(
                "return_code", -1,
                "return_message", "Hóa đơn đã được thanh toán rồi!"
            ));
        }
        
        // ✅ KIỂM TRA: Đã tạo order ZaloPay chưa?
        if (hoaDon.getGhi_chu() != null && hoaDon.getGhi_chu().contains("ZaloPay:")) {
            System.out.println("⚠️ Cảnh báo: Hóa đơn đã có order ZaloPay, tạo order mới...");
        }
        
        // Tiếp tục logic cũ...
        java.math.BigDecimal tongTien = hoaDon.getTong_tien_sau_giam();
        if (tongTien == null || tongTien.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                "return_code", -1,
                "return_message", "Số tiền thanh toán không hợp lệ"
            ));
        }
        
        String moTa = "Thanh toán hóa đơn " + hoaDon.getMa_hoa_don();
        
        System.out.println("=== TẠO ORDER ZALOPAY ===");
        System.out.println("Mã hóa đơn: " + hoaDon.getMa_hoa_don());
        System.out.println("Số tiền: " + tongTien);
        
        Map<String, Object> result = zaloPayService.createOrder(
            hoaDon.getMa_hoa_don(),
            tongTien,
            moTa
        );
        
        System.out.println("ZaloPay Create Result: " + result);
        
        // Lưu app_trans_id vào DB để tracking
        if (result.get("return_code") != null && (Double) result.get("return_code") == 1.0) {
            try {
                String ghiChuCu = hoaDon.getGhi_chu() != null ? hoaDon.getGhi_chu() : "";
                ghiChuCu = ghiChuCu.replaceAll("ZaloPay:[^ ]+", "").trim();
                hoaDon.setGhi_chu(ghiChuCu + " ZaloPay:" + result.get("app_trans_id"));
                hoaDonRepo.save(hoaDon);
                System.out.println("✅ Đã lưu app_trans_id: " + result.get("app_trans_id"));
            } catch (Exception saveEx) {
                System.err.println("❌ Lỗi khi lưu app_trans_id: " + saveEx.getMessage());
            }
        }
        
        return ResponseEntity.ok(result);
        
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body(Map.of(
            "return_code", -1,
            "return_message", e.getMessage()
        ));
    }
}
```

---

## 🧪 CÁCH TEST VÀ DEBUG

### Bước 1: Thêm logging (đã có ở trên)

### Bước 2: Restart backend và test lại

```bash
cd /home/huunghia/DATNFUll/duanbe
./mvnw spring-boot:run
```

### Bước 3: Theo dõi log khi tạo order

**Tạo order mới**, xem log backend:

```
=== TẠO ORDER ZALOPAY ===
Mã hóa đơn: HD001
Số tiền: 500000
ZaloPay Order Data: {...}
ZaloPay Response: {"return_code":1,"order_url":"...","app_trans_id":"..."}
✅ Đã lưu app_trans_id: 251117_1234567890
```

### Bước 4: Theo dõi log khi frontend gọi check-status

**KHÔNG QUÉT QR**, chỉ đợi và xem log:

```
=== CHECK STATUS DEBUG ===
ID Hóa đơn: 1
Trạng thái hiện tại: Đang chờ
Ghi chú: ZaloPay:251117_1234567890
App Trans ID: 251117_1234567890
>>> GỌI ZALOPAY QUERY API...

=== ZALOPAY QUERY ORDER ===
App Trans ID: 251117_1234567890
Query Params: {"app_id":"2553","app_trans_id":"251117_1234567890","mac":"..."}
ZaloPay Query Response: {"return_code":2,"return_message":"Đang xử lý"}
Parsed Return Code: 2.0
=== END ZALOPAY QUERY ===

ZaloPay Response: {return_code=2.0, return_message=Đang xử lý}
Return Code: 2.0
>>> ZALOPAY CHƯA THANH TOÁN - Return Code: 2.0
=== END CHECK STATUS ===
```

**Nếu thấy `return_code = 2.0`** → ĐÚNG! Chưa thanh toán

**Nếu thấy `return_code = 1.0` NGAY LẬP TỨC** → **ZaloPay Sandbox tự động approve!**

### Bước 5: Quét QR và xem log

**Sau khi quét QR và thanh toán thật:**

```
=== CHECK STATUS DEBUG ===
...
ZaloPay Query Response: {"return_code":1,"return_message":"Giao dịch thành công"}
Return Code: 1.0
>>> ZALOPAY XÁC NHẬN THÀNH CÔNG - CẬP NHẬT DB
✅ Cập nhật trạng thái: Đã thanh toán
=== END CHECK STATUS ===
```

**Hoặc nếu có callback:**

```
ZaloPay Callback Data: {"data":"...","mac":"..."}
Cập nhật trạng thái hóa đơn thành công cho app_trans_id: 251117_1234567890
```

---

## 📊 CÁC TRƯỜNG HỢP CÓ THỂ XẢY RA

### Trường hợp 1: ZaloPay Sandbox Auto-Approve

**Hiện tượng:**
- Tạo order → Gọi query ngay → `return_code = 1` (thành công)
- KHÔNG CẦN quét QR

**Nguyên nhân:**
- Sandbox mode có thể auto-approve để test dễ
- Hoặc bạn đang dùng credentials không phải sandbox

**Giải pháp:**
- Kiểm tra lại APP_ID, KEY1, KEY2
- Đảm bảo đang dùng sandbox: `app_id = 2553`

### Trường hợp 2: Frontend polling quá nhanh

**Hiện tượng:**
- Frontend gọi check-status mỗi 1-2 giây
- Tạo nhiều request đến ZaloPay
- Có thể bị rate limit

**Giải pháp:**
- Tăng interval lên 5-10 giây
- Dừng polling sau 2-3 phút

### Trường hợp 3: Callback hoạt động nhưng không thấy log

**Hiện tượng:**
- Thanh toán thành công
- DB update
- KHÔNG thấy log callback

**Nguyên nhân:**
- Callback URL sai
- ZaloPay không gọi callback được
- Frontend polling update DB trước

**Giải pháp:**
- Test callback bằng curl
- Kiểm tra Ngrok dashboard

---

## ✅ CHECKLIST DEBUG

- [ ] Đã thêm logging vào check-status
- [ ] Đã thêm logging vào queryOrder
- [ ] Đã thêm logging vào create-order
- [ ] Đã restart backend
- [ ] Đã tạo order mới và xem log
- [ ] Đã kiểm tra return_code từ ZaloPay
- [ ] Đã xác định: Sandbox auto-approve hay không?
- [ ] Đã test callback bằng curl
- [ ] Đã kiểm tra frontend có polling không?
- [ ] Đã verify CALLBACK_URL đúng (Ngrok URL)

---

## 🎯 KẾT LUẬN

**VẤN ĐỀ CHỦ YẾU:**

Có 3 khả năng:

1. **ZaloPay Sandbox tự động approve** (khả năng cao nhất)
   → return_code = 1 ngay lập tức

2. **Frontend polling quá nhiều** 
   → Gọi check-status liên tục

3. **Callback URL đúng và hoạt động** 
   → ZaloPay gọi callback thành công

**GIẢI PHÁP:**

1. ✅ Thêm logging (đã cung cấp code ở trên)
2. ✅ Test và xem log để xác định nguyên nhân
3. ✅ Fix logic nếu cần

**BƯỚC TIẾP THEO:**

Hãy thêm logging và chạy lại, sau đó gửi log cho tôi để phân tích chính xác!
