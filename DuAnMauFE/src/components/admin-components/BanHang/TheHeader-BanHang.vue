<template>
    <div class="header-container">
        <!-- Search Combo Box -->
        <div class="search-section">
            <a-dropdown v-model:open="dropdownVisible" :trigger="['click']" overlayClassName="product-dropdown">
                <a-input-search v-model:value="searchQuery" placeholder="Tìm kiếm sản phẩm theo tên..."
                    @search="performSearch" style="width: 300px">
                    <template #enterButton>
                        <search-outlined />
                    </template>
                </a-input-search>

                <template #overlay>
                    <div class="dropdown-content-custom">
                        <div v-if="filteredProducts.length === 0 && searchQuery.length > 0" class="empty-result">
                            Không tìm thấy sản phẩm phù hợp.
                        </div>
                        <div v-if="filteredProducts.length > 0">
                            <div v-for="(product) in filteredProducts" :key="product.id" class="product-option"
                                @click="handleDropdownClick(product)">

                                <img :src="product.hinh_anh || 'default-product.png'" alt="Product"
                                    class="product-image" />
                                <div class="product-info-split">
                                    <div class="info-left">
                                        <div class="product-name">{{ product.ten_san_pham }}</div>
                                        <div class="product-details">
                                            <span>Kích thước: {{ product.gia_tri }}</span>
                                            <span>Màu sắc: {{ product.ten_mau }}</span>
                                        </div>
                                    </div>
                                    <div class="info-right">
                                        <div class="product-price-container">
                                            <!-- Nếu có khuyến mãi: hiển thị giá gốc gạch ngang + giá sau giảm -->
                                            <template v-if="product.giaGoc && product.gia_ban < product.giaGoc">
                                                <div class="price-with-discount">
                                                    <span class="original-price">{{ formatCurrency(product.giaGoc) }} VNĐ</span>
                                                    <span class="discount-badge">SALE</span>
                                                </div>
                                                <div class="current-price">{{ formatCurrency(product.gia_ban) }} VNĐ</div>
                                            </template>
                                            <!-- Nếu không có khuyến mãi: chỉ hiển thị giá bình thường -->
                                            <template v-else>
                                                <div class="product-price">{{ formatCurrency(product.gia_ban) }} VNĐ</div>
                                            </template>
                                        </div>
                                        <div class="product-stock">
                                            Tồn kho: <span :class="product.so_luong > 5 ? 'in-stock' : 'low-stock'">{{
                                                product.so_luong }}</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
            </a-dropdown>
            <!-- Thêm nút QR Code bên ngoài kính lúp -->
            <a-tooltip title="Quét mã QR">
                <a-qrcode error-level="H" :value="qrValue" :size="70" :icon="logo" :iconSize="20" @click="showQrScanner"
                    style="cursor: pointer; margin-left: 10px;" />
            </a-tooltip>
        </div>

        <!-- Thêm modal cho quét QR -->
        <a-modal v-model:open="qrScannerVisible" title="Quét mã QR sản phẩm" @cancel="stopQrScanner" :footer="null">
            <div id="qr-reader" style="width: 100%;"></div>
        </a-modal>

        <!-- Invoice Tabs with Suspended Dropdown -->
        <div class="invoice-tabs" style="display: flex; align-items: center;">
            <a-tabs v-model:activeKey="activeKey" type="editable-card" @edit="onEdit" style="flex: 1;">
                <a-tab-pane v-for="pane in activeInvoices" :key="pane.key" :tab="pane.title" :closable="pane.closable">
                    {{ pane.content }}
                </a-tab-pane>
            </a-tabs>
            
            <!-- Dropdown cho hóa đơn treo - ngay sau dấu + -->
            <a-dropdown v-if="suspendedInvoices.length > 0" :trigger="['click']" placement="bottomRight">
                <a-button type="dashed" style="margin-left: 8px; white-space: nowrap;">
                    <template #icon><more-outlined /></template>
                    Hóa đơn treo ({{ suspendedInvoices.length }})
                </a-button>
                <template #overlay>
                    <a-menu>
                        <a-menu-item 
                            v-for="(invoice, index) in suspendedInvoices" 
                            :key="invoice.key"
                            @click="activateSuspendedInvoice(invoice.hd.id_hoa_don)"
                        >
                            <div style="display: flex; justify-content: space-between; align-items: center; min-width: 200px;">
                                <span>
                                    <strong>{{ invoice.title }}</strong> - {{ invoice.hd.ma_hoa_don }}
                                </span>
                                <a-badge 
                                    :count="`${getRemainingMinutes(invoice.hd.id_hoa_don)}p`" 
                                    :number-style="{ 
                                        backgroundColor: getRemainingMinutes(invoice.hd.id_hoa_don) <= 5 ? '#ff4d4f' : '#faad14'
                                    }"
                                />
                            </div>
                        </a-menu-item>
                    </a-menu>
                </template>
            </a-dropdown>
        </div>


        <!-- Action Buttons -->
        <div class="action-buttons">
            <!-- <a-tooltip title="Tra cứu đơn hàng">
                <a-button type="primary" shape="circle" class="action-btn" @click="changeRoute('/admin/quanlyhoadon')">
                    <template #icon> <file-search-outlined /></template>
                </a-button>
            </a-tooltip>
            <a-tooltip title="Trả hàng">
                <a-button type="primary" shape="circle" class="action-btn" @click="changeRoute('/admin/traHang')">
                    <template #icon><rollback-outlined /></template>
                </a-button>
            </a-tooltip>
            <a-tooltip title="Báo cáo thống kê">
                <a-button type="primary" shape="circle" class="action-btn" @click="changeRoute('/admin/')">
                    <template #icon><bar-chart-outlined /></template>
                </a-button>
            </a-tooltip> -->
        </div>

    </div>
    <div class="text">
        <div class="row ">
            <div class="col-8 text-center">
                <div class="table-responsive mt-4" style="max-height: 350px; height: 350px; overflow-y: auto;">
                    <table class="table table-hover">
                        <thead class="sticky-top bg-white" style="top: 0; z-index: 1;">
                            <tr>
                                <th scope="col">#</th>
                                <th scope="col">Ảnh</th>
                                <th scope="col">Tên sản phẩm</th>
                                <th scope="col">Số lượng</th>
                                <th scope="col">Giá bán</th>
                                <th scope="col">Tổng tiền</th>
                                <th scope="col">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-if="!activeTabData || currentInvoiceItems.length === 0">
                                <td colspan="7" class="text-center" style="padding: 20px;">
                                    {{ !activeTabData ? 'Vui lòng chọn hoặc tạo hóa đơn.' : 'Chưa có sản phẩm nào.' }}
                                </td>
                            </tr>
                            <tr v-for="(item, index) in currentInvoiceItems" :key="item.id_chi_tiet_san_pham">
                                <td>{{ index + 1 }}</td>
                                <td>
                                    <img style="width: 50px; height: 50px;"
                                        :src="item.hinh_anh || 'default-product.png'" alt="Item"
                                        class="invoice-item-image" />
                                </td>
                                <td>
                                    {{ item.ten_san_pham }} <br />
                                    <small>(Màu: {{ item.mau_sac }} - Size: {{ item.kich_thuoc }})</small>
                                </td>
                                <td>
                                    <a-space direction="vertical">
                                        <a-input-number v-model:value="item.so_luong" :min="1"
                                            :max="item.so_luong_ton_goc" @change="updateItemTotal(item)"
                                            style="width: 80px;" />

                                    </a-space>
                                </td>
                                <td>{{ formatCurrency(item.gia_ban) }} đ</td>
                                <td>{{ formatCurrency(item.gia_ban * item.so_luong) }} đ</td>
                                <td>
                                    <a-button type="danger" shape="circle" size="small"
                                        @click="removeFromBill(item.id_chi_tiet_san_pham)">
                                        <template #icon><delete-outlined /></template>
                                    </a-button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div v-if="ptnh === 'Giao hàng'">
                    <FormKhachHangBH :triggerUpdate="triggerUpdate" />
                </div>
            </div>
            <div class="col-4">
                <form v-if="activeTabData && activeTabData.hd" @submit.prevent="handlePayment">
                    <input type="hidden" v-model="activeTabData.hd.id_hoa_don">
                    <div class="mb-3">
                        <label class="form-label">Mã hóa đơn: {{ activeTabData.hd.ma_hoa_don }}</label>
                    </div>
                   
                    <div class="mb-3">
                        <div class="row align-items-center">
                            <label for="idKhachHang" class="form-label col-6">
                                Tên khách hàng: 
                                {{activeTabData.hd.ten_khach_hang||activeTabData.hd.ho_ten||'Khách lẻ'}}
                            </label>
                            <div class="col 4">
                                <a-button type="primary" @click="showModal">Chọn khách hàng</a-button>
                            </div>
                        </div>
                        <div class="row mb-3">


                            <a-modal v-model:open="open" title="Danh sách khách hàng" @ok="handleOk" width="1000px">
                                <template #footer>
                                    <a-button key="back" @click="handleCancel">Quay lại</a-button>
                                    <a-button key="submit" type="primary" :loading="loading" @click="handleOk">Xác
                                        nhận</a-button>
                                </template>
                                <!-- Thanh tìm kiếm -->
                                <div class="mb-4">
                                    <a-input v-model:value="searchQueryKH" style="width: 350px; height: 40px;"
                                        placeholder="     Tìm kiếm theo tên hoặc số điện thoại"
                                        @input="handleSearch" />
                                </div>
                                <div v-if="filteredKhachHang.length === 0" class="text-center py-4">
                                    <a-empty :image="simpleImage" />
                                </div>
                                <div v-else>
                                    <div class="table-responsive mt-4" ref="scrollContainer"
                                        style="max-height: 400px; overflow-y: auto" @scroll="handleScroll">
                                        <table class="table table-hover">
                                            <thead>
                                                <tr>
                                                    <th scope="col">STT</th>
                                                    <th scope="col">Tên khách hàng</th>
                                                    <th scope="col">Giới tính</th>
                                                    <th scope="col">Số điện thoại</th>
                                                    <th scope="col">Địa chỉ</th>
                                                    <th scope="col">Thao tác</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <tr v-for="(khachHang, index) in filteredKhachHang"
                                                    :key="khachHang.idKhachHang">
                                                    <td>{{ index + 1 }}</td>
                                                    <td>{{ khachHang.tenKhachHang }}</td>
                                                    <td>{{ khachHang.gioiTinh ? "Nam" : "Nữ" }}</td>
                                                    <td>{{ khachHang.soDienThoai }}</td>
                                                    <td>{{ khachHang.diaChi }}</td>
                                                    <td>
                                                        <a-button size="small" type="link"
                                                            @click="chonKhachHang(khachHang)">Chọn</a-button>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </a-modal>
                        </div>


                    </div>
                    <div class="mb-3">
                        <label class="form-label d-block mb-2">Phương thức nhận hàng</label>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" :name="'phuongThucNhanHang_' + activeKey"
                                :id="'nhanTaiCuahang_' + activeKey" value="Nhận tại cửa hàng"
                                v-model="activeTabData.hd.phuong_thuc_nhan_hang" @change="handlePhuongThucChange" />
                            <label class="form-check-label" :for="'nhanTaiCuahang_' + activeKey">Nhận tại cửa
                                hàng</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" :name="'phuongThucNhanHang_' + activeKey"
                                :id="'giaoHang_' + activeKey" value="Giao hàng"
                                v-model="activeTabData.hd.phuong_thuc_nhan_hang" @change="handlePhuongThucChange" />
                            <label class="form-check-label" :for="'giaoHang_' + activeKey">Giao hàng</label>
                        </div>
                        <div v-if="activeTabData.hd.phuong_thuc_nhan_hang === 'Giao hàng'" class="mt-2">
                            <div class="form-label-with-logo">
                                <label class="form-label">Phí vận chuyển (VNĐ)</label>
                                <img src="../../../images/logo/logo_GHTK.png" alt="GHTK Logo" class="ghtk-logo" />
                            </div>
                            <a-input-number v-model:value="activeTabData.hd.phi_van_chuyen" :min="0"
                                :formatter="value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                                :parser="value => value.replace(/\$\s?|(,*)/g, '')" placeholder="Nhập phí vận chuyển"
                                style="width: 100%" />
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Tổng tiền hàng:</label>
                        <input type="text" class="form-control"
                            :value="formatCurrency(fe_tongTienHang) +' '+ 'đ'" disabled>
                    </div>
                    <div class="mb-3" v-if="activeTabData.hd.phuong_thuc_nhan_hang === 'Giao hàng'">
                        <label class="form-label">Phí vận chuyển:</label>
                        <input type="text" class="form-control"
                            :value="formatCurrency(fe_phiVanChuyen) + 'đ'" disabled>
                    </div>
                    <div class="mb-3">
                        <label for="idVoucher" class="form-label">Voucher</label>
                        <select name="idVoucher" id="idVoucher" class="form-select"
                            v-model="activeTabData.hd.id_voucher" @change="updateVoucher(true)">
                            <option :value="null">-- Không dùng voucher --</option>
                             <option v-for="voucher in availableVouchers" :key="voucher.id_voucher" :value="voucher.id_voucher">
                                {{ voucher.ten_voucher }} (Giảm {{ formatCurrency(voucher.so_tien_giam) }} đ)
                            </option>
                        </select>
                    </div>
                    <div class="mb-3" v-if="fe_giamGia > 0">
                        <label class="form-label">Giảm từ Voucher:</label>
                        <input type="text" class="form-control text-success fw-bold"
                            :value="'-' + formatCurrency(fe_giamGia) +' '+ 'đ'" disabled>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Tổng thanh toán:</label>
                        <input type="text" class="form-control fw-bold fs-5"
                            :value="formatCurrency(fe_tongThanhToan) +' '+ 'đ'" disabled>
                    </div>
                    <div class="mb-3">
                        <label class="form-label d-block mb-2">Hình thức thanh toán</label>
                        <div class="payment-methods-grid">
                            <div class="payment-method-option" :class="{ 'active': activeTabData.hd.hinh_thuc_thanh_toan === 'Tiền mặt' }">
                                <input class="form-check-input" type="radio" :name="'hinhThucThanhToan_' + activeKey"
                                    :id="'tienMat_' + activeKey" value="Tiền mặt"
                                    v-model="activeTabData.hd.hinh_thuc_thanh_toan" @change="updateHinhThucThanhToan" />
                                <label class="payment-label" :for="'tienMat_' + activeKey">
                                    <div class="payment-icon">💵</div>
                                    <div class="payment-text">Tiền mặt</div>
                                </label>
                            </div>
                            <div class="payment-method-option" :class="{ 'active': activeTabData.hd.hinh_thuc_thanh_toan === 'PayOS' }">
                                <input class="form-check-input" type="radio" :name="'hinhThucThanhToan_' + activeKey"
                                    :id="'payos_' + activeKey" value="PayOS"
                                    v-model="activeTabData.hd.hinh_thuc_thanh_toan" @change="updateHinhThucThanhToan" />
                                <label class="payment-label" :for="'payos_' + activeKey">
                                    <div class="payment-icon">🏦</div>
                                    <div class="payment-text">PayOS</div>
                                </label>
                            </div>
                            <div class="payment-method-option" :class="{ 'active': activeTabData.hd.hinh_thuc_thanh_toan === 'ZaloPay' }">
                                <input class="form-check-input" type="radio" :name="'hinhThucThanhToan_' + activeKey"
                                    :id="'zalopay_' + activeKey" value="ZaloPay"
                                    v-model="activeTabData.hd.hinh_thuc_thanh_toan" @change="updateHinhThucThanhToan" />
                                <label class="payment-label" :for="'zalopay_' + activeKey">
                                    <div class="payment-icon">⚡</div>
                                    <div class="payment-text">ZaloPay</div>
                                </label>
                            </div>
                        </div>
                        
                        <div v-if="activeTabData.hd.hinh_thuc_thanh_toan === 'Tiền mặt'" class="mt-2">
                            <label class="form-label">Tiền khách đưa (VNĐ)</label>
                            <a-input-number v-model:value="tienKhachDua" :min="0"
                                :formatter="value => `${Number(value).toLocaleString('vi-VN', { style: 'currency', currency: 'VND' })}`"
                                :parser="value => value.replace(/[^\d]/g, '')" placeholder="Nhập số tiền khách đưa"
                                style="width: 100%" />
                            <label class="form-label mt-2">Tiền dư trả khách (VNĐ)</label>
                            <input type="text" class="form-control" :value="formatCurrency(calculatedChange)" disabled>
                        </div>
                    </div>

                    <!-- Nút thanh toán với điều kiện vô hiệu hóa -->
                    <button type="submit" class="btn btn-primary w-100" :disabled="isPaymentDisabled">
                        Thanh toán
                    </button>
                    <!-- Modal 1: Xác nhận thanh toán -->
                    <a-modal 
                        v-model:open="showPaymentConfirm" 
                        :closable="false"
                        :maskClosable="false"
                        width="450px"
                        centered
                    >
                        <template #title>
                            <div style="display: flex; align-items: center; gap: 10px;">
                                <ExclamationCircleOutlined style="color: #faad14; font-size: 24px;" />
                                <span style="font-size: 18px; font-weight: 600;">Xác nhận thanh toán</span>
                            </div>
                        </template>
                        
                        <div style="padding: 20px 0;">
                            <p style="font-size: 15px; margin-bottom: 16px;">
                                Bạn có chắc chắn muốn thanh toán đơn hàng này?
                            </p>
                            <div style="background: #f5f5f5; padding: 16px; border-radius: 8px; margin-top: 16px;">
                                <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                                    <span style="color: #666;">Mã hóa đơn:</span>
                                    <strong>{{ activeTabData?.hd?.ma_hoa_don }}</strong>
                                </div>
                                <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                                    <span style="color: #666;">Tổng tiền:</span>
                                    <strong style="color: #ff6600; font-size: 16px;">{{ formatCurrency(fe_tongThanhToan) }}</strong>
                                </div>
                                <div v-if="activeTabData?.hd?.hinh_thuc_thanh_toan === 'Tiền mặt'" style="display: flex; justify-content: space-between;">
                                    <span style="color: #666;">Tiền trả khách:</span>
                                    <strong style="color: #52c41a;">{{ formatCurrency(calculatedChange) }}</strong>
                                </div>
                            </div>
                        </div>
                        
                        <template #footer>
                            <a-button key="cancel" size="large" @click="cancelPayment" style="height: 40px;">
                                Hủy
                            </a-button>
                            <a-button 
                                key="ok" 
                                type="primary" 
                                size="large"
                                @click="proceedToPayment" 
                                style="height: 40px; background: #ff6600; border-color: #ff6600;"
                            >
                                Xác nhận thanh toán
                            </a-button>
                        </template>
                    </a-modal>

                    <!-- Modal 2: Xác nhận in hóa đơn (sau khi thanh toán) -->
                    <a-modal 
                        v-model:open="showPrintConfirm" 
                        :closable="false"
                        :maskClosable="false"
                        width="450px"
                        centered
                    >
                        <template #title>
                            <div style="display: flex; align-items: center; gap: 10px;">
                                <PrinterOutlined style="color: #1890ff; font-size: 24px;" />
                                <span style="font-size: 18px; font-weight: 600;">In hóa đơn</span>
                            </div>
                        </template>
                        
                        <div style="padding: 20px 0;">
                            <p style="font-size: 15px; margin-bottom: 16px;">
                                Thanh toán thành công! Bạn có muốn in hóa đơn không?
                            </p>
                            <div style="background: #e6f7ff; padding: 16px; border-radius: 8px; border: 1px solid #91d5ff;">
                                <div style="display: flex; align-items: center; gap: 8px; color: #1890ff;">
                                    <CheckCircleOutlined style="font-size: 18px;" />
                                    <span style="font-weight: 500;">Đơn hàng {{ activeTabData?.hd?.ma_hoa_don }} đã được thanh toán</span>
                                </div>
                            </div>
                        </div>
                        
                        <template #footer>
                            <a-button key="cancel" size="large" @click="confirmPrint(false)" style="height: 40px;">
                                Không in
                            </a-button>
                            <a-button 
                                key="ok" 
                                type="primary" 
                                size="large"
                                @click="confirmPrint(true)" 
                                style="height: 40px; background: #52c41a; border-color: #52c41a;"
                            >
                                In hóa đơn
                            </a-button>
                        </template>
                    </a-modal>
                </form>
                <div v-else class="text-center text-muted mt-5">
                    Vui lòng chọn hoặc tạo một hóa đơn.
                </div>
            </div>
        </div>
    </div>

</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, onUnmounted, h } from 'vue';
import {
    SearchOutlined,
    FileSearchOutlined,
    RollbackOutlined,
    BarChartOutlined,
    DeleteOutlined,
    QrcodeOutlined,
    MoreOutlined,
    ExclamationCircleOutlined,
    PrinterOutlined,
    CheckCircleOutlined
} from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';
import { useGbStore } from '@/stores/gbStore';
import { Empty } from 'ant-design-vue';
import jsPDF from 'jspdf';
import logo from '../../../images/logo/LogoM.png';
import '../../../config/fonts/Roboto-normal'
import '../../../config/fonts/Roboto-bold'
import { toast } from 'vue3-toastify';
import { thanhToanService } from '@/services/thanhToan';
import FormKhachHangBH from './formKhachHangBH.vue';
import { useRouter } from 'vue-router';
import { banHangService } from '@/services/banHangService';
import QRCode from 'qrcode';
const router = useRouter();
import { Html5Qrcode } from 'html5-qrcode';
// Thêm state cho quét QR
const qrScannerVisible = ref(false);
const qrScanResult = ref('');
const qrValue = ref('Quét sản phẩm'); // Giá trị mặc định cho QR code
let html5QrCode = null;
let isProcessing = false;
const triggerUpdate = ref(Date.now());



// ✅ ZALOPAY STATE
const showZaloPayModal = ref(false);
const zaloPayQRUrl = ref('');
const zaloPayQRCode = ref(''); // QR code string từ ZaloPay
const isLoadingZaloPay = ref(false);
const paymentStatus = ref(''); // checking, success, failed
let checkPaymentInterval = null;

// Hiển thị modal quét QR
const showQrScanner = () => {
    qrScannerVisible.value = true;
    // Khởi tạo scanner sau khi modal được mở
    setTimeout(() => {
        initQrScanner();
    }, 100);
};

// Khởi tạo máy quét QR
const initQrScanner = () => {
    html5QrCode = new Html5Qrcode("qr-reader");
    const qrCodeSuccessCallback = async (decodedText) => {
        if (isProcessing) return; // Nếu đang xử lý, bỏ qua
        isProcessing = true;
        qrScanResult.value = decodedText;
        stopQrScanner();
        await handleQrResult(decodedText);
        isProcessing = false; // Đặt lại trạng thái sau khi xử lý xong
    };
    const qrCodeErrorCallback = (error) => {
        console.warn(`QR scan error: ${error}`);
    };

    // Cấu hình quét QR
    const config = { fps: 10, qrbox: { width: 250, height: 250 } };
    html5QrCode.start(
        { facingMode: "environment" },
        config,
        qrCodeSuccessCallback,
        qrCodeErrorCallback
    ).catch(err => {
        message.error('Không thể truy cập camera. Vui lòng kiểm tra quyền truy cập!');
        console.error("QR Scanner error:", err);
        qrScannerVisible.value = false;
    });
};

// Xử lý kết quả quét QR
const handleQrResult = async (qrData) => {
    try {
        const product = allProducts.value.find(p => p.id_chi_tiet_san_pham === Number(qrData));

        if (!product) {
            message.error('Không tìm thấy sản phẩm với mã QR này!(Sản phẩm đã ngừng hoạt động)');
            return;
        }

        const currentTab = activeTabData.value;
        if (!currentTab || !currentTab.hd?.id_hoa_don) {
            message.error('Vui lòng chọn hoặc tạo một hóa đơn hợp lệ trước!');
            return;
        }

        // Kiểm tra xem sản phẩm đã có trong hóa đơn chưa
        const existingItem = currentTab.items.value.find(
            item => item.id_chi_tiet_san_pham === product.id_chi_tiet_san_pham
        );

        if (existingItem) {
            // Nếu sản phẩm đã có, tăng số lượng
            const newQuantity = existingItem.so_luong + 1;
            const productInfo = allProducts.value.find(p => p.id_chi_tiet_san_pham === existingItem.id_chi_tiet_san_pham);
            const soLuongTonKho = productInfo ? productInfo.so_luong_ton : 0;

            if (newQuantity > soLuongTonKho + existingItem.so_luong) {
                message.warning(`Số lượng vượt quá tồn kho (${soLuongTonKho})!`);
                return;
            }

            existingItem.so_luong = newQuantity;
            await updateItemTotal(existingItem);
        } else {
            // Nếu chưa có, thêm mới
            await addToBill(product);
        }

    } catch (error) {
        console.error('Lỗi khi xử lý mã QR:', error);
        message.error('Có lỗi xảy ra khi xử lý mã QR!');
    }
};

// Dừng máy quét QR
const stopQrScanner = () => {
    if (html5QrCode) {
        html5QrCode.stop().then(() => {
            html5QrCode.clear();
            html5QrCode = null;
        }).catch(err => {
            console.error("Lỗi khi dừng QR scanner:", err);
        });
    }
    qrScannerVisible.value = false;
    qrScanResult.value = '';
};
const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;
const pageSize = ref(5);
const store = useGbStore();
const scrollContainer = ref(null);

const danhSachKhachHang = computed(() => {
    return store.getAllKhachHangNoPageList.map(khachHang => ({
        ...khachHang,
        diaChi: store.diaChiMap[khachHang.idKhachHang] || 'Chưa có địa chỉ'
    }));
});

const chonKhachHang = async (khachHang) => {
    try {
        Object.assign(activeTabData.value.hd, {
            ten_khach_hang: khachHang.tenKhachHang,
            so_dien_thoai: khachHang.soDienThoai,
            dia_chi: khachHang.diaChi || 'Chưa có địa chỉ',
            id_khach_hang: khachHang.idKhachHang
        });

        await store.addKHHD(
            activeTabData.value.hd.id_hoa_don,
            khachHang.idKhachHang,
            khachHang.diaChi,
            khachHang.tenKhachHang,
            khachHang.soDienThoai
        );

        await store.getAllKhachHangNoPage();

        open.value = false;
        if (!activeTabData.value.hd.isKhachLe) {
            handlePhuongThucChange();
        }
        
        await refreshHoaDon(activeTabData.value.hd.id_hoa_don);

        localStorage.setItem('khachHangBH', JSON.stringify(khachHang));
        localStorage.setItem('chonKH', true);

        message.success(`Đã chọn khách hàng: ${khachHang.tenKhachHang}`);
        triggerUpdate.value = Date.now();
    } catch (error) {
        console.error('Lỗi khi chọn khách hàng:', error);
        message.error('Không thể chọn khách hàng. Vui lòng thử lại!');
    }
};



// --- State cho tìm kiếm và dropdown ---
const dropdownVisible = ref(false);
const searchQuery = ref('');
const searchQueryKH = ref('');
const allProducts = ref([]); // Danh sách TẤT CẢ sản phẩm chi tiết lấy từ API/store


// --- State cho quản lý Tab hóa đơn ---
const panes = ref([]); // Khởi tạo rỗng, sẽ tạo tab đầu tiên trong onMounted
const activeKey = ref('');
const newTabIndex = ref(0); // Chỉ dùng để tạo key duy nhất nếu cần, không dùng cho tiêu đề

// ==================== INVOICE QUEUE MANAGEMENT ====================
const MAX_ACTIVE_INVOICES = 5; // Số hóa đơn hiển thị trên tabs
const MAX_SUSPENDED_INVOICES = 15; // Số hóa đơn treo tối đa
const MAX_TOTAL_INVOICES = MAX_ACTIVE_INVOICES + MAX_SUSPENDED_INVOICES; // Tổng: 20 hóa đơn
const EXPIRY_WARNING_TIME = 5 * 60 * 1000; // 5 phút
const EXPIRY_TIME = 10 * 60 * 1000; // 10 phút

// Map lưu trữ timer data cho mỗi hóa đơn
const invoiceTimers = ref(new Map());
// Structure: Map<invoiceId, { createdAt, warningTimeoutId, expiryTimeoutId, warningShown }>

// Computed: Danh sách hóa đơn chờ (hiển thị trên tabs)
const activeInvoices = computed(() => panes.value.slice(0, MAX_ACTIVE_INVOICES));

// Computed: Danh sách hóa đơn treo (trong dropdown)
const suspendedInvoices = computed(() => panes.value.slice(MAX_ACTIVE_INVOICES));

const loading = ref(false);
const open = ref(false);
const showModal = () => {
    open.value = true;
};
const handleOk = () => {
    loading.value = true;
    setTimeout(() => {
        loading.value = false;
        open.value = false;
    }, 2000);
};
const handleCancel = () => {
    open.value = false;
};
const ptnh = ref('Nhận tại cửa hàng');

const selectedKeys = ref([store.indexMenu]);

const changeRoute = (path) => {
    store.getPath(path);
    store.getRoutePresent(router.path);
    store.getIndex(path);
    selectedKeys.value = store.indexMenu;
    router.push(path);
};



// Computed để lọc danh sách khách hàng
const filteredKhachHang = computed(() => {
    if (!danhSachKhachHang.value || danhSachKhachHang.value.length === 0) {
        return [];
    }
    if (!searchQueryKH.value) {
        return danhSachKhachHang.value;
    }

    const normalizedQuery = normalizeString(searchQueryKH.value);
    return danhSachKhachHang.value.filter(khachHang => {
        const normalizedName = normalizeString(khachHang.tenKhachHang);
        const normalizedPhone = normalizeString(khachHang.soDienThoai);
        return normalizedName.includes(normalizedQuery) || normalizedPhone.includes(normalizedQuery);
    });
});

// Xử lý sự kiện tìm kiếm
const handleSearch = () => {
    // Không cần thêm logic vì filteredKhachHang đã tự động cập nhật qua computed
};


// --- Computed Properties ---
// Lọc sản phẩm cho dropdown tìm kiếm
const filteredProducts = computed(() => {
    console.log('🔍 filteredProducts computed - allProducts:', allProducts.value?.length, 'searchQuery:', searchQuery.value);
    if (!allProducts.value || allProducts.value.length === 0) {
        console.log('⚠️ allProducts is empty!');
        return [];
    }
    if (!searchQuery.value) {
        console.log('✅ Returning all products:', allProducts.value.length);
        return allProducts.value;
    }

    const normalizedQuery = normalizeString(searchQuery.value);
    const filtered = allProducts.value.filter(product => {
        const normalizedProductName = normalizeString(product.ten_san_pham);
        return normalizedProductName.includes(normalizedQuery);
    });
    console.log('✅ Filtered products:', filtered.length);
    return filtered;
});


// Lấy dữ liệu của tab đang active
const activeTabData = computed(() => {
    return panes.value.find(pane => pane.key === activeKey.value);
});

const currentInvoiceItems = computed(() => {
    return activeTabData.value?.items?.value || [];
});

// --- Methods ---
// Định dang tiền tệ
const formatCurrency = (value) => {
    if (!value && value !== 0) return '0';
    return Number(value).toLocaleString('vi-VN');
};

// ==================== TIMER MANAGEMENT FUNCTIONS ====================
// Bắt đầu timer cho hóa đơn suspended
const startInvoiceTimer = (invoiceId, invoiceCode) => {
    console.log(`⏰ Starting timer for invoice ${invoiceCode} (ID: ${invoiceId})`);
    
    const now = Date.now();
    
    // Set timeout cho cảnh báo (5 phút)
    const warningTimeoutId = setTimeout(() => {
        const timerData = invoiceTimers.value.get(invoiceId);
        if (timerData && !timerData.warningShown) {
            message.warning(`Hóa đơn ${invoiceCode} sắp hết hạn (còn 5 phút). Vui lòng thanh toán!`, 10);
            timerData.warningShown = true;
        }
    }, EXPIRY_WARNING_TIME);
    
    // Set timeout cho tự động xóa (10 phút)
    const expiryTimeoutId = setTimeout(async () => {
        await deleteExpiredInvoice(invoiceId, invoiceCode);
    }, EXPIRY_TIME);
    
    // Lưu timer data
    invoiceTimers.value.set(invoiceId, {
        createdAt: now,
        warningTimeoutId,
        expiryTimeoutId,
        warningShown: false
    });
};

// Xóa timer cho hóa đơn
const clearInvoiceTimer = (invoiceId) => {
    const timerData = invoiceTimers.value.get(invoiceId);
    if (timerData) {
        clearTimeout(timerData.warningTimeoutId);
        clearTimeout(timerData.expiryTimeoutId);
        invoiceTimers.value.delete(invoiceId);
        console.log(`⏰ Cleared timer for invoice ID: ${invoiceId}`);
    }
};

// Lấy thời gian còn lại (phút) cho hóa đơn
const getRemainingMinutes = (invoiceId) => {
    const timerData = invoiceTimers.value.get(invoiceId);
    if (!timerData) return null;
    
    const elapsed = Date.now() - timerData.createdAt;
    const remaining = EXPIRY_TIME - elapsed;
    return Math.ceil(remaining / 60000); // Convert to minutes
};

// Xóa hóa đơn hết hạn
const deleteExpiredInvoice = async (invoiceId, invoiceCode) => {
    try {
        console.log(`🗑️ Deleting expired invoice ${invoiceCode}`);
        
        // Xóa hóa đơn qua API
        await store.deleteHoaDon(invoiceId);
        
        // Xóa khỏi panes
        const index = panes.value.findIndex(p => p.hd.id_hoa_don === invoiceId);
        if (index !== -1) {
            panes.value.splice(index, 1);
        }
        
        // Clear timer
        clearInvoiceTimer(invoiceId);
        
        // Thông báo
        message.error(`Hóa đơn ${invoiceCode} đã hết hạn và bị xóa tự động.`, 5);
    } catch (error) {
        console.error('Lỗi khi xóa hóa đơn hết hạn:', error);
    }
};

// Kích hoạt hóa đơn treo (đưa lên active)
const activateSuspendedInvoice = (invoiceId) => {
    const suspendedIndex = panes.value.findIndex(p => p.hd.id_hoa_don === invoiceId);
    if (suspendedIndex === -1 || suspendedIndex < MAX_ACTIVE_INVOICES) return;
    
    // Lấy hóa đơn suspended
    const suspendedInvoice = panes.value[suspendedIndex];
    
    // Xóa khỏi vị trí hiện tại
    panes.value.splice(suspendedIndex, 1);
    
    // Thêm vào đầu danh sách (làm active)
    panes.value.unshift(suspendedInvoice);
    
    // Clear timer vì đã active
    clearInvoiceTimer(invoiceId);
    
    // Set làm active key
    activeKey.value = suspendedInvoice.key;
    
    // Kiểm tra xem có hóa đơn nào mới vào suspended không
    if (panes.value.length > MAX_ACTIVE_INVOICES) {
        const newSuspendedInvoice = panes.value[MAX_ACTIVE_INVOICES];
        startInvoiceTimer(newSuspendedInvoice.hd.id_hoa_don, newSuspendedInvoice.hd.ma_hoa_don);
    }
    
    message.success(`Đã kích hoạt hóa đơn ${suspendedInvoice.hd.ma_hoa_don}`);
};
// Xử lý khi người dùng gõ vào ô tìm kiếm
const normalizeString = (str) => {
    if (!str) return '';
    return str
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .replace(/[^\p{L}\p{N}\s]/gu, '')
        .replace(/\s+/g, ' ')
        .trim();
};

// handleSearchInput đã được xóa vì filteredProducts là computed property tự động





// Xử lý khi nhấn Enter hoặc nút Search
const performSearch = () => {
    if (!dropdownVisible.value && searchQuery.value) {
        dropdownVisible.value = true;
    }
};

const refreshHoaDon = async (idHoaDon) => {
    try {
        await store.getHoaDonByIdHoaDon(idHoaDon);
        const hoaDonInfo = store.getHDBIDHD;

        const currentTab = activeTabData.value;
        if (hoaDonInfo && currentTab && currentTab.hd?.id_hoa_don === idHoaDon) {
            currentTab.hd = {
                ...currentTab.hd,
                ...hoaDonInfo
            };
            ptnh.value = hoaDonInfo.phuong_thuc_nhan_hang
        }
    } catch (error) {
        console.error('Lỗi khi cập nhật thông tin hóa đơn:', error);
        message.error('Không thể làm mới thông tin hóa đơn.');
    }
};



// Thêm sản phẩm vào hóa đơn chi tiết của tab hiện tại
const handleDropdownClick = (product) => {
    if (!dropdownVisible.value) return; // Ngăn nếu dropdown đang ẩn
    addToBill(product);
};

// ✅ Thêm biến chống spam click
let isAdding = false;
let lastClickTime = 0;
const CLICK_DELAY = 500; // ms - thời gian chờ giữa 2 lần click

const addToBill = (product) => {
    const now = Date.now();
    if (isAdding || (now - lastClickTime < CLICK_DELAY)) {
        return;
    }
    lastClickTime = now;
    isAdding = true;

    const currentTab = activeTabData.value;
    if (!currentTab || !currentTab.hd?.id_hoa_don) {
        message.error('Vui lòng chọn hoặc tạo một hóa đơn hợp lệ trước!');
        isAdding = false;
        return;
    }

    if (product.so_luong <= 0) {
        message.warning(`Sản phẩm "${product.ten_san_pham}" đã hết hàng!`);
        isAdding = false;
        return;
    }

    // --- Optimistic UI Update ---
    const existingItem = currentTab.items.value.find(item => item.id_chi_tiet_san_pham === product.id_chi_tiet_san_pham);
    
    if (existingItem) {
        // Chỉ tăng số lượng local
        existingItem.so_luong++;
    } else {
        // Thêm sản phẩm mới vào mảng local
        const newItem = {
            id_hoa_don: currentTab.hd.id_hoa_don,
            id_chi_tiet_san_pham: product.id_chi_tiet_san_pham,
            hinh_anh: product.hinh_anh,
            ten_san_pham: product.ten_san_pham,
            mau_sac: product.ten_mau,
            kich_thuoc: product.gia_tri,
            so_luong: 1,
            gia_ban: product.gia_ban,
            so_luong_ton_goc: product.so_luong - 1, // Giả định giảm tồn kho local
        };
        currentTab.items.value.push(newItem);
    }
    message.success(`Đã thêm "${product.ten_san_pham}"`);
    dropdownVisible.value = false;
    searchQuery.value = '';
    // --- Kết thúc Optimistic UI Update ---

    // --- Gửi yêu cầu lên backend ở chế độ nền ---
    store.themSPHDMoi(currentTab.hd.id_hoa_don, product.id_chi_tiet_san_pham, 1)
        .then(result => {
            if (!result) { throw new Error("Thêm sản phẩm thất bại"); }
            // Cập nhật lại toàn bộ hóa đơn từ backend để đảm bảo đồng bộ 100%
            refreshHoaDon(currentTab.hd.id_hoa_don); 
            store.getAllCTSPKM().then(p => allProducts.value = p); // Tải lại danh sách sản phẩm
        })
        .catch(error => {
            console.error('Lỗi khi thêm sản phẩm (backend):', error);
            message.error('Lỗi: Không thể thêm sản phẩm vào hóa đơn.');
            // --- Hoàn tác lại thay đổi trên UI nếu có lỗi ---
            if (existingItem) {
                existingItem.so_luong--; // Trả lại số lượng
            } else {
                const itemIndex = currentTab.items.value.findIndex(item => item.id_chi_tiet_san_pham === product.id_chi_tiet_san_pham);
                if (itemIndex > -1) {
                    currentTab.items.value.splice(itemIndex, 1);
                }
            }
        })
        .finally(() => {
            isAdding = false;
        });
};


const tienKhachDua = ref(0);

// Tính toán tiền thừa trả khách (calculatedChange) dựa trên tong_tien_sau_giam
const calculatedChange = computed(() => {
    const total = fe_tongThanhToan.value || 0;
    const cash = tienKhachDua.value || 0;
    return cash >= total ? cash - total : 0;
});

const isPaymentDisabled = computed(() => {
    if (currentInvoiceItems.value.length === 0) {
        return true; 
    }
    if (activeTabData.value?.hd?.hinh_thuc_thanh_toan === 'Tiền mặt') {
        const total = fe_tongThanhToan.value || 0;
        const cash = tienKhachDua.value || 0;
        return cash < total;
    }
    return false;
});

// =================================================================
// LOGIC TÍNH TOÁN VÀ VOUCHER TỰ ĐỘNG CỦA FRONTEND
// =================================================================

const userHasManuallyDeselectedVoucher = ref(false);
const availableVouchers = ref([]);

// 1. TÍNH TOÁN CÁC GIÁ TRỊ TỨC THÌ
const fe_tongTienHang = computed(() => {
    if (!currentInvoiceItems.value) return 0;
    return currentInvoiceItems.value.reduce((total, item) => (total + (Number(item.gia_ban) || 0) * (Number(item.so_luong) || 0)), 0);
});

const fe_giamGia = computed(() => {
    const currentTab = activeTabData.value;
    if (!currentTab?.hd?.id_voucher || !availableVouchers.value.length) return 0;
    const selectedVoucher = availableVouchers.value.find(v => v.id_voucher === currentTab.hd.id_voucher);
    if (!selectedVoucher) return 0;

    const subtotal = fe_tongTienHang.value;
    if (subtotal < (Number(selectedVoucher.gia_tri_toi_thieu) || 0)) return 0;

    let discount = 0;
    const giaTriGiam = Number(selectedVoucher.gia_tri_giam) || 0;
    if (selectedVoucher.kieu_giam_gia === 'Phần trăm') {
        discount = subtotal * (giaTriGiam / 100);
        const giaTriToiDa = Number(selectedVoucher.gia_tri_toi_da) || 0;
        if (giaTriToiDa > 0 && discount > giaTriToiDa) discount = giaTriToiDa;
    } else {
        discount = giaTriGiam;
    }
    return Math.min(discount, subtotal);
});

const fe_phiVanChuyen = computed(() => {
    const currentTab = activeTabData.value;
    return (currentTab?.hd?.phuong_thuc_nhan_hang === 'Giao hàng') ? (Number(currentTab.hd.phi_van_chuyen) || 0) : 0;
});

const fe_tongThanhToan = computed(() => {
    const total = fe_tongTienHang.value - fe_giamGia.value + fe_phiVanChuyen.value;
    return total > 0 ? total : 0;
});


// 2. HÀM CẬP NHẬT VOUCHER KHI NGƯỜI DÙNG CHỌN
const updateVoucher = async (isManualAction = false) => {
    const currentTab = activeTabData.value;
    if (!currentTab?.hd?.id_hoa_don) return;

    // CHỈ set flag khi người dùng CHỌN THỦ CÔNG "Không dùng voucher"
    // KHÔNG set khi voucher bị gỡ tự động do không hợp lệ
    if (isManualAction && currentTab.hd.id_voucher === null) {
        userHasManuallyDeselectedVoucher.value = true;
    }

    // Gọi API mới để áp dụng voucher
    const updatedInvoice = await store.applyVoucherToInvoice(
        currentTab.hd.id_hoa_don, 
        currentTab.hd.id_voucher
    );
    
    if (updatedInvoice) {
        // Cập nhật hóa đơn với dữ liệu mới từ backend
        Object.assign(currentTab.hd, updatedInvoice);
    }
};

// 3. LOGIC TỰ ĐỘNG XỬ LÝ VOUCHER
watch(fe_tongTienHang, async (newTotal) => {
    const currentTab = activeTabData.value;
    if (!currentTab || !currentTab.hd || !currentTab.hd.id_hoa_don) return;

    // Lấy danh sách voucher phù hợp từ API mới
    const vouchers = newTotal > 0 ? await store.getSuitableVouchersForInvoice(newTotal) : [];
    availableVouchers.value = (vouchers && Array.isArray(vouchers)) ? vouchers : [];
    
    const currentVoucherId = currentTab.hd.id_voucher;

    // Nếu người dùng đã chủ động chọn "không dùng", thì dừng lại
    if (userHasManuallyDeselectedVoucher.value) {
        return;
    }

    // Tìm voucher tốt nhất (giảm nhiều nhất)
    const bestVoucher = availableVouchers.value.length > 0
        ? [...availableVouchers.value].sort((a, b) => (b.so_tien_giam || 0) - (a.so_tien_giam || 0))[0]
        : null;

    // Kịch bản 1: Voucher đang dùng không còn hợp lệ (ví dụ: giảm số lượng)
    if (currentVoucherId && !availableVouchers.value.some(v => v.id_voucher === currentVoucherId)) {
        currentTab.hd.id_voucher = null; // Gỡ voucher khỏi giao diện
        message.warning('Voucher không còn hợp lệ và đã được gỡ bỏ.');
        // Reset flag để cho phép tự động áp dụng lại sau
        userHasManuallyDeselectedVoucher.value = false;
        await updateVoucher(false); // false = không phải manual action
    } 
    // Kịch bản 2: Chưa có voucher, nhưng giờ đã đủ điều kiện cho voucher tốt nhất
    else if (!currentVoucherId && bestVoucher) {
        currentTab.hd.id_voucher = bestVoucher.id_voucher; // Tự động áp dụng trên giao diện
        message.success(`Đã tự động áp dụng voucher: ${bestVoucher.ten_voucher}`);
        // Reset flag vì đây là auto-apply
        userHasManuallyDeselectedVoucher.value = false;
        await updateVoucher(false); // false = không phải manual action
    }
});





// Cập nhật tổng tiền khi số lượng thay đổi trong bảng hóa đơn
const updateItemTotal = (item) => {
    let soLuongMoi = item.so_luong;
    const gioiHanToiDa = item.so_luong_ton_goc;

    // Validate số lượng
    if (!soLuongMoi || soLuongMoi <= 0) {
        soLuongMoi = 1;
        message.warning('Số lượng phải lớn hơn 0. Đã đặt lại thành 1.');
    }
    if (soLuongMoi > gioiHanToiDa) {
        message.warning(`Số lượng vượt quá tồn kho (${gioiHanToiDa}). Đã đặt lại về số lượng tối đa.`);
        soLuongMoi = gioiHanToiDa;
    }
    
    // Cập nhật lại số lượng trên giao diện. Giao diện sẽ tự tính toán lại tổng tiền.
    item.so_luong = soLuongMoi;

    // Gửi yêu cầu cập nhật lên backend ở chế độ nền
    store.setSPHD(item.id_hoa_don, item.id_chi_tiet_san_pham, soLuongMoi)
        .then(() => {
            console.log(`Updated quantity for ${item.ten_san_pham} on backend.`);
            // Sau khi backend cập nhật thành công, làm mới lại dữ liệu của hóa đơn trong nền
            // để đảm bảo trạng thái cuối cùng được đồng bộ.
            refreshHoaDon(item.id_hoa_don);
        })
        .catch(err => {
            console.error('Failed to update quantity on backend:', err);
            message.error('Lỗi khi cập nhật số lượng trên máy chủ.');
        });
};





// Xóa sản phẩm khỏi hóa đơn chi tiết của tab hiện tại
const removeFromBill = (productId) => {
    const currentTab = activeTabData.value;
    if (!currentTab?.items) return;

    const itemsArray = currentTab.items.value;
    const itemIndex = itemsArray.findIndex(item => item.id_chi_tiet_san_pham === productId);
    if (itemIndex === -1) return;

    // --- Optimistic UI Update ---
    const removedItem = { ...itemsArray[itemIndex] }; // Sao chép item để có thể hoàn tác
    itemsArray.splice(itemIndex, 1);
    message.info(`Đã xóa "${removedItem.ten_san_pham}" khỏi hóa đơn.`);
    // --- Kết thúc Optimistic UI Update ---

    // --- Gửi yêu cầu lên backend ở chế độ nền ---
    store.xoaSPHD(currentTab.hd.id_hoa_don, productId)
        .then(result => {
            if (!result?.success) {
                throw new Error(result.message || "Xóa sản phẩm thất bại");
            }
            console.log('Backend updated successfully for remove.');
            // Đồng bộ lại hóa đơn và tồn kho trong nền
            refreshHoaDon(currentTab.hd.id_hoa_don);
            store.getAllCTSPKM().then(p => allProducts.value = p);
        })
        .catch(error => {
            console.error('Lỗi khi xóa sản phẩm (backend):', error);
            message.error('Lỗi: Không thể xóa sản phẩm.');
            // --- Hoàn tác lại thay đổi trên UI nếu có lỗi ---
            itemsArray.splice(itemIndex, 0, removedItem); // Thêm lại item vào vị trí cũ
        });
};


// Hàm tạo mới một tab hóa đơn
const add = async () => {
    // Kiểm tra giới hạn tổng số hóa đơn (5 active + 15 suspended = 20)
    if (panes.value.length >= MAX_TOTAL_INVOICES) {
        const activeCount = Math.min(panes.value.length, MAX_ACTIVE_INVOICES);
        const suspendedCount = panes.value.length - activeCount;
        
        message.warning({
            content: `Đã đạt giới hạn ${MAX_TOTAL_INVOICES} hóa đơn! (${activeCount} đang hiển thị + ${suspendedCount} hóa đơn treo). Vui lòng thanh toán hoặc xóa hóa đơn cũ trước khi tạo mới.`,
            duration: 6,
            style: {
                marginTop: '20vh',
                fontSize: '16px'
            }
        });
        return;
    }
    
    try {
        const response = await store.createHoaDon();
        if (!response || response.error) {
            throw new Error(response?.message || 'Không thể tạo hóa đơn');
        }

        newTabIndex.value++;
        const newKey = `invoiceTab_${Date.now()}_${newTabIndex.value}`;

        const newInvoice = {
            title: `Đơn ${panes.value.length + 1}`,
            key: newKey,
            closable: true,
            items: ref([]),
            hd: reactive({
                id_hoa_don: response.id_hoa_don,
                ma_hoa_don: response.ma_hoa_don,
                ngay_tao: response.ngay_tao,
                trang_thai: response.trang_thai,
                id_voucher: null,
                id_khach_hang: null,
                hinh_thuc_thanh_toan: 'Tiền mặt',
                phuong_thuc_nhan_hang: 'Nhận tại cửa hàng',
                isKhachLe: true,
                phi_van_chuyen: 0,
                tong_tien_truoc_giam: 0,
                tong_tien_sau_giam: 0
            })
        };

        // Thêm hóa đơn mới vào đầu danh sách
        panes.value.unshift(newInvoice);
        ptnh.value = 'Nhận tại cửa hàng';
        activeKey.value = newKey;

        // Nếu có >= 5 hóa đơn, hóa đơn thứ 5 (index 4) sẽ vào suspended
        if (panes.value.length > MAX_ACTIVE_INVOICES) {
            const suspendedInvoice = panes.value[MAX_ACTIVE_INVOICES];
            startInvoiceTimer(suspendedInvoice.hd.id_hoa_don, suspendedInvoice.hd.ma_hoa_don);
            message.info(`Hóa đơn ${suspendedInvoice.hd.ma_hoa_don} đã chuyển vào danh sách treo.`);
        }

        console.log(`📝 Created invoice ${response.ma_hoa_don}, total: ${panes.value.length}`);
    } catch (error) {
        console.error("Lỗi khi tạo hóa đơn:", error);
        toast.error(error.message || 'Lỗi khi tạo hóa đơn!');
    }
};

// Hàm đóng tab hóa đơn (Đã sửa)
const remove = async (targetKey) => {
    const tabToRemove = panes.value.find(p => p.key === targetKey);
    if (!tabToRemove) return;

    if (tabToRemove.items?.value?.length > 0) {
        Modal.confirm({
            title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
                h(DeleteOutlined, { style: 'color: #ff4d4f; font-size: 22px;' }),
                h('span', { style: 'font-size: 16px; font-weight: 600;' }, `Hủy ${tabToRemove.title}`)
            ]),
            content: () => h('div', { style: 'padding: 8px 0;' }, [
                h('p', { style: 'margin: 0 0 12px 0; font-size: 14px;' }, `Hóa đơn có ${tabToRemove.items.value.length} sản phẩm. Bạn chắc chắn muốn hủy?`),
                h('div', { style: 'background: #fff1f0; padding: 12px; border-radius: 6px; border: 1px solid #ffccc7;' }, [
                    h('div', { style: 'display: flex; align-items: center; gap: 8px; color: #cf1322;' }, [
                        h(ExclamationCircleOutlined, { style: 'font-size: 14px;' }),
                        h('span', { style: 'font-size: 13px;' }, 'Hóa đơn và tất cả sản phẩm sẽ bị xóa')
                    ])
                ])
            ]),
            okText: 'Hủy hóa đơn',
            cancelText: 'Quay lại',
            okButtonProps: { danger: true, size: 'large', style: { height: '38px' } },
            cancelButtonProps: { size: 'large', style: { height: '38px' } },
            centered: true,
            width: 450,
            onOk: async () => {
                await performRemove(tabToRemove, targetKey);
            }
        });
    } else {
        await performRemove(tabToRemove, targetKey);
    }
};

const performRemove = async (tabToRemove, targetKey) => {
    try {
        if (tabToRemove.hd?.id_hoa_don) {
            // Clear timer nếu hóa đơn này đang có timer
            clearInvoiceTimer(tabToRemove.hd.id_hoa_don);
            
            const result = await store.deleteHoaDon(tabToRemove.hd.id_hoa_don);
            if (result.error || !result.success) {
                message.error(result.message || 'Xóa hóa đơn thất bại');
                return;
            }

            // Làm mới danh sách sản phẩm
            await store.getAllCTSPKM();
            allProducts.value = store.getAllCTSPKMList;
        }

        // Xóa tab
        panes.value = panes.value.filter(pane => pane.key !== targetKey);

        // ✅ Cập nhật lại tiêu đề tab sau khi xóa
        panes.value.forEach((pane, index) => {
            pane.title = `Đơn ${index + 1}`;
        });

        // Nếu tab đang active bị xóa thì chuyển sang tab gần nhất
        if (activeKey.value === targetKey) {
            const remainingPanes = panes.value;
            activeKey.value = remainingPanes.length > 0
                ? remainingPanes[remainingPanes.length - 1].key
                : '';
        }

        // Nếu không còn tab nào, tạo tab mới
        if (panes.value.length === 0) {
            await add();
        }

        message.success('Đã xóa hóa đơn thành công');
    } catch (error) {
        console.error("Lỗi khi xóa hóa đơn:", error);
        message.error('Đã xảy ra lỗi khi xóa hóa đơn!');
    }
};

// Thêm font Arial tiếng Việt (cần tải file font .ttf và chuyển thành base64)
const callAddFont = function () {
    this.addFileToVFS('Arial-normal.ttf', 'base64-encoded-font-here');
    this.addFont('Arial-normal.ttf', 'Arial', 'normal');
};
jsPDF.API.events.push(['addFonts', callAddFont]);

const formatDate = (date) => {
    if (!date) return 'N/A';
    const d = new Date(date);
    return d.toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
};

const printInvoice = async () => {
    const doc = new jsPDF();
    doc.setFont("Roboto");
    const logoWidth = 30;
    const logoHeight = 20;
    const pageWidth = doc.internal.pageSize.getWidth();
    const logoX = (pageWidth - logoWidth) / 2;
    doc.addImage(logo, 'PNG', logoX, 15, logoWidth, logoHeight); // Logo gần chữ hơn
    const qrCodeDataUrl = await QRCode.toDataURL(activeTabData.value.hd.ma_hoa_don || 'N/A');
    doc.addImage(qrCodeDataUrl, 'PNG', 15, 10, 40, 40); // QR code gần chữ hơn
    doc.setFontSize(18);
    doc.setFont("Roboto", "bold");
    doc.text("HÓA ĐƠN BÁN HÀNG", 105, 45, { align: "center" }); // Chỉnh vị trí chữ "Hóa đơn bán hàng"
    // Thông tin cửa hàng
    doc.setFontSize(16);
    doc.setFont("Roboto", "bold");
    doc.text("Tập đoàn R", 105, 55, { align: "center" });
    doc.setFontSize(10);
    doc.setFont("Roboto", "normal");
    doc.text("Địa chỉ: Trịnh Văn Bô, Nam Từ Liêm, Hà Nội", 105, 63, { align: "center" });
    doc.text("Điện thoại: 0987654321", 105, 69, { align: "center" });
    // Vẽ đường kẻ ngang
    doc.setLineWidth(0.5);
    doc.line(20, 73, 190, 73);
    // Thông tin hóa đơn
    let y = 120;
    doc.setFontSize(12);
    doc.setFont("Roboto", "normal");
    doc.text(`Mã hóa đơn: ${activeTabData.value.hd.ma_hoa_don || 'N/A'}`, 20, 86);
    doc.text(`Ngày: ${formatDate(activeTabData.value.hd.ngay_tao)}`, 20, 102);
    doc.text(`Tên khách hàng: ${activeTabData.value.hd.ho_ten || 'Khách lẻ'}`, 20, 110);
    // Kiểm tra nếu là đơn Online/Offline và giao hàng thì hiển thị thêm số điện thoại và địa chỉ
    if (activeTabData.value.hd.loai_hoa_don === 'Online' || activeTabData.value.hd.loai_hoa_don === 'Offline'
        && activeTabData.value.hd.phuong_thuc_nhan_hang === 'Giao hàng') {
        doc.text(`SĐT: ${activeTabData.value.hd.sdt_nguoi_nhan || ''}`, 110, 110, { align: "left" });
        doc.text(`Địa chỉ: ${activeTabData.value.hd.dia_chi || ''}`, 20, 118);
        y = 126; // cập nhật vị trí `y` sau địa chỉ
    } else {
        y = 118; // nếu không có địa chỉ, dòng sản phẩm bắt đầu ngay sau tên khách hàng
    }
    // Danh sách sản phẩm

    doc.setFontSize(10);
    doc.setFont("Roboto", "bold");
    doc.text("Thông tin sản phẩm", 20, y);
    // Tiêu đề bảng
    // y += 10;
    doc.setFontSize(10);
    doc.setFont("Roboto", "bold");
    doc.text("Số lượng", 110, y, { align: "center" });
    doc.text("Đơn giá", 140, y, { align: "center" });
    doc.text("Tổng tiền", 180, y, { align: "center" });
    // Vẽ đường kẻ ngang dưới tiêu đề bảng
    y += 2;
    doc.setLineWidth(0.2);
    doc.line(20, y, 190, y);
    // Danh sách sản phẩm
    y += 6;
    doc.setFontSize(10);
    doc.setFont("Roboto", "normal");
    currentInvoiceItems.value.forEach((item, index) => {
        const productName = `${index + 1}. ${item.ten_san_pham} (Màu: ${item.mau_sac} - Size: ${item.kich_thuoc})`;
        const productLines = doc.splitTextToSize(productName, 85);
        doc.text(productLines, 20, y);
        doc.text(`${item.so_luong}`, 110, y, { align: "center" });

        // Đơn giá
        const donGia = item.gia_sau_giam && item.gia_sau_giam < item.gia_ban ? item.gia_sau_giam : item.gia_ban;
        if (item.gia_sau_giam && item.gia_sau_giam < item.gia_ban) {
            doc.setTextColor(255, 0, 0); // Màu đỏ
        }
        doc.text(`${formatCurrency(donGia)}`, 140, y, { align: "center" });
        doc.setTextColor(0); // Reset màu về đen

        // Thành tiền
        if (item.gia_sau_giam && item.gia_sau_giam < item.gia_ban) {
            doc.setTextColor(255, 0, 0); // Màu đỏ
        }
        doc.text(`${formatCurrency(donGia * item.so_luong)}`, 180, y, { align: "center" });
        doc.setTextColor(0); // Reset màu về đen

        y += productLines.length * 6 + 4;

        // Hiển thị giá gốc nếu có khuyến mãi
        if (item.gia_sau_giam && item.gia_sau_giam < item.gia_ban) {
            doc.setFontSize(8);
            doc.setTextColor(150); // Màu xám
            doc.text(`Giá gốc: ${formatCurrency(item.gia_ban)}`, 140, y - 6, { align: "center" });
            doc.setTextColor(0); // Reset màu về đen
            doc.setFontSize(10);
            y += 4;
        }
    });
    // Vẽ đường kẻ ngang sau danh sách sản phẩm
    doc.setLineWidth(0.2);
    doc.line(20, y, 190, y);
    // Tổng tiền
    y += 10;
    doc.setFontSize(12);
    doc.setFont("Roboto", "normal");
    
    // Tổng tiền sản phẩm (chưa có ship)
    const tongTienSanPham = (activeTabData.value.hd.tong_tien_truoc_giam || 0) - (activeTabData.value.hd.phi_van_chuyen || 0);
    doc.text(`Tổng tiền hàng:`, 115, y, { align: "left" });
    doc.text(`${formatCurrency(tongTienSanPham)}`, 190, y, { align: "right" });

    // Phí vận chuyển (nếu có)
    if (activeTabData.value.hd.phi_van_chuyen && activeTabData.value.hd.phi_van_chuyen > 0) {
        y += 6;
        doc.text(`Phí vận chuyển:`, 115, y, { align: "left" });
        doc.text(`+${formatCurrency(activeTabData.value.hd.phi_van_chuyen)}`, 190, y, { align: "right" });
    }

    y += 6;
    // Giảm giá từ voucher = Tổng trước giảm - Tổng sau giảm
    const giamGia = (activeTabData.value.hd.tong_tien_truoc_giam || 0) -
        (activeTabData.value.hd.tong_tien_sau_giam || 0);
    if (giamGia > 0) {
        doc.text(`Giảm giá (Voucher):`, 115, y, { align: "left" });
        doc.text(`-${formatCurrency(giamGia)}`, 190, y, { align: "right" });
        y += 6;
    }

    doc.setFont("Roboto", "bold");
    doc.text(`Thành tiền:`, 115, y, { align: "left" });
    doc.text(`${formatCurrency(activeTabData.value.hd.tong_tien_sau_giam)}`, 190, y, { align: "right" });
    if (activeTabData.value.hd.hinh_thuc_thanh_toan === "Tiền mặt") {
        y += 6;
        doc.setFont("Roboto", "bold");
        doc.text(`Tiền khách đưa:`, 115, y, { align: "left" });
        doc.text(`${formatCurrency(tienKhachDua.value)}`, 190, y, { align: "right" });
        y += 6;
        doc.setFont("Roboto", "bold");
        doc.text(`Tiền trả khách:`, 115, y, { align: "left" });
        doc.text(`${formatCurrency(calculatedChange.value)}`, 190, y, { align: "right" });
    }


    // Chân trang
    y += 10;
    doc.setFontSize(10);
    doc.setFont("Roboto", "normal");
    doc.text("Cảm ơn Quý Khách, hẹn gặp lại!", 105, y, { align: "center" });

    // Lưu file PDF
    doc.save(`HoaDon_${activeTabData.value.hd.ma_hoa_don}.pdf`);
};

// Xử lý sự kiện edit tab (add hoặc remove)
const onEdit = (targetKeyOrAction, action) => {
    if (action === 'add') {
        add();
    } else {
        remove(targetKeyOrAction);
    }
};

const showPaymentConfirm = ref(false);
const showPrintConfirm = ref(false);

// Hàm xử lý thanh toán - Bước 1: Hiển thị modal confirm thanh toán
const handlePayment = async () => {
    const currentTab = activeTabData.value;
    if (!currentTab) {
        message.error("Không có hóa đơn nào đang được chọn.");
        return;
    }

    if (!currentTab.items || currentTab.items.value.length === 0) {
        message.warning("Hóa đơn chưa có sản phẩm nào.");
        return;
    }

    if (currentTab.hd.phuong_thuc_nhan_hang === 'Giao hàng') {
        const tenKH = currentTab.hd.ten_khach_hang || currentTab.hd.ho_ten || '';
        const sdt = currentTab.hd.so_dien_thoai || currentTab.hd.sdt || currentTab.hd.sdt_nguoi_nhan || '';
        const diaChi = currentTab.hd.dia_chi || '';
        
        if (!tenKH?.trim() || !sdt?.trim() || !diaChi?.trim()) {
            message.error("Vui lòng nhập đầy đủ thông tin giao hàng (Tên, SĐT, Địa chỉ) hoặc chọn khách hàng");
            return;
        }
        if (!currentTab.hd.phi_van_chuyen || currentTab.hd.phi_van_chuyen <= 0) {
            message.error("Vui lòng nhập phí vận chuyển cho đơn hàng giao.");
            return;
        }
    }
    if (currentTab.hd.hinh_thuc_thanh_toan === 'Tiền mặt') {
        const totalAfterVoucher = fe_tongThanhToan.value;
        if (currentTab.hd.tien_khach_dua === null || currentTab.hd.tien_khach_dua < totalAfterVoucher) {
            message.error("Vui lòng nhập đủ tiền khách đưa.");
            return;
        }
        currentTab.hd.tien_du = currentTab.hd.tien_khach_dua - totalAfterVoucher;
    }

    const total = fe_tongThanhToan.value || 0;
    const cash = tienKhachDua.value || 0;

    if (activeTabData.value.hd.hinh_thuc_thanh_toan === 'Tiền mặt' && cash < total) {
        message.error('Tiền khách đưa không đủ để thanh toán!');
        return;
    }

    // Hiển thị modal confirm thanh toán
    showPaymentConfirm.value = true;
};

// Hủy thanh toán
const cancelPayment = () => {
    showPaymentConfirm.value = false;
};

// Bước 2: Xác nhận thanh toán -> Thực hiện thanh toán -> Hiển thị modal in hóa đơn
const proceedToPayment = async () => {
    showPaymentConfirm.value = false;
    
    const hinhThuc = activeTabData.value.hd.hinh_thuc_thanh_toan;
    
    // Thực hiện thanh toán
    try {
        if (hinhThuc === "Tiền mặt") {
            await store.trangThaiDonHang(activeTabData.value.hd.id_hoa_don);
            // Sau khi thanh toán thành công -> hiển thị modal in hóa đơn
            showPrintConfirm.value = true;
        } else if (hinhThuc === "PayOS") {
            // Validate payment amount - USE computed property!
            const paymentAmount = fe_tongThanhToan.value;
            
            if (paymentAmount <= 0) {
                message.error('Số tiền thanh toán không hợp lệ. Vui lòng thêm sản phẩm vào hóa đơn!');
                console.error('Invalid payment amount:', paymentAmount);
                console.log('Debug - Tổng hàng:', fe_tongTienHang.value);
                console.log('Debug - Giảm giá:', fe_giamGia.value);
                console.log('Debug - Phí ship:', fe_phiVanChuyen.value);
                return;
            }
            
            // PayOS payment
            const payment_info = {
                productName: "Đơn hàng " + `R-${activeTabData.value.hd.id_hoa_don}-${new Date().getTime()}`,
                description: `PayOS - ${currentInvoiceItems.value.length} sản phẩm`,
                returnUrl: window.location.origin + "/admin/banhang",
                price: paymentAmount,
                cancelUrl: window.location.origin + "/admin/banhang"
            };
            
            console.log('PayOS Payment Info:', payment_info);
            
            localStorage.setItem('checkPaymentStatus', 'true');
            localStorage.setItem('idHDPayMent', JSON.stringify(activeTabData.value.hd.id_hoa_don));
            localStorage.setItem('paymentMethod', 'PayOS');
            localStorage.removeItem('khachHangBH');
            
            await thanhToanService.handlePayOSPayment(payment_info);
            
        } else if (hinhThuc === "ZaloPay") {
            // Validate payment amount
            const paymentAmount = fe_tongThanhToan.value;
            
            if (paymentAmount <= 0) {
                message.error('Số tiền thanh toán không hợp lệ. Vui lòng thêm sản phẩm vào hóa đơn!');
                console.error('Invalid payment amount:', paymentAmount);
                return;
            }
            
            console.log('ZaloPay Payment - ID Hóa đơn:', activeTabData.value.hd.id_hoa_don);
            console.log('ZaloPay Payment - Số tiền:', paymentAmount);
            
            localStorage.setItem('checkPaymentStatus', 'true');
            localStorage.setItem('idHDPayMent', JSON.stringify(activeTabData.value.hd.id_hoa_don));
            localStorage.setItem('paymentMethod', 'ZaloPay');
            localStorage.removeItem('khachHangBH');
            
            await thanhToanService.handleZaloPayPayment(activeTabData.value.hd.id_hoa_don);
        }
    } catch (error) {
        console.error('Lỗi khi thanh toán:', error);
        message.error('Đã xảy ra lỗi khi thanh toán!');
    }
};

// Bước 3: Xác nhận in hóa đơn
const confirmPrint = async (shouldPrint) => {
    showPrintConfirm.value = false;
    
    if (shouldPrint) {
        printInvoice();
    }
    
    // Thông báo thành công và reload
    message.success({
        content: `✅ Thanh toán thành công đơn hàng ${activeTabData.value.hd.ma_hoa_don}!`,
        duration: 3
    });
    
    localStorage.removeItem('khachHangBH');
    
    setTimeout(() => {
        router.push('/admin/banhang');
        window.location.reload();
    }, 1500);
};

const updateHinhThucThanhToan = async () => {
    try {
        const id = activeTabData.value.hd.id_hoa_don;
        const hinhThuc = activeTabData.value.hd.hinh_thuc_thanh_toan;
        await store.updateHinhThucTTHoaDon(id, hinhThuc);
    } catch (err) {
        console.error("Lỗi cập nhật hình thức thanh toán", err);
    }
};


const da = ref([]);

// --- Lifecycle Hooks ---
onMounted(async () => {
    await checkAndApplyLocalData();
    await loadData();
    stopQrScanner();
    setupAutoReloadAtMidnight();
    startChecking();

    const checkPaymentStatus = localStorage.getItem('checkPaymentStatus');
    const paymentMethod = localStorage.getItem('paymentMethod'); // 'PayOS' or 'ZaloPay'
    
    if (checkPaymentStatus === 'true') {
        if (paymentMethod === 'ZaloPay') {
            // ZaloPay Polling Mechanism
            const idhdpay = JSON.parse(localStorage.getItem('idHDPayMent'));
            let pollCount = 0;
            const maxPolls = 20; // Poll tối đa 20 lần (60 giây)
            const pollInterval = 3000; // Poll mỗi 3 giây
            
            console.log('🔄 Starting ZaloPay payment status polling...');
            
            // Show initial notification
            const loadingMessage = message.loading({
                content: '⏳ Đang kiểm tra trạng thái thanh toán ZaloPay...',
                duration: 0 // Keep showing until we close it
            });
            
            const pollPaymentStatus = setInterval(async () => {
                pollCount++;
                console.log(`🔍 Poll #${pollCount}: Checking ZaloPay status for invoice ${idhdpay}...`);
                
                try {
                    const zaloStatus = await thanhToanService.checkZaloPayStatus(idhdpay);
                    console.log('ZaloPay Status Response:', zaloStatus);
                    
                    if (zaloStatus && zaloStatus.return_code === 1) {
                        // ✅ Payment successful!
                        clearInterval(pollPaymentStatus);
                        loadingMessage();
                        
                        message.success({
                            content: '✅ Thanh toán ZaloPay thành công!',
                            duration: 5
                        });
                        
                        await refreshHoaDon(idhdpay);
                        showPrintConfirm.value = true;
                        
                        // Cleanup
                        localStorage.removeItem('checkPaymentStatus');
                        localStorage.removeItem('paymentMethod');
                        localStorage.removeItem('zaloPayResponse');
                        localStorage.removeItem('idHDPayMent');
                        
                    } else if (pollCount >= maxPolls) {
                        // ⏱️ Timeout - stop polling
                        clearInterval(pollPaymentStatus);
                        loadingMessage();
                        
                        message.warning({
                            content: '⚠️ Không thể xác nhận trạng thái thanh toán. Vui lòng kiểm tra lại hóa đơn!',
                            duration: 6
                        });
                        
                        // Cleanup
                        localStorage.removeItem('checkPaymentStatus');
                        localStorage.removeItem('paymentMethod');
                        localStorage.removeItem('zaloPayResponse');
                        localStorage.removeItem('idHDPayMent');
                    } else {
                        // Continue polling
                        console.log(`⏳ Payment pending... (${pollCount}/${maxPolls})`);
                    }
                } catch (error) {
                    console.error('Error checking ZaloPay status:', error);
                    
                    if (pollCount >= maxPolls) {
                        clearInterval(pollPaymentStatus);
                        loadingMessage();
                        
                        message.error({
                            content: '❌ Lỗi khi kiểm tra trạng thái thanh toán ZaloPay!',
                            duration: 5
                        });
                        
                        // Cleanup
                        localStorage.removeItem('checkPaymentStatus');
                        localStorage.removeItem('paymentMethod');
                        localStorage.removeItem('zaloPayResponse');
                        localStorage.removeItem('idHDPayMent');
                    }
                }
            }, pollInterval);
            
        } else if (paymentMethod === 'PayOS') {
            // PayOS status check (one-time)
            try {
                const idhdpay = JSON.parse(localStorage.getItem('idHDPayMent'));
                const paymentResponse = JSON.parse(localStorage.getItem('paymentResponse'));
                
                console.log('🔍 Checking PayOS payment status for invoice:', idhdpay);
                
                if (paymentResponse && paymentResponse.data && paymentResponse.data.orderCode) {
                    const paystatus = await thanhToanService.checkStatusPayment(paymentResponse.data.orderCode);
                    
                    if (paystatus.status === "PAID") {
                        await store.trangThaiDonHang(idhdpay);
                        message.success({
                            content: '✅ Thanh toán PayOS thành công!',
                            duration: 3
                        });
                        await refreshHoaDon(idhdpay);
                        showPrintConfirm.value = true;
                    } else if (paystatus.status === "PENDING") {
                        message.warning({
                            content: '⏳ Thanh toán PayOS đang chờ xử lý...',
                            duration: 3
                        });
                    } else if (paystatus.status === "CANCELLED") {
                        message.error({
                            content: '❌ Thanh toán PayOS đã bị hủy!',
                            duration: 3
                        });
                    }
                }
            } catch (error) {
                console.error("Lỗi khi kiểm tra trạng thái PayOS:", error);
                message.error({
                    content: '⚠️ Không thể kiểm tra trạng thái thanh toán PayOS!',
                    duration: 4
                });
            } finally {
                // Cleanup
                localStorage.removeItem('checkPaymentStatus');
                localStorage.removeItem('paymentMethod');
                localStorage.removeItem('paymentResponse');
                localStorage.removeItem('idHDPayMent');
            }
        }
    }



});

// Thiết lập setInterval để kiểm tra luuTTKHBH và shippingFeeUpdated
let intervalId = null;
const startChecking = () => {
    intervalId = setInterval(async () => {
        await checkAndApplyLocalData();
        await checkAndApplyShippingFee();
    }, 3000); // Kiểm tra mỗi 3 giây để tối ưu hiệu năng
};

// Dọn dẹp interval khi component bị hủy
onUnmounted(() => {
    if (intervalId) {
        clearInterval(intervalId);
    }
});

async function loadData() {
    try {
        await store.getAllHoaDonCTT();
        await store.getAllCTSPKM();
        await store.getAllKhachHangNoPage();
        da.value = store.getAllHoaDonCTTArr;

        panes.value = da.value.map((hd, index) => ({
            key: `invoiceTab_${index}_${Date.now()}`,
            title: `Đơn ${index + 1}`,
            closable: true,
            items: ref([]),
            hd: reactive({
                ...hd,
                hinh_thuc_thanh_toan: hd.hinh_thuc_thanh_toan,
                phuong_thuc_nhan_hang: hd.phuong_thuc_nhan_hang,
                isKhachLe: !hd.id_khach_hang,

            })
        }));

        if (panes.value.length > 0) {
            activeKey.value = panes.value[0].key;
        } else {
            await add();
        }

        allProducts.value = store.getAllCTSPKMList;

    } catch (error) {
        console.error("Lỗi khi tải dữ liệu:", error);
    }
}

function setupAutoReloadAtMidnight() {
    const now = new Date();
    const midnight = new Date();
    midnight.setHours(24, 0, 0, 0);

    const timeUntilMidnight = midnight.getTime() - now.getTime();

    setTimeout(() => {
        loadData();
        setInterval(loadData, 24 * 60 * 60 * 1000);
    }, timeUntilMidnight);
}


watch(() => activeKey.value, async (newKey) => {
    console.log('👁️ WATCH activeKey triggered, newKey:', newKey);
    const currentTab = panes.value.find(p => p.key === newKey);
    if (currentTab && currentTab.hd.id_hoa_don) {
        console.log('📡 WATCH: GỌI API getAllSPHD cho hóa đơn:', currentTab.hd.id_hoa_don);
        await store.getAllSPHD(currentTab.hd.id_hoa_don);
        
        console.log('📦 WATCH: Dữ liệu từ server:', store.getAllSPHDArr.length, 'items');
        
        currentTab.items.value = store.getAllSPHDArr.map(item => ({
            id_hoa_don: item.id_hoa_don,
            id_chi_tiet_san_pham: item.id_chi_tiet_san_pham,
            hinh_anh: item.hinh_anh,
            ten_san_pham: item.ten_san_pham,
            mau_sac: item.ten_mau_sac || item.mau_sac || null,
            kich_thuoc: item.gia_tri || null,
            so_luong: item.so_luong,
            gia_ban: item.gia_ban,  // ✅ Giá lẻ
            tong_tien: item.don_gia,  // ✅ Tổng tiền
            so_luong_ton_goc: item.so_luong_ton || 0
        })) || [];
        
        console.log('🎨 WATCH: Mapped items:', currentTab.items.value.length, 'items');
        
        // Cập nhật các giá trị liên quan
        ptnh.value = currentTab.hd.phuong_thuc_nhan_hang;
        store.setCurrentHoaDonId(currentTab.hd.id_hoa_don);
    }
}, { immediate: true });

// Watcher kiểm soát dropdown đã được xóa - a-dropdown tự quản lý visibility qua trigger=['click']

// Watcher này đã được xóa vì không cần thiết (filteredProducts tự động update)

const isLoading = ref(false);

const checkAndApplyLocalData = async () => {
    const customerData = JSON.parse(localStorage.getItem('luuTTKHBH'));
    if (customerData && customerData.saved) {
        console.log('📥 Đọc thông tin khách hàng từ localStorage:', customerData);
        
        isLoading.value = true;
        await new Promise(resolve => setTimeout(resolve, 500));
        try {
            const idHoaDon = activeTabData.value.hd.id_hoa_don;
            
            // ✅ Cập nhật thông tin khách hàng vào hóa đơn hiện tại
            Object.assign(activeTabData.value.hd, {
                ten_khach_hang: customerData.ten_khach_hang,
                so_dien_thoai: customerData.so_dien_thoai,
                dia_chi: customerData.dia_chi,
                email: customerData.email
            });
            
            console.log('✅ Đã cập nhật thông tin vào hóa đơn:', {
                ten_khach_hang: customerData.ten_khach_hang,
                so_dien_thoai: customerData.so_dien_thoai,
                dia_chi: customerData.dia_chi
            });
            
            await refreshHoaDon(idHoaDon);
        } catch (error) {
            console.error("Lỗi khi làm mới dữ liệu:", error);
            message.error("Không thể làm mới dữ liệu hóa đơn!");
        } finally {
            localStorage.removeItem('luuTTKHBH');
            isLoading.value = false;
        }
    }
};

// Kiểm tra và cập nhật phí vận chuyển từ localStorage
const checkAndApplyShippingFee = async () => {
    const shippingData = JSON.parse(localStorage.getItem('shippingFeeUpdated'));
    if (shippingData) {
        const currentIdHoaDon = activeTabData.value?.hd?.id_hoa_don;
        
        if (currentIdHoaDon === shippingData.idHoaDon) {
            console.log('📦 Cập nhật phí vận chuyển:', shippingData.phiVanChuyen);
            
            activeTabData.value.hd.phi_van_chuyen = shippingData.phiVanChuyen;
            
            await refreshHoaDon(currentIdHoaDon);
            localStorage.removeItem('shippingFeeUpdated');
        }
    }
};



function tachDiaChi(addressString) {
    if (!addressString) return null;

    const parts = addressString.split(',').map(p => p.trim());
    if (parts.length < 4) return null;

    return {
        address: parts[0],
        ward: parts[1],
        district: parts[2],
        province: parts[3],
    };
}


const handlePhuongThucChange = async () => {
    const idHD = activeTabData.value.hd.id_hoa_don;
    const diaChiNhan = activeTabData.value.hd.dia_chi;
    let phiShip = 0;
    const weight = 500;
    const tongTienHoaDon = activeTabData.value.hd.tong_tien_sau_giam;
    
    if (activeTabData.value.hd.phuong_thuc_nhan_hang === 'Nhận tại cửa hàng') {
        ptnh.value = 'Nhận tại cửa hàng';
        await store.setTrangThaiNhanHang(idHD, 'Nhận tại cửa hàng', 0);
    } else {
        ptnh.value = 'Giao hàng';
        if (activeTabData.value.hd.tong_tien_truoc_giam >= 2000000) {
            phiShip = 0;
            await store.setTrangThaiNhanHang(idHD, 'Giao hàng', phiShip);
            refreshHoaDon(idHD);
            return;
        }
        const diaChi = tachDiaChi(diaChiNhan);
        if (diaChi) {
            const result = await banHangService.tinhPhiShip(
                "Hà Nội",
                "Nam Từ Liêm",
                diaChi.province,
                diaChi.district,
                weight,
                tongTienHoaDon
            );
            phiShip = result.fee;
            activeTabData.value.hd.phi_van_chuyen = phiShip;
        } else {
            activeTabData.value.hd.phi_van_chuyen = 0;
        }
        await store.setTrangThaiNhanHang(idHD, 'Giao hàng', phiShip);
    }
};

// ✅ ZALOPAY - Hiển thị QR Code
const showZaloPayQR = async () => {
    try {
        isLoadingZaloPay.value = true;
        
        // Kiểm tra dữ liệu hóa đơn
        if (!activeTabData.value || !activeTabData.value.hd || !activeTabData.value.hd.id_hoa_don) {
            message.error('Vui lòng chọn hóa đơn cần thanh toán');
            return;
        }
        
        const idHoaDon = activeTabData.value.hd.id_hoa_don;
        console.log('Tạo QR ZaloPay cho hóa đơn ID:', idHoaDon);
        
        const result = await store.createZaloPayOrder(idHoaDon);
        console.log('ZaloPay Response:', result);
        
        if (result.return_code === 1) {
            // ZaloPay trả về order_url string, cần convert sang image
            if (result.order_url) {
                try {
                    // Generate QR code image từ string
                    const qrDataUrl = await QRCode.toDataURL(result.order_url, {
                        width: 300,
                        margin: 2,
                        color: {
                            dark: '#000000',
                            light: '#FFFFFF'
                        }
                    });
                    zaloPayQRUrl.value = qrDataUrl;
                    zaloPayQRCode.value = result.order_url;
                } catch (qrError) {
                    console.error('Lỗi tạo QR image:', qrError);
                    message.error('Không thể tạo mã QR');
                    return;
                }
            } else {
                message.error('Không nhận được mã QR từ ZaloPay');
                return;
            }
            
            showZaloPayModal.value = true;
            paymentStatus.value = 'checking';
            
            // Bắt đầu kiểm tra trạng thái thanh toán mỗi 3 giây
            startCheckingPaymentStatus();
        } else {
            message.error(result.return_message || 'Không thể tạo mã QR thanh toán');
        }
    } catch (error) {
        console.error('Lỗi khi tạo QR ZaloPay:', error);
        message.error('Đã xảy ra lỗi khi tạo mã thanh toán: ' + (error.message || ''));
    } finally {
        isLoadingZaloPay.value = false;
    }
};

// ✅ ZALOPAY - Kiểm tra trạng thái thanh toán
const startCheckingPaymentStatus = () => {
    checkPaymentInterval = setInterval(async () => {
        try {
            const result = await store.checkZaloPayStatus(activeTabData.value.hd.id_hoa_don);
            
            if (result.return_code === 1) {
                // Thanh toán thành công
                paymentStatus.value = 'success';
                clearInterval(checkPaymentInterval);
                
                setTimeout(() => {
                    showZaloPayModal.value = false;
                    message.success('Thanh toán ZaloPay thành công!');
                    
                    // Refresh hóa đơn
                    refreshHoaDon(activeTabData.value.hd.id_hoa_don);
                    closeZaloPayModal();
                }, 2000);
                
            } else if (result.return_code === 2) {
                // Đang xử lý
                paymentStatus.value = 'checking';
            } else {
                // Thất bại hoặc đã hủy
                paymentStatus.value = 'failed';
                clearInterval(checkPaymentInterval);
            }
        } catch (error) {
            console.error('Lỗi khi kiểm tra trạng thái:', error);
        }
    }, 3000); // Kiểm tra mỗi 3 giây
};

// ✅ ZALOPAY - Đóng modal
const closeZaloPayModal = () => {
    if (checkPaymentInterval) {
        clearInterval(checkPaymentInterval);
        checkPaymentInterval = null;
    }
    showZaloPayModal.value = false;
    zaloPayQRUrl.value = '';
    zaloPayQRCode.value = '';
    paymentStatus.value = '';
};


</script>

<style scoped>
/* Global Reset for Consistency */
* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

/* Header Container */
.header-container {
    height: 70px;
    background-color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    border-bottom: 1px solid #f0f0f0;
    width: 100%;
}

/* Search Section */
.search-section {
    display: flex;
    align-items: center;
    gap: 12px;
}

/* Dropdown Content */
.dropdown-content-custom {
    width: 600px;
    max-height: 400px;
    background-color: #ffffff;
    border: 1px solid #e5e5e5;
    border-radius: 8px;
    padding: 12px;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
    overflow-y: auto;
    z-index: 1000;
}

/* Product Option */
.product-option {
    display: flex;
    align-items: center;
    padding: 10px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background-color 0.2s ease;
}

.product-option:hover {
    background-color: #fff3e6;
}

/* Product Image */
.product-image {
    width: 50px;
    height: 50px;
    object-fit: cover;
    border-radius: 6px;
    margin-right: 12px;
    border: 1px solid #f0f0f0;
}

/* Product Info Split */
.product-info-split {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    gap: 12px;
}

/* Info Left and Right */
.info-left {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.info-right {
    min-width: 90px;
    text-align: right;
    font-weight: 600;
    color: #ff6600;
}

/* Product Price and Name */
.product-price {
    font-size: 14px;
}

/* Price container for products with discounts */
.product-price-container {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 4px;
}

.price-with-discount {
    display: flex;
    align-items: center;
    gap: 6px;
}

.original-price {
    font-size: 12px;
    color: #999;
    text-decoration: line-through;
    font-weight: 400;
}

.discount-badge {
    background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
    color: white;
    font-size: 10px;
    font-weight: 700;
    padding: 2px 6px;
    border-radius: 4px;
    letter-spacing: 0.5px;
    box-shadow: 0 2px 4px rgba(255, 107, 107, 0.3);
}

.current-price {
    font-size: 15px;
    font-weight: 700;
    color: #ff6600;
    animation: priceGlow 2s ease-in-out infinite;
}

@keyframes priceGlow {
    0%, 100% {
        text-shadow: 0 0 5px rgba(255, 102, 0, 0.3);
    }
    50% {
        text-shadow: 0 0 10px rgba(255, 102, 0, 0.5);
    }
}

.product-name {
    font-weight: 600;
    color: #1f1f1f;
    margin-bottom: 4px;
}

/* Product Details */
.product-details span {
    font-size: 12px;
    color: #666;
    display: block;
}

/* Empty Result */
.empty-result {
    padding: 16px;
    color: #999;
    text-align: center;
    font-style: italic;
}

/* Invoice Tabs */
.invoice-tabs {
    flex: 1;
    max-width: 600px;
}

/* Action Buttons */
.action-buttons {
    display: flex;
    gap: 12px;
    align-items: center;
}

.action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #ff6600;
    border-color: #ff6600;
    color: white;
    transition: all 0.3s ease;
}

.action-btn:hover {
    background-color: #e55a00;
    border-color: #e55a00;
    color: white;
}

/* Custom Tab */
.custom-tab {
    position: relative;
    padding-right: 4px;
    display: flex;
    align-items: center;
    color: #1f1f1f;
}

/* Close Icon */
.close-icon {
    font-size: 12px;
    margin-left: 5px;
    opacity: 0;
    transition: opacity 0.3s ease;
    color: #999;
}

.close-icon:hover {
    color: #ff6600;
}

.custom-tab:hover .close-icon {
    opacity: 1;
}

/* Ant Design Overrides */
:deep(.ant-tabs-card > .ant-tabs-nav .ant-tabs-tab),
:deep(.ant-tabs-card > div > .ant-tabs-nav .ant-tabs-tab) {
    background-color: #f9f9f9;
    border: none;
    color: #1f1f1f !important;
    border-radius: 6px;
    margin-right: 4px;
}

:deep(.ant-tabs-card > .ant-tabs-nav .ant-tabs-tab-active),
:deep(.ant-tabs-card > div > .ant-tabs-nav .ant-tabs-tab-active) {
    background-color: #ff6600;
    color: white !important;
}

:deep(.ant-tabs-card > .ant-tabs-nav .ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn),
:deep(.ant-tabs-card > div > .ant-tabs-nav .ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn) {
    color: white !important;
}

:deep(.ant-tabs-nav) {
    margin-bottom: 0;
}

:deep(.ant-select-selector) {
    background-color: #ffffff !important;
    border-color: #d9d9d9 !important;
}

:deep(.ant-tabs-content) {
    display: none;
}

:deep(.ant-qrcode) {
    cursor: pointer;
    transition: transform 0.2s ease;
    border: 2px solid #ff6600;
    border-radius: 6px;
}

:deep(.ant-qrcode:hover) {
    transform: scale(1.05);
}

:deep(.ant-input-search .ant-input) {
    border-radius: 6px 0 0 6px;
    border-color: #d9d9d9;
}

:deep(.ant-input-search .ant-btn) {
    background-color: #ff6600;
    border-color: #ff6600;
    color: white;
    border-radius: 0 6px 6px 0;
}

:deep(.ant-input-search .ant-btn:hover) {
    background-color: #e55a00;
    border-color: #e55a00;
}

:deep(.ant-btn-primary) {
    background-color: #ff6600;
    border-color: #ff6600;
}

:deep(.ant-btn-primary:hover) {
    background-color: #e55a00;
    border-color: #e55a00;
}

:deep(.ant-modal-header) {
    background-color: #ff6600;
    color: white;
}

:deep(.ant-modal-title) {
    color: white;
}

:deep(.ant-table-thead > tr > th) {
    background-color: #fff3e6;
    color: #1f1f1f;
    font-weight: 600;
}

:deep(.ant-table-row:hover > td) {
    background-color: #fff9f0 !important;
}

/* Switch (Toggle) Styling */
:deep(.ant-switch) {
    background-color: #d9d9d9;
}

:deep(.ant-switch-checked) {
    background-color: #ff6600;
}

:deep(.ant-switch-checked:hover:not(.ant-switch-disabled)) {
    background-color: #e55a00;
}

:deep(.ant-switch-handle::before) {
    background-color: #ffffff;
}

/* Payment Button Styling */
:deep(.btn-primary) {
    background-color: #ff6600 !important;
    border-color: #ff6600 !important;
    color: white !important;
    border-radius: 6px;
    font-weight: 500;
    transition: all 0.3s ease;
}

:deep(.btn-primary:hover:not(:disabled)) {
    background-color: #e55a00 !important;
    border-color: #e55a00 !important;
}

:deep(.btn-primary:disabled) {
    background-color: #ffb380 !important;
    border-color: #ffb380 !important;
    color: #ffffff !important;
    cursor: not-allowed;
}

/* QR Reader */
#qr-reader {
    width: 100%;
    max-height: 400px;
    border-radius: 8px;
}

/* QR Scanner Modal */
.qr-scanner-modal :deep(.ant-modal-body) {
    padding: 20px;
    text-align: center;
    background-color: #f9f9f9;
}

/* Form Label with Logo */
.form-label-with-logo {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
}

.ghtk-logo {
    width: 100px;
    height: 24px;
    object-fit: contain;
}

/* Main Layout (Text Container) */
.text {
    display: flex;
    flex-wrap: wrap;
    width: 100%;
    margin: 0;
    padding: 10px 0;
    /* Padding to match header */
}

/* Row Layout */
.row {
    display: flex;
    width: 100%;
    gap: 16px;
    margin: 0;
    padding: 0;
    align-items: stretch;
    /* Đảm bảo bảng và form có chiều cao bằng nhau */
}

/* Columns */
.col-8,
.col-4 {
    flex: 1;
    min-width: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
}

.col-8 {
    flex: 2;
    /* Bảng chiếm 2/3 không gian */
}

.col-4 {
    flex: 1;
    /* Form chiếm 1/3 không gian */
    min-width: 300px;
}

/* Table Styling */
.table-responsive {
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
    height: auto;
    /* Chiều cao động dựa trên nội dung */
    min-height: 350px;
    /* Chiều cao tối thiểu để căn với form */
    overflow-y: auto;
    margin: 0;
    flex: 1;
    /* Đảm bảo bảng chiếm toàn bộ không gian có sẵn */
}

.table {
    background: #ffffff;
    border-collapse: separate;
    border-spacing: 0;
    width: 100%;
}

/* Center text in specific columns */
.table th:nth-child(1),
.table td:nth-child(1),
.table th:nth-child(4),
.table td:nth-child(4),
.table th:nth-child(5),
.table td:nth-child(5),
.table th:nth-child(6),
.table td:nth-child(6),
.table th:nth-child(7),
.table td:nth-child(7) {
    text-align: center;
}

.table-hover tbody tr:hover {
    background: #fff9f0;
    transition: background 0.3s ease;
}

/* Ensure images in the table don't cause layout shifts */
.invoice-item-image {
    width: 50px;
    height: 50px;
    object-fit: cover;
    border-radius: 8px;
}

/* Form Styling */
form {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px;
    background: #ffffff;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    height: auto;
    /* Chiều cao động dựa trên nội dung */
    min-height: 350px;
    /* Chiều cao tối thiểu để căn với bảng */
    overflow-y: auto;
    /* Cho phép cuộn nếu nội dung vượt quá */
    margin: 0;
    flex: 1;
    /* Đảm bảo form chiếm toàn bộ không gian có sẵn */
}

/* Form Inputs */
:deep(.form-control) {
    border-radius: 6px;
    border: 1px solid #d9d9d9;
    padding: 8px;
    transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

:deep(.form-control:focus) {
    border-color: #ff6600;
    box-shadow: 0 0 0 3px rgba(255, 102, 0, 0.2);
}

/* Form Labels */
label.form-label {
    font-weight: 500;
    color: #1f1f1f;
    margin-bottom: 5px;
    display: block;
    font-size: 14px;
    line-height: 1.5;
}

/* Radio Buttons */
:deep(.form-check) {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    margin-left: 20px;
    padding: 0;
}

:deep(.form-check-inline) {
    display: inline-flex;
    align-items: center;
    margin-right: 16px;
    margin-bottom: 0;
}

:deep(.form-check-input) {
    appearance: none;
    /* Loại bỏ kiểu mặc định của trình duyệt */
    width: 16px;
    height: 16px;
    border: 2px solid #d9d9d9;
    border-radius: 50%;
    /* Làm tròn nút radio */
    transition: all 0.3s ease;
    margin-right: 8px;
    margin-top: 0;
    /* Đảm bảo không bị thụt vào */
    background-color: #fff;
    cursor: pointer;
    vertical-align: middle;
    flex-shrink: 0;
    /* Ngăn nút radio bị co lại */
}

:deep(.form-check-input:checked) {
    background-color: #ff6600;
    border-color: #ff6600;
    box-shadow: 0 0 0 2px rgba(255, 102, 0, 0.2);
    /* Hiệu ứng khi chọn */
}

:deep(.form-check-label) {
    font-size: 14px;
    color: #1f1f1f;
    line-height: 1.5;
    margin-bottom: 0;
    cursor: pointer;
    /* Thêm con trỏ để nhấn vào nhãn */
    vertical-align: middle;
    /* Căn giữa nhãn với nút radio */
}

/* Remove unnecessary margins */
.mb-3 {
    margin-bottom: 0 !important;
}

/* ===== MODERN PAYMENT METHODS GRID ===== */
.payment-methods-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    margin-top: 12px;
}

.payment-method-option {
    position: relative;
    border: 2px solid #e5e5e5;
    border-radius: 12px;
    padding: 16px 12px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;
    background: #ffffff;
}

.payment-method-option:hover {
    border-color: #ff6600;
    background: #fff9f0;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(255, 102, 0, 0.1);
}

.payment-method-option.active {
    border-color: #ff6600;
    background: linear-gradient(135deg, #fff3e6 0%, #ffffff 100%);
    box-shadow: 0 4px 16px rgba(255, 102, 0, 0.15);
}

.payment-method-option .form-check-input {
    position: absolute;
    opacity: 0;
    pointer-events: none;
}

.payment-label {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    margin: 0;
    cursor: pointer;
    font-weight: 500;
    color: #1f1f1f;
}

.payment-icon {
    font-size: 32px;
    margin-bottom: 4px;
}

.payment-text {
    font-size: 14px;
    font-weight: 600;
}

.payment-method-option.active .payment-text {
    color: #ff6600;
}

/* Responsive Adjustments */
@media (max-width: 768px) {
    .header-container {
        flex-direction: column;
        height: auto;
        padding: 16px;
    }

    .search-section {
        width: 100%;
        margin-bottom: 16px;
    }

    .dropdown-content-custom {
        width: 100%;
    }

    .invoice-tabs {
        max-width: 100%;
    }

    .action-buttons {
        justify-content: center;
    }

    .row {
        flex-direction: column;
        gap: 16px;
    }

    .col-8,
    .col-4 {
        flex: 100%;
    }

    .table-responsive,
    form {
        height: auto;
        min-height: 200px;
        /* Adjusted for mobile */
    }

    :deep(.form-check-inline) {
        display: flex;
        margin-right: 0;
        margin-bottom: 8px;
    }
}

</style>