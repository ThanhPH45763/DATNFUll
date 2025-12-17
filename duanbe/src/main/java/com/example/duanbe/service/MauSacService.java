package com.example.duanbe.service;

import com.example.duanbe.entity.DanhMuc;
import com.example.duanbe.entity.MauSac;
import com.example.duanbe.entity.ThuongHieu;
import com.example.duanbe.repository.MauSacRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MauSacService {
    @Autowired
    MauSacRepo mauSacRepo;

    public List<MauSac> listFindAllMauSac() {
        return mauSacRepo.findAll();
    }

    // ⚠️ DEPRECATED: Sử dụng createMauSac thay thế
    public MauSac getMauSacOrCreateMauSac(String tenMauSac) {
        Optional<MauSac> exitingMauSac = mauSacRepo.findAll().stream()
                .filter(mauSac -> tenMauSac.equalsIgnoreCase(Optional.ofNullable(mauSac.getTen_mau_sac()).orElse("")))
                .findFirst();

        if (exitingMauSac.isPresent()) {
            return exitingMauSac.get();
        }

        // Nếu không tìm thấy, tạo mã mới
        int maxNumber = mauSacRepo.findAll().stream()
                .map(MauSac::getMa_mau_sac)
                .filter(ma -> ma.startsWith("MS0"))
                .map(ma -> ma.substring(3))
                .filter(num -> num.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);

        // Tạo đối tượng mới (dùng mã auto-gen)
        MauSac newMauSac = new MauSac();
        newMauSac.setMa_mau_sac("MS0" + (maxNumber + 1));
        newMauSac.setTen_mau_sac(tenMauSac);
        newMauSac.setTrang_thai(true);
        mauSacRepo.save(newMauSac);
        return newMauSac;
    }

    // ✅ NEW: Tạo màu sắc với hex code
    public MauSac createMauSac(String maMauSac, String tenMauSac) {
        // Validate hex code format
        if (maMauSac == null || !maMauSac.matches("^#[A-Fa-f0-9]{6}$")) {
            throw new IllegalArgumentException("Mã màu không hợp lệ! Phải có định dạng #RRGGBB (ví dụ: #FF5733)");
        }

        // Validate tên màu
        if (tenMauSac == null || tenMauSac.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên màu không được để trống!");
        }

        // Kiểm tra trùng lặp theo hex code
        Optional<MauSac> existingByHex = mauSacRepo.findAll().stream()
                .filter(m -> maMauSac.equalsIgnoreCase(m.getMa_mau_sac()))
                .findFirst();

        if (existingByHex.isPresent()) {
            throw new IllegalArgumentException("Mã màu " + maMauSac + " đã tồn tại!");
        }

        // Tạo màu mới
        MauSac newMauSac = new MauSac();
        newMauSac.setMa_mau_sac(maMauSac.toUpperCase()); // Normalize to uppercase
        newMauSac.setTen_mau_sac(tenMauSac.trim());
        newMauSac.setTrang_thai(true);

        return mauSacRepo.save(newMauSac);
    }

    public ResponseEntity<?> changeTrangThaiMauSac(@RequestParam("idMauSac") Integer idMauSac) {
        if (idMauSac != null) {
            MauSac mauSac = mauSacRepo.findById(idMauSac).get();
            if (mauSac.getTrang_thai().equals(true)) {
                mauSac.setTrang_thai(false);
            } else {
                mauSac.setTrang_thai(true);
            }
            mauSacRepo.save(mauSac);
            return ResponseEntity.ok(mauSac);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("messege", "Lỗi null");
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> updateMauSac(@RequestBody MauSac mauSac) {
        if (mauSac.getId_mau_sac() != null) {
            MauSac mauSacSua = mauSacRepo.findById(mauSac.getId_mau_sac()).get();

            // ✅ UPDATE: Cho phép cập nhật cả mã màu và tên màu
            if (mauSac.getMa_mau_sac() != null && !mauSac.getMa_mau_sac().isEmpty()) {
                // Validate hex format nếu update mã màu
                if (!mauSac.getMa_mau_sac().matches("^#[A-Fa-f0-9]{6}$")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Mã màu không hợp lệ! Phải có định dạng #RRGGBB");
                    return ResponseEntity.badRequest().body(error);
                }
                mauSacSua.setMa_mau_sac(mauSac.getMa_mau_sac().toUpperCase());
            }

            if (mauSac.getTen_mau_sac() != null && !mauSac.getTen_mau_sac().trim().isEmpty()) {
                mauSacSua.setTen_mau_sac(mauSac.getTen_mau_sac());
            }

            mauSacRepo.save(mauSacSua);
            return ResponseEntity.ok(mauSacSua);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi id null");
            return ResponseEntity.badRequest().body(error);
        }
    }
}
