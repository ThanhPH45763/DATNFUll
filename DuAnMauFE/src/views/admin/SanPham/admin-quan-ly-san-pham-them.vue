<template>
    <div>
        <menuAction></menuAction>
        <themSanPham></themSanPham>
        <router-view></router-view>
    </div>
</template>

<script>
import menuAction from '@/components/admin-components/QuanLySanPham/menuAction.vue';
import themSanPham from '@/components/admin-components/QuanLySanPham/themSanPham.vue';
export default {
    components: {
        menuAction,
        themSanPham
    }
}
</script>
