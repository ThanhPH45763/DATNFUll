// package com.example.duanbe.ImportAndExportEx;

// import com.example.duanbe.entity.ChiTietSanPham;
// import com.example.duanbe.service.ChiTietSanPhamService;
// import org.springframework.stereotype.Component;

// import java.util.ArrayList;
// import java.util.List;

// @Component
// public class ExcelSaveDB {
//     private final ChiTietSanPhamService chiTietSanPhamService;
    
//     public ExcelSaveDB(ChiTietSanPhamService chiTietSanPhamService) {
//         this.chiTietSanPhamService = chiTietSanPhamService;
//     }
    
//     public List<ChiTietSanPhamValidate> saveDataFromExcel(List<ChiTietSanPhamValidate> excelData) {
//         List<ChiTietSanPhamValidate> results = new ArrayList<>();
        
//         for (ChiTietSanPhamValidate data : excelData) {
//             data.validate();
//             if (data.isValid()) {
//                 try {
//                     ChiTietSanPham chiTietSanPham = new ChiTietSanPham();
//                     // Map validated data to entity
//                     chiTietSanPham.setMaSanPham(data.getMaSanPham());
//                     // Set other fields
                    
//                     chiTietSanPhamService.save(chiTietSanPham);
//                     data.setMessage("Saved successfully");
//                 } catch (Exception e) {
//                     data.setValid(false);
//                     data.setMessage("Error saving to database: " + e.getMessage());
//                 }
//             }
//             results.add(data);
//         }
        
//         return results;
//     }
// }