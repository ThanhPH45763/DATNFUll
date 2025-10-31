<template>
    <div class="header-container fixed-top" @mouseout="store.hideModalSideBar(0)">
        <div class="row">
            <div class="col-12 headers d-flex align-items-center">
                <div class="logo-section col-sm-2 align-items-center">
                    <img src="../../public/image/logo/logoGB.png" @click="chuyenTrang('/home')" class="logo-image img-fluid ms-2"
                        alt="GB Sports Logo">
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
                <div class="nav-icons col-sm-4 d-flex justify-content-evenly align-items-center">
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
                        <span class="nav-text">{{ displayName }} </span>

                        <!-- User dropdown menu -->
                        <div v-if="store.isLoggedIn && showMenu" class="user-dropdown">
                            <div class="dropdown-item" @click.stop="navigateTo('/khachhang')">
                                <UserCircle class="dropdown-icon" />
                                <span>Hồ sơ của tôi</span>
                            </div>
                            <div class="dropdown-item" @click.stop="navigateTo('/khachhang?tab=orders')">
                                <ShoppingBag class="dropdown-icon" />
                                <span>Đơn mua</span>
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

const displayName = computed(() => {
    if (store.isLoggedIn && store.userDetails || sessionStorage.getItem('userDetails')) {
        return store.userDetails.tenKhachHang;
    }
    return store.changeLanguage.nguoiDung || 'Đăng nhập';
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
    if (!store.isLoggedIn) {
        chuyenTrang('/login-register/login');
    } else {
        showMenu.value = !showMenu.value;
    }
};

// Sửa lại hàm xử lý đăng xuất
const handleLogout = () => {
    showMenu.value = false;
    store.logoutKH();

    // Cách 1: Sử dụng window.location để làm mới hoàn toàn trang
    window.location.href = '/home';

    // HOẶC Cách 2: Sử dụng router của Vue, nhưng có thể cần reload sau đó
    // router.push('/home');
    // setTimeout(() => window.location.reload(), 100);
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
    await updateCartCount();

    // Lắng nghe sự kiện 'cart-updated' nếu có
    window.addEventListener('cart-updated', updateCartCount);

    // Thêm lắng nghe click bên ngoài để đóng dropdown
    document.addEventListener('click', closeMenuOnOutsideClick);
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
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&display=swap');

.header-container {
    padding: 0 2rem;
    box-shadow: 0 2px 15px rgba(0, 0, 0, 0.08);
    background: linear-gradient(to right, #ffffff, #f8f9fa);
    z-index: 1030;
    width: 100%;
}

.headers {
    height: 5rem;
    background-color: transparent;
    position: relative;
}

.logo-image {
    height: 4rem;
    width: auto;
    transition: transform 0.3s ease;
}

.logo-image:hover {
    transform: scale(1.05);
}

.language-selector {
    position: relative;
    cursor: pointer;
    padding: 5px 10px;
    border-radius: 20px;
    transition: all 0.3s ease;
}

.langue {
    font-size: 1.1rem;
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    color: #333;
    transition: all 0.3s ease;
}

.language-selector:hover {
    background-color: #3a86ff;
}

.language-selector:hover .langue {
    color: white;
}

.search-container {
    height: 3rem;
    border: 1px solid #e0e0e0;
    border-radius: 30px;
    overflow: hidden;
    transition: all 0.3s ease;
    background-color: #fff;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.search-container:focus-within {
    border-color: #3a86ff;
    box-shadow: 0 0 0 3px rgba(58, 134, 255, 0.2);
    transform: translateY(-2px);
}

.search-icon {
    color: #666;
    transition: color 0.3s ease;
}

.search-container:focus-within .search-icon {
    color: #3a86ff;
}

.search-input {
    border: none;
    outline: none;
    box-shadow: none;
    height: 100%;
    font-family: 'Montserrat', sans-serif;
    font-size: 0.9rem;
    transition: background-color 0.3s ease;
}

.search-input:focus {
    background-color: transparent;
}

.nav-icons {
    display: flex;
    justify-content: space-around;
}

.nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    transition: transform 0.2s ease;
    padding: 5px;
    border-radius: 8px;
}

.nav-item:hover {
    transform: translateY(-3px);
}

.icon-container {
    position: relative;
    margin-bottom: 5px;
}

.nav-icon {
    width: 24px;
    height: 24px;
    color: #333;
    transition: all 0.3s ease;
}

.nav-item:hover .nav-icon {
    color: #3a86ff;
}

.icon-animated {
    animation: pulse 0.5s ease;
}

@keyframes pulse {
    0% {
        transform: scale(1);
    }

    50% {
        transform: scale(1.2);
    }

    100% {
        transform: scale(1);
    }
}

.nav-text {
    font-size: 11px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 500;
    color: #555;
    transition: color 0.3s ease;
}

.nav-item:hover .nav-text {
    color: #3a86ff;
}

.cart-badge {
    position: absolute;
    top: -8px;
    right: -8px;
    background-color: #ff3a3a;
    color: white;
    font-size: 10px;
    font-weight: bold;
    width: 18px;
    height: 18px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    animation: bounce 1s infinite;
}

@keyframes bounce {

    0%,
    100% {
        transform: translateY(0);
    }

    50% {
        transform: translateY(-3px);
    }
}

.user-nav-item {
    position: relative;
}

.user-dropdown {
    position: absolute;
    top: 100%;
    right: 0;
    width: 180px;
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    margin-top: 5px;
    z-index: 1031;
    overflow: hidden;
    animation: dropdown-fade 0.2s ease;
}

@keyframes dropdown-fade {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.dropdown-item {
    display: flex;
    align-items: center;
    padding: 10px 14px;
    cursor: pointer;
    transition: background-color 0.2s;
    font-size: 13px;
}

.dropdown-item:hover {
    background-color: #f5f5f5;
}

.dropdown-icon {
    width: 18px;
    height: 18px;
    color: #555;
    margin-right: 12px;
}

.dropdown-divider {
    height: 1px;
    background-color: #eee;
    margin: 5px 0;
}

.logout {
    color: #ff3a3a;
}

.logout .dropdown-icon {
    color: #ff3a3a;
}
</style>