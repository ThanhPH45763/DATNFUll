// package com.example.duanbe.ImportAndExportEx;

// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// public class ChiTietSanPhamValidate {
//     private String maSanPham;
//     private String tenSanPham;
//     private Double giaBan;
//     private Integer soLuongTon;
//     private String moTa;
//     private String trangThai;
//     // Add more fields as needed
    
//     private String message;
//     private boolean isValid;
    
//     public void validate() {
//         isValid = true;
//         StringBuilder messageBuilder = new StringBuilder();
        
//         if (maSanPham == null || maSanPham.trim().isEmpty()) {
//             messageBuilder.append("Mã sản phẩm không được để trống. ");
//             isValid = false;
//         }
        
//         // Add more validation rules as needed
        
//         message = messageBuilder.toString();
//     }
// }