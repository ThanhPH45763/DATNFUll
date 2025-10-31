import axiosInstance from '@/config/axiosConfig'

// Thêm sản phẩm vào danh sách yêu thích
const addToFavorite = async (idKhachHang, idChiTietSanPham) => {
  try {
    const response = await axiosInstance.post(`/api/favorites/add?idKhachHang=${idKhachHang}&idChiTietSanPham=${idChiTietSanPham}`)
    return response.data
  } catch (error) {
    console.error('Lỗi khi thêm vào yêu thích:', error)
    throw error
  }
}

// Xóa sản phẩm khỏi danh sách yêu thích
const removeFromFavorite = async (idKhachHang, idChiTietSanPham) => {
  try {
    const response = await axiosInstance.delete(`/api/favorites/remove?idKhachHang=${idKhachHang}&idChiTietSanPham=${idChiTietSanPham}`)
    return response.data
  } catch (error) {
    console.error('Lỗi khi xóa khỏi yêu thích:', error)
    throw error
  }
}

// Kiểm tra trạng thái yêu thích của sản phẩm
const checkFavoriteStatus = async (idKhachHang, idChiTietSanPham) => {
  try {
    const response = await axiosInstance.get(`/api/favorites/check?idKhachHang=${idKhachHang}&idChiTietSanPham=${idChiTietSanPham}`)
    return response.data
  } catch (error) {
    console.error('Lỗi khi kiểm tra trạng thái yêu thích:', error)
    throw error
  }
}

// Lấy số lượt yêu thích của sản phẩm
const getFavoriteCount = async (idChiTietSanPham) => {
  try {
    const response = await axiosInstance.get(`/api/favorites/count?idChiTietSanPham=${idChiTietSanPham}`)
    return response.data
  } catch (error) {
    console.error('Lỗi khi lấy số lượt yêu thích:', error)
    throw error
  }
}

// Lấy số lượng sản phẩm yêu thích của một khách hàng
const getCustomerFavoritesCount = async (idKhachHang) => {
  try {
    const response = await axiosInstance.get(`/api/favorites/customer-count?idKhachHang=${idKhachHang}`)
    return response.data
  } catch (error) {
    console.error('Lỗi khi lấy số lượng sản phẩm yêu thích của khách hàng:', error)
    throw error
  }
}

// Đồng bộ danh sách yêu thích từ localStorage lên server
const syncFavoritesToServer = async (idKhachHang) => {
  try {
    // Lấy danh sách yêu thích từ localStorage
    const wishlist = JSON.parse(localStorage.getItem('gb-sport-wishlist') || '[]')

    // Đồng bộ từng sản phẩm
    for (const idChiTietSanPham of wishlist) {
      try {
        await addToFavorite(idKhachHang, idChiTietSanPham)
      } catch (error) {
        console.error(`Lỗi khi đồng bộ sản phẩm ${idChiTietSanPham}:`, error)
      }
    }

    return { status: 'success', message: 'Đồng bộ thành công' }
  } catch (error) {
    console.error('Lỗi khi đồng bộ danh sách yêu thích:', error)
    throw error
  }
}

// Lấy danh sách ID sản phẩm yêu thích của một khách hàng
const getFavoriteProductIds = async (idKhachHang) => {
  try {
    const response = await axiosInstance.get(`/api/favorites/product-ids?idKhachHang=${idKhachHang}`)
    return response.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách ID sản phẩm yêu thích:', error)
    return []
  }
}

// Export các functions
export const favoriteService = {
  addToFavorite,
  removeFromFavorite,
  checkFavoriteStatus,
  getFavoriteCount,
  getCustomerFavoritesCount,
  getFavoriteProductIds,
  syncFavoritesToServer
}
