package com.example.duanbe.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.duanbe.response.HinhAnhView;
import com.example.duanbe.service.HinhAnhService;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE })
public class HinhAnhController {
    @Autowired
    HinhAnhService hinhAnhService;
    
    // Endpoint cũ để lấy hình ảnh (giữ nguyên cho tương thích)
    @GetMapping("/admin/quan_ly_san_pham/HinhAnhSanPham")
    public List<HinhAnhView> listHATheoCTSP(@RequestParam(name = "idCTSP") Integer id,
                                            @RequestParam(name = "anhChinh", required = false) Boolean anhChinh){
        return hinhAnhService.listAnhTheoCTSP(id,anhChinh);
    }
    
    // Endpoint mới để xóa hình ảnh
    @DeleteMapping("/admin/quan_ly_hinh_anh/deleteHinhAnh/{id}")
    public ResponseEntity<?> deleteHinhAnh(@PathVariable Integer id) {
        return hinhAnhService.deleteHinhAnh(id);
    }
}
