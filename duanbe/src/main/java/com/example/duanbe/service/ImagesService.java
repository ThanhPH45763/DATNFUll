package com.example.duanbe.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImagesService {
    private final Cloudinary cloudinary;

    public ImagesService() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dryt7bnjl",
                "api_key", "526585127685277",
                "api_secret", "3JgRZQohaSg7R3KKqQXLK4pTH7U"
        ));
    }

    public String uploadFile(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = uploadResult.get("secure_url").toString();
            String publicId = uploadResult.get("public_id").toString();
            System.out.println("imageUrl"+imageUrl);
            System.out.println(publicId);
            return uploadResult.get("url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }
    public void deleteFile(String publicId) throws Exception {
        try {
            // Xóa ảnh từ Cloudinary bằng public_id
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            // Kiểm tra kết quả trả về từ Cloudinary
            if (!"ok".equals(result.get("result"))) {
                throw new Exception("Không tìm thấy ảnh với public_id: " + publicId);
            }
        } catch (IOException e) {
            throw new Exception("Lỗi khi xóa ảnh từ Cloudinary", e);
        }
    }
}
