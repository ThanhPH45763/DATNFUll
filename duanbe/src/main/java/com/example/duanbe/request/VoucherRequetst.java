package com.example.duanbe.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VoucherRequetst {
    private Integer id;
    private String maVoucher;
    private String tenVoucher;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayHetHan;
    private BigDecimal giaTriGiam;
    private BigDecimal giaTriToiThieu;
    private String trangThai;
    private Integer soLuong;
    private String kieuGiamGia;
    private String moTa;
    private BigDecimal giaTriToiDa;

}
