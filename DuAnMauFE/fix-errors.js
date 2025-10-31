import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const filePath = path.join(process.cwd(), 'src/components/admin-components/QuanLySanPham/tableSanPham.vue');

try {
    // Đọc nội dung file
    let content = fs.readFileSync(filePath, 'utf8');

    // Sửa lỗi thẻ a-image không đóng và a-table lồng nhau không cần thiết
    content = content.replace(
        /<template v-if="column\.key === 'hinh_anh'">([\s\S]*?)<a-table/,
        '<template v-if="column.key === \'hinh_anh\'">\n                        <a-image style="width: 40px; height: 40px;" :src="record.hinh_anh" />\n                    </template>\n                    <a-table'
    );

    // Xóa định nghĩa trùng lặp của hàm filterCTSPByStatus
    content = content.replace(
        /\/\/ Hàm xử lý lọc trạng thái chi tiết sản phẩm\nconst filterCTSPByStatus = \(e\) => \{\n[\s\S]*?\n\};\n\n\/\/ Hàm thay đổi trạng thái/,
        '// Hàm thay đổi trạng thái'
    );

    // Ghi nội dung đã sửa lại vào file
    fs.writeFileSync(filePath, content, 'utf8');

    console.log('Đã sửa thành công các lỗi trong file tableSanPham.vue');
} catch (error) {
    console.error('Lỗi khi sửa file:', error);
} 