package com.example.duanbe.response;
import java.math.BigDecimal;
import java.util.Date;

public interface SanPhamView {
    Integer getId_san_pham();

    String getMa_san_pham();

    String getTen_san_pham();

    String getMo_ta();

    Boolean getTrang_thai();

    String getTen_danh_muc();

    String getTen_thuong_hieu();

    String getTen_chat_lieu();

    Integer getTong_so_luong();

    String getHinh_anh();

    Date getNgay_sua_moi();

    Float getDanh_gia();

    Float getSo_luong_danh_gia();

    BigDecimal getGia_max();

    BigDecimal getGia_min();

    BigDecimal getGia_tot_nhat();

    BigDecimal getGia_khuyen_mai_cao_nhat();

    default Integer getTongSoLuongSafe() {
        return getTong_so_luong() != null ? getTong_so_luong() : 0;
    }
}
