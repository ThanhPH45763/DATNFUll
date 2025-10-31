package com.example.duanbe.repository;

import com.example.duanbe.entity.ChiTietGioHang;
import com.example.duanbe.entity.ChiTietGioHangId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChiTietGioHangRepository extends JpaRepository<ChiTietGioHang, ChiTietGioHangId> {

    Optional<ChiTietGioHang> findById(ChiTietGioHangId chiTietGioHangId);
}
