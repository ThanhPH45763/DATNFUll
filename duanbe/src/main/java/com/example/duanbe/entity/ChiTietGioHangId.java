package com.example.duanbe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietGioHangId implements Serializable {
    @Column(name = "id_gio_hang")
    private Integer idGioHang;

    @Column(name = "id_chi_tiet_san_pham")
    private Integer idChiTietSanPham;
}
