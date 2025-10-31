// package com.example.duanbe.ImportAndExportEx;

// import com.example.duanbe.entity.ChiTietSanPham;
// import org.apache.poi.ss.usermodel.*;
// import org.apache.poi.xssf.usermodel.XSSFWorkbook;
// import java.io.ByteArrayInputStream;
// import java.io.ByteArrayOutputStream;
// import java.io.IOException;
// import java.util.List;

// public class ExcelExport {
//     public static ByteArrayInputStream contactListToExcelFile(List<ChiTietSanPham> chiTietSanPhams) {
//         try(Workbook workbook = new XSSFWorkbook()){
//             Sheet sheet = workbook.createSheet("ChiTietSanPham");
            
//             Row row = sheet.createRow(0);
//             CellStyle headerCellStyle = workbook.createCellStyle();
//             headerCellStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
//             headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
//             // Creating header
//             Cell cell = row.createCell(0);
//             cell.setCellValue("ID");
//             cell.setCellStyle(headerCellStyle);
            
//             cell = row.createCell(1);
//             cell.setCellValue("Mã sản phẩm");
//             cell.setCellStyle(headerCellStyle);
            
//             // Add more headers as needed
            
//             // Creating data rows
//             for(int i = 0; i < chiTietSanPhams.size(); i++) {
//                 Row dataRow = sheet.createRow(i + 1);
//                 ChiTietSanPham chiTietSanPham = chiTietSanPhams.get(i);
                
//                 dataRow.createCell(0).setCellValue(chiTietSanPham.getId());
//                 dataRow.createCell(1).setCellValue(chiTietSanPham.getMaSanPham());
//                 // Add more fields as needed
//             }
            
//             // Making size of column auto resize to fit with data
//             sheet.autoSizeColumn(0);
//             sheet.autoSizeColumn(1);
            
//             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//             workbook.write(outputStream);
//             return new ByteArrayInputStream(outputStream.toByteArray());
//         } catch (IOException ex) {
//             throw new RuntimeException("Error creating Excel file: " + ex.getMessage());
//         }
//     }
// }