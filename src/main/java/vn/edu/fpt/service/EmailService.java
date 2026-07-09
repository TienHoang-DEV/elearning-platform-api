package vn.edu.fpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
            String otp){

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
}
