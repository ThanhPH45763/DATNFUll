package com.example.duanbe.request;

import com.example.duanbe.entity.KichThuoc;
import com.example.duanbe.entity.MauSac;
import com.example.duanbe.entity.SanPham;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChiTietSanPhamRequest {
    private Integer id_chi_tiet_san_pham;
    @NotNull(message = "Không để trống sản phẩm")
    private Integer id_san_pham;
    private String qr_code;
    @NotNull(message = "Không để trống giá bán")
    private BigDecimal gia_ban;
    @NotNull(message = "Không để trống số lượng")
    private Integer so_luong;
    @NotBlank(message = "Không để trống trạng thái")
    private String trang_thai;
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @NotNull(message = "Không để trống ngày tạo")
    private Date ngay_tao;
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date ngay_sua;
    @NotNull(message = "Không để trống kích thước")
    private Integer id_kich_thuoc;
    @NotNull(message = "Không để trống màu sắc")
    private Integer id_mau_sac;
    private ArrayList<String> hinh_anh;
}
