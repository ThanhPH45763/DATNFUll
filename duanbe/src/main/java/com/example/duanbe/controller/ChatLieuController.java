
package com.example.duanbe.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.duanbe.entity.ChatLieu;
import com.example.duanbe.service.ChatLieuService;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173",allowedHeaders = "*",methods = {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
@RequestMapping("/admin/quan_ly_san_pham")

class ChatLieuController {
@Autowired
    ChatLieuService chatLieuService;
    @GetMapping("/ChatLieu")
    public List<ChatLieu> getAllChatLieu(){
        return chatLieuService.listFindAllChatLieu();
    }
    @PostMapping("/addChatLieu")
    public ResponseEntity<?> addChatLieu(@RequestParam("tenChatLieu") String tenChatLieu){
        return ResponseEntity.ok(chatLieuService.getChatLieuOrCreateChatLieu(tenChatLieu));
    }
    @PutMapping("/changeTrangThaiChatLieu")
    public ResponseEntity<?> changeTrangThaiChatLieu(@RequestParam("idChatLieu") Integer idChatLieu){
        return chatLieuService.changeTrangThaiChatLieu(idChatLieu);
    }
    @PutMapping("/updateChatLieu")
    public ResponseEntity<?> updateChatLieu(@RequestBody ChatLieu chatLieu){
        return chatLieuService.updateChatLieu(chatLieu);
    }
    
}
