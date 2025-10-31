// import { name } from '@vue/eslint-config-prettier/skip-formatting';

const home = [
    {
        path: '/',
        name: 'home',
        component: () => import('../layouts/home.vue'),
        children: [
            {
                path: "home",
                name: "home-sell",
                component: () => import('../views/web/index.vue')
            },
            {
                path: "sanphamdetail/:id",
                name: "sanPhamDetail-BanHang",
                component: () => import('../views/web/sanPhamDetail-BanHang.vue'),
                meta: { scrollToTop: true }
            },
            {
                path: "thanhtoan-banhang",
                name: "thanhtoan-banhang",
                component: () => import('../views/web/thanhToan-BanHang.vue'),
                meta: { scrollToTop: true }
            },
            {
                path: "giohang-banhang",
                name: "giohang-banhang",
                component: () => import('@/views/web/gioHang-BanHang.vue')
            },
            {
                path: "tracuudonhang-banhang",
                name: "tracuudonhang-banhang",
                component: () => import('@/views/web/traCuuDonHang-BanHang.vue')
            },
            {
                path: "payment-callback",
                name: "payment-callback",
                component: () => import('@/views/web/PaymentCallback.vue'),
                meta: { scrollToTop: true }
            },
            {
                path: "khachhang",
                name: "khachhang",
                component: () => import('@/views/web/KhachHang/KhachHang.vue')
            },
            {
                path: "cuaHang",
                name: "cuaHang",
                component: () => import('@/views/web/CuaHang/CuaHang.vue')
            },
            {
                path: "hoTro",
                name: "hoTro",
                component: () => import('@/views/web/CuaHang/HoTro.vue') 
            },
            {
                path: "danhSachSanPham",
                name: "danhSachSanPham",
                component:() => import('@/views/web/SanPham.vue')
            },
            {
                path: "sanPham",
                name: "sanPham",
                component:() => import('@/views/web/SanPham.vue')
            },
            {
                path: "sanPham/:tenSanPham",
                name: "sanPham",
                component:() => import('@/views/web/SanPham.vue')
            }
        ]
    }
];

// Thêm hàm scrollBehavior để tự động cuộn lên đầu trang khi chuyển route
const scrollBehavior = (to, from, savedPosition) => {
    if (savedPosition) {
        return savedPosition;
    }

    if (to.matched.some(record => record.meta.scrollToTop)) {
        return { top: 0 };
    }

    return { top: 0 };
};

export { home, scrollBehavior };
export default home;