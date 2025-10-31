import axiosInstance from "@/config/axiosConfig";
const banHangOnline = "banhangweb/";
const createOrder = async (hoaDon) => {
    const response = await axiosInstance.post(banHangOnline + 'taoHoaDonWeb', hoaDon);
    return response.data;
}
const createOrderChiTiet = async (hoaDonChiTiet) => {
    const response = await axiosInstance.post(banHangOnline + 'taoHoaDonChiTiet', hoaDonChiTiet);
    return response.data;
}
const getThongTinHoaDon = async (maHoaDon) => {
    const response = await axiosInstance.get(banHangOnline + 'thongTinHoaDon?maHoaDon=' + maHoaDon);
    return response.data;
}
const getThongTinTimeLine = async (maHoaDon) => {
    const response = await axiosInstance.get(banHangOnline + 'thongTinTimeLine?maHoaDon=' + maHoaDon);
    return response.data;
}
const getThongTinHoaDonChiTiet = async (maHoaDon) => {
    const response = await axiosInstance.get(banHangOnline + 'thongTinHoaDonChiTiet?maHoaDon=' + maHoaDon);
    return response.data;
}

// Cập nhật trạng thái hóa đơn
const updateOrderStatus = async (data) => {
    try {
        const response = await axiosInstance.put('/hoa-don/update-trang-thai', data);
        return response.data;
    } catch (error) {
        console.error('Error updating order status:', error);
        throw error;
    }
};

const createOrder1 = async (hoaDon) => {
    const response = await axiosInstance.post(banHangOnline + 'taoHoaDonWeb1', hoaDon);
    return response.data;
}
// Giỏ hàng của KH có tài khoản
const getGioHang = async (idKhachHang) => {
    const response = await axiosInstance.get('/gioHangWeb/gioHangByKH?idKhachHang=' + idKhachHang);
    return response.data;
}
// Lấy danh sách địa chỉ của khách hàng
const getDanhSachDiaChi = async (idKhachHang) => {
    const response = await axiosInstance.get('/gioHangWeb/danhSachDiaChi?idKhachHang=' + idKhachHang);
    return response.data;
}
const voucherByGiaTruyen = async (giaTruyen) => {
    try {
        const { data } = await axiosInstance.get(banHangOnline + `voucherTheoGiaTruyen?giaTruyen=${giaTruyen}`);
        console.log('Dữ liệu voucher từ API:', data);
        return data;
    } catch (error) {
        console.error('Lỗi API lấy voucher theo giá truyền:', error);
        return { error: true };
    }
}
// Thêm giỏ hàng cho khách hàng có tài khoản
const themGioHangByIdKH = async (idKH, idCTSP, soLuong) => {
    const response = await axiosInstance.post('/gioHangWeb/themGHByIdKH?idKH=' + idKH + '&idCTSP=' + idCTSP + '&soLuong=' + soLuong);
    return response.data;
}
// Xoá số lượng sản phẩm trong giỏ hàng
const xoaSoLuongSPGH = async (idKH, idCTSP, soLuong) => {
    const response = await axiosInstance.delete('/gioHangWeb/deleteGHByIdKH?idKH=' + idKH + '&idCTSP=' + idCTSP + '&soLuong=' + soLuong);
    return response.data;
}
// lấy số lượng còn lại của sản phẩm theo ID
const maxSoLuongSP = async (idCTSP) => {
    const response = await axiosInstance.get('/gioHangWeb/maxSoLuong?idCTSP=' + idCTSP);
    return response.data;
}
const getTrangThaiCTSP = async (idCTSP) => {
    try {
        console.log('[SERVICE] Bắt đầu lấy trạng thái CTSP:', idCTSP);
        const response = await axiosInstance.get('/banhangweb/trangThaiCTSP?idCTSP=' + idCTSP);
        console.log('[SERVICE] Kết quả API trạng thái CTSP:', response.data, typeof response.data);
        return response.data;
    } catch (error) {
        console.error('[SERVICE] Lỗi khi lấy trạng thái CTSP:', error);
        return false;
    }
}
//Tạo hóa đơn chi tiết mua ngay
const createOrderChiTietMuaNgay = async (hoaDonChiTiet) => {
    const response = await axiosInstance.post('/banhangweb/taoHoaDonChiTietMuaNgay', hoaDonChiTiet);
    console.log('[SERVICE] Kết quả tạo hóa đơn chi tiết mua ngay:', response.data);
    return response.data;
}
export const banHangOnlineService = {
    createOrder,
    createOrderChiTiet,
    getThongTinHoaDon,
    getThongTinTimeLine,
    getThongTinHoaDonChiTiet,
    updateOrderStatus,
    createOrder1,
    getGioHang,
    getDanhSachDiaChi,
    voucherByGiaTruyen,
    themGioHangByIdKH,
    xoaSoLuongSPGH,
    maxSoLuongSP,
    getTrangThaiCTSP,
    createOrderChiTietMuaNgay
}

