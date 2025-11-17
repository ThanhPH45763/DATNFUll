package com.example.duanbe.dto;

import com.example.duanbe.response.SanPhamView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamDisplayDTO {
    private Integer id_san_pham;
    private String ma_san_pham;
    private String ten_san_pham;
    private String mo_ta;
    private Boolean trang_thai;
    private String ten_danh_muc;
    private String ten_thuong_hieu;
    private String ten_chat_lieu;
    private String hinh_anh;
    private Integer tong_so_luong;
    private Float danh_gia;
    private Float so_luong_danh_gia;
    
    // Giá hiển thị
    private BigDecimal gia_hien_thi;           // Giá chính hiển thị (ưu tiên giá khuyến mãi)
    private BigDecimal gia_goc;                // Giá gốc (để gạch ngang)
    private Boolean co_khuyen_mai;             // Có khuyến mãi không
    private Integer phan_tram_giam;            // % giảm giá (để hiển thị badge)
    private String khoang_gia;                 // VD: "500.000₫ - 800.000₫"
    
    public static SanPhamDisplayDTO fromView(SanPhamView view) {
        SanPhamDisplayDTO dto = new SanPhamDisplayDTO();
        dto.setId_san_pham(view.getId_san_pham());
        dto.setMa_san_pham(view.getMa_san_pham());
        dto.setTen_san_pham(view.getTen_san_pham());
        dto.setMo_ta(view.getMo_ta());
        dto.setTrang_thai(view.getTrang_thai());
        dto.setTen_danh_muc(view.getTen_danh_muc());
        dto.setTen_thuong_hieu(view.getTen_thuong_hieu());
        dto.setTen_chat_lieu(view.getTen_chat_lieu());
        dto.setHinh_anh(view.getHinh_anh());
        dto.setTong_so_luong(view.getTong_so_luong());
        dto.setDanh_gia(view.getDanh_gia());
        dto.setSo_luong_danh_gia(view.getSo_luong_danh_gia());
        
        BigDecimal giaMin = view.getGia_min();
        BigDecimal giaMax = view.getGia_max();
        BigDecimal giaTotNhat = view.getGia_tot_nhat();
        BigDecimal giaKhuyenMaiCaoNhat = view.getGia_khuyen_mai_cao_nhat();
        
        // Kiểm tra có khuyến mãi không
        boolean coKhuyenMai = giaTotNhat != null && giaMin != null && 
                              giaTotNhat.compareTo(giaMin) < 0;
        
        dto.setCo_khuyen_mai(coKhuyenMai);
        
        if (coKhuyenMai) {
            // Có khuyến mãi: hiển thị giá khuyến mãi
            dto.setGia_hien_thi(giaTotNhat);
            dto.setGia_goc(giaMin);
            
            // Tính % giảm
            if (giaMin.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal phanTram = giaMin.subtract(giaTotNhat)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(giaMin, 0, RoundingMode.HALF_UP);
                dto.setPhan_tram_giam(phanTram.intValue());
            }
            
            // Khoảng giá sau khuyến mãi (nếu có nhiều biến thể)
            if (giaKhuyenMaiCaoNhat != null && giaTotNhat.compareTo(giaKhuyenMaiCaoNhat) != 0) {
                dto.setKhoang_gia(formatPrice(giaTotNhat) + " - " + formatPrice(giaKhuyenMaiCaoNhat));
            } else {
                dto.setKhoang_gia(formatPrice(giaTotNhat));
            }
        } else {
            // Không có khuyến mãi: hiển thị giá gốc
            dto.setGia_hien_thi(giaMin);
            dto.setGia_goc(null);
            dto.setPhan_tram_giam(0);
            
            // Khoảng giá gốc (nếu có nhiều biến thể)
            if (giaMax != null && giaMin != null && giaMin.compareTo(giaMax) != 0) {
                dto.setKhoang_gia(formatPrice(giaMin) + " - " + formatPrice(giaMax));
            } else {
                dto.setKhoang_gia(formatPrice(giaMin));
            }
        }
        
        return dto;
    }
    
    private static String formatPrice(BigDecimal price) {
        if (price == null) return "0₫";
        return String.format("%,.0f₫", price);
    }
}
