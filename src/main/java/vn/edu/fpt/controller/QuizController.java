package vn.edu.fpt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.dto.quizdto.QuizDTO;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.entity.Quiz;
import vn.edu.fpt.entity.QuizAttempt;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.service.LessonService;
import vn.edu.fpt.service.quizservice.QuizService;
import vn.edu.fpt.service.quizservice.QuizAttemptService;
import vn.edu.fpt.util.SecurityUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class QuizController {

    private final LessonService lessonService;
    private final QuizService quizService;
    private final QuizAttemptService quizAttemptService;

    @GetMapping("/quiz/lesson/{lessonId}")
    public String viewQuiz(
            @PathVariable("lessonId") Integer lessonId,
            @RequestParam(value = "retake", required = false, defaultValue = "false") boolean retake,
            Model model) {
        
        User user = SecurityUtils.getCurrentUser();
        Lesson lesson = lessonService.findByIdWithQuizzes(lessonId);
        List<QuizDTO> quizzes = quizService.toQuizDtos(lesson.getQuizzes());
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
            jakarta.servlet.http.HttpServletRequest request) {

        User user = SecurityUtils.getCurrentUser();
        Quiz quiz = quizService.findById(quizId)
                .orElseThrow(() -> new vn.edu.fpt.exception.ResourceNotFoundException("Quiz not found"));

        int totalPoints = 0;
        int userPoints = 0;

        for (vn.edu.fpt.entity.QuizQuestion question : quiz.getQuestions()) {
            int qPoints = question.getPoints() != null ? question.getPoints() : 1;
            totalPoints += qPoints;

            List<Integer> correctAnswers = question.getAnswers().stream()
                    .filter(vn.edu.fpt.entity.QuizAnswer::getCorrect)
                    .map(vn.edu.fpt.entity.BaseEntity::getId)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                        list.sort(Integer::compareTo);
                        return list;
                    }));

            // Get selected answer ids from parameter
            String[] paramValues = request.getParameterValues("quiz-" + quizId + "-question-" + question.getId());
            List<Integer> userSelected = new ArrayList<>();
            if (paramValues != null) {
                for (String val : paramValues) {
                    try {
                        userSelected.add(Integer.parseInt(val));
                    } catch (NumberFormatException ignored) {}
                }
            }
            userSelected.sort(Integer::compareTo);

            boolean isCorrect = userSelected.equals(correctAnswers);
            if (isCorrect) {
                userPoints += qPoints;
            }
        }

        double scorePercentage = totalPoints > 0 ? ((double) userPoints * 100.0 / totalPoints) : 0.0;
        boolean passed = scorePercentage >= quiz.getPassScore();

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
