<template>
    <div class="tracking-container">
        <!-- Breadcrumb -->
        <div class="breadcrumb-container">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a href="/home">Trang chủ</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Tra cứu đơn hàng</li>
                </ol>
            </nav>
        </div>

        <div class="tracking-content">
            <div class="tracking-header">
                <h1>Tra cứu đơn hàng</h1>
                <p class="tracking-description">
                    Nhập mã đơn hàng hoặc mã vận đơn để tra cứu thông tin và trạng thái đơn hàng của bạn
                </p>
            </div>

            <div class="tracking-search">
                <a-form layout="vertical" @finish="timKiemDonHang">
                    <a-form-item label="Mã đơn hàng / Mã vận đơn" name="trackingCode">
                        <a-input v-model:value="trackingCode" placeholder="Nhập mã đơn hàng hoặc mã vận đơn"
                            :maxLength="50" :allowClear="true" size="large" />
                    </a-form-item>
                    <a-form-item>
                        <a-button type="primary" html-type="submit" @click="timKiemDonHang" :loading="dangTai"
                            size="large" block>
                            <search-outlined /> Tra cứu
                        </a-button>
                    </a-form-item>
                </a-form>
            </div>

            <div class="search-results" v-if="daTimKiem">
                <!-- Not Found State -->
                <div v-if="!timThayDonHang" class="not-found">
                    <file-search-outlined class="not-found-icon" />
                    <h2>Không tìm thấy đơn hàng</h2>
                    <p>Không tìm thấy đơn hàng với mã đơn hàng / mã vận đơn bạn vừa nhập.</p>
                    <p>Vui lòng kiểm tra lại mã đơn hàng hoặc liên hệ với chúng tôi để được hỗ trợ.</p>
                </div>

                <!-- Order Found State -->
                <div v-else class="order-details">
                    <div class="order-header">
                        <div class="order-info">
                            <h2>Thông tin đơn hàng #{{ thongTinHoaDon.ma_hoa_don }}</h2>
                            <p v-if="thongTinHoaDon.ma_hoa_don">Ngày đặt hàng: {{
                                dinhDangNgayGio(thongTinHoaDon.ngay_tao)
                            }}
                            </p>
                        </div>
                        <div :class="['order-status', `status-${currentStatus?.code || 'pending'}`]">
                            <component :is="layBieuTuongTrangThai(currentStatus?.code || 'pending')"
                                class="status-icon" />
                            <span>{{ currentStatus?.name || 'Chờ xác nhận' }}</span>
                        </div>
                    </div>

                    <div class="tracking-timeline">
                        <div class="timeline-container">
                            <div v-for="(status, index) in getTimelineData" :key="index" :class="['timeline-step', {
                                active: kiemTraTrangThaiDangHoatDong(status.code),
                                completed: kiemTraTrangThaiDaHoanThanh(status.code),
                                cancelled: status.code === 'cancelled'
                            }]">
                                <div class="step-icon">
                                    <component :is="status.icon" />
                                </div>
                                <div class="step-content">
                                    <p class="step-name">{{ status.name }}</p>
                                    <p v-if="status.date" class="step-date">{{ dinhDangNgayGio(status.date) }}</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="order-summary">
                        <h3>Chi tiết đơn hàng</h3>
                        <div class="order-products">
                            <div class="product-item" v-for="(item, index) in thongTinHoaDonChiTiet" :key="index">
                                <div class="product-image">
                                    <img :src="item.hinh_anh" :alt="item.ten_san_pham" />
                                    <span class="product-quantity">{{ item.quantity }}</span>
                                </div>
                                <div class="product-details">
                                    <p class="product-name">{{ item.ten_san_pham }}</p>
                                    <p class="product-variant" v-if="item.ten_mau_sac || item.gia_tri">
                                        <span v-if="item.ten_mau_sac">Màu: {{ item.ten_mau_sac }}</span>
                                        <span v-if="item.gia_tri">Size: {{ item.gia_tri + " " + item.don_vi }}</span>
                                    </p>
                                </div>
                                <div class="product-price">
                                    {{ dinhDangTien(item.don_gia) }}
                                </div>
                            </div>
                        </div>

                        <div class="order-totals">
                            <div class="total-row">
                                <span>Tạm tính:</span>
                                <span>{{ dinhDangTien(thongTinHoaDon.tong_tien_truoc_giam) }}</span>
                            </div>
                            <div class="total-row" v-if="thongTinHoaDon.ma_voucher">
                                <span>Giảm giá:</span>
                                <span>{{ dinhDangTien(thongTinHoaDon.tong_tien_truoc_giam+ thongTinHoaDon.phi_van_chuyen - thongTinHoaDon.tong_tien_sau_giam) }}</span>
                            </div>
                            <div class="total-row">
                                <span>Phí vận chuyển:</span>
                                <span>{{ dinhDangTien(thongTinHoaDon.phi_van_chuyen) }}</span>
                            </div>
                            <div class="total-row grand-total">
                                <span>Tổng cộng:</span>
                                <span>{{ dinhDangTien(thongTinHoaDon.tong_tien_sau_giam) }}</span>
                            </div>
                        </div>

                        <div class="shipping-info">
                            <h3>Thông tin giao hàng</h3>
                            <div class="info-row">
                                <span class="info-label">Người nhận:</span>
                                <span class="info-value">{{ thongTinHoaDon.ho_ten }}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Số điện thoại:</span>
                                <span class="info-value">{{ thongTinHoaDon.sdt_nguoi_nhan }}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Địa chỉ:</span>
                                <span class="info-value">{{ thongTinHoaDon.dia_chi }}</span>
                            </div>
                            <div class="info-row" v-if="donHang.deliveryService">
                                <span class="info-label">Đơn vị vận chuyển:</span>
                                <span class="info-value">G-B Sports</span>
                            </div>
                            <div class="info-row" v-if="donHang.trackingNumber">
                                <span class="info-label">Mã vận đơn:</span>
                                <span class="info-value">{{ thongTinHoaDon.ma_hoa_don }}</span>
                            </div>
                        </div>

                        <div class="order-actions">
                            <a-button type="primary" @click="inHoaDon" :disabled="!timThayDonHang">
                                <printer-outlined /> In đơn hàng
                            </a-button>
                            <a-button>
                                <customer-service-outlined /> Liên hệ hỗ trợ
                            </a-button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Component in hóa đơn (ẩn mặc định) -->
            <div style="display: none;">
                <print-invoice ref="printInvoiceRef" :hoa-don="thongTinHoaDon"
                    :hoa-don-chi-tiet="thongTinHoaDonChiTiet" />
            </div>

            <!-- FAQ Section -->
            <div class="tracking-faqs">
                <h2>Các câu hỏi thường gặp</h2>
                <a-collapse>
                    <a-collapse-panel key="1" header="Làm sao để tìm mã đơn hàng của tôi?">
                        <p>Mã đơn hàng thường được gửi qua email xác nhận đơn hàng hoặc tin nhắn SMS sau khi bạn hoàn
                            tất đặt hàng.
                            Bạn cũng có thể tìm thấy mã đơn hàng trong lịch sử đơn hàng tại tài khoản của bạn.</p>
                    </a-collapse-panel>
                    <a-collapse-panel key="2" header="Tôi không nhận được mã vận đơn?">
                        <p>Mã vận đơn sẽ được cập nhật sau khi đơn hàng của bạn được xác nhận và bàn giao cho đơn vị vận
                            chuyển.
                            Quá trình này có thể mất từ 1-2 ngày làm việc. Nếu sau thời gian này bạn vẫn chưa nhận được,
                            vui lòng liên hệ với chúng tôi để được hỗ trợ.</p>
                    </a-collapse-panel>
                    <a-collapse-panel key="3" header="Làm thế nào để hủy đơn hàng?">
                        <p>Bạn có thể hủy đơn hàng khi đơn hàng đang ở trạng thái "Chờ xác nhận" hoặc "Đã xác nhận" và
                            chưa được giao cho đơn vị vận chuyển.
                            Để hủy đơn hàng, vui lòng nhập mã đơn hàng vào ô tìm kiếm và nhấn nút "Hủy đơn hàng" ở cuối
                            trang thông tin đơn hàng.</p>
                    </a-collapse-panel>
                    <a-collapse-panel key="4" header="Tôi muốn thay đổi thông tin giao hàng?">
                        <p>Để thay đổi thông tin giao hàng, vui lòng liên hệ với bộ phận Chăm sóc Khách hàng của chúng
                            tôi càng sớm càng tốt.
                            Lưu ý rằng nếu đơn hàng đã được giao cho đơn vị vận chuyển, chúng tôi có thể không thay đổi
                            được thông tin giao hàng.</p>
                    </a-collapse-panel>
                </a-collapse>
                <img src="../images/logo/logo2.png" alt="logo" id="logo" hidden>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue';
import {
    SearchOutlined,
    FileSearchOutlined,
    ClockCircleOutlined,
    CheckCircleOutlined,
    SyncOutlined,
    InboxOutlined,
    GiftOutlined,
    CarOutlined,
    TrophyOutlined,
    CloseCircleOutlined,
    PrinterOutlined,
    CustomerServiceOutlined
} from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';
import { banHangOnlineService } from '@/services/banHangOnlineService';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import QRCode from 'qrcode';
// Import font từ thư mục config
import '@/config/fonts/Roboto-normal.js';
import '@/config/fonts/Roboto-bold.js';

// Biến lưu mã tra cứu
const trackingCode = ref('');
const dangTai = ref(false);
const daTimKiem = ref(false);
const timThayDonHang = ref(false);
const isLoading = ref(false);
// Dữ liệu mẫu cho đơn hàng - Trong ứng dụng thực tế, dữ liệu này sẽ được lấy từ API
const donHang = reactive({
    orderCode: 'GB123456789',
    dateCreated: new Date(2023, 6, 15),
    status: {
        code: 'processing',
        name: 'Đang xử lý'
    },
    items: [
        {
            id: 1,
            name: 'Áo thể thao nam ProSport',
            color: 'Đen',
            size: 'L',
            price: 250000,
            quantity: 2,
            image: 'https://via.placeholder.com/80x80'
        },
        {
            id: 2,
            name: 'Giày chạy bộ UltraRun',
            color: 'Xanh Navy',
            size: '42',
            price: 1200000,
            quantity: 1,
            image: 'https://via.placeholder.com/80x80'
        }
    ],
    discount: 100000,
    shippingFee: 30000,
    total: 1630000,
    recipient: {
        name: 'Nguyễn Văn A',
        phone: '0987654321',
        address: '123 Đường Lê Lợi, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh'
    },
    deliveryService: 'Giao Hàng Nhanh',
    trackingNumber: 'GHN1234567890',
    statusHistory: [
        { code: 'pending', date: new Date(2023, 6, 15), name: 'Chờ xác nhận' },
        { code: 'confirmed', date: new Date(2023, 6, 16), name: 'Đã xác nhận' },
        { code: 'processing', date: new Date(2023, 6, 17), name: 'Đang xử lý' }
    ]
});
const thongTinHoaDon = ref({});
const thongTinTimeLine = ref([]);
const thongTinHoaDonChiTiet = ref([]);
// Định nghĩa các trạng thái trong timeline
const danhSachTrangThai = [
    { code: 'pending', name: 'Chờ xác nhận', icon: ClockCircleOutlined },
    { code: 'updated', name: 'Đã cập nhật', icon: SyncOutlined },
    { code: 'confirmed', name: 'Đã xác nhận', icon: CheckCircleOutlined },
    { code: 'packaging', name: 'Chờ đóng gói', icon: InboxOutlined },
    { code: 'shipping', name: 'Đang giao', icon: CarOutlined },
    { code: 'completed', name: 'Hoàn thành', icon: TrophyOutlined }
];

// Ánh xạ mã trạng thái và thứ tự trong timeline
const viTriTrangThai = {
    'pending': 0,
    'updated': 1,
    'confirmed': 2,
    'packaging': 3,
    'shipping': 4,
    'completed': 5,
    'cancelled': -1
};

// Tính tổng giá trị tạm tính
const tongTamTinh = computed(() => {
    return donHang.items.reduce((total, item) => total + (item.price * item.quantity), 0);
});

// Kiểm tra xem có thể hủy đơn hàng hay không
const coTheHuyDonHang = computed(() => {
    if (!currentStatus.value) return false;
    const trangThaiCoTheHuy = ['pending', 'confirmed', 'updated'];
    return trangThaiCoTheHuy.includes(currentStatus.value.code);
});

// Thêm hiệu ứng khi hiển thị kết quả tìm kiếm
const hienThiKetQuaTimKiem = async () => {
    try {
        dangTai.value = true;
        isLoading.value = true;

        // Gọi API và kiểm tra response
        const thongTinHoaDonResponse = await banHangOnlineService.getThongTinHoaDon(trackingCode.value);
        if (thongTinHoaDonResponse) {
            thongTinHoaDon.value = thongTinHoaDonResponse;
        }

        const thongTinTimeLineResponse = await banHangOnlineService.getThongTinTimeLine(trackingCode.value);
        if (thongTinTimeLineResponse) {
            thongTinTimeLine.value = thongTinTimeLineResponse;
        }

        const thongTinHoaDonChiTietResponse = await banHangOnlineService.getThongTinHoaDonChiTiet(trackingCode.value);
        if (thongTinHoaDonChiTietResponse) {
            thongTinHoaDonChiTiet.value = thongTinHoaDonChiTietResponse;
        }

        daTimKiem.value = true;

        // Kiểm tra mã hóa đơn
        if (trackingCode.value === thongTinHoaDon.value.ma_hoa_don) {
            timThayDonHang.value = true;
            dataLoaded.value = true;
            message.success('Tìm thấy thông tin đơn hàng!');
        } else {
            timThayDonHang.value = false;
            dataLoaded.value = false;
            message.error('Không tìm thấy đơn hàng với mã bạn đã nhập!');
        }
    } catch (error) {
        console.error('Lỗi khi tìm kiếm đơn hàng:', error);
        message.error('Có lỗi xảy ra khi tìm kiếm đơn hàng');
    } finally {
        dangTai.value = false;
        isLoading.value = false;
    }
};

// Thay đổi hàm tìm kiếm để gọi hàm hiển thị kết quả
const timKiemDonHang = () => {
    if (!trackingCode.value) {
        message.warning('Vui lòng nhập mã đơn hàng hoặc mã vận đơn');
        return;
    }

    hienThiKetQuaTimKiem();
};

// Cập nhật lịch sử trạng thái đơn hàng cho demo
const capNhatLichSuTrangThai = (maTrangThai) => {
    // Xóa lịch sử hiện tại
    donHang.statusHistory = [];

    // Thêm lịch sử dựa vào trạng thái hiện tại
    const danhSachMaTrangThai = ['pending', 'updated', 'confirmed', 'packaging', 'shipping', 'completed'];
    const viTriHienTai = danhSachMaTrangThai.indexOf(maTrangThai);

    if (maTrangThai === 'cancelled') {
        // Nếu hủy, chỉ thêm trạng thái chờ xác nhận và đã hủy
        donHang.statusHistory.push(
            { code: 'pending', date: new Date(2023, 6, 15), name: 'Chờ xác nhận' },
            { code: 'cancelled', date: new Date(), name: 'Đã hủy' }
        );
    } else {
        // Thêm lịch sử đến trạng thái hiện tại
        for (let i = 0; i <= viTriHienTai; i++) {
            const ngay = new Date();
            ngay.setDate(ngay.getDate() - (viTriHienTai - i));
            donHang.statusHistory.push({
                code: danhSachMaTrangThai[i],
                date: ngay,
                name: layTenTrangThai(danhSachMaTrangThai[i])
            });
        }
    }
};

// Lấy tên trạng thái từ mã
const layTenTrangThai = (maTrangThai) => {
    const trangThai = danhSachTrangThai.find(s => s.code === maTrangThai);
    return trangThai ? trangThai.name : '';
};

// Lấy vị trí trạng thái
const layViTriTrangThai = (maTrangThai) => {
    return viTriTrangThai[maTrangThai] !== undefined ? viTriTrangThai[maTrangThai] : -1;
};

// Kiểm tra xem trạng thái có đang active (trạng thái hiện tại)
const kiemTraTrangThaiDangHoatDong = (maTrangThai) => {
    if (!currentStatus.value) return false;

    // Nếu đơn hàng đã hủy
    if (currentStatus.value.code === 'cancelled') {
        // Chỉ trạng thái "Đã hủy" là active
        return maTrangThai === 'cancelled';
    }

    // Nếu không hủy, kiểm tra xem trạng thái có khớp với trạng thái hiện tại không
    return maTrangThai === currentStatus.value.code;
};

// Kiểm tra xem trạng thái đã hoàn thành chưa (trạng thái đã qua)
const kiemTraTrangThaiDaHoanThanh = (maTrangThai) => {
    if (!currentStatus.value) return false;

    // Nếu đơn hàng đã hủy
    if (currentStatus.value.code === 'cancelled') {
        // Đối với đơn hàng đã hủy, chỉ các trạng thái đã thực sự xảy ra (có date) mới được đánh dấu là đã hoàn thành
        // trừ trạng thái "Đã hủy" (vì nó đang active)
        if (maTrangThai === 'cancelled') return false;

        // Tìm trạng thái trong danh sách 
        const allStatuses = getTimelineData.value;
        const statusObj = allStatuses.find(s => s.code === maTrangThai);

        // Nếu trạng thái này có ngày tháng, tức là đã xảy ra
        return statusObj && statusObj.date !== null;
    }

    // Nếu không phải hủy, so sánh vị trí trong quy trình
    const viTriTrangThaiHienTai = layViTriTrangThai(currentStatus.value.code);
    const viTriTrangThaiKiemTra = layViTriTrangThai(maTrangThai);

    return viTriTrangThaiKiemTra < viTriTrangThaiHienTai && viTriTrangThaiKiemTra !== -1;
};

// Lấy biểu tượng trạng thái dựa trên mã trạng thái
const layBieuTuongTrangThai = (maTrangThai) => {
    switch (maTrangThai) {
        case 'pending':
            return ClockCircleOutlined;
        case 'updated':
            return SyncOutlined;
        case 'confirmed':
            return CheckCircleOutlined;
        case 'packaging':
            return InboxOutlined;
        case 'shipping':
            return CarOutlined;
        case 'completed':
            return TrophyOutlined;
        case 'cancelled':
            return CloseCircleOutlined;
        default:
            return ClockCircleOutlined;
    }
};

// Định dạng ngày tháng
const dinhDangNgay = (ngay) => {
    if (!ngay) return '';
    return new Date(ngay).toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
    });
};

// Định dạng ngày giờ đầy đủ
const dinhDangNgayGio = (ngay) => {
    if (!ngay) return '';
    return new Date(ngay).toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
    });
};

// Định dạng tiền tệ
const dinhDangTien = (giaTri) => {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0
    }).format(giaTri);
};

// Hiện hộp thoại xác nhận hủy đơn hàng
const hienThiXacNhanHuy = () => {
    Modal.confirm({
        title: 'Xác nhận hủy đơn hàng',
        content: 'Bạn có chắc chắn muốn hủy đơn hàng này không? Hành động này không thể hoàn tác.',
        okText: 'Đồng ý',
        okType: 'danger',
        cancelText: 'Hủy bỏ',
        onOk() {
            huyDonHang();
        }
    });
};

// Hủy đơn hàng
const huyDonHang = () => {
    dangTai.value = true;

    // Mô phỏng gọi API
    setTimeout(() => {
        donHang.status = { code: 'cancelled', name: 'Đã hủy' };
        capNhatLichSuTrangThai('cancelled');
        message.success('Đơn hàng đã được hủy thành công');
        dangTai.value = false;
    }, 1000);
};

// Cập nhật phần computed và methods
const mapTrangThaiToCode = (trangThai) => {
    if (!trangThai) return 'pending';

    // Chuẩn hóa chuỗi bằng cách loại bỏ khoảng trắng thừa
    const cleanedTrangThai = trangThai.trim();

    const mappings = {
        'Chờ xác nhận': 'pending',
        'Đã cập nhật': 'updated',
        'Đã xác nhận': 'confirmed',
        'Chờ đóng gói': 'packaging',
        'Đang giao': 'shipping',
        'Hoàn thành': 'completed',
        'Đã hoàn thành': 'completed',
        'Đã hủy': 'cancelled',
        'Hủy': 'cancelled'
    };

    return mappings[cleanedTrangThai] || 'pending';
};

// Thêm computed property để xử lý trạng thái từ API
const currentStatus = computed(() => {
    if (!thongTinTimeLine.value || thongTinTimeLine.value.length === 0) return null;

    // Lấy trạng thái mới nhất (trạng thái cuối cùng trong mảng)
    const latestStatus = thongTinTimeLine.value[thongTinTimeLine.value.length - 1];

    return {
        code: mapTrangThaiToCode(latestStatus.trang_thai),
        name: latestStatus.trang_thai,
        date: latestStatus.ngay_tao || latestStatus.ngay_chuyen
    };
});

// Cập nhật timeline để sử dụng dữ liệu từ API
const getTimelineData = computed(() => {
    if (!thongTinTimeLine.value || thongTinTimeLine.value.length === 0) return [];

    // Tạo danh sách các trạng thái cố định
    const allStatuses = [
        { code: 'pending', name: 'Chờ xác nhận', date: null, icon: layBieuTuongTrangThai('pending') },
        { code: 'confirmed', name: 'Đã xác nhận', date: null, icon: layBieuTuongTrangThai('confirmed') },
        { code: 'packaging', name: 'Chờ đóng gói', date: null, icon: layBieuTuongTrangThai('packaging') },
        { code: 'shipping', name: 'Đang giao', date: null, icon: layBieuTuongTrangThai('shipping') },
        { code: 'completed', name: 'Hoàn thành', date: null, icon: layBieuTuongTrangThai('completed') }
    ];

    // Object để lưu trữ và sắp xếp dữ liệu theo từng trạng thái
    const statusRecords = {};

    // Kiểm tra xem có trạng thái hủy không
    let hasCancelledStatus = false;
    let cancelledDate = null;

    // Phân loại dữ liệu theo trạng thái
    thongTinTimeLine.value.forEach(item => {
        const statusCode = mapTrangThaiToCode(item.trang_thai);
        // Ưu tiên ngay_chuyen, nếu không có thì dùng ngay_tao
        const date = item.ngay_chuyen || item.ngay_tao;

        if (!date) return;

        // Kiểm tra nếu là trạng thái hủy
        if (statusCode === 'cancelled') {
            hasCancelledStatus = true;
            cancelledDate = new Date(date);
            return;
        }

        if (!statusRecords[statusCode]) {
            statusRecords[statusCode] = [];
        }

        statusRecords[statusCode].push({
            date: new Date(date),
            record: item
        });
    });

    // Sắp xếp và cập nhật ngày cho các trạng thái
    Object.keys(statusRecords).forEach(statusCode => {
        const records = statusRecords[statusCode];
        if (records && records.length > 0) {
            // Sắp xếp theo ngày tăng dần
            records.sort((a, b) => a.date - b.date);

            // Tìm và cập nhật trạng thái tương ứng
            const statusIndex = allStatuses.findIndex(s => s.code === statusCode);
            if (statusIndex >= 0) {
                // Sử dụng ngày của bản ghi đầu tiên (sớm nhất)
                allStatuses[statusIndex].date = records[0].date;
            }
        }
    });

    // Kiểm tra Chờ xác nhận trước
    if (!allStatuses[0].date && thongTinHoaDon.value.ngay_tao) {
        allStatuses[0].date = new Date(thongTinHoaDon.value.ngay_tao);
    }

    // Nếu đơn hàng bị hủy, thêm trạng thái hủy vào cuối timeline
    if (hasCancelledStatus || (currentStatus.value && currentStatus.value.code === 'cancelled')) {
        // Tìm ngày hủy đơn, sử dụng cancelledDate nếu đã tìm thấy, nếu không sử dụng từ currentStatus
        const finalCancelledDate = cancelledDate ||
            (currentStatus.value && currentStatus.value.code === 'cancelled' ?
                new Date(currentStatus.value.date) : new Date());

        // Tạo bản sao của allStatuses để tránh thay đổi mảng gốc
        const result = [...allStatuses];

        // Thêm trạng thái hủy vào cuối
        result.push({
            code: 'cancelled',
            name: 'Đã hủy',
            date: finalCancelledDate,
            icon: layBieuTuongTrangThai('cancelled')
        });

        return result;
    }

    return allStatuses;
});

// Hàm xuất hóa đơn PDF
const inHoaDon = async () => {
    try {
        // Kiểm tra dữ liệu
        if (!thongTinHoaDon.value || !thongTinHoaDonChiTiet.value || thongTinHoaDonChiTiet.value.length === 0) {
            message.error('Không có dữ liệu hóa đơn để in');
            return;
        }

        // Hiển thị loading
        message.loading({ content: 'Đang tạo hóa đơn...', key: 'pdfLoading' });
        
        // Tạo một tài liệu PDF mới với kích thước A4
        const doc = new jsPDF({
            orientation: 'portrait',
            unit: 'mm',
            format: 'a4'
        });

        // Thiết lập font mặc định cho tài liệu và các thông số căn lề
        doc.setFont('Roboto', 'normal');
        const pageWidth = doc.internal.pageSize.width;
        const pageHeight = doc.internal.pageSize.height;
        const margin = 20; // Lề 20mm cho tất cả các cạnh
        const usableWidth = pageWidth - (margin * 2);
        const logo = document.getElementById('logo');
         // Thêm mã QR chứa mã hóa đơn ở góc trái
         try {
            // Thêm mã QR chứa mã hóa đơn ở góc trái
            const qrCodeData = thongTinHoaDon.value.ma_hoa_don || 'UNKNOWN';
            const qrDataUrl = await QRCode.toDataURL(qrCodeData);
            doc.addImage(qrDataUrl, 'PNG', margin, 20, 25, 25);
            
            doc.setFontSize(8);
            doc.text('Mã hóa đơn:', margin + 12.5, 50, { align: 'center' });
            doc.setFont('Roboto', 'bold');
            doc.text(qrCodeData, margin + 12.5, 55, { align: 'center' });
        } catch (error) {
            console.error('Không thể tạo mã QR:', error);
            // Vẽ một hình chữ nhật đơn giản thay thế nếu không thể tạo mã QR
            doc.setFillColor(240, 240, 240);
            doc.rect(margin, 20, 25, 25, 'F');
            doc.setFontSize(8);
            doc.text('Mã QR', margin + 12.5, 32.5, { align: 'center' });
        }
        
        // Thêm logo ở giữa và phía trên
        try {
            doc.addImage(logo.src, 'PNG', pageWidth / 2 - 20, 20, 40, 20);
        } catch (error) {
            console.error('Không thể tải logo:', error);
            // Vẽ một hình chữ nhật đơn giản thay thế nếu không thể tải logo
            doc.setFillColor(240, 240, 240);
            doc.rect(pageWidth / 2 - 17.5, 20, 35, 15, 'F');
            doc.setFontSize(10);
            doc.setTextColor(100, 100, 100);
            doc.text('Logo GB', pageWidth / 2, 27.5, { align: 'center' });
        }
        
        // Thông tin cửa hàng - header đơn giản, ở dưới logo và căn giữa
        doc.setFontSize(16);
        doc.setFont('Roboto', 'bold');
       
        doc.setFontSize(9);
        doc.setFont('Roboto', 'normal');
        doc.text('Địa chỉ: 123 Đường Thế Thao, Quận Cầu Giấy, Hà Nội', pageWidth / 2, 45, { align: 'center' });
        doc.text('Điện thoại: (024) 123 4567 | Email: info@gbsports.com', pageWidth / 2, 50, { align: 'center' });
        
        
        // Tiêu đề hóa đơn - Đơn giản và căn giữa
        doc.setFontSize(14);
        doc.setFont('Roboto', 'bold');
        doc.text('HÓA ĐƠN BÁN HÀNG', pageWidth / 2, 60, { align: 'center' });
        
        // Mã và ngày tạo hóa đơn
        doc.setFontSize(10);
        doc.setFont('Roboto', 'normal');
        doc.text(`Mã hóa đơn: ${thongTinHoaDon.value.ma_hoa_don || 'N/A'}`, pageWidth / 2, 67, { align: 'center' });
        const ngayDat = dinhDangNgay(thongTinHoaDon.value.ngay_tao);
        doc.text(`Ngày đặt: ${ngayDat || 'N/A'}`, pageWidth / 2, 72, { align: 'center' });
        
        // Đường gạch ngang phân cách
        doc.setDrawColor(200, 200, 200);
        doc.setLineWidth(0.5);
        doc.line(margin, 75, pageWidth - margin, 75);
        
        // Thông tin khách hàng - Đơn giản và rõ ràng
        let y = 85;
        doc.setFontSize(11);
        doc.setFont('Roboto', 'bold');
        doc.text('THÔNG TIN KHÁCH HÀNG', margin, y);
        
        y += 8;
        doc.setFontSize(10);
        doc.setFont('Roboto', 'normal');
        
        doc.text('Họ tên:', margin, y);
        doc.text(`${thongTinHoaDon.value.ho_ten || 'N/A'}`, margin + 60, y);
        
        y += 6;
        doc.text('Điện thoại:', margin, y);
        doc.text(`${thongTinHoaDon.value.sdt_nguoi_nhan || 'N/A'}`, margin + 60, y);
        
        y += 6;
        doc.text('Địa chỉ:', margin, y);
        const diaChi = thongTinHoaDon.value.dia_chi || 'N/A';
        const diaChiLines = doc.splitTextToSize(diaChi, usableWidth - 60);
        doc.text(diaChiLines, margin + 60, y);
        
        // Cập nhật vị trí y dựa trên số dòng của địa chỉ
        y += diaChiLines.length * 5 + 2;
        
        // Email (nếu có)
        if (thongTinHoaDon.value.email) {
            doc.text('Email:', margin, y);
            doc.text(thongTinHoaDon.value.email, margin + 60, y);
            y += 6;
        }
        
        // Phương thức thanh toán
        doc.text('Phương thức:', margin, y);
        doc.text(`${thongTinHoaDon.value.hinh_thuc_thanh_toan || 'Chuyển khoản'}`, margin + 60, y);
        
        y += 6;
        doc.text('Trạng thái:', margin, y);
        doc.text(`${currentStatus?.value?.name || 'Chờ xác nhận'}`, margin + 60, y);
        
        // Đường gạch ngang phân cách
        y += 10;
        doc.line(margin, y, pageWidth - margin, y);
        
        // Chi tiết đơn hàng - Tiêu đề
        y += 10;
        doc.setFontSize(11);
        doc.setFont('Roboto', 'bold');
        doc.text('CHI TIẾT ĐƠN HÀNG', margin, y);
        
        y += 7;
        
        // Tạo dữ liệu bảng sản phẩm
        const tableColumn = ['STT', 'Tên sản phẩm', 'Phân loại', 'SL', 'Đơn giá', 'Thành tiền'];
        const tableRows = [];
        
        try {
            thongTinHoaDonChiTiet.value.forEach((item, index) => {
                // Tính đơn giá thực tế = thành tiền / số lượng
                const soLuong = parseInt(item.so_luong) || 1;
                const thanhTien = parseInt(item.don_gia) || 0;
                const donGiaThuc = Math.round(thanhTien / soLuong);
                
                let phanLoai = '';
                if (item.ten_mau_sac) {
                    phanLoai += `Màu: ${item.ten_mau_sac}`;
                }
                if (item.gia_tri) {
                    if (phanLoai) phanLoai += '\nSize: ';
                    else phanLoai += 'Size: ';
                    phanLoai += `${item.gia_tri}${item.don_vi ? ' ' + item.don_vi : ''}`;
                }
                
                const itemData = [
                    index + 1,
                    item.ten_san_pham,
                    phanLoai,
                    soLuong,
                    dinhDangTien(donGiaThuc).replace('₫', 'đ'),
                    dinhDangTien(thanhTien).replace('₫', 'đ')
                ];
                tableRows.push(itemData);
            });
        } catch (err) {
            console.error('Lỗi khi xử lý dữ liệu sản phẩm:', err);
        }
        
        // Vẽ bảng sản phẩm với kiểu dáng đơn giản
        try {
            autoTable(doc, {
                head: [tableColumn],
                body: tableRows,
                startY: y,
                margin: { left: margin, right: margin },
                theme: 'grid',
                styles: { 
                    fontSize: 9, 
                    font: 'Roboto', 
                    cellPadding: 4,
                    lineColor: [200, 200, 200],
                    lineWidth: 0.1,
                    valign: 'middle'
                },
                headStyles: { 
                    fillColor: [240, 240, 240], 
                    textColor: [50, 50, 50], 
                    fontStyle: 'bold',
                    halign: 'center'
                },
                columnStyles: {
                    0: { cellWidth: 15, halign: 'center' },
                    1: { cellWidth: null },
                    2: { cellWidth: 30 },
                    3: { cellWidth: 12, halign: 'center' },
                    4: { cellWidth: 25, halign: 'right' },
                    5: { cellWidth: 30, halign: 'right' }
                },
                alternateRowStyles: { 
                    fillColor: [250, 250, 250] 
                },
                didDrawPage: (data) => {
                    // Thêm số trang nếu có nhiều trang
                    doc.setFontSize(8);
                    doc.setTextColor(100, 100, 100);
                    doc.text(
                        `Trang ${data.pageNumber} / ${doc.getNumberOfPages()}`, 
                        pageWidth / 2, 
                        pageHeight - 10, 
                        { align: 'center' }
                    );
                }
            });
        } catch (tableErr) {
            console.error('Lỗi khi tạo bảng sản phẩm:', tableErr);
            y += 10; // Tăng y để tiếp tục nếu có lỗi
        }
        
        // Cập nhật vị trí sau bảng
        y = doc.lastAutoTable ? doc.lastAutoTable.finalY + 10 : y + 60;
        
        // Phần tổng tiền - đơn giản và rõ ràng
        doc.setFontSize(10);
        doc.setFont('Roboto', 'normal');
        
        // Tổng tiền trước giảm
        doc.text('Tổng tiền hàng:', pageWidth - margin - 80, y);
        doc.text(`${dinhDangTien(thongTinHoaDon.value.tong_tien_truoc_giam).replace('₫', 'đ')}`, pageWidth - margin, y, { align: 'right' });
        
        y += 7;
        
        // Giảm giá (voucher)
        const giamGia = thongTinHoaDon.value.tong_tien_truoc_giam - thongTinHoaDon.value.tong_tien_sau_giam - thongTinHoaDon.value.phi_van_chuyen;
        doc.text('Giảm giá:', pageWidth - margin - 80, y);
        if (giamGia > 0) {
            doc.text(`${dinhDangTien(giamGia).replace('₫', 'đ')}`, pageWidth - margin, y, { align: 'right' });
        } else {
            doc.text('0 đ', pageWidth - margin, y, { align: 'right' });
        }
        
        y += 7;
        
        // Phí vận chuyển
        doc.text('Phí vận chuyển:', pageWidth - margin - 80, y);
        if (thongTinHoaDon.value.phi_van_chuyen > 0) {
            doc.text(`${dinhDangTien(thongTinHoaDon.value.phi_van_chuyen).replace('₫', 'đ')}`, pageWidth - margin, y, { align: 'right' });
        } else {
            doc.text('0 đ', pageWidth - margin, y, { align: 'right' });
        }
        
        // Đường kẻ phân cách trước tổng thanh toán
        y += 5;
        doc.setDrawColor(200, 200, 200);
        doc.line(pageWidth - margin - 80, y, pageWidth - margin, y);
        
        y += 7;
        
        // Tổng thanh toán
        doc.setFontSize(12);
        doc.setFont('Roboto', 'bold');
        doc.text('TỔNG THANH TOÁN:', pageWidth - margin - 80, y);
        doc.text(`${dinhDangTien(thongTinHoaDon.value.tong_tien_sau_giam).replace('₫', 'đ')}`, pageWidth - margin, y, { align: 'right' });
        
        // Chữ ký
        y += 20;
        
        // Đường kẻ phân cách
        doc.setDrawColor(200, 200, 200);
        doc.line(margin, y, pageWidth - margin, y);
        y += 10;
        
        // Thêm khoảng trống cho chữ ký
        y += 25;
        
        // Footer đơn giản
        y = pageHeight - 20;
        doc.setFontSize(9);
        doc.setFont('Roboto', 'bold');
        doc.text('Cảm ơn quý khách đã mua hàng tại G-B Sports!', pageWidth / 2, y, { align: 'center' });
        
        // Đóng loading
        message.destroy('pdfLoading');
        
        // Xuất file PDF
        doc.save(`HoaDon_${thongTinHoaDon.value.ma_hoa_don || 'GB'}.pdf`);
        message.success('Đã tạo hóa đơn PDF thành công');
    } catch (error) {
        console.error('Lỗi khi tạo hóa đơn PDF:', error);
        message.destroy('pdfLoading');
        message.error('Không thể tạo hóa đơn PDF. Vui lòng thử lại sau.');
    }
};
</script>

<style scoped>
.tracking-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    background-color: #f7f9fc;
    min-height: 100vh;
}

/* Breadcrumb styles */
.breadcrumb-container {
    margin-bottom: 30px;
    animation: fadeIn 0.8s ease-in-out;
}

.breadcrumb {
    display: flex;
    list-style: none;
    padding: 0;
    margin: 0;
}

.breadcrumb-item {
    display: inline-block;
    font-size: 14px;
}

.breadcrumb-item+.breadcrumb-item::before {
    content: '/';
    padding: 0 8px;
    color: #6c757d;
}

.breadcrumb-item a {
    color: #3f6ad8;
    text-decoration: none;
    transition: all 0.3s ease;
}

.breadcrumb-item a:hover {
    color: #2952cc;
    text-decoration: underline;
}

.breadcrumb-item.active {
    color: #6c757d;
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
    margin-bottom: 15px;
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

/* Order details styling */
.order-details {
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.07);
    margin-bottom: 40px;
    overflow: hidden;
    animation: fadeInUp 0.8s ease-in-out;
}

.order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 25px;
    background: linear-gradient(120deg, #f8f9fa, #eef2f7);
    border-bottom: 1px solid #eee;
    flex-wrap: wrap;
    gap: 15px;
}

.order-info h2 {
    font-size: 22px;
    font-weight: 700;
    color: #2b3940;
    margin-bottom: 5px;
}

.order-info p {
    color: #6c757d;
    margin: 0;
    font-size: 15px;
}

.order-status {
    display: flex;
    align-items: center;
    padding: 10px 20px;
    border-radius: 50px;
    font-weight: 600;
    transition: all 0.3s ease;
    box-shadow: 0 5px 10px rgba(0, 0, 0, 0.05);
}

.order-status:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
}

.status-icon {
    margin-right: 8px;
    font-size: 20px;
}

.status-pending {
    background: linear-gradient(45deg, #fff7e6, #fff1dc);
    color: #fa8c16;
    border: none;
}

.status-updated {
    background: linear-gradient(45deg, #e6f7ff, #d1e9ff);
    color: #1890ff;
    border: none;
}

.status-confirmed {
    background: linear-gradient(45deg, #f6ffed, #e8f9e0);
    color: #52c41a;
    border: none;
}

.status-packaging {
    background: linear-gradient(45deg, #f0f5ff, #e1ebff);
    color: #2f54eb;
    border: none;
}

.status-shipping {
    background: linear-gradient(45deg, #fcf4ff, #f4e6ff);
    color: #722ed1;
    border: none;
}

.status-completed {
    background: linear-gradient(45deg, #f6ffed, #e8f9e0);
    color: #52c41a;
    border: none;
}

.status-cancelled {
    background: linear-gradient(45deg, #fff1f0, #ffdfde);
    color: #f5222d;
    border: none;
}

/* Timeline styling */
.tracking-timeline {
    padding: 40px 30px;
    border-bottom: 1px solid #eee;
}

.timeline-container {
    display: flex;
    justify-content: space-between;
    position: relative;
    max-width: 900px;
    margin: 0 auto;
}

.timeline-container::before {
    content: '';
    position: absolute;
    top: 30px;
    left: 50px;
    right: 50px;
    height: 4px;
    background: linear-gradient(90deg, #f0f0f0, #e0e0e0);
    z-index: 1;
    border-radius: 4px;
}

.timeline-step {
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
    z-index: 2;
    flex: 1;
    transition: all 0.5s ease;
}

.step-icon {
    width: 70px;
    height: 70px;
    border-radius: 50%;
    background-color: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 15px;
    border: 4px solid #eee;
    color: #d9d9d9;
    font-size: 28px;
    transition: all 0.5s cubic-bezier(0.68, -0.6, 0.32, 1.6);
    position: relative;
    overflow: hidden;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.step-icon::after {
    content: '';
    position: absolute;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.15);
    top: -50%;
    left: -50%;
    transform: rotate(45deg);
    transition: all 0.3s ease-in-out;
}

.timeline-step:hover .step-icon::after {
    top: 150%;
    left: 150%;
}

.timeline-step:hover .step-icon {
    transform: scale(1.1);
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.timeline-step.active .step-icon {
    background-color: #fff;
    border-color: currentColor;
    transform: scale(1.1);
    animation: pulse 2s infinite;
}

.timeline-step.completed .step-icon {
    background-color: #f6ffed;
    border-color: #52c41a;
    color: #52c41a;
    transform: scale(1.05);
}

.timeline-step.completed .step-icon::before {
    content: "";
    position: absolute;
    width: 100%;
    height: 100%;
    background: rgba(82, 196, 26, 0.1);
    border-radius: 50%;
    transform: scale(0);
    animation: ripple 1.5s ease-out 1;
}

.timeline-step.active.cancelled .step-icon,
.timeline-step.cancelled .step-icon {
    background-color: #fff1f0;
    border-color: #f5222d;
    color: #f5222d;
}

.timeline-step:nth-child(1).active .step-icon {
    color: #fa8c16;
    /* Pending */
    box-shadow: 0 10px 20px rgba(250, 140, 22, 0.2);
}

.timeline-step:nth-child(2).active .step-icon {
    color: #1890ff;
    /* Updated */
    box-shadow: 0 10px 20px rgba(24, 144, 255, 0.2);
}

.timeline-step:nth-child(3).active .step-icon {
    color: #52c41a;
    /* Confirmed */
    box-shadow: 0 10px 20px rgba(82, 196, 26, 0.2);
}

.timeline-step:nth-child(4).active .step-icon {
    color: #2f54eb;
    /* Packaging */
    box-shadow: 0 10px 20px rgba(47, 84, 235, 0.2);
}

.timeline-step:nth-child(5).active .step-icon {
    color: #722ed1;
    /* Shipping */
    box-shadow: 0 10px 20px rgba(114, 46, 209, 0.2);
}

.timeline-step:nth-child(6).active .step-icon {
    color: #52c41a;
    /* Completed */
    box-shadow: 0 10px 20px rgba(82, 196, 26, 0.2);
}

.step-content {
    text-align: center;
    max-width: 120px;
    transition: all 0.3s ease;
}

.timeline-step:hover .step-content {
    transform: translateY(5px);
}

.step-name {
    font-weight: 600;
    color: #666;
    margin-bottom: 5px;
    font-size: 15px;
    transition: all 0.3s ease;
}

.timeline-step.active .step-name {
    color: #333;
    font-size: 16px;
}

.timeline-step:hover .step-name {
    color: #333;
}

.step-date {
    font-size: 13px;
    color: #999;
    margin: 0;
}

/* Order summary */
.order-summary {
    padding: 40px 30px;
    animation: fadeIn 0.8s ease-in-out;
}

.order-summary h3 {
    font-size: 22px;
    font-weight: 700;
    color: #2b3940;
    margin-bottom: 25px;
    padding-bottom: 15px;
    border-bottom: 2px solid #f0f2f5;
    position: relative;
}

.order-summary h3::after {
    content: '';
    position: absolute;
    width: 80px;
    height: 2px;
    background: linear-gradient(90deg, #3f6ad8, #25b0ed);
    bottom: -2px;
    left: 0;
    border-radius: 2px;
}

.order-products {
    margin-bottom: 30px;
}

.product-item {
    display: flex;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #f5f5f5;
    transition: all 0.3s ease;
    border-radius: 8px;
}

.product-item:hover {
    background-color: #f9fafc;
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.03);
}

.product-image {
    position: relative;
    width: 90px;
    height: 90px;
    border-radius: 10px;
    overflow: hidden;
    margin-right: 20px;
    border: none;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
}

.product-item:hover .product-image {
    transform: scale(1.05);
}

.product-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: all 0.5s ease;
}

.product-item:hover .product-image img {
    transform: scale(1.1);
}

.product-quantity {
    position: absolute;
    top: -10px;
    right: -10px;
    background: linear-gradient(135deg, #3f6ad8, #1890ff);
    color: #fff;
    border-radius: 50%;
    width: 26px;
    height: 26px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    font-weight: 700;
    box-shadow: 0 4px 8px rgba(24, 144, 255, 0.25);
    border: 2px solid #fff;
    z-index: 1;
}

.product-details {
    flex: 1;
}

.product-name {
    font-weight: 600;
    margin-bottom: 8px;
    font-size: 17px;
    color: #2b3940;
    transition: all 0.3s ease;
}

.product-item:hover .product-name {
    color: #3f6ad8;
}

.product-variant {
    font-size: 14px;
    color: #6c757d;
    background-color: #f8f9fa;
    padding: 3px 10px;
    border-radius: 20px;
    display: inline-block;
}

.product-variant span {
    margin-right: 15px;
}

.product-price {
    font-weight: 700;
    color: #f5222d;
    font-size: 18px;
    transition: all 0.3s ease;
}

.product-item:hover .product-price {
    transform: scale(1.05);
}

/* Order totals */
.order-totals {
    background: linear-gradient(135deg, #f9fafc, #f0f5ff);
    padding: 25px;
    border-radius: 12px;
    margin-bottom: 35px;
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.03);
    transition: all 0.3s ease;
}

.order-totals:hover {
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.07);
    transform: translateY(-5px);
}

.total-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px dashed #e8eef3;
}

.total-row:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
}

.grand-total {
    font-weight: 700;
    color: #2b3940;
    font-size: 20px;
}

.grand-total span:last-child {
    color: #f5222d;
    font-size: 22px;
}

/* Shipping info */
.shipping-info {
    margin-bottom: 35px;
    background-color: #fff;
    padding: 25px;
    border-radius: 12px;
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.03);
    transition: all 0.3s ease;
}

.shipping-info:hover {
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.07);
}

.shipping-info h3 {
    font-size: 22px;
    font-weight: 700;
    color: #2b3940;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 2px solid #f0f2f5;
    position: relative;
}

.shipping-info h3::after {
    content: '';
    position: absolute;
    width: 80px;
    height: 2px;
    background: linear-gradient(90deg, #3f6ad8, #25b0ed);
    bottom: -2px;
    left: 0;
    border-radius: 2px;
}

.info-row {
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px solid #f5f7fa;
}

.info-row:last-child {
    margin-bottom: 0;
    padding-bottom: 0;
    border-bottom: none;
}

.info-label {
    font-weight: 600;
    color: #6c757d;
    min-width: 150px;
    display: inline-block;
}

.info-value {
    color: #2b3940;
    font-weight: 500;
}

/* Actions */
.order-actions {
    display: flex;
    gap: 15px;
    margin-top: 30px;
    flex-wrap: wrap;
}

.order-actions .ant-btn {
    padding: 10px 25px;
    height: auto;
    border-radius: 50px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    transition: all 0.3s ease !important;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.order-actions .ant-btn:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.order-actions .ant-btn-primary {
    background: linear-gradient(135deg, #3f6ad8, #25b0ed);
    border: none;
}

.order-actions .ant-btn-primary:hover {
    background: linear-gradient(135deg, #25b0ed, #3f6ad8);
}

.order-actions .ant-btn-danger {
    background: linear-gradient(135deg, #ff4d4f, #f5222d);
    border: none;
    color: white;
}

.order-actions .ant-btn-default {
    border: 2px solid #e8eef3;
}

/* FAQs section */
.tracking-faqs {
    margin-top: 70px;
    margin-bottom: 40px;
    animation: fadeIn 0.8s ease-in-out;
}

.tracking-faqs h2 {
    font-size: 28px;
    font-weight: 700;
    color: #2b3940;
    margin-bottom: 30px;
    text-align: center;
    position: relative;
    padding-bottom: 15px;
}

.tracking-faqs h2::after {
    content: '';
    position: absolute;
    width: 80px;
    height: 4px;
    background: linear-gradient(90deg, #3f6ad8, #25b0ed);
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    border-radius: 4px;
}

.tracking-faqs .ant-collapse {
    background-color: transparent;
    border: none;
    border-radius: 12px;
    overflow: hidden;
}

.tracking-faqs .ant-collapse-item {
    margin-bottom: 15px;
    border: none;
    background-color: #fff;
    border-radius: 12px !important;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
}

.tracking-faqs .ant-collapse-item:hover {
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
}

.tracking-faqs .ant-collapse-header {
    font-weight: 600 !important;
    color: #2b3940 !important;
    font-size: 16px !important;
    padding: 18px 24px !important;
    background-color: #f9fafc;
    border-radius: 12px 12px 0 0 !important;
}

.tracking-faqs .ant-collapse-content {
    border-top: 1px solid #f0f2f5;
}

.tracking-faqs .ant-collapse-content-box {
    padding: 20px 24px !important;
    color: #6c757d;
    font-size: 15px;
    line-height: 1.6;
}

/* Animations */
@keyframes fadeIn {
    from {
        opacity: 0;
    }

    to {
        opacity: 1;
    }
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes slideDown {
    from {
        opacity: 0;
        transform: translateY(-20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes pulse {
    0% {
        box-shadow: 0 0 0 0 rgba(63, 106, 216, 0.4);
    }

    70% {
        box-shadow: 0 0 0 15px rgba(63, 106, 216, 0);
    }

    100% {
        box-shadow: 0 0 0 0 rgba(63, 106, 216, 0);
    }
}

@keyframes ripple {
    from {
        transform: scale(0);
        opacity: 1;
    }

    to {
        transform: scale(1.5);
        opacity: 0;
    }
}

/* Responsive styling */
@media (max-width: 992px) {
    .timeline-container {
        flex-direction: column;
        align-items: flex-start;
        padding-left: 30px;
    }

    .timeline-container::before {
        top: 0;
        bottom: 0;
        left: 30px;
        right: auto;
        width: 4px;
        height: auto;
        background: linear-gradient(180deg, #f0f0f0, #e0e0e0);
    }

    .timeline-step {
        flex-direction: row;
        margin-bottom: 30px;
        width: 100%;
    }

    .timeline-step:last-child {
        margin-bottom: 0;
    }

    .step-icon {
        margin-right: 20px;
        margin-bottom: 0;
    }

    .step-content {
        text-align: left;
        max-width: none;
    }

    .timeline-step:hover .step-content {
        transform: translateY(0);
    }
}

@media (max-width: 768px) {
    .tracking-header h1 {
        font-size: 30px;
    }

    .order-header {
        flex-direction: column;
        align-items: flex-start;
    }

    .order-actions {
        flex-wrap: wrap;
        gap: 10px;
    }

    .order-actions .ant-btn {
        flex: 1;
        min-width: 45%;
    }
}

@media (max-width: 576px) {
    .product-item {
        flex-wrap: wrap;
    }

    .product-price {
        width: 100%;
        margin-top: 15px;
        text-align: right;
    }

    .product-image {
        width: 70px;
        height: 70px;
    }

    .tracking-faqs h2 {
        font-size: 24px;
    }

    .order-actions .ant-btn {
        width: 100%;
        min-width: 100%;
    }
}
</style>
