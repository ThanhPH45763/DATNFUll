# 🔧 FIX: JAVA UNKNOWNHOSTEXCEPTION - sb-openapi.zalopay.vn

## 🐛 VẤN ĐỀ

```
java.net.UnknownHostException: sb-openapi.zalopay.vn
```

**Nguyên nhân:**
- ✅ Internet hoạt động bình thường (ping 8.8.8.8 OK)
- ✅ DNS hoạt động (host sb-openapi.zalopay.vn → 118.102.5.66)
- ✅ curl kết nối được ZaloPay API
- ❌ **NHƯNG Java không thể resolve DNS!**

## 🔍 NGUYÊN NHÂN

Java có DNS cache riêng và có thể bị vấn đề về:
1. IPv6 vs IPv4 conflict
2. DNS cache cũ
3. Network config trong JVM
4. Firewall/proxy chặn Java

## ✅ FIX 1: THÊM JVM ARGS (NHANH NHẤT)

### Cách 1: Chạy với JVM args

```bash
cd /home/huunghia/DATNFUll/duanbe

# Chạy với args disable IPv6 và prefer IPv4
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false"
```

### Cách 2: Thêm vào application.properties

**File:** `src/main/resources/application.properties`

Thêm vào cuối file:
```properties
# Fix DNS resolution
spring.jmx.enabled=false
java.net.preferIPv4Stack=true
```

## ✅ FIX 2: SỬA /etc/hosts (NẾU FIX 1 KHÔNG HIỆU QUẢ)

```bash
# Thêm ZaloPay IP vào /etc/hosts
sudo bash -c 'echo "118.102.5.66 sb-openapi.zalopay.vn" >> /etc/hosts'

# Kiểm tra
cat /etc/hosts | grep zalopay
```

## ✅ FIX 3: RESTART NETWORK SERVICE

```bash
# Restart network manager (nếu cần)
sudo systemctl restart NetworkManager

# Flush DNS cache
sudo systemd-resolve --flush-caches
```

## ✅ FIX 4: KIỂM TRA NGROK PORT SAI

**Bạn cũng có lỗi này:**

```
Forwarding  https://fourcha-adolph-noncondescending.ngrok-free.dev -> http://localhost:80
                                                                                      ^^
                                                                                    SAI!
```

Backend chạy trên **port 8080**, nhưng Ngrok forward **port 80**!

### Sửa:

```bash
# 1. Stop Ngrok
pkill ngrok

# 2. Chạy lại với port ĐÚNG
ngrok http 8080

# 3. Ngrok sẽ hiển thị:
# Forwarding  https://...ngrok-free.app -> http://localhost:8080
#                                                             ^^^^
#                                                            ĐÚNG!

# 4. Copy URL mới và update ZaloPayConfig.java
```

## 🧪 TEST SAU KHI FIX

### Test 1: Java có resolve được không?

Tạo file test: `/tmp/TestDNS.java`

```java
import java.net.InetAddress;

public class TestDNS {
    public static void main(String[] args) {
        try {
            System.out.println("Resolving sb-openapi.zalopay.vn...");
            InetAddress address = InetAddress.getByName("sb-openapi.zalopay.vn");
            System.out.println("✅ SUCCESS: " + address.getHostAddress());
        } catch (Exception e) {
            System.out.println("❌ FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

Chạy test:
```bash
cd /tmp
javac TestDNS.java
java TestDNS
```

**Kết quả mong đợi:**
```
Resolving sb-openapi.zalopay.vn...
✅ SUCCESS: 118.102.5.66
```

### Test 2: Restart backend với JVM args

```bash
cd /home/huunghia/DATNFUll/duanbe

./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.net.preferIPv4Stack=true"
```

Xem log có còn `UnknownHostException` không.

### Test 3: Tạo order lại

1. Vào trang bán hàng
2. Tạo order ZaloPay
3. Xem log backend

**Kết quả mong đợi:**
```
=== TẠO ORDER ZALOPAY ===
...
ZaloPay Response: {"return_code":1,"order_url":"https://..."}
✅ Đã lưu app_trans_id: 251117_...
=== END TẠO ORDER ===
```

**KHÔNG còn UnknownHostException!**

## 🎯 SUMMARY - LÀM THEO THỨ TỰ

### Bước 1: FIX NGROK PORT

```bash
pkill ngrok
ngrok http 8080  # ← Port 8080, không phải 80!
```

### Bước 2: UPDATE ZaloPayConfig.java

```java
// Lấy URL mới từ Ngrok và update
public static final String CALLBACK_URL = "https://NEW_NGROK_URL/api/zalopay/callback";
```

### Bước 3: FIX Java DNS

```bash
# Chạy backend với JVM args
cd /home/huunghia/DATNFUll/duanbe
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.net.preferIPv4Stack=true"
```

### Bước 4: TEST

1. Tạo order → Xem log không còn UnknownHostException
2. Thanh toán → Xem log có callback
3. Frontend → Modal tự động đóng

## 🔍 DEBUG NẾU VẪN BỊ LỖI

### Kiểm tra DNS từ Java:

```bash
cd /tmp
cat > TestDNS.java << 'EOF'
import java.net.InetAddress;
public class TestDNS {
    public static void main(String[] args) {
        try {
            System.setProperty("java.net.preferIPv4Stack", "true");
            InetAddress addr = InetAddress.getByName("sb-openapi.zalopay.vn");
            System.out.println("✅ " + addr.getHostAddress());
        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }
    }
}
EOF
javac TestDNS.java
java -Djava.net.preferIPv4Stack=true TestDNS
```

### Kiểm tra firewall:

```bash
# Kiểm tra có rule nào chặn Java không
sudo iptables -L -n | grep -i java
```

### Kiểm tra proxy:

```bash
# Xem có proxy environment variables không
env | grep -i proxy
```

Nếu có → Unset:
```bash
unset http_proxy
unset https_proxy
unset HTTP_PROXY
unset HTTPS_PROXY
```

## ✅ GIẢI PHÁP DỰ PHÒNG: HARDCODE IP

**Nếu tất cả đều thất bại**, sửa code tạm thời:

**File:** `ZaloPayService.java` (line ~86)

```java
// Tạm thời hardcode IP
String endpoint = "https://118.102.5.66/v2/create";  // Thay vì dùng domain

// Thêm header Host
post.setHeader("Host", "sb-openapi.zalopay.vn");
```

**NHƯNG đây chỉ là workaround, không nên dùng lâu dài!**

---

## 📞 NẾU VẪN KHÔNG ĐƯỢC

Gửi cho tôi output của:

```bash
# 1. Java version
java -version

# 2. Network config
ip addr show

# 3. DNS config
cat /etc/resolv.conf

# 4. Test DNS từ Java
java -Djava.net.preferIPv4Stack=true TestDNS

# 5. Backend log đầy đủ khi tạo order
```

Tôi sẽ phân tích thêm! 🔍
