# WARP.md

luôn giao tiếp với tôi bằng tiếng việt
This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project overview

This repository contains a full-stack DATN project for managing a clothing store (`Quản lý bán quần áo`), split into:

- **Backend**: `duanbe/` — Spring Boot (Java 17) application exposing REST APIs for products, inventory, orders, promotions, customers, payments, and reporting.
- **Frontend**: `DuAnMauFE/` — Vue 3 + Vite SPA that provides both an admin interface and a customer-facing storefront, using Pinia for state management.
- **Root documentation**: several Markdown and SQL files documenting the analysis and fixes for the in-store sales flow (`Bán hàng tại quầy`), including detailed rollout and testing steps.

Use this file as the primary entry point for how to build, run, and navigate the codebase. For deeper background on the sales-flow bugfix work, see:

- `README_SUA_LOI.md`
- `PHAN_TICH_VA_GIAI_PHAP.md`
- `TOMTAT_SUA_LOI.md`
- `CHECKLIST_KIEM_TRA.md`

These documents describe the original bugs (duplicate line items and incorrect quantity handling in "Bán hàng tại quầy"), the backend/frontend changes that fixed them, and a comprehensive manual test checklist.

---

## Common commands

### Backend (Spring Boot, Maven)

Working directory: `duanbe/`

**Install dependencies & build (including tests):**

```bash
cd duanbe
mvn clean install
```

**Run the backend locally:**

```bash
cd duanbe
mvn spring-boot:run
```

By default, the app will run on the standard Spring Boot port (typically `8080`) unless overridden in `src/main/resources/application.properties`.

**Run the backend test suite:**

```bash
cd duanbe
mvn test
```

**Run a single backend test class:**

```bash
cd duanbe
mvn -Dtest=ClassNameTest test
```

Replace `ClassNameTest` with the desired JUnit test class name.

---

### Frontend (Vue 3 + Vite)

Working directory: `DuAnMauFE/`

**Install dependencies:**

```bash
cd DuAnMauFE
npm install
```

**Run the frontend dev server (hot reload):**

```bash
cd DuAnMauFE
npm run dev
```

This uses Vite; by default the app is available at `http://localhost:5173`.

**Build frontend for production:**

```bash
cd DuAnMauFE
npm run build
```

**Lint and auto-fix frontend code:**

```bash
cd DuAnMauFE
npm run lint
```

The ESLint configuration is in `DuAnMauFE/eslint.config.js` (flat config with Vue plugin and Prettier-based formatting). A formatting helper exists as:

```bash
cd DuAnMauFE
npm run format
```

which runs Prettier over `src/`.

---

### End-to-end local run (for manual testing)

The root documentation (`README_SUA_LOI.md`, `CHECKLIST_KIEM_TRA.md`) standardizes this workflow:

1. **Start backend:**
   - `cd duanbe`
   - `mvn clean install`
   - `mvn spring-boot:run`
2. **Start frontend:**
   - `cd DuAnMauFE`
   - `npm install`
   - `npm run dev`
3. **Open the app:**
   - Navigate to the Vite dev URL in a browser (commonly `http://localhost:5173`).
4. **Run the manual tests:**
   - Follow the scenarios in `CHECKLIST_KIEM_TRA.md`, especially around "Bán hàng tại quầy".

---

## Backend architecture (duanbe/)

### High-level structure

The backend is a typical Spring Boot layered architecture:

- `com.example.duanbe.DuanbeApplication` — main application entry point.
- `config/` — cross-cutting configuration:
  - `SecurityConfig`, `PasswordEncoderConfig` — security and password encoding.
  - `MailConfig`, `EmailSenderService`, `MailService` — email sending.
  - `ZaloPayConfig`, plus other payment-related config.
  - `WebConfig` — generic web configuration (CORS, formatters, etc.).
- `entity/` — JPA entities modeling the domain: products (`SanPham`, `ChiTietSanPham`, `HinhAnhSanPham`), classifications (`DanhMuc`, `ThuongHieu`, `ChatLieu`, `MauSac`, `KichThuoc`), customers and addresses, carts, orders and order lines (`HoaDon`, `HoaDonChiTiet`, `TheoDoiDonHang`), promotions (`KhuyenMai`, `ChiTietKhuyenMai`, `Voucher`), reviews (`BinhLuan`), etc.
- `repository/` — Spring Data repositories for all major entities, including some custom query projections for pricing/promotion views (e.g., `ChiTietSanPhamRepo`, `KhuyenMaiRepository`, `VoucherRepository`).
- `request/` and `response/` — DTOs for inbound requests and outbound responses (e.g., `HoaDonRequest`, `VoucherRequest`, `ChiTietSanPhamView`, `HoaDonResponse`, `PageResponse`).
- `service/` — business logic services for each domain area, plus integrations:
  - `SanPhamService`, `ChiTietSanPhamService`, `KhuyenMaiService`, `VoucherService`, etc.
  - `HoaDonService`, `HoaDonScheduler` for order lifecycle and scheduled tasks.
  - `GhtkService` (shipping), `ZaloPayService`, `EmailService`, `ImagesService`, `VoucherService`.
  - `BCTKService` (indirectly referenced via `BCTKController`) for reporting/statistics.
- `controller/` — REST controllers exposing endpoints grouped by domain:
  - `BanHangController` — core in-store sales ("Bán hàng tại quầy") flow.
  - `HoaDonController`, `VoucherController`, `KhuyenMaiController`, `SanPhamController`, etc.
  - `BCTKController` — business reports / statistics.
  - Integration controllers: `UnifiedPaymentController`, `ZaloPayController`, `ImagesController`, etc.
- `payos/` — PayOS (payment) integration DTOs and controllers (`PaymentController`, `OrderController`).
- `ImportAndExportEx/` — Excel import/export utilities for product data and validation.
- `utils/` — shared helpers such as `HMACUtil` for signature generation.
- `resources/application.properties` — environment-specific configuration (DB connection, mail, external APIs, etc.).

### Core in-store sales flow (Bán hàng tại quầy)

The in-store sales feature is one of the most interconnected flows between frontend and backend. The key backend pieces:

- `controller/BanHangController`:
  - Exposes endpoints such as `getAllHoaDonCTT`, `createHoaDon`, `getSPHD`, `themSPHDMoi`, `setSPHD`, `xoaSPHD`, `trangThaiDonHang`, and configuration endpoints for shipping and receiving methods.
  - `themSPHDMoi` is the central endpoint for adding items to an order. It now:
    - Looks up the order and product.
    - Checks if a `HoaDonChiTiet` already exists for the `{idHoaDon, idChiTietSanPham}` pair.
    - Computes the best applicable promotional unit price using `ChiTietKhuyenMai` (minimum `giaSauGiam` or the base price).
    - Either increments the quantity and recomputes the line total, or creates a new line if none exists.
    - Decrements stock in `ChiTietSanPham` accordingly.
    - Calls `capNhatVoucher(idHD)` to recalculate order totals and vouchers.
- `repository/HoaDonChiTietRepo` and `repository/ChiTietSanPhamRepo`:
  - Provide the queries backing the above logic, including:
    - A finder for existing line items per order.
    - `getAllCTSPKM()` which calculates the effective selling price based on active promotions.

The root docs (`PHAN_TICH_VA_GIAI_PHAP.md`, `TOMTAT_SUA_LOI.md`, `CHECKLIST_KIEM_TRA.md`) directly reference these methods and queries, and should be consulted when modifying this flow. They encode both the intended behavior and regression scenarios.

### Promotions and pricing

Promotion logic is centralized at the data-access layer and enforced in the order logic:

- `ChiTietSanPhamRepo#getAllCTSPKM()` selects the **minimum** promotional price among all active promotions for a SKU, falling back to the base price when no promotion applies.
- When applying promotions, `gia_sau_giam` is precomputed and stored in `ChiTietKhuyenMai` for both percentage- and amount-based discounts.
- Order totals and voucher discounts are recalculated via helper methods like `capNhatVoucher` and `capNhatTongTienVaVoucher` in `BanHangController` / related services.

When adjusting discount behavior, update both the repository queries and the code that computes `gia_sau_giam` during promotion creation/update, and re-run the SQL sanity checks in `KIEM_TRA_DATABASE.sql`.

### Reporting and statistics (BCTK)

The reporting/analytics features are surfaced primarily through:

- `controller/BCTKController`
- `repository/BCTKRepo`
- `service/BCTKService` (referenced from the Pinia store via `bctkService`)

They provide aggregate metrics for revenue, order counts, product performance, and order-status distributions, which are consumed by admin dashboard views and charts on the frontend.

---

## Frontend architecture (DuAnMauFE/)

### High-level structure

Key directories under `DuAnMauFE/src`:

- `main.js` — Vue 3 entry point:
  - Sets up the app with `createApp(App)`, `createPinia()`, the Vue Router, Ant Design Vue, Bootstrap, ApexCharts, and global CSS.
  - Initializes global auth/session state by calling `useGbStore().restoreLoginState()` before mounting.
- `router/` — router configuration:
  - `index.js` wires together route modules for admin (`admin-router.js`), customer-facing web routes (`home-router.js`), and auth (`dangNhapDangKy.js`), plus some test routes.
- `layouts/`:
  - `admin-layout.vue`, `home.vue`, `dangNhapDangKy.vue` provide top-level shells for admin pages, storefront pages, and auth views.
- `views/`:
  - `views/admin/**` — page-level views for admin modules: products, orders, returns, customers, employees, promotions, vouchers, reporting (`BCTK`), etc.
  - `views/web/**` — storefront views: home, product detail, cart, checkout, order tracking, and payment callbacks.
  - `views/DangNhapDangKy/**` — login/registration flows for both admin and customers.
- `components/`:
  - `components/admin-components/**` — reusable admin UI fragments, including the in-store sales header (`BanHang/TheHeader-BanHang.vue`), sales tables, voucher and promotion forms, invoice views, etc.
  - `components/web-components/**` and `components/ingredient-web-components/**` — storefront visual components (featured products, categories, carts, brand blocks, etc.).
  - `components/common/EditorMoTa.vue` — rich-text editor for product descriptions.
- `services/` — API client modules built around `config/axiosConfig.js`, each mapping to a backend controller:
  - `banHangService.js`, `banHangOnlineService.js`, `sanPhamService.js`, `khuyenMaiService.js`, `voucherService.js`, `hoaDonService.js`, `khachHangService.js`, `bctkService.js`, etc.
- `stores/gbStore.js` — the primary Pinia store coordinating most frontend business logic.
- `config/fonts/` — font configuration used for PDF generation (e.g. Roboto for `jspdf`).
- `utils/shippingFeeCalculator.js` — client-side shipping fee helper (paired with `GhtkService` on the backend).
- `constants/constants.js` — shared constants.

### Core in-store sales flow (Bán hàng tại quầy) on the frontend

The in-store sales UI connects multiple layers:

- `components/admin-components/BanHang/TheHeader-BanHang.vue`:
  - Manages the tabbed UI for open orders, customer selection, search dropdown for products, and the in-cart table.
  - Handles product search/deburring (via `normalizeString` and `filteredProducts`) so searches are accent-insensitive.
  - On product selection, calls `addToBill(product)`, which:
    - Applies a debounce using `isAdding`, `lastClickTime`, and `CLICK_DELAY` to prevent double-click issues.
    - Validates that a current order tab and a positive stock quantity exist.
    - Calls the Pinia store action that ultimately uses `banHangService.themSPHDMoi` to post to the backend.
    - Refreshes the order lines and order header via `store.getAllSPHD` and `refreshHoaDon`.
- `services/banHangService.js`:
  - Wraps HTTP calls to the `BanHangController`, including `themSPHDMoi`, `setSPHD`, `getSPHD`, `trangThaiDonHang`, shipping method endpoints, and payment-related calls (e.g., MoMo, GHTK).
- `stores/gbStore.js`:
  - Holds shared state for products on sale, current invoices, discount info, statistics, and more.
  - Exposes actions like `getAllSPHD`, `getHoaDonByIdHoaDon`, `getAllCTSPKM`, and various reporting actions used by the dashboard.

When modifying the in-store sales flow, you typically need to update these three pieces together:

1. `BanHangController` (backend) behavior and responses.
2. `banHangService.js` request/response handling.
3. `TheHeader-BanHang.vue` UI and the corresponding `gbStore` actions.

The root docs capture the expected behavior and regression checklist for this flow and should be followed when making or validating changes.

### State management and reporting

The Pinia store `useGbStore` centralizes a significant portion of the application state:

- Product catalog and filters (`getAllSanPham`, `searchFilterParams`, `filteredProductsData`).
- Admin entities (employees, customers, vouchers, promotions) with pagination.
- Order-related state for both in-store and online orders (`getAllHoaDonArr`, `getAllHoaDonCTTArr`, `chiTietHoaDons`, `currentHoaDonId`).
- Customer shopping cart and addresses for the online flow (`gioHang`, `danhSachDiaChi`).
- Dashboard statistics and charts (`thongKe`, `bctkFilter`, `topSanPhamBanChay`, `topSanPhamSapHetHang`, `tiLeTrangThai`).

On the UI side, admin reporting views under `views/admin/BCTK` consume these store fields and actions, which in turn delegate to `bctkService` and the backend `BCTKController`.

---

## Domain-specific documentation

The following root-level documents are important for understanding the current behavior and requirements:

- `PHAN_TICH_VA_GIAI_PHAP.md` — deep analysis of the historical bugs in the in-store sales flow, including backend and frontend root-cause analysis and SQL validation queries.
- `TOMTAT_SUA_LOI.md` — high-level summary of the changes made in backend (`BanHangController`) and frontend (`TheHeader-BanHang.vue`, `banHangService.js`, `gbStore.js`).
- `CHECKLIST_KIEM_TRA.md` — detailed manual QA checklist for end-to-end flows (build, deploy, database validation, UI scenarios for adding/removing items, promotions, stock constraints, multi-tab orders, etc.).
- `README_SUA_LOI.md` — overall guide for how the bugfix was applied and verified, including recommended commands to rebuild backend and frontend.

When changing anything related to order handling, promotions, or the in-store sales experience, review and, if necessary, update these documents alongside the code to keep the implementation and operational playbook in sync.
