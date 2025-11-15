package com.example.duanbe.controller;

import com.example.duanbe.entity.HoaDon;
import com.example.duanbe.repository.HoaDonRepo;
import com.example.duanbe.service.ZaloPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/zalopay")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE })
public class ZaloPayController {
    
    @Autowired
    private ZaloPayService zaloPayService;
    
    @Autowired
    private HoaDonRepo hoaDonRepo;
    
    /**
     * Tạo đơn hàng ZaloPay và trả về QR code
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestParam("idHoaDon") Integer idHoaDon) {
        try {
            HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            // Kiểm tra số tiền hợp lệ
            java.math.BigDecimal tongTien = hoaDon.getTong_tien_sau_giam();
            if (tongTien == null || tongTien.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "return_code", -1,
                    "return_message", "Số tiền thanh toán không hợp lệ"
                ));
            }
            
            String moTa = "Thanh toán hóa đơn " + hoaDon.getMa_hoa_don();
            
            Map<String, Object> result = zaloPayService.createOrder(
                hoaDon.getMa_hoa_don(),
                tongTien,
                moTa
            );
            
            // Lưu app_trans_id vào DB để tracking
            if (result.get("return_code") != null && result.get("return_code").equals(1.0)) {
                try {
                    String ghiChuCu = hoaDon.getGhi_chu() != null ? hoaDon.getGhi_chu() : "";
                    hoaDon.setGhi_chu(ghiChuCu + " ZaloPay:" + result.get("app_trans_id"));
                    hoaDonRepo.save(hoaDon);
                } catch (Exception saveEx) {
                    System.err.println("Lỗi khi lưu app_trans_id: " + saveEx.getMessage());
                    // Vẫn trả về result thành công từ ZaloPay
                }
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "return_code", -1,
                "return_message", e.getMessage()
            ));
        }
    }
    
    /**
     * Kiểm tra trạng thái thanh toán
     */
    @GetMapping("/check-status")
    public ResponseEntity<?> checkStatus(@RequestParam Integer idHoaDon) {
        try {
            HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            String ghiChu = hoaDon.getGhi_chu();
            if (ghiChu == null || !ghiChu.contains("ZaloPay:")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "return_code", -1,
                    "return_message", "Chưa tạo đơn hàng ZaloPay"
                ));
            }
            
            // Trích xuất app_trans_id từ ghi chú
            String appTransId = extractAppTransId(ghiChu);
            Map<String, Object> result = zaloPayService.queryOrder(appTransId);
            
            // Nếu thanh toán thành công, cập nhật hóa đơn
            if (result.get("return_code") != null && result.get("return_code").equals(1.0)) {
                hoaDon.setTrang_thai("Đã thanh toán");
                hoaDon.setHinh_thuc_thanh_toan("Chuyển khoản (ZaloPay)");
                hoaDonRepo.save(hoaDon);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "return_code", -1,
                "return_message", e.getMessage()
            ));
        }
    }
    
    /**
     * Callback từ ZaloPay (khi thanh toán thành công)
     */
    @PostMapping("/callback")
    public ResponseEntity<?> callback(@RequestBody Map<String, Object> jsonData) {
        try {
            System.out.println("ZaloPay Callback: " + jsonData);
            
            // Xử lý callback từ ZaloPay
            // TODO: Xác thực MAC, cập nhật trạng thái đơn hàng
            
            Map<String, Object> result = new HashMap<>();
            result.put("return_code", 1);
            result.put("return_message", "success");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("return_code", -1);
            result.put("return_message", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * Trích xuất app_trans_id từ ghi chú
     */
    private String extractAppTransId(String ghiChu) {
        int startIndex = ghiChu.indexOf("ZaloPay:") + 8;
        int endIndex = ghiChu.indexOf(" ", startIndex);
        if (endIndex == -1) {
            endIndex = ghiChu.length();
        }
        return ghiChu.substring(startIndex, endIndex).trim();
    }
}
