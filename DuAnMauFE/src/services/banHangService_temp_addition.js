const updateCustomerInfo = async (idHD, tenKhachHang, soDienThoai, email, diaChi) => {
    try {
        const params = new URLSearchParams();
        params.append('idHD', idHD);
        params.append('tenKhachHang', tenKhachHang);
        params.append('soDienThoai', soDienThoai);
        if (email) params.append('email', email);
        if (diaChi) params.append('diaChi', diaChi);

        const { data } = await axiosInstance.post(banHang + `updateCustomerInfo?${params.toString()}`);
        console.log('✅ API updateCustomerInfo response:', data);
        return data;
    } catch (error) {
        console.error('❌ Lỗi API update customer info:', error);
        return { error: true };
    }
}

banHangService.updateCustomerInfo = updateCustomerInfo;
