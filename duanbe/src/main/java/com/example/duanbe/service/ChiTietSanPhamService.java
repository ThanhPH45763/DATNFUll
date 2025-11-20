package com.example.duanbe.service;

import com.example.duanbe.entity.*;
import com.example.duanbe.repository.*;
import com.example.duanbe.request.ChiTietSanPhamRequest;
import com.example.duanbe.response.ChiTietSanPhamView;
import com.example.duanbe.response.HinhAnhView;
import com.example.duanbe.response.SanPhamView;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ChiTietSanPhamService {
    @Autowired
    ChiTietSanPhamRepo chiTietSanPhamRepo;
    @Autowired
    HinhAnhSanPhamRepo hinhAnhSanPhamRepo;
    @Autowired
    MauSacRepo mauSacRepo;
    @Autowired
    KichThuocRepo kichThuocRepo;
    @Autowired
    SanPhamRepo sanPhamRepo;
    @Autowired
    SanPhamService sanPhamService;

    // @Cacheable(value = "detailProducts")
    public List<ChiTietSanPhamView> getAllCTSP() {
        return chiTietSanPhamRepo.listCTSP();
    }

    public List<ChiTietSanPham> getAllCTSPFindAll() {
        return chiTietSanPhamRepo.findAll();
    }

    public Page<ChiTietSanPhamView> getAllCTSPPhanTrang(Pageable pageable) {
        return chiTietSanPhamRepo.listPhanTrangChiTietSanPham(pageable);
    }

    public static String convertJsDateToUtc7(String jsDateString) {
        // Chuyển chuỗi từ JS (ISO 8601) thành Instant (UTC)
        Instant instant = Instant.parse(jsDateString);

        // Chuyển từ UTC sang UTC+7 (Asia/Bangkok)
        ZonedDateTime utc7Time = instant.atZone(ZoneId.of("Asia/Bangkok"));

        // Định dạng kết quả theo "yyyy-MM-dd HH:mm:ss"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return utc7Time.format(formatter);
    }

    /**
     * API endpoint để lưu thông tin chi tiết sản phẩm
     * Xử lý thêm mới, cập nhật và xử lý trùng lặp chi tiết sản phẩm
     */
    public ResponseEntity<?> saveChiTietSanPham(@Valid @RequestBody ChiTietSanPhamRequest chiTietSanPhamRequest,
            BindingResult result) {
        
        // Kiểm tra lỗi validation
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            // Lấy dữ liệu liên quan (các entity cần thiết)
            KichThuoc kichThuoc = kichThuocRepo.findById(chiTietSanPhamRequest.getId_kich_thuoc())
                    .orElseThrow(() -> new ResourceNotFoundException("KichThuoc", "id",
                            chiTietSanPhamRequest.getId_kich_thuoc()));

            MauSac mauSac = mauSacRepo.findById(chiTietSanPhamRequest.getId_mau_sac())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("MauSac", "id", chiTietSanPhamRequest.getId_mau_sac()));

            SanPham sanPham = sanPhamRepo.findById(chiTietSanPhamRequest.getId_san_pham())
                    .orElseThrow(() -> new ResourceNotFoundException("SanPham", "id",
                            chiTietSanPhamRequest.getId_san_pham()));

            // Biến để lưu trữ chi tiết sản phẩm cần xử lý
            ChiTietSanPham chiTietSanPham = null;
            String message = "";

            // CASE 1: Cập nhật nếu có ID chi tiết sản phẩm
            if (chiTietSanPhamRequest.getId_chi_tiet_san_pham() != null &&
                    chiTietSanPhamRepo.existsById(chiTietSanPhamRequest.getId_chi_tiet_san_pham())) {

                chiTietSanPham = chiTietSanPhamRepo.findById(chiTietSanPhamRequest.getId_chi_tiet_san_pham()).get();
                message = "Cập nhật chi tiết sản phẩm thành công";
                
                // Cập nhật các thuộc tính
                chiTietSanPham.setGia_ban(chiTietSanPhamRequest.getGia_ban());
                chiTietSanPham.setSo_luong(chiTietSanPhamRequest.getSo_luong());
                chiTietSanPham.setQr_code(chiTietSanPhamRequest.getQr_code());

            } else {
                // CASE 2: Nếu không có ID, kiểm tra trùng lặp thuộc tính (màu và size) để cập nhật số lượng
                Optional<ChiTietSanPham> existingByAttributes = chiTietSanPhamRepo
                        .findByIdSanPhamIdMauSacIdKichThuoc(
                                chiTietSanPhamRequest.getId_san_pham(),
                                chiTietSanPhamRequest.getId_mau_sac(),
                                chiTietSanPhamRequest.getId_kich_thuoc());

                if (existingByAttributes.isPresent()) {
                    chiTietSanPham = existingByAttributes.get();
                    int oldQuantity = chiTietSanPham.getSo_luong();
                    chiTietSanPham.setSo_luong(oldQuantity + chiTietSanPhamRequest.getSo_luong());
                    message = "Cập nhật số lượng chi tiết sản phẩm thành công";
                }
                // CASE 3: Tạo mới hoàn toàn
                else {
                    chiTietSanPham = new ChiTietSanPham();
                    chiTietSanPham.setGia_ban(chiTietSanPhamRequest.getGia_ban());
                    chiTietSanPham.setSo_luong(chiTietSanPhamRequest.getSo_luong());
                    chiTietSanPham.setQr_code(chiTietSanPhamRequest.getQr_code());
                    chiTietSanPham.setNgay_tao(new Date());
                    message = "Thêm mới chi tiết sản phẩm thành công";
                }
            }

            // Cập nhật các thông tin chung cho cả trường hợp thêm mới và cập nhật
            chiTietSanPham.setMauSac(mauSac);
            chiTietSanPham.setKichThuoc(kichThuoc);
            chiTietSanPham.setSanPham(sanPham);
            chiTietSanPham.setNgay_sua(new Date());
            
            // Tự động cập nhật trạng thái dựa trên số lượng
            chiTietSanPham.setTrang_thai(chiTietSanPham.getSo_luong() > 0);

            // Lưu chi tiết sản phẩm để có ID
            ChiTietSanPham savedProduct = chiTietSanPhamRepo.save(chiTietSanPham);

            // Xử lý hình ảnh (luôn xóa cũ và thêm mới để đảm bảo đồng bộ)
            updateAndSaveImages(savedProduct, chiTietSanPhamRequest.getHinh_anh());

            // Chuẩn bị response
            Map<String, Object> response = new HashMap<>();
            response.put("message", message);
            response.put("data", savedProduct);

            return ResponseEntity.ok().body(response);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Log lỗi chi tiết
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi phía máy chủ: " + e.getMessage());
        }
    }

    /**
     * Phương thức duy nhất để xử lý ảnh: Xóa tất cả ảnh cũ và thêm lại từ danh sách mới.
     * Cách tiếp cận "Source of Truth", đảm bảo trạng thái DB khớp với FE.
     */
    private void updateAndSaveImages(ChiTietSanPham product, List<String> newImagePaths) {
        // 1. Xóa tất cả các ảnh cũ liên quan đến chi tiết sản phẩm này
        // Sử dụng một phương thức xóa tùy chỉnh trong repository
        hinhAnhSanPhamRepo.deleteByChiTietSanPham(product);

        // 2. Thêm lại các ảnh từ danh sách mới, đảm bảo đúng thứ tự và ảnh chính
        if (newImagePaths != null && !newImagePaths.isEmpty()) {
            boolean firstImage = true;
            for (String path : newImagePaths) {
                HinhAnhSanPham newImage = new HinhAnhSanPham();
                newImage.setChiTietSanPham(product);
                newImage.setHinh_anh(path);
                newImage.setAnh_chinh(firstImage); // Ảnh đầu tiên trong list là ảnh chính
                hinhAnhSanPhamRepo.save(newImage);
                firstImage = false; // Chỉ ảnh đầu tiên là ảnh chính
            }
        }
    }

    /**
     * Lưu hình ảnh cho sản phẩm mới
     */
    /**
     * Class xử lý ngoại lệ khi tài nguyên không tìm thấy
     */
    public class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
            super(String.format("%s không tìm thấy với %s: '%s'", resourceName, fieldName, fieldValue));
        }
    }



    public String deleteChiTietSanPham(@PathVariable Integer id) {
        ChiTietSanPham ctspDelete = new ChiTietSanPham();
        for (ChiTietSanPham ctsp : chiTietSanPhamRepo.findAll()) {
            if (ctsp.getId_chi_tiet_san_pham() == id) {
                ctspDelete = ctsp;
                ctspDelete.setTrang_thai(false);
            }
        }
        chiTietSanPhamRepo.save(ctspDelete);
        return "Xóa thành công";
    }

    public ResponseEntity<?> chuyenTrangThai(@RequestParam("id") Integer id) {
        ChiTietSanPham ctspDelete = new ChiTietSanPham();
        int countHoatDong = 0;
        ArrayList<ChiTietSanPham> listTam = new ArrayList<>();
        listTam.clear();
        for (ChiTietSanPham ctsp : chiTietSanPhamRepo.findAll()) {
            if (ctsp.getId_chi_tiet_san_pham() == id) {
                ctspDelete = ctsp;
            }
        }
        SanPham sanPham = sanPhamRepo.findById(ctspDelete.getSanPham().getId_san_pham()).get();
        Boolean sanPhamTrangThaiCu = sanPham.getTrang_thai(); // Lưu trạng thái cũ
        
        if (sanPham.getTrang_thai()) {
            if (ctspDelete.getTrang_thai()) {
                ctspDelete.setTrang_thai(false);
                chiTietSanPhamRepo.save(ctspDelete);
            } else {
                ctspDelete.setTrang_thai(true);
                chiTietSanPhamRepo.save(ctspDelete);
            }
            for (ChiTietSanPham ctsp : chiTietSanPhamRepo.findAll()) {
                if (ctsp.getSanPham().getId_san_pham().equals(sanPham.getId_san_pham())) {
                    listTam.add(ctsp);
                }
            }
            for (ChiTietSanPham ctspLoad : listTam) {
                if (ctspLoad.getTrang_thai()) {
                    countHoatDong++;
                }
            }
            if (countHoatDong == 0) {
                sanPhamService.chuyenTrangThai(sanPham.getId_san_pham());
                // Reload sản phẩm sau khi thay đổi trạng thái
                sanPham = sanPhamRepo.findById(sanPham.getId_san_pham()).get();
            }
        } else {
            // Nếu người dùng đang cố gắng kích hoạt CTSP
            if (!ctspDelete.getTrang_thai()) {
                // Kích hoạt CTSP và sản phẩm cha
                ctspDelete.setTrang_thai(true);
                chiTietSanPhamRepo.save(ctspDelete);
                sanPham.setTrang_thai(true);
                sanPhamRepo.save(sanPham);
            }
            // Nếu người dùng đang cố gắng vô hiệu hóa CTSP, giữ nguyên trạng thái sản phẩm cha
            else {
                ctspDelete.setTrang_thai(false);
                chiTietSanPhamRepo.save(ctspDelete);
                // Không thay đổi trạng thái sản phẩm cha
            }
        }

        // Tạo response object chứa cả CTSP và thông tin sản phẩm cha
        Map<String, Object> response = new HashMap<>();
        response.put("chiTietSanPham", ctspDelete);
        
        // Thêm thông tin sản phẩm cha nếu trạng thái đã thay đổi
        if (!sanPhamTrangThaiCu.equals(sanPham.getTrang_thai())) {
            Map<String, Object> sanPhamInfo = new HashMap<>();
            sanPhamInfo.put("id_san_pham", sanPham.getId_san_pham());
            sanPhamInfo.put("trang_thai", sanPham.getTrang_thai());
            response.put("sanPham", sanPhamInfo);
        }
        
        return ResponseEntity.ok(response);
    }

    public ArrayList<ChiTietSanPhamView> listTimKiem(@RequestParam(name = "keywork") String keyword) {
        ArrayList<ChiTietSanPhamView> listTam = new ArrayList<>();
        for (ChiTietSanPhamView ctsp : chiTietSanPhamRepo.listCTSP()) {
            if (ctsp.getTen_san_pham().toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT)) ||
                    ctsp.getTen_chat_lieu().toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT)) ||
                    ctsp.getTen_danh_muc().toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT)) ||
                    ctsp.getTen_thuong_hieu().toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT))) {
                listTam.add(ctsp);
            }
        }
        return listTam;
    }

    public ArrayList<ChiTietSanPhamView> listLocCTSP(String tenSanPham, float giaBanMin, float giaBanMax,
            Integer soLuongMin, Integer soLuongMax, String trangThai,
            String tenMauSac, String tenDanhMuc, String tenThuongHieu, String tenChatLieu) {
        return chiTietSanPhamRepo.listLocCTSP(tenSanPham, giaBanMin, giaBanMax, soLuongMin, soLuongMax,
                trangThai, tenMauSac, tenDanhMuc, tenThuongHieu, tenChatLieu);
        // Thiếu Kích thước
    }

    public BigDecimal getMaxGiaBan() {
        BigDecimal maxGiaBan = BigDecimal.ZERO;
        for (ChiTietSanPham ctsp : chiTietSanPhamRepo.findAll()) {
            BigDecimal giaBan = ctsp.getGia_ban();
            if (giaBan != null && giaBan.compareTo(maxGiaBan) > 0) {
                maxGiaBan = giaBan;
            }
        }
        return maxGiaBan;
    }

    public Page<ChiTietSanPhamView> sapXep(Pageable pageable) {
        return chiTietSanPhamRepo.listPhanTrangChiTietSanPham(pageable);
    }

    // @Cacheable(value = "ctspBySP", key = "#idSanPham")
    public ArrayList<ChiTietSanPhamView> listCTSPTheoSanPham(@RequestParam("id") Integer id) {
        return chiTietSanPhamRepo.listCTSPFolowSanPham(id);
    }

    public List<ChiTietSanPhamView> getCTSPBySanPhamFull(@RequestParam("idSanPham") Integer idSanPham) {
        return chiTietSanPhamRepo.getCTSPBySanPhamFull(idSanPham);
    }

    // @CacheEvict(value = "ctspBySp", key = "#idSanPham")
    // @Caching(evict = {
    // @CacheEvict(value = "ctspBySp", allEntries = true),
    // @CacheEvict(value = "products", key = "'allSanPham'")
    // })
    public ResponseEntity<?> changeAllCTSPKhongHoatDong(@RequestParam("id") Integer id) {
        ArrayList<ChiTietSanPham> listTam = new ArrayList<>();
        int countCTSP = 0;
        listTam.clear();
        System.out.println("Chuyen ctsp khong hoat dong");
        ChiTietSanPham ctsp = chiTietSanPhamRepo.findById(id).get();
        System.out.println("idTatCaCTSPKhongHoatDOng" + ctsp.getId_chi_tiet_san_pham());
        ctsp.setTrang_thai(false);
        chiTietSanPhamRepo.save(ctsp);
        if(chiTietSanPhamRepo.findById(id).get().getTrang_thai() == true){
            return ResponseEntity.badRequest().body("Chuyển trạng thái không thành công");

        } else {
            SanPham sanPham = sanPhamRepo.findById(ctsp.getSanPham().getId_san_pham()).get();
            Boolean sanPhamTrangThaiCu = sanPham.getTrang_thai();
            
            for (ChiTietSanPham ctspXet : chiTietSanPhamRepo.findAll()) {
                if (ctspXet.getSanPham().getId_san_pham().equals(sanPham.getId_san_pham())) {
                    listTam.add(ctspXet);
                }
            }
            for (ChiTietSanPham ctspChuyen : listTam) {
                if (ctspChuyen.getTrang_thai()) {
                    countCTSP++;
                }
            }
            if (countCTSP == 0) {
                sanPham.setTrang_thai(false);
                sanPhamRepo.save(sanPham);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("chiTietSanPham", ctsp);
            
            if (!sanPhamTrangThaiCu.equals(sanPham.getTrang_thai())) {
                Map<String, Object> sanPhamInfo = new HashMap<>();
                sanPhamInfo.put("id_san_pham", sanPham.getId_san_pham());
                sanPhamInfo.put("trang_thai", sanPham.getTrang_thai());
                response.put("sanPham", sanPhamInfo);
            }
            
            return ResponseEntity.ok(response);
        }
        
    }

    // @Caching(evict = {
    // @CacheEvict(value = "ctspBySp", allEntries = true),
    // @CacheEvict(value = "products", key = "'allSanPham'")
    // })
    public ResponseEntity<?> changeAllCTSPHoatDong(@RequestParam("id") Integer id) {
        ArrayList<ChiTietSanPham> listTam = new ArrayList<>();
        int countCTSP = 0;
        listTam.clear();
        System.out.println("Chuyen ctsp khong hoat dong");
        ChiTietSanPham ctsp = chiTietSanPhamRepo.findById(id).get();
        System.out.println("idTatCaCTSPHoatDOng" + ctsp.getId_chi_tiet_san_pham());
        ctsp.setTrang_thai(true);
        chiTietSanPhamRepo.save(ctsp);
        if (chiTietSanPhamRepo.findById(id).get().getTrang_thai() == false) {
            return ResponseEntity.badRequest().body("Chuyển trạng thái không thành công");

        } else {
            SanPham sanPham = sanPhamRepo.findById(ctsp.getSanPham().getId_san_pham()).get();
            Boolean sanPhamTrangThaiCu = sanPham.getTrang_thai();
            
            for (ChiTietSanPham ctspXet : chiTietSanPhamRepo.findAll()) {
                if (ctspXet.getSanPham().getId_san_pham().equals(sanPham.getId_san_pham())) {
                    listTam.add(ctspXet);
                }
            }
            for (ChiTietSanPham ctspChuyen : listTam) {
                if (ctspChuyen.getTrang_thai()) {
                    countCTSP++;
                }
            }
            if (countCTSP >= 0) {
                sanPham.setTrang_thai(true);
                sanPhamRepo.save(sanPham);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("chiTietSanPham", ctsp);
            
            if (!sanPhamTrangThaiCu.equals(sanPham.getTrang_thai())) {
                Map<String, Object> sanPhamInfo = new HashMap<>();
                sanPhamInfo.put("id_san_pham", sanPham.getId_san_pham());
                sanPhamInfo.put("trang_thai", sanPham.getTrang_thai());
                response.put("sanPham", sanPhamInfo);
            }
            
            return ResponseEntity.ok(response);
        }
    }

    public ResponseEntity<List<SanPhamView>> timKiemVaLoc(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "tenSanPham", required = false) String tenSanPham,
            @RequestParam(name = "giaBanMin", required = false) Float giaBanMin,
            @RequestParam(name = "giaBanMax", required = false) Float giaBanMax,
            @RequestParam(name = "soLuongMin", required = false) Integer soLuongMin,
            @RequestParam(name = "soLuongMax", required = false) Integer soLuongMax,
            @RequestParam(name = "trangThai", required = false) String trangThai,
            @RequestParam(name = "listMauSac", required = false) List<String> listMauSac,
            @RequestParam(name = "listDanhMuc", required = false) List<String> listDanhMuc,
            @RequestParam(name = "listThuongHieu", required = false) List<String> listThuongHieu,
            @RequestParam(name = "listChatLieu", required = false) List<String> listChatLieu,
            @RequestParam(name = "listKichThuoc", required = false) List<String> listKichThuoc) {

        List<ChiTietSanPhamView> danhSachSanPham = chiTietSanPhamRepo.listCTSP();
        Stream<ChiTietSanPhamView> finalStream = danhSachSanPham.stream();

        // Áp dụng từng bộ lọc
        if (!isEmpty(keyword)) {
            String keywordLowercase = keyword.toLowerCase(Locale.ROOT);
            finalStream = finalStream.filter(ctsp -> (ctsp.getTen_san_pham() != null
                    && ctsp.getTen_san_pham().toLowerCase(Locale.ROOT).contains(keywordLowercase)) ||
                    (ctsp.getMa_san_pham() != null
                            && ctsp.getMa_san_pham().toLowerCase(Locale.ROOT).contains(keywordLowercase))
                    ||
                    (ctsp.getTen_chat_lieu() != null
                            && ctsp.getTen_chat_lieu().toLowerCase(Locale.ROOT).contains(keywordLowercase))
                    ||
                    (ctsp.getTen_danh_muc() != null
                            && ctsp.getTen_danh_muc().toLowerCase(Locale.ROOT).contains(keywordLowercase))
                    ||
                    (ctsp.getTen_thuong_hieu() != null
                            && ctsp.getTen_thuong_hieu().toLowerCase(Locale.ROOT).contains(keywordLowercase))
                    ||
                    (ctsp.getTen_mau_sac() != null
                            && ctsp.getTen_mau_sac().toLowerCase(Locale.ROOT).contains(keywordLowercase))
                    ||
                    (ctsp.getGia_tri() != null
                            && ctsp.getGia_tri().toLowerCase(Locale.ROOT).contains(keywordLowercase)));
        }

        if (!isEmpty(tenSanPham)) {
            String tenSanPhamLowercase = tenSanPham.toLowerCase(Locale.ROOT);
            finalStream = finalStream.filter(ctsp -> ctsp.getTen_san_pham() != null &&
                    ctsp.getTen_san_pham().toLowerCase(Locale.ROOT).contains(tenSanPhamLowercase));
        }

        if (giaBanMin != null) {
            finalStream = finalStream
                    .filter(ctsp -> ctsp.getGia_ban() != null && ctsp.getGia_ban().floatValue() >= giaBanMin);
        }

        if (giaBanMax != null) {
            finalStream = finalStream
                    .filter(ctsp -> ctsp.getGia_ban() != null && ctsp.getGia_ban().floatValue() <= giaBanMax);
        }

        if (soLuongMin != null) {
            finalStream = finalStream.filter(ctsp -> ctsp.getSo_luong() != null && ctsp.getSo_luong() >= soLuongMin);
        }

        if (soLuongMax != null) {
            finalStream = finalStream.filter(ctsp -> ctsp.getSo_luong() != null && ctsp.getSo_luong() <= soLuongMax);
        }
        // Todo
        if (!isEmpty(trangThai)) {
            // Chuyển đổi giá trị chuỗi đầu vào thành giá trị Boolean
            Boolean trangThaiBoolean;
            if ("true".equalsIgnoreCase(trangThai) || "1".equals(trangThai)
                    || "Hoạt động".equalsIgnoreCase(trangThai)) {
                trangThaiBoolean = true;
            } else {
                trangThaiBoolean = false;
            }

            // So sánh với trường trang_thai kiểu Boolean
            finalStream = finalStream.filter(ctsp -> ctsp.getTrang_thai() != null &&
                    ctsp.getTrang_thai().equals(trangThaiBoolean));
        }

        if (!isEmpty(listMauSac)) {
            finalStream = finalStream.filter(ctsp -> {
                if (ctsp.getTen_mau_sac() == null)
                    return false;
                String mauSacValue = ctsp.getTen_mau_sac().trim();
                return listMauSac.stream().anyMatch(ms -> ms != null && ms.trim().equalsIgnoreCase(mauSacValue));
            });
        }

        if (!isEmpty(listDanhMuc)) {
            finalStream = finalStream.filter(ctsp -> {
                if (ctsp.getTen_danh_muc() == null)
                    return false;
                String danhMucValue = ctsp.getTen_danh_muc().trim();
                return listDanhMuc.stream().anyMatch(dm -> dm != null && dm.trim().equalsIgnoreCase(danhMucValue));
            });
        }

        if (!isEmpty(listThuongHieu)) {
            finalStream = finalStream.filter(ctsp -> {
                if (ctsp.getTen_thuong_hieu() == null)
                    return false;
                String thuongHieuValue = ctsp.getTen_thuong_hieu().trim();
                return listThuongHieu.stream()
                        .anyMatch(th -> th != null && th.trim().equalsIgnoreCase(thuongHieuValue));
            });
        }

        if (!isEmpty(listChatLieu)) {
            finalStream = finalStream.filter(ctsp -> {
                if (ctsp.getTen_chat_lieu() == null)
                    return false;
                String chatLieuValue = ctsp.getTen_chat_lieu().trim();
                return listChatLieu.stream().anyMatch(cl -> cl != null && cl.trim().equalsIgnoreCase(chatLieuValue));
            });
        }

        if (!isEmpty(listKichThuoc)) {
            finalStream = finalStream.filter(ctsp -> {
                if (ctsp.getGia_tri() == null)
                    return false;
                String giaTriValue = ctsp.getGia_tri().trim();
                return listKichThuoc.stream().anyMatch(kt -> kt != null && kt.trim().equalsIgnoreCase(giaTriValue));
            });
        }

        // Chuyển stream trở lại thành danh sách
        List<ChiTietSanPhamView> ketQua = finalStream.collect(Collectors.toList());

        if (ketQua.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<Integer> listIDCTSP = ketQua.stream()
                .map(ChiTietSanPhamView::getId_chi_tiet_san_pham)
                .distinct()
                .collect(Collectors.toList());

        if (listIDCTSP.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<SanPhamView> listReturn = sanPhamRepo.getSanPhamByListCTSP(listIDCTSP);
        return ResponseEntity.ok(listReturn);
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    // Phương thức tiện ích để kiểm tra danh sách trống hoặc null
    private boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
}
