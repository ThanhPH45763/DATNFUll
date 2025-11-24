import './assets/main.css'
import './assets/smooth-scroll.css'  // Import CSS cho hiệu ứng cuộn mượt
import './assets/admin-theme.css'  // Import Design System cho Admin
import './assets/web-theme.css'  // Import Design System cho Web (Khách hàng)
import './polyfills/global';

import { createApp } from 'vue'
import { createPinia } from 'pinia';
import App from './App.vue'
import router from './router/index.js';
import Antd from 'ant-design-vue';
import { Carousel } from 'ant-design-vue'
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle';
import 'ant-design-vue/dist/reset.css';
import 'bootstrap-icons/font/bootstrap-icons.css';
import 'vue3-toastify/dist/index.css';
import 'bootstrap';
import VueApexCharts from 'vue3-apexcharts'
import { useGbStore } from '@/stores/gbStore';




const app = createApp(App)

const pinia = createPinia();
app.use(router);
app.use(Carousel);
app.use(pinia);
app.use(Antd);
app.use(VueApexCharts)
// Khôi phục trạng thái đăng nhập ngay khi ứng dụng khởi động
const gbStore = useGbStore();
gbStore.restoreLoginState();
app.mount('#app');