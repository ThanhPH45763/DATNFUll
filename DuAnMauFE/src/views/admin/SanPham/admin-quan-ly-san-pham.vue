<template>
    <!-- <tableAllSanPham></tableAllSanPham> -->
    <tableSanPham></tableSanPham>
    <router-view></router-view>
</template>

<script>
import tableAllSanPham from '@/components/admin-components/table-admin/table-admin-allSanPham.vue';
import tableSanPham from '@/components/admin-components/QuanLySanPham/tableSanPham.vue';
export default {
    components: {
        tableAllSanPham,
        tableSanPham
    }
}
</script>

<style scoped></style>