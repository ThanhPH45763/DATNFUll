<template>
    <a-list :data-source="filteredOrders" :pagination="{ pageSize: 5 }">
      <template #renderItem="{ item }">
        <a-list-item>
          <a-card style="width: 100%">
            <a-row>
              <a-col :span="16">
                <p>Mã đơn: {{ item.ma_hoa_don }}</p>
                <p>Ngày đặt: {{ formatDate(item.ngay_tao) }}</p>
                <p>
                  Trạng thái:
                  <a-tag :color="getOrderStatusColor(item.trang_thai)">
                    {{ item.trang_thai }}
                  </a-tag>
                </p>
              </a-col>
              <a-col :span="8" style="text-align: right">
                <p>Tổng tiền: {{ formatCurrency(item.tong_tien_sau_giam) }}</p>
                <a-button type="primary" size="small" @click="$emit('view-detail', item.id_hoa_don)">
                  Chi tiết
                </a-button>
              </a-col>
            </a-row>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
</template>
  
<script setup>
  import { computed } from 'vue';
  
  const props = defineProps({
    orders: Array,
    statusFilter: String
  });
  
  const emit = defineEmits(['view-detail']);
  
  const filteredOrders = computed(() => {
    if (!props.orders) return [];
    if (props.statusFilter === 'all') return props.orders;
    return props.orders.filter(
      o => o.trang_thai?.toLowerCase().trim() === props.statusFilter.toLowerCase().trim()
    );
  });
  
  const formatDate = (date) => {
    const d = new Date(date);
    return `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getFullYear()}`;
  };
  
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };
  
  const getOrderStatusColor = (status) => {
    const colors = {
      'Chờ xác nhận': 'orange',
      'Đang giao': 'blue',
      'Đã giao': 'green',
      'Đã hủy': 'red',
      'Chưa thanh toán': 'gray'
    };
    return colors[status] || 'default';
  };
</script>
