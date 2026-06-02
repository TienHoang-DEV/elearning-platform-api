//package vn.edu.fpt.config;
//
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import org.jspecify.annotations.NonNull;
//import vn.edu.fpt.entity.*;
//import vn.edu.fpt.repository.*;
//
//import java.math.BigDecimal;
//
//@Component
//@RequiredArgsConstructor // tự động sinh constructor với tất cả các field final nên không cần gán @Autowired cho từng repository
//@Transactional
//public class DataInitializer implements CommandLineRunner {
//
//    private final RoleRepository roleRepository;
//    private final UserRepository userRepository;
//    private final CategoryRepository categoryRepository;
//    private final CourseRepository courseRepository;
//    private final CourseSectionRepository courseSectionRepository;
//    private final LessonRepository lessonRepository;
//    private final QuizRepository quizRepository;
//    private final QuizQuestionRepository quizQuestionRepository;
//    private final QuizAnswerRepository quizAnswerRepository;
//
//    @Override
//    public void run(String @NonNull... args) {
//        Role instructorRole = roleRepository.findByName("instructor").orElse(null);
//        if (instructorRole == null) {
//            instructorRole = roleRepository.save(Role.builder()
//                    .name("instructor")
//                    .description("Giảng viên")
//                    .build());
//        }
//
//        User instructor = userRepository.findByEmail("28tech@gmail.com").orElse(null);
//        if (instructor == null) {
//            instructor = userRepository.save(User.builder()
//                    .role(instructorRole)
//                    .firstName("28")
//                    .lastName("Tech")
//                    .email("28tech@gmail.com")
//                    .phone("0909999999")
//                    .passwordHash("123456")
//                    .status("active")
//                    .build());
//        }
//
//        Category category = categoryRepository.save(Category.builder()
//                .name("Lập trình")
//                .description("Các khóa học lập trình")
//                .status("active")
//                .build());
//
//        Course course = courseRepository.save(Course.builder()
//                .instructor(instructor)
//                .category(category)
//                .title("Lập Trình C Cơ Bản - 28Tech")
//                .description("Khóa học lập trình C cơ bản")
//                .thumbnailUrl("course-thumbnails/2aOboQZWp6Ov5iGTAZLOlCgmiOhOKsGgeQU1cI0O.jpg")
//                .price(BigDecimal.ZERO)
//                .level("beginner")
//                .status("published")
//                .build());
//
//        CourseSection section = courseSectionRepository.save(CourseSection.builder()
//                .course(course)
//                .title("Giới thiệu")
//                .position(1)
//                .build());
//
//        lessonRepository.save(Lesson.builder()
//                .courseSection(section)
//                .title("Bài 1 - Giới thiệu ngôn ngữ C")
//                .videoUrl("videos/Recording 2026-05-28 212131.mp4")
//                .durationSeconds(600)
//                .position(1)
//                .published(true)
//                .moderationStatus("approved")
//                .build());
//
//        Lesson lesson2 = lessonRepository.save(Lesson.builder()
//                .courseSection(section)
//                .title("Bài 2 - Kiểu dữ liệu và khai báo biến trong C")
//                .videoUrl("videos/L%E1%BA%ADp%20tr%C3%ACnh%20C%20-%2002.%20Ki%E1%BB%83u%20d%E1%BB%AF%20li%E1%BB%87u%20v%C3%A0%20c%C3%A1ch%20khai%20b%C3%A1o%20bi%E1%BA%BFn%20trong%20l%E1%BA%ADp%20tr%C3%ACnh%20C%20-%20T%E1%BB%B1%20h%E1%BB%8Dc%20l%E1%BA%ADp%20tr%C3%ACnh%20C-C++.mp4")
//                .durationSeconds(900)
//                .position(2)
//                .published(true)
//                .moderationStatus("approved")
//                .build());
//
//        Lesson lesson3 = lessonRepository.save(Lesson.builder()
//                .courseSection(section)
//                .title("Bài 3 - Xuất dữ liệu với printf")
//                .videoUrl("videos/L%E1%BA%ADp%20tr%C3%ACnh%20C%20-%2003.%20C%C3%A1ch%20xu%E1%BA%A5t%20d%E1%BB%AF%20li%E1%BB%87u%20ra%20m%C3%A0n%20h%C3%ACnh%20l%E1%BA%ADp%20tr%C3%ACnh%20C%20-%20H%C3%A0m%20printf%20-%20T%E1%BB%B1%20h%E1%BB%8Dc%20l%E1%BA%ADp%20tr%C3%ACnh%20C.mp4")
//                .durationSeconds(850)
//                .position(3)
//                .published(true)
//                .moderationStatus("approved")
//                .build());
//
//        Lesson lesson4 = lessonRepository.save(Lesson.builder()
//                .courseSection(section)
//                .title("Bài 4 - Nhập dữ liệu với scanf")
//                .videoUrl("videos/L%E1%BA%ADp%20tr%C3%ACnh%20C%20-%2004.%20C%C3%A1ch%20nh%E1%BA%ADp%20d%E1%BB%AF%20li%E1%BB%87u%20t%E1%BB%AB%20b%C3%A0n%20ph%C3%ADm%20trong%20l%E1%BA%ADp%20tr%C3%ACnh%20C%20-%20T%E1%BB%B1%20h%E1%BB%8Dc%20l%E1%BA%ADp%20tr%C3%ACnh%20C.mp4")
//                .durationSeconds(920)
//                .position(4)
//                .published(true)
//                .moderationStatus("approved")
//                .build());
//
//        seedQuizForLesson2(lesson2);
//        seedQuizForLesson3(lesson3);
//        seedQuizForLesson4(lesson4);
//
//    }
//
//
//    private void seedQuizForLesson2(Lesson lesson2) {
//        Quiz quiz = Quiz.builder()
//                .lesson(lesson2)
//                .title("Quiz - Kiểu dữ liệu và biến")
//                .passScore(70)
//                .build();
//        quiz = quizRepository.save(quiz);
//
//        QuizQuestion q1 = QuizQuestion.builder()
//                .quiz(quiz)
//                .questionText("Kiểu dữ liệu nào dùng để lưu số nguyên trong C?")
//                .questionType("single")
//                .points(1)
//                .position(1)
//                .build();
//        q1 = quizQuestionRepository.save(q1);
//        persistAnswer(q1, "int", true);
//        persistAnswer(q1, "float", false);
//        persistAnswer(q1, "double", false);
//        persistAnswer(q1, "char", false);
//
//        QuizQuestion q2 = QuizQuestion.builder()
//                .quiz(quiz)
//                .questionText("Từ khóa nào dùng để khai báo biến số thực?")
//                .questionType("single")
//                .points(1)
//                .position(2)
//                .build();
//        q2 = quizQuestionRepository.save(q2);
//        persistAnswer(q2, "float", true);
//        persistAnswer(q2, "int", false);
//        persistAnswer(q2, "char", false);
//        persistAnswer(q2, "void", false);
//    }
//
//    private void seedQuizForLesson3(Lesson lesson3) {
//        Quiz quiz = Quiz.builder()
//                .lesson(lesson3)
//                .title("Quiz - Hàm printf")
//                .passScore(70)
//                .build();
//        quiz = quizRepository.save(quiz);
//
//        QuizQuestion q1 = QuizQuestion.builder()
//                .quiz(quiz)
//                .questionText("Hàm nào dùng để xuất dữ liệu ra màn hình?")
//                .questionType("single")
//                .points(1)
//                .position(1)
//                .build();
//        q1 = quizQuestionRepository.save(q1);
//        persistAnswer(q1, "printf", true);
//        persistAnswer(q1, "scanf", false);
//        persistAnswer(q1, "gets", false);
//        persistAnswer(q1, "cin", false);
//
//        QuizQuestion q2 = QuizQuestion.builder()
//                .quiz(quiz)
//                .questionText("%d trong printf dùng để in kiểu dữ liệu nào?")
//                .questionType("single")
//                .points(1)
//                .position(2)
//                .build();
//        q2 = quizQuestionRepository.save(q2);
//        persistAnswer(q2, "Số nguyên", true);
//        persistAnswer(q2, "Số thực", false);
//        persistAnswer(q2, "Ký tự", false);
//        persistAnswer(q2, "Chuỗi", false);
//    }
//
//    private void seedQuizForLesson4(Lesson lesson4) {
//        Quiz quiz = Quiz.builder()
//                .lesson(lesson4)
//                .title("Quiz - Hàm scanf")
//                .passScore(70)
//                .build();
//        quiz = quizRepository.save(quiz);
//
//        QuizQuestion q1 = QuizQuestion.builder()
//                .quiz(quiz)
//                .questionText("Hàm nào dùng để nhập dữ liệu từ bàn phím?")
//                .questionType("single")
//                .points(1)
//                .position(1)
//                .build();
//        q1 = quizQuestionRepository.save(q1);
//        persistAnswer(q1, "scanf", true);
//        persistAnswer(q1, "printf", false);
//        persistAnswer(q1, "puts", false);
//        persistAnswer(q1, "cout", false);
//
//        QuizQuestion q2 = QuizQuestion.builder()
//                .quiz(quiz)
//                .questionText("Dấu & trong scanf có tác dụng gì?")
//                .questionType("single")
//                .points(1)
//                .position(2)
//                .build();
//        q2 = quizQuestionRepository.save(q2);
//        persistAnswer(q2, "Lấy địa chỉ biến", true);
//        persistAnswer(q2, "Kết thúc lệnh", false);
//        persistAnswer(q2, "Nối chuỗi", false);
//        persistAnswer(q2, "Xuất dữ liệu", false);
//    }
//
//    private void persistAnswer(QuizQuestion question, String text, boolean correct) {
//        QuizAnswer answer = QuizAnswer.builder()
//                .question(question)
//                .answerText(text)
//                .correct(correct)
//                .build();
//        quizAnswerRepository.save(answer);
//    }
//}
