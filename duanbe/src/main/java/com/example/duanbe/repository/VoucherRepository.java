package com.example.duanbe.repository;

import com.example.duanbe.entity.Voucher;
import com.example.duanbe.response.VoucherBHResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    boolean existsByMaVoucher(String maVoucher);

    Optional<Voucher> findByMaVoucher(String maVoucher);

    Page<Voucher> findByTrangThai(String trangThai, Pageable pageable);

    Page<Voucher> findByKieuGiamGia(String kieuGiamGia, Pageable pageable);

    @Query("SELECT v FROM Voucher v WHERE v.maVoucher LIKE %:keyword% OR v.tenVoucher LIKE %:keyword%")
    Page<Voucher> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT v FROM Voucher v WHERE v.ngayBatDau >= :startDate AND v.ngayHetHan <= :endDate")
    Page<Voucher> searchByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate, Pageable pageable);

    Page<Voucher> findByNgayBatDauGreaterThanEqual(LocalDateTime startDate, Pageable pageable);

    Page<Voucher> findByNgayHetHanLessThanEqual(LocalDateTime endDate, Pageable pageable);

    @Query("SELECT v FROM Voucher v WHERE v.giaTriToiDa BETWEEN :minPrice AND :maxPrice")
    Page<Voucher> searchByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice,
                                     Pageable pageable);

    @Query("SELECT COALESCE(MIN(v.giaTriToiDa), 0) FROM Voucher v")
    BigDecimal findMinPrice();

    @Query("SELECT COALESCE(MAX(v.giaTriToiDa), 0) FROM Voucher v")
    BigDecimal findMaxPrice();

    @Query(value = """
            SELECT
                hd.id_hoa_don,
                v.id_voucher,
                CASE
                    WHEN v.kieu_giam_gia = N'Phần trăm' THEN
                        CASE
                            WHEN (hd.tong_tien_truoc_giam * v.gia_tri_giam / 100) >= COALESCE(v.gia_tri_toi_da, hd.tong_tien_truoc_giam)
                            THEN COALESCE(v.gia_tri_toi_da, hd.tong_tien_truoc_giam)
                            ELSE (hd.tong_tien_truoc_giam * v.gia_tri_giam / 100)
                        END
                    WHEN v.kieu_giam_gia = N'Tiền mặt' THEN
                        CASE
                            WHEN hd.tong_tien_truoc_giam > COALESCE(v.gia_tri_giam, 0)
                            THEN COALESCE(v.gia_tri_giam, 0)
                            ELSE 0
                        END
                    ELSE 0
                END AS gia_tri_giam_thuc_te,
                v.ten_voucher
            FROM hoa_don hd
            CROSS JOIN voucher v
            WHERE v.trang_thai = N'Đang diễn ra'
            AND hd.trang_thai = N'Đang chờ'
            AND v.gia_tri_toi_thieu <= hd.tong_tien_truoc_giam
            AND hd.id_hoa_don = :idHD
            ORDER BY gia_tri_giam_thuc_te DESC
            """, nativeQuery = true)
    List<VoucherBHResponse> giaTriGiamThucTeByIDHD(@RequestParam("idHD") Integer idHD);

    @Query(nativeQuery = true, value = """
                    SELECT\s
                    		vc.*,
                    		:giaTruyen AS gia_truyen_vao,
                    		CASE\s
                    			WHEN kieu_giam_gia = N'Tiền mặt' THEN gia_tri_giam
                    			WHEN kieu_giam_gia = N'Phần trăm' THEN\s
                    				CASE\s
                    					WHEN (:giaTruyen * gia_tri_giam / 100) > gia_tri_toi_da THEN gia_tri_toi_da
                    					ELSE (:giaTruyen * gia_tri_giam / 100)
                    				END
                    		END AS so_tien_giam,
                    		CASE\s
                    			WHEN kieu_giam_gia = N'Tiền mặt' THEN :giaTruyen - gia_tri_giam
                    			WHEN kieu_giam_gia = N'Phần trăm' THEN\s
                    				CASE\s
                    					WHEN (:giaTruyen * gia_tri_giam / 100) > gia_tri_toi_da THEN :giaTruyen - gia_tri_toi_da
                    					ELSE :giaTruyen - (:giaTruyen * gia_tri_giam / 100)
                    				END
                    		END AS gia_sau_giam
                    	FROM\s
                    		voucher vc
                    	WHERE\s
                    		vc.trang_thai = N'Đang diễn ra'\s
                    		AND :giaTruyen >= gia_tri_toi_thieu;
            """)
    List<VoucherBHResponse> listVoucherHopLeTheoGia(@Param("giaTruyen") BigDecimal giaTruyen);
}