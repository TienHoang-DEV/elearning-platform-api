package vn.edu.fpt.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.dto.course.CourseDto;
import vn.edu.fpt.dto.CourseSectionDto;
import vn.edu.fpt.dto.LessonDto;
import vn.edu.fpt.dto.quizdto.QuizDTO;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.service.CourseSectionService;
import vn.edu.fpt.service.CourseService;
import vn.edu.fpt.service.LessonService;
import vn.edu.fpt.service.quiz.QuizService;
import vn.edu.fpt.util.SecurityUtils;

@Controller
@RequestMapping("/instructor")
public class InstructorLessonController {
    private final QuizService quizService;
    private final LessonService lessonService;
    private final CourseSectionService courseSectionService;
    private final CourseService courseService;


    public InstructorLessonController(QuizService quizService, LessonService lessonService, CourseSectionService courseSectionService, CourseService courseService) {
        this.quizService = quizService;
        this.lessonService = lessonService;
        this.courseSectionService = courseSectionService;
        this.courseService = courseService;

    }

    @GetMapping("/lesson-detail/{id}")
    public String getLessonDetail(@PathVariable("id") Integer lessonId,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "size", defaultValue = "5") int size,
                                  @RequestParam(value = "status", defaultValue = "ALL") String status,
                                  Model model) {
        User currentUser = SecurityUtils.getCurrentUser();

        model.addAttribute("currentUser", currentUser);
        Lesson lesson = lessonService.findLessonById(lessonId);
        LessonDto lessonDto = lessonService.getLessonById(lessonId);
        CourseSectionDto courseSectionDto = courseSectionService.findByCourseSectionId(lesson.getCourseSection().getId());
        CourseDto courseDto = courseService.getCourseDetail(lesson.getCourseSection().getCourse().getId());

        Page<QuizDTO> quizPage = quizService.getQuizzesByStatus(page, size, status, lessonId);

        // --- TÍNH TOÁN GIÁ TRỊ END ITEM TẠI ĐÂY ---
        long endItem = Math.min(
                (long) (quizPage.getNumber() + 1) * quizPage.getSize(),
                quizPage.getTotalElements()
        );
        model.addAttribute("endItem", endItem); // Gửi giá trị an toàn sang HTML
        // ------------------------------------------
        //model.addAttribute("lesson", lesson);
        model.addAttribute("quiz", new QuizDTO());
        model.addAttribute("quizPage", quizPage);                      // Đối tượng Page để render thanh chuyển trang
        model.addAttribute("quizzes", quizPage.getContent());           // Danh sách Quiz của trang hiện tại
        model.addAttribute("currentStatus", status);                    // Để giữ trạng thái Active cho nút bấm Bộ lọc

        model.addAttribute("lesson", lessonDto);
        model.addAttribute("course", courseDto);
        model.addAttribute("section", courseSectionDto);
        return "instructor_course/lesson-detail";
    }

    @GetMapping("/lesson-detail/{id}/quizzes-fragment")
    public String getQuizzesFragment(@PathVariable("id") Integer lessonId,
                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "size", defaultValue = "5") int size,
                                     @RequestParam(value = "status", defaultValue = "ALL") String status,
                                     Model model) {

        // 1. Lấy dữ liệu phân trang Quiz giống hệt như hàm gốc của bạn
        Page<QuizDTO> quizPage = quizService.getQuizzesByStatus(page, size, status, lessonId);

        long endItem = Math.min(
                (long) (quizPage.getNumber() + 1) * quizPage.getSize(),
                quizPage.getTotalElements()
        );

        // 2. Đẩy các attribute cần thiết để render vùng danh sách và phân trang
        model.addAttribute("endItem", endItem);
        model.addAttribute("quizPage", quizPage);
        model.addAttribute("quizzes", quizPage.getContent());
        model.addAttribute("currentStatus", status);

        // Đẩy thêm Dto này vì trong file HTML có thẻ input dùng đến lesson.id
        LessonDto lessonDto = lessonService.getLessonById(lessonId);
        model.addAttribute("lesson", lessonDto);

        // Tránh lỗi binding form Thymeleaf object trống (nếu form tạo quiz nằm chung file)
        model.addAttribute("quiz", new QuizDTO());

        // 3. TRẢ VỀ FRAGMENT (Cú pháp định danh: "đường_dẫn_file_html :: tên_th_fragment")
        return "instructor_course/lesson-detail :: quizListSection";
    }

    @PostMapping("/quiz/create-inline")
    public String createQuizInline(@ModelAttribute("quiz") QuizDTO quizDTO,
                                   @RequestParam("lessonId") Integer lessonId,
                                   @RequestParam("actionTarget") String actionTarget,
                                   RedirectAttributes attributes) {

        QuizDTO savedQuiz =
                quizService.createQuiz(
                        lessonId,
                        quizDTO);



        attributes.addFlashAttribute(
                "success",
                "Khởi tạo quiz thành công");

        if ("CONTINUE".equals(actionTarget)) {

            return "redirect:/instructor/quiz/quiz-manage/"
                    + savedQuiz.getId();
        }

        return "redirect:/instructor/lesson-detail/"
                + lessonId;
    }

    @PostMapping
    public String createLesson(@PathVariable("sectionId") Integer sectionId,
                               @RequestParam("courseId") Integer courseId,
                               @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                               @Valid @ModelAttribute("lesson") LessonDto lessonDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        if (videoFile != null && !videoFile.isEmpty()) {
            lessonDto.setVideoUrl(videoFile.getOriginalFilename());
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu nhập không hợp lệ.");
            return "redirect:/instructorcourse/" + courseId + "/curriculum";
        }

        try {
            lessonService.saveLesson(sectionId, lessonDto);
            redirectAttributes.addFlashAttribute("success", "Thêm bài giảng thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/instructorcourse/" + courseId + "/curriculum";
    }

}
