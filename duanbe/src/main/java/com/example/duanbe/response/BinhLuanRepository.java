package com.example.duanbe.response;

import com.example.duanbe.entity.BinhLuan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BinhLuanRepository extends JpaRepository<BinhLuan, Integer> {

    @Query("SELECT b FROM BinhLuan b WHERE b.idChiTietSanPham = :idChiTietSanPham ORDER BY b.ngayTao DESC")
    List<BinhLuan> findByIdChiTietSanPham(Integer idChiTietSanPham);

    @Query("SELECT COUNT(b) FROM BinhLuan b WHERE b.idChiTietSanPham = :idChiTietSanPham")
    long countByIdChiTietSanPham(Integer idChiTietSanPham);

    @Query("SELECT AVG(b.danhGia) FROM BinhLuan b WHERE b.idChiTietSanPham = :idChiTietSanPham")
    Double getAverageRatingByIdChiTietSanPham(Integer idChiTietSanPham);

    @Query("SELECT b FROM BinhLuan b WHERE b.idKhachHang = :idKhachHang AND b.idChiTietSanPham = :idChiTietSanPham")
    Optional<BinhLuan> findByIdKhachHangAndIdChiTietSanPham(Integer idKhachHang, Integer idChiTietSanPham);

    @Modifying
    @Transactional
    @Query("DELETE FROM BinhLuan b WHERE b.idKhachHang = :idKhachHang AND b.idChiTietSanPham = :idChiTietSanPham")
    void deleteById_KhachHangAndId_ChiTietSanPham(Integer idKhachHang, Integer idChiTietSanPham);
}
