# 🔧 HƯỚNG DẪN SỬA LỖI LOGIC KHÁCH HÀNG

## 📋 PHÂN TÍCH VẤN ĐỀ

### ❌ **Logic CŨ (SAI)**:
- Khách hàng có quan hệ với bảng `tai_khoan` riêng biệt
- Controller tham chiếu đến `TaiKhoan`, `TaiKhoanRepo`, `RolesRepo`
- Phức tạp và không cần thiết

### ✅ **Logic MỚI (ĐÚNG)**:
- Bảng `khach_hang` đã có sẵn:
  - `ten_dang_nhap` (username)
  - `mat_khau` (password đã mã hóa)
- Không cần bảng `tai_khoan` riêng
- Đơn giản hóa logic

---

## 🗄️ CẤU TRÚC BẢNG (Đã có sẵn)

```sql
CREATE TABLE khach_hang (
    id_khach_hang int IDENTITY(1,1) PRIMARY KEY,
    ma_khach_hang nvarchar(50) NOT NULL UNIQUE,
    ten_dang_nhap nvarchar(100) NOT NULL UNIQUE,
    mat_khau nvarchar(255) NOT NULL,
    email nvarchar(255) UNIQUE,
    so_dien_thoai nvarchar(20) UNIQUE,
    ho_ten nvarchar(255),
    gioi_tinh nvarchar(10),
    ngay_sinh date,
    dia_chi nvarchar(500),
    ngay_lap datetime DEFAULT getdate(),
    trang_thai nvarchar(20) DEFAULT 'HOAT_DONG',
    ghi_chu nvarchar(500)
);
```

---

## 🔨 CÁC THAY ĐỔI CẦN THỰC HIỆN

### 1️⃣ **Entity KhachHang** ✅ (Đã đúng)
File đã có đầy đủ:
- `tenDangNhap`
- `matKhau`
Không cần sửa gì!

### 2️⃣ **KhachHangRepo** 
Cần thêm queries:

```java
// Tìm theo tên đăng nhập
Optional<KhachHang> findByTenDangNhap(String tenDangNhap);

// Kiểm tra tồn tại email (cho register)
boolean existsByEmail(String email);

// Kiểm tra tồn tại số điện thoại
boolean existsBySoDienThoai(String soDienThoai);

// Kiểm tra tồn tại tên đăng nhập
boolean existsByTenDangNhap(String tenDangNhap);
```

### 3️⃣ **KhachHangController** 
Cần loại bỏ TẤT CẢ tham chiếu đến:
- ❌ `TaiKhoan`
- ❌ `TaiKhoanRepo`
- ❌ `RolesRepo`
- ❌ `NhanVienRepo`
- ❌ `passwordEncoder`
- ❌ `authenticationManager`
- ❌ `userDetailsService`
- ❌ `jwtUtil`

Thay thế bằng:
- ✅ Sử dụng BCryptPasswordEncoder trực tiếp
- ✅ Xác thực bằng cách so sánh password trong KhachHang
- ✅ Tạo JWT token đơn giản (hoặc dùng session)

---

## 📝 CODE MẪU CẦN SỬA

### **POST /register** - Đăng ký khách hàng mới

```java
@PostMapping("/register")
public ResponseEntity<Map<String, Object>> registerKhachHang(
        @Valid @RequestBody RegisterRequest registerRequest,
        BindingResult result) {
    
    Map<String, Object> response = new HashMap<>();
    
    // Validation
    if (result.hasErrors()) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : result.getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        response.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }
    
    // Kiểm tra confirm password
    if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
        response.put("error", "Mật khẩu xác nhận không khớp");
        return ResponseEntity.badRequest().body(response);
    }
    
    // Kiểm tra tuổi >= 14
    LocalDate ngaySinh = registerRequest.getBirthDate()
        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    int tuoi = Period.between(ngaySinh, LocalDate.now()).getYears();
    if (tuoi < 14) {
        response.put("error", "Bạn phải đủ 14 tuổi để đăng ký");
        return ResponseEntity.badRequest().body(response);
    }
    
    try {
        // Kiểm tra email/username đã tồn tại
        if (khachHangRepo.existsByEmail(registerRequest.getEmail())) {
            response.put("error", "Email đã được sử dụng!");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (khachHangRepo.existsByTenDangNhap(registerRequest.getEmail())) {
            response.put("error", "Tên đăng nhập đã tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Tạo mã khách hàng
        String maKhachHang = generateMaKhachHang();
        
        // Mã hóa mật khẩu
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(registerRequest.getPassword());
        
        // Tạo khách hàng
        KhachHang khachHang = new KhachHang();
        khachHang.setMaKhachHang(maKhachHang);
        khachHang.setTenDangNhap(registerRequest.getEmail()); // Email là username
        khachHang.setMatKhau(hashedPassword);
        khachHang.setHoTen(registerRequest.getFullName());
        khachHang.setSoDienThoai(registerRequest.getPhone());
        khachHang.setEmail(registerRequest.getEmail());
        khachHang.setNgaySinh(registerRequest.getBirthDate());
        khachHang.setTrangThai("Đang hoạt động");
        khachHang.setNgayLap(LocalDateTime.now());
        
        // Xử lý giới tính
        if ("Nam".equals(registerRequest.getGender())) {
            khachHang.setGioiTinh(true);
        } else if ("Nữ".equals(registerRequest.getGender())) {
            khachHang.setGioiTinh(false);
        }
        
        khachHang = khachHangRepo.save(khachHang);
        
        // Gửi email chào mừng (optional)
        sendWelcomeEmail(khachHang, registerRequest.getPassword());
        
        response.put("success", true);
        response.put("message", "Đăng ký thành công!");
        response.put("khachHang", khachHang);
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        response.put("error", "Có lỗi xảy ra: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

### **POST /login** - Đăng nhập

```java
@PostMapping("/login")
public ResponseEntity<Map<String, Object>> login(
        @Valid @RequestBody LoginRequest loginRequest,
        BindingResult result,
        HttpServletRequest request) {
    
    Map<String, Object> response = new HashMap<>();
    
    if (result.hasErrors()) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : result.getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        response.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }
    
    try {
        // Tìm khách hàng theo email
        Optional<KhachHang> khachHangOpt = khachHangRepo.findByTenDangNhap(loginRequest.getEmail());
        
        if (!khachHangOpt.isPresent()) {
            response.put("error", "Tài khoản không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }
        
        KhachHang khachHang = khachHangOpt.get();
        
        // Kiểm tra trạng thái
        if (!"Đang hoạt động".equals(khachHang.getTrangThai())) {
            response.put("error", "Tài khoản đã bị ngừng hoạt động!");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Kiểm tra mật khẩu
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(loginRequest.getPassword(), khachHang.getMatKhau())) {
            response.put("error", "Mật khẩu không đúng!");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Lưu lịch sử đăng nhập (nếu cần)
        String ipAddress = request.getRemoteAddr();
        // saveLoginHistory(khachHang, ipAddress);
        
        // Trả về thông tin đăng nhập
        response.put("success", true);
        response.put("message", "Đăng nhập thành công!");
        response.put("khachHang", khachHang);
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        response.put("error", "Có lỗi xảy ra: " + e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
```

### **POST /add** - Thêm khách hàng (Admin)

```java
@PostMapping("/add")
public ResponseEntity<Map<String, Object>> addKhachHang(
        @Valid @RequestBody KhachHangRequest request,
        BindingResult result) {
    
    Map<String, Object> response = new HashMap<>();
    
    if (result.hasErrors()) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : result.getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        response.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }
    
    try {
        // Kiểm tra trùng lặp
        if (khachHangRepo.existsByEmail(request.getEmail())) {
            response.put("error", "Email đã được sử dụng!");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Sinh mã khách hàng
        String maKhachHang = request.getMaKhachHang();
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            maKhachHang = generateMaKhachHang();
        } else if (khachHangRepo.findByMaKhachHang(maKhachHang).isPresent()) {
            response.put("error", "Mã khách hàng đã tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Sinh mật khẩu ngẫu nhiên
        String randomPassword = generateRandomPassword();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(randomPassword);
        
        // Tạo khách hàng
        KhachHang khachHang = new KhachHang();
        BeanUtils.copyProperties(request, khachHang);
        khachHang.setMaKhachHang(maKhachHang);
        khachHang.setTenDangNhap(request.getEmail()); // Email là username
        khachHang.setMatKhau(hashedPassword);
        khachHang.setNgayLap(LocalDateTime.now());
        khachHang = khachHangRepo.save(khachHang);
        
        // Lưu địa chỉ
        if (request.getDiaChiList() != null && !request.getDiaChiList().isEmpty()) {
            for (KhachHangRequest.DiaChiRequest diaChiReq : request.getDiaChiList()) {
                DiaChiKhachHang diaChiKhachHang = new DiaChiKhachHang();
                diaChiKhachHang.setKhachHang(khachHang);
                BeanUtils.copyProperties(diaChiReq, diaChiKhachHang);
                diaChiKhachHangRepo.save(diaChiKhachHang);
            }
        }
        
        // Gửi email
        sendWelcomeEmail(khachHang, randomPassword);
        
        response.put("success", true);
        response.put("message", "Thêm khách hàng thành công!");
        response.put("khachHang", khachHang);
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        response.put("error", "Có lỗi xảy ra: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

---

## 🛠️ CÁC FILE CẦN TẠO/SỬA

1. ✅ `KhachHangRepo.java` - Thêm methods
2. ✅ `KhachHangController.java` - Sửa toàn bộ logic
3. ✅ Xóa các import không cần thiết
4. ✅ Thêm BCryptPasswordEncoder

---

## ⚠️ LƯU Ý QUAN TRỌNG

1. **Mã hóa mật khẩu**: Luôn dùng BCryptPasswordEncoder
2. **Username = Email**: Đơn giản hóa logic
3. **Không cần JWT phức tạp**: Dùng session hoặc JWT đơn giản
4. **Xóa bảng `tai_khoan`**: Nếu không dùng cho nhân viên nữa

---

## 🎯 TIẾP THEO

Bạn muốn tôi:
1. ✅ Sửa KhachHangRepo?
2. ✅ Sửa KhachHangController?
3. ✅ Tạo file SQL update?
4. ✅ Tất cả các file trên?

