// src/services/khuyenMaiService.js
import axiosInstance from "@/config/axiosConfig";

const normalizeResponse = (data) => {
  if (data.error) return { error: true, content: [], totalPages: 0, totalElements: 0 };
  return {
    content: data.content || [],
    totalPages: data.totalPages || 0,
    totalElements: data.totalElements || 0,
  };
};

// Lấy tất cả khuyến mãi
const getAllKhuyenMai = async (page, size) => {
  try {
    const { data } = await axiosInstance.get(`/api/khuyen-mai/hien-thi-KM?page=${page}&size=${size}`);
    return normalizeResponse(data);
  } catch (error) {
    console.error('Lỗi API:', error);
    return { error: true };
  }
};

// Lọc khuyến mãi theo trạng thái
const getKhuyenMaiLocTrangThai = async (page, size, trangThai) => {
  try {
    const { data } = await axiosInstance.get(
      `/api/khuyen-mai/loc-trang-thai-KM?page=${page}&size=${size}&trangThai=${trangThai}`
    );
    return normalizeResponse(data);
  } catch (error) {
    console.error('Lỗi API:', error);
    return { error: true };
  }
};


const getKhuyenMaiLocKieuGiamGia = async (page, size, kieuGiamGia) => {
  try {
    console.log('Sending request to API:', `api/khuyen-mai/loc-kieu-giam-gia-KM?page=${page}&size=${size}&kieuGiamGia=${kieuGiamGia}`);
    const { data } = await axiosInstance.get(
      `api/khuyen-mai/loc-kieu-giam-gia-KM?page=${page}&size=${size}&kieuGiamGia=${kieuGiamGia}`
    );
    console.log('Raw API data:', data);
    return normalizeResponse(data); // Đảm bảo hàm này tồn tại và hoạt động đúng
  } catch (error) {
    console.error('Lỗi khi gọi API:', error.message, error.response?.data || error);
    return { error: true };
  }
};
// Tìm kiếm khuyến mãi
const searchKhuyenMai = async (keyword, page, size) => {
  try {
    const { data } = await axiosInstance.get(
      `/api/khuyen-mai/tim-kiem-KM?keyword=${keyword}&page=${page}&size=${size}`
    );
    return normalizeResponse(data);
  } catch (error) {
    console.error('Lỗi API:', error);
    return { error: true };
  }
};

// Tắt khuyến mãi
const offKhuyenMai = async (id) => {
  try {
    const { data } = await axiosInstance.get(`/api/khuyen-mai/off-KM?id=${id}`);
    return normalizeResponse(data);
  } catch (error) {
    console.error('Lỗi API:', error);
    return { error: true };
  }
};

// Lọc khuyến mãi theo khoảng giá trị giảm
const timKiemKhuyenMaiByPrice = async (minPrice, maxPrice, page, size) => {
  try {
    const { data } = await axiosInstance.get(
      `/api/khuyen-mai/tim-kiem-KM-by-price?page=${page}&size=${size}&minPrice=${minPrice || ''}&maxPrice=${maxPrice || ''}`
    );
    return normalizeResponse(data);
  } catch (error) {
    console.error('Lỗi API khi lọc theo giá:', error);
    return { error: true };
  }
};

// Lọc khuyến mãi theo khoảng ngày
const timKiemKhuyenMaiByDate = async (startDate, endDate, page, size) => {
  try {
    const { data } = await axiosInstance.get(
      `/api/khuyen-mai/tim-kiem-KM-by-date?page=${page}&size=${size}&startDate=${startDate || ''}&endDate=${endDate || ''}`
    );
    return normalizeResponse(data);
  } catch (error) {
    console.error('Lỗi API khi lọc theo ngày:', error);
    return { error: true };
  }
};

// Thêm khuyến mãi mới
const addKhuyenMai = async (khuyenMaiData, selectedChiTietSanPhamIds) => {
  try {
    const response = await axiosInstance.post('/api/khuyen-mai/add-KM', khuyenMaiData, {
      params: { selectedChiTietSanPhamIds: selectedChiTietSanPhamIds.join(',') },
    });
    return response.data; // Trả về chuỗi từ backend (ví dụ: "Thêm khuyến mãi thành công!")
  } catch (error) {
    console.error('Lỗi khi thêm khuyến mãi:', error);
    throw error; // Ném lỗi để xử lý ở tầng trên
  }
};

// Tìm kiếm sản phẩm (đã có phân trang, sửa lại cho phù hợp)
const searchSanPhamKM = async (keyword, page, size) => {
  try {
    const response = await axiosInstance.get('/api/khuyen-mai/search-san-pham', {
      params: { keywordSanPham: keyword, page, size },
    });
    return normalizeResponse(response.data); // Chuẩn hóa dữ liệu phân trang
  } catch (error) {
    console.error('Lỗi khi tìm kiếm sản phẩm:', error);
    return { error: true, content: [], totalPages: 0, totalElements: 0 };
  }
};

// Lấy chi tiết sản phẩm theo sản phẩm
const getChiTietSanPhamBySanPham = async (idSanPham) => {
  try {
    const response = await axiosInstance.get('/api/khuyen-mai/chi-tiet-san-pham-by-san-pham', {
      params: { idSanPham },
    });
    return response.data; // Trả về danh sách chi tiết sản phẩm
  } catch (error) {
    console.error(`Lỗi khi lấy chi tiết sản phẩm cho ID ${idSanPham}:`, error);
    return [];
  }
};

// Lấy chi tiết khuyến mãi theo ID (sử dụng endpoint mới)
const getKhuyenMaiById = async (id) => {
  try {
    const { data } = await axiosInstance.get(`/api/khuyen-mai/get-khuyen-mai-by-id`, {
      params: { id }, // Truyền id như query parameter
    });
    if (data && !data.error) {
      return data; // Trả về object KhuyenMaiResponse bao gồm chiTietSanPhams
    } else {
      throw new Error('Dữ liệu không hợp lệ từ server');
    }
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết khuyến mãi:', error);
    return { error: true, message: error.message };
  }
};

// Cập nhật khuyến mãi
const updateKhuyenMai = async (khuyenMaiData, selectedChiTietSanPhamIds) => {
  try {
    const response = await axiosInstance.put('/api/khuyen-mai/update-KM', khuyenMaiData, {
      params: { selectedChiTietSanPhamIds: selectedChiTietSanPhamIds.join(',') },
    });
    return response.data; // Chuỗi như "Cập nhật khuyến mãi thành công!"
  } catch (error) {
    console.error('Lỗi khi cập nhật khuyến mãi:', error);
    throw error.response?.data || 'Có lỗi xảy ra khi cập nhật khuyến mãi';
  }
};

export const khuyenMaiService = {
  getAllKhuyenMai,
  getKhuyenMaiLocTrangThai,
  searchKhuyenMai,
  offKhuyenMai,
  timKiemKhuyenMaiByPrice,
  timKiemKhuyenMaiByDate,
  addKhuyenMai,
  searchSanPhamKM,
  getChiTietSanPhamBySanPham,
  updateKhuyenMai,
  getKhuyenMaiById,
  getKhuyenMaiLocKieuGiamGia,
};