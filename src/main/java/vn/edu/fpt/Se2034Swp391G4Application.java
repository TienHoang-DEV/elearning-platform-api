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
        // Load .env early so values are available as system properties and env for Spring
        Dotenv dotenv = Dotenv.configure().filename(".env").ignoreIfMissing().load();
        for (DotenvEntry entry : dotenv.entries()) {
            // set as system property so Spring's ${...} can resolve it
            System.setProperty(entry.getKey(), entry.getValue());
        }

        SpringApplication.run(Se2034Swp391G4Application.class, args);

    }

}
