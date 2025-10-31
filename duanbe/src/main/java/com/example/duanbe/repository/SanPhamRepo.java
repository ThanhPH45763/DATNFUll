package com.example.duanbe.repository;

import com.example.duanbe.entity.SanPham;
import com.example.duanbe.response.SanPhamView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.List;

public interface SanPhamRepo extends JpaRepository<SanPham, Integer> {
    @Query(nativeQuery = true, value = """
            SELECT
            sp.id_san_pham AS id_san_pham,
            sp.ma_san_pham,
            sp.ten_san_pham,
            sp.mo_ta,
            CASE
            WHEN SUM(ctsp.so_luong) <= 0 THEN '0'
            ELSE sp.trang_thai
            END AS trang_thai, 
            dm.ten_danh_muc AS ten_danh_muc,
            th.ten_thuong_hieu AS ten_thuong_hieu,
            cl.ten_chat_lieu,
            sp.anh_dai_dien as hinh_anh,
            SUM(ctsp.so_luong) AS tong_so_luong
            FROM san_pham sp
            LEFT JOIN danh_muc_san_pham dm ON dm.id_danh_muc = sp.id_danh_muc
            LEFT JOIN thuong_hieu th ON th.id_thuong_hieu = sp.id_thuong_hieu
            LEFT JOIN chat_lieu cl ON cl.id_chat_lieu = sp.id_chat_lieu
            FULL OUTER JOIN chi_tiet_san_pham ctsp ON ctsp.id_san_pham = sp.id_san_pham
            GROUP BY
            sp.id_san_pham,
            sp.ma_san_pham,
            sp.ten_san_pham,
            sp.mo_ta,
            sp.trang_thai, 
            dm.ten_danh_muc,
            th.ten_thuong_hieu,
            cl.ten_chat_lieu,
            sp.anh_dai_dien
                        """)
    ArrayList<SanPhamView> getAllSanPham();

    @Query(nativeQuery = true, value = """
            SELECT
                        sp.id_san_pham AS id_san_pham,
                        sp.ma_san_pham,
                        sp.ten_san_pham,
                        sp.mo_ta,
                        CASE
                        WHEN SUM(ctsp.so_luong) <= 0 THEN 'Không hoạt động'
                        ELSE sp.trang_thai
                        END AS trang_thai,  -- Sửa ở đây
                        dm.ten_danh_muc AS ten_danh_muc,
                        th.ten_thuong_hieu AS ten_thuong_hieu,
                        cl.ten_chat_lieu,
                        sp.anh_dai_dien,
                        SUM(ctsp.so_luong) AS tong_so_luong,
            			max(ctsp.ngay_sua) as ngay_sua_moi
                        FROM san_pham sp
                        LEFT JOIN danh_muc_san_pham dm ON dm.id_danh_muc = sp.id_danh_muc
                        LEFT JOIN thuong_hieu th ON th.id_thuong_hieu = sp.id_thuong_hieu
                        LEFT JOIN chat_lieu cl ON cl.id_chat_lieu = sp.id_chat_lieu
                        FULL OUTER JOIN chi_tiet_san_pham ctsp ON ctsp.id_san_pham = sp.id_san_pham
                        GROUP BY
                        sp.id_san_pham,
                        sp.ma_san_pham,
                        sp.ten_san_pham,
                        sp.mo_ta,
                        sp.trang_thai,  -- Giữ nguyên trong GROUP BY
                        dm.ten_danh_muc,
                        th.ten_thuong_hieu,
                        cl.ten_chat_lieu,
                        sp.anh_dai_dien
            			order by ngay_sua_moi desc
            """)
    ArrayList<SanPhamView> getAllSanPhamSapXepTheoNgaySua();

    @Query(nativeQuery = true, value = "select sp.id_san_pham as id_san_pham, sp.ma_san_pham, sp.ten_san_pham, sp.mo_ta, sp.trang_thai as trang_thai, dm.ten_danh_muc as ten_danh_muc, \n"
            +
            "            th.ten_thuong_hieu as ten_thuong_hieu, cl.ten_chat_lieu, sp.anh_dai_dien, sum(ctsp.so_luong) as tong_so_luong\n"
            +
            "            from san_pham sp\n" +
            "           full outer join danh_muc_san_pham dm on dm.id_danh_muc = sp.id_danh_muc\n" +
            "           full outer join thuong_hieu th on th.id_thuong_hieu = sp.id_thuong_hieu\n" +
            "            full outer join chat_lieu cl on cl.id_chat_lieu = sp.id_chat_lieu\n" +
            "\t\t\tfull outer join chi_tiet_san_pham ctsp on ctsp.id_san_pham = sp.id_san_pham\n" +
            "\t\t\tgroup by sp.id_san_pham, sp.ma_san_pham, sp.ten_san_pham, sp.mo_ta, sp.trang_thai, dm.ten_danh_muc, \n"
            +
            "            th.ten_thuong_hieu, cl.ten_chat_lieu,sp.anh_dai_dien")
    Page<SanPhamView> getAllSanPhamPhanTrang(Pageable pageable);

    @Query(nativeQuery = true, value = "select sp.id_san_pham as id_san_pham, sp.ma_san_pham, sp.ten_san_pham, sp.mo_ta, sp.trang_thai as trang_thai, dm.ten_danh_muc as ten_danh_muc, \n"
            +
            "            th.ten_thuong_hieu as ten_thuong_hieu, cl.ten_chat_lieu, sp.anh_dai_dien, sum(ctsp.so_luong) as tong_so_luong\n"
            +
            "            from san_pham sp\n" +
            "          full outer  join danh_muc_san_pham dm on dm.id_danh_muc = sp.id_danh_muc\n" +
            "           full outer join thuong_hieu th on th.id_thuong_hieu = sp.id_thuong_hieu\n" +
            "           full outer join chat_lieu cl on cl.id_chat_lieu = sp.id_chat_lieu\n" +
            "\t\t\tfull outer join chi_tiet_san_pham ctsp on ctsp.id_san_pham = sp.id_san_pham\n" +
            "\t\t\tgroup by sp.id_san_pham, sp.ma_san_pham, sp.ten_san_pham, sp.mo_ta, sp.trang_thai, dm.ten_danh_muc, \n"
            +
            "            th.ten_thuong_hieu, cl.ten_chat_lieu,sp.anh_dai_dien" +
            "where dm.ten_danh_muc like CONCAT('%', :tenDanhMuc, '%') and th.ten_thuong_hieu like CONCAT('%', :tenThuongHieu, '%') and cl.ten_chat_lieu like CONCAT('%', :tenChatLieu, '%')")
    ArrayList<SanPhamView> locSanPham(@Param("tenDanhMuc") String tenDanhMuc,
                                      @Param("tenThuongHieu") String tenThuongHieu, @Param("tenChatLieu") String tenChatLieu);

    // Tìm kiếm sản phẩm theo mã hoặc tên với phân trang
    @Query("SELECT sp FROM SanPham sp WHERE LOWER(sp.ma_san_pham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(sp.ten_san_pham) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<SanPham> findByMaSanPhamOrTenSanPhamContainingIgnoreCase(String keyword, Pageable pageable);

    // Lấy tất cả sản phẩm, sắp xếp theo ID với phân trang
    @Query("SELECT sp FROM SanPham sp ORDER BY sp.id_san_pham")
    Page<SanPham> findAllSortedByIdSanPham(Pageable pageable);

    @Query(nativeQuery = true, value = """
            WITH KhuyenMaiHieuLuc AS (
            SELECT 
            ctkm.id_chi_tiet_san_pham,
            GiamGia = CASE 
            WHEN km.kieu_giam_gia = N'Phần trăm' THEN ctsp.gia_ban * (1 - km.gia_tri_giam / 100)
            WHEN km.kieu_giam_gia = N'Tiền mặt' THEN ctsp.gia_ban - km.gia_tri_giam
            ELSE ctsp.gia_ban
            END
            FROM chi_tiet_khuyen_mai ctkm
            JOIN khuyen_mai km 
            ON ctkm.id_khuyen_mai = km.id_khuyen_mai
            AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
            JOIN chi_tiet_san_pham ctsp 
            ON ctkm.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
            ),
            GiaTotNhat AS (
            SELECT
            ctsp.id_san_pham,
            GiaGiamMin = MIN(ISNULL(kh.GiamGia, ctsp.gia_ban)),
            GiaGiamMax = MAX(ISNULL(kh.GiamGia, ctsp.gia_ban))
            FROM chi_tiet_san_pham ctsp
            LEFT JOIN KhuyenMaiHieuLuc kh 
            ON ctsp.id_chi_tiet_san_pham = kh.id_chi_tiet_san_pham
            GROUP BY ctsp.id_san_pham
            )
            SELECT DISTINCT top 10
            sp.id_san_pham,
            sp.ma_san_pham,
            sp.ten_san_pham,
            sp.mo_ta,
            sp.trang_thai,
            dm.ten_danh_muc,
            th.ten_thuong_hieu,
            cl.ten_chat_lieu,
            sp.anh_dai_dien,
            avg(bl.danh_gia) over (PARTITION BY sp.id_san_pham) as danh_gia,
            count(bl.danh_gia) over(PARTITION BY sp.id_san_pham) as so_luong_danh_gia,
            SUM(ctsp.so_luong) OVER (PARTITION BY sp.id_san_pham) AS tong_so_luong,
            MAX(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham) AS gia_max,
            MIN(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham) AS gia_min,
            COALESCE(gt.GiaGiamMin, MIN(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham)) AS gia_tot_nhat,
            COALESCE(gt.GiaGiamMax, MAX(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham)) AS gia_khuyen_mai_cao_nhat
            FROM san_pham sp
            INNER JOIN danh_muc_san_pham dm 
            ON sp.id_danh_muc = dm.id_danh_muc
            INNER JOIN thuong_hieu th 
            ON sp.id_thuong_hieu = th.id_thuong_hieu
            INNER JOIN chat_lieu cl 
            ON sp.id_chat_lieu = cl.id_chat_lieu
            INNER JOIN chi_tiet_san_pham ctsp 
            ON sp.id_san_pham = ctsp.id_san_pham
            LEFT JOIN GiaTotNhat gt 
            ON sp.id_san_pham = gt.id_san_pham
            left join binh_luan bl 
            on bl.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
            WHERE 
            sp.trang_thai = N'Hoạt động'
            AND EXISTS (
            SELECT 1
            FROM STRING_SPLIT(:tenSanPham, ',') AS kw
            WHERE sp.ten_san_pham LIKE '%' + kw.value + '%')
               """)
    ArrayList<SanPhamView> listSanPhamBanHangWebTheoSP(@Param("tenSanPham") String tenSanPham);

    @Query(nativeQuery = true, value = """
            WITH KhuyenMaiHieuLuc AS (
            SELECT ctkm.id_chi_tiet_san_pham,
            GiamGia = CASE 
            WHEN km.kieu_giam_gia = N'Phần trăm' THEN ctsp.gia_ban * (1 - km.gia_tri_giam / 100)
            WHEN km.kieu_giam_gia = N'Tiền mặt' THEN ctsp.gia_ban - km.gia_tri_giam
            ELSE ctsp.gia_ban
            END
            FROM chi_tiet_khuyen_mai ctkm
            JOIN khuyen_mai km 
            ON ctkm.id_khuyen_mai = km.id_khuyen_mai
            AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
            JOIN chi_tiet_san_pham ctsp 
            ON ctkm.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
            ),
            GiaTotNhat AS (
            SELECT
            ctsp.id_san_pham,
            GiaGiamMin = MIN(ISNULL(kh.GiamGia, ctsp.gia_ban)),
            GiaGiamMax = MAX(ISNULL(kh.GiamGia, ctsp.gia_ban))
            FROM chi_tiet_san_pham ctsp
            LEFT JOIN KhuyenMaiHieuLuc kh 
            ON ctsp.id_chi_tiet_san_pham = kh.id_chi_tiet_san_pham
            GROUP BY ctsp.id_san_pham
            )
            SELECT DISTINCT
            sp.id_san_pham,
            sp.ma_san_pham,
            sp.ten_san_pham,
            sp.mo_ta,
            sp.trang_thai,
            dm.ten_danh_muc,
            th.ten_thuong_hieu,
            cl.ten_chat_lieu,
            sp.anh_dai_dien,
            avg(bl.danh_gia) over (PARTITION BY sp.id_san_pham) as danh_gia,
            count(bl.danh_gia) over(PARTITION BY sp.id_san_pham) as so_luong_danh_gia,
            SUM(ctsp.so_luong) OVER (PARTITION BY sp.id_san_pham) AS tong_so_luong,
            MAX(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham) AS gia_max,
            MIN(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham) AS gia_min,
            COALESCE(gt.GiaGiamMin, MIN(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham)) AS gia_tot_nhat,
            COALESCE(gt.GiaGiamMax, MAX(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham)) AS gia_khuyen_mai_cao_nhat
            FROM san_pham sp
            INNER JOIN danh_muc_san_pham dm 
            ON sp.id_danh_muc = dm.id_danh_muc
            INNER JOIN thuong_hieu th 
            ON sp.id_thuong_hieu = th.id_thuong_hieu
            INNER JOIN chat_lieu cl 
            ON sp.id_chat_lieu = cl.id_chat_lieu
            INNER JOIN chi_tiet_san_pham ctsp 
            ON sp.id_san_pham = ctsp.id_san_pham
            LEFT JOIN GiaTotNhat gt 
            ON sp.id_san_pham = gt.id_san_pham
            left join binh_luan bl 
            on bl.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
            WHERE 
            sp.trang_thai = N'Hoạt động'
             AND EXISTS (SELECT 1
                         FROM STRING_SPLIT(:tenSanPham, ',') AS kw
                         WHERE sp.ten_san_pham LIKE '%' + kw.value + '%')
            """)
    ArrayList<SanPhamView> listSanPhamByTenSP(@Param("tenSanPham") String tenSanPham);

    @Query(nativeQuery = true, value = """
            WITH KhuyenMaiHieuLuc AS (
                       SELECT ctkm.id_chi_tiet_san_pham,
                       GiamGia = CASE 
                       WHEN km.kieu_giam_gia = N'Phần trăm' THEN ctsp.gia_ban * (1 - km.gia_tri_giam / 100)
                       WHEN km.kieu_giam_gia = N'Tiền mặt' THEN ctsp.gia_ban - km.gia_tri_giam
                       ELSE ctsp.gia_ban
                       END
                       FROM chi_tiet_khuyen_mai ctkm
                       JOIN khuyen_mai km 
                       ON ctkm.id_khuyen_mai = km.id_khuyen_mai
                       AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
                       JOIN chi_tiet_san_pham ctsp 
                       ON ctkm.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
                       ),
                       GiaTotNhat AS (
                       SELECT
                       ctsp.id_san_pham,
                       GiaGiamMin = MIN(ISNULL(kh.GiamGia, ctsp.gia_ban)),
                       GiaGiamMax = MAX(ISNULL(kh.GiamGia, ctsp.gia_ban))
                       FROM chi_tiet_san_pham ctsp
                       LEFT JOIN KhuyenMaiHieuLuc kh 
                       ON ctsp.id_chi_tiet_san_pham = kh.id_chi_tiet_san_pham
                       GROUP BY ctsp.id_san_pham
                       )
                       SELECT DISTINCT
                       sp.id_san_pham,
                       sp.ma_san_pham,
                       sp.ten_san_pham,
                       sp.mo_ta,
                       sp.trang_thai,
                       dm.ten_danh_muc,
                       th.ten_thuong_hieu,
                       cl.ten_chat_lieu,
                       sp.anh_dai_dien,
                       avg(bl.danh_gia) over (PARTITION BY sp.id_san_pham) as danh_gia,
                       count(bl.danh_gia) over(PARTITION BY sp.id_san_pham) as so_luong_danh_gia,
                       SUM(ctsp.so_luong) OVER (PARTITION BY sp.id_san_pham) AS tong_so_luong,
                       MAX(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham) AS gia_max,
                       MIN(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham) AS gia_min,
                       COALESCE(gt.GiaGiamMin, MIN(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham)) AS gia_tot_nhat,
                       COALESCE(gt.GiaGiamMax, MAX(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham)) AS gia_khuyen_mai_cao_nhat
                       FROM san_pham sp
                       INNER JOIN danh_muc_san_pham dm 
                       ON sp.id_danh_muc = dm.id_danh_muc
                       INNER JOIN thuong_hieu th 
                       ON sp.id_thuong_hieu = th.id_thuong_hieu
                       INNER JOIN chat_lieu cl 
                       ON sp.id_chat_lieu = cl.id_chat_lieu
                       INNER JOIN chi_tiet_san_pham ctsp 
                       ON sp.id_san_pham = ctsp.id_san_pham
                       LEFT JOIN GiaTotNhat gt 
                       ON sp.id_san_pham = gt.id_san_pham
                       left join binh_luan bl 
                       on bl.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
                       WHERE 
                       sp.trang_thai = N'Hoạt động'
                       AND EXISTS (SELECT 1
                                   FROM STRING_SPLIT(:tenDanhMuc, ',') AS kw
                                   WHERE dm.ten_danh_muc LIKE '%' + kw.value + '%')
                                  """)
    ArrayList<SanPhamView> listSanPhamByTenDM(@Param("tenDanhMuc") String tenDanhMuc);

    @Query("SELECT s FROM SanPham s WHERE LOWER(s.ma_san_pham) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.ten_san_pham) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY s.id_san_pham DESC")
    List<SanPham> findByMaSanPhamOrTenSanPhamContainingIgnoreCase(@Param("keyword") String keyword);

    @Query("SELECT s FROM SanPham s ORDER BY s.id_san_pham DESC")
    List<SanPham> findAllSortedByIdSanPham();

    @Query(nativeQuery = true, value = """
             SELECT distinct
                        sp.id_san_pham AS id_san_pham,
                        sp.ma_san_pham,
                        sp.ten_san_pham,
                        sp.mo_ta,
                        CASE
                            WHEN SUM(ctsp.so_luong) <= 0 THEN 'Không hoạt động'
                            ELSE sp.trang_thai
                        END AS trang_thai,
                        dm.ten_danh_muc AS ten_danh_muc,
                        th.ten_thuong_hieu AS ten_thuong_hieu,
                        cl.ten_chat_lieu,
                        sp.anh_dai_dien as hinh_anh,
                        SUM(ctsp.so_luong) AS tong_so_luong
             FROM san_pham sp
             LEFT JOIN danh_muc_san_pham dm ON dm.id_danh_muc = sp.id_danh_muc
             LEFT JOIN thuong_hieu th ON th.id_thuong_hieu = sp.id_thuong_hieu
             LEFT JOIN chat_lieu cl ON cl.id_chat_lieu = sp.id_chat_lieu
             JOIN chi_tiet_san_pham ctsp ON ctsp.id_san_pham = sp.id_san_pham
             WHERE ctsp.id_chi_tiet_san_pham in (:list)
             GROUP BY
                sp.id_san_pham,
                sp.ma_san_pham,
                sp.ten_san_pham,
                sp.mo_ta,
                sp.trang_thai,
                dm.ten_danh_muc,
                th.ten_thuong_hieu,
                cl.ten_chat_lieu,
                sp.anh_dai_dien
            """)
    List<SanPhamView> getSanPhamByListCTSP(@Param("list") List<Integer> listIdCTSP);
    //Hoàn thiện _Thu
    @Query(nativeQuery = true, value = """
            -- Lấy giá giảm tốt nhất từ các khuyến mãi hợp lệ, có tính đến giới hạn giảm giá tối đa
            WITH KhuyenMaiTotNhat AS (
                SELECT
                    ctkm.id_chi_tiet_san_pham,
                    GiamGiaTotNhat = MIN(
                        CASE\s
                            WHEN km.kieu_giam_gia = N'Phần trăm' THEN\s
                                CASE\s
                                    WHEN ctsp.gia_ban * km.gia_tri_giam / 100 > km.gia_tri_toi_da\s
                                        THEN ctsp.gia_ban - km.gia_tri_toi_da
                                    ELSE ctsp.gia_ban * (1 - km.gia_tri_giam / 100)
                                END
                            WHEN km.kieu_giam_gia = N'Tiền mặt' THEN ctsp.gia_ban - km.gia_tri_giam
                            ELSE ctsp.gia_ban
                        END
                    )
                FROM chi_tiet_khuyen_mai ctkm
                JOIN khuyen_mai km\s
                    ON ctkm.id_khuyen_mai = km.id_khuyen_mai
                    AND GETDATE() BETWEEN km.ngay_bat_dau AND km.ngay_het_han
                    AND km.trang_thai = N'Đang diễn ra'
                JOIN chi_tiet_san_pham ctsp\s
                    ON ctkm.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
                GROUP BY ctkm.id_chi_tiet_san_pham
            ),

            -- Tính giá khuyến mãi min và max cho từng sản phẩm
            GiaTotNhat AS (
                SELECT
                    ctsp.id_san_pham,
                    GiaGiamMin = MIN(ISNULL(kmtn.GiamGiaTotNhat, ctsp.gia_ban)),
                    GiaGiamMax = MAX(ISNULL(kmtn.GiamGiaTotNhat, ctsp.gia_ban))
                FROM chi_tiet_san_pham ctsp
                LEFT JOIN KhuyenMaiTotNhat kmtn\s
                    ON ctsp.id_chi_tiet_san_pham = kmtn.id_chi_tiet_san_pham
                GROUP BY ctsp.id_san_pham
            ),

            -- Tính giá max và giá khuyến mãi để so sánh trong WHERE
            SanPhamWithPrices AS (
                SELECT DISTINCT
                    sp.id_san_pham,
                    sp.ma_san_pham,
                    sp.ten_san_pham,
                    sp.mo_ta,
                    sp.trang_thai,
                    dm.ten_danh_muc,
                    th.ten_thuong_hieu,
                    cl.ten_chat_lieu,
                    sp.anh_dai_dien,
                    AVG(bl.danh_gia) OVER (PARTITION BY sp.id_san_pham) AS danh_gia,
                    COUNT(bl.danh_gia) OVER (PARTITION BY sp.id_san_pham) AS so_luong_danh_gia,
                    SUM(ctsp.so_luong) OVER (PARTITION BY sp.id_san_pham) AS tong_so_luong,
                    MAX(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham) AS gia_max,
                    MIN(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham) AS gia_min,
                    COALESCE(gt.GiaGiamMin, MIN(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham)) AS gia_tot_nhat,
                    COALESCE(gt.GiaGiamMax, MAX(ctsp.gia_ban) OVER (PARTITION BY sp.id_san_pham)) AS gia_khuyen_mai_cao_nhat
                FROM san_pham sp
                INNER JOIN danh_muc_san_pham dm
                    ON sp.id_danh_muc = dm.id_danh_muc
                INNER JOIN thuong_hieu th
                    ON sp.id_thuong_hieu = th.id_thuong_hieu
                INNER JOIN chat_lieu cl
                    ON sp.id_chat_lieu = cl.id_chat_lieu
                INNER JOIN chi_tiet_san_pham ctsp
                    ON sp.id_san_pham = ctsp.id_san_pham
                LEFT JOIN GiaTotNhat gt
                    ON sp.id_san_pham = gt.id_san_pham
                LEFT JOIN binh_luan bl
                    ON bl.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham
                WHERE
                    sp.trang_thai = N'Hoạt động'
            )

            -- Truy vấn chính với điều kiện lọc
            SELECT *
            FROM SanPhamWithPrices
            WHERE gia_khuyen_mai_cao_nhat < gia_max
            ORDER BY id_san_pham;
            """)
    ArrayList<SanPhamView> listSanPhamSieuKhuyeMai();
}
