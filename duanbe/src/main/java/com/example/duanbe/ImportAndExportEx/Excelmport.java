// package com.example.duanbe.ImportAndExportEx;

// import com.example.duanbe.entity.ChiTietSanPham;
// import org.apache.poi.ss.usermodel.*;
// import org.apache.poi.xssf.usermodel.XSSFWorkbook;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.IOException;
// import java.io.InputStream;
// import java.util.ArrayList;
// import java.util.Iterator;
// import java.util.List;

// public class Excelmport {
//     public static List<ChiTietSanPhamValidate> excelToChiTietSanPhamList(InputStream inputStream) {
//         try {
//             Workbook workbook = new XSSFWorkbook(inputStream);
//             Sheet sheet = workbook.getSheetAt(0);
//             Iterator<Row> rows = sheet.iterator();
//             List<ChiTietSanPhamValidate> chiTietSanPhamList = new ArrayList<>();

//             int rowNumber = 0;
//             while (rows.hasNext()) {
//                 Row currentRow = rows.next();
                
//                 // Skip header
//                 if (rowNumber == 0) {
//                     rowNumber++;
//                     continue;
//                 }

//                 ChiTietSanPhamValidate chiTietSanPham = new ChiTietSanPhamValidate();
                
//                 // Get cell values
//                 if (currentRow.getCell(0) != null) {
//                     chiTietSanPham.setMaSanPham(getStringValue(currentRow.getCell(0)));
//                 }
//                 // Add more fields as needed
                
//                 chiTietSanPhamList.add(chiTietSanPham);
//             }
            
//             workbook.close();
//             return chiTietSanPhamList;
            
//         } catch (IOException e) {
//             throw new RuntimeException("Error reading Excel file: " + e.getMessage());
//         }
//     }
    
//     private static String getStringValue(Cell cell) {
//         if (cell == null) return null;
//         switch (cell.getCellType()) {
//             case STRING:
//                 return cell.getStringCellValue();
//             case NUMERIC:
//                 return String.valueOf((int) cell.getNumericCellValue());
//             default:
//                 return null;
//         }
//     }
// }