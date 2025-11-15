package com.example.duanbe.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public class HMACUtil {
    /**
     * Tạo chữ ký HMAC SHA256
     * @param algorithm Thuật toán (HmacSHA256)
     * @param key Key để ký
     * @param data Dữ liệu cần ký
     * @return Chữ ký dạng hex string
     */
    public static String HMacHexStringEncode(String algorithm, String key, String data) {
        try {
            Mac hmac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);
            hmac.init(secretKeySpec);
            byte[] hmacBytes = hmac.doFinal(data.getBytes());
            return Hex.encodeHexString(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC: " + e.getMessage(), e);
        }
    }
}
