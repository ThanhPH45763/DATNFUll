# SỬA LỖI TĂNG/GIẢM SỐ LƯỢNG SẢN PHẨM

## 🐛 VẤN ĐỀ
Khi click nút tăng/giảm số lượng sản phẩm trong hóa đơn:
- Hóa đơn có **5 sản phẩm** giá **8,000đ/sp**
- Tổng tiền **PHẢI LÀ**: 40,000đ
- **Hiển thị SAI**: 8,000đ (chỉ tính 1 sản phẩm)

## 🔍 NGUYÊN NHÂN

### Hàm `setSPHD` (dòng 452-511) trong BanHangController.java

**LOGIC SAI:**
```java
@PostMapping("/setSPHD")
public ResponseEntity<?> setSPHD(
        @RequestParam("idHoaDon") Integer idHD,
        @RequestParam("idCTSP") Integer idCTSP,
        @RequestParam("soLuongMoi") Integer soLuongMoi) {
    
    // Lấy giá lẻ (giá 1 sản phẩm)
    BigDecimal donGiaLe = chiTietSanPhamRepo.getAllCTSPKM().stream()
            .filter(ct -> ct.getId_chi_tiet_san_pham().equals(chiTietSP.getId_chi_tiet_san_pham()))
            .map(ct -> BigDecimal.valueOf(ct.getGia_ban()))
            .findFirst()
            .orElse(BigDecimal.ZERO);
    // donGiaLe = 8,000đ
    
    chiTiet.setSo_luong(soLuongMoi);  // 5
    chiTiet.setDon_gia(donGiaLe);     // ❌ SAI: chỉ lưu 8,000đ
    //                                 // ❌ PHẢI LƯU: 8,000 × 5 = 40,000đ
}
```

### HIỆN TƯỢNG:
- Khi tăng/giảm số lượng từ 5 → 6 → 5
- `don_gia` bị ghi đè = **8,000đ** (chỉ giá lẻ)
- Tổng tiền hiển thị = **8,000đ** thay vì **40,000đ**

## ✅ GIẢI PHÁP

### Sửa dòng 499 trong `setSPHD`:
```java
chiTiet.setSo_luong(soLuongMoi);
// don_gia phải lưu TỔNG TIỀN (giá_lẻ × số_lượng)
chiTiet.setDon_gia(donGiaLe.multiply(BigDecimal.valueOf(soLuongMoi)));
```

### SO SÁNH:

#### ❌ TRƯỚC KHI SỬA:
```java
chiTiet.setSo_luong(5);
chiTiet.setDon_gia(8000);  // ❌ Chỉ lưu giá lẻ

// Lưu vào DB:
// - so_luong = 5
// - don_gia = 8,000 ❌ SAI!
```

#### ✅ SAU KHI SỬA:
```java
chiTiet.setSo_luong(5);
chiTiet.setDon_gia(8000 × 5);  // ✅ Lưu tổng tiền

// Lưu vào DB:
// - so_luong = 5
// - don_gia = 40,000 ✅ ĐÚNG!
```

## 📊 KẾT QUẢ

### Trước khi sửa:
```
Sản phẩm: 8,000đ
Số lượng: 5
Click tăng/giảm số lượng

❌ don_gia trong DB = 8,000đ (chỉ giá lẻ)
❌ Tổng tiền hiển thị = 8,000đ (SAI!)
```

### Sau khi sửa:
```
Sản phẩm: 8,000đ
Số lượng: 5
Click tăng/giảm số lượng

✅ don_gia trong DB = 40,000đ (8,000 × 5)
✅ Tổng tiền hiển thị = 40,000đ (ĐÚNG!)
```

## 🔑 QUY TẮC QUAN TRỌNG

### Trong bảng `hoa_don_chi_tiet`:
- `so_luong`: Số lượng sản phẩm
- `don_gia`: **TỔNG TIỀN** (giá_lẻ × so_luong)

### Khi lưu vào DB:
```java
// ✅ ĐÚNG - Luôn nhân với số lượng
chiTiet.setDon_gia(giaLe.multiply(BigDecimal.valueOf(soLuong)));

// ❌ SAI - Chỉ lưu giá lẻ
chiTiet.setDon_gia(giaLe);
```

### Khi tính tổng tiền hóa đơn:
```java
// ✅ ĐÚNG - Chỉ SUM don_gia (đã là tổng tiền)
BigDecimal tongTien = chiTietList.stream()
    .map(HoaDonChiTiet::getDon_gia)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// ❌ SAI - KHÔNG nhân thêm với so_luong
BigDecimal tongTien = chiTietList.stream()
    .map(ct -> ct.getDon_gia().multiply(BigDecimal.valueOf(ct.getSo_luong())))
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```

## 📁 FILES THAY ĐỔI
- `duanbe/src/main/java/com/example/duanbe/controller/BanHangController.java` (dòng 499-500)

## ✅ HOÀN TẤT
Đã sửa lỗi **không lưu tổng tiền** khi tăng/giảm số lượng sản phẩm trong hóa đơn.
