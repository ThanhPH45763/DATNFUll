package com.example.duanbe.payos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.type.PaymentLinkData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RequestMapping("/payment")
public class PaymentController {
    private final PayOS payOS = new PayOS("30965015-9adc-4cb9-8afc-073995fe805c",
            "82ad6f69-754c-4f45-85c8-da89f8423973",
            "988c02f4c4ab53b04f91c8b9fdbebe860ab12f78b4ec905cc797f1bf44752801");

    @PostMapping(path = "/payos_transfer_handler")
    public ObjectNode payosTransferHandler(@RequestBody ObjectNode body)
            throws JsonProcessingException, IllegalArgumentException {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode response = objectMapper.createObjectNode();
        Webhook webhookBody = objectMapper.treeToValue(body, Webhook.class);

        try {
            // Init Response
            response.put("error", 0);
            response.put("message", "Webhook delivered");
            response.set("data", null);

            WebhookData data = payOS.verifyPaymentWebhookData(webhookBody);
            System.out.println("webhook"+data);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", -1);
            response.put("message", e.getMessage());
            response.set("data", null);
            return response;
        }
    }
    @GetMapping(path = "/payment-status")
    public ObjectNode getPaymentStatus(@RequestParam("orderId") long orderId) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode response = objectMapper.createObjectNode();

        try {
            // Hiện tại chỉ gọi API PayOS để kiểm tra (sau này có thể dùng database)
            PaymentLinkData paymentInfo = payOS.getPaymentLinkInformation(orderId);
            String status = paymentInfo.getStatus();

            response.put("error", 0);
            response.put("message", "success");
            response.put("status", status);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", -1);
            response.put("message", e.getMessage());
            response.put("status", "UNKNOWN");
            return response;
        }
    }
}
