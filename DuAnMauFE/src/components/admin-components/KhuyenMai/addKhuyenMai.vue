<template>
  <div class="container-fluid">
    <div class="d-flex justify-content-between align-items-center mb-3 co p-3">
      <h3 class="m-0 fw-bold" style="color: #ff6600;">Thêm khuyến mãi</h3>
    </div>

    <div class="container-fluid mt-4">
      <form @submit.prevent="submitForm" id="addKhuyenMaiForm">
        <div class="row">
          <!-- Form khuyến mãi -->
          <div class="col-lg-4 bg-light p-4 rounded">
            <div class="mb-3">
              <label for="maKhuyenMai" class="form-label">Mã khuyến mãi</label>
              <input
                type="text"
                class="form-control"
                id="maKhuyenMai"
                v-model="khuyenMai.maKhuyenMai"
                required
                :class="{ 'is-invalid': errors.maKhuyenMai }"
                @input="handleMaKhuyenMaiInput"
              />
              <div class="text-danger" v-if="errors.maKhuyenMai">{{ errors.maKhuyenMai }}</div>
            </div>
            <div class="mb-3">
              <label for="tenKhuyenMai" class="form-label">Tên khuyến mãi</label>
              <input
                type="text"
                class="form-control"
                id="tenKhuyenMai"
                v-model="khuyenMai.tenKhuyenMai"
                required
                :class="{ 'is-invalid': errors.tenKhuyenMai }"
                @input="validateTenKhuyenMai"
              />
              <div class="text-danger" v-if="errors.tenKhuyenMai">{{ errors.tenKhuyenMai }}</div>
            </div>
            <div class="mb-3">
              <label for="kieuGiamGia" class="form-label">Kiểu giảm giá</label>
              <select
                class="form-select"
                id="kieuGiamGia"
                v-model="khuyenMai.kieuGiamGia"
                required
                :class="{ 'is-invalid': errors.kieuGiamGia }"
                @change="validateKieuGiamGia"
              >
                <option value="" disabled>Chọn kiểu</option>
                <option value="Phần trăm">Phần trăm</option>
                <option value="Tiền mặt">Tiền mặt</option>
              </select>
              <div class="text-danger" v-if="errors.kieuGiamGia">{{ errors.kieuGiamGia }}</div>
            </div>
            <div class="mb-3">
              <label for="giaTriGiam" class="form-label">Giá trị giảm</label>
              <div class="input-group">
                <input
                  type="text"
                  class="form-control"
                  id="giaTriGiam"
                  v-model="displayGiaTriGiam"
                  required
                  :class="{ 'is-invalid': errors.giaTriGiam }"
                  @input="handleGiaTriGiamInput"
                />
                <span class="input-group-text">{{ khuyenMai.kieuGiamGia === 'Phần trăm' ? '%' : '₫' }}</span>
              </div>
              <div class="text-danger" v-if="errors.giaTriGiam">{{ errors.giaTriGiam }}</div>
            </div>
            <div class="mb-3">
              <label for="giaTriToiDa" class="form-label">Giá trị tối đa</label>
              <div class="input-group">
                <input
                  type="text"
                  class="form-control"
                  id="giaTriToiDa"
                  v-model="displayGiaTriToiDa"
                  :disabled="khuyenMai.kieuGiamGia === 'Tiền mặt'"
                  :class="{ 'is-invalid': errors.giaTriToiDa }"
                  @input="handleGiaTriToiDaInput"
                />
                <span class="input-group-text">₫</span>
              </div>
              <div class="text-danger" v-if="errors.giaTriToiDa">{{ errors.giaTriToiDa }}</div>
            </div>
            <div class="mb-3">
              <label for="ngayBatDau" class="form-label">Ngày bắt đầu</label>
              <input
                type="datetime-local"
                class="form-control"
                id="ngayBatDau"
                v-model="khuyenMai.ngayBatDau"
                required
                :class="{ 'is-invalid': errors.ngayBatDau }"
                @input="validateDates"
              />
              <div class="text-danger" v-if="errors.ngayBatDau">{{ errors.ngayBatDau }}</div>
            </div>
            <div class="mb-3">
              <label for="ngayHetHan" class="form-label">Ngày kết thúc</label>
              <input
                type="datetime-local"
                class="form-control"
                id="ngayHetHan"
                v-model="khuyenMai.ngayHetHan"
                required
                :class="{ 'is-invalid': errors.ngayHetHan }"
                @input="validateDates"
              />
              <div class="text-danger" v-if="errors.ngayHetHan">{{ errors.ngayHetHan }}</div>
            </div>
            <div class="mb-3">
              <label for="moTa" class="form-label">Mô tả</label>
              <textarea
                class="form-control"
                id="moTa"
                v-model="khuyenMai.moTa"
                rows="3"
              ></textarea>
            </div>
            <div class="d-flex justify-content-center gap-3 mt-4">
              <button
                type="submit"
                class="btn btn-primary"
                :disabled="hasErrors || selectedChiTietSanPhamIds.length === 0"
              >
                Lưu
              </button>
              <button
                type="button"
                class="btn btn-secondary"
                @click="router.push('/admin/quanlykhuyenmai')"
              >
                Quay lại
              </button>
            </div>
          </div>

          <!-- Bảng sản phẩm và chi tiết sản phẩm -->
          <div class="col-lg-8 d-flex flex-column gap-3">
            <!-- SanPham Table -->
            <div class="bg-light p-4 rounded">
              <h5 style="color: #ff6600;">Sản phẩm</h5>
              <div class="d-flex gap-3 align-items-center mt-2">
                <input
                  type="text"
                  class="form-control w-75"
                  id="keywordSanPham"
                  v-model="keywordSanPham"
                  placeholder="Nhập mã hoặc tên sản phẩm"
                  @input="debounceFetchSanPham"
                />
              </div>
              <div class="table-responsive p-2 mt-3 scrollable-table">
                <table class="table table-bordered">
                  <thead class="co">
                    <tr>
                      <th><input class="form-check-input" type="checkbox" @change="toggleSelectAllSanPham" /></th>
                      <th>STT</th>
                      <th>Mã sản phẩm</th>
                      <th>Tên sản phẩm</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-if="sanPhamList.length === 0">
                      <td colspan="4" class="text-center">Không có sản phẩm nào</td>
                    </tr>
                    <tr v-for="(sanPham, index) in sanPhamList" :key="sanPham.id_san_pham">
                      <td>
                        <input
                          class="form-check-input sanPhamCheckbox"
                          type="checkbox"
                          :value="sanPham.id_san_pham"
                          v-model="selectedSanPhamIds"
                          @change="refreshChiTietSanPham"
                        />
                      </td>
                      <td>{{ index + 1 }}</td>
                      <td>{{ sanPham.ma_san_pham }}</td>
                      <td>{{ sanPham.ten_san_pham }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- ChiTietSanPham Table -->
            <div class="bg-light p-4 rounded">
              <h5 style="color: #ff6600;">Chi tiết sản phẩm</h5>
              <div class="table-responsive p-2 scrollable-table">
                <table class="table table-bordered">
                  <thead class="co">
                    <tr>
                      <th><input class="form-check-input" type="checkbox" @change="toggleSelectAllChiTietSanPham" /></th>
                      <th>STT</th>
                      <th>Mã sản phẩm</th>
                      <th>Tên sản phẩm</th>
                      <th>Giá bán</th>
                      <th>Số lượng</th>
                      <th>Kích thước</th>
                      <th>Màu sắc</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-if="chiTietSanPhamList.length === 0">
                      <td colspan="8" class="text-center">Không có chi tiết sản phẩm nào được chọn</td>
                    </tr>
                    <tr v-for="(item, index) in chiTietSanPhamList" :key="item.id_chi_tiet_san_pham">
                      <td>
                        <input
                          class="form-check-input chiTietSanPhamCheckbox"
                          type="checkbox"
                          :value="item.id_chi_tiet_san_pham"
                          v-model="selectedChiTietSanPhamIds"
                        />
                      </td>
                      <td>{{ index + 1 }}</td>
                      <td>{{ item.sanPham.ma_san_pham }}</td>
                      <td>{{ item.sanPham.ten_san_pham }}</td>
                      <td>{{ formatNumber(item.gia_ban) }}</td>
                      <td>{{ item.so_luong }}</td>
                      <td>{{ item.kichThuoc?.gia_tri || 'N/A' }}</td>
                      <td>{{ item.mauSac?.ten_mau_sac || 'N/A' }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useGbStore } from '@/stores/gbStore';
import { toast } from 'vue3-toastify';
import { khuyenMaiService } from '@/services/khuyenMaiService';

const router = useRouter();
const store = useGbStore();

const khuyenMai = ref({
  maKhuyenMai: '',
  tenKhuyenMai: '',
  giaTriGiam: null,
  kieuGiamGia: '',
  giaTriToiDa: null,
  ngayBatDau: null,
  ngayHetHan: null,
  moTa: '',
});

const errors = ref({
  maKhuyenMai: '',
  tenKhuyenMai: '',
  giaTriGiam: '',
  kieuGiamGia: '',
  giaTriToiDa: '',
  ngayBatDau: '',
  ngayHetHan: '',
});

const keywordSanPham = ref('');
const sanPhamList = ref([]);
const selectedSanPhamIds = ref([]);
const chiTietSanPhamList = ref([]);
const selectedChiTietSanPhamIds = ref([]);

// Display values for formatted inputs
const displayGiaTriGiam = ref('');
const displayGiaTriToiDa = ref('');

// Format number with VND currency and dot separators
const formatNumber = (number, includeCurrency = true) => {
  if (!number && number !== 0) return '0';
  const formatted = new Intl.NumberFormat('vi-VN', {
    maximumFractionDigits: 2,
    minimumFractionDigits: 0
  }).format(number);
  return includeCurrency ? `${formatted} ₫` : formatted;
};

// Parse input to remove formatting
const parseInput = (value) => {
  if (!value) return null;
  return parseFloat(value.replace(/\./g, '').replace(',', '.'));
};

// Format input to add dots
const formatInput = (value) => {
  if (!value && value !== 0) return '';
  const num = parseFloat(value);
  if (isNaN(num)) return '';
  return new Intl.NumberFormat('vi-VN', {
    maximumFractionDigits: 0,
    minimumFractionDigits: 0
  }).format(num);
};

// Handle input for giaTriGiam
const handleGiaTriGiamInput = (event) => {
  const rawValue = event.target.value.replace(/\./g, '');
  const parsedValue = parseInput(rawValue);
  khuyenMai.value.giaTriGiam = parsedValue;
  displayGiaTriGiam.value = formatInput(parsedValue);
  validateGiaTriGiam();
};

// Handle input for giaTriToiDa
const handleGiaTriToiDaInput = (event) => {
  const rawValue = event.target.value.replace(/\./g, '');
  const parsedValue = parseInput(rawValue);
  khuyenMai.value.giaTriToiDa = parsedValue;
  displayGiaTriToiDa.value = formatInput(parsedValue);
  validateGiaTriToiDa();
};

// Validation functions
const validateMaKhuyenMai = () => {
  if (!khuyenMai.value.maKhuyenMai || khuyenMai.value.maKhuyenMai.trim() === '') {
    errors.value.maKhuyenMai = 'Mã khuyến mãi không được để trống!';
  } else {
    errors.value.maKhuyenMai = '';
  }
};

const validateTenKhuyenMai = () => {
  if (!khuyenMai.value.tenKhuyenMai || khuyenMai.value.tenKhuyenMai.trim() === '') {
    errors.value.tenKhuyenMai = 'Tên khuyến mãi không được để trống!';
  } else {
    errors.value.tenKhuyenMai = '';
  }
};

const validateKieuGiamGia = () => {
  if (!khuyenMai.value.kieuGiamGia || khuyenMai.value.kieuGiamGia.trim() === '') {
    errors.value.kieuGiamGia = 'Kiểu giảm giá không được để trống!';
  } else {
    errors.value.kieuGiamGia = '';
    validateGiaTriGiam();
  }
};

const validateGiaTriGiam = () => {
  const giaTri = khuyenMai.value.giaTriGiam;
  if (giaTri === null || isNaN(giaTri) || giaTri <= 0) {
    errors.value.giaTriGiam = 'Giá trị giảm phải là số lớn hơn 0!';
  } else if (giaTri > 5000000) {
    errors.value.giaTriGiam = `Giá trị giảm không được lớn hơn ${formatNumber(5000000)}!`;
  } else if (khuyenMai.value.kieuGiamGia === 'Phần trăm' && giaTri > 100) {
    errors.value.giaTriGiam = 'Giá trị giảm không được vượt quá 100 khi chọn Phần trăm!';
  } else {
    errors.value.giaTriGiam = '';
  }

  if (khuyenMai.value.kieuGiamGia === 'Tiền mặt') {
    khuyenMai.value.giaTriToiDa = giaTri;
    displayGiaTriToiDa.value = formatInput(giaTri);
    validateGiaTriToiDa();
  }
};

const validateGiaTriToiDa = () => {
  const giaTriToiDa = khuyenMai.value.giaTriToiDa;
  if (khuyenMai.value.kieuGiamGia === 'Tiền mặt') {
    khuyenMai.value.giaTriToiDa = khuyenMai.value.giaTriGiam;
    displayGiaTriToiDa.value = formatInput(khuyenMai.value.giaTriGiam);
    errors.value.giaTriToiDa = '';
  } else if (giaTriToiDa === null || isNaN(giaTriToiDa) || giaTriToiDa <= 0) {
    errors.value.giaTriToiDa = 'Giá trị tối đa phải là số lớn hơn 0!';
  } else if (giaTriToiDa > 5000000) {
    errors.value.giaTriToiDa = `Giá trị tối đa không được lớn hơn ${formatNumber(5000000)}!`;
  } else {
    errors.value.giaTriToiDa = '';
  }
};

const validateDates = () => {
  if (!khuyenMai.value.ngayBatDau) {
    errors.value.ngayBatDau = 'Ngày bắt đầu không được để trống!';
  } else {
    errors.value.ngayBatDau = '';
  }

  if (!khuyenMai.value.ngayHetHan) {
    errors.value.ngayHetHan = 'Ngày kết thúc không được để trống!';
  } else if (khuyenMai.value.ngayBatDau && new Date(khuyenMai.value.ngayHetHan) <= new Date(khuyenMai.value.ngayBatDau)) {
    errors.value.ngayHetHan = 'Ngày kết thúc phải sau ngày bắt đầu!';
  } else {
    errors.value.ngayHetHan = '';
  }
};

// Handle maKhuyenMai input to remove spaces
const handleMaKhuyenMaiInput = () => {
  const originalValue = khuyenMai.value.maKhuyenMai;
  khuyenMai.value.maKhuyenMai = khuyenMai.value.maKhuyenMai.replace(/\s/g, '');
  if (originalValue !== khuyenMai.value.maKhuyenMai) {
    toast.error('Mã khuyến mãi không được chứa khoảng cách!', { autoClose: 1000 });
  }
  validateMaKhuyenMai();
};

// Computed to check for errors
const hasErrors = computed(() => {
  return Object.values(errors.value).some(error => error !== '');
});

// Watch kieuGiamGia and giaTriGiam
watch(() => khuyenMai.value.kieuGiamGia, () => {
  if (khuyenMai.value.kieuGiamGia === 'Tiền mặt') {
    khuyenMai.value.giaTriToiDa = khuyenMai.value.giaTriGiam;
    displayGiaTriToiDa.value = formatInput(khuyenMai.value.giaTriGiam);
  }
  validateKieuGiamGia();
});

watch(() => khuyenMai.value.giaTriGiam, (newValue) => {
  if (khuyenMai.value.kieuGiamGia === 'Tiền mặt') {
    khuyenMai.value.giaTriToiDa = newValue;
    displayGiaTriToiDa.value = formatInput(newValue);
  }
  displayGiaTriGiam.value = formatInput(newValue);
  validateGiaTriGiam();
});

watch(() => khuyenMai.value.giaTriToiDa, (newValue) => {
  displayGiaTriToiDa.value = formatInput(newValue);
  validateGiaTriToiDa();
});

// Debounce function
const debounce = (func, wait) => {
  let timeout;
  return (...args) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
};

// Fetch SanPham with search (no pagination)
const fetchSanPham = async () => {
  try {
    const response = await khuyenMaiService.searchSanPhamKM(keywordSanPham.value);
    if (!response.error && Array.isArray(response.content)) {
      sanPhamList.value = response.content.map(sp => ({
        id_san_pham: sp.idSanPham || sp.id_san_pham,
        ma_san_pham: sp.maSanPham || sp.ma_san_pham,
        ten_san_pham: sp.tenSanPham || sp.ten_san_pham
      }));
    } else {
      sanPhamList.value = [];
      toast.error('Không tìm thấy sản phẩm', { autoClose: 1000 });
    }
  } catch (error) {
    console.error('Error fetching SanPham:', error);
    sanPhamList.value = [];
    toast.error('Không thể tải danh sách sản phẩm', { autoClose: 3000 });
  }
};

// Debounced version of fetchSanPham
const debounceFetchSanPham = debounce(() => fetchSanPham(), 300);

// Refresh ChiTietSanPham, only active product variants (no pagination)
const refreshChiTietSanPham = async () => {
  chiTietSanPhamList.value = [];
  if (selectedSanPhamIds.value.length === 0) return;

  for (const sanPhamId of selectedSanPhamIds.value) {
    try {
      const response = await khuyenMaiService.getChiTietSanPhamBySanPham(sanPhamId);
      const filteredChiTiet = (response || []).filter(ctsp => ctsp.trang_thai === true);
      chiTietSanPhamList.value = [...chiTietSanPhamList.value, ...filteredChiTiet];
    } catch (error) {
      console.error(`Error fetching ChiTietSanPham for SanPham ID ${sanPhamId}:`, error);
      toast.error(`Không thể tải chi tiết sản phẩm cho ID ${sanPhamId}`, { autoClose: 3000 });
    }
  }
};

// Toggle select all functions
const toggleSelectAllSanPham = (event) => {
  if (event.target.checked) {
    selectedSanPhamIds.value = sanPhamList.value.map(sp => sp.id_san_pham);
  } else {
    selectedSanPhamIds.value = [];
  }
  refreshChiTietSanPham();
};

const toggleSelectAllChiTietSanPham = (event) => {
  if (event.target.checked) {
    selectedChiTietSanPhamIds.value = chiTietSanPhamList.value.map(ctsp => ctsp.id_chi_tiet_san_pham);
  } else {
    selectedChiTietSanPhamIds.value = [];
  }
};

// Submit form
const submitForm = async () => {
  validateMaKhuyenMai();
  validateTenKhuyenMai();
  validateKieuGiamGia();
  validateGiaTriGiam();
  validateGiaTriToiDa();
  validateDates();

  if (hasErrors.value) {
    const errorMessages = Object.values(errors.value).filter(error => error !== '').join(' ');
    console.log('Frontend Validation Errors:', errors.value);
    toast.error(errorMessages || 'Vui lòng sửa các lỗi trước khi lưu!', { autoClose: 1000 });
    return;
  }

  if (selectedChiTietSanPhamIds.value.length === 0) {
    console.log('No ChiTietSanPham selected');
    toast.error('Vui lòng chọn ít nhất một chi tiết sản phẩm!', { autoClose: 1000 });
    return;
  }

  const khuyenMaiData = {
    maKhuyenMai: khuyenMai.value.maKhuyenMai,
    tenKhuyenMai: khuyenMai.value.tenKhuyenMai,
    giaTriGiam: parseFloat(khuyenMai.value.giaTriGiam),
    kieuGiamGia: khuyenMai.value.kieuGiamGia,
    giaTriToiDa: parseFloat(khuyenMai.value.giaTriToiDa),
    ngayBatDau: khuyenMai.value.ngayBatDau ? new Date(khuyenMai.value.ngayBatDau).toISOString() : null,
    ngayHetHan: khuyenMai.value.ngayHetHan ? new Date(khuyenMai.value.ngayHetHan).toISOString() : null,
    moTa: khuyenMai.value.moTa || '',
  };

  console.log('Submitting Data:', khuyenMaiData, 'Selected ChiTietSanPhamIds:', selectedChiTietSanPhamIds.value);

  try {
    const response = await khuyenMaiService.addKhuyenMai(khuyenMaiData, selectedChiTietSanPhamIds.value);
    console.log('Add KhuyenMai Response:', response);

    if (response === 'Thêm khuyến mãi thành công!') {
      toast.success(response, {
        autoClose: 3000,
        onClose: () => router.push('/admin/quanlykhuyenmai'),
      });
    } else {
      if (typeof response === 'string') {
        toast.error(response, { autoClose: 1000 });
      } else if (response && response.message) {
        toast.error(response.message, { autoClose: 1000 });
      } else {
        toast.error('Thêm khuyến mãi thất bại!', { autoClose: 1000 });
      }
    }
  } catch (error) {
    console.error('Error adding KhuyenMai:', error);
    if (error.response && error.response.data) {
      const backendError = error.response.data.message || error.response.data.error || 'Có lỗi xảy ra khi thêm khuyến mãi';
      toast.error(backendError, { autoClose: 1000 });
    } else {
      toast.error('Không thể kết nối đến server!', { autoClose: 1000 });
    }
  }
};

// Initial fetch on component mount
onMounted(() => {
  fetchSanPham();
});
</script>

<style scoped>
.co {
  background-color: #f8f9fa;
}

.bg-light {
  background-color: #f8f9fa !important;
}

.btn-primary {
  background-color: #ff6600;
  border-color: #ff6600;
  font-weight: bold;
}

.btn-primary:hover {
  background-color: #e65c00;
  border-color: #e65c00;
}

.btn-secondary {
  background-color: #6c757d;
  border-color: #6c757d;
}

.is-invalid {
  border-color: #dc3545;
}

.text-danger {
  font-size: 0.875em;
  margin-top: 0.25rem;
}

.table {
  --bs-table-hover-bg: rgb(183 183 183 / 8%);
}

.scrollable-table {
  max-height: 300px;
  overflow-y: auto;
}

.input-group-text {
  background-color: #f8f9fa;
  border-color: #ced4da;
}
</style>