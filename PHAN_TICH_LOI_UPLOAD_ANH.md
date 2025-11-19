# PHÂN TÍCH LỖI UPLOAD NHIỀU ẢNH CHO CHI TIẾT SẢN PHẨM

## Vấn đề
- Người dùng upload nhiều ảnh nhưng chỉ có 1 ảnh được lưu vào database
- Các ảnh còn lại bị mất tích

## Nguyên nhân phát hiện

### 1. Frontend giới hạn tối đa 2 ảnh
**File**: `DuAnMauFE/src/components/admin-components/QuanLySanPham/suaSanPham.vue`

#### Dòng 759-762:
```javascript
if (currentCount >= 2) {
    message.error('Chỉ được upload tối đa 2 ảnh!');
    onError(new Error('Max count reached'));
    return;
}
```

#### Dòng 188:
```vue
<div v-if="!variant.fileList || variant.fileList.length < 2">
```

#### Dòng 224:
```vue
Nhấp vào ảnh để chọn làm ảnh chính. Tối đa 2 ảnh.
```

### 2. Backend logic trông OK
Backend có 3 methods xử lý ảnh:
- `saveProductImages()` - Lưu ảnh cho sản phẩm mới (OK)
- `addNewProductImagesOnly()` - Thêm ảnh mới không xóa ảnh cũ (OK)
- `processProductImages()` - Xử lý cập nhật ảnh (OK)

Tất cả đều có vòng lặp `for` để lưu TẤT CẢ ảnh trong list.

## Giải pháp

### Tùy chọn 1: Tăng giới hạn số ảnh (Nếu muốn nhiều hơn 2 ảnh)

#### Bước 1: Sửa file suaSanPham.vue
```javascript
// Thay đổi từ 2 thành số lượng mong muốn (ví dụ: 5)
if (currentCount >= 5) {
    message.error('Chỉ được upload tối đa 5 ảnh!');
    onError(new Error('Max count reached'));
    return;
}
```

#### Bước 2: Cập nhật UI
```vue
<div v-if="!variant.fileList || variant.fileList.length < 5">
    <plus-outlined />
    <div style="margin-top: 8px">Upload</div>
</div>
```

```vue
<div class="image-note">
    Nhấp vào ảnh để chọn làm ảnh chính. Tối đa 5 ảnh.
</div>
```

### Tùy chọn 2: Debug nếu vẫn lỗi với 2 ảnh

Đã thêm debug logging vào backend để kiểm tra:
- Số lượng ảnh nhận được trong request
- Số lượng ảnh được lưu vào database

#### Restart backend và kiểm tra console log:
```bash
cd duanbe
# Nếu đang chạy: Ctrl+C
mvn spring-boot:run
```

#### Test bằng cách:
1. Upload 2 ảnh cho 1 biến thể
2. Lưu sản phẩm
3. Xem console backend sẽ in ra:
   - "Số lượng ảnh nhận được: X"
   - "Danh sách URL ảnh: [...]"
   - "Gọi saveProductImages với X ảnh"
   - "Đã lưu ảnh #1 - ID: ... - URL: ..."
   - "Đã lưu ảnh #2 - ID: ... - URL: ..."

#### Kiểm tra database:
```sql
SELECT * FROM hinh_anh WHERE id_chi_tiet_san_pham = <id_vừa_tạo>
```

## Các file đã sửa

1. `duanbe/src/main/java/com/example/duanbe/service/ChiTietSanPhamService.java`
   - Thêm debug logging ở method `saveChiTietSanPham()` (dòng 78)
   - Thêm debug logging ở method `saveProductImages()` (dòng 235)

## Hành động tiếp theo

1. **Quyết định**: Bạn muốn giới hạn bao nhiêu ảnh? (2, 5, 10, ...)
2. **Cập nhật frontend** theo giới hạn mới
3. **Test** với debug logging đã thêm
4. **Xóa debug logging** sau khi xác nhận hoạt động OK

