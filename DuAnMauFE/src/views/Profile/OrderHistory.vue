<template>
  <div class="order-history">
    <h2 class="page-title">Đơn hàng của tôi</h2>

    <!-- Order Filters -->
    <div class="order-filters">
      <a-tabs v-model:activeKey="activeStatus" @change="handleStatusChange">
        <a-tab-pane key="all" tab="Tất cả"></a-tab-pane>
        <a-tab-pane key="pending" tab="Chờ xác nhận"></a-tab-pane>
        <a-tab-pane key="processing" tab="Đang xử lý"></a-tab-pane>
        <a-tab-pane key="shipping" tab="Đang giao"></a-tab-pane>
        <a-tab-pane key="completed" tab="Hoàn thành"></a-tab-pane>
        <a-tab-pane key="cancelled" tab="Đã hủy"></a-tab-pane>
      </a-tabs>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <a-spin size="large" />
      <p>Đang tải đơn hàng...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="orders.length === 0" class="empty-state">
      <i class="fas fa-shopping-bag"></i>
      <h3>Chưa có đơn hàng nào</h3>
      <p>Hãy khám phá và đặt hàng ngay hôm nay!</p>
      <router-link to="/shop">
        <a-button type="primary" size="large">
          <i class="fas fa-shopping-cart"></i>
          Mua sắm ngay
        </a-button>
      </router-link>
    </div>

    <!-- Order List -->
    <div v-else class="order-list">
      <div 
        v-for="order in orders" 
        :key="order.idHoaDon"
        class="order-card"
      >
        <!-- Order Header -->
        <div class="order-header">
          <div class="order-info">
            <span class="order-code">
              <i class="fas fa-receipt"></i>
              {{ order.maHoaDon }}
            </span>
            <span class="order-date">
              <i class="fas fa-calendar"></i> 
              Đặt ngày {{ formatDate(order.ngayTao) }}
            </span>
          </div>
          <div :class="['order-status', getStatusClass(order.trangThai)]">
            {{ order.trangThai }}
          </div>
        </div>

        <!-- Order Items (preview first 2 items) -->
        <div class="order-items">
          <div 
            v-for="(item, index) in order.items?.slice(0, 2)" 
            :key="index"
            class="order-item"
          >
            <img 
              :src="item.imageUrl || '/images/no-image.png'" 
              :alt="item.tenSanPham"
              class="item-image"
            />
            <div class="item-info">
              <div class="item-name">{{ item.tenSanPham }}</div>
              <div class="item-details">
                {{ item.mauSac }} | {{ item.kichThuoc }} | x{{ item.soLuong }}
              </div>
            </div>
            <div class="item-price">
              {{ formatCurrency(item.donGia) }}
            </div>
          </div>
          <div v-if="order.items?.length > 2" class="more-items">
            +{{ order.items.length - 2 }} sản phẩm khác
          </div>
        </div>

        <!-- Order Footer -->
        <div class="order-footer">
          <div class="order-total">
            <span>Tổng tiền:</span>
            <span class="total-amount">{{ formatCurrency(order.tongTienSauGiam) }}</span>
          </div>
          <div class="order-actions">
            <router-link :to="`/profile/orders/${order.idHoaDon}`">
              <a-button size="large">Xem chi tiết</a-button>
            </router-link>
            <a-button 
              v-if="canReorder(order.trangThai)"
              type="primary" 
              size="large"
              @click="handleReorder(order)"
            >
              Mua lại
            </a-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalOrders > pageSize" class="pagination">
      <a-pagination
        v-model:current="currentPage"
        :total="totalOrders"
        :page-size="pageSize"
        @change="handlePageChange"
        show-size-changer
        :page-size-options="['5', '10', '20']"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { toast } from 'vue3-toastify';
import dayjs from 'dayjs';

const loading = ref(false);
const orders = ref([]);
const activeStatus = ref('all');
const currentPage = ref(1);
const pageSize = ref(10);
const totalOrders = ref(0);

onMounted(async () => {
  await loadOrders();
});

const loadOrders = async () => {
  try {
    loading.value = true;
    const khachHang = JSON.parse(localStorage.getItem('khachHang') || sessionStorage.getItem('khachHang'));
    
    if (!khachHang?.idKhachHang) {
      toast.error('Chưa đăng nhập');
      return;
    }

    // TODO: Call API to load orders
    // const response = await hoaDonService.getByKhachHang(khachHang.idKhachHang, currentPage.value, pageSize.value, activeStatus.value);
    
    // Mock data for demonstration
    orders.value = [];
    totalOrders.value = 0;
    
  } catch (error) {
    console.error('Error loading orders:', error);
    toast.error('Không thể tải danh sách đơn hàng');
  } finally {
    loading.value = false;
  }
};

const handleStatusChange = () => {
  currentPage.value = 1;
  loadOrders();
};

const handlePageChange = (page, size) => {
  currentPage.value = page;
  pageSize.value = size;
  loadOrders();
};

const formatDate = (date) => {
  return dayjs(date).format('DD/MM/YYYY HH:mm');
};

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value);
};

const getStatusClass = (status) => {
  const statusMap = {
    'Chờ xác nhận': 'pending',
    'Đang xử lý': 'processing',
    'Đang giao': 'shipping',
    'Hoàn thành': 'completed',
    'Đã hủy': 'cancelled'
  };
  return statusMap[status] || 'pending';
};

const canReorder = (status) => {
  return ['Hoàn thành', 'Đã hủy'].includes(status);
};

const handleReorder = (order) => {
  toast.info('Tính năng đang được phát triển');
};
</script>

<style scoped>
.order-history {
  max-width: 100%;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #ff6600;
}

.order-filters {
  margin: -20px 0 24px 0;
}

:deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

:deep(.ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #ff6600;
}

:deep(.ant-tabs-ink-bar) {
  background-color: #ff6600;
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
}

.loading-state p {
  margin-top: 16px;
  color: #666;
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

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: white;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.order-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  border-color: #ff6600;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f9f9f9;
  border-bottom: 1px solid #f0f0f0;
}

.order-info {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.order-code,
.order-date {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
}

.order-code {
  font-weight: 600;
  color: #333;
}

.order-status {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.order-status.pending {
  background: #fff7e6;
  color: #ff9800;
}

.order-status.processing {
  background: #e6f7ff;
  color: #1890ff;
}

.order-status.shipping {
  background: #f0f5ff;
  color: #597ef7;
}

.order-status.completed {
  background: #f6ffed;
  color: #52c41a;
}

.order-status.cancelled {
  background: #fff1f0;
  color: #ff4d4f;
}

.order-items {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.order-item {
  display: flex;
  gap: 16px;
  padding: 12px 0;
}

.order-item:not(:last-child) {
  border-bottom: 1px solid #f5f5f5;
}

.item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.item-details {
  font-size: 13px;
  color: #999;
}

.item-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff6600;
}

.more-items {
  text-align: center;
  padding: 12px;
  color: #666;
  font-size: 14px;
  background: #f9f9f9;
  border-radius: 6px;
  margin-top: 12px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fafafa;
}

.order-total {
  display: flex;
  gap: 12px;
  align-items: center;
  font-size: 15px;
}

.total-amount {
  font-size: 20px;
  font-weight: 700;
  color: #ff6600;
}

.order-actions {
  display: flex;
  gap: 12px;
}

:deep(.ant-btn-primary) {
  background-color: #ff6600;
  border-color: #ff6600;
}

:deep(.ant-btn-primary:hover) {
  background-color: #e55a00;
  border-color: #e55a00;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .order-footer {
    flex-direction: column;
    gap: 16px;
  }

  .order-actions {
    width: 100%;
    flex-direction: column;
  }

  .order-actions button {
    width: 100%;
  }
}
</style>
