package com.example.duanbe.entity;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_voucher")
    private Integer id;

    @Column(name = "ma_voucher")
    private String maVoucher;

    @Column(name = "ten_voucher")
    private String tenVoucher;

    @Column(name = "ngay_bat_dau")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_het_han")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayHetHan;

    @Column(name = "gia_tri_giam")
    private BigDecimal giaTriGiam;

    @Column(name = "gia_tri_toi_thieu")
    private BigDecimal giaTriToiThieu;

    @Column(name = "trang_thai")
    private String trangThai;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "kieu_giam_gia")
    private String kieuGiamGia;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "gia_tri_toi_da")
    private BigDecimal giaTriToiDa;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaVoucher() {
        return maVoucher;
    }

    public void setMaVoucher(String maVoucher) {
        this.maVoucher = maVoucher;
    }

    public String getTenVoucher() {
        return tenVoucher;
    }

    public void setTenVoucher(String tenVoucher) {
        this.tenVoucher = tenVoucher;
    }

    public LocalDateTime getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDateTime ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDateTime getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(LocalDateTime ngayHetHan) {
        this.ngayHetHan = ngayHetHan;
    }

    public BigDecimal getGiaTriGiam() {
        return giaTriGiam;
    }

    public void setGiaTriGiam(BigDecimal giaTriGiam) {
        this.giaTriGiam = giaTriGiam;
    }

    public BigDecimal getGiaTriToiThieu() {
        return giaTriToiThieu;
    }

    public void setGiaTriToiThieu(BigDecimal giaTriToiThieu) {
        this.giaTriToiThieu = giaTriToiThieu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public String getKieuGiamGia() {
        return kieuGiamGia;
    }

    public void setKieuGiamGia(String kieuGiamGia) {
        this.kieuGiamGia = kieuGiamGia;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public BigDecimal getGiaTriToiDa() {
        return giaTriToiDa;
    }

    public void setGiaTriToiDa(BigDecimal giaTriToiDa) {
        this.giaTriToiDa = giaTriToiDa;
    }

    @Override
    public String toString() {
        return "Voucher{" +
                "id=" + id +
                ", maVoucher='" + maVoucher + '\'' +
                ", tenVoucher='" + tenVoucher + '\'' +
                ", ngayBatDau=" + ngayBatDau +
                ", ngayHetHan=" + ngayHetHan +
                ", giaTriGiam=" + giaTriGiam +
                ", giaTriToiThieu=" + giaTriToiThieu +
                ", trangThai='" + trangThai + '\'' +
                ", soLuong=" + soLuong +
                ", kieuGiamGia='" + kieuGiamGia + '\'' +
                ", moTa='" + moTa + '\'' +
                ", giaTriToiDa=" + giaTriToiDa +
                '}';
    }
}