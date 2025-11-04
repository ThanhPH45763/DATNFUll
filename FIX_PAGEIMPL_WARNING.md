# Sửa Lỗi PageImpl Serialization Warning

## Vấn Đề
Cảnh báo xuất hiện khi serialize PageImpl:
```
Serializing PageImpl instances as-is is not supported, meaning that there is no guarantee about the stability of the resulting JSON structure!
```

## Giải Pháp Đã Áp Dụng

### 1. Tạo Class PageResponse (response wrapper)
- **File**: `duanbe/src/main/java/com/example/duanbe/response/PageResponse.java`
- Đây là DTO để wrap dữ liệu phân trang với cấu trúc JSON ổn định
- Có static method `fromPage()` để convert từ Spring Page

### 2. Cập Nhật HoaDonController
Đã thay đổi return type từ `Page<T>` sang `PageResponse<T>` cho các endpoint:

#### `/admin/qlhd/loc_hoa_don` (line 182)
```java
// Trước
public Page<HoaDonResponse> searchHoaDon(...)

// Sau
public PageResponse<HoaDonResponse> searchHoaDon(...) {
    Page<HoaDonResponse> page = hoaDonRepo.locHoaDon(...);
    return PageResponse.fromPage(page);
}
```

#### `/admin/qlhd/ctsp_hd` (line 582)
```java
// Trước  
public Page<ChiTietSanPhamView> getAllCTSP_HD(...)

// Sau
public PageResponse<ChiTietSanPhamView> getAllCTSP_HD(...) {
    Page<ChiTietSanPhamView> result = ...;
    return PageResponse.fromPage(result);
}
```

## Cấu Trúc JSON Mới

### Trước (PageImpl - không ổn định)
```json
{
  "content": [...],
  "pageable": {...},
  "sort": {...},
  ...nhiều field phức tạp khác
}
```

### Sau (PageResponse - ổn định)
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 100,
  "totalPages": 10,
  "last": false,
  "first": true
}
```

## Lưu Ý Cho Frontend
Nếu frontend đang sử dụng các endpoint phân trang, cần kiểm tra lại:
- `page.number` → `page`
- `page.totalElements` → `totalElements`
- `page.totalPages` → `totalPages`

Cấu trúc mới đơn giản và rõ ràng hơn.

## Build & Test
```bash
cd /home/huunghia/Documents/DuAnBe/duanbe
./mvnw clean compile
```

✅ Đã compile thành công!
