package vn.edu.fpt.controller.quiz;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.dto.quizdto.LessonQuizPageDTO;
import vn.edu.fpt.dto.quizdto.QuizSubmitDTO;
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
    public String viewQuiz(@PathVariable("lessonId") Integer lessonId, @RequestParam(value = "retake", required = false, defaultValue = "false") boolean retake, @RequestParam(value = "activeQuizId", required = false) Integer activeQuizId, @RequestParam(value = "activeAttemptId", required = false) Integer activeAttemptId, Model model) {
        User user = SecurityUtils.getCurrentUser();
        LessonQuizPageDTO page = quizService.getLessonQuizPage(lessonId, user, retake, activeQuizId, activeAttemptId);
        model.addAttribute("page", page);
        return "quiz/lesson-quiz";
    }

    @PostMapping("/quiz/submit/{quizId}")
    public String submitQuiz(@PathVariable("quizId") Integer quizId, @ModelAttribute QuizSubmitDTO submitDTO) {
        User user = SecurityUtils.getCurrentUser();
        submitDTO.setQuizId(quizId);
        QuizAttempt attempt = quizAttemptService.submitQuiz(quizId, user, submitDTO);
        return "redirect:/quiz/lesson/" + attempt.getQuiz().getLesson().getId() + "?activeQuizId=" + quizId;
    }
}
