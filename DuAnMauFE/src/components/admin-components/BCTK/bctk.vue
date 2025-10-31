<template>
    <div class="statistics-container">
        <div class="row">
            <h4 class="col-md-6 d-flex justify-content-start">📑 Thống kê doanh thu</h4>
            <div class="filter-section col-md-6 d-flex justify-content-end gap-3 mb-4">
                <!-- Date Range Picker luôn hiển thị -->
                <a-range-picker v-model:value="dateRange" @change="handleDateChange"
                    :placeholder="['Ngày bắt đầu', 'Ngày kết thúc']" :locale="locale" />
                <!-- Filter Select -->
                <a-select v-model:value="selectedFilter" show-search placeholder="Chọn thời gian cần thống kê"
                    style="width: 250px;" :options="options" :filter-option="filterOption"
                    @change="handleFilterChange"></a-select>

            </div>
        </div>

        <!-- Thống kê cards -->
        <div class="row g-4">
            <div class="col-md-4">
                <div class="card revenue-card">
                    <div class="card-body row">
                        <div class="col-md-3" style="font-size: 3rem;">💰</div>
                        <div class="col-md-8">
                            <h5 class="card-title">Doanh thu</h5>
                            <p class="card-text">{{ formatCurrency(thongKe.doanhThu) }}</p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card orders-card">
                    <div class="card-body row">
                        <div class="col-md-3" style="font-size: 3rem;">📦</div>
                        <div class="col-md-8">
                            <h5 class="card-title">Tổng đơn hàng</h5>
                            <p class="card-text">{{ thongKe.tongDonHang }}</p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card products-card">
                    <div class="card-body row">
                        <div class="col-md-3" style="font-size: 3rem;">🏷️</div>
                        <div class="col-md-8">
                            <h5 class="card-title">Số sản phẩm đã bán</h5>
                            <p class="card-text">{{ thongKe.tongSanPham }}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="row mb-4">
            <div class="chart-container mt-4 col-md-8">
                <div class="chart-header d-flex justify-content-between align-items-center mb-3">
                    <h3>Biểu đồ doanh thu</h3>
                    <div class="chart-controls d-flex gap-2">
                        <a-select v-model:value="timeUnit" style="width: 120px">
                            <!-- <a-select-option value="day">Theo ngày</a-select-option> -->
                            <a-select-option value="week">Theo tuần</a-select-option>
                            <a-select-option value="month">Theo tháng</a-select-option>
                            <a-select-option value="year">Theo năm</a-select-option>
                        </a-select>
                        <a-select v-model:value="chartType" style="width: 120px">
                            <a-select-option value="revenue">Doanh thu</a-select-option>
                            <a-select-option value="orders">Đơn hàng</a-select-option>
                            <a-select-option value="products">Sản phẩm</a-select-option>
                        </a-select>
                    </div>
                </div>
                <div class="chart-body">
                    <apexchart type="bar" height="400" :options="chartOptions" :series="series"></apexchart>
                </div>
            </div>
            <div class="col-md-4 mt-4">
                <a-card title="Tỉ lệ trạng thái đơn hàng" :bordered="false">
                    <a-spin :spinning="gbStore.loading">
                        <div id="orderStatusChart">
                            <apexchart type="pie" :options="chartOptionTiLeTrangThai" :series="chartSeries"
                                height="380" />
                        </div>
                    </a-spin>
                </a-card>
            </div>
        </div>

        <div class="row">
            <!-- Top 5 sản phẩm bán chạy -->
            <div class="col-md-6">
                <a-card :bordered="false">
                    <template #title>
                        <div class="d-flex justify-content-between align-items-center">
                            <span>Top 5 sản phẩm bán chạy</span>
                            <div class="filter-section d-flex gap-3">
                                <!-- Date Range Picker luôn hiển thị -->
                                <a-range-picker v-model:value="sellingDateRange" @change="handleSellingDateChange"
                                    :placeholder="['Start date', 'End date']" :locale="locale"
                                    style="margin-left: 10px;" />
                                <!-- Filter Select cho sản phẩm bán chạy -->
                                <a-select v-model:value="sellingFilter" show-search placeholder="Chọn thời gian"
                                    style="width: 120px;margin-right: 0.5px;" :options="options"
                                    :filter-option="filterOption" @change="handleSellingFilterChange"></a-select>
                            </div>
                        </div>
                    </template>
                    <a-table :columns="columns" :data-source="gbStore.topSanPhamBanChay" :pagination="false"
                        size="small">
                        <template #bodyCell="{ column, text, index }">
                            <template v-if="column.key === 'stt'">
                                <a-tag :color="getTagColor(index + 1)">
                                    {{ getTopRank(index + 1) }}
                                </a-tag>
                            </template>
                            <template v-else-if="column.key === 'gia_ban'">
                                {{ formatCurrency(text) }}
                            </template>
                        </template>
                    </a-table>
                </a-card>
            </div>

            <!-- Những sản phẩm sắp hết hàng -->
            <div class="col-md-6">
                <a-card :bordered="false">
                    <template #title>
                        <div class="d-flex justify-content-between align-items-center">
                            <span>Những sản phẩm sắp hết hàng</span>
                        </div>
                    </template>
                    <a-table :columns="columns" :data-source="gbStore.topSanPhamSapHetHang" :pagination="false"
                        size="small">
                        <template #bodyCell="{ column, text, index }">
                            <template v-if="column.key === 'stt'">
                                <a-tag :color="getTagColor(index + 1)">
                                    {{ getTopRank(index + 1) }}
                                </a-tag>
                            </template>
                            <template v-else-if="column.key === 'gia_ban'">
                                {{ formatCurrency(text) }}
                            </template>
                        </template>
                    </a-table>
                </a-card>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import { useGbStore } from '@/stores/gbStore';
import { storeToRefs } from 'pinia';
import viVN from 'ant-design-vue/es/date-picker/locale/vi_VN';
import VueApexCharts from 'vue3-apexcharts';
import { bctkService } from '@/services/bctkService';
import { Card, Table, Tag } from 'ant-design-vue';


// Định nghĩa cột
const columns = [
    {
        title: 'STT',
        dataIndex: 'stt',
        key: 'stt',
        width: 100,
    },
    {
        title: 'Mã sản phẩm',
        dataIndex: 'ma_san_pham',
        key: 'ma_san_pham',
    },
    {
        title: 'Tên sản phẩm',
        dataIndex: 'ten_san_pham',
        key: 'ten_san_pham',
    },
    {
        title: 'Số lượng',
        dataIndex: 'so_luong',
        key: 'so_luong',
        align: 'right',
    },
    {
        title: 'Giá',
        dataIndex: 'gia_ban',
        key: 'gia_ban',
        align: 'right',
        render: (text) => formatCurrency(text)
    },
];

// Dữ liệu mẫu - thay thế bằng dữ liệu thực từ API
const topSellingProducts = ref([]);

const topSanPhamSapHetHang = ref([]);


// Hàm lấy màu cho tag
const getTagColor = (index) => {
    switch (index) {
        case 1:
            return 'gold';
        case 2:
            return 'silver';
        case 3:
            return '#cd7f32'; // bronze
        default:
            return 'default';
    }
};
const gbStore = useGbStore();
const { thongKe } = storeToRefs(gbStore);
const { topSanPhamBanChay } = storeToRefs(gbStore);
const selectedFilter = ref('hom-nay');
const dateRange = ref();
const locale = {
    ...viVN,
    lang: {
        ...viVN.lang,
        shortWeekDays: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
        shortMonths: ['Th1', 'Th2', 'Th3', 'Th4', 'Th5', 'Th6', 'Th7', 'Th8', 'Th9', 'Th10', 'Th11', 'Th12'],
        months: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'],
        weekDays: ['Chủ nhật', 'Thứ hai', 'Thứ ba', 'Thứ tư', 'Thứ năm', 'Thứ sáu', 'Thứ bảy'],
        yearFormat: 'YYYY',
        monthFormat: 'TM'
    }
};

const options = [
    { value: 'hom-nay', label: 'Hôm nay' },
    { value: 'hom-qua', label: 'Hôm qua' },
    { value: 'tuan-nay', label: 'Tuần này' },
    { value: 'thang-nay', label: 'Tháng này' },
    { value: 'thang-truoc', label: 'Tháng trước' },
    { value: 'quy-nay', label: 'Quý này' },
    { value: 'quy-truoc', label: 'Quý trước' },
    { value: 'nam-nay', label: 'Năm nay' },
    { value: 'nam-ngoai', label: 'Năm ngoái' },
    { value: 'tuy-chon', label: 'Tùy chọn' }
];

const filterOption = (input, option) => {
    return option.label.toLowerCase().includes(input.toLowerCase());
};

// Xử lý khi thay đổi filter cho thống kê doanh thu
const handleFilterChange = async (value) => {
    console.log('Filter được chọn:', value);
    selectedFilter.value = value;
    if (value !== 'tuy-chon') {
        // Reset date range khi chọn filter khác tùy chọn
        dateRange.value = null;
        console.log('Gửi request với filter:', value);
        await gbStore.getSoLieu(value);
    }
};

// Xử lý khi thay đổi ngày cho thống kê doanh thu
const handleDateChange = async (dates) => {
    if (dates && dates[0] && dates[1]) {
        // Tự động set selectedFilter thành 'tuy-chon'
        selectedFilter.value = 'tuy-chon';

        const startDate = dates[0].format('YYYY-MM-DD');
        const endDate = dates[1].format('YYYY-MM-DD');
        console.log('Gửi request với ngày:', { startDate, endDate });
        await gbStore.getSoLieu('tuy-chon', startDate, endDate);
    }
};

const formatCurrency = (value) => {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(value || 0);
};

// Thêm state cho biểu đồ
const timeUnit = ref('month');
const chartType = ref('revenue');
const chartData = ref(null);

const series = ref([{
    name: 'Doanh thu',
    data: []
}]);

const chartOptions = ref({
    chart: {
        type: 'bar',
        zoom: { enabled: false },
        toolbar: { show: false }
    },
    stroke: {
        width: 0
    },
    grid: {
        borderColor: '#f1f1f1',
    },
    xaxis: {
        categories: [],
        labels: {
            style: { fontSize: '12px' }
        }
    },
    yaxis: {
        labels: {
            formatter: function (value) {
                if (chartType.value === 'revenue') {
                    return new Intl.NumberFormat('vi-VN', {
                        style: 'currency',
                        currency: 'VND',
                        maximumFractionDigits: 0
                    }).format(value);
                }
                return value.toFixed(0);
            }
        }
    },
    colors: ['#0ea5e9'],
    fill: {
        opacity: 1
    },
    dataLabels: {
        enabled: true,
        formatter: function (value) {
            if (chartType.value === 'revenue') {
                return new Intl.NumberFormat('vi-VN', {
                    style: 'currency',
                    currency: 'VND',
                    maximumFractionDigits: 0
                }).format(value);
            }
            return value.toFixed(0);
        },
        style: {
            fontSize: '12px',
            colors: ['#333']
        },
        offsetY: -20
    },
    plotOptions: {
        bar: {
            borderRadius: 4,
            columnWidth: '50%',
            dataLabels: {
                position: 'top'
            }
        }
    },
    tooltip: {
        theme: 'light',
        y: {
            formatter: function (value) {
                if (chartType.value === 'revenue') {
                    return new Intl.NumberFormat('vi-VN', {
                        style: 'currency',
                        currency: 'VND',
                        maximumFractionDigits: 0
                    }).format(value);
                }
                return value;
            }
        }
    }
});

// Hàm helper
const getChartName = (type) => {
    switch (type) {
        case 'revenue': return 'Doanh thu';
        case 'orders': return 'Đơn hàng';
        case 'products': return 'Sản phẩm';
        default: return 'Doanh thu';
    }
};

const getChartColor = (type) => {
    switch (type) {
        case 'revenue': return '#0ea5e9';
        case 'orders': return '#f59e0b';
        case 'products': return '#10b981';
        default: return '#0ea5e9';
    }
};

// Hàm lấy dữ liệu cho biểu đồ
const fetchChartData = async (timeUnitValue) => {
    try {
        console.log('Bắt đầu fetch dữ liệu cho:', timeUnitValue);
        const data = await bctkService.getChartData(timeUnitValue);
        console.log('Nhận được dữ liệu:', data);

        if (data) {
            chartData.value = data;
            console.log('Đã cập nhật chartData:', chartData.value);
            updateChartDisplay();
        }
    } catch (error) {
        console.error('Chi tiết lỗi:', error);
        chartData.value = {
            doanhThu: Array(getDefaultLength()).fill(0),
            donHang: Array(getDefaultLength()).fill(0),
            sanPham: Array(getDefaultLength()).fill(0)
        };
        updateChartDisplay();
    }
};

// Helper function to get default array length based on timeUnit
const getDefaultLength = () => {
    switch (timeUnit.value) {
        case 'day': return 6;
        case 'week': return 7;
        case 'month': return 12;
        case 'year': return 5;
        default: return 0;
    }
};

// Hàm cập nhật hiển thị biểu đồ
const updateChartDisplay = () => {
    console.log('updateChartDisplay được gọi');
    const { categories, data } = formatChartData();
    console.log('Categories:', categories);
    console.log('Data:', data);

    series.value = [{
        name: getChartName(chartType.value),
        data: data
    }];
    console.log('Series mới:', series.value);

    chartOptions.value = {
        ...chartOptions.value,
        xaxis: {
            categories: categories,
            labels: {
                style: {
                    fontSize: '12px'
                }
            }
        },
        yaxis: {
            labels: {
                formatter: function (value) {
                    if (chartType.value === 'revenue') {
                        return new Intl.NumberFormat('vi-VN', {
                            style: 'currency',
                            currency: 'VND',
                            maximumFractionDigits: 0
                        }).format(value);
                    }
                    return Math.round(value).toString();
                }
            }
        },
        colors: [getChartColor(chartType.value)]
    };
};

// Hàm format dữ liệu cho biểu đồ
const formatChartData = () => {
    console.log('formatChartData được gọi');
    console.log('timeUnit hiện tại:', timeUnit.value);
    console.log('chartData hiện tại:', chartData.value);

    let categories = [];
    let data = [];

    switch (timeUnit.value) {
        case 'day':
            categories = ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'];
            break;
        case 'week':
            categories = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];
            break;
        case 'month':
            categories = ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'];
            break;
        case 'year':
            const currentYear = new Date(new Date().toLocaleString('en-US', { timeZone: 'Asia/Ho_Chi_Minh' })).getFullYear();
            categories = [
                (currentYear - 4).toString(),
                (currentYear - 3).toString(),
                (currentYear - 2).toString(),
                (currentYear - 1).toString(),
                currentYear.toString()
            ];
            console.log('Năm hiện tại (Hà Nội):', currentYear);
            console.log('Categories năm:', categories);
            break;
    }

    if (chartData.value) {
        console.log('Dữ liệu chart hiện tại:', chartData.value);
        switch (chartType.value) {
            case 'revenue':
                data = chartData.value.doanhThu || Array(categories.length).fill(0);
                break;
            case 'orders':
                data = chartData.value.donHang || Array(categories.length).fill(0);
                break;
            case 'products':
                data = chartData.value.sanPham || Array(categories.length).fill(0);
                break;
        }
    } else {
        data = Array(categories.length).fill(0);
    }

    console.log('Categories:', categories);
    console.log('Data formatted:', data);

    return { categories, data };
};

// Theo dõi thay đổi
watch([timeUnit, chartType], async ([newTimeUnit, newChartType], [oldTimeUnit, oldChartType]) => {
    console.log('Watch triggered:', { newTimeUnit, newChartType, oldTimeUnit, oldChartType });

    if (newTimeUnit !== oldTimeUnit) {
        await fetchChartData(newTimeUnit);
    } else if (newChartType !== oldChartType) {
        updateChartDisplay();
    }
});

// State cho bộ lọc sản phẩm bán chạy
const sellingFilter = ref('nam-nay');
const sellingDateRange = ref();

// Xử lý khi thay đổi filter cho sản phẩm bán chạy
const handleSellingFilterChange = async (value) => {
    try {
        console.log('Selected filter:', value);
        sellingFilter.value = value;

        if (value !== 'tuy-chon') {
            // Reset date range khi chọn filter khác tùy chọn
            sellingDateRange.value = null;
            await gbStore.getTopSanPhamBanChay(value);
            console.log('Dữ liệu sau khi cập nhật:', gbStore.topSanPhamBanChay);
        }
    } catch (error) {
        console.error('Filter change error:', error);
    }
};

// Xử lý khi thay đổi ngày cho sản phẩm bán chạy
const handleSellingDateChange = async (dates) => {
    try {
        if (!dates || !dates[0] || !dates[1]) return;

        // Tự động set sellingFilter thành 'tuy-chon'
        sellingFilter.value = 'tuy-chon';

        const startDate = dates[0].format('YYYY-MM-DD');
        const endDate = dates[1].format('YYYY-MM-DD');

        await gbStore.getTopSanPhamBanChay('tuy-chon', startDate, endDate);
        console.log('Dữ liệu sau khi cập nhật date range:', gbStore.topSanPhamBanChay);
    } catch (error) {
        console.error('Date change error:', error);
    }
};

// Cập nhật onMounted
// onMounted(async () => {
//     try {
//         console.log('Đang fetch dữ liệu...');
//         await Promise.all([
//             gbStore.getSoLieu('hom-nay'),
//             fetchChartData('month'),
//             gbStore.fetchTiLeTrangThai(),
//             gbStore.getTopSanPhamBanChay('hom-nay'),
//             gbStore.getTopSanPhamBanCham('hom-nay')
//         ]);

//         // Cập nhật dữ liệu
//         topSellingProducts.value = gbStore.topSanPhamBanChay;
//         topSlowProducts.value = gbStore.topSanPhamBanCham;
//     } catch (error) {
//         console.error('Lỗi trong onMounted:', error);
//     }
// });

// thử nghiệm
onMounted(async () => {
    try {
        console.log('Đang fetch dữ liệu...');
        await Promise.all([
            gbStore.getSoLieu('hom-nay'),
            fetchChartData('month'),
            gbStore.fetchTiLeTrangThai(),
            gbStore.getTopSanPhamBanChay('nam-nay'),
            gbStore.getTopSanPhamSapHetHang()
        ]);

        // Cập nhật dữ liệu
        topSellingProducts.value = gbStore.topSanPhamBanChay;
        topSanPhamSapHetHang.value = gbStore.topSanPhamSapHetHang;
    } catch (error) {
        console.error('Lỗi trong onMounted:', error);
    }
});

// Thêm hàm này vào script
const getTopRank = (index) => {
    switch (index) {
        case 1:
            return '🥇 1';
        case 2:
            return '🥈 2';
        case 3:
            return '🥉 3';
        default:
            return `🏅 ${index}`;  // Thêm emoji medal cho các số khác
    }
};



// Cập nhật các computed properties
const chartSeries = computed(() => {
    if (!gbStore.tiLeTrangThai || !Array.isArray(gbStore.tiLeTrangThai) || gbStore.tiLeTrangThai.length === 0) {
        console.log("Không có dữ liệu tiLeTrangThai, trả về [100]");
        return [100]; // Giá trị mặc định để chart không bị lỗi
    }
    const values = gbStore.tiLeTrangThai.map(item => item.soLuong || 0);
    console.log("chartSeries values:", values);
    return values.length > 0 ? values : [100];
});

const chartLabels = computed(() => {
    if (!gbStore.tiLeTrangThai || !Array.isArray(gbStore.tiLeTrangThai) || gbStore.tiLeTrangThai.length === 0) {
        console.log("Không có dữ liệu tiLeTrangThai, trả về ['Không có dữ liệu']");
        return ['Không có dữ liệu']; // Giá trị mặc định
    }
    const labels = gbStore.tiLeTrangThai.map(item => item.trangThai || 'Không xác định');
    console.log("chartLabels:", labels);
    return labels.length > 0 ? labels : ['Không có dữ liệu'];
});

// Cấu hình biểu đồ
const chartOptionTiLeTrangThai = computed(() => ({
    chart: {
        type: 'pie',
    },
    labels: chartLabels.value,
    colors: ['#bb05e8', '#52c41a', '#faad14', '#cf1322', '#0f79ec', '#d86103'],
    legend: {
        position: 'bottom',
        horizontalAlign: 'center',
    },
    tooltip: {
        y: {
            formatter: (value) => `${(value || 0).toFixed(2)}%`
        }
    },
    plotOptions: {
        pie: {
            dataLabels: {
                offset: -5
            }
        }
    },
    dataLabels: {
        formatter(val, opts) {
            const name = opts.w.globals.labels[opts.seriesIndex] || 'Unknown';
            return [name, `${(val || 0).toFixed(1)}%`];
        }
    },
    title: {
        text: 'Phân bố trạng thái đơn hàng',
        align: 'center',
        style: {
            fontSize: '16px',
            fontFamily: '"Segoe UI", "Roboto", "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji'

        }
    },
    noData: {
        text: 'No data available',
        align: 'center',
        verticalAlign: 'middle'
    }
}));
</script>

<style scoped>
.ant-card {
    margin-bottom: 16px;
}

.statistics-container {
    padding: 20px;
}

.loading-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.8);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
}

.card {
    border: none;
    border-radius: 10px;
    transition: transform 0.2s;
}

.card:hover {
    transform: translateY(-5px);
}

.revenue-card {
    background: linear-gradient(135deg, #0ea5e9 0%, #38bdf8 100%);
}

.orders-card {
    background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
}

.products-card {
    background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
}

.card-body {
    padding: 1.5rem;
}

.card-title {
    color: white;
    font-size: 1.2rem;
    margin-bottom: 8px;
    opacity: 0.9;
}

.card-text {
    color: white;
    font-size: 1.5rem;
    font-weight: 600;
    margin: 0;
}

.col-md-3 {
    display: flex;
    align-items: center;
    justify-content: center;
}

/* Style cho a-select */
:deep(.ant-select) {
    font-size: 14px;
    font-weight: 600;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

:deep(.ant-select-selector) {
    font-size: 14px !important;
    font-weight: 600;
}

:deep(.ant-select-selection-item) {
    font-size: 14px !important;
    font-weight: 600;
}

:deep(.ant-select-item) {
    font-size: 14px !important;
    font-weight: 600;
}

/* Style cho date range picker */
:deep(.ant-picker) {
    font-size: 14px;
    font-weight: 600;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

:deep(.ant-picker-input) {
    font-size: 14px !important;
    font-weight: 600;
}

:deep(.ant-picker-range-separator) {
    font-size: 14px !important;
}

:deep(.ant-picker-suffix) {
    font-size: 14px !important;
    font-weight: 600;
}

.chart-container {
    background: white;
    border-radius: 10px;
    padding: 20px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chart-header h3 {
    font-size: 1.2rem;
    margin: 0;
    color: #374151;
}

#orderStatusChart {
    min-height: 380px;
}

/* Thêm vào phần <style scoped> */
:deep(.ant-tag) {
    min-width: 50px;
    text-align: center;
    padding: 0 8px;
    font-weight: 600;
    font-size: 14px;
    line-height: 22px;
    border-radius: 4px;
}

/* Style cho top 5 */
:deep(.ant-tag-gold) {
    background: linear-gradient(135deg, #FFD700 0%, #FFC000 100%);
    color: #000;
    border: none;
}

:deep(.ant-tag-silver) {
    background: linear-gradient(135deg, #C0C0C0 0%, #A9A9A9 100%);
    color: #000;
    border: none;
}

:deep([color="#cd7f32"]) {
    background: linear-gradient(135deg, #CD7F32 0%, #B8732D 100%);
    color: #000;
    border: none;
}

/* Style cho các số từ 4 trở đi */
:deep(.ant-tag-default) {
    background: linear-gradient(135deg, #f0f0f0 0%, #e0e0e0 100%);
    color: #000;
    border: 1px solid #d9d9d9;
}
</style>