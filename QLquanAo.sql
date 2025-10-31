-- DROP SCHEMA dbo;

CREATE SCHEMA dbo;
-- QLBanQuanAo.dbo.chat_lieu definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.chat_lieu;

CREATE TABLE QLBanQuanAo.dbo.chat_lieu (
	id_chat_lieu int IDENTITY(1,1) NOT NULL,
	ma_chat_lieu nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	ten_chat_lieu nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	ngay_tao datetime DEFAULT getdate() NULL,
	ngay_sua datetime NULL,
	trang_thai bit DEFAULT 1 NULL,
	CONSTRAINT PK__Chat_Lie__B66D25F3320C0D8B PRIMARY KEY (id_chat_lieu),
	CONSTRAINT UQ__Chat_Lie__95D58767F6FB1C33 UNIQUE (ma_chat_lieu)
);


-- QLBanQuanAo.dbo.danh_muc_san_pham definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.danh_muc_san_pham;

CREATE TABLE QLBanQuanAo.dbo.danh_muc_san_pham (
	id_danh_muc int IDENTITY(1,1) NOT NULL,
	ma_danh_muc nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	ten_danh_muc nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	mo_ta nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	ngay_tao datetime DEFAULT getdate() NULL,
	ngay_sua datetime NULL,
	trang_thai bit NULL,
	CONSTRAINT PK__Danh_Muc__8CE82CD1C30AA2C7 PRIMARY KEY (id_danh_muc),
	CONSTRAINT UQ__Danh_Muc__70624BDCCCF705D7 UNIQUE (ma_danh_muc)
);


-- QLBanQuanAo.dbo.khach_hang definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.khach_hang;

CREATE TABLE QLBanQuanAo.dbo.khach_hang (
	id_khach_hang int IDENTITY(1,1) NOT NULL,
	ma_khach_hang nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	ten_dang_nhap nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	mat_khau nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	email nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	so_dien_thoai nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	ho_ten nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	gioi_tinh nvarchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	ngay_sinh date NULL,
	dia_chi nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	ngay_lap datetime DEFAULT getdate() NULL,
	trang_thai nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT N'HOAT_DONG' NULL,
	ghi_chu nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK__Khach_Ha__9B2CAEF3DC923D0D PRIMARY KEY (id_khach_hang),
	CONSTRAINT UQ__Khach_Ha__363698B3CD57D331 UNIQUE (ten_dang_nhap),
	CONSTRAINT UQ__Khach_Ha__AB6E6164469764EB UNIQUE (email),
	CONSTRAINT UQ__Khach_Ha__BD03D94CFF13179E UNIQUE (so_dien_thoai),
	CONSTRAINT UQ__Khach_Ha__C9817AF749A9E051 UNIQUE (ma_khach_hang)
);


-- QLBanQuanAo.dbo.khuyen_mai definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.khuyen_mai;

CREATE TABLE QLBanQuanAo.dbo.khuyen_mai (
	id_khuyen_mai int IDENTITY(1,1) NOT NULL,
	ma_khuyen_mai nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	ten_khuyen_mai nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	mo_ta nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	ngay_bat_dau datetime NULL,
	ngay_het_han datetime NULL,
	gia_tri_toi_da decimal(38,0) NULL,
	gia_tri_giam decimal(18,2) NULL,
	kieu_giam_gia nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	trang_thai bit DEFAULT 1 NULL,
	CONSTRAINT PK__Khuyen_M__E5173E98C2D85497 PRIMARY KEY (id_khuyen_mai),
	CONSTRAINT UQ__Khuyen_M__01A88CB22FB16AD3 UNIQUE (ma_khuyen_mai)
);


-- QLBanQuanAo.dbo.kich_thuoc definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.kich_thuoc;

CREATE TABLE QLBanQuanAo.dbo.kich_thuoc (
	id_kich_thuoc int IDENTITY(1,1) NOT NULL,
	ma_kich_thuoc nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	gia_tri nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	don_vi nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	trang_thai bit DEFAULT 1 NULL,
	CONSTRAINT PK__Kich_Thu__CA778371DCFA4D35 PRIMARY KEY (id_kich_thuoc),
	CONSTRAINT UQ__Kich_Thu__8C3E646FB4A2067E UNIQUE (ma_kich_thuoc)
);


-- QLBanQuanAo.dbo.mau_sac definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.mau_sac;

CREATE TABLE QLBanQuanAo.dbo.mau_sac (
	id_mau_sac int IDENTITY(1,1) NOT NULL,
	ma_mau_sac nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	ten_mau_sac nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	trang_thai bit DEFAULT 1 NULL,
	CONSTRAINT PK__Mau_Sac__5D8EF42621AA00A6 PRIMARY KEY (id_mau_sac),
	CONSTRAINT UQ__Mau_Sac__2C3B09CF12679E16 UNIQUE (ma_mau_sac)
);


-- QLBanQuanAo.dbo.thuong_hieu definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.thuong_hieu;

CREATE TABLE QLBanQuanAo.dbo.thuong_hieu (
	id_thuong_hieu int IDENTITY(1,1) NOT NULL,
	ma_thuong_hieu nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	ten_thuong_hieu nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	ngay_tao datetime DEFAULT getdate() NULL,
	ngay_sua datetime NULL,
	trang_thai bit DEFAULT 1 NULL,
	CONSTRAINT PK__Thuong_H__37E13EF3AAB321E1 PRIMARY KEY (id_thuong_hieu),
	CONSTRAINT UQ__Thuong_H__C4FB3F33CF1E9141 UNIQUE (ma_thuong_hieu)
);


-- QLBanQuanAo.dbo.voucher definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.voucher;

CREATE TABLE QLBanQuanAo.dbo.voucher (
	id_voucher int IDENTITY(1,1) NOT NULL,
	ma_voucher nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	ten_voucher nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	mo_ta nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	ngay_bat_dau datetime NULL,
	ngay_het_han datetime NULL,
	gia_tri_giam decimal(18,2) NULL,
	kieu_giam_gia nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	so_luong int NULL,
	gia_tri_toi_thieu decimal(18,2) NULL,
	gia_tri_toi_da decimal(18,2) NULL,
	trang_thai bit DEFAULT 1 NULL,
	CONSTRAINT PK__Voucher__AEE2A674225588E5 PRIMARY KEY (id_voucher),
	CONSTRAINT UQ__Voucher__466D17BF62442306 UNIQUE (ma_voucher)
);


-- QLBanQuanAo.dbo.dia_chi_khach_hang definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.dia_chi_khach_hang;

CREATE TABLE QLBanQuanAo.dbo.dia_chi_khach_hang (
	id_dia_chi_khach_hang int IDENTITY(1,1) NOT NULL,
	id_khach_hang int NOT NULL,
	so_nha nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	xa_phuong nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	quan_huyen nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	tinh_thanh_pho nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	dia_chi_mac_dinh bit DEFAULT 0 NULL,
	CONSTRAINT PK__Dia_Chi___2FC4FDE4A93DC72F PRIMARY KEY (id_dia_chi_khach_hang),
	CONSTRAINT FK__Dia_Chi_K__id_kh__5CD6CB2B FOREIGN KEY (id_khach_hang) REFERENCES QLBanQuanAo.dbo.khach_hang(id_khach_hang)
);


-- QLBanQuanAo.dbo.gio_hang definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.gio_hang;

CREATE TABLE QLBanQuanAo.dbo.gio_hang (
	id_gio_hang int IDENTITY(1,1) NOT NULL,
	id_khach_hang int NULL,
	CONSTRAINT PK__Gio_Hang__0EE4A21978717638 PRIMARY KEY (id_gio_hang),
	CONSTRAINT FK__Gio_Hang__id_kha__6EF57B66 FOREIGN KEY (id_khach_hang) REFERENCES QLBanQuanAo.dbo.khach_hang(id_khach_hang)
);


-- QLBanQuanAo.dbo.hoa_don definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.hoa_don;

CREATE TABLE QLBanQuanAo.dbo.hoa_don (
	id_hoa_don int IDENTITY(1,1) NOT NULL,
	ma_hoa_don nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	id_khach_hang int NULL,
	id_voucher int NULL,
	ngay_tao datetime DEFAULT getdate() NULL,
	ngay_sua datetime NULL,
	email nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	sdt nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	ghi_chu nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	dia_chi nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	tong_tien_sau_giam decimal(18,2) NULL,
	phuong_thuc_nhan_hang nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	tong_tien_truoc_giam decimal(18,2) NULL,
	hinh_thuc_thanh_toan nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	phu_thu decimal(18,2) NULL,
	trang_thai nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	ho_ten nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	loai_hoa_don nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	phi_van_chuyen decimal(18,2) NULL,
	CONSTRAINT PK__Hoa_Don__342B812A4656E7DD PRIMARY KEY (id_hoa_don),
	CONSTRAINT UQ__Hoa_Don__DBE2D9E2FB8F3908 UNIQUE (ma_hoa_don),
	CONSTRAINT FK__Hoa_Don__id_khac__7C4F7684 FOREIGN KEY (id_khach_hang) REFERENCES QLBanQuanAo.dbo.khach_hang(id_khach_hang),
	CONSTRAINT FK__Hoa_Don__id_vouc__7D439ABD FOREIGN KEY (id_voucher) REFERENCES QLBanQuanAo.dbo.voucher(id_voucher)
);


-- QLBanQuanAo.dbo.lich_su_dang_nhap definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.lich_su_dang_nhap;

CREATE TABLE QLBanQuanAo.dbo.lich_su_dang_nhap (
	id_lich_su_dang_nhap int IDENTITY(1,1) NOT NULL,
	id_khach_hang int NULL,
	ngay_dang_nhap datetime DEFAULT getdate() NULL,
	ip_address nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK__Lich_Su___B327771298B349BC PRIMARY KEY (id_lich_su_dang_nhap),
	CONSTRAINT FK__Lich_Su_D__id_kh__60A75C0F FOREIGN KEY (id_khach_hang) REFERENCES QLBanQuanAo.dbo.khach_hang(id_khach_hang)
);


-- QLBanQuanAo.dbo.san_pham definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.san_pham;

CREATE TABLE QLBanQuanAo.dbo.san_pham (
	id_san_pham int IDENTITY(1,1) NOT NULL,
	ma_san_pham nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	ten_san_pham nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	mo_ta nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	trang_thai bit DEFAULT 1 NULL,
	id_thuong_hieu int NULL,
	id_chat_lieu int NULL,
	id_danh_muc int NULL,
	anh_dai_dien nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK__San_Pham__5776A529C38D78AC PRIMARY KEY (id_san_pham),
	CONSTRAINT UQ__San_Pham__9D25990D1D77C4D5 UNIQUE (ma_san_pham),
	CONSTRAINT FK__San_Pham__id_cha__3C69FB99 FOREIGN KEY (id_chat_lieu) REFERENCES QLBanQuanAo.dbo.chat_lieu(id_chat_lieu),
	CONSTRAINT FK__San_Pham__id_dan__3D5E1FD2 FOREIGN KEY (id_danh_muc) REFERENCES QLBanQuanAo.dbo.danh_muc_san_pham(id_danh_muc),
	CONSTRAINT FK__San_Pham__id_thu__3B75D760 FOREIGN KEY (id_thuong_hieu) REFERENCES QLBanQuanAo.dbo.thuong_hieu(id_thuong_hieu)
);


-- QLBanQuanAo.dbo.theo_doi_don_hang definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.theo_doi_don_hang;

CREATE TABLE QLBanQuanAo.dbo.theo_doi_don_hang (
	id_theo_doi_don_hang int IDENTITY(1,1) NOT NULL,
	id_hoa_don int NULL,
	trang_thai nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	ngay_chuyen datetime DEFAULT getdate() NULL,
	noi_dung_doi nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK__Theo_Doi__8E25443F55FCB87C PRIMARY KEY (id_theo_doi_don_hang),
	CONSTRAINT FK__Theo_Doi___id_ho__05D8E0BE FOREIGN KEY (id_hoa_don) REFERENCES QLBanQuanAo.dbo.hoa_don(id_hoa_don)
);


-- QLBanQuanAo.dbo.chi_tiet_san_pham definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.chi_tiet_san_pham;

CREATE TABLE QLBanQuanAo.dbo.chi_tiet_san_pham (
	id_chi_tiet_san_pham int IDENTITY(1,1) NOT NULL,
	qr_code nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	gia_ban decimal(18,2) NULL,
	so_luong int DEFAULT 0 NULL,
	ngay_tao datetime DEFAULT getdate() NULL,
	ngay_sua datetime NULL,
	trang_thai bit DEFAULT 1 NULL,
	id_san_pham int NOT NULL,
	id_kich_thuoc int NULL,
	id_mau_sac int NULL,
	CONSTRAINT PK__Chi_Tiet__F5455242116E6761 PRIMARY KEY (id_chi_tiet_san_pham),
	CONSTRAINT FK__Chi_Tiet___id_ki__440B1D61 FOREIGN KEY (id_kich_thuoc) REFERENCES QLBanQuanAo.dbo.kich_thuoc(id_kich_thuoc),
	CONSTRAINT FK__Chi_Tiet___id_ma__44FF419A FOREIGN KEY (id_mau_sac) REFERENCES QLBanQuanAo.dbo.mau_sac(id_mau_sac),
	CONSTRAINT FK__Chi_Tiet___id_sa__4316F928 FOREIGN KEY (id_san_pham) REFERENCES QLBanQuanAo.dbo.san_pham(id_san_pham)
);


-- QLBanQuanAo.dbo.danh_sach_yeu_thich definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.danh_sach_yeu_thich;

CREATE TABLE QLBanQuanAo.dbo.danh_sach_yeu_thich (
	id_khach_hang int NOT NULL,
	id_chi_tiet_san_pham int NOT NULL,
	ngay_them datetime DEFAULT getdate() NULL,
	CONSTRAINT PK__Danh_Sac__A478FBD77471C564 PRIMARY KEY (id_khach_hang,id_chi_tiet_san_pham),
	CONSTRAINT FK__Danh_Sach__id_ch__656C112C FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES QLBanQuanAo.dbo.chi_tiet_san_pham(id_chi_tiet_san_pham),
	CONSTRAINT FK__Danh_Sach__id_kh__6477ECF3 FOREIGN KEY (id_khach_hang) REFERENCES QLBanQuanAo.dbo.khach_hang(id_khach_hang)
);


-- QLBanQuanAo.dbo.hinh_anh definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.hinh_anh;

CREATE TABLE QLBanQuanAo.dbo.hinh_anh (
	id_hinh_anh int IDENTITY(1,1) NOT NULL,
	id_chi_tiet_san_pham int NULL,
	hinh_anh nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	anh_chinh bit DEFAULT 0 NOT NULL,
	CONSTRAINT PK__Hinh_Anh__B62B4243FA5807F8 PRIMARY KEY (id_hinh_anh),
	CONSTRAINT FK__Hinh_Anh__id_chi__49C3F6B7 FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES QLBanQuanAo.dbo.chi_tiet_san_pham(id_chi_tiet_san_pham)
);


-- QLBanQuanAo.dbo.hoa_don_chi_tiet definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.hoa_don_chi_tiet;

CREATE TABLE QLBanQuanAo.dbo.hoa_don_chi_tiet (
	id_hoa_don_chi_tiet int IDENTITY(1,1) NOT NULL,
	id_hoa_don int NULL,
	id_chi_tiet_san_pham int NULL,
	so_luong int NULL,
	don_gia decimal(18,2) NULL,
	CONSTRAINT PK__Hoa_Don___683F56942B4831EF PRIMARY KEY (id_hoa_don_chi_tiet),
	CONSTRAINT FK__Hoa_Don_C__id_ch__02084FDA FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES QLBanQuanAo.dbo.chi_tiet_san_pham(id_chi_tiet_san_pham),
	CONSTRAINT FK__Hoa_Don_C__id_ho__01142BA1 FOREIGN KEY (id_hoa_don) REFERENCES QLBanQuanAo.dbo.hoa_don(id_hoa_don)
);


-- QLBanQuanAo.dbo.binh_luan definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.binh_luan;

CREATE TABLE QLBanQuanAo.dbo.binh_luan (
	id_binh_luan int IDENTITY(1,1) NOT NULL,
	id_khach_hang int NULL,
	id_chi_tiet_san_pham int NULL,
	noi_dung_binh_luan nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	ngay_tao datetime DEFAULT getdate() NULL,
	ngay_sua datetime NULL,
	danh_gia int NULL,
	chinh_sua bit DEFAULT 0 NULL,
	CONSTRAINT PK__Binh_Lua__798139FDDD50ECA1 PRIMARY KEY (id_binh_luan),
	CONSTRAINT FK__Binh_Luan__id_ch__6C190EBB FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES QLBanQuanAo.dbo.chi_tiet_san_pham(id_chi_tiet_san_pham),
	CONSTRAINT FK__Binh_Luan__id_kh__6B24EA82 FOREIGN KEY (id_khach_hang) REFERENCES QLBanQuanAo.dbo.khach_hang(id_khach_hang)
);
ALTER TABLE QLBanQuanAo.dbo.binh_luan WITH NOCHECK ADD CONSTRAINT CK__Binh_Luan__danh___693CA210 CHECK (([danh_gia]>=(1) AND [danh_gia]<=(5)));


-- QLBanQuanAo.dbo.chi_tiet_gio_hang definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.chi_tiet_gio_hang;

CREATE TABLE QLBanQuanAo.dbo.chi_tiet_gio_hang (
	id_chi_tiet_gio_hang int IDENTITY(1,1) NOT NULL,
	id_gio_hang int NULL,
	id_chi_tiet_san_pham int NULL,
	so_luong int DEFAULT 1 NULL,
	CONSTRAINT PK__Chi_Tiet__589F3069373DAFD1 PRIMARY KEY (id_chi_tiet_gio_hang),
	CONSTRAINT FK__Chi_Tiet___id_ch__73BA3083 FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES QLBanQuanAo.dbo.chi_tiet_san_pham(id_chi_tiet_san_pham),
	CONSTRAINT FK__Chi_Tiet___id_gi__72C60C4A FOREIGN KEY (id_gio_hang) REFERENCES QLBanQuanAo.dbo.gio_hang(id_gio_hang)
);


-- QLBanQuanAo.dbo.chi_tiet_khuyen_mai definition

-- Drop table

-- DROP TABLE QLBanQuanAo.dbo.chi_tiet_khuyen_mai;

CREATE TABLE QLBanQuanAo.dbo.chi_tiet_khuyen_mai (
	id_ctkm int IDENTITY(1,1) NOT NULL,
	id_khuyen_mai int NULL,
	id_chi_tiet_san_pham int NULL,
	gia_sau_giam decimal(18,2) NULL,
	CONSTRAINT PK__Chi_Tiet__69C7DD2EA41CDF1D PRIMARY KEY (id_ctkm),
	CONSTRAINT FK__Chi_Tiet___id_ch__5165187F FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES QLBanQuanAo.dbo.chi_tiet_san_pham(id_chi_tiet_san_pham),
	CONSTRAINT FK__Chi_Tiet___id_kh__5070F446 FOREIGN KEY (id_khuyen_mai) REFERENCES QLBanQuanAo.dbo.khuyen_mai(id_khuyen_mai)
);


