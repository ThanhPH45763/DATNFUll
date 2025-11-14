# Debug lỗi thêm khách hàng - HTTP 500

## Kết quả kiểm tra

✅ **Backend API hoạt động bình thường** khi test trực tiếp với curl.

Response thành công:
```json
{
  "emailMessage": "Email chào mừng đã được gửi thành công!",
  "khachHang": {...},
  "successMessage": "Thêm khách hàng thành công!"
}
```

## Nguyên nhân có thể:

### 1. Frontend gửi thiếu trường required
Kiểm tra các trường bắt buộc trong entity `KhachHang`:
- ✅ `maKhachHang` (có thể auto-generate)
- ✅ `hoTen` - **REQUIRED** - @NotBlank
- ✅ `email` - **REQUIRED** - @NotBlank, @Email
- ✅ `soDienThoai` - **REQUIRED** - @NotBlank, @Pattern(regexp = "^0\\d{9}$")
- ✅ `ngaySinh` - **REQUIRED** - @NotNull, @PastOrPresent
- ✅ `trangThai` - **REQUIRED** - @NotBlank

### 2. Format dữ liệu không đúng

#### Số điện thoại:
- Phải bắt đầu bằng 0
- Đúng 10 chữ số
- Pattern: `^0\\d{9}$`
- ❌ SAI: "123456789", "84123456789", "0123"
- ✅ ĐÚNG: "0123456789"

#### Ngày sinh:
- Format: `yyyy-MM-dd` hoặc ISO 8601
- ❌ SAI: "01/01/2000", "2000.01.01"
- ✅ ĐÚNG: "2000-01-01"

#### Email:
- Phải đúng định dạng email
- ❌ SAI: "test", "test@"
- ✅ ĐÚNG: "test@example.com"

### 3. Địa chỉ (diaChiList)

Nếu gửi địa chỉ, cần đủ các trường:
```json
{
  "diaChiList": [
    {
      "soNha": "123",
      "xaPhuong": "Phường 1",
      "quanHuyen": "Quận 1",
      "tinhThanhPho": "TP.HCM",
      "diaChiMacDinh": true
    }
  ]
}
```

## Cách debug:

### Bước 1: Kiểm tra request payload trong Browser
1. Mở **DevTools** (F12)
2. Vào tab **Network**
3. Thử thêm khách hàng
4. Click vào request `/api/khach-hang/add`
5. Xem tab **Payload** hoặc **Request** để thấy data đã gửi

### Bước 2: Xem response error
Trong tab **Response**, xem message lỗi chi tiết từ backend.

### Bước 3: Kiểm tra validation errors
Controller có trả về `fieldErrors` nếu validation fail:
```json
{
  "fieldErrors": {
    "soDienThoai": "Số điện thoại phải bắt đầu bằng 0 và đúng 10 chữ số",
    "email": "Email không đúng định dạng"
  }
}
```

## Ví dụ request ĐÚNG:

```javascript
// Frontend code
const khachHangData = {
  maKhachHang: "", // Có thể để trống, backend sẽ auto-generate
  hoTen: "Nguyễn Văn A",
  email: "nguyenvana@gmail.com",
  soDienThoai: "0987654321",
  ngaySinh: "1990-05-15",
  gioiTinh: true, // true = Nam, false = Nữ
  trangThai: "Đang hoạt động",
  ghiChu: "Khách hàng VIP", // Optional
  diaChiList: [
    {
      soNha: "123 Nguyễn Trãi",
      xaPhuong: "Phường Bến Thành",
      quanHuyen: "Quận 1",
      tinhThanhPho: "TP.HCM",
      diaChiMacDinh: true
    }
  ]
};

axios.post('/api/khach-hang/add', khachHangData)
  .then(response => console.log(response.data))
  .catch(error => {
    console.error('Error details:', error.response?.data);
  });
```

## Checklist:

- [ ] Kiểm tra payload trong DevTools Network tab
- [ ] Kiểm tra số điện thoại đúng format (0xxxxxxxxx)
- [ ] Kiểm tra email đúng format
- [ ] Kiểm tra ngày sinh đúng format (yyyy-MM-dd)
- [ ] Kiểm tra trangThai có giá trị (không null/undefined)
- [ ] Nếu có địa chỉ, kiểm tra đủ các trường
- [ ] Xem response error message chi tiết

## Test nhanh:

Dùng lệnh này để test API:
```bash
curl -X POST http://localhost:8080/api/khach-hang/add \
  -H "Content-Type: application/json" \
  -d '{
    "hoTen": "Test Customer",
    "email": "test123@example.com",
    "soDienThoai": "0123456789",
    "ngaySinh": "2000-01-01",
    "gioiTinh": true,
    "trangThai": "Đang hoạt động"
  }'
```

