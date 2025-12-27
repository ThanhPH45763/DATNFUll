<template>
    <div class="header-container fixed-top" @mouseout="store.hideModalSideBar(0)">
        <div class="row">
            <div class="col-12 headers d-flex align-items-center">
                <div class="logo-section col-sm-2 align-items-center">
                    <img src="../../src/images/logo/anhLogoMenWear.png" @click="chuyenTrang('/home')" class="logo-image img-fluid ms-2"
                        alt="MenWear Logo">
                </div>
                <div class="search-section col-sm-6">
                    <div class="search-container d-flex align-items-center">
                        <Search class="search-icon ms-3" />
                        <input
                            type="text"
                            v-model="searchKeyword"
                            @keyup.enter="handleSearch"
                            class="search-input form-control"
                            placeholder="Bạn đang muốn tìm kiếm gì?"
                        >
                    </div>
                    <TheHeaderSearchModal />
                </div>
                <div class="nav-icons col-sm-4 d-y-content-evenly align-items-center">
                    <div class="nav-item text-center" @click="chuyenTrang('/cuaHang')"
                        @mouseenter="animateIcon('store')">
                        <div class="icon-container">
                            <Store class="nav-icon" :class="{ 'icon-animated': animatedIcon === 'store' }" />
                        </div>
                        <span class="nav-text">Cửa hàng</span>
                    </div>
                    <div class="nav-item text-center" @click="chuyenTrang('/hoTro')"
                        @mouseenter="animateIcon('support')">
                        <div class="icon-container">
                            <MessageCircleQuestion class="nav-icon"
                                :class="{ 'icon-animated': animatedIcon === 'support' }" />
                        </div>
                        <span class="nav-text">Hỗ trợ</span>
                    </div>
                    <div class="nav-item text-center" @click="chuyenTrang('/giohang-banhang')"
                        @mouseenter="animateIcon('cart')">
                        <div class="icon-container">
                            <ShoppingCart class="nav-icon" :class="{ 'icon-animated': animatedIcon === 'cart' }" />
                            <span v-if="cartItemCount > 0" class="cart-badge">{{ cartItemCount }}</span>
                        </div>
                        <span class="nav-text">Giỏ hàng</span>
                    </div>

                    <div class="nav-item text-center" @click="chuyenTrang('/tracuudonhang-banhang')"
                        @mouseenter="animateIcon('order')">
                        <div class="icon-container">
                            <ClipboardList class="nav-icon" :class="{ 'icon-animated': animatedIcon === 'order' }" />
                        </div>
                        <span class="nav-text">Tra cứu đơn</span>
                    </div>

                    <div class="nav-item text-center user-nav-item" @mouseenter="animateIcon('user')"
                        @click="toggleUserMenu">
                        <div class="icon-container">
                            <User class="nav-icon" :class="{ 'icon-animated': animatedIcon === 'user' }" />
                        </div>
                        <span class="nav-text">{{ displayName }}</span>

                        <!-- User dropdown menu -->
                        <div v-if="(isCustomerLoggedIn || store.isLoggedIn) && showMenu" class="user-dropdown">
                            <div class="dropdown-item" @click.stop="navigateTo('/profile')">
                                <UserCircle class="dropdown-icon" />
                                <span>Tài khoản của tôi</span>
                            </div>
                            <div class="dropdown-item" @click.stop="navigateTo('/profile/orders')">
                                <ShoppingBag class="dropdown-icon" />
                                <span>Đơn hàng</span>
                            </div>
                            <div class="dropdown-item" @click.stop="navigateTo('/profile/change-password')">
                                <UserCircle class="dropdown-icon" />
                                <span>Đổi mật khẩu</span>
                            </div>
                            <div class="dropdown-divider"></div>
                            <div class="dropdown-item logout" @click.stop="handleLogout">
                                <LogOut class="dropdown-icon" />
                                <span>Đăng xuất</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { Search, User, Store, MessageCircleQuestion, ShoppingCart, UserCircle, ShoppingBag, LogOut, ClipboardList } from 'lucide-vue-next';
import { useGbStore } from '@/stores/gbStore';
import TheHeaderSearchModal from './TheHeaderSearchModal.vue';
import { ref, onMounted, watch, computed, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { banHangOnlineService } from '@/services/banHangOnlineService';

const store = useGbStore();
const animatedIcon = ref(null);
const cartItemCount = ref(0); // Số lượng sản phẩm trong giỏ hàng
const router = useRouter();
const showMenu = ref(false);
const searchKeyword = ref('');
const displayName = ref('Đăng nhập'); // Use ref instead of computed for reactivity

// Function to update display name from storage
const updateDisplayName = () => {
    // Check for customer login (khachHang)
    const khachHangStr = localStorage.getItem('khachHang') || sessionStorage.getItem('khachHang');
    if (khachHangStr) {
        try {
            const khachHang = JSON.parse(khachHangStr);
            displayName.value = khachHang.hoTen || khachHang.email || 'Khách hàng';
            console.log('Updated displayName from customer:', displayName.value);
            return;
        } catch (e) {
            console.error('Error parsing khachHang:', e);
        }
    }

    // Fallback to amin check
    if (store.isLoggedIn && store.userDetails) {
        displayName.value = store.userDetails.tenKhachHang;
        console.log('Updated displayName from admin:', displayName.value);
        return;
    }

    // Default
    displayName.value = store.changeLanguage.nguoiDung || 'Đăng nhập';
    console.log('Updated displayName to default:', displayName.value);
};

// Check if customer is logged in
const isCustomerLoggedIn = computed(() => {
    const khachHangStr = localStorage.getItem('khachHang') || sessionStorage.getItem('khachHang');
    return !!khachHangStr;
});

const chuyenTrang = (path) => {
    router.push(path);
}

const animateIcon = (iconName) => {
    animatedIcon.value = iconName;
    setTimeout(() => {
        animatedIcon.value = null;
    }, 500);
};

// Sử dụng toggle menu thay vì hover
const toggleUserMenu = () => {
    // Force fresh check from storage (không dùng computed để tránh cache)
    const khachHangStr = localStorage.getItem('khachHang') || sessionStorage.getItem('khachHang');
    const hasCustomerLogin = !!khachHangStr;

    console.log('=== Toggle User Menu Debug ===');
    console.log('Customer logged in:', hasCustomerLogin);
    console.log('Admin logged in:', store.isLoggedIn);
    console.log('khachHangStr:', khachHangStr);

    // Check customer login OR admin login
    if (!hasCustomerLogin && !store.isLoggedIn) {
        console.log('→ Redirecting to login page');
        // Use window.location for reliable redirect
        window.location.href = '/login-register/login';
    } else {
        console.log('→ Toggling menu');
        showMenu.value = !showMenu.value;
    }
};

// Sửa lại hàm xử lý đăng xuất
const handleLogout = () => {
    showMenu.value = false;

    console.log('=== Logout Debug ===');
    console.log('Clearing all auth data...');

    // Clear ALL customer auth data
    localStorage.removeItem('khachHang');
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('userInfo');
    localStorage.removeItem('userDetails');
    localStorage.removeItem('id_roles');
    localStorage.removeItem('token');

    sessionStorage.removeItem('khachHang');
    sessionStorage.removeItem('isLoggedIn');
    sessionStorage.removeItem('userInfo');
    sessionStorage.removeItem('userDetails');
    sessionStorage.removeItem('id_roles');
    sessionStorage.removeItem('token');

    // Also clear store state
    store.isLoggedIn = false;
    store.userInfo = null;
    store.userDetails = null;

    console.log('All auth data cleared. Redirecting...');

    // Force reload to ensure clean state
    window.location.href = '/home';
};

// Thêm hàm để đóng menu khi click bên ngoài
const closeMenuOnOutsideClick = (event) => {
    if (showMenu.value && !event.target.closest('.user-nav-item')) {
        showMenu.value = false;
    }
};

// Hàm tải giỏ hàng và cập nhật số lượng
const updateCartCount = async () => {
    try {
        // Kiểm tra xem khách hàng đã đăng nhập chưa
        const userDetailsStr = sessionStorage.getItem('userDetails');

        if (userDetailsStr) {
            const userDetails = JSON.parse(userDetailsStr);

            if (userDetails && userDetails.idKhachHang) {
                // Nếu đã đăng nhập, lấy giỏ hàng từ API
                const response = await banHangOnlineService.getGioHang(userDetails.idKhachHang);

                if (response && Array.isArray(response)) {
                    // Tính tổng số lượng sản phẩm từ API
                    cartItemCount.value = response.reduce((total, item) => total + (item.so_luong || 1), 0);
                    console.log('Số lượng sản phẩm trong giỏ hàng của KH đã đăng nhập:', cartItemCount.value);
                } else {
                    cartItemCount.value = 0;
                }
                return; // Kết thúc hàm sau khi đã xử lý KH đăng nhập
            }
        }

        // Nếu không đăng nhập hoặc không có idKhachHang, lấy từ localStorage
        const savedCart = localStorage.getItem('gb-sport-cart');
        if (savedCart) {
            const cartItems = JSON.parse(savedCart);
            cartItemCount.value = cartItems.reduce((total, item) => total + (item.quantity || 1), 0);
        } else {
            cartItemCount.value = 0;
        }
    } catch (error) {
        console.error('Lỗi khi cập nhật số lượng giỏ hàng:', error);
        cartItemCount.value = 0;
    }
};

// Thêm hàm mới cho việc điều hướng từ dropdown
const navigateTo = (path) => {
    showMenu.value = false; // Đóng dropdown
    chuyenTrang(path); // Chuyển trang
};

// Thêm hàm xử lý tìm kiếm
const handleSearch = async () => {
    if (!searchKeyword.value.trim()) return;

    try {
        store.isProductLoading = true;
        // Gọi API tìm kiếm và lưu kết quả vào store
        await store.getSanPhamByTenSP(searchKeyword.value);

        // Đóng modal search nếu đang mở
        store.showModal(false);

        // Điều hướng đến trang danh sách sản phẩm
        await router.push({
            path: '/danhSachSanPham',
            query: {
                filter: searchKeyword.value
            }
        });

        // Reset input sau khi tìm kiếm
        searchKeyword.value = '';
    } catch (error) {
        console.error('Lỗi khi tìm kiếm:', error);
    } finally {
        store.isProductLoading = false;
    }
};

// Cập nhật lại onMounted để thêm listener document.click
onMounted(async () => {
    // Update display name from storage on mount
    updateDisplayName();

    await updateCartCount();

    // Lắng nghe sự kiện 'cart-updated' nếu có
    window.addEventListener('cart-updated', updateCartCount);

    // Thêm lắng nghe click bên ngoài để đóng dropdown
    document.addEventListener('click', closeMenuOnOutsideClick);

    // Listen for storage changes (e.g., login/logout in another tab)
    window.addEventListener('storage', (e) => {
        if (e.key === 'khachHang' || e.key === 'isLoggedIn') {
            console.log('Storage changed, updating display name...');
            updateDisplayName();
        }
    });
});

// Làm sạch listener khi component bị hủy
onBeforeUnmount(() => {
    window.removeEventListener('cart-updated', updateCartCount);
    document.removeEventListener('click', closeMenuOnOutsideClick);
    clearInterval(checkCartInterval);
});

// Kiểm tra giỏ hàng định kỳ để đảm bảo hiển thị chính xác
const checkCartInterval = setInterval(updateCartCount, 5000);
</script>

<style scoped>
/* ===================================================
   HEADER COMPONENT - Elegant Menswear Design
   ===================================================
   Philosophy: Clean, Professional, Timeless
*/

.header-container {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: var(--z-fixed);
    background: var(--color-bg);
    box-shadow: var(--shadow-sm);
    border-bottom: 1px solid var(--color-border);
    transition: box-shadow var(--transition-base);
    font-family: var(--font-primary);
}

.header-container:hover {
    box-shadow: var(--shadow-md);
}

.headers {
    height: var(--header-height);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: var(--space-md);
    max-width: 1400px;
    margin: 0 auto;
}

/* ========== Logo Section ========== */
.logo-section {
    min-width: 180px;
    display: flex;
    align-items: center;
}

.logo-image {
    height: 3.5rem;
    width: auto;
    transition: opacity var(--transition-base);
    cursor: pointer;
}

.logo-image:hover {
    opacity: 0.85;
}

/* ========== Search Section ========== */
.search-section {
    flex: 1;
    max-width: 600px;
}

.search-container {
    display: flex;
    align-items: center;
    height: 3rem;
    background-color: var(--color-bg);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-full);
    padding: 0 1.25rem;
    transition: all var(--transition-base);
}

.search-container:focus-within {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.08);
}

.search-icon {
    color: var(--color-text-light);
    width: 20px;
    height: 20px;
    flex-shrink: 0;
    transition: color var(--transition-base);
}

.search-container:focus-within .search-icon {
    color: var(--color-primary);
}

.search-input {
    flex: 1;
    border: none;
    outline: none;
    background: transparent;
    font-family: var(--font-body);
    font-size: var(--text-base);
    color: var(--color-text);
    margin-left: var(--space-sm);
}

.search-input::placeholder {
    color: var(--color-text-muted);
}

/* ========== Navigation Icons ========== */
.nav-icons {
    display: flex;
    align-items: center;
    gap: var(--space-md);
}

.nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.25rem;
    cursor: pointer;
    padding: var(--space-sm);
    border-radius: var(--radius-md);
    transition: background-color var(--transition-base);
}

.nav-item:hover {
    background-color: var(--color-bg-alt);
}

.icon-container {
    position: relative;
}

.nav-icon {
    width: 22px;
    height: 22px;
    color: var(--color-text);
    transition: color var(--transition-base);
}

.nav-item:hover .nav-icon {
    color: var(--color-primary);
}

/* Minimal icon animation - subtle scale only */
.icon-animated {
    transform: scale(1.08);
    transition: transform var(--transition-fast);
}

.nav-text {
    font-size: var(--text-xs);
    font-weight: var(--weight-medium);
    color: var(--color-text-light);
    transition: color var(--transition-base);
    font-family: var(--font-primary);
}

.nav-item:hover .nav-text {
    color: var(--color-primary);
}

/* ========== Cart Badge ========== */
.cart-badge {
    position: absolute;
    top: -6px;
    right: -6px;
    background-color: var(--color-error);
    color: var(--color-white);
    font-size: var(--text-xs);
    font-weight: var(--weight-semibold);
    width: 18px;
    height: 18px;
    border-radius: var(--radius-full);
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: var(--font-primary);
}

/* ========== User Dropdown Menu ========== */
.user-nav-item {
    position: relative;
}

.user-dropdown {
    position: absolute;
    top: calc(100% + 0.5rem);
    right: 0;
    min-width: 200px;
    background-color: var(--color-bg);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-lg);
    overflow: hidden;
    animation: slideDown var(--transition-base);
}

@keyframes slideDown {
    from {
        opacity: 0;
        transform: translateY(-8px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.dropdown-item {
    display: flex;
    align-items: center;
    gap: var(--space-sm);
    padding: 0.875rem 1.25rem;
    cursor: pointer;
    font-size: var(--text-sm);
    color: var(--color-text);
    transition: background-color var(--transition-fast);
    border: none;
    background: none;
    font-family: var(--font-primary);
}

.dropdown-item:hover {
    background-color: var(--color-bg-alt);
}

.dropdown-icon {
    width: 18px;
    height: 18px;
    color: var(--color-text-light);
}

.dropdown-divider {
    height: 1px;
    background-color: var(--color-border);
    margin: 0.25rem 0;
}

.dropdown-item.logout {
    color: var(--color-error);
}

.dropdown-item.logout .dropdown-icon {
    color: var(--color-error);
}

.dropdown-item.logout:hover {
    background-color: rgba(239, 68, 68, 0.08);
}

/* ========== Responsive Design ========== */
@media (max-width: 1024px) {
    .nav-text {
        display: none;
    }

    .nav-item {
        padding: var(--space-sm);
    }
}

@media (max-width: 768px) {
    .header-container {
        padding: 0 1rem;
    }

    .headers {
        height: 64px;
    }

    .logo-section {
        min-width: 120px;
    }

    .logo-image {
        height: 2.5rem;
    }

    .search-section {
        max-width: none;
    }

    .search-container {
        height: 2.5rem;
        padding: 0 1rem;
    }

    .nav-icons {
        gap: var(--space-xs);
    }
}
</style>
