package com.example.duanbe.controller;

import com.example.duanbe.config.ZaloPayConfig;
import com.example.duanbe.entity.HoaDon;
import com.example.duanbe.repository.HoaDonRepo;
import com.example.duanbe.service.ZaloPayService;
import com.example.duanbe.utils.HMACUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/zalopay")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE })
public class ZaloPayController {
    
    @Autowired
    private ZaloPayService zaloPayService;
    
    @Autowired
    private HoaDonRepo hoaDonRepo;

    private final Gson gson = new Gson();
    
    /**
     * Tạo đơn hàng ZaloPay và trả về QR code
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestParam("idHoaDon") Integer idHoaDon) {
        try {
            System.out.println("\n=== TẠO ORDER ZALOPAY ===");
            System.out.println("ID Hóa đơn: " + idHoaDon);
            
            HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            System.out.println("Mã hóa đơn: " + hoaDon.getMa_hoa_don());
            System.out.println("Trạng thái hiện tại: " + hoaDon.getTrang_thai());
            
            // Kiểm tra hóa đơn đã thanh toán chưa
            if ("Đã thanh toán".equalsIgnoreCase(hoaDon.getTrang_thai())) {
                System.out.println("!!! HÓA ĐƠN ĐÃ THANH TOÁN - KHÔNG CHO TẠO ORDER MỚI");
                return ResponseEntity.badRequest().body(Map.of(
                    "return_code", -1,
                    "return_message", "Hóa đơn đã được thanh toán rồi!"
                ));
            }
            
            // Kiểm tra số tiền hợp lệ
            java.math.BigDecimal tongTien = hoaDon.getTong_tien_sau_giam();
            System.out.println("Số tiền: " + tongTien);
            
            if (tongTien == null || tongTien.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                System.out.println("!!! SỐ TIỀN KHÔNG HỢP LỆ");
                return ResponseEntity.badRequest().body(Map.of(
                    "return_code", -1,
                    "return_message", "Số tiền thanh toán không hợp lệ"
                ));
            }
            
            String moTa = "Thanh toán hóa đơn " + hoaDon.getMa_hoa_don();
            
            System.out.println(">>> Gọi ZaloPay Create Order API...");
            Map<String, Object> result = zaloPayService.createOrder(
                hoaDon.getMa_hoa_don(),
                tongTien,
                moTa
            );
            
            System.out.println("ZaloPay Response Return Code: " + result.get("return_code"));
            
            // Lưu app_trans_id vào DB để tracking
            if (result.get("return_code") != null && (Double) result.get("return_code") == 1.0) {
                try {
                    String ghiChuCu = hoaDon.getGhi_chu() != null ? hoaDon.getGhi_chu() : "";
                    // Xóa app_trans_id cũ nếu có
                    ghiChuCu = ghiChuCu.replaceAll("ZaloPay:[^ ]+", "").trim();
                    hoaDon.setGhi_chu(ghiChuCu + " ZaloPay:" + result.get("app_trans_id"));
                    hoaDonRepo.save(hoaDon);
                    System.out.println("✅ Đã lưu app_trans_id: " + result.get("app_trans_id"));
                } catch (Exception saveEx) {
                    System.err.println("!!! Lỗi khi lưu app_trans_id: " + saveEx.getMessage());
                    // Vẫn trả về result thành công từ ZaloPay
                }
            } else {
                System.out.println("!!! ZALOPAY TRẢ VỀ LỖI: " + result.get("return_message"));
            }
            
            System.out.println("=== END TẠO ORDER ===\n");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("!!! LỖI TẠO ORDER: " + e.getMessage());
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
            System.out.println("=== CHECK STATUS DEBUG ===");
            System.out.println("ID Hóa đơn: " + idHoaDon);
            
            HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
            System.out.println("Trạng thái hiện tại trong DB: " + hoaDon.getTrang_thai());
            System.out.println("Ghi chú: " + hoaDon.getGhi_chu());
            
            // Nếu hóa đơn đã được thanh toán, trả về thành công luôn
            if ("Đã thanh toán".equalsIgnoreCase(hoaDon.getTrang_thai())) {
                 System.out.println(">>> HÓA ĐƠN ĐÃ THANH TOÁN TRƯỚC ĐÓ - KHÔNG GỌI ZALOPAY");
                 Map<String, Object> result = new HashMap<>();
                 result.put("return_code", 1);
                 result.put("return_message", "Thanh toán thành công");
                 return ResponseEntity.ok(result);
            }

            String ghiChu = hoaDon.getGhi_chu();
            if (ghiChu == null || !ghiChu.contains("ZaloPay:")) {
                System.out.println(">>> CHƯA CÓ APP_TRANS_ID TRONG GHI CHÚ");
                return ResponseEntity.badRequest().body(Map.of(
                    "return_code", -1,
                    "return_message", "Chưa tạo đơn hàng ZaloPay"
                ));
            }
            
            // Trích xuất app_trans_id từ ghi chú
            String appTransId = extractAppTransId(ghiChu);
            System.out.println("App Trans ID: " + appTransId);
            System.out.println(">>> GỌI ZALOPAY QUERY API...");
            
            Map<String, Object> result = zaloPayService.queryOrder(appTransId);
            
            System.out.println("ZaloPay Response Return Code: " + result.get("return_code"));
            System.out.println("ZaloPay Response Message: " + result.get("return_message"));
            
            // Nếu thanh toán thành công, cập nhật hóa đơn
            if (result.get("return_code") != null && (Double) result.get("return_code") == 1.0) {
                System.out.println(">>> ZALOPAY XÁC NHẬN THÀNH CÔNG - CẬP NHẬT DB");
                hoaDon.setTrang_thai("Đã thanh toán");
                hoaDon.setHinh_thuc_thanh_toan("Chuyển khoản (ZaloPay)");
                hoaDonRepo.save(hoaDon);
            } else {
                System.out.println(">>> CHƯA THANH TOÁN - Return Code: " + result.get("return_code"));
            }
            
            System.out.println("=== END CHECK STATUS ===\n");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("!!! LỖI CHECK STATUS: " + e.getMessage());
            e.printStackTrace();
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
    public ResponseEntity<?> callback(@RequestBody String jsonStr) {
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("ZaloPay Callback Data: " + jsonStr);
            JsonObject cbdata = gson.fromJson(jsonStr, JsonObject.class);
            String dataStr = cbdata.get("data").getAsString();
            String mac = cbdata.get("mac").getAsString();

            String calculatedMac = HMACUtil.HMacHexStringEncode("HmacSHA256", ZaloPayConfig.KEY2, dataStr);

            if (!calculatedMac.equals(mac)) {
                result.put("return_code", -1);
                result.put("return_message", "MAC không hợp lệ");
            } else {
                JsonObject data = gson.fromJson(dataStr, JsonObject.class);
                String appTransId = data.get("app_trans_id").getAsString();

                Optional<HoaDon> hoaDonOpt = hoaDonRepo.findByGhiChuContaining(appTransId);

                if (hoaDonOpt.isPresent()) {
                    HoaDon hoaDon = hoaDonOpt.get();
                    hoaDon.setTrang_thai("Đã thanh toán");
                    hoaDon.setHinh_thuc_thanh_toan("Chuyển khoản (ZaloPay)");
                    hoaDonRepo.save(hoaDon);
                    System.out.println("Cập nhật trạng thái hóa đơn thành công cho app_trans_id: " + appTransId);
                    result.put("return_code", 1);
                    result.put("return_message", "success");
                } else {
                     System.err.println("Không tìm thấy hóa đơn cho app_trans_id: " + appTransId);
                    result.put("return_code", 0);
                    result.put("return_message", "Không tìm thấy hóa đơn");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("return_code", -1);
            result.put("return_message", e.getMessage());
        }

        return ResponseEntity.ok(result);
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
