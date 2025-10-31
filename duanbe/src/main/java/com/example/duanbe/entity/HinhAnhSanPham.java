package com.example.duanbe.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "hinh_anh")
public class HinhAnhSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_hinh_anh;
    @ManyToOne
    @JoinColumn(name = "id_chi_tiet_san_pham")
    ChiTietSanPham chiTietSanPham;
    private String hinh_anh;
    private Boolean anh_chinh;

}
