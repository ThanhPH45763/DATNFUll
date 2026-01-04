<template>
    <div class="login-wrapper">
        <!-- Background với overlay gradient -->
        <div class="background-container">
            <div class="background-overlay"></div>
        </div>

        <div class="breadcrumb">
            <div class="breadcrumb-content">
                <a @click="chuyenTrang('/home')" class="breadcrumb-link" style="cursor: pointer;">Trang chủ</a>
                <span class="separator">/</span>
                <span class="current">Đăng nhập quản trị</span>
            </div>
        </div>
        <!-- Form đăng nhập -->
        <div class="login-container">
            <div class="login-box">
                <div class="login-header">
                    <a href="">
                        <img src="../../images/logo/anhLogoMenWear-1-removebg-preview.png" alt="Logo"
                            class="container-fluid d-flex justify-content-center align-items-center mb-3"
                            style="width:150px">
                    </a>

                    <p>Đăng nhập hệ thống quản trị 🔐</p>
                </div>

                <!-- Form đăng nhập -->
                <form v-if="!showForgotPassword" @submit.prevent="handleLogin" class="login-form">
                    <div class="form-group">
                        <label for="username">
                            <i class="fas fa-user"></i> Tên đăng nhập
                        </label>
                        <input type="text" id="username" v-model="username" class="form-control"
                            placeholder="Nhập tên đăng nhập" required />
                    </div>

                    <div class="form-group">
                        <label for="password">
                            <i class="fas fa-lock"></i> Mật khẩu
                        </label>
                        <div class="password-input">
                            <input :type="showPassword ? 'text' : 'password'" id="password" v-model="password"
                                class="form-control" placeholder="Nhập mật khẩu" required />
                            <span class="password-toggle" @click="togglePassword">
                                <EyeInvisibleOutlined v-if="!showPassword" />
                                <EyeOutlined v-else />
                            </span>
                        </div>
                    </div>

                    <div class="form-options">
                        <label class="remember-me">
                            <input type="checkbox" v-model="rememberMe" />
                            <span>Ghi nhớ đăng nhập</span>
                        </label>
                        <a href="#" class="forgot-password" @click.prevent="toggleForgotPassword">Quên mật khẩu?</a>
                    </div>

                    <button type="submit" class="login-button" :disabled="isLoading">
                        <span v-if="!isLoading">Đăng nhập</span>
                        <span v-else class="loading-spinner"></span>
                    </button>

                    <div class="customer-login-prompt">
                        <a @click="chuyenTrang('/login-register/login')" class="customer-link">
                            Đăng nhập dành cho khách hàng
                        </a>
                    </div>
                </form>

                <!-- Form quên mật khẩu -->
                <form v-else @submit.prevent="handleResetPassword" class="login-form">
                    <div class="form-group" v-if="!isTokenValid">
                        <label for="forgot-username">
                            <i class="fas fa-user"></i> Tên đăng nhập
                        </label>
                        <input type="text" id="forgot-username" v-model="forgotUsername" class="form-control"
                            placeholder="Nhập tên đăng nhập" required :disabled="isEmailChecked" />
                    </div>

                    <div class="form-group">
                        <label for="new-password">
                            <i class="fas fa-lock"></i> Mật khẩu mới
                        </label>
                        <div class="password-input">
                            <input :type="showNewPassword ? 'text' : 'password'" id="new-password" v-model="newPassword"
                                class="form-control" placeholder="Nhập mật khẩu mới" :disabled="!isTokenValid"
                                required />
                            <span class="password-toggle" @click="toggleNewPassword">
                                <EyeInvisibleOutlined v-if="!showNewPassword" />
                                <EyeOutlined v-else />
                            </span>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="confirm-password">
                            <i class="fas fa-lock"></i> Nhập lại mật khẩu mới
                        </label>
                        <div class="password-input">
                            <input :type="showConfirmPassword ? 'text' : 'password'" id="confirm-password"
                                v-model="confirmPassword" class="form-control" placeholder="Nhập lại mật khẩu mới"
                                :disabled="!isTokenValid" required />
                            <span class="password-toggle" @click="toggleConfirmPassword">
                                <EyeInvisibleOutlined v-if="!showConfirmPassword" />
                                <EyeOutlined v-else />
                            </span>
                        </div>
                    </div>

                    <div class="form-options">
                        <button type="button" class="cancel-button" @click="toggleForgotPassword">Hủy</button>
                        <button type="submit" class="action-button" :disabled="isLoading">
                            <span v-if="!isEmailChecked">Kiểm tra</span>
                            <span v-else>Xác nhận</span>
                            <span v-if="isLoading" class="loading-spinner"></span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { toast } from 'vue3-toastify';
import { EyeOutlined, EyeInvisibleOutlined } from '@ant-design/icons-vue';
import { useGbStore } from '@/stores/gbStore';
import axiosInstance from "@/config/axiosConfig";

const router = useRouter();
const gbStore = useGbStore();
const username = ref('');
const password = ref('');
const rememberMe = ref(false);
const showPassword = ref(false);
const isLoading = ref(false);

// Các biến cho form quên mật khẩu
const showForgotPassword = ref(false);
const forgotUsername = ref('');
const newPassword = ref('');
const confirmPassword = ref('');
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);
const isEmailChecked = ref(false);
const isTokenValid = ref(false);
const resetToken = ref('');

// Khôi phục trạng thái đăng nhập khi component được tạo
gbStore.restoreLoginState();

// Kiểm tra token từ query parameter khi component được mounted
onMounted(async () => {
    const urlParams = new URLSearchParams(window.location.search);
    resetToken.value = urlParams.get('token');

    if (resetToken.value) {
        showForgotPassword.value = true; // Mở form quên mật khẩu
        isEmailChecked.value = true; // Giả lập đã kiểm tra email

        try {
            isLoading.value = true;
            const response = await axiosInstance.get(`admin/quan-ly-nhan-vien/reset-password?token=${resetToken.value}`);
            if (response.data.successMessage) {
                isTokenValid.value = true;
                toast.success(response.data.successMessage);
            }
        } catch (error) {
            isTokenValid.value = false;
            toast.error(error.response?.data?.error || 'Token không hợp lệ!');
        } finally {
            isLoading.value = false;
        }
    }
});

const chuyenTrang = (path) => {
    router.push(path);
};

const togglePassword = () => {
    showPassword.value = !showPassword.value;
};

const toggleNewPassword = () => {
    showNewPassword.value = !showNewPassword.value;
};

const toggleConfirmPassword = () => {
    showConfirmPassword.value = !showConfirmPassword.value;
};

const toggleForgotPassword = () => {
    showForgotPassword.value = !showForgotPassword.value;
    // Reset form khi hủy
    if (!showForgotPassword.value) {
        forgotUsername.value = '';
        newPassword.value = '';
        confirmPassword.value = '';
        isEmailChecked.value = false;
        isTokenValid.value = false;
        resetToken.value = '';
        router.replace({ query: {} }); // Xóa token trong URL
    }
};

const handleLogin = async () => {
    // Kiểm tra đầu vào
    if (!username.value || !password.value) {
        toast.error('Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!');
        return;
    }

    if (password.value.includes(' ')) {
        toast.error('Mật khẩu không được chứa khoảng trắng!');
        return;
    }

    // Logic đăng nhập "cứng" cho admin
    if (username.value === 'admin' && password.value === 'admin') {
        sessionStorage.setItem('isLoggedIn', 'true');
        sessionStorage.setItem('id_roles', '1'); // Giả sử role 1 là Admin
        toast.success('Đăng nhập với tư cách Admin thành công!');
        router.push('/admin');
        return;
    }

    try {
        isLoading.value = true;
        const loginData = {
            email: username.value,
            password: password.value,
            rememberMe: rememberMe.value
        };

        const result = await gbStore.loginNV(loginData);

        if (result.error) {
            if (result.fieldErrors) {
                toast.error('Vui lòng kiểm tra lại thông tin!');
            }
            return;
        }

        // Kiểm tra role
        if (result.id_roles === 4) {
            toast.error('Tài khoản không có quyền truy cập hệ thống quản trị!');
            return;
        }

        // Chuyển đến trang phù hợp với role
        console.log('Đăng nhập thành công với vai trò:', result.id_roles);

        // Nhân viên (role 3) chuyển thẳng đến trang bán hàng
        if (result.id_roles === 3) {
            router.push('/admin/banhang');
        } else {
            // Admin và quản lý đến trang chính
            router.push('/admin');
        }
    } catch (error) {
        toast.error('Đăng nhập thất bại. Vui lòng thử lại!');
    } finally {
        isLoading.value = false;
    }
};

const handleResetPassword = async () => {
    isLoading.value = true;

    // Bước 1: Kiểm tra username (email)
    if (!isEmailChecked.value) {
        if (!forgotUsername.value) {
            toast.error('Vui lòng nhập tên đăng nhập!');
            isLoading.value = false;
            return;
        }
        // if (forgotUsername.value.includes(' ')) {
        //     toast.error('Tên đăng nhập không tồn tại!');
        //     isLoading.value = false;
        //     return;
        // }
        try {
            const response = await axiosInstance.post('admin/quan-ly-nhan-vien/forgot-password', {
                email: forgotUsername.value
            });

            if (response.data.successMessage) {
                // resetToken.value = response.data.resetToken;
                isEmailChecked.value = true;
                toast.success(response.data.successMessage);
                // toast.success('Tên đăng nhập hợp lệ, vui lòng nhập mật khẩu mới.');
            }
        } catch (error) {
            toast.error(error.response?.data?.error || 'Có lỗi xảy ra khi kiểm tra tên đăng nhập!');
        } finally {
            isLoading.value = false;
        }
        return;
    }

    // Bước 2: Xác nhận đặt lại mật khẩu
    if (!isTokenValid.value) {
        toast.error('Vui lòng kiểm tra gmail để xác nhận đặt lại mật khẩu!');
        isLoading.value = false;
        return;
    }
    if (!newPassword.value || !confirmPassword.value) {
        toast.error('Vui lòng nhập đầy đủ mật khẩu mới và xác nhận!');
        isLoading.value = false;
        return;
    }
    if (newPassword.value.includes(' ')) {
        toast.error('Mật khẩu mới không được chứa khoảng trắng!');
        isLoading.value = false;
        return;
    }
    if (newPassword.value.length < 6) {
        toast.error('Mật khẩu mới phải có ít nhất 6 ký tự!');
        isLoading.value = false;
        return;
    }
    if (newPassword.value.length > 20) {
        toast.error('Mật khẩu mới không được vượt quá 20 ký tự!');
        isLoading.value = false;
        return;
    }
    if (newPassword.value !== confirmPassword.value) {
        toast.error('Mật khẩu mới và mật khẩu xác nhận không khớp!');
        isLoading.value = false;
        return;
    }

    try {
        const response = await axiosInstance.post('admin/quan-ly-nhan-vien/reset-password', {
            token: resetToken.value,
            newPassword: newPassword.value
        });

        if (response.data.successMessage) {
            toast.success('Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.');
            toggleForgotPassword(); // Ẩn form quên mật khẩu
        }
    } catch (error) {
        toast.error(error.response?.data?.error || 'Có lỗi xảy ra khi đặt lại mật khẩu!');
    } finally {
        isLoading.value = false;
    }
};
</script>

<style scoped>
.login-wrapper {
    position: relative;
    min-height: 100vh;
    overflow: hidden;
}

.background-container {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    /* Thay đổi ảnh nền phù hợp với admin */
    background-image: url('https://img.freepik.com/free-photo/business-concept-with-view-glasses_23-2149666090.jpg');
    background-size: cover;
    background-position: center;
    z-index: 1;
}

.background-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg,
            rgba(0, 0, 0, 0.7) 0%,
            rgba(0, 0, 0, 0.5) 100%);
    backdrop-filter: blur(2px);
    z-index: 2;
}

.login-container {
    position: relative;
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 3;
    padding: 20px;
}

.login-box {
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(12px);
    border-radius: 24px;
    padding: 40px;
    width: 100%;
    max-width: 480px;
    box-shadow:
        0 10px 30px rgba(0, 0, 0, 0.2),
        inset 0 0 0 1px rgba(255, 255, 255, 0.7);
    animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(-20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.login-header {
    text-align: center;
    margin-bottom: 40px;
}

.login-header h2 {
    color: #000000;
    font-size: 28px;
    margin-bottom: 10px;
}

.login-header p {
    color: #333333;
    font-size: 16px;
}

.form-group {
    margin-bottom: 24px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    color: rgba(0, 0, 0, 0.8);
    font-weight: 600;
}

.form-control {
    width: 100%;
    padding: 14px 18px;
    border: 1px solid rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(4px);
    border-radius: 12px;
    font-size: 15px;
    transition: all 0.3s ease;
    background: rgba(255, 255, 255, 0.9);
}

.form-control:focus {
    background: rgba(255, 255, 255, 1);
    border-color: rgba(0, 0, 0, 0.3);
    box-shadow: 0 0 0 4px rgba(0, 0, 0, 0.1);
}

.password-input {
    position: relative;
}

.password-toggle {
    position: absolute;
    right: 16px;
    top: 50%;
    transform: translateY(-50%);
    cursor: pointer;
    background: rgba(0, 0, 0, 0.05);
    color: rgba(0, 0, 0, 0.6);
    z-index: 10;
    width: 35px;
    height: 35px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: all 0.3s ease;
}

.password-toggle:hover {
    background: rgba(0, 0, 0, 0.1);
    color: rgba(0, 0, 0, 0.8);
}

.password-toggle i {
    font-size: 18px;
}

.password-input .form-control {
    padding-right: 55px;
}

.form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.remember-me {
    display: flex;
    align-items: center;
    gap: 8px;
    color: rgba(0, 0, 0, 0.7);
    cursor: pointer;
}

.forgot-password {
    color: rgba(0, 0, 0, 0.7);
    text-decoration: none;
    font-weight: 500;
    transition: all 0.3s ease;
}

.forgot-password:hover {
    color: rgba(0, 0, 0, 0.9);
    text-decoration: underline;
}

.login-button {
    width: 100%;
    padding: 14px;
    background: rgba(0, 0, 0, 0.8);
    backdrop-filter: blur(4px);
    color: white;
    border: none;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
}

.login-button:hover {
    background: rgba(0, 0, 0, 0.9);
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.login-button:active {
    transform: translateY(0);
}

.login-button:disabled {
    background: #cccccc;
    cursor: not-allowed;
    transform: none;
    box-shadow: none;
}

.loading-spinner {
    display: inline-block;
    width: 20px;
    height: 20px;
    border: 3px solid #ffffff;
    border-radius: 50%;
    border-top-color: transparent;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

/* Custom checkbox style */
input[type="checkbox"] {
    width: 16px;
    height: 16px;
    margin-right: 8px;
    accent-color: #000000;
}

.customer-login-prompt {
    text-align: center;
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid rgba(0, 0, 0, 0.1);
    color: rgba(0, 0, 0, 0.7);
    font-size: 15px;
}

.customer-link {
    color: #000;
    font-weight: 600;
    text-decoration: none;
    transition: all 0.3s ease;
    cursor: pointer;
}

.customer-link:hover {
    opacity: 0.7;
    text-decoration: underline;
}

.breadcrumb {
    position: absolute;
    top: 30px;
    left: 30px;
    z-index: 10;
}

.breadcrumb-content {
    font-size: 15px;
    display: flex;
    align-items: center;
}

.breadcrumb-link {
    color: #fff;
    text-decoration: none;
    font-weight: normal;
    transition: all 0.2s ease;
    position: relative;
}

.breadcrumb-link:after {
    content: '';
    position: absolute;
    width: 0;
    height: 1px;
    bottom: -2px;
    left: 0;
    background-color: #fff;
    transition: width 0.2s ease;
}

.breadcrumb-link:hover:after {
    width: 100%;
}

.breadcrumb-link:hover {
    opacity: 0.8;
}

.breadcrumb .separator {
    margin: 0 8px;
    color: #fff;
    font-weight: normal;
}

.breadcrumb .current {
    color: #fff;
    font-weight: 600;
}

@media (max-width: 480px) {
    .login-box {
        padding: 24px;
        margin: 20px;
    }

    .login-header h2 {
        font-size: 24px;
    }

    .form-options {
        flex-direction: column;
        gap: 12px;
        align-items: flex-start;
    }

    .breadcrumb {
        top: 20px;
        left: 20px;
    }
}
/* Thêm style cho form quên mật khẩu */

.cancel-button {
    background-color: #ccc;
    color: black;
    padding: 10px 20px;
    border: none;
    border-radius: 5px;
    cursor: pointer;
}

.cancel-button:hover {
    background-color: #bbb;
}

.action-button {
    background-color: #d02c39;
    color: #fff;
    padding: 10px 20px;
    border: none;
    border-radius: 5px;
    cursor: pointer;
}

.action-button:hover {
    background-color: #b02330;
}

.action-button:disabled {
    background-color: #999;
    cursor: not-allowed;
}
</style>
