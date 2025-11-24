<template>
    <div class="web p-0 m-0">
        <TheNoitification />
        <TheBaner />
        <BoSuuTapHot />
        <!-- <DanhMucSanPham /> -->
        <SanPhamBanChay />
        <!-- <SanPhamMoiChoTatCa /> -->
        <AoTheThaoBanChay />
        <!-- <QuanTheThaoBanChay /> -->
        <ThuongHieuSanPham />
    </div>
    <!-- <ChatWidget :customerId="'1'" :staffId="'1'" senderType="CUSTOMER" :anou="false" /> -->
</template>

<script>
import TheBaner from '@/components/TheBaner.vue';
import SanPhamBanChay from '@/components/ingredient-web-components/SanPhamBanChay.vue';
import BoSuuTapHot from '@/components/ingredient-web-components/BoSuuTapHot.vue';
import SanPhamMoiChoTatCa from '@/components/ingredient-web-components/SanPhamMoiChoTatCa.vue';
import AoTheThaoBanChay from '@/components/ingredient-web-components/AoTheThaoBanChay.vue';
import QuanTheThaoBanChay from '@/components/ingredient-web-components/QuanTheThaoBanChay.vue';
import DanhMucSanPham from '@/components/ingredient-web-components/DanhMucSanPham.vue';
import ThuongHieuSanPham from '@/components/ingredient-web-components/ThuongHieuSanPham.vue';
import TheNoitification from '@/components/TheNoitification.vue';
// import ChatWidget from '@/components/ChatWidge/ChatWidget.vue';

export default {
    components: {
        TheBaner,
        AoTheThaoBanChay,
        QuanTheThaoBanChay,
        SanPhamBanChay,
        BoSuuTapHot,
        SanPhamMoiChoTatCa,
        DanhMucSanPham,
        ThuongHieuSanPham,
        TheNoitification,
        // ChatWidget
    }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&display=swap');

.web {
    font-family: 'Montserrat', sans-serif;
}
</style>