<template>
    <!-- <div class="container"> -->
    <div class="d-flex justify-content-between align-items-center mb-3">
        <div class="d-flex align-items-center gap-2">
            <a-form-item name="range-time-picker" label="Thời gian" class="mb-0">
                <a-range-picker v-model:value="formState['range-time-picker']" show-time format="DD-MM-YYYY HH:mm:ss"
                    value-format="YYYY-MM-DD HH:mm:ss" :placeholder="['Chọn ngày bắt đầu', 'Chọn ngày kết thúc']" />
            </a-form-item>
        </div>
        <div class="d-flex gap-3">
            <label class="fw-bold">Loại hóa đơn:</label>
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" id="tatCa" value="Tất cả" v-model="selectedLoaiHoaDon"
                    @change="handleLoaiHoaDonChange" />
                <label class="form-check-label" for="tatCa">Tất cả</label>
            </div>
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" id="offline" value="Offline" v-model="selectedLoaiHoaDon"
                    @change="handleLoaiHoaDonChange" />
                <label class="form-check-label" for="offline">Offline</label>
            </div>
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" id="online" value="Online" v-model="selectedLoaiHoaDon"
                    @change="handleLoaiHoaDonChange" />
                <label class="form-check-label" for="online">Online</label>
            </div>
        </div>
        <div class="d-flex align-items-center">
            <label for="limitSelect" class="me-2 fw-medium text-muted mb-0">Hiển thị:</label>
            <select id="limitSelect" class="form-select form-select-sm w-auto" v-model="pageSize"
                @change="fetchData(0)">
                <option value="5">5</option>
                <option value="10">10</option>
                <option value="20">20</option>
            </select>
        </div>
    </div>
    <div class="d-flex justify-content-between align-items-center border-bottom pb-2">
        <h5 class="fw-bold mb-0" style="color: #ff6600;">📋 Danh sách đơn hàng</h5>
    </div>

    <div class="table-responsive mt-4">
        <!-- Dải trạng thái ngang -->
        <div class="d-flex flex-wrap gap-2 mt-2 status-strip">
            <a-badge :count="totalHoaDonCount" :overflow-count="999">
                <button class="btn d-flex align-items-center flex-fill" :class="{
                    'btn-primary': valueTrangThaiDonHang === 'Tất cả',
                    'btn-outline-primary': valueTrangThaiDonHang !== 'Tất cả',
                }" @click="handleTrangThaiChange('Tất cả')">
                    Tất cả
                </button>
            </a-badge>
            <a-badge v-for="option in trangThaiDonHangOptions" :key="option.value"
                :count="countByTrangThai[option.value] || 0" :overflow-count="999">
                <button class="btn d-flex align-items-center flex-fill" :class="{
                    'btn-primary': valueTrangThaiDonHang === option.value,
                    'btn-outline-primary': valueTrangThaiDonHang !== option.value,
                }" @click="handleTrangThaiChange(option.value)">
                    {{ option.label }}
                </button>
            </a-badge>
        </div>
        <table class="table table-hover">
            <thead>
                <tr>
                    <th scope="col">#</th>
                    <th scope="col">Mã hoá đơn</th>
                    <th scope="col">Ngày tạo</th>
                    <th scope="col">Khách hàng</th>
                    <th scope="col">Tổng tiền(VNĐ)</th>
                    <th scope="col">Phương thức thanh toán</th>
                    <th scope="col">Trạng thái hóa đơn</th>
                    <th scope="col">Hình thức nhận hàng</th>
                    <th scope="col">Loại hóa đơn</th>
                    <th scope="col">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(hoaDon, index) in store.getAllHoaDonArr" :key="hoaDon.id_hoa_don">
                    <td>{{ index + 1 }}</td>
                    <td>{{ hoaDon.ma_hoa_don }}</td>
                    <td>{{ formatDateTime(hoaDon.ngay_tao) }}</td>
                    <td>
                        {{ hoaDon.ho_ten }} <br>
                        {{ hoaDon.sdt_nguoi_nhan }} <br>
                        <!-- {{ hoaDon.dia_chi }} -->
                    </td>
                    <td>{{ formatCurrency(hoaDon.tong_tien_sau_giam) }}</td>
                    <td>{{ hoaDon.hinh_thuc_thanh_toan }}</td>
                    <td>{{ hoaDon.trang_thai }}</td>
                    <td>{{ hoaDon.phuong_thuc_nhan_hang }}</td>
                    <td>
                        <span :class="{
                            'status-online': hoaDon.loai_hoa_don === 'Online',
                            'status-offline': hoaDon.loai_hoa_don === 'Offline'
                        }">
                            {{ hoaDon.loai_hoa_don }}
                        </span>
                    </td>
                    <td>
                        <button class="btn d-flex align-items-center shadow-sm"
                            @click="router.push(`/admin/hoadon/hdct/${hoaDon.ma_hoa_don}`)">
                            <i class="bi bi-eye" style="color: #ff6600;"></i>
                        </button>
                    </td>
                </tr>
            </tbody>
            <tbody>
                <tr v-if="!store.getAllHoaDonArr || store.getAllHoaDonArr.length === 0">
                    <td colspan="10" class="text-center" style="color: #ff6600;">Không tìm thấy hóa đơn bạn muốn tìm!
                    </td>
                </tr>
                <tr v-else v-for="(hoaDon, index) in store.getAllHoaDonArr" :key="hoaDon.id_hoa_don">
                    <!-- Nội dung hiện tại -->
                </tr>
            </tbody>
        </table>
    </div>

    <div class="d-flex justify-content-center align-items-center mt-3">
        <button class="btn buttonPT p-0" @click="fetchData(store.currentHoaDon - 1)"
            :disabled="store.currentHoaDon === 0 || store.totalHoaDon === 0">Previous</button>
        <span class="mx-3">Trang {{ store.totalHoaDon === 0 ? 0 : (store.currentHoaDon ?? 0) + 1 }} / {{
            store.totalHoaDon }}</span>
        <button class="btn buttonPT" @click="fetchData(store.currentHoaDon + 1)"
            :disabled="store.totalHoaDon === 0 || store.currentHoaDon >= store.totalHoaDon - 1">Next</button>
    </div>
    <!-- </div> -->
</template>

<script setup>
import { onMounted, onUnmounted, ref, computed, reactive, watch } from 'vue';
import { useGbStore } from '@/stores/gbStore';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { toast } from 'vue3-toastify';

const router = useRouter();
const store = useGbStore();
const searchKeyword = ref('');
let intervalId = null;
const pageSize = ref(5);
const valueTrangThaiDonHang = ref('Tất cả');
const selectedLoaiHoaDon = ref('Tất cả');
const trangThaiDonHangOptions = ref([
    { label: 'Chờ xác nhận', value: 'Chờ xác nhận' },
    // { label: 'Đã cập nhật', value: 'Đã cập nhật' },
    { label: 'Đã xác nhận', value: 'Đã xác nhận' },
    { label: 'Chờ đóng gói', value: 'Chờ đóng gói' },
    { label: 'Đang giao', value: 'Đang giao' },
    { label: 'Hoàn thành', value: 'Hoàn thành' },
    { label: 'Đã hủy', value: 'Đã hủy' },
    { label: 'Trả hàng', value: 'Trả hàng' }
]);
const filterOption = (input, option) => {
    return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};
const formState = reactive({
    'range-time-picker': []
});
const onFinish = values => {
    console.log('Success:', values, formState);
};
const onFinishFailed = errorInfo => {
    console.log('Failed:', errorInfo);
};
const formatCurrency = (value) => {
    if (value === null || value === undefined) return '0';
    return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, "."); // Thêm ' VNĐ' nếu cần
};
const formatDateTime = (date) => {
    if (!date) return 'N/A';
    const d = new Date(date);
    return d.toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
};
const handleLoaiHoaDonChange = async () => {
    console.log(`Loại hóa đơn đã chọn: ${selectedLoaiHoaDon.value}`);
    await fetchData(0);
};
const countByTrangThai = ref({});

const totalHoaDonCount = computed(() => {
    return Object.values(countByTrangThai.value).reduce((sum, count) => sum + count, 0);
});

const calculateCounts = () => {
    // Khởi tạo đối tượng đếm số lượng theo trạng thái
    const counts = {};

    // Duyệt qua danh sách hóa đơn và đếm số lượng theo trạng thái
    store.getAllHoaDonArr.forEach((hoaDon) => {
        const trangThai = hoaDon.trang_thai || 'Không xác định';
        counts[trangThai] = (counts[trangThai] || 0) + 1;
    });

    // Cập nhật `countByTrangThai`
    countByTrangThai.value = counts;

    console.log('Số lượng theo trạng thái:', countByTrangThai.value);
};
// Hàm xử lý tìm kiếm
const handleSearch = () => {
    fetchData(0);
};

// Theo dõi thay đổi của `searchKeyword` để tự động tìm kiếm
watch(searchKeyword, (newKeyword) => {
    if (!newKeyword || newKeyword.trim() === '') {
        // Nếu từ khóa rỗng, lấy lại tất cả hóa đơn
        fetchData(0);
    }
});
watch(() => formState['range-time-picker'], async (newRange) => {
    const [tuNgay, denNgay] = newRange || [];
    if (tuNgay && denNgay) {
        console.log('Tự động tìm kiếm với khoảng thời gian:', tuNgay, denNgay);
        await fetchData(0);
    } else {
        console.log('Xóa khoảng thời gian, lấy lại tất cả hóa đơn');
        await fetchData(0);
    }
});

const fetchData = async (page = 0) => {
    store.currentHoaDon = page;
    const [tuNgay, denNgay] = formState['range-time-picker'] || [];
    const loaiHoaDon = selectedLoaiHoaDon.value === 'Tất cả' ? '' : selectedLoaiHoaDon.value;
    const trangThai = valueTrangThaiDonHang.value === 'Tất cả' ? '' : valueTrangThaiDonHang.value;
    await store.locHoaDon(
        searchKeyword.value,
        tuNgay || '',
        denNgay || '',
        trangThai,
        loaiHoaDon,
        page,
        pageSize.value
    );

    // Cập nhật số lượng hóa đơn theo trạng thái
    // calculateCounts();
};

const handleTrangThaiChange = async (value) => {
    console.log(`Trạng thái đã chọn: ${value}, Số lượng: ${countByTrangThai.value[value] || 0}`);
    valueTrangThaiDonHang.value = value;

    // Reset về trang đầu tiên khi thay đổi trạng thái
    await fetchData(0);
};
const isFilteringByDate = ref(false); // Mặc định không lọc theo ngày
const filterData = async () => {
    const [tuNgay, denNgay] = formState['range-time-picker'] || [];
    if (!tuNgay || !denNgay) {
        toast.warning('Vui lòng chọn đầy đủ khoảng thời gian!');
        return;
    }
    isFilteringByDate.value = true; // Bật trạng thái lọc theo ngày
    await fetchData(0);
    // calculateCounts();
};
// const handleDateChange = (value) => {
//     if (!value || value.length === 0) {
//         // Nếu giá trị bị xóa, hủy trạng thái lọc
//         resetFilters();
//     }
// };
// const resetFilters = async () => {
//     valueTrangThaiDonHang.value = 'Tất cả';
//     formState['range-time-picker'] = [];
//     isFilteringByDate.value = false; // Tắt trạng thái lọc theo ngày
//     // Thêm reset các bộ lọc khác
//     searchKeyword.value = '';
//     selectedLoaiHoaDon.value = 'Tất cả';
//     await fetchData(0); // Lấy lại tất cả hóa đơn
// };

onMounted(async () => {
    await fetchData(0); // Mặc định lấy tất cả hóa đơn
    // Tự động cập nhật danh sách hóa đơn sau mỗi 5 giây
    intervalId = setInterval(async () => {
        await fetchData(store.currentHoaDon);
    }, 5000);
});
onUnmounted(() => {
    // Dọn dẹp interval khi component bị hủy
    if (intervalId) {
        clearInterval(intervalId);
    }
});
</script>

<style scoped>
.table {
    --bs-table-hover-bg: rgb(183 183 183 / 8%);
}

.buttonPT {
    background-color: transparent;
    text-align: center;
    color: #ff6600;
    border: 2px solid #ff6600;
    cursor: pointer;
    transition: all 0.3s ease-in-out;
    width: 70px;
    height: 40px;
    font-size: 14px;
    font-weight: bold;
}

.buttonPT:hover {
    background-color: #ff6600;
    color: white;
}

/* Dải trạng thái */
.status-strip {
    display: flex;
    flex-wrap: wrap;
    gap: 0;
    /* Không có khoảng cách giữa các nút */
    justify-content: center;
    margin-top: 16px;
    border-bottom: 2px solid #dcdcdc;
    /* Đường viền dưới */
}

/* Nút trạng thái */
.status-strip .btn {
    flex: 1;
    min-width: 140px;
    /* Đảm bảo nút không quá nhỏ */
    height: 40px;
    font-size: 14px;
    font-weight: bold;
    text-transform: uppercase;
    /* Chữ in hoa */
    border: none;
    /* Loại bỏ viền mặc định */
    border-top-left-radius: 8px;
    /* Bo góc trên trái */
    border-top-right-radius: 8px;
    /* Bo góc trên phải */
    border-bottom: 2px solid transparent;
    /* Đường viền dưới mặc định */
    transition: all 0.3s ease-in-out;
    /* Hiệu ứng hover */
    cursor: pointer;
    background-color: #e9ecef;
    /* Màu nền xám nhạt */
    color: #495057;
    /* Màu chữ đậm */
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;
    /* Đảm bảo chữ nằm giữa nút */
}

/* Nút trạng thái đang được chọn */
.status-strip .btn-primary {
    background-color: #ffffff;
    /* Nền trắng */
    color: #ff6600;
    /* Màu cam */
    border-bottom: 2px solid #ff6600;
    /* Đường viền dưới cam */
    font-weight: bold;
}

/* Nút trạng thái chưa được chọn */
.status-strip .btn-outline-primary {
    background-color: #f8f9fa;
    /* Nền xám nhạt */
    color: #6c757d;
    /* Màu chữ xám */
    border-bottom: 2px solid #dcdcdc;
    /* Đường viền dưới nhạt */
}

/* Hover cho nút chưa được chọn */
.status-strip .btn-outline-primary:hover {
    background-color: #ffffff;
    /* Nền trắng khi hover */
    color: #ff6600;
    /* Màu cam khi hover */
    border-bottom: 2px solid #ff6600;
    /* Đường viền dưới cam khi hover */
}

/* Hover cho nút đang được chọn */
.status-strip .btn-primary:hover {
    background-color: #ffffff;
    /* Giữ nguyên nền trắng */
    color: #ff6600;
    /* Màu cam đậm hơn */
}

/* Badge hiển thị số lượng */
.ant-badge {
    display: flex;
    align-items: center;
    justify-content: center;
}

.ant-badge-count {
    font-size: 12px;
    min-width: 24px;
    height: 24px;
    line-height: 24px;
    border-radius: 50%;
    background-color: #007bff;
    /* Màu xanh dương */
    color: white;
    font-weight: bold;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    /* Đổ bóng cho badge */
}

.d-flex {
    display: flex;
    align-items: center;
}

.search-container {
    width: 25%;
    /* Đặt chiều rộng cho ô tìm kiếm */
}

.form-control {
    height: 32px;
    /* Đảm bảo chiều cao bằng với nút lọc */
    font-size: 14px;
    color: #ff6600;
}

/* Trạng thái Online */
.status-online {
    display: inline-block;
    padding: 4px 8px;
    border: 2px solid #007bff;
    /* Màu xanh dương */
    border-radius: 12px;
    color: #007bff;
    font-weight: bold;
    text-align: center;
    background-color: rgba(0, 123, 255, 0.1);
    /* Nền xanh nhạt */
}

/* Trạng thái Offline */
.status-offline {
    display: inline-block;
    padding: 4px 8px;
    border: 2px solid #28a745;
    /* Màu xanh lá */
    border-radius: 12px;
    color: #28a745;
    font-weight: bold;
    text-align: center;
    background-color: rgba(40, 167, 69, 0.1);
    /* Nền xanh lá nhạt */
}

.form-check-input {
    appearance: none;
    width: 16px;
    height: 16px;
    border: 1px solid #ff6600;
    border-radius: 50%;
    display: inline-block;
    position: relative;
    background-color: white;
}

.form-check-input:checked::before {
    content: "";
    width: 8px;
    height: 8px;
    background-color: #ff6600;
    border-radius: 50%;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
}

.form-check-input:focus {
    box-shadow: none;
    outline: none;
}

.form-check-input:focus-visible {
    box-shadow: none;
    outline: none;
}
</style>