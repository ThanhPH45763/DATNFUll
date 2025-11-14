# ✅ CHECKLIST KIỂM TRA SAU KHI SỬA LỖI

## 📋 DANH SÁCH CÔNG VIỆC

### Giai đoạn 1: Build và Deploy

- [ ] **Backend Build**
  ```bash
  cd /home/huunghia/DATNFUll/duanbe
  mvn clean install
  ```
  - [ ] Build thành công không có lỗi
  - [ ] Khởi động server: `mvn spring-boot:run`
  - [ ] Server chạy ổn định

- [ ] **Frontend Build**
  ```bash
  cd /home/huunghia/DATNFUll/DuAnMauFE
  npm install
  npm run dev
  ```
  - [ ] Build thành công
  - [ ] Không có warning nghiêm trọng
  - [ ] Truy cập được: http://localhost:5173

---

### Giai đoạn 2: Kiểm tra Database

- [ ] **Chạy SQL Kiểm tra**
  - [ ] Mở file: `KIEM_TRA_DATABASE.sql`
  - [ ] Chạy Query #1: Kiểm tra sản phẩm trùng
    - Kết quả mong đợi: **0 dòng**
  - [ ] Chạy Query #2: Kiểm tra giá khuyến mãi
    - Kết quả mong đợi: Tất cả dòng = **'✅ ĐÚNG'**
  - [ ] Chạy Query #3: Xem sản phẩm có nhiều khuyến mãi
    - Xác nhận giá tốt nhất được chọn

- [ ] **Thêm UNIQUE Constraint** (Khuyến nghị)
  - [ ] Chạy Query #8 để thêm constraint
  - [ ] Verify: `EXEC sp_helpindex 'hoa_don_chi_tiet'`

---

### Giai đoạn 3: Test Chức Năng

#### Test 1: Thêm Sản Phẩm Mới

**Mục tiêu:** Kiểm tra thêm sản phẩm lần đầu vào hóa đơn

- [ ] Đăng nhập vào hệ thống
- [ ] Vào màn hình **Bán hàng tại quầy**
- [ ] Tạo hóa đơn mới hoặc chọn hóa đơn đang chờ
- [ ] Tìm kiếm sản phẩm A (chưa có trong giỏ)
- [ ] Click chọn sản phẩm A

**Kết quả mong đợi:**
- [ ] Sản phẩm A xuất hiện trong giỏ hàng
- [ ] Số lượng = 1
- [ ] Chỉ có **1 dòng** sản phẩm A
- [ ] Giá hiển thị đúng (có áp dụng khuyến mãi nếu có)
- [ ] Tổng tiền được cập nhật chính xác

---

#### Test 2: Thêm Sản Phẩm Trùng (QUAN TRỌNG)

**Mục tiêu:** Kiểm tra cộng số lượng khi thêm sản phẩm đã có

**Setup:**
- Giỏ hàng đã có: Sản phẩm A (số lượng = 2)

**Các bước:**
- [ ] Tìm kiếm lại sản phẩm A
- [ ] Click chọn sản phẩm A thêm 1 lần nữa

**Kết quả mong đợi:**
- [ ] Vẫn chỉ có **1 dòng** sản phẩm A
- [ ] Số lượng tăng lên **3** (2 + 1)
- [ ] **KHÔNG** tạo dòng mới
- [ ] Tổng tiền = Giá lẻ × 3
- [ ] Tồn kho giảm thêm 1

---

#### Test 3: Double-Click Nhanh

**Mục tiêu:** Kiểm tra chống spam click

- [ ] Tìm sản phẩm B
- [ ] Click chọn sản phẩm B **nhiều lần liên tiếp rất nhanh** (3-5 lần trong 1 giây)

**Kết quả mong đợi:**
- [ ] Chỉ thêm **1 lần** vào giỏ hàng
- [ ] Console log hiển thị: _"Đang xử lý yêu cầu trước, vui lòng đợi..."_
- [ ] Số lượng sản phẩm B = 1 (hoặc +1 nếu đã có)
- [ ] Không bị thêm nhiều lần

---

#### Test 4: Cập Nhật Số Lượng Thủ Công

**Mục tiêu:** Kiểm tra thay đổi số lượng trực tiếp

- [ ] Sản phẩm C trong giỏ có số lượng = 2, tồn kho = 10
- [ ] Thay đổi số lượng thành 5
- [ ] Enter hoặc blur khỏi input

**Kết quả mong đợi:**
- [ ] Số lượng cập nhật thành 5
- [ ] Tổng tiền = Giá lẻ × 5
- [ ] Tồn kho còn lại = 10 - 5 = 5
- [ ] Không tạo dòng mới

---

#### Test 5: Giá Khuyến Mãi

**Mục tiêu:** Kiểm tra giá được áp dụng đúng

**Setup:**
- Sản phẩm D có:
  - Giá gốc: 100,000đ
  - KM1: Giảm 10% → Giá = 90,000đ
  - KM2: Giảm 15,000đ → Giá = 85,000đ

**Các bước:**
- [ ] Thêm sản phẩm D vào giỏ
- [ ] Kiểm tra giá hiển thị

**Kết quả mong đợi:**
- [ ] Giá hiển thị = **85,000đ** (MIN)
- [ ] Đơn giá = 85,000đ × số lượng
- [ ] Icon/badge khuyến mãi hiển thị (nếu có)

---

#### Test 6: Xóa Sản Phẩm Khỏi Giỏ

**Mục tiêu:** Kiểm tra xóa sản phẩm

- [ ] Click nút xóa sản phẩm E khỏi giỏ
- [ ] Xác nhận xóa (nếu có popup)

**Kết quả mong đợi:**
- [ ] Sản phẩm E biến mất khỏi giỏ
- [ ] Tổng tiền được cập nhật
- [ ] Tồn kho sản phẩm E tăng trở lại
- [ ] Danh sách sản phẩm được refresh

---

#### Test 7: Hết Tồn Kho

**Mục tiêu:** Kiểm tra thông báo hết hàng

**Setup:**
- Sản phẩm F có tồn kho = 0

**Các bước:**
- [ ] Tìm và chọn sản phẩm F

**Kết quả mong đợi:**
- [ ] Hiển thị thông báo: _"Sản phẩm đã hết hàng!"_
- [ ] Không thêm vào giỏ
- [ ] Giỏ hàng không thay đổi

---

#### Test 8: Vượt Quá Tồn Kho

**Mục tiêu:** Kiểm tra giới hạn số lượng

**Setup:**
- Sản phẩm G: Tồn kho = 3
- Giỏ hàng đã có: Sản phẩm G số lượng = 2

**Các bước:**
- [ ] Thay đổi số lượng sản phẩm G thành 10
- [ ] Enter

**Kết quả mong đợi:**
- [ ] Hiển thị cảnh báo: _"Tồn kho không đủ. Đặt lại số lượng tối đa là 5"_
- [ ] Số lượng tự động điều chỉnh về **5** (2 trong giỏ + 3 tồn)
- [ ] Tổng tiền = Giá lẻ × 5

---

### Giai đoạn 4: Test Tích Hợp

#### Test 9: Quy Trình Bán Hàng Hoàn Chỉnh

- [ ] Tạo hóa đơn mới
- [ ] Thêm 3 sản phẩm khác nhau
- [ ] Thêm 1 sản phẩm trùng (kiểm tra cộng số lượng)
- [ ] Cập nhật số lượng 1 sản phẩm
- [ ] Xóa 1 sản phẩm
- [ ] Chọn khách hàng
- [ ] Chọn phương thức nhận hàng
- [ ] Áp dụng voucher (nếu có)
- [ ] Thanh toán

**Kết quả mong đợi:**
- [ ] Tất cả bước thực hiện mượt mà
- [ ] Không có sản phẩm trùng
- [ ] Tổng tiền chính xác
- [ ] Hóa đơn chuyển trạng thái thành công

---

#### Test 10: Nhiều Tab Hóa Đơn

- [ ] Mở tab hóa đơn 1
- [ ] Thêm sản phẩm A vào tab 1
- [ ] Tạo tab hóa đơn 2
- [ ] Thêm sản phẩm A vào tab 2
- [ ] Chuyển qua lại giữa 2 tab

**Kết quả mong đợi:**
- [ ] Mỗi tab độc lập
- [ ] Sản phẩm không bị chồng chéo giữa các tab
- [ ] Tổng tiền mỗi tab đúng

---

### Giai đoạn 5: Kiểm tra Database Lại

- [ ] **Sau khi test xong, chạy lại Query #1**
  ```sql
  -- Kiểm tra sản phẩm trùng
  ```
  - Kết quả: **0 dòng** ✅

- [ ] **Kiểm tra tồn kho chính xác**
  ```sql
  -- So sánh tồn kho trước và sau test
  ```

- [ ] **Xem log hóa đơn chi tiết**
  ```sql
  -- Query #5: Xem chi tiết hóa đơn vừa test
  ```

---

## 🐛 BẢNG GHI LỖI (NẾU CÓ)

| # | Mô tả lỗi | Test Case | Ảnh/Log | Trạng thái |
|---|-----------|-----------|---------|------------|
| 1 |           |           |         | [ ]        |
| 2 |           |           |         | [ ]        |
| 3 |           |           |         | [ ]        |

---

## 📊 KẾT QUẢ TỔNG QUAN

**Ngày test:** __________

**Người test:** __________

**Tổng số test case:** 10

**Số test PASS:** ______ / 10

**Số test FAIL:** ______ / 10

**Đánh giá:**
- [ ] ✅ Tất cả test PASS → Triển khai production
- [ ] ⚠️ Có test FAIL → Cần sửa lỗi thêm
- [ ] ❌ Nhiều test FAIL → Review lại toàn bộ

---

## 📝 GHI CHÚ

**Các vấn đề phát hiện:**


**Đề xuất cải tiến:**


**Tài liệu tham khảo:**
- File phân tích: `PHAN_TICH_VA_GIAI_PHAP.md`
- File tóm tắt: `TOMTAT_SUA_LOI.md`
- File SQL: `KIEM_TRA_DATABASE.sql`

---

## ✨ HOÀN TẤT

- [ ] Tất cả test case đã kiểm tra
- [ ] Lỗi đã được ghi nhận và xử lý
- [ ] Database đã được kiểm tra
- [ ] Tài liệu đã được cập nhật
- [ ] Code đã được commit
- [ ] Ready for deployment

**Chữ ký người test:** __________________

**Ngày:** __________________
