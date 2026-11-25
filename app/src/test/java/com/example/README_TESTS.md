# Bo Unit Test VocabMaster (Pure JVM - JUnit 4)

Thu muc `vocabmaster/` nay duoc dong goi theo dung cau truc package `app/src/test/java/com/example/vocabmaster/` cua du an, chay thuan JVM (`./gradlew compileDebugJavaWithJavac testDebugUnitTest`) ma khong can cai them Robolectric hay Mockito.

## Danh sach tep kiem thu (11 tep)

### 1. Tep goc duoc giu nguyen & bo sung tu du an
- `vocabmaster/ExampleUnitTest.java`: Unit test mac dinh.
- `vocabmaster/controller/InMemorySharedPreferences.java`: Fake `SharedPreferences` trong bo nho cho pure JVM test.
- `vocabmaster/controller/DailyProgressManagerTest.java`: Kiem thu muc tieu ngay, doi ngay, TTS locale/speed, Reminder va reset `totalQuizCompleted`.
- `vocabmaster/controller/FirebaseSyncLogicTest.java`: Kiem thu doc key tu tuy chinh, huong dong bo settings theo timestamp va hop nhat du lieu Cloud.
- `vocabmaster/controller/LearningProgressLogicTest.java`: Kiem thu logic cham phat am (chuoi rong, 1 ky tu, khop chuan, gan dung).
- `vocabmaster/controller/PracticeControllerTest.java`: Kiem thu sinh dap an Quiz, xu ly `null` an toan va chong luu trung ket qua `finishSession()`.

### 2. Tep kiem thu moi cho dot toi uu Muc 3
- `vocabmaster/model/entity/VocabEntityCustomFlagTest.java`: Kiem thu cot `is_custom` (`isCustom()` / `setCustom(boolean)`) thay the magic number `id > 120`.
- `vocabmaster/model/entity/TopicProgressFormulaTest.java`: Kiem thu cong thuc tien do thong nhat **50% Flashcard + 50% Quiz MASTERED** tren `TopicProgressItem`.
- `vocabmaster/controller/SrsAlgorithmTest.java`: Kiem thu thuat toan SRS luy tien (`1 -> 3 -> 7 -> 14 -> 30` ngay), 2 lan dung moi len `MASTERED`, tra loi sai ha ve `LEARNING` + 1 gio (`3_600_000L`).
- `vocabmaster/controller/FlashcardControllerTest.java`: Kiem thu topic rong tra ve `null` (khong tao the gia `"Family"`), `nextVocab()` khong tu danh dau da hoc, chi danh dau khi lat the (`markCurrentAsLearned()`).
- `vocabmaster/controller/TopicFilterLogicTest.java`: Kiem thu `TopicController.VocabFilterType` (`ALL`, `LEARNED`, `FAVORITE`, `WRONG`, `CUSTOM`), loc truc tiep theo `topicId` va `isCustom()`.
- `vocabmaster/controller/SpeechAndReminderLogicTest.java`: Kiem thu anh xa giong `UK`/`US` cho `SpeechController` va trang thai nhac hoc cua `DailyProgressManager` phuc vu `BootCompletedReceiver`.

## Luu y doi chieu chu ky ham khi tich hop
De cac unit test chay thuan JVM (khong phu thuoc `AppDatabase` hay `Context`), hay dam bao cac ham logic thuan sau ton tai (package-private hoac `public static`) dung ten:
1. `TopicProgressItem` dat trong `com.example.vocabmaster.model.entity` co constructor `(int topicId, int totalWords, int flashcardCount, int quizCount)` va ham `getProgressPercent()`.
2. `LearningProgressController.applySrsResult(LearningProgressEntity progress, boolean isCorrect, long now)` duoc goi ben trong `recordAnswer(...)`.
3. `TopicController.filterVocabList(List<VocabEntity> allVocabs, Set<Integer> wrongVocabIds, VocabFilterType filterType, int selectedTopicId)` duoc goi ben trong `filterVocabs(...)`.
4. `SpeechController.resolveLocale(String localeCode)` duoc goi trong `createSpeechRecognitionIntent()`.
