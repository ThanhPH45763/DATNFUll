# 🔍 PHÂN TÍCH LỖI KHÔNG CHỌN ĐƯỢC SIZE SAU KHI CHỌN MÀU

## ❌ VẤN ĐỀ HIỆN TẠI

Người dùng chọn được màu nhưng KHÔNG chọn được size sau đó.

## 🔎 NGUYÊN NHÂN

### 1. **Logic trong `availableSizes` computed**

```javascript
// Dòng 2441-2473
const availableSizes = computed(() => {
    // Nếu chưa chọn màu, hiển thị tất cả size
    if (!selectedColor.value) {
        return product.value.kich_thuoc;  // ✅ OK
    }

    // Nếu đã chọn màu, lọc các size có trong màu đó
    const sizesForSelectedColor = [];
    const sizeMap = new Map();

    productDetails.value.forEach(variant => {
        if (variant.id_mau_sac === selectedColor.value) {  // ✅ Đúng
            if (!sizeMap.has(variant.id_kich_thuoc)) {
                const sizeInfo = product.value.kich_thuoc.find(size => size.ma === variant.id_kich_thuoc);
                
                if (sizeInfo) {
                    sizeMap.set(variant.id_kich_thuoc, true);
                    
                    // ❌ VẤN ĐỀ Ở ĐÂY!
                    const isAvailable = variant.trang_thai === 'Hoạt động' && variant.so_luong > 0;
                    
                    sizesForSelectedColor.push({
                        ...sizeInfo,
                        co_san: isAvailable,              // ❌ Sai!
                        trang_thai: variant.trang_thai    // ❌ Sai!
                    });
                }
            }
        }
    });

    return sizesForSelectedColor;
});
```

**Vấn đề:**
- `sizeInfo` đã có `co_san` và `trang_thai` từ ban đầu (dòng 697-698)
- Nhưng khi spread `...sizeInfo`, những giá trị này BỊ GHI ĐÈ bởi giá trị mới
- Nếu `isAvailable = false`, size sẽ bị disable mặc dù có thể có variant khác cùng màu cùng size với trạng thái khác

### 2. **Logic trong button size**

```html
<!-- Dòng 114-124 -->
<button v-for="(size, index) in availableSizes" :key="'size-' + index" 
    class="size-option"
    :class="{ 
        active: selectedSize === size.ma, 
        disabled: !size.co_san || size.trang_thai === 'Không hoạt động'  // ❌
    }"
    @click="selectSize(size)" 
    :disabled="!size.co_san || size.trang_thai === 'Không hoạt động'">  // ❌
    {{ size.ten }}
    <span v-if="size.trang_thai === 'Không hoạt động'" class="size-unavailable">✕</span>
</button>
```

**Vấn đề:**
- Điều kiện disable quá nghiêm ngặt
- `!size.co_san || size.trang_thai === 'Không hoạt động'` → Cả 2 điều kiện đều có thể làm disable

### 3. **Logic trong `selectSize`**

```javascript
// Dòng 921-940
const selectSize = (size) => {
    // Kiểm tra cả trạng thái của size và co_san
    if (size.co_san && size.trang_thai !== 'Không hoạt động') {  // ❌ Quá nghiêm ngặt
        selectedSize.value = size.ma;
        selectedSizeName.value = size.ten;
        updateSelectedVariant();
    } else {
        // Show warning
        notification.warning({...});
    }
};
```

**Vấn đề:** Điều kiện kiểm tra quá khắt khe, ngăn chặn việc chọn size hợp lệ

---

## ✅ GIẢI PHÁP

### **Fix 1: Sửa logic `availableSizes`**

Cần tổng hợp TẤT CẢ variant của cùng màu + cùng size, sau đó kiểm tra xem có BẤT KỲ variant nào available không:

```javascript
const availableSizes = computed(() => {
    if (!selectedColor.value) {
        return product.value.kich_thuoc;
    }

    const sizesForSelectedColor = [];
    const sizeMap = new Map(); // Map: id_kich_thuoc -> { có_variant_available, variants }

    // Bước 1: Tổng hợp tất cả variants theo size
    productDetails.value.forEach(variant => {
        if (variant.id_mau_sac === selectedColor.value) {
            if (!sizeMap.has(variant.id_kich_thuoc)) {
                sizeMap.set(variant.id_kich_thuoc, {
                    hasAvailable: false,
                    variants: []
                });
            }
            
            const sizeData = sizeMap.get(variant.id_kich_thuoc);
            sizeData.variants.push(variant);
            
            // Kiểm tra xem variant này có available không
            if (variant.trang_thai === 'Hoạt động' && variant.so_luong > 0) {
                sizeData.hasAvailable = true;
            }
        }
    });

    // Bước 2: Tạo danh sách size với trạng thái đúng
    sizeMap.forEach((sizeData, sizeId) => {
        const sizeInfo = product.value.kich_thuoc.find(size => size.ma === sizeId);
        
        if (sizeInfo) {
            sizesForSelectedColor.push({
                ...sizeInfo,
                co_san: sizeData.hasAvailable,
                trang_thai: sizeData.hasAvailable ? 'Hoạt động' : 'Không hoạt động'
            });
        }
    });

    console.log('Các size có sẵn cho màu', selectedColorName.value, ':', sizesForSelectedColor);
    return sizesForSelectedColor;
});
```

### **Fix 2: Đơn giản hóa điều kiện disable trong template**

```html
<button v-for="(size, index) in availableSizes" 
    :key="'size-' + index" 
    class="size-option"
    :class="{ 
        active: selectedSize === size.ma, 
        disabled: !size.co_san
    }"
    @click="selectSize(size)" 
    :disabled="!size.co_san">
    {{ size.ten }}
    <span v-if="!size.co_san" class="size-unavailable">✕</span>
</button>
```

### **Fix 3: Đơn giản hóa `selectSize`**

```javascript
const selectSize = (size) => {
    if (!size.co_san) {
        notification.warning({
            message: 'Kích thước không khả dụng',
            description: `Size ${size.ten} hiện tạm hết hàng.`,
            placement: 'topRight',
            duration: 3,
            style: { zIndex: 1500 }
        });
        return;
    }
    
    selectedSize.value = size.ma;
    selectedSizeName.value = size.ten;
    updateSelectedVariant();
};
```

---

## 📊 SO SÁNH TRƯỚC VÀ SAU

### TRƯỚC (SAI):
```javascript
// Một variant có trang_thai = 'Không hoạt động' 
// → Toàn bộ size bị disable!

const isAvailable = variant.trang_thai === 'Hoạt động' && variant.so_luong > 0;
sizesForSelectedColor.push({
    ...sizeInfo,
    co_san: isAvailable,      // ❌ Sai vì chỉ check 1 variant
    trang_thai: variant.trang_thai
});
```

### SAU (ĐÚNG):
```javascript
// Kiểm tra TẤT CẢ variants cùng màu + cùng size
// → Nếu có BẤT KỲ variant nào available → Size vẫn chọn được!

const sizeData = sizeMap.get(variant.id_kich_thuoc);
sizeData.variants.push(variant);

if (variant.trang_thai === 'Hoạt động' && variant.so_luong > 0) {
    sizeData.hasAvailable = true;  // ✅ Đúng!
}
```

---

## 🎯 KẾT QUẢ SAU KHI FIX

1. ✅ Chọn màu → Danh sách size được lọc đúng
2. ✅ Size available → Click được
3. ✅ Size hết hàng → Hiển thị dấu ✕ và không click được
4. ✅ Logic rõ ràng, dễ bảo trì
5. ✅ Không có false positive (size available nhưng bị disable nhầm)

---

## 🚀 CÁCH KIỂM TRA

1. **Test case 1:** Chọn màu → Kiểm tra tất cả size có hiển thị
2. **Test case 2:** Click vào size available → Phải chọn được
3. **Test case 3:** Click vào size hết hàng → Hiển thị warning
4. **Test case 4:** Chọn màu khác → Size list cập nhật đúng
5. **Test case 5:** Console.log kiểm tra `availableSizes` value

---

## 📝 DEBUG TIPS

Nếu vẫn lỗi, thêm debug vào code:

```javascript
const availableSizes = computed(() => {
    console.log('=== DEBUG availableSizes ===');
    console.log('selectedColor:', selectedColor.value);
    console.log('productDetails:', productDetails.value);
    
    if (!selectedColor.value) {
        console.log('Chưa chọn màu, trả về tất cả size');
        return product.value.kich_thuoc;
    }
    
    // ... rest of code
    
    console.log('Kết quả sizesForSelectedColor:', sizesForSelectedColor);
    console.log('=== END DEBUG ===');
    return sizesForSelectedColor;
});
```

Kiểm tra trong browser console khi click chọn màu!
