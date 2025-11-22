package com.example.duanbe.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "khach_hang")
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_khach_hang")
    private Integer idKhachHang;

    @Column(name = "ma_khach_hang")
    private String maKhachHang;

    @Column(name = "ten_dang_nhap")
    private String tenDangNhap;

    @Column(name = "mat_khau")
    private String matKhau;

    @Column(name = "ho_ten")
   
    @Size(max = 50, message = "Tên khách hàng không được vượt quá 50 ký tự")
    private String hoTen;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "ngay_sinh")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    private Date ngaySinh;

    @Column(name = "email")
    private String email;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "trang_thai")
    private String trangThai;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "ngay_lap")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ngayLap;
}
