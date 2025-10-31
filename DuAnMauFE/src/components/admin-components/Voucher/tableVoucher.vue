<template>
  <div class="container-fluid">
    <!-- Lọc trạng thái -->
    <div class="d-flex align-items-center justify-content-between">
      <div class="me-3">
        <span class="fw-bold me-2" style="font-size: 16px;">Trạng thái:</span>
        <div class="form-check form-check-inline">
          <input class="form-check-input" type="radio" name="trangThai" id="sapDienRa" value="Sắp diễn ra"
            v-model="selectedTrangThai" @change="fetchData(0)">
          <label class="form-check-label" for="sapDienRa">Sắp diễn ra</label>
        </div>
        <div class="form-check form-check-inline">
          <input class="form-check-input" type="radio" name="trangThai" id="dangDienRa" value="Đang diễn ra"
            v-model="selectedTrangThai" @change="fetchData(0)">
          <label class="form-check-label" for="dangDienRa">Đang diễn ra</label>
        </div>
        <div class="form-check form-check-inline">
          <input class="form-check-input" type="radio" name="trangThai" id="daKetThuc" value="Đã kết thúc"
            v-model="selectedTrangThai" @change="fetchData(0)">
          <label class="form-check-label" for="daKetThuc">Đã kết thúc</label>
        </div>
        <div class="form-check form-check-inline">
          <input class="form-check-input" type="radio" name="trangThai" id="tatCa" value=""
            v-model="selectedTrangThai" @change="fetchData(0)">
          <label class="form-check-label" for="tatCa">Tất cả</label>
        </div>
      </div>
      <div>
        <button class="btn buttonADD" @click="router.push('/admin/quanlyvoucher/add')">+ Thêm Voucher</button>
      </div>
    </div>

    <!-- Tìm kiếm và lọc theo giá, ngày -->
    <div class="row mt-3 g-2 align-items-center">
      <div class="col-md-8 d-flex align-items-center">
        <label class="me-2" style="white-space: nowrap;">Tìm kiếm:</label>
        <input type="text" class="form-control" v-model="store.voucherSearchs" placeholder="Nhập mã hoặc tên voucher">
      </div>
      <div class="col-md-3 d-flex align-items-center mt-2">
        <label class="me-2" style="white-space: nowrap;">Kiểu giảm giá:</label>
        <select class="form-select" v-model="selectedKieuGiamGia" @change="fetchData(0)">
          <option value="">Tất cả</option>
          <option value="Phần trăm">Phần trăm</option>
          <option value="Tiền mặt">Tiền mặt</option>
        </select>
      </div>
      <div class="col-md-5 d-flex align-items-center flex-nowrap">
        <label class="me-2" style="white-space: nowrap;">Giá trị giảm tối đa:</label>
        <div class="d-flex flex-nowrap w-100">
          <input type="number" class="form-control me-2" v-model="minPrice" placeholder="Min" min="0" step="1" @keyup.enter="fetchData(0)">
          <span class="align-self-center">-</span>
          <input type="number" class="form-control ms-2" v-model="maxPrice" placeholder="Max" min="0" step="1" @keyup.enter="fetchData(0)">
        </div>
      </div>
      <div class="col-md-6 d-flex align-items-center mt-2">
        <label class="me-2" style="white-space: nowrap;">Ngày:</label>
        <input type="datetime-local" class="form-control w-50" v-model="startDate">
        <span class="mx-2">-</span>
        <input type="datetime-local" class="form-control w-50" v-model="endDate">
      </div>
      <div class="col-md-1 text-end mt-2">
        <button class="btn btn-outline-danger" @click="refreshData">
          <i class="bi bi-arrow-clockwise"></i>
        </button>
      </div>
    </div>

    <!-- Tiêu đề và chọn số lượng hiển thị -->
    <div class="card p-3 border-0 mt-4">
      <div class="d-flex justify-content-between align-items-center border-bottom pb-2">
        <h5 class="fw-bold mb-0" style="color: #f33b47;">📋 Danh sách voucher</h5>
        <div class="d-flex align-items-center">
          <label for="limitSelect" class="me-2 fw-medium text-muted mb-0">Hiển thị:</label>
          <select id="limitSelect" class="form-select form-select-sm w-auto" v-model="pageSize">
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Bảng voucher -->
    <div class="table-responsive mt-4">
      <table class="table table-hover">
        <thead>
          <tr>
            <th scope="col">#</th>
            <th scope="col">Mã voucher</th>
            <th scope="col">Tên voucher</th>
            <th scope="col">Giá trị giảm</th>
            <th scope="col">Giá trị tối thiểu</th>
            <th scope="col">Số lượng</th>
            <th scope="col">Kiểu giảm giá</th>
            <th scope="col">Giá trị tối đa</th>
            <th scope="col">Ngày bắt đầu</th>
            <th scope="col">Ngày kết thúc</th>
            <th scope="col">Trạng thái</th>
            <th scope="col">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="dataVoucher.length === 0">
            <td colspan="13" class="text-center">
              <a-empty :image="simpleImage" />
            </td>
          </tr>
          <tr v-for="(voucher, index) in dataVoucher" :key="voucher.id">
            <td>{{ index + 1 + (store.voucherCurrentPage * pageSize) }}</td>
            <td>{{ voucher.maVoucher }}</td>
            <td>{{ voucher.tenVoucher }}</td>
            <td>{{ formatGiaTriGiam(voucher) }}</td>
            <td>{{ formatGiaTriToiThieu(voucher) }}</td>
            <td>{{ voucher.soLuong }}</td>
            <td>{{ voucher.kieuGiamGia }}</td>
            <td>{{ formatGiaTriToiDa(voucher) }}</td>
            <td>{{ formatDate(voucher.ngayBatDau) }}</td>
            <td>{{ formatDate(voucher.ngayHetHan) }}</td>
            <td>{{ voucher.trangThai }}</td>
            <td>
              <button class="btn btn-outline-danger btn-sm" 
                      v-if="voucher.trangThai !== 'Đã kết thúc'"
                      @click="router.push(`/admin/quanlyvoucher/update/${voucher.id}`)">
                <i class="fas fa-edit me-1"></i>Sửa
              </button>
              <a-switch v-if="voucher.trangThai === 'Đang diễn ra'"
                        :checked="true" 
                        :style="{ backgroundColor: '#f33b47' }" 
                        @click="offVoucher(voucher.id)" />
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Phân trang -->
    <div class="d-flex justify-content-center align-items-center mt-4">
      <button class="btn buttonPT p-0" @click="fetchData(store.voucherCurrentPage - 1)" 
              :disabled="store.voucherCurrentPage === 0">Previous</button>
      <span class="mx-3">Trang {{ store.voucherCurrentPage + 1 }} / {{ store.voucherTotalPages }}</span>
      <button class="btn buttonPT" @click="fetchData(store.voucherCurrentPage + 1)" 
              :disabled="store.voucherCurrentPage >= store.voucherTotalPages - 1">Next</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, onUnmounted } from 'vue';
import { useGbStore } from '@/stores/gbStore';
import { useRouter } from 'vue-router';
import { Empty } from 'ant-design-vue'; // Import Empty từ Ant Design Vue
import { toast } from 'vue3-toastify';

const router = useRouter();
const store = useGbStore();
const pageSize = ref(5);
const selectedTrangThai = ref('');
const selectedKieuGiamGia = ref('');
const minPrice = ref(null);
const maxPrice = ref(null);
const startDate = ref(null);
const endDate = ref(null);
const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE; // Định nghĩa simpleImage

let refreshInterval = null;

const fetchData = async (page = 0) => {
  if (page < 0 || page >= store.voucherTotalPages) return;
  store.voucherCurrentPage = page;

  try {
    if (store.voucherSearchs && store.voucherSearchs.trim() !== '') {
      await store.searchVoucher(store.voucherSearchs, page, pageSize.value);
    } else if (minPrice.value || maxPrice.value) {
      await store.timKiemVoucherByPrice(minPrice.value, maxPrice.value, page, pageSize.value);
    } else if (startDate.value || endDate.value) {
      await store.timKiemVoucherByDate(startDate.value, endDate.value, page, pageSize.value);
    } else if (selectedTrangThai.value) {
      await store.getVoucherLocTrangThai(page, pageSize.value, selectedTrangThai.value);
    } else if (selectedKieuGiamGia.value) {
      await store.getVoucherLocKieuGiamGia(page, pageSize.value, selectedKieuGiamGia.value);
    } else {
      await store.getAllVouchers(page, pageSize.value);
    }
  } catch (error) {
    console.error('Lỗi trong fetchData:', error);
    toast.error('Có lỗi xảy ra khi tải dữ liệu!');
  }
};

const refreshData = async () => {
  store.voucherSearchs = '';
  selectedTrangThai.value = '';
  selectedKieuGiamGia.value = '';
  minPrice.value = null;
  maxPrice.value = null;
  startDate.value = null;
  endDate.value = null;
  await store.getAllVouchers(0, pageSize.value);
};

const offVoucher = async (id) => {
  await store.offVoucher(id);
  await fetchData(store.voucherCurrentPage);
};

const dataVoucher = computed(() => {
  let data = [];
  if (store.voucherSearchs && store.voucherSearchs.trim() !== '') {
    data = store.voucherSearch.length > 0 ? [...store.voucherSearch] : [];
  } else {
    data = [...store.getAllVoucherArr];
  }
  return data.sort((a, b) => b.id - a.id);
});

const formatNumber = (number) => {
  if (!number) return '0';
  return new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 2 }).format(number);
};

const formatGiaTriGiam = (voucher) => {
  const value = formatNumber(voucher.giaTriGiam);
  return voucher.kieuGiamGia === 'Phần trăm' ? `${value}%` : `${value} VNĐ`;
};

const formatGiaTriToiThieu = (voucher) => {
  return `${formatNumber(voucher.giaTriToiThieu)} VNĐ`;
};

const formatGiaTriToiDa = (voucher) => {
  return `${formatNumber(voucher.giaTriToiDa)} VNĐ`;
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

watch(() => store.voucherSearchs, async (newValue) => {
  if (!newValue || newValue.trim() === '') {
    await fetchData(0);
  } else {
    await store.searchVoucher(newValue, 0, pageSize.value);
  }
});



watch([startDate, endDate], async () => {
  await fetchData(0);
});

watch([pageSize, selectedTrangThai, selectedKieuGiamGia], async () => {
  await fetchData(0);
});

onMounted(async () => {
  await store.getAllVouchers(0, pageSize.value);
  refreshInterval = setInterval(() => {
    fetchData(store.voucherCurrentPage);
  }, 5000);
});

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval);
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
  color: #d02c39;
  border: 2px solid #f33b47;
  cursor: pointer;
  transition: all 0.3s ease-in-out;
  width: 70px;
  height: 40px;
  font-size: 14px;
  font-weight: bold;
}

.buttonPT:hover {
  background-color: #f33b47;
  color: white;
}

.buttonADD {
  background-color: #d02c39;
  color: white;
  font-weight: bold;
}

.buttonADD:hover {
  background-color: #f33b47;
  color: white;
}

.form-check-input {
  appearance: none;
  width: 16px;
  height: 16px;
  border: 1px solid #f33b47;
  border-radius: 50%;
  display: inline-block;
  position: relative;
  background-color: white;
}

.form-check-input:checked::before {
  content: "";
  width: 8px;
  height: 8px;
  background-color: #f33b47;
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