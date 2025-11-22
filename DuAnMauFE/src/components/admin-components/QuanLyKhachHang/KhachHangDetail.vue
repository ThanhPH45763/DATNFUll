<template>
  <div class="container-fluid">
    <!-- Card chính -->
    <div class="card shadow-lg border-0">
      <!-- Card Header -->
      <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
        <h5 class="mb-0">
          <i class="fas fa-user me-2"></i>Chi tiết khách hàng
        </h5>
        <button class="btn btn-light btn-sm" @click="router.push('/admin/quanlykhachhang')">
          <i class="fas fa-arrow-left me-1"></i>Quay lại
        </button>
      </div>

      <!-- Card Body -->
      <div class="card-body p-4" v-if="khachHang">
        <!-- Thông tin khách hàng -->
        <div class="info-section">
          <h6 class="section-title text-primary mb-3">
            <i class="fas fa-info-circle me-1"></i>Thông tin cơ bản
          </h6>
          <div class="row g-4">
            <!-- Mã khách hàng -->
            <div class="col-md-6">
              <div class="info-item">
                <label class="text-muted small">Mã khách hàng</label>
                <p class="fw-medium mb-0">{{ khachHang.maKhachHang }}</p>
              </div>
            </div>

            <!-- Tên khách hàng -->
            <div class="col-md-6">
              <div class="info-item">
                <label class="text-muted small">Tên khách hàng</label>
                <p class="fw-medium mb-0">{{ khachHang.hoTen }}</p>
              </div>
            </div>

            <!-- Giới tính -->
            <div class="col-md-6">
              <div class="info-item">
                <label class="text-muted small">Giới tính</label>
                <p class="fw-medium mb-0">
                  <span :class="khachHang.gioiTinh ? 'badge bg-info' : 'badge bg-warning'">
                    {{ khachHang.gioiTinh ? 'Nam' : 'Nữ' }}
                  </span>
                </p>
              </div>
            </div>

            <!-- Ngày sinh -->
            <div class="col-md-6">
              <div class="info-item">
                <label class="text-muted small">Ngày sinh</label>
                <p class="fw-medium mb-0">{{ formatDate(khachHang.ngaySinh) }}</p>
              </div>
            </div>

            <!-- Số điện thoại -->
            <div class="col-md-6">
              <div class="info-item">
                <label class="text-muted small">Số điện thoại</label>
                <p class="fw-medium mb-0">{{ khachHang.soDienThoai }}</p>
              </div>
            </div>

            <!-- Email -->
            <div class="col-md-6">
              <div class="info-item">
                <label class="text-muted small">Email</label>
                <p class="fw-medium mb-0">{{ khachHang.email }}</p>
              </div>
            </div>

            <!-- Trạng thái -->
            <div class="col-md-6">
              <div class="info-item">
                <label class="text-muted small">Trạng thái</label>
                <p class="fw-medium mb-0">
                  <span :class="khachHang.trangThai === 'Đang hoạt động' ? 'badge bg-success' : 'badge bg-danger'">
                    {{ khachHang.trangThai }}
                  </span>
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Danh sách địa chỉ -->
        <div class="info-section mt-4 address-section">
          <h6 class="section-title text-primary mb-3">
            <i class="fas fa-map-marker-alt me-1"></i>Danh sách địa chỉ
          </h6>
          <div v-if="diaChiList && diaChiList.length > 0">
            <div class="table-responsive">
              <table class="table table-hover table-bordered align-middle">
                <thead class="table-light">
                  <tr>
                    <th scope="col" class="text-center">#</th>
                    <th scope="col">Số nhà</th>
                    <th scope="col">Xã/Phường</th>
                    <th scope="col">Quận/Huyện</th>
                    <th scope="col">Tỉnh/Thành phố</th>
                    <th scope="col" class="text-center">Mặc định</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(diaChi, index) in diaChiList" :key="diaChi.idDiaChi">
                    <td class="text-center">{{ index + 1 }}</td>
                    <td>{{ diaChi.soNha }}</td>
                    <td>{{ diaChi.xaPhuong }}</td>
                    <td>{{ diaChi.quanHuyen }}</td>
                    <td>{{ diaChi.tinhThanhPho }}</td>
                    <td class="text-center">
                      <span :class="diaChi.diaChiMacDinh ? 'badge bg-success' : 'badge bg-secondary'">
                        {{ diaChi.diaChiMacDinh ? 'Có' : 'Không' }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div v-else class="text-center py-4">
            <p class="text-muted mb-0">
              <i class="fas fa-exclamation-circle me-1"></i>Khách hàng chưa có địa chỉ nào.
            </p>
          </div>
        </div>
      </div>

      <!-- Hiển thị khi không có dữ liệu -->
      <div class="card-body text-center py-5" v-else>
        <p class="text-danger mb-0">
          <i class="fas fa-exclamation-triangle me-1"></i>Không tìm thấy thông tin khách hàng.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { toast } from 'vue3-toastify';
import { useGbStore } from '@/stores/gbStore';

const router = useRouter();
const route = useRoute();
const store = useGbStore();

const khachHang = ref(null);
const diaChiList = ref([]);
const matKhau = ref('');

// Hàm định dạng ngày sinh
const formatDate = (dateString) => {
  if (!dateString) return 'Chưa có ngày sinh';
  const date = new Date(dateString);
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  return `${day}/${month}/${year}`;
};

// Hàm lấy chi tiết khách hàng
const loadKhachHangDetail = async () => {
  try {
    const id = route.params.id;
    const response = await store.getKhachHangDetail(id);

    if (!response) {
      toast.error('Không tìm thấy thông tin khách hàng');
      router.push('/admin/quanlykhachhang');
      return;
    }

    khachHang.value = response.khachHang;
    diaChiList.value = response.diaChiList || [];
    matKhau.value = response.matKhau;
  } catch (error) {
    console.error('Lỗi khi tải chi tiết khách hàng:', error);
    toast.error('Có lỗi xảy ra khi tải chi tiết khách hàng');
    router.push('/admin/quanlykhachhang');
  }
};

onMounted(() => {
  loadKhachHangDetail();
});
</script>

<style scoped>
/* Tinh chỉnh container để khớp với khung của danh sách khách hàng */
.container-fluid {
  padding: 0 ; /* Giữ padding giống container-fluid trong danh sách khách hàng */

}

/* Tinh chỉnh card */


.card-header {
  background: linear-gradient(90deg, #007bff, #0056b3);
  padding: 1rem 1.5rem;
  flex-shrink: 0; /* Không co lại */
}

.card-body {
  background-color: #f8f9fa;
  flex: 1; /* Chiếm toàn bộ không gian còn lại */
  padding: 1.5rem;
  overflow-y: auto; /* Thanh cuộn nếu nội dung vượt quá chiều cao */
}

/* Tinh chỉnh section tiêu đề */
.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  border-bottom: 2px solid #007bff;
  padding-bottom: 0.5rem;
}

/* Tinh chỉnh thông tin khách hàng */
.info-item {
  background: #fff;
  padding: 0.75rem; /* Giảm padding để tiết kiệm không gian */
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.info-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.info-item label {
  font-size: 0.85rem;
  margin-bottom: 0.25rem;
  display: block;
}

.info-item p {
  font-size: 0.95rem; /* Giảm font-size để tiết kiệm không gian */
  color: #333;
}

/* Tinh chỉnh phần danh sách địa chỉ */
.address-section {
  margin-top: 1rem; /* Giảm margin-top để tiết kiệm không gian */
}

/* Tinh chỉnh bảng địa chỉ */
.table {
  margin-bottom: 0;
  border-collapse: separate;
  border-spacing: 0;
  --bs-table-hover-bg: rgba(0, 0, 0, 0.05); /* Giữ style hover giống danh sách khách hàng */
}

.table th,
.table td {
  vertical-align: middle;
  padding: 0.5rem; /* Giảm padding để tiết kiệm không gian */
  font-size: 0.9rem; /* Giảm font-size để tiết kiệm không gian */
}

.table thead th {
  background-color: #e9ecef;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #dee2e6;
  position: sticky;
  top: 0;
  z-index: 1;
}

.table tbody tr {
  transition: background-color 0.2s ease;
}

.table tbody tr:hover {
  background-color: #f1f3f5;
}

/* Tinh chỉnh badge */
.badge {
  font-size: 0.8rem; /* Giảm font-size để tiết kiệm không gian */
  padding: 0.3em 0.6em;
  border-radius: 10px;
}

/* Tinh chỉnh màu sắc */
.text-primary {
  color: #007bff !important;
}

.text-muted {
  color: #6c757d !important;
}

.bg-success {
  background-color: #28a745 !important;
}

.bg-danger {
  background-color: #dc3545 !important;
}

.bg-info {
  background-color: #17a2b8 !important;
}

.bg-warning {
  background-color: #ffc107 !important;
}

.bg-secondary {
  background-color: #6c757d !important;
}
</style>
