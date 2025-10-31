import axiosInstance from "@/config/axiosConfig";

// Get reviews for a specific product
const getProductReviews = async (idChiTietSanPham) => {
  try {
    const response = await axiosInstance.get(`/api/reviews/product/${idChiTietSanPham}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching product reviews:', error);
    return {
      error: true,
      message: error.response?.data?.message || 'Lỗi khi tải đánh giá sản phẩm',
      reviews: [],
      averageRating: 0,
      totalReviews: 0,
      ratingDistribution: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
    };
  }
};

// Add a new review
const addProductReview = async (reviewData) => {
  try {
    const response = await axiosInstance.post('/api/reviews/add', reviewData);
    return response.data;
  } catch (error) {
    console.error('Error adding product review:', error);
    // Trả về đối tượng lỗi có cấu trúc giống như backend để component có thể xử lý nhất quán
    return {
      error: true,
      message: error.response?.data?.message || 'Lỗi khi thêm đánh giá'
    };
  }
};

// Update an existing review
const updateProductReview = async (reviewId, reviewData) => {
  try {
    const response = await axiosInstance.put(`/api/reviews/update/${reviewId}`, reviewData);
    return response.data;
  } catch (error) {
    console.error('Error updating product review:', error);
    return {
      error: true,
      message: error.response?.data?.message || 'Lỗi khi cập nhật đánh giá'
    };
  }
};

// Delete a review
const deleteProductReview = async (reviewId) => {
  try {
    const response = await axiosInstance.delete(`/api/reviews/delete/${reviewId}`);
    return response.data;
  } catch (error) {
    console.error('Error deleting product review:', error);
    return {
      error: true,
      message: error.response?.data?.message || 'Lỗi khi xóa đánh giá'
    };
  }
};

// đoạn mới thêm
// Kiểm tra nếu khách hàng có thể đánh giá sản phẩm
const checkCanReviewProduct = async (idKhachHang, idChiTietSanPham) => {
  try {
    const response = await axiosInstance.get(
      `/api/reviews/can-review?idKhachHang=${idKhachHang}&idChiTietSanPham=${idChiTietSanPham}`,
    )
    return response.data
  } catch (error) {
    console.error('Error checking if customer can review product:', error)
    return {
      canReview: false,
      message: error.response?.data?.message || 'Lỗi khi kiểm tra quyền đánh giá sản phẩm',
    }
  }
}

export const reviewService = {
  getProductReviews,
  addProductReview,
  updateProductReview,
  deleteProductReview,
  checkCanReviewProduct
};
