# HƯỚNG DẪN HIỂN THỊ GIÁ SẢN PHẨM VỚI KHUYẾN MÃI

## 📋 TÓM TẮT THAY ĐỔI

Đã tạo hệ thống hiển thị giá sản phẩm tương tự các trang thương mại điện tử như Shopee, Lazada với logic:
- **Ưu tiên hiển thị giá khuyến mãi** nếu sản phẩm đang có khuyến mãi
- **Hiển thị giá gốc bị gạch ngang** để khách thấy được mức giảm giá
- **Badge % giảm giá** để thu hút khách hàng
- **Khoảng giá** nếu sản phẩm có nhiều biến thể (size, màu sắc)

---

## 🔧 CÁC THAY ĐỔI ĐÃ THỰC HIỆN

### 1. **Sửa Query trong SanPhamRepo.java** (Line 249-310)

**Vấn đề cũ:**
- Query không kiểm tra `trang_thai` của khuyến mãi
- Không xử lý `gia_tri_toi_da` khi giảm theo phần trăm
- Dùng `sp.trang_thai = 1` (sai, phải dùng string)
- Không trim khoảng trắng trong `STRING_SPLIT`

**Đã sửa:**
```sql
WITH KhuyenMaiHieuLuc AS (
    SELECT 
        ctkm.id_chi_tiet_san_pham,
        GiamGia = CASE 
            WHEN km.kieu_giam_gia = N'Phần trăm' THEN 
                CASE 
                    -- Giới hạn giảm tối đa
                    WHEN ctsp.gia_ban * km.gia_tri_giam / 100 > ISNULL(km.gia_tri_toi_da, 999999999)
                        THEN ctsp.gia_ban - km.gia_tri_toi_da
                    ELSE ctsp.gia_ban * (1 - km.gia_tri_giam / 100)
                END
            WHEN km.kieu_giam_gia = N'Tiền mặt' THEN ctsp.gia_ban - km.gia_tri_giam
            ELSE ctsp.gia_ban
        END
    FROM chi_tiet_khuyen_mai ctkm
    JOIN khuyen_mai km 
        ON ctkm.id_khuyen_mai = km.id_khuyen_mai
        AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
        AND km.trang_thai = N'Đang diễn ra'  -- Thêm điều kiện này
    ...
WHERE 
    sp.trang_thai = N'Hoạt động'  -- Sửa từ = 1
    AND EXISTS (SELECT 1
               FROM STRING_SPLIT(:tenDanhMuc, ',') AS kw
               WHERE dm.ten_danh_muc LIKE '%' + LTRIM(RTRIM(kw.value)) + '%')  -- Trim space
```

### 2. **Tạo DTO mới: SanPhamDisplayDTO.java**

```java
@Data
public class SanPhamDisplayDTO {
    private BigDecimal gia_hien_thi;           // Giá hiển thị chính (ưu tiên KM)
    private BigDecimal gia_goc;                // Giá gốc (gạch ngang)
    private Boolean co_khuyen_mai;             // Flag có KM
    private Integer phan_tram_giam;            // % giảm (badge)
    private String khoang_gia;                 // "500.000₫ - 800.000₫"
    
    public static SanPhamDisplayDTO fromView(SanPhamView view) {
        // Logic tự động xử lý giá...
    }
}
```

**Logic xử lý:**
1. So sánh `gia_tot_nhat` với `gia_min` để phát hiện khuyến mãi
2. Nếu có KM:
   - `gia_hien_thi` = giá sau khuyến mãi
   - `gia_goc` = giá trước khuyến mãi (để gạch ngang)
   - Tính `phan_tram_giam` = `(gia_goc - gia_hien_thi) / gia_goc * 100`
3. Nếu không có KM:
   - `gia_hien_thi` = giá gốc
   - `gia_goc` = null

### 3. **Thêm Service Methods** (SanPhamService.java)

```java
public List<SanPhamDisplayDTO> getSanPhamTheoTenDMFormatted(String tenDanhMuc) {
    List<SanPhamView> views = sanPhamRepo.listSanPhamByTenDM(tenDanhMuc);
    return views.stream()
            .map(SanPhamDisplayDTO::fromView)
            .collect(Collectors.toList());
}
```

### 4. **Thêm Endpoints Mới** (SanPhamController.java)

```java
@GetMapping("/getSanPhamByTenDM/formatted")
public List<SanPhamDisplayDTO> getSanPhamByTenDMFormatted(@RequestParam("tenDanhMuc") String tenDanhMuc)

@GetMapping("/getSanPhamByTenSP/formatted")
public List<SanPhamDisplayDTO> getSanPhamByTenSPFormatted(@RequestParam("tenSanPham") String tenSanPham)

@GetMapping("/getSanPhamSieuSale/formatted")
public List<SanPhamDisplayDTO> getSanPhamSieuSaleFormatted()
```

---

## 🌐 CÁCH SỬ DỤNG TRÊN FRONTEND

### **API Endpoints Mới:**

```
GET /admin/quan_ly_san_pham/getSanPhamByTenDM/formatted?tenDanhMuc=Áo,Quần
GET /admin/quan_ly_san_pham/getSanPhamByTenSP/formatted?tenSanPham=Áo thun
GET /admin/quan_ly_san_pham/getSanPhamSieuSale/formatted
```

### **Response Format:**

```json
[
  {
    "id_san_pham": 1,
    "ten_san_pham": "Áo thun nam basic",
    "hinh_anh": "https://...",
    "gia_hien_thi": 180000,        // Giá hiển thị (đã giảm)
    "gia_goc": 250000,             // Giá gốc (gạch ngang)
    "co_khuyen_mai": true,
    "phan_tram_giam": 28,          // Badge: -28%
    "khoang_gia": "180.000₫ - 200.000₫",
    "danh_gia": 4.5,
    "so_luong_danh_gia": 120
  }
]
```

### **Code React/Vue Example:**

```jsx
// React Component
{products.map(product => (
  <div className="product-card">
    <img src={product.hinh_anh} alt={product.ten_san_pham} />
    
    {/* Badge giảm giá */}
    {product.co_khuyen_mai && (
      <span className="badge-sale">-{product.phan_tram_giam}%</span>
    )}
    
    <h3>{product.ten_san_pham}</h3>
    
    {/* Giá */}
    <div className="price-container">
      <span className="price-current">
        {product.gia_hien_thi.toLocaleString('vi-VN')}₫
      </span>
      
      {product.gia_goc && (
        <span className="price-original">
          {product.gia_goc.toLocaleString('vi-VN')}₫
        </span>
      )}
    </div>
    
    {/* Khoảng giá (nếu có nhiều biến thể) */}
    <p className="price-range">{product.khoang_gia}</p>
    
    {/* Rating */}
    <div className="rating">
      ⭐ {product.danh_gia} ({product.so_luong_danh_gia} đánh giá)
    </div>
  </div>
))}
```

### **CSS Styling:**

```css
.price-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.price-current {
  font-size: 20px;
  font-weight: bold;
  color: #ee4d2d; /* Màu đỏ giống Shopee */
}

.price-original {
  font-size: 14px;
  color: #888;
  text-decoration: line-through; /* Gạch ngang */
}

.badge-sale {
  position: absolute;
  top: 10px;
  left: 0;
  background: #ee4d2d;
  color: white;
  padding: 4px 8px;
  font-size: 12px;
  font-weight: bold;
}

.price-range {
  font-size: 12px;
  color: #666;
}
```

---

## 📊 SO SÁNH TRƯỚC VÀ SAU

### **TRƯỚC (Endpoint cũ):**
```json
GET /getSanPhamByTenDM?tenDanhMuc=Áo

{
  "ten_san_pham": "Áo thun",
  "gia_min": 250000,
  "gia_max": 350000,
  "gia_tot_nhat": 180000,
  "gia_khuyen_mai_cao_nhat": 200000
}
```
❌ **Frontend phải tự xử lý logic**
❌ **Khó hiểu, dễ nhầm lẫn**
❌ **Không biết có khuyến mãi hay không**

### **SAU (Endpoint mới):**
```json
GET /getSanPhamByTenDM/formatted?tenDanhMuc=Áo

{
  "ten_san_pham": "Áo thun",
  "gia_hien_thi": 180000,      // ✅ Giá hiển thị rõ ràng
  "gia_goc": 250000,           // ✅ Giá gốc để gạch
  "co_khuyen_mai": true,       // ✅ Flag rõ ràng
  "phan_tram_giam": 28,        // ✅ % giảm cho badge
  "khoang_gia": "180.000₫ - 200.000₫"  // ✅ Đã format sẵn
}
```
✅ **Frontend chỉ cần hiển thị, không cần logic phức tạp**
✅ **Dữ liệu rõ ràng, dễ hiểu**
✅ **Giống UX của Shopee, Lazada**

---

## 🧪 TESTING

### **1. Test Query trực tiếp trong SQL Server:**

```sql
-- Test với danh mục "Áo"
DECLARE @tenDanhMuc NVARCHAR(100) = 'Áo';
-- (Paste toàn bộ query từ SanPhamRepo)
```

### **2. Test API qua Postman:**

```
GET http://localhost:8080/admin/quan_ly_san_pham/getSanPhamByTenDM/formatted?tenDanhMuc=Áo
```

**Expected Response:**
- `co_khuyen_mai = true` nếu có khuyến mãi hiệu lực
- `gia_hien_thi < gia_goc` khi có khuyến mãi
- `phan_tram_giam > 0` khi có khuyến mãi

### **3. Test từ Frontend:**

```javascript
const response = await fetch('/admin/quan_ly_san_pham/getSanPhamByTenDM/formatted?tenDanhMuc=Áo');
const products = await response.json();

console.log(products[0]);
// {
//   gia_hien_thi: 180000,
//   gia_goc: 250000,
//   co_khuyen_mai: true,
//   phan_tram_giam: 28
// }
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

### **1. Database Requirements:**
- Bảng `khuyen_mai` phải có cột `trang_thai` = `'Đang diễn ra'`
- Khuyến mãi phải nằm trong khoảng `ngay_bat_dau` và `ngay_het_han`

### **2. Performance:**
- Query sử dụng CTE (Common Table Expression) nên hiệu suất tốt
- Với > 10,000 sản phẩm, nên thêm index:
```sql
CREATE INDEX idx_ctkm_id_km ON chi_tiet_khuyen_mai(id_khuyen_mai);
CREATE INDEX idx_km_date ON khuyen_mai(ngay_bat_dau, ngay_het_han);
```

### **3. Endpoint Cũ Vẫn Hoạt Động:**
- `/getSanPhamByTenDM` (cũ) - trả về `SanPhamView`
- `/getSanPhamByTenDM/formatted` (mới) - trả về `SanPhamDisplayDTO`

Frontend có thể chuyển dần sang endpoint mới.

---

## 🎨 GỢI Ý HIỂN THỊ UX/UI

### **Layout giống Shopee:**

```
┌────────────────────────┐
│  [-28%]       ⭐4.5    │
│                        │
│    [Hình ảnh SP]       │
│                        │
│ Áo thun nam basic      │
│                        │
│ 180.000₫  250.000₫     │
│  (đỏ)     (gạch)       │
│                        │
│ 120 đánh giá           │
└────────────────────────┘
```

### **Màu sắc gợi ý:**
- Giá khuyến mãi: `#ee4d2d` (đỏ Shopee)
- Giá gốc: `#888` (xám, gạch ngang)
- Badge giảm giá: `#ee4d2d` background, `white` text
- Rating: `#ffa727` (vàng)

---

## 📝 CHANGELOG

**Version 1.0 - 2025-11-17**
- ✅ Sửa query `listSanPhamByTenDM` với logic khuyến mãi chính xác
- ✅ Tạo DTO `SanPhamDisplayDTO` với logic format giá tự động
- ✅ Thêm 3 endpoints mới: `formatted` variants
- ✅ Tài liệu hướng dẫn sử dụng đầy đủ

---

## 🤝 HỖ TRỢ

Nếu gặp vấn đề:
1. Kiểm tra database có khuyến mãi đang hiệu lực không
2. Xem console log backend khi call API
3. Kiểm tra response format có đúng như tài liệu không
4. Test query trực tiếp trong SQL Server Management Studio

**Endpoint test nhanh:**
```
GET /admin/quan_ly_san_pham/getSanPhamSieuSale/formatted
```
Endpoint này chỉ trả về sản phẩm CÓ KHUYẾN MÃI, dễ test.
