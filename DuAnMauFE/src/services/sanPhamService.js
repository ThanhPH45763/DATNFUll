import axiosInstance from '@/config/axiosConfig'
// import { toast } from "vue3-toastify/index";
//Hàm xử lý dữ liệu
const qlsp = 'admin/quan_ly_san_pham/'
const getAllSanPham = async () => {
  try {
    const { data } = await axiosInstance.get(qlsp + 'SanPham')
    return data
  } catch (error) {
    console.log(error)
    console.log('Không lấy được danh sách sản phẩm')
  }
}

const getAllChiTietSanPham = async () => {
  try {
    const { data } = await axiosInstance.get(qlsp + 'getAllCTSP')
    return data
  } catch (error) {
    console.log(error)
    console.log('Không lấy được danh sách chi tiết sản phẩm')
  }
}
const searchChiTietSanPham = async (search) => {
  try {
    const { data } = await axiosInstance.get(qlsp + 'searchCTSP?keywork=' + search)
    console.log('Datasearch', data)
    return data
  } catch (error) {
    console.log(error)
    console.log('Không lấy được danh sách chi tiết sản phẩm')
  }
}

const getImageInCTSP = async (id, anhChinh) => {
  try {
    const { data } = await axiosInstance.get(
      qlsp + 'HinhAnhSanPham?idCTSP=' + id + '&anhChinh=' + anhChinh,
    )
    return data
  } catch (error) {
    console.log(error)
    // toast.error('Không lấy được hình anh')
  }
}
const getCTSPBySanPham = async (id) => {
  try {
    const { data } = await axiosInstance.get(qlsp + 'CTSPTheoSanPham?id=' + id)
    return data
  } catch (error) {
    console.log(error)
  }
}
const changeStatusSanPham = async (id) => {
  try {
    const { data } = await axiosInstance.put(qlsp + 'chuyenTrangThaiSanPham?id=' + id)
    return data
  } catch (error) {
    console.log(error)
  }
}
const changeStatusCTSP = async (id) => {
  try {
    const { data } = await axiosInstance.put(qlsp + 'changeStatusCTSP?id=' + id)
    return data
  } catch (error) {
    console.log(error)
  }
}

// Hàm cập nhật trạng thái cho tất cả chi tiết sản phẩm theo sản phẩm chính
const updateCTSPStatusBySanPham = async (id, status) => {
  try {
    const { data } = await axiosInstance.put(qlsp + 'updateCTSPStatusBySanPham', {
      id_san_pham: id,
      trang_thai: status,
    })
    return data
  } catch (error) {
    console.log('Lỗi khi cập nhật trạng thái CTSP:', error)
    throw error
  }
}

const getDanhMucList = async () => {
  try {
    const { data } = await axiosInstance.get(qlsp + 'DanhMuc')
    return data
  } catch (error) {
    console.log(error)
  }
}
const getThuongHieuList = async () => {
  try {
    const { data } = await axiosInstance.get(qlsp + 'ThuongHieu')
    return data
  } catch (error) {
    console.log(error)
  }
}
const getChatLieuList = async () => {
  try {
    const { data } = await axiosInstance.get(qlsp + 'ChatLieu')
    return data
  } catch (error) {
    console.log(error)
  }
}
const getMauSacList = async () => {
  try {
    const { data } = await axiosInstance.get(qlsp + 'MauSac')
    return data
  } catch (error) {
    console.log(error)
  }
}
const getSizeList = async () => {
  try {
    const { data } = await axiosInstance.get(qlsp + 'Size')
    return data
  } catch (error) {
    console.log(error)
  }
}

const createSanPhams = async (data) => {
  try {
    console.log('Data gửi đi:', data)
    const response = await axiosInstance.post(qlsp + 'saveSanPham', data)
    console.log('Response từ server:', response)
    return response.data
  } catch (error) {
    console.error('Chi tiết lỗi:', error)
    throw error
  }
}
const createCTSP = async (data) => {
  try {
    console.log('Data CTSP gửi đi (final):', data);
    const response = await axiosInstance.post(qlsp + 'saveCTSP', data);
    return response.data;
  } catch (error) {
    console.log('Chi tiết lỗi từ server:', error.response?.data || 'Không có thông tin chi tiết');
    console.log('Lỗi thêm chi tiết sản phẩm', error);
    throw error;
  }
}

// Hàm lấy ngày hiện tại theo định dạng yyyy-MM-dd
const getCurrentDate = () => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// Hàm lấy ngày giờ hiện tại đầy đủ
const getCurrentDateTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const importSanPhamFromExcel = async (file) => {
  try {
    console.log('Service processing file:', file);
    console.log('File name:', file.name);
    console.log('File type:', file.type);
    console.log('File size:', file.size, 'bytes');

    // Ensure we have a valid File object
    if (!(file instanceof File) && !(file instanceof Blob)) {
      console.error('Invalid file object:', file);
      throw new Error('Invalid file object provided');
    }

    const formData = new FormData();
    formData.append('file', file);

    // Log form data entries for debugging
    for (let pair of formData.entries()) {
      console.log('FormData entry:', pair[0], pair[1]);
    }

    const response = await axiosInstance.post(qlsp + 'listImport', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    return response.data;
  } catch (error) {
    console.log('Excel import error:', error);
    console.log('Error response:', error.response?.data);
    throw error;
  }
}
const saveExcelImports = async (data) => {
  try {
    const response = await axiosInstance.post(qlsp + 'save', data, {
      headers: {
        'Content-Type': 'application/json',
      },
    })
    return response.data
  } catch (error) {
    console.error('Lỗi khi lưu dữ liệu Excel:', error)
    return {
      error: true,
      message: error.message || 'Có lỗi xảy ra khi lưu dữ liệu Excel',
    }
  }
}
const getSanPhamById = async (id) => {
  try {
    const response = await axiosInstance.get(qlsp + 'sanPhamDetail?id=' + id)
    // Lấy dữ liệu từ response.data vì đây là một object đơn lẻ
    return response.data
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết sản phẩm:', error)
    return {
      error: true,
      message: error.message || 'Có lỗi xảy ra khi lấy chi tiết sản phẩm',
    }
  }
}
const getSanPhamBySanPham = async (tenSanPham) => {
  try {
    const response = await axiosInstance.get(
      qlsp + 'getSanPhamByTenSanPham?tenSanPham=' + tenSanPham,
    )
    return response.data
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết sản phẩm:', error)
    return {
      error: true,
      message: error.message || 'Có lỗi xảy ra khi lấy chi tiết sản phẩm',
    }
  }
}
const getAllCTSPKM = async () => {
  try {
    const response = await axiosInstance.get(qlsp + 'getAllCTSPKM')
    return response.data
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết sản phẩm khuyến mãi:', error)
    return {
      error: true,
      message: error.message || 'Có lỗi xảy ra khi lấy chi tiết sản phẩm khuyến mái',
    }
  }
}
const getCTSPBySanPhamFull = async (idSanPham) => {
  try {
    const response = await axiosInstance.get(qlsp + 'CTSPBySanPhamFullWeb?idSanPham=' + idSanPham)
    return response.data
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết sản phẩm:', error)
    return {
      error: true,
      message: error.message || 'Có lỗi xảy ra khi lấy chi tiết sản phẩm',
    }
  }
}
const getAllSanPhamNgaySua = async () => {
  try {
    const { data } = await axiosInstance.get(qlsp + 'sanPhamTheoNgaySua')
    return data
  } catch (error) {
    console.log(error)
    console.log('Không lấy được danh sách sản phẩm')
  }
}
const searchSanPham = async (search) => {
  try {
    const response = await axiosInstance.get(qlsp + 'timKiemSanPham?search=' + search)
    console.log('Data search sản phẩm:', response.data)
    return response.data
  } catch (error) {
    console.log(error)
    console.log('Không lấy được danh sách sản phẩm theo từ khóa')
    return { error: true, data: [] }
  }
}

const exportExcel = async (productIds, fields) => {
  try {
    const response = await axiosInstance.post(qlsp + 'exportExcel',
      {
        productIds: productIds,
        fields: fields
      },
      {
        responseType: 'blob', // Quan trọng để nhận dữ liệu dạng file
      }
    );
    return response.data;
  } catch (error) {
    console.error('Lỗi khi xuất Excel:', error);
    throw error;
  }
}
const changeAllCTSPHoatDong = async (id) => {
  try {
    const response = await axiosInstance.put(qlsp + 'changeAllCTSPHoatDong?id=' + id)
    console.log('Response changeAllCTSPHoatDong:', response)
    return response.data
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái CTSP:', error)
  }
}
const changeAllCTSPKhongHoatDong = async (id) => {
  try {
    const response = await axiosInstance.put(qlsp + 'changeAllCTSPKhongHoatDong?id=' + id)
    console.log('Response changeAllCTSPKhongHoatDong:', response)
    return response.data
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái CTSP:', error)
  }
}

// Add new service methods for adding items
const addDanhMuc = async (danhMucData) => {
  try {
    const response = await axiosInstance.post(qlsp + 'addDanhMuc?tenDanhMuc=' + danhMucData);
    return response.data;
  } catch (error) {
    console.error('Error in addDanhMuc:', error);
    return { error: true, message: error.response?.data?.message || 'Có lỗi xảy ra khi thêm danh mục' };
  }
};

const addThuongHieu = async (thuongHieuData) => {
  try {
    const response = await axiosInstance.post(qlsp + 'addThuongHieu?tenThuongHieu=' + thuongHieuData);
    return response.data;
  } catch (error) {
    console.error('Error in addThuongHieu:', error);
    return { error: true, message: error.response?.data?.message || 'Có lỗi xảy ra khi thêm thương hiệu' };
  }
};

const addChatLieu = async (chatLieuData) => {
  try {
    const response = await axiosInstance.post(qlsp + 'addChatLieu?tenChatLieu=' + chatLieuData);
    return response.data;
  } catch (error) {
    console.error('Error in addChatLieu:', error);
    return { error: true, message: error.response?.data?.message || 'Có lỗi xảy ra khi thêm chất liệu' };
  }
};

const addMauSac = async (mauSacData) => {
  try {
    const response = await axiosInstance.post(qlsp + 'addMauSac?tenMauSac=' + mauSacData);
    return response.data;
  } catch (error) {
    console.error('Error in addMauSac:', error);
    return { error: true, message: error.response?.data?.message || 'Có lỗi xảy ra khi thêm màu sắc' };
  }
};

const addKichThuoc = async (giaTri, donVi) => {
  try {
    const response = await axiosInstance.post(qlsp + 'addSize?giaTri=' + giaTri + '&donVi=' + donVi);
    return response.data;
  } catch (error) {
    console.error('Error in addKichThuoc:', error);
    return { error: true, message: error.response?.data?.message || 'Có lỗi xảy ra khi thêm kích thước' };
  }
};
//Thuộc tính
const changeTrangThaiChatLieu = async (id) => {
  try {
    const { data } = await axiosInstance.put(qlsp + 'changeTrangThaiChatLieu?idChatLieu=' + id)
    return data
  } catch (error) {
    console.log(error)
  }
}
const changeTrangThaiMauSac = async (id) => {
  try {
    const { data } = await axiosInstance.put(qlsp + 'changeTrangThaiMauSac?idMauSac=' + id)
    return data
  } catch (error) {
    console.log(error)
  }
}
const changeTrangThaiThuongHieu = async (id) => {
  try {
    const { data } = await axiosInstance.put(qlsp + 'changeTrangThaiThuongHieu?idThuongHieu=' + id)
    return data
  } catch (error) {
    console.log(error)
  }
}
const changeTrangThaiDanhMuc = async (id) => {
  try {
    const { data } = await axiosInstance.put(qlsp + 'changeTrangThaiDanhMuc?idDanhMuc=' + id)
    return data
  } catch (error) {
    console.log(error)
  }
}
const changeTrangThaiKichThuoc = async (id) => {
  try {
    const { data } = await axiosInstance.put(qlsp + 'changeTrangThaiKichThuoc?idKichThuoc=' + id)
    return data
  } catch (error) {
    console.log(error)
  }
}
const updateChatLieu = async (chatLieuData) => {
  try {
    const { data } = await axiosInstance.put(qlsp + 'updateChatLieu', chatLieuData)
    return data
  } catch (error) {
    console.log(error)
  }
}
const updateMauSac = async (mauSacData) => {
  try {
    const { data } = await axiosInstance.put(qlsp + 'updateMauSac', mauSacData)
    return data
  } catch (error) {
    console.log(error)
  }
}
const updateThuongHieu = async (thuongHieuData) => {
  try {
    const { data } = await axiosInstance.put(qlsp + 'updateThuongHieu', thuongHieuData)
    return data
  } catch (error) {
    console.log(error)
  }
}
const updateDanhMuc = async (danhMucData) => {
  try {
    const { data } = await axiosInstance.put(qlsp + 'updateDanhMuc', danhMucData)
    return data
  } catch (error) {
    console.log(error)
  }
}
const updateKichThuoc = async (kichThuocData) => {
  try {
    const { data } = await axiosInstance.put(qlsp + 'updateKichThuoc', kichThuocData)
    return data
  } catch (error) {
    console.log(error)
  }
}

const locSanPhamVaChiTietSanPham = async (keyword, giaBanMin, giaBanMax, listMauSac, listDanhMuc, listThuongHieu, listChatLieu, listKichThuoc) => {
  try {
    // Xây dựng URL với các tham số
    let url = `${qlsp}locCTSP?`;

    // Thêm các tham số nếu chúng tồn tại và không phải null
    if (keyword) url += `keyword=${encodeURIComponent(keyword)}&`;
    if (giaBanMin !== undefined && giaBanMin !== null) url += `giaBanMin=${giaBanMin}&`;
    if (giaBanMax !== undefined && giaBanMax !== null) url += `giaBanMax=${giaBanMax}&`;

    // Thêm các danh sách nếu chúng tồn tại
    if (listMauSac && listMauSac.length > 0) {
      listMauSac.forEach(mauSac => {
        url += `listMauSac=${encodeURIComponent(mauSac)}&`;
      });
    }

    if (listDanhMuc && listDanhMuc.length > 0) {
      listDanhMuc.forEach(danhMuc => {
        url += `listDanhMuc=${encodeURIComponent(danhMuc)}&`;
      });
    }

    if (listThuongHieu && listThuongHieu.length > 0) {
      listThuongHieu.forEach(thuongHieu => {
        url += `listThuongHieu=${encodeURIComponent(thuongHieu)}&`;
      });
    }

    if (listChatLieu && listChatLieu.length > 0) {
      listChatLieu.forEach(chatLieu => {
        url += `listChatLieu=${encodeURIComponent(chatLieu)}&`;
      });
    }

    if (listKichThuoc && listKichThuoc.length > 0) {
      listKichThuoc.forEach(kichThuoc => {
        url += `listKichThuoc=${encodeURIComponent(kichThuoc)}&`;
      });
    }

    // Loại bỏ dấu & cuối cùng nếu có
    url = url.endsWith('&') ? url.slice(0, -1) : url;

    console.log('URL tìm kiếm và lọc:', url);

    const response = await axiosInstance.get(url);
    console.log('Response locSanPhamVaChiTietSanPham:', response.data);
    return response.data;
  } catch (error) {
    console.error('Lỗi khi lọc sản phẩm và chi tiết sản phẩm:', error);
    return {
      error: true,
      message: error.message || 'Có lỗi xảy ra khi lọc sản phẩm và chi tiết sản phẩm',
    };
  }
}
const giaMax = async () => {
  try {
    const response = await axiosInstance.get(qlsp + 'giaLonNhat')
    return response.data
  } catch (error) {
    console.error('Lỗi khi lấy giá max:', error)
    return {
      error: true,
      message: error.message || 'Có lỗi xảy ra khi lấy giá max',
    }
  }
}
const getSanPhamByTenSP = async (tenSanPham) => {
  try {
    const response = await axiosInstance.get(qlsp + 'getSanPhamByTenSP?tenSanPham=' + tenSanPham)
    return response.data
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết sản phẩm:', error)
    return {
      error: true,
      message: error.message || 'Có lỗi xảy ra khi lấy chi tiết sản phẩm',
    }
  }
}
const getSanPhamByTenDM  = async (tenDanhMuc) => {
  try {
    const response = await axiosInstance.get(qlsp + 'getSanPhamByTenDM?tenDanhMuc=' + tenDanhMuc)
    return response.data
  } catch (error) {
    console.error('Lỗi khi lấy chi tiết sản phẩm:', error)
    return {
      error: true,
      message: error.message || 'Có lỗi xảy ra khi lấy chi tiết sản phẩm',
    }
  }
}
const getSanPhamSieuSale = async () => {
  try {
    const response = await axiosInstance.get(qlsp + 'getSanPhamSieuSale')
    return response.data
  } catch (error) {
    console.error('Lỗi khi lấy sản phẩm siêu sale:', error)
    return {
      error: true,
      message: error.message || 'Có lỗi xảy ra khi lấy sản phẩm siêu sale',
    }
  }
}
export const sanPhamService = {
  getAllSanPham,
  getAllChiTietSanPham,
  getImageInCTSP,
  searchChiTietSanPham,
  getCTSPBySanPham,
  changeStatusSanPham,
  updateCTSPStatusBySanPham,
  getDanhMucList,
  getThuongHieuList,
  getChatLieuList,
  getMauSacList,
  getSizeList,
  createSanPhams,
  createCTSP,
  getCurrentDate,
  getCurrentDateTime,
  importSanPhamFromExcel,
  saveExcelImports,
  getSanPhamById,
  getSanPhamBySanPham,
  getCTSPBySanPhamFull,
  getAllSanPhamNgaySua,
  getAllCTSPKM,
  searchSanPham,
  exportExcel,
  changeStatusCTSP,
  changeAllCTSPHoatDong,
  changeAllCTSPKhongHoatDong,
  addDanhMuc,
  addThuongHieu,
  addChatLieu,
  addMauSac,
  addKichThuoc,
  changeTrangThaiChatLieu,
  changeTrangThaiMauSac,
  changeTrangThaiThuongHieu,
  changeTrangThaiDanhMuc,
  changeTrangThaiKichThuoc,
  updateChatLieu,
  updateMauSac,
  updateThuongHieu,
  updateDanhMuc,
  updateKichThuoc,
  locSanPhamVaChiTietSanPham,
  giaMax,
  getSanPhamByTenSP,
  getSanPhamByTenDM,
  getSanPhamSieuSale
}
