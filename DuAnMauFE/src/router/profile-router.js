/**
 * Profile Routes
 * Protected routes - requires customer authentication
 */

const profileRoutes = [
    {
        path: '/profile',
        component: () => import('../views/Profile/ProfileLayout.vue'),
        meta: { requiresAuth: true }, // Protected route
        children: [
            {
                path: '',
                name: 'profile-overview',
                component: () => import('../views/Profile/ProfileOverview.vue'),
                meta: { title: 'Tài khoản của tôi' }
            },
            {
                path: 'edit',
                name: 'profile-edit',
                component: () => import('../views/Profile/ProfileEdit.vue'),
                meta: { title: 'Chỉnh sửa thông tin' }
            },
            {
                path: 'addresses',
                name: 'profile-addresses',
                component: () => import('../views/Profile/AddressList.vue'),
                meta: { title: 'Địa chỉ của tôi' }
            },
            {
                path: 'orders',
                name: 'profile-orders',
                component: () => import('../views/Profile/OrderHistory.vue'),
                meta: { title: 'Đơn hàng của tôi' }
            },
            {
                path: 'change-password',
                name: 'profile-change-password',
                component: () => import('../views/Profile/ChangePassword.vue'),
                meta: { title: 'Đổi mật khẩu' }
            }
        ]
    }
];

export default profileRoutes;
