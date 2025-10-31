package com.example.duanbe.repository;

import com.example.duanbe.entity.ChiTietSanPham;
import com.example.duanbe.response.ChiTietSanPhamView;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ChiTietSanPhamRepo
        extends JpaRepository<ChiTietSanPham, Integer>, JpaSpecificationExecutor<ChiTietSanPhamView> {
    @Query("SELECT c FROM ChiTietSanPham c WHERE c.sanPham.id_san_pham = :idSanPham")
    List<ChiTietSanPham> findBySanPhamIdSanPham(@Param("idSanPham") Integer idSanPham);

    @Query("SELECT c FROM ChiTietSanPham c WHERE LOWER(c.sanPham.ma_san_pham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.sanPham.ten_san_pham) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ChiTietSanPham> findBySanPham_MaSanPhamContainingIgnoreCaseOrSanPham_TenSanPhamContainingIgnoreCase(
            String keyword);

    Optional<ChiTietSanPham> findById(Integer id);

    @Query(nativeQuery = true, value = "select id_chi_tiet_san_pham, ma_san_pham, ten_san_pham, qr_code, gia_ban, so_luong, ctsp.trang_thai as trang_thai,\n"
            +
            "ctsp.ngay_tao, ctsp.ngay_sua, gia_tri, don_vi, ten_mau_sac as ten_mau_sac, ten_danh_muc, ten_thuong_hieu, ten_chat_lieu, ctsp.id_mau_sac,ctsp.id_kich_thuoc, sp.id_san_pham, sp.id_danh_muc, sp.id_thuong_hieu, sp.id_chat_lieu\n"
            +
            "from chi_tiet_san_pham ctsp\n" +
            "full outer join san_pham sp on sp.id_san_pham = ctsp.id_san_pham\n" +
            "left join kich_thuoc kt on kt.id_kich_thuoc = ctsp.id_kich_thuoc\n" +
            "left join mau_sac ms on ms.id_mau_sac = ctsp.id_mau_sac\n" +
            "left join danh_muc_san_pham dm on dm.id_danh_muc = sp.id_danh_muc\n" +
            "left join thuong_hieu th on th.id_thuong_hieu = sp.id_thuong_hieu\n" +
            "left join chat_lieu cl on cl.id_chat_lieu = sp.id_chat_lieu")
    ArrayList<ChiTietSanPhamView> listCTSP();

    @Query(nativeQuery = true, value = "select id_chi_tiet_san_pham,ma_san_pham, ten_san_pham, qr_code, gia_ban, so_luong, ctsp.trang_thai as trang_thai,\n"
            +
            "ctsp.ngay_tao, ctsp.ngay_sua,  gia_tri, don_vi, ten_mau_sac as ten_mau, ten_danh_muc, ten_thuong_hieu, ten_chat_lieu, ctsp.id_mau_sac,ctsp.id_kich_thuoc\n"
            +
            "from chi_tiet_san_pham ctsp\n" +
            "full outer join san_pham sp on sp.id_san_pham = ctsp.id_san_pham\n" +
            "left join kich_thuoc kt on kt.id_kich_thuoc = ctsp.id_kich_thuoc\n" +
            "left join mau_sac ms on ms.id_mau_sac = ctsp.id_mau_sac\n" +
            "left join danh_muc_san_pham dm on dm.id_danh_muc = sp.id_danh_muc\n" +
            "left join thuong_hieu th on th.id_thuong_hieu = sp.id_thuong_hieu\n" +
            "left join chat_lieu cl on cl.id_chat_lieu = sp.id_chat_lieu")
    Page<ChiTietSanPhamView> listPhanTrangChiTietSanPham(Pageable pageable);

    @Query(nativeQuery = true, value = "select id_chi_tiet_san_pham,ma_san_pham, ten_san_pham, qr_code, gia_ban, so_luong, ctsp.trang_thai as trang_thai,\n"
            +
            "ctsp.ngay_tao, ctsp.ngay_sua,  gia_tri, don_vi, ten_mau_sac as ten_mau, ten_danh_muc, ten_thuong_hieu, ten_chat_lieu, ctsp.id_mau_sac,ctsp.id_kich_thuoc\n"
            +
            "from chi_tiet_san_pham ctsp\n" +
            "full outer join san_pham sp on sp.id_san_pham = ctsp.id_san_pham\n" +
            "left join kich_thuoc kt on kt.id_kich_thuoc = ctsp.id_kich_thuoc\n" +
            "left join mau_sac ms on ms.id_mau_sac = ctsp.id_mau_sac\n" +
            "left join danh_muc_san_pham dm on dm.id_danh_muc = sp.id_danh_muc\n" +
            "left join thuong_hieu th on th.id_thuong_hieu = sp.id_thuong_hieu\n" +
            "left join chat_lieu cl on cl.id_chat_lieu = sp.id_chat_lieu\n" +
            "        WHERE (:tenSanPham IS NULL OR ten_san_pham LIKE CONCAT('%', :tenSanPham, '%')) \n" +
            "        AND (:giaBanMin IS NULL OR gia_ban >= :giaBanMin) \n" +
            "        AND (:giaBanMax IS NULL OR gia_ban <= :giaBanMax) \n" +
            "        AND (:soLuongMin IS NULL OR so_luong >= :soLuongMin) \n" +
            "        AND (:soLuongMax IS NULL OR so_luong <= :soLuongMax) \n" +
            "        AND (:trangThai IS NULL OR ctsp.trang_thai = :trangThai) \n" +
            "        AND (:tenMauSac IS NULL OR ten_mau_sac like CONCAT('%', :tenMauSac, '%')) \n" +
            "        AND (:tenDanhMuc IS NULL OR ten_danh_muc like CONCAT('%', :tenDanhMuc, '%')) \n" +
            "        AND (:tenThuongHieu IS NULL OR ten_thuong_hieu like CONCAT('%', :tenThuongHieu, '%')) \n" +
            "        AND (:tenChatLieu IS NULL OR ten_chat_lieu like CONCAT('%', :tenChatLieu, '%'))")
    ArrayList<ChiTietSanPhamView> listLocCTSP(@Param("tenSanPham") String tenSanPham,
            @Param("giaBanMin") float giaBanMin,
            @Param("giaBanMax") float giaBanMax,
            @Param("soLuongMin") Integer soLuongMin,
            @Param("soLuongMax") Integer soLuongMax,
            @Param("trangThai") String trangThai,
            @Param("tenMauSac") String tenMauSac,
            @Param("tenDanhMuc") String tenDanhMuc,
            @Param("tenThuongHieu") String tenThuongHieu,
            @Param("tenChatLieu") String tenChatLieu);

    @Query(nativeQuery = true, value = "select id_chi_tiet_san_pham,ma_san_pham, ten_san_pham, qr_code, gia_ban, so_luong, ctsp.trang_thai as trang_thai,\n"
            +
            "ctsp.ngay_tao, ctsp.ngay_sua,  gia_tri, don_vi, ten_mau_sac as ten_mau, ten_danh_muc, ten_thuong_hieu, ten_chat_lieu, ctsp.id_mau_sac,ctsp.id_kich_thuoc\n"
            +
            "from chi_tiet_san_pham ctsp\n" +
            "full outer join san_pham sp on sp.id_san_pham = ctsp.id_san_pham\n" +
            "left join kich_thuoc kt on kt.id_kich_thuoc = ctsp.id_kich_thuoc\n" +
            "left join mau_sac ms on ms.id_mau_sac = ctsp.id_mau_sac\n" +
            "left join danh_muc_san_pham dm on dm.id_danh_muc = sp.id_danh_muc\n" +
            "left join thuong_hieu th on th.id_thuong_hieu = sp.id_thuong_hieu\n" +
            "left join chat_lieu cl on cl.id_chat_lieu = sp.id_chat_lieu\n" +
            "where ctsp.id_san_pham = :idSanPham ")
    ArrayList<ChiTietSanPhamView> listCTSPFolowSanPham(@Param("idSanPham") Integer idSanPham);

    // =============================== Của
    // Dũng====================================//
    @Query(value = """
            SELECT ctsp.id_chi_tiet_san_pham, sp.ten_san_pham, dm.ten_danh_muc, ms.ten_mau_sac AS ten_mau, kt.gia_tri,
                    ctsp.so_luong, COALESCE(km_max.giaHienTai, ctsp.gia_ban) AS giaHienTai, ctsp.trang_thai,
                    ctsp.gia_ban AS giaGoc, sp.hinh_anh, ha.anh_chinh
            FROM chi_tiet_san_pham ctsp
            JOIN san_pham sp ON ctsp.id_san_pham = sp.id_san_pham
            JOIN danh_muc_san_pham dm ON sp.id_danh_muc = dm.id_danh_muc
            JOIN mau_sac ms ON ctsp.id_mau_sac = ms.id_mau_sac
            JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id_kich_thuoc
            LEFT JOIN hinh_anh ha ON ctsp.id_chi_tiet_san_pham = ha.id_chi_tiet_san_pham AND ha.anh_chinh = 1
            LEFT JOIN ( SELECT
                            ctkm.id_chi_tiet_san_pham,
                            MIN(ctkm.gia_sau_giam) AS giaHienTai
                        FROM chi_tiet_khuyen_mai ctkm
                        JOIN khuyen_mai km ON ctkm.id_khuyen_mai = km.id_khuyen_mai
                        WHERE km.trang_thai = N'Đang diễn ra'
                        AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
                        GROUP BY ctkm.id_chi_tiet_san_pham
                        ) km_max ON ctsp.id_chi_tiet_san_pham = km_max.id_chi_tiet_san_pham
            WHERE ctsp.trang_thai = 1
            ORDER BY ctsp.id_chi_tiet_san_pham DESC
            """, nativeQuery = true)
    Page<ChiTietSanPhamView> getAllCTSP_HD(Pageable pageable);

    @Query(value = """
            SELECT ctsp.id_chi_tiet_san_pham, sp.ten_san_pham, dm.ten_danh_muc, ms.ten_mau_sac AS ten_mau, kt.gia_tri,
                    ctsp.so_luong, COALESCE(km_max.giaHienTai, ctsp.gia_ban) AS giaHienTai, ctsp.trang_thai,
                    ctsp.gia_ban AS giaGoc, sp.hinh_anh, ha.anh_chinh
            FROM chi_tiet_san_pham ctsp
            JOIN san_pham sp ON ctsp.id_san_pham = sp.id_san_pham
            JOIN danh_muc_san_pham dm ON sp.id_danh_muc = dm.id_danh_muc
            JOIN mau_sac ms ON ctsp.id_mau_sac = ms.id_mau_sac
            JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id_kich_thuoc
            LEFT JOIN hinh_anh ha ON ctsp.id_chi_tiet_san_pham = ha.id_chi_tiet_san_pham AND ha.anh_chinh = 1
            LEFT JOIN ( SELECT
                            ctkm.id_chi_tiet_san_pham,
                            MIN(ctkm.gia_sau_giam) AS giaHienTai
                        FROM chi_tiet_khuyen_mai ctkm
                        JOIN khuyen_mai km ON ctkm.id_khuyen_mai = km.id_khuyen_mai
                        WHERE km.trang_thai = N'Đang diễn ra'
                        AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
                        GROUP BY ctkm.id_chi_tiet_san_pham
                        ) km_max ON ctsp.id_chi_tiet_san_pham = km_max.id_chi_tiet_san_pham
            WHERE ctsp.trang_thai = 1
            AND (sp.ten_san_pham LIKE CONCAT('%', :keyword, '%') OR dm.ten_danh_muc LIKE CONCAT('%', :keyword, '%'))
            ORDER BY ctsp.id_chi_tiet_san_pham DESC
            """, nativeQuery = true)
    Page<ChiTietSanPhamView> searchCTSP_HD(@Param("keyword") String keyword, Pageable pageable);

    // =============================== Của
    // Dũng====================================//
    @Query(nativeQuery = true, value = """
            WITH DanhGiaSanPham AS (
                SELECT
                    id_chi_tiet_san_pham,
                    AVG(COALESCE(danh_gia, 0) * 1.0) AS danh_gia_trung_binh,
                    COUNT(binh_luan) AS so_luong_danh_gia
                FROM binh_luan
                GROUP BY id_chi_tiet_san_pham
            ),
            KhuyenMaiHieuLuc AS (
                SELECT
                    ctkm.id_chi_tiet_san_pham,
                    km.kieu_giam_gia,
                    km.gia_tri_giam,
                    ROW_NUMBER() OVER (
                        PARTITION BY ctkm.id_chi_tiet_san_pham
                        ORDER BY km.ngay_bat_dau DESC
                    ) AS rn
                FROM chi_tiet_khuyen_mai ctkm
                JOIN khuyen_mai km ON ctkm.id_khuyen_mai = km.id_khuyen_mai
                WHERE GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
            ),
            KhuyenMaiHieuLucNhat AS (
                SELECT *
                FROM KhuyenMaiHieuLuc
                WHERE rn = 1
            ),
            AnhSanPham AS (
                SELECT
                    id_chi_tiet_san_pham,
                    STUFF((
                        SELECT ',' + ha.hinh_anh
                        FROM hinh_anh ha
                        WHERE ha.id_chi_tiet_san_pham = outer_ha.id_chi_tiet_san_pham
                          AND ha.hinh_anh IS NOT NULL AND ha.hinh_anh <> ''
                        ORDER BY ha.id_hinh_anh
                        FOR XML PATH('')
                    ), 1, 1, '') AS hinh_anh
                FROM hinh_anh outer_ha
                GROUP BY id_chi_tiet_san_pham
            )

            SELECT
                ctsp.id_chi_tiet_san_pham,
                sp.id_san_pham,
                sp.ma_san_pham,
                sp.ten_san_pham,
                sp.mo_ta,
                dm.ten_danh_muc,
                th.ten_thuong_hieu,
                cl.ten_chat_lieu,
                COALESCE(asp.hinh_anh, '') AS hinh_anh,
                kt.gia_tri,
                kt.don_vi,
                ms.ma_mau_sac,
                ms.ten_mau_sac,
                kt.id_kich_thuoc,
                ms.id_mau_sac,
                ctsp.ngay_tao,
                ctsp.ngay_sua,
                ctsp.so_luong,
                COALESCE(dgs.danh_gia_trung_binh, 0) AS danh_gia_trung_binh,
                COALESCE(dgs.so_luong_danh_gia, 0) AS so_luong_danh_gia,
                ctsp.gia_ban AS GiaGoc,
                COALESCE(
                    CASE
                        WHEN kh.kieu_giam_gia = 'Phần trăm' THEN ctsp.gia_ban * (1 - kh.gia_tri_giam / 100.0)
                        WHEN kh.kieu_giam_gia = 'Tiền mặt' THEN ctsp.gia_ban - kh.gia_tri_giam
                    END,
                    ctsp.gia_ban
                ) AS GiaHienTai,
                kh.gia_tri_giam AS GiaTriKhuyenMai,
                kh.kieu_giam_gia AS KieuKhuyenMai,
                ctsp.trang_thai
            FROM chi_tiet_san_pham ctsp
            INNER JOIN san_pham sp ON sp.id_san_pham = ctsp.id_san_pham
            INNER JOIN danh_muc_san_pham dm ON sp.id_danh_muc = dm.id_danh_muc
            INNER JOIN thuong_hieu th ON sp.id_thuong_hieu = th.id_thuong_hieu
            INNER JOIN chat_lieu cl ON sp.id_chat_lieu = cl.id_chat_lieu
            LEFT JOIN KhuyenMaiHieuLucNhat kh ON ctsp.id_chi_tiet_san_pham = kh.id_chi_tiet_san_pham
            LEFT JOIN DanhGiaSanPham dgs ON ctsp.id_chi_tiet_san_pham = dgs.id_chi_tiet_san_pham
            LEFT JOIN kich_thuoc kt ON kt.id_kich_thuoc = ctsp.id_kich_thuoc
            LEFT JOIN mau_sac ms ON ms.id_mau_sac = ctsp.id_mau_sac
            LEFT JOIN AnhSanPham asp ON ctsp.id_chi_tiet_san_pham = asp.id_chi_tiet_san_pham
            WHERE
                sp.trang_thai = 1
                AND sp.id_san_pham = :idSanPham
            ORDER BY
                ctsp.id_chi_tiet_san_pham;
            """)
    ArrayList<ChiTietSanPhamView> getCTSPBySanPhamFull(@Param("idSanPham") Integer idSanPham);

    // ddd
    @Modifying
    @Transactional
    @Query(value = """
            update chi_tiet_san_pham
            set
                so_luong = so_luong + :soLuong
                where id_chi_tiet_san_pham = :idCTSP
            """, nativeQuery = true)
    void updateSLCTSPByIdCTSP(@RequestParam("idCTSP") Integer idCTSP,
            @RequestParam("soLuong") Integer soLuong);

    @Query(value = """
            SELECT ctsp.id_chi_tiet_san_pham,
            ma_san_pham,
            sp.ten_san_pham,
            dm.ten_danh_muc,
            ms.ten_mau_sac AS ten_mau,
            kt.gia_tri,
            ctsp.so_luong,
            COALESCE(km_max.giaHienTai, ctsp.gia_ban) AS gia_ban,
            ctsp.trang_thai,
            sp.hinh_anh
            FROM chi_tiet_san_pham ctsp
            JOIN san_pham sp ON ctsp.id_san_pham = sp.id_san_pham
            JOIN danh_muc_san_pham dm ON sp.id_danh_muc = dm.id_danh_muc
            JOIN mau_sac ms ON ctsp.id_mau_sac = ms.id_mau_sac
            JOIN kich_thuoc kt ON ctsp.id_kich_thuoc = kt.id_kich_thuoc
            LEFT JOIN hinh_anh ha ON ctsp.id_chi_tiet_san_pham = ha.id_chi_tiet_san_pham AND ha.anh_chinh = 1
            LEFT JOIN ( SELECT
            	ctkm.id_chi_tiet_san_pham,
                MIN(ctkm.gia_sau_giam) AS giaHienTai
            	FROM chi_tiet_khuyen_mai ctkm
            	JOIN khuyen_mai km ON ctkm.id_khuyen_mai = km.id_khuyen_mai
            	WHERE km.trang_thai = N'Đang diễn ra'
            	AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
            	GROUP BY ctkm.id_chi_tiet_san_pham
            	) km_max ON ctsp.id_chi_tiet_san_pham = km_max.id_chi_tiet_san_pham
            WHERE ctsp.trang_thai = 1
            ORDER BY ctsp.id_chi_tiet_san_pham
            """, nativeQuery = true)
    List<ChiTietSanPhamView> getAllCTSPKM();

    //////
    @Query(nativeQuery = true, value = """
            select * from chi_tiet_san_pham ctsp
            where ctsp.id_san_pham = :idSanPham and ctsp.id_mau_sac= :idMauSac and ctsp.id_kich_thuoc = :idKichThuoc
            """)
    Optional<ChiTietSanPham> findByIdSanPhamIdMauSacIdKichThuoc(
            @Param("idSanPham") Integer idSanPham,
            @Param("idMauSac") Integer idMauSac,
            @Param("idKichThuoc") Integer idKichThuoc);

}
