package com.example.duanbe.service;

import com.example.duanbe.entity.BinhLuan;
import com.example.duanbe.entity.ChiTietSanPham;
import com.example.duanbe.entity.KhachHang;
import com.example.duanbe.entity.HoaDon;
import com.example.duanbe.entity.HoaDonChiTiet;
import com.example.duanbe.repository.ChiTietSanPhamRepo;
import com.example.duanbe.repository.HoaDonChiTietRepo;
import com.example.duanbe.repository.HoaDonRepo;
import com.example.duanbe.repository.KhachHangRepo;
import com.example.duanbe.repository.TheoDoiDonHangRepo;
import com.example.duanbe.response.BinhLuanRepository;
import com.example.duanbe.response.ReviewResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private BinhLuanRepository binhLuanRepository;

    @Autowired
    private ChiTietSanPhamRepo chiTietSanPhamRepo;

    @Autowired
    private KhachHangRepo khachHangRepo;

    @Autowired
    private HoaDonRepo hoaDonRepo;

    @Autowired
    private HoaDonChiTietRepo hoaDonChiTietRepo;

    @Autowired
    private TheoDoiDonHangRepo theoDoiDonHangRepo;

    public ReviewResponse getProductReviews(Integer idChiTietSanPham) {
        ChiTietSanPham chiTietSanPham = chiTietSanPhamRepo.findById(idChiTietSanPham)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        var reviews = binhLuanRepository.findByIdChiTietSanPham(idChiTietSanPham);

        double averageRating = reviews.stream()
                .filter(r -> r.getDanhGia() != null)
                .mapToDouble(review -> review.getDanhGia())
                .average()
                .orElse(0.0);

        Map<Integer, Long> ratingCount = reviews.stream()
                .filter(r -> r.getDanhGia() != null)
                .collect(Collectors.groupingBy(
                        review -> review.getDanhGia(),
                        Collectors.counting()
                ));

        Map<Integer, Integer> ratingDistribution = new HashMap<>();
        int totalReviews = reviews.size();

        for (int i = 1; i <= 5; i++) {
            long count = ratingCount.getOrDefault(i, 0L);
            int percentage = totalReviews > 0 ? (int) ((count * 100) / totalReviews) : 0;
            ratingDistribution.put(i, percentage);
        }

        var formattedReviews = reviews.stream().map(review -> {
            KhachHang khachHang = khachHangRepo.findById(review.getIdKhachHang())
                    .orElse(null);

            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("id", review.getIdBinhLuan());
            reviewData.put("id_khach_hang", review.getIdKhachHang());
            reviewData.put("id_chi_tiet_san_pham", review.getIdChiTietSanPham());
            reviewData.put("ten_nguoi_dung", khachHang != null ? khachHang.getHoTen() : "Khách hàng ẩn danh");
            reviewData.put("avatar", "/image/logo/default-avatar.png");
            reviewData.put("danh_gia", review.getDanhGia() != null ? review.getDanhGia() : 0);
            reviewData.put("noi_dung", review.getNoiDungBinhLuan());
            reviewData.put("ngay", review.getNgayTao() != null ? review.getNgayTao().toString() : "");
            reviewData.put("hinh_anh", List.of());

            Boolean isEdited = review.getChinhSua();
            reviewData.put("da_chinh_sua", isEdited != null && isEdited ? 1 : 0);

            return reviewData;
        }).collect(Collectors.toList());

        ReviewResponse response = new ReviewResponse();
        response.setAverageRating(Math.round(averageRating * 10.0) / 10.0);
        response.setTotalReviews(totalReviews);
        response.setReviews(formattedReviews);
        response.setRatingDistribution(ratingDistribution);

        return response;
    }

    /**
     * Kiểm tra xem khách hàng đã mua sản phẩm và đơn hàng có trạng thái hợp lệ không
     * @param idKhachHang ID của khách hàng
     * @param idChiTietSanPham ID của chi tiết sản phẩm
     * @return true nếu khách hàng đã mua sản phẩm với trạng thái hợp lệ, false nếu không
     */
    public boolean canCustomerReviewProduct(Integer idKhachHang, Integer idChiTietSanPham) {
        try {
            logger.info("Kiểm tra xem khách hàng ID {} có thể đánh giá sản phẩm ID {} không", idKhachHang, idChiTietSanPham);

            // CHỈ DÙNG CHO KIỂM TRA - Bỏ chú thích để bỏ qua xác thực trong quá trình kiểm tra
            // return true;

            // Lấy tất cả đơn hàng của khách hàng
            List<HoaDon> customerOrders = hoaDonRepo.findByKhachHang_IdKhachHang(idKhachHang);
            logger.info("Tìm thấy {} đơn hàng cho khách hàng ID {}", customerOrders.size(), idKhachHang);

            if (customerOrders.isEmpty()) {
                logger.info("Khách hàng không có đơn hàng, không thể đánh giá");
                return false;
            }

            // Chỉ trạng thái "Hoàn thành" được coi là hợp lệ để đánh giá
            List<String> validStatuses = Arrays.asList("Hoàn thành");

            for (HoaDon hoaDon : customerOrders) {
                logger.info("Kiểm tra đơn hàng ID {}", hoaDon.getId_hoa_don());

                // Kiểm tra trạng thái theo dõi đơn hàng từ theo_doi_don_hang
                String trackingStatus = hoaDonRepo.findLatestNonUpdatedStatusByIdHoaDon(hoaDon.getId_hoa_don());
                logger.info("Trạng thái theo dõi đơn hàng: {}", trackingStatus);

                // Đơn hàng hợp lệ chỉ khi trạng thái theo dõi là "Hoàn thành"
                boolean validTracking = trackingStatus != null && validStatuses.contains(trackingStatus);

                if (validTracking) {
                    logger.info("Đơn hàng có trạng thái hợp lệ (Hoàn thành)");

                    // Lấy chi tiết đơn hàng
                    List<HoaDonChiTiet> orderDetails = hoaDonChiTietRepo.findByIdHoaDon(hoaDon.getId_hoa_don());
                    logger.info("Đơn hàng có {} sản phẩm", orderDetails.size());

                    // Kiểm tra xem đơn hàng này có chứa sản phẩm không
                    for (HoaDonChiTiet detail : orderDetails) {
                        Integer productId = null;
                        try {
                            if (detail.getChiTietSanPham() != null) {
                                productId = detail.getChiTietSanPham().getId_chi_tiet_san_pham();
                            }
                        } catch (Exception e) {
                            logger.error("Lỗi khi lấy ID sản phẩm từ chi tiết đơn hàng", e);
                        }
                        logger.info("Đơn hàng chứa sản phẩm ID: {}", productId);

                        // Kiểm tra xem sản phẩm có khớp không
                        if (productId != null && productId.equals(idChiTietSanPham)) {
                            logger.info("Tìm thấy sản phẩm khớp trong đơn hàng hoàn thành! Khách hàng có thể đánh giá.");
                            return true;
                        }
                    }
                }
            }

            logger.info("Khách hàng chưa mua sản phẩm này với đơn hàng hoàn thành");
            return false;
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra xem khách hàng có thể đánh giá sản phẩm không", e);
            // Nếu có lỗi khi kiểm tra đơn hàng, không cho phép đánh giá
            return false;
        }
    }

    public Map<String, Object> addReview(Map<String, Object> reviewData) {
        try {
            Integer idKhachHang = Integer.parseInt(reviewData.get("id_khach_hang").toString());
            Integer idChiTietSanPham = Integer.parseInt(reviewData.get("id_chi_tiet_san_pham").toString());
            Integer rating = Integer.parseInt(reviewData.get("danh_gia").toString());
            String comment = (String) reviewData.get("binh_luan");

            KhachHang khachHang = khachHangRepo.findById(idKhachHang)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

            ChiTietSanPham chiTietSanPham = chiTietSanPhamRepo.findById(idChiTietSanPham)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            if (!canCustomerReviewProduct(idKhachHang, idChiTietSanPham)) {
                return Map.of(
                        "error", true,
                        "message", "Bạn chỉ có thể đánh giá sản phẩm sau khi đã mua và nhận hàng thành công."
                );
            }

            BinhLuan review = new BinhLuan();
            review.setIdKhachHang(idKhachHang);
            review.setIdChiTietSanPham(idChiTietSanPham);
            review.setDanhGia(rating);
            review.setNoiDungBinhLuan(comment);
            review.setNgayTao(new Date());
            review.setChinhSua(false);

            binhLuanRepository.save(review);

            return Map.of(
                    "error", false,
                    "message", "Đã thêm đánh giá thành công",
                    "reviewId", review.getIdBinhLuan()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "error", true,
                    "message", "Lỗi: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> updateReview(String reviewId, Map<String, Object> reviewData) {
        try {
            Integer idBinhLuan = Integer.parseInt(reviewId);

            BinhLuan existingReview = binhLuanRepository.findById(idBinhLuan)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

            if (reviewData.containsKey("danh_gia")) {
                Integer rating = Integer.parseInt(reviewData.get("danh_gia").toString());
                existingReview.setDanhGia(rating);
            }

            if (reviewData.containsKey("binh_luan")) {
                String comment = (String) reviewData.get("binh_luan");
                existingReview.setNoiDungBinhLuan(comment);
            }

            if (reviewData.containsKey("da_chinh_sua")) {
                Object value = reviewData.get("da_chinh_sua");
                Boolean editStatus;

                if (value instanceof Boolean) {
                    editStatus = (Boolean) value;
                } else if (value instanceof Number) {
                    editStatus = ((Number) value).intValue() > 0;
                } else if (value instanceof String) {
                    editStatus = "1".equals(value) || "true".equalsIgnoreCase((String) value);
                } else {
                    editStatus = false;
                }

                existingReview.setChinhSua(editStatus);
            }

            existingReview.setNgaySua(new Date());
            binhLuanRepository.save(existingReview);

            return Map.of(
                    "error", false,
                    "message", "Đã cập nhật đánh giá thành công",
                    "reviewId", reviewId
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "error", true,
                    "message", "Lỗi: " + e.getMessage()
            );
        }
    }

    public Map<String, Object> deleteReview(String reviewId) {
        try {
            Integer idBinhLuan = Integer.parseInt(reviewId);

            boolean exists = binhLuanRepository.existsById(idBinhLuan);

            if (!exists) {
                throw new RuntimeException("Không tìm thấy đánh giá");
            }

            binhLuanRepository.deleteById(idBinhLuan);

            return Map.of(
                    "error", false,
                    "message", "Đã xóa đánh giá thành công",
                    "reviewId", reviewId
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "error", true,
                    "message", "Lỗi: " + e.getMessage()
            );
        }
    }
}
