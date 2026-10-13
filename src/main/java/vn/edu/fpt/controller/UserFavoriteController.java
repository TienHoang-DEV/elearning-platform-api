package vn.edu.fpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.dto.course.CategoryDto;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.service.CategoryService;
import vn.edu.fpt.service.UserFavoriteService;

import jakarta.servlet.http.HttpSession;
import java.util.*;

@Controller
@Transactional
public class UserFavoriteController {

    private final CategoryService categoryService;
    private final UserFavoriteService userFavoriteService;
    private final DtoMapper dtoMapper;

    public UserFavoriteController(CategoryService categoryService,
                                  UserFavoriteService userFavoriteService,
                                  DtoMapper dtoMapper) {
        this.categoryService = categoryService;
        this.userFavoriteService = userFavoriteService;
        this.dtoMapper = dtoMapper;
    }

    private User getSessionUser() {
        return vn.edu.fpt.util.SecurityUtils.getCurrentUser();
    }

    @GetMapping("/student/favorites/step1")
    public String showStep1(Model model) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login";
        }

        List<CategoryDto> parents = categoryService.findByParentIsNullAndStatus("ACTIVE");
        
        model.addAttribute("currentUser", dtoMapper.toUserDto(user));
        model.addAttribute("parents", parents);

        return "recommendations/favorites_step1";
    }

    @GetMapping("/student/favorites/step2")
    public String showStep2(@RequestParam("parentId") Integer parentId, Model model) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login";
        }

        CategoryDto parent = userFavoriteService.getActiveParentCategoryDto(parentId);
        List<CategoryDto> children = userFavoriteService.getActiveChildrenCategoryDtos(parentId);
        Set<Integer> selectedChildIds = new HashSet<>();

        model.addAttribute("currentUser", dtoMapper.toUserDto(user));
        model.addAttribute("parent", parent);
        model.addAttribute("children", children);
        model.addAttribute("selectedChildIds", selectedChildIds);

        return "recommendations/favorites_step2";
    }

    @PostMapping("/student/favorites/save")
    public String saveFavorites(@RequestParam("parentId") Integer parentId,
                                @RequestParam(value = "childIds", required = false) List<Integer> childIds,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login";
        }

        User savedUser = userFavoriteService.saveUserFavorites(user, parentId, childIds);
        
        // Cập nhật lại session user nếu cần thiết
        if (session.getAttribute("user") != null) {
            session.setAttribute("user", savedUser);
        }

        return "redirect:/";
    }
}
