package com.example.duanbe.ImportAndExportEx;

import com.example.duanbe.entity.ChatLieu;
import com.example.duanbe.entity.ChiTietSanPham;
import com.example.duanbe.entity.DanhMuc;
import com.example.duanbe.entity.KichThuoc;
import com.example.duanbe.entity.MauSac;
import com.example.duanbe.entity.SanPham;
import com.example.duanbe.entity.ThuongHieu;
import com.example.duanbe.repository.ChatLieuRepo;
import com.example.duanbe.repository.ChiTietSanPhamRepo;
import com.example.duanbe.repository.DanhMucRepo;
import com.example.duanbe.repository.MauSacRepo;
import com.example.duanbe.repository.SanPhamRepo;
import com.example.duanbe.repository.ThuongHieuRepo;
import com.example.duanbe.service.ChatLieuService;
import com.example.duanbe.service.ChiTietSanPhamService;
import com.example.duanbe.service.DanhMucService;
import com.example.duanbe.service.KichThuocService;
import com.example.duanbe.service.MauSacService;
import com.example.duanbe.service.SanPhamService;
import com.example.duanbe.service.ThuongHieuService;

import jakarta.transaction.Transactional;
import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Service
public class ExcelSaveDB {
  @Autowired
  SanPhamRepo sanPhamRepo;
  @Autowired
  ChiTietSanPhamRepo chiTietSanPhamRepo;
  @Autowired
  DanhMucRepo danhMucRepo;
  @Autowired
  ThuongHieuRepo thuongHieuRepo;
  @Autowired
  ChatLieuRepo chatLieuRepo;
  @Autowired
  MauSacRepo mauSacRepo;
  @Autowired
  SanPhamService sanPhamService;
  @Autowired
  DanhMucService danhMucService;
  @Autowired
  ChatLieuService chatLieuService;
  @Autowired
  ThuongHieuService thuongHieuService;
  @Autowired
  KichThuocService kichThuocService;
  @Autowired
  MauSacService mauSacService;

  // ArrayList<ChiTietSanPham> listSoSanh = new ArrayList<>();
  @Transactional
  public void saveToDB(List<ChiTietSanPham> list) {
    Map<String, ChiTietSanPham> mapChiTietSanPham = new HashMap<>();

    // Đọc tất cả các bản ghi hiện có từ cơ sở dữ liệu
    for (ChiTietSanPham ctsp : chiTietSanPhamRepo.findAll()) {
      String key = ctsp.getSanPham().getId_san_pham() + "-"
          + ctsp.getMauSac().getId_mau_sac() + "-"
          + ctsp.getKichThuoc().getId_kich_thuoc(); // Key không bao gồm giới tính
      mapChiTietSanPham.put(key, ctsp);
    }

    // Xử lý từng ChiTietSanPham trong danh sách nhập từ file Excel
    for (ChiTietSanPham ctspss : list) {
      // ✅ Đảm bảo các đối tượng liên quan tồn tại trong DB
      DanhMuc danhMuc = danhMucService.getDanhMucOrCreateDanhMuc(ctspss.getSanPham().getDanhMuc().getTen_danh_muc());
      ThuongHieu thuongHieu = thuongHieuService
          .getThuongHieuOrCreateThuongHieu(ctspss.getSanPham().getThuongHieu().getTen_thuong_hieu());
      ChatLieu chatLieu = chatLieuService
          .getChatLieuOrCreateChatLieu(ctspss.getSanPham().getChatLieu().getTen_chat_lieu());
      SanPham sanPham = sanPhamService.getSanPhamOrCreateSanPham(
          ctspss.getSanPham().getTen_san_pham(),
          thuongHieu,
          danhMuc,
          chatLieu);
      MauSac mauSac = mauSacService.getMauSacOrCreateMauSac(ctspss.getMauSac().getTen_mau_sac());
      KichThuoc kichThuoc = kichThuocService.getKichThuocOrCreateKichThuoc(
          ctspss.getKichThuoc().getGia_tri(),
          ctspss.getKichThuoc().getDon_vi());

      // ✅ Cập nhật đối tượng ChiTietSanPham với các đối tượng liên quan
      ctspss.setSanPham(sanPham);
      ctspss.setMauSac(mauSac);
      ctspss.setKichThuoc(kichThuoc);

      // ✅ Tạo key để kiểm tra trùng lặp
      String key = ctspss.getSanPham().getId_san_pham() + "-"
          + ctspss.getMauSac().getId_mau_sac() + "-"
          + ctspss.getKichThuoc().getId_kich_thuoc(); // Key không bao gồm giới tính

      if (mapChiTietSanPham.containsKey(key)) {
        // Nếu đã tồn tại, cộng dồn số lượng
        ChiTietSanPham existingCtsp = mapChiTietSanPham.get(key);
        existingCtsp.setSo_luong(existingCtsp.getSo_luong() + ctspss.getSo_luong()); // Cộng dồn số lượng
        existingCtsp.setNgay_sua(new Date());
        System.out.println("Chạy đến kiểm tra map rồi");
        chiTietSanPhamRepo.save(existingCtsp);
      } else {
        // Nếu chưa tồn tại, thêm mới vào cơ sở dữ liệu
        System.out.println("Chạy vào phần else của kiểm tra map");
        ctspss.setTrang_thai(true);
        ctspss.setNgay_tao(new Date());
        ctspss.setNgay_sua(new Date());
        chiTietSanPhamRepo.save(ctspss);
      }
    }
  }
}
