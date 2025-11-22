<template>
  <div class="container-fluid">
    <!-- Phần lọc trạng thái -->
    <div class="d-flex align-items-center justify-content-between">
      <div class="me-3">
        <span class="fw-bold me-2" style="font-size: 16px;">Trạng thái:</span>
        <div class="form-check form-check-inline">
          <input class="form-check-input" type="radio" name="trangThai" id="active" value="Đang hoạt động"
            v-model="selectedTrangThai">
          <label class="form-check-label" for="active">Đang hoạt động</label>
        </div>
        <div class="form-check form-check-inline">
          <input class="form-check-input" type="radio" name="trangThai" id="inactive" value="Không hoạt động"
            v-model="selectedTrangThai">
          <label class="form-check-label" for="inactive">Không hoạt động</label>
        </div>
        <div class="form-check form-check-inline">
          <input class="form-check-input" type="radio" name="trangThai" id="all" value="" v-model="selectedTrangThai">
          <label class="form-check-label" for="all">Tất cả</label>
        </div>
      </div>
      <button class="btn buttonADD" @click="router.push('/admin/quanlykhachhang/add')">+ Tạo tài khoản</button>
    </div>

    <!-- Card chứa tiêu đề và select số lượng hiển thị -->
    <div class="card p-3 border-0">
      <div class="d-flex justify-content-between align-items-center border-bottom pb-2">
        <h5 class="fw-bold mb-0" style="color: #f33b47;">📋 Danh sách khách hàng</h5>
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

    <!-- Hiển thị thông báo khi không có dữ liệu -->
    <div v-if="danhSachKhachHang.length === 0" class="text-center py-4">
      <a-empty :image="simpleImage" />
    </div>
    <div v-else>
      <!-- Bảng danh sách khách hàng -->
      <div class="table-responsive mt-4">
        <table class="table table-hover">
          <thead>
            <tr>
              <th scope="col">#</th>
              <th scope="col">Mã KH</th>
              <th scope="col">Tên khách hàng</th>
              <th scope="col">Giới tính</th>
              <th scope="col">Ngày sinh</th>
              <th scope="col">Số điện thoại</th>
              <th scope="col">Email</th>
              <th scope="col">Địa chỉ</th>
              <th scope="col">Trạng thái</th>
              <th scope="col">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(khachHang, index) in danhSachKhachHang" :key="khachHang.idKhachHang">
              <td>{{ index + 1 }}</td>
              <td>{{ khachHang.maKhachHang }}</td>
              <td>{{ khachHang.hoTen }}</td>
              <td>{{ khachHang.gioiTinh ? 'Nam' : 'Nữ' }}</td>
              <td>{{ formatDate(khachHang.ngaySinh) }}</td>
              <td>{{ khachHang.soDienThoai }}</td>
              <td>{{ khachHang.email }}</td>
              <td>{{ diaChiMap[khachHang.idKhachHang] || 'Chưa có địa chỉ' }}</td>
              <td>
                <a-switch :checked="khachHang.trangThai === 'Đang hoạt động'" :style="{
                  backgroundColor: khachHang.trangThai === 'Đang hoạt động' ? '#f33b47' : '#ccc'
                }" @change="confirmChuyenTrangThai(khachHang.idKhachHang)" />
              </td>
              <td>
                <div class="d-flex gap-2 align-items-center">
                  <button class="btn btn-outline-danger btn-sm"
                    @click="router.push(`/admin/quanlykhachhang/update/${khachHang.idKhachHang}`)">
                    <i class="fas fa-edit"></i> Sửa
                  </button>
                  <button class="btn btn-outline-danger btn-sm text-nowrap small-text"
                    @click="confirmShowDetail(khachHang.idKhachHang)">
                    <i class="fas fa-eye"></i> Xem chi tiết
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Phân trang -->
      <div class="d-flex justify-content-center align-items-center mt-3">
        <button class="btn buttonPT p-0" @click="fetchData(currentPage - 1)" :disabled="currentPage === 0">
          Previous
        </button>
        <span class="mx-2">Trang {{ currentPage + 1 }} / {{ totalPages }}</span>
        <button class="btn buttonPT" @click="fetchData(currentPage + 1)" :disabled="currentPage >= totalPages - 1">
          Next
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, h } from 'vue';
import { useGbStore } from '@/stores/gbStore';
import { toast } from 'vue3-toastify';
import { Empty, Modal } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { SwapOutlined, EyeOutlined, ExclamationCircleOutlined } from '@ant-design/icons-vue';

const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;
const router = useRouter();
const store = useGbStore();
const pageSize = ref(5);
const selectedTrangThai = ref('');

// Computed properties để lấy dữ liệu từ store
const danhSachKhachHang = computed(() => store.getAllKhachHangArr.slice(0, pageSize.value));
const currentPage = computed(() => store.currentKhachHang);
const totalPages = computed(() => store.totalKhachHang);
const diaChiMap = computed(() => store.diaChiMap);

// Hàm định dạng ngày sinh
const formatDate = (dateString) => {
  if (!dateString) return 'Chưa có ngày sinh';
  const date = new Date(dateString);
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  return `${day}/${month}/${year}`;
};

// Hàm lấy dữ liệu
const fetchData = async (page) => {
  try {
    if (page < 0 || (totalPages.value > 0 && page >= totalPages.value)) return;
    await store.getAllKhachHang(page, pageSize.value, store.searchs, selectedTrangThai.value);
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu:', error);
    toast.error('Có lỗi xảy ra khi tải dữ liệu', {
      autoClose: 3000,
      position: "top-right"
    });
  }
};
const confirmChuyenTrangThai = (idKhachHang) => {
  if (store.id_roles === 3) {
      toast.warning('Bạn không có quyền thay đổi trạng thái của khách hàng!');
      return;
  }
  const khachHang = danhSachKhachHang.value.find(kh => kh.idKhachHang === idKhachHang);
  const newStatus = khachHang.trangThai === 'Đang hoạt động' ? 'Không hoạt động' : 'Đang hoạt động';
  
  Modal.confirm({
    title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
      h(SwapOutlined, { style: 'color: #faad14; font-size: 22px;' }),
      h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Xác nhận chuyển trạng thái')
    ]),
    content: () => h('div', { style: 'padding: 8px 0;' }, [
      h('p', { style: 'margin: 0 0 12px 0; font-size: 14px;' }, `Bạn có chắc chắn muốn chuyển trạng thái của khách hàng này sang "${newStatus}" không?`),
      h('div', { style: 'background: #fffbe6; padding: 12px; border-radius: 6px; border: 1px solid #ffe58f;' }, [
        h('div', { style: 'display: flex; align-items: center; gap: 8px; color: #faad14;' }, [
          h(ExclamationCircleOutlined, { style: 'font-size: 14px;' }),
          h('span', { style: 'font-size: 13px;' }, 'Trạng thái sẽ được cập nhật ngay lập tức')
        ])
      ])
    ]),
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    okButtonProps: { size: 'large', style: { height: '38px' } },
    cancelButtonProps: { size: 'large', style: { height: '38px' } },
    centered: true,
    width: 450,
    onOk: () => {
      chuyenTrangThai(idKhachHang);
    }
  });
};

// Hàm chuyển trạng thái
const chuyenTrangThai = async (idKhachHang) => {
  try {
    const success = await store.changeTrangThaiKhachHang(idKhachHang);
    if (success) {
      toast.success('Thay đổi trạng thái thành công');
      await fetchData(currentPage.value);
    } else {
      toast.error('Thay đổi trạng thái thất bại');
    }
  } catch (error) {
    console.error('Lỗi khi thay đổi trạng thái:', error);
    toast.error('Có lỗi xảy ra khi thay đổi trạng thái');
  }
};

const confirmShowDetail = (id) => {
  Modal.confirm({
    title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
      h(EyeOutlined, { style: 'color: #1890ff; font-size: 22px;' }),
      h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Xem chi tiết khách hàng')
    ]),
    content: () => h('div', { style: 'padding: 8px 0;' }, [
      h('p', { style: 'margin: 0; font-size: 14px;' }, 'Bạn có muốn xem chi tiết thông tin khách hàng này không?')
    ]),
    okText: 'Xem chi tiết',
    cancelText: 'Hủy',
    okButtonProps: { size: 'large', style: { height: '38px' } },
    cancelButtonProps: { size: 'large', style: { height: '38px' } },
    centered: true,
    width: 420,
    onOk: () => {
      showDetail(id);
    }
  });
};
// Hàm chuyển hướng đến trang chi tiết
const showDetail = (id) => {
  router.push({ name: 'admin-quan-ly-khach-hang-detail', params: { id } });
};

// Watch changes
watch(() => store.searchs, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    store.currentKhachHang = 0;
    fetchData(0);
  }
});

watch(selectedTrangThai, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    store.currentKhachHang = 0;
    fetchData(0);
  }
});

watch(pageSize, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    store.currentKhachHang = 0;
    fetchData(0);
  }
});

onMounted(() => {
  if (store.getAllKhachHangArr.length === 0) {
    fetchData(0);
  }
});
</script>

<style scoped>
.table {
  --bs-table-hover-bg: rgba(0, 0, 0, 0.05);
}

.btn-outline-primary {
  --bs-btn-hover-bg: #0d6efd;
  --bs-btn-hover-color: #fff;
}

.btn-outline-danger {
  --bs-btn-hover-bg: #dc3545;
  --bs-btn-hover-color: #fff;
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

.text-nowrap {
  white-space: nowrap;
}

.small-text {
  font-size: 0.85rem;
}

.toast-success {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1050;
  padding: 10px 20px;
  background-color: #28a745;
  color: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  font-size: 14px;
  min-width: 200px;
  text-align: center;
}
</style>
