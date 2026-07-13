# Hướng dẫn chi tiết dự án AI Nutrition Planner (Guideline)

Tài liệu này cung cấp hướng dẫn từng bước để cài đặt, thiết lập cấu hình và chạy dự án một cách trơn tru nhất. Dành cho các thành viên trong nhóm, nhà phát triển tiếp nối và giáo viên chấm điểm bộ môn Lập trình Di động.

---

## 1. Yêu cầu hệ thống (Prerequisites)
Để mở, biên dịch ứng dụng và tránh lỗi tương thích, bạn cần chuẩn bị:
- **Android Studio:** Phiên bản Iguana (2023.2.1), Jellyfish (2023.3.1) hoặc các bản mới nhất.
- **Java Development Kit (JDK):** JDK 17 (Nên chọn cấu hình JDK tích hợp sẵn trong Android Studio).
- **Android SDK:** Tối thiểu SDK 24 (Android 7.0), Target SDK 34 (Android 14).
- **Thiết bị thử nghiệm:** Khuyến khích sử dụng **điện thoại thật** chạy Android 7.0 trở lên để tính năng chụp ảnh (Camera) và phân tích AI (TensorFlow Lite) đạt hiệu năng thực tế. Có thể dùng Máy ảo (Emulator) nhưng cần cấu hình webcam ảo cho Emulator.

---

## 2. Các bước cài đặt (Setup Instructions)

### Bước 1: Mở dự án trong Android Studio
1. Mở **Android Studio**.
2. Chọn **Open** và trỏ đến thư mục `Group04_AINutritionPlanner`.
3. Đợi tiến trình **Gradle Sync** hoàn tất (Quá trình này có thể mất vài phút để tải các thư viện Room, Firebase, TensorFlow Lite, Navigation, v.v.).

### Bước 2: Cấu hình Firebase (Bắt buộc)
Do ứng dụng sử dụng cơ sở dữ liệu thời gian thực và quản lý tài khoản qua Firebase, bạn cần kết nối ứng dụng với dự án Firebase của nhóm:
1. Đăng nhập vào [Firebase Console](https://console.firebase.google.com/).
2. Đảm bảo đã bật các dịch vụ: **Authentication** (Email/Password), **Firestore Database**, và **Storage**.
3. Tải file `google-services.json` từ phần Project Settings trên Firebase.
4. Chép (copy) file `google-services.json` và dán vào thư mục `app/` của dự án trong Android Studio (Ghi đè nếu đã có).

### Bước 3: Kiểm tra AI Model (TensorFlow Lite)
Đảm bảo các tệp AI (Model) đã nằm đúng vị trí trong thư mục `app/src/main/assets/`. Nếu thiếu, tính năng nhận diện món ăn sẽ văng lỗi (Crash):
- `food_model.tflite`: Mô hình đã được huấn luyện.
- `labels.txt`: Danh sách tên các nhãn món ăn.
- `nutrition_data.json`: Bộ dữ liệu ánh xạ lượng Calories/Macro tương ứng với từng món ăn.

---

## 3. Hướng dẫn chạy & Kịch bản Demo (Run & Demo Cases)

### 3.1. Chạy ứng dụng
1. Kết nối điện thoại Android qua cáp USB (đã bật Developer Options & USB Debugging).
2. Nhấn nút **Run 'app'** (Biểu tượng tam giác màu xanh) trên Android Studio.
3. Cấp các quyền cần thiết khi ứng dụng hỏi lúc vừa mở lên: Quyền Camera (Để chụp đồ ăn), Quyền Gửi Thông báo (Để nhận nhắc nhở).

### 3.2. Kịch bản Demo môn Mobile Programming (Demo Script)
Để show được toàn bộ cấu trúc kiến trúc (đủ 4 components) cho giáo viên chấm bài, hãy thực hiện theo trình tự:

1. **Minh họa Activity & Fragment (Giao diện chính):** 
   - Mở app, đăng ký một tài khoản mới và đăng nhập.
   - Thao tác chuyển đổi mượt mà giữa các tab Nhật ký / Kế hoạch / Thống kê để thấy tính ứng dụng của Fragment.
2. **Minh họa Service (AI & Sync background):**
   - Tại trang chủ, bấm **Thêm bữa ăn** -> Chụp một bức ảnh (ví dụ: màn hình máy tính hiển thị ảnh một dĩa Phở bò).
   - Show cho giáo viên xem quá trình loading phân tích (Lúc này `FoodAnalysisService` đang chạy dưới dạng Foreground Service).
   - Tắt WiFi (Offline mode) -> Thêm một bữa ăn thủ công (lưu vào Room db).
   - Bật WiFi lại, chờ 1 lúc để thấy thông báo hoặc kiểm tra trên giao diện việc dữ liệu vừa được đẩy lên Cloud (Nhờ `FirebaseSyncService` kết hợp với `NetworkChangeReceiver`).
3. **Minh họa BroadcastReceiver & AlarmManager:**
   - Vào mục **Cài đặt** -> **Nhắc nhở bữa ăn**, đặt lịch nhắc nhở vào thời gian 1 phút sau thời điểm hiện tại.
   - Bấm nút Home để đưa ứng dụng chạy ngầm. Đúng giờ, điện thoại sẽ rung và đẩy Notification nhắc nhở. (Nhờ `MealReminderReceiver`).
4. **Minh họa ContentProvider:**
   - Trình bày code đoạn `MealLogContentProvider` cung cấp dữ liệu qua lại với View như thế nào, chứng tỏ dự án đã tích hợp chuẩn chỉnh.

---

## 4. Cấu trúc Database (Dữ liệu)
Ứng dụng áp dụng nguyên tắc "Local-first" (Lưu máy trước, đồng bộ mạng sau):
- **Room Database (Local):** `AppDatabase` chứa các bảng quản lý nội bộ như `UserEntity`, `GoalEntity`, `MealLogEntity` (bữa ăn), `PendingSyncEntity` (các bản ghi bị hoãn đồng bộ do mất mạng).
- **Firestore (Cloud):** Dữ liệu được đồng bộ lên thành các collections: `users`, `goals`, `meal_logs`, `meal_plans`.

---

## 5. Quy tắc Đóng góp (Developer Notes / Contribution)
- **Quy tắc Code:** Luôn giữ kiến trúc MVC. Tuyệt đối không code logic xử lý phức tạp (tính toán, xử lý ảnh, định dạng ngày tháng) trực tiếp trong Activity. Hãy chuyển chúng vào các lớp trong thư mục `helper` hoặc `controller`.
- **Cấp quyền (Permissions):** 
  - Android 12 (API 31+): Yêu cầu quyền `SCHEDULE_EXACT_ALARM`.
  - Android 13 (API 33+): Yêu cầu quyền `POST_NOTIFICATIONS`.
  - Bộ code trong `AlarmHelper` và `SettingsActivity` đã xử lý việc xin các quyền này theo dạng runtime. Khi sửa đổi, cẩn thận không xóa các hàm xin quyền này.
