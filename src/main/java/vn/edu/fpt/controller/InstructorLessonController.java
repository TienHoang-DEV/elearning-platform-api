    package vn.edu.fpt.controller;

    import jakarta.persistence.criteria.CriteriaBuilder;
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
    import vn.edu.fpt.entity.*;
    import vn.edu.fpt.service.section.CourseSectionService;
    import vn.edu.fpt.service.CourseService;
    import vn.edu.fpt.service.material.LessonMaterialService;
    import vn.edu.fpt.service.lesson.LessonService;
    import vn.edu.fpt.service.quiz.QuizService;
    import vn.edu.fpt.util.SecurityUtils;

    import java.util.List;

    @Controller
    @RequestMapping("/instructor")
    public class InstructorLessonController {
        private final QuizService quizService;
        private final LessonService lessonService;
        private final CourseSectionService courseSectionService;
        private final CourseService courseService;
        private final LessonMaterialService lessonMaterialService;


        public InstructorLessonController(QuizService quizService, LessonService lessonService, CourseSectionService courseSectionService, CourseService courseService, LessonMaterialService lessonMaterialService) {
            this.quizService = quizService;
            this.lessonService = lessonService;
            this.courseSectionService = courseSectionService;
            this.courseService = courseService;
            this.lessonMaterialService = lessonMaterialService;
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

            model.addAttribute("lessonMaterials", lesson.getMaterials());
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

            Page<QuizDTO> quizPage = quizService.getQuizzesByStatus(page, size, status, lessonId);

            long endItem = Math.min(
                    (long) (quizPage.getNumber() + 1) * quizPage.getSize(),
                    quizPage.getTotalElements()
            );

            model.addAttribute("endItem", endItem);
            model.addAttribute("quizPage", quizPage);
            model.addAttribute("quizzes", quizPage.getContent());
            model.addAttribute("currentStatus", status);

        LessonDto lessonDto = lessonService.getLessonById(lessonId);
        model.addAttribute("lesson", lessonDto);

            model.addAttribute("quiz", new QuizDTO());

            return "instructor_course/lesson-detail :: quizListSection";
        }



        @PostMapping("/sections/{sectionId}/lessons")
        public String createLesson(
                                   @RequestParam("source") String source,
                                   @PathVariable("sectionId") Integer sectionId,
                                   @RequestParam("courseId") Integer courseId,
                                   @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                                   @RequestParam(value = "materialFiles", required = false) List<MultipartFile> materials,
                                   @Valid @ModelAttribute("lesson") LessonDto lessonDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {


           User instrutor = SecurityUtils.getCurrentUser();

            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("error", "Dữ liệu nhập không hợp lệ.");
                return "redirect:/instructorcourse/" + courseId + "/curriculum";
            }

            try {
                Lesson tmp = lessonService.saveLesson(sectionId, lessonDto, videoFile);
                lessonMaterialService.saveAllMaterial(materials,tmp.getId(), instrutor);
                redirectAttributes.addFlashAttribute("success", "Thêm bài giảng thành công!");
                if("edit".equals(source)){
                    return "redirect:/instructorcourse/" + courseId + "/edit";
                }
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            }

            return "redirect:/instructorcourse/" + courseId + "/curriculum";
        }

        @PostMapping("/sections/{sectionId}/lessons/{lessonId}/edit")
        public String updateLesson(
                @RequestParam("source") String source,
                @PathVariable("sectionId") Integer sectionId,
                @PathVariable("lessonId") Integer lessonId,
                @RequestParam("courseId") Integer courseId,
                @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                @RequestParam(value = "materialFiles", required = false) List<MultipartFile> newMaterials,
                @ModelAttribute("lesson") LessonDto lessonDto,
                RedirectAttributes redirectAttributes) {

            User instructor = SecurityUtils.getCurrentUser();

            try {
                lessonService.updateLesson(lessonId, lessonDto, videoFile);


                if (newMaterials != null && !newMaterials.isEmpty()) {
                    lessonMaterialService.saveAllMaterial(newMaterials, lessonId, instructor);
                }

                redirectAttributes.addFlashAttribute("success", "✓ Cập nhật bài giảng thành công!");
                if("edit".equals(source)){
                    return "redirect:/instructorcourse/" + courseId + "/edit";
                }
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            }

            return "redirect:/instructorcourse/" + courseId + "/curriculum";
        }


        @PostMapping("/lessons/{lessonId}/delete")
        public String deleteLesson(
                @RequestParam("source") String source,
                @PathVariable("lessonId") Integer lessonId,
                @RequestParam("courseId") Integer courseId,
                RedirectAttributes redirectAttributes) {

            try {
                lessonService.deleteLesson(lessonId);
                redirectAttributes.addFlashAttribute("success", "✓ Xóa bài giảng thành công!");
                return "redirect:/instructorcourse/" + courseId + "/edit";
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            }

            return "redirect:/instructorcourse/" + courseId + "/curriculum";
        }

        @PostMapping("/materials/{materialId}/delete")
        public String deleteMaterial(
                @PathVariable("materialId") Integer materialId,
                @RequestParam("lessonId") Integer lessonId,
                @RequestParam("courseId") Integer courseId,
                RedirectAttributes redirectAttributes) {
            try {
                lessonMaterialService.deleteMaterialById(materialId);
                redirectAttributes.addFlashAttribute("success", "✓ Xóa tài liệu thành công!");
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            }

            return "redirect:/instructorcourse/" + courseId + "/curriculum";
        }


}

