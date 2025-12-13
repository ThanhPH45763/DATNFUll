<template>
    <div class="support-page">
        <!-- Hero Section -->
        <section class="hero-section support-hero" ref="heroRef" :class="{ 'visible': heroVisible }">
            <div class="container">
                <div class="hero-content">
                    <h1 class="hero-title">Hỗ trợ & Chăm sóc khách hàng</h1>
                    <p class="hero-description">MenWear luôn sẵn sàng hỗ trợ khách hàng mọi lúc mọi nơi</p>
                    <div class="hero-buttons">
                        <a-button type="primary" size="large" @click="scrollToElement('contactForm')">
                            Liên hệ ngay
                        </a-button>
                        <a-button size="large" @click="scrollToElement('faqSection')">
                            Câu hỏi thường gặp
                        </a-button>
                    </div>
                </div>
                <div class="hero-image">
                    <img src="https://nixxis.vn/wp-content/uploads/elementor/thumbs/MicrosoftTeams-image-4-qnwkxm3ijk2rvoj4dfboeakakcj5pphugunfmf18js.png"
                        alt="Chăm sóc khách hàng" />
                </div>
            </div>
        </section>

        <!-- Support Options Section -->
        <section class="support-options" ref="optionsRef" :class="{ 'visible': optionsVisible }">
            <div class="container">
                <h2 class="section-title">Chúng tôi hỗ trợ bạn bằng nhiều cách</h2>
                <div class="divider center-divider"></div>

                <div class="options-container">
                    <div class="support-option">
                        <div class="option-icon">
                            <phone-outlined />
                        </div>
                        <h3>Gọi điện</h3>
                        <p>0989 864 737</p>
                        <span class="time">(8:00 - 21:00 mỗi ngày)</span>
                    </div>


                    <div class="support-option">
                        <div class="option-icon">
                            <mail-outlined />
                        </div>
                        <h3>Email</h3>
                        <p>chinhhtph46334@fpt.edu.vn</p>
                        <span class="time">(Phản hồi trong 24h)</span>
                    </div>

                    <div class="support-option">
                        <div class="option-icon">
                            <shop-outlined />
                        </div>
                        <h3>Cửa hàng</h3>
                        <p>Ghé thăm cửa hàng MenWear</p>
                        <a-button @click="router.push('/cuaHang')">Xem địa chỉ</a-button>
                    </div>
                </div>
            </div>
        </section>

        <!-- FAQ Section -->
        <section class="faq-section" id="faqSection" ref="faqRef" :class="{ 'visible': faqVisible }">
            <div class="container">
                <h2 class="section-title">Câu hỏi thường gặp</h2>
                <div class="divider"></div>

                <div class="faq-categories">
                    <a-tabs>
                        <a-tab-pane key="order" tab="Đặt hàng">
                            <a-collapse accordion>
                                <a-collapse-panel header="Làm thế nào để đặt hàng trên website?" key="1">
                                    <p>Để đặt hàng trên website MenWear, bạn vui lòng thực hiện theo các bước sau:
                                    </p>
                                    <ul>
                                        <li>Tìm kiếm và chọn sản phẩm bạn muốn mua</li>
                                        <li>Chọn kích cỡ, màu sắc, số lượng và thêm vào giỏ hàng</li>
                                        <li>Kiểm tra giỏ hàng và nhấn "Thanh toán"</li>
                                        <li>Điền thông tin giao hàng và phương thức thanh toán</li>
                                        <li>Xác nhận đơn hàng</li>
                                    </ul>
                                    <p>Sau khi đặt hàng thành công, bạn sẽ nhận được email xác nhận đơn hàng.</p>
                                </a-collapse-panel>
                                <a-collapse-panel header="Tôi có thể theo dõi đơn hàng của mình không?" key="2">
                                    <p>Có, bạn có thể theo dõi đơn hàng bằng cách:</p>
                                    <ul>
                                        <li>Đăng nhập vào tài khoản của bạn trên website</li>
                                        <li>Vào mục "Đơn hàng của tôi"</li>
                                        <li>Chọn đơn hàng bạn muốn theo dõi</li>
                                    </ul>
                                    <p>Hoặc bạn có thể gọi điện thoại đến số 0987 654 321 để được hỗ trợ.</p>
                                </a-collapse-panel>
                                <a-collapse-panel header="Các phương thức thanh toán được chấp nhận?" key="3">
                                    <p>MenWear chấp nhận các phương thức thanh toán sau:</p>
                                    <ul>
                                        <li>Thanh toán khi nhận hàng (COD)</li>
                                        <li>Chuyển khoản ngân hàng</li>
                                    </ul>
                                </a-collapse-panel>
                            </a-collapse>
                        </a-tab-pane>
                        <a-tab-pane key="shipping" tab="Vận chuyển">
                            <a-collapse accordion>
                                <a-collapse-panel header="Thời gian giao hàng từ Hà Nội là bao lâu?" key="1">
                                <p>MenWear, đặt tại <strong>Hà Nội</strong>, hợp tác với <strong>Giao Hàng Tiết Kiệm (GHTK)</strong> để giao hàng nhanh chóng và an toàn. Thời gian giao hàng phụ thuộc vào địa điểm nhận hàng:</p>
                                <ul>
                                    <li><strong>Nội thành Hà Nội</strong>: 1-2 ngày làm việc.</li>
                                    <li><strong>Các tỉnh miền Bắc</strong> (gần Hà Nội, như Bắc Ninh, Hải Phòng): 2-3 ngày làm việc.</li>
                                    <li><strong>Các tỉnh miền Trung và miền Nam</strong> (như Đà Nẵng, TP. HCM): 3-5 ngày làm việc.</li>
                                    <li><strong>Vùng sâu, vùng xa, hải đảo</strong>: 5-7 ngày làm việc.</li>
                                </ul>
                                <p><strong>Lưu ý</strong>: Thời gian giao hàng có thể bị ảnh hưởng bởi thời tiết, dịp lễ Tết, hoặc các sự kiện bất khả kháng. Bạn có thể theo dõi trạng thái đơn hàng bằng <strong>mã vận đơn</strong> được gửi qua email hoặc tin nhắn sau khi đơn hàng được xử lý.</p>
                                </a-collapse-panel>
                                <a-collapse-panel header="Chi phí vận chuyển được tính như thế nào?" key="2">
                                <p>Chi phí vận chuyển được tính dựa trên <strong>trọng lượng đơn hàng</strong>, <strong>kích thước gói hàng</strong>, và <strong>khoảng cách</strong> từ Hà Nội, theo bảng giá của Giao Hàng Tiết Kiệm (GHTK):</p>
                                <ul>
                                    <li><strong>Miễn phí vận chuyển</strong> cho đơn hàng từ <strong>2.000.000đ</strong> (trừ một số khu vực vùng sâu, vùng xa).</li>
                                    <li><strong>Nội thành Hà Nội</strong>: 20.000đ - 30.000đ (tùy trọng lượng và kích thước).</li>
                                    <li><strong>Các tỉnh miền Bắc</strong>: 25.000đ - 40.000đ (tùy khu vực).</li>
                                    <li><strong>Các tỉnh miền Trung và miền Nam</strong>: 35.000đ - 50.000đ (tùy khoảng cách).</li>
                                    <li><strong>Vùng sâu, vùng xa, hải đảo</strong>: 50.000đ trở lên, tùy thuộc vào địa điểm cụ thể.</li>
                                </ul>
                                <p>Chi phí vận chuyển chính xác sẽ được hiển thị tại bước thanh toán trên website hoặc ứng dụng của MenWear. Nếu bạn chọn dịch vụ <strong>GHTK Express</strong>, chi phí có thể cao hơn nhưng thời gian giao hàng sẽ nhanh hơn. Để được hỗ trợ, vui lòng liên hệ qua số <strong>0397572262</strong> hoặc email <strong>info@menwear.com</strong>.</p>
                                </a-collapse-panel>
                            </a-collapse>
                            </a-tab-pane>
                        <a-tab-pane key="return" tab="Trả hàng tại quầy">
                            <a-collapse accordion>
                                <a-collapse-panel header="Chính sách trả hàng tại quầy của MenWear?" key="1">
                                <p>MenWear cam kết mang đến trải nghiệm mua sắm tốt nhất và hỗ trợ trả hàng tại quầy trong vòng <strong>14 ngày</strong> kể từ ngày nhận hàng, với các điều kiện sau:</p>
                                <ul>
                                    <li>Sản phẩm còn <strong>nguyên tem, mác, chưa qua sử dụng, giặt ủi</strong> hoặc không bị hư hỏng do khách hàng.</li>
                                    <li>Sản phẩm bị <strong>lỗi do nhà sản xuất</strong> hoặc <strong>giao sai mẫu, kích thước, màu sắc</strong>.</li>
                                    <li>Có <strong>hóa đơn mua hàng</strong> hoặc <strong>mã đơn hàng</strong> để tra cứu thông tin.</li>
                                </ul>
                                <p>Chúng tôi <strong>không hỗ trợ trả hàng</strong> trong các trường hợp:</p>
                                <ul>
                                    <li>Sản phẩm đã qua sử dụng, giặt ủi hoặc bị hư hỏng do khách hàng.</li>
                                    <li>Sản phẩm không còn tem mác hoặc bị can thiệp, chỉnh sửa.</li>
                                    <li>Trả hàng do lý do cá nhân như không thích, không hợp style.</li>
                                </ul>
                                </a-collapse-panel>
                                <a-collapse-panel header="Làm thế nào để trả hàng tại quầy?" key="2">
                                <p>Để thực hiện trả hàng tại quầy, bạn vui lòng làm theo các bước sau:</p>
                                <ol>
                                    <li>Đến cửa hàng MenWear gần nhất với <strong>sản phẩm cần trả</strong> và <strong>mã đơn hàng</strong> hoặc <strong>hóa đơn mua hàng</strong>.</li>
                                    <li>Cung cấp <strong>mã đơn hàng</strong> để nhân viên tra cứu hoặc sử dụng chức năng <strong>quét mã QR</strong> trên hóa đơn (nếu có).</li>
                                    <li>Chọn sản phẩm cần trả, nhập số lượng và lý do trả hàng (ví dụ: sản phẩm lỗi, nhầm lẫn của nhân viên).</li>
                                    <li>Nhân viên sẽ kiểm tra sản phẩm và xử lý yêu cầu trả hàng ngay tại quầy. Nếu được chấp thuận, bạn sẽ nhận được <strong>hoàn tiền</strong> theo tổng số tiền của sản phẩm trả lại.</li>
                                </ol>
                                <p>Sau khi tiếp nhận, yêu cầu trả hàng sẽ được xử lý ngay lập tức tại quầy, và bạn sẽ nhận được hóa đơn trả hàng (nếu yêu cầu in). Để được hỗ trợ thêm, vui lòng liên hệ qua số điện thoại <strong>0397572262</strong> hoặc email <strong>info@menwear.com</strong>.</p>
                                </a-collapse-panel>
                            </a-collapse>
                            </a-tab-pane>
                    </a-tabs>
                </div>
            </div>
        </section>

        <!-- Contact Form Section -->
        <section class="contact-form-section" id="contactForm" ref="contactRef" :class="{ 'visible': contactVisible }">
            <div class="container">
                <div class="contact-form-wrapper">
                    <div class="contact-info">
                        <h2 class="section-title">Thông tin liên hệ</h2>
                        <div class="divider"></div>

                        <div class="contact-item">
                            <environment-outlined />
                            <div>
                                <h4>Địa chỉ cửa hàng</h4>
                                <p>13 P. Trịnh Văn Bô, Xuân Phương, Nam Từ Liêm, Hà Nội</p>
                            </div>
                        </div>

                        <div class="contact-item">
                            <phone-outlined />
                            <div>
                                <h4>Đường dây nóng</h4>
                                <p>0989 864 737</p>
                            </div>
                        </div>

                        <div class="contact-item">
                            <mail-outlined />
                            <div>
                                <h4>Email hỗ trợ</h4>
                                <p>chinhhtph46334@fpt.edu.vn</p>
                            </div>
                        </div>

                        <div class="contact-item">
                            <clock-circle-outlined />
                            <div>
                                <h4>Giờ làm việc</h4>
                                <p>Thứ 2 - Chủ nhật: 8:00 - 21:00</p>
                            </div>
                        </div>

                        <div class="social-links">
                            <a href="https://www.facebook.com/profile.php?id=100014810557839" class="social-link"><facebook-outlined /></a>
                            <a href="https://zalo.me/0989864737" class="social-link" title="Zalo">
                             <img src="../../../images/icons8-zalo-50 (1)_preview_rev_1.png" alt="">
                            </a>
                            <a href="https://www.instagram.com/hawin_syney/profilecard/?igsh=aG13dDhhZTJtNm41" class="social-link"><instagram-outlined /></a>
                        </div>
                    </div>

                    <div class="contact-form">
                        <h2 class="section-title">Gửi yêu cầu hỗ trợ</h2>
                        <div class="divider"></div>

                        <a-form layout="vertical" :model="formData" @finish="handleSubmit">
                            <a-row :gutter="16">
                                <a-col :span="12">
                                    <a-form-item label="Họ và tên" name="hoTen"
                                        :validate-status="errors.hoTen ? 'error' : ''" :help="errors.hoTen">
                                        <a-input v-model:value="formData.hoTen" placeholder="Nhập họ và tên" />
                                    </a-form-item>
                                </a-col>
                                <a-col :span="12">
                                    <a-form-item label="Số điện thoại" name="soDienThoai"
                                        :validate-status="errors.soDienThoai ? 'error' : ''" :help="errors.soDienThoai">
                                        <a-input v-model:value="formData.soDienThoai"
                                            placeholder="Nhập số điện thoại" />
                                    </a-form-item>
                                </a-col>
                            </a-row>

                            <a-form-item label="Email" name="email" :validate-status="errors.email ? 'error' : ''"
                                :help="errors.email">
                                <a-input v-model:value="formData.email" placeholder="Nhập địa chỉ email" />
                            </a-form-item>

                            <a-form-item label="Chủ đề" name="chuDe" :validate-status="errors.chuDe ? 'error' : ''"
                                :help="errors.chuDe">
                                <a-select v-model:value="formData.chuDe" placeholder="Chọn chủ đề">
                                    <a-select-option value="order">Đơn hàng</a-select-option>
                                    <a-select-option value="return">Đổi trả</a-select-option>
                                    <a-select-option value="product">Thông tin sản phẩm</a-select-option>
                                    <a-select-option value="other">Khác</a-select-option>
                                </a-select>
                            </a-form-item>

                            <a-form-item label="Nội dung" name="noiDung"
                                :validate-status="errors.noiDung ? 'error' : ''" :help="errors.noiDung">
                                <a-textarea v-model:value="formData.noiDung" :rows="4"
                                    placeholder="Nhập nội dung yêu cầu hỗ trợ" />
                            </a-form-item>

                            <a-form-item>
                                <a-button type="primary" html-type="submit" size="large" block :loading="loading">
                                    Gửi yêu cầu
                                </a-button>
                            </a-form-item>
                        </a-form>
                    </div>
                </div>
            </div>
        </section>

        <!-- Success Modal -->
        <a-modal v-model:visible="showSuccessModal" title="Yêu cầu hỗ trợ đã được gửi!" :footer="null" centered>
            <div class="success-modal-content">
                <p>Cảm ơn bạn đã gửi yêu cầu hỗ trợ. Chúng tôi sẽ phản hồi trong thời gian sớm nhất!</p>
                <a-button type="primary" block @click="showSuccessModal = false">
                    Đóng
                </a-button>
            </div>
        </a-modal>

        <!-- Support Policy Section -->
        <section class="policy-section" ref="policyRef" :class="{ 'visible': policyVisible }">
            <div class="container">
                <h2 class="section-title text-center">Chính sách chăm sóc khách hàng</h2>
                <div class="divider center-divider"></div>

                <div class="policy-cards">
                    <div class="policy-card">
                        <div class="policy-icon">
                            <safety-outlined />
                        </div>
                        <h3>Bảo hành sản phẩm</h3>
                        <div class="counter-wrapper">
                            <span class="counter">365 </span>
                            <span>ngày</span>
                        </div>
                        <p>Tất cả sản phẩm chính hãng tại MenWear đều được bảo hành theo chính sách của nhà sản xuất,
                            từ 30 đến 365 ngày tùy loại sản phẩm.</p>
                    </div>

                    <!-- <div class="policy-card">
                        <div class="policy-icon">
                            <swap-outlined />
                        </div>
                        <h3>Đổi size miễn phí</h3>
                        <p>Khách hàng được miễn phí đổi size trong vòng 15 ngày nếu sản phẩm chưa qua sử dụng và còn
                            nguyên tem mác.</p>
                    </div> -->

                    <div class="policy-card">
                        <div class="policy-icon">
                            <car-outlined />
                        </div>
                        <h3>Giao hàng tận nơi</h3>
                        <p>Giao hàng nhanh chóng trên toàn quốc với chi phí hợp lý. Miễn phí vận chuyển cho đơn hàng từ
                            2.000.000đ.</p>
                    </div>

                    <div class="policy-card">
                        <div class="policy-icon">
                            <gift-outlined />
                        </div>
                        <h3>Ưu đãi thành viên</h3>
                        <p>Trở thành thành viên để nhận nhiều ưu đãi, theo dõi đơn hàng, yêu thích sản phẩm.</p>
                    </div>
                </div>
            </div>
        </section>

        <!-- Map Section -->
        <section class="map-section" ref="mapRef" :class="{ 'visible': mapVisible }">
            <div class="container">
                <h2 class="section-title text-center">Địa điểm cửa hàng</h2>
                <div class="divider center-divider"></div>

                <div class="map-container">
                    <iframe
                        src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d4428.446608759952!2d105.74426870081332!3d21.03809188745573!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x313455e940879933%3A0xcf10b34e9f1a03df!2zVHLGsOG7nW5nIENhbyDEkeG6s25nIEZQVCBQb2x5dGVjaG5pYw!5e0!3m2!1svi!2s!4v1744306375354!5m2!1svi!2s"
                        width="100%" height="450" style="border:0;" allowfullscreen="" loading="lazy"
                        referrerpolicy="no-referrer-when-downgrade">
                    </iframe>
                </div>
            </div>
        </section>

        <!-- Scroll Indicator -->
        <div class="scroll-indicator"></div>
    </div>
</template>

<script setup>
import {
    PhoneOutlined,
    MailOutlined,
    ShopOutlined,
    EnvironmentOutlined,
    ClockCircleOutlined,
    FacebookOutlined,
    InstagramOutlined,
    SafetyOutlined,
    CarOutlined,
    GiftOutlined
} from '@ant-design/icons-vue';
import { ref, onMounted, reactive } from 'vue';
import axiosInstance from '@/config/axiosConfig'; // Nhập axiosInstance
import { message } from 'ant-design-vue';
import { useGbStore } from '@/stores/gbStore'; // Nhập Pinia store
import { useRouter } from 'vue-router'; // Nhập vue-router

// Khởi tạo store và router
const store = useGbStore();
const router = useRouter();

// Dữ liệu biểu mẫu
const formData = ref({
    hoTen: '',
    soDienThoai: '',
    email: '',
    chuDe: undefined,
    noiDung: ''
});

// Trạng thái hiển thị modal thành công
const showSuccessModal = ref(false);

// Đối tượng lưu trữ lỗi validation
const errors = reactive({
    hoTen: '',
    soDienThoai: '',
    email: '',
    chuDe: '',
    noiDung: ''
});

// Trạng thái loading khi gửi yêu cầu
const loading = ref(false);

// Refs cho các section
const heroRef = ref(null);
const optionsRef = ref(null);
const faqRef = ref(null);
const contactRef = ref(null);
const policyRef = ref(null);
const mapRef = ref(null);

// Trạng thái hiển thị của các section
const heroVisible = ref(false);
const optionsVisible = ref(false);
const faqVisible = ref(false);
const contactVisible = ref(false);
const policyVisible = ref(false);
const mapVisible = ref(false);

// Ánh xạ giá trị chuDe từ tiếng Anh sang tiếng Việt
const chuDeMapping = {
    order: 'Đơn hàng',
    return: 'Đổi trả',
    product: 'Thông tin sản phẩm',
    other: 'Khác'
};

// Hàm validate số điện thoại
const validatePhoneNumber = (phone) => {
    const cleanedPhone = phone.replace(/\s+/g, '');
    const regex = /^(0)(3[2-9]|5[2689]|7[06-9]|8[1-9]|9[0-9])[0-9]{7}$/;
    return regex.test(cleanedPhone);
};

// Hàm validate email
const validateEmail = (email) => {
    const regex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
    return regex.test(email);
};

// Hàm validate form trước khi gửi (chỉ gọi khi đã đăng nhập)
const validateForm = () => {
    let isValid = true;
    Object.keys(errors).forEach(key => {
        errors[key] = '';
    });

    // Chuẩn hóa các trường văn bản
    formData.value.hoTen = formData.value.hoTen?.replace(/\s+/g, ' ').trim() || '';
    formData.value.soDienThoai = formData.value.soDienThoai?.replace(/\s+/g, '').trim() || '';
    formData.value.email = formData.value.email?.replace(/\s+/g, '').trim() || '';
    formData.value.noiDung = formData.value.noiDung?.replace(/\s+/g, ' ').trim() || '';

    // Validate họ tên
    if (!formData.value.hoTen) {
        errors.hoTen = 'Họ và tên không được để trống';
        isValid = false;
    } else if (!/^[a-zA-Z\s\u00C0-\u1EF9]+$/.test(formData.value.hoTen)) {
        errors.hoTen = 'Họ và tên chỉ được chứa chữ cái';
        isValid = false;
    } else if (formData.value.hoTen.length > 100) {
        errors.hoTen = 'Họ và tên không được vượt quá 100 ký tự';
        isValid = false;
    } else if (formData.value.hoTen.length < 2) {
        errors.hoTen = 'Họ và tên không được nhỏ hơn 2 ký tự';
        isValid = false;
    }

    // Validate số điện thoại
    if (!formData.value.soDienThoai) {
        errors.soDienThoai = 'Số điện thoại không được để trống';
        isValid = false;
    } else if (!validatePhoneNumber(formData.value.soDienThoai)) {
        errors.soDienThoai = 'Số điện thoại phải bắt đầu bằng 0 và đúng 10 chữ số (VD: 0912345678)';
        isValid = false;
    }

    // Validate email
    if (!formData.value.email) {
        errors.email = 'Email không được để trống';
        isValid = false;
    } else if (!validateEmail(formData.value.email)) {
        errors.email = 'Email không hợp lệ (VD: example@gmail.com)';
        isValid = false;
    } else if (formData.value.email.length > 100) {
        errors.email = 'Email không được vượt quá 100 ký tự';
        isValid = false;
    }

    // Validate chủ đề
    if (!formData.value.chuDe) {
        errors.chuDe = 'Chủ đề không được để trống';
        isValid = false;
    }

    // Validate nội dung
    if (!formData.value.noiDung) {
        errors.noiDung = 'Nội dung không được để trống';
        isValid = false;
    } else if (formData.value.noiDung.length > 255) {
        errors.noiDung = 'Nội dung không được vượt quá 255 ký tự';
        isValid = false;
    }

    return isValid;
};

// Hàm xử lý khi gửi biểu mẫu
const handleSubmit = async () => {
    loading.value = true;
    try {
        // Kiểm tra trạng thái đăng nhập trước
        if (!store.isLoggedIn || !store.userDetails) {
            message.error('Bạn cần đăng nhập để gửi yêu cầu hỗ trợ!');
            setTimeout(async () => {
                await router.push('/login-register/login');
                loading.value = false;
            }, 2000); // Độ trễ 2 giây để hiển thị thông báo
            return;
        }

        // Nếu đã đăng nhập, tiến hành validate form
        if (!validateForm()) {
            loading.value = false;
            return; // Hiển thị các lỗi validation nếu có
        }

        // Ánh xạ giá trị chuDe sang tiếng Việt trước khi gửi
        const mappedFormData = {
            ...formData.value,
            chuDe: chuDeMapping[formData.value.chuDe] || formData.value.chuDe
        };

        const response = await axiosInstance.post('/api/khach-hang/send-support-request', mappedFormData);
        if (response.data.success) {
            // Hiển thị modal thành công
            showSuccessModal.value = true;
            // Xóa dữ liệu biểu mẫu
            formData.value = { hoTen: '', soDienThoai: '', email: '', chuDe: undefined, noiDung: '' };
            Object.keys(errors).forEach(key => {
                errors[key] = '';
            });
        } else {
            message.error(response.data.message || 'Có lỗi xảy ra khi gửi yêu cầu!');
        }
    } catch (error) {
        console.error('Lỗi:', error);
        if (error.response?.status === 401 || error.response?.status === 403) {
            message.error('Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại!');
            setTimeout(async () => {
                store.logoutKH();
                await router.push('/login-register/login');
            }, 2000); // Độ trễ 2 giây để hiển thị thông báo
        } else {
            message.error('Có lỗi xảy ra khi gửi yêu cầu: ' + (error.response?.data?.message || error.message));
        }
    } finally {
        loading.value = false;
    }
};

// Hàm cuộn đến phần tử
const scrollToElement = (elementId) => {
    const element = document.getElementById(elementId);
    if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
};

// Hàm mở chat (giả lập, bạn có thể tùy chỉnh)
function openChat() {
    message.info('Chức năng chat đang được phát triển!');
}

onMounted(() => {
    // Hiệu ứng cuộn: Không kiểm tra đăng nhập ở đây để khách chưa đăng nhập vẫn xem được trang
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.target === heroRef.value) {
                if (entry.isIntersecting) {
                    heroVisible.value = true;
                    observer.unobserve(entry.target);
                }
            } else if (entry.target === optionsRef.value) {
                if (entry.isIntersecting) {
                    optionsVisible.value = true;
                    observer.unobserve(entry.target);
                }
            } else if (entry.target === faqRef.value) {
                if (entry.isIntersecting) {
                    faqVisible.value = true;
                    observer.unobserve(entry.target);
                }
            } else if (entry.target === contactRef.value) {
                if (entry.isIntersecting) {
                    contactVisible.value = true;
                    observer.unobserve(entry.target);
                }
            } else if (entry.target === policyRef.value) {
                if (entry.isIntersecting) {
                    policyVisible.value = true;
                    observer.unobserve(entry.target);
                }
            } else if (entry.target === mapRef.value) {
                if (entry.isIntersecting) {
                    mapVisible.value = true;
                    observer.unobserve(entry.target);
                }
            }
        });
    }, { threshold: 0.2 });

    // Quan sát các section
    if (heroRef.value) observer.observe(heroRef.value);
    if (optionsRef.value) observer.observe(optionsRef.value);
    if (faqRef.value) observer.observe(faqRef.value);
    if (contactRef.value) observer.observe(contactRef.value);
    if (policyRef.value) observer.observe(policyRef.value);
    if (mapRef.value) observer.observe(mapRef.value);

    // Hiển thị hero section ngay khi trang load
    heroVisible.value = true;

    // Thêm hiệu ứng parallax cho hero section
    window.addEventListener('scroll', () => {
        const scrollPosition = window.scrollY;

        // Hiệu ứng parallax cho hình ảnh hero
        if (heroRef.value) {
            const heroImage = heroRef.value.querySelector('.hero-image img');
            if (heroImage) {
                heroImage.style.transform = `translateY(${scrollPosition * 0.05}px)`;
            }
        }

        // Thay đổi màu nền khi cuộn
        document.querySelectorAll('section').forEach((section, index) => {
            const sectionTop = section.offsetTop;
            const sectionHeight = section.offsetHeight;

            if (scrollPosition > (sectionTop - window.innerHeight / 2) &&
                scrollPosition < (sectionTop + sectionHeight - window.innerHeight / 2)) {
                section.classList.add('active-section');
            } else {
                section.classList.remove('active-section');
            }
        });
    });

    // Hiệu ứng đếm số khi cuộn đến section chính sách
    const animateCounters = () => {
        const counters = document.querySelectorAll('.counter');
        counters.forEach(counter => {
            const target = +counter.getAttribute('data-target');
            const count = +counter.innerText;
            const increment = target / 200;

            if (count < target) {
                counter.innerText = Math.ceil(count + increment);
                setTimeout(animateCounters, 1);
            } else {
                counter.innerText = target;
            }
        });
    };

    // Thêm các counter vào trong section chính sách
    if (policyRef.value) {
        const observer = new IntersectionObserver(entries => {
            if (entries[0].isIntersecting) {
                animateCounters();
            }
        });
        observer.observe(policyRef.value);
    }

    // Hiệu ứng thanh cuộn
    window.addEventListener('scroll', () => {
        const winScroll = document.body.scrollTop || document.documentElement.scrollTop;
        const height = document.documentElement.scrollHeight - document.documentElement.clientHeight;
        const scrolled = (winScroll / height) * 100;
        document.querySelector('.scroll-indicator').style.width = scrolled + '%';
    });
});
</script>

<style scoped>
.social-link img {
  width: 28px;
  height: 28px;
  filter: brightness(10) contrast(100) drop-shadow(0 0 2px #fff);
}
/* Base styles */
.support-page {
    font-family: 'Roboto', sans-serif;
    color: #111;
    background-color: white;
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 15px;
}

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
    font-size: 2.5rem;
    margin-bottom: 15px;
    font-weight: 700;
    color: #000;
}

.divider {
    width: 70px;
    height: 4px;
    background-color: #e53935;
    margin-bottom: 30px;
}

.center-divider {
    margin-left: auto;
    margin-right: auto;
}

.text-center {
    text-align: center;
}

/* Hero Section */
.support-hero {
    background: linear-gradient(135deg, #fff 0%, #f5f5f5 100%);
    min-height: 550px;
    display: flex;
    align-items: center;
}

.support-hero .container {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
}

.hero-content {
    flex: 1;
    padding-right: 30px;
    min-width: 300px;
}

.hero-image {
    flex: 1;
    min-width: 350px;
    text-align: center;
}

.hero-image img {
    max-width: 150%;
    height: 350px;
    animation: floatAnimation 4s ease-in-out infinite;
}

.hero-title {
    font-size: 3.5rem;
    margin-bottom: 20px;
    color: #000;
    font-weight: 800;
    line-height: 1.2;
    font-family: 'Montserrat', sans-serif !important;
}

.hero-description {
    font-size: 1.4rem;
    margin-bottom: 30px;
    color: #333;
    line-height: 1.6;
}

.hero-buttons {
    display: flex;
    gap: 15px;
    flex-wrap: wrap;
}

@keyframes floatAnimation {
    0% {
        transform: translateY(0px);
    }

    50% {
        transform: translateY(-15px);
    }

    100% {
        transform: translateY(0px);
    }
}

/* Support Options */
.support-options {
    background-color: #f9f9f9;
    text-align: center;
}

.support-options .section-title {
    color: #000;
}

.options-container {
    display: flex;
    justify-content: center;
    gap: 30px;
    flex-wrap: wrap;
    margin-top: 40px;
}

.support-option {
    background-color: white;
    border-radius: 10px;
    padding: 30px 20px;
    width: 250px;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;
}

.support-option:hover {
    transform: translateY(-10px);
    box-shadow: 0 10px 25px rgba(229, 57, 53, 0.25);
}

.option-icon {
    font-size: 48px;
    color: #e53935;
    margin-bottom: 20px;
}

.support-option h3 {
    font-size: 1.5rem;
    margin-bottom: 15px;
    color: #000;
    font-weight: 600;
}

.support-option p {
    color: #333;
    margin-bottom: 10px;
    font-size: 1.1rem;
}

.support-option .time {
    display: block;
    font-size: 0.95rem;
    color: #777;
    margin-bottom: 15px;
}

.visible .support-option {
    animation: fadeInUp 0.6s ease forwards;
}

.visible .support-option:nth-child(1) {
    animation-delay: 0.1s;
}

.visible .support-option:nth-child(2) {
    animation-delay: 0.2s;
}

.visible .support-option:nth-child(3) {
    animation-delay: 0.3s;
}

.visible .support-option:nth-child(4) {
    animation-delay: 0.4s;
}

/* FAQ Section */
.faq-section {
    background-color: #fff;
}

.faq-categories {
    margin-top: 30px;
}

:deep(.ant-collapse-header) {
    font-weight: 600;
    font-size: 1.1rem;
}

:deep(.ant-collapse-content-box p),
:deep(.ant-collapse-content-box li) {
    font-size: 1.05rem;
}

/* Contact Form Section */
.contact-form-section {
    background-color: #f8f8f8;
}

.contact-form-wrapper {
    display: flex;
    flex-wrap: wrap;
    gap: 30px;
    margin-top: 20px;
}

.contact-info {
    flex: 1;
    min-width: 300px;
    background-color: #f9f9f9;
    padding: 35px;
    border-radius: 10px;
    color: #333;
    border: 1px solid #eee;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.contact-info .section-title {
    color: #000;
}

.contact-form {
    flex: 2;
    min-width: 300px;
    background-color: #fff;
    padding: 35px;
    border-radius: 10px;
    box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.contact-item {
    display: flex;
    margin-bottom: 25px;
    align-items: flex-start;
}

.contact-item .anticon {
    font-size: 24px;
    color: #e53935;
    margin-right: 15px;
    margin-top: 5px;
}

.contact-item h4 {
    margin: 0 0 5px 0;
    color: #000;
    font-size: 1.2rem;
}

.contact-item p {
    margin: 0;
    color: #555;
    font-size: 1.05rem;
}

.social-links {
    margin-top: 30px;
    display: flex;
    gap: 12px;
}

.social-link {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 45px;
    height: 45px;
    border-radius: 50%;
    background-color: #e53935;
    color: white;
    transition: all 0.3s ease;
    font-size: 1.2rem;
}

.social-link:hover {
    background-color: #c62828;
    transform: translateY(-5px);
}

/* Form labels và inputs lớn hơn */
:deep(.ant-form-item-label > label) {
    font-size: 1.1rem;
}

:deep(.ant-input),
:deep(.ant-select-selector),
:deep(.ant-btn) {
    font-size: 1.05rem;
}

/* Success Modal Styling */
.success-modal-content {
    text-align: center;
}

.success-modal-content p {
    font-size: 1.1rem;
    margin-bottom: 20px;
    color: #333;
}

/* Policy Section */
.policy-section {
    background-color: #fff;
    text-align: center;
    color: #333;
}

.policy-section .section-title {
    color: #000;
}

.policy-cards {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 30px;
    margin-top: 50px;
}

.policy-card {
    background-color: #f9f9f9;
    border: 1px solid #eee;
    border-radius: 12px;
    padding: 35px;
    width: 260px;
    transition: all 0.3s ease;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}

.policy-card:hover {
    transform: translateY(-10px);
    box-shadow: 0 10px 25px rgba(229, 57, 53, 0.15);
    border-color: #e53935;
}

.policy-icon {
    font-size: 48px;
    color: #e53935;
    margin-bottom: 25px;
}

.policy-card h3 {
    font-size: 1.5rem;
    margin-bottom: 15px;
    color: #000;
    font-weight: 600;
}

.policy-card p {
    color: #555;
    line-height: 1.6;
    font-size: 1.05rem;
}

/* Map Section */
.map-section {
    padding-bottom: 0;
    background-color: white;
}

.map-container {
    margin-top: 40px;
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    border: 1px solid #eee;
}

/* Override Ant Design styles */
:deep(.ant-btn-primary) {
    background-color: #e53935;
    border-color: #e53935;
    font-weight: 500;
}

:deep(.ant-btn-primary:hover) {
    background-color: #c62828;
    border-color: #c62828;
}

:deep(.ant-tabs-ink-bar) {
    background-color: #e53935;
    height: 3px;
}

:deep(.ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn) {
    color: #e53935;
    font-size: 1.1rem;
    font-weight: 600;
}

:deep(.ant-tabs-tab) {
    font-size: 1.1rem;
}

:deep(.ant-tabs-tab:hover) {
    color: #e53935;
}

:deep(.ant-collapse-item:hover) {
    border-color: #e53935;
}

/* Animation */
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

/* Responsive */
@media (max-width: 768px) {

    .hero-content,
    .hero-image {
        flex: 100%;
        padding-right: 0;
        text-align: center;
    }

    .hero-image {
        margin-top: 30px;
    }

    .hero-title {
        font-size: 2.8rem;
    }

    .section-title {
        font-size: 2.2rem;
    }

    section {
        padding: 60px 0;
    }

    .contact-info,
    .contact-form {
        flex: 100%;
    }

    .hero-buttons {
        justify-content: center;
    }
}

@media (max-width: 576px) {

    .support-option,
    .policy-card {
        width: 100%;
    }
}

/* Thêm CSS riêng cho vạch cam ở section support-options */
.support-options .divider {
    background-color: #FF5722;
    width: 100px;
    margin-left: auto;
    margin-right: auto;
}

/* Hiệu ứng cuộn */
html {
    scroll-behavior: smooth;
}

section {
    position: relative;
    transition: transform 0.5s ease, background-color 0.5s ease;
}

section.active-section {
    z-index: 2;
}

.visible {
    animation: fadeInScale 0.8s ease forwards;
}

@keyframes fadeInScale {
    0% {
        opacity: 0;
        transform: scale(0.96) translateY(30px);
    }

    100% {
        opacity: 1;
        transform: scale(1) translateY(0);
    }
}

/* Hiệu ứng hover 3D cho các cards */
.support-option,
.policy-card {
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    perspective: 1000px;
}

.support-option:hover,
.policy-card:hover {
    transform: translateY(-10px) rotateX(5deg);
    box-shadow: 0 15px 30px rgba(229, 57, 53, 0.2);
}

/* Animation cho divider */
.divider {
    position: relative;
    overflow: hidden;
}

.divider::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.5), transparent);
    animation: shine 2s infinite;
}

@keyframes shine {
    to {
        left: 100%;
    }
}

/* Hiệu ứng sóng cho các button khi hover */
:deep(.ant-btn) {
    position: relative;
    overflow: hidden;
}

:deep(.ant-btn)::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 5px;
    height: 5px;
    background: rgba(255, 255, 255, 0.5);
    opacity: 0;
    border-radius: 100%;
    transform: scale(1, 1) translate(-50%, -50%);
    transform-origin: 50% 50%;
}

:deep(.ant-btn):hover::after {
    animation: ripple 1s ease-out;
}

@keyframes ripple {
    0% {
        transform: scale(0, 0);
        opacity: 0.5;
    }

    100% {
        transform: scale(100, 100);
        opacity: 0;
    }
}

/* Hiệu ứng scroll indicator */
.scroll-indicator {
    position: fixed;
    top: 0;
    left: 0;
    height: 3px;
    background-color: #e53935;
    z-index: 1000;
    width: 0%;
    transition: width 0.1s ease;
}
</style>