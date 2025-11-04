import axios from "axios";
import { message } from 'ant-design-vue';
import router from '../router';

const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080/',
    headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json'
    }
})
// Thêm interceptor để gửi token trong mọi request
// axiosInstance.interceptors.request.use(
//     (config) => {
//         const token = sessionStorage.getItem('token') || localStorage.getItem('token'); // Lấy token từ localStorage
//         if (token) {
//             config.headers.Authorization = `Bearer ${token}`;
//         } else {
//             console.error('Token not found in localStorage');
//         }
//         return config;
//     },
//     (error) => Promise.reject(error)
// );
// Response Interceptor để xử lý lỗi 403 toàn cục
axiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 403) {
            localStorage.removeItem('userInfo')
            localStorage.removeItem('isLoggedIn')
            localStorage.removeItem('id_roles')
            localStorage.removeItem('userDetails')
            localStorage.removeItem('token')
            sessionStorage.removeItem('userInfo')
            sessionStorage.removeItem('isLoggedIn')
            sessionStorage.removeItem('id_roles')
            sessionStorage.removeItem('userDetails')
            sessionStorage.removeItem('token')
            router.push('/login-register/loginAdmin');
            message.error('Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại!');
        }
        return Promise.reject(error);
    }
);

export default axiosInstance