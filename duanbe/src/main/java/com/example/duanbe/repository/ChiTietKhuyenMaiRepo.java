package com.example.duanbe.repository;

import com.example.duanbe.entity.ChiTietKhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChiTietKhuyenMaiRepo extends JpaRepository<ChiTietKhuyenMai, Integer> {
    @Query("SELECT c FROM ChiTietKhuyenMai c JOIN FETCH c.chiTietSanPham WHERE c.khuyenMai.id = :khuyenMaiId")
    List<ChiTietKhuyenMai> findByKhuyenMaiId(Integer khuyenMaiId);

    void deleteByKhuyenMaiId(Integer khuyenMaiId);

    @Query("SELECT ckm FROM ChiTietKhuyenMai ckm " +
            "JOIN ckm.khuyenMai km " +
            "WHERE ckm.chiTietSanPham.id_chi_tiet_san_pham = :idChiTietSanPham " +
            "AND km.trangThai = 'Đang hoạt động' " +
            "AND km.ngayBatDau <= CURRENT_TIMESTAMP " +
            "AND km.ngayHetHan >= CURRENT_TIMESTAMP")
    List<ChiTietKhuyenMai> findAllByChiTietSanPhamId(@Param("idChiTietSanPham") Integer idChiTietSanPham);

}
