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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.duanbe.ImportAndExportEx.ExcelSaveDB;
import com.example.duanbe.dto.SanPhamDisplayDTO;
import com.example.duanbe.entity.ChiTietSanPham;
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

    @PutMapping("/updateSanPham")
    public ResponseEntity<?> updateSanPham(@RequestBody SanPhamRequest sanPhamRequest) {
        return sanPhamService.updateSanPham(sanPhamRequest);
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

    /**
     * ✅ NEW: API endpoint to permanently delete product
     * DELETE /admin/quan_ly_san_pham/deleteSanPhamPermanent?id={id}
     */
    @DeleteMapping("/deleteSanPhamPermanent")
    public ResponseEntity<?> deleteSanPhamPermanent(@RequestParam("id") Integer id) {
        return sanPhamService.deleteSanPhamPermanent(id);
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

    // ============= ENDPOINTS MỚI: TRẢ VỀ DỮ LIỆU ĐÃ FORMAT CHO FRONTEND
    // =============

    /**
     * Lấy danh sách sản phẩm theo tên danh mục với giá đã format
     * Ưu tiên hiển thị giá khuyến mãi nếu có
     * 
     * @param tenDanhMuc Tên danh mục (có thể nhiều, cách nhau bởi dấu phẩy)
     * @return List sản phẩm với thông tin giá đã format
     */
    @GetMapping("/getSanPhamByTenDM/formatted")
    public List<SanPhamDisplayDTO> getSanPhamByTenDMFormatted(@RequestParam("tenDanhMuc") String tenDanhMuc) {
        return sanPhamService.getSanPhamTheoTenDMFormatted(tenDanhMuc);
    }

    /**
     * Lấy danh sách sản phẩm theo tên sản phẩm với giá đã format
     * 
     * @param tenSanPham Tên sản phẩm (có thể nhiều, cách nhau bởi dấu phẩy)
     * @return List sản phẩm với thông tin giá đã format
     */
    @GetMapping("/getSanPhamByTenSP/formatted")
    public List<SanPhamDisplayDTO> getSanPhamByTenSPFormatted(@RequestParam("tenSanPham") String tenSanPham) {
        return sanPhamService.getSanPhamTheoTenSPFormatted(tenSanPham);
    }

    /**
     * Lấy danh sách sản phẩm siêu khuyến mãi với giá đã format
     * 
     * @return List sản phẩm đang có khuyến mãi
     */
    @GetMapping("/getSanPhamSieuSale/formatted")
    public List<SanPhamDisplayDTO> getSanPhamSieuSaleFormatted() {
        return sanPhamService.getSanPhamSieuSaleFormatted();
    }

    /**
     * Top 10 sản phẩm bán chạy nhất (theo doanh số)
     * 
     * @return List sản phẩm bán chạy nhất với giá khuyến mãi đầy đủ
     */
    @GetMapping("/san-pham-ban-chay-nhat")
    public List<SanPhamView> getSanPhamBanChayNhat() {
        return sanPhamService.getSanPhamBanChayNhat();
    }

    /**
     * Top 10 sản phẩm mới nhập (theo ngày tạo/sửa)
     * 
     * @return List sản phẩm mới nhất với giá khuyến mãi đầy đủ
     */
    @GetMapping("/san-pham-moi-nhat")
    public List<SanPhamView> getSanPhamMoiNhat() {
        return sanPhamService.getSanPhamMoiNhat();
    }

    @Autowired
    ExcelSaveDB excelSaveDB;

    @PostMapping("/save")
    public ResponseEntity<?> saveToDB(@RequestBody List<ChiTietSanPham> list) {
        excelSaveDB.saveToDB(list);
        return ResponseEntity.ok("ok");
    }

    @PutMapping("/checkStatusSPByCTSP")
    public ResponseEntity<?> checkStatusSPByCTSP(@RequestParam("id_san_pham") Integer idSanPham) {
        return sanPhamService.checkStatusSPByCTSP(idSanPham);
    }
}
