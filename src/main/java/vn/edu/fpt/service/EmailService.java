package vn.edu.fpt.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import vn.edu.fpt.entity.Course;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetPasswordEmail(String email, String token) {

        String url = "http://localhost:8080/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset Password");
        message.setText("Click link to reset password:\n" + url);

        mailSender.send(message);
    }

    public void sendCourseApprovedEmail(String email, String instructorName, String courseTitle) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your course has been approved");
        message.setText(String.format(
                "Hello %s,\n\n" +
                        "Congratulations!\n\n" +
                        "Your course \"%s\" has been approved by our review team and is now published on the platform.\n\n" +
                        "Students can now enroll in your course.\n\n" +
                        "Best regards,\n" +
                        "E-learning Team",
                instructorName, courseTitle
        ));
        mailSender.send(message);
    }

    public void sendCourseRejectedEmail(String email, String instructorName, String courseTitle, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Course Review Result");
        message.setText(String.format(
                "Hello %s,\n\n" +
                        "Unfortunately, your course \"%s\" was not approved.\n\n" +
                        "Reason:\n" +
                        "%s\n\n" +
                        "Please update the course and submit it again.\n\n" +
                        "Best regards,\n" +
                        "E-learning Team",
                instructorName, courseTitle, reason != null ? reason : "No reason provided."
        ));
        mailSender.send(message);
    }

    public void sendVerifyEmail(
            String email,
            String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);

        message.setSubject("Email Verification");

        message.setText(

                """
                        Your verification code is:
                        
                        %s
                        
                        This code expires in 5 minutes.
                        
                        """
                        .formatted(otp)

        );

        mailSender.send(message);

    }

    public void sendGrantAccessCourseEmail(String toEmail, Course course) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Thông báo: Bạn đã được cấp quyền tham gia khóa học mới");

            String htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <style>
                            body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f6f9fc; margin: 0; padding: 0; -webkit-font-smoothing: antialiased; }
                            .wrapper { width: 100%; table-layout: fixed; background-color: #f6f9fc; padding: 40px 0; }
                            .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border: 1px solid #eef2f5; }
                            .header { background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); padding: 35px 40px; text-align: center; }
                            .header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 700; letter-spacing: -0.5px; }
                            .content { padding: 40px; color: #334155; line-height: 1.6; }
                            .greeting { font-size: 18px; font-weight: 600; color: #1e293b; margin-bottom: 16px; }
                            .course-box { background: #f8fafc; border-left: 4px solid #4f46e5; padding: 20px; border-radius: 0 8px 8px 0; margin: 25px 0; }
                            .course-title { font-size: 18px; font-weight: 700; color: #1e293b; margin: 0 0 8px 0; }
                            .course-desc { font-size: 14px; color: #64748b; margin: 0; }
                            .btn-wrapper { text-align: center; margin: 35px 0 20px 0; }
                            .btn { display: inline-block; background-color: #4f46e5; color: #ffffff !important; text-decoration: none; padding: 12px 30px; font-weight: 600; font-size: 15px; border-radius: 8px; box-shadow: 0 4px 6px rgba(79, 70, 229, 0.15); transition: background-color 0.2s; }
                            .footer { background-color: #f8fafc; padding: 25px 40px; text-align: center; border-top: 1px solid #eef2f5; font-size: 13px; color: #94a3b8; }
                            .footer a { color: #4f46e5; text-decoration: none; }
                        </style>
                    </head>
                    <body>
                        <div class="wrapper">
                            <div class="container">
                                <div class="header">
                                    <h1>HỌC TẬP KHÔNG GIỚI HẠN</h1>
                                </div>
                                <div class="content">
                                    <div class="greeting">Xin chào bạn học viên thân mến,</div>
                                    <p>Chúng tôi rất vui mừng được thông báo rằng bạn đã được ban quản trị cấp quyền truy cập chính thức vào khóa học mới trên nền tảng của chúng tôi.</p>
                    
                                    <div class="course-box">
                                        <h3 class="course-title">%s</h3>
                                        <p class="course-desc">Khóa học hiện đã mở, bạn có thể truy cập toàn bộ bài giảng, tài liệu học tập và bài tập thực hành ngay từ lúc này.</p>
                                    </div>
                    
                                    <p>Hãy bắt đầu hành trình chinh phục kiến thức mới ngay hôm nay bằng cách đăng nhập vào hệ thống học tập của chúng tôi.</p>
                    
                                    <div class="btn-wrapper">
                                        <a href="http://localhost:8080/login_no" class="btn">Vào Học Ngay</a>
                                    </div>
                                </div>
                                <div class="footer">
                                    <p>Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ <a href="mailto:support@elearning.com">Bộ phận Hỗ trợ</a> để được trợ giúp.</p>
                                    <p>&copy; 2026 E-learning Platform. All rights reserved.</p>
                                </div>
                            </div>
                        </div>
                    </body>
                    </html>
                    """.formatted(course.getTitle());

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException("Gửi mail thất bại", e);
        }
    }
}
