<template>
  <div>
    <router-view></router-view>
    
    <!-- Nút scroll to top -->
    <button 
      @click="scrollToTop" 
      class="scroll-to-top"
      :class="{ 'show': showScrollButton }"
    >
      <i class="fas fa-arrow-up"></i>
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

const showScrollButton = ref(false);

// Hàm kiểm tra vị trí scroll để hiện/ẩn nút
const checkScroll = () => {
  // Hiện nút khi scroll xuống 300px
  showScrollButton.value = window.scrollY > 300;
};

// Hàm scroll lên đầu trang
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth' // Tạo hiệu ứng scroll mượt
  });
};

// Thêm event listener khi component được mount
onMounted(() => {
  window.addEventListener('scroll', checkScroll);
});

// Cleanup event listener khi component bị destroy
onUnmounted(() => {
  window.removeEventListener('scroll', checkScroll);
});
</script>

<style>
.scroll-to-top {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 40px;
  height: 40px;
  background-color: #f33b47;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  z-index: 1000;
}

.scroll-to-top.show {
  opacity: 1;
  visibility: visible;
}

.scroll-to-top:hover {
  background-color: #d62d3a;
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

/* Animation khi hover */
.scroll-to-top i {
  transition: transform 0.3s ease;
}

.scroll-to-top:hover i {
  transform: translateY(-2px);
}

/* Responsive */
@media (max-width: 768px) {
  .scroll-to-top {
    bottom: 20px;
    right: 20px;
    width: 35px;
    height: 35px;
  }
}
</style>
