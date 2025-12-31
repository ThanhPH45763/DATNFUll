                                    # API: Update Hóa Đơn - Cập nhật thông tin cơ bản

## Endpoint
```
PUT /banhang/updateHoaDon
```

## Mô tả
Cập nhật **chỉ thông tin cơ bản** của hóa đơn (thông tin khách hàng). Các thông tin khác như tổng tiền, voucher, sản phẩm, trạng thái sẽ được **giữ nguyên**.

## Request Body

Gửi đối tượng `HoaDon` dưới dạng JSON:

```json
{
  "id_hoa_don": 29,
  "ho_ten": "Nguyễn Văn A",
  "email": "nguyenvana@example.com",
  "sdt": "0987654321",
  "dia_chi": "123 Đường ABC, Quận 1, TP.HCM"
}
```

### Trường bắt buộc
- `id_hoa_don` (Integer): **BẮT BUỘC** - ID của hóa đơn cần cập nhật

### Các trường được cập nhật
- `ho_ten` (String): Họ tên khách hàng
- `email` (String): Email khách hàng
- `sdt` (String): Số điện thoại khách hàng
- `dia_chi` (String): Địa chỉ khách hàng
- `ngay_sua` (LocalDateTime): **Tự động** set = thời điểm hiện tại

### Các trường KHÔNG bị ảnh hưởng
- Tổng tiền (tong_tien_truoc_giam, tong_tien_sau_giam)
- Voucher (id_voucher)
- Khách hàng (id_khach_hang)
- Phí vận chuyển (phi_van_chuyen)
- Phương thức thanh toán (hinh_thuc_thanh_toan)
- Phương thức nhận hàng (phuong_thuc_nhan_hang)
- Trạng thái (trang_thai)
- Sản phẩm trong hóa đơn
- v.v.

## Response

### Success (200 OK)
Trả về `HoaDonResponse` với đầy đủ thông tin hóa đơn sau khi cập nhật:

```json
{
  "id_hoa_don": 29,
  "ma_hoa_don": "HD123456",
  "ho_ten": "Nguyễn Văn A",
  "email": "nguyenvana@example.com",
  "sdt": "0987654321",
  "dia_chi": "123 Đường ABC, Quận 1, TP.HCM",
  "ngay_sua": "2025-12-31 14:15:30",
  "tong_tien_truoc_giam": 500000,
  "tong_tien_sau_giam": 450000,
  "phi_van_chuyen": 30000,
  "phuong_thuc_nhan_hang": "Giao hàng",
  "hinh_thuc_thanh_toan": "Online",
  "trang_thai": "Chờ xác nhận",
  "id_voucher": 5,
  "ma_voucher": "SALE10",
  ...
}
```

### Error (400 Bad Request)
Khi thiếu `id_hoa_don`:
```json
{
  "status": 400,
  "error": "Bad Request"
}
```

### Error (404 Not Found)
Khi không tìm thấy hóa đơn với ID đã cho:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Không tìm thấy hóa đơn với ID: 999"
}
```

## Ví dụ sử dụng

### JavaScript/Axios
```javascript
const updateInvoiceInfo = async (invoiceId, customerInfo) => {
  try {
    const response = await axios.put('/banhang/updateHoaDon', {
      id_hoa_don: invoiceId,
      ho_ten: customerInfo.name,
      email: customerInfo.email,
      sdt: customerInfo.phone,
      dia_chi: customerInfo.address
    });
    
    console.log('Cập nhật thành công:', response.data);
    // response.data chứa full HoaDonResponse
    return response.data;
  } catch (error) {
    console.error('Lỗi cập nhật:', error.response?.data);
    throw error;
  }
};

// Sử dụng
updateInvoiceInfo(29, {
  name: 'Nguyễn Văn A',
  email: 'nguyenvana@example.com',
  phone: '0987654321',
  address: '123 Đường ABC, Quận 1, TP.HCM'
});
```

### cURL
```bash
curl -X PUT "http://localhost:8080/banhang/updateHoaDon" \
  -H "Content-Type: application/json" \
  -d '{
    "id_hoa_don": 29,
    "ho_ten": "Nguyễn Văn A",
    "email": "nguyenvana@example.com",
    "sdt": "0987654321",
    "dia_chi": "123 Đường ABC, Quận 1, TP.HCM"
  }'
```

## Lưu ý quan trọng

1. **Không ảnh hưởng sản phẩm**: API này chỉ cập nhật thông tin khách hàng, không thay đổi sản phẩm trong hóa đơn
2. **Không tính lại tổng tiền**: Vì chỉ đổi thông tin khách hàng, tổng tiền giữ nguyên
3. **Tự động cập nhật ngày sửa**: Không cần truyền `ngay_sua`, hệ thống tự động set
4. **Giữ nguyên các field khác**: Chỉ 4 field được update (ho_ten, email, sdt, dia_chi), các field khác trong object input sẽ bị bỏ qua
5. **Trả về full data**: Response luôn chứa thông tin đầy đủ của hóa đơn để FE cập nhật UI

## Use Case

API này phù hợp khi:
- ✅ Khách hàng đổi số điện thoại
- ✅ Khách hàng đổi địa chỉ giao hàng
- ✅ Sửa lỗi chính tả trong tên khách hàng
- ✅ Cập nhật email liên hệ

API này **KHÔNG** phù hợp khi:
- ❌ Thêm/xóa sản phẩm → Dùng `/themSPHDMoi`, `/xoaSPHD`
- ❌ Thay đổi voucher → Dùng `/apply-voucher`
- ❌ Đổi phương thức nhận hàng → Dùng `/setTrangThaiNhanHang`
- ❌ Thay đổi trạng thái đơn hàng → Dùng `/trangThaiDonHang`
