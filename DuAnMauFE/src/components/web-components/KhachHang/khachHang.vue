<template>
  <div class="customer-profile">
    <!-- Header profile -->
    <a-row :gutter="[24, 24]">
      <a-col :span="24">
        <a-card class="profile-header">
          <div class="profile-content">
            <div class="profile-info">
              <div class="profile-info-header">
                <h2>{{ userInfo.name || 'Khách hàng' }}</h2>
              </div>
              <p class="user-email"><mail-outlined /> {{ userInfo.email }}</p>
              <p class="user-member-since"><calendar-outlined /> Thành viên từ: {{ formatDate(userInfo.createdAt) }}</p>
              <div class="user-stats">
                <div class="stat-item">
                  <shopping-outlined />
                  <span>{{ orders.length || 0 }}</span>
                  <p>Đơn hàng</p>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <environment-outlined />
                  <span>{{ addresses.length || 0 }}</span>
                  <p>Địa chỉ</p>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <heart-outlined />
                  <span>{{ favoriteProducts || 0 }}</span>
                  <p>Sản phẩm đã yêu thích</p>
                  <a-dropdown v-if="favoriteProducts > 0" :trigger="['click']" placement="bottomRight"
                    overlay-class-name="favorite-products-dropdown">
                    <a-button type="link" class="view-favorites-btn">
                      <span class="favorites-btn-content">
                        Xem sản phẩm <down-outlined />
                      </span>
                    </a-button>
                    <template #overlay>
                      <div class="favorites-dropdown-container">
                        <div class="favorites-header">
                          <heart-outlined />
                          <span>Sản phẩm đã yêu thích</span>
                          <span class="favorites-count">{{ favoriteProducts }}</span>
                        </div>
                        <a-menu class="favorites-dropdown-menu">
                          <a-menu-item v-if="favoritesList.length === 0" disabled>
                            <div class="favorites-loading">
                              <a-spin size="small" />
                              <span>Đang tải danh sách...</span>
                            </div>
                          </a-menu-item>
                          <a-menu-item v-for="item in favoritesList" :key="item.id_chi_tiet_san_pham"
                            @click="goToProductDetail(item)">
                            <div class="favorite-product-item">
                              <div class="favorite-product-image">
                                <img :src="item.hinh_anh" :alt="item.ten_san_pham" />
                              </div>
                              <div class="favorite-product-info">
                                <div class="product-name">{{ item.ten_san_pham }}</div>
                                <div class="product-details">
                                  <span v-if="item.ten_mau_sac">{{ item.ten_mau_sac }}</span>
                                  <span v-if="item.gia_tri && item.ten_mau_sac"> - {{ item.gia_tri }}</span>
                                  <span v-if="item.gia_tri && !item.ten_mau_sac">{{ item.gia_tri }}</span>
                                </div>
                                <div class="product-price">{{ formatCurrency(item.gia_ban) }}</div>
                              </div>
                            </div>
                          </a-menu-item>
                        </a-menu>
                      </div>
                    </template>
                  </a-dropdown>
                </div>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- Menu và nội dung -->
    <a-row :gutter="[24, 24]" class="mt-4">
      <a-col :xs="24" :md="6">
        <a-card title="Tài khoản của tôi">
          <a-menu v-model:selectedKeys="selectedMenu" mode="inline" style="border-right: 0">
            <a-menu-item key="info">
              <template #icon><user-outlined /></template>
              Thông tin cá nhân
            </a-menu-item>
            <a-menu-item key="orders">
              <template #icon><shopping-outlined /></template>
              Đơn hàng của tôi
            </a-menu-item>
            <a-menu-item key="address">
              <template #icon><environment-outlined /></template>
              Sổ địa chỉ
            </a-menu-item>
            <a-menu-item key="password">
              <template #icon><lock-outlined /></template>
              Đổi mật khẩu
            </a-menu-item>
            <a-menu-item key="logout">
              <template #icon><logout-outlined /></template>
              Đăng xuất
            </a-menu-item>
          </a-menu>
        </a-card>
      </a-col>

      <a-col :xs="24" :md="18">
        <!-- Thông tin cá nhân -->
        <a-card v-if="selectedMenu.includes('info')" title="Thông tin cá nhân">
          <a-form :model="userInfo" layout="vertical">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="Nhập tên" :validateStatus="nameValidation.status" :help="nameValidation.message">
                  <a-input v-model:value="userInfo.name" placeholder="Nhập tên" @change="validateName" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="Email">
                  <a-input v-model:value="userInfo.email" disabled placeholder="Email" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="Số điện thoại" :validateStatus="phoneValidation.status"
                  :help="phoneValidation.message">
                  <a-input v-model:value="userInfo.phone" placeholder="Nhập số điện thoại" @change="validatePhone" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="Ngày sinh" :validateStatus="birthdayValidation.status"
                  :help="birthdayValidation.message">
                  <a-date-picker v-model:value="userInfo.birthday" style="width: 100%" format="DD/MM/YYYY"
                    @change="validateBirthday" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item>
              <a-button type="primary" :loading="isUpdating" @click="updateProfile" class="update-profile-btn"
                size="large" :disabled="!isFormValid">
                Cập nhật thông tin
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>

        <!-- Đơn hàng -->
        <a-card v-if="selectedMenu.includes('orders')" title="Đơn hàng của tôi">
          <a-tabs v-model:activeKey="orderTabActive">
            <!-- Tab Tất cả -->
            <a-tab-pane key="all" tab="Tất cả">
              <a-empty v-if="allOrders.length === 0" description="Bạn chưa có đơn hàng nào" />
              <a-list v-else :data-source="allOrders" :pagination="{ pageSize: 5 }">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-card style="width: 100%">
                      <a-row>
                        <a-col :span="16">
                          <p>Mã đơn: {{ item.ma_hoa_don }}</p>
                          <p>Ngày đặt: {{ formatDate(item.ngay_tao) }}</p>
                          <p>Trạng thái:
                            <a-tag :color="getOrderStatusColor(item.trang_thai)">
                              {{ item.trang_thai }}
                            </a-tag>
                          </p>
                        </a-col>
                        <a-col :span="8" style="text-align: right">
                          <p>Tổng tiền: {{ formatCurrency(item.tong_tien_sau_giam) }}</p>
                          <div style="display: flex; gap: 8px; justify-content: flex-end">
                            <a-button type="primary" size="small" @click="viewOrderDetail(item.id_hoa_don)">Chi
                              tiết</a-button>
                            <a-button type="primary" size="small" @click="showUpdateCustomerModal(item)"
                              v-if="['Chờ xác nhận'].includes(item.trang_thai)">Cập nhật
                              KH</a-button>
                          </div>
                        </a-col>
                      </a-row>
                    </a-card>
                  </a-list-item>
                </template>
              </a-list>
            </a-tab-pane>

            <!-- Tab Chờ xác nhận -->
            <a-tab-pane key="pending" tab="Chờ xác nhận">
              <a-empty v-if="pendingOrders.length === 0" description="Không có đơn hàng nào đang chờ xác nhận" />
              <a-list v-else :data-source="pendingOrders" :pagination="{ pageSize: 5 }">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-card style="width: 100%">
                      <a-row>
                        <a-col :span="16">
                          <p>Mã đơn: {{ item.ma_hoa_don }}</p>
                          <p>Ngày đặt: {{ formatDate(item.ngay_tao) }}</p>
                          <p>Trạng thái:
                            <a-tag color="orange">{{ item.trang_thai }}</a-tag>
                          </p>
                        </a-col>
                        <a-col :span="8" style="text-align: right">
                          <p>Tổng tiền: {{ formatCurrency(item.tong_tien_sau_giam) }}</p>
                          <div style="display: flex; gap: 8px; justify-content: flex-end">
                            <a-button type="primary" size="small" @click="viewOrderDetail(item.id_hoa_don)">Chi
                              tiết</a-button>
                            <a-button type="primary" size="small" @click="showUpdateCustomerModal(item)"
                              v-if="['Chờ xác nhận'].includes(item.trang_thai)">Cập nhật
                              KH</a-button>
                          </div>
                        </a-col>
                      </a-row>
                    </a-card>
                  </a-list-item>
                </template>
              </a-list>
            </a-tab-pane>

            <!-- Tab Đã xác nhận -->
            <a-tab-pane key="confirmed" tab="Đã xác nhận">
              <a-empty v-if="confirmedOrders.length === 0" description="Không có đơn hàng nào đã xác nhận" />
              <a-list v-else :data-source="confirmedOrders" :pagination="{ pageSize: 5 }">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-card style="width: 100%">
                      <a-row>
                        <a-col :span="16">
                          <p>Mã đơn: {{ item.ma_hoa_don }}</p>
                          <p>Ngày đặt: {{ formatDate(item.ngay_tao) }}</p>
                          <p>Trạng thái:
                            <a-tag color="orange">{{ item.trang_thai }}</a-tag>
                          </p>
                        </a-col>
                        <a-col :span="8" style="text-align: right">
                          <p>Tổng tiền: {{ formatCurrency(item.tong_tien_sau_giam) }}</p>
                          <div style="display: flex; gap: 8px; justify-content: flex-end">
                            <a-button type="primary" size="small" @click="viewOrderDetail(item.id_hoa_don)">Chi
                              tiết</a-button>
                          </div>
                        </a-col>
                      </a-row>
                    </a-card>
                  </a-list-item>
                </template>
              </a-list>
            </a-tab-pane>

            <!-- Tab Chờ đóng gói -->
            <a-tab-pane key="packing" tab="Chờ đóng gói">
              <a-empty v-if="packingOrders.length === 0" description="Không có đơn hàng nào đang chờ đóng gói" />
              <a-list v-else :data-source="packingOrders" :pagination="{ pageSize: 5 }">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-card style="width: 100%">
                      <a-row>
                        <a-col :span="16">
                          <p>Mã đơn: {{ item.ma_hoa_don }}</p>
                          <p>Ngày đặt: {{ formatDate(item.ngay_tao) }}</p>
                          <p>Trạng thái:
                            <a-tag color="orange">{{ item.trang_thai }}</a-tag>
                          </p>
                        </a-col>
                        <a-col :span="8" style="text-align: right">
                          <p>Tổng tiền: {{ formatCurrency(item.tong_tien_sau_giam) }}</p>
                          <div style="display: flex; gap: 8px; justify-content: flex-end">
                            <a-button type="primary" size="small" @click="viewOrderDetail(item.id_hoa_don)">Chi
                              tiết</a-button>
                          </div>
                        </a-col>
                      </a-row>
                    </a-card>
                  </a-list-item>
                </template>
              </a-list>
            </a-tab-pane>

            <!-- Tab Đang giao -->
            <a-tab-pane key="shipping" tab="Đang giao">
              <a-empty v-if="shippingOrders.length === 0" description="Không có đơn hàng nào đang giao" />
              <a-list v-else :data-source="shippingOrders" :pagination="{ pageSize: 5 }">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-card style="width: 100%">
                      <a-row>
                        <a-col :span="16">
                          <p>Mã đơn: {{ item.ma_hoa_don }}</p>
                          <p>Ngày đặt: {{ formatDate(item.ngay_tao) }}</p>
                          <p>Trạng thái:
                            <a-tag color="blue">{{ item.trang_thai }}</a-tag>
                          </p>
                        </a-col>
                        <a-col :span="8" style="text-align: right">
                          <p>Tổng tiền: {{ formatCurrency(item.tong_tien_sau_giam) }}</p>
                          <a-button type="primary" size="small" @click="viewOrderDetail(item.id_hoa_don)">Chi
                            tiết</a-button>
                        </a-col>
                      </a-row>
                    </a-card>
                  </a-list-item>
                </template>
              </a-list>
            </a-tab-pane>

            <!-- Tab Hoàn thành -->
            <a-tab-pane key="completed" tab="Hoàn thành">
              <a-empty v-if="completedOrders.length === 0" description="Không có đơn hàng nào hoàn thành" />
              <a-list v-else :data-source="completedOrders" :pagination="{ pageSize: 5 }">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-card style="width: 100%">
                      <a-row>
                        <a-col :span="16">
                          <p>Mã đơn: {{ item.ma_hoa_don }}</p>
                          <p>Ngày đặt: {{ formatDate(item.ngay_tao) }}</p>
                          <p>Trạng thái:
                            <a-tag color="green">{{ item.trang_thai }}</a-tag>
                          </p>
                        </a-col>
                        <a-col :span="8" style="text-align: right">
                          <p>Tổng tiền: {{ formatCurrency(item.tong_tien_sau_giam) }}</p>
                          <a-button type="primary" size="small" @click="viewOrderDetail(item.id_hoa_don)">Chi
                            tiết</a-button>
                        </a-col>
                      </a-row>
                    </a-card>
                  </a-list-item>
                </template>
              </a-list>
            </a-tab-pane>

            <!-- Tab Đã hủy -->
            <a-tab-pane key="cancelled" tab="Đã hủy">
              <a-empty v-if="cancelledOrders.length === 0" description="Không có đơn hàng nào đã hủy" />
              <a-list v-else :data-source="cancelledOrders" :pagination="{ pageSize: 5 }">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-card style="width: 100%">
                      <a-row>
                        <a-col :span="16">
                          <p>Mã đơn: {{ item.ma_hoa_don }}</p>
                          <p>Ngày đặt: {{ formatDate(item.ngay_tao) }}</p>
                          <p>Trạng thái:
                            <a-tag color="red">{{ item.trang_thai }}</a-tag>
                          </p>
                        </a-col>
                        <a-col :span="8" style="text-align: right">
                          <p>Tổng tiền: {{ formatCurrency(item.tong_tien_sau_giam) }}</p>
                          <a-button type="primary" size="small" @click="viewOrderDetail(item.id_hoa_don)">Chi
                            tiết</a-button>
                        </a-col>
                      </a-row>
                    </a-card>
                  </a-list-item>
                </template>
              </a-list>
            </a-tab-pane>
          </a-tabs>
        </a-card>

        <!-- Địa chỉ -->
        <a-card v-if="selectedMenu.includes('address')" title="Sổ địa chỉ">
          <a-button type="primary" style="margin-bottom: 16px; display: flex; align-items: center; gap: 8px"
            @click="showAddAddressModal">
            <plus-outlined /> <span>Thêm địa chỉ mới</span>
          </a-button>

          <a-list :data-source="addresses" :pagination="{ pageSize: 3 }">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-card style="width: 100%">
                  <div style="display: flex; justify-content: space-between">
                    <div>
                      <div style="display: flex; align-items: center; gap: 8px">
                        <strong>{{ store.userDetails.tenKhachHang }}</strong>
                        <a-tag v-if="item.isDefault" color="blue">Mặc định</a-tag>
                      </div>
                      <p style="margin: 8px 0">{{ store.userDetails.soDienThoai }}</p>
                      <p>{{ item.address }}, {{ item.ward }}, {{ item.district }}, {{ item.province }}</p>
                    </div>
                    <div>
                      <a-button type="link" @click="editAddress(item)">Sửa</a-button>
                      <a-popconfirm v-if="addresses.length > 1" title="Bạn có chắc muốn xóa địa chỉ này không?"
                        ok-text="Có" cancel-text="Không" @confirm="deleteAddress(item.id)">
                        <a-button type="link" danger>Xóa</a-button>
                      </a-popconfirm>
                    </div>
                  </div>
                </a-card>
              </a-list-item>
            </template>
          </a-list>
        </a-card>

        <!-- Đổi mật khẩu -->
        <a-card v-if="selectedMenu.includes('password')" title="Đổi mật khẩu">
          <a-form :model="passwordForm" layout="vertical">
            <a-form-item label="Mật khẩu hiện tại" name="currentPassword">
              <a-input-password v-model:value="passwordForm.currentPassword" placeholder="Nhập mật khẩu hiện tại" />
            </a-form-item>
            <a-form-item label="Mật khẩu mới" name="newPassword">
              <a-input-password v-model:value="passwordForm.newPassword" placeholder="Nhập mật khẩu mới" />
            </a-form-item>
            <a-form-item label="Xác nhận mật khẩu mới" name="confirmPassword">
              <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="Nhập lại mật khẩu mới" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="changePassword">Đổi mật khẩu</a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
    </a-row>

    <!-- Modal thêm/sửa địa chỉ -->
    <a-modal v-model:visible="addressModal.visible" :title="addressModal.isEdit ? 'Sửa địa chỉ' : 'Thêm địa chỉ mới'"
      :footer="null" :width="550" class="mt-5">
      <div>
        <div class="row mb-3">
          <div class="col-md-4">
            <label>Tỉnh/Thành phố</label>
            <a-select v-model:value="addressModal.data.provinceCode" placeholder="Chọn Tỉnh/Thành phố"
              style="width: 100%" @change="handleProvinceChange">
              <a-select-option v-for="province in provinces" :key="province.code" :value="province.code">
                {{ province.name }}
              </a-select-option>
            </a-select>
          </div>
          <div class="col-md-4">
            <label>Quận/Huyện</label>
            <a-select v-model:value="addressModal.data.districtCode" placeholder="Chọn Quận/Huyện" style="width: 100%"
              :disabled="!addressModal.data.provinceCode" @change="handleDistrictChange">
              <a-select-option v-for="district in districts" :key="district.code" :value="district.code">
                {{ district.name }}
              </a-select-option>
            </a-select>
          </div>
          <div class="col-md-4">
            <label>Phường/Xã</label>
            <a-select v-model:value="addressModal.data.wardCode" placeholder="Chọn Phường/Xã" style="width: 100%"
              :disabled="!addressModal.data.districtCode">
              <a-select-option v-for="ward in wards" :key="ward.code" :value="ward.code">
                {{ ward.name }}
              </a-select-option>
            </a-select>
          </div>
        </div>

        <div class="mb-3">
          <label>Địa chỉ cụ thể</label>
          <a-input v-model:value="addressModal.data.address" placeholder="Số nhà, tên đường..." />
        </div>

        <div class="mb-3">
          <a-checkbox v-model:checked="addressModal.data.isDefault"
            :disabled="addressModal.isEdit && addressModal.data.isDefault && addresses.length === 1">
            Đặt làm địa chỉ mặc định
          </a-checkbox>
        </div>

        <div class="text-end">
          <a-button @click="addressModal.visible = false" class="me-2">Hủy</a-button>
          <a-button type="primary" @click="saveAddress">Lưu</a-button>
        </div>
      </div>
    </a-modal>
    <!-- Template modal chi tiết đơn hàng đã được cập nhật -->
    <a-modal v-model:visible="orderDetailModal.visible" :title="`Chi tiết đơn hàng: ${orderDetailModal.maHoaDon}`"
      :footer="null" :width="800" class="order-detail-modal" centered>
      <a-spin :spinning="orderDetailModal.loading">
        <div v-if="orderDetailModal.data" class="modal-content">
          <!-- Thông tin chung -->
          <div class="section-container">
            <h3 class="section-title">Thông tin đơn hàng</h3>
            <a-descriptions bordered :column="1" size="small">
              <a-descriptions-item label="Mã đơn hàng">
                {{ orderDetailModal.data.ma_hoa_don }}
              </a-descriptions-item>
              <a-descriptions-item label="Ngày đặt">
                {{ formatDate(orderDetailModal.data.ngay_tao) }}
              </a-descriptions-item>
              <a-descriptions-item label="Trạng thái">
                <a-tag :color="getOrderStatusColor(orderDetailModal.data.trang_thai)">
                  {{ orderDetailModal.data.trang_thai }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="Phương thức thanh toán">
                {{ orderDetailModal.data.hinh_thuc_thanh_toan || 'Tiền mặt' }}
              </a-descriptions-item>
            </a-descriptions>
          </div>

          <!-- Thông tin người nhận -->
          <div class="section-container">
            <h3 class="section-title">Thông tin người nhận</h3>
            <a-descriptions bordered :column="1" size="small">
              <a-descriptions-item label="Tên người nhận">
                {{ orderDetailModal.data.ho_ten || 'Không có thông tin' }}
              </a-descriptions-item>
              <a-descriptions-item label="Số điện thoại">
                {{ orderDetailModal.data.sdt_nguoi_nhan || 'Không có thông tin' }}
              </a-descriptions-item>
              <a-descriptions-item label="Địa chỉ">
                {{ orderDetailModal.data.dia_chi || 'Không có thông tin' }}
              </a-descriptions-item>
              <a-descriptions-item label="Ghi chú">
                <a-tooltip :title="orderDetailModal.data.ghi_chu || 'Không có ghi chú'">
                  <span class="truncate">{{ orderDetailModal.data.ghi_chu || 'Không có ghi chú' }}</span>
                </a-tooltip>
              </a-descriptions-item>
            </a-descriptions>
          </div>

          <!-- Danh sách sản phẩm -->
          <div class="section-container">
            <h3 class="section-title">Sản phẩm đã mua</h3>
            <a-table :columns="orderProductColumns" :data-source="orderDetailModal.products" :pagination="false"
              :rowKey="record => record.id_chi_tiet_san_pham" size="small" class="compact-table">
              <template #bodyCell="{ column, text, record }">
                <template v-if="column.dataIndex === 'hinh_anh'">
                  <img :src="record.hinh_anh" :alt="record.ten_san_pham" class="product-image" />
                </template>
                <template v-else-if="column.dataIndex === 'ten_san_pham'">
                  <div class="product-info">
                    <div class="product-name truncate">{{ record.ten_san_pham }}</div>
                    <div class="product-attrs">
                      <span v-if="record.mau_sac">{{ record.mau_sac }}</span>
                      <span v-if="record.kich_thuoc"> - {{ record.kich_thuoc }}</span>
                    </div>
                  </div>
                </template>
                <template v-else-if="column.dataIndex === 'gia'">
                  {{ formatCurrency(record.gia) }}
                </template>
                <template v-else-if="column.dataIndex === 'thanh_tien'">
                  {{ formatCurrency(record.gia * record.so_luong) }}
                </template>
                <template v-else-if="column.dataIndex === 'actions'">
                  <a-button v-if="orderDetailModal.data.trang_thai === 'Hoàn thành'" type="primary" size="small"
                    @click="goToProductReview(record)">Đánh giá</a-button>
                </template>
              </template>
            </a-table>
          </div>

          <!-- Thông tin thanh toán -->
          <div class="section-container">
            <h3 class="section-title">Thanh toán</h3>
            <div class="payment-summary">
              <div class="summary-item">
                <span>Tổng tiền sản phẩm</span>
                <span>{{ formatCurrency(calculateProductTotal()) }}</span>
              </div>
              <div class="summary-item">
                <span>Phí vận chuyển</span>
                <span>{{ formatCurrency(orderDetailModal.data.phi_van_chuyen || 0) }}</span>
              </div>
              <div class="summary-item" v-if="orderDetailModal.data.giam_gia > 0">
                <span>Giảm giá</span>
                <span class="discount-value">- {{ formatCurrency(orderDetailModal.data.giam_gia || 0) }}</span>
              </div>
              <div class="summary-item total-row">
                <span>Tổng thanh toán:</span>
                <span class="total-value">{{ formatCurrency(calculateProductTotal() +
                  (orderDetailModal.data.phi_van_chuyen
                    || 0) - (orderDetailModal.data.giam_gia || 0)) }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else-if="!orderDetailModal.loading" class="empty-data">
          <a-empty description="Không có thông tin đơn hàng" />
        </div>
      </a-spin>
      <div class="modal-footer">
        <a-button @click="orderDetailModal.visible = false" class="btn-close">Đóng</a-button>
        <a-button v-if="orderDetailModal.data && orderDetailModal.data.trang_thai === 'Chờ xác nhận'" type="danger"
          @click="cancelOrder(orderDetailModal.data.id_hoa_don)" class="btn-cancel">
          Hủy đơn hàng
        </a-button>
      </div>
    </a-modal>

    <!-- Modal cập nhật thông tin khách hàng -->
    <a-modal v-model:visible="updateCustomerModal.visible" title="Cập nhật thông tin khách hàng" :footer="null"
      :width="550" class="mt-5">
      <a-form :model="updateCustomerModal.data" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="Họ tên" :validate-status="updateCustomerModal.nameValidation.status"
              :help="updateCustomerModal.nameValidation.message">
              <a-input v-model:value="updateCustomerModal.data.ho_ten" placeholder="Nhập họ tên"
                @change="validateUpdateCustomerName" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="Số điện thoại" :validate-status="updateCustomerModal.phoneValidation.status"
              :help="updateCustomerModal.phoneValidation.message">
              <a-input v-model:value="updateCustomerModal.data.sdt_nguoi_nhan" placeholder="Nhập số điện thoại"
                @change="validateUpdateCustomerPhone" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="Số nhà, đường" :validate-status="updateCustomerModal.addressValidation.status"
              :help="updateCustomerModal.addressValidation.message">
              <a-input v-model:value="updateCustomerModal.data.soNha" placeholder="Nhập số nhà, tên đường"
                @change="validateAddress" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="Tỉnh/Thành phố">
              <a-select v-model:value="updateCustomerModal.data.provinceCode" placeholder="Chọn Tỉnh/Thành phố"
                style="width: 100%" @change="handleProvinceChangeForUpdate">
                <a-select-option v-for="province in provinces" :key="province.code" :value="province.code">
                  {{ province.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="Quận/Huyện">
              <a-select v-model:value="updateCustomerModal.data.districtCode" placeholder="Chọn Quận/Huyện"
                style="width: 100%" :disabled="!updateCustomerModal.data.provinceCode"
                @change="handleDistrictChangeForUpdate">
                <a-select-option v-for="district in districts" :key="district.code" :value="district.code">
                  {{ district.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="Phường/Xã">
              <a-select v-model:value="updateCustomerModal.data.wardCode" placeholder="Chọn Phường/Xã"
                style="width: 100%" :disabled="!updateCustomerModal.data.districtCode">
                <a-select-option v-for="ward in wards" :key="ward.code" :value="ward.code">
                  {{ ward.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
      <div class="text-end">
        <a-button @click="updateCustomerModal.visible = false" class="me-2">Hủy</a-button>
        <a-button type="primary" :loading="updateCustomerModal.loading" @click="saveCustomerInfo">Lưu</a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue';
import {
  UserOutlined,
  ShoppingOutlined,
  EnvironmentOutlined,
  LockOutlined,
  LogoutOutlined,
  MailOutlined,
  CalendarOutlined,
  HeartOutlined,
  PlusOutlined,
  DownOutlined
} from '@ant-design/icons-vue';
import { message, Modal } from 'ant-design-vue';
import axiosInstance from '@/config/axiosConfig';
import { useGbStore } from '@/stores/gbStore';
import { useRouter } from 'vue-router';
import dayjs from 'dayjs';
import { khachHangService } from '@/services/khachHangService';
import { hoaDonService } from '@/services/hoaDonService';
import { favoriteService } from '@/services/favoriteService';
import { toast } from 'vue3-toastify';
import { banHangService } from '@/services/banHangService';

// Add a global center message for consistent UX
if (!window.centerMessage) {
  window.centerMessage = {
    success: (content, duration = 3) => {
      message.success({
        content,
        duration,
        style: { marginTop: '20vh' }
      });
    },
    error: (content, duration = 3) => {
      message.error({
        content,
        duration,
        style: { marginTop: '20vh' }
      });
    },
    warning: (content, duration = 3) => {
      message.warning({
        content,
        duration,
        style: { marginTop: '20vh' }
      });
    },
    info: (content, duration = 3) => {
      message.info({
        content,
        duration,
        style: { marginTop: '20vh' }
      });
    },
    loading: (content, duration = 0) => {
      const hide = message.loading({
        content,
        duration,
        style: { marginTop: '20vh' }
      });
      return hide;
    }
  };
}

// Router và store
const router = useRouter();
const store = useGbStore();

// State cho menu
const selectedMenu = ref(['info']);
const orderTabActive = ref('all');

// Thông tin người dùng mặc định
const defaultAvatar = 'https://joeschmoe.io/api/v1/random';
const userInfo = reactive({
  name: '',
  email: '',
  phone: '',
  birthday: null,
  avatar: '',
  createdAt: new Date()
});

// Thêm state loading cho nút cập nhật
const isUpdating = ref(false);

// Danh sách đơn hàng, địa chỉ và sản phẩm yêu thích
const orders = ref([]);
const addresses = ref([]);
const favoriteProducts = ref(0);
const favoritesList = ref([]);

// Dữ liệu cho địa chỉ
const provinces = ref([]);
const districts = ref([]);
const wards = ref([]);

// State cho modal cập nhật thông tin khách hàng
const updateCustomerModal = reactive({
  visible: false,
  loading: false,
  data: {
    ma_hoa_don: '',
    ho_ten: '',
    sdt_nguoi_nhan: '',
    dia_chi: '',
    soNha: '',
    provinceCode: '',
    districtCode: '',
    wardCode: ''
  },
  nameValidation: {
    status: '',
    message: ''
  },
  phoneValidation: {
    status: '',
    message: ''
  },
  addressValidation: {
    status: '',
    message: ''
  }
});

// State cho modal địa chỉ
const addressModal = reactive({
  visible: false,
  isEdit: false,
  data: {
    id: null,
    provinceCode: '',
    province: '',
    districtCode: '',
    district: '',
    wardCode: '',
    ward: '',
    address: '',
    isDefault: false
  }
});

// State cho modal chi tiết đơn hàng
const orderDetailModal = reactive({
  visible: false,
  loading: false,
  maHoaDon: '',
  data: null,
  products: []
});

// Computed properties cho các trạng thái đơn hàng
const allOrders = computed(() => orders.value);
const pendingOrders = computed(() => orders.value.filter(o => o.trang_thai.toLowerCase() === 'chờ xác nhận'));
const confirmedOrders = computed(() => orders.value.filter(o => o.trang_thai.toLowerCase() === 'đã xác nhận'));
const packingOrders = computed(() => orders.value.filter(o => o.trang_thai.toLowerCase() === 'chờ đóng gói'));
const shippingOrders = computed(() => orders.value.filter(o => o.trang_thai.toLowerCase() === 'đang giao'));
const completedOrders = computed(() => orders.value.filter(o => o.trang_thai.toLowerCase() === 'hoàn thành'));
const cancelledOrders = computed(() => orders.value.filter(o => o.trang_thai.toLowerCase() === 'đã hủy'));

// Form đổi mật khẩu
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// Validation states
const nameValidation = reactive({
  status: '',
  message: ''
});

const phoneValidation = reactive({
  status: '',
  message: ''
});

const birthdayValidation = reactive({
  status: '',
  message: ''
});

// Kiểm tra tính hợp lệ của form
const isFormValid = computed(() => {
  return nameValidation.status !== 'error' &&
    phoneValidation.status !== 'error' &&
    birthdayValidation.status !== 'error' &&
    userInfo.name &&
    userInfo.phone &&
    userInfo.birthday;
});

// Format ngày
const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  return `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getFullYear()}`;
};

// Format tiền tệ
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
};

// Màu tag theo trạng thái đơn hàng
const getOrderStatusColor = (status) => {
  const colors = {
    'Chờ xác nhận': 'orange',
    'Đã xác nhận': 'orange',
    'Chờ đóng gói': 'orange',
    'Đang giao': 'blue',
    'Hoàn thành': 'green',
    'Đã hủy': 'red'
  };
  return colors[status] || 'default';
};

// Cấu hình cột cho bảng sản phẩm trong đơn hàng
const orderProductColumns = [
  {
    title: 'Hình ảnh',
    dataIndex: 'hinh_anh',
    width: 60,
  },
  {
    title: 'Sản phẩm',
    dataIndex: 'ten_san_pham',
  },
  {
    title: 'Số lượng',
    dataIndex: 'so_luong',
    width: 80,
    align: 'center',
  },
  {
    title: 'Đơn giá',
    dataIndex: 'gia',
    width: 100,
    align: 'right',
  },
  {
    title: 'Thành tiền',
    dataIndex: 'thanh_tien',
    width: 100,
    align: 'right',
  },
  {
    title: 'Hành động',
    dataIndex: 'actions',
    width: 100,
    align: 'center',
  },
];
// Hàm tính tổng tiền sản phẩm
const calculateProductTotal = () => {
  if (!orderDetailModal.products || orderDetailModal.products.length === 0) return 0;
  return orderDetailModal.products.reduce((total, item) => {
    const itemTotal = Number(item.gia || 0) * Number(item.so_luong || 1);
    return total + itemTotal;
  }, 0);
};

// Validate họ tên
const validateName = () => {
  const specialCharsAndNumbers = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?0-9]/;
  userInfo.name = userInfo.name.trim().replace(/\s+/g, ' ');
  if (userInfo.name.length > 100) {
    nameValidation.status = 'error';
    nameValidation.message = 'Tên khách hàng không được vượt quá 100 ký tự';
    return false;
  }
  if (userInfo.name.length < 2) {
    nameValidation.status = 'error';
    nameValidation.message = 'Tên khách hàng không được nhỏ hơn 2 ký tự';
    return false;
  }
  if (!userInfo.name) {
    nameValidation.status = 'error';
    nameValidation.message = 'Vui lòng nhập tên';
    return false;
  }
  if (specialCharsAndNumbers.test(userInfo.name)) {
    nameValidation.status = 'error';
    nameValidation.message = 'Tên khách hàng không được chứa số hoặc ký tự đặc biệt';
    return false;
  }
  nameValidation.status = 'success';
  nameValidation.message = '';
  return true;
};

// Validate số điện thoại
const validatePhone = () => {
  const phoneRegex = /^[0-9]{10}$/;
  const hasLetters = /[a-zA-Z]/;
  const hasSpecialChars = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/;
  userInfo.phone = userInfo.phone.trim().replace(/\s+/g, '');
  if (!userInfo.phone) {
    phoneValidation.status = 'error';
    phoneValidation.message = 'Vui lòng nhập số điện thoại';
    return false;
  }
  if (hasLetters.test(userInfo.phone)) {
    phoneValidation.status = 'error';
    phoneValidation.message = 'Số điện thoại không được chứa chữ cái';
    return false;
  }
  if (hasSpecialChars.test(userInfo.phone)) {
    phoneValidation.status = 'error';
    phoneValidation.message = 'Số điện thoại không được chứa ký tự đặc biệt';
    return false;
  }
  if (userInfo.phone.length > 10) {
    phoneValidation.status = 'error';
    phoneValidation.message = 'Số điện thoại không được vượt quá 10 chữ số';
    return false;
  }
  if (!phoneRegex.test(userInfo.phone)) {
    phoneValidation.status = 'error';
    phoneValidation.message = 'Số điện thoại phải có đúng 10 chữ số';
    return false;
  }
  phoneValidation.status = 'success';
  phoneValidation.message = '';
  return true;
};

// Validate ngày sinh
const validateBirthday = () => {
  if (!userInfo.birthday) {
    birthdayValidation.status = 'error';
    birthdayValidation.message = 'Vui lòng chọn ngày sinh';
    return false;
  }
  const birthDate = userInfo.birthday.toDate();
  const today = new Date();
  let age = today.getFullYear() - birthDate.getFullYear();
  const monthDiff = today.getMonth() - birthDate.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
    age--;
  }
  if (age < 14) {
    birthdayValidation.status = 'error';
    birthdayValidation.message = 'Bạn phải đủ 14 tuổi trở lên';
    return false;
  }
  birthdayValidation.status = 'success';
  birthdayValidation.message = '';
  return true;
};

// Validate họ tên trong modal cập nhật khách hàng
const validateUpdateCustomerName = () => {
  const specialCharsAndNumbers = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?0-9]/;
  updateCustomerModal.data.ho_ten = updateCustomerModal.data.ho_ten.trim().replace(/\s+/g, ' ');
  if (updateCustomerModal.data.ho_ten.length > 100) {
    updateCustomerModal.nameValidation.status = 'error';
    updateCustomerModal.nameValidation.message = 'Tên khách hàng không được vượt quá 100 ký tự';
    return false;
  }
  if (updateCustomerModal.data.ho_ten.length < 2) {
    updateCustomerModal.nameValidation.status = 'error';
    updateCustomerModal.nameValidation.message = 'Tên khách hàng không được nhỏ hơn 2 ký tự';
    return false;
  }
  if (!updateCustomerModal.data.ho_ten) {
    updateCustomerModal.nameValidation.status = 'error';
    updateCustomerModal.nameValidation.message = 'Vui lòng nhập tên';
    return false;
  }
  if (specialCharsAndNumbers.test(updateCustomerModal.data.ho_ten)) {
    updateCustomerModal.nameValidation.status = 'error';
    updateCustomerModal.nameValidation.message = 'Tên khách hàng không được chứa số hoặc ký tự đặc biệt';
    return false;
  }
  updateCustomerModal.nameValidation.status = 'success';
  updateCustomerModal.nameValidation.message = '';
  return true;
};

// Validate số điện thoại trong modal cập nhật khách hàng
const validateUpdateCustomerPhone = () => {
  const phoneRegex = /^[0-9]{10}$/;
  const hasLetters = /[a-zA-Z]/;
  const hasSpecialChars = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/;
  updateCustomerModal.data.sdt_nguoi_nhan = updateCustomerModal.data.sdt_nguoi_nhan.trim().replace(/\s+/g, '');
  if (!updateCustomerModal.data.sdt_nguoi_nhan) {
    updateCustomerModal.phoneValidation.status = 'error';
    updateCustomerModal.phoneValidation.message = 'Vui lòng nhập số điện thoại';
    return false;
  }
  if (hasLetters.test(updateCustomerModal.data.sdt_nguoi_nhan)) {
    updateCustomerModal.phoneValidation.status = 'error';
    updateCustomerModal.phoneValidation.message = 'Số điện thoại không được chứa chữ cái';
    return false;
  }
  if (hasSpecialChars.test(updateCustomerModal.data.sdt_nguoi_nhan)) {
    updateCustomerModal.phoneValidation.status = 'error';
    updateCustomerModal.phoneValidation.message = 'Số điện thoại không được chứa ký tự đặc biệt';
    return false;
  }
  if (updateCustomerModal.data.sdt_nguoi_nhan.length > 10) {
    updateCustomerModal.phoneValidation.status = 'error';
    updateCustomerModal.phoneValidation.message = 'Số điện thoại không được vượt quá 10 chữ số';
    return false;
  }
  if (!phoneRegex.test(updateCustomerModal.data.sdt_nguoi_nhan)) {
    updateCustomerModal.phoneValidation.status = 'error';
    updateCustomerModal.phoneValidation.message = 'Số điện thoại phải có đúng 10 chữ số';
    return false;
  }
  updateCustomerModal.phoneValidation.status = 'success';
  updateCustomerModal.phoneValidation.message = '';
  return true;
};

// Validate số nhà
const validateAddress = () => {
  updateCustomerModal.data.soNha = updateCustomerModal.data.soNha.trim().replace(/\s+/g, ' ');
  const specialChars = /[!@#$%^&*()_+\-=\[\]{};':"\\|.<>?~`]/; // Ký tự đặc biệt không cho phép

  if (!updateCustomerModal.data.soNha) {
    updateCustomerModal.addressValidation.status = 'error';
    updateCustomerModal.addressValidation.message = 'Vui lòng nhập số nhà, tên đường';
    return false;
  }
  if (updateCustomerModal.data.soNha.length > 200) {
    updateCustomerModal.addressValidation.status = 'error';
    updateCustomerModal.addressValidation.message = 'Địa chỉ không được vượt quá 200 ký tự';
    return false;
  }
  if (specialChars.test(updateCustomerModal.data.soNha)) {
    updateCustomerModal.addressValidation.status = 'error';
    updateCustomerModal.addressValidation.message = 'Số nhà, tên đường không được chứa ký tự đặc biệt';
    return false;
  }
  updateCustomerModal.addressValidation.status = 'success';
  updateCustomerModal.addressValidation.message = '';
  return true;
};

// Hàm phân tích địa chỉ
const parseAddress = (address) => {
  if (!address) return { soNha: '', ward: '', district: '', province: '' };

  // Giả định địa chỉ có định dạng: "Số nhà, Phường/Xã, Quận/Huyện, Tỉnh/Thành phố"
  const parts = address.split(',').map(part => part.trim());
  if (parts.length < 4) {
    return { soNha: parts[0] || '', ward: '', district: '', province: '' };
  }

  return {
    soNha: parts[0],
    ward: parts[1],
    district: parts[2],
    province: parts[3]
  };
};

// Hiển thị modal cập nhật thông tin khách hàng
const showUpdateCustomerModal = async (order) => {
  try {
    // Reset modal data
    updateCustomerModal.data = {
      ma_hoa_don: order.ma_hoa_don,
      ho_ten: order.ho_ten || '',
      sdt_nguoi_nhan: order.sdt_nguoi_nhan || '',
      dia_chi: order.dia_chi || '',
      soNha: '',
      provinceCode: '',
      districtCode: '',
      wardCode: ''
    };
    updateCustomerModal.nameValidation = { status: '', message: '' };
    updateCustomerModal.phoneValidation = { status: '', message: '' };
    updateCustomerModal.addressValidation = { status: '', message: '' };

    // Phân tích địa chỉ
    const parsedAddress = parseAddress(order.dia_chi);
    updateCustomerModal.data.soNha = parsedAddress.soNha;

    // Đảm bảo danh sách tỉnh/thành phố đã được tải
    if (!provinces.value.length) {
      await loadProvinces();
    }

    // Tìm tỉnh/thành phố
    const province = provinces.value.find(p => p.name === parsedAddress.province);
    if (province) {
      updateCustomerModal.data.provinceCode = province.code;

      // Tải danh sách quận/huyện
      await handleProvinceChangeForUpdate(province.code);

      // Tìm quận/huyện
      const district = districts.value.find(d => d.name === parsedAddress.district);
      if (district) {
        updateCustomerModal.data.districtCode = district.code;

        // Tải danh sách phường/xã
        await handleDistrictChangeForUpdate(district.code);

        // Tìm phường/xã
        const ward = wards.value.find(w => w.name === parsedAddress.ward);
        if (ward) {
          updateCustomerModal.data.wardCode = ward.code;
        }
      }
    }

    // Mở modal
    updateCustomerModal.visible = true;
  } catch (error) {
    console.error('Lỗi khi hiển thị modal cập nhật khách hàng:', error);
    window.centerMessage.error('Không thể tải thông tin địa chỉ');
  }
};

// Xử lý khi chọn tỉnh/thành phố trong modal cập nhật
const handleProvinceChangeForUpdate = async (value) => {
  updateCustomerModal.data.districtCode = '';
  updateCustomerModal.data.wardCode = '';
  districts.value = [];
  wards.value = [];
  try {
    const response = await fetch(`https://provinces.open-api.vn/api/p/${value}?depth=2`);
    const data = await response.json();
    districts.value = data.districts || [];
  } catch (error) {
    console.error('Lỗi khi tải quận/huyện:', error);
    window.centerMessage.error('Không thể tải danh sách quận/huyện');
  }
};

// Xử lý khi chọn quận/huyện trong modal cập nhật
const handleDistrictChangeForUpdate = async (value) => {
  updateCustomerModal.data.wardCode = '';
  wards.value = [];
  try {
    const response = await fetch(`https://provinces.open-api.vn/api/d/${value}?depth=2`);
    const data = await response.json();
    wards.value = data.wards || [];
  } catch (error) {
    console.error('Lỗi khi tải phường/xã:', error);
    window.centerMessage.error('Không thể tải danh sách phường/xã');
  }
};

// Hàm tính phí vận chuyển
const calculatePhiVanChuyen = async () => {
  try {
    // Kiểm tra dữ liệu hợp lệ
    if (!orderDetailModal.data || !orderDetailModal.data.tong_tien_sau_giam) {
      console.error('Missing order data for shipping fee calculation');
      return 0;
    }

    // Điều kiện miễn phí vận chuyển
    if (orderDetailModal.data.tong_tien_sau_giam >= 2000000) {
      console.log('Miễn phí vận chuyển: Tổng tiền >= 2,000,000 VNĐ');
      return 0;
    }

    const weight = 500; // 500g mỗi sản phẩm
    const tongTienHoaDon = orderDetailModal.data.tong_tien_sau_giam - (orderDetailModal.data.phi_van_chuyen || 0);
    const provinceName = provinces.value.find(p => p.code === updateCustomerModal.data.provinceCode)?.name || '';
    const districtName = districts.value.find(d => d.code === updateCustomerModal.data.districtCode)?.name || '';

    if (!updateCustomerModal.data || !provinceName || !districtName) {
      console.warn('⚠️ Không có địa chỉ giao hàng hợp lệ, phí = 0');
      console.log('Địa chỉ cụ thể:', updateCustomerModal.data.soNha, districtName, provinceName, weight, tongTienHoaDon, orderDetailModal.data.tong_tien_sau_giam);
      return 0;
    }

    // Gọi API tính phí vận chuyển
    const phiShip = await banHangService.tinhPhiShip(
      'Hà Nội',
      'Nam Từ Liêm',
      provinceName,
      districtName,
      weight,
      tongTienHoaDon
    );

    console.log('Địa chỉ cụ thể:', updateCustomerModal.data.soNha, districtName, provinceName);
    console.log('Dữ liệu:', districtName, provinceName, weight, tongTienHoaDon, orderDetailModal.data.tong_tien_sau_giam, orderDetailModal.data.phi_van_chuyen);
    console.log('Phí ship:', phiShip.fee);

    return phiShip.fee || 0;
  } catch (error) {
    console.error('Lỗi khi tính phí vận chuyển:', error);
    toast.error('Có lỗi xảy ra khi tính phí vận chuyển');
    return 0;
  }
};

// Lưu thông tin khách hàng
const saveCustomerInfo = async () => {
  // Validate dữ liệu
  validateUpdateCustomerName();
  validateUpdateCustomerPhone();
  validateAddress();

  // Kiểm tra các trường địa chỉ
  if (!updateCustomerModal.data.provinceCode || !updateCustomerModal.data.districtCode || !updateCustomerModal.data.wardCode) {
    window.centerMessage.error('Vui lòng chọn đầy đủ Tỉnh/Thành phố, Quận/Huyện, Phường/Xã');
    return;
  }

  if (
    updateCustomerModal.nameValidation.status === 'error' ||
    updateCustomerModal.phoneValidation.status === 'error' ||
    updateCustomerModal.addressValidation.status === 'error'
  ) {
    window.centerMessage.error('Vui lòng kiểm tra lại thông tin');
    return;
  }

  // Ghép chuỗi địa chỉ
  const provinceName = provinces.value.find(p => p.code === updateCustomerModal.data.provinceCode)?.name || '';
  const districtName = districts.value.find(d => d.code === updateCustomerModal.data.districtCode)?.name || '';
  const wardName = wards.value.find(w => w.code === updateCustomerModal.data.wardCode)?.name || '';
  const fullAddress = `${updateCustomerModal.data.soNha}, ${wardName}, ${districtName}, ${provinceName}`;
  updateCustomerModal.data.dia_chi = fullAddress.trim();

  updateCustomerModal.loading = true;
  try {
    // Tính phí vận chuyển mới
    const phiVanChuyen = await calculatePhiVanChuyen();
    console.log('Phí vận chuyển tính được:', phiVanChuyen);

    // Gửi yêu cầu cập nhật thông tin khách hàng
    const response = await axiosInstance.post(
      '/api/khach-hang/update-order-info',
      {
        maHoaDon: updateCustomerModal.data.ma_hoa_don,
        hoTen: updateCustomerModal.data.ho_ten,
        sdtNguoiNhan: updateCustomerModal.data.sdt_nguoi_nhan,
        diaChi: updateCustomerModal.data.dia_chi
      },
      {
        params: {
          phiVanChuyen: phiVanChuyen
        }
      }
    );

    if (response.data.success) {
      window.centerMessage.success('Cập nhật thông tin khách hàng thành công');
      updateCustomerModal.visible = false;

      // Cập nhật lại danh sách đơn hàng
      await fetchOrders();

      // Cập nhật chi tiết đơn hàng trong modal nếu đang mở
      if (orderDetailModal.visible && orderDetailModal.data?.ma_hoa_don === updateCustomerModal.data.ma_hoa_don) {
        orderDetailModal.data.ho_ten = updateCustomerModal.data.ho_ten;
        orderDetailModal.data.sdt_nguoi_nhan = updateCustomerModal.data.sdt_nguoi_nhan;
        orderDetailModal.data.dia_chi = updateCustomerModal.data.dia_chi;
        orderDetailModal.data.phi_van_chuyen = phiVanChuyen;
        orderDetailModal.data.tong_tien_sau_giam = calculateProductTotal() + phiVanChuyen - (orderDetailModal.data.giam_gia || 0);
      }
    } else {
      window.centerMessage.error(response.data.message || 'Cập nhật thất bại');
    }
  } catch (error) {
    console.error('Lỗi khi cập nhật thông tin khách hàng:', error);
    window.centerMessage.error('Có lỗi xảy ra khi cập nhật');
  } finally {
    updateCustomerModal.loading = false;
  }
};

// Cập nhật thông tin cá nhân
const updateProfile = async (event) => {
  if (event) event.preventDefault();
  validateName();
  validatePhone();
  validateBirthday();
  if (!isFormValid.value) {
    window.centerMessage.error('Vui lòng kiểm tra lại thông tin');
    return;
  }
  if (isUpdating.value) return;
  try {
    Modal.confirm({
      title: 'Xác nhận cập nhật',
      content: 'Bạn có chắc chắn muốn cập nhật thông tin cá nhân?',
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      centered: true,
      onOk: async () => {
        const loadingMessage = window.centerMessage.loading('Đang cập nhật thông tin...');
        isUpdating.value = true;
        try {
          let diaChiList = store.userDetails.diaChiList || [];
          if (!diaChiList.length) {
            const currentAddresses = await khachHangService.fetchDiaChiByKhachHangId(store.userDetails.idKhachHang);
            diaChiList = currentAddresses.map((diaChi) => ({
              idDiaChiKhachHang: diaChi.id_dia_chi_khach_hang || diaChi.idDiaChiKhachHang,
              soNha: diaChi.so_nha || diaChi.soNha,
              xaPhuong: diaChi.xa_phuong || diaChi.xaPhuong,
              quanHuyen: diaChi.quan_huyen || diaChi.quanHuyen,
              tinhThanhPho: diaChi.tinh_thanh_pho || diaChi.tinhThanhPho,
              diaChiMacDinh: diaChi.dia_chi_mac_dinh || diaChi.diaChiMacDinh || false,
            }));
          }

          const updateData = {
            idKhachHang: store.userDetails.idKhachHang,
            maKhachHang: store.userDetails.maKhachHang,
            tenKhachHang: userInfo.name.trim(),
            gioiTinh: store.userDetails.gioiTinh,
            soDienThoai: userInfo.phone.trim(),
            ngaySinh: userInfo.birthday.toDate(),
            email: userInfo.email,
            trangThai: store.userDetails.trangThai || 'Đang hoạt động',
            diaChiList: diaChiList,
          };
          const result = await store.suaKhachHang(updateData);
          loadingMessage();
          if (result) {
            store.userDetails.tenKhachHang = userInfo.name.trim();
            store.userDetails.soDienThoai = userInfo.phone.trim();
            store.userDetails.ngaySinh = userInfo.birthday.toDate();
            store.userDetails.diaChiList = diaChiList;
            window.centerMessage.success('Cập nhật thông tin thành công!');
            await fetchAddresses();
          } else {
            window.centerMessage.error('Cập nhật thông tin không thành công!');
          }
        } catch (error) {
          console.error('Lỗi khi cập nhật thông tin:', error);
          window.centerMessage.error('Có lỗi xảy ra khi cập nhật thông tin');
        } finally {
          isUpdating.value = false;
        }
      },
      onCancel() {
        window.centerMessage.info('Đã hủy cập nhật thông tin');
      }
    });
  } catch (error) {
    window.centerMessage.error('Có lỗi xảy ra khi chuẩn bị dữ liệu');
    console.error('Lỗi khi chuẩn bị dữ liệu cập nhật:', error);
    isUpdating.value = false;
  }
};

// Đổi mật khẩu
const changePassword = async () => {
  if (!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    return toast.error('Vui lòng nhập đầy đủ thông tin');
  }
  if (/\s/.test(passwordForm.newPassword)) {
    return toast.error('Mật khẩu mới không được chứa khoảng trắng');
  }
  if (passwordForm.newPassword.length < 6) {
    return toast.error('Mật khẩu mới phải có ít nhất 6 ký tự');
  }
  if (passwordForm.newPassword.length > 20) {
    return toast.error('Mật khẩu mới không được vượt quá 20 ký tự');
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    return toast.error('Mật khẩu xác nhận không khớp');
  }
  try {
    const response = await axiosInstance.post('api/khach-hang/change-password', {
      oldPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    });
    if (response.data.successMessage) {
      toast.success(response.data.successMessage + ' Vui lòng đăng nhập lại!');
      passwordForm.currentPassword = '';
      passwordForm.newPassword = '';
      passwordForm.confirmPassword = '';
      localStorage.clear();
      sessionStorage.clear();
      await new Promise(resolve => setTimeout(resolve, 500));
      router.push('/login-register/login');
    } else {
      toast.error(response.data.error || 'Có lỗi xảy ra khi đổi mật khẩu');
    }
  } catch (error) {
    if (error.response?.status === 403) {
      toast.error('Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại!');
      localStorage.clear();
      sessionStorage.clear();
      router.push('/login-register/login');
    } else if (error.response?.data?.error) {
      toast.error(error.response.data.error);
    } else {
      toast.error('Có lỗi xảy ra khi đổi mật khẩu');
    }
    console.error(error);
  }
};

// Xem chi tiết đơn hàng
const viewOrderDetail = async (orderId) => {
  try {
    orderDetailModal.loading = true;
    orderDetailModal.visible = true;

    const order = orders.value.find(o => o.id_hoa_don === orderId);
    if (!order) {
      window.centerMessage.error('Không tìm thấy thông tin đơn hàng');
      orderDetailModal.visible = false;
      orderDetailModal.loading = false;
      return;
    }

    orderDetailModal.maHoaDon = order.ma_hoa_don;
    orderDetailModal.data = {
      id_hoa_don: order.id_hoa_don,
      ma_hoa_don: order.ma_hoa_don,
      ngay_tao: order.ngay_tao,
      trang_thai: order.trang_thai,
      tong_tien_sau_giam: order.tong_tien_sau_giam,
      hinh_thuc_thanh_toan: order.hinh_thuc_thanh_toan || 'Tiền mặt',
      ho_ten: order.ho_ten || 'Không có thông tin',
      sdt_nguoi_nhan: order.sdt_nguoi_nhan || 'Không có thông tin',
      dia_chi: order.dia_chi || 'Không có thông tin',
      ghi_chu: order.ghi_chu || 'Không có thông tin',
      giam_gia: order.giam_gia || 0
    };
    orderDetailModal.products = [];

    // Lấy phí vận chuyển
    try {
      const shippingResponse = await axiosInstance.get(`/admin/qlhd/phi-van-chuyen/${order.id_hoa_don}`);
      if (shippingResponse.data.success) {
        orderDetailModal.data.phi_van_chuyen = shippingResponse.data.phi_van_chuyen || 0;
      } else {
        orderDetailModal.data.phi_van_chuyen = 0;
      }
    } catch (feeError) {
      console.warn('Không lấy được phí vận chuyển:', feeError);
      orderDetailModal.data.phi_van_chuyen = 0;
    }

    // Lấy danh sách sản phẩm
    try {
      const productsResponse = await axiosInstance.get(`/admin/qlhd/get-products/${order.ma_hoa_don}`);
      if (productsResponse.data && Array.isArray(productsResponse.data)) {
        orderDetailModal.products = productsResponse.data.map(item => ({
          id_san_pham: item.id_san_pham,
          id_chi_tiet_san_pham: item.id_chi_tiet_san_pham,
          ten_san_pham: item.ten_san_pham,
          hinh_anh: item.hinh_anh || '/image/logo/logo.png',
          so_luong: item.so_luong,
          gia: item.don_gia,
          mau_sac: item.ten_mau_sac || item.mau_sac || '',
          kich_thuoc: item.gia_tri || item.kich_thuoc || ''
        }));
      } else {
        window.centerMessage.warning('Đơn hàng này không có sản phẩm nào');
      }
    } catch (productsError) {
      console.error('Lỗi khi lấy danh sách sản phẩm:', productsError);
      window.centerMessage.warning('Không thể tải thông tin sản phẩm');
    }
  } catch (error) {
    console.error('Lỗi khi xử lý chi tiết đơn hàng:', error);
    window.centerMessage.error('Không thể lấy thông tin chi tiết đơn hàng');
    orderDetailModal.visible = false;
  } finally {
    orderDetailModal.loading = false;
  }
};

// 3. Thêm hàm goToProductReview
const goToProductReview = (record) => {
  if (!record.id_san_pham || record.id_san_pham <= 0) {
    console.error('ID sản phẩm không hợp lệ:', record);
    message.error('Không thể tìm thấy ID sản phẩm');
    return;
  }
  router.push(`/sanphamdetail/${record.id_san_pham}?variant=${record.id_chi_tiet_san_pham}`);
};

// Hủy đơn hàng
const cancelOrder = async (orderId) => {
  try {
    Modal.confirm({
      title: 'Xác nhận hủy đơn',
      content: 'Bạn có chắc chắn muốn hủy đơn hàng này không?',
      okText: 'Đồng ý',
      cancelText: 'Hủy',
      centered: true,
      async onOk() {
        const loadingMessage = message.loading('Đang xử lý...', 0);
        try {
          const result = await axiosInstance.put(`/admin/qlhd/huy-don/${orderId}`);
          loadingMessage();
          if (result.data.success) {
            message.success('Đơn hàng đã được hủy thành công');
            orderDetailModal.visible = false;
            await fetchOrders();
          } else {
            message.error(result.data.message || 'Không thể hủy đơn hàng');
          }
        } catch (error) {
          loadingMessage();
          console.error('Lỗi khi hủy đơn hàng:', error);
          message.error('Có lỗi xảy ra khi hủy đơn hàng');
        }
      }
    });
  } catch (error) {
    console.error('Lỗi khi xử lý hủy đơn:', error);
    message.error('Có lỗi xảy ra khi xử lý yêu cầu');
  }
};

// Xử lý địa chỉ
const showAddAddressModal = () => {
  if (addresses.value.length >= 5) {
    window.centerMessage.warning('Bạn chỉ có thể lưu tối đa 5 địa chỉ!');
    return;
  }
  Object.assign(addressModal.data, {
    id: null,
    provinceCode: '',
    province: '',
    districtCode: '',
    district: '',
    wardCode: '',
    ward: '',
    address: '',
    isDefault: false
  });
  addressModal.isEdit = false;
  addressModal.visible = true;
};

const editAddress = async (address) => {
  try {
    const loadingMsg = window.centerMessage.loading('Đang tải dữ liệu...', 0);
    addressModal.isEdit = true;
    addressModal.data.id = address.id;
    addressModal.data.address = address.address;
    addressModal.data.isDefault = address.isDefault;
    if (!provinces.value.length) {
      await loadProvinces();
    }
    const province = provinces.value.find(p => p.name === address.province);
    if (province) {
      addressModal.data.provinceCode = province.code;
      const provinceResponse = await fetch(`https://provinces.open-api.vn/api/p/${province.code}?depth=2`);
      const provinceData = await provinceResponse.json();
      districts.value = provinceData.districts || [];
      const district = districts.value.find(d => d.name === address.district);
      if (district) {
        addressModal.data.districtCode = district.code;
        const districtResponse = await fetch(`https://provinces.open-api.vn/api/d/${district.code}?depth=2`);
        const districtData = await districtResponse.json();
        wards.value = districtData.wards || [];
        const ward = wards.value.find(w => w.name === address.ward);
        if (ward) {
          addressModal.data.wardCode = ward.code;
        }
      }
    }
    loadingMsg();
    addressModal.visible = true;
  } catch (error) {
    window.centerMessage.error('Có lỗi xảy ra khi tải thông tin địa chỉ');
    console.error('Lỗi khi tải thông tin địa chỉ để sửa:', error);
  }
};

const fetchAddresses = async () => {
  if (!store.userDetails?.idKhachHang) return;
  try {
    const loadingIndicator = setTimeout(() => {
      window.centerMessage.loading('Đang tải dữ liệu địa chỉ...', 0);
    }, 300);
    const diaChiList = await khachHangService.fetchDiaChiByKhachHangId(store.userDetails.idKhachHang);
    clearTimeout(loadingIndicator);
    addresses.value = diaChiList.map((diaChi) => ({
      id: diaChi.id_dia_chi_khach_hang || diaChi.idDiaChiKhachHang,
      address: diaChi.so_nha || diaChi.soNha || '',
      ward: diaChi.xa_phuong || diaChi.xaPhuong || '',
      district: diaChi.quan_huyen || diaChi.quanHuyen || '',
      province: diaChi.tinh_thanh_pho || diaChi.tinhThanhPho || '',
      isDefault: diaChi.dia_chi_mac_dinh || diaChi.diaChiMacDinh || false
    }));
  } catch (error) {
    console.error('Lỗi khi fetchAddresses:', error);
    window.centerMessage.error('Không thể tải danh sách địa chỉ');
  }
};

const deleteAddress = async (addressId) => {
  try {
    const result = await khachHangService.deleteDiaChi(addressId);
    if (result.error) {
      window.centerMessage.error(result.message || 'Có lỗi xảy ra khi xóa địa chỉ');
      return;
    }
    window.centerMessage.success('Xóa địa chỉ thành công');
    await fetchAddresses();
  } catch (error) {
    console.error('Lỗi khi xóa địa chỉ:', error);
    window.centerMessage.error('Có lỗi xảy ra khi xóa địa chỉ');
  }
};

const saveAddress = async () => {
  try {
    const addressData = { ...addressModal.data };
    if (!addressData.address || !addressData.provinceCode || !addressData.districtCode || !addressData.wardCode) {
      return window.centerMessage.error('Vui lòng điền đầy đủ thông tin địa chỉ');
    }
    const province = provinces.value.find(p => p.code === addressData.provinceCode);
    const district = districts.value.find(d => d.code === addressData.districtCode);
    const ward = wards.value.find(w => w.code === addressData.wardCode);
    addressData.province = province ? province.name : '';
    addressData.district = district ? district.name : '';
    addressData.ward = ward ? ward.name : '';
    const diaChiData = {
      soNha: addressData.address,
      xaPhuong: addressData.ward,
      quanHuyen: addressData.district,
      tinhThanhPho: addressData.province,
      diaChiMacDinh: addressData.isDefault
    };
    const loadingMsg = window.centerMessage.loading('Đang xử lý...', 0);
    let result;
    if (addressModal.isEdit && addressModal.data.id) {
      result = await khachHangService.updateDiaChi(addressModal.data.id, diaChiData);
    } else {
      if (!addressModal.isEdit && addresses.value.length === 0) {
        addressModal.data.isDefault = true;
        diaChiData.diaChiMacDinh = true;
      }
      result = await khachHangService.addDiaChi(store.userDetails.idKhachHang, diaChiData);
    }
    loadingMsg();
    if (result.error) {
      window.centerMessage.error(result.message || 'Có lỗi xảy ra khi lưu địa chỉ');
      return;
    }
    addressModal.visible = false;
    window.centerMessage.success(addressModal.isEdit ? 'Cập nhật địa chỉ thành công' : 'Thêm địa chỉ mới thành công');
    await fetchAddresses();
  } catch (error) {
    console.error('Lỗi khi lưu địa chỉ:', error);
    window.centerMessage.error('Có lỗi xảy ra khi lưu địa chỉ');
  }
};

// Load tỉnh/thành phố
const loadProvinces = async () => {
  try {
    const response = await fetch('https://provinces.open-api.vn/api/p/');
    provinces.value = await response.json();
  } catch (error) {
    console.error('Lỗi khi tải tỉnh/thành:', error);
    window.centerMessage.error('Không thể tải danh sách tỉnh/thành phố');
  }
};

// Xử lý khi chọn tỉnh/thành phố
const handleProvinceChange = async (value) => {
  addressModal.data.districtCode = '';
  addressModal.data.wardCode = '';
  districts.value = [];
  wards.value = [];
  try {
    const response = await fetch(`https://provinces.open-api.vn/api/p/${value}?depth=2`);
    const data = await response.json();
    districts.value = data.districts;
  } catch (error) {
    console.error('Lỗi khi tải quận/huyện:', error);
    window.centerMessage.error('Không thể tải danh sách quận/huyện');
  }
};

// Xử lý khi chọn quận/huyện
const handleDistrictChange = async (value) => {
  addressModal.data.wardCode = '';
  wards.value = [];
  try {
    const response = await fetch(`https://provinces.open-api.vn/api/d/${value}?depth=2`);
    const data = await response.json();
    wards.value = data.wards;
  } catch (error) {
    console.error('Lỗi khi tải phường/xã:', error);
    window.centerMessage.error('Không thể tải danh sách phường/xã');
  }
};

// Xử lý đăng xuất
const handleLogout = () => {
  Modal.confirm({
    title: 'Xác nhận đăng xuất',
    content: 'Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?',
    okText: 'Đăng xuất',
    cancelText: 'Hủy',
    centered: true,
    onOk: () => {
      store.logoutKH();
      window.centerMessage.success('Đăng xuất thành công');
      router.push('/login-register/login');
    }
  });
};

// Tải danh sách đơn hàng
const fetchOrders = async () => {
  if (!store.userDetails?.idKhachHang) {
    console.error('Không tìm thấy idKhachHang trong store.userDetails');
    window.centerMessage.error('Vui lòng đăng nhập lại');
    router.push('/login-register/login');
    return;
  }
  try {
    const loadingMsg = window.centerMessage.loading('Đang tải danh sách đơn hàng...', 0);
    const response = await axiosInstance.get(`/admin/qlhd/khach-hang/${store.userDetails.idKhachHang}`);
    loadingMsg();
    if (response.data && Array.isArray(response.data)) {
      orders.value = response.data.map(order => ({
        ...order,
        trang_thai: order.trang_thai.trim().replace(/\s+/g, ' ')
      }));
    } else {
      orders.value = [];
      window.centerMessage.warning('Bạn chưa có đơn hàng nào');
    }
  } catch (error) {
    console.error('Lỗi khi lấy danh sách đơn hàng:', error);
    if (error.response?.status === 401 || error.response?.status === 403) {
      window.centerMessage.error('Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại!');
      store.logoutKH();
      router.push('/login-register/login');
    } else {
      window.centerMessage.error('Không thể tải danh sách đơn hàng');
    }
    orders.value = [];
  }
};

// Tải danh sách sản phẩm yêu thích
const fetchFavoriteProducts = async () => {
  try {
    if (!store.userDetails || !store.userDetails.idKhachHang) return;
    const countResponse = await favoriteService.getCustomerFavoritesCount(store.userDetails.idKhachHang);
    if (countResponse.status === 'success') {
      favoriteProducts.value = countResponse.favoritesCount;
      if (favoriteProducts.value > 0) {
        const response = await axiosInstance.get(`/api/favorites/by-customer?idKhachHang=${store.userDetails.idKhachHang}`);
        if (response.data && Array.isArray(response.data)) {
          favoritesList.value = response.data.map(item => ({
            id_chi_tiet_san_pham: item.id_chi_tiet_san_pham,
            id_san_pham: item.id_san_pham,
            ten_san_pham: item.ten_san_pham,
            ten_mau_sac: item.ten_mau_sac,
            gia_tri: item.gia_tri,
            gia_ban: item.gia_ban,
            hinh_anh: item.hinh_anh || '/image/logo/logo.png'
          }));
        } else {
          favoritesList.value = [];
        }
      }
    } else {
      favoriteProducts.value = 0;
      favoritesList.value = [];
    }
  } catch (error) {
    console.error('Lỗi khi lấy sản phẩm yêu thích:', error);
    favoriteProducts.value = 0;
    favoritesList.value = [];
    window.centerMessage.error('Không thể tải danh sách sản phẩm yêu thích');
  }
};
// Chuyển hướng đến chi tiết sản phẩm
const goToProductDetail = (product) => {
  if (!product.id_san_pham || product.id_san_pham <= 0) {
    console.error('ID sản phẩm không hợp lệ:', product);
    message.error('Không thể tìm thấy ID sản phẩm');
    return;
  }
  router.push(`/sanphamdetail/${product.id_san_pham}?variant=${product.id_chi_tiet_san_pham}`);
}

// Khởi tạo dữ liệu
onMounted(async () => {
  if (!store.isLoggedIn) {
    window.centerMessage.warning('Bạn cần đăng nhập để xem thông tin tài khoản!');
    router.push('/login-register/login');
    return;
  }
  if (store.id_roles !== 4) {
    window.centerMessage.warning('Bạn không có quyền truy cập trang này!');
    router.push([1, 2, 3].includes(store.id_roles) ? '/admin' : '/home');
    return;
  }
  await loadUserInfo();
  await loadProvinces();
  if (userInfo.name) validateName();
  if (userInfo.phone) validatePhone();
});

// Tải thông tin người dùng
const loadUserInfo = async () => {
  if (store.userDetails) {
    userInfo.name = store.userDetails.tenKhachHang || store.userInfo?.ten_dang_nhap || 'Khách hàng';
    userInfo.email = store.userDetails.email || '';
    userInfo.phone = store.userDetails.soDienThoai || '';
    userInfo.createdAt = store.userDetails.ngayTao || new Date();
    if (store.userDetails.ngaySinh) {
      userInfo.birthday = dayjs(store.userDetails.ngaySinh);
    }
    const idKhachHang = store.userDetails.idKhachHang;
    if (idKhachHang) {
      await Promise.all([
        fetchAddresses(),
        fetchOrders(),
        fetchFavoriteProducts()
      ]);
    }
  } else {
    window.centerMessage.warning('Bạn cần đăng nhập để xem thông tin tài khoản!');
    router.push('/login-register/login');
  }
};

// Theo dõi thay đổi menu
watch(selectedMenu, (newVal) => {
  if (newVal.includes('logout')) {
    handleLogout();
    selectedMenu.value = ['info'];
  }
});
</script>

<style scoped>
/* Giữ nguyên style từ file gốc */
.customer-profile {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.profile-header {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
  background: #fff;
}

.profile-content {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 20px;
}

@media (min-width: 768px) {
  .profile-content {
    flex-direction: row;
    padding-left: 30px;
  }
}

.profile-info {
  flex: 1;
  width: 100%;
}

.profile-info-header {
  margin-bottom: 10px;
}

.profile-info h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.user-email,
.user-member-since {
  margin: 5px 0;
  display: flex;
  align-items: center;
  color: #666;
}

.user-email .anticon,
.user-member-since .anticon {
  margin-right: 8px;
  color: #e57373;
}

.user-stats {
  display: flex;
  margin-top: 20px;
  padding: 15px;
  background-color: #ffebee;
  border-radius: 8px;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-item .anticon {
  font-size: 20px;
  color: #e53935;
}

.stat-item span {
  display: block;
  font-size: 18px;
  font-weight: 600;
  margin: 5px 0;
}

.stat-item p {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.stat-divider {
  width: 1px;
  background-color: #e8e8e8;
  margin: 0 15px;
}

.mt-4 {
  margin-top: 24px;
}

.mt-5 {
  margin-top: 30px;
}

.update-profile-btn {
  height: 40px !important;
  padding: 0 24px !important;
  font-size: 16px !important;
  min-width: 180px !important;
  border-radius: 4px !important;
  cursor: pointer !important;
  transition: all 0.3s ease !important;
  position: relative !important;
  overflow: hidden !important;
}

.update-profile-btn:active {
  transform: scale(0.96) !important;
  opacity: 0.9 !important;
}

:deep(.ant-btn-primary) {
  background-color: #e53935 !important;
  border-color: #e53935 !important;
}

:deep(.ant-btn-primary:hover) {
  background-color: #c62828 !important;
  border-color: #c62828 !important;
}

:deep(.ant-btn-link) {
  color: #e53935 !important;
}

:deep(.ant-btn-link:hover) {
  color: #c62828 !important;
}

:deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #e53935 !important;
  border-color: #e53935 !important;
}

:deep(.ant-radio-checked .ant-radio-inner) {
  border-color: #e53935 !important;
}

:deep(.ant-radio-inner::after) {
  background-color: #e53935 !important;
}

:deep(.ant-tag-blue) {
  color: #e53935 !important;
  background: #ffebee !important;
  border-color: #ffcdd2 !important;
}

:deep(.ant-select-focused:not(.ant-select-disabled).ant-select:not(.ant-select-customize-input) .ant-select-selector) {
  border-color: #e53935 !important;
  box-shadow: 0 0 0 2px rgba(229, 57, 53, 0.2) !important;
}

:deep(.ant-select-item-option-selected:not(.ant-select-item-option-disabled)) {
  background-color: #ffebee !important;
  color: #e53935 !important;
}

:deep(.ant-input:focus),
:deep(.ant-input-focused) {
  border-color: #e53935 !important;
  box-shadow: 0 0 0 2px rgba(229, 57, 53, 0.2) !important;
}

:deep(.ant-menu-item-selected) {
  color: #e53935 !important;
  background-color: #ffebee !important;
}

:deep(.ant-menu-item:hover) {
  color: #e53935 !important;
}

:deep(.ant-menu-item-selected::after) {
  border-right-color: #e53935 !important;
}

:deep(.ant-tabs-ink-bar) {
  background-color: #e53935 !important;
}

:deep(.ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #e53935 !important;
}

:deep(.ant-tabs-tab:hover) {
  color: #e53935 !important;
}

:deep(.ant-pagination-item-active) {
  border-color: #e53935 !important;
}

:deep(.ant-pagination-item-active a) {
  color: #e53935 !important;
}

/* Styles for order detail modal */
.order-detail-modal :deep(.ant-modal) {
  max-width: 800px !important;
  width: 100% !important;
}

.order-detail-modal :deep(.ant-modal-content) {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 16px;
}

.order-detail-modal :deep(.ant-modal-title) {
  color: #e53935;
  font-size: 18px;
  font-weight: 600;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-content {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.modal-content::-webkit-scrollbar {
  width: 6px;
}

.modal-content::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 3px;
}

.modal-content::-webkit-scrollbar-thumb {
  background: #e53935;
  border-radius: 3px;
}

.section-container {
  margin-bottom: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #e53935;
  margin-bottom: 8px;
  padding-bottom: 4px;
  border-bottom: 1px solid #f5f5f5;
}

:deep(.ant-descriptions-item-label) {
  background-color: #fafafa;
  font-size: 12px;
  padding: 6px 12px;
  width: 150px;
}

:deep(.ant-descriptions-item-content) {
  font-size: 12px;
  padding: 6px 12px;
}

:deep(.ant-descriptions-bordered .ant-descriptions-item) {
  border-bottom: 1px solid #f0f0f0;
}

.truncate {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 300px;
}

.compact-table :deep(.ant-table) {
  font-size: 12px;
}

.compact-table :deep(.ant-table-thead > tr > th) {
  background-color: #ffebee;
  color: #333;
  font-weight: 500;
  padding: 8px;
}

.compact-table :deep(.ant-table-tbody > tr > td) {
  padding: 8px;
}

.product-image {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: 12px;
  font-weight: 500;
  color: #333;
}

.product-attrs {
  font-size: 10px;
  color: #888;
}

.product-attrs span {
  margin-right: 8px;
}

.payment-summary {
  background-color: #fff;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-bottom: 6px;
}

.total-row {
  font-weight: 600;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.total-value {
  color: #e53935;
  font-size: 14px;
}

.discount-value {
  color: #52c41a;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.btn-close,
.btn-cancel,
.btn-reorder {
  height: 32px;
  padding: 0 16px;
  font-size: 12px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.btn-close {
  background-color: #f5f5f5;
  border-color: #d9d9d9;
  color: #333;
}

.btn-close:hover {
  background-color: #e0e0e0;
  border-color: #bfbfbf;
}

.btn-cancel {
  background-color: #fff;
  border-color: #e53935;
  color: #e53935;
}

.btn-cancel:hover {
  background-color: #e53935;
  color: #fff;
}

.btn-reorder {
  background-color: #e53935;
  border-color: #e53935;
  color: #fff;
}

.btn-reorder:hover {
  background-color: #c62828;
  border-color: #c62828;
}

@media (max-width: 576px) {
  .order-detail-modal :deep(.ant-modal) {
    width: 90% !important;
    margin: 16px auto;
  }

  .modal-content {
    max-height: 70vh;
  }

  :deep(.ant-descriptions-item-label) {
    width: 120px;
  }

  .truncate {
    max-width: 200px;
  }

  .product-image {
    width: 32px;
    height: 32px;
  }

  .modal-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .btn-close,
  .btn-cancel,
  .btn-reorder {
    width: 100%;
  }
}

.favorite-products-dropdown {
  width: 300px !important;
}

.favorites-dropdown-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.favorites-header {
  padding: 10px 16px;
  background-color: #fff;
  color: #333;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.favorites-count {
  background-color: #fff;
  color: #e53935;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: bold;
  margin-left: auto;
}

.favorites-dropdown-menu {
  padding: 0 !important;
  max-height: 300px;
  overflow-y: auto;
  border: none !important;
  box-shadow: none !important;
}

.favorites-loading {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px;
  justify-content: center;
}

.favorite-product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.favorite-product-item:hover {
  background-color: #f9f9f9;
}

.favorite-product-image {
  flex-shrink: 0;
}

.favorite-product-image img {
  width: 45px;
  height: 45px;
  object-fit: cover;
  border-radius: 4px;
}

.favorite-product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-details {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.product-price {
  font-size: 14px;
  font-weight: 600;
  color: #e53935;
}
</style>
