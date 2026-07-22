package vn.edu.fpt.controller.quiz;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.entity.QuizAttempt;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.service.quiz.QuizAttemptService;
import vn.edu.fpt.service.quiz.QuizService;
import vn.edu.fpt.util.SecurityUtils;

@Controller
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizAttemptService quizAttemptService;

    @GetMapping("/quiz/lesson/{lessonId}")
    public String viewQuiz(@PathVariable("lessonId") Integer lessonId, @RequestParam(value = "retake", required = false, defaultValue = "false") boolean retake, @RequestParam(value = "activeQuizId", required = false) Integer activeQuizId,Model model) {
        User user = SecurityUtils.getCurrentUser();
        quizService.populateLessonQuizModel(lessonId, user, retake, activeQuizId, model);
        return "quiz/lesson-quiz :: quizContent";
    }

    @PostMapping("/quiz/submit/{quizId}")
    public String submitQuiz(@PathVariable("quizId") Integer quizId, HttpServletRequest request) {
        User user = SecurityUtils.getCurrentUser();
        QuizAttempt attempt = quizAttemptService.submitQuiz(quizId, user, request.getParameterMap());
        return "redirect:/quiz/lesson/" + attempt.getQuiz().getLesson().getId() + "?activeQuizId=" + quizId;
    }
}
