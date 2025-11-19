# 🖼️ PHÂN TÍCH VÀ CẢI TIẾN LOGIC LẤY ẢNH SẢN PHẨM

## 📊 HIỆN TRẠNG

### Database Schema:
```sql
CREATE TABLE hinh_anh (
    id_hinh_anh int IDENTITY(1,1) PRIMARY KEY,
    id_chi_tiet_san_pham int,
    hinh_anh nvarchar(500) NOT NULL,
    anh_chinh bit DEFAULT 0 NOT NULL  -- ✅ Có cột đánh dấu ảnh chính
)
```

### Backend Query (AnhSanPham CTE):
```sql
AnhSanPham AS (
    SELECT
        id_chi_tiet_san_pham,
        STUFF((
            SELECT ',' + ha.hinh_anh
            FROM hinh_anh ha
            WHERE ha.id_chi_tiet_san_pham = outer_ha.id_chi_tiet_san_pham
              AND ha.hinh_anh IS NOT NULL AND ha.hinh_anh <> ''
            ORDER BY CASE WHEN ha.anh_chinh = 1 THEN 0 ELSE 1 END, ha.id_hinh_anh
            FOR XML PATH('')
        ), 1, 1, '') AS hinh_anh
    FROM hinh_anh outer_ha
    GROUP BY id_chi_tiet_san_pham
)
```
✅ **ĐÃ ĐÚNG:** Ưu tiên ảnh chính lên trước

### Kết quả trả về:
```json
{
  "id_chi_tiet_san_pham": 1,
  "id_mau_sac": 1,
  "hinh_anh": "anh1.jpg,anh2.jpg,anh3.jpg"  // ✅ Chuỗi phân tách bằng dấu phẩy
}
```

---

## ❌ VẤN ĐỀ HIỆN TẠI

### 1. **Backend không trả về thông tin ảnh chính riêng**
- Chỉ trả về chuỗi URL phân tách bằng dấu phẩy
- Frontend không biết ảnh nào là ảnh chính

### 2. **Frontend xử lý ảnh chưa tối ưu:**

```javascript
// Dòng 741-817
const organizeImagesByColor = () => {
    // ❌ Vấn đề:
    // 1. Split chuỗi nhưng không biết ảnh nào là ảnh chính
    // 2. Logic loại bỏ trùng lặp dựa trên URL, không tối ưu
    // 3. allImages.value không được sắp xếp theo màu
    // 4. Khi chọn màu, ảnh hiển thị không theo thứ tự ưu tiên
}
```

### 3. **Yêu cầu:**
✅ Lấy TẤT CẢ ảnh của TẤT CẢ chi tiết sản phẩm  
✅ Loại bỏ ảnh trùng (cùng URL)  
✅ Ưu tiên ảnh chính lên đầu mỗi màu  
✅ Khi click màu → Hiện ảnh đầu tiên của màu đó  

---

## ✅ GIẢI PHÁP TỐI ƯU

### **OPTION 1: Sửa Backend để trả về cấu trúc tốt hơn**

#### Tạo query riêng để lấy tất cả ảnh:

```sql
-- Thêm endpoint mới: GET /api/chi-tiet-san-pham/{idSanPham}/images
@Query(nativeQuery = true, value = """
    SELECT 
        ha.id_hinh_anh,
        ha.id_chi_tiet_san_pham,
        ha.hinh_anh,
        ha.anh_chinh,
        ctsp.id_mau_sac,
        ms.ten_mau_sac,
        ms.ma_mau_sac
    FROM hinh_anh ha
    INNER JOIN chi_tiet_san_pham ctsp ON ha.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
    INNER JOIN san_pham sp ON ctsp.id_san_pham = sp.id_san_pham
    INNER JOIN mau_sac ms ON ctsp.id_mau_sac = ms.id_mau_sac
    WHERE sp.id_san_pham = :idSanPham
      AND ha.hinh_anh IS NOT NULL 
      AND ha.hinh_anh <> ''
      AND ctsp.trang_thai = 1
    ORDER BY 
        ctsp.id_mau_sac,
        CASE WHEN ha.anh_chinh = 1 THEN 0 ELSE 1 END,
        ha.id_hinh_anh
    """)
List<Map<String, Object>> getAllImagesByProductId(@Param("idSanPham") Integer idSanPham);
```

**Kết quả trả về:**
```json
[
  {
    "id_hinh_anh": 1,
    "id_chi_tiet_san_pham": 1,
    "hinh_anh": "anh1.jpg",
    "anh_chinh": true,
    "id_mau_sac": 1,
    "ten_mau_sac": "Đen",
    "ma_mau_sac": "#000000"
  },
  {
    "id_hinh_anh": 2,
    "id_chi_tiet_san_pham": 1,
    "hinh_anh": "anh2.jpg",
    "anh_chinh": false,
    "id_mau_sac": 1,
    "ten_mau_sac": "Đen",
    "ma_mau_sac": "#000000"
  }
]
```

---

### **OPTION 2: Cải thiện Frontend (KHÔNG CẦN SỬA BACKEND)**

#### Sửa logic `organizeImagesByColor`:

```javascript
const organizeImagesByColor = () => {
    imagesByColor.value = new Map();
    const uniqueImages = new Map(); // Map<url, imageObject>
    const imagesByColorAndPriority = new Map(); // Map<colorId, Array<{isPrimary, image}>>

    productDetails.value.forEach(variant => {
        if (!variant.hinh_anh) return;

        // Xử lý chuỗi ảnh phân tách bằng dấu phẩy
        const imageUrls = typeof variant.hinh_anh === 'string' 
            ? variant.hinh_anh.split(',').map(url => url.trim()).filter(url => url)
            : (Array.isArray(variant.hinh_anh) ? variant.hinh_anh : [variant.hinh_anh]);

        imageUrls.forEach((url, index) => {
            // Kiểm tra ảnh đã tồn tại chưa
            if (uniqueImages.has(url)) {
                // Nếu đã có, kiểm tra xem có phải ảnh chính không
                const existing = uniqueImages.get(url);
                if (index === 0 && !existing.isPrimary) {
                    // Ảnh đầu tiên được coi là ảnh chính
                    existing.isPrimary = true;
                }
                return;
            }

            // Tạo object ảnh mới
            const imageObj = {
                id: `${variant.id_chi_tiet_san_pham}_${index}`,
                url: url,
                alt: `${variant.ten_san_pham} - ${variant.ten_mau_sac}`,
                color_id: variant.id_mau_sac,
                color_name: variant.ten_mau_sac || `Màu ${variant.id_mau_sac}`,
                color_code: getColorCode(variant.id_mau_sac),
                isPrimary: index === 0, // Ảnh đầu tiên trong list là ảnh chính
                chi_tiet_san_pham_id: variant.id_chi_tiet_san_pham
            };

            uniqueImages.set(url, imageObj);

            // Thêm vào map theo màu
            if (!imagesByColorAndPriority.has(variant.id_mau_sac)) {
                imagesByColorAndPriority.set(variant.id_mau_sac, []);
            }
            imagesByColorAndPriority.get(variant.id_mau_sac).push(imageObj);
        });
    });

    // Sắp xếp ảnh theo màu (ảnh chính trước)
    imagesByColorAndPriority.forEach((images, colorId) => {
        const sorted = images.sort((a, b) => {
            if (a.isPrimary && !b.isPrimary) return -1;
            if (!a.isPrimary && b.isPrimary) return 1;
            return 0;
        });
        imagesByColor.value.set(colorId, sorted);
    });

    // Tạo danh sách tất cả ảnh (theo thứ tự: màu đầu tiên, ảnh chính trước)
    allImages.value = [];
    const firstColorId = product.value.mau_sac?.[0]?.ma;
    
    // Thêm ảnh của màu đầu tiên trước
    if (firstColorId && imagesByColor.value.has(firstColorId)) {
        allImages.value.push(...imagesByColor.value.get(firstColorId));
    }
    
    // Thêm ảnh của các màu khác
    imagesByColor.value.forEach((images, colorId) => {
        if (colorId !== firstColorId) {
            allImages.value.push(...images);
        }
    });

    product.value.hinh_anh = allImages.value;

    console.log('✅ Hình ảnh theo màu:', imagesByColor.value);
    console.log('✅ Tổng số ảnh unique:', allImages.value.length);
};
```

#### Sửa hàm `findAndShowFirstImageOfColor`:

```javascript
const findAndShowFirstImageOfColor = (colorId) => {
    if (!imagesByColor.value.has(colorId)) {
        console.log('Không tìm thấy ảnh cho màu:', colorId);
        return;
    }

    const imagesForColor = imagesByColor.value.get(colorId);
    if (imagesForColor.length === 0) return;

    // Tìm ảnh đầu tiên (đã được sắp xếp, ảnh chính ở đầu)
    const firstImage = imagesForColor[0];
    
    // Tìm index trong allImages
    const firstImageIndex = allImages.value.findIndex(img => img.url === firstImage.url);
    
    if (firstImageIndex !== -1) {
        currentImageIndex.value = firstImageIndex;
        console.log('✅ Chuyển đến ảnh của màu', colorId, 'tại vị trí:', firstImageIndex);
        console.log('✅ Ảnh chính:', firstImage.isPrimary);
    }
};
```

---

## 📊 SO SÁNH

### TRƯỚC (Hiện tại):
```javascript
// ❌ Không phân biệt ảnh chính
// ❌ Logic loại trùng không tối ưu  
// ❌ Thứ tự ảnh không đúng
const hasImage = allImages.value.some(existing => existing.url === img.url);
if (!hasImage) {
    allImages.value.push(img);
}
```

### SAU (Cải tiến):
```javascript
// ✅ Đánh dấu ảnh chính (index === 0)
// ✅ Dùng Map để loại trùng O(1)
// ✅ Sắp xếp ảnh chính lên đầu mỗi màu
const uniqueImages = new Map();
// ... logic tối ưu
```

---

## 🎯 KẾT QUẢ MONG MUỐN

1. ✅ Lấy TẤT CẢ ảnh của tất cả CTSP
2. ✅ Loại bỏ ảnh trùng URL
3. ✅ Ảnh chính (index 0 trong chuỗi) hiển thị đầu tiên mỗi màu
4. ✅ Click màu → Chuyển đến ảnh đầu tiên của màu đó
5. ✅ Performance tối ưu với Map

---

## 🚀 KHUYẾN NGHỊ

**Nên chọn OPTION 2** vì:
- ✅ Không cần sửa backend
- ✅ Hoạt động với dữ liệu hiện tại
- ✅ Logic rõ ràng, dễ bảo trì
- ✅ Performance tốt hơn

Nếu cần mở rộng sau này (thêm nhiều ảnh, phân loại ảnh), mới cân nhắc OPTION 1.
