package vn.edu.fpt.controller;

import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.entity.Category;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.CategoryStatus;
import vn.edu.fpt.enums.CourseLevel;
import vn.edu.fpt.service.CategoryService;
import vn.edu.fpt.service.CourseService;
import vn.edu.fpt.service.UserService;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/courses")
public class CRUDCourseControler {
    private UserService userService;
    private AuthController authController;
    private CategoryService categoryService;
    private CourseService courseService;

    public CRUDCourseControler(UserService userService, AuthController authController, CategoryService categoryService, CourseService courseService) {
        this.userService = userService;
        this.authController = authController;
        this.categoryService = categoryService;
        this.courseService = courseService;
    }


    @PostMapping("/save")
    @Transactional
    public String saveCourse(@RequestParam("title") String title,
                             @RequestParam("shortDesc") String shortdesc,
                             @RequestParam("description") String desc,
                             @RequestParam("outcomes") String outcome,
                             @RequestParam("requirements") String requirement,
                             @RequestParam("level") CourseLevel level,
                             @RequestParam("categoryId") Integer categoryId,
                             @RequestParam(value = "price", required = false) BigDecimal price,
                             @RequestParam("thumbnailFile")MultipartFile file,
                              RedirectAttributes redirectAttributes){

       User user = userService.getCurrentUser();
       courseService.save(user, title, shortdesc, desc, outcome, requirement, level, categoryId, file, price);
       return "redirect:/courses";
    }

    @GetMapping("/listcourses")
    public String listCourseByStatus(Model model, @RequestParam(value = "status", defaultValue = "PUBLISHED") String status){
        User user = userService.getCurrentUser();
        model.addAttribute("currentStatus", status);
        model.addAttribute("courses", courseService.findByInstructorAndStatus(user, status));
        return "instructor_course/course_manager";
    }
}
