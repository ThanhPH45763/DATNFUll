# ZALOPAY - HƯỚNG DẪN NHANH

## 🚀 BƯỚC 1: BẮT ĐẦU NGAY (5 PHÚT)

### Thêm vào `pom.xml`:
```xml
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
</dependency>
```

### Tạo 4 file Java:

**1. `duanbe/src/main/java/com/example/duanbe/config/ZaloPayConfig.java`**
**2. `duanbe/src/main/java/com/example/duanbe/utils/HMACUtil.java`**
**3. `duanbe/src/main/java/com/example/duanbe/service/ZaloPayService.java`**
**4. `duanbe/src/main/java/com/example/duanbe/controller/ZaloPayController.java`**

→ Xem chi tiết code trong file `HUONG_DAN_TICH_HOP_ZALOPAY.md`

---

## 📱 BƯỚC 2: TEST NGAY (2 PHÚT)

### Trên máy tính:
1. Start backend: `./mvnw spring-boot:run`
2. Start frontend: `npm run dev`
3. Vào: http://localhost:3000/admin/banhang
4. Thêm sản phẩm → Chọn "ZaloPay" → Click "Hiển thị QR"

### Trên điện thoại:
1. Tải app **ZaloPay** từ CH Play/App Store
2. Đăng ký tài khoản (dùng số ĐT thật)
3. Quét mã QR trên màn hình máy tính
4. Xác nhận thanh toán (tiền ảo, KHÔNG MẤT TIỀN)

---

## ⚡ LƯU Ý QUAN TRỌNG

### ✅ ĐÚNG:
- Dùng credentials Sandbox (app_id=2553)
- Thanh toán dùng tiền ảo
- Test trên app ZaloPay chính thức (KHÔNG cần app riêng)
- Tự động chuyển sang môi trường Sandbox

### ❌ SAI:
- KHÔNG dùng tiền thật
- KHÔNG cần đăng ký merchant
- KHÔNG cần KYC

---

## 🎯 DEMO FLOW

```
1. Khách chọn sản phẩm
2. Nhân viên chọn "Thanh toán ZaloPay"
3. Hiển thị QR code
4. Khách quét QR bằng app ZaloPay
5. Khách xác nhận thanh toán
6. Hệ thống tự động cập nhật "Đã thanh toán"
```

---

## 🔧 TROUBLESHOOTING

### Lỗi "Invalid MAC"
→ Kiểm tra `HMACUtil.java` - dòng `data.toByteArray()` phải là `data.getBytes()`

### QR không hiển thị
→ Check console browser: F12 → Console → Xem lỗi gì

### App không quét được
→ Đảm bảo màn hình sáng 100%, QR code rõ nét

---

## 📞 SUPPORT

Nếu gặp lỗi, check file log:
- Backend: Terminal chạy Spring Boot
- Frontend: Browser Console (F12)

Tài liệu đầy đủ: `HUONG_DAN_TICH_HOP_ZALOPAY.md`
