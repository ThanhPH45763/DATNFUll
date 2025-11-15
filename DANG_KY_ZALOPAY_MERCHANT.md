# HƯỚNG DẪN ĐĂNG KÝ VÀ CẤU HÌNH ZALOPAY MERCHANT

## 🎯 TÓM TẮT NHANH

**ĐỂ TEST/DEMO:** Bạn **KHÔNG CẦN** đăng ký gì cả!  
Chỉ cần dùng **credentials Sandbox có sẵn** trong code:

```java
app_id = 2553
key1 = PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL
key2 = kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz
```

---

## 📋 CHI TIẾT CÁC TRƯỜNG HỢP

### ❌ TRƯỜNG HỢP 1: CHỈ TEST/DEMO (KHUYẾN NGHỊ)

**KHÔNG CẦN đăng ký gì trên web ZaloPay!**

#### Lý do:
- ZaloPay đã cung cấp sẵn môi trường **Sandbox** (demo)
- Credentials **công khai** cho mọi developer test
- Không cần KYC, không cần hợp đồng
- Hoàn toàn MIỄN PHÍ

#### Chỉ cần:
1. ✅ Dùng credentials có sẵn (xem code mẫu)
2. ✅ Tải app ZaloPay trên điện thoại
3. ✅ Đăng ký tài khoản ZaloPay user (số ĐT thật)
4. ✅ Test thanh toán bằng app

#### Không cần:
- ❌ Đăng ký merchant trên web
- ❌ Tạo app ID riêng
- ❌ Xác minh doanh nghiệp
- ❌ Ký hợp đồng

---

### ✅ TRƯỜNG HỢP 2: TRIỂN KHAI THẬT (SẢN PHẨM THỰC TẾ)

**CẦN đăng ký merchant và tạo app riêng**

## 📋 BƯỚC 1: ĐĂNG KÝ TÀI KHOẢN MERCHANT

### 1.1. Truy cập trang đăng ký
```
URL: https://merchant.zalopay.vn/
```

### 1.2. Click "Đăng ký" → Chọn loại hình
- **Cá nhân**: Cần CMND/CCCD
- **Doanh nghiệp**: Cần giấy phép kinh doanh

### 1.3. Điền thông tin
```
- Họ tên/Tên doanh nghiệp
- Email
- Số điện thoại
- Địa chỉ
- Mã số thuế (nếu là DN)
- Lĩnh vực kinh doanh
```

### 1.4. Upload giấy tờ
- CMND/CCCD (mặt trước + sau)
- Giấy phép kinh doanh (nếu là DN)
- Chứng nhận đăng ký thuế

### 1.5. Xác thực
- ZaloPay sẽ gọi điện xác minh
- Thời gian: 1-3 ngày làm việc

---

## 📋 BƯỚC 2: TẠO ỨNG DỤNG (APP)

### 2.1. Đăng nhập Merchant Portal
```
URL: https://merchant.zalopay.vn/
Email: email_đã_đăng_ký
Password: mật_khẩu_đã_tạo
```

### 2.2. Tạo App mới
1. Vào menu **"Ứng dụng"**
2. Click **"Tạo ứng dụng mới"**
3. Điền thông tin:
   ```
   - Tên ứng dụng: Hệ thống bán hàng
   - Loại ứng dụng: Payment Gateway
   - Mô tả: Thanh toán đơn hàng online
   - Website: http://your-domain.com
   - Callback URL: http://your-domain.com/api/zalopay/callback
   ```

### 2.3. Nhận thông tin App
Sau khi tạo, bạn sẽ nhận được:

```java
// PRODUCTION credentials (THẬT)
app_id = 1234  // ID riêng của bạn
key1 = AbCdEfGh1234567890  // Key riêng của bạn
key2 = XyZ9876543210AbCdE  // Key riêng của bạn
```

### 2.4. Cập nhật vào code
Thay thế trong `ZaloPayConfig.java`:
```java
@Configuration
public class ZaloPayConfig {
    // ❌ XÓA credentials Sandbox
    // public static final String APP_ID = "2553";  
    
    // ✅ DÙNG credentials PRODUCTION
    public static final String APP_ID = "1234";  // App ID của bạn
    public static final String KEY1 = "AbCdEfGh1234567890";
    public static final String KEY2 = "XyZ9876543210AbCdE";
    
    // ✅ DÙNG endpoint PRODUCTION
    public static final String ENDPOINT_CREATE = "https://openapi.zalopay.vn/v2/create";
    public static final String ENDPOINT_QUERY = "https://openapi.zalopay.vn/v2/query";
    
    // ✅ DÙNG domain thật
    public static final String CALLBACK_URL = "https://your-domain.com/api/zalopay/callback";
    public static final String REDIRECT_URL = "https://your-domain.com/admin/banhang";
}
```

---

## 📋 BƯỚC 3: CẤU HÌNH CALLBACK URL

### 3.1. Tại sao cần callback?
Callback là URL mà ZaloPay sẽ gọi khi:
- Thanh toán thành công
- Thanh toán thất bại
- Hủy giao dịch

### 3.2. Cấu hình callback trên Merchant Portal
1. Vào **"Cấu hình"** → **"Callback"**
2. Nhập URL callback:
   ```
   https://your-domain.com/api/zalopay/callback
   ```
3. Chọn phương thức: **POST**
4. Click **"Lưu"**

### 3.3. Test callback với ngrok
**Vấn đề:** localhost không public, ZaloPay không gọi được

**Giải pháp:** Dùng ngrok
```bash
# 1. Cài ngrok
npm install -g ngrok

# 2. Chạy ngrok
ngrok http 8080

# 3. Nhận được URL public
# Forwarding: https://abc123.ngrok.io -> localhost:8080

# 4. Cập nhật callback URL
https://abc123.ngrok.io/api/zalopay/callback
```

---

## 📋 BƯỚC 4: TEST TRÊN PRODUCTION

### 4.1. Khác biệt Sandbox vs Production

| Tính năng | Sandbox | Production |
|-----------|---------|------------|
| **Tiền** | Ảo (test) | Thật |
| **App ID** | 2553 (chung) | Riêng cho từng merchant |
| **Endpoint** | sb-openapi.zalopay.vn | openapi.zalopay.vn |
| **Callback** | Không bắt buộc | Bắt buộc phải public |
| **KYC** | Không cần | Cần xác minh |

### 4.2. Các bước test Production
1. ✅ Cập nhật credentials Production vào code
2. ✅ Deploy lên server có domain thật (hoặc dùng ngrok)
3. ✅ Cấu hình callback URL
4. ✅ Test thanh toán bằng app ZaloPay
5. ✅ **MẤT TIỀN THẬT** khi thanh toán thành công!

---

## 📋 BƯỚC 5: PHÍ GIAO DỊCH

### 5.1. Phí ZaloPay thu
```
Phí giao dịch: 1.1% - 2.2% (tùy gói)
Phí rút tiền: 11,000đ/lần (tùy ngân hàng)
Phí hoàn tiền: Miễn phí
```

### 5.2. Ví dụ tính phí
```
Đơn hàng: 1,000,000đ
Phí ZaloPay (1.5%): 15,000đ
Merchant nhận: 985,000đ
```

### 5.3. Chu kỳ đối soát
```
T+1: ZaloPay đối soát và chuyển tiền sau 1 ngày
T+7: Hoặc sau 7 ngày (tùy hợp đồng)
```

---

## 📋 BƯỚC 6: HỢP ĐỒNG VÀ CHÍNH SÁCH

### 6.1. Hợp đồng hợp tác
Sau khi tài khoản được duyệt:
1. ZaloPay gửi hợp đồng qua email
2. In ra, ký tên, đóng dấu (nếu là DN)
3. Scan và gửi lại
4. Hoặc ký điện tử (nếu có)

### 6.2. Chính sách quan trọng
- **Không** được bán hàng cấm
- **Không** được gian lận
- **Phải** có chính sách hoàn trả rõ ràng
- **Phải** bảo mật thông tin khách hàng

---

## 🎯 SO SÁNH: SANDBOX vs PRODUCTION

### ✅ DÙNG SANDBOX KHI:
- Đang học tập, nghiên cứu
- Phát triển tính năng mới
- Demo cho khách hàng
- Test tính năng trước khi deploy
- **Đồ án tốt nghiệp, đồ án môn học** ✨

### ✅ DÙNG PRODUCTION KHI:
- Triển khai hệ thống thật
- Bán hàng online chính thức
- Thu tiền từ khách hàng
- Có giấy phép kinh doanh

---

## 📞 LIÊN HỆ HỖ TRỢ

### ZaloPay Merchant Support
```
Hotline: 1900 5555 77
Email: merchant@zalopay.vn
Chat: https://merchant.zalopay.vn (góc phải màn hình)
```

### Tài liệu tham khảo
```
Developer Docs: https://docs.zalopay.vn/
Merchant Portal: https://merchant.zalopay.vn/
API Reference: https://docs.zalopay.vn/v2/
```

---

## ✅ KHUYẾN NGHỊ CHO DỰ ÁN CỦA BẠN

### 🎓 Nếu là đồ án tốt nghiệp/demo:

**DÙNG SANDBOX - KHÔNG CẦN ĐĂNG KÝ GÌ!**

```java
// Chỉ cần copy/paste vào code:
app_id = 2553
key1 = PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL
key2 = kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz
endpoint = https://sb-openapi.zalopay.vn/v2/create
```

**Lợi ích:**
- ✅ Không tốn phí
- ✅ Không cần giấy tờ
- ✅ Test unlimited
- ✅ Đủ để demo/bảo vệ đồ án
- ✅ Giống 100% môi trường thật

### 🏢 Nếu triển khai thật:

**ĐĂNG KÝ MERCHANT - LÀM THEO 6 BƯỚC TRÊN**

---

## 🎉 KẾT LUẬN

**TL;DR:**
- **Đồ án/Demo:** KHÔNG cần đăng ký, dùng Sandbox có sẵn
- **Sản phẩm thật:** Cần đăng ký merchant, tạo app, ký hợp đồng

**Cho dự án của bạn:** Chỉ cần dùng credentials Sandbox đã đủ! 🚀
