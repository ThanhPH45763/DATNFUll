# SỬA LỖI TÍNH TIỀN KHI BÁN HÀNG

## 🐛 VẤN ĐỀ
Khi thêm sản phẩm từ thanh tìm kiếm vào giỏ hàng, tổng tiền hàng bị tính **SAI GẤP 5 LẦN**:
- Sản phẩm giá: 8,000đ × 5 = 40,000đ
- **Hiển thị SAI: 200,000đ** (40,000 × 5)

## 🔍 NGUYÊN NHÂN

### LỖI NGHIÊM TRỌNG: NHÂN 2 LẦN SỐ LƯỢNG

Trong database, trường `don_gia` của bảng `hoa_don_chi_tiet` **ĐÃ LƯU TỔNG TIỀN**:
```java
// Khi lưu vào DB
chiTiet.setDon_gia(donGiaLe.multiply(BigDecimal.valueOf(soLuong)));
// don_gia = 8,000 × 5 = 40,000 (ĐÃ LÀ TỔNG)
```

Nhưng khi tính tổng tiền, code lại **NHÂN THÊM** với số lượng:

#### 1. Hàm `capNhatVoucher` (dòng 609-612)
```java
// ❌ SAI
BigDecimal tongTienSanPham = dsSanPham.stream()
    .map(ct -> ct.getDon_gia().multiply(BigDecimal.valueOf(ct.getSo_luong())))
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// don_gia = 40,000 (đã là tổng)
// Nhân thêm với so_luong = 5
// Kết quả: 40,000 × 5 = 200,000 ❌ SAI!
```

#### 2. Hàm `setTrangThaiNhanHang` (dòng 165-167)
```java
// ❌ SAI
BigDecimal tongTienHang = chiTietList.stream()
    .map(ct -> ct.getDon_gia().multiply(BigDecimal.valueOf(ct.getSo_luong())))
    .reduce(BigDecimal.ZERO, BigDecimal::add);
// Kết quả: 40,000 × 5 = 200,000 ❌ SAI!
```

#### 3. Hàm `themSPHDMoi` (dòng 441-448)
```java
// ❌ SAI - CỘNG 2 LẦN phí vận chuyển
BigDecimal tongTien = danhSachChiTiet.stream()
    .map(HoaDonChiTiet::getDon_gia)
    .reduce(BigDecimal.ZERO, BigDecimal::add)
    .add(hoaDon.getPhi_van_chuyen());  // ❌ CỘNG LẦN 1

hoaDon.setTong_tien_truoc_giam(tongTien);
hoaDonRepo.save(hoaDon);

capNhatVoucher(idHD);  // ❌ CỘNG THÊM LẦN 2 phí VC
```

#### 4. Hàm `capNhatTongTienVaVoucher` (dòng 582-594)
```java
// ❌ SAI - Trùng lặp logic, gây CỘNG 2 LẦN phí VC
```

## ✅ GIẢI PHÁP

### Nguyên tắc:
- **`don_gia` trong DB = tổng tiền (giá_lẻ × số_lượng)**
- **KHÔNG ĐƯỢC NHÂN THÊM** với `so_luong` khi tính tổng
- **CHỈ GỌI `capNhatVoucher()`** để tính tổng tiền

### Thay đổi:

#### 1. Sửa `capNhatVoucher` (dòng 609-615)
```java
// ✅ ĐÚNG: Chỉ SUM don_gia, KHÔNG nhân thêm
BigDecimal tongTienSanPham = dsSanPham.stream()
    .filter(ct -> ct.getDon_gia() != null)
    .map(ct -> ct.getDon_gia())  // ✅ Chỉ lấy don_gia
    .reduce(BigDecimal.ZERO, BigDecimal::add);

BigDecimal phiVanChuyen = Optional.ofNullable(hoaDon.getPhi_van_chuyen()).orElse(BigDecimal.ZERO);
BigDecimal tongTruocGiam = tongTienSanPham.add(phiVanChuyen);
hoaDon.setTong_tien_truoc_giam(tongTruocGiam);
```

#### 2. Sửa `setTrangThaiNhanHang` (dòng 164-172)
```java
// ✅ ĐÚNG: Chỉ SUM don_gia
BigDecimal tongTienHang = chiTietList.stream()
    .map(HoaDonChiTiet::getDon_gia)  // ✅ Chỉ lấy don_gia
    .reduce(BigDecimal.ZERO, BigDecimal::add);

BigDecimal tongTienTruocGiam = tongTienHang.add(pvc);
hoaDon.setTong_tien_truoc_giam(tongTienTruocGiam);
hoaDonRepo.save(hoaDon);

capNhatVoucher(idHD);  // ✅ Tính lại tổng tiền sau giảm
```

#### 3. Sửa `themSPHDMoi` (dòng 436-440)
```java
// ✅ ĐÚNG: Chỉ gọi capNhatVoucher để tính toàn bộ
hoaDonChiTietRepo.save(chiTiet);
capNhatVoucher(idHD);
```

#### 4. Sửa `capNhatTongTienVaVoucher` (dòng 582-584)
```java
// ✅ ĐÚNG: Chỉ gọi capNhatVoucher
private void capNhatTongTienVaVoucher(HoaDon hoaDon) {
    capNhatVoucher(hoaDon.getId_hoa_don());
}
```

## 📊 KẾT QUẢ

### Trước khi sửa:
```
Sản phẩm: 8,000đ × 5 = 40,000đ
don_gia trong DB = 40,000

❌ Tính tổng: 40,000 × 5 = 200,000đ (NHÂN 2 LẦN!)
❌ Cộng phí VC 2 lần: 200,000 + 30,000 + 30,000 = 260,000đ
```

### Sau khi sửa:
```
Sản phẩm: 8,000đ × 5 = 40,000đ
don_gia trong DB = 40,000

✅ Tính tổng: 40,000 (ĐÚNG!)
✅ Cộng phí VC 1 lần: 40,000 + 30,000 = 70,000đ (ĐÚNG!)
```

## 🔑 ĐIỂM QUAN TRỌNG

### Cách lưu `don_gia` trong DB:
```java
// Khi thêm/cập nhật sản phẩm
chiTiet.setDon_gia(donGiaLe.multiply(BigDecimal.valueOf(soLuong)));
// don_gia = giá_lẻ × số_lượng = TỔNG TIỀN
```

### Cách tính tổng tiền hóa đơn:
```java
// ✅ ĐÚNG: Chỉ SUM don_gia
BigDecimal tongTien = chiTietList.stream()
    .map(HoaDonChiTiet::getDon_gia)  // ✅ ĐÃ LÀ TỔNG
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// ❌ SAI: KHÔNG ĐƯỢC nhân thêm với so_luong
BigDecimal tongTien = chiTietList.stream()
    .map(ct -> ct.getDon_gia().multiply(BigDecimal.valueOf(ct.getSo_luong())))
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```

### Xác nhận từ code:
```java
// Dòng 524 trong hàm giamSPHD
BigDecimal giaLe = chiTiet.getDon_gia().divide(BigDecimal.valueOf(chiTiet.getSo_luong()), 2, RoundingMode.HALF_UP);
// Phải CHIA cho so_luong để lấy giá_lẻ → XÁC NHẬN don_gia là TỔNG TIỀN
```

## 📁 FILES THAY ĐỔI
- `duanbe/src/main/java/com/example/duanbe/controller/BanHangController.java`

## ✅ HOÀN TẤT
Đã sửa **4 LỖI NGHIÊM TRỌNG**:
1. ✅ Nhân 2 lần số lượng trong `capNhatVoucher`
2. ✅ Nhân 2 lần số lượng trong `setTrangThaiNhanHang`
3. ✅ Cộng 2 lần phí vận chuyển trong `themSPHDMoi`
4. ✅ Logic trùng lặp trong `capNhatTongTienVaVoucher`
