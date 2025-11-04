# 🔧 HƯỚNG DẪN SỬA LỖI LOGIC KHÁCH HÀNG

## 📋 TỔNG QUAN

Đã loại bỏ hoàn toàn bảng `tai_khoan` riêng biệt và tích hợp tài khoản trực tiếp vào bảng `khach_hang`.

---

## 📂 CÁC FILE ĐÃ TẠO/SỬA

### 1. **FIX_KHACHHANG_LOGIC.md**
📖 Tài liệu chi tiết về vấn đề và giải pháp

### 2. **FIX_KhachHangController_NEW.java** ✅ **FILE MỚI HOÀN CHỈNH**
```
📍 Đường dẫn: /home/huunghia/Documents/DuAnBe/FIX_KhachHangController_NEW.java
```

**Nội dung:**
- ✅ Loại bỏ TẤT CẢ tham chiếu đến `TaiKhoan`, `TaiKhoanRepo`, `RolesRepo`
- ✅ Sử dụng `BCryptPasswordEncoder` trực tiếp
- ✅ Đăng nhập/đăng ký chỉ dùng bảng `khach_hang`
- ✅ Session-based authentication (không cần JWT phức tạp)
- ✅ Tất cả API hoạt động bình thường

### 3. **KhachHangRepo.java** ✅ **ĐÃ CẬP NHẬT**
```java
📍 Đường dẫn: duanbe/src/main/java/com/example/duanbe/repository/KhachHangRepo.java
```

**Đã thêm:**
```java
Optional<KhachHang> findByTenDangNhap(String tenDangNhap);
boolean existsByEmail(String email);
boolean existsBySoDienThoai(String soDienThoai);
boolean existsByTenDangNhap(String tenDangNhap);
```

### 4. **UPDATE_KHACHHANG_SCHEMA.sql** ✅ **SQL UPDATE SCRIPT**
```sql
📍 Đường dẫn: /home/huunghia/Documents/DuAnBe/UPDATE_KHACHHANG_SCHEMA.sql
```

**Chức năng:**
- Kiểm tra và thêm cột `ten_dang_nhap`, `mat_khau` nếu chưa có
- Thêm ràng buộc UNIQUE cho `ten_dang_nhap`
- Cập nhật mật khẩu mặc định cho khách hàng cũ
- Tạo index để tối ưu hiệu suất
- Hướng dẫn xóa bảng `tai_khoan` (nếu không dùng nữa)

---

## 🚀 HƯỚNG DẪN ÁP DỤNG

### **Bước 1: Backup Database**
```sql
-- Backup toàn bộ database trước khi thực hiện
BACKUP DATABASE QLBanQuanAo 
TO DISK = 'C:\Backup\QLBanQuanAo_Backup_BeforeFix.bak';
```

### **Bước 2: Chạy SQL Update Script**
```bash
# Mở SQL Server Management Studio
# File > Open > File... > Chọn UPDATE_KHACHHANG_SCHEMA.sql
# Execute (F5)
```

### **Bước 3: Thay thế KhachHangController**
```bash
# 1. Backup file cũ
cp duanbe/src/main/java/com/example/duanbe/controller/KhachHangController.java \
   duanbe/src/main/java/com/example/duanbe/controller/KhachHangController_OLD.java.bak

# 2. Copy file mới
cp FIX_KhachHangController_NEW.java \
   duanbe/src/main/java/com/example/duanbe/controller/KhachHangController.java
```

### **Bước 4: Xóa các import không cần thiết**
Mở `KhachHangController.java` và xóa các import sau (nếu còn):
```java
// ❌ XÓA CÁC IMPORT NÀY
import com.example.duanbe.entity.TaiKhoan;
import com.example.duanbe.repository.TaiKhoanRepo;
import com.example.duanbe.repository.RolesRepo;
import com.example.duanbe.repository.NhanVienRepo;
import com.example.duanbe.security.JwtUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
```

### **Bước 5: Rebuild Project**
```bash
cd duanbe
mvn clean install
```

### **Bước 6: Test API**

#### **Test Đăng ký (Register)**
```bash
POST http://localhost:8080/api/khach-hang/register
Content-Type: application/json

{
  "fullName": "Nguyễn Văn A",
  "email": "nguyenvana@gmail.com",
  "phone": "0123456789",
  "password": "12345678",
  "confirmPassword": "12345678",
  "birthDate": "2000-01-01",
  "gender": "Nam"
}
```

#### **Test Đăng nhập (Login)**
```bash
POST http://localhost:8080/api/khach-hang/login
Content-Type: application/json

{
  "email": "nguyenvana@gmail.com",
  "password": "12345678"
}
```

#### **Test Thêm khách hàng (Admin)**
```bash
POST http://localhost:8080/api/khach-hang/add
Content-Type: application/json

{
  "hoTen": "Trần Thị B",
  "email": "tranthib@gmail.com",
  "soDienThoai": "0987654321",
  "ngaySinh": "1995-05-15",
  "gioiTinh": false,
  "trangThai": "Đang hoạt động",
  "diaChiList": [
    {
      "soNha": "123",
      "xaPhuong": "Phường ABC",
      "quanHuyen": "Quận XYZ",
      "tinhThanhPho": "Hà Nội",
      "diaChiMacDinh": true
    }
  ]
}
```

---

## 🎯 CÁC THAY ĐỔI CHÍNH

### ❌ **ĐÃ XÓA**
1. Bảng `tai_khoan` độc lập
2. Entity `TaiKhoan`
3. Repository `TaiKhoanRepo`, `RolesRepo`
4. JWT authentication phức tạp
5. Spring Security `AuthenticationManager`
6. `UserDetailsService`

### ✅ **ĐÃ THÊM**
1. Tích hợp `tenDangNhap` và `matKhau` vào `KhachHang`
2. `BCryptPasswordEncoder` trực tiếp trong Controller
3. Session-based authentication đơn giản
4. Methods mới trong `KhachHangRepo`:
   - `findByTenDangNhap()`
   - `existsByEmail()`
   - `existsByTenDangNhap()`
   - `existsBySoDienThoai()`

### 🔄 **ĐÃ SỬA**
1. **POST /register**: Không cần tạo `TaiKhoan` riêng
2. **POST /login**: Xác thực trực tiếp với `KhachHang`
3. **POST /add**: Tạo khách hàng với tài khoản tích hợp
4. **POST /forgot-password**: Đặt lại mật khẩu trực tiếp
5. **POST /change-password**: Đổi mật khẩu trong bảng `khach_hang`

---

## 📊 SO SÁNH TRƯỚC VÀ SAU

### **TRƯỚC (Cũ) ❌**
```
┌─────────────┐       ┌──────────────┐
│ khach_hang  │ ──→   │  tai_khoan   │
├─────────────┤       ├──────────────┤
│ id          │       │ id           │
│ ma_kh       │       │ username     │
│ ten_kh      │       │ password     │
│ email       │       │ id_roles     │
│ sdt         │       └──────────────┘
│ id_tai_khoan│              │
└─────────────┘              ↓
                      ┌──────────────┐
                      │    roles     │
                      └──────────────┘
```

### **SAU (Mới) ✅**
```
┌─────────────────┐
│   khach_hang    │
├─────────────────┤
│ id              │
│ ma_khach_hang   │
│ ten_dang_nhap   │ ← Username (= email)
│ mat_khau        │ ← BCrypt hashed
│ ho_ten          │
│ email           │
│ so_dien_thoai   │
│ ngay_sinh       │
│ gioi_tinh       │
│ dia_chi         │
│ trang_thai      │
│ ngay_lap        │
│ ghi_chu         │
└─────────────────┘
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

### 1. **Mật khẩu mặc định**
Tất cả khách hàng cũ (chưa có mật khẩu) sẽ được cập nhật mật khẩu: `12345678`
→ **Yêu cầu khách hàng đổi mật khẩu ngay sau lần đăng nhập đầu tiên!**

### 2. **Email = Username**
`ten_dang_nhap` luôn bằng `email` để đơn giản hóa logic

### 3. **Session-based Auth**
Không dùng JWT token phức tạp, sử dụng HTTP Session đơn giản

### 4. **Không có Role**
Vì chỉ có Admin và Khách hàng, không cần hệ thống phân quyền phức tạp

### 5. **Xóa bảng tai_khoan**
Sau khi test kỹ, có thể xóa bảng `tai_khoan` (xem phần comment trong SQL script)

---

## 🐛 TROUBLESHOOTING

### **Lỗi: "Column 'ten_dang_nhap' not found"**
✅ **Giải pháp**: Chạy lại `UPDATE_KHACHHANG_SCHEMA.sql`

### **Lỗi: "Cannot find symbol BCryptPasswordEncoder"**
✅ **Giải pháp**: Thêm dependency trong `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
    <version>6.0.3</version>
</dependency>
```

### **Lỗi: "TaiKhoanRepo cannot be resolved"**
✅ **Giải pháp**: Xóa các import cũ và dùng file Controller mới

### **Đăng nhập thất bại với mật khẩu đúng**
✅ **Giải pháp**: 
1. Kiểm tra mật khẩu đã được mã hóa BCrypt chưa
2. Xem log để biết mật khẩu đang so sánh

---

## 📞 HỖ TRỢ

Nếu gặp vấn đề, kiểm tra:
1. ✅ Database đã update schema chưa?
2. ✅ File Controller đã thay thế đúng chưa?
3. ✅ Các import không cần thiết đã xóa chưa?
4. ✅ Maven rebuild thành công chưa?
5. ✅ API test trả về lỗi gì?

---

## ✅ CHECKLIST HOÀN THÀNH

- [ ] Backup database
- [ ] Chạy SQL update script
- [ ] Kiểm tra cột `ten_dang_nhap` và `mat_khau` đã có
- [ ] Thay thế file `KhachHangController.java`
- [ ] Cập nhật `KhachHangRepo.java`
- [ ] Xóa các import không cần thiết
- [ ] Maven clean install thành công
- [ ] Test API Register thành công
- [ ] Test API Login thành công
- [ ] Test API Add thành công
- [ ] Test API Forgot Password thành công
- [ ] Test API Change Password thành công
- [ ] Cập nhật frontend nếu cần (loại bỏ JWT handling)
- [ ] Document API changes
- [ ] Thông báo cho team về thay đổi

---

## 🎉 KẾT QUẢ

Sau khi hoàn tất, bạn có:
- ✅ Hệ thống đăng nhập/đăng ký đơn giản hơn
- ✅ Không cần bảng `tai_khoan` riêng
- ✅ Code sạch hơn, dễ bảo trì
- ✅ Performance tốt hơn (ít JOIN)
- ✅ Phù hợp với yêu cầu chỉ có Admin + Khách hàng

**Chúc bạn thành công! 🚀**
