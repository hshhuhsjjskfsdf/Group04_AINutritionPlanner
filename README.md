# AI Nutrition Planner App


## 📌 Giới thiệu dự án (Introduction)
**AI Nutrition Planner App** là một ứng dụng Android thông minh giúp người dùng theo dõi chế độ ăn uống, kiểm soát calories và quản lý dinh dưỡng cá nhân hằng ngày một cách tiện lợi. 

Điểm nổi bật của ứng dụng là khả năng **nhận diện món ăn từ hình ảnh bằng AI (TensorFlow Lite)** kết hợp với đồng bộ dữ liệu đám mây (Firebase) và lưu trữ ngoại tuyến (Room Database). Đồ án được thiết kế đáp ứng đầy đủ yêu cầu môn học Lập trình Di động (Mobile Programming) thông qua việc áp dụng cấu trúc hệ thống rõ ràng và các thành phần cốt lõi của Android.

## 🚀 Tính năng chính (Key Features)
- 🔐 **Xác thực người dùng:** Đăng nhập, Đăng ký, Quên mật khẩu qua Firebase Authentication.
- 📸 **Nhận diện món ăn (AI Recognition):** Chụp/Chọn ảnh để hệ thống tự động nhận diện món ăn bằng mô hình TensorFlow Lite ngay trên thiết bị.
- ✍️ **Nhập liệu thủ công (Manual Entry):** Tùy chọn nhập thông tin món ăn, định lượng bằng tay.
- 🎯 **Mục tiêu cá nhân:** Cài đặt lượng Calories, Protein, Carbs, Fat mục tiêu hàng ngày.
- 📊 **Thống kê & Theo dõi:**
  - **Nhật ký bữa ăn (Meal Log):** Lưu trữ lịch sử ăn uống.
  - **Tiến độ hàng ngày (Daily Progress):** Theo dõi lượng calo đã nạp/còn lại qua giao diện Card trực quan.
  - **Kế hoạch bữa ăn (Meal Plan):** Lên kế hoạch ăn uống cho các ngày sắp tới.
- ⏰ **Nhắc nhở (Reminder):** Đặt lịch nhắc giờ ăn bằng hệ thống Notification (AlarmManager).
- ☁️ **Đồng bộ hóa (Cloud Sync):** Lưu trữ cục bộ với Room Database (hỗ trợ sử dụng khi mất mạng) và tự động đồng bộ lên Firestore/Firebase Storage khi có kết nối Internet trở lại.

## 🏗️ Kiến trúc & Công nghệ (Tech Stack)
Dự án được xây dựng theo mô hình **MVC (Model - View - Controller)**.

- **Ngôn ngữ:** Java & XML Layout
- **Thành phần cốt lõi (4 Android Components):**
  1. **Activity & Fragment:** `MainActivity`, `AuthActivity`, `MealEntryActivity`, `NutritionActivity`, `SettingsActivity` cùng các Fragment quản lý tab tĩnh.
  2. **Service:** `FoodAnalysisService` (Foreground xử lý AI) và `FirebaseSyncService` (Background đồng bộ).
  3. **BroadcastReceiver:** `MealReminderReceiver` (Thông báo giờ ăn), `NetworkChangeReceiver` (Lắng nghe trạng thái mạng), `BootCompletedReceiver` (Phục hồi lịch nhắc sau khi khởi động lại).
  4. **ContentProvider:** `MealLogContentProvider` (Quản lý và cung cấp dữ liệu nhật ký bữa ăn).
- **Cơ sở dữ liệu:**
  - Local: **Room Database**.
  - Cloud: **Firebase Cloud Firestore** & **Firebase Storage**.
- **AI & Machine Learning:** **TensorFlow Lite** (Offline model).

## 📂 Cấu trúc thư mục (Project Structure)
Dự án được tổ chức chặt chẽ theo từng module nhiệm vụ:
```text
com.example.a23110035_23110060
 ┣ 📂 controller    # Chứa các Controller xử lý logic giao tiếp giữa View và Model
 ┣ 📂 data          # Cấu hình Room, DAO, Provider và Repository (kết nối Firebase)
 ┣ 📂 helper        # Các lớp tiện ích (DateHelper, AlarmHelper, TensorFlowHelper...)
 ┣ 📂 model         # Lớp đối tượng (User, MealLog, Goal...)
 ┣ 📂 receiver      # Broadcast Receivers bắt các sự kiện hệ thống
 ┣ 📂 service       # Các dịch vụ chạy nền và phân tích AI
 ┗ 📂 view          # Tầng giao diện: Activities, Fragments, Adapters
```

## 📜 Tài liệu Hướng dẫn (Guideline)
Vui lòng tham khảo file [GUIDELINE.md](GUIDELINE.md) trong thư mục gốc để xem chi tiết cách cấu hình IDE, cài đặt Firebase, chạy dự án và kịch bản demo (test cases) cho môn học.

---
*Dự án Lập trình Di động (Mobile Programming) - Phát triển bởi Nhóm 04.*
