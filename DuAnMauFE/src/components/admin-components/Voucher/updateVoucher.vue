<template>
  <div class="container-fluid">
    <h3 class="fw-bold mb-4" style="color: #f33b47;">Cập Nhật Voucher</h3>
    <form @submit.prevent="submitForm" v-if="voucher">
      <div class="row g-3">
        <input type="hidden" v-model="voucher.id">
        <div class="col-md-6">
          <label for="maVoucher" class="form-label">Mã Voucher</label>
          <input type="text" class="form-control" id="maVoucher" v-model="voucher.maVoucher" disabled
                 :class="{ 'is-invalid': errors.maVoucher }">
          <div class="text-danger" v-if="errors.maVoucher">{{ errors.maVoucher }}</div>
        </div>
        <div class="col-md-6">
          <label for="tenVoucher" class="form-label">Tên Voucher</label>
          <input type="text" class="form-control" id="tenVoucher" v-model="voucher.tenVoucher" required
                 :class="{ 'is-invalid': errors.tenVoucher }" @input="handleTenVoucherInput">
          <div class="text-danger" v-if="errors.tenVoucher">{{ errors.tenVoucher }}</div>
        </div>
        <div class="col-md-6">
          <label for="kieuGiamGia" class="form-label">Kiểu giảm giá</label>
          <select class="form-select" id="kieuGiamGia" v-model="voucher.kieuGiamGia" required
                  :class="{ 'is-invalid': errors.kieuGiamGia }" @change="validateKieuGiamGia">
            <option value="" disabled>Chọn kiểu giảm giá</option>
            <option value="Phần trăm">Phần trăm</option>
            <option value="Tiền mặt">Tiền mặt</option>
          </select>
          <div class="text-danger" v-if="errors.kieuGiamGia">{{ errors.kieuGiamGia }}</div>
        </div>
        <div class="col-md-6">
          <label for="giaTriGiam" class="form-label">Giá trị giảm</label>
          <div class="input-group">
            <input type="text" class="form-control" id="giaTriGiam" v-model="displayGiaTriGiam" required
                   :class="{ 'is-invalid': errors.giaTriGiam }" @input="handleCurrencyInput('giaTriGiam', $event)">
            <span class="input-group-text">{{ voucher.kieuGiamGia === 'Phần trăm' ? '%' : '₫' }}</span>
          </div>
          <div class="text-danger" v-if="errors.giaTriGiam">{{ errors.giaTriGiam }}</div>
        </div>
        <div class="col-md-6">
          <label for="giaTriToiThieu" class="form-label">Giá trị tối thiểu</label>
          <div class="input-group">
            <input type="text" class="form-control" id="giaTriToiThieu" v-model="displayGiaTriToiThieu"
                   :class="{ 'is-invalid': errors.giaTriToiThieu }" @input="handleCurrencyInput('giaTriToiThieu', $event)">
            <span class="input-group-text">₫</span>
          </div>
          <div class="text-danger" v-if="errors.giaTriToiThieu">{{ errors.giaTriToiThieu }}</div>
        </div>
        <div class="col-md-6">
          <label for="soLuong" class="form-label">Số lượng</label>
          <input type="number" class="form-control" id="soLuong" v-model="voucher.soLuong" min="0" step="1" required
                 :class="{ 'is-invalid': errors.soLuong }" @input="validateSoLuong">
          <div class="text-danger" v-if="errors.soLuong">{{ errors.soLuong }}</div>
        </div>
        <div class="col-md-6">
          <label for="giaTriToiDa" class="form-label">Giá trị tối đa</label>
          <div class="input-group">
            <input type="text" class="form-control" id="giaTriToiDa" v-model="displayGiaTriToiDa" required
                   :disabled="voucher.kieuGiamGia === 'Tiền mặt'" :class="{ 'is-invalid': errors.giaTriToiDa }"
                   @input="handleCurrencyInput('giaTriToiDa', $event)">
            <span class="input-group-text">₫</span>
          </div>
          <div class="text-danger" v-if="errors.giaTriToiDa">{{ errors.giaTriToiDa }}</div>
        </div>
        <div class="col-md-6">
          <label for="ngayBatDau" class="form-label">Ngày bắt đầu</label>
          <input type="datetime-local" class="form-control" id="ngayBatDau" v-model="voucher.ngayBatDau" required
                 :class="{ 'is-invalid': errors.ngayBatDau }" @input="validateDates">
          <div class="text-danger" v-if="errors.ngayBatDau">{{ errors.ngayBatDau }}</div>
        </div>
        <div class="col-md-6">
          <label for="ngayHetHan" class="form-label">Ngày kết thúc</label>
          <input type="datetime-local" class="form-control" id="ngayHetHan" v-model="voucher.ngayHetHan" required
                 :class="{ 'is-invalid': errors.ngayHetHan }" @input="validateDates">
          <div class="text-danger" v-if="errors.ngayHetHan">{{ errors.ngayHetHan }}</div>
        </div>
        <div class="col-md-6">
          <label for="moTa" class="form-label">Mô tả</label>
          <textarea class="form-control" id="moTa" v-model="voucher.moTa" rows="3"></textarea>
        </div>
        <div class="col-12 mt-5">
          <div class="d-flex justify-content-center gap-3">
            <button type="submit" class="btn btn-primary" :disabled="hasErrors">Lưu</button>
            <button type="button" class="btn btn-secondary" @click="router.push('/admin/quanlyvoucher')">Quay lại</button>
          </div>
        </div>
      </div>
    </form>
    <div v-else>Loading...</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useGbStore } from '@/stores/gbStore';
import { toast } from 'vue3-toastify';

const router = useRouter();
const route = useRoute();
const store = useGbStore();

const voucher = ref(null);
const originalMaVoucher = ref('');

const errors = ref({
  maVoucher: '',
  tenVoucher: '',
  giaTriGiam: '',
  giaTriToiThieu: '',
  soLuong: '',
  kieuGiamGia: '',
  giaTriToiDa: '',
  ngayBatDau: '',
  ngayHetHan: '',
});

// Display values for formatted inputs
const displayGiaTriGiam = ref('');
const displayGiaTriToiThieu = ref('');
const displayGiaTriToiDa = ref('');

onMounted(async () => {
  const id = route.params.id;
  try {
    const voucherData = await store.getVoucherById(id);
    const adjustToLocalTime = (dateStr) => {
      const date = new Date(dateStr);
      const offset = 7 * 60; // Adjust for UTC+7
      const localDate = new Date(date.getTime() + offset * 60 * 1000);
      return localDate.toISOString().slice(0, 16);
    };

    voucher.value = {
      id: voucherData.id,
      maVoucher: voucherData.maVoucher,
      tenVoucher: voucherData.tenVoucher,
      giaTriGiam: voucherData.giaTriGiam,
      giaTriToiThieu: voucherData.giaTriToiThieu,
      soLuong: voucherData.soLuong,
      kieuGiamGia: voucherData.kieuGiamGia,
      giaTriToiDa: voucherData.giaTriToiDa,
      ngayBatDau: adjustToLocalTime(voucherData.ngayBatDau),
      ngayHetHan: adjustToLocalTime(voucherData.ngayHetHan),
      moTa: voucherData.moTa,
    };
    originalMaVoucher.value = voucherData.maVoucher;

    // Initialize display values
    displayGiaTriGiam.value = formatCurrency(voucher.value.giaTriGiam);
    displayGiaTriToiThieu.value = formatCurrency(voucher.value.giaTriToiThieu);
    displayGiaTriToiDa.value = formatCurrency(voucher.value.giaTriToiDa);
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết voucher:', error);
    toast.error('Không thể tải thông tin voucher', { autoClose: 3000 });
    router.push('/admin/quanlyvoucher');
  }
});

// Format number to VND currency (e.g., 1000000 -> 1.000.000)
const formatCurrency = (value, includeCurrency = false) => {
  if (value === null || value === '' || isNaN(value)) return '';
  const formatted = new Intl.NumberFormat('vi-VN', {
    maximumFractionDigits: 0,
    minimumFractionDigits: 0,
  }).format(value);
  return includeCurrency ? `${formatted} ₫` : formatted;
};

// Parse currency input (e.g., "1.000.000" -> 1000000)
const parseCurrency = (value) => {
  if (!value) return null;
  return parseFloat(value.replace(/\./g, '').replace(',', '.')) || null;
};

// Handle currency input for giaTriGiam, giaTriToiThieu, giaTriToiDa
const handleCurrencyInput = (field, event) => {
  const rawValue = event.target.value.replace(/\./g, '');
  const parsedValue = parseCurrency(rawValue);
  voucher.value[field] = parsedValue;
  if (field === 'giaTriGiam') {
    displayGiaTriGiam.value = formatCurrency(parsedValue);
    validateGiaTriGiam();
  }
  if (field === 'giaTriToiThieu') {
    displayGiaTriToiThieu.value = formatCurrency(parsedValue);
    validateGiaTriToiThieu();
  }
  if (field === 'giaTriToiDa') {
    displayGiaTriToiDa.value = formatCurrency(parsedValue);
    validateGiaTriToiDa();
  }
};

// Handle tenVoucher input to trim only
const handleTenVoucherInput = () => {
  voucher.value.tenVoucher = voucher.value.tenVoucher.trim();
  validateTenVoucher();
};

const validateMaVoucher = () => {
  if (!voucher.value.maVoucher) {
    errors.value.maVoucher = 'Mã voucher không được để trống!';
  } else if (!/^[a-zA-Z0-9]+$/.test(voucher.value.maVoucher)) {
    errors.value.maVoucher = 'Mã voucher chỉ được chứa chữ cái và số!';
  } else {
    errors.value.maVoucher = '';
  }
};

const validateTenVoucher = () => {
  if (!voucher.value.tenVoucher) {
    errors.value.tenVoucher = 'Tên voucher không được để trống!';
  } else {
    errors.value.tenVoucher = '';
  }
};

const validateGiaTriGiam = () => {
  const giaTri = voucher.value.giaTriGiam;
  if (giaTri === null || isNaN(giaTri) || giaTri <= 0) {
    errors.value.giaTriGiam = 'Giá trị giảm phải lớn hơn 0!';
  } else if (giaTri > 5000000) {
    errors.value.giaTriGiam = `Giá trị giảm không được lớn hơn ${formatCurrency(5000000, true)}!`;
  } else if (voucher.value.kieuGiamGia === 'Phần trăm' && giaTri > 100) {
    errors.value.giaTriGiam = 'Giá trị giảm không được vượt quá 100 khi chọn Phần trăm!';
  } else {
    errors.value.giaTriGiam = '';
  }

  if (voucher.value.kieuGiamGia === 'Tiền mặt') {
    voucher.value.giaTriToiDa = giaTri;
    displayGiaTriToiDa.value = formatCurrency(giaTri);
    validateGiaTriToiDa();
  }
};

const validateGiaTriToiThieu = () => {
  const giaTri = voucher.value.giaTriToiThieu || 0;
  if (giaTri < 0) {
    errors.value.giaTriToiThieu = 'Giá trị tối thiểu không được nhỏ hơn 0!';
  } else if (giaTri > 5000000) {
    errors.value.giaTriToiThieu = `Giá trị tối thiểu không được lớn hơn ${formatCurrency(5000000, true)}!`;
  } else {
    errors.value.giaTriToiThieu = '';
  }
};

const validateSoLuong = () => {
  const soLuong = parseInt(voucher.value.soLuong) || 0;
  if (voucher.value.soLuong === null || soLuong <= 0) {
    errors.value.soLuong = 'Số lượng phải lớn hơn 0!';
  } else if (soLuong > 5000000) {
    errors.value.soLuong = `Số lượng không được lớn hơn ${formatCurrency(5000000)}!`;
  } else {
    errors.value.soLuong = '';
  }
};

const validateGiaTriToiDa = () => {
  const giaTri = voucher.value.giaTriToiDa || 0;
  if (voucher.value.kieuGiamGia === 'Tiền mặt') {
    voucher.value.giaTriToiDa = voucher.value.giaTriGiam;
    displayGiaTriToiDa.value = formatCurrency(voucher.value.giaTriGiam);
    errors.value.giaTriToiDa = '';
  } else if (giaTri <= 0) {
    errors.value.giaTriToiDa = 'Giá trị tối đa phải lớn hơn 0!';
  } else if (giaTri > 5000000) {
    errors.value.giaTriToiDa = `Giá trị tối đa không được lớn hơn ${formatCurrency(5000000, true)}!`;
  } else {
    errors.value.giaTriToiDa = '';
  }
};

const validateKieuGiamGia = () => {
  if (!voucher.value.kieuGiamGia || voucher.value.kieuGiamGia.trim() === '') {
    errors.value.kieuGiamGia = 'Kiểu giảm giá không được để trống!';
  } else {
    errors.value.kieuGiamGia = '';
    validateGiaTriGiam();
  }
};

const validateDates = () => {
  if (!voucher.value.ngayBatDau) {
    errors.value.ngayBatDau = 'Ngày bắt đầu không được để trống!';
  } else {
    errors.value.ngayBatDau = '';
  }

  if (!voucher.value.ngayHetHan) {
    errors.value.ngayHetHan = 'Ngày kết thúc không được để trống!';
  } else if (voucher.value.ngayBatDau && new Date(voucher.value.ngayHetHan) < new Date(voucher.value.ngayBatDau)) {
    errors.value.ngayHetHan = 'Ngày kết thúc phải sau ngày bắt đầu!';
  } else {
    errors.value.ngayHetHan = '';
  }
};

const hasErrors = computed(() => {
  return Object.values(errors.value).some(error => error !== '');
});

// Watch kieuGiamGia and giaTriGiam to sync giaTriToiDa
watch(() => voucher.value?.kieuGiamGia, () => {
  if (voucher.value.kieuGiamGia === 'Tiền mặt') {
    voucher.value.giaTriToiDa = voucher.value.giaTriGiam;
    displayGiaTriToiDa.value = formatCurrency(voucher.value.giaTriGiam);
  }
  validateKieuGiamGia();
});

watch(() => voucher.value?.giaTriGiam, (newValue) => {
  displayGiaTriGiam.value = formatCurrency(newValue);
  if (voucher.value.kieuGiamGia === 'Tiền mặt') {
    voucher.value.giaTriToiDa = newValue;
    displayGiaTriToiDa.value = formatCurrency(newValue);
  }
  validateGiaTriGiam();
});

watch(() => voucher.value?.giaTriToiThieu, (newValue) => {
  displayGiaTriToiThieu.value = formatCurrency(newValue);
  validateGiaTriToiThieu();
});

watch(() => voucher.value?.giaTriToiDa, (newValue) => {
  displayGiaTriToiDa.value = formatCurrency(newValue);
  validateGiaTriToiDa();
});

const submitForm = async () => {
  validateMaVoucher();
  validateTenVoucher();
  validateGiaTriGiam();
  validateGiaTriToiThieu();
  validateSoLuong();
  validateKieuGiamGia();
  validateGiaTriToiDa();
  validateDates();

  if (hasErrors.value) {
    toast.error('Vui lòng sửa các lỗi trước khi lưu!', { autoClose: 1000 });
    return;
  }

  try {
    const response = await store.updateVoucher(voucher.value);
    if (response === 'Cập nhật voucher thành công!') {
      toast.success(response, {
        autoClose: 1000,
        onClose: () => router.push('/admin/quanlyvoucher'),
      });
    } else {
      toast.error(response, { autoClose: 1000 });
    }
  } catch (error) {
    console.error('Lỗi khi cập nhật voucher:', error);
    toast.error('Có lỗi xảy ra khi cập nhật voucher', { autoClose: 1000 });
  }
};
</script>

<style scoped>
.btn-primary {
  background-color: #d02c39;
  border-color: #d02c39;
  font-weight: bold;
}

.btn-primary:hover {
  background-color: #f33b47;
  border-color: #f33b47;
}

.is-invalid {
  border-color: #dc3545;
}

.text-danger {
  font-size: 0.875em;
  margin-top: 0.25rem;
}

.input-group-text {
  background-color: #f8f9fa;
  border-color: #ced4da;
}
</style>