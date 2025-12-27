<template>
    <div class="header-container">
        <!-- Search Combo Box -->
        <div class="search-section">
            <a-dropdown v-model:open="dropdownVisible" :trigger="['click']" overlayClassName="product-dropdown">
                <a-input-search v-model:value="searchQuery" class="product-search-bar"
                    placeholder="Tìm kiếm sản phẩm theo tên..." @focus="handleSearchFocus" @search="performSearch"
                    size="large" style="width: 320px">
                    <template #enterButton>
                        <search-outlined style="font-size: 18px;" />
                    </template>
                </a-input-search>

                <template #overlay>
                    <div class="dropdown-content-custom">
                        <div v-if="filteredProducts.length === 0 && searchQuery.length > 0" class="empty-result">
                            Không tìm thấy sản phẩm phù hợp.
                        </div>
                        <div v-if="filteredProducts.length > 0">
                            <div v-for="(product) in filteredProducts" :key="product.id" class="product-option" :class="{
                                'out-of-stock-item': product.so_luong <= 0 && product.trang_thai !== false,
                                'inactive-item': product.trang_thai === false || product.trang_thai === 0
                            }" @click="handleDropdownClick(product)">

                                <img :src="product.hinh_anh || 'default-product.png'" alt="Product"
                                    class="product-image" />
                                <div class="product-info-split">
                                    <div class="info-left">
                                        <div class="product-name">
                                            {{ product.ten_san_pham }}
                                            <!-- Status badges -->
                                            <a-tag v-if="product.trang_thai === false || product.trang_thai === 0"
                                                color="red" style="margin-left: 8px;">
                                                Ngừng hoạt động
                                            </a-tag>
                                            <a-tag v-else-if="product.so_luong <= 0" color="orange"
                                                style="margin-left: 8px;">
                                                Hết hàng
                                            </a-tag>
                                        </div>
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
                                                    <span class="original-price">{{ formatCurrency(product.giaGoc) }}
                                                        VNĐ</span>
                                                    <span class="discount-badge">SALE</span>
                                                </div>
                                                <div class="current-price">{{ formatCurrency(product.gia_ban) }} VNĐ
                                                </div>
                                            </template>
                                            <!-- Nếu không có khuyến mãi: chỉ hiển thị giá bình thường -->
                                            <template v-else>
                                                <div class="product-price">{{ formatCurrency(product.gia_ban) }} VNĐ
                                                </div>
                                            </template>
                                        </div>
                                        <div class="product-stock">
                                            Tồn kho: <span
                                                :class="product.so_luong > 5 ? 'in-stock' : (product.so_luong > 0 ? 'low-stock' : 'no-stock')">{{
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
            <a-button type="primary" class="qr-scan-button" @click="showQrScanner" size="large"
                style="margin-left: 10px;">
                <template #icon>
                    <qrcode-outlined style="font-size: 18px;" />
                </template>
                <span style="font-weight: 500; margin-left: 6px;">Quét QR</span>
            </a-button>
        </div>

        <!-- Thêm modal cho quét QR -->
        <a-modal v-model:open="qrScannerVisible" title="Quét mã QR sản phẩm" @cancel="stopQrScanner" :footer="null">
            <div id="qr-reader" style="width: 100%;"></div>
        </a-modal>

        <!-- Invoice Tabs with Suspended Dropdown -->
        <div class="invoice-tabs" style="display: flex; align-items: center;">
            <a-tabs v-model:activeKey="activeKey" type="editable-card" @edit="onEdit" style="flex: 1;">
                <a-tab-pane v-for="pane in activeInvoices" :key="pane.key" :closable="pane.closable">
                    <template #tab>
                        <div class="invoice-tab-label">
                            <span class="product-count-badge"
                                :class="{ 'has-products': getInvoiceProductCount(pane) > 0 }">
                                {{ getInvoiceProductCount(pane) }}
                            </span>
                            <span class="tab-title">{{ pane.title }}</span>
                        </div>
                    </template>
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
                        <a-menu-item v-for="(invoice, index) in suspendedInvoices" :key="invoice.key"
                            @click="activateSuspendedInvoice(invoice.hd.id_hoa_don)">
                            <div
                                style="display: flex; justify-content: space-between; align-items: center; min-width: 250px; position: relative;">
                                <!-- Badge số lượng sản phẩm ở góc trái -->
                                <a-badge :count="getInvoiceProductCount(invoice)" :show-zero="true" :number-style="{
                                    backgroundColor: getInvoiceProductCount(invoice) > 0 ? '#52c41a' : '#d9d9d9',
                                    fontSize: '10px',
                                    minWidth: '18px',
                                    height: '18px',
                                    lineHeight: '18px'
                                }" style="margin-right: 8px;">
                                    <a-tooltip
                                        :title="getInvoiceProductCount(invoice) > 0 ? `${getInvoiceProductCount(invoice)} sản phẩm` : 'Chưa có sản phẩm'">
                                        <span style="display: inline-block; width: 8px;"></span>
                                    </a-tooltip>
                                </a-badge>
                                <span style="flex: 1;">
                                    <strong>{{ invoice.title }}</strong> - {{ invoice.hd.ma_hoa_don }}
                                </span>
                                <a-badge :count="`${getRemainingMinutes(invoice.hd.id_hoa_don)}p`" :number-style="{
                                    backgroundColor: getRemainingMinutes(invoice.hd.id_hoa_don) <= 5 ? '#ff4d4f' : '#faad14'
                                }" />
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
                    <table class="table cart-table">
                        <thead class="cart-table-header">
                            <tr>
                                <th style="width: 5%;">#</th>
                                <th style="width: 12%;">Ảnh</th>
                                <th style="width: 35%;">Tên sản phẩm</th>
                                <th style="width: 13%;">Số lượng</th>
                                <th style="width: 13%;">Đơn giá</th>
                                <th style="width: 13%;">Thành tiền</th>
                                <th style="width: 9%;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <!-- ✅ Warning banner for invalid items -->
                            <tr v-if="activeTabData && hasInvalidItems(activeTabData)" class="invalid-items-banner">
                                <td colspan="7">
                                    <a-alert type="warning" message="Có sản phẩm không hợp lệ trong giỏ hàng!"
                                        :description="getInvalidItemsMessage(activeTabData)" show-icon closable />
                                </td>
                            </tr>
                            <tr v-if="!activeTabData || !currentInvoiceItems || currentInvoiceItems.length === 0">
                                <td colspan="7" class="text-center" style="padding: 20px;">
                                    {{ !activeTabData ? 'Vui lòng chọn hoặc tạo hóa đơn.' : 'Chưa có sản phẩm nào.' }}
                                </td>
                            </tr>
                            <tr v-for="(item, index) in currentInvoiceItems" :key="item.id_chi_tiet_san_pham"
                                :class="{ 'inactive-product-row': isProductInactive(item) }">
                                <td class="text-center cart-index">{{ index + 1 }}</td>
                                <td class="text-center">
                                    <img class="cart-product-image" :src="item.hinh_anh || 'default-product.png'"
                                        alt="Item" />
                                </td>
                                <td>
                                    {{ item.ten_san_pham }} <br />
                                    <small>(Màu: {{ item.mau_sac }} - Size: {{ item.kich_thuoc }})</small>
                                    <!-- ✅ Status badges -->
                                    <div v-if="isItemInvalid(item)" class="item-status-badges">
                                        <a-tag v-if="isItemInactive(item)" color="red">Ngưng hoạt động</a-tag>
                                        <a-tag v-else-if="isItemOutOfStock(item)" color="orange">Hết hàng</a-tag>
                                        <a-tag v-else-if="isItemInsufficientStock(item)" color="gold">Không đủ
                                            hàng</a-tag>
                                    </div>
                                </td>
                                <td>
                                    <a-space direction="vertical">
                                        <a-input-number v-model:value="item.so_luong" :min="1"
                                            :max="getItemMaxQuantity(item)" :disabled="isItemInvalid(item)"
                                            @blur="handleQuantityBlur(item)" @change="handleQuantityChange(item)"
                                            style="width: 80px;" />
                                    </a-space>
                                </td>
                                <td class="cart-price-cell">{{ formatCurrency(item.gia_ban) }} đ</td>
                                <td class="cart-total-cell">{{ formatCurrency(item.gia_ban * item.so_luong) }} đ</td>
                                <td class="text-center">
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
                    <FormKhachHangBH :triggerUpdate="triggerUpdate"
                        @shippingFeeCalculated="handleShippingFeeCalculated"
                        @customerDataSaved="handleCustomerDataSaved" />
                </div>
            </div>
            <div class="col-4">
                <form v-if="activeTabData && activeTabData.hd" @submit.prevent="handlePayment">
                    <input type="hidden" v-model="activeTabData.hd.id_hoa_don">

                    <!-- Mã hóa đơn -->
                    <div class="invoice-info-card mb-3">
                        <div class="info-item">
                            <file-text-outlined class="info-icon" />
                            <div class="info-content">
                                <span class="info-label">Mã hóa đơn</span>
                                <span class="info-value">{{ activeTabData.hd.ma_hoa_don }}</span>
                            </div>
                        </div>
                    </div>

                    <!-- Tên khách hàng -->
                    <div class="invoice-info-card mb-3">
                        <div class="info-item">
                            <user-outlined class="info-icon" />
                            <div class="info-content">
                                <span class="info-label">Tên khách hàng</span>
                                <span class="info-value">
                                    {{ activeTabData.hd.ten_khach_hang || activeTabData.hd.ho_ten || 'Khách lẻ' }}
                                </span>
                            </div>
                            <a-space>
                                <a-button type="primary" size="small" class="select-customer-btn" @click="showModal">
                                    <template #icon>
                                        <edit-outlined />
                                    </template>
                                    Chọn
                                </a-button>
                                <a-button
                                    v-if="activeTabData.hd.ten_khach_hang && activeTabData.hd.ten_khach_hang !== 'Khách lẻ'"
                                    type="default" danger size="small" class="remove-customer-btn"
                                    @click="confirmBoChonKhachHang">
                                    <template #icon>
                                        <close-circle-outlined />
                                    </template>
                                    Bỏ chọn
                                </a-button>
                            </a-space>
                        </div>
                    </div> <!-- Closing invoice-info-card for customer -->

                    <!-- Phương thức nhận hàng -->
                    <div class="invoice-info-card mb-3">
                        <label class="shipping-method-label mb-3">
                            <car-outlined style="margin-right: 8px; color: #ff6600;" />
                            Phương thức nhận hàng
                        </label>
                        <div class="shipping-methods">
                            <label class="shipping-radio-card"
                                :class="{ 'active': activeTabData.hd.phuong_thuc_nhan_hang === 'Nhận tại cửa hàng' }">
                                <input class="form-check-input" type="radio" :name="'phuongThucNhanHang_' + activeKey"
                                    :id="'nhanTaiCuahang_' + activeKey" value="Nhận tại cửa hàng"
                                    v-model="activeTabData.hd.phuong_thuc_nhan_hang" @change="handlePhuongThucChange" />
                                <shop-outlined class="radio-icon" />
                                <span class="radio-text">Nhận tại cửa hàng</span>
                            </label>

                            <label class="shipping-radio-card"
                                :class="{ 'active': activeTabData.hd.phuong_thuc_nhan_hang === 'Giao hàng' }">
                                <input class="form-check-input" type="radio" :name="'phuongThucNhanHang_' + activeKey"
                                    :id="'giaoHang_' + activeKey" value="Giao hàng"
                                    v-model="activeTabData.hd.phuong_thuc_nhan_hang" @change="handlePhuongThucChange" />
                                <car-outlined class="radio-icon" />
                                <span class="radio-text">Giao hàng</span>
                            </label>
                        </div>

                        <div v-if="activeTabData.hd.phuong_thuc_nhan_hang === 'Giao hàng'" class="mt-3">
                            <div class="form-label-with-logo">
                                <label class="form-label">Phí vận chuyển (VNĐ)</label>
                                <img src="../../../images/logo/logo_GHTK.png" alt="GHTK Logo" class="ghtk-logo" />
                            </div>
                            <a-input-number v-model:value="activeTabData.hd.phi_van_chuyen" :min="0"
                                :formatter="value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                                :parser="value => value.replace(/\$\s?|(,*)/g, '')" placeholder="Nhập phí vận chuyển"
                                style="width: 100%" 
                                :readonly="true" />
                        </div>
                    </div> <!-- Closing invoice-info-card -->
                    <div class="mb-3">
                        <label class="form-label">Tổng tiền hàng:</label>
                        <input type="text" class="form-control" :value="formatCurrency(fe_tongTienHang) + ' ' + 'đ'"
                            disabled>
                    </div>
                    <div class="mb-3" v-if="activeTabData.hd.phuong_thuc_nhan_hang === 'Giao hàng'">
                        <label class="form-label">Phí vận chuyển:</label>
                        <input type="text" class="form-control" :value="formatCurrency(fe_phiVanChuyen) + 'đ'" disabled>
                    </div>
                    <div class="mb-3">
                        <label for="idVoucher" class="form-label voucher-label">
                            <gift-outlined style="margin-right: 8px; color: #ff6600;" />
                            Voucher
                        </label>
                        <a-select v-model:value="activeTabData.hd.id_voucher" class="voucher-select" size="large"
                            placeholder="Chọn voucher giảm giá" @change="updateVoucher(true)" style="width: 100%"
                            :options="voucherOptions">
                            <template #suffixIcon>
                                <gift-outlined style="color: #ff6600;" />
                            </template>
                        </a-select>
                    </div>
                    <div class="mb-3" v-if="fe_giamGia > 0">
                        <label class="form-label">Giảm từ Voucher:</label>
                        <input type="text" class="form-control text-success fw-bold"
                            :value="'-' + formatCurrency(fe_giamGia) + ' ' + 'đ'" disabled>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Tổng thanh toán:</label>
                        <input type="text" class="form-control fw-bold fs-5"
                            :value="formatCurrency(fe_tongThanhToan) + ' ' + 'đ'" disabled>
                    </div>
                    <div class="mb-3">
                        <label class="form-label d-block mb-2">Hình thức thanh toán</label>
                        <div class="payment-methods-grid">
                            <div class="payment-method-option"
                                :class="{ 'active': activeTabData.hd.hinh_thuc_thanh_toan === 'Tiền mặt' }">
                                <input class="form-check-input" type="radio" :name="'hinhThucThanhToan_' + activeKey"
                                    :id="'tienMat_' + activeKey" value="Tiền mặt"
                                    v-model="activeTabData.hd.hinh_thuc_thanh_toan" @change="updateHinhThucThanhToan" />
                                <label class="payment-label" :for="'tienMat_' + activeKey">
                                    <div class="payment-icon">💵</div>
                                    <div class="payment-text">Tiền mặt</div>
                                </label>
                            </div>
                            <div class="payment-method-option"
                                :class="{ 'active': activeTabData.hd.hinh_thuc_thanh_toan === 'PayOS' }">
                                <input class="form-check-input" type="radio" :name="'hinhThucThanhToan_' + activeKey"
                                    :id="'payos_' + activeKey" value="PayOS"
                                    v-model="activeTabData.hd.hinh_thuc_thanh_toan" @change="updateHinhThucThanhToan" />
                                <label class="payment-label" :for="'payos_' + activeKey">
                                    <div class="payment-icon">🏦</div>
                                    <div class="payment-text">PayOS</div>
                                </label>
                            </div>
                            <div class="payment-method-option"
                                :class="{ 'active': activeTabData.hd.hinh_thuc_thanh_toan === 'Chuyển khoản' }">
                                <input class="form-check-input" type="radio" :name="'hinhThucThanhToan_' + activeKey"
                                    :id="'zalopay_' + activeKey" value="Chuyển khoản"
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

                    <!-- ✅ NEW: Warning for inactive products -->
                    <a-alert v-if="hasInactiveProducts" type="error" show-icon style="margin-bottom: 16px;">
                        <template #message>
                            Có sản phẩm đã ngừng hoạt động trong giỏ hàng
                        </template>
                        <template #description>
                            Vui lòng xóa các sản phẩm không hoạt động để thanh toán
                        </template>
                    </a-alert>

                    <!-- ✅ Debug: Show button state -->
                    <div v-if="isPaymentDisabled" style="color: red; margin-bottom: 8px; font-size: 12px;">
                        ⚠️ Button bị vô hiệu hóa:
                        <div>- Có hóa đơn: {{ !!activeTabData?.hd?.id_hoa_don }}</div>
                        <div>- Số sản phẩm: {{ currentInvoiceItems.length }}</div>
                        <div>- Sản phẩm inactive: {{ hasInactiveProducts }}</div>
                        <div>- Hình thức TT: {{ activeTabData?.hd?.hinh_thuc_thanh_toan }}</div>
                        <div v-if="activeTabData?.hd?.hinh_thuc_thanh_toan === 'Tiền mặt'">
                            - Tiền khách đưa: {{ tienKhachDua }}
                        </div>
                    </div>

                    <!-- Nút thanh toán với điều kiện vô hiệu hóa -->
                    <button type="submit" class="btn btn-primary w-100" :disabled="isPaymentDisabled"
                        @click="console.log('🔘 Button clicked')">
                        Thanh toán
                    </button>
                    <!-- Modal 1: Xác nhận thanh toán -->
                    <a-modal v-model:open="showPaymentConfirm" :closable="false" :maskClosable="false" width="450px"
                        centered>
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
                                    <strong style="color: #ff6600; font-size: 16px;">{{ formatCurrency(fe_tongThanhToan)
                                        }}</strong>
                                </div>
                                <div v-if="activeTabData?.hd?.hinh_thuc_thanh_toan === 'Tiền mặt'"
                                    style="display: flex; justify-content: space-between;">
                                    <span style="color: #666;">Tiền trả khách:</span>
                                    <strong style="color: #52c41a;">{{ formatCurrency(calculatedChange) }}</strong>
                                </div>
                            </div>
                        </div>

                        <template #footer>
                            <a-button key="cancel" size="large" @click="cancelPayment" style="height: 40px;">
                                Hủy
                            </a-button>
                            <a-button key="ok" type="primary" size="large" @click="proceedToPayment"
                                style="height: 40px; background: #ff6600; border-color: #ff6600;">
                                Xác nhận thanh toán
                            </a-button>
                        </template>
                    </a-modal>

                    <!-- Modal 2: Xác nhận in hóa đơn (sau khi thanh toán) -->
                    <a-modal v-model:open="showPrintConfirm" :closable="false" :maskClosable="false" width="450px"
                        centered>
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
                            <div
                                style="background: #e6f7ff; padding: 16px; border-radius: 8px; border: 1px solid #91d5ff;">
                                <div style="display: flex; align-items: center; gap: 8px; color: #1890ff;">
                                    <CheckCircleOutlined style="font-size: 18px;" />
                                    <span style="font-weight: 500;">Đơn hàng {{ activeTabData?.hd?.ma_hoa_don }} đã được
                                        thanh toán</span>
                                </div>
                            </div>
                        </div>

                        <template #footer>
                            <a-button key="cancel" size="large" @click="confirmPrint(false)" style="height: 40px;">
                                Không in
                            </a-button>
                            <a-button key="ok" type="primary" size="large" @click="confirmPrint(true)"
                                style="height: 40px; background: #52c41a; border-color: #52c41a;">
                                In hóa đơn
                            </a-button>
                        </template>
                    </a-modal>
                </form>

                <div v-else class="text-center text-muted mt-5">
                    Vui lòng chọn hoặc tạo một hóa đơn.
                </div>

                <!-- Modal chọn khách hàng - đặt ngoài v-if/v-else -->
                <a-modal v-model:open="open" class="customer-select-modal" width="1100px" :footer="null">
                    <template #title>
                        <div class="modal-custom-title">
                            <team-outlined class="title-icon" />
                            <span>Danh sách khách hàng</span>
                        </div>
                    </template>

                    <!-- Thanh tìm kiếm đẹp -->
                    <div class="customer-search-section mb-4">
                        <a-input-search v-model:value="searchQueryKH" class="customer-search-input" size="large"
                            placeholder="Tìm kiếm theo tên hoặc số điện thoại..." @input="handleSearch">
                            <template #prefix>
                                <search-outlined style="color: #ff6600;" />
                            </template>
                        </a-input-search>
                    </div>

                    <div v-if="filteredKhachHang.length === 0" class="text-center py-5">
                        <a-empty :image="simpleImage" description="Không tìm thấy khách hàng" />
                    </div>

                    <div v-else class="customer-table-wrapper">
                        <div class="table-responsive" ref="scrollContainer" style="max-height: 450px; overflow-y: auto"
                            @scroll="handleScroll">
                            <table class="table customer-table">
                                <thead>
                                    <tr>
                                        <th scope="col" class="text-center" style="width: 50px;">STT</th>
                                        <th scope="col" style="width: 200px;">
                                            <user-outlined style="margin-right: 6px;" />
                                            Tên khách hàng
                                        </th>
                                        <th scope="col" class="text-center" style="width: 90px;">Giới tính</th>
                                        <th scope="col" class="text-center" style="width: 120px;">
                                            <phone-outlined style="margin-right: 6px;" />
                                            SĐT
                                        </th>
                                        <th scope="col">
                                            <environment-outlined style="margin-right: 6px;" />
                                            Địa chỉ
                                        </th>
                                        <th scope="col" class="text-center"
                                            style="width: 110px; padding-right: 20px !important;">Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr v-for="(khachHang, index) in filteredKhachHang" :key="khachHang.idKhachHang"
                                        class="customer-row">
                                        <td class="text-center">{{ index + 1 }}</td>
                                        <td class="customer-name">{{ khachHang.hoTen }}</td>
                                        <td class="text-center">
                                            <a-tag :color="khachHang.gioiTinh ? 'blue' : 'pink'">
                                                {{ khachHang.gioiTinh ? "Nam" : "Nữ" }}
                                            </a-tag>
                                        </td>
                                        <td class="text-center">{{ khachHang.soDienThoai }}</td>
                                        <td class="customer-address">{{ khachHang.diaChi }}</td>
                                        <td class="text-center">
                                            <a-button type="primary" size="small" class="select-btn"
                                                @click="chonKhachHang(khachHang)">
                                                <template #icon>
                                                    <check-circle-outlined />
                                                </template>
                                                Chọn
                                            </a-button>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </a-modal>
            </div>
        </div>
    </div>

</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, onUnmounted, nextTick, h } from 'vue';
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
    CheckCircleOutlined,
    GiftOutlined,
    FileTextOutlined,
    UserOutlined,
    EditOutlined,
    CarOutlined,
    ShopOutlined,
    TeamOutlined,
    PhoneOutlined,
    EnvironmentOutlined,
    CloseCircleOutlined
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
import { banHangService } from '@/services/banHangService';
import FormKhachHangBH from './formKhachHangBH.vue';
import { useRouter } from 'vue-router';
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

// ✅ PAYMENT PROCESSING FLAG - Ngăn auto-apply voucher khi đang thanh toán
const isProcessingPayment = ref(false);

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
        // ✅ Xóa khách lẻ (nếu có) trước khi chọn khách TK
        const walkInCustomer = localStorage.getItem('walkInCustomer');
        if (walkInCustomer) {
            console.log('⚠️ Phát hiện khách lẻ → Xóa để chọn khách có TK');
            localStorage.removeItem('walkInCustomer');
        }
        
        Object.assign(activeTabData.value.hd, {
            ten_khach_hang: khachHang.hoTen,
            so_dien_thoai: khachHang.soDienThoai,
            dia_chi: khachHang.diaChi || 'Chưa có địa chỉ',
            id_khach_hang: khachHang.idKhachHang
        });

        await store.addKHHD(
            activeTabData.value.hd.id_hoa_don,
            khachHang.idKhachHang,
            khachHang.diaChi,
            khachHang.hoTen,
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

        message.success(`Đã chọn khách hàng: ${khachHang.hoTen}`);
        triggerUpdate.value = Date.now();
    } catch (error) {
        console.error('Lỗi khi chọn khách hàng:', error);
        message.error('Không thể chọn khách hàng. Vui lòng thử lại!');
    }
};

// Hàm xác nhận bỏ chọn khách hàng
const confirmBoChonKhachHang = () => {
    Modal.confirm({
        title: () => h('div', { style: 'display: flex; align-items: center; gap: 10px;' }, [
            h(CloseCircleOutlined, { style: 'color: #ff4d4f; font-size: 22px;' }),
            h('span', { style: 'font-size: 16px; font-weight: 600;' }, 'Bỏ chọn khách hàng')
        ]),
        content: () => h('div', { style: 'padding: 8px 0;' }, [
            h('p', { style: 'margin: 0; font-size: 14px;' }, 'Bạn có chắc chắn muốn bỏ chọn khách hàng này không?'),
            h('p', { style: 'margin: 8px 0 0 0; font-size: 13px; color: #666;' }, 'Hóa đơn sẽ được chuyển về trạng thái "Khách lẻ" và bỏ phí vận chuyển.')
        ]),
        okText: 'Bỏ chọn',
        cancelText: 'Hủy',
        okButtonProps: { danger: true, size: 'large', style: { height: '38px' } },
        cancelButtonProps: { size: 'large', style: { height: '38px' } },
        centered: true,
        width: 450,
        onOk: () => {
            boChonKhachHang();
        },
    });
};

// Hàm bỏ chọn khách hàng
const boChonKhachHang = async () => {
    try {
        const idHoaDon = activeTabData.value.hd.id_hoa_don;

        // Gọi API để reset khách hàng về khách lẻ
        await store.removeCustomerFromHD(idHoaDon);

        // Cập nhật UI
        Object.assign(activeTabData.value.hd, {
            ten_khach_hang: 'Khách lẻ',
            ho_ten: 'Khách lẻ',
            so_dien_thoai: null,
            dia_chi: null,
            email: null,
            id_khach_hang: null,
            phuong_thuc_nhan_hang: 'Nhận tại cửa hàng',
            phi_van_chuyen: 0
        });

        // Xóa localStorage
        localStorage.removeItem('khachHangBH');
        localStorage.removeItem('chonKH');
        localStorage.removeItem('luuTTKHBH');
        localStorage.removeItem('shippingFeeUpdated');
        localStorage.removeItem('calculatedShippingFee');  // ← Thêm dòng này

        // Reload hóa đơn để cập nhật tổng tiền
        await refreshHoaDon(idHoaDon);

        message.success('Đã bỏ chọn khách hàng và chuyển về khách lẻ');

        // Trigger update cho form khách hàng
        triggerUpdate.value = Date.now();

        // Reset ptnh về nhận tại cửa hàng
        ptnh.value = 'Nhận tại cửa hàng';

    } catch (error) {
        console.error('Lỗi khi bỏ chọn khách hàng:', error);
        message.error('Không thể bỏ chọn khách hàng. Vui lòng thử lại!');
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
        const normalizedName = normalizeString(khachHang.hoTen);
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

    // ✅ Filter out zero-stock products FIRST
    let availableProducts = allProducts.value.filter(product => product.so_luong > 0);
    console.log('📦 Available products (stock > 0):', availableProducts.length);

    if (!searchQuery.value) {
        console.log('✅ Returning all available products:', availableProducts.length);
        return availableProducts;
    }

    const normalizedQuery = normalizeString(searchQuery.value);
    const filtered = availableProducts.filter(product => {
        const normalizedProductName = normalizeString(product.ten_san_pham);
        return normalizedProductName.includes(normalizedQuery);
    });
    console.log('✅ Filtered products:', filtered.length);
    return filtered;
});


// ✅ QUY TẮC MỚI: Luôn reload products khi click vào search
const handleSearchFocus = async () => {
    console.log('🔍 Search focused - Reloading fresh product data...');
    try {
        const freshProducts = await store.getAllCTSPKM();
        allProducts.value = freshProducts;
        console.log(`✅ Loaded ${freshProducts.length} products from API`);
    } catch (error) {
        console.error('❌ Error loading products:', error);
        message.error('Lỗi tải danh sách sản phẩm');
    }
};


// Lấy dữ liệu của tab đang active
const activeTabData = computed(() => {
    return panes.value.find(pane => pane.key === activeKey.value);
});

const currentInvoiceItems = computed(() => {
    return activeTabData.value?.items?.value || [];
});

// ✅ Get realtime stock từ backend API
const getCTSPRealtime = async (idCTSP) => {
    try {
        const response = await store.getCTSPRealtime(idCTSP);
        return response;
    } catch (error) {
        console.error('❌ Error getting realtime stock:', error);
        return null;
    }
};

// ✅ Tính max quantity - GỌI API REALTIME
const getMaxQuantity = async (item) => {
    // Gọi API để lấy stock mới nhất
    const realtimeData = await getCTSPRealtime(item.id_chi_tiet_san_pham);

    if (!realtimeData) {
        console.warn(`⚠️ Cannot get realtime data for ${item.ten_san_pham}`);
        return item.so_luong || 1; // Fallback to current quantity
    }

    // Max = Stock hiện tại trong DB + Số lượng hiện tại trong giỏ
    // Lưu ý: Stock trong DB đã bị trừ khi thêm vào giỏ, nên cần cộng lại
    const currentStock = realtimeData.so_luong || 0;
    const cartQuantity = item.so_luong || 0;
    const maxQty = currentStock + cartQuantity;

    console.log(`📊 Max for ${item.ten_san_pham}: stock=${currentStock}, cart=${cartQuantity}, max=${maxQty}`);
    return maxQty > 0 ? maxQty : cartQuantity; // Nếu stock = 0, giữ nguyên số lượng hiện tại
};

// ✅ ĐỒNG BỘ - Tính max quantity cho input-number :max attribute
// QUAN TRỌNG: Max phải CỐ ĐỊNH dựa trên stock hiện tại trong DB
// Không sử dụng item.so_luong vì nó thay đổi khi user nhập
const getItemMaxQuantity = (item) => {
    // so_luong_ton hoặc so_luong_ton_kho = stock HIỆN TẠI trong DB (đã trừ khi thêm vào giỏ)
    // Lấy giá trị _originalMax nếu đã được cache, hoặc tính mới
    if (item._originalMax !== undefined) {
        console.log(`📊 getItemMaxQuantity (cached): max=${item._originalMax}`);
        return item._originalMax;
    }

    const stockInDB = item.so_luong_ton ?? item.so_luong_ton_kho ?? 0;
    const cartQty = item.so_luong || 0;

    // Max = Stock còn lại + số đang trong giỏ (tính 1 lần)
    const max = stockInDB + cartQty;

    // Cache lại để không tính lại
    item._originalMax = max > 0 ? max : 1;

    console.log(`📊 getItemMaxQuantity (new): stock=${stockInDB}, cart=${cartQty}, max=${item._originalMax}`);
    return item._originalMax;
};

// ✅ Validate và auto-correct quantity khi user thay đổi
const validateAndCorrectQuantity = (item) => {
    console.log(`🔍 Validating quantity for ${item.ten_san_pham}:`, item.so_luong);

    const maxQty = getItemMaxQuantity(item);
    console.log(`📊 Max allowed: ${maxQty}, Current: ${item.so_luong}`);

    // Nếu số lượng vượt quá max, tự động chuyển về max
    if (item.so_luong > maxQty) {
        const oldQuantity = item.so_luong;
        item.so_luong = maxQty;

        console.log(`⚠️ EXCEEDED! Auto-correcting ${oldQuantity} → ${maxQty}`);

        message.warning(
            `Không thể tăng quá ${maxQty} sản phẩm! Đã tự động điều chỉnh về ${maxQty}.`,
            4
        );
    } else if (item.so_luong === maxQty) {
        // ✅ Thông báo khi đạt max
        console.log(`✅ REACHED MAX: ${item.so_luong} = ${maxQty}`);
        message.info(
            `Đã đạt số lượng tối đa! Kho chỉ còn ${maxQty} sản phẩm.`,
            3
        );
    } else {
        console.log(`✅ Quantity OK: ${item.so_luong} <= ${maxQty}`);
    }

    // Cập nhật tổng tiền
    updateItemTotal(item);
};

// ✅ NEW: Handle quantity change - chạy mỗi khi user thay đổi số lượng (realtime)
let quantityChangeTimer = null;
const handleQuantityChange = async (item) => {
    // Debounce: chờ 300ms sau khi user ngừng nhập
    if (quantityChangeTimer) clearTimeout(quantityChangeTimer);

    quantityChangeTimer = setTimeout(async () => {
        const maxQty = getItemMaxQuantity(item);

        if (item.so_luong > maxQty) {
            item.so_luong = maxQty;
            message.warning(`Số lượng tối đa: ${maxQty}`);
        }

        if (!item.so_luong || item.so_luong < 1) {
            item.so_luong = 1;
        }
    }, 300);
};

// ✅ Handle khi user rời khỏi input (blur) - GỌI API VÀ RELOAD
const handleQuantityBlur = async (item) => {
    // Lấy max từ sync function (nhanh hơn)
    const maxQty = getItemMaxQuantity(item);

    // Validate min
    if (!item.so_luong || item.so_luong < 1) {
        item.so_luong = 1;
        message.info('Số lượng tối thiểu là 1');
    }

    // Validate max
    if (item.so_luong > maxQty) {
        item.so_luong = maxQty;
        message.warning(`Đã điều chỉnh về số lượng tối đa: ${maxQty}`);
    }

    try {
        // Gọi API update
        const result = await store.setSPHD(item.id_hoa_don, item.id_chi_tiet_san_pham, item.so_luong);

        if (!result || result.error) {
            message.error(result?.message || 'Cập nhật số lượng thất bại');
        }
    } catch (error) {
        console.error('Lỗi cập nhật số lượng:', error);
        message.error('Lỗi khi cập nhật số lượng');
    } finally {
        // ✅ LUÔN reload cart từ backend để đảm bảo data đồng bộ
        await reloadCartFromBackend(item.id_hoa_don);
        store.getAllCTSPKM().then(p => allProducts.value = p);
    }
};

// ✅ Validation: Check if item is FULLY INVALID (chỉ khi inactive - admin đã tắt)
// CHÚ Ý: Không block sản phẩm hết hàng - vẫn cho phép giảm số lượng
const isItemInvalid = (item) => {
    // CHỈ block khi sản phẩm thực sự inactive, KHÔNG block khi chỉ hết stock
    return isItemInactive(item);
};

// ✅ Check if product is INACTIVE (admin đã tắt trạng thái)
// CHỈ dựa vào trang_thai từ backend response, KHÔNG dựa vào allProducts
const isItemInactive = (item) => {
    // Chỉ check backend response fields - đây là nguồn đáng tin cậy nhất
    // trang_thai_ctsp và trang_thai_san_pham được trả về từ API getSPGH
    if (item.trang_thai_ctsp === false || item.trang_thai_ctsp === 0) {
        return true;
    }
    if (item.trang_thai_san_pham === false || item.trang_thai_san_pham === 0) {
        return true;
    }

    return false;
};

// ✅ Check if product is OUT OF STOCK (hết hàng - stock = 0)
const isItemOutOfStock = (item) => {
    // Cách 1: Check từ backend response (so_luong_ton_kho)
    if (item.so_luong_ton_kho !== undefined && item.so_luong_ton_kho <= 0) {
        return true;
    }

    // Cách 2: Nếu không tìm thấy trong allProducts (do query filter stock > 0), coi như hết hàng
    const product = allProducts.value.find(p => p.id_chi_tiet_san_pham === item.id_chi_tiet_san_pham);
    if (!product) {
        // Sản phẩm không có trong allProducts = stock = 0 (đã thêm hết vào giỏ)
        return true;
    }

    // Cách 3: Check stock từ allProducts
    return product.so_luong <= 0;
};

// ✅ Check if stock is insufficient for current cart quantity
const isItemInsufficientStock = (item) => {
    const product = allProducts.value.find(p => p.id_chi_tiet_san_pham === item.id_chi_tiet_san_pham);
    if (!product) return true; // Không có trong list = không đủ

    return product.so_luong < item.so_luong;
};

// ✅ Check if tab has any invalid items
const hasInvalidItems = (tab) => {
    if (!tab?.items?.value) return false;
    return tab.items.value.some(item => isItemInvalid(item));
};

// ✅ Get message describing invalid items
const getInvalidItemsMessage = (tab) => {
    if (!tab?.items?.value) return '';

    const invalidItems = tab.items.value.filter(item => isItemInvalid(item));
    const inactiveCount = invalidItems.filter(item => isItemInactive(item)).length;
    const outOfStockCount = invalidItems.filter(item => isItemOutOfStock(item)).length;

    let messages = [];
    if (inactiveCount > 0) messages.push(`${inactiveCount} sản phẩm ngưng hoạt động`);
    if (outOfStockCount > 0) messages.push(`${outOfStockCount} sản phẩm hết hàng`);

    return `${messages.join(', ')}. Vui lòng xóa để tiếp tục thanh toán.`;
};

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

// Lấy tổng số lượng sản phẩm trong hóa đơn (cộng tất cả so_luong)
const getInvoiceProductCount = (invoice) => {
    if (!invoice || !invoice.items || !invoice.items.value) return 0;
    // Tính tổng số lượng của tất cả sản phẩm trong hóa đơn
    return invoice.items.value.reduce((total, item) => total + (item.so_luong || 0), 0);
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

/**
 * ✅ Helper: Reload cart items từ backend (Single Source of Truth)
 * Gọi sau mỗi thao tác thêm/sửa/xóa để đảm bảo data luôn đồng bộ
 */
const reloadCartFromBackend = async (idHoaDon) => {
    await store.getAllSPHD(idHoaDon);
    const currentTab = activeTabData.value;
    if (currentTab) {
        currentTab.items.value = store.getAllSPHDArr.map(item => ({
            id_hoa_don: item.id_hoa_don,
            id_chi_tiet_san_pham: item.id_chi_tiet_san_pham,
            hinh_anh: item.hinh_anh,
            ten_san_pham: item.ten_san_pham,
            mau_sac: item.ten_mau_sac,
            kich_thuoc: item.gia_tri,
            so_luong: item.so_luong,
            gia_ban: item.gia_ban,
            tong_tien: item.don_gia,
            trang_thai_ctsp: item.trang_thai_ctsp,
            trang_thai_san_pham: item.trang_thai_san_pham,
            so_luong_ton_kho: item.so_luong_ton_kho
        }));
    }
};

// ✅ Helper function to check if product is inactive (FIXED)
const isProductInactive = (item) => {
    const isInactive = (status) => {
        // null/undefined = không có data, coi như active (không block)
        if (status === null || status === undefined) return false;

        // Boolean: false = inactive, true = active
        if (typeof status === 'boolean') return status === false;

        // String: "false" hoặc "0" = inactive
        if (typeof status === 'string') {
            const lower = status.toLowerCase();
            return lower === 'false' || lower === '0';
        }

        // Number: 0 = inactive
        if (typeof status === 'number') return status === 0;

        return false; // default: active
    };

    // Product inactive nếu CTSP HOẶC Product inactive
    const ctspInactive = isInactive(item.trang_thai_ctsp);
    const productInactive = isInactive(item.trang_thai_san_pham);

    // Debug log
    if (ctspInactive || productInactive) {
        console.log('🔴 Inactive product detected:', {
            ten_san_pham: item.ten_san_pham,
            trang_thai_ctsp: item.trang_thai_ctsp,
            trang_thai_san_pham: item.trang_thai_san_pham,
            ctspInactive,
            productInactive
        });
    }

    return ctspInactive || productInactive;
};


// Thêm sản phẩm vào hóa đơn chi tiết của tab hiện tại
const handleDropdownClick = async (product) => {
    if (!dropdownVisible.value) return; // Ngăn nếu dropdown đang ẩn

    // ✅ Block sản phẩm ngừng hoạt động
    if (product.trang_thai === false || product.trang_thai === 0) {
        message.error('Sản phẩm này đã ngừng hoạt động!');
        return;
    }

    // ✅ Block sản phẩm hết hàng
    if (product.so_luong <= 0) {
        message.warning('Sản phẩm này đã hết hàng!');
        return;
    }

    await addToBill(product);

    // ✅ QUY TẮC MỚI: Reload dữ liệu sau khi chọn sản phẩm
    await handleSearchFocus();
};

// ✅ Thêm biến chống spam click
let isAdding = false;
let lastClickTime = 0;
const CLICK_DELAY = 500; // ms - thời gian chờ giữa 2 lần click

const addToBill = async (product) => {
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

    // ✅ Check product status before adding
    const isActiveProduct = (status) => {
        if (status === true || status === 1 || status === '1') return true;
        if (status === false || status === 0 || status === '0' || status === 'false') return false;
        return true;
    };

    if (!isActiveProduct(product.trang_thai)) {
        message.error(`Sản phẩm "${product.ten_san_pham}" không còn hoạt động. Đang tải lại danh sách...`);
        store.getAllCTSPKM().then(p => {
            allProducts.value = p;
            message.info('Đã cập nhật danh sách sản phẩm');
        });
        isAdding = false;
        return;
    }

    if (product.so_luong <= 0) {
        message.warning(`Sản phẩm "${product.ten_san_pham}" đã hết hàng!`);
        isAdding = false;
        return;
    }

    // ✅ Safety check - auto-initialize items if not exists
    if (!currentTab.items) {
        currentTab.items = ref([]);
    }
    if (!currentTab.items.value) {
        currentTab.items.value = [];
    }

    try {
        // ✅ REFACTORED: GỌI API TRƯỚC - KHÔNG optimistic UI
        const result = await store.themSPHDMoi(
            currentTab.hd.id_hoa_don,
            product.id_chi_tiet_san_pham,
            1
        );

        if (!result) {
            throw new Error("Thêm sản phẩm thất bại");
        }

        // ✅ SAU KHI THÀNH CÔNG: Reload từ backend
        await reloadCartFromBackend(currentTab.hd.id_hoa_don);
        await refreshHoaDon(currentTab.hd.id_hoa_don);
        store.getAllCTSPKM().then(p => allProducts.value = p);

        message.success(`Đã thêm "${product.ten_san_pham}"`);
        dropdownVisible.value = false;
        searchQuery.value = '';

    } catch (error) {
        console.error('Lỗi khi thêm sản phẩm:', error);
        message.error('Lỗi: Không thể thêm sản phẩm vào hóa đơn.');
    } finally {
        isAdding = false;
    }
};


const tienKhachDua = ref(0);

// Tính toán tiền thừa trả khách (calculatedChange) dựa trên tong_tien_sau_giam
const calculatedChange = computed(() => {
    const total = fe_tongThanhToan.value || 0;
    const cash = tienKhachDua.value || 0;
    return cash >= total ? cash - total : 0;
});

// ✅ NEW: Check for inactive products (handles both string and boolean)
const hasInactiveProducts = computed(() => {
    return currentInvoiceItems.value.some(item => {
        // Helper to check if status is inactive
        const isInactive = (status) => {
            if (status === null || status === undefined) return false; // No data = active
            if (typeof status === 'boolean') return status === false;  // false = inactive
            if (typeof status === 'string') {
                const lower = status.toLowerCase();
                return lower === 'false' || lower === '0';
            }
            if (typeof status === 'number') return status === 0;
            return false;
        };

        return isInactive(item.trang_thai_ctsp) || isInactive(item.trang_thai_san_pham);
    });
});

const isPaymentDisabled = computed(() => {
    if (!activeTabData.value?.hd?.id_hoa_don) return true;
    if (currentInvoiceItems.value.length === 0) return true;

    // ✅ NEW: Block payment if has inactive products
    if (hasInactiveProducts.value) return true;

    // Nếu là tiền mặt, kiểm tra tiền khách đưa
    if (activeTabData.value?.hd?.hinh_thuc_thanh_toan === 'Tiền mặt') {
        if (!tienKhachDua.value) return true;
        if (calculatedChange.value < 0) return true;
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

// Computed cho voucher options với format đẹp
const voucherOptions = computed(() => {
    const options = [
        {
            value: null,
            label: '-- Không dùng voucher --'
        }
    ];

    availableVouchers.value.forEach(voucher => {
        options.push({
            value: voucher.id_voucher,
            label: `🎁 ${voucher.ten_voucher} - Giảm ${formatCurrency(voucher.so_tien_giam)}đ`
        });
    });

    return options;
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

    // ❌ KHÔNG auto-apply voucher khi đang trong quá trình thanh toán
    if (isProcessingPayment.value) {
        console.log('🛑 Đang trong quá trình thanh toán, bỏ qua auto-apply voucher');
        return;
    }

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
        // ✅ FIX: Không reset flag - tôn trọng lựa chọn của user
        await updateVoucher(false); // false = không phải manual action
    }
    // Kịch bản 2: Chưa có voucher, nhưng giờ đã đủ điều kiện cho voucher tốt nhất
    else if (!currentVoucherId && bestVoucher) {
        currentTab.hd.id_voucher = bestVoucher.id_voucher; // Tự động áp dụng trên giao diện
        message.success(`Đã tự động áp dụng voucher: ${bestVoucher.ten_voucher}`);
        // ✅ FIX: Không reset flag - nếu user đã bỏ chọn, không tự động apply lại
        await updateVoucher(false); // false = không phải manual action
    }
});





// Cập nhật tổng tiền khi số lượng thay đổi trong bảng hóa đơn
const updateItemTotal = (item) => {
    // Gửi yêu cầu cập nhật lên backend ở chế độ nền
    store.setSPHD(item.id_hoa_don, item.id_chi_tiet_san_pham, item.so_luong)
        .then(() => {
            console.log(`✅ Updated quantity for ${item.ten_san_pham} to ${item.so_luong} on backend.`);
            // Sau khi backend cập nhật thành công, làm mới lại dữ liệu của hóa đơn
            refreshHoaDon(item.id_hoa_don);

            // ✅ Reload allProducts để cập nhật stock trong search bar
            store.getAllCTSPKM().then(p => {
                allProducts.value = p;
                console.log(`🔄 Reloaded allProducts after quantity update`);
            });
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
            // Đồng bộ lại hóa đơn
            refreshHoaDon(currentTab.hd.id_hoa_don);
            // ✅ RELOAD products để cập nhật stock và getMaxQuantity chính xác
            store.getAllCTSPKM().then(p => allProducts.value = p);
        })
        .catch(error => {
            console.error('Lỗi khi xóa sản phẩm (backend):', error);
            message.error('Lỗi: Không thể xóa sản phẩm.');
            // --- Hoàn tác lại thay đổi trên UI nếu có lỗi ---
            itemsArray.splice(itemIndex, 0, removedItem); // Thêm lại item vào vị trí cũ
        });
};

// ✅ Watch activeTabData để lưu ID hóa đơn hiện tại vào localStorage
watch(
    activeTabData,
    (newData) => {
        if (newData?.hd?.id_hoa_don) {
            localStorage.setItem('currentInvoiceId', newData.hd.id_hoa_don.toString());
            console.log('💾 Đã lưu currentInvoiceId vào localStorage:', newData.hd.id_hoa_don);
        } else {
            localStorage.removeItem('currentInvoiceId');
        }
    },
    { deep: true }
);

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
    doc.text("Cửa hàng MenWear", 105, 55, { align: "center" });
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

// Hàm xử lý thanh toán - Bước 1: Validate và hiển thị modal
const handlePayment = async () => {
    console.log('💰 handlePayment được gọi!');
    
    const currentTab = activeTabData.value;
    console.log('💰 Current tab:', currentTab);
    
    if (!currentTab || !currentTab.hd?.id_hoa_don) {
        message.error('Không tìm thấy hóa đơn!');
        return;
    }

    // ✅ Kiểm tra có sản phẩm không
    if (!currentTab.items?.value || currentTab.items.value.length === 0) {
        message.error('Vui lòng thêm sản phẩm vào hóa đơn trước khi thanh toán!');
        return;
    }

    // ✅ Kiểm tra thông tin khách hàng
    const tenKhachHang = currentTab.hd.ho_ten;
    const isWalkInCustomer = !currentTab.hd.id_khach_hang || tenKhachHang === 'Khách lẻ';
    
    console.log('👤 Tên khách hàng:', tenKhachHang);
    console.log('👤 Là khách lẻ?', isWalkInCustomer);

    if (isWalkInCustomer) {
        // Kiểm tra localStorage có thông tin khách lẻ không
        const walkInData = localStorage.getItem('walkInCustomer');
        console.log('💾 walkInCustomer từ localStorage:', walkInData);

        if (!walkInData) {
            console.error('❌ RETURN: Không có walkInCustomer trong localStorage');
            message.error('Vui lòng nhập và lưu thông tin khách hàng trước khi thanh toán!');
            return;
        }

        try {
            const customerData = JSON.parse(walkInData);
            console.log('✅ Parse customer data thành công:', customerData);

            // Validate thông tin cơ bản
            if (!customerData.ten_khach_hang || !customerData.sdt) {
                console.error('❌ RETURN: Thông tin khách hàng không đầy đủ');
                message.error('Thông tin khách hàng chưa đầy đủ. Vui lòng nhập lại!');
                localStorage.removeItem('walkInCustomer');
                return;
            }

            // Nếu chọn giao hàng, validate địa chỉ
            console.log('🚚 Phương thức nhận hàng:', currentTab.hd.phuong_thuc_nhan_hang);
            if (currentTab.hd.phuong_thuc_nhan_hang === 'Giao hàng') {
                console.log('📍 Checking address for delivery...', customerData.dia_chi_list);

                if (!customerData.dia_chi_list || customerData.dia_chi_list.length === 0) {
                    message.error('Vui lòng nhập địa chỉ giao hàng!');
                    return;
                }

                // ✅ FIX: Check both trangThai and diaChiMacDinh fields
                const defaultAddress = customerData.dia_chi_list.find(dc => dc.trangThai || dc.diaChiMacDinh);

                console.log('📍 Default address found:', defaultAddress);

                if (!defaultAddress) {
                    message.error('Vui lòng chọn địa chỉ mặc định!');
                    return;
                }

                if (!defaultAddress.tinhThanhPho || !defaultAddress.quanHuyen) {
                    message.error('Địa chỉ giao hàng chưa đầy đủ!');
                    console.error('❌ Missing fields:', {
                        tinhThanhPho: defaultAddress.tinhThanhPho,
                        quanHuyen: defaultAddress.quanHuyen,
                        xaPhuong: defaultAddress.xaPhuong,
                        soNha: defaultAddress.soNha
                    });
                    return;
                }

                console.log('✅ Address validation passed!');
            }

            console.log('✅ Thông tin khách lẻ hợp lệ:', customerData);
        } catch (error) {
            console.error('Lỗi parse customer data:', error);
            message.error('Dữ liệu khách hàng không hợp lệ!');
            localStorage.removeItem('walkInCustomer');
            return;
        }
    }


    // ✅ Tiếp tục thanh toán
    showPaymentConfirm.value = true;
};

// Hủy thanh toán
const cancelPayment = () => {
    showPaymentConfirm.value = false;
    // ✅ RESET FLAG: User hủy thanh toán
    isProcessingPayment.value = false;
    console.log('🚫 User hủy thanh toán - Tắt isProcessingPayment flag');
};

// Bước 2: Xác nhận thanh toán -> Thực hiện thanh toán -> Hiển thị modal in hóa đơn
const proceedToPayment = async () => {
    showPaymentConfirm.value = false;

    // ✅ BẬT FLAG: Bắt đầu quá trình thanh toán
    isProcessingPayment.value = true;
    console.log('🚀 Bắt đầu quá trình thanh toán - Bật isProcessingPayment flag');

    const hinhThuc = activeTabData.value.hd.hinh_thuc_thanh_toan;

    // Thực hiện thanh toán
    try {
        if (hinhThuc === "Tiền mặt") {
            await store.trangThaiDonHang(activeTabData.value.hd.id_hoa_don);
            // Sau khi thanh toán thành công -> hiển thị modal in hóa đơn
            showPrintConfirm.value = true;
            // ✅ RESET FLAG: Thanh toán tiền mặt thành công
            isProcessingPayment.value = false;
            console.log('💰 Thanh toán tiền mặt thành công - Tắt isProcessingPayment flag');
        } else if (hinhThuc === "PayOS") {
            // Validate payment amount - USE computed property!
            const paymentAmount = fe_tongThanhToan.value;

            if (paymentAmount <= 0) {
                message.error('Số tiền thanh toán không hợp lệ. Vui lòng thêm sản phẩm vào hóa đơn!');
                console.error('Invalid payment amount:', paymentAmount);
                console.log('Debug - Tổng hàng:', fe_tongTienHang.value);
                console.log('Debug - Giảm giá:', fe_giamGia.value);
                console.log('Debug - Phí ship:', fe_phiVanChuyen.value);
                // ✅ RESET FLAG: Lỗi validation
                isProcessingPayment.value = false;
                console.log('🚫 Lỗi PayOS - Tắt isProcessingPayment flag');
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
            
            // ✅ RESET FLAG: PayOS thanh toán thành công
            isProcessingPayment.value = false;
            console.log('💳 PayOS thành công - Tắt isProcessingPayment flag');

        } else if (hinhThuc === "Chuyển khoản") {
            // ✅ PHASE 1: Đồng bộ dữ liệu trước khi thanh toán
            showPaymentConfirm.value = false;
            
            try {
                // 1. Validate payment amount cơ bản
                const paymentAmount = fe_tongThanhToan.value;
                if (paymentAmount <= 0) {
                    message.error('Số tiền thanh toán không hợp lệ. Vui lòng thêm sản phẩm vào hóa đơn!');
                    console.error('Invalid payment amount:', paymentAmount);
                    // ✅ RESET FLAG: Lỗi validation ZaloPay
                    isProcessingPayment.value = false;
                    console.log('🚫 Lỗi ZaloPay validation - Tắt isProcessingPayment flag');
                    return;
                }

                // 2. Validate phí vận chuyển cho ZaloPay
                if (activeTabData.value.hd.phuong_thuc_nhan_hang === "Giao hàng") {
                    const phiVanChuyen = activeTabData.value.hd.phi_van_chuyen || 0;
                    if (!phiVanChuyen || phiVanChuyen === 0) {
                        message.error("Phí vận chuyển chưa được tính. Vui lòng chọn địa chỉ giao hàng!");
                        console.error("Phí vận chuyển = 0 khi thanh toán ZaloPay");
                        showPaymentConfirm.value = true;
                        // ✅ RESET FLAG: Lỗi phí vận chuyển
                        isProcessingPayment.value = false;
                        console.log('🚫 Lỗi phí vận chuyển ZaloPay - Tắt isProcessingPayment flag');
                        return;
                    }
                    console.log("✅ Phí vận chuyển đã được tính:", phiVanChuyen);
                } else if (activeTabData.value.hd.phuong_thuc_nhan_hang === "Nhận tại cửa hàng") {
                    console.log("✅ Nhận tại cửa hàng - không tính phí vận chuyển");
                }

                // 3. ✅ Đồng bộ hóa đơn từ backend
                console.log('🔄 Bắt đầu đồng bộ trước khi thanh toán ZaloPay...');
                const { dbTotal, feTotal, hasDifference } = await syncHoaDonBeforePayment(
                    activeTabData.value.hd.id_hoa_don
                );

                // 4. ✅ Check sự khác biệt và xác nhận với user
                if (hasDifference) {
                    const shouldContinue = await showPriceDifferenceDialog(dbTotal, feTotal);
                    if (!shouldContinue) {
                        console.log('❌ User hủy thanh toán do sự khác biệt giá');
                        // ✅ RESET FLAG: User hủy do khác biệt giá
                        isProcessingPayment.value = false;
                        console.log('🚫 User hủy (giá khác biệt) - Tắt isProcessingPayment flag');
                        return;
                    }
                    // Refresh lại state để dùng giá mới nhất
                    await new Promise(resolve => setTimeout(resolve, 200));
                }

                // 5. ✅ Log thông tin cuối cùng
                console.log('🎯 ZALOPAY THANH TOÁN:');
                console.log('  - ID Hóa đơn:', activeTabData.value.hd.id_hoa_don);
                console.log('  - Tiền sản phẩm:', fe_tongTienHang.value - fe_phiVanChuyen.value);
                console.log('  - Phí vận chuyển:', fe_phiVanChuyen.value);
                console.log('  - FE Total:', feTotal);
                console.log('  - DB Total:', dbTotal);
                console.log('  - Sẽ thanh toán:', hasDifference ? dbTotal : feTotal);

                localStorage.setItem('checkPaymentStatus', 'true');
                localStorage.setItem('idHDPayMent', JSON.stringify(activeTabData.value.hd.id_hoa_don));
                localStorage.setItem('paymentMethod', 'ZaloPay');
                localStorage.removeItem('khachHangBH');

                // 6. ✅ Gọi ZaloPay với số tiền từ FE
                await thanhToanService.handleZaloPayPayment(
                    activeTabData.value.hd.id_hoa_don,
                    fe_tongThanhToan.value  // ← TRUYỀN TỔNG TIỀN
                );
                
                // ✅ RESET FLAG: ZaloPay thanh toán thành công
                isProcessingPayment.value = false;
                console.log('💰 ZaloPay thành công - Tắt isProcessingPayment flag');

            } catch (error) {
                console.error('❌ Lỗi khi đồng bộ/thanh toán ZaloPay:', error);
                message.error('Không thể đồng bộ dữ liệu. Vui lòng thử lại!');
                // ✅ RESET FLAG: Lỗi ZaloPay inner catch
                isProcessingPayment.value = false;
                console.log('🚫 Lỗi ZaloPay inner - Tắt isProcessingPayment flag');
            }
        }
    } catch (error) {
        console.error('Lỗi khi thanh toán:', error);
        message.error('Đã xảy ra lỗi khi thanh toán!');
    } finally {
        // ✅ RESET FLAG: Kết thúc quá trình thanh toán
        isProcessingPayment.value = false;
        console.log('🏁 Kết thúc quá trình thanh toán - Tắt isProcessingPayment flag');
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

    // ✅ Xóa TẤT CẢ localStorage liên quan đến đơn hàng
    localStorage.removeItem('khachHangBH');           // Khách có TK
    localStorage.removeItem('walkInCustomer');        // Khách lẻ
    localStorage.removeItem('chonKH');                // Flag chọn KH
    localStorage.removeItem('shippingFeeUpdated');    // Phí ship đã update
    localStorage.removeItem('calculatedShippingFee'); // Phí ship tính toán
    localStorage.removeItem('luuTTKHBH');             // Lưu TT KH BH
    console.log('✅ Đã xóa toàn bộ localStorage sau thanh toán thành công');

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

// ✅ PHASE 1: Đồng bộ hóa đơn trước khi thanh toán ZaloPay
const syncHoaDonBeforePayment = async (idHoaDon) => {
    try {
        console.log('🔄 Bắt đầu đồng bộ hóa đơn trước khi thanh toán...');
        
        // ✅ VALIDATE: Ensure store is initialized
        store.initializeStore();
        
        // ✅ VALIDATE: Check store state before operations
        if (!store.tabs || store.tabs.length === 0) {
            console.warn('⚠️ Store tabs not ready, using fallback calculation');
            return {
                dbTotal: fe_tongThanhToan.value,
                feTotal: fe_tongThanhToan.value,
                hasDifference: false
            };
        }
        
        // 1. Refresh thông tin hóa đơn từ DB
        await store.refreshHoaDon(idHoaDon);
        console.log('✅ Đã refresh thông tin hóa đơn');
        
        // 2. Refresh danh sách sản phẩm từ DB
        await store.getAllSPHD(idHoaDon);
        console.log('✅ Đã refresh danh sách sản phẩm');
        
        // 3. Đợi 300ms để đảm bảo state được cập nhật
        await new Promise(resolve => setTimeout(resolve, 300));
        
        // 4. Lấy giá trị từ DB sau khi sync với fallback
        const dbHoaDon = store.getHoaDonById(idHoaDon);
        let dbTotal = fe_tongThanhToan.value; // fallback to FE value
        
        if (dbHoaDon) {
            dbTotal = (dbHoaDon.tong_tien_sau_giam || 0) + (dbHoaDon.phi_van_chuyen || 0);
            console.log('✅ Got data from DB');
        } else {
            console.warn('⚠️ DB hoa don not found, using FE calculation');
        }
        
        const feTotal = fe_tongThanhToan.value;
        
        console.log('📊 So sánh sau khi sync:');
        console.log('  - DB Total:', dbTotal);
        console.log('  - FE Total:', feTotal);
        
        return {
            dbTotal,
            feTotal,
            hasDifference: Math.abs(dbTotal - feTotal) > 100 // Chấp nhận sai lệch < 100đ
        };
        
    } catch (error) {
        console.error('❌ Lỗi khi đồng bộ hóa đơn:', error);
        // ✅ FALLBACK: Return FE values on error
        return {
            dbTotal: fe_tongThanhToan.value,
            feTotal: fe_tongThanhToan.value,
            hasDifference: false
        };
    }
};

// ✅ Hiển thị dialog xác nhận khi có sự khác biệt
const showPriceDifferenceDialog = (dbTotal, feTotal) => {
    return new Promise((resolve) => {
        Modal.confirm({
            title: '⚠️ Phát hiện sự khác biệt về giá',
            width: 500,
            content: h('div', { style: 'padding: 10px 0;' }, [
                h('p', { style: 'margin-bottom: 10px; font-weight: bold;' }, 'Dữ liệu đã thay đổi trong lúc bạn thao tác:'),
                h('div', { style: 'margin-bottom: 8px;' }, [
                    h('span', { style: 'display: inline-block; width: 120px;' }, 'Giao diện: '),
                    h('span', { style: 'font-weight: bold; color: #ff6600;' }, formatCurrency(feTotal))
                ]),
                h('div', { style: 'margin-bottom: 15px;' }, [
                    h('span', { style: 'display: inline-block; width: 120px;' }, 'Hệ thống: '),
                    h('span', { style: 'font-weight: bold; color: #52c41a;' }, formatCurrency(dbTotal))
                ]),
                h('p', { style: 'margin-bottom: 10px; color: #666;' }, 'Bạn có muốn tiếp tục với giá mới nhất từ hệ thống không?')
            ]),
            okText: 'Dùng giá hệ thống',
            cancelText: 'Hủy thanh toán',
            onOk: () => resolve(true),
            onCancel: () => resolve(false),
            class: 'price-difference-dialog'
        });
    });
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

        // ✅ Load items cho tất cả các tabs ngay từ đầu
        for (const pane of panes.value) {
            if (pane.hd.id_hoa_don) {
                try {
                    await store.getAllSPHD(pane.hd.id_hoa_don);
                    pane.items.value = store.getAllSPHDArr.map(item => ({
                        id_hoa_don: item.id_hoa_don,
                        id_chi_tiet_san_pham: item.id_chi_tiet_san_pham,
                        hinh_anh: item.hinh_anh,
                        ten_san_pham: item.ten_san_pham,
                        mau_sac: item.ten_mau_sac,
                        kich_thuoc: item.gia_tri,
                        so_luong: item.so_luong,
                        gia_ban: item.gia_ban,
                        tong_tien: item.don_gia,
                        so_luong_ton_goc: item.so_luong_ton || item.so_luong_ton_kho || 0,
                        trang_thai_ctsp: item.trang_thai_ctsp,
                        trang_thai_san_pham: item.trang_thai_san_pham,
                        so_luong_ton_kho: item.so_luong_ton_kho
                    }));
                    console.log(`✅ Loaded ${pane.items.value.length} items for invoice ${pane.hd.id_hoa_don}`);
                } catch (error) {
                    console.error(`❌ Error loading items for invoice ${pane.hd.id_hoa_don}:`, error);
                }
            }
        }

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

        // ✅ QUY TẮC MỚI: Reload products để lấy status mới nhất
        await handleSearchFocus();

        // Reload cart items
        await store.getAllSPHD(currentTab.hd.id_hoa_don);

        console.log('📦 WATCH: Dữ liệu từ server:', store.getAllSPHDArr.length, 'items');

        // ✅ NEW: Kiểm tra stock và hiển thị thông báo nếu có items không hợp lệ
        const stockCheck = await store.checkCartStock(currentTab.hd.id_hoa_don);
        if (stockCheck.has_invalid_items) {
            message.warning(`⚠️ Có ${stockCheck.invalid_item_names.length} sản phẩm không hợp lệ: ${stockCheck.invalid_item_names.join(', ')}. Vui lòng kiểm tra giỏ hàng!`, 5);
        }

        // Map items với validation
        const mappedItems = store.getAllSPHDArr.map(item => ({
            id_hoa_don: item.id_hoa_don,
            id_chi_tiet_san_pham: item.id_chi_tiet_san_pham,
            hinh_anh: item.hinh_anh,
            ten_san_pham: item.ten_san_pham,
            mau_sac: item.ten_mau_sac,
            kich_thuoc: item.gia_tri,
            so_luong: item.so_luong,
            gia_ban: item.gia_ban,
            tong_tien: item.don_gia,
            so_luong_ton_goc: item.so_luong_ton || item.so_luong_ton_kho || 0,
            // ✅ Validation fields từ backend
            trang_thai_ctsp: item.trang_thai_ctsp,
            trang_thai_san_pham: item.trang_thai_san_pham,
            so_luong_ton_kho: item.so_luong_ton_kho
        }));

        console.log('🎨 WATCH: Mapped items:', mappedItems.length, 'items');

        // ✅ Validate và auto-adjust nếu cần
        // KHÔNG auto-adjust nếu sản phẩm inactive - để nguyên số lượng
        for (const item of mappedItems) {
            // Bỏ qua sản phẩm inactive - không cần adjust
            if (item.trang_thai_ctsp === false || item.trang_thai_ctsp === 0 ||
                item.trang_thai_san_pham === false || item.trang_thai_san_pham === 0) {
                console.log(`⏭️ Skipping inactive product: ${item.ten_san_pham}`);
                continue;
            }

            // Chỉ check nếu stock thay đổi VÀ sản phẩm còn active
            const stockAvailable = item.so_luong_ton_kho ?? 0;
            if (item.so_luong > stockAvailable + item.so_luong) {
                console.warn(`⚠️ Stock changed for ${item.ten_san_pham}: cart=${item.so_luong}, stock=${stockAvailable}`);
                // KHÔNG reset về 1 - giữ nguyên số lượng trong giỏ
                // Chỉ notify user thay vì auto-adjust
            }
        }

        // Reload again sau khi adjust
        await store.getAllSPHD(currentTab.hd.id_hoa_don);
        currentTab.items.value = store.getAllSPHDArr.map(item => ({
            id_hoa_don: item.id_hoa_don,
            id_chi_tiet_san_pham: item.id_chi_tiet_san_pham,
            hinh_anh: item.hinh_anh,
            ten_san_pham: item.ten_san_pham,
            mau_sac: item.ten_mau_sac,
            kich_thuoc: item.gia_tri,
            so_luong: item.so_luong,
            gia_ban: item.gia_ban,
            tong_tien: item.don_gia,
            so_luong_ton_goc: item.so_luong_ton || item.so_luong_ton_kho || 0,
            trang_thai_ctsp: item.trang_thai_ctsp,
            trang_thai_san_pham: item.trang_thai_san_pham,
            so_luong_ton_kho: item.so_luong_ton_kho
        }));

        // Cập nhật các giá trị liên quan
        ptnh.value = currentTab.hd.phuong_thuc_nhan_hang;
        // checkForSuspendedInvoiceOverflow(); // Function đã bị xóa - không cần nữa
        console.log('✅ WATCH: Tab loaded with validation');
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


// ✅ Nhận phí vận chuyển từ FormKhachHangBH và cập nhật vào input
const handleShippingFeeCalculated = async (fee) => {
    console.log('📦 Nhận phí vận chuyển:', fee);
    const currentTab = activeTabData.value;

    if (currentTab && currentTab.hd) {
        // ✅ Cập nhật phí vận chuyển trong state
        currentTab.hd.phi_van_chuyen = fee;

        // ✅ LƯU VÀO DATABASE
        try {
            const idHoaDon = currentTab.hd.id_hoa_don;
            if (typeof store.updatePhiVanChuyen === 'function') {
                await store.updatePhiVanChuyen(idHoaDon, fee);
                console.log('✅ Đã lưu phí vận chuyển vào DB:', fee);
            } else {
                console.warn('⚠️ Method updatePhiVanChuyen không tồn tại trong store');
                // Fallback: Gọi API trực tiếp
                await banHangService.updatePhiVanChuyen(idHoaDon, fee);
                console.log('✅ Đã lưu phí vận chuyển vào DB (API trực tiếp):', fee);
            }
        } catch (error) {
            console.error('❌ Lỗi lưu phí vận chuyển:', error);
        }


        console.log('✅ Đã cập nhật phí vận chuyển:', fee);
    }
};

// ✅ Nhận event khi form khách hàng thay đổi (reset hoặc lưu)
const handleCustomerDataSaved = async (customerData) => {
    // CHỈ refresh UI khi RESET (customerData = null)
    // KHÔNG refresh khi LƯU (customerData có giá trị) để tránh form biến mất
    if (customerData === null) {
        const idHoaDon = activeTabData.value?.hd?.id_hoa_don;
        if (idHoaDon) {
            await refreshHoaDon(idHoaDon);
            console.log('✅ Đã refresh UI sau khi reset form');
        }
    } else {
        console.log('ℹ️ Form saved, skip refresh to keep form visible');
    }
};

const handlePhuongThucChange = async () => {
    console.log('🔄 Phương thức nhận hàng đã thay đổi:', activeTabData.value.hd.phuong_thuc_nhan_hang);
    ptnh.value = activeTabData.value.hd.phuong_thuc_nhan_hang;

    if (activeTabData.value.hd.phuong_thuc_nhan_hang === 'Nhận tại cửa hàng') {
        activeTabData.value.hd.phi_van_chuyen = 0;
    }

    // Trigger update cho formKhachHangBH khi chuyển sang 'Giao hàng'
    if (activeTabData.value.hd.phuong_thuc_nhan_hang === 'Giao hàng') {
        triggerUpdate.value = Date.now();
    }
};

// ✅ Watch localStorage để tự động cập nhật phí vận chuyển
const updateShippingFeeFromStorage = () => {
    const shippingData = localStorage.getItem('shippingFeeUpdated');
    if (shippingData && activeTabData.value?.hd) {
        try {
            const { idHoaDon, phiVanChuyen } = JSON.parse(shippingData);
            if (idHoaDon === activeTabData.value.hd.id_hoa_don && phiVanChuyen) {
                activeTabData.value.hd.phi_van_chuyen = phiVanChuyen;
                console.log(`💰 Đã cập nhật phí vận chuyển vào input: ${phiVanChuyen}`);
            }
        } catch (e) {
            console.error('Lỗi parse shippingFeeUpdated:', e);
        }
    }
};

// Chạy mỗi 500ms để check localStorage
setInterval(updateShippingFeeFromStorage, 500);

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

    0%,
    100% {
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

/* Invoice Tab Label với Badge */
.invoice-tab-label {
    display: inline-flex;
    align-items: center;
    position: relative;
    padding-left: 8px;
    overflow: visible;
}

.invoice-tab-label .tab-title {
    padding: 0 4px;
    font-weight: 500;
}

/* Badge số lượng sản phẩm - góc trái lòi ra ngoài */
.product-count-badge {
    position: absolute;
    top: -16px;
    left: -16px;
    min-width: 22px;
    height: 22px;
    padding: 0 6px;
    font-size: 11px;
    font-weight: 700;
    line-height: 22px;
    text-align: center;
    color: #fff;
    background-color: #bfbfbf;
    border-radius: 11px;
    box-shadow: 0 0 0 2px #fff, 0 2px 4px rgba(0, 0, 0, 0.15);
    z-index: 10;
}

.product-count-badge.has-products {
    background-color: #52c41a;
    animation: pulse-green 2s infinite;
}

@keyframes pulse-green {
    0% {
        box-shadow: 0 0 0 2px #fff;
    }

    50% {
        box-shadow: 0 0 0 2px #fff, 0 0 4px 2px rgba(107, 255, 34, 0.4);
    }

    100% {
        box-shadow: 0 0 0 2px #fff;
    }
}

/* Override Ant Design tabs để badge không bị cắt */
:deep(.ant-tabs-nav),
:deep(.ant-tabs-nav-wrap),
:deep(.ant-tabs-nav-list),
:deep(.ant-tabs-tab) {
    overflow: visible !important;
}

/* QR Scan Button */
.qr-scan-button {
    display: inline-flex !important;
    align-items: center !important;
    justify-content: center !important;
    padding: 8px 20px !important;
    height: 42px !important;
    border-radius: 8px !important;
    font-size: 15px !important;
    font-weight: 500 !important;
    box-shadow: 0 2px 8px #ff6600 !important;
    transition: all 0.3s ease !important;
    background: linear-gradient(135deg, #ff6600 0%, #ff6600 100%) !important;
    border: none !important;
}

.qr-scan-button:hover {
    transform: translateY(-2px) !important;
    box-shadow: 0 4px 12px #ff6600 !important;
    background: linear-gradient(135deg, #ff6600 0%, #ff6600 100%) !important;
}

.qr-scan-button:active {
    transform: translateY(0) !important;
    box-shadow: 0 2px 6px #ff6600 !important;
}

/* Product Search Bar */
.product-search-bar {
    border-radius: 8px !important;
    overflow: hidden !important;
}

.product-search-bar :deep(.ant-input) {
    height: 42px !important;
    border-radius: 8px 0 0 8px !important;
    border: 2px solid #d9d9d9 !important;
    font-size: 15px !important;
    padding: 8px 16px !important;
    transition: all 0.3s ease !important;
}

.product-search-bar :deep(.ant-input:hover) {
    border-color: #ff6600 !important;
}

.product-search-bar :deep(.ant-input:focus) {
    border-color: #ff6600 !important;
    box-shadow: 0 0 0 2px rgba(255, 102, 0, 0.1) !important;
}

.product-search-bar :deep(.ant-input-search-button) {
    height: 42px !important;
    border-radius: 0 8px 8px 0 !important;
    background: #ff6600 !important;
    border: 2px solid #ff6600 !important;
    border-left: none !important;
    transition: all 0.3s ease !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
}

.product-search-bar :deep(.ant-input-search-button:hover) {
    background: #ff8533 !important;
    border-color: #ff8533 !important;
    transform: scale(1.05) !important;
}

.product-search-bar :deep(.ant-input-group-addon) {
    background: transparent !important;
}

/* Voucher Select Styling */
.voucher-label {
    display: flex;
    align-items: center;
    font-weight: 600 !important;
    font-size: 15px !important;
    color: #262626 !important;
    margin-bottom: 8px !important;
}

.voucher-select :deep(.ant-select-selector) {
    height: 44px !important;
    border-radius: 8px !important;
    border: 2px solid #ffd591 !important;
    background: linear-gradient(135deg, #fff7e6 0%, #ffffff 100%) !important;
    transition: all 0.3s ease !important;
    padding: 4px 12px !important;
}

.voucher-select :deep(.ant-select-selector:hover) {
    border-color: #ff6600 !important;
    background: linear-gradient(135deg, #fff4e6 0%, #ffffff 100%) !important;
    box-shadow: 0 2px 8px rgba(255, 102, 0, 0.15) !important;
}

.voucher-select :deep(.ant-select-focused .ant-select-selector) {
    border-color: #ff6600 !important;
    box-shadow: 0 0 0 2px rgba(255, 102, 0, 0.1) !important;
}

.voucher-select :deep(.ant-select-selection-item) {
    font-weight: 500 !important;
    color: #ff6600 !important;
    display: flex !important;
    align-items: center !important;
    line-height: 36px !important;
}

.voucher-select :deep(.ant-select-arrow) {
    color: #ff6600 !important;
}

/* Voucher Dropdown Options */
:deep(.ant-select-dropdown .ant-select-item) {
    padding: 12px 16px !important;
    border-radius: 6px !important;
    margin: 4px 8px !important;
    transition: all 0.2s ease !important;
}

:deep(.ant-select-dropdown .ant-select-item-option) {
    background: #fff !important;
}

:deep(.ant-select-dropdown .ant-select-item-option:hover) {
    background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%) !important;
    color: #ff6600 !important;
    transform: translateX(4px) !important;
}

:deep(.ant-select-dropdown .ant-select-item-option-selected) {
    background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%) !important;
    color: white !important;
    font-weight: 600 !important;
}

/* Invoice Info Cards */
.invoice-info-card {
    background: linear-gradient(135deg, #ffffff 0%, #f9f9f9 100%);
    border: 2px solid #e8e8e8;
    border-radius: 12px;
    padding: 16px;
    transition: all 0.3s ease;
}

.invoice-info-card:hover {
    border-color: #ff6600;
    box-shadow: 0 4px 12px rgba(255, 102, 0, 0.1);
    transform: translateY(-2px);
}

.info-item {
    display: flex;
    align-items: center;
    gap: 12px;
}

.info-icon {
    font-size: 24px;
    color: #ff6600;
    flex-shrink: 0;
}

.info-content {
    display: flex;
    flex-direction: column;
    gap: 4px;
    flex: 1;
}

.info-label {
    font-size: 13px;
    color: #8c8c8c;
    font-weight: 500;
}

.info-value {
    font-size: 16px;
    color: #262626;
    font-weight: 600;
}

.select-customer-btn {
    border-radius: 8px !important;
    height: 36px !important;
}

.remove-customer-btn {
    border-radius: 8px !important;
    height: 36px !important;
}

/* Shipping Method Styling */
.shipping-method-label {
    display: flex;
    align-items: center;
    font-weight: 600;
    font-size: 14px;
    color: #262626;
    margin-bottom: 12px;
}

.shipping-methods {
    display: flex;
    gap: 10px;
}

.shipping-radio-card {
    margin-top: 12px;
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 8px 12px;
    border: 2px solid #e8e8e8;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    background: #ffffff;
}

.shipping-radio-card:hover {
    border-color: #ff6600;
    background: linear-gradient(135deg, #fff7e6 0%, #ffffff 100%);
    transform: translateY(-1px);
    box-shadow: 0 2px 6px rgba(255, 102, 0, 0.1);
}

.shipping-radio-card.active {
    border-color: #ff6600;
    background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
    box-shadow: 0 3px 10px rgba(255, 102, 0, 0.25);
}

.shipping-radio-card.active .radio-icon,
.shipping-radio-card.active .radio-text {
    color: white !important;
}

.shipping-radio-card .form-check-input {
    display: none;
}

.shipping-radio-card .radio-icon {
    font-size: 16px;
    color: #ff6600;
    transition: color 0.3s ease;
}

.shipping-radio-card .radio-text {
    font-size: 13px;
    font-weight: 500;
    color: #262626;
    transition: color 0.3s ease;
}

/* Customer Select Modal */
.customer-select-modal :deep(.ant-modal-content) {
    border-radius: 12px;
    overflow: hidden;
}

.customer-select-modal :deep(.ant-modal-header) {
    background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
    border-bottom: none;
    padding: 20px 24px;
}

.customer-select-modal :deep(.ant-modal-body) {
    padding: 24px;
    max-height: 70vh;
    overflow-y: auto;
}

.modal-custom-title {
    display: flex;
    align-items: center;
    gap: 12px;
    color: white;
    font-size: 18px;
    font-weight: 600;
}

.modal-custom-title .title-icon {
    font-size: 24px;
}

/* Customer Search Section */
.customer-search-section {
    margin-bottom: 20px;
}

.customer-search-input {
    width: 100%;
}

.customer-search-input :deep(.ant-input) {
    border-radius: 8px;
    border: 2px solid #e8e8e8;
    padding: 8px 16px;
    font-size: 15px;
    transition: all 0.3s ease;
}

.customer-search-input :deep(.ant-input:hover) {
    border-color: #ff6600;
}

.customer-search-input :deep(.ant-input:focus) {
    border-color: #ff6600;
    box-shadow: 0 0 0 2px rgba(255, 102, 0, 0.1);
}

.customer-search-input :deep(.ant-input-search-button) {
    background: #ff6600;
    border-color: #ff6600;
    height: 44px;
}

.customer-search-input :deep(.ant-input-search-button:hover) {
    background: #ff8533;
    border-color: #ff8533;
}

/* Customer Table */
.customer-table-wrapper {
    background: #ffffff;
    border-radius: 8px;
    border: 1px solid #e8e8e8;
    overflow: hidden;
}

.customer-table {
    margin-bottom: 0 !important;
    width: 100%;
    table-layout: fixed;
}

.customer-table thead {
    background: linear-gradient(135deg, #f5f5f5 0%, #fafafa 100%);
}

.customer-table thead th {
    font-weight: 600;
    color: #262626;
    border-bottom: 2px solid #e8e8e8 !important;
    padding: 16px 12px !important;
    font-size: 13px;
    white-space: nowrap;
}

.customer-table tbody .customer-row {
    transition: all 0.2s ease;
}

.customer-table tbody .customer-row:hover {
    background: linear-gradient(135deg, #fff7e6 0%, #fffbf0 100%);
    box-shadow: 0 2px 8px rgba(255, 102, 0, 0.08);
}

.customer-table tbody td {
    padding: 14px 12px !important;
    vertical-align: middle !important;
    font-size: 13px;
    border-bottom: 1px solid #f0f0f0 !important;
}

.customer-name {
    font-weight: 500;
    color: #262626;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.customer-address {
    color: #595959;
    font-size: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.customer-table .select-btn {
    border-radius: 6px;
    font-weight: 500;
    padding: 4px 16px !important;
    height: 32px !important;
    font-size: 13px !important;
}

.customer-table .select-btn:hover {
    transform: scale(1.08);
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

/* Action column spacing */
.customer-table tbody td:last-child {
    padding-right: 20px !important;
}

/* Modal Custom Footer */
.modal-custom-footer {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 16px;
    padding-top: 20px;
    margin-top: 20px;
    border-top: 2px solid #e8e8e8;
}

.modal-custom-footer .cancel-btn {
    border-radius: 8px;
    height: 42px;
    padding: 0 28px;
    font-weight: 500;
    font-size: 14px;
    border: 2px solid #d9d9d9;
    display: inline-flex;
    align-items: center;
    gap: 8px;
}

.modal-custom-footer .cancel-btn:hover {
    border-color: #ff6600;
    color: #ff6600;
    transform: translateY(-1px);
}

.modal-custom-footer .confirm-btn {
    border-radius: 8px;
    height: 42px;
    padding: 0 28px;
    font-weight: 500;
    font-size: 14px;
    background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
    border: none;
    display: inline-flex;
    align-items: center;
    gap: 8px;
}

.modal-custom-footer .confirm-btn:hover {
    background: linear-gradient(135deg, #ff8533 0%, #ffa366 100%);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(255, 102, 0, 0.35);
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

/* ✅ NEW: Inactive product row styling */
.inactive-product-row {
    background-color: #f5f5f5 !important;
    opacity: 0.65;
    text-decoration: line-through;
}

/* ✅ Invalid items banner */
.invalid-items-banner {
    background-color: #fff7e6 !important;
}

/* ✅ Item status badges */
.item-status-badges {
    margin-top: 8px;
}

/* ✅ Search dropdown - Out of stock item (gray) */
.out-of-stock-item {
    background-color: #f5f5f5 !important;
    opacity: 0.7;
    cursor: not-allowed !important;
}

.out-of-stock-item:hover {
    background-color: #e0e0e0 !important;
}

/* ✅ Search dropdown - Inactive item (darker gray, crossed) */
.inactive-item {
    background-color: #e8e8e8 !important;
    opacity: 0.5;
    cursor: not-allowed !important;
}

.inactive-item .product-name {
    text-decoration: line-through;
    color: #999;
}

.inactive-item:hover {
    background-color: #d9d9d9 !important;
}

/* ✅ Stock status colors */
.in-stock {
    color: #52c41a;
    font-weight: bold;
}

.low-stock {
    color: #faad14;
    font-weight: bold;
}

.no-stock {
    color: #ff4d4f;
    font-weight: bold;
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

/* ======================== CART TABLE STYLING ======================== */
.cart-table-header {
    position: sticky !important;
    top: 0 !important;
    z-index: 10 !important;
    background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%) !important;
}

.cart-table-header th {
    color: white !important;
    font-weight: 600 !important;
    font-size: 14px !important;
    padding: 6px 8px !important;
    border: none !important;
    line-height: 1.4 !important;
}

.cart-table tbody tr {
    transition: all 0.2s ease;
}

.cart-table tbody tr:hover {
    background: linear-gradient(135deg, #fff7e6 0%, #fffbf0 100%);
    box-shadow: 0 2px 8px rgba(255, 102, 0, 0.08);
}

.cart-table tbody td {
    vertical-align: middle !important;
    padding: 8px 6px !important;
}

.cart-index {
    font-weight: 600;
    color: #595959;
}

.cart-product-image {
    width: 60px;
    height: 60px;
    border-radius: 8px;
    object-fit: cover;
    border: 2px solid #e8e8e8;
    transition: all 0.3s ease;
}

.cart-product-image:hover {
    transform: scale(1.15);
    border-color: #ff6600;
}

.cart-price-cell,
.cart-total-cell {
    font-weight: 600;
    color: #ff6600;
    text-align: right;
    padding-right: 16px !important;
}

.cart-total-cell {
    color: #262626;
    font-size: 15px;
}

/* ======================== PRODUCT DROPDOWN STYLING ======================== */
.product-option {
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;
    transition: all 0.2s ease;
    cursor: pointer;
}

.product-option:hover {
    background: linear-gradient(135deg, #fff7e6 0%, #fffbf0 100%);
    transform: translateX(4px);
    border-left: 3px solid #ff6600;
}

.product-image {
    border-radius: 8px;
    border: 2px solid #e8e8e8;
}

.product-option:hover .product-image {
    border-color: #ff6600;
}

.product-name {
    font-size: 15px;
    font-weight: 600;
    color: #262626;
}

.product-price {
    font-size: 16px;
    font-weight: 700;
    color: #ff6600;
}

.original-price {
    font-size: 13px;
    color: #8c8c8c;
    text-decoration: line-through;
}

.discount-badge {
    background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
    color: white;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 700;
}

.current-price {
    font-size: 16px;
    font-weight: 700;
    color: #ff6600;
}

.in-stock {
    color: #52c41a;
    font-weight: 600;
}

.low-stock {
    color: #faad14;
    font-weight: 600;
}

.no-stock {
    color: #ff4d4f;
    font-weight: 600;
}

/* ======================== WARNING BANNER STYLING ======================== */
.invalid-items-banner {
    background: linear-gradient(135deg, #fff7e6 0%, #fffbf0 100%);
    border-left: 4px solid #faad14;
}

.invalid-items-banner td {
    padding: 16px 20px !important;
}

.invalid-items-banner :deep(.ant-alert) {
    background: transparent !important;
    border: none !important;
    padding: 0 !important;
}

.invalid-items-banner :deep(.ant-alert-warning) {
    background: transparent !important;
}

.invalid-items-banner :deep(.ant-alert-message) {
    color: #262626 !important;
    font-weight: 600 !important;
    font-size: 15px !important;
    margin-bottom: 8px !important;
}

.invalid-items-banner :deep(.ant-alert-description) {
    color: #595959 !important;
    font-size: 13px !important;
    line-height: 1.6 !important;
}

.invalid-items-banner :deep(.ant-alert-icon) {
    color: #faad14 !important;
    font-size: 24px !important;
}

.invalid-items-banner :deep(.ant-alert-close-icon) {
    color: #8c8c8c !important;
    font-size: 14px !important;
}

.invalid-items-banner :deep(.ant-alert-close-icon:hover) {
    color: #ff6600 !important;
}

/* ✅ PHASE 1: Price Difference Dialog Styles */
.price-difference-dialog :deep(.ant-modal-header) {
    background: linear-gradient(135deg, #fff7e6 0%, #fffbf0 100%);
    border-bottom: 2px solid #faad14;
}

.price-difference-dialog :deep(.ant-modal-title) {
    color: #d48806;
    font-weight: 600;
}

.price-difference-dialog :deep(.ant-modal-body) {
    padding: 20px 24px;
}

.price-difference-dialog :deep(.ant-btn-primary) {
    background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
    border: none;
    border-radius: 6px;
    font-weight: 500;
}

.price-difference-dialog :deep(.ant-btn-primary:hover) {
    background: linear-gradient(135deg, #73d13d 0%, #95de64 100%);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}
</style>