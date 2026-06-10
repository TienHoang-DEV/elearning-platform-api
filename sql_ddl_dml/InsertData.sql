USE ElearningPlatform

-- =========================
-- ROLES SAMPLE DATA
-- =========================

-- Role đã chèn trước ở bảng Elearning nên sẽ không cần tạo lại

-- =========================
-- USERS SAMPLE DATA
-- Password cho tất cả user:
-- $2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy
-- =========================

INSERT INTO users (
    first_name,
    last_name,
    email,
    phone,
    bio,
    password_hash,
    avatar_url,
    google_id,
    status
)
VALUES
-- ADMIN
(N'Nguyễn', N'An', 'admin1@elearning.com', '0901000001',
 NULL,
 '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy',
 'avatar.jpg',
 NULL,
 'ACTIVE'),

(N'Trần', N'Bình', 'admin2@elearning.com', '0901000002',
 NULL,
 '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy',
 'avatar.jpg',
 NULL,
 'ACTIVE'),

-- MANAGER
(N'Lê', N'Cường', 'manager1@elearning.com', '0902000001',
 NULL,
 '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy',
 'avatar.jpg',
 NULL,
 'ACTIVE'),

(N'Phạm', N'Dung', 'manager2@elearning.com', '0902000002',
 NULL,
 '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy',
 'avatar.jpg',
 NULL,
 'ACTIVE'),

-- INSTRUCTOR
(N'Hoàng', N'Giang', 'instructor1@elearning.com', '0903000001',
 N'Java Backend Instructor với hơn 5 năm kinh nghiệm.',
 '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy',
 'avatar.jpg',
 NULL,
 'ACTIVE'),

(N'Vũ', N'Hải', 'instructor2@elearning.com', '0903000002',
 N'Spring Boot và SQL Server Instructor.',
 '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy',
 'avatar.jpg',
 NULL,
 'ACTIVE'),

-- LEARNER
(N'Đỗ', N'Minh', 'learner1@elearning.com', '0904000001',
 NULL,
 '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy',
 'avatar.jpg',
 NULL,
 'ACTIVE'),

(N'Bùi', N'Ngọc', 'learner2@elearning.com', '0904000002',
 NULL,
 '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy',
 'avatar.jpg',
 NULL,
 'ACTIVE');

 -- =========================
-- USER ROLES SAMPLE DATA
-- =========================

INSERT INTO user_roles (user_id, role_id)
VALUES
-- Admin
(1, 1),
(2, 1),

-- Manager
(3, 2),
(4, 2),

-- Instructor
(5, 3),
(6, 3),

-- Learner
(7, 4),
(8, 4);

-- =========================
-- CATEGORY
-- =========================
-- Đăng ký các danh mục cha trước (parent_id = NULL)
INSERT INTO categories (name, description, parent_id, status)
VALUES (N'Lập trình Front-End', N'Các khóa học lập trình Front-End', NULL, 'ACTIVE'); -- ID = 1
INSERT INTO categories (name, description, parent_id, status)
VALUES (N'Lập trình Back-End', N'Các khóa học lập trình Back-End', NULL, 'ACTIVE'); -- ID = 2
INSERT INTO categories (name, description, parent_id, status)
VALUES (N'Lập trình iOS', N'Các khóa học lập trình iOS', NULL, 'ACTIVE');
-- ID = 3

-- Đăng ký các danh mục con (parent_id trỏ về ID cha tương ứng)
INSERT INTO categories (name, description, parent_id, status)
VALUES (N'HTML', N'HTML5 cơ bản và nâng cao', 1, 'ACTIVE'); -- ID = 4
INSERT INTO categories (name, description, parent_id, status)
VALUES (N'CSS', N'CSS3, Flexbox, Grid, Responsive', 1, 'ACTIVE'); -- ID = 5
INSERT INTO categories (name, description, parent_id, status)
VALUES (N'React', N'ReactJS Component, Hooks, Redux', 1, 'ACTIVE'); -- ID = 6
INSERT INTO categories (name, description, parent_id, status)
VALUES (N'Node.js', N'Backend với Express, Node.js', 2, 'ACTIVE'); -- ID = 7
INSERT INTO categories (name, description, parent_id, status)
VALUES (N'Python', N'Lập trình Python từ cơ bản đến nâng cao', 2, 'ACTIVE'); -- ID = 8
INSERT INTO categories (name, description, parent_id, status)
VALUES (N'Java', N'Lập trình Java core và nâng cao', 2, 'ACTIVE'); -- ID = 9
INSERT INTO categories (name, description, parent_id, status)
VALUES (N'Swift', N'Ngôn ngữ lập trình Swift', 3, 'ACTIVE'); -- ID = 10
INSERT INTO categories (name, description, parent_id, status)
VALUES (N'SwiftUI', N'Thiết kế giao diện SwiftUI', 3, 'ACTIVE');

-- =========================
-- COURSES SAMPLE DATA
-- =========================

INSERT INTO courses (
    instructor_id,
    category_id,
    title,
    description,
    thumbnail_url,
    price,
    level,
    status,
    approved_by,
    approved_at
)
VALUES

-- Instructor 1
(
    5,
    9,
    N'Java Core Từ Cơ Bản Đến Nâng Cao',
    N'Học Java từ cú pháp cơ bản, OOP, Collections đến xử lý ngoại lệ.',
    'fptthumbnail.jpg',
    499000,
    'BEGINNER',
    'PUBLISHED',
    3,
    GETDATE()
),

(
    5,
    6,
    N'ReactJS Thực Chiến',
    N'Xây dựng ứng dụng ReactJS sử dụng Hooks, Router và Redux Toolkit.',
    'fptthumbnail.jpg',
    699000,
    'INTERMEDIATE',
    'PUBLISHED',
    3,
    GETDATE()
),

(
    5,
    5,
    N'CSS Mastery',
    N'Flexbox, Grid, Animation và Responsive Design chuyên sâu.',
    'fptthumbnail.jpg',
    299000,
    'BEGINNER',
    'PUBLISHED',
    3,
    GETDATE()
),

-- Instructor 2
(
    6,
    7,
    N'Node.js REST API Với Express',
    N'Thiết kế và xây dựng RESTful API bằng Node.js và Express.',
    'fptthumbnail.jpg',
    599000,
    'INTERMEDIATE',
    'PUBLISHED',
    4,
    GETDATE()
),

(
    6,
    8,
    N'Python Cho Người Mới Bắt Đầu',
    N'Học Python từ cơ bản đến lập trình hướng đối tượng.',
    'fptthumbnail.jpg',
    399000,
    'BEGINNER',
    'PUBLISHED',
    4,
    GETDATE()
),

(
    6,
    10,
    N'Swift Programming Fundamentals',
    N'Lập trình ứng dụng iOS bằng ngôn ngữ Swift.',
    'fptthumbnail.jpg',
    799000,
    'BEGINNER',
    'PUBLISHED',
    4,
    GETDATE()
),

(
    6,
    11,
    N'Xây Dựng Giao Diện Với SwiftUI',
    N'Thiết kế giao diện hiện đại trên iOS bằng SwiftUI.',
    'fptthumbnail.jpg',
    899000,
    'INTERMEDIATE',
    'PUBLISHED',
    4,
    GETDATE()
),

(
    6,
    9,
    N'Spring Boot REST API',
    N'Xây dựng hệ thống Backend với Spring Boot, JPA và Security.',
    'fptthumbnail.jpg',
    999000,
    'ADVANCED',
    'PUBLISHED',
    4,
    GETDATE()
);

-- =========================
-- COURSE SECTIONS SAMPLE DATA
-- =========================

-- Course 1: Java Core Từ Cơ Bản Đến Nâng Cao
INSERT INTO course_sections (course_id, title, position)
VALUES
(1, N'Chương 1: Giới thiệu Java', 1),
(1, N'Chương 2: Lập trình hướng đối tượng', 2),
(1, N'Chương 3: Collections và Exception', 3);

-- Course 2: ReactJS Thực Chiến
INSERT INTO course_sections (course_id, title, position)
VALUES
(2, N'Chương 1: React Fundamentals', 1),
(2, N'Chương 2: React Hooks', 2),
(2, N'Chương 3: Redux Toolkit', 3);

-- Course 3: CSS Mastery
INSERT INTO course_sections (course_id, title, position)
VALUES
(3, N'Chương 1: CSS Cơ Bản', 1),
(3, N'Chương 2: Flexbox và Grid', 2),
(3, N'Chương 3: Responsive Design', 3);

-- Course 4: Node.js REST API Với Express
INSERT INTO course_sections (course_id, title, position)
VALUES
(4, N'Chương 1: Node.js Cơ Bản', 1),
(4, N'Chương 2: Express Framework', 2),
(4, N'Chương 3: REST API Thực Chiến', 3);

-- Course 5: Python Cho Người Mới Bắt Đầu
INSERT INTO course_sections (course_id, title, position)
VALUES
(5, N'Chương 1: Python Fundamentals', 1),
(5, N'Chương 2: Hàm và Module', 2),
(5, N'Chương 3: OOP Với Python', 3);

-- Course 6: Swift Programming Fundamentals
INSERT INTO course_sections (course_id, title, position)
VALUES
(6, N'Chương 1: Swift Basics', 1),
(6, N'Chương 2: Control Flow', 2),
(6, N'Chương 3: Object-Oriented Programming', 3);

-- Course 7: Xây Dựng Giao Diện Với SwiftUI
INSERT INTO course_sections (course_id, title, position)
VALUES
(7, N'Chương 1: SwiftUI Introduction', 1),
(7, N'Chương 2: Layout và Navigation', 2),
(7, N'Chương 3: State Management', 3);

-- Course 8: Spring Boot REST API
INSERT INTO course_sections (course_id, title, position)
VALUES
(8, N'Chương 1: Spring Boot Fundamentals', 1),
(8, N'Chương 2: Spring Data JPA', 2),
(8, N'Chương 3: Spring Security', 3);

-- =========================
-- LESSONS SAMPLE DATA
-- Mỗi section có 3 bài học
-- =========================

DECLARE @sectionId INT = 1;

WHILE @sectionId <= 24
BEGIN

    INSERT INTO lessons (
        section_id,
        title,
        video_url,
        duration_seconds,
        position,
        is_published,
        moderation_status
    )
    VALUES
    (
        @sectionId,
        N'Bài 1',
        'Recording 2026-05-28 212131.mp4',
        600,
        1,
        1,
        'APPROVED'
    ),

    (
        @sectionId,
        N'Bài 2',
        'Lập trình C - 03. Cách xuất dữ liệu ra màn hình lập trình C - Hàm printf - Tự học lập trình C.mp4',
        720,
        2,
        1,
        'APPROVED'
    ),

    (
        @sectionId,
        N'Bài 3',
        'Lập trình C - 04. Cách nhập dữ liệu từ bàn phím trong lập trình C - Tự học lập trình C.mp4',
        840,
        3,
        1,
        'APPROVED'
    );

    SET @sectionId = @sectionId + 1;
END

-- =========================
-- QUIZZES SAMPLE DATA
-- 1 quiz cho mỗi lesson
-- =========================

DECLARE @lessonId INT = 1;

WHILE @lessonId <= 72
BEGIN

    INSERT INTO quizzes (
        lesson_id,
        title,
        pass_score_percent
    )
    VALUES (
        @lessonId,
        N'Quiz bài học ' + CAST(@lessonId AS NVARCHAR(10)),
        70
    );

    SET @lessonId = @lessonId + 1;
END

-- =========================
-- QUIZ 1 (Java)
-- =========================

INSERT INTO quiz_questions
(quiz_id, question_text, question_type, points, position)
VALUES
(1, N'Java là ngôn ngữ lập trình thuộc loại nào?', 'SINGLE', 1, 1),

(1, N'Từ khóa nào được sử dụng để kế thừa trong Java?', 'SINGLE', 1, 2),

(1, N'Những kiểu dữ liệu nguyên thủy nào tồn tại trong Java?', 'MULTIPLE', 1, 3);

-- =========================
-- QUIZ 2 (Java)
-- =========================

INSERT INTO quiz_questions
(quiz_id, question_text, question_type, points, position)
VALUES
(2, N'OOP là viết tắt của cụm từ nào?', 'SINGLE', 1, 1),

(2, N'Đặc tính nào KHÔNG thuộc OOP?', 'SINGLE', 1, 2),

(2, N'Những tính chất nào thuộc OOP?', 'MULTIPLE', 1, 3);

-- =========================
-- QUIZ 3 (React)
-- =========================

INSERT INTO quiz_questions
(quiz_id, question_text, question_type, points, position)
VALUES
(3, N'React được phát triển bởi công ty nào?', 'SINGLE', 1, 1),

(3, N'Hook nào dùng để quản lý state?', 'SINGLE', 1, 2),

(3, N'Những Hook nào là Hook có sẵn của React?', 'MULTIPLE', 1, 3);

-- =========================
-- QUIZ 4 (React)
-- =========================

INSERT INTO quiz_questions
(quiz_id, question_text, question_type, points, position)
VALUES
(4, N'JSX là gì?', 'SINGLE', 1, 1),

(4, N'Virtual DOM dùng để làm gì?', 'SINGLE', 1, 2),

(4, N'Các lợi ích của React là gì?', 'MULTIPLE', 1, 3);

-- =========================
-- QUIZ 5 (NodeJS)
-- =========================

INSERT INTO quiz_questions
(quiz_id, question_text, question_type, points, position)
VALUES
(5, N'Node.js chạy trên engine nào?', 'SINGLE', 1, 1),

(5, N'Framework phổ biến nhất của Node.js là gì?', 'SINGLE', 1, 2),

(5, N'Node.js thường được sử dụng để làm gì?', 'MULTIPLE', 1, 3);

-- =========================
-- QUIZ 6 (Python)
-- =========================

INSERT INTO quiz_questions
(quiz_id, question_text, question_type, points, position)
VALUES
(6, N'Python là ngôn ngữ thông dịch hay biên dịch?', 'SINGLE', 1, 1),

(6, N'Hàm nào dùng để xuất dữ liệu ra màn hình?', 'SINGLE', 1, 2),

(6, N'Những kiểu dữ liệu nào tồn tại trong Python?', 'MULTIPLE', 1, 3);

-- =========================
-- QUIZ 7 (Swift)
-- =========================

INSERT INTO quiz_questions
(quiz_id, question_text, question_type, points, position)
VALUES
(7, N'Ngôn ngữ Swift được phát triển bởi công ty nào?', 'SINGLE', 1, 1),

(7, N'Swift chủ yếu dùng để phát triển nền tảng nào?', 'SINGLE', 1, 2),

(7, N'Những framework nào thuộc hệ sinh thái Apple?', 'MULTIPLE', 1, 3);

-- =========================
-- QUIZ 8 (Spring Boot)
-- =========================

INSERT INTO quiz_questions
(quiz_id, question_text, question_type, points, position)
VALUES
(8, N'Spring Boot thuộc hệ sinh thái nào?', 'SINGLE', 1, 1),

(8, N'Annotation nào dùng để đánh dấu Controller REST?', 'SINGLE', 1, 2),

(8, N'Những module nào thuộc Spring Framework?', 'MULTIPLE', 1, 3);

-- =========================
-- QUESTION 1
-- Java là ngôn ngữ lập trình thuộc loại nào?
-- =========================
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(1, N'Ngôn ngữ hướng đối tượng', 1),
(1, N'Ngôn ngữ đánh dấu', 0),
(1, N'Hệ quản trị cơ sở dữ liệu', 0),
(1, N'Hệ điều hành', 0);

-- QUESTION 2
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(2, N'extends', 1),
(2, N'implements', 0),
(2, N'inherit', 0),
(2, N'super', 0);

-- QUESTION 3
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(3, N'int', 1),
(3, N'long', 1),
(3, N'double', 1),
(3, N'String', 0);

-- QUESTION 4
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(4, N'Object Oriented Programming', 1),
(4, N'Open Object Programming', 0),
(4, N'Object Operating Process', 0),
(4, N'Online Oriented Program', 0);

-- QUESTION 5
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(5, N'Kế thừa', 0),
(5, N'Đóng gói', 0),
(5, N'Đa hình', 0),
(5, N'Biên dịch động', 1);

-- QUESTION 6
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(6, N'Đóng gói', 1),
(6, N'Kế thừa', 1),
(6, N'Đa hình', 1),
(6, N'Trừu tượng', 1);

-- QUESTION 7
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(7, N'Google', 0),
(7, N'Microsoft', 0),
(7, N'Meta', 1),
(7, N'Apple', 0);

-- QUESTION 8
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(8, N'useState', 1),
(8, N'useClass', 0),
(8, N'useComponent', 0),
(8, N'useVariable', 0);

-- QUESTION 9
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(9, N'useState', 1),
(9, N'useEffect', 1),
(9, N'useMemo', 1),
(9, N'useController', 0);

-- QUESTION 10
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(10, N'Mở rộng HTML trong JavaScript', 1),
(10, N'Một database', 0),
(10, N'Một CSS Framework', 0),
(10, N'Một API', 0);

-- QUESTION 11
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(11, N'Tăng hiệu năng render', 1),
(11, N'Tạo database', 0),
(11, N'Quản lý server', 0),
(11, N'Xử lý CSS', 0);

-- QUESTION 12
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(12, N'Tái sử dụng component', 1),
(12, N'Virtual DOM', 1),
(12, N'Hiệu năng tốt', 1),
(12, N'Không cần JavaScript', 0);

-- QUESTION 13
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(13, N'V8 Engine', 1),
(13, N'JVM', 0),
(13, N'.NET CLR', 0),
(13, N'Python VM', 0);

-- QUESTION 14
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(14, N'Express', 1),
(14, N'Spring Boot', 0),
(14, N'Django', 0),
(14, N'Laravel', 0);

-- QUESTION 15
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(15, N'Xây dựng API', 1),
(15, N'Realtime Application', 1),
(15, N'Backend Service', 1),
(15, N'Thiết kế Photoshop', 0);

-- QUESTION 16
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(16, N'Thông dịch', 1),
(16, N'Biên dịch hoàn toàn', 0),
(16, N'Hợp ngữ', 0),
(16, N'Đánh dấu', 0);

-- QUESTION 17
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(17, N'print()', 1),
(17, N'println()', 0),
(17, N'echo()', 0),
(17, N'console.log()', 0);

-- QUESTION 18
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(18, N'int', 1),
(18, N'str', 1),
(18, N'list', 1),
(18, N'boolean', 0);

-- QUESTION 19
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(19, N'Apple', 1),
(19, N'Google', 0),
(19, N'Microsoft', 0),
(19, N'Meta', 0);

-- QUESTION 20
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(20, N'iOS', 1),
(20, N'Android', 0),
(20, N'Windows', 0),
(20, N'Linux', 0);

-- QUESTION 21
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(21, N'SwiftUI', 1),
(21, N'UIKit', 1),
(21, N'Combine', 1),
(21, N'Spring MVC', 0);

-- QUESTION 22
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(22, N'Java', 1),
(22, N'Python', 0),
(22, N'PHP', 0),
(22, N'JavaScript', 0);

-- QUESTION 23
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(23, N'@RestController', 1),
(23, N'@ControllerAdvice', 0),
(23, N'@Repository', 0),
(23, N'@Service', 0);

-- QUESTION 24
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES
(24, N'Spring MVC', 1),
(24, N'Spring Security', 1),
(24, N'Spring Data JPA', 1),
(24, N'Laravel', 0);
