# PHÂN TÍCH VÀ GIẢI PHÁP - BÁN HÀNG TẠI QUẦY

## 📌 TÓM TẮT VẤN ĐỀ

### Vấn đề 1: Thêm 2 sản phẩm khi chỉ click 1 lần
- Khi click chọn sản phẩm để thêm vào giỏ hàng, hệ thống thêm 2 dòng sản phẩm giống nhau

### Vấn đề 2: Thêm sản phẩm trùng tạo dòng mới
- Khi sản phẩm đã có trong giỏ hàng, thêm lại sản phẩm đó sẽ tạo dòng mới thay vì cộng số lượng

---

## 🔍 NGUYÊN NHÂN CHI TIẾT

### 1. Logic Backend - `themSPHDMoi()` có lỗi

**File:** `BanHangController.java` (Line 368-432)

**Vấn đề:**
```java
@PostMapping("/themSPHDMoi")
public ResponseEntity<?> themSPHDMoi(...) {
    // ❌ KHÔNG KIỂM TRA sản phẩm đã tồn tại trong hóa đơn
    
    // ✅ Tạo mới chi tiết hóa đơn - LUÔN LUÔN INSERT MỚI
    HoaDonChiTiet chiTiet = new HoaDonChiTiet();
    chiTiet.setHoaDon(hoaDon);
    chiTiet.setChiTietSanPham(ctsp);
    chiTiet.setSo_luong(soLuong);
    chiTiet.setDon_gia(giaBan.multiply(BigDecimal.valueOf(soLuong)));
    
    hoaDonChiTietRepo.save(chiTiet); // ❌ Insert mới, không update
}
```

**Lý do:** Hàm này không kiểm tra xem sản phẩm đã có trong hóa đơn hay chưa, nên luôn tạo dòng mới.

---

### 2. Frontend - Có thể bị gọi API 2 lần

**File:** `TheHeader-BanHang.vue` (Line 655-708)

**Vấn đề tiềm ẩn:**
```javascript
const addToBill = async (product) => {
    if (isAdding) return;  // ✅ Có flag chống spam
    isAdding = true;

    try {
        const result = await store.themSPHDMoi(
            currentTab.hd.id_hoa_don,
            product.id_chi_tiet_san_pham,
            1,
            product.gia_sau_giam || product.gia_ban
        );
        // ... refresh data
    } finally {
        isAdding = false;
    }
};

const handleDropdownClick = (product) => {
    if (!dropdownVisible.value) return;
    addToBill(product);  // ✅ Logic này đúng
};
```

**Nguyên nhân có thể:**
- Người dùng double-click nhanh trước khi `isAdding = false`
- Dropdown được click nhiều lần do event bubbling
- Có event listener khác trigger cùng lúc

---

### 3. Logic tính giá khuyến mãi

**File:** `ChiTietSanPhamRepo.java` (Line 259-288)

**Query `getAllCTSPKM()`:**
```sql
SELECT COALESCE(km_max.giaHienTai, ctsp.gia_ban) AS gia_ban
FROM chi_tiet_san_pham ctsp
LEFT JOIN (
    SELECT ctkm.id_chi_tiet_san_pham,
           MIN(ctkm.gia_sau_giam) AS giaHienTai  -- ✅ Lấy giá tốt nhất
    FROM chi_tiet_khuyen_mai ctkm
    JOIN khuyen_mai km ON ctkm.id_khuyen_mai = km.id_khuyen_mai
    WHERE km.trang_thai = N'Đang diễn ra'
    AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
    GROUP BY ctkm.id_chi_tiet_san_pham
) km_max ON ctsp.id_chi_tiet_san_pham = km_max.id_chi_tiet_san_pham
```

**✅ Logic này ĐÚNG** - Chọn giá sau giảm nhỏ nhất nếu có nhiều khuyến mãi cùng lúc.

**⚠️ Lưu ý:** Cột `gia_sau_giam` trong bảng `chi_tiet_khuyen_mai` phải được tính đúng:
- **Phần trăm**: `gia_sau_giam = gia_ban * (1 - gia_tri_giam/100)`
- **Tiền mặt**: `gia_sau_giam = gia_ban - gia_tri_giam`

---

## ✅ GIẢI PHÁP ĐỀ XUẤT

### Giải pháp 1: Sửa Backend - `themSPHDMoi()`

**Thay đổi logic để kiểm tra và cộng số lượng nếu đã tồn tại:**

```java
@PostMapping("/themSPHDMoi")
public ResponseEntity<?> themSPHDMoi(
        @RequestParam("idHoaDon") Integer idHD,
        @RequestParam("idCTSP") Integer idCTSP,
        @RequestParam("soLuong") Integer soLuongInput) {
    try {
        HoaDon hoaDon = hoaDonRepo.findById(idHD)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại!"));

        ChiTietSanPham ctsp = chiTietSanPhamRepo.findById(idCTSP)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        // ✅ 1. KIỂM TRA SẢN PHẨM ĐÃ CÓ TRONG HÓA ĐƠN CHƯA
        Optional<HoaDonChiTiet> existingItem = hoaDonChiTietRepo
                .findByChiTietSanPhamIdAndHoaDonId(idCTSP, idHD);

        int soLuongTonKho = ctsp.getSo_luong();
        int soLuongTrongHD = existingItem.map(HoaDonChiTiet::getSo_luong).orElse(0);
        
        // ✅ 2. Tính số lượng có thể thêm
        int soLuongCoTheThemToiDa = soLuongTonKho;
        int soLuong = Math.min(soLuongInput, soLuongCoTheThemToiDa);
        
        if (soLuong <= 0) {
            return ResponseEntity.badRequest().body("Sản phẩm đã hết hàng!");
        }

        // ✅ 3. Lấy giá khuyến mãi tốt nhất
        List<ChiTietKhuyenMai> khuyenMais = chiTietKhuyenMaiRepo.findAllByChiTietSanPhamId(idCTSP);
        Optional<BigDecimal> giaGiamTotNhat = khuyenMais.stream()
                .map(ChiTietKhuyenMai::getGiaSauGiam)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo);

        BigDecimal donGiaLe = giaGiamTotNhat.orElse(ctsp.getGia_ban());

        HoaDonChiTiet chiTiet;
        
        // ✅ 4. NẾU ĐÃ TỒN TẠI -> CỘNG SỐ LƯỢNG
        if (existingItem.isPresent()) {
            chiTiet = existingItem.get();
            int soLuongMoi = chiTiet.getSo_luong() + soLuong;
            chiTiet.setSo_luong(soLuongMoi);
            chiTiet.setDon_gia(donGiaLe.multiply(BigDecimal.valueOf(soLuongMoi)));
        } 
        // ✅ 5. NẾU CHƯA TỒN TẠI -> TẠO MỚI
        else {
            chiTiet = new HoaDonChiTiet();
            chiTiet.setHoaDon(hoaDon);
            chiTiet.setChiTietSanPham(ctsp);
            chiTiet.setSo_luong(soLuong);
            chiTiet.setDon_gia(donGiaLe.multiply(BigDecimal.valueOf(soLuong)));
        }

        // ✅ 6. Trừ tồn kho
        ctsp.setSo_luong(ctsp.getSo_luong() - soLuong);
        chiTietSanPhamRepo.save(ctsp);

        // ✅ 7. Lưu chi tiết hóa đơn
        hoaDonChiTietRepo.save(chiTiet);

        // ✅ 8. Tính lại tổng tiền
        List<HoaDonChiTiet> danhSachChiTiet = hoaDonChiTietRepo.findByIdHoaDon(idHD);
        BigDecimal tongTien = danhSachChiTiet.stream()
                .map(HoaDonChiTiet::getDon_gia)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(hoaDon.getPhi_van_chuyen());

        hoaDon.setTong_tien_truoc_giam(tongTien);
        hoaDon.setTong_tien_sau_giam(tongTien);
        hoaDonRepo.save(hoaDon);

        // ✅ 9. Cập nhật voucher (nếu có)
        capNhatVoucher(idHD);

        return ResponseEntity.ok(existingItem.isPresent() 
            ? "Đã cộng số lượng sản phẩm vào hóa đơn" 
            : "Thêm sản phẩm mới vào hóa đơn thành công");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi thêm sản phẩm: " + e.getMessage());
    }
}
```

---

### Giải pháp 2: Cải thiện Frontend - Chống double-click

**File:** `TheHeader-BanHang.vue`

**Thêm debounce và kiểm tra kỹ hơn:**

```javascript
let isAdding = false;
let lastClickTime = 0;
const CLICK_DELAY = 500; // ms

const addToBill = async (product) => {
    const now = Date.now();
    
    // ✅ 1. Chống spam click
    if (isAdding || (now - lastClickTime < CLICK_DELAY)) {
        console.log('Đang xử lý, vui lòng đợi...');
        return;
    }
    
    lastClickTime = now;
    isAdding = true;

    const currentTab = activeTabData.value;
    if (!currentTab || !currentTab.hd?.id_hoa_don) {
        message.error('Vui lòng chọn hoặc tạo một hóa đơn hợp lệ trước!');
        isAdding = false;
        return;
    }

    if (product.so_luong <= 0) {
        message.warning(`Sản phẩm "${product.ten_san_pham}" đã hết hàng!`);
        isAdding = false;
        return;
    }

    try {
        const result = await store.themSPHDMoi(
            currentTab.hd.id_hoa_don,
            product.id_chi_tiet_san_pham,
            1
        );
        
        if (!result) {
            isAdding = false;
            return;
        }

        // ✅ 2. Refresh data
        await store.getAllSPHD(currentTab.hd.id_hoa_don);
        currentTab.items.value = store.getAllSPHDArr.map(item => ({
            id_hoa_don: item.id_hoa_don,
            id_chi_tiet_san_pham: item.id_chi_tiet_san_pham,
            hinh_anh: item.hinh_anh,
            ten_san_pham: item.ten_san_pham,
            mau_sac: item.ten_mau_sac || item.mau_sac || null,
            kich_thuoc: item.gia_tri || null,
            so_luong: item.so_luong,
            gia_ban: item.don_gia,
            tong_tien: item.don_gia * item.so_luong,
            so_luong_ton_goc: item.so_luong_ton || 0
        }));
        
        await refreshHoaDon(currentTab.hd.id_hoa_don);

        dropdownVisible.value = false;
        searchQuery.value = '';
        message.success(`Đã thêm "${product.ten_san_pham}" vào hóa đơn.`);
        
        await store.getAllCTSPKM();
        allProducts.value = store.getAllCTSPKMList;

    } catch (error) {
        console.error('Lỗi khi thêm sản phẩm:', error);
        message.error('Đã xảy ra lỗi khi thêm sản phẩm!');
    } finally {
        isAdding = false;
    }
};
```

---

### Giải pháp 3: Đảm bảo `gia_sau_giam` được tính đúng

**Khi tạo/cập nhật khuyến mãi, cần tính `gia_sau_giam` trong bảng `chi_tiet_khuyen_mai`:**

```java
// Giả sử có controller KhuyenMaiController
@PostMapping("/apDungKhuyenMai")
public ResponseEntity<?> apDungKhuyenMai(
        @RequestParam Integer idKhuyenMai,
        @RequestParam List<Integer> danhSachIdCTSP) {
    
    KhuyenMai khuyenMai = khuyenMaiRepo.findById(idKhuyenMai)
        .orElseThrow(() -> new RuntimeException("Khuyến mãi không tồn tại"));
    
    for (Integer idCTSP : danhSachIdCTSP) {
        ChiTietSanPham ctsp = chiTietSanPhamRepo.findById(idCTSP)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        BigDecimal giaBan = ctsp.getGia_ban();
        BigDecimal giaSauGiam;
        
        // ✅ Tính giá sau giảm dựa vào loại khuyến mãi
        if ("Phần trăm".equalsIgnoreCase(khuyenMai.getKieu_giam_gia())) {
            BigDecimal giam = giaBan.multiply(khuyenMai.getGia_tri_giam())
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            
            // Kiểm tra giá trị tối đa
            if (khuyenMai.getGia_tri_toi_da() != null && giam.compareTo(khuyenMai.getGia_tri_toi_da()) > 0) {
                giam = khuyenMai.getGia_tri_toi_da();
            }
            
            giaSauGiam = giaBan.subtract(giam);
        } 
        else if ("Tiền mặt".equalsIgnoreCase(khuyenMai.getKieu_giam_gia())) {
            BigDecimal giam = khuyenMai.getGia_tri_giam();
            
            // Kiểm tra giá trị tối đa
            if (khuyenMai.getGia_tri_toi_da() != null && giam.compareTo(khuyenMai.getGia_tri_toi_da()) > 0) {
                giam = khuyenMai.getGia_tri_toi_da();
            }
            
            giaSauGiam = giaBan.subtract(giam);
        } else {
            giaSauGiam = giaBan; // Không giảm
        }
        
        // Đảm bảo giá không âm
        if (giaSauGiam.compareTo(BigDecimal.ZERO) < 0) {
            giaSauGiam = BigDecimal.ZERO;
        }
        
        // Lưu chi tiết khuyến mãi với giá sau giảm đã tính
        ChiTietKhuyenMai ctkm = new ChiTietKhuyenMai();
        ctkm.setKhuyenMai(khuyenMai);
        ctkm.setChiTietSanPham(ctsp);
        ctkm.setGiaSauGiam(giaSauGiam);
        
        chiTietKhuyenMaiRepo.save(ctkm);
    }
    
    return ResponseEntity.ok("Áp dụng khuyến mãi thành công!");
}
```

---

## 🔧 CÁC BƯỚC TRIỂN KHAI

### Bước 1: Sửa Backend
1. ✅ Sửa hàm `themSPHDMoi()` trong `BanHangController.java`
2. ✅ Test API bằng Postman/cURL:
   - Thêm sản phẩm lần 1 → Phải tạo mới
   - Thêm sản phẩm lần 2 (cùng sản phẩm) → Phải cộng số lượng, không tạo dòng mới

### Bước 2: Cải thiện Frontend
1. ✅ Thêm debounce logic vào `addToBill()` trong `TheHeader-BanHang.vue`
2. ✅ Test trên UI:
   - Click nhanh nhiều lần → Chỉ được xử lý 1 lần
   - Thêm sản phẩm trùng → Cộng số lượng

### Bước 3: Kiểm tra logic khuyến mãi
1. ✅ Kiểm tra dữ liệu trong bảng `chi_tiet_khuyen_mai`
2. ✅ Đảm bảo `gia_sau_giam` được tính đúng
3. ✅ Test query `getAllCTSPKM()` bằng SQL trực tiếp

---

## 📊 KIỂM TRA DATABASE

### Kiểm tra sản phẩm trùng trong hóa đơn:
```sql
SELECT 
    hd.id_hoa_don,
    hd.ma_hoa_don,
    hdct.id_chi_tiet_san_pham,
    COUNT(*) as so_lan_xuat_hien
FROM hoa_don_chi_tiet hdct
JOIN hoa_don hd ON hd.id_hoa_don = hdct.id_hoa_don
WHERE hd.trang_thai = N'Đang chờ'
GROUP BY hd.id_hoa_don, hd.ma_hoa_don, hdct.id_chi_tiet_san_pham
HAVING COUNT(*) > 1;
```

Nếu query này trả về kết quả → Có sản phẩm bị trùng → Cần fix backend.

### Kiểm tra giá khuyến mãi:
```sql
SELECT 
    ctsp.id_chi_tiet_san_pham,
    sp.ten_san_pham,
    ctsp.gia_ban AS gia_goc,
    km.kieu_giam_gia,
    km.gia_tri_giam,
    ctkm.gia_sau_giam,
    -- Tính lại để kiểm tra
    CASE 
        WHEN km.kieu_giam_gia = N'Phần trăm' 
        THEN ctsp.gia_ban * (1 - km.gia_tri_giam / 100.0)
        WHEN km.kieu_giam_gia = N'Tiền mặt'
        THEN ctsp.gia_ban - km.gia_tri_giam
    END AS gia_tinh_lai
FROM chi_tiet_khuyen_mai ctkm
JOIN chi_tiet_san_pham ctsp ON ctsp.id_chi_tiet_san_pham = ctkm.id_chi_tiet_san_pham
JOIN khuyen_mai km ON km.id_khuyen_mai = ctkm.id_khuyen_mai
JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
WHERE km.trang_thai = N'Đang diễn ra'
AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han;
```

So sánh `gia_sau_giam` và `gia_tinh_lai` → Phải giống nhau.

---

## ✨ KẾT LUẬN

### Logic hiện tại của bạn:
- ✅ **Logic tính giá khuyến mãi**: ĐÚNG (lấy MIN từ nhiều KM)
- ✅ **Query `getAllCTSPKM()`**: ĐÚNG
- ❌ **Hàm `themSPHDMoi()`**: SAI (luôn tạo mới, không kiểm tra trùng)

### Sau khi sửa:
- ✅ Sản phẩm trùng sẽ cộng số lượng thay vì tạo dòng mới
- ✅ Chống double-click hiệu quả hơn
- ✅ Giá khuyến mãi được áp dụng đúng

### Gợi ý cải tiến thêm:
1. **Thêm unique constraint** trong DB:
   ```sql
   ALTER TABLE hoa_don_chi_tiet
   ADD CONSTRAINT UK_hoa_don_ctsp UNIQUE (id_hoa_don, id_chi_tiet_san_pham);
   ```
   → Database sẽ tự động ngăn thêm trùng

2. **Thêm transaction** cho hàm `themSPHDMoi()`:
   ```java
   @Transactional
   @PostMapping("/themSPHDMoi")
   ```
   → Đảm bảo tính toàn vẹn dữ liệu

3. **Log để debug**:
   ```java
   logger.info("Thêm SP: idHD={}, idCTSP={}, existed={}", idHD, idCTSP, existingItem.isPresent());
   ```

---

**Ngày phân tích:** 2025-11-13
**Người thực hiện:** GitHub Copilot CLI
