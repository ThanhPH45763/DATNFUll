// src/stores/gbStore.js
import { defineStore } from 'pinia'
import { toast } from 'vue3-toastify'
import axiosInstance from '@/config/axiosConfig'
import { sanPhamService } from '@/services/sanPhamService'
import { nhanVienService } from '@/services/nhanVienService'
import { voucherService } from '@/services/voucherService'
import { khuyenMaiService } from '@/services/khuyenMaiService'
import { hoaDonService } from '@/services/hoaDonService'
import { check } from 'prettier'
import { useRoute } from 'vue-router'
import { khachHangService } from '@/services/khachHangService'
import { banHangService } from '@/services/banHangService'
import { bctkService } from '@/services/bctkService'
import { banHangOnlineService } from '@/services/banHangOnlineService'
import router from '@/router'
import { set } from 'date-fns'
export const useGbStore = defineStore('gbStore', {
  state: () => ({
    // Các state hiện tại chung
    changeLanguage: {},
    check: true,
    language: 'EN',
    status: false,
    id: 0,
    checkNoitification: true,
    checkRouter: '',
    checkRoutePresent: '',
    indexMenu: ['1'],
    searchs: '', // Dùng chung cho tìm kiếm
    //Sản phẩm
    getAllSanPham: [],
    getAllChiTietSanPham: [],
    searchChiTietSanPham: [],
    searchSanPham: [],
    getCTSPBySanPhams: [],
    getImages: [],
    danhMucList: [],
    thuongHieuList: [],
    chatLieuList: [],
    mauSacList: [],
    sizeList: [],
    sanPhamById: {},
    // State cho chi tiết sản phẩm
    cTSPBySanPhamFull: [],
    filteredSanPhamList: [],
    //Giỏ hàng và thanh toán
    checkoutItems: [], //Dữ liệu sản phẩm mua ngay
    justAddedProduct: false, // Thêm flag để đánh dấu vừa thêm sản phẩm mới
    maxSoLuongSP: 0,
    trangThaiCTSP: null,
    // Cập nhật cấu trúc cho tìm kiếm và lọc
    currentFilter: null, // Bộ lọc hiện tại
    needFilterRefresh: false, // Flag để đánh dấu khi nào cần refresh dữ liệu bộ lọc
    giaMax: null,
    // State cho tìm kiếm
    searchKeyword: '', // Từ khóa tìm kiếm hiện tại
    searchProductIds: [], // Lưu trữ ID của các sản phẩm tìm thấy
    filterCriteria: {}, // Lưu trữ các tiêu chí lọc
    filteredProductIds: [], // Lưu trữ ID của các sản phẩm đã lọc
    finalProductIds: [], // ID sản phẩm cuối cùng sau khi kết hợp tìm kiếm và lọc
    isSearching: false, // Đang trong trạng thái tìm kiếm?
    isFiltering: false, // Đang trong trạng thái lọc?
    debug: true, // Chế độ debug để ghi log chi tiết
    searchFilterParams: {
      keyword: '',
      giaBanMin: null,
      giaBanMax: null,
      listMauSac: [],
      listDanhMuc: [],
      listThuongHieu: [],
      listChatLieu: [],
      listKichThuoc: []
    },
    //Nhân viên
    getAllNhanVienArr: [],
    totalPages: 0, // Dùng chung cho phân trang
    currentPage: 0, // Dùng chung cho phân trang
    totalItems: 0, // Dùng chung cho phân trang
    nhanVienArr: [],
    nhanVienSearch: [],

    //Hóa đơn
    getAllHoaDonArr: [],
    totalHoaDon: 0,
    currentHoaDon: 0,
    totalItemsHoaDon: 0,
    hoaDonDetail: {},
    chiTietHoaDons: [],
    trangThaiHistory: [],
    getAllHoaDonCTTArr: [],
    listCTSP_HD: [],
    listDCKHinHD: [],
    chiTietTraHang: [], // For returned product details
    traHangs: [],

    // Đăng ký đăng nhập // Thêm state để lưu thông tin người dùng
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
    isLoggedIn: localStorage.getItem('isLoggedIn') === 'true' || false,
    id_roles: localStorage.getItem('id_roles') || null, // Thêm id_roles vào state
    userDetails: null,

    //Khách hàng
    getAllKhachHangArr: [],
    totalKhachHang: 0,
    currentKhachHang: 0,
    totalItemsKhachHang: 0,
    listSanPhamBanHang: [],
    getAllCTSPKMList: [],
    getAllKhachHangNoPageList: [],
    lastCreatedKhachHang: null, // Lưu khách hàng vừa được tạo

    // State cho voucher
    getAllVoucherArr: [],
    voucherSearch: [],
    voucherTotalPages: 0,
    voucherCurrentPage: 0,
    voucherTotalItems: 0,
    voucherSearchs: '',

    // State cho khuyến mãi
    getAllKhuyenMaiArr: [],
    khuyenMaiSearch: [],
    khuyenMaiTotalPages: 0, // Riêng cho khuyến mãi
    khuyenMaiCurrentPage: 0, // Riêng cho khuyến mãi
    khuyenMaiTotalItems: 0, // Riêng cho khuyến mãi
    khuyenMaiSearchs: '', // Riêng cho khuyến mãi

    //Thống kê
    thongKe: {
      doanhThu: 0,
      tongDonHang: 0,
      tongSanPham: 0,
    },
    // Thêm state cho filter
    bctkFilter: {
      type: 'hom-nay',
      startDate: '',
      endDate: '',
    },
    topSanPhamBanChay: [],
    topSanPhamSapHetHang: [],
    // tỉ lệ trạng thái đơn hàng
    tiLeTrangThai: [],
    loading: false,
    error: null,
    //Giỏ hàng của khấch hàng có tài khoản
    gioHang: [],
    // Danh sách địa chỉ của khách hàng
    danhSachDiaChi: [],
    //lưu id hoá đơn
    currentHoaDonId: null,

    // Thêm vào phần state
    filteredProductsData: [],
    isProductLoading: false, // Thêm state để quản lý trạng thái loading sản phẩm
    //Biến check có phải thanh toán mua ngay không
    isThanhToanMuaNgay: false,
  }),

  ///Đầu mút2
  actions: {
    // Sản phẩm siêu sale
    // Thêm action mới cho siêu sale
    async getSanPhamSieuSale() {
      try {
        this.isLoading = true;
        this.error = null;

        const data = await sanPhamService.getSanPhamSieuSale();
        if (!data.error) {
          this.listSanPhamBanHang = data.map(item => ({
            id: item.id_san_pham,
            name: item.ten_san_pham,
            price: Number(item.gia_ban || item.gia_khuyen_mai_cao_nhat || 0),
            oldPrice: Number(item.gia_max || 0),
            brand: item.ten_thuong_hieu,
            image: item.anh_chinh || item.hinh_anh || item.anh_dai_dien,
            gender: item.gioi_tinh || '',
            type: item.ten_danh_muc || '',
            colors: item.mau_sac ? item.mau_sac.split(',') : [],
            rating: item.danh_gia || 0,
            reviews: item.so_luong_danh_gia || 0,
          }));
        } else {
          this.error = data.message;
        }
      } catch (error) {
        this.error = error.message;
        console.error('Lỗi khi lấy sản phẩm siêu sale:', error);
      } finally {
        this.isLoading = false;
      }
    },
    //Tạo hóa đơn chi tiết mua ngay
    async createOrderChiTietMuaNgay(hoaDonChiTiet) {
      const response = await banHangOnlineService.createOrderChiTietMuaNgay(hoaDonChiTiet);
      if (response) {
        this.isThanhToanMuaNgay = true;
      } else {
        this.isThanhToanMuaNgay = false;
      }
    },
    //Chuyển biến thanh toán mua ngay
    setIsThanhToanMuaNgay(value) {
      this.isThanhToanMuaNgay = value;
    },
    //Lấy biến thanh toán mua ngay
    getIsThanhToanMuaNgay() {
      return this.isThanhToanMuaNgay;
    },
    // List sản phẩm theo tên sản phẩm(trang sản phẩm)
    async getSanPhamByTenSP(keywords) {
      console.log('Gọi getSanPhamByTenSP với:', keywords);
      let tenSanPham = '';
      if (Array.isArray(keywords)) {
        tenSanPham = keywords.join(',');
      } else if (typeof keywords === 'string') {
        tenSanPham = keywords;
      }
      const data = await sanPhamService.getSanPhamByTenSP(tenSanPham);
      // Map lại field nếu cần
      this.listSanPhamBanHang = data.map(item => ({
        id: item.id_san_pham,
        name: item.ten_san_pham,
        price: Number(item.gia_ban || item.gia_khuyen_mai_cao_nhat || 0),
        oldPrice: Number(item.gia_max || 0),
        brand: item.ten_thuong_hieu,
        image: item.anh_chinh || item.hinh_anh || item.anh_dai_dien,
        gender: item.gioi_tinh || '',
        type: item.ten_danh_muc || '',
        colors: item.mau_sac ? item.mau_sac.split(',') : [],
        rating: item.danh_gia || 0,
        reviews: item.so_luong_danh_gia || 0,
      }));
    },
    // List sản phẩm theo tên danh mục(trang sản phẩm)
    async getSanPhamByTenDM(keywords) {
      let tenDanhMuc = '';
      if (Array.isArray(keywords)) {
        tenDanhMuc = keywords.join(',');
      } else if (typeof keywords === 'string') {
        tenDanhMuc = keywords;
      }
      // Nếu tenDanhMuc là '', backend nên trả về tất cả sản phẩm
      const data = await sanPhamService.getSanPhamByTenDM(tenDanhMuc);
      this.listSanPhamBanHang = data.map(item => ({
        id: item.id_san_pham,
        name: item.ten_san_pham,
        price: Number(item.gia_ban || item.gia_khuyen_mai_cao_nhat || 0),
        oldPrice: Number(item.gia_max || 0),
        brand: item.ten_thuong_hieu,
        image: item.anh_chinh || item.hinh_anh || item.anh_dai_dien,
        gender: item.gioi_tinh || '',
        type: item.ten_danh_muc || '',
        colors: item.mau_sac ? item.mau_sac.split(',') : [],
        rating: item.danh_gia || 0,
        reviews: item.so_luong_danh_gia || 0,
      }));
    },
    // lấy số lượng còn lại của sản phẩm theo ID
    async getMaxSoLuongSP(idCTSP) {
      const response = await banHangOnlineService.maxSoLuongSP(idCTSP);
      this.maxSoLuongSP = response;
    },
    async getTrangThaiCTSP(idCTSP) {
      try {
        const response = await banHangOnlineService.getTrangThaiCTSP(idCTSP);
        this.trangThaiCTSP = response;
        console.log('trangThaiCTSP: ', this.trangThaiCTSP);
        return response; // Trả về trực tiếp giá trị từ API
      } catch (error) {
        console.error('Lỗi khi lấy trạng thái CTSP:', error);
        return false; // Trả về false nếu có lỗi
      }
    },
    //Giỏ hàng của khấch hàng có tài khoản
    async getGioHang(idKhachHang) {
      const response = await banHangOnlineService.getGioHang(idKhachHang)
      console.log('response: ', response)
      if (response) {
        this.gioHang = response
      } else {
        this.gioHang = []
      }
    },
    async getGioHangByIdKH(idKH, idCTSP, soLuong) {
      try {
        const response = await banHangOnlineService.themGioHangByIdKH(idKH, idCTSP, soLuong)
        console.log('response: ', response)
      } catch (error) {
        console.error('Lỗi trong getGioHang:', error)
        toast.error('Có lỗi xảy ra khi lấy giỏ hàng')
      }
    },
    // Xoá số lượng sản phẩm trong giỏ hàng
    async xoaSoLuongSPGH(idKH, idCTSP, soLuong) {
      try {
        const response = await banHangOnlineService.xoaSoLuongSPGH(idKH, idCTSP, soLuong)
        console.log('response: ', response)
      } catch (error) {
        console.error('Lỗi trong xoaSoLuongSanPhamTrongGioHang:', error)
        toast.error('Có lỗi xảy ra khi xoá số lượng sản phẩm trong giỏ hàng')
      }
    },
    // Danh sách địa chỉ của khách hàng
    async getDanhSachDiaChi(idKhachHang) {
      try {
        const response = await banHangOnlineService.getDanhSachDiaChi(idKhachHang)
        this.danhSachDiaChi = response
        console.log('Danh sách địa chỉ:', this.danhSachDiaChi)
      } catch (error) {
        console.error('Lỗi trong getDanhSachDiaChi:', error)
        toast.error('Có lỗi xảy ra khi lấy danh sách địa chỉ')
      }
    },
    // BCTK
    async getSoLieu(type = null, startDate = null, endDate = null) {
      try {
        console.log('Store getSoLieu được gọi với:', { type, startDate, endDate })
        const response = await bctkService.getSoLieu(type, startDate, endDate)

        if (response) {
          console.log('Dữ liệu nhận được trong store:', response)
          this.thongKe = {
            doanhThu: response.doanhThu || 0,
            tongDonHang: response.tongDonHang || 0,
            tongSanPham: response.tongSanPham || 0,
          }
          console.log('State sau khi cập nhật:', this.thongKe)
          return response
        } else {
          toast.error('Không lấy được dữ liệu thống kê')
          return null
        }
      } catch (error) {
        console.error('Lỗi trong store getSoLieu:', error)
        toast.error('Có lỗi xảy ra khi lấy thống kê')
        return null
      }
    },

    // Action để xử lý thay đổi filter
    async handleBCTKFilterChange(filter, customStartDate = null, customEndDate = null) {
      try {
        this.bctkFilter.type = filter

        if (filter === 'tuy-chon') {
          if (!customStartDate || !customEndDate) {
            toast.error('Vui lòng chọn khoảng thời gian')
            return
          }
          this.bctkFilter.startDate = customStartDate
          this.bctkFilter.endDate = customEndDate
          await this.getSoLieu(filter, customStartDate, customEndDate)
        } else {
          this.bctkFilter.startDate = ''
          this.bctkFilter.endDate = ''
          await this.getSoLieu(filter)
        }
      } catch (error) {
        console.error('Lỗi khi thay đổi filter thống kê:', error)
        toast.error('Có lỗi xảy ra khi lọc thống kê')
      }
    },
    async fetchTiLeTrangThai() {
      try {
        this.loading = true
        const data = await bctkService.getTiLeTrangThai()
        console.log('Dữ liệu thô từ API:', data)

        if (!data || !Array.isArray(data)) {
          console.error('Định dạng dữ liệu không hợp lệ')
          this.tiLeTrangThai = []
          return
        }

        // Chuyển đổi từ dữ liệu API sang định dạng frontend cần
        this.tiLeTrangThai = data.map((item) => ({
          trangThai: this.formatTrangThai(item.trangThaiDonHang || 'Unknown'),
          soLuong: parseFloat(item.tiLeTrangThaiDonHang || 0), // Lấy phần trăm
          tiLe: parseFloat(item.tiLeTrangThaiDonHang || 0),
        }))

        // Thêm dữ liệu mẫu nếu không có
        if (this.tiLeTrangThai.length === 0) {
          this.tiLeTrangThai = [
            {
              trangThai: 'Không có dữ liệu',
              soLuong: 100,
              tiLe: 100,
            },
          ]
        }

        console.log('Dữ liệu đã xử lý:', this.tiLeTrangThai)
      } catch (error) {
        console.error('Lỗi trong fetchTiLeTrangThai:', error)
        this.error = error.message
        this.tiLeTrangThai = [
          {
            trangThai: 'Lỗi',
            soLuong: 100,
            tiLe: 100,
          },
        ]
      } finally {
        this.loading = false
      }
    },

    // Hàm format tên trạng thái (nếu chưa có)
    formatTrangThai(trangThai) {
      switch (trangThai) {
        case 'CHO_XAC_NHAN':
          return 'Chờ xác nhận'
        case 'DA_XAC_NHAN':
          return 'Đã xác nhận'
        case 'DA_CAP_NHAT':
          return 'Đã cập nhật'
        case 'DANG_GIAO':
          return 'Đang giao'
        case 'HOAN_THANH':
          return 'Hoàn thành'
        case 'DA_HUY':
          return 'Đã hủy'
        default:
          return trangThai
      }
    },
    // async getTopSanPhamBanChay() {
    //   const topSanPhamBanChay = await bctkService.topSanPhamBanChay()
    //   this.topSanPhamBanChay = topSanPhamBanChay
    // },

    // Hàm format tên trạng thái (nếu chưa có)
    // formatTrangThai(trangThai) {
    //   switch (trangThai) {
    //     case "CHO_XAC_NHAN": return "Chờ xác nhận";
    //     case "DA_XAC_NHAN": return "Đã xác nhận";
    //     case "DA_CAP_NHAT": return "Đã cập nhật";
    //     case "DANG_GIAO": return "Đang giao";
    //     case "HOAN_THANH": return "Hoàn thành";
    //     case "DA_HUY": return "Đã hủy";
    //     default: return trangThai;
    //   }
    // },
    // async getTopSanPhamBanChay() {
    //   const topSanPhamBanChay = await bctkService.topSanPhamBanChay()
    //   this.topSanPhamBanChay = topSanPhamBanChay
    // },
    // thử nghiệm
    async getTopSanPhamBanChay(type = 'nam-nay', startDate = null, endDate = null) {
      try {
        console.log('Store params:', { type, startDate, endDate })
        // Đảm bảo luôn có type
        const response = await bctkService.getTopSanPhamBanChay(
          type || 'nam-nay',
          startDate,
          endDate,
        )

        if (response && Array.isArray(response)) {
          this.topSanPhamBanChay = response.map((item, index) => ({
            stt: index + 1,
            ma_san_pham: item.ma_san_pham || '',
            ten_san_pham: item.ten_san_pham || '',
            so_luong: item.so_luong || 0,
            gia_ban: item.gia_ban || 0,
          }))
        } else {
          console.warn('Invalid response format:', response)
          this.topSanPhamBanChay = []
        }
        return this.topSanPhamBanChay
      } catch (error) {
        console.error('Store error:', error)
        this.topSanPhamBanChay = []
        throw error
      }
    },
    // async getTopSanPhamBanCham() {
    //   const topSanPhamBanCham = await bctkService.topSanPhamBanCham()
    //   this.topSanPhamBanCham = topSanPhamBanCham
    // },
    async getTopSanPhamSapHetHang() {
      try {
        const response = await bctkService.getTopSanPhamSapHetHang()
        if (response && Array.isArray(response)) {
          this.topSanPhamSapHetHang = response.map((item, index) => ({
            stt: index + 1,
            ma_san_pham: item.ma_san_pham || '',
            ten_san_pham: item.ten_san_pham || '',
            so_luong: item.so_luong || 0,
            gia_ban: item.gia_ban || 0,
          }))
        } else {
          console.warn('Invalid response format for topSanPhamSapHetHang:', response)
          this.topSanPhamSapHetHang = []
        }
      } catch (error) {
        console.error('Error in getTopSanPhamSapHetHang:', error)
        this.topSanPhamSapHetHang = []
      }
    },
    //Kết thúc BCTK

    //Nhân viên
    async layDanhSachNhanVien() {
      const nhanVienArr = await nhanVienService.layDanhSachNhanVien()
      this.nhanVienArr = nhanVienArr
    },
    async getAllNhanVien(page = 0, size = 5) {
      try {
        const nhanVien = await nhanVienService.getAllNhanVien(page, size)
        if (nhanVien.error) {
          toast.error('Không lấy được dữ liệu')
          return
        } else {
          // this.getAllNhanVienArr = nhanVien;
          // this.totalItems = 50;  // Tạm thởi hardcode để test
          // this.currentPage = page;
          this.getAllNhanVienArr = nhanVien.content || [] // Lấy danh sách nhân viên
          this.totalPages = nhanVien.totalPages || 0
          this.currentPage = page
          this.totalItems = nhanVien.totalElements || 0
        }
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async getNhanVienLocTrangThai(page = 0, size = 5, trangThai = this.selectTrangThai) {
      try {
        let params = { page, size }
        if (trangThai) {
          params.trangThai = trangThai // Chỉ thêm trạng thái nếu có giá trị
        }
        const nhanvienFilter = await nhanVienService.getNhanVienLocTrangThai(page, size, trangThai)
        if (nhanvienFilter.error) {
          toast.error('Không lấy được dữ liệu')
          return
        } else {
          this.getAllNhanVienArr = nhanvienFilter.content || [] // Lấy danh sách nhân viên
          this.totalPages = nhanvienFilter.totalPages || 0
          this.currentPage = page
          this.totalItems = nhanvienFilter.totalElements || 0
          this.selectedTrangThai = trangThai
        }
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async changeTrangThai(id) {
      try {
        // Cập nhật ngay lập tức UI trước khi gọi API
        const nhanVien = this.getAllNhanVienArr.find((nhanVien) => nhanVien.idNhanVien === id)
        if (nhanVien) {
          nhanVien.trangThai =
            nhanVien.trangThai === 'Đang hoạt động' ? 'Đã nghỉ việc' : 'Đang hoạt động'
        }

        //     const chuyenTrangThai = await nhanVienService.changeTrangThai(id);
        //     if (chuyenTrangThai.error) {
        //         toast.error('Có lỗi xảy ra');
        // Gọi API nhưng không chờ phản hồi để tránh lag
        nhanVienService.changeTrangThai(id).then((response) => {
          if (response.error) {
            toast.error('Có lỗi xảy ra')
            // Nếu lỗi, revert trạng thái lại
            nhanVien.trangThai =
              nhanVien.trangThai === 'Đang hoạt động' ? 'Đã nghỉ việc' : 'Đang hoạt động'
          } else {
            toast.success('Chuyển trạng thái thành công')
          }
        })
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async themNhanVien(nhanVienMoi) {
      console.log('Dữ liệu truyền vào', nhanVienMoi)
      try {
        const themNhanVienres = await nhanVienService.themNhanViens(nhanVienMoi)
        if (themNhanVienres.error) {
          toast.error('Có lỗi xảy ra')
          return
        }
        return themNhanVienres
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    //Lấy nhân viên theo id
    async getNhanVienById(id) {
      const nhanVienById = await nhanVienService.getNhanVienById(id)
      return nhanVienById
    },
    //Sửa nhân viên
    async suaNhanVien(nhanVienUpdate) {
      console.log('Dữ liệu truyền vào', nhanVienUpdate)
      try {
        const suaNhanVienRes = await nhanVienService.suaNhanViens(nhanVienUpdate)
        if (suaNhanVienRes.error) {
          toast.error('Có lỗi xảy ra')
          return
        }
        return suaNhanVienRes
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    //Search nhân viên
    async searchNhanVien(keyword, page = 0, size = 5) {
      try {
        const searchNhanVienRes = await nhanVienService.searchNhanVien(keyword, page, size)
        // console.log('Kết quả tìm kiếm nhân viên:', searchNhanVienRes)
        if (searchNhanVienRes.error) {
          toast.error('Có lỗi xảy ra')
          this.nhanVienSearch = []
          this.totalPages = 0
          this.currentPage = 0
          this.totalItems = 0
        } else if (searchNhanVienRes.content.length === 0) {
          toast.error('Không tìm thấy nhân viên nào')
        } else {
          this.nhanVienSearch = searchNhanVienRes.content || []
          this.totalPages = searchNhanVienRes.totalPages || 0
          this.currentPage = page
          this.totalItems = searchNhanVienRes.totalElements || 0
        }
      } catch (error) {
        console.error(error)
      }
    },
    ////////////-----------------Sản phẩm Action-------------------////////////
    //update Data
    setFilteredProductsData(data) {
      this.filteredProductsData = data; // Tạo mảng mới để đảm bảo reactive
    },
    changeAction(status) {
      this.justAddedProduct = status;
    },
    // Reset tham số tìm kiếm và lọc
    resetSearchFilterParams() {
      console.log('Reset params tìm kiếm/lọc');
      this.searchFilterParams = {
        keyword: '',
        giaBanMin: null,
        giaBanMax: null,
        listMauSac: [],
        listDanhMuc: [],
        listThuongHieu: [],
        listChatLieu: [],
        listKichThuoc: []
      };
    },
    // Cập nhật tham số tìm kiếm và lọc
    updateSearchFilterParams(params) {
      console.log('Cập nhật params tìm kiếm/lọc:', params);
      this.searchFilterParams = {
        // ...this.searchFilterParams,
        ...params
      };
      // Cập nhật filterCriteria cho biến isFiltering hoạt động
      this.filterCriteria = {};

      // Thêm các tiêu chí vào filterCriteria nếu có giá trị
      if (params.listDanhMuc?.length > 0) this.filterCriteria.danhMuc = params.listDanhMuc;
      if (params.listThuongHieu?.length > 0) this.filterCriteria.thuongHieu = params.listThuongHieu;
      if (params.listChatLieu?.length > 0) this.filterCriteria.chatLieu = params.listChatLieu;
      if (params.listMauSac?.length > 0) this.filterCriteria.mauSac = params.listMauSac;
      if (params.listKichThuoc?.length > 0) this.filterCriteria.kichThuoc = params.listKichThuoc;
      if (params.giaBanMin > 0 || params.giaBanMax < this.giaMax) this.filterCriteria.gia = true;

    },
    //Lấy danh sách sản phẩm theo sản phẩm
    async getSanPhamBySP(tenSanPham) {
      try {
        const sanPham = await sanPhamService.getSanPhamBySanPham(tenSanPham)
        if (sanPham.error) {
          toast.error('Không lấy được dữ liệu')
          return
        } else {
          this.listSanPhamBanHang = sanPham
        }
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    //Lấy danh sách sản phẩm theo danh mục

    //Lấy sản phẩm theo id
    async getSanPhamById(id) {
      const sanPhamByIds = await sanPhamService.getSanPhamById(id)
      if (sanPhamByIds.error) {
        toast.error('Không lấy được dữ liệu')
        return
      } else {
        this.sanPhamById = sanPhamByIds
      }
    },
    //Lấy danh sách danh mục
    async getDanhMucList() {
      try {
        console.log('Đang tải danh sách danh mục...')
        const response = await sanPhamService.getDanhMucList()
        if (!response || response.error) {
          toast.error('Không lấy được dữ liệu danh mục')
          return []
        }
        console.log('Danh mục loaded:', response)
        this.danhMucList = response
        return response
      } catch (error) {
        console.error('Lỗi khi tải danh sách danh mục:', error)
        toast.error('Có lỗi xảy ra khi tải danh mục')
        return []
      }
    },

    //Lấy danh sách thương hiệu
    async getThuongHieuList() {
      try {
        console.log('Đang tải danh sách thương hiệu...')
        const response = await sanPhamService.getThuongHieuList()
        if (!response || response.error) {
          toast.error('Không lấy được dữ liệu thương hiệu')
          return []
        }
        console.log('Thương hiệu loaded:', response)
        this.thuongHieuList = response
        return response
      } catch (error) {
        console.error('Lỗi khi tải danh sách thương hiệu:', error)
        toast.error('Có lỗi xảy ra khi tải thương hiệu')
        return []
      }
    },

    //Lấy danh sách chất liệu
    async getChatLieuList() {
      try {
        console.log('Đang tải danh sách chất liệu...')
        const response = await sanPhamService.getChatLieuList()
        if (!response || response.error) {
          toast.error('Không lấy được dữ liệu chất liệu')
          return []
        }
        console.log('Chất liệu loaded:', response)
        this.chatLieuList = response
        return response
      } catch (error) {
        console.error('Lỗi khi tải danh sách chất liệu:', error)
        toast.error('Có lỗi xảy ra khi tải chất liệu')
        return []
      }
    },

    //Lấy danh sách màu sắc
    async getMauSacList() {
      try {
        console.log('Đang tải danh sách màu sắc...')
        const response = await sanPhamService.getMauSacList()
        if (!response || response.error) {
          toast.error('Không lấy được dữ liệu màu sắc')
          return []
        }
        console.log('Màu sắc loaded:', response)
        this.mauSacList = response
        return response
      } catch (error) {
        console.error('Lỗi khi tải danh sách màu sắc:', error)
        toast.error('Có lỗi xảy ra khi tải màu sắc')
        return []
      }
    },

    //Lấy danh sách size
    async getSizeList() {
      try {
        console.log('Đang tải danh sách kích thước...')
        const response = await sanPhamService.getSizeList()
        if (!response || response.error) {
          toast.error('Không lấy được dữ liệu kích thước')
          return []
        }
        console.log('Kích thước loaded:', response)
        this.sizeList = response
        return response
      } catch (error) {
        console.error('Lỗi khi tải danh sách kích thước:', error)
        toast.error('Có lỗi xảy ra khi tải kích thước')
        return []
      }
    },
    //Cập nhật trạng thái sản phẩm
    async changeStatusSanPham(id) {
      try {
        console.log(`[changeStatusSanPham] Bắt đầu xử lý cho sản phẩm ID: ${id}`);

        // Bước 1: Tìm và lưu trạng thái ban đầu
        await this.getAllSP();
        const sanPham = this.getAllSanPham.find((sp) => sp.id_san_pham === id);
        if (!sanPham) {
          console.error(`[changeStatusSanPham] Không tìm thấy sản phẩm với ID: ${id}`);
          toast.error('Không tìm thấy sản phẩm');
          throw new Error('Không tìm thấy sản phẩm');
        }

        // Bước 2: Lưu trạng thái ban đầu
        const oldStatus = sanPham.trang_thai;
        console.log(`[changeStatusSanPham] Trạng thái hiện tại của sản phẩm ID ${id}: ${oldStatus}`);

        // Bước 3: Gọi API để chuyển đổi trạng thái
        console.log(`[changeStatusSanPham] Gọi API chuyển đổi trạng thái cho sản phẩm ID ${id}`);
        const response = await sanPhamService.changeStatusSanPham(id);

        // Bước 4: Lấy và kiểm tra response từ API
        console.log(`[changeStatusSanPham] Response từ API:`, response);

        if (response.error) {
          console.error(`[changeStatusSanPham] API trả về lỗi:`, response.error);
          toast.error('Có lỗi xảy ra khi cập nhật trạng thái');
          return { success: false, message: 'Có lỗi xảy ra' };
        }

        // Bước 5: So sánh trạng thái mới từ response với trạng thái cũ
        const apiNewStatus = response.trang_thai || (oldStatus === true ? false : true);
        console.log(`[changeStatusSanPham] Trạng thái mới từ API: ${apiNewStatus}`);
        console.log(`[changeStatusSanPham] So sánh trạng thái: cũ=${oldStatus}, mới=${apiNewStatus}`);

        // Bước 6: Cập nhật trạng thái trên UI dựa trên kết quả API
        if (oldStatus !== apiNewStatus) {
          console.log(`[changeStatusSanPham] Cập nhật trạng thái UI từ ${oldStatus} thành ${apiNewStatus}`);
          sanPham.trang_thai = apiNewStatus;
          toast.success(`Đã chuyển trạng thái thành ${apiNewStatus}`);
        } else {
          console.log(`[changeStatusSanPham] Không cần cập nhật UI vì trạng thái không thay đổi`);
          toast.info('Trạng thái không thay đổi');
        }

        // Trả về kết quả từ API
        console.log(`[changeStatusSanPham] Hoàn tất xử lý chuyển trạng thái sản phẩm ID ${id}`);
        return {
          success: true,
          data: response,
          oldStatus: oldStatus,
          newStatus: apiNewStatus,
          message: `Cập nhật trạng thái thành công: ${apiNewStatus}`
        };
      } catch (error) {
        console.error(`[changeStatusSanPham] Lỗi khi chuyển trạng thái sản phẩm:`, error);
        toast.error('Có lỗi xảy ra khi xử lý');
        return { success: false, message: error.message };
      }
    },
    async getCTSPBySanPhamFull(idSanPham) {
      const cTSPBySanPhamFull = await sanPhamService.getCTSPBySanPhamFull(idSanPham)
      if (cTSPBySanPhamFull.error) {
        toast.error('Không lấy được dữ liệu')
        return
      } else {
        this.cTSPBySanPhamFull = cTSPBySanPhamFull
        console.log('📦 ==> CHI TIẾT SẢN PHẨM TỪ API <== 📦');
        console.log('Tổng số variants:', cTSPBySanPhamFull.length);
        if (cTSPBySanPhamFull.length > 0) {
          console.log('🔍 Variant đầu tiên:', cTSPBySanPhamFull[0]);
          console.log('📋 Tất cả field names:', Object.keys(cTSPBySanPhamFull[0]));
          console.log('💰 Giá gốc field:', cTSPBySanPhamFull[0].giaGoc || cTSPBySanPhamFull[0].gia_goc || cTSPBySanPhamFull[0].gia_ban);
          console.log('💸 Giá hiện tại field:', cTSPBySanPhamFull[0].giaHienTai || cTSPBySanPhamFull[0].gia_hien_tai || cTSPBySanPhamFull[0].gia_khuyen_mai);
        }
      }
    },
    async getAllSanPhamNgaySua() {
      const sanPhamNgaySua = await sanPhamService.getAllSanPhamNgaySua()
      console.log(sanPhamNgaySua)
      if (sanPhamNgaySua.error) {
        toast.error('Không lấy được dữ liệu')
        return
      } else {
        this.getAllSanPham = sanPhamNgaySua
        return sanPhamNgaySua
      }
    },
    //Chuyển trạng thái chi tiết sản phẩm
    async changeStatusCTSP(id) {
      const changeStatusCTSP = await sanPhamService.changeStatusCTSP(id)
      if (changeStatusCTSP.error) {
        toast.error('Không lấy được dữ liệu')
        return
      } else {
        toast.success('Chuyển trạng thái chi tiết sản phẩm thành công')
      }
    },
    //Chuyển trạng thái All CTSP Hoạt động
    async changeAllCTSPHoatDong(id) {
      try {
        const response = await sanPhamService.changeAllCTSPHoatDong(id)
        console.log('Response changeAllCTSPHoatDong ở store:', response)
        if (response && response.data) {
          toast.success('Chuyển trạng thái chi tiết sản phẩm thành công')
          return response.data  // Trả về dữ liệu từ API
        } else if (response && !response.error) {
          toast.success('Chuyển trạng thái chi tiết sản phẩm thành công')
          return response  // Trường hợp response không có .data nhưng vẫn là response hợp lệ
        } else {
          toast.error('Không lấy được dữ liệu')
          return null
        }
      } catch (error) {
        console.error('Lỗi khi chuyển trạng thái CTSP sang Hoạt động:', error)
        toast.error('Có lỗi xảy ra khi chuyển trạng thái')
        return null
      }
    },
    //Chuyển trạng thái All CTSP Không Hoạt động
    async changeAllCTSPKhongHoatDong(id) {
      try {
        const response = await sanPhamService.changeAllCTSPKhongHoatDong(id)
        console.log('Response changeAllCTSPKhongHoatDong ở store:', response)
        if (response && response.data) {
          toast.success('Chuyển trạng thái chi tiết sản phẩm thành công')
          return response.data  // Trả về dữ liệu từ API
        } else if (response && !response.error) {
          toast.success('Chuyển trạng thái chi tiết sản phẩm thành công')
          return response  // Trường hợp response không có .data nhưng vẫn là response hợp lệ
        } else {
          toast.error('Không lấy được dữ liệu')
          return null
        }
      } catch (error) {
        console.error('Lỗi khi chuyển trạng thái CTSP sang Không Hoạt động:', error)
        toast.error('Có lỗi xảy ra khi chuyển trạng thái')
        return null
      }
    },
    // Add new actions for adding items
    async addDanhMuc(danhMucData) {
      try {
        const response = await sanPhamService.addDanhMuc(danhMucData)
        if (response.error) {
          return { success: false, message: response.message || 'Thêm danh mục thất bại' }
        }
        return { success: true, data: response }
      } catch (error) {
        console.error('Lỗi khi thêm danh mục:', error)
        return { success: false, message: 'Có lỗi xảy ra khi thêm danh mục' }
      }
    },

    async addThuongHieu(thuongHieuData) {
      try {
        const response = await sanPhamService.addThuongHieu(thuongHieuData)
        if (response.error) {
          return { success: false, message: response.message || 'Thêm thương hiệu thất bại' }
        }
        return { success: true, data: response }
      } catch (error) {
        console.error('Lỗi khi thêm thương hiệu:', error)
        return { success: false, message: 'Có lỗi xảy ra khi thêm thương hiệu' }
      }
    },

    async addChatLieu(chatLieuData) {
      try {
        const response = await sanPhamService.addChatLieu(chatLieuData)
        if (response.error) {
          return { success: false, message: response.message || 'Thêm chất liệu thất bại' }
        }
        return { success: true, data: response }
      } catch (error) {
        console.error('Lỗi khi thêm chất liệu:', error)
        return { success: false, message: 'Có lỗi xảy ra khi thêm chất liệu' }
      }
    },

    async addMauSac(mauSacData) {
      try {
        const response = await sanPhamService.addMauSac(mauSacData)
        if (response.error) {
          return { success: false, message: response.message || 'Thêm màu sắc thất bại' }
        }
        return { success: true, data: response }
      } catch (error) {
        console.error('Lỗi khi thêm màu sắc:', error)
        return { success: false, message: 'Có lỗi xảy ra khi thêm màu sắc' }
      }
    },

    async addKichThuoc(giaTri, donVi) {
      try {
        const response = await sanPhamService.addKichThuoc(giaTri, donVi)
        if (response.error) {
          return { success: false, message: response.message || 'Thêm kích thước thất bại' }
        }
        return { success: true, data: response }
      } catch (error) {
        console.error('Lỗi khi thêm kích thước:', error)
        return { success: false, message: 'Có lỗi xảy ra khi thêm kích thước' }
      }
    },
    ///Update thuộc tính sản phẩm
    async updateDanhMuc(danhMucData) {
      try {
        const response = await sanPhamService.updateDanhMuc(danhMucData)
        if (response.error) {
          return { success: false, message: response.message || 'Cập nhật danh mục thất bại' }
        }
        return { success: true, data: response }
      } catch (error) {
        console.error('Lỗi khi cập nhật danh mục:', error)
        return { success: false, message: 'Có lỗi xảy ra khi cập nhật danh mucch' }
      }
    },
    async updateThuongHieu(thuongHieuData) {
      try {
        const response = await sanPhamService.updateThuongHieu(thuongHieuData)
        if (response.error) {
          return { success: false, message: response.message || 'Cập nhật thương hiệu thất bại' }
        }
        return { success: true, data: response }
      } catch (error) {
        console.error('Lỗi khi cập nhật thương hiệu:', error)
        return { success: false, message: 'Có lỗi xảy ra khi cập nhật thương hiệu' }
      }
    },
    async updateChatLieu(chatLieuData) {
      try {
        const response = await sanPhamService.updateChatLieu(chatLieuData)
        if (response.error) {
          return { success: false, message: response.message || 'Cập nhật chất liệu thất bại' }
        }
        return { success: true, data: response }
      } catch (error) {
        console.error('Lỗi khi cập nhật chất liệu:', error)
        return { success: false, message: 'Có lỗi xảy ra khi cập nhật chất liệu' }
      }
    },
    async updateMauSac(mauSacData) {
      try {
        const response = await sanPhamService.updateMauSac(mauSacData)
        if (response.error) {
          return { success: false, message: response.message || 'Cập nhật màu sắc thất bại' }
        }
        return { success: true, data: response }
      } catch (error) {
        console.error('Lỗi khi cập nhật màu sắc:', error)
        return { success: false, message: 'Có lỗi xảy ra khi cập nhật màu sắc' }
      }
    },
    async updateKichThuoc(kichThuocData) {
      try {
        const response = await sanPhamService.updateKichThuoc(kichThuocData)
        if (response.error) {
          return { success: false, message: response.message || 'Cập nhật kích thước thất bại' }
        }
        return { success: true, data: response }
      } catch (error) {
        console.error('Lỗi khi cập nhật kích thước:', error)
        return { success: false, message: 'Có lỗi xảy ra khi cập nhật kích thước' }
      }
    },
    //Chuyển trạng thái thuộc tính
    async changeTrangThaiDanhMuc(id) {
      try {
        const response = await sanPhamService.changeTrangThaiDanhMuc(id)
        if (response.error) {
          toast.error('Có lỗi xảy ra')
          return
        }
        toast.success('Chuyển trạng thái thành công')
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async changeTrangThaiThuongHieu(id) {
      try {
        const response = await sanPhamService.changeTrangThaiThuongHieu(id)
        if (response.error) {
          toast.error('Có lỗi xảy ra')
          return
        }
        toast.success('Chuyển trạng thái thành công')
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async changeTrangThaiChatLieu(id) {
      try {
        const response = await sanPhamService.changeTrangThaiChatLieu(id)
        if (response.error) {
          toast.error('Có lỗi xảy ra')
          return
        }
        toast.success('Chuyển trạng thái thành công')
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async changeTrangThaiMauSac(id) {
      try {
        const response = await sanPhamService.changeTrangThaiMauSac(id)
        if (response.error) {
          toast.error('Có lỗi xảy ra')
          return
        }
        toast.success('Chuyển trạng thái thành công')
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async changeTrangThaiKichThuoc(id) {
      try {
        const response = await sanPhamService.changeTrangThaiKichThuoc(id)
        if (response.error) {
          toast.error('Có lỗi xảy ra')
          return
        }
        toast.success('Chuyển trạng thái thành công')
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async locAndTimKiemSanPhamVaChiTietSanPham(keyword, tenSanPham, giaBanMin, giaBanMax, listMauSac, listDanhMuc, listThuongHieu, listChatLieu, listKichThuoc) {
      try {
        const response = await sanPhamService.locSanPhamVaChiTietSanPham(keyword, tenSanPham, giaBanMin, giaBanMax, listMauSac, listDanhMuc, listThuongHieu, listChatLieu, listKichThuoc)
        return response
      } catch (error) {
        console.error(error)
        return null
      }
    },
    ///////////-----------------Hóa đơn-------------------////////////
    async getAllHoaDon(page = 0, size = 5) {
      try {
        const hoaDon = await hoaDonService.getAllHoaDon(page, size)
        if (hoaDon.error) {
          toast.error('Không lấy được dữ liệu')
          return
        }
        this.getAllHoaDonArr = hoaDon.content || []
        this.totalHoaDon = hoaDon.totalPages || 0
        this.currentHoaDon = page
        this.totalItemsHoaDon = hoaDon.totalElements || 0
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async filterByTrangThai(trangThai, page = 0, size = 5) {
      try {
        const response = await hoaDonService.filterByTrangThai(trangThai, page, size)
        if (response.error) {
          toast.error('Không lọc được dữ liệu')
          return
        }
        this.getAllHoaDonArr = response.content || []
        this.totalHoaDon = response.totalPages || 0
        this.currentHoaDon = page
        this.totalItemsHoaDon = response.totalElements || 0
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async locHoaDon(keyword, tuNgay, denNgay, trangThai, loaiHoaDon, page = 0, size = 5) {
      try {
        const response = await hoaDonService.locHD(keyword, tuNgay, denNgay, trangThai, loaiHoaDon, page, size);
        if (response.error) {
          throw new Error(response.message || 'Không thể lấy dữ liệu hóa đơn.');
        }
        this.getAllHoaDonArr = response.content;
        this.currentHoaDon = response.number;
        this.totalHoaDon = response.totalPages;
      } catch (error) {
        console.error('Lỗi khi tìm kiếm hóa đơn:', error);
        toast.error('Có lỗi xảy ra')
      }
    },
    async filterByDate(tuNgay, denNgay, page = 0, size = 5) {
      try {
        console.log('Từ ngày: ' + tuNgay + 'Đến ngày: ' + denNgay)
        const response = await hoaDonService.filterByDate(tuNgay, denNgay, page, size)
        console.log('Response từ getDate:', response)
        if (response.error) {
          toast.error('Không lọc được dữ liệu')
          return
        }
        this.getAllHoaDonArr = response.content || []
        this.totalHoaDon = response.totalPages || 0
        this.currentHoaDon = page
        this.totalItemsHoaDon = response.totalElements || 0
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async searchHoaDon(keyword, page = 0, size = 5) {
      try {
        const response = await hoaDonService.searchHoaDon(keyword, page, size)
        if (response.error) {
          toast.error('Không tìm kiếm được dữ liệu')
          return
        }
        this.getAllHoaDonArr = response.content || []
        this.totalHoaDon = response.totalPages || 0
        this.currentHoaDon = page
        this.totalItemsHoaDon = response.totalElements || 0
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async getHoaDonDetail(maHoaDon) {
      try {
        const response = await hoaDonService.getCTHD(maHoaDon);
        const response1 = await hoaDonService.getCTTH(maHoaDon);
        console.log('Dữ liệu từ getCTHD:', response.chiTietHoaDons); // Kiểm tra dữ liệu
        if (response.error) {
          toast.error(response.message || 'Không lấy được chi tiết hóa đơn');
          return;
        }

        this.hoaDonDetail = response.hoaDon || {};
        // Lọc trùng lặp dựa trên id_chi_tiet_san_pham
        const uniqueChiTietHoaDons = [];
        const seenIds = new Set();
        for (const item of response.chiTietHoaDons || []) {
          if (!seenIds.has(item.id_chi_tiet_san_pham)) {
            seenIds.add(item.id_chi_tiet_san_pham);
            uniqueChiTietHoaDons.push(item);
          }
        }
        this.chiTietHoaDons = uniqueChiTietHoaDons;
        this.trangThaiHistory = response.trangThaiHistory || [];
        this.chiTietTraHangs = response1.chiTietTraHangs || [];
        this.traHangs = response1.traHangs || [];
      } catch (error) {
        console.error('Lỗi trong getHoaDonDetail:', error);
        toast.error('Có lỗi xảy ra khi lấy chi tiết hóa đơn');
      }
    },
    // Thêm action để thay đổi trạng thái hóa đơn
    async changeTrangThaiHoaDon(maHoaDon, newTrangThai, nhanVienDoi, noiDungDoi) {
      try {
        const response = await hoaDonService.changeTrangThai(
          maHoaDon,
          newTrangThai,
          nhanVienDoi,
          noiDungDoi,
        )
        if (response.error) {
          toast.error('Cập nhật trạng thái thất bại')
          return
        }
        toast.success('Cập nhật trạng thái thành công')
        await this.getHoaDonDetail(maHoaDon)
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async revertToInitialStatus(maHoaDon, nhanVienDoi, noiDungDoi) {
      try {
        const response = await hoaDonService.quayLaiTrangThai(maHoaDon, nhanVienDoi, noiDungDoi)
        if (response.error) {
          toast.error('Quay lại trạng thái ban đầu thất bại')
          return
        }
        toast.success('Đã quay lại trạng thái ban đầu thành công')
        await this.getHoaDonDetail(maHoaDon)
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra khi quay lại trạng thái ban đầu')
      }
    },
    async cancelHoaDon(maHoaDon, nhanVienDoi, noiDungDoi) {
      try {
        const response = await hoaDonService.cancelHoaDon(maHoaDon, nhanVienDoi, noiDungDoi)
        if (response.error) {
          toast.error('Hủy hóa đơn thất bại')
          return
        }
        toast.success('Hủy hóa đơn thành công')
        await this.getHoaDonDetail(maHoaDon)
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async updateCustomerInfo(maHoaDon, ttkh, phiVanChuyen) {
      try {
        const nhanVienDoi = this.userDetails?.tenNhanVien || this.userInfo?.ten_dang_nhap || ''
        const response = await hoaDonService.updateTTKH_in_HD(maHoaDon, { ...ttkh, nhanVienDoi }, phiVanChuyen)
        if (response.error) {
          toast.error('Cập nhật thông tin khách hàng thất bại')
          return
        }
        toast.success('Cập nhật thông tin khách hàng thành công')
        await this.getHoaDonDetail(maHoaDon)
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra khi cập nhật thông tin khách hàng')
      }
    },
    async updateNote(maHoaDon, ghiChu) {
      try {
        const nhanVienDoi = this.userDetails?.tenNhanVien || this.userInfo?.ten_dang_nhap || ''
        const response = await hoaDonService.updateNote(maHoaDon, ghiChu, nhanVienDoi)
        if (response.error) {
          toast.error('Cập nhật ghi chú thất bại')
          return
        }
        toast.success('Cập nhật ghi chú thành công')
        await this.getHoaDonDetail(maHoaDon)
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra khi cập nhật ghi chú')
      }
    },
    async getAllCTSP_HD(page = 0, size = 5, keyword = '') {
      try {
        const response = await hoaDonService.getAllCTSP_HD(page, size, keyword)
        if (response.error) {
          toast.error('Không lấy được danh sách sản phẩm')
          return
        }
        this.listCTSP_HD = response.content || []
        this.totalPages = response.totalPages || 0
        this.currentPage = page
        this.totalItems = response.totalElements || 0
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra khi lấy danh sách sản phẩm')
      }
    },
    async addProductsToInvoice(maHoaDon, products) {
      try {
        const nhanVienDoi = this.userDetails?.tenNhanVien || this.userInfo?.ten_dang_nhap || ''
        const response = await hoaDonService.addProductsToInvoice(maHoaDon, products, nhanVienDoi)
        if (response.error) {
          toast.error('Thêm sản phẩm vào hóa đơn thất bại')
          return
        }
        toast.success('Thêm sản phẩm vào hóa đơn thành công')
        await this.getHoaDonDetail(maHoaDon)
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra khi thêm sản phẩm')
      }
    },
    async removeProductFromInvoice(maHoaDon, idCTSP) {
      try {
        const nhanVienDoi = this.userDetails?.tenNhanVien || this.userInfo?.ten_dang_nhap || ''
        const noiDungDoi = 'Xóa sản phẩm khỏi hóa đơn' // Giá trị mặc định
        const response = await hoaDonService.removeProductFromInvoice(
          maHoaDon,
          idCTSP,
          nhanVienDoi,
          noiDungDoi,
        )
        if (response.error) {
          return { error: true }
        }
        return response
      } catch (error) {
        console.error('Lỗi khi xóa sản phẩm khỏi hóa đơn:', error)
        return { error: true }
      }
    },
    async updateProductQuantity(maHoaDon, idCTSP, quantityChange) {
      try {
        const nhanVienDoi = this.userDetails?.tenNhanVien || this.userInfo?.ten_dang_nhap || ''
        const noiDungDoi = 'Update số lượng sản phẩm' // Giá trị mặc định
        const response = await hoaDonService.updateProductQuantity(
          maHoaDon,
          idCTSP,
          quantityChange,
          nhanVienDoi,
          noiDungDoi,
        )
        if (response.error) {
          return { error: true }
        }
        return response
      } catch (error) {
        console.error('Lỗi khi cập nhật số lượng sản phẩm:', error)
        return { error: true }
      }
    },
    ///////////////////////////Đăng ký tài khoản khách hàng + đăng nhập /////////////////////////////////////////
    async registerKhachHang(registerData) {
      const result = await khachHangService.registerKhachHang(registerData)
      if (result.error) {
        if (result.fieldErrors) {
          return {
            error: true,
            fieldErrors: result.fieldErrors,
          }
        }
        toast.error(result.message || 'Đăng ký tài khoản thất bại!')
        return { error: true }
      }
      toast.success(result.successMessage || 'Đăng ký tài khoản thành công!')
      return { success: true, khachHang: result.khachHang }
    },
    // Thêm action login khách hàng
    async login(loginData) {
      const result = await khachHangService.login(loginData)
      if (result.error) {
        if (result.fieldErrors) {
          return {
            error: true,
            fieldErrors: result.fieldErrors,
          }
        }
        toast.error(result.message || 'Đăng nhập thất bại!')
        return { error: true }
      }

      // Kiểm tra dữ liệu trả về từ API đăng nhập KHÁCH HÀNG
      if (!result.khachHang || !result.khachHang.email) {
        console.error('Dữ liệu khách hàng không hợp lệ:', result)
        toast.error('Dữ liệu tài khoản không hợp lệ!')
        return { error: true }
      }

      // Lưu thông tin khách hàng
      const customerData = result.khachHang
      console.log('Thông tin khách hàng:', customerData)

      // Lưu vào localStorage hoặc sessionStorage
      if (loginData.rememberMe) {
        localStorage.setItem('khachHang', JSON.stringify(customerData))
        localStorage.setItem('isLoggedIn', 'true')
      } else {
        sessionStorage.setItem('khachHang', JSON.stringify(customerData))
        sessionStorage.setItem('isLoggedIn', 'true')
      }

      // Trigger storage event for other components (like header) to update
      window.dispatchEvent(new Event('storage'))

      toast.success(result.successMessage || 'Đăng nhập thành công!')

      // Redirect to home or intended page
      if (router.currentRoute.value.query.redirect) {
        router.push(router.currentRoute.value.query.redirect)
      } else {
        router.push('/home')
      }

      return { success: true, khachHang: customerData }
    },
    // Thêm action login thằng làm thêm
    async loginNV(loginData) {
      const result = await nhanVienService.login(loginData)
      if (result.error) {
        if (result.fieldErrors) {
          return {
            error: true,
            fieldErrors: result.fieldErrors,
          }
        }
        toast.error(result.message || 'Đăng nhập thất bại!')
        return { error: true }
      }
      // Kiểm tra dữ liệu trả về từ API đăng nhập
      if (!result.taiKhoan || !result.taiKhoan.ten_dang_nhap) {
        console.error('Dữ liệu tài khoản không hợp lệ:', result)
        toast.error('Dữ liệu tài khoản không hợp lệ!')
        return { error: true }
      }
      // Lưu thông tin cơ bản
      this.userInfo = result.taiKhoan
      this.isLoggedIn = true
      this.id_roles = result.id_roles
      this.token = result.token
      // In thông tin tài khoản cơ bản
      console.log('Thông tin tài khoản (tai_khoan):', this.userInfo)
      console.log('ID Roles:', this.id_roles)
      console.log('Token:', this.token)
      // Lưu vào sessionStorage
      sessionStorage.setItem('userInfo', JSON.stringify(result.taiKhoan))
      sessionStorage.setItem('isLoggedIn', 'true')
      sessionStorage.setItem('id_roles', result.id_roles)
      sessionStorage.setItem('token', result.token)
      if (loginData.rememberMe) {
        localStorage.setItem('userInfo', JSON.stringify(result.taiKhoan))
        localStorage.setItem('isLoggedIn', 'true')
        localStorage.setItem('id_roles', result.id_roles)
        localStorage.setItem('token', result.token)
      }
      // Lấy thông tin chi tiết
      try {
        const userDetails = await khachHangService.getUserDetail({
          username: result.taiKhoan.ten_dang_nhap,
          id_roles: result.id_roles,
        })
        this.userDetails = userDetails
        // In thông tin chi tiết
        console.log('Thông tin chi tiết (userDetails):', this.userDetails)
        sessionStorage.setItem('userDetails', JSON.stringify(userDetails))
        if (loginData.rememberMe) {
          localStorage.setItem('userDetails', JSON.stringify(userDetails))
        }
      } catch (error) {
        console.error('Lỗi khi lấy thông tin chi tiết:', error)
        toast.error('Không thể lấy thông tin chi tiết tài khoản!')
      }
      toast.success(result.successMessage || 'Đăng nhập thành công!')
      return { success: true, id_roles: result.id_roles }
    },
    // Cập nhật restoreLoginState để kiểm tra cả sessionStorage
    // Khôi phục trạng thái đăng nhập từ localStorage hoặc sessionStorage
    restoreLoginState() {
      let userInfo = localStorage.getItem('userInfo')
      let isLoggedIn = localStorage.getItem('isLoggedIn')
      let id_roles = localStorage.getItem('id_roles')
      let userDetails = localStorage.getItem('userDetails')
      let token = localStorage.getItem('token')

      // Nếu không có trong localStorage, kiểm tra sessionStorage
      if (!userInfo || isLoggedIn !== 'true') {
        userInfo = sessionStorage.getItem('userInfo')
        isLoggedIn = sessionStorage.getItem('isLoggedIn')
        id_roles = sessionStorage.getItem('id_roles')
        userDetails = sessionStorage.getItem('userDetails')
        token = sessionStorage.getItem('token')
      }

      if (userInfo && isLoggedIn === 'true') {
        this.userInfo = JSON.parse(userInfo)
        this.isLoggedIn = true
        this.id_roles = id_roles ? parseInt(id_roles) : null
        this.userDetails = userDetails ? JSON.parse(userDetails) : null
        this.token = token

        // In thông tin để kiểm tra
        console.log('Khôi phục trạng thái đăng nhập:')
        console.log('User Info:', this.userInfo)
        console.log('ID Roles:', this.id_roles)
        console.log('User Details:', this.userDetails)
        console.log('Token:', this.token)
      } else {
        console.log('Không có dữ liệu đăng nhập để khôi phục.')
      }
    },

    // Đăng xuất
    logout() {
      // Xóa dữ liệu trong localStorage và sessionStorage
      localStorage.removeItem('userInfo')
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('id_roles')
      localStorage.removeItem('userDetails')
      localStorage.removeItem('token')
      sessionStorage.removeItem('userInfo')
      sessionStorage.removeItem('isLoggedIn')
      sessionStorage.removeItem('id_roles')
      sessionStorage.removeItem('userDetails')
      sessionStorage.removeItem('token')

      // Reset trạng thái trong store
      this.userInfo = null
      this.isLoggedIn = false
      this.id_roles = null
      this.userDetails = null
      this.token = null
      toast.success('Đăng xuất thành công!')
      window.location.href = '/login-register/loginAdmin'
    },
    logoutKH() {
      // Xóa dữ liệu trong localStorage và sessionStorage
      localStorage.removeItem('userInfo')
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('id_roles')
      localStorage.removeItem('userDetails')
      localStorage.removeItem('token')
      sessionStorage.removeItem('userInfo')
      sessionStorage.removeItem('isLoggedIn')
      sessionStorage.removeItem('id_roles')
      sessionStorage.removeItem('userDetails')
      sessionStorage.removeItem('token')

      // Reset trạng thái trong store
      this.userInfo = null
      this.isLoggedIn = false
      this.id_roles = null
      this.userDetails = null
      this.token = null

      // const router = useRouter();
      // router.replace('/login-register/login');
      toast.success('Đăng xuất thành công!')
      window.location.href = '/home'
    },
    //Import excel
    async importExcel(file) {
      try {
        console.log('Store processing file:', file)
        console.log('File type:', file.type)
        console.log('File name:', file.name)

        // If we have a file from Ant Design Upload, make sure we're using the right reference
        const fileToSend = file.originFileObj || file

        const importExcelRespone = await sanPhamService.importSanPhamFromExcel(fileToSend)

        if (importExcelRespone.error) {
          toast.error('Không lấy được dữ liệu')
          return importExcelRespone // Return the error response so we can handle it
        } else {
          toast.success('Import dữ liệu thành công')
        }
        return importExcelRespone
      } catch (error) {
        console.error('Error in importExcel store method:', error)
        toast.error('Lỗi khi xử lý file Excel')
        throw error // Rethrow to let the component handle it
      }
    },

    //Save excel
    async saveExcelImport(data) {
      const saveExcelImportRespone = await sanPhamService.saveExcelImports(data)
      if (saveExcelImportRespone.error) {
        toast.error('Không thể lưu dữ liệu')
        return
      } else {
        toast.success('Lưu dữ liệu thành công')
      }
      return saveExcelImportRespone
    },

    // Xuất Excel với các tùy chọn
    async exportExcel(productIds, fields) {
      try {
        console.log('Đang xuất Excel cho sản phẩm:', productIds)
        console.log('Các trường được chọn:', fields)

        const blob = await sanPhamService.exportExcel(productIds, fields)

        // Tạo URL cho blob và tải xuống
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `san-pham-export-${new Date().getTime()}.xlsx`)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)

        toast.success('Xuất Excel thành công')
        return true
      } catch (error) {
        console.error('Lỗi khi xuất Excel:', error)
        toast.error('Không thể xuất Excel')
        return false
      }
    },
    //Kết thúc Xuất Excel
    //Khách hàng
    // Lấy danh sách khách hàng

    async themKhachHangBH(khachHangData) {
      try {
        console.log('API themKhachHangBH input:', khachHangData);
        const response = await khachHangService.themKhachHangBH(khachHangData);
        console.log('API themKhachHangBH response:', response);
        if (response.error) {
          const errorMessage = typeof response.error === 'string' ? response.error : 'Có lỗi không xác định từ server';
          throw new Error(errorMessage);
        }
        // Lưu khách hàng vừa tạo để sử dụng sau này
        if (response.idKhachHang) {
          this.lastCreatedKhachHang = {
            idKhachHang: response.idKhachHang,
            tenKhachHang: khachHangData.tenKhachHang,
            soDienThoai: khachHangData.soDienThoai,
            email: khachHangData.email
          };
        }
        await this.getAllKhachHang(this.currentKhachHang, 10000);
        return response.khachHang;
      } catch (error) {
        console.error('Lỗi trong themKhachHangBH:', error);
        console.log('Error response:', error.response);
        console.log('Error message:', error.message);
        throw error; // Truyền lỗi lên để frontend xử lý
      }
    },

    getLatestKhachHang() {
      return this.lastCreatedKhachHang;
    },

    // Lấy danh sách khách hàng
    async getAllKhachHang(page = 0, size = 5, keyword = null, trangThai = null, updatedId = null) {
      try {
        const khachHang = await khachHangService.getAllKhachHang(
          page,
          size,
          keyword,
          trangThai,
          updatedId,
        )
        if (khachHang.error) {
          this.getAllKhachHangArr = []
          this.diaChiMap = {}
          this.totalKhachHang = 0
          this.currentKhachHang = 0
          this.totalItemsKhachHang = 0
          return
        }
        if (!khachHang.danhSachKhachHang || khachHang.danhSachKhachHang.length === 0) {
          this.getAllKhachHangArr = []
          this.diaChiMap = {}
          this.totalKhachHang = 0
          this.currentKhachHang = 0
          this.totalItemsKhachHang = 0
          return
        }

        this.getAllKhachHangArr = khachHang.danhSachKhachHang || []
        this.diaChiMap = khachHang.diaChiMap || {}
        this.totalKhachHang = khachHang.totalPages || 0
        this.currentKhachHang = page
        this.totalItemsKhachHang = khachHang.totalElements || 0
      } catch (error) {
        console.error('Lỗi khi lấy danh sách khách hàng:', error)
        toast.error('Có lỗi xảy ra khi lấy danh sách khách hàng')
        this.getAllKhachHangArr = []
        this.diaChiMap = {}
        this.totalKhachHang = 0
        this.currentKhachHang = 0
        this.totalItemsKhachHang = 0
      }
    },

    // Thêm khách hàng
    async themKhachHang(khachHangData) {
      const response = await khachHangService.themKhachHang(khachHangData)
      if (response.error) {
        throw new Error(response.message || 'Có lỗi xảy ra khi thêm khách hàng') // Ném lỗi để component xử lý
      }
      await this.getAllKhachHang(this.currentKhachHang, 5) // Làm mới danh sách
      return response.khachHang // Trả về thông tin khách hàng vừa thêm
    },

    // Lấy thông tin khách hàng để chỉnh sửa
    async getKhachHangByIdForEdit(id) {
      try {
        const response = await khachHangService.getKhachHangByIdForEdit(id)
        if (response.error) {
          toast.error(response.message || 'Không lấy được thông tin khách hàng')
          return null
        }
        return response.khachHang
      } catch (error) {
        console.error('Lỗi khi lấy thông tin khách hàng:', error)
        toast.error('Có lỗi xảy ra khi lấy thông tin khách hàng')
        return null
      }
    },

    async suaKhachHang(khachHangData) {
      try {
        console.log('Sending data to update customer:', khachHangData)

        // Make a copy of the data to avoid modifying the original
        const dataToSend = { ...khachHangData }

        // Format the date if it's not already a string
        if (dataToSend.ngaySinh && typeof dataToSend.ngaySinh !== 'string') {
          dataToSend.ngaySinh = new Date(dataToSend.ngaySinh).toISOString()
        }

        const response = await khachHangService.suaKhachHang(dataToSend)

        if (response.error) {
          console.error('Error response from update API:', response)
          return false
        }

        // Update user details in store if update is for the current logged-in user
        if (this.userDetails && this.userDetails.idKhachHang === khachHangData.idKhachHang) {
          // Make shallow copy of userDetails
          const updatedUserDetails = { ...this.userDetails }

          // Update relevant fields
          updatedUserDetails.tenKhachHang = khachHangData.tenKhachHang
          updatedUserDetails.soDienThoai = khachHangData.soDienThoai
          updatedUserDetails.ngaySinh = khachHangData.ngaySinh
          updatedUserDetails.gioiTinh = khachHangData.gioiTinh

          // Update the store
          this.userDetails = updatedUserDetails

          // Update localStorage or sessionStorage based on remember me setting
          if (localStorage.getItem('isLoggedIn') === 'true') {
            localStorage.setItem('userDetails', JSON.stringify(updatedUserDetails))
          } else if (sessionStorage.getItem('isLoggedIn') === 'true') {
            sessionStorage.setItem('userDetails', JSON.stringify(updatedUserDetails))
          }

          console.log('Updated user details in store:', this.userDetails)
        }

        // // Refresh the customer list if we're on the admin page
        // await this.getAllKhachHang(
        //   0,
        //   3,
        //   this.searchs,
        //   this.selectedTrangThai,
        //   khachHangData.idKhachHang,
        // )

        this.successMessage = 'Cập nhật khách hàng thành công!'
        return true
      } catch (error) {
        console.error('Error updating customer:', error)
        return false
      }
    },

    // Lấy chi tiết khách hàng
    async getKhachHangDetail(id) {
      try {
        const response = await khachHangService.getKhachHangDetail(id)
        if (response.error) {
          toast.error(response.message || 'Không lấy được chi tiết khách hàng')
          return null
        }
        return {
          khachHang: response.khachHang,
          diaChiList: response.diaChiList, // Trả về danh sách địa chỉ đầy đủ
          matKhau: response.matKhau,
        } // Trả về chi tiết khách hàng
      } catch (error) {
        console.error('Lỗi khi lấy chi tiết khách hàng:', error)
        toast.error('Có lỗi xảy ra khi lấy chi tiết khách hàng')
        return null
      }
    },

    // Thay đổi trạng thái khách hàng
    async changeTrangThaiKhachHang(idKhachHang) {
      try {
        // Cập nhật UI trước khi gọi API
        const khachHang = this.getAllKhachHangArr.find((kh) => kh.idKhachHang === idKhachHang)
        if (khachHang) {
          khachHang.trangThai =
            khachHang.trangThai === true ? false : true
        }

        const response = await khachHangService.changeTrangThai(idKhachHang)
        if (response.error) {
          toast.error(response.message || 'Có lỗi xảy ra khi thay đổi trạng thái')
          // Revert trạng thái nếu lỗi
          if (khachHang) {
            khachHang.trangThai =
              khachHang.trangThai === true ? false : true
          }
          return false
        }

        return true
      } catch (error) {
        console.error('Lỗi khi thay đổi trạng thái khách hàng:', error)
        toast.error('Có lỗi xảy ra khi thay đổi trạng thái')
        // Revert trạng thái nếu lỗi
        const khachHang = this.getAllKhachHangArr.find((kh) => kh.idKhachHang === idKhachHang)
        if (khachHang) {
          khachHang.trangThai =
            khachHang.trangThai === true ? false : true
        }
        return false
      }
    },

    async getAllKhachHangNoPage() {
      try {
        const response = await khachHangService.getAllKhachHangNoPage()
        if (response.error) {
          toast.error(response.message || 'Không lấy được dữ liệu khách hàng')
          return null
        }

        this.getAllKhachHangNoPageList = response.danhSachKhachHang
        this.diaChiMap = response.diaChiMap
      } catch (error) {
        console.error('Lỗi khi lấy dữ liệu khách hàng no page:', error)
        toast.error('Có lỗi xảy ra khi lấy dữ liệu khách hàng no page')
        return null
      }
    },

    ///=======Code bán hàng tại quầy=======///
    async getAllCTSPKM() {
      try {
        const response = await sanPhamService.getAllCTSPKM()
        if (response.error) {
          toast.error('Không lấy được dữ liệu chi tiết sản phẩm khuyến mãi')
          this.getAllCTSPKMList = []
          return []
        }
        this.getAllCTSPKMList = response
        return response
      } catch (error) {
        console.log(error)
        this.getAllCTSPKMList = []
        return []
      }
    },
    async getAllHoaDonCTT() {
      try {
        const hoaDonCTT = await banHangService.getAllHoaDonCTT()
        if (hoaDonCTT.error) {
          toast.error('Không lấy được dữ liệu')
          return
        }
        this.getAllHoaDonCTTArr = hoaDonCTT
      } catch (error) {
        console.log(error)
        this.getAllHoaDonCTTArr = []
      }
    },
    async createHoaDon() {
      try {
        const hoaDon = await banHangService.createHoaDon()
        if (hoaDon.error) {
          toast.error('Không thêm được hoá đơn')
          return
        }
        console.log('hoaDon đã thêm', hoaDon)
        return hoaDon
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
        throw error
      }
    },
    async updateHoaDon(payload) {
      try {
        const result = await banHangService.updateHoaDon(payload);
        if (result.error) {
          toast.error(result.message || 'Không thể cập nhật hoá đơn');
          return null;
        }
        return result;
      } catch (error) {
        console.error('Lỗi store updateHoaDon:', error);
        toast.error('Có lỗi xảy ra khi cập nhật hoá đơn');
        throw error;
      }
    },
    // Lấy danh sách voucher phù hợp với tổng tiền
    async getSuitableVouchersForInvoice(tongTien) {
      try {
        console.log('Đang lấy voucher phù hợp với tổng tiền:', tongTien);
        const vouchers = await banHangService.getSuitableVouchers(tongTien);
        return vouchers || [];
      } catch (error) {
        console.error('Lỗi khi lấy voucher phù hợp:', error);
        toast.error('Có lỗi xảy ra khi lấy danh sách voucher');
        return [];
      }
    },
    // Áp dụng voucher cho hóa đơn
    async applyVoucherToInvoice(idHoaDon, idVoucher) {
      try {
        console.log('Đang áp dụng voucher:', { idHoaDon, idVoucher });
        const result = await banHangService.applyVoucher(idHoaDon, idVoucher);
        if (result.error) {
          toast.error(result.message || 'Không thể áp dụng voucher');
          return null;
        }
        toast.success(idVoucher ? 'Áp dụng voucher thành công' : 'Đã bỏ voucher');
        return result;
      } catch (error) {
        console.error('Lỗi khi áp dụng voucher:', error);
        toast.error('Có lỗi xảy ra khi áp dụng voucher');
        return null;
      }
    },
    async deleteHoaDon(idHoaDon) {
      try {
        const hoaDon = await banHangService.deleteHoaDon(idHoaDon)
        if (!hoaDon.success) {
          // Kiểm tra success thay vì error
          toast.error(hoaDon.message || 'Không xóa được hoá đơn')
          return { error: true, message: hoaDon.message }
        }
        return hoaDon
      } catch (error) {
        const errorMessage = error.response?.data?.message || 'Có lỗi xảy ra'
        console.error('Lỗi khi xóa hóa đơn:', error)
        toast.error(errorMessage)
        throw error
      }
    },
    async getAllSPHD(idHoaDon) {
      try {
        const hoaDon = await banHangService.getAllSPHD(idHoaDon)
        if (hoaDon.error) {
          toast.error('Không lấy được dữ liệu')
          return
        }
        this.getAllSPHDArr = hoaDon
      } catch (error) {
        console.error(error)
        toast.error('Có lị xảy ra')
        throw error
      }
    },
    async addSPHD(idHoaDon, idCTSP, soLuong) {
      try {
        const hoaDon = await banHangService.addSPHD(idHoaDon, idCTSP, soLuong)
        if (hoaDon.error) {
          toast.error('Không thêm được sản phẩm vào hoá đơn')
          return
        }
        return hoaDon
      } catch (error) {
        console.error(error)
        toast.error('Có lị xảy ra')
        throw error
      }
    },
    async themSPHDMoi(idHoaDon, idCTSP, soLuong) {
      try {
        const result = await banHangService.themSPHDMoi(idHoaDon, idCTSP, soLuong)
        if (result.error) {
          toast.error(result.message || 'Không thêm được sản phẩm vào hoá đơn')
          return null
        }
        return result
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra khi thêm sản phẩm')
        throw error
      }
    },
    async trangThaiDonHang(idHoaDon) {
      try {
        const hoaDon = await banHangService.trangThaiDonHang(idHoaDon)
        if (hoaDon.error) {
          toast.error('Không lấy được dữ liệu')
          return
        }
        return hoaDon
      } catch (error) {
        console.error(error)
        toast.error('Có lị xảy ra')
        throw error
      }
    },
    async phuongThucNhanHang(idHoaDon, phuongThucNhanHang) {
      try {
        const hoaDon = await banHangService.phuongThucNhanHang(idHoaDon, phuongThucNhanHang)
        if (hoaDon.error) {
          toast.error('Không lấy được dữ liệu')
          return
        }
        return hoaDon
      } catch (error) {
        console.error(error)
        toast.error('Có lị xảy ra')
        throw error
      }
    },
    async giamSPHD(idHoaDon, idCTSP, soLuong) {
      try {
        const hoaDon = await banHangService.giamSPHD(idHoaDon, idCTSP, soLuong)
        if (hoaDon.error) {
          toast.error('Không giảm được sản phẩm hoá đơn')
          return
        }
        return hoaDon
      } catch (error) {
        console.error(error)
        toast.error('Có lị xảy ra')
        throw error
      }
    },
    async getListHoaDon() {
      try {
        const listHD = await hoaDonService.getListHoaDon()
        if (listHD.error) {
          toast.error('Không lấy được dữ liệu')
          return
        }
        this.getListHoaDonAll = listHD
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },

    async xoaSPHD(idHoaDon, idCTSP) {
      try {
        const result = await banHangService.xoaSPHD(idHoaDon, idCTSP)
        return result // ✨ Trả luôn response object { success, message }
      } catch (error) {
        console.error('Lỗi từ API:', error.response?.data || error.message)
        return {
          success: false,
          message: error.response?.data?.message || 'Có lỗi xảy ra khi xóa sản phẩm',
        }
      }
    },

    async getHoaDonByIdHoaDon(idHD) {
      try {
        const hoaDon = await banHangService.getHoaDonByIdHoaDon(idHD)
        if (hoaDon.error) {
          toast.error('Không lấy được dữ liệu hoa đơn bai id hoá đơn')
          return
        }
        this.getHDBIDHD = hoaDon
      } catch (error) {
        console.error(error)
        toast.error('Có lị xảy ra')
        throw error
      }
    },

    async addKHHD(idHoaDon, idKhachHang, diaChi, tenKhachHang, sdt, email) {
      try {
        const result = await banHangService.addKhHD(
          idHoaDon,
          idKhachHang,
          diaChi,
          tenKhachHang,
          sdt,
          email,
        )
        if (result.error) {
          toast.error(result.message || 'Không thêm được khách hàng hoá đơn')
          return null
        }
        return result
      } catch (error) {
        console.error(error)
        toast.error('Có lị xảy ra')
        throw error
      }
    },

    async setTrangThaiNhanHang(idHoaDon, phuongThucNhanHang, phiVanChuyen) {
      try {
        const result = await banHangService.setTrangThaiNhanHang(idHoaDon, phuongThucNhanHang, phiVanChuyen)
        if (result.error) {
          toast.error(result.message || 'Không thêm được khách hàng hoá đơn')
          return null
        }
        return result
      } catch (error) {
        console.error(error)
        toast.error('Có lị xảy ra')
        throw error
      }
    },

    async thanhToanMomo(idHoaDon) {
      try {
        const result = await banHangService.thanhToanMomo(idHoaDon)
        if (result.error) {
          toast.error(result.message || 'Không thêm được khách hàng hoá đơn')
          return null
        }
        return result
      } catch (error) {
        console.error(error)
        toast.error('Có lị xảy ra')
        throw error
      }
    },

    async updateHinhThucTTHoaDon(idHD, hinhThucThanhToan) {
      try {
        const result = await hoaDonService.updateHinhThucTTHoaDon(idHD, hinhThucThanhToan)
        if (result.error) {
          toast.error(result.message || 'Không update được hình thức thanh toán')
          return null
        }
        return result
      } catch (error) {
        console.error(error)
        toast.error('Có lị xảy ra')
        throw error
      }
    },

    async setSPHD(idHoaDon, idCTSP, soLuong) {
      try {
        const result = await banHangService.setSPHD(idHoaDon, idCTSP, soLuong)
        if (result.error) {
          toast.error(result.message || 'Không thêm được khách hàng hoá đơn')
          return null
        }
        return result
      } catch (error) {
        console.error(error)
        toast.error('Có lị xảy ra')
        throw error
      }
    },

    /////

    //================= Action cho voucher =================///
    async getAllVouchers(page = 0, size = 5) {
      try {
        const vouchers = await voucherService.getAllVouchers(page, size)
        if (vouchers.error) {
          toast.error('Không lấy được dữ liệu voucher')
          return
        }
        this.getAllVoucherArr = vouchers.content || []
        this.voucherTotalPages = vouchers.totalPages || 0
        this.voucherCurrentPage = page
        this.voucherTotalItems = vouchers.totalElements || 0
      } catch (error) {
        console.error('Lỗi khi lấy danh sách voucher:', error)
        toast.error('Có lỗi xảy ra')
      }
    },

    async getVoucherLocTrangThai(page = 0, size = 5, trangThai) {
      try {
        const voucherFilter = await voucherService.getVoucherLocTrangThai(page, size, trangThai)
        if (voucherFilter.error) {
          toast.error('Không lấy được dữ liệu voucher')
          return
        }
        this.getAllVoucherArr = voucherFilter.content || []
        this.voucherTotalPages = voucherFilter.totalPages || 0
        this.voucherCurrentPage = page
        this.voucherTotalItems = voucherFilter.totalElements || 0
      } catch (error) {
        console.error('Lỗi khi lọc voucher theo trạng thái:', error)
        toast.error('Có lỗi xảy ra')
      }
    },

    async searchVoucher(keyword, page = 0, size = 5) {
      try {
        const voucherSearch = await voucherService.searchVoucher(keyword, page, size)
        if (voucherSearch.error) {
          toast.error('Không tìm thấy voucher')
          this.voucherSearch = []
          this.voucherTotalPages = 0
          this.voucherCurrentPage = 0
          this.voucherTotalItems = 0
        } else {
          this.voucherSearch = voucherSearch.content || []
          this.voucherTotalPages = voucherSearch.totalPages || 0
          this.voucherCurrentPage = page
          this.voucherTotalItems = voucherSearch.totalElements || 0
        }
      } catch (error) {
        console.error('Lỗi khi tìm kiếm voucher:', error)
        toast.error('Có lỗi xảy ra')
      }
    },

    async offVoucher(id) {
      try {
        const voucher = this.getAllVoucherArr.find((v) => v.id === id)
        if (voucher) {
          voucher.trangThai = voucher.trangThai === 'Đang diễn ra' ? 'Đã kết thúc' : 'Đang diễn ra'
        }
        const response = await voucherService.offVoucher(id)
        if (response.error) {
          toast.error('Không thể tắt voucher')
          voucher.trangThai = voucher.trangThai === 'Đang diễn ra' ? 'Đã kết thúc' : 'Đang diễn ra'
        } else {
          toast.success('Đã tắt voucher thành công')
        }
      } catch (error) {
        console.error('Lỗi khi tắt voucher:', error)
        toast.error('Có lỗi xảy ra')
      }
    },

    async timKiemVoucherByPrice(minPrice, maxPrice, page = 0, size = 5) {
      try {
        const voucherFilter = await voucherService.timKiemVoucherByPrice(
          minPrice,
          maxPrice,
          page,
          size,
        )
        if (voucherFilter.error) {
          toast.error('Không tìm thấy voucher theo giá')
          return
        }
        this.getAllVoucherArr = voucherFilter.content || []
        this.voucherTotalPages = voucherFilter.totalPages || 0
        this.voucherCurrentPage = page
        this.voucherTotalItems = voucherFilter.totalElements || 0
      } catch (error) {
        console.error('Lỗi khi lọc voucher theo giá:', error)
        toast.error('Có lỗi xảy ra')
      }
    },

    async timKiemVoucherByDate(startDate, endDate, page = 0, size = 5) {
      try {
        const voucherFilter = await voucherService.timKiemVoucherByDate(
          startDate,
          endDate,
          page,
          size,
        )
        if (voucherFilter.error) {
          toast.error('Không tìm thấy voucher theo ngày')
          return
        }
        this.getAllVoucherArr = voucherFilter.content || []
        this.voucherTotalPages = voucherFilter.totalPages || 0
        this.voucherCurrentPage = page
        this.voucherTotalItems = voucherFilter.totalElements || 0
      } catch (error) {
        console.error('Lỗi khi lọc voucher theo ngày:', error)
        toast.error('Có lỗi xảy ra')
      }
    },
    async addVoucher(voucherData) {
      try {
        const response = await voucherService.addVoucher(voucherData)
        return response // Trả về chuỗi từ backend
      } catch (error) {
        console.error('Lỗi khi thêm voucher:', error)
        throw error // Để frontend xử lý lỗi
      }
    },
    async updateVoucher(voucherData) {
      try {
        const response = await voucherService.updateVoucher(voucherData)
        return response
      } catch (error) {
        console.error('Lỗi khi cập nhật voucher:', error)
        throw error
      }
    },

    async getVoucherById(id) {
      try {
        const response = await voucherService.getVoucherById(id)
        return response
      } catch (error) {
        console.error('Lỗi khi lấy chi tiết voucher:', error)
        throw error
      }
    },

    // Action cho khuyến mãi
    async getAllKhuyenMai(page = 0, size = 5) {
      try {
        const khuyenMai = await khuyenMaiService.getAllKhuyenMai(page, size)
        if (khuyenMai.error) {
          toast.error('Không lấy được dữ liệu khuyến mãi')
          return
        }
        this.getAllKhuyenMaiArr = khuyenMai.content || []
        this.khuyenMaiTotalPages = khuyenMai.totalPages || 0
        this.khuyenMaiCurrentPage = page
        this.khuyenMaiTotalItems = khuyenMai.totalElements || 0
      } catch (error) {
        console.error('Lỗi khi lấy danh sách khuyến mãi:', error)
        toast.error('Có lỗi xảy ra')
      }
    },

    async getKhuyenMaiLocTrangThai(page = 0, size = 5, trangThai) {
      try {
        const khuyenMaiFilter = await khuyenMaiService.getKhuyenMaiLocTrangThai(
          page,
          size,
          trangThai,
        )
        if (khuyenMaiFilter.error) {
          toast.error('Không lấy được dữ liệu')
          return
        }
        this.getAllKhuyenMaiArr = khuyenMaiFilter.content || []
        this.khuyenMaiTotalPages = khuyenMaiFilter.totalPages || 0
        this.khuyenMaiCurrentPage = page
        this.khuyenMaiTotalItems = khuyenMaiFilter.totalElements || 0
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },

    async offKhuyenMai(id) {
      try {
        const khuyenMai = this.getAllKhuyenMaiArr.find((km) => km.id === id)
        if (khuyenMai) {
          khuyenMai.trangThai =
            khuyenMai.trangThai === 'Đang diễn ra' ? 'Đã kết thúc' : 'Đang diễn ra'
        }
        const response = await khuyenMaiService.offKhuyenMai(id)
        if (response.error) {
          toast.error('Không thể tắt khuyến mãi')
          khuyenMai.trangThai =
            khuyenMai.trangThai === 'Đang diễn ra' ? 'Đã kết thúc' : 'Đang diễn ra'
        } else {
          toast.success('Đã tắt khuyến mãi thành công')
        }
      } catch (error) {
        console.error(error)
        toast.error('Có lỗi xảy ra')
      }
    },

    async timKiemKhuyenMaiByPrice(minPrice, maxPrice, page = 0, size = 5) {
      try {
        const khuyenMaiFilter = await khuyenMaiService.timKiemKhuyenMaiByPrice(
          minPrice,
          maxPrice,
          page,
          size,
        )
        if (khuyenMaiFilter.error) {
          toast.error('Không tìm thấy khuyến mãi theo giá')
          return
        }
        this.getAllKhuyenMaiArr = khuyenMaiFilter.content || []
        this.khuyenMaiTotalPages = khuyenMaiFilter.totalPages || 0
        this.khuyenMaiCurrentPage = page
        this.khuyenMaiTotalItems = khuyenMaiFilter.totalElements || 0
      } catch (error) {
        console.error('Lỗi khi lọc theo giá:', error)
        toast.error('Có lỗi xảy ra')
      }
    },

    async timKiemKhuyenMaiByDate(startDate, endDate, page = 0, size = 5) {
      try {
        const khuyenMaiFilter = await khuyenMaiService.timKiemKhuyenMaiByDate(
          startDate,
          endDate,
          page,
          size,
        )
        if (khuyenMaiFilter.error) {
          toast.error('Không tìm thấy khuyến mãi theo ngày')
          return
        }
        this.getAllKhuyenMaiArr = khuyenMaiFilter.content || []
        this.khuyenMaiTotalPages = khuyenMaiFilter.totalPages || 0
        this.khuyenMaiCurrentPage = page
        this.khuyenMaiTotalItems = khuyenMaiFilter.totalElements || 0
      } catch (error) {
        console.error('Lỗi khi lọc theo ngày:', error)
        toast.error('Có lỗi xảy ra')
      }
    },

    setCheckoutItems(items) {
      this.checkoutItems = Array.isArray(items) ? items : [items]
      console.log('checkoutItems', this.checkoutItems)
    },

    clearCheckoutItems() {
      this.checkoutItems = []
    },

    // Action tìm kiếm sản phẩm (tùy chọn nếu muốn tích hợp vào store)
    async searchSanPhamKM(keyword) {
      try {
        const response = await khuyenMaiService.searchSanPhamKM(keyword)
        if (response && !response.error) {
          this.searchSanPham = response
          return response
        } else {
          toast.error('Không tìm thấy sản phẩm')
          this.searchSanPham = []
          return []
        }
      } catch (error) {
        console.error('Lỗi khi tìm kiếm sản phẩm:', error)
        toast.error('Có lỗi xảy ra')
        return []
      }
    },

    // Action lấy chi tiết sản phẩm theo sản phẩm
    async getChiTietSanPhamBySanPham(idSanPham) {
      try {
        const response = await khuyenMaiService.getChiTietSanPhamBySanPham(idSanPham)
        if (response && !response.error) {
          return response
        } else {
          toast.error('Không lấy được chi tiết sản phẩm')
          return []
        }
      } catch (error) {
        console.error('Lỗi khi lấy chi tiết sản phẩm:', error)
        toast.error('Có lỗi xảy ra')
        return []
      }
    },
    async getKhuyenMaiById(id) {
      if (!id || id <= 0) {
        toast.error('ID khuyến mãi không hợp lệ')
        return null
      }

      try {
        const response = await khuyenMaiService.getKhuyenMaiById(id)
        if (response.error) {
          toast.error(response.message || 'Không lấy được chi tiết khuyến mãi')
          return null
        }
        return response
      } catch (error) {
        console.error('Lỗi khi lấy chi tiết khuyến mãi:', error)
        toast.error(error.response?.data?.message || 'Có lỗi xảy ra khi tải chi tiết khuyến mãi')
        return null
      }
    },
    async addKhuyenMai(khuyenMaiData, selectedChiTietSanPhamIds) {
      try {
        const response = await khuyenMaiService.addKhuyenMai(
          khuyenMaiData,
          selectedChiTietSanPhamIds,
        )
        if (response === 'Thêm khuyến mãi thành công!') {
          toast.success(response)
          await this.getAllKhuyenMai(0, 5) // Làm mới danh sách khuyến mãi
          return true
        } else {
          toast.error(response)
          return false
        }
      } catch (error) {
        console.error('Lỗi khi thêm khuyến mãi:', error)
        toast.error('Có lỗi xảy ra khi thêm khuyến mãi')
        return false
      }
    },

    async updateKhuyenMai(khuyenMaiData, selectedChiTietSanPhamIds) {
      try {
        const response = await khuyenMaiService.updateKhuyenMai(
          khuyenMaiData,
          selectedChiTietSanPhamIds,
        )
        if (response === 'Cập nhật khuyến mãi thành công!') {
          toast.success(response)
          await this.getAllKhuyenMai(0, 5) // Làm mới danh sách
          return true
        } else {
          toast.error(response)
          return false
        }
      } catch (error) {
        console.error('Lỗi khi cập nhật khuyến mãi:', error)
        toast.error('Có lỗi xảy ra khi cập nhật khuyến mãi')
        return false
      }
    },
    //================= Kết thúc khuyến mãi =================///
    //------------------- Bán hàng online -------------------//
    async getVoucherByGiaTruyen(giaTruyen) {
      try {
        // Đảm bảo giaTruyen là số
        const giaTriSo = Number(giaTruyen)
        console.log('Giá trị truyền vào API:', giaTriSo)
        const response = await banHangOnlineService.voucherByGiaTruyen(giaTriSo)
        return response
      } catch (error) {
        console.error('Lỗi khi lấy voucher theo giá truyền:', error)
        toast.error('Có lỗi xảy ra khi lấy voucher theo giá truyền')
        return []
      }
    },
    getPath(path) {
      this.checkRouter = '';
      // Ensure consistent path format (always with leading slash)
      if (path) {
        this.checkRouter = path.startsWith('/') ? path : '/' + path
        console.log('Path set in store:', this.checkRouter)
      }
    },
    getRoutePresent(path) {
      this.checkRoutePresent = ''
      this.checkRoutePresent = path
    },
    getIndex(path) {
      this.indexMenu = ['1']
      switch (path) {
        case '/admin':
          this.indexMenu = ['1']
          break
        case '/admin/quanlysanpham':
          this.indexMenu = ['3']
          break
        case '/admin/quanlynhanvien':
          this.indexMenu = ['10']
          break
        case '/admin/quanlyhoadon':
          this.indexMenu = ['8']
          break
        case '/admin/quanlysanpham/add':
          this.indexMenu = ['3']
          break
        case '/admin/quanlysanpham/sua':
          this.indexMenu = ['3']
          break
        case '/admin/quanlykhachhang':
          this.indexMenu = ['11']
          break
        case '/admin/banhang':
          this.indexMenu = ['2']
          break
        case '/admin/quanlyvoucher':
          this.indexMenu = ['12']
          break
        case '/admin/quanlykhuyenmai':
          this.indexMenu = ['13']
          break
        case '/admin/quanlysanpham/sanpham':
          this.indexMenu = ['4']
          break
        case '/admin/quanlysanpham/thuoctinh':
          this.indexMenu = ['7']
          break
        case '/admin/quanlysanpham/chitietsanpham':
          this.indexMenu = ['5']
          break
        case '/admin/traHang':
          this.indexMenu = ['9']
          break
        default:
          this.indexMenu = ['1']
          break
      }
    },

    // Lấy ảnh sản phẩm

    async getImage(id, anhChinh) {
      const getImageRespone = await sanPhamService.getImageInCTSP(id, anhChinh)
      if (getImageRespone.error) {
        toast.error('Không lấy được dữ liệu')
        return
      } else {
        this.getImages = getImageRespone
      }
      return getImageRespone
    },
    //Lấy danh sách chi tiết sản phẩm theo sản phẩm
    async getCTSPBySanPham(id) {
      try {
        const getCTSPBySanPhamRespone = await sanPhamService.getCTSPBySanPham(id)
        if (getCTSPBySanPhamRespone.error) {
          toast.error('Không lấy được dữ liệu chi tiết sản phẩm')
          return
        } else {
          // Lấy hình ảnh cho từng chi tiết sản phẩm trong một lần gọi
          for (let i = 0; i < getCTSPBySanPhamRespone.length; i++) {
            const ctsp = getCTSPBySanPhamRespone[i]
            // Lấy TẤT CẢ hình ảnh (anhChinh = "")
            const images = await sanPhamService.getImageInCTSP(ctsp.id_chi_tiet_san_pham, "")
            console.log('Images for CTSP ' + ctsp.id_chi_tiet_san_pham, images);

            if (images && Array.isArray(images) && images.length > 0) {
              // Thêm danh sách hình ảnh đầy đủ vào chi tiết sản phẩm
              ctsp.hinh_anh_list = images
            } else {
              ctsp.hinh_anh_list = []
            }
          }
          this.getCTSPBySanPhams = getCTSPBySanPhamRespone
          return getCTSPBySanPhamRespone;

        }
      } catch (error) {
        console.error('Lỗi khi lấy chi tiết sản phẩm và hình ảnh:', error)
        toast.error('Có lỗi xảy ra khi lấy dữ liệu')
      }
    },
    //Lấy danh sách sản phẩm
    async getAllSP() {
      try {
        console.log('Đang tải danh sách sản phẩm')
        const sanPhamResponse = await sanPhamService.getAllSanPham()

        if (!sanPhamResponse || sanPhamResponse.error) {
          console.error('Không lấy được dữ liệu sản phẩm')
          this.getAllSanPham = []
          return []
        }

        console.log('Đã tải', sanPhamResponse.length, 'sản phẩm')
        this.getAllSanPham = sanPhamResponse

        // Reset trạng thái tìm kiếm và lọc nếu đang tải lại dữ liệu
        if (this.isSearching || this.isFiltering) {
          console.log('Tự động reset trạng thái tìm kiếm và lọc khi tải lại dữ liệu')
          this.resetSearch()
          this.resetFilter()
        }

        return sanPhamResponse
      } catch (error) {
        console.error('Lỗi khi tải danh sách sản phẩm:', error)
        this.getAllSanPham = []
        return []
      }
    },
    //Thêm sản phẩm
    async createSanPham(sanPhamData) {
      try {
        console.log('Data gửi đi:', sanPhamData)
        const response = await sanPhamService.createSanPhams(sanPhamData)
        console.log('Response từ service:', response)
        return response
      } catch (error) {
        console.error('Lỗi trong createSanPham:', error)
        throw error
      }
    },
    //Thêm chi tiết sản phẩm
    async createCTSP(CTSPData) {
      try {
        // Đảm bảo dữ liệu đúng định dạng trước khi gửi
        const formattedData = {
          ...CTSPData,
          // Đảm bảo id_chi_tiet_san_pham là null nếu không có
          id_chi_tiet_san_pham: CTSPData.id_chi_tiet_san_pham || null,
          // Đảm bảo gia_ban là số
          gia_ban: Number(CTSPData.gia_ban),
          // Đảm bảo so_luong là số
          so_luong: Number(CTSPData.so_luong),
          // Đảm bảo trang_thai là chuỗi hợp lệ
          trang_thai: CTSPData.trang_thai || true,
          // Đảm bảo hinh_anh là mảng không có phần tử rỗng
          hinh_anh: Array.isArray(CTSPData.hinh_anh) ?
            CTSPData.hinh_anh.filter(url => url && url.trim() !== '') : []
        };

        console.log('Dữ liệu CTSP gửi đi sau khi format:', formattedData);
        const response = await sanPhamService.createCTSP(formattedData);
        console.log('Response từ service thêm chi tiết sản phẩm:', response);
        return response;
      } catch (error) {
        console.error('Lỗi trong createCTSP:', error);
        if (error.response && error.response.data) {
          console.error('Thông báo lỗi từ server:', error.response.data);
        }
        throw error;
      }
    },
    //Lấy danh sách chi tiết sản phẩm
    async getAllCTSP() {
      try {
        console.log('Đang tải danh sách chi tiết sản phẩm')
        const chiTietSanPhamResponse = await sanPhamService.getAllChiTietSanPham()

        if (!chiTietSanPhamResponse || chiTietSanPhamResponse.error) {
          console.error('Không lấy được dữ liệu chi tiết sản phẩm')
          this.getAllChiTietSanPham = []
          return []
        }

        // Xử lý dữ liệu - thêm hình ảnh nếu cần
        try {
          const imagePromises = chiTietSanPhamResponse.map(async (ctsp) => {
            if (ctsp.id_chi_tiet_san_pham) {
              const images = await this.getImage(ctsp.id_chi_tiet_san_pham, true)
              ctsp.hinh_anh =
                images && images.length > 0 ? images[0].hinh_anh : 'Không có ảnh chính'
            }
            return ctsp
          })

          await Promise.all(imagePromises)
        } catch (imageError) {
          console.error('Lỗi khi tải hình ảnh:', imageError)
        }

        console.log('Đã tải', chiTietSanPhamResponse.length, 'chi tiết sản phẩm')
        this.getAllChiTietSanPham = chiTietSanPhamResponse

        return chiTietSanPhamResponse
      } catch (error) {
        console.error('Lỗi khi tải danh sách chi tiết sản phẩm:', error)
        this.getAllChiTietSanPham = []
        return []
      }
    },
    //Tìm kiếm chi tiết sản phẩm
    async searchCTSP(search) {
      try {
        const chiTietSanPhamRespone = await sanPhamService.searchChiTietSanPham(search)
        if (chiTietSanPhamRespone && chiTietSanPhamRespone.error) {
          toast.error('Không lấy được dữ liệu')
          this.searchChiTietSanPham = []
          return
        }

        // Nếu không có dữ liệu hoặc mảng rỗng
        if (
          !chiTietSanPhamRespone ||
          !Array.isArray(chiTietSanPhamRespone) ||
          chiTietSanPhamRespone.length === 0
        ) {
          console.log('Không có kết quả tìm kiếm chi tiết sản phẩm')
          this.searchChiTietSanPham = []
          return
        }

        try {
          const imagePromises = chiTietSanPhamRespone.map(async (ctsp) => {
            if (ctsp && ctsp.id_chi_tiet_san_pham) {
              const images = await this.getImage(ctsp.id_chi_tiet_san_pham, true)
              ctsp.hinh_anh =
                images && images.length > 0 ? images[0].hinh_anh : 'Không có ảnh chính'
            }
            return ctsp
          })

          const results = await Promise.all(imagePromises)
          this.searchChiTietSanPham = results.filter((item) => item !== null)
          console.log('Kết quả tìm kiếm chi tiết sản phẩm đã xử lý:', this.searchChiTietSanPham)
        } catch (error) {
          console.log('Lỗi khi xử lý hình ảnh:', error)
          this.searchChiTietSanPham = chiTietSanPhamRespone
        }
      } catch (error) {
        console.log('Lỗi khi tìm kiếm chi tiết sản phẩm:', error)
        this.searchChiTietSanPham = []
      }
    },
    // Tìm kiếm sản phẩm
    async searchSP(keyword) {
      console.log('Đang tìm kiếm sản phẩm với từ khóa:', keyword)
      if (!keyword || keyword.trim() === '') {
        this.resetSearch()
        return
      }

      try {
        // Lưu từ khóa tìm kiếm
        this.searchKeyword = keyword.trim().toLowerCase()

        // Đảm bảo dữ liệu chi tiết sản phẩm đã được tải
        if (!this.getAllChiTietSanPham || this.getAllChiTietSanPham.length === 0) {
          console.log('Chưa có dữ liệu chi tiết sản phẩm, tải từ API')
          await this.getAllCTSP()
        }

        // Tìm kiếm trên chi tiết sản phẩm
        const matchingProductIds = new Set()

        this.getAllChiTietSanPham.forEach((ctsp) => {
          // Kết hợp tất cả thông tin có thể tìm kiếm
          const searchableText = [
            ctsp.ten_san_pham?.toLowerCase() || '',
            ctsp.mo_ta?.toLowerCase() || '',
            ctsp.ten_thuong_hieu?.toLowerCase() || '',
            ctsp.ten_danh_muc?.toLowerCase() || '',
            ctsp.ten_chat_lieu?.toLowerCase() || '',
            ctsp.ten_mau_sac?.toLowerCase() || '',
            ctsp.ten_size?.toLowerCase() || '',
            `${ctsp.gia_ban || ''}`,
            `${ctsp.gia_nhap || ''}`,
            `${ctsp.so_luong_ton || ''}`,
          ].join(' ')

          // Nếu nội dung có thể tìm kiếm chứa từ khóa
          if (searchableText.includes(this.searchKeyword)) {
            matchingProductIds.add(ctsp.id_san_pham)
          }
        })

        // Cập nhật ID sản phẩm tìm thấy
        this.searchProductIds = [...matchingProductIds]
        console.log(
          `Tìm thấy ${this.searchProductIds.length} sản phẩm khớp với từ khóa "${keyword}"`,
        )

        // Cập nhật danh sách ID cuối cùng
        this.updateFinalProductIds()
      } catch (error) {
        console.error('Lỗi khi tìm kiếm sản phẩm:', error)
      }
    },
    getLangue(check) {
      const vni = {
        nguoiDung: 'Đăng nhập',
        cuaHang: 'Cửa hàng',
        hoTro: 'Hỗ trợ',
        gioHang: 'Giỏ hàng',
        timKiem: 'Bạn đang muốn tìm kiếm gì?',
      }
      const eng = {
        nguoiDung: 'Login',
        cuaHang: 'Store',
        hoTro: 'Support',
        gioHang: 'Cart',
        timKiem: 'What are you looking for?',
      }
      if (!check) {
        this.changeLanguage = vni
        this.check = true
        this.language = 'EN'
      } else {
        this.changeLanguage = eng
        this.check = false
        this.language = 'VI'
      }
    },
    showModal(show) {
      this.status = show
    },
    showModalSideBar(id) {
      this.id = id
      if (this.status) {
        id = 0
        this.id = id
      }
    },
    hideModalSideBar(id) {
      this.id = 0
    },
    showModalSideBar1(show) {
      this.statusSideBar1 = show
    },
    closeNoitification() {
      this.checkNoitification = false
    },
    async getGiaMax() {
      const response = await sanPhamService.giaMax()
      this.giaMax = response
    },
    // Action để áp dụng bộ lọc
    async filterSanPham(filterCriteria) {
      console.log('Đang lọc sản phẩm với tiêu chí:', filterCriteria)
      this.filterCriteria = { ...filterCriteria }

      try {
        // Đảm bảo dữ liệu chi tiết sản phẩm đã được tải
        if (!this.getAllChiTietSanPham || this.getAllChiTietSanPham.length === 0) {
          console.warn('Không có dữ liệu chi tiết sản phẩm để lọc')
          this.filteredProductIds = []
          return
        }

        // Debug: Log 5 sản phẩm chi tiết đầu tiên để kiểm tra cấu trúc
        if (this.debug) {
          console.log('=== DEBUG: Mẫu chi tiết sản phẩm ===')
          console.log(this.getAllChiTietSanPham.slice(0, 5))
        }

        // Khởi tạo Set để lưu trữ ID sản phẩm khớp
        const matchingProductIds = new Set()
        await this.getGiaMax()
        // Kiểm tra xem có tiêu chí lọc nào không
        const hasFilterCriteria =
          (filterCriteria.id_danh_muc && filterCriteria.id_danh_muc.length > 0) ||
          (filterCriteria.id_thuong_hieu && filterCriteria.id_thuong_hieu.length > 0) ||
          (filterCriteria.id_chat_lieu && filterCriteria.id_chat_lieu.length > 0) ||
          (filterCriteria.id_mau_sac && filterCriteria.id_mau_sac.length > 0) ||
          (filterCriteria.id_size && filterCriteria.id_size.length > 0) ||
          filterCriteria.minPrice > 0 ||
          filterCriteria.maxPrice < this.giaMax

        // Nếu không có tiêu chí lọc, trả về tất cả sản phẩm
        if (!hasFilterCriteria) {
          console.log('Không có tiêu chí lọc, lấy tất cả sản phẩm')
          this.filteredProductIds = this.getAllSanPham.map((sp) => sp.id_san_pham)
          return
        }

        // Lọc chi tiết sản phẩm dựa trên tiêu chí
        for (const ctsp of this.getAllChiTietSanPham) {
          // Kiểm tra từng tiêu chí
          let matches = true

          // Debug: Ghi log lọc đa tiêu chí
          if (
            this.debug &&
            ((filterCriteria.id_danh_muc && filterCriteria.id_danh_muc.includes(1)) ||
              (filterCriteria.id_thuong_hieu && filterCriteria.id_thuong_hieu.includes(1)) ||
              (filterCriteria.id_chat_lieu && filterCriteria.id_chat_lieu.includes(1)))
          ) {
            console.log('=== DEBUG: Chi tiết kiểm tra khớp với ID = 1 ===')
            console.log('Sản phẩm đang kiểm tra:', ctsp.ten_san_pham)
            console.log('ID danh mục:', ctsp.id_danh_muc, 'Tiêu chí:', filterCriteria.id_danh_muc)
            console.log(
              'ID thương hiệu:',
              ctsp.id_thuong_hieu,
              'Tiêu chí:',
              filterCriteria.id_thuong_hieu,
            )
            console.log(
              'ID chất liệu:',
              ctsp.id_chat_lieu,
              'Tiêu chí:',
              filterCriteria.id_chat_lieu,
            )
          }

          // Lọc danh mục - Kiểm tra xem id_danh_muc của sản phẩm có nằm trong danh sách các id đã chọn không
          if (filterCriteria.id_danh_muc && filterCriteria.id_danh_muc.length > 0) {
            const idDanhMucStr = String(ctsp.id_danh_muc)
            const found = filterCriteria.id_danh_muc.some((id) => String(id) === idDanhMucStr)
            if (!found) {
              matches = false
            }
          }

          // Lọc thương hiệu
          if (
            matches &&
            filterCriteria.id_thuong_hieu &&
            filterCriteria.id_thuong_hieu.length > 0
          ) {
            const idThuongHieuStr = String(ctsp.id_thuong_hieu)
            const found = filterCriteria.id_thuong_hieu.some((id) => String(id) === idThuongHieuStr)
            if (!found) {
              matches = false
            }
          }

          // Lọc chất liệu
          if (matches && filterCriteria.id_chat_lieu && filterCriteria.id_chat_lieu.length > 0) {
            const idChatLieuStr = String(ctsp.id_chat_lieu)
            const found = filterCriteria.id_chat_lieu.some((id) => String(id) === idChatLieuStr)
            if (!found) {
              matches = false
            }
          }

          // Lọc màu sắc
          if (matches && filterCriteria.id_mau_sac && filterCriteria.id_mau_sac.length > 0) {
            const idMauSacStr = String(ctsp.id_mau_sac)
            const found = filterCriteria.id_mau_sac.some((id) => String(id) === idMauSacStr)
            if (!found) {
              matches = false
            }
          }

          // Lọc kích thước
          if (matches && filterCriteria.id_size && filterCriteria.id_size.length > 0) {
            const idSizeStr = String(ctsp.id_kich_thuoc)
            const found = filterCriteria.id_size.some((id) => String(id) === idSizeStr)
            if (!found) {
              matches = false
            }
          }

          // Lọc giá - vẫn giữ nguyên
          if (matches && filterCriteria.minPrice > 0) {
            // Chuyển đổi giá bán sang kiểu số nếu là chuỗi
            const giaBan =
              typeof ctsp.gia_ban === 'string'
                ? parseFloat(ctsp.gia_ban.replace(/[^\d]/g, ''))
                : ctsp.gia_ban

            if (giaBan < filterCriteria.minPrice) {
              matches = false
            }
          }
          await this.getGiaMax()
          if (matches && filterCriteria.maxPrice < this.giaMax) {
            // Chuyển đổi giá bán sang kiểu số nếu là chuỗi
            const giaBan =
              typeof ctsp.gia_ban === 'string'
                ? parseFloat(ctsp.gia_ban.replace(/[^\d]/g, ''))
                : ctsp.gia_ban

            if (giaBan > filterCriteria.maxPrice) {
              matches = false
            }
          }

          // Nếu chi tiết sản phẩm khớp với tất cả tiêu chí, thêm ID của nó vào Set
          if (matches) {
            matchingProductIds.add(ctsp.id_san_pham)
          }
        }

        // Cập nhật ID sản phẩm đã lọc
        this.filteredProductIds = [...matchingProductIds]
        console.log(`Lọc được ${this.filteredProductIds.length} sản phẩm khớp với tiêu chí`)
      } catch (error) {
        console.error('Lỗi khi lọc sản phẩm:', error)
        this.filteredProductIds = []
      }
    },

    // Tạo thêm alias để tương thích với code cũ
    async applyFilter(filterCriteria) {
      // Gọi lại hàm filterSanPham để đảm bảo tương thích
      return this.filterSanPham(filterCriteria)
    },

    // Reset bộ lọc
    resetFilter() {
      console.log('Đang reset trạng thái lọc...')
      this.filterCriteria = {}
      this.filteredProductIds = []

      // Nếu đang kết hợp tìm kiếm và lọc, cập nhật lại kết quả cuối cùng
      if (this.searchProductIds.length > 0) {
        console.log('Vẫn còn tìm kiếm, giữ lại kết quả tìm kiếm')
      }
    },

    // Hàm cập nhật kết quả cuối cùng dựa trên trạng thái tìm kiếm và lọc
    updateFinalProductIds() {
      const isSearching = this.searchKeyword && this.searchProductIds.length > 0
      const isFiltering =
        Object.keys(this.filterCriteria).length > 0 && this.filteredProductIds.length > 0

      console.log(
        `Đang cập nhật danh sách cuối cùng. Tìm kiếm: ${isSearching}, Lọc: ${isFiltering}`,
      )

      if (isSearching && isFiltering) {
        // Nếu đang tìm kiếm và lọc, lấy giao của 2 danh sách
        this.finalProductIds = this.searchProductIds.filter((id) =>
          this.filteredProductIds.includes(id),
        )
        console.log('Kết hợp tìm kiếm và lọc:', this.finalProductIds.length)
      } else if (isSearching) {
        // Nếu chỉ tìm kiếm
        this.finalProductIds = [...this.searchProductIds]
        console.log('Chỉ tìm kiếm:', this.finalProductIds.length)
      } else if (isFiltering) {
        // Nếu chỉ lọc
        this.finalProductIds = [...this.filteredProductIds]
        console.log('Chỉ lọc:', this.finalProductIds.length)
      } else {
        // Nếu không có cả 2
        this.finalProductIds = []
        console.log('Không có tìm kiếm hoặc lọc')
      }
    },

    // Hàm reset trạng thái tìm kiếm
    resetSearch() {
      console.log('Đang reset trạng thái tìm kiếm...')
      this.searchKeyword = ''
      this.searchProductIds = []

      // Nếu đang kết hợp tìm kiếm và lọc, cập nhật lại kết quả cuối cùng
      if (this.filteredProductIds.length > 0) {
        console.log('Vẫn còn bộ lọc, giữ lại kết quả lọc')
      }
    },

    // Thêm các phương thức tải dữ liệu danh mục, thương hiệu, chất liệu, màu sắc, kích thước
    async getAllDM() {
      try {
        console.log('Đang tải danh sách danh mục...')
        const response = await sanPhamService.getDanhMucList()

        if (!response || response.error) {
          console.error('Không lấy được dữ liệu danh mục')
          return []
        }

        console.log(`Đã tải ${response.length} danh mục`)
        this.getAllDanhMuc = response
        return response
      } catch (error) {
        console.error('Lỗi khi tải danh sách danh mục:', error)
        this.getAllDanhMuc = []
        return []
      }
    },

    async getAllTH() {
      try {
        console.log('Đang tải danh sách thương hiệu...')
        const response = await sanPhamService.getThuongHieuList()

        if (!response || response.error) {
          console.error('Không lấy được dữ liệu thương hiệu')
          return []
        }

        console.log(`Đã tải ${response.length} thương hiệu`)
        this.getAllThuongHieu = response
        return response
      } catch (error) {
        console.error('Lỗi khi tải danh sách thương hiệu:', error)
        this.getAllThuongHieu = []
        return []
      }
    },
    async getVoucherLocKieuGiamGia(page = 0, size = 5, kieuGiamGia = '') {
      try {
        console.log('Calling getVoucherLocKieuGiamGia with:', { page, size, kieuGiamGia })
        const response = await voucherService.getVoucherLocKieuGiamGia(page, size, kieuGiamGia)
        console.log('Response from getVoucherLocKieuGiamGia:', response)
        if (response.error) {
          toast.error('Không lấy được dữ liệu voucher theo kiểu giảm giá')
          return
        }
        this.getAllVoucherArr = response.content || []
        this.voucherTotalPages = response.totalPages || 0
        this.voucherCurrentPage = page
      } catch (error) {
        console.error('Lỗi khi lọc voucher theo kiểu giảm giá:', error)
        toast.error('Có lỗi xảy ra')
      }
    },
    // Lọc khuyến mãi theo kiểu giảm giá
    async getKhuyenMaiLocKieuGiamGia(page = 0, size = 5, kieuGiamGia = '') {
      try {
        console.log('Calling getKhuyenMaiLocKieuGiamGia with:', { page, size, kieuGiamGia })
        const response = await khuyenMaiService.getKhuyenMaiLocKieuGiamGia(page, size, kieuGiamGia)
        console.log('Response from getKhuyenMaiLocKieuGiamGia:', response)
        if (response.error) {
          console.error('API returned error:', response)
          toast.error('Không lấy được dữ liệu khuyến mãi theo kiểu giảm giá')
          return
        }
        this.getAllKhuyenMaiArr = response.content || []
        this.khuyenMaiTotalPages = response.totalPages || 0
        this.khuyenMaiCurrentPage = page
      } catch (error) {
        console.error(
          'Lỗi khi gọi API lọc khuyến mãi theo kiểu giảm giá:',
          error.message,
          error.stack,
        )
        toast.error('Có lỗi xảy ra')
      }
    },

    async getAllCL() {
      try {
        console.log('Đang tải danh sách chất liệu...')
        const response = await sanPhamService.getChatLieuList()

        if (!response || response.error) {
          console.error('Không lấy được dữ liệu chất liệu')
          return []
        }

        console.log(`Đã tải ${response.length} chất liệu`)
        this.getAllChatLieu = response
        return response
      } catch (error) {
        console.error('Lỗi khi tải danh sách chất liệu:', error)
        this.getAllChatLieu = []
        return []
      }
    },

    async getAllMS() {
      try {
        console.log('Đang tải danh sách màu sắc...')
        const response = await sanPhamService.getMauSacList()

        if (!response || response.error) {
          console.error('Không lấy được dữ liệu màu sắc')
          return []
        }

        console.log(`Đã tải ${response.length} màu sắc`)
        this.getAllMauSac = response
        return response
      } catch (error) {
        console.error('Lỗi khi tải danh sách màu sắc:', error)
        this.getAllMauSac = []
        return []
      }
    },

    async getAllKT() {
      try {
        console.log('Đang tải danh sách kích thước...')
        const response = await sanPhamService.getSizeList()

        if (!response || response.error) {
          console.error('Không lấy được dữ liệu kích thước')
          return []
        }

        console.log(`Đã tải ${response.length} kích thước`)
        this.getAllKichThuoc = response
        return response
      } catch (error) {
        console.error('Lỗi khi tải danh sách kích thước:', error)
        this.getAllKichThuoc = []
        return []
      }
    },

    async searchKhuyenMai(keyword, page = 0, size = 5) {
      try {
        const khuyenMaiSearch = await khuyenMaiService.searchKhuyenMai(keyword, page, size)
        if (khuyenMaiSearch.error) {
          toast.error('Không tìm thấy khuyến mãi')
          this.khuyenMaiSearch = []
          this.khuyenMaiTotalPages = 0
          this.khuyenMaiCurrentPage = 0
          this.khuyenMaiTotalItems = 0
        } else {
          this.khuyenMaiSearch = khuyenMaiSearch.content || []
          this.khuyenMaiTotalPages = khuyenMaiSearch.totalPages || 0
          this.khuyenMaiCurrentPage = page
          this.khuyenMaiTotalItems = khuyenMaiSearch.totalElements || 0
        }
      } catch (error) {
        console.error('Lỗi khi tìm kiếm khuyến mãi:', error)
        toast.error('Có lỗi xảy ra')
        this.khuyenMaiSearch = [] // Đặt về rỗng khi có lỗi
      }
    },

    // Thêm vào sau hàm changeAllCTSPKhongHoatDong
    async updateCTSPStatus(ctspId, newStatus) {
      try {
        // Gọi API thay đổi trạng thái dựa vào trạng thái mới
        let response;
        if (newStatus === true) {
          response = await this.changeAllCTSPHoatDong(ctspId);
        } else {
          response = await this.changeAllCTSPKhongHoatDong(ctspId);
        }

        // Lấy dữ liệu từ response
        const responseData = response?.data || response;

        if (!responseData) {
          throw new Error('Không nhận được dữ liệu từ API');
        }

        // Nếu response có chứa thông tin sản phẩm cha, cập nhật trạng thái sản phẩm cha trong store
        if (responseData.sanPham && responseData.sanPham.id_san_pham !== undefined) {
          const parentId = responseData.sanPham.id_san_pham;
          const newParentStatus = responseData.sanPham.trang_thai;

          // Tìm và cập nhật sản phẩm trong store
          const productIndex = this.getAllSanPham.findIndex(item => item.id_san_pham === parentId);
          if (productIndex !== -1) {
            // Cập nhật trạng thái cho sản phẩm trong store để đảm bảo UI đồng bộ
            this.getAllSanPham[productIndex].trang_thai = newParentStatus;
            console.log(`Đã cập nhật trạng thái sản phẩm ${parentId} trong store thành ${newParentStatus}`);
          }
        }

        // Lấy trạng thái CTSP từ chiTietSanPham trong response
        const ctspStatus = responseData.chiTietSanPham?.trang_thai ?? newStatus;

        // Trả về dữ liệu đã được cập nhật
        return {
          success: true,
          data: responseData,
          message: `Đã chuyển trạng thái thành ${ctspStatus ? 'Hoạt động' : 'Không hoạt động'}`
        };
      } catch (error) {
        console.error('Lỗi khi cập nhật trạng thái CTSP:', error);
        return {
          success: false,
          message: error.message || 'Có lỗi xảy ra khi cập nhật trạng thái'
        };
      }
    },

    async bulkUpdateCTSPStatus(selectedCtspIds, newStatus) {
      try {
        let successCount = 0;
        let errorCount = 0;

        // Sử dụng Set để theo dõi các ID sản phẩm cha đã được cập nhật
        const updatedParentIds = new Set();
        // Map để lưu trạng thái cuối cùng của mỗi sản phẩm cha
        const parentStatusMap = new Map();

        // Xử lý từng chi tiết sản phẩm
        for (const ctspId of selectedCtspIds) {
          try {
            // Gọi API thông qua hàm đã tạo trước đó
            let response;
            if (newStatus === true) {
              response = await this.changeAllCTSPHoatDong(ctspId);
            } else {
              response = await this.changeAllCTSPKhongHoatDong(ctspId);
            }

            console.log(`Response từ API cho CTSP ${ctspId}:`, response);

            // Kiểm tra response
            if (response) {
              const responseData = response.data || response;

              // Kiểm tra trạng thái của CTSP từ chiTietSanPham
              const ctspStatus = responseData.chiTietSanPham?.trang_thai;
              if (ctspStatus === newStatus) {
                successCount++;

                // Cập nhật sản phẩm cha trong store nếu có thông tin
                if (responseData.sanPham && responseData.sanPham.id_san_pham !== undefined) {
                  const parentId = responseData.sanPham.id_san_pham;
                  const newParentStatus = responseData.sanPham.trang_thai;

                  // Thêm ID sản phẩm cha vào danh sách đã cập nhật
                  updatedParentIds.add(parentId);
                  // Lưu trạng thái mới nhất của sản phẩm cha
                  parentStatusMap.set(parentId, newParentStatus);

                  // Tìm và cập nhật sản phẩm trong store
                  const productIndex = this.getAllSanPham.findIndex(item => item.id_san_pham === parentId);
                  if (productIndex !== -1) {
                    this.getAllSanPham[productIndex].trang_thai = newParentStatus;
                    console.log(`Đã cập nhật trạng thái sản phẩm ${parentId} trong store thành ${newParentStatus}`);
                  }
                }
              } else {
                errorCount++;
              }
            } else {
              errorCount++;
            }
          } catch (error) {
            errorCount++;
            console.error(`Lỗi khi thay đổi trạng thái CTSP ${ctspId}:`, error);
          }
        }

        // Chuyển đổi Set và Map thành mảng để trả về
        const updatedParents = Array.from(updatedParentIds).map(parentId => ({
          id: parentId,
          status: parentStatusMap.get(parentId)
        }));

        // Trả về kết quả chi tiết hơn
        return {
          success: successCount > 0,
          successCount,
          errorCount,
          totalProcessed: selectedCtspIds.length,
          parentStatusUpdated: updatedParents.length > 0,
          updatedParents: updatedParents,
          message: `Đã chuyển ${successCount}/${selectedCtspIds.length} biến thể thành ${newStatus}`
        };
      } catch (error) {
        console.error('Lỗi khi cập nhật hàng loạt trạng thái CTSP:', error);
        return {
          success: false,
          message: error.message || 'Có lỗi xảy ra khi cập nhật trạng thái hàng loạt'
        };
      }
    },
    setCurrentHoaDonId(idHoaDon) {
      this.currentHoaDonId = idHoaDon
    },
    getCurrentHoaDonId() {
      return this.currentHoaDonId
    },

    // Lấy chi tiết hóa đơn
    async getHoaDonDetails(maHoaDon) {
      try {
        const result = await hoaDonService.getHoaDonDetails(maHoaDon);
        const response = await hoaDonService.getCTHD(maHoaDon);

        if (result.thanh_cong) {
          const data = {
            ma_hoa_don: result.ma_hoa_don,
            trang_thai: result.trang_thai,
            hinh_thuc_thanh_toan: result.hinh_thuc_thanh_toan,
            ngay_tao: result.ngay_tao,
            ho_ten: result.ho_ten,
            dia_chi: result.dia_chi,
            gia_tri_giam: result.gia_tri_giam,
            tong_tien: result.tong_tien,
            tong_tien_thanh_toan: result.tong_tien_thanh_toan,
            chiTietHoaDons: result.chi_tiet_hoa_don,
            return_status: result.trang_thai_tra_hang,
            return_history: result.lich_su_tra_hang
          };

          this.hoaDonDetail = response.hoaDon || {};
          this.chiTietHoaDons = response.chiTietHoaDons || [];
          this.trangThaiHistory = response.trangThaiHistory || [];

          return data;
        } else {

          throw new Error(result.message || 'Không tìm thấy đơn hàng');
        }
      } catch (error) {
        console.error('Lỗi khi tìm kiếm đơn hàng:', error);
        throw error;
      }
    },

    // Xử lý trả hàng
    async processReturn(returnData) {
      try {
        const result = await hoaDonService.processReturn(returnData);
        if (result.thanh_cong) {
          return {
            success: true,
            id_tra_hang: result.id_tra_hang
          };
        } else {
          toast.error(result.message || 'Xử lý trả hàng thất bại');
          return {
            success: false,
            thong_bao: result.message || 'Xử lý trả hàng thất bại'
          };
        }
      } catch (error) {
        console.error('Lỗi khi xử lý trả hàng:', error);
        toast.error('Có lỗi xảy ra khi xử lý trả hàng');
        throw error;
      }
    },

    // Thêm vào phần actions
    // Cập nhật lại hàm applySearchAndFilter để sử dụng đối tượng searchFilterParams
    async applySearchAndFilter() {
      try {
        console.log('Áp dụng tìm kiếm và lọc với params:', this.searchFilterParams);
        //Xét xem có dữ liệu cờ không
        const justAddedProduct = localStorage.getItem('justAddedProduct');

        if (justAddedProduct === 'true' || this.justAddedProduct === 'true') {
          console.log('Phát hiện dữ liệu mới từ localStorage, lấy dữ liệu mới nhất');

          // Lấy dữ liệu theo ngày sửa
          const newData = await this.getAllSanPhamNgaySua();

          // Đảm bảo dữ liệu trả về là mảng
          if (newData && Array.isArray(newData)) {
            this.filteredProductsData = newData;
          } else {
            console.warn('getAllSanPhamNgaySua() không trả về mảng:', newData);
            this.filteredProductsData = this.getAllSanPham || [];
          }

          // Xóa cờ từ localStorage sau khi đã lấy dữ liệu
          localStorage.removeItem('justAddedProduct');
          this.justAddedProduct = false;

          // Phát sự kiện để thông báo có dữ liệu mới
          window.dispatchEvent(new CustomEvent('search-filter-changed', {
            detail: {
              results: this.filteredProductsData,
              params: this.searchFilterParams,
              source: 'newData'
            }
          }));

          console.log('Đã phát sự kiện search-filter-changed với dữ liệu mới');
          return this.filteredProductsData;
        }
        // Destructuring các tham số
        const {
          keyword,
          giaBanMin,
          giaBanMax,
          listMauSac,
          listDanhMuc,
          listThuongHieu,
          listChatLieu,
          listKichThuoc
        } = this.searchFilterParams;
        // Gọi API với các tham số
        const response = await this.locAndTimKiemSanPhamVaChiTietSanPham(
          keyword,
          giaBanMin,
          giaBanMax,
          listMauSac,
          listDanhMuc,
          listThuongHieu,
          listChatLieu,
          listKichThuoc
        );

        // Xử lý kết quả
        if (response) {
          if (Array.isArray(response)) {
            this.filteredProductsData = response;
          } else if (typeof response === 'object' && response.data && Array.isArray(response.data)) {
            this.filteredProductsData = response.data;
          } else {
            this.filteredProductsData = [];
          }
        } else {
          this.filteredProductsData = [];
        }
        console.log('Kết quả tìm kiếm/lọc:', this.filteredProductsData.length, 'sản phẩm');

        // Phát sự kiện để thông báo thay đổi
        setTimeout(() => {
          window.dispatchEvent(new CustomEvent('search-filter-changed', {
            detail: {
              results: this.filteredProductsData,
              params: this.searchFilterParams
            }
          }));
          console.log('Đã phát sự kiện search-filter-changed');
        }, 0);

        return this.filteredProductsData;
      } catch (error) {
        console.error('Lỗi khi áp dụng tìm kiếm và lọc:', error);
        this.filteredProductsData = [];
        return [];
      }
    },

    // Thêm getter để lấy dữ liệu sản phẩm đã lọc
    getFilteredProductsData() {
      return this.filteredProductsData;
    },

    // Hàm reset tìm kiếm và lọc
    resetSearchAndFilter() {
      this.filteredProductsData = [];
      this.getAllSanPhamNgaySua(); // Tải lại tất cả sản phẩm

    },

    // Xử lý trả hàng
    async processReturn(returnData) {
      try {
        const result = await hoaDonService.processReturn(returnData);
        if (result.thanh_cong) {
          return {
            success: true,
            id_tra_hang: result.id_tra_hang
          };
        } else {
          toast.error(result.message || 'Xử lý trả hàng thất bại');
          return {
            success: false,
            thong_bao: result.message || 'Xử lý trả hàng thất bại'
          };
        }
      } catch (error) {
        console.error('Lỗi khi xử lý trả hàng:', error);
        toast.error('Có lỗi xảy ra khi xử lý trả hàng');
        throw error;
      }
    },

    // Thêm action để quản lý trạng thái loading
    setProductLoading(status) {
      this.isProductLoading = status;
    },

    // ✅ ZaloPay - Tạo đơn hàng
    async createZaloPayOrder(idHoaDon) {
      try {
        // Ensure idHoaDon is a number
        const invoiceId = typeof idHoaDon === 'object' ? idHoaDon.id_hoa_don : idHoaDon;

        const response = await axiosInstance.post('api/zalopay/create-order', null, {
          params: { idHoaDon: invoiceId }
        });
        return response.data;
      } catch (error) {
        console.error('Lỗi tạo đơn ZaloPay:', error);
        throw error;
      }
    },

    // ✅ ZaloPay - Kiểm tra trạng thái thanh toán
    async checkZaloPayStatus(idHoaDon) {
      try {
        // Ensure idHoaDon is a number
        const invoiceId = typeof idHoaDon === 'object' ? idHoaDon.id_hoa_don : idHoaDon;

        const response = await axiosInstance.get('api/zalopay/check-status', {
          params: { idHoaDon: invoiceId }
        });
        return response.data;
      } catch (error) {
        console.error('Lỗi kiểm tra trạng thái:', error);
        throw error;
      }
    },
  },
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'gbStore',
        storage: localStorage,
        paths: ['checkRouter', 'indexMenu', 'language', 'checkNoitification'],
      },
    ],
  },

  getters: {
    /**
     * Lấy danh sách sản phẩm đã được lọc và/hoặc tìm kiếm
     * @returns {Array} Danh sách sản phẩm đã được lọc
     */
    getFilteredProducts() {
      console.log('Đang lấy danh sách sản phẩm đã lọc/tìm kiếm...')

      // Nếu không có tìm kiếm và không có lọc, trả về tất cả sản phẩm
      const isSearching = this.searchKeyword && this.searchProductIds.length > 0
      const isFiltering = Object.keys(this.filterCriteria).length > 0

      if (!isSearching && !isFiltering) {
        console.log(
          'Không có tìm kiếm hoặc lọc, trả về tất cả sản phẩm:',
          this.getAllSanPham.length,
        )
        return this.getAllSanPham
      }

      try {
        // Format hàm trả về products đúng cấu trúc
        const formatProducts = (productIds) => {
          return productIds.length > 0
            ? this.getAllSanPham.filter((product) => productIds.includes(product.id_san_pham))
            : []
        }

        // Nếu chỉ đang tìm kiếm (không lọc)
        if (isSearching && !isFiltering) {
          const result = formatProducts(this.searchProductIds)
          console.log('Chỉ tìm kiếm, không lọc. Kết quả:', result.length)
          return result
        }

        // Nếu chỉ đang lọc (không tìm kiếm)
        if (!isSearching && isFiltering) {
          const result = formatProducts(this.filteredProductIds)
          console.log('Chỉ lọc, không tìm kiếm. Kết quả:', result.length)
          return result
        }

        // Nếu đang tìm kiếm và lọc, lấy giao của 2 danh sách
        const intersectionIds = this.searchProductIds.filter((id) =>
          this.filteredProductIds.includes(id),
        )
        const result = formatProducts(intersectionIds)
        console.log('Kết hợp tìm kiếm và lọc:', result.length)
        return result
      } catch (error) {
        console.error('Lỗi khi lấy danh sách sản phẩm đã lọc:', error)
        return []
      }
    },
  },


})
