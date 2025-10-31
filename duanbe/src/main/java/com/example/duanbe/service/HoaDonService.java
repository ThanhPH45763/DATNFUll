package com.example.duanbe.service;

import com.example.duanbe.entity.HoaDon;
import com.example.duanbe.repository.HoaDonRepo;
import com.example.duanbe.response.HoaDonResponse;
import com.itextpdf.text.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class HoaDonService {
    @Autowired
    private HoaDonRepo hoaDonRepo;


    // lềnh thay đổi
    public List<HoaDonResponse> getHoaDonByKhachHangId(Integer idKhachHang) {
        return hoaDonRepo.findHoaDonWithLatestStatusByKhachHangId(idKhachHang);
    }

    public int countHoaDonByKhachHangId(Integer idKhachHang) {
        return hoaDonRepo.countByKhachHangId(idKhachHang);
    }
}

