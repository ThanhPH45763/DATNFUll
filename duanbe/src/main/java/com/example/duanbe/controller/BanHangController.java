package com.example.duanbe.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.duanbe.entity.ChiTietKhuyenMai;
import com.example.duanbe.entity.ChiTietSanPham;
import com.example.duanbe.entity.HoaDon;
import com.example.duanbe.entity.HoaDonChiTiet;
import com.example.duanbe.entity.KhachHang;
import com.example.duanbe.entity.TheoDoiDonHang;
import com.example.duanbe.entity.Voucher;
import com.example.duanbe.repository.ChiTietKhuyenMaiRepo;
import com.example.duanbe.repository.ChiTietSanPhamRepo;
import com.example.duanbe.repository.HoaDonChiTietRepo;
import com.example.duanbe.repository.HoaDonRepo;
import com.example.duanbe.repository.KhachHangRepo;
import com.example.duanbe.repository.TheoDoiDonHangRepo;
import com.example.duanbe.repository.VoucherRepository;
import com.example.duanbe.response.ChiTietSanPhamView;
import com.example.duanbe.response.HoaDonChiTietResponse;
import com.example.duanbe.response.HoaDonResponse;
import com.example.duanbe.response.VoucherBHResponse;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE })
@RequestMapping("/banhang")

public class BanHangController {
    @Autowired
    private HoaDonRepo hoaDonRepo;

    @Autowired
    private ChiTietSanPhamRepo chiTietSanPhamRepo;

    @Autowired
    private KhachHangRepo khachHangRepo;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private HoaDonChiTietRepo hoaDonChiTietRepo;

    @Autowired
    private TheoDoiDonHangRepo theoDoiDonHangRepo;

    @Autowired
    private ChiTietKhuyenMaiRepo chiTietKhuyenMaiRepo;

    // Integer idHD = null;
    // Integer idCTSP = null;
    // Integer idNV = null;

    // public void viewALl(Model model) {
    // model.addAttribute("listHoaDon", hoaDonRepo.getAllHoaDonCTT());
    // model.addAttribute("listCTSP", chiTietSanPhamRepo.listCTSP());
    // model.addAttribute("listKH", khachHangRepo.findAll());
    // model.addAttribute("listVC", voucherRepository.findAll());
    // model.addAttribute("listNV", nhanVienRepo.findAll());
    // if (idHD == null) {
    // model.addAttribute("hdbh", null);
    // } else {
    // model.addAttribute("hdbh", hoaDonRepo.findHoaDonById(idHD).get(0));
    // model.addAttribute("listGH", hoaDonChiTietRepo.getSPGH(idHD));
    // }
    // if (idCTSP == null) {
    // model.addAttribute("slgh", null);
    // } else {
    // ChiTietSanPham ct = new ChiTietSanPham();
    // for (ChiTietSanPham ctsp : chiTietSanPhamRepo.findAll()) {
    // if (idCTSP == ctsp.getId_chi_tiet_san_pham()) {
    // ct = ctsp;``
    // }
    // }
    // model.addAttribute("slgh", ct);
    // }
    // }
    //
    @PostMapping("/addKhHD")
    public ResponseEntity<?> addKhHd(
            @RequestParam(value = "idKH", required = false) String idKHStr,
            @RequestParam("idHD") Integer idHD,
            @RequestParam("diaChi") String diaChi,
            @RequestParam("tenKhachHang") String tenKhachHang,
            @RequestParam("soDienThoai") String soDienThoai,
            @RequestParam("email") String email) {
        try {
            System.out.println("=== API addKhHD được gọi ===");
            System.out.println("idKH: " + idKHStr);
            System.out.println("idHD: " + idHD);
            System.out.println("tenKhachHang: " + tenKhachHang);
            System.out.println("soDienThoai: " + soDienThoai);
            System.out.println("email: " + email);
            System.out.println("diaChi: " + diaChi);

            HoaDon hoaDon = hoaDonRepo.findById(idHD)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            if (idKHStr != null && !idKHStr.equals("null") && !idKHStr.isEmpty()) {
                try {
                    Integer idKH = Integer.valueOf(idKHStr);
                    KhachHang khachHang = khachHangRepo.findById(idKH)
                            .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
                    hoaDon.setKhachHang(khachHang);
                    hoaDon.setHo_ten(khachHang.getHoTen());
                    hoaDon.setSdt(khachHang.getSoDienThoai());
                    hoaDon.setDia_chi(diaChi);
                    hoaDon.setEmail(khachHang.getEmail());
                    System.out.println("→ Lưu KHÁCH CÓ TK");
                } catch (NumberFormatException ex) {
                    // Nếu idKH không phải là số, coi như nhập khách hàng mới
                    hoaDon.setKhachHang(null);
                    hoaDon.setHo_ten(tenKhachHang);
                    hoaDon.setSdt(soDienThoai);
                    hoaDon.setDia_chi(diaChi);
                    hoaDon.setEmail(email);
                    System.out.println("→ Lưu KHÁCH LẺ (idKH không parse được)");
                }
            } else {
                hoaDon.setKhachHang(null);
                hoaDon.setHo_ten(tenKhachHang);
                hoaDon.setSdt(soDienThoai);
                hoaDon.setDia_chi(diaChi);
                hoaDon.setEmail(email);
                System.out.println("→ Lưu KHÁCH LẺ (idKH = null)");
            }

            hoaDonRepo.save(hoaDon);
            System.out.println("✅ Đã lưu hóa đơn vào DB");
            return ResponseEntity.ok("Cập nhật khách hàng cho hóa đơn thành công!");
        } catch (Exception e) {
            System.err.println("❌ Lỗi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật khách hàng cho hóa đơn: " + e.getMessage());
        }
    }

    // ✅ NEW: Endpoint riêng để update thông tin khách hàng vào hóa đơn
    @PostMapping("/updateCustomerInfo")
    public ResponseEntity<?> updateCustomerInfo(
            @RequestParam("idHD") Integer idHD,
            @RequestParam("tenKhachHang") String tenKhachHang,
            @RequestParam("soDienThoai") String soDienThoai,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "diaChi", required = false) String diaChi) {
        try {
            System.out.println("=== API updateCustomerInfo được gọi ===");
            System.out.println("idHD: " + idHD);
            System.out.println("tenKhachHang: " + tenKhachHang);
            System.out.println("soDienThoai: " + soDienThoai);
            System.out.println("email: " + email);
            System.out.println("diaChi: " + diaChi);

            HoaDon hoaDon = hoaDonRepo.findById(idHD)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            // ✅ Lưu thông tin khách lẻ
            hoaDon.setKhachHang(null); // id_khach_hang = NULL
            hoaDon.setHo_ten(tenKhachHang);
            hoaDon.setSdt(soDienThoai);
            hoaDon.setEmail(email != null && !email.isEmpty() ? email : null);
            hoaDon.setDia_chi(diaChi != null && !diaChi.isEmpty() ? diaChi : null);

            hoaDonRepo.save(hoaDon);
            System.out.println("✅ ĐÃ LƯU THÔNG TIN KHÁCH HÀNG VÀO DB");

            return ResponseEntity.ok("Cập nhật thông tin khách hàng thành công!");
        } catch (Exception e) {
            System.err.println("❌ Lỗi updateCustomerInfo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/trangThaiDonHang")
    public String trangThaiDonHang(@RequestParam("idHD") Integer idHD) {
        try {
            HoaDon hoaDon = hoaDonRepo.findById(idHD)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            // ✅ LOGIC MỚI: Set trạng thái dựa trên loại hóa đơn và phương thức nhận
            String loaiHoaDon = hoaDon.getLoai_hoa_don();
            String phuongThucNhanHang = hoaDon.getPhuong_thuc_nhan_hang();
            String trangThaiMoi = "";

            if ("Offline".equals(loaiHoaDon)) {
                // Offline - Thanh toán tại quầy
                if ("Nhận tại cửa hàng".equals(phuongThucNhanHang)) {
                    // Trường hợp 1: Offline + Nhận tại cửa hàng
                    // → Đã thanh toán + Đã nhận hàng → HOÀN THÀNH
                    trangThaiMoi = "Hoàn thành";
                    hoaDon.setTrang_thai(trangThaiMoi);
                } else {
                    // Trường hợp 2: Offline + Giao hàng
                    // → Đã thanh toán nhưng CHƯA giao → ĐÃ XÁC NHẬN
                    trangThaiMoi = "Đã xác nhận";
                    hoaDon.setTrang_thai(trangThaiMoi);
                }
            } else {
                // Trường hợp 3: Online (đã được xử lý trong callback ZaloPay/PayOS)
                // → Đã thanh toán online nhưng CHƯA giao → ĐÃ XÁC NHẬN
                trangThaiMoi = "Đã xác nhận";
                hoaDon.setTrang_thai(trangThaiMoi);
            }

            hoaDonRepo.save(hoaDon);

            // ✅ THÊM: Insert/Update bảng theo_doi_don_hang
            if ("Đã xác nhận".equals(trangThaiMoi)) {
                TheoDoiDonHang tracking = new TheoDoiDonHang();
                tracking.setHoaDon(hoaDon);
                tracking.setTrang_thai("Đã xác nhận");
                tracking.setNgay_chuyen(LocalDateTime.now());
                tracking.setNoi_dung_doi("Đơn hàng đã được xác nhận và chờ giao hàng");
                theoDoiDonHangRepo.save(tracking);
                System.out.println("✅ Đã tạo theo dõi đơn hàng: Đã xác nhận");
            }

            return "Cập nhật trạng thái hóa đơn thành công!";
        } catch (Exception e) {
            return "Lỗi khi cập nhật trạng thái hóa đơn: " + e.getMessage();
        }
    }

    @PostMapping("/removeCustomerFromInvoice")
    public ResponseEntity<?> removeCustomerFromInvoice(@RequestParam("idHD") Integer idHD) {
        try {
            HoaDon hoaDon = hoaDonRepo.findById(idHD)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            // Reset thông tin khách hàng về khách lẻ
            hoaDon.setKhachHang(null);
            hoaDon.setHo_ten("Khách lẻ");
            hoaDon.setSdt(null);
            hoaDon.setDia_chi(null);
            hoaDon.setEmail(null);

            // Reset phương thức nhận hàng về nhận tại cửa hàng
            hoaDon.setPhuong_thuc_nhan_hang("Nhận tại cửa hàng");
            hoaDon.setPhi_van_chuyen(BigDecimal.ZERO);

            hoaDonRepo.save(hoaDon);

            // Cập nhật lại tổng tiền sau khi bỏ phí vận chuyển
            updateTongTienHoaDon(idHD);

            return ResponseEntity.ok("Đã bỏ chọn khách hàng và reset về khách lẻ!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi bỏ chọn khách hàng: " + e.getMessage());
        }
    }

    @PostMapping("/setTrangThaiNhanHang")
    public ResponseEntity<?> setTrangThaiNhanHang(
            @RequestParam("idHD") Integer idHD,
            @RequestParam("phuongThucNhanHang") String ptnh,
            @RequestParam("phiVanChuyen") BigDecimal pvc) {
        try {
            HoaDon hoaDon = hoaDonRepo.findById(idHD)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với id: " + idHD));

            hoaDon.setPhuong_thuc_nhan_hang(ptnh);

            if ("Giao hàng".equalsIgnoreCase(ptnh)) {
                hoaDon.setPhi_van_chuyen(pvc);
            } else if ("Nhận tại cửa hàng".equalsIgnoreCase(ptnh)) {
                hoaDon.setDia_chi(null);
                hoaDon.setPhi_van_chuyen(BigDecimal.ZERO);
            }

            // ✅ LƯU PHÍ SHIP
            hoaDonRepo.save(hoaDon);

            // ✅ GỌI updateTongTienHoaDon để tính lại ĐÚNG
            // (tongTienTruocGiam = CHỈ sản phẩm, KHÔNG cộng ship)
            updateTongTienHoaDon(idHD);

            return ResponseEntity.ok("Cập nhật phương thức nhận hàng và tính tổng tiền thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật phương thức nhận hàng: " + e.getMessage());
        }
    }

    @GetMapping("/getAllHoaDonCTT")
    public List<HoaDonResponse> getAllHDCTT() {
        return hoaDonRepo.getAllHoaDonCTT();
    }

    @GetMapping("/getHoaDonByIdHoaDon")
    public HoaDonResponse getHoaDonByIdHoaDon(@RequestParam("idHD") Integer idHD) {
        return hoaDonRepo.findHoaDonById(idHD).get(0);
    }

    @GetMapping("/createHoaDon")
    public ResponseEntity<?> createHoaDon() {
        try {
            // 1. Validate input
            // 3. Create new invoice
            HoaDon newHoaDon = new HoaDon();
            newHoaDon.setMa_hoa_don(generateUniqueMaHoaDon());
            newHoaDon.setNgay_tao(LocalDateTime.now());
            newHoaDon.setTrang_thai("Đang chờ");
            newHoaDon.setLoai_hoa_don("Offline");
            newHoaDon.setHinh_thuc_thanh_toan("Tiền mặt");
            newHoaDon.setPhuong_thuc_nhan_hang("Nhận tại cửa hàng");
            newHoaDon.setHo_ten("Khách lẻ");

            // 4. Set default values
            newHoaDon.setTong_tien_truoc_giam(BigDecimal.ZERO);
            newHoaDon.setTong_tien_sau_giam(BigDecimal.ZERO);
            newHoaDon.setPhi_van_chuyen(BigDecimal.ZERO);

            // 5. Save to database
            HoaDon savedHoaDon = hoaDonRepo.save(newHoaDon);
            TheoDoiDonHang theoDoiDonHang = new TheoDoiDonHang();
            theoDoiDonHang.setTrang_thai("Chờ xác nhận");
            theoDoiDonHang.setHoaDon(savedHoaDon);
            theoDoiDonHang.setNgay_chuyen(LocalDateTime.now());
            theoDoiDonHangRepo.save(theoDoiDonHang);
            // 6. Create response DTO
            Map<String, Object> response = new HashMap<>();
            response.put("id_hoa_don", savedHoaDon.getId_hoa_don());
            response.put("ma_hoa_don", savedHoaDon.getMa_hoa_don());
            response.put("ngay_tao", savedHoaDon.getNgay_tao().format(DateTimeFormatter.ISO_DATE_TIME));
            response.put("trang_thai", savedHoaDon.getTrang_thai());

            return ResponseEntity.ok(response);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(
                    Map.of(
                            "error", true,
                            "message", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "error", true,
                            "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @DeleteMapping("/deleteHoaDon")
    @Transactional
    public ResponseEntity<?> deleteHoaDon(@RequestParam(value = "idHoaDon") Integer id) {
        try {
            Optional<HoaDon> hoaDonOpt = hoaDonRepo.findById(id);
            if (hoaDonOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy hóa đơn với ID: " + id));
            }

            HoaDon hoaDon = hoaDonOpt.get();

            // Lưu thông tin trước khi xóa
            List<Map<String, Integer>> productUpdates = new ArrayList();
            for (HoaDonChiTiet chiTiet : hoaDon.getDanhSachChiTiet()) {
                Map<String, Integer> update = new HashMap<>();
                update.put("idCTSP", chiTiet.getChiTietSanPham().getId_chi_tiet_san_pham());
                update.put("soLuong", chiTiet.getSo_luong());
                productUpdates.add(update);
            }

            // Lưu thông tin voucher
            Integer idVoucher = null;
            if (hoaDon.getVoucher() != null && "Đang diễn ra".equalsIgnoreCase(hoaDon.getVoucher().getTrangThai())) {
                idVoucher = hoaDon.getVoucher().getId();
            }

            // Xóa hóa đơn trước (cascade sẽ xóa chi tiết)
            hoaDonRepo.delete(hoaDon);
            hoaDonRepo.flush(); // Đảm bảo xóa được thực thi ngay

            // Cập nhật lại số lượng tồn sản phẩm sau khi xóa
            for (Map<String, Integer> update : productUpdates) {
                ChiTietSanPham ctsp = chiTietSanPhamRepo.findById(update.get("idCTSP")).orElse(null);
                if (ctsp != null) {
                    ctsp.setSo_luong(ctsp.getSo_luong() + update.get("soLuong"));
                    chiTietSanPhamRepo.save(ctsp);
                }
            }

            // Cập nhật voucher nếu có
            if (idVoucher != null) {
                Voucher voucher = voucherRepository.findById(idVoucher).orElse(null);
                if (voucher != null) {
                    voucher.setSoLuong(voucher.getSoLuong() + 1);
                    voucherRepository.save(voucher);
                }
            }

            return ResponseEntity
                    .ok(Map.of("success", true, "message", "Đã xóa hóa đơn và cập nhật lại tồn kho, voucher nếu có"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Lỗi khi xóa hóa đơn: " + e.getMessage()));
        }
    }

    // ✅ CẬP NHẬT PHÍ VẬN CHUYỂN
    @PutMapping("/hoa-don/{idHoaDon}/phi-van-chuyen")
    public ResponseEntity<?> updatePhiVanChuyen(
            @PathVariable Integer idHoaDon,
            @RequestParam BigDecimal phiVanChuyen) {
        try {
            HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            hoaDon.setPhi_van_chuyen(phiVanChuyen);
            hoaDonRepo.save(hoaDon);

            System.out.println("✅ Đã cập nhật phí vận chuyển " + phiVanChuyen + " cho hóa đơn " + idHoaDon);

            return ResponseEntity.ok(hoaDon);
        } catch (Exception e) {
            System.err.println("❌ Lỗi cập nhật phí vận chuyển: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/updateHoaDon")
    public ResponseEntity<HoaDonResponse> updateHoaDon(@RequestBody Map<String, Object> payload) {
        Integer idHD = (Integer) payload.get("id_hoa_don");
        if (idHD == null) {
            return ResponseEntity.badRequest().build();
        }

        Object idVoucherObj = payload.get("id_voucher");
        Integer idVoucher = null;
        if (idVoucherObj instanceof Number) {
            idVoucher = ((Number) idVoucherObj).intValue();
        }

        HoaDon hoaDon = hoaDonRepo.findById(idHD)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy hóa đơn với ID: " + idHD));

        Voucher voucher = null;
        if (idVoucher != null) {
            // Fix for effectively final issue
            final Integer finalIdVoucher = idVoucher;
            voucher = voucherRepository.findById(finalIdVoucher)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Không tìm thấy voucher với ID: " + finalIdVoucher));
        }

        hoaDon.setVoucher(voucher);
        hoaDonRepo.save(hoaDon);

        updateTongTienHoaDon(idHD);

        List<HoaDonResponse> responseList = hoaDonRepo.findHoaDonById(idHD);
        if (responseList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể tải lại hóa đơn sau khi cập nhật");
        }
        HoaDonResponse updatedHoaDonResponse = responseList.get(0);

        return ResponseEntity.ok(updatedHoaDonResponse);
    }

    @GetMapping("/getSPHD")
    public List<HoaDonChiTietResponse> getAllSPHD(@RequestParam(value = "idHoaDon") Integer idHD) {
        return hoaDonChiTietRepo.getSPGH(idHD);
    }

    @PostMapping("/themSPHDMoi")
    public ResponseEntity<?> themSPHDMoi(
            @RequestParam("idHoaDon") Integer idHD,
            @RequestParam("idCTSP") Integer idCTSP,
            @RequestParam("soLuong") Integer soLuongInput) {
        try {
            // Kiểm tra hóa đơn
            HoaDon hoaDon = hoaDonRepo.findById(idHD)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại!"));

            // Kiểm tra sản phẩm
            ChiTietSanPham ctsp = chiTietSanPhamRepo.findById(idCTSP)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

            // ✅ 1. Lấy giá HIỆN TẠI của sản phẩm (sau khuyến mãi)
            System.out.println("🔍 Tìm KM cho sản phẩm ID: " + idCTSP);

            // DEBUG: Lấy TẤT CẢ KM cho SP này (không filter) để xem có data không
            List<ChiTietKhuyenMai> allKM = chiTietKhuyenMaiRepo.findAll().stream()
                    .filter(ckm -> ckm.getChiTietSanPham() != null &&
                            ckm.getChiTietSanPham().getId_chi_tiet_san_pham().equals(idCTSP))
                    .toList();
            System.out.println("📊 Tất cả KM cho SP (không filter): " + allKM.size());
            allKM.forEach(ckm -> {
                if (ckm.getKhuyenMai() != null) {
                    System.out.println("  - KM ID: " + ckm.getKhuyenMai().getId());
                    System.out.println("    Trạng thái: " + ckm.getKhuyenMai().getTrangThai());
                    System.out.println("    Ngày bắt đầu: " + ckm.getKhuyenMai().getNgayBatDau());
                    System.out.println("    Ngày hết hạn: " + ckm.getKhuyenMai().getNgayHetHan());
                    System.out.println("    Giá sau giảm: " + ckm.getGiaSauGiam());
                }
            });
            System.out.println("⏰ Thời gian hiện tại: " + new java.util.Date());

            List<ChiTietKhuyenMai> khuyenMais = chiTietKhuyenMaiRepo.findAllByChiTietSanPhamId(idCTSP);
            System.out.println("📦 Số KM tìm thấy (có filter): " + khuyenMais.size());

            Optional<BigDecimal> giaGiamTotNhat = khuyenMais.stream()
                    .map(ChiTietKhuyenMai::getGiaSauGiam)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo);

            BigDecimal donGiaPerUnit = giaGiamTotNhat.orElse(ctsp.getGia_ban());
            System.out.println("💰 Giá gốc: " + ctsp.getGia_ban());
            System.out.println("💰 Giá sau KM: " + donGiaPerUnit);

            // ✅ 2. Kiểm tra tồn kho - tính tổng số lượng đã mua (tất cả các dòng)
            List<HoaDonChiTiet> allItemsOfProduct = hoaDonChiTietRepo
                    .findAllByHoaDonAndChiTietSanPham(idHD, idCTSP);

            int soLuongTonKho = ctsp.getSo_luong();
            int soLuongDaMuaAllPrices = allItemsOfProduct.stream()
                    .mapToInt(HoaDonChiTiet::getSo_luong)
                    .sum();

            int soLuongCoTheMua = soLuongTonKho - soLuongDaMuaAllPrices;
            int soLuong = Math.min(soLuongInput, soLuongCoTheMua);

            if (soLuong <= 0) {
                return ResponseEntity.badRequest()
                        .body("Sản phẩm đã hết hàng hoặc đã đạt giới hạn trong giỏ!");
            }

            // ✅ 3. TÌM sản phẩm với CÙNG ID **VÀ** CÙNG ĐƠN GIÁ
            Optional<HoaDonChiTiet> existingItemWithSamePrice = hoaDonChiTietRepo
                    .findByHoaDonAndChiTietSanPhamAndDonGia(idHD, idCTSP, donGiaPerUnit);

            HoaDonChiTiet chiTiet;

            // ✅ 4. NẾU ĐÃ TỒN TẠI CÙNG GIÁ -> CỘNG SỐ LƯỢNG
            if (existingItemWithSamePrice.isPresent()) {
                chiTiet = existingItemWithSamePrice.get();
                int soLuongMoi = chiTiet.getSo_luong() + soLuong;
                chiTiet.setSo_luong(soLuongMoi);
                // ✅ don_gia = đơn giá * số lượng
                chiTiet.setDon_gia(donGiaPerUnit.multiply(BigDecimal.valueOf(soLuongMoi)));

                System.out.println("✅ Cộng số lượng vào dòng có cùng giá: " + donGiaPerUnit);
            }
            // ✅ 5. NẾU CHƯA TỒN TẠI HOẶC KHÁC GIÁ -> TẠO DÒNG MỚI
            else {
                chiTiet = new HoaDonChiTiet();
                chiTiet.setHoaDon(hoaDon);
                chiTiet.setChiTietSanPham(ctsp);
                chiTiet.setSo_luong(soLuong);
                // ✅ don_gia = đơn giá * số lượng
                chiTiet.setDon_gia(donGiaPerUnit.multiply(BigDecimal.valueOf(soLuong)));

                System.out.println("✅ Thêm dòng mới với giá: " + donGiaPerUnit);
            }

            // ✅ 6. Trừ tồn kho
            ctsp.setSo_luong(ctsp.getSo_luong() - soLuong);
            chiTietSanPhamRepo.save(ctsp);

            // ✅ 7. Lưu chi tiết hóa đơn
            hoaDonChiTietRepo.save(chiTiet);

            // ✅ 8. Cập nhật lại tổng tiền và voucher (hàm này sẽ tính toàn bộ)
            updateTongTienHoaDon(idHD);

            return ResponseEntity.ok(existingItemWithSamePrice.isPresent()
                    ? "Đã cộng số lượng vào dòng có cùng giá"
                    : "Đã thêm dòng mới (giá khác hoặc sản phẩm mới)");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm sản phẩm: " + e.getMessage());
        }
    }

    /**
     * ✅ API mới: Get realtime stock và status của CTSP
     * Gọi trước khi tăng/giảm số lượng để check stock hiện tại
     */
    @GetMapping("/getCTSPRealtime/{idCTSP}")
    public ResponseEntity<?> getCTSPRealtime(@PathVariable Integer idCTSP) {
        try {
            ChiTietSanPham ctsp = chiTietSanPhamRepo.findById(idCTSP)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

            // Trả về thông tin cần thiết
            Map<String, Object> response = new HashMap<>();
            response.put("id_chi_tiet_san_pham", ctsp.getId_chi_tiet_san_pham());
            response.put("so_luong", ctsp.getSo_luong());
            response.put("trang_thai", ctsp.getTrang_thai());
            response.put("trang_thai_san_pham", ctsp.getSanPham().getTrang_thai());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy thông tin sản phẩm: " + e.getMessage());
        }
    }

    /**
     * ✅ API mới: Kiểm tra stock realtime cho TẤT CẢ items trong hóa đơn
     * Gọi khi: switch tab, reload page, trước khi thanh toán
     */
    @GetMapping("/checkCartStock/{idHoaDon}")
    public ResponseEntity<?> checkCartStock(@PathVariable Integer idHoaDon) {
        try {
            List<HoaDonChiTiet> items = hoaDonChiTietRepo.findByIdHoaDon(idHoaDon);
            List<Map<String, Object>> stockStatus = new ArrayList<>();
            List<String> invalidItems = new ArrayList<>();

            for (HoaDonChiTiet item : items) {
                ChiTietSanPham ctsp = item.getChiTietSanPham();
                Map<String, Object> status = new HashMap<>();
                status.put("id", ctsp.getId_chi_tiet_san_pham());
                status.put("name", ctsp.getSanPham().getTen_san_pham());
                status.put("qty_in_cart", item.getSo_luong());
                status.put("stock", ctsp.getSo_luong());
                status.put("ctsp_active", ctsp.getTrang_thai());
                status.put("product_active", ctsp.getSanPham().getTrang_thai());

                boolean isInvalid = !Boolean.TRUE.equals(ctsp.getTrang_thai())
                        || !Boolean.TRUE.equals(ctsp.getSanPham().getTrang_thai())
                        || ctsp.getSo_luong() < 0;
                status.put("invalid", isInvalid);

                if (isInvalid)
                    invalidItems.add(ctsp.getSanPham().getTen_san_pham());
                stockStatus.add(status);
            }

            return ResponseEntity.ok(Map.of(
                    "items", stockStatus,
                    "has_invalid_items", !invalidItems.isEmpty(),
                    "invalid_item_names", invalidItems));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", true, "message", "Lỗi kiểm tra stock: " + e.getMessage()));
        }
    }

    @PostMapping("/setSPHD")
    public ResponseEntity<?> setSPHD(
            @RequestParam("idHoaDon") Integer idHD,
            @RequestParam("idCTSP") Integer idCTSP,
            @RequestParam("soLuongMoi") Integer soLuongMoi) {
        try {
            // ✅ QUY TẮC MỚI: Enforce minimum quantity = 1
            if (soLuongMoi < 1) {
                return ResponseEntity.badRequest()
                        .body("Số lượng tối thiểu là 1. Vui lòng sử dụng nút xóa để loại bỏ sản phẩm khỏi giỏ hàng.");
            }

            if (soLuongMoi <= 0) {
                return ResponseEntity.badRequest().body("Số lượng phải lớn hơn 0!");
            }

            HoaDon hoaDon = hoaDonRepo.findById(idHD)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại!"));

            ChiTietSanPham chiTietSP = chiTietSanPhamRepo.findById(idCTSP)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

            Optional<HoaDonChiTiet> optionalCT = hoaDonChiTietRepo
                    .findByChiTietSanPhamIdAndHoaDonId(idCTSP, idHD);

            int soLuongTrongHD = optionalCT.map(HoaDonChiTiet::getSo_luong).orElse(0);
            int soLuongTonKho = chiTietSP.getSo_luong();

            int tongToiDa = soLuongTonKho + soLuongTrongHD;
            if (soLuongMoi > tongToiDa) {
                return ResponseEntity.badRequest().body("Vượt quá số lượng tồn kho cho phép!");
            }

            // Cập nhật tồn kho
            int chenhLech = soLuongMoi - soLuongTrongHD;
            chiTietSP.setSo_luong(soLuongTonKho - chenhLech);

            // Tìm đơn giá (ưu tiên giá khuyến mãi)
            BigDecimal donGiaLe = chiTietSanPhamRepo.getAllCTSPKM().stream()
                    .filter(ct -> ct.getId_chi_tiet_san_pham().equals(chiTietSP.getId_chi_tiet_san_pham()))
                    .map(ct -> BigDecimal.valueOf(ct.getGia_ban()))
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

            // Tạo hoặc cập nhật chi tiết hóa đơn
            HoaDonChiTiet chiTiet = optionalCT.orElseGet(() -> {
                HoaDonChiTiet newCT = new HoaDonChiTiet();
                newCT.setHoaDon(hoaDon);
                newCT.setChiTietSanPham(chiTietSP);
                return newCT;
            });

            chiTiet.setSo_luong(soLuongMoi);
            // don_gia phải lưu TỔNG TIỀN (giá_lẻ × số_lượng)
            chiTiet.setDon_gia(donGiaLe.multiply(BigDecimal.valueOf(soLuongMoi)));

            // Lưu lại DB
            chiTietSanPhamRepo.save(chiTietSP);
            hoaDonChiTietRepo.save(chiTiet);
            updateTongTienHoaDon(hoaDon.getId_hoa_don());

            return ResponseEntity.ok("Cập nhật số lượng thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật số lượng: " + e.getMessage());
        }
    }

    @PostMapping("/giamSPHD")
    public ResponseEntity<?> giamSPHD(
            @RequestParam("idHoaDon") Integer idHD,
            @RequestParam("idCTSP") Integer idCTSP,
            @RequestParam("soLuong") Integer soLuong) {
        try {
            HoaDonChiTiet chiTiet = hoaDonChiTietRepo.findByChiTietSanPhamIdAndHoaDonId(idCTSP, idHD)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong hóa đơn"));

            ChiTietSanPham sp = chiTietSanPhamRepo.findById(idCTSP)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

            BigDecimal giaLe = chiTiet.getDon_gia().divide(BigDecimal.valueOf(chiTiet.getSo_luong()), 2,
                    RoundingMode.HALF_UP);

            int soLuongConLai = chiTiet.getSo_luong() - soLuong;
            if (soLuongConLai <= 0) {
                hoaDonChiTietRepo.delete(chiTiet);
            } else {
                chiTiet.setSo_luong(soLuongConLai);
                chiTiet.setDon_gia(giaLe.multiply(BigDecimal.valueOf(soLuongConLai)));
                hoaDonChiTietRepo.save(chiTiet);
            }

            sp.setSo_luong(sp.getSo_luong() + soLuong);
            chiTietSanPhamRepo.save(sp);

            HoaDon hoaDon = hoaDonRepo.findById(idHD)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại!"));

            updateTongTienHoaDon(hoaDon.getId_hoa_don());

            return ResponseEntity.ok("Giảm sản phẩm thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi giảm sản phẩm: " + e.getMessage());
        }
    }

    @DeleteMapping("/xoaSPHD")
    public ResponseEntity<?> xoaSanPhamKhoiHoaDon(
            @RequestParam("idHoaDon") Integer idHoaDon,
            @RequestParam("idChiTietSanPham") Integer idChiTietSanPham) {
        try {
            HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại!"));

            if ("Đã thanh toán".equalsIgnoreCase(hoaDon.getTrang_thai())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Không thể xóa sản phẩm từ hóa đơn đã thanh toán!"));
            }

            // ✅ NEW: Get quantity before deleting to restore stock
            Optional<HoaDonChiTiet> hdctOpt = hoaDonChiTietRepo.findByHoaDonAndChiTietSanPham(idHoaDon,
                    idChiTietSanPham);
            if (hdctOpt.isPresent()) {
                HoaDonChiTiet hdct = hdctOpt.get();
                int soLuongXoa = hdct.getSo_luong();

                // Xóa sản phẩm khỏi hóa đơn
                hoaDonChiTietRepo.xoaSPKhoiHD(idHoaDon, idChiTietSanPham);

                // ✅ Restore stock to CTSP
                ChiTietSanPham ctsp = chiTietSanPhamRepo.findById(idChiTietSanPham)
                        .orElseThrow(() -> new RuntimeException("CTSP không tồn tại!"));

                ctsp.setSo_luong(ctsp.getSo_luong() + soLuongXoa);

                // ⛔ KHÔNG tự động thay đổi trạng thái sản phẩm
                // Trạng thái sản phẩm (trang_thai) phải được admin quản lý thủ công
                // Đã xóa logic: auto-restore trang_thai = true khi stock > 0

                chiTietSanPhamRepo.save(ctsp);
            } else {
                // Fallback: just delete if not found
                hoaDonChiTietRepo.xoaSPKhoiHD(idHoaDon, idChiTietSanPham);
            }

            // Cập nhật tổng tiền
            try {
                updateTongTienHoaDon(idHoaDon);
            } catch (Exception ex) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Lỗi khi cập nhật tổng tiền: " + ex.getMessage()));
            }

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Lỗi khi xóa sản phẩm: " + e.getMessage()));
        }
    }

    @GetMapping("/get-suitable-vouchers")
    public ResponseEntity<?> getSuitableVouchers(@RequestParam("tongTien") BigDecimal tongTien) {
        try {
            List<VoucherBHResponse> vouchers = voucherRepository.listVoucherHopLeTheoGia(tongTien);
            return ResponseEntity.ok(vouchers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách voucher: " + e.getMessage());
        }
    }

    @PostMapping("/apply-voucher")
    public ResponseEntity<?> applyVoucher(
            @RequestParam("idHoaDon") Integer idHoaDon,
            @RequestParam(value = "idVoucher", required = false) Integer idVoucher) {
        try {
            HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại!"));

            Voucher oldVoucher = hoaDon.getVoucher();

            // Trường hợp 1: Bỏ voucher (idVoucher là null)
            if (idVoucher == null) {
                if (oldVoucher != null) {
                    // Trả lại số lượng cho voucher cũ
                    oldVoucher.setSoLuong(oldVoucher.getSoLuong() + 1);
                    voucherRepository.save(oldVoucher);
                }
                hoaDon.setVoucher(null);
            }
            // Trường hợp 2: Áp dụng voucher mới
            else {
                Voucher newVoucher = voucherRepository.findById(idVoucher)
                        .orElseThrow(() -> new RuntimeException("Voucher không tồn tại!"));

                // Nếu voucher mới khác voucher cũ
                if (oldVoucher == null || !oldVoucher.getId().equals(newVoucher.getId())) {
                    // Trả lại số lượng cho voucher cũ (nếu có)
                    if (oldVoucher != null) {
                        oldVoucher.setSoLuong(oldVoucher.getSoLuong() + 1);
                        voucherRepository.save(oldVoucher);
                    }

                    // Kiểm tra số lượng voucher mới
                    if (newVoucher.getSoLuong() <= 0) {
                        return ResponseEntity.badRequest().body("Voucher đã hết số lượng!");
                    }

                    // Trừ số lượng voucher mới
                    newVoucher.setSoLuong(newVoucher.getSoLuong() - 1);
                    voucherRepository.save(newVoucher);
                    hoaDon.setVoucher(newVoucher);
                }
                // Nếu giống nhau thì không làm gì cả (hoặc có thể check lại điều kiện)
            }

            hoaDonRepo.save(hoaDon);

            // Cập nhật lại tổng tiền (sẽ tự động tính lại giảm giá dựa trên voucher đã set)
            updateTongTienHoaDon(idHoaDon);

            // Trả về hóa đơn đã cập nhật
            return ResponseEntity.ok(hoaDonRepo.findHoaDonById(idHoaDon).get(0));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi áp dụng voucher: " + e.getMessage());
        }
    }

    @GetMapping("/phuongThucNhanHang")
    public ResponseEntity<?> phuongThucNhanHang(
            @RequestParam("idHoaDon") Integer idHD,
            @RequestParam("phuongThucNhanHang") String phuongThuc) {
        Optional<HoaDon> hoaDon = hoaDonRepo.findById(idHD);
        HoaDon hd = hoaDon.get();

        // ✅ CHỈ SET PHƯƠNG THỨC NHẬN HÀNG
        // Phí vận chuyển sẽ được tính và set khi thanh toán ZaloPay
        hd.setPhuong_thuc_nhan_hang(phuongThuc);

        // ❌ KHÔNG SET PHÍ VẬN CHUYỂN Ở ĐÂY
        // Lý do: Tránh bug cộng dồn khi user đổi phương thức nhiều lần
        // Phí ship sẽ được tính trong ZaloPayController.createOrder()

        hoaDonRepo.save(hd);

        System.out.println("✅ Đã set phương thức nhận hàng: " + phuongThuc +
                " cho hóa đơn " + idHD);

        return ResponseEntity.ok("ok");
    }

    // @GetMapping("/view")
    // public String viewBanHang(Model model) {
    // viewALl(model);
    // return "banhang";
    // }
    //
    // @GetMapping("/view/{idHd}")
    // public String detail(@RequestParam("idHd") Integer id) {
    // idHD = id;
    // return "redirect:/admin/ban-hang/view";
    // }
    //
    // @PostMapping("/view/add-hoa-don")
    // public String addHoaDon() {
    // HoaDon hoaDon = new HoaDon();
    // idNV = 1;
    // Optional<NhanVien> nv = nhanVienRepo.findById(idNV);
    // hoaDon.setMa_hoa_don(generateUniqueMaHoaDon());
    // hoaDon.setNhanVien(nv.get());
    // hoaDon.setNgay_tao(LocalDateTime.now());
    // hoaDon.setTrang_thai("Chưa thanh toán");
    // hoaDon.setTong_tien_truoc_giam(BigDecimal.ZERO);
    // hoaDon.setPhi_van_chuyen(BigDecimal.ZERO);
    // hoaDon.setTong_tien_sau_giam(BigDecimal.ZERO);
    //
    // HoaDon savedHoaDon = hoaDonRepo.save(hoaDon);
    // idHD = savedHoaDon.getId_hoa_don();
    //
    // return "redirect:/admin/ban-hang/view";
    // }

    @RequestMapping(value = "/update-khach-hang", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public Map<String, Object> updateKhachHang(
            @RequestParam("idHoaDonUDKH") String idHoaDonStr,
            @RequestParam("idKhachHangUDKH") String idKhachHangStr) {

        Map<String, Object> response = new HashMap<>();

        System.out.println("idHoaDon: " + idHoaDonStr);
        System.out.println("idKhachHang: " + idKhachHangStr);

        Integer idHoaDon = null;
        Integer idKhachHang = null;

        try {
            if (idHoaDonStr != null && !idHoaDonStr.trim().isEmpty()) {
                idHoaDon = Integer.parseInt(idHoaDonStr);
            }

            if (idKhachHangStr != null && !idKhachHangStr.trim().isEmpty()) {
                idKhachHang = Integer.parseInt(idKhachHangStr);
            }
        } catch (NumberFormatException e) {
            response.put("success", false);
            response.put("message", "ID không phải là số hợp lệ");
            return response;
        }

        if (idHoaDon == null || idKhachHang == null) {
            response.put("success", false);
            response.put("message", "ID hóa đơn hoặc ID khách hàng không hợp lệ");
            return response;
        }

        try {
            Optional<HoaDon> hoaDonOpt = hoaDonRepo.findById(idHoaDon);
            Optional<KhachHang> khachHangOpt = khachHangRepo.findById(idKhachHang);

            if (hoaDonOpt.isPresent() && khachHangOpt.isPresent()) {
                HoaDon hoaDon = hoaDonOpt.get();
                KhachHang khachHang = khachHangOpt.get();
                hoaDon.setKhachHang(khachHang);
                hoaDonRepo.save(hoaDon);

                response.put("success", true);
                response.put("message", "Cập nhật khách hàng thành công");
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn hoặc khách hàng");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/admin/khach-hang/them-moi")
    @ResponseBody
    public Map<String, Object> themKhachHang(
            @RequestBody KhachHang khachHang) {
        Map<String, Object> response = new HashMap<>();
        try {
            KhachHang newKhachHang = khachHangRepo.save(khachHang);
            response.put("success", true);
            response.put("idKhachHang", newKhachHang.getIdKhachHang());
            response.put("message", "Thêm khách hàng thành công");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi thêm khách hàng: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/admin/ban-hang/update-khach-hang")
    @ResponseBody
    public Map<String, Object> updateKhachHang(
            @RequestParam("idHoaDonUDKH") Integer idHoaDon,
            @RequestParam("idKhachHangUDKH") Integer idKhachHang) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<HoaDon> hoaDonOpt = hoaDonRepo.findById(idHoaDon);
            Optional<KhachHang> khachHangOpt = khachHangRepo.findById(idKhachHang);

            if (hoaDonOpt.isPresent() && khachHangOpt.isPresent()) {
                HoaDon hoaDon = hoaDonOpt.get();
                KhachHang khachHang = khachHangOpt.get();
                hoaDon.setKhachHang(khachHang);
                hoaDonRepo.save(hoaDon);

                response.put("success", true);
                response.put("message", "Cập nhật khách hàng thành công");
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn hoặc khách hàng");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/update-voucher")
    @ResponseBody
    public ResponseEntity<?> updateVoucher(
            @RequestParam("idHoaDon") Integer idHoaDon,
            @RequestParam("idVoucher") Integer idVoucher) {

        if (idHoaDon == null) {
            return ResponseEntity.badRequest().body("ID hóa đơn không hợp lệ");
        }

        try {
            Optional<HoaDon> hoaDonOpt = hoaDonRepo.findById(idHoaDon);

            if (hoaDonOpt.isPresent()) {
                HoaDon hoaDon = hoaDonOpt.get();

                if (idVoucher != null && idVoucher > 0) {
                    Optional<Voucher> voucherOpt = voucherRepository.findById(idVoucher);
                    if (voucherOpt.isPresent()) {
                        hoaDon.setVoucher(voucherOpt.get());
                    }
                } else {
                    hoaDon.setVoucher(null);
                }

                hoaDonRepo.save(hoaDon);
                updateTongTienHoaDon(idHoaDon);

                HoaDon updatedHoaDon = hoaDonRepo.findById(idHoaDon).get();
                Map<String, Object> response = new HashMap<>();
                response.put("tongTienTruocGiam", updatedHoaDon.getTong_tien_truoc_giam());
                response.put("tongTienSauGiam", updatedHoaDon.getTong_tien_sau_giam());
                response.put("message", "Cập nhật voucher thành công");

                return ResponseEntity.ok().body(response);
            } else {
                return ResponseEntity.badRequest().body("Không tìm thấy hóa đơn");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/get-applicable-vouchers")
    @ResponseBody
    public ResponseEntity<?> getApplicableVouchers(@RequestParam("idHoaDon") Integer idHoaDon) {
        if (idHoaDon == null) {
            return ResponseEntity.badRequest().body("ID hóa đơn không hợp lệ");
        }

        try {
            Optional<HoaDon> hoaDonOpt = hoaDonRepo.findById(idHoaDon);

            if (hoaDonOpt.isPresent()) {
                HoaDon hoaDon = hoaDonOpt.get();
                BigDecimal tongTienTruocGiam = hoaDon.getTong_tien_truoc_giam();

                List<Voucher> applicableVouchers = voucherRepository.findAll().stream()
                        .filter(v -> tongTienTruocGiam.compareTo(v.getGiaTriToiThieu()) >= 0)
                        .collect(Collectors.toList());

                return ResponseEntity.ok().body(applicableVouchers);
            } else {
                return ResponseEntity.badRequest().body("Không tìm thấy hóa đơn");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    // @PostMapping("/thanh-toan")
    // public String thanhToan(
    // @RequestParam(value = "id_hoa_don", required = true) String idHoaDonStr,
    // @RequestParam("idKhachHang") Integer idKhachHang,
    // @RequestParam("idNhanVien") Integer idNhanVien,
    // @RequestParam("hinhThucThanhToan") String hinhThucThanhToan,
    // @RequestParam("phuongThucNhanHang") String phuongThucNhanHang,
    // @RequestParam(value = "phiVanChuyen", required = false, defaultValue = "0")
    // BigDecimal phiVanChuyen,
    // @RequestParam(value = "idVoucher", required = false) Integer idVoucher,
    // @RequestParam(value = "tienKhachDua", required = false) BigDecimal
    // tienKhachDua,
    // Model model) {
    // Integer idHoaDon = (idHoaDonStr != null && !idHoaDonStr.isEmpty()) ?
    // Integer.parseInt(idHoaDonStr) : null;
    //
    // if (idHoaDon == null) {
    // model.addAttribute("error", "ID hóa đơn không hợp lệ!");
    // return "redirect:/admin/ban-hang/view";
    // }
    //
    // Optional<HoaDon> hoaDonOpt = hoaDonRepo.findById(idHoaDon);
    // if (!hoaDonOpt.isPresent()) {
    // model.addAttribute("error", "Không tìm thấy hóa đơn!");
    // return "redirect:/admin/ban-hang/view";
    // }
    //
    // HoaDon hoaDon = hoaDonOpt.get();
    //
    // Optional<KhachHang> khachHangOpt = khachHangRepo.findById(idKhachHang);
    // if (khachHangOpt.isPresent()) {
    // hoaDon.setKhachHang(khachHangOpt.get());
    // }
    //
    // Optional<NhanVien> nhanVienOpt = nhanVienRepo.findById(idNhanVien);
    // if (nhanVienOpt.isPresent()) {
    // hoaDon.setNhanVien(nhanVienOpt.get());
    // }
    //
    // if (idVoucher != null && idVoucher > 0) {
    // Optional<Voucher> voucherOpt = voucherRepository.findById(idVoucher);
    // if (voucherOpt.isPresent()) {
    // hoaDon.setVoucher(voucherOpt.get());
    // }
    // } else {
    // hoaDon.setVoucher(null);
    // }
    //
    // hoaDon.setPhuong_thuc_nhan_hang(phuongThucNhanHang);
    // hoaDon.setHinh_thuc_thanh_toan(hinhThucThanhToan);
    // hoaDon.setPhi_van_chuyen(phiVanChuyen);
    // hoaDon.setTrang_thai("Đã thanh toán");
    // updateTongTienHoaDon(idHoaDon);
    //
    // hoaDon = hoaDonRepo.findById(idHoaDon).get();
    //
    // if ("Tiền mặt".equals(hinhThucThanhToan)) {
    // if (tienKhachDua == null) {
    // model.addAttribute("error", "Vui lòng nhập số tiền khách đưa!");
    // return "redirect:/admin/ban-hang/view";
    // } else if (tienKhachDua.compareTo(hoaDon.getTong_tien_sau_giam()) >= 0) {
    // System.out.println("nhảy vào thanh toán
    // ----------------------------------------");
    // hoaDon.setTrang_thai("Đã thanh toán");
    // hoaDonRepo.save(hoaDon);
    // model.addAttribute("message", "Thanh toán thành công!");
    // return "redirect:/admin/ban-hang/view";
    // } else {
    // model.addAttribute("error", "Số tiền khách đưa không đủ!");
    // return "redirect:/admin/ban-hang/view";
    // }
    // } else if ("Chuyển khoản".equals(hinhThucThanhToan)) {
    // try {
    // // Lấy mã QR từ ZaloPay
    // Map<String, Object> qrCodeResponse = zaloPayService.createQRCode(
    // hoaDon.getTong_tien_sau_giam().longValue(),
    // idHoaDon.longValue()
    // );
    // String qrCodeUrl = (String) qrCodeResponse.get("qr_code_url");
    //
    // if (qrCodeUrl != null && !qrCodeUrl.isEmpty()) {
    // model.addAttribute("qrCodeUrl", qrCodeUrl);
    // model.addAttribute("message", "Vui lòng quét mã QR để thanh toán.");
    // hoaDon.setTrang_thai("Đã thanh toán");
    // hoaDonRepo.save(hoaDon);
    // return "payment-qr"; // Trả về view hiển thị mã QR
    // } else {
    // model.addAttribute("error", "Không thể tạo mã QR. Vui lòng thử lại.");
    // return "redirect:/admin/ban-hang/view";
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // model.addAttribute("error", "Có lỗi xảy ra khi tạo mã QR. Vui lòng thử
    // lại.");
    // return "redirect:/admin/ban-hang/view";
    // }
    // } else {
    // model.addAttribute("error", "Hình thức thanh toán không hợp lệ!");
    // return "redirect:/admin/ban-hang/view";
    // }
    // }

    @PostMapping("/zalopay/callback")
    public ResponseEntity<String> handleZaloPayCallback(@RequestBody Map<String, Object> callbackData) {
        try {
            // Kiểm tra tính hợp lệ của callback
            if (isValidCallback(callbackData)) {
                // Lấy thông tin từ callback
                Long appTransId = Long.parseLong(callbackData.get("app_trans_id").toString());
                String status = callbackData.get("status").toString();

                if ("1".equals(status)) { // Thanh toán thành công
                    // Cập nhật trạng thái hóa đơn
                    Optional<HoaDon> hoaDonOpt = hoaDonRepo.findById(appTransId.intValue());
                    if (hoaDonOpt.isPresent()) {
                        HoaDon hoaDon = hoaDonOpt.get();
                        hoaDon.setTrang_thai("Đã thanh toán");
                        hoaDonRepo.save(hoaDon);
                    }
                }
                return ResponseEntity.ok("Callback processed successfully");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid callback data");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing callback");
        }
    }

    private boolean isValidCallback(Map<String, Object> callbackData) {
        // Kiểm tra tính hợp lệ của callback (ví dụ: chữ ký HMAC)
        return true; // Thay thế bằng logic thực tế
    }

    @GetMapping("/check-so-luong")
    public ResponseEntity<Map<String, Integer>> checkSoLuong(
            @RequestParam("idCTSP") Integer idCTSP) {
        Optional<ChiTietSanPham> ctspOpt = chiTietSanPhamRepo.findById(idCTSP);

        if (!ctspOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        int soLuongTon = ctspOpt.get().getSo_luong();

        Map<String, Integer> response = new HashMap<>();
        response.put("soLuongTon", soLuongTon);

        return ResponseEntity.ok(response);
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private String generateUniqueMaHoaDon() {
        Random random = new Random();
        String maHoaDon;
        boolean isDuplicate;
        do {
            StringBuilder code = new StringBuilder("HD");
            for (int i = 0; i < 6; i++) {
                code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }
            maHoaDon = code.toString();

            final String finalMaHoaDon = maHoaDon;
            isDuplicate = hoaDonRepo.findAll().stream()
                    .anyMatch(hd -> finalMaHoaDon.equalsIgnoreCase(hd.getMa_hoa_don()));

        } while (isDuplicate);

        return maHoaDon;
    }

    // @GetMapping("/view/addAndUdateSPGH")
    // public String addAndUpdateSPGH(
    // @RequestParam("idCTSP") Integer idChiTietSanPham,
    // @RequestParam(value = "idHoaDonADGH", required = false) Integer idHoaDon,
    // @RequestParam("soLuong") Integer soLuong,
    // Model model) {
    //
    // if (idHoaDon == null) {
    // if (idHD != null) {
    // idHoaDon = idHD;
    // } else {
    // model.addAttribute("error", "Không có hóa đơn được chọn!");
    // return "redirect:/admin/ban-hang/view";
    // }
    // }
    //
    // Optional<HoaDonChiTiet> existingHdct =
    // hoaDonChiTietRepo.findByChiTietSanPhamIdAndHoaDonId(idChiTietSanPham,
    // idHoaDon);
    //
    // Optional<ChiTietSanPham> chiTietSanPhamOpt =
    // chiTietSanPhamRepo.findById(idChiTietSanPham);
    // if (!chiTietSanPhamOpt.isPresent()) {
    // model.addAttribute("error", "Không tìm thấy sản phẩm!");
    // return "redirect:/admin/ban-hang/view";
    // }
    //
    // ChiTietSanPham chiTietSanPham = chiTietSanPhamOpt.get();
    //
    // if (chiTietSanPham.getSo_luong() < soLuong) {
    // model.addAttribute("error", "Số lượng không đủ!");
    // return "redirect:/admin/ban-hang/view";
    // }
    //
    // Optional<HoaDon> hoaDonOpt = hoaDonRepo.findById(idHoaDon);
    // if (!hoaDonOpt.isPresent()) {
    // model.addAttribute("error", "Không tìm thấy hóa đơn!");
    // return "redirect:/admin/ban-hang/view";
    // }
    //
    // HoaDon hoaDon = hoaDonOpt.get();
    //
    // if (existingHdct.isPresent()) {
    // HoaDonChiTiet hdct = existingHdct.get();
    // int newSoLuong = hdct.getSo_luong() + soLuong;
    // if (chiTietSanPham.getSo_luong() < newSoLuong) {
    // model.addAttribute("error", "Số lượng không đủ!");
    // return "redirect:/admin/ban-hang/view";
    // }
    // hdct.setSo_luong(newSoLuong);
    // hdct.setDon_gia(BigDecimal.valueOf(newSoLuong).multiply(chiTietSanPham.getGia_ban()));
    // hoaDonChiTietRepo.save(hdct);
    // } else {
    // HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
    // hoaDonChiTiet.setChiTietSanPham(chiTietSanPham);
    // hoaDonChiTiet.setHoaDon(hoaDon);
    // hoaDonChiTiet.setSo_luong(soLuong);
    // hoaDonChiTiet.setDon_gia(BigDecimal.valueOf(soLuong).multiply(chiTietSanPham.getGia_ban()));
    // hoaDonChiTietRepo.save(hoaDonChiTiet);
    // }
    //
    // chiTietSanPham.setSo_luong(chiTietSanPham.getSo_luong() - soLuong);
    // chiTietSanPhamRepo.save(chiTietSanPham);
    //
    // updateTongTienHoaDon(idHoaDon);
    //
    // return "redirect:/admin/ban-hang/view";
    // }

    private void updateTongTienHoaDon(Integer idHoaDon) {
        Optional<HoaDon> hoaDonOpt = hoaDonRepo.findById(idHoaDon);
        if (!hoaDonOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy hóa đơn");
        }

        HoaDon hoaDon = hoaDonOpt.get();

        // ✅ Tổng tiền sản phẩm (KHÔNG BAO GỒM SHIP)
        BigDecimal tongDonGia = hoaDonChiTietRepo.sumDonGiaByHoaDonId(idHoaDon);
        if (tongDonGia == null)
            tongDonGia = BigDecimal.ZERO;

        // ✅ THAY ĐỔI: tongTienTruocGiam = CHỈ SẢN PHẨM (không cộng ship)
        BigDecimal tongTienTruocGiam = tongDonGia;

        // ✅ Tính voucher dựa trên tổng sản phẩm (không tính ship)
        BigDecimal giamGia = BigDecimal.ZERO;
        if (hoaDon.getVoucher() != null) {
            Voucher voucher = hoaDon.getVoucher();
            if (tongTienTruocGiam.compareTo(voucher.getGiaTriToiThieu()) >= 0) {
                // Check voucher type before calculation
                if ("Phần trăm".equals(voucher.getKieuGiamGia())) {
                    // Calculate percentage discount
                    giamGia = tongTienTruocGiam.multiply(voucher.getGiaTriGiam())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                    // Apply maximum discount limit if specified
                    if (voucher.getGiaTriToiDa() != null && voucher.getGiaTriToiDa().compareTo(BigDecimal.ZERO) > 0) {
                        if (giamGia.compareTo(voucher.getGiaTriToiDa()) > 0) {
                            giamGia = voucher.getGiaTriToiDa();
                        }
                    }
                } else {
                    // Fixed amount discount
                    giamGia = voucher.getGiaTriGiam();
                }

                // Ensure discount doesn't exceed total amount
                if (giamGia.compareTo(tongTienTruocGiam) > 0) {
                    giamGia = tongTienTruocGiam;
                }
            }
        }

        // ✅ tongTienSauGiam = sản phẩm - voucher (không tính ship)
        BigDecimal tongTienSauGiam = tongTienTruocGiam.subtract(giamGia);
        if (tongTienSauGiam.compareTo(BigDecimal.ZERO) < 0) {
            tongTienSauGiam = BigDecimal.ZERO;
        }

        // ✅ Lưu vào DB (ship được lưu riêng trong phi_van_chuyen)
        hoaDon.setTong_tien_truoc_giam(tongTienTruocGiam);
        hoaDon.setTong_tien_sau_giam(tongTienSauGiam);
        hoaDonRepo.save(hoaDon);

        System.out.println("✅ Updated invoice: tongTruocGiam=" + tongTienTruocGiam +
                ", tongSauGiam=" + tongTienSauGiam +
                ", ship=" + hoaDon.getPhi_van_chuyen());
    }
}
