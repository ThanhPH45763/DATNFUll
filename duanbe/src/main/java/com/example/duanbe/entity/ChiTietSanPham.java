package com.example.duanbe.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chi_tiet_san_pham")
public class ChiTietSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_chi_tiet_san_pham;
    @ManyToOne
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham;

    @Column(name = "qr_code")
    private String qr_code;

    @Column(name = "gia_ban")
    private BigDecimal gia_ban;

    @Column(name = "so_luong")
    private Integer so_luong;

    @Column(name = "trang_thai")
    private Boolean trang_thai;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date ngay_tao;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date ngay_sua;
    @ManyToOne
    @JoinColumn(name = "id_kich_thuoc")
    KichThuoc kichThuoc;
    @ManyToOne
    @JoinColumn(name = "id_mau_sac")
    MauSac mauSac;

    // ⛔ ĐÃ TẮT: Không tự động thay đổi trạng thái khi số lượng <= 0
    // Trạng thái phải được admin quản lý thủ công
    // @PrePersist
    // @PreUpdate
    // private void checkSoLuong() {
    // if (this.so_luong <= 0) {
    // this.trang_thai = false;
    // }
    // }

    public void inThongTin() {
        System.out.println("ChiTietSanPham{" +
                "id_chi_tiet_san_pham=" + id_chi_tiet_san_pham +
                ", sanPham=" + sanPham +
                ", qr_code='" + qr_code + '\'' +
                ", gia_ban=" + gia_ban +
                ", so_luong=" + so_luong +
                ", trang_thai='" + trang_thai + '\'' +
                ", ngay_tao=" + ngay_tao +
                ", ngay_sua=" + ngay_sua +
                ", kichThuoc=" + kichThuoc +
                ", mauSac=" + mauSac +
                '}');
    }
}
