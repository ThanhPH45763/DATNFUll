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
                <span class="current">Đăng nhập</span>
            </div>
        </div>
        <!-- Form đăng nhập -->
        <div class="login-container">
            <div class="login-box">
                <div class="login-header">
                    <a href="">
                        <img src="../../images/logo/anhLogoMenWear-1-removebg-preview.png" alt="Logo M"
                            class="container-fluid d-flex justify-content-center align-items-center mb-3"
                            style="width:150px">
                    </a>

                    <p>Chào mừng đến với MenWear</p>
                </div>

                <!-- Form đăng nhập -->
                <form v-if="!showForgotPassword" @submit.prevent="handleLogin" class="login-form">
                    <div class="form-group">
                        <label for="email">
                            <i class="fas fa-envelope"></i> Email
                        </label>
                        <input type="email" id="email" v-model="email" class="form-control"
                            placeholder="Nhập email của bạn" required />
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

                    <div class="signup-prompt">
                        Bạn chưa có tài khoản? <a @click="chuyenTrang('/login-register/register')"
                            class="signup-link">Đăng ký</a>
                    </div>
                </form>

                <!-- Form quên mật khẩu -->
                <form v-else @submit.prevent="handleResetPassword" class="login-form">
                    <div class="form-group" v-if="!isTokenValid">
                        <label for="forgot-email">
                            <i class="fas fa-envelope"></i> Email
                        </label>
                        <input type="email" id="forgot-email" v-model="forgotEmail" class="form-control"
                            placeholder="Nhập email của bạn" required  />
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
                                :disabled="!isEmailChecked" required />
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
const email = ref('');
const password = ref('');
const rememberMe = ref(false);
const showPassword = ref(false);
const isLoading = ref(false);

// Các biến cho form quên mật khẩu
const showForgotPassword = ref(false);
const forgotEmail = ref('');
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
            const response = await axiosInstance.get(`api/khach-hang/reset-password?token=${resetToken.value}`);
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
        forgotEmail.value = '';
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
    if (!email.value || !password.value) {
        toast.error('Vui lòng nhập đầy đủ email và mật khẩu!');
        return;
    }

    if (password.value.includes(' ')) {
        toast.error('Mật khẩu không được chứa khoảng trắng!');
        return;
    }
    try {
        isLoading.value = true;
        const loginData = {
            email: email.value,
            password: password.value,
            rememberMe: rememberMe.value
        };
        const result = await gbStore.login(loginData);

        if (result.error) {
            if (result.fieldErrors) {
                toast.error('Vui lòng kiểm tra lại thông tin!');
            }
            return;
        }

        // Kiểm tra đăng nhập thành công và có thông tin khách hàng
        if (result.khachHang) {
            // Lưu thông tin khách hàng vào localStorage hoặc store
            localStorage.setItem('khachHang', JSON.stringify(result.khachHang));
            toast.success('Đăng nhập thành công!');
            router.push('/home');
        } else {
            toast.error('Đăng nhập thất bại!');
        }

    } catch (error) {
        toast.error('Đăng nhập thất bại. Vui lòng thử lại!');
    } finally {
        isLoading.value = false;
    }
};

const handleResetPassword = async () => {
    isLoading.value = true;

    // Bước 1: Kiểm tra email
    if (!isEmailChecked.value) {
        if (!forgotEmail.value) {
            toast.error('Vui lòng nhập email!');
            isLoading.value = false;
            return;
        }
        // if (forgotEmail.value.includes(' ')) {
        //     toast.error('Email không được chứa khoảng trắng!');
        //     isLoading.value = false;
        //     return;
        // }
        try {
            const response = await axiosInstance.post('api/khach-hang/forgot-password', {
                email: forgotEmail.value
            });

            if (response.data.successMessage) {
                // resetToken.value = response.data.resetToken;
                isEmailChecked.value = true;
                toast.success(response.data.successMessage);
                // toast.success('Email hợp lệ, vui lòng nhập mật khẩu mới.');
            }
        } catch (error) {
            toast.error(error.response?.data?.error || 'Có lỗi xảy ra khi kiểm tra email!');
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
        const response = await axiosInstance.post('api/khach-hang/reset-password', {
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
    background-image: url('../../images/anhcuahang.png');
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
            rgba(255, 255, 255, 0.7) 0%,
            rgba(255, 255, 255, 0.4) 100%);
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
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(12px);
    border-radius: 24px;
    padding: 40px;
    width: 100%;
    max-width: 480px;
    box-shadow:
        0 10px 30px rgba(0, 0, 0, 0.1),
        inset 0 0 0 1px rgba(255, 255, 255, 0.5);
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
    border: 1px solid rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(4px);
    border-radius: 12px;
    font-size: 15px;
    transition: all 0.3s ease;
    background: rgba(255, 255, 255, 0.7);
}

.form-control:focus {
    background: rgba(255, 255, 255, 0.9);
    border-color: rgba(0, 0, 0, 0.2);
    box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.3);
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
}

.signup-prompt {
    text-align: center;
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid rgba(0, 0, 0, 0.1);
    color: rgba(0, 0, 0, 0.7);
    font-size: 15px;
}

.signup-link {
    color: #000;
    font-weight: 600;
    text-decoration: none;
    transition: all 0.3s ease;
    cursor: pointer;
}

.signup-link:hover {
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
    color: #000;
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
    background-color: #000;
    transition: width 0.2s ease;
}

.breadcrumb-link:hover:after {
    width: 100%;
}

.breadcrumb-link:hover {
    opacity: 0.7;
}

.breadcrumb .separator {
    margin: 0 8px;
    color: #000;
    font-weight: normal;
}

.breadcrumb .current {
    color: #000;
    font-weight: 600;
}

@media (max-width: 480px) {
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
    background-color: #ff6600;
    color: #fff;
    padding: 10px 20px;
    border: none;
    border-radius: 5px;
    cursor: pointer;
}

.action-button:hover {
    background-color: #e55a00;
}

.action-button:disabled {
    background-color: #999;
    cursor: not-allowed;
}
</style>
