# CẬP NHẬT FRONTEND CHO ZALOPAY

## ✅ ĐÃ HOÀN THÀNH

### Backend:
- ✅ Thêm dependencies vào pom.xml
- ✅ Tạo ZaloPayConfig.java
- ✅ Tạo HMACUtil.java  
- ✅ Tạo ZaloPayService.java
- ✅ Tạo ZaloPayController.java

### Frontend Store:
- ✅ Thêm createZaloPayOrder() vào gbStore.js
- ✅ Thêm checkZaloPayStatus() vào gbStore.js

---

## 📋 BƯỚC TIẾP THEO: CẬP NHẬT UI

### File cần sửa: `TheHeader-BanHang.vue`

**Vị trí:** `/home/huunghia/DATNFUll/DuAnMauFE/src/components/admin-components/BanHang/TheHeader-BanHang.vue`

### 1. Thêm import icon (sau dòng ~185)

Tìm phần import icons, thêm:
```javascript
import { QrcodeOutlined } from '@ant-design/icons-vue';
```

### 2. Thêm state cho ZaloPay (sau dòng ~400, trong script setup)

Thêm vào phần khai báo biến:
```javascript
// ✅ ZALOPAY STATE
const showZaloPayModal = ref(false);
const zaloPayQRUrl = ref('');
const isLoadingZaloPay = ref(false);
const paymentStatus = ref(''); // checking, success, failed
let checkPaymentInterval = null;
```

### 3. Thêm option ZaloPay vào form thanh toán (dòng ~275-290)

Tìm phần "Hình thức thanh toán", thêm radio button ZaloPay:
```vue
<div class="form-check form-check-inline">
    <input class="form-check-input" type="radio" 
        :name="'hinhThucThanhToan_' + activeKey"
        :id="'zaloPay_' + activeKey" value="ZaloPay"
        v-model="activeTabData.hd.hinh_thuc_thanh_toan" 
        @change="updateHinhThucThanhToan" />
    <label class="form-check-label" :for="'zaloPay_' + activeKey">
        ZaloPay
    </label>
</div>

<!-- UI hiển thị nút QR ZaloPay -->
<div v-if="activeTabData.hd.hinh_thuc_thanh_toan === 'ZaloPay'" class="mt-3">
    <a-button type="primary" @click="showZaloPayQR" :loading="isLoadingZaloPay" block>
        <template #icon><qrcode-outlined /></template>
        Hiển thị mã QR thanh toán
    </a-button>
</div>
```

### 4. Thêm Modal hiển thị QR Code (sau phần form, trước </template>)

```vue
<!-- Modal hiển thị QR Code ZaloPay -->
<a-modal v-model:open="showZaloPayModal" title="Quét mã QR để thanh toán" 
    :footer="null" width="450px" @cancel="closeZaloPayModal">
    <div class="text-center p-3">
        <div v-if="zaloPayQRUrl">
            <img :src="zaloPayQRUrl" alt="ZaloPay QR Code" 
                style="width: 100%; max-width: 300px; border: 2px solid #0068FF; border-radius: 8px;" />
            <p class="mt-3 mb-2" style="font-size: 16px; font-weight: 500;">
                Quét mã QR bằng ứng dụng ZaloPay
            </p>
            <p class="text-muted mb-3">
                Tổng tiền: <span class="fw-bold">{{ formatCurrency(activeTabData.hd.tong_tien_sau_giam) }}</span>
            </p>
            
            <!-- Trạng thái thanh toán -->
            <a-alert v-if="paymentStatus === 'checking'" 
                type="info" 
                message="Đang chờ thanh toán..." 
                show-icon 
                class="mb-2" />
            <a-alert v-if="paymentStatus === 'success'" 
                type="success" 
                message="Thanh toán thành công!" 
                show-icon 
                class="mb-2" />
            <a-alert v-if="paymentStatus === 'failed'" 
                type="error" 
                message="Thanh toán thất bại hoặc đã hủy!" 
                show-icon 
                class="mb-2" />
        </div>
        <div v-else class="py-5">
            <a-spin size="large" />
            <p class="mt-3">Đang tạo mã QR...</p>
        </div>
    </div>
</a-modal>
```

### 5. Thêm các hàm xử lý ZaloPay (trong script setup)

```javascript
// ✅ HIỂN THỊ QR CODE ZALOPAY
const showZaloPayQR = async () => {
    try {
        isLoadingZaloPay.value = true;
        
        const result = await store.createZaloPayOrder(activeTabData.value.hd.id_hoa_don);
        
        if (result.return_code === 1) {
            zaloPayQRUrl.value = result.order_url;
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
                    
                    // Đóng tab hoặc reset
                    closeZaloPayModal();
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

// ✅ ĐÓNG MODAL ZALOPAY
const closeZaloPayModal = () => {
    if (checkPaymentInterval) {
        clearInterval(checkPaymentInterval);
        checkPaymentInterval = null;
    }
    showZaloPayModal.value = false;
    zaloPayQRUrl.value = '';
    paymentStatus.value = '';
};

// ✅ CLEANUP KHI COMPONENT BỊ DESTROY
onUnmounted(() => {
    if (checkPaymentInterval) {
        clearInterval(checkPaymentInterval);
    }
});
```

---

## 🎨 STYLING (Tùy chọn)

Thêm CSS vào phần `<style scoped>` để làm đẹp modal:

```css
.zalopay-modal .ant-modal-body {
    padding: 24px;
}

.zalopay-qr-container {
    background: linear-gradient(135deg, #0068FF 0%, #0098FF 100%);
    padding: 20px;
    border-radius: 12px;
}
```

---

## ✅ CHECKLIST FRONTEND

- [ ] Import QrcodeOutlined
- [ ] Thêm state ZaloPay (showZaloPayModal, zaloPayQRUrl, etc.)
- [ ] Thêm radio button "ZaloPay" vào form
- [ ] Thêm nút "Hiển thị mã QR"
- [ ] Thêm Modal hiển thị QR Code
- [ ] Thêm hàm showZaloPayQR()
- [ ] Thêm hàm startCheckingPaymentStatus()
- [ ] Thêm hàm closeZaloPayModal()
- [ ] Thêm onUnmounted cleanup

---

## 🚀 TEST

1. Start backend: `./mvnw spring-boot:run`
2. Start frontend: `npm run dev`
3. Tạo đơn hàng → Chọn "ZaloPay"
4. Click "Hiển thị mã QR"
5. Quét bằng app ZaloPay trên điện thoại
6. Xác nhận thanh toán
7. Kiểm tra trạng thái tự động cập nhật

---

## 📝 GHI CHÚ

- Modal sẽ tự động kiểm tra trạng thái mỗi 3 giây
- Khi thanh toán thành công, modal tự đóng sau 2 giây
- Hóa đơn tự động chuyển trạng thái "Đã thanh toán"
- Cleanup interval khi đóng modal hoặc component unmount

