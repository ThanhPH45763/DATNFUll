<template>
    <div>
        <h3>Danh sách chi tiết sản phẩm</h3>
        <table class="table table-bordered">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Tên sản phẩm</th>
                    <th>Giá</th>
                    <th>Số lượng</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(item, index) in store.getAllChiTietSanPham" :key="item.id_chi_tiet_san_pham">
                    <td>{{ index + 1 }}</td>
                    <td>{{ item.ten_san_pham }}</td>
                    <td>{{ item.gia_ban }}</td>
                    <td>{{ item.so_luong }}</td>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useGbStore } from '@/stores/gbStore';
onMounted(() => {
    store.getAllCTSP();
})

const store = useGbStore()


// Định dạng giá tiền
</script>

<style scoped>
.table {
    width: 100%;
    margin-top: 20px;
}
</style>