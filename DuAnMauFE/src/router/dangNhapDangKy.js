const dangNhapDangKy = [
    {
        path: '/login-register',
        name: 'login-register',
        component: () => import('../layouts/dangNhapDangKy.vue'),
        children: [
            {
                path: "register",
                name: "register",
                component: () => import('../views/DangNhapDangKy/viewDangKy.vue'),
                meta: { requiresGuest: true } // Redirect if logged in as customer
            },
            {
                path: "login",
                name: "login",
                component: () => import('../views/DangNhapDangKy/viewDangNhap.vue'),
                meta: { requiresGuest: true } // Redirect if logged in as customer
            },
            {
                path: "loginAdmin",
                name: "loginAdmin",
                component: () => import('../views/DangNhapDangKy/viewDNAdmin.vue')
                // NO requiresGuest - Admin login should always be accessible
            }
        ]
    }
];

export default dangNhapDangKy;