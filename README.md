<div align="center">
  <img src="https://cdn-icons-png.flaticon.com/512/906/906343.png" width="80" height="80" alt="KTX Logo">
  <h1>🚀 KTX Management System - Backend API</h1>

  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=for-the-badge&logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java">
  <img src="https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql" alt="MySQL">
  <img src="https://img.shields.io/badge/Hibernate-ORM-yellow?style=for-the-badge&logo=hibernate" alt="Hibernate">
  <img src="https://img.shields.io/badge/Maven-Project-red?style=for-the-badge&logo=apachemaven" alt="Maven">

  <p>Hệ thống quản lý Ký túc xá tập trung, cung cấp RESTful API bảo mật và hiệu năng cao.</p>
</div>

---

## 📋 Chức năng chính
- **Auth API:** Xử lý đăng nhập và phân quyền (Admin/Student).
- **Student API:** Quản lý CRUD hồ sơ sinh viên.
- **Room API:** Quản lý danh mục tòa nhà, phòng ở và trạng thái sức chứa.
- **Invoice API:** Lập hóa đơn và theo dõi lịch sử thanh toán điện nước.
- **CORS Configuration:** Cấu hình cho phép kết nối an toàn với Frontend độc lập.

## ⚙️ Hướng dẫn cài đặt
1. **Database:** Tạo database `ql_ky_tuc_xa` trong MySQL và chạy script SQL khởi tạo.
2. **Configuration:** Cập nhật thông tin kết nối DB tại `src/main/resources/application.properties`.
3. **Run:** Chạy file `TtcsApplication.java` trên IntelliJ. Server chạy tại cổng `8080`.
