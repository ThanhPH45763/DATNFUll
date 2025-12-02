package com.example.duanbe.repository;

import com.example.duanbe.entity.ChiTietGioHang;
import com.example.duanbe.response.GioHangWebResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import com.example.duanbe.entity.ChiTietGioHangId;
import org.springframework.data.repository.query.Param;

//sưa ngay 20/10
public interface GioHangWebRepo extends JpaRepository<ChiTietGioHang, ChiTietGioHangId> {
        @Query(nativeQuery = true, value = """
                        select gh.id_gio_hang,ctsp.id_chi_tiet_san_pham ,gh.id_khach_hang, sp.hinh_anh, sp.ten_san_pham, ms.ten_mau_sac, kt.gia_tri, kt.don_vi, ctgh.so_luong, ctsp.gia_ban from gio_hang gh
                        join chi_tiet_gio_hang ctgh on ctgh.id_gio_hang = gh.id_gio_hang
                        join chi_tiet_san_pham ctsp on ctsp.id_chi_tiet_san_pham = ctgh.id_chi_tiet_san_pham
                        join khach_hang kh on kh.id_khach_hang = gh.id_khach_hang
                        join san_pham sp on sp.id_san_pham = ctsp.id_san_pham
                        join mau_sac ms on ms.id_mau_sac = ctsp.id_mau_sac
                        join kich_thuoc kt on kt.id_kich_thuoc = ctsp.id_kich_thuoc
                        where gh.id_khach_hang = :idKhachHang
                        """)
        List<GioHangWebResponse> listGioHangByKhachHang(@Param("idKhachHang") Integer idKhachHang);

        @Query(nativeQuery = true, value = """
                        select kh.id_khach_hang, dckh.so_nha, dckh.xa_phuong, dckh.quan_huyen, dckh.tinh_thanh_pho, dckh.dia_chi_mac_dinh from khach_hang kh
                        join dia_chi_khach_hang dckh on dckh.id_khach_hang = kh.id_khach_hang
                        where kh.id_khach_hang = :idKhachHang
                        """)
        List<GioHangWebResponse> listDiaChiByKH(@Param("idKhachHang") Integer idKhachHang);

        // ✅ NEW: Check if CTSP exists in any cart (for deletion validation)
        @Query("SELECT COUNT(c) FROM ChiTietGioHang c WHERE c.chiTietSanPham.id_chi_tiet_san_pham = :idCTSP")
        Long countByCTSPId(@Param("idCTSP") Integer idCTSP);

        // ✅ NEW: Check if any CTSP of a product exists in carts
        @Query("SELECT COUNT(c) FROM ChiTietGioHang c WHERE c.chiTietSanPham.sanPham.id_san_pham = :idSanPham")
        Long countBySanPhamId(@Param("idSanPham") Integer idSanPham);
}
