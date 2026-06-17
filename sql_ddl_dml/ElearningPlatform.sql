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

                         video_url NVARCHAR(500) NULL,
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

    title VARCHAR(200) NOT NULL,
    -- tên mã

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