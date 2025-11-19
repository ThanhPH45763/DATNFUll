package com.example.duanbe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "binh_luan")
public class BinhLuan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_binh_luan")
    private Integer idBinhLuan;

    @Column(name = "id_khach_hang")
    private Integer idKhachHang;

    @Column(name = "id_chi_tiet_san_pham")
    private Integer idChiTietSanPham;

    @Column(name = "noi_dung_binh_luan", columnDefinition = "nvarchar(MAX)")
    private String noiDungBinhLuan;

    @Column(name = "danh_gia")
    private Integer danhGia;

    @Column(name = "ngay_tao")
    private Date ngayTao;

    @Column(name = "ngay_sua")
    private Date ngaySua;

    @Column(name = "chinh_sua")
    private Boolean chinhSua;
}