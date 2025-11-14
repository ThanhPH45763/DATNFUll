# Cập nhật KhachHangController - Loại bỏ TaiKhoan riêng biệt

## Tóm tắt thay đổi

Đã cập nhật toàn bộ logic trong `KhachHangController` để:
1. **Loại bỏ** dependency vào entity `TaiKhoan` riêng biệt
2. **Sử dụng** trực tiếp các trường `tenDangNhap` và `matKhau` trong entity `KhachHang`
3. **Xóa** tất cả các annotation phân quyền `@PreAuthorize`

## Chi tiết các thay đổi

### 1. Entity KhachHang
- Đã có sẵn các trường:
  - `tenDangNhap` (username)
  - `matKhau` (password - đã mã hóa)
  - Các trường thông tin khách hàng khác

### 2. Controller Methods Updated

#### a. `/add` - Thêm khách hàng
- ✅ Lưu `tenDangNhap = email`
- ✅ Lưu `matKhau` đã mã hóa trực tiếp vào KhachHang
- ✅ Không tạo TaiKhoan riêng nữa

#### b. `/addKHMoi` - Thêm khách hàng nhanh
- ✅ Tương tự như `/add`
- ✅ Kiểm tra email trùng bằng `khachHangRepo.existsByEmail()`

#### c. `/edit/{id}` - Lấy thông tin để edit
- ✅ Lấy mật khẩu từ `khachHang.getMatKhau()`
- ✅ Không dùng `khachHang.getTaiKhoan()` nữa

#### d. `/update` - Cập nhật khách hàng
- ✅ **Đã xóa** annotation `@PreAuthorize`
- ✅ Cập nhật trực tiếp thông tin KhachHang

#### e. `/detail/{id}` - Xem chi tiết
- ✅ Trả về mật khẩu từ `khachHang.getMatKhau()`

#### f. `/register` - Đăng ký khách hàng mới
- ✅ Tạo KhachHang với `tenDangNhap = email`
- ✅ Lưu `matKhau` đã mã hóa
- ✅ Không tạo TaiKhoan riêng
- ✅ Gửi email chào mừng với thông tin đăng nhập

#### g. `/login` - Đăng nhập
- ✅ **Thay đổi lớn**: Không dùng `authenticationManager`, `jwtUtil`, `userDetailsService` nữa
- ✅ Tìm KhachHang theo email
- ✅ Kiểm tra mật khẩu bằng `passwordEncoder.matches()`
- ✅ Kiểm tra trạng thái "Đang hoạt động"
- ✅ Lưu lịch sử đăng nhập với `lichSuDangNhap.setKhachHang()`
- ⚠️ **Lưu ý**: Không trả về JWT token nữa (có thể cần thêm nếu frontend yêu cầu)

#### h. `/change-password` - Đổi mật khẩu
- ✅ Nhận email qua `@RequestParam` thay vì từ JWT token
- ✅ Kiểm tra và cập nhật mật khẩu trực tiếp trong KhachHang
- ✅ Không còn logic phức tạp cho nhiều roles

#### i. `/forgot-password` - Quên mật khẩu
- ✅ **Đơn giản hóa**: Sinh mật khẩu mới ngẫu nhiên
- ✅ Gửi email với mật khẩu mới (thay vì reset token)
- ✅ Cập nhật mật khẩu ngay vào database

#### j. `/reset-password` endpoints
- ✅ **Đã xóa** các endpoint GET và POST `/reset-password`
- ✅ Không dùng reset token nữa

#### k. `/details` - Lấy thông tin khách hàng
- ✅ Tìm theo email thay vì tenDangNhap
- ✅ Sử dụng `khachHangRepo.findByEmail()`

#### l. `/update-order-info` - Cập nhật thông tin đơn hàng
- ✅ Nhận email qua `@RequestParam` thay vì `@AuthenticationPrincipal`
- ✅ Không cần authentication

#### m. `/send-support-request` - Gửi yêu cầu hỗ trợ
- ✅ Nhận email qua `@RequestParam`
- ✅ **Đã xóa** comment về ROLE_KH

### 3. Entity LichSuDangNhap
- ✅ **Thêm mới** quan hệ `@ManyToOne` với KhachHang
- ✅ Có trường `id_khach_hang` để lưu lịch sử đăng nhập

### 4. Dependencies đã loại bỏ
- ❌ `TaiKhoanRepo`
- ❌ `RolesRepo`  
- ❌ `NhanVienRepo`
- ❌ `AuthenticationManager`
- ❌ `JwtUtil`
- ❌ `UserDetailsService`
- ❌ Các annotation `@PreAuthorize`

## Lưu ý quan trọng

### ⚠️ Cần kiểm tra thêm:

1. **Frontend Authentication**: 
   - Nếu frontend đang sử dụng JWT token, cần cập nhật lại logic login
   - Có thể cần giữ lại JWT hoặc chuyển sang session-based auth

2. **Bảo mật**:
   - Các endpoint hiện không có phân quyền
   - Cần thêm validation để đảm bảo chỉ khách hàng mới có thể cập nhật thông tin của mình
   - Xem xét thêm middleware/interceptor để kiểm tra quyền

3. **Database**:
   - Đảm bảo bảng `lich_su_dang_nhap` có cột `id_khach_hang`
   - Có thể cần migration nếu cấu trúc cũ khác

4. **Email Service**:
   - Forgot password giờ gửi mật khẩu mới trực tiếp
   - Cần đảm bảo email service hoạt động tốt

## Testing Checklist

- [ ] Test đăng ký khách hàng mới
- [ ] Test đăng nhập với email/password
- [ ] Test đổi mật khẩu
- [ ] Test quên mật khẩu
- [ ] Test thêm/sửa/xóa khách hàng từ admin
- [ ] Test cập nhật thông tin đơn hàng
- [ ] Test gửi yêu cầu hỗ trợ
- [ ] Kiểm tra lịch sử đăng nhập được lưu đúng

## Files đã thay đổi

1. `/duanbe/src/main/java/com/example/duanbe/controller/KhachHangController.java`
2. `/duanbe/src/main/java/com/example/duanbe/entity/LichSuDangNhap.java`

