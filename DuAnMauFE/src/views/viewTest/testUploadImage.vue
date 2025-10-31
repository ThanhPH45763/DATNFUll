<template>
    <div class="upload-container">
        <input type="file" @change="onFileChange" accept="image/*" />
        <button @click="uploadImage" :disabled="loading">
            {{ loading ? 'Đang upload...' : 'Upload' }}
        </button>

        <div v-if="imageUrl" class="preview-container">
            <img :src="imageUrl" alt="Preview" class="preview-image" />
            <button @click="deleteImage" class="delete-button" :disabled="loading">
                Xóa ảnh
            </button>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import axiosInstance from '@/config/axiosConfig';
import { message } from 'ant-design-vue';

const file = ref(null);
const imageUrl = ref('');
const loading = ref(false);

const onFileChange = (event) => {
    const selectedFile = event.target.files[0];
    if (selectedFile && selectedFile.type.startsWith('image/')) {
        file.value = selectedFile;
        // Preview ảnh
        const reader = new FileReader();
        reader.onload = (e) => {
            imageUrl.value = e.target.result;
        };
        reader.readAsDataURL(selectedFile);
    } else {
        message.error('Vui lòng chọn file ảnh!');
    }
};
//upload ảnh
const uploadImage = async () => {
    if (!file.value) {
        message.warning('Vui lòng chọn ảnh trước!');
        return;
    }

    loading.value = true;
    const formData = new FormData();
    formData.append('file', file.value); // Phải đúng tên 'file' để match với @RequestParam

    try {
        const response = await axiosInstance.post('testImage?file=', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
        console.log('Response:', response.data); // Debug response
        if (response.data) {
            imageUrl.value = response.data; // URL từ Cloudinary
            message.success('Upload ảnh thành công!');
        }
    } catch (error) {
        console.error('Upload error:', error);
        message.error('Có lỗi khi upload ảnh: ' + (error.response?.data || error.message));
    } finally {
        loading.value = false;
    }
};
//xóa ảnh
const deleteImage = async () => {
    if (!imageUrl.value) {
        message.warning('Không có ảnh để xóa!');
        return;
    }

    loading.value = true;
    try {
        // Lấy publicId từ URL
        const urlParts = imageUrl.value.split('/');
        const fileName = urlParts[urlParts.length - 1];
        const publicId = fileName.split('.')[0]; // Lấy phần trước .jpg hoặc .png

        // Gọi API xóa ảnh
        await axiosInstance.delete("testDeleteImage?publicId=" + publicId);

        // Reset state sau khi xóa
        imageUrl.value = '';
        file.value = null;
        message.success('Xóa ảnh thành công!');
    } catch (error) {
        console.error('Xóa ảnh error:', error);
        message.error('Có lỗi khi xóa ảnh: ' + (error.response?.data || error.message));
    } finally {
        loading.value = false;
    }
};
</script>

<style scoped>
/* Thêm style cho trạng thái loading */
button {
    padding: 8px 16px;
    background-color: #f33b47;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    position: relative;

    /* Disable button khi đang loading */
    &:disabled {
        opacity: 0.7;
        cursor: not-allowed;
    }
}

/* Thêm style cho container */
.upload-container {
    padding: 20px;
    border: 2px dashed #ddd;
    border-radius: 8px;
    text-align: center;
    margin: 20px 0;
}

/* Style cho preview image */
.preview-image {
    max-width: 300px;
    margin-top: 20px;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

input[type="file"] {
    margin-right: 10px;
}
</style>