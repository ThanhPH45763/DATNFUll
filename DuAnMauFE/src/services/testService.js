import axiosInstance from "@/config/axiosConfig";

const uploadImage = async (file) => {
    if (!file) {
        console.warn('Không có file để tải lên');
        return null;
    }

    console.log('Đang tải lên file:', file.name, 'Kích thước:', file.size, 'Type:', file.type);
    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await axiosInstance.post('/testImage', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });

        console.log('Phản hồi đầy đủ từ Cloudinary:', response);

        if (response && response.data) {
            return response.data;
        } else if (response) {
            return response;
        }
        return null;
    } catch (error) {
        console.error('Chi tiết lỗi khi tải ảnh lên:', error);
        if (error.response) {
            console.error('Dữ liệu phản hồi từ server:', error.response.data);
        }
        throw error;
    }
}
const deleteImage = async (publicId) => {
    const response = await axiosInstance.delete('testDeleteImage?publicId=' + publicId);
    return response.data;
}

const deleteHinhAnh = async (idHinhAnh) => {
    const response = await axiosInstance.delete(`/admin/quan_ly_hinh_anh/deleteHinhAnh/${idHinhAnh}`);
    return response.data;
}

export const testService = {
    uploadImage,
    deleteImage,
    deleteHinhAnh
}
