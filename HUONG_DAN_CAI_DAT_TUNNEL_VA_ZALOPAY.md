# 🚀 HƯỚNG DẪN HOÀN CHỈNH: CÀI ĐẶT TUNNEL VÀ TÍCH HỢP ZALOPAY

## ✅ PHẦN 1: ĐÃ SỬA LỖI SPRING BOOT

### Lỗi gốc:
```
No property 'ghiChu' found for type 'HoaDon'; Did you mean 'ghi_chu'
```

### Nguyên nhân:
- Entity `HoaDon` có field `ghi_chu` (snake_case)
- Repository method `findByGhiChuContaining` (camelCase) không khớp
- Spring JPA không tự động convert underscore trong method naming

### Giải pháp đã áp dụng:
**File:** `duanbe/src/main/java/com/example/duanbe/repository/HoaDonRepo.java`

```java
// ❌ CŨ - Lỗi
Optional<HoaDon> findByGhiChuContaining(String appTransId);

// ✅ MỚI - Đúng (dùng @Query)
@Query("SELECT h FROM HoaDon h WHERE h.ghi_chu LIKE %:appTransId%")
Optional<HoaDon> findByGhiChuContaining(@Param("appTransId") String appTransId);
```

### Kết quả:
✅ **Application đã chạy thành công** trên `http://localhost:8080`

---

## 🌐 PHẦN 2: CÀI ĐẶT TUNNEL ĐỂ NHẬN CALLBACK TỪ ZALOPAY

### Vấn đề:
- ZaloPay sandbox server cần gửi callback về backend của bạn
- Backend đang chạy trên `localhost:8080` (không thể truy cập từ internet)
- **Giải pháp:** Dùng tunnel để public localhost ra internet

---

## 📦 OPTION 1: LOCALTUNNEL (KHUYẾN NGHỊ CHO LINUX)

### Tại sao chọn LocalTunnel?
- ✅ Miễn phí 100%
- ✅ Không cần đăng ký tài khoản
- ✅ Chạy bằng 1 lệnh duy nhất (npx)
- ✅ Tương thích cả Linux và Windows
- ✅ Không giới hạn bandwidth cho sandbox test

### Bước 1: Cài đặt (nếu cần)
```bash
# Không cần cài đặt - dùng npx trực tiếp!
# Hoặc cài global:
npm install -g localtunnel
```

### Bước 2: Chạy tunnel
**Mở terminal MỚI** (không phải terminal đang chạy backend):

```bash
npx localtunnel --port 8080
```

Hoặc nếu đã cài global:
```bash
lt --port 8080
```

### Bước 3: Lấy URL public
Sau khi chạy, bạn sẽ thấy output:
```
your url is: https://bright-cats-enjoy.loca.lt
```

**Lưu URL này lại!**

### Bước 4: Test tunnel
Mở trình duyệt, vào URL vừa lấy được (ví dụ: `https://bright-cats-enjoy.loca.lt`)

- Lần đầu sẽ có trang cảnh báo → Click "Continue"
- Sau đó sẽ thấy trang web của bạn

### Bước 5: Cập nhật Callback URL trong code

**File:** `duanbe/src/main/java/com/example/duanbe/config/ZaloPayConfig.java`

```java
// ❌ CŨ
public static final String CALLBACK_URL = "http://localhost:8080/api/zalopay/callback";

// ✅ MỚI - Thay YOUR_SUBDOMAIN bằng subdomain bạn nhận được
public static final String CALLBACK_URL = "https://bright-cats-enjoy.loca.lt/api/zalopay/callback";
```

### Bước 6: Restart backend
```bash
# Ctrl+C để stop backend đang chạy
# Sau đó chạy lại:
cd /home/huunghia/DATNFUll/duanbe
./mvnw spring-boot:run
```

### ⚠️ Lưu ý quan trọng về LocalTunnel:
1. **URL thay đổi mỗi lần chạy** → Phải update code mỗi lần restart tunnel
2. **Giải pháp:** Dùng subdomain cố định:
   ```bash
   npx localtunnel --port 8080 --subdomain myshop2024
   # URL sẽ là: https://myshop2024.loca.lt
   ```
   Subdomain có thể bị chiếm bởi người khác, hãy thử tên unique!

---

## 📦 OPTION 2: NGROK (CHO WINDOWS VÀ LINUX)

### Ưu điểm:
- ✅ Ổn định hơn LocalTunnel
- ✅ UI web dashboard đẹp
- ✅ Xem được tất cả request/response
- ❌ Free plan bị giới hạn (40 connections/phút)
- ❌ Cần đăng ký tài khoản

### Bước 1: Tải và cài đặt

**Linux:**
```bash
# Tải ngrok
wget https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-linux-amd64.tgz

# Giải nén
tar -xzf ngrok-v3-stable-linux-amd64.tgz

# Di chuyển vào PATH
sudo mv ngrok /usr/local/bin/
```

**Windows:**
- Download từ: https://ngrok.com/download
- Giải nén và chạy `ngrok.exe`

### Bước 2: Đăng ký và lấy authtoken
1. Truy cập: https://dashboard.ngrok.com/signup
2. Đăng ký tài khoản miễn phí
3. Vào Dashboard → Copy authtoken
4. Chạy lệnh:
   ```bash
   ngrok config add-authtoken YOUR_AUTH_TOKEN
   ```

### Bước 3: Chạy tunnel
```bash
ngrok http 8080
```

### Bước 4: Lấy URL
Sau khi chạy, bạn sẽ thấy:
```
Forwarding   https://abc123.ngrok-free.app -> http://localhost:8080
```

Copy URL `https://abc123.ngrok-free.app`

### Bước 5: Cập nhật Callback URL
```java
// File: ZaloPayConfig.java
public static final String CALLBACK_URL = "https://abc123.ngrok-free.app/api/zalopay/callback";
```

### Bước 6: Xem dashboard
Truy cập: `http://127.0.0.1:4040` để xem tất cả request/response

---

## 📦 OPTION 3: CLOUDFLARE TUNNEL (ỔN ĐỊNH NHẤT - FREE)

### Ưu điểm:
- ✅ Miễn phí KHÔNG GIỚI HẠN
- ✅ URL cố định (không đổi khi restart)
- ✅ Tốc độ nhanh (dùng network Cloudflare)
- ❌ Cài đặt phức tạp hơn

### Bước 1: Cài đặt cloudflared

**Linux:**
```bash
# Tải cloudflared
wget https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb

# Cài đặt
sudo dpkg -i cloudflared-linux-amd64.deb
```

**Hoặc dùng Docker:**
```bash
docker run cloudflare/cloudflared:latest tunnel --url http://localhost:8080
```

### Bước 2: Login Cloudflare
```bash
cloudflared tunnel login
```
Browser sẽ mở → Đăng nhập Cloudflare (hoặc đăng ký free)

### Bước 3: Tạo tunnel
```bash
cloudflared tunnel create myshop-tunnel
```

### Bước 4: Cấu hình tunnel
Tạo file config:
```bash
nano ~/.cloudflared/config.yml
```

Nội dung:
```yaml
tunnel: YOUR_TUNNEL_ID
credentials-file: /home/huunghia/.cloudflared/YOUR_TUNNEL_ID.json

ingress:
  - hostname: myshop.yourdomain.com
    service: http://localhost:8080
  - service: http_status:404
```

### Bước 5: Chạy tunnel
```bash
cloudflared tunnel run myshop-tunnel
```

---

## 🎯 SO SÁNH CÁC GIẢI PHÁP

| Tiêu chí | LocalTunnel | Ngrok | Cloudflare Tunnel |
|----------|------------|-------|-------------------|
| **Giá** | Free | Free (giới hạn) | Free unlimited |
| **Cài đặt** | ⭐⭐⭐⭐⭐ Rất dễ | ⭐⭐⭐⭐ Dễ | ⭐⭐⭐ Trung bình |
| **Tốc độ** | ⭐⭐⭐ Khá | ⭐⭐⭐⭐ Tốt | ⭐⭐⭐⭐⭐ Rất tốt |
| **Ổn định** | ⭐⭐⭐ Khá | ⭐⭐⭐⭐ Tốt | ⭐⭐⭐⭐⭐ Rất tốt |
| **URL cố định** | ❌ Không | ✅ Có (trả phí) | ✅ Có (free) |
| **Dashboard** | ❌ Không | ✅ Có | ⚠️ Cơ bản |
| **Đăng ký** | ❌ Không cần | ✅ Cần | ✅ Cần |

### 🏆 Khuyến nghị:
- **Test nhanh, demo:** → **LocalTunnel** (chạy 1 lệnh là xong)
- **Development:** → **Ngrok** (có dashboard debug)
- **Production/Staging:** → **Cloudflare Tunnel** (ổn định, miễn phí)

---

## 🧪 PHẦN 3: TEST TÍCH HỢP ZALOPAY

### Bước 1: Khởi động đầy đủ

**Terminal 1 - Backend:**
```bash
cd /home/huunghia/DATNFUll/duanbe
./mvnw spring-boot:run
```

**Terminal 2 - Tunnel (LocalTunnel):**
```bash
npx localtunnel --port 8080
# Copy URL nhận được, ví dụ: https://abc123.loca.lt
```

**Terminal 3 - Frontend:**
```bash
cd /home/huunghia/DATNFUll/DuAnMauFE
npm run dev
```

### Bước 2: Cập nhật Callback URL

**File:** `duanbe/src/main/java/com/example/duanbe/config/ZaloPayConfig.java`

```java
public static final String CALLBACK_URL = "https://abc123.loca.lt/api/zalopay/callback";
```

**Restart backend** (Ctrl+C và chạy lại)

### Bước 3: Test API endpoint

**Test 1: Kiểm tra callback endpoint có hoạt động không:**
```bash
curl -X POST https://abc123.loca.lt/api/zalopay/callback \
  -H "Content-Type: application/json" \
  -d '{"data":"test","mac":"test"}'
```

Nếu thấy response JSON → Tunnel hoạt động OK!

**Test 2: Tạo đơn hàng test:**
```bash
# Tạo hóa đơn test trước (qua UI hoặc API)
# Sau đó:
curl -X POST "http://localhost:8080/api/zalopay/create-order?idHoaDon=1"
```

### Bước 4: Test thanh toán end-to-end

1. **Vào trang bán hàng:** `http://localhost:5173/admin/banhang`
2. **Thêm sản phẩm** vào giỏ hàng
3. **Chọn "ZaloPay"** làm phương thức thanh toán
4. **Click "Hiển thị QR"** → Mã QR xuất hiện
5. **Mở app ZaloPay** trên điện thoại
6. **Quét mã QR** và thanh toán
7. **Callback tự động** update trạng thái hóa đơn

### Bước 5: Debug callback

**Xem log backend:**
```bash
# Log sẽ hiển thị khi ZaloPay gửi callback:
ZaloPay Callback Data: {...}
Cập nhật trạng thái hóa đơn thành công cho app_trans_id: 251117_HD001
```

**Nếu không thấy log callback:**
1. Kiểm tra tunnel vẫn đang chạy
2. Kiểm tra CALLBACK_URL đã đúng
3. Thử test bằng curl (như Test 1 ở trên)
4. Kiểm tra firewall có block không

---

## 🔧 TROUBLESHOOTING

### Lỗi 1: "Invalid MAC" khi tạo order
```
Nguyên nhân: Sai Key1/Key2 hoặc cách tính MAC
Giải pháp: Kiểm tra lại ZaloPayConfig.java
```

### Lỗi 2: Callback không được gọi
```bash
# Test callback thủ công:
curl -X POST https://YOUR_TUNNEL_URL/api/zalopay/callback \
  -H "Content-Type: application/json" \
  -d '{"data":"test","mac":"test"}'

# Nếu lỗi 404 → URL sai
# Nếu lỗi 500 → Backend lỗi
# Nếu OK → Tunnel hoạt động, vấn đề từ ZaloPay
```

### Lỗi 3: LocalTunnel bị "Connection refused"
```bash
# Nguyên nhân: Backend chưa chạy
# Giải pháp:
cd /home/huunghia/DATNFUll/duanbe
./mvnw spring-boot:run

# Đợi backend start xong (thấy "Started DuanbeApplication")
# Sau đó chạy tunnel
```

### Lỗi 4: "No property ghi found for type HoaDon"
```
Nguyên nhân: Code chưa được update
Giải pháp: Đã fix trong PHẦN 1 ở trên
```

---

## 📚 TÀI LIỆU THAM KHẢO

### ZaloPay Sandbox:
- **Docs:** https://docs.zalopay.vn/
- **Dashboard:** https://sbx-merchant.zalopay.vn/
- **Test Cards:** https://docs.zalopay.vn/sandbox/guide/

### Tunnel Tools:
- **LocalTunnel:** https://theboroer.github.io/localtunnel-www/
- **Ngrok:** https://ngrok.com/docs
- **Cloudflare Tunnel:** https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/

---

## ✅ CHECKLIST HOÀN CHỈNH

- [x] Đã sửa lỗi Spring Boot (findByGhiChuContaining)
- [x] Backend chạy thành công trên localhost:8080
- [ ] Đã chọn tunnel tool (LocalTunnel/Ngrok/Cloudflare)
- [ ] Tunnel đang chạy và có URL public
- [ ] Đã update CALLBACK_URL trong ZaloPayConfig.java
- [ ] Đã restart backend sau khi update
- [ ] Đã test callback endpoint bằng curl
- [ ] Frontend chạy thành công
- [ ] Đã test tạo QR code thanh toán
- [ ] Đã test thanh toán end-to-end bằng app ZaloPay
- [ ] Callback nhận được và update DB thành công

---

## 🎉 KẾT LUẬN

Bây giờ bạn đã có:
1. ✅ Backend chạy không lỗi
2. ✅ Tunnel để nhận callback từ ZaloPay
3. ✅ Hướng dẫn chi tiết cho cả Linux và Windows
4. ✅ Cách debug khi có vấn đề

**Khuyến nghị:** Dùng **LocalTunnel** để test nhanh ngay bây giờ!

```bash
# Chạy ngay 2 lệnh này:
# Terminal 1:
cd /home/huunghia/DATNFUll/duanbe && ./mvnw spring-boot:run

# Terminal 2:
npx localtunnel --port 8080
```

Chúc bạn thành công! 🚀
