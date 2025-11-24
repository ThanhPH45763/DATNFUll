/**
 * Authentication Guard for Vue Router
 * Kiểm tra đăng nhập trước khi truy cập các route cần bảo vệ
 */

/**
 * Check if user is authenticated
 * @returns {boolean}
 */
export const isAuthenticated = () => {
    // Kiểm tra localStorage hoặc sessionStorage
    const userInfo = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo')
    const khachHang = localStorage.getItem('khachHang') || sessionStorage.getItem('khachHang')

    return !!(userInfo || khachHang)
}

/**
 * Get current user info
 * @returns {Object|null}
 */
export const getCurrentUser = () => {
    try {
        const userInfo = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo')
        const khachHang = localStorage.getItem('khachHang') || sessionStorage.getItem('khachHang')

        if (khachHang) {
            return JSON.parse(khachHang)
        }

        if (userInfo) {
            return JSON.parse(userInfo)
        }

        return null
    } catch (error) {
        console.error('Error parsing user info:', error)
        return null
    }
}

/**
 * Auth Guard - Middleware cho router
 * Chặn truy cập nếu chưa đăng nhập
 */
export const authGuard = (to, from, next) => {
    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!isAuthenticated()) {
            // Chưa đăng nhập → redirect về trang login
            next({
                path: '/login-register/login',
                query: { redirect: to.fullPath } // Lưu đường dẫn để redirect sau khi login
            })
        } else {
            // Đã đăng nhập → cho phép truy cập
            next()
        }
    } else {
        // Route không cần auth → cho phép truy cập
        next()
    }
}

/**
 * Guest Guard - Middleware cho các trang login/register
 * Redirect về home nếu đã đăng nhập
 */
export const guestGuard = (to, from, next) => {
    if (to.matched.some(record => record.meta.requiresGuest)) {
        if (isAuthenticated()) {
            // Đã đăng nhập → redirect về home
            next({ path: '/home' })
        } else {
            // Chưa đăng nhập → cho phép truy cập
            next()
        }
    } else {
        next()
    }
}

/**
 * Admin Guard - Middleware cho admin routes
 */
export const adminGuard = (to, from, next) => {
    if (to.matched.some(record => record.meta.requiresAdmin)) {
        const user = getCurrentUser()

        if (!user) {
            next({ path: '/login-register/loginAdmin' })
            return
        }

        // Kiểm tra role admin (id_roles: 1, 2, 3 là admin/nhân viên)
        if (user.id_roles && [1, 2, 3].includes(user.id_roles)) {
            next()
        } else {
            // Không phải admin → redirect về home
            next({ path: '/home' })
        }
    } else {
        next()
    }
}

/**
 * Combined Router Guards
 * Sử dụng tất cả guards
 */
export const setupRouterGuards = (router) => {
    router.beforeEach((to, from, next) => {
        // Check admin guard first
        if (to.matched.some(record => record.meta.requiresAdmin)) {
            return adminGuard(to, from, next)
        }

        // Check guest guard (login/register pages)
        if (to.matched.some(record => record.meta.requiresGuest)) {
            return guestGuard(to, from, next)
        }

        // Check auth guard (protected pages)
        if (to.matched.some(record => record.meta.requiresAuth)) {
            return authGuard(to, from, next)
        }

        // No guard needed
        next()
    })

    // Optional: Restore scroll position
    router.afterEach((to, from) => {
        // Scroll to top khi chuyển route
        window.scrollTo(0, 0)
    })
}

export default {
    isAuthenticated,
    getCurrentUser,
    authGuard,
    guestGuard,
    adminGuard,
    setupRouterGuards
}
