USE master
IF DB_ID('ElearningPlatform') IS NOT NULL
BEGIN
    ALTER DATABASE ElearningPlatform
    SET SINGLE_USER
    WITH ROLLBACK IMMEDIATE;

    DROP DATABASE ElearningPlatform;
END
GO

CREATE DATABASE ElearningPlatform;
GO

USE ElearningPlatform;
GO

-- =========================
-- ROLES
-- =========================
-- Bảng lưu các vai trò hệ thống (tách riêng để dễ mở rộng)
CREATE TABLE roles (
                       id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh vai trò

                       name VARCHAR(50) UNIQUE NOT NULL,
    -- Tên vai trò: 'admin', 'manager', 'instructor', 'learner'

                       description NVARCHAR(255) NULL,
    -- Mô tả chi tiết vai trò

                       created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo vai trò
                       updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất
);

-- Seed dữ liệu vai trò mặc định
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'admin')
    INSERT INTO roles (name, description) VALUES ('admin', N'Quản trị hệ thống');

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'manager')
    INSERT INTO roles (name, description) VALUES ('manager', N'Quản lý nội dung');

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'instructor')
    INSERT INTO roles (name, description) VALUES ('instructor', N'Giảng viên');

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'learner')
    INSERT INTO roles (name, description) VALUES ('learner', N'Học viên');

-- =========================
-- USERS
-- =========================
CREATE TABLE users (
                       id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh duy nhất, tự động tăng

                       first_name NVARCHAR(255) NOT NULL,
    -- Họ của người dùng

                       last_name NVARCHAR(255) NOT NULL,
    -- Tên của người dùng

                       email VARCHAR(255) UNIQUE NOT NULL,
    -- Email duy nhất dùng cho đăng nhập local, phải unique

                       phone VARCHAR(20) UNIQUE NULL,
    -- Số điện thoại (tuỳ chọn), duy nhất nếu có giá trị

                       bio NVARCHAR(MAX) NULL,
    ---Giới thiệu bản thân của user (instructor)

                       password_hash VARCHAR(255) NULL,
    -- Hash mật khẩu (BCrypt/Argon2). NULL nếu user chỉ login via Google

                       avatar_url VARCHAR(500) NULL,
    -- URL ảnh đại diện (lưu link từ Azure Blob Storage)

                       google_id VARCHAR(255) NULL,
    -- Google ID nếu user authenticate via OAuth Google

                       status VARCHAR(20) NOT NULL
                           CHECK (status IN ('ACTIVE', 'BANNED')),
    -- Trạng thái: active (hoạt động), banned (cấm)

                       created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo tài khoản (mặc định là thời chạy lệnh CREATE)

                       updated_at DATETIME NULL
    -- Thời gian cập nhật gần nhất
);

CREATE TABLE user_roles (
                            id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh tự tăng cho mỗi bản ghi user_role

                            user_id INT NOT NULL,
                            role_id INT NOT NULL,

                            created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian token được tạo
                            updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất


                            CONSTRAINT UQ_user_roles_user_role UNIQUE (user_id, role_id),

                            CONSTRAINT FK_user_roles_user
                                FOREIGN KEY (user_id) REFERENCES users(id),

                            CONSTRAINT FK_user_roles_role
                                FOREIGN KEY (role_id) REFERENCES roles(id)
);
-- =========================
-- PASSWORD RESET TOKENS
-- =========================
CREATE TABLE password_reset_tokens (
                                       id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh token reset

                                       user_id INT NOT NULL,
    -- Tham chiếu đến bảng users, người dùng yêu cầu reset password

                                       token VARCHAR(255) NOT NULL,
    -- Token ngẫu nhiên được gửi qua email (dùng cho link reset)

                                       expired_at DATETIME NOT NULL,
    -- Thời gian token hết hiệu lực (thường 1 giờ sau khi tạo)

                                       is_used BIT DEFAULT 0,
    -- Cờ đánh dấu token đã được sử dụng (reset password thành công) hay chưa

                                       created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian token được tạo
                                       updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                                       CONSTRAINT FK_reset_tokens_user
                                           FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =========================
-- SYSTEM LOGS
-- =========================
CREATE TABLE system_logs (
                             id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh log

                             user_id INT NOT NULL,
    -- Tham chiếu đến bảng users, người dùng thực hiện hành động

                             action VARCHAR(255),
    -- Hành động thực hiện (ví dụ: 'create_course', 'upload_video', 'edit_course', 'delete_lesson')

                             target_type VARCHAR(100),
    -- Loại đối tượng bị tác động (ví dụ: 'course', 'lesson', 'quiz', 'user')

                             target_id VARCHAR(100),
    -- ID của đối tượng bị tác động (để trace được)

                             meta NVARCHAR(MAX) NULL,
    -- Dữ liệu bổ sung dạng JSON (ví dụ chi tiết thay đổi, thông tin cũ/mới)

                             created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian ghi log
                             updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                             CONSTRAINT FK_system_logs_user
                                 FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =========================
-- INSTRUCTOR REQUESTS
-- =========================
CREATE TABLE instructor_requests (
                                     id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh yêu cầu

                                     user_id INT NOT NULL,
    -- Tham chiếu đến bảng users, người dùng yêu cầu trở thành giáo viên

                                     cv_url VARCHAR(500) NULL,
    -- URL file CV (lưu link từ Azure Blob Storage)


                                     national_id_card_front VARCHAR(500) NOT NULL,
    -- URL file ảnh cccd mặt trước

                                     national_id_card_back VARCHAR(500) NOT NULL,
    -- URL file ảnh cccd mặt sau

                                     description NVARCHAR(MAX) NULL,
    -- Mô tả kinh nghiệm, lý do muốn trở thành giáo viên

                                     bio NVARCHAR(MAX) NULL,
    -- Mô tả tiểu sử ngắn

                                     certificate_url VARCHAR(500) NOT NULL,
    -- URL file chứng chỉ/bằng cấp (lưu link từ Azure Blob Storage)

                                     rejection_reason NVARCHAR(1000) NULL,
    -- Lý do từ chối nếu status = rejected

                                     status VARCHAR(20) NOT NULL
                                         CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    -- Trạng thái: pending (chờ duyệt), approved (phê duyệt)

                                     reviewed_by INT NULL,
    -- Tham chiếu đến manager/admin đã review yêu cầu này

                                     created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo yêu cầu

                                     updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất (khi được review)

                                     CONSTRAINT FK_instructor_requests_user
                                         FOREIGN KEY (user_id) REFERENCES users(id),
                                     CONSTRAINT FK_instructor_requests_reviewed_by
                                         FOREIGN KEY (reviewed_by) REFERENCES users(id)
);


-- =========================
-- CATEGORIES
-- =========================
CREATE TABLE categories (
                            id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh danh mục

                            name NVARCHAR(255) NOT NULL,
    -- Tên danh mục (ví dụ: 'Lập trình', 'Thiết kế', 'Kinh doanh')

                            description NVARCHAR(MAX) NULL,
    -- Mô tả chi tiết về danh mục

                            parent_id INT NULL,
    -- ID danh mục cha (cho phép phân cấp danh mục: Lập trình -> Web Development -> Frontend)

                            status VARCHAR(20)
                                CHECK (status IN ('ACTIVE', 'INACTIVE')),
    -- Trạng thái: active (hiển thị), inactive (ẩn)

                            created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo danh mục

                            updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                            CONSTRAINT FK_categories_parent
                                FOREIGN KEY (parent_id) REFERENCES categories(id)
);



-- =========================
-- COURSES
-- =========================
CREATE TABLE courses (
                         id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh khóa học

                         instructor_id INT NOT NULL,
    -- Tham chiếu đến bảng users (role='instructor'), giáo viên tạo khóa học

                         category_id INT NOT NULL,
    -- Tham chiếu đến bảng categories, danh mục khóa học

                         title NVARCHAR(255) NOT NULL,
    -- Tên khóa học (ví dụ: 'Lập trình C Cơ bản')

                         description NVARCHAR(MAX) NULL,
    -- Mô tả chi tiết nội dung, mục tiêu khóa học

                         thumbnail_url VARCHAR(500) NULL,
    -- URL ảnh bìa khóa học (lưu link từ Azure Blob Storage)

                         price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    -- Giá khóa học (0 = miễn phí)

                         level VARCHAR(20)
                             CHECK (level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    -- Mức độ: beginner (cơ bản), intermediate (trung cấp), advanced (nâng cao)

                         status VARCHAR(20)
                             CHECK (status IN ('DRAFT', 'PENDING', 'PUBLISHED', 'REJECTED', 'HIDDEN')),
    -- Trạng thái: draft (nháp), pending (chờ duyệt), published (đã xuất bản), rejected (từ chối), hidden (ẩn)

                         approved_by INT NULL,
    -- Tham chiếu đến manager/admin đã phê duyệt khóa học

                         approved_at DATETIME NULL,
    -- Thời gian phê duyệt

                         rejection_reason NVARCHAR(1000) NULL,
    -- Lý do từ chối khóa học (nếu status = rejected)

                         created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo khóa học

                         updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                         CONSTRAINT FK_courses_instructor
                             FOREIGN KEY (instructor_id) REFERENCES users(id),
                         CONSTRAINT FK_courses_category
                             FOREIGN KEY (category_id) REFERENCES categories(id),
                         CONSTRAINT FK_courses_approved_by
                             FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- =========================
-- COURSE SECTIONS
-- =========================
CREATE TABLE course_sections (
                                 id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh section (chương)

                                 course_id INT NOT NULL,
    -- Tham chiếu đến bảng courses, khóa học mà section này thuộc về

                                 title NVARCHAR(255) NOT NULL,
    -- Tên section (ví dụ: 'Chương 1: Nhập môn', 'Chương 2: Cú pháp cơ bản')

                                 position INT NOT NULL,
    -- Thứ tự hiển thị section trong khóa học (1, 2, 3, ...)

                                 created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo section

                                 updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                                 CONSTRAINT FK_sections_course
                                     FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- =========================
-- LESSONS
-- =========================
CREATE TABLE lessons (
                         id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh bài học

                         section_id INT NOT NULL,
    -- Tham chiếu đến bảng course_sections, bài học này thuộc section nào

                         title NVARCHAR(255) NOT NULL,
    -- Tên bài học (ví dụ: 'Bài 1: Giới thiệu ngôn ngữ C')

                         video_url VARCHAR(500) NULL,
    -- URL video bài học (lưu link từ Azure Blob Storage)

                         duration_seconds INT NULL CHECK (duration_seconds > 0),
    -- Thời lượng video (giây), kiểm tra phải > 0 nếu có giá trị

                         position INT NULL,
    -- Thứ tự hiển thị bài trong section (1, 2, 3, ...)

                         is_published BIT DEFAULT 0,
    -- Cờ đánh dấu bài học đã xuất bản cho học viên hay chưa

                         moderation_status VARCHAR(20) DEFAULT 'PENDING'
                             CHECK (moderation_status IN ('PENDING', 'AUTO_FLAGGED', 'CLEAN', 'APPROVED', 'REJECTED')),
    -- Trạng thái kiểm duyệt video bởi Azure AI:
    -- pending (chờ), auto_flagged (AI phát hiện vấn đề), clean (không vấn đề),
    -- approved (manager phê duyệt), rejected (manager từ chối)

                         created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo bài học

                         updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                         CONSTRAINT FK_lessons_section
                             FOREIGN KEY (section_id) REFERENCES course_sections(id)
);

-- =========================
-- VIDEO MODERATION FLAGS
-- =========================
-- Bảng lưu các dấu hiệu vi phạm được phát hiện bởi Azure AI cho từng video
-- Giups manager dễ dàng review những vị trí nhạy cảm trong video
CREATE TABLE video_moderation_flags (
                                        id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh cờ (flag)

                                        lesson_id INT NOT NULL,
    -- Tham chiếu đến bảng lessons, video bị phát hiện vấn đề

                                        flagged_at_second INT NOT NULL CHECK (flagged_at_second >= 0),
    -- Vị trí giây thứ bao nhiêu trong video bị phát hiện (dùng cho skip trực tiếp)

                                        category VARCHAR(100) NOT NULL,
    -- Thể loại vấn đề: 'violence' (bạo lực), 'nudity' (khỏa thân), 'offensive_language' (từ ngữ kích động), ...

                                        confidence_score DECIMAL(5,2) NOT NULL CHECK (confidence_score BETWEEN 0.00 AND 100.00),
    -- Độ tin cậy của phát hiện Azure AI (0.00-100.00%)

                                        description NVARCHAR(500) NULL,
    -- Mô tả chi tiết về lỗi phát hiện bởi AI

                                        created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian phát hiện
                                        updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                                        CONSTRAINT FK_moderation_flags_lesson
                                            FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);

-- =========================
-- LESSON MATERIALS
-- =========================
CREATE TABLE lesson_materials (
                                  id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh tài liệu

                                  instructor_id INT NOT NULL,
    -- Tham chiếu đến bảng users (role='instructor'), giáo viên upload tài liệu

                                  course_id INT NULL,
    -- Tham chiếu đến bảng courses (tài liệu cho cả khóa học nếu có)

                                  lesson_id INT NULL,
    -- Tham chiếu đến bảng lessons (tài liệu cho bài học cụ thể)

                                  file_name NVARCHAR(255) NULL,
    -- Tên file gốc

                                  file_url VARCHAR(500) NULL,
    -- URL file (lưu link từ Azure Blob Storage)

                                  file_type VARCHAR(50) NULL,
    -- Loại file: 'pdf', 'docx', 'pptx', 'zip', 'txt', ...

                                  file_size BIGINT NULL,
    -- Kích thước file (bytes)

                                  created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian upload tài liệu

                                  updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                                  CONSTRAINT FK_materials_instructor
                                      FOREIGN KEY (instructor_id) REFERENCES users(id),
                                  CONSTRAINT FK_materials_course
                                      FOREIGN KEY (course_id) REFERENCES courses(id),
                                  CONSTRAINT FK_materials_lesson
                                      FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);

-- =========================
-- QUIZZES
-- =========================
CREATE TABLE quizzes (
                         id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh bài quiz

                         lesson_id INT NOT NULL,
    -- Tham chiếu đến bảng lessons, quiz nằm trong bài học nào

                         title NVARCHAR(255) NOT NULL,
    -- Tên bài quiz (ví dụ: 'Quiz: Kiểu dữ liệu và biến')

                         pass_score_percent  INT NOT NULL CHECK (pass_score_percent  >= 0),
    -- Phần trăm Điểm tối thiểu để pass quiz (ví dụ: 70)

                         created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo quiz

                         updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                         CONSTRAINT FK_quizzes_lesson
                             FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);

-- =========================
-- QUIZ QUESTIONS
-- =========================
CREATE TABLE quiz_questions (
                                id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh câu hỏi

                                quiz_id INT NOT NULL,
    -- Tham chiếu đến bảng quizzes, câu hỏi này thuộc quiz nào

                                question_text NVARCHAR(MAX) NOT NULL,
    -- Nội dung câu hỏi

                                question_type VARCHAR(20)
                                    CHECK (question_type IN ('SINGLE', 'MULTIPLE')),
    -- Loại câu hỏi: single (1 đáp án đúng), multiple (nhiều đáp án đúng)

                                points INT DEFAULT 1,
    -- Điểm thưởng nếu trả lời đúng (mặc định 1)

                                position INT NULL,
    -- Thứ tự câu hỏi trong quiz

                                created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo câu hỏi

                                updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                                CONSTRAINT FK_questions_quiz
                                    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);

-- =========================
-- QUIZ ANSWERS
-- =========================
CREATE TABLE quiz_answers (
                              id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh đáp án

                              question_id INT NOT NULL,
    -- Tham chiếu đến bảng quiz_questions, đáp án thuộc câu hỏi nào

                              answer_text NVARCHAR(MAX) NOT NULL,
    -- Nội dung đáp án

                              is_correct BIT DEFAULT 0,
    -- Cờ đánh dấu đáp án đúng hay sai (1 = đúng, 0 = sai)

                              created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo đáp án
                              updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                              CONSTRAINT FK_answers_question
                                  FOREIGN KEY (question_id) REFERENCES quiz_questions(id)
);

-- =========================
-- QUIZ ATTEMPTS
-- =========================
CREATE TABLE quiz_attempts (
                               id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh lần làm quiz

                               user_id INT NOT NULL,
    -- Tham chiếu đến bảng users, học viên làm quiz

                               quiz_id INT NOT NULL,
    -- Tham chiếu đến bảng quizzes, quiz nào được làm

                               score DECIMAL(5,2) NULL,
    -- Điểm đạt được (NULL nếu còn đang làm)

                               is_passed BIT NULL,
    -- Cờ đánh dấu pass hay fail (NULL nếu chưa submit, 1 = pass, 0 = fail)

                               started_at DATETIME DEFAULT GETDATE(),
    -- Thời gian bắt đầu làm quiz
                               submitted_at DATETIME NULL,
    -- Thời gian submit quiz (NULL nếu chưa submit)
                               created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo bản ghi
                               updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                               CONSTRAINT FK_attempts_user
                                   FOREIGN KEY (user_id) REFERENCES users(id),
                               CONSTRAINT FK_attempts_quiz
                                   FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);

-- =========================
-- COUPONS
-- =========================
CREATE TABLE coupons (
                         id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh mã giảm giá

                         instructor_id INT NOT NULL,
    -- Tham chiếu đến bảng users (role='instructor'), giáo viên tạo coupon cho khóa học của họ

                         code VARCHAR(100) UNIQUE NOT NULL,
    -- Mã code coupon (ví dụ: 'SUMMER50', 'NEWYEAR2024'), phải unique

                         discount_type VARCHAR(20)
                             CHECK (discount_type IN ('PERCENT', 'FIXED')),
    -- Loại giảm giá: percent (giảm theo %), fixed (giảm số tiền cố định)

                         discount_value DECIMAL(10,2) NOT NULL CHECK (discount_value > 0),
    -- Giá trị giảm (ví dụ: 50 cho percent, 100000 cho fixed)

                         usage_limit INT NULL CHECK (usage_limit >= 1),
    -- Giới hạn số lần sử dụng coupon (NULL = không giới hạn)

                         used_count INT DEFAULT 0 CHECK (used_count >= 0),
    -- Số lần coupon đã được sử dụng

                         expired_at DATETIME NULL,
    -- Thời gian hết hiệu lực coupon (NULL = không có hạn)

                         status VARCHAR(20)
                             CHECK (status IN ('ACTIVE', 'INACTIVE')),
    -- Trạng thái: active (đang hoạt động), inactive (không hoạt động)

                         created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo coupon

                         updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                         CONSTRAINT FK_coupons_instructor
                             FOREIGN KEY (instructor_id) REFERENCES users(id)
);

-- =========================
-- CARTS
-- =========================
CREATE TABLE carts (
                       id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh giỏ hàng

                       user_id INT UNIQUE NOT NULL,
    -- Tham chiếu đến bảng users, mỗi user có 1 giỏ hàng duy nhất

                       created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo giỏ hàng

                       updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                       CONSTRAINT FK_carts_user
                           FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =========================
-- CART ITEMS
-- =========================
CREATE TABLE cart_items (
                            id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh item trong giỏ

                            cart_id INT NOT NULL,
    -- Tham chiếu đến bảng carts, item này thuộc giỏ nào

                            course_id INT NOT NULL,
    -- Tham chiếu đến bảng courses, khóa học nào được thêm vào giỏ

                            selected BIT NOT NULL DEFAULT 1,
    -- Trạng thái chọn thanh toán của khóa học (1 = true, 0 = false)

                            created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo bản ghi
                            updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                            CONSTRAINT FK_cart_items_cart
                                FOREIGN KEY (cart_id) REFERENCES carts(id),

                            CONSTRAINT FK_cart_items_course
                                FOREIGN KEY (course_id) REFERENCES courses(id),

    -- Constraint unique để tránh thêm cùng khóa học 2 lần vào giỏ
                            CONSTRAINT UQ_cart_course UNIQUE(cart_id, course_id)
);

-- =========================
-- CART INSTRUCTOR COUPONS
-- =========================
CREATE TABLE cart_instructor_coupons (
                                         id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh mối quan hệ

                                         cart_id INT NOT NULL,
    -- Tham chiếu đến bảng carts, giỏ nào áp dụng coupon

                                         instructor_id INT NOT NULL,
    -- Tham chiếu đến bảng users, coupon của giáo viên nào

                                         coupon_id INT NOT NULL,
    -- Tham chiếu đến bảng coupons, coupon nào được áp dụng

                                         created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo bản ghi
                                         updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                                         CONSTRAINT FK_cart_coupon_cart
                                             FOREIGN KEY (cart_id) REFERENCES carts(id),

                                         CONSTRAINT FK_cart_coupon_instructor
                                             FOREIGN KEY (instructor_id) REFERENCES users(id),

                                         CONSTRAINT FK_cart_coupon_coupon
                                             FOREIGN KEY (coupon_id) REFERENCES coupons(id),

    -- Constraint unique để tránh áp dụng coupon cùng giáo viên 2 lần
                                         CONSTRAINT UQ_cart_instructor UNIQUE(cart_id, instructor_id)
);


-- =========================
-- ORDERS
-- =========================
CREATE TABLE orders (
                        id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh đơn hàng

                        user_id INT NOT NULL,
    -- Tham chiếu đến bảng users, học viên tạo đơn hàng

                        total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    -- Tổng tiền trước khi giảm giá

                        discount_amount DECIMAL(10,2) DEFAULT 0 CHECK (discount_amount >= 0),
    -- Tổng tiền giảm từ tất cả coupons

                        status VARCHAR(20)
                            CHECK (status IN ('PENDING', 'PAID', 'COMPLETED', 'CANCELLED', 'EXPIRED')),
    -- Trạng thái đơn hàng:
    -- pending (chờ thanh toán), paid (đã thanh toán), completed (hoàn tất),
    -- cancelled (hủy), expired (hết hạn thanh toán)

                        payment_method VARCHAR(50) NULL,
    -- Phương thức thanh toán (ví dụ: 'MOMO', 'VNPAY', 'CARD')

                        created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo đơn hàng

                        updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                        CONSTRAINT FK_orders_user
                            FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =========================
-- ORDER ITEMS
-- =========================
CREATE TABLE order_items (
                             id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh item trong đơn hàng

                             order_id INT NOT NULL,
    -- Tham chiếu đến bảng orders, item này thuộc đơn hàng nào

                             course_id INT NOT NULL,
    -- Tham chiếu đến bảng courses, khóa học nào được thanh toán

                             coupon_id INT NULL,
    -- Tham chiếu đến bảng coupons, coupon được áp dụng cho item này (nếu có)

                             price_snapshot DECIMAL(10,2) NOT NULL CHECK (price_snapshot >= 0),
    -- Giá gốc khóa học tại thời điểm tạo đơn (snapshot)

                             discount_amount DECIMAL(10,2) DEFAULT 0 CHECK (discount_amount >= 0),
    -- Tiền giảm từ coupon (nếu có)

                             final_price DECIMAL(10,2) NOT NULL CHECK (final_price >= 0),
    -- Giá cuối cùng = price_snapshot - discount_amount

                             course_title_snapshot NVARCHAR(255) NULL,
    -- Tên khóa học tại thời điểm tạo đơn (snapshot, dùng cho lịch sử)

                             created_at DATETIME DEFAULT GETDATE(),


                             updated_at DATETIME NULL,

                             CONSTRAINT FK_order_items_order
                                 FOREIGN KEY (order_id) REFERENCES orders(id),
                             CONSTRAINT FK_order_items_course
                                 FOREIGN KEY (course_id) REFERENCES courses(id),
                             CONSTRAINT FK_order_items_coupon
                                 FOREIGN KEY (coupon_id) REFERENCES coupons(id)
);

-- =========================
-- PAYMENTS
-- =========================
CREATE TABLE payments (
                          id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh ghi nhận thanh toán

                          order_id INT NOT NULL,
    -- Tham chiếu đến bảng orders, thanh toán cho đơn hàng nào

                          transaction_code VARCHAR(255) NULL,
    -- Mã giao dịch nội bộ

                          gateway VARCHAR(50) NULL,
    -- Cổng thanh toán: 'MOMO', 'VNPAY', 'CARD', ...

                          gateway_tx_id VARCHAR(255) NULL,
    -- Transaction ID từ gateway MOMO, VNPAY, ... (để trace)

                          amount DECIMAL(10,2) NOT NULL CHECK (amount >= 0),
    -- Số tiền thanh toán

                          qr_code_url VARCHAR(500) NULL,
    -- URL mã QR động từ MOMO (để check QR trên điện thoại)

                          status VARCHAR(20)
                              CHECK (status IN ('SUCCESS', 'FAILED', 'PENDING')),
    -- Trạng thái thanh toán: success (thành công), failed (thất bại), pending (chờ xác nhận)

                          paid_at DATETIME NULL,
    -- Thời gian thanh toán thành công

                          created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo bản ghi thanh toán

                          updated_at DATETIME NULL,

                          CONSTRAINT FK_payments_order
                              FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- =========================
-- COUPON USAGES
-- =========================
CREATE TABLE coupon_usages (
                               id INT PRIMARY KEY IDENTITY(1,1),
    -- Mã định danh lần sử dụng coupon

                               coupon_id INT NOT NULL,
    -- Tham chiếu đến bảng coupons, coupon nào được sử dụng

                               user_id INT NOT NULL,
    -- Tham chiếu đến bảng users, người dùng nào sử dụng

                               order_id INT NOT NULL,
                               discount_amount DECIMAL(10,2) NOT NULL,

                               used_at DATETIME NULL,

                               created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo bản ghi
                               updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                               CONSTRAINT FK_coupon_usages_coupon
                                   FOREIGN KEY (coupon_id) REFERENCES coupons(id),
                               CONSTRAINT FK_coupon_usages_user
                                   FOREIGN KEY (user_id) REFERENCES users(id),
                               CONSTRAINT FK_coupon_usages_order
                                   FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- =========================
-- ENROLLMENTS
-- =========================
CREATE TABLE enrollments (
                             id INT PRIMARY KEY IDENTITY(1,1),
                             user_id INT NOT NULL,
                             course_id INT NOT NULL,
                             progress_percent DECIMAL(5,2) DEFAULT 0,
                             completed_at DATETIME NULL,
                             created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo bản ghi
                             updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                             CONSTRAINT UQ_enrollment UNIQUE (user_id, course_id),
                             CONSTRAINT FK_enrollments_user
                                 FOREIGN KEY (user_id) REFERENCES users(id),
                             CONSTRAINT FK_enrollments_course
                                 FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- =========================
-- LESSON PROGRESS
-- =========================
CREATE TABLE lesson_progress (
                                 id INT PRIMARY KEY IDENTITY(1,1),
                                 enrollment_id INT NOT NULL,
                                 lesson_id INT NOT NULL,
                                 is_completed BIT DEFAULT 0,
                                 last_accessed DATETIME NULL,
                                 created_at DATETIME DEFAULT GETDATE(),
    -- Thời gian tạo bản ghi
                                 updated_at DATETIME NULL,
    -- Thời gian cập nhật gần nhất

                                 CONSTRAINT UQ_lesson_progress UNIQUE (enrollment_id, lesson_id),
                                 CONSTRAINT FK_lesson_progress_enrollment
                                     FOREIGN KEY (enrollment_id) REFERENCES enrollments(id),
                                 CONSTRAINT FK_lesson_progress_lesson
                                     FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);

-- =========================
-- FEEDBACKS
-- =========================
CREATE TABLE feedbacks (
                           id INT PRIMARY KEY IDENTITY(1,1),
                           user_id INT NOT NULL,
                           course_id INT NOT NULL,
                           rating INT CHECK (rating BETWEEN 1 AND 5),
                           comment NVARCHAR(MAX) NULL,
                           status VARCHAR(20)
                               CHECK (status IN ('VISIBLE', 'HIDDEN', 'VIOLATION')),
                           created_at DATETIME DEFAULT GETDATE(),
                           updated_at DATETIME NULL,

                           CONSTRAINT FK_feedbacks_user
                               FOREIGN KEY (user_id) REFERENCES users(id),
                           CONSTRAINT FK_feedbacks_course
                               FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- =========================
-- FEEDBACK REPORTS
-- =========================
CREATE TABLE feedback_reports (
                                  id INT PRIMARY KEY IDENTITY(1,1),
                                  feedback_id INT NOT NULL,
                                  reporter_id INT NOT NULL,
                                  reason NVARCHAR(MAX) NULL,
                                  status VARCHAR(20)
                                      CHECK (status IN ('PENDING', 'RESOLVED')),
                                  resolved_by INT NULL,
                                  created_at DATETIME DEFAULT GETDATE(),
                                  updated_at DATETIME NULL,

                                  CONSTRAINT FK_reports_feedback
                                      FOREIGN KEY (feedback_id) REFERENCES feedbacks(id),
                                  CONSTRAINT FK_reports_reporter
                                      FOREIGN KEY (reporter_id) REFERENCES users(id),
                                  CONSTRAINT FK_reports_resolved_by
                                      FOREIGN KEY (resolved_by) REFERENCES users(id)
);

-- ==========================================
-- PERFORMANCE INDEXES (MỚI - TỐI ƯU HÓA TRUY VẤN)
-- ==========================================
CREATE INDEX IX_courses_instructor ON courses(instructor_id);
CREATE INDEX IX_courses_category ON courses(category_id);
CREATE INDEX IX_course_sections_course ON course_sections(course_id);
CREATE INDEX IX_lessons_section ON lessons(section_id);
CREATE INDEX IX_quiz_attempts_user_quiz ON quiz_attempts(user_id, quiz_id);
CREATE INDEX IX_lesson_progress_lookup ON lesson_progress(enrollment_id, lesson_id);
CREATE INDEX IX_video_moderation_lookup ON video_moderation_flags(lesson_id); -- Tối ưu hiển thị dòng thời gian nhạy cảm của video cho Manager
CREATE INDEX IX_orders_user ON orders(user_id);
CREATE INDEX IX_order_items_order ON order_items(order_id);
CREATE INDEX IX_payments_order ON payments(order_id);
CREATE INDEX IX_enrollments_lookup ON enrollments(user_id, course_id);
GO

-- =========================
-- INSTRUCTOR USER (SAMPLE)
-- =========================
-- Tạo một giáo viên mẫu (role_id = 3 : instructor)
INSERT INTO users (
    first_name,
    last_name,
    email,
    phone,
    password_hash,
    status
)
VALUES (
    N'28',
    N'Tech',
    '28tech@gmail.com',
    '0909999999',
    '123456',
    'ACTIVE'
);
INSERT INTO user_roles (user_id, role_id) VALUES (SCOPE_IDENTITY(), 3);

-- =========================
-- CATEGORY
-- =========================
-- Đăng ký các danh mục cha trước (parent_id = NULL)
INSERT INTO categories (name, description, parent_id, status) VALUES (N'Lập trình Front-End', N'Các khóa học lập trình Front-End', NULL, 'ACTIVE'); -- ID = 1
INSERT INTO categories (name, description, parent_id, status) VALUES (N'Lập trình Back-End', N'Các khóa học lập trình Back-End', NULL, 'ACTIVE'); -- ID = 2
INSERT INTO categories (name, description, parent_id, status) VALUES (N'Lập trình iOS', N'Các khóa học lập trình iOS', NULL, 'ACTIVE'); -- ID = 3

-- Đăng ký các danh mục con (parent_id trỏ về ID cha tương ứng)
INSERT INTO categories (name, description, parent_id, status) VALUES (N'HTML', N'HTML5 cơ bản và nâng cao', 1, 'ACTIVE'); -- ID = 4
INSERT INTO categories (name, description, parent_id, status) VALUES (N'CSS', N'CSS3, Flexbox, Grid, Responsive', 1, 'ACTIVE'); -- ID = 5
INSERT INTO categories (name, description, parent_id, status) VALUES (N'React', N'ReactJS Component, Hooks, Redux', 1, 'ACTIVE'); -- ID = 6
INSERT INTO categories (name, description, parent_id, status) VALUES (N'Node.js', N'Backend với Express, Node.js', 2, 'ACTIVE'); -- ID = 7
INSERT INTO categories (name, description, parent_id, status) VALUES (N'Python', N'Lập trình Python từ cơ bản đến nâng cao', 2, 'ACTIVE'); -- ID = 8
INSERT INTO categories (name, description, parent_id, status) VALUES (N'Java', N'Lập trình Java core và nâng cao', 2, 'ACTIVE'); -- ID = 9
INSERT INTO categories (name, description, parent_id, status) VALUES (N'Swift', N'Ngôn ngữ lập trình Swift', 3, 'ACTIVE'); -- ID = 10
INSERT INTO categories (name, description, parent_id, status) VALUES (N'SwiftUI', N'Thiết kế giao diện SwiftUI', 3, 'ACTIVE'); -- ID = 11

-- =========================
-- COURSE
-- =========================
INSERT INTO courses (
    instructor_id,
    category_id,
    title,
    description,
    thumbnail_url,
    price,
    level,
    status
)
VALUES (
    1,
    9, -- Java (ID = 9)
    N'Lập Trình C Cơ Bản - 28Tech',
    N'Khóa học lập trình C cơ bản',
    '2aOboQZWp6Ov5iGTAZLOlCgmiOhOKsGgeQU1cI0O.jpg',
    0.00,
    'BEGINNER',
    'PUBLISHED'
);

-- =========================
-- SECTION
-- =========================
INSERT INTO course_sections (
    course_id,
    title,
    position
)
VALUES (
    1,
    N'Giới thiệu',
    1
);

-- =========================
-- LESSON
-- =========================
INSERT INTO lessons (
    section_id,
    title,
    video_url,
    duration_seconds,
    position,
    is_published,
    moderation_status
)
VALUES (
    1,
    N'Bài 1 - Giới thiệu ngôn ngữ C',
    'Recording%202026-05-28%20212131.mp4',
    600,
    1,
    1,
    'APPROVED'
);

USE ElearningPlatform;
GO

-- =========================
-- SAMPLE LESSON 2
-- =========================
-- Bài học 2: Kiểu dữ liệu và khai báo biến
INSERT INTO lessons (
    section_id,
    title,
    video_url,
    duration_seconds,
    position,
    is_published,
    moderation_status
)
VALUES (
    1,
    N'Bài 2 - Kiểu dữ liệu và khai báo biến trong C',

    'Recording%202026-05-28%20212131.mp4',

    900,
    2,
    1,
    'APPROVED'
);

DECLARE @Lesson2Id INT = SCOPE_IDENTITY();

-- ==========
-- QUIZ 2
-- ==========
-- Quiz cho bài học 2: Kiểu dữ liệu và biến
INSERT INTO quizzes (
    lesson_id,
    title,
    pass_score_percent 
)
VALUES (
    @Lesson2Id,
    N'Quiz - Kiểu dữ liệu và biến',
    70
);

DECLARE @Quiz2Id INT = SCOPE_IDENTITY();

-- ==========
-- QUIZ 2 - QUESTION 1
-- ==========
-- Câu hỏi 1: Kiểu dữ liệu cho số nguyên
INSERT INTO quiz_questions (
    quiz_id,
    question_text,
    question_type,
    points,
    position
)
VALUES (
    @Quiz2Id,
    N'Kiểu dữ liệu nào dùng để lưu số nguyên trong C?',
    'SINGLE',
    1,
    1
);

DECLARE @Q1Id INT = SCOPE_IDENTITY();

-- Đáp án cho Q1
INSERT INTO quiz_answers (
    question_id,
    answer_text,
    is_correct
)
VALUES
(@Q1Id, N'int', 1),
(@Q1Id, N'float', 0),
(@Q1Id, N'double', 0),
(@Q1Id, N'char', 0);

-- ==========
-- QUIZ 2 - QUESTION 2
-- ==========
-- Câu hỏi 2: Từ khóa cho số thực
INSERT INTO quiz_questions (
    quiz_id,
    question_text,
    question_type,
    points,
    position
)
VALUES (
    @Quiz2Id,
    N'Từ khóa nào dùng để khai báo biến số thực?',
    'SINGLE',
    1,
    2
);

DECLARE @Q2Id INT = SCOPE_IDENTITY();

-- Đáp án cho Q2
INSERT INTO quiz_answers (
    question_id,
    answer_text,
    is_correct
)
VALUES
(@Q2Id, N'float', 1),
(@Q2Id, N'int', 0),
(@Q2Id, N'char', 0),
(@Q2Id, N'void', 0);

-- =========================
-- SAMPLE LESSON 3
-- =========================
-- Bài học 3: Xuất dữ liệu với printf
INSERT INTO lessons (
    section_id,
    title,
    video_url,
    duration_seconds,
    position,
    is_published,
    moderation_status
)
VALUES (
    1,
    N'Bài 3 - Xuất dữ liệu với printf',

    'Recording%202026-05-28%20212131.mp4',

    850,
    3,
    1,
    'APPROVED'
);

DECLARE @Lesson3Id INT = SCOPE_IDENTITY();

-- ==========
-- QUIZ 3
-- ==========
-- Quiz cho bài học 3: Hàm printf
INSERT INTO quizzes (
    lesson_id,
    title,
    pass_score_percent 
)
VALUES (
    @Lesson3Id,
    N'Quiz - Hàm printf',
    70
);

DECLARE @Quiz3Id INT = SCOPE_IDENTITY();

-- ==========
-- QUIZ 3 - QUESTION 1
-- ==========
-- Câu hỏi 1: Hàm xuất dữ liệu
INSERT INTO quiz_questions (
    quiz_id,
    question_text,
    question_type,
    points,
    position
)
VALUES (
    @Quiz3Id,
    N'Hàm nào dùng để xuất dữ liệu ra màn hình?',
    'SINGLE',
    1,
    1
);

DECLARE @Q3Id INT = SCOPE_IDENTITY();

INSERT INTO quiz_answers (
    question_id,
    answer_text,
    is_correct
)
VALUES
(@Q3Id, N'printf', 1),
(@Q3Id, N'scanf', 0),
(@Q3Id, N'gets', 0),
(@Q3Id, N'cin', 0);

-- ==========
-- QUIZ 3 - QUESTION 2
-- ==========
-- Câu hỏi 2: %d trong printf
INSERT INTO quiz_questions (
    quiz_id,
    question_text,
    question_type,
    points,
    position
)
VALUES (
    @Quiz3Id,
    N'%d trong printf dùng để in kiểu dữ liệu nào?',
    'SINGLE',
    1,
    2
);

DECLARE @Q4Id INT = SCOPE_IDENTITY();

INSERT INTO quiz_answers (
    question_id,
    answer_text,
    is_correct
)
VALUES
(@Q4Id, N'Số nguyên', 1),
(@Q4Id, N'Số thực', 0),
(@Q4Id, N'Ký tự', 0),
(@Q4Id, N'Chuỗi', 0);

-- =========================
-- SAMPLE LESSON 4
-- =========================
-- Bài học 4: Nhập dữ liệu với scanf
INSERT INTO lessons (
    section_id,
    title,
    video_url,
    duration_seconds,
    position,
    is_published,
    moderation_status
)
VALUES (
    1,
    N'Bài 4 - Nhập dữ liệu với scanf',

    'Recording%202026-05-28%20212131.mp4',

    920,
    4,
    1,
    'APPROVED'
);

DECLARE @Lesson4Id INT = SCOPE_IDENTITY();

-- ==========
-- QUIZ 4
-- ==========
-- Quiz cho bài học 4: Hàm scanf
INSERT INTO quizzes (
    lesson_id,
    title,
    pass_score_percent 
)
VALUES (
    @Lesson4Id,
    N'Quiz - Hàm scanf',
    70
);

DECLARE @Quiz4Id INT = SCOPE_IDENTITY();

-- ==========
-- QUIZ 4 - QUESTION 1
-- ==========
-- Câu hỏi 1: Hàm nhập dữ liệu
INSERT INTO quiz_questions (
    quiz_id,
    question_text,
    question_type,
    points,
    position
)
VALUES (
    @Quiz4Id,
    N'Hàm nào dùng để nhập dữ liệu từ bàn phím?',
    'SINGLE',
    1,
    1
);

DECLARE @Q5Id INT = SCOPE_IDENTITY();

INSERT INTO quiz_answers (
    question_id,
    answer_text,
    is_correct
)
VALUES
(@Q5Id, N'scanf', 1),
(@Q5Id, N'printf', 0),
(@Q5Id, N'puts', 0),
(@Q5Id, N'cout', 0);

-- ==========
-- QUIZ 4 - QUESTION 2
-- ==========
-- Câu hỏi 2: Dấu & trong scanf
INSERT INTO quiz_questions (
    quiz_id,
    question_text,
    question_type,
    points,
    position
)
VALUES (
    @Quiz4Id,
    N'Dấu & trong scanf có tác dụng gì?',
    'SINGLE',
    1,
    2
);

DECLARE @Q6Id INT = SCOPE_IDENTITY();

INSERT INTO quiz_answers (
    question_id,
    answer_text,
    is_correct
)
VALUES
(@Q6Id, N'Lấy địa chỉ biến', 1),
(@Q6Id, N'Kết thúc lệnh', 0),
(@Q6Id, N'Nối chuỗi', 0),
(@Q6Id, N'Xuất dữ liệu', 0);

-- =========================
-- EXTRA SECTIONS FOR COURSE ID = 1
-- =========================
DECLARE @Section2Id INT;
INSERT INTO course_sections (course_id, title, position)
VALUES (1, N'Câu lệnh điều kiện và vòng lặp', 2);
SET @Section2Id = SCOPE_IDENTITY();

INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status)
VALUES (@Section2Id, N'Bài 5 - Câu lệnh if else', 'Recording%202026-05-28%20212131.mp4', 780, 1, 1, 'APPROVED');
DECLARE @Lesson5Id INT = SCOPE_IDENTITY();
INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@Lesson5Id, N'Quiz - Câu lệnh if else', 70);
DECLARE @Quiz5Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz5Id, N'Câu lệnh nào dùng để rẽ nhánh trong C?', 'SINGLE', 1, 1);
DECLARE @Q51Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q51Id, N'if else', 1), (@Q51Id, N'switch case', 0), (@Q51Id, N'for', 0), (@Q51Id, N'while', 0);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz5Id, N'Điều kiện trong if thường có kiểu dữ liệu gì?', 'SINGLE', 1, 2);
DECLARE @Q52Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q52Id, N'Boolean', 1), (@Q52Id, N'String', 0), (@Q52Id, N'Char', 0), (@Q52Id, N'Float', 0);

INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status)
VALUES (@Section2Id, N'Bài 6 - Câu lệnh switch case', 'Recording%202026-05-28%20212131.mp4', 840, 2, 1, 'APPROVED');
DECLARE @Lesson6Id INT = SCOPE_IDENTITY();
INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@Lesson6Id, N'Quiz - Câu lệnh switch case', 70);
DECLARE @Quiz6Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz6Id, N'Switch case thường dùng để làm gì?', 'SINGLE', 1, 1);
DECLARE @Q61Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q61Id, N'Chọn một nhánh theo giá trị', 1), (@Q61Id, N'Lặp vô hạn', 0), (@Q61Id, N'Khai báo biến', 0), (@Q61Id, N'Tạo hàm', 0);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz6Id, N'Từ khóa nào dùng để thoát khỏi case?', 'SINGLE', 1, 2);
DECLARE @Q62Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q62Id, N'break', 1), (@Q62Id, N'continue', 0), (@Q62Id, N'return', 0), (@Q62Id, N'exit', 0);

INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status)
VALUES (@Section2Id, N'Bài 7 - Vòng lặp for và while', 'Recording%202026-05-28%20212131.mp4', 900, 3, 1, 'APPROVED');
DECLARE @Lesson7Id INT = SCOPE_IDENTITY();
INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@Lesson7Id, N'Quiz - Vòng lặp for và while', 70);
DECLARE @Quiz7Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz7Id, N'Vòng lặp nào thường biết trước số lần lặp?', 'SINGLE', 1, 1);
DECLARE @Q71Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q71Id, N'for', 1), (@Q71Id, N'while', 0), (@Q71Id, N'do while', 0), (@Q71Id, N'switch', 0);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz7Id, N'Câu lệnh nào kiểm tra điều kiện trước khi lặp?', 'SINGLE', 1, 2);
DECLARE @Q72Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q72Id, N'while', 1), (@Q72Id, N'if', 0), (@Q72Id, N'switch', 0), (@Q72Id, N'goto', 0);

DECLARE @Section3Id INT;
INSERT INTO course_sections (course_id, title, position)
VALUES (1, N'Mảng và chuỗi', 3);
SET @Section3Id = SCOPE_IDENTITY();

INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status)
VALUES (@Section3Id, N'Bài 8 - Khai báo và truy cập mảng', 'Recording%202026-05-28%20212131.mp4', 870, 1, 1, 'APPROVED');
DECLARE @Lesson8Id INT = SCOPE_IDENTITY();
INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@Lesson8Id, N'Quiz - Mảng trong C', 70);
DECLARE @Quiz8Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz8Id, N'Chỉ số phần tử đầu tiên của mảng trong C là gì?', 'SINGLE', 1, 1);
DECLARE @Q81Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q81Id, N'0', 1), (@Q81Id, N'1', 0), (@Q81Id, N'-1', 0), (@Q81Id, N'Kích thước mảng', 0);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz8Id, N'Mảng trong C có đặc điểm nào?', 'SINGLE', 1, 2);
DECLARE @Q82Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q82Id, N'Lưu các phần tử cùng kiểu dữ liệu', 1), (@Q82Id, N'Lưu mọi kiểu dữ liệu', 0), (@Q82Id, N'Không có kích thước', 0), (@Q82Id, N'Chỉ lưu chuỗi', 0);

INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status)
VALUES (@Section3Id, N'Bài 9 - Duyệt mảng với vòng lặp', 'Recording%202026-05-28%20212131.mp4', 930, 2, 1, 'APPROVED');
DECLARE @Lesson9Id INT = SCOPE_IDENTITY();
INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@Lesson9Id, N'Quiz - Duyệt mảng', 70);
DECLARE @Quiz9Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz9Id, N'Vòng lặp nào thường dùng để duyệt mảng?', 'SINGLE', 1, 1);
DECLARE @Q91Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q91Id, N'for', 1), (@Q91Id, N'switch', 0), (@Q91Id, N'goto', 0), (@Q91Id, N'break', 0);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz9Id, N'Độ dài mảng tĩnh trong C được xác định khi nào?', 'SINGLE', 1, 2);
DECLARE @Q92Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q92Id, N'Khi khai báo', 1), (@Q92Id, N'Khi chạy chương trình', 0), (@Q92Id, N'Khi in ra', 0), (@Q92Id, N'Khi kết thúc', 0);

INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status)
VALUES (@Section3Id, N'Bài 10 - Xử lý chuỗi trong C', 'Recording%202026-05-28%20212131.mp4', 960, 3, 1, 'APPROVED');
DECLARE @Lesson10Id INT = SCOPE_IDENTITY();
INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@Lesson10Id, N'Quiz - Xử lý chuỗi', 70);
DECLARE @Quiz10Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz10Id, N'Chuỗi trong C kết thúc bằng ký tự nào?', 'SINGLE', 1, 1);
DECLARE @Q101Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q101Id, N'\\0', 1), (@Q101Id, N'\n', 0), (@Q101Id, N'space', 0), (@Q101Id, N'#', 0);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz10Id, N'Hàm nào thường dùng để đo độ dài chuỗi?', 'SINGLE', 1, 2);
DECLARE @Q102Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q102Id, N'strlen', 1), (@Q102Id, N'strcpy', 0), (@Q102Id, N'printf', 0), (@Q102Id, N'scanf', 0);

DECLARE @Section4Id INT;
INSERT INTO course_sections (course_id, title, position)
VALUES (1, N'Hàm và con trỏ', 4);
SET @Section4Id = SCOPE_IDENTITY();

INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status)
VALUES (@Section4Id, N'Bài 11 - Hàm trong C', 'Recording%202026-05-28%20212131.mp4', 840, 1, 1, 'APPROVED');
DECLARE @Lesson11Id INT = SCOPE_IDENTITY();
INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@Lesson11Id, N'Quiz - Hàm trong C', 70);
DECLARE @Quiz11Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz11Id, N'Mục đích của hàm trong C là gì?', 'SINGLE', 1, 1);
DECLARE @Q111Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q111Id, N'Tái sử dụng và tổ chức code', 1), (@Q111Id, N'Tăng dung lượng RAM', 0), (@Q111Id, N'Xóa biến toàn cục', 0), (@Q111Id, N'Thay thế vòng lặp', 0);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz11Id, N'Từ khóa nào thường dùng để khai báo hàm trả về số nguyên?', 'SINGLE', 1, 2);
DECLARE @Q112Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q112Id, N'int', 1), (@Q112Id, N'void', 0), (@Q112Id, N'char', 0), (@Q112Id, N'float', 0);

INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status)
VALUES (@Section4Id, N'Bài 12 - Tham số và giá trị trả về', 'Recording%202026-05-28%20212131.mp4', 900, 2, 1, 'APPROVED');
DECLARE @Lesson12Id INT = SCOPE_IDENTITY();
INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@Lesson12Id, N'Quiz - Tham số và giá trị trả về', 70);
DECLARE @Quiz12Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz12Id, N'Tham số hàm được truyền vào khi nào?', 'SINGLE', 1, 1);
DECLARE @Q121Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q121Id, N'Khi gọi hàm', 1), (@Q121Id, N'Khi biên dịch', 0), (@Q121Id, N'Khi import file', 0), (@Q121Id, N'Khi kết thúc hàm', 0);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz12Id, N'Giá trị trả về của hàm được khai báo bằng gì?', 'SINGLE', 1, 2);
DECLARE @Q122Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q122Id, N'Kiểu dữ liệu trả về', 1), (@Q122Id, N'Tên hàm', 0), (@Q122Id, N'Số tham số', 0), (@Q122Id, N'Tên biến cục bộ', 0);

INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status)
VALUES (@Section4Id, N'Bài 13 - Con trỏ cơ bản', 'Recording%202026-05-28%20212131.mp4', 980, 3, 1, 'APPROVED');
DECLARE @Lesson13Id INT = SCOPE_IDENTITY();
INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@Lesson13Id, N'Quiz - Con trỏ cơ bản', 70);
DECLARE @Quiz13Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz13Id, N'Con trỏ lưu gì?', 'SINGLE', 1, 1);
DECLARE @Q131Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q131Id, N'Địa chỉ bộ nhớ', 1), (@Q131Id, N'Giá trị chuỗi', 0), (@Q131Id, N'Tên biến', 0), (@Q131Id, N'Kết quả hàm', 0);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@Quiz13Id, N'Toán tử nào dùng để lấy địa chỉ biến?', 'SINGLE', 1, 2);
DECLARE @Q132Id INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES (@Q132Id, N'&', 1), (@Q132Id, N'*', 0), (@Q132Id, N'%', 0), (@Q132Id, N'#', 0);

-- =========================
-- LESSON MATERIALS
-- =========================
INSERT INTO lesson_materials (instructor_id, course_id, lesson_id, file_name, file_url, file_type, created_at)
VALUES
       (1, 1, 1,  '[28Tech] BUOI 1.pdf', '%5B28Tech%5D.%20BUOI%201.pdf', 'pdf', GETDATE()),
       (1, 1, 2,  '[28Tech] BUOI 1.pdf', '%5B28Tech%5D.%20BUOI%201.pdf', 'pdf', GETDATE()),
       (1, 1, 3,  '[28Tech] BUOI 1.pdf', '%5B28Tech%5D.%20BUOI%201.pdf', 'pdf', GETDATE()),
       (1, 1, 4,  '[28Tech] BUOI 1.pdf', '%5B28Tech%5D.%20BUOI%201.pdf', 'pdf', GETDATE()),
       (1, 1, 5,  '[28Tech] BUOI 1.pdf', '%5B28Tech%5D.%20BUOI%201.pdf', 'pdf', GETDATE()),
       (1, 1, 6,  '[28Tech] BUOI 1.pdf', '%5B28Tech%5D.%20BUOI%201.pdf', 'pdf', GETDATE()),
       (1, 1, 7,  '[28Tech] BUOI 1.pdf', '%5B28Tech%5D.%20BUOI%201.pdf', 'pdf', GETDATE()),
       (1, 1, 8,  '[28Tech] BUOI 1.pdf', '%5B28Tech%5D.%20BUOI%201.pdf', 'pdf', GETDATE()),
       (1, 1, 9,  '[28Tech] BUOI 1.pdf', '%5B28Tech%5D.%20BUOI%201.pdf', 'pdf', GETDATE()),
       (1, 1, 10, '[28Tech] BUOI 1.pdf', '%5B28Tech%5D.%20BUOI%201.pdf', 'pdf', GETDATE()),
       (1, 1, 11, '[28Tech] BUOI 1.pdf', '%5B28Tech%5D.%20BUOI%201.pdf', 'pdf', GETDATE()),
       (1, 1, 12, '[28Tech] BUOI 1.pdf', '%5B28Tech%5D.%20BUOI%201.pdf', 'pdf', GETDATE()),
       (1, 1, 13, '[28Tech] BUOI 1.pdf', '%5B28Tech%5D.%20BUOI%201.pdf', 'pdf', GETDATE());

-- =========================
-- SAMPLE DATA COMPLETE
-- =========================
GO

-- =========================
-- ADDITIONAL COURSES (Course 2,3,4) - mirror structure of Course 1
-- =========================

-- COURSE 2
DECLARE @Course2Id INT;
INSERT INTO courses (instructor_id, category_id, title, description, thumbnail_url, price, level, status)
VALUES (1, 9, N'Lập Trình C Nâng Cao - 28Tech', N'Khóa học nâng cao lập trình C', '2aOboQZWp6Ov5iGTAZLOlCgmiOhOKsGgeQU1cI0O.jpg', 350000.00, 'INTERMEDIATE', 'PUBLISHED');
SET @Course2Id = SCOPE_IDENTITY();

-- Sections and lessons for Course 2
DECLARE @C2S1 INT; INSERT INTO course_sections (course_id, title, position) VALUES (@Course2Id, N'Giới thiệu', 1); SET @C2S1 = SCOPE_IDENTITY();
DECLARE @C2L1 INT; INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C2S1, N'Bài 1 - Giới thiệu ngôn ngữ C', 'Recording%202026-05-28%20212131.mp4', 600, 1, 1, 'APPROVED'); SET @C2L1 = SCOPE_IDENTITY();
DECLARE @C2L2 INT; INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C2S1, N'Bài 2 - Kiểu dữ liệu và khai báo biến trong C', 'Recording%202026-05-28%20212131.mp4', 900, 2, 1, 'APPROVED'); SET @C2L2 = SCOPE_IDENTITY();
DECLARE @C2L3 INT; INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C2S1, N'Bài 3 - Xuất dữ liệu với printf', 'Recording%202026-05-28%20212131.mp4', 850, 3, 1, 'APPROVED'); SET @C2L3 = SCOPE_IDENTITY();

-- Seed quizzes for C2 lessons s1
-- For brevity, add one quiz with two simple questions per lesson
INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@C2L1, N'Quiz - Giới thiệu', 70); DECLARE @C2Q1 INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@C2Q1, N'Giới thiệu C chủ yếu dùng cho gì?', 'SINGLE', 1, 1); DECLARE @C2Q11 INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct) VALUES (@C2Q11, N'Lập trình hệ thống', 1), (@C2Q11, N'Trình duyệt', 0), (@C2Q11, N'Office', 0), (@C2Q11, N'Khác', 0);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@C2Q1, N'C có bước biên dịch hay thông dịch?', 'SINGLE', 1, 2); DECLARE @C2Q12 INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct) VALUES (@C2Q12, N'Biên dịch', 1), (@C2Q12, N'Thông dịch', 0), (@C2Q12, N'Cả hai', 0), (@C2Q12, N'Không rõ', 0);

INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@C2L2, N'Quiz - Kiểu dữ liệu', 70); DECLARE @C2Q2 INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@C2Q2, N'Kiểu dữ liệu nào lưu số nguyên?', 'SINGLE', 1, 1); DECLARE @C2Q21 INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct) VALUES (@C2Q21, N'int', 1), (@C2Q21, N'float', 0), (@C2Q21, N'double', 0), (@C2Q21, N'char', 0);
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@C2Q2, N'Kiểu để lưu ký tự?', 'SINGLE', 1, 2); DECLARE @C2Q22 INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct) VALUES (@C2Q22, N'char', 1), (@C2Q22, N'string', 0), (@C2Q22, N'int', 0), (@C2Q22, N'float', 0);

INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@C2L3, N'Quiz - printf', 70); DECLARE @C2Q3 INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@C2Q3, N'Hàm printf dùng để?', 'SINGLE', 1, 1); DECLARE @C2Q31 INT = SCOPE_IDENTITY();
INSERT INTO quiz_answers (question_id, answer_text, is_correct) VALUES (@C2Q31, N'Xuất dữ liệu', 1), (@C2Q31, N'Nhập dữ liệu', 0), (@C2Q31, N'Thực thi', 0), (@C2Q31, N'Khác', 0);

-- create more sections for course 2 (positions 2..4) and lessons/quizzes
DECLARE @C2S2 INT; INSERT INTO course_sections (course_id, title, position) VALUES (@Course2Id, N'Cấu trúc điều khiển', 2); SET @C2S2 = SCOPE_IDENTITY();
DECLARE @C2S3 INT; INSERT INTO course_sections (course_id, title, position) VALUES (@Course2Id, N'Mảng và chuỗi', 3); SET @C2S3 = SCOPE_IDENTITY();
DECLARE @C2S4 INT; INSERT INTO course_sections (course_id, title, position) VALUES (@Course2Id, N'Hàm và con trỏ', 4); SET @C2S4 = SCOPE_IDENTITY();

-- For each of these sections, insert 3 lessons and a quiz (use same video file)
-- Section 2 lessons
DECLARE @c2l4 INT; INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C2S2, N'Bài 5 - Câu lệnh if else', 'Recording%202026-05-28%20212131.mp4', 780, 1, 1, 'APPROVED'); SET @c2l4 = SCOPE_IDENTITY();
DECLARE @c2l5 INT; INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C2S2, N'Bài 6 - Câu lệnh switch case', 'Recording%202026-05-28%20212131.mp4', 840, 2, 1, 'APPROVED'); SET @c2l5 = SCOPE_IDENTITY();
DECLARE @c2l6 INT; INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C2S2, N'Bài 7 - Vòng lặp for và while', 'Recording%202026-05-28%20212131.mp4', 900, 3, 1, 'APPROVED'); SET @c2l6 = SCOPE_IDENTITY();

INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@c2l4, N'Quiz - If else', 70); DECLARE @c2q4 INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@c2q4, N'If else để làm gì?', 'SINGLE', 1, 1); DECLARE @c2q41 INT = SCOPE_IDENTITY(); INSERT INTO quiz_answers (question_id, answer_text, is_correct) VALUES (@c2q41, N'Rẽ nhánh',1),(@c2q41,N'Lặp',0),(@c2q41,N'Khai báo',0),(@c2q41,N'Khác',0);

INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@c2l5, N'Quiz - Switch', 70); DECLARE @c2q5 INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@c2q5, N'Switch dùng để?', 'SINGLE', 1, 1); DECLARE @c2q51 INT = SCOPE_IDENTITY(); INSERT INTO quiz_answers (question_id, answer_text, is_correct) VALUES (@c2q51, N'Chọn nhánh theo giá trị',1),(@c2q51,N'Lặp',0),(@c2q51,N'Khai báo',0),(@c2q51,N'Khác',0);

INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@c2l6, N'Quiz - Vòng lặp', 70); DECLARE @c2q6 INT = SCOPE_IDENTITY();
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@c2q6, N'Vòng lặp for dùng khi?', 'SINGLE', 1, 1); DECLARE @c2q61 INT = SCOPE_IDENTITY(); INSERT INTO quiz_answers (question_id, answer_text, is_correct) VALUES (@c2q61, N'Biết trước số lần lặp',1),(@c2q61,N'Không biết trước',0),(@c2q61,N'Khai báo',0),(@c2q61,N'Khác',0);

-- Section 3 lessons (array/string)
DECLARE @c2l7 INT; INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C2S3, N'Bài 8 - Khai báo mảng', 'Recording%202026-05-28%20212131.mp4', 870, 1, 1, 'APPROVED'); SET @c2l7 = SCOPE_IDENTITY();
DECLARE @c2l8 INT; INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C2S3, N'Bài 9 - Duyệt mảng', 'Recording%202026-05-28%20212131.mp4', 930, 2, 1, 'APPROVED'); SET @c2l8 = SCOPE_IDENTITY();
DECLARE @c2l9 INT; INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C2S3, N'Bài 10 - Chuỗi', 'Recording%202026-05-28%20212131.mp4', 960, 3, 1, 'APPROVED'); SET @c2l9 = SCOPE_IDENTITY();

INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@c2l7, N'Quiz - Mảng', 70); DECLARE @c2q7 INT = SCOPE_IDENTITY(); INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@c2q7, N'Chỉ số bắt đầu của mảng?', 'SINGLE', 1, 1); DECLARE @c2q71 INT = SCOPE_IDENTITY(); INSERT INTO quiz_answers (question_id, answer_text, is_correct) VALUES (@c2q71, N'0',1),(@c2q71,N'1',0),(@c2q71,N'-1',0),(@c2q71,N'Khác',0);

INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@c2l8, N'Quiz - Duyệt mảng', 70); DECLARE @c2q8 INT = SCOPE_IDENTITY(); INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@c2q8, N'Vòng lặp hay dùng để duyệt mảng?', 'SINGLE', 1, 1); DECLARE @c2q81 INT = SCOPE_IDENTITY(); INSERT INTO quiz_answers (question_id, answer_text, is_correct) VALUES (@c2q81, N'for',1),(@c2q81,N'while',0),(@c2q81,N'switch',0),(@c2q81,N'Khác',0);

INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@c2l9, N'Quiz - Chuỗi', 70); DECLARE @c2q9 INT = SCOPE_IDENTITY(); INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@c2q9, N'Chuỗi kết thúc bằng gì?', 'SINGLE', 1, 1); DECLARE @c2q91 INT = SCOPE_IDENTITY(); INSERT INTO quiz_answers (question_id, answer_text, is_correct) VALUES (@c2q91, N'\0',1),(@c2q91,N'\n',0),(@c2q91,N'space',0),(@c2q91,N'Khác',0);

-- Section 4 lessons (functions/pointers)
DECLARE @c2l10 INT; INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C2S4, N'Bài 11 - Hàm', 'Recording%202026-05-28%20212131.mp4', 840, 1, 1, 'APPROVED'); SET @c2l10 = SCOPE_IDENTITY();
DECLARE @c2l11 INT; INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C2S4, N'Bài 12 - Tham số', 'Recording%202026-05-28%20212131.mp4', 900, 2, 1, 'APPROVED'); SET @c2l11 = SCOPE_IDENTITY();
DECLARE @c2l12 INT; INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C2S4, N'Bài 13 - Con trỏ', 'Recording%202026-05-28%20212131.mp4', 980, 3, 1, 'APPROVED'); SET @c2l12 = SCOPE_IDENTITY();

INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@c2l12, N'Quiz - Con trỏ', 70); DECLARE @c2_l12_quiz INT = SCOPE_IDENTITY(); INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position) VALUES (@c2_l12_quiz, N'Con trỏ lưu gì?', 'SINGLE', 1, 1); DECLARE @c2q121 INT = SCOPE_IDENTITY(); INSERT INTO quiz_answers (question_id, answer_text, is_correct) VALUES (@c2q121, N'Địa chỉ bộ nhớ',1),(@c2q121,N'Giá trị',0),(@c2q121,N'Tên biến',0),(@c2q121,N'Khác',0);

-- lesson material for one of course2 lessons
INSERT INTO lesson_materials (instructor_id, course_id, lesson_id, file_name, file_url, file_type, created_at)
VALUES (1, @Course2Id, @c2l12, N'[28Tech] COURSE2_LESSON13.pdf', '%5BCOURSE2%5D.%20LESSON13.pdf', 'pdf', GETDATE());

-- COURSE 3 (mirror structure)
DECLARE @Course3Id INT;
INSERT INTO courses (instructor_id, category_id, title, description, thumbnail_url, price, level, status)
VALUES (1, 9, N'Lập Trình C Thực Hành - 28Tech', N'Bài tập thực hành và project nhỏ với C', '2aOboQZWp6Ov5iGTAZLOlCgmiOhOKsGgeQU1cI0O.jpg', 600000.00, 'BEGINNER', 'PUBLISHED');
SET @Course3Id = SCOPE_IDENTITY();

-- Use same pattern: create 4 sections with 3 lessons each; to save space, reuse Recording video for all lessons
DECLARE @C3S1 INT; INSERT INTO course_sections (course_id, title, position) VALUES (@Course3Id, N'Giới thiệu',1); SET @C3S1 = SCOPE_IDENTITY();
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C3S1, N'Bài 1 - Giới thiệu', 'Recording%202026-05-28%20212131.mp4',600,1,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C3S1, N'Bài 2 - Kiểu dữ liệu', 'Recording%202026-05-28%20212131.mp4',900,2,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C3S1, N'Bài 3 - printf', 'Recording%202026-05-28%20212131.mp4',850,3,1,'APPROVED');
DECLARE @C3S2 INT; INSERT INTO course_sections (course_id, title, position) VALUES (@Course3Id, N'Cấu trúc điều khiển',2); SET @C3S2 = SCOPE_IDENTITY();
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C3S2, N'Bài 4', 'Recording%202026-05-28%20212131.mp4',780,1,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C3S2, N'Bài 5', 'Recording%202026-05-28%20212131.mp4',840,2,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C3S2, N'Bài 6', 'Recording%202026-05-28%20212131.mp4',900,3,1,'APPROVED');
DECLARE @C3S3 INT; INSERT INTO course_sections (course_id, title, position) VALUES (@Course3Id, N'Mảng và chuỗi',3); SET @C3S3 = SCOPE_IDENTITY();
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C3S3, N'Bài 7', 'Recording%202026-05-28%20212131.mp4',870,1,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C3S3, N'Bài 8', 'Recording%202026-05-28%20212131.mp4',930,2,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C3S3, N'Bài 9', 'Recording%202026-05-28%20212131.mp4',960,3,1,'APPROVED');
DECLARE @C3S4 INT; INSERT INTO course_sections (course_id, title, position) VALUES (@Course3Id, N'Hàm và con trỏ',4); SET @C3S4 = SCOPE_IDENTITY();
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C3S4, N'Bài 10', 'Recording%202026-05-28%20212131.mp4',840,1,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C3S4, N'Bài 11', 'Recording%202026-05-28%20212131.mp4',900,2,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C3S4, N'Bài 12', 'Recording%202026-05-28%20212131.mp4',980,3,1,'APPROVED');

INSERT INTO lesson_materials (instructor_id, course_id, lesson_id, file_name, file_url, file_type, created_at) VALUES (1, @Course3Id, (SELECT TOP 1 id FROM lessons WHERE section_id = @C3S4 ORDER BY position DESC), N'[28Tech] COURSE3_LESSON.pdf', '%5BCOURSE3%5D.%20LESSON.pdf', 'pdf', GETDATE());

-- COURSE 4 (mirror structure)
DECLARE @Course4Id INT;
INSERT INTO courses (instructor_id, category_id, title, description, thumbnail_url, price, level, status)
VALUES (1,1,N'Thuật Toán C với 28Tech', N'Giải thuật và cấu trúc dữ liệu cơ bản bằng C','2aOboQZWp6Ov5iGTAZLOlCgmiOhOKsGgeQU1cI0O.jpg',0,'ADVANCED','PUBLISHED');
SET @Course4Id = SCOPE_IDENTITY();

-- create four sections and three lessons each using same recording
DECLARE @C4S1 INT; INSERT INTO course_sections (course_id, title, position) VALUES (@Course4Id, N'Giới thiệu',1); SET @C4S1 = SCOPE_IDENTITY();
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C4S1, N'Bài 1', 'Recording%202026-05-28%20212131.mp4',600,1,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C4S1, N'Bài 2', 'Recording%202026-05-28%20212131.mp4',900,2,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C4S1, N'Bài 3', 'Recording%202026-05-28%20212131.mp4',850,3,1,'APPROVED');
DECLARE @C4S2 INT; INSERT INTO course_sections (course_id, title, position) VALUES (@Course4Id, N'Cơ sở giải thuật',2); SET @C4S2 = SCOPE_IDENTITY();
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C4S2, N'Bài 4', 'Recording%202026-05-28%20212131.mp4',780,1,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C4S2, N'Bài 5', 'Recording%202026-05-28%20212131.mp4',840,2,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C4S2, N'Bài 6', 'Recording%202026-05-28%20212131.mp4',900,3,1,'APPROVED');
DECLARE @C4S3 INT; INSERT INTO course_sections (course_id, title, position) VALUES (@Course4Id, N'Cấu trúc dữ liệu',3); SET @C4S3 = SCOPE_IDENTITY();
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C4S3, N'Bài 7', 'Recording%202026-05-28%20212131.mp4',870,1,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C4S3, N'Bài 8', 'Recording%202026-05-28%20212131.mp4',930,2,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C4S3, N'Bài 9', 'Recording%202026-05-28%20212131.mp4',960,3,1,'APPROVED');
DECLARE @C4S4 INT; INSERT INTO course_sections (course_id, title, position) VALUES (@Course4Id, N'Hàm và tối ưu',4); SET @C4S4 = SCOPE_IDENTITY();
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C4S4, N'Bài 10', 'Recording%202026-05-28%20212131.mp4',840,1,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C4S4, N'Bài 11', 'Recording%202026-05-28%20212131.mp4',900,2,1,'APPROVED');
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status) VALUES (@C4S4, N'Bài 12', 'Recording%202026-05-28%20212131.mp4',980,3,1,'APPROVED');

INSERT INTO lesson_materials (instructor_id, course_id, lesson_id, file_name, file_url, file_type, created_at) VALUES (1, @Course4Id, (SELECT TOP 1 id FROM lessons WHERE section_id = @C4S4 ORDER BY position DESC), N'[28Tech] COURSE4_LESSON.pdf', '%5BCOURSE4%5D.%20LESSON.pdf', 'pdf', GETDATE());

-- =========================
-- ADDITIONAL COURSES (Course 5 -> Course 14)
-- 4 sections/course, 3 lessons/section, quiz per lesson (2 questions)
-- =========================
DECLARE @CourseIndex INT = 5;
DECLARE @NewCourseId INT;
DECLARE @SectionPos INT;
DECLARE @SectionId INT;
DECLARE @SectionTitle NVARCHAR(255);
DECLARE @LessonPos INT;
DECLARE @LessonId INT;
DECLARE @LessonGlobalPos INT;
DECLARE @QuizId INT;
DECLARE @QuestionId1 INT;
DECLARE @QuestionId2 INT;
DECLARE @LastLessonId INT;
DECLARE @Title NVARCHAR(255);
DECLARE @CatId INT;
DECLARE @Price DECIMAL(18,2);

WHILE @CourseIndex <= 14
    BEGIN
        -- Xác định Category & Title dựa trên @CourseIndex
        IF @CourseIndex = 5
        BEGIN
            SET @Title = N'Lập Trình Python Cơ Bản và Nâng Cao';
            SET @CatId = 8; -- Python (con của Lập trình Back-End)
        END
        ELSE IF @CourseIndex = 6
        BEGIN
            SET @Title = N'Xây Dựng Giao Diện Web Với HTML5';
            SET @CatId = 4; -- HTML (con của Lập trình Front-End)
        END
        ELSE IF @CourseIndex = 7
        BEGIN
            SET @Title = N'Làm Chủ CSS Grid Và Flexbox Responsive';
            SET @CatId = 5; -- CSS (con của Lập trình Front-End)
        END
        ELSE IF @CourseIndex = 8
        BEGIN
            SET @Title = N'Lập Trình Frontend Hiện Đại Với ReactJS';
            SET @CatId = 6; -- React (con của Lập trình Front-End)
        END
        ELSE IF @CourseIndex = 9
        BEGIN
            SET @Title = N'Xây Dựng RESTful API Với Node.js';
            SET @CatId = 7; -- Node.js (con của Lập trình Back-End)
        END
        ELSE IF @CourseIndex = 10
        BEGIN
            SET @Title = N'Lập Trình Ứng Dụng iOS Với Ngôn Ngữ Swift';
            SET @CatId = 10; -- Swift (con của Lập trình iOS)
        END
        ELSE IF @CourseIndex = 11
        BEGIN
            SET @Title = N'Thiết Kế Giao Diện iOS Bằng SwiftUI';
            SET @CatId = 11; -- SwiftUI (con của Lập trình iOS)
        END
        ELSE IF @CourseIndex = 12
        BEGIN
            SET @Title = N'Lập Trình Hướng Đối Tượng Java Core';
            SET @CatId = 9; -- Java (con của Lập trình Back-End)
        END
        ELSE IF @CourseIndex = 13
        BEGIN
            SET @Title = N'Phát Triển Ứng Dụng Web Với Java Spring Boot';
            SET @CatId = 9; -- Java (con của Lập trình Back-End)
        END
        ELSE
        BEGIN
            SET @Title = CONCAT(N'Lập Trình C Chuyên Đề ', @CourseIndex, N' - 28Tech');
            SET @CatId = 8; -- Python (con của Lập trình Back-End)
        END

        -- Xác định Price
        IF @CourseIndex = 5
        BEGIN
            SET @Price = 1500000.00;
        END
        ELSE
        BEGIN
            SET @Price = CASE (@CourseIndex % 5)
                WHEN 0 THEN 0.00        -- Miễn phí
                WHEN 1 THEN 250000.00   -- 200k - 500k
                WHEN 2 THEN 550000.00   -- 500k - 700k
                WHEN 3 THEN 800000.00   -- 700k - 1M
                ELSE 1200000.00         -- Trên 1M
            END;
        END

        INSERT INTO courses (instructor_id, category_id, title, description, thumbnail_url, price, level, status)
        VALUES (
                   1,
                   @CatId,
                   @Title,
                   CONCAT(N'Khóa học chuyên đề số ', @CourseIndex),
                   '2aOboQZWp6Ov5iGTAZLOlCgmiOhOKsGgeQU1cI0O.jpg',
                   @Price,
                   CASE
                       WHEN @CourseIndex % 3 = 2 THEN 'BEGINNER'
                       WHEN @CourseIndex % 3 = 0 THEN 'INTERMEDIATE'
                       ELSE 'ADVANCED'
                       END,
                   'PUBLISHED'
               );
        SET @NewCourseId = SCOPE_IDENTITY();

        SET @SectionPos = 1;
        WHILE @SectionPos <= 4
            BEGIN
                SET @SectionTitle = CASE @SectionPos
                                        WHEN 1 THEN N'Giới thiệu'
                                        WHEN 2 THEN N'Cấu trúc điều kiện'
                                        WHEN 3 THEN N'Mảng và chuỗi'
                                        ELSE N'Hàm và con trỏ'
                    END;

                INSERT INTO course_sections (course_id, title, position)
                VALUES (@NewCourseId, @SectionTitle, @SectionPos);
                SET @SectionId = SCOPE_IDENTITY();

                SET @LessonPos = 1;
                WHILE @LessonPos <= 3
                    BEGIN
                        SET @LessonGlobalPos = ((@SectionPos - 1) * 3) + @LessonPos;

                        INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status)
                        VALUES (
                                   @SectionId,
                                   CONCAT(N'Bài ', @LessonGlobalPos, N' - Chuyên đề ', @CourseIndex),
                                   'Recording%202026-05-28%20212131.mp4',
                                   600 + (@LessonGlobalPos * 35),
                                   @LessonPos,
                                   1,
                                   'APPROVED'
                               );
                        SET @LessonId = SCOPE_IDENTITY();

                        INSERT INTO quizzes (lesson_id, title, pass_score_percent )
                        VALUES (@LessonId, CONCAT(N'Quiz - Bài ', @LessonGlobalPos, N' - Khóa ', @CourseIndex), 70);
                        SET @QuizId = SCOPE_IDENTITY();

                        INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position)
                        VALUES (@QuizId, CONCAT(N'Bài ', @LessonGlobalPos, N' thuộc section nào?'), 'SINGLE', 1, 1);
                        SET @QuestionId1 = SCOPE_IDENTITY();
                        INSERT INTO quiz_answers (question_id, answer_text, is_correct)
                        VALUES (@QuestionId1, CONCAT(N'Section ', @SectionPos), 1),
                               (@QuestionId1, N'Section 5', 0),
                               (@QuestionId1, N'Section 6', 0),
                               (@QuestionId1, N'Section 7', 0);

                        INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position)
                        VALUES (@QuizId, CONCAT(N'Khóa học chuyên đề số mấy? (Bài ', @LessonGlobalPos, N')'), 'SINGLE', 1, 2);
                        SET @QuestionId2 = SCOPE_IDENTITY();
                        INSERT INTO quiz_answers (question_id, answer_text, is_correct)
                        VALUES (@QuestionId2, CAST(@CourseIndex AS NVARCHAR(10)), 1),
                               (@QuestionId2, N'1', 0),
                               (@QuestionId2, N'2', 0),
                               (@QuestionId2, N'3', 0);

                        SET @LessonPos = @LessonPos + 1;
                    END

                SET @SectionPos = @SectionPos + 1;
            END

        SET @LastLessonId = NULL;
        SELECT TOP 1 @LastLessonId = l.id
        FROM lessons l
                 INNER JOIN course_sections s ON l.section_id = s.id
        WHERE s.course_id = @NewCourseId
        ORDER BY s.position DESC, l.position DESC;

        INSERT INTO lesson_materials (instructor_id, course_id, lesson_id, file_name, file_url, file_type, created_at)
        VALUES (
                   1,
                   @NewCourseId,
                   @LastLessonId,
                   CONCAT(N'[28Tech] COURSE', @CourseIndex, N'_LESSON.pdf'),
                   CONCAT('%5BCOURSE', @CourseIndex, '%5D.%20LESSON.pdf'),
                   'pdf',
                   GETDATE()
               );

        SET @CourseIndex = @CourseIndex + 1;
    END


-- =========================
-- ADMIN USER
-- =========================
INSERT INTO users (
    first_name,
    last_name,
    email,
    phone,
    password_hash,
    avatar_url,
    status
)
VALUES (
    N'Đặng',
    N'Minh Quân',
    'admin@elearning.com',
    '0901234567',
    'admin123',
    NULL,
    'ACTIVE'
);
INSERT INTO user_roles (user_id, role_id) VALUES (SCOPE_IDENTITY(), 1);

-- ==========================================
-- INSTRUCTOR SAMPLE DATA
-- ==========================================
-------------STEP 1 : register as a Student---------------
INSERT INTO users
(
    first_name,
    last_name,
    email,
    phone,
    password_hash,
    status
)
VALUES
(N'Nguyễn Văn',N'An','nguyenvanan@gmail.com','0900000001','12345678','ACTIVE'),
(N'Trần Minh',N'Bình','tranminhbinh@gmail.com','0900000002','12345678','ACTIVE'),
(N'Lê Quốc',N'Cường','lequoccuong@gmail.com','0900000003','12345678','ACTIVE'),
(N'Phạm Đức',N'Dũng','phamducdung@gmail.com','0900000004','12345678','ACTIVE'),
(N'Hoàng Thu',N'Giang','hoangthugiang@gmail.com','0900000005','12345678','ACTIVE'),
(N'Vũ Thanh',N'Hải','vuthanhhai@gmail.com','0900000006','12345678','ACTIVE'),
(N'Đỗ Khánh',N'Huy','dokhanhhuy@gmail.com','0900000007','12345678','ACTIVE'),
(N'Bùi Anh',N'Khoa','buianhkhoa@gmail.com','0900000008','12345678','ACTIVE'),
(N'Đặng Quang',N'Long','dangquanglong@gmail.com','0900000009','12345678','ACTIVE'),
(N'Phan Minh',N'Nam','phanminhnam@gmail.com','0900000010','12345678','ACTIVE');

INSERT INTO user_roles (user_id, role_id)
SELECT id, 4 FROM users WHERE email IN (
    'nguyenvanan@gmail.com', 'tranminhbinh@gmail.com', 'lequoccuong@gmail.com',
    'phamducdung@gmail.com', 'hoangthugiang@gmail.com', 'vuthanhhai@gmail.com',
    'dokhanhhuy@gmail.com', 'buianhkhoa@gmail.com', 'dangquanglong@gmail.com',
    'phanminhnam@gmail.com'
);

---------STEP 2 : Send request to become a instructor and manager approved
INSERT INTO instructor_requests
(
    user_id,
    cv_url,
    certificate_url,
    description,
    status,
    reviewed_by
)
VALUES
    (1,'https://blob/cv1.pdf','https://blob/cert1.pdf',N'5 năm kinh nghiệm Java Backend','APPROVED',11),
    (2,'https://blob/cv2.pdf','https://blob/cert2.pdf',N'Chuyên gia Spring Boot','APPROVED',11),
    (3,'https://blob/cv3.pdf','https://blob/cert3.pdf',N'Giảng viên SQL Server','APPROVED',11),
    (4,'https://blob/cv4.pdf','https://blob/cert4.pdf',N'Giảng viên ReactJS','APPROVED',11),
    (5,'https://blob/cv5.pdf','https://blob/cert5.pdf',N'Giảng viên Flutter','APPROVED',11),
    (6,'https://blob/cv6.pdf','https://blob/cert6.pdf',N'Chuyên gia Azure Cloud','APPROVED',11),
    (7,'https://blob/cv7.pdf','https://blob/cert7.pdf',N'Giảng viên Python Data Science','APPROVED',11),
    (8,'https://blob/cv8.pdf','https://blob/cert8.pdf',N'Giảng viên DevOps','APPROVED',11),
    (9,'https://blob/cv9.pdf','https://blob/cert9.pdf',N'Giảng viên Machine Learning','APPROVED',11),
    (10,'https://blob/cv10.pdf','https://blob/cert10.pdf',N'Giảng viên UI/UX Design','APPROVED',11);

--Manager cấp tiến hành cấp role
UPDATE user_roles
SET role_id = 3
WHERE user_id BETWEEN 1 AND 10 AND role_id != 3;

UPDATE users
SET updated_at = GETDATE()
WHERE id BETWEEN 1 AND 10;
----*NOTE : coi như có 1 manager có user id là 11 và người này là người duyệt đơn



-- =========================
-- MANAGER USER
-- =========================
INSERT INTO users (
    first_name,
    last_name,
    email,
    phone,
    password_hash,
    avatar_url,
    status
)
VALUES (
    N'Lê',
    N'Thị Mai',
    'manager@elearning.com',
    '0912345678',
    'manager123',
    NULL,
    'ACTIVE'
);
INSERT INTO user_roles (user_id, role_id) VALUES (SCOPE_IDENTITY(), 2);

-- Seed test learner user Do Thanh and enroll into course id=1
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'dothanh2572005@gmail.com')
    BEGIN
        DECLARE @learnerRoleId INT;
        SELECT @learnerRoleId = id FROM roles WHERE name = 'learner';
        IF @learnerRoleId IS NULL
            BEGIN
                INSERT INTO roles (name, description) VALUES ('learner', N'Học viên');
                SET @learnerRoleId = SCOPE_IDENTITY();
            END

        INSERT INTO users (first_name, last_name, email, phone, password_hash, avatar_url, google_id, status)
        VALUES (N'Do', N'Thanh', 'dothanh2572005@gmail.com', NULL, '123', NULL, NULL, 'ACTIVE');
        INSERT INTO user_roles (user_id, role_id) VALUES (SCOPE_IDENTITY(), @learnerRoleId);
    END

-- Enroll user into course id = 1 if course exists and enrollment not present
DECLARE @userId INT;
SELECT @userId = id FROM users WHERE email = 'dothanh2572005@gmail.com';
IF EXISTS (SELECT 1 FROM courses WHERE id = 1)
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM enrollments WHERE user_id = @userId AND course_id = 1)
            BEGIN
                INSERT INTO enrollments (user_id, course_id, progress_percent)
                VALUES (@userId, 1, 0);
            END
    END
GO

-- ============================================================================
-- ADDITIONAL COURSES, SECTIONS, LESSONS, QUIZZES, & COUPONS
-- ============================================================================
USE ElearningPlatform;
GO

-- 1. Insert more courses
INSERT INTO courses (instructor_id, category_id, title, description, thumbnail_url, price, level, status, approved_by, approved_at)
VALUES 
(1, 1, N'Cấu Trúc Dữ Liệu Và Giải Thuật - 28Tech', N'Khóa học cung cấp kiến thức nền tảng về Cấu trúc dữ liệu và Giải thuật sử dụng C/C++.', 'dsa-28tech.jpg', 500000.00, 'INTERMEDIATE', 'PUBLISHED', 3, GETDATE()),
(1, 1, N'Lập Trình Java Web với Spring Boot', N'Khóa học Java Web toàn diện từ Zero đến Hero với Spring Boot, Spring Security, JPA, và Azure.', 'spring-boot.jpg', 1200000.00, 'ADVANCED', 'PUBLISHED', 3, GETDATE());

DECLARE @Course2Id INT = (SELECT id FROM courses WHERE title = N'Cấu Trúc Dữ Liệu Và Giải Thuật - 28Tech');
DECLARE @Course3Id INT = (SELECT id FROM courses WHERE title = N'Lập Trình Java Web với Spring Boot');

-- 2. Insert sections for Course 2 & 3
INSERT INTO course_sections (course_id, title, position)
VALUES 
(@Course2Id, N'Chương 1: Các cấu trúc dữ liệu cơ bản', 1),
(@Course3Id, N'Chương 1: Khởi đầu với Spring Boot', 1);

DECLARE @Section2Id INT = (SELECT id FROM course_sections WHERE course_id = @Course2Id AND title = N'Chương 1: Các cấu trúc dữ liệu cơ bản');
DECLARE @Section3Id INT = (SELECT id FROM course_sections WHERE course_id = @Course3Id AND title = N'Chương 1: Khởi đầu với Spring Boot');

-- 3. Insert lessons for Course 2 & 3
INSERT INTO lessons (section_id, title, video_url, duration_seconds, position, is_published, moderation_status)
VALUES 
(@Section2Id, N'Bài 1 - Mảng động và Danh sách liên kết', 'videos/dsa_array_linkedlist.mp4', 800, 1, 1, 'APPROVED'),
(@Section2Id, N'Bài 2 - Ngăn xếp (Stack) và Hàng đợi (Queue)', 'videos/dsa_stack_queue.mp4', 900, 2, 1, 'APPROVED'),
(@Section3Id, N'Bài 1 - Giới thiệu Spring Framework và Spring Boot', 'videos/spring_intro.mp4', 1000, 1, 1, 'APPROVED'),
(@Section3Id, N'Bài 2 - Hướng dẫn cấu hình môi trường Spring Boot', 'videos/spring_setup.mp4', 1200, 2, 1, 'APPROVED');

DECLARE @Lesson5Id INT = (SELECT id FROM lessons WHERE title = N'Bài 1 - Mảng động và Danh sách liên kết');
DECLARE @Lesson6Id INT = (SELECT id FROM lessons WHERE title = N'Bài 2 - Ngăn xếp (Stack) và Hàng đợi (Queue)');
DECLARE @Lesson7Id INT = (SELECT id FROM lessons WHERE title = N'Bài 1 - Giới thiệu Spring Framework và Spring Boot');
DECLARE @Lesson8Id INT = (SELECT id FROM lessons WHERE title = N'Bài 2 - Hướng dẫn cấu hình môi trường Spring Boot');

-- 4. Insert quizzes for Course 2 & 3
INSERT INTO quizzes (lesson_id, title, pass_score_percent )
VALUES 
(@Lesson6Id, N'Quiz - Stack và Queue', 70),
(@Lesson8Id, N'Quiz - Tổng quan Spring Boot', 70);

DECLARE @QuizDSAId INT = (SELECT id FROM quizzes WHERE title = N'Quiz - Stack và Queue');
DECLARE @QuizSpringId INT = (SELECT id FROM quizzes WHERE title = N'Quiz - Tổng quan Spring Boot');

-- 5. Insert quiz questions & answers
-- Quiz DSA Question
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position)
VALUES (@QuizDSAId, N'Cấu trúc dữ liệu Ngăn xếp (Stack) hoạt động theo nguyên lý nào?', 'SINGLE', 1, 1);
DECLARE @QDSAQ1Id INT = SCOPE_IDENTITY();

INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES 
(@QDSAQ1Id, N'LIFO (Last In First Out)', 1),
(@QDSAQ1Id, N'FIFO (First In First Out)', 0),
(@QDSAQ1Id, N'LILO (Last In Last Out)', 0),
(@QDSAQ1Id, N'Ngẫu nhiên', 0);

-- Quiz Spring Boot Question
INSERT INTO quiz_questions (quiz_id, question_text, question_type, points, position)
VALUES (@QuizSpringId, N'Spring Boot giúp đơn giản hóa việc gì trong phát triển ứng dụng Java?', 'SINGLE', 1, 1);
DECLARE @QSpringQ1Id INT = SCOPE_IDENTITY();

INSERT INTO quiz_answers (question_id, answer_text, is_correct)
VALUES 
(@QSpringQ1Id, N'Cấu hình và triển khai ứng dụng (Auto-configuration)', 1),
(@QSpringQ1Id, N'Viết cú pháp ngôn ngữ Java', 0),
(@QSpringQ1Id, N'Thiết kế giao diện người dùng HTML/CSS', 0),
(@QSpringQ1Id, N'Quản lý hệ điều hành máy chủ', 0);

-- 6. Insert Coupons
INSERT INTO coupons (instructor_id, code, discount_type, discount_value, usage_limit, used_count, expired_at, status)
VALUES 
(1, 'WELCOME10', 'PERCENT', 10.00, 100, 1, DATEADD(month, 6, GETDATE()), 'ACTIVE'),
(1, 'DEVSPECIAL', 'FIXED', 100000.00, 50, 0, DATEADD(month, 3, GETDATE()), 'ACTIVE');

DECLARE @Coupon1Id INT = (SELECT id FROM coupons WHERE code = 'WELCOME10');

GO

-- ============================================================================
-- 10 LEARNERS FULL ACTIVITIES MOCK DATA (SYNCHRONIZED FROM JAVA)
-- ============================================================================
USE ElearningPlatform;
GO

-- A. Declare cache variables for existing elements
-- Filter by course title to avoid "Subquery returned more than 1 value" when lesson titles are duplicated across courses
DECLARE @L1Id INT = (SELECT TOP 1 l.id FROM lessons l JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE l.title = N'Bài 1 - Giới thiệu ngôn ngữ C' AND c.title = N'Lập Trình C Cơ Bản - 28Tech');
DECLARE @L2Id INT = (SELECT TOP 1 l.id FROM lessons l JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE l.title = N'Bài 2 - Kiểu dữ liệu và khai báo biến trong C' AND c.title = N'Lập Trình C Cơ Bản - 28Tech');
DECLARE @L3Id INT = (SELECT TOP 1 l.id FROM lessons l JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE l.title = N'Bài 3 - Xuất dữ liệu với printf' AND c.title = N'Lập Trình C Cơ Bản - 28Tech');
DECLARE @L4Id INT = (SELECT TOP 1 l.id FROM lessons l JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE l.title = N'Bài 4 - Nhập dữ liệu với scanf' AND c.title = N'Lập Trình C Cơ Bản - 28Tech');

DECLARE @Qz2Id INT = (SELECT TOP 1 q.id FROM quizzes q JOIN lessons l ON q.lesson_id = l.id JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE q.title = N'Quiz - Kiểu dữ liệu và biến' AND c.title = N'Lập Trình C Cơ Bản - 28Tech');
DECLARE @Qz3Id INT = (SELECT TOP 1 q.id FROM quizzes q JOIN lessons l ON q.lesson_id = l.id JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE q.title = N'Quiz - Hàm printf' AND c.title = N'Lập Trình C Cơ Bản - 28Tech');
DECLARE @Qz4Id INT = (SELECT TOP 1 q.id FROM quizzes q JOIN lessons l ON q.lesson_id = l.id JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE q.title = N'Quiz - Hàm scanf' AND c.title = N'Lập Trình C Cơ Bản - 28Tech');

DECLARE @C1Id INT = (SELECT id FROM courses WHERE title = N'Lập Trình C Cơ Bản - 28Tech');
DECLARE @C2Id INT = (SELECT id FROM courses WHERE title = N'Lập Trình C Nâng Cao - 28Tech');
DECLARE @C3Id INT = (SELECT id FROM courses WHERE title = N'Lập Trình C Thực Hành - 28Tech');
DECLARE @C4Id INT = (SELECT id FROM courses WHERE title = N'Thuật Toán C với 28Tech');
DECLARE @C5Id INT = (SELECT id FROM courses WHERE title = N'Lập Trình Python Cơ Bản và Nâng Cao');
DECLARE @C6Id INT = (SELECT id FROM courses WHERE title = N'Xây Dựng Giao Diện Web Với HTML5');
DECLARE @C7Id INT = (SELECT id FROM courses WHERE title = N'Làm Chủ CSS Grid Và Flexbox Responsive');
DECLARE @C8Id INT = (SELECT id FROM courses WHERE title = N'Lập Trình Frontend Hiện Đại Với ReactJS');
DECLARE @C9Id INT = (SELECT id FROM courses WHERE title = N'Xây Dựng RESTful API Với Node.js');
DECLARE @C10Id INT = (SELECT id FROM courses WHERE title = N'Lập Trình Ứng Dụng iOS Với Ngôn Ngữ Swift');
DECLARE @C11Id INT = (SELECT id FROM courses WHERE title = N'Thiết Kế Giao Diện iOS Bằng SwiftUI');
DECLARE @C12Id INT = (SELECT id FROM courses WHERE title = N'Lập Trình Hướng Đối Tượng Java Core');
DECLARE @C13Id INT = (SELECT id FROM courses WHERE title = N'Phát Triển Ứng Dụng Web Với Java Spring Boot');
DECLARE @C14Id INT = (SELECT id FROM courses WHERE title = N'Lập Trình C Chuyên Đề 14 - 28Tech');

-- Lesson IDs for Course 2 (C Nâng cao) Section 1 (Giới thiệu)
DECLARE @C2_L1 INT = (SELECT l.id FROM lessons l JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE l.title = N'Bài 1 - Giới thiệu ngôn ngữ C' AND c.title = N'Lập Trình C Nâng Cao - 28Tech');
DECLARE @C2_L2 INT = (SELECT l.id FROM lessons l JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE l.title = N'Bài 2 - Kiểu dữ liệu và khai báo biến trong C' AND c.title = N'Lập Trình C Nâng Cao - 28Tech');
DECLARE @C2_L3 INT = (SELECT l.id FROM lessons l JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE l.title = N'Bài 3 - Xuất dữ liệu với printf' AND c.title = N'Lập Trình C Nâng Cao - 28Tech');

-- Lesson IDs for Course 3 (C Thực hành) Section 1 (Giới thiệu)
DECLARE @C3_L1 INT = (SELECT l.id FROM lessons l JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE l.title = N'Bài 1 - Giới thiệu' AND c.title = N'Lập Trình C Thực Hành - 28Tech');
DECLARE @C3_L2 INT = (SELECT l.id FROM lessons l JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE l.title = N'Bài 2 - Kiểu dữ liệu' AND c.title = N'Lập Trình C Thực Hành - 28Tech');
DECLARE @C3_L3 INT = (SELECT l.id FROM lessons l JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE l.title = N'Bài 3 - printf' AND c.title = N'Lập Trình C Thực Hành - 28Tech');

-- Lesson IDs for Course 4 Section 1 (Giới thiệu)
DECLARE @C4_L1 INT = (SELECT l.id FROM lessons l JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE l.title = N'Bài 1' AND c.title = N'Thuật Toán C với 28Tech');

-- Lesson IDs for Course 5 Section 1 (Giới thiệu)
DECLARE @C5_L1 INT = (SELECT l.id FROM lessons l JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE l.title = N'Bài 1 - Chuyên đề 5' AND c.title = N'Lập Trình Python Cơ Bản và Nâng Cao');
DECLARE @C5_L2 INT = (SELECT l.id FROM lessons l JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE l.title = N'Bài 2 - Chuyên đề 5' AND c.title = N'Lập Trình Python Cơ Bản và Nâng Cao');
DECLARE @C5_L3 INT = (SELECT l.id FROM lessons l JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE l.title = N'Bài 3 - Chuyên đề 5' AND c.title = N'Lập Trình Python Cơ Bản và Nâng Cao');

-- Create missing Quizzes for Course 3 if not exists
IF NOT EXISTS (SELECT 1 FROM quizzes q JOIN lessons l ON q.lesson_id = l.id WHERE q.title = N'Quiz - Kiểu dữ liệu và biến' AND l.id = @C3_L2)
BEGIN
    INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@C3_L2, N'Quiz - Kiểu dữ liệu và biến', 70);
    INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@C3_L3, N'Quiz - Hàm printf', 70);
    INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@C3_L1, N'Quiz - Hàm scanf', 70);
END

-- Quizzes for Course 3
DECLARE @C3_Qz2Id INT = (SELECT q.id FROM quizzes q JOIN lessons l ON q.lesson_id = l.id JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE q.title = N'Quiz - Kiểu dữ liệu và biến' AND c.title = N'Lập Trình C Thực Hành - 28Tech');
DECLARE @C3_Qz3Id INT = (SELECT q.id FROM quizzes q JOIN lessons l ON q.lesson_id = l.id JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE q.title = N'Quiz - Hàm printf' AND c.title = N'Lập Trình C Thực Hành - 28Tech');
DECLARE @C3_Qz4Id INT = (SELECT q.id FROM quizzes q JOIN lessons l ON q.lesson_id = l.id JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE q.title = N'Quiz - Hàm scanf' AND c.title = N'Lập Trình C Thực Hành - 28Tech');

-- Create missing Quizzes for Course 4 if not exists
IF NOT EXISTS (SELECT 1 FROM quizzes q JOIN lessons l ON q.lesson_id = l.id WHERE q.title = N'Quiz - Kiểu dữ liệu và biến' AND l.id = @C4_L1)
BEGIN
    INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@C4_L1, N'Quiz - Kiểu dữ liệu và biến', 70);
    INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@C4_L1, N'Quiz - Hàm printf', 70);
    INSERT INTO quizzes (lesson_id, title, pass_score_percent ) VALUES (@C4_L1, N'Quiz - Hàm scanf', 70);
END

-- Quizzes for Course 4
DECLARE @C4_Qz2Id INT = (SELECT TOP 1 q.id FROM quizzes q JOIN lessons l ON q.lesson_id = l.id JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE q.title = N'Quiz - Kiểu dữ liệu và biến' AND c.title = N'Thuật Toán C với 28Tech');
DECLARE @C4_Qz3Id INT = (SELECT TOP 1 q.id FROM quizzes q JOIN lessons l ON q.lesson_id = l.id JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE q.title = N'Quiz - Hàm printf' AND c.title = N'Thuật Toán C với 28Tech');
DECLARE @C4_Qz4Id INT = (SELECT TOP 1 q.id FROM quizzes q JOIN lessons l ON q.lesson_id = l.id JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE q.title = N'Quiz - Hàm scanf' AND c.title = N'Thuật Toán C với 28Tech');

-- Quizzes for Course 5 (title format from WHILE loop: 'Quiz - Bài X - Khóa Y')
DECLARE @C5_Qz1Id INT = (SELECT TOP 1 q.id FROM quizzes q JOIN lessons l ON q.lesson_id = l.id JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE q.title = N'Quiz - Bài 1 - Khóa 5' AND c.title = N'Lập Trình Python Cơ Bản và Nâng Cao');
DECLARE @C5_Qz2Id INT = (SELECT TOP 1 q.id FROM quizzes q JOIN lessons l ON q.lesson_id = l.id JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE q.title = N'Quiz - Bài 2 - Khóa 5' AND c.title = N'Lập Trình Python Cơ Bản và Nâng Cao');
DECLARE @C5_Qz3Id INT = (SELECT TOP 1 q.id FROM quizzes q JOIN lessons l ON q.lesson_id = l.id JOIN course_sections s ON l.section_id = s.id JOIN courses c ON s.course_id = c.id WHERE q.title = N'Quiz - Bài 3 - Khóa 5' AND c.title = N'Lập Trình Python Cơ Bản và Nâng Cao');

-- Coupons already inserted in previous batch (lines ~1978-1981), just read their IDs
DECLARE @Cp1Id INT = (SELECT id FROM coupons WHERE code = 'WELCOME10');
DECLARE @Cp2Id INT = (SELECT id FROM coupons WHERE code = 'DEVSPECIAL');

-- B. INSERT USERS (LEARNERS)
INSERT INTO users (first_name, last_name, email, phone, password_hash, avatar_url, google_id, status)
VALUES 
(N'Nguyễn Văn', N'An', 'an.nguyen@elearning.com', '0981112222', 'password123', 'avatars/an_nguyen.jpg', NULL, 'ACTIVE'),
(N'Trần Thị', N'Bình', 'binh.tran@elearning.com', '0982223333', 'password123', 'avatars/binh_tran.jpg', NULL, 'ACTIVE'),
(N'Phạm Văn', N'Cường', 'cuong.pham@elearning.com', '0983334444', 'password123', 'avatars/cuong_pham.jpg', NULL, 'ACTIVE'),
(N'Hoàng Thị', N'Dung', 'dung.hoang@elearning.com', '0984445555', NULL, 'avatars/dung_hoang.jpg', 'google_1029384756', 'ACTIVE'),
(N'Đỗ Văn', N'Em', 'em.do@elearning.com', '0985556666', 'password123', 'avatars/em_do.jpg', NULL, 'ACTIVE'),
(N'Lê Thu', N'Giang', 'giang.le@elearning.com', '0986667777', 'password123', 'avatars/giang_le.jpg', NULL, 'ACTIVE'),
(N'Vũ Thanh', N'Hải', 'hai.vu@elearning.com', '0987778888', 'password123', 'avatars/hai_vu.jpg', NULL, 'ACTIVE'),
(N'Đỗ Khánh', N'Huy', 'huy.do@elearning.com', '0988889999', 'password123', 'avatars/huy_do.jpg', NULL, 'ACTIVE'),
(N'Bùi Anh', N'Khoa', 'khoa.bui@elearning.com', '0989990000', 'password123', 'avatars/khoa_bui.jpg', NULL, 'ACTIVE'),
(N'Đặng Quang', N'Long', 'long.dang@elearning.com', '0980001111', 'password123', 'avatars/long_dang.jpg', NULL, 'ACTIVE');

DECLARE @UserAnId INT = (SELECT id FROM users WHERE email = 'an.nguyen@elearning.com');
DECLARE @UserBinhId INT = (SELECT id FROM users WHERE email = 'binh.tran@elearning.com');
DECLARE @UserCuongId INT = (SELECT id FROM users WHERE email = 'cuong.pham@elearning.com');
DECLARE @UserDungId INT = (SELECT id FROM users WHERE email = 'dung.hoang@elearning.com');
DECLARE @UserEmId INT = (SELECT id FROM users WHERE email = 'em.do@elearning.com');
DECLARE @UserGiangId INT = (SELECT id FROM users WHERE email = 'giang.le@elearning.com');
DECLARE @UserHaiId INT = (SELECT id FROM users WHERE email = 'hai.vu@elearning.com');
DECLARE @UserHuyId INT = (SELECT id FROM users WHERE email = 'huy.do@elearning.com');
DECLARE @UserKhoaId INT = (SELECT id FROM users WHERE email = 'khoa.bui@elearning.com');
DECLARE @UserLongId INT = (SELECT id FROM users WHERE email = 'long.dang@elearning.com');

INSERT INTO user_roles (user_id, role_id) VALUES 
(@UserAnId, 4),
(@UserBinhId, 4),
(@UserCuongId, 4),
(@UserDungId, 4),
(@UserEmId, 4),
(@UserGiangId, 4),
(@UserHaiId, 4),
(@UserHuyId, 4),
(@UserKhoaId, 4),
(@UserLongId, 4);

-- C. INSERT CARTS
INSERT INTO carts (user_id) VALUES 
(@UserAnId),
(@UserBinhId),
(@UserCuongId),
(@UserDungId),
(@UserEmId),
(@UserGiangId),
(@UserHaiId),
(@UserHuyId),
(@UserKhoaId),
(@UserLongId);

DECLARE @CartAnId INT = (SELECT id FROM carts WHERE user_id = @UserAnId);
DECLARE @CartBinhId INT = (SELECT id FROM carts WHERE user_id = @UserBinhId);
DECLARE @CartCuongId INT = (SELECT id FROM carts WHERE user_id = @UserCuongId);
DECLARE @CartDungId INT = (SELECT id FROM carts WHERE user_id = @UserDungId);
DECLARE @CartEmId INT = (SELECT id FROM carts WHERE user_id = @UserEmId);
DECLARE @CartGiangId INT = (SELECT id FROM carts WHERE user_id = @UserGiangId);
DECLARE @CartHaiId INT = (SELECT id FROM carts WHERE user_id = @UserHaiId);
DECLARE @CartHuyId INT = (SELECT id FROM carts WHERE user_id = @UserHuyId);
DECLARE @CartKhoaId INT = (SELECT id FROM carts WHERE user_id = @UserKhoaId);
DECLARE @CartLongId INT = (SELECT id FROM carts WHERE user_id = @UserLongId);

-- D. INSERT ACTIVE CART ITEMS
-- Binh has Course 3 (C Thực Hành) in cart
INSERT INTO cart_items (cart_id, course_id) VALUES (@CartBinhId, @C3Id);
-- Dung has Course 2 (C Nâng Cao) in cart
INSERT INTO cart_items (cart_id, course_id) VALUES (@CartDungId, @C2Id);
-- Giang has Course 1 (C Cơ Bản) in cart
INSERT INTO cart_items (cart_id, course_id) VALUES (@CartGiangId, @C1Id);
-- Huy has Course 1 and Course 4 in cart
INSERT INTO cart_items (cart_id, course_id) VALUES 
(@CartHuyId, @C1Id),
(@CartHuyId, @C4Id);

-- E. ORDERS, ORDER ITEMS, PAYMENTS & COUPON USAGES

-- == LEARNER 0: An (Paid Order for C1 & C2)
INSERT INTO orders (user_id, total_amount, discount_amount, status, payment_method, created_at)
VALUES (@UserAnId, 350000.00, 0.00, 'PAID', 'MOMO', DATEADD(day, -5, GETDATE()));
DECLARE @OrderAnId INT = SCOPE_IDENTITY();

INSERT INTO order_items (order_id, course_id, coupon_id, price_snapshot, discount_amount, final_price, course_title_snapshot, created_at)
VALUES 
(@OrderAnId, @C1Id, NULL, 0.00, 0.00, 0.00, N'Lập Trình C Cơ Bản - 28Tech', DATEADD(day, -5, GETDATE())),
(@OrderAnId, @C2Id, NULL, 350000.00, 0.00, 350000.00, N'Lập Trình C Nâng Cao - 28Tech', DATEADD(day, -5, GETDATE()));

INSERT INTO payments (order_id, transaction_code, gateway, gateway_tx_id, amount, status, paid_at, created_at)
VALUES (@OrderAnId, 'TX_AN_001', 'MOMO', 'GATEWAY_8837', 350000.00, 'SUCCESS', DATEADD(day, -5, GETDATE()), DATEADD(day, -5, GETDATE()));

-- == LEARNER 1: Binh (Paid Order for C1)
INSERT INTO orders (user_id, total_amount, discount_amount, status, payment_method, created_at)
VALUES (@UserBinhId, 0.00, 0.00, 'PAID', 'VNPAY', DATEADD(day, -4, GETDATE()));
DECLARE @OrderBinhId INT = SCOPE_IDENTITY();

INSERT INTO order_items (order_id, course_id, coupon_id, price_snapshot, discount_amount, final_price, course_title_snapshot, created_at)
VALUES (@OrderBinhId, @C1Id, NULL, 0.00, 0.00, 0.00, N'Lập Trình C Cơ Bản - 28Tech', DATEADD(day, -4, GETDATE()));

INSERT INTO payments (order_id, transaction_code, gateway, gateway_tx_id, amount, status, paid_at, created_at)
VALUES (@OrderBinhId, 'TX_BINH_001', 'VNPAY', 'GATEWAY_2283', 0.00, 'SUCCESS', DATEADD(day, -4, GETDATE()), DATEADD(day, -4, GETDATE()));

-- == LEARNER 2: Cuong (Paid Order for C2 with WELCOME10 coupon)
INSERT INTO orders (user_id, total_amount, discount_amount, status, payment_method, created_at)
VALUES (@UserCuongId, 300000.00, 50000.00, 'PAID', 'CARD', DATEADD(day, -3, GETDATE()));
DECLARE @OrderCuongId INT = SCOPE_IDENTITY();

INSERT INTO order_items (order_id, course_id, coupon_id, price_snapshot, discount_amount, final_price, course_title_snapshot, created_at)
VALUES (@OrderCuongId, @C2Id, @Cp1Id, 350000.00, 50000.00, 300000.00, N'Lập Trình C Nâng Cao - 28Tech', DATEADD(day, -3, GETDATE()));

INSERT INTO payments (order_id, transaction_code, gateway, gateway_tx_id, amount, status, paid_at, created_at)
VALUES (@OrderCuongId, 'TX_CUONG_001', 'CARD', 'GATEWAY_9948', 300000.00, 'SUCCESS', DATEADD(day, -3, GETDATE()), DATEADD(day, -3, GETDATE()));

INSERT INTO coupon_usages (coupon_id, user_id, order_id, discount_amount, created_at)
VALUES (@Cp1Id, @UserCuongId, @OrderCuongId, 50000.00, DATEADD(day, -3, GETDATE()));

-- == LEARNER 3: Dung (Paid Order for C3)
INSERT INTO orders (user_id, total_amount, discount_amount, status, payment_method, created_at)
VALUES (@UserDungId, 600000.00, 0.00, 'PAID', 'MOMO', DATEADD(day, -2, GETDATE()));
DECLARE @OrderDungId INT = SCOPE_IDENTITY();

INSERT INTO order_items (order_id, course_id, coupon_id, price_snapshot, discount_amount, final_price, course_title_snapshot, created_at)
VALUES (@OrderDungId, @C3Id, NULL, 600000.00, 0.00, 600000.00, N'Lập Trình C Thực Hành - 28Tech', DATEADD(day, -2, GETDATE()));

INSERT INTO payments (order_id, transaction_code, gateway, gateway_tx_id, amount, status, paid_at, created_at)
VALUES (@OrderDungId, 'TX_DUNG_001', 'MOMO', 'GATEWAY_7749', 600000.00, 'SUCCESS', DATEADD(day, -2, GETDATE()), DATEADD(day, -2, GETDATE()));

-- == LEARNER 4: Em (Paid Order for C1 & C3)
INSERT INTO orders (user_id, total_amount, discount_amount, status, payment_method, created_at)
VALUES (@UserEmId, 600000.00, 0.00, 'PAID', 'VNPAY', DATEADD(day, -1, GETDATE()));
DECLARE @OrderEmId INT = SCOPE_IDENTITY();

INSERT INTO order_items (order_id, course_id, coupon_id, price_snapshot, discount_amount, final_price, course_title_snapshot, created_at)
VALUES 
(@OrderEmId, @C1Id, NULL, 0.00, 0.00, 0.00, N'Lập Trình C Cơ Bản - 28Tech', DATEADD(day, -1, GETDATE())),
(@OrderEmId, @C3Id, NULL, 600000.00, 0.00, 600000.00, N'Lập Trình C Thực Hành - 28Tech', DATEADD(day, -1, GETDATE()));

INSERT INTO payments (order_id, transaction_code, gateway, gateway_tx_id, amount, status, paid_at, created_at)
VALUES (@OrderEmId, 'TX_EM_001', 'VNPAY', 'GATEWAY_5539', 600000.00, 'SUCCESS', DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()));

-- == LEARNER 5: Giang (Pending Order for C2)
INSERT INTO orders (user_id, total_amount, discount_amount, status, payment_method, created_at)
VALUES (@UserGiangId, 350000.00, 0.00, 'PENDING', 'BANK_TRANSFER', DATEADD(day, -1, GETDATE()));
DECLARE @OrderGiangId INT = SCOPE_IDENTITY();

INSERT INTO order_items (order_id, course_id, coupon_id, price_snapshot, discount_amount, final_price, course_title_snapshot, created_at)
VALUES (@OrderGiangId, @C2Id, NULL, 350000.00, 0.00, 350000.00, N'Lập Trình C Nâng Cao - 28Tech', DATEADD(day, -1, GETDATE()));

-- == LEARNER 6: Hai (Paid Order for C4 with DEVSPECIAL coupon)
INSERT INTO orders (user_id, total_amount, discount_amount, status, payment_method, created_at)
VALUES (@UserHaiId, 750000.00, 100000.00, 'PAID', 'BANK_TRANSFER', DATEADD(day, -1, GETDATE()));
DECLARE @OrderHaiId INT = SCOPE_IDENTITY();

INSERT INTO order_items (order_id, course_id, coupon_id, price_snapshot, discount_amount, final_price, course_title_snapshot, created_at)
VALUES (@OrderHaiId, @C4Id, @Cp2Id, 850000.00, 100000.00, 750000.00, N'Thuật Toán C với 28Tech', DATEADD(day, -1, GETDATE()));

INSERT INTO payments (order_id, transaction_code, gateway, gateway_tx_id, amount, status, paid_at, created_at)
VALUES (@OrderHaiId, 'TX_HAI_001', 'BANK_TRANSFER', 'GATEWAY_4439', 750000.00, 'SUCCESS', DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()));

INSERT INTO coupon_usages (coupon_id, user_id, order_id, discount_amount, created_at)
VALUES (@Cp2Id, @UserHaiId, @OrderHaiId, 100000.00, DATEADD(day, -1, GETDATE()));

-- == LEARNER 8: Khoa (Paid Order for C5 with WELCOME10 coupon)
INSERT INTO orders (user_id, total_amount, discount_amount, status, payment_method, created_at)
VALUES (@UserKhoaId, 1500000.00, 0.00, 'PAID', 'MOMO', DATEADD(day, -1, GETDATE()));
DECLARE @OrderKhoaId INT = SCOPE_IDENTITY();

INSERT INTO order_items (order_id, course_id, coupon_id, price_snapshot, discount_amount, final_price, course_title_snapshot, created_at)
VALUES (@OrderKhoaId, @C5Id, @Cp1Id, 1500000.00, 0.00, 1500000.00, N'Lập Trình Python Cơ Bản và Nâng Cao', DATEADD(day, -1, GETDATE()));

INSERT INTO payments (order_id, transaction_code, gateway, gateway_tx_id, amount, status, paid_at, created_at)
VALUES (@OrderKhoaId, 'TX_KHOA_001', 'MOMO', 'GATEWAY_3329', 1500000.00, 'SUCCESS', DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()));

INSERT INTO coupon_usages (coupon_id, user_id, order_id, discount_amount, created_at)
VALUES (@Cp1Id, @UserKhoaId, @OrderKhoaId, 0.00, DATEADD(day, -1, GETDATE()));

-- == LEARNER 9: Long (Paid Order for C1 & C5)
INSERT INTO orders (user_id, total_amount, discount_amount, status, payment_method, created_at)
VALUES (@UserLongId, 1500000.00, 0.00, 'PAID', 'VNPAY', DATEADD(day, -1, GETDATE()));
DECLARE @OrderLongId INT = SCOPE_IDENTITY();

INSERT INTO order_items (order_id, course_id, coupon_id, price_snapshot, discount_amount, final_price, course_title_snapshot, created_at)
VALUES 
(@OrderLongId, @C1Id, NULL, 0.00, 0.00, 0.00, N'Lập Trình C Cơ Bản - 28Tech', DATEADD(day, -1, GETDATE())),
(@OrderLongId, @C5Id, NULL, 1500000.00, 0.00, 1500000.00, N'Lập Trình Python Cơ Bản và Nâng Cao', DATEADD(day, -1, GETDATE()));

INSERT INTO payments (order_id, transaction_code, gateway, gateway_tx_id, amount, status, paid_at, created_at)
VALUES (@OrderLongId, 'TX_LONG_001', 'VNPAY', 'GATEWAY_9938', 1500000.00, 'SUCCESS', DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()));

-- F. ENROLLMENTS & LESSON PROGRESS & QUIZ ATTEMPTS

-- == LEARNER 0 (Nguyễn Văn An) ==
-- Enrolled Course 1 (100% progress) and Course 2 (50% progress)
INSERT INTO enrollments (user_id, course_id, progress_percent, created_at, completed_at)
VALUES 
(@UserAnId, @C1Id, 100.00, DATEADD(day, -5, GETDATE()), DATEADD(day, -3, GETDATE())),
(@UserAnId, @C2Id, 50.00, DATEADD(day, -5, GETDATE()), NULL);

DECLARE @EnrollAnC2 INT = (SELECT id FROM enrollments WHERE user_id = @UserAnId AND course_id = @C2Id);

-- Course 1 Lesson Progress (4/4 lessons)
INSERT INTO lesson_progress (enrollment_id, lesson_id, is_completed, last_accessed)
VALUES 
((SELECT id FROM enrollments WHERE user_id = @UserAnId AND course_id = @C1Id), @L1Id, 1, DATEADD(day, -5, GETDATE())),
((SELECT id FROM enrollments WHERE user_id = @UserAnId AND course_id = @C1Id), @L2Id, 1, DATEADD(day, -4, GETDATE())),
((SELECT id FROM enrollments WHERE user_id = @UserAnId AND course_id = @C1Id), @L3Id, 1, DATEADD(day, -4, GETDATE())),
((SELECT id FROM enrollments WHERE user_id = @UserAnId AND course_id = @C1Id), @L4Id, 1, DATEADD(day, -3, GETDATE()));

-- Course 1 Quiz Attempts
INSERT INTO quiz_attempts (user_id, quiz_id, score, is_passed, started_at, submitted_at)
VALUES 
(@UserAnId, @Qz2Id, 100.00, 1, DATEADD(day, -4, GETDATE()), DATEADD(day, -4, GETDATE())),
(@UserAnId, @Qz3Id, 100.00, 1, DATEADD(day, -4, GETDATE()), DATEADD(day, -4, GETDATE())),
(@UserAnId, @Qz4Id, 100.00, 1, DATEADD(day, -3, GETDATE()), DATEADD(day, -3, GETDATE()));

-- Course 2 Lesson Progress (1/12 lessons)
INSERT INTO lesson_progress (enrollment_id, lesson_id, is_completed, last_accessed)
VALUES (@EnrollAnC2, @C2_L1, 1, DATEADD(day, -2, GETDATE()));

-- == LEARNER 1 (Trần Thị Bình) ==
-- Enrolled Course 1 (50% progress)
INSERT INTO enrollments (user_id, course_id, progress_percent, created_at, completed_at)
VALUES (@UserBinhId, @C1Id, 50.00, DATEADD(day, -4, GETDATE()), NULL);

DECLARE @EnrollBinhC1 INT = (SELECT id FROM enrollments WHERE user_id = @UserBinhId AND course_id = @C1Id);

-- Course 1 Lesson Progress (2/4 lessons)
INSERT INTO lesson_progress (enrollment_id, lesson_id, is_completed, last_accessed)
VALUES 
(@EnrollBinhC1, @L1Id, 1, DATEADD(day, -4, GETDATE())),
(@EnrollBinhC1, @L2Id, 1, DATEADD(day, -3, GETDATE()));

-- Course 1 Quiz Attempts
INSERT INTO quiz_attempts (user_id, quiz_id, score, is_passed, started_at, submitted_at)
VALUES (@UserBinhId, @Qz2Id, 75.00, 1, DATEADD(day, -3, GETDATE()), DATEADD(day, -3, GETDATE()));

-- == LEARNER 2 (Phạm Văn Cường) ==
-- Enrolled Course 2 (0% progress)
INSERT INTO enrollments (user_id, course_id, progress_percent, created_at, completed_at)
VALUES (@UserCuongId, @C2Id, 0.00, DATEADD(day, -3, GETDATE()), NULL);

-- == LEARNER 4 (Hoàng Thị Dung) ==
-- Enrolled Course 3 (100% progress)
INSERT INTO enrollments (user_id, course_id, progress_percent, created_at, completed_at)
VALUES (@UserDungId, @C3Id, 100.00, DATEADD(day, -2, GETDATE()), DATEADD(day, -1, GETDATE()));

DECLARE @EnrollDungC3 INT = (SELECT id FROM enrollments WHERE user_id = @UserDungId AND course_id = @C3Id);

-- Course 3 Lesson Progress (3/3 lessons completed)
INSERT INTO lesson_progress (enrollment_id, lesson_id, is_completed, last_accessed)
VALUES 
(@EnrollDungC3, @C3_L1, 1, DATEADD(day, -2, GETDATE())),
(@EnrollDungC3, @C3_L2, 1, DATEADD(day, -1, GETDATE())),
(@EnrollDungC3, @C3_L3, 1, DATEADD(day, -1, GETDATE()));

-- Course 3 Quiz Attempts
INSERT INTO quiz_attempts (user_id, quiz_id, score, is_passed, started_at, submitted_at)
VALUES 
(@UserDungId, @C3_Qz2Id, 100.00, 1, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE())),
(@UserDungId, @C3_Qz3Id, 100.00, 1, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()));


-- == LEARNER 5 (Đỗ Văn Em) ==
-- Enrolled Course 1 (75% progress) and Course 3 (50% progress)
INSERT INTO enrollments (user_id, course_id, progress_percent, created_at, completed_at)
VALUES 
(@UserEmId, @C1Id, 75.00, DATEADD(day, -1, GETDATE()), NULL),
(@UserEmId, @C3Id, 50.00, DATEADD(day, -1, GETDATE()), NULL);

DECLARE @EnrollEmC1 INT = (SELECT id FROM enrollments WHERE user_id = @UserEmId AND course_id = @C1Id);
DECLARE @EnrollEmC3 INT = (SELECT id FROM enrollments WHERE user_id = @UserEmId AND course_id = @C3Id);

-- Course 1 Lesson Progress (3/4 lessons)
INSERT INTO lesson_progress (enrollment_id, lesson_id, is_completed, last_accessed)
VALUES 
(@EnrollEmC1, @L1Id, 1, DATEADD(day, -1, GETDATE())),
(@EnrollEmC1, @L2Id, 1, DATEADD(day, -1, GETDATE())),
(@EnrollEmC1, @L3Id, 1, DATEADD(day, -1, GETDATE()));

-- Course 1 Quiz Attempts
INSERT INTO quiz_attempts (user_id, quiz_id, score, is_passed, started_at, submitted_at)
VALUES 
(@UserEmId, @Qz2Id, 85.00, 1, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE())),
(@UserEmId, @Qz3Id, 50.00, 0, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE())), -- Failed attempt
(@UserEmId, @Qz3Id, 80.00, 1, DATEADD(minute, 30, DATEADD(day, -1, GETDATE())), DATEADD(minute, 35, DATEADD(day, -1, GETDATE()))); -- Passed attempt

-- Course 3 Lesson Progress (1 lesson completed)
INSERT INTO lesson_progress (enrollment_id, lesson_id, is_completed, last_accessed)
VALUES (@EnrollEmC3, @C3_L1, 1, DATEADD(day, -1, GETDATE()));


-- == LEARNER 6 (Vũ Thanh Hải) ==
-- Enrolled Course 4 (25% progress)
INSERT INTO enrollments (user_id, course_id, progress_percent, created_at, completed_at)
VALUES (@UserHaiId, @C4Id, 25.00, DATEADD(day, -1, GETDATE()), NULL);

DECLARE @EnrollHaiC4 INT = (SELECT id FROM enrollments WHERE user_id = @UserHaiId AND course_id = @C4Id);

-- Course 4 Lesson Progress (1 lesson completed)
INSERT INTO lesson_progress (enrollment_id, lesson_id, is_completed, last_accessed)
VALUES (@EnrollHaiC4, @C4_L1, 1, DATEADD(day, -1, GETDATE()));

-- Course 4 Quiz Attempt (Failed)
INSERT INTO quiz_attempts (user_id, quiz_id, score, is_passed, started_at, submitted_at)
VALUES (@UserHaiId, @C4_Qz2Id, 60.00, 0, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()));


-- == LEARNER 8 (Bùi Anh Khoa) ==
-- Enrolled Course 5 (100% progress)
INSERT INTO enrollments (user_id, course_id, progress_percent, created_at, completed_at)
VALUES (@UserKhoaId, @C5Id, 100.00, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()));

DECLARE @EnrollKhoaC5 INT = (SELECT id FROM enrollments WHERE user_id = @UserKhoaId AND course_id = @C5Id);

-- Course 5 Lesson Progress (3 lessons completed)
INSERT INTO lesson_progress (enrollment_id, lesson_id, is_completed, last_accessed)
VALUES 
(@EnrollKhoaC5, @C5_L1, 1, DATEADD(day, -1, GETDATE())),
(@EnrollKhoaC5, @C5_L2, 1, DATEADD(day, -1, GETDATE())),
(@EnrollKhoaC5, @C5_L3, 1, DATEADD(day, -1, GETDATE()));

-- Course 5 Quiz Attempts
INSERT INTO quiz_attempts (user_id, quiz_id, score, is_passed, started_at, submitted_at)
VALUES 
(@UserKhoaId, @C5_Qz1Id, 90.00, 1, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE())),
(@UserKhoaId, @C5_Qz2Id, 90.00, 1, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE())),
(@UserKhoaId, @C5_Qz3Id, 90.00, 1, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()));


-- == LEARNER 9 (Đặng Quang Long) ==
-- Enrolled Course 1 (100% progress) and Course 5 (100% progress)
INSERT INTO enrollments (user_id, course_id, progress_percent, created_at, completed_at)
VALUES 
(@UserLongId, @C1Id, 100.00, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE())),
(@UserLongId, @C5Id, 100.00, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()));

DECLARE @EnrollLongC1 INT = (SELECT id FROM enrollments WHERE user_id = @UserLongId AND course_id = @C1Id);
DECLARE @EnrollLongC5 INT = (SELECT id FROM enrollments WHERE user_id = @UserLongId AND course_id = @C5Id);

-- Course 1 Lesson Progress (4/4 lessons)
INSERT INTO lesson_progress (enrollment_id, lesson_id, is_completed, last_accessed)
VALUES 
(@EnrollLongC1, @L1Id, 1, DATEADD(day, -1, GETDATE())),
(@EnrollLongC1, @L2Id, 1, DATEADD(day, -1, GETDATE())),
(@EnrollLongC1, @L3Id, 1, DATEADD(day, -1, GETDATE())),
(@EnrollLongC1, @L4Id, 1, DATEADD(day, -1, GETDATE()));

-- Course 5 Lesson Progress (3/3 lessons)
INSERT INTO lesson_progress (enrollment_id, lesson_id, is_completed, last_accessed)
VALUES 
(@EnrollLongC5, @C5_L1, 1, DATEADD(day, -1, GETDATE())),
(@EnrollLongC5, @C5_L2, 1, DATEADD(day, -1, GETDATE())),
(@EnrollLongC5, @C5_L3, 1, DATEADD(day, -1, GETDATE()));

-- Quiz Attempts (Course 1 and Course 5)
INSERT INTO quiz_attempts (user_id, quiz_id, score, is_passed, started_at, submitted_at)
VALUES 
(@UserLongId, @Qz2Id, 100.00, 1, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE())),
(@UserLongId, @Qz3Id, 100.00, 1, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE())),
(@UserLongId, @Qz4Id, 100.00, 1, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE())),
(@UserLongId, @C5_Qz1Id, 100.00, 1, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE())),
(@UserLongId, @C5_Qz2Id, 100.00, 1, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE())),
(@UserLongId, @C5_Qz3Id, 100.00, 1, DATEADD(day, -1, GETDATE()), DATEADD(day, -1, GETDATE()));


-- G. FEEDBACKS / REVIEWS
INSERT INTO feedbacks (user_id, course_id, rating, comment, status, created_at)
VALUES 
-- Course 1 Feedbacks (Avg = 3.3)
(@UserAnId, @C1Id, 5, N'Khóa học C cơ bản vô cùng chất lượng, giảng viên giải thích cực kỳ tỉ mỉ và dễ nhớ!', 'VISIBLE', DATEADD(day, -3, GETDATE())),
(@UserBinhId, @C1Id, 4, N'Bài giảng chuẩn bị rất công phu, giao diện học tập trực quan. Tuy nhiên, một số bài tập tự luyện hơi khó.', 'VISIBLE', DATEADD(day, -3, GETDATE())),
(@UserLongId, @C1Id, 1, N'Chất lượng âm thanh video bài 2 và bài 3 mờ nhạt và rất khó nghe, mong admin sớm cải thiện.', 'VISIBLE', DATEADD(day, -1, GETDATE())),

-- Course 2 Feedbacks (Avg = 4.7)
(@UserCuongId, @C2Id, 5, N'Tài liệu PDF đi kèm rất xịn, bài tập trắc nghiệm có giải thích chi tiết đáp án.', 'VISIBLE', DATEADD(day, -2, GETDATE())),
(@UserAnId, @C2Id, 5, N'Khóa học nâng cao siêu hay, kiến thức sâu sắc.', 'VISIBLE', DATEADD(day, -2, GETDATE())),
(@UserBinhId, @C2Id, 4, N'Khá tốt nhưng cần thêm bài tập thực hành.', 'VISIBLE', DATEADD(day, -2, GETDATE())),

-- Course 3 Feedbacks (Avg = 4.3)
(@UserDungId, @C3Id, 5, N'Lập trình C thực hành rất thực tế, nhiều bài tập hay.', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserBinhId, @C3Id, 4, N'Bài tập đa dạng, rất bám sát thực tế đi làm.', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserCuongId, @C3Id, 4, N'Giao diện bài thực hành chạy mượt mà.', 'VISIBLE', DATEADD(day, -1, GETDATE())),

-- Course 4 Feedbacks (Avg = 3.3)
(@UserHaiId, @C4Id, 4, N'Nội dung cấu trúc dữ liệu và giải thuật chi tiết.', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserCuongId, @C4Id, 3, N'Thuật toán C hơi phức tạp so với trình độ của tôi, bài giảng đi nhanh quá.', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserAnId, @C4Id, 3, N'Nội dung tạm ổn.', 'VISIBLE', DATEADD(day, -1, GETDATE())),

-- Course 5 Feedbacks (Avg = 3.7)
(@UserKhoaId, @C5Id, 5, N'Khóa học chuyên đề rất hữu ích, giúp tôi hiểu sâu về mảng và con trỏ!', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserDungId, @C5Id, 4, N'Học rất ổn.', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserAnId, @C5Id, 2, N'Hơi khó so với người mới bắt đầu.', 'VISIBLE', DATEADD(day, -1, GETDATE())),

-- Course 6 Feedbacks (Avg = 3.7)
(@UserAnId, @C6Id, 4, N'Khá tốt!', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserBinhId, @C6Id, 4, N'Bài giảng rõ ràng.', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserCuongId, @C6Id, 3, N'Được.', 'VISIBLE', DATEADD(day, -1, GETDATE())),

-- Course 7 Feedbacks
(@UserBinhId, @C7Id, 1, N'Quá tệ!', 'VISIBLE', DATEADD(day, -1, GETDATE())),

-- Course 8 Feedbacks (Avg = 2.7)
(@UserCuongId, @C8Id, 3, N'Bình thường.', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserDungId, @C8Id, 3, N'Hơi khó hiểu.', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserEmId, @C8Id, 2, N'Chưa thực sự chi tiết.', 'VISIBLE', DATEADD(day, -1, GETDATE())),

-- Course 9 Feedbacks
(@UserDungId, @C9Id, 4, N'Rất hay.', 'VISIBLE', DATEADD(day, -1, GETDATE())),

-- Course 10 Feedbacks (Avg = 4.7)
(@UserEmId, @C10Id, 5, N'Tuyệt vời.', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserGiangId, @C10Id, 5, N'Khá hay.', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserHaiId, @C10Id, 4, N'Nội dung phong phú.', 'VISIBLE', DATEADD(day, -1, GETDATE())),

-- Course 11 Feedbacks
(@UserGiangId, @C11Id, 2, N'Hơi sơ sài.', 'VISIBLE', DATEADD(day, -1, GETDATE())),

-- Course 12 Feedbacks (Avg = 3.7)
(@UserHaiId, @C12Id, 4, N'Được.', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserHuyId, @C12Id, 4, N'Bổ ích.', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserKhoaId, @C12Id, 3, N'Tạm được.', 'VISIBLE', DATEADD(day, -1, GETDATE())),

-- Course 13 Feedbacks
(@UserHuyId, @C13Id, 4, N'Tốt.', 'VISIBLE', DATEADD(day, -1, GETDATE())),

-- Course 14 Feedbacks (Avg = 4.3)
(@UserKhoaId, @C14Id, 5, N'Cực tốt.', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserLongId, @C14Id, 4, N'Chất lượng.', 'VISIBLE', DATEADD(day, -1, GETDATE())),
(@UserAnId, @C14Id, 4, N'Học xong làm được ngay.', 'VISIBLE', DATEADD(day, -1, GETDATE()));


DECLARE @EnrollmentId INT = SCOPE_IDENTITY();

INSERT INTO lesson_progress (
    enrollment_id,
    lesson_id,
    is_completed,
    last_accessed
)
VALUES
(1, 1, 1, GETDATE()),
(1, 2, 1, GETDATE()),
(1, 3, 1, GETDATE()),
(1, 4, 1, GETDATE()),
(1, 5, 1, GETDATE()),
(1, 6, 1, GETDATE());

UPDATE users
SET password_hash = '$2a$12$BBHjuDWH7w0RXg9ejOmJ1uds8/7ZLaDM0zpX/9INmkUqawEwaaXUy'
WHERE id = 1;

GO
