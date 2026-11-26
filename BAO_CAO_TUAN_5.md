# BÁO CÁO TIẾN ĐỘ ĐỒ ÁN: ỨNG DỤNG HỌC TỪ VỰNG TIẾNG ANH "VOCABMASTER"
**Môn học / Dự án:** Phát triển ứng dụng di động Android (Mô hình MVC & Offline-First)  
**Package ứng dụng:** `com.example.vocabmaster`  
**Nội dung báo cáo:** Tuần 5 – Quy trình kiểm thử và sửa lỗi (Testing & Bug Fixing Process)  

---

## PHẦN 1: LẬP KẾ HOẠCH KIỂM THỬ (TEST PLAN)

### 1. Mục tiêu kiểm thử
Đảm bảo toàn bộ các chức năng của ứng dụng `VocabMaster` hoạt động ổn định, chính xác theo đúng mô tả yêu cầu nghiệp vụ, tuân thủ kiến trúc mô hình MVC, không phát sinh lỗi truy vấn cơ sở dữ liệu trên luồng chính (Main Thread DB Access Exception), không rò rỉ bộ nhớ (Memory Leak) và đáp ứng trải nghiệm người dùng mượt mà trên đa dạng thiết bị Android.

### 2. Phạm vi kiểm thử (Testing Scope)
- **Kiểm thử chức năng (Functional Testing)**: Tất cả 8 mô-đun chức năng chính (Quản lý chủ đề, Flashcard 3D, Quiz trắc nghiệm, Thuật toán SRS, Tìm kiếm thời gian thực, Luyện phát âm STT/TTS, Đăng nhập Google & Firebase Sync, Thông báo cục bộ AlarmManager).
- **Kiểm thử dữ liệu & Biên (Data & Boundary Testing)**: Nhập chuỗi rỗng, khoảng trắng, ký tự đặc biệt, chuỗi cực ngắn (1 ký tự), danh sách dữ liệu rỗng (0 từ vựng), dữ liệu ID vượt ngưỡng, bảo vệ cờ `isCustom` và đồng bộ offline.
- **Kiểm thử môi trường & Ngoại tuyến (Environment & Offline Testing)**:
  - Máy ảo Android Studio (API 24 - API 35).
  - Thiết bị thật (Samsung Galaxy S22, Pixel 7, Máy tính bảng 10-inch).
  - Đa dạng kích thước màn hình (Màn hình nhỏ, Màn hình chuẩn 1080p, Màn hình 2K, Chế độ xoay ngang Landscape).
  - Kiểm thử khi ngắt hoàn toàn kết nối mạng Internet (Chế độ Offline-First với Room Database).
- **Kiểm thử tự động đơn vị (JVM Unit Testing)**: Chạy toàn bộ 43 unit test trong bộ mã nguồn `app/src/test/`.

### 3. Tiêu chí đạt yêu cầu (Acceptance Criteria)
- **100% JVM Unit Test PASS** (43/43 test cases thành công).
- **0% lỗi Crash ứng dụng** khi thực hiện các thao tác chuyển tab, lật thẻ, làm bài thi, tìm kiếm và đồng bộ.
- **0% lỗi `IllegalStateException`** từ Room Database trên luồng giao diện chính.
- Tỉ lệ sửa lỗi đạt **100%** đối với các lỗi mức độ Nghiêm trọng (Critical) và Nặng (Major).

---

## PHẦN 2: BẢNG TEST CASE CHI TIẾT (TEST CASES SPECIFICATION)

Dưới đây là bảng đặc tả các Test Case đại diện bao quát đủ các trường hợp nhập đúng, nhập sai, để trống và dữ liệu biên cho các phân hệ của ứng dụng:

### Bảng 1: Phân hệ Xác thực & Đăng nhập (`Auth & Sync`)
| Mã TC | Tên Test Case | Điều kiện / Dữ liệu vào | Trọng tâm kiểm thử | Kết quả Mong đợi | Trạng thái |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **TC-01** | Đăng ký Email hợp lệ | Email: `user@example.com`, Pass: `12456` | Nhập đúng | Đăng ký thành công, tự động khởi chạy đồng bộ Cloud. | **PASS** |
| **TC-02** | Đăng ký để trống Email/Pass | Email: `""`, Pass: `""` | Để trống | Hiển thị Toast thông báo *"Vui lòng nhập Email và Mật khẩu!"*. | **PASS** |
| **TC-03** | Đăng nhập sai Mật khẩu | Email: `user@example.com`, Pass: `sai_pass` | Nhập sai | Firebase trả lỗi, hiển thị Toast thông báo lỗi chi tiết. | **PASS** |
| **TC-04** | Đăng nhập Google Sign-In | Bấm *"Tiếp tục với Google"*, chọn tài khoản Google | Nhập đúng OAuth | Lấy ID Token thành công, xác thực Firebase & kéo dữ liệu Cloud. | **PASS** |
| **TC-05** | Khóa nút khi đang Sync | Bấm nút Đăng nhập hoặc Đồng bộ ngay | Kiểm thử giao diện | Nút Submit và Sync bị mờ (`setEnabled(false)`) chống spam click. | **PASS** |

### Bảng 2: Phân hệ Từ vựng & Flashcard 3D (`Vocab & Flashcard`)
| Mã TC | Tên Test Case | Điều kiện / Dữ liệu vào | Trọng tâm kiểm thử | Kết quả Mong đợi | Trạng thái |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **TC-06** | Mở Flashcard chủ đề rỗng | Chọn chủ đề không có từ vựng | Dữ liệu biên (Rỗng) | Hiển thị Toast *"Chủ đề này chưa có từ vựng nào!"*, không mở Dialog rỗng. | **PASS** |
| **TC-07** | Lật thẻ Flashcard 3D | Chạm vào mặt trước thẻ Flashcard | Nhập đúng | Thẻ xoay 180° mặt sau, tự động đánh dấu `isLearned = true`. | **PASS** |
| **TC-08** | Spam click lật thẻ nhanh | Chạm liên tục nhiều lần khi thẻ đang xoay | Thao tác nhanh | Cờ `isAnimating` chặn click trùng, animation chạy mượt không giật. | **PASS** |
| **TC-09** | Ví dụ dịch nghĩa bị null | Từ vựng có `exampleMeaning = null` | Dữ liệu biên (Null) | TextView ẩn `GONE`, tuyệt đối **không** hiển thị chuỗi `"(null)"`. | **PASS** |
| **TC-10** | Thêm từ mới & cờ `isCustom` | Nhập Word: `Algorithm`, Meaning: `Thuật toán` | Nhập đúng | Lưu từ mới thành công, gán cờ `isCustom = true` và bảo toàn khi sync Cloud. | **PASS** |

### Bảng 3: Phân hệ Trắc nghiệm Quiz & SRS (`Quiz & SRS Algorithm`)
| Mã TC | Tên Test Case | Điều kiện / Dữ liệu vào | Trọng tâm kiểm thử | Kết quả Mong đợi | Trạng thái |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **TC-11** | Sinh 4 đáp án trắc nghiệm | Chủ đề có đủ từ vựng | Nhập đúng | Sinh 1 đáp án đúng + 3 đáp án sai xáo trộn ngẫu nhiên. | **PASS** |
| **TC-12** | Kho từ vựng < 4 từ | Chủ đề chỉ có 2 từ vựng | Dữ liệu biên (Ít từ) | Ẩn 2 thẻ đáp án thừa (`View.GONE`), setClickListener null, không văng app. | **PASS** |
| **TC-13** | Chọn đáp án sai trắc nghiệm | Chọn một đáp án sai | Nhập sai | Thẻ chọn đổi màu Đỏ, tự động tô màu Xanh cho đáp án đúng chuẩn. | **PASS** |
| **TC-14** | Chống lưu trùng bài thi | Bấm nút Hoàn thành bài quiz | Thao tác liên tục | Cờ `sessionFinished` chặn lưu lặp, bài thi và điểm chỉ ghi 1 lần. | **PASS** |
| **TC-15** | Thuật toán SRS lũy tiến | Trả lời Đúng 1 lần vs Đúng 2 lần | Logic SRS | Đúng 1 lần: mốc ôn 1 ngày; Đúng 2 lần: đạt `MASTERED`, mốc ôn 3 ngày. | **PASS** |

### Bảng 4: Phân hệ Phát âm, Tìm kiếm & Hệ thống (`Speech, Search & System`)
| Mã TC | Tên Test Case | Điều kiện / Dữ liệu vào | Trọng tâm kiểm thử | Kết quả Mong đợi | Trạng thái |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **TC-16** | Đánh giá phát âm chuỗi rỗng | Nhập text nhận dạng `""` hoặc `" "` | Để trống | Trả về kết quả Chưa chính xác (`#DC2626`), không chấm nhầm Gần đúng. | **PASS** |
| **TC-17** | Đánh giá phát âm 1 ký tự | Nhập text nhận dạng `"a"` cho `"Hardware"` | Dữ liệu biên (1 char) | Trả về Chưa chính xác (`#DC2626`), yêu cầu độ dài `>= 2` cho Gần đúng. | **PASS** |
| **TC-18** | Tìm kiếm Debounce 300ms | Gõ nhanh từ khóa `comp` vào ô tìm kiếm | Gõ phím liên tục | Hoãn 300ms sau khi ngừng gõ mới truy vấn SQL `LIKE` trên luồng ngầm. | **PASS** |
| **TC-19** | Thay đổi tốc độ đọc TTS | Chọn mức tốc độ `0.75x`, `1.0x`, `1.25x` | Nhập đúng | Áp dụng tốc độ thực tế vào `TextToSpeech` và lưu `DailyProgressManager`. | **PASS** |
| **TC-20** | Khôi phục sau khi Reboot | Khởi động lại thiết bị khi đang bật nhắc học | Khởi động hệ thống | `BootCompletedReceiver` nhận `BOOT_COMPLETED`, tự đặt lại lịch báo thức. | **PASS** |

---

## PHẦN 3: THỰC HIỆN KIỂM THỬ TRÊN CÁC MÔI TRƯỜNG

### 1. Thử nghiệm trên Máy ảo & Thiết bị thật
- **Máy ảo Android Studio**: Chạy thử nghiệm trên Google Pixel 7 (API 34) và Pixel 4 (API 27).
- **Thiết bị thật**: Thử nghiệm trên Samsung Galaxy S22 (Android 14) và Xiaomi Redmi Note 11 (Android 12).
- **Kết quả quan sát**: Giao diện tự động co giãn linh hoạt theo độ phân giải màn hình, các nút bấm BottomSheetDialog không bị chèn lấn hoặc che mất nội dung.

### 2. Thử nghiệm chế độ Ngoại tuyến (Offline Mode)
- **Thao tác**: Bật chế độ Máy bay (Airplane Mode) ngắt toàn bộ Wifi và Mobile Data.
- **Kết quả**: Ứng dụng vẫn khởi chạy bình thường, đọc dữ liệu từ Room SQLite (`vocabulary_app.db`), lật thẻ Flashcard, làm bài thi Quiz, tính phần trăm tiến độ 50-50 và phát âm TTS không gặp bất kỳ rào cản nào. Khi bật lại mạng, thao tác đồng bộ Cloud tiến hành mượt mà, bảo vệ tiến độ học offline nhờ logic so sánh số lượt làm (`cloudAttempts >= localAttempts`).

### 3. Kết quả kiểm thử tự động JVM Unit Tests
Chạy lệnh kiểm thử tự động trong terminal:
```bash
./gradlew compileDebugJavaWithJavac testDebugUnitTest
```
- **Kết quả tổng hợp**: `43 passed, 0 skipped, 0 failed` (100% Test Cases vượt qua kiểm thử thành công).

---

## PHẦN 4: GHI NHẬN LỖI VÀ TÌNH TRẠNG XỬ LÝ (BUG TRACKING LOG)

Trong quá trình thực hiện kiểm thử trên các môi trường, nhóm phát triển đã phát hiện và xử lý dứt điểm các lỗi theo bảng theo dõi dưới đây:

| Mã Lỗi | Mô tả Lỗi phát hiện | Cách Tái hiện Lỗi | Mức độ | Nguyên nhân Gốc | Giải pháp Sửa lỗi & Kết quả | Tình trạng |
| :---: | :--- | :--- | :---: | :--- | :--- | :---: |
| **BUG-01** | Lỗi văng app `IllegalStateException` khi mở cơ sở dữ liệu Room. | Xóa dòng `.allowMainThreadQueries()` và truy vấn DAO trực tiếp trên UI Thread. | **Critical** | Room cấm truy vấn dữ liệu trực tiếp trên luồng giao diện chính (Main Thread). | Chuyển toàn bộ truy vấn DAO sang luồng ngầm `ExecutorService` / `LiveData`. Kiểm thử lại: **PASS**. | **ĐÃ SỬA** |
| **BUG-02** | Nhấn liên tục lật thẻ Flashcard làm giao diện bị đè đè nhiều lớp. | Bấm chạm thẻ liên tục nhiều lần trong khi hiệu ứng lật 3D đang chạy. | **Major** | Sự kiện click không bị chặn khi animation chưa kết thúc. | Thêm cờ khóa `isAnimating`, chỉ mở lại click khi `withEndAction` hoàn tất. Kiểm thử lại: **PASS**. | **ĐÃ SỬA** |
| **BUG-03** | Chấm phát âm sai: Nói ký tự rác `"a"` cho từ `"Hardware"` vẫn được "Gần đúng". | Bật micro STT và chỉ nói duy nhất 1 ký tự `"a"`. | **Major** | Thuật toán so khớp chứa chuỗi con không kiểm tra độ dài tối thiểu của chuỗi nhận dạng. | Bổ sung điều kiện độ dài `recognized.length() >= 2` và tỷ lệ tương đồng `>= 0.6`. Kiểm thử lại: **PASS**. | **ĐÃ SỬA** |
| **BUG-04** | Nhấn nút "Hoàn thành" Quiz làm ghi nhận 2 lần điểm số vào Cloud. | Bấm nút hoàn thành bài thi Quiz và đóng dialog nhanh. | **Major** | Hàm `finishSession()` bị kích hoạt 2 lần từ cả UI và Listener đóng Dialog. | Thêm cờ bảo vệ `sessionFinished = true` trong `PracticeController`. Kiểm thử lại: **PASS**. | **ĐÃ SỬA** |
| **BUG-05** | Mất cờ `isCustom` khi đồng bộ dữ liệu từ Firestore về. | Thêm từ mới, đăng xuất, đăng nhập lại trên máy khác. | **Major** | `mergeCustomVocabs` không gọi `setCustom(true)` cho từ mới tải từ Cloud. | Bổ sung gọi `customVocab.setCustom(true)` trong cả `mergeCustomVocabs` và `pushVocabState`. Kiểm thử lại: **PASS**. | **ĐÃ SỬA** |
| **BUG-06** | Rò rỉ bộ nhớ (Memory Leak) từ biến static `activeSearchDialog`. | Mở Dialog tìm kiếm từ vựng rồi xoay ngang màn hình nhiều lần. | **Moderate** | Biến static giữ tham chiếu đến Activity Context cũ đã bị hủy. | Xóa bỏ biến static, quản lý qua biến instance trong `MainActivity` và cleanup ở `onDestroy()`. Kiểm thử lại: **PASS**. | **ĐÃ SỬA** |
| **BUG-07** | Hiển thị chuỗi `"(null)"` trên thẻ từ vựng khi câu ví dụ không có dịch nghĩa. | Xem từ vựng có `exampleMeaning = null`. | **Minor** | Chuỗi nối trực tiếp `"(" + vocab.getExampleMeaning() + ")"`. | Kiểm tra null/rỗng trước khi nối chuỗi, nếu rỗng thì ẩn `TextView` tương ứng. Kiểm thử lại: **PASS**. | **ĐÃ SỬA** |
| **BUG-08** | Lỗi N+1 Query làm chậm ứng dụng khi tải tiến độ từng chủ đề. | Mở danh sách chủ đề trên trang chủ. | **Minor** | Mỗi chủ đề lại gọi 1 câu lệnh SQL đếm riêng biệt trong vòng lặp `for`. | Thêm truy vấn gom nhóm `getAllTopicProgressLiveData()` dùng `GROUP BY v.topic_id` trong `VocabDao`. Kiểm thử lại: **PASS**. | **ĐÃ SỬA** |

---

## PHẦN 5: KIỂM THỬ LẠI VÀ ĐÁNH GIÁ CHẤT LƯỢNG (REGRESSION TESTING)

### 1. Kiểm thử hồi quy (Regression Testing)
Sau khi khắc phục toàn bộ các lỗi trong bảng theo dõi, nhóm phát triển đã tiến hành chạy lại toàn bộ tập test case (TC-01 đến TC-20) và bộ unit test tự động để đảm bảo việc sửa lỗi không gây ảnh hưởng đến các chức năng hiện có:
- **Biên dịch Java**: `./gradlew compileDebugJavaWithJavac` → **SUCCESSFUL**.
- **Chạy Unit Test**: `./gradlew testDebugUnitTest` → **43/43 PASSED**.
- **Đóng gói bản Release**: `./gradlew assembleRelease` → **BUILD SUCCESSFUL** (R8/ProGuard tối ưu kích thước APK mượt mà).

### 2. Đánh giá chất lượng tổng thể
- **Độ tin cậy & An toàn đa luồng**: Cơ sở dữ liệu Room SQLite đạt chuẩn 100% thao tác bất đồng bộ ngầm, không gây giật lag luồng giao diện chính.
- **Tính toàn vẹn dữ liệu**: Thuật toán lặp lại ngắt quãng (SRS), công thức tính phần trăm tiến độ 50% Flashcard + 50% Quiz và cơ chế bảo vệ `isCustom` phản ánh chính xác năng lực học viên.
- **Trải nghiệm người dùng (UX/UI)**: Các hiệu ứng lật thẻ 3D, trượt thẻ ngang, chuyển tab fade-in, và các hộp thoại BottomSheetDialog hiển thị mượt mà, trực quan.

---
*Báo cáo kiểm thử và quy trình sửa lỗi Tuần 5 dự án VocabMaster được nghiệm thu hoàn thành với 43/43 Unit Test PASS.*
