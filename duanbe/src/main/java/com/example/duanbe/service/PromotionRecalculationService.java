package com.example.duanbe.service;

import com.example.duanbe.entity.ChiTietKhuyenMai;
import com.example.duanbe.entity.ChiTietSanPham;
import com.example.duanbe.entity.KhuyenMai;
import com.example.duanbe.repository.ChiTietKhuyenMaiRepo;
import com.example.duanbe.repository.ChiTietSanPhamRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PromotionRecalculationService {

    @Autowired
    private ChiTietKhuyenMaiRepo ctkmRepo;

    @Autowired
    private ChiTietSanPhamRepo ctspRepo;

    /**
     * Tính lại giá sau giảm cho TẤT CẢ khuyến mãi của 1 sản phẩm
     * Tự động được gọi khi sửa giá sản phẩm
     * 
     * @param idChiTietSanPham ID sản phẩm vừa thay đổi giá
     */
    @Transactional
    public void recalculatePromotionPrices(Integer idChiTietSanPham) {
        try {
            // Lấy giá hiện tại của sản phẩm
            ChiTietSanPham ctsp = ctspRepo.findById(idChiTietSanPham).orElse(null);
            if (ctsp == null) {
                System.out.println("⚠️ Không tìm thấy CTSP #" + idChiTietSanPham);
                return;
            }

            BigDecimal giaBan = ctsp.getGia_ban();
            if (giaBan == null || giaBan.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("⚠️ Giá bán không hợp lệ cho CTSP #" + idChiTietSanPham);
                return;
            }

            // Lấy TẤT CẢ khuyến mãi đang áp dụng cho sản phẩm này
            List<ChiTietKhuyenMai> danhSachKM = ctkmRepo.findAllByChiTietSanPhamId(idChiTietSanPham);

            if (danhSachKM.isEmpty()) {
                System.out.println("ℹ️ CTSP #" + idChiTietSanPham + " không có khuyến mãi");
                return;
            }

            System.out.println("🔄 Bắt đầu tính lại " + danhSachKM.size() + " khuyến mãi cho CTSP #"
                    + idChiTietSanPham + " (Giá: " + giaBan + ")");

            for (ChiTietKhuyenMai ctkm : danhSachKM) {
                KhuyenMai km = ctkm.getKhuyenMai();

                // Chỉ tính lại cho KM đang diễn ra
                if (km == null || !"Đang diễn ra".equals(km.getTrangThai())) {
                    continue;
                }

                BigDecimal giaSauGiam = calculateDiscountedPrice(giaBan, km);

                // Cập nhật lại giá sau giảm
                ctkm.setGiaSauGiam(giaSauGiam);
                ctkmRepo.save(ctkm);

                System.out.println(String.format(
                        "✅ Cập nhật CTKM #%d: %s | %s | Giá gốc: %s → Giá sau giảm: %s",
                        ctkm.getId(),
                        km.getTenKhuyenMai(),
                        km.getKieuGiamGia(),
                        giaBan,
                        giaSauGiam));
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tính lại giá khuyến mãi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tính giá sau khi áp dụng khuyến mãi
     * 
     * @param giaBan Giá gốc
     * @param km     Khuyến mãi
     * @return Giá sau giảm
     */
    private BigDecimal calculateDiscountedPrice(BigDecimal giaBan, KhuyenMai km) {
        BigDecimal giaSauGiam;

        if ("Phần trăm".equals(km.getKieuGiamGia())) {
            // ✅ Case 1: Giảm % với giới hạn tối đa
            BigDecimal giaTriGiam = km.getGiaTriGiam(); // % giảm (vd: 20)
            BigDecimal giaTriToiDa = km.getGiaTriToiDa(); // Số tiền giảm tối đa (vd: 150000)

            // Tính số tiền giảm = giaBan * % / 100
            BigDecimal soTienGiam = giaBan
                    .multiply(giaTriGiam)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            // Áp dụng giới hạn tối đa nếu có
            if (giaTriToiDa != null && soTienGiam.compareTo(giaTriToiDa) > 0) {
                soTienGiam = giaTriToiDa;
            }

            giaSauGiam = giaBan.subtract(soTienGiam);

        } else if ("Tiền mặt".equals(km.getKieuGiamGia())) {
            // ✅ Case 2: Giảm thẳng số tiền
            giaSauGiam = giaBan.subtract(km.getGiaTriGiam());

        } else {
            // Unknown type, keep original price
            giaSauGiam = giaBan;
        }

        // ❌ Không cho giá âm
        if (giaSauGiam.compareTo(BigDecimal.ZERO) < 0) {
            giaSauGiam = BigDecimal.ZERO;
        }

        return giaSauGiam;
    }

    /**
     * Tính lại cho nhiều sản phẩm cùng lúc
     * 
     * @param listIdCTSP Danh sách ID sản phẩm
     */
    public void recalculateMultipleProducts(List<Integer> listIdCTSP) {
        for (Integer id : listIdCTSP) {
            recalculatePromotionPrices(id);
        }
    }
}
