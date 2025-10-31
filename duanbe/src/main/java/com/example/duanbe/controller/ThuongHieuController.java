package com.example.duanbe.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.example.duanbe.entity.ThuongHieu;
import com.example.duanbe.service.ThuongHieuService;

import org.springframework.web.bind.annotation.*;


import java.util.List;
@RestController
@CrossOrigin(origins = "http://localhost:5173",allowedHeaders = "*",methods = {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
@RequestMapping("/admin/quan_ly_san_pham")
public class ThuongHieuController {
 @Autowired
    ThuongHieuService thuongHieuService;
    
    @GetMapping("/ThuongHieu")
    public List<ThuongHieu> getAllThuongHieu(){
        return thuongHieuService.listFindAllThuongHieu();
    }

    @PostMapping("/addThuongHieu")
    public ResponseEntity<?> addThuongHieu(@RequestParam("tenThuongHieu") String tenThuongHieu){
        return ResponseEntity.ok(thuongHieuService.getThuongHieuOrCreateThuongHieu(tenThuongHieu));
    }
    @PutMapping("/changeTrangThaiThuongHieu")
    public ResponseEntity<?> changeTrangThaiThuongHieu(@RequestParam("idThuongHieu") Integer idThuongHieu){
        return thuongHieuService.changeTrangThaiThuongHieu(idThuongHieu);
    }
    @PutMapping("/updateThuongHieu")
    public ResponseEntity<?> updateThuongHieu(@RequestBody ThuongHieu thuongHieu){
        return thuongHieuService.updateThuongHieu(thuongHieu);
    }
    
}
