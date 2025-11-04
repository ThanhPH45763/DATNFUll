package com.example.duanbe.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import com.example.duanbe.entity.DiaChiKhachHang;
import com.example.duanbe.entity.HoaDon;
import com.example.duanbe.entity.KhachHang;
import com.example.duanbe.repository.DiaChiKhachHangRepo;
import com.example.duanbe.repository.HoaDonRepo;
import com.example.duanbe.repository.KhachHangRepo;
import com.example.duanbe.request.KhachHangRequest;
import com.example.duanbe.request.LoginRequest;
import com.example.duanbe.request.QuenMKRequest;
import com.example.duanbe.request.RegisterRequest;
import com.example.duanbe.request.SupportRequestDTO;
import com.example.duanbe.request.UpdateOrderCustomerInfoDTO;
import com.example.duanbe.response.HoaDonResponse;
import com.example.duanbe.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "http://localhost:5173/", allowedHeaders = "*", methods = { 
    RequestMethod.GET, RequestMethod.DELETE, RequestMethod.POST, RequestMethod.PUT 
})
@RequestMapping("/api/khach-hang")
public class KhachHangController {
    
    @Autowired
    private KhachHangRepo khachHangRepo;

    @Autowired
    private DiaChiKhachHangRepo diaChiKhachHangRepo;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private HoaDonRepo hoaDonRepo;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ==================== VIEW & SEARCH ====================
    
    @GetMapping("/view")
    public ResponseEntity<Map<String, Object>> getKhachHang(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "5") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) String trangThai,
            @RequestParam(value = "updatedId", required = false) Integer updatedId) {

        Pageable pageable = PageRequest.of(page, size);
        Page<KhachHang> danhSachKhachHang;

        if (trangThai != null && !trangThai.isEmpty()) {
            danhSachKhachHang = khachHangRepo.locKhachHangTheoTrangThai(trangThai, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            String trimmedKeyword = keyword.trim();
            danhSachKhachHang = khachHangRepo.timKhachHang(trimmedKeyword, pageable);
        } else {
            danhSachKhachHang = khachHangRepo.findAllSortedByIdDesc(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        List<KhachHang> khachHangList = danhSachKhachHang.getContent();

        if (updatedId != null && page == 0) {
            Optional<KhachHang> updatedKhachHangOpt = khachHangRepo.findById(updatedId);
            if (updatedKhachHangOpt.isPresent()) {
                KhachHang updatedKhachHang = updatedKhachHangOpt.get();
                khachHangList.removeIf(kh -> kh.getIdKhachHang().equals(updatedId));
                khachHangList.add(0, updatedKhachHang);
            }
        }

        if (khachHangList.isEmpty() && (keyword != null || trangThai != null)) {
            response.put("message", "Không tìm thấy khách hàng nào phù hợp!");
        }

        Map<Integer, String> diaChiMap = new HashMap<>();
        for (KhachHang kh : khachHangList) {
            var diaChiList = diaChiKhachHangRepo.findByKhachHangId(kh.getIdKhachHang());
            String diaChiMacDinh = diaChiList.stream()
                    .filter(DiaChiKhachHang::getDiaChiMacDinh)
                    .map(DiaChiKhachHang::getDiaChiKhachHang)
                    .findFirst()
                    .orElse("Chưa có địa chỉ mặc định");
            diaChiMap.put(kh.getIdKhachHang(), diaChiMacDinh);
        }

        response.put("danhSachKhachHang", khachHangList);
        response.put("diaChiMap", diaChiMap);
        response.put("currentPage", page);
        response.put("totalPages", danhSachKhachHang.getTotalPages());
        response.put("totalElements", danhSachKhachHang.getTotalElements());
        response.put("trangThai", trangThai);
        response.put("keyword", keyword);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getAllKH")
    public ResponseEntity<Map<String, Object>> getAllKhachHang() {
        List<KhachHang> khachHangList = khachHangRepo.findAll(Sort.by(Sort.Direction.DESC, "idKhachHang"));

        Map<Integer, String> diaChiMap = new HashMap<>();
        for (KhachHang kh : khachHangList) {
            var diaChiList = diaChiKhachHangRepo.findByKhachHangId(kh.getIdKhachHang());
            String diaChiMacDinh = diaChiList.stream()
                    .filter(DiaChiKhachHang::getDiaChiMacDinh)
                    .map(DiaChiKhachHang::getDiaChiKhachHang)
                    .findFirst()
                    .orElse("Chưa có địa chỉ mặc định");
            diaChiMap.put(kh.getIdKhachHang(), diaChiMacDinh);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("danhSachKhachHang", khachHangList);
        response.put("diaChiMap", diaChiMap);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<Map<String, Object>> getKhachHangDetail(@PathVariable("id") Integer id) {
        Map<String, Object> response = new HashMap<>();

        KhachHang khachHang = khachHangRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        List<DiaChiKhachHang> diaChiList = diaChiKhachHangRepo.findByKhachHangId(khachHang.getIdKhachHang());

        response.put("khachHang", khachHang);
        response.put("diaChiList", diaChiList);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/details")
    public ResponseEntity<KhachHang> getKhachHangDetails(@RequestParam String tenDangNhap) {
        Optional<KhachHang> khachHang = khachHangRepo.findByTenDangNhap(tenDangNhap);
        return khachHang.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ==================== REGISTER & LOGIN ====================

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerKhachHang(
            @Valid @RequestBody RegisterRequest registerRequest,
            BindingResult result) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (result.hasErrors()) {
            Map<String, String> fieldErrors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                fieldErrors.put(error.getField(), error.getDefaultMessage());
            }
            response.put("fieldErrors", fieldErrors);
            return ResponseEntity.badRequest().body(response);
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            Map<String, String> fieldErrors = new HashMap<>();
            fieldErrors.put("confirmPassword", "Mật khẩu xác nhận không khớp");
            response.put("fieldErrors", fieldErrors);
            return ResponseEntity.badRequest().body(response);
        }

        LocalDate ngaySinh = registerRequest.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int tuoi = Period.between(ngaySinh, LocalDate.now()).getYears();
        if (tuoi < 14) {
            Map<String, String> fieldErrors = new HashMap<>();
            fieldErrors.put("birthDate", "Bạn phải đủ 14 tuổi để đăng ký");
            response.put("fieldErrors", fieldErrors);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            if (khachHangRepo.existsByEmail(registerRequest.getEmail())) {
                response.put("error", "Email đã được sử dụng!");
                return ResponseEntity.badRequest().body(response);
            }

            if (khachHangRepo.existsByTenDangNhap(registerRequest.getEmail())) {
                response.put("error", "Tên đăng nhập đã tồn tại!");
                return ResponseEntity.badRequest().body(response);
            }

            String maKhachHang = generateMaKhachHang();
            String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());

            KhachHang khachHang = new KhachHang();
            khachHang.setMaKhachHang(maKhachHang);
            khachHang.setTenDangNhap(registerRequest.getEmail());
            khachHang.setMatKhau(hashedPassword);
            khachHang.setHoTen(registerRequest.getFullName());
            khachHang.setSoDienThoai(registerRequest.getPhone());
            khachHang.setEmail(registerRequest.getEmail());
            khachHang.setNgaySinh(registerRequest.getBirthDate());
            khachHang.setTrangThai("Đang hoạt động");
            khachHang.setNgayLap(LocalDateTime.now());

            if ("Nam".equals(registerRequest.getGender())) {
                khachHang.setGioiTinh(true);
            } else if ("Nữ".equals(registerRequest.getGender())) {
                khachHang.setGioiTinh(false);
            }

            khachHang = khachHangRepo.save(khachHang);

            sendWelcomeEmail(khachHang, registerRequest.getPassword());

            response.put("successMessage", "Đăng ký thành công!");
            response.put("khachHang", khachHang);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Có lỗi xảy ra khi đăng ký: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            BindingResult result,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        
        if (result.hasErrors()) {
            Map<String, String> fieldErrors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                fieldErrors.put(error.getField(), error.getDefaultMessage());
            }
            response.put("fieldErrors", fieldErrors);
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            Optional<KhachHang> khachHangOpt = khachHangRepo.findByTenDangNhap(loginRequest.getEmail());
            
            if (!khachHangOpt.isPresent()) {
                response.put("error", "Tài khoản không tồn tại!");
                return ResponseEntity.badRequest().body(response);
            }
            
            KhachHang khachHang = khachHangOpt.get();
            
            if (!"Đang hoạt động".equals(khachHang.getTrangThai())) {
                response.put("error", "Tài khoản đã bị ngừng hoạt động!");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!passwordEncoder.matches(loginRequest.getPassword(), khachHang.getMatKhau())) {
                response.put("error", "Mật khẩu không đúng!");
                return ResponseEntity.badRequest().body(response);
            }
            
            HttpSession session = request.getSession();
            session.setAttribute("khachHang", khachHang);
            
            response.put("successMessage", "Đăng nhập thành công!");
            response.put("khachHang", khachHang);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== ADD & UPDATE ====================

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addKhachHang(@RequestBody KhachHangRequest khachHangRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (khachHangRepo.existsByEmail(khachHangRequest.getEmail())) {
                response.put("error", "Email đã được sử dụng!");
                return ResponseEntity.badRequest().body(response);
            }

            String maKhachHang = khachHangRequest.getMaKhachHang();
            if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
                maKhachHang = generateMaKhachHang();
            } else {
                Optional<KhachHang> existingKhachHang = khachHangRepo.findByMaKhachHang(maKhachHang);
                if (existingKhachHang.isPresent()) {
                    response.put("error", "Mã khách hàng đã tồn tại!");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            khachHangRequest.setMaKhachHang(maKhachHang);

            String randomPassword = generateRandomPassword();
            String hashedPassword = passwordEncoder.encode(randomPassword);

            KhachHang khachHang = new KhachHang();
            BeanUtils.copyProperties(khachHangRequest, khachHang);
            khachHang.setTenDangNhap(khachHangRequest.getEmail());
            khachHang.setMatKhau(hashedPassword);
            khachHang.setNgayLap(LocalDateTime.now());
            khachHang = khachHangRepo.save(khachHang);

            if (khachHangRequest.getDiaChiList() != null && !khachHangRequest.getDiaChiList().isEmpty()) {
                for (KhachHangRequest.DiaChiRequest diaChiReq : khachHangRequest.getDiaChiList()) {
                    DiaChiKhachHang diaChiKhachHang = new DiaChiKhachHang();
                    diaChiKhachHang.setKhachHang(khachHang);
                    BeanUtils.copyProperties(diaChiReq, diaChiKhachHang);
                    diaChiKhachHangRepo.save(diaChiKhachHang);
                }
            }

            sendWelcomeEmailWithPassword(khachHang, randomPassword);

            response.put("successMessage", "Thêm khách hàng thành công!");
            response.put("khachHang", khachHang);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Có lỗi xảy ra khi thêm khách hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/addKHMoi")
    public ResponseEntity<Map<String, Object>> addKhachHangNhanh(
            @Valid @RequestBody KhachHangRequest khachHangRequest,
            BindingResult result) {

        Map<String, Object> response = new HashMap<>();

        khachHangRequest.setTrangThai("Đang hoạt động");
        khachHangRequest.setGioiTinh(true);
        khachHangRequest.setNgaySinh(new Date());
        
        try {
            if (khachHangRepo.existsByEmail(khachHangRequest.getEmail())) {
                response.put("error", "Email đã được sử dụng!");
                return ResponseEntity.badRequest().body(response);
            }

            String maKhachHang = khachHangRequest.getMaKhachHang();
            if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
                maKhachHang = generateMaKhachHang();
            } else {
                Optional<KhachHang> existingKhachHang = khachHangRepo.findByMaKhachHang(maKhachHang);
                if (existingKhachHang.isPresent()) {
                    response.put("error", "Mã khách hàng đã tồn tại!");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            khachHangRequest.setMaKhachHang(maKhachHang);

            String randomPassword = generateRandomPassword();
            String hashedPassword = passwordEncoder.encode(randomPassword);

            KhachHang khachHang = new KhachHang();
            BeanUtils.copyProperties(khachHangRequest, khachHang);
            khachHang.setTenDangNhap(khachHangRequest.getEmail());
            khachHang.setMatKhau(hashedPassword);
            khachHang.setNgayLap(LocalDateTime.now());
            khachHang = khachHangRepo.save(khachHang);

            if (khachHangRequest.getDiaChiList() != null && !khachHangRequest.getDiaChiList().isEmpty()) {
                for (KhachHangRequest.DiaChiRequest diaChiReq : khachHangRequest.getDiaChiList()) {
                    DiaChiKhachHang diaChiKhachHang = new DiaChiKhachHang();
                    diaChiKhachHang.setKhachHang(khachHang);
                    BeanUtils.copyProperties(diaChiReq, diaChiKhachHang);
                    diaChiKhachHangRepo.save(diaChiKhachHang);
                }
            }

            sendWelcomeEmailWithPassword(khachHang, randomPassword);

            response.put("successMessage", "Thêm khách hàng thành công!");
            response.put("khachHang", khachHang);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Có lỗi xảy ra khi thêm khách hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateKhachHang(@RequestBody KhachHangRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            KhachHang khachHang = khachHangRepo.findById(request.getIdKhachHang())
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

            if (!khachHang.getMaKhachHang().equals(request.getMaKhachHang())) {
                Optional<KhachHang> existing = khachHangRepo.findByMaKhachHang(request.getMaKhachHang());
                if (existing.isPresent()) {
                    response.put("error", "Mã khách hàng đã tồn tại!");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            BeanUtils.copyProperties(request, khachHang);
            khachHang = khachHangRepo.save(khachHang);

            var existingDiaChiList = diaChiKhachHangRepo.findByKhachHangId(khachHang.getIdKhachHang());
            diaChiKhachHangRepo.deleteAll(existingDiaChiList);

            if (request.getDiaChiList() != null && !request.getDiaChiList().isEmpty()) {
                for (KhachHangRequest.DiaChiRequest diaChiReq : request.getDiaChiList()) {
                    DiaChiKhachHang diaChi = new DiaChiKhachHang();
                    diaChi.setKhachHang(khachHang);
                    BeanUtils.copyProperties(diaChiReq, diaChi);
                    diaChiKhachHangRepo.save(diaChi);
                }
            }

            response.put("message", "Cập nhật khách hàng thành công!");
            response.put("khachHang", khachHang);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Lỗi khi cập nhật khách hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/edit/{id}")
    public ResponseEntity<Map<String, Object>> getKhachHangForEdit(@PathVariable("id") Integer id) {
        Map<String, Object> response = new HashMap<>();

        Optional<KhachHang> khachHangOpt = khachHangRepo.findById(id);
        if (!khachHangOpt.isPresent()) {
            response.put("error", "Không tìm thấy khách hàng với ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        KhachHang khachHang = khachHangOpt.get();
        KhachHangRequest request = new KhachHangRequest();
        BeanUtils.copyProperties(khachHang, request);

        var diaChiList = diaChiKhachHangRepo.findByKhachHangId(khachHang.getIdKhachHang());
        for (DiaChiKhachHang diaChi : diaChiList) {
            KhachHangRequest.DiaChiRequest diaChiReq = new KhachHangRequest.DiaChiRequest();
            diaChiReq.setSoNha(diaChi.getSoNha());
            diaChiReq.setXaPhuong(diaChi.getXaPhuong());
            diaChiReq.setQuanHuyen(diaChi.getQuanHuyen());
            diaChiReq.setTinhThanhPho(diaChi.getTinhThanhPho());
            diaChiReq.setDiaChiMacDinh(diaChi.getDiaChiMacDinh());
            request.getDiaChiList().add(diaChiReq);
        }

        response.put("khachHang", request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chuyen-trang-thai")
    public ResponseEntity<Map<String, Object>> changeTrangThai(@RequestParam("idKhachHang") Integer idKhachHang) {
        Map<String, Object> response = new HashMap<>();

        KhachHang khachHang = khachHangRepo.findById(idKhachHang)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        
        if ("Đang hoạt động".equals(khachHang.getTrangThai())) {
            khachHang.setTrangThai("Không hoạt động");
        } else {
            khachHang.setTrangThai("Đang hoạt động");
        }
        khachHangRepo.save(khachHang);

        response.put("successMessage", "Thay đổi trạng thái thành công!");
        response.put("khachHang", khachHang);
        return ResponseEntity.ok(response);
    }

    // ==================== PASSWORD MANAGEMENT ====================

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody QuenMKRequest request) {
        Map<String, Object> response = new HashMap<>();

        Optional<KhachHang> khachHangOpt = khachHangRepo.findByEmail(request.getEmail());
        if (!khachHangOpt.isPresent()) {
            response.put("error", "Email không tồn tại trong hệ thống!");
            return ResponseEntity.badRequest().body(response);
        }

        KhachHang khachHang = khachHangOpt.get();
        if (!"Đang hoạt động".equals(khachHang.getTrangThai())) {
            response.put("error", "Tài khoản của bạn đã bị ngừng hoạt động!");
            return ResponseEntity.badRequest().body(response);
        }

        String newPassword = generateRandomPassword();
        String hashedPassword = passwordEncoder.encode(newPassword);
        khachHang.setMatKhau(hashedPassword);
        khachHangRepo.save(khachHang);

        sendResetPasswordEmail(khachHang, newPassword);

        response.put("successMessage", "Mật khẩu mới đã được gửi đến email của bạn!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestParam String email,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        
        Map<String, Object> response = new HashMap<>();

        Optional<KhachHang> khachHangOpt = khachHangRepo.findByEmail(email);
        if (!khachHangOpt.isPresent()) {
            response.put("error", "Khách hàng không tồn tại!");
            return ResponseEntity.badRequest().body(response);
        }

        KhachHang khachHang = khachHangOpt.get();

        if (!"Đang hoạt động".equals(khachHang.getTrangThai())) {
            response.put("error", "Tài khoản đã bị ngừng hoạt động!");
            return ResponseEntity.badRequest().body(response);
        }

        if (!passwordEncoder.matches(oldPassword, khachHang.getMatKhau())) {
            response.put("error", "Mật khẩu cũ không đúng!");
            return ResponseEntity.badRequest().body(response);
        }

        khachHang.setMatKhau(passwordEncoder.encode(newPassword));
        khachHangRepo.save(khachHang);

        response.put("successMessage", "Đổi mật khẩu thành công!");
        return ResponseEntity.ok(response);
    }

    // ==================== DIA CHI MANAGEMENT ====================

    @PostMapping("/dia-chi/add")
    public ResponseEntity<Map<String, Object>> addDiaChi(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer idKhachHang = Integer.parseInt(request.get("idKhachHang").toString());
            String soNha = (String) request.get("soNha");
            String xaPhuong = (String) request.get("xaPhuong");
            String quanHuyen = (String) request.get("quanHuyen");
            String tinhThanhPho = (String) request.get("tinhThanhPho");
            Boolean diaChiMacDinh = (Boolean) request.getOrDefault("diaChiMacDinh", false);

            if (soNha == null || xaPhuong == null || quanHuyen == null || tinhThanhPho == null) {
                response.put("error", true);
                response.put("message", "Vui lòng điền đầy đủ thông tin địa chỉ");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<KhachHang> khachHangOpt = khachHangRepo.findById(idKhachHang);
            if (khachHangOpt.isEmpty()) {
                response.put("error", true);
                response.put("message", "Không tìm thấy thông tin khách hàng");
                return ResponseEntity.status(404).body(response);
            }

            KhachHang khachHang = khachHangOpt.get();

            List<DiaChiKhachHang> existingAddresses = diaChiKhachHangRepo.findByKhachHangId(idKhachHang);

            if (existingAddresses.isEmpty()) {
                diaChiMacDinh = true;
            } else if (diaChiMacDinh) {
                for (DiaChiKhachHang addr : existingAddresses) {
                    addr.setDiaChiMacDinh(false);
                    diaChiKhachHangRepo.save(addr);
                }
            }

            DiaChiKhachHang diaChi = new DiaChiKhachHang();
            diaChi.setKhachHang(khachHang);
            diaChi.setSoNha(soNha);
            diaChi.setXaPhuong(xaPhuong);
            diaChi.setQuanHuyen(quanHuyen);
            diaChi.setTinhThanhPho(tinhThanhPho);
            diaChi.setDiaChiMacDinh(diaChiMacDinh);

            diaChi = diaChiKhachHangRepo.save(diaChi);

            response.put("success", true);
            response.put("message", "Thêm địa chỉ thành công");
            response.put("diaChi", diaChi);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/dia-chi/update")
    public ResponseEntity<Map<String, Object>> updateDiaChi(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer idDiaChi = Integer.parseInt(request.get("idDiaChi").toString());
            String soNha = (String) request.get("soNha");
            String xaPhuong = (String) request.get("xaPhuong");
            String quanHuyen = (String) request.get("quanHuyen");
            String tinhThanhPho = (String) request.get("tinhThanhPho");
            Boolean diaChiMacDinh = (Boolean) request.getOrDefault("diaChiMacDinh", false);

            if (soNha == null || xaPhuong == null || quanHuyen == null || tinhThanhPho == null) {
                response.put("error", true);
                response.put("message", "Vui lòng điền đầy đủ thông tin địa chỉ");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<DiaChiKhachHang> diaChiOpt = diaChiKhachHangRepo.findById(idDiaChi);
            if (diaChiOpt.isEmpty()) {
                response.put("error", true);
                response.put("message", "Không tìm thấy địa chỉ");
                return ResponseEntity.status(404).body(response);
            }

            DiaChiKhachHang diaChi = diaChiOpt.get();
            KhachHang khachHang = diaChi.getKhachHang();

            if (diaChiMacDinh && !diaChi.getDiaChiMacDinh()) {
                List<DiaChiKhachHang> existingAddresses = diaChiKhachHangRepo
                        .findByKhachHangId(khachHang.getIdKhachHang());
                for (DiaChiKhachHang addr : existingAddresses) {
                    if (!addr.getIdDiaChiKhachHang().equals(idDiaChi)) {
                        addr.setDiaChiMacDinh(false);
                        diaChiKhachHangRepo.save(addr);
                    }
                }
            }

            diaChi.setSoNha(soNha);
            diaChi.setXaPhuong(xaPhuong);
            diaChi.setQuanHuyen(quanHuyen);
            diaChi.setTinhThanhPho(tinhThanhPho);
            diaChi.setDiaChiMacDinh(diaChiMacDinh);

            diaChi = diaChiKhachHangRepo.save(diaChi);

            response.put("success", true);
            response.put("message", "Cập nhật địa chỉ thành công");
            response.put("diaChi", diaChi);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/dia-chi/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteDiaChi(@PathVariable("id") Integer idDiaChi) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<DiaChiKhachHang> diaChiOpt = diaChiKhachHangRepo.findById(idDiaChi);
            if (diaChiOpt.isEmpty()) {
                response.put("error", true);
                response.put("message", "Không tìm thấy địa chỉ");
                return ResponseEntity.status(404).body(response);
            }

            DiaChiKhachHang diaChi = diaChiOpt.get();
            Integer idKhachHang = diaChi.getKhachHang().getIdKhachHang();

            List<DiaChiKhachHang> allAddresses = diaChiKhachHangRepo.findByKhachHangId(idKhachHang);

            if (allAddresses.size() <= 1) {
                response.put("error", true);
                response.put("message", "Phải có ít nhất một địa chỉ. Không thể xóa.");
                return ResponseEntity.badRequest().body(response);
            }

            if (diaChi.getDiaChiMacDinh()) {
                List<DiaChiKhachHang> otherAddresses = allAddresses.stream()
                        .filter(addr -> !addr.getIdDiaChiKhachHang().equals(idDiaChi))
                        .collect(Collectors.toList());

                if (!otherAddresses.isEmpty()) {
                    DiaChiKhachHang newDefault = otherAddresses.get(0);
                    newDefault.setDiaChiMacDinh(true);
                    diaChiKhachHangRepo.save(newDefault);
                }
            }

            diaChiKhachHangRepo.delete(diaChi);

            response.put("success", true);
            response.put("message", "Xóa địa chỉ thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== HOA DON ====================

    @GetMapping("/hd_kh")
    public Page<HoaDonResponse> getAllHDbyidKH(
            @RequestParam(name = "idKH", required = false) Integer idKH,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "3") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return hoaDonRepo.getAllHDByidKH(idKH, pageable);
    }

    @GetMapping("/hd_kh_tt")
    public Page<HoaDonResponse> getAllHDbyidKHandTT(
            @RequestParam(name = "idKH", required = false) Integer idKH,
            @RequestParam(name = "trangThai", required = false) String trangThai,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "3") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return (trangThai == null || trangThai.trim().isEmpty())
                ? hoaDonRepo.getAllHDByidKH(idKH, pageable)
                : hoaDonRepo.getAllHDByidKHandTT(idKH, trangThai, pageable);
    }

    @PostMapping("/update-order-info")
    public ResponseEntity<Map<String, Object>> updateOrderCustomerInfo(
            @RequestParam String email,
            @RequestBody UpdateOrderCustomerInfoDTO request,
            @RequestParam(value = "phiVanChuyen", required = false, defaultValue = "0") BigDecimal phiVanChuyen) {
        
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<KhachHang> khachHangOpt = khachHangRepo.findByEmail(email);
            if (!khachHangOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin khách hàng!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Integer idKhachHang = khachHangOpt.get().getIdKhachHang();

            Optional<HoaDon> hoaDonOpt = hoaDonRepo.findByMaHoaDonAndIdKhachHang(request.getMaHoaDon(), idKhachHang);
            if (!hoaDonOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn hoặc bạn không có quyền cập nhật!");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            HoaDon hoaDon = hoaDonOpt.get();

            String currentStatus = hoaDonRepo.findLatestStatusByIdHoaDon(hoaDon.getId_hoa_don());
            List<String> allowedStatuses = Arrays.asList("Chờ xác nhận", "Đã xác nhận", "Chờ đóng gói");
            if (!allowedStatuses.contains(currentStatus)) {
                response.put("success", false);
                response.put("message", "Không thể cập nhật thông tin khách hàng cho đơn hàng này!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (request.getHoTen() == null || request.getHoTen().trim().isEmpty() ||
                request.getSdtNguoiNhan() == null || request.getSdtNguoiNhan().trim().isEmpty() ||
                request.getDiaChi() == null || request.getDiaChi().trim().isEmpty()) {
                
                response.put("success", false);
                response.put("message", "Vui lòng điền đầy đủ thông tin!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            hoaDon.setHo_ten(request.getHoTen().trim());
            hoaDon.setSdt_nguoi_nhan(request.getSdtNguoiNhan().trim());
            hoaDon.setDia_chi(request.getDiaChi().trim());
            hoaDon.setNgay_sua(LocalDateTime.now());
            
            BigDecimal pvcCu = hoaDon.getPhi_van_chuyen() != null ? hoaDon.getPhi_van_chuyen() : BigDecimal.ZERO;
            hoaDon.setTong_tien_sau_giam(hoaDon.getTong_tien_sau_giam().subtract(pvcCu).add(phiVanChuyen));
            hoaDon.setPhi_van_chuyen(phiVanChuyen);
            hoaDonRepo.save(hoaDon);

            LocalDateTime ngayChuyen = LocalDateTime.now();
            String noiDungDoi = "Khách hàng tự cập nhật thông tin";
            hoaDonRepo.insertTrangThaiDonHang(request.getMaHoaDon(), "Đã cập nhật", ngayChuyen, null, noiDungDoi);

            response.put("success", true);
            response.put("message", "Cập nhật thông tin khách hàng thành công!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/send-support-request")
    public ResponseEntity<Map<String, Object>> sendSupportRequest(
            @RequestParam String email,
            @RequestBody SupportRequestDTO request) {
        
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<KhachHang> khachHangOpt = khachHangRepo.findByEmail(email);
            if (!khachHangOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin khách hàng!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            KhachHang khachHang = khachHangOpt.get();

            if (!"Đang hoạt động".equals(khachHang.getTrangThai())) {
                response.put("success", false);
                response.put("message", "Tài khoản của bạn đã bị ngừng hoạt động!");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            String subject = "Yêu cầu hỗ trợ mới từ khách hàng - " + request.getChuDe();
            String body = buildSupportEmailBody(request);

            emailService.sendEmail("chinhhtph46334@gmail.com", subject, body);

            response.put("success", true);
            response.put("message", "Yêu cầu hỗ trợ đã được gửi thành công!");
            return ResponseEntity.ok(response);

        } catch (MessagingException e) {
            response.put("success", false);
            response.put("message", "Gửi yêu cầu thất bại: Không thể gửi email - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== UTILITY METHODS ====================

    private String generateMaKhachHang() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder maKhachHang = new StringBuilder("KH");

        for (int i = 0; i < 6; i++) {
            maKhachHang.append(chars.charAt(random.nextInt(chars.length())));
        }

        String newMaKhachHang = maKhachHang.toString();
        while (khachHangRepo.findByMaKhachHang(newMaKhachHang).isPresent()) {
            maKhachHang = new StringBuilder("KH");
            for (int i = 0; i < 6; i++) {
                maKhachHang.append(chars.charAt(random.nextInt(chars.length())));
            }
            newMaKhachHang = maKhachHang.toString();
        }

        return newMaKhachHang;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private void sendWelcomeEmail(KhachHang khachHang, String password) {
        String subject = "Chào mừng bạn đến với G&B SPORTS 🎉";
        String body = buildWelcomeEmailBody(khachHang, password);
        try {
            emailService.sendEmail(khachHang.getEmail(), subject, body);
        } catch (MessagingException e) {
            System.err.println("Không thể gửi email: " + e.getMessage());
        }
    }

    private void sendWelcomeEmailWithPassword(KhachHang khachHang, String password) {
        String subject = "Chào mừng bạn đến với GB Sports!";
        String body = buildWelcomeEmailBody(khachHang, password);
        try {
            emailService.sendEmail(khachHang.getEmail(), subject, body);
        } catch (MessagingException e) {
            System.err.println("Không thể gửi email: " + e.getMessage());
        }
    }

    private void sendResetPasswordEmail(KhachHang khachHang, String newPassword) {
        String subject = "Đặt lại mật khẩu - G&B SPORTS";
        String body = buildResetPasswordEmailBody(khachHang, newPassword);
        try {
            emailService.sendEmail(khachHang.getEmail(), subject, body);
        } catch (MessagingException e) {
            System.err.println("Không thể gửi email: " + e.getMessage());
        }
    }

    private String buildWelcomeEmailBody(KhachHang khachHang, String password) {
        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px; background-color: #f9f9f9; }" +
                ".header { background-color: #4CAF50; color: white; padding: 15px; text-align: center; border-radius: 10px 10px 0 0; }" +
                ".content { padding: 20px; background-color: white; border-radius: 0 0 10px 10px; }" +
                ".highlight { color: #4CAF50; font-weight: bold; }" +
                ".info-box { background-color: #e8f5e9; padding: 15px; border-left: 5px solid #4CAF50; margin: 15px 0; }" +
                ".footer { text-align: center; margin-top: 20px; font-size: 14px; color: #777; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h2>Chào mừng bạn đến với G&B SPORTS 🎉</h2>" +
                "</div>" +
                "<div class='content'>" +
                "<h3>Xin chào <span class='highlight'>" + khachHang.getHoTen() + "</span>,</h3>" +
                "<p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>G&B SPORTS</strong>. Tài khoản của bạn đã được tạo thành công!</p>" +
                "<div class='info-box'>" +
                "<h4>Thông tin đăng nhập của bạn:</h4>" +
                "<ul>" +
                "<li>Tên đăng nhập: <strong>" + khachHang.getTenDangNhap() + "</strong></li>" +
                "<li>Mật khẩu: <strong>" + password + "</strong></li>" +
                "</ul>" +
                "</div>" +
                "<p>Vui lòng <a href='http://localhost:5173/login-register/login'>đăng nhập</a> để bắt đầu sử dụng dịch vụ và khám phá các ưu đãi hấp dẫn.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Trân trọng,<br>Đội ngũ G&B SPORTS</p>" +
                "<p><a href='http://localhost:5173/home'>Ghé thăm website của chúng tôi</a></p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildResetPasswordEmailBody(KhachHang khachHang, String newPassword) {
        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }" +
                ".container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }" +
                ".header { background-color: #d02c39; color: white; padding: 20px; text-align: center; border-top-left-radius: 10px; border-top-right-radius: 10px; }" +
                ".content { padding: 20px; }" +
                ".info-box { background-color: #fff5f5; border-left: 5px solid #d02c39; padding: 15px; margin: 20px 0; border-radius: 5px; }" +
                ".footer { text-align: center; padding: 10px; font-size: 14px; color: #666; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Đặt lại mật khẩu - G&B SPORTS</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h3>Xin chào " + khachHang.getHoTen() + ",</h3>" +
                "<p>Mật khẩu của bạn đã được đặt lại thành công.</p>" +
                "<div class='info-box'>" +
                "<p><strong>Mật khẩu mới của bạn:</strong> " + newPassword + "</p>" +
                "</div>" +
                "<p>Vui lòng đăng nhập và đổi mật khẩu ngay.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Trân trọng,<br>Đội ngũ G&B SPORTS</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildSupportEmailBody(SupportRequestDTO request) {
        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }" +
                ".container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }" +
                ".header { background-color: #e53935; color: #ffffff; padding: 20px; text-align: center; border-top-left-radius: 10px; border-top-right-radius: 10px; }" +
                ".content { padding: 20px; }" +
                ".info-box { background-color: #fff5f5; border-left: 5px solid #e53935; padding: 15px; margin: 20px 0; border-radius: 5px; }" +
                ".footer { text-align: center; padding: 10px; font-size: 14px; color: #666; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Yêu cầu hỗ trợ từ khách hàng</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h3>Thông tin yêu cầu:</h3>" +
                "<div class='info-box'>" +
                "<p><strong>Họ và tên:</strong> " + request.getHoTen() + "</p>" +
                "<p><strong>Số điện thoại:</strong> " + request.getSoDienThoai() + "</p>" +
                "<p><strong>Email:</strong> " + request.getEmail() + "</p>" +
                "<p><strong>Chủ đề:</strong> " + request.getChuDe() + "</p>" +
                "<p><strong>Nội dung:</strong> " + request.getNoiDung() + "</p>" +
                "</div>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Trân trọng,<br>Đội ngũ G&B SPORTS</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
