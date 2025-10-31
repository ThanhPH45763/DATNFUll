import axiosInstance from '@/config/axiosConfig'

// Lấy danh sách khách hàng
const getAllKhachHang = async (
  page = 0,
  size = 5,
  keyword = null,
  trangThai = null,
  updatedId = null,
) => {
  try {
    let url = `/api/khach-hang/view?page=${page}&size=${size}`
    if (keyword) {
      url += `&keyword=${encodeURIComponent(keyword)}`
    }
    if (trangThai) {
      url += `&trangThai=${encodeURIComponent(trangThai)}`
    }
    if (updatedId) {
      url += `&updatedId=${updatedId}`
    }
    const { data } = await axiosInstance.get(url)
    return data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách khách hàng:', error)
    return {
      error: true,
      message: 'Có lỗi xảy ra khi tải danh sách khách hàng',
    }
  }
}

// Thêm khách hàng mới
const themKhachHang = async (data) => {
  try {
    const response = await axiosInstance.post('/api/khach-hang/add', data)
    return response.data
  } catch (error) {
    console.error('Lỗi khi thêm khách hàng:', error)
    throw error // Ném lỗi gốc để giữ thông tin từ backend
  }
}

// Lấy thông tin khách hàng để chỉnh sửa
const getKhachHangByIdForEdit = async (id) => {
  try {
    const response = await axiosInstance.get(`/api/khach-hang/edit/${id}`)
    if (response.data.error) {
      return { error: true, message: response.data.error }
    }
    return response.data // { khachHang }
  } catch (error) {
    console.error('Lỗi khi lấy thông tin khách hàng:', error)
    return {
      error: true,
      message: error.response?.data?.error || 'Có lỗi xảy ra khi lấy thông tin khách hàng',
    }
  }
}
const suaKhachHang = async (khachHangData) => {
  try {
    const { data } = await axiosInstance.put('/api/khach-hang/update', khachHangData)
    return data
  } catch (error) {
    console.error('Lỗi khi cập nhật khách hàng:', error)
    return {
      error: true,
      message: 'Có lỗi xảy ra khi cập nhật khách hàng',
    }
  }
}

// Lấy chi tiết khách hàng
const getKhachHangDetail = async (id) => {
  try {
    const response = await axiosInstance.get(`/api/khach-hang/detail/${id}`)
    return response.data
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết khách hàng:', error)
    return { error: true, message: 'Có lỗi xảy ra khi lấy chi tiết khách hàng' }
  }
}

// Thay đổi trạng thái khách hàng
const changeTrangThai = async (idKhachHang) => {
  try {
    const response = await axiosInstance.post('/api/khach-hang/chuyen-trang-thai', null, {
      params: { idKhachHang },
    })
    return response.data
  } catch (error) {
    console.error('Lỗi khi thay đổi trạng thái khách hàng:', error)
    return { error: true, message: 'Có lỗi xảy ra khi thay đổi trạng thái' }
  }
}

const getAllKhachHangNoPage = async () => {
  try {
    const response = await axiosInstance.get('/api/khach-hang/getAllKH')
    return response.data
  } catch (error) {
    console.error('Lỗi khi lấy api alll khách hàng:', error)
    return { error: true, message: 'Có lỗi xảy ra khi lấy api alll khách hàng' }
  }
}

// Đăng ký khách hàng
const registerKhachHang = async (registerData) => {
  try {
    const { data } = await axiosInstance.post('api/khach-hang/register', registerData)
    return data
  } catch (error) {
    console.error('Lỗi khi đăng ký khách hàng:', error)
    return {
      error: true,
      message: error.response?.data?.error || 'Có lỗi xảy ra khi đăng ký khách hàng',
      fieldErrors: error.response?.data?.fieldErrors || null,
    }
  }
}
// Đăng nhập
const login = async (loginData) => {
  try {
    const { data } = await axiosInstance.post('api/khach-hang/login', loginData)
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
// Lấy thông tin chi tiết
const getUserDetail = async ({ username, id_roles }) => {
  try {
    let userDetails = null
    if ([1, 2, 3].includes(id_roles)) {
      // Gọi API lấy thông tin nhân viên
      const response = await axiosInstance.get(`admin/quan-ly-nhan-vien/details?tenDangNhap=${username}`)
      userDetails = response.data
      console.log('Thông tin nhân viên từ API:', userDetails)
    } else if (id_roles === 4) {
      // Gọi API lấy thông tin khách hàng
      const response = await axiosInstance.get(`api/khach-hang/details?tenDangNhap=${username}`)
      userDetails = response.data
      console.log('Thông tin khách hàng từ API:', userDetails)
    }
    return userDetails
  } catch (error) {
    console.error('Lỗi khi gọi API getUserDetail:', error)
    throw new Error(error.response?.data?.message || 'Không thể lấy thông tin chi tiết')
  }
}

const themKhachHangBH = async (data) => {
  try {
    const response = await axiosInstance.post('/api/khach-hang/addKHMoi', data);
    console.log('khachHangService response:', response.data); // Log phản hồi
    return response.data;
  } catch (error) {
    console.error('Lỗi khi thêm khách hàng:', error);
    console.log('Error response:', error.response); // Log chi tiết
    console.log('Error message:', error.message); // Log thông báo lỗi
    console.log('Error status:', error.response?.status); // Log mã trạng thái
    throw error; // Truyền lỗi gốc từ axios
  }
};


// Thêm địa chỉ mới
const addDiaChi = async (idKhachHang, diaChiData) => {
  try {
    const { data } = await axiosInstance.post('/api/khach-hang/dia-chi/add', {
      idKhachHang,
      ...diaChiData
    })
    return data
  } catch (error) {
    console.error('Lỗi khi thêm địa chỉ:', error)
    return {
      error: true,
      message: error.response?.data?.message || 'Có lỗi xảy ra khi thêm địa chỉ'
    }
  }
}

// Cập nhật địa chỉ
const updateDiaChi = async (idDiaChi, diaChiData) => {
  try {
    const { data } = await axiosInstance.put('/api/khach-hang/dia-chi/update', {
      idDiaChi,
      ...diaChiData
    })
    return data
  } catch (error) {
    console.error('Lỗi khi cập nhật địa chỉ:', error)
    return {
      error: true,
      message: error.response?.data?.message || 'Có lỗi xảy ra khi cập nhật địa chỉ'
    }
  }
}

// Xóa địa chỉ
const deleteDiaChi = async (idDiaChi) => {
  try {
    const { data } = await axiosInstance.delete(`/api/khach-hang/dia-chi/delete/${idDiaChi}`)
    return data
  } catch (error) {
    console.error('Lỗi khi xóa địa chỉ:', error)
    return {
      error: true,
      message: error.response?.data?.message || 'Có lỗi xảy ra khi xóa địa chỉ'
    }
  }
}

// Đổi mật khẩu
const changePassword = async (idKhachHang, passwordData) => {
  try {
    const response = await axiosInstance.post('/api/khach-hang/change-password', {
      idKhachHang,
      currentPassword: passwordData.currentPassword,
      newPassword: passwordData.newPassword
    });

    return response.data;
  } catch (error) {
    console.error('Lỗi khi đổi mật khẩu:', error);

    // Trích xuất thông báo lỗi từ phản hồi của backend
    if (error.response && error.response.data) {
      return {
        error: true,
        message: error.response.data.message || 'Có lỗi xảy ra khi đổi mật khẩu'
      };
    }

    return {
      error: true,
      message: 'Có lỗi xảy ra khi đổi mật khẩu'
    };
  }
}

// Tạo một service mới để xử lý địa chỉ một cách riêng biệt
const fetchDiaChiByKhachHangId = async (idKhachHang) => {
  try {
    const { data } = await axiosInstance.get(`/api/khach-hang/detail/${idKhachHang}`);
    console.log('Dữ liệu chi tiết khách hàng từ API:', data);

    if (data && data.diaChiList && Array.isArray(data.diaChiList)) {
      return data.diaChiList;
    }
    return [];
  } catch (error) {
    console.error('Lỗi khi lấy địa chỉ khách hàng:', error);
    return [];
  }
};


export const khachHangService = {
  getAllKhachHang,
  themKhachHang,
  getKhachHangByIdForEdit,
  suaKhachHang,
  getKhachHangDetail,
  changeTrangThai,
  getAllKhachHangNoPage,
  registerKhachHang,
  login,
  getUserDetail,
  themKhachHangBH,
  addDiaChi,
  updateDiaChi,
  deleteDiaChi,
  changePassword,
  fetchDiaChiByKhachHangId
}
