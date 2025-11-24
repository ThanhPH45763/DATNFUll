<template>
  <div class="address-list">
    <div class="header-section">
      <h2 class="page-title">Địa chỉ của tôi</h2>
      <a-button type="primary" size="large" @click="showAddModal = true">
        <i class="fas fa-plus"></i>
        Thêm địa chỉ mới
      </a-button>
    </div>

    <!-- Empty State -->
    <div v-if="!loading && addresses.length === 0" class="empty-state">
      <i class="fas fa-map-marker-alt"></i>
      <h3>Chưa có địa chỉ nào</h3>
      <p>Thêm địa chỉ giao hàng để tiện cho việc mua sắm</p>
      <a-button type="primary" size="large" @click="showAddModal = true">
        Thêm địa chỉ đầu tiên
      </a-button>
    </div>

    <!-- Address Cards -->
    <div v-else class="address-grid">
      <div 
        v-for="address in addresses" 
        :key="address.idDiaChiKhachHang"
        class="address-card"
        :class="{ 'is-default': address.diaChiMacDinh }"
      >
        <div class="card-header">
          <div class="address-info">
            <i class="fas fa-map-marker-alt"></i>
            <span class="address-text">{{ getFullAddress(address) }}</span>
          </div>
          <div v-if="address.diaChiMacDinh" class="default-badge">
            <i class="fas fa-check-circle"></i>
            Mặc định
          </div>
        </div>

        <div class="card-actions">
          <a-button 
            v-if="!address.diaChiMacDinh"
            type="link" 
            @click="setDefault(address)"
          >
            Đặt làm mặc định
          </a-button>
          <a-button 
            type="link" 
            @click="editAddress(address)"
          >
            Sửa
          </a-button>
          <a-button 
            type="link" 
            danger
            @click="deleteAddress(address)"
            :disabled="address.diaChiMacDinh"
          >
            Xóa
          </a-button>
        </div>
      </div>
    </div>

    <!-- Add/Edit Modal -->
    <a-modal
      v-model:open="showAddModal"
      :title="editingAddress ? 'Chỉnh sửa địa chỉ' : 'Thêm địa chỉ mới'"
      :ok-text="editingAddress ? 'Cập nhật' : 'Thêm'"
      cancel-text="Hủy"
      @ok="handleSaveAddress"
      :confirm-loading="isSaving"
      width="600px"
    >
      <a-form :model="addressForm" layout="vertical" class="address-form">
        <a-form-item label="Số nhà, tên đường" required>
          <a-input 
            v-model:value="addressForm.soNha" 
            placeholder="VD: 123 Nguyễn Văn Linh"
            size="large"
          />
        </a-form-item>

        <a-form-item label="Xã/Phường" required>
          <a-input 
            v-model:value="addressForm.xaPhuong" 
            placeholder="Nhập xã/phường"
            size="large"
          />
        </a-form-item>

        <a-form-item label="Quận/Huyện" required>
          <a-input 
            v-model:value="addressForm.quanHuyen"
            placeholder="Nhập quận/huyện"
            size="large"
          />
        </a-form-item>

        <a-form-item label="Tỉnh/Thành phố" required>
          <a-input 
            v-model:value="addressForm.tinhThanhPho"
            placeholder="Nhập tỉnh/thành phố"
            size="large"
          />
        </a-form-item>

        <a-form-item>
          <a-checkbox v-model:checked="addressForm.diaChiMacDinh">
            Đặt làm địa chỉ mặc định
          </a-checkbox>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { toast } from 'vue3-toastify';
import { Modal } from 'ant-design-vue';
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
import { h } from 'vue';
import axiosInstance from '@/config/axiosConfig';

const loading = ref(false);
const isSaving = ref(false);
const showAddModal = ref(false);
const addresses = ref([]);
const editingAddress = ref(null);

const addressForm = ref({
  soNha: '',
  xaPhuong: '',
  quanHuyen: '',
  tinhThanhPho: '',
  diaChiMacDinh: false
});

// Load addresses
onMounted(async () => {
  await loadAddresses();
});

const loadAddresses = async () => {
  try {
    loading.value = true;
    const khachHang = JSON.parse(localStorage.getItem('khachHang') || sessionStorage.getItem('khachHang'));
    
    if (!khachHang?.idKhachHang) return;

    const response = await axiosInstance.get(`/api/khach-hang/detail/${khachHang.idKhachHang}`);
    
    if (response.data && response.data.diaChiList) {
      addresses.value = response.data.diaChiList;
    }
  } catch (error) {
    console.error('Error loading addresses:', error);
    toast.error('Không thể tải danh sách địa chỉ');
  } finally {
    loading.value = false;
  }
};

const getFullAddress = (address) => {
  return `${address.soNha}, ${address.xaPhuong}, ${address.quanHuyen}, ${address.tinhThanhPho}`;
};

const editAddress = (address) => {
  editingAddress.value = address;
  addressForm.value = {
    soNha: address.soNha,
    xaPhuong: address.xaPhuong,
    quanHuyen: address.quanHuyen,
    tinhThanhPho: address.tinhThanhPho,
    diaChiMacDinh: address.diaChiMacDinh
  };
  showAddModal.value = true;
};

const setDefault = async (address) => {
  try {
    // Implementation depends on backend API
    toast.info('Tính năng đang được phát triển');
  } catch (error) {
    toast.error('Không thể đặt địa chỉ mặc định');
  }
};

const deleteAddress = (address) => {
  Modal.confirm({
    title: 'Xác nhận xóa',
    icon: h(ExclamationCircleOutlined),
    content: 'Bạn có chắc chắn muốn xóa địa chỉ này?',
    okText: 'Xóa',
    okType: 'danger',
    cancelText: 'Hủy',
    onOk: async () => {
      try {
        // Implementation depends on backend API
        toast.info('Tính năng đang được phát triển');
      } catch (error) {
        toast.error('Không thể xóa địa chỉ');
      }
    }
  });
};

const handleSaveAddress = async () => {
  try {
    isSaving.value = true;
    
    // Validate
    if (!addressForm.value.soNha || !addressForm.value.xaPhuong || 
        !addressForm.value.quanHuyen || !addressForm.value.tinhThanhPho) {
      toast.error('Vui lòng điền đầy đủ thông tin địa chỉ');
      return;
    }

    // Implementation depends on backend API
    toast.info('Tính năng đang được phát triển');
    
    showAddModal.value = false;
    resetForm();
  } catch (error) {
    toast.error('Có lỗi xảy ra khi lưu địa chỉ');
  } finally {
    isSaving.value = false;
  }
};

const resetForm = () => {
  editingAddress.value = null;
  addressForm.value = {
    soNha: '',
    xaPhuong: '',
    quanHuyen: '',
    tinhThanhPho: '',
    diaChiMacDinh: false
  };
};
</script>

<style scoped>
.address-list {
  max-width: 100%;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #ff6600;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

:deep(.ant-btn-primary) {
  background-color: #ff6600;
  border-color: #ff6600;
}

:deep(.ant-btn-primary:hover) {
  background-color: #e55a00;
  border-color: #e55a00;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: #f9f9f9;
  border-radius: 12px;
}

.empty-state i {
  font-size: 64px;
  color: #ccc;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 8px;
}

.empty-state p {
  color: #666;
  margin-bottom: 24px;
}

.address-grid {
  display: grid;
  gap: 16px;
}

.address-card {
  background: white;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s;
}

.address-card.is-default {
  border-color: #ff6600;
  background: #fff7e6;
}

.address-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.address-info {
  display: flex;
  gap: 12px;
  flex: 1;
}

.address-info i {
  font-size: 18px;
  color: #ff6600;
  margin-top: 2px;
}

.address-text {
  font-size: 15px;
  color: #333;
  line-height: 1.6;
}

.default-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: #ff6600;
  color: white;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
}

.card-actions {
  display: flex;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

:deep(.ant-btn-link) {
  color: #ff6600;
}

:deep(.ant-btn-link:hover) {
  color: #e55a00;
}

.address-form :deep(.ant-input) {
  border-radius: 8px;
}

.address-form :deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #ff6600;
  border-color: #ff6600;
}

@media (max-width: 768px) {
  .header-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-section button {
    width: 100%;
  }

  .card-actions {
    flex-direction: column;
  }
}
</style>
