<template>
    <footer class="footer-container">
        <div class="footer-content">
            <div class="footer-brand">
                <div class="logo-container">
                    <img src="../images/logo/logo2.png" class="footer-logo" alt="G&B Sports Logo">
                </div>
                <p class="brand-description">
                    Chúng tôi luôn muốn mang đến những sản phẩm quần áo thể thao tốt nhất cho người tiêu dùng.
                    Chỉ một khách hàng hài lòng đều là một niềm vui của chúng tôi!
                </p>
                <div class="social-icons">
                    <a href="#" class="social-link">
                        <Facebook class="icon" />
                    </a>
                    <a href="#" class="social-link">
                        <Instagram class="icon" />
                    </a>
                    <a href="#" class="social-link">
                        <Youtube class="icon" />
                    </a>
                </div>
            </div>

            <div class="footer-links-container">
                <div class="footer-section">
                    <h3 class="footer-heading">Bạn cần hỗ trợ</h3>
                    <div class="contact-info">
                        <p class="contact-item">
                            <span class="contact-label">Hotline:</span>
                            <span class="contact-value">0989 864 737</span>
                        </p>
                        <p class="contact-item">
                            <span class="contact-label">Địa chỉ:</span>
                            <span class="contact-value">Cao đẳng FPT Polytechnic, Trịnh Văn Bô, nam Từ Liêm, Hà
                                Nội</span>
                        </p>
                        <p class="contact-item">
                            <span class="contact-label">Email:</span>
                            <span class="contact-value">chinhhtph46334@fpt.edu.vn</span>
                        </p>
                    </div>
                </div>

                <div class="footer-section">
                    <h3 class="footer-heading">Hỗ trợ khách hàng</h3>
                    <ul class="footer-links">
                        <li><a href="#" class="footer-link">Đặt hàng</a></li>
                        <li><a href="#" class="footer-link">C/s hỗ trợ</a></li>
                        <li><a href="#" class="footer-link">Thắc mắc</a></li>
                        <li><a href="#" class="footer-link">Đóng góp</a></li>
                    </ul>
                    
                </div>
                <div class="footer-section">
                    <h3 class="footer-heading">Cửa hàng</h3>
                    <ul class="footer-links">
                        <li @click="router.push('/home')" style="cursor: pointer;"><a class="footer-link">Trang chủ</a></li>
                        <li @click="router.push('/cuaHang')" style="cursor: pointer;"><a class="footer-link">Giới thiệu</a></li>
                        <li @click="router.push('/hoTro')" style="cursor: pointer;"><a class="footer-link">Liên hệ</a></li>
                    </ul>
                    <div class="certification">
                        <img src="../images/logo/boCongThuong.webp" alt="Bộ Công Thương" class="certification-img">
                    </div>
                </div>

            </div>
        </div>

        <div class="footer-bottom">
            <p class="copyright"> Bản quyền thuộc về G&B Sports | Thiết kế 2025</p>
        </div>
    </footer>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router';
const router = useRouter();

</script>

<style scoped>
.footer-container {
    background: linear-gradient(to right, #f5f4f5, #e9e9e9);
    padding: 3rem 0 1rem;
    color: #333;
    font-family: 'Arial', sans-serif;
    width: 100%;
}

.footer-content {
    display: flex;
    flex-wrap: wrap;
    width: 100%;
    margin: 0 auto;
    padding: 0 3rem;
}

.footer-brand {
    flex: 0 0 25%;
    padding-right: 2rem;
    border-right: 1px solid #ddd;
}

.logo-container {
    display: flex;
    justify-content: flex-start;
    margin-bottom: 1.5rem;
}

.footer-logo {
    width: 150px;
    height: auto;
}

.brand-description {
    font-size: 0.9rem;
    line-height: 1.6;
    color: #555;
    margin-bottom: 1.5rem;
}

.social-icons {
    display: flex;
    gap: 1rem;
    margin-top: 1rem;
}

.social-link {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background-color: #f0f0f0;
    transition: all 0.3s ease;
}

.social-link:hover {
    background-color: #e0e0e0;
    transform: translateY(-3px);
}

.icon {
    width: 20px;
    height: 20px;
    color: #555;
}

.footer-links-container {
    flex: 0 0 75%;
    display: flex;
    flex-wrap: wrap;
    padding-left: 2rem;
}

.footer-section {
    flex: 1;
    min-width: 200px;
    padding: 0 1rem;
}

.footer-heading {
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: 1.2rem;
    color: #333;
    position: relative;
    padding-bottom: 0.5rem;
}

.footer-heading:after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 40px;
    height: 2px;
    background-color: #003887;
}

.contact-info {
    margin-top: 1rem;
}

.contact-item {
    margin-bottom: 0.8rem;
    font-size: 0.9rem;
    line-height: 1.5;
}

.contact-label {
    font-weight: 600;
    color: #444;
}

.contact-value {
    color: #555;
}

.footer-links {
    list-style: none;
    padding: 0;
    margin: 0;
}

.footer-links li {
    margin-bottom: 0.7rem;
}

.footer-link {
    color: #555;
    text-decoration: none;
    font-size: 0.9rem;
    transition: color 0.3s ease;
    position: relative;
    padding-left: 0.8rem;
}

.footer-link:before {
    content: '›';
    position: absolute;
    left: 0;
    color: #003089;
}

.footer-link:hover {
    color: #00379c;
}

.certification {
    margin-top: 1rem;
}

.certification-img {
    width: 120px;
    height: auto;
}

.footer-bottom {
    text-align: center;
    padding-top: 2rem;
    margin-top: 2rem;
    border-top: 1px solid #ddd;
    width: 100%;
}

.copyright {
    font-size: 0.9rem;
    color: #777;
}

@media (max-width: 992px) {
    .footer-brand {
        flex: 0 0 100%;
        border-right: none;
        border-bottom: 1px solid #ddd;
        padding-bottom: 2rem;
        margin-bottom: 2rem;
        padding-right: 0;
    }

    .footer-links-container {
        flex: 0 0 100%;
        padding-left: 0;
    }
}

@media (max-width: 768px) {
    .footer-section {
        flex: 0 0 50%;
        margin-bottom: 2rem;
    }
}

@media (max-width: 576px) {
    .footer-section {
        flex: 0 0 100%;
    }
}
</style>