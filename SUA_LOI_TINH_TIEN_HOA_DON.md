# SỬA LỖI LOGIC TÍNH TIỀN HÓA ĐƠN

## ❌ VẤN ĐỀ

### 1. **Logic tính tổng tiền SAI**
```java
// SAI - Cũ
tong_tien_truoc_giam = Chỉ tổng SP (không có ship)
tong_tien_sau_giam = tong_tien_truoc_giam + phiShip - voucher

// ĐÚNG - Mới
tong_tien_truoc_giam = Tổng SP + Phí ship (tổng chưa giảm)
tong_tien_sau_giam = tong_tien_truoc_giam - voucher (tổng cuối)
```

### 2. **Voucher hiển thị sai**
- Không có voucher nhưng vẫn bị trừ tiền
- Chỗ "Chọn voucher" không rõ ràng (phải là "Không dùng voucher")

### 3. **Hiển thị PDF không rõ ràng**
- Không tách riêng phí ship
- Không hiển thị giảm giá từ voucher

## ✅ ĐÃ SỬA

### **Backend - HoaDonController.java**

#### 1. Sửa hàm `addSP_HD` (dòng 728-764)
```java
// Tính tổng tiền sản phẩm
BigDecimal tongTienSanPham = chiTietList.stream()
        .map(HoaDonChiTiet::getDon_gia)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

// Tổng trước giảm = Tổng SP + Ship
BigDecimal tongTienTruocGiam = tongTienSanPham.add(phiVanChuyen);

// Tính voucher CHỈ trên tổng SP (không tính ship)
if (voucher.getKieuGiamGia().equals("Phần trăm")) {
    tienGiam = tongTienSanPham.multiply(voucher.getGiaTriGiam().divide(new BigDecimal("100")));
    if (voucher.getGiaTriToiDa() != null && tienGiam.compareTo(voucher.getGiaTriToiDa()) > 0) {
        tienGiam = voucher.getGiaTriToiDa();
    }
} else if (voucher.getKieuGiamGia().equals("Tiền mặt")) {
    tienGiam = voucher.getGiaTriGiam();
}

// Cập nhật
hd.setTong_tien_truoc_giam(tongTienTruocGiam);  // SP + Ship
hd.setTong_tien_sau_giam(tongTienTruocGiam.subtract(tienGiam));  // - Voucher
```

#### 2. Sửa hàm `update_soLuong` (dòng 1018-1064)
Tương tự logic trên.

#### 3. Sửa tính `tienGiamCu` (dòng 645-648 và 924-928)
```java
// SAI - Cũ
BigDecimal tienGiamCu = tongTienTruocGiamCu.add(phiVanChuyen).subtract(tongTienSauGiamCu);

// ĐÚNG - Mới
BigDecimal tienGiamCu = tongTienTruocGiamCu.subtract(tongTienSauGiamCu);
```

### **Frontend - TheHeader-BanHang.vue**

#### 1. Hiển thị rõ ràng hơn (dòng 243-270)
```vue
<!-- Tổng tiền hàng (chỉ SP, không có ship) -->
<div class="mb-3">
    <label class="form-label">Tổng tiền hàng (VNĐ):</label>
    <input type="text" class="form-control"
        :value="formatCurrency((activeTabData.hd.tong_tien_truoc_giam || 0) - (activeTabData.hd.phi_van_chuyen || 0))" 
        disabled>
</div>

<!-- Phí ship (chỉ hiện khi giao hàng) -->
<div class="mb-3" v-if="activeTabData.hd.phuong_thuc_nhan_hang === 'Giao hàng'">
    <label class="form-label">Phí vận chuyển (VNĐ):</label>
    <input type="text" class="form-control"
        :value="formatCurrency(activeTabData.hd.phi_van_chuyen || 0)" 
        disabled>
</div>

<!-- Voucher -->
<div class="mb-3">
    <label for="idVoucher" class="form-label">Voucher</label>
    <select v-model="activeTabData.hd.id_voucher" @change="updateVoucher">
        <option :value="null">-- Không dùng voucher --</option>  <!-- Sửa label -->
        ...
    </select>
</div>

<!-- Giảm giá (chỉ hiện khi có voucher) -->
<div class="mb-3" v-if="(activeTabData.hd.tong_tien_truoc_giam - activeTabData.hd.tong_tien_sau_giam) > 0">
    <label class="form-label">Giảm từ Voucher (VNĐ):</label>
    <input type="text" class="form-control text-success fw-bold"
        :value="'-' + formatCurrency((activeTabData.hd.tong_tien_truoc_giam || 0) - (activeTabData.hd.tong_tien_sau_giam || 0))" 
        disabled>
</div>

<!-- Tổng thanh toán (làm nổi bật) -->
<div class="mb-3">
    <label class="form-label fw-bold">Tổng thanh toán (VNĐ):</label>
    <input type="text" class="form-control fw-bold fs-5"
        :value="formatCurrency(activeTabData.hd.tong_tien_sau_giam)" 
        disabled>
</div>
```

#### 2. Sửa PDF (dòng 1102-1134)
```javascript
// Tổng tiền sản phẩm (không có ship)
const tongTienSanPham = (activeTabData.value.hd.tong_tien_truoc_giam || 0) - 
                        (activeTabData.value.hd.phi_van_chuyen || 0);
doc.text(`Tổng tiền hàng:`, 115, y, { align: "left" });
doc.text(`${formatCurrency(tongTienSanPham)}`, 190, y, { align: "right" });

// Phí ship (nếu có)
if (activeTabData.value.hd.phi_van_chuyen && activeTabData.value.hd.phi_van_chuyen > 0) {
    y += 6;
    doc.text(`Phí vận chuyển:`, 115, y, { align: "left" });
    doc.text(`+${formatCurrency(activeTabData.value.hd.phi_van_chuyen)}`, 190, y, { align: "right" });
}

// Giảm giá (nếu có)
const giamGia = (activeTabData.value.hd.tong_tien_truoc_giam || 0) -
                (activeTabData.value.hd.tong_tien_sau_giam || 0);
if (giamGia > 0) {
    y += 6;
    doc.text(`Giảm giá (Voucher):`, 115, y, { align: "left" });
    doc.text(`-${formatCurrency(giamGia)}`, 190, y, { align: "right" });
}

// Tổng cuối
y += 6;
doc.setFont("Roboto", "bold");
doc.text(`Thành tiền:`, 115, y, { align: "left" });
doc.text(`${formatCurrency(activeTabData.value.hd.tong_tien_sau_giam)}`, 190, y, { align: "right" });
```

## 📋 CÔNG THỨC CUỐI CÙNG

```
Tổng SP = 100,000đ
Phí ship = 25,000đ
Voucher giảm 10% (max 50,000đ)

→ Tổng trước giảm = 100,000 + 25,000 = 125,000đ
→ Voucher giảm = 100,000 × 10% = 10,000đ (tính trên SP, không tính ship)
→ Tổng sau giảm = 125,000 - 10,000 = 115,000đ

HIỂN THỊ:
✅ Tổng tiền hàng: 100,000đ
✅ Phí vận chuyển: +25,000đ
✅ Giảm từ Voucher: -10,000đ
✅ Tổng thanh toán: 115,000đ
```

## 🎯 KẾT QUẢ

### ✅ Trước (SAI):
```
Tổng tiền hàng: 125,000đ  ← Đã bao gồm ship (gây nhầm lẫn)
Voucher: (chọn nhưng không hiện)
Tổng thanh toán: 115,000đ  ← Không biết giảm bao nhiêu
```

### ✅ Sau (ĐÚNG):
```
Tổng tiền hàng: 100,000đ  ← Chỉ SP
Phí vận chuyển: 25,000đ   ← Tách riêng
Voucher: Giảm 10%
Giảm từ Voucher: -10,000đ ← Hiện rõ
Tổng thanh toán: 115,000đ
```

## 🚀 TEST

1. Tạo đơn hàng mới
2. Thêm sản phẩm 100,000đ
3. Chọn "Giao hàng" → Tự động tính ship 25,000đ
4. **Không chọn voucher** → Tổng = 125,000đ ✓
5. **Chọn voucher giảm 10%** → Tổng = 115,000đ ✓
6. In hóa đơn PDF → Kiểm tra hiển thị rõ ràng ✓

## 📄 FILES ĐÃ SỬA

1. ✅ `duanbe/src/main/java/com/example/duanbe/controller/HoaDonController.java`
   - Dòng 645-648: Sửa tính tienGiamCu
   - Dòng 728-764: Sửa logic addSP_HD
   - Dòng 924-928: Sửa tính tienGiamCu
   - Dòng 1018-1064: Sửa logic update_soLuong

2. ✅ `DuAnMauFE/src/components/admin-components/BanHang/TheHeader-BanHang.vue`
   - Dòng 243-270: Hiển thị form rõ ràng hơn
   - Dòng 1102-1134: Sửa hiển thị PDF

## ⚠️ LƯU Ý

- **Voucher CHỈ tính trên tổng tiền sản phẩm**, không tính trên phí ship
- **Phí ship LUÔN LUÔN được cộng vào**, không bị giảm bởi voucher
- Nếu không chọn voucher, `tong_tien_truoc_giam = tong_tien_sau_giam`
