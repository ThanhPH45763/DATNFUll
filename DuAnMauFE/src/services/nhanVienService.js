import axiosInstance from "@/config/axiosConfig";

// Lấy danh sách nhân viên
const layDanhSachNhanVien = async() => {
    try {
        const {data} = await axiosInstance.get('admin/quan-ly-nhan-vien/findAll');
        return data;
    } catch (error) {
        console.error('Lỗi API',error);
        return {error:true};
    }
}
const getAllNhanVien = async(page, size) => {
    try {
        // Đảm bảo page luôn >= 1
        const {data} = await axiosInstance.get(`admin/quan-ly-nhan-vien/phanTrang?page=${page}&size=${size}`)
        return data;
    } catch (error) {
        console.error('Lỗi API:', error);
        return { error: true };
    }
}
const getNhanVienLocTrangThai = async(page, size, trangThai) => {
    try {
        const {data} = await axiosInstance.get(`admin/quan-ly-nhan-vien/locTrangThai?page=${page}&size=${size}&trangThai=${trangThai}`)
        return data;
    } catch (error) {
        console.error('Lỗi API',error);
        return {error:true};
    }
}
const changeTrangThai = async(id) => {
    try {
        const data = await axiosInstance.put(`admin/quan-ly-nhan-vien/changeStatus?id=${id}`)
        return data;
    } catch (error) {
        console.error('Lỗi API',error);
        return {error:true};
    }
}
const themNhanViens = async(data) => {
    try {
        const response = await axiosInstance.post('admin/quan-ly-nhan-vien/add', data);
        return response;
    } catch (error) {
        console.error('Lỗi API',error); 
        return {error:true};
    }
}
const suaNhanViens = async(data) => {
    try {
        const response = await axiosInstance.put(`admin/quan-ly-nhan-vien/update`,data);
        return response;
    } catch (error) {
        console.error('Lỗi API',error);
        return {error:true};
    }
}
const getNhanVienById = async(id) => {
    try {
        const response = await axiosInstance.get(`admin/quan-ly-nhan-vien/findById?id=${id}`);
        return response.data;
    } catch (error) {
        console.error('Lỗi API',error);
        return {error:true};
    }
}
const searchNhanVien = async(keyword, page, size) => {
    try {
        const response = await axiosInstance.get(`admin/quan-ly-nhan-vien/search?keyword=${keyword}&page=${page}&size=${size}`);
        return response.data;
    } catch (error) {
        console.error('Lỗi API',error);
        return {error:true};
    }
}
// Đăng nhập by thằng làm thêm
const login = async (loginData) => {
    try {
      const { data } = await axiosInstance.post('admin/quan-ly-nhan-vien/login_admin', loginData)
      console.log('Dữ liệu từ API đăng nhập:', data) // In dữ liệu từ API đăng nhập
      return data
    } catch (error) {
      console.error('Lỗi khi đăng nhập:', error)
      return {
        error: true,
        message: error.response?.data?.error || 'Có lỗi xảy ra khi đăng nhập',
        fieldErrors: error.response?.data?.fieldErrors || null,
      }
    }
  }
export const nhanVienService = {
    getAllNhanVien,
    getNhanVienLocTrangThai,
    changeTrangThai,
    themNhanViens,
    suaNhanViens,
    layDanhSachNhanVien,
    getNhanVienById,
    searchNhanVien,
    login
}