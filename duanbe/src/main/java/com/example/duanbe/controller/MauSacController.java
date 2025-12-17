package com.example.duanbe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;

import com.example.duanbe.entity.MauSac;
import com.example.duanbe.service.MauSacService;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.PUT, RequestMethod.DELETE })
@RequestMapping("/admin/quan_ly_san_pham")

public class MauSacController {
    @Autowired
    MauSacService mauSacService;

    @GetMapping("/MauSac")
    public List<MauSac> getAllMauSac() {
        return mauSacService.listFindAllMauSac();
    }

    @PostMapping("/addMauSac")
    public ResponseEntity<?> addMauSac(
            @RequestParam("maMauSac") String maMauSac,
            @RequestParam("tenMauSac") String tenMauSac) {
        try {
            MauSac newMauSac = mauSacService.createMauSac(maMauSac, tenMauSac);
            return ResponseEntity.ok(newMauSac);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/changeTrangThaiMauSac")
    public ResponseEntity<?> changeTrangThaiMauSac(@RequestParam("idMauSac") Integer idMauSac) {
        return mauSacService.changeTrangThaiMauSac(idMauSac);
    }

    @PutMapping("/updateMauSac")
    public ResponseEntity<?> updateMauSac(@RequestBody MauSac mauSac) {
        return mauSacService.updateMauSac(mauSac);
    }
}
