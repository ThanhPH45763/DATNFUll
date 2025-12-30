package com.example.duanbe.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "chi_tiet_khuyen_mai")
public class ChiTietKhuyenMai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ctkm")
    private Integer id;
    @Column(name = "gia_sau_giam")
    private BigDecimal giaSauGiam;

    @ManyToOne
    @JoinColumn(name = "id_khuyen_mai")
    private KhuyenMai khuyenMai;

    @ManyToOne
    @JoinColumn(name = "id_chi_tiet_san_pham")
    private ChiTietSanPham chiTietSanPham;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getGiaSauGiam() {
        return giaSauGiam;
    }

    public void setGiaSauGiam(BigDecimal giaSauGiam) {
        this.giaSauGiam = giaSauGiam;
    }

    public KhuyenMai getKhuyenMai() {
        return khuyenMai;
    }

    public void setKhuyenMai(KhuyenMai khuyenMai) {
        this.khuyenMai = khuyenMai;
    }

    public ChiTietSanPham getChiTietSanPham() {
        return chiTietSanPham;
    }

    public void setChiTietSanPham(ChiTietSanPham chiTietSanPham) {
        this.chiTietSanPham = chiTietSanPham;
    }

    @Override
    public String toString() {
        return "ChiTietKhuyenMai{" +
                "id=" + id +
                ", giaSauGiam=" + giaSauGiam +
                ", khuyenMai=" + khuyenMai +
                ", chiTietSanPham=" + chiTietSanPham +
                '}';
    }
}