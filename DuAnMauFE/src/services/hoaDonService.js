import axiosInstance from "@/config/axiosConfig";

const qlhd = 'admin/qlhd/'; // Base path cho hóa đơn

// Lấy danh sách hóa đơn với phân trang
const getAllHoaDon = async (page = 0, size = 5) => {
    try {
        const { data } = await axiosInstance.get(qlhd + `danh_sach_hoa_don?page=${page}&size=${size}`);
        return data;
    } catch (error) {
        console.error('Lỗi API lấy danh sách hóa đơn:', error);
        return { error: true };
    }
};

const getListHoaDon = async () => {
    try {
        const { data } = await axiosInstance.get(qlhd + `all-hoa-don`);
        return data;
    } catch (error) {
        console.error('Lỗi API lấy list hóa đơn:', error);
        return { error: true };
    }
};

// Lọc hóa đơn theo trạng thái
const filterByTrangThai = async (trangThai, page = 0, size = 5) => {
    try {
        const { data } = await axiosInstance.get(qlhd + `loc_trang_thai_don_hang?trangThai=${trangThai}&page=${page}&size=${size}`);
        return data;
    } catch (error) {
        console.error('Lỗi API lọc hóa đơn theo trạng thái:', error);
        return { error: true };
    }
};

// Lọc hóa đơn theo ngày
const filterByDate = async (tuNgay, denNgay, page = 0, size = 5) => {
    try {
        const params = new URLSearchParams();
        if (tuNgay) params.append('tuNgay', tuNgay);
        if (denNgay) params.append('denNgay', denNgay);
        params.append('page', page);
        params.append('size', size);
        const { data } = await axiosInstance.get(qlhd + `loc_ngay?${params.toString()}`);
        return data;
    } catch (error) {
        console.error('Lỗi API lọc hóa đơn theo ngày:', error);
        return { error: true };
    }
};

// Tìm kiếm hóa đơn
const searchHoaDon = async (keyword, page = 0, size = 5) => {
    try {
        const { data } = await axiosInstance.get(qlhd + `tim_kiem?keyword=${keyword}&page=${page}&size=${size}`);
        return data;
    } catch (error) {
        console.error('Lỗi API tìm kiếm hóa đơn:', error);
        return { error: true };
    }
};
const locHD = async (keyword, tuNgay, denNgay, trangThai, loaiHoaDon, page = 0, size = 5) => {
    try {
        const { data } = await axiosInstance.get(qlhd + `loc_hoa_don?keyword=${keyword}&tuNgay=${tuNgay}&denNgay=${denNgay}&trangThai=${trangThai}&loaiHoaDon=${loaiHoaDon}&page=${page}&size=${size}`);
        return data;
    } catch (error) {
        console.error('Lỗi API lọc hóa đơn:', error);
        return { error: true, message: error.response?.data?.message || 'Không thể lấy dữ liệu hóa đơn.' };
    }
};
// Lấy chi tiết hóa đơn theo mã hóa đơn
const getCTHD = async (maHoaDon) => {
    try {
        const { data } = await axiosInstance.get(qlhd + `hdct?id=${maHoaDon}`);
        return data;
    } catch (error) {
        console.error('Lỗi API lấy chi tiết hóa đơn:', error);
        return { error: true };
    }
};
const getCTTH = async (maHoaDon) => {
    try {
        const { data } = await axiosInstance.get(qlhd + `ctth?id=${maHoaDon}`);
        return data;
    } catch (error) {
        console.error('Lỗi API lấy chi tiết hóa đơn:', error);
        return { error: true };
    }
};
// Thay đổi trạng thái hóa đơn
const changeTrangThai = async (maHoaDon, newTrangThai, nhanVienDoi, noiDungDoi) => {
    try {
        const { data } = await axiosInstance.post(qlhd + 'chuyen_trang_thai', null, {
            params: { maHoaDon, newTrangThai, nhanVienDoi, noiDungDoi }
        });
        return data;
    } catch (error) {
        console.error('Lỗi API thay đổi trạng thái hóa đơn:', error);
        return { error: true };
    }
};
// Quay lại trạng thái ban đầu
const quayLaiTrangThai = async (maHoaDon, nhanVienDoi, noiDungDoi) => {
    try {
        const { data } = await axiosInstance.post(qlhd + 'quay_lai_trang_thai', null, {
            params: { maHoaDon, nhanVienDoi, noiDungDoi }
        });
        return data;
    } catch (error) {
        console.error('Lỗi API quay lại trạng thái ban đầu:', error);
        return { error: true };
    }
};
// Hủy hóa đơn
const cancelHoaDon = async (maHoaDon, nhanVienDoi, noiDungDoi) => {
    try {
        const { data } = await axiosInstance.post(qlhd + 'cancel_order', null, {
            params: { maHoaDon, nhanVienDoi, noiDungDoi }
        });
        return data;
    } catch (error) {
        console.error('Lỗi API hủy hóa đơn:', error);
        return { error: true };
    }
};
// Cập nhật phí ván chuyen trong hóa đơn
const updatePhiShip = async (maHoaDon, phiVanChuyen) => {
    try {
        const { data } = await axiosInstance.post(qlhd + `update_phiShip?maHoaDon=${maHoaDon}&phiVanChuyen=${phiVanChuyen}`);
        return data;
    } catch (error) {
        console.error('Lỗi API cập nhật phí vận chuyển:', error);
        return { error: true };
    }
};
// Cập nhật thông tin khách hàng trong hóa đơn
const updateTTKH_in_HD = async (maHoaDon, ttkh, phiVanChuyen) => {
    try {
        const { data } = await axiosInstance.post(qlhd + 'update_ttkh', {
            maHoaDon,
            hoTen: ttkh.hoTen,
            email: ttkh.email,
            sdtNguoiNhan: ttkh.sdtNguoiNhan,
            diaChi: ttkh.diaChi,
            phiVanChuyen,
            nhanVienDoi: ttkh.nhanVienDoi // Thêm nhanVienDoi
        });
        return data;
    } catch (error) {
        console.error('Lỗi API cập nhật thông tin khách hàng:', error);
        return { error: true };
    }
};
// Cập nhật ghi chú
const updateNote = async (maHoaDon, ghiChu, nhanVienDoi) => {
    try {
        const { data } = await axiosInstance.post(qlhd + 'update_note', {
            maHoaDon,
            ghiChu,
            nhanVienDoi // Thêm nhanVienDoi
        });
        return data;
    } catch (error) {
        console.error('Lỗi API cập nhật ghi chú:', error);
        return { error: true };
    }
};
// Lấy danh sách chi tiết sản phẩm
const getAllCTSP_HD = async (page = 0, size = 5, keyword = '') => {
    try {
        const params = new URLSearchParams();
        params.append('page', page);
        params.append('size', size);
        if (keyword) params.append('keyword', keyword);
        const { data } = await axiosInstance.get(qlhd + `ctsp_hd?${params.toString()}`);
        return data;
    } catch (error) {
        console.error('Lỗi API lấy danh sách chi tiết sản phẩm:', error);
        return { error: true };
    }
};
// Thêm sản phẩm vào hóa đơn
const addProductsToInvoice = async (maHoaDon, products, nhanVienDoi) => {
    try {
        const { data } = await axiosInstance.post(qlhd + 'addSP_HD', {
            maHoaDon,
            products,
            nhanVienDoi // Thêm nhanVienDoi
        });
        return data;
    } catch (error) {
        console.error('Lỗi API thêm sản phẩm vào hóa đơn:', error);
        return { error: true };
    }
};
const removeProductFromInvoice = async (maHoaDon, idCTSP, nhanVienDoi, noiDungDoi) => {
    try {
        const { data } = await axiosInstance.post(qlhd + 'removeSP_HD', null, {
            params: { maHoaDon, idCTSP, nhanVienDoi, noiDungDoi }
        });
        return data;
    } catch (error) {
        console.error('Lỗi API xóa sản phẩm khỏi hóa đơn:', error);
        return { error: true };
    }
};
const updateProductQuantity = async (maHoaDon, idCTSP, quantityChange, nhanVienDoi, noiDungDoi) => {
    try {
        const { data } = await axiosInstance.post(qlhd + 'update_soLuong', null, {
            params: { maHoaDon, idCTSP, quantityChange, nhanVienDoi, noiDungDoi }
        });
        return data;
    } catch (error) {
        console.error('Lỗi API cập nhật số lượng sản phẩm:', error);
        return { error: true };
    }
};


const updateHinhThucTTHoaDon = async (idHD, hinhThucThanhToan) => {
    try {
        const { data } = await axiosInstance.put(qlhd + `updateHTTTHD?idHD=${idHD}&hinhThucThanhToan=${hinhThucThanhToan}`);
        return data;
    } catch (error) {
        console.error('Lỗi API cập nhật hình thức thanh toán:', error);
        return { error: true };
    }
};
// === THÊM HÀM MỚI (KHÔNG SỬA HÀM CŨ) ===
// Lấy chi tiết hóa đơn và thông tin trả hàng
const getHoaDonDetails = async (maHoaDon) => {
    try {
      const { data } = await axiosInstance.get(qlhd + `${maHoaDon}/chi-tiet-tra-hang`);
  
      // Nếu backend trả về `thanh_cong: false` thì ném lỗi để xử lý nhất quán
      if (!data.thanh_cong) {
        throw new Error(data.thong_bao || 'Không tìm thấy đơn hàng');
      }
  
      return data;
    } catch (error) {
      console.error('Lỗi API lấy chi tiết hóa đơn và trả hàng:', error);
  
      // Ưu tiên lấy message từ backend
      const thongBao = error.response?.data?.thong_bao || error.message || 'Lỗi không xác định';
      throw new Error(thongBao);  // để đồng nhất với catch phía trên
    }
  };
  

// Xử lý trả hàng
const processReturn = async (returnData) => {
    try {
        const payload = {
            ma_hoa_don: returnData.ma_hoa_don,
            danh_sach_san_pham: returnData.products.map(p => ({
                id_chi_tiet_san_pham: p.id_chi_tiet_san_pham,
                so_luong: p.so_luong,
                so_tien_hoan: p.refund_amount
            })),
            ly_do: returnData.reason,
            ghi_chu: returnData.note,
            nhan_vien: returnData.staff,
            tong_tien_hoan: returnData.total_refunded
        };
        const { data } = await axiosInstance.post(qlhd + 'tra-hang', payload);
        return data;
    } catch (error) {
        console.error('Lỗi API xử lý trả hàng:', error);
        return { error: true, message: error.response?.data?.thong_bao || 'Lỗi khi xử lý trả hàng' };
    }
};

export const hoaDonService = {
    getAllHoaDon,
    getListHoaDon,
    filterByTrangThai,
    locHD,
    filterByDate,
    searchHoaDon,
    getCTHD,
    changeTrangThai,
    cancelHoaDon,
    updatePhiShip,
    updateTTKH_in_HD,
    updateNote,
    getAllCTSP_HD,
    addProductsToInvoice,
    removeProductFromInvoice,
    updateProductQuantity,
    quayLaiTrangThai,
    updateHinhThucTTHoaDon,
    getHoaDonDetails, // Thêm hàm mới
    processReturn ,// Thêm hàm mới
    getCTTH
};
