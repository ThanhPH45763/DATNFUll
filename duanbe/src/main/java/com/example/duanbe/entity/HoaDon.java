package com.example.duanbe.entity;

import com.example.duanbe.config.TimezoneConfig;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Hoa Don entity with proper JavaBeans getters and setters
 */
@Entity
@Table(name = "hoa_don")
public class HoaDon {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id_hoa_don;

  @Column(name = "ma_hoa_don", nullable = false)
  private String ma_hoa_don;

  @Column(name = "ngay_tao")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime ngay_tao;

  @Column(name = "ngay_sua")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime ngay_sua;

  @Column(name = "email", nullable = true)
  private String email;

  @Column(name = "sdt", nullable = true)
  private String sdt;

  @Column(name = "ghi_chu", nullable = true)
  private String ghi_chu;

  @Column(name = "dia_chi", nullable = true)
  private String dia_chi;

  @Column(name = "tong_tien_truoc_giam")
  private BigDecimal tong_tien_truoc_giam;

  @Column(name = "tong_tien_sau_giam")
  private BigDecimal tong_tien_sau_giam;

  @Column(name = "phi_van_chuyen")
  private BigDecimal phi_van_chuyen;

  @Column(name = "hinh_thuc_thanh_toan")
  private String hinh_thuc_thanh_toan;

  @Column(name = "phuong_thuc_nhan_hang")
  private String phuong_thuc_nhan_hang;

  @Column(name = "loai_hoa_don")
  private String loai_hoa_don;

  @Column(name = "trang_thai")
  private String trang_thai;

  @Column(name = "ho_ten")
  private String ho_ten;

  @Column(name = "phu_thu")
  private BigDecimal phu_thu;
  // Relationships
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_khach_hang")
  private KhachHang khachHang;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_voucher")
  private Voucher voucher;

  @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonManagedReference(value = "hoaDon-chiTiet")
  private List<HoaDonChiTiet> hoaDonChiTietList = new ArrayList<>();

  @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonManagedReference(value = "hoaDon-theoDoiDonHang")
  private List<TheoDoiDonHang> theoDoiDonHangList = new ArrayList<>();

  // Constructors
  public HoaDon() {
  }

  public HoaDon(String ma_hoa_don) {
    this.ma_hoa_don = ma_hoa_don;
    // ✅ Use Vietnam timezone explicitly via TimezoneConfig
    this.ngay_tao = LocalDateTime.now(TimezoneConfig.VIETNAM_ZONE);
    this.trang_thai = "Đang chờ";
    this.hinh_thuc_thanh_toan = "Tiền mặt";
    this.phuong_thuc_nhan_hang = "Nhận tại cửa hàng";
    this.loai_hoa_don = "Offline";
    this.ho_ten = "Khách lẻ";
  }

  // Getters and setters following JavaBeans conventions
  public Integer getId_hoa_don() {
    return id_hoa_don;
  }

  public String getMa_hoa_don() {
    return ma_hoa_don;
  }

  public void setMa_hoa_don(String ma_hoa_don) {
    this.ma_hoa_don = ma_hoa_don;
  }

  public LocalDateTime getNgay_tao() {
    return ngay_tao;
  }

  public void setNgay_tao(LocalDateTime ngay_tao) {
    this.ngay_tao = ngay_tao;
  }

  public LocalDateTime getNgay_sua() {
    return ngay_sua;
  }

  public void setNgay_sua(LocalDateTime ngay_sua) {
    this.ngay_sua = ngay_sua;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getSdt() {
    return sdt;
  }

  public void setSdt(String sdt) {
    this.sdt = sdt;
  }

  public String getGhi_chu() {
    return ghi_chu;
  }

  public void setGhi_chu(String ghi_chu) {
    this.ghi_chu = ghi_chu;
  }

  public String getDia_chi() {
    return dia_chi;
  }

  public void setDia_chi(String dia_chi) {
    this.dia_chi = dia_chi;
  }

  public BigDecimal getTong_tien_truoc_giam() {
    return tong_tien_truoc_giam;
  }

  public void setTong_tien_truoc_giam(BigDecimal tong_tien_truoc_giam) {
    this.tong_tien_truoc_giam = tong_tien_truoc_giam;
  }

  public BigDecimal getTong_tien_sau_giam() {
    return tong_tien_sau_giam;
  }

  public void setTong_tien_sau_giam(BigDecimal tong_tien_sau_giam) {
    this.tong_tien_sau_giam = tong_tien_sau_giam;
  }

  public BigDecimal getPhi_van_chuyen() {
    return phi_van_chuyen;
  }

  public void setPhi_van_chuyen(BigDecimal phi_van_chuyen) {
    this.phi_van_chuyen = phi_van_chuyen;
  }

  public String getHinh_thuc_thanh_toan() {
    return hinh_thuc_thanh_toan;
  }

  public void setHinh_thuc_thanh_toan(String hinh_thuc_thanh_toan) {
    this.hinh_thuc_thanh_toan = hinh_thuc_thanh_toan;
  }

  public String getPhuong_thuc_nhan_hang() {
    return phuong_thuc_nhan_hang;
  }

  public void setPhuong_thuc_nhan_hang(String phuong_thuc_nhan_hang) {
    this.phuong_thuc_nhan_hang = phuong_thuc_nhan_hang;
  }

  public String getLoai_hoa_don() {
    return loai_hoa_don;
  }

  public void setLoai_hoa_don(String loai_hoa_don) {
    this.loai_hoa_don = loai_hoa_don;
  }

  public String getTrang_thai() {
    return trang_thai;
  }

  public void setTrang_thai(String trang_thai) {
    this.trang_thai = trang_thai;
  }

  public String getHo_ten() {
    return ho_ten;
  }

  public void setHo_ten(String ho_ten) {
    this.ho_ten = ho_ten;
  }

  public BigDecimal getPhu_thu() {
    return phu_thu;
  }

  public void setPhu_thu(BigDecimal phu_thu) {
    this.phu_thu = phu_thu;
  }

  public KhachHang getKhachHang() {
    return khachHang;
  }

  public void setKhachHang(KhachHang khachHang) {
    this.khachHang = khachHang;
  }

  public Voucher getVoucher() {
    return voucher;
  }

  public void setVoucher(Voucher voucher) {
    this.voucher = voucher;
  }

  public List<HoaDonChiTiet> getHoaDonChiTietList() {
    return hoaDonChiTietList;
  }

  public void setHoaDonChiTietList(List<HoaDonChiTiet> hoaDonChiTietList) {
    this.hoaDonChiTietList = hoaDonChiTietList;
  }

  @Override
  public String toString() {
    return "HoaDon{" +
        "id=" + id_hoa_don +
        ", maHoaDon='" + ma_hoa_don + "'" +
        ", ngayTao=" + ngay_tao +
        ", trangThai='" + trang_thai + "'" +
        ", loaiHoaDon='" + loai_hoa_don + "'" +
        "}";
  }
}
