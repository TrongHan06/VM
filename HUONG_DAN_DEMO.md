# HƯỚNG DẪN KỊCH BẢN DEMO CHI TIẾT ỨNG DỤNG "VOCABMASTER"
**Tên dự án:** VocabMaster - Ứng dụng Học Từ Vựng Tiếng Anh theo thuật toán SRS & Mô hình MVC  
**Package:** `com.example.vocabmaster`  
**Môi trường thử nghiệm:** Android Studio (Emulator / Thiết bị thật Android 7.0 - 15)  

---

## I. TỔNG QUAN KỊCH BẢN DEMO

Kịch bản demo được thiết kế chi tiết gồm **8 mô-đun chức năng chính**, giúp người đánh giá / giảng viên dễ dàng theo dõi toàn bộ các tính năng cốt lõi và nâng cao của ứng dụng theo luồng trải nghiệm thực tế của học viên.

---

## II. KỊCH BẢN DEMO CHI TIẾT THEO TỪNG MÔ-ĐUN

### MÔ-ĐUN 1: MÀN HÌNH TRANG CHỦ & THỐNG KÊ DASHBOARD (TAB HOME)

#### Các bước thực hiện:
1. Mở ứng dụng **VocabMaster**. Màn hình mặc định hiển thị Tab 1 (Trang chủ / Home).
2. Quan sát **Thanh Header màu xanh đậm (`#1E3A5F`)**:
   - Tiêu đề ứng dụng `VocabMaster` và khẩu hiệu *"Học từ vựng tiếng Anh mỗi ngày"*.
   - Icon **Kính lúp** (`btnSearchAction`) mở Dialog tìm kiếm từ vựng.
   - Icon **Chuông thông báo** (`btnReminderSetting`) mở Dialog cài đặt giờ nhắc nhở học tập.
3. Quan sát **Khối Tiến độ mục tiêu ngày (Circular Progress)**:
   - Hiển thị tiến độ hoàn thành từ vựng trong ngày (Ví dụ: `0/10` từ hoặc `5/10` từ).
   - Vòng tròn phần trăm tự động chạy hiệu ứng Animation mượt mà.
   - Nút **"Sửa mục tiêu"** mở Dialog cho phép thay đổi mục tiêu ngày (5, 10, 15, 20, 30 từ/ngày).
4. Quan sát **4 Thẻ thống kê chỉ số nhanh**:
   - **Từ đã học**: Số lượng từ vựng đã được học qua Flashcard hoặc đánh dấu đã học.
   - **Bài đã làm**: Tổng số bài thi Quiz trắc nghiệm đã hoàn thành.
   - **Câu đúng**: Tổng số câu trả lời trắc nghiệm chính xác.
   - **Chính xác**: Tỉ lệ phần trăm trả lời đúng tổng quan.
5. Quan sát **3 Thẻ phím tắt tác vụ nhanh**:
   - **Ôn từ hay sai**: Hiển thị số lượng từ vựng từng làm sai (kích hoạt mở phiên ôn tập riêng).
   - **Thêm từ mới**: Hiển thị số từ vựng do người dùng tự thêm (mở Dialog thêm từ thủ công).
   - **Luyện phát âm**: Kích hoạt tính năng đánh giá phát âm bằng microphone giọng nói (Google STT).
6. Quan sát **Lưới danh sách các chủ đề từ vựng (`rvTopics`)**:
   - Hiển thị 6 chủ đề mặc định: *Gia đình*, *Trường học*, *Công việc*, *Đồ ăn*, *Du lịch*, *Công nghệ*.
   - Mỗi chủ đề có Card hình ảnh màu sắc động (Dynamic Palette), hiển thị tên tiếng Việt, tiêu đề tiếng Anh, số từ đã học/tổng số từ và thanh tiến độ % (áp dụng công thức chuẩn **50% Flashcard + 50% Quiz Mastered**).

---

### MÔ-ĐUN 2: HỌC THẺ TỪ VỰNG FLASHCARD 3D INTERACTIVE

#### Các bước thực hiện:
1. Trên trang chủ, bấm chọn một chủ đề từ vựng bất kỳ (Ví dụ: **Gia đình**).
2. Màn hình tự động chuyển sang giao diện **Chi tiết chủ đề (`TopicDetail`)** với hiệu ứng chuyển tab mượt mà:
   - Hiển thị Banner chủ đề, icon minh họa, tiêu đề phụ và tổng số từ vựng trong chủ đề.
   - Thanh tiến độ chi tiết của chủ đề.
   - Danh sách các từ vựng thuộc chủ đề (`rvDetailVocabs`).
3. Bấm vào nút màu xanh **"Học Flashcard"** (`btnDetailLearn`):
   - Ứng dụng mở BottomSheetDialog lật thẻ Flashcard 3D.
   - **Mặt trước thẻ (Card Front)**: Hiển thị từ tiếng Anh (Ví dụ: `Family`), phiên âm IPA (Ví dụ: `/ˈfæməli/`), nút phát âm giọng nói (TTS) và hướng dẫn *"Chạm vào thẻ để xem nghĩa"*.
   - Bấm vào biểu tượng Loa phát âm: Ứng dụng đọc chuẩn từ tiếng Anh theo giọng đọc đã chọn (US hoặc UK).
4. **Thao tác lật thẻ 3D**:
   - Chạm trực tiếp vào thẻ Flashcard: Thẻ xoay 180 độ theo trục Y (RotationY) mượt mà chuyển sang **Mặt sau (Card Back)**.
   - Mặt sau hiển thị: Nghĩa tiếng Việt (*Gia đình*), câu ví dụ tiếng Anh (*I love my family.*), dịch nghĩa câu ví dụ (*Tôi yêu gia đình tôi.*) và badge loại từ/cấp độ.
   - **Lưu ý nghiệp vụ**: Ngay khi lật sang mặt sau xem nghĩa, từ vựng mới chính thức được đánh dấu là **Đã học (`isLearned = true`)** và cập nhật lên hệ thống.
5. **Thao tác điều hướng thẻ**:
   - Bấm nút **"Từ tiếp theo →"**: Thẻ cũ trượt sang trái (Slide Left) và thẻ mới trượt vào.
   - Bấm nút **"← Từ trước"**: Thẻ trượt ngược lại sang phải (Slide Right).
   - **Chống spam click**: Nếu bấm liên tục khi thẻ đang xoay/trượt, hệ thống tự động chặn nhờ cờ khóa `isAnimating`, giúp animation không bị giật lag hay đè đè lớp.
   - Khi đến thẻ cuối cùng, nút chuyển đổi hiển thị chữ **"Hoàn thành"**, bấm vào sẽ tự động đóng Dialog và cập nhật lại tiến độ trang chủ.

---

### MÔ-ĐUN 3: BÀI TẬP TRẮC NGHIỆM QUIZ & TỔNG KẾT ĐIỂM SỐ

#### Các bước thực hiện:
1. Tại màn hình Chi tiết chủ đề, bấm chọn nút **"Ôn tập Trắc nghiệm"** (`btnDetailReview`) HOẶC chọn Tab 2 (Kiểm tra) bấm **"Làm bài kiểm tra"**.
2. Ứng dụng mở giao diện thi trắc nghiệm Quiz (`PracticeDialogView`):
   - Thanh tiến trình câu hỏi (Ví dụ: `Câu 1/10`).
   - Từ tiếng Anh cần kiểm tra (Ví dụ: `Teacher`).
   - 4 thẻ đáp án trắc nghiệm A, B, C, D chứa 1 đáp án đúng và 3 đáp án sai xáo trộn ngẫu nhiên.
3. **Thao tác chọn đáp án**:
   - **Chọn đáp án ĐÚNG**: Thẻ đáp án đổi sang màu **Xanh lá (`#16A34A`)**, hiển thị thông báo *"Chính xác!"*.
   - **Chọn đáp án SAI**: Thẻ được chọn đổi sang màu **Đỏ (`#DC2626`)**, hiển thị *"Chưa chính xác!"*, đồng thời hệ thống **tự động tô màu Xanh lá cho đáp án đúng chuẩn** giúp học viên nhận biết ngay lỗi sai.
4. **Khối giải thích chi tiết**:
   - Sau khi chọn đáp án, khối giải thích tự động mở rộng hiển thị câu ví dụ minh họa và dịch nghĩa tiếng Việt.
   - Nút bấm **"Câu tiếp theo →"** xuất hiện. Bấm để chuyển sang câu hỏi kế tiếp.
5. **Hoàn thành bài thi & Chống lưu trùng**:
   - Khi làm xong câu cuối cùng (Ví dụ: `10/10`), bấm **"Hoàn thành"**.
   - Dialog làm bài đóng lại, ứng dụng mở Dialog **Tổng kết điểm số (`showQuizSummaryDialog`)**: Hiển thị điểm phần trăm (Ví dụ: `90%`), số câu đúng (Ví dụ: `9/10 câu đúng`) và thông điệp động viên.
   - **Chống lưu trùng**: Nhờ cờ `sessionFinished = true`, kết quả bài thi và bộ đếm bài làm chỉ được lưu duy nhất **1 lần** vào cơ sở dữ liệu và Cloud, tuyệt đối không bị nhân bản điểm khi đóng/mở dialog.
   - Bấm nút **"Làm lại bài thi"** để ôn tập lại bộ từ vựng hiện tại với thứ tự câu hỏi và đáp án được xáo trộn mới.

---

### MÔ-ĐUN 4: ÔN TẬP THÔNG MINH SRS & ÔN TỪ HAY SAI

#### Các bước thực hiện:
1. Chuyển sang **Tab 2 (Kiểm tra / Quiz)**.
2. **Chức năng Ôn tập ngắt quãng (SRS - Spaced Repetition System)**:
   - Bấm vào thẻ **"Ôn tập ngắt quãng (SRS)"**.
   - Ứng dụng lọc các từ vựng đã đến hạn ôn tập dựa trên mốc thời gian timestamp `nextReview <= currentTime`.
   - **Thuật toán lũy tiến**:
     - Khi trả lời đúng 1 lần: Thời gian ôn tiếp theo là **1 ngày**.
     - Trả lời đúng 2 lần liên tiếp: Đạt trạng thái **MASTERED**, mốc ôn tăng lên **3 ngày**.
     - Các lần đúng tiếp theo: Mốc ôn tăng lũy tiến **7 ngày → 14 ngày → 30 ngày** (tối đa 30 ngày).
     - Nếu trả lời sai: Từ vựng bị giảm trạng thái về **LEARNING**, mốc ôn tập rút ngắn về **1 giờ** để người dùng học lại ngay.
   - Nếu chưa có từ nào đến hạn ôn, hệ thống hiển thị Toast thông báo nhẹ nhàng: *"Hiện chưa có từ vựng nào đến hạn ôn tập ngắt quãng (SRS)!"*.
3. **Chức năng Ôn từ vựng hay sai**:
   - Bấm vào thẻ **"Ôn từ hay sai"** (trên Trang chủ hoặc Tab Kiểm tra).
   - Ứng dụng tự động tập hợp tất cả các từ vựng có `wrong_count > 0` và chưa đạt `MASTERED`.
   - Mở bài thi trắc nghiệm dành riêng cho nhóm từ hay sai này giúp học viên khắc phục lỗ hổng kiến thức.

---

### MÔ-ĐUN 5: TÌM KIẾM THEO THỜI GIAN THỰC & BỘ LỌC TỪ VỰNG DẠNG CHIP

#### Các bước thực hiện:
1. **Chức năng Tìm kiếm thời gian thực (Search Dialog)**:
   - Tại bất kỳ màn hình nào trên Trang chủ, bấm vào icon **Kính lúp** trên Header.
   - BottomSheetDialog tìm kiếm từ vựng xuất hiện.
   - Nhập từ khóa tiếng Anh hoặc nghĩa tiếng Việt vào ô tìm kiếm (Ví dụ: `comp` hoặc `máy tính`).
   - **Tối ưu đa luồng & Debounce 300ms**: Hệ thống hoãn 300ms sau khi ngừng gõ mới thực thi câu lệnh SQL `LIKE` trên luồng ngầm `ExecutorService`, danh sách kết quả hiển thị ngay lập tức bên dưới mà không gây giật lag UI.
   - Bấm vào một từ vựng trong danh sách kết quả để mở xem chi tiết từ, nghe phát âm hoặc thả tim yêu thích.
2. **Chức năng Bộ lọc từ vựng đa tầng (Tab Word List)**:
   - Chọn **Tab 3 (Danh sách từ vựng / Word List)**.
   - **Bộ lọc Chip chính**: Bấm chuyển đổi giữa các Chip *Tất cả*, *Đã học*, *Ưa thích*, *Hay sai*, *Được thêm vào*. Danh sách từ vựng tự động cập nhật phản hồi mượt mà nhờ `ListAdapter` + `DiffUtil`.
   - **Bộ lọc Chip chủ đề con**: Chọn một chủ đề cụ thể trên hàng Chip con (Ví dụ: *Trường học*) để thu hẹp phạm vi từ vựng.
3. **Thao tác Yêu thích & Thêm từ vựng thủ công**:
   - Bấm vào biểu tượng **Ngôi sao** trên từng dòng từ vựng: Ngôi sao sáng lên với hiệu ứng phóng to/thu nhỏ (Scale Animation 1.3x) và trạng thái `isFavorite` được cập nhật đồng bộ.
   - Bấm nút **"+"** (Thêm từ mới): Mở Dialog cho phép nhập Từ tiếng Anh, Phiên âm, Nghĩa tiếng Việt, Câu ví dụ và chọn Chủ đề.
   - Khi lưu, từ vựng mới tự động được gán cờ `isCustom = true` (không dùng magic number `id > 120`) và đồng bộ lên Cloud Firestore.

---

### MÔ-ĐUN 6: LUYỆN PHÁT ÂM GIỌNG NÓI (STT) & CÀI ĐẶT ÂM THANH (TTS)

#### Các bước thực hiện:
1. **Chức năng Luyện phát âm qua Micro (Speech-to-Text)**:
   - Trên Trang chủ, bấm chọn thẻ **"Luyện phát âm"**.
   - Hộp thoại luyện phát âm mở ra với từ mục tiêu tiếng Anh (Ví dụ: `Computer`).
   - Bấm biểu tượng **Microphone**: Ứng dụng kích hoạt Google Speech Recognition Intent.
   - Nói từ vựng vào micro điện thoại.
   - **Đánh giá kết quả phát âm**:
     - Nói chuẩn xác 100%: Hiển thị dải màu **Xanh lá (`#16A34A`)** kèm thông báo *"Chính xác 100%!"*.
     - Nói gần đúng: Hiển thị dải màu **Cam (`#D97706`)** kèm thông báo *"Gần đúng! Phát âm chuẩn hơn nữa nhé."*.
     - Nói sai hoặc chỉ phát âm 1 ký tự rác (như `"a"`): Hiển thị dải màu **Đỏ (`#DC2626`)** kèm thông báo *"Chưa chính xác! Thử lại xem sao nhé."*.
2. **Cài đặt Giọng đọc & Tốc độ phát âm (TTS)**:
   - Chọn **Tab 4 (Thông tin & Cài đặt)** -> Bấm vào thẻ **"Cài đặt & Khác"**.
   - **Chọn giọng đọc**: Chuyển đổi giữa **Mỹ (US)** và **Anh (UK)**.
   - **Tốc độ đọc**: Chọn giữa các mức **0.75x (Chậm)**, **1.0x (Bình thường)**, **1.25x (Nhanh)**.
   - Thử nghiệm bấm nút phát âm trên Flashcard: Tốc độ đọc và chất giọng lập tức áp dụng thay đổi thực tế qua `SpeechController`.

---

### MÔ-ĐUN 7: ĐĂNG NHẬP, ĐĂNG KÝ & ĐỒNG BỘ CLOUD (FIREBASE & GOOGLE SIGN-IN)

#### Các bước thực hiện:
1. Chọn **Tab 4 (Thông tin & Cài đặt)** -> Bấm vào thẻ **"Hồ sơ cá nhân & Đồng bộ"**.
2. **Màn hình chưa đăng nhập (State Logged Out)**:
   - Hiển thị 2 Chip lựa chọn: *Đăng nhập* / *Đăng ký tài khoản*.
   - Ô nhập Email và Mật khẩu với viền màu xanh dương `#2563EB`.
   - Nút màu xanh **"Đăng nhập"** / **"Đăng ký tài khoản"**.
   - Dải phân cách `— Hoặc —` và nút **"Tiếp tục với Google"** có icon Google 4 màu.
3. **Đăng nhập bằng Google (Google Sign-In)**:
   - Bấm nút **"Tiếp tục với Google"**.
   - Giao diện chọn tài khoản Google của Android xuất hiện.
   - Chọn tài khoản Google mong muốn: Ứng dụng tự động lấy `idToken`, xác thực với Firebase Auth qua `GoogleAuthProvider.getCredential(...)` và hiển thị Toast *"Đăng nhập Google thành công!"*.
4. **Đồng bộ hóa dữ liệu 2 chiều (Cloud Firestore Sync)**:
   - Ngay sau khi đăng nhập thành công, hệ thống tự động kích hoạt `syncAllFromCloud(...)` sử dụng `Tasks.whenAllComplete(...)`.
   - Kéo và gộp an toàn các từ vựng tự thêm (`custom_vocabs`), trạng thái đã học/yêu thích (`vocab_states`), lịch sử làm bài (`quiz_results`) và cài đặt cá nhân (`settings`) từ Firestore về Room Database cục bộ.
   - **Bảo vệ dữ liệu**: Cài đặt được gộp theo mốc thời gian `updatedAt` (Last-Write-Wins), kết quả quiz chống trùng nhờ `completedAt`, không làm đè dữ liệu.
5. **Màn hình đã đăng nhập (State Logged In)**:
   - Hiển thị Card thông tin người dùng với Email đại diện và dòng chữ màu xanh lá *"Đã kết nối Cloud Firestore"*.
   - Nút **"Đồng bộ ngay (Cloud & Room)"** cho phép kích hoạt đồng bộ thủ công bất kỳ lúc nào.
   - Nút **"Đăng xuất tài khoản"**: Đăng xuất sạch cả Firebase Auth và Google Client, xóa phiên làm việc để lần sau người dùng chọn tài khoản khác.

---

### MÔ-ĐUN 8: THÔNG BÁO NHẮC HỌC CỤC BỘ & TỰ KHÔI PHỤC SAU BOOT

#### Các bước thực hiện:
1. Trên Header trang chủ, bấm vào icon **Chuông thông báo** (`btnReminderSetting`).
2. **Cấp quyền thông báo (Android 13+)**:
   - Nếu chạy trên Android 13 trở lên, ứng dụng tự động hiển thị hộp thoại xin quyền `POST_NOTIFICATIONS`. Bấm **"Cho phép"**.
3. **Cài đặt thời gian nhắc nhở**:
   - Điều chỉnh giờ và phút trên bộ chọn thời gian `TimePicker` (Ví dụ: `20:30`).
   - Bật công tắc `MaterialSwitch` sang trạng thái Kích hoạt.
   - Bấm **"Lưu cài đặt"**: Hệ thống lên lịch báo thức lặp lại hằng ngày qua `AlarmManager.setInexactRepeating` (không dùng exact alarm, đảm bảo quy định Google Play Store).
4. **Thử nghiệm gửi thông báo**:
   - Bấm nút **"Gửi thông báo thử nghiệm"**: Ứng dụng lập tức phát một Notification lên thanh trạng thái Android với tiêu đề *"Đã đến giờ học từ vựng!"* và nội dung tiến độ thực tế trong ngày.
5. **Tự động khôi phục sau khi khởi động lại máy (Boot Recovery)**:
   - Khi thiết bị Android bị khởi động lại (Reboot), `BootCompletedReceiver` lắng nghe `ACTION_BOOT_COMPLETED`, kiểm tra nếu nhắc nhở đang bật sẽ tự động đặt lại lịch báo thức mà người dùng không cần phải mở lại app.

---

## III. TỔNG KẾT BẢNG KIỂM TRA CHỨC NĂNG DEMO (CHECKLIST)

| STT | Tên Chức năng Demo | Thao tác Kiểm tra | Kết quả Mong đợi | Trạng thái |
| :---: | :--- | :--- | :--- | :---: |
| 1 | **Dashboard & Tiến độ** | Quan sát trang chủ và lưới chủ đề | Chỉ số thống kê chuẩn xác, % tiến độ hợp nhất 50-50 mượt mà | **ĐẠT** |
| 2 | **Flashcard 3D** | Chạm lật thẻ và bấm chuyển từ | Xoay 180° mượt, tự đánh dấu đã học khi xem nghĩa, không lặp animation | **ĐẠT** |
| 3 | **Bài tập Quiz** | Làm bài thi trắc nghiệm 10/20 câu | Đổi màu Đúng/Sai, hiện giải thích, chấm điểm và chống lưu trùng kết quả | **ĐẠT** |
| 4 | **Thuật toán SRS** | Làm bài thi SRS & kiểm tra mốc thời gian | Lũy tiến 1-3-7-14-30 ngày, 2 lần đúng mới MASTERED, sai lùi về 1 giờ | **ĐẠT** |
| 5 | **Tìm kiếm Real-time** | Bấm kính lúp Header & gõ từ khóa | Tìm kiếm nền qua ExecutorService, debounce 300ms, không lag UI | **ĐẠT** |
| 6 | **Bộ lọc Chip** | Bấm các Chip chính và con ở Tab 3 | Danh sách phản hồi tức thì nhờ ListAdapter + DiffUtil | **ĐẠT** |
| 7 | **Luyện phát âm STT** | Bấm micro & đọc từ tiếng Anh | Nhận diện chính xác, đổi màu Xanh/Cam/Đỏ theo kết quả | **ĐẠT** |
| 8 | **Google Sign-In & Cloud**| Đăng nhập Google & bấm Đồng bộ | Lấy ID Token thành công, sync 2 chiều phẳng qua Tasks.whenAllComplete | **ĐẠT** |
| 9 | **Thông báo Nhắc học** | Cài giờ nhắc nhở & gửi thử nghiệm | Phát Notification chuẩn xác, khôi phục tự động sau khi reboot | **ĐẠT** |
| 10 | **An toàn đa luồng DB** | Thực hiện toàn bộ các tác vụ app | Room DB chạy 100% luồng ngầm, 0% crash Main Thread DB | **ĐẠT** |

---
*Hướng dẫn kịch bản demo ứng dụng VocabMaster chuẩn hóa phục vụ kiểm thử và báo cáo đồ án.*
