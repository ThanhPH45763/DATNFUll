# SO SÁNH CÁC PHƯƠNG ÁN TÍNH PHÍ VẬN CHUYỂN

## Bạn đang có 2 file:

### 1️⃣ **shippingFeeCalculator.js** (Đang dùng - Fix cứng theo vùng)
- ✅ **Ưu điểm**: Đơn giản, nhanh, không tốn phí
- ❌ **Nhược điểm**: Không chính xác, phải cập nhật thủ công
- 💰 **Chi phí**: 0đ

### 2️⃣ **shippingFeeGPS.js** (Mới tạo - Tính theo vị trí thực)
- ✅ **Ưu điểm**: Chính xác theo khoảng cách thực tế
- ❌ **Nhược điểm**: Phức tạp hơn, có thể tốn phí API
- 💰 **Chi phí**: Xem bảng dưới

---

## 📊 BẢNG SO SÁNH CHI TIẾT

| Tiêu chí | Fix cứng (hiện tại) | Goong API | Google Maps | GPS Offline |
|----------|---------------------|-----------|-------------|-------------|
| **Độ chính xác** | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Chi phí** | 0đ | ~200k/tháng | ~500k/tháng | 0đ |
| **Tốc độ** | ⚡⚡⚡ | ⚡⚡ | ⚡⚡ | ⚡⚡⚡ |
| **Cần Internet** | ❌ | ✅ | ✅ | ❌ |
| **Dữ liệu VN** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Độ phức tạp** | Đơn giản | Trung bình | Trung bình | Đơn giản |

---

## 💡 KHUYẾN NGHỊ

### Nếu bạn là shop nhỏ, vừa (< 100 đơn/ngày):
→ **Dùng Fix cứng** (đang dùng) hoặc **GPS Offline** (miễn phí)

### Nếu bạn là shop lớn, chuyên nghiệp (> 100 đơn/ngày):
→ **Dùng Goong API** (chính xác + giá rẻ)

### Nếu bạn muốn tốt nhất, không quan tâm chi phí:
→ **Dùng Google Maps API**

---

## 🚀 HƯỚNG DẪN CHUYỂN ĐỔI

### Cách 1: Dùng GPS theo Goong API (Khuyên dùng cho VN)

**Bước 1:** Đăng ký tài khoản miễn phí
```
https://account.goong.io/register
```

**Bước 2:** Lấy API Key
- Vào Dashboard → API Keys → Copy key

**Bước 3:** Cập nhật code trong `formKhachHangBH.vue`

Thay thế dòng import:
```javascript
// Cũ
import { calculateShippingFee, formatVND } from '@/utils/shippingFeeCalculator';

// Mới
import { calculateShippingFeeByLocation, formatVND } from '@/utils/shippingFeeGPS';
```

**Bước 4:** Cập nhật hàm `updateShippingFee`
```javascript
const updateShippingFee = async (index) => {
    const diaChi = formData.diaChiList[index];
    if (diaChi.tinhThanhPho && diaChi.quanHuyen) {
        const diaChiDayDu = `${diaChi.soNha}, ${diaChi.xaPhuong}, ${diaChi.quanHuyen}, ${diaChi.tinhThanhPho}`;
        
        // Gọi API tính khoảng cách thực tế
        const result = await calculateShippingFeeByLocation(diaChiDayDu, diaChi.quanHuyen);
        
        if (result) {
            calculatedShippingFee.value = result.fee;
            
            // Hiển thị thông tin chi tiết
            toast.success(
                `📦 Khoảng cách: ${result.distance.toFixed(1)}km\n` +
                `⏱️ Thời gian: ~${result.duration} phút\n` +
                `💰 Phí: ${formatVND(result.fee)}`,
                { autoClose: 3000 }
            );
            
            // Cập nhật vào hóa đơn
            const idHoaDon = gbStore.getCurrentHoaDonId();
            if (idHoaDon) {
                await gbStore.setTrangThaiNhanHang(idHoaDon, 'Giao hàng', result.fee);
                localStorage.setItem('shippingFeeUpdated', JSON.stringify({
                    idHoaDon,
                    phiVanChuyen: result.fee,
                    distance: result.distance,
                    duration: result.duration,
                    timestamp: Date.now()
                }));
            }
        }
    }
};
```

**Bước 5:** Cập nhật API Key trong `shippingFeeGPS.js`
```javascript
const GOONG_API_KEY = 'API_KEY_CUA_BAN_O_DAY'
```

**Bước 6:** Lấy tọa độ GPS chính xác của shop
```
1. Vào Google Maps
2. Tìm: "Số 7 ngõ 324/167 Phương Canh, Nam Từ Liêm, Hà Nội"
3. Click chuột phải → Chọn "What's here?"
4. Copy tọa độ (VD: 21.0571, 105.7654)
```

Cập nhật trong `shippingFeeGPS.js`:
```javascript
export const SHOP_LOCATION = {
  address: 'Số 7 ngõ 324/167 Phương Canh, Nam Từ Liêm, Hà Nội',
  lat: 21.0571,  // ← Thay bằng tọa độ thực
  lng: 105.7654  // ← Thay bằng tọa độ thực
}
```

---

### Cách 2: Dùng GPS Offline (Miễn phí nhưng kém chính xác)

**Ưu điểm:**
- Không cần API key
- Không tốn phí
- Không cần Internet khi tính

**Nhược điểm:**
- Tính theo đường chim bay (sai lệch 20-30%)
- Phải cập nhật database tọa độ quận/huyện thủ công

**Cách dùng:** Tương tự Cách 1, nhưng không cần API key

---

## 📋 BẢNG GIÁ API

### Goong.io (Việt Nam)
| Gói | Requests/tháng | Giá |
|-----|----------------|-----|
| Free | 5,000 | 0đ |
| Basic | 50,000 | 200,000đ |
| Pro | 500,000 | 1,500,000đ |

**→ Nếu bạn có ~50 đơn/ngày = ~1,500 đơn/tháng → Dùng gói Free (5,000 requests) là đủ!**

### Google Maps API
| Gói | Requests/tháng | Giá |
|-----|----------------|-----|
| Free | 40,000 | 0đ |
| Trên 40,000 | Mỗi 1,000 | ~100,000đ |

**→ Đắt hơn Goong ~5 lần**

---

## ❓ CÂU HỎI THƯỜNG GẶP

### 1. Tôi nên chọn phương án nào?
- Shop nhỏ (< 30 đơn/ngày): **Fix cứng** (đang dùng)
- Shop vừa (30-100 đơn/ngày): **GPS Offline** hoặc **Goong Free**
- Shop lớn (> 100 đơn/ngày): **Goong Basic/Pro**

### 2. GPS có chính xác không?
- **Fix cứng**: Sai lệch ±20,000đ
- **GPS Offline**: Sai lệch ±5,000đ
- **Goong/Google API**: Sai lệch ±500đ (rất chính xác)

### 3. Tôi có thể kết hợp được không?
**Có!** Hệ thống tự động chọn:
1. Thử Goong API (nếu có key)
2. Thử Google API (nếu có key)
3. Dùng GPS Offline (nếu không có API)
4. Dùng Fix cứng (fallback cuối cùng)

### 4. API có bị giới hạn không?
Có, mỗi tháng có quota. Nếu vượt quota:
- Goong: Tự động chuyển sang GPS Offline
- Google: Báo lỗi hoặc tính phí thêm

### 5. Tôi có thể test trước không?
**Có!** Goong & Google đều có gói Free để test.

---

## 🎯 QUYẾT ĐỊNH NHANH

### Chọn Fix cứng nếu:
- ✅ Chỉ giao trong Hà Nội & lân cận
- ✅ Không quan tâm sai số ±10,000-20,000đ
- ✅ Muốn đơn giản, không phức tạp

### Chọn GPS (Goong) nếu:
- ✅ Giao toàn quốc
- ✅ Muốn chính xác
- ✅ Có < 5,000 đơn/tháng (dùng Free)
- ✅ Chấp nhận tích hợp API

---

**Bạn muốn chọn phương án nào? Tôi sẽ hỗ trợ integrate ngay!**
