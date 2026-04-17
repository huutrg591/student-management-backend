# 🚀 KTX Management System - Backend API

Hệ thống quản lý Ký túc xá tập trung, xây dựng trên nền tảng **Spring Boot 3 REST API**.

## 🛠 Tech Stack
* **Language:** Java 17+
* **Framework:** Spring Boot 3.x
* **Database:** MySQL 8.x
* **ORM:** Spring Data JPA / Hibernate
* **Build Tool:** Maven

## 📋 Chức năng chính
- **Auth API:** Xử lý đăng nhập và phân quyền (Admin/Student).
- **Student API:** Quản lý CRUD hồ sơ sinh viên.
- **Room API:** Quản lý danh mục tòa nhà, phòng ở và trạng thái sức chứa.
- **Invoice API:** Lập hóa đơn và theo dõi lịch sử thanh toán điện nước.
- **CORS Configuration:** Cấu hình cho phép kết nối an toàn với Frontend độc lập.

## ⚙️ Hướng dẫn cài đặt
1. **Database:** Tạo database `ql_ky_tuc_xa` trong MySQL và chạy script SQL khởi tạo.
2. **Configuration:** Cập nhật thông tin kết nối DB tại `src/main/resources/application.properties`.
3. **Run:** Chạy file `TtcsApplication.java`. Server sẽ lắng nghe tại cổng `8080`.

## 📌 API Endpoints tiêu biểu
- `POST /api/auth/login` - Đăng nhập hệ thống.
- `GET /api/students` - Lấy danh sách toàn bộ sinh viên.
- `GET /api/students/{msv}` - Xem chi tiết 1 sinh viên.
- `PUT /api/students/{msv}` - Cập nhật thông tin.
