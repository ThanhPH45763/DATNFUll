<template>
    <div class="store-about">
        <!-- Banner Hero Section -->
        <section class="hero-section">
            <div class="hero-content">
                <img src="../../../images/logo/logoGB.png" alt="G&B SPORTS" class="hero-logo" />
                <h1 class="hero-title">G&B SPORTS</h1>
                <p class="hero-slogan">CHẤT LƯỢNG TẠO NÊN PHONG CÁCH</p>
                <a-button type="primary" size="large" class="shop-now-btn" @click="navigateTo('/home')">KHÁM PHÁ
                    NGAY</a-button>
            </div>
        </section>

        <!-- About Us Section -->
        <section class="about-section container mt-4" ref="aboutRef" :class="{ 'visible': aboutVisible }">
            <a-row :gutter="[24, 24]" align="middle">
                <a-col :xs="24" :md="12">
                    <div class="section-content scroll-animation">
                        <h2 class="section-title">Giới thiệu về G&B SPORTS</h2>
                        <div class="title-divider"></div>
                        <p class="section-text">
                            G&B SPORTS tự hào là thương hiệu chuyên cung cấp các sản phẩm thể thao chất lượng cao tại
                            Việt Nam. Ra đời với sứ mệnh mang đến những sản phẩm thể thao chất lượng, đa dạng và phù hợp
                            với mọi nhu cầu, G&B SPORTS đã và đang trở thành người bạn đồng hành đáng tin cậy của nhiều
                            thế hệ người yêu thể thao.
                        </p>
                        <p class="section-text">
                            Với hơn 10 năm kinh nghiệm trong lĩnh vực, chúng tôi không ngừng đổi mới và phát triển để
                            mang đến cho khách hàng những sản phẩm tốt nhất với giá thành hợp lý, kết hợp cùng dịch vụ
                            chăm sóc khách hàng tận tâm.
                        </p>
                    </div>
                </a-col>
                <a-col :xs="24" :md="12">
                    <div class="about-image scroll-animation delay-200">
                        <img src="https://cdn.qwenlm.ai/output/90379cce-2313-4ef6-a5f5-69f4ca22e9aa/t2i/fa9d07c5-c1e3-47ca-996c-0464302354a5/de19f865-4918-4b9e-82b6-3e358dda3c04.png?key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyZXNvdXJjZV91c2VyX2lkIjoiOTAzNzljY2UtMjMxMy00ZWY2LWE1ZjUtNjlmNGNhMjJlOWFhIiwicmVzb3VyY2VfaWQiOiJkZTE5Zjg2NS00OTE4LTRiOWUtODJiNi0zZTM1OGRkYTNjMDQiLCJyZXNvdXJjZV9jaGF0X2lkIjpudWxsfQ.zKoDbBl0PP6ow6MhGtGAG1ISbPgN9HIHEtFoE3j_2gk"
                            alt="G&B Store" />
                    </div>
                </a-col>
            </a-row>
        </section>

        <!-- Our Values Section -->
        <section class="values-section" ref="valuesRef" :class="{ 'visible': valuesVisible }">
            <div class="container">
                <h2 class="section-title text-center scroll-animation">Giá trị cốt lõi</h2>
                <div class="title-divider center-divider scroll-animation delay-100"></div>

                <a-row :gutter="[32, 32]" class="mt-5">
                    <a-col :xs="24" :md="8">
                        <div class="value-card scroll-animation fade-right delay-200">
                            <div class="value-icon">
                                <trophy-outlined />
                            </div>
                            <h3>Chất lượng hàng đầu</h3>
                            <p>Sản phẩm được tuyển chọn kỹ lưỡng, đảm bảo chất lượng từ chất liệu đến đường may chi
                                tiết.</p>
                        </div>
                    </a-col>

                    <a-col :xs="24" :md="8">
                        <div class="value-card scroll-animation delay-300">
                            <div class="value-icon">
                                <star-outlined />
                            </div>
                            <h3>Đa dạng mẫu mã</h3>
                            <p>Đa dạng về kiểu dáng, màu sắc, kích cỡ phù hợp với mọi lứa tuổi và nhu cầu sử dụng.</p>
                        </div>
                    </a-col>

                    <a-col :xs="24" :md="8">
                        <div class="value-card scroll-animation fade-left delay-400">
                            <div class="value-icon">
                                <customer-service-outlined />
                            </div>
                            <h3>Dịch vụ tận tâm</h3>
                            <p>Đội ngũ nhân viên chuyên nghiệp, tận tình tư vấn giúp quý khách tìm được sản phẩm ưng ý
                                nhất.</p>
                        </div>
                    </a-col>
                </a-row>
            </div>
        </section>

        <!-- Products Section -->
        <section class="products-section container" ref="productsRef" :class="{ 'visible': productsVisible }">
            <h2 class="section-title text-center">Sản phẩm nổi bật</h2>
            <div class="title-divider center-divider"></div>

            <a-row :gutter="[24, 24]" class="mt-5">
                <a-col :xs="24" :sm="12" :md="6">
                    <div class="product-category">
                        <div class="category-image">
                            <img src="https://bulbal.vn/wp-content/uploads/2023/08/BO-QUAN-AO-BONG-DA-BULBAL-OLAS-2-BE-1-scaled.jpg"
                                alt="Quần áo bóng đá" />
                        </div>
                        <h3>Quần áo bóng đá</h3>
                    </div>
                </a-col>

                <a-col :xs="24" :sm="12" :md="6">
                    <div class="product-category">
                        <div class="category-image">
                            <img src="https://supersports.com.vn/cdn/shop/files/HY2703-1_1200x1200.jpg?v=1689241417"
                                alt="Quần áo tennis" />
                        </div>
                        <h3>Quần áo tennis</h3>
                    </div>
                </a-col>

                <a-col :xs="24" :sm="12" :md="6">
                    <div class="product-category">
                        <div class="category-image">
                            <img src="https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?q=80&w=1000"
                                alt="Đồ tập gym" />
                        </div>
                        <h3>Đồ tập gym</h3>
                    </div>
                </a-col>

                <a-col :xs="24" :sm="12" :md="6">
                    <div class="product-category">
                        <div class="category-image">
                            <img src="https://cdn.vuahanghieu.com/unsafe/0x0/left/top/smart/filters:quality(90)/https://admin.vuahanghieu.com/upload/news/content/2022/08/mu-adidas-hn1081-2-jpg-1660546649-15082022135729.jpg"
                                alt="Phụ kiện thể thao" />
                        </div>
                        <h3>Phụ kiện thể thao</h3>
                    </div>
                </a-col>
            </a-row>

            <div class="text-center mt-4 mb-4" @click="navigateTo('/danhSachSanPham?filter=all')">
                <a-button type="primary" size="large">XEM TẤT CẢ SẢN PHẨM</a-button>
            </div>
        </section>

        <!-- Store Gallery Section -->
        <section class="gallery-section" ref="galleryRef" :class="{ 'visible': galleryVisible }">
            <div class="container">
                <h2 class="section-title text-center">Hình ảnh cửa hàng</h2>
                <div class="title-divider center-divider"></div>

                <a-row :gutter="[16, 16]" class="mt-5">
                    <a-col :xs="24" :sm="12" :md="8">
                        <div class="gallery-item">
                            <img src="https://penviet.com/wp-content/uploads/2022/08/thiet-ke-shop-quan-ao-the-thao-6.jpg"
                                alt="Cửa hàng G&B SPORTS" />
                        </div>
                    </a-col>
                    <a-col :xs="24" :sm="12" :md="8">
                        <div class="gallery-item">
                            <img src="https://conndesign.vn/wp-content/uploads/2024/04/thiet-ke-shop-the-thao-12.jpg"
                                alt="Cửa hàng G&B SPORTS" />
                        </div>
                    </a-col>
                    <a-col :xs="24" :sm="12" :md="8">
                        <div class="gallery-item">
                            <img src="https://penviet.com/wp-content/uploads/2022/01/thiet-ke-shop-the-thao-4rv-9.jpg"
                                alt="Cửa hàng G&B SPORTS" />
                        </div>
                    </a-col>
                </a-row>
            </div>
        </section>

        <!-- Testimonials Section -->
        <section class="testimonials-section container" ref="testimonialsRef"
            :class="{ 'visible': testimonialsVisible }">
            <h2 class="section-title text-center">Khách hàng nói gì về chúng tôi</h2>
            <div class="title-divider center-divider"></div>

            <a-carousel autoplay class="testimonial-carousel mt-5">
                <div class="testimonial-slide">
                    <div class="testimonial-content">
                        <div class="testimonial-avatar">
                            <img src="https://randomuser.me/api/portraits/men/32.jpg" alt="Khách hàng" />
                        </div>
                        <p class="testimonial-text">"Quần áo thể thao ở G&B rất chất lượng, tôi đã mua và sử dụng trong
                            hơn 2 năm mà vẫn giữ được form dáng và màu sắc. Nhân viên tư vấn rất nhiệt tình và chuyên
                            nghiệp."</p>
                        <h4 class="testimonial-name">Nguyễn Văn A</h4>
                        <p class="testimonial-title">Khách hàng thân thiết</p>
                    </div>
                </div>
                <div class="testimonial-slide">
                    <div class="testimonial-content">
                        <div class="testimonial-avatar">
                            <img src="https://randomuser.me/api/portraits/women/44.jpg" alt="Khách hàng" />
                        </div>
                        <p class="testimonial-text">"Tôi rất hài lòng với chất lượng dịch vụ tại G&B SPORTS. Đội ngũ
                            nhân viên luôn tận tình hỗ trợ và sản phẩm đa dạng, phù hợp với nhiều nhu cầu khác nhau."
                        </p>
                        <h4 class="testimonial-name">Trần Thị B</h4>
                        <p class="testimonial-title">Huấn luyện viên thể hình</p>
                    </div>
                </div>
            </a-carousel>
        </section>

        <!-- Contact Section -->
        <section class="contact-section" ref="contactRef" :class="{ 'visible': contactVisible }">
            <div class="container">
                <a-row :gutter="[24, 24]">
                    <a-col :xs="24" :md="12">
                        <div class="contact-info">
                            <h2 class="section-title">Liên hệ với chúng tôi</h2>
                            <div class="title-divider"></div>

                            <div class="contact-item">
                                <environment-outlined />
                                <p>13 P. Trịnh Văn Bô, Xuân Phương, Nam Từ Liêm, Hà Nội 100000, Việt Nam</p>
                            </div>

                            <div class="contact-item">
                                <phone-outlined />
                                <p>0989 864 737</p>
                            </div>

                            <div class="contact-item">
                                <mail-outlined />
                                <p>chinhhtph46334@fpt.edu.vn</p>
                            </div>

                            <div class="contact-item">
                                <clock-circle-outlined />
                                <p>Thứ 2 - Chủ nhật: 8:00 - 21:00</p>
                            </div>

                            <div class="social-links">
                                <a href="#" class="social-link"><facebook-outlined /></a>
                                <a href="#" class="social-link"><instagram-outlined /></a>
                                <a href="#" class="social-link"><youtube-outlined /></a>
                            </div>
                        </div>
                    </a-col>

                    <a-col :xs="24" :md="12">
                        <div class="contact-map">
                            <iframe
                                src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d4428.446608759952!2d105.74426870081332!3d21.03809188745573!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x313455e940879933%3A0xcf10b34e9f1a03df!2zVHLGsOG7nW5nIENhbyDEkeG6s25nIEZQVCBQb2x5dGVjaG5pYw!5e0!3m2!1svi!2s!4v1744306375354!5m2!1svi!2s"
                                width="600" height="450" style="border:0;" allowfullscreen="" loading="lazy"
                                referrerpolicy="no-referrer-when-downgrade"></iframe>
                        </div>
                    </a-col>
                </a-row>
            </div>
        </section>
    </div>
</template>

<script setup>
import {
    TrophyOutlined,
    StarOutlined,
    CustomerServiceOutlined,
    EnvironmentOutlined,
    PhoneOutlined,
    MailOutlined,
    ClockCircleOutlined,
    FacebookOutlined,
    InstagramOutlined,
    YoutubeOutlined
} from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { ref, onMounted } from 'vue';

const router = useRouter();

const navigateTo = (path) => {
    router.push(path);
};

// Refs for sections
const aboutRef = ref(null);
const valuesRef = ref(null);
const productsRef = ref(null);
const galleryRef = ref(null);
const testimonialsRef = ref(null);
const contactRef = ref(null);

// Visibility state
const aboutVisible = ref(false);
const valuesVisible = ref(false);
const productsVisible = ref(false);
const galleryVisible = ref(false);
const testimonialsVisible = ref(false);
const contactVisible = ref(false);

onMounted(() => {
    // Tạo Intersection Observer
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');

                // Thêm class scrolled cho tất cả các phần tử scroll-animation bên trong
                const animatedElements = entry.target.querySelectorAll('.scroll-animation');
                animatedElements.forEach(el => {
                    el.classList.add('scrolled');
                });

                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.2 });

    // Quan sát các section
    const sections = document.querySelectorAll('section');
    sections.forEach(section => {
        observer.observe(section);
    });
});
</script>

<style scoped>
.store-about {
    font-family: 'Roboto', sans-serif;
    color: #333;
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
}

.text-center {
    text-align: center;
}

.mt-4 {
    margin-top: 20px;
}

.mt-5 {
    margin-top: 30px;
}

/* Hero Section */
.hero-section {
    height: 600px;
    background: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('https://images.unsplash.com/photo-1576678927484-cc907957088c?q=80&w=1000') center/cover no-repeat;
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    color: white;
    position: relative;
}

.hero-content {
    max-width: 800px;
    padding: 0 20px;
}

.hero-logo {
    width: 150px;
    margin-bottom: 20px;
}

.hero-title {
    font-size: 3.5rem;
    margin-bottom: 10px;
    letter-spacing: 2px;
}

.hero-slogan {
    font-size: 1.2rem;
    margin-bottom: 30px;
    letter-spacing: 1px;
}

.shop-now-btn {
    background-color: #e53935;
    border-color: #e53935;
    margin-top: 20px;
}

.shop-now-btn:hover {
    background-color: #c62828;
    border-color: #c62828;
}

/* Section Styles */
section {
    padding: 80px 0;
    opacity: 0;
    transform: translateY(30px);
    transition: opacity 0.8s ease, transform 0.8s ease;
}

section.visible {
    opacity: 1;
    transform: translateY(0);
}

.section-title {
    font-size: 2rem;
    margin-bottom: 15px;
    font-weight: 600;
}

.title-divider {
    width: 60px;
    height: 3px;
    background-color: #e53935;
    margin-bottom: 25px;
}

.center-divider {
    margin-left: auto;
    margin-right: auto;
}

.section-text {
    font-size: 1rem;
    line-height: 1.8;
    margin-bottom: 20px;
}

/* About Section */
.about-section {
    background-color: #fff;
}

.about-image img {
    width: 100%;
    height: auto;
    border-radius: 8px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

/* Values Section */
.values-section {
    background-color: #f9f9f9;
}

.value-card,
.product-category,
.gallery-item,
.contact-item {
    opacity: 0;
    transform: translateY(20px);
    transition: opacity 0.6s ease, transform 0.6s ease;
}

.visible .value-card,
.visible .product-category,
.visible .gallery-item,
.visible .contact-item {
    opacity: 1;
    transform: translateY(0);
}

.value-card {
    background-color: #fff;
    border-radius: 8px;
    padding: 30px;
    text-align: center;
    height: 100%;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
    transition: transform 0.3s ease;
}

.value-card:hover {
    transform: translateY(-10px);
}

.value-icon {
    font-size: 40px;
    color: #e53935;
    margin-bottom: 20px;
}

.value-card h3 {
    font-size: 1.3rem;
    margin-bottom: 15px;
}

/* Products Section */
.products-section {
    background-color: #fff;
}

.product-category {
    text-align: center;
    margin-bottom: 20px;
}

.category-image {
    overflow: hidden;
    border-radius: 8px;
    margin-bottom: 15px;
}

.category-image img {
    width: 100%;
    height: 200px;
    object-fit: cover;
    transition: transform 0.5s ease;
}

.product-category:hover .category-image img {
    transform: scale(1.1);
}

.product-category h3 {
    font-size: 1.2rem;
    margin-top: 10px;
}

/* Gallery Section */
.gallery-section {
    background-color: #f9f9f9;
}

.gallery-item {
    margin-bottom: 20px;
    overflow: hidden;
    border-radius: 8px;
}

.gallery-item img {
    width: 100%;
    height: 250px;
    object-fit: cover;
    transition: transform 0.5s ease;
}

.gallery-item:hover img {
    transform: scale(1.1);
}

/* Testimonials Section */
.testimonials-section {
    background-color: #fff;
}

.testimonial-slide {
    padding: 20px;
}

.testimonial-content {
    max-width: 700px;
    margin: 0 auto;
    text-align: center;
    padding: 40px 20px;
}

.testimonial-avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    overflow: hidden;
    margin: 0 auto 20px;
    border: 3px solid #e53935;
}

.testimonial-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.testimonial-text {
    font-size: 1.1rem;
    line-height: 1.8;
    margin-bottom: 20px;
    font-style: italic;
}

.testimonial-name {
    font-size: 1.2rem;
    margin-bottom: 5px;
    color: #e53935;
}

.testimonial-title {
    font-size: 0.9rem;
    color: #666;
}

/* Contact Section */
.contact-section {
    background-color: #1e1e1e;
    color: #fff;
}

.contact-info {
    padding: 20px;
}

.contact-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 20px;
}

.contact-item .anticon {
    font-size: 20px;
    color: #e53935;
    margin-right: 15px;
    margin-top: 3px;
}

.social-links {
    margin-top: 30px;
}

.social-link {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: #e53935;
    color: white;
    margin-right: 10px;
    transition: background-color 0.3s ease;
}

.social-link:hover {
    background-color: #c62828;
}

.contact-map {
    height: 100%;
    min-height: 400px;
}

/* Override Ant Design styles */
:deep(.ant-btn-primary) {
    background-color: #e53935;
    border-color: #e53935;
}

:deep(.ant-btn-primary:hover) {
    background-color: #c62828;
    border-color: #c62828;
}

:deep(.ant-carousel .slick-dots li button) {
    background: #e53935;
}

:deep(.ant-carousel .slick-dots li.slick-active button) {
    background: #c62828;
}

/* CSS cho hiệu ứng cuộn */
.scroll-animation {
    opacity: 0;
    transform: translateY(30px);
    transition: all 0.8s ease;
}

.scroll-animation.scrolled {
    opacity: 1;
    transform: translateY(0);
}

.fade-right {
    transform: translateX(-30px);
}

.fade-right.scrolled {
    transform: translateX(0);
}

.fade-left {
    transform: translateX(30px);
}

.fade-left.scrolled {
    transform: translateX(0);
}

.zoom-in {
    transform: scale(0.8);
}

.zoom-in.scrolled {
    transform: scale(1);
}

/* Delay cho các phần tử */
.delay-100 {
    transition-delay: 0.1s;
}

.delay-200 {
    transition-delay: 0.2s;
}

.delay-300 {
    transition-delay: 0.3s;
}

.delay-400 {
    transition-delay: 0.4s;
}

.delay-500 {
    transition-delay: 0.5s;
}

@media (max-width: 768px) {
    .hero-title {
        font-size: 2.5rem;
    }

    .section-title {
        font-size: 1.8rem;
    }

    section {
        padding: 60px 0;
    }

    .contact-map {
        margin-top: 30px;
    }
}

/* Animation cho các phần tử con trong section */
.visible .product-category,
.visible .value-card,
.visible .gallery-item,
.visible .contact-item,
.visible .section-title,
.visible .title-divider {
    animation: fadeInUp 0.6s ease forwards;
}

.visible .product-category:nth-child(1),
.visible .value-card:nth-child(1),
.visible .gallery-item:nth-child(1),
.visible .contact-item:nth-child(1) {
    animation-delay: 0.1s;
}

.visible .product-category:nth-child(2),
.visible .value-card:nth-child(2),
.visible .gallery-item:nth-child(2),
.visible .contact-item:nth-child(2) {
    animation-delay: 0.2s;
}

.visible .product-category:nth-child(3),
.visible .value-card:nth-child(3),
.visible .gallery-item:nth-child(3),
.visible .contact-item:nth-child(3) {
    animation-delay: 0.3s;
}

.visible .product-category:nth-child(4),
.visible .value-card:nth-child(4),
.visible .contact-item:nth-child(4) {
    animation-delay: 0.4s;
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>