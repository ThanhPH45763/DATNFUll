// Địa chỉ gốc của shop
export const SHOP_ADDRESS = {
  soNha: 'Số 7 ngõ 324/167',
  duong: 'Phương Canh',
  quan: 'Quận Nam Từ Liêm',
  tinh: 'Tỉnh Hà Nội'
}

// Bảng giá vận chuyển theo khu vực (VNĐ)
const SHIPPING_RATES = {
  // Nội thành Hà Nội
  'noithanh_hanoi': {
    fee: 25000,
    districts: [
      'Quận Ba Đình',
      'Quận Hoàn Kiếm',
      'Quận Đống Đa',
      'Quận Hai Bà Trưng',
      'Quận Hoàng Mai',
      'Quận Thanh Xuân',
      'Quận Long Biên',
      'Quận Cầu Giấy',
      'Quận Tây Hồ',
      'Quận Hà Đông',
      'Quận Bắc Từ Liêm',
      'Quận Nam Từ Liêm'
    ]
  },
  // Ngoại thành Hà Nội
  'ngoaithanh_hanoi': {
    fee: 35000,
    districts: [
      'Huyện Sóc Sơn',
      'Huyện Đông Anh',
      'Huyện Gia Lâm',
      'Huyện Thanh Trì',
      'Huyện Thường Tín',
      'Huyện Phú Xuyên',
      'Huyện Ứng Hòa',
      'Huyện Mỹ Đức',
      'Huyện Thạch Thất',
      'Huyện Quốc Oai',
      'Huyện Chương Mỹ',
      'Huyện Đan Phượng',
      'Huyện Hoài Đức',
      'Huyện Thanh Oai',
      'Huyện Mê Linh',
      'Huyện Ba Vì',
      'Thị xã Sơn Tây'
    ]
  },
  // Các tỉnh lân cận
  'lancan': {
    fee: 50000,
    provinces: [
      'Tỉnh Bắc Ninh',
      'Tỉnh Bắc Giang',
      'Tỉnh Hưng Yên',
      'Tỉnh Hải Dương',
      'Thành phố Hải Phòng',
      'Tỉnh Vĩnh Phúc',
      'Tỉnh Phú Thọ',
      'Tỉnh Hà Nam',
      'Tỉnh Nam Định',
      'Tỉnh Thái Bình',
      'Tỉnh Ninh Bình'
    ]
  },
  // Miền Bắc
  'mienbac': {
    fee: 70000,
    provinces: [
      'Tỉnh Lào Cai',
      'Tỉnh Điện Biên',
      'Tỉnh Lai Châu',
      'Tỉnh Sơn La',
      'Tỉnh Yên Bái',
      'Tỉnh Hòa Bình',
      'Tỉnh Thái Nguyên',
      'Tỉnh Lạng Sơn',
      'Tỉnh Quảng Ninh',
      'Tỉnh Cao Bằng',
      'Tỉnh Bắc Kạn',
      'Tỉnh Tuyên Quang',
      'Tỉnh Hà Giang'
    ]
  },
  // Miền Trung
  'mientrung': {
    fee: 90000,
    provinces: [
      'Tỉnh Thanh Hóa',
      'Tỉnh Nghệ An',
      'Tỉnh Hà Tĩnh',
      'Tỉnh Quảng Bình',
      'Tỉnh Quảng Trị',
      'Tỉnh Thừa Thiên Huế',
      'Thành phố Đà Nẵng',
      'Tỉnh Quảng Nam',
      'Tỉnh Quảng Ngãi',
      'Tỉnh Bình Định',
      'Tỉnh Phú Yên',
      'Tỉnh Khánh Hòa',
      'Tỉnh Ninh Thuận',
      'Tỉnh Bình Thuận'
    ]
  },
  // Tây Nguyên
  'taynguyen': {
    fee: 100000,
    provinces: [
      'Tỉnh Kon Tum',
      'Tỉnh Gia Lai',
      'Tỉnh Đắk Lắk',
      'Tỉnh Đắk Nông',
      'Tỉnh Lâm Đồng'
    ]
  },
  // TP.HCM
  'tphcm': {
    fee: 90000,
    provinces: ['Thành phố Hồ Chí Minh']
  },
  // Miền Nam
  'miennam': {
    fee: 100000,
    provinces: [
      'Tỉnh Long An',
      'Tỉnh Tiền Giang',
      'Tỉnh Bến Tre',
      'Tỉnh Trà Vinh',
      'Tỉnh Vĩnh Long',
      'Tỉnh Đồng Tháp',
      'Tỉnh An Giang',
      'Tỉnh Kiên Giang',
      'Tỉnh Cần Thơ',
      'Tỉnh Hậu Giang',
      'Tỉnh Sóc Trăng',
      'Tỉnh Bạc Liêu',
      'Tỉnh Cà Mau',
      'Tỉnh Đồng Nai',
      'Tỉnh Bình Dương',
      'Tỉnh Bình Phước',
      'Tỉnh Tây Ninh',
      'Tỉnh Bà Rịa - Vũng Tàu'
    ]
  }
}

export function calculateShippingFee(tinhThanhPho, quanHuyen) {
  if (!tinhThanhPho) return 0

  const normalizedProvince = tinhThanhPho.trim()
  const normalizedDistrict = quanHuyen ? quanHuyen.trim() : ''

  if (normalizedProvince === 'Tỉnh Hà Nội') {
    const isNoiThanh = SHIPPING_RATES.noithanh_hanoi.districts.some(
      d => d === normalizedDistrict
    )
    if (isNoiThanh) return SHIPPING_RATES.noithanh_hanoi.fee

    const isNgoaiThanh = SHIPPING_RATES.ngoaithanh_hanoi.districts.some(
      d => d === normalizedDistrict
    )
    if (isNgoaiThanh) return SHIPPING_RATES.ngoaithanh_hanoi.fee

    return SHIPPING_RATES.ngoaithanh_hanoi.fee
  }

  for (const [region, config] of Object.entries(SHIPPING_RATES)) {
    if (config.provinces && config.provinces.includes(normalizedProvince)) {
      return config.fee
    }
  }

  return 120000
}

export function getRegionName(tinhThanhPho) {
  if (!tinhThanhPho) return 'Chưa xác định'

  const normalizedProvince = tinhThanhPho.trim()

  if (normalizedProvince === 'Tỉnh Hà Nội') return 'Hà Nội'
  if (SHIPPING_RATES.lancan.provinces.includes(normalizedProvince)) return 'Lân cận Hà Nội'
  if (SHIPPING_RATES.mienbac.provinces.includes(normalizedProvince)) return 'Miền Bắc'
  if (SHIPPING_RATES.mientrung.provinces.includes(normalizedProvince)) return 'Miền Trung'
  if (SHIPPING_RATES.taynguyen.provinces.includes(normalizedProvince)) return 'Tây Nguyên'
  if (SHIPPING_RATES.tphcm.provinces.includes(normalizedProvince)) return 'TP. Hồ Chí Minh'
  if (SHIPPING_RATES.miennam.provinces.includes(normalizedProvince)) return 'Miền Nam'

  return 'Vùng xa'
}

export function formatVND(value) {
  if (!value && value !== 0) return '0đ'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value)
}
