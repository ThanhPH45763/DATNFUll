<template>
  <div class="table-responsive mt-4">
    <table class="table table-hover">
      <thead>
        <tr class="">
          <th scope="col">#</th>
          <th scope="col">Ảnh</th>
          <th scope="col">Tên sản phẩm</th>
          <th scope="col">Số lượng</th>
          <th scope="col">Giá bán</th>
          <th scope="col">Tổng tiền</th>
          <th scope="col">Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>{{ index + 1 }}</td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { useGbStore } from '@/stores/gbStore';

export default {
  setup() {
    const store = useGbStore();
    return {
      store
    }
  }
}
</script>

<style lang="scss" scoped></style>