package com.example.duanbe.response;

import com.example.duanbe.entity.KichThuoc;
import com.example.duanbe.entity.MauSac;
import com.example.duanbe.entity.SanPham;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

public interface ChiTietSanPhamView {
    Integer getId_chi_tiet_san_pham();

    String getMa_san_pham();

    String getTen_san_pham();

    String getQr_code();

    Float getGia_ban();

    Integer getSo_luong();

    Boolean getTrang_thai();

    LocalDateTime getNgay_tao();

    LocalDateTime getNgay_sua();

    String getGia_tri();

    String getDon_vi();

    String getTen_mau();

    String getTen_danh_muc();

    String getTen_thuong_hieu();

    String getTen_chat_lieu();

    Boolean getGioi_tinh();

    Integer getId_kich_thuoc();

    Integer getId_mau_sac();

    Float getDanh_gia_trung_binh();

    Integer getSo_luong_danh_gia();

    BigDecimal getGiaGoc();

    BigDecimal getGiaHienTai();

    Float getGiaTriKhuyenMai();

    String getKieuKhuyenMai();

    String getHinh_anh();

    Integer getId_san_pham();

    Integer getId_danh_muc();

    Integer getId_thuong_hieu();

    Integer getId_chat_lieu();

    String getTen_mau_sac();

    String getMo_ta();

    default Integer getSoLuongSafe() {
        return getSo_luong() != null ? getSo_luong() : 0;
    }
}
