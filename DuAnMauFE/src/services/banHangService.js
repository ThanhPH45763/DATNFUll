import axiosInstance from "@/config/axiosConfig";

const banHang = "banhang/";

const getAllHoaDonCTT = async () => {
    try {
        const { data } = await axiosInstance.get(banHang + `getAllHoaDonCTT`);
        return data;
    } catch (error) {
        console.error('Lỗi API lấy danh sách hóa đơn chưa thanh toán:', error);
        return { error: true };
    }
}

const createHoaDon = async () => {
    try {
        const { data } = await axiosInstance.get(banHang + `createHoaDon`);
        return data;
    } catch (error) {
        console.error('Lỗi API tạo hoá đơn:', error);
        return { error: true };
    }
}

const updateHoaDon = async (payload) => {
    try {
        // Gửi dữ liệu dưới dạng JSON body để khớp với @RequestBody của backend
        const { data } = await axiosInstance.put(banHang + `updateHoaDon`, payload);
        return data;
    } catch (error) {
        console.error('Lỗi API update hoá đơn:', error);
        return { error: true, message: error.response?.data?.message || 'Có lỗi xảy ra' };
    }
}

const deleteHoaDon = async (idHoaDon) => {
    try {
        const { data } = await axiosInstance.delete(`banhang/deleteHoaDon?idHoaDon=${idHoaDon}`);
        console.log('Response từ API:', data); // Kiểm tra response
        return data;
    } catch (error) {
        console.error('Lỗi API xóa hoá đơn:', error.response?.data || error.message);
        throw error;
    }
};

const getAllSPHD = async (idHoaDon) => {
    try {
        const { data } = await axiosInstance.get(banHang + `getSPHD?idHoaDon=` + idHoaDon);
        return data;
    } catch (error) {
        console.error('Lỗi API lấy danh sách sản phẩm hóa đơn:', error);
        return { error: true };
    }
}

// ✅ API mới: Get realtime stock của CTSP
const getCTSPRealtime = async (idCTSP) => {
    try {
        const { data } = await axiosInstance.get(banHang + `getCTSPRealtime/${idCTSP}`);
        return data;
    } catch (error) {
        console.error('Lỗi API lấy realtime CTSP:', error);
        return { error: true };
    }
}

const addSPHD = async (idHoaDon, idCTSP, soLuong) => {
    try {
        const { data } = await axiosInstance.post(banHang + `addSPHD?idHoaDon=${idHoaDon}&idCTSP=${idCTSP}&soLuong=${soLuong}`);
        return data;
    } catch (error) {
        console.error('Lỗi API thêm sp hoá đơn:', error);
        return { error: true };
    }
}
const themSPHDMoi = async (idHoaDon, idCTSP, soLuong) => {
    try {
        const { data } = await axiosInstance.post(
            banHang + `themSPHDMoi?idHoaDon=${idHoaDon}&idCTSP=${idCTSP}&soLuong=${soLuong}`
        );
        return data;
    } catch (error) {
        console.error('Lỗi API thêm sp mới hoá đơn:', error.response?.data || error.message);
        return { error: true, message: error.response?.data || 'Có lỗi xảy ra' };
    }
};

const giamSPHD = async (idHoaDon, idCTSP, soLuong) => {
    try {
        const { data } = await axiosInstance.post(banHang + `giamSPHD?idHoaDon=${idHoaDon}&idCTSP=${idCTSP}&soLuong=${soLuong}`);
        return data;
    } catch (error) {
        console.error('Lỗi API giảm sp hoá đơn:', error);
        return { error: true };
    }
}

const setSPHD = async (idHoaDon, idCTSP, soLuong) => {
    try {
        const { data } = await axiosInstance.post(banHang + `setSPHD?idHoaDon=${idHoaDon}&idCTSP=${idCTSP}&soLuongMoi=${soLuong}`);
        return data;
    } catch (error) {
        console.error('Lỗi API set sp hoá đơn:', error);
        return { error: true };
    }
}

const xoaSPHD = async (idHoaDon, idCTSP) => {
    try {
        const response = await axiosInstance.delete(banHang + 'xoaSPHD', {
            params: {
                idHoaDon,
                idChiTietSanPham: idCTSP
            }
        });
        return response.data;
    } catch (error) {
        if (error.response) {
            console.error('Lỗi từ server:', error.response.status, error.response.data);
            return { success: false, message: error.response.data.message || 'Có lỗi xảy ra' };
        } else if (error.request) {
            console.error('Không nhận được phản hồi:', error.message);
            return { success: false, message: 'Lỗi mạng hoặc server không phản hồi' };
        } else {
            console.error('Lỗi khác:', error.message);
            return { success: false, message: 'Có lỗi xảy ra' };
        }
    }
};


const trangThaiDonHang = async (idHoaDon) => {
    try {
        const { data } = await axiosInstance.get(banHang + `trangThaiDonHang?idHoaDon=` + idHoaDon);
        return data;
    } catch (error) {
        console.error('Lỗi API trang thai don hang:', error);
        return { error: true };
    }
}

const phuongThucNhanHang = async (idHoaDon, phuongThucNhanHang) => {
    try {
        const { data } = await axiosInstance.post(banHang + `phuongThucNhanHang?idHoaDon=${idHoaDon}&phuongThucNhanHang=${phuongThucNhanHang}`);
        return data;
    } catch (error) {
        console.error('Lỗi API phuong thuc nhan hang:', error);
        return { error: true };
    }
}

const getHoaDonByIdHoaDon = async (idHD) => {
    try {
        const { data } = await axiosInstance.get(banHang + `getHoaDonByIdHoaDon?idHD=${idHD}`);
        return data;
    } catch (error) {
        console.error('Lỗi API lấy hoá đơn theo id:', error);
        return { error: true };
    }
}

const addKhHD = async (idHoaDon, idKhachHang, diaChi, tenKhachHang, sdt, email) => {
    try {
        const { data } = await axiosInstance.post(banHang + `addKhHD?idHD=${idHoaDon}&idKH=${idKhachHang}&diaChi=${diaChi}&tenKhachHang=${tenKhachHang}&soDienThoai=${sdt}&email=${email}`);
        return data;
    } catch (error) {
        console.error('Lỗi API thêm khách hàng hoá đơn:', error);
        return { error: true };
    }
}

const removeCustomerFromInvoice = async (idHoaDon) => {
    try {
        const { data } = await axiosInstance.post(banHang + `removeCustomerFromInvoice?idHD=${idHoaDon}`);
        return data;
    } catch (error) {
        console.error('Lỗi API bỏ chọn khách hàng:', error);
        return { error: true };
    }
}

const setTrangThaiNhanHang = async (idHoaDon, phuongThucNhanHang, phiVanChuyen) => {
    try {
        const { data } = await axiosInstance.post(banHang + `setTrangThaiNhanHang?idHD=${idHoaDon}&phuongThucNhanHang=${phuongThucNhanHang}&phiVanChuyen=${phiVanChuyen}`);
        return data;
    } catch (error) {
        console.error('Lỗi API set trang thai nhan hang:', error);
        return { error: true };
    }
}

const thanhToanMomo = async (idHoaDon) => {
    try {
        const { data } = await axiosInstance.post(`api/momo/thanhToanMomo?idHoaDon=${idHoaDon}`);
        return data;
    } catch (error) {
        console.error('Lỗi API thanh toán momo:', error);
        return { error: true };
    }
}

const tinhPhiShip = async (pickProvince, pickDistrict, province, district, weight, tongTienHoaDon) => {
    try {
        // Properly encode Vietnamese parameters using URLSearchParams
        const params = new URLSearchParams({
            pickProvince,
            pickDistrict,
            province,
            district,
            weight: weight.toString(),
            value: tongTienHoaDon.toString()
        });

        const { data } = await axiosInstance.get(`api/ghtk/fee?${params.toString()}`);

        // Backend trả về string JSON từ GHTK
        // Response format: {"success":true,"message":"...","fee":{"name":"...","fee":35000,"ship_fee_only":35000,...}}
        const ghtkResponse = typeof data === 'string' ? JSON.parse(data) : data;

        if (ghtkResponse.success && ghtkResponse.fee && ghtkResponse.fee.ship_fee_only) {
            return ghtkResponse.fee.ship_fee_only; // Trả về số tiền
        }

        console.error('GHTK response không hợp lệ:', ghtkResponse);
        return { error: true, message: ghtkResponse.message || 'Không tính được phí' };
    } catch (error) {
        console.error('Lỗi API tính phí ship:', error);
        return { error: true };
    }
}

const getSuitableVouchers = async (tongTien) => {
    try {
        const { data } = await axiosInstance.get(banHang + `get-suitable-vouchers?tongTien=${tongTien}`);
        return data;
    } catch (error) {
        console.error('Lỗi API lấy danh sách voucher phù hợp:', error);
        return [];
    }
}

const applyVoucher = async (idHoaDon, idVoucher) => {
    try {
        const params = new URLSearchParams();
        params.append('idHoaDon', idHoaDon);
        if (idVoucher !== null && idVoucher !== undefined) {
            params.append('idVoucher', idVoucher);
        }
        const { data } = await axiosInstance.post(banHang + `apply-voucher?${params.toString()}`);
        return data;
    } catch (error) {
        console.error('Lỗi API áp dụng voucher:', error);
        return { error: true, message: error.response?.data || 'Có lỗi xảy ra khi áp dụng voucher' };
    }
}

/**
 * Kiểm tra stock realtime cho tất cả items trong giỏ hàng
 * @param {number} idHoaDon - ID hóa đơn cần kiểm tra
 * @returns {Object} { has_invalid_items, items, invalid_item_names }
 */
const checkCartStock = async (idHoaDon) => {
    try {
        const { data } = await axiosInstance.get(banHang + `checkCartStock/${idHoaDon}`);
        return data;
    } catch (error) {
        console.error('Lỗi API kiểm tra stock:', error);
        return { has_invalid_items: false, items: [], invalid_item_names: [] };
    }
}

export const banHangService = {
    getAllHoaDonCTT,
    createHoaDon,
    updateHoaDon,
    deleteHoaDon,
    getAllSPHD,
    getCTSPRealtime,
    addSPHD,
    giamSPHD,
    themSPHDMoi,
    trangThaiDonHang,
    phuongThucNhanHang,
    xoaSPHD,
    getHoaDonByIdHoaDon,
    addKhHD,
    removeCustomerFromInvoice,
    setTrangThaiNhanHang,
    thanhToanMomo,
    setSPHD,
    tinhPhiShip,
    getSuitableVouchers,
    applyVoucher,
    checkCartStock
}