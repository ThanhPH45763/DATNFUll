# 🐛 FIX: FRONTEND HIỂU SAI return_code TỪ ZALOPAY

## ❌ VẤN ĐỀ NGHIÊM TRỌNG

Bạn đúng 100%! Frontend đang hiểu SAI ý nghĩa của `return_code`!

### Response từ ZaloPay create-order:

```javascript
{
  return_code: 1,                    // ← TẠO ORDER THÀNH CÔNG (chưa thanh toán!)
  return_message: "Giao dịch thành công",  // ← Misleading message!
  qr_code: "00020101...",
  order_url: "https://...",
  app_trans_id: "251117_..."
}
```

**`return_code: 1` KHÔNG có nghĩa là thanh toán thành công!**

**Nó chỉ nghĩa là:** ZaloPay đã tạo order OK, user CÓ THỂ thanh toán!

---

## 🔍 PHÂN TÍCH CODE HIỆN TẠI

### Frontend đang làm gì?

**File:** `TheHeader-BanHang.vue`

```javascript
// ❌ SAI - Trong function showZaloPayQR()
if (result.return_code === 1) {
    // Hiển thị QR code ✅ ĐÚNG
    showZaloPayModal.value = true;
    
    // Bắt đầu polling ✅ ĐÚNG
    startCheckingPaymentStatus();
}

// ❌ SAI - Trong function startCheckingPaymentStatus()
if (result.return_code === 1) {
    // Thanh toán thành công  ← SAI! Đây mới chỉ là tạo order!
    paymentStatus.value = 'success';
    clearInterval(checkPaymentInterval);
    showZaloPayModal.value = false;
    message.success('Thanh toán ZaloPay thành công!');
}
```

**Vấn đề:** Polling đang check response từ `create-order` (return_code = 1 = tạo order thành công) và hiểu nhầm là thanh toán thành công!

---

## ✅ GIẢI PHÁP ĐÚNG

### Hiểu đúng 2 API khác nhau:

| API | Endpoint | return_code = 1 nghĩa là gì? |
|-----|----------|------------------------------|
| **Create Order** | `/v2/create` | Tạo order thành công (chưa thanh toán) |
| **Query Order** | `/v2/query` | Đã thanh toán thành công |

### Flow ĐÚNG:

```
[1] Frontend gọi: POST /api/zalopay/create-order
    → Backend gọi ZaloPay /v2/create
    → return_code = 1 → "Tạo order OK"
    → Hiển thị QR code
    
[2] User quét QR và thanh toán trên app ZaloPay
    
[3] Frontend polling: GET /api/zalopay/check-status
    → Backend kiểm tra DB: trang_thai = ?
    → Nếu "Đã thanh toán" → return_code = 1
    → Nếu "Đang chờ" → return_code = 2
    
[4] ZaloPay gửi callback (async):
    → POST https://your-ngrok.com/api/zalopay/callback
    → Backend update DB: trang_thai = "Đã thanh toán"
```

---

## 🔧 FIX FRONTEND

### File: `src/components/admin-components/BanHang/TheHeader-BanHang.vue`

**Tìm function `showZaloPayQR()` và sửa:**

```javascript
const showZaloPayQR = async () => {
    try {
        isLoadingZaloPay.value = true;
        
        if (!activeTabData.value || !activeTabData.value.hd || !activeTabData.value.hd.id_hoa_don) {
            message.error('Vui lòng chọn hóa đơn cần thanh toán');
            return;
        }
        
        const idHoaDon = activeTabData.value.hd.id_hoa_don;
        console.log('🔄 Tạo QR ZaloPay cho hóa đơn ID:', idHoaDon);
        
        const result = await store.createZaloPayOrder(idHoaDon);
        console.log('📨 ZaloPay Create Order Response:', result);
        
        // ✅ ĐÚNG: return_code = 1 chỉ nghĩa là TẠO ORDER THÀNH CÔNG
        if (result.return_code === 1) {
            console.log('✅ TẠO ORDER THÀNH CÔNG (chưa thanh toán)');
            console.log('   - app_trans_id:', result.app_trans_id);
            console.log('   - order_url:', result.order_url);
            
            // Hiển thị QR code
            if (result.qr_code) {
                try {
                    const qrDataUrl = await QRCode.toDataURL(result.qr_code, {
                        width: 300,
                        margin: 2,
                        color: {
                            dark: '#000000',
                            light: '#FFFFFF'
                        }
                    });
                    zaloPayQRUrl.value = qrDataUrl;
                    zaloPayQRCode.value = result.qr_code;
                    
                    console.log('✅ Đã tạo QR code image');
                } catch (qrError) {
                    console.error('❌ Lỗi tạo QR image:', qrError);
                    message.error('Không thể tạo mã QR');
                    return;
                }
            } else {
                message.error('Không nhận được mã QR từ ZaloPay');
                return;
            }
            
            // Hiển thị modal và bắt đầu polling
            showZaloPayModal.value = true;
            paymentStatus.value = 'waiting';  // ← CHƯA THANH toán!
            
            console.log('🔄 Bắt đầu polling để check thanh toán...');
            startCheckingPaymentStatus();
            
        } else {
            console.error('❌ Tạo order thất bại:', result.return_message);
            message.error(result.return_message || 'Không thể tạo mã QR thanh toán');
        }
    } catch (error) {
        console.error('❌ Lỗi khi tạo QR ZaloPay:', error);
        message.error('Đã xảy ra lỗi khi tạo mã thanh toán: ' + (error.message || ''));
    } finally {
        isLoadingZaloPay.value = false;
    }
};
```

**Sửa function `startCheckingPaymentStatus()`:**

```javascript
const startCheckingPaymentStatus = () => {
    console.log('🔄 Bắt đầu polling check payment status...');
    
    checkPaymentInterval = setInterval(async () => {
        try {
            console.log('📡 Gọi checkZaloPayStatus, idHoaDon:', activeTabData.value.hd.id_hoa_don);
            
            // ⚡ GỌI API CHECK-STATUS (không phải create-order!)
            const result = await store.checkZaloPayStatus(activeTabData.value.hd.id_hoa_don);
            
            console.log('📨 Check Status Response:', result);
            console.log('   - return_code:', result.return_code);
            console.log('   - return_message:', result.return_message);
            
            // ✅ ĐÚNG: return_code từ check-status
            if (result.return_code === 1) {
                console.log('🎉 THANH TOÁN THÀNH CÔNG!');
                
                // Thanh toán thành công
                paymentStatus.value = 'success';
                clearInterval(checkPaymentInterval);
                checkPaymentInterval = null;
                
                // Cập nhật UI
                if (activeTabData.value && activeTabData.value.hd) {
                    console.log('🔄 Cập nhật trạng thái hóa đơn trong UI...');
                    activeTabData.value.hd.trang_thai = 'Đã thanh toán';
                    activeTabData.value.hd.hinh_thuc_thanh_toan = 'Chuyển khoản (ZaloPay)';
                }
                
                setTimeout(() => {
                    showZaloPayModal.value = false;
                    message.success('Thanh toán ZaloPay thành công!');
                    closeZaloPayModal();
                }, 2000);
                
            } else if (result.return_code === 2) {
                console.log('⏳ Đang chờ thanh toán...');
                // Đang xử lý - chưa thanh toán
                paymentStatus.value = 'checking';
                
            } else if (result.return_code === 3) {
                console.log('❌ Thanh toán thất bại');
                // Thất bại
                paymentStatus.value = 'failed';
                clearInterval(checkPaymentInterval);
                checkPaymentInterval = null;
                message.error('Thanh toán thất bại: ' + (result.return_message || ''));
                
            } else {
                console.log('⚠️ Trạng thái không xác định:', result.return_code);
                // Trạng thái khác
                paymentStatus.value = 'checking';
            }
        } catch (error) {
            console.error('❌ LỖI khi kiểm tra trạng thái thanh toán:', error);
            // Không clear interval, thử lại lần sau
        }
    }, 3000); // Kiểm tra mỗi 3 giây
};
```

---

## 🔧 FIX BACKEND check-status

### File: `duanbe/src/main/java/com/example/duanbe/controller/ZaloPayController.java`

Sửa `@GetMapping("/check-status")`:

```java
@GetMapping("/check-status")
public ResponseEntity<?> checkStatus(@RequestParam Integer idHoaDon) {
    try {
        System.out.println("\n=== CHECK STATUS DEBUG ===");
        System.out.println("ID Hóa đơn: " + idHoaDon);
        
        HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        
        System.out.println("Trạng thái hiện tại trong DB: " + hoaDon.getTrang_thai());
        System.out.println("Ghi chú: " + hoaDon.getGhi_chu());
        
        Map<String, Object> result = new HashMap<>();
        
        // ✅ ĐÚNG: Kiểm tra trạng thái trong DB
        if ("Đã thanh toán".equalsIgnoreCase(hoaDon.getTrang_thai())) {
             System.out.println(">>> HÓA ĐƠN ĐÃ THANH TOÁN");
             result.put("return_code", 1);  // ← Thanh toán thành công
             result.put("return_message", "Thanh toán thành công");
             return ResponseEntity.ok(result);
        }
        
        // ⚠️ Chưa thanh toán
        System.out.println(">>> HÓA ĐƠN CHƯA THANH TOÁN");
        result.put("return_code", 2);  // ← Đang chờ thanh toán
        result.put("return_message", "Đang chờ thanh toán");
        
        System.out.println("=== END CHECK STATUS ===\n");
        return ResponseEntity.ok(result);
        
    } catch (Exception e) {
        System.err.println("!!! LỖI CHECK STATUS: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.badRequest().body(Map.of(
            "return_code", -1,
            "return_message", e.getMessage()
        ));
    }
}
```

**CHÚ Ý:** 
- Không cần gọi ZaloPay query API nữa!
- Chỉ cần check DB: nếu callback đã update → trả về success
- Đơn giản hơn và nhanh hơn!

---

## 🧪 TEST LẠI

### Bước 1: Restart frontend

```bash
cd /home/huunghia/DATNFUll/DuAnMauFE
npm run dev
```

### Bước 2: Test flow đúng

1. **Tạo order** → Xem console:
   ```
   ✅ TẠO ORDER THÀNH CÔNG (chưa thanh toán)
   🔄 Bắt đầu polling để check thanh toán...
   ```

2. **Modal hiển thị QR** → Trạng thái: "⏳ Đang chờ thanh toán"

3. **Polling mỗi 3s** → Console log:
   ```
   📡 Gọi checkZaloPayStatus...
   📨 Check Status Response: {return_code: 2}
   ⏳ Đang chờ thanh toán...
   ```

4. **Thanh toán bằng app ZaloPay**

5. **Backend nhận callback:**
   ```
   ╔═══════════════════════════════════════════════════════╗
   ║  ⚡ ZALOPAY CALLBACK NHẬN ĐƯỢC                       ║
   ╚═══════════════════════════════════════════════════════╝
   ✅ ĐÃ CẬP NHẬT TRẠNG THÁI: Đã thanh toán
   ```

6. **Polling tiếp theo:**
   ```
   📨 Check Status Response: {return_code: 1}
   🎉 THANH TOÁN THÀNH CÔNG!
   ```

7. **Modal tự động đóng**, hiển thị "Thanh toán thành công"

---

## ✅ CHECKLIST

- [ ] Đã sửa `showZaloPayQR()` - return_code = 1 chỉ là tạo order
- [ ] Đã sửa `startCheckingPaymentStatus()` - phân biệt return_code
- [ ] Đã sửa backend `/check-status` - check DB thay vì gọi ZaloPay
- [ ] Đã restart frontend
- [ ] Test: Tạo order → Modal mở, trạng thái "waiting"
- [ ] Test: Chưa thanh toán → Polling return_code = 2
- [ ] Test: Đã thanh toán → Polling return_code = 1 → Modal đóng

---

## 🎯 KẾT LUẬN

**Lỗi chính:** Frontend nhầm lẫn `return_code = 1` từ `/v2/create` (tạo order) với thanh toán thành công!

**Sửa:** Phân biệt rõ 2 loại response:
- `create-order` → return_code = 1 → Tạo order OK → Hiển thị QR
- `check-status` → return_code = 1 → Đã thanh toán → Đóng modal

**Xin lỗi vì đã hiểu sai trước đó!** 🙏
