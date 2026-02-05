# TheXuong - Sport Apparel E-commerce

Dự án website bán đồ thể thao, phát triển bằng Java Spring Boot và Spring Security.

## 🛠 Công nghệ sử dụng
* **Java:** JDK 21
* **Framework:** Spring Boot 3.5.9
* **Database:** SQL Server
* **Frontend:** HTML5, Bootstrap 5, Thymeleaf
* **Build Tool:** Gradle

## 🚀 Hướng dẫn chạy dự án (Getting Started)

### 1. Cấu hình Database
Mở file `application.yml` hoặc `application.properties` và chỉnh lại thông tin SQL Server của bạn:
- Database Name: `dbTheXuong` (Chạy file `dbTheXuong.sql` để tạo bảng và dữ liệu mẫu)
- Username/Password: (Của máy bạn)

### 2. Cấu hình Biến môi trường (Bắt buộc)
Dự án có sử dụng Google Login, bạn cần set 2 biến môi trường sau hoặc sửa trực tiếp trong file cấu hình (không khuyến khích):
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`


### 3. Chạy ứng dụng
Mở terminal tại thư mục gốc và chạy lệnh:
```bash
./gradlew bootRun/
```

### 4. Chỉnh sửa tuổi thọ web
- Sửa file src/main/resources/application.yml
- Sửa file src/main/java/com/example/thexuong/security/JwtService.java
- Sửa file src/main/java/com/example/thexuong/controller/AuthController.java
- Sửa file src/main/java/com/example/thexuong/security/OAuth2SuccessHandler.java
