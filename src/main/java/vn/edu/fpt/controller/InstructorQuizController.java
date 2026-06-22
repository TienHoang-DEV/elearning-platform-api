package vn.edu.fpt.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.dto.quizdto.QuizDTO;
import vn.edu.fpt.dto.quizdto.QuizQuestionDTO;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.entity.Quiz;
import vn.edu.fpt.entity.QuizQuestion;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.QuestionType;
import vn.edu.fpt.service.LessonService;
import vn.edu.fpt.service.quiz.QuizQuestionService;
import vn.edu.fpt.service.quiz.QuizService;
import vn.edu.fpt.util.SecurityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/instructor/quiz")
public class InstructorQuizController {

    private final QuizService quizService;
    private final QuizQuestionService quizQuestionService;


    public InstructorQuizController(QuizService quizService, QuizQuestionService quizQuestionService)
    {
        this.quizService = quizService;
        this.quizQuestionService = quizQuestionService;
    }


    @GetMapping("/add-question/{quizId}")
    public String CreateQuestionForm(
            @PathVariable("quizId") Integer quizId,
            Model model) {

        User currentUser = SecurityUtils.getCurrentUser();
        model.addAttribute("currentUser", currentUser);


        QuizDTO quizDto = quizService.findQuizById(quizId);
        model.addAttribute("quiz", quizDto);
        model.addAttribute("question", new QuizQuestionDTO());
        model.addAttribute("questionTypes", QuestionType.values());



        return "instructor_course/question-create";
    }

    @PostMapping("/save-question")
    public String saveQuestion(
            @ModelAttribute("question") QuizQuestionDTO questionDTO,
            @RequestParam("quizId") Integer quizId) {

        quizQuestionService.saveQuestion(questionDTO, quizId);
        System.out.println("-----START-----");
        System.out.println(questionDTO.getExplanation());
        System.out.println("-----END-----");

        return "redirect:/instructor/quiz/quiz-manage/" + quizId;
    }

    @GetMapping("/quiz-manage/{quizId}")
    String quizManagePage(@PathVariable("quizId") Integer quizId,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "size", defaultValue = "3") int size,
                          Model model){
        User currentUser = SecurityUtils.getCurrentUser();
        QuizDTO quizDto = quizService.findQuizById(quizId);

        Page<QuizQuestionDTO> questionPage = quizQuestionService.getQuestionsByQuizId(quizId, page, size);

        // 2. Đẩy các thuộc tính phân trang ra Model khớp với các biến trong HTML
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("quiz", quizDto);
        model.addAttribute("questions", questionPage.getContent()); // danh sách câu hỏi trang hiện tại
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", questionPage.getTotalPages());

        return "instructor_course/manage-questions";
    }

    @PostMapping("/update-quiz-meta")
    public String updateQuizMeta(
            @ModelAttribute("quiz") QuizDTO quizDTO) {

        quizService.updateQuizMeta(quizDTO);

        return "redirect:/instructor/quiz/quiz-manage/" + quizDTO.getId();
    }

    @PostMapping("/{quizId}/delete") // Đổi sang PostMapping
    public ResponseEntity<?> deleteQuiz(@PathVariable("quizId") Integer quizId) {
        try {
            quizService.deleteQuiz(quizId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa bài trắc nghiệm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: Không thể xóa bài trắc nghiệm này."));
        }
    }

    @PostMapping("/{quizId}/archive")
    public ResponseEntity<?> archiveQuiz(@PathVariable("quizId") Integer quizId) {
        try {
            quizService.archived(quizId); // Logic chuyển status thành 'ARCHIVED'
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã đưa bài trắc nghiệm vào kho lưu trữ!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: Không thể lưu trữ bài trắc nghiệm này."));
        }
    }
}