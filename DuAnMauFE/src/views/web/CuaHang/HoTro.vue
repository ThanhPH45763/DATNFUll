<template>
    <div>
        <HoTro/>
    </div>
    <!-- <ChatWidget :customerId="'1'" :staffId="'1'" senderType="CUSTOMER" :anou="false" /> -->
</template>

<script>
import HoTro from '@/components/web-components/CuaHang/hoTro.vue'
// import ChatWidget from '@/components/ChatWidge/ChatWidget.vue'
export default {
    components: {
        HoTro
        // ChatWidget
    }
}
</script>
