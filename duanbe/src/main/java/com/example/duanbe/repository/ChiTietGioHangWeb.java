package com.example.duanbe.repository;

import com.example.duanbe.entity.ChiTietGioHang;
import com.example.duanbe.entity.ChiTietGioHangId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChiTietGioHangWeb extends JpaRepository<ChiTietGioHang, ChiTietGioHangId> {
}
