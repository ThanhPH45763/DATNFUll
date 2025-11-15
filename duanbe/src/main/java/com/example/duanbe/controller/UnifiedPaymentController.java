package com.example.duanbe.controller;

import com.example.duanbe.entity.HoaDon;
import com.example.duanbe.repository.HoaDonRepo;
import com.example.duanbe.service.ZaloPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE })
public class UnifiedPaymentController {

    @Autowired
    private ZaloPayService zaloPayService;

    @Autowired
    private HoaDonRepo hoaDonRepo;

    private final PayOS payOS = new PayOS("30965015-9adc-4cb9-8afc-073995fe805c",
            "82ad6f69-754c-4f45-85c8-da89f8423973",
            "988c02f4c4ab53b04f91c8b9fdbebe860ab12f78b4ec905cc797f1bf44752801");

    /**
     * Tạo QR code thanh toán (PayOS hoặc ZaloPay)
     * @param idHoaDon ID hóa đơn
     * @param paymentMethod Phương thức thanh toán: "payos" hoặc "zalopay"
     */
    @PostMapping("/create-qr")
    public ResponseEntity<?> createPaymentQR(
            @RequestParam Integer idHoaDon,
            @RequestParam String paymentMethod) {
        try {
            HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            if ("zalopay".equalsIgnoreCase(paymentMethod)) {
                return createZaloPayQR(hoaDon);
            } else if ("payos".equalsIgnoreCase(paymentMethod)) {
                return createPayOSQR(hoaDon);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", true,
                        "message", "Phương thức thanh toán không hợp lệ. Vui lòng chọn 'payos' hoặc 'zalopay'"
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", true,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Tạo QR code ZaloPay
     */
    private ResponseEntity<?> createZaloPayQR(HoaDon hoaDon) {
        try {
            String moTa = "Thanh toán hóa đơn " + hoaDon.getMa_hoa_don();

            Map<String, Object> result = zaloPayService.createOrder(
                    hoaDon.getMa_hoa_don(),
                    hoaDon.getTong_tien_sau_giam(),
                    moTa
            );

            // Kiểm tra kết quả từ ZaloPay
            if (result.get("return_code") != null) {
                Double returnCode = (Double) result.get("return_code");
                if (returnCode.intValue() == 1) {
                    // Thành công - lưu app_trans_id vào DB
                    String appTransId = (String) result.get("app_trans_id");
                    String ghiChuCu = hoaDon.getGhi_chu() != null ? hoaDon.getGhi_chu() : "";
                    hoaDon.setGhi_chu(ghiChuCu + " ZaloPay:" + appTransId);
                    hoaDonRepo.save(hoaDon);

                    // Trả về response với QR code URL
                    Map<String, Object> response = new HashMap<>();
                    response.put("error", false);
                    response.put("paymentMethod", "zalopay");
                    response.put("qrUrl", result.get("order_url")); // URL QR code từ ZaloPay
                    response.put("orderUrl", result.get("order_url")); // URL để thanh toán
                    response.put("appTransId", appTransId);
                    response.put("amount", hoaDon.getTong_tien_sau_giam());
                    response.put("message", "Tạo mã QR ZaloPay thành công");

                    return ResponseEntity.ok(response);
                } else {
                    // ZaloPay trả về lỗi
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", true,
                            "message", "ZaloPay: " + result.get("return_message")
                    ));
                }
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", true,
                        "message", "Không nhận được phản hồi từ ZaloPay"
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", true,
                    "message", "Lỗi khi tạo QR ZaloPay: " + e.getMessage()
            ));
        }
    }

    /**
     * Tạo QR code PayOS
     */
    private ResponseEntity<?> createPayOSQR(HoaDon hoaDon) {
        try {
            long orderCode = System.currentTimeMillis();
            String description = "Thanh toán hóa đơn " + hoaDon.getMa_hoa_don();

            ItemData item = ItemData.builder()
                    .name("Hóa đơn " + hoaDon.getMa_hoa_don())
                    .quantity(1)
                    .price(hoaDon.getTong_tien_sau_giam().intValue())
                    .build();

            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(hoaDon.getTong_tien_sau_giam().intValue())
                    .description(description)
                    .returnUrl("http://localhost:5173/payment-callback")
                    .cancelUrl("http://localhost:5173/thanhtoan-banhang")
                    .item(item)
                    .build();

            var createPayment = payOS.createPaymentLink(paymentData);

            // Lưu orderCode vào DB
            String ghiChuCu = hoaDon.getGhi_chu() != null ? hoaDon.getGhi_chu() : "";
            hoaDon.setGhi_chu(ghiChuCu + " PayOS:" + orderCode);
            hoaDonRepo.save(hoaDon);

            // Trả về response
            Map<String, Object> response = new HashMap<>();
            response.put("error", false);
            response.put("paymentMethod", "payos");
            response.put("checkoutUrl", createPayment.getCheckoutUrl());
            response.put("qrUrl", createPayment.getQrCode()); // QR code từ PayOS
            response.put("orderCode", orderCode);
            response.put("amount", hoaDon.getTong_tien_sau_giam());
            response.put("message", "Tạo mã QR PayOS thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", true,
                    "message", "Lỗi khi tạo QR PayOS: " + e.getMessage()
            ));
        }
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    @GetMapping("/check-status")
    public ResponseEntity<?> checkPaymentStatus(
            @RequestParam Integer idHoaDon,
            @RequestParam String paymentMethod) {
        try {
            HoaDon hoaDon = hoaDonRepo.findById(idHoaDon)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

            if ("zalopay".equalsIgnoreCase(paymentMethod)) {
                return checkZaloPayStatus(hoaDon);
            } else if ("payos".equalsIgnoreCase(paymentMethod)) {
                return checkPayOSStatus(hoaDon);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", true,
                        "message", "Phương thức thanh toán không hợp lệ"
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", true,
                    "message", e.getMessage()
            ));
        }
    }

    private ResponseEntity<?> checkZaloPayStatus(HoaDon hoaDon) {
        try {
            String ghiChu = hoaDon.getGhi_chu();
            if (ghiChu == null || !ghiChu.contains("ZaloPay:")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", true,
                        "message", "Chưa tạo đơn hàng ZaloPay"
                ));
            }

            String appTransId = extractTransactionId(ghiChu, "ZaloPay:");
            Map<String, Object> result = zaloPayService.queryOrder(appTransId);

            if (result.get("return_code") != null) {
                Double returnCode = (Double) result.get("return_code");
                if (returnCode.intValue() == 1) {
                    // Thanh toán thành công
                    hoaDon.setTrang_thai("Đã thanh toán");
                    hoaDon.setHinh_thuc_thanh_toan("Chuyển khoản (ZaloPay)");
                    hoaDonRepo.save(hoaDon);

                    return ResponseEntity.ok(Map.of(
                            "error", false,
                            "status", "success",
                            "message", "Thanh toán thành công"
                    ));
                } else {
                    return ResponseEntity.ok(Map.of(
                            "error", false,
                            "status", "pending",
                            "message", "Chưa thanh toán"
                    ));
                }
            }

            return ResponseEntity.badRequest().body(Map.of(
                    "error", true,
                    "message", "Không thể kiểm tra trạng thái"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", true,
                    "message", e.getMessage()
            ));
        }
    }

    private ResponseEntity<?> checkPayOSStatus(HoaDon hoaDon) {
        try {
            String ghiChu = hoaDon.getGhi_chu();
            if (ghiChu == null || !ghiChu.contains("PayOS:")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", true,
                        "message", "Chưa tạo đơn hàng PayOS"
                ));
            }

            String orderCodeStr = extractTransactionId(ghiChu, "PayOS:");
            long orderCode = Long.parseLong(orderCodeStr);

            var paymentInfo = payOS.getPaymentLinkInformation(orderCode);
            String status = paymentInfo.getStatus();

            if ("PAID".equals(status)) {
                hoaDon.setTrang_thai("Đã thanh toán");
                hoaDon.setHinh_thuc_thanh_toan("Chuyển khoản (PayOS)");
                hoaDonRepo.save(hoaDon);

                return ResponseEntity.ok(Map.of(
                        "error", false,
                        "status", "success",
                        "message", "Thanh toán thành công"
                ));
            } else if ("CANCELLED".equals(status)) {
                return ResponseEntity.ok(Map.of(
                        "error", false,
                        "status", "cancelled",
                        "message", "Thanh toán đã bị hủy"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "error", false,
                        "status", "pending",
                        "message", "Chưa thanh toán"
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", true,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Trích xuất transaction ID từ ghi chú
     */
    private String extractTransactionId(String ghiChu, String prefix) {
        int startIndex = ghiChu.indexOf(prefix) + prefix.length();
        int endIndex = ghiChu.indexOf(" ", startIndex);
        if (endIndex == -1) {
            endIndex = ghiChu.length();
        }
        return ghiChu.substring(startIndex, endIndex).trim();
    }
}
