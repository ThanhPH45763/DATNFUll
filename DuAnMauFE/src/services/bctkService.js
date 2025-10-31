// src/services/bctkService.js
import axiosInstance from "@/config/axiosConfig";
import { formatInTimeZone } from 'date-fns-tz';
import { format } from 'date-fns';
import { startOfToday, endOfToday } from 'date-fns';
// import { utcToZonedTime } from 'date-fns-tz';
const getSoLieu = async (type = null, startDate = null, endDate = null) => {
    try {
        let params = {};
        console.log('Input params:', { type, startDate, endDate });

        if (type === 'tuy-chon') {
            params = {
                type,
                startDate,
                endDate
            };
        } else if (type) {
            params = { type };
        }

        console.log('Params gửi đi:', params);
        const response = await axiosInstance.get('/admin/baoCaoThongKe', { params });
        console.log('Dữ liệu nhận về:', response.data);
        return response.data;
    } catch (error) {
        console.error('Lỗi trong getSoLieu:', error);
        throw error;
    }
};
const getTiLeTrangThai = async () => {
    try {
        const response = await axiosInstance.get('/admin/tiLeTrangThaiDonHang');
        console.log("Dữ liệu trả về từ API:", response.data);
        return response.data;
    } catch (error) {
        console.error("Lỗi khi lấy tỉ lệ trạng thái:", error);
        throw error;
    }
};
// const topSanPhamBanChay = async () => {
//     try {
//         const response = await axiosInstance.get('/admin/topSPBanChay');
//         return response.data;
//     } catch (error) {
//         console.error('Lỗi trong topSanPhamBanChay:', error);
//         throw error;
//     }
// };
// thử nghiệm
const getTopSanPhamBanChay = async (type = 'nam-nay', startDate = null, endDate = null) => {
    try {
        let url = '/admin/topSPBanChay';
        let params = { type };
        if (type === 'tuy-chon') {
            params.startDate = startDate;
            params.endDate = endDate;
        }
        const response = await axiosInstance.get(url, { params });
        console.log('API Response:', response.data);
        return response.data;
    } catch (error) {
        console.error('Error in getTopSanPhamBanChay:', error);
        throw error;
    }
};
// const topSanPhamBanCham = async () => {
//     try {
//         const response = await axiosInstance.get('/admin/topSPBanCham');
//         return response.data;
//     } catch (error) {
//         console.error('Lỗi trong topSanPhamBanCham:', error);
//         throw error;
//     }
// };
const getTopSanPhamSapHetHang = async () => {
    try {
        const url = '/admin/topSPSapHetHang';
        const response = await axiosInstance.get(url);
        console.log('API Response (Sắp hết hàng):', response.data);
        return response.data;
    } catch (error) {
        console.error('Error in getTopSanPhamSapHetHang:', error);
        throw error;
    }
};

const getChartData = async (timeUnit) => {
    try {
        const timeZone = 'Asia/Ho_Chi_Minh';
        const now = new Date();
        const year = format(now, 'yyyy', { timeZone });
        const currentMonth = now.getMonth() + 1;

        let results = [];

        switch (timeUnit) {
            // case 'day':
            //     try {
            //         const timeZone = 'Asia/Ho_Chi_Minh';
            //         const today = format(new Date(), 'yyyy-MM-dd');

            //         console.log('Gọi API với ngày:', today);
            //         const response = await getSoLieu('tuy-chon', {
            //             startDate: today,
            //             endDate: today
            //         });

            //         const hourlyData = [0, 0, 0, 0, 0, 0];
            //         const orderData = [0, 0, 0, 0, 0, 0];
            //         const productData = [0, 0, 0, 0, 0, 0];

            //         if (response && response.donHang && Array.isArray(response.donHang)) {
            //             response.donHang.forEach(order => {
            //                 if (!order.thoiGian) {
            //                     console.warn('Thiếu thoiGian trong order:', order);
            //                     return;
            //                 }

            //                 const orderTime = new Date(order.thoiGian);
            //                 const hour = orderTime.getHours();
            //                 const timeSlot = Math.floor(hour / 4);

            //                 if (timeSlot >= 0 && timeSlot < 6) {
            //                     hourlyData[timeSlot] += order.tongTien || 0;
            //                     orderData[timeSlot] += 1;
            //                     productData[timeSlot] += order.soLuongSanPham || 0;
            //                     console.log(`Order tại ${format(orderTime, 'HH:mm')}: Đã thêm vào khung ${timeSlot}`);
            //                 } else {
            //                     console.warn(`Giờ ${hour} ngoài phạm vi (0-23), bỏ qua`);
            //                 }
            //             });
            //         } else {
            //             console.warn('Không có dữ liệu donHang hoặc không phải mảng:', response);
            //         }

            //         console.log('Dữ liệu theo giờ:', { hourlyData, orderData, productData });
            //         return {
            //             doanhThu: hourlyData,
            //             donHang: orderData,
            //             sanPham: productData
            //         };
            //     } catch (error) {
            //         console.error('Lỗi khi lấy dữ liệu theo ngày:', error);
            //         return {
            //             doanhThu: [0, 0, 0, 0, 0, 0],
            //             donHang: [0, 0, 0, 0, 0, 0],
            //             sanPham: [0, 0, 0, 0, 0, 0]
            //         };
            //     }

            case 'week':
                const weekData = [];
                const today = new Date();
                today.setHours(0, 0, 0, 0);
                // const timeZone = 'Asia/Ho_Chi_Minh';

                // Tìm thứ 2
                const monday = new Date(today);
                const dayOfWeek = today.getDay();

                if (dayOfWeek === 0) {
                    monday.setDate(today.getDate() - 6);
                } else {
                    monday.setDate(today.getDate() - (dayOfWeek - 1));
                }
                let currentDate = new Date(monday);

                for (let i = 0; i < 7; i++) {
                    const dateStr = formatInTimeZone(currentDate, timeZone, 'yyyy-MM-dd');

                    if (currentDate.getTime() <= today.getTime()) {
                        const dayResult = await getSoLieu('tuy-chon', dateStr, dateStr);
                        weekData.push(dayResult);
                    } else {
                        weekData.push({
                            doanhThu: 0,
                            tongDonHang: 0,
                            tongSanPham: 0
                        });
                    }

                    currentDate.setDate(currentDate.getDate() + 1);
                }

                return {
                    doanhThu: weekData.map(day => day.doanhThu || 0),
                    donHang: weekData.map(day => day.tongDonHang || 0),
                    sanPham: weekData.map(day => day.tongSanPham || 0)
                };

            case 'month':
                const monthData = [];
                const currentMonth = now.getMonth() + 1; // 1-12

                for (let i = 1; i <= 12; i++) {
                    if (i <= currentMonth) {
                        // Sử dụng type là 'thang-nay' cho tháng hiện tại
                        if (i === currentMonth) {
                            const monthResult = await getSoLieu('thang-nay');
                            monthData.push(monthResult);
                        } else {
                            // Với các tháng trước, sử dụng tuy-chon
                            const firstDay = `${year}-${i.toString().padStart(2, '0')}-01`;
                            const lastDay = format(new Date(year, i, 0), 'yyyy-MM-dd', { timeZone });
                            const monthResult = await getSoLieu('tuy-chon', firstDay, lastDay);
                            monthData.push(monthResult);
                        }
                    } else {
                        // Các tháng trong tương lai
                        monthData.push({
                            doanhThu: 0,
                            tongDonHang: 0,
                            tongSanPham: 0
                        });
                    }
                }

                return {
                    doanhThu: monthData.map(month => month.doanhThu || 0),
                    donHang: monthData.map(month => month.tongDonHang || 0),
                    sanPham: monthData.map(month => month.tongSanPham || 0)
                };

            case 'year':
                const yearData = [];

                for (let i = 0; i < 5; i++) {
                    const yearValue = year - 4 + i;
                    const firstDay = `${yearValue}-01-01`;
                    const lastDay = `${yearValue}-12-31`;
                    const yearResult = await getSoLieu('tuy-chon', firstDay, lastDay);
                    yearData.push(yearResult);
                }

                return {
                    doanhThu: yearData.map(year => year.doanhThu || 0),
                    donHang: yearData.map(year => year.tongDonHang || 0),
                    sanPham: yearData.map(year => year.tongSanPham || 0)
                };

            default:
                return null;
        }
    } catch (error) {
        console.error('Lỗi khi lấy dữ liệu biểu đồ:', error);
        return null;
    }
};

export const bctkService = {
    getSoLieu,
    getChartData,
    getTiLeTrangThai,
    getTopSanPhamBanChay,
    getTopSanPhamSapHetHang,
    getTiLeTrangThai
}