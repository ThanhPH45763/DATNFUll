package com.example.duanbe.service;

import com.example.duanbe.config.ZaloPayConfig;
import com.example.duanbe.utils.HMACUtil;
import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ZaloPayService {

    @Value("${app.public-url}")
    private String publicUrl;

    private final Gson gson = new Gson();

    /**
     * Tạo đơn hàng ZaloPay
     * 
     * @param maHoaDon Mã hóa đơn
     * @param tongTien Tổng tiền (VNĐ)
     * @param moTa     Mô tả đơn hàng
     * @return Map chứa order_url để hiển thị QR code
     */
    public Map<String, Object> createOrder(String maHoaDon, BigDecimal tongTien, String moTa) {
        try {
            // 1. Tạo app_trans_id (unique cho mỗi giao dịch)
            String appTransId = getCurrentTimeString("yyMMdd") + "_" + System.currentTimeMillis();

            // 2. Tạo embed_data
            Map<String, String> embedData = new HashMap<>();
            embedData.put("redirecturl", ZaloPayConfig.REDIRECT_URL);
            embedData.put("merchantinfo", "GB Sport - " + maHoaDon);

            // 3. Tạo item (danh sách sản phẩm)
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("itemid", maHoaDon);
            item.put("itemname", "Hóa đơn " + maHoaDon);
            item.put("itemprice", tongTien.longValue());
            item.put("itemquantity", 1);
            items.add(item);

            // 4. Tạo order data
            Map<String, Object> order = new HashMap<>();
            order.put("app_id", Integer.parseInt(ZaloPayConfig.APP_ID));
            order.put("app_trans_id", appTransId);
            order.put("app_user", "user_" + System.currentTimeMillis());
            order.put("app_time", System.currentTimeMillis());
            order.put("amount", tongTien.longValue());
            System.out.println("💰 ZALOPAY AMOUNT: " + tongTien.longValue() + " VNĐ");
            order.put("description", moTa);
            order.put("bank_code", "");
            order.put("item", gson.toJson(items));
            order.put("embed_data", gson.toJson(embedData));

            // Xây dựng URL callback động
            String callbackUrl = publicUrl + "/api/zalopay/callback";
            order.put("callback_url", callbackUrl);

            // 5. Tạo MAC (chữ ký)
            String data = order.get("app_id") + "|"
                    + order.get("app_trans_id") + "|"
                    + order.get("app_user") + "|"
                    + order.get("amount") + "|"
                    + order.get("app_time") + "|"
                    + order.get("embed_data") + "|"
                    + order.get("item");

            String mac = HMACUtil.HMacHexStringEncode("HmacSHA256", ZaloPayConfig.KEY1, data);
            order.put("mac", mac);

            // Log để debug
            System.out.println("ZaloPay Order Data: " + gson.toJson(order));
            System.out.println("ZaloPay MAC String: " + data);
            System.out.println("ZaloPay MAC: " + mac);

            // 6. Gọi API ZaloPay với application/x-www-form-urlencoded
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(ZaloPayConfig.ENDPOINT_CREATE);

            // ✅ ZaloPay yêu cầu application/x-www-form-urlencoded, KHÔNG phải JSON!
            List<org.apache.http.NameValuePair> params = new ArrayList<>();
            params.add(new org.apache.http.message.BasicNameValuePair("app_id", order.get("app_id").toString()));
            params.add(new org.apache.http.message.BasicNameValuePair("app_trans_id",
                    order.get("app_trans_id").toString()));
            params.add(new org.apache.http.message.BasicNameValuePair("app_user", order.get("app_user").toString()));
            params.add(new org.apache.http.message.BasicNameValuePair("app_time", order.get("app_time").toString()));
            params.add(new org.apache.http.message.BasicNameValuePair("amount", order.get("amount").toString()));
            params.add(
                    new org.apache.http.message.BasicNameValuePair("description", order.get("description").toString()));
            params.add(new org.apache.http.message.BasicNameValuePair("bank_code", order.get("bank_code").toString()));
            params.add(new org.apache.http.message.BasicNameValuePair("item", order.get("item").toString()));
            params.add(
                    new org.apache.http.message.BasicNameValuePair("embed_data", order.get("embed_data").toString()));
            params.add(new org.apache.http.message.BasicNameValuePair("callback_url",
                    order.get("callback_url").toString()));
            params.add(new org.apache.http.message.BasicNameValuePair("mac", mac));

            post.setEntity(new org.apache.http.client.entity.UrlEncodedFormEntity(params, "UTF-8"));

            CloseableHttpResponse response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());

            System.out.println("ZaloPay Response: " + responseString);

            Map<String, Object> result = gson.fromJson(responseString, Map.class);
            result.put("app_trans_id", appTransId); // Trả về để lưu DB

            client.close();

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("return_code", -1.0);
            error.put("return_message", e.getMessage());
            return error;
        }
    }

    /**
     * Kiểm tra trạng thái giao dịch
     */
    public Map<String, Object> queryOrder(String appTransId) {
        try {
            System.out.println("\n=== ZALOPAY QUERY ORDER ===");
            System.out.println("App Trans ID: " + appTransId);

            Map<String, String> params = new HashMap<>();
            params.put("app_id", ZaloPayConfig.APP_ID);
            params.put("app_trans_id", appTransId);

            String data = params.get("app_id") + "|" + params.get("app_trans_id") + "|" + ZaloPayConfig.KEY1;
            String mac = HMACUtil.HMacHexStringEncode("HmacSHA256", ZaloPayConfig.KEY1, data);
            params.put("mac", mac);
            System.out.println("Query URL: " + ZaloPayConfig.ENDPOINT_QUERY);
            System.out.println("Query Params: " + gson.toJson(params));

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(ZaloPayConfig.ENDPOINT_QUERY);

            // ✅ Sử dụng form-urlencoded
            List<org.apache.http.NameValuePair> formParams = new ArrayList<>();
            formParams.add(new org.apache.http.message.BasicNameValuePair("app_id", params.get("app_id")));
            formParams.add(new org.apache.http.message.BasicNameValuePair("app_trans_id", params.get("app_trans_id")));
            formParams.add(new org.apache.http.message.BasicNameValuePair("mac", params.get("mac")));

            post.setEntity(new org.apache.http.client.entity.UrlEncodedFormEntity(formParams, "UTF-8"));

            CloseableHttpResponse response = client.execute(post);
            String responseString = EntityUtils.toString(response.getEntity());

            System.out.println("ZaloPay Query Response: " + responseString);

            Map<String, Object> result = gson.fromJson(responseString, Map.class);

            System.out.println("Parsed Return Code: " + result.get("return_code"));
            System.out.println("Parsed Return Message: " + result.get("return_message"));
            System.out.println("=== END ZALOPAY QUERY ===\n");

            client.close();

            return result;

        } catch (Exception e) {
            System.err.println("!!! LỖI ZALOPAY QUERY: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("return_code", -1);
            error.put("return_message", e.getMessage());
            return error;
        }
    }

    private String getCurrentTimeString(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }
}
