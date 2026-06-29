package vn.edu.fpt.controller.quiz;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.dto.quizdto.QuizDTO;
import vn.edu.fpt.entity.*;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.service.LessonService;
import vn.edu.fpt.service.quiz.QuizService;
import vn.edu.fpt.service.quiz.QuizAttemptService;
import vn.edu.fpt.util.SecurityUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class QuizController {

    private final LessonService lessonService;
    private final QuizService quizService;
    private final QuizAttemptService quizAttemptService;
    private final DtoMapper dtoMapper;

    @GetMapping("/quiz/lesson/{lessonId}")
    public String viewQuiz(
            @PathVariable("lessonId") Integer lessonId,
            @RequestParam(value = "retake", required = false, defaultValue = "false") boolean retake,
            Model model) {

        User user = SecurityUtils.getCurrentUser();
        Lesson lesson = lessonService.findByIdWithQuizzes(lessonId);
        List<QuizDTO> quizzes = dtoMapper.toQuizDtos(lesson.getQuizzes());
        int totalQuestions = quizService.totalQuestion(quizzes);

        Map<Integer, List<QuizAttempt>> quizAttemptsMap = new HashMap<>();
        for (QuizDTO q : quizzes) {
            List<QuizAttempt> attempts = quizAttemptService.findAttemptsByUserAndQuiz(user.getId(), q.getId());
            quizAttemptsMap.put(q.getId(), attempts);
        }

        model.addAttribute("lessonId", lesson.getId());
        model.addAttribute("lessonTitle", lesson.getTitle());
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("quizCount", quizzes.size());
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("quizAttemptsMap", quizAttemptsMap);
        model.addAttribute("retake", retake);
        return "quiz/lesson-quiz";
    }

    @Transactional
    @PostMapping("/quiz/submit/{quizId}")
    public String submitQuiz(
            @PathVariable("quizId") Integer quizId,
            HttpServletRequest request) {

        User user = SecurityUtils.getCurrentUser();
        Quiz quiz = quizService.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        int totalPoints = 0;
        int userPoints = 0;

        for (QuizQuestion question : quiz.getQuestions()) {
            int qPoints = question.getPoints() != null ? question.getPoints() : 1;
            totalPoints += qPoints;

            List<Integer> correctAnswers = new ArrayList<>();

            for (QuizAnswer answer : question.getAnswers()) {
                if (answer.getCorrect()) {
                    correctAnswers.add(answer.getId());
                }
            }

            Collections.sort(correctAnswers);
            // Get selected answer ids from parameter
            String[] paramValues = request.getParameterValues("quiz-" + quizId + "-question-" + question.getId());
            List<Integer> userSelected = new ArrayList<>();
            if (paramValues != null) {
                for (String val : paramValues) {
                    userSelected.add(Integer.parseInt(val));
                }
            }
            Collections.sort(userSelected);

            boolean isCorrect = userSelected.equals(correctAnswers);
            if (isCorrect) {
                userPoints += qPoints;
            }
        }

        double scorePercentage = totalPoints > 0 ? ((double) userPoints * 100.0 / totalPoints) : 0.0;
        boolean passed = scorePercentage >= quiz.getPassScorePercent();

        // Save Attempt
        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .score(BigDecimal.valueOf(scorePercentage))
                .passed(passed)
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .submittedAt(LocalDateTime.now())
                .build();
        quizAttemptService.save(attempt);

        return "redirect:/quiz/lesson/" + quiz.getLesson().getId();
    }
}
