package com.example.duanbe.repository;

import com.example.duanbe.entity.HoaDon;
import com.example.duanbe.response.ChiTietSanPhamView;
import com.example.duanbe.response.HoaDonResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BCTKRepo extends JpaRepository<HoaDon, Integer> {
    @Query(nativeQuery = true, value = """
            SELECT COALESCE(SUM(hd.tong_tien_sau_giam) - ISNULL(SUM(hd.phi_van_chuyen), 0), 0) AS [Doanh thu] 
            FROM hoa_don hd
            JOIN theo_doi_don_hang tddh ON hd.id_hoa_don = tddh.id_hoa_don
            WHERE tddh.trang_thai = N'Hoàn thành'
            AND CAST(tddh.ngay_chuyen AS DATE) BETWEEN :startDate AND :endDate
            AND tddh.ngay_chuyen = (
                SELECT MAX(t2.ngay_chuyen)
                FROM theo_doi_don_hang t2
                WHERE t2.id_hoa_don = tddh.id_hoa_don
            )
            """)
    BigDecimal getDoanhThu(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(nativeQuery = true, value = """
            SELECT COALESCE(COUNT(hd.id_hoa_don), 0) AS [Đơn hàng] 
            FROM hoa_don hd
            JOIN theo_doi_don_hang tddh ON tddh.id_hoa_don = hd.id_hoa_don
            WHERE tddh.trang_thai = N'Hoàn thành' 
            AND CAST(tddh.ngay_chuyen AS DATE) BETWEEN :startDate AND :endDate
            AND tddh.ngay_chuyen = (
                SELECT MAX(t2.ngay_chuyen)
                FROM theo_doi_don_hang t2
                WHERE t2.id_hoa_don = tddh.id_hoa_don
            )
            """)
    Integer getTongDonHang(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(nativeQuery = true, value = """
            SELECT 
                COALESCE(SUM(hdct.so_luong), 0) AS so_luong_ban_thuc_te
            FROM hoa_don hd
            JOIN hoa_don_chi_tiet hdct ON hd.id_hoa_don = hdct.id_hoa_don
            JOIN theo_doi_don_hang tddh ON hd.id_hoa_don = tddh.id_hoa_don
            WHERE tddh.trang_thai = N'Hoàn thành'
              AND CAST(tddh.ngay_chuyen AS DATE) BETWEEN :startDate AND :endDate
              AND tddh.ngay_chuyen = (
                  SELECT MAX(t2.ngay_chuyen)
                  FROM theo_doi_don_hang t2
                  WHERE t2.id_hoa_don = tddh.id_hoa_don
              )
            """)
    Integer getTongSanPham(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query(nativeQuery = true, value = """
            SELECT TOP 5 
                hdct.id_chi_tiet_san_pham,
                sp.ma_san_pham, 
                (sp.ten_san_pham + N': Màu: ' + ms.ten_mau_sac + N', size: ' + kt.gia_tri) AS ten_san_pham, 
                SUM(hdct.so_luong) AS so_luong, 
                ctsp.gia_ban 
            FROM hoa_don hd
            JOIN hoa_don_chi_tiet hdct ON hdct.id_hoa_don = hd.id_hoa_don
            JOIN chi_tiet_san_pham ctsp ON ctsp.id_chi_tiet_san_pham = hdct.id_chi_tiet_san_pham
            JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
            JOIN theo_doi_don_hang tddh ON tddh.id_hoa_don = hd.id_hoa_don
            JOIN mau_sac ms ON ms.id_mau_sac = ctsp.id_mau_sac
            JOIN kich_thuoc kt ON kt.id_kich_thuoc = ctsp.id_kich_thuoc
            WHERE tddh.trang_thai = N'Hoàn thành' 
            AND CAST(tddh.ngay_chuyen AS DATE) BETWEEN :startDate AND :endDate
            AND tddh.ngay_chuyen = (
                SELECT MAX(t2.ngay_chuyen)
                FROM theo_doi_don_hang t2
                WHERE t2.id_hoa_don = tddh.id_hoa_don
            )
            GROUP BY sp.ma_san_pham, sp.ten_san_pham, ctsp.gia_ban, hdct.id_chi_tiet_san_pham, ms.ten_mau_sac, kt.gia_tri
            ORDER BY so_luong DESC
            """)
    List<HoaDonResponse> topSanPhamBanChay(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(nativeQuery = true, value = """
            select ctsp.id_chi_tiet_san_pham ,sp.ma_san_pham, (sp.ten_san_pham+N': Màu: '+ms.ten_mau_sac+N', size: '+kt.gia_tri) as ten_san_pham, 
            ctsp.so_luong, ctsp.gia_ban from san_pham sp
            join chi_tiet_san_pham ctsp on ctsp.id_san_pham = sp.id_san_pham
            join mau_sac ms on ms.id_mau_sac = ctsp.id_mau_sac
            join kich_thuoc kt on kt.id_kich_thuoc = ctsp.id_kich_thuoc
            where ctsp.so_luong < 10
            order by so_luong asc
                        """)
    List<ChiTietSanPhamView> topSanPhamSapHetHang();

    @Query(nativeQuery = true, value = """
            SELECT 
                tddh.trang_thai AS trangThaiDonHang,
                COUNT(*) AS [Số lượng đơn hàng],
                CAST(COUNT(*) AS FLOAT) / NULLIF((
                    SELECT COUNT(DISTINCT hd2.id_hoa_don) 
                    FROM hoa_don hd2
                    JOIN theo_doi_don_hang tddh2 ON tddh2.id_hoa_don = hd2.id_hoa_don
                    WHERE hd2.trang_thai = N'Hoàn thành'
                    AND tddh2.ngay_chuyen = (
                        SELECT MAX(t3.ngay_chuyen)
                        FROM theo_doi_don_hang t3
                        WHERE t3.id_hoa_don = tddh2.id_hoa_don
                    )
                ), 0) * 100 AS tiLeTrangThaiDonHang
            FROM hoa_don hd
            JOIN theo_doi_don_hang tddh ON tddh.id_hoa_don = hd.id_hoa_don
            WHERE tddh.ngay_chuyen = (
                SELECT MAX(t2.ngay_chuyen)
                FROM theo_doi_don_hang t2
                WHERE t2.id_hoa_don = tddh.id_hoa_don
            )
            AND hd.trang_thai = N'Hoàn thành'
            GROUP BY tddh.trang_thai
            """)
    List<HoaDonResponse> tiLeTrangThaiHoaDon();
}
