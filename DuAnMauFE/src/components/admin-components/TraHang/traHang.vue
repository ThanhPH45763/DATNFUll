<template>
  <div class="container-fluid main-content">
      <div v-if="loading" class="text-center">
          <a-spin tip="Đang tải dữ liệu..." />
      </div>
      <div v-else>
          <!-- Header Section -->
          <div class="tracking-header">
              <h1>Tra cứu đơn hàng</h1>
              <p class="tracking-description">
                  Nhập mã đơn hàng để tra cứu thông tin và xử lý trả hàng hoàn tiền
              </p>
          </div>

        <!-- Search Section -->
      <div class="tracking-search" v-if="!orderFound">
        <a-form layout="vertical" @submit.prevent="searchOrder" aria-label="Form tra cứu đơn hàng">
          <a-form-item label="Mã đơn hàng" name="orderCode">
            <a-input v-model:value="orderCode" placeholder="Nhập mã đơn hàng" :maxLength="50" :allowClear="true" size="large" />
          </a-form-item>
          <a-form-item>
            <a-row :gutter="16">
              <a-col :span="12">
                <a-button type="primary" html-type="submit" :loading="searching" size="large" block>
                  <search-outlined /> Tra cứu
                </a-button>
              </a-col>
              <a-col :span="12">
                <a-button type="default" @click="showQrScanner" size="large" block>
                  <scan-outlined /> Quét QR
                </a-button>
              </a-col>
            </a-row>
          </a-form-item>
        </a-form>
      </div>

      <!-- Not Found State -->
      <div v-if="!orderFound && searched" class="not-found">
        <file-search-outlined class="not-found-icon" />
        <h2>Không tìm thấy đơn hàng</h2>
        <p>Không tìm thấy đơn hàng với mã bạn vừa nhập.</p>
        <p>Vui lòng kiểm tra lại mã đơn hàng hoặc liên hệ với chúng tôi để được hỗ trợ.</p>
        <a-button type="primary" @click="resetSearch" size="large">Thử lại</a-button>
      </div>

      <!-- Return Interface (shown when order is found) -->
      <div v-if="orderFound" class="order-details">
        

        <!-- Quantity Input Modal -->
        <a-modal
          v-model:open="quantityModalVisible"
          title="Nhập số lượng trả"
          @ok="confirmQuantity"
          @cancel="cancelQuantity"
          :ok-button-props="{ disabled: !selectedQuantity || selectedQuantity < 1 || selectedQuantity > maxQuantity }"
          aria-label="Modal nhập số lượng trả hàng"
        >
          <a-form layout="vertical">
            <a-form-item label="Số lượng trả hàng">
              <a-input-number
                v-model:value="selectedQuantity"
                :min="1"
                :max="maxQuantity"
                placeholder="Nhập số lượng"
                style="width: 100%;"
              />
            </a-form-item>
          </a-form>
        </a-modal>

        <!-- Print Confirmation Modal -->
        <a-modal
          v-model:open="showPrintConfirm"
          title="Xác nhận xuất hóa đơn"
          @ok="confirmPrint(true)"
          @cancel="confirmPrint(false)"
        >
          <p>Bạn muốn xuất hóa đơn này phải không?</p>
          <template #footer>
            <a-button key="cancel" @click="confirmPrint(false)">Không</a-button>
            <a-button key="ok" type="primary" @click="confirmPrint(true)">Đúng</a-button>
          </template>
        </a-modal>
      </div>

      <!-- QR Scanner Container -->
      <div v-show="qrScannerVisible" class="qr-scanner-container">
        <div class="qr-scanner-content">
          <h2>Quét mã QR đơn hàng</h2>
          <div ref="qrReader" id="qr-reader" class="qr-reader"></div>
          <a-button type="default" @click="stopQrScanner" size="large" block>
            Hủy
          </a-button>
        </div>
      </div>

          <!-- Return Interface (shown when order is found) -->
          <div v-if="orderFound" class="order-details">
              <a-row :gutter="16">
                  <a-col :md="17">
                      <h2 style="color: #ff6600;">Trả hàng hoàn tiền #{{ orderDetail.ma_hoa_don || 'N/A' }}</h2>
                  </a-col>
                  <a-col :md="7" class="text-right">
                      <a-button type="primary" @click="resetSearch">Tìm đơn hàng khác</a-button>
                  </a-col>
              </a-row>
              <a-divider />

              <!-- Order Information -->
              <a-row :gutter="16">
                  <a-col :md="24">
                      <div class="info-box">
                          <h5 class="text-center">Chính sách trả hàng</h5>
                          <hr>
                          <p class="text-center mb-6">
                              G&B SPORTS cam kết mang đến cho khách hàng trải nghiệm mua sắm tốt nhất. Vì vậy, chúng tôi hỗ trợ <strong>trả hàng</strong> trong các trường hợp sau:
                          </p>
                          <a-row :gutter="16">
                              <a-col :span="12">
                                  <h5 class="font-semibold mb-2">1. Điều kiện trả hàng</h5>
                                  <ul class="list-disc pl-5 space-y-2">
                                      <li>Sản phẩm còn <strong>nguyên tem, mác, chưa qua sử dụng hoặc giặt ủi</strong>.</li>
                                      <li>Sản phẩm bị <strong>lỗi do nhà sản xuất</strong> hoặc <strong>giao sai mẫu, size, màu sắc</strong>.</li>
                                      <li>Yêu cầu trả hàng được gửi <strong>trong vòng 14 ngày</strong> kể từ ngày nhận hàng.</li>
     
                                  </ul>
                              </a-col>
                              <a-col :span="12">
                                  <h5 class="font-semibold mb-2">2. Trường hợp không hỗ trợ trả hàng</h5>
                                  <ul class="list-disc pl-5 space-y-2">
                                      <li>Sản phẩm đã qua sử dụng, giặt ủi hoặc hư hỏng do khách hàng gây ra.</li>
                                      <li>Sản phẩm không còn tem mác hoặc bị can thiệp, chỉnh sửa.</li>
                                      <li>Trả hàng do lý do cá nhân như không thích, không hợp style...</li>
                                  </ul>
                              </a-col>
                          </a-row>
                      </div>
                  </a-col>
              </a-row>

              <!-- Order Information -->
              <a-row :gutter="16">
                  <a-col :md="24">
                      <div class="info-box">
                          <h5>Thông tin đơn hàng</h5>
                          <hr>
                          <a-row :gutter="16">
                              <a-col :span="12">
                                  <p>Mã hóa đơn: {{ orderDetail.ma_hoa_don || 'N/A' }}</p>
                                  <p>Trạng thái: {{ store.hoaDonDetail.trang_thai || 'N/A' }}</p>
                                  <p>Phương thức thanh toán: {{ orderDetail.hinh_thuc_thanh_toan || 'Chưa xác định' }}</p>
                              </a-col>
                              <a-col :span="12">
                                  <p>Ngày tạo: {{ formatDateTime(store.hoaDonDetail.ngay_chuyen) }}</p>
                                  <p>Tên khách hàng: {{ orderDetail.ho_ten || 'Chưa xác định' }}</p>
                                  <p>Địa chỉ: {{ orderDetail.dia_chi || 'Chưa xác định' }}</p>
                              </a-col>
                          </a-row>
                      </div>
                  </a-col>
              </a-row>

              <!-- Return Form and Products -->
              <a-row :gutter="16">
                  <a-col :md="16">
                      <div class="info-box">
                          <h5>Danh sách sản phẩm</h5>
                          <hr>
                          <a-table :columns="orderColumns" :data-source="orderProducts" :pagination="false" row-key="id_chi_tiet_san_pham">
                              <template #headerCell="{ column }">
                                  <template v-if="column.key === 'select'">
                                      <a-checkbox
                                          :checked="allSelected"
                                          :disabled="returnStatus === 'Đã hoàn tiền'"
                                          @change="toggleSelectAll"
                                      />
                                  </template>
                                  <template v-else>
                                      {{ column.title }}
                                  </template>
                              </template>
                              <template #bodyCell="{ column, record }">
                                  <template v-if="column.key === 'select'">
                                      <a-checkbox
                                          :checked="returnableProducts.some(p => p.id_chi_tiet_san_pham === record.id_chi_tiet_san_pham)"
                                          :disabled="returnStatus === 'Đã hoàn tiền'"
                                          @change="e => toggleReturnProduct(record, e.target.checked)"
                                      />
                                  </template>
                                  <template v-if="column.key === 'san_pham'">
                                      <img :src="record.hinh_anh || '/images/default.jpg'" alt="Product" class="product-image">
                                      {{ record.ten_san_pham || 'N/A' }} <br>
                                      Kích thước: {{ record.kich_thuoc || 'N/A' }},
                                      Màu: {{ record.ten_mau_sac || 'N/A' }}
                                  </template>
                                  <template v-if="column.key === 'don_gia'">
                                      {{ formatCurrency(record.unit_price) }} VNĐ
                                  </template>
                                  <template v-if="column.key === 'so_luong'">
                                      {{ record.so_luong }}
                                  </template>
                                  <template v-if="column.key === 'thanh_tien'">
                                      {{ formatCurrency(record.so_luong * record.unit_price) }} VNĐ
                                  </template>
                                  <template v-if="column.key === 'action'">
                                      <a-button
                                          type="primary"
                                          size="small"
                                          :disabled="returnStatus === 'Đã hoàn tiền' || returnableProducts.some(p => p.id_chi_tiet_san_pham === record.id_chi_tiet_san_pham)"
                                          @click="openQuantityModal(record)"
                                      >
                                          Chọn trả
                                      </a-button>
                                  </template>
                              </template>
                          </a-table>
                      </div>
                      <div class="info-box">
                          <h5>Danh sách sản phẩm trả</h5>
                          <hr>
                          <a-table
                              :columns="returnColumns"
                              :data-source="returnableProducts"
                              :pagination="false"
                              row-key="id_chi_tiet_san_pham"
                              :loading="submitting"
                          >
                              <template #bodyCell="{ column, record }">
                                  <template v-if="column.key === 'san_pham'">
                                      <img :src="record.hinh_anh || '/images/default.jpg'" alt="Product" class="product-image">
                                      {{ record.ten_san_pham || 'N/A' }} <br>
                                      Kích thước: {{ record.kich_thuoc || 'N/A' }},
                                      Màu: {{ record.ten_mau_sac || 'N/A' }}
                                  </template>
                                  <template v-if="column.key === 'don_gia'">
                                      {{ formatCurrency(record.unit_price) }} VNĐ
                                  </template>
                                  <template v-if="column.key === 'so_luong'">
                                      <a-input-number
                                          v-model:value="record.return_quantity"
                                          type="number"
                                          :min="0"
                                          :max="record.so_luong"
                                          :disabled="!canEditReturn"
                                          @change="updateReturnTotal"
                                      />
                                  </template>
                                  <template v-if="column.key === 'thanh_tien'">
                                      {{ formatCurrency(calculateRefundForProduct(record)) }} VNĐ
                                  </template>
                                  <template v-if="column.key === 'action'">
                                      <a-button
                                          type="danger"
                                          size="small"
                                          :disabled="!canEditReturn"
                                          @click="removeFromReturn(record)"
                                      >
                                          Xóa
                                      </a-button>
                                  </template>
                              </template>
                          </a-table>
                          <div class="total-section">
                              <a-row>
                                  <a-col :md="16"></a-col>
                                  <a-col :md="4" style="text-align: left;">
                                      <p>Tổng tiền hoàn:</p>
                                  </a-col>
                                  <a-col :md="4" style="text-align: right;">
                                      <h6 style="color: #ff6600;">{{ formatCurrency(totalRefundForDB) }} VNĐ</h6>
                                  </a-col>
                              </a-row>
                          </div>
                      </div>
                  </a-col>
                  <a-col :md="8">
                      <div class="info-box">
                          <h5>Thông tin trả hàng</h5>
                          <hr>
                          <p>Tổng tiền hàng: {{ formatCurrency(store.hoaDonDetail.tong_tien_truoc_giam) }} VNĐ</p>
                          <p>Giảm giá voucher: {{ formatCurrency((store.hoaDonDetail.tong_tien_truoc_giam || 0) + (store.hoaDonDetail.phi_van_chuyen || 0) - (store.hoaDonDetail.tong_tien_sau_giam || 0)) }} VNĐ</p>
                          <p>Phí vận chuyển: {{ formatCurrency(store.hoaDonDetail.phi_van_chuyen) }} VNĐ</p>
                          <p>Tổng tiền khách đã thanh toán: {{ formatCurrency(orderDetail.tong_tien_thanh_toan || 0) }} VNĐ</p>
                          <p>Tổng tiền hoàn: {{ formatCurrency(totalRefundForDB) }} VNĐ</p>
                          <a-form :model="returnForm" layout="vertical" @submit.prevent="submitReturn" aria-label="Form xử lý trả hàng">
                              <a-form-item label="Lý do trả hàng" name="reason" :rules="[{ required: true, message: 'Vui lòng chọn lý do trả hàng' }]">
                                  <a-select
                                      v-model:value="returnForm.reason"
                                      placeholder="Chọn lý do trả hàng"
                                      :disabled="!canEditReturn"
                                  >
                                      <a-select-option value="Sản phẩm lỗi">Sản phẩm lỗi</a-select-option>
                                      <a-select-option value="Có sự nhầm lẫn của nhân viên">Có sự nhầm lẫn của nhân viên</a-select-option>
                                  </a-select>
                              </a-form-item>
                              <a-form-item label="Ghi chú" name="note">
                                  <a-textarea
                                      v-model:value="returnForm.note"
                                      :rows="4"
                                      placeholder="Nhập ghi chú (tối đa 200 ký tự)"
                                      maxlength="200"
                                      :disabled="!canEditReturn"
                                  />
                              </a-form-item>
                              <a-form-item label="Nhân viên xử lý" name="staff">
                                  <a-input v-model:value="returnForm.staff" :readonly="true" />
                              </a-form-item>
                              <a-form-item>
                                  <a-button
                                      type="primary"
                                      html-type="submit"
                                      :disabled="!canSubmitReturn || !returnForm.reason || totalRefundForDB <= 0"
                                      :loading="submitting"
                                  >
                                      Xử lý trả hàng
                                  </a-button>
                                  <a-button
                                      type="default"
                                      style="margin-left: 8px;"
                                      @click="resetForm"
                                      v-if="canEditReturn"
                                  >
                                      Hủy
                                  </a-button>
                              </a-form-item>
                          </a-form>
                      </div>
                  </a-col>
              </a-row>

              <!-- Quantity Input Modal -->
              <a-modal
                  v-model:open="quantityModalVisible"
                  title="Nhập số lượng trả"
                  @ok="confirmQuantity"
                  @cancel="cancelQuantity"
                  :ok-button-props="{ disabled: !selectedQuantity || selectedQuantity < 1 || selectedQuantity > maxQuantity }"
                  aria-label="Modal nhập số lượng trả hàng"
              >
                  <a-form layout="vertical">
                      <a-form-item label="Số lượng trả hàng">
                          <a-input-number
                              v-model:value="selectedQuantity"
                              :min="1"
                              :max="maxQuantity"
                              placeholder="Nhập số lượng"
                              style="width: 100%;"
                          />
                      </a-form-item>
                  </a-form>
              </a-modal>

              <!-- Print Confirmation Modal -->
              <a-modal
                  v-model:open="showPrintConfirm"
                  title="Xác nhận xuất hóa đơn"
                  @ok="confirmPrint(true)"
                  @cancel="confirmPrint(false)"
              >
                  <p>Bạn muốn xuất hóa đơn này phải không?</p>
                  <template #footer>
                      <a-button key="cancel" @click="confirmPrint(false)">Không</a-button>
                      <a-button key="ok" type="primary" @click="confirmPrint(true)">Đúng</a-button>
                  </template>
              </a-modal>
          </div>
      </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useGbStore } from '@/stores/gbStore';
import { toast } from 'vue3-toastify';
import { SearchOutlined, FileSearchOutlined, ScanOutlined } from '@ant-design/icons-vue';
import { Row as ARow, Col as ACol, Button as AButton, Divider as ADivider, Form as AForm, FormItem as AFormItem, Input as AInput, Textarea as ATextarea, Table as ATable, InputNumber as AInputNumber, Spin as ASpin, Select as ASelect, SelectOption as ASelectOption, Modal as AModal, Checkbox as ACheckbox } from 'ant-design-vue';
import { Html5Qrcode } from 'html5-qrcode';
import jsPDF from 'jspdf';
import { debounce } from 'lodash';
import '../../../config/fonts/Roboto-normal';
import '../../../config/fonts/Roboto-bold';
import logo from '../../../images/logo/logo2.png';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import QRCode from 'qrcode';

// Initialize store and reactive state
const store = useGbStore();
const loading = ref(true);
const searching = ref(false);
const orderCode = ref('');
const orderFound = ref(false);
const searched = ref(false);
const orderDetail = ref({});
const orderProducts = ref([]);
const returnableProducts = ref([]);
const returnForm = ref({
  reason: null,
  note: '',
  staff: ''
});
const returnStatus = ref(null);
const submitting = ref(false);
const returnHistory = ref([]);
const showPrintConfirm = ref(false);

// Modal state for quantity input
const quantityModalVisible = ref(false);
const selectedQuantity = ref(null);
const selectedProduct = ref(null);
const maxQuantity = ref(0);

// Toast flags to prevent duplicates
let isSearchToastShown = false;
let isSubmitToastShown = false;
let isNegativeRefundToastShown = ref(false);

// Track displayed error messages to prevent duplicates
const displayedErrors = ref(new Set());

// State cho quét QR
const qrScannerVisible = ref(false);
const qrScanResult = ref('');
const qrReader = ref(null); // Ref to the qr-reader element
let html5QrCode = null;
let isProcessing = ref(false);

// Table columns for order and return products
const orderColumns = [
  { title: '', key: 'select', width: '5%' },
  { title: 'Sản phẩm', key: 'san_pham', width: '35%' },
  { title: 'Đơn giá', key: 'don_gia', width: '15%' },
  { title: 'Số lượng', key: 'so_luong', width: '15%' },
  { title: 'Thành tiền', key: 'thanh_tien', width: '15%' },
  { title: 'Hành động', key: 'action', width: '15%' }
];
const returnColumns = [
  { title: 'Sản phẩm', key: 'san_pham', width: '40%' },
  { title: 'Đơn giá', key: 'don_gia', width: '15%' },
  { title: 'Số lượng trả', key: 'so_luong', width: '15%' },
  { title: 'Thành tiền', key: 'thanh_tien', width: '15%' },
  { title: 'Hành động', key: 'action', width: '15%' }
];

// Computed properties
const canEditReturn = computed(() => {
  return returnStatus.value !== 'Đã hoàn tiền';
});

const returnTotal = computed(() => {
  return returnableProducts.value.reduce((total, item) => total + calculateRefundForProduct(item), 0);
});

const allProductsSelected = computed(() => {
  if (orderProducts.value.length === 0 || returnableProducts.value.length === 0) return false;
  return orderProducts.value.every(op => 
    returnableProducts.value.some(rp => 
      rp.id_chi_tiet_san_pham === op.id_chi_tiet_san_pham && rp.return_quantity === op.so_luong
    )
  );
});

const totalRefundForDB = computed(() => {
  const hasSelectedProducts = returnableProducts.value.some(p => p.return_quantity > 0);
  if (!hasSelectedProducts) {
    return 0;
  }
  if (allProductsSelected.value) {
    return orderDetail.value.tong_tien_thanh_toan || 0;
  }
  const voucherDiscount = (store.hoaDonDetail.tong_tien_truoc_giam || 0) + 
                         (store.hoaDonDetail.phi_van_chuyen || 0) - 
                         (store.hoaDonDetail.tong_tien_sau_giam || 0);
  return (returnTotal.value || 0) - voucherDiscount;
});

const canSubmitReturn = computed(() => {
  return returnableProducts.value.some(p => p.return_quantity > 0) && 
         !!returnForm.value.reason && 
         totalRefundForDB.value > 0;
});

const allSelected = computed(() => {
  return orderProducts.value.length > 0 &&
         orderProducts.value.every(p => returnableProducts.value.some(rp => rp.id_chi_tiet_san_pham === p.id_chi_tiet_san_pham && rp.return_quantity === p.so_luong));
});

// Watch totalRefundForDB for negative values
watch(totalRefundForDB, (newValue, oldValue) => {
  if (newValue < 0 && !isNegativeRefundToastShown.value) {
    isNegativeRefundToastShown.value = true;
    toast.error('Tổng tiền hoàn không được nhỏ hơn 0!', {
      onClose: () => {
        isNegativeRefundToastShown.value = false;
      }
    });
  } else if (newValue >= 0 && isNegativeRefundToastShown.value) {
    isNegativeRefundToastShown.value = false;
  }
});

// Watch returnableProducts to remove items with invalid return_quantity
watch(
  returnableProducts,
  (newProducts) => {
    const invalidProducts = newProducts.filter(
      p => !p.return_quantity || p.return_quantity <= 0
    );
    if (invalidProducts.length > 0) {
      returnableProducts.value = newProducts.filter(
        p => p.return_quantity && p.return_quantity > 0
      );
      console.log('Removed products with invalid return_quantity:', invalidProducts);
    }
  },
  { deep: true }
);

// Methods
// Calculate refund amount for a product (unit_price * return_quantity)
const calculateRefundForProduct = (item) => {
  return (item.unit_price || 0) * (item.return_quantity || 0);
};

// Format currency in VNĐ
const formatCurrency = (value) => {
  return value ? new Intl.NumberFormat('vi-VN').format(value) : '0';
};

// Format date for timeline
const formatDate = (date) => {
  if (!date) return 'N/A';
  const d = new Date(date);
  return d.toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
};

// Format full datetime for order details
const formatDateTime = (date) => {
  if (!date) return 'N/A';
  const d = new Date(date);
  return d.toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
};

// Toggle product selection for return
const toggleReturnProduct = (record, checked) => {
  if (checked) {
    if (record.unit_price == null || record.so_luong == null || record.so_luong <= 0) {
      console.error(`Sản phẩm "${record.ten_san_pham || 'N/A'}" (ID: ${record.id_chi_tiet_san_pham}) không có đơn giá hoặc số lượng hợp lệ:`, record);
      if (!isSearchToastShown) {
        isSearchToastShown = true;
        toast.error(`Sản phẩm "${record.ten_san_pham || 'N/A'}" không có đơn giá hoặc số lượng hợp lệ!`, {
          onClose: () => { isSearchToastShown = false; }
        });
      }
      return;
    }
    returnableProducts.value.push({
      ...record,
      return_quantity: record.so_luong || 0,
      unit_price: record.unit_price || 0
    });
  } else {
    returnableProducts.value = returnableProducts.value.filter(
      p => p.id_chi_tiet_san_pham !== record.id_chi_tiet_san_pham
    );
  }
  updateReturnTotal();
};

// Toggle select all products for return
const toggleSelectAll = (e) => {
  const checked = e.target.checked;
  if (checked) {
    const invalidProducts = orderProducts.value.filter(p => p.unit_price == null || p.so_luong == null || p.so_luong <= 0);
    if (invalidProducts.length > 0) {
      const productNames = invalidProducts.map(p => p.ten_san_pham || 'N/A').join(', ');
      console.error('Các sản phẩm không có đơn giá hoặc số lượng hợp lệ:', invalidProducts);
      if (!isSearchToastShown) {
        isSearchToastShown = true;
        toast.error(`Các sản phẩm [${productNames}] không có đơn giá hoặc số lượng hợp lệ!`, {
          onClose: () => { isSearchToastShown = false; }
        });
      }
      return;
    }
    returnableProducts.value = orderProducts.value.map(p => ({
      ...p,
      return_quantity: p.so_luong || 0,
      unit_price: p.unit_price || 0
    }));
  } else {
    returnableProducts.value = [];
  }
  updateReturnTotal();
};

// Search for an order by code
const searchOrder = debounce(async () => {
  if (searching.value || isSearchToastShown) {
    return;
  }

  if (!orderCode.value.trim()) {
    if (!isSearchToastShown) {
      isSearchToastShown = true;
      toast.warning('Vui lòng nhập mã đơn hàng!', {
        onClose: () => { isSearchToastShown = false; }
      });
    }
    return;
  }

  searching.value = true;
  try {
    const response = await store.getHoaDonDetails(orderCode.value);
    console.log('API Response:', response);
    if (!response || !response.ma_hoa_don) {
      throw new Error('Dữ liệu đơn hàng không hợp lệ');
    }
    store.$patch({ hoaDonDetail: response });
    if (!response.tong_tien_thanh_toan || response.tong_tien_thanh_toan <= 0) {
      console.warn('Tổng tiền thanh toán không hợp lệ:', response.tong_tien_thanh_toan);
      if (!isSearchToastShown) {
        isSearchToastShown = true;
        toast.warning('Tổng tiền thanh toán không hợp lệ, vui lòng kiểm tra đơn hàng!', {
          onClose: () => { isSearchToastShown = false; }
        });
      }
    }
    orderDetail.value = response;
    orderProducts.value = (response.chiTietHoaDons || []).map(item => {
      const unit_price = item.don_gia != null && item.so_luong > 0
        ? item.don_gia / item.so_luong
        : (item.gia_ban != null ? item.gia_ban : 0);
      return {
        ...item,
        unit_price
      };
    });
    const missingPriceProducts = orderProducts.value.filter(p => p.unit_price == null);
    if (missingPriceProducts.length > 0) {
      console.warn('Các sản phẩm thiếu đơn giá:', missingPriceProducts);
      if (!isSearchToastShown) {
        isSearchToastShown = true;
        toast.warning('Một số sản phẩm không có đơn giá, vui lòng kiểm tra dữ liệu!', {
          onClose: () => { isSearchToastShown = false; }
        });
      }
    }
    returnableProducts.value = [];
    returnStatus.value = response.return_status || 'Chưa yêu cầu';
    returnHistory.value = response.return_history || [];
    returnForm.value.staff = store.userDetails?.tenNhanVien || store.userInfo?.ten_dang_nhap || 'N/A';
    orderFound.value = true;
    searched.value = true;
    if (!isSearchToastShown) {
      isSearchToastShown = true;
      toast.success('Tìm thấy thông tin đơn hàng!', {
        onClose: () => { isSearchToastShown = false; }
      });
    }
  } catch (error) {
    console.error('Lỗi khi tìm kiếm đơn hàng:', error);
    orderFound.value = false;
    searched.value = true;
    if (!isSearchToastShown) {
      isSearchToastShown = true;
      toast.error(error.response?.data?.thong_bao || error.message || 'Không tìm thấy đơn hàng!', {
        onClose: () => { isSearchToastShown = false; }
      });
    }
  } finally {
    searching.value = false;
  }
}, 300);

// Reset search state
const resetSearch = () => {
  orderCode.value = '';
  orderFound.value = false;
  searched.value = false;
  orderDetail.value = {};
  orderProducts.value = [];
  returnableProducts.value = [];
  returnStatus.value = null;
  returnForm.value = { reason: null, note: '', staff: '' };
  returnHistory.value = [];
  displayedErrors.value.clear();
  isNegativeRefundToastShown.value = false;
  store.$patch({ hoaDonDetail: {} });
};

// Reset return form
const resetForm = () => {
  returnForm.value = { reason: null, note: '', staff: returnForm.value.staff };
  returnableProducts.value = [];
  updateReturnTotal();
  isNegativeRefundToastShown.value = false;
};

// Open modal to input return quantity
const openQuantityModal = (record) => {
  if (record.unit_price == null || record.so_luong == null || record.so_luong <= 0) {
    console.error(`Sản phẩm "${record.ten_san_pham || 'N/A'}" (ID: ${record.id_chi_tiet_san_pham}) không có đơn giá hoặc số lượng hợp lệ:`, record);
    if (!isSearchToastShown) {
      isSearchToastShown = true;
      toast.error(`Sản phẩm "${record.ten_san_pham || 'N/A'}" không có đơn giá hoặc số lượng hợp lệ!`, {
        onClose: () => { isSearchToastShown = false; }
      });
    }
    return;
  }
  selectedProduct.value = record;
  maxQuantity.value = record.so_luong || 0;
  selectedQuantity.value = 1;
  quantityModalVisible.value = true;
};

// Confirm return quantity
const confirmQuantity = () => {
  if (!selectedQuantity.value || selectedQuantity.value < 1 || selectedQuantity.value > maxQuantity.value) {
    if (!isSearchToastShown) {
      isSearchToastShown = true;
      toast.error('Vui lòng nhập số lượng hợp lệ!', {
        onClose: () => { isSearchToastShown = false; }
      });
    }
    return;
  }
  returnableProducts.value.push({
    ...selectedProduct.value,
    return_quantity: selectedQuantity.value,
    unit_price: selectedProduct.value.unit_price || 0
  });
  quantityModalVisible.value = false;
  selectedQuantity.value = null;
  selectedProduct.value = null;
  updateReturnTotal();
};

// Cancel quantity input
const cancelQuantity = () => {
  quantityModalVisible.value = false;
  selectedQuantity.value = null;
  selectedProduct.value = null;
};

// Remove product from return list
const removeFromReturn = (record) => {
  returnableProducts.value = returnableProducts.value.filter(p => p.id_chi_tiet_san_pham !== record.id_chi_tiet_san_pham);
  updateReturnTotal();
};

// Trigger recomputation of return total
const updateReturnTotal = () => {
  // Handled by computed returnTotal and watchers
};

// Print return invoice
const printReturnInvoice = async () => {
  const doc = new jsPDF();
  doc.setFont("Helvetica");

  // Thêm logo
  const logoWidth = 30;
  const logoHeight = 20;
  const pageWidth = doc.internal.pageSize.getWidth();
  const logoX = (pageWidth - logoWidth) / 2;
  try {
    doc.addImage(logo, 'PNG', logoX, 10, logoWidth, logoHeight);
  } catch (e) {
    console.warn('Không thể thêm logo:', e);
  }

  // Tiêu đề
  doc.setFontSize(18);
  doc.setFont("Roboto", "bold");
  doc.text("HÓA ĐƠN TRẢ HÀNG", 105, 50, { align: "center" });

  doc.setFontSize(16);
  doc.setFont("Roboto", "bold");
  doc.text("G&B SPORTS", 105, 60, { align: "center" });

  doc.setFontSize(10);
  doc.setFont("Roboto", "normal");
  doc.text("Địa chỉ: Phương Canh, Nam Từ Liêm, Hà Nội", 105, 68, { align: "center" });
  doc.text("Điện thoại: 0397572262", 105, 74, { align: "center" });

  doc.setLineWidth(0.5);
  doc.line(20, 78, 190, 78);

  // Thông tin hóa đơn
  doc.setFontSize(12);
  doc.setFont("Roboto", "normal");
  doc.text(`Mã hóa đơn: ${orderDetail.value.ma_hoa_don || 'N/A'}`, 20, 86);
  doc.text(`Ngày trả: ${formatDateTime(new Date())}`, 20, 94);
  doc.text(`Tên khách hàng: ${orderDetail.value.ho_ten || 'Khách lẻ'}`, 20, 102);
  doc.text(`Lý do trả hàng: ${returnForm.value.reason || 'N/A'}`, 20, 110);

  let y = 118;
  y += 8;

  // Bảng sản phẩm đã mua
  doc.setFontSize(12);
  doc.setFont("Roboto", "bold");
  doc.text("Thông tin sản phẩm đã mua", 20, y);
  doc.text("Số lượng", 100, y, { align: "center" });
  doc.text("Đơn giá", 130, y, { align: "center" });
  doc.text("Thành tiền", 170, y, { align: "center" });
  y += 2;
  doc.setLineWidth(0.2);
  doc.line(20, y, 190, y);
  y += 6;

  doc.setFontSize(10);
  doc.setFont("Roboto", "normal");
  orderProducts.value.forEach((item, index) => {
    const productName = `${index + 1}. ${item.ten_san_pham || 'N/A'} (Màu: ${item.ten_mau_sac || 'N/A'} - Size: ${item.kich_thuoc || 'N/A'})`;
    const productLines = doc.splitTextToSize(productName, 80);
    doc.text(productLines, 20, y);
    doc.text(`${item.so_luong}`, 100, y, { align: "center" });
    doc.text(`${formatCurrency(item.unit_price)} VNĐ`, 130, y, { align: "center" });
    doc.text(`${formatCurrency(item.so_luong * item.unit_price)} VNĐ`, 170, y, { align: "center" });
    y += productLines.length * 6 + 4;
  });

  doc.setLineWidth(0.2);
  doc.line(20, y, 190, y);
  y += 10;

  // Bảng sản phẩm hoàn trả
  doc.setFontSize(12);
  doc.setFont("Roboto", "bold");
  doc.text("Thông tin sản phẩm hoàn trả", 20, y);
  doc.setFont("Roboto", "bold");
  doc.text("Số lượng", 100, y, { align: "center" });
  doc.text("Đơn giá", 130, y, { align: "center" });
  doc.text("Tổng tiền", 170, y, { align: "center" });
  y += 2;
  doc.setLineWidth(0.2);
  doc.line(20, y, 190, y);
  y += 6;

  doc.setFontSize(10);
  doc.setFont("Roboto", "normal");
  returnableProducts.value
    .filter(item => item.return_quantity > 0)
    .forEach((item, index) => {
      const productName = `${index + 1}. ${item.ten_san_pham} (Màu: ${item.ten_mau_sac} - Size: ${item.kich_thuoc})`;
      const productLines = doc.splitTextToSize(productName, 80);
      doc.text(productLines, 20, y);
      doc.text(`${item.return_quantity}`, 100, y, { align: "center" });
      doc.text(`${formatCurrency(item.unit_price)} VNĐ`, 130, y, { align: "center" });
      const thanhTien = item.return_quantity * item.unit_price;
      doc.text(`${formatCurrency(thanhTien)} VNĐ`, 170, y, { align: "center" });
      y += productLines.length * 6 + 4;
    });

  doc.setLineWidth(0.2);
  doc.line(20, y, 190, y);
  y += 10;

  // Tổng tiền
  doc.setFontSize(12);
  doc.setFont("Roboto", "normal");
  doc.text(`Tổng tiền hàng: ${formatCurrency(store.hoaDonDetail.tong_tien_truoc_giam)} VNĐ`, 20, y);
  y += 6;
  const giamGia = (store.hoaDonDetail.tong_tien_truoc_giam || 0) +
                  (store.hoaDonDetail.phi_van_chuyen || 0) -
                  (store.hoaDonDetail.tong_tien_sau_giam || 0);
  doc.text(`Giảm giá: ${formatCurrency(giamGia)} VNĐ`, 20, y);
  y += 6;
  doc.text(`Phí vận chuyển: ${formatCurrency(store.hoaDonDetail.phi_van_chuyen || 0)} VNĐ`, 20, y);
  y += 6;
  doc.setFont("Roboto", "bold");
  doc.text(`Tổng tiền khách đã trả: ${formatCurrency(store.hoaDonDetail.tong_tien_sau_giam || 0)} VNĐ`, 20, y);
  y += 6;
  doc.text(`Tổng tiền hoàn trả lại khách: ${formatCurrency(totalRefundForDB.value)} VNĐ`, 20, y);

  y += 10;
  doc.setFontSize(10);
  doc.setFont("Roboto", "normal");
  doc.text("Cảm ơn Quý Khách, hẹn gặp lại!", 105, y, { align: "center" });

  doc.save(`HoaDonTraHang_${orderDetail.value.ma_hoa_don}.pdf`);
};

// Handle print confirmation
const confirmPrint = (shouldPrint) => {
  showPrintConfirm.value = false;
  if (shouldPrint) {
    printReturnInvoice();
  }
  resetSearch();
};

// Submit return request
const submitReturn = debounce(async () => {
  if (!returnForm.value.reason) {
    if (!isSubmitToastShown) {
      isSubmitToastShown = true;
      toast.error('Vui lòng chọn lý do trả hàng!', {
        onClose: () => { isSubmitToastShown = false; }
      });
    }
    return;
  }
  if (totalRefundForDB.value === 0) {
    if (!isSubmitToastShown) {
      isSubmitToastShown = true;
      toast.error('Tổng tiền hoàn không hợp lệ!', {
        onClose: () => { isSubmitToastShown = false; }
      });
    }
    return;
  }
  if (!returnForm.value.staff) {
    if (!isSubmitToastShown) {
      isSubmitToastShown = true;
      toast.error('Vui lòng cung cấp thông tin nhân viên xử lý!', {
        onClose: () => { isSubmitToastShown = false; }
      });
    }
    return;
  }

  if (submitting.value) return;
  submitting.value = true;

  try {
    const returnData = {
      ma_hoa_don: orderDetail.value.ma_hoa_don,
      products: returnableProducts.value
        .filter(p => p.return_quantity > 0)
        .map(p => ({
          id_chi_tiet_san_pham: p.id_chi_tiet_san_pham,
          so_luong: p.return_quantity,
          refund_amount: calculateRefundForProduct(p)
        })),
      reason: returnForm.value.reason,
      note: returnForm.value.note,
      staff: returnForm.value.staff,
      total_refunded: totalRefundForDB.value
    };

    console.log('Submitting return data:', returnData);
    const response = await store.processReturn(returnData);
    console.log('Backend response:', response);

    if (response.success) {
      returnStatus.value = 'Yêu cầu trả hàng';
      returnHistory.value.push({
        trang_thai: 'Yêu cầu trả hàng',
        ngay_tra: new Date().toISOString(),
        ly_do: returnForm.value.reason,
        ghi_chu: returnForm.value.note,
        nhan_vien: returnForm.value.staff,
        tong_tien_hoan: totalRefundForDB.value
      });
      if (!isSubmitToastShown) {
        isSubmitToastShown = true;
        toast.success('Trả hàng thành công!', {
          onClose: () => { isSubmitToastShown = false; }
        });
      }
      showPrintConfirm.value = true;
    } else {
      const errorMessage = response.thong_bao || 'Xử lý trả hàng thất bại!';
      console.warn('Backend error:', errorMessage);
      if (!isSubmitToastShown && !displayedErrors.value.has(errorMessage)) {
        isSubmitToastShown = true;
        displayedErrors.value.add(errorMessage);
        toast.error(errorMessage, {
          onClose: () => {
            isSubmitToastShown = false;
            displayedErrors.value.delete(errorMessage);
          }
        });
      }
      return;
    }
  } catch (error) {
    console.error('Exception caught in submitReturn:', error);
    const errorMessage = error.response?.data?.thong_bao || error.message || 'Có lỗi xảy ra khi xử lý trả hàng!';
    console.warn('Error message:', errorMessage);
    if (!isSubmitToastShown && !displayedErrors.value.has(errorMessage)) {
      isSubmitToastShown = true;
      displayedErrors.value.add(errorMessage);
      toast.error(errorMessage, {
        onClose: () => {
          isSubmitToastShown = false;
          displayedErrors.value.delete(errorMessage);
        }
      });
    }
  } finally {
    submitting.value = false;
  }
}, 300);

// Get date for a return status
const getReturnStatusDate = (status) => {
  const statusHistory = returnHistory.value.find(h => h.trang_thai === status);
  return statusHistory ? statusHistory.ngay_tra || statusHistory.ngay_chuyen : null;
};




// Hiển thị QR scanner
const showQrScanner = () => {
  qrScannerVisible.value = true;
  nextTick(() => {
    if (!qrReader.value) {
      console.error('QR reader element not found during initialization');
      toast.error('Không thể khởi tạo máy quét QR. Vui lòng thử lại!');
      qrScannerVisible.value = false;
      return;
    }
    console.log('QR reader element available:', qrReader.value); // Debug log
    initQrScanner();
  });
};

// Khởi tạo máy quét QR
const initQrScanner = () => {
  if (!qrReader.value) {
    console.error('QR reader element not found in initQrScanner');
    toast.error('Không thể khởi tạo máy quét QR. Vui lòng thử lại!');
    qrScannerVisible.value = false;
    return;
  }

  console.log('Initializing QR scanner on element:', qrReader.value); // Debug log
  try {
    html5QrCode = new Html5Qrcode('qr-reader');
    const qrCodeSuccessCallback = async (decodedText, decodedResult) => {
      if (isProcessing.value) return;
      isProcessing.value = true;
      qrScanResult.value = decodedText;
      console.log('QR code scanned:', decodedText); // Debug log
      stopQrScanner();
      await handleQrResult(decodedText);
      isProcessing.value = false;
    };
    const qrCodeErrorCallback = (error) => {
      console.warn(`QR scan error: ${error}`);
    };

    const config = { fps: 10, qrbox: { width: 250, height: 250 } };
    html5QrCode.start(
      { facingMode: 'environment' },
      config,
      qrCodeSuccessCallback,
      qrCodeErrorCallback
    ).catch(err => {
      toast.error('Không thể truy cập camera. Vui lòng kiểm tra quyền truy cập!');
      console.error('QR Scanner start error:', err);
      qrScannerVisible.value = false;
    });
  } catch (err) {
    console.error('Error initializing Html5Qrcode:', err);
    toast.error('Không thể khởi tạo máy quét QR. Vui lòng thử lại!');
    qrScannerVisible.value = false;
  }
};

// Xử lý kết quả quét QR
const handleQrResult = async (qrData) => {
  try {
    if (!qrData || !qrData.trim()) {
      toast.error('Mã QR không hợp lệ!');
      return;
    }

    let maHoaDon = qrData.trim();
    // Handle different QR code formats
    try {
      // Case 1: QR code is JSON (e.g., {"ma_hoa_don": "HDISWGOJ"})
      const parsedJson = JSON.parse(qrData);
      if (parsedJson.ma_hoa_don) {
        maHoaDon = parsedJson.ma_hoa_don.trim();
      }
    } catch (e) {
      // Not JSON, try URL or plain text
      try {
        // Case 2: QR code is a URL (e.g., "https://example.com/invoice?ma_hoa_don=HDISWGOJ")
        const url = new URL(qrData);
        const urlMaHoaDon = url.searchParams.get('ma_hoa_don');
        if (urlMaHoaDon) {
          maHoaDon = urlMaHoaDon.trim();
        }
      } catch (e) {
        // Case 3: QR code is plain text (e.g., "HDISWGOJ")
        // Already set as maHoaDon
      }
    }

    if (!maHoaDon) {
      toast.error('Không tìm thấy mã hóa đơn trong mã QR!');
      return;
    }

    console.log('Extracted ma_hoa_don:', maHoaDon); // Debug log
    orderCode.value = maHoaDon;
    await searchOrder();
  } catch (error) {
    console.error('Lỗi khi xử lý mã QR:', error);
    toast.error('Có lỗi xảy ra khi xử lý mã QR!');
  }
};

// Dừng máy quét QR
const stopQrScanner = () => {
  if (html5QrCode && html5QrCode.isScanning) {
    html5QrCode.stop().then(() => {
      html5QrCode.clear();
      html5QrCode = null;
      console.log('QR scanner stopped and cleared'); // Debug log
    }).catch(err => {
      console.error('Lỗi khi dừng QR scanner:', err);
    });
  }
  qrScannerVisible.value = false;
  qrScanResult.value = '';
};

// Initialize staff name on mount and cleanup on unmount
onMounted(() => {
  returnForm.value.staff = store.userDetails?.tenNhanVien || store.userInfo?.ten_dang_nhap || 'N/A';
  loading.value = false;
});

onUnmounted(() => {
  isSearchToastShown = false;
  isSubmitToastShown = false;
  isNegativeRefundToastShown.value = false;
  displayedErrors.value.clear();
  stopQrScanner();
});
</script>
<style scoped>
/* QR Scanner container styling */
.qr-scanner-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.qr-scanner-content {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  max-width: 600px;
  width: 90%;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.qr-scanner-content h2 {
  margin-bottom: 20px;
  font-size: 20px;
  color: #2b3940;
}

.qr-reader {
  width: 100%;
  max-width: 500px;
  margin: 0 auto 20px;
  min-height: 250px; /* Ensure space for scanner */
}

@media (max-width: 576px) {
  .qr-scanner-content {
    width: 95%;
    padding: 15px;
  }

  .qr-reader {
    max-width: 100%;
  }
}
.main-content {
  padding: 20px;
  background-color: #f7f9fc;
  min-height: 100vh;
}

/* Header styling */
.tracking-header {
  text-align: center;
  margin-bottom: 40px;
  animation: slideDown 0.8s ease-in-out;
}

.tracking-header h1 {
  font-size: 36px;
  font-weight: 700;
  color: #2b3940;
  marginilhar-bottom: 15px;
  position: relative;
  display: inline-block;
}

.tracking-header h1::after {
  content: '';
  position: absolute;
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #3f6ad8, #25b0ed);
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  border-radius: 10px;
}

.tracking-description {
  font-size: 17px;
  color: #6c757d;
  max-width: 700px;
  margin: 20px auto 0;
  line-height: 1.6;
}

/* Search box styling */
.tracking-search {
  max-width: 600px;
  margin: 0 auto 40px;
  padding: 30px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.07);
  transform: translateY(0);
  transition: all 0.3s ease;
  animation: fadeInUp 0.8s ease-in-out;
}

.tracking-search:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

/* Not found state */
.not-found {
  text-align: center;
  padding: 60px 20px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.07);
  margin-bottom: 40px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  animation: fadeIn 0.8s ease-in-out;
}

.not-found-icon {
  font-size: 80px;
  color: #d9d9d9;
  margin-bottom: 20px;
  animation: pulse 2s infinite;
}

.not-found h2 {
  font-size: 28px;
  color: #2b3940;
  margin-bottom: 15px;
}

.not-found p {
  color: #6c757d;
  max-width: 600px;
  margin: 0 auto 10px;
  font-size: 16px;
  line-height: 1.6;
}

.not-found .ant-btn {
  margin-top: 20px;
  padding: 10px 25px;
  height: auto;
  border-radius: 50px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.not-found .ant-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

/* Order details styling */
.order-details {
  background-color: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 20px;
}

.info-box {
  background-color: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.info-box h5 {
  margin-top: 0;
  color: #1f1f1f;
  font-weight: 500;
}

.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  margin-right: 10px;
}

.total-section {
  margin-top: 20px;
  text-align: right;
}

.text-center {
  text-align: center;
}

.text-right {
  text-align: right;
}

.order-status-timeline {
  position: relative;
  margin: 40px 0 60px;
  padding: 0 20px;
  width: 100%;
}

.timeline-track {
  position: absolute;
  top: 40px;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(90deg, #f5f5f5, #f0f0f0);
  border-radius: 3px;
  z-index: 1;
}

.timeline-steps {
  display: flex;
  justify-content: space-between;
  position: relative;
  z-index: 2;
}

.timeline-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  min-width: 120px;
  margin: 0 5px;
  flex: 1;
}

.timeline-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 15px;
  transition: all 0.4s cubic-bezier(0.215, 0.61, 0.355, 1);
  position: relative;
  border: 3px solid #e8e8e8;
  z-index: 2;
}

.timeline-icon i {
  font-size: 32px;
  color: #b0b0b0;
  transition: all 0.4s ease;
}

.timeline-content {
  text-align: center;
  max-width: 150px;
  transition: all 0.3s ease;
}

.timeline-content h4 {
  margin: 0 0 5px;
  font-size: 16px;
  font-weight: 600;
  color: #666;
  transition: all 0.3s ease;
}

.timeline-content p {
  margin: 0;
  font-size: 13px;
  color: #888;
}

.timeline-step.active .timeline-icon {
  border-color: #1890ff;
  box-shadow: 0 8px 16px rgba(24, 144, 255, 0.3);
  transform: translateY(-10px) scale(1.05);
  animation: pulse-blue 2s infinite;
}

.timeline-step.active .timeline-icon i {
  color: #1890ff;
}

.timeline-step.active .timeline-content h4 {
  color: #1890ff;
  font-weight: 700;
  font-size: 18px;
}

.timeline-step.completed .timeline-icon {
  border-color: #52c41a;
  background-color: #f6ffed;
}

.timeline-step.completed .timeline-icon i {
  color: #52c41a;
}

.timeline-step.completed .timeline-content h4 {
  color: #52c41a;
}

.timeline-step::before {
  content: '';
  position: absolute;
  top: 40px;
  width: 100%;
  height: 6px;
  background-color: #e8e8e8;
  z-index: 1;
}

.timeline-step.completed::before,
.timeline-step.active::before {
  background: linear-gradient(90deg, #52c41a, #1890ff);
}

.timeline-step:first-child::before {
  left: 50%;
  width: 50%;
}

.timeline-step:last-child::before {
  width: 50%;
  right: 50%;
}

/* Animations */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(63, 106, 216, 0.4); }
  70% { box-shadow: 0 0 0 15px rgba(63, 106, 216, 0); }
  100% { box-shadow: 0 0 0 0 rgba(63, 106, 216, 0); }
}

@keyframes pulse-blue {
  0% { box-shadow: 0 0 0 0 rgba(24, 144, 255, 0.5); }
  70% { box-shadow: 0 0 0 15px rgba(24, 144, 255, 0); }
  100% { box-shadow: 0 0 0 0 rgba(24, 144, 255, 0); }
}

@media (max-width: 992px) {
  .timeline-step {
      min-width: 80px;
  }
  .timeline-icon {
      width: 60px;
      height: 60px;
  }
  .timeline-icon i {
      font-size: 26px;
  }
  .timeline-content h4 {
      font-size: 14px;
  }
}

@media (max-width: 767px) {
  .timeline-steps {
      flex-wrap: nowrap;
      overflow-x: auto;
      padding-bottom: 20px;
      justify-content: flex-start;
  }
  .timeline-step {
      min-width: 140px;
      flex: 0 0 auto;
  }
  .timeline-track {
      height: 4px;
  }
}

@media (max-width: 576px) {
  .tracking-header h1 {
      font-size: 30px;
  }
  .tracking-search {
      padding: 20px;
  }
  .not-found {
      padding: 40px 15px;
  }
  .not-found-icon {
      font-size: 60px;
  }
  .not-found h2 {
      font-size: 24px;
  }
}
</style>