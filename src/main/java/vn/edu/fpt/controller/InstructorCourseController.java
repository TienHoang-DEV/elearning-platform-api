package vn.edu.fpt.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.dto.*;

import vn.edu.fpt.dto.course.CategoryDto;
import vn.edu.fpt.dto.CourseCreateDto;
import vn.edu.fpt.dto.course.CourseDto;
import vn.edu.fpt.entity.*;
import vn.edu.fpt.enums.CourseLevel;
import vn.edu.fpt.enums.CourseStatus;

import vn.edu.fpt.exception.CourseValidationException;
import vn.edu.fpt.service.CategoryService;
import vn.edu.fpt.service.section.CourseSectionService;
import vn.edu.fpt.service.CourseService;
import vn.edu.fpt.service.lesson.LessonService;
import vn.edu.fpt.util.AppConstants;
import vn.edu.fpt.util.SecurityUtils;


import javax.swing.text.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vn.edu.fpt.service.SystemLogService;
import vn.edu.fpt.enums.LogAction;

@Controller
@RequestMapping("/instructorcourse")
public class InstructorCourseController {
    private final CategoryService categoryService;
    private final CourseService courseService;
    private final CourseSectionService courseSectionService;
    private final LessonService lessonService;
    private final SystemLogService systemLogService;

    public InstructorCourseController(CategoryService categoryService, 
                                       CourseService courseService, 
                                       CourseSectionService courseSectionService, 
                                       LessonService lessonService,
                                       SystemLogService systemLogService) {
        this.categoryService = categoryService;
        this.courseService = courseService;
        this.courseSectionService = courseSectionService;
        this.lessonService = lessonService;
        this.systemLogService = systemLogService;
    }



    ///Danh sách khoá học theo từng status

    @GetMapping("/courses")
    public String getAllListCourse (@RequestParam(name = "pagePublished", defaultValue = "0") int pagePushlished,
                                    @RequestParam(name= "pageDraft", defaultValue = "0") int pageDraf,
                                    @RequestParam(name = "pageRejected", defaultValue = "0") int pageReject,
                                    @RequestParam(name = "pageHidden", defaultValue = "0") int pageHidden,
                                    @RequestParam(name = "pagePending", defaultValue = "0") int pagePending,
                                    @RequestParam(name = "pageResubmit", defaultValue = "0") int pageResubmit,
                                    @RequestParam(name = "tab", defaultValue = "all") String activeTab,
                                    Model model){
        User  user = SecurityUtils.getCurrentUser();
        Sort sort = Sort.by("updateAt").descending();
        int size = 8;
        Page<CourseDto> published = courseService.findByInstructorAndStatus(user, PageRequest.of(pagePushlished, size, sort), CourseStatus.PUBLISHED);
        Page<CourseDto> draft = courseService.findByInstructorAndStatus(user, PageRequest.of(pageDraf, size, sort), CourseStatus.DRAFT);
        Page<CourseDto> reject = courseService.findByInstructorAndStatus(user, PageRequest.of(pageReject, size, sort), CourseStatus.REJECTED);
        Page<CourseDto> hidden = courseService.findByInstructorAndStatus(user, PageRequest.of(pageHidden, size, sort), CourseStatus.HIDDEN);
        Page<CourseDto> pending = courseService.findByInstructorAndStatus(user, PageRequest.of(pagePending, size, sort), CourseStatus.PENDING);
        Page<CourseDto> resubmit = courseService.findByInstructorAndStatus(user, PageRequest.of(pageResubmit, size, sort), CourseStatus.RESUBMIT);

        loadFormModel(model);

        model.addAttribute("listpublished", published.getContent());
        model.addAttribute("publishedPage", published);

        model.addAttribute("listpending", pending.getContent());
        model.addAttribute("pendingPage", pending);

        model.addAttribute("listdraft", draft.getContent());
        model.addAttribute("draftPage", draft);

        model.addAttribute("listrejected", reject.getContent());
        model.addAttribute("rejectedPage", reject);

        model.addAttribute("listresubmit", resubmit.getContent());
        model.addAttribute("resubmitPage", resubmit);

        model.addAttribute("listhidden", hidden.getContent());
        model.addAttribute("hiddenPage", hidden);

        model.addAttribute("pagePublished", pagePushlished);
        model.addAttribute("pagePending", pagePending);
        model.addAttribute("pageDraft", pageDraf);
        model.addAttribute("pageRejected", pageReject);
        model.addAttribute("pageResubmit", pageResubmit);
        model.addAttribute("pageHidden", pageHidden);
        model.addAttribute("activeTab", activeTab);
        return "instructor_course/courses_v2";
    }


    @GetMapping("/create")
    public String getCreatePage(Model model, RedirectAttributes redirectAttributes){
        User currentUser = SecurityUtils.getCurrentUser();
        try {
            courseService.validateInstructorProfileReadyForCreateCourse(currentUser);
        } catch (CourseValidationException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/instructorcourse/courses";
        }

        List<CourseLevel> levels = Arrays.asList(CourseLevel.values());
        List<CategoryDto> categoryParentList = categoryService.findByParentIsNullAndStatus("ACTIVE".trim());
        List<CategoryDto> categoryChildList = categoryService.findByParentIsNotNulAndStatus("ACTIVE".trim());

        model.addAttribute("courseRequest", new CourseCreateDto());
        model.addAttribute("courselevels", levels);
        model.addAttribute("categoryparents", categoryParentList);
        model.addAttribute("categorychilds", categoryChildList);
        model.addAttribute("activeStep", "info");
        model.addAttribute("section", new CourseSectionDto());
        model.addAttribute("lesson", new LessonDto());
        return "instructor_course/editcourse";
    }


    private void loadFormModel(Model model){
        List<CourseLevel> levels = Arrays.asList(CourseLevel.values());
        model.addAttribute("courselevels", Arrays.asList(CourseLevel.values()));
        model.addAttribute("categoryparents",
                categoryService.findByParentIsNullAndStatus("ACTIVE"));
        model.addAttribute("categorychilds",
                categoryService.findByParentIsNotNulAndStatus("ACTIVE"));
        model.addAttribute("section", new CourseSectionDto());
        model.addAttribute("lesson", new LessonDto());
        model.addAttribute("courselevels", levels);
        model.addAttribute("urlAvatar", AppConstants.AZURE_STORAGE_BASE_URL + "/" + AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS + "/");
    }

    @PostMapping("/save")
    public String saveCourse(
            @Valid @ModelAttribute("courseRequest") CourseCreateDto courseDto,
            BindingResult bindingResult,
            Model model,
            @RequestParam(name = "trangthai") String status,
            RedirectAttributes attributes) {

        User u = SecurityUtils.getCurrentUser();

        if (bindingResult.hasErrors()) {
            loadFormModel(model);
            model.addAttribute("activeStep", "info");
            model.addAttribute("error", "Vui lòng kiểm tra lại các thông tin chưa hợp lệ.");
            return "instructor_course/editcourse";
        }

        try {
            Course saved = courseService.save(u, courseDto);
            Integer id = saved.getId();
            boolean isUpdate = courseDto.getId() != null;

            attributes.addFlashAttribute("success",
                    isUpdate ? "Cập nhật khoá học thành công!" : "Thêm khoá học thành công!");
            if (isUpdate && ("save".equals(status) || "save_publish".equals(status))) {
                return "redirect:/instructorcourse/" + id + "/submit-review";
            }
            if (!isUpdate || "save_continue".equals(status)) {
                return "redirect:/instructorcourse/" + id + "/curriculum";
            }
            return "redirect:/instructorcourse/create";

        } catch (CourseValidationException e) {
            if ("profile".equals(e.getField())) {
                attributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/instructorcourse/courses";
            }

            bindingResult.rejectValue(
                    e.getField(),
                    "error",
                    e.getMessage());

            loadFormModel(model);
            model.addAttribute("activeStep", "info");
            model.addAttribute("error", e.getMessage());
            return "instructor_course/editcourse";
        }
    }

    @GetMapping("/{courseId}/curriculum")
    public String getCurriculumPage(@PathVariable Integer courseId, Model model) {
        List<CourseSectionDto> listSection = courseSectionService.findByCourseAndLesson(courseId);
        model.addAttribute("courseId", courseId);
        model.addAttribute("activeStep", "curriculum");
        model.addAttribute("courseRequest",
                courseService.findById(courseId));
        model.addAttribute("section", new CourseSectionDto());
        model.addAttribute("lesson", new LessonDto());
        model.addAttribute("sections", listSection);
        model.addAttribute("totalLessons", courseSectionService.totalLesson(listSection));
        model.addAttribute("urlAvatar", AppConstants.AZURE_STORAGE_BASE_URL + "/" + AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS + "/");
        return "instructor_course/editcourse";

    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'MANAGER')")
    @GetMapping("/{id}/view")
    public String viewCourse(@PathVariable("id") Integer courseId, Model model)
    {
        CourseRespon courseRespon = courseService.getCourseDetailToView(courseId);
        // Trong controller
        int totalLessons = courseRespon.getSections().stream()
                .mapToInt(s -> s.getLessons().size())
                .sum();
        model.addAttribute("totalLessons", totalLessons);
        model.addAttribute("courseDetal", courseRespon);
        model.addAttribute("urlAvatar", AppConstants.AZURE_STORAGE_BASE_URL + "/" + AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS + "/");
        return "instructor_course/viewCourse";
    }

    @GetMapping("/{id}/submit-review")
    public String submitReviewPage(@PathVariable("id") Integer courseId, Model model) {
        User user = SecurityUtils.getCurrentUser();
        loadSubmitReviewModel(courseId, user, model);
        return "instructor_course/editcourse";
    }

    @GetMapping("/{id}/reviews")
    public String viewCourseReviews(@PathVariable("id") Integer courseId, Model model) {
        User user = SecurityUtils.getCurrentUser();
        Course course = courseService.getInstructorOwnedCourse(courseId, user);
        List<Feedback> reviews = courseService.getInstructorCourseReviews(courseId, user);
        // Instructor course list: truyen reviews cua dung course sang trang xem danh gia.
        model.addAttribute("course", course);
        model.addAttribute("reviews", reviews);
        addCourseReviewStats(model, reviews);
        model.addAttribute("userAvatarBaseUrl", AppConstants.AZURE_STORAGE_BASE_URL + "/" + AppConstants.AZURE_STORAGE_CONTAINER_USER_AVATARS + "/");
        return "instructor_course/course_reviews";
    }

    private void addCourseReviewStats(Model model, List<Feedback> reviews) {
        int totalRatings = 0;
        int ratingSum = 0;
        int[] starCounts = new int[6];

        for (Feedback review : reviews) {
            Integer rating = review.getRating();
            if (rating != null && rating >= 1 && rating <= 5) {
                totalRatings++;
                ratingSum += rating;
                starCounts[rating]++;
            }
        }

        double averageRating = totalRatings == 0 ? 0.0 : Math.round((ratingSum * 10.0 / totalRatings)) / 10.0;
        List<Map<String, Object>> ratingBreakdown = new ArrayList<>();
        for (int star = 5; star >= 1; star--) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("star", star);
            row.put("count", starCounts[star]);
            row.put("percent", totalRatings == 0 ? 0 : Math.round(starCounts[star] * 100.0 / totalRatings));
            ratingBreakdown.add(row);
        }

        model.addAttribute("averageRating", averageRating);
        model.addAttribute("ratingStars", (int) Math.round(averageRating));
        model.addAttribute("ratingCount", totalRatings);
        model.addAttribute("ratingBreakdown", ratingBreakdown);
    }

    @GetMapping("/{id}/rejection-reason")
    public String viewRejectionReason(@PathVariable("id") Integer courseId, Model model) {
        User user = SecurityUtils.getCurrentUser();
        Course course = courseService.getInstructorOwnedCourse(courseId, user);
        // Instructor course list: truyen ly do reject de instructor xem chi tiet.
        model.addAttribute("course", course);
        model.addAttribute("rejectionReason",
                course.getRejectionReason() != null && !course.getRejectionReason().isBlank()
                        ? course.getRejectionReason()
                        : "Manager chua nhap ly do tu choi.");
        return "instructor_course/rejection_reason";
    }

    @PostMapping("/{id}/submit-review")
    public String submitReview(@PathVariable("id") Integer courseId,
                               @RequestParam(name = "acceptPolicy", defaultValue = "false") boolean acceptPolicy,
                               @RequestParam(name = "resubmitNote", required = false) String resubmitNote,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        User user = SecurityUtils.getCurrentUser();
        try {
            Course course = courseService.findById(courseId);
            boolean isResubmit = course.getStatus() == CourseStatus.REJECTED || course.getStatus() == CourseStatus.RESUBMIT;

            courseService.submitCourseForApproval(courseId, user, acceptPolicy);

            String logMeta = (resubmitNote != null && !resubmitNote.isBlank()) 
                             ? resubmitNote 
                             : (isResubmit ? "Giảng viên đã chỉnh sửa và gửi lại yêu cầu xét duyệt khóa học." : "Giảng viên đã gửi yêu cầu xét duyệt khóa học.");

            systemLogService.log(user, isResubmit ? LogAction.RESUBMIT_COURSE : LogAction.CREATE_COURSE, "COURSE", String.valueOf(courseId), logMeta);

            redirectAttributes.addFlashAttribute("success", "Đã gửi yêu cầu xét duyệt khóa học thành công!");
            return "redirect:/instructorcourse/courses?tab=" + (isResubmit ? "resubmit" : "pending");
        } catch (CourseValidationException e) {
            model.addAttribute("error", e.getMessage());
            loadSubmitReviewModel(courseId, user, model);
            return "instructor_course/editcourse";
        }
    }

    private void loadSubmitReviewModel(Integer courseId, User user, Model model) {
        loadFormModel(model);
        model.addAttribute("activeStep", "publish");
        model.addAttribute("courseId", courseId);
        model.addAttribute("courseRequest", courseService.findById(courseId));
        model.addAttribute("submitReview", courseService.getSubmitReview(courseId, user, false));
    }

    @GetMapping("/{id}/edit")
    public String viewEditCourse(@PathVariable("id") Integer courseId, Model model){
            User u = SecurityUtils.getCurrentUser();
            CourseCreateDto dto = courseService.getCourseForEdit(courseId, u);

            List<CourseSectionDto> sections = courseSectionService.findByCourseAndLesson(courseId);

            int totalesson = courseSectionService.totalLesson(sections);
        model.addAttribute("courseRequest", dto);
        model.addAttribute("activeStep", "info");
        model.addAttribute("courseId", courseId);
        model.addAttribute("urlAvatar", AppConstants.AZURE_STORAGE_BASE_URL + "/" + AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS + "/");

        ///Object rộng để binding
        loadFormModel(model);

       return "instructor_course/editcourse";
    }

    @PostMapping("/{id}/edit")
    public String EditCourse(@PathVariable("id") Integer CourseId,
                             @Valid @ModelAttribute("courseRequest") CourseCreateDto coursedto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model){
     if(bindingResult.hasErrors()){
         loadFormModel(model);
         model.addAttribute("activeStep", "info");
         model.addAttribute("error", "Vui lòng kiểm tra lại các thông tin chưa hợp lệ.");
          model.addAttribute("CourseRequest", coursedto);
          return "instructor_course/edit_course";
     }
     try{
         User user = SecurityUtils.getCurrentUser();
         courseService.save(user, coursedto);
         redirectAttributes.addFlashAttribute("success", "Chỉnh Sửa Khoá Học Thành Công");
         return "redirect:/instructorcourse/"+coursedto.getId()+"/submit-review";
     }catch(CourseValidationException e){

         bindingResult.rejectValue(
                 e.getField(),
                 "error",
                 e.getMessage());

          loadFormModel(model);
          model.addAttribute("activeStep", "info");
          model.addAttribute("error", e.getMessage());
          model.addAttribute("CourseRequest", coursedto);
          return "instructor_course/edit_course";
     }

    }


    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable("id") Integer courseId,
                               @RequestParam(name = "tab", required = false, defaultValue = "all") String tab,
                               RedirectAttributes redirectAttributes){
        User user = SecurityUtils.getCurrentUser();
        try {
            courseService.deleteCourseById(courseId, user);
            redirectAttributes.addFlashAttribute("success", "Xoá khoá học thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xoá khoá học: " + e.getMessage());
        }
        return "redirect:/instructorcourse/courses?tab=" + tab;
    }

    @PostMapping("/{id}/hide")
    public String hideCourse(@PathVariable("id") Integer courseId,
                             RedirectAttributes redirectAttributes) {
        User user = SecurityUtils.getCurrentUser();
        courseService.hidePublishedCourse(courseId, user);
        redirectAttributes.addFlashAttribute("success", "Da an khoa hoc khoi danh sach dang ban.");
        return "redirect:/instructorcourse/courses?tab=all";
    }

    @PostMapping("/{id}/publish")
    public String publishHiddenCourse(@PathVariable("id") Integer courseId,
                                      RedirectAttributes redirectAttributes) {
        User user = SecurityUtils.getCurrentUser();
        courseService.publishHiddenCourse(courseId, user);
        redirectAttributes.addFlashAttribute("success", "Da hien lai khoa hoc.");
        return "redirect:/instructorcourse/courses?tab=hidden";
    }

}
