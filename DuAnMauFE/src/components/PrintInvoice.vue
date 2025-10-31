<template>
    <div class="invoice-print-container" ref="printContainer">
        <!-- Nội dung chỉ hiển thị khi in -->
        <div class="print-only">
            <div class="invoice-header">
                <div class="company-info">
                    <img class="company-logo" :src="logoSrc" alt="G-B Sports Logo" />
                    <div class="company-details">
                        <h2 class="company-name">G-B SPORTS</h2>
                        <p>Địa chỉ: 123 Đường Thể Thao, Quận Cầu Giấy, Hà Nội</p>
                        <p>Điện thoại: (024) 123 4567</p>
                        <p>Email: info@gbsports.com</p>
                        <p>Website: www.gbsports.com</p>
                    </div>
                </div>
                <div class="invoice-qr-container">
                    <qrcode-vue :value="hoaDon.ma_hoa_don || 'GB001'" :size="130" level="H"
                        class="invoice-qr"></qrcode-vue>
                    <div class="invoice-code">
                        <p>Mã hóa đơn:</p>
                        <p class="invoice-id">{{ hoaDon.ma_hoa_don }}</p>
                    </div>
                </div>
            </div>

            <div class="invoice-title">
                <h1>HÓA ĐƠN BÁN HÀNG</h1>
                <p class="invoice-date">Ngày: {{ dinhDangNgayGio(hoaDon.ngay_tao) }}</p>
            </div>

            <div class="customer-info">
                <h3>THÔNG TIN KHÁCH HÀNG</h3>
                <table class="info-table">
                    <tr>
                        <td class="info-label">Họ tên:</td>
                        <td class="info-value">{{ hoaDon.ho_ten }}</td>
                    </tr>
                    <tr>
                        <td class="info-label">Điện thoại:</td>
                        <td class="info-value">{{ hoaDon.sdt_nguoi_nhan }}</td>
                    </tr>
                    <tr>
                        <td class="info-label">Địa chỉ:</td>
                        <td class="info-value">{{ hoaDon.dia_chi }}</td>
                    </tr>
                    <tr v-if="hoaDon.email">
                        <td class="info-label">Email:</td>
                        <td class="info-value">{{ hoaDon.email }}</td>
                    </tr>
                </table>
            </div>

            <div class="invoice-items">
                <h3>CHI TIẾT ĐƠN HÀNG</h3>
                <table class="items-table">
                    <thead>
                        <tr>
                            <th class="item-stt">STT</th>
                            <th class="item-name">Tên sản phẩm</th>
                            <th class="item-variant">Phân loại</th>
                            <th class="item-qty">SL</th>
                            <th class="item-price">Đơn giá</th>
                            <th class="item-total">Thành tiền</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, index) in hoaDonChiTiet" :key="index">
                            <td class="item-stt">{{ index + 1 }}</td>
                            <td class="item-name">{{ item.ten_san_pham }}</td>
                            <td class="item-variant">
                                <span v-if="item.ten_mau_sac">Màu: {{ item.ten_mau_sac }}</span>
                                <span v-if="item.gia_tri">{{ item.gia_tri + " " + item.don_vi }}</span>
                            </td>
                            <td class="item-qty">{{ item.so_luong }}</td>
                            <td class="item-price">{{ dinhDangTien(item.don_gia / item.so_luong) }}</td>
                            <td class="item-total">{{ dinhDangTien(item.don_gia) }}</td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="invoice-summary">
                <div class="summary-table">
                    <div class="summary-row">
                        <div class="summary-label">Tổng tiền hàng:</div>
                        <div class="summary-value">{{ dinhDangTien(hoaDon.tong_tien_truoc_giam || 0) }}</div>
                    </div>
                    <div class="summary-row" v-if="hoaDon.ma_voucher">
                        <div class="summary-label">Giảm giá ({{ hoaDon.ma_voucher }}):</div>
                        <div class="summary-value">{{ dinhDangTien((hoaDon.tong_tien_truoc_giam || 0) -
                            (hoaDon.tong_tien_sau_giam || 0) + (hoaDon.phi_van_chuyen || 0)) }}</div>
                    </div>
                    <div class="summary-row">
                        <div class="summary-label">Phí vận chuyển:</div>
                        <div class="summary-value">{{ dinhDangTien(hoaDon.phi_van_chuyen || 0) }}</div>
                    </div>
                    <div class="summary-row total">
                        <div class="summary-label">TỔNG THANH TOÁN:</div>
                        <div class="summary-value">{{ dinhDangTien(hoaDon.tong_tien_sau_giam || 0) }}</div>
                    </div>
                </div>
            </div>

            <div class="payment-info">
                <div class="payment-method">
                    <h3>PHƯƠNG THỨC NHẬN HÀNG</h3>
                    <p>{{ hoaDon.phuong_thuc_nhan_hang || 'Thanh toán khi nhận hàng (COD)' }}</p>
                </div>
                <div class="payment-status">
                    <h3>HÌNH THỨC THANH TOÁN</h3>
                    <p>{{ hoaDon.hinh_thuc_thanh_toan || 'Đã thanh toán' }}</p>
                </div>
            </div>

            <div class="invoice-footer">
                <div class="signature-section">
                    <div class="seller-signature">
                        <p>Người bán hàng</p>
                        <p class="signature-name">(Ký, ghi rõ họ tên)</p>
                    </div>
                    <div class="buyer-signature">
                        <p>Người mua hàng</p>
                        <p class="signature-name">(Ký, ghi rõ họ tên)</p>
                    </div>
                </div>
                <div class="invoice-notes">
                    <p><strong>Lưu ý:</strong></p>
                    <p>- Hóa đơn này có giá trị xuất hóa đơn điện tử trong vòng 7 ngày kể từ ngày mua hàng</p>
                    <p>- Quý khách vui lòng giữ lại hóa đơn để đối chiếu khi cần thiết</p>
                    <p>- Mọi thắc mắc vui lòng liên hệ hotline: 1900 1234</p>
                </div>
                <div class="thank-you">
                    <p>Cảm ơn quý khách đã mua hàng tại G-B Sports!</p>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { defineProps, ref, defineExpose, computed } from 'vue';
import QrcodeVue from 'qrcode.vue';
import { useRouter } from 'vue-router';
import html2pdf from 'html2pdf.js';
import logoGB from '@/images/logo/logoGB.png';

const props = defineProps({
    hoaDon: {
        type: Object,
        required: true
    },
    hoaDonChiTiet: {
        type: Array,
        required: true
    }
});

const printContainer = ref(null);
const router = useRouter();
const logoSrc = computed(() => logoGB);

// Định dạng ngày giờ
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

// Hàm in hóa đơn
const inHoaDon = () => {
    const options = {
        margin: [10, 10, 10, 10],
        filename: `Hoa_Don_${props.hoaDon.ma_hoa_don || 'GB_invoice'}.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2, logging: true, useCORS: true },
        jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
    };

    html2pdf().set(options).from(printContainer.value).save();
};

// Hàm tạo hóa đơn dưới dạng PDF
const taoHoaDonPDF = () => {
    const options = {
        margin: [10, 10, 10, 10],
        filename: `Hoa_Don_${props.hoaDon.ma_hoa_don || 'GB_invoice'}.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2, logging: true, useCORS: true },
        jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
    };

    return html2pdf().set(options).from(printContainer.value).outputPdf('blob');
};

// Export các phương thức
defineExpose({
    inHoaDon,
    taoHoaDonPDF
});
</script>

<style scoped>
/* Styles cho việc in */
@media print {
    body * {
        visibility: hidden;
    }

    .print-only,
    .print-only * {
        visibility: visible;
    }

    .print-only {
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
    }
}

.invoice-print-container {
    font-family: 'Roboto', Arial, sans-serif;
    max-width: 210mm;
    padding: 20mm;
    margin: 0 auto;
    background-color: white;
    color: #333;
}

.invoice-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
    border-bottom: 1px solid #eee;
    padding-bottom: 20px;
}

.company-info {
    display: flex;
    align-items: center;
}

.company-logo {
    max-width: 120px;
    max-height: 60px;
    margin-right: 20px;
}

.company-details {
    font-size: 12px;
}

.company-name {
    font-size: 22px;
    font-weight: bold;
    margin: 0 0 8px 0;
    color: #2b3940;
}

.invoice-qr-container {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.invoice-qr {
    margin-bottom: 10px;
}

.invoice-code {
    text-align: center;
    font-size: 12px;
}

.invoice-id {
    font-weight: bold;
    font-size: 14px;
}

.invoice-title {
    text-align: center;
    margin: 20px 0;
}

.invoice-title h1 {
    font-size: 24px;
    font-weight: bold;
    margin: 0;
    color: #2b3940;
}

.invoice-date {
    font-size: 14px;
    margin: 8px 0 0 0;
}

.customer-info {
    margin-bottom: 30px;
}

h3 {
    font-size: 14px;
    font-weight: bold;
    margin: 0 0 15px 0;
    padding-bottom: 8px;
    border-bottom: 1px solid #eee;
    color: #2b3940;
}

.info-table {
    width: 100%;
    border-collapse: collapse;
}

.info-table td {
    padding: 5px 0;
    font-size: 13px;
}

.info-label {
    font-weight: bold;
    width: 100px;
    vertical-align: top;
}

.info-value {
    color: #555;
}

.invoice-items {
    margin-bottom: 30px;
}

.items-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 13px;
}

.items-table th,
.items-table td {
    border: 1px solid #ddd;
    padding: 10px;
    text-align: center;
}

.items-table th {
    background-color: #f5f5f5;
    font-weight: bold;
}

.item-stt {
    width: 40px;
}

.item-name {
    text-align: left;
}

.item-variant {
    font-size: 12px;
    text-align: left;
}

.item-qty {
    width: 50px;
}

.item-price,
.item-total {
    width: 120px;
    text-align: right;
}

.invoice-summary {
    margin-bottom: 30px;
}

.summary-table {
    margin-left: auto;
    width: 350px;
    font-size: 13px;
}

.summary-row {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    border-bottom: 1px solid #eee;
}

.summary-label {
    font-weight: bold;
}

.summary-value {
    text-align: right;
    min-width: 120px;
}

.total {
    font-size: 16px;
    font-weight: bold;
    color: #2b3940;
    border-top: 2px solid #ddd;
    border-bottom: 2px solid #ddd;
    padding: 10px 0;
    margin-top: 10px;
}

.payment-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 40px;
    font-size: 13px;
}

.payment-method,
.payment-status {
    width: 48%;
}

.invoice-footer {
    margin-top: 50px;
    font-size: 12px;
}

.signature-section {
    display: flex;
    justify-content: space-between;
    margin-bottom: 40px;
    text-align: center;
}

.signature-name {
    margin-top: 60px;
    font-style: italic;
}

.invoice-notes {
    margin-bottom: 30px;
    line-height: 1.5;
}

.thank-you {
    text-align: center;
    font-weight: bold;
    margin-top: 40px;
    font-size: 14px;
    color: #2b3940;
}
</style>