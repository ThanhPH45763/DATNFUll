<template>
    <div>
        <table class="table table-hover fade-in-table">
            <thead>
                <tr>
                    <th scope="col">STT</th>
                    <th scope="col">Mã sản phẩm</th>
                    <th scope="col">Hình ảnh</th>
                    <th scope="col">Mô tả</th>
                    <th scope="col">Trạng thái</th>
                    <th scope="col">Giới tính</th>
                    <th scope="col">Chất liệu</th>
                    <th scope="col">Thương hiệu</th>
                    <th scope="col">Danh mục</th>
                    <th scope="col">Màu</th>
                    <th scope="col">Kích thước</th>
                    <th scope="col">Hành động</th>
                </tr>
            </thead>
            <tbody v-if="store.searchs === ''">
                <tr v-for="(ctsp, index) in store.getAllChiTietSanPham" :key="ctsp.id_chi_tiet_san_pham">
                    <th scope="row">{{ index + 1 }}</th>
                    <td><img style="width: 40px; height: auto;" :src="ctsp.hinh_anh" alt="sp"></td>
                    <td>{{ ctsp.ten_san_pham }}</td>

                    <td>Đôn có gì</td>
                    <td>
                        <a-switch :checked="ctsp.trang_thai == 'Còn hàng' ? true : false" />
                    </td>
                    <td>{{ ctsp.gioi_tinh ? 'Nam' : 'Nữ' }}</td>
                    <td>{{ ctsp.ten_chat_lieu }}</td>
                    <td>{{ ctsp.ten_thuong_hieu }}</td>
                    <td>{{ ctsp.ten_danh_muc }}</td>
                    <td :style="{ backgroundColor: selectColor(ctsp.ten_mau), width: '40px', height: '20px' }">
                    </td>
                    <td>{{ ctsp.gia_tri + ' ' + ctsp.don_vi }}</td>

                </tr>
            </tbody>
            <tbody v-else>
                <tr v-for="(ctsp, index) in store.searchChiTietSanPham" :key="ctsp.id_chi_tiet_san_pham">
                    <th scope="row">{{ index + 1 }}</th>
                    <td>{{ ctsp.ten_san_pham }}</td>
                    <td><img style="width: 40px; height: auto;" :src="ctsp.hinh_anh" alt="sp"></td>
                    <td>Đôn có gì</td>
                    <td>
                        <a-switch :checked="ctsp.trang_thai == 'Còn hàng' ? true : false" />
                    </td>
                    <td>{{ ctsp.gioi_tinh ? 'Nam' : 'Nữ' }}</td>
                    <td>{{ ctsp.ten_chat_lieu }}</td>
                    <td>{{ ctsp.ten_thuong_hieu }}</td>
                    <td>{{ ctsp.ten_danh_muc }}</td>
                    <td :style="{ backgroundColor: selectColor(ctsp.ten_mau), width: '40px', height: '20px' }">
                    </td>
                    <td>{{ ctsp.gia_tri + ' ' + ctsp.don_vi }}</td>

                </tr>
            </tbody>
        </table>
    </div>
</template>

<script setup>
import { useGbStore } from '@/stores/gbStore';
import { onMounted, watch } from 'vue';

const store = useGbStore();

// Thêm hàm debounce
const debounce = (fn, delay) => {
    let timeoutId;
    return (...args) => {
        // Hủy timeout cũ nếu có
        if (timeoutId) {
            clearTimeout(timeoutId);
        }
        // Tạo timeout mới
        timeoutId = setTimeout(() => {
            fn(...args);
        }, delay);
    };
};

const selectColor = (color) => {
    const colors = {
        'Đỏ': 'red',
        'Xanh': 'blue',
        'Xanh lá': 'green',
        'Vàng': 'yellow',
        'Cam': 'orange',
        'Tím': 'purple',
        'Hồng': 'pink',
        'Nâu': 'brown',
        'Đen': 'black',
        'Trắng': 'white',
        'Xám': 'gray',
    };
    return colors[color] || 'black'; // Mặc định trả về 'black' nếu không tìm thấy màu
};

onMounted(async () => {
    try {
        console.log("Component mounted, fetching data...");
        await store.getAllCTSP();
    } catch (error) {
        console.error("Error fetching data:", error);
    }
});

// Áp dụng debounce vào watch function
watch(
    () => store.searchs,
    debounce(async (newValue) => {
        try {
            if (newValue) {
                await store.searchCTSP(newValue);
            } else {
                await store.getAllCTSP();
            }
        } catch (error) {
            console.error("Error searching:", error);
        }
    }, 300),
    { immediate: true }
);
</script>

<style scoped>
.fade-in-table {
    opacity: 0;
    transform: translateY(10px);
    animation: fadeIn 0.3s ease-out forwards;
}

table {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
    font-size: 14px;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>