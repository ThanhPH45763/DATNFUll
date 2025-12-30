package com.example.duanbe.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import com.example.duanbe.entity.ChiTietKhuyenMai;
import com.example.duanbe.entity.ChiTietSanPham;
import com.example.duanbe.entity.DiaChiKhachHang;
import com.example.duanbe.entity.HoaDon;
import com.example.duanbe.entity.HoaDonChiTiet;
import com.example.duanbe.entity.KhuyenMai;
import com.example.duanbe.entity.TheoDoiDonHang;
import com.example.duanbe.entity.Voucher;
import com.example.duanbe.repository.ChiTietKhuyenMaiRepo;
import com.example.duanbe.repository.ChiTietSanPhamRepo;
import com.example.duanbe.repository.DiaChiKhachHangRepo;
import com.example.duanbe.repository.HoaDonChiTietRepo;
import com.example.duanbe.repository.HoaDonRepo;
import com.example.duanbe.repository.TheoDoiDonHangRepo;
import com.example.duanbe.repository.VoucherRepository;
import com.example.duanbe.response.ChiTietSanPhamView;
import com.example.duanbe.response.ChiTietTraHangDTO;
import com.example.duanbe.response.HoaDonChiTietResponse;
import com.example.duanbe.response.HoaDonResponse;
import com.example.duanbe.response.PageResponse;
import com.example.duanbe.response.TheoDoiDonHangResponse;
import com.example.duanbe.service.HoaDonService;

import jakarta.transaction.Transactional;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE })
@RequestMapping("/admin/qlhd")
public class HoaDonController {
    @Autowired
    private HoaDonRepo hoaDonRepo;
    @Autowired
    private HoaDonChiTietRepo hoaDonChiTietRepo;
    @Autowired
    private ChiTietSanPhamRepo chiTietSanPhamRepo;
    @Autowired
    private VoucherRepository voucherRepo;
    @Autowired
    private TheoDoiDonHangRepo theoDoiDonHangRepo;
    @Autowired
    private DiaChiKhachHangRepo diaChiKhachHangRepo;
    @Autowired
    private HoaDonService hoaDonService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ChiTietKhuyenMaiRepo chiTietKhuyenMaiRepo;

    @PostMapping("/update-status")
    public ResponseEntity<Map<String, Object>> updateInvoiceStatus(
            @RequestBody Map<String, Object> request) {
        Integer idHoaDon = (Integer) request.get("idHoaDon");
        String status = (String) request.get("status");
        System.out.println(status + "hhhhhhhhhhhhhhhhhhhhhhhhhh");
        Optional<HoaDon> hoaDonOpt = hoaDonRepo.findById(idHoaDon);
        if (hoaDonOpt.isPresent()) {
            HoaDon hoaDon = hoaDonOpt.get();
            hoaDon.setTrang_thai("Đã thanh toán");
            hoaDonRepo.save(hoaDon);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật trạng thái hóa đơn thành công!");
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Không tìm thấy hóa đơn!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/updateHTTTHD")
    public ResponseEntity<HoaDon> updateHinhThucTTHoaDon(@RequestParam("idHD") Integer id,
            @RequestParam("hinhThucThanhToan") String httt) {
        HoaDon hoaDon = hoaDonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        hoaDon.setHinh_thuc_thanh_toan(httt);
        return ResponseEntity.ok(hoaDonRepo.save(hoaDon));
    }

    @GetMapping("/all-hoa-don")
    public List<HoaDonResponse> getListHD() {
        return hoaDonRepo.getListHD();
    }

    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_QL', 'ROLE_NV')")
    // @GetMapping("/danh_sach_hoa_don")
    // public Page<HoaDonResponse> getAllHD(
    // @RequestParam(value = "page", defaultValue = "0") Integer page,
    // @RequestParam(value = "size", defaultValue = "3") Integer size) {
    // if (page < 0) {
    // throw new IllegalArgumentException("Số trang không hợp lệ");
    // }
    // if (size <= 0) {
    // throw new IllegalArgumentException("Kích thước trang không hợp lệ");
    // }
    // Pageable pageable = PageRequest.of(page, size);
    // Page<HoaDonResponse> list = hoaDonRepo.getAllHD(pageable);
    // return list;
    // }
    //
    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_QL', 'ROLE_NV')")
    // @GetMapping("/tim_kiem")
    // public Page<HoaDonResponse> search(
    // @RequestParam(name = "keyword", required = false) String keyword,
    // @RequestParam(value = "page", defaultValue = "0") Integer page,
    // @RequestParam(value = "size", defaultValue = "3") Integer size) {
    // Pageable pageable = PageRequest.of(page, size);
    // String searchKeyword = null;
    // searchKeyword = keyword.trim();
    // return hoaDonRepo.timHoaDon(searchKeyword, pageable);
    // }
    //
    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_QL', 'ROLE_NV')")
    // @GetMapping("/loc_ngay")
    // public Page<HoaDonResponse> filterHoaDonByDate(
    // @RequestParam(value = "tuNgay", required = false) String tuNgayStr,
    // @RequestParam(value = "denNgay", required = false) String denNgayStr,
    // @RequestParam(value = "page", defaultValue = "0") Integer page,
    // @RequestParam(value = "size", defaultValue = "3") Integer size) {
    // if (page < 0 || size <= 0) {
    // throw new IllegalArgumentException("Page hoặc size không hợp lệ");
    // }
    // Pageable pageable = PageRequest.of(page, size);
    // LocalDateTime tuNgay = null;
    // LocalDateTime denNgay = null;
    // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
    // HH:mm:ss");
    //
    // if (tuNgayStr != null && !tuNgayStr.isEmpty()) {
    // tuNgay = LocalDateTime.parse(tuNgayStr, formatter);
    // }
    // if (denNgayStr != null && !denNgayStr.isEmpty()) {
    // denNgay = LocalDateTime.parse(denNgayStr, formatter);
    // }
    // if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
    // throw new IllegalArgumentException("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày
    // kết thúc");
    // }
    // return hoaDonRepo.findHoaDonByNgay(tuNgay, denNgay, pageable);
    // }
    //
    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_QL', 'ROLE_NV')")
    // @GetMapping("/loc_trang_thai_don_hang")
    // public Page<HoaDonResponse> filterHoaDonByTrangThai(
    // @RequestParam(value = "trangThai", required = false) String trangThai,
    // @RequestParam(value = "page", defaultValue = "0") Integer page,
    // @RequestParam(value = "size", defaultValue = "3") Integer size) {
    // if (page < 0 || size <= 0) {
    // throw new IllegalArgumentException("Page hoặc size không hợp lệ");
    // }
    // Pageable pageable = PageRequest.of(page, size);
    // return (trangThai == null || trangThai.trim().isEmpty())
    // ? hoaDonRepo.getAllHD(pageable)
    // : hoaDonRepo.findHoaDonByTrangThaiGiaoHang(trangThai, pageable);
    // }

    @GetMapping("/loc_hoa_don")
    public PageResponse<HoaDonResponse> searchHoaDon(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "tuNgay", required = false) String tuNgayStr,
            @RequestParam(value = "denNgay", required = false) String denNgayStr,
            @RequestParam(value = "trangThai", required = false) String trangThai,
            @RequestParam(value = "loaiHoaDon", required = false) String loaiHoaDon,
            Pageable pageable) {
        // keyword
        String searchKeyword = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchKeyword = keyword.trim();
        }
        // date
        LocalDateTime tuNgay = null;
        LocalDateTime denNgay = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (tuNgayStr != null && !tuNgayStr.isEmpty() && !"undefined".equals(tuNgayStr)) {
            tuNgay = LocalDateTime.parse(tuNgayStr, formatter);
        }
        if (denNgayStr != null && !denNgayStr.isEmpty() && !"undefined".equals(denNgayStr)) {
            denNgay = LocalDateTime.parse(denNgayStr, formatter);
        }
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc");
        }
        String finalTrangThai = (trangThai != null && !trangThai.isEmpty()) ? trangThai : null;
        String finalLoaiHoaDon = (loaiHoaDon != null && !loaiHoaDon.isEmpty()) ? loaiHoaDon : null;
        Page<HoaDonResponse> page = hoaDonRepo.locHoaDon(searchKeyword, tuNgay, denNgay, finalTrangThai,
                finalLoaiHoaDon, pageable);
        return PageResponse.fromPage(page);
    }

    @GetMapping("/hdct")
    public Map<String, Object> getHDCTBymaHD(@RequestParam("id") String maHoaDon) {
        HoaDonResponse hoaDon = hoaDonRepo.findByMaHoaDon(maHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon));
        Integer idHoaDon = hoaDon.getId_hoa_don();
        List<HoaDonChiTietResponse> chiTietHoaDons = hoaDonChiTietRepo.findHoaDonChiTietById(idHoaDon);
        List<TheoDoiDonHangResponse> trangThaiHistory = hoaDonRepo.findTrangThaiHistoryByIdHoaDon(idHoaDon);
        List<DiaChiKhachHang> listDC = diaChiKhachHangRepo.findByKhachHangId(hoaDon.getId_khach_hang());
        Map<String, Object> response = new HashMap<>();
        response.put("hoaDon", hoaDon);
        response.put("chiTietHoaDons", chiTietHoaDons);
        response.put("trangThaiHistory", trangThaiHistory);
        response.put("listDC", listDC);
        return response;
    }

    @PostMapping("/chuyen_trang_thai")
    @Transactional
    public String updateTrangThai(
            @RequestParam("maHoaDon") String maHoaDon,
            @RequestParam("newTrangThai") String newTrangThai,
            @RequestParam(value = "noiDungDoi", required = false) String noiDungDoi) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty() ||
                newTrangThai == null || newTrangThai.trim().isEmpty()) {
            throw new IllegalArgumentException("Thông tin không hợp lệ");
        }
        Optional<HoaDonResponse> hoaDonOpt = hoaDonRepo.findByMaHoaDon(maHoaDon);
        if (!hoaDonOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon);
        }
        Integer idHoaDon = hoaDonOpt.get().getId_hoa_don();
        LocalDateTime ngayChuyen = LocalDateTime.now();

        if ("Đã xác nhận".equals(newTrangThai)) {
            System.out.println("🔍 Bắt đầu xác nhận đơn hàng: " + maHoaDon + " (ID: " + idHoaDon + ")");

            List<HoaDonChiTietResponse> chiTietHoaDons = hoaDonChiTietRepo.findHoaDonChiTietById(idHoaDon);

            if (chiTietHoaDons == null || chiTietHoaDons.isEmpty()) {
                System.err.println("❌ Không tìm thấy chi tiết hóa đơn cho ID: " + idHoaDon);
                throw new RuntimeException("Không tìm thấy sản phẩm trong hóa đơn");
            }

            System.out.println("📦 Tìm thấy " + chiTietHoaDons.size() + " sản phẩm trong hóa đơn");

            for (HoaDonChiTietResponse chiTiet : chiTietHoaDons) {
                Integer idCTSP = chiTiet.getId_chi_tiet_san_pham();
                Integer soLuong = chiTiet.getSo_luong();
                String tenSanPham = chiTiet.getTen_san_pham();

                System.out.println("  → Sản phẩm: " + tenSanPham + " (ID: " + idCTSP + "), SL: " + soLuong);

                Optional<ChiTietSanPham> chiTietSanPhamOpt = chiTietSanPhamRepo.findById(idCTSP);
                if (chiTietSanPhamOpt.isPresent()) {
                    ChiTietSanPham chiTietSanPham = chiTietSanPhamOpt.get();
                    int tonKho = chiTietSanPham.getSo_luong();
                    System.out.println("    Tồn kho hiện tại: " + tonKho);

                    if (chiTietSanPham.getSo_luong() < soLuong) {
                        String errorMsg = "Số lượng tồn kho không đủ cho sản phẩm: " + tenSanPham +
                                " (Cần: " + soLuong + ", Có: " + tonKho + ")";
                        System.err.println("❌ " + errorMsg);
                        throw new RuntimeException(errorMsg);
                    }
                    chiTietSanPham.setSo_luong(chiTietSanPham.getSo_luong() - soLuong);
                    chiTietSanPhamRepo.save(chiTietSanPham);
                    System.out.println("    ✅ Đã trừ tồn kho. Còn lại: " + chiTietSanPham.getSo_luong());

                    Optional<HoaDon> optHD = hoaDonRepo.findById(idHoaDon);
                    HoaDon hoaDon = optHD.get();
                    hoaDon.setNgay_sua(LocalDateTime.now());
                    hoaDonRepo.save(hoaDon);
                } else {
                    String errorMsg = "Không tìm thấy sản phẩm chi tiết với ID: " + idCTSP;
                    System.err.println("❌ " + errorMsg);
                    throw new RuntimeException(errorMsg);
                }
            }
            System.out.println("✅ Xác nhận đơn hàng thành công: " + maHoaDon);
        }
        // Xử lý khi trạng thái là "Hoàn thành"
        // if ("Hoàn thành".equals(newTrangThai)) {
        // Optional<HoaDon> optHD = hoaDonRepo.findById(idHoaDon);
        // if (optHD.isPresent()) {
        // HoaDon hoaDon = optHD.get();
        // hoaDon.setPhu_thu(BigDecimal.valueOf(0)); // Reset so_tien_thanh_toan_them về
        // 0
        // hoaDon.setNgay_sua(LocalDateTime.now());
        // hoaDonRepo.save(hoaDon);
        // } else {
        // throw new RuntimeException("Không tìm thấy hóa đơn với ID: " + idHoaDon);
        // }
        // }
        // Cập nhật trạng thái đơn hàng với nhan_vien_doi và noi_dung_doi
        hoaDonRepo.insertTrangThaiDonHang(maHoaDon, newTrangThai, ngayChuyen, noiDungDoi);
        return "Cập nhật trạng thái thành công: " + newTrangThai;
    }

    @PostMapping("/quay_lai_trang_thai")
    @Transactional
    public ResponseEntity<Map<String, Object>> revertToInitialStatus(
            @RequestParam("maHoaDon") String maHoaDon,
            @RequestParam(value = "noiDungDoi", required = false) String noiDungDoi) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Mã hóa đơn không hợp lệ!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<HoaDonResponse> hoaDonOpt = hoaDonRepo.findByMaHoaDon(maHoaDon);
        if (!hoaDonOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Không tìm thấy hóa đơn với mã: " + maHoaDon);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Integer idHoaDon = hoaDonOpt.get().getId_hoa_don();
        List<TheoDoiDonHangResponse> trangThaiHistory = hoaDonRepo.findTrangThaiHistoryByIdHoaDon(idHoaDon);

        if (trangThaiHistory.isEmpty() || !trangThaiHistory.get(0).getTrang_thai().equals("Chờ xác nhận")) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Không thể quay lại vì trạng thái ban đầu không phải 'Chờ xác nhận'!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String trangThaiHienTai = trangThaiHistory.get(trangThaiHistory.size() - 1).getTrang_thai();
        if ("Đã xác nhận".equals(trangThaiHienTai)) {
            List<HoaDonChiTietResponse> chiTietHoaDons = hoaDonChiTietRepo.findHoaDonChiTietById(idHoaDon);
            for (HoaDonChiTietResponse chiTiet : chiTietHoaDons) {
                Integer idCTSP = chiTiet.getId_chi_tiet_san_pham();
                Integer soLuong = chiTiet.getSo_luong();
                Optional<ChiTietSanPham> chiTietSanPhamOpt = chiTietSanPhamRepo.findById(idCTSP);
                if (chiTietSanPhamOpt.isPresent()) {
                    ChiTietSanPham chiTietSanPham = chiTietSanPhamOpt.get();
                    chiTietSanPham.setSo_luong(chiTietSanPham.getSo_luong() + soLuong);
                    chiTietSanPhamRepo.save(chiTietSanPham);
                } else {
                    throw new RuntimeException("Không tìm thấy sản phẩm chi tiết với ID: " + idCTSP);
                }
            }
        }
        Optional<HoaDon> optHD = hoaDonRepo.findById(idHoaDon);
        HoaDon hoaDon = optHD.get();
        hoaDon.setNgay_sua(LocalDateTime.now());
        hoaDonRepo.save(hoaDon);

        LocalDateTime ngayChuyen = LocalDateTime.now();
        hoaDonRepo.insertTrangThaiDonHang(maHoaDon, "Chờ xác nhận", ngayChuyen, noiDungDoi);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã quay lại trạng thái 'Chờ xác nhận'!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel_order")
    @Transactional
    public String cancelOrder(@RequestParam("maHoaDon") String maHoaDon,
            @RequestParam(value = "noiDungDoi", required = false) String noiDungDoi) { // Thêm tham số
        Optional<HoaDonResponse> hoaDonOpt = hoaDonRepo.findByMaHoaDon(maHoaDon);
        if (!hoaDonOpt.isPresent()) {
            throw new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon);
        }

        Integer idHoaDon = hoaDonOpt.get().getId_hoa_don();
        LocalDateTime ngayChuyen = LocalDateTime.now();

        String trangThaiGanNhat = hoaDonRepo.findLatestNonUpdatedStatusByIdHoaDon(idHoaDon);
        if (trangThaiGanNhat == null) {
            throw new RuntimeException("Không tìm thấy trạng thái phù hợp cho hóa đơn với mã: " + maHoaDon);
        }

        if ("Chờ xác nhận".equals(trangThaiGanNhat)) {
            Optional<HoaDon> hoaDonEntityOpt = hoaDonRepo.findById(idHoaDon);
            if (hoaDonEntityOpt.isPresent()) {
                HoaDon hoaDon = hoaDonEntityOpt.get();
                Integer idVoucher = hoaDon.getVoucher() != null ? hoaDon.getVoucher().getId() : null;
                if (idVoucher != null) {
                    Optional<Voucher> voucherOpt = voucherRepo.findById(idVoucher);
                    if (voucherOpt.isPresent()) {
                        Voucher voucher = voucherOpt.get();
                        voucher.setSoLuong(voucher.getSoLuong() + 1);
                        voucherRepo.save(voucher);
                    } else {
                        throw new RuntimeException("Không tìm thấy voucher với ID: " + idVoucher);
                    }
                }
            }
        } else if ("Đã xác nhận".equals(trangThaiGanNhat) || "Chờ đóng gói".equals(trangThaiGanNhat)) {
            Optional<HoaDon> hoaDonEntityOpt = hoaDonRepo.findById(idHoaDon);
            if (hoaDonEntityOpt.isPresent()) {
                HoaDon hoaDon = hoaDonEntityOpt.get();
                Integer idVoucher = hoaDon.getVoucher() != null ? hoaDon.getVoucher().getId() : null;
                if (idVoucher != null) {
                    Optional<Voucher> voucherOpt = voucherRepo.findById(idVoucher);
                    if (voucherOpt.isPresent()) {
                        Voucher voucher = voucherOpt.get();
                        voucher.setSoLuong(voucher.getSoLuong() + 1);
                        voucherRepo.save(voucher);
                    } else {
                        throw new RuntimeException("Không tìm thấy voucher với ID: " + idVoucher);
                    }
                }
            }

            List<HoaDonChiTietResponse> chiTietHoaDons = hoaDonChiTietRepo.findHoaDonChiTietById(idHoaDon);
            for (HoaDonChiTietResponse chiTiet : chiTietHoaDons) {
                Integer idCTSP = chiTiet.getId_chi_tiet_san_pham();
                Integer soLuong = chiTiet.getSo_luong();
                Optional<ChiTietSanPham> chiTietSanPhamOpt = chiTietSanPhamRepo.findById(idCTSP);
                if (chiTietSanPhamOpt.isPresent()) {
                    ChiTietSanPham chiTietSanPham = chiTietSanPhamOpt.get();
                    chiTietSanPham.setSo_luong(chiTietSanPham.getSo_luong() + soLuong);
                    chiTietSanPhamRepo.save(chiTietSanPham);
                } else {
                    throw new RuntimeException("Không tìm thấy sản phẩm chi tiết với ID: " + idCTSP);
                }
            }
        } else {
            throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái: " + trangThaiGanNhat);
        }
        hoaDonRepo.insertTrangThaiDonHang(maHoaDon, "Đã hủy", ngayChuyen, noiDungDoi);
        return "Đơn hàng đã được hủy";
    }

    @PostMapping("/update_phiShip")
    @Transactional
    public ResponseEntity<?> updatePhiShip(
            @RequestParam("maHoaDon") String maHoaDon,
            @RequestParam(value = "phiVanChuyen", required = false, defaultValue = "0") BigDecimal phiVanChuyen) {
        Optional<HoaDonResponse> hoaDon = hoaDonRepo.findByMaHoaDon(maHoaDon);
        if (!hoaDon.isPresent()) {
            return ResponseEntity.badRequest().body("Hóa đơn không tồn tại");
        }
        Integer idHoaDon = hoaDon.get().getId_hoa_don();
        Optional<HoaDon> optHD = hoaDonRepo.findById(idHoaDon);
        if (!optHD.isPresent()) {
            return ResponseEntity.badRequest().body("Hóa đơn không tồn tại");
        }
        HoaDon hd = optHD.get();
        boolean isOnlineCash = "Online".equalsIgnoreCase(hd.getLoai_hoa_don())
                && "Tiền mặt".equalsIgnoreCase(hd.getHinh_thuc_thanh_toan());

        BigDecimal phuThu = hd.getPhu_thu() != null ? hd.getPhu_thu() : BigDecimal.ZERO;
        BigDecimal oldPhiVanChuyen = hd.getPhi_van_chuyen() != null ? hd.getPhi_van_chuyen() : BigDecimal.ZERO;
        // Cập nhật phụ thu dựa trên phí vận chuyển mới
        BigDecimal phiVanChuyenIncrease = phiVanChuyen.subtract(oldPhiVanChuyen).max(BigDecimal.ZERO);
        BigDecimal phuThuFinal = phuThu.add(phiVanChuyenIncrease);

        // Cập nhật hóa đơn
        hd.setPhi_van_chuyen(phiVanChuyen);
        if (!isOnlineCash) {
            if (phiVanChuyen.compareTo(BigDecimal.ZERO) == 0) {
                hd.setPhu_thu(phuThuFinal.subtract(oldPhiVanChuyen));
            } else {
                hd.setPhu_thu(phuThuFinal);
            }
        }
        BigDecimal tienGiam = hd.getVoucher() != null ? hd.getVoucher().getKieuGiamGia().equals("Phần trăm")
                ? hd.getTong_tien_truoc_giam()
                        .multiply(hd.getVoucher().getGiaTriGiam().divide(new BigDecimal("100")))
                        .min(hd.getVoucher().getGiaTriToiDa() != null ? hd.getVoucher().getGiaTriToiDa()
                                : BigDecimal.valueOf(Double.MAX_VALUE))
                : hd.getVoucher().getGiaTriGiam() : BigDecimal.ZERO;

        hd.setTong_tien_sau_giam(hd.getTong_tien_truoc_giam().add(phiVanChuyen).subtract(tienGiam));
        hd.setNgay_sua(LocalDateTime.now());
        hoaDonRepo.save(hd);

        return ResponseEntity.ok("Cập nhật phí vận chuyển thành công!");
    }

    @PostMapping("/update_ttkh")
    public ResponseEntity<Map<String, Object>> updateCustomerInfo(
            @RequestBody Map<String, Object> request) {
        String maHoaDon = (String) request.get("maHoaDon");
        String hoTen = (String) request.get("hoTen");
        String email = (String) request.get("email");
        String sdtNguoiNhan = (String) request.get("sdtNguoiNhan");
        String diaChi = (String) request.get("diaChi");
        Integer phiVanChuyen = (Integer) request.get("phiVanChuyen");

        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Mã hóa đơn không hợp lệ!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        Optional<HoaDon> hoaDonOpt = hoaDonRepo.findById(hoaDonRepo.findByMaHoaDon(maHoaDon)
                .map(HoaDonResponse::getId_hoa_don)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon)));

        if (hoaDonOpt.isPresent()) {
            HoaDon hoaDon = hoaDonOpt.get();
            if (hoTen != null && !hoTen.trim().isEmpty()) {
                hoaDon.setHo_ten(hoTen);
            }
            if (email != null && !email.trim().isEmpty()) {
                hoaDon.setEmail(email);
            }
            if (sdtNguoiNhan != null && !sdtNguoiNhan.trim().isEmpty()) {
                hoaDon.setSdt(sdtNguoiNhan);
            }
            if (diaChi != null && !diaChi.trim().isEmpty()) {
                hoaDon.setDia_chi(diaChi);
            }
            BigDecimal pvcCu = hoaDon.getPhi_van_chuyen() != null ? hoaDon.getPhi_van_chuyen() : BigDecimal.ZERO;
            BigDecimal pvcMoi = BigDecimal.valueOf(phiVanChuyen);
            BigDecimal pvcChange = pvcMoi.subtract(pvcCu);
            BigDecimal phuThu = hoaDon.getPhu_thu() != null ? hoaDon.getPhu_thu() : BigDecimal.ZERO;

            boolean isOnlineCash = "Online".equalsIgnoreCase(hoaDon.getLoai_hoa_don())
                    && "Tiền mặt".equalsIgnoreCase(hoaDon.getHinh_thuc_thanh_toan());
            BigDecimal pvc = pvcCu; // Mặc định giữ nguyên phí cũ
            if (pvcChange.compareTo(BigDecimal.ZERO) > 0) {
                // Nếu phí mới cao hơn → tăng phụ thu và cập nhật phí mới
                pvc = pvcMoi;
                if (!isOnlineCash) {
                    phuThu = phuThu.add(pvcChange);
                    hoaDon.setPhu_thu(phuThu);
                }
            }
            hoaDon.setTong_tien_sau_giam(hoaDon.getTong_tien_sau_giam().subtract(pvcCu).add(pvc));
            hoaDon.setPhi_van_chuyen(pvc);
            hoaDon.setNgay_sua(LocalDateTime.now());
            hoaDonRepo.save(hoaDon);
            System.out.println("phiVanChuyen: " + request.get("phiVanChuyen") + ", type: "
                    + (request.get("phiVanChuyen") != null ? request.get("phiVanChuyen").getClass().getName()
                            : "null"));
            LocalDateTime ngayChuyen = LocalDateTime.now();

            String noiDungDoi = "Update thông tin khách hàng";
            hoaDonRepo.insertTrangThaiDonHang(maHoaDon, "Đã cập nhật", ngayChuyen, noiDungDoi);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật thông tin khách hàng thành công!");
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Không tìm thấy hóa đơn!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/update_note")
    public ResponseEntity<Map<String, Object>> updateNote(
            @RequestBody Map<String, Object> request) {
        String maHoaDon = (String) request.get("maHoaDon");
        String ghiChu = (String) request.get("ghiChu");

        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Mã hóa đơn không hợp lệ!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<HoaDon> hoaDonOpt = hoaDonRepo.findById(hoaDonRepo.findByMaHoaDon(maHoaDon)
                .map(HoaDonResponse::getId_hoa_don)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon)));

        if (hoaDonOpt.isPresent()) {
            HoaDon hoaDon = hoaDonOpt.get();
            hoaDon.setGhi_chu(ghiChu != null ? ghiChu : "");
            hoaDon.setNgay_sua(LocalDateTime.now());
            hoaDonRepo.save(hoaDon);

            LocalDateTime ngayChuyen = LocalDateTime.now();

            String noiDungDoi = "Update ghi chú";
            hoaDonRepo.insertTrangThaiDonHang(maHoaDon, "Đã cập nhật", ngayChuyen, noiDungDoi);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật ghi chú thành công!");
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Không tìm thấy hóa đơn!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/ctsp_hd")
    public PageResponse<ChiTietSanPhamView> getAllCTSP_HD(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "5") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Page hoặc size không hợp lệ");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<ChiTietSanPhamView> result;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKeyword = keyword.trim().replaceAll("[^\\p{L}\\p{N}\\s]", "");
            result = chiTietSanPhamRepo.searchCTSP_HD(searchKeyword, pageable);
        } else {
            result = chiTietSanPhamRepo.getAllCTSP_HD(pageable);
        }
        return PageResponse.fromPage(result);
    }

    @PostMapping("/addSP_HD")
    @Transactional
    public ResponseEntity<Map<String, Object>> addProductsToInvoice(
            @RequestBody Map<String, Object> request) {
        String maHoaDon = (String) request.get("maHoaDon");
        List<Map<String, Object>> products = (List<Map<String, Object>>) request.get("products");

        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Mã hóa đơn không hợp lệ!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (products == null || products.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Danh sách sản phẩm không được để trống!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<HoaDonResponse> hoaDonOpt = hoaDonRepo.findByMaHoaDon(maHoaDon);
        if (!hoaDonOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Không tìm thấy hóa đơn với mã: " + maHoaDon);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Integer idHoaDon = hoaDonOpt.get().getId_hoa_don();
        try {
            Optional<HoaDon> optHD = hoaDonRepo.findById(idHoaDon);
            if (!optHD.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            HoaDon hd = optHD.get();
            boolean isOnlineCash = "Online".equalsIgnoreCase(hd.getLoai_hoa_don())
                    && "Tiền mặt".equalsIgnoreCase(hd.getHinh_thuc_thanh_toan());

            BigDecimal phiVanChuyen = hd.getPhi_van_chuyen() != null ? hd.getPhi_van_chuyen() : BigDecimal.ZERO;
            BigDecimal tongTienSauGiamCu = hd.getTong_tien_sau_giam() != null ? hd.getTong_tien_sau_giam()
                    : BigDecimal.ZERO;
            BigDecimal tongTienTruocGiamCu = hd.getTong_tien_truoc_giam() != null ? hd.getTong_tien_truoc_giam()
                    : BigDecimal.ZERO;
            // tienGiamCu = Tổng trước giảm - Tổng sau giảm (bao gồm cả voucher đã trừ)
            BigDecimal tienGiamCu = tongTienTruocGiamCu.subtract(tongTienSauGiamCu);
            BigDecimal phuThu = isOnlineCash ? BigDecimal.ZERO
                    : (hd.getPhu_thu() != null ? hd.getPhu_thu() : BigDecimal.ZERO);

            // Kiểm tra trạng thái gần nhất (bỏ qua "Đã cập nhật")
            List<TheoDoiDonHang> trangThaiList = theoDoiDonHangRepo.findByIdHoaDon(idHoaDon);
            String trangThai = trangThaiList.stream()
                    .filter(t -> !t.getTrang_thai().equals("Đã cập nhật"))
                    .max(Comparator.comparing(TheoDoiDonHang::getNgay_chuyen))
                    .map(TheoDoiDonHang::getTrang_thai)
                    .orElse(null);

            // ✅ MOD: Chỉ cho phép thêm sản phẩm khi trạng thái là "Chờ xác nhận"
            if (trangThai == null || !trangThai.equals("Chờ xác nhận")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Chỉ có thể thêm sản phẩm khi hóa đơn ở trạng thái 'Chờ xác nhận'!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            int totalQuantity = 0;
            BigDecimal tienThanhToanThem = BigDecimal.ZERO;
            // ✅ NEW: Theo dõi các sản phẩm cộng số lượng (để tính phụ thu)
            List<Map<String, Object>> mergedProducts = new ArrayList<>(); // Sản phẩm cộng số lượng (trùng giá)
            List<Map<String, Object>> newProducts = new ArrayList<>(); // Sản phẩm thêm mới (khác giá)

            for (Map<String, Object> product : products) {
                Integer idCTSP = (Integer) product.get("idCTSP");
                Integer soLuongMua = (Integer) product.get("soLuongMua");
                totalQuantity += soLuongMua;

                Optional<ChiTietSanPham> chiTietSanPhamOpt = chiTietSanPhamRepo.findById(idCTSP);
                if (!chiTietSanPhamOpt.isPresent()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Không tìm thấy sản phẩm với ID: " + idCTSP);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                ChiTietSanPham ctsp = chiTietSanPhamOpt.get();

                // ✅ VALIDATE: Kiểm tra trạng thái và tồn kho
                if (!ctsp.getTrang_thai()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Sản phẩm đã ngừng kinh doanh!");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                if (ctsp.getSo_luong() <= 0) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Sản phẩm đã hết hàng!");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                if (ctsp.getSo_luong() < soLuongMua) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Số lượng tồn kho không đủ! (Còn: " + ctsp.getSo_luong() + ")");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                // Lấy giá bán gốc
                BigDecimal giaBan = ctsp.getGia_ban();

                // Kiểm tra khuyến mãi
                BigDecimal giaSauGiam = giaBan;
                OffsetDateTime now = OffsetDateTime.now();
                List<ChiTietKhuyenMai> chiTietKhuyenMais = chiTietKhuyenMaiRepo.findAllByChiTietSanPhamId(idCTSP);
                for (ChiTietKhuyenMai ctkm : chiTietKhuyenMais) {
                    KhuyenMai km = ctkm.getKhuyenMai();
                    if (km.getTrangThai().equals("Đang diễn ra") &&
                            now.isAfter(km.getNgayBatDau()) &&
                            now.isBefore(km.getNgayHetHan())) {
                        if (ctkm.getGiaSauGiam().compareTo(giaSauGiam) < 0) {
                            giaSauGiam = ctkm.getGiaSauGiam();
                        }
                    }
                }
                // Tính tiền thanh toán thêm
                BigDecimal tienThem = giaSauGiam.multiply(new BigDecimal(soLuongMua));
                tienThanhToanThem = tienThanhToanThem.add(tienThem);
                // Kiểm tra và thêm/cập nhật sản phẩm trong hóa đơn chi tiết
                Optional<HoaDon> hdOpt = hoaDonRepo.findById(idHoaDon);
                Optional<ChiTietSanPham> ctspOpt = chiTietSanPhamRepo.findById(idCTSP);

                // ✅ LOGIC MỚI: Kiểm tra sản phẩm theo cả ID và đơn giá
                BigDecimal donGiaMoi = giaSauGiam.multiply(new BigDecimal(soLuongMua));
                Optional<HoaDonChiTiet> hoaDonChiTietOpt = hoaDonChiTietRepo.findByHoaDonAndChiTietSanPhamAndDonGia(
                        idHoaDon, idCTSP, giaSauGiam);

                if (hoaDonChiTietOpt.isPresent()) {
                    // ✅ TRÙNG GIÁ: Cộng số lượng vào dòng hiện tại
                    HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietOpt.get();
                    hoaDonChiTiet.setSo_luong(hoaDonChiTiet.getSo_luong() + soLuongMua);
                    hoaDonChiTiet.setDon_gia(giaSauGiam.multiply(new BigDecimal(hoaDonChiTiet.getSo_luong())));
                    hoaDonChiTietRepo.save(hoaDonChiTiet);

                    // ✅ Theo dõi sản phẩm cộng số lượng để tính phụ thu
                    Map<String, Object> mergedProduct = new HashMap<>();
                    mergedProduct.put("idCTSP", idCTSP);
                    mergedProduct.put("soLuongMua", soLuongMua);
                    mergedProduct.put("giaSauGiam", giaSauGiam);
                    mergedProducts.add(mergedProduct);
                } else {
                    // ✅ KHÁC GIÁ hoặc CHƯA TỒN TẠI: Tạo dòng mới
                    HoaDon hoaDon = hdOpt.get();
                    ChiTietSanPham chiTietSanPham = ctspOpt.get();
                    HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
                    hoaDonChiTiet.setHoaDon(hoaDon);
                    hoaDonChiTiet.setChiTietSanPham(chiTietSanPham);
                    hoaDonChiTiet.setSo_luong(soLuongMua);
                    hoaDonChiTiet.setDon_gia(donGiaMoi);
                    hoaDonChiTietRepo.save(hoaDonChiTiet);

                    // ✅ Theo dõi sản phẩm thêm mới (không tính phụ thu)
                    Map<String, Object> newProduct = new HashMap<>();
                    newProduct.put("idCTSP", idCTSP);
                    newProduct.put("soLuongMua", soLuongMua);
                    newProduct.put("giaSauGiam", giaSauGiam);
                    newProducts.add(newProduct);
                }
            }
            // Tính tổng tiền sản phẩm
            List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepo.findByIdHoaDon(idHoaDon);
            BigDecimal tongTienSanPham = chiTietList.stream()
                    .map(HoaDonChiTiet::getDon_gia)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Tính tổng tiền trước giảm = Tổng SP + Phí ship
            BigDecimal tongTienTruocGiam = tongTienSanPham.add(phiVanChuyen);

            // Tính lại số tiền giảm nếu có voucher
            BigDecimal tienGiam = BigDecimal.ZERO;
            Integer idVoucher = hd.getVoucher() != null ? hd.getVoucher().getId() : null;
            if (idVoucher != null) {
                Optional<Voucher> voucherOpt = voucherRepo.findById(idVoucher);
                if (voucherOpt.isPresent()) {
                    Voucher voucher = voucherOpt.get();
                    // Voucher chỉ tính trên tổng tiền sản phẩm, không tính trên phí ship
                    if (voucher.getKieuGiamGia().equals("Phần trăm")) {
                        tienGiam = tongTienSanPham.multiply(voucher.getGiaTriGiam().divide(new BigDecimal("100")));
                        if (voucher.getGiaTriToiDa() != null && tienGiam.compareTo(voucher.getGiaTriToiDa()) > 0) {
                            tienGiam = voucher.getGiaTriToiDa();
                        }
                    } else if (voucher.getKieuGiamGia().equals("Tiền mặt")) {
                        tienGiam = voucher.getGiaTriGiam();
                    }
                }
            }

            BigDecimal giamThemTuVoucher = tienGiam.subtract(tienGiamCu);
            if (giamThemTuVoucher.compareTo(BigDecimal.ZERO) < 0) {
                giamThemTuVoucher = BigDecimal.ZERO;
            }

            // ✅ LOGIC MỚI: Chỉ tính phụ thu cho sản phẩm cộng số lượng (trùng giá)
            BigDecimal tienThanhToanThemPhuThu = BigDecimal.ZERO;
            for (Map<String, Object> mergedProduct : mergedProducts) {
                BigDecimal giaSauGiam = (BigDecimal) mergedProduct.get("giaSauGiam");
                Integer soLuongMua = (Integer) mergedProduct.get("soLuongMua");
                tienThanhToanThemPhuThu = tienThanhToanThemPhuThu.add(giaSauGiam.multiply(new BigDecimal(soLuongMua)));
            }

            BigDecimal phuThuFinal = isOnlineCash ? BigDecimal.ZERO
                    : phuThu.add(tienThanhToanThemPhuThu).subtract(giamThemTuVoucher);

            // Cập nhật hóa đơn
            // tong_tien_truoc_giam = Tổng SP + Ship (chưa trừ voucher)
            // tong_tien_sau_giam = Tổng trước giảm - Voucher (tổng cuối cùng)
            hd.setTong_tien_truoc_giam(tongTienTruocGiam);
            hd.setTong_tien_sau_giam(tongTienTruocGiam.subtract(tienGiam));
            hd.setPhu_thu(phuThuFinal);
            hd.setNgay_sua(LocalDateTime.now());
            hoaDonRepo.save(hd);

            LocalDateTime ngayChuyen = LocalDateTime.now();

            String noiDungDoi = "Thêm sản phẩm vào hóa đơn";
            hoaDonRepo.insertTrangThaiDonHang(maHoaDon, "Đã cập nhật", ngayChuyen, noiDungDoi);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thêm sản phẩm vào hóa đơn thành công!");

            // ✅ NEW: Trả về thông tin chi tiết về sản phẩm đã xử lý
            response.put("mergedProducts", mergedProducts.size()); // Số sản phẩm cộng số lượng (trùng giá)
            response.put("newProducts", newProducts.size()); // Số sản phẩm thêm mới (khác giá)
            response.put("hasPriceConflict", newProducts.size() > 0); // Có xung đột giá không
            response.put("phuThuApplied", tienThanhToanThemPhuThu.compareTo(BigDecimal.ZERO) > 0); // Có tính phụ thu
                                                                                                   // không

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/removeSP_HD")
    @Transactional
    public ResponseEntity<?> removeProductFromInvoice(
            @RequestParam("maHoaDon") String maHoaDon,
            @RequestParam("idCTSP") Integer idCTSP,

            @RequestParam(value = "noiDungDoi", required = false) String noiDungDoi) {
        try {
            Optional<HoaDonResponse> hoaDonOpt = hoaDonRepo.findByMaHoaDon(maHoaDon);
            if (!hoaDonOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Hóa đơn không tồn tại");
            }
            Integer idHoaDon = hoaDonOpt.get().getId_hoa_don();
            Optional<HoaDon> hoaDonEntityOpt = hoaDonRepo.findById(idHoaDon);
            if (!hoaDonEntityOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Hóa đơn không tồn tại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            HoaDon hoaDon = hoaDonEntityOpt.get();

            boolean isOnlineCash = "Online".equalsIgnoreCase(hoaDon.getLoai_hoa_don())
                    && "Tiền mặt".equalsIgnoreCase(hoaDon.getHinh_thuc_thanh_toan());

            BigDecimal phuThu = hoaDon.getPhu_thu() != null ? hoaDon.getPhu_thu() : BigDecimal.ZERO;

            // Kiểm tra trạng thái gần nhất (bỏ qua "Đã cập nhật")
            List<TheoDoiDonHang> trangThaiList = theoDoiDonHangRepo.findByIdHoaDon(idHoaDon);
            String trangThai = trangThaiList.stream()
                    .filter(t -> !t.getTrang_thai().equals("Đã cập nhật"))
                    .max(Comparator.comparing(TheoDoiDonHang::getNgay_chuyen))
                    .map(TheoDoiDonHang::getTrang_thai)
                    .orElse(null);

            // ✅ MOD: Chỉ cho phép xóa sản phẩm khi trạng thái là "Chờ xác nhận"
            if (trangThai == null || !trangThai.equals("Chờ xác nhận")) {
                return ResponseEntity.badRequest()
                        .body("Chỉ có thể xóa sản phẩm khi hóa đơn ở trạng thái 'Chờ xác nhận'!");
            }

            // Lấy số lượng hiện tại trong chi tiết hóa đơn
            Optional<HoaDonChiTiet> hoaDonChiTietOpt = hoaDonChiTietRepo.findByChiTietSanPhamIdAndHoaDonId(idCTSP,
                    idHoaDon);
            if (!hoaDonChiTietOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Sản phẩm không tồn tại trong hóa đơn!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietOpt.get();
            int soLuongHienTai = hoaDonChiTiet.getSo_luong();

            // Hoàn lại số lượng tồn kho nếu trạng thái là "Đã xác nhận" hoặc "Chờ đóng gói"
            Optional<ChiTietSanPham> chiTietSanPhamOpt = chiTietSanPhamRepo.findById(idCTSP);
            if (!chiTietSanPhamOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy sản phẩm với ID: " + idCTSP);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            ChiTietSanPham chiTietSanPham = chiTietSanPhamOpt.get();

            // Xóa sản phẩm
            hoaDonChiTietRepo.delete(hoaDonChiTiet);
            System.out.println("✅ removeProductFromInvoice CALLED");
            System.out.println("➡️ maHoaDon = " + maHoaDon + ", idCTSP = " + idCTSP);

            // Tính tổng tiền trước giảm
            List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepo.findByIdHoaDon(idHoaDon);
            BigDecimal tongTienTruocGiam = chiTietList.stream()
                    .map(HoaDonChiTiet::getDon_gia)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Tính lại số tiền giảm nếu có voucher
            BigDecimal tienGiam = BigDecimal.ZERO;
            Integer idVoucher = hoaDon.getVoucher() != null ? hoaDon.getVoucher().getId() : null;
            if (idVoucher != null) {
                Optional<Voucher> voucherOpt = voucherRepo.findById(idVoucher);
                if (voucherOpt.isPresent()) {
                    Voucher voucher = voucherOpt.get();
                    if (voucher.getKieuGiamGia().equals("Phần trăm")) {
                        tienGiam = tongTienTruocGiam.multiply(voucher.getGiaTriGiam().divide(new BigDecimal("100")));
                        if (voucher.getGiaTriToiDa() != null && tienGiam.compareTo(voucher.getGiaTriToiDa()) > 0) {
                            tienGiam = voucher.getGiaTriToiDa();
                        }
                    } else if (voucher.getKieuGiamGia().equals("Tiền mặt")) {
                        tienGiam = voucher.getGiaTriGiam();
                    }
                }
            }

            // Cập nhật hóa đơn
            hoaDon.setTong_tien_truoc_giam(tongTienTruocGiam);
            BigDecimal phiVanChuyen = hoaDon.getPhi_van_chuyen() != null ? hoaDon.getPhi_van_chuyen() : BigDecimal.ZERO;
            hoaDon.setTong_tien_sau_giam(tongTienTruocGiam.add(phiVanChuyen).subtract(tienGiam));
            if (!isOnlineCash) {
                hoaDon.setPhu_thu(phuThu);
            }
            hoaDon.setNgay_sua(LocalDateTime.now());
            hoaDonRepo.save(hoaDon);

            LocalDateTime ngayChuyen = LocalDateTime.now();
            String noiDungDoiDefault = noiDungDoi != null ? noiDungDoi : "Xóa sản phẩm khỏi hóa đơn";
            hoaDonRepo.insertTrangThaiDonHang(maHoaDon, "Đã cập nhật", ngayChuyen, noiDungDoiDefault);

            return ResponseEntity.ok("Xóa sản phẩm thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
    }

    @PostMapping("/update_soLuong")
    @Transactional
    public ResponseEntity<?> updateProductQuantity(
            @RequestParam("maHoaDon") String maHoaDon,
            @RequestParam("idCTSP") Integer idCTSP,
            @RequestParam("quantityChange") Integer quantityChange,
            @RequestParam(value = "noiDungDoi", required = false) String noiDungDoi) {
        try {
            Optional<HoaDonResponse> hoaDonOpt = hoaDonRepo.findByMaHoaDon(maHoaDon);
            if (!hoaDonOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Hóa đơn không tồn tại");
            }
            Integer idHoaDon = hoaDonOpt.get().getId_hoa_don();
            Optional<HoaDon> hoaDonEntityOpt = hoaDonRepo.findById(idHoaDon);
            if (!hoaDonEntityOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Hóa đơn không tồn tại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            HoaDon hoaDon = hoaDonEntityOpt.get();
            boolean isOnlineCash = "Online".equalsIgnoreCase(hoaDon.getLoai_hoa_don())
                    && "Tiền mặt".equalsIgnoreCase(hoaDon.getHinh_thuc_thanh_toan());

            BigDecimal phiVanChuyen = hoaDon.getPhi_van_chuyen() != null ? hoaDon.getPhi_van_chuyen() : BigDecimal.ZERO;
            BigDecimal tongTienSauGiamCu = hoaDon.getTong_tien_sau_giam() != null ? hoaDon.getTong_tien_sau_giam()
                    : BigDecimal.ZERO;
            BigDecimal tongTienTruocGiamCu = hoaDon.getTong_tien_truoc_giam() != null ? hoaDon.getTong_tien_truoc_giam()
                    : BigDecimal.ZERO;
            // tienGiamCu = Tổng trước giảm - Tổng sau giảm (bao gồm cả voucher đã trừ)
            BigDecimal tienGiamCu = tongTienTruocGiamCu.subtract(tongTienSauGiamCu);
            BigDecimal phuThu = isOnlineCash ? BigDecimal.ZERO
                    : (hoaDon.getPhu_thu() != null ? hoaDon.getPhu_thu() : BigDecimal.ZERO);

            // Kiểm tra trạng thái gần nhất (bỏ qua "Đã cập nhật")
            List<TheoDoiDonHang> trangThaiList = theoDoiDonHangRepo.findByIdHoaDon(idHoaDon);
            String trangThai = trangThaiList.stream()
                    .filter(t -> !t.getTrang_thai().equals("Đã cập nhật"))
                    .max(Comparator.comparing(TheoDoiDonHang::getNgay_chuyen))
                    .map(TheoDoiDonHang::getTrang_thai)
                    .orElse(null);

            // ✅ MOD: Chỉ cho phép sửa số lượng khi trạng thái là "Chờ xác nhận"
            if (trangThai == null || !trangThai.equals("Chờ xác nhận")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Chỉ có thể sửa số lượng khi hóa đơn ở trạng thái 'Chờ xác nhận'!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Lấy thông tin sản phẩm
            Optional<ChiTietSanPham> chiTietSanPhamOpt = chiTietSanPhamRepo.findById(idCTSP);
            if (!chiTietSanPhamOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy sản phẩm với ID: " + idCTSP);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            ChiTietSanPham chiTietSanPham = chiTietSanPhamOpt.get();

            // Lấy giá bán gốc
            BigDecimal giaBan = chiTietSanPham.getGia_ban();

            // Kiểm tra khuyến mãi
            BigDecimal giaSauGiam = giaBan;
            List<ChiTietKhuyenMai> chiTietKhuyenMais = chiTietKhuyenMaiRepo.findAllByChiTietSanPhamId(idCTSP);
            for (ChiTietKhuyenMai ctkm : chiTietKhuyenMais) {
                KhuyenMai km = ctkm.getKhuyenMai();
                if (km.getTrangThai().equals("Đang diễn ra") &&
                        LocalDateTime.now().isAfter(km.getNgayBatDau().toLocalDateTime()) &&
                        LocalDateTime.now().isBefore(km.getNgayHetHan().toLocalDateTime())) {
                    if (ctkm.getGiaSauGiam().compareTo(giaSauGiam) < 0) {
                        giaSauGiam = ctkm.getGiaSauGiam();
                    }
                }
            }

            // Lấy số lượng hiện tại trong chi tiết hóa đơn
            Optional<HoaDonChiTiet> hoaDonChiTietOpt = hoaDonChiTietRepo.findByChiTietSanPhamIdAndHoaDonId(idCTSP,
                    idHoaDon);
            if (!hoaDonChiTietOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Sản phẩm không tồn tại trong hóa đơn!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietOpt.get();
            int soLuongHienTai = hoaDonChiTiet.getSo_luong();
            int soLuongMoi = soLuongHienTai + quantityChange;

            // Kiểm tra số lượng mới không âm
            if (soLuongMoi < 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Số lượng không thể âm!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Tính tiền thanh toán thêm
            BigDecimal tienThanhToanThem = quantityChange > 0 ? giaSauGiam.multiply(new BigDecimal(quantityChange))
                    : BigDecimal.ZERO;

            // Cập nhật số lượng trong chi tiết hóa đơn
            hoaDonChiTiet.setSo_luong(soLuongMoi);
            hoaDonChiTiet.setDon_gia(giaSauGiam.multiply(new BigDecimal(soLuongMoi)));
            hoaDonChiTietRepo.save(hoaDonChiTiet);

            // Tính tổng tiền sản phẩm
            List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepo.findByIdHoaDon(idHoaDon);
            BigDecimal tongTienSanPham = chiTietList.stream()
                    .map(HoaDonChiTiet::getDon_gia)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Tính tổng tiền trước giảm = Tổng SP + Phí ship
            BigDecimal tongTienTruocGiam = tongTienSanPham.add(phiVanChuyen);

            // Cập nhật phụ thu nếu tăng số lượng
            BigDecimal phuThuFinal = phuThu;
            if (quantityChange > 0 && !isOnlineCash) {
                phuThuFinal = phuThu.add(tienThanhToanThem);
            }

            // Tính lại số tiền giảm nếu có voucher
            BigDecimal tienGiam = BigDecimal.ZERO;
            Integer idVoucher = hoaDon.getVoucher() != null ? hoaDon.getVoucher().getId() : null;
            if (idVoucher != null) {
                Optional<Voucher> voucherOpt = voucherRepo.findById(idVoucher);
                if (voucherOpt.isPresent()) {
                    Voucher voucher = voucherOpt.get();
                    // Voucher chỉ tính trên tổng tiền sản phẩm, không tính trên phí ship
                    if (voucher.getKieuGiamGia().equals("Phần trăm")) {
                        tienGiam = tongTienSanPham.multiply(voucher.getGiaTriGiam().divide(new BigDecimal("100")));
                        if (voucher.getGiaTriToiDa() != null && tienGiam.compareTo(voucher.getGiaTriToiDa()) > 0) {
                            tienGiam = voucher.getGiaTriToiDa();
                        }
                    } else if (voucher.getKieuGiamGia().equals("Tiền mặt")) {
                        tienGiam = voucher.getGiaTriGiam();
                    }
                }
            }

            BigDecimal giamThemTuVoucher = tienGiam.subtract(tienGiamCu);
            if (giamThemTuVoucher.compareTo(BigDecimal.ZERO) < 0) {
                giamThemTuVoucher = BigDecimal.ZERO;
            }

            if (!isOnlineCash) {
                phuThuFinal = phuThuFinal.subtract(giamThemTuVoucher);
            } else {
                phuThuFinal = BigDecimal.ZERO;
            }

            // Cập nhật hóa đơn
            // tong_tien_truoc_giam = Tổng SP + Ship (chưa trừ voucher)
            // tong_tien_sau_giam = Tổng trước giảm - Voucher (tổng cuối cùng)
            hoaDon.setTong_tien_truoc_giam(tongTienTruocGiam);
            hoaDon.setTong_tien_sau_giam(tongTienTruocGiam.subtract(tienGiam));
            hoaDon.setPhu_thu(phuThuFinal);
            hoaDon.setNgay_sua(LocalDateTime.now());
            hoaDonRepo.save(hoaDon);

            LocalDateTime ngayChuyen = LocalDateTime.now();
            String noiDungDoiDefault = noiDungDoi != null ? noiDungDoi : "Update số lượng sản phẩm";
            hoaDonRepo.insertTrangThaiDonHang(maHoaDon, "Đã cập nhật", ngayChuyen, noiDungDoiDefault);

            return ResponseEntity.ok("Cập nhật số lượng thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật số lượng: " + e.getMessage());
        }
    }

    // lềnh thay đổi

    @GetMapping("/khach-hang/{idKhachHang}")
    public ResponseEntity<?> getDonHangByKhachHang(@PathVariable Integer idKhachHang) {
        try {
            List<HoaDonResponse> hoaDons = hoaDonService.getHoaDonByKhachHangId(idKhachHang);
            System.out.println("✅ Số đơn hàng tìm thấy cho idKhachHang " + idKhachHang + ": " + hoaDons.size());
            // Gỡ lỗi: In giá trị ghi_chu của mỗi hóa đơn
            hoaDons.forEach(
                    hd -> System.out.println("Ghi_chu của hóa đơn " + hd.getMa_hoa_don() + ": " + hd.getGhi_chu()));
            return ResponseEntity.ok(hoaDons);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy đơn hàng cho idKhachHang " + idKhachHang + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
        }
    }

    @GetMapping("/count/{idKhachHang}")
    public ResponseEntity<Integer> countHoaDonByKhachHang(@PathVariable Integer idKhachHang) {
        int count = hoaDonService.countHoaDonByKhachHangId(idKhachHang);
        return ResponseEntity.ok(count);
    }

    /**
     * API thay thế cho getHDCTBymaHD khi có lỗi với gia_sau_giam
     * Sử dụng truy vấn SQL đơn giản hơn không tham chiếu đến cột gia_sau_giam
     */

    @GetMapping("/get-products/{maHoaDon}")
    public List<Map<String, Object>> getOrderProducts(@PathVariable String maHoaDon) {
        try {
            // Lấy ID hóa đơn từ mã hóa đơn
            HoaDonResponse hoaDon = hoaDonRepo.findByMaHoaDon(maHoaDon)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon));
            Integer idHoaDon = hoaDon.getId_hoa_don();

            // Truy vấn SQL cập nhật để bao gồm id_san_pham, id_chi_tiet_san_pham,
            // ten_mau_sac, gia_tri
            String sql = "SELECT s.id_san_pham, t.id_chi_tiet_san_pham, s.hinh_anh, s.ma_san_pham, s.ten_san_pham, " +
                    "c.so_luong, c.don_gia, c.so_luong * c.don_gia as 'tong_tien', " +
                    "ms.ten_mau_sac, kt.gia_tri " +
                    "FROM hoa_don h " +
                    "JOIN hoa_don_chi_tiet c ON h.id_hoa_don = c.id_hoa_don " +
                    "JOIN chi_tiet_san_pham t ON c.id_chi_tiet_san_pham = t.id_chi_tiet_san_pham " +
                    "JOIN san_pham s ON s.id_san_pham = t.id_san_pham " +
                    "LEFT JOIN mau_sac ms ON t.id_mau_sac = ms.id_mau_sac " +
                    "LEFT JOIN kich_thuoc kt ON t.id_kich_thuoc = kt.id_kich_thuoc " +
                    "WHERE h.id_hoa_don = ?";

            return jdbcTemplate.queryForList(sql, idHoaDon);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy thông tin sản phẩm trong đơn hàng: " + e.getMessage());
        }
    }

    /**
     * API dự phòng cuối cùng chỉ trả về thông tin rất cơ bản của đơn hàng
     */

    @GetMapping("/basic-order-detail")
    public Map<String, Object> getBasicOrderDetail(@RequestParam("ma_hoa_don") String maHoaDon) {
        try {
            // Lấy thông tin cơ bản của hóa đơn
            HoaDonResponse hoaDon = hoaDonRepo.findByMaHoaDon(maHoaDon)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon));
            Integer idHoaDon = hoaDon.getId_hoa_don();

            // Truy vấn SQL đơn giản nhất để lấy thông tin sản phẩm
            String sql = "SELECT sp.ten_san_pham, hdct.so_luong, hdct.don_gia " +
                    "FROM hoa_don_chi_tiet hdct " +
                    "JOIN chi_tiet_san_pham ctsp ON hdct.id_chi_tiet_san_pham = ctsp.id_chi_tiet_san_pham " +
                    "JOIN san_pham sp ON ctsp.id_san_pham = sp.id_san_pham " +
                    "WHERE hdct.id_hoa_don = ?";

            List<Map<String, Object>> products = jdbcTemplate.queryForList(sql, idHoaDon);

            Map<String, Object> result = new HashMap<>();
            result.put("hoaDon", hoaDon);
            result.put("san_pham", products);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy thông tin cơ bản của đơn hàng: " + e.getMessage());
        }
    }

    // Lấy phí vận chuyển theo ID hóa đơn

    @GetMapping("/phi-van-chuyen/{idHoaDon}")
    public ResponseEntity<?> getPhiVanChuyen(@PathVariable Integer idHoaDon) {
        try {
            String sql = "select phi_van_chuyen from hoa_don where id_hoa_don = ?";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, idHoaDon);

            BigDecimal phiVanChuyen = (BigDecimal) result.get("phi_van_chuyen");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("phi_van_chuyen", phiVanChuyen);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy phí vận chuyển: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Endpoint mới cho trả hàng

    @GetMapping("/{maHoaDon}/chi-tiet-tra-hang")
    public ResponseEntity<Map<String, Object>> layChiTietHoaDonTraHang(@PathVariable String maHoaDon) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Fetch invoice details
            HoaDonResponse hoaDon = hoaDonRepo.getHoaDonWithReturnInfoByMaHoaDon(maHoaDon);
            if (hoaDon == null) {
                response.put("thanh_cong", false);
                response.put("thong_bao", "Không tìm thấy hóa đơn với mã: " + maHoaDon);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Validate order status in TheoDoiDonHang
            List<TheoDoiDonHang> statusList = theoDoiDonHangRepo
                    .findByIdHoaDonOrderByNgayChuyenDesc(hoaDon.getId_hoa_don());
            // Check if the invoice has already been returned
            boolean hasReturn = statusList.stream().anyMatch(status -> "Trả hàng".equals(status.getTrang_thai()));
            if (hasReturn) {
                response.put("thanh_cong", false);
                response.put("thong_bao", "Mỗi hóa đơn chỉ được phép trả hàng một lần duy nhất!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (statusList.isEmpty() || !"Hoàn thành".equals(statusList.get(0).getTrang_thai())) {
                response.put("thanh_cong", false);
                response.put("thong_bao", "Hóa đơn không ở trạng thái Hoàn thành!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            LocalDateTime ngayChuyen = statusList.get(0).getNgay_chuyen();
            LocalDateTime now = LocalDateTime.now();
            long daysBetween = ChronoUnit.DAYS.between(ngayChuyen.toLocalDate(), now.toLocalDate());
            if (daysBetween > 14) {
                response.put("thanh_cong", false);
                response.put("thong_bao", "Hóa đơn đã quá 14 ngày kể từ khi hoàn thành, không thể trả hàng!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            // Fetch invoice details
            List<HoaDonChiTietResponse> chiTietHoaDons = hoaDonRepo.getChiTietHoaDonByMaHoaDon(maHoaDon);
            if (chiTietHoaDons == null || chiTietHoaDons.isEmpty()) {
                response.put("thanh_cong", false);
                response.put("thong_bao", "Không tìm thấy chi tiết hóa đơn!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Fetch customer information
            HoaDonChiTietResponse thongTinKhachHang = hoaDonRepo.getKhachHangInfoByMaHoaDon(maHoaDon);
            if (thongTinKhachHang == null) {
                response.put("thanh_cong", false);
                response.put("thong_bao", "Không tìm thấy thông tin khách hàng!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Build successful response
            response.put("thanh_cong", true);
            response.put("ma_hoa_don", hoaDon.getMa_hoa_don());
            response.put("trang_thai", hoaDon.getTrang_thai());
            response.put("hinh_thuc_thanh_toan", hoaDon.getHinh_thuc_thanh_toan());
            response.put("ngay_tao", hoaDon.getNgay_tao());
            response.put("ho_ten", hoaDon.getHo_ten());
            response.put("dia_chi", hoaDon.getDia_chi());
            response.put("gia_tri_giam", hoaDon.getGia_tri_giam() != null ? hoaDon.getGia_tri_giam() : BigDecimal.ZERO);
            response.put("tong_tien",
                    hoaDon.getTong_tien_truoc_giam() != null ? hoaDon.getTong_tien_truoc_giam() : BigDecimal.ZERO);
            response.put("tong_tien_thanh_toan",
                    hoaDon.getTong_tien_sau_giam() != null ? hoaDon.getTong_tien_sau_giam() : BigDecimal.ZERO);
            response.put("trang_thai_don_hang", hoaDon.getTrang_thai_thanh_toan());
            response.put("trang_thai_tra_hang",
                    hoaDon.getTrang_thai_tra_hang() != null ? hoaDon.getTrang_thai_tra_hang() : "Chưa yêu cầu");
            response.put("chi_tiet_hoa_don", chiTietHoaDons);
            response.put("thong_tin_khach_hang", thongTinKhachHang);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("thanh_cong", false);
            response.put("thong_bao", "Lỗi khi lấy thông tin chi tiết hóa đơn: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /// Của lềnh
    // lềnh sửa

    @PutMapping("/huy-don/{idHoaDon}")
    @Transactional
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Integer idHoaDon) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<HoaDon> hoaDonOpt = hoaDonRepo.findById(idHoaDon);

            if (!hoaDonOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            HoaDon hoaDon = hoaDonOpt.get();

            // Lấy lịch sử trạng thái từ bảng theo_doi_don_hang
            List<TheoDoiDonHangResponse> trangThaiHistory = hoaDonRepo.findTrangThaiHistoryByIdHoaDon(idHoaDon);
            if (trangThaiHistory.isEmpty()
                    || !"Chờ xác nhận".equals(trangThaiHistory.get(trangThaiHistory.size() - 1).getTrang_thai())) {
                response.put("success", false);
                response.put("message", "Chỉ có thể hủy đơn hàng ở trạng thái Chờ xác nhận!");
                return ResponseEntity.badRequest().body(response);
            }

            // Hoàn lại số lượng sản phẩm
            List<HoaDonChiTiet> chiTietList = hoaDon.getHoaDonChiTietList();
            for (HoaDonChiTiet chiTiet : chiTietList) {
                ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
                ctsp.setSo_luong(ctsp.getSo_luong() + chiTiet.getSo_luong());
                chiTietSanPhamRepo.save(ctsp);
            }

            // Hoàn lại voucher nếu có
            if (hoaDon.getVoucher() != null) {
                Voucher voucher = hoaDon.getVoucher();
                voucher.setSoLuong(voucher.getSoLuong() + 1);
                voucherRepo.save(voucher);
            }

            // Cập nhật trạng thái đơn hàng trong hoa_don
            hoaDon.setTrang_thai("Đã hủy");
            hoaDon.setNgay_sua(LocalDateTime.now());
            hoaDonRepo.save(hoaDon);

            // Thêm vào lịch sử theo dõi đơn hàng
            TheoDoiDonHang theoDoiDonHang = new TheoDoiDonHang();
            theoDoiDonHang.setHoaDon(hoaDon);
            theoDoiDonHang.setTrang_thai("Đã hủy");
            theoDoiDonHang.setNgay_chuyen(LocalDateTime.now());
            theoDoiDonHangRepo.save(theoDoiDonHang);

            response.put("success", true);
            response.put("message", "Hủy đơn hàng thành công!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi hủy đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
