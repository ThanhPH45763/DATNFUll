package com.example.duanbe.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "chi_tiet_gio_hang")
public class ChiTietGioHang {
    @EmbeddedId
    private ChiTietGioHangId id;

    @ManyToOne
    @MapsId("idGioHang")
    @JoinColumn(name = "id_gio_hang", nullable = false)
//    @JsonIgnore // Ngắt vòng lặp
    @JsonBackReference
    private GioHang gioHang;

    @ManyToOne
    @MapsId("idChiTietSanPham")
    @JoinColumn(name = "id_chi_tiet_san_pham", nullable = false)
    private ChiTietSanPham chiTietSanPham;

    @Column(name = "so_luong")
    private Integer soLuong;

}
