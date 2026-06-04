package vn.edu.fpt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.edu.fpt.dto.quizdto.QuizDTO;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.service.LessonService;
import vn.edu.fpt.service.quizservice.QuizService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class QuizController {

    private final LessonService lessonService;

    private final QuizService quizService;

    @GetMapping("/quiz/lesson/{lessonId}")
    public String viewQuiz(@PathVariable("lessonId") Integer lessonId, Model model) {
        Lesson lesson = lessonService.findByIdWithQuizzes(lessonId);
        List<QuizDTO> quizzes = quizService.toQuizDtos(lesson.getQuizzes());
        int totalQuestions = quizService.totalQuestion(quizzes);

        model.addAttribute("lessonId", lesson.getId());
        model.addAttribute("lessonTitle", lesson.getTitle());
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("quizCount", quizzes.size());
        model.addAttribute("totalQuestions", totalQuestions);
        return "quiz/lesson-quiz";
    }
}
