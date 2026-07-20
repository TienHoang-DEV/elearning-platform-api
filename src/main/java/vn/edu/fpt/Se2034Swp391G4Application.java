package vn.edu.fpt;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import vn.edu.fpt.entity.Category;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.repository.CategoryRepository;
import vn.edu.fpt.service.CategoryService;
import vn.edu.fpt.service.UserService;

import java.util.List;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class Se2034Swp391G4Application {

    public static void main(String[] args) {
        System.out.println("Working directory: " + System.getProperty("user.dir"));

        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        if (System.getProperty("MAIL_HOST") == null) {
            throw new IllegalStateException(
                    "Không đọc được MAIL_HOST từ file .env"
            );
        }

        SpringApplication.run(Se2034Swp391G4Application.class, args);
    }

}
