const dangNhapDangKy = [
    {
        path: '/login-register',
        name: 'login-register',
        component: () => import('../layouts/dangNhapDangKy.vue'),
        children: [
            {
                path: "register",
                name: "register",
                component: () => import('../views/DangNhapDangKy/viewDangKy.vue')
            },
            {
                path: "login",
                name: "login",
                component: () => import('../views/DangNhapDangKy/viewDangNhap.vue')
            },
            {
                path: "loginAdmin",
                name: "loginAdmin",
                component: () => import('../views/DangNhapDangKy/viewDNAdmin.vue')
            }
        ]
    }
];

export default dangNhapDangKy;