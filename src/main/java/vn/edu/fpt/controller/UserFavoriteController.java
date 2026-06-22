package vn.edu.fpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.edu.fpt.dto.CategoryDto;
import vn.edu.fpt.entity.Category;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.CategoryRepository;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.service.CategoryService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.*;

@Controller
@Transactional
public class UserFavoriteController {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final DtoMapper dtoMapper;

    public UserFavoriteController(UserRepository userRepository,
                                  CategoryRepository categoryRepository,
                                  CategoryService categoryService,
                                  DtoMapper dtoMapper) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.dtoMapper = dtoMapper;
    }

    private User getSessionUser() {
        try {
            User currentUser = vn.edu.fpt.util.SecurityUtils.getCurrentUser();
            if (currentUser != null) {
                return userRepository.findById(currentUser.getId()).orElse(currentUser);
            }
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            HttpSession session = request.getSession(false);
            if (session != null) {
                User sessionUser = (User) session.getAttribute("user");
                if (sessionUser != null) {
                    return userRepository.findById(sessionUser.getId()).orElse(sessionUser);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @GetMapping("/student/favorites/step1")
    public String showStep1(Model model) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login_no";
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
            return "redirect:/login_no";
        }

        Category parent = categoryRepository.findByIdAndStatus(parentId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Category not found"));

        List<Category> childEntities = categoryRepository.findByParentIdAndStatus(parentId, "ACTIVE");
        List<CategoryDto> children = new ArrayList<>();
        for (Category child : childEntities) {
            children.add(dtoMapper.toCategoryDto(child));
        }

        Set<Integer> selectedChildIds = new HashSet<>();

        model.addAttribute("currentUser", dtoMapper.toUserDto(user));
        model.addAttribute("parent", dtoMapper.toCategoryDto(parent));
        model.addAttribute("children", children);
        model.addAttribute("selectedChildIds", selectedChildIds);

        return "recommendations/favorites_step2";
    }

    @PostMapping("/student/favorites/save")
    public String saveFavorites(@RequestParam("parentId") Integer parentId,
                                @RequestParam(value = "childIds", required = false) List<Integer> childIds,
                                HttpSession session) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login_no";
        }

        // Tải toàn bộ các category con tương ứng
        Set<Category> newFavorites = new HashSet<>();
        if (childIds != null && !childIds.isEmpty()) {
            List<Category> selectedChildren = categoryRepository.findAllById(childIds);
            newFavorites.addAll(selectedChildren);
        } else {
            // Tự động chọn tất cả danh mục con của parentId nếu người dùng không chọn gì
            List<Category> allChildren = categoryRepository.findByParentIdAndStatus(parentId, "ACTIVE");
            newFavorites.addAll(allChildren);
        }

        user.getFavoriteCategories().clear();
        user.getFavoriteCategories().addAll(newFavorites);
        user.setFavoriteSetupCompleted(true);

        User savedUser = userRepository.save(user);
        
        // Cập nhật lại session user nếu cần thiết
        if (session.getAttribute("user") != null) {
            session.setAttribute("user", savedUser);
        }

        return "redirect:/";
    }
}
