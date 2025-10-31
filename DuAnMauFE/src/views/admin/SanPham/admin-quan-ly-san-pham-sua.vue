<template>
    <div>
        <menuAction />
        <suaSanPham />
        <router-view />
    </div>
</template>

<script>
import suaSanPham from '@/components/admin-components/QuanLySanPham/suaSanPham.vue';
import menuAction from '@/components/admin-components/QuanLySanPham/menuAction.vue';
export default {
    components: {
        suaSanPham,
        menuAction
    }
}
</script>

<style lang="scss" scoped></style>