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
    1000,
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
    1000,
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
    5000,
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
    10000,
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
    2000,
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
    1000,
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
    1000,
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
    1000,
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
        N'Lập trình C - 03. Cách xuất dữ liệu ra màn hình lập trình C - Hàm printf - Tự học lập trình C.mp4',
        720,
        2,
        1,
        'APPROVED'
    ),

    (
        @sectionId,
        N'Bài 3',
        N'Lập trình C - 04. Cách nhập dữ liệu từ bàn phím trong lập trình C - Tự học lập trình C.mp4',
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


-- =========================================================================
-- INSTRUCTOR REQUESTS & DASHBOARD TEST DATA (FOR PAGINATION & CHARTS TESTING)
-- =========================================================================

-- 1. KHAI BÁO CÁC BIẾN ĐỂ LƯU ID CỦA USER
DECLARE @UserId1 INT, @UserId2 INT, @UserId3 INT, @UserId4 INT, @UserId5 INT, @UserId6 INT, @UserId7 INT, @UserId8 INT, @UserId9 INT, @UserId10 INT;
DECLARE @PasswordHash VARCHAR(255) = '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy';

-- USER 1
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'learner_test1@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Nguyễn Văn', N'Tiến', 'learner_test1@elearning.com', '0905000001', N'Đam mê dạy học lập trình C++.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test1.jpg', NULL, 'ACTIVE', DATEADD(hour, -12, GETDATE()));
    SET @UserId1 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId1 = id FROM users WHERE email = 'learner_test1@elearning.com'; END

-- USER 2
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'learner_test2@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Trần Thị', N'Quỳnh', 'learner_test2@elearning.com', '0905000002', N'Giảng viên tiếng Anh có 2 năm kinh nghiệm.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test2.jpg', NULL, 'ACTIVE', DATEADD(day, -2, GETDATE()));
    SET @UserId2 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId2 = id FROM users WHERE email = 'learner_test2@elearning.com'; END

-- USER 3
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'learner_test3@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Phạm Minh', N'Hoàng', 'learner_test3@elearning.com', '0905000003', N'Kỹ sư phần mềm mong muốn chia sẻ kiến thức React Native.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test3.jpg', NULL, 'ACTIVE', DATEADD(day, -3, GETDATE()));
    SET @UserId3 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId3 = id FROM users WHERE email = 'learner_test3@elearning.com'; END

-- USER 4
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'learner_test4@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Hoàng Gia', N'Bảo', 'learner_test4@elearning.com', '0905000004', N'Chuyên gia UI/UX Designer.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test4.jpg', NULL, 'ACTIVE', DATEADD(hour, -2, GETDATE()));
    SET @UserId4 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId4 = id FROM users WHERE email = 'learner_test4@elearning.com'; END

-- USER 5
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'learner_test5@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Lê Minh', N'Khánh', 'learner_test5@elearning.com', '0905000005', N'Fullstack Developer.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test1.jpg', NULL, 'ACTIVE', DATEADD(hour, -14, GETDATE()));
    SET @UserId5 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId5 = id FROM users WHERE email = 'learner_test5@elearning.com'; END

-- USER 6
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'learner_test6@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Phan Thanh', N'Hà', 'learner_test6@elearning.com', '0905000006', N'Data Analyst.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test2.jpg', NULL, 'ACTIVE', DATEADD(hour, -16, GETDATE()));
    SET @UserId6 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId6 = id FROM users WHERE email = 'learner_test6@elearning.com'; END

-- USER 7
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'learner_test7@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Đặng Quốc', N'Bảo', 'learner_test7@elearning.com', '0905000007', N'DevOps Engineer.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test3.jpg', NULL, 'ACTIVE', DATEADD(hour, -18, GETDATE()));
    SET @UserId7 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId7 = id FROM users WHERE email = 'learner_test7@elearning.com'; END

-- USER 8
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'learner_test8@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Bùi Minh', N'Tuấn', 'learner_test8@elearning.com', '0905000008', N'Backend developer.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test4.jpg', NULL, 'ACTIVE', DATEADD(hour, -20, GETDATE()));
    SET @UserId8 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId8 = id FROM users WHERE email = 'learner_test8@elearning.com'; END

-- USER 9
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'learner_test9@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Vũ Thị', N'Lan', 'learner_test9@elearning.com', '0905000009', N'Tester.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test1.jpg', NULL, 'ACTIVE', DATEADD(hour, -22, GETDATE()));
    SET @UserId9 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId9 = id FROM users WHERE email = 'learner_test9@elearning.com'; END

-- USER 10
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'learner_test10@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Đỗ Hoàng', N'Anh', 'learner_test10@elearning.com', '0905000010', N'AI Engineer.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test2.jpg', NULL, 'ACTIVE', DATEADD(hour, -24, GETDATE()));
    SET @UserId10 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId10 = id FROM users WHERE email = 'learner_test10@elearning.com'; END


-- -------------------------------------------------------------
-- 2. GÁN VAI TRÒ HỌC VIÊN (ROLE_ID = 4) NẾU CHƯA CÓ
-- -------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId1 AND role_id = 4) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId1, 4);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId2 AND role_id = 4) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId2, 4);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId3 AND role_id = 4) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId3, 4);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId4 AND role_id = 4) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId4, 4);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId5 AND role_id = 4) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId5, 4);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId6 AND role_id = 4) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId6, 4);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId7 AND role_id = 4) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId7, 4);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId8 AND role_id = 4) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId8, 4);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId9 AND role_id = 4) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId9, 4);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId10 AND role_id = 4) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId10, 4);


-- -------------------------------------------------------------
-- 3. XÓA CÁC YÊU CẦU CŨ CỦA CÁC USER NÀY
-- -------------------------------------------------------------
DELETE FROM instructor_requests WHERE user_id IN (@UserId1, @UserId2, @UserId3, @UserId4, @UserId5, @UserId6, @UserId7, @UserId8, @UserId9, @UserId10);


-- -------------------------------------------------------------
-- 4. THÊM MỚI CÁC YÊU CẦU LÀM GIẢNG VIÊN (INSTRUCTOR REQUESTS)
-- -------------------------------------------------------------
INSERT INTO instructor_requests (
    user_id, cv_url, national_id_card_front, national_id_card_back, description, bio, certificate_url, rejection_reason, status, reviewed_by, created_at, updated_at
)
VALUES
-- Yêu cầu 1: PENDING
(
    @UserId1, 
    'https://fptcontainer.blob.core.windows.net/cvs/9_NguyenVanTien_CV.pdf', 
    'https://fptcontainer.blob.core.windows.net/national-ids/9_front.jpg', 
    'https://fptcontainer.blob.core.windows.net/national-ids/9_back.jpg', 
    N'Tôi muốn tham gia giảng dạy các khóa học cấu trúc dữ liệu và giải thuật bằng ngôn ngữ C++ cho người mới bắt đầu.', 
    N'Kỹ sư lập trình C++ tại FPT Software', 
    'https://fptcontainer.blob.core.windows.net/certificates/9_IELTS_Certificate.pdf', 
    NULL, 
    'PENDING', 
    NULL, 
    DATEADD(hour, -12, GETDATE()), 
    NULL
),

-- Yêu cầu 2: APPROVED (Duyệt bởi manager 1 - ID: 3)
(
    @UserId2, 
    'https://fptcontainer.blob.core.windows.net/cvs/10_TranThiQuynh_CV.pdf', 
    'https://fptcontainer.blob.core.windows.net/national-ids/10_front.jpg', 
    'https://fptcontainer.blob.core.windows.net/national-ids/10_back.jpg', 
    N'Tôi mong muốn xây dựng các khoá học tiếng Anh giao tiếp và chuẩn bị cho bài thi IELTS.', 
    N'Giảng viên tiếng Anh tại trung tâm Anh ngữ lớn', 
    'https://fptcontainer.blob.core.windows.net/certificates/10_TOEFL_Certificate.pdf', 
    NULL, 
    'APPROVED', 
    3, 
    DATEADD(day, -2, GETDATE()), 
    DATEADD(day, -1, GETDATE())
),

-- Yêu cầu 3: REJECTED (Từ chối bởi manager 2 - ID: 4)
(
    @UserId3, 
    'https://fptcontainer.blob.core.windows.net/cvs/11_PhamMinhHoang_CV.pdf', 
    'https://fptcontainer.blob.core.windows.net/national-ids/11_front.jpg', 
    'https://fptcontainer.blob.core.windows.net/national-ids/11_back.jpg', 
    N'Muốn xuất bản khoá học lập trình React Native thực chiến.', 
    N'Senior Mobile Developer tại công ty công nghệ', 
    'https://fptcontainer.blob.core.windows.net/certificates/11_Cert.pdf', 
    N'Ảnh chụp CCCD bị mờ, không nhìn rõ thông tin cá nhân. Vui lòng tải lên ảnh chụp rõ nét hơn.', 
    'REJECTED', 
    4, 
    DATEADD(day, -3, GETDATE()), 
    DATEADD(day, -2, GETDATE())
),

-- Yêu cầu 4: PENDING
(
    @UserId4, 
    'https://fptcontainer.blob.core.windows.net/cvs/12_HoangGiaBao_CV.pdf', 
    'https://fptcontainer.blob.core.windows.net/national-ids/12_front.jpg', 
    'https://fptcontainer.blob.core.windows.net/national-ids/12_back.jpg', 
    N'Giảng dạy các kiến thức thiết kế giao diện Figma chuyên sâu cho Web & App.', 
    N'UI/UX Design Lead', 
    'https://fptcontainer.blob.core.windows.net/certificates/12_DesignCert.pdf', 
    NULL, 
    'PENDING', 
    NULL, 
    DATEADD(hour, -2, GETDATE()), 
    NULL
),

-- Yêu cầu 5: PENDING
(
    @UserId5, 
    'https://fptcontainer.blob.core.windows.net/cvs/test_cv_5.pdf', 
    'https://fptcontainer.blob.core.windows.net/national-ids/test_front_5.jpg', 
    'https://fptcontainer.blob.core.windows.net/national-ids/test_back_5.jpg', 
    N'Giảng dạy lập trình Javascript từ Zero đến Hero.', 
    N'Fullstack Developer', 
    'https://fptcontainer.blob.core.windows.net/certificates/test_cert_5.pdf', 
    NULL, 
    'PENDING', 
    NULL, 
    DATEADD(hour, -14, GETDATE()), 
    NULL
),

-- Yêu cầu 6: PENDING
(
    @UserId6, 
    'https://fptcontainer.blob.core.windows.net/cvs/test_cv_6.pdf', 
    'https://fptcontainer.blob.core.windows.net/national-ids/test_front_6.jpg', 
    'https://fptcontainer.blob.core.windows.net/national-ids/test_back_6.jpg', 
    N'Giảng dạy các kiến thức phân tích dữ liệu cơ bản.', 
    N'Data Analyst', 
    'https://fptcontainer.blob.core.windows.net/certificates/test_cert_6.pdf', 
    NULL, 
    'PENDING', 
    NULL, 
    DATEADD(hour, -16, GETDATE()), 
    NULL
),

-- Yêu cầu 7: PENDING
(
    @UserId7, 
    'https://fptcontainer.blob.core.windows.net/cvs/test_cv_7.pdf', 
    'https://fptcontainer.blob.core.windows.net/national-ids/test_front_7.jpg', 
    'https://fptcontainer.blob.core.windows.net/national-ids/test_back_7.jpg', 
    N'Giảng dạy DevOps và CI/CD thực chiến.', 
    N'DevOps Engineer', 
    'https://fptcontainer.blob.core.windows.net/certificates/test_cert_7.pdf', 
    NULL, 
    'PENDING', 
    NULL, 
    DATEADD(hour, -18, GETDATE()), 
    NULL
),

-- Yêu cầu 8: PENDING
(
    @UserId8, 
    'https://fptcontainer.blob.core.windows.net/cvs/test_cv_8.pdf', 
    'https://fptcontainer.blob.core.windows.net/national-ids/test_front_8.jpg', 
    'https://fptcontainer.blob.core.windows.net/national-ids/test_back_8.jpg', 
    N'Xây dựng các hệ thống backend chịu tải cao.', 
    N'Backend Developer', 
    'https://fptcontainer.blob.core.windows.net/certificates/test_cert_8.pdf', 
    NULL, 
    'PENDING', 
    NULL, 
    DATEADD(hour, -20, GETDATE()), 
    NULL
),

-- Yêu cầu 9: PENDING
(
    @UserId9, 
    'https://fptcontainer.blob.core.windows.net/cvs/test_cv_9.pdf', 
    'https://fptcontainer.blob.core.windows.net/national-ids/test_front_9.jpg', 
    'https://fptcontainer.blob.core.windows.net/national-ids/test_back_9.jpg', 
    N'Kiểm thử phần mềm tự động với Selenium.', 
    N'Senior QA/QC', 
    'https://fptcontainer.blob.core.windows.net/certificates/test_cert_9.pdf', 
    NULL, 
    'PENDING', 
    NULL, 
    DATEADD(hour, -22, GETDATE()), 
    NULL
),

-- Yêu cầu 10: PENDING
(
    @UserId10, 
    'https://fptcontainer.blob.core.windows.net/cvs/test_cv_10.pdf', 
    'https://fptcontainer.blob.core.windows.net/national-ids/test_front_10.jpg', 
    'https://fptcontainer.blob.core.windows.net/national-ids/test_back_10.jpg', 
    N'Giảng dạy cơ bản Machine Learning và Deep Learning.', 
    N'AI Engineer', 
    'https://fptcontainer.blob.core.windows.net/certificates/test_cert_10.pdf', 
    NULL, 
    'PENDING', 
    NULL, 
    DATEADD(hour, -24, GETDATE()), 
    NULL
);


-- =========================================================================
-- 5. DỮ LIỆU TEST DASHBOARD MẪU (KHÓA HỌC CHỜ DUYỆT, BÁO CÁO VI PHẠM, BIỂU ĐỒ)
-- =========================================================================

DELETE FROM feedback_reports WHERE reason IN (N'Bình luận thô tục, xúc phạm giảng viên', N'Spam nội dung quảng cáo');
DELETE FROM feedbacks WHERE comment IN (N'Khoá học rất hay, nhưng bài 3 video hơi mờ.', N'Quá tệ, giảng viên nói tục tĩu.', N'Giảng viên lười trả lời câu hỏi, khoá học cũ kỹ.');
DELETE FROM courses WHERE title IN (N'Lập trình Java Web với Spring Boot', N'Thiết kế giao diện nâng cao với Figma');
DELETE FROM payments WHERE gateway = 'TEST_GATEWAY';
DELETE FROM orders WHERE payment_method = 'TEST_METHOD';

-- Tạo các khóa học chờ phê duyệt (PENDING COURSES)
INSERT INTO courses (instructor_id, category_id, title, description, thumbnail_url, price, level, status, approved_by, approved_at, created_at)
VALUES 
(5, 9, N'Lập trình Java Web với Spring Boot', N'Học Spring MVC, JPA, Security và xây dựng Restful API hoàn chỉnh.', 'fptthumbnail.jpg', 1000, 'ADVANCED', 'PENDING', NULL, NULL, DATEADD(hour, -5, GETDATE())),
(6, 5, N'Thiết kế giao diện nâng cao với Figma', N'Làm chủ Figma, AutoLayout, Component, Variable và Design System.', 'fptthumbnail.jpg', 1000, 'INTERMEDIATE', 'PENDING', NULL, NULL, DATEADD(hour, -1, GETDATE()));

-- Tạo feedback & báo cáo vi phạm
DECLARE @FeedbackId1 INT, @FeedbackId2 INT, @FeedbackId3 INT;

INSERT INTO feedbacks (user_id, course_id, rating, comment, status, created_at)
VALUES (7, 1, 4, N'Khoá học rất hay, nhưng bài 3 video hơi mờ.', 'VISIBLE', GETDATE());
SET @FeedbackId1 = SCOPE_IDENTITY();

INSERT INTO feedbacks (user_id, course_id, rating, comment, status, created_at)
VALUES (8, 2, 1, N'Quá tệ, giảng viên nói tục tĩu.', 'VISIBLE', GETDATE());
SET @FeedbackId2 = SCOPE_IDENTITY();

INSERT INTO feedbacks (user_id, course_id, rating, comment, status, created_at)
VALUES (7, 3, 2, N'Giảng viên lười trả lời câu hỏi, khoá học cũ kỹ.', 'VISIBLE', GETDATE());
SET @FeedbackId3 = SCOPE_IDENTITY();

INSERT INTO feedback_reports (feedback_id, reporter_id, reason, status, resolved_by, created_at)
VALUES 
(@FeedbackId2, 7, N'Bình luận thô tục, xúc phạm giảng viên', 'PENDING', NULL, DATEADD(hour, -3, GETDATE())),
(@FeedbackId3, 8, N'Spam nội dung quảng cáo', 'PENDING', NULL, DATEADD(hour, -1, GETDATE()));


-- =========================
-- LESSON MATERIALS
-- Lessons 1 -> 9
-- =========================

INSERT INTO lesson_materials
(
    instructor_id,
    lesson_id,
    file_name,
    file_url,
    file_type,
    file_size
)
VALUES
    (5, 1, N'[28Tech]. BUOI 1.pdf', '[28Tech]. BUOI 1.pdf', 'pdf', 1048576),
    (5, 2, N'[28Tech]. BUOI 1.pdf', '[28Tech]. BUOI 1.pdf', 'pdf', 1048576),
    (5, 3, N'[28Tech]. BUOI 1.pdf', '[28Tech]. BUOI 1.pdf', 'pdf', 1048576),
    (5, 4, N'[28Tech]. BUOI 1.pdf', '[28Tech]. BUOI 1.pdf', 'pdf', 1048576),
    (5, 5, N'[28Tech]. BUOI 1.pdf', '[28Tech]. BUOI 1.pdf', 'pdf', 1048576),
    (5, 6, N'[28Tech]. BUOI 1.pdf', '[28Tech]. BUOI 1.pdf', 'pdf', 1048576),
    (5, 7, N'[28Tech]. BUOI 1.pdf', '[28Tech]. BUOI 1.pdf', 'pdf', 1048576),
    (5, 8, N'[28Tech]. BUOI 1.pdf', '[28Tech]. BUOI 1.pdf', 'pdf', 1048576),
    (5, 9, N'[28Tech]. BUOI 1.pdf', '[28Tech]. BUOI 1.pdf', 'pdf', 1048576);
