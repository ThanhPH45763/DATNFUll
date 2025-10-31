package com.example.duanbe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.List;

import com.example.duanbe.entity.DanhMuc;
import com.example.duanbe.service.DanhMucService;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE })
@RequestMapping("/admin/quan_ly_san_pham")

public class DanhMucController {
    @Autowired
    DanhMucService danhMucService;

    @GetMapping("/DanhMuc")
    public List<DanhMuc> getAllDanhMuc() {
        return danhMucService.listFindAllDanhMuc();
    }

    @PostMapping("/addDanhMuc")
    public ResponseEntity<?> addDanhMuc(@RequestParam("tenDanhMuc") String tenDanhMuc) {
        return ResponseEntity.ok(danhMucService.getDanhMucOrCreateDanhMuc(tenDanhMuc));
    }

    @PutMapping("/changeTrangThaiDanhMuc")
    public ResponseEntity<?> changeTrangThaiDanhMuc(@RequestParam("idDanhMuc") Integer idDanhMuc) {
        return danhMucService.changeTrangThaiDanhMuc(idDanhMuc);
    }

    @PutMapping("/updateDanhMuc")
    public ResponseEntity<?> updateDanhMuc(@RequestBody DanhMuc danhMuc) {
        return danhMucService.updateDanhMuc(danhMuc);
    }
}
