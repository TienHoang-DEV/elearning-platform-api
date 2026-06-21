package vn.edu.fpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.dto.CategoryDto;
import vn.edu.fpt.dto.CourseCreateDto;
import vn.edu.fpt.dto.CourseDto;
import vn.edu.fpt.dto.quizdto.QuizDTO;
import vn.edu.fpt.entity.Category;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.CourseLevel;
import vn.edu.fpt.service.CategoryService;
import vn.edu.fpt.service.CourseService;
import vn.edu.fpt.service.quizservice.QuizService;
import vn.edu.fpt.util.SecurityUtils;

import java.lang.reflect.Array;
import java.util.*;

@Controller
@RequestMapping("/instructorcourse")
public class InstructorCourseController {
    private final CategoryService categoryService;
    private final CourseService courseService;

    public InstructorCourseController(CategoryService categoryService, CourseService courseService) {
        this.categoryService = categoryService;
        this.courseService = courseService;
    }

    @GetMapping("/course")
    public String getPage(){
        return "instructor_course/courses";
    }

    @GetMapping("/curriculum")
    public String getCurriculumPage(){
        return "instructor_course/stepcurriculum";
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
        return "instructor_course/editcourse";
    }

    @PostMapping("/save")
    public String saveCourse(@ModelAttribute("courseRequest") CourseCreateDto courseDto,
                             BindingResult bindingResult,
                             RedirectAttributes attributes
                             ){
        try{
            User u = SecurityUtils.getCurrentUser();
            Course course = courseService.save(u, courseDto);
            attributes.addFlashAttribute("success", "Thêm khoá học thành công!");
            return "redirect:/instructorcourse/create";
        }catch(RuntimeException e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/instructorcourse/create";
        }
    }

    @GetMapping("/create-quizz")
    String quizzCreate(Model model){
        User currentUser = SecurityUtils.getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("quiz", new QuizDTO());

        return "instructor_course/quizz-create";
    }









}
