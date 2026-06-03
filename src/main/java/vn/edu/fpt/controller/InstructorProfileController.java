package vn.edu.fpt.controller;


import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.service.CategoryService;
import vn.edu.fpt.service.UserService;

@RequestMapping("/instructor")
@Controller
public class InstructorProfileController {
     private final UserService service;
     private final CategoryService categoryService;
     private final vn.edu.fpt.mapper.DtoMapper dtoMapper;
    public InstructorProfileController(UserService userService, CategoryService categoryService, vn.edu.fpt.mapper.DtoMapper dtoMapper) {
        this.service = userService;
        this.categoryService = categoryService;
        this.dtoMapper = dtoMapper;
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model){
        //Sau có login
//       User user = (User)session.getAttribute("user");

       User tmp = service.findById(1);
       model.addAttribute("instructor", dtoMapper.toUserDto(tmp));
       return "instructor_course/course_manager";
    }

    @PostMapping("/profiles")
    public String updateProfile(
                                @RequestParam("email") String email,
                                @RequestParam("firstName") String firstName,
                                @RequestParam("lastName") String lastName,
                                @RequestParam("bio") String bio,
                                @RequestParam("avatarFile") MultipartFile avatar,
                                @RequestParam("phone") String phone,
                                RedirectAttributes redirectAttributes
                                ){
        User tmp = service.findById(1);
        service.updateProfileInstuctor(email, firstName, lastName, bio, phone, avatar);
        redirectAttributes.addFlashAttribute("sucess","Thay đổi thành công!!!");
        return "redirect:/instructor/profile";
    }
}
