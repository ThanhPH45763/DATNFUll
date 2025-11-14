# HƯỚNG DẪN TỰ ĐỘNG TÍNH PHÍ VẬN CHUYỂN

## Tính năng đã implement

### 1. **Tự động tính phí vận chuyển theo địa chỉ**
Khi người dùng chọn địa chỉ giao hàng (Tỉnh/Thành phố và Quận/Huyện), hệ thống sẽ:
- Tự động tính phí vận chuyển dựa trên bảng giá cố định
- Hiển thị phí vận chuyển dự tính ngay trên form
- Tự động cập nhật phí vận chuyển vào hóa đơn

### 2. **Địa chỉ gốc của shop**
```
Số 7 ngõ 324/167 đường Phương Canh
Quận Nam Từ Liêm
Tỉnh Hà Nội
```

### 3. **Bảng giá vận chuyển**

| Khu vực | Phí vận chuyển |
|---------|----------------|
| **Nội thành Hà Nội** | 25,000đ |
| - Quận Ba Đình, Hoàn Kiếm, Đống Đa, Hai Bà Trưng | |
| - Quận Hoàng Mai, Thanh Xuân, Long Biên | |
| - Quận Cầu Giấy, Tây Hồ, Hà Đông | |
| - Quận Bắc Từ Liêm, Nam Từ Liêm | |
| | |
| **Ngoại thành Hà Nội** | 35,000đ |
| - Các huyện: Sóc Sơn, Đông Anh, Gia Lâm, Thanh Trì | |
| - Thường Tín, Phú Xuyên, Ứng Hòa, Mỹ Đức | |
| - Thạch Thất, Quốc Oai, Chương Mỹ, v.v. | |
| | |
| **Các tỉnh lân cận** | 50,000đ |
| - Bắc Ninh, Bắc Giang, Hưng Yên, Hải Dương | |
| - Hải Phòng, Vĩnh Phúc, Phú Thọ, Hà Nam | |
| - Nam Định, Thái Bình, Ninh Bình | |
| | |
| **Miền Bắc** | 70,000đ |
| - Lào Cai, Điện Biên, Lai Châu, Sơn La | |
| - Yên Bái, Hòa Bình, Thái Nguyên, Lạng Sơn | |
| - Quảng Ninh, Cao Bằng, Bắc Kạn, v.v. | |
| | |
| **Miền Trung** | 90,000đ |
| - Thanh Hóa, Nghệ An, Hà Tĩnh, Quảng Bình | |
| - Huế, Đà Nẵng, Quảng Nam, Quảng Ngãi | |
| - Bình Định, Phú Yên, Khánh Hòa, v.v. | |
| | |
| **Tây Nguyên** | 100,000đ |
| - Kon Tum, Gia Lai, Đắk Lắk, Đắk Nông, Lâm Đồng | |
| | |
| **TP. Hồ Chí Minh** | 90,000đ |
| | |
| **Miền Nam** | 100,000đ |
| - Đồng Nai, Bình Dương, Long An, Tiền Giang | |
| - Cần Thơ, An Giang, Kiên Giang, v.v. | |
| | |
| **Vùng xa chưa xác định** | 120,000đ |

## Cách sử dụng

### 1. **Nhập thông tin khách hàng**
- Vào trang **Bán hàng**
- Nhập thông tin khách hàng: Tên, SĐT, Email

### 2. **Chọn địa chỉ giao hàng**
- Chọn **Tỉnh/Thành phố**
- Chọn **Quận/Huyện** 
- ➡️ **Phí vận chuyển sẽ tự động tính và hiển thị**

### 3. **Xác nhận**
- Phí vận chuyển sẽ tự động được cập nhật vào hóa đơn
- Kiểm tra lại phí ở phần thanh toán

## Tùy chỉnh bảng giá

Nếu muốn thay đổi giá vận chuyển, chỉnh sửa file:
```
DuAnMauFE/src/utils/shippingFeeCalculator.js
```

### Ví dụ: Thay đổi phí nội thành Hà Nội từ 25,000đ thành 30,000đ

```javascript
'noithanh_hanoi': {
    fee: 30000,  // Thay đổi ở đây
    districts: [
        'Quận Ba Đình',
        // ...
    ]
}
```

### Thêm tỉnh/huyện mới

```javascript
'lancan': {
    fee: 50000,
    provinces: [
        'Tỉnh Bắc Ninh',
        'Tỉnh Mới ABC',  // Thêm tỉnh mới
        // ...
    ]
}
```

## Nâng cấp lên API tính khoảng cách thực tế

### Phương án 1: Sử dụng Google Maps Distance Matrix API

**Ưu điểm:**
- Tính chính xác theo khoảng cách thực tế
- Tính theo nhiều phương thức (xe máy, ô tô, đi bộ)
- Có tính đến tắc đường

**Nhược điểm:**
- Tốn phí (sau 40,000 requests miễn phí/tháng)
- Cần API key

**Cách implement:**

1. Đăng ký API key tại: https://console.cloud.google.com/
2. Enable Distance Matrix API
3. Sửa file `shippingFeeCalculator.js`:

```javascript
export async function calculateShippingFeeByDistance(destination) {
  const SHOP_ADDRESS_FULL = "Số 7 ngõ 324/167 Phương Canh, Nam Từ Liêm, Hà Nội"
  const API_KEY = "YOUR_GOOGLE_API_KEY"
  
  const url = `https://maps.googleapis.com/maps/api/distancematrix/json?origins=${encodeURIComponent(SHOP_ADDRESS_FULL)}&destinations=${encodeURIComponent(destination)}&key=${API_KEY}`
  
  const response = await fetch(url)
  const data = await response.json()
  
  if (data.rows[0].elements[0].status === 'OK') {
    const distanceInKm = data.rows[0].elements[0].distance.value / 1000
    
    // Tính phí theo km (ví dụ: 5000đ/km cho 10km đầu, 3000đ/km tiếp theo)
    if (distanceInKm <= 10) {
      return Math.ceil(distanceInKm * 5000)
    } else {
      return Math.ceil(10 * 5000 + (distanceInKm - 10) * 3000)
    }
  }
  
  return 30000 // Phí mặc định nếu không tính được
}
```

### Phương án 2: Sử dụng Goong API (Việt Nam)

**Ưu điểm:**
- Dữ liệu bản đồ Việt Nam chính xác hơn
- Hỗ trợ tiếng Việt tốt
- Giá rẻ hơn Google Maps

**Cách sử dụng:**
1. Đăng ký tại: https://goong.io/
2. Tương tự Google Maps, thay endpoint:

```javascript
const url = `https://rsapi.goong.io/DistanceMatrix?origins=${encodeURIComponent(origin)}&destinations=${encodeURIComponent(destination)}&vehicle=bike&api_key=${GOONG_API_KEY}`
```

## Lưu ý quan trọng

1. **Phí vận chuyển có thể điều chỉnh thủ công** ở màn hình thanh toán
2. **Chỉ áp dụng khi chọn "Giao hàng"**, không áp dụng cho "Nhận tại cửa hàng"
3. **Phí tính theo khu vực**, không phân biệt khối lượng đơn hàng (có thể bổ sung sau)

## Files đã tạo/sửa

1. ✅ **DuAnMauFE/src/utils/shippingFeeCalculator.js** - Logic tính phí
2. ✅ **formKhachHangBH.vue** - Tự động tính khi chọn địa chỉ
3. ✅ **TheHeader-BanHang.vue** - Cập nhật phí vào hóa đơn
4. ✅ **HoaDonRepo.java** - Mapping field số điện thoại
5. ✅ **HoaDonResponse.java** - Thêm field so_dien_thoai

## Test thử

1. Tạo hóa đơn mới
2. Nhập thông tin khách hàng
3. Chọn Tỉnh: **Hà Nội** → Quận: **Nam Từ Liêm** → Phí: **25,000đ**
4. Chọn Tỉnh: **Bắc Ninh** → Phí: **50,000đ**
5. Chọn Tỉnh: **Điện Biên** → Phí: **70,000đ**
