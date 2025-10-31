// src/services/voucherService.js
import axiosInstance from "@/config/axiosConfig";

const normalizeResponse = (data) => {
  if (!data || data.error) return { error: true, content: [], totalPages: 0, totalElements: 0 };
  return {
    content: data.content || [],
    totalPages: data.totalPages || 0,
    totalElements: data.totalElements || 0,
  };
};

// Lấy tất cả voucher
const getAllVouchers = async (page, size) => {
  try {
    const { data } = await axiosInstance.get(`/admin/quan_ly_voucher/hien-thi?page=${page}&size=${size}`);
    return normalizeResponse(data);
  } catch (error) {
    console.error('Lỗi API khi lấy danh sách voucher:', error);
    return { error: true };
  }
};
// Thêm voucher
const addVoucher = async (voucherData) => {
  try {
    const { data } = await axiosInstance.post('/admin/quan_ly_voucher/add', voucherData);
    return data; // Trả về chuỗi từ backend
  } catch (error) {
    console.error('Lỗi API khi thêm voucher:', error);
    throw error;
  }
};
const getVoucherLocKieuGiamGia = async (page, size, kieuGiamGia) => {
  try {
    console.log('Sending request to API:', `/admin/quan_ly_voucher/loc-kieu-giam-gia-VC?page=${page}&size=${size}&kieuGiamGia=${kieuGiamGia}`);
    const { data } = await axiosInstance.get(
      `/admin/quan_ly_voucher/loc-kieu-giam-gia-VC?page=${page}&size=${size}&kieuGiamGia=${kieuGiamGia}`
    );
    console.log('Raw API data:', data);
    return normalizeResponse(data);
  } catch (error) {
    console.error('Lỗi khi gọi API:', error.message, error.response?.data || error);
    return { error: true };
  }
};
const updateVoucher = async (voucherData) => {
  try {
    const { data } = await axiosInstance.put('/admin/quan_ly_voucher/update', voucherData);
    return data;
  } catch (error) {
    console.error('Lỗi API khi cập nhật voucher:', error);
    throw error;
  }
};

const getVoucherById = async (id) => {
  try {
    const { data } = await axiosInstance.get(`/admin/quan_ly_voucher/detail?id=${id}`);
    return data;
  } catch (error) {
    console.error('Lỗi API khi lấy chi tiết voucher:', error);
    throw error;
  }
};

// Lọc voucher theo trạng thái
const getVoucherLocTrangThai = async (page, size, trangThai) => {
  try {
    const { data } = await axiosInstance.get(
      `/admin/quan_ly_voucher/loc-trang-thai?page=${page}&size=${size}&trangThai=${trangThai || ''}`
    );
    return normalizeResponse(data);
  } catch (error) {
    console.error('Lỗi API khi lọc theo trạng thái:', error);
    return { error: true };
  }
};

// Tìm kiếm voucher
const searchVoucher = async (keyword, page, size) => {
  try {
    const { data } = await axiosInstance.get(
      `/admin/quan_ly_voucher/tim-kiem?keyword=${keyword || ''}&page=${page}&size=${size}`
    );
    return normalizeResponse(data);
  } catch (error) {
    console.error('Lỗi API khi tìm kiếm:', error);
    return { error: true };
  }
};

// Tắt voucher
const offVoucher = async (id) => {
  try {
    const { data } = await axiosInstance.get(`/admin/quan_ly_voucher/off?id=${id}`);
    return data; // API trả về String, không cần normalize
  } catch (error) {
    console.error('Lỗi API khi tắt voucher:', error);
    return { error: true };
  }
};

// Lọc voucher theo khoảng giá trị giảm
const timKiemVoucherByPrice = async (minPrice, maxPrice, page, size) => {
  try {
    const { data } = await axiosInstance.get(
      `/admin/quan_ly_voucher/tim-kiem-by-price?page=${page}&size=${size}&minPrice=${minPrice || ''}&maxPrice=${maxPrice || ''}`
    );
    return normalizeResponse(data);
  } catch (error) {
    console.error('Lỗi API khi lọc theo giá:', error);
    return { error: true };
  }
};

// Lọc voucher theo khoảng ngày
const timKiemVoucherByDate = async (startDate, endDate, page, size) => {
  try {
    const { data } = await axiosInstance.get(
      `/admin/quan_ly_voucher/tim-kiem-by-date?page=${page}&size=${size}&startDate=${startDate || ''}&endDate=${endDate || ''}`
    );
    return normalizeResponse(data);
  } catch (error) {
    console.error('Lỗi API khi lọc theo ngày:', error);
    return { error: true };
  }
};

export const voucherService = {
  getAllVouchers,
  getVoucherLocTrangThai,
  searchVoucher,
  offVoucher,
  timKiemVoucherByPrice,
  timKiemVoucherByDate,
  addVoucher,
  updateVoucher,
  getVoucherById,
  getVoucherLocKieuGiamGia,
};