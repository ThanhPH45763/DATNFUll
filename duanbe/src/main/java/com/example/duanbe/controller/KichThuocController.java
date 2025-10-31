package com.example.duanbe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.duanbe.entity.KichThuoc;
import com.example.duanbe.service.KichThuocService;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE })
@RequestMapping("/admin/quan_ly_san_pham")

public class KichThuocController {
     @Autowired
    KichThuocService kichThuocService;

    
    @GetMapping("/Size")
    public List<KichThuoc> getAllKichThuoc() {
        return kichThuocService.listFindAllKichThuoc();
    }

    @PostMapping("/addSize")
    public ResponseEntity<?> addSize(@RequestParam("giaTri") String giaTri,
                                     @RequestParam("donVi") String donVi) {
        return ResponseEntity.ok(kichThuocService.getKichThuocOrCreateKichThuoc(giaTri, donVi));
    }
    @PutMapping("/changeTrangThaiKichThuoc")
    public ResponseEntity<?> changeTrangThaiKichThuoc(@RequestParam("idKichThuoc") Integer idKichThuoc){
        return kichThuocService.changeTrangThaiKichThuoc(idKichThuoc);
    }
    @PutMapping("/updateKichThuoc")
    public ResponseEntity<?> updateKichThuoc(@RequestBody KichThuoc kichThuoc){
        return kichThuocService.updateKichThuoc(kichThuoc);
    }
}