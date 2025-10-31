package com.example.duanbe.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.duanbe.entity.SanPham;
import com.example.duanbe.request.SanPhamRequest;
import com.example.duanbe.response.ChiTietSanPhamView;
import com.example.duanbe.response.SanPhamView;
import com.example.duanbe.service.ChiTietSanPhamService;
import com.example.duanbe.service.SanPhamService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE })
@RequestMapping("/admin/quan_ly_san_pham/")
public class SanPhamController {
    @Autowired
    private SanPhamService sanPhamService;
    @Autowired
    private ChiTietSanPhamService chiTietSanPhamService;

    @GetMapping("/SanPham")
    public List<SanPhamView> getAll() {
        System.out.println("chạy vào đây");
        return sanPhamService.getAll();
    }

    @GetMapping("/SanPhamFindAll")
    public List<SanPham> getAllfindAll() {
        return sanPhamService.getAllFindAll();
    }

    @GetMapping("/sanPhamTheoNgaySua")
    public List<SanPhamView> getAllSPTheoNgay() {
        return sanPhamService.getAllSPNgaySua();
    }

    @GetMapping("/allSanPham")
    public List<SanPhamView> getAll(@RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return sanPhamService.getAllPhanTrang(pageable).getContent();
    }

    @GetMapping("/sanPhamDetail")
    public SanPham spDetail(@RequestParam("id") Integer id) {
        return sanPhamService.detailSP(id);
    }

    @PostMapping("/saveSanPham2")
    public ResponseEntity<?> addSanPham2(@Valid @RequestBody SanPhamRequest sanPhamRequest,
            BindingResult bindingResult) {
        return sanPhamService.saveSanPham2(sanPhamRequest, bindingResult);
    }

    @PostMapping("/saveSanPham")
    public ResponseEntity<?> addSanPham(@RequestBody SanPhamRequest sanPhamRequest) {
        return sanPhamService.saveSanPham(sanPhamRequest);
    }

    @PostMapping("/xoaSanPham")
    public String xoaSanPham(@RequestParam("id") Integer id) {
        return sanPhamService.deleteSanPham(id);
    }

    @GetMapping("/timKiemSanPham")
    public List<SanPham> searchSanPham(@RequestParam("search") String search) {
        return sanPhamService.listTimKiem(search);
    }

    @PutMapping("/chuyenTrangThaiSanPham")
    public ResponseEntity<?> chuyenTrangThaiSanPham(@RequestParam("id") Integer id) {
        return sanPhamService.chuyenTrangThai(id);
    }

    @GetMapping("/locSanPham")
    public List<SanPhamView> locSanPham(
            @RequestParam(value = "danhMuc", required = false) String danhMuc,
            @RequestParam(value = "thuongHieu", required = false) String thuongHieu,
            @RequestParam(value = "chatLieu", required = false) String chatLieu) {
        return sanPhamService.locSanPham(danhMuc, thuongHieu, chatLieu);
    }

    @GetMapping("/sapXep")
    public List<SanPhamView> sapXep(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "5") Integer size,
            @RequestParam(name = "tieuChi", required = false) String tieuChi) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(tieuChi).ascending());
        return sanPhamService.sapXep(pageable).getContent();
    }

    @GetMapping("/getSanPhamByTenSanPham")
    public List<SanPhamView> getSanPhamBySP(@RequestParam("tenSanPham") String tenSanPham) {
        // Spring sẽ tự split "áo,quần" thành List.of("áo","quần")
        String joined = String.join(",", tenSanPham);
        return sanPhamService.getSanPhamTheoTen(joined);
    }

    @GetMapping("/getSanPhamByTenSP")
    public List<SanPhamView> getSanPhamByTenSP(@RequestParam("tenSanPham") String tenSanPham) {
        return sanPhamService.getSanPhamTheoTenSP(tenSanPham);
    }

    @GetMapping("/getSanPhamByTenDM")
    public List<SanPhamView> getSanPhamByTenDM(@RequestParam("tenDanhMuc") String tenDanhMuc) {
        return sanPhamService.getSanPhamTheoTenDM(tenDanhMuc);
    }

    @GetMapping("/getSanPhamSieuSale")
    public List<SanPhamView> getSanPhamSieuSale() {
        return sanPhamService.getSanPhamSieuSale();
    }

    @GetMapping("/getAllCTSPKM")
    public List<ChiTietSanPhamView> getAllCTSPKM() {
        return sanPhamService.getAllCTSPKM();
    }
}
