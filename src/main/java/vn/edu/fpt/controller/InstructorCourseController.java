package vn.edu.fpt.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/instructorcourse")
public class InstructorCourseController {
    private CategoryService categoryService;
    private CourseService courseService;
    private CourseSectionService courseSectionService;
    private LessonService lessonService;

    public InstructorCourseController(CategoryService categoryService, CourseService courseService, CourseSectionService courseSectionService, LessonService lessonService) {
        this.categoryService = categoryService;
        this.courseService = courseService;
        this.courseSectionService = courseSectionService;
        this.lessonService = lessonService;
    }



    ///Danh sách khoá học theo từng status

    @GetMapping("/courses")
    public String getAllListCourse (@RequestParam(name = "pagePublished", defaultValue = "0") int pagePushlished,
                                    @RequestParam(name= "pageDraft", defaultValue = "0") int pageDraf,
                                    @RequestParam(name = "pageRejected", defaultValue = "0") int pageReject,
                                    @RequestParam(name = "pageHidden", defaultValue = "0") int pageHidden,
                                    @RequestParam(name = "pagePending", defaultValue = "0") int pagePending,
                                    Model model){
        User  user = SecurityUtils.getCurrentUser();
        Sort sort = Sort.by("updateAt").descending();
        int size = 8;
        Page<CourseDto> published = courseService.findByInstructorAndStatus(user, PageRequest.of(pagePushlished, size, sort), CourseStatus.PUBLISHED);
        Page<CourseDto> draft = courseService.findByInstructorAndStatus(user, PageRequest.of(pageDraf, size, sort), CourseStatus.DRAFT);
        Page<CourseDto> reject = courseService.findByInstructorAndStatus(user, PageRequest.of(pageReject, size, sort), CourseStatus.REJECTED);
        Page<CourseDto> hidden = courseService.findByInstructorAndStatus(user, PageRequest.of(pageHidden, size, sort), CourseStatus.HIDDEN);
        Page<CourseDto> pending = courseService.findByInstructorAndStatus(user, PageRequest.of(pagePending, size, sort), CourseStatus.PENDING);

        loadFormModel(model);

        model.addAttribute("listpublished", published.getContent());
        model.addAttribute("publishedPage", published);

        model.addAttribute("listpending", pending.getContent());
        model.addAttribute("pendingPage", pending);

        model.addAttribute("listdraft", draft.getContent());
        model.addAttribute("draftPage", draft);

        model.addAttribute("listrejected", reject.getContent());
        model.addAttribute("rejectedPage", reject);

        model.addAttribute("listhidden", hidden.getContent());
        model.addAttribute("hiddenPage", hidden);

        model.addAttribute("pagePublished", pagePushlished);
        model.addAttribute("pagePending", pagePending);
        model.addAttribute("pageDraft", pageDraf);
        model.addAttribute("pageRejected", pageReject);
        model.addAttribute("pageHidden", pageHidden);
        return "instructor_course/courses_v2";
    }


    @GetMapping("/create")
    public String getCreatePage(Model model){
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
        model.addAttribute("courselevels", Arrays.asList(CourseLevel.values()));
        model.addAttribute("categoryparents",
                categoryService.findByParentIsNullAndStatus("ACTIVE"));
        model.addAttribute("categorychilds",
                categoryService.findByParentIsNotNulAndStatus("ACTIVE"));
        model.addAttribute("section", new CourseSectionDto());
        model.addAttribute("lesson", new LessonDto());
        model.addAttribute("urlAvatar", AppConstants.AZURE_STORAGE_BASE_URL + "/" + AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS + "/");
    }

    @PostMapping("/save")
    public String saveCourse(
            @Valid @ModelAttribute("courseRequest") CourseCreateDto courseDto,
            BindingResult bindingResult,
            Model model,
            @RequestParam(name = "trangthai") String status,
            RedirectAttributes attributes) {

        if (bindingResult.hasErrors()) {
            loadFormModel(model);
            model.addAttribute("activeStep", "info");
            return "instructor_course/editcourse";
        }

        try {
            User u = SecurityUtils.getCurrentUser();
            Course saved = courseService.save(u, courseDto);
            Integer id = saved.getId();

            attributes.addFlashAttribute("success",
                    "Thêm khoá học thành công!");
            if ("save_continue".equals(status)) {
                return "redirect:/instructorcourse/" + id + "/curriculum";
            }
            return "redirect:/instructorcourse/create";

        } catch (CourseValidationException e) {

            bindingResult.rejectValue(
                    e.getField(),
                    "error",
                    e.getMessage());

            loadFormModel(model);
            model.addAttribute("activeStep", "info");
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

    @GetMapping("/{id}/edit")
    public String viewEditCourse(@PathVariable("id") Integer courseId, Model model){
            User u = SecurityUtils.getCurrentUser();
            CourseCreateDto dto = courseService.getCourseForEdit(courseId, u);

            List<CourseSectionDto> sections = courseSectionService.findByCourseAndLesson(courseId);

            int totalesson = courseSectionService.totalLesson(sections);

        model.addAttribute("CourseRequest", dto);
        model.addAttribute("sections", sections);
        model.addAttribute("totalLessons", totalesson);
        model.addAttribute("courseId", courseId);
        model.addAttribute("urlAvatar", AppConstants.AZURE_STORAGE_BASE_URL + "/" + AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS + "/");

        ///Object rộng để binding
        loadFormModel(model);

       return "instructor_course/edit_course";
    }

    @PostMapping("/{id}/edit")
    public String EditCourse(@PathVariable("id") Integer CourseId,
                             @Valid @ModelAttribute("CourseRequest") CourseCreateDto coursedto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model){
     if(bindingResult.hasErrors()){
         loadFormModel(model);
         model.addAttribute("activeStep", "info");
         return "instructor_course/edit_course";
     }
     try{
         User user = SecurityUtils.getCurrentUser();
         courseService.save(user, coursedto);
         redirectAttributes.addFlashAttribute("success", "Chỉnh Sửa Khoá Học Thành Công");
         return "redirect:/instructorcourse/"+coursedto.getId()+"/edit";
     }catch(CourseValidationException e){

         bindingResult.rejectValue(
                 e.getField(),
                 "error",
                 e.getMessage());

         loadFormModel(model);
         model.addAttribute("activeStep", "info");
         return "instructor_course/edit_course";
     }

    }


    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable("id") Integer courseId,
                               @RequestParam(name = "tab", required = false, defaultValue = "all") String tab,
                               RedirectAttributes redirectAttributes){
        courseService.deleteCourseById(courseId);
        redirectAttributes.addFlashAttribute("success", "Xoá khoá học thành công");
        return "redirect:/instructorcourse/courses?tab=" + tab;
    }







}
