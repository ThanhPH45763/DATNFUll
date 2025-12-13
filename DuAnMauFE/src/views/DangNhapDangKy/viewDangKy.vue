<template>
    <div class="login-wrapper">
        <div class="background-container">
            <div class="background-overlay"></div>
        </div>

        <div class="breadcrumb">
            <div class="breadcrumb-content">
                <a @click="chuyenTrang('/home')" style="cursor: pointer; text-decoration: none;"
                    class="breadcrumb-link">Trang chủ</a>
                <span class="separator">/</span>
                <span class="current">Đăng ký</span>
            </div>
        </div>

        <div class="login-container">
            <div class="login-box">
                <div class="login-header">
                    <a href="">
                        <img src="../../images/logo/LogoM.png" alt="Logo MenWear"
                            class="container-fluid d-flex justify-content-center align-items-center mb-3"
                            style="width:150px">
                    </a>
                    <p>Chào mừng bạn đến với G&B SPORTS! 👋</p>
                </div>

                <form @submit.prevent="handleRegister" class="login-form">
                    <!-- Họ và tên -->
                    <div class="form-group">
                        <label for="fullName">
                            <i class="fas fa-user"></i> Họ và tên
                        </label>
                        <input type="text" id="fullName" v-model="form.fullName" class="form-control"
                            placeholder="Nhập họ và tên của bạn" required @blur="validateFullName" />
                        <span class="error-message" v-if="errors.fullName">{{ errors.fullName }}</span>
                    </div>

                    <!-- Ngày sinh -->
                    <div class="form-group">
                        <label for="birthDate">
                            <i class="fas fa-birthday-cake"></i> Ngày sinh
                        </label>
                        <input type="date" id="birthDate" v-model="form.birthDate" class="form-control" required
                            @blur="validateBirthDate" />
                        <span class="error-message" v-if="errors.birthDate">{{ errors.birthDate }}</span>
                    </div>

                    <!-- Số điện thoại -->
                    <div class="form-group">
                        <label for="phone">
                            <i class="fas fa-phone"></i> Số điện thoại
                        </label>
                        <input type="tel" id="phone" v-model="form.phone" class="form-control"
                            placeholder="Nhập số điện thoại" required @blur="validatePhone" />
                        <span class="error-message" v-if="errors.phone">{{ errors.phone }}</span>
                    </div>


                    <!-- Giới tính -->
                    <div class="form-group">
                        <label class="d-block">
                            <i class="fas fa-venus-mars"></i> Giới tính
                        </label>
                        <div class="gender-options">
                            <label class="gender-option">
                                <input type="radio" v-model="form.gender" value="Nam" required>
                                <span class="ms-1">Nam</span>
                            </label>
                            <label class="gender-option">
                                <input type="radio" v-model="form.gender" value="Nữ" required>
                                <span class="ms-1">Nữ</span>
                            </label>
                            <label class="gender-option">
                                <input type="radio" v-model="form.gender" value="Khác" required>
                                <span class="ms-1">Khác</span>
                            </label>
                            <span class="error-message" v-if="errors.gender">{{ errors.gender }}</span>
                        </div>
                    </div>

                    <!-- Email -->
                    <div class="form-group">
                        <label for="email">
                            <i class="fas fa-envelope"></i> Email
                        </label>
                        <input type="email" id="email" v-model="form.email" class="form-control"
                            placeholder="Nhập email của bạn" required @blur="validateEmail" />
                        <span class="error-message" v-if="errors.email">{{ errors.email }}</span>
                    </div>

                    <!-- Mật khẩu -->
                    <div class="form-group">
                        <label for="password">
                            <i class="fas fa-lock"></i> Mật khẩu
                        </label>
                        <div class="password-input">
                            <input :type="showPassword ? 'text' : 'password'" id="password" v-model="form.password"
                                class="form-control" placeholder="Nhập mật khẩu" required @blur="validatePassword" />
                            <span class="password-toggle" @click="togglePassword">
                                <EyeInvisibleOutlined v-if="!showPassword" />
                                <EyeOutlined v-else />
                            </span>
                        </div>
                        <span class="error-message" v-if="errors.password">{{ errors.password }}</span>
                    </div>

                    <!-- Xác nhận mật khẩu -->
                    <div class="form-group">
                        <label for="confirmPassword">
                            <i class="fas fa-lock"></i> Xác nhận mật khẩu
                        </label>
                        <div class="password-input">
                            <input :type="showConfirmPassword ? 'text' : 'password'" id="confirmPassword"
                                v-model="form.confirmPassword" class="form-control" placeholder="Nhập lại mật khẩu"
                                required @blur="validateConfirmPassword" />
                            <span class="password-toggle" @click="toggleConfirmPassword">
                                <EyeInvisibleOutlined v-if="!showConfirmPassword" />
                                <EyeOutlined v-else />
                            </span>
                        </div>
                        <span class="error-message" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</span>
                    </div>

                    <button type="submit" class="login-button" :disabled="isLoading || hasErrors">
                        <span v-if="!isLoading">Đăng ký</span>
                        <span v-else class="loading-spinner"></span>
                    </button>

                    <div class="signup-prompt">
                        Bạn đã có tài khoản? <a @click="chuyenTrang('/login-register/login')" style="cursor: pointer;"
                            class="signup-link">Đăng nhập</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { toast } from 'vue3-toastify';
import { EyeOutlined, EyeInvisibleOutlined } from '@ant-design/icons-vue';
import { useGbStore } from '@/stores/gbStore';

const router = useRouter();
const gbStore = useGbStore();
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const isLoading = ref(false);

const chuyenTrang = (path) => {
    router.push(path);
}


const form = ref({
    fullName: '',
    phone: '',
    birthDate: '',
    gender: '',
    address: '',
    email: '',
    password: '',
    confirmPassword: ''
});

const errors = ref({
    fullName: '',
    phone: '',
    birthDate: '',
    // address: '',
    gender: '',
    email: '',
    password: '',
    confirmPassword: ''
});

const hasErrors = computed(() => {
    return Object.values(errors.value).some(error => error !== '');
});

const validateFullName = () => {
    const fullName = form.value.fullName.trim();
    const nameParts = fullName.split(/\s+/);

    if (fullName.length < 4) {
        errors.value.fullName = 'Họ tên phải có ít nhất 4 ký tự';
    } else if (fullName.length > 50) {
        errors.value.fullName = 'Họ tên không được vượt quá 50 ký tự';
    } else if (nameParts.length < 2) {
        errors.value.fullName = 'Vui lòng nhập đầy đủ họ và tên (VD: Nguyễn Văn A)';
    } else if (!/^[\p{L}]+([\s][\p{L}]+)*$/u.test(fullName)) {
        errors.value.fullName = 'Họ tên không hợp lệ (chỉ bao gồm chữ cái và khoảng trắng)';
    } else {
        errors.value.fullName = '';
    }
};

const validatePhone = () => {
    const phoneRegex = /^(84|0[3|5|7|8|9])[0-9]{8}$/;
    if (!phoneRegex.test(form.value.phone)) {
        errors.value.phone = 'Số điện thoại không hợp lệ (VD: 0912345678)';
    } else {
        errors.value.phone = '';
    }
};

const validateGender = () => {
    if (form.value.gender === '') {
        errors.value.gender = 'Vui lòng chọn giới tính';
    } else {
        errors.value.gender = '';
    }
}

const validateBirthDate = () => {
    const today = new Date();
    const birthDate = new Date(form.value.birthDate);
    if (isNaN(birthDate.getTime())) {
        errors.value.birthDate = 'Ngày sinh không hợp lệ';
        return;
    }
    // Tính tuổi chính xác
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    const dayDiff = today.getDate() - birthDate.getDate();

    if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
        age--;
    }
    if (age < 0) {
        errors.value.birthDate = 'Ngày sinh không hợp lệ';
    } else if (age < 14) {
        errors.value.birthDate = 'Bạn phải đủ 14 tuổi để đăng ký tài khoản';
    } else {
        errors.value.birthDate = '';
    }
};

const validateAddress = () => {
    if (form.value.address.length < 10) {
        errors.value.address = 'Vui lòng nhập địa chỉ đầy đủ';
    } else {
        errors.value.address = '';
    }
};

const validateEmail = () => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(form.value.email)) {
        errors.value.email = 'Email không hợp lệ';
    } else {
        errors.value.email = '';
    }
};

const validatePassword = () => {
    const password = form.value.password;
    if (password.length < 6) {
        errors.value.password = 'Mật khẩu phải có ít nhất 6 ký tự';
    } else if (password.length > 20) {
        errors.value.password = 'Mật khẩu không được vuợt quá 20 ký tự';
    } else if (/\s/.test(password)) {
        errors.value.password = 'Mật khẩu không được chứa khoảng trắng';
    } else {
        errors.value.password = '';
    }
};

const validateConfirmPassword = () => {
    if (form.value.password !== form.value.confirmPassword) {
        errors.value.confirmPassword = 'Mật khẩu xác nhận không khớp';
    } else {
        errors.value.confirmPassword = '';
    }
};

const togglePassword = () => {
    showPassword.value = !showPassword.value;
};

const toggleConfirmPassword = () => {
    showConfirmPassword.value = !showConfirmPassword.value;
};

const handleRegister = async () => {
    // Validate all fields
    validateFullName();
    validatePhone();
    validateBirthDate();
    // validateAddress();
    validateEmail();
    validatePassword();
    validateConfirmPassword();
    validateGender();

    // Check if there are any errors
    if (hasErrors.value) {
        toast.error('Vui lòng kiểm tra lại thông tin đăng ký!');
        console.log('Errors:', hasErrors.value);
        return;
    }

    try {
        isLoading.value = true;
        // Xử lý logic đăng ký ở đây
        const registerData = {
            fullName: form.value.fullName,
            phone: form.value.phone,
            gender: form.value.gender ? form.value.gender : 'Nam',
            birthDate: form.value.birthDate ? new Date(form.value.birthDate).toISOString() : null,
            email: form.value.email,
            password: form.value.password,
            confirmPassword: form.value.confirmPassword
        };
        // Gọi action registerKhachHang từ store
        console.log('Register data:', registerData); // Kiểm tra dữ liệu gửi lên
        const result = await gbStore.registerKhachHang(registerData);
        console.log('Register result:', result);
        await new Promise(resolve => setTimeout(resolve, 1000));
        if (result.error) {
            if (result.fieldErrors) {
                console.log('Field errors:', result.fieldErrors); // Kiểm tra lỗi từ backend
                Object.keys(result.fieldErrors).forEach((field) => {
                    errors.value[field] = result.fieldErrors[field];
                });
                toast.error('Vui lòng kiểm tra lại thông tin!');
            }
            return;
        }
        // toast.success('Đăng ký thành công!');
        router.push('/login-register/login');
    } catch (error) {
        toast.error('Đăng ký thất bại. Vui lòng thử lại!');
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
    padding: 50px;
    width: 100%;
    max-width: 550px;
    box-shadow:
        0 10px 30px rgba(0, 0, 0, 0.1),
        inset 0 0 0 1px rgba(255, 255, 255, 0.5);
    animation: fadeIn 0.6s ease-out;
}

.login-header {
    text-align: center;
    margin-bottom: 40px;
}

.login-header img {
    width: 180px;
    margin-bottom: 20px;
}

.login-header p {
    font-size: 16px;
    color: #333;
}

.form-group {
    margin-bottom: 25px;
}

.form-group label {
    display: block;
    margin-bottom: 10px;
    color: #333;
    font-size: 15px;
    font-weight: 500;
}

.form-control {
    width: 100%;
    padding: 15px 20px;
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

.form-control::placeholder {
    color: #999;
}

.password-input {
    position: relative;
}

.password-toggle {
    position: absolute;
    right: 15px;
    top: 50%;
    transform: translateY(-50%);
    cursor: pointer;
    background: rgba(0, 0, 0, 0.05);
    color: rgba(0, 0, 0, 0.6);
    width: 35px;
    height: 35px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
}

.password-toggle:hover {
    background: #e0e0e0;
    color: #333;
}

.gender-options {
    display: flex;
    gap: 35px;
    padding: 8px 0;
    left: 20px;
}

.gender-option {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
}

.gender-option input[type="radio"] {
    margin: 0;
    width: 16px;
    height: 16px;
    border: 2px solid rgba(0, 0, 0, 0.3);
    border-radius: 50%;
    cursor: pointer;
    vertical-align: middle;
}

.gender-option input[type="radio"]:checked {
    border-color: #000;
    background: #000;
    box-shadow: inset 0 0 0 3px #fff;
}

.gender-option span {
    font-size: 14px;
    line-height: 16px;
    color: rgba(0, 0, 0, 0.8);
}

.login-button {
    width: 100%;
    padding: 16px;
    background: rgba(0, 0, 0, 0.8);
    backdrop-filter: blur(4px);
    color: white;
    border: none;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    margin-top: 20px;
}

.login-button:hover {
    background: #333;
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.login-button:disabled {
    background: #ccc;
    cursor: not-allowed;
    transform: none;
    box-shadow: none;
}

.signup-prompt {
    text-align: center;
    margin-top: 30px;
    padding-top: 30px;
    border-top: 1px solid rgba(0, 0, 0, 0.1);
    color: #666;
    font-size: 15px;
}

.signup-prompt a {
    color: #000;
    font-weight: 600;
    text-decoration: none;
    margin-left: 5px;
}

.signup-prompt a:hover {
    opacity: 0.7;
}

.signup-link {
    color: #000;
    font-weight: 600;
    text-decoration: none;
    transition: all 0.3s ease;
}

.signup-link:hover {
    opacity: 0.7;
    text-decoration: underline;
}

.error-message {
    color: #dc3545;
    font-size: 13px;
    margin-top: 8px;
    display: block;
}

/* Responsive */
@media (max-width: 480px) {
    .login-box {
        padding: 30px;
        margin: 15px;
    }

    .gender-options {
        gap: 25px;
    }
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

/* Thêm keyframes animation */
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
</style>