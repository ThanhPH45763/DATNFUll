package com.example.duanbe.repository;

import com.example.duanbe.entity.HoaDon;
import com.example.duanbe.response.ChiTietTraHangResponse;
import com.example.duanbe.response.HoaDonChiTietResponse;
import com.example.duanbe.response.HoaDonResponse;
import com.example.duanbe.response.TheoDoiDonHangResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HoaDonRepo extends JpaRepository<HoaDon, Integer> {

    @Query(value = """
            SELECT DISTINCT hd.ma_hoa_don, hd.ngay_tao, hd.ho_ten, hd.sdt,
                            hd.trang_thai AS trang_thai_thanh_toan, hd.loai_hoa_don,
                            hd.id_dia_chi, v.ma_voucher, hd.tong_tien_sau_giam, tdh.trang_thai,
                            hd.hinh_thuc_thanh_toan, hd.phuong_thuc_nhan_hang
            FROM hoa_don hd
            LEFT JOIN voucher v ON hd.id_voucher = v.id_voucher
            LEFT JOIN (
                SELECT t1.id_hoa_don, t1.trang_thai, t1.ngay_chuyen
                FROM theo_doi_don_hang t1
                WHERE t1.trang_thai != N'Đã cập nhật'
                AND NOT EXISTS (
                    SELECT 1
                    FROM theo_doi_don_hang t2
                    WHERE t2.id_hoa_don = t1.id_hoa_don
                    AND t2.trang_thai != N'Đã cập nhật'
                    AND t2.ngay_chuyen > t1.ngay_chuyen
                )
            ) AS tdh ON hd.id_hoa_don = tdh.id_hoa_don
            WHERE hd.trang_thai = N'Hoàn thành'
            AND (:keyword IS NULL
                OR hd.ma_hoa_don LIKE CONCAT('%', :keyword, '%')
                OR hd.ho_ten LIKE CONCAT('%', :keyword, '%')
                OR hd.sdt LIKE CONCAT('%', :keyword, '%'))
            AND (:tuNgay IS NULL OR hd.ngay_tao >= :tuNgay)
            AND (:denNgay IS NULL OR hd.ngay_tao <= :denNgay)
            AND (:trangThai IS NULL OR tdh.trang_thai = :trangThai)
            AND (:loaiHoaDon IS NULL OR hd.loai_hoa_don = :loaiHoaDon)
            ORDER BY hd.ngay_tao DESC
            """, nativeQuery = true)
    Page<HoaDonResponse> locHoaDon(
            @Param("keyword") String keyword,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay,
            @Param("trangThai") String trangThai,
            @Param("loaiHoaDon") String loaiHoaDon,
            Pageable pageable);

    @Query(value = """
            SELECT hd.id_hoa_don, hd.ma_hoa_don, hd.id_khach_hang, hd.ngay_tao, hd.email, hd.ho_ten, hd.sdt,
            hd.id_dia_chi, v.ma_voucher, hd.tong_tien_truoc_giam, hd.tong_tien_sau_giam,
            hd.hinh_thuc_thanh_toan, hd.phuong_thuc_nhan_hang, hd.id_voucher, hd.phi_van_chuyen, v.mo_ta,
            hd.trang_thai AS trang_thai_thanh_toan, hd.loai_hoa_don, hd.ghi_chu,
            (SELECT TOP 1 trang_thai FROM theo_doi_don_hang t
            WHERE t.id_hoa_don = hd.id_hoa_don
            ORDER BY t.ngay_chuyen DESC) as trang_thai,
            (SELECT TOP 1 ngay_chuyen FROM theo_doi_don_hang t
            WHERE t.id_hoa_don = hd.id_hoa_don
            ORDER BY t.ngay_chuyen DESC) as ngay_chuyen
            FROM hoa_don hd
            LEFT JOIN voucher v ON hd.id_voucher = v.id_voucher
            WHERE hd.ma_hoa_don = :maHoaDon""", nativeQuery = true)
    Optional<HoaDonResponse> findByMaHoaDon(@Param("maHoaDon") String maHoaDon);

    @Query(value = """
                INSERT INTO theo_doi_don_hang (id_hoa_don, trang_thai, ngay_chuyen, nhan_vien_doi, noi_dung_doi)
                SELECT id_hoa_don, :newTrangThai, :ngayChuyen, :nhanVienDoi, :noiDungDoi
                FROM hoa_don
                WHERE ma_hoa_don = :maHoaDon
            """, nativeQuery = true)
    @Modifying
    @Transactional
    void insertTrangThaiDonHang(@Param("maHoaDon") String maHoaDon,
                                @Param("newTrangThai") String newTrangThai,
                                @Param("ngayChuyen") LocalDateTime ngayChuyen,
                                @Param("nhanVienDoi") String nhanVienDoi,
                                @Param("noiDungDoi") String noiDungDoi);

    // Lấy trạng thái mới nhất
    @Query(value = """
                SELECT trang_thai, ngay_chuyen, nhan_vien_doi, noi_dung_doi
                FROM theo_doi_don_hang
                WHERE id_hoa_don = :idHoaDon
                ORDER BY ngay_chuyen ASC
            """, nativeQuery = true)
    List<TheoDoiDonHangResponse> findTrangThaiHistoryByIdHoaDon(@Param("idHoaDon") Integer idHoaDon);

    @Query(value = """
            select id_hoa_don, ma_hoa_don, hd.id_khach_hang, kh.ho_ten as ten_khach_hang, hd.trang_thai,
            hd.id_voucher, vc.ten_voucher, sdt, id_dia_chi, hd.email, tong_tien_truoc_giam, phi_van_chuyen, ho_ten,
            tong_tien_sau_giam, hinh_thuc_thanh_toan, phuong_thuc_nhan_hang, loai_hoa_don, ghi_chu, hd.ngay_tao
            from hoa_don hd
            LEFT join khach_hang kh on kh.id_khach_hang = hd.id_khach_hang
            LEFT join voucher vc on vc.id_voucher = hd.id_voucher
            where hd.trang_thai = N'Đang chờ' and hd.loai_hoa_don like N'Offline'
            """, nativeQuery = true)
    List<HoaDonResponse> getAllHoaDonCTT();

    @Query(value = """
                        SELECT id_hoa_don,ma_hoa_don,kh.id_khach_hang,kh.ho_ten as ten_khach_hang,hd.ngay_tao,hd.ngay_sua,hd.trang_thai
                        ,vc.id_voucher,vc.ten_voucher,sdt,id_dia_chi,hd.email,tong_tien_truoc_giam,phi_van_chuyen,ho_ten
                        ,tong_tien_sau_giam,hinh_thuc_thanh_toan,phuong_thuc_nhan_hang
                        from hoa_don hd
                         LEFT JOIN khach_hang kh ON hd.id_khach_hang = kh.id_khach_hang
                         LEFT JOIN voucher vc ON hd.id_voucher = vc.id_voucher
                        where hd.id_hoa_don = :idHd
            """, nativeQuery = true)
    List<HoaDonResponse> findHoaDonById(@Param("idHd") Integer idHd);

    @Query(value = """
                SELECT * FROM hoa_don
            """, nativeQuery = true)
    List<HoaDonResponse> getListHD();

    @Query(value = """
            SELECT TOP 1 trang_thai
            FROM theo_doi_don_hang
            WHERE id_hoa_don = :idHoaDon
              AND trang_thai != N'Đã cập nhật'
            ORDER BY ngay_chuyen DESC
            """, nativeQuery = true)
    String findLatestNonUpdatedStatusByIdHoaDon(@Param("idHoaDon") Integer idHoaDon);

    @Query(value = """
        SELECT DISTINCT hd.ma_hoa_don, hd.ngay_tao, tdh.trang_thai, hd.tong_tien_sau_giam
                    FROM hoa_don hd
                    LEFT JOIN (SELECT t.id_hoa_don, t.trang_thai
                                FROM theo_doi_don_hang t
                                WHERE t.ngay_chuyen = (SELECT MAX(ngay_chuyen)
                                                        FROM theo_doi_don_hang t2
                                                        WHERE t2.id_hoa_don = t.id_hoa_don
                                                        )
                            ) tdh ON hd.id_hoa_don = tdh.id_hoa_don
                    WHERE hd.trang_thai = N'Hoàn thành'
        			AND hd.id_khach_hang = :idKH
                    ORDER BY hd.ngay_tao DESC""", nativeQuery = true)
    Page<HoaDonResponse> getAllHDByidKH(@Param("idKH") Integer idKH, Pageable pageable);

    @Query(value = """
        SELECT DISTINCT hd.ma_hoa_don, hd.ngay_tao, tdh.trang_thai, hd.tong_tien_sau_giam
                    FROM hoa_don hd
                    LEFT JOIN (SELECT t.id_hoa_don, t.trang_thai
                                FROM theo_doi_don_hang t
                                WHERE t.ngay_chuyen = (SELECT MAX(ngay_chuyen)
                                                        FROM theo_doi_don_hang t2
                                                        WHERE t2.id_hoa_don = t.id_hoa_don
                                                        )
                            ) tdh ON hd.id_hoa_don = tdh.id_hoa_don
                    WHERE hd.trang_thai = N'Hoàn thành'
                    AND tdh.trang_thai = :trangThai
        			AND hd.id_khach_hang = :idKH
                    ORDER BY hd.ngay_tao DESC""", nativeQuery = true)
    Page<HoaDonResponse> getAllHDByidKHandTT(@Param("idKH") Integer idKH, @Param("trangThai") String trangThai, Pageable pageable);

    //Nghía
    @Query(nativeQuery = true, value = """
            select ctsp.id_chi_tiet_san_pham, sp.hinh_anh, sp.ten_san_pham,\s
                              ms.ten_mau_sac, kt.gia_tri, hdct.don_gia,\s
                              kt.don_vi, hdct.so_luong
                              from hoa_don hd
                              join hoa_don_chi_tiet hdct on hdct.id_hoa_don = hd.id_hoa_don
                              join chi_tiet_san_pham ctsp on ctsp.id_chi_tiet_san_pham = hdct.id_chi_tiet_san_pham
                              join san_pham sp on sp.id_san_pham = ctsp.id_san_pham
                              join mau_sac ms on ms.id_mau_sac = ctsp.id_mau_sac
                              join kich_thuoc kt on kt.id_kich_thuoc = ctsp.id_kich_thuoc
                             left join voucher vc on vc.id_voucher = hd.id_voucher
                              where ma_hoa_don = :maHoaDon
                        """)
    List<HoaDonChiTietResponse> listThongTinHoaDon(@Param("maHoaDon") String maHoaDon);

    @Query(nativeQuery = true, value = """
            select tddh.trang_thai, tddh.ngay_chuyen, hd.ngay_tao, ma_hoa_don from theo_doi_don_hang tddh
            join hoa_don hd on hd.id_hoa_don = tddh.id_hoa_don
            where hd.ma_hoa_don = :maHoaDon
            """)
    List<HoaDonChiTietResponse> listTrangThaiTimeLineBanHangWeb(@Param("maHoaDon") String maHoaDon);

    @Query(nativeQuery = true, value = """
            select ho_ten, id_dia_chi, sdt, ma_hoa_don from hoa_don
            where ma_hoa_don = :maHoaDon
            """)
    List<HoaDonChiTietResponse> listThongTinKhachHang(@Param("maHoaDon") String maHoaDon);

    @Query(nativeQuery = true, value = """
            select id_hoa_don, ma_hoa_don, hoa_don.ngay_tao, hoa_don.ngay_sua,
            hoa_don.trang_thai, hoa_don.id_voucher, sdt, id_dia_chi,
            email, tong_tien_truoc_giam, tong_tien_sau_giam, hinh_thuc_thanh_toan,
            phuong_thuc_nhan_hang,loai_hoa_don, ghi_chu, ten_voucher, ma_voucher,
            gia_tri_giam, kieu_giam_gia, ho_ten, phi_van_chuyen
            from hoa_don
            left join voucher vc on vc.id_voucher = hoa_don.id_voucher
            where ma_hoa_don = :maHoaDon
                                    """)
    HoaDonResponse getHoaDonByMaHoaDon(@Param("maHoaDon") String maHoaDon);

    @Query(value = """
            SELECT h.* FROM hoa_don h WHERE h.trang_thai like N'Đang chờ' AND h.ngay_tao < :startOfToday
            """, nativeQuery = true)
    List<HoaDon> findExpiredChoHoaDons(@Param("startOfToday") LocalDateTime startOfToday);

    //lenh
    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.khachHang.idKhachHang = :idKhachHang")
    int countByKhachHangId(@Param("idKhachHang") Integer idKhachHang);
    List<HoaDon> findByKhachHang_IdKhachHang(Integer idKhachHang);

    // Phương thức mới cho trả hàng

    @Query("SELECT h FROM HoaDon h WHERE h.ma_hoa_don = :maHoaDon")
    Optional<HoaDon> TimMaHoaDon(@Param("maHoaDon") String maHoaDon);

    @Query(value = """
         SELECT
             hd.id_hoa_don AS id_hoa_don,
             hd.ma_hoa_don,
             hd.ngay_tao,
             hd.ngay_sua,
             hd.trang_thai,
             hd.sdt,
             hd.id_dia_chi,
             v.ma_voucher,
             hd.email,
             hd.tong_tien_truoc_giam,
             hd.phi_van_chuyen,
             hd.ho_ten,
             COALESCE(hd.tong_tien_sau_giam, hd.tong_tien_truoc_giam + hd.phi_van_chuyen) AS tong_tien_sau_giam,
             hd.hinh_thuc_thanh_toan,
             hd.phuong_thuc_nhan_hang,
             hd.id_khach_hang,
             kh.ho_ten as ten_khach_hang,
             v.id_voucher,
             v.ten_voucher,
             hd.ghi_chu,
             hd.loai_hoa_don,
             v.gia_tri_giam,
             v.kieu_giam_gia,
             (SELECT TOP 1 tdh.trang_thai
              FROM theo_doi_don_hang tdh
              WHERE tdh.id_hoa_don = hd.id_hoa_don
              ORDER BY tdh.ngay_chuyen DESC) AS trang_thai_don_hang,
             (SELECT TOP 1 th.trang_thai
              FROM tra_hang th
              WHERE th.id_hoa_don = hd.id_hoa_don
              ORDER BY th.ngay_tao DESC) AS trang_thai_tra_hang
         FROM hoa_don hd
         LEFT JOIN khach_hang kh ON kh.id_khach_hang = hd.id_khach_hang
         LEFT JOIN voucher v ON v.id_voucher = hd.id_voucher
         WHERE hd.ma_hoa_don =:maHoaDon
            """, nativeQuery = true)
    HoaDonResponse getHoaDonWithReturnInfoByMaHoaDon(@Param("maHoaDon") String maHoaDon);

    @Query(value = """
            SELECT
                hdct.id_chi_tiet_san_pham,
                sp.ten_san_pham,
                hdct.so_luong,
                COALESCE(ctth.so_luong, 0) AS so_luong_da_tra,
                COALESCE(th.trang_thai, 'Chưa yêu cầu') AS trang_thai_tra_hang,
                th.ly_do AS ly_do_tra_hang,
                sp.hinh_anh,
                kt.gia_tri AS kich_thuoc,
                ms.ten_mau_sac,
                hdct.don_gia
            FROM hoa_don_chi_tiet hdct
            JOIN chi_tiet_san_pham ctsp ON ctsp.id_chi_tiet_san_pham = hdct.id_chi_tiet_san_pham
            JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
            JOIN mau_sac ms ON ms.id_mau_sac = ctsp.id_mau_sac
            JOIN kich_thuoc kt ON kt.id_kich_thuoc = ctsp.id_kich_thuoc
            JOIN hoa_don hd ON hd.id_hoa_don = hdct.id_hoa_don
            LEFT JOIN tra_hang th ON th.id_hoa_don = hd.id_hoa_don
            LEFT JOIN chi_tiet_tra_hang ctth ON ctth.id_tra_hang = th.id AND ctth.id_chi_tiet_san_pham = hdct.id_chi_tiet_san_pham
            WHERE hd.ma_hoa_don = :maHoaDon
            """, nativeQuery = true)
    List<HoaDonChiTietResponse> getChiTietHoaDonByMaHoaDon(@Param("maHoaDon") String maHoaDon);

    @Query(value = """
            SELECT
                hd.ho_ten,
                hd.id_dia_chi,
                hd.sdt,
                COALESCE(th.trang_thai, 'Chưa yêu cầu') AS trang_thai_tra_hang,
                th.ly_do AS ly_do_tra_hang,
                kh.ho_ten as ten_khach_hang
            FROM hoa_don hd
            LEFT JOIN khach_hang kh ON kh.id_khach_hang = hd.id_khach_hang
            LEFT JOIN tra_hang th ON th.id_hoa_don = hd.id_hoa_don
            WHERE hd.ma_hoa_don = :maHoaDon
            """, nativeQuery = true)
    HoaDonChiTietResponse getKhachHangInfoByMaHoaDon(@Param("maHoaDon") String maHoaDon);


    // lềnh thêm và sửa
    @Query(value = """
            SELECT DISTINCT hd.id_hoa_don, hd.ma_hoa_don, hd.ngay_tao, hd.ho_ten, hd.email, hd.sdt, 
                            hd.trang_thai AS trang_thai_thanh_toan, hd.loai_hoa_don, hd.id_dia_chi, hd.ghi_chu,
                            v.ma_voucher, hd.tong_tien_sau_giam, tdh.trang_thai,
                            hd.hinh_thuc_thanh_toan, hd.phuong_thuc_nhan_hang
            FROM hoa_don hd
            LEFT JOIN voucher v ON hd.id_voucher = v.id_voucher
            LEFT JOIN (SELECT t.id_hoa_don, t.trang_thai
                        FROM theo_doi_don_hang t
                        WHERE t.ngay_chuyen = (SELECT MAX(ngay_chuyen)
                                                FROM theo_doi_don_hang t2
                                                WHERE t2.id_hoa_don = t.id_hoa_don
                                                )
                        ) tdh ON hd.id_hoa_don = tdh.id_hoa_don
            WHERE hd.id_khach_hang = :idKhachHang
            ORDER BY hd.ngay_tao DESC
            """, nativeQuery = true)
    List<HoaDonResponse> findHoaDonWithLatestStatusByKhachHangId(@Param("idKhachHang") Integer idKhachHang);

    @Query("SELECT h FROM HoaDon h WHERE h.ma_hoa_don = :maHoaDon AND h.khachHang.idKhachHang = :idKhachHang")
    Optional<HoaDon> findByMaHoaDonAndIdKhachHang(@Param("maHoaDon") String maHoaDon, @Param("idKhachHang") Integer idKhachHang);

    @Query(value = "SELECT TOP 1 trang_thai FROM theo_doi_don_hang WHERE id_hoa_don = :idHoaDon ORDER BY ngay_chuyen DESC", nativeQuery = true)
    String findLatestStatusByIdHoaDon(@Param("idHoaDon") Integer idHoaDon);

}
