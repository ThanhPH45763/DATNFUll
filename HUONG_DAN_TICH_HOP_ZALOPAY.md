# HƯỚNG DẪN TÍCH HỢP ZALOPAY VÀO BÁN HÀNG

## 📋 BƯỚC 1: ĐĂNG KÝ TÀI KHOẢN ZALOPAY SANDBOX

### 1.1. Truy cập ZaloPay Developer
```
URL: https://docs.zalopay.vn/
```

### 1.2. Đăng ký tài khoản Sandbox
- Vào: https://sbx-merchant.zalopay.vn/
- Đăng ký tài khoản merchant (nhà bán hàng)
- Lấy thông tin:
  - **App ID**: ID của ứng dụng
  - **Key1**: Dùng để ký dữ liệu
  - **Key2**: Dùng để callback

### 1.3. Thông tin Sandbox (có sẵn để test)
```properties
# Môi trường Sandbox
app_id=2553
key1=PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL
key2=kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz
endpoint=https://sb-openapi.zalopay.vn/v2/create
```

## 📋 BƯỚC 2: CÀI ĐẶT THƯ VIỆN BACKEND (JAVA)

### 2.1. Thêm dependencies vào `pom.xml`
```xml
<!-- ZaloPay dependencies -->
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.13</version>
</dependency>
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
    <version>1.15</version>
</dependency>
```

## 📋 BƯỚC 3: TẠO CONFIG CHO ZALOPAY

### 3.1. Tạo file `ZaloPayConfig.java`
```java
package com.example.duanbe.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ZaloPayConfig {
    // Sandbox credentials
    public static final String APP_ID = "2553";
    public static final String KEY1 = "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL";
    public static final String KEY2 = "kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz";
    public static final String ENDPOINT_CREATE = "https://sb-openapi.zalopay.vn/v2/create";
    public static final String ENDPOINT_QUERY = "https://sb-openapi.zalopay.vn/v2/query";
    public static final String CALLBACK_URL = "http://localhost:8080/api/zalopay/callback";
    public static final String REDIRECT_URL = "http://localhost:3000/admin/banhang";
}
```

## 📋 BƯỚC 4: TẠO HELPER CLASS

### 4.1. Tạo `HMACUtil.java` - Mã hóa HMAC SHA256
```java
package com.example.duanbe.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public class HMACUtil {
    public static String HMacHexStringEncode(String algorithm, String key, String data) {
        try {
            Mac hmac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);
            hmac.init(secretKeySpec);
            byte[] hmacBytes = hmac.doFinal(data.toByteArray());
            return Hex.encodeHexString(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC", e);
        }
    }
}
```

## 📋 BƯỚC 5: TẠO SERVICE XỬ LÝ ZALOPAY

### 5.1. Tạo `ZaloPayService.java`
```java
package com.example.duanbe.service;

import com.example.duanbe.config.ZaloPayConfig;
import com.example.duanbe.utils.HMACUtil;
import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ZaloPayService {
    
    private final Gson gson = new Gson();
    
    /**
     * Tạo đơn hàng ZaloPay
     * @param maHoaDon Mã hóa đơn
     * @param tongTien Tổng tiền (VNĐ)
     * @param moTa Mô tả đơn hàng
     * @return Map chứa order_url để hiển thị QR code
     */
    public Map<String, Object> createOrder(String maHoaDon, BigDecimal tongTien, String moTa) {
        try {
            // 1. Tạo app_trans_id (unique cho mỗi giao dịch)
            String appTransId = getCurrentTimeString("yyMMdd") + "_" + maHoaDon;
            
            // 2. Tạo embed_data
            Map<String, String> embedData = new HashMap<>();
            embedData.put("redirecturl", ZaloPayConfig.REDIRECT_URL);
            
            // 3. Tạo item (danh sách sản phẩm)
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("itemid", maHoaDon);
            item.put("itemname", "Hóa đơn " + maHoaDon);
            item.put("itemprice", tongTien.longValue());
            item.put("itemquantity", 1);
            items.add(item);
            
            // 4. Tạo order data
            Map<String, Object> order = new HashMap<>();
            order.put("app_id", ZaloPayConfig.APP_ID);
            order.put("app_trans_id", appTransId);
            order.put("app_user", "user_" + maHoaDon);
            order.put("app_time", System.currentTimeMillis());
            order.put("amount", tongTien.longValue());
            order.put("description", moTa);
            order.put("bank_code", "");
            order.put("item", gson.toJson(items));
            order.put("embed_data", gson.toJson(embedData));
            order.put("callback_url", ZaloPayConfig.CALLBACK_URL);
            
            // 5. Tạo MAC (chữ ký)
            String data = order.get("app_id") + "|" 
                        + order.get("app_trans_id") + "|" 
                        + order.get("app_user") + "|" 
                        + order.get("amount") + "|" 
                        + order.get("app_time") + "|" 
                        + order.get("embed_data") + "|" 
                        + order.get("item");
            
            String mac = HMACUtil.HMacHexStringEncode("HmacSHA256", ZaloPayConfig.KEY1, data);
            order.put("mac", mac);
            
            // 6. Gọi API ZaloPay
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(ZaloPayConfig.ENDPOINT_CREATE);
            
            StringEntity entity = new StringEntity(gson.toJson(order), "UTF-8");
            entity.setContentType("application/json");
            post.setEntity(entity);
            
            CloseableHttpResponse response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            
            Map<String, Object> result = gson.fromJson(responseString, Map.class);
            result.put("app_trans_id", appTransId); // Trả về để lưu DB
            
            return result;
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("return_code", -1);
            error.put("return_message", e.getMessage());
            return error;
        }
    }
    
    /**
     * Kiểm tra trạng thái giao dịch
     */
    public Map<String, Object> queryOrder(String appTransId) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("app_id", ZaloPayConfig.APP_ID);
            params.put("app_trans_id", appTransId);
            
            String data = params.get("app_id") + "|" + params.get("app_trans_id") + "|" + ZaloPayConfig.KEY1;
            String mac = HMACUtil.HMacHexStringEncode("HmacSHA256", ZaloPayConfig.KEY1, data);
            params.put("mac", mac);
            
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(ZaloPayConfig.ENDPOINT_QUERY);
            
            StringEntity entity = new StringEntity(gson.toJson(params), "UTF-8");
            entity.setContentType("application/json");
            post.setEntity(entity);
            
            CloseableHttpResponse response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());
            
            return gson.fromJson(responseString, Map.class);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("return_code", -1);
            error.put("return_message", e.getMessage());
            return error;
        }
    }
    
    private String getCurrentTimeString(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }
}
```

## 📋 BƯỚC 6: TẠO CONTROLLER XỬ LÝ API

### 6.1. Tạo `ZaloPayController.java`
```java
package com.example.duanbe.controller;

import com.example.duanbe.entity.HoaDon;
import com.example.duanbe.repository.HoaDonRepo;
import com.example.duanbe.service.ZaloPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/zalopay")
@CrossOrigin(origins = "*")
public class ZaloPayController {
    
    @Autowired
    private ZaloPayService zaloPayService;
    
    @Autowired
    private HoaDonRepo hoaDonRepo;
    
    /**
     * Tạo đơn hàng ZaloPay và trả về QR code
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestParam Integer idHoaDon) {
        try {
            HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            String moTa = "Thanh toán hóa đơn " + hoaDon.getMa_hoa_don();
            
            Map<String, Object> result = zaloPayService.createOrder(
                hoaDon.getMa_hoa_don(),
                hoaDon.getTong_tien_sau_giam(),
                moTa
            );
            
            // Lưu app_trans_id vào DB để tracking
            if (result.get("return_code").equals(1.0)) {
                hoaDon.setGhi_chu("ZaloPay:" + result.get("app_trans_id"));
                hoaDonRepo.save(hoaDon);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "return_code", -1,
                "return_message", e.getMessage()
            ));
        }
    }
    
    /**
     * Kiểm tra trạng thái thanh toán
     */
    @GetMapping("/check-status")
    public ResponseEntity<?> checkStatus(@RequestParam Integer idHoaDon) {
        try {
            HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            String ghiChu = hoaDon.getGhi_chu();
            if (ghiChu == null || !ghiChu.startsWith("ZaloPay:")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "return_code", -1,
                    "return_message", "Chưa tạo đơn hàng ZaloPay"
                ));
            }
            
            String appTransId = ghiChu.replace("ZaloPay:", "");
            Map<String, Object> result = zaloPayService.queryOrder(appTransId);
            
            // Nếu thanh toán thành công, cập nhật hóa đơn
            if (result.get("return_code").equals(1.0)) {
                hoaDon.setTrang_thai("Đã thanh toán");
                hoaDon.setHinh_thuc_thanh_toan("Chuyển khoản (ZaloPay)");
                hoaDonRepo.save(hoaDon);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "return_code", -1,
                "return_message", e.getMessage()
            ));
        }
    }
    
    /**
     * Callback từ ZaloPay (khi thanh toán thành công)
     */
    @PostMapping("/callback")
    public ResponseEntity<?> callback(@RequestBody Map<String, Object> jsonData) {
        try {
            // Xử lý callback từ ZaloPay
            // TODO: Implement callback handler
            
            Map<String, Object> result = new HashMap<>();
            result.put("return_code", 1);
            result.put("return_message", "success");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("return_code", -1);
            result.put("return_message", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
}
```

---

**TIẾP TỤC BƯỚC 7 (FRONTEND) →**

## 📋 BƯỚC 7: TÍCH HỢP FRONTEND (VUE.JS)

### 7.1. Cập nhật store để gọi API ZaloPay

Thêm vào `stores/gbStore.js`:
```javascript
// Tạo đơn hàng ZaloPay
async createZaloPayOrder(idHoaDon) {
    try {
        const response = await axios.post(`${url}/api/zalopay/create-order`, null, {
            params: { idHoaDon }
        });
        return response.data;
    } catch (error) {
        console.error('Lỗi tạo đơn ZaloPay:', error);
        throw error;
    }
},

// Kiểm tra trạng thái thanh toán
async checkZaloPayStatus(idHoaDon) {
    try {
        const response = await axios.get(`${url}/api/zalopay/check-status`, {
            params: { idHoaDon }
        });
        return response.data;
    } catch (error) {
        console.error('Lỗi kiểm tra trạng thái:', error);
        throw error;
    }
}
```

### 7.2. Cập nhật UI trong TheHeader-BanHang.vue

Thêm nút thanh toán ZaloPay vào form thanh toán (sau dòng 293):

```vue
<template>
  <!-- ... existing code ... -->
  
  <div class="mb-3">
      <label class="form-label d-block mb-2">Hình thức thanh toán</label>
      <div class="form-check form-check-inline">
          <input class="form-check-input" type="radio" :name="'hinhThucThanhToan_' + activeKey"
              :id="'tienMat_' + activeKey" value="Tiền mặt"
              v-model="activeTabData.hd.hinh_thuc_thanh_toan" @change="updateHinhThucThanhToan" />
          <label class="form-check-label" :for="'tienMat_' + activeKey">Tiền mặt</label>
      </div>
      <div class="form-check form-check-inline">
          <input class="form-check-input" type="radio" :name="'hinhThucThanhToan_' + activeKey"
              :id="'chuyenKhoan_' + activeKey" value="Chuyển khoản"
              v-model="activeTabData.hd.hinh_thuc_thanh_toan" @change="updateHinhThucThanhToan" />
          <label class="form-check-label" :for="'chuyenKhoan_' + activeKey">Chuyển khoản</label>
      </div>
      <!-- ✅ THÊM OPTION ZALOPAY -->
      <div class="form-check form-check-inline">
          <input class="form-check-input" type="radio" :name="'hinhThucThanhToan_' + activeKey"
              :id="'zaloPay_' + activeKey" value="ZaloPay"
              v-model="activeTabData.hd.hinh_thuc_thanh_toan" @change="updateHinhThucThanhToan" />
          <label class="form-check-label" :for="'zaloPay_' + activeKey">
              <img src="@/images/logo/zalopay-logo.png" alt="ZaloPay" style="height: 20px; vertical-align: middle;" />
              ZaloPay
          </label>
      </div>
      
      <!-- UI hiển thị QR Code ZaloPay -->
      <div v-if="activeTabData.hd.hinh_thuc_thanh_toan === 'ZaloPay'" class="mt-3">
          <a-button type="primary" @click="showZaloPayQR" :loading="isLoadingZaloPay" block>
              <template #icon><qrcode-outlined /></template>
              Hiển thị mã QR thanh toán
          </a-button>
      </div>
      
      <!-- Existing code for Tiền mặt -->
      <div v-if="activeTabData.hd.hinh_thuc_thanh_toan === 'Tiền mặt'" class="mt-2">
          <!-- ... existing code ... -->
      </div>
  </div>
  
  <!-- Modal hiển thị QR Code ZaloPay -->
  <a-modal v-model:open="showZaloPayModal" title="Quét mã QR để thanh toán" :footer="null" width="400px">
      <div class="text-center">
          <div v-if="zaloPayQRUrl">
              <img :src="zaloPayQRUrl" alt="ZaloPay QR Code" style="width: 100%; max-width: 300px;" />
              <p class="mt-3">Quét mã QR bằng ứng dụng ZaloPay</p>
              <p class="text-muted">Tổng tiền: {{ formatCurrency(activeTabData.hd.tong_tien_sau_giam) }}</p>
              
              <!-- Trạng thái thanh toán -->
              <a-alert v-if="paymentStatus === 'checking'" type="info" message="Đang chờ thanh toán..." show-icon />
              <a-alert v-if="paymentStatus === 'success'" type="success" message="Thanh toán thành công!" show-icon />
              <a-alert v-if="paymentStatus === 'failed'" type="error" message="Thanh toán thất bại!" show-icon />
          </div>
          <a-spin v-else size="large" />
      </div>
  </a-modal>
  
  <!-- ... existing code ... -->
</template>

<script setup>
import { QrcodeOutlined } from '@ant-design/icons-vue';

// ✅ THÊM STATE CHO ZALOPAY
const showZaloPayModal = ref(false);
const zaloPayQRUrl = ref('');
const isLoadingZaloPay = ref(false);
const paymentStatus = ref(''); // checking, success, failed
let checkPaymentInterval = null;

// ✅ HIỂN THỊ QR CODE ZALOPAY
const showZaloPayQR = async () => {
    try {
        isLoadingZaloPay.value = true;
        
        const result = await store.createZaloPayOrder(activeTabData.value.hd.id_hoa_don);
        
        if (result.return_code === 1) {
            zaloPayQRUrl.value = result.order_url; // URL của QR code
            showZaloPayModal.value = true;
            paymentStatus.value = 'checking';
            
            // Bắt đầu kiểm tra trạng thái thanh toán mỗi 3 giây
            startCheckingPaymentStatus();
        } else {
            message.error(result.return_message || 'Không thể tạo mã QR thanh toán');
        }
    } catch (error) {
        console.error('Lỗi khi tạo QR ZaloPay:', error);
        message.error('Đã xảy ra lỗi khi tạo mã thanh toán');
    } finally {
        isLoadingZaloPay.value = false;
    }
};

// ✅ KIỂM TRA TRẠNG THÁI THANH TOÁN
const startCheckingPaymentStatus = () => {
    checkPaymentInterval = setInterval(async () => {
        try {
            const result = await store.checkZaloPayStatus(activeTabData.value.hd.id_hoa_don);
            
            if (result.return_code === 1) {
                // Thanh toán thành công
                paymentStatus.value = 'success';
                clearInterval(checkPaymentInterval);
                
                setTimeout(() => {
                    showZaloPayModal.value = false;
                    message.success('Thanh toán ZaloPay thành công!');
                    
                    // Refresh hóa đơn
                    refreshHoaDon(activeTabData.value.hd.id_hoa_don);
                }, 2000);
                
            } else if (result.return_code === 2) {
                // Đang xử lý
                paymentStatus.value = 'checking';
            } else {
                // Thất bại hoặc đã hủy
                paymentStatus.value = 'failed';
                clearInterval(checkPaymentInterval);
            }
        } catch (error) {
            console.error('Lỗi khi kiểm tra trạng thái:', error);
        }
    }, 3000); // Kiểm tra mỗi 3 giây
};

// ✅ CLEANUP KHI ĐÓNG MODAL
watch(showZaloPayModal, (newVal) => {
    if (!newVal) {
        if (checkPaymentInterval) {
            clearInterval(checkPaymentInterval);
            checkPaymentInterval = null;
        }
        zaloPayQRUrl.value = '';
        paymentStatus.value = '';
    }
});

// Cleanup khi component bị destroy
onUnmounted(() => {
    if (checkPaymentInterval) {
        clearInterval(checkPaymentInterval);
    }
});
</script>
```

## 📋 BƯỚC 8: TẢI VÀ CÀI ĐẶT APP ZALOPAY SANDBOX

### 8.1. Tải app ZaloPay trên điện thoại

**Android:**
1. Vào Google Play Store
2. Tìm kiếm "ZaloPay"
3. Tải về và cài đặt

**iOS:**
1. Vào App Store
2. Tìm kiếm "ZaloPay"
3. Tải về và cài đặt

### 8.2. Đăng ký tài khoản ZaloPay

1. Mở app ZaloPay
2. Đăng ký tài khoản bằng số điện thoại
3. Xác thực OTP
4. Hoàn tất đăng ký

### 8.3. Nạp tiền vào tài khoản Sandbox

**LƯU Ý:** Môi trường Sandbox sử dụng tiền ảo, KHÔNG PHẢI tiền thật!

```
Số điện thoại test: 0123456789
PIN: 111111
OTP: 222222
```

## 📋 BƯỚC 9: TEST THANH TOÁN

### 9.1. Test trên máy tính

1. **Khởi động backend:**
```bash
cd duanbe
./mvnw spring-boot:run
```

2. **Khởi động frontend:**
```bash
cd DuAnMauFE
npm run dev
```

3. **Truy cập trang bán hàng:**
```
http://localhost:3000/admin/banhang
```

4. **Thực hiện thanh toán:**
   - Thêm sản phẩm vào hóa đơn
   - Chọn "Hình thức thanh toán" = **ZaloPay**
   - Click "Hiển thị mã QR thanh toán"
   - Mã QR sẽ hiển thị

### 9.2. Test trên điện thoại

1. **Mở app ZaloPay trên điện thoại**

2. **Quét mã QR:**
   - Click vào icon "Quét mã"
   - Quét mã QR hiển thị trên màn hình máy tính

3. **Xác nhận thanh toán:**
   - Kiểm tra thông tin đơn hàng
   - Nhập PIN để xác nhận
   - Thanh toán thành công

4. **Kiểm tra kết quả:**
   - Trên màn hình máy tính sẽ hiển thị "Thanh toán thành công!"
   - Hóa đơn tự động chuyển trạng thái "Đã thanh toán"

## 📋 BƯỚC 10: XỬ LÝ LỖI THƯỜNG GẶP

### 10.1. Lỗi "Invalid MAC"
```
Nguyên nhân: Chữ ký (MAC) không đúng
Giải pháp: Kiểm tra lại key1, key2 và thứ tự tham số khi tạo MAC
```

### 10.2. Lỗi "Invalid app_trans_id"
```
Nguyên nhân: app_trans_id trùng lặp hoặc sai format
Giải pháp: Đảm bảo app_trans_id unique cho mỗi giao dịch
Format: yyMMdd_xxxxx (ví dụ: 251115_HD001)
```

### 10.3. QR Code không hiển thị
```
Nguyên nhân: 
- API endpoint sai
- CORS chưa được cấu hình
- Thiếu dependencies

Giải pháp:
- Kiểm tra lại endpoint API
- Thêm @CrossOrigin trong controller
- Kiểm tra lại pom.xml
```

### 10.4. Callback không hoạt động
```
Nguyên nhân: 
- localhost không public
- ZaloPay không thể gọi được callback URL

Giải pháp:
- Sử dụng ngrok để public localhost:
  ngrok http 8080
- Cập nhật CALLBACK_URL trong config
```

## 📋 BƯỚC 11: SỬ DỤNG NGROK ĐỂ TEST CALLBACK

### 11.1. Tải ngrok
```bash
# Download từ: https://ngrok.com/download
# Hoặc cài đặt qua npm:
npm install -g ngrok
```

### 11.2. Chạy ngrok
```bash
ngrok http 8080
```

### 11.3. Cập nhật callback URL
```java
// Trong ZaloPayConfig.java
public static final String CALLBACK_URL = "https://xxxx.ngrok.io/api/zalopay/callback";
// Thay xxxx bằng domain ngrok của bạn
```

## 📊 RESPONSE CODE TỪ ZALOPAY

| Return Code | Ý nghĩa |
|-------------|---------|
| 1 | Thành công |
| 2 | Đơn hàng đang xử lý |
| 3 | Đơn hàng bị từ chối |
| -1 | Lỗi hệ thống |
| -49 | MAC không hợp lệ |

## ✅ CHECKLIST

- [ ] Đã thêm dependencies vào pom.xml
- [ ] Đã tạo ZaloPayConfig.java
- [ ] Đã tạo HMACUtil.java
- [ ] Đã tạo ZaloPayService.java
- [ ] Đã tạo ZaloPayController.java
- [ ] Đã cập nhật store.js
- [ ] Đã cập nhật UI TheHeader-BanHang.vue
- [ ] Đã tải app ZaloPay trên điện thoại
- [ ] Đã test thanh toán thành công
- [ ] Đã kiểm tra callback (nếu cần)

## 📞 HỖ TRỢ

**ZaloPay Developer:**
- Docs: https://docs.zalopay.vn/
- SDK: https://github.com/zalopay-oss
- Support: developer@zalopay.vn

**Tài liệu tham khảo:**
- API Reference: https://docs.zalopay.vn/v2/
- Sandbox Guide: https://docs.zalopay.vn/sandbox/

---

🎉 **HOÀN TẤT!** Bây giờ bạn đã có thể test thanh toán ZaloPay trong ứng dụng bán hàng!
