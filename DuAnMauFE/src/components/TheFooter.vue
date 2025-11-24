<template>
    <footer class="footer-elegant">
        <div class="footer-content">
            <!-- Brand Section -->
            <div class="footer-brand">
                <div class="logo-container">
                    <img src="../images/logo/logo2.png" class="footer-logo" alt="G&B Sports Logo">
                </div>
                <p class="brand-description">
                    Chuyên cung cấp áo sơ mi nam cao cấp với chất liệu tốt nhất. 
                    Sự hài lòng của quý khách là niềm tự hào của chúng tôi.
                </p>
                <div class="social-icons">
                    <a href="#" class="social-link" aria-label="Facebook">
                        <Facebook class="icon" />
                    </a>
                    <a href="#" class="social-link" aria-label="Instagram">
                        <Instagram class="icon" />
                    </a>
                    <a href="#" class="social-link" aria-label="Youtube">
                        <Youtube class="icon" />
                    </a>
                </div>
            </div>

            <!-- Links Sections -->
            <div class="footer-links-container">
                <div class="footer-section">
                    <h3 class="footer-heading">Hỗ trợ khách hàng</h3>
                    <div class="contact-info">
                        <p class="contact-item">
                            <span class="contact-label">Hotline:</span>
                            <span class="contact-value">0989 864 737</span>
                        </p>
                        <p class="contact-item">
                            <span class="contact-label">Email:</span>
                            <span class="contact-value">chinhhtph46334@fpt.edu.vn</span>
                        </p>
                        <p class="contact-item">
                            <span class="contact-label">Địa chỉ:</span>
                            <span class="contact-value">Cao đẳng FPT Polytechnic, Trịnh Văn Bô, Nam Từ Liêm, Hà Nội</span>
                        </p>
                    </div>
                </div>

                <div class="footer-section">
                    <h3 class="footer-heading">Dịch vụ</h3>
                    <ul class="footer-links">
                        <li><a href="#" class="footer-link">Đặt hàng</a></li>
                        <li><a href="#" class="footer-link">Chính sách đổi trả</a></li>
                        <li><a href="#" class="footer-link">Hướng dẫn chọn size</a></li>
                        <li><a href="#" class="footer-link">Chăm sóc sản phẩm</a></li>
                    </ul>
                </div>

                <div class="footer-section">
                    <h3 class="footer-heading">Thông tin</h3>
                    <ul class="footer-links">
                        <li @click="router.push('/home')" style="cursor: pointer;">
                            <a class="footer-link">Trang chủ</a>
                        </li>
                        <li @click="router.push('/cuaHang')" style="cursor: pointer;">
                            <a class="footer-link">Giới thiệu</a>
                        </li>
                        <li @click="router.push('/hoTro')" style="cursor: pointer;">
                            <a class="footer-link">Liên hệ</a>
                        </li>
                    </ul>
                    <div class="certification">
                        <img src="../images/logo/boCongThuong.webp" alt="Bộ Công Thương" class="certification-img">
                    </div>
                </div>
            </div>
        </div>

        <div class="footer-bottom">
            <p class="copyright">© 2025 G&B Sports. Bản quyền thuộc về công ty.</p>
        </div>
    </footer>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { Facebook, Instagram, Youtube } from 'lucide-vue-next';

const router = useRouter();
</script>

<style scoped>
/* ===================================================
   FOOTER - Elegant Men's Dress Shirt Design
   =================================================== */

.footer-elegant {
    background-color: var(--color-primary);
    color: var(--color-cream);
    padding: var(--space-3xl) 0 var(--space-lg);
    font-family: var(--font-primary);
    width: 100%;
    margin-top: var(--space-3xl);
}

.footer-content {
    display: flex;
    flex-wrap: wrap;
    max-width: var(--container-max);
    margin: 0 auto;
    padding: 0 var(--space-xl);
    gap: var(--space-xl);
}

/* ========== Brand Section ========== */
.footer-brand {
    flex: 0 0 30%;
    min-width: 280px;
}

.logo-container {
    margin-bottom: var(--space-lg);
}

.footer-logo {
    width: 150px;
    height: auto;
    filter: brightness(0) invert(1);
    opacity: 0.9;
}

.brand-description {
    font-size: var(--text-sm);
    line-height: 1.8;
    color: var(--color-cream);
    margin-bottom: var(--space-lg);
    opacity: 0.9;
}

/* ========== Social Icons ========== */
.social-icons {
    display: flex;
    gap: var(--space-md);
}

.social-link {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: var(--radius-full);
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    transition: all var(--transition-base);
}

.social-link:hover {
    background-color: var(--color-accent);
    border-color: var(--color-accent);
    transform: translateY(-2px);
}

.icon {
    width: 20px;
    height: 20px;
    color: var(--color-white);
}

/* ========== Links Container ========== */
.footer-links-container {
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    gap: var(--space-xl);
}

.footer-section {
    flex: 1;
    min-width: 200px;
}

/* ========== Footer Headings ========== */
.footer-heading {
    font-size: var(--text-lg);
    font-weight: var(--weight-semibold);
    margin-bottom: var(--space-lg);
    color: var(--color-white);
    position: relative;
    padding-bottom: var(--space-sm);
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.footer-heading:after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 40px;
    height: 2px;
    background-color: var(--color-accent);
}

/* ========== Contact Info ========== */
.contact-info {
    margin-top: var(--space-md);
}

.contact-item {
    margin-bottom: var(--space-md);
    font-size: var(--text-sm);
    line-height: 1.6;
}

.contact-label {
    font-weight: var(--weight-semibold);
    color: var(--color-white);
    display: block;
    margin-bottom: var(--space-xs);
}

.contact-value {
    color: var(--color-cream);
    opacity: 0.9;
}

/* ========== Footer Links ========== */
.footer-links {
    list-style: none;
    padding: 0;
    margin: 0;
}

.footer-links li {
    margin-bottom: var(--space-sm);
}

.footer-link {
    color: var(--color-cream);
    text-decoration: none;
    font-size: var(--text-sm);
    transition: all var(--transition-base);
    display: inline-block;
    opacity: 0.9;
}

.footer-link:hover {
    color: var(--color-accent);
    transform: translateX(4px);
}

/* ========== Certification ========== */
.certification {
    margin-top: var(--space-lg);
}

.certification-img {
    width: 120px;
    height: auto;
    opacity: 0.8;
    transition: opacity var(--transition-base);
}

.certification-img:hover {
    opacity: 1;
}

/* ========== Footer Bottom ========== */
.footer-bottom {
    text-align: center;
    padding-top: var(--space-xl);
    margin-top: var(--space-xl);
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    max-width: var(--container-max);
    margin-left: auto;
    margin-right: auto;
}

.copyright {
    font-size: var(--text-sm);
    color: var(--color-cream);
    opacity: 0.7;
}

/* ========== Responsive Design ========== */
@media (max-width: 992px) {
    .footer-brand {
        flex: 0 0 100%;
        text-align: center;
        margin-bottom: var(--space-xl);
        padding-bottom: var(--space-xl);
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }

    .logo-container {
        display: flex;
        justify-content: center;
    }

    .social-icons {
        justify-content: center;
    }

    .footer-heading:after {
        left: 50%;
        transform: translateX(-50%);
    }
}

@media (max-width: 768px) {
    .footer-content {
        padding: 0 var(--space-lg);
    }

    .footer-section {
        flex: 0 0 48%;
    }
}

@media (max-width: 576px) {
    .footer-elegant {
        padding: var(--space-2xl) 0 var(--space-md);
    }

    .footer-content {
        padding: 0 var(--space-md);
    }

    .footer-section {
        flex: 0 0 100%;
        text-align: center;
        margin-bottom: var(--space-xl);
    }

    .footer-heading:after {
        left: 50%;
        transform: translateX(-50%);
    }

    .contact-item {
        text-align: center;
    }

    .footer-link:hover {
        transform: none;
    }
}
</style>