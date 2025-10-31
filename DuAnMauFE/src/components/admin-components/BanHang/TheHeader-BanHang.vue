<template>
    <div class="header-container">
        <!-- Search Combo Box -->
        <div class="search-section">
            <a-dropdown v-model:visible="dropdownVisible" :trigger="['click']" overlayClassName="product-dropdown">
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
                        <div v-else-if="filteredProducts.length > 0">
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
                                    <div class="info-left">
                                        <span class="product-quantity">SL: {{ product.so_luong || 1 }}</span>
                                    </div>
                                    <div class="info-right">
                                        <span class="product-price">{{ formatCurrency(product.gia_ban) }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div v-else class="empty-result">
                            Nhập tên sản phẩm để tìm kiếm.
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
            <!-- <div v-if="qrScanResult" class="mt-2">
                <p>Kết quả quét: {{ qrScanResult }}</p>
            </div> -->
        </a-modal>

        <!-- Invoice Tabs -->
        <div class="invoice-tabs">
            <a-tabs v-model:activeKey="activeKey" type="editable-card" @edit="onEdit">
                <a-tab-pane v-for="pane in panes" :key="pane.key" :tab="pane.title" :closable="pane.closable">
                    {{ pane.content }}
                </a-tab-pane>
            </a-tabs>
        </div>


        <!-- Action Buttons -->
        <div class="action-buttons">
            <a-tooltip title="Tra cứu đơn hàng">
                <a-button type="primary" shape="circle" class="action-btn" @click="changeRoute('/admin/quanlyhoadon')">
                    <template #icon> <file-search-outlined /></template>
                </a-button>
            </a-tooltip>
            <a-tooltip title="Trả hàng">
                <a-button type="primary" shape="circle" class="action-btn" @click="changeRoute('/admin/traHang')">
                    <template #icon><rollback-outlined /></template>
                </a-button>
            </a-tooltip>
            <a-tooltip v-if="store.id_roles !== 3" title="Báo cáo thống kê">
                <a-button type="primary" shape="circle" class="action-btn" @click="changeRoute('/admin/')">
                    <template #icon><bar-chart-outlined /></template>
                </a-button>
            </a-tooltip>
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
                                            :max="item.so_luong_ton_goc + item.so_luong" @change="updateItemTotal(item)"
                                            style="width: 80px;" />

                                    </a-space>
                                </td>
                                <td>{{ formatCurrency(item.gia_ban) }}</td>
                                <td>{{ formatCurrency(item.tong_tien) }}</td>
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
                        <label class="form-label">Tên nhân viên: {{ activeTabData.hd.ten_nhan_vien }}</label>
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
                            <!-- <label class="form-label">Địa chỉ nhận hàng</label>
                            <input type="text" class="form-control mb-2" placeholder="Nhập địa chỉ"
                                v-model="activeTabData.hd.dia_chi"> -->
                            <!-- <label class="form-label">Phí vận chuyển (VNĐ)</label> -->
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
                        <label class="form-label">Tổng tiền hàng (VNĐ):</label>
                        <input type="text" class="form-control"
                            :value="formatCurrency(activeTabData.hd.tong_tien_truoc_giam)" disabled>
                    </div>
                    <div class="mb-3">
                        <label for="idVoucher" class="form-label">Voucher</label>
                        <select name="idVoucher" id="idVoucher" class="form-select"
                            v-model="activeTabData.hd.id_voucher" @change="updateVoucher">
                            <option :value="null">-- Chọn voucher --</option>
                            <option v-if="activeTabData.hd.id_voucher" :value="activeTabData.hd.id_voucher">
                                {{ `${activeTabData.hd.ten_voucher}` }}
                            </option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Tổng tiền thanh toán (VNĐ):</label>
                        <input type="text" class="form-control"
                            :value="formatCurrency(activeTabData.hd.tong_tien_sau_giam)" disabled>
                    </div>
                    <div class="mb-3">
                        <label class="form-label d-block mb-2">Hình thức thanh toán</label>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" :name="'hinhThucThanhToan_' + activeKey"
                                :id="'tienMat_' + activeKey" value="Tiền mặt"
                                v-model="activeTabData.hd.hinh_thuc_thanh_toan" @change="updateHinhThucThanhToan" />
                            <label class="form-check-label" :for="'tienMat_' + activeKey">Tiền mặt</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" :name="'hinhThucThanhToan_' + activeKey"
                                :id="'chuyenKhoan_' + activeKey" value="Chuyển khoản"
                                v-model="activeTabData.hd.hinh_thuc_thanh_toan" @change="updateHinhThucThanhToan" />
                            <label class="form-check-label" :for="'chuyenKhoan_' + activeKey">Chuyển khoản</label>
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
                    <a-modal v-model:open="showPrintConfirm" title="Xác nhận in hóa đơn" @ok="confirmPrint(true)"
                        @cancel="confirmPrint(false)">
                        <p>Bạn có muốn in hóa đơn không?</p>
                        <template #footer>
                            <a-button key="cancel" @click="confirmPrint(false)">Không</a-button>
                            <a-button key="ok" type="primary" @click="confirmPrint(true)">Có</a-button>
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
import { ref, reactive, computed, onMounted, watch, onUnmounted } from 'vue';
import {
    SearchOutlined,
    FileSearchOutlined,
    RollbackOutlined,
    BarChartOutlined,
    DeleteOutlined
} from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';
import { useGbStore } from '@/stores/gbStore';
import { Empty } from 'ant-design-vue';
import jsPDF from 'jspdf';
import logo from '../../../images/logo/logo2.png';
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
let html5QrCode = null;
let isProcessing = false;
const triggerUpdate = ref(Date.now());



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
        // Giả sử mã QR chứa id_chi_tiet_san_pham
        const product = allProducts.value.find(p => p.id_chi_tiet_san_pham === Number(qrData));
        console.log("qrData: ", qrData);
        console.log("product: ", product);
        console.log("allProducts.value: ", allProducts.value);

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

console.log("danhSachKhachHang: ", danhSachKhachHang.value);
// const diaChiMap = computed(() => store.diaChiMap);

const chonKhachHang = async (khachHang) => {
    try {
        console.log('Khách hàng được chọn:', khachHang);

        // Cập nhật thông tin khách hàng
        Object.assign(activeTabData.value.hd, {
            ten_khach_hang: khachHang.tenKhachHang,
            so_dien_thoai: khachHang.soDienThoai,
            dia_chi: khachHang.diaChi || 'Chưa có địa chỉ',
            id_khach_hang: khachHang.idKhachHang
        });

        // Gọi store để thêm khách hàng vào hóa đơn
        await store.addKHHD(
            activeTabData.value.hd.id_hoa_don,
            khachHang.idKhachHang,
            khachHang.diaChi,
            khachHang.tenKhachHang,
            khachHang.soDienThoai
        );

        // Làm mới danh sách khách hàng
        await store.getAllKhachHangNoPage();

        // Đóng modal
        open.value = false;
        if (!activeTabData.value.hd.isKhachLe) {
            handlePhuongThucChange();

        }
        // Làm mới dữ liệu hóa đơn
        await refreshHoaDon(activeTabData.value.hd.id_hoa_don);

        // Lưu vào localStorage
        try {
            localStorage.setItem('khachHangBH', JSON.stringify(khachHang));
            localStorage.setItem('chonKH', true);



            console.log('Đã lưu khách hàng vào localStorage:', khachHang);
        } catch (error) {
            console.error('Lỗi khi lưu khách hàng vào localStorage:', error);
        }

        console.log('activeTabData.hd sau khi làm mới:', activeTabData.value.hd);
        message.success(`Đã chọn khách hàng: ${khachHang.tenKhachHang}`);
        triggerUpdate.value = Date.now(); // Sau khi chọn xong khách
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

// const openKhachLe = ref(false);
// const khachLeForm = reactive({
//     tenKhachHang: "",
//     soDienThoai: "",
//     diaChi: ""
// });

// const handleThemDiaChi = () => {
//     openKhachLe.value = true;
// };

// const handleAddKhachLe = () => {
//     if (!khachLeForm.tenKhachHang || !khachLeForm.soDienThoai || !khachLeForm.diaChi) {
//         return message.warning("Vui lòng nhập đầy đủ thông tin khách lẻ!");
//     }

//     activeTabData.value.hd.ten_khach_hang = khachLeForm.tenKhachHang;
//     activeTabData.value.hd.so_dien_thoai = khachLeForm.soDienThoai;
//     activeTabData.value.hd.dia_chi = khachLeForm.diaChi;
//     activeTabData.value.hd.id_khach_hang = null;
//     store.addKHHD(activeTabData.value.hd.id_hoa_don, null, khachLeForm.diaChi, khachLeForm.tenKhachHang, khachLeForm.soDienThoai);
//     openKhachLe.value = false;
// };

const selectedKeys = ref([store.indexMenu]);
console.log(selectedKeys);

const changeRoute = (path) => {
    // Update all store properties related to navigation
    store.getPath(path);
    store.getRoutePresent(router.path);
    store.getIndex(path);

    // Log for debugging
    console.log('TheFraming - Navigating to:', path);
    console.log('TheFraming - Updated store.checkRouter:', store.checkRouter);
    console.log('TheFraming - Updated selectedKeys:', store.indexMenu);

    // Update selectedKeys from store
    selectedKeys.value = store.indexMenu;

    // Navigate
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
    console.log('Đang tìm kiếm:', searchQuery.value);
};


// --- Computed Properties ---
// Lọc sản phẩm cho dropdown tìm kiếm
const filteredProducts = computed(() => {
    if (!allProducts.value || allProducts.value.length === 0) {
        return [];
    }
    if (!searchQuery.value) {
        return allProducts.value;
    }

    const normalizedQuery = normalizeString(searchQuery.value);
    return allProducts.value.filter(product => {
        const normalizedProductName = normalizeString(product.ten_san_pham);
        return normalizedProductName.includes(normalizedQuery);
    });
});


// Lấy dữ liệu của tab đang active
const activeTabData = computed(() => {
    return panes.value.find(pane => pane.key === activeKey.value);
});

const currentInvoiceItems = computed(() => {
    return activeTabData.value?.items?.value || [];
});

// --- Methods ---
// Định dạng tiền tệ
const formatCurrency = (value) => {
    if (value === null || value === undefined) return '';
    return value.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
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

const handleSearchInput = (query) => {
    const normalizedQuery = normalizeString(query);
    if (!normalizedQuery) {
        filteredProducts.value = [];
        return;
    }
    console.log('All products:', allProducts.value.map(p => p.ten_san_pham));

    filteredProducts.value = allProducts.value.filter(product => {
        const normalizedProductName = normalizeString(product.ten_san_pham || '');
        const match = normalizedProductName.includes(normalizedQuery);
        console.log(`✅ MATCH: ${match} | "${normalizedProductName}" vs "${normalizedQuery}"`);
        return match;
    });
    console.log('🎯 Kết quả lọc:', filteredProducts.value.map(p => p.ten_san_pham));

};





// Xử lý khi nhấn Enter hoặc nút Search (có thể dùng để tìm kiếm chính xác hơn)
const performSearch = () => {
    if (!dropdownVisible.value && searchQuery.value) {
        dropdownVisible.value = true;
    }
    console.log('Performing search for:', searchQuery.value);
};

const refreshHoaDon = async (idHoaDon) => {
    try {
        // Gọi API để lấy lại dữ liệu hóa đơn
        await store.getHoaDonByIdHoaDon(idHoaDon);
        const hoaDonInfo = store.getHDBIDHD;

        const currentTab = activeTabData.value;
        if (hoaDonInfo && currentTab && currentTab.hd?.id_hoa_don === idHoaDon) {
            // Gán lại toàn bộ đối tượng hóa đơn cho tab hiện tại
            currentTab.hd = {
                ...currentTab.hd, // giữ lại tham chiếu nếu cần
                ...hoaDonInfo     // ghi đè bằng dữ liệu mới từ server

            };
            ptnh.value = hoaDonInfo.phuong_thuc_nhan_hang
        }
        console.log("currentTab.hd sau khi làm mới:", currentTab.hd);
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


let isAdding = false;

const addToBill = async (product) => {
    if (isAdding) return;
    isAdding = true;

    const currentTab = activeTabData.value;
    if (!currentTab || !currentTab.hd?.id_hoa_don) {
        message.error('Vui lòng chọn hoặc tạo một hóa đơn hợp lệ trước!');
        isAdding = false;
        return;
    }

    if (product.so_luong_ton <= 0) {
        message.warning(`Sản phẩm "${product.ten_san_pham}" đã hết hàng!`);
        isAdding = false;
        return;
    }

    try {
        const result = await store.themSPHDMoi(
            currentTab.hd.id_hoa_don,
            product.id_chi_tiet_san_pham,
            1,
            product.gia_sau_giam || product.gia_ban
        );
        if (!result) return;

        await store.getAllSPHD(currentTab.hd.id_hoa_don);
        console.log("store.getAllSPHDArr: ", store.getAllSPHDArr);
        currentTab.items.value = store.getAllSPHDArr.map(item => ({
            id_hoa_don: item.id_hoa_don,
            id_chi_tiet_san_pham: item.id_chi_tiet_san_pham,
            hinh_anh: item.hinh_anh,
            ten_san_pham: item.ten_san_pham,
            mau_sac: item.ten_mau_sac || item.mau_sac || null,
            kich_thuoc: item.gia_tri || null,
            so_luong: item.so_luong,
            gia_ban: item.don_gia,
            tong_tien: item.don_gia * item.so_luong,
            so_luong_ton_goc: item.so_luong_ton || 0
        }));
        console.log("currentTab.items.value");
        console.log(currentTab.items.value);
        await refreshHoaDon(currentTab.hd.id_hoa_don);

        dropdownVisible.value = false;
        searchQuery.value = '';
        message.success(`Đã thêm "${product.ten_san_pham}" vào hóa đơn.`);
        await store.getAllCTSPKM();
        allProducts.value = store.getAllCTSPKMList;

    } catch (error) {
        console.error('Lỗi khi thêm sản phẩm:', error);
        message.error('Đã xảy ra lỗi khi thêm sản phẩm!');
    } finally {
        isAdding = false;
    }
};


const tienKhachDua = ref(0);

// Tính toán tiền thừa trả khách (calculatedChange) dựa trên tong_tien_sau_giam
const calculatedChange = computed(() => {
    const total = activeTabData.value?.hd?.tong_tien_sau_giam || 0;
    const cash = tienKhachDua.value || 0;
    return cash >= total ? cash - total : 0;
});

const isPaymentDisabled = computed(() => {
    if (currentInvoiceItems.value.length === 0) {
        return true; // Không có sản phẩm nào trong hóa đơn

    }
    if (activeTabData.value?.hd?.hinh_thuc_thanh_toan === 'Tiền mặt') {
        const total = activeTabData.value.hd.tong_tien_sau_giam || 0;
        const cash = tienKhachDua.value || 0;
        return cash < total;
    }
    return false;
});

// Cập nhật tổng tiền khi số lượng thay đổi trong bảng hóa đơn
const updateItemTotal = async (item) => {
    const productInfo = allProducts.value.find(p => p.id_chi_tiet_san_pham === item.id_chi_tiet_san_pham);
    const sphdItem = store.getAllSPHDArr.find(sp =>
        sp.id_hoa_don === item.id_hoa_don &&
        sp.id_chi_tiet_san_pham === item.id_chi_tiet_san_pham
    );

    const soLuongTonKho = productInfo ? productInfo.so_luong : 0;
    const soLuongTrongHD = sphdItem ? sphdItem.so_luong : 0;
    let soLuongMoi = item.so_luong;

    // 1. Nếu nhập ≤ 0 → đặt lại 1
    if (soLuongMoi <= 0) {
        soLuongMoi = 1;
    }

    // 2. Nếu nhập vượt quá tồn + trong hóa đơn → giới hạn lại
    const gioiHanToiDa = soLuongTrongHD + soLuongTonKho;
    if (soLuongMoi > gioiHanToiDa) {
        message.warning(`Tồn kho không đủ. Đặt lại số lượng tối đa là ${gioiHanToiDa}`);
        soLuongMoi = gioiHanToiDa;
    }

    // Cập nhật lại item trong UI
    item.so_luong = soLuongMoi;

    try {
        // 🔄 Gọi API mới: set lại số lượng mong muốn
        await store.setSPHD(item.id_hoa_don, item.id_chi_tiet_san_pham, soLuongMoi);

        // Làm mới lại dữ liệu hóa đơn
        await store.getAllSPHD(item.id_hoa_don);

        const currentTab = activeTabData.value;
        if (currentTab) {
            currentTab.items.value = store.getAllSPHDArr.map(hd => ({
                id_hoa_don: hd.id_hoa_don,
                id_chi_tiet_san_pham: hd.id_chi_tiet_san_pham,
                hinh_anh: hd.hinh_anh,
                ten_san_pham: hd.ten_san_pham,
                mau_sac: hd.ten_mau_sac || hd.mau_sac || null,
                kich_thuoc: hd.gia_tri || null,
                so_luong: hd.so_luong,
                gia_ban: hd.don_gia,
                tong_tien: hd.don_gia * hd.so_luong,
                so_luong_ton_goc: hd.so_luong_ton || 0
            }));
        }

        await refreshHoaDon(item.id_hoa_don);
        await store.getAllCTSPKM();
        allProducts.value = store.getAllCTSPKMList;
    } catch (error) {
        console.error('Lỗi khi cập nhật số lượng:', error);
        message.error('Đã xảy ra lỗi khi cập nhật số lượng!');
    }
};





// Xóa sản phẩm khỏi hóa đơn chi tiết của tab hiện tại
const removeFromBill = async (productId) => {
    const currentTab = activeTabData.value;
    if (!currentTab || !currentTab.items) return;
    const itemsArray = currentTab.items.value;
    const itemIndex = itemsArray.findIndex(item => item.id_chi_tiet_san_pham === productId);
    if (itemIndex === -1) return;

    const removedItem = itemsArray[itemIndex];

    try {
        const result = await store.xoaSPHD(currentTab.hd.id_hoa_don, productId);

        if (!result || !result.success) {
            message.error(result.message || 'Không xóa được sản phẩm khỏi hóa đơn!');
            return;
        }

        // Làm mới danh sách sản phẩm từ server
        await store.getAllSPHD(currentTab.hd.id_hoa_don);
        currentTab.items.value = store.getAllSPHDArr.map(item => ({
            id_hoa_don: item.id_hoa_don,
            id_chi_tiet_san_pham: item.id_chi_tiet_san_pham,
            hinh_anh: item.hinh_anh,
            ten_san_pham: item.ten_san_pham,
            mau_sac: item.ten_mau_sac || item.mau_sac || null,
            kich_thuoc: item.gia_tri || null,
            so_luong: item.so_luong,
            gia_ban: item.don_gia,
            tong_tien: item.don_gia * item.so_luong,
            so_luong_ton_goc: item.so_luong_ton || 0,
        }));

        await refreshHoaDon(currentTab.hd.id_hoa_don);
        await store.getAllCTSPKM();
        allProducts.value = store.getAllCTSPKMList;

        message.info(`Đã xóa "${removedItem.ten_san_pham}" khỏi hóa đơn.`);
    } catch (error) {
        console.error('Lỗi không mong đợi:', error);
        message.error('Đã xảy ra lỗi bất ngờ khi xóa sản phẩm!');
    }
};


// Hàm tạo mới một tab hóa đơn (Đã sửa)
const add = async () => {
    try {
        // Giới hạn số lượng hóa đơn tối đa là 5
        // if (panes.value.length >= 5) {
        //     throw new Error('Bạn chỉ có thể tạo tối đa 5 hóa đơn cùng lúc!');
        // }

        // Kiểm tra xem người dùng đã đăng nhập và có thông tin chi tiết không
        if (!store.isLoggedIn || !store.userDetails) {
            throw new Error('Người dùng chưa đăng nhập hoặc không có thông tin chi tiết!');
        }

        // Kiểm tra vai trò (chỉ nhân viên mới được tạo hóa đơn)
        if (![1, 2, 3].includes(store.id_roles)) {
            throw new Error('Chỉ nhân viên mới có thể tạo hóa đơn!');
        }

        const idNhanVien = store.userDetails.idNhanVien;
        if (!idNhanVien) {
            throw new Error('Không tìm thấy ID nhân viên!');
        }

        console.log('ID Nhân viên được sử dụng để tạo hóa đơn:', idNhanVien);

        const response = await store.createHoaDon(idNhanVien);
        if (!response || response.error) {
            throw new Error(response?.message || 'Không thể tạo hóa đơn');
        }

        newTabIndex.value++;
        const newKey = `invoiceTab_${Date.now()}_${newTabIndex.value}`;

        panes.value.push({
            title: `Đơn ${panes.value.length + 1}`,
            key: newKey,
            closable: true,
            items: ref([]),
            hd: reactive({
                id_hoa_don: response.id_hoa_don,
                ma_hoa_don: response.ma_hoa_don,
                ngay_tao: response.ngay_tao,
                trang_thai: response.trang_thai,
                id_nhan_vien: response.id_nhan_vien,
                ten_nhan_vien: response.ten_nhan_vien,
                id_voucher: null,
                id_khach_hang: null,
                hinh_thuc_thanh_toan: 'Tiền mặt',
                phuong_thuc_nhan_hang: 'Nhận tại cửa hàng',
                isKhachLe: true,
                phi_van_chuyen: 0,
                tong_tien_truoc_giam: 0,
                tong_tien_sau_giam: 0

            })
        });
        ptnh.value = 'Nhận tại cửa hàng';
        activeKey.value = newKey;
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
            title: `Xác nhận hủy hóa đơn "${tabToRemove.title}"`,
            content: `Hóa đơn có ${tabToRemove.items.value.length} sản phẩm. Bạn chắc chắn muốn hủy?`,
            okText: 'Xác nhận',
            cancelText: 'Hủy',
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
    doc.text("G&B SPORTS", 105, 55, { align: "center" });
    doc.setFontSize(10);
    doc.setFont("Roboto", "normal");
    doc.text("Địa chỉ: Phương Canh, Nam Từ Liêm, Hà Nội", 105, 63, { align: "center" });
    doc.text("Điện thoại: 0397572262", 105, 69, { align: "center" });
    // Vẽ đường kẻ ngang
    doc.setLineWidth(0.5);
    doc.line(20, 73, 190, 73);
    // Thông tin hóa đơn
    let y = 120;
    doc.setFontSize(12);
    doc.setFont("Roboto", "normal");
    doc.text(`Mã hóa đơn: ${activeTabData.value.hd.ma_hoa_don || 'N/A'}`, 20, 86);
    doc.text(`Tên nhân viên: ${activeTabData.value.hd.ten_nhan_vien || 'N/A'}`, 20, 94);
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
    doc.text(`Tổng tiền hàng:`, 115, y, { align: "left" });
    doc.text(`${formatCurrency(activeTabData.value.hd.tong_tien_truoc_giam)}`, 190, y, { align: "right" });

    y += 6;
    const giamGia = (activeTabData.value.hd.tong_tien_truoc_giam || 0) +
        (activeTabData.value.hd.phi_van_chuyen || 0) -
        (activeTabData.value.hd.tong_tien_sau_giam || 0);
    doc.text(`Giảm giá:`, 115, y, { align: "left" });
    doc.text(`-${formatCurrency(giamGia)}`, 190, y, { align: "right" });

    y += 6;
    doc.text(`Phí vận chuyển:`, 115, y, { align: "left" });
    doc.text(`+${formatCurrency(activeTabData.value.hd.phi_van_chuyen || 0)}`, 190, y, { align: "right" });

    y += 6;
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

// Xử lý sự kiện edit tab (add hoặc remove) (Đã sửa)
const onEdit = (targetKeyOrAction, action) => {
    if (action === 'add') {
        add();
    } else {
        remove(targetKeyOrAction);
    }
};

const showPrintConfirm = ref(false);

// Hàm xử lý thanh toán (submit form)
const handlePayment = async () => { // Thêm async nếu gọi API
    const currentTab = activeTabData.value;
    if (!currentTab) {
        message.error("Không có hóa đơn nào đang được chọn.");
        return;
    }

    // Truy cập items qua .value
    if (!currentTab.items || currentTab.items.value.length === 0) {
        message.warning("Hóa đơn chưa có sản phẩm nào.");
        return;
    }

    // Validate các trường thông tin... (Giữ nguyên logic validate cũ)
    if (currentTab.hd.phuong_thuc_nhan_hang === 'Giao hàng') {
        if (!currentTab.hd.id_khach_hang && (!currentTab.hd.ho_ten_nguoi_nhan || !currentTab.hd.sdt_nguoi_nhan || !currentTab.hd.dia_chi_nhan_hang)) {
            message.error("Vui lòng nhập đầy đủ thông tin giao hàng (Tên, SĐT, Địa chỉ) hoặc chọn khách hàng.");
            return;
        }
        if (currentTab.hd.phi_van_chuyen <= 0) {
            message.warning("Vui lòng nhập phí vận chuyển cho đơn hàng giao.");
            // return;
        }
    }
    if (currentTab.hd.hinh_thuc_thanh_toan === 'Tiền mặt') {
        // Validate tiền khách đưa (nếu cần thiết)
        if (currentTab.hd.tien_khach_dua === null || currentTab.hd.tien_khach_dua < currentTab.hd.tong_tien_sau_giam) {
            message.error("Vui lòng nhập đủ tiền khách đưa.");
            return;
        }
        currentTab.hd.tien_du = currentTab.hd.tien_khach_dua - currentTab.hd.tong_tien_sau_giam;
    }


    console.log("Submitting Invoice Data for Tab:", currentTab.key);
    // Cần dùng JSON.parse(JSON.stringify(...)) để log giá trị thực sự của reactive object
    console.log("Invoice Header (hd):", JSON.parse(JSON.stringify(currentTab.hd)));
    // Items là ref, cần .value
    console.log("Invoice Items (items):", JSON.parse(JSON.stringify(currentTab.items.value)));

    const total = activeTabData.value.hd.tong_tien_sau_giam || 0;
    const cash = tienKhachDua.value || 0;

    // Kiểm tra tiền khách đưa nếu là "Tiền mặt"
    if (activeTabData.value.hd.hinh_thuc_thanh_toan === 'Tiền mặt' && cash < total) {
        message.error('Tiền khách đưa không đủ để thanh toán!');
        return;
    }

    // Hiển thị modal xác nhận in hóa đơn
    showPrintConfirm.value = true;

};



const confirmPrint = async (shouldPrint) => {
    showPrintConfirm.value = false; // Đóng modal

    const hinhThuc = activeTabData.value.hd.hinh_thuc_thanh_toan;

    if (shouldPrint) {
        printInvoice();
    }

    if (hinhThuc === "Tiền mặt") {
        try {
            await store.trangThaiDonHang(activeTabData.value.hd.id_hoa_don);
            message.success('Thanh toán tiền mặt thành công!');
            localStorage.removeItem('khachHangBH')
            window.location.href = 'http://localhost:5173/admin/banhang';
        } catch (error) {
            console.error('Lỗi khi thanh toán:', error);
            message.error('Đã xảy ra lỗi khi thanh toán!');
        }
    } else if (hinhThuc === "Chuyển khoản") {
        try {
            // const res = await store.thanhToanMomo(activeTabData.value.hd.id_hoa_don);
            // Điều hướng đến trang thanh toán MoMo
            // window.location.href = res.payUrl;
            const payment_info = {
                productName: "Đơn hàng " + `GB-${activeTabData.value.hd.id_hoa_don}-${new Date().getTime()}`,
                description: `GB Sport - ${allProducts.value.length} sản phẩm`,
                returnUrl: "http://localhost:5173/admin/banhang",
                price: Number(activeTabData.value.hd.tong_tien_sau_giam || 0),
                cancelUrl: "http://localhost:5173/admin/banhang"
            }
            localStorage.setItem('checkPaymentStatus', 'true');
            localStorage.setItem('idHDPayMent', JSON.stringify(activeTabData.value.hd.id_hoa_don));
            localStorage.removeItem('khachHangBH')
            console.log(payment_info);
            await thanhToanService.handlePayOSPayment(payment_info);


        } catch (error) {
            console.error('Lỗi khi tạo yêu cầu thanh toán PayOS:', error);
            message.error('Không thể tạo thanh toán PayOs!');
        }
    }
};

const updateHinhThucThanhToan = async () => {
    try {
        const id = activeTabData.value.hd.id_hoa_don;
        const hinhThuc = activeTabData.value.hd.hinh_thuc_thanh_toan;

        await store.updateHinhThucTTHoaDon(id, hinhThuc);

        console.log("Đã cập nhật hình thức thanh toán:", hinhThuc);
    } catch (err) {
        console.error("Lỗi cập nhật hình thức thanh toán", err);
    }
};


const da = ref([]);

// --- Lifecycle Hooks ---
onMounted(async () => {
    // Kiểm tra luuTTKHBH ngay khi component được gắn
    await checkAndApplyLocalData();
    await loadData(); // Gọi lần đầu
    stopQrScanner();
    setupAutoReloadAtMidnight(); // Cài lịch chạy hằng ngày

    // Bắt đầu kiểm tra liên tục
    startChecking();

    stopQrScanner();
    setupAutoReloadAtMidnight();

    const checkPaymentStatus = localStorage.getItem('checkPaymentStatus');
    if (checkPaymentStatus === 'true') {
        try {
            const paymentResponse = JSON.parse(localStorage.getItem('paymentResponse'));
            const idhdpay = JSON.parse(localStorage.getItem('idHDPayMent'));
            console.log("idhdpay: ", idhdpay);
            if (paymentResponse && paymentResponse.data && paymentResponse.data.orderCode) {
                const paystatus = await thanhToanService.checkStatusPayment(paymentResponse.data.orderCode);
                console.log("paystatus: ", paystatus);

                if (paystatus.status === "PAID") {
                    console.log("Paid:", idhdpay);
                    await store.trangThaiDonHang(idhdpay);
                    router.push('/admin/banhang');
                    toast.success('Thanh toán thành công');
                    await refreshHoaDon(idhdpay);
                } else if (paystatus.status === "PENDING") {
                    console.log("Pending:", idhdpay);
                    toast.warning('Thanh toán đang chờ xử lý');
                } else if (paystatus.status === "CANCELLED") {
                    console.log("CANCELLED:", idhdpay);
                    toast.error('Thanh toán đã bị huỷ');
                }
            } else {
                console.error("Không tìm thấy thông tin thanh toán trong localStorage.");
            }
        } catch (error) {
            console.error("Lỗi khi kiểm tra trạng thái thanh toán:", error);
            toast.error('Không thể kiểm tra trạng thái thanh toán');
        } finally {
            // Xóa cờ sau khi kiểm tra xong
            localStorage.removeItem('checkPaymentStatus');
        }
    }

});

// Thiết lập setInterval để kiểm tra luuTTKHBH liên tục
let intervalId = null;
const startChecking = () => {
    intervalId = setInterval(async () => {
        await checkAndApplyLocalData();
    }, 1000); // Kiểm tra mỗi 1000ms
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
        await store.getAllNhanVien(0, pageSize.value);
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
    midnight.setHours(24, 0, 0, 0); // 00:00 của ngày mai

    const timeUntilMidnight = midnight.getTime() - now.getTime();

    // Đợi tới 00:00, rồi gọi loadData, sau đó chạy mỗi 24 giờ
    setTimeout(() => {
        loadData(); // chạy lần đầu vào 00:00
        setInterval(loadData, 24 * 60 * 60 * 1000); // gọi lại mỗi ngày
    }, timeUntilMidnight);
}


watch(() => activeKey.value, async (newKey) => {
    const currentTab = panes.value.find(p => p.key === newKey);
    if (currentTab && currentTab.hd.id_hoa_don) {
        await store.getAllSPHD(currentTab.hd.id_hoa_don);
        currentTab.items.value = store.getAllSPHDArr.map(item => ({
            id_hoa_don: item.id_hoa_don,
            id_chi_tiet_san_pham: item.id_chi_tiet_san_pham,
            hinh_anh: item.hinh_anh,
            ten_san_pham: item.ten_san_pham,
            mau_sac: item.ten_mau_sac || item.mau_sac || null,
            kich_thuoc: item.gia_tri || null,
            so_luong: item.so_luong,
            gia_ban: item.don_gia,
            tong_tien: item.don_gia * item.so_luong,
            so_luong_ton_goc: item.so_luong_ton || 0
        })) || [];
        console.log("store.getAllSPHDArr: ", currentTab.items.value);
    }
    ptnh.value = currentTab.hd.phuong_thuc_nhan_hang;
    store.setCurrentHoaDonId(currentTab.hd.id_hoa_don);

}, { immediate: true });

watch(() => searchQuery, (newVal) => {
    if (newVal.length > 0) {
        dropdownVisible.value = true
    } else {
        dropdownVisible.value = false
    }
})

watch(searchQuery, (newQuery) => {
    handleSearchInput(newQuery);
    dropdownVisible.value = true;
});

const isLoading = ref(false);

// Hàm kiểm tra luuTTKHBH với timeout
const checkAndApplyLocalData = async () => {
    const checkluuTTKHBH = JSON.parse(localStorage.getItem('luuTTKHBH'));
    if (checkluuTTKHBH === true) {
        console.log("Phát hiện luuTTKHBH = true, chờ 500ms để làm mới dữ liệu...");
        isLoading.value = true;
        // Chờ 500ms để đảm bảo API addKHHD hoàn tất
        await new Promise(resolve => setTimeout(resolve, 500));
        try {
            const idHoaDon = activeTabData.value.hd.id_hoa_don;
            console.log("ID hóa đơn để làm mới:", idHoaDon);
            await refreshHoaDon(idHoaDon);
            await handlePhuongThucChange();
            message.success("Đã làm mới thông tin hóa đơn!");
        } catch (error) {
            console.error("Lỗi khi làm mới dữ liệu:", error);
            message.error("Không thể làm mới dữ liệu hóa đơn!");
        } finally {
            localStorage.removeItem('luuTTKHBH');
            isLoading.value = false;
        }
    } else {
        console.log("Không tìm thấy luuTTKHBH trong localStorage.");
    }
};




function tachDiaChi(addressString) {
    if (!addressString) return null;

    const parts = addressString.split(',').map(p => p.trim());
    if (parts.length < 4) return null;

    const diaChi = {
        address: parts[0],                        // Số nhà 11
        ward: parts[1],                           // Phường Xuân Đỉnh
        district: parts[2],                       // Quận Bắc Từ Liêm
        province: parts[3],                       // Hà Nội
    };
    return diaChi;
}


const handlePhuongThucChange = async () => {
    const idHD = activeTabData.value.hd.id_hoa_don;
    const diaChiNhan = activeTabData.value.hd.dia_chi; // chuỗi full địa chỉ
    // const khachHangData = localStorage.getItem('khachHangBH');
    let phiShip = 0;
    const weight = 500; // gram — bạn có thể lấy từ thực tế hàng hóa
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
        console.log("Địa chỉ giao hàng:", diaChi);
        if (diaChi) {
            const result = await banHangService.tinhPhiShip(
                "Hà Nội", // pickProvince
                "Nam Từ Liêm", // pickDistrict
                diaChi.province,
                diaChi.district,
                weight,
                tongTienHoaDon
            );
            console.log("Kết quả tính phí ship:", result);
            phiShip = result.fee;
            activeTabData.value.hd.phi_van_chuyen = phiShip;
            console.log("Phí ship:", phiShip);
        } else {
            activeTabData.value.hd.phi_van_chuyen = 0;
            console.warn('⚠️ Không có địa chỉ giao hàng hợp lệ, phí = 0');
        }
        // refreshHoaDon(idHD);
        await store.setTrangThaiNhanHang(idHD, 'Giao hàng', phiShip);
    }

    // refreshHoaDon(idHD);
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
    background-color: #fff1f2;
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
    color: #f33b47;
}

/* Product Price and Name */
.product-price {
    font-size: 14px;
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
    background-color: #f33b47;
    border-color: #f33b47;
    color: white;
    transition: all 0.3s ease;
}

.action-btn:hover {
    background-color: #e02b37;
    border-color: #e02b37;
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
    color: #f33b47;
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
    background-color: #f33b47;
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
    border: 2px solid #f33b47;
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
    background-color: #f33b47;
    border-color: #f33b47;
    color: white;
    border-radius: 0 6px 6px 0;
}

:deep(.ant-input-search .ant-btn:hover) {
    background-color: #e02b37;
    border-color: #e02b37;
}

:deep(.ant-btn-primary) {
    background-color: #f33b47;
    border-color: #f33b47;
}

:deep(.ant-btn-primary:hover) {
    background-color: #e02b37;
    border-color: #e02b37;
}

:deep(.ant-modal-header) {
    background-color: #f33b47;
    color: white;
}

:deep(.ant-modal-title) {
    color: white;
}

:deep(.ant-table-thead > tr > th) {
    background-color: #fff1f2;
    color: #1f1f1f;
    font-weight: 600;
}

:deep(.ant-table-row:hover > td) {
    background-color: #fff8f8 !important;
}

/* Switch (Toggle) Styling */
:deep(.ant-switch) {
    background-color: #d9d9d9;
}

:deep(.ant-switch-checked) {
    background-color: #f33b47;
}

:deep(.ant-switch-checked:hover:not(.ant-switch-disabled)) {
    background-color: #e02b37;
}

:deep(.ant-switch-handle::before) {
    background-color: #ffffff;
}

/* Payment Button Styling */
:deep(.btn-primary) {
    background-color: #f33b47 !important;
    border-color: #f33b47 !important;
    color: white !important;
    border-radius: 6px;
    font-weight: 500;
    transition: all 0.3s ease;
}

:deep(.btn-primary:hover:not(:disabled)) {
    background-color: #e02b37 !important;
    border-color: #e02b37 !important;
}

:deep(.btn-primary:disabled) {
    background-color: #f4a6ac !important;
    border-color: #f4a6ac !important;
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
    background: #fff8f8;
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
    border-color: #f33b47;
    box-shadow: 0 0 0 3px rgba(243, 59, 71, 0.2);
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
    background-color: #f33b47;
    border-color: #f33b47;
    box-shadow: 0 0 0 2px rgba(243, 59, 71, 0.2);
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