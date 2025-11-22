# Hướng dẫn RESTART Backend để áp dụng thay đổi

## Cách 1: Restart trong IDE (IntelliJ/Eclipse)

### IntelliJ IDEA:
1. Tìm tab "Run" ở dưới cùng màn hình
2. Click nút **Stop** (hình vuông đỏ) ⏹️
3. Đợi app dừng hoàn toàn (console hiện "Process finished")
4. Click nút **Run** (hình tam giác xanh) ▶️ hoặc nhấn `Shift + F10`
5. Đợi Spring Boot khởi động lại (xem log "Started Application in...")

### Eclipse:
1. Vào tab "Console"
2. Click nút **Terminate** (hình vuông đỏ)
3. Right-click vào project → Run As → Spring Boot App

---

## Cách 2: Restart bằng Terminal

```bash
# Bước 1: Tìm process đang chạy
ps aux | grep java

# Bước 2: Dừng process (thay <PID> bằng process ID)
kill -9 <PID>

# Bước 3: Khởi động lại
cd /home/huunghia/DATNFUll/duanbe
./mvnw spring-boot:run

# HOẶC nếu đã build jar:
java -jar target/duanbe-*.jar
```

---

## Cách 3: Restart Docker (nếu dùng)

```bash
docker-compose restart backend
# HOẶC
docker restart <container_name>
```

---

## ✅ Kiểm tra đã restart thành công

Sau khi restart, xem log console, tìm dòng:
```
Started DuanbeApplication in X.XXX seconds
```

---

## 🧪 Test lại sau khi restart

### Test 1: Call API trực tiếp
```bash
curl http://localhost:8080/admin/quan_ly_san_pham/getAllCTSPKM
```

Kiểm tra response có `gia_ban` đã giảm không.

### Test 2: Test trong giao diện
1. Mở trình duyệt
2. Hard refresh: `Ctrl + Shift + R` (xóa cache)
3. Vào trang Bán hàng tại quầy
4. Kiểm tra giá sản phẩm

---

## 🔍 Debug nếu vẫn không được

### Kiểm tra 1: Query có đúng không?

Chạy trực tiếp trong SSMS:
```sql
SELECT 
    GETDATE() AS ServerTimeUTC,
    DATEADD(HOUR, 7, GETDATE()) AS VietnamTime,
    km.ngay_bat_dau,
    km.ngay_het_han,
    CASE 
        WHEN DATEADD(HOUR, 7, GETDATE()) BETWEEN km.ngay_bat_dau AND km.ngay_het_han 
        THEN N'✅ MATCH'
        ELSE N'❌ NOT MATCH'
    END AS ket_qua
FROM khuyen_mai km
WHERE km.trang_thai = N'Đang diễn ra';
```

**Kết quả mong đợi:** `ket_qua` = `✅ MATCH`

### Kiểm tra 2: Code Java có được compile không?

```bash
cd /home/huunghia/DATNFUll/duanbe
./mvnw clean compile

# Kiểm tra có lỗi compile không
```

### Kiểm tra 3: Xem log backend

Khi gọi API `/getAllCTSPKM`, xem log có query SQL nào được execute không:

```properties
# Trong application.properties, bật log SQL:
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Restart lại và xem log, query phải có `DATEADD(HOUR, 7, GETDATE())`.

---

## ❓ Vẫn không được?

Gửi cho tôi:
1. Screenshot log sau khi restart
2. Kết quả của query test trong SSMS (BƯỚC 4)
3. Response của API `/getAllCTSPKM`
