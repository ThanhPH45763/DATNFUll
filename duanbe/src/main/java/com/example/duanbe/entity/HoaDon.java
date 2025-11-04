package com.example.duanbe.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "hoa_don")
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_hoa_don;

    private String ma_hoa_don;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "id_voucher")
    private Voucher voucher;

    @Column(name = "ngay_tao")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ngay_tao;

    @Column(name = "ngay_sua")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ngay_sua;

    private String email;
    private String sdt;
    private String ghi_chu;

    private String dia_chi;

    @Column(name = "tong_tien_sau_giam")
    private BigDecimal tong_tien_sau_giam;
    @Column(name = "tong_tien_truoc_giam")
    private BigDecimal tong_tien_truoc_giam;
    // Fields added back to match controller logic, assuming ERD is incomplete
    private BigDecimal phi_van_chuyen;
    private String hinh_thuc_thanh_toan;
    private String phuong_thuc_nhan_hang;
    private String loai_hoa_don;
    private String ho_ten;
    private String trang_thai;
    private BigDecimal phu_thu;
    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<TheoDoiDonHang> lichSuTrangThai = new ArrayList<>();

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "hoaDon-chiTiet")
    private List<HoaDonChiTiet> danhSachChiTiet = new ArrayList<>();
}
