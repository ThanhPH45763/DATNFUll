package com.example.duanbe.service;

import com.example.duanbe.entity.HinhAnhSanPham;
import com.example.duanbe.repository.HinhAnhSanPhamRepo;
import com.example.duanbe.response.HinhAnhView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HinhAnhService {
    @Autowired
    HinhAnhSanPhamRepo hinhAnhSanPhamRepo;
    
    public List<HinhAnhView> listAnhTheoCTSP(@RequestParam("idCTSP") Integer id,
                                             @RequestParam("anhChinh") Boolean anhChinh) {
        ArrayList<HinhAnhView> listTam = new ArrayList<>();
        if (anhChinh == null){
            return hinhAnhSanPhamRepo.listHinhAnhTheoSanPham(id);
        }else {
            for (HinhAnhView hinhanh: hinhAnhSanPhamRepo.listHinhAnhTheoSanPham(id)) {
                if (hinhanh.getAnh_chinh()){
                    listTam.add(hinhanh);
                }
            }
            return listTam;
        }
    }
    
    public ResponseEntity<?> deleteHinhAnh(@PathVariable Integer id) {
        try {
            if (!hinhAnhSanPhamRepo.existsById(id)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không tìm thấy hình ảnh với id: " + id);
                return ResponseEntity.badRequest().body(error);
            }
            
            hinhAnhSanPhamRepo.deleteById(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Xóa hình ảnh thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi xóa hình ảnh: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
