-- USE [ElearningPlatform];
-- GO


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
 'ACTIVE'),

(N'Nguyễn', N'Tuấn', 'learner3@elearning.com', '0904000003',
 NULL,
 '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy',
 'avatar.jpg',
 NULL,
 'ACTIVE'),

(N'Lê', N'Hương', 'learner4@elearning.com', '0904000004',
 NULL,
 '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy',
 'avatar.jpg',
 NULL,
 'ACTIVE'),

(N'Phạm', N'Vy', 'learner5@elearning.com', '0904000005',
 NULL,
 '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy',
 'avatar.jpg',
 NULL,
 'ACTIVE'),

(N'Nguyễn', N'Thảo', 'learner6@elearning.com', '0904000006', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Trần', N'Linh', 'learner7@elearning.com', '0904000007', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Lê', N'Kha', 'learner8@elearning.com', '0904000008', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Phạm', N'Phong', 'learner9@elearning.com', '0904000009', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Hoàng', N'Sơn', 'learner10@elearning.com', '0904000010', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Vũ', N'Lan', 'learner11@elearning.com', '0904000011', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Đặng', N'Hùng', 'learner12@elearning.com', '0904000012', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Bùi', N'Trang', 'learner13@elearning.com', '0904000013', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Đỗ', N'Phúc', 'learner14@elearning.com', '0904000014', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Hồ', N'Quân', 'learner15@elearning.com', '0904000015', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Ngô', N'Mai', 'learner16@elearning.com', '0904000016', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Dương', N'Nam', 'learner17@elearning.com', '0904000017', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Lý', N'Hà', 'learner18@elearning.com', '0904000018', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Vương', N'Tú', 'learner19@elearning.com', '0904000019', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Trịnh', N'Hải', 'learner20@elearning.com', '0904000020', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Đoàn', N'Hòa', 'learner21@elearning.com', '0904000021', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Lâm', N'Yến', 'learner22@elearning.com', '0904000022', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Phùng', N'Cường', 'learner23@elearning.com', '0904000023', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Tống', N'Huy', 'learner24@elearning.com', '0904000024', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE'),
(N'Diệp', N'Trúc', 'learner25@elearning.com', '0904000025', NULL, '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy', 'avatar.jpg', NULL, 'ACTIVE');

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
(8, 4),
(9, 4),
(10, 4),
(11, 4),
(12, 4),
(13, 4),
(14, 4),
(15, 4),
(16, 4),
(17, 4),
(18, 4),
(19, 4),
(20, 4),
(21, 4),
(22, 4),
(23, 4),
(24, 4),
(25, 4),
(26, 4),
(27, 4),
(28, 4),
(29, 4),
(30, 4),
(31, 4);

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
    N'Java Core Từ Cơ Bản Đến Nâng Cao.jpg',
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
    N'ReactJS Thực Chiến.png',
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
    N'CSS Mastery.jpg',
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
    N'Node.js REST API Với Express.jpg',
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
    N'Python Cho Người Mới Bắt Đầu.jpg',
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
    N'Swift Programming Fundamentals.jpg',
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
    N'Xây Dựng Giao Diện Với SwiftUI.jpg',
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
    N'Spring Boot REST API.jpg',
    1000,
    'ADVANCED',
    'PUBLISHED',
    4,
    GETDATE()
),

-- HTML (ID = 4)
(
    5,
    4,
    N'HTML5 & CSS3 Cơ Bản Cho Người Mới',
    N'Khóa học nền tảng thiết kế giao diện web với HTML5 và CSS3.',
    N'HTML5 & CSS3 Cơ Bản Cho Người Mới.jpg',
    190000,
    'BEGINNER',
    'PUBLISHED',
    3,
    GETDATE()
),
(
    6,
    4,
    N'Thiết Kế Web Landing Page Với HTML5',
    N'Lập trình và hoàn thiện giao diện landing page chuyên nghiệp.',
    N'Thiết Kế Web Landing Page Với HTML5.jpg',
    350000,
    'INTERMEDIATE',
    'PUBLISHED',
    4,
    GETDATE()
),

-- CSS (ID = 5)
(
    5,
    5,
    N'Tailwind CSS Từ Zero Đến Hero',
    N'Sử dụng Tailwind CSS để thiết kế nhanh các giao diện hiện đại.',
    N'Tailwind CSS Từ Zero Đến Hero.jpg',
    250000,
    'BEGINNER',
    'PUBLISHED',
    3,
    GETDATE()
),
(
    6,
    5,
    N'Responsive Web Design Với Flexbox & Grid',
    N'Làm chủ bố cục Responsive với CSS Flexbox và CSS Grid.',
    N'Responsive Web Design Với Flexbox & Grid.jpg',
    450000,
    'INTERMEDIATE',
    'PUBLISHED',
    4,
    GETDATE()
),

-- React (ID = 6)
(
    5,
    6,
    N'React Native - Lập Trình Di Động Thực Chiến',
    N'Xây dựng ứng dụng di động đa nền tảng Android/iOS bằng React Native.',
    N'React Native - Lập Trình Di Động Thực Chiến.jpg',
    890000,
    'ADVANCED',
    'PUBLISHED',
    3,
    GETDATE()
),
(
    6,
    6,
    N'Next.js 14 - Tối Ưu Hóa Ứng Dụng React',
    N'Xây dựng ứng dụng Server-side Rendering với Next.js 14 mới nhất.',
    N'Next.js 14 - Tối Ưu Hóa Ứng Dụng React.png',
    1150000,
    'ADVANCED',
    'PUBLISHED',
    4,
    GETDATE()
),

-- Node.js (ID = 7)
(
    5,
    7,
    N'Node.js RESTful API & NestJS nâng cao',
    N'Thiết kế kiến trúc hệ thống chuyên nghiệp với NestJS và Node.js.',
    N'Node.js RESTful API & NestJS nâng cao.png',
    1290000,
    'ADVANCED',
    'PUBLISHED',
    3,
    GETDATE()
),
(
    6,
    7,
    N'Lập Trình Backend Thực Chiến Với Node.js & MongoDB',
    N'Kết nối Express, Node.js với cơ sở dữ liệu MongoDB NoSQL.',
    N'Lập Trình Backend Thực Chiến Với Node.js & MongoDB.jpg',
    490000,
    'INTERMEDIATE',
    'PUBLISHED',
    4,
    GETDATE()
),

-- Python (ID = 8)
(
    5,
    8,
    N'Phân Tích Dữ Liệu Với Python, Pandas & NumPy',
    N'Sử dụng thư viện Pandas và NumPy để xử lý và phân tích số liệu.',
    N'Phân Tích Dữ Liệu Với Python, Pandas & NumPy.jpg',
    750000,
    'INTERMEDIATE',
    'PUBLISHED',
    3,
    GETDATE()
),
(
    6,
    8,
    N'Django & Python - Xây Dựng Website Tin Tức',
    N'Tạo dự án Website tin tức hoàn thiện bằng Python & Django Framework.',
    N'Django & Python - Xây Dựng Website Tin Tức.jpg',
    590000,
    'INTERMEDIATE',
    'PUBLISHED',
    4,
    GETDATE()
),

-- Java (ID = 9)
(
    5,
    9,
    N'Lập Trình Hướng Đối Tượng Java Core Cơ Bản',
    N'Học 4 tính chất OOP cơ bản trong Java: Kế thừa, Đa hình, Đóng gói, Trừu tượng.',
    N'Lập Trình Hướng Đối Tượng Java Core Cơ Bản.jpg',
    150000,
    'BEGINNER',
    'PUBLISHED',
    3,
    GETDATE()
),
(
    6,
    9,
    N'Microservices Với Spring Boot & Spring Cloud',
    N'Xây dựng hệ thống phân tán chịu tải cao bằng Microservices.',
    N'Microservices Với Spring Boot & Spring Cloud.png',
    1490000,
    'ADVANCED',
    'PUBLISHED',
    4,
    GETDATE()
),

-- Swift (ID = 10)
(
    5,
    10,
    N'iOS Development Swift & Xcode Thực Hành',
    N'Tự làm ứng dụng iOS đầu tiên sử dụng Storyboard, AutoLayout và Xcode.',
    N'iOS Development Swift & Xcode Thực Hành.jpg',
    650000,
    'BEGINNER',
    'PUBLISHED',
    3,
    GETDATE()
),
(
    6,
    10,
    N'Cấu Trúc Dữ Liệu & Giải Thuật Bằng Swift',
    N'Cải thiện tư duy thuật toán khi thiết kế logic trên iOS.',
    N'Cấu Trúc Dữ Liệu & Giải Thuật Bằng Swift.jpg',
    990000,
    'ADVANCED',
    'PUBLISHED',
    4,
    GETDATE()
),

-- SwiftUI (ID = 11)
(
    5,
    11,
    N'SwiftUI Animation - Chuyển Động Đẹp Mắt',
    N'Tạo các chuyển cảnh, chuyển động mượt mà bằng SwiftUI Animation.',
    N'SwiftUI Animation - Chuyển Động Đẹp Mắt.jpg',
    550000,
    'ADVANCED',
    'PUBLISHED',
    3,
    GETDATE()
),
(
    6,
    11,
    N'Xây Dựng Clone App iOS Với SwiftUI & Firebase',
    N'Clone ứng dụng mạng xã hội nổi tiếng sử dụng SwiftUI và Realtime Database.',
    N'Xây Dựng Clone App iOS Với SwiftUI & Firebase.jpg',
    1090000,
    'INTERMEDIATE',
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

-- Course 9: HTML5 & CSS3 Cơ Bản Cho Người Mới
INSERT INTO course_sections (course_id, title, position) VALUES
(9, N'Chương 1: Giới thiệu HTML5', 1),
(9, N'Chương 2: Thẻ và Bố cục cơ bản', 2),
(9, N'Chương 3: Kết hợp CSS3 cơ bản', 3);

-- Course 10: Thiết Kế Web Landing Page Với HTML5
INSERT INTO course_sections (course_id, title, position) VALUES
(10, N'Chương 1: Lập kế hoạch Landing Page', 1),
(10, N'Chương 2: Cấu trúc HTML & Layout', 2),
(10, N'Chương 3: Hoàn thiện Landing Page', 3);

-- Course 11: Tailwind CSS Từ Zero Đến Hero
INSERT INTO course_sections (course_id, title, position) VALUES
(11, N'Chương 1: Setup Tailwind CSS', 1),
(11, N'Chương 2: Tailwind Utility Classes', 2),
(11, N'Chương 3: Responsive & Customization', 3);

-- Course 12: Responsive Web Design Với Flexbox & Grid
INSERT INTO course_sections (course_id, title, position) VALUES
(12, N'Chương 1: Tư duy Responsive', 1),
(12, N'Chương 2: Flexbox Layout', 2),
(12, N'Chương 3: Grid Layout chuyên sâu', 3);

-- Course 13: React Native - Lập Trình Di Động Thực Chiến
INSERT INTO course_sections (course_id, title, position) VALUES
(13, N'Chương 1: Khởi động dự án React Native', 1),
(13, N'Chương 2: Core Components & Navigation', 2),
(13, N'Chương 3: API & State Management', 3);

-- Course 14: Next.js 14 - Tối Ưu Hóa Ứng Dụng React
INSERT INTO course_sections (course_id, title, position) VALUES
(14, N'Chương 1: App Router & Routing', 1),
(14, N'Chương 2: Server & Client Components', 2),
(14, N'Chương 3: Data Fetching & Caching', 3);

-- Course 15: Node.js RESTful API & NestJS nâng cao
INSERT INTO course_sections (course_id, title, position) VALUES
(15, N'Chương 1: Kiến trúc NestJS', 1),
(15, N'Chương 2: Controllers & Providers', 2),
(15, N'Chương 3: Authentication & Security', 3);

-- Course 16: Lập Trình Backend Thực Chiến Với Node.js & MongoDB
INSERT INTO course_sections (course_id, title, position) VALUES
(16, N'Chương 1: Cơ sở dữ liệu NoSQL MongoDB', 1),
(16, N'Chương 2: Mongoose ODM trong Express', 2),
(16, N'Chương 3: CRUD & Middlewares', 3);

-- Course 17: Phân Tích Dữ Liệu Với Python, Pandas & NumPy
INSERT INTO course_sections (course_id, title, position) VALUES
(17, N'Chương 1: Giới thiệu NumPy', 1),
(17, N'Chương 2: Pandas DataFrames', 2),
(17, N'Chương 3: Trực quan hóa dữ liệu', 3);

-- Course 18: Django & Python - Xây Dựng Website Tin Tức
INSERT INTO course_sections (course_id, title, position) VALUES
(18, N'Chương 1: Cấu trúc Django Project', 1),
(18, N'Chương 2: Models & Django Admin', 2),
(18, N'Chương 3: Views, Templates & URL routing', 3);

-- Course 19: Lập Trình Hướng Đối Tượng Java Core Cơ Bản
INSERT INTO course_sections (course_id, title, position) VALUES
(19, N'Chương 1: Cú pháp cơ bản Java', 1),
(19, N'Chương 2: Lớp và Đối tượng', 2),
(19, N'Chương 3: Tính chất OOP cơ bản', 3);

-- Course 20: Microservices Với Spring Boot & Spring Cloud
INSERT INTO course_sections (course_id, title, position) VALUES
(20, N'Chương 1: Kiến trúc Microservices', 1),
(20, N'Chương 2: Service Discovery & Gateway', 2),
(20, N'Chương 3: Distributed Tracing & Config', 3);

-- Course 21: iOS Development Swift & Xcode Thực Hành
INSERT INTO course_sections (course_id, title, position) VALUES
(21, N'Chương 1: Giao diện Xcode', 1),
(21, N'Chương 2: Storyboard & AutoLayout', 2),
(21, N'Chương 3: ViewControllers & Lifecycle', 3);

-- Course 22: Cấu Trúc Dữ Liệu & Giải Thuật Bằng Swift
INSERT INTO course_sections (course_id, title, position) VALUES
(22, N'Chương 1: Các cấu trúc dữ liệu cơ bản', 1),
(22, N'Chương 2: Thuật toán Sắp xếp & Tìm kiếm', 2),
(22, N'Chương 3: Cây & Đồ thị trong Swift', 3);

-- Course 23: SwiftUI Animation - Chuyển Động Đẹp Mắt
INSERT INTO course_sections (course_id, title, position) VALUES
(23, N'Chương 1: Animatable Properties', 1),
(23, N'Chương 2: Transitions & Spring Animations', 2),
(23, N'Chương 3: Custom Animations nâng cao', 3);

-- Course 24: Xây Dựng Clone App iOS Với SwiftUI & Firebase
INSERT INTO course_sections (course_id, title, position) VALUES
(24, N'Chương 1: Cấu hình Firebase SDK', 1),
(24, N'Chương 2: Đăng nhập & Đăng ký', 2),
(24, N'Chương 3: Realtime Database & Storage', 3);

-- =========================
-- LESSONS SAMPLE DATA
-- Mỗi section có 3 bài học
-- =========================

DECLARE @sectionId INT = 1;

WHILE @sectionId <= 72
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

WHILE @lessonId <= 216
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

-- ==========================================
-- BÀI QUIZ THỨ 2 CHO MỖI LESSON (ĐỂ ĐẠT ÍT NHẤT 2 QUIZ/LESSON)
-- ==========================================
DECLARE @lessonId2 INT = 1;

WHILE @lessonId2 <= 216
BEGIN

    INSERT INTO quizzes (
        lesson_id,
        title,
        pass_score_percent
    )
    VALUES (
        @lessonId2,
        N'Quiz nâng cao bài học ' + CAST(@lessonId2 AS NVARCHAR(10)),
        75
    );

    SET @lessonId2 = @lessonId2 + 1;
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
-- TỰ ĐỘNG TẠO CÂU HỎI VÀ ĐÁP ÁN MẪU PHÙ HỢP VỚI TIÊU ĐỀ KHÓA HỌC VÀ BÀI HỌC
-- =========================================================================
DECLARE @currentQuizId INT;
DECLARE @insertedQuestionId INT;

DECLARE @courseTitle NVARCHAR(255);
DECLARE @sectionTitle NVARCHAR(255);
DECLARE @lessonTitle NVARCHAR(255);

-- Sử dụng Cursor để duyệt qua tất cả các bài Quiz thực tế có trong Database
DECLARE quiz_cursor CURSOR FOR 
SELECT id FROM quizzes;

OPEN quiz_cursor;
FETCH NEXT FROM quiz_cursor INTO @currentQuizId;

WHILE @@FETCH_STATUS = 0
BEGIN
    -- Chỉ tạo câu hỏi mẫu nếu bài Quiz đó chưa có câu hỏi nào
    IF NOT EXISTS (SELECT 1 FROM quiz_questions WHERE quiz_id = @currentQuizId)
    BEGIN
        -- Lấy thông tin khóa học, chương học và bài học tương ứng với Quiz ID
        SELECT 
            @courseTitle = c.title,
            @sectionTitle = s.title,
            @lessonTitle = l.title
        FROM quizzes q
        JOIN lessons l ON q.lesson_id = l.id
        JOIN course_sections s ON l.section_id = s.id
        JOIN courses c ON s.course_id = c.id
        WHERE q.id = @currentQuizId;

        -- Câu hỏi 1 (SINGLE choice)
        INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position)
        VALUES (
            @currentQuizId, 
            N'Câu hỏi trắc nghiệm số 1 của bài học "' + @lessonTitle + N'" thuộc "' + @sectionTitle + N'" của khóa học "' + @courseTitle + N'". Đâu là khẳng định đúng?', 
            'SINGLE', 
            1, 
            1
        );
        SET @insertedQuestionId = (SELECT MAX(id) FROM quiz_questions);

        INSERT INTO quiz_answers (question_id, answer_text, is_correct)
        VALUES 
        (@insertedQuestionId, N'Khái niệm chính xác về ' + @courseTitle + N' (Đáp án đúng)', 1),
        (@insertedQuestionId, N'Định nghĩa sai lệch liên quan đến ' + @courseTitle, 0),
        (@insertedQuestionId, N'Nội dung không thuộc phạm vi của bài học ' + @lessonTitle, 0),
        (@insertedQuestionId, N'Tất cả các phương án trên đều sai', 0);

        -- Câu hỏi 2 (MULTIPLE choice)
        INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position)
        VALUES (
            @currentQuizId, 
            N'Câu hỏi trắc nghiệm số 2 của bài học "' + @lessonTitle + N'" thuộc "' + @sectionTitle + N'" của khóa học "' + @courseTitle + N'". Chọn các đáp án đúng:', 
            'MULTIPLE', 
            1, 
            2
        );
        SET @insertedQuestionId = (SELECT MAX(id) FROM quiz_questions);

        INSERT INTO quiz_answers (question_id, answer_text, is_correct)
        VALUES 
        (@insertedQuestionId, N'Đặc tính cơ bản của ' + @courseTitle + N' trong thực tế', 1),
        (@insertedQuestionId, N'Phương pháp áp dụng tốt nhất cho bài học ' + @lessonTitle, 1),
        (@insertedQuestionId, N'Lý thuyết lỗi thời không nên dùng', 0),
        (@insertedQuestionId, N'Lỗi cú pháp thường gặp khi thực hành', 0);
    END

    FETCH NEXT FROM quiz_cursor INTO @currentQuizId;
END

CLOSE quiz_cursor;
DEALLOCATE quiz_cursor;


-- =========================
-- FEEDBACKS SAMPLE DATA
-- =========================
INSERT INTO feedbacks (user_id, course_id, rating, comment, status, created_at)
VALUES
-- Course 1 (Java Core): 5, 4, 3, 5, 4
(8, 1, 4, N'Nội dung rất ổn, tuy nhiên một số bài tập cuối chương hơi khó.', 'VISIBLE', GETDATE()),
(9, 1, 3, N'Khóa học bình thường, phần giải thích OOP hơi nhanh quá.', 'VISIBLE', GETDATE()),
(10, 1, 5, N'Giảng viên nhiệt tình, hỗ trợ Q&A rất nhanh.', 'VISIBLE', GETDATE()),
(11, 1, 4, N'Tài liệu đầy đủ, video chất lượng cao.', 'VISIBLE', GETDATE()),

-- Course 2 (ReactJS): 5, 5, 4, 4, 5
(7, 2, 5, N'ReactJS thực chiến đỉnh cao, học xong làm được dự án ngay.', 'VISIBLE', GETDATE()),
(9, 2, 4, N'Nội dung cập nhật mới, thực hành nhiều.', 'VISIBLE', GETDATE()),
(10, 2, 4, N'Tốt, nhưng nên nói sâu hơn về Performance Optimization.', 'VISIBLE', GETDATE()),
(11, 2, 5, N'Giảng viên có kiến thức thực tế sâu rộng, giải thích rất dễ thấm.', 'VISIBLE', GETDATE()),

-- Course 3 (CSS): 3, 2, 4, 3, 2
(8, 3, 2, N'Khóa học hơi ngắn, thiếu bài tập thực tế về Grid Layout.', 'VISIBLE', GETDATE()),
(9, 3, 4, N'Responsive Design được giải thích khá ổn.', 'VISIBLE', GETDATE()),
(10, 3, 3, N'Tạm được, chưa xứng đáng lắm với giá tiền.', 'VISIBLE', GETDATE()),
(11, 3, 2, N'Nói hơi lan man và slide chuẩn bị hơi sơ sài.', 'VISIBLE', GETDATE()),

-- Course 4 (Node.js): 4, 4, 3, 4, 5
(7, 4, 4, N'Học làm API với Express rất thực chiến, cấu trúc thư mục chuẩn.', 'VISIBLE', GETDATE()),
(8, 4, 4, N'Rất hay, code chạy mượt mà không lỗi.', 'VISIBLE', GETDATE()),
(9, 4, 3, N'Nội dung cơ bản tốt nhưng phần Middleware giảng hơi khó hiểu.', 'VISIBLE', GETDATE()),
(10, 4, 4, N'Tài liệu tham khảo đầy đủ, có github repo kèm theo.', 'VISIBLE', GETDATE()),
(11, 4, 5, N'Khóa học xuất sắc, giảng viên có giọng nói rất cuốn hút.', 'VISIBLE', GETDATE()),

-- Course 5 (Python): 5, 4, 5, 4, 3
(7, 5, 5, N'Python cơ bản rất dễ học qua khóa này, ví dụ thực tế phong phú.', 'VISIBLE', GETDATE()),
(8, 5, 4, N'Thích hợp cho người bắt đầu từ con số 0.', 'VISIBLE', GETDATE()),
(9, 5, 5, N'Rất thích phần giải thích về OOP trong Python.', 'VISIBLE', GETDATE()),
(10, 5, 4, N'Bài tập thực hành phong phú giúp củng cố kiến thức.', 'VISIBLE', GETDATE()),
(11, 5, 3, N'Video hơi dài và tốc độ giảng hơi chậm.', 'VISIBLE', GETDATE()),

-- Course 6 (Swift): 2, 1, 3, 2, 1
(7, 6, 2, N'Nội dung Swift này cũ quá rồi, nhiều cú pháp không còn đúng ở Xcode mới.', 'VISIBLE', GETDATE()),
(8, 6, 1, N'Không nên mua, giảng viên không cập nhật nội dung mới.', 'VISIBLE', GETDATE()),
(9, 6, 3, N'Tạm được nếu tự tìm tòi sửa lỗi cú pháp thêm.', 'VISIBLE', GETDATE()),
(10, 6, 2, N'Giọng giảng viên đều đều nghe buồn ngủ quá.', 'VISIBLE', GETDATE()),
(11, 6, 1, N'Quá tệ, support cực kỳ chậm hoặc không thèm trả lời.', 'VISIBLE', GETDATE()),

-- Course 7 (SwiftUI): 5, 5, 5, 4, 5
(7, 7, 5, N'SwiftUI quá đẹp, giảng viên thiết kế giao diện đỉnh cao.', 'VISIBLE', GETDATE()),
(8, 7, 5, N'Best course về SwiftUI trên nền tảng này!', 'VISIBLE', GETDATE()),
(9, 7, 5, N'Học xong tự tin code UI cho iOS luôn.', 'VISIBLE', GETDATE()),
(10, 7, 4, N'Cần bổ dung thêm phần kết nối API.', 'VISIBLE', GETDATE()),
(11, 7, 5, N'Tài nguyên hình ảnh và source code kèm theo rất xịn.', 'VISIBLE', GETDATE()),

-- Course 8 (Spring Boot REST API): 4, 3, 4, 5, 4
(7, 8, 4, N'Kiến trúc Spring Boot chuẩn chỉnh, học được nhiều best practice.', 'VISIBLE', GETDATE()),
(8, 8, 3, N'Phần Spring Security cấu hình hơi phức tạp và giảng hơi nhanh.', 'VISIBLE', GETDATE()),
(9, 8, 4, N'Học backend Java thì nên học khóa này.', 'VISIBLE', GETDATE()),
(10, 8, 5, N'Tuyệt vời, deploy lên Azure chạy mượt mà.', 'VISIBLE', GETDATE()),
(11, 8, 4, N'Rất đáng tiền, giảng viên giải đáp thắc mắc chi tiết.', 'VISIBLE', GETDATE()),

-- Course 9 (HTML5 & CSS3): 5, 4, 4, 5, 3
(7, 9, 5, N'Nhập môn Web không thể bỏ qua khóa này, rất dễ hiểu.', 'VISIBLE', GETDATE()),
(8, 9, 4, N'Nền tảng vững chắc để học tiếp Front-End.', 'VISIBLE', GETDATE()),
(9, 9, 4, N'Bài tập làm web đơn giản khá thú vị.', 'VISIBLE', GETDATE()),
(10, 9, 5, N'Thích cách giảng viên truyền đạt kiến thức.', 'VISIBLE', GETDATE()),
(11, 9, 3, N'Hơi cơ bản, mong có thêm phần nâng cao.', 'VISIBLE', GETDATE()),

-- Course 10 (Web Landing Page): 3, 2, 3, 4, 2
(7, 10, 3, N'Khóa học trung bình, giao diện mẫu chưa được đẹp lắm.', 'VISIBLE', GETDATE()),
(8, 10, 2, N'Thiếu phần tối ưu SEO cho Landing Page.', 'VISIBLE', GETDATE()),
(9, 10, 3, N'Tạm ổn để làm quen với layout.', 'VISIBLE', GETDATE()),
(10, 10, 4, N'Hướng dẫn từng bước khá dễ đi theo.', 'VISIBLE', GETDATE()),
(11, 10, 2, N'Không học được nhiều kỹ thuật mới, toàn copy code.', 'VISIBLE', GETDATE()),

-- Course 11 (Tailwind CSS): 5, 5, 4, 5, 5
(7, 11, 5, N'Học xong bỏ luôn CSS thuần, viết Tailwind siêu nhanh.', 'VISIBLE', GETDATE()),
(8, 11, 5, N'Cấu trúc và cách sử dụng class rất trực quan.', 'VISIBLE', GETDATE()),
(9, 11, 4, N'Giải thích cặn kẽ cách custom config Tailwind.', 'VISIBLE', GETDATE()),
(10, 11, 5, N'Tuyệt vời, tiết kiệm bao nhiêu thời gian làm UI.', 'VISIBLE', GETDATE()),
(11, 11, 5, N'Giảng viên giải thích rất hay, ví dụ sinh động.', 'VISIBLE', GETDATE()),

-- Course 12 (Responsive Web Design): 4, 3, 5, 4, 4
(7, 12, 4, N'Hiểu sâu sắc về Media Query và cách chia cột.', 'VISIBLE', GETDATE()),
(8, 12, 3, N'Phần Grid hơi phức tạp, cần xem lại nhiều lần.', 'VISIBLE', GETDATE()),
(9, 12, 5, N'Responsive chạy hoàn hảo trên cả mobile và tablet.', 'VISIBLE', GETDATE()),
(10, 12, 4, N'Khóa học bổ ích cho các Web Designer.', 'VISIBLE', GETDATE()),
(11, 12, 4, N'Code mẫu sạch và dễ áp dụng vào dự án thực tế.', 'VISIBLE', GETDATE()),

-- Course 13 (React Native): 2, 2, 3, 1, 2
(7, 13, 2, N'Config môi trường Android/iOS quá phức tạp và nhiều lỗi.', 'VISIBLE', GETDATE()),
(8, 13, 2, N'Giảng viên code lỗi liên tục trong video làm mất thời gian.', 'VISIBLE', GETDATE()),
(9, 13, 3, N'Nội dung tạm chấp nhận được nếu chịu khó search StackOverflow.', 'VISIBLE', GETDATE()),
(10, 13, 1, N'Không chạy được code mẫu trên phiên bản React Native mới.', 'VISIBLE', GETDATE()),
(11, 13, 2, N'Nói lắp bắp và giải thích kiến thức không rõ ràng.', 'VISIBLE', GETDATE()),

-- Course 14 (Next.js 14): 5, 4, 5, 5, 4
(7, 14, 5, N'NextJS 14 App Router giảng cực kỳ dễ hiểu, quá hay.', 'VISIBLE', GETDATE()),
(8, 14, 4, N'Server Component và Client Component phân biệt rất rõ ràng.', 'VISIBLE', GETDATE()),
(9, 14, 5, N'SEO và Performance cải thiện rõ rệt sau khi áp dụng.', 'VISIBLE', GETDATE()),
(10, 14, 5, N'Khóa học chất lượng cao nhất về Next.js hiện tại.', 'VISIBLE', GETDATE()),
(11, 14, 4, N'Tài liệu hướng dẫn siêu chi tiết, code mẫu chuẩn.', 'VISIBLE', GETDATE()),

-- Course 15 (Node.js RESTful & NestJS): 4, 5, 4, 3, 5
(7, 15, 4, N'Kiến trúc NestJS sạch sẽ, chuẩn doanh nghiệp.', 'VISIBLE', GETDATE()),
(8, 15, 5, N'Khóa học nâng cao chất lượng, áp dụng được ngay vào công việc.', 'VISIBLE', GETDATE()),
(9, 15, 4, N'Giải thích rõ về Dependency Injection.', 'VISIBLE', GETDATE()),
(10, 15, 3, N'Phần microservices giảng hơi sơ sài.', 'VISIBLE', GETDATE()),
(11, 15, 5, N'Thích cách giảng viên thiết kế DB và bảo mật JWT.', 'VISIBLE', GETDATE()),

-- Course 16 (Node.js & MongoDB): 3, 3, 4, 3, 2
(7, 16, 3, N'Học MongoDB cơ bản ổn, nhưng phần Aggregation giảng chưa sâu.', 'VISIBLE', GETDATE()),
(8, 16, 3, N'Slide bài giảng hơi sơ sài, chủ yếu là live code.', 'VISIBLE', GETDATE()),
(9, 16, 4, N'Kết nối Express với MongoDB dễ dàng theo hướng dẫn.', 'VISIBLE', GETDATE()),
(10, 16, 3, N'Nội dung ở mức trung bình, có thể tìm thấy free trên mạng.', 'VISIBLE', GETDATE()),
(11, 16, 2, N'Nhiều đoạn code bị lỗi thời do thư viện cập nhật.', 'VISIBLE', GETDATE()),

-- Course 17 (Pandas & NumPy): 5, 5, 4, 5, 4
(7, 17, 5, N'Tuyệt vời cho AI và Data Science path, NumPy/Pandas rất trực quan.', 'VISIBLE', GETDATE()),
(8, 17, 5, N'Thao tác dữ liệu cực kỳ nhanh gọn sau khóa này.', 'VISIBLE', GETDATE()),
(9, 17, 4, N'Nhiều bài tập thực hành phân tích file CSV thực tế.', 'VISIBLE', GETDATE()),
(10, 17, 5, N'Giảng viên giải thích toán học đằng sau rất dễ hiểu.', 'VISIBLE', GETDATE()),
(11, 17, 4, N'Tài liệu Jupyter Notebook đi kèm rất tiện tra cứu.', 'VISIBLE', GETDATE()),

-- Course 18 (Django & Python): 4, 4, 3, 4, 3
(7, 18, 4, N'Xây dựng web với Django nhanh chóng, admin panel quá xịn.', 'VISIBLE', GETDATE()),
(8, 18, 4, N'Học được cách làm hệ thống phân quyền và CMT.', 'VISIBLE', GETDATE()),
(9, 18, 3, N'Tốc độ nói của giảng viên hơi nhanh ở phần Template.', 'VISIBLE', GETDATE()),
(10, 18, 4, N'Hướng dẫn Deploy lên Heroku rất chi tiết.', 'VISIBLE', GETDATE()),
(11, 18, 3, N'Nội dung tạm ổn, có thể bổ sung thêm REST Framework.', 'VISIBLE', GETDATE()),

-- Course 19 (OOP Java Core): 1, 2, 1, 3, 2
(7, 19, 1, N'Khóa học chán quá, giảng viên chỉ đọc slide.', 'VISIBLE', GETDATE()),
(8, 19, 2, N'Bài tập OOP quá đơn điệu, không thực tế.', 'VISIBLE', GETDATE()),
(9, 19, 1, N'Microphone của giảng viên bị rè, nghe rất khó chịu.', 'VISIBLE', GETDATE()),
(10, 19, 3, N'Nội dung cơ bản ở mức tạm chấp nhận được.', 'VISIBLE', GETDATE()),
(11, 19, 2, N'Giải thích phần Đa hình rất mơ hồ và khó hiểu.', 'VISIBLE', GETDATE()),

-- Course 20 (Microservices): 5, 5, 5, 4, 5
(7, 20, 5, N'Kiến trúc đỉnh cao, Eureka, Gateway, Config Server đầy đủ.', 'VISIBLE', GETDATE()),
(8, 20, 5, N'Khóa học chất lượng nhất về Microservices bằng Java.', 'VISIBLE', GETDATE()),
(9, 20, 5, N'Rất thực chiến, có phần Docker hóa các service.', 'VISIBLE', GETDATE()),
(10, 20, 4, N'Cần thêm phần Kubernetes nữa thì hoàn hảo.', 'VISIBLE', GETDATE()),
(11, 20, 5, N'Thầy dạy cực kỳ tâm huyết, hỗ trợ group chat nhiệt tình.', 'VISIBLE', GETDATE()),

-- Course 21 (iOS Swift & Xcode): 4, 4, 5, 4, 3
(7, 21, 4, N'Làm quen Xcode nhanh chóng, tạo được app đầu tiên sau vài giờ.', 'VISIBLE', GETDATE()),
(8, 21, 4, N'Rất thích hợp cho người mới chuyển sang lập trình iOS.', 'VISIBLE', GETDATE()),
(9, 21, 5, N'Tư duy chia Layout bằng AutoLayout rất dễ thấm.', 'VISIBLE', GETDATE()),
(10, 21, 4, N'Bài giảng trực quan, hình ảnh đẹp.', 'VISIBLE', GETDATE()),
(11, 21, 3, N'Nên bổ sung thêm phần UIKit nâng cao.', 'VISIBLE', GETDATE()),

-- Course 22 (Data Structures Swift): 3, 3, 4, 3, 4
(7, 22, 3, N'Cấu trúc dữ liệu bằng Swift giảng ở mức vừa phải.', 'VISIBLE', GETDATE()),
(8, 22, 3, N'Nhiều phần giải thuật viết code hơi phức tạp hóa vấn đề.', 'VISIBLE', GETDATE()),
(9, 22, 4, N'Giúp cải thiện tư duy thuật toán khi làm iOS.', 'VISIBLE', GETDATE()),
(10, 22, 3, N'Tạm được, chưa có nhiều ví dụ nâng cao.', 'VISIBLE', GETDATE()),
(11, 22, 4, N'Slide đẹp, hình ảnh minh họa thuật toán dễ hiểu.', 'VISIBLE', GETDATE()),

-- Course 23 (SwiftUI Animation): 5, 5, 4, 4, 5
(7, 23, 5, N'Animation trong SwiftUI quá ảo diệu, dạy rất chi tiết.', 'VISIBLE', GETDATE()),
(8, 23, 5, N'Học xong làm được hiệu ứng chuyển động mượt mà.', 'VISIBLE', GETDATE()),
(9, 23, 4, N'Cung cấp nhiều ý tưởng thiết kế UI độc đáo.', 'VISIBLE', GETDATE()),
(10, 23, 4, N'Rất hay, mong giảng viên làm thêm phần Transition nâng cao.', 'VISIBLE', GETDATE()),
(11, 23, 5, N'Không thể chê được điểm nào, video HD siêu nét.', 'VISIBLE', GETDATE()),

-- Course 24 (Clone App & Firebase): 2, 3, 2, 3, 1
(7, 24, 2, N'Cấu hình Firebase iOS quá khó khăn và lỗi liên tục.', 'VISIBLE', GETDATE()),
(8, 24, 3, N'Khóa học hơi vội vàng ở phần xử lý Realtime Database.', 'VISIBLE', GETDATE()),
(9, 24, 2, N'Nhiều đoạn code bị lỗi do Firebase update SDK mới.', 'VISIBLE', GETDATE()),
(10, 24, 3, N'Ở mức trung bình, xem để biết luồng đăng nhập.', 'VISIBLE', GETDATE()),
(11, 24, 1, N'Không được hỗ trợ khi hỏi bài, code lỗi không chạy được.', 'VISIBLE', GETDATE());


-- =========================
-- ENROLLMENTS SAMPLE DATA
-- =========================
INSERT INTO enrollments (user_id, course_id, progress_percent, created_at)
VALUES
-- Course 1
(7, 1, 50.00, GETDATE()),
(8, 1, 40.00, GETDATE()),
(9, 1, 35.00, GETDATE()),
(10, 1, 80.00, GETDATE()),
(11, 1, 100.00, GETDATE()),

-- Course 2
(7, 2, 55.00, GETDATE()),
(8, 2, 60.00, GETDATE()),
(9, 2, 30.00, GETDATE()),
(10, 2, 75.00, GETDATE()),
(11, 2, 100.00, GETDATE()),

-- Course 3
(7, 3, 20.00, GETDATE()),
(8, 3, 10.00, GETDATE()),
(9, 3, 45.00, GETDATE()),
(10, 3, 5.00, GETDATE()),
(11, 3, 15.00, GETDATE()),

-- Course 4
(7, 4, 90.00, GETDATE()),
(8, 4, 85.00, GETDATE()),
(9, 4, 70.00, GETDATE()),
(10, 4, 65.00, GETDATE()),
(11, 4, 100.00, GETDATE()),

-- Course 5
(7, 5, 45.00, GETDATE()),
(8, 5, 50.00, GETDATE()),
(9, 5, 60.00, GETDATE()),
(10, 5, 55.00, GETDATE()),
(11, 5, 30.00, GETDATE()),

-- Course 6
(7, 6, 12.00, GETDATE()),
(8, 6, 8.00, GETDATE()),
(9, 6, 15.00, GETDATE()),
(10, 6, 20.00, GETDATE()),
(11, 6, 5.00, GETDATE()),

-- Course 7
(7, 7, 95.00, GETDATE()),
(8, 7, 100.00, GETDATE()),
(9, 7, 88.00, GETDATE()),
(10, 7, 70.00, GETDATE()),
(11, 7, 100.00, GETDATE()),

-- Course 8
(7, 8, 40.00, GETDATE()),
(8, 8, 30.00, GETDATE()),
(9, 8, 45.00, GETDATE()),
(10, 8, 50.00, GETDATE()),
(11, 8, 60.00, GETDATE()),

-- Course 9
(7, 9, 80.00, GETDATE()),
(8, 9, 70.00, GETDATE()),
(9, 9, 60.00, GETDATE()),
(10, 9, 90.00, GETDATE()),
(11, 9, 50.00, GETDATE()),

-- Course 10
(7, 10, 25.00, GETDATE()),
(8, 10, 15.00, GETDATE()),
(9, 10, 30.00, GETDATE()),
(10, 10, 40.00, GETDATE()),
(11, 10, 20.00, GETDATE()),

-- Course 11
(7, 11, 100.00, GETDATE()),
(8, 11, 95.00, GETDATE()),
(9, 11, 85.00, GETDATE()),
(10, 11, 90.00, GETDATE()),
(11, 11, 100.00, GETDATE()),

-- Course 12
(7, 12, 60.00, GETDATE()),
(8, 12, 50.00, GETDATE()),
(9, 12, 70.00, GETDATE()),
(10, 12, 65.00, GETDATE()),
(11, 12, 80.00, GETDATE()),

-- Course 13
(7, 13, 10.00, GETDATE()),
(8, 13, 15.00, GETDATE()),
(9, 13, 20.00, GETDATE()),
(10, 13, 5.00, GETDATE()),
(11, 13, 12.00, GETDATE()),

-- Course 14
(7, 14, 90.00, GETDATE()),
(8, 14, 85.00, GETDATE()),
(9, 14, 95.00, GETDATE()),
(10, 14, 100.00, GETDATE()),
(11, 14, 80.00, GETDATE()),

-- Course 15
(7, 15, 75.00, GETDATE()),
(8, 15, 80.00, GETDATE()),
(9, 15, 70.00, GETDATE()),
(10, 15, 60.00, GETDATE()),
(11, 15, 90.00, GETDATE()),

-- Course 16
(7, 16, 30.00, GETDATE()),
(8, 16, 25.00, GETDATE()),
(9, 16, 40.00, GETDATE()),
(10, 16, 35.00, GETDATE()),
(11, 16, 20.00, GETDATE()),

-- Course 17
(7, 17, 100.00, GETDATE()),
(8, 17, 90.00, GETDATE()),
(9, 17, 80.00, GETDATE()),
(10, 17, 95.00, GETDATE()),
(11, 17, 85.00, GETDATE()),

-- Course 18
(7, 18, 50.00, GETDATE()),
(8, 18, 60.00, GETDATE()),
(9, 18, 40.00, GETDATE()),
(10, 18, 55.00, GETDATE()),
(11, 18, 45.00, GETDATE()),

-- Course 19
(7, 19, 15.00, GETDATE()),
(8, 19, 20.00, GETDATE()),
(9, 19, 10.00, GETDATE()),
(10, 19, 25.00, GETDATE()),
(11, 19, 30.00, GETDATE()),

-- Course 20
(7, 20, 100.00, GETDATE()),
(8, 20, 95.00, GETDATE()),
(9, 20, 100.00, GETDATE()),
(10, 20, 85.00, GETDATE()),
(11, 20, 90.00, GETDATE()),

-- Course 21
(7, 21, 70.00, GETDATE()),
(8, 21, 65.00, GETDATE()),
(9, 21, 80.00, GETDATE()),
(10, 21, 75.00, GETDATE()),
(11, 21, 60.00, GETDATE()),

-- Course 22
(7, 22, 40.00, GETDATE()),
(8, 22, 35.00, GETDATE()),
(9, 22, 45.00, GETDATE()),
(10, 22, 30.00, GETDATE()),
(11, 22, 50.00, GETDATE()),

-- Course 23
(7, 23, 90.00, GETDATE()),
(8, 23, 95.00, GETDATE()),
(9, 23, 80.00, GETDATE()),
(10, 23, 85.00, GETDATE()),
(11, 23, 100.00, GETDATE()),

-- Course 24
(7, 24, 20.00, GETDATE()),
(8, 24, 25.00, GETDATE()),
(9, 24, 15.00, GETDATE()),
(10, 24, 30.00, GETDATE()),
(11, 24, 10.00, GETDATE());


-- =========================================================================
-- INSTRUCTOR SAMPLE TEST DATA
-- =========================================================================

-- 1. KHAI BÁO CÁC BIẾN ĐỂ LƯU ID CỦA USER
DECLARE @UserId1 INT, @UserId2 INT, @UserId3 INT, @UserId4 INT, @UserId5 INT, @UserId6 INT, @UserId7 INT, @UserId8 INT, @UserId9 INT, @UserId10 INT;
DECLARE @PasswordHash VARCHAR(255) = '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy';

-- USER 1
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'instructor_test1@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Nguyễn Văn', N'Tiến', 'instructor_test1@elearning.com', '0905000001', N'Đam mê dạy học lập trình C++.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test1.jpg', NULL, 'ACTIVE', DATEADD(hour, -12, GETDATE()));
    SET @UserId1 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId1 = id FROM users WHERE email = 'instructor_test1@elearning.com'; END

-- USER 2
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'instructor_test2@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Trần Thị', N'Quỳnh', 'instructor_test2@elearning.com', '0905000002', N'Giảng viên tiếng Anh có 2 năm kinh nghiệm.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test2.jpg', NULL, 'ACTIVE', DATEADD(day, -2, GETDATE()));
    SET @UserId2 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId2 = id FROM users WHERE email = 'instructor_test2@elearning.com'; END

-- USER 3
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'instructor_test3@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Phạm Minh', N'Hoàng', 'instructor_test3@elearning.com', '0905000003', N'Kỹ sư phần mềm mong muốn chia sẻ kiến thức React Native.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test3.jpg', NULL, 'BANNED', DATEADD(day, -3, GETDATE()));
    SET @UserId3 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId3 = id FROM users WHERE email = 'instructor_test3@elearning.com'; END

-- USER 4
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'instructor_test4@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Hoàng Gia', N'Bảo', 'instructor_test4@elearning.com', '0905000004', N'Chuyên gia UI/UX Designer.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test4.jpg', NULL, 'ACTIVE', DATEADD(hour, -2, GETDATE()));
    SET @UserId4 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId4 = id FROM users WHERE email = 'instructor_test4@elearning.com'; END

-- USER 5
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'instructor_test5@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Lê Minh', N'Khánh', 'instructor_test5@elearning.com', '0905000005', N'Fullstack Developer.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test1.jpg', NULL, 'ACTIVE', DATEADD(hour, -14, GETDATE()));
    SET @UserId5 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId5 = id FROM users WHERE email = 'instructor_test5@elearning.com'; END

-- USER 6
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'instructor_test6@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Phan Thanh', N'Hà', 'instructor_test6@elearning.com', '0905000006', N'Data Analyst.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test2.jpg', NULL, 'BANNED', DATEADD(hour, -16, GETDATE()));
    SET @UserId6 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId6 = id FROM users WHERE email = 'instructor_test6@elearning.com'; END

-- USER 7
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'instructor_test7@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Đặng Quốc', N'Bảo', 'instructor_test7@elearning.com', '0905000007', N'DevOps Engineer.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test3.jpg', NULL, 'ACTIVE', DATEADD(hour, -18, GETDATE()));
    SET @UserId7 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId7 = id FROM users WHERE email = 'instructor_test7@elearning.com'; END

-- USER 8
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'instructor_test8@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Bùi Minh', N'Tuấn', 'instructor_test8@elearning.com', '0905000008', N'Backend developer.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test4.jpg', NULL, 'ACTIVE', DATEADD(hour, -20, GETDATE()));
    SET @UserId8 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId8 = id FROM users WHERE email = 'instructor_test8@elearning.com'; END

-- USER 9
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'instructor_test9@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Vũ Thị', N'Lan', 'instructor_test9@elearning.com', '0905000009', N'Tester.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test1.jpg', NULL, 'BANNED', DATEADD(hour, -22, GETDATE()));
    SET @UserId9 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId9 = id FROM users WHERE email = 'instructor_test9@elearning.com'; END

-- USER 10
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'instructor_test10@elearning.com')
BEGIN
    INSERT INTO users (first_name, last_name, email, phone, bio, password_hash, avatar_url, google_id, status, created_at)
    VALUES (N'Đỗ Hoàng', N'Anh', 'instructor_test10@elearning.com', '0905000010', N'AI Engineer.', @PasswordHash, 'https://fptcontainer.blob.core.windows.net/avatars/avatar_test2.jpg', NULL, 'ACTIVE', DATEADD(hour, -24, GETDATE()));
    SET @UserId10 = SCOPE_IDENTITY();
END
ELSE BEGIN SELECT @UserId10 = id FROM users WHERE email = 'instructor_test10@elearning.com'; END


-- -------------------------------------------------------------
-- 2. GÁN VAI TRÒ GIẢNG VIÊN (ROLE_ID = 3) NẾU CHƯA CÓ
-- -------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId1 AND role_id = 3) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId1, 3);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId2 AND role_id = 3) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId2, 3);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId3 AND role_id = 3) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId3, 3);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId4 AND role_id = 3) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId4, 3);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId5 AND role_id = 3) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId5, 3);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId6 AND role_id = 3) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId6, 3);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId7 AND role_id = 3) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId7, 3);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId8 AND role_id = 3) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId8, 3);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId9 AND role_id = 3) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId9, 3);
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @UserId10 AND role_id = 3) INSERT INTO user_roles (user_id, role_id) VALUES (@UserId10, 3);


-- =========================================================================
-- 5. DỮ LIỆU TEST DASHBOARD MẪU (KHÓA HỌC CHỜ DUYỆT, BÁO CÁO VI PHẠM, BIỂU ĐỒ)
-- =========================================================================

DELETE FROM feedbacks WHERE comment IN (N'Khoá học rất hay, nhưng bài 3 video hơi mờ.', N'Quá tệ, giảng viên nói tục tĩu.', N'Giảng viên lười trả lời câu hỏi, khoá học cũ kỹ.');
DELETE FROM courses WHERE title IN (N'Lập trình Java Web với Spring Boot', N'Thiết kế giao diện nâng cao với Figma');
DELETE FROM payments WHERE gateway = 'TEST_GATEWAY';
DELETE FROM orders WHERE payment_method = 'TEST_METHOD';

-- Tạo các khóa học chờ phê duyệt (PENDING COURSES)
INSERT INTO courses (instructor_id, category_id, title, description, thumbnail_url, price, level, status, approved_by, approved_at, created_at)
VALUES 
(5, 9, N'Lập trình Java Web với Spring Boot', N'Học Spring MVC, JPA, Security và xây dựng Restful API hoàn chỉnh.', N'Lập trình Java Web với Spring Boot.jpg', 1000, 'ADVANCED', 'PENDING', NULL, NULL, DATEADD(hour, -5, GETDATE())),
(6, 5, N'Thiết kế giao diện nâng cao với Figma', N'Làm chủ Figma, AutoLayout, Component, Variable và Design System.', N'Thiết kế giao diện nâng cao với Figma.jpg', 1000, 'INTERMEDIATE', 'PENDING', NULL, NULL, DATEADD(hour, -1, GETDATE()));

-- Tạo feedback
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

INSERT INTO orders (
    user_id,
    total_amount,
    status,
    payment_method
)
VALUES
    (1, 1000, 'PAID', 'PAYOS'),
    (2, 2000, 'PAID', 'PAYOS'),
    (3, 5000, 'PENDING', 'PAYOS'),
    (4, 10000, 'PENDING', 'PAYOS'),
    (5, 2000, 'PENDING', 'PAYOS'),
    (6, 1000, 'PENDING', 'PAYOS'),
    (7, 1000, 'PENDING', 'PAYOS'),
    (8, 190000, 'CANCELLED', 'PAYOS'),
    (9, 350000, 'PAID', 'PAYOS'),
    (10, 250000, 'PAID', 'PAYOS');

INSERT INTO payments (
    order_id,
    gateway,
    gateway_order_code,
    amount,
    payment_url,
    qr_code_url,
    status,
    webhook_received,
    webhook_received_at,
    paid_at,
    expired_at
)
VALUES

-- Thanh toán thành công
(1, 'PAYOS', 'PAYOS_100001', 1000,
 'https://pay.payos.vn/100001',
 'https://qr.payos.vn/100001',
 'PAID',
 1,
 DATEADD(MINUTE, 1, GETDATE()),
 GETDATE(),
 DATEADD(HOUR, 2, GETDATE())),

(2, 'PAYOS', 'PAYOS_100002', 1000,
 'https://pay.payos.vn/100002',
 'https://qr.payos.vn/100002',
 'PAID',
 1,
 DATEADD(MINUTE, 2, GETDATE()),
 GETDATE(),
 DATEADD(HOUR, 2, GETDATE())),

(3, 'PAYOS', 'PAYOS_100003', 5000,
 'https://pay.payos.vn/100003',
 'https://qr.payos.vn/100003',
 'PAID',
 1,
 DATEADD(MINUTE, 3, GETDATE()),
 GETDATE(),
 DATEADD(HOUR, 2, GETDATE())),

-- Đang chờ thanh toán
(4, 'PAYOS', 'PAYOS_100004', 10000,
 'https://pay.payos.vn/100004',
 'https://qr.payos.vn/100004',
 'PENDING',
 0,
 NULL,
 NULL,
 DATEADD(HOUR, 2, GETDATE())),

(5, 'PAYOS', 'PAYOS_100005', 2000,
 'https://pay.payos.vn/100005',
 'https://qr.payos.vn/100005',
 'PENDING',
 0,
 NULL,
 NULL,
 DATEADD(HOUR, 2, GETDATE())),

-- Hết hạn
(6, 'PAYOS', 'PAYOS_100006', 1000,
 'https://pay.payos.vn/100006',
 'https://qr.payos.vn/100006',
 'EXPIRED',
 0,
 NULL,
 NULL,
 DATEADD(HOUR, -1, GETDATE())),

-- Thanh toán thất bại
(7, 'PAYOS', 'PAYOS_100007', 1000,
 'https://pay.payos.vn/100007',
 'https://qr.payos.vn/100007',
 'FAILED',
 1,
 GETDATE(),
 NULL,
 DATEADD(HOUR, 2, GETDATE())),

-- Bị hủy
(8, 'PAYOS', 'PAYOS_100008', 190000,
 'https://pay.payos.vn/100008',
 'https://qr.payos.vn/100008',
 'CANCELLED',
 0,
 NULL,
 NULL,
 DATEADD(HOUR, 2, GETDATE())),

-- Thành công
(9, 'PAYOS', 'PAYOS_100009', 350000,
 'https://pay.payos.vn/100009',
 'https://qr.payos.vn/100009',
 'PAID',
 1,
 GETDATE(),
 GETDATE(),
 DATEADD(HOUR, 2, GETDATE())),

(10, 'PAYOS', 'PAYOS_100010', 250000,
 'https://pay.payos.vn/100010',
 'https://qr.payos.vn/100010',
 'PAID',
 1,
 GETDATE(),
 GETDATE(),
 DATEADD(HOUR, 2, GETDATE()));


